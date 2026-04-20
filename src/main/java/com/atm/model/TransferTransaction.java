package com.atm.model;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("TRANSFER")
public class TransferTransaction extends Transaction {

    @Column(name = "target_account_number")
    private String targetAccountNumber;

    @Override
    public boolean execute() {
        return verifySufficientFunds();
    }

    public boolean verifySufficientFunds() {
        return getAccount() != null && getAccount().verifyFunds(getAmount());
    }

    public String getTargetAccountNumber() { return targetAccountNumber; }
    public void setTargetAccountNumber(String targetAccountNumber) {
        this.targetAccountNumber = targetAccountNumber;
    }
}
