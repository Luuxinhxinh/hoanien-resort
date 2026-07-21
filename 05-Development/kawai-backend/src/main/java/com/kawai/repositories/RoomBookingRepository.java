package com.kawai.repositories;

import com.kawai.models.RoomBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.kawai.models.Customer;

@Repository
public interface RoomBookingRepository extends JpaRepository<RoomBooking, Long> {
       @Query("SELECT COUNT(rb) FROM RoomBooking rb WHERE rb.checkInDate = :date AND rb.bookingStatus = 'Confirmed'")
       long countCheckInsOnDate(@Param("date") LocalDate date);

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
                     "AND LOWER(rbd.roomBooking.bookingStatus) NOT LIKE '%cancel%' " +
                     "AND rbd.roomBooking.bookingStatus != 'Pending'")
       long countOverlappingBookings(@Param("roomNumber") String roomNumber,
                     @Param("checkIn") LocalDate checkIn,
                     @Param("checkOut") LocalDate checkOut);

       List<RoomBooking> findByBookingStatusInAndHoldExpiresAtBefore(List<String> statuses, LocalDateTime time);

       List<RoomBooking> findByBookingStatusAndCheckInDateBefore(String status, LocalDate date);

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
                     "AND LOWER(rbd.roomBooking.bookingStatus) NOT LIKE '%cancel%' " +
                     "AND rbd.roomBooking.bookingStatus != 'Pending'")
       long countOverlappingBookingsByCategoryWithoutExclude(
                     @Param("categoryName") String categoryName,
                     @Param("checkIn") LocalDate checkIn,
                     @Param("checkOut") LocalDate checkOut);

       @Query("SELECT COUNT(rbd) FROM RoomBookingDetail rbd " +
                     "WHERE rbd.category.categoryName = :categoryName " +
                     "AND rbd.roomBooking.checkInDate < :checkOut " +
                     "AND rbd.roomBooking.checkOutDate > :checkIn " +
                     "AND LOWER(rbd.roomBooking.bookingStatus) NOT LIKE '%cancel%' " +
                     "AND rbd.roomBooking.bookingStatus != 'Pending' " +
                     "AND rbd.roomBooking.bookingStatus != 'Checked_In'")
       long countOverlappingNotCheckedIn(
                     @Param("categoryName") String categoryName,
                     @Param("checkIn") LocalDate checkIn,
                     @Param("checkOut") LocalDate checkOut);

        @Query(value = "SELECT c.category_name, SUM(c.capacity) FROM room_booking_details d JOIN room_categories c ON d.category_id = c.category_id JOIN room_bookings rb ON d.room_booking_id = rb.room_booking_id JOIN bookings b ON rb.room_booking_id = b.booking_id WHERE b.booking_status IN ('Confirmed', 'Checked_In', 'Checked_Out') GROUP BY c.category_name", nativeQuery = true)
        List<Object[]> getGuestCapacityByCategory();

        @Query(value = "SELECT c.category_name, COALESCE(AVG(DATEDIFF(rb.check_out_date, rb.check_in_date)), 0) FROM room_booking_details d JOIN room_bookings rb ON d.room_booking_id = rb.room_booking_id JOIN bookings b ON rb.room_booking_id = b.booking_id JOIN room_categories c ON d.category_id = c.category_id WHERE b.booking_status IN ('Confirmed', 'Checked_In', 'Checked_Out') GROUP BY c.category_name", nativeQuery = true)
        List<Object[]> getAverageStayDurationByCategory();
}