package com.kawai.repositories;

import com.kawai.models.PaymentTransaction;
import com.kawai.models.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    Optional<PaymentTransaction> findByTransactionRef(String transactionRef);

    boolean existsByBookingIdAndStatus(Long bookingId, PaymentStatus status);

    java.util.List<PaymentTransaction> findByBookingId(Long bookingId);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(p.amount) FROM PaymentTransaction p WHERE p.paidAt >= :start AND p.paidAt < :end AND p.status = :status")
    java.math.BigDecimal sumRevenueBetween(@org.springframework.data.repository.query.Param("start") java.time.LocalDateTime start, @org.springframework.data.repository.query.Param("end") java.time.LocalDateTime end, @org.springframework.data.repository.query.Param("status") PaymentStatus status);

    @org.springframework.data.jpa.repository.Query("SELECT p FROM PaymentTransaction p WHERE p.paidAt >= :start AND p.paidAt < :end AND p.status = :status")
    java.util.List<PaymentTransaction> findBetween(@org.springframework.data.repository.query.Param("start") java.time.LocalDateTime start, @org.springframework.data.repository.query.Param("end") java.time.LocalDateTime end, @org.springframework.data.repository.query.Param("status") PaymentStatus status);
}
