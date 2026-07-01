package com.kawai.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Fnb_Daily_Reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
public class FnBDailyReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long id;

    // Ngày chốt báo cáo (Chỉ được phép có 1 bản ghi duy nhất cho 1 ngày)
    @Column(name = "report_date", nullable = false, unique = true)
    private LocalDate reportDate;

    // Nhân viên thực hiện thao tác chốt ca/chốt ngày
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "closed_by_staff_id", nullable = false)
    @org.hibernate.envers.Audited(targetAuditMode = org.hibernate.envers.RelationTargetAuditMode.NOT_AUDITED)
    private Employee closedByStaff;

    // Thống kê lượng đơn
    @Column(name = "total_dine_in_orders", nullable = false)
    private Integer totalDineInOrders;

    @Column(name = "total_room_service_orders", nullable = false)
    private Integer totalRoomServiceOrders;

    // Doanh thu chia theo nguồn tiền
    @Column(name = "total_cash_revenue", nullable = false)
    private BigDecimal totalCashRevenue; // Đây là số tiền thực tế nhân viên phải đếm được trong két sắt

    @Column(name = "total_vnpay_revenue", nullable = false)
    private BigDecimal totalVnpayRevenue; // Tiền chảy về tài khoản hệ thống (không nằm ở quầy)

    @Column(name = "total_charge_to_room_revenue", nullable = false)
    private BigDecimal totalChargeToRoomRevenue; // Tiền khách nợ, lễ tân sẽ thu lúc check-out

    // Tổng doanh thu = Tiền mặt + VNPAY + Ghi nợ phòng
    @Column(name = "total_revenue", nullable = false)
    private BigDecimal totalRevenue;

    @Column(name = "closed_at", nullable = false)
    private LocalDateTime closedAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
