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

    @PostMapping("/{id}/toggle")
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
