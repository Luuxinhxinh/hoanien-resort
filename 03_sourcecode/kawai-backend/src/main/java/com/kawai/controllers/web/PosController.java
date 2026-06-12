package com.kawai.controllers.web;

import com.kawai.models.FoodOrder;
import com.kawai.models.MenuItem;
import com.kawai.models.RestaurantTable;
import com.kawai.repositories.FoodItemRepository;
import com.kawai.repositories.FoodOrderRepository;
import com.kawai.repositories.RestaurantTableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/fbStaff")
public class PosController {

    @Autowired
    private FoodOrderRepository foodOrderRepository;

    @Autowired
    private RestaurantTableRepository restaurantTableRepository;

    @Autowired
    private FoodItemRepository foodItemRepository;

    @GetMapping({"/dashboard", ""})
    public String dashboard(Model model) {
        List<FoodOrder> foodOrders = foodOrderRepository.findAll();
        List<RestaurantTable> tables = restaurantTableRepository.findAll();

        long totalTables = tables.size();
        long occupiedTables = tables.stream().filter(t -> "Occupied".equalsIgnoreCase(t.getTableStatus())).count();
        long activeOrders = foodOrders.stream().filter(o -> "Pending".equalsIgnoreCase(o.getOrderStatus()) || "Preparing".equalsIgnoreCase(o.getOrderStatus()) || "Ready".equalsIgnoreCase(o.getOrderStatus())).count();
        long pendingRoomServices = foodOrders.stream().filter(o -> "Room Service".equalsIgnoreCase(o.getOrderType()) && "Pending".equalsIgnoreCase(o.getOrderStatus())).count();

        model.addAttribute("foodOrders", foodOrders);
        model.addAttribute("tables", tables);
        model.addAttribute("totalTables", totalTables);
        model.addAttribute("occupiedTables", occupiedTables);
        model.addAttribute("activeOrders", activeOrders);
        model.addAttribute("pendingRoomServices", pendingRoomServices);

        return "f&bStaff/pos-dashboard";
    }

    @GetMapping("/create-food-order")
    public String createFoodOrder() {
        return "f&bStaff/create-food-order";
    }

    @GetMapping("/table-management")
    public String tableManagement(Model model) {
        List<RestaurantTable> tables = restaurantTableRepository.findAll();
        model.addAttribute("tables", tables);
        return "f&bStaff/table-management";
    }

    @GetMapping("/room-service")
    public String roomService() {
        return "f&bStaff/room-service-management";
    }

    @GetMapping("/room-service-detail")
    public String roomServiceDetail() {
        return "f&bStaff/room-service-detail";
    }

    @GetMapping("/emenu")
    public String emenu(Model model) {
        List<MenuItem> rawItems = foodItemRepository.findAll();
        List<Map<String, Object>> mappedItems = new ArrayList<>();

        for (MenuItem item : rawItems) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", item.getId());
            map.put("name", item.getItemName());
            map.put("desc", item.getDescription() != null ? item.getDescription() : "");
            map.put("price", item.getPrice());
            map.put("catLabel", item.getCategory());

            // Determine category slug
            String cat = "starter";
            String category = item.getCategory() != null ? item.getCategory().toLowerCase() : "";
            if (category.contains("khai vị")) {
                cat = "starter";
            } else if (category.contains("chính")) {
                cat = "main";
            } else if (category.contains("tráng miệng")) {
                cat = "dessert";
            } else if (category.contains("uống")) {
                cat = "drink";
            } else if (category.contains("salad")) {
                cat = "salad";
            } else {
                cat = category;
            }
            map.put("cat", cat);

            map.put("status", item.getIsAvailable() ? "available" : "out-of-stock");

            // Set UI backgrounds and icons based on category
            if ("starter".equals(cat)) {
                map.put("bgFrom", "#fff4eb");
                map.put("bgTo", "#ffe8d6");
                map.put("icon", "set_meal");
                map.put("iconColor", "#e27221");
            } else if ("main".equals(cat)) {
                map.put("bgFrom", "#fbebeb");
                map.put("bgTo", "#f7d5d5");
                map.put("icon", "outdoor_grill");
                map.put("iconColor", "#be3131");
            } else if ("dessert".equals(cat)) {
                map.put("bgFrom", "#fbf0f6");
                map.put("bgTo", "#f7dceb");
                map.put("icon", "cake");
                map.put("iconColor", "#be318a");
            } else if ("drink".equals(cat)) {
                map.put("bgFrom", "#eaf5fb");
                map.put("bgTo", "#d3eafd");
                map.put("icon", "local_bar");
                map.put("iconColor", "#218be2");
            } else if ("salad".equals(cat)) {
                map.put("bgFrom", "#ebfbf0");
                map.put("bgTo", "#d6f7dc");
                map.put("icon", "eco");
                map.put("iconColor", "#21be55");
            } else {
                map.put("bgFrom", "#f5f5f5");
                map.put("bgTo", "#e0e0e0");
                map.put("icon", "restaurant");
                map.put("iconColor", "#757575");
            }

            mappedItems.add(map);
        }

        model.addAttribute("menuItems", mappedItems);
        return "f&bStaff/e-menu";
    }

    @GetMapping("/shift-report")
    public String shiftReport() {
        return "f&bStaff/shift-report";
    }

    @GetMapping("/order-detail")
    public String orderDetail() {
        return "f&bStaff/order-detail";
    }
}
