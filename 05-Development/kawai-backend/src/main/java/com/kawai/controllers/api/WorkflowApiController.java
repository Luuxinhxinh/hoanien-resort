package com.kawai.controllers.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kawai.models.Workflow;
import com.kawai.repositories.WorkflowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/api/v1/workflows")
@RequiredArgsConstructor
public class WorkflowApiController {

    private final WorkflowRepository workflowRepository;
    private final ObjectMapper objectMapper;

    @GetMapping
    public ResponseEntity<List<Workflow>> getAllWorkflows() {
        List<Workflow> workflows = workflowRepository.findAll();
        return ResponseEntity.ok(workflows);
    }

    @PostMapping
    public ResponseEntity<?> saveWorkflow(@RequestBody Map<String, Object> payload) {
        try {
            Workflow workflow = new Workflow();
            if (payload.containsKey("id") && payload.get("id") != null) {
                Long id = Long.parseLong(payload.get("id").toString());
                workflow = workflowRepository.findById(id).orElse(new Workflow());
            }

            String name = (String) payload.get("workflowName");
            String triggerEvent = (String) payload.get("triggerEvent");
            Object conditionsObj = payload.get("conditionsJson");
            Object actionsObj = payload.get("actionsJson");
            Boolean isActive = (Boolean) payload.get("isActive");

            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Workflow name is required"));
            }
            if (triggerEvent == null || triggerEvent.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Trigger event is required"));
            }

            workflow.setWorkflowName(name);
            workflow.setTriggerEvent(triggerEvent);

            if (isActive != null) {
                workflow.setIsActive(isActive);
            } else {
                workflow.setIsActive(true);
            }

            // Convert map/list to JSON string if passed as object
            if (conditionsObj instanceof String) {
                workflow.setConditionsJson((String) conditionsObj);
            } else if (conditionsObj != null) {
                workflow.setConditionsJson(objectMapper.writeValueAsString(conditionsObj));
            } else {
                workflow.setConditionsJson("{}");
            }

            if (actionsObj instanceof String) {
                workflow.setActionsJson((String) actionsObj);
            } else if (actionsObj != null) {
                workflow.setActionsJson(objectMapper.writeValueAsString(actionsObj));
            } else {
                workflow.setActionsJson("[]");
            }

            Workflow saved = workflowRepository.save(workflow);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to save workflow: " + e.getMessage()));
        }
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<?> toggleWorkflow(@PathVariable Long id) {
        try {
            Workflow workflow = workflowRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Workflow not found with id: " + id));
            workflow.setIsActive(!workflow.getIsActive());
            Workflow saved = workflowRepository.save(workflow);
            return ResponseEntity.ok(Map.of("message", "Toggled successfully", "isActive", saved.getIsActive()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to toggle workflow: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteWorkflow(@PathVariable Long id) {
        try {
            workflowRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete workflow: " + e.getMessage()));
        }
    }
}
