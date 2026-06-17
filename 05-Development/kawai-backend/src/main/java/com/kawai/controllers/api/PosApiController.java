package com.kawai.controllers.api;

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

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private RoomBookingRepository roomBookingRepository;

    @PostMapping("/orders")
    public ResponseEntity<?> createOrder(@RequestBody CreateFoodOrderRequest request, java.security.Principal principal) {
        try {
            FoodOrder order = new FoodOrder();

            Account userAccount = null;
            if (principal != null) {
                String identifier = principal.getName();
                if (principal instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) {
                    org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken oauthToken = 
                        (org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) principal;
                    identifier = oauthToken.getPrincipal().getAttribute("email");
                }
                if (identifier != null) {
                    userAccount = accountRepository.findByUsername(identifier).orElse(null);
                }
            }

            Booking activeBooking = null;

            // Mapping order type
            if ("room-svc".equals(request.getOrderType())) {
                order.setOrderType("Room Service");
                Optional<Room> roomOpt = roomRepository.findByRoomNumber(request.getRoomNumber());
                if (roomOpt.isPresent() && roomOpt.get().getCurrentBookingDetailId() != null) {
                    Optional<RoomBookingDetail> detailOpt = roomBookingDetailRepository
                            .findById(roomOpt.get().getCurrentBookingDetailId());
                    if (detailOpt.isPresent()) {
                        RoomBookingDetail detail = detailOpt.get();
                        order.setRoomBookingDetail(detail);
                        if (detail.getRoomBooking() != null) {
                            activeBooking = detail.getRoomBooking();
                        }
                    }
                }
            } else {
                order.setOrderType("Dine In");
                if (request.getTableId() != null) {
                    Optional<RestaurantTable> tableOpt = restaurantTableRepository.findById(request.getTableId());
                    tableOpt.ifPresent(order::setTable);
                }
            }

            // Fallback: if activeBooking is still null, look up user's active/latest booking
            if (activeBooking == null && userAccount != null) {
                Customer customer = customerRepository.findByAccount_Username(userAccount.getUsername()).orElse(null);
                if (customer != null) {
                    java.util.List<RoomBooking> rbs = roomBookingRepository.findByCustomerOrderByBookingDateDesc(customer);
                    if (!rbs.isEmpty()) {
                        activeBooking = rbs.stream()
                            .filter(rb -> "Checked_In".equals(rb.getBookingStatus()) || 
                                           "Confirmed".equals(rb.getBookingStatus()))
                            .findFirst()
                            .orElse(rbs.get(rbs.size() - 1));
                    }
                }
            }

            if (activeBooking != null) {
                order.setBooking(activeBooking);
            }

            order.setOrderStatus("Pending");
            order.setPaymentType(request.getPaymentType() != null ? request.getPaymentType() : "Pay_Later");
            if ("ONLINE".equalsIgnoreCase(request.getPaymentType())) {
                order.setIsPaidInPos(true);
            } else {
                order.setIsPaidInPos(false);
            }
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
                    "message", e.getMessage() != null ? e.getMessage() : "null message"));
        }
    }
}
