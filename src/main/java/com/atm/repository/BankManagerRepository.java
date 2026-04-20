package com.atm.repository;

import com.atm.model.BankManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BankManagerRepository extends JpaRepository<BankManager, Long> {
    Optional<BankManager> findByUsername(String username);
    boolean existsByUsername(String username);
}
