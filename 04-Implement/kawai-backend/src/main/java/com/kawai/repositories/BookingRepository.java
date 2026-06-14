package com.kawai.repositories;

import com.kawai.models.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT b.bookingStatus, COUNT(b) FROM Booking b GROUP BY b.bookingStatus")
    List<Object[]> countByStatus();

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.bookingStatus = 'Confirmed'")
    long countConfirmed();

    @Query("SELECT b FROM Booking b WHERE b.bookingStatus = 'Confirmed'")
    List<Booking> findConfirmed();
}