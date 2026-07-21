package com.kawai.services.impl;

import com.kawai.dto.fnb.FnBDailyReportPreviewResponse;
import com.kawai.dto.fnb.FnBTransactionDto;
import com.kawai.models.Employee;
import com.kawai.models.FnBDailyReport;
import com.kawai.models.FoodOrder;
import com.kawai.repositories.EmployeeRepository;
import com.kawai.repositories.FnBDailyReportRepository;
import com.kawai.repositories.FoodOrderRepository;
import com.kawai.services.interfaces.FnBDailyReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FnBDailyReportServiceImpl implements FnBDailyReportService {

    private final FoodOrderRepository foodOrderRepository;
    private final FnBDailyReportRepository fnBDailyReportRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public FnBDailyReportPreviewResponse previewDailyReport(LocalDate date, Long staffId) {
        // 1. Kiểm tra xem nhân viên chốt ca có tồn tại trong hệ thống không
        Employee staff = employeeRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff not found"));

        // 2. Lấy giới hạn thời gian của ngày hôm nay (từ 00:00:00 đến 23:59:59)
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        // 3. Query toàn bộ các đơn hàng F&B được tạo trong ngày hôm nay
        List<FoodOrder> dailyOrders = foodOrderRepository.findByOrderTimeBetweenOrderByOrderTimeDesc(startOfDay, endOfDay);

        int dineInOrders = 0;
        int roomServiceOrders = 0;
        BigDecimal cashRevenue = BigDecimal.ZERO;
        BigDecimal vnpayRevenue = BigDecimal.ZERO;
        BigDecimal roomChargeRevenue = BigDecimal.ZERO;
        BigDecimal totalRevenue = BigDecimal.ZERO;

        List<FnBTransactionDto> transactions = new ArrayList<>();

        for (FoodOrder order : dailyOrders) {
            String oType = order.getOrderType() != null ? order.getOrderType() : "Unknown";
            String pType = order.getPaymentType() != null ? order.getPaymentType().toUpperCase() : "UNKNOWN";
            
            // Báo cáo doanh thu chỉ quan tâm đơn hàng đã hoàn tất vòng đời tài chính (isPaidInPos) VÀ không bị Hủy/Pending
            boolean isRevenue = Boolean.TRUE.equals(order.getIsPaidInPos()) && 
                                ("Completed".equalsIgnoreCase(order.getOrderStatus()) || 
                                 "Paid".equalsIgnoreCase(order.getOrderStatus()) || 
                                 "Served".equalsIgnoreCase(order.getOrderStatus()));
            
            if ("ROOMSERVICE".equalsIgnoreCase(oType.replace(" ", "")) || "ROOM-SVC".equalsIgnoreCase(oType)) {
                roomServiceOrders++;
            } else {
                dineInOrders++;
            }
                                
            if (isRevenue) {
                BigDecimal amount = order.getTotalAmount();
                totalRevenue = totalRevenue.add(amount);

                // Phân loại dòng tiền theo phương thức thanh toán
                if (pType.contains("VNPAY")) {
                    vnpayRevenue = vnpayRevenue.add(amount); // Tiền khách thanh toán qua hệ thống
                } else if (pType.contains("ROOM") || pType.contains("CHARGE")) {
                    roomChargeRevenue = roomChargeRevenue.add(amount); // Tiền khách nợ, sẽ thanh toán ở lễ tân lúc Check-out
                } else {
                    cashRevenue = cashRevenue.add(amount); // Mặc định các phương thức khác (Tiền mặt, Pay_Later...) vào Tiền mặt
                }
            }

            // Ghi nhận tất cả các bill (kể cả Cancelled/Pending) vào danh sách để staff tiện đối soát nếu lệch tiền
            transactions.add(FnBTransactionDto.builder()
                    .orderId(order.getId())
                    .tableName(order.getTable() != null ? order.getTable().getTableNumber() : null)
                    .roomName(order.getRoomBookingDetail() != null && order.getRoomBookingDetail().getRoom() != null ? order.getRoomBookingDetail().getRoom().getRoomNumber() : null)
                    .orderType(oType)
                    .orderTime(order.getOrderTime())
                    .totalAmount(order.getTotalAmount())
                    .paymentType(order.getPaymentType())
                    .orderStatus(order.getOrderStatus())
                    .build());
        }

        return FnBDailyReportPreviewResponse.builder()
                .reportDate(date)
                .staffName(staff.getFullName())
                .totalDineInOrders(dineInOrders)
                .totalRoomServiceOrders(roomServiceOrders)
                .totalCashRevenue(cashRevenue)
                .totalVnpayRevenue(vnpayRevenue)
                .totalChargeToRoomRevenue(roomChargeRevenue)
                .totalRevenue(totalRevenue)
                .transactions(transactions)
                .build();
    }

    @Override
    @Transactional // Đảm bảo tính toàn vẹn dữ liệu: Nếu lỗi ở bất kỳ dòng nào, toàn bộ quá trình chốt ngày sẽ bị Rollback
    public FnBDailyReport closeDailyReport(LocalDate date, Long staffId, String notes) {
        // Ngăn chặn việc bấm "Chốt ngày" nhiều lần trong cùng 1 ngày
        if (fnBDailyReportRepository.existsByReportDate(date)) {
            throw new IllegalArgumentException("Daily report for date " + date + " is already closed.");
        }

        // Tính toán lại các số liệu mới nhất tại thời điểm bấm chốt
        FnBDailyReportPreviewResponse preview = previewDailyReport(date, staffId);

        FnBDailyReport report = FnBDailyReport.builder()
                .reportDate(date)
                .closedByStaff(employeeRepository.findById(staffId).orElseThrow(() -> new IllegalArgumentException("Staff not found")))
                .totalDineInOrders(preview.getTotalDineInOrders())
                .totalRoomServiceOrders(preview.getTotalRoomServiceOrders())
                .totalCashRevenue(preview.getTotalCashRevenue())
                .totalVnpayRevenue(preview.getTotalVnpayRevenue())
                .totalChargeToRoomRevenue(preview.getTotalChargeToRoomRevenue())
                .totalRevenue(preview.getTotalRevenue())
                .closedAt(LocalDateTime.now())
                .notes(notes) // Ghi chú của nhân viên (ví dụ: "Tiền trong két bị thiếu 50k, em đã bù tiền túi")
                .build();
                
        // Lưu trữ lại Snapshot của ngày hôm nay vào Database. 
        // Sau này Manager chỉ cần chọc vào bảng này để vẽ Biểu đồ doanh thu thay vì phải quét hàng ngàn bill cũ.
        return fnBDailyReportRepository.save(report);
    }
}
