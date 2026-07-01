package com.kawai.models;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Refund_Requests")
@Data
public class RefundRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private FoodOrder order;

    @ManyToOne
    @JoinColumn(name = "room_booking_id")
    private RoomBooking roomBooking;

    @ManyToOne
    @JoinColumn(name = "tour_booking_id")
    private TourBooking tourBooking;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "account_name")
    private String accountName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "status", nullable = false)
    private String status = "Pending";

    @Column(name = "manager_note", length = 500)
    private String managerNote;

    @Column(name = "evidence_image_url", length = 1000)
    private String evidenceImageUrl;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public String getReferenceCode() {
        if (order != null)
            return "ORD-" + order.getId();
        if (roomBooking != null)
            return "RB-" + roomBooking.getId();
        if (tourBooking != null)
            return "TB-" + tourBooking.getId();
        return "N/A";
    }
}
