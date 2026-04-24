package com.atm.model;

import com.atm.hardware.ATMHardwareController;
import jakarta.persistence.*;

/**
 * DESIGN PATTERN — TEMPLATE METHOD (Concrete Step)
 * DESIGN PATTERN — STRATEGY (Concrete Strategy: Withdrawal)
 * SOLID           — LSP: substitutable for Transaction everywhere
 *
 * CREATIONAL PATTERN 1 — SINGLETON (usage site)
 * dispenseCash() calls ATMHardwareController.getInstance()
 * to interact with the single physical cash dispenser.
 * We do NOT call `new ATMHardwareController()` — that is
 * forbidden. We always use the shared Singleton instance.
 */
@Entity
@DiscriminatorValue("WITHDRAW")
public class WithdrawTransaction extends Transaction {

    /**
     * TEMPLATE METHOD hook — concrete withdrawal algorithm:
     *   Step 1: Verify account has enough funds.
     *   Step 2: Instruct the hardware (Singleton) to dispense cash.
     */
    @Override
    public boolean execute() {
        if (!verifySufficientFunds()) return false;
        return dispenseCash();
    }

    /** Step 1 — Fund check (uses account model). */
    public boolean verifySufficientFunds() {
        return getAccount() != null && getAccount().verifyFunds(getAmount());
    }

    /**
     * Step 2 — Physical cash dispensing.
     *
     * SINGLETON usage: ATMHardwareController.getInstance()
     * returns the one shared hardware controller. There is only
     * one cash dispenser on the machine — Singleton enforces this.
     */
    public boolean dispenseCash() {
        // SINGLETON: always use getInstance(), never `new ATMHardwareController()`
        return ATMHardwareController.getInstance().dispenseCash(getAmount());
    }
}
