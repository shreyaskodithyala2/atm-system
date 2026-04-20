package com.atm.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "atm_cards")
public class ATMCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "card_number", unique = true, nullable = false)
    private String cardNumber;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "is_retained")
    private boolean isRetained = false;

    @Column(name = "failed_attempts")
    private int failedAttempts = 0;

    public boolean isValid() {
        return !isRetained && expiryDate != null && expiryDate.isAfter(LocalDate.now());
    }

    public void retain() {
        this.isRetained = true;
    }

    public void release() {
        this.isRetained = false;
        this.failedAttempts = 0;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public boolean isRetained() { return isRetained; }
    public void setRetained(boolean retained) { isRetained = retained; }
    public int getFailedAttempts() { return failedAttempts; }
    public void setFailedAttempts(int failedAttempts) { this.failedAttempts = failedAttempts; }
}
