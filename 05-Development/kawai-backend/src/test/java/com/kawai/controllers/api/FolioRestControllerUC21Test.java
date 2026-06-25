package com.kawai.controllers.api;

import com.kawai.models.*;
import com.kawai.repositories.*;
import com.kawai.services.interfaces.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FolioRestControllerUC21Test {

    @Mock
    private NightAuditService nightAuditService;
    @Mock
    private FolioItemRepository folioItemRepository;
    @Mock
    private RoomBookingDetailRepository roomBookingDetailRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private ConsolidatedInvoiceRepository consolidatedInvoiceRepository;
    @Mock
    private PaymentService paymentService;
    @Mock
    private InvoicePdfService invoicePdfService;
    @Mock
    private EmailService emailService;
    @Mock
    private RoomBookingRepository roomBookingRepository;
    @Mock
    private VnPayService vnPayService;

    @InjectMocks
    private FolioRestController folioRestController;

    @Test
    @DisplayName("MOD5-TC-001 - Lấy danh sách Folio Items theo phòng thành công")
    void testGetFolioByRoom_Success() {
        // Arrange
        Long detailId = 1L;
        RoomBookingDetail mockDetail = new RoomBookingDetail();
        mockDetail.setId(detailId);
        mockDetail.setRoomCharge(new BigDecimal("1000000"));

        RoomBooking mockBooking = new RoomBooking();
        Customer mockCustomer = new Customer();
        mockCustomer.setFullName("Nguyen Van A");
        mockBooking.setCustomer(mockCustomer);
        mockDetail.setRoomBooking(mockBooking);

        Room mockRoom = new Room();
        mockRoom.setRoomNumber("101");
        mockDetail.setRoom(mockRoom);

        FolioItem mockItem = new FolioItem();
        mockItem.setId(1L);
        mockItem.setAmount(new BigDecimal("500000"));
        mockItem.setSourceDepartment("F&B");

        when(roomBookingDetailRepository.findById(detailId)).thenReturn(Optional.of(mockDetail));
        when(nightAuditService.getFolioItems(detailId)).thenReturn(List.of(mockItem));
        when(nightAuditService.calculateFolioBalance(detailId)).thenReturn(new BigDecimal("500000"));

        // Act
        ResponseEntity<?> response = folioRestController.getFolioByRoom(detailId);

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertTrue((Boolean) body.get("success"));
        assertEquals("Nguyen Van A", body.get("guestName"));
        assertEquals("101", body.get("roomNumber"));
        assertEquals(new BigDecimal("1650000.00"), body.get("currentBalance"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
        assertTrue(items.size() >= 1);
    }

    @Test
    @DisplayName("MOD5-TC-002 - Tách Folio Item riêng lẻ thành công")
    void testSplitFolioItem_Success() {
        // Arrange
        Long itemId = 1L;
        FolioItem mockItem = new FolioItem();
        mockItem.setId(itemId);
        mockItem.setIsSettledSeparately(false);

        when(folioItemRepository.findById(itemId)).thenReturn(Optional.of(mockItem));

        Map<String, Boolean> payload = new HashMap<>();
        payload.put("isSettledSeparately", true);

        // Act
        ResponseEntity<?> response = folioRestController.splitFolioItem(itemId, payload);

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertTrue((Boolean) body.get("success"));

        verify(folioItemRepository, times(1)).save(mockItem);
        assertTrue(mockItem.getIsSettledSeparately());
    }

    @Test
    @DisplayName("MOD5-TC-003 - Tất toán thành công với số tiền đủ")
    void testCheckoutFolio_Success() {
        // Arrange
        Long detailId = 1L;
        RoomBookingDetail mockDetail = new RoomBookingDetail();
        mockDetail.setId(detailId);

        Room mockRoom = new Room();
        mockRoom.setRoomNumber("101");
        mockDetail.setRoom(mockRoom);

        RoomBooking mockBooking = new RoomBooking();
        Customer mockCustomer = new Customer();
        mockCustomer.setEmail("test@gmail.com");
        mockBooking.setCustomer(mockCustomer);
        mockDetail.setRoomBooking(mockBooking);

        when(roomBookingDetailRepository.findById(detailId)).thenReturn(Optional.of(mockDetail));
        when(nightAuditService.calculateFolioBalance(detailId)).thenReturn(new BigDecimal("1000000"));
        when(invoicePdfService.generateInvoicePdf(any())).thenReturn("path/to/invoice.pdf");

        Map<String, Object> payload = new HashMap<>();
        payload.put("paymentAmount", 1100000);
        payload.put("paymentMethod", "CASH");

        // Act
        ResponseEntity<?> response = folioRestController.checkoutFolio(detailId, payload, null);

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertTrue((Boolean) body.get("success"));

        assertEquals("Checked_Out", mockDetail.getDetailStatus());
        assertEquals("Vacant_Dirty", mockRoom.getRoomStatus());

        verify(roomBookingDetailRepository, times(1)).save(mockDetail);
        verify(roomRepository, times(1)).save(mockRoom);
        verify(consolidatedInvoiceRepository, times(1)).save(any(ConsolidatedInvoice.class));
        verify(paymentService, times(1)).recordPayment(any(), any(), eq(new BigDecimal("1100000")), anyString(),
                eq("CASH"), eq(PaymentStatus.SUCCESS), anyString());
    }

    @Test
    @DisplayName("MOD5-TC-004 - Tất toán thất bại do tiền thanh toán không đủ")
    void testCheckoutFolio_Fail_InsufficientPayment() {
        // Arrange
        Long detailId = 1L;
        RoomBookingDetail mockDetail = new RoomBookingDetail();
        mockDetail.setId(detailId);

        when(roomBookingDetailRepository.findById(detailId)).thenReturn(Optional.of(mockDetail));
        when(nightAuditService.calculateFolioBalance(detailId)).thenReturn(new BigDecimal("1000000"));

        Map<String, Object> payload = new HashMap<>();
        payload.put("paymentAmount", 500000);
        payload.put("paymentMethod", "CASH");

        // Act
        ResponseEntity<?> response = folioRestController.checkoutFolio(detailId, payload, null);

        // Assert
        assertTrue(response.getStatusCode().is4xxClientError());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertFalse((Boolean) body.get("success"));
        assertTrue(body.get("message").toString().contains("Số tiền thanh toán chưa đủ"));

        // Ensure no state changes
        verify(roomBookingDetailRepository, never()).save(any());
        verify(consolidatedInvoiceRepository, never()).save(any());
    }

    @Test
    @DisplayName("MOD5-TC-005 - Tách riêng thanh toán không trừ tiền cọc")
    void testCheckoutFolio_Individual_DepositNotSubtracted() {
        // Arrange
        Long detailId = 1L;
        RoomBookingDetail mockDetail = new RoomBookingDetail();
        mockDetail.setId(detailId);
        mockDetail.setRoomCharge(new BigDecimal("1000000"));

        RoomBooking mockBooking = new RoomBooking();
        mockBooking.setId(10L);
        mockBooking.setDepositAmount(new BigDecimal("500000"));
        Customer mockCustomer = new Customer();
        mockCustomer.setEmail("test@gmail.com");
        mockBooking.setCustomer(mockCustomer);
        mockDetail.setRoomBooking(mockBooking);

        Room mockRoom = new Room();
        mockRoom.setRoomNumber("101");
        mockDetail.setRoom(mockRoom);

        // Stubbing
        when(roomBookingDetailRepository.findById(detailId)).thenReturn(Optional.of(mockDetail));
        when(roomBookingDetailRepository.findByRoomBookingId(mockBooking.getId())).thenReturn(List.of(mockDetail));
        when(nightAuditService.calculateFolioBalance(detailId)).thenReturn(new BigDecimal("1000000"));

        // Deposit txn is mocked
        PaymentTransaction depositTxn = new PaymentTransaction();
        depositTxn.setStatus(PaymentStatus.SUCCESS);
        depositTxn.setAmount(new BigDecimal("500000"));
        depositTxn.setTransactionType("ROOM_BOOKING");
        when(paymentService.getPaymentsByBookingId(mockBooking.getId())).thenReturn(List.of(depositTxn));
        when(invoicePdfService.generateInvoicePdf(any())).thenReturn("path/to/invoice.pdf");

        Map<String, Object> payload = new HashMap<>();
        payload.put("isGroup", false); // Tách riêng
        payload.put("paymentAmount", 1100000); // 1.000.000 + 10% VAT = 1.100.000 (Không trừ cọc)
        payload.put("paymentMethod", "CASH");

        // Act
        ResponseEntity<?> response = folioRestController.checkoutFolio(detailId, payload, null);

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        verify(paymentService, times(1)).recordPayment(any(), any(), eq(new BigDecimal("1100000")), anyString(),
                eq("CASH"), eq(PaymentStatus.SUCCESS), anyString());
        // Verify deposit amount on booking remains 500,000 (not subtracted)
        assertEquals(new BigDecimal("500000"), mockBooking.getDepositAmount());
    }

    @Test
    @DisplayName("MOD5-TC-006 - Thanh toán hóa đơn tổng được trừ tiền cọc")
    void testCheckoutFolio_Group_DepositSubtracted() {
        // Arrange
        Long detailId = 1L;
        RoomBookingDetail mockDetail = new RoomBookingDetail();
        mockDetail.setId(detailId);
        mockDetail.setRoomCharge(new BigDecimal("1000000"));

        RoomBooking mockBooking = new RoomBooking();
        mockBooking.setId(10L);
        mockBooking.setDepositAmount(new BigDecimal("500000"));
        Customer mockCustomer = new Customer();
        mockCustomer.setEmail("test@gmail.com");
        mockBooking.setCustomer(mockCustomer);
        mockDetail.setRoomBooking(mockBooking);

        Room mockRoom = new Room();
        mockRoom.setRoomNumber("101");
        mockDetail.setRoom(mockRoom);

        // Stubbing
        when(roomBookingDetailRepository.findById(detailId)).thenReturn(Optional.of(mockDetail));
        when(roomBookingDetailRepository.findByRoomBookingId(mockBooking.getId())).thenReturn(List.of(mockDetail));
        when(nightAuditService.calculateFolioBalance(detailId)).thenReturn(new BigDecimal("1000000"));

        // Deposit txn
        PaymentTransaction depositTxn = new PaymentTransaction();
        depositTxn.setStatus(PaymentStatus.SUCCESS);
        depositTxn.setAmount(new BigDecimal("500000"));
        depositTxn.setTransactionType("ROOM_BOOKING");
        when(paymentService.getPaymentsByBookingId(mockBooking.getId())).thenReturn(List.of(depositTxn));
        when(invoicePdfService.generateInvoicePdf(any())).thenReturn("path/to/invoice.pdf");

        Map<String, Object> payload = new HashMap<>();
        payload.put("isGroup", true); // Hóa đơn tổng
        payload.put("paymentAmount", 600000); // 1.000.000 + 10% VAT = 1.100.000 - 500.000 cọc = 600.000
        payload.put("paymentMethod", "CASH");

        // Act
        ResponseEntity<?> response = folioRestController.checkoutFolio(detailId, payload, null);

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        verify(paymentService, times(1)).recordPayment(any(), any(), eq(new BigDecimal("600000")), anyString(),
                eq("CASH"), eq(PaymentStatus.SUCCESS), anyString());
        // Verify deposit amount on booking is subtracted to 0
        assertEquals(new BigDecimal("0"), mockBooking.getDepositAmount());
    }
}
