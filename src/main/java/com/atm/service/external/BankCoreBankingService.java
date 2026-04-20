package com.atm.service.external;

import com.atm.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Simulates the BankCoreBankingSystem external integration.
 * Delegates to local AccountRepository to simulate core banking operations.
 */
@Service
public class BankCoreBankingService {

    private static final String API_ENDPOINT = "https://corebanking.securebank.internal/api/v1";

    @Autowired
    private AccountRepository accountRepository;

    public boolean verifyAccount(String accountNumber) {
        return accountRepository.existsByAccountNumber(accountNumber);
    }

    public double checkBalance(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .map(account -> account.getBalance())
                .orElse(-1.0);
    }

    public boolean processTransaction(String transactionId, String accountNumber, double amount, String type) {
        // Simulates sending transaction acknowledgment to core banking
        return accountRepository.existsByAccountNumber(accountNumber);
    }

    public String getApiEndpoint() {
        return API_ENDPOINT;
    }
}
