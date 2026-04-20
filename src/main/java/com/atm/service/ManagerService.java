package com.atm.service;

import com.atm.model.*;
import com.atm.model.enums.TransactionStatus;
import com.atm.repository.AccountRepository;
import com.atm.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ManagerService {

    @Autowired private TransactionRepository transactionRepository;
    @Autowired private AccountRepository accountRepository;

    public List<Transaction> getPendingApprovals() {
        return transactionRepository.findByStatusOrderByTimestampDesc(TransactionStatus.AWAITING_APPROVAL);
    }

    @Transactional
    public boolean approveLargeTransaction(String transactionId) {
        Transaction tx = transactionRepository.findById(transactionId).orElse(null);
        if (tx == null || tx.getStatus() != TransactionStatus.AWAITING_APPROVAL) return false;

        Account account = tx.getAccount();
        if (!account.verifyFunds(tx.getAmount())) {
            tx.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(tx);
            return false;
        }

        account.debit(tx.getAmount());
        accountRepository.save(account);

        if (tx instanceof TransferTransaction transferTx) {
            accountRepository.findByAccountNumber(transferTx.getTargetAccountNumber())
                    .ifPresent(target -> {
                        target.credit(tx.getAmount());
                        accountRepository.save(target);
                    });
        }

        tx.setStatus(TransactionStatus.COMPLETED);
        transactionRepository.save(tx);
        return true;
    }

    @Transactional
    public boolean declineLargeTransaction(String transactionId) {
        Transaction tx = transactionRepository.findById(transactionId).orElse(null);
        if (tx == null || tx.getStatus() != TransactionStatus.AWAITING_APPROVAL) return false;
        tx.setStatus(TransactionStatus.DECLINED);
        transactionRepository.save(tx);
        return true;
    }

    public List<Transaction> auditDailyTransactions(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);
        return transactionRepository.findByTimestampBetweenOrderByTimestampDesc(start, end);
    }

    public long getPendingCount() {
        return transactionRepository.countByStatus(TransactionStatus.AWAITING_APPROVAL);
    }

    public long getTodayTransactionCount() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(23, 59, 59);
        return transactionRepository.countByTimestampBetween(start, end);
    }
}
