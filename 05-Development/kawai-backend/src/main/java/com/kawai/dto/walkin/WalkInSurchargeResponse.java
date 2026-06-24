package com.kawai.dto.walkin;

import java.math.BigDecimal;

/**
 * DTO trả về kết quả tính phí phụ thu dựa trên số khách trong luồng Walk-in
 * Check-in.
 */
public class WalkInSurchargeResponse {

    private BigDecimal surchargeAmount;
    private int adultsCount;
    private int childrenCount;
    private String message;
    private BigDecimal baseRoomPrice;
    private BigDecimal totalCharge;
    private BigDecimal suggestedDeposit;

    public WalkInSurchargeResponse() {
    }

    public WalkInSurchargeResponse(BigDecimal surchargeAmount, int adultsCount, int childrenCount, String message,
            BigDecimal baseRoomPrice, BigDecimal totalCharge, BigDecimal suggestedDeposit) {
        this.surchargeAmount = surchargeAmount;
        this.adultsCount = adultsCount;
        this.childrenCount = childrenCount;
        this.message = message;
        this.baseRoomPrice = baseRoomPrice;
        this.totalCharge = totalCharge;
        this.suggestedDeposit = suggestedDeposit;
    }

    public BigDecimal getSurchargeAmount() {
        return surchargeAmount;
    }

    public void setSurchargeAmount(BigDecimal surchargeAmount) {
        this.surchargeAmount = surchargeAmount;
    }

    public int getAdultsCount() {
        return adultsCount;
    }

    public void setAdultsCount(int adultsCount) {
        this.adultsCount = adultsCount;
    }

    public int getChildrenCount() {
        return childrenCount;
    }

    public void setChildrenCount(int childrenCount) {
        this.childrenCount = childrenCount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public BigDecimal getBaseRoomPrice() {
        return baseRoomPrice;
    }

    public void setBaseRoomPrice(BigDecimal baseRoomPrice) {
        this.baseRoomPrice = baseRoomPrice;
    }

    public BigDecimal getTotalCharge() {
        return totalCharge;
    }

    public void setTotalCharge(BigDecimal totalCharge) {
        this.totalCharge = totalCharge;
    }

    public BigDecimal getSuggestedDeposit() {
        return suggestedDeposit;
    }

    public void setSuggestedDeposit(BigDecimal suggestedDeposit) {
        this.suggestedDeposit = suggestedDeposit;
    }
}
