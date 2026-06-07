package com.kawai.models.core;
import jakarta.persistence.*;
import lombok.Data;
@Entity @Table(name="Tour_Staff_Assignments") @Data
public class TourStaffAssignment {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="assignment_id") private Long id;
    @ManyToOne @JoinColumn(name="schedule_id", nullable=false) private TourSchedule schedule;
    @ManyToOne @JoinColumn(name="employee_id", nullable=false) private Employee employee;
    @Column(name="staff_role", nullable=false) private String staffRole;
}
