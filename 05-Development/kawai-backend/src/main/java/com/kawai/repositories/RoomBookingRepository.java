package com.kawai.repositories;

import com.kawai.models.RoomBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.kawai.models.Customer;

@Repository
public interface RoomBookingRepository extends JpaRepository<RoomBooking, Long> {
       List<RoomBooking> findByCustomerOrderByBookingDateDesc(Customer customer);

       List<RoomBooking> findByCustomerOrderByIdDesc(Customer customer);

       java.util.Optional<RoomBooking> findByIdAndCustomerId(Long id, Long customerId);

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
                     "AND rbd.roomBooking.bookingStatus != 'CANCELLED'")
       long countOverlappingBookings(@Param("roomNumber") String roomNumber,
                     @Param("checkIn") LocalDate checkIn,
                     @Param("checkOut") LocalDate checkOut);

       @Query("SELECT COUNT(rbd) FROM RoomBookingDetail rbd " +
                     "WHERE rbd.room.roomNumber = :roomNumber " +
                     "AND rbd.roomBooking.checkInDate < :checkOut " +
                     "AND rbd.roomBooking.checkOutDate > :checkIn " +
                     "AND rbd.roomBooking.bookingStatus != 'CANCELLED' " +
                     "AND rbd.roomBooking.id != :excludeBookingId")
       long countOverlappingBookingsByRoom(@Param("roomNumber") String roomNumber,
                     @Param("checkIn") LocalDate checkIn,
                     @Param("checkOut") LocalDate checkOut,
                     @Param("excludeBookingId") Long excludeBookingId);

       /**
        * Dùng trong createBooking() để kiểm tra phòng đã bị booking/HOLD bởi người
        * khác chưa.
        * Status HOLD và CONFIRMED đều được đếm (chỉ bỏ CANCELLED).
        */

       @Query("SELECT rb FROM RoomBooking rb WHERE rb.bookingStatus = 'HOLD' AND rb.holdExpiresAt <= :now")
       List<RoomBooking> findStaleHolds(@Param("now") java.time.LocalDateTime now);

       @Query(value = "SELECT DATE(rb.check_in_date) FROM Room_Bookings rb GROUP BY DATE(rb.check_in_date) ORDER BY COUNT(rb.room_booking_id) DESC LIMIT 1", nativeQuery = true)
       java.sql.Date findPeakOccupancyDate();

       @Query("SELECT COUNT(DISTINCT rbd.room.id) FROM RoomBookingDetail rbd " +
              "WHERE rbd.roomBooking.checkInDate <= :date " +
              "AND rbd.roomBooking.checkOutDate > :date " +
              "AND rbd.roomBooking.bookingStatus IN ('Confirmed', 'Checked_In')")
       Integer countOccupiedRoomsOnDate(@Param("date") LocalDate date);

       @Query("SELECT SUM(b.totalPrice) FROM RoomBooking b WHERE b.bookingDate = :date AND b.bookingStatus IN ('Confirmed', 'Checked_In', 'Checked_Out')")
       BigDecimal revenueOnDate(@Param("date") LocalDate date);

       @Query("SELECT COALESCE(SUM(b.totalPrice), 0) FROM RoomBooking b WHERE b.bookingDate >= :start AND b.bookingDate <= :end AND b.bookingStatus IN ('Confirmed', 'Checked_In', 'Checked_Out')")
       BigDecimal revenueBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

       @Query(value = "SELECT COALESCE(AVG(DATEDIFF(check_out_date, check_in_date)), 0) FROM Room_Bookings", nativeQuery = true)
       Double getAverageStayDuration();

       @Query("SELECT COUNT(rbd) FROM RoomBookingDetail rbd " +
                     "WHERE rbd.category.categoryName = :categoryName " +
                     "AND rbd.roomBooking.checkInDate < :checkOut " +
                     "AND rbd.roomBooking.checkOutDate > :checkIn " +
                     "AND rbd.roomBooking.bookingStatus != 'CANCELLED' " +
                     "AND rbd.roomBooking.id != :excludeBookingId")
       long countOverlappingBookingsByCategory(
                     @Param("categoryName") String categoryName,
                     @Param("checkIn") LocalDate checkIn,
                     @Param("checkOut") LocalDate checkOut,
                     @Param("excludeBookingId") Long excludeBookingId);

       @Query("SELECT COUNT(rbd) FROM RoomBookingDetail rbd " +
                     "WHERE rbd.category.categoryName = :categoryName " +
                     "AND rbd.roomBooking.checkInDate < :checkOut " +
                     "AND rbd.roomBooking.checkOutDate > :checkIn " +
                     "AND rbd.roomBooking.bookingStatus != 'CANCELLED'")
       long countOverlappingBookingsByCategoryWithoutExclude(
                     @Param("categoryName") String categoryName,
                     @Param("checkIn") LocalDate checkIn,
                     @Param("checkOut") LocalDate checkOut);
}