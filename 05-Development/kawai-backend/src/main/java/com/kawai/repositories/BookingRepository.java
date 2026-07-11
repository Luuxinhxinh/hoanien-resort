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

    List<Booking> findByCustomerId(Long customerId);

    @Query("SELECT b FROM Booking b WHERE b.bookingStatus = 'Confirmed'")
    List<Booking> findConfirmed();

    @Query("SELECT b FROM Booking b WHERE b.bookingStatus = 'Checked_In'")
    List<Booking> findCheckedIn();

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.customer.id = :customerId AND UPPER(b.appliedPromotion.promoCode) = UPPER(:promoCode) AND LOWER(b.bookingStatus) NOT LIKE '%cancel%' AND LOWER(b.bookingStatus) != 'no-show'")
    long countByCustomerIdAndPromoCode(@org.springframework.data.repository.query.Param("customerId") Long customerId, @org.springframework.data.repository.query.Param("promoCode") String promoCode);

    @Query("SELECT b FROM Booking b WHERE b.customer.id = :customerId AND UPPER(b.appliedPromotion.promoCode) = UPPER(:promoCode) AND LOWER(b.bookingStatus) NOT LIKE '%cancel%' AND LOWER(b.bookingStatus) != 'no-show'")
    List<Booking> findUsedPromoBookings(@org.springframework.data.repository.query.Param("customerId") Long customerId, @org.springframework.data.repository.query.Param("promoCode") String promoCode);

    @Query("SELECT b FROM Booking b WHERE b.customer.id = :customerId AND (b.bookingStatus = 'Completed' OR b.bookingStatus = 'Confirmed' OR b.bookingStatus = 'Checked_In')")
    List<Booking> findCompletedOrConfirmedBookingsByCustomerId(@org.springframework.data.repository.query.Param("customerId") Long customerId);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.customer.id = :customerId AND (b.bookingStatus = 'Completed' OR b.bookingStatus = 'Confirmed' OR b.bookingStatus = 'Checked_In')")
    long countCompletedOrConfirmedBookingsByCustomerId(@org.springframework.data.repository.query.Param("customerId") Long customerId);

    @Query("SELECT b FROM Booking b WHERE LOWER(b.bookingStatus) LIKE '%cancel%' OR LOWER(b.bookingStatus) = 'no-show'")
    List<Booking> findCancelledBookings();
}