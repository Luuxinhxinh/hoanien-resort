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
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final RefundRequestRepository refundRequestRepository;
    private final HousekeepingTaskRepository housekeepingTaskRepository;
    private final EmployeeRepository employeeRepository;
    private final RoomCategoryRepository roomCategoryRepository;

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

    @GetMapping("/refunds")
    public String refundManagement(Model model) {
        model.addAttribute("pendingRefunds", refundRequestRepository.findByStatusOrderByCreatedAtDesc("Pending"));
        model.addAttribute("completedRefunds", refundRequestRepository.findByStatusOrderByCreatedAtDesc("COMPLETED"));
        return "manager/refund-management";
    }

    @GetMapping("/approvals")
    public String approvals(Model model) {
        model.addAttribute("todayLabel", todayLabel());
        
        List<String> types = java.util.Arrays.asList("Manager_Approval", "Late_Checkout_Waiver", "Cancellation_Fee_Waiver", "Room_Downgrade_Refund");
        List<HotelOperation> pendingOps = housekeepingTaskRepository.findByOperationalTypesAndStatusSorted(types, "Pending");
        List<HotelOperation> completedOps = housekeepingTaskRepository.findByOperationalTypesAndStatusSorted(types, "Completed");
        List<HotelOperation> rejectedOps = housekeepingTaskRepository.findByOperationalTypesAndStatusSorted(types, "Rejected");
        
        List<ApprovalDTO> pending = convertToApprovalDTOs(pendingOps);
        List<ApprovalDTO> completed = convertToApprovalDTOs(completedOps);
        List<ApprovalDTO> rejected = convertToApprovalDTOs(rejectedOps);
        
        model.addAttribute("pendingApprovals", pending);
        model.addAttribute("completedApprovals", completed);
        model.addAttribute("rejectedApprovals", rejected);
        
        return "manager/approvals";
    }

    private List<ApprovalDTO> convertToApprovalDTOs(List<HotelOperation> ops) {
        List<ApprovalDTO> dtos = new ArrayList<>();
        for (HotelOperation op : ops) {
            Long bookingId = parseBookingIdFromNotes(op.getNotes());
            String customerName = "N/A";
            String totalPriceStr = "N/A";
            String bookingDetails = "N/A";
            String promoCode = "N/A";
            
            if (bookingId != null) {
                Booking booking = bookingRepository.findById(bookingId).orElse(null);
                if (booking != null) {
                    customerName = booking.getCustomer() != null ? booking.getCustomer().getFullName() : "N/A";
                    totalPriceStr = fmt(booking.getTotalPrice()) + " VNĐ";
                    promoCode = booking.getAppliedPromotion() != null ? booking.getAppliedPromotion().getPromoCode() : "N/A";
                    if ("N/A".equals(promoCode) && op.getNotes() != null) {
                        java.util.regex.Matcher m = java.util.regex.Pattern.compile("Mã giảm giá (\\S+) áp dụng").matcher(op.getNotes());
                        if (m.find()) {
                            promoCode = m.group(1);
                        }
                    }
                    
                    if (booking instanceof RoomBooking rb) {
                        bookingDetails = "Đặt phòng (" + rb.getCheckInDate() + " -> " + rb.getCheckOutDate() + ")";
                    } else if (booking instanceof TourBooking tb) {
                        bookingDetails = "Đặt Tour: " + (tb.getSchedule() != null && tb.getSchedule().getTour() != null ? tb.getSchedule().getTour().getTourName() : "N/A");
                    }
                }
            }
            
            dtos.add(new ApprovalDTO(
                op.getId(),
                bookingId,
                customerName,
                promoCode,
                op.getNotes(),
                op.getStatus(),
                op.getCreatedAt() != null ? op.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "N/A",
                bookingDetails,
                totalPriceStr,
                op.getOperationalType()
            ));
        }
        return dtos;
    }

    private Long parseBookingIdFromNotes(String notes) {
        if (notes == null) return null;
        int idx = notes.lastIndexOf("booking ID: ");
        if (idx == -1) {
            idx = notes.lastIndexOf("ID: ");
        }
        if (idx != -1) {
            try {
                String sub = notes.substring(idx + 12).trim();
                if (sub.isEmpty() || !Character.isDigit(sub.charAt(0))) {
                    sub = notes.substring(idx + 4).trim();
                }
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < sub.length(); i++) {
                    char c = sub.charAt(i);
                    if (Character.isDigit(c)) {
                        sb.append(c);
                    } else {
                        break;
                    }
                }
                if (sb.length() > 0) {
                    return Long.parseLong(sb.toString());
                }
            } catch (Exception e) {
                // fallback
            }
        }
        return null;
    }

    @GetMapping({ "/dashboard", "/" })
    public String dashboard(Model model) {
        model.addAttribute("todayLabel", todayLabel());
        long totalRooms = 1; // fallback tránh chia 0
        long occupied = 0;
        try {
            occupied = roomRepository.findOccupied().size();
            long dbTotal = roomRepository.countTotalRooms();
            if (dbTotal > 0)
                totalRooms = dbTotal;
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
            if (avgStay == null)
                avgStay = 0.0;
        } catch (Exception e) {
        }
        model.addAttribute("avgStayDays", String.format(Locale.US, "%.0f", avgStay));

        long totalGuests = 0;
        try {
            totalGuests = bookingRepository.count();
        } catch (Exception e) {
        }
        model.addAttribute("totalGuests", totalGuests);

        List<DailyRevenueDTO> dailyRevenue = new ArrayList<>();
        BigDecimal totalRevRoom7Days = BigDecimal.ZERO;
        BigDecimal totalRevFnb7Days = BigDecimal.ZERO;
        BigDecimal totalRevTour7Days = BigDecimal.ZERO;
        BigDecimal revenueToday = BigDecimal.ZERO;

        try {
            for (int i = 6; i >= 0; i--) {
                LocalDate d = LocalDate.now().minusDays(i);
                String label = d.format(DateTimeFormatter.ofPattern("E", new Locale("vi")));

                BigDecimal rDate = roomBookingRepository.revenueOnDate(d);
                if (rDate == null)
                    rDate = BigDecimal.ZERO;
                totalRevRoom7Days = totalRevRoom7Days.add(rDate);

                BigDecimal fDate = foodOrderRepository.revenueOnDate(d.atStartOfDay(), d.plusDays(1).atStartOfDay());
                if (fDate == null)
                    fDate = BigDecimal.ZERO;
                totalRevFnb7Days = totalRevFnb7Days.add(fDate);

                BigDecimal tDate = tourBookingRepository.revenueOnDate(d);
                if (tDate == null)
                    tDate = BigDecimal.ZERO;
                totalRevTour7Days = totalRevTour7Days.add(tDate);

                if (i == 0) {
                    revenueToday = rDate.add(fDate).add(tDate);
                }

                dailyRevenue.add(new DailyRevenueDTO(
                        label,
                        rDate.doubleValue() / 1_000_000.0,
                        fDate.doubleValue() / 1_000_000.0,
                        tDate.doubleValue() / 1_000_000.0));
            }
        } catch (Exception e) {
        }

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
        List<Integer> occVals = new ArrayList<>();
        List<String> occLabels = new ArrayList<>();

        LocalDate today = LocalDate.now();
        for (int i = 29; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            int occOnDay = 0;
            try {
                Integer c = roomBookingRepository.countOccupiedRoomsOnDate(d);
                if (c != null)
                    occOnDay = c;
            } catch (Exception e) {
            }

            int pct = totalRooms > 0 ? (int) Math.round((double) occOnDay * 100 / totalRooms) : 0;
            if (i == 0) {
                pct = (int) occupancyRate;
            }

            occVals.add(pct);
            occLabels.add(d.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM")));

            if (pct >= peakOcc) {
                peakOcc = pct;
                peakDateLabel = d.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM"));
            }
            sumOcc += pct;
        }

        model.addAttribute("occVals", occVals);
        model.addAttribute("occLabels", occLabels);

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
        }

        java.util.List<String> todayChartLabels = java.util.List.of("00:00", "04:00", "08:00", "12:00", "16:00", "20:00", "24:00");
        java.util.List<Double> todayRoomValues = new java.util.ArrayList<>();
        java.util.List<Double> todayFnbValues = new java.util.ArrayList<>();
        java.util.List<Double> todayTourValues = new java.util.ArrayList<>();

        for (int h = 0; h <= 24; h += 4) {
            if (h == 0) {
                todayRoomValues.add(0.0);
                todayFnbValues.add(0.0);
                todayTourValues.add(0.0);
                continue;
            }
            java.time.LocalDateTime start = today.atTime(h - 4, 0);
            java.time.LocalDateTime end = h == 24 ? today.plusDays(1).atStartOfDay() : today.atTime(h, 0);
            
            java.util.List<PaymentTransaction> txs = paymentTransactionRepository.findBetween(start, end, PaymentStatus.SUCCESS);
            
            BigDecimal room = BigDecimal.ZERO;
            BigDecimal fnb = BigDecimal.ZERO;
            BigDecimal tour = BigDecimal.ZERO;

            for (PaymentTransaction t : txs) {
                if (t.getFoodOrder() != null) {
                    fnb = fnb.add(t.getAmount());
                } else if (t.getBooking() != null) {
                    if (t.getBooking() instanceof TourBooking) {
                        tour = tour.add(t.getAmount());
                    } else {
                        room = room.add(t.getAmount());
                    }
                } else if (t.getInvoice() != null) {
                    // For simplicity, we assign consolidated invoice revenue to room since it primarily contains room charges
                    room = room.add(t.getAmount());
                } else {
                    room = room.add(t.getAmount());
                }
            }

            todayRoomValues.add(Math.round((room.doubleValue() / 1_000_000.0) * 10.0) / 10.0);
            todayFnbValues.add(Math.round((fnb.doubleValue() / 1_000_000.0) * 10.0) / 10.0);
            todayTourValues.add(Math.round((tour.doubleValue() / 1_000_000.0) * 10.0) / 10.0);
        }

        model.addAttribute("todayChartLabels", todayChartLabels);
        model.addAttribute("todayRoomValues", todayRoomValues);
        model.addAttribute("todayFnbValues", todayFnbValues);
        model.addAttribute("todayTourValues", todayTourValues);

        return "manager/dashboard";
    }

    // =========================================================================
    // 2. Doanh thu theo ngày
    // =========================================================================

    @GetMapping("/revenue")
    public String revenue(Model model) {
        model.addAttribute("todayLabel", todayLabel());
        LocalDate today = LocalDate.now();

        // --- DAILY ---
        BigDecimal roomToday = roomBookingRepository.revenueBetween(today, today);
        BigDecimal fnbToday = foodOrderRepository.revenueBetween(today.atStartOfDay(), today.plusDays(1).atStartOfDay());
        BigDecimal tourToday = tourBookingRepository.revenueBetween(today, today);
        BigDecimal totalToday = roomToday.add(fnbToday).add(tourToday);

        LocalDate weekStart = today.minusDays(6);
        BigDecimal roomWeek = roomBookingRepository.revenueBetween(weekStart, today);
        BigDecimal fnbWeek = foodOrderRepository.revenueBetween(weekStart.atStartOfDay(), today.plusDays(1).atStartOfDay());
        BigDecimal tourWeek = tourBookingRepository.revenueBetween(weekStart, today);
        BigDecimal totalWeek = roomWeek.add(fnbWeek).add(tourWeek);

        LocalDate yesterday = today.minusDays(1);
        BigDecimal roomYesterday = roomBookingRepository.revenueBetween(yesterday, yesterday);
        BigDecimal fnbYesterday = foodOrderRepository.revenueBetween(yesterday.atStartOfDay(), yesterday.plusDays(1).atStartOfDay());
        BigDecimal tourYesterday = tourBookingRepository.revenueBetween(yesterday, yesterday);
        BigDecimal totalYesterday = roomYesterday.add(fnbYesterday).add(tourYesterday);

        model.addAttribute("totalToday", fmt(totalToday));
        model.addAttribute("totalWeek", fmt(totalWeek));
        model.addAttribute("totalYesterday", fmt(totalYesterday));
        model.addAttribute("yesterdayLabel", "Ngày " + yesterday.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        String todayGrowthText = "";
        String todayGrowthColor = "";
        if (totalYesterday.compareTo(BigDecimal.ZERO) == 0) {
            if (totalToday.compareTo(BigDecimal.ZERO) > 0) {
                todayGrowthText = "Tăng 100% so với hôm qua";
                todayGrowthColor = "#2e7d32";
            } else {
                todayGrowthText = "Bằng hôm qua";
                todayGrowthColor = "#757575";
            }
        } else {
            double diff = totalToday.subtract(totalYesterday).doubleValue();
            double pct = (diff / totalYesterday.doubleValue()) * 100;
            if (pct > 0) {
                todayGrowthText = String.format("Tăng %.1f%% so với hôm qua", pct).replace(".0%", "%");
                todayGrowthColor = "#2e7d32";
            } else if (pct < 0) {
                todayGrowthText = String.format("Giảm %.1f%% so với hôm qua", Math.abs(pct)).replace(".0%", "%");
                todayGrowthColor = "#d32f2f";
            } else {
                todayGrowthText = "Bằng hôm qua";
                todayGrowthColor = "#757575";
            }
        }
        model.addAttribute("todayGrowthText", todayGrowthText);
        model.addAttribute("todayGrowthColor", todayGrowthColor);

        List<RevenueRowDTO> dailyRows = new ArrayList<>();
        List<String> dailyChartLabels = new ArrayList<>();
        List<Double> dailyChartRoom = new ArrayList<>();
        List<Double> dailyChartFnb = new ArrayList<>();
        List<Double> dailyChartTour = new ArrayList<>();
        BigDecimal peakRevenue = BigDecimal.ZERO;
        String peakRevenueDate = "-";

        try {
            for (int i = 6; i >= 0; i--) {
                LocalDate d = today.minusDays(i);
                String lbl = d.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                BigDecimal rDate = roomBookingRepository.revenueOnDate(d);
                if (rDate == null) rDate = BigDecimal.ZERO;
                BigDecimal fDate = foodOrderRepository.revenueOnDate(d.atStartOfDay(), d.plusDays(1).atStartOfDay());
                if (fDate == null) fDate = BigDecimal.ZERO;
                BigDecimal tDate = tourBookingRepository.revenueOnDate(d);
                if (tDate == null) tDate = BigDecimal.ZERO;

                BigDecimal totalDate = rDate.add(fDate).add(tDate);
                dailyRows.add(0, new RevenueRowDTO(lbl, fmt(rDate), fmt(fDate), fmt(tDate), fmt(totalDate)));

                if (totalDate.compareTo(peakRevenue) >= 0) {
                    peakRevenue = totalDate;
                    peakRevenueDate = "Ngày " + d.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                }

                dailyChartLabels.add(d.format(DateTimeFormatter.ofPattern("dd/MM")));
                dailyChartRoom.add(rDate.doubleValue() / 1_000_000.0);
                dailyChartFnb.add(fDate.doubleValue() / 1_000_000.0);
                dailyChartTour.add(tDate.doubleValue() / 1_000_000.0);
            }
        } catch (Exception e) {}

        model.addAttribute("dailyRows", dailyRows);
        model.addAttribute("dailyChartLabels", dailyChartLabels);
        model.addAttribute("dailyChartRoom", dailyChartRoom);
        model.addAttribute("dailyChartFnb", dailyChartFnb);
        model.addAttribute("dailyChartTour", dailyChartTour);
        model.addAttribute("peakRevenue", fmt(peakRevenue));
        model.addAttribute("peakRevenueDate", peakRevenueDate);

        // --- MONTHLY ---
        LocalDate now = LocalDate.now();
        
        LocalDate currentMonthStart = LocalDate.of(now.getYear(), now.getMonthValue(), 1);
        BigDecimal currentMonthRoom = roomBookingRepository.revenueBetween(currentMonthStart, now);
        BigDecimal currentMonthFnb = foodOrderRepository.revenueBetween(currentMonthStart.atStartOfDay(), now.plusDays(1).atStartOfDay());
        BigDecimal currentMonthTour = tourBookingRepository.revenueBetween(currentMonthStart, now);
        BigDecimal currentMonthTotal = currentMonthRoom.add(currentMonthFnb).add(currentMonthTour);
        model.addAttribute("currentMonthTotal", fmt(currentMonthTotal));
        model.addAttribute("currentMonthLabel", "Tháng " + now.getMonthValue() + "/" + now.getYear());

        LocalDate previousMonthStart = currentMonthStart.minusMonths(1);
        LocalDate previousMonthEnd = currentMonthStart.minusDays(1);
        BigDecimal previousMonthRoom = roomBookingRepository.revenueBetween(previousMonthStart, previousMonthEnd);
        BigDecimal previousMonthFnb = foodOrderRepository.revenueBetween(previousMonthStart.atStartOfDay(), previousMonthEnd.atTime(23, 59, 59));
        BigDecimal previousMonthTour = tourBookingRepository.revenueBetween(previousMonthStart, previousMonthEnd);
        BigDecimal previousMonthTotal = previousMonthRoom.add(previousMonthFnb).add(previousMonthTour);
        model.addAttribute("previousMonthTotal", fmt(previousMonthTotal));
        model.addAttribute("previousMonthLabel", "Tháng " + previousMonthStart.getMonthValue() + "/" + previousMonthStart.getYear());

        BigDecimal ytdRoom = roomBookingRepository.revenueBetween(LocalDate.of(now.getYear(), 1, 1), LocalDate.of(now.getYear(), now.getMonthValue(), now.lengthOfMonth()));
        BigDecimal ytdFnb = foodOrderRepository.revenueBetween(java.time.LocalDateTime.of(now.getYear(), 1, 1, 0, 0), LocalDate.of(now.getYear(), now.getMonthValue(), now.lengthOfMonth()).atTime(23, 59, 59));
        BigDecimal ytdTour = tourBookingRepository.revenueBetween(LocalDate.of(now.getYear(), 1, 1), LocalDate.of(now.getYear(), now.getMonthValue(), now.lengthOfMonth()));
        BigDecimal totalYear = ytdRoom.add(ytdFnb).add(ytdTour);
        model.addAttribute("totalYear", fmt(totalYear));

        BigDecimal ytdRoomLastYear = roomBookingRepository.revenueBetween(LocalDate.of(now.getYear() - 1, 1, 1), LocalDate.of(now.getYear() - 1, now.getMonthValue(), now.lengthOfMonth()));
        BigDecimal ytdFnbLastYear = foodOrderRepository.revenueBetween(java.time.LocalDateTime.of(now.getYear() - 1, 1, 1, 0, 0), LocalDate.of(now.getYear() - 1, now.getMonthValue(), now.lengthOfMonth()).atTime(23, 59, 59));
        BigDecimal ytdTourLastYear = tourBookingRepository.revenueBetween(LocalDate.of(now.getYear() - 1, 1, 1), LocalDate.of(now.getYear() - 1, now.getMonthValue(), now.lengthOfMonth()));
        BigDecimal totalLastYear = ytdRoomLastYear.add(ytdFnbLastYear).add(ytdTourLastYear);
        
        LocalDate yesterdayForMtd = now.minusDays(1);
        BigDecimal currentMtdTotal = BigDecimal.ZERO;
        BigDecimal prevMtdTotal = BigDecimal.ZERO;

        if (!yesterdayForMtd.isBefore(currentMonthStart)) {
            BigDecimal cRoom = roomBookingRepository.revenueBetween(currentMonthStart, yesterdayForMtd);
            BigDecimal cFnb = foodOrderRepository.revenueBetween(currentMonthStart.atStartOfDay(), yesterdayForMtd.plusDays(1).atStartOfDay());
            BigDecimal cTour = tourBookingRepository.revenueBetween(currentMonthStart, yesterdayForMtd);
            currentMtdTotal = cRoom.add(cFnb).add(cTour);

            LocalDate previousMonthYesterday = yesterdayForMtd.minusMonths(1);
            BigDecimal pRoom = roomBookingRepository.revenueBetween(previousMonthStart, previousMonthYesterday);
            BigDecimal pFnb = foodOrderRepository.revenueBetween(previousMonthStart.atStartOfDay(), previousMonthYesterday.plusDays(1).atStartOfDay());
            BigDecimal pTour = tourBookingRepository.revenueBetween(previousMonthStart, previousMonthYesterday);
            prevMtdTotal = pRoom.add(pFnb).add(pTour);
        }

        String momStr = "-";
        if (prevMtdTotal.compareTo(BigDecimal.ZERO) == 0) {
            if (currentMtdTotal.compareTo(BigDecimal.ZERO) > 0) {
                momStr = "+100.0%";
            } else {
                momStr = "0.0%";
            }
        } else if (currentMtdTotal.compareTo(BigDecimal.ZERO) == 0) {
            momStr = "Chưa phát sinh";
        } else {
            double mom = (currentMtdTotal.doubleValue() - prevMtdTotal.doubleValue()) / prevMtdTotal.doubleValue() * 100;
            momStr = (mom >= 0 ? "+" : "") + String.format(java.util.Locale.US, "%.1f%%", mom);
        }
        model.addAttribute("growthMoM", momStr);

        List<RevenueRowDTO> monthlyRows = new ArrayList<>();
        List<String> monthlyChartLabels = new ArrayList<>();
        List<Double> monthlyChartRoom = new ArrayList<>();
        List<Double> monthlyChartFnb = new ArrayList<>();
        List<Double> monthlyChartTour = new ArrayList<>();
        BigDecimal maxMonthRev = BigDecimal.ZERO;
        String bestMonthLabel = "-";

        for (int m = 1; m <= 12; m++) {
            LocalDate start = LocalDate.of(now.getYear(), m, 1);
            LocalDate end = LocalDate.of(now.getYear(), m, start.lengthOfMonth());
            BigDecimal rMonth = roomBookingRepository.revenueBetween(start, end);
            BigDecimal fMonth = foodOrderRepository.revenueBetween(start.atStartOfDay(), end.atTime(23, 59, 59));
            BigDecimal tMonth = tourBookingRepository.revenueBetween(start, end);
            BigDecimal totalM = rMonth.add(fMonth).add(tMonth);

            if (totalM.compareTo(maxMonthRev) > 0) {
                maxMonthRev = totalM;
                bestMonthLabel = "Tháng " + m;
            }

            monthlyRows.add(new RevenueRowDTO("Tháng " + m + "/" + now.getYear(), fmt(rMonth), fmt(fMonth), fmt(tMonth), fmt(totalM)));
            monthlyChartLabels.add("T" + m);
            monthlyChartRoom.add(rMonth.doubleValue() / 1_000_000.0);
            monthlyChartFnb.add(fMonth.doubleValue() / 1_000_000.0);
            monthlyChartTour.add(tMonth.doubleValue() / 1_000_000.0);
        }

        model.addAttribute("bestMonth", bestMonthLabel);
        model.addAttribute("bestMonthVal", fmt(maxMonthRev));
        model.addAttribute("monthlyRows", monthlyRows);
        model.addAttribute("monthlyChartLabels", monthlyChartLabels);
        model.addAttribute("monthlyChartRoom", monthlyChartRoom);
        model.addAttribute("monthlyChartFnb", monthlyChartFnb);
        model.addAttribute("monthlyChartTour", monthlyChartTour);

        // --- YEARLY ---
        model.addAttribute("currentYear", String.valueOf(now.getYear()));
        model.addAttribute("previousYear", String.valueOf(now.getYear() - 1));
        model.addAttribute("startYear", String.valueOf(now.getYear() - 2));
        List<RevenueRowDTO> yearlyRows = new ArrayList<>();
        List<String> yearlyChartLabels = new ArrayList<>();
        List<Double> yearlyChartRoom = new ArrayList<>();
        List<Double> yearlyChartFnb = new ArrayList<>();
        List<Double> yearlyChartTour = new ArrayList<>();
        int currentYear = now.getYear();

        BigDecimal maxYearRev = BigDecimal.ZERO;
        String bestYearLabel = "-";
        BigDecimal revStartYtd = BigDecimal.ZERO;
        BigDecimal revPrevYtd = BigDecimal.ZERO;
        BigDecimal revCurrentYtd = BigDecimal.ZERO;

        for (int i = 2; i >= 0; i--) {
            int y = currentYear - i;
            LocalDate start = LocalDate.of(y, 1, 1);
            LocalDate end = LocalDate.of(y, 12, 31);
            BigDecimal rYear = roomBookingRepository.revenueBetween(start, end);
            BigDecimal fYear = foodOrderRepository.revenueBetween(start.atStartOfDay(), end.atTime(23, 59, 59));
            BigDecimal tYear = tourBookingRepository.revenueBetween(start, end);
            BigDecimal totalY = rYear.add(fYear).add(tYear);

            if (totalY.compareTo(maxYearRev) > 0) {
                maxYearRev = totalY;
                bestYearLabel = "Năm " + y;
            }

            int m = now.getMonthValue();
            int d = now.getDayOfMonth();
            if (m == 2 && d == 29 && !java.time.Year.isLeap(y)) {
                d = 28;
            }
            LocalDate ytdEnd = LocalDate.of(y, m, d);
            BigDecimal rYtd = roomBookingRepository.revenueBetween(start, ytdEnd);
            BigDecimal fYtd = foodOrderRepository.revenueBetween(start.atStartOfDay(), ytdEnd.atTime(23, 59, 59));
            BigDecimal tYtd = tourBookingRepository.revenueBetween(start, ytdEnd);
            BigDecimal totalYtd = rYtd.add(fYtd).add(tYtd);

            if (i == 2) revStartYtd = totalYtd;
            if (i == 1) revPrevYtd = totalYtd;
            if (i == 0) revCurrentYtd = totalYtd;

            String lbl = String.valueOf(y) + (i == 0 ? " (YTD)" : "");
            yearlyRows.add(0, new RevenueRowDTO(lbl, fmt(rYear), fmt(fYear), fmt(tYear), fmt(totalY)));
            yearlyChartLabels.add(lbl);
            yearlyChartRoom.add(rYear.doubleValue() / 1_000_000.0);
            yearlyChartFnb.add(fYear.doubleValue() / 1_000_000.0);
            yearlyChartTour.add(tYear.doubleValue() / 1_000_000.0);

            if (i == 0) {
                model.addAttribute("ytdRevenue", fmt(totalY));
            }
        }

        model.addAttribute("bestYear", bestYearLabel);
        model.addAttribute("bestYearVal", fmt(maxYearRev));

        String yoyStr = "-";
        if (revPrevYtd.compareTo(BigDecimal.ZERO) == 0) {
            if (revCurrentYtd.compareTo(BigDecimal.ZERO) > 0) {
                yoyStr = "+100.0%";
            } else if (revCurrentYtd.compareTo(BigDecimal.ZERO) == 0) {
                yoyStr = "Chưa phát sinh";
            }
        } else {
            double yoy = (revCurrentYtd.doubleValue() - revPrevYtd.doubleValue()) / revPrevYtd.doubleValue() * 100;
            yoyStr = (yoy >= 0 ? "+" : "") + String.format(java.util.Locale.US, "%.1f%%", yoy);
        }

        String cagrStr = "-";
        if (revStartYtd.compareTo(BigDecimal.ZERO) > 0) {
            double cagr = Math.pow(revCurrentYtd.doubleValue() / revStartYtd.doubleValue(), 1.0 / 2.0) - 1.0;
            cagrStr = (cagr >= 0 ? "+" : "") + String.format(java.util.Locale.US, "%.1f%%", cagr * 100);
        } else {
            cagrStr = "N/A";
        }

        model.addAttribute("growthVsLast", yoyStr);
        model.addAttribute("cagr3y", cagrStr);
        model.addAttribute("yearlyRows", yearlyRows);
        model.addAttribute("yearlyChartLabels", yearlyChartLabels);
        model.addAttribute("yearlyChartRoom", yearlyChartRoom);
        model.addAttribute("yearlyChartFnb", yearlyChartFnb);
        model.addAttribute("yearlyChartTour", yearlyChartTour);

        return "manager/revenue";
    }

    // =========================================================================
    // 5. Tỷ lệ lấp đầy phòng
    // =========================================================================

    @GetMapping("/analytics/room")
    public String analyticsRoom(Model model) {
        model.addAttribute("todayLabel", todayLabel());
        
        // --- OCCUPANCY LOGIC ---
        long total = 0;
        long occupied = 0;
        try {
            occupied = roomRepository.findOccupied().size();
            total = roomRepository.countTotalRooms();
        } catch (Exception e) {
        }

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

        java.time.LocalDate today = java.time.LocalDate.now();
        for (int i = 29; i >= 0; i--) {
            java.time.LocalDate d = today.minusDays(i);
            int occOnDay = 0;
            try {
                Integer c = roomBookingRepository.countOccupiedRoomsOnDate(d);
                if (c != null)
                    occOnDay = c;
            } catch (Exception e) {
            }

            int pct = total > 0 ? (int) Math.round((double) occOnDay * 100 / total) : 0;
            if (i == 0) {
                pct = (int) occRate;
            }

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

        List<OccupancyRowDTO> occRows = new ArrayList<>();
        try {
            List<Object[]> roomCounts = roomRepository.countByCategory();
            List<com.kawai.models.Room> occupiedList = roomRepository.findOccupied();
            for (com.kawai.models.RoomCategory cat : roomCategoryRepository.findAll()) {
                long catTotal = roomCounts.stream().filter(r -> cat.getCategoryName().equals(r[0]))
                        .mapToLong(r -> (Long) r[1]).findFirst().orElse(0);
                long catOccupied = occupiedList.stream()
                        .filter(r -> r.getCategory() != null && r.getCategory().getId().equals(cat.getId())).count();
                int occPct = catTotal > 0 ? (int) (catOccupied * 100 / catTotal) : 0;
                occRows.add(new OccupancyRowDTO(cat.getCategoryName(), (int) catTotal, occPct, (int) catOccupied));
            }
        } catch (Exception e) {
        }
        model.addAttribute("occRows", occRows);

        // --- STAY LOGIC ---
        Double avgStay = 0.0;
        try {
            avgStay = roomBookingRepository.getAverageStayDuration();
            if (avgStay == null)
                avgStay = 0.0;
        } catch (Exception e) {
        }

        model.addAttribute("avgStay", String.format(java.util.Locale.US, "%.0f", avgStay));

        long guests = 0;
        try {
            guests = bookingRepository.count();
        } catch (Exception e) {
        }

        model.addAttribute("totalGuests", guests);
        model.addAttribute("minStay", 1);
        model.addAttribute("maxStay", 12);
        model.addAttribute("medianStay", String.format(java.util.Locale.US, "%.0f", avgStay));

        List<StayRowDTO> stayRows = new ArrayList<>();
        try {
            List<Object[]> guestCaps = roomBookingRepository.getGuestCapacityByCategory();
            List<Object[]> avgStays = roomBookingRepository.getAverageStayDurationByCategory();
            List<Object[]> roomCounts = roomRepository.countByCategory();
            
            for (com.kawai.models.RoomCategory cat : roomCategoryRepository.findAll()) {
                String catName = cat.getCategoryName();
                long catTotal = roomCounts.stream().filter(r -> catName.equals(r[0]))
                        .mapToLong(r -> (Long) r[1]).findFirst().orElse(0);
                
                long catGuests = guestCaps.stream().filter(r -> catName.equals(r[0]))
                        .mapToLong(r -> r[1] != null ? ((Number) r[1]).longValue() : 0).findFirst().orElse(0);
                        
                double catAvgStay = avgStays.stream().filter(r -> catName.equals(r[0]))
                        .mapToDouble(r -> r[1] != null ? ((Number) r[1]).doubleValue() : 0.0).findFirst().orElse(0.0);
                        
                stayRows.add(new StayRowDTO(catName, String.format(java.util.Locale.US, "%.1f ngày", catAvgStay),
                        (int) catGuests, (int) catTotal));
            }
        } catch (Exception e) {
            System.err.println("[ManagerController] Error computing stay statistics: " + e.getMessage());
        }

        model.addAttribute("stayRows", stayRows);
        model.addAttribute("distLabels", java.util.List.of("1 ngày", "2 ngày", "3 ngày", "4 ngày", "5 ngày", "6+ ngày"));
        model.addAttribute("distValues", java.util.List.of(58, 72, 85, 61, 28, 16));
        
        return "manager/analytics-room";
    }

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
        } catch (Exception e) {
        }

        List<TourRowDTO> rows = new ArrayList<>();
        try {
            List<Object[]> tourData = tourBookingRepository.getTourAnalytics();
            for (Object[] r : tourData) {
                String name = r[0] != null ? r[0].toString() : "Khác";
                String cat = r[1] != null ? r[1].toString() : "Khác";
                int cnt = r[2] != null ? ((Number) r[2]).intValue() : 0;
                BigDecimal rev = r[3] != null ? (BigDecimal) r[3] : BigDecimal.ZERO;
                BigDecimal basePrice = r[4] != null ? (BigDecimal) r[4] : BigDecimal.ZERO;

                totalRevenue = totalRevenue.add(rev);
                rows.add(new TourRowDTO(name, cat, cnt, fmt(basePrice), fmt(rev), 0));
            }
            rows.sort((a, b) -> Integer.compare(b.getBookings(), a.getBookings()));
            int maxBookings = rows.stream().mapToInt(TourRowDTO::getBookings).max().orElse(0);
            for (TourRowDTO row : rows) {
                row.setBarPct(maxBookings > 0 ? (row.getBookings() * 100 / maxBookings) : 0);
            }
        } catch (Exception e) {
        }

        model.addAttribute("totalBookings", totalBookings);
        model.addAttribute("totalRevenue", fmt(totalRevenue));
        model.addAttribute("avgTicket",
                totalBookings > 0
                        ? fmt(totalRevenue.divide(BigDecimal.valueOf(totalBookings), java.math.RoundingMode.HALF_UP))
                        : "0");
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
        } catch (Exception e) {
        }

        List<FoodRowDTO> rows = new ArrayList<>();
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
                rows.add(new FoodRowDTO(name, cat, qty, fmt(price), fmt(rev), 100));
            }
            rows.sort((a, b) -> Integer.compare(b.getOrders(), a.getOrders()));
            if (rows.size() > 10)
                rows = rows.subList(0, 10);

            int maxOrders = rows.stream().mapToInt(FoodRowDTO::getOrders).max().orElse(0);
            for (FoodRowDTO row : rows) {
                row.setBarPct(maxOrders > 0 ? (row.getOrders() * 100 / maxOrders) : 0);
            }
        } catch (Exception e) {
        }

        List<String> catLabels = new ArrayList<>(catCounts.keySet());
        List<Integer> catValues = catLabels.stream().map(catCounts::get).collect(Collectors.toList());
        int totalItemsSold = catValues.stream().mapToInt(Integer::intValue).sum();

        model.addAttribute("totalOrders", String.valueOf(totalOrders));
        model.addAttribute("totalItemsSold", totalItemsSold);
        model.addAttribute("totalRevenue", fmt(totalRevenue));
        model.addAttribute("avgOrder",
                totalOrders > 0
                        ? fmt(totalRevenue.divide(BigDecimal.valueOf(totalOrders), java.math.RoundingMode.HALF_UP))
                        : "0");
        model.addAttribute("rows", rows);
        model.addAttribute("catLabels", catLabels.isEmpty() ? List.of("Tất cả") : catLabels);
        model.addAttribute("catValues", catValues.isEmpty() ? List.of(100) : catValues);
        return "manager/analytics-food";
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

    // =========================================================================
    // 3. Doanh thu theo ngày (chi tiết)
    // =========================================================================

    @GetMapping("/revenue/daily")
    public String revenueDaily(Model model) {
        model.addAttribute("todayLabel", todayLabel());
        LocalDate today = LocalDate.now();

        BigDecimal roomToday = roomBookingRepository.revenueBetween(today, today);
        BigDecimal fnbToday = foodOrderRepository.revenueBetween(today.atStartOfDay(), today.plusDays(1).atStartOfDay());
        BigDecimal tourToday = tourBookingRepository.revenueBetween(today, today);
        BigDecimal totalToday = roomToday.add(fnbToday).add(tourToday);
        model.addAttribute("totalToday", fmt(totalToday));

        // Week total (7 days)
        LocalDate weekStart = today.minusDays(6);
        BigDecimal roomWeek = roomBookingRepository.revenueBetween(weekStart, today);
        BigDecimal fnbWeek = foodOrderRepository.revenueBetween(weekStart.atStartOfDay(), today.plusDays(1).atStartOfDay());
        BigDecimal tourWeek = tourBookingRepository.revenueBetween(weekStart, today);
        BigDecimal totalWeek = roomWeek.add(fnbWeek).add(tourWeek);
        model.addAttribute("totalWeek", fmt(totalWeek));

        // Month total (current month)
        LocalDate currentMonthStart = LocalDate.of(today.getYear(), today.getMonthValue(), 1);
        BigDecimal roomMonth = roomBookingRepository.revenueBetween(currentMonthStart, today);
        BigDecimal fnbMonth = foodOrderRepository.revenueBetween(currentMonthStart.atStartOfDay(), today.plusDays(1).atStartOfDay());
        BigDecimal tourMonth = tourBookingRepository.revenueBetween(currentMonthStart, today);
        BigDecimal totalMonth = roomMonth.add(fnbMonth).add(tourMonth);
        model.addAttribute("totalMonth", fmt(totalMonth));
        model.addAttribute("currentMonthLabel", "Tháng " + today.getMonthValue() + "/" + today.getYear());

        List<RevenueRowDTO> rows = new ArrayList<>();
        List<java.util.Map<String, Object>> chartData = new ArrayList<>();
        BigDecimal peakRevenue = BigDecimal.ZERO;
        String peakRevenueDate = "-";

        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            String lbl = d.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            BigDecimal rDate = roomBookingRepository.revenueOnDate(d);
            if (rDate == null) rDate = BigDecimal.ZERO;
            BigDecimal fDate = foodOrderRepository.revenueOnDate(d.atStartOfDay(), d.plusDays(1).atStartOfDay());
            if (fDate == null) fDate = BigDecimal.ZERO;
            BigDecimal tDate = tourBookingRepository.revenueOnDate(d);
            if (tDate == null) tDate = BigDecimal.ZERO;
            BigDecimal totalDate = rDate.add(fDate).add(tDate);
            rows.add(0, new RevenueRowDTO(lbl, fmt(rDate), fmt(fDate), fmt(tDate), fmt(totalDate)));

            if (totalDate.compareTo(peakRevenue) >= 0) {
                peakRevenue = totalDate;
                peakRevenueDate = "Ngày " + d.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }

            java.util.Map<String, Object> point = new java.util.HashMap<>();
            point.put("label", d.format(DateTimeFormatter.ofPattern("dd/MM")));
            point.put("room", rDate.doubleValue() / 1_000_000.0);
            point.put("fnb", fDate.doubleValue() / 1_000_000.0);
            point.put("tour", tDate.doubleValue() / 1_000_000.0);
            chartData.add(point);
        }
        model.addAttribute("rows", rows);
        model.addAttribute("chartData", chartData);
        model.addAttribute("peakRevenue", fmt(peakRevenue));
        model.addAttribute("peakRevenueDate", peakRevenueDate);
        return "manager/revenue-daily";
    }

    // =========================================================================
    // 4. Doanh thu theo tháng (chi tiết)
    // =========================================================================

    @GetMapping("/revenue/monthly")
    public String revenueMonthly(Model model) {
        model.addAttribute("todayLabel", todayLabel());
        LocalDate now = LocalDate.now();
        model.addAttribute("nowYear", now.getYear());

        LocalDate currentMonthStart = LocalDate.of(now.getYear(), now.getMonthValue(), 1);
        BigDecimal currentMonthRoom = roomBookingRepository.revenueBetween(currentMonthStart, now);
        BigDecimal currentMonthFnb = foodOrderRepository.revenueBetween(currentMonthStart.atStartOfDay(), now.plusDays(1).atStartOfDay());
        BigDecimal currentMonthTour = tourBookingRepository.revenueBetween(currentMonthStart, now);
        BigDecimal currentMonthTotal = currentMonthRoom.add(currentMonthFnb).add(currentMonthTour);
        model.addAttribute("currentMonthTotal", fmt(currentMonthTotal));

        BigDecimal ytdRoom = roomBookingRepository.revenueBetween(LocalDate.of(now.getYear(), 1, 1), LocalDate.of(now.getYear(), now.getMonthValue(), now.lengthOfMonth()));
        BigDecimal ytdFnb = foodOrderRepository.revenueBetween(java.time.LocalDateTime.of(now.getYear(), 1, 1, 0, 0), LocalDate.of(now.getYear(), now.getMonthValue(), now.lengthOfMonth()).atTime(23, 59, 59));
        BigDecimal ytdTour = tourBookingRepository.revenueBetween(LocalDate.of(now.getYear(), 1, 1), LocalDate.of(now.getYear(), now.getMonthValue(), now.lengthOfMonth()));
        BigDecimal totalYear = ytdRoom.add(ytdFnb).add(ytdTour);
        model.addAttribute("totalYear", fmt(totalYear));

        BigDecimal ytdRoomLastYear = roomBookingRepository.revenueBetween(LocalDate.of(now.getYear() - 1, 1, 1), LocalDate.of(now.getYear() - 1, now.getMonthValue(), now.lengthOfMonth()));
        BigDecimal ytdFnbLastYear = foodOrderRepository.revenueBetween(java.time.LocalDateTime.of(now.getYear() - 1, 1, 1, 0, 0), LocalDate.of(now.getYear() - 1, now.getMonthValue(), now.lengthOfMonth()).atTime(23, 59, 59));
        BigDecimal ytdTourLastYear = tourBookingRepository.revenueBetween(LocalDate.of(now.getYear() - 1, 1, 1), LocalDate.of(now.getYear() - 1, now.getMonthValue(), now.lengthOfMonth()));
        BigDecimal totalLastYear = ytdRoomLastYear.add(ytdFnbLastYear).add(ytdTourLastYear);

        String growthYoY = "-";
        if (totalLastYear.compareTo(BigDecimal.ZERO) == 0) {
            if (totalYear.compareTo(BigDecimal.ZERO) > 0) {
                growthYoY = "+100.0%";
            } else {
                growthYoY = "0.0%";
            }
        } else {
            double yoy = (totalYear.doubleValue() - totalLastYear.doubleValue()) / totalLastYear.doubleValue() * 100;
            growthYoY = (yoy >= 0 ? "+" : "") + String.format(java.util.Locale.US, "%.1f%%", yoy);
        }
        model.addAttribute("growthYoY", growthYoY);

        List<RevenueRowDTO> rows = new ArrayList<>();
        List<String> chartLabels = new ArrayList<>();
        List<Double> chartRoom = new ArrayList<>();
        List<Double> chartFnb = new ArrayList<>();
        List<Double> chartTour = new ArrayList<>();

        for (int m = 1; m <= 12; m++) {
            LocalDate start = LocalDate.of(now.getYear(), m, 1);
            LocalDate end = LocalDate.of(now.getYear(), m, start.lengthOfMonth());
            BigDecimal rMonth = roomBookingRepository.revenueBetween(start, end);
            BigDecimal fMonth = foodOrderRepository.revenueBetween(start.atStartOfDay(), end.atTime(23, 59, 59));
            BigDecimal tMonth = tourBookingRepository.revenueBetween(start, end);
            BigDecimal totalM = rMonth.add(fMonth).add(tMonth);

            rows.add(new RevenueRowDTO("Tháng " + m + "/" + now.getYear(), fmt(rMonth), fmt(fMonth), fmt(tMonth), fmt(totalM)));
            chartLabels.add("T" + m);
            chartRoom.add(rMonth.doubleValue() / 1_000_000.0);
            chartFnb.add(fMonth.doubleValue() / 1_000_000.0);
            chartTour.add(tMonth.doubleValue() / 1_000_000.0);
        }

        model.addAttribute("rows", rows);
        model.addAttribute("chartLabels", chartLabels);
        model.addAttribute("chartRoom", chartRoom);
        model.addAttribute("chartFnb", chartFnb);
        model.addAttribute("chartTour", chartTour);

        return "manager/revenue-monthly";
    }

    @GetMapping("/revenue/yearly")
    public String revenueYearly(Model model) {
        model.addAttribute("todayLabel", todayLabel());
        LocalDate now = LocalDate.now();

        model.addAttribute("currentYear", String.valueOf(now.getYear()));
        model.addAttribute("previousYear", String.valueOf(now.getYear() - 1));
        model.addAttribute("startYear", String.valueOf(now.getYear() - 2));
        List<RevenueRowDTO> yearlyRows = new ArrayList<>();
        List<String> yearlyChartLabels = new ArrayList<>();
        List<Double> yearlyChartRoom = new ArrayList<>();
        List<Double> yearlyChartFnb = new ArrayList<>();
        List<Double> yearlyChartTour = new ArrayList<>();
        int currentYear = now.getYear();

        BigDecimal maxYearRev = BigDecimal.ZERO;
        String bestYearLabel = "-";
        BigDecimal revStartYtd = BigDecimal.ZERO;
        BigDecimal revPrevYtd = BigDecimal.ZERO;
        BigDecimal revCurrentYtd = BigDecimal.ZERO;

        for (int i = 2; i >= 0; i--) {
            int y = currentYear - i;
            LocalDate start = LocalDate.of(y, 1, 1);
            LocalDate end = LocalDate.of(y, 12, 31);
            BigDecimal rYear = roomBookingRepository.revenueBetween(start, end);
            if (rYear == null) rYear = BigDecimal.ZERO;
            BigDecimal fYear = foodOrderRepository.revenueBetween(start.atStartOfDay(), end.atTime(23, 59, 59));
            if (fYear == null) fYear = BigDecimal.ZERO;
            BigDecimal tYear = tourBookingRepository.revenueBetween(start, end);
            if (tYear == null) tYear = BigDecimal.ZERO;
            BigDecimal totalY = rYear.add(fYear).add(tYear);

            if (totalY.compareTo(maxYearRev) > 0) {
                maxYearRev = totalY;
                bestYearLabel = "Năm " + y;
            }

            int m = now.getMonthValue();
            int d = now.getDayOfMonth();
            if (m == 2 && d == 29 && !java.time.Year.isLeap(y)) {
                d = 28;
            }
            LocalDate ytdEnd = LocalDate.of(y, m, d);
            BigDecimal rYtd = roomBookingRepository.revenueBetween(start, ytdEnd);
            if (rYtd == null) rYtd = BigDecimal.ZERO;
            BigDecimal fYtd = foodOrderRepository.revenueBetween(start.atStartOfDay(), ytdEnd.atTime(23, 59, 59));
            if (fYtd == null) fYtd = BigDecimal.ZERO;
            BigDecimal tYtd = tourBookingRepository.revenueBetween(start, ytdEnd);
            if (tYtd == null) tYtd = BigDecimal.ZERO;
            BigDecimal totalYtd = rYtd.add(fYtd).add(tYtd);

            if (i == 2) revStartYtd = totalYtd;
            if (i == 1) revPrevYtd = totalYtd;
            if (i == 0) revCurrentYtd = totalYtd;

            String lbl = String.valueOf(y) + (i == 0 ? " (YTD)" : "");
            yearlyRows.add(0, new RevenueRowDTO(lbl, fmt(rYear), fmt(fYear), fmt(tYear), fmt(totalY)));
            yearlyChartLabels.add(lbl);
            yearlyChartRoom.add(rYear.doubleValue() / 1_000_000.0);
            yearlyChartFnb.add(fYear.doubleValue() / 1_000_000.0);
            yearlyChartTour.add(tYear.doubleValue() / 1_000_000.0);

            if (i == 0) {
                model.addAttribute("ytdRevenue", fmt(totalYtd));
            }
        }

        model.addAttribute("bestYear", bestYearLabel);
        model.addAttribute("bestYearVal", fmt(maxYearRev));

        String yoyStr = "-";
        if (revPrevYtd.compareTo(BigDecimal.ZERO) == 0) {
            if (revCurrentYtd.compareTo(BigDecimal.ZERO) > 0) {
                yoyStr = "+100.0%";
            } else if (revCurrentYtd.compareTo(BigDecimal.ZERO) == 0) {
                yoyStr = "Chưa phát sinh";
            }
        } else {
            double yoy = (revCurrentYtd.doubleValue() - revPrevYtd.doubleValue()) / revPrevYtd.doubleValue() * 100;
            yoyStr = (yoy >= 0 ? "+" : "") + String.format(java.util.Locale.US, "%.1f%%", yoy);
        }

        String cagrStr = "-";
        if (revStartYtd.compareTo(BigDecimal.ZERO) > 0) {
            double cagr = Math.pow(revCurrentYtd.doubleValue() / revStartYtd.doubleValue(), 1.0 / 2.0) - 1.0;
            cagrStr = (cagr >= 0 ? "+" : "") + String.format(java.util.Locale.US, "%.1f%%", cagr * 100);
        } else {
            cagrStr = "N/A";
        }

        model.addAttribute("growthVsLast", yoyStr);
        model.addAttribute("cagr3y", cagrStr);
        model.addAttribute("yearlyRows", yearlyRows);
        model.addAttribute("yearlyChartLabels", yearlyChartLabels);
        model.addAttribute("yearlyChartRoom", yearlyChartRoom);
        model.addAttribute("yearlyChartFnb", yearlyChartFnb);
        model.addAttribute("yearlyChartTour", yearlyChartTour);

        return "manager/revenue-yearly";
    }

    // =========================================================================
    // 8. Phân tích tỷ lệ lấp đầy
    // =========================================================================

    @GetMapping("/analytics/occupancy")
    public String analyticsOccupancy(Model model) {
        model.addAttribute("todayLabel", todayLabel());
        long total = 0;
        long occupied = 0;
        try {
            occupied = roomRepository.findOccupied().size();
            total = roomRepository.countTotalRooms();
        } catch (Exception e) {
        }

        long currentOccupancy = total > 0 ? Math.round((double) occupied / total * 100) : 0;
        model.addAttribute("currentOccupancy", currentOccupancy);
        model.addAttribute("totalRooms", total);
        model.addAttribute("occupiedRooms", occupied);

        List<Integer> chartOccVals = new ArrayList<>();
        List<String> chartOccLabels = new ArrayList<>();
        int peakOcc = -1;
        String peakDateLabel = "--/--";
        int sumOcc = 0;

        java.time.LocalDate today = java.time.LocalDate.now();
        for (int i = 29; i >= 0; i--) {
            java.time.LocalDate d = today.minusDays(i);
            int occOnDay = 0;
            try {
                Integer c = roomBookingRepository.countOccupiedRoomsOnDate(d);
                if (c != null) occOnDay = c;
            } catch (Exception e) {
            }
            int pct = total > 0 ? (int) Math.round((double) occOnDay * 100 / total) : 0;
            if (i == 0) pct = (int) currentOccupancy;
            chartOccVals.add(pct);
            chartOccLabels.add(d.format(java.time.format.DateTimeFormatter.ofPattern("d/MM")));
            if (pct >= peakOcc) {
                peakOcc = pct;
                peakDateLabel = d.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM"));
            }
            sumOcc += pct;
        }

        model.addAttribute("peakOccupancy", peakOcc == -1 ? 0 : peakOcc);
        model.addAttribute("peakDate", peakDateLabel);
        model.addAttribute("avgOccupancy", sumOcc / 30);
        model.addAttribute("chartOccVals", chartOccVals);
        model.addAttribute("chartOccLabels", chartOccLabels);

        return "manager/analytics-occupancy";
    }

    // Inner classes
    // =========================================================================

    @Data
    @AllArgsConstructor
    public static class DailyRevenueDTO {
        private String label;
        private double room;
        private double fnb;
        private double tour;
    }

    @Data
    @AllArgsConstructor
    public static class RevenueRowDTO {
        private String period;
        private String room;
        private String fnb;
        private String tour;
        private String total;
    }

    @Data
    @AllArgsConstructor
    public static class OccupancyRowDTO {
        private String category;
        private int totalRooms;
        private int occupancyPct;
        private int occupied;
    }

    @Data
    @AllArgsConstructor
    public static class TourRowDTO {
        private String name;
        private String category;
        private int bookings;
        private String price;
        private String revenue;
        private int barPct;
    }

    @Data
    @AllArgsConstructor
    public static class FoodRowDTO {
        private String name;
        private String category;
        private int orders;
        private String price;
        private String revenue;
        private int barPct;
    }

    @Data
    @AllArgsConstructor
    public static class StayRowDTO {
        private String category;
        private String avgStay;
        private int guests;
        private int rooms;
    }

    @Data
    @AllArgsConstructor
    public static class ExportHistoryDTO {
        private String reportName;
        private String format;
        private String exportedAt;
        private String exportedBy;
        private String fileSize;
    }

    @Data
    @AllArgsConstructor
    public static class ApprovalDTO {
        private Long taskId;
        private Long bookingId;
        private String customerName;
        private String promoCode;
        private String notes;
        private String status;
        private String createdAt;
        private String bookingDetails;
        private String totalPrice;
        private String operationalType;
    }
    @GetMapping("/schedules")
    public String showSchedules(Model model) {
        model.addAttribute("employees", employeeRepository.findAll());
        return "manager/schedules";
    }
}
