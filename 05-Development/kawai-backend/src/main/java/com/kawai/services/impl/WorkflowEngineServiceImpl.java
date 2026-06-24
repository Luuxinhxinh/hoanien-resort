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
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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



    @Override
    @Transactional
    public void triggerEvent(String eventType, Map<String, Object> payload) {
        System.out.println("========== WORKFLOW ENGINE: Trigger Event " + eventType + " ==========");
        List<Workflow> activeWorkflows = workflowRepository.findByTriggerEventAndIsActive(eventType, true);

        for (Workflow workflow : activeWorkflows) {
            try {
                boolean conditionsMatch = evaluateConditions(workflow.getConditionsJson(), payload, eventType);
                if (conditionsMatch) {
                    executeActions(workflow, payload, eventType);
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
        System.out.println("Executing actions for event type: " + eventType);
        
        // Let's implement the specific logic for each event type as defined in requirements:
        if ("ROOM_REPORT_DAMAGE".equals(eventType)) {
            executeRoomReportDamage(workflow, payload);
        } else if ("PROMOTION_EXCEEDED".equals(eventType)) {
            executePromotionExceeded(payload);
        } else if ("ROOM_CHECKOUT".equals(eventType)) {
            executeRoomCheckout(workflow, payload);
        }

        // We can also parse actions_json and execute dynamic actions if defined:
        String actionsJson = workflow.getActionsJson();
        if (actionsJson != null && !actionsJson.trim().isEmpty()) {
            try {
                List<Map<String, Object>> actions = objectMapper.readValue(actionsJson, new TypeReference<List<Map<String, Object>>>() {});
                for (Map<String, Object> action : actions) {
                    String type = (String) action.get("type");
                    if ("UPDATE_ROOM_STATUS".equals(type)) {
                        String statusValue = (String) action.get("value");
                        Long roomId = safeLong(payload.get("room_id"));
                        if (roomId != null && statusValue != null) {
                            roomRepository.findById(roomId).ifPresent(room -> {
                                room.setRoomStatus(statusValue);
                                roomRepository.save(room);
                                System.out.println("Dynamic Action: Updated Room " + room.getRoomNumber() + " status to " + statusValue);
                            });
                        }
                    } else if ("SEND_EMAIL".equals(type)) {
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
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to execute dynamic actions: " + e.getMessage());
            }
        }
    }

    private void executeRoomReportDamage(Workflow workflow, Map<String, Object> payload) {
        Long roomId = safeLong(payload.get("room_id"));
        Long staffId = safeLong(payload.get("staff_id"));
        String notes = (String) payload.get("notes");

        if (roomId == null) {
            throw new IllegalArgumentException("room_id is required in payload");
        }

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found with ID: " + roomId));
        
        // Action 1: Cập nhật trực tiếp room_status thành 'Maintenance'
        room.setRoomStatus("Maintenance");
        roomRepository.save(room);
        System.out.println("Action 1: Set room " + room.getRoomNumber() + " status to Maintenance");

        // Action 2: Insert một dòng mới vào bảng Hotel_Operations
        Employee staff = null;
        if (staffId != null) {
            staff = employeeRepository.findById(staffId).orElse(null);
        }

        // Đọc cấu hình từ workflow actions
        String priority = "High"; // default
        Employee supervisor = staff; // default
        
        try {
            if (workflow.getActionsJson() != null && !workflow.getActionsJson().trim().isEmpty()) {
                List<Map<String, Object>> actions = objectMapper.readValue(workflow.getActionsJson(), new TypeReference<List<Map<String, Object>>>() {});
                for (Map<String, Object> act : actions) {
                    if ("CREATE_OPERATION_TASK".equals(act.get("type"))) {
                        if (act.containsKey("priority")) {
                            priority = act.get("priority").toString();
                        }
                        if (act.containsKey("supervisor_id")) {
                            Long supId = safeLong(act.get("supervisor_id"));
                            if (supId != null) {
                                supervisor = employeeRepository.findById(supId).orElse(supervisor);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to read workflow actions for priority/supervisor: " + e.getMessage());
        }

        HotelOperation operation = new HotelOperation();
        operation.setRoom(room);
        operation.setStaff(staff);
        operation.setSupervisor(supervisor);
        operation.setOperationalType("Maintenance");
        operation.setPriority(priority);
        operation.setStatus("Pending");
        operation.setCreatedAt(LocalDateTime.now());
        operation.setNotes(notes);

        hotelOperationRepository.save(operation);
        System.out.println("Action 2: Created Hotel Operation maintenance task with priority: " + priority + " and supervisor: " + (supervisor != null ? supervisor.getFullName() : "null"));
    }

    private void executePromotionExceeded(Map<String, Object> payload) {
        Long promoId = safeLong(payload.get("promo_id"));
        Double inputDiscountPct = safeDouble(payload.get("input_discount_pct"));
        Long bookingId = safeLong(payload.get("booking_id"));

        if (promoId == null) {
            throw new IllegalArgumentException("promo_id is required in payload");
        }

        Promotion promotion = promotionRepository.findById(promoId)
                .orElseThrow(() -> new IllegalArgumentException("Promotion not found with ID: " + promoId));

        Integer threshold = promotion.getManagerApprovalThresholdPct();
        if (threshold == null) {
            threshold = 0; // default to 0 if not set
        }

        if (inputDiscountPct != null && inputDiscountPct > threshold) {
            System.out.println("Discount percentage " + inputDiscountPct + "% exceeds threshold of " + threshold + "%");
            
            // 1. Treo trạng thái nghiệp vụ hiện tại sang 'Pending_Approval'
            if (bookingId != null) {
                bookingRepository.findById(bookingId).ifPresent(booking -> {
                    booking.setBookingStatus("Pending_Approval");
                    bookingRepository.save(booking);
                    System.out.println("Suspended Booking ID: " + bookingId + " status to Pending_Approval");
                });
            }

            // 2. Tự động bắn bản ghi phê duyệt tới tài khoản cấp Supervisor/Manager
            // Tạo một Task phê duyệt trong Hotel_Operations để giám sát/phê duyệt
            HotelOperation approvalTask = new HotelOperation();
            approvalTask.setOperationalType("Manager_Approval");
            approvalTask.setPriority("High");
            approvalTask.setStatus("Pending");
            approvalTask.setCreatedAt(LocalDateTime.now());
            
            // Assign supervisor to first employee with a manager/supervisor/admin role
            Employee manager = employeeRepository.findAll().stream()
                    .filter(emp -> emp.getAccount() != null && emp.getAccount().getRole() != null &&
                            (emp.getAccount().getRole().getRoleName().toLowerCase().contains("manager") ||
                             emp.getAccount().getRole().getRoleName().toLowerCase().contains("supervisor") ||
                             emp.getAccount().getRole().getRoleName().toLowerCase().contains("admin")))
                    .findFirst()
                    .orElse(null);

            // Fallback to first available employee if no manager found
            if (manager == null) {
                manager = employeeRepository.findAll().stream().findFirst().orElse(null);
            }

            approvalTask.setStaff(manager);
            approvalTask.setSupervisor(manager);
            
            String notes = "Mã giảm giá " + promotion.getPromoCode() + " áp dụng vượt ngưỡng (" + 
                           inputDiscountPct + "% > " + threshold + "%). Yêu cầu phê duyệt cho booking ID: " + 
                           (bookingId != null ? bookingId : "N/A");
            approvalTask.setNotes(notes);
            
            // If room is present in booking or payload, set it
            if (bookingId != null) {
                // If it's room booking, we can find room and set it
                bookingRepository.findById(bookingId).ifPresent(booking -> {
                    // Let's set room to room repository first room as placeholder or null if not applicable
                    roomRepository.findAll().stream().findFirst().ifPresent(approvalTask::setRoom);
                });
            } else {
                roomRepository.findAll().stream().findFirst().ifPresent(approvalTask::setRoom);
            }

            if (approvalTask.getRoom() != null) {
                hotelOperationRepository.save(approvalTask);
                System.out.println("Created Approval Task in Hotel_Operations for Supervisor: " + (manager != null ? manager.getFullName() : "N/A"));
            }
        }
    }

    private void executeRoomCheckout(Workflow workflow, Map<String, Object> payload) {
        Long roomId = safeLong(payload.get("room_id"));
        if (roomId == null) return;
        
        Room room = roomRepository.findById(roomId).orElse(null);
        if (room == null) return;

        String targetStatus = "Vacant_Dirty"; // default
        String taskType = null;
        
        try {
            if (workflow.getActionsJson() != null && !workflow.getActionsJson().trim().isEmpty()) {
                List<Map<String, Object>> actions = objectMapper.readValue(workflow.getActionsJson(), new TypeReference<List<Map<String, Object>>>() {});
                for (Map<String, Object> act : actions) {
                    String type = (String) act.get("type");
                    if ("UPDATE_ROOM_STATUS".equals(type)) {
                        targetStatus = (String) act.get("value");
                    } else if ("CREATE_OPERATION_TASK".equals(type)) {
                        taskType = (String) act.get("value");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to read workflow actions for room checkout: " + e.getMessage());
        }

        // Action 1: Update Room Status
        room.setRoomStatus(targetStatus);
        room.setCurrentBookingDetailId(null);
        roomRepository.save(room);
        System.out.println("Checkout Workflow: Updated room status to " + targetStatus);

        // Action 2: Create Housekeeping Task
        if (taskType != null) {
            List<Employee> housekeepingStaff = employeeRepository.findAll().stream()
                    .filter(emp -> emp.getAccount() != null && emp.getAccount().getRole() != null &&
                            "Housekeeping".equalsIgnoreCase(emp.getAccount().getRole().getRoleName()))
                    .toList();
            
            Employee assignedStaff = housekeepingStaff.isEmpty() ? null : housekeepingStaff.get(0);
            Employee supervisor = employeeRepository.findAll().stream()
                    .filter(emp -> emp.getAccount() != null && emp.getAccount().getRole() != null &&
                            emp.getAccount().getRole().getRoleName().toLowerCase().contains("supervisor"))
                    .findFirst()
                    .orElse(assignedStaff);

            HotelOperation operation = new HotelOperation();
            operation.setRoom(room);
            operation.setStaff(assignedStaff);
            operation.setSupervisor(supervisor);
            operation.setOperationalType(taskType);
            operation.setPriority("Normal");
            operation.setStatus("Pending");
            operation.setCreatedAt(LocalDateTime.now());
            operation.setNotes("Tự động dọn phòng sau khi check-out (Quy trình tự động).");

            hotelOperationRepository.save(operation);
            System.out.println("Checkout Workflow: Created Housekeeping task for room: " + room.getRoomNumber());
        }
    }

    @Scheduled(cron = "0 */1 * * * *")
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
                            eventPublisher.publishEvent(new com.kawai.events.SystemEmailEvent(this, supervisor.getEmail(), "SLA Warning", "sla-warning", ctx));
                            System.out.println("SLA Warning sent to: " + supervisor.getEmail() + " for task ID: " + op.getId());
                        } catch (Exception e) {
                            System.err.println("Failed to send SLA warning email: " + e.getMessage());
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
