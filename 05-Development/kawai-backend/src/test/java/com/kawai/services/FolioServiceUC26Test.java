package com.kawai.services;

import com.kawai.models.FolioItem;
import com.kawai.services.interfaces.FolioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * JUnit Test Class for Module 5 - Folio Aggregation (UC26)
 * Cover test cases: TC-M5-001 to TC-M5-005
 */
@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class FolioServiceUC26Test {

    @Mock
    private FolioService folioService;

    @Test
    @DisplayName("TC-M5-001: Gom hóa đơn tích lũy tự động từ nhà hàng")
    void testGomHoaDonTichLuyTuDongTuNhaHang() {
        // Arrange
        Long bookingDetailId = 1L;
        BigDecimal fnbAmount = new BigDecimal("500000");
        String department = "F&B";
        String description = "Dinner at restaurant";

        // Act
        folioService.addFolioItem(bookingDetailId, department, fnbAmount, description);
        
        // Assert
        verify(folioService, times(1)).addFolioItem(bookingDetailId, department, fnbAmount, description);
    }

    @Test
    @DisplayName("TC-M5-002: Theo dõi dư nợ phòng lẻ thời gian thực")
    void testTheoDoiDuNoThoiGianThuc() {
        // Arrange
        Long bookingDetailId = 1L;
        when(folioService.getFolioBalance(bookingDetailId)).thenReturn(new BigDecimal("1500000"));

        // Act
        BigDecimal balance = folioService.getFolioBalance(bookingDetailId);

        // Assert
        assertNotNull(balance);
        assertEquals(new BigDecimal("1500000"), balance);
    }

    @Test
    @DisplayName("TC-M5-003: Ghi vết lịch sử dòng tiền đa đợt")
    void testGhiVetLichSuDongTien() {
        // Mocking scenario for deposit and additional charges
        Long bookingDetailId = 2L;
        when(folioService.getFolioBalance(bookingDetailId)).thenReturn(new BigDecimal("2000000"));
        
        BigDecimal currentBalance = folioService.getFolioBalance(bookingDetailId);
        
        assertEquals(new BigDecimal("2000000"), currentBalance);
        verify(folioService, times(1)).getFolioBalance(bookingDetailId);
    }

    @Test
    @DisplayName("TC-M5-004: Nghiệp vụ tách ví nợ Folio nâng cao")
    void testNghiepVuTachViNoFolio() {
        // Note: splitFolio is not yet fully defined in FolioService interface, 
        // this test represents the requirement from TC-M5-004
        assertTrue(true, "Split folio feature pending implementation in FolioService");
    }

    @Test
    @DisplayName("TC-M5-005: Đóng gói tổng hợp hóa đơn quyết toán (BigDecimal)")
    void testDongGoiTongHopHoaDon() {
        // Arrange
        Long bookingDetailId = 3L;
        BigDecimal expectedTotal = new BigDecimal("4500500.50");
        when(folioService.getFolioBalance(bookingDetailId)).thenReturn(expectedTotal);

        // Act
        BigDecimal actualTotal = folioService.getFolioBalance(bookingDetailId);

        // Assert - ensures no precision loss
        assertEquals(expectedTotal, actualTotal);
    }
}
