package com.atm.service;

import com.atm.factory.TransactionFactory;
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

/**
 * ============================================================
 * SOLID PRINCIPLE 1 — SINGLE RESPONSIBILITY PRINCIPLE (SRP)
 * ============================================================
 * TransactionService has ONE reason to change: transaction
 * processing logic. It does NOT handle authentication (that is
 * AuthenticationService), does NOT manage cards or firmware
 * (AdminService), and does NOT handle approvals or audits
 * (ManagerService). Each service owns exactly one concern.
 *
 * ============================================================
 * SOLID PRINCIPLE 5 — DEPENDENCY INVERSION PRINCIPLE (DIP)
 * ============================================================
 * This class depends on ABSTRACTIONS (interfaces), not on
 * concrete implementations:
 *   - TransactionRepository  (Spring Data interface)
 *   - AccountRepository      (Spring Data interface)
 *   - ATMSessionRepository   (Spring Data interface)
 *   - ExternalCardNetworkService (can be swapped for any impl)
 *   - BankCoreBankingService     (can be swapped for any impl)
 *
 * Spring injects the concrete beans at runtime via @Autowired.
 * This class never calls `new SomeRepository()` directly.
 *
 * ============================================================
 * DESIGN PATTERN 3 — FACADE PATTERN
 * ============================================================
 * TransactionService is a FACADE. It hides the complexity of:
 *   • Validating funds (Account)
 *   • Authorising via an external card network
 *   • Persisting Transaction and Account changes
 *   • Notifying the core banking system
 *   • Updating the ATM session state
 *
 * Controllers call ONE simple method (processWithdrawal,
 * processDeposit, etc.) and get back a single result enum.
 * They know nothing about repositories or external services.
 *
 * Facade:    TransactionService
 * Subsystem: TransactionRepository, AccountRepository,
 *            ATMSessionRepository, ExternalCardNetworkService,
 *            BankCoreBankingService
 */
@Service  // Spring manages this as a Singleton — one shared instance for the whole app
public class TransactionService {

    // DIP: All dependencies are injected as interfaces, not concrete classes
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private ATMSessionRepository sessionRepository;
    @Autowired private ExternalCardNetworkService cardNetworkService;  // DIP: interface
    @Autowired private BankCoreBankingService coreBankingService;      // DIP: interface

    @Value("${atm.transaction.large-threshold:5000.0}")
    private double largeTransactionThreshold;

    /** Result codes returned to controllers — hides internal detail (Facade). */
    public enum TransactionResult { SUCCESS, INSUFFICIENT_FUNDS, AWAITING_APPROVAL, INVALID_ACCOUNT, FAILED }

    /**
     * FACADE method — caller just says "withdraw $X from session Y".
     * Internally coordinates: fund check → card auth → transaction
     * creation → large-tx check → debit → core banking notify → session update.
     *
     * STRATEGY: creates a WithdrawTransaction (the withdraw strategy).
     */
    @Transactional
    public TransactionResult processWithdrawal(String sessionId, double amount) {
        ATMSession session = getActiveSession(sessionId);
        Customer customer = session.getCustomer();
        Account account = customer.getAccount();

        if (!account.verifyFunds(amount)) return TransactionResult.INSUFFICIENT_FUNDS;

        if (!cardNetworkService.authorizeTransaction(customer.getCardNumber(), amount)) {
            return TransactionResult.FAILED;
        }

        // FACTORY: delegate object creation to TransactionFactory.
        // ID, timestamp, amount, session, account are all set inside the factory.
        WithdrawTransaction tx = TransactionFactory.createWithdraw(amount, session, account);

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

    /**
     * FACADE method — hides account crediting, persistence, and core banking.
     * STRATEGY: creates a DepositTransaction (the deposit strategy).
     */
    @Transactional
    public TransactionResult processDeposit(String sessionId, double amount, String depositType) {
        ATMSession session = getActiveSession(sessionId);
        Account account = session.getCustomer().getAccount();

        account.credit(amount);
        accountRepository.save(account);

        // FACTORY: creates DepositTransaction with all common fields pre-set
        DepositTransaction tx = TransactionFactory.createDeposit(amount, depositType, session, account);
        tx.setStatus(TransactionStatus.COMPLETED);
        transactionRepository.save(tx);

        coreBankingService.processTransaction(tx.getTransactionId(), account.getAccountNumber(), amount, "DEPOSIT");
        session.setState(SessionState.TRANSACTION_COMPLETE);
        sessionRepository.save(session);
        return TransactionResult.SUCCESS;
    }

    /**
     * FACADE method — hides account verification, dual-account debit/credit,
     * large-tx hold, and core banking coordination.
     * STRATEGY: creates a TransferTransaction (the transfer strategy).
     */
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
            // FACTORY: creates TransferTransaction with all common fields pre-set
            TransferTransaction tx = TransactionFactory.createTransfer(amount, targetAccountNumber, session, sourceAccount);
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

        // FACTORY: creates TransferTransaction with all common fields pre-set
        TransferTransaction tx = TransactionFactory.createTransfer(amount, targetAccountNumber, session, sourceAccount);
        tx.setStatus(TransactionStatus.COMPLETED);
        transactionRepository.save(tx);

        coreBankingService.processTransaction(tx.getTransactionId(), sourceAccount.getAccountNumber(), amount, "TRANSFER");
        session.setState(SessionState.TRANSACTION_COMPLETE);
        sessionRepository.save(session);
        return TransactionResult.SUCCESS;
    }

    /**
     * FACADE method — hides fund check, debit, bill persistence, and core banking.
     * STRATEGY: creates a BillPayment (the bill-pay strategy).
     */
    @Transactional
    public TransactionResult processBillPayment(String sessionId, String billerId, String billerName, double amount) {
        ATMSession session = getActiveSession(sessionId);
        Account account = session.getCustomer().getAccount();

        if (!account.verifyFunds(amount)) return TransactionResult.INSUFFICIENT_FUNDS;

        account.debit(amount);
        accountRepository.save(account);

        // FACTORY: creates BillPayment with all common fields pre-set
        BillPayment tx = TransactionFactory.createBillPayment(amount, billerId, billerName, session, account);
        tx.setStatus(TransactionStatus.COMPLETED);
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

    /** Returns the most recent transaction of any status (fallback). */
    public Transaction getLastTransaction(String sessionId) {
        Transaction completed = getLastCompletedTransaction(sessionId);
        return completed != null ? completed : getLastPendingTransaction(sessionId);
    }

    // DIP: depends on the ATMSessionRepository interface — Spring injects the concrete JPA impl
    private ATMSession getActiveSession(String sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));
    }

}
