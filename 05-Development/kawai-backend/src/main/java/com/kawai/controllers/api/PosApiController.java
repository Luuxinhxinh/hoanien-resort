package com.kawai.controllers.api;

import com.kawai.dto.CreateFoodOrderRequest;
import com.kawai.models.FoodOrder;
import com.kawai.services.interfaces.PosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/pos")
@org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('OP_FNB_ORDER', 'ROLE_ADMIN', 'ROLE_MANAGER')")
public class PosApiController {

    @Autowired
    private PosService posService;

    @PostMapping("/orders")
    public ResponseEntity<?> createOrder(@RequestBody CreateFoodOrderRequest request,
            java.security.Principal principal) {
        try {
            String identifier = null;
            if (principal != null) {
                identifier = principal.getName();
                if (principal instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) {
                    org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken oauthToken = (org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) principal;
                    identifier = oauthToken.getPrincipal().getAttribute("email");
                }
            }

            FoodOrder savedOrder = posService.createOrder(request, identifier);

            return ResponseEntity.ok().body(Map.of("status", "success", "orderId", savedOrder.getId()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(400).body(Map.of(
                    "error", e.getClass().getName(),
                    "message", e.getMessage() != null ? e.getMessage() : "null message"));
        }
    }

    @PostMapping("/orders/{id}/pay")
    public ResponseEntity<?> payOrder(@PathVariable Long id) {
        try {
            posService.payOrder(id);
            return ResponseEntity.ok().body(Map.of("status", "success", "message", "Thanh toán thành công"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(400).body(Map.of(
                    "error", e.getClass().getName(),
                    "message", e.getMessage() != null ? e.getMessage() : "null message"));
        }
    }

    @PostMapping("/orders/{id}/add-items")
    public ResponseEntity<?> addItemsToOrder(@PathVariable Long id,
            @RequestBody java.util.List<com.kawai.dto.CartItemDto> items) {
        try {
            posService.addItemsToOrder(id, items);
            return ResponseEntity.ok()
                    .body(Map.of("status", "success", "message", "Đã thêm món vào đơn hàng thành công"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(400).body(Map.of(
                    "error", e.getClass().getName(),
                    "message", e.getMessage() != null ? e.getMessage() : "null message"));
        }
    }

    @PutMapping("/orders/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            posService.updateOrderStatus(id, status);
            return ResponseEntity.ok().body(Map.of("status", "success", "message", "Cập nhật trạng thái thành công"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(400).body(Map.of(
                    "error", e.getClass().getName(),
                    "message", e.getMessage() != null ? e.getMessage() : "null message"));
        }
    }

    @PostMapping("/batch-update-status")
    public ResponseEntity<?> batchUpdateStatus(@RequestBody Map<String, Object> payload) {
        try {
            java.util.List<String> orderIds = (java.util.List<String>) payload.get("orderIds");
            String newStatus = (String) payload.get("newStatus");
            for (String idStr : orderIds) {
                posService.updateOrderStatus(Long.parseLong(idStr), newStatus);
            }
            return ResponseEntity.ok().body(Map.of("status", "success", "message", "Cập nhật hàng loạt thành công"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(400).body(Map.of(
                    "error", e.getClass().getName(),
                    "message", e.getMessage() != null ? e.getMessage() : "null message"));
        }
    }
    @PostMapping("/orders/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id, @RequestBody(required = false) com.kawai.dtos.CancelOrderRequestDTO dto) {
        try {
            posService.cancelOrder(id, dto);
            return ResponseEntity.ok().body(Map.of("status", "success", "message", "Cập nhật trạng thái thành công"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(400).body(Map.of(
                    "error", e.getClass().getName(),
                    "message", e.getMessage() != null ? e.getMessage() : "null message"));
        }
    }
}
