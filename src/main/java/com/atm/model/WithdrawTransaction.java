package com.atm.model;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("WITHDRAW")
public class WithdrawTransaction extends Transaction {

    @Override
    public boolean execute() {
        if (!verifySufficientFunds()) return false;
        return dispenseCash();
    }

    public boolean verifySufficientFunds() {
        return getAccount() != null && getAccount().verifyFunds(getAmount());
    }

    public boolean dispenseCash() {
        return true;
    }
}
