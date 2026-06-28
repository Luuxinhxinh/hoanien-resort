package com.kawai.models;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
@Entity @Table(name="Tour_Staff_Assignments") @Data
public class TourStaffAssignment {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="assignment_id") private Long id;
    @ManyToOne @JoinColumn(name="schedule_id", nullable=false) private TourSchedule schedule;
    @ManyToOne @JoinColumn(name="employee_id", nullable=false) private Employee employee;
    @Column(name="staff_role", nullable=false) private String staffRole;
    @Column(name="is_lead_guide") private Boolean isLeadGuide = false;
    @Column(name="current_gps_lat", precision=10, scale=8) private BigDecimal currentGpsLat;
    @Column(name="current_gps_lng", precision=11, scale=8) private BigDecimal currentGpsLng;
}
