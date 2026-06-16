package com.kawai.models;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity @Table(name="Consolidated_Invoices") @Data
public class ConsolidatedInvoice {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="invoice_id") private Long id;
    @Column(name="invoice_number", unique=true, nullable=false) private String invoiceNumber;
    @OneToOne @JoinColumn(name="booking_id", unique=true, nullable=false) private Booking booking;
    @Column(name="subtotal_before_vat", nullable=false) private BigDecimal subtotalBeforeVat;
    @Column(name="vat_amount", nullable=false) private BigDecimal vatAmount;
    @Column(name="total_amount", nullable=false) private BigDecimal totalAmount;
    @ManyToOne @JoinColumn(name="promo_id") private Promotion promo;
    @Column(name="invoice_status", nullable=false) private String invoiceStatus = "Draft";
    @Column(name="created_at", nullable=false) private LocalDateTime createdAt = LocalDateTime.now();
    @Column(name="issued_at", nullable=false) private LocalDateTime issuedAt = LocalDateTime.now();
}
