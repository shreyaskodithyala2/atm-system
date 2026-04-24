package com.atm.model;

import com.atm.model.enums.TransactionStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * ============================================================
 * SOLID PRINCIPLE 2 — OPEN/CLOSED PRINCIPLE (OCP)
 * ============================================================
 * Transaction is CLOSED for modification — the core fields
 * (id, amount, status, session, account) never change.
 * It is OPEN for extension — new transaction types such as
 * WithdrawTransaction, DepositTransaction, TransferTransaction,
 * and BillPayment are added by subclassing, not by editing here.
 *
 * Adding a brand-new transaction type (e.g. CryptoTransaction)
 * requires ZERO changes to this class.
 *
 * ============================================================
 * DESIGN PATTERN 1 — TEMPLATE METHOD PATTERN
 * ============================================================
 * This abstract class defines the SKELETON (template) for all
 * transactions. The abstract method execute() is the "hook"
 * that every subclass MUST implement with its own logic.
 *
 * The template guarantees: every transaction has an ID,
 * a timestamp, an amount, a status, and a printReceipt().
 * Only the execution step differs between subtypes.
 *
 * execute() ← template method (hook)
 *   └── WithdrawTransaction.execute()  → verify funds + dispense cash
 *   └── DepositTransaction.execute()   → accept and credit cash
 *   └── TransferTransaction.execute()  → debit source, credit target
 *   └── BillPayment.execute()          → debit and pay biller
 *
 * ============================================================
 * DESIGN PATTERN 2 — STRATEGY PATTERN
 * ============================================================
 * Each concrete subclass is a different STRATEGY for processing
 * a transaction. TransactionService selects the appropriate
 * strategy at runtime based on the user's menu choice.
 *
 * Without Strategy, TransactionService would need a giant
 * if-else block. With it, each strategy is self-contained
 * and swappable.
 *
 * Context       → TransactionService
 * Strategy IF   → Transaction (abstract)
 * Strategies    → WithdrawTransaction, DepositTransaction,
 *                 TransferTransaction, BillPayment
 */
@Entity
@Table(name = "transactions")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype", discriminatorType = DiscriminatorType.STRING)
public abstract class Transaction {

    @Id
    @Column(name = "transaction_id")
    private String transactionId;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private double amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private ATMSession session;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id")
    private Account account;

    /**
     * TEMPLATE METHOD (hook step) + STRATEGY entry point.
     *
     * Template Method: This abstract method is the one step in
     * the transaction lifecycle that varies per type. Every other
     * step (id, timestamp, status, receipt) is shared.
     *
     * Strategy: Each concrete class provides its own algorithm
     * here — e.g. WithdrawTransaction verifies funds and dispenses
     * cash; DepositTransaction credits the account instead.
     */
    public abstract boolean execute();

    /**
     * Shared template behaviour — returns a human-readable label.
     * All subtypes inherit this without repeating code. (OCP)
     */
    public String getType() {
        String name = this.getClass().getSimpleName();
        name = name.replace("Transaction", "");
        if (name.isEmpty()) name = "Transaction";
        return name.replaceAll("([A-Z])", " $1").trim();
    }

    /** Convenience helpers for Thymeleaf type-checking (LSP — safe downcasting). */
    public boolean isTransfer()    { return this instanceof TransferTransaction; }
    public boolean isBillPayment() { return this instanceof BillPayment; }
    public boolean isWithdrawal()  { return this instanceof WithdrawTransaction; }
    public boolean isDeposit()     { return this instanceof DepositTransaction; }

    /** Shared receipt format — defined once in the template, used by all subtypes. */
    public String printReceipt() {
        return String.format(
            "Transaction ID: %s%nType: %s%nAmount: $%.2f%nStatus: %s%nDate: %s",
            transactionId, getClass().getSimpleName(), amount, status, timestamp
        );
    }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }
    public ATMSession getSession() { return session; }
    public void setSession(ATMSession session) { this.session = session; }
    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }
}
