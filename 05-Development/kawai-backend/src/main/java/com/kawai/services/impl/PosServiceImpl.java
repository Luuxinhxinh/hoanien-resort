package com.kawai.services.impl;

import com.kawai.dto.CartItemDto;
import com.kawai.dto.CreateFoodOrderRequest;
import com.kawai.exceptions.BusinessException;
import com.kawai.models.*;
import com.kawai.repositories.*;
import com.kawai.services.interfaces.PosService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PosServiceImpl implements PosService {

    private final RoomRepository roomRepository;
    private final RoomBookingDetailRepository roomBookingDetailRepository;
    private final FolioItemRepository folioItemRepository;
    private final FoodOrderRepository foodOrderRepository;
    private final FoodOrderDetailRepository foodOrderDetailRepository;
    private final RestaurantTableRepository restaurantTableRepository;
    private final FoodItemRepository foodItemRepository;
    private final EmployeeRepository employeeRepository;
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final RoomBookingRepository roomBookingRepository;
    private final TableReservationRepository tableReservationRepository;
    private final com.kawai.services.interfaces.EmailService emailService;
    private final RefundRequestRepository refundRequestRepository;

    public PosServiceImpl(RoomRepository roomRepository,
            RoomBookingDetailRepository roomBookingDetailRepository,
            FolioItemRepository folioItemRepository,
            FoodOrderRepository foodOrderRepository,
            FoodOrderDetailRepository foodOrderDetailRepository,
            RestaurantTableRepository restaurantTableRepository,
            FoodItemRepository foodItemRepository,
            EmployeeRepository employeeRepository,
            AccountRepository accountRepository,
            CustomerRepository customerRepository,
            RoomBookingRepository roomBookingRepository,
            TableReservationRepository tableReservationRepository,
            com.kawai.services.interfaces.EmailService emailService,
            RefundRequestRepository refundRequestRepository) {
        this.roomRepository = roomRepository;
        this.roomBookingDetailRepository = roomBookingDetailRepository;
        this.folioItemRepository = folioItemRepository;
        this.foodOrderRepository = foodOrderRepository;
        this.foodOrderDetailRepository = foodOrderDetailRepository;
        this.restaurantTableRepository = restaurantTableRepository;
        this.foodItemRepository = foodItemRepository;
        this.employeeRepository = employeeRepository;
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.roomBookingRepository = roomBookingRepository;
        this.tableReservationRepository = tableReservationRepository;
        this.emailService = emailService;
        this.refundRequestRepository = refundRequestRepository;
    }

    @jakarta.annotation.PostConstruct
    public void cleanupOrphanedFolios() {
        try {
            System.out.println("--- CLEANING UP ORPHANED FOLIO ITEMS FOR CANCELLED FOOD ORDERS ---");
            List<com.kawai.models.FolioItem> folios = folioItemRepository.findAll();
            for (com.kawai.models.FolioItem folio : folios) {
                if (folio.getDescription() != null && folio.getDescription().startsWith("Ký bill đồ ăn F&B (Order #")) {
                    String desc = folio.getDescription();
                    try {
                        String idStr = desc.substring(desc.indexOf("#") + 1, desc.indexOf(")"));
                        Long orderId = Long.parseLong(idStr);
                        FoodOrder order = foodOrderRepository.findById(orderId).orElse(null);
                        if (order != null && "Cancelled".equalsIgnoreCase(order.getOrderStatus())) {
                            System.out.println("Deleting orphaned FolioItem ID " + folio.getId() + " for cancelled order " + orderId);
                            folioItemRepository.delete(folio);
                        }
                    } catch (Exception e) {}
                }
            }
        } catch (Exception e) {
            // Bỏ qua lỗi khi DB vừa được tạo lại (ddl-auto=create) và data chưa được seed
            System.out.println("--- cleanupOrphanedFolios: DB chưa có dữ liệu, bỏ qua cleanup lần này. ---");
        }
    }

    @Override
    public FoodOrder getOrderById(Long id) {
        return foodOrderRepository.findById(id).orElse(null);
    }

    @Override
    public FoodOrder createOrder(CreateFoodOrderRequest request, String userIdentifier) {
        FoodOrder order = new FoodOrder();

        Account userAccount = null;
        if (userIdentifier != null) {
            userAccount = accountRepository.findByUsername(userIdentifier).orElse(null);
        }

        Booking activeBooking = null;

        if ("room-svc".equals(request.getOrderType())) {
            order.setOrderType("Room Service");
            Room room = roomRepository.findByRoomNumber(request.getRoomNumber())
                    .orElseThrow(() -> new BusinessException("POS-001", "Phòng không tồn tại!"));

            if (room.getCurrentBookingDetailId() != null) {
                Optional<RoomBookingDetail> detailOpt = roomBookingDetailRepository
                        .findById(room.getCurrentBookingDetailId());
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

            java.time.LocalTime orderTime = java.time.LocalTime.now();
            if (orderTime.isAfter(java.time.LocalTime.of(22, 59)) || orderTime.isBefore(java.time.LocalTime.of(8, 0))) {
                throw new BusinessException("POS-009",
                        "Nhà hàng không nhận khách ăn tại bàn trong khung giờ từ 23:00 đến 08:00 sáng. Quý khách vui lòng sử dụng dịch vụ gọi món lên phòng.");
            }

            if (request.getTableId() != null) {
                // Check if there is already an active order for this table
                List<FoodOrder> activeOrders = foodOrderRepository.findActiveOrdersByTable(request.getTableId());
                if (!activeOrders.isEmpty()) {
                    FoodOrder existingOrder = activeOrders.get(0);
                    if (request.getItems() != null && !request.getItems().isEmpty()) {
                        addItemsToOrder(existingOrder.getId(), request.getItems());
                    }
                    return foodOrderRepository.findById(existingOrder.getId()).orElse(existingOrder);
                }

                RestaurantTable table = restaurantTableRepository.findById(request.getTableId())
                        .orElseThrow(() -> new BusinessException("POS-002", "Bàn ăn không tồn tại!"));

                if ("Cleaning".equalsIgnoreCase(table.getTableStatus())
                        || "Out_of_service".equalsIgnoreCase(table.getTableStatus())) {
                    throw new BusinessException("POS-007", "Bàn đang được dọn hoặc bảo trì, không thể tạo hóa đơn!");
                }

                if ("Available".equalsIgnoreCase(table.getTableStatus())) {
                    java.time.LocalDate today = java.time.LocalDate.now();
                    java.time.LocalTime now = java.time.LocalTime.now();

                    java.time.LocalDateTime currentDT = java.time.LocalDateTime.now();

                    List<TableReservation> reservations = tableReservationRepository
                            .findByTable_IdAndReserveDateOrderByReserveTimeAsc(table.getId(), today);
                    for (TableReservation res : reservations) {
                        if ("Confirmed".equalsIgnoreCase(res.getStatus()) || "Pending".equalsIgnoreCase(res.getStatus())
                                || "Seated".equalsIgnoreCase(res.getStatus())
                                || "Completed".equalsIgnoreCase(res.getStatus())) {
                            java.time.LocalDateTime resStartDT = java.time.LocalDateTime.of(today,
                                    res.getReserveTime());
                            java.time.LocalDateTime resEndDT;
                            if (res.getEndTime() != null) {
                                resEndDT = java.time.LocalDateTime.of(today, res.getEndTime());
                                if (resEndDT.isBefore(resStartDT)) {
                                    resEndDT = resEndDT.plusDays(1);
                                }
                            } else {
                                resEndDT = resStartDT.plusHours(1);
                            }

                            // Add 15 minutes buffer time
                            resEndDT = resEndDT.plusMinutes(15);

                            if (currentDT.isAfter(resStartDT.minusHours(2)) && currentDT.isBefore(resEndDT)) {
                                throw new BusinessException("POS-008",
                                        "Bàn đã có khách đặt trước trong thời gian tới!");
                            }
                        }
                    }
                }

                table.setTableStatus("Occupied");
                restaurantTableRepository.save(table);

                order.setTable(table);
            }
        }

        if (activeBooking == null && userAccount != null) {
            Customer customer = customerRepository.findByAccount_Username(userAccount.getUsername()).orElse(null);
            if (customer != null) {
                List<RoomBooking> rbs = roomBookingRepository
                        .findByCustomerOrderByBookingDateDesc(customer);
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
            order.setOrderStatus("Pending");
            order.setIsPaidInPos(true);
        } else {
            order.setPaymentType(request.getPaymentType() != null ? request.getPaymentType() : "Pay_Later");
            if ("VNPAY".equalsIgnoreCase(request.getPaymentType())) {
                order.setOrderStatus("AWAITING_PAYMENT");
                order.setIsPaidInPos(false);
            } else if ("ONLINE".equalsIgnoreCase(request.getPaymentType())) {
                order.setOrderStatus("Pending");
                order.setIsPaidInPos(true);
            } else {
                order.setOrderStatus("Pending");
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

        Optional<Employee> empOpt = employeeRepository.findById(2L);
        if (empOpt.isPresent()) {
            order.setCreatedByStaff(empOpt.get());
        } else {
            employeeRepository.findAll().stream().findFirst().ifPresent(order::setCreatedByStaff);
        }

        FoodOrder savedOrder = foodOrderRepository.save(order);

        BigDecimal subtotal = BigDecimal.ZERO;

        List<FoodOrderDetail> savedDetails = new java.util.ArrayList<>();
        if (request.getItems() != null) {
            for (CartItemDto itemDto : request.getItems()) {
                MenuItem menuItem = foodItemRepository.findById(itemDto.getId())
                        .orElseThrow(() -> new BusinessException("POS-004", "Món ăn không tồn tại!"));

                // =========================================================
                // CHỐT CHẶN BẢO MẬT: KIỂM TRA MÓN ĂN THEO NGÀY
                // Ngăn chặn trường hợp user dùng Postman hack gửi id món ăn của ngày mai vào
                // giỏ hàng hôm nay.
                // Nếu món này KHÔNG phải món cố định (isAlwaysAvailable = false)
                // VÀ ngày hiện tại không nằm trong danh sách được bán -> Văng lỗi!
                // =========================================================
                if (!Boolean.TRUE.equals(menuItem.getIsAlwaysAvailable()) &&
                        (menuItem.getAvailableDays() == null
                                || !menuItem.getAvailableDays().contains(java.time.LocalDate.now().getDayOfWeek()))) {
                    throw new BusinessException("POS-010", "Món ăn '" + menuItem.getItemName()
                            + "' không được phục vụ vào hôm nay. Vui lòng làm mới giỏ hàng.");
                }

                FoodOrderDetail detail = new FoodOrderDetail();
                detail.setFoodOrder(savedOrder);
                detail.setMenuItem(menuItem);
                detail.setQuantity(itemDto.getQty());
                detail.setPriceAtOrder(itemDto.getPrice());
                detail.setKotStatus("Pending");
                foodOrderDetailRepository.save(detail);

                savedDetails.add(detail);

                if (itemDto.getPrice() != null && itemDto.getQty() != null) {
                    subtotal = subtotal.add(itemDto.getPrice().multiply(new BigDecimal(itemDto.getQty())));
                }
            }
        }
        savedOrder.setDetails(savedDetails);

        // --- NEW CODE: Calculate and set final total amount explicitly ---
        BigDecimal finalTotal = subtotal;
        if ("Room Service".equalsIgnoreCase(savedOrder.getOrderType())) {
            BigDecimal feePercent = new BigDecimal("0.05");
            if ("VNPAY".equalsIgnoreCase(request.getPaymentType())
                    || "ONLINE".equalsIgnoreCase(request.getPaymentType())) {
                feePercent = new BigDecimal("0.03");
            }
            BigDecimal fee = subtotal.multiply(feePercent);
            finalTotal = subtotal.add(fee);
        }
        savedOrder.setTotalAmount(finalTotal);
        foodOrderRepository.save(savedOrder);
        // -----------------------------------------------------------------

        if ("CHARGE_TO_ROOM".equalsIgnoreCase(request.getPaymentType()) && activeBooking != null
                && activeBooking instanceof RoomBooking) {
            BigDecimal totalAmount = savedOrder.getTotalAmount();

            RoomBookingDetail detailToCharge = order.getRoomBookingDetail();
            if (detailToCharge == null) {
                List<RoomBookingDetail> details = roomBookingDetailRepository
                        .findByRoomBookingId(activeBooking.getId());
                detailToCharge = details.stream().filter(d -> "Checked_In".equals(d.getDetailStatus())).findFirst()
                        .orElse(!details.isEmpty() ? details.get(0) : null);
            }

            if (detailToCharge != null) {
                BigDecimal subLimit = detailToCharge.getSubCreditLimit();
                BigDecimal limit = (subLimit != null && subLimit.compareTo(BigDecimal.ZERO) > 0)
                        ? subLimit
                        : (((RoomBooking) activeBooking).getCreditLimit() != null
                                ? ((RoomBooking) activeBooking).getCreditLimit()
                                : BigDecimal.ZERO);
                java.util.List<com.kawai.models.FolioItem> folioItems = folioItemRepository.findByRoomBookingDetailId(detailToCharge.getId());
                BigDecimal charged = folioItems.stream()
                        .filter(f -> !Boolean.TRUE.equals(f.getIsSettledSeparately()))
                        .map(FolioItem::getAmount)
                        .filter(a -> a != null && a.compareTo(BigDecimal.ZERO) > 0)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal creditTopUp = folioItems.stream()
                        .filter(f -> !Boolean.TRUE.equals(f.getIsSettledSeparately()))
                        .filter(f -> f.getDescription() != null && f.getDescription().startsWith("Nạp tiền nâng hạn mức"))
                        .map(FolioItem::getAmount)
                        .filter(a -> a != null && a.compareTo(BigDecimal.ZERO) < 0)
                        .map(BigDecimal::abs)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                if (limit.add(creditTopUp).subtract(charged).compareTo(totalAmount) >= 0) {
                    Customer payer = null;
                    if (detailToCharge.getCustomer() != null) {
                        payer = detailToCharge.getCustomer();
                    } else if (activeBooking.getCustomer() != null) {
                        payer = activeBooking.getCustomer();
                    }

                    if ("Room Service".equalsIgnoreCase(savedOrder.getOrderType())) {
                        FolioItem folioItem = new FolioItem();
                        folioItem.setRoomBookingDetail(detailToCharge);
                        folioItem.setBooking(activeBooking);
                        folioItem.setSourceDepartment("F&B");
                        folioItem.setAmount(totalAmount);
                        folioItem.setDescription("Ký bill Room Service F&B (Order #" + savedOrder.getId() + ")");
                        folioItem.setPayerCustomer(payer);
                        folioItem.setRevenueCode("FB_ROOMSERVICE");
                        folioItemRepository.save(folioItem);
                    } else {
                        BigDecimal beverageAmount = BigDecimal.ZERO;
                        if (savedOrder.getDetails() != null) {
                            for (FoodOrderDetail detail : savedOrder.getDetails()) {
                                if (detail.getMenuItem() != null && "Đồ uống".equalsIgnoreCase(detail.getMenuItem().getCategory())) {
                                    BigDecimal price = detail.getPriceAtOrder() != null ? detail.getPriceAtOrder() : detail.getMenuItem().getPrice();
                                    if (price != null) {
                                        BigDecimal qty = BigDecimal.valueOf(detail.getQuantity() != null ? detail.getQuantity() : 0);
                                        beverageAmount = beverageAmount.add(price.multiply(qty));
                                    }
                                }
                            }
                        }
                        BigDecimal foodAmount = totalAmount.subtract(beverageAmount);
                        if (foodAmount.compareTo(BigDecimal.ZERO) < 0) {
                            foodAmount = BigDecimal.ZERO;
                        }

                        if (foodAmount.compareTo(BigDecimal.ZERO) > 0) {
                            FolioItem fItem = new FolioItem();
                            fItem.setRoomBookingDetail(detailToCharge);
                            fItem.setBooking(activeBooking);
                            fItem.setSourceDepartment("F&B");
                            fItem.setAmount(foodAmount);
                            fItem.setDescription("Ký bill đồ ăn F&B (Order #" + savedOrder.getId() + ")");
                            fItem.setPayerCustomer(payer);
                            fItem.setRevenueCode("FB_FOOD");
                            folioItemRepository.save(fItem);
                        }
                        if (beverageAmount.compareTo(BigDecimal.ZERO) > 0) {
                            FolioItem bItem = new FolioItem();
                            bItem.setRoomBookingDetail(detailToCharge);
                            bItem.setBooking(activeBooking);
                            bItem.setSourceDepartment("F&B");
                            bItem.setAmount(beverageAmount);
                            bItem.setDescription("Ký bill đồ uống F&B (Order #" + savedOrder.getId() + ")");
                            bItem.setPayerCustomer(payer);
                            bItem.setRevenueCode("FB_BEV");
                            folioItemRepository.save(bItem);
                        }
                    }
                } else {
                    throw new BusinessException("POS-005", "Hạn mức tín dụng của phòng không đủ để thanh toán!");
                }
            } else {
                throw new BusinessException("POS-009", "Không tìm thấy phòng để ký bill!");
            }
        }

        if ("Room Service".equalsIgnoreCase(savedOrder.getOrderType())
                && !"AWAITING_PAYMENT".equalsIgnoreCase(savedOrder.getOrderStatus())) {
            if (activeBooking != null && activeBooking.getCustomer() != null) {
                String roomNum = request.getRoomNumber();
                if (roomNum == null && savedOrder.getRoomBookingDetail() != null
                        && savedOrder.getRoomBookingDetail().getRoom() != null) {
                    roomNum = savedOrder.getRoomBookingDetail().getRoom().getRoomNumber();
                }
                emailService.sendRoomServiceConfirmation(savedOrder, activeBooking.getCustomer(), roomNum);
            }
        }

        return savedOrder;
    }

    @Override
    public FoodOrder payOrder(Long id) {
        FoodOrder order = foodOrderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("POS-006", "Đơn hàng không tồn tại"));
        if ("AWAITING_PAYMENT".equalsIgnoreCase(order.getOrderStatus())) {
            order.setOrderStatus("Pending");
        }
        order.setIsPaidInPos(true);

        // Update reservation to Completed and set endTime to now
        RestaurantTable table = order.getTable();
        if (table != null) {
            List<TableReservation> activeReservations = tableReservationRepository
                    .findByTable_IdAndReserveDateOrderByReserveTimeAsc(table.getId(), java.time.LocalDate.now());
            for (TableReservation res : activeReservations) {
                if ("Seated".equalsIgnoreCase(res.getStatus())) {
                    res.setStatus("Completed");
                    res.setEndTime(java.time.LocalTime.now());
                    tableReservationRepository.save(res);
                }
            }

            // Tự động chuyển bàn sang Cleaning
            table.setTableStatus("Cleaning");
            table.setCleaningStartTime(java.time.LocalDateTime.now());
            restaurantTableRepository.save(table);
        }

        return foodOrderRepository.save(order);
    }

    @Override
    public void chargeToRoom(String roomNumber, BigDecimal amount) {
        Room room = getOccupiedRoom(roomNumber);
        RoomBookingDetail detail = getBookingDetail(room.getCurrentBookingDetailId());

        validateCreditLimit(detail, amount);

        postChargeToFolio(detail, amount);
    }

    private Room getOccupiedRoom(String roomNumber) {
        Room room = roomRepository.findByRoomNumber(roomNumber)
                .orElseThrow(() -> new BusinessException("ROOM_NOT_FOUND", "Phòng không tồn tại"));

        if (!"OCCUPIED".equalsIgnoreCase(room.getRoomStatus())) {
            throw new BusinessException("ROOM_NOT_OCCUPIED", "Phòng không ở trạng thái OCCUPIED");
        }
        return room;
    }

    private RoomBookingDetail getBookingDetail(Long bookingDetailId) {
        return roomBookingDetailRepository.findById(bookingDetailId)
                .orElseThrow(() -> new BusinessException("BOOKING_NOT_FOUND", "Không tìm thấy thông tin đặt phòng"));
    }

    private void validateCreditLimit(RoomBookingDetail detail, BigDecimal amount) {
        BigDecimal limit = detail.getSubCreditLimit() != null ? detail.getSubCreditLimit() : BigDecimal.ZERO;
        BigDecimal used = folioItemRepository.findByRoomBookingDetailId(detail.getId()).stream()
                .map(com.kawai.models.FolioItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (limit.subtract(used).compareTo(amount) < 0) {
            throw new BusinessException("POS-003", "Hạn mức chi tiêu phòng không đủ để thanh toán (vượt Credit Limit)");
        }
    }

    private void postChargeToFolio(RoomBookingDetail detail, BigDecimal amount) {
        FolioItem folioItem = new FolioItem();
        folioItem.setRoomBookingDetail(detail);
        folioItem.setSourceDepartment("POS");
        folioItem.setAmount(amount);
        folioItem.setDescription("Ký gửi hóa đơn từ nhà hàng");
        folioItem.setRevenueCode("FB_FOOD");
        folioItemRepository.save(folioItem);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void addItemsToOrder(Long orderId, java.util.List<com.kawai.dto.CartItemDto> items) {
        if (items == null || items.isEmpty()) {
            throw new com.kawai.exceptions.BusinessException("POS-001", "Đơn hàng trống — không có món");
        }

        FoodOrder order = foodOrderRepository.findById(orderId)
                .orElseThrow(() -> new com.kawai.exceptions.BusinessException("POS-002", "Không tìm thấy đơn hàng"));

        if ("Room Service".equalsIgnoreCase(order.getOrderType())
                || "RoomService".equalsIgnoreCase(order.getOrderType())) {
            throw new com.kawai.exceptions.BusinessException("POS-005",
                    "Không hỗ trợ gọi thêm món cho đơn Room Service. Vui lòng tạo đơn mới.");
        }

        if (Boolean.TRUE.equals(order.getIsPaidInPos()) || "PAID".equalsIgnoreCase(order.getOrderStatus())
                || "Cancelled".equalsIgnoreCase(order.getOrderStatus())) {
            throw new com.kawai.exceptions.BusinessException("POS-006",
                    "Đơn hàng đã thanh toán hoặc bị hủy, không thể thêm món.");
        }

        java.util.List<FoodOrderDetail> newDetails = new java.util.ArrayList<>();
        for (com.kawai.dto.CartItemDto item : items) {
            MenuItem menuItem = foodItemRepository.findById(item.getId())
                    .orElseThrow(() -> new com.kawai.exceptions.BusinessException("POS-003",
                            "Món ăn không tồn tại: ID " + item.getId()));

            if (Boolean.FALSE.equals(menuItem.getIsAvailable())) {
                throw new com.kawai.exceptions.BusinessException("POS-004",
                        "Món đã hết — không thể order: " + menuItem.getItemName());
            }

            // CHỐT CHẶN BẢO MẬT: Áp dụng tương tự cho tính năng "Gọi thêm món" khi đang ăn
            // tại bàn
            if (!Boolean.TRUE.equals(menuItem.getIsAlwaysAvailable()) &&
                    (menuItem.getAvailableDays() == null
                            || !menuItem.getAvailableDays().contains(java.time.LocalDate.now().getDayOfWeek()))) {
                throw new com.kawai.exceptions.BusinessException("POS-010",
                        "Món ăn '" + menuItem.getItemName() + "' không được phục vụ vào hôm nay.");
            }

            FoodOrderDetail detail = new FoodOrderDetail();
            detail.setFoodOrder(order);
            detail.setMenuItem(menuItem);
            detail.setQuantity(item.getQty());
            detail.setPriceAtOrder(menuItem.getPrice());
            detail.setKotStatus("Pending");

            newDetails.add(foodOrderDetailRepository.save(detail));
        }

        // if details collection is initialized, we can add to it
        if (order.getDetails() != null) {
            order.getDetails().addAll(newDetails);
        } else {
            order.setDetails(newDetails);
        }

        // Reset order status to pending so kitchen sees new items
        order.setOrderStatus("Pending");
        foodOrderRepository.save(order);
    }

    @Override
    public void updateOrderStatus(Long orderId, String status) {
        FoodOrder order = foodOrderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("POS-006", "Đơn hàng không tồn tại"));

        order.setOrderStatus(status);

        // Update KOT status for all details based on order status
        if ("Preparing".equalsIgnoreCase(status)) {
            for (com.kawai.models.FoodOrderDetail detail : order.getDetails()) {
                if ("Pending".equalsIgnoreCase(detail.getKotStatus())) {
                    detail.setKotStatus("Preparing");
                }
            }
        } else if ("Ready".equalsIgnoreCase(status)) {
            for (com.kawai.models.FoodOrderDetail detail : order.getDetails()) {
                if ("Preparing".equalsIgnoreCase(detail.getKotStatus())
                        || "Pending".equalsIgnoreCase(detail.getKotStatus())) {
                    detail.setKotStatus("Ready");
                }
            }
        } else if ("Served".equalsIgnoreCase(status)) {
            for (com.kawai.models.FoodOrderDetail detail : order.getDetails()) {
                if ("Ready".equalsIgnoreCase(detail.getKotStatus())) {
                    detail.setKotStatus("Served");
                }
            }

            // Tự động gán cờ thanh toán cho đơn Room-Service khi giao xong (Served)
            String orderTypeStr = order.getOrderType() != null ? order.getOrderType().replace(" ", "") : "";
            if ("RoomService".equalsIgnoreCase(orderTypeStr) || "Room-Svc".equalsIgnoreCase(orderTypeStr)) {
                order.setIsPaidInPos(true);
            }
        }

        foodOrderRepository.save(order);
    }

    public void cancelOrder(Long orderId, com.kawai.dtos.CancelOrderRequestDTO dto) {
        FoodOrder order = foodOrderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("POS-006", "Đơn hàng không tồn tại"));

        if (!"Pending".equalsIgnoreCase(order.getOrderStatus())) {
            throw new BusinessException("POS-007", "Chỉ có thể hủy đơn hàng ở trạng thái Pending");
        }

        order.setOrderStatus("Cancelled");

        // Set kotStatus to Cancelled for all details
        for (com.kawai.models.FoodOrderDetail detail : order.getDetails()) {
            detail.setKotStatus("Cancelled");
        }

        // Handle refund logic based on payment method
        String pType = order.getPaymentType();
        if ("Post to Room".equalsIgnoreCase(pType) || "CHARGE_TO_ROOM".equalsIgnoreCase(pType)
                || "Post_To_Room".equalsIgnoreCase(pType)) {
            java.util.List<com.kawai.models.FolioItem> folios = null;
            if (order.getRoomBookingDetail() != null) {
                folios = folioItemRepository.findByRoomBookingDetailId(order.getRoomBookingDetail().getId());
            } else if (order.getBooking() != null) {
                folios = folioItemRepository.findByBookingId(order.getBooking().getId());
            }
            if (folios != null) {
                for (com.kawai.models.FolioItem folio : folios) {
                    if (folio.getDescription() != null
                            && folio.getDescription().contains("Order #" + order.getId() + ")")) {
                        folioItemRepository.delete(folio);
                    }
                }
            }
        } else if (dto != null && dto.getAccountNumber() != null && !dto.getAccountNumber().isEmpty()) {
            // Online/Card payment: Save refund request
            com.kawai.models.RefundRequest refund = new com.kawai.models.RefundRequest();
            refund.setOrder(order);
            refund.setBankName(dto.getBankName());
            refund.setAccountNumber(dto.getAccountNumber());
            refund.setAccountName(dto.getAccountName());
            refund.setPhoneNumber(dto.getPhoneNumber());
            refund.setAmount(order.getTotalAmount());
            refund.setStatus("Pending");
            refundRequestRepository.save(refund);
        }

        foodOrderRepository.save(order);
    }

    @Override
    public void cancelOrderByGuest(Long orderId, com.kawai.dtos.CancelOrderRequestDTO dto, String username) {
        // [AUTHORIZATION CHECK] Bước 1: Lấy thông tin Khách hàng (Customer) từ username hiện tại
        Customer customer = customerRepository.findByAccount_Username(username)
                .orElseGet(() -> customerRepository.findByEmail(username).orElse(null));
        if (customer == null) {
            throw new BusinessException("CUSTOMER_NOT_FOUND", "Không tìm thấy thông tin khách hàng");
        }
        
        FoodOrder order = foodOrderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("POS-006", "Đơn hàng không tồn tại"));

        // [AUTHORIZATION CHECK] Bước 2: Xác thực quyền sở hữu (IDOR Protection)
        // Chỉ cho phép hủy nếu đơn hàng này được đặt bởi đúng tài khoản Customer đang gửi request
        boolean isOwner = false;
        if (order.getBooking() != null && order.getBooking().getCustomer() != null
                && order.getBooking().getCustomer().getId().equals(customer.getId())) {
            // Đơn hàng gắn với Booking của chính khách này
            isOwner = true;
        } else if (order.getRoomBookingDetail() != null && order.getRoomBookingDetail().getRoomBooking() != null
                && order.getRoomBookingDetail().getRoomBooking().getCustomer() != null
                && order.getRoomBookingDetail().getRoomBooking().getCustomer().getId().equals(customer.getId())) {
            // Đơn hàng gắn với 1 RoomBookingDetail của phòng thuộc Booking của chính khách này
            isOwner = true;
        }

        if (!isOwner) {
            // Chặn đứng nỗ lực gọi API hủy đơn của người khác
            throw new BusinessException("ACCESS_DENIED", "Bạn không có quyền hủy đơn hàng này");
        }

        // [DELEGATION] Bước 3: Đã an toàn -> Chuyển tiếp (delegate) cho Core Logic xử lý.
        // Tuyệt đối không lặp lại code hủy đơn (xử lý KOT, refund, v.v...) ở đây để đảm bảo DRY.
        cancelOrder(orderId, dto);
    }
}
