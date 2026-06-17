package com.kawai.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Payment_Transactions")
@Getter
@Setter
public class PaymentTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "booking_id", nullable = false)
    @ManyToOne(optional = false)
    private Booking booking;

    @JoinColumn(name = "invoice_id", nullable = true)
    @ManyToOne(optional = true)
    private ConsolidatedInvoice invoice;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    private String transactionRef; // internal: bookingId_timestamp

    private String vnpTransactionNo; // [NEW] Mã giao dịch do VNPAY trả về (vnp_TransactionNo)

    private String responseCode; // [NEW] Mã phản hồi VNPAY để debug và audit (vnp_ResponseCode)

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime paidAt;
}