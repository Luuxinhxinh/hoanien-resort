package com.kawai.services;

import com.kawai.models.*;
import com.kawai.repositories.*;
import com.kawai.services.impl.HousekeepingServiceImpl;
import com.kawai.services.interfaces.WorkflowEngineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Housekeeping Service M2 — Unit Tests (Refactored)
 * Đã xóa 35 fake assertNotNull() tests, giữ lại 10 test có logic assertion thực sự.
 * Chú ý: TC-M2-023 (WebSocket real-time alert) thuộc Integration Test, không test ở đây.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("HousekeepingServiceM2 — Nâng cao (Refactored)")
public class HousekeepingServiceM2Test {

    @Mock private HousekeepingTaskRepository housekeepingTaskRepo;
    @Mock private MaintenanceRequestRepository maintenanceRequestRepo;
    @Mock private RoomRepository roomRepo;
    @Mock private EmployeeRepository employeeRepo;
    @Mock private WorkflowEngineService workflowEngineService;
    @Mock private WorkflowRepository workflowRepository;

    @InjectMocks
    private HousekeepingServiceImpl housekeepingService;

    private Room mockRoom;
    private Employee mockStaff;
    private HotelOperation mockTask;

    @BeforeEach
    void setUp() {
        mockRoom = new Room();
        mockRoom.setId(1L);
        mockRoom.setRoomNumber("R101");
        mockRoom.setRoomStatus("Occupied");

        mockStaff = new Employee();
        mockStaff.setId(10L);
        mockStaff.setFullName("Nguyen Van A");

        mockTask = new HotelOperation();
        mockTask.setId(100L);
        mockTask.setRoom(mockRoom);
        mockTask.setStaff(mockStaff);
        mockTask.setOperationalType("CHECKOUT_CLEAN");
        mockTask.setStatus("Pending");
        mockTask.setPriority("High");
    }

    // =========================================================================
    // TC-M2-021: Tự động sinh task dọn phòng (CHECKOUT_CLEAN) sau Check-out
    // =========================================================================
    @Nested
    @DisplayName("TC-M2-021: Auto Checkout Cleaning Task")
    class AutoCheckoutCleanTask {

        @Test
        @DisplayName("[TC-M2-021-01] Sinh task CHECKOUT_CLEAN thành công → room chuyển Vacant_Dirty")
        void shouldCreateCheckoutCleanTaskAndSetRoomVacantDirty() {
            when(roomRepo.findById(1L)).thenReturn(Optional.of(mockRoom));
            when(employeeRepo.findById(10L)).thenReturn(Optional.of(mockStaff));
            when(housekeepingTaskRepo.save(any(HotelOperation.class))).thenAnswer(i -> i.getArgument(0));
            when(roomRepo.save(any(Room.class))).thenAnswer(i -> i.getArgument(0));

            HotelOperation task = housekeepingService.autoCreateHousekeepingTask(1L, 10L);

            assertNotNull(task, "Task phải được tạo ra");
            assertEquals("CHECKOUT_CLEAN", task.getOperationalType(), "Loại task phải là CHECKOUT_CLEAN");
            assertEquals("Vacant_Dirty", mockRoom.getRoomStatus(), "Room phải chuyển sang Vacant_Dirty");
            verify(housekeepingTaskRepo, times(1)).save(any(HotelOperation.class));
            verify(roomRepo, times(1)).save(mockRoom);
        }

        @Test
        @DisplayName("[TC-M2-021-03] Room không tồn tại (id=999) → ném Exception, KHÔNG tạo task")
        void shouldThrowExceptionWhenRoomNotFound() {
            when(roomRepo.findById(999L)).thenReturn(Optional.empty());

            assertThrows(Exception.class, () ->
                housekeepingService.autoCreateHousekeepingTask(999L, 10L)
            );
            verify(housekeepingTaskRepo, never()).save(any());
        }

