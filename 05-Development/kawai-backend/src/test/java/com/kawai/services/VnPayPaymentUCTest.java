package com.kawai.services;

import com.kawai.config.VnPayConfig;
import com.kawai.models.Booking;
import com.kawai.models.PaymentStatus;
import com.kawai.models.PaymentTransaction;
import com.kawai.models.RoomBooking;
import com.kawai.repositories.PaymentTransactionRepository;
import com.kawai.repositories.RoomBookingRepository;
import com.kawai.services.impl.VnPayServiceImpl;
import com.kawai.utils.VnPayUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UC-PAY — Thanh toán VNPAY")
class VnPayPaymentUCTest {

    @Mock
    private VnPayConfig vnPayConfig;

    @Mock
    private RoomBookingRepository roomBookingRepository;

    @Mock
    private PaymentTransactionRepository paymentTransactionRepository;

    @InjectMocks
    private VnPayServiceImpl vnPayService;

    private final String HASH_SECRET = "4JVVJ2NUMIMEWODNGA3CR8DV52L5PKII";

    @BeforeEach
    void setUp() {
        // Setup leniency cho config mock
        lenient().when(vnPayConfig.getTmnCode()).thenReturn("77G0NGGT");
        lenient().when(vnPayConfig.getHashSecret()).thenReturn(HASH_SECRET);
        lenient().when(vnPayConfig.getPayUrl()).thenReturn("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html");
        lenient().when(vnPayConfig.getReturnUrl()).thenReturn("http://localhost:8080/api/v1/payments/vnpay-return");
        lenient().when(vnPayConfig.getApiVersion()).thenReturn("2.1.0");
    }

    @Test
    @DisplayName("Create Payment URL - Success")
    void testCreatePaymentUrl() {
        RoomBooking booking = new RoomBooking();
        booking.setId(123L);
        booking.setDepositAmount(new BigDecimal("150000")); // 150k

        when(roomBookingRepository.findById(123L)).thenReturn(Optional.of(booking));
        when(paymentTransactionRepository.save(any(PaymentTransaction.class))).thenAnswer(i -> i.getArguments()[0]);

        String url = vnPayService.createPaymentUrl(123L, "127.0.0.1");

        assertNotNull(url);
        assertTrue(url.startsWith("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html"));
        assertTrue(url.contains("vnp_Amount=15000000")); // 150000 * 100
        assertTrue(url.contains("vnp_TmnCode=77G0NGGT"));
        assertTrue(url.contains("vnp_SecureHash="));
    }

    @Test
    @DisplayName("Checksum Generation - Validated by VnPayUtil")
    void testChecksumGeneration() {
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Amount", "15000000");
        params.put("vnp_Command", "pay");
        params.put("vnp_CreateDate", "20231010101010");
        params.put("vnp_TmnCode", "77G0NGGT");
        params.put("vnp_TxnRef", "123_123456789");

        String rawString = VnPayUtil.buildRawHashString(params);
        String secureHash = VnPayUtil.hmacSHA512(HASH_SECRET, rawString);

        assertNotNull(secureHash);

        // Add hash back to verify validation works
        params.put("vnp_SecureHash", secureHash);
        assertTrue(VnPayUtil.validateSignature(params, secureHash, HASH_SECRET));
    }

    @Test
    @DisplayName("Invalid Checksum - Returns 97")
    void testVerifyIpn_InvalidChecksum() {
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Amount", "15000000");
        params.put("vnp_SecureHash", "wrong_hash_123");

        Map<String, String> result = vnPayService.verifyIpn(params);

        assertEquals("97", result.get("RspCode"));
        assertEquals("Invalid Checksum", result.get("Message"));
    }

    @Test
    @DisplayName("Amount Mismatch - Returns 04")
    void testVerifyIpn_AmountMismatch() {
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Amount", "20000000"); // 200k from VNPay
        params.put("vnp_TxnRef", "123_123456");

        String rawString = VnPayUtil.buildRawHashString(params);
        String secureHash = VnPayUtil.hmacSHA512(HASH_SECRET, rawString);
        params.put("vnp_SecureHash", secureHash);

        PaymentTransaction txn = new PaymentTransaction();
        txn.setAmount(new BigDecimal("150000")); // 150k in DB
        txn.setStatus(PaymentStatus.INIT);

        when(paymentTransactionRepository.findByTransactionRef("123_123456")).thenReturn(Optional.of(txn));

        Map<String, String> result = vnPayService.verifyIpn(params);

        assertEquals("04", result.get("RspCode"));
        assertEquals("Invalid Amount", result.get("Message"));
    }

    @Test
    @DisplayName("Duplicate IPN - Idempotency - Returns 02")
    void testVerifyIpn_DuplicateIPN() {
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Amount", "15000000");
        params.put("vnp_TxnRef", "123_123456");

        String rawString = VnPayUtil.buildRawHashString(params);
        String secureHash = VnPayUtil.hmacSHA512(HASH_SECRET, rawString);
        params.put("vnp_SecureHash", secureHash);

        PaymentTransaction txn = new PaymentTransaction();
        txn.setAmount(new BigDecimal("150000"));
        txn.setStatus(PaymentStatus.SUCCESS); // Already SUCCESS

        when(paymentTransactionRepository.findByTransactionRef("123_123456")).thenReturn(Optional.of(txn));

        Map<String, String> result = vnPayService.verifyIpn(params);

        assertEquals("02", result.get("RspCode"));
        assertEquals("Order already confirmed", result.get("Message"));
    }

    @Test
    @DisplayName("Successful Payment - Returns 00")
    void testVerifyIpn_SuccessfulPayment() {
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Amount", "15000000");
        params.put("vnp_TxnRef", "123_123456");
        params.put("vnp_ResponseCode", "00");
        params.put("vnp_TransactionNo", "VNP123456");

        String rawString = VnPayUtil.buildRawHashString(params);
        String secureHash = VnPayUtil.hmacSHA512(HASH_SECRET, rawString);
        params.put("vnp_SecureHash", secureHash);

        PaymentTransaction txn = new PaymentTransaction();
        txn.setAmount(new BigDecimal("150000"));
        txn.setStatus(PaymentStatus.INIT);
        txn.setTransactionType("ROOM_BOOKING");

        RoomBooking booking = new RoomBooking();
        booking.setBookingStatus("Pending_Payment"); // BR-RSV-04: tại thời điểm IPN callback, booking đã qua confirmBooking() → phải là Pending_Payment
        txn.setBooking(booking);

        when(paymentTransactionRepository.findByTransactionRef("123_123456")).thenReturn(Optional.of(txn));
        when(paymentTransactionRepository.save(any(PaymentTransaction.class))).thenAnswer(i -> i.getArguments()[0]);

        Map<String, String> result = vnPayService.verifyIpn(params);

        assertEquals("00", result.get("RspCode"));
        assertEquals("Confirm Success", result.get("Message"));
        assertEquals(PaymentStatus.SUCCESS, txn.getStatus());
        assertEquals("00", txn.getResponseCode());
        assertEquals("VNP123456", txn.getVnpTransactionNo());
        assertNotNull(txn.getPaidAt());
        assertEquals("CONFIRMED", booking.getBookingStatus());
    }
}
