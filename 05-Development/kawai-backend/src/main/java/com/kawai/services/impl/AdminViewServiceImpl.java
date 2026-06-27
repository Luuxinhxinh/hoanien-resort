package com.kawai.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kawai.models.*;
import com.kawai.repositories.*;
import com.kawai.services.interfaces.AdminViewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminViewServiceImpl implements AdminViewService {

    private final EmployeeRepository employeeRepository;
    private final CustomerRepository customerRepository;
    private final RoleRepository roleRepository;
    private final RoomCategoryRepository roomCategoryRepository;
    private final RoomRepository roomRepository;
    private final FoodItemRepository foodItemRepository;
    private final TourRepository tourRepository;
    private final PromotionRepository promotionRepository;
    private final DailyRateRepository dailyRateRepository;
    private final BookingRepository bookingRepository;
    private final AuditLogRepository auditLogRepository;
    private final ReviewRepository reviewRepository;
    private final RoomBookingDetailRepository roomBookingDetailRepository;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Map<String, Object> getDashboardMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        try {
            long totalRooms = roomRepository.countTotalRooms();
            long occupied = roomRepository.countByStatus().stream()
                    .filter(r -> "Occupied".equals(r[0]))
                    .mapToLong(r -> (Long) r[1])
                    .findFirst().orElse(0);
            long occupancyRate = totalRooms > 0 ? Math.round((double) occupied / totalRooms * 100) : 0;
            metrics.put("totalRooms", totalRooms);
            metrics.put("occupiedRooms", occupied);
            metrics.put("currentGuests", occupied * 2);
            metrics.put("occupancyRate", occupancyRate);
            metrics.put("pendingMaintenance", 0L);
            metrics.put("urgentMaintenance", 0);
            metrics.put("normalMaintenance", 0);
        } catch (Exception e) {
            metrics.put("totalRooms", 0);
            metrics.put("occupiedRooms", 0);
            metrics.put("currentGuests", 0);
            metrics.put("occupancyRate", 0);
            metrics.put("pendingMaintenance", 0);
            metrics.put("urgentMaintenance", 0);
            metrics.put("normalMaintenance", 0);
        }
        return metrics;
    }

    @Override
    public Map<String, List<RoomMock>> getRoomsByFloor() {
        List<Room> allRooms = roomRepository.findAll();
        return allRooms.stream().map(r -> {
            String status = "vacant";
            String issue = "";
            String dbStatus = r.getRoomStatus() == null ? "Vacant_Clean" : r.getRoomStatus();

            if ("Occupied".equalsIgnoreCase(dbStatus)) {
                status = "occupied";
            } else if ("Vacant_Dirty".equalsIgnoreCase(dbStatus)) {
                status = "dirty";
            } else if ("Maintenance".equalsIgnoreCase(dbStatus)) {
                status = "broken";
                issue = "Hỏng khóa cửa";
            } else {
                status = "vacant";
            }

            // Inject some mock broken rooms for rich operation hub preview
            if ("103".equals(r.getRoomNumber())) {
                status = "broken";
                issue = "Hỏng điều hòa";
            } else if ("207".equals(r.getRoomNumber())) {
                status = "broken";
                issue = "Hỏng vòi sen";
            }

            return new RoomMock(r.getRoomNumber(), status, issue);
        }).collect(Collectors.groupingBy(r -> {
            String rn = r.getRoomNumber();
            if (rn != null && rn.length() >= 3) {
                return "Tầng " + rn.substring(0, rn.length() - 2);
            }
            return "Tầng trệt";
        }, TreeMap::new, Collectors.toList()));
    }

    @Override
    public List<ActivityMock> getRecentActivities() {
        List<ActivityMock> activities = new ArrayList<>();
        try {
            for (AuditLog log : auditLogRepository.findAll()) {
                String action = log.getAction() != null ? log.getAction() : "No action";
                String name = log.getAccount() != null && log.getAccount().getUsername() != null
                        ? log.getAccount().getUsername()
                        : "System";
                activities.add(new ActivityMock(
                        log.getTimestamp() != null
                                ? log.getTimestamp().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))
                                : "--",
                        name.length() >= 2 ? name.substring(0, 2).toUpperCase() : "SY", name, "Info", action, "Info"));
                if (activities.size() >= 10)
                    break;
            }
        } catch (Exception e) {
            /* fallback */ }

        if (activities.isEmpty()) {
            activities.add(new ActivityMock("--", "DB", "Database", "Info", "Dữ liệu DB đang được kết nối", "Info"));
        }
        return activities;
    }

    @Override
    public List<CheckoutMock> getCheckouts() {
        List<Room> allRooms = roomRepository.findAll();
        return allRooms.stream()
                .filter(r -> "Occupied".equals(r.getRoomStatus()))
                .map(r -> new CheckoutMock(r.getRoomNumber(), "Khách (DB)", true))
                .collect(Collectors.toList());
    }

    @Override
    public List<MasterDataColumn> getMasterDataColumns(String tab) {
        return switch (tab) {
            case "Room Categories" -> List.of(col("id", "Mã", "text"), col("name", "Tên hạng phòng", "text"),
                    col("rooms", "Số phòng", "text"), col("price", "Giá cơ bản / đêm", "text"),
                    col("status", "Trạng thái", "toggle"));
            case "Rooms" -> List.of(col("id", "Mã", "text"), col("name", "Phòng", "text"),
                    col("category", "Hạng", "text"), col("status", "Trạng thái", "badge"));
            case "Restaurant Menu" ->
                List.of(col("id", "Mã", "text"), col("name", "Tên món", "text"), col("category", "Danh mục", "text"),
                        col("price", "Đơn giá", "text"), col("status", "Trạng thái", "badge"));
            case "Menu Categories" -> List.of(col("id", "Mã", "text"), col("name", "Danh mục", "text"),
                    col("items", "Số món", "text"), col("status", "Trạng thái", "toggle"));
            case "Tour Categories" -> List.of(col("id", "Mã", "text"), col("name", "Danh mục tour", "text"),
                    col("tours", "Số tour", "text"), col("status", "Trạng thái", "toggle"));
            case "Tours" ->
                List.of(col("id", "Mã", "text"), col("name", "Tên tour", "text"), col("category", "Danh mục", "text"),
                        col("price", "Giá / người", "text"), col("status", "Trạng thái", "badge"));
            case "Promotions" ->
                List.of(col("id", "Mã", "text"), col("code", "Mã giảm giá", "code"), col("type", "Loại", "badge"),
                        col("value", "Giá trị", "text"), col("minOrder", "Đơn tối thiểu", "text"),
                        col("scope", "Áp dụng cho", "badge"), col("uses", "Lượt dùng", "text"),
                        col("expires", "Hết hạn", "text"), col("status", "Trạng thái", "badge"));
            case "Account Management" ->
                List.of(col("id", "Mã", "text"), col("name", "Tên", "text"), col("type", "Loại tài khoản", "badge"),
                        col("role", "Vai trò", "badge"), col("email", "Email", "text"),
                        col("status", "Kích hoạt", "toggle"));
            case "Role Management" -> List.of(col("id", "Mã", "text"), col("name", "Tên vai trò", "text"), col("permissions", "Quyền hạn", "text"));
            case "Pricing Management" -> List.of(col("id", "Mã", "text"), col("roomCategory", "Hạng phòng", "text"),
                    col("date", "Ngày", "text"), col("price", "Giá / đêm", "text"));
            case "Bookings" -> List.of(col("id", "Mã Booking", "text"), col("customer", "Khách hàng", "text"),
                    col("room", "Phòng", "text"), col("checkIn", "Ngày Check-in", "text"),
                    col("checkOut", "Ngày Check-out", "text"), col("status", "Trạng thái", "badge"));
            case "F&B Orders" -> List.of(col("id", "Mã Đơn", "text"), col("table", "Bàn/Phòng", "text"),
                    col("items", "Chi tiết món", "text"), col("total", "Tổng tiền", "text"),
                    col("status", "Trạng thái", "badge"));
            case "Tour Schedules" -> List.of(col("id", "Mã Lịch", "text"), col("tour", "Tên Tour", "text"),
                    col("date", "Ngày khởi hành", "text"), col("capacity", "Sĩ số", "text"),
                    col("status", "Trạng thái", "badge"));
            default -> Collections.emptyList();
        };
    }

    @Override
    public List<Map<String, String>> getMasterDataRows(String tab) {
        return switch (tab) {
            case "Room Categories" -> {
                List<Map<String, String>> r = new ArrayList<>();
                for (RoomCategory cat : roomCategoryRepository.findAll()) {
                    r.add(r("id", "RC-" + cat.getId(), "name", cat.getCategoryName(), "rooms",
                            String.valueOf(cat.getCapacity()), "price", formatVnd(cat.getBasePrice()), "status",
                            cat.getIsActive() != null && cat.getIsActive() ? "Hoạt động" : "Ngừng hoạt động", "__statusStyle",
                            bs(cat.getIsActive() != null && cat.getIsActive() ? "Active" : "Inactive"),
                            "description", cat.getDescription() != null ? cat.getDescription() : "",
                            "coverImgUrl", cat.getCoverImgUrl() != null ? cat.getCoverImgUrl() : "",
                            "baseAdults", String.valueOf(cat.getBaseAdults()), "baseChildren",
                            String.valueOf(cat.getBaseChildren()),
                            "maxAdults", String.valueOf(cat.getMaxAdults()), "maxChildren",
                            String.valueOf(cat.getMaxChildren()),
                            "extraAdultSurcharge",
                            cat.getExtraAdultSurcharge() != null ? cat.getExtraAdultSurcharge().toString() : "0",
                            "extraChildSurcharge",
                            cat.getExtraChildSurcharge() != null ? cat.getExtraChildSurcharge().toString() : "0"));
                }
                yield r;
            }
            case "Rooms" -> {
                List<Map<String, String>> r = new ArrayList<>();
                for (Room room : roomRepository.findAll()) {
                    String st = switch (room.getRoomStatus() == null ? "" : room.getRoomStatus()) {
                        case "Vacant_Clean" -> "Vacant";
                        case "Vacant_Dirty" -> "Dirty";
                        case "OutOfOrder" -> "Maintenance";
                        default -> room.getRoomStatus() == null ? "N/A" : room.getRoomStatus();
                    };
                    r.add(r("id", "RM-" + room.getId(), "name", room.getRoomNumber(), "category",
                            room.getCategory() != null ? room.getCategory().getCategoryName() : "N/A", "status", st,
                            "__statusStyle", bs(st)));
                }
                yield r;
            }
            case "Restaurant Menu" -> {
                List<Map<String, String>> r = new ArrayList<>();
                for (MenuItem item : foodItemRepository.findAll()) {
                    String st = item.getIsAvailable() != null && item.getIsAvailable() ? "Available" : "Unavailable";
                    r.add(r("id", "MI-" + item.getId(), "name", item.getItemName(), "category",
                            item.getCategory() != null ? item.getCategory() : "Khác", "price",
                            formatVnd(item.getPrice()), "status", st, "__statusStyle", bs(st),
                            "description", item.getDescription() != null ? item.getDescription() : "",
                            "imageUrl", item.getImageUrl() != null ? item.getImageUrl() : ""));
                }
                yield r;
            }
            case "Menu Categories" -> {
                List<Map<String, String>> r = new ArrayList<>();
                int i = 1;
                for (String cat : foodItemRepository.findDistinctCategories()) {
                    long count = foodItemRepository.findAll().stream().filter(f -> cat.equals(f.getCategory())).count();
                    r.add(r("id", "MC-" + String.format("%02d", i++), "name", cat, "items", String.valueOf(count),
                            "status", "Hoạt động", "__statusStyle", bs("Active")));
                }
                yield r;
            }
            case "Tour Categories" -> {
                List<Map<String, String>> r = new ArrayList<>();
                int i = 1;
                List<String> cats = tourRepository.findDistinctCategories();
                if (cats != null) {
                    for (String cat : cats) {
                        long count = tourRepository.findAll().stream().filter(t -> cat.equals(t.getTourType())).count();
                        r.add(r("id", "TC-" + String.format("%02d", i++), "name", cat, "tours", String.valueOf(count),
                                "status", "Hoạt động", "__statusStyle", bs("Active")));
                    }
                }
                yield r;
            }
            case "Tours" -> {
                List<Map<String, String>> r = new ArrayList<>();
                for (Tour tour : tourRepository.findAll()) {
                    String st = tour.getIsActive() != null && tour.getIsActive() ? "Active" : "Inactive";
                    r.add(r("id", "T-" + tour.getId(), "name",
                            tour.getTourName() != null ? tour.getTourName() : "Tour #" + tour.getId(), "category",
                            tour.getTourType() != null ? tour.getTourType() : "Khác", "price",
                            tour.getBasePrice() != null ? formatVnd(tour.getBasePrice()) : "-", "status", st,
                            "__statusStyle", bs(st),
                            "description", tour.getDescription() != null ? tour.getDescription() : "",
                            "duration", tour.getDuration() != null ? tour.getDuration() : "",
                            "imageUrl", tour.getImageUrl() != null ? tour.getImageUrl() : "",
                            "maxCapacity", tour.getMaxCapacity() != null ? String.valueOf(tour.getMaxCapacity()) : "0",
                            "shortQuote", tour.getShortQuote() != null ? tour.getShortQuote() : ""));
                }
                yield r;
            }
            case "Promotions" -> {
                List<Map<String, String>> r = new ArrayList<>();
                for (Promotion promo : promotionRepository.findAll()) {
                    String st = promo.getIsActive() != null && !promo.getIsActive() ? "Inactive" : "Active";
                    String expires = promo.getValidTo() != null
                            ? promo.getValidTo().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                            : "-";
                    if (promo.getValidTo() != null && promo.getValidTo().isBefore(LocalDate.now()))
                        st = "Expired";
                    r.add(r("id", "PR-" + promo.getId(), "code",
                            promo.getPromoCode() != null ? promo.getPromoCode() : "-", "type",
                            promo.getDiscountType() != null ? promo.getDiscountType() : "Phần trăm", "__typeStyle",
                            bs(promo.getDiscountType() != null ? promo.getDiscountType() : "Phần trăm"),
                            "value",
                            promo.getDiscountValue() != null
                                    ? (promo.getDiscountValue().compareTo(new java.math.BigDecimal("100")) <= 0
                                            ? promo.getDiscountValue().stripTrailingZeros().toPlainString() + "%"
                                            : formatVnd(promo.getDiscountValue()))
                                    : "-",
                            "minOrder", "Không có", "scope", "Toàn bộ", "__scopeStyle", bs("Toàn bộ"),
                            "uses",
                            (promo.getCurrentUses() != null ? promo.getCurrentUses() : 0) + "/"
                                    + (promo.getMaxUses() != null ? promo.getMaxUses() : 0),
                            "expires", expires, "status", st, "__statusStyle", bs(st),
                            "description", promo.getDescription() != null ? promo.getDescription() : "",
                            "comboConfig", promo.getComboConfig() != null ? promo.getComboConfig() : "",
                            "validFrom",
                            promo.getValidFrom() != null
                                    ? promo.getValidFrom().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))
                                    : "",
                            "validTo", promo.getValidTo() != null ? promo.getValidTo().toString() : "",
                            "maxUses", promo.getMaxUses() != null ? String.valueOf(promo.getMaxUses()) : "0",
                            "maxDiscountValueVnd",
                            promo.getMaxDiscountValueVnd() != null ? promo.getMaxDiscountValueVnd().toString() : "",
                            "maxUsesPerCustomer",
                            promo.getMaxUsesPerCustomer() != null ? promo.getMaxUsesPerCustomer().toString() : "",
                            "managerApprovalThresholdPct",
                            promo.getManagerApprovalThresholdPct() != null
                                    ? promo.getManagerApprovalThresholdPct().toString()
                                    : ""));
                }
                yield r;
            }
            case "Account Management" -> {
                List<Map<String, String>> rows = new ArrayList<>();
                try {
                    for (Employee e : employeeRepository.findAll()) {
                        String rn = e.getAccount() != null && e.getAccount().getRole() != null
                                ? e.getAccount().getRole().getRoleName()
                                : "Nhân viên";
                        boolean isActive = e.getAccount() != null && e.getAccount().getIsActive();
                        rows.add(r("id", "E-" + e.getId(),
                                "name", e.getFullName(),
                                "type", "Nhân viên",
                                "__typeStyle", bs("Nhân viên"),
                                "role", rn,
                                "__roleStyle", bs(rn),
                                "email", e.getEmail() != null ? e.getEmail() : "-",
                                "status", isActive ? "Hoạt động" : "Ngừng hoạt động",
                                "username", e.getAccount() != null ? e.getAccount().getUsername() : "",
                                "phone", e.getPhone() != null ? e.getPhone() : "",
                                "gender", e.getGender() != null ? e.getGender() : "MALE",
                                "cccd", e.getCccd() != null ? e.getCccd() : "",
                                "salary", e.getSalary() != null ? e.getSalary().toString() : "0"));
                    }
                    for (Customer c : customerRepository.findAll()) {
                        String rn = c.getAccount() != null && c.getAccount().getRole() != null
                                ? c.getAccount().getRole().getRoleName()
                                : "Khách thường";
                        boolean isActive = c.getAccount() != null && c.getAccount().getIsActive();
                        rows.add(r("id", "C-" + c.getId(),
                                "name", c.getFullName(),
                                "type", "Khách hàng",
                                "__typeStyle", bs("Khách hàng"),
                                "role", rn,
                                "__roleStyle", bs(rn),
                                "email", c.getEmail() != null ? c.getEmail() : "-",
                                "status", isActive ? "Hoạt động" : "Ngừng hoạt động",
                                "username", c.getAccount() != null ? c.getAccount().getUsername() : "",
                                "phone", c.getPhone() != null ? c.getPhone() : "",
                                "gender", c.getGender() != null ? c.getGender() : "MALE",
                                "cccd", c.getCccdPassportEncrypted() != null ? c.getCccdPassportEncrypted() : "",
                                "salary", ""));
                    }
                } catch (Exception e) {
                }
                yield rows;
            }
            case "Role Management" -> {
                List<Map<String, String>> rows = new ArrayList<>();
                try {
                    for (Role role : roleRepository.findAll()) {
                        rows.add(r("id", "RL-" + role.getId(), "name", role.getRoleName(), "permissions", role.getPermissions() != null ? role.getPermissions() : ""));
                    }
                } catch (Exception e) {
                }
                yield rows;
            }
            case "Pricing Management" -> {
                List<Map<String, String>> r = new ArrayList<>();
                for (DailyRate rate : dailyRateRepository.findAll()) {
                    String dayType = "Ngày thường";
                    String dayStyle = "badge-gray";
                    if (rate.getIsHoliday() != null && rate.getIsHoliday()) {
                        dayType = "Ngày Lễ";
                        dayStyle = "badge-red";
                    } else if (rate.getIsWeekend() != null && rate.getIsWeekend()) {
                        dayType = "Cuối tuần";
                        dayStyle = "badge-yellow";
                    }
                    r.add(r("id", "PR-" + rate.getId(), "roomCategory",
                            rate.getCategory() != null ? rate.getCategory().getCategoryName() : "N/A", "date",
                            rate.getRateDate() != null ? rate.getRateDate().toString() : "", "dayType", dayType, "__dayStyle", dayStyle, "price",
                            rate.getComputedPrice() != null ? formatVnd(rate.getComputedPrice()) : "-"));
                }
                yield r;
            }
            case "Bookings" -> {
                List<Map<String, String>> r = new ArrayList<>();
                for (Booking b : bookingRepository.findAll()) {
                    String checkIn = "-";
                    String checkOut = "-";
                    String roomStr = "N/A";
                    String displayStatus = b.getBookingStatus() != null ? b.getBookingStatus() : "Pending";
                    
                    if (b instanceof RoomBooking rb) {
                        checkIn = rb.getCheckInDate() != null ? rb.getCheckInDate().toString() : "-";
                        checkOut = rb.getCheckOutDate() != null ? rb.getCheckOutDate().toString() : "-";
                        
                        try {
                            List<RoomBookingDetail> details = roomBookingDetailRepository.findByRoomBookingId(rb.getId());
                            if (details != null && !details.isEmpty()) {
                                String rooms = details.stream()
                                    .map(d -> d.getRoom() != null ? d.getRoom().getRoomNumber() : "TBD")
                                    .collect(Collectors.joining(", "));
                                roomStr = rooms.isEmpty() ? "TBD" : rooms;
                            }
                        } catch (Exception e) {}
                        
                        if (rb.getCheckOutDate() != null && rb.getCheckOutDate().isBefore(LocalDate.now())) {
                            displayStatus = "Checked-out";
                        } else if (rb.getCheckInDate() != null && !rb.getCheckInDate().isAfter(LocalDate.now()) && rb.getCheckOutDate() != null && rb.getCheckOutDate().isAfter(LocalDate.now())) {
                            if ("Confirmed".equals(displayStatus) || "Pending".equals(displayStatus)) {
                                displayStatus = "Checked-in";
                            }
                        }
                    }
                    
                    if ("Checked_In".equals(displayStatus)) {
                        displayStatus = "Checked-in";
                    }
                    if ("Checked_Out".equals(displayStatus)) {
                        displayStatus = "Checked-out";
                    }

                    r.add(r("id", "BK-" + b.getId(), "customer",
                            b.getCustomer() != null ? b.getCustomer().getFullName() : "-", "room", roomStr, "checkIn",
                            checkIn, "checkOut", checkOut, "status",
                            displayStatus, "__statusStyle",
                            bs(displayStatus)));
                }
                yield r;
            }
            case "F&B Orders" -> {
                List<Map<String, String>> r = new ArrayList<>();
                r.add(r("id", "FB-1001", "table", "Bàn T12", "items", "2x Phở Bò, 1x Trà", "total", formatVnd(250000),
                        "status", "Pending", "__statusStyle", bs("Pending")));
                r.add(r("id", "FB-1002", "table", "Bàn T04", "items", "1x Combo BBQ", "total", formatVnd(1200000),
                        "status", "Cooking", "__statusStyle", bs("Cooking")));
                yield r;
            }
            case "Tour Schedules" -> {
                List<Map<String, String>> r = new ArrayList<>();
                r.add(r("id", "TS-2001", "tour", "Khám phá văn hóa Tây Bắc", "date", "25/06/2026", "capacity", "18/30",
                        "status", "Open", "__statusStyle", bs("Open")));
                r.add(r("id", "TS-2002", "tour", "Trekking Dã ngoại", "date", "20/06/2026", "capacity", "20/20",
                        "status", "Closed", "__statusStyle", bs("Closed")));
                yield r;
            }
            default -> Collections.emptyList();
        };
    }

    @Override
    public Map<String, List<String>> getFormOptions() {
        Map<String, List<String>> opts = new LinkedHashMap<>();
        opts.put("roomCategories", roomCategoryRepository.findAll().stream()
                .filter(c -> c.getIsActive() == null || c.getIsActive()) // Lọc chỉ lấy Hạng đang Active
                .map(RoomCategory::getCategoryName)
                .collect(Collectors.toList()));
        opts.put("roomStatuses", List.of("Vacant", "Occupied", "Dirty", "Maintenance"));
        try {
            opts.put("menuCategories", foodItemRepository.findDistinctCategories());
        } catch (Exception e) {
            opts.put("menuCategories", List.of());
        }
        try {
            opts.put("tourCategories",
                    tourRepository.findDistinctCategories() != null ? tourRepository.findDistinctCategories()
                            : List.of());
        } catch (Exception e) {
            opts.put("tourCategories", List.of());
        }
        opts.put("promoScopes", List.of("Toàn bộ", "Phòng", "F&B", "Tours", "Khách VIP"));
        opts.put("seasonTypes", List.of("Thường ngày", "Cuối tuần", "Cao điểm"));
        opts.put("staffRoles",
                roleRepository.findAll().stream()
                        .filter(r -> !r.getRoleName().startsWith("Khách") && !"Admin".equalsIgnoreCase(r.getRoleName()))
                        .map(Role::getRoleName).collect(Collectors.toList()));
        opts.put("guestRoles", roleRepository.findAll().stream().filter(r -> r.getRoleName().startsWith("Khách"))
                .map(Role::getRoleName).collect(Collectors.toList()));
        return opts;
    }

    @Override
    public List<AuditLogMock> getAuditLogs() {
        List<AuditLogMock> logs = new ArrayList<>();
        try {
            for (AuditLog l : auditLogRepository.findAll()) {
                String action = l.getAction() != null ? l.getAction() : "No action";
                String ip = l.getIpAddress() != null ? l.getIpAddress() : "127.0.0.1";
                String name = l.getAccount() != null && l.getAccount().getUsername() != null
                        ? l.getAccount().getUsername()
                        : "System";
                String date = l.getTimestamp() != null
                        ? l.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        : "";
                String role = l.getAccount() != null && l.getAccount().getRole() != null && l.getAccount().getRole().getRoleName() != null
                        ? l.getAccount().getRole().getRoleName()
                        : "System";
                logs.add(new AuditLogMock(
                        l.getTimestamp() != null ? l.getTimestamp().format(DateTimeFormatter.ofPattern("HH:mm")) : "--",
                        name, name.length() >= 2 ? name.substring(0, 2).toUpperCase() : "SY",
                        role, action, List.of(l.getTableName() != null ? l.getTableName() : "System"), ip, "normal",
                        l.getRecordId() != null ? l.getRecordId() : 0L,
                        l.getTableName() != null ? l.getTableName() : "", date));
            }
        } catch (Exception e) {
            /* empty */ }
        if (logs.isEmpty()) {
            logs.add(new AuditLogMock("--", "System", "SY", "System", "Hệ thống đang chạy", List.of("System"),
                    "127.0.0.1", "normal", 0L, "", ""));
        }
        return logs;
    }

    @Override
    public Set<String> getAuditEmployees() {
        Set<String> employees = new java.util.LinkedHashSet<>();
        try {
            for (AuditLog l : auditLogRepository.findAll()) {
                if (l.getAccount() != null && l.getAccount().getUsername() != null) {
                    employees.add(l.getAccount().getUsername());
                }
            }
        } catch (Exception e) {
            /* empty */ }
        if (employees.isEmpty()) {
            employees.add("System");
        }
        return employees;
    }

    @Override
    public Set<String> getAuditModules() {
        Set<String> modules = new java.util.LinkedHashSet<>();
        try {
            for (AuditLog l : auditLogRepository.findAll()) {
                if (l.getTableName() != null && !l.getTableName().isEmpty()) {
                    modules.add(l.getTableName());
                }
            }
        } catch (Exception e) {
            /* empty */ }
        if (modules.isEmpty()) {
            modules.add("System");
        }
        return modules;
    }

    @Override
    public List<ReviewMock> getReviews() {
        List<ReviewMock> reviews = new ArrayList<>();
        try {
            for (Review r : reviewRepository.findAll()) {
                String guestName = r.getCustomer() != null ? r.getCustomer().getFullName() : "Ẩn danh";
                String initials = guestName.length() >= 2 ? guestName.substring(0, 2).toUpperCase() : "AN";
                reviews.add(new ReviewMock("RV-" + r.getId(), guestName, initials, "Dịch vụ", "Room",
                        r.getRatingService() != null ? r.getRatingService() : 5,
                        r.getReviewText() != null ? r.getReviewText() : "",
                        r.getCreatedAt() != null ? r.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                                : "--",
                        "Approved".equals(r.getModerationStatus()), null, null, null));
            }
        } catch (Exception e) {
            /* empty */ }
        if (reviews.isEmpty()) {
            reviews.add(new ReviewMock("RV-001", "System", "SY", "Sample", "Room", 5, "Chưa có đánh giá.", "--", true,
                    null, null, null));
        }
        return reviews;
    }

    private static String formatVnd(Number amount) {
        if (amount == null)
            return "-";
        long val = amount.longValue();
        if (val >= 1_000_000)
            return String.format("%,.0f", (val / 1_000_000.0)).replace(",", ".") + "M VNĐ";
        return String.format("%,d", val).replace(",", ".") + " VNĐ";
    }

    private static String bs(String val) {
        return switch (val == null ? "" : val) {
            case "Active", "Available", "Nhân viên" -> "badge-green";
            case "Khách", "Khách hàng", "Receptionist", "Tours", "Số tiền" -> "badge-blue";
            case "Occupied", "Vacant", "Phòng", "Thường ngày", "Cuối tuần" -> "badge-brown";
            case "Manager", "Khách VIP", "Unavailable", "Cao điểm" -> "badge-yellow";
            case "Maintenance", "Expired" -> "badge-dark";
            default -> "badge-gray";
        };
    }

    private static Map<String, String> r(String... kv) {
        Map<String, String> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2)
            m.put(kv[i], kv[i + 1]);
        try {
            m.put("jsonString", objectMapper.writeValueAsString(m));
        } catch (Exception e) {
            m.put("jsonString", "{}");
        }
        return m;
    }

    private static MasterDataColumn col(String k, String l, String t) {
        return new MasterDataColumn(k, l, t);
    }
}
