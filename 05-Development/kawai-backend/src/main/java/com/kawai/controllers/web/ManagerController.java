package com.kawai.controllers.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import lombok.Data;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import com.kawai.repositories.*;
import com.kawai.models.*;

@Controller
@RequestMapping("/manager")
@AllArgsConstructor
public class ManagerController {

    private final RoomRepository roomRepository;
    private final RoomBookingRepository roomBookingRepository;
    private final FoodOrderRepository foodOrderRepository;
    private final FoodOrderDetailRepository foodOrderDetailRepository;
    private final TourBookingRepository tourBookingRepository;
    private final TourRepository tourRepository;
    private final BookingRepository bookingRepository;
    private final FoodItemRepository foodItemRepository;
    private final DailyRateRepository dailyRateRepository;

    private static String todayLabel() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d 'tháng' M, yyyy", new Locale("vi")));
    }

    private static final long MOCK_TOTAL_ROOMS = 100;

    // ── Helper: format tiền ──
    private static String fmt(BigDecimal val) {
        if (val == null)
            return "0";
        long v = val.longValue();
        if (v >= 1_000_000_000)
            return String.format("%,.1f", v / 1_000_000_000.0) + " Tỷ";
        if (v >= 1_000_000)
            return String.format("%,.0f", v / 1_000_000.0) + "M";
        return String.format("%,d", v);
    }

    // =========================================================================
    // 1. Dashboard
    // =========================================================================

    @GetMapping({ "/dashboard", "/" })
    public String dashboard(Model model) {
        model.addAttribute("todayLabel", todayLabel());
        long totalRooms = MOCK_TOTAL_ROOMS;
        long occupied = 78;
        try {
            occupied = roomRepository.findOccupied().size();
            totalRooms = roomRepository.countTotalRooms();
        } catch (Exception e) {
        }
        long occupancyRate = totalRooms > 0 ? Math.round((double) occupied / totalRooms * 100) : 0;
        model.addAttribute("occupancyRate", occupancyRate);
        model.addAttribute("totalRooms", totalRooms);
        model.addAttribute("occupiedRooms", occupied);
        model.addAttribute("avgStayDays", "3.2");

        long totalGuests = occupied * 2;
        model.addAttribute("totalGuests", totalGuests);

        // Revenue
        BigDecimal revRoom = BigDecimal.ZERO;
        BigDecimal revFnb = BigDecimal.ZERO;
        try {
            revRoom = roomBookingRepository.totalDepositsSince(LocalDate.now().minusDays(30));
            if (revRoom == null) {
                revRoom = BigDecimal.ZERO;
            }
        } catch (Exception e) {
            revRoom = BigDecimal.ZERO;
        }
        try {
            revFnb = foodOrderRepository.totalCompletedRevenue();
            if (revFnb == null) {
                revFnb = BigDecimal.ZERO;
            }
        } catch (Exception e) {
            revFnb = BigDecimal.ZERO;
        }
        long revTour = 0;
        try {
            revTour = tourBookingRepository.count();
        } catch (Exception e) {
        }

        model.addAttribute("revenueToday", fmt(revRoom.add(revFnb)));
        model.addAttribute("revenueMonth", fmt(revRoom.add(revFnb)));
        model.addAttribute("revenueMonthNote", "Phòng + F&B + Tour");
        model.addAttribute("revenueTodayNote", "Phòng · F&B · Tour");

        List<DailyRevenueMock> dailyRevenue = new ArrayList<>();
        try {
            for (int i = 6; i >= 0; i--) {
                LocalDate d = LocalDate.now().minusDays(i);
                String label = d.format(DateTimeFormatter.ofPattern("E", new Locale("vi")));
                dailyRevenue.add(new DailyRevenueMock(label, 200, 70, 50));
            }
        } catch (Exception e) {
        }
        if (dailyRevenue.isEmpty()) {
            dailyRevenue = List.of(
                    new DailyRevenueMock("T2", 180, 65, 42), new DailyRevenueMock("T3", 145, 55, 28),
                    new DailyRevenueMock("T4", 200, 78, 55), new DailyRevenueMock("T5", 220, 82, 61),
                    new DailyRevenueMock("T6", 265, 95, 72), new DailyRevenueMock("T7", 310, 110, 88),
                    new DailyRevenueMock("CN", 295, 105, 80));
        }
        model.addAttribute("dailyRevenue", dailyRevenue);

        model.addAttribute("revRoom", revRoom.longValue() / 1_000_000);
        model.addAttribute("revRoomPct", 61);
        model.addAttribute("revFnb", revFnb.longValue() / 1_000_000);
        model.addAttribute("revFnbPct", 26);
        model.addAttribute("revTour", revTour);
        model.addAttribute("revTourPct", 13);
        model.addAttribute("peakOccupancy", 94);
        model.addAttribute("peakOccupancyDate", "03/06");
        model.addAttribute("avgOccupancyMonth", occupancyRate);
        model.addAttribute("topDish", "Cá sông nướng");
        model.addAttribute("topDishOrders", 87);
        model.addAttribute("topTour", "Du thuyền hoàng hôn");
        model.addAttribute("topTourBookings", 43);
        return "manager/dashboard";
    }

    // =========================================================================
    // 2. Doanh thu theo ngày
    // =========================================================================

    @GetMapping("/revenue/daily")
    public String revenueDaily(Model model) {
        model.addAttribute("todayLabel", todayLabel());
        model.addAttribute("totalToday", "480M");
        model.addAttribute("totalWeek", "2.63 Tỷ");
        model.addAttribute("totalMonth", "1.84 Tỷ");

        List<RevenueRowMock> rows = new ArrayList<>();
        try {
            for (int i = 8; i >= 0; i--) {
                LocalDate d = LocalDate.now().minusDays(i);
                String lbl = d.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                rows.add(new RevenueRowMock(lbl, "200M", "70M", "50M", "320M"));
            }
        } catch (Exception e) {
        }
        if (rows.isEmpty()) {
            rows = List.of(
                    new RevenueRowMock("09/06/2026", "295M", "105M", "80M", "480M"),
                    new RevenueRowMock("08/06/2026", "310M", "110M", "88M", "508M"),
                    new RevenueRowMock("07/06/2026", "265M", "95M", "72M", "432M"),
                    new RevenueRowMock("06/06/2026", "220M", "82M", "61M", "363M"),
                    new RevenueRowMock("05/06/2026", "200M", "78M", "55M", "333M"),
                    new RevenueRowMock("04/06/2026", "145M", "55M", "28M", "228M"));
        }
        model.addAttribute("rows", rows);

        List<DailyRevenueMock> chart = rows.stream().map(r -> new DailyRevenueMock(
                r.getPeriod().length() >= 5 ? r.getPeriod().substring(0, 5) : r.getPeriod(),
                parseM(r.getRoom()), parseM(r.getFnb()), parseM(r.getTour()))).collect(Collectors.toList());
        model.addAttribute("chartData", chart);
        return "manager/revenue-daily";
    }

    private static int parseM(String s) {
        try {
            return Integer.parseInt(s.replace("M", "").replace(",", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    // =========================================================================
    // 3. Doanh thu theo tháng
    // =========================================================================

    @GetMapping("/revenue/monthly")
    public String revenueMonthly(Model model) {
        model.addAttribute("todayLabel", todayLabel());
        model.addAttribute("totalYear", "29.5 Tỷ");
        model.addAttribute("bestMonth", "Tháng 5");
        model.addAttribute("bestMonthVal", "6.29 Tỷ");
        model.addAttribute("growthYoY", "+22%");

        List<RevenueRowMock> rows = List.of(
                new RevenueRowMock("Tháng 1/2026", "3.20 Tỷ", "1.10 Tỷ", "620M", "4.92 Tỷ"),
                new RevenueRowMock("Tháng 2/2026", "2.80 Tỷ", "940M", "510M", "4.25 Tỷ"),
                new RevenueRowMock("Tháng 3/2026", "3.50 Tỷ", "1.20 Tỷ", "680M", "5.38 Tỷ"),
                new RevenueRowMock("Tháng 4/2026", "3.80 Tỷ", "1.30 Tỷ", "720M", "5.82 Tỷ"),
                new RevenueRowMock("Tháng 5/2026", "4.10 Tỷ", "1.40 Tỷ", "790M", "6.29 Tỷ"),
                new RevenueRowMock("Tháng 6/2026", "1.13 Tỷ", "487M", "231M", "1.84 Tỷ"));
        model.addAttribute("rows", rows);
        model.addAttribute("chartLabels", List.of("T1", "T2", "T3", "T4", "T5", "T6"));
        model.addAttribute("chartRoom", List.of(3.20, 2.80, 3.50, 3.80, 4.10, 1.13));
        model.addAttribute("chartFnb", List.of(1.10, 0.94, 1.20, 1.30, 1.40, 0.49));
        model.addAttribute("chartTour", List.of(0.62, 0.51, 0.68, 0.72, 0.79, 0.23));
        return "manager/revenue-monthly";
    }

    // =========================================================================
    // 4. Doanh thu theo năm
    // =========================================================================

    @GetMapping("/revenue/yearly")
    public String revenueYearly(Model model) {
        model.addAttribute("todayLabel", todayLabel());
        model.addAttribute("currentYear", "2026");
        model.addAttribute("ytdRevenue", "29.5 Tỷ");
        model.addAttribute("growthVsLast", "+19%");
        model.addAttribute("cagr3y", "+16%");
        model.addAttribute("rows", List.of(
                new RevenueRowMock("2024", "32 Tỷ", "11 Tỷ", "6.2 Tỷ", "49.2 Tỷ"),
                new RevenueRowMock("2025", "38 Tỷ", "13 Tỷ", "7.8 Tỷ", "58.8 Tỷ"),
                new RevenueRowMock("2026 (YTD)", "19.5 Tỷ", "6.4 Tỷ", "3.6 Tỷ", "29.5 Tỷ")));
        model.addAttribute("chartLabels", List.of("2024", "2025", "2026 YTD"));
        model.addAttribute("chartRoom", List.of(32.0, 38.0, 19.5));
        model.addAttribute("chartFnb", List.of(11.0, 13.0, 6.4));
        model.addAttribute("chartTour", List.of(6.2, 7.8, 3.6));
        return "manager/revenue-yearly";
    }

    // =========================================================================
    // 5. Tỷ lệ lấp đầy phòng
    // =========================================================================

    @GetMapping("/analytics/occupancy")
    public String analyticsOccupancy(Model model) {
        model.addAttribute("todayLabel", todayLabel());
        long total = MOCK_TOTAL_ROOMS;
        long occupied = 78;
        try {
            occupied = roomRepository.findOccupied().size();
            total = roomRepository.countTotalRooms();
        } catch (Exception e) {
        }
        long occRate = total > 0 ? Math.round((double) occupied / total * 100) : 0;
        model.addAttribute("currentOccupancy", occRate);
        model.addAttribute("peakOccupancy", Math.max(94, occRate));
        model.addAttribute("peakDate", "03/06");
        model.addAttribute("lowOccupancy", Math.min(62, occRate));
        model.addAttribute("lowDate", "04/06");
        model.addAttribute("avgOccupancy", occRate);
        model.addAttribute("totalRooms", total);
        model.addAttribute("occupiedRooms", occupied);

        List<OccupancyRowMock> rows = new ArrayList<>();
        try {
            for (RoomCategory cat : roomCategoryRepository.findAll()) {
                long catRooms = roomRepository.countByCategory().stream()
                        .filter(r -> cat.getCategoryName().equals(r[0]))
                        .mapToLong(r -> (Long) r[1]).findFirst().orElse(1);
                rows.add(new OccupancyRowMock(cat.getCategoryName(), (int) catRooms,
                        (int) Math.min(100, (double) occupied / total * 100), (int) Math.min(catRooms, occupied)));
            }
        } catch (Exception e) {
        }
        if (rows.isEmpty()) {
            rows = List.of(
                    new OccupancyRowMock("Deluxe River View", 8, 94, 8),
                    new OccupancyRowMock("Garden Bungalow", 6, 82, 5),
                    new OccupancyRowMock("Forest Suite", 4, 76, 3),
                    new OccupancyRowMock("Standard Room", 6, 61, 4));
        }
        model.addAttribute("rows", rows);
        return "manager/analytics-occupancy";
    }

    // ── Inject roomCategoryRepo ──
    private final RoomCategoryRepository roomCategoryRepository;

    // =========================================================================
    // 6. Tỷ lệ tour
    // =========================================================================

    @GetMapping("/analytics/tour")
    public String analyticsTour(Model model) {
        model.addAttribute("todayLabel", todayLabel());
        long totalBookings = 0;
        try {
            totalBookings = tourBookingRepository.count();
        } catch (Exception e) {
        }
        model.addAttribute("totalBookings", totalBookings);
        model.addAttribute("totalRevenue", "231M");
        model.addAttribute("avgTicket", "481K");

        List<TourRowMock> rows = new ArrayList<>();
        try {
            for (Tour tour : tourRepository.findAll()) {
                String name = tour.getTourName() != null ? tour.getTourName() : "Tour #" + tour.getId();
                String cat = tour.getTourType() != null ? tour.getTourType() : "Khác";
                long cnt = totalBookings / Math.max(1, tourRepository.findAll().size());
                rows.add(new TourRowMock(name, cat, (int) cnt,
                        tour.getBasePrice() != null ? fmt(tour.getBasePrice()) : "-", "-", (int) cnt));
            }
        } catch (Exception e) {
        }
        if (rows.isEmpty()) {
            rows = List.of(
                    new TourRowMock("Du thuyền hoàng hôn", "Du thuyền sông", 43, "450K", "19.35M", 87),
                    new TourRowMock("Thăm bản Hmông", "Tham quan bản làng", 38, "580K", "22.04M", 77));
        }
        model.addAttribute("rows", rows);
        return "manager/analytics-tour";
    }

    // =========================================================================
    // 7. Tỷ lệ các món ăn
    // =========================================================================

    @GetMapping("/analytics/food")
    public String analyticsFood(Model model) {
        model.addAttribute("todayLabel", todayLabel());
        long totalOrders = 0;
        try {
            totalOrders = foodOrderRepository.count();
        } catch (Exception e) {
        }
        model.addAttribute("totalOrders", String.valueOf(totalOrders));
        model.addAttribute("totalRevenue", "487M");
        model.addAttribute("avgOrder", "192K");

        List<FoodRowMock> rows = new ArrayList<>();
        try {
            for (MenuItem item : foodItemRepository.findAll()) {
                rows.add(new FoodRowMock(item.getItemName(),
                        item.getCategory() != null ? item.getCategory() : "Khác",
                        0, fmt(item.getPrice()), "-", 0));
            }
            if (rows.size() > 4)
                rows = rows.subList(0, 4);
        } catch (Exception e) {
        }
        if (rows.isEmpty()) {
            rows = List.of(
                    new FoodRowMock("Cá sông nướng", "Món chính", 87, "320K", "27.84M", 100),
                    new FoodRowMock("Tom Yum Soup", "Súp", 72, "185K", "13.32M", 83));
        }
        model.addAttribute("rows", rows);
        model.addAttribute("catLabels", List.of("Món chính", "Súp", "Set Menu", "Tráng miệng", "Đồ uống"));
        model.addAttribute("catValues", List.of(42, 28, 21, 16, 18));
        return "manager/analytics-food";
    }

    // =========================================================================
    // 8. Thời gian lưu trú trung bình
    // =========================================================================

    @GetMapping("/analytics/stay")
    public String analyticsStay(Model model) {
        model.addAttribute("todayLabel", todayLabel());
        model.addAttribute("avgStay", "3.2");
        long guests = 0;
        try {
            guests = bookingRepository.count();
        } catch (Exception e) {
        }
        model.addAttribute("totalGuests", guests);
        model.addAttribute("minStay", 1);
        model.addAttribute("maxStay", 12);
        model.addAttribute("medianStay", "3");
        model.addAttribute("rows", List.of(
                new StayRowMock("Deluxe River View", "4.1 ngày", 38, 8),
                new StayRowMock("Garden Bungalow", "3.6 ngày", 31, 6),
                new StayRowMock("Forest Suite", "5.2 ngày", 20, 4),
                new StayRowMock("Standard Room", "2.1 ngày", 231, 6)));
        model.addAttribute("distLabels", List.of("1 ngày", "2 ngày", "3 ngày", "4 ngày", "5 ngày", "6+ ngày"));
        model.addAttribute("distValues", List.of(58, 72, 85, 61, 28, 16));
        return "manager/analytics-stay";
    }

    // =========================================================================
    // 9. Xuất báo cáo
    // =========================================================================

    @GetMapping("/export")
    public String export(Model model) {
        model.addAttribute("todayLabel", todayLabel());
        model.addAttribute("history", List.of(
                new ExportHistoryMock("Doanh thu tháng 5/2026", "Excel", "01/06/2026 09:15", "Manager Dũng", "2.4 MB"),
                new ExportHistoryMock("Tỷ lệ lấp đầy Q2", "PDF", "30/05/2026 14:30", "Manager Dũng", "1.1 MB"),
                new ExportHistoryMock("Báo cáo tour tháng 4", "CSV", "02/05/2026 10:00", "Manager Dũng", "320 KB"),
                new ExportHistoryMock("Doanh thu năm 2025", "Excel", "15/01/2026 08:45", "Manager Dũng", "5.8 MB")));
        return "manager/export";
    }

    // =========================================================================
    // Inner classes
    // =========================================================================

    @Data
    @AllArgsConstructor
    public static class DailyRevenueMock {
        private String label;
        private int room;
        private int fnb;
        private int tour;
    }

    @Data
    @AllArgsConstructor
    public static class RevenueRowMock {
        private String period;
        private String room;
        private String fnb;
        private String tour;
        private String total;
    }

    @Data
    @AllArgsConstructor
    public static class OccupancyRowMock {
        private String category;
        private int totalRooms;
        private int occupancyPct;
        private int occupied;
    }

    @Data
    @AllArgsConstructor
    public static class TourRowMock {
        private String name;
        private String category;
        private int bookings;
        private String price;
        private String revenue;
        private int barPct;
    }

    @Data
    @AllArgsConstructor
    public static class FoodRowMock {
        private String name;
        private String category;
        private int orders;
        private String price;
        private String revenue;
        private int barPct;
    }

    @Data
    @AllArgsConstructor
    public static class StayRowMock {
        private String category;
        private String avgStay;
        private int guests;
        private int rooms;
    }

    @Data
    @AllArgsConstructor
    public static class ExportHistoryMock {
        private String reportName;
        private String format;
        private String exportedAt;
        private String exportedBy;
        private String fileSize;
    }
}