package com.kawai.repositories;

import com.kawai.models.RoomBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface RoomBookingRepository extends JpaRepository<RoomBooking, Long> {

       @Query("SELECT COALESCE(SUM(rb.depositAmount), 0) FROM RoomBooking rb WHERE rb.checkInDate >= :since")
       BigDecimal totalDepositsSince(LocalDate since);

       @Query("SELECT COUNT(rb) FROM RoomBooking rb WHERE rb.checkInDate >= :since")
       long countSince(LocalDate since);

       @Query("SELECT rb FROM RoomBooking rb WHERE rb.checkOutDate BETWEEN :start AND :end")
       List<RoomBooking> findCheckOutsBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

       @Query("SELECT COUNT(rbd) FROM RoomBookingDetail rbd " +
              "WHERE rbd.room.roomNumber = :roomNumber " +
              "AND rbd.roomBooking.checkInDate < :checkOut " +
              "AND rbd.roomBooking.checkOutDate > :checkIn " +
              "AND rbd.roomBooking.booking.bookingStatus != 'CANCELLED'")
       long countOverlappingBookings(@Param("roomNumber") String roomNumber,
                                     @Param("checkIn") LocalDate checkIn,
                                     @Param("checkOut") LocalDate checkOut);

       @Query("SELECT rb FROM RoomBooking rb WHERE rb.booking.customer = :customer")
       List<RoomBooking> findByCustomer(@Param("customer") com.kawai.models.Customer customer);
}