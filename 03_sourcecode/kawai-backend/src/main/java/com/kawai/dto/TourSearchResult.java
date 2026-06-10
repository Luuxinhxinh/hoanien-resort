package com.kawai.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO trả về kết quả tìm kiếm tour cho UC19.
 * Chứa thông tin tour + thông tin thời tiết từ API bên ngoài.
 */
public class TourSearchResult {

    private Long tourId;
    private String tourName;
    private String tourType;
    private BigDecimal basePrice;
    private Integer maxCapacity;
    private Integer availableSlots;
    private LocalDate departureDate;
    private String scheduleStatus;

    // Thông tin thời tiết từ OpenWeather API
    private String weatherDescription;
    private Double temperature;
    private boolean weatherAvailable;

    public TourSearchResult() {}

    // --- Getters & Setters ---

    public Long getTourId() { return tourId; }
    public void setTourId(Long tourId) { this.tourId = tourId; }

    public String getTourName() { return tourName; }
    public void setTourName(String tourName) { this.tourName = tourName; }

    public String getTourType() { return tourType; }
    public void setTourType(String tourType) { this.tourType = tourType; }

    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }

    public Integer getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(Integer maxCapacity) { this.maxCapacity = maxCapacity; }

    public Integer getAvailableSlots() { return availableSlots; }
    public void setAvailableSlots(Integer availableSlots) { this.availableSlots = availableSlots; }

    public LocalDate getDepartureDate() { return departureDate; }
    public void setDepartureDate(LocalDate departureDate) { this.departureDate = departureDate; }

    public String getScheduleStatus() { return scheduleStatus; }
    public void setScheduleStatus(String scheduleStatus) { this.scheduleStatus = scheduleStatus; }

    public String getWeatherDescription() { return weatherDescription; }
    public void setWeatherDescription(String weatherDescription) { this.weatherDescription = weatherDescription; }

    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }

    public boolean isWeatherAvailable() { return weatherAvailable; }
    public void setWeatherAvailable(boolean weatherAvailable) { this.weatherAvailable = weatherAvailable; }
}
