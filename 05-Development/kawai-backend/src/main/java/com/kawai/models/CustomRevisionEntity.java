package com.kawai.models;

import com.kawai.config.CustomRevisionListener;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionEntity;

@Entity
@Table(name = "revinfo_custom")
@RevisionEntity(CustomRevisionListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class CustomRevisionEntity extends DefaultRevisionEntity {
    @jakarta.persistence.Column(columnDefinition = "TEXT")
    private String username;
}
