package com.kawai.services.impl;

import com.kawai.dto.TourSearchResult;
import com.kawai.dto.WeatherInfo;
import com.kawai.models.Tour;
import com.kawai.models.TourSchedule;
import com.kawai.repositories.TourScheduleRepository;
import com.kawai.services.interfaces.TourService;
import com.kawai.services.interfaces.WeatherApiClient;

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

    private final TourScheduleRepository tourScheduleRepository;
    private final WeatherApiClient weatherApiClient;

    public TourServiceImpl(TourScheduleRepository tourScheduleRepository,
            WeatherApiClient weatherApiClient) {
        this.tourScheduleRepository = tourScheduleRepository;
        this.weatherApiClient = weatherApiClient;
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
        result.setAvailableSlots(schedule.getAvailableSlots());
        result.setDepartureDate(schedule.getDepartureDate());
        result.setScheduleStatus(schedule.getScheduleStatus());
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
}