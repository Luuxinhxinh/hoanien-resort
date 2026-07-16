package com.kawai.services.interfaces;

import com.kawai.models.HotelOperation;
import com.kawai.models.Room;

import java.util.List;

/**
 * HousekeepingService — UC13: Quản lý sơ đồ phòng vật lý (Room Matrix).
 * <p>
 * Business Rules:
 * BR-FO-04: Luân chuyển trạng thái phòng (Vacant_Clean → Occupied → Dirty)
 * BR-HK-01: Ghi nhận Mini-bar vào Folio
 * BR-HK-02: Báo cáo hư hỏng tài sản
 * BR-HK-03: Chặn Check-in phòng MAINTENANCE
 * BR-HK-04: Phân công dọn phòng theo tầng
 */
public interface HousekeepingService {

    /**
     * Check-out → tự động sinh yêu cầu dọn phòng (UC13.1).
     *
     * @param roomId  ID phòng vừa check-out
     * @param staffId ID nhân viên được phân công
     * @return HotelOperation task dọn dẹp
     */
    HotelOperation autoCreateHousekeepingTask(Long roomId, Long staffId);

    /**
     * Housekeeping cập nhật phòng DIRTY → CLEAN (UC13.2).
     *
     * @param taskId ID nhiệm vụ dọn dẹp
     * @return Room phòng đã được dọn sạch
     */
    Room updateRoomToClean(Long taskId, String notes);

    /**
     * Lễ tân xem danh sách yêu cầu dọn/sửa phòng (UC13.3).
     *
     * @return Danh sách HotelOperation
     */
    List<HotelOperation> getPendingOperations();

    /**
     * Tự động tạo task Maintenance cho các hư hỏng đã được ghi nhận giá.
     * @param room Phòng cần kiểm tra và tạo task bảo trì
     */
    void createMaintenanceTaskForPricedDamages(Room room);

    void escalateTaskByRoomNumber(String roomNumber);

    /**
     * Housekeeping tạo phiếu sửa chữa → phòng chuyển MAINTENANCE (UC13.4).
     *
     * @param roomId  ID phòng cần sửa
     * @param staffId ID nhân viên báo hỏng
     * @param notes   Mô tả sự cố
     * @return HotelOperation phiếu sửa chữa
     */
    HotelOperation createMaintenanceRequest(Long roomId, Long staffId, String notes, boolean isEmergency);

    /**
     * Maintenance hoàn thành → phòng chuyển AVAILABLE (UC13.5).
     *
     * @param taskId ID nhiệm vụ bảo trì
     * @return Room phòng đã sẵn sàng
     */
    Room completeMaintenance(Long taskId);

    void createMaintenanceTaskForPricedDamages(Room room);
}