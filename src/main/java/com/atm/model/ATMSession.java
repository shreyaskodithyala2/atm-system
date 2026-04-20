package com.atm.model;

import com.atm.model.enums.SessionState;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "atm_sessions")
public class ATMSession {

    @Id
    @Column(name = "session_id")
    private String sessionId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(name = "is_authenticated")
    private boolean isAuthenticated = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionState state = SessionState.IDLE;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Transaction> transactions = new ArrayList<>();

    public void authenticate(String pin) {
        this.isAuthenticated = true;
        this.state = SessionState.SESSION_ACTIVE;
    }

    public void triggerSilentAlarm() {
        this.state = SessionState.CARD_RETAINED;
    }

    public void retainCard() {
        if (customer != null && customer.getAtmCard() != null) {
            customer.getAtmCard().retain();
        }
        this.state = SessionState.CARD_RETAINED;
    }

    public void endSession() {
        this.isAuthenticated = false;
        this.state = SessionState.ENDED;
        this.endTime = LocalDateTime.now();
    }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public boolean isAuthenticated() { return isAuthenticated; }
    public void setAuthenticated(boolean authenticated) { isAuthenticated = authenticated; }
    public SessionState getState() { return state; }
    public void setState(SessionState state) { this.state = state; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public List<Transaction> getTransactions() { return transactions; }
    public void setTransactions(List<Transaction> transactions) { this.transactions = transactions; }
}
