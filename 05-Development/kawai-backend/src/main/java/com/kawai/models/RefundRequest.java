package com.kawai.models;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="Refund_Requests")
@Data
public class RefundRequest {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="order_id", nullable=false)
    private FoodOrder order;

    @Column(name="bank_name", nullable=false)
    private String bankName;

    @Column(name="account_number", nullable=false)
    private String accountNumber;

    @Column(name="account_name", nullable=false)
    private String accountName;

    @Column(name="phone_number")
    private String phoneNumber;

    @Column(name="amount", nullable=false)
    private BigDecimal amount;

    @Column(name="status", nullable=false)
    private String status = "Pending";

    @Column(name="manager_note", length=500)
    private String managerNote;

    @Column(name="evidence_image_url", length=1000)
    private String evidenceImageUrl;

    @Column(name="created_at", nullable=false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name="completed_at")
    private LocalDateTime completedAt;
}
