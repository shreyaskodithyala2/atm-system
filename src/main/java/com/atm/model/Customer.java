package com.atm.model;

import com.atm.model.enums.UserRole;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customers")
@DiscriminatorValue("CUSTOMER")
public class Customer extends ATMUser {

    @Column(name = "card_number", unique = true, nullable = false)
    private String cardNumber;

    @Column(nullable = false)
    private String pin;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "card_id")
    private ATMCard atmCard;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id")
    private Account account;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ATMSession> sessions = new ArrayList<>();

    public Customer() {
        setRole(UserRole.CUSTOMER);
    }

    @Override
    public boolean login() {
        return atmCard != null && !atmCard.isRetained();
    }

    @Override
    public void logout() {
        // Session end handled by AuthController
    }

    public boolean withdrawCash(double amount) {
        return account != null && account.debit(amount);
    }

    public boolean depositFunds(double amount) {
        return account != null && account.credit(amount);
    }

    public boolean transferFunds(String targetAccountNumber, double amount) {
        return account != null && account.debit(amount);
    }

    public boolean payUtilityBill(String billId) {
        return account != null;
    }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }
    public ATMCard getAtmCard() { return atmCard; }
    public void setAtmCard(ATMCard atmCard) { this.atmCard = atmCard; }
    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }
    public List<ATMSession> getSessions() { return sessions; }
    public void setSessions(List<ATMSession> sessions) { this.sessions = sessions; }
}
