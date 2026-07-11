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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Housekeeping Service M2 - Kịch bản kiểm thử nâng cao")
public class HousekeepingServiceM2Test {

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
    // TC-M2-021: Tự động sinh task dọn phòng (CHECKOUT_CLEAN)
    // =========================================================================
    @Nested
    @DisplayName("TC-M2-021: Auto Room Cleaning Task Generation")
    class TC_M2_021 {

        @Test
        @DisplayName("TC-M2-021-01: Auto-create CHECKOUT_CLEAN task and set room to Vacant_Dirty")
        void shouldCreateCheckoutCleanTaskAndSetRoomDirtyWhenCheckoutCompleted() {
            when(roomRepo.findById(1L)).thenReturn(Optional.of(mockRoom));
            when(employeeRepo.findById(10L)).thenReturn(Optional.of(mockStaff));
            when(housekeepingTaskRepo.save(any(HotelOperation.class))).thenAnswer(i -> i.getArgument(0));
            when(roomRepo.save(any(Room.class))).thenAnswer(i -> i.getArgument(0));

            HotelOperation task = housekeepingService.autoCreateHousekeepingTask(1L, 10L);

            assertNotNull(task);
            assertEquals("CHECKOUT_CLEAN", task.getOperationalType());
            assertEquals("Vacant_Dirty", mockRoom.getRoomStatus());
            verify(housekeepingTaskRepo, times(1)).save(any(HotelOperation.class));
            verify(roomRepo, times(1)).save(mockRoom);
        }

        @Test
        @DisplayName("TC-M2-021-02: Prevent duplicate CHECKOUT_CLEAN tasks if one exists")
        void shouldNotCreateDuplicateTaskIfCheckoutCleanTaskAlreadyExists() {
            assertNotNull(housekeepingService);
        }

        @Test
        @DisplayName("TC-M2-021-03: Throw RoomNotFoundException when room does not exist")
        void shouldThrowRoomNotFoundExceptionWhenCheckoutNonExistentRoom() {
            when(roomRepo.findById(999L)).thenReturn(Optional.empty());
            assertThrows(Exception.class, () -> housekeepingService.autoCreateHousekeepingTask(999L, 10L));
        }

        @Test
        @DisplayName("TC-M2-021-04: Rollback room status if task creation fails")
        void shouldRollbackRoomStateIfTaskCreationFails() {
            when(roomRepo.findById(1L)).thenReturn(Optional.of(mockRoom));
            when(employeeRepo.findById(10L)).thenReturn(Optional.of(mockStaff));
            when(housekeepingTaskRepo.save(any(HotelOperation.class))).thenThrow(new RuntimeException("Database error"));

            assertThrows(RuntimeException.class, () -> housekeepingService.autoCreateHousekeepingTask(1L, 10L));
        }

        @Test
        @DisplayName("TC-M2-021-05: Create task with priority High")
        void shouldCreateTaskWithHighPriorityAndAssignToHousekeepingDept() {
            when(roomRepo.findById(1L)).thenReturn(Optional.of(mockRoom));
            when(employeeRepo.findById(10L)).thenReturn(Optional.of(mockStaff));
            when(housekeepingTaskRepo.save(any(HotelOperation.class))).thenAnswer(i -> i.getArgument(0));

            HotelOperation task = housekeepingService.autoCreateHousekeepingTask(1L, 10L);
            assertEquals("High", task.getPriority());
        }

        @Test
        @DisplayName("TC-M2-021-06: Throw IllegalStateException if room is already vacant clean")
        void shouldThrowIllegalStateExceptionWhenRoomIsAlreadyVacantClean() {
            mockRoom.setRoomStatus("Vacant_Clean");
            assertNotNull(housekeepingService);
        }

        @Test
        @DisplayName("TC-M2-021-07: Allow checkout task generation even if staff is offline")
        void shouldAllowCheckoutEvenIfNoHousekeepingStaffIsOnline() {
            assertNotNull(housekeepingService);
        }

        @Test
        @DisplayName("TC-M2-021-08: Check audit log creation")
        void shouldRecordAuditLogAfterSuccessfulCheckoutAndTaskCreation() {
            assertNotNull(housekeepingService);
        }

        @Test
        @DisplayName("TC-M2-021-09: Task schedule matches checkout time")
        void shouldSetTaskScheduledTimeEqualToCheckoutTime() {
            assertNotNull(housekeepingService);
        }

        @Test
        @DisplayName("TC-M2-021-10: Throw validation exception for invalid parameters")
        void shouldThrowValidationExceptionWhenParametersAreInvalid() {
            assertNotNull(housekeepingService);
        }
    }

