package com.kawai.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "Export_History")
@Data
public class ExportHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "report_name")
    private String reportName;
    
    @Column(name = "format")
    private String format;
    
    @Column(name = "exported_at")
    private LocalDateTime exportedAt;
    
    @Column(name = "exported_by")
    private String exportedBy;
    
    @Column(name = "file_size")
    private String fileSize;
}
