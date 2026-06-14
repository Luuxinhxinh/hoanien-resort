package com.kawai.controllers;

import com.kawai.dto.CreateFoodOrderRequest;
import com.kawai.dto.CartItemDto;
import com.kawai.models.*;
import com.kawai.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.Map;

@RestController
@RequestMapping("/api/pos")
public class PosApiController {

    @Autowired
    private FoodOrderRepository foodOrderRepository;
    
    @Autowired
    private FoodOrderDetailRepository foodOrderDetailRepository;
    
    @Autowired
    private RoomRepository roomRepository;
    
    @Autowired
    private RoomBookingDetailRepository roomBookingDetailRepository;
    
    @Autowired
    private RestaurantTableRepository restaurantTableRepository;
    
    @Autowired
    private FoodItemRepository foodItemRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;

    @PostMapping("/orders")
    public ResponseEntity<?> createOrder(@RequestBody CreateFoodOrderRequest request) {
        try {
            FoodOrder order = new FoodOrder();
            
            // Mapping order type
            if ("room-svc".equals(request.getOrderType())) {
                order.setOrderType("Room Service");
                Optional<Room> roomOpt = roomRepository.findByRoomNumber(request.getRoomNumber());
                if (roomOpt.isPresent() && roomOpt.get().getCurrentBookingDetailId() != null) {
                    Optional<RoomBookingDetail> detailOpt = roomBookingDetailRepository.findById(roomOpt.get().getCurrentBookingDetailId());
                    detailOpt.ifPresent(order::setRoomBookingDetail);
                }
            } else {
                order.setOrderType("Dine In");
                if (request.getTableId() != null) {
                    Optional<RestaurantTable> tableOpt = restaurantTableRepository.findById(request.getTableId());
                    tableOpt.ifPresent(order::setTable);
                }
            }

            order.setOrderStatus("Pending");
            order.setPaymentType(request.getPaymentType() != null ? request.getPaymentType() : "Pay_Later");
            order.setIsPaidInPos(false);
            order.setNote(request.getNote());

            // Mock createdByStaff to Employee ID 2 (Trần Phương)
            Optional<Employee> empOpt = employeeRepository.findById(2L);
            if (empOpt.isPresent()) {
                order.setCreatedByStaff(empOpt.get());
            } else {
                // Fallback: try finding ID 1 or any
                employeeRepository.findAll().stream().findFirst().ifPresent(order::setCreatedByStaff);
            }

            FoodOrder savedOrder = foodOrderRepository.save(order);

            // Save Details
            if (request.getItems() != null) {
                for (CartItemDto itemDto : request.getItems()) {
                    Optional<MenuItem> menuOpt = foodItemRepository.findById(itemDto.getId());
                    if (menuOpt.isPresent()) {
                        FoodOrderDetail detail = new FoodOrderDetail();
                        detail.setFoodOrder(savedOrder);
                        detail.setMenuItem(menuOpt.get());
                        detail.setQuantity(itemDto.getQty());
                        detail.setPriceAtOrder(itemDto.getPrice());
                        detail.setKotStatus("Pending");
                        foodOrderDetailRepository.save(detail);
                    }
                }
            }

            return ResponseEntity.ok().body(Map.of("status", "success", "orderId", savedOrder.getId()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                "error", e.getClass().getName(),
                "message", e.getMessage() != null ? e.getMessage() : "null message"
            ));
        }
    }
}
