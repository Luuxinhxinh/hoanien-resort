package com.kawai.services.impl;

import com.kawai.models.Employee;
import com.kawai.models.HotelOperation;
import com.kawai.models.Room;
import com.kawai.repositories.EmployeeRepository;
import com.kawai.repositories.HousekeepingTaskRepository;
import com.kawai.repositories.MaintenanceRequestRepository;
import com.kawai.repositories.RoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.kawai.services.interfaces.HousekeepingService;
import com.kawai.services.interfaces.WorkflowEngineService;
import com.kawai.repositories.WorkflowRepository;
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

    private static final Logger log = LoggerFactory.getLogger(HousekeepingServiceImpl.class);

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

    private final WorkflowEngineService workflowEngineService;
    private final WorkflowRepository workflowRepository;
    private final HousekeepingTaskRepository housekeepingTaskRepo;
    private final MaintenanceRequestRepository maintenanceRequestRepo;
    private final RoomRepository roomRepo;
    private final EmployeeRepository employeeRepo;

    @Autowired
    private org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    @Autowired
    public HousekeepingServiceImpl(HousekeepingTaskRepository housekeepingTaskRepo,
            MaintenanceRequestRepository maintenanceRequestRepo,
            RoomRepository roomRepo,
            EmployeeRepository employeeRepo,
            WorkflowEngineService workflowEngineService,
            WorkflowRepository workflowRepository) {
        this.housekeepingTaskRepo = housekeepingTaskRepo;
        this.maintenanceRequestRepo = maintenanceRequestRepo;
        this.roomRepo = roomRepo;
        this.employeeRepo = employeeRepo;
        this.workflowEngineService = workflowEngineService;
        this.workflowRepository = workflowRepository;
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
    @Override
    @Transactional
    public Room updateRoomToClean(Long taskId, String notes) {
        HotelOperation task = findTaskById(taskId);
        Room room = task.getRoom();
        String currentStatus = room.getRoomStatus();
        boolean isOccupied = currentStatus != null && currentStatus.toLowerCase().contains("occupied");

        if (isOccupied) {
            room.setRoomStatus("Occupied");
        } else {
            room.setRoomStatus(STATUS_VACANT_CLEAN);
        }
        task.setStatus(STATUS_COMPLETED);
        task.setCompletedAt(LocalDateTime.now());
        if (notes != null && !notes.isEmpty()) {
            String existingNotes = task.getNotes() != null ? task.getNotes() + "\n" : "";
            task.setNotes(existingNotes + "[Ghi chú hoàn thành]: " + notes);
        }

        roomRepo.save(room);
        housekeepingTaskRepo.save(task);

        // Gửi thông báo WebSocket cho RoomMatrix (Receptionist) để auto-reload
        try {
            String wsMessage = isOccupied
                    ? "Phòng " + room.getRoomNumber() + " (đang có khách) đã được dọn dẹp sạch sẽ!"
                    : "Phòng " + room.getRoomNumber() + " đã được dọn dẹp sạch sẽ và sẵn sàng đón khách!";

            messagingTemplate.convertAndSend("/topic/operations", java.util.Map.of(
                    "message", wsMessage,
                    "type", "ROOM_CLEANED"));
        } catch (Exception e) {
            log.error("Failed to send WebSocket message", e);
        }

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
        return housekeepingTaskRepo.findPendingTasksSorted(STATUS_PENDING);
    }

    @Override
    @Transactional
    public void escalateTaskByRoomNumber(String roomNumber) {
        List<HotelOperation> tasks = housekeepingTaskRepo.findByRoomNumberAndStatusAndType(roomNumber, STATUS_PENDING,
                OPERATION_CLEAN);
        if (!tasks.isEmpty()) {
            HotelOperation task = tasks.get(0);
            task.setPriority("Lễ tân báo dọn khẩn");
            task.setOperationalType("URGENT_CLEAN");
            String currentNotes = task.getNotes() != null ? task.getNotes() : "";
            if (!currentNotes.contains("[Khẩn cấp]")) {
                task.setNotes(currentNotes + " \n[Khẩn cấp] Lễ tân hối thúc dọn ưu tiên để khách Check-in!");
            }
            housekeepingTaskRepo.save(task);
        } else {
            // Nếu chưa có phiếu dọn phòng (có thể do lỗi dữ liệu test chưa tự động sinh
            // ra),
            // ta sẽ tự động tạo một phiếu mới tinh với mức độ Lễ tân báo dọn khẩn.
            Room room = roomRepo.findByRoomNumber(roomNumber).orElse(null);
            if (room != null) {
                List<Employee> allStaff = employeeRepo.findAll();
                if (!allStaff.isEmpty()) {
                    Employee staff = allStaff.get(0);
                    HotelOperation newTask = buildHotelOperation(room, staff, OPERATION_CLEAN, "Lễ tân báo dọn khẩn",
                            "Lễ tân yêu cầu dọn phòng khẩn cấp.");
                    housekeepingTaskRepo.save(newTask);
                }
            }
        }
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
    public HotelOperation createMaintenanceRequest(Long roomId, Long staffId, String notes, boolean isEmergency) {
        Room room = findRoomById(roomId);
        Employee staff = findEmployeeById(staffId);
        String priority = isEmergency ? "Urgent" : "Normal";

        List<com.kawai.models.Workflow> activeWorkflows = workflowRepository
                .findByTriggerEventAndIsActive("ROOM_REPORT_DAMAGE", true);
        if (!activeWorkflows.isEmpty()) {
            // Execute the automated workflows
            try {
                workflowEngineService.triggerEvent("ROOM_REPORT_DAMAGE", java.util.Map.of(
                        "room_id", roomId,
                        "staff_id", staffId,
                        "notes", notes != null ? notes : "",
                        "is_emergency", isEmergency));
            } catch (Exception e) {
                log.error("Failed executing workflow engine trigger for ROOM_REPORT_DAMAGE", e);
            }

            // Return the created task
            return maintenanceRequestRepo.findAll().stream()
                    .filter(t -> t.getRoom().getId().equals(roomId) && ("Maintenance".equals(t.getOperationalType())
                            || "MAINTENANCE".equals(t.getOperationalType())
                            || "DAMAGE_CHECK".equals(t.getOperationalType())))
                    .sorted((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt()))
                    .findFirst()
                    .orElseGet(() -> {
                        String type = (isEmergency || room.getCurrentBookingDetailId() == null) ? OPERATION_MAINTENANCE
                                : "DAMAGE_CHECK";
                        HotelOperation task = buildHotelOperation(room, staff, type, priority, notes);
                        return maintenanceRequestRepo.save(task);
                    });
        }

        String type;
        if (isEmergency || room.getCurrentBookingDetailId() == null) {
            // Cập nhật trạng thái phòng → Maintenance
            room.setRoomStatus(STATUS_MAINTENANCE);
            roomRepo.save(room);
            type = OPERATION_MAINTENANCE;
        } else {
            // Khách chưa checkout và không khẩn cấp -> Tạo phiếu DAMAGE_CHECK và giữ nguyên
            // trạng thái phòng (không đổi sang Maintenance)
            type = "DAMAGE_CHECK";
        }

        // Tạo phiếu bảo trì
        HotelOperation task = buildHotelOperation(room, staff, type, priority, notes);
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

        // Phòng sau bảo trì → Trả về Occupied nếu đang có khách ở, ngược lại kiểm tra
        // xem có task dọn dẹp nào chưa hoàn thành
        if (room.getCurrentBookingDetailId() != null) {
            room.setRoomStatus("Occupied");
        } else {
            boolean hasActiveCleaningTask = housekeepingTaskRepo.findAll().stream()
                    .anyMatch(t -> t.getRoom() != null && t.getRoom().getId().equals(room.getId())
                            && ("CHECKOUT_CLEAN".equals(t.getOperationalType())
                                    || "URGENT_CLEAN".equals(t.getOperationalType()))
                            && !"Completed".equalsIgnoreCase(t.getStatus()));
            if (hasActiveCleaningTask) {
                room.setRoomStatus(STATUS_VACANT_DIRTY);
            } else {
                room.setRoomStatus(STATUS_VACANT_CLEAN);
            }
        }
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

    @Override
    @Transactional
    public void createMaintenanceTaskForPricedDamages(Room room) {
        if (room == null)
            return;
        List<HotelOperation> damageChecks = maintenanceRequestRepo.findAll().stream()
                .filter(t -> t.getRoom() != null && t.getRoom().getId().equals(room.getId())
                        && "DAMAGE_CHECK".equals(t.getOperationalType())
                        && t.getDamagePrice() != null
                        && !"ConvertedToRepair".equals(t.getStatus()))
                .collect(java.util.stream.Collectors.toList());

        if (damageChecks.isEmpty()) {
            room.setRoomStatus("Vacant_Dirty");
            roomRepo.save(room);
        } else {
            for (HotelOperation dc : damageChecks) {
                dc.setStatus("ConvertedToRepair");
                maintenanceRequestRepo.save(dc);

                room.setRoomStatus("Maintenance");
                roomRepo.save(room);

                HotelOperation repairTask = new HotelOperation();
                repairTask.setRoom(room);
                repairTask.setStaff(dc.getStaff());
                repairTask.setSupervisor(dc.getSupervisor());
                repairTask.setOperationalType("MAINTENANCE");
                repairTask.setPriority(dc.getPriority());
                repairTask.setStatus("Pending");
                repairTask.setNotes("[Cần sửa chữa - Đền bù hỏng hóc] " + (dc.getNotes() != null ? dc.getNotes() : "")
                        + " | Chi phí đền bù: "
                        + dc.getDamagePrice() + " VNĐ");
                repairTask.setImageUrl(dc.getImageUrl());
                repairTask.setDamagePrice(dc.getDamagePrice());
                repairTask.setCreatedAt(java.time.LocalDateTime.now());
                maintenanceRequestRepo.save(repairTask);
            }
        }
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