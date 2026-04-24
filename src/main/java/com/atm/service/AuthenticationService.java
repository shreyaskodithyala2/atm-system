package com.atm.service;

import com.atm.model.ATMSession;
import com.atm.model.Customer;
import com.atm.model.enums.SessionState;
import com.atm.repository.ATMCardRepository;
import com.atm.repository.ATMSessionRepository;
import com.atm.repository.CustomerRepository;
import com.atm.service.external.ExternalCardNetworkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ============================================================
 * SOLID PRINCIPLE 1 — SINGLE RESPONSIBILITY PRINCIPLE (SRP)
 * ============================================================
 * AuthenticationService has ONE reason to change: the rules
 * around how a customer authenticates at the ATM.
 *
 * It is NOT responsible for:
 *   - Processing transactions  → TransactionService
 *   - Approving large amounts  → ManagerService
 *   - Managing cards/firmware  → AdminService
 *
 * If PIN policy changes (e.g. 4 attempts instead of 3), only
 * THIS class needs to be updated. No other service is affected.
 *
 * ============================================================
 * SOLID PRINCIPLE 5 — DEPENDENCY INVERSION PRINCIPLE (DIP)
 * ============================================================
 * All dependencies are declared as interfaces:
 *   - CustomerRepository       (Spring Data interface)
 *   - ATMCardRepository        (Spring Data interface)
 *   - ATMSessionRepository     (Spring Data interface)
 *   - ExternalCardNetworkService (interface — swappable impl)
 *   - PasswordEncoder          (Spring Security interface)
 *
 * This class never depends on concrete implementations.
 * Spring resolves and injects the correct bean at startup.
 */
@Service  // Spring Singleton — one shared instance across the application
public class AuthenticationService {

    // DIP: every field is typed to an interface, not a class
    @Autowired private CustomerRepository customerRepository;
    @Autowired private ATMCardRepository atmCardRepository;
    @Autowired private ATMSessionRepository sessionRepository;
    @Autowired private ExternalCardNetworkService cardNetworkService; // DIP: interface
    @Autowired private PasswordEncoder passwordEncoder;               // DIP: interface

    // Externalised to application.properties — easy to change without recompiling
    @Value("${atm.pin.max-attempts:3}")
    private int maxPinAttempts;

    /** Result of inserting a card — keeps HTTP responses clean (SRP of result encoding). */
    public enum CardInsertResult { SUCCESS, INVALID_CARD, CARD_RETAINED, CARD_EXPIRED }

    /** Result of entering a PIN. */
    public enum PinResult { SUCCESS, WRONG_PIN, CARD_RETAINED }

    /**
     * SRP: This method's ONLY job is card validation.
     * DIP: Uses ExternalCardNetworkService interface — a real
     * bank would swap in a live network implementation without
     * changing any code here.
     */
    @Transactional
    public CardInsertResult insertCard(String cardNumber) {
        if (!cardNetworkService.validateCard(cardNumber)) {
            return CardInsertResult.INVALID_CARD;
        }
        Customer customer = customerRepository.findByCardNumber(cardNumber).orElse(null);
        if (customer == null) return CardInsertResult.INVALID_CARD;

        if (customer.getAtmCard().isRetained()) return CardInsertResult.CARD_RETAINED;
        if (!customer.getAtmCard().isValid()) return CardInsertResult.CARD_EXPIRED;

        return CardInsertResult.SUCCESS;
    }

    /**
     * SRP: Creates and persists a new ATMSession — session
     * lifecycle management is part of authentication concern.
     */
    @Transactional
    public ATMSession createSession(String cardNumber) {
        Customer customer = customerRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        ATMSession session = new ATMSession();
        session.setSessionId(UUID.randomUUID().toString());
        session.setCustomer(customer);
        session.setState(SessionState.AUTHENTICATING);
        session.setStartTime(LocalDateTime.now());
        return sessionRepository.save(session);
    }

    /**
     * SRP: PIN verification and retry/retention logic lives
     * entirely here — no other class needs to know about it.
     *
     * DIP: Uses PasswordEncoder interface — BCrypt today,
     * any other algorithm tomorrow, without changing this method.
     */
    @Transactional
    public PinResult authenticatePin(String sessionId, String rawPin) {
        ATMSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        Customer customer = session.getCustomer();

        if (passwordEncoder.matches(rawPin, customer.getPin())) {
            customer.getAtmCard().setFailedAttempts(0);
            customerRepository.save(customer);
            session.setAuthenticated(true);
            session.setState(SessionState.SESSION_ACTIVE);
            sessionRepository.save(session);
            return PinResult.SUCCESS;
        }

        int attempts = customer.getAtmCard().getFailedAttempts() + 1;
        customer.getAtmCard().setFailedAttempts(attempts);

        if (attempts >= maxPinAttempts) {
            customer.getAtmCard().retain();
            session.triggerSilentAlarm();
            session.setState(SessionState.CARD_RETAINED);
            sessionRepository.save(session);
            customerRepository.save(customer);
            return PinResult.CARD_RETAINED;
        }

        customerRepository.save(customer);
        return PinResult.WRONG_PIN;
    }

    @Transactional
    public void endSession(String sessionId) {
        sessionRepository.findById(sessionId).ifPresent(session -> {
            session.endSession();
            sessionRepository.save(session);
        });
    }

    public int getRemainingAttempts(String sessionId) {
        return sessionRepository.findById(sessionId)
                .map(s -> maxPinAttempts - s.getCustomer().getAtmCard().getFailedAttempts())
                .orElse(0);
    }
}
