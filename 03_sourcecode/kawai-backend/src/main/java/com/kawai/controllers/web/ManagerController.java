package com.kawai.controllers.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import lombok.Data;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * ManagerController — Giao diện Manager Portal.
 *
 * Mỗi mục sidebar là 1 route riêng → 1 trang HTML riêng.
 * Dữ liệu hiện tại là mock data. Thay bằng Service/Repository khi có DB.
 */
@Controller
@RequestMapping("/manager")
public class ManagerController {

    // ── Shared date formatter ──────────────────────────────────────────────────
    private static String todayLabel() {
        return LocalDate.now()
                .format(DateTimeFormatter.ofPattern("EEEE, d 'tháng' M, yyyy", new Locale("vi")));
    }

    // =========================================================================
    //  1. Tổng quan (Dashboard)
    // =========================================================================

    @GetMapping({"/dashboard", "/"})
    public String dashboard(Model model) {
        model.addAttribute("todayLabel",       todayLabel());
        model.addAttribute("occupancyRate",    78);
        model.addAttribute("totalRooms",       100);
        model.addAttribute("occupiedRooms",     78);
        model.addAttribute("revenueToday",     "24.8M");
        model.addAttribute("revenueTodayNote", "Phòng · F&B · Tour");
        model.addAttribute("avgStayDays",      "3.2");
        model.addAttribute("totalGuests",       320);
        model.addAttribute("revenueMonth",     "1.84 Tỷ");
        model.addAttribute("revenueMonthNote", "Phòng + F&B + Tour");

        List<DailyRevenueMock> dailyRevenue = Arrays.asList(
            new DailyRevenueMock("T2", 180, 65, 42),
            new DailyRevenueMock("T3", 145, 55, 28),
            new DailyRevenueMock("T4", 200, 78, 55),
            new DailyRevenueMock("T5", 220, 82, 61),
            new DailyRevenueMock("T6", 265, 95, 72),
            new DailyRevenueMock("T7", 310, 110, 88),
            new DailyRevenueMock("CN", 295, 105, 80)
        );
        model.addAttribute("dailyRevenue",     dailyRevenue);
        model.addAttribute("revRoom",           1128);
        model.addAttribute("revRoomPct",          61);
        model.addAttribute("revFnb",             487);
        model.addAttribute("revFnbPct",           26);
        model.addAttribute("revTour",            231);
        model.addAttribute("revTourPct",          13);
        model.addAttribute("peakOccupancy",       94);
        model.addAttribute("peakOccupancyDate", "03/06");
        model.addAttribute("avgOccupancyMonth",   78);
        model.addAttribute("topDish",           "Cá sông nướng");
        model.addAttribute("topDishOrders",       87);
        model.addAttribute("topTour",           "Du thuyền hoàng hôn");
        model.addAttribute("topTourBookings",     43);
        return "manager/dashboard";
    }

    // =========================================================================
    //  2. Doanh thu theo ngày
    // =========================================================================

    @GetMapping("/revenue/daily")
    public String revenueDaily(Model model) {
        model.addAttribute("todayLabel", todayLabel());
        model.addAttribute("totalToday",  "480M");
        model.addAttribute("totalWeek",   "2.63 Tỷ");
        model.addAttribute("totalMonth",  "1.84 Tỷ");

        List<RevenueRowMock> rows = Arrays.asList(
            new RevenueRowMock("09/06/2026", "295M", "105M",  "80M", "480M"),
            new RevenueRowMock("08/06/2026", "310M", "110M",  "88M", "508M"),
            new RevenueRowMock("07/06/2026", "265M",  "95M",  "72M", "432M"),
            new RevenueRowMock("06/06/2026", "220M",  "82M",  "61M", "363M"),
            new RevenueRowMock("05/06/2026", "200M",  "78M",  "55M", "333M"),
            new RevenueRowMock("04/06/2026", "145M",  "55M",  "28M", "228M"),
            new RevenueRowMock("03/06/2026", "180M",  "65M",  "42M", "287M"),
            new RevenueRowMock("02/06/2026", "220M",  "80M",  "58M", "358M"),
            new RevenueRowMock("01/06/2026", "195M",  "72M",  "51M", "318M")
        );
        model.addAttribute("rows", rows);

        List<DailyRevenueMock> chartData = Arrays.asList(
            new DailyRevenueMock("01/06", 195, 72, 51),
            new DailyRevenueMock("02/06", 220, 80, 58),
            new DailyRevenueMock("03/06", 180, 65, 42),
            new DailyRevenueMock("04/06", 145, 55, 28),
            new DailyRevenueMock("05/06", 200, 78, 55),
            new DailyRevenueMock("06/06", 220, 82, 61),
            new DailyRevenueMock("07/06", 265, 95, 72),
            new DailyRevenueMock("08/06", 310,110, 88),
            new DailyRevenueMock("09/06", 295,105, 80)
        );
        model.addAttribute("chartData", chartData);
        return "manager/revenue-daily";
    }

    // =========================================================================
    //  3. Doanh thu theo tháng
    // =========================================================================

