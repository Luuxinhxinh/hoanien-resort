package com.hoanien.controllers.api;

import com.hoanien.models.Room;
import com.hoanien.models.RoomCategory;
import com.hoanien.models.MenuItem;
import com.hoanien.models.Tour;
import com.hoanien.models.Promotion;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/admin/api/v1/audit")
@RequiredArgsConstructor
public class AuditApiController {

    private final EntityManager entityManager;
    private final javax.sql.DataSource dataSource;

    private String getDisplayString(Object obj) {
        if (obj == null) return "";
        if (obj instanceof String || obj instanceof Number || obj instanceof Boolean || obj instanceof Enum || obj instanceof java.time.temporal.Temporal) {
            return obj.toString();
        }
        try {
            if (obj instanceof com.hoanien.models.RoomCategory) return ((com.hoanien.models.RoomCategory) obj).getCategoryName();
            if (obj instanceof com.hoanien.models.Account) return ((com.hoanien.models.Account) obj).getUsername();
            if (obj instanceof com.hoanien.models.Customer) return ((com.hoanien.models.Customer) obj).getFullName();
            if (obj instanceof com.hoanien.models.Employee) return ((com.hoanien.models.Employee) obj).getFullName();
            if (obj instanceof com.hoanien.models.Room) return ((com.hoanien.models.Room) obj).getRoomNumber();
            if (obj instanceof com.hoanien.models.Role) return ((com.hoanien.models.Role) obj).getRoleName();
            
            try {
                java.lang.reflect.Method getId = obj.getClass().getMethod("getId");
                Object id = getId.invoke(obj);
                return obj.getClass().getSimpleName().replaceAll("\\$HibernateProxy.*", "") + " (ID: " + id + ")";
            } catch (Exception e) {}
            
            return obj.getClass().getSimpleName().replaceAll("\\$HibernateProxy.*", "");
        } catch (Throwable e) {
            return "Unreadable Reference";
        }
    }

    private Class<?> getEntityClass(String entityType) {
        if (entityType == null) return null;
        String type = entityType.toLowerCase();
        if (type.endsWith("s")) type = type.substring(0, type.length() - 1); // rough singularization

        switch (type) {
            case "account": return com.hoanien.models.Account.class;
            case "authorized_device": return com.hoanien.models.AuthorizedDevice.class;
            case "employee": return com.hoanien.models.Employee.class;
            case "role": return com.hoanien.models.Role.class;
            case "room_category": case "room-categorie": case "room_categorie": return com.hoanien.models.RoomCategory.class;
            case "room": return com.hoanien.models.Room.class;
            case "hotel_operation": return com.hoanien.models.HotelOperation.class;
            case "hotel_service": return com.hoanien.models.HotelService.class;
            case "menu_item": case "menu-item": return com.hoanien.models.MenuItem.class;
            case "promotion": return com.hoanien.models.Promotion.class;
            case "tour": return com.hoanien.models.Tour.class;
            case "membership-tier": case "membership_tier": return com.hoanien.models.MembershipTier.class;
            case "tour-location": case "tour_location": return com.hoanien.models.TourLocation.class;
            case "tour-price": case "tour_price": return com.hoanien.models.TourPrice.class;
            case "restaurant-table": case "restaurant_table": return com.hoanien.models.RestaurantTable.class;
            case "workflow": return com.hoanien.models.Workflow.class;
            case "food_order": case "order_food": return com.hoanien.models.FoodOrder.class;
            case "food_order_detail": return com.hoanien.models.FoodOrderDetail.class;
            case "booking": return com.hoanien.models.Booking.class;
            case "booking_service": return com.hoanien.models.BookingService.class;
            case "room_booking_detail": return com.hoanien.models.RoomBookingDetail.class;
            case "tour_booking": return com.hoanien.models.TourBooking.class;
            case "refund_request": return com.hoanien.models.RefundRequest.class;
            case "review": return com.hoanien.models.Review.class;
            case "customer": return com.hoanien.models.Customer.class;
            case "consolidated_invoice": case "invoice": return com.hoanien.models.ConsolidatedInvoice.class;
            case "payment_transaction": return com.hoanien.models.PaymentTransaction.class;
            case "folio_item": return com.hoanien.models.FolioItem.class;
            case "room_surcharge": return com.hoanien.models.RoomSurcharge.class;
            case "tour_schedule": return com.hoanien.models.TourSchedule.class;
            case "tour_itinerarie": case "tour_itinerary": return com.hoanien.models.TourItinerary.class;
            case "tour_itinerary_detail": return com.hoanien.models.TourItineraryDetail.class;
            case "tour_staff_assignment": return com.hoanien.models.TourStaffAssignment.class;
            case "tour_attendee": return com.hoanien.models.TourAttendee.class;
            case "daily_rate": return com.hoanien.models.DailyRate.class;
            case "shift": return com.hoanien.models.Shift.class;
            case "staff_schedule": return com.hoanien.models.StaffSchedule.class;
            case "table_reservation": return com.hoanien.models.TableReservation.class;
            case "fnb_daily_report": return com.hoanien.models.FnBDailyReport.class;
            case "run_itinerary_statu": case "run_itinerary_status": return com.hoanien.models.RunItineraryStatus.class;
        }

        // Auto fallback reflection
        String[] parts = entityType.split("_");
        StringBuilder camelCase = new StringBuilder();
        for (String p : parts) {
            if(p.length() > 0) camelCase.append(p.substring(0, 1).toUpperCase()).append(p.substring(1));
        }
        String className = camelCase.toString();
        if (className.endsWith("s")) className = className.substring(0, className.length() - 1);
        try { return Class.forName("com.hoanien.models." + className); } catch (Exception e) {}
        try { return Class.forName("com.hoanien.models." + camelCase.toString()); } catch (Exception e) {}

        return null;
    }

