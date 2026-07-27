package com.hoanien.services.interfaces;

import com.hoanien.models.Booking;
import com.hoanien.models.ConsolidatedInvoice;
import com.hoanien.models.PaymentTransaction;

import java.math.BigDecimal;
import java.util.List;

import com.hoanien.models.PaymentStatus;

public interface PaymentService {
    PaymentTransaction recordPayment(ConsolidatedInvoice invoice, Booking booking, BigDecimal amount, String type, String method, PaymentStatus status, String ref);
    List<PaymentTransaction> getPaymentsByBookingId(Long bookingId);
}
