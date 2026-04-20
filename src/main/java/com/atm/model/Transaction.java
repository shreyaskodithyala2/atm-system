package com.atm.model;

import com.atm.model.enums.TransactionStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

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

    public abstract boolean execute();

    /** Returns a human-readable type label for use in templates. */
    public String getType() {
        String name = this.getClass().getSimpleName();
        name = name.replace("Transaction", "");
        if (name.isEmpty()) name = "Transaction";
        // Convert camel-case to spaced words: e.g. BillPayment → Bill Payment
        return name.replaceAll("([A-Z])", " $1").trim();
    }

    /** Convenience for Thymeleaf type-checking without SpEL T() operator. */
    public boolean isTransfer() { return this instanceof TransferTransaction; }
    public boolean isBillPayment() { return this instanceof BillPayment; }
    public boolean isWithdrawal() { return this instanceof WithdrawTransaction; }
    public boolean isDeposit() { return this instanceof DepositTransaction; }

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
