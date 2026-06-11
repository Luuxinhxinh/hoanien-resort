package com.kawai.models;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity @Table(name="Folio_Items") @Data
public class FolioItem {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="folio_item_id") private Long id;
    @ManyToOne @JoinColumn(name="booking_id", nullable=false) private Booking booking;
    @ManyToOne @JoinColumn(name="room_booking_detail_id") private RoomBookingDetail roomBookingDetail;
    @ManyToOne @JoinColumn(name="payer_customer_id", nullable=false) private Customer payerCustomer;
    @Column(name="source_department", nullable=false) private String sourceDepartment;
    @Column(nullable=false) private BigDecimal amount;
    @Column(nullable=false) private String description;
    @Column(name="is_settled_separately", nullable=false) private Boolean isSettledSeparately = false;
    @ManyToOne @JoinColumn(name="created_by_staff_id") private Employee createdByStaff;
    @Column(name="created_at", nullable=false) private LocalDateTime createdAt = LocalDateTime.now();
    @Column(name="signature_img_url", length=500) private String signatureImgUrl;
}
