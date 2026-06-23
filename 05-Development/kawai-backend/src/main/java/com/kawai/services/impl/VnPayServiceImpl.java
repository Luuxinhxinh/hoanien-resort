package com.kawai.services.impl;

import com.kawai.config.VnPayConfig;
import com.kawai.exceptions.BusinessException;
import com.kawai.models.Booking;
import com.kawai.models.PaymentStatus;
import com.kawai.models.PaymentTransaction;
import com.kawai.models.FoodOrder;
import com.kawai.models.RoomBooking;
import com.kawai.repositories.FoodOrderRepository;
import com.kawai.repositories.PaymentTransactionRepository;
import com.kawai.repositories.RoomBookingRepository;
import com.kawai.services.interfaces.VnPayService;
import com.kawai.utils.VnPayUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VnPayServiceImpl implements VnPayService {

    @Autowired
    private VnPayConfig vnPayConfig;

    @Autowired
    private RoomBookingRepository roomBookingRepository;

    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;

    @Autowired
    private com.kawai.repositories.ConsolidatedInvoiceRepository consolidatedInvoiceRepository;

    @Autowired
    private FoodOrderRepository foodOrderRepository;

    @Override
    @Transactional
    public String createPaymentUrl(Long bookingId, String ipAddress) {
        RoomBooking booking = roomBookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException("BOOKING_NOT_FOUND", "Không tìm thấy thông tin đặt phòng"));

        // 1. Tạo PaymentTransaction status = INIT
        PaymentTransaction txn = new PaymentTransaction();
        txn.setBooking(booking);
        txn.setAmount(booking.getDepositAmount());
        txn.setStatus(PaymentStatus.INIT);
        txn.setTransactionType("ROOM_BOOKING");
        txn.setCreatedAt(LocalDateTime.now());

        // 2. Sinh transactionRef mới
        String transactionRef = bookingId + "_" + System.currentTimeMillis();
        txn.setTransactionRef(transactionRef);
        paymentTransactionRepository.save(txn);

        // 3. Build params VNPay
        long amountVal = booking.getDepositAmount().multiply(new BigDecimal("100")).setScale(0, RoundingMode.HALF_UP)
                .longValue();
        String createDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnPayConfig.getApiVersion());
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        vnp_Params.put("vnp_Amount", String.valueOf(amountVal));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", transactionRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan dat coc phong " + bookingId);
        vnp_Params.put("vnp_OrderType", "250000");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
        vnp_Params.put("vnp_IpAddr", ipAddress);
        vnp_Params.put("vnp_CreateDate", createDate);

        // 4. Lọc null/empty, sắp xếp và build hashData & query
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        java.util.Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        java.util.Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                try {
                    // Build hash data
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    // Build query
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    if (itr.hasNext()) {
                        query.append('&');
                        hashData.append('&');
                    }
                } catch (java.io.UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }

        String queryUrl = query.toString();
        String vnp_SecureHash = VnPayUtil.hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

        return vnPayConfig.getPayUrl() + "?" + queryUrl;
    }

    @Override
    @Transactional
    public String createPaymentUrlForFoodOrder(Long orderId, String ipAddress) {
        FoodOrder foodOrder = foodOrderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("ORDER_NOT_FOUND", "Không tìm thấy thông tin đơn món"));

        // 1. Tạo PaymentTransaction status = INIT
        PaymentTransaction txn = new PaymentTransaction();
        txn.setFoodOrder(foodOrder);
        txn.setAmount(foodOrder.getTotalAmount());
        txn.setStatus(PaymentStatus.INIT);
        txn.setTransactionType("FOOD_ORDER");
        txn.setPaymentMethod("VNPAY");
        txn.setCreatedAt(LocalDateTime.now());

        // 2. Sinh transactionRef mới
        String transactionRef = "FOOD_" + orderId + "_" + System.currentTimeMillis();
        txn.setTransactionRef(transactionRef);
        paymentTransactionRepository.save(txn);

        // 3. Build params VNPay
        long amountVal = foodOrder.getTotalAmount().multiply(new BigDecimal("100")).setScale(0, RoundingMode.HALF_UP)
                .longValue();
        String createDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnPayConfig.getApiVersion());
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        vnp_Params.put("vnp_Amount", String.valueOf(amountVal));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", transactionRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don goi mon " + orderId);
        vnp_Params.put("vnp_OrderType", "250000");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
        vnp_Params.put("vnp_IpAddr", ipAddress);
        vnp_Params.put("vnp_CreateDate", createDate);

        // 4. Lọc null/empty, sắp xếp và build hashData & query
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        java.util.Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        java.util.Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                try {
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    if (itr.hasNext()) {
                        query.append('&');
                        hashData.append('&');
                    }
                } catch (java.io.UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }

        String queryUrl = query.toString();
        String vnp_SecureHash = VnPayUtil.hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

        return vnPayConfig.getPayUrl() + "?" + queryUrl;
    }

    @Override
    @Transactional
    public Map<String, String> verifyIpn(Map<String, String> params) {
        Map<String, String> response = new HashMap<>();

        // Bước 2: Lấy vnp_SecureHash
        String secureHash = params.get("vnp_SecureHash");

        // Xác thực chữ ký (Bước 3, 4, 5, 6 được xử lý trong validateSignature)
        boolean isValidSignature = VnPayUtil.validateSignature(params, secureHash, vnPayConfig.getHashSecret());
        if (!isValidSignature) {
            response.put("RspCode", "97");
            response.put("Message", "Invalid Checksum");
            return response;
        }

        String txnRef = params.get("vnp_TxnRef");
        PaymentTransaction txn = paymentTransactionRepository.findByTransactionRef(txnRef).orElse(null);
        if (txn == null) {
            response.put("RspCode", "01");
            response.put("Message", "Order not found");
            return response;
        }

        // 10. Validate Amount
        long vnpAmount = Long.parseLong(params.getOrDefault("vnp_Amount", "0"));
        long expectedAmount = txn.getAmount().multiply(new BigDecimal("100")).setScale(0, RoundingMode.HALF_UP)
                .longValue();
        if (vnpAmount != expectedAmount) {
            response.put("RspCode", "04");
            response.put("Message", "Invalid Amount");
            return response;
        }

        // 11. Idempotency check
        if (txn.getStatus() == PaymentStatus.SUCCESS) {
            response.put("RspCode", "02");
            response.put("Message", "Order already confirmed");
            return response;
        }

        String rspCode = params.get("vnp_ResponseCode");
        txn.setVnpTransactionNo(params.get("vnp_TransactionNo"));
        txn.setResponseCode(rspCode);

        Booking booking = txn.getBooking();

        // 12. Thành công
        if ("00".equals(rspCode)) {
            txn.setStatus(PaymentStatus.SUCCESS);
            txn.setPaidAt(LocalDateTime.now());

            if ("FOOD_ORDER".equals(txn.getTransactionType())) {
                FoodOrder foodOrder = txn.getFoodOrder();
                if (foodOrder != null) {
                    foodOrder.setOrderStatus("PAID");
                    foodOrder.setIsPaidInPos(true);
                    foodOrderRepository.save(foodOrder);
                }
            } else if ("ROOM_BOOKING".equals(txn.getTransactionType()) && booking != null) {
                if ("PENDING".equals(booking.getBookingStatus()) || "HOLD".equals(booking.getBookingStatus())) {
                    booking.setBookingStatus("CONFIRMED");
                }
            }

            // Tự động chuyển trạng thái Hóa Đơn sang PAID
            if (txn.getInvoice() != null) {
                com.kawai.models.ConsolidatedInvoice invoice = txn.getInvoice();
                invoice.setInvoiceStatus("Paid");
                consolidatedInvoiceRepository.save(invoice);
            }
        } else {
            txn.setStatus(PaymentStatus.FAILED);
        }

        paymentTransactionRepository.save(txn);

        response.put("RspCode", "00");
        response.put("Message", "Confirm Success");
        return response;
    }
}
