package com.kawai.services.impl;

import com.kawai.models.*;
import com.kawai.repositories.*;
import com.kawai.services.interfaces.KdsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class KdsServiceImpl implements KdsService {

    @Autowired
    private TableReservationRepository tableReservationRepository;

    @Autowired
    private RestaurantTableRepository restaurantTableRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private FoodOrderRepository foodOrderRepository;

    @Autowired
    private FoodOrderDetailRepository foodOrderDetailRepository;

    @Autowired
    private FoodItemRepository foodItemRepository;

    @Autowired
    private RoomBookingDetailRepository roomBookingDetailRepository;

    @Autowired
    private FolioItemRepository folioItemRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    @Transactional
    public TableReservation createTableReservation(Long customerId, Long tableId, LocalDate date, LocalTime time,
            BigDecimal depositAmount) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Khách hàng không tồn tại"));
        RestaurantTable table = restaurantTableRepository.findById(tableId)
                .orElseThrow(() -> new IllegalArgumentException("Bàn không tồn tại"));

        TableReservation res = new TableReservation();
        res.setCustomer(customer);
        res.setTable(table);
        res.setReserveDate(date);
        res.setReserveTime(time);
        res.setDepositAmount(depositAmount != null ? depositAmount : BigDecimal.ZERO);
        res.setStatus("Pending");

        // Đánh dấu bàn là đã được đặt trước
        table.setTableStatus("Reserved");
        restaurantTableRepository.save(table);

        return tableReservationRepository.save(res);
    }

    @Override
    @Transactional
    public void checkAndCancelExpiredReservations() {
        List<TableReservation> pending = tableReservationRepository.findAll().stream()
                .filter(r -> "Pending".equalsIgnoreCase(r.getStatus()))
                .toList();

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        for (TableReservation res : pending) {
            boolean isExpired = false;
            if (res.getReserveDate().isBefore(today)) {
                isExpired = true;
            } else if (res.getReserveDate().isEqual(today)) {
                if (res.getReserveTime().plusMinutes(30).isBefore(now)) {
                    isExpired = true;
                }
            }

            if (isExpired) {
                res.setStatus("Hủy do quá hạn");
                tableReservationRepository.save(res);

                // Giải phóng bàn khi reservation bị hủy
                if (res.getTable() != null) {
                    res.getTable().setTableStatus("Vacant");
                    restaurantTableRepository.save(res.getTable());
                }
            }
        }
    }

    @Override
    @Transactional
    public FoodOrder createFoodOrder(Long bookingDetailId, Long tableId, String orderType, List<FoodOrderDetail> items,
            Long staffId) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("POS-001: Đơn hàng trống — không có món");
        }

        Employee staff = employeeRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Nhân viên không tồn tại"));

        RoomBookingDetail roomDetail = null;
        if (bookingDetailId != null) {
            roomDetail = roomBookingDetailRepository.findById(bookingDetailId)
                    .orElseThrow(() -> new IllegalArgumentException("Chi tiết đặt phòng không tồn tại"));

            // BR-FB-01 (1): Room must be Occupied
            Room room = roomDetail.getRoom();
            if (room == null || !"OCCUPIED".equalsIgnoreCase(room.getRoomStatus())) {
                throw new IllegalStateException("ROOM-001: Phòng không ở trạng thái OCCUPIED");
            }
        }

        RestaurantTable table = null;
        if (tableId != null) {
            table = restaurantTableRepository.findById(tableId).orElse(null);
        }

        FoodOrder order = new FoodOrder();
        order.setRoomBookingDetail(roomDetail);
        order.setTable(table);
        order.setOrderType(orderType);
        order.setOrderStatus("Pending");
        order.setCreatedByStaff(staff);
        order.setPaymentType("RoomCharge");
        order = foodOrderRepository.save(order);

        BigDecimal subTotal = BigDecimal.ZERO;
        List<FoodOrderDetail> savedDetails = new ArrayList<>();

        for (FoodOrderDetail detail : items) {
            MenuItem menuItem = foodItemRepository.findById(detail.getMenuItem().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Món ăn không tồn tại"));

            // BR-FB-02: Check item availability
            if (menuItem.getIsAvailable() != null && !menuItem.getIsAvailable()) {
                throw new IllegalStateException("POS-002: Món đã hết — không thể order");
            }

            FoodOrderDetail savedDetail = new FoodOrderDetail();
            savedDetail.setFoodOrder(order);
            savedDetail.setMenuItem(menuItem);
            savedDetail.setQuantity(detail.getQuantity());
            savedDetail.setPriceAtOrder(menuItem.getPrice());
            savedDetail.setKotStatus("Pending");

            subTotal = subTotal.add(menuItem.getPrice().multiply(BigDecimal.valueOf(detail.getQuantity())));
            savedDetails.add(foodOrderDetailRepository.save(savedDetail));
        }

        order.setDetails(savedDetails);

        BigDecimal total = subTotal;
        // BR-FB-05: 10% room service fee
        if ("RoomService".equalsIgnoreCase(orderType)) {
            BigDecimal fee = subTotal.multiply(new BigDecimal("0.10"));
            total = subTotal.add(fee);
        }

        // BR-FB-01 (3): Credit limit check
        if (roomDetail != null) {
            BigDecimal currentFolioBalance = BigDecimal.ZERO;
            List<FolioItem> folioItems = folioItemRepository.findAll().stream()
                    .filter(fi -> fi.getRoomBookingDetail() != null
                            && fi.getRoomBookingDetail().getId().equals(bookingDetailId))
                    .toList();
            for (FolioItem fi : folioItems) {
                currentFolioBalance = currentFolioBalance.add(fi.getAmount());
            }

            BigDecimal limit = roomDetail.getSubCreditLimit() != null ? roomDetail.getSubCreditLimit()
                    : BigDecimal.ZERO;
            if (currentFolioBalance.add(total).compareTo(limit) > 0) {
                throw new IllegalStateException("POS-003: Vượt hạn mức tín dụng phòng (Credit Limit)");
            }

            // Post to folio
            FolioItem folioItem = new FolioItem();
            folioItem.setRoomBookingDetail(roomDetail);
            folioItem.setSourceDepartment("POS");
            folioItem.setAmount(total);
            folioItem.setDescription("Ăn uống Room Service - Order #" + order.getId());
            folioItem.setPayerCustomer(roomDetail.getRoomBooking().getCustomer());
            folioItem.setBooking(roomDetail.getRoomBooking());
            if ("RoomService".equalsIgnoreCase(orderType)) {
                folioItem.setRevenueCode("FB_ROOMSERVICE");
            } else {
                folioItem.setRevenueCode("FB_FOOD");
            }
            folioItemRepository.save(folioItem);
        }

        return order;
    }

    @Override
    @Transactional
    public void updateKitchenStatus(Long detailId, String status) {
        FoodOrderDetail detail = foodOrderDetailRepository.findById(detailId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy order detail"));
        detail.setKotStatus(status);
        foodOrderDetailRepository.save(detail);

        FoodOrder order = detail.getFoodOrder();
        if (order != null) {
            boolean allCompleted = true;
            boolean anyPreparing = false;
            for (FoodOrderDetail od : order.getDetails()) {
                if (!"Completed".equalsIgnoreCase(od.getKotStatus())) {
                    allCompleted = false;
                }
                if ("Preparing".equalsIgnoreCase(od.getKotStatus())) {
                    anyPreparing = true;
                }
            }

            if (allCompleted) {
                order.setOrderStatus("Completed");
            } else if (anyPreparing) {
                order.setOrderStatus("Preparing");
            }
            foodOrderRepository.save(order);
        }
    }

    @Override
    @Transactional
    public void markItemUnavailable(Long itemId) {
        MenuItem menuItem = foodItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy món ăn"));
        menuItem.setIsAvailable(false);
        foodItemRepository.save(menuItem);
    }

    @Override
    @Transactional
    public void cancelFoodOrderDetail(Long detailId, String reason, Long staffId) {
        FoodOrderDetail detail = foodOrderDetailRepository.findById(detailId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy order detail"));

        if ("Preparing".equalsIgnoreCase(detail.getKotStatus())
                || "Completed".equalsIgnoreCase(detail.getKotStatus())) {
            throw new IllegalStateException("POS-004: Không thể sửa/xóa — order đã vào bếp");
        }

        Employee staff = employeeRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Nhân viên không tồn tại"));

        detail.setKotStatus("Cancelled");
        foodOrderDetailRepository.save(detail);

        // Audit Log
        AuditLog log = new AuditLog();
        log.setAction("CANCEL_ORDER_ITEM");
        log.setTableName("Food_Order_Details");
        log.setRecordId(detail.getId());
        log.setOldValue("Pending");
        log.setNewValue("Cancelled (Reason: " + reason + ")");
        log.setIpAddress("127.0.0.1");
        log.setTimestamp(LocalDateTime.now());
        if (staff.getAccount() != null) {
            log.setAccount(staff.getAccount());
        }
        auditLogRepository.save(log);
    }
}
