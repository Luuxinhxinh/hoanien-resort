package com.kawai.services.interfaces;

import com.kawai.dto.walkin.WalkInCheckInRequest;
import com.kawai.dto.walkin.WalkInCheckInResponse;
import com.kawai.dto.walkin.WalkInSurchargeResponse;

public interface WalkInCheckInService {

    /**
     * Thực hiện toàn bộ luồng Walk-in Check-in (16 bước theo UC-14):
     * 1. Validate thông tin định danh (CCCD format, dateOfBirth not-null)
     * 2. Lock phòng chỉ định bằng Pessimistic Lock
     * 3. Validate trạng thái phòng (Vacant_Clean)
     * 4. Validate số lượng khách (soft limit → surcharge; hard limit → reject)
     * 5. Find-or-Create Customer profile
     * 6. Auto-create Account nếu khách mới (BR-08/09)
     * 7. Tạo RoomBooking với bookingSource=WALK_IN
     * 8. Tính và lưu extra surcharge vào RoomBookingDetail
     * 9. Cập nhật trạng thái phòng → OCCUPIED
     * 10. Lưu Dependent records cho khách đi kèm (BR-07)
     * 11. Publish RoomCheckedInEvent
     *
     * @param request Dữ liệu Walk-in từ Lễ tân
     * @return WalkInCheckInResponse chứa bookingId, status, customerId,
     *         isNewCustomer
     * @throws com.kawai.exceptions.BusinessException nếu validation thất bại hoặc
     *                                                transaction rollback
     */
    WalkInCheckInResponse createWalkInBookingAndCheckIn(WalkInCheckInRequest request);
    void cancelPendingWalkIn(Long bookingId);

    java.util.Optional<com.kawai.models.Customer> searchCustomer(String keyword);

    /**
     * Tính trước phụ thu (Surcharge) dựa trên độ tuổi khách để hiển thị cho Lễ tân.
     * 
     * @param request Chứa dateOfBirth của khách chính và khách đi kèm, kèm theo
     *                roomId
     * @return WalkInSurchargeResponse chứa số tiền phụ thu và thông tin phân loại
     *         khách
     */
    WalkInSurchargeResponse calculateSurchargePreview(WalkInCheckInRequest request);
}
