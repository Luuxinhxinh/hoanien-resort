package com.kawai.models;
import jakarta.persistence.*;
import lombok.Data;
@Entity @Table(name="Roles") @Data
public class Role {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="role_id") private Long id;
    @Column(name="role_name", unique=true, nullable=false) private String roleName;
}
