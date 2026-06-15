package com.kawai.models;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
@Entity @Table(name="Audit_Logs") @Data
public class AuditLog {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="log_id") private Long id;
    @ManyToOne @JoinColumn(name="account_id") private Account account;
    @Column(nullable=false) private String action;
    @Column(name="table_name", nullable=false) private String tableName;
    @Column(name="record_id", nullable=false) private Long recordId;
    @Column(name="old_value", columnDefinition="TEXT") private String oldValue;
    @Column(name="new_value", columnDefinition="TEXT") private String newValue;
    @Column(name="ip_address", nullable=false) private String ipAddress;
    @Column(nullable=false) private LocalDateTime timestamp = LocalDateTime.now();
}
