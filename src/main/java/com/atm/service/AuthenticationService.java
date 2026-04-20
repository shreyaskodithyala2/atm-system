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

@Service
public class AuthenticationService {

    @Autowired private CustomerRepository customerRepository;
    @Autowired private ATMCardRepository atmCardRepository;
    @Autowired private ATMSessionRepository sessionRepository;
    @Autowired private ExternalCardNetworkService cardNetworkService;
    @Autowired private PasswordEncoder passwordEncoder;

    @Value("${atm.pin.max-attempts:3}")
    private int maxPinAttempts;

    public enum CardInsertResult { SUCCESS, INVALID_CARD, CARD_RETAINED, CARD_EXPIRED }
    public enum PinResult { SUCCESS, WRONG_PIN, CARD_RETAINED }

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
