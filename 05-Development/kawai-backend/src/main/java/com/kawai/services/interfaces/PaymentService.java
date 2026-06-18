package com.kawai.services.interfaces;

import com.kawai.models.Booking;
import com.kawai.models.ConsolidatedInvoice;
import com.kawai.models.PaymentTransaction;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentService {
    PaymentTransaction recordPayment(ConsolidatedInvoice invoice, Booking booking, BigDecimal amount, String type, String method, String status, String ref);
    List<PaymentTransaction> getPaymentsByBookingId(Long bookingId);
}
