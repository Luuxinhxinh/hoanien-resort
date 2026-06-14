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
     *
     * @param bookingDetailId ID chi tiết booking cần check-in
     * @param roomId          ID phòng vật lý sẽ gán cho khách
     * @return RoomBookingDetail đã cập nhật trạng thái
     * @throws IllegalStateException nếu phòng đang DIRTY hoặc MAINTENANCE
     */
    RoomBookingDetail checkIn(Long bookingDetailId, Long roomId);

    /**
     * Ủy quyền hạn mức chi tiêu cho phòng (UC12.2).
     *
     * @param bookingDetailId ID chi tiết booking
     * @param newCreditLimit  hạn mức mới
     */
    void updateCreditLimit(Long bookingDetailId, BigDecimal newCreditLimit);

    /**
     * Đổi phòng vật lý cho khách đang lưu trú (UC12.3).
     *
     * @param bookingDetailId ID chi tiết booking
     * @param newRoomId       ID phòng mới
     * @return RoomBookingDetail đã chuyển sang phòng mới
     * @throws IllegalStateException nếu phòng mới không khả dụng
     */
    RoomBookingDetail transferRoom(Long bookingDetailId, Long newRoomId);

    /**
     * Nâng cấp Dependent thành Customer (UC12.4).
     *
     * @param dependentId ID người phụ thuộc
     * @return Customer mới được tạo từ thông tin Dependent
     */
    Customer upgradeDependentToCustomer(Long dependentId);
}