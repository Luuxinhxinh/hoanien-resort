package com.kawai.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Kết quả trả về khi tìm kiếm phòng trống (UC09).
 */
public class RoomSearchResponseDTO {

    private Long roomId;
    private String roomNumber;
    private String categoryName;
    private BigDecimal pricePerNight;
    private Integer capacity;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;

    public RoomSearchResponseDTO() {
    }

    // Getters / Setters
    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long v) {
        this.roomId = v;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String v) {
        this.roomNumber = v;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String v) {
        this.categoryName = v;
    }

    public BigDecimal getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(BigDecimal v) {
        this.pricePerNight = v;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer v) {
        this.capacity = v;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate v) {
        this.checkInDate = v;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate v) {
        this.checkOutDate = v;
    }
}