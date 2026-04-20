package com.atm.service.external;

import com.atm.repository.ATMCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Simulates the ExternalCardNetwork (Visa/MasterCard).
 * In production this would make HTTP calls to the card network.
 */
@Service
public class ExternalCardNetworkService {

    private static final String NETWORK_NAME = "SecureNet";

    @Autowired
    private ATMCardRepository atmCardRepository;

    public boolean validateCard(String cardNumber) {
        if (cardNumber == null || cardNumber.replaceAll("\\s|-", "").length() != 16) {
            return false;
        }
        return atmCardRepository.findByCardNumber(cardNumber).isPresent();
    }

    public boolean authorizeTransaction(String cardNumber, double amount) {
        if (amount <= 0 || amount > 50000) return false;
        return atmCardRepository.findByCardNumber(cardNumber)
                .map(card -> !card.isRetained() && card.isValid())
                .orElse(false);
    }

    public String getNetworkName() {
        return NETWORK_NAME;
    }
}
