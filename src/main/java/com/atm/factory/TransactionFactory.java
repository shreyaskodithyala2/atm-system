package com.atm.factory;

import com.atm.model.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ============================================================
 * CREATIONAL PATTERN 2 — FACTORY PATTERN
 * ============================================================
 * WHAT IS FACTORY?
 * The Factory Pattern delegates object CREATION to a dedicated
 * class instead of scattering `new SomeClass()` calls throughout
 * the codebase. The caller asks the factory WHAT it wants;
 * the factory decides HOW to build it.
 *
 * WHY IT FITS HERE:
 * Every transaction type (Withdraw, Deposit, Transfer, Bill)
 * needs the same 5 fields set on creation:
 *   transactionId (UUID), timestamp, amount, session, account
 *
 * Before this Factory, TransactionService repeated those 5
 * lines for EVERY transaction type — 4 × 5 = 20 lines of
 * boilerplate. The Factory centralises that initialisation,
 * meaning there is now ONE place to change if, for example,
 * the ID format changes from UUID to a bank-specific format.
 *
 * HOW IT IS USED:
 *   // Before Factory (in TransactionService):
 *   WithdrawTransaction tx = new WithdrawTransaction();
 *   tx.setTransactionId(UUID.randomUUID().toString());
 *   tx.setTimestamp(LocalDateTime.now());
 *   tx.setAmount(amount);
 *   tx.setSession(session);
 *   tx.setAccount(account);
 *
 *   // After Factory:
 *   WithdrawTransaction tx = TransactionFactory.createWithdraw(amount, session, account);
 *
 * OPEN/CLOSED synergy: adding a new transaction type only
 * requires adding ONE new factory method here — callers stay
 * unchanged.
 *
 * STRUCTURE:
 *   TransactionFactory
 *     + createWithdraw(amount, session, account)   → WithdrawTransaction
 *     + createDeposit(amount, type, session, acct) → DepositTransaction
 *     + createTransfer(amount, target, session, a) → TransferTransaction
 *     + createBillPayment(amt, id, name, sess, a)  → BillPayment
 *     - initCommonFields(tx, amount, session, acct) [private helper]
 */
public class TransactionFactory {

    /**
     * Factory: private constructor — this class is never instantiated.
     * All methods are static factory methods.
     */
    private TransactionFactory() {}

    /**
     * Creates a fully initialised WithdrawTransaction.
     * Common fields (ID, timestamp, amount, session, account)
     * are set by the private helper — the caller only provides
     * the domain values it actually knows about.
     */
    public static WithdrawTransaction createWithdraw(double amount,
                                                     ATMSession session,
                                                     Account account) {
        WithdrawTransaction tx = new WithdrawTransaction();
        initCommonFields(tx, amount, session, account);
        return tx;
    }

    /**
     * Creates a fully initialised DepositTransaction.
     * depositType ("CASH" or "CHEQUE") is set in addition
     * to the common fields.
     */
    public static DepositTransaction createDeposit(double amount,
                                                   String depositType,
                                                   ATMSession session,
                                                   Account account) {
        DepositTransaction tx = new DepositTransaction();
        initCommonFields(tx, amount, session, account);
        tx.setDepositType(depositType);   // deposit-specific field
        return tx;
    }

    /**
     * Creates a fully initialised TransferTransaction.
     * targetAccountNumber is set in addition to common fields.
     */
    public static TransferTransaction createTransfer(double amount,
                                                     String targetAccountNumber,
                                                     ATMSession session,
                                                     Account account) {
        TransferTransaction tx = new TransferTransaction();
        initCommonFields(tx, amount, session, account);
        tx.setTargetAccountNumber(targetAccountNumber); // transfer-specific field
        return tx;
    }

    /**
     * Creates a fully initialised BillPayment.
     * billerId and billerName are set in addition to common fields.
     */
    public static BillPayment createBillPayment(double amount,
                                                String billerId,
                                                String billerName,
                                                ATMSession session,
                                                Account account) {
        BillPayment tx = new BillPayment();
        initCommonFields(tx, amount, session, account);
        tx.setBillerId(billerId);       // bill-specific fields
        tx.setBillerName(billerName);
        return tx;
    }

    /**
     * FACTORY private helper — sets the 5 fields every transaction
     * needs. Centralised here so the logic is never duplicated.
     * If the ID format ever changes, this is the ONLY place to edit.
     */
    private static void initCommonFields(Transaction tx,
                                         double amount,
                                         ATMSession session,
                                         Account account) {
        tx.setTransactionId(UUID.randomUUID().toString());
        tx.setTimestamp(LocalDateTime.now());
        tx.setAmount(amount);
        tx.setSession(session);
        tx.setAccount(account);
    }
}
