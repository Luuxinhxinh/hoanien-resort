package com.kawai.services.interfaces;

import com.kawai.dto.BookingRequestDTO;
import com.kawai.dto.BookingResponseDTO;
import com.kawai.dto.BookingDetailResponseDTO;
import com.kawai.exceptions.RoomNotAvailableException;

import java.math.BigDecimal;

/**
 * BookingService — UC10: Đặt phòng & Thanh toán cọc trực tuyến
 * 
 * Business Rules:
 * BR-FO-01: Chống đặt trùng phòng (Pessimistic Locking)
 * BR-FO-02: Ràng buộc đặt cọc (30 phút)
 * BR-FIN-02: Chính sách hủy & hoàn tiền (48h)
 * BR-DATE-01: checkOutDate > checkInDate
 */
public interface BookingService {

    /**
     * Tạo booking mới (UC10.1).
     * 
     * @param request thông tin đặt phòng
     * @return BookingResponseDTO chứa thông tin booking + cancellationDeadline
     * @throws RoomNotAvailableException nếu phòng đã có booking trùng ngày
     *                                   (BR-FO-01)
     * @throws IllegalArgumentException  nếu:
     *                                   - checkOutDate <= checkInDate (BR-DATE-01)
     *                                   - promotionCode không hợp lệ (hết hạn,
     *                                   không active, không tồn tại) — UC10.2
     */
    BookingResponseDTO createBooking(BookingRequestDTO request)
            throws RoomNotAvailableException, IllegalArgumentException;

    /**
     * Hủy booking (UC10.1 — cancellation policy).
     * 
     * @param bookingId ID của booking cần hủy
     * @return Số tiền hoàn lại (0 nếu hủy trong 48h — BR-FIN-02)
     */
    BookingResponseDTO cancelBooking(Long bookingId, Long customerId, com.kawai.dto.CancelRequestDTO cancelRequest);

    /**
     * Lấy chi tiết booking (UC10.1).
     */
    BookingDetailResponseDTO getBookingDetail(Long bookingId, Long customerId);

    /**
     * Áp dụng mã coupon cho booking (UC10.2).
     */
    BigDecimal applyCoupon(Long bookingId, String couponCode, Long customerId);

    /**
     * Xác nhận thanh toán và cập nhật thông tin khách hàng (UC10).
     */
    void confirmBooking(Long bookingId, Long customerId, String fullName, String phone, String email,
            String cccd, String address, String notes, String paymentMethod, String birthDateStr);

    /**
     * Get folios for a booking based on role (Master Booker or Primary Contact).
     */
    java.util.List<java.util.Map<String, Object>> getBookingFolios(Long bookingId, Long customerId);

    /**
     * Delete a pending booking completely when user exits the payment flow.
     */
    void deletePendingBooking(Long bookingId, Long customerId);
}