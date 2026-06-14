package com.kawai.repositories;

import com.kawai.models.TourAttendee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository cho TourAttendee entity (UC20.1: Đặt tour).
 * Lưu danh sách chi tiết từng cá nhân tham gia tour.
 */
@Repository
public interface TourAttendeeRepository extends JpaRepository<TourAttendee, Long> {
    java.util.List<TourAttendee> findByTourBooking_Schedule_DepartureDate(java.time.LocalDate date);
}