package com.atm.repository;

import com.atm.model.Transaction;
import com.atm.model.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    List<Transaction> findByStatusOrderByTimestampDesc(TransactionStatus status);

    List<Transaction> findBySession_Customer_IdOrderByTimestampDesc(Long customerId);

    List<Transaction> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime start, LocalDateTime end);

    @Query("SELECT t FROM Transaction t WHERE t.session.sessionId = :sessionId AND t.status = :status ORDER BY t.timestamp DESC")
    List<Transaction> findBySessionIdAndStatus(@Param("sessionId") String sessionId,
                                               @Param("status") TransactionStatus status);

    long countByStatus(TransactionStatus status);

    long countByTimestampBetween(LocalDateTime start, LocalDateTime end);
}
