package com.kawai.services.interfaces;

import com.kawai.dto.WeatherInfo;

import java.time.LocalDate;

/**
 * Client interface để gọi API thời tiết bên ngoài (OpenWeather).
 * Được inject vào TourService để lấy thông tin thời tiết cho ngày khởi hành
 * tour.
 * <p>
 * Design: Interface segregation — tách rời implementation của API thời tiết
 * để dễ dàng mock trong Unit Test và đổi nhà cung cấp khi cần.
 */
public interface WeatherApiClient {

    /**
     * Lấy thông tin thời tiết cho một ngày cụ thể.
     *
     * @param date ngày cần lấy thời tiết
     * @return WeatherInfo chứa mô tả và nhiệt độ, hoặc null nếu không lấy được
     * @throws RuntimeException nếu API không phản hồi hoặc lỗi kết nối
     */
    WeatherInfo getWeatherForDate(LocalDate date);

    /**
     * Lấy thông tin thời tiết cho một ngày và địa điểm cụ thể.
     *
     * @param date ngày cần lấy thời tiết
     * @param location địa điểm (tỉnh/thành phố) cần lấy thời tiết
     * @return WeatherInfo chứa mô tả và nhiệt độ, hoặc null nếu không lấy được
     */
    WeatherInfo getWeatherForDateAndLocation(LocalDate date, String location);
}