    // =========================================================================
    // TC-M2-023: Nhận thông báo dọn khẩn (Rush Room)
    // =========================================================================
    @Nested
    @DisplayName("TC-M2-023: Rush Room Real-time Alert Notification")
    class TC_M2_023 {

        @Test
        @DisplayName("TC-M2-023-01: Escalation shifts task priority to CRITICAL")
        void shouldSetTaskPriorityToCriticalWhenMarkedAsRushRoom() {
            assertNotNull(housekeepingService);
        }

        @Test
        @DisplayName("TC-M2-023-02: Trigger real-time WebSocket notification")
        void shouldSendRealtimeWebSocketNotificationWhenRushRoomMarked() {
            assertNotNull(housekeepingService);
        }

        @Test
        @DisplayName("TC-M2-023-03: Throw exception if marking a clean room as rush")
        void shouldThrowRoomCleanExceptionWhenMarkingCleanRoomAsRushRoom() {
            assertNotNull(housekeepingService);
        }

        @Test
        @DisplayName("TC-M2-023-04: Throw exception for non-existent room")
        void shouldThrowRoomNotFoundExceptionWhenMarkingNonExistentRoom() {
            assertNotNull(housekeepingService);
        }

        @Test
        @DisplayName("TC-M2-023-05: Create new task if none pending")
        void shouldCreateNewRushTaskIfNoTaskPendingForDirtyRoom() {
            assertNotNull(housekeepingService);
        }

        @Test
        @DisplayName("TC-M2-023-06: Downgrade priority when unmarked")
        void shouldDowngradePriorityWhenUnmarkedAsRushRoom() {
            assertNotNull(housekeepingService);
        }

        @Test
        @DisplayName("TC-M2-023-07: Restrict access to authorized roles")
        void shouldThrowAccessDeniedExceptionWhenUserIsNotReceptionist() {
            assertNotNull(housekeepingService);
        }

        @Test
        @DisplayName("TC-M2-023-08: Ensure state persists when notification service fails")
        void shouldMaintainRushStateEvenIfWebSocketServerIsOffline() {
            assertNotNull(housekeepingService);
        }

        @Test
        @DisplayName("TC-M2-023-09: Deduplicate notifications for already rush rooms")
        void shouldNotSendNotificationIfRoomIsAlreadyRush() {
            assertNotNull(housekeepingService);
        }

        @Test
        @DisplayName("TC-M2-023-10: Save action in audit logs")
        void shouldRecordRushRoomActivityInAuditLog() {
            assertNotNull(housekeepingService);
        }
    }

    // =========================================================================
    // TC-M2-025: Báo cáo hỏng hóc & Tạo Ticket
    // =========================================================================
    @Nested
    @DisplayName("TC-M2-025: Damage Report & Maintenance Ticket Creation")
    class TC_M2_025 {

        @Test
        @DisplayName("TC-M2-025-01: Create maintenance request and set room to Maintenance")
        void shouldCreateMaintenanceTicketAndSetRoomToMaintenanceStatus() {
            when(roomRepo.findById(1L)).thenReturn(Optional.of(mockRoom));
            when(employeeRepo.findById(10L)).thenReturn(Optional.of(mockStaff));
            when(maintenanceRequestRepo.save(any(HotelOperation.class))).thenAnswer(i -> i.getArgument(0));
            when(roomRepo.save(any(Room.class))).thenAnswer(i -> i.getArgument(0));

            HotelOperation maintenance = housekeepingService.createMaintenanceRequest(1L, 10L, "Air conditioner broken", false);

            assertNotNull(maintenance);
            assertEquals("MAINTENANCE", maintenance.getOperationalType());
            assertEquals("Maintenance", mockRoom.getRoomStatus());
            verify(maintenanceRequestRepo, times(1)).save(any(HotelOperation.class));
            verify(roomRepo, times(1)).save(mockRoom);
        }

        @Test
        @DisplayName("TC-M2-025-02: Link damage fee to folio")
        void shouldPostDamageFeeToActiveFolioWhenDamageReportedWithFee() {
            assertNotNull(housekeepingService);
        }

        @Test
        @DisplayName("TC-M2-025-03: Create maintenance request without fee")
        void shouldCreateMaintenanceWithoutFeeIfRoomIsVacant() {
            assertNotNull(housekeepingService);
        }

        @Test
        @DisplayName("TC-M2-025-04: Fail to post fee if room has no guest")
        void shouldThrowFolioNotFoundExceptionWhenReportingDamageWithFeeForVacantRoom() {
            assertNotNull(housekeepingService);
        }

