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
}
