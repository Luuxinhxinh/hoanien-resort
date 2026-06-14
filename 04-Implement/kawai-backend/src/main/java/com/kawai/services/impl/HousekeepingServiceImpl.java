package com.kawai.services.impl;

import com.kawai.models.Employee;
import com.kawai.models.HotelOperation;
import com.kawai.models.Room;
import com.kawai.repositories.EmployeeRepository;
import com.kawai.repositories.HousekeepingTaskRepository;
import com.kawai.repositories.MaintenanceRequestRepository;
import com.kawai.repositories.RoomRepository;
import com.kawai.services.interfaces.HousekeepingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * HousekeepingServiceImpl — UC13: Quản lý sơ đồ phòng vật lý (Room Matrix).
 * <p>
 * Business Rules:
 * BR-FO-04: Luân chuyển trạng thái phòng (Vacant_Clean → Occupied → Dirty →
 * Vacant_Clean)
 * BR-HK-02: Báo cáo hư hỏng tài sản khi tạo maintenance (notes)
 * BR-HK-03: Phòng MAINTENANCE bị khóa khỏi luồng đặt phòng
 * BR-HK-04: Phân công dọn phòng theo tầng/khu vực (qua staff assignment)
 */
@Service
public class HousekeepingServiceImpl implements HousekeepingService {

    private static final String OPERATION_CLEAN = "CHECKOUT_CLEAN";
    private static final String OPERATION_MAINTENANCE = "MAINTENANCE";
    private static final String STATUS_PENDING = "Pending";
    private static final String STATUS_COMPLETED = "Completed";
    private static final String STATUS_IN_PROGRESS = "InProgress";
    private static final String PRIORITY_HIGH = "High";
    private static final String STATUS_DIRTY = "Dirty";
    private static final String STATUS_VACANT_DIRTY = "Vacant_Dirty";
    private static final String STATUS_VACANT_CLEAN = "Vacant_Clean";
    private static final String STATUS_MAINTENANCE = "Maintenance";

    private final HousekeepingTaskRepository housekeepingTaskRepo;
    private final MaintenanceRequestRepository maintenanceRequestRepo;
    private final RoomRepository roomRepo;
    private final EmployeeRepository employeeRepo;

    @Autowired
    public HousekeepingServiceImpl(HousekeepingTaskRepository housekeepingTaskRepo,
            MaintenanceRequestRepository maintenanceRequestRepo,
            RoomRepository roomRepo,
            EmployeeRepository employeeRepo) {
        this.housekeepingTaskRepo = housekeepingTaskRepo;
        this.maintenanceRequestRepo = maintenanceRequestRepo;
        this.roomRepo = roomRepo;
        this.employeeRepo = employeeRepo;
    }

    // ========================================================================
    // UC13.1: Check-out → tự động sinh yêu cầu dọn phòng
    // ========================================================================

    /**
     * Tự động tạo yêu cầu dọn phòng sau khi khách Check-out.
     * Cập nhật trạng thái phòng thành Vacant_Dirty theo BR-FO-04.
     *
     * @param roomId  ID của phòng vừa check-out
     * @param staffId ID của nhân viên dọn phòng (hoặc supervisor)
     * @return Yêu cầu dọn phòng đã được lưu
     */
    @Override
    @Transactional
    public HotelOperation autoCreateHousekeepingTask(Long roomId, Long staffId) {
        Room room = findRoomById(roomId);
        Employee staff = findEmployeeById(staffId);

        // BR-FO-04: Cập nhật phòng thành dơ sau khi checkout
        room.setRoomStatus(STATUS_VACANT_DIRTY);
        roomRepo.save(room);

        HotelOperation task = buildHotelOperation(room, staff, OPERATION_CLEAN, PRIORITY_HIGH, null);
        return housekeepingTaskRepo.save(task);
    }

    // ========================================================================
    // UC13.2: Housekeeping cập nhật phòng DIRTY → CLEAN
    // ========================================================================