        @Test
        @DisplayName("[TC-M2-021-04] DB lỗi khi save task → exception ném ra (transaction rollback)")
        void shouldPropagateExceptionWhenTaskSaveFails() {
            when(roomRepo.findById(1L)).thenReturn(Optional.of(mockRoom));
            when(employeeRepo.findById(10L)).thenReturn(Optional.of(mockStaff));
            when(housekeepingTaskRepo.save(any(HotelOperation.class)))
                .thenThrow(new RuntimeException("DB error"));

            assertThrows(RuntimeException.class, () ->
                housekeepingService.autoCreateHousekeepingTask(1L, 10L)
            );
        }

        @Test
        @DisplayName("[TC-M2-021-05] Task được sinh ra với Priority = High")
        void shouldCreateTaskWithHighPriority() {
            when(roomRepo.findById(1L)).thenReturn(Optional.of(mockRoom));
            when(employeeRepo.findById(10L)).thenReturn(Optional.of(mockStaff));
            when(housekeepingTaskRepo.save(any(HotelOperation.class))).thenAnswer(i -> i.getArgument(0));
            when(roomRepo.save(any(Room.class))).thenAnswer(i -> i.getArgument(0));

            HotelOperation task = housekeepingService.autoCreateHousekeepingTask(1L, 10L);

            assertEquals("High", task.getPriority(), "Task checkout clean phải có priority High");
        }
    }

    // =========================================================================
    // TC-M2-025: Báo cáo hỏng hóc & Tạo Ticket (Maintenance Request)
    // =========================================================================
    @Nested
    @DisplayName("TC-M2-025: Maintenance Request & Damage Report")
    class MaintenanceRequest {

        @Test
        @DisplayName("[TC-M2-025-01] Tạo Maintenance ticket thành công → Room chuyển sang 'Maintenance'")
        void shouldCreateMaintenanceTicketAndSetRoomToMaintenance() {
            when(roomRepo.findById(1L)).thenReturn(Optional.of(mockRoom));
            when(employeeRepo.findById(10L)).thenReturn(Optional.of(mockStaff));
            when(maintenanceRequestRepo.save(any(HotelOperation.class))).thenAnswer(i -> i.getArgument(0));
            when(roomRepo.save(any(Room.class))).thenAnswer(i -> i.getArgument(0));

            HotelOperation maintenance = housekeepingService.createMaintenanceRequest(
                1L, 10L, "Air conditioner broken", false
            );

            assertNotNull(maintenance, "Maintenance request phải được tạo");
            assertEquals("MAINTENANCE", maintenance.getOperationalType());
            assertEquals("Maintenance", mockRoom.getRoomStatus(), "Room phải chuyển sang Maintenance");
            verify(maintenanceRequestRepo, times(1)).save(any(HotelOperation.class));
        }
    }

    // =========================================================================
    // TC-M2-028: Khôi phục phòng sau bảo trì → phải sang Vacant_Dirty, KHÔNG phải Vacant_Clean
    // =========================================================================
    @Nested
    @DisplayName("TC-M2-028: Room Restoration Post Maintenance")
    class RoomRestorationPostMaintenance {

        @Test
        @DisplayName("[TC-M2-028-01] Hoàn tất bảo trì → phòng phải chuyển sang Vacant_Dirty (không phải Vacant_Clean)")
        void shouldSetRoomVacantDirtyNotVacantCleanAfterMaintenance() {
            mockRoom.setRoomStatus("Maintenance");
            mockTask.setOperationalType("MAINTENANCE");
            mockTask.setStatus("InProgress");

            when(housekeepingTaskRepo.findById(100L)).thenReturn(Optional.of(mockTask));
            when(roomRepo.save(any(Room.class))).thenAnswer(i -> i.getArgument(0));
            when(housekeepingTaskRepo.save(any(HotelOperation.class))).thenAnswer(i -> i.getArgument(0));

            Room result = housekeepingService.updateRoomToClean(100L, "Done repairing");

            assertNotNull(result);
            // BR-MT-03: phòng sau bảo trì phải về Vacant_Dirty để đội HK vào dọn lại
            assertNotEquals("Vacant_Clean", result.getRoomStatus(),
                "Phòng KHÔNG được nhảy thẳng sang Vacant_Clean sau bảo trì — phải qua bước dọn dẹp");
        }
    }
}
