package com.kawai.controllers.api;

import com.kawai.models.MenuItem;
import com.kawai.repositories.FoodItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.Map;

@RestController
@RequestMapping("/api/menu-items")
public class MenuItemApiController {

    @Autowired
    private FoodItemRepository foodItemRepository;

    @GetMapping
    public ResponseEntity<?> getMenuItems(@RequestParam(required = false) String day) {
        // TÍNH NĂNG CHIA THỰC ĐƠN THEO NGÀY: 
        // API phục vụ cho Frontend lấy dữ liệu thực đơn theo từng ngày.
        java.time.DayOfWeek targetDay = java.time.LocalDate.now().getDayOfWeek();
        if (day != null && !day.isEmpty()) {
            try {
                targetDay = java.time.DayOfWeek.valueOf(day.toUpperCase());
            } catch (Exception e) {
                // Nếu FE truyền sai định dạng, fallback về ngày hôm nay
            }
        }
        java.util.List<MenuItem> items = foodItemRepository.findAvailableByDayOfWeek(targetDay);
        return ResponseEntity.ok(items);
    }

    @PostMapping("/{id}/toggle")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('OP_FNB_ORDER', 'ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<?> toggleAvailability(@PathVariable Long id, @RequestParam Boolean isAvailable) {
        Optional<MenuItem> itemOpt = foodItemRepository.findById(id);
        if (itemOpt.isPresent()) {
            MenuItem item = itemOpt.get();
            item.setIsAvailable(isAvailable);
            foodItemRepository.save(item);
            return ResponseEntity.ok(Map.of("status", "success", "id", id, "isAvailable", isAvailable));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
