package com.kawai.services;

import com.kawai.models.*;
import com.kawai.repositories.*;
import com.kawai.services.impl.HousekeepingServiceImpl;
import com.kawai.services.interfaces.WorkflowEngineService;
import com.kawai.repositories.WorkflowRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ================================================================
 * KAWAI RESORT — TDD Unit Test cho HousekeepingService (UC13)
 * ================================================================
 *
 * Test Case tham chiếu:
 * TC-M2-016 (UC13.1): Check-out → tự động sinh yêu cầu dọn phòng
 * TC-M2-017 (UC13.2): Housekeeping cập nhật phòng DIRTY → CLEAN
 * TC-M2-018 (UC13.3): Lễ tân xem danh sách yêu cầu dọn/sửa phòng
 * TC-M2-019 (UC13.4): Housekeeping tạo phiếu sửa chữa → phòng MAINTENANCE
 * TC-M2-020 (UC13.5): Maintenance hoàn thành → phòng AVAILABLE
 *
 * Business Rules:
 * BR-FO-04: Luân chuyển trạng thái phòng (Vacant_Clean → Occupied → Dirty)
 * BR-HK-02: Báo cáo hư hỏng tài sản khi tạo maintenance
 * BR-HK-03: Chặn Check-in phòng MAINTENANCE
 *
 * TDD Phase: 🔴 RED
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UC13 — Quản lý sơ đồ phòng vật lý (HousekeepingService)")
class HousekeepingServiceUC13Test {

    @Mock
    private HousekeepingTaskRepository housekeepingTaskRepo;
    @Mock
    private MaintenanceRequestRepository maintenanceRequestRepo;
    @Mock
    private RoomRepository roomRepo;
    @Mock
    private EmployeeRepository employeeRepo;
    @Mock
    private WorkflowEngineService workflowEngineService;
    @Mock
    private WorkflowRepository workflowRepository;

    @InjectMocks
    private HousekeepingServiceImpl housekeepingService;

    // ===== Test Data Fixtures =====
    private Room sampleRoom;
    private Room dirtyRoom;
    private Room maintenanceRoom;
    private Employee sampleStaff;
    private HotelOperation sampleTask;
    private HotelOperation maintenanceTask;

    @BeforeEach
    void setUp() {
        // room mẫu
        sampleRoom = new Room();
        sampleRoom.setId(1L);
        sampleRoom.setRoomNumber("R101");
        sampleRoom.setRoomStatus("Dirty");

        dirtyRoom = new Room();
        dirtyRoom.setId(1L);
        dirtyRoom.setRoomNumber("R101");
        dirtyRoom.setRoomStatus("Dirty");

        maintenanceRoom = new Room();
        maintenanceRoom.setId(2L);
        maintenanceRoom.setRoomNumber("R102");
        maintenanceRoom.setRoomStatus("Maintenance");

        // Employee mẫu
        sampleStaff = new Employee();
        sampleStaff.setId(10L);
        sampleStaff.setFullName("Nguyen Van A");

        // HotelOperation mẫu — Housekeeping
        sampleTask = new HotelOperation();
        sampleTask.setId(100L);
        sampleTask.setRoom(sampleRoom);
        sampleTask.setStaff(sampleStaff);
        sampleTask.setSupervisor(sampleStaff);
        sampleTask.setOperationalType("CHECKOUT_CLEAN");
        sampleTask.setPriority("High");
        sampleTask.setStatus("Pending");
        sampleTask.setCreatedAt(LocalDateTime.now());

        // HotelOperation mẫu — Maintenance
        maintenanceTask = new HotelOperation();
        maintenanceTask.setId(200L);
        maintenanceTask.setRoom(maintenanceRoom);
        maintenanceTask.setStaff(sampleStaff);
        maintenanceTask.setSupervisor(sampleStaff);
        maintenanceTask.setOperationalType("MAINTENANCE");
        maintenanceTask.setPriority("Normal");
        maintenanceTask.setStatus("InProgress");
        maintenanceTask.setCreatedAt(LocalDateTime.now());
        maintenanceTask.setNotes("Dieu hoa khong lanh");
    }

    // ================================================================
    // TC-M2-016: Check-out → tự động sinh yêu cầu dọn phòng (Trigger DB)
    // ================================================================
    @Nested
    @DisplayName("TC-M2-016: Auto-create housekeeping task after checkout")
    class TC_M2_016 {