    @GetMapping("/{entityType}/{id}/history")
    public ResponseEntity<?> getEntityHistory(@PathVariable String entityType, @PathVariable Long id) {
        try {
            Class<?> clazz = getEntityClass(entityType);
            if (clazz == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid entity type: " + entityType));
            }

            AuditReader reader = AuditReaderFactory.get(entityManager);
            List<Number> revisions = new ArrayList<>();
            try {
                revisions = reader.getRevisions(clazz, id);
            } catch (Exception e) {
                // If not audited, it will throw NotAuditedException. We catch it and leave revisions empty.
            }

            if (revisions == null || revisions.isEmpty()) {
                Object currentEntity = entityManager.find(clazz, id);
                if (currentEntity != null) {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("revisionNumber", 1);
                    map.put("timestamp", "--");
                    map.put("username", "System (Dữ liệu gốc)");
                    map.put("action", "CREATE");

                    Map<String, Object> changes = new LinkedHashMap<>();
                    for (var field : clazz.getDeclaredFields()) {
                        if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) continue;
                        field.setAccessible(true);
                        String fieldName = field.getName();
                        if (fieldName.equals("currentBookingDetailId") || fieldName.equals("id")) continue;
                        try {
                            Object newVal = field.get(currentEntity);
                            String newStr = getDisplayString(newVal);
                            changes.put(fieldName, Map.of("old", "-", "new", newStr));
                        } catch (Exception ignored) {}
                    }
                    map.put("changes", changes);
                    return ResponseEntity.ok(List.of(map));
                }
                return ResponseEntity.ok(Collections.emptyList());
            }

            List<Map<String, Object>> history = new ArrayList<>();
            
            // Limit to the most recent 5 revisions to improve performance and UI smoothness
            int limit = 5;
            int startIdx = Math.max(0, revisions.size() - limit);

            for (int i = startIdx; i < revisions.size(); i++) {
                Number rev = revisions.get(i);
                Number prevRev = (i > 0) ? revisions.get(i - 1) : null;

                Map<String, Object> map = new LinkedHashMap<>();
                map.put("revisionNumber", rev.intValue());

                try {
                    com.hoanien.models.CustomRevisionEntity customRev = 
                        reader.findRevision(com.hoanien.models.CustomRevisionEntity.class, rev);
                    map.put("timestamp", new java.util.Date(customRev.getTimestamp()).toString());
                    map.put("username", customRev.getUsername() != null ? customRev.getUsername() : "System");
                } catch (Exception e) {
                    map.put("timestamp", "--");
                    map.put("username", "System");
                }

                map.put("action", (i == 0) ? "CREATE" : "UPDATE");
                map.put("changes", Map.of());

                try {
                    Object entity = reader.find(clazz, id, rev);
                    Object prevEntity = prevRev != null ? reader.find(clazz, id, prevRev) : null;

                    Map<String, Object> changes = new LinkedHashMap<>();
                    if (entity != null) {
                        for (var field : clazz.getDeclaredFields()) {
                            if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) continue;
                            field.setAccessible(true);
                            String fieldName = field.getName();
                            if (fieldName.equals("currentBookingDetailId") || fieldName.equals("id")) {
                                continue;
                            }
                            try {
                                Object newVal = field.get(entity);
                                Object oldVal = prevEntity != null ? field.get(prevEntity) : null;

                                String newStr = getDisplayString(newVal);
                                String oldStr = getDisplayString(oldVal);

                                if (!newStr.equals(oldStr)) {
                                    changes.put(fieldName, Map.of("old", oldStr, "new", newStr));
                                }
                            } catch (Exception ignored) {
                            }
                        }
                    }
                    map.put("changes", changes);
                } catch (Exception e) {
                    map.put("changes", Map.of("error", "Could not load snapshot"));
                }

                history.add(map);
            }

