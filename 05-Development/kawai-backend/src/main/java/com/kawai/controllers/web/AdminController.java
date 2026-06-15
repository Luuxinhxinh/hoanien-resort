package com.kawai.controllers.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import lombok.Data;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import com.kawai.repositories.*;
import com.kawai.models.*;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@RequestMapping("/admin")
@AllArgsConstructor
public class AdminController {

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
  private static final ObjectMapper objectMapper = new ObjectMapper();

  // =========================================================================
  // Dashboard
  // =========================================================================

  @GetMapping("/dashboard")
  public String dashboard(Model model) {
    try {
      long totalRooms = roomRepository.countTotalRooms();
      long occupied = roomRepository.countByStatus().stream()
          .filter(r -> "Occupied".equals(r[0]))
          .mapToLong(r -> (Long) r[1])
          .findFirst().orElse(0);
      long occupancyRate = totalRooms > 0 ? Math.round((double) occupied / totalRooms * 100) : 0;
      model.addAttribute("totalRooms", totalRooms);
      model.addAttribute("occupiedRooms", occupied);
      model.addAttribute("currentGuests", occupied * 2);
      model.addAttribute("occupancyRate", occupancyRate);
      model.addAttribute("pendingMaintenance", 0L);
      model.addAttribute("urgentMaintenance", 0);
      model.addAttribute("normalMaintenance", 0);
    } catch (Exception e) {
      model.addAttribute("totalRooms", 0);
      model.addAttribute("occupiedRooms", 0);
      model.addAttribute("currentGuests", 0);
      model.addAttribute("occupancyRate", 0);
      model.addAttribute("pendingMaintenance", 0);
      model.addAttribute("urgentMaintenance", 0);
      model.addAttribute("normalMaintenance", 0);
    }
    String dateLabel = LocalDate.now()
        .format(DateTimeFormatter.ofPattern("EEEE, dd 'tháng' M, yyyy", new Locale("vi")));
    model.addAttribute("todayLabel", "Overview — " + dateLabel);

    List<Room> allRooms = roomRepository.findAll();
    List<RoomMock> rooms = allRooms.stream().map(r -> {
      String status = switch (r.getRoomStatus()) {
        case "Vacant_Clean", "Vacant_Dirty" -> "vacant";
        case "Occupied" -> "occupied";
        case "Maintenance" -> "maintenance";
        default -> "dirty";
      };
      return new RoomMock(r.getRoomNumber(), status);
    }).collect(Collectors.toList());
    model.addAttribute("rooms", rooms);

    List<ActivityMock> activities = new ArrayList<>();
    try {
      for (AuditLog log : auditLogRepository.findAll()) {
        String action = log.getAction() != null ? log.getAction() : "No action";
        String name = log.getAccount() != null && log.getAccount().getUsername() != null
            ? log.getAccount().getUsername()
            : "System";
        activities.add(new ActivityMock(
            log.getTimestamp() != null ? log.getTimestamp().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))
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
    model.addAttribute("activities", activities);

    List<CheckoutMock> checkouts = allRooms.stream()
        .filter(r -> "Occupied".equals(r.getRoomStatus()))
        .map(r -> new CheckoutMock(r.getRoomNumber(), "Khách (DB)", true))
        .collect(Collectors.toList());
    model.addAttribute("checkouts", checkouts);
    model.addAttribute("checkoutCount", checkouts.size());
    return "admin/dashboard";
  }

  // =========================================================================
  // Master Data
  // =========================================================================

  private static final List<String> MD_TABS = Arrays.asList(
      "Room Categories", "Rooms", "Restaurant Menu", "Menu Categories",
      "Tour Categories", "Tours", "Account Management", "Promotions", "Pricing Management");

  @GetMapping("/master-data")
  public String masterData(@RequestParam(value = "tab", defaultValue = "Room Categories") String tab, Model model) {
    model.addAttribute("tabs", MD_TABS);
    model.addAttribute("activeTab", tab);
    model.addAttribute("columns", buildColumns(tab));
    model.addAttribute("rows", buildRows(tab));
    model.addAttribute("totalRows", buildRows(tab).size());
    model.addAttribute("formOptions", buildFormOptions());
    if ("Account Management".equals(tab)) {
      List<Role> allRoles = roleRepository.findAll();
      model.addAttribute("staffRoles", allRoles.stream()
          .filter(r -> !r.getRoleName().startsWith("Khách") && !"Admin".equalsIgnoreCase(r.getRoleName())).toList());
      model.addAttribute("guestRoles", allRoles.stream().filter(r -> r.getRoleName().startsWith("Khách")).toList());
    }
    return "admin/master-data";
  }

  private List<MasterDataColumn> buildColumns(String tab) {
    return switch (tab) {
      case "Room Categories" ->
        List.of(col("id", "Mã", "text"), col("name", "Tên hạng phòng", "text"), col("rooms", "Số phòng", "text"),
            col("price", "Giá cơ bản / đêm", "text"), col("status", "Trạng thái", "badge"));
      case "Rooms" -> List.of(col("id", "Mã", "text"), col("name", "Phòng", "text"), col("category", "Hạng", "text"),
          col("status", "Trạng thái", "badge"));
      case "Restaurant Menu" -> List.of(col("id", "Mã", "text"), col("name", "Tên món", "text"),
          col("category", "Danh mục", "text"), col("price", "Đơn giá", "text"), col("status", "Trạng thái", "badge"));
      case "Menu Categories" -> List.of(col("id", "Mã", "text"), col("name", "Danh mục", "text"),
          col("items", "Số món", "text"), col("status", "Trạng thái", "badge"));
      case "Tour Categories" -> List.of(col("id", "Mã", "text"), col("name", "Danh mục tour", "text"),
          col("tours", "Số tour", "text"), col("status", "Trạng thái", "badge"));
      case "Tours" ->
        List.of(col("id", "Mã", "text"), col("name", "Tên tour", "text"), col("category", "Danh mục", "text"),
            col("price", "Giá / người", "text"), col("status", "Trạng thái", "badge"));
      case "Promotions" -> List.of(col("id", "Mã", "text"), col("code", "Mã giảm giá", "code"),
          col("type", "Loại", "badge"), col("value", "Giá trị", "text"), col("minOrder", "Đơn tối thiểu", "text"),
          col("scope", "Áp dụng cho", "badge"), col("uses", "Lượt dùng", "text"), col("expires", "Hết hạn", "text"),
          col("status", "Trạng thái", "badge"));
      case "Account Management" -> List.of(col("id", "Mã", "text"), col("name", "Tên", "text"),
          col("type", "Loại tài khoản", "badge"), col("role", "Vai trò", "badge"), col("email", "Email", "text"),
          col("lastLogin", "Đăng nhập cuối", "text"), col("status", "Kích hoạt", "toggle"));
      case "Pricing Management" -> List.of(col("id", "Mã", "text"), col("roomCategory", "Hạng phòng", "text"),
          col("date", "Ngày", "text"), col("price", "Giá / đêm", "text"));
      default -> Collections.emptyList();
    };
  }

  private List<Map<String, String>> buildRows(String tab) {
    return switch (tab) {
      case "Room Categories" -> {
        List<Map<String, String>> r = new ArrayList<>();
        for (RoomCategory cat : roomCategoryRepository.findAll()) {
          r.add(r("id", "RC-" + cat.getId(), "name", cat.getCategoryName(), "rooms", "0", "price",
              formatVnd(cat.getBasePrice()), "status", "Active", "__statusStyle", bs("Active")));
        }
        yield r;
      }
      case "Rooms" -> {
        List<Map<String, String>> r = new ArrayList<>();
        for (Room room : roomRepository.findAll()) {
          String st = switch (room.getRoomStatus()) {
            case "Vacant_Clean" -> "Vacant";
            case "Vacant_Dirty" -> "Dirty";
            default -> room.getRoomStatus();
          };
          r.add(r("id", "R-" + room.getRoomNumber(), "name", "Phòng " + room.getRoomNumber(), "category",
              room.getCategory() != null ? room.getCategory().getCategoryName() : "N/A", "status", st, "__statusStyle",
              bs(st)));
        }
        yield r;
      }
      case "Restaurant Menu" -> {
        List<Map<String, String>> r = new ArrayList<>();
        for (MenuItem item : foodItemRepository.findAll()) {
          String st = item.getIsAvailable() != null && item.getIsAvailable() ? "Available" : "Unavailable";
          r.add(r("id", "M-" + item.getId(), "name", item.getItemName(), "category",
              item.getCategory() != null ? item.getCategory() : "Khác", "price", formatVnd(item.getPrice()), "status",
              st, "__statusStyle", bs(st)));
        }
        yield r;
      }
      case "Menu Categories" -> {
        List<Map<String, String>> r = new ArrayList<>();
        int i = 1;
        for (String cat : foodItemRepository.findDistinctCategories())
          r.add(r("id", "MC-" + String.format("%02d", i++), "name", cat, "items", "-", "status", "Active",
              "__statusStyle", bs("Active")));
        yield r;
      }
      case "Tour Categories" -> {
        List<Map<String, String>> r = new ArrayList<>();
        int i = 1;
        List<String> cats = tourRepository.findDistinctCategories();
        if (cats != null)
          for (String cat : cats)
            r.add(r("id", "TC-" + String.format("%02d", i++), "name", cat, "tours", "-", "status", "Active",
                "__statusStyle", bs("Active")));
        yield r;
      }
      case "Tours" -> {
        List<Map<String, String>> r = new ArrayList<>();
        for (Tour tour : tourRepository.findAll())
          r.add(r("id", "T-" + tour.getId(), "name",
              tour.getTourName() != null ? tour.getTourName() : "Tour #" + tour.getId(), "category",
              tour.getTourType() != null ? tour.getTourType() : "Khác", "price",
              tour.getBasePrice() != null ? formatVnd(tour.getBasePrice()) : "-", "status", "Active", "__statusStyle",
              bs("Active")));
        yield r;
      }
      case "Promotions" -> {
        List<Map<String, String>> r = new ArrayList<>();
        for (Promotion promo : promotionRepository.findAll()) {
          String st = "Active";
          String expires = promo.getValidTo() != null
              ? promo.getValidTo().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
              : "-";
          if (promo.getValidTo() != null && promo.getValidTo().isBefore(LocalDate.now()))
            st = "Expired";
          r.add(r("id", "PRO-" + promo.getId(), "code", promo.getPromoCode() != null ? promo.getPromoCode() : "-",
              "type", promo.getDiscountType() != null ? promo.getDiscountType() : "Phần trăm", "__typeStyle",
              bs(promo.getDiscountType() != null ? promo.getDiscountType() : "Phần trăm"),
              "value", promo.getDiscountValue() != null ? formatVnd(promo.getDiscountValue()) : "-",
              "minOrder", "Không có", "scope", "Toàn bộ", "__scopeStyle", bs("Toàn bộ"),
              "uses",
              (promo.getCurrentUses() != null ? promo.getCurrentUses() : 0) + "/"
                  + (promo.getMaxUses() != null ? promo.getMaxUses() : 0),
              "expires", expires, "status", st, "__statusStyle", bs(st)));
        }
        yield r;
      }
      case "Account Management" -> {
        List<Map<String, String>> rows = new ArrayList<>();
        try {
          for (Employee e : employeeRepository.findAll()) {
            String rn = e.getAccount().getRole().getRoleName();
            rows.add(r("id", "E-" + e.getId(), "name", e.getFullName(), "type", "Nhân viên", "__typeStyle",
                bs("Nhân viên"), "role", rn, "__roleStyle", bs(rn), "email", e.getEmail(), "lastLogin", "-", "status",
                e.getAccount().getIsActive() ? "true" : "false", "username", e.getAccount().getUsername(), "phone",
                e.getPhone() != null ? e.getPhone() : ""));
          }
          for (Customer c : customerRepository.findAll()) {
            String rn = c.getAccount() != null && c.getAccount().getRole() != null
                ? c.getAccount().getRole().getRoleName()
                : "Khách thường";
            rows.add(r("id", "C-" + c.getId(), "name", c.getFullName(), "type", "Khách hàng", "__typeStyle",
                bs("Khách hàng"), "role", rn, "__roleStyle", bs(rn), "email", c.getEmail() != null ? c.getEmail() : "-",
                "lastLogin", "-", "status", c.getAccount() != null && c.getAccount().getIsActive() ? "true" : "false"));
          }
        } catch (Exception e) {
          /* empty */}
        yield rows;
      }
      case "Pricing Management" -> {
        List<Map<String, String>> r = new ArrayList<>();
        for (DailyRate rate : dailyRateRepository.findAll()) {
          r.add(r("id", "PR-" + rate.getId(), "roomCategory",
              rate.getCategory() != null ? rate.getCategory().getCategoryName() : "N/A", "date",
              rate.getRateDate() != null ? rate.getRateDate().toString() : "", "price",
              rate.getComputedPrice() != null ? formatVnd(rate.getComputedPrice()) : "-"));
        }
        yield r;
      }
      default -> Collections.emptyList();
    };
  }

  @GetMapping("/audit-log")
  public String auditLog(Model model) {
    List<AuditLogMock> logs = new ArrayList<>();
    try {
      for (AuditLog l : auditLogRepository.findAll()) {
        String action = l.getAction() != null ? l.getAction() : "No action";
        String ip = l.getIpAddress() != null ? l.getIpAddress() : "127.0.0.1";
        String name = l.getAccount() != null && l.getAccount().getUsername() != null ? l.getAccount().getUsername()
            : "System";
        logs.add(new AuditLogMock(
            l.getTimestamp() != null ? l.getTimestamp().format(DateTimeFormatter.ofPattern("HH:mm")) : "--",
            name, name.length() >= 2 ? name.substring(0, 2).toUpperCase() : "SY",
            "System", action, List.of(l.getTableName() != null ? l.getTableName() : "System"), ip, "normal"));
      }
    } catch (Exception e) {
      /* empty */ }
    if (logs.isEmpty()) {
      logs.add(new AuditLogMock("--", "System", "SY", "System", "Hệ thống đang chạy", List.of("System"), "127.0.0.1",
          "normal"));
    }
    model.addAttribute("logs", logs);
    return "admin/audit-log";
  }

  @GetMapping("/reviews")
  public String reviews(Model model) {
    List<ReviewMock> reviews = new ArrayList<>();
    try {
      for (Review r : reviewRepository.findAll()) {
        String guestName = r.getCustomer() != null ? r.getCustomer().getFullName() : "Ẩn danh";
        String initials = guestName.length() >= 2 ? guestName.substring(0, 2).toUpperCase() : "AN";
        reviews.add(new ReviewMock("RV-" + r.getId(), guestName, initials, "Dịch vụ", "Room",
            r.getRatingService() != null ? r.getRatingService() : 5,
            r.getReviewText() != null ? r.getReviewText() : "",
            r.getCreatedAt() != null ? r.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "--",
            "Approved".equals(r.getModerationStatus()), null, null, null));
      }
    } catch (Exception e) {
      /* empty */ }
    if (reviews.isEmpty()) {
      reviews.add(new ReviewMock("RV-001", "System", "SY", "Sample", "Room", 5, "Chưa có đánh giá.", "--", true, null,
          null, null));
    }
    model.addAttribute("reviews", reviews);
    model.addAttribute("reviewCount", reviews.size());
    return "admin/reviews";
  }

  private Map<String, List<String>> buildFormOptions() {
    Map<String, List<String>> opts = new LinkedHashMap<>();
    opts.put("roomCategories",
        roomCategoryRepository.findAll().stream().map(RoomCategory::getCategoryName).collect(Collectors.toList()));
    opts.put("roomStatuses", List.of("Vacant", "Occupied", "Dirty", "Maintenance"));
    try {
      opts.put("menuCategories", foodItemRepository.findDistinctCategories());
    } catch (Exception e) {
      opts.put("menuCategories", List.of());
    }
    try {
      opts.put("tourCategories",
          tourRepository.findDistinctCategories() != null ? tourRepository.findDistinctCategories() : List.of());
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

  @Data
  @AllArgsConstructor
  public static class MasterDataColumn {
    private String key;
    private String label;
    private String renderType;
  }

  @Data
  @AllArgsConstructor
  public static class ReviewMock {
    private String id;
    private String guest;
    private String initials;
    private String service;
    private String serviceType;
    private int rating;
    private String text;
    private String date;
    private boolean visible;
    private String hiddenBy;
    private String hiddenDate;
    private String hiddenReason;

    public String getServiceTypeBg() {
      return switch (serviceType) {
        case "Room" -> "rgba(61,74,46,0.15)";
        case "Dish" -> "rgba(122,78,26,0.15)";
        case "Tour" -> "rgba(45,107,122,0.15)";
        default -> "rgba(0,0,0,0.07)";
      };
    }

    public String getServiceTypeColor() {
      return switch (serviceType) {
        case "Room" -> "#3D4A2E";
        case "Dish" -> "#7A4E1A";
        case "Tour" -> "#1D5B6A";
        default -> "#2C2A1E";
      };
    }

    public String getServiceTypeLabel() {
      return switch (serviceType) {
        case "Room" -> "Phòng";
        case "Dish" -> "Món ăn";
        case "Tour" -> "Tour";
        default -> serviceType;
      };
    }
  }

  @Data
  @AllArgsConstructor
  public static class RoomMock {
    private String roomNumber;
    private String status;

    public String getBgColor() {
      return switch (status) {
        case "vacant" -> "#2E3D35";
        case "dirty" -> "#4A3A1A";
        case "maintenance" -> "#2E2E35";
        default -> "#3D4A2E";
      };
    }

    public String getDotColor() {
      return switch (status) {
        case "vacant" -> "#5A8C6B";
        case "dirty" -> "#C9A96E";
        case "maintenance" -> "#7A7A9A";
        default -> "#6B8C42";
      };
    }
  }

  @Data
  @AllArgsConstructor
  public static class ActivityMock {
    private String time;
    private String initials;
    private String name;
    private String role;
    private String action;
    private String badgeLabel;

    public String getAvatarBg() {
      return switch (role) {
        case "Receptionist" -> "#2D6B7A";
        case "F&B" -> "#7A4E1A";
        case "Housekeeping" -> "#5A3A6B";
        case "Manager" -> "#8B7020";
        default -> "#3A3526";
      };
    }

    public String getRoleBadgeBg() {
      return switch (role) {
        case "Receptionist" -> "rgba(45,107,122,0.15)";
        case "F&B" -> "rgba(122,78,26,0.15)";
        case "Housekeeping" -> "rgba(90,58,107,0.15)";
        case "Manager" -> "rgba(139,112,32,0.20)";
        default -> "rgba(44,42,30,0.15)";
      };
    }

    public String getRoleBadgeColor() {
      return switch (role) {
        case "Receptionist" -> "#1D5B6A";
        case "F&B" -> "#7A4E1A";
        case "Housekeeping" -> "#5A3A6B";
        case "Manager" -> "#7A6010";
        default -> "#2C2A1E";
      };
    }
  }

  @Data
  @AllArgsConstructor
  public static class CheckoutMock {
    private String roomNumber;
    private String guestName;
    private boolean paid;
  }

  @Data
  @AllArgsConstructor
  public static class AuditLogMock {
    private String time;
    private String name;
    private String initials;
    private String role;
    private String action;
    private List<String> modules;
    private String ip;
    private String severity;
  }
}