        @Test
        @DisplayName("TC-M2-016: Check-out → tạo task CHECKOUT_CLEAN, phòng → Vacant_Dirty")
        void autoCreateHousekeepingTask_AfterCheckout_ShouldCreateTask() {
            // ARRANGE
            Long roomId = 1L;
            Long staffId = 10L;

            when(roomRepo.findById(roomId)).thenReturn(Optional.of(sampleRoom));
            when(employeeRepo.findById(staffId)).thenReturn(Optional.of(sampleStaff));
            when(housekeepingTaskRepo.save(any(HotelOperation.class)))
                    .thenAnswer(inv -> {
                        HotelOperation saved = inv.getArgument(0);
                        saved.setId(100L);
                        return saved;
                    });
            when(roomRepo.save(any(Room.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // ACT
            HotelOperation result = housekeepingService.autoCreateHousekeepingTask(roomId, staffId);

            // ASSERT
            assertNotNull(result, "Task không được null");
            assertEquals("CHECKOUT_CLEAN", result.getOperationalType(),
                    "Loại tác vụ phải là CHECKOUT_CLEAN");
            assertEquals("High", result.getPriority(),
                    "Ưu tiên phải là High (BR-FO-04)");
            assertEquals("Pending", result.getStatus(),
                    "Trạng thái task ban đầu phải là Pending");
            assertEquals(sampleRoom, result.getRoom(),
                    "Task phải gắn với phòng check-out");

            // Verify tương tác
            verify(roomRepo).findById(roomId);
            verify(employeeRepo).findById(staffId);
            verify(housekeepingTaskRepo).save(any(HotelOperation.class));
        }
    }

    // ================================================================
    // TC-M2-017: Housekeeping cập nhật phòng DIRTY → CLEAN
    // ================================================================
    @Nested
    @DisplayName("TC-M2-017: HK cập nhật phòng DIRTY → CLEAN")
    class TC_M2_017 {

        @Test
        @DisplayName("TC-M2-017: Cập nhật DIRTY → Vacant_Clean, đánh dấu task Completed")
        void updateRoomToClean_DirtyRoom_ShouldMakeVacantClean() {
            // ARRANGE
            Long taskId = 100L;

            when(housekeepingTaskRepo.findById(taskId)).thenReturn(Optional.of(sampleTask));
            when(roomRepo.save(any(Room.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(housekeepingTaskRepo.save(any(HotelOperation.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // ACT
            Room result = housekeepingService.updateRoomToClean(taskId, null);

            // ASSERT
            assertNotNull(result, "Kết quả không được null");
            assertEquals("Vacant_Clean", result.getRoomStatus(),
                    "Phòng phải chuyển thành Vacant_Clean (BR-FO-04)");
            assertEquals("Completed", sampleTask.getStatus(),
                    "Task phải chuyển sang Completed");

            // Verify tương tác
            verify(housekeepingTaskRepo).findById(taskId);
            verify(roomRepo).save(any(Room.class));
            verify(housekeepingTaskRepo).save(any(HotelOperation.class));
        }

        @Test
        @DisplayName("TC-M2-017-02: Cập nhật phòng đang có khách (Occupied) → giữ nguyên Occupied, đánh dấu task Completed")
        void updateRoomToClean_OccupiedRoom_ShouldKeepOccupied() {
            // ARRANGE
            Long taskId = 100L;
            sampleRoom.setRoomStatus("Occupied");
            sampleRoom.setCurrentBookingDetailId(555L); // Khách đã check-in

            when(housekeepingTaskRepo.findById(taskId)).thenReturn(Optional.of(sampleTask));
            when(roomRepo.save(any(Room.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(housekeepingTaskRepo.save(any(HotelOperation.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // ACT
            Room result = housekeepingService.updateRoomToClean(taskId, null);

            // ASSERT
            assertNotNull(result, "Kết quả không được null");
            assertEquals("Occupied", result.getRoomStatus(),
                    "Phòng phải giữ nguyên trạng thái Occupied vì khách đang ở");
            assertEquals("Completed", sampleTask.getStatus(),
                    "Task phải chuyển sang Completed");

            // Verify tương tác
            verify(housekeepingTaskRepo).findById(taskId);
            verify(roomRepo).save(any(Room.class));
            verify(housekeepingTaskRepo).save(any(HotelOperation.class));

            // Reset
            sampleRoom.setCurrentBookingDetailId(null);
        }
    }

    // ================================================================
    // TC-M2-018: Lễ tân xem danh sách yêu cầu dọn/sửa phòng
    // ================================================================
    @Nested
    @DisplayName("TC-M2-018: Lễ tân xem danh sách yêu cầu dọn/sửa phòng")
    class TC_M2_018 {

        @Test
        @DisplayName("TC-M2-018: Trả về danh sách các HotelOperation đang Pending")
        void getPendingOperations_ShouldReturnPendingTasks() {
            // ARRANGE
            List<HotelOperation> pendingTasks = new ArrayList<>();
            pendingTasks.add(sampleTask);
            maintenanceTask.setStatus("Pending");
            pendingTasks.add(maintenanceTask);

            when(housekeepingTaskRepo.findPendingTasksSorted("Pending"))
                    .thenReturn(pendingTasks);

            // ACT
            List<HotelOperation> result = housekeepingService.getPendingOperations();

            // ASSERT
            assertNotNull(result, "Danh sách không được null");
            assertEquals(2, result.size(), "Phải trả về 2 task pending");
            assertTrue(result.stream().allMatch(t -> "Pending".equals(t.getStatus())),
                    "Tất cả task phải có status Pending");

            // Verify
            verify(housekeepingTaskRepo).findPendingTasksSorted("Pending");
        }
    }

    // ================================================================
    // TC-M2-019: Housekeeping tạo phiếu sửa chữa → phòng MAINTENANCE
    // ================================================================
    @Nested
    @DisplayName("TC-M2-019: HK tạo phiếu sửa chữa → phòng MAINTENANCE")
    class TC_M2_019 {

        @Test
        @DisplayName("TC-M2-019: Tạo maintenance request → phòng chuyển MAINTENANCE")
        void createMaintenanceRequest_ShouldChangeRoomToMaintenance() {
            // ARRANGE
            Long roomId = 1L;
            Long staffId = 10L;
            String notes = "Dieu hoa phong R101 khong lanh";

            when(roomRepo.findById(roomId)).thenReturn(Optional.of(sampleRoom));
            when(employeeRepo.findById(staffId)).thenReturn(Optional.of(sampleStaff));
            when(maintenanceRequestRepo.save(any(HotelOperation.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(roomRepo.save(any(Room.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // ACT
            HotelOperation result = housekeepingService.createMaintenanceRequest(roomId, staffId, notes, false);

            // ASSERT
            assertNotNull(result, "Phiếu bảo trì không được null");
            assertEquals("MAINTENANCE", result.getOperationalType(),
                    "Loại tác vụ phải là MAINTENANCE");
            assertEquals("Pending", result.getStatus(),
                    "Trạng thái ban đầu phải là Pending");
            assertEquals(notes, result.getNotes(),
                    "Ghi chú sự cố phải khớp");
            assertEquals("Maintenance", sampleRoom.getRoomStatus(),
                    "Phòng phải chuyển sang MAINTENANCE (BR-HK-03)");

            // Verify
            verify(roomRepo).findById(roomId);
            verify(employeeRepo).findById(staffId);
            verify(maintenanceRequestRepo).save(any(HotelOperation.class));
            verify(roomRepo).save(any(Room.class));
        }

        @Test
        @DisplayName("TC-M2-019-02: Tạo maintenance request khi khách chưa checkout -> tạo DAMAGE_CHECK và giữ nguyên trạng thái phòng")
        void createMaintenanceRequest_WhenGuestNotCheckedOut_ShouldCreateDamageCheck() {
            // ARRANGE
            Long roomId = 1L;
            Long staffId = 10L;
            String notes = "Dieu hoa khong lanh";
            sampleRoom.setCurrentBookingDetailId(555L); // set active booking (not checked out)
            sampleRoom.setRoomStatus("Occupied");

            when(roomRepo.findById(roomId)).thenReturn(Optional.of(sampleRoom));
            when(employeeRepo.findById(staffId)).thenReturn(Optional.of(sampleStaff));
            when(maintenanceRequestRepo.save(any(HotelOperation.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // ACT
            HotelOperation result = housekeepingService.createMaintenanceRequest(roomId, staffId, notes, false);

            // ASSERT
            assertNotNull(result, "Phiếu báo hỏng không được null");
            assertEquals("DAMAGE_CHECK", result.getOperationalType(), "Loại tác vụ phải là DAMAGE_CHECK");
            assertEquals("Normal", result.getPriority());
            assertEquals("Occupied", sampleRoom.getRoomStatus(), "Phòng phải giữ nguyên trạng thái Occupied");

            // Reset for other tests
            sampleRoom.setCurrentBookingDetailId(null);
        }

        @Test
        @DisplayName("TC-M2-019-03: Tạo maintenance request KHẨN CẤP khi khách chưa checkout -> thành công")
        void createMaintenanceRequest_WhenEmergencyAndGuestNotCheckedOut_ShouldSucceed() {
            // ARRANGE
            Long roomId = 1L;
            Long staffId = 10L;
            String notes = "Rò rỉ nước khẩn cấp";
            sampleRoom.setCurrentBookingDetailId(555L); // set active booking (not checked out)

            when(roomRepo.findById(roomId)).thenReturn(Optional.of(sampleRoom));
            when(employeeRepo.findById(staffId)).thenReturn(Optional.of(sampleStaff));
            when(maintenanceRequestRepo.save(any(HotelOperation.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // ACT
            HotelOperation result = housekeepingService.createMaintenanceRequest(roomId, staffId, notes, true);

            // ASSERT
            assertNotNull(result, "Phiếu bảo trì khẩn cấp không được null");
            assertEquals("MAINTENANCE", result.getOperationalType());
            assertEquals("Urgent", result.getPriority(), "Độ ưu tiên của sự cố khẩn cấp phải là Urgent");

            // Reset for other tests
            sampleRoom.setCurrentBookingDetailId(null);
        }
    }

    // ================================================================
    // TC-M2-020: Maintenance hoàn thành → phòng chuyển AVAILABLE
    // ================================================================
    @Nested
    @DisplayName("TC-M2-020: Maintenance hoàn thành → phòng AVAILABLE")
    class TC_M2_020 {

        @Test
        @DisplayName("TC-M2-020-01: Hoàn thành maintenance → phòng Vacant_Clean, task Completed")
        void completeMaintenance_ShouldMakeRoomAvailable() {
            // ARRANGE
            Long taskId = 200L;

            when(maintenanceRequestRepo.findById(taskId)).thenReturn(Optional.of(maintenanceTask));
            when(housekeepingTaskRepo.findAll()).thenReturn(new ArrayList<>());
            when(roomRepo.save(any(Room.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(maintenanceRequestRepo.save(any(HotelOperation.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // ACT
            Room result = housekeepingService.completeMaintenance(taskId);

            // ASSERT
            assertNotNull(result, "Phòng sau bảo trì không được null");
            assertEquals("Vacant_Clean", result.getRoomStatus(),
                    "Phòng phải chuyển thành Vacant_Clean (BR-FO-04)");
            assertEquals("Completed", maintenanceTask.getStatus(),
                    "Task bảo trì phải chuyển sang Completed");

            // Verify
            verify(maintenanceRequestRepo).findById(taskId);
            verify(roomRepo).save(any(Room.class));
            verify(maintenanceRequestRepo).save(any(HotelOperation.class));
        }

        @Test
        @DisplayName("TC-M2-020-02: Hoàn thành maintenance nhưng còn task dọn dẹp chưa xong -> phòng Vacant_Dirty")
        void completeMaintenance_ShouldKeepRoomDirtyIfHousekeepingTaskIsPending() {
            // ARRANGE
            Long taskId = 200L;
            List<HotelOperation> hkTasks = new ArrayList<>();
            HotelOperation pendingHkTask = new HotelOperation();
            pendingHkTask.setRoom(maintenanceRoom);
            pendingHkTask.setOperationalType("CHECKOUT_CLEAN");
            pendingHkTask.setStatus("Pending");
            hkTasks.add(pendingHkTask);

            when(maintenanceRequestRepo.findById(taskId)).thenReturn(Optional.of(maintenanceTask));
            when(housekeepingTaskRepo.findAll()).thenReturn(hkTasks);
            when(roomRepo.save(any(Room.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(maintenanceRequestRepo.save(any(HotelOperation.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // ACT
            Room result = housekeepingService.completeMaintenance(taskId);

            // ASSERT
            assertNotNull(result, "Phòng sau bảo trì không được null");
            assertEquals("Vacant_Dirty", result.getRoomStatus(),
                    "Phòng phải giữ trạng thái Vacant_Dirty vì còn task dọn dẹp chưa hoàn thành");
            assertEquals("Completed", maintenanceTask.getStatus(),
                    "Task bảo trì phải chuyển sang Completed");

            // Verify
            verify(maintenanceRequestRepo).findById(taskId);
            verify(roomRepo).save(any(Room.class));
            verify(maintenanceRequestRepo).save(any(HotelOperation.class));
        }
    }
}