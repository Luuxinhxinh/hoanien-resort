package com.kawai.models;

import com.kawai.config.CustomRevisionListener;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;
import java.util.Date;

@Entity
@Table(name = "revinfo_custom")
@RevisionEntity(CustomRevisionListener.class)
@Data
public class CustomRevisionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @RevisionNumber
    private int id;

    @RevisionTimestamp
    @Column(name = "REVTSTMP")
    private long timestamp;

    @Column(columnDefinition = "TEXT")
    private String username;

    @Transient
    public Date getRevisionDate() {
        return new Date(timestamp);
    }
}
