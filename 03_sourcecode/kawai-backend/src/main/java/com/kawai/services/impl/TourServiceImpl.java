package com.kawai.services.impl;

import com.kawai.dto.TourSearchResult;
import com.kawai.dto.WeatherInfo;
import com.kawai.models.Tour;
import com.kawai.models.TourSchedule;
import com.kawai.models.TourAttendee;
import com.kawai.repositories.TourScheduleRepository;
import com.kawai.repositories.TourAttendeeRepository;
import com.kawai.services.interfaces.TourService;
import com.kawai.services.interfaces.WeatherApiClient;
import com.kawai.services.interfaces.AIServiceClient;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Implementation của {@link TourService} cho UC19: Tìm kiếm gói tour.
 *
 * <p>
 * Chịu trách nhiệm tra cứu lịch trình tour còn nhận khách (trạng thái "Open")
 * trong khoảng ngày và tích hợp thông tin thời tiết từ OpenWeather API.
 *
 * <p>
 * Nguyên tắc Clean Code áp dụng:
 * <ul>
 * <li>Single Responsibility: Mỗi method làm đúng 1 việc</li>
 * <li>Fail Fast: Validate đầu vào ngay từ method public</li>
 * <li>Error Handling: Graceful degradation — không để exception từ third-party
 * làm hỏng luồng chính</li>
 * <li>Magic Number/String: Tất cả hằng số được khai báo tập trung</li>
 * </ul>
 *
 * <p>
 * TDD — Implementation pass 6/6 test cases (TC-M4-001 + TC-M4-002).
 */
@Service
public class TourServiceImpl implements TourService {

    private static final Logger LOG = LoggerFactory.getLogger(TourServiceImpl.class);

    private static final String SCHEDULE_STATUS_OPEN = "Open";
    private static final double MIN_MATCH_SCORE_FOR_ATTENDANCE = 0.85; // BR-TR-02
    private static final String ATTENDANCE_STATUS_PRESENT = "PRESENT";

    private final TourScheduleRepository tourScheduleRepository;
    private final WeatherApiClient weatherApiClient;
    private final TourAttendeeRepository tourAttendeeRepository;
    private final AIServiceClient aiServiceClient;

    public TourServiceImpl(TourScheduleRepository tourScheduleRepository,
            WeatherApiClient weatherApiClient,
            TourAttendeeRepository tourAttendeeRepository,
            AIServiceClient aiServiceClient) {
        this.tourScheduleRepository = tourScheduleRepository;
        this.weatherApiClient = weatherApiClient;
        this.tourAttendeeRepository = tourAttendeeRepository;
        this.aiServiceClient = aiServiceClient;
    }

    @Override
    public List<TourSearchResult> searchAvailableTours(LocalDate fromDate, LocalDate toDate) {
        validateDateRange(fromDate, toDate);

        List<TourSchedule> schedules = tourScheduleRepository
                .findByDepartureDateBetweenAndScheduleStatus(fromDate, toDate, SCHEDULE_STATUS_OPEN);

        if (schedules.isEmpty()) {
            return Collections.emptyList();
        }

        return schedules.stream()
                .map(this::toSearchResult)
                .collect(Collectors.toList());
    }

    /**
     * Kiểm tra tính hợp lệ của khoảng ngày tìm kiếm.
     *
     * @throws IllegalArgumentException nếu fromDate null, toDate null, hoặc
     *                                  fromDate > toDate
     */
    private void validateDateRange(LocalDate fromDate, LocalDate toDate) {
        Objects.requireNonNull(fromDate, "fromDate must not be null");
        Objects.requireNonNull(toDate, "toDate must not be null");
        if (fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException(
                    "fromDate (" + fromDate + ") must not be after toDate (" + toDate + ")");
        }
    }

    /**
     * Chuyển đổi {@link TourSchedule} entity thành {@link TourSearchResult} DTO.
     * Gọi API thời tiết cho ngày khởi hành với graceful degradation nếu lỗi.
     */
    private TourSearchResult toSearchResult(TourSchedule schedule) {
        Tour tour = schedule.getTour();
        TourSearchResult result = mapTourFields(schedule, tour);
        enrichWithWeatherData(result, schedule.getDepartureDate());
        return result;
    }

