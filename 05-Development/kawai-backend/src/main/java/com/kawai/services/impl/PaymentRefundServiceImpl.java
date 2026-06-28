package com.kawai.services.impl;

import com.kawai.config.VnPayConfig;
import com.kawai.models.PaymentStatus;
import com.kawai.models.PaymentTransaction;
import com.kawai.repositories.PaymentTransactionRepository;
import com.kawai.services.interfaces.PaymentRefundService;
import com.kawai.utils.VnPayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentRefundServiceImpl implements PaymentRefundService {

    private static final Logger log = LoggerFactory.getLogger(PaymentRefundServiceImpl.class);

    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;

    @Autowired
    private VnPayConfig vnPayConfig;

    @Override
    public void processRefund(String transactionId, BigDecimal amount) {
        String prefix = "TXN_";
        if (transactionId.startsWith(prefix)) {
            Long bookingId = Long.parseLong(transactionId.substring(prefix.length()));
            List<PaymentTransaction> txns = paymentTransactionRepository.findByBookingId(bookingId);
            
            // Tìm giao dịch thanh toán thành công qua VNPay
            PaymentTransaction txnToRefund = txns.stream()
                .filter(t -> t.getStatus() == PaymentStatus.SUCCESS && t.getVnpTransactionNo() != null)
                .findFirst()
                .orElse(null);
                
            if (txnToRefund != null) {
                doVnPayRefund(txnToRefund, amount);
            } else {
                log.warn("Không tìm thấy giao dịch VNPay thành công nào cho bookingId {}", bookingId);
            }
        }
    }

    private void doVnPayRefund(PaymentTransaction txn, BigDecimal amountToRefund) {
        try {
            String vnp_RequestId = UUID.randomUUID().toString();
            String vnp_Version = vnPayConfig.getApiVersion();
            String vnp_Command = "refund";
            String vnp_TmnCode = vnPayConfig.getTmnCode();
            String vnp_TransactionType = "02"; // 02: Hoàn trả toàn phần
            String vnp_TxnRef = txn.getTransactionRef();
            long amountVal = amountToRefund.multiply(new BigDecimal("100")).setScale(0, RoundingMode.HALF_UP).longValue();
            String vnp_Amount = String.valueOf(amountVal);
            String vnp_OrderInfo = "Hoan tien dat coc cho booking " + txn.getBooking().getId();
            String vnp_TransactionNo = txn.getVnpTransactionNo();
            // Lấy thời gian tạo giao dịch (do không có vnp_PayDate trả về, dùng thời gian tạo nội bộ làm default)
            String vnp_TransactionDate = txn.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")); 
            String vnp_CreateBy = "system";
            String vnp_CreateDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String vnp_IpAddr = "127.0.0.1";
            
            String hashData = vnp_RequestId + "|" + vnp_Version + "|" + vnp_Command + "|" + vnp_TmnCode + "|" + 
                              vnp_TransactionType + "|" + vnp_TxnRef + "|" + vnp_Amount + "|" + vnp_TransactionNo + "|" + 
                              vnp_TransactionDate + "|" + vnp_CreateBy + "|" + vnp_CreateDate + "|" + vnp_IpAddr + "|" + vnp_OrderInfo;
            
            String vnp_SecureHash = VnPayUtil.hmacSHA512(vnPayConfig.getHashSecret(), hashData);
            
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("vnp_RequestId", vnp_RequestId);
            requestBody.put("vnp_Version", vnp_Version);
            requestBody.put("vnp_Command", vnp_Command);
            requestBody.put("vnp_TmnCode", vnp_TmnCode);
            requestBody.put("vnp_TransactionType", vnp_TransactionType);
            requestBody.put("vnp_TxnRef", vnp_TxnRef);
            requestBody.put("vnp_Amount", vnp_Amount);
            requestBody.put("vnp_OrderInfo", vnp_OrderInfo);
            requestBody.put("vnp_TransactionNo", vnp_TransactionNo);
            requestBody.put("vnp_TransactionDate", vnp_TransactionDate);
            requestBody.put("vnp_CreateBy", vnp_CreateBy);
            requestBody.put("vnp_CreateDate", vnp_CreateDate);
            requestBody.put("vnp_IpAddr", vnp_IpAddr);
            requestBody.put("vnp_SecureHash", vnp_SecureHash);

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);
            // Sandbox URL cho Refund VNPAY
            String refundUrl = "https://sandbox.vnpayment.vn/merchant_webapi/api/transaction";
            
            log.info("Sending Refund Request to VNPay: {}", requestBody);
            ResponseEntity<Map> response = restTemplate.postForEntity(refundUrl, request, Map.class);
            log.info("VNPay Refund Response: {}", response.getBody());
            
            if (response.getBody() != null) {
                String responseCode = (String) response.getBody().get("vnp_ResponseCode");
                if ("00".equals(responseCode)) {
                    log.info("Refund successful! Recording PaymentRefund transaction for booking {}", txn.getBooking().getId());
                    PaymentTransaction refundTxn = new PaymentTransaction();
                    refundTxn.setBooking(txn.getBooking());
                    refundTxn.setAmount(amountToRefund);
                    refundTxn.setStatus(PaymentStatus.SUCCESS);
                    refundTxn.setTransactionType("PaymentRefund");
                    refundTxn.setPaymentMethod("VNPAY");
                    refundTxn.setTransactionRef(txn.getTransactionRef() + "_REFUND");
                    refundTxn.setVnpTransactionNo(txn.getVnpTransactionNo());
                    refundTxn.setResponseCode(responseCode);
                    refundTxn.setCreatedAt(LocalDateTime.now());
                    refundTxn.setPaidAt(LocalDateTime.now());
                    
                    paymentTransactionRepository.save(refundTxn);
                } else {
                    log.warn("VNPay Refund failed with code: {}", responseCode);
                }
            }
            
        } catch (Exception e) {
            log.error("Error processing VNPay Refund", e);
        }
    }
}
