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
}