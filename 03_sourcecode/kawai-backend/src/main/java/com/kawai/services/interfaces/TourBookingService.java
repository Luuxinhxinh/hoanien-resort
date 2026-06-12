package com.kawai.services.interfaces;

import com.kawai.dto.TourBookingRequest;
import java.math.BigDecimal;

/**
 * Service interface cho UC20.1: Đặt tour du lịch.
 *
 * <p>
 * Định nghĩa hợp đồng nghiệp vụ đặt tour, kiểm tra available slots
 * và hỗ trợ Post to Room.
 *
 * <p>
 * Business Rules áp dụng:
 * <ul>
 * <li>BR-TR-01: Chống Double-booking — kiểm tra availableSlots trước khi tạo
 * booking</li>
 * <li>Post to Room: Nếu {@code postToRoom = true}, ghi nợ vào Folio phòng</li>
 * </ul>
 *
 * @see com.kawai.dto.TourBookingRequest
 */
public interface TourBookingService {

    /**
     * Tạo booking tour cho khách hàng.
     *
     * <p>
     * Kiểm tra available slots của lịch trình tour trước khi tạo.
     * Nếu không đủ chỗ, throw exception với mã lỗi TOUR-001.
     * Nếu {@code postToRoom = true}, tự động ghi nợ vào Folio phòng.
     *
     * @param request thông tin đặt tour (scheduleId, customerId, participantCount,
     *                ...)
     * @return booking ID vừa được tạo
     * @throws IllegalStateException nếu tour đã hết chỗ (TOUR-001)
     */
    Long createTourBooking(TourBookingRequest request);

    /**
     * Lập lịch chuyến tour (gán nhân viên/xe).
     * (UC20.2)
     */
    void scheduleTour(Long scheduleId, Long employeeId, String staffRole);

    /**
     * Hủy tour lữ hành và tính toán tiền hoàn cọc.
     * (UC20.3)
     */
    BigDecimal cancelTour(Long bookingId, boolean cancelledByResort);
}