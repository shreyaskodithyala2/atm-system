package com.atm.repository;

import com.atm.model.ATMCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ATMCardRepository extends JpaRepository<ATMCard, Long> {
    Optional<ATMCard> findByCardNumber(String cardNumber);
}
