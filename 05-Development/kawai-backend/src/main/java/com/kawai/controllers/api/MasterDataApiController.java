package com.kawai.controllers.api;

import com.kawai.services.interfaces.MasterDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/api/v1")
@RequiredArgsConstructor
public class MasterDataApiController {

    private final MasterDataService masterDataService;

    @PostMapping("/{entityType}")
    @com.kawai.utils.LogActivity(action = "Tạo mới dữ liệu Master Data", module = "Master Data")
    public ResponseEntity<?> createEntity(@PathVariable String entityType, @RequestBody Map<String, Object> payload) {
        System.out.println("========== CREATE ENTITY API HIT! Type: " + entityType + " ==========");
        try {
            Map<String, Object> result = masterDataService.createEntity(entityType, payload);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "Created successfully", "data", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{entityType}/{id}")
    @com.kawai.utils.LogActivity(action = "Cập nhật dữ liệu Master Data", module = "Master Data")
    public ResponseEntity<?> updateEntity(@PathVariable String entityType, @PathVariable String id,
            @RequestBody Map<String, Object> payload) {
        System.out.println("========== UPDATE ENTITY API HIT! Type: " + entityType + " | ID: " + id + " ==========");
        try {
            Map<String, Object> result = masterDataService.updateEntity(entityType, id, payload);
            return ResponseEntity.ok(Map.of("message", "Updated successfully", "data", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{entityType}/{id}")
    @com.kawai.utils.LogActivity(action = "Xóa dữ liệu Master Data", module = "Master Data")
    public ResponseEntity<?> deleteEntity(@PathVariable String entityType, @PathVariable String id) {
        System.out.println("========== DELETE ENTITY API HIT! Type: " + entityType + " | ID: " + id + " ==========");
        try {
            masterDataService.deleteEntity(entityType, id);
            return ResponseEntity.ok(Map.of("message", "Deleted successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{entityType}/{id}/toggle")
    @com.kawai.utils.LogActivity(action = "Đổi trạng thái Kích hoạt/Vô hiệu hoá", module = "Master Data")
    public ResponseEntity<?> toggleEntityStatus(@PathVariable String entityType, @PathVariable String id, @RequestBody Map<String, Boolean> payload) {
        System.out.println("========== TOGGLE ENTITY API HIT! Type: " + entityType + " | ID: " + id + " ==========");
        try {
            Boolean newStatus = payload.get("status");
            masterDataService.toggleEntityStatus(entityType, id, newStatus);
            return ResponseEntity.ok(Map.of("message", "Toggled successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }
}
