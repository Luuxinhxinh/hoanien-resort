package com.kawai.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO chứa thông tin chi tiết của RoomBooking để hiển thị ở trang thanh toán
 * (payment.html).
 */
public class BookingDetailResponseDTO {

    private String hotelName = "HoaNien Resort";
    private List<String> roomCategories;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer totalAdults;
    private Integer totalChildren;
    private BigDecimal baseRoomPrice;
    private BigDecimal servicesFee;
    private BigDecimal promotionDiscount;
    private BigDecimal totalAmount;
    private BigDecimal depositAmount;
    private String bookingStatus;
    private Long remainingHoldSeconds;

    // Constructors
    public BookingDetailResponseDTO() {
    }

    // Getters and Setters
    public String getHotelName() {
        return hotelName;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    public List<String> getRoomCategories() {
        return roomCategories;
    }

    public void setRoomCategories(List<String> roomCategories) {
        this.roomCategories = roomCategories;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public Integer getTotalAdults() {
        return totalAdults;
    }

    public void setTotalAdults(Integer totalAdults) {
        this.totalAdults = totalAdults;
    }

    public Integer getTotalChildren() {
        return totalChildren;
    }

    public void setTotalChildren(Integer totalChildren) {
        this.totalChildren = totalChildren;
    }

    public BigDecimal getBaseRoomPrice() {
        return baseRoomPrice;
    }

    public void setBaseRoomPrice(BigDecimal baseRoomPrice) {
        this.baseRoomPrice = baseRoomPrice;
    }

    public BigDecimal getServicesFee() {
        return servicesFee;
    }

    public void setServicesFee(BigDecimal servicesFee) {
        this.servicesFee = servicesFee;
    }

    public BigDecimal getPromotionDiscount() {
        return promotionDiscount;
    }

    public void setPromotionDiscount(BigDecimal promotionDiscount) {
        this.promotionDiscount = promotionDiscount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(BigDecimal depositAmount) {
        this.depositAmount = depositAmount;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public Long getRemainingHoldSeconds() {
        return remainingHoldSeconds;
    }

    public void setRemainingHoldSeconds(Long remainingHoldSeconds) {
        this.remainingHoldSeconds = remainingHoldSeconds;
    }
}
