package com.kawai.services;

import com.kawai.dto.fnb.FnBDailyReportPreviewResponse;
import com.kawai.models.Employee;
import com.kawai.models.FnBDailyReport;
import com.kawai.models.FoodOrder;
import com.kawai.repositories.EmployeeRepository;
import com.kawai.repositories.FnBDailyReportRepository;
import com.kawai.repositories.FoodOrderRepository;
import com.kawai.services.impl.FnBDailyReportServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UC18 — Chốt ca F&B")
class FnBDailyReportServiceUC18Test {

    @InjectMocks
    private FnBDailyReportServiceImpl dailyReportService;

    @Mock
    private FoodOrderRepository foodOrderRepository;

    @Mock
    private FnBDailyReportRepository fnBDailyReportRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Test
    @DisplayName("TC-UC18-01 | Phân loại chính xác doanh thu theo loại đơn và thanh toán")
    void testPreviewDailyReport_RevenueCategorization() {
        // Arrange
        LocalDate today = LocalDate.now();
        Long staffId = 1L;
        Employee mockStaff = new Employee();
        mockStaff.setId(staffId);
        mockStaff.setFullName("Nguyen Van A");

        FoodOrder order1 = spy(new FoodOrder());
        order1.setId(1L);
        order1.setOrderType("Dine In");
        order1.setPaymentType("CASH");
        order1.setIsPaidInPos(true);
        order1.setOrderStatus("Completed");
        doReturn(new BigDecimal("1000")).when(order1).getTotalAmount();

        FoodOrder order2 = spy(new FoodOrder());
        order2.setId(2L);
        order2.setOrderType("Room Service");
        order2.setPaymentType("CHARGE_TO_ROOM");
        order2.setIsPaidInPos(true);
        order2.setOrderStatus("Served");
        doReturn(new BigDecimal("2000")).when(order2).getTotalAmount();

        when(employeeRepository.findById(staffId)).thenReturn(Optional.of(mockStaff));
        when(foodOrderRepository.findByOrderTimeBetweenOrderByOrderTimeDesc(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(order1, order2));

        // Act
        FnBDailyReportPreviewResponse result = dailyReportService.previewDailyReport(today, staffId);

        // Assert
        assertEquals(1, result.getTotalDineInOrders());
        assertEquals(1, result.getTotalRoomServiceOrders());
        assertEquals(new BigDecimal("1000"), result.getTotalCashRevenue());
        assertEquals(new BigDecimal("2000"), result.getTotalChargeToRoomRevenue());
        assertEquals(new BigDecimal("3000"), result.getTotalRevenue());
        assertEquals(2, result.getTransactions().size());
    }

    @Test
    @DisplayName("TC-UC18-02 | Chốt ca thành công và lưu db")
    void testCloseDailyReport_Success() {
        // Arrange
        LocalDate today = LocalDate.now();
        Long staffId = 1L;
        String notes = "Kết ca bình thường";

        Employee mockStaff = new Employee();
        mockStaff.setId(staffId);

        when(fnBDailyReportRepository.existsByReportDate(today)).thenReturn(false);
        when(employeeRepository.findById(staffId)).thenReturn(Optional.of(mockStaff));
        when(foodOrderRepository.findByOrderTimeBetweenOrderByOrderTimeDesc(any(), any())).thenReturn(Arrays.asList());
        
        FnBDailyReport savedReport = new FnBDailyReport();
        savedReport.setId(10L);
        savedReport.setNotes(notes);
        when(fnBDailyReportRepository.save(any(FnBDailyReport.class))).thenReturn(savedReport);

        // Act
        FnBDailyReport result = dailyReportService.closeDailyReport(today, staffId, notes);

        // Assert
        assertNotNull(result);
        assertEquals(notes, result.getNotes());
        verify(fnBDailyReportRepository, times(1)).save(any(FnBDailyReport.class));
    }

    @Test
    @DisplayName("TC-UC18-03 | Exception khi chốt ca 2 lần 1 ngày")
    void testCloseDailyReport_AlreadyClosed() {
        // Arrange
        LocalDate today = LocalDate.now();
        when(fnBDailyReportRepository.existsByReportDate(today)).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            dailyReportService.closeDailyReport(today, 1L, "");
        });

        assertTrue(exception.getMessage().contains("already closed"));
    }
}
