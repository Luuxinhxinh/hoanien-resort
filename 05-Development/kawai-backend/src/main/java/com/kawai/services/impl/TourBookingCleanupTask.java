package com.kawai.services.impl;

import com.kawai.models.TourBooking;
import com.kawai.models.TourSchedule;
import com.kawai.repositories.TourBookingRepository;
import com.kawai.repositories.TourScheduleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
public class TourBookingCleanupTask {

    private static final Logger log = LoggerFactory.getLogger(TourBookingCleanupTask.class);
    private final TourBookingRepository tourBookingRepository;
    private final TourScheduleRepository tourScheduleRepository;

    public TourBookingCleanupTask(
            TourBookingRepository tourBookingRepository,
            TourScheduleRepository tourScheduleRepository) {
        this.tourBookingRepository = tourBookingRepository;
        this.tourScheduleRepository = tourScheduleRepository;
    }

    // Chạy vào 00:01 mỗi ngày
    @Scheduled(cron = "0 1 0 * * ?")
    @Transactional
    public void cleanupPastTourBookings() {
        LocalDate today = LocalDate.now();
        
        // Cập nhật trạng thái TourSchedule và TourBooking đã qua ngày thành Completed
        List<TourSchedule> pastSchedules = tourScheduleRepository.findAll().stream()
                .filter(s -> s.getDepartureDate() != null && s.getDepartureDate().isBefore(today))
                .filter(s -> "Confirmed".equalsIgnoreCase(s.getScheduleStatus()) || "Open".equalsIgnoreCase(s.getScheduleStatus()) || "In_Progress".equalsIgnoreCase(s.getScheduleStatus()))
                .collect(java.util.stream.Collectors.toList());
                
        if (!pastSchedules.isEmpty()) {
            log.info("Bắt đầu cập nhật {} TourSchedule trong quá khứ sang Completed.", pastSchedules.size());
            for (TourSchedule schedule : pastSchedules) {
                schedule.setScheduleStatus("Completed");
                tourScheduleRepository.save(schedule);
                
                // Cập nhật tất cả bookings của schedule này
                List<TourBooking> bookings = tourBookingRepository.findByScheduleId(schedule.getId());
                for (TourBooking booking : bookings) {
                    if ("Confirmed".equalsIgnoreCase(booking.getBookingStatus()) || "In_Progress".equalsIgnoreCase(booking.getBookingStatus())) {
                        booking.setBookingStatus("Completed");
                        tourBookingRepository.save(booking);
                    }
                }
            }
        }

        // Cập nhật các tour hôm nay thành In_Progress
        List<TourSchedule> todaySchedules = tourScheduleRepository.findAll().stream()
                .filter(s -> s.getDepartureDate() != null && s.getDepartureDate().isEqual(today))
                .filter(s -> "Confirmed".equalsIgnoreCase(s.getScheduleStatus()) || "Open".equalsIgnoreCase(s.getScheduleStatus()))
                .collect(java.util.stream.Collectors.toList());
                
        if (!todaySchedules.isEmpty()) {
            log.info("Bắt đầu cập nhật {} TourSchedule hôm nay sang In_Progress.", todaySchedules.size());
            for (TourSchedule schedule : todaySchedules) {
                schedule.setScheduleStatus("In_Progress");
                tourScheduleRepository.save(schedule);
                
                List<TourBooking> bookings = tourBookingRepository.findByScheduleId(schedule.getId());
                for (TourBooking booking : bookings) {
                    if ("Confirmed".equalsIgnoreCase(booking.getBookingStatus())) {
                        booking.setBookingStatus("In_Progress");
                        tourBookingRepository.save(booking);
                    }
                }
            }
        }
    }
}
