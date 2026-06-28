package com.kawai.repositories;

import com.kawai.models.TourBooking;
import com.kawai.models.TourSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository cho TourBooking entity (UC20.1: Đặt tour).
 * Hỗ trợ truy vấn booking theo schedule để kiểm tra double-booking.
 */
@Repository
public interface TourBookingRepository extends JpaRepository<TourBooking, Long> {

    /**
     * Đếm số lượng booking đã được xác nhận cho một lịch trình tour.
     * Dùng để kiểm tra available slots trước khi cho phép đặt thêm.
     *
     * @param schedule lịch trình tour cần kiểm tra
     * @return tổng số participantCount đã đặt (confirmed bookings)
     */
    int countByScheduleAndBookingStatus(TourSchedule schedule, String bookingStatus);

    List<TourBooking> findBySchedule(TourSchedule schedule);

    List<TourBooking> findByCustomer(com.kawai.models.Customer customer);

    /**
     * Lấy các TourBookings đã đặt cho một RoomBooking nhưng CHƯA được phân bổ vào
     * phòng cụ thể (roomBookingDetail IS NULL).
     * Dùng tại check-in: lễ tân sẽ phân bổ các tour này vào phòng vật lý.
     */
    // List<TourBooking> findByRoomBookingIdAndRoomBookingDetailIsNull(Long roomBookingId);

    /**
     * Lấy tất cả TourBookings của một RoomBooking (kể cả đã phân bổ và chưa).
     */
    // List<TourBooking> findByRoomBookingId(Long roomBookingId);

    @org.springframework.data.jpa.repository.Query(value = "SELECT t.tour_name FROM tour_bookings b JOIN tour_schedules s ON b.schedule_id = s.schedule_id JOIN tours t ON s.tour_id = t.tour_id GROUP BY t.tour_id ORDER BY COUNT(b.booking_id) DESC LIMIT 1", nativeQuery = true)
    String findTopTourName();

    @org.springframework.data.jpa.repository.Query(value = "SELECT COUNT(b.booking_id) FROM tour_bookings b JOIN tour_schedules s ON b.schedule_id = s.schedule_id JOIN tours t ON s.tour_id = t.tour_id GROUP BY t.tour_id ORDER BY COUNT(b.booking_id) DESC LIMIT 1", nativeQuery = true)
    Integer findTopTourBookings();

    @org.springframework.data.jpa.repository.Query("SELECT SUM(b.totalPrice) FROM TourBooking b WHERE b.bookingDate = :date AND b.bookingStatus = 'Confirmed'")
    java.math.BigDecimal revenueOnDate(
            @org.springframework.data.repository.query.Param("date") java.time.LocalDate date);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(b.totalPrice), 0) FROM TourBooking b WHERE b.bookingDate >= :start AND b.bookingDate <= :end AND b.bookingStatus = 'Confirmed'")
    java.math.BigDecimal revenueBetween(
            @org.springframework.data.repository.query.Param("start") java.time.LocalDate start,
            @org.springframework.data.repository.query.Param("end") java.time.LocalDate end);

    @org.springframework.data.jpa.repository.Query(value = "SELECT t.tour_name, t.tour_type, COUNT(b.booking_id), COALESCE(SUM(bk.total_price), 0), t.base_price FROM tours t LEFT JOIN tour_schedules s ON t.tour_id = s.tour_id LEFT JOIN tour_bookings b ON s.schedule_id = b.schedule_id LEFT JOIN bookings bk ON b.booking_id = bk.booking_id AND bk.booking_status = 'Confirmed' GROUP BY t.tour_id", nativeQuery = true)
    java.util.List<Object[]> getTourAnalytics();
}