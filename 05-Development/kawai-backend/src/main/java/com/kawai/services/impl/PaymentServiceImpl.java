package com.kawai.services.impl;

import com.kawai.models.Booking;
import com.kawai.models.ConsolidatedInvoice;
import com.kawai.models.PaymentTransaction;
import com.kawai.repositories.PaymentTransactionRepository;
import com.kawai.services.interfaces.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.kawai.models.PaymentStatus;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentTransactionRepository paymentTransactionRepository;

    @Autowired
    public PaymentServiceImpl(PaymentTransactionRepository paymentTransactionRepository) {
        this.paymentTransactionRepository = paymentTransactionRepository;
    }

    @Override
    @Transactional
    public PaymentTransaction recordPayment(ConsolidatedInvoice invoice, Booking booking, BigDecimal amount, String type, String method, PaymentStatus status, String ref) {
        PaymentTransaction tx = new PaymentTransaction();
        if (invoice != null) {
            tx.setInvoice(invoice);
        } else {
            // Fallback: If invoice is null, we need to handle it or ensure DB allows nullable, 
            // but PaymentTransaction schema says invoice_id nullable=false. 
            // In a real scenario, we might create a dummy invoice or throw error if not provided.
            // Assuming invoice is required.
            throw new IllegalArgumentException("Invoice cannot be null for payment transaction");
        }
        tx.setBooking(booking);
        tx.setAmount(amount);
        tx.setTransactionType(type); // e.g., DEPOSIT, FINAL_PAYMENT, REFUND
        tx.setPaymentMethod(method); // e.g., CASH, VNPAY, CREDIT_CARD
        tx.setStatus(status);        // Ghi vào trường Enum chuẩn xác
        tx.setGatewayStatus(status.name()); // Backup vào String để tương thích code cũ (nếu có)
        tx.setTransactionRef(ref);
        tx.setCreatedAt(LocalDateTime.now());
        
        return paymentTransactionRepository.save(tx);
    }

    @Override
    public List<PaymentTransaction> getPaymentsByBookingId(Long bookingId) {
        return paymentTransactionRepository.findByBookingId(bookingId);
    }
}
