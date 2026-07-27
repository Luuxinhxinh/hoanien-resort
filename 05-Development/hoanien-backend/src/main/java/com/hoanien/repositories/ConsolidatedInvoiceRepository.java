package com.hoanien.repositories;

import com.hoanien.models.ConsolidatedInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsolidatedInvoiceRepository extends JpaRepository<ConsolidatedInvoice, Long> {
    java.util.Optional<ConsolidatedInvoice> findByBooking_Id(Long bookingId);
}