    @GetMapping("/revenue/monthly")
    public String revenueMonthly(Model model) {
        model.addAttribute("todayLabel",   todayLabel());
        model.addAttribute("totalYear",    "29.5 Tỷ");
        model.addAttribute("bestMonth",    "Tháng 5");
        model.addAttribute("bestMonthVal", "6.29 Tỷ");
        model.addAttribute("growthYoY",    "+22%");

        List<RevenueRowMock> rows = Arrays.asList(
            new RevenueRowMock("Tháng 1/2026", "3.20 Tỷ", "1.10 Tỷ", "620M",  "4.92 Tỷ"),
            new RevenueRowMock("Tháng 2/2026", "2.80 Tỷ", "940M",    "510M",  "4.25 Tỷ"),
            new RevenueRowMock("Tháng 3/2026", "3.50 Tỷ", "1.20 Tỷ", "680M",  "5.38 Tỷ"),
            new RevenueRowMock("Tháng 4/2026", "3.80 Tỷ", "1.30 Tỷ", "720M",  "5.82 Tỷ"),
            new RevenueRowMock("Tháng 5/2026", "4.10 Tỷ", "1.40 Tỷ", "790M",  "6.29 Tỷ"),
            new RevenueRowMock("Tháng 6/2026", "1.13 Tỷ", "487M",    "231M",  "1.84 Tỷ")
        );
        model.addAttribute("rows", rows);

        // Chart labels & values (tỷ)
        model.addAttribute("chartLabels", Arrays.asList("T1","T2","T3","T4","T5","T6"));
        model.addAttribute("chartRoom",   Arrays.asList(3.20, 2.80, 3.50, 3.80, 4.10, 1.13));
        model.addAttribute("chartFnb",    Arrays.asList(1.10, 0.94, 1.20, 1.30, 1.40, 0.49));
        model.addAttribute("chartTour",   Arrays.asList(0.62, 0.51, 0.68, 0.72, 0.79, 0.23));
        return "manager/revenue-monthly";
    }

    // =========================================================================
    //  4. Doanh thu theo năm
    // =========================================================================

    @GetMapping("/revenue/yearly")
    public String revenueYearly(Model model) {
        model.addAttribute("todayLabel",    todayLabel());
        model.addAttribute("currentYear",   "2026");
        model.addAttribute("ytdRevenue",    "29.5 Tỷ");
        model.addAttribute("growthVsLast",  "+19%");
        model.addAttribute("cagr3y",        "+16%");

        List<RevenueRowMock> rows = Arrays.asList(
            new RevenueRowMock("2024", "32 Tỷ",   "11 Tỷ",   "6.2 Tỷ", "49.2 Tỷ"),
            new RevenueRowMock("2025", "38 Tỷ",   "13 Tỷ",   "7.8 Tỷ", "58.8 Tỷ"),
            new RevenueRowMock("2026 (YTD)", "19.5 Tỷ", "6.4 Tỷ", "3.6 Tỷ", "29.5 Tỷ")
        );
        model.addAttribute("rows", rows);
        model.addAttribute("chartLabels", Arrays.asList("2024","2025","2026 YTD"));
        model.addAttribute("chartRoom",   Arrays.asList(32.0, 38.0, 19.5));
        model.addAttribute("chartFnb",    Arrays.asList(11.0, 13.0,  6.4));
        model.addAttribute("chartTour",   Arrays.asList( 6.2,  7.8,  3.6));
        return "manager/revenue-yearly";
    }

    // =========================================================================
    //  5. Tỷ lệ lấp đầy phòng
    // =========================================================================

    @GetMapping("/analytics/occupancy")
    public String analyticsOccupancy(Model model) {
        model.addAttribute("todayLabel",       todayLabel());
        model.addAttribute("currentOccupancy",  78);
        model.addAttribute("peakOccupancy",      94);
        model.addAttribute("peakDate",          "03/06");
        model.addAttribute("lowOccupancy",       62);
        model.addAttribute("lowDate",           "04/06");
        model.addAttribute("avgOccupancy",       78);
        model.addAttribute("totalRooms",         100);
        model.addAttribute("occupiedRooms",       78);

        List<OccupancyRowMock> rows = Arrays.asList(
            new OccupancyRowMock("Deluxe River View", 8,   94, 8),
            new OccupancyRowMock("Garden Bungalow",   6,   82, 5),
            new OccupancyRowMock("Forest Suite",      4,   76, 3),
            new OccupancyRowMock("Standard Room",     6,   61, 4)
        );
        model.addAttribute("rows", rows);
        return "manager/analytics-occupancy";
    }

    // =========================================================================
    //  6. Tỷ lệ tour
    // =========================================================================

