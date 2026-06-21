package com.kawai.controllers.api;

import com.kawai.dto.CreateFoodOrderRequest;
import com.kawai.dto.CartItemDto;
import com.kawai.models.*;
import com.kawai.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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
                if (roomOpt.isEmpty()) {
                    return ResponseEntity.status(400).body(Map.of("status", "error", "message", "Phòng không tồn tại!"));
                }
                if (roomOpt.get().getCurrentBookingDetailId() != null) {
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
                    if (tableOpt.isPresent()) {
                        order.setTable(tableOpt.get());
                    } else {
                        return ResponseEntity.status(400).body(Map.of("status", "error", "message", "Bàn ăn không tồn tại!"));
                    }
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

            if (Boolean.TRUE.equals(request.getIsPaid())) {
                order.setOrderStatus("PAID");
                order.setIsPaidInPos(true);
            } else {
                order.setOrderStatus("Pending");
                order.setPaymentType(request.getPaymentType() != null ? request.getPaymentType() : "Pay_Later");
                if ("ONLINE".equalsIgnoreCase(request.getPaymentType())) {
                    order.setIsPaidInPos(true);
                } else if ("VNPAY".equalsIgnoreCase(request.getPaymentType())) {
                    order.setIsPaidInPos(false);
                } else {
                    order.setIsPaidInPos(false);
                }
            }
            
            String finalNote = "";
            if (request.getGuestName() != null && !request.getGuestName().trim().isEmpty()) {
                finalNote = "GUEST:" + request.getGuestName().trim() + "|";
            }
            if (request.getNote() != null) {
                finalNote += request.getNote();
            }
            order.setNote(finalNote);

            // Mock createdByStaff to Employee ID 2 (Trần Phương)
            Optional<Employee> empOpt = employeeRepository.findById(2L);
            if (empOpt.isPresent()) {
                order.setCreatedByStaff(empOpt.get());
            } else {
                // Fallback: try finding ID 1 or any
                employeeRepository.findAll().stream().findFirst().ifPresent(order::setCreatedByStaff);
            }

            FoodOrder savedOrder = foodOrderRepository.save(order);

            BigDecimal subtotal = BigDecimal.ZERO;

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

                        // Accumulate subtotal
                        if (itemDto.getPrice() != null && itemDto.getQty() != null) {
                            subtotal = subtotal.add(itemDto.getPrice().multiply(new BigDecimal(itemDto.getQty())));
                        }
                    } else {
                        return ResponseEntity.status(400).body(Map.of("status", "error", "message", "Món ăn không tồn tại!"));
                    }
                }
            }

            // Deduct credit limit for CHARGE_TO_ROOM
            if ("CHARGE_TO_ROOM".equalsIgnoreCase(request.getPaymentType()) && activeBooking != null && activeBooking instanceof RoomBooking) {
                RoomBooking roomBooking = (RoomBooking) activeBooking;
                
                if (!"Checked_In".equalsIgnoreCase(roomBooking.getBookingStatus())) {
                    return ResponseEntity.status(400).body(Map.of("status", "error", "message", "Tài khoản hoặc phòng chưa thực hiện Check-in, không thể ghi nợ hạn mức!"));
                }

                BigDecimal feePercent = new BigDecimal("0.05");
                BigDecimal fee = subtotal.multiply(feePercent);
                BigDecimal totalAmount = subtotal.add(fee);

                BigDecimal currentLimit = roomBooking.getCreditLimit() != null ? roomBooking.getCreditLimit() : BigDecimal.ZERO;
                if (currentLimit.compareTo(totalAmount) >= 0) {
                    roomBooking.setCreditLimit(currentLimit.subtract(totalAmount));
                    roomBookingRepository.save(roomBooking);
                } else {
                    return ResponseEntity.status(400).body(Map.of("status", "error", "message", "Hạn mức tín dụng của phòng không đủ để thanh toán!"));
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

    @PostMapping("/orders/{id}/pay")
    public ResponseEntity<?> payOrder(@PathVariable Long id) {
        Optional<FoodOrder> orderOpt = foodOrderRepository.findById(id);
        if (orderOpt.isPresent()) {
            FoodOrder order = orderOpt.get();
            order.setOrderStatus("PAID");
            order.setIsPaidInPos(true);
            foodOrderRepository.save(order);
            return ResponseEntity.ok(Map.of("status", "success"));
        }
        return ResponseEntity.status(404).body(Map.of("error", "Order not found"));
    }
}
