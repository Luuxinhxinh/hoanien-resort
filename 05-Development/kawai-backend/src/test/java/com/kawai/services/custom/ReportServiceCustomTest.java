package com.kawai.services.custom;

import com.kawai.models.*;
import com.kawai.repositories.*;
import com.kawai.services.impl.ReportServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReportServiceCustomTest {

    @Mock
    private FolioItemRepository folioItemRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private RoomBookingDetailRepository roomBookingDetailRepository;

    @InjectMocks
    private ReportServiceImpl reportService;

    @Test
    void testGetUsaliRevenueReport() {
        FolioItem roomItem = new FolioItem();
        roomItem.setSourceDepartment("Rooms");
        roomItem.setAmount(new BigDecimal("1000000"));
        roomItem.setCreatedAt(LocalDateTime.now());
        roomItem.setRevenueCode("ROOM_TRANSIENT");

        FolioItem fbItem = new FolioItem();
        fbItem.setSourceDepartment("RoomService");
        fbItem.setAmount(new BigDecimal("350000"));
        fbItem.setCreatedAt(LocalDateTime.now());
        fbItem.setRevenueCode("FB_ROOMSERVICE");

        FolioItem tourItem = new FolioItem();
        tourItem.setSourceDepartment("Tour");
        tourItem.setAmount(new BigDecimal("1200000"));
        tourItem.setCreatedAt(LocalDateTime.now());
        tourItem.setRevenueCode("OTH_TOUR");

        FolioItem payment = new FolioItem();
        payment.setSourceDepartment("PAYMENT");
        payment.setAmount(new BigDecimal("500000"));
        payment.setCreatedAt(LocalDateTime.now());

        when(folioItemRepository.findAll()).thenReturn(Arrays.asList(roomItem, fbItem, tourItem, payment));

        Map<String, BigDecimal> report = reportService.getUsaliRevenueReport(LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));

        assertNotNull(report);
        assertEquals(new BigDecimal("1000000"), report.get("REV-ROOM"));
        assertEquals(new BigDecimal("350000"), report.get("REV-FB"));
        assertEquals(new BigDecimal("1200000"), report.get("REV-TOUR"));
        assertEquals(new BigDecimal("2550000"), report.get("TOTAL"));
    }

    @Test
    void testGetOccupancyRate() {
        Room r1 = new Room();
        r1.setRoomStatus("Occupied");
        Room r2 = new Room();
        r2.setRoomStatus("Vacant_Clean");

        when(roomRepository.count()).thenReturn(2L);
        when(roomRepository.findAll()).thenReturn(Arrays.asList(r1, r2));

        double rate = reportService.getOccupancyRate(LocalDate.now());

        assertEquals(50.0, rate);
    }

    @Test
    void testExportUsaliReport() {
        FolioItem roomItem = new FolioItem();
        roomItem.setSourceDepartment("Rooms");
        roomItem.setAmount(new BigDecimal("1000000"));
        roomItem.setCreatedAt(LocalDateTime.now());
        roomItem.setRevenueCode("ROOM_TRANSIENT");

        when(folioItemRepository.findAll()).thenReturn(Collections.singletonList(roomItem));

        byte[] reportBytes = reportService.exportUsaliReport(LocalDate.now().minusDays(1), LocalDate.now().plusDays(1), "xlsx");

        assertNotNull(reportBytes);
        assertTrue(reportBytes.length > 2);
        assertEquals('P', (char) reportBytes[0]);
        assertEquals('K', (char) reportBytes[1]);
    }

    @Test
    void testExportUsaliReportPdf() {
        assertThrows(com.kawai.exceptions.BusinessException.class, () -> {
            reportService.exportUsaliReport(LocalDate.now().minusDays(1), LocalDate.now().plusDays(1), "pdf");
        });
    }

    @Test
    void testExportUsaliReportXlsx() {
        FolioItem roomItem = new FolioItem();
        roomItem.setSourceDepartment("Rooms");
        roomItem.setAmount(new BigDecimal("1000000"));
        roomItem.setCreatedAt(LocalDateTime.now());
        roomItem.setRevenueCode("ROOM_TRANSIENT");

        when(folioItemRepository.findAll()).thenReturn(Collections.singletonList(roomItem));

        byte[] reportBytes = reportService.exportUsaliReport(LocalDate.now().minusDays(1), LocalDate.now().plusDays(1), "XLSX");

        assertNotNull(reportBytes);
        assertTrue(reportBytes.length > 2);
        // XLSX/ZIP starts with PK
        assertEquals('P', (char) reportBytes[0]);
        assertEquals('K', (char) reportBytes[1]);
    }

    @Test
    void testExportReportRoomAndFnb() {
        FolioItem roomItem = new FolioItem();
        roomItem.setSourceDepartment("Rooms");
        roomItem.setAmount(new BigDecimal("1000000"));
        roomItem.setCreatedAt(LocalDateTime.now());
        roomItem.setRevenueCode("ROOM_TRANSIENT");

        when(folioItemRepository.findAll()).thenReturn(Collections.singletonList(roomItem));

        // Test room report XLSX
        byte[] roomXlsx = reportService.exportReport("room", LocalDate.now().minusDays(1), LocalDate.now().plusDays(1), "xlsx");
        assertNotNull(roomXlsx);
        assertTrue(roomXlsx.length > 2);
        assertEquals('P', (char) roomXlsx[0]);
        assertEquals('K', (char) roomXlsx[1]);

        // Test fnb report XLSX
        byte[] fnbXlsx = reportService.exportReport("fnb", LocalDate.now().minusDays(1), LocalDate.now().plusDays(1), "xlsx");
        assertNotNull(fnbXlsx);
        assertTrue(fnbXlsx.length > 2);
        assertEquals('P', (char) fnbXlsx[0]);
        assertEquals('K', (char) fnbXlsx[1]);

        // Test invalid format throws exception
        assertThrows(com.kawai.exceptions.BusinessException.class, () -> {
            reportService.exportReport("tour", LocalDate.now().minusDays(1), LocalDate.now().plusDays(1), "pdf");
        });
    }
}
