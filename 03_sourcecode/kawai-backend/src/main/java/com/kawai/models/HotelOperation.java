package com.kawai.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Hotel_Operations")
public class HotelOperation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne
    @JoinColumn(name = "staff_id", nullable = false)
    private Employee staff;

    @ManyToOne
    @JoinColumn(name = "supervisor_id", nullable = false)
    private Employee supervisor;

    @Column(name = "operational_type", nullable = false)
    private String operationalType;

    @Column(nullable = false)
    private String priority = "Normal";

    @Column(nullable = false)
    private String status = "Pending";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(columnDefinition = "TEXT")
    private String notes;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long v) {
        this.id = v;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room v) {
        this.room = v;
    }

    public Employee getStaff() {
        return staff;
    }

    public void setStaff(Employee v) {
        this.staff = v;
    }

    public Employee getSupervisor() {
        return supervisor;
    }

    public void setSupervisor(Employee v) {
        this.supervisor = v;
    }

    public String getOperationalType() {
        return operationalType;
    }

    public void setOperationalType(String v) {
        this.operationalType = v;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String v) {
        this.priority = v;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String v) {
        this.status = v;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime v) {
        this.createdAt = v;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime v) {
        this.startedAt = v;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime v) {
        this.completedAt = v;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String v) {
        this.notes = v;
    }
}