    /**
     * Housekeeping đánh dấu phòng đã được dọn sạch.
     * Cập nhật phòng thành Vacant_Clean và Task thành Completed (BR-FO-04).
     *
     * @param taskId ID của yêu cầu dọn phòng
     * @return Phòng đã cập nhật trạng thái sạch
     */
    @Override
    @Transactional
    public Room updateRoomToClean(Long taskId) {
        HotelOperation task = findTaskById(taskId);
        Room room = task.getRoom();

        // BR-FO-04: Luân chuyển trạng thái phòng sang Vacant_Clean
        room.setRoomStatus(STATUS_VACANT_CLEAN);
        task.setStatus(STATUS_COMPLETED);
        task.setCompletedAt(LocalDateTime.now());

        roomRepo.save(room);
        housekeepingTaskRepo.save(task);

        return room;
    }

    // ========================================================================
    // UC13.3: Lễ tân xem danh sách yêu cầu dọn/sửa phòng
    // ========================================================================

    /**
     * Lấy danh sách các yêu cầu dọn phòng/bảo trì đang chờ xử lý (Pending).
     *
     * @return Danh sách HotelOperation
     */
    @Override
    public List<HotelOperation> getPendingOperations() {
        return housekeepingTaskRepo.findByStatus(STATUS_PENDING);
    }

    // ========================================================================
    // UC13.4: Housekeeping tạo phiếu sửa chữa → phòng MAINTENANCE
    // ========================================================================

    /**
     * Housekeeping tạo phiếu yêu cầu bảo trì khi phát hiện thiết bị hỏng.
     * Phòng bị chuyển sang MAINTENANCE và khóa khỏi luồng đặt phòng.
     *
     * @param roomId  ID phòng gặp sự cố
     * @param staffId ID nhân viên phát hiện sự cố
     * @param notes   Ghi chú chi tiết về sự cố
     * @return Phiếu yêu cầu bảo trì đã được lưu
     */
    @Override
    @Transactional
    public HotelOperation createMaintenanceRequest(Long roomId, Long staffId, String notes) {
        Room room = findRoomById(roomId);
        Employee staff = findEmployeeById(staffId);

        // Cập nhật trạng thái phòng → Maintenance (BR-HK-03)
        room.setRoomStatus(STATUS_MAINTENANCE);
        roomRepo.save(room);

        // Tạo phiếu bảo trì (BR-HK-02)
        HotelOperation task = buildHotelOperation(room, staff, OPERATION_MAINTENANCE, "Normal", notes);
        return maintenanceRequestRepo.save(task);
    }

    // ========================================================================
    // UC13.5: Maintenance hoàn thành → phòng AVAILABLE
    // ========================================================================

    /**
     * Maintenance hoàn thành sửa chữa, đưa phòng về trạng thái có sẵn.
     *
     * @param taskId ID của phiếu bảo trì
     * @return Phòng đã cập nhật trạng thái sạch
     */
    @Override
    @Transactional
    public Room completeMaintenance(Long taskId) {
        HotelOperation task = findMaintenanceTaskById(taskId);
        Room room = task.getRoom();

        // Phòng sau bảo trì → Vacant_Clean (BR-FO-04)
        room.setRoomStatus(STATUS_VACANT_CLEAN);
        task.setStatus(STATUS_COMPLETED);
        task.setCompletedAt(LocalDateTime.now());

        roomRepo.save(room);
        maintenanceRequestRepo.save(task);

        return room;
    }

    // ========================================================================
    // Private helpers
    // ========================================================================

    private HotelOperation buildHotelOperation(Room room, Employee staff, String type, String priority, String notes) {
        HotelOperation task = new HotelOperation();
        task.setRoom(room);
        task.setStaff(staff);
        task.setSupervisor(staff);
        task.setOperationalType(type);
        task.setPriority(priority);
        task.setStatus(STATUS_PENDING);
        task.setCreatedAt(LocalDateTime.now());
        task.setNotes(notes);
        return task;
    }

    private Room findRoomById(Long id) {
        return roomRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Phòng không tìm thấy với ID: " + id));
    }

    private Employee findEmployeeById(Long id) {
        return employeeRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nhân viên không tìm thấy với ID: " + id));
    }

    private HotelOperation findTaskById(Long id) {
        return housekeepingTaskRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task không tìm thấy với ID: " + id));
    }

    private HotelOperation findMaintenanceTaskById(Long id) {
        return maintenanceRequestRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task bảo trì không tìm thấy với ID: " + id));
    }
}