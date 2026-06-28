package com.kawai.repositories;

import com.kawai.models.BookingService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingServiceRepository extends JpaRepository<BookingService, Long> {
    List<BookingService> findByBookingId(Long bookingId);
}
