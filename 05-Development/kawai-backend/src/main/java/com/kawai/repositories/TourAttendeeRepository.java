package com.kawai.repositories;

import com.kawai.models.TourAttendee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository cho TourAttendee entity (UC20.1: Đặt tour).
 * Lưu danh sách chi tiết từng cá nhân tham gia tour.
 */
@Repository
public interface TourAttendeeRepository extends JpaRepository<TourAttendee, Long> {
    java.util.List<TourAttendee> findByTourBooking_Schedule_DepartureDate(java.time.LocalDate date);
    java.util.List<TourAttendee> findByTourBooking_Schedule_Id(Long scheduleId);
    java.util.List<TourAttendee> findByTourBookingId(Long bookingId);
    boolean existsByCustomerIdAndTourBookingScheduleIdAndTourBookingBookingStatusNot(Long customerId, Long scheduleId, String status);

    @Query("SELECT CASE WHEN COUNT(ta) > 0 THEN true ELSE false END FROM TourAttendee ta WHERE ta.customer.id = :customerId AND ta.tourBooking.schedule.id = :scheduleId AND LOWER(ta.tourBooking.bookingStatus) NOT LIKE '%cancel%'")
    boolean existsActiveAttendeeByCustomerAndSchedule(@Param("customerId") Long customerId, @Param("scheduleId") Long scheduleId);

    @Query("SELECT ta FROM TourAttendee ta WHERE ta.tourBooking.roomBookingDetail.id = :detailId AND LOWER(ta.tourBooking.bookingStatus) NOT LIKE '%cancel%'")
    java.util.List<TourAttendee> findActiveAttendeesByRoomBookingDetailId(@Param("detailId") Long detailId);
}