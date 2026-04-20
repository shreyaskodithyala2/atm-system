package com.atm.model;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("BILL_PAYMENT")
public class BillPayment extends Transaction {

    @Column(name = "biller_id")
    private String billerId;

    @Column(name = "biller_name")
    private String billerName;

    @Override
    public boolean execute() {
        return processBillPayment();
    }

    public boolean processBillPayment() {
        return getAccount() != null && getAccount().verifyFunds(getAmount());
    }

    public String getBillerId() { return billerId; }
    public void setBillerId(String billerId) { this.billerId = billerId; }
    public String getBillerName() { return billerName; }
    public void setBillerName(String billerName) { this.billerName = billerName; }
}
