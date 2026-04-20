package com.atm.repository;

import com.atm.model.SystemAdministrator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemAdministratorRepository extends JpaRepository<SystemAdministrator, Long> {
    Optional<SystemAdministrator> findByUsername(String username);
    boolean existsByUsername(String username);
}
