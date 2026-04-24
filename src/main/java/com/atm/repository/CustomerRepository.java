package com.atm.repository;

import com.atm.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ============================================================
 * SOLID PRINCIPLE 4 — INTERFACE SEGREGATION PRINCIPLE (ISP)
 * ============================================================
 * CustomerRepository is a FOCUSED interface — it only exposes
 * methods relevant to Customer data access.
 *
 * Instead of one massive "DatabaseService" class with methods
 * for every entity, the system uses separate, narrow interfaces:
 *
 *   CustomerRepository       → Customer operations only
 *   AccountRepository        → Account operations only
 *   TransactionRepository    → Transaction operations only
 *   ATMSessionRepository     → Session operations only
 *   ATMCardRepository        → Card operations only
 *
 * No class is forced to depend on methods it does not use.
 * AuthenticationService uses CustomerRepository — it never
 * sees or needs AccountRepository or TransactionRepository.
 *
 * JpaRepository<Customer, Long> provides standard CRUD — we
 * only ADD the specific methods this context actually needs.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /** ISP: only the two card-lookup methods needed by AuthenticationService are here. */
    Optional<Customer> findByCardNumber(String cardNumber);
    boolean existsByCardNumber(String cardNumber);
}
