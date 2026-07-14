package com.kawai.dto;

import lombok.Data;

/**
 * DTO cho yêu cầu hủy đơn đặt phòng.
 * Chứa thông tin tài khoản ngân hàng để hoàn tiền thủ công.
 */
@Data
public class CancelBookingRequestDTO {

    /**
     * Số tài khoản ngân hàng
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

    /**
     * Số điện thoại chủ tài khoản
     */
    private String phoneNumber;
}
