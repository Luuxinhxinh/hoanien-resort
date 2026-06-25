package com.kawai.services.interfaces;

import com.kawai.models.Customer;
import com.kawai.models.RoomBookingDetail;

import java.math.BigDecimal;

/**
 * CheckinService — UC12: Check-in / Check-out / Đổi phòng
 *
 * Business Rules:
 * BR-FO-03: Ràng buộc tuổi Check-in (≥18)
 * BR-FO-04: Luân chuyển trạng thái phòng (Vacant_Clean → Occupied → Dirty)
 * BR-FO-06: Hạn mức chi tiêu phòng (Credit Limit)
 * BR-FO-08: Khai báo tạm trú (thu thập CCCD/Hộ chiếu)
 * BR-FO-09: Phụ phí Early Check-in / Late Check-out
 */
public interface CheckinService {

    /**
     * Check-in khách vào phòng (UC12.1).
     */
    RoomBookingDetail checkIn(Long bookingDetailId, Long roomId);

    /**
     * Ủy quyền hạn mức chi tiêu cho phòng (UC12.2).
     */
    void updateCreditLimit(Long bookingDetailId, BigDecimal newCreditLimit);

    /**
     * BR-FO-09: Xử lý tự động đánh dấu No-Show cho khách không đến
     */
    void markAsNoShow(Long bookingId);

    /**
     * Đổi phòng vật lý cho khách đang lưu trú (UC12.3).
     */
    RoomBookingDetail transferRoom(Long bookingDetailId, Long newRoomId);

    Customer upgradeDependentToCustomer(Long dependentId);
}