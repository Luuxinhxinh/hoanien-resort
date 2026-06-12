package com.kawai.controllers.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/account")
public class AccountController {

    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<?> toggleStatus(@PathVariable String id,
                                          @RequestBody Map<String, Boolean> body) {

        Boolean active = body.get("active");

        System.out.println("Toggle account " + id + " -> " + active);

      return ResponseEntity.ok(Map.of(
                "id", id,
                "active", active
        ));
    }
}
