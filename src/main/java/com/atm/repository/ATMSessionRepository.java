package com.atm.repository;

import com.atm.model.ATMSession;
import com.atm.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ATMSessionRepository extends JpaRepository<ATMSession, String> {
    List<ATMSession> findByCustomerOrderByStartTimeDesc(Customer customer);
}