    @GetMapping("/analytics/tour")
    public String analyticsTour(Model model) {
        model.addAttribute("todayLabel",    todayLabel());
        model.addAttribute("totalBookings", 127);
        model.addAttribute("totalRevenue",  "231M");
        model.addAttribute("avgTicket",     "481K");

        List<TourRowMock> rows = Arrays.asList(
            new TourRowMock("Du thuyền hoàng hôn",   "Du thuyền sông",    43, "450K", "19.35M",  87),
            new TourRowMock("Thăm bản Hmông",         "Tham quan bản làng",38, "580K", "22.04M",  77),
            new TourRowMock("Đi bộ rừng sáng sớm",   "Trekking rừng",     29, "320K",  "9.28M",  59),
            new TourRowMock("Kayak đến thác nước",    "Chèo thuyền kayak", 17, "520K",  "8.84M",  35)
        );
        model.addAttribute("rows", rows);
        return "manager/analytics-tour";
    }

    // =========================================================================
    //  7. Tỷ lệ các món ăn
    // =========================================================================

    @GetMapping("/analytics/food")
    public String analyticsFood(Model model) {
        model.addAttribute("todayLabel",   todayLabel());
        model.addAttribute("totalOrders",  "254");
        model.addAttribute("totalRevenue", "487M");
        model.addAttribute("avgOrder",     "192K");

        List<FoodRowMock> rows = Arrays.asList(
            new FoodRowMock("Cá sông nướng",          "Món chính",   87, "320K", "27.84M", 100),
            new FoodRowMock("Tom Yum Soup",            "Súp",         72, "185K", "13.32M",  83),
            new FoodRowMock("Set thử món theo mùa",   "Set Menu",    54, "680K", "36.72M",  62),
            new FoodRowMock("Xôi xoài",               "Tráng miệng", 41,  "95K",  "3.90M",  47)
        );
        model.addAttribute("rows", rows);

        // Donut chart: by category
        model.addAttribute("catLabels", Arrays.asList("Món chính","Súp","Set Menu","Tráng miệng","Đồ uống"));
        model.addAttribute("catValues", Arrays.asList(42, 28, 21, 16, 18));
        return "manager/analytics-food";
    }

    // =========================================================================
    //  8. Thời gian lưu trú trung bình
    // =========================================================================

    @GetMapping("/analytics/stay")
    public String analyticsStay(Model model) {
        model.addAttribute("todayLabel",    todayLabel());
        model.addAttribute("avgStay",       "3.2");
        model.addAttribute("totalGuests",    320);
        model.addAttribute("minStay",        1);
        model.addAttribute("maxStay",        12);
        model.addAttribute("medianStay",    "3");

        List<StayRowMock> rows = Arrays.asList(
            new StayRowMock("Deluxe River View", "4.1 ngày", 38,  8),
            new StayRowMock("Garden Bungalow",   "3.6 ngày", 31,  6),
            new StayRowMock("Forest Suite",      "5.2 ngày", 20,  4),
            new StayRowMock("Standard Room",     "2.1 ngày", 231, 6)
        );
        model.addAttribute("rows", rows);

        // Distribution: 1 ngày, 2 ngày, 3 ngày, 4 ngày, 5+ ngày
        model.addAttribute("distLabels", Arrays.asList("1 ngày","2 ngày","3 ngày","4 ngày","5 ngày","6+ ngày"));
        model.addAttribute("distValues", Arrays.asList(58, 72, 85, 61, 28, 16));
        return "manager/analytics-stay";
    }

    // =========================================================================
    //  9. Xuất báo cáo
    // =========================================================================

    @GetMapping("/export")
    public String export(Model model) {
        model.addAttribute("todayLabel", todayLabel());
        List<ExportHistoryMock> history = Arrays.asList(
            new ExportHistoryMock("Doanh thu tháng 5/2026", "Excel", "01/06/2026 09:15", "Manager Dũng", "2.4 MB"),
            new ExportHistoryMock("Tỷ lệ lấp đầy Q2",      "PDF",   "30/05/2026 14:30", "Manager Dũng", "1.1 MB"),
            new ExportHistoryMock("Báo cáo tour tháng 4",   "CSV",   "02/05/2026 10:00", "Manager Dũng", "320 KB"),
            new ExportHistoryMock("Doanh thu năm 2025",     "Excel", "15/01/2026 08:45", "Manager Dũng", "5.8 MB")
        );
        model.addAttribute("history", history);
        return "manager/export";
    }

    // =========================================================================
    //  Inner mock classes
    // =========================================================================

    @Data @AllArgsConstructor public static class DailyRevenueMock  { private String label; private int room; private int fnb; private int tour; }
    @Data @AllArgsConstructor public static class RevenueRowMock    { private String period; private String room; private String fnb; private String tour; private String total; }
    @Data @AllArgsConstructor public static class OccupancyRowMock  { private String category; private int totalRooms; private int occupancyPct; private int occupied; }
    @Data @AllArgsConstructor public static class TourRowMock       { private String name; private String category; private int bookings; private String price; private String revenue; private int barPct; }
    @Data @AllArgsConstructor public static class FoodRowMock       { private String name; private String category; private int orders; private String price; private String revenue; private int barPct; }
    @Data @AllArgsConstructor public static class StayRowMock       { private String category; private String avgStay; private int guests; private int rooms; }
    @Data @AllArgsConstructor public static class ExportHistoryMock { private String reportName; private String format; private String exportedAt; private String exportedBy; private String fileSize; }
}
