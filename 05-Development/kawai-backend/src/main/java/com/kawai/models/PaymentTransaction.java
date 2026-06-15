package com.kawai.models;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity @Table(name="Payment_Transactions") @Data
public class PaymentTransaction {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="transaction_id") private Long id;
    @ManyToOne @JoinColumn(name="invoice_id", nullable=false) private ConsolidatedInvoice invoice;
    @ManyToOne @JoinColumn(name="booking_id", nullable=false) private Booking booking;
    @Column(nullable=false) private BigDecimal amount;
    @Column(name="transaction_type", nullable=false) private String transactionType;
    @Column(name="payment_method", nullable=false) private String paymentMethod;
    @Column(name="gateway_status", nullable=false) private String gatewayStatus;
    @Column(name="transaction_ref", unique=true, nullable=false) private String transactionRef;
    @Column(name="created_at", nullable=false) private LocalDateTime createdAt = LocalDateTime.now();
}
