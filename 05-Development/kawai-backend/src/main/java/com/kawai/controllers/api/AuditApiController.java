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

            entityManager.merge(currentEntity);
            entityManager.flush();
            return ResponseEntity.ok(Map.of("message", "Rollback successful"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Rollback failed: " + e.getMessage()));
        }
    }
}