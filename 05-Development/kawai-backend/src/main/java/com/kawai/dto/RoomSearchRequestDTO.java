package com.kawai.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO cho yêu cầu tìm kiếm phòng trống (UC09).
 */
public class RoomSearchRequestDTO {

    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private String categoryName; // nullable: lọc theo hạng phòng
    private Integer minCapacity; // nullable: số khách tối thiểu
    private BigDecimal maxPricePerNight; // nullable: giá tối đa mỗi đêm

    public RoomSearchRequestDTO() {
    }

    public RoomSearchRequestDTO(LocalDate checkInDate, LocalDate checkOutDate) {
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
    }

    // ------- Getters / Setters -------
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

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String v) {
        this.categoryName = v;
    }

    public Integer getMinCapacity() {
        return minCapacity;
    }

    public void setMinCapacity(Integer v) {
        this.minCapacity = v;
    }

    public BigDecimal getMaxPricePerNight() {
        return maxPricePerNight;
    }

    public void setMaxPricePerNight(BigDecimal v) {
        this.maxPricePerNight = v;
    }

    private Integer page;
    private Integer size;

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }
}