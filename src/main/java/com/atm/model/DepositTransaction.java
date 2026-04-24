package com.atm.model;

import com.atm.hardware.ATMHardwareController;
import jakarta.persistence.*;

/**
 * DESIGN PATTERN — TEMPLATE METHOD (Concrete Step)
 * DESIGN PATTERN — STRATEGY (Concrete Strategy: Deposit)
 *
 * CREATIONAL PATTERN 1 — SINGLETON (usage site)
 * processDeposit() uses ATMHardwareController.getInstance()
 * to instruct the physical cash acceptor to receive the notes,
 * then credits the account in the database.
 */
@Entity
@DiscriminatorValue("DEPOSIT")
public class DepositTransaction extends Transaction {

    @Column(name = "deposit_type")
    private String depositType;

    /**
     * TEMPLATE METHOD hook — concrete deposit algorithm:
     *   Step 1: Hardware (Singleton) accepts the physical cash.
     *   Step 2: Credit the account in the database.
     */
    @Override
    public boolean execute() {
        return processDeposit();
    }

    /**
     * SINGLETON usage: the cash acceptor slot is part of the
     * single ATMHardwareController instance.
     * After hardware confirms receipt, the account is credited.
     */
    public boolean processDeposit() {
        if (getAccount() == null) return false;

        // SINGLETON: instruct the one physical cash acceptor
        boolean hardwareAccepted = ATMHardwareController.getInstance().acceptCashDeposit(getAmount());
        if (!hardwareAccepted) return false;

        // Credit the account in the database
        return getAccount().credit(getAmount());
    }

    public String getDepositType() { return depositType; }
    public void setDepositType(String depositType) { this.depositType = depositType; }
}
