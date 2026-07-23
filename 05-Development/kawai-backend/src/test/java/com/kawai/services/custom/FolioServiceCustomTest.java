package com.kawai.services.custom;

import com.kawai.models.*;
import com.kawai.repositories.*;
import com.kawai.services.impl.FolioServiceImpl;
import com.kawai.services.interfaces.WorkflowEngineService;
import com.kawai.services.interfaces.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FolioServiceCustomTest {

    @Mock
    private FolioItemRepository folioItemRepository;
    @Mock
    private RoomBookingDetailRepository roomBookingDetailRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private ConsolidatedInvoiceRepository consolidatedInvoiceRepository;
    @Mock
    private AuditLogRepository auditLogRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private WorkflowEngineService workflowEngineService;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private FolioServiceImpl folioService;

    @Test
    void testGetFolioBalance() {
        RoomBookingDetail detail = new RoomBookingDetail();
        detail.setId(1L);

        FolioItem item1 = new FolioItem();
        item1.setRoomBookingDetail(detail);
        item1.setSourceDepartment("POS");
        item1.setAmount(new BigDecimal("100000"));

        FolioItem item2 = new FolioItem();
        item2.setRoomBookingDetail(detail);
        item2.setSourceDepartment("Rooms");
        item2.setAmount(new BigDecimal("200000"));

        FolioItem payment = new FolioItem();
        payment.setRoomBookingDetail(detail);
        payment.setSourceDepartment("PAYMENT");
        payment.setAmount(new BigDecimal("150000"));

        when(folioItemRepository.findAll()).thenReturn(Arrays.asList(item1, item2, payment));

        BigDecimal balance = folioService.getFolioBalance(1L);

        // 100k + 200k - 150k = 150k
        assertEquals(new BigDecimal("150000"), balance);
    }

    @Test
    void testPerformNightAudit() {
        RoomBooking roomBooking = new RoomBooking();
        roomBooking.setCustomer(new Customer());

        RoomBookingDetail detail = new RoomBookingDetail();
        detail.setDetailStatus("CHECKED_IN");
        detail.setRoomCharge(new BigDecimal("500000"));
        detail.setRoomBooking(roomBooking);

        when(roomBookingDetailRepository.findAll()).thenReturn(Collections.singletonList(detail));
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        folioService.performNightAudit(1L);

        verify(folioItemRepository, times(1)).save(any(FolioItem.class));
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    void testCheckOutAndSettle_NonZeroBalance_Blocked() {
        RoomBookingDetail detail = new RoomBookingDetail();
        detail.setId(1L);

        FolioItem item1 = new FolioItem();
        item1.setRoomBookingDetail(detail);
        item1.setSourceDepartment("POS");
        item1.setAmount(new BigDecimal("100000"));

        when(roomBookingDetailRepository.findById(1L)).thenReturn(Optional.of(detail));
        when(folioItemRepository.findAll()).thenReturn(Collections.singletonList(item1));

        assertThrows(IllegalStateException.class, () -> {
            folioService.checkOutAndSettle(1L, "Cash");
        });
    }

    @Test
    void testCheckOutAndSettle_ZeroBalance_Success() {
        RoomBooking roomBooking = new RoomBooking();
        roomBooking.setId(10L);
        roomBooking.setCustomer(new Customer());

        RoomBookingDetail detail = new RoomBookingDetail();
        detail.setId(1L);
        detail.setRoomBooking(roomBooking);

        Room room = new Room();
        room.setId(1L);
        room.setRoomStatus("Occupied");
        detail.setRoom(room);

        when(roomBookingDetailRepository.findById(1L)).thenReturn(Optional.of(detail));
        // No items in folio -> balance = 0
        when(folioItemRepository.findAll()).thenReturn(Collections.emptyList());
        // Simulate workflow triggering room status change to Vacant_Dirty
        doAnswer(invocation -> {
            room.setRoomStatus("Vacant_Dirty");
            return null;
        }).when(workflowEngineService).triggerEvent(eq("ROOM_CHECKOUT"), anyMap());
        when(roomRepository.save(any(Room.class))).thenAnswer(i -> i.getArgument(0));
        when(roomBookingDetailRepository.save(any(RoomBookingDetail.class))).thenAnswer(i -> i.getArgument(0));
        when(consolidatedInvoiceRepository.save(any(ConsolidatedInvoice.class))).thenAnswer(i -> i.getArgument(0));
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(i -> i.getArgument(0));

        folioService.checkOutAndSettle(1L, "Cash");

        assertEquals("CHECKED_OUT", detail.getDetailStatus());
        assertEquals("Vacant_Dirty", room.getRoomStatus(),
            "Room phải chuyển sang Vacant_Dirty (không phải Dirty) sau khi checkout");
        assertNull(room.getCurrentBookingDetailId());
        verify(consolidatedInvoiceRepository, times(1)).save(any(ConsolidatedInvoice.class));
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }
}
