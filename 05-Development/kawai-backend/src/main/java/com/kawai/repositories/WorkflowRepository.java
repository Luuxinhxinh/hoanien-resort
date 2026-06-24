package com.kawai.repositories;

import com.kawai.models.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowRepository extends JpaRepository<Workflow, Long> {
    List<Workflow> findByTriggerEventAndIsActive(String triggerEvent, Boolean isActive);
    boolean existsByWorkflowNameIgnoreCase(String workflowName);
}
