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
        java.util.Collections.reverse(foodOrders); // Đơn mới hiển thị trước
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
        java.time.DayOfWeek today = java.time.LocalDate.now().getDayOfWeek();
        List<MenuItem> rawItems = foodItemRepository.findAvailableByDayOfWeek(today);
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
        String cleanId = idParam.replace("ORD-", "").replace("RES-", "").replace("RS-", "");
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

    @Override
    public Map<String, Object> getKitchenData() {
        Map<String, Object> data = new HashMap<>();
        List<FoodOrder> orders = foodOrderRepository.findAll().stream()
                .filter(o -> "Pending".equalsIgnoreCase(o.getOrderStatus()) || "Preparing".equalsIgnoreCase(o.getOrderStatus()))
                .toList();
        data.put("orders", orders);
        return data;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Map<String, Object> getRoomServiceManagementData(String dateString) {
        Map<String, Object> data = new HashMap<>();
        
        java.time.LocalDate date = java.time.LocalDate.now();
        if (dateString != null && !dateString.isEmpty()) {
            try {
                date = java.time.LocalDate.parse(dateString);
            } catch (Exception e) {
                // ignore, use today
            }
        }
        
        java.time.LocalDateTime startOfDay = date.atStartOfDay();
        java.time.LocalDateTime endOfDay = startOfDay.plusDays(1);
        
        data.put("currentDate", date.toString()); // For UI filtering
        
        List<FoodOrder> rsOrders = foodOrderRepository.findRoomServiceOrders().stream()
            .filter(o -> o.getOrderTime() != null && !o.getOrderTime().isBefore(startOfDay) && o.getOrderTime().isBefore(endOfDay))
            .collect(java.util.stream.Collectors.toList());
        
        long totalOrders = 0;
        long pendingOrders = 0;
        long preparingOrders = 0;
        long servedOrders = 0;
        
        java.time.format.DateTimeFormatter timeFormatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm");
        
        List<Map<String, Object>> mappedOrders = new ArrayList<>();
        
        for (FoodOrder order : rsOrders) {
            totalOrders++;
            String status = order.getOrderStatus() != null ? order.getOrderStatus().toLowerCase() : "pending";
            if (status.equals("pending")) pendingOrders++;
            else if (status.equals("preparing")) preparingOrders++;
            else if (status.equals("served") || status.equals("ready")) servedOrders++;
            
            // Bổ sung Delivering status count
            long deliveringOrders = 0;
            if (data.containsKey("deliveringOrders")) deliveringOrders = (long) data.get("deliveringOrders");
            if (status.equals("delivering")) deliveringOrders++;
            data.put("deliveringOrders", deliveringOrders);
            
            Map<String, Object> orderMap = new HashMap<>();
            orderMap.put("id", order.getId());
            orderMap.put("displayId", "RS-" + order.getId());
            
            String room = "";
            String guest = "";
            if (order.getRoomBookingDetail() != null) {
                if (order.getRoomBookingDetail().getRoom() != null) {
                    room = order.getRoomBookingDetail().getRoom().getRoomNumber();
                }
                if (order.getRoomBookingDetail().getCustomer() != null) {
                    guest = order.getRoomBookingDetail().getCustomer().getFullName();
                }
            } else if (order.getBooking() != null && order.getBooking().getCustomer() != null) {
                guest = order.getBooking().getCustomer().getFullName();
            }
            
            orderMap.put("room", room);
            orderMap.put("guestName", guest);
            orderMap.put("time", order.getOrderTime() != null ? order.getOrderTime().format(timeFormatter) : "");
            orderMap.put("orderTimeIso", order.getOrderTime() != null ? order.getOrderTime().toString() : "");
            
            // Lấy Tầng dựa trên số phòng (Giả định số phòng bắt đầu bằng Tầng, VD: 305 -> Tầng 3)
            String floor = "Unknown";
            if (room != null && room.length() > 0) {
                floor = "Tầng " + room.substring(0, 1);
            }
            orderMap.put("floor", floor);
            
            // Tính thời gian ETA linh động (Dynamic SLA) không cần Database
            // Quét qua tất cả các món để tìm món cần chuẩn bị lâu nhất
            int itemsCount = 0;
            int maxPrep = 0;
            if (order.getDetails() != null) {
                for (com.kawai.models.FoodOrderDetail detail : order.getDetails()) {
                    int qty = detail.getQuantity() != null ? detail.getQuantity() : 1;
                    itemsCount += qty;
                    
                    if (detail.getMenuItem() != null && detail.getMenuItem().getCategory() != null) {
                        String cat = detail.getMenuItem().getCategory().toLowerCase();
                        int prep = 10; // Thời gian chuẩn bị cơ bản
                        if (cat.contains("main") || cat.contains("chính") || cat.contains("hot")) prep = 20; // Món chính lâu hơn
                        else if (cat.contains("drink") || cat.contains("nước")) prep = 5; // Đồ uống nhanh hơn
                        if (prep > maxPrep) maxPrep = prep;
                    }
                }
            }
            if (maxPrep == 0) maxPrep = 15; // Mặc định nếu không phân loại được
            
            // Công thức: Thời gian của món lâu nhất + (2 phút/món phụ trội nếu > 2 món) + 5 phút di chuyển
            int etaMins = maxPrep + (itemsCount > 2 ? (itemsCount - 2) * 2 : 0) + 5;
            orderMap.put("etaMins", etaMins);
            
            orderMap.put("itemsText", itemsCount + (itemsCount > 1 ? " items" : " item"));
            
            java.text.NumberFormat format = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("vi", "VN"));
            orderMap.put("amount", format.format(order.getTotalAmount()));
            
            orderMap.put("rawStatus", status);
            
            mappedOrders.add(orderMap);
        }
        
        // Gom đơn theo Tầng (Group by Floor) để phục vụ cho tính năng Giao Cả Tầng (Batch Dispatch)
        Map<String, List<Map<String, Object>>> groupedOrders = new java.util.TreeMap<>();
        for (Map<String, Object> om : mappedOrders) {
            String floor = (String) om.get("floor");
            groupedOrders.computeIfAbsent(floor, k -> new ArrayList<>()).add(om);
        }
        
        data.put("orders", mappedOrders);
        data.put("groupedOrders", groupedOrders);
        data.put("totalOrders", totalOrders);
        data.put("pendingOrders", pendingOrders);
        data.put("preparingOrders", preparingOrders);
        data.put("servedOrders", servedOrders);
        
        return data;
    }
}
