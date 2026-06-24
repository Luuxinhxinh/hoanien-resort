package com.kawai.services.impl;

import com.kawai.models.RestaurantTable;
import com.kawai.models.MenuItem;
import com.kawai.models.FoodOrder;
import com.kawai.repositories.RestaurantTableRepository;
import com.kawai.repositories.FoodItemRepository;
import com.kawai.repositories.FoodOrderRepository;
import com.kawai.repositories.TableReservationRepository;
import com.kawai.services.interfaces.TableService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
public class TableServiceImpl implements TableService {

    private final RestaurantTableRepository tableRepository;
    private final FoodItemRepository foodItemRepository;
    private final FoodOrderRepository foodOrderRepository;
    private final TableReservationRepository tableReservationRepository;

    public TableServiceImpl(RestaurantTableRepository tableRepository, FoodItemRepository foodItemRepository, FoodOrderRepository foodOrderRepository, TableReservationRepository tableReservationRepository) {
        this.tableRepository = tableRepository;
        this.foodItemRepository = foodItemRepository;
        this.foodOrderRepository = foodOrderRepository;
        this.tableReservationRepository = tableReservationRepository;
    }

    @Override
    @Transactional
    public RestaurantTable saveTable(String tableNumber, int capacity) {
        boolean exists = tableRepository.findAll().stream()
                .anyMatch(t -> t.getTableNumber().equalsIgnoreCase(tableNumber));
        if (exists) {
            throw new DataIntegrityViolationException("Duplicate entry '" + tableNumber + "' for key 'table_number'");
        }

        RestaurantTable table = new RestaurantTable();
        table.setTableNumber(tableNumber);
        table.setCapacity(capacity);
        table.setTableStatus("Available");
        table.setIsActive(true);
        return tableRepository.save(table);
    }

    @Override
    @Transactional
    public RestaurantTable toggleStatus(Long id, String newStatus) {
        RestaurantTable table = tableRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Table not found"));

        if ("OCCUPIED".equalsIgnoreCase(table.getTableStatus()) && "OUT_OF_SERVICE".equalsIgnoreCase(newStatus)) {
            throw new IllegalStateException("INVALID_TABLE_STATUS: Cannot close a table that is currently occupied");
        }
        
        if ("Occupied".equalsIgnoreCase(table.getTableStatus()) && 
            ("Available".equalsIgnoreCase(newStatus) || "Cleaning".equalsIgnoreCase(newStatus))) {
            List<FoodOrder> orders = foodOrderRepository.findAll();
            java.time.LocalDate today = java.time.LocalDate.now();
            boolean hasPending = orders.stream().anyMatch(o -> o.getTable() != null && o.getTable().getId().equals(id) &&
                    ("Pending".equalsIgnoreCase(o.getOrderStatus()) || "Preparing".equalsIgnoreCase(o.getOrderStatus()) || "Served".equalsIgnoreCase(o.getOrderStatus())) &&
                    o.getOrderTime() != null && o.getOrderTime().toLocalDate().equals(today));
            if (hasPending) {
                throw new IllegalStateException("TABLE-004: Bàn vẫn còn hoá đơn chưa thanh toán trong ngày hôm nay");
            }

            // Hoàn thành các lịch đặt bàn đang Seated của bàn này
            List<com.kawai.models.TableReservation> reservations = tableReservationRepository.findByTable_IdAndReserveDateOrderByReserveTimeAsc(id, java.time.LocalDate.now());
            for (com.kawai.models.TableReservation res : reservations) {
                if ("Seated".equalsIgnoreCase(res.getStatus())) {
                    res.setStatus("Completed");
                    tableReservationRepository.save(res);
                }
            }
        }

        table.setTableStatus(newStatus);
        return tableRepository.save(table);
    }

    @Override
    @Transactional
    public void softDeleteTable(Long id) {
        RestaurantTable table = tableRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Table not found"));

        table.setIsActive(false);
        tableRepository.save(table);
    }

    @Override
    @Transactional
    public MenuItem createMenuItem(String itemName, BigDecimal price, String category) {
        // Validation for negative price
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }

        boolean exists = foodItemRepository.findAll().stream()
                .anyMatch(m -> m.getItemName().equalsIgnoreCase(itemName));
        if (exists) {
            throw new DataIntegrityViolationException("Duplicate menu item name");
        }

        MenuItem item = new MenuItem();
        item.setItemName(itemName);
        item.setPrice(price);
        item.setCategory(category);
        item.setIsAvailable(true);
        return foodItemRepository.save(item);
    }

    @Override
    @Transactional
    public MenuItem toggleMenuAvailability(Long id, boolean isAvailable) {
        MenuItem item = foodItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found"));
        item.setIsAvailable(isAvailable);
        return foodItemRepository.save(item);
    }
}