            history.sort((a, b) -> ((Integer) b.get("revisionNumber")) - ((Integer) a.get("revisionNumber")));
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    @PostMapping("/{entityType}/{id}/rollback/{revisionId}")
    @Transactional
    @com.hoanien.utils.LogActivity(action = "Phục hồi dữ liệu", module = "Hệ thống Audit")
    public ResponseEntity<?> rollbackEntity(@PathVariable String entityType, @PathVariable Long id,
            @PathVariable Number revisionId) {
        try {
            Class<?> clazz = getEntityClass(entityType);
            if (clazz == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid entity type"));
            }

            AuditReader reader = AuditReaderFactory.get(entityManager);
            Object oldEntity = reader.find(clazz, id, revisionId);

            if (oldEntity == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Revision not found"));
            }

            Object currentEntity = entityManager.find(clazz, id);
            if (currentEntity == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Current entity not found"));
            }

            for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) ||
                    field.isAnnotationPresent(org.hibernate.envers.NotAudited.class) ||
                    field.getName().equals("id")) {
                    continue;
                }
                field.setAccessible(true);
                try {
                    field.set(currentEntity, field.get(oldEntity));
                } catch (Exception ignored) {
                }
            }

            entityManager.flush();
            return ResponseEntity.ok(Map.of("message", "Rollback successful"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Rollback failed: " + e.getMessage()));
        }
    }

    @PostMapping("/backup")
    @com.hoanien.utils.LogActivity(action = "Sao lưu dữ liệu", module = "Hệ thống Audit")
    public ResponseEntity<?> backupDatabase() {
        try {
            java.io.File backupDir = new java.io.File("backups");
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }

            String backupFileName = "Hoanien_Backup_" + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".sql";
            java.io.File backupFile = new java.io.File(backupDir, backupFileName);

            // Execute real database dump using pure JDBC
            try (java.sql.Connection conn = dataSource.getConnection();
                 java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.OutputStreamWriter(new java.io.FileOutputStream(backupFile), java.nio.charset.StandardCharsets.UTF_8))) {
                 
                writer.println("-- Hoanien Retreat Database Backup (Java JDBC Dumper)");
                writer.println("-- Generated automatically at: " + java.time.LocalDateTime.now().toString());
                writer.println("-- ------------------------------------------------------");
                writer.println("SET FOREIGN_KEY_CHECKS=0;\n");
                
                java.sql.DatabaseMetaData metaData = conn.getMetaData();
                try (java.sql.ResultSet tables = metaData.getTables(conn.getCatalog(), null, "%", new String[]{"TABLE"})) {
                    while (tables.next()) {
                        String tableName = tables.getString("TABLE_NAME");
                        writer.println("-- Data for table `" + tableName + "`");
                        
                        try (java.sql.Statement stmt = conn.createStatement();
                             java.sql.ResultSet rs = stmt.executeQuery("SELECT * FROM `" + tableName + "`")) {
                             
                            java.sql.ResultSetMetaData rsmd = rs.getMetaData();
                            int columnCount = rsmd.getColumnCount();
                            
                            while (rs.next()) {
                                StringBuilder sql = new StringBuilder("INSERT INTO `" + tableName + "` VALUES (");
                                for (int i = 1; i <= columnCount; i++) {
                                    Object value = rs.getObject(i);
                                    if (value == null) {
                                        sql.append("NULL");
                                    } else if (value instanceof Number) {
                                        sql.append(value);
                                    } else if (value instanceof Boolean) {
                                        sql.append(((Boolean)value) ? "1" : "0");
                                    } else {
                                        String strVal = value.toString()
                                            .replace("\\", "\\\\")
                                            .replace("'", "\\'")
                                            .replace("\n", "\\n")
                                            .replace("\r", "\\r");
                                        sql.append("'").append(strVal).append("'");
                                    }
                                    if (i < columnCount) sql.append(", ");
                                }
                                sql.append(");\n");
                                writer.print(sql.toString());
                            }
                        } catch (Exception ignored) {
                            // Skip views or inaccessible tables
                        }
                        writer.println("\n");
                    }
                }
                writer.println("SET FOREIGN_KEY_CHECKS=1;");
            }

            String absolutePath = backupFile.getAbsolutePath();
            return ResponseEntity.ok(Map.of(
                "success", true, 
                "message", "Dữ liệu thực tế đã được trích xuất thành công vào file:\n" + absolutePath
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Sao lưu thất bại: " + e.getMessage()));
        }
    }
}