package com.kawai.dto;

import lombok.Data;

/**
 * DTO cho yêu cầu hủy đơn hàng F&B (UC19 / WF-24).
 * Chứa thông tin tài khoản ngân hàng để hoàn tiền (nếu cần).
 */
@Data
public class CancelOrderRequestDTO {
    /**
     * Số tài khoản ngân hàng (cần thiết cho VNPay refund)
     */
    private String accountNumber;

    /**
     * Tên ngân hàng (ví dụ: VCB, ACB, MB...)
     */
    private String bankName;

    /**
     * Tên chủ tài khoản
     */
    private String accountName;

    /**
     * Lý do hủy đơn (tùy chọn)
     */
    private String reason;
}