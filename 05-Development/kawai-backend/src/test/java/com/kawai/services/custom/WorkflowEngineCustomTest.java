package com.kawai.services.custom;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kawai.models.*;
import com.kawai.repositories.*;
import com.kawai.services.impl.AuthServiceImpl;
import com.kawai.services.impl.WorkflowEngineServiceImpl;
import com.kawai.services.interfaces.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WorkflowEngineCustomTest {

    @Mock
    private WorkflowRepository workflowRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private HotelOperationRepository hotelOperationRepository;
    @Mock
    private PromotionRepository promotionRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private AuditLogRepository auditLogRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EmailService emailService;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    private WorkflowEngineServiceImpl workflowEngineService;
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        workflowEngineService = new WorkflowEngineServiceImpl(
                workflowRepository,
                roomRepository,
                employeeRepository,
                hotelOperationRepository,
                promotionRepository,
                bookingRepository,
                objectMapper,
                eventPublisher,
                emailService,
                messagingTemplate);

        authService = new AuthServiceImpl();
        ReflectionTestUtils.setField(authService, "accountRepository", accountRepository);
        ReflectionTestUtils.setField(authService, "customerRepository", customerRepository);
        ReflectionTestUtils.setField(authService, "roleRepository", roleRepository);
        ReflectionTestUtils.setField(authService, "auditLogRepository", auditLogRepository);
        ReflectionTestUtils.setField(authService, "passwordEncoder", passwordEncoder);
        ReflectionTestUtils.setField(authService, "workflowRepository", workflowRepository);
        ReflectionTestUtils.setField(authService, "eventPublisher", eventPublisher);
    }

    @Test
    void testTriggerEvent_RoomCheckout_Success() throws Exception {
        Workflow wf = new Workflow();
        wf.setId(1L);
        wf.setTriggerEvent("ROOM_CHECKOUT");
        wf.setIsActive(true);
        wf.setConditionsJson("{}");
        wf.setActionsJson(
                "[{\"type\":\"UPDATE_ROOM_STATUS\",\"value\":\"Vacant_Dirty\"},{\"type\":\"CREATE_OPERATION_TASK\",\"value\":\"Housekeeping\"}]");

        Room room = new Room();
        room.setId(10L);
        room.setRoomNumber("101");
        room.setRoomStatus("Occupied");

        when(workflowRepository.findByTriggerEventAndIsActive("ROOM_CHECKOUT", true))
                .thenReturn(Collections.singletonList(wf));
        when(roomRepository.findById(10L)).thenReturn(Optional.of(room));

        workflowEngineService.triggerEvent("ROOM_CHECKOUT", Map.of("room_id", 10L));

        assertEquals("Vacant_Dirty", room.getRoomStatus());
        verify(roomRepository, atLeastOnce()).save(room);
        verify(hotelOperationRepository, atLeastOnce()).save(any(HotelOperation.class));
    }

    @Test
    void testScanSlaEscalations_Success() throws Exception {
        Workflow wf = new Workflow();
        wf.setId(2L);
        wf.setTriggerEvent("SLA_ESCALATE");
        wf.setIsActive(true);
        wf.setConditionsJson("{\"max_pending_minutes\":15}");

        Employee supervisor = new Employee();
        supervisor.setId(1L);
        supervisor.setFullName("Supervisor Bob");
        supervisor.setEmail("bob@resort.com");

        HotelOperation op = new HotelOperation();
        op.setId(5L);
        op.setOperationalType("Maintenance");
        op.setStatus("Pending");
        op.setCreatedAt(LocalDateTime.now().minusMinutes(20));
        op.setSupervisor(supervisor);

        when(workflowRepository.findByTriggerEventAndIsActive("SLA_ESCALATE", true))
                .thenReturn(Collections.singletonList(wf));
        when(hotelOperationRepository.findByStatus("Pending"))
                .thenReturn(Collections.singletonList(op));

        workflowEngineService.scanSlaEscalations();

        // Email giờ được gửi qua SystemEmailEvent thay vì gọi trực tiếp emailService
        verify(eventPublisher, times(1)).publishEvent(any());
    }

    @Test
    void testAuthLockout_DynamicSettings() throws Exception {
        Workflow wf = new Workflow();
        wf.setId(3L);
        wf.setTriggerEvent("ACCOUNT_SECURITY");
        wf.setIsActive(true);
        wf.setConditionsJson("{\"failed_login_attempts\":3,\"lockout_time_minutes\":10}");

        Account account = new Account();
        account.setUsername("lockeduser");
        account.setPasswordHash("hash");
        account.setFailedLoginAttempts(2);

        when(workflowRepository.findByTriggerEventAndIsActive("ACCOUNT_SECURITY", true))
                .thenReturn(Collections.singletonList(wf));
        when(accountRepository.findByUsername("lockeduser")).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);

        boolean result = authService.login("lockeduser", "wrong");

        assertFalse(result);
        assertEquals(3, account.getFailedLoginAttempts());
        assertNotNull(account.getLockoutTime());
    }
}
