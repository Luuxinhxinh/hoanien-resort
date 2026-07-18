package com.kawai.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kawai.models.*;
import com.kawai.repositories.*;
import com.kawai.services.interfaces.WorkflowEngineService;
import com.kawai.services.interfaces.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.messaging.simp.SimpMessagingTemplate;

@Service
@RequiredArgsConstructor
public class WorkflowEngineServiceImpl implements WorkflowEngineService {

    private final WorkflowRepository workflowRepository;
    private final RoomRepository roomRepository;
    private final EmployeeRepository employeeRepository;
    private final HotelOperationRepository hotelOperationRepository;
    private final PromotionRepository promotionRepository;
    private final BookingRepository bookingRepository;
    private final ObjectMapper objectMapper;
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;
    private final EmailService emailService;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public void triggerEvent(String eventType, Map<String, Object> payload) {
        System.out.println("========== WORKFLOW ENGINE: Trigger Event " + eventType + " ==========");
        System.out.println("[DEBUG WF] Nhận sự kiện: " + eventType + " | Payload: " + payload);
        List<Workflow> activeWorkflows = workflowRepository.findByTriggerEventAndIsActive(eventType, true);
        System.out.println("[DEBUG WF] Tìm thấy " + activeWorkflows.size() + " quy trình hoạt động cho sự kiện " + eventType);

        boolean executed = false;
        for (Workflow workflow : activeWorkflows) {
            try {
                System.out.println("[DEBUG WF] Đang xét quy trình: \"" + workflow.getWorkflowName() + "\" (ID: " + workflow.getId() + ")");
                boolean conditionsMatch = evaluateConditions(workflow.getConditionsJson(), payload, eventType);
                System.out.println("[DEBUG WF] Kết quả so khớp điều kiện: " + conditionsMatch + " (Điều kiện cấu hình: " + workflow.getConditionsJson() + ")");
                if (conditionsMatch) {
                    System.out.println("[DEBUG WF] Thỏa mãn điều kiện! Bắt đầu thực thi các Action...");
                    executeActions(workflow, payload, eventType);
                    executed = true;
                }
            } catch (Exception e) {
                System.err.println("Error processing workflow ID " + workflow.getId() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private boolean evaluateConditions(String conditionsJson, Map<String, Object> payload, String eventType) {
        if (conditionsJson == null || conditionsJson.trim().isEmpty() || "{}".equals(conditionsJson.trim())) {
            return true;
        }

        try {
            Map<String, Object> conditions = objectMapper.readValue(conditionsJson, new TypeReference<Map<String, Object>>() {});
            
            if ("PROMOTION_EXCEEDED".equals(eventType)) {
                // If payload has input_discount_pct, check if it exceeds conditions like threshold_pct_gt
                if (conditions.containsKey("threshold_pct_gt")) {
                    Number thresholdVal = (Number) conditions.get("threshold_pct_gt");
                    Number inputVal = (Number) payload.get("input_discount_pct");
                    if (thresholdVal != null && inputVal != null) {
                        return inputVal.doubleValue() > thresholdVal.doubleValue();
                    }
                }
            }
            // For general check, match any key-value in conditions with payload
            for (Map.Entry<String, Object> entry : conditions.entrySet()) {
                String key = entry.getKey();
                Object expectedValue = entry.getValue();
                
                // Skip threshold comparison already handled
                if ("threshold_pct_gt".equals(key)) {
                    continue;
                }

                // Handle generic _gt logic
                if (key.endsWith("_gt")) {
                    String baseKey = key.replace("_gt", "");
                    Object payloadValue = payload.get(baseKey);
                    if (payloadValue == null) {
                        return false;
                    }
                    try {
                        double expectedVal = Double.parseDouble(expectedValue.toString());
                        double payloadVal = Double.parseDouble(payloadValue.toString());
                        if (payloadVal <= expectedVal) {
                            return false;
                        }
                    } catch (NumberFormatException e) {
                        return false;
                    }
                    continue;
                }

                Object payloadValue = payload.get(key);
                if (payloadValue == null || !payloadValue.toString().equals(expectedValue.toString())) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            System.err.println("Failed to evaluate conditions: " + e.getMessage());
            return false;
        }
    }

    private void executeActions(Workflow workflow, Map<String, Object> payload, String eventType) {
        System.out.println("Executing dynamic actions for event type: " + eventType);
        String actionsJson = workflow.getActionsJson();
        if (actionsJson == null || actionsJson.trim().isEmpty()) {
            return;
        }

        try {
            List<Map<String, Object>> actions = objectMapper.readValue(actionsJson, new TypeReference<List<Map<String, Object>>>() {});
            for (Map<String, Object> action : actions) {
                
                Runnable actionTask = () -> {
                    try {
                        String type = (String) action.get("type");
                        
                        if ("UPDATE_ROOM_STATUS".equals(type)) {
                            String statusValue = (String) action.get("value");
                            Long roomId = safeLong(payload.get("room_id"));
                            if (roomId != null && statusValue != null) {
                                roomRepository.findById(roomId).ifPresent(room -> {
                                    String finalStatus = statusValue;
                                    if ("Vacant_Clean".equalsIgnoreCase(statusValue) || "Vacant_Dirty".equalsIgnoreCase(statusValue)) {
                                        boolean hasActiveMaintenance = hotelOperationRepository.findAll().stream()
                                                .anyMatch(t -> t.getRoom() != null && t.getRoom().getId().equals(room.getId())
                                                        && ("Maintenance".equalsIgnoreCase(t.getOperationalType())
                                                                || "MAINTENANCE".equalsIgnoreCase(t.getOperationalType())
                                                                || "DAMAGE_CHECK".equalsIgnoreCase(t.getOperationalType()))
                                                        && !"Completed".equalsIgnoreCase(t.getStatus()));
                                        if (hasActiveMaintenance) {
                                            finalStatus = "Maintenance";
                                        }
                                    }
                                    room.setRoomStatus(finalStatus);
                                    roomRepository.save(room);
                                    System.out.println("Dynamic Action: Updated Room " + room.getRoomNumber() + " status to " + finalStatus);
                                });
                            }
                        } 
                        else if ("CREATE_OPERATION_TASK".equals(type)) {
                            String taskType = (String) action.get("value");
                            Boolean isEmergency = null;
                            if (payload.containsKey("is_emergency")) {
                                Object val = payload.get("is_emergency");
                                if (val instanceof Boolean) {
                                    isEmergency = (Boolean) val;
                                } else if (val instanceof String) {
                                    isEmergency = Boolean.parseBoolean((String) val);
                                }
                            }
                            // Override for ROOM_REPORT_DAMAGE: force task type to be "DAMAGE_CHECK" unless it's an emergency
                            if ("ROOM_REPORT_DAMAGE".equals(eventType)) {
                                if (isEmergency == null || !isEmergency) {
                                    taskType = "DAMAGE_CHECK";
                                } else {
                                    taskType = "MAINTENANCE";
                                }
                            }
                            String priority = action.containsKey("priority") ? (String) action.get("priority") : "Normal";
                            if (isEmergency != null && isEmergency) {
                                priority = "Urgent";
                            }
                    Long roomId = safeLong(payload.get("room_id"));
                    
                    // Extract assignee and notes from ACTION config (configured by Admin), fallback to payload
                    Long staffId = action.containsKey("assignee_id") ? safeLong(action.get("assignee_id")) : safeLong(payload.get("staff_id"));
                    String notes = action.containsKey("custom_notes") ? (String) action.get("custom_notes") : 
                                  (payload.containsKey("notes") ? (String) payload.get("notes") : "Task created by workflow.");

                    Room room = null;
                    if (roomId != null) {
                        room = roomRepository.findById(roomId).orElse(null);
                    }

                    Employee staff = null;
                    if (staffId != null) {
                        staff = employeeRepository.findById(staffId).orElse(null);
                    }

                    Employee supervisor = staff;
                    if (action.containsKey("supervisor_id")) {
                        Long supId = safeLong(action.get("supervisor_id"));
                        if (supId != null) {
                            supervisor = employeeRepository.findById(supId).orElse(supervisor);
                        }
                    }

                    HotelOperation operation = new HotelOperation();
                    operation.setRoom(room);
                    operation.setStaff(staff);
                    operation.setSupervisor(supervisor);
                    operation.setOperationalType(taskType);
                    operation.setPriority(priority);
                    operation.setStatus("Pending");
                    operation.setCreatedAt(LocalDateTime.now());
                    operation.setNotes(notes);
                    hotelOperationRepository.save(operation);
                    System.out.println("Dynamic Action: Created Task " + taskType + " with priority " + priority + " | staffId=" + (staff != null ? staff.getId() : "[QUEUE]"));

                    // Send WebSocket Notification AFTER transaction commits
                    // Using TransactionSynchronizationManager to avoid sending before DB is committed
                    final String wsMessage;
                    final String wsTopic;
                    
                    String friendlyTaskName = taskType;
                    if ("CHECKOUT_CLEAN".equalsIgnoreCase(taskType)) friendlyTaskName = "Dọn phòng sau Check-out";
                    else if ("F&B_Welcome_Fruit".equalsIgnoreCase(taskType)) friendlyTaskName = "Phục vụ trái cây (Welcome Fruit)";
                    
                    String noteSuffix = (notes != null && !notes.isBlank()) ? " (Ghi chú: " + notes + ")" : "";

                    if (staff != null) {
                        wsMessage = String.format("Nhiệm vụ mới: %s%s", friendlyTaskName, noteSuffix);
                        wsTopic = "/topic/operations/" + staff.getId();
                    } else {
                        wsMessage = String.format("Nhiệm vụ chung: %s%s", friendlyTaskName, noteSuffix);
                        // Phân luồng nhóm trách nhiệm để tránh rác kênh chung của Lễ tân
                        if (taskType != null && taskType.contains("CLEAN")) {
                            wsTopic = "/topic/operations/housekeeping";
                        } else if (taskType != null && taskType.contains("MAINTENANCE")) {
                            wsTopic = "/topic/operations/maintenance";
                        } else {
                            wsTopic = "/topic/operations"; // Các task khác (F&B...) vẫn gửi chung
                        }
                    }
                    final Map<String, Object> wsPayload = Map.of("message", wsMessage, "type", "NEW_TASK");

                    if (TransactionSynchronizationManager.isActualTransactionActive()) {
                        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                            @Override
                            public void afterCommit() {
                                System.out.println("[WS] Sending notification to " + wsTopic + " after TX commit");
                                messagingTemplate.convertAndSend(wsTopic, wsPayload);
                            }
                        });
                    } else {
                        // No active transaction (e.g. delayed async execution) — send directly
                        System.out.println("[WS] Sending notification to " + wsTopic + " directly (no TX)");
                        messagingTemplate.convertAndSend(wsTopic, wsPayload);
                    }
                }
                else if ("SEND_EMAIL".equals(type)) {
                    String sender = (String) action.get("sender_email");
                    String target = (String) action.get("target_email");
                    String subject = (String) action.get("email_subject");
                    String bodyHtml = (String) action.get("email_body_html");
                    
                    if (target != null && subject != null && bodyHtml != null) {
                        // Basic payload replacement for placeholders like {{email}}
                        for(Map.Entry<String, Object> entry : payload.entrySet()) {
                            if (entry.getValue() != null) {
                                String placeholder = "{{" + entry.getKey() + "}}";
                                String val = entry.getValue().toString();
                                target = target.replace(placeholder, val);
                                subject = subject.replace(placeholder, val);
                                bodyHtml = bodyHtml.replace(placeholder, val);
                            }
                        }
                        Map<String, Object> ctx = new java.util.HashMap<>();
                        ctx.put("fromEmail", sender);
                        ctx.put("htmlContent", bodyHtml);
                        eventPublisher.publishEvent(new com.kawai.events.SystemEmailEvent(this, target, subject, "custom-workflow", ctx));
                        System.out.println("Dynamic Action: Triggered SystemEmailEvent to " + target);
                    }
                }
                else if ("REQUIRE_MANAGER_APPROVAL".equals(type)) {
                    Long bookingId = safeLong(payload.get("booking_id"));
                    if (bookingId != null) {
                        bookingRepository.findById(bookingId).ifPresent(booking -> {
                            booking.setBookingStatus("Pending_Approval");
                            bookingRepository.save(booking);
                            System.out.println("Dynamic Action: Suspended Booking ID: " + bookingId + " status to Pending_Approval");
                        });
                    }
                    
                    HotelOperation approvalTask = new HotelOperation();
                    approvalTask.setOperationalType("Manager_Approval");
                    approvalTask.setPriority("High");
                    approvalTask.setStatus("Pending");
                    approvalTask.setCreatedAt(LocalDateTime.now());
                    
                    Employee manager = employeeRepository.findAll().stream()
                            .filter(emp -> emp.getAccount() != null && emp.getAccount().getRole() != null &&
                                    (emp.getAccount().getRole().getRoleName().toLowerCase().contains("manager") ||
                                     emp.getAccount().getRole().getRoleName().toLowerCase().contains("supervisor") ||
                                     emp.getAccount().getRole().getRoleName().toLowerCase().contains("admin")))
                            .findFirst()
                            .orElse(null);

                    if (manager == null) {
                        manager = employeeRepository.findAll().stream().findFirst().orElse(null);
                    }

                    approvalTask.setStaff(manager);
                    approvalTask.setSupervisor(manager);
                    approvalTask.setNotes("Yêu cầu phê duyệt tự động từ hệ thống. Booking ID: " + (bookingId != null ? bookingId : "N/A"));
                    hotelOperationRepository.save(approvalTask);
                    System.out.println("Dynamic Action: Created Manager_Approval Task");
                    }
                } catch (Exception e) {
                    System.err.println("Error executing dynamic action inside Runnable: " + e.getMessage());
                }
            }; // End of Runnable

            // Check for delay_minutes
                if (action.containsKey("delay_minutes")) {
                    try {
                        long delayMinutes = Long.parseLong(action.get("delay_minutes").toString());
                        if (delayMinutes > 0) {
                            System.out.println("Dynamic Action: Scheduling action " + action.get("type") + " to run after " + delayMinutes + " minutes");
                            java.util.concurrent.CompletableFuture.runAsync(actionTask, 
                                java.util.concurrent.CompletableFuture.delayedExecutor(delayMinutes, java.util.concurrent.TimeUnit.MINUTES));
                            continue; // Skip immediate execution
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to parse delay_minutes, executing immediately: " + e.getMessage());
                    }
                }
                
                // Execute immediately
                actionTask.run();
            }
        } catch (Exception e) {
            System.err.println("Failed to execute dynamic actions: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void scanSlaEscalations() {
        System.out.println("========== WORKFLOW ENGINE: Scanning SLA Escalations ==========");
        List<Workflow> activeWorkflows = workflowRepository.findByTriggerEventAndIsActive("SLA_ESCALATE", true);
        if (activeWorkflows.isEmpty()) {
            return;
        }

        for (Workflow workflow : activeWorkflows) {
            int maxPendingMinutes = 30; // default
            try {
                if (workflow.getConditionsJson() != null && !workflow.getConditionsJson().trim().isEmpty()) {
                    Map<String, Object> conds = objectMapper.readValue(workflow.getConditionsJson(), new TypeReference<Map<String, Object>>() {});
                    if (conds.containsKey("max_pending_minutes")) {
                        maxPendingMinutes = Integer.parseInt(conds.get("max_pending_minutes").toString());
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to read SLA config: " + e.getMessage());
            }

            List<HotelOperation> pendingOperations = hotelOperationRepository.findByStatus("Pending");
            LocalDateTime limitTime = LocalDateTime.now().minusMinutes(maxPendingMinutes);

            for (HotelOperation op : pendingOperations) {
                if (op.getIsEscalated() != null && op.getIsEscalated()) {
                    continue; // Skip already escalated tasks
                }
                if (op.getCreatedAt() != null && op.getCreatedAt().isBefore(limitTime)) {
                    Employee supervisor = op.getSupervisor();
                    if (supervisor != null && supervisor.getEmail() != null && !supervisor.getEmail().isBlank()) {
                        try {
                            String taskName = op.getOperationalType() + " Task (ID: " + op.getId() + ")";
                            String roomNum = op.getRoom() != null ? op.getRoom().getRoomNumber() : "N/A";
                            Map<String, Object> ctx = new java.util.HashMap<>();
                            ctx.put("taskName", taskName);
                            ctx.put("pendingMinutes", maxPendingMinutes);
                            ctx.put("roomNumber", roomNum);
                            ctx.put("email", supervisor.getEmail());
                            
                            // Let the WorkflowEngine trigger the action dynamically!
                            triggerEvent("SLA_ESCALATE", ctx);
                            
                            // Mark as escalated and save to database
                            op.setIsEscalated(true);
                            hotelOperationRepository.save(op);
                            System.out.println("SLA Escalated for Task ID: " + op.getId() + " - Email alert sent to " + supervisor.getEmail());
                        } catch (Exception e) {
                            System.err.println("Failed to trigger SLA escalation event: " + e.getMessage());
                        }
                    }
                }
            }
        }
    }

    private Long safeLong(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        try {
            return Long.parseLong(obj.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double safeDouble(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        }
        try {
            return Double.parseDouble(obj.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
