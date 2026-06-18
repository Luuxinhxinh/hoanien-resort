package com.kawai.controllers.web;

import com.kawai.models.Account;
import com.kawai.models.MenuItem;
import com.kawai.models.RestaurantTable;
import com.kawai.models.Room;
import com.kawai.models.RoomBooking;
import com.kawai.models.RoomBookingDetail;
import com.kawai.repositories.AccountRepository;
import com.kawai.repositories.FoodItemRepository;
import com.kawai.repositories.RestaurantTableRepository;
import com.kawai.repositories.RoomRepository;
import com.kawai.repositories.RoomBookingDetailRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;
import java.security.Principal;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class OrderFoodController {

    private static final Logger log = LoggerFactory.getLogger(OrderFoodController.class);

    @Autowired
    private FoodItemRepository foodItemRepository;

    @Autowired
    private RestaurantTableRepository restaurantTableRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RoomBookingDetailRepository roomBookingDetailRepository;

    @GetMapping("/order-food")
    public String showOrderFoodPage(Principal principal, Model model, HttpSession session) {

        List<MenuItem> allItems = Collections.emptyList();
        List<String> categories = Collections.emptyList();
        Room currentRoom = null;
        RestaurantTable currentTable = null;
        BigDecimal currentCreditLimit = null;
        boolean isLoggedIn = (principal != null);

        // 1. Tải thực đơn (Áp dụng chung cho cả Guest và Member)
        try {
            allItems = foodItemRepository.findAll().stream()
                    .filter(item -> Boolean.TRUE.equals(item.getIsAvailable()))
                    .collect(Collectors.toList());
            categories = allItems.stream()
                    .map(MenuItem::getCategory)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Lỗi khi tải Menu hoặc lọc danh mục: {}", e.getMessage(), e);
        }

        model.addAttribute("menuItems", allItems);
        model.addAttribute("categories", categories);

        // ── XỬ LÝ KHI KHÁCH CHƯA ĐĂNG NHẬP ──
        if (!isLoggedIn) {
            model.addAttribute("isLoggedIn", false);
            model.addAttribute("currentRoom", null);
            model.addAttribute("currentTable", null);
            model.addAttribute("currentCreditLimit", null);
            session.removeAttribute("user"); // Đảm bảo clear session cũ nếu có
            return "guest/order-food";
        }

        // ── XỬ LÝ KHI ĐÃ ĐĂNG NHẬP THÀNH CÔNG ──
        model.addAttribute("isLoggedIn", true);

        try {
            Account userAccount = (Account) session.getAttribute("user");

            if (userAccount == null) {
                String identifier = null;

                // Trích xuất chuỗi định danh (Username hoặc Email từ nhà cung cấp)
                if (principal instanceof OAuth2AuthenticationToken) {
                    OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) principal;
                    identifier = oauthToken.getPrincipal().getAttribute("email");
                    log.info("Khách hàng đăng nhập qua Google OAuth2 với định danh: {}", identifier);
                } else {
                    identifier = principal.getName();
                    log.info("Khách hàng đăng nhập qua Form với Username: {}", identifier);
                }

                if (identifier != null) {
                    // ĐỒNG BỘ THEO DB: Chỉ quét theo findByUsername vì tài khoản Google
                    // cũng lưu email trực tiếp tại cột username này.
                    userAccount = accountRepository.findByUsername(identifier).orElse(null);

                    if (userAccount != null) {
                        session.setAttribute("user", userAccount);
                    } else {
                        log.warn("Không tìm thấy bản ghi Account trong DB cho mã định danh: {}", identifier);
                    }
                }
            }

            // 2. Quét tìm Option vị trí (Phòng / Bàn) khi Account đã tồn tại hợp lệ
            if (userAccount != null) {
                Long accountId = userAccount.getId();
                log.info(">>> Xử lý cho userAccount: id={}, username={}", accountId, userAccount.getUsername());

                // Quét tìm thông tin phòng đang thuê
                try {
                    currentRoom = roomRepository.findActiveRoomByUserId(accountId).orElse(null);
                    log.info(">>> findActiveRoomByUserId({}) = {}", accountId,
                            currentRoom != null ? currentRoom.getRoomNumber() : "NULL");
                } catch (Exception e) {
                    log.warn("Lỗi khi truy vấn phòng cho userId={}: {}", accountId, e.getMessage(), e);
                }

                // Quét tìm thông tin bàn đặt tiệc hôm nay
                try {
                    currentTable = restaurantTableRepository.findTodayTableByUserId(accountId).orElse(null);
                } catch (Exception e) {
                    log.warn("Lỗi khi truy vấn bàn cho userId={}: {}", accountId, e.getMessage());
                }

                // Tính toán hạn mức ký bill gửi về phòng
                if (currentRoom != null && currentRoom.getCurrentBookingDetailId() != null) {
                    try {
                        Optional<RoomBookingDetail> detailOpt = roomBookingDetailRepository
                                .findById(currentRoom.getCurrentBookingDetailId());
                        if (detailOpt.isPresent()) {
                            RoomBooking roomBooking = detailOpt.get().getRoomBooking();
                            if (roomBooking != null && roomBooking.getCreditLimit() != null) {
                                currentCreditLimit = roomBooking.getCreditLimit();
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Lỗi khi truy vấn credit limit: {}", e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Lỗi hệ thống trong luồng xử lý định danh dữ liệu POS: {}", e.getMessage(), e);
        }

        // Trả các thực thể dữ liệu về cho view Thymeleaf kết xuất
        model.addAttribute("currentRoom", currentRoom);
        model.addAttribute("currentTable", currentTable);
        model.addAttribute("currentCreditLimit", currentCreditLimit);

        return "guest/order-food";
    }
}