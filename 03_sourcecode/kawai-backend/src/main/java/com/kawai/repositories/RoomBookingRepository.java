package com.kawai.repositories;

import com.kawai.models.RoomBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;

@Repository
public interface RoomBookingRepository extends JpaRepository<RoomBooking, Long> {

       @Query("SELECT COALESCE(SUM(rb.depositAmount), 0) FROM RoomBooking rb WHERE rb.checkInDate >= :since")
       BigDecimal totalDepositsSince(LocalDate since);

       @Query("SELECT COUNT(rb) FROM RoomBooking rb WHERE rb.checkInDate >= :since")
       long countSince(LocalDate since);

       @Query("SELECT rb FROM RoomBooking rb WHERE rb.checkOutDate BETWEEN :start AND :end")
       List<RoomBooking> findCheckOutsBetween(LocalDate start, LocalDate end);
}