    /**
     * Map các field từ entity sang DTO.
     * Tách riêng để dễ đọc và dễ maintain.
     */
    private TourSearchResult mapTourFields(TourSchedule schedule, Tour tour) {
        TourSearchResult result = new TourSearchResult();
        result.setTourId(tour.getId());
        result.setTourName(tour.getTourName());
        result.setTourType(tour.getTourType());
        result.setBasePrice(tour.getBasePrice());
        result.setMaxCapacity(tour.getMaxCapacity());
        result.setAvailableSlots(tour.getMaxCapacity() - schedule.getBookedSeats());
        result.setDepartureDate(schedule.getDepartureDate());
        result.setScheduleStatus(schedule.getScheduleStatus());
        result.setDescription(tour.getDescription());
        return result;
    }

    /**
     * Gắn thông tin thời tiết vào {@link TourSearchResult}.
     *
     * <p>
     * Nếu API lỗi (RuntimeException) hoặc trả về null, đặt
     * {@code weatherAvailable = false} và log warning — không fail toàn bộ request.
     * Đây là pattern {@code Circuit Breaker} ở mức đơn giản.
     */
    private void enrichWithWeatherData(TourSearchResult result, LocalDate departureDate) {
        try {
            WeatherInfo weather = weatherApiClient.getWeatherForDate(departureDate);
            if (weather != null) {
                result.setWeatherAvailable(true);
                result.setWeatherDescription(weather.getDescription());
                result.setTemperature(weather.getTemperature());
            } else {
                LOG.warn("Weather API returned null for date: {}", departureDate);
                result.setWeatherAvailable(false);
            }
        } catch (RuntimeException e) {
            LOG.warn("Weather API failed for date {}: {}", departureDate, e.getMessage());
            result.setWeatherAvailable(false);
        }
    }

    /**
     * Xác thực điểm danh bằng AI Face Scan.
     * Cập nhật trạng thái thành PRESENT nếu độ trùng khớp >= 85% (BR-TR-02).
     *
     * @param attendeeId ID của người tham gia tour
     * @param image      Ảnh chụp khuôn mặt khách hàng
     * @return true nếu điểm danh thành công, false nếu không đạt độ trùng khớp
     * @throws IllegalArgumentException nếu không tìm thấy attendee
     */
    @Override
    public boolean verifyAttendance(Long attendeeId, MultipartFile image) {
        TourAttendee attendee = getAttendeeById(attendeeId);
        double matchScore = aiServiceClient.verifyFaceMatch(image, attendeeId);
        
        if (matchScore >= MIN_MATCH_SCORE_FOR_ATTENDANCE) {
            attendee.setStatus(ATTENDANCE_STATUS_PRESENT);
            tourAttendeeRepository.save(attendee);
            return true;
        }
        return false;
    }

    /**
     * Điểm danh thủ công (Dự phòng khi AI lỗi).
     *
     * @param attendeeId ID của người tham gia tour
     * @param status     Trạng thái điểm danh (VD: PRESENT, ABSENT)
     * @throws IllegalArgumentException nếu không tìm thấy attendee
     */
    @Override
    public void markAttendanceManually(Long attendeeId, String status) {
        TourAttendee attendee = getAttendeeById(attendeeId);
        attendee.setStatus(status);
        tourAttendeeRepository.save(attendee);
    }

    /**
     * Helper: Lấy thông tin người tham gia tour theo ID.
     *
     * @param attendeeId ID của người tham gia tour
     * @return Entity TourAttendee
     * @throws IllegalArgumentException nếu không tìm thấy attendee
     */
    private TourAttendee getAttendeeById(Long attendeeId) {
        return tourAttendeeRepository.findById(attendeeId)
                .orElseThrow(() -> new IllegalArgumentException("Attendee not found with ID: " + attendeeId));
    }
}