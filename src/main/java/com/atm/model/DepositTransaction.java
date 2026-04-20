package com.atm.model;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("DEPOSIT")
public class DepositTransaction extends Transaction {

    @Column(name = "deposit_type")
    private String depositType;

    @Override
    public boolean execute() {
        return processDeposit();
    }

    public boolean processDeposit() {
        return getAccount() != null && getAccount().credit(getAmount());
    }

    public String getDepositType() { return depositType; }
    public void setDepositType(String depositType) { this.depositType = depositType; }
}
