package com.kawai.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "workflows")
public class Workflow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "workflow_id")
    private Long id;

    @Column(name = "workflow_name", nullable = false)
    private String workflowName;

    @Column(name = "trigger_event", nullable = false, length = 100)
    private String triggerEvent;

    @Column(name = "conditions_json", columnDefinition = "TEXT")
    private String conditionsJson;

    @Column(name = "actions_json", columnDefinition = "TEXT")
    private String actionsJson;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
    }

    public String getTriggerEvent() {
        return triggerEvent;
    }

    public void setTriggerEvent(String triggerEvent) {
        this.triggerEvent = triggerEvent;
    }

    public String getConditionsJson() {
        return conditionsJson;
    }

    public void setConditionsJson(String conditionsJson) {
        this.conditionsJson = conditionsJson;
    }

    public String getActionsJson() {
        return actionsJson;
    }

    public void setActionsJson(String actionsJson) {
        this.actionsJson = actionsJson;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
