package com.atm.dto;

import java.time.LocalDateTime;

public class DiagnosticsReport {
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

    public DiagnosticsReport() {}

    public LocalDateTime getRunAt() { return runAt; }
    public void setRunAt(LocalDateTime runAt) { this.runAt = runAt; }
    public String getSystemStatus() { return systemStatus; }
    public void setSystemStatus(String systemStatus) { this.systemStatus = systemStatus; }
    public String getDatabaseStatus() { return databaseStatus; }
    public void setDatabaseStatus(String databaseStatus) { this.databaseStatus = databaseStatus; }
    public String getNetworkStatus() { return networkStatus; }
    public void setNetworkStatus(String networkStatus) { this.networkStatus = networkStatus; }
    public String getCashDispenserStatus() { return cashDispenserStatus; }
    public void setCashDispenserStatus(String cashDispenserStatus) { this.cashDispenserStatus = cashDispenserStatus; }
    public String getCardReaderStatus() { return cardReaderStatus; }
    public void setCardReaderStatus(String cardReaderStatus) { this.cardReaderStatus = cardReaderStatus; }
    public long getActiveSessionCount() { return activeSessionCount; }
    public void setActiveSessionCount(long activeSessionCount) { this.activeSessionCount = activeSessionCount; }
    public long getTodayTransactionCount() { return todayTransactionCount; }
    public void setTodayTransactionCount(long todayTransactionCount) { this.todayTransactionCount = todayTransactionCount; }
    public long getRetainedCardCount() { return retainedCardCount; }
    public void setRetainedCardCount(long retainedCardCount) { this.retainedCardCount = retainedCardCount; }
    public String getFirmwareVersion() { return firmwareVersion; }
    public void setFirmwareVersion(String firmwareVersion) { this.firmwareVersion = firmwareVersion; }
}
