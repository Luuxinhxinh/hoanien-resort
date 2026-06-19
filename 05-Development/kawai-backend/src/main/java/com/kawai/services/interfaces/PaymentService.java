package com.kawai.services.interfaces;

import com.kawai.models.Booking;
import com.kawai.models.ConsolidatedInvoice;
import com.kawai.models.PaymentTransaction;

import java.math.BigDecimal;
import java.util.List;

import com.kawai.models.PaymentStatus;

public interface PaymentService {
    PaymentTransaction recordPayment(ConsolidatedInvoice invoice, Booking booking, BigDecimal amount, String type, String method, PaymentStatus status, String ref);
    List<PaymentTransaction> getPaymentsByBookingId(Long bookingId);
}
