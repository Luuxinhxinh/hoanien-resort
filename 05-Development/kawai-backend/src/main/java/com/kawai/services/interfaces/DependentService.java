package com.kawai.services.interfaces;

import com.kawai.dto.DependentRegistrationDTO;
import com.kawai.dto.DependentResponseDTO;

import java.util.List;

/**
 * DependentService — UC16: Register Accompanying Guests
 *
 * @version 1.0
 *
 * Business Rules:
 * BR-SYS-01 : CCCD/Hộ chiếu phải mã hoá AES-256 trước khi lưu DB
 * BR-FO-07  : Chỉ dependent đã đăng ký mới được cấp quyền dịch vụ (UC17)
 * ADR-002   : Kiểm tra trùng lặp theo booking_id + cccd_encrypted
 */
public interface DependentService {

    /**
     * Đăng ký khách đi kèm mới vào reservation.
     * Mã hoá CCCD bằng AES-256 trước khi lưu (BR-SYS-01).
     *
     * @param bookingId ID booking đang hoạt động (Confirmed hoặc Checked_In)
     * @param dto       Thông tin khách đi kèm
     * @return DependentResponseDTO với dependentId và status="REGISTERED"
     * @throws com.kawai.exceptions.BusinessException errorCode=MOD2-003 nếu booking không tồn tại
     * @throws com.kawai.exceptions.BusinessException errorCode=MOD2-015 nếu booking không hoạt động
     * @throws com.kawai.exceptions.BusinessException errorCode=MOD2-016 nếu CCCD đã đăng ký trong booking này
     * @throws com.kawai.exceptions.BusinessException errorCode=MOD2-017 nếu CCCD/Passport không hợp lệ
     */
    DependentResponseDTO registerDependent(Long bookingId, DependentRegistrationDTO dto);

    /**
     * Lấy danh sách toàn bộ khách đi kèm của một booking.
     *
     * @param bookingId ID booking cần tra cứu
     * @return Danh sách DependentResponseDTO
     */
    List<DependentResponseDTO> getGuestListByBooking(Long bookingId);
}
