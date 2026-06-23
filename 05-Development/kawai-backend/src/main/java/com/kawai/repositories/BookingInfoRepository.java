package com.kawai.repositories;

import com.kawai.dto.BookingInfoDto;
import com.kawai.models.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingInfoRepository extends JpaRepository<Booking, Long> {
    @Query(value = "SELECT tb.booking_id AS bookingId, " +
            "c.full_name AS customerName, " +
            "c.email AS customerEmail, " +
            "c.phone AS customerPhone, " +
            "COUNT(rbd.detail_id) AS roomQuantity, " +
            "GROUP_CONCAT(r.room_number SEPARATOR ', ') AS roomNames " +
            "FROM Tour_Bookings tb " +
            "JOIN Bookings b ON tb.booking_id = b.booking_id " +
            "JOIN Customers c ON b.customer_id = c.customer_id " +
            "LEFT JOIN Room_Booking_Details rbd ON rbd.room_booking_id = b.booking_id " +
            "LEFT JOIN Rooms r ON rbd.room_id = r.room_id " +
            "GROUP BY tb.booking_id, c.full_name, c.email, c.phone " +
            "ORDER BY tb.booking_id DESC LIMIT 5",
            nativeQuery = true)
    List<BookingInfoDto> findRecentBookings();
}
