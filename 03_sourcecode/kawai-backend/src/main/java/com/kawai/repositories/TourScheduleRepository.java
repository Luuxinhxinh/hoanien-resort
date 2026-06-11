package com.kawai.repositories;

import com.kawai.models.TourSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository truy vấn lịch trình tour.
 * Hỗ trợ UC19: Tìm kiếm tour khả dụng theo khoảng ngày.
 */
@Repository
public interface TourScheduleRepository extends JpaRepository<TourSchedule, Long> {

    /**
     * Tìm tất cả lịch trình tour có ngày khởi hành trong khoảng [fromDate, toDate]
     * và trạng thái lịch trình là "Open" (còn nhận khách).
     */
    List<TourSchedule> findByDepartureDateBetweenAndScheduleStatus(
            LocalDate fromDate, LocalDate toDate, String scheduleStatus);
}
