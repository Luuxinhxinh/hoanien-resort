package com.kawai.models;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
@Entity @Table(name="Hotel_Operations") @Data
public class HotelOperation {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="task_id") private Long id;
    @ManyToOne @JoinColumn(name="room_id", nullable=false) private Room room;
    @ManyToOne @JoinColumn(name="staff_id", nullable=false) private Employee staff;
    @ManyToOne @JoinColumn(name="supervisor_id", nullable=false) private Employee supervisor;
    @Column(name="operational_type", nullable=false) private String operationalType;
    @Column(nullable=false) private String priority = "Normal";
    @Column(nullable=false) private String status = "Pending";
    @Column(name="created_at", nullable=false) private LocalDateTime createdAt = LocalDateTime.now();
    @Column(name="started_at") private LocalDateTime startedAt;
    @Column(name="completed_at") private LocalDateTime completedAt;
    @Column(columnDefinition="TEXT") private String notes;
}
