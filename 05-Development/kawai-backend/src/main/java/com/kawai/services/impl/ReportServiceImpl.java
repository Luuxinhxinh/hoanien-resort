package com.kawai.services.impl;

import com.kawai.models.FolioItem;
import com.kawai.models.RoomBookingDetail;
import com.kawai.repositories.FolioItemRepository;
import com.kawai.repositories.RoomBookingDetailRepository;
import com.kawai.repositories.RoomRepository;
import com.kawai.services.interfaces.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.util.Locale;

// POI imports
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

// OpenPDF imports
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import java.awt.Color;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private FolioItemRepository folioItemRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private RoomBookingDetailRepository roomBookingDetailRepository;

    @Autowired
    private com.kawai.services.jobs.RoomBookingCleanupTask roomBookingCleanupTask;

    private static class ReportRow {
        String label;
        String key;
        int type; // 0 = Section Header, 1 = Sub-item, 2 = Dept Total, 3 = Grand Total, 4 = KPI Section Header, 5 = KPI item

        ReportRow(String label, String key, int type) {
            this.label = label;
            this.key = key;
            this.type = type;
        }
    }

    private List<ReportRow> getReportRowsDefinition() {
        return List.of(
            new ReportRow("I. DOANH THU PHÒNG", null, 0),
            new ReportRow("  - Khách lẻ (Transient)", "REV-ROOM-TRANSIENT", 1),
            new ReportRow("  - Khách đoàn (Group)", "REV-ROOM-GROUP", 1),
            new ReportRow("Tổng doanh thu phòng", "REV-ROOM", 2),
            
            new ReportRow("II. DOANH THU ẨM THỰC (F&B)", null, 0),
            new ReportRow("  - Đồ ăn (Food)", "REV-FB-FOOD", 1),
            new ReportRow("  - Đồ uống (Beverage)", "REV-FB-BEVERAGE", 1),
            new ReportRow("  - Minibar", "REV-FB-MINIBAR", 1),
            new ReportRow("  - Ăn uống tại phòng (Room Service)", "REV-FB-ROOMSERVICE", 1),
            new ReportRow("Tổng doanh thu ẩm thực (F&B)", "REV-FB", 2),
            
            new ReportRow("III. DOANH THU CÁC BỘ PHẬN KHÁC", null, 0),
            new ReportRow("  - Dịch vụ Spa", "REV-OTHER-SPA", 1),
            new ReportRow("  - Dịch vụ Giặt là", "REV-OTHER-LAUNDRY", 1),
            new ReportRow("  - Dịch vụ Tour du lịch", "REV-TOUR", 1),
            new ReportRow("  - Doanh thu khác", "REV-OTHER-MISC", 1),
            new ReportRow("Tổng doanh thu bộ phận khác", "REV-OTHER-TOTAL", 2),
            
            new ReportRow("TỔNG DOANH THU HOẠT ĐỘNG", "TOTAL", 3),
            
            new ReportRow("IV. CHỈ SỐ HOẠT ĐỘNG CHỦ CHỐT (KPIs)", null, 4),
            new ReportRow("  - Tỷ lệ lấp đầy (%)", "KPI-OCCUPANCY", 5),
            new ReportRow("  - ADR (Giá phòng trung bình)", "KPI-ADR", 5),
            new ReportRow("  - RevPAR (Doanh thu trên phòng trống)", "KPI-REVPAR", 5),
            new ReportRow("  - TrevPAR (Tổng doanh thu trên phòng trống)", "KPI-TREVPAR", 5)
        );
    }

    private Map<String, BigDecimal> calculateActualsForPeriod(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        List<FolioItem> items = folioItemRepository.findAll().stream()
                .filter(item -> item.getCreatedAt() != null && !item.getCreatedAt().isBefore(start) && !item.getCreatedAt().isAfter(end))
                .toList();

        BigDecimal transientRoom = BigDecimal.ZERO;
        BigDecimal groupRoom = BigDecimal.ZERO;

        BigDecimal foodFb = BigDecimal.ZERO;
        BigDecimal bevFb = BigDecimal.ZERO;
        BigDecimal minibarFb = BigDecimal.ZERO;
        BigDecimal serviceFb = BigDecimal.ZERO;

        BigDecimal spaOther = BigDecimal.ZERO;
        BigDecimal laundryOther = BigDecimal.ZERO;
        BigDecimal tourOther = BigDecimal.ZERO;
        BigDecimal miscellaneousRevenue = BigDecimal.ZERO;

        for (FolioItem item : items) {
            String dept = item.getSourceDepartment();
            if (dept == null) continue;

            BigDecimal amount = item.getAmount() != null ? item.getAmount() : BigDecimal.ZERO;
            if ("PAYMENT".equalsIgnoreCase(dept) || amount.compareTo(BigDecimal.ZERO) < 0) {
                continue;
            }

            String code = item.getRevenueCode();
            if (code == null) {
                miscellaneousRevenue = miscellaneousRevenue.add(amount);
                continue;
            }

            switch (code) {
                case "ROOM_TRANSIENT":
                    transientRoom = transientRoom.add(amount);
                    break;
                case "ROOM_GROUP":
                    groupRoom = groupRoom.add(amount);
                    break;
                case "FB_FOOD":
                    foodFb = foodFb.add(amount);
                    break;
                case "FB_BEV":
                case "FB_BEVERAGE":
                    bevFb = bevFb.add(amount);
                    break;
                case "FB_MINIBAR":
                    minibarFb = minibarFb.add(amount);
                    break;
                case "FB_ROOMSERVICE":
                    serviceFb = serviceFb.add(amount);
                    break;
                case "OTH_SPA":
                    spaOther = spaOther.add(amount);
                    break;
                case "OTH_LAUNDRY":
                    laundryOther = laundryOther.add(amount);
                    break;
                case "OTH_TOUR":
                case "REV-TOUR":
                    tourOther = tourOther.add(amount);
                    break;
                default:
                    miscellaneousRevenue = miscellaneousRevenue.add(amount);
                    break;
            }
        }

        BigDecimal totalRoom = transientRoom.add(groupRoom);
        BigDecimal totalFb = foodFb.add(bevFb).add(minibarFb).add(serviceFb);
        BigDecimal totalOther = spaOther.add(laundryOther).add(tourOther).add(miscellaneousRevenue);
        BigDecimal totalRevenue = totalRoom.add(totalFb).add(totalOther);

        long totalRooms = roomRepository.count();
        if (totalRooms == 0) totalRooms = 50;

        long daysInPeriod = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        long totalAvailableRoomNights = totalRooms * daysInPeriod;

        long occupiedRoomNights = 0;
        try {
            List<RoomBookingDetail> activeDetails = roomBookingDetailRepository.findAll().stream()
                .filter(d -> "Checked_In".equalsIgnoreCase(d.getDetailStatus()) || "Checked_Out".equalsIgnoreCase(d.getDetailStatus()) || "Completed".equalsIgnoreCase(d.getDetailStatus()))
                .toList();

            for (RoomBookingDetail d : activeDetails) {
                if (d.getRoomBooking() == null || d.getRoomBooking().getCheckInDate() == null || d.getRoomBooking().getCheckOutDate() == null) {
                    continue;
                }
                LocalDate ci = d.getRoomBooking().getCheckInDate();
                LocalDate co = d.getRoomBooking().getCheckOutDate();

                LocalDate overlapStart = ci.isAfter(startDate) ? ci : startDate;
                LocalDate overlapEnd = co.isBefore(endDate) ? co : endDate;

                if (!overlapStart.isAfter(overlapEnd)) {
                    long overlapDays = java.time.temporal.ChronoUnit.DAYS.between(overlapStart, overlapEnd);
                    if (overlapDays > 0) {
                        occupiedRoomNights += overlapDays;
                    }
                }
            }
        } catch (Exception e) {
            // ignore
        }

        if (occupiedRoomNights > totalAvailableRoomNights) {
            occupiedRoomNights = totalAvailableRoomNights;
        }

        double occupancyRate = totalAvailableRoomNights > 0 ? ((double) occupiedRoomNights / totalAvailableRoomNights) * 100.0 : 0.0;
        BigDecimal occupiedNightsBd = BigDecimal.valueOf(occupiedRoomNights);
        BigDecimal availableNightsBd = BigDecimal.valueOf(totalAvailableRoomNights);

        BigDecimal adr = occupiedRoomNights > 0 ? totalRoom.divide(occupiedNightsBd, 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal revPar = totalAvailableRoomNights > 0 ? totalRoom.divide(availableNightsBd, 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal trevPar = totalAvailableRoomNights > 0 ? totalRevenue.divide(availableNightsBd, 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        Map<String, BigDecimal> actuals = new HashMap<>();
        actuals.put("REV-ROOM-TRANSIENT", transientRoom);
        actuals.put("REV-ROOM-GROUP", groupRoom);
        actuals.put("REV-ROOM", totalRoom);

        actuals.put("REV-FB-FOOD", foodFb);
        actuals.put("REV-FB-BEVERAGE", bevFb);
        actuals.put("REV-FB-MINIBAR", minibarFb);
        actuals.put("REV-FB-ROOMSERVICE", serviceFb);
        actuals.put("REV-FB", totalFb);

        actuals.put("REV-OTHER-SPA", spaOther);
        actuals.put("REV-OTHER-LAUNDRY", laundryOther);
        actuals.put("REV-TOUR", tourOther);
        actuals.put("REV-OTHER-MISC", miscellaneousRevenue);
        actuals.put("REV-OTHER-TOTAL", totalOther);

        actuals.put("TOTAL", totalRevenue);

        actuals.put("KPI-OCCUPANCY", BigDecimal.valueOf(occupancyRate).setScale(2, RoundingMode.HALF_UP));
        actuals.put("KPI-ADR", adr);
        actuals.put("KPI-REVPAR", revPar);
        actuals.put("KPI-TREVPAR", trevPar);

        return actuals;
    }

    @Override
    public Map<String, BigDecimal> getUsaliRevenueReport(LocalDate startDate, LocalDate endDate) {
        Map<String, BigDecimal> actuals = calculateActualsForPeriod(startDate, endDate);
        Map<String, BigDecimal> lastYear = calculateActualsForPeriod(startDate.minusYears(1), endDate.minusYears(1));

        Map<String, BigDecimal> report = new HashMap<>(actuals);

        for (String key : actuals.keySet()) {
            BigDecimal actualValue = actuals.get(key);
            
            BigDecimal lyVal = lastYear.getOrDefault(key, BigDecimal.ZERO);
            report.put(key + "-LASTYEAR", lyVal);

            // Keep budget calculation as 1.1 times actual for fallback compatibility
            if (key.startsWith("KPI-OCCUPANCY")) {
                double act = actualValue.doubleValue();
                double budgetVal = Math.min(100.0, act * 1.1);
                report.put(key + "-BUDGET", BigDecimal.valueOf(budgetVal).setScale(2, RoundingMode.HALF_UP));
            } else {
                report.put(key + "-BUDGET", actualValue.multiply(new BigDecimal("1.10")).setScale(2, RoundingMode.HALF_UP));
            }
        }

        return report;
    }

    @Override
    public double getOccupancyRate(LocalDate date) {
        long totalRooms = roomRepository.count();
        if (totalRooms == 0) return 0.0;

        long occupiedRooms = roomRepository.findAll().stream()
                .filter(r -> "Occupied".equalsIgnoreCase(r.getRoomStatus()))
                .count();

        return ((double) occupiedRooms / totalRooms) * 100.0;
    }

    @Override
    public long getGuestCount(LocalDate date) {
        return roomBookingDetailRepository.findAll().stream()
                .filter(d -> "CHECKED_IN".equalsIgnoreCase(d.getDetailStatus()))
                .count();
    }

    @Override
    public byte[] exportUsaliReport(LocalDate startDate, LocalDate endDate, String format) {
        if (!"xlsx".equalsIgnoreCase(format)) {
            throw new com.kawai.exceptions.BusinessException("REPORT-001", "Hệ thống chỉ hỗ trợ xuất báo cáo định dạng Excel (.xlsx)!");
        }
        Map<String, BigDecimal> report = getUsaliRevenueReport(startDate, endDate);
        return generateExcelReport(startDate, endDate, report);
    }

    @Override
    public byte[] exportReport(String type, LocalDate startDate, LocalDate endDate, String format) {
        if (!"xlsx".equalsIgnoreCase(format)) {
            throw new com.kawai.exceptions.BusinessException("REPORT-001", "Hệ thống chỉ hỗ trợ xuất báo cáo định dạng Excel (.xlsx)!");
        }
        try {
            roomBookingCleanupTask.cleanupNoShowRoomBookings();
        } catch (Exception e) {
            // ignore
        }

        if ("room".equalsIgnoreCase(type)) {
            return exportRoomReport(startDate, endDate, format);
        } else if ("fnb".equalsIgnoreCase(type)) {
            return exportFnBReport(startDate, endDate, format);
        } else if ("tour".equalsIgnoreCase(type)) {
            return exportTourReport(startDate, endDate, format);
        } else if ("occupancy".equalsIgnoreCase(type)) {
            return exportOccupancyReport(startDate, endDate, format);
        } else if ("stay".equalsIgnoreCase(type)) {
            return exportStayReport(startDate, endDate, format);
        } else {
            return exportUsaliReport(startDate, endDate, format);
        }
    }

    private byte[] exportRoomReport(LocalDate startDate, LocalDate endDate, String format) {
        String reportTitle = "BÁO CÁO PHÒNG";
        String[] headers = {"Phòng", "Hạng phòng", "Khách hàng", "Ngày nhận phòng", "Ngày trả phòng", "Số đêm", "Tiền phòng", "Trạng thái"};
        float[] widths = {10f, 20f, 20f, 15f, 15f, 8f, 15f, 12f};
        boolean[] isNumeric = {false, false, false, false, false, true, true, false};
        boolean[] isPercentage = {false, false, false, false, false, false, false, false};

        List<RoomBookingDetail> details = roomBookingDetailRepository.findAll().stream()
            .filter(d -> d.getRoomBooking() != null && d.getRoomBooking().getCheckInDate() != null && d.getRoomBooking().getCheckOutDate() != null)
            .filter(d -> {
                LocalDate ci = d.getRoomBooking().getCheckInDate();
                LocalDate co = d.getRoomBooking().getCheckOutDate();
                return ci.isBefore(endDate.plusDays(1)) && co.isAfter(startDate.minusDays(1));
            })
            .toList();

        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        List<String[]> dataRows = new java.util.ArrayList<>();
        BigDecimal totalCharge = BigDecimal.ZERO;

        for (RoomBookingDetail d : details) {
            String roomNum = d.getRoom() != null ? d.getRoom().getRoomNumber() : "N/A";
            String catName = d.getCategory() != null ? d.getCategory().getCategoryName() : "N/A";
            String custName = d.getRoomBooking().getCustomer() != null ? d.getRoomBooking().getCustomer().getFullName() : "N/A";
            String checkIn = d.getRoomBooking().getCheckInDate().toString();
            String checkOut = d.getRoomBooking().getCheckOutDate().toString();
            long nights = java.time.temporal.ChronoUnit.DAYS.between(d.getRoomBooking().getCheckInDate(), d.getRoomBooking().getCheckOutDate());
            BigDecimal charge = d.getRoomCharge() != null ? d.getRoomCharge() : BigDecimal.ZERO;
            totalCharge = totalCharge.add(charge);
            String status = d.getDetailStatus();

            dataRows.add(new String[]{
                roomNum, catName, custName, checkIn, checkOut, String.valueOf(nights), nf.format(charge) + " VND", status
            });
        }

        String[] summaryVals = new String[headers.length];
        summaryVals[6] = nf.format(totalCharge) + " VND";

        if ("xlsx".equalsIgnoreCase(format)) {
            return generateTabularExcel(reportTitle, startDate, endDate, headers, dataRows, isNumeric, isPercentage, "Tổng cộng", summaryVals);
        } else if ("pdf".equalsIgnoreCase(format)) {
            return generateTabularPdf(reportTitle, startDate, endDate, headers, widths, dataRows, isNumeric, isPercentage, "Tổng cộng", summaryVals);
        } else {
            return generateTabularCsv(reportTitle, startDate, endDate, headers, dataRows, "Tổng cộng", summaryVals);
        }
    }

    private byte[] exportFnBReport(LocalDate startDate, LocalDate endDate, String format) {
        String reportTitle = "BÁO CÁO F&B";
        String[] headers = {"Ngày tạo", "Mã đặt phòng", "Phòng", "Hạng mục", "Nội dung", "Số tiền"};
        float[] widths = {15f, 15f, 10f, 15f, 30f, 15f};
        boolean[] isNumeric = {false, false, false, false, false, true};
        boolean[] isPercentage = {false, false, false, false, false, false};

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        List<FolioItem> items = folioItemRepository.findAll().stream()
            .filter(item -> item.getCreatedAt() != null && !item.getCreatedAt().isBefore(start) && !item.getCreatedAt().isAfter(end))
            .filter(item -> {
                String dept = item.getSourceDepartment();
                return "F&B".equalsIgnoreCase(dept) || "POS".equalsIgnoreCase(dept) || "Restaurant".equalsIgnoreCase(dept)
                    || "Minibar".equalsIgnoreCase(dept) || "Mini-bar".equalsIgnoreCase(dept) || "RoomService".equalsIgnoreCase(dept);
            })
            .toList();

        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        List<String[]> dataRows = new java.util.ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (FolioItem item : items) {
            String date = item.getCreatedAt().toLocalDate().toString();
            String bookingId = item.getBooking() != null ? String.valueOf(item.getBooking().getId()) : "N/A";
            String roomNum = item.getRoomBookingDetail() != null && item.getRoomBookingDetail().getRoom() != null 
                ? item.getRoomBookingDetail().getRoom().getRoomNumber() : "N/A";
            String dept = item.getSourceDepartment();
            String desc = item.getDescription() != null ? item.getDescription() : "";
            BigDecimal amount = item.getAmount() != null ? item.getAmount() : BigDecimal.ZERO;
            total = total.add(amount);

            dataRows.add(new String[]{
                date, bookingId, roomNum, dept, desc, nf.format(amount) + " VND"
            });
        }

        String[] summaryVals = new String[headers.length];
        summaryVals[5] = nf.format(total) + " VND";

        if ("xlsx".equalsIgnoreCase(format)) {
            return generateTabularExcel(reportTitle, startDate, endDate, headers, dataRows, isNumeric, isPercentage, "Tổng cộng", summaryVals);
        } else if ("pdf".equalsIgnoreCase(format)) {
            return generateTabularPdf(reportTitle, startDate, endDate, headers, widths, dataRows, isNumeric, isPercentage, "Tổng cộng", summaryVals);
        } else {
            return generateTabularCsv(reportTitle, startDate, endDate, headers, dataRows, "Tổng cộng", summaryVals);
        }
    }

    private byte[] exportTourReport(LocalDate startDate, LocalDate endDate, String format) {
        String reportTitle = "BÁO CÁO TOUR";
        String[] headers = {"Ngày tạo", "Mã đặt phòng", "Phòng", "Tên dịch vụ", "Số tiền"};
        float[] widths = {15f, 15f, 10f, 40f, 20f};
        boolean[] isNumeric = {false, false, false, false, true};
        boolean[] isPercentage = {false, false, false, false, false};

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        List<FolioItem> items = folioItemRepository.findAll().stream()
            .filter(item -> item.getCreatedAt() != null && !item.getCreatedAt().isBefore(start) && !item.getCreatedAt().isAfter(end))
            .filter(item -> {
                String dept = item.getSourceDepartment();
                return "Tour".equalsIgnoreCase(dept) || "Tours".equalsIgnoreCase(dept);
            })
            .toList();

        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        List<String[]> dataRows = new java.util.ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (FolioItem item : items) {
            String date = item.getCreatedAt().toLocalDate().toString();
            String bookingId = item.getBooking() != null ? String.valueOf(item.getBooking().getId()) : "N/A";
            String roomNum = item.getRoomBookingDetail() != null && item.getRoomBookingDetail().getRoom() != null 
                ? item.getRoomBookingDetail().getRoom().getRoomNumber() : "N/A";
            String desc = item.getDescription() != null ? item.getDescription() : "";
            BigDecimal amount = item.getAmount() != null ? item.getAmount() : BigDecimal.ZERO;
            total = total.add(amount);

            dataRows.add(new String[]{
                date, bookingId, roomNum, desc, nf.format(amount) + " VND"
            });
        }

        String[] summaryVals = new String[headers.length];
        summaryVals[4] = nf.format(total) + " VND";

        if ("xlsx".equalsIgnoreCase(format)) {
            return generateTabularExcel(reportTitle, startDate, endDate, headers, dataRows, isNumeric, isPercentage, "Tổng cộng", summaryVals);
        } else if ("pdf".equalsIgnoreCase(format)) {
            return generateTabularPdf(reportTitle, startDate, endDate, headers, widths, dataRows, isNumeric, isPercentage, "Tổng cộng", summaryVals);
        } else {
            return generateTabularCsv(reportTitle, startDate, endDate, headers, dataRows, "Tổng cộng", summaryVals);
        }
    }

    private byte[] exportOccupancyReport(LocalDate startDate, LocalDate endDate, String format) {
        String reportTitle = "BÁO CÁO CÔNG SUẤT PHÒNG";
        String[] headers = {"Ngày", "Tổng số phòng", "Số phòng đã bán", "Số phòng trống", "Tỷ lệ lấp đầy"};
        float[] widths = {20f, 20f, 20f, 20f, 20f};
        boolean[] isNumeric = {false, true, true, true, false};
        boolean[] isPercentage = {false, false, false, false, true};

        long totalRooms = roomRepository.count();
        if (totalRooms == 0) totalRooms = 50;

        List<RoomBookingDetail> activeDetails = roomBookingDetailRepository.findAll().stream()
            .filter(d -> "Checked_In".equalsIgnoreCase(d.getDetailStatus()) || "Checked_Out".equalsIgnoreCase(d.getDetailStatus()) || "Completed".equalsIgnoreCase(d.getDetailStatus()))
            .toList();

        List<String[]> dataRows = new java.util.ArrayList<>();
        double occupancySum = 0.0;
        long totalDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;

        for (LocalDate day = startDate; !day.isAfter(endDate); day = day.plusDays(1)) {
            long occupied = 0;
            for (RoomBookingDetail d : activeDetails) {
                if (d.getRoom() == null || d.getRoomBooking() == null || d.getRoomBooking().getCheckInDate() == null || d.getRoomBooking().getCheckOutDate() == null) {
                    continue;
                }
                LocalDate ci = d.getRoomBooking().getCheckInDate();
                LocalDate co = d.getRoomBooking().getCheckOutDate();
                if (!day.isBefore(ci) && day.isBefore(co)) {
                    occupied++;
                }
            }

            if (occupied > totalRooms) occupied = totalRooms;
            long vacant = totalRooms - occupied;
            double rate = ((double) occupied / totalRooms) * 100.0;
            occupancySum += rate;

            dataRows.add(new String[]{
                day.toString(),
                String.valueOf(totalRooms),
                String.valueOf(occupied),
                String.valueOf(vacant),
                String.format("%.2f%%", rate)
            });
        }

        double avgOccupancy = totalDays > 0 ? occupancySum / totalDays : 0.0;
        String[] summaryVals = new String[headers.length];
        summaryVals[4] = String.format("%.2f%%", avgOccupancy);

        if ("xlsx".equalsIgnoreCase(format)) {
            return generateTabularExcel(reportTitle, startDate, endDate, headers, dataRows, isNumeric, isPercentage, "Bình quân công suất", summaryVals);
        } else if ("pdf".equalsIgnoreCase(format)) {
            return generateTabularPdf(reportTitle, startDate, endDate, headers, widths, dataRows, isNumeric, isPercentage, "Bình quân công suất", summaryVals);
        } else {
            return generateTabularCsv(reportTitle, startDate, endDate, headers, dataRows, "Bình quân công suất", summaryVals);
        }
    }

    private byte[] exportStayReport(LocalDate startDate, LocalDate endDate, String format) {
        String reportTitle = "BÁO CÁO THỜI GIAN LƯU TRÚ";
        String[] headers = {"Mã đặt phòng", "Khách hàng", "Phòng", "Hạng phòng", "Ngày nhận phòng", "Ngày trả phòng", "Số đêm lưu trú", "Tổng tiền"};
        float[] widths = {12f, 20f, 10f, 18f, 15f, 15f, 12f, 18f};
        boolean[] isNumeric = {false, false, false, false, false, false, true, true};
        boolean[] isPercentage = {false, false, false, false, false, false, false, false};

        List<RoomBookingDetail> details = roomBookingDetailRepository.findAll().stream()
            .filter(d -> d.getRoomBooking() != null && d.getRoomBooking().getCheckInDate() != null && d.getRoomBooking().getCheckOutDate() != null)
            .filter(d -> {
                LocalDate co = d.getRoomBooking().getCheckOutDate();
                return !co.isBefore(startDate) && !co.isAfter(endDate);
            })
            .toList();

        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        List<String[]> dataRows = new java.util.ArrayList<>();
        double staySum = 0.0;
        BigDecimal totalRevenue = BigDecimal.ZERO;

        for (RoomBookingDetail d : details) {
            String bookingId = String.valueOf(d.getRoomBooking().getId());
            String custName = d.getRoomBooking().getCustomer() != null ? d.getRoomBooking().getCustomer().getFullName() : "N/A";
            String roomNum = d.getRoom() != null ? d.getRoom().getRoomNumber() : "N/A";
            String catName = d.getCategory() != null ? d.getCategory().getCategoryName() : "N/A";
            String checkIn = d.getRoomBooking().getCheckInDate().toString();
            String checkOut = d.getRoomBooking().getCheckOutDate().toString();
            long nights = java.time.temporal.ChronoUnit.DAYS.between(d.getRoomBooking().getCheckInDate(), d.getRoomBooking().getCheckOutDate());
            staySum += nights;
            BigDecimal charge = d.getRoomCharge() != null ? d.getRoomCharge() : BigDecimal.ZERO;
            totalRevenue = totalRevenue.add(charge);

            dataRows.add(new String[]{
                bookingId, custName, roomNum, catName, checkIn, checkOut, String.valueOf(nights), nf.format(charge) + " VND"
            });
        }

        double avgStay = details.size() > 0 ? staySum / details.size() : 0.0;
        String[] summaryVals = new String[headers.length];
        summaryVals[6] = String.format("%.2f đêm", avgStay);
        summaryVals[7] = nf.format(totalRevenue) + " VND";

        if ("xlsx".equalsIgnoreCase(format)) {
            return generateTabularExcel(reportTitle, startDate, endDate, headers, dataRows, isNumeric, isPercentage, "Tổng cộng / Trung bình lưu trú", summaryVals);
        } else if ("pdf".equalsIgnoreCase(format)) {
            return generateTabularPdf(reportTitle, startDate, endDate, headers, widths, dataRows, isNumeric, isPercentage, "Tổng cộng / Trung bình lưu trú", summaryVals);
        } else {
            return generateTabularCsv(reportTitle, startDate, endDate, headers, dataRows, "Tổng cộng / Trung bình lưu trú", summaryVals);
        }
    }



    private byte[] generateExcelReport(LocalDate startDate, LocalDate endDate, Map<String, BigDecimal> report) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("USALI Revenue Report");
            sheet.setDisplayGridlines(true);

            Font titleFont = workbook.createFont();
            titleFont.setFontName("Arial");
            titleFont.setFontHeightInPoints((short) 16);
            titleFont.setBold(true);
            CellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setFont(titleFont);

            Font headerFont = workbook.createFont();
            headerFont.setFontName("Arial");
            headerFont.setFontHeightInPoints((short) 11);
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.MEDIUM);

            Font sectionFont = workbook.createFont();
            sectionFont.setFontName("Arial");
            sectionFont.setFontHeightInPoints((short) 11);
            sectionFont.setBold(true);
            CellStyle sectionStyle = workbook.createCellStyle();
            sectionStyle.setFont(sectionFont);
            sectionStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            sectionStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Font normalFont = workbook.createFont();
            normalFont.setFontName("Arial");
            normalFont.setFontHeightInPoints((short) 11);
            CellStyle labelStyle = workbook.createCellStyle();
            labelStyle.setFont(normalFont);
            labelStyle.setIndention((short) 1);

            CellStyle numberStyle = workbook.createCellStyle();
            numberStyle.setFont(normalFont);
            numberStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));
            numberStyle.setAlignment(HorizontalAlignment.RIGHT);

            CellStyle pctStyle = workbook.createCellStyle();
            pctStyle.setFont(normalFont);
            pctStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00%"));
            pctStyle.setAlignment(HorizontalAlignment.RIGHT);

            Font boldFont = workbook.createFont();
            boldFont.setFontName("Arial");
            boldFont.setFontHeightInPoints((short) 11);
            boldFont.setBold(true);
            
            CellStyle deptLabelStyle = workbook.createCellStyle();
            deptLabelStyle.setFont(boldFont);
            
            CellStyle deptNumStyle = workbook.createCellStyle();
            deptNumStyle.setFont(boldFont);
            deptNumStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));
            deptNumStyle.setAlignment(HorizontalAlignment.RIGHT);
            deptNumStyle.setBorderTop(BorderStyle.THIN);
            deptNumStyle.setBorderBottom(BorderStyle.THIN);

            CellStyle grandLabelStyle = workbook.createCellStyle();
            grandLabelStyle.setFont(boldFont);
            grandLabelStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
            grandLabelStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            CellStyle grandNumStyle = workbook.createCellStyle();
            grandNumStyle.setFont(boldFont);
            grandNumStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));
            grandNumStyle.setAlignment(HorizontalAlignment.RIGHT);
            grandNumStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
            grandNumStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            grandNumStyle.setBorderTop(BorderStyle.THIN);
            grandNumStyle.setBorderBottom(BorderStyle.DOUBLE);

            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("KAWAI RESORT & TOUR HUB");
            titleCell.setCellStyle(titleStyle);

            Row subtitleRow = sheet.createRow(1);
            subtitleRow.createCell(0).setCellValue("BÁO CÁO DOANH THU");
            subtitleRow.getCell(0).setCellStyle(sectionStyle);

            Row periodRow = sheet.createRow(2);
            periodRow.createCell(0).setCellValue("Kỳ báo cáo: từ " + startDate + " đến " + endDate);

            Row metaRow = sheet.createRow(3);
            metaRow.createCell(0).setCellValue("Người lập: Manager | Ngày lập: " + LocalDate.now());

            Row headerRow = sheet.createRow(5);
            String[] headers = {"Khoản mục", "Thực tế kỳ này (VND)", "Cùng kỳ năm ngoái (VND)", "Chênh lệch (VND)", "Chênh lệch (%)"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 6;
            List<ReportRow> rows = getReportRowsDefinition();
            for (ReportRow r : rows) {
                Row row = sheet.createRow(rowIdx++);
                if (r.type == 0 || r.type == 4) {
                    Cell c = row.createCell(0);
                    c.setCellValue(r.label);
                    c.setCellStyle(sectionStyle);
                    for (int i = 1; i < 5; i++) {
                        Cell ec = row.createCell(i);
                        ec.setCellStyle(sectionStyle);
                    }
                } else {
                    Cell labelCell = row.createCell(0);
                    labelCell.setCellValue(r.label);
                    
                    CellStyle currentLabelStyle;
                    CellStyle currentNumStyle;
                    if (r.type == 2) {
                        currentLabelStyle = deptLabelStyle;
                        currentNumStyle = deptNumStyle;
                    } else if (r.type == 3) {
                        currentLabelStyle = grandLabelStyle;
                        currentNumStyle = grandNumStyle;
                    } else {
                        currentLabelStyle = labelStyle;
                        currentNumStyle = numberStyle;
                    }
                    
                    labelCell.setCellStyle(currentLabelStyle);

                    BigDecimal actual = report.getOrDefault(r.key, BigDecimal.ZERO);
                    BigDecimal lastYear = report.getOrDefault(r.key + "-LASTYEAR", BigDecimal.ZERO);

                    Cell cAct = row.createCell(1);
                    Cell cLy = row.createCell(2);
                    Cell cVar = row.createCell(3);
                    Cell cVarPct = row.createCell(4);

                    String rowNumStr = String.valueOf(rowIdx);

                    if (r.type == 5 && r.key.equals("KPI-OCCUPANCY")) {
                        cAct.setCellValue(actual.doubleValue() / 100.0);
                        cLy.setCellValue(lastYear.doubleValue() / 100.0);
                        
                        cVar.setCellFormula("B" + rowNumStr + "-C" + rowNumStr);
                        cVarPct.setCellFormula("IF(C" + rowNumStr + "=0,0,D" + rowNumStr + "/C" + rowNumStr + ")");
                        
                        cAct.setCellStyle(pctStyle);
                        cLy.setCellStyle(pctStyle);
                        cVar.setCellStyle(pctStyle);
                        cVarPct.setCellStyle(pctStyle);
                    } else {
                        cAct.setCellValue(actual.doubleValue());
                        cLy.setCellValue(lastYear.doubleValue());

                        cVar.setCellFormula("B" + rowNumStr + "-C" + rowNumStr);
                        cVarPct.setCellFormula("IF(C" + rowNumStr + "=0,0,D" + rowNumStr + "/C" + rowNumStr + ")");

                        cAct.setCellStyle(currentNumStyle);
                        cLy.setCellStyle(currentNumStyle);
                        cVar.setCellStyle(currentNumStyle);
                        cVarPct.setCellStyle(pctStyle);
                    }
                }
            }

            for (int i = 0; i < 5; i++) {
                try {
                    sheet.autoSizeColumn(i);
                } catch (Throwable t) {
                    sheet.setColumnWidth(i, 25 * 256);
                }
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Throwable e) {
            throw new RuntimeException("Lỗi khi sinh file Excel", e);
        }
    }

    private byte[] generateTabularExcel(
            String reportTitle,
            LocalDate startDate,
            LocalDate endDate,
            String[] headers,
            List<String[]> dataRows,
            boolean[] isNumeric,
            boolean[] isPercentage,
            String summaryLabel,
            String[] summaryVals) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Report");
            sheet.setDisplayGridlines(true);

            org.apache.poi.ss.usermodel.Font titleFont = workbook.createFont();
            titleFont.setFontName("Arial");
            titleFont.setFontHeightInPoints((short) 16);
            titleFont.setBold(true);
            CellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setFont(titleFont);

            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setFontName("Arial");
            headerFont.setFontHeightInPoints((short) 11);
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.MEDIUM);

            org.apache.poi.ss.usermodel.Font normalFont = workbook.createFont();
            normalFont.setFontName("Arial");
            normalFont.setFontHeightInPoints((short) 11);
            CellStyle labelStyle = workbook.createCellStyle();
            labelStyle.setFont(normalFont);

            CellStyle numberStyle = workbook.createCellStyle();
            numberStyle.setFont(normalFont);
            numberStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));
            numberStyle.setAlignment(HorizontalAlignment.RIGHT);

            CellStyle pctStyle = workbook.createCellStyle();
            pctStyle.setFont(normalFont);
            pctStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00%"));
            pctStyle.setAlignment(HorizontalAlignment.RIGHT);

            org.apache.poi.ss.usermodel.Font boldFont = workbook.createFont();
            boldFont.setFontName("Arial");
            boldFont.setFontHeightInPoints((short) 11);
            boldFont.setBold(true);

            CellStyle boldLabelStyle = workbook.createCellStyle();
            boldLabelStyle.setFont(boldFont);

            CellStyle boldNumStyle = workbook.createCellStyle();
            boldNumStyle.setFont(boldFont);
            boldNumStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));
            boldNumStyle.setAlignment(HorizontalAlignment.RIGHT);
            boldNumStyle.setBorderTop(BorderStyle.THIN);
            boldNumStyle.setBorderBottom(BorderStyle.DOUBLE);

            Row r0 = sheet.createRow(0);
            Cell titleCell0 = r0.createCell(0);
            titleCell0.setCellValue("KAWAI RESORT & TOUR HUB");
            titleCell0.setCellStyle(titleStyle);
            
            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue(reportTitle);
            
            Row r2 = sheet.createRow(2);
            r2.createCell(0).setCellValue("Kỳ báo cáo: từ " + startDate + " đến " + endDate);

            Row r3 = sheet.createRow(3);
            r3.createCell(0).setCellValue("Người lập: Manager | Ngày lập: " + LocalDate.now());

            Row r5 = sheet.createRow(5);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = r5.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 6;
            for (String[] rowData : dataRows) {
                Row row = sheet.createRow(rowIdx++);
                for (int i = 0; i < rowData.length; i++) {
                    Cell cell = row.createCell(i);
                    String val = rowData[i];
                    if (isPercentage[i]) {
                        try {
                            double d = Double.parseDouble(val.replace("%", "").trim());
                            cell.setCellValue(d / 100.0);
                        } catch (Exception e) {
                            cell.setCellValue(val);
                        }
                        cell.setCellStyle(pctStyle);
                    } else if (isNumeric[i]) {
                        try {
                            double d = Double.parseDouble(val.replace("VND", "").replace(",", "").replace(".", "").trim());
                            cell.setCellValue(d);
                        } catch (Exception e) {
                            cell.setCellValue(val);
                        }
                        cell.setCellStyle(numberStyle);
                    } else {
                        cell.setCellValue(val);
                        cell.setCellStyle(labelStyle);
                    }
                }
            }

            if (summaryLabel != null && summaryVals != null) {
                Row row = sheet.createRow(rowIdx++);
                Cell cell = row.createCell(0);
                cell.setCellValue(summaryLabel);
                cell.setCellStyle(boldLabelStyle);
                for (int i = 1; i < headers.length; i++) {
                    Cell c = row.createCell(i);
                    String val = summaryVals[i];
                    if (val == null) {
                        c.setCellValue("");
                    } else if (isNumeric[i]) {
                        try {
                            double d = Double.parseDouble(val.replace("VND", "").replace(",", "").replace(".", "").trim());
                            c.setCellValue(d);
                        } catch (Exception e) {
                            c.setCellValue(val);
                        }
                        c.setCellStyle(boldNumStyle);
                    } else {
                        c.setCellValue(val);
                        c.setCellStyle(boldLabelStyle);
                    }
                }
            }

            for (int i = 0; i < headers.length; i++) {
                try {
                    sheet.autoSizeColumn(i);
                } catch (Throwable t) {
                    sheet.setColumnWidth(i, 25 * 256);
                }
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Throwable e) {
            e.printStackTrace();
            return generateTabularCsv(reportTitle, startDate, endDate, headers, dataRows, summaryLabel, summaryVals);
        }
    }

    private byte[] generateTabularPdf(
            String reportTitle,
            LocalDate startDate,
            LocalDate endDate,
            String[] headers,
            float[] widths,
            List<String[]> dataRows,
            boolean[] isNumeric,
            boolean[] isPercentage,
            String summaryLabel,
            String[] summaryVals) {
        Document document = new Document(PageSize.A4.rotate());
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter.getInstance(document, out);
            document.open();

            com.lowagie.text.Font resortNameFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 16, com.lowagie.text.Font.BOLD, new Color(44, 62, 80));
            com.lowagie.text.Font reportTitleFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 14, com.lowagie.text.Font.BOLD, new Color(52, 73, 94));
            com.lowagie.text.Font metaFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10, com.lowagie.text.Font.ITALIC, Color.GRAY);
            com.lowagie.text.Font thFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10, com.lowagie.text.Font.BOLD, Color.WHITE);
            com.lowagie.text.Font tdFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10, com.lowagie.text.Font.NORMAL, Color.BLACK);
            com.lowagie.text.Font tdBoldFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10, com.lowagie.text.Font.BOLD, Color.BLACK);

            Paragraph resortName = new Paragraph("KAWAI RESORT & TOUR HUB", resortNameFont);
            resortName.setAlignment(Element.ALIGN_CENTER);
            document.add(resortName);

            Paragraph reportTitlePara = new Paragraph(reportTitle.toUpperCase(), reportTitleFont);
            reportTitlePara.setAlignment(Element.ALIGN_CENTER);
            reportTitlePara.setSpacingAfter(5f);
            document.add(reportTitlePara);

            Paragraph periodPara = new Paragraph("Period: " + startDate + " to " + endDate, tdBoldFont);
            periodPara.setAlignment(Element.ALIGN_CENTER);
            document.add(periodPara);

            Paragraph metaPara = new Paragraph("Generated by: Manager | Date: " + LocalDate.now(), metaFont);
            metaPara.setAlignment(Element.ALIGN_CENTER);
            metaPara.setSpacingAfter(15f);
            document.add(metaPara);

            PdfPTable table = new PdfPTable(headers.length);
            table.setWidthPercentage(100f);
            table.setWidths(widths);

            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, thFont));
                cell.setBackgroundColor(new Color(44, 62, 80));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setPadding(8f);
                table.addCell(cell);
            }

            for (String[] rowData : dataRows) {
                for (int i = 0; i < rowData.length; i++) {
                    PdfPCell cell = new PdfPCell(new Phrase(rowData[i], tdFont));
                    cell.setPadding(6f);
                    if (isNumeric[i] || isPercentage[i]) {
                        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    } else {
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    }
                    table.addCell(cell);
                }
            }

            if (summaryLabel != null && summaryVals != null) {
                PdfPCell labelCell = new PdfPCell(new Phrase(summaryLabel, tdBoldFont));
                labelCell.setPadding(6f);
                labelCell.setBackgroundColor(new Color(254, 249, 231));
                table.addCell(labelCell);

                for (int i = 1; i < headers.length; i++) {
                    String val = summaryVals[i] != null ? summaryVals[i] : "";
                    PdfPCell cell = new PdfPCell(new Phrase(val, tdBoldFont));
                    cell.setPadding(6f);
                    cell.setBackgroundColor(new Color(254, 249, 231));
                    if (isNumeric[i] || isPercentage[i]) {
                        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    } else {
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    }
                    table.addCell(cell);
                }
            }

            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (Throwable e) {
            e.printStackTrace();
            return generateTabularCsv(reportTitle, startDate, endDate, headers, dataRows, summaryLabel, summaryVals);
        }
    }

    private byte[] generateTabularCsv(
            String reportTitle,
            LocalDate startDate,
            LocalDate endDate,
            String[] headers,
            List<String[]> dataRows,
            String summaryLabel,
            String[] summaryVals) {
        StringBuilder sb = new StringBuilder();
        sb.append("\uFEFF");
        sb.append(reportTitle).append("\n");
        sb.append("Kỳ báo cáo: ;").append(startDate).append(" ;đến; ").append(endDate).append("\n\n");
        
        sb.append(String.join(";", headers)).append("\n");
        
        for (String[] rowData : dataRows) {
            sb.append(String.join(";", rowData)).append("\n");
        }
        
        if (summaryLabel != null && summaryVals != null) {
            sb.append(summaryLabel).append(";");
            for (int i = 1; i < headers.length; i++) {
                sb.append(summaryVals[i] != null ? summaryVals[i] : "").append(";");
            }
            sb.append("\n");
        }
        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }
}
