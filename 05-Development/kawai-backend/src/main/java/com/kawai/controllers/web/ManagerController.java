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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import com.kawai.repositories.*;
import com.kawai.models.*;

import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@RequestMapping("/manager")
@AllArgsConstructor
@PreAuthorize("hasAnyAuthority('OP_ANALYTICS', 'ROLE_ADMIN', 'ROLE_MANAGER')")
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
    private final ExportHistoryRepository exportHistoryRepository;

    private static String todayLabel() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d 'tháng' M, yyyy", new Locale("vi")));
    }


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
        long totalRooms = 1; // fallback tránh chia 0
        long occupied = 0;
        try {
            occupied = roomRepository.findOccupied().size();
            long dbTotal = roomRepository.countTotalRooms();
            if (dbTotal > 0) totalRooms = dbTotal;
        } catch (Exception e) {
            System.err.println("[ManagerController] Cannot fetch room count: " + e.getMessage());
        }
        
        long occupancyRate = totalRooms > 0 ? Math.round((double) occupied / totalRooms * 100) : 0;
        model.addAttribute("occupancyRate", occupancyRate);
        model.addAttribute("totalRooms", totalRooms);
        model.addAttribute("occupiedRooms", occupied);
        Double avgStay = 0.0;
        try {
            avgStay = roomBookingRepository.getAverageStayDuration();
            if (avgStay == null) avgStay = 0.0;
        } catch (Exception e) {}
        model.addAttribute("avgStayDays", String.format(Locale.US, "%.0f", avgStay));

        long totalGuests = 0;
        try {
            totalGuests = bookingRepository.count();
        } catch (Exception e) {}
        model.addAttribute("totalGuests", totalGuests);

        List<DailyRevenueMock> dailyRevenue = new ArrayList<>();
        BigDecimal totalRevRoom7Days = BigDecimal.ZERO;
        BigDecimal totalRevFnb7Days = BigDecimal.ZERO;
        BigDecimal totalRevTour7Days = BigDecimal.ZERO;
        BigDecimal revenueToday = BigDecimal.ZERO;

        try {
            for (int i = 6; i >= 0; i--) {
                LocalDate d = LocalDate.now().minusDays(i);
                String label = d.format(DateTimeFormatter.ofPattern("E", new Locale("vi")));
                
                BigDecimal rDate = roomBookingRepository.revenueOnDate(d);
                if (rDate == null) rDate = BigDecimal.ZERO;
                totalRevRoom7Days = totalRevRoom7Days.add(rDate);

                BigDecimal fDate = foodOrderRepository.revenueOnDate(d.atStartOfDay(), d.plusDays(1).atStartOfDay());
                if (fDate == null) fDate = BigDecimal.ZERO;
                totalRevFnb7Days = totalRevFnb7Days.add(fDate);

                BigDecimal tDate = tourBookingRepository.revenueOnDate(d);
                if (tDate == null) tDate = BigDecimal.ZERO;
                totalRevTour7Days = totalRevTour7Days.add(tDate);

                if (i == 0) {
                    revenueToday = rDate.add(fDate).add(tDate);
                }

                dailyRevenue.add(new DailyRevenueMock(
                    label, 
                    rDate.doubleValue() / 1_000_000.0, 
                    fDate.doubleValue() / 1_000_000.0, 
                    tDate.doubleValue() / 1_000_000.0
                ));
            }
        } catch (Exception e) {}

        model.addAttribute("dailyRevenue", dailyRevenue);
        model.addAttribute("revenueToday", fmt(revenueToday));
        model.addAttribute("revenueMonth", fmt(totalRevRoom7Days.add(totalRevFnb7Days).add(totalRevTour7Days)));
        model.addAttribute("revenueMonthNote", "Tổng cộng 7 ngày qua");
        model.addAttribute("revenueTodayNote", "Phòng · F&B · Tour");

        double revRoomMil = Math.round((totalRevRoom7Days.doubleValue() / 1_000_000.0) * 100.0) / 100.0;
        double revFnbMil = Math.round((totalRevFnb7Days.doubleValue() / 1_000_000.0) * 100.0) / 100.0;
        double revTourMil = Math.round((totalRevTour7Days.doubleValue() / 1_000_000.0) * 100.0) / 100.0;
        double totalRevMil = revRoomMil + revFnbMil + revTourMil;

        int pctRoom = totalRevMil > 0 ? (int) Math.round(revRoomMil * 100 / totalRevMil) : 0;
        int pctFnb = totalRevMil > 0 ? (int) Math.round(revFnbMil * 100 / totalRevMil) : 0;
        int pctTour = totalRevMil > 0 ? (int) Math.round(revTourMil * 100 / totalRevMil) : 0;

        model.addAttribute("revRoom", revRoomMil);
        model.addAttribute("revRoomPct", pctRoom);
        model.addAttribute("revFnb", revFnbMil);
        model.addAttribute("revFnbPct", pctFnb);
        model.addAttribute("revTour", revTourMil);
        model.addAttribute("revTourPct", pctTour);

        int peakOcc = -1;
        String peakDateLabel = "--/--";
        int sumOcc = 0;
        
        LocalDate today = LocalDate.now();
        for (int i = 29; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            int occOnDay = 0;
            try {
                Integer c = roomBookingRepository.countOccupiedRoomsOnDate(d);
                if (c != null) occOnDay = c;
            } catch (Exception e) {}
            
            int pct = totalRooms > 0 ? (int) Math.round((double) occOnDay * 100 / totalRooms) : 0;
            if (i == 0) { pct = (int) occupancyRate; }
            
            if (pct >= peakOcc) {
                peakOcc = pct;
                peakDateLabel = d.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM"));
            }
            sumOcc += pct;
        }

        model.addAttribute("peakOccupancyDate", peakDateLabel);
        model.addAttribute("peakOccupancy", peakOcc == -1 ? 0 : peakOcc);
        model.addAttribute("avgOccupancyMonth", sumOcc / 30);

        try {
            String topDish = foodOrderDetailRepository.findTopDishName();
            Integer topDishOrders = foodOrderDetailRepository.findTopDishOrders();
            model.addAttribute("topDish", topDish != null ? topDish : "Chưa có");
            model.addAttribute("topDishOrders", topDishOrders != null ? topDishOrders : 0);
        } catch (Exception e) {
            model.addAttribute("topDish", "Chưa có");
            model.addAttribute("topDishOrders", 0);
        }

        try {
            String topTour = tourBookingRepository.findTopTourName();
            Integer topTourBookings = tourBookingRepository.findTopTourBookings();
            model.addAttribute("topTour", topTour != null ? topTour : "Chưa có");
            model.addAttribute("topTourBookings", topTourBookings != null ? topTourBookings : 0);
        } catch (Exception e) {
            model.addAttribute("topTour", "Chưa có");
            model.addAttribute("topTourBookings", 0);
        }

        return "manager/dashboard";
    }

    // =========================================================================
    // 2. Doanh thu theo ngày
    // =========================================================================

    @GetMapping("/revenue/daily")
    public String revenueDaily(Model model) {
        model.addAttribute("todayLabel", todayLabel());

        LocalDate today = LocalDate.now();
        BigDecimal roomToday = roomBookingRepository.revenueBetween(today, today);
        BigDecimal fnbToday = foodOrderRepository.revenueBetween(today.atStartOfDay(), today.plusDays(1).atStartOfDay());
        BigDecimal tourToday = tourBookingRepository.revenueBetween(today, today);
        BigDecimal totalToday = roomToday.add(fnbToday).add(tourToday);

        LocalDate weekStart = today.minusDays(6);
        BigDecimal roomWeek = roomBookingRepository.revenueBetween(weekStart, today);
        BigDecimal fnbWeek = foodOrderRepository.revenueBetween(weekStart.atStartOfDay(), today.plusDays(1).atStartOfDay());
        BigDecimal tourWeek = tourBookingRepository.revenueBetween(weekStart, today);
        BigDecimal totalWeek = roomWeek.add(fnbWeek).add(tourWeek);

        LocalDate monthStart = today.minusDays(29);
        BigDecimal roomMonth = roomBookingRepository.revenueBetween(monthStart, today);
        BigDecimal fnbMonth = foodOrderRepository.revenueBetween(monthStart.atStartOfDay(), today.plusDays(1).atStartOfDay());
        BigDecimal tourMonth = tourBookingRepository.revenueBetween(monthStart, today);
        BigDecimal totalMonth = roomMonth.add(fnbMonth).add(tourMonth);

        model.addAttribute("totalToday", fmt(totalToday));
        model.addAttribute("totalWeek", fmt(totalWeek));
        model.addAttribute("totalMonth", fmt(totalMonth));

        List<RevenueRowMock> rows = new ArrayList<>();
        List<DailyRevenueMock> chart = new ArrayList<>();
        try {
            for (int i = 8; i >= 0; i--) {
                LocalDate d = today.minusDays(i);
                String lbl = d.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                
                BigDecimal rDate = roomBookingRepository.revenueOnDate(d);
                if (rDate == null) rDate = BigDecimal.ZERO;
                
                BigDecimal fDate = foodOrderRepository.revenueOnDate(d.atStartOfDay(), d.plusDays(1).atStartOfDay());
                if (fDate == null) fDate = BigDecimal.ZERO;
                
                BigDecimal tDate = tourBookingRepository.revenueOnDate(d);
                if (tDate == null) tDate = BigDecimal.ZERO;
                
                BigDecimal totalDate = rDate.add(fDate).add(tDate);
                
                rows.add(0, new RevenueRowMock(lbl, fmt(rDate), fmt(fDate), fmt(tDate), fmt(totalDate)));
                
                chart.add(new DailyRevenueMock(d.format(DateTimeFormatter.ofPattern("dd/MM")), 
                    rDate.doubleValue() / 1_000_000.0, 
                    fDate.doubleValue() / 1_000_000.0, 
                    tDate.doubleValue() / 1_000_000.0));
            }
        } catch (Exception e) {}

        model.addAttribute("rows", rows);
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

        LocalDate now = LocalDate.now();
        BigDecimal ytdRoom = roomBookingRepository.revenueBetween(LocalDate.of(now.getYear(), 1, 1), LocalDate.of(now.getYear(), now.getMonthValue(), now.lengthOfMonth()));
        BigDecimal ytdFnb = foodOrderRepository.revenueBetween(java.time.LocalDateTime.of(now.getYear(), 1, 1, 0, 0), LocalDate.of(now.getYear(), now.getMonthValue(), now.lengthOfMonth()).atTime(23, 59, 59));
        BigDecimal ytdTour = tourBookingRepository.revenueBetween(LocalDate.of(now.getYear(), 1, 1), LocalDate.of(now.getYear(), now.getMonthValue(), now.lengthOfMonth()));
        
        BigDecimal totalYear = ytdRoom.add(ytdFnb).add(ytdTour);
        model.addAttribute("totalYear", fmt(totalYear));
        
        // Tính toán YoY (Tăng trưởng so với cùng kỳ năm trước)
        BigDecimal ytdRoomLastYear = roomBookingRepository.revenueBetween(LocalDate.of(now.getYear() - 1, 1, 1), LocalDate.of(now.getYear() - 1, now.getMonthValue(), now.lengthOfMonth()));
        BigDecimal ytdFnbLastYear = foodOrderRepository.revenueBetween(java.time.LocalDateTime.of(now.getYear() - 1, 1, 1, 0, 0), LocalDate.of(now.getYear() - 1, now.getMonthValue(), now.lengthOfMonth()).atTime(23, 59, 59));
        BigDecimal ytdTourLastYear = tourBookingRepository.revenueBetween(LocalDate.of(now.getYear() - 1, 1, 1), LocalDate.of(now.getYear() - 1, now.getMonthValue(), now.lengthOfMonth()));
        
        BigDecimal totalLastYear = ytdRoomLastYear.add(ytdFnbLastYear).add(ytdTourLastYear);
        String yoyStr = "-";
        if (totalLastYear.compareTo(BigDecimal.ZERO) > 0) {
            double yoy = (totalYear.doubleValue() - totalLastYear.doubleValue()) / totalLastYear.doubleValue() * 100;
            yoyStr = (yoy >= 0 ? "+" : "") + String.format(java.util.Locale.US, "%.1f%%", yoy);
        }
        model.addAttribute("growthYoY", yoyStr);

        List<RevenueRowMock> rows = new ArrayList<>();
        List<String> chartLabels = new ArrayList<>();
        List<Double> chartRoom = new ArrayList<>();
        List<Double> chartFnb = new ArrayList<>();
        List<Double> chartTour = new ArrayList<>();

        BigDecimal maxMonthRev = BigDecimal.ZERO;
        String bestMonthLabel = "-";

        for (int i = 5; i >= 0; i--) {
            LocalDate mDate = now.minusMonths(i);
            LocalDate start = LocalDate.of(mDate.getYear(), mDate.getMonthValue(), 1);
            LocalDate end = LocalDate.of(mDate.getYear(), mDate.getMonthValue(), mDate.lengthOfMonth());

            BigDecimal rMonth = roomBookingRepository.revenueBetween(start, end);
            BigDecimal fMonth = foodOrderRepository.revenueBetween(start.atStartOfDay(), end.atTime(23, 59, 59));
            BigDecimal tMonth = tourBookingRepository.revenueBetween(start, end);

            BigDecimal totalM = rMonth.add(fMonth).add(tMonth);

            if (totalM.compareTo(maxMonthRev) > 0) {
                maxMonthRev = totalM;
                bestMonthLabel = "Tháng " + mDate.getMonthValue();
            }

            rows.add(0, new RevenueRowMock("Tháng " + mDate.getMonthValue() + "/" + mDate.getYear(), fmt(rMonth), fmt(fMonth), fmt(tMonth), fmt(totalM)));
            chartLabels.add("T" + mDate.getMonthValue());
            chartRoom.add(rMonth.doubleValue() / 1_000_000.0);
            chartFnb.add(fMonth.doubleValue() / 1_000_000.0);
            chartTour.add(tMonth.doubleValue() / 1_000_000.0);
        }

        model.addAttribute("bestMonth", bestMonthLabel);
        model.addAttribute("bestMonthVal", fmt(maxMonthRev));

        model.addAttribute("rows", rows);
        model.addAttribute("chartLabels", chartLabels);
        model.addAttribute("chartRoom", chartRoom);
        model.addAttribute("chartFnb", chartFnb);
        model.addAttribute("chartTour", chartTour);
        return "manager/revenue-monthly";
    }

    // =========================================================================
    // 4. Doanh thu theo năm
    // =========================================================================

    @GetMapping("/revenue/yearly")
    public String revenueYearly(Model model) {
        model.addAttribute("todayLabel", todayLabel());
        model.addAttribute("currentYear", String.valueOf(LocalDate.now().getYear()));
        
        List<RevenueRowMock> rows = new ArrayList<>();
        List<String> chartLabels = new ArrayList<>();
        List<Double> chartRoom = new ArrayList<>();
        List<Double> chartFnb = new ArrayList<>();
        List<Double> chartTour = new ArrayList<>();

        int currentYear = LocalDate.now().getYear();

        for (int i = 2; i >= 0; i--) {
            int y = currentYear - i;
            LocalDate start = LocalDate.of(y, 1, 1);
            LocalDate end = LocalDate.of(y, 12, 31);

            BigDecimal rYear = roomBookingRepository.revenueBetween(start, end);
            BigDecimal fYear = foodOrderRepository.revenueBetween(start.atStartOfDay(), end.atTime(23, 59, 59));
            BigDecimal tYear = tourBookingRepository.revenueBetween(start, end);

            BigDecimal totalY = rYear.add(fYear).add(tYear);

            String lbl = String.valueOf(y) + (i == 0 ? " (YTD)" : "");
            rows.add(0, new RevenueRowMock(lbl, fmt(rYear), fmt(fYear), fmt(tYear), fmt(totalY)));

            chartLabels.add(lbl);
            chartRoom.add(rYear.doubleValue() / 1_000_000.0);
            chartFnb.add(fYear.doubleValue() / 1_000_000.0);
            chartTour.add(tYear.doubleValue() / 1_000_000.0);
            
            if (i == 0) {
                model.addAttribute("ytdRevenue", fmt(totalY));
            }
        }
        
        model.addAttribute("growthVsLast", "-");
        model.addAttribute("cagr3y", "-");

        model.addAttribute("rows", rows);
        model.addAttribute("chartLabels", chartLabels);
        model.addAttribute("chartRoom", chartRoom);
        model.addAttribute("chartFnb", chartFnb);
        model.addAttribute("chartTour", chartTour);
        return "manager/revenue-yearly";
    }

    // =========================================================================
    // 5. Tỷ lệ lấp đầy phòng
    // =========================================================================

    @GetMapping("/analytics/occupancy")
    public String analyticsOccupancy(Model model) {
        model.addAttribute("todayLabel", todayLabel());
        long total = 0;
        long occupied = 0;
        try {
            occupied = roomRepository.findOccupied().size();
            total = roomRepository.countTotalRooms();
        } catch (Exception e) {}
        
        long occRate = total > 0 ? Math.round((double) occupied / total * 100) : 0;
        model.addAttribute("currentOccupancy", occRate);
        
        model.addAttribute("totalRooms", total);
        model.addAttribute("occupiedRooms", occupied);

        // 30 days chart
        List<Integer> chartOccVals = new ArrayList<>();
        List<String> chartOccLabels = new ArrayList<>();
        int peakOcc = -1;
        String peakDateLabel = "--/--";
        int lowOcc = 101;
        String lowDateLabel = "--/--";
        int sumOcc = 0;
        
        LocalDate today = LocalDate.now();
        for (int i = 29; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            int occOnDay = 0;
            try {
                Integer c = roomBookingRepository.countOccupiedRoomsOnDate(d);
                if (c != null) occOnDay = c;
            } catch (Exception e) {}
            
            int pct = total > 0 ? (int) Math.round((double) occOnDay * 100 / total) : 0;
            if (i == 0) { pct = (int) occRate; }
            
            chartOccVals.add(pct);
            chartOccLabels.add(d.format(java.time.format.DateTimeFormatter.ofPattern("d/MM")));
            
            if (pct >= peakOcc) {
                peakOcc = pct;
                peakDateLabel = d.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM"));
            }
            if (pct <= lowOcc) {
                lowOcc = pct;
                lowDateLabel = d.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM"));
            }
            sumOcc += pct;
        }
        
        model.addAttribute("peakOccupancy", peakOcc == -1 ? 0 : peakOcc);
        model.addAttribute("peakDate", peakDateLabel);
        model.addAttribute("lowOccupancy", lowOcc == 101 ? 0 : lowOcc);
        model.addAttribute("lowDate", lowDateLabel);
        model.addAttribute("avgOccupancy", sumOcc / 30);
        
        model.addAttribute("chartOccVals", chartOccVals);
        model.addAttribute("chartOccLabels", chartOccLabels);

        List<OccupancyRowMock> rows = new ArrayList<>();
        try {
            List<Object[]> roomCounts = roomRepository.countByCategory();
            List<com.kawai.models.Room> occupiedList = roomRepository.findOccupied();
            for (RoomCategory cat : roomCategoryRepository.findAll()) {
                long catTotal = roomCounts.stream().filter(r -> cat.getCategoryName().equals(r[0])).mapToLong(r -> (Long) r[1]).findFirst().orElse(0);
                long catOccupied = occupiedList.stream().filter(r -> r.getCategory() != null && r.getCategory().getId().equals(cat.getId())).count();
                int occPct = catTotal > 0 ? (int)(catOccupied * 100 / catTotal) : 0;
                rows.add(new OccupancyRowMock(cat.getCategoryName(), (int) catTotal, occPct, (int) catOccupied));
            }
        } catch (Exception e) {}
        
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
        BigDecimal totalRevenue = BigDecimal.ZERO;
        
        try {
            totalBookings = tourBookingRepository.count();
        } catch (Exception e) {}
        
        List<TourRowMock> rows = new ArrayList<>();
        try {
            List<Object[]> tourData = tourBookingRepository.getTourAnalytics();
            for (Object[] r : tourData) {
                String name = r[0] != null ? r[0].toString() : "Khác";
                String cat = r[1] != null ? r[1].toString() : "Khác";
                int cnt = r[2] != null ? ((Number) r[2]).intValue() : 0;
                BigDecimal rev = r[3] != null ? (BigDecimal) r[3] : BigDecimal.ZERO;
                BigDecimal basePrice = r[4] != null ? (BigDecimal) r[4] : BigDecimal.ZERO;
                
                totalRevenue = totalRevenue.add(rev);
                rows.add(new TourRowMock(name, cat, cnt, fmt(basePrice), fmt(rev), 0));
            }
            rows.sort((a, b) -> Integer.compare(b.getBookings(), a.getBookings()));
            int maxBookings = rows.stream().mapToInt(TourRowMock::getBookings).max().orElse(0);
            for (TourRowMock row : rows) {
                row.setBarPct(maxBookings > 0 ? (row.getBookings() * 100 / maxBookings) : 0);
            }
        } catch (Exception e) {}
        
        model.addAttribute("totalBookings", totalBookings);
        model.addAttribute("totalRevenue", fmt(totalRevenue));
        model.addAttribute("avgTicket", totalBookings > 0 ? fmt(totalRevenue.divide(BigDecimal.valueOf(totalBookings), java.math.RoundingMode.HALF_UP)) : "0");
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
        BigDecimal totalRevenue = BigDecimal.ZERO;
        try {
            totalOrders = foodOrderRepository.count();
        } catch (Exception e) {}

        List<FoodRowMock> rows = new ArrayList<>();
        java.util.Map<String, Integer> catCounts = new java.util.HashMap<>();
        try {
            List<Object[]> foodData = foodOrderDetailRepository.getFoodAnalytics();
            for (Object[] r : foodData) {
                String name = r[0] != null ? r[0].toString() : "Khác";
                String cat = r[1] != null ? r[1].toString() : "Khác";
                int qty = r[2] != null ? ((Number) r[2]).intValue() : 0;
                BigDecimal rev = r[3] != null ? (BigDecimal) r[3] : BigDecimal.ZERO;
                BigDecimal price = r[4] != null ? (BigDecimal) r[4] : BigDecimal.ZERO;
                
                catCounts.put(cat, catCounts.getOrDefault(cat, 0) + qty);
                totalRevenue = totalRevenue.add(rev);
                rows.add(new FoodRowMock(name, cat, qty, fmt(price), fmt(rev), 100));
            }
            rows.sort((a, b) -> Integer.compare(b.getOrders(), a.getOrders()));
            if (rows.size() > 10) rows = rows.subList(0, 10);
            
            for (FoodRowMock row : rows) {
                row.setBarPct(totalRevenue.compareTo(BigDecimal.ZERO) > 0 ? (int)(parseM(row.getRevenue()) * 1000000L * 100 / totalRevenue.longValue()) : 0);
            }
        } catch (Exception e) {}
        
        List<String> catLabels = new ArrayList<>(catCounts.keySet());
        List<Integer> catValues = catLabels.stream().map(catCounts::get).collect(Collectors.toList());
        
        model.addAttribute("totalOrders", String.valueOf(totalOrders));
        model.addAttribute("totalRevenue", fmt(totalRevenue));
        model.addAttribute("avgOrder", totalOrders > 0 ? fmt(totalRevenue.divide(BigDecimal.valueOf(totalOrders), java.math.RoundingMode.HALF_UP)) : "0");
        model.addAttribute("rows", rows);
        model.addAttribute("catLabels", catLabels.isEmpty() ? List.of("Tất cả") : catLabels);
        model.addAttribute("catValues", catValues.isEmpty() ? List.of(100) : catValues);
        return "manager/analytics-food";
    }

    // =========================================================================
    // 8. Thời gian lưu trú trung bình
    // =========================================================================

    @GetMapping("/analytics/stay")
    public String analyticsStay(Model model) {
        model.addAttribute("todayLabel", todayLabel());
        Double avgStay = 0.0;
        try {
            avgStay = roomBookingRepository.getAverageStayDuration();
            if (avgStay == null) avgStay = 0.0;
        } catch (Exception e) {}
        
        model.addAttribute("avgStay", String.format(Locale.US, "%.0f", avgStay));
        
        long guests = 0;
        try {
            guests = bookingRepository.count();
        } catch (Exception e) {}
        
        model.addAttribute("totalGuests", guests);
        model.addAttribute("minStay", 1);
        model.addAttribute("maxStay", 12);
        model.addAttribute("medianStay", String.format(Locale.US, "%.0f", avgStay));
        
        List<StayRowMock> rows = new ArrayList<>();
        try {
            List<Object[]> roomCounts = roomRepository.countByCategory();
            for (RoomCategory cat : roomCategoryRepository.findAll()) {
                long catTotal = roomCounts.stream().filter(r -> cat.getCategoryName().equals(r[0])).mapToLong(r -> (Long) r[1]).findFirst().orElse(0);
                long catGuests = (guests * catTotal) / 50; // simple rough estimation for display
                rows.add(new StayRowMock(cat.getCategoryName(), String.format(Locale.US, "%.0f ngày", avgStay), (int) catGuests, (int) catTotal));
            }
        } catch (Exception e) {}
        
        model.addAttribute("rows", rows);
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
        model.addAttribute("history", exportHistoryRepository.findAllByOrderByExportedAtDesc());
        return "manager/export";
    }

    // =========================================================================
    // Inner classes
    // =========================================================================

    @Data
    @AllArgsConstructor
    public static class DailyRevenueMock {
        private String label;
        private double room;
        private double fnb;
        private double tour;
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