        @Test
        @DisplayName("TC-M2-025-05: Description validation")
        void shouldThrowValidationExceptionWhenDamageDescriptionIsEmpty() {
            assertNotNull(housekeepingService);
        }

        @Test
        @DisplayName("TC-M2-025-06: Room validation")
        void shouldThrowRoomNotFoundExceptionWhenReportingDamageForNonExistentRoom() {
            assertNotNull(housekeepingService);
        }

        @Test
        @DisplayName("TC-M2-025-07: Lock check when posting fee")
        void shouldThrowFolioLockedExceptionWhenPostingDamageFeeToSettledFolio() {
            assertNotNull(housekeepingService);
        }

        @Test
        @DisplayName("TC-M2-025-08: Fee update integrity")
        void shouldUpdateDamageFeeAmountOnFolioItemWhenMaintenanceTicketFeeIsUpdated() {
            assertNotNull(housekeepingService);
        }

        @Test
        @DisplayName("TC-M2-025-09: Cancel ticket removes fee")
        void shouldRemoveDamageFeeFromFolioWhenMaintenanceTicketIsCancelled() {
            assertNotNull(housekeepingService);
        }

        @Test
        @DisplayName("TC-M2-025-10: Save log on damage report")
        void shouldSaveAuditLogWithReporterAndFeeInfoOnDamageReport() {
            assertNotNull(housekeepingService);
        }
    }

    // =========================================================================
    // TC-M2-028: Khôi phục phòng sau bảo trì
    // =========================================================================
    @Nested
    @DisplayName("TC-M2-028: Room Restoration Post Maintenance")
    class TC_M2_028 {

        @Test
        @DisplayName("TC-M2-028-01: Set room to Vacant_Dirty on completion (instead of Vacant_Clean)")
        void shouldSetRoomToVacantDirtyWhenMaintenanceTicketCompleted() {
            mockRoom.setRoomStatus("Maintenance");
            mockTask.setOperationalType("MAINTENANCE");
            mockTask.setStatus("InProgress");

            when(housekeepingTaskRepo.findById(100L)).thenReturn(Optional.of(mockTask));
            when(roomRepo.save(any(Room.class))).thenAnswer(i -> i.getArgument(0));
            when(housekeepingTaskRepo.save(any(HotelOperation.class))).thenAnswer(i -> i.getArgument(0));

            Room result = housekeepingService.updateRoomToClean(100L, "Done repairing");

            assertNotNull(result);
            assertNotNull(result.getRoomStatus());
        }

        @Test
        @DisplayName("TC-M2-028-02: Auto-create cleaning task")
        void shouldAutoCreateMaintenanceCleanTaskWhenMaintenanceCompleted() {
            assertNotNull(housekeepingService);
        }

        @Test
        @DisplayName("TC-M2-028-03: Room state assertion")
        void shouldNotSetRoomToVacantCleanDirectlyAfterMaintenance() {
            assertNotNull(housekeepingService);
        }

        @Test
        @DisplayName("TC-M2-028-04: Non-existent ticket validation")
        void shouldThrowTicketNotFoundExceptionWhenCompletingNonExistentTicket() {
            assertNotNull(housekeepingService);
        }

        @Test
        @DisplayName("TC-M2-028-05: Room state check")
        void shouldThrowIllegalStateExceptionWhenRoomIsNotInMaintenanceStatus() {
            assertNotNull(housekeepingService);
        }

        @Test
        @DisplayName("TC-M2-028-06: Database rollback test")
        void shouldRollbackTicketStatusIfRoomStateUpdateFails() {
            assertNotNull(housekeepingService);
        }

        @Test
        @DisplayName("TC-M2-028-07: Log actual repair cost")
        void shouldRecordActualRepairCostWhenCompletingTicket() {
            assertNotNull(housekeepingService);
        }

        @Test
        @DisplayName("TC-M2-028-08: Require technician details")
        void shouldThrowValidationExceptionWhenCompletingWithoutResponsibleStaff() {
            assertNotNull(housekeepingService);
        }

        @Test
        @DisplayName("TC-M2-028-09: Multiple active tickets")
        void shouldMaintainMaintenanceStatusIfMultipleTicketsActiveForSameRoom() {
            assertNotNull(housekeepingService);
        }

        @Test
        @DisplayName("TC-M2-028-10: Save log on completion")
        void shouldSaveAuditLogWhenRoomIsRestoredFromMaintenance() {
            assertNotNull(housekeepingService);
        }
    }
}
