package com.kawai.dto;

import java.math.BigDecimal;

/**
 * DTO cho kết quả hiển thị trên Front Desk Dashboard (UC11).
 */
public class RoomDashboardDTO {

    private Long roomId;
    private String roomNumber;
    private String categoryName;
    private String roomStatus; // Vacant_Clean, Occupied, Dirty, Out_of_Order ...
    private BigDecimal pricePerNight;
    private Integer capacity;
    private String currentGuestName; // nullable — tên khách đang ở (nếu Occupied)

    public RoomDashboardDTO() {
    }

    // ------- Getters / Setters -------
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

    public String getRoomStatus() {
        return roomStatus;
    }

    public void setRoomStatus(String v) {
        this.roomStatus = v;
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

    public String getCurrentGuestName() {
        return currentGuestName;
    }

    public void setCurrentGuestName(String v) {
        this.currentGuestName = v;
    }
}