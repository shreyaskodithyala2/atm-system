package com.atm.service;

import com.atm.model.*;
import com.atm.model.enums.SessionState;
import com.atm.model.enums.TransactionStatus;
import com.atm.repository.AccountRepository;
import com.atm.repository.ATMSessionRepository;
import com.atm.repository.TransactionRepository;
import com.atm.service.external.BankCoreBankingService;
import com.atm.service.external.ExternalCardNetworkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TransactionService {

    @Autowired private TransactionRepository transactionRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private ATMSessionRepository sessionRepository;
    @Autowired private ExternalCardNetworkService cardNetworkService;
    @Autowired private BankCoreBankingService coreBankingService;

    @Value("${atm.transaction.large-threshold:5000.0}")
    private double largeTransactionThreshold;

    public enum TransactionResult { SUCCESS, INSUFFICIENT_FUNDS, AWAITING_APPROVAL, INVALID_ACCOUNT, FAILED }

    @Transactional
    public TransactionResult processWithdrawal(String sessionId, double amount) {
        ATMSession session = getActiveSession(sessionId);
        Customer customer = session.getCustomer();
        Account account = customer.getAccount();

        if (!account.verifyFunds(amount)) return TransactionResult.INSUFFICIENT_FUNDS;

        if (!cardNetworkService.authorizeTransaction(customer.getCardNumber(), amount)) {
            return TransactionResult.FAILED;
        }

        WithdrawTransaction tx = new WithdrawTransaction();
        tx.setTransactionId(UUID.randomUUID().toString());
        tx.setTimestamp(LocalDateTime.now());
        tx.setAmount(amount);
        tx.setSession(session);
        tx.setAccount(account);

        if (amount > largeTransactionThreshold) {
            tx.setStatus(TransactionStatus.AWAITING_APPROVAL);
            transactionRepository.save(tx);
            session.setState(SessionState.AWAITING_APPROVAL);
            sessionRepository.save(session);
            return TransactionResult.AWAITING_APPROVAL;
        }

        account.debit(amount);
        accountRepository.save(account);
        tx.setStatus(TransactionStatus.COMPLETED);
        transactionRepository.save(tx);
        coreBankingService.processTransaction(tx.getTransactionId(), account.getAccountNumber(), amount, "WITHDRAW");

        session.setState(SessionState.TRANSACTION_COMPLETE);
        sessionRepository.save(session);
        return TransactionResult.SUCCESS;
    }

    @Transactional
    public TransactionResult processDeposit(String sessionId, double amount, String depositType) {
        ATMSession session = getActiveSession(sessionId);
        Account account = session.getCustomer().getAccount();

        account.credit(amount);
        accountRepository.save(account);

        DepositTransaction tx = new DepositTransaction();
        tx.setTransactionId(UUID.randomUUID().toString());
        tx.setTimestamp(LocalDateTime.now());
        tx.setAmount(amount);
        tx.setStatus(TransactionStatus.COMPLETED);
        tx.setDepositType(depositType);
        tx.setSession(session);
        tx.setAccount(account);
        transactionRepository.save(tx);

        coreBankingService.processTransaction(tx.getTransactionId(), account.getAccountNumber(), amount, "DEPOSIT");
        session.setState(SessionState.TRANSACTION_COMPLETE);
        sessionRepository.save(session);
        return TransactionResult.SUCCESS;
    }

    @Transactional
    public TransactionResult processTransfer(String sessionId, String targetAccountNumber, double amount) {
        ATMSession session = getActiveSession(sessionId);
        Account sourceAccount = session.getCustomer().getAccount();

        if (!coreBankingService.verifyAccount(targetAccountNumber)) return TransactionResult.INVALID_ACCOUNT;
        if (!sourceAccount.verifyFunds(amount)) return TransactionResult.INSUFFICIENT_FUNDS;

        Account targetAccount = accountRepository.findByAccountNumber(targetAccountNumber)
                .orElse(null);
        if (targetAccount == null) return TransactionResult.INVALID_ACCOUNT;

        if (amount > largeTransactionThreshold) {
            TransferTransaction tx = buildTransferTx(session, sourceAccount, targetAccountNumber, amount);
            tx.setStatus(TransactionStatus.AWAITING_APPROVAL);
            transactionRepository.save(tx);
            session.setState(SessionState.AWAITING_APPROVAL);
            sessionRepository.save(session);
            return TransactionResult.AWAITING_APPROVAL;
        }

        sourceAccount.debit(amount);
        targetAccount.credit(amount);
        accountRepository.save(sourceAccount);
        accountRepository.save(targetAccount);

        TransferTransaction tx = buildTransferTx(session, sourceAccount, targetAccountNumber, amount);
        tx.setStatus(TransactionStatus.COMPLETED);
        transactionRepository.save(tx);

        coreBankingService.processTransaction(tx.getTransactionId(), sourceAccount.getAccountNumber(), amount, "TRANSFER");
        session.setState(SessionState.TRANSACTION_COMPLETE);
        sessionRepository.save(session);
        return TransactionResult.SUCCESS;
    }

    @Transactional
    public TransactionResult processBillPayment(String sessionId, String billerId, String billerName, double amount) {
        ATMSession session = getActiveSession(sessionId);
        Account account = session.getCustomer().getAccount();

        if (!account.verifyFunds(amount)) return TransactionResult.INSUFFICIENT_FUNDS;

        account.debit(amount);
        accountRepository.save(account);

        BillPayment tx = new BillPayment();
        tx.setTransactionId(UUID.randomUUID().toString());
        tx.setTimestamp(LocalDateTime.now());
        tx.setAmount(amount);
        tx.setStatus(TransactionStatus.COMPLETED);
        tx.setBillerId(billerId);
        tx.setBillerName(billerName);
        tx.setSession(session);
        tx.setAccount(account);
        transactionRepository.save(tx);

        coreBankingService.processTransaction(tx.getTransactionId(), account.getAccountNumber(), amount, "BILL");
        session.setState(SessionState.TRANSACTION_COMPLETE);
        sessionRepository.save(session);
        return TransactionResult.SUCCESS;
    }

    /** Returns the most recent completed transaction for a session (for the receipt page). */
    public Transaction getLastCompletedTransaction(String sessionId) {
        return transactionRepository.findBySessionIdAndStatus(sessionId, TransactionStatus.COMPLETED)
                .stream().findFirst().orElse(null);
    }

    /** Returns the most recent awaiting-approval transaction for a session (for the awaiting page). */
    public Transaction getLastPendingTransaction(String sessionId) {
        return transactionRepository.findBySessionIdAndStatus(sessionId, TransactionStatus.AWAITING_APPROVAL)
                .stream().findFirst().orElse(null);
    }

    /** Returns the most recent transaction of any status (for the receipt page after redirect). */
    public Transaction getLastTransaction(String sessionId) {
        Transaction completed = getLastCompletedTransaction(sessionId);
        return completed != null ? completed : getLastPendingTransaction(sessionId);
    }

    private ATMSession getActiveSession(String sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));
    }

    private TransferTransaction buildTransferTx(ATMSession session, Account account, String targetAccNum, double amount) {
        TransferTransaction tx = new TransferTransaction();
        tx.setTransactionId(UUID.randomUUID().toString());
        tx.setTimestamp(LocalDateTime.now());
        tx.setAmount(amount);
        tx.setTargetAccountNumber(targetAccNum);
        tx.setSession(session);
        tx.setAccount(account);
        return tx;
    }
}
