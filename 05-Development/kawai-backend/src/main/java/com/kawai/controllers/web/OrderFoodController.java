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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;
import java.security.Principal;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class OrderFoodController {

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

        // 1. Đổ thực đơn món ăn và trích xuất danh mục động ra thanh Tabs
        List<MenuItem> allItems = foodItemRepository.findAll();
        model.addAttribute("menuItems", allItems);

        // Gom nhóm các chuỗi danh mục không trùng lặp từ bảng Menu_Items
        List<String> categories = allItems.stream()
                .map(MenuItem::getCategory)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        model.addAttribute("categories", categories);

        // ── KHÁCH CHƯA ĐĂNG NHẬP VẪN XEM ĐƯỢC THỰC ĐƠN ──
        if (principal == null) {
            model.addAttribute("isLoggedIn", false);
            model.addAttribute("currentRoom", null);
            model.addAttribute("currentTable", null);
            model.addAttribute("currentCreditLimit", null);
            return "guest/order-food";
        }

        // ── XỬ LÝ KHI ĐÃ ĐĂNG NHẬP THÀNH CÔNG ──
        model.addAttribute("isLoggedIn", true);
        model.addAttribute("currentCreditLimit", null);

        // Đồng bộ dữ liệu User Account vào Session nếu chưa tồn tại
        Account userAccount = (Account) session.getAttribute("user");
        if (userAccount == null) {
            userAccount = accountRepository.findByUsername(principal.getName()).orElse(null);
            if (userAccount != null) {
                session.setAttribute("user", userAccount);
            }
        }

        // 2. Định danh ID khách hàng để quét Option 1 (Phòng) hoặc Option 2 (Bàn)
        if (userAccount != null) {
            Long accountId = userAccount.getId();

            // Quét tìm thông tin phòng
            Room currentRoom = roomRepository.findActiveRoomByUserId(accountId).orElse(null);
            RestaurantTable currentTable = restaurantTableRepository.findTodayTableByUserId(accountId).orElse(null);

            model.addAttribute("currentRoom", currentRoom);
            model.addAttribute("currentTable", currentTable);

            // Lấy credit limit từ RoomBooking (nếu có phòng)
            if (currentRoom != null && currentRoom.getCurrentBookingDetailId() != null) {
                Optional<RoomBookingDetail> detailOpt = roomBookingDetailRepository
                        .findById(currentRoom.getCurrentBookingDetailId());
                if (detailOpt.isPresent()) {
                    RoomBooking roomBooking = detailOpt.get().getRoomBooking();
                    if (roomBooking != null && roomBooking.getCreditLimit() != null) {
                        model.addAttribute("currentCreditLimit", roomBooking.getCreditLimit());
                    }
                }
            }
        } else {
            model.addAttribute("currentRoom", null);
            model.addAttribute("currentTable", null);
        }

        return "guest/order-food";
    }
}
