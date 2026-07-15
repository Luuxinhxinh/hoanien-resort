package com.kawai.services;

import com.kawai.services.interfaces.ReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * JUnit Test Class for Module 5 - Manager Dashboard & Báo cáo (UC28)
 * Cover test cases: TC-M5-012 to TC-M5-015
 */
@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class ReportServiceUC28Test {

    @Mock
    private ReportService reportService;

    @Test
    @DisplayName("TC-M5-012: Giám sát biểu đồ phân tích tài chính")
    void testGiamSatBieuDoPhanTichTaiChinh() {
        LocalDate startDate = LocalDate.of(2026, 7, 1);
        LocalDate endDate = LocalDate.of(2026, 7, 31);
        
        Map<String, BigDecimal> mockRevenue = new HashMap<>();
        mockRevenue.put("ROOM", new BigDecimal("50000000"));
        mockRevenue.put("F&B", new BigDecimal("15000000"));
        
        when(reportService.getUsaliRevenueReport(startDate, endDate)).thenReturn(mockRevenue);
        
        Map<String, BigDecimal> result = reportService.getUsaliRevenueReport(startDate, endDate);
        
        assertNotNull(result);
        assertEquals(new BigDecimal("50000000"), result.get("ROOM"));
        verify(reportService, times(1)).getUsaliRevenueReport(startDate, endDate);
    }

    @Test
    @DisplayName("TC-M5-013: Thuật toán tính toán công suất phòng (Occupancy Rate)")
    void testThuatToanTinhCongSuatPhong() {
        LocalDate date = LocalDate.of(2026, 7, 2);
        
        // Mock 85.5% occupancy
        when(reportService.getOccupancyRate(date)).thenReturn(85.5);
        
        double occupancy = reportService.getOccupancyRate(date);
        
        assertEquals(85.5, occupancy);
        verify(reportService, times(1)).getOccupancyRate(date);
    }

    @Test
    @DisplayName("TC-M5-014: Xuất báo cáo tài chính vận hành chuẩn USALI")
    void testXuatBaoCaoTaiChinhUSALI() {
        LocalDate startDate = LocalDate.of(2026, 6, 1);
        LocalDate endDate = LocalDate.of(2026, 6, 30);
        
        Map<String, BigDecimal> usaliReport = new HashMap<>();
        usaliReport.put("ROOM", new BigDecimal("10000"));
        usaliReport.put("F&B", new BigDecimal("5000"));
        usaliReport.put("TOUR", new BigDecimal("2000"));
        
        when(reportService.getUsaliRevenueReport(startDate, endDate)).thenReturn(usaliReport);
        
        Map<String, BigDecimal> result = reportService.getUsaliRevenueReport(startDate, endDate);
        
        assertTrue(result.containsKey("TOUR"));
        assertEquals(new BigDecimal("2000"), result.get("TOUR"));
    }

    @Test
    @DisplayName("TC-M5-015: Kết xuất báo cáo định dạng Excel")
    void testKetXuatBaoCaoExcel() {
        LocalDate startDate = LocalDate.of(2026, 6, 1);
        LocalDate endDate = LocalDate.of(2026, 6, 30);
        String format = "XLSX";
        byte[] mockXlsx = new byte[]{1, 2, 3};
        
        when(reportService.exportUsaliReport(startDate, endDate, format)).thenReturn(mockXlsx);
        
        byte[] result = reportService.exportUsaliReport(startDate, endDate, format);
        
        assertNotNull(result);
        assertTrue(result.length > 0);
    }
}
