package com.atm.repository;

import com.atm.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByCardNumber(String cardNumber);
    boolean existsByCardNumber(String cardNumber);
}
