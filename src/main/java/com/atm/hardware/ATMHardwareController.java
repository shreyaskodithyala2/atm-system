package com.atm.hardware;

/**
 * ============================================================
 * CREATIONAL PATTERN 1 — SINGLETON PATTERN
 * ============================================================
 * WHAT IS SINGLETON?
 * A Singleton ensures that a class has only ONE instance
 * throughout the entire application, and provides a global
 * access point to it via getInstance().
 *
 * WHY IT FITS HERE:
 * There is physically only ONE ATM machine at any location.
 * There is only ONE cash dispenser, ONE card reader, ONE
 * receipt printer attached to it.
 *
 * Creating multiple ATMHardwareController objects would make
 * no sense — it would imply multiple physical machines.
 * Singleton enforces this real-world constraint in code.
 *
 * HOW IT IS USED IN THIS PROJECT:
 *   WithdrawTransaction.dispenseCash()
 *       → ATMHardwareController.getInstance().dispenseCash(amount)
 *   DepositTransaction.processDeposit()
 *       → ATMHardwareController.getInstance().acceptCashDeposit(amount)
 *
 * IMPLEMENTATION DETAILS:
 *   - Private constructor prevents `new ATMHardwareController()`
 *   - volatile + double-checked locking makes it thread-safe
 *   - Static `instance` field is shared across ALL callers
 *
 * STRUCTURE:
 *   ATMHardwareController
 *     - private static volatile instance
 *     - private ATMHardwareController()        ← no external instantiation
 *     + static getInstance()                   ← global access point
 *     + dispenseCash(amount)
 *     + acceptCashDeposit(amount)
 *     + getHardwareStatus()
 */
public class ATMHardwareController {

    // ---- Singleton: the one and only instance ----
    private static volatile ATMHardwareController instance;

    // Track internal state of the hardware
    private boolean cashDispenserOnline = true;
    private boolean cardReaderOnline    = true;
    private boolean receiptPrinterOnline = true;
    private int     cashAvailable        = 100_000; // cents, simulated cash in machine

    /**
     * SINGLETON: Private constructor — prevents any external code
     * from calling `new ATMHardwareController()`.
     * The only way to get an instance is through getInstance().
     */
    private ATMHardwareController() {
        System.out.println("[ATMHardwareController] Hardware initialised — Singleton instance created.");
    }

    /**
     * SINGLETON: Global access point.
     * Uses double-checked locking to be safe when multiple
     * threads (concurrent ATM requests) call getInstance()
     * at the same time — only the very first call creates the
     * object; every subsequent call returns the same instance.
     *
     * @return the single ATMHardwareController instance
     */
    public static ATMHardwareController getInstance() {
        if (instance == null) {                          // first check (no lock)
            synchronized (ATMHardwareController.class) {
                if (instance == null) {                  // second check (with lock)
                    instance = new ATMHardwareController();
                }
            }
        }
        return instance;
    }

    /**
     * Simulates the physical cash dispenser ejecting notes.
     * Called by WithdrawTransaction.dispenseCash().
     *
     * @param amount amount in dollars to dispense
     * @return true if dispensed successfully, false if hardware offline or insufficient cash
     */
    public boolean dispenseCash(double amount) {
        if (!cashDispenserOnline) {
            System.out.println("[ATMHardwareController] Cash dispenser OFFLINE — cannot dispense $" + amount);
            return false;
        }
        int cents = (int)(amount * 100);
        if (cents > cashAvailable) {
            System.out.println("[ATMHardwareController] Insufficient cash in machine for $" + amount);
            return false;
        }
        cashAvailable -= cents;
        System.out.printf("[ATMHardwareController] Dispensed $%.2f — Cash remaining: $%.2f%n",
                amount, cashAvailable / 100.0);
        return true;
    }

    /**
     * Simulates the cash deposit slot accepting physical notes.
     * Called by DepositTransaction.processDeposit().
     *
     * @param amount amount in dollars being deposited
     * @return true if accepted successfully
     */
    public boolean acceptCashDeposit(double amount) {
        if (!cashDispenserOnline) {
            System.out.println("[ATMHardwareController] Cash acceptor OFFLINE — cannot accept $" + amount);
            return false;
        }
        cashAvailable += (int)(amount * 100);
        System.out.printf("[ATMHardwareController] Accepted deposit $%.2f — Cash in machine: $%.2f%n",
                amount, cashAvailable / 100.0);
        return true;
    }

    /**
     * Returns a status summary used by the Admin diagnostics report.
     */
    public String getCashDispenserStatus() {
        return cashDispenserOnline ? "FUNCTIONAL" : "OFFLINE";
    }

    public String getCardReaderStatus() {
        return cardReaderOnline ? "FUNCTIONAL" : "OFFLINE";
    }

    public double getCashAvailable() {
        return cashAvailable / 100.0;
    }
}
