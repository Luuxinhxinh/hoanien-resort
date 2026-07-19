package com.kawai.controllers.web;

import com.kawai.dto.roomchange.ChangeRoomCategoryRequest;
import com.kawai.exceptions.BusinessException;
import com.kawai.models.RoomBookingDetail;
import com.kawai.models.RoomCategory;
import com.kawai.repositories.AccountRepository;
import com.kawai.repositories.RoomBookingDetailRepository;
import com.kawai.repositories.RoomCategoryRepository;
import com.kawai.repositories.RoomRepository;
import com.kawai.services.interfaces.ChangeRoomCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller xử lý toàn bộ chức năng Đổi hạng phòng (UC44 - Change Room
 * Category)
 * cho lễ tân khi khách đang lưu trú (In-House).
 *
 * URL Prefix: /receptionist/in-house
 */
@Controller
@RequestMapping("/receptionist/in-house")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('OP_BOOKING', 'ROLE_ADMIN', 'ROLE_MANAGER', 'OP_RECEPTION_INHOUSE')")
public class ChangeRoomController {

    private final ChangeRoomCategoryService changeRoomCategoryService;
    private final RoomBookingDetailRepository roomBookingDetailRepository;
    private final RoomCategoryRepository roomCategoryRepository;
    private final RoomRepository roomRepository;
    private final AccountRepository accountRepository;

    // ----------------------------------------------------------------
    // API: Danh sách hạng phòng + số phòng trống + chênh lệch giá
    // GET /receptionist/in-house/categories-available?excludeDetailId={id}
    // ----------------------------------------------------------------
    @GetMapping("/categories-available")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getAvailableCategories(
            @RequestParam(required = false) Long excludeDetailId) {
        try {
            String currentCategoryName = null;
            BigDecimal currentPrice = null;

            if (excludeDetailId != null) {
                RoomBookingDetail detail = roomBookingDetailRepository.findById(excludeDetailId).orElse(null);
                if (detail != null && detail.getCategory() != null) {
                    currentCategoryName = detail.getCategory().getCategoryName();
                    currentPrice = detail.getCategory().getBasePrice();
                }
            }

            final String finalCurrentCategoryName = currentCategoryName;
            final BigDecimal finalCurrentPrice = currentPrice;

            List<RoomCategory> allCategories = roomCategoryRepository.findAll();
            List<Map<String, Object>> result = new ArrayList<>();

            for (RoomCategory cat : allCategories) {
                long vacantCount = 0;
                if (cat.getCategoryName() != null) {
                    vacantCount = roomRepository.countVacantCleanRoomsByCategoryName(cat.getCategoryName());
                }

                Map<String, Object> catMap = new HashMap<>();
                catMap.put("categoryName", cat.getCategoryName() != null ? cat.getCategoryName() : "Unknown");
                catMap.put("basePrice", cat.getBasePrice());
                catMap.put("vacantCount", vacantCount);

                boolean isCurrent = false;
                if (cat.getCategoryName() != null && finalCurrentCategoryName != null) {
                    isCurrent = cat.getCategoryName().equals(finalCurrentCategoryName);
                }
                catMap.put("isCurrent", isCurrent);

                if (finalCurrentPrice != null && cat.getBasePrice() != null) {
                    catMap.put("priceDiff", cat.getBasePrice().subtract(finalCurrentPrice));
                } else {
                    catMap.put("priceDiff", BigDecimal.ZERO);
                }

                result.add(catMap);
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("error", e.getMessage());
            errorMap.put("trace", java.util.Arrays.toString(e.getStackTrace()));
            return ResponseEntity.status(500).body(Collections.singletonList(errorMap));
        }
    }

    // ----------------------------------------------------------------
    // API: Danh sách phòng Vacant_Clean theo hạng
    // GET /receptionist/in-house/rooms-by-category?categoryName={name}
    // ----------------------------------------------------------------
    @GetMapping("/rooms-by-category")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getRoomsByCategory(
            @RequestParam String categoryName) {
        try {
            List<Map<String, Object>> rooms = roomRepository.findByCategoryName(categoryName).stream()
                    .filter(r -> "Vacant_Clean".equalsIgnoreCase(r.getRoomStatus()))
                    .map(r -> {
                        Map<String, Object> rm = new HashMap<>();
                        rm.put("id", r.getId());
                        rm.put("roomNumber", r.getRoomNumber());
                        return rm;
                    })
                    .collect(Collectors.toList());
            return ResponseEntity.ok(rooms);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    // ----------------------------------------------------------------
    // ACTION: Thực hiện đổi phòng
    // POST /receptionist/in-house/transfer-room
    // ----------------------------------------------------------------
    @PostMapping("/transfer-room")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> transferRoom(
            @RequestParam("bookingDetailId") Long bookingDetailId,
            @RequestParam("newRoomId") Long newRoomId,
            Principal principal) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long accountId = 1L; // Fallback
            if (principal != null) {
                accountId = accountRepository.findByUsername(principal.getName())
                        .map(acc -> acc.getId())
                        .orElse(1L);
            }

            ChangeRoomCategoryRequest request = new ChangeRoomCategoryRequest();
            request.setBookingDetailId(bookingDetailId);
            request.setSelectedRoomId(newRoomId);
            request.setReceptionistAccountId(accountId);

            changeRoomCategoryService.changeCategory(request);

            response.put("success", true);
            response.put("message", "Đổi phòng thành công!");
            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
