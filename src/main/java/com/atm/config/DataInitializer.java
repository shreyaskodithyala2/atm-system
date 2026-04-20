package com.atm.config;

import com.atm.model.*;
import com.atm.model.enums.AccountType;
import com.atm.model.enums.UserRole;
import com.atm.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired private CustomerRepository customerRepository;
    @Autowired private BankManagerRepository managerRepository;
    @Autowired private SystemAdministratorRepository adminRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (customerRepository.count() > 0) return;

        // Customer 1 — Normal balance, tests standard transactions
        createCustomer("CUST001", "Alice Johnson", "4111111111111111", "1234",
                "ACC001", 8000.00, AccountType.SAVINGS, LocalDate.of(2027, 12, 31));

        // Customer 2 — High balance, tests large transaction (>$5000) approval
        createCustomer("CUST002", "Bob Smith", "5500005555555559", "5678",
                "ACC002", 12000.00, AccountType.CHECKING, LocalDate.of(2027, 6, 30));

        // Customer 3 — Low balance, tests insufficient funds scenario
        createCustomer("CUST003", "Carol White", "4000000000000002", "9999",
                "ACC003", 150.00, AccountType.SAVINGS, LocalDate.of(2026, 3, 31));

        // Bank Manager
        if (!managerRepository.existsByUsername("MGR001")) {
            BankManager manager = new BankManager();
            manager.setUserId("MGR001");
            manager.setName("David Rodriguez");
            manager.setManagerId("MGR001");
            manager.setUsername("MGR001");
            manager.setPassword(passwordEncoder.encode("manager123"));
            manager.setRole(UserRole.BANK_MANAGER);
            managerRepository.save(manager);
        }

        // System Administrator
        if (!adminRepository.existsByUsername("ADM001")) {
            SystemAdministrator admin = new SystemAdministrator();
            admin.setUserId("ADM001");
            admin.setName("Eve Chen");
            admin.setAdminId("ADM001");
            admin.setUsername("ADM001");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(UserRole.ADMIN);
            admin.setFirmwareVersion("2.4.1");
            adminRepository.save(admin);
        }

        System.out.println("\n========================================");
        System.out.println("  ATM System — Test Credentials");
        System.out.println("========================================");
        System.out.println("  CUSTOMER 1: Card 4111111111111111  PIN 1234  Balance $8,000");
        System.out.println("  CUSTOMER 2: Card 5500005555555559  PIN 5678  Balance $12,000");
        System.out.println("  CUSTOMER 3: Card 4000000000000002  PIN 9999  Balance $150");
        System.out.println("  MANAGER:    Username MGR001        Password manager123");
        System.out.println("  ADMIN:      Username ADM001        Password admin123");
        System.out.println("========================================\n");
    }

    private void createCustomer(String userId, String name, String cardNumber, String pin,
                                String accountNumber, double balance, AccountType accountType,
                                LocalDate cardExpiry) {
        ATMCard card = new ATMCard();
        card.setCardNumber(cardNumber);
        card.setExpiryDate(cardExpiry);

        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setBalance(balance);
        account.setAccountType(accountType);

        Customer customer = new Customer();
        customer.setUserId(userId);
        customer.setName(name);
        customer.setCardNumber(cardNumber);
        customer.setPin(passwordEncoder.encode(pin));
        customer.setAtmCard(card);
        customer.setAccount(account);
        customer.setRole(UserRole.CUSTOMER);

        customerRepository.save(customer);
    }
}
