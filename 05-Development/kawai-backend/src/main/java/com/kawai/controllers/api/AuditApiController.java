package com.kawai.controllers.api;

import com.kawai.models.Room;
import com.kawai.models.RoomCategory;
import com.kawai.models.MenuItem;
import com.kawai.models.Tour;
import com.kawai.models.Promotion;
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

    private Class<?> getEntityClass(String entityType) {
        switch (entityType) {
            case "rooms":
                return Room.class;
            case "room-categories":
                return RoomCategory.class;
            case "menu-items":
                return MenuItem.class;
            case "tours":
                return Tour.class;
            case "promotions":
                return Promotion.class;
            default:
                return null;
        }
    }

    @GetMapping("/{entityType}/{id}/history")
    public ResponseEntity<?> getEntityHistory(@PathVariable String entityType, @PathVariable Long id) {
        try {
            Class<?> clazz = getEntityClass(entityType);
            if (clazz == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid entity type: " + entityType));
            }

            AuditReader reader = AuditReaderFactory.get(entityManager);
            List<Number> revisions = reader.getRevisions(clazz, id);

            if (revisions == null || revisions.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            }

            List<Map<String, Object>> history = new ArrayList<>();

            for (int i = 0; i < revisions.size(); i++) {
                Number rev = revisions.get(i);
                Number prevRev = (i > 0) ? revisions.get(i - 1) : null;

                Map<String, Object> map = new LinkedHashMap<>();
                map.put("revisionNumber", rev.intValue());

                try {
                    com.kawai.models.CustomRevisionEntity customRev = 
                        reader.findRevision(com.kawai.models.CustomRevisionEntity.class, rev);
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
                            field.setAccessible(true);
                            String fieldName = field.getName();
                            if (fieldName.equals("currentBookingDetailId") || fieldName.equals("id")) {
                                continue;
                            }
                            try {
                                Object newVal = field.get(entity);
                                Object oldVal = prevEntity != null ? field.get(prevEntity) : null;

                                String newStr = "";
                                if (newVal != null) {
                                    if (newVal instanceof com.kawai.models.RoomCategory) {
                                        newStr = ((com.kawai.models.RoomCategory) newVal).getCategoryName();
                                    } else {
                                        newStr = newVal.toString();
                                    }
                                }

                                String oldStr = "";
                                if (oldVal != null) {
                                    if (oldVal instanceof com.kawai.models.RoomCategory) {
                                        oldStr = ((com.kawai.models.RoomCategory) oldVal).getCategoryName();
                                    } else {
                                        oldStr = oldVal.toString();
                                    }
                                }

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
    @com.kawai.utils.LogActivity(action = "Phục hồi dữ liệu", module = "Hệ thống Audit")
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
    @com.kawai.utils.LogActivity(action = "Sao lưu dữ liệu", module = "Hệ thống Audit")
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