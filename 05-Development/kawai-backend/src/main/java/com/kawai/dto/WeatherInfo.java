package com.kawai.dto;

/**
 * DTO chứa thông tin thời tiết trả về từ OpenWeather API.
 * Dùng nội bộ trong TourService để map dữ liệu trước khi gộp vào TourSearchResult.
 */
public class WeatherInfo {

    private String description;
    private Double temperature;

    public WeatherInfo() {}

    public WeatherInfo(String description, Double temperature) {
        this.description = description;
        this.temperature = temperature;
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }
}
