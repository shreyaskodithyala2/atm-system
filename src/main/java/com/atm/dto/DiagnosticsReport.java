package com.atm.dto;

import java.time.LocalDateTime;

/**
 * ============================================================
 * CREATIONAL PATTERN 3 — BUILDER PATTERN
 * ============================================================
 * WHAT IS BUILDER?
 * The Builder Pattern separates the construction of a complex
 * object from its representation. Instead of passing 10
 * arguments to a constructor (telescoping constructor) or
 * making 10 setter calls, a fluent Builder chains the steps
 * and returns the final object with .build().
 *
 * WHY IT FITS HERE:
 * DiagnosticsReport has 10 fields:
 *   runAt, systemStatus, databaseStatus, networkStatus,
 *   cashDispenserStatus, cardReaderStatus, activeSessionCount,
 *   todayTransactionCount, retainedCardCount, firmwareVersion
 *
 * Before Builder (in AdminService — 10 separate setters):
 *   DiagnosticsReport report = new DiagnosticsReport();
 *   report.setRunAt(LocalDateTime.now());
 *   report.setSystemStatus("OPERATIONAL");
 *   report.setDatabaseStatus("CONNECTED");
 *   ...  (10 lines total)
 *
 * After Builder (single readable chain):
 *   DiagnosticsReport report = new DiagnosticsReport.Builder()
 *       .runAt(LocalDateTime.now())
 *       .systemStatus("OPERATIONAL")
 *       .databaseStatus("CONNECTED")
 *       ...
 *       .build();
 *
 * BENEFITS:
 *   - Readable: each line names what it sets
 *   - Safe: build() can validate that required fields are set
 *   - Immutable-friendly: fields can be made final after build()
 *   - Easy to add optional fields without breaking callers
 *
 * STRUCTURE:
 *   DiagnosticsReport          ← Product (the complex object)
 *     + Builder (inner class)  ← Concrete Builder
 *       - runAt(...)
 *       - systemStatus(...)
 *       - ... (one method per field)
 *       + build()              ← creates the final Product
 */
public class DiagnosticsReport {

    // ---- Product fields (all set via Builder) ----
    private LocalDateTime runAt;
    private String systemStatus;
    private String databaseStatus;
    private String networkStatus;
    private String cashDispenserStatus;
    private String cardReaderStatus;
    private long activeSessionCount;
    private long todayTransactionCount;
    private long retainedCardCount;
    private String firmwareVersion;

    /**
     * BUILDER: private no-arg constructor — forces callers to
     * use Builder instead of `new DiagnosticsReport()`.
     * You cannot create a DiagnosticsReport without the Builder.
     */
    private DiagnosticsReport() {}

    // ============================================================
    // BUILDER — inner class
    // ============================================================
    /**
     * BUILDER: Fluent inner class that accumulates field values
     * and creates the DiagnosticsReport via build().
     *
     * Usage:
     *   DiagnosticsReport r = new DiagnosticsReport.Builder()
     *       .runAt(LocalDateTime.now())
     *       .systemStatus("OPERATIONAL")
     *       .build();
     */
    public static class Builder {

        // Builder mirrors every field of the product
        private LocalDateTime runAt;
        private String systemStatus;
        private String databaseStatus;
        private String networkStatus;
        private String cashDispenserStatus;
        private String cardReaderStatus;
        private long activeSessionCount;
        private long todayTransactionCount;
        private long retainedCardCount;
        private String firmwareVersion;

        /** BUILDER: Each setter returns `this` — enabling method chaining. */
        public Builder runAt(LocalDateTime runAt) {
            this.runAt = runAt;
            return this;
        }

        public Builder systemStatus(String systemStatus) {
            this.systemStatus = systemStatus;
            return this;
        }

        public Builder databaseStatus(String databaseStatus) {
            this.databaseStatus = databaseStatus;
            return this;
        }

        public Builder networkStatus(String networkStatus) {
            this.networkStatus = networkStatus;
            return this;
        }

        public Builder cashDispenserStatus(String cashDispenserStatus) {
            this.cashDispenserStatus = cashDispenserStatus;
            return this;
        }

        public Builder cardReaderStatus(String cardReaderStatus) {
            this.cardReaderStatus = cardReaderStatus;
            return this;
        }

        public Builder activeSessionCount(long activeSessionCount) {
            this.activeSessionCount = activeSessionCount;
            return this;
        }

        public Builder todayTransactionCount(long todayTransactionCount) {
            this.todayTransactionCount = todayTransactionCount;
            return this;
        }

        public Builder retainedCardCount(long retainedCardCount) {
            this.retainedCardCount = retainedCardCount;
            return this;
        }

        public Builder firmwareVersion(String firmwareVersion) {
            this.firmwareVersion = firmwareVersion;
            return this;
        }

        /**
         * BUILDER: Terminal method — copies all accumulated values
         * into a new DiagnosticsReport and returns it.
         * This is the only way to obtain a DiagnosticsReport instance.
         */
        public DiagnosticsReport build() {
            DiagnosticsReport report = new DiagnosticsReport();
            report.runAt                 = this.runAt;
            report.systemStatus          = this.systemStatus;
            report.databaseStatus        = this.databaseStatus;
            report.networkStatus         = this.networkStatus;
            report.cashDispenserStatus   = this.cashDispenserStatus;
            report.cardReaderStatus      = this.cardReaderStatus;
            report.activeSessionCount    = this.activeSessionCount;
            report.todayTransactionCount = this.todayTransactionCount;
            report.retainedCardCount     = this.retainedCardCount;
            report.firmwareVersion       = this.firmwareVersion;
            return report;
        }
    }

    // ---- Getters (no setters — object is built, then read-only) ----
    public LocalDateTime getRunAt()              { return runAt; }
    public String getSystemStatus()              { return systemStatus; }
    public String getDatabaseStatus()            { return databaseStatus; }
    public String getNetworkStatus()             { return networkStatus; }
    public String getCashDispenserStatus()       { return cashDispenserStatus; }
    public String getCardReaderStatus()          { return cardReaderStatus; }
    public long getActiveSessionCount()          { return activeSessionCount; }
    public long getTodayTransactionCount()       { return todayTransactionCount; }
    public long getRetainedCardCount()           { return retainedCardCount; }
    public String getFirmwareVersion()           { return firmwareVersion; }
}
