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

       /**
        * Đếm booking trùng ngày cho 1 phòng cụ thể, bỏ qua booking của chính mình
        * (excludeBookingId).
        * Dùng trong createBooking() để kiểm tra phòng đã bị booking/HOLD bởi người
        * khác chưa.
        * Status HOLD và CONFIRMED đều được đếm (chỉ bỏ CANCELLED).
        */
       @Query("SELECT COUNT(rbd) FROM RoomBookingDetail rbd " +
                     "WHERE rbd.room.roomNumber = :roomNumber " +
                     "AND rbd.roomBooking.checkInDate < :checkOut " +
                     "AND rbd.roomBooking.checkOutDate > :checkIn " +
                     "AND rbd.roomBooking.bookingStatus != 'CANCELLED' " +
                     "AND rbd.roomBooking.id != :excludeBookingId")
       long countOverlappingBookingsByRoom(
                     @Param("roomNumber") String roomNumber,
                     @Param("checkIn") LocalDate checkIn,
                     @Param("checkOut") LocalDate checkOut,
                     @Param("excludeBookingId") Long excludeBookingId);

       @Query("SELECT rb FROM RoomBooking rb WHERE rb.bookingStatus = 'HOLD' AND rb.holdExpiresAt <= :now")
       List<RoomBooking> findStaleHolds(@Param("now") java.time.LocalDateTime now);

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