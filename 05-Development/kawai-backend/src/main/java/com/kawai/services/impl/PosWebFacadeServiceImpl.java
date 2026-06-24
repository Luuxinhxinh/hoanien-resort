package com.kawai.services.impl;

import com.kawai.models.FoodOrder;
import com.kawai.models.MenuItem;
import com.kawai.models.RestaurantTable;
import com.kawai.models.TableReservation;
import com.kawai.repositories.FoodItemRepository;
import com.kawai.repositories.FoodOrderRepository;
import com.kawai.repositories.RestaurantTableRepository;
import com.kawai.repositories.TableReservationRepository;
import com.kawai.services.interfaces.PosWebFacadeService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PosWebFacadeServiceImpl implements PosWebFacadeService {

    private final FoodOrderRepository foodOrderRepository;
    private final RestaurantTableRepository restaurantTableRepository;
    private final FoodItemRepository foodItemRepository;
    private final TableReservationRepository tableReservationRepository;

    public PosWebFacadeServiceImpl(FoodOrderRepository foodOrderRepository,
                                   RestaurantTableRepository restaurantTableRepository,
                                   FoodItemRepository foodItemRepository,
                                   TableReservationRepository tableReservationRepository) {
        this.foodOrderRepository = foodOrderRepository;
        this.restaurantTableRepository = restaurantTableRepository;
        this.foodItemRepository = foodItemRepository;
        this.tableReservationRepository = tableReservationRepository;
    }

    @Override
    public Map<String, Object> getDashboardData() {
        List<FoodOrder> foodOrders = foodOrderRepository.findAll();
        List<RestaurantTable> tables = restaurantTableRepository.findAll();

        long totalTables = tables.size();
        long occupiedTables = tables.stream().filter(t -> "Occupied".equalsIgnoreCase(t.getTableStatus())).count();
        long activeOrders = foodOrders.stream().filter(o -> "Pending".equalsIgnoreCase(o.getOrderStatus()) || "Preparing".equalsIgnoreCase(o.getOrderStatus()) || "Ready".equalsIgnoreCase(o.getOrderStatus())).count();
        long pendingRoomServices = foodOrders.stream().filter(o -> "Room Service".equalsIgnoreCase(o.getOrderType()) && "Pending".equalsIgnoreCase(o.getOrderStatus())).count();

        Map<String, Object> data = new HashMap<>();
        data.put("foodOrders", foodOrders);
        data.put("tables", tables);
        data.put("totalTables", totalTables);
        data.put("occupiedTables", occupiedTables);
        data.put("activeOrders", activeOrders);
        data.put("pendingRoomServices", pendingRoomServices);
        return data;
    }

    @Override
    public Map<String, Object> getCreateFoodOrderData() {
        List<RestaurantTable> tables = restaurantTableRepository.findAll();

        List<RestaurantTable> occupiedTables = tables.stream()
                .filter(t -> "Occupied".equalsIgnoreCase(t.getTableStatus()))
                .toList();

        List<RestaurantTable> otherTables = tables.stream()
                .filter(t -> "Available".equalsIgnoreCase(t.getTableStatus()) || "Cleaning".equalsIgnoreCase(t.getTableStatus()))
                .toList();

        List<RestaurantTable> vacantTables = new java.util.ArrayList<>();
        List<RestaurantTable> reservedTables = new java.util.ArrayList<>();

        Map<Long, String> tableAvailabilityText = new HashMap<>();
        LocalDate today = LocalDate.now();
        java.time.LocalDateTime currentDT = java.time.LocalDateTime.now();

        for (RestaurantTable table : otherTables) {
            List<TableReservation> reservations = tableReservationRepository.findByTable_IdAndReserveDateOrderByReserveTimeAsc(table.getId(), today);
            String availability = "Trống cả ngày";
            boolean hasUpcoming = false;
            
            for (TableReservation res : reservations) {
                if ("Confirmed".equalsIgnoreCase(res.getStatus()) || "Pending".equalsIgnoreCase(res.getStatus())) {
                    java.time.LocalDateTime resStartDT = java.time.LocalDateTime.of(today, res.getReserveTime());
                    java.time.LocalDateTime resEndDT;
                    if (res.getEndTime() != null) {
                        resEndDT = java.time.LocalDateTime.of(today, res.getEndTime());
                        if (resEndDT.isBefore(resStartDT)) resEndDT = resEndDT.plusDays(1);
                    } else {
                        resEndDT = resStartDT.plusHours(2);
                    }

                    if (currentDT.isBefore(resStartDT)) {
                        availability = "Trống đến " + res.getReserveTime().toString();
                        hasUpcoming = true;
                        break;
                    } else if (currentDT.isBefore(resEndDT)) {
                        availability = "Đã đến giờ đặt (" + res.getReserveTime().toString() + ")";
                        hasUpcoming = true;
                        break;
                    }
                }
            }
            if ("Cleaning".equalsIgnoreCase(table.getTableStatus())) {
                availability = "Đang dọn - " + availability;
            }
            tableAvailabilityText.put(table.getId(), availability);

            if (hasUpcoming) {
                reservedTables.add(table);
            } else {
                vacantTables.add(table);
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("vacantTables", vacantTables);
        data.put("reservedTables", reservedTables);
        data.put("occupiedTables", occupiedTables);
        data.put("tableAvailabilityText", tableAvailabilityText);
        data.put("menuItems", getMappedMenuItems());
        return data;
    }

    @Override
    public Map<String, Object> getTableManagementData() {
        List<RestaurantTable> tables = restaurantTableRepository.findAll();

        List<FoodOrder> activeOrders = foodOrderRepository.findAll().stream()
                .filter(o -> ("Dine-In".equalsIgnoreCase(o.getOrderType()) || "Dine In".equalsIgnoreCase(o.getOrderType()) || "Table".equalsIgnoreCase(o.getOrderType()))
                        && ("Pending".equalsIgnoreCase(o.getOrderStatus()) || "Preparing".equalsIgnoreCase(o.getOrderStatus()) || "Served".equalsIgnoreCase(o.getOrderStatus())))
                .toList();

        Map<Long, Long> tableActiveOrderMap = new HashMap<>();
        for (FoodOrder order : activeOrders) {
            if (order.getTable() != null) {
                tableActiveOrderMap.put(order.getTable().getId(), order.getId());
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("tables", tables);
        data.put("tableActiveOrderMap", tableActiveOrderMap);
        return data;
    }

    @Override
    public List<Map<String, Object>> getMappedMenuItems() {
        List<MenuItem> rawItems = foodItemRepository.findAll();
        List<Map<String, Object>> mappedItems = new ArrayList<>();

        for (MenuItem item : rawItems) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", item.getId());
            map.put("name", item.getItemName());
            map.put("desc", item.getDescription() != null ? item.getDescription() : "");
            map.put("price", item.getPrice());
            map.put("catLabel", item.getCategory());
            map.put("imageUrl", item.getImageUrl());

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
        return mappedItems;
    }

    @Override
    public Map<String, Object> getOrderDetailData(String idParam) {
        Map<String, Object> data = new HashMap<>();
        String cleanId = idParam.replace("ORD-", "").replace("RES-", "");
        Long id = Long.parseLong(cleanId);
        foodOrderRepository.findById(id).ifPresent(order -> {
            String extractedGuestName = "";
            String realNote = order.getNote();
            if (realNote != null && realNote.startsWith("GUEST:")) {
                int pipeIndex = realNote.indexOf("|");
                if (pipeIndex != -1) {
                    extractedGuestName = realNote.substring(6, pipeIndex);
                    order.setNote(realNote.substring(pipeIndex + 1));
                }
            }
            data.put("extractedGuestName", extractedGuestName);
            data.put("foodOrder", order);
        });
        
        data.put("menuItems", getMappedMenuItems());
        
        return data;
    }
}
