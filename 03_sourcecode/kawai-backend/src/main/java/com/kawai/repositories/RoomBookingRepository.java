package com.kawai.repositories;

import com.kawai.models.RoomBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RoomBookingRepository extends JpaRepository<RoomBooking, Long> {

    @Query("SELECT COUNT(rb) FROM RoomBooking rb " +
           "JOIN RoomBookingDetail rbd ON rbd.roomBooking = rb " +
           "WHERE rbd.room.roomNumber = :roomNumber " +
           "AND rb.bookingStatus <> 'Cancelled' " +
           "AND ((rb.checkInDate <= :checkOutDate) AND (rb.checkOutDate >= :checkInDate))")
    long countOverlappingBookings(@Param("roomNumber") String roomNumber, 
                                  @Param("checkInDate") LocalDate checkInDate, 
                                  @Param("checkOutDate") LocalDate checkOutDate);
}
