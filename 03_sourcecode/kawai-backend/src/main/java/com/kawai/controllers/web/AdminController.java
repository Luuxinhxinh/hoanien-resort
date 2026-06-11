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

import com.kawai.repositories.EmployeeRepository;
import com.kawai.repositories.CustomerRepository;
import com.kawai.models.Employee;
import com.kawai.models.Customer;
import com.kawai.models.Role;
import com.kawai.repositories.RoleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@RequestMapping("/admin")
@AllArgsConstructor
public class AdminController {

  private final EmployeeRepository employeeRepository;
  private final CustomerRepository customerRepository;
  private final RoleRepository roleRepository;
  private static final ObjectMapper objectMapper = new ObjectMapper();

  // =========================================================================
  // Dashboard
  // =========================================================================

  @GetMapping("/dashboard")
  public String dashboard(Model model) {

    // ── KPI ───────────────────────────────────────────────────────────────
    int totalRooms = 100;
    int occupiedRooms = 83;
    int currentGuests = 150;
    int pendingMaintenance = 3;
    int urgentMaintenance = 2;
    int normalMaintenance = 1;
    int occupancyRate = (int) Math.round((double) occupiedRooms / totalRooms * 100);

    model.addAttribute("totalRooms", totalRooms);
    model.addAttribute("occupiedRooms", occupiedRooms);
    model.addAttribute("currentGuests", currentGuests);
    model.addAttribute("pendingMaintenance", pendingMaintenance);
    model.addAttribute("urgentMaintenance", urgentMaintenance);
    model.addAttribute("normalMaintenance", normalMaintenance);
    model.addAttribute("occupancyRate", occupancyRate);

    // ── Today's date label ────────────────────────────────────────────────
    String dateLabel = LocalDate.now()
        .format(DateTimeFormatter.ofPattern("EEEE, dd 'tháng' M, yyyy", new Locale("vi")));
    model.addAttribute("todayLabel", "Overview — " + dateLabel);

    // ── Room Matrix ───────────────────────────────────────────────────────
    List<String> dirtyRooms = List.of("205", "207", "210", "605");
    List<String> maintenanceRooms = List.of("302", "308");
    List<String> vacantRooms = List.of("609", "610", "709", "710",
        "809", "810", "909", "910",
        "1008", "1009", "1010");

    List<RoomMock> rooms = new ArrayList<>();
    for (int floor = 1; floor <= 10; floor++) {
      for (int room = 1; room <= 10; room++) {
        String roomId = String.valueOf(floor * 100 + room);
        String status;
        if (dirtyRooms.contains(roomId))
          status = "dirty";
        else if (maintenanceRooms.contains(roomId))
          status = "maintenance";
        else if (vacantRooms.contains(roomId))
          status = "vacant";
        else
          status = "occupied";
        rooms.add(new RoomMock(roomId, status));
      }
    }
    model.addAttribute("rooms", rooms);

    // ── Recent Activity ───────────────────────────────────────────────────
    List<ActivityMock> activities = Arrays.asList(
        new ActivityMock("08:42", "LN", "Linh Nguyễn", "Receptionist",
            "Check-in khách #1204 — Deluxe River View 302", "Booking"),
        new ActivityMock("09:15", "MT", "Minh Trần", "F&B",
            "Đóng bàn #7 — Hoá đơn #3041 (1.240.000 VNĐ)", "F&B"),
        new ActivityMock("09:33", "HP", "Hà Phạm", "Housekeeping",
            "Đánh dấu Phòng 204 đã dọn xong", "Phòng"),
        new ActivityMock("10:01", "AD", "Admin Dũng", "Admin",
            "Cập nhật giá phòng hạng Deluxe River View", "Tài chính"),
        new ActivityMock("10:17", "TL", "Tuấn Lê", "Manager",
            "Duyệt yêu cầu bảo trì #R-049", "Phòng"));
    model.addAttribute("activities", activities);

    // ── Checkouts Tonight ─────────────────────────────────────────────────
    List<CheckoutMock> checkouts = Arrays.asList(
        new CheckoutMock("102", "Mr. Haruto Yamamoto", true),
        new CheckoutMock("205", "Ms. Sarah Chen", false),
        new CheckoutMock("301", "Mr. & Mrs. Dubois", true),
        new CheckoutMock("404", "Mr. Nguyen Van An", false));
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
  public String masterData(@RequestParam(value = "tab", defaultValue = "Room Categories") String tab,
      Model model) {

    List<MasterDataColumn> columns = buildColumns(tab);
    List<Map<String, String>> rows = buildRows(tab);

    model.addAttribute("tabs", MD_TABS);
    model.addAttribute("activeTab", tab);
    model.addAttribute("columns", columns);
    model.addAttribute("rows", rows);
    model.addAttribute("totalRows", rows.size());
    // Form dropdown options — injected via Thymeleaf JS inline →
    // window.KAWAI_FORM_OPTIONS
    model.addAttribute("formOptions", buildFormOptions());

    if ("Account Management".equals(tab)) {
      List<Role> allRoles = roleRepository.findAll();
      List<Role> staffRoles = allRoles.stream()
          .filter(r -> !r.getRoleName().startsWith("Khách") && !r.getRoleName().equalsIgnoreCase("Admin"))
          .toList();
      List<Role> guestRoles = allRoles.stream()
          .filter(r -> r.getRoleName().startsWith("Khách"))
          .toList();
      model.addAttribute("staffRoles", staffRoles);
      model.addAttribute("guestRoles", guestRoles);
    }

    return "admin/master-data";
  }

  // ── Column definitions per tab ────────────────────────────────────────────

  private List<MasterDataColumn> buildColumns(String tab) {
    return switch (tab) {
      case "Room Categories" -> Arrays.asList(
          col("id", "Mã", "text"), col("name", "Tên hạng phòng", "text"),
          col("rooms", "Số phòng", "text"), col("price", "Giá cơ bản / đêm", "text"),
          col("status", "Trạng thái", "badge"));
      case "Rooms" -> Arrays.asList(
          col("id", "Mã", "text"), col("name", "Phòng", "text"),
          col("category", "Hạng", "text"), col("status", "Trạng thái", "badge"));
      case "Restaurant Menu" -> Arrays.asList(
          col("id", "Mã", "text"), col("name", "Tên món", "text"),
          col("category", "Danh mục", "text"), col("price", "Đơn giá", "text"),
          col("status", "Trạng thái", "badge"));
      case "Menu Categories" -> Arrays.asList(
          col("id", "Mã", "text"), col("name", "Danh mục", "text"),
          col("items", "Số món", "text"), col("status", "Trạng thái", "badge"));
      case "Tour Categories" -> Arrays.asList(
          col("id", "Mã", "text"), col("name", "Danh mục tour", "text"),
          col("tours", "Số tour", "text"), col("status", "Trạng thái", "badge"));
      case "Tours" -> Arrays.asList(
          col("id", "Mã", "text"), col("name", "Tên tour", "text"),
          col("category", "Danh mục", "text"), col("duration", "Thời gian", "text"),
          col("price", "Giá / người", "text"), col("status", "Trạng thái", "badge"));
      case "Promotions" -> Arrays.asList(
          col("id", "Mã", "text"), col("code", "Mã giảm giá", "code"),
          col("type", "Loại", "badge"), col("value", "Giá trị", "text"),
          col("minOrder", "Đơn tối thiểu", "text"), col("scope", "Áp dụng cho", "badge"),
          col("uses", "Lượt dùng", "text"), col("expires", "Hết hạn", "text"),
          col("status", "Trạng thái", "badge"));
      case "Account Management" -> Arrays.asList(
          col("id", "Mã", "text"), col("name", "Tên", "text"),
          col("type", "Loại tài khoản", "badge"), col("role", "Vai trò", "badge"),
          col("email", "Email", "text"), col("lastLogin", "Đăng nhập cuối", "text"),
          col("status", "Kích hoạt", "toggle"));
      case "Pricing Management" -> Arrays.asList(
          col("id", "Mã", "text"), col("roomCategory", "Hạng phòng", "text"),
          col("seasonType", "Loại mùa", "badge"),
          col("startDate", "Ngày bắt đầu", "text"),
          col("endDate", "Ngày kết thúc", "text"),
          col("price", "Giá / đêm", "text"), col("status", "Trạng thái", "badge"));
      default -> Collections.emptyList();
    };
  }

  // ── Row data per tab ──────────────────────────────────────────────────────

  private List<Map<String, String>> buildRows(String tab) {
    return switch (tab) {

      case "Room Categories" -> Arrays.asList(
          r("id", "RC-01", "name", "Deluxe River View", "rooms", "8", "price", "2.800.000 VNĐ",
              "status", "Active", "__statusStyle", bs("Active")),
          r("id", "RC-02", "name", "Garden Bungalow", "rooms", "6", "price", "2.200.000 VNĐ",
              "status", "Active", "__statusStyle", bs("Active")),
          r("id", "RC-03", "name", "Forest Suite", "rooms", "4", "price", "4.500.000 VNĐ",
              "status", "Active", "__statusStyle", bs("Active")),
          r("id", "RC-04", "name", "Standard Room", "rooms", "6", "price", "1.600.000 VNĐ",
              "status", "Inactive", "__statusStyle", bs("Inactive")));

      case "Rooms" -> Arrays.asList(
          r("id", "R-101", "name", "Phòng 101", "category", "Standard Room",
              "status", "Occupied", "__statusStyle", bs("Occupied")),
          r("id", "R-102", "name", "Phòng 102", "category", "Standard Room",
              "status", "Vacant", "__statusStyle", bs("Vacant")),
          r("id", "R-201", "name", "Phòng 201", "category", "Deluxe River View",
              "status", "Occupied", "__statusStyle", bs("Occupied")),
          r("id", "R-202", "name", "Phòng 202", "category", "Deluxe River View",
              "status", "Dirty", "__statusStyle", bs("Dirty")),
          r("id", "R-301", "name", "Phòng 301", "category", "Forest Suite",
              "status", "Occupied", "__statusStyle", bs("Occupied")),
          r("id", "R-302", "name", "Phòng 302", "category", "Garden Bungalow",
              "status", "Maintenance", "__statusStyle", bs("Maintenance")));

      case "Restaurant Menu" -> Arrays.asList(
          r("id", "M-001", "name", "Tom Yum Soup", "category", "Súp",
              "price", "185.000 VNĐ", "status", "Available", "__statusStyle", bs("Available")),
          r("id", "M-002", "name", "Cá sông nướng", "category", "Món chính",
              "price", "320.000 VNĐ", "status", "Available", "__statusStyle", bs("Available")),
          r("id", "M-003", "name", "Xôi xoài", "category", "Tráng miệng",
              "price", "95.000 VNĐ", "status", "Available", "__statusStyle", bs("Available")),
          r("id", "M-004", "name", "Set thử món theo mùa", "category", "Set Menu",
              "price", "680.000 VNĐ", "status", "Unavailable", "__statusStyle", bs("Unavailable")));

      case "Menu Categories" -> Arrays.asList(
          r("id", "MC-01", "name", "Súp", "items", "4", "status", "Active", "__statusStyle", bs("Active")),
          r("id", "MC-02", "name", "Món chính", "items", "12", "status", "Active", "__statusStyle", bs("Active")),
          r("id", "MC-03", "name", "Tráng miệng", "items", "6", "status", "Active", "__statusStyle", bs("Active")),
          r("id", "MC-04", "name", "Đồ uống", "items", "18", "status", "Active", "__statusStyle", bs("Active")),
          r("id", "MC-05", "name", "Set Menu", "items", "3", "status", "Active", "__statusStyle", bs("Active")));

      case "Tour Categories" -> Arrays.asList(
          r("id", "TC-01", "name", "Du thuyền sông", "tours", "3", "status", "Active", "__statusStyle", bs("Active")),
          r("id", "TC-02", "name", "Trekking rừng", "tours", "2", "status", "Active", "__statusStyle", bs("Active")),
          r("id", "TC-03", "name", "Tham quan bản làng", "tours", "4", "status", "Active", "__statusStyle",
              bs("Active")),
          r("id", "TC-04", "name", "Chèo thuyền kayak", "tours", "2", "status", "Inactive", "__statusStyle",
              bs("Inactive")));

      case "Tours" -> Arrays.asList(
          r("id", "T-001", "name", "Du thuyền hoàng hôn", "category", "Du thuyền sông",
              "duration", "2 tiếng", "price", "450.000 VNĐ", "status", "Active", "__statusStyle", bs("Active")),
          r("id", "T-002", "name", "Đi bộ rừng sáng sớm", "category", "Trekking rừng",
              "duration", "3 tiếng", "price", "320.000 VNĐ", "status", "Active", "__statusStyle", bs("Active")),
          r("id", "T-003", "name", "Thăm bản Hmông", "category", "Tham quan bản làng",
              "duration", "5 tiếng", "price", "580.000 VNĐ", "status", "Active", "__statusStyle", bs("Active")),
          r("id", "T-004", "name", "Kayak đến thác nước", "category", "Chèo thuyền kayak",
              "duration", "4 tiếng", "price", "520.000 VNĐ", "status", "Inactive", "__statusStyle", bs("Inactive")));

      case "Promotions" -> Arrays.asList(
          r("id", "PRO-001", "code", "SUMMER2026", "type", "Phần trăm", "__typeStyle", bs("Phần trăm"),
              "value", "15%", "minOrder", "500.000 VNĐ", "scope", "Toàn bộ", "__scopeStyle", bs("Toàn bộ"),
              "uses", "42 / 100", "expires", "30/06/2026", "status", "Active", "__statusStyle", bs("Active")),
          r("id", "PRO-002", "code", "KAWAI100K", "type", "Số tiền", "__typeStyle", bs("Số tiền"),
              "value", "100.000 VNĐ", "minOrder", "800.000 VNĐ", "scope", "Phòng", "__scopeStyle", bs("Phòng"),
              "uses", "18 / 50", "expires", "15/07/2026", "status", "Active", "__statusStyle", bs("Active")),
          r("id", "PRO-003", "code", "VIPONLY20", "type", "Phần trăm", "__typeStyle", bs("Phần trăm"),
              "value", "20%", "minOrder", "1.000.000 VNĐ", "scope", "Khách VIP", "__scopeStyle", bs("Khách VIP"),
              "uses", "7 / 30", "expires", "31/07/2026", "status", "Active", "__statusStyle", bs("Active")),
          r("id", "PRO-004", "code", "FBDEAL50K", "type", "Số tiền", "__typeStyle", bs("Số tiền"),
              "value", "50.000 VNĐ", "minOrder", "300.000 VNĐ", "scope", "F&B", "__scopeStyle", bs("F&B"),
              "uses", "65 / 80", "expires", "20/06/2026", "status", "Active", "__statusStyle", bs("Active")),
          r("id", "PRO-005", "code", "TOURPACK10", "type", "Phần trăm", "__typeStyle", bs("Phần trăm"),
              "value", "10%", "minOrder", "Không có", "scope", "Tours", "__scopeStyle", bs("Tours"),
              "uses", "11 / 40", "expires", "31/08/2026", "status", "Active", "__statusStyle", bs("Active")),
          r("id", "PRO-006", "code", "NEWYEAR2026", "type", "Phần trăm", "__typeStyle", bs("Phần trăm"),
              "value", "25%", "minOrder", "2.000.000 VNĐ", "scope", "Phòng", "__scopeStyle", bs("Phòng"),
              "uses", "100 / 100", "expires", "10/01/2026", "status", "Expired", "__statusStyle", bs("Expired")),
          r("id", "PRO-007", "code", "TESTCODE", "type", "Số tiền", "__typeStyle", bs("Số tiền"),
              "value", "200.000 VNĐ", "minOrder", "Không có", "scope", "Toàn bộ", "__scopeStyle", bs("Toàn bộ"),
              "uses", "0 / 10", "expires", "01/06/2026", "status", "Inactive", "__statusStyle", bs("Inactive")));

      case "Account Management" -> {
        List<Map<String, String>> accountRows = new ArrayList<>();
        List<Employee> employees = employeeRepository.findAll();
        for (Employee emp : employees) {
          String roleName = emp.getAccount().getRole().getRoleName();
          String status = emp.getAccount().getIsActive() ? "true" : "false";
          accountRows.add(r(
              "id", "E-" + String.format("%03d", emp.getId()),
              "name", emp.getFullName(),
              "type", "Nhân viên", "__typeStyle", bs("Nhân viên"),
              "role", roleName, "__roleStyle", bs(roleName),
              "email", emp.getEmail(),
              "lastLogin", "-",
              "status", status,
              "username", emp.getAccount().getUsername(),
              "phone", emp.getPhone() != null ? emp.getPhone() : "",
              "cccd", emp.getCccd() != null ? emp.getCccd() : "",
              "gender", emp.getGender() != null ? emp.getGender() : "MALE",
              "salary", emp.getSalary() != null ? emp.getSalary().toString() : ""));
        }
        // Load real guest accounts
        List<Customer> customers = customerRepository.findAll();
        for (Customer cus : customers) {
          String roleName = cus.getAccount() != null && cus.getAccount().getRole() != null
              ? cus.getAccount().getRole().getRoleName()
              : "Khách thường";
          String status = cus.getAccount() != null && cus.getAccount().getIsActive() ? "true" : "false";
          String email = cus.getEmail() != null ? cus.getEmail() : "-";
          String username = cus.getAccount() != null ? cus.getAccount().getUsername() : "";
          accountRows.add(r(
              "id", "C-" + String.format("%03d", cus.getId()),
              "name", cus.getFullName(),
              "type", "Khách hàng", "__typeStyle", bs("Khách hàng"),
              "role", roleName, "__roleStyle", bs(roleName),
              "email", email,
              "lastLogin", "-",
              "status", status,
              "username", username,
              "phone", cus.getPhone() != null ? cus.getPhone() : "",
              "gender", cus.getGender() != null ? cus.getGender() : "MALE"));
        }
        // // Mock some guest accounts just for display if db is empty
        // if (customers.isEmpty()) {
        // accountRows.add(r("id", "C-001", "name", "Haruto Yamamoto", "type", "Khách
        // hàng", "__typeStyle", bs("Khách hàng"),
        // "role", "Khách VIP", "__roleStyle", bs("Khách VIP"),
        // "email", "haruto@email.com", "lastLogin", "05/06/2026", "status", "true"));
        // accountRows.add(r("id", "C-002", "name", "Sarah Chen", "type", "Khách hàng",
        // "__typeStyle", bs("Khách hàng"),
        // "role", "Khách thường", "__roleStyle", bs("Khách thường"),
        // "email", "sarah@email.com", "lastLogin", "04/06/2026", "status", "true"));
        // }
        yield accountRows;
      }

      case "Pricing Management" -> Arrays.asList(
          r("id", "PR-001", "roomCategory", "Deluxe River View", "seasonType", "Thường ngày",
              "__seasonTypeStyle", bs("Thường ngày"), "startDate", "2026-06-01", "endDate", "2026-12-31",
              "price", "2.800.000 VNĐ", "status", "Active", "__statusStyle", bs("Active")),
          r("id", "PR-002", "roomCategory", "Deluxe River View", "seasonType", "Cuối tuần",
              "__seasonTypeStyle", bs("Cuối tuần"), "startDate", "2026-06-01", "endDate", "2026-12-31",
              "price", "3.200.000 VNĐ", "status", "Active", "__statusStyle", bs("Active")),
          r("id", "PR-003", "roomCategory", "Deluxe River View", "seasonType", "Cao điểm",
              "__seasonTypeStyle", bs("Cao điểm"), "startDate", "2026-07-01", "endDate", "2026-08-31",
              "price", "4.500.000 VNĐ", "status", "Active", "__statusStyle", bs("Active")),
          r("id", "PR-004", "roomCategory", "Garden Bungalow", "seasonType", "Thường ngày",
              "__seasonTypeStyle", bs("Thường ngày"), "startDate", "2026-06-01", "endDate", "2026-12-31",
              "price", "2.200.000 VNĐ", "status", "Active", "__statusStyle", bs("Active")),
          r("id", "PR-005", "roomCategory", "Garden Bungalow", "seasonType", "Cuối tuần",
              "__seasonTypeStyle", bs("Cuối tuần"), "startDate", "2026-06-01", "endDate", "2026-12-31",
              "price", "2.600.000 VNĐ", "status", "Active", "__statusStyle", bs("Active")),
          r("id", "PR-006", "roomCategory", "Forest Suite", "seasonType", "Thường ngày",
              "__seasonTypeStyle", bs("Thường ngày"), "startDate", "2026-06-01", "endDate", "2026-12-31",
              "price", "4.500.000 VNĐ", "status", "Active", "__statusStyle", bs("Active")),
          r("id", "PR-007", "roomCategory", "Forest Suite", "seasonType", "Cao điểm",
              "__seasonTypeStyle", bs("Cao điểm"), "startDate", "2026-12-20", "endDate", "2027-01-05",
              "price", "6.800.000 VNĐ", "status", "Active", "__statusStyle", bs("Active")),
          r("id", "PR-008", "roomCategory", "Standard Room", "seasonType", "Thường ngày",
              "__seasonTypeStyle", bs("Thường ngày"), "startDate", "2026-06-01", "endDate", "2026-12-31",
              "price", "1.600.000 VNĐ", "status", "Inactive", "__statusStyle", bs("Inactive")));

      default -> Collections.emptyList();
    };
  }

  // =========================================================================
  // Audit Log
  // =========================================================================

  @GetMapping("/audit-log")
  public String auditLog(Model model) {
    List<AuditLogMock> logs = Arrays.asList(
        new AuditLogMock("06:45", "Admin Dũng", "AD", "Admin", "Đăng nhập vào Operations Hub",
            Arrays.asList("Tài khoản"), "192.168.1.10", "normal"),
        new AuditLogMock("07:30", "Hà Phạm", "HP", "Housekeeping", "Đánh dấu Phòng 204 đã dọn xong",
            Arrays.asList("Phòng"), "192.168.1.24", "normal"),
        new AuditLogMock("07:55", "Minh Trần", "MT", "F&B", "Mở bàn #7 phục vụ", Arrays.asList("F&B"),
            "192.168.1.31", "normal"),
        new AuditLogMock("08:31", "Linh Nguyễn", "LN", "Receptionist",
            "Check-in khách #1204 — Deluxe River View Phòng 302", Arrays.asList("Đặt phòng"),
            "192.168.1.12", "normal"),
        new AuditLogMock("09:15", "Minh Trần", "MT", "F&B", "Đóng bàn #7 — Hoá đơn #3041 (1.240.000 VNĐ)",
            Arrays.asList("F&B", "Tài chính"), "192.168.1.31", "sensitive"),
        new AuditLogMock("10:01", "Admin Dũng", "AD", "Admin",
            "Cập nhật giá đêm Deluxe River View: 2.500.000 VNĐ → 2.800.000 VNĐ",
            Arrays.asList("Tài chính", "Phòng"), "192.168.1.10", "critical"),
        new AuditLogMock("10:17", "Tuấn Lê", "TL", "Manager",
            "Duyệt yêu cầu bảo trì #R-049 — Phòng 305 điều hoà", Arrays.asList("Phòng"),
            "192.168.1.05", "normal"),
        new AuditLogMock("10:42", "Linh Nguyễn", "LN", "Receptionist",
            "Sửa hoá đơn #2047 Phòng 204 — Điều chỉnh phí trả phòng muộn",
            Arrays.asList("Tài chính", "Đặt phòng"), "192.168.1.12", "sensitive"),
        new AuditLogMock("11:08", "Admin Dũng", "AD", "Admin",
            "Thu hồi quyền module Tài chính của tài khoản E-006 Mai Hoàng",
            Arrays.asList("Tài khoản"), "192.168.1.10", "critical"),
        new AuditLogMock("11:30", "Hà Phạm", "HP", "Housekeeping",
            "Nộp báo cáo dọn phòng Tầng 3 (6 phòng)", Arrays.asList("Phòng"),
            "192.168.1.24", "normal"),
        new AuditLogMock("12:15", "Minh Trần", "MT", "F&B",
            "Thêm món mới — Súp cua theo mùa (280.000 VNĐ)", Arrays.asList("F&B"),
            "192.168.1.31", "sensitive"),
        new AuditLogMock("13:00", "Linh Nguyễn", "LN", "Receptionist",
            "Xử lý trả phòng sớm Phòng 205 — Ms. Sarah Chen",
            Arrays.asList("Đặt phòng"), "192.168.1.12", "sensitive"));
    model.addAttribute("logs", logs);
    return "admin/audit-log";
  }

  // =========================================================================
  // Reviews
  // =========================================================================

  @GetMapping("/reviews")
  public String reviews(Model model) {
    List<ReviewMock> reviews = Arrays.asList(
        new ReviewMock("RV-001", "Haruto Y.", "HY", "Deluxe River View Room", "Room", 5,
            "An absolutely magical stay. The river view at dawn was worth every penny. Staff were attentive without being intrusive — rare in hospitality. The bamboo bathroom was a beautiful touch.",
            "05/06/2026", true, null, null, null),
        new ReviewMock("RV-002", "Sarah C.", "SC", "Tom Yum Soup", "Dish", 4,
            "Beautifully presented and genuinely spicy — the galangal and lemongrass were very fresh. Slightly too salty for my preference but I'd order it again.",
            "04/06/2026", true, null, null, null),
        new ReviewMock("RV-003", "Pierre D.", "PD", "Du thuyền hoàng hôn", "Tour", 5,
            "The guide was incredibly knowledgeable about local flora. We spotted a kingfisher and two river otters. The bamboo raft added to the authenticity. Highly recommend the evening slot.",
            "03/06/2026", true, null, null, null),
        new ReviewMock("RV-004", "Ẩn danh", "AN", "Garden Bungalow", "Room", 2,
            "Phòng có mùi ẩm và điều hoà ồn. Tôi đã ở tại những khu resort tương tự với chi phí thấp hơn và có trải nghiệm tốt hơn. Khá thất vọng với mức giá này.",
            "02/06/2026", false, "Admin Dũng", "03/06/2026",
            "Đang xem xét — bộ phận bảo trì đang xử lý phản ánh về HVAC"),
        new ReviewMock("RV-005", "Nguyễn V.", "NV", "Đi bộ rừng sáng sớm", "Tour", 5,
            "Tuyệt vời! Hướng dẫn viên rất nhiệt tình và am hiểu về hệ sinh thái địa phương. Chúng tôi đã thấy nhiều loài chim quý hiếm. Nhất định sẽ quay lại.",
            "01/06/2026", true, null, null, null),
        new ReviewMock("RV-006", "Emma W.", "EW", "Cá sông nướng", "Dish", 3,
            "The fish was fresh but the preparation was quite plain. A little more of the herb crust would have elevated this. Service was slow during peak dinner hours.",
            "31/05/2026", true, null, null, null),
        new ReviewMock("RV-007", "Ẩn danh", "AN", "Forest Suite", "Room", 1,
            "Book this place at an amazing price! Click here to get 80% off! This is totally not spam at all. Buy now!",
            "30/05/2026", false, "Admin Dũng", "30/05/2026",
            "Spam — nội dung quảng cáo, không phải đánh giá khách thật"),
        new ReviewMock("RV-008", "Keiko M.", "KM", "Thăm bản Hmông", "Tour", 5,
            "A profoundly moving cultural experience. The weaving demonstration by the village elder was extraordinary. Our guide translated with great sensitivity. Please preserve this program.",
            "29/05/2026", true, null, null, null));
    model.addAttribute("reviews", reviews);
    model.addAttribute("reviewCount", reviews.size());
    return "admin/reviews";
  }

  // ── Form dropdown options (injected into master-data.html via Thymeleaf JS
  // inline) ──
  // Sau này có thể thay bằng truy vấn DB: roomCategoryRepo.findAll() v.v.

  private static Map<String, List<String>> buildFormOptions() {
    Map<String, List<String>> opts = new LinkedHashMap<>();

    // Danh mục hạng phòng — Room Categories tab + Rooms tab + Pricing tab
    opts.put("roomCategories", Arrays.asList(
        "Deluxe River View", "Garden Bungalow", "Forest Suite", "Standard Room"));

    // Trạng thái phòng — Rooms tab
    opts.put("roomStatuses", Arrays.asList(
        "Vacant", "Occupied", "Dirty", "Maintenance"));

    // Danh mục món ăn — Restaurant Menu tab
    opts.put("menuCategories", Arrays.asList(
        "Súp", "Món chính", "Tráng miệng", "Đồ uống", "Set Menu"));

    // Danh mục tour — Tours tab
    opts.put("tourCategories", Arrays.asList(
        "Du thuyền sông", "Trekking rừng", "Tham quan bản làng", "Chèo thuyền kayak"));

    // Phạm vi áp dụng khuyến mãi — Promotions tab
    opts.put("promoScopes", Arrays.asList(
        "Toàn bộ", "Phòng", "F&B", "Tours", "Khách VIP"));

    // Loại mùa giá — Pricing Management tab
    opts.put("seasonTypes", Arrays.asList(
        "Thường ngày", "Cuối tuần", "Cao điểm"));

    // Vai trò nhân viên — Account Management tab
    opts.put("staffRoles", Arrays.asList(
        "Receptionist", "F&B", "Housekeeping", "Manager", "Tourguide"));

    // Vai trò khách — Account Management tab
    opts.put("guestRoles", Arrays.asList(
        "Khách VIP", "Khách thường"));

    return opts;
  }

  // =========================================================================
  // Shared static helpers
  // =========================================================================

  /** Pre-compute inline badge style from value string. */
  private static String bs(String val) {
    return badgeStyle(val);
  }

  private static String badgeStyle(String val) {
    return switch (val) {

      // GREEN
      case "Active", "Available", "Nhân viên" ->
        "badge-green";

      // BLUE
      case "Khách", "Khách hàng", "Receptionist", "Tours", "Số tiền" ->
        "badge-blue";

      // BROWN
      case "Occupied", "Vacant", "Phòng", "Thường ngày", "Cuối tuần" ->
        "badge-brown";

      // YELLOW (VIP / warning / important)
      case "Manager", "Khách VIP", "Unavailable", "Cao điểm" ->
        "badge-yellow";

      // DARK (maintenance / expired)
      case "Maintenance", "Expired" ->
        "badge-dark";

      // GRAY (default / neutral / inactive)
      case "Inactive", "Khách thường", "Phần trăm", "Toàn bộ" ->
        "badge-gray";

      default ->
        "badge-gray";
    };
  }

  /** Build a LinkedHashMap row from alternating key/value pairs. */
  private static Map<String, String> r(String... kv) {
    Map<String, String> map = new LinkedHashMap<>();
    for (int i = 0; i < kv.length; i += 2)
      map.put(kv[i], kv[i + 1]);

    try {
      map.put("jsonString", objectMapper.writeValueAsString(map));
    } catch (Exception e) {
      map.put("jsonString", "{}");
    }
    return map;
  }

  /** Shorthand column constructor. */
  private static MasterDataColumn col(String key, String label, String renderType) {
    return new MasterDataColumn(key, label, renderType);
  }

  // =========================================================================
  // Inner mock classes — TẠM THỜI để test giao diện.
  // Sẽ thay bằng Entity / DTO / Service thật khi kết nối database.
  // =========================================================================

  /** Column definition for Master Data generic table. */
  @Data
  @AllArgsConstructor
  public static class MasterDataColumn {
    private String key;
    private String label;
    /** text | badge | code | toggle */
    private String renderType;
  }

  /** Review entry for Review Management page. */
  @Data
  @AllArgsConstructor
  public static class ReviewMock {
    private String id;
    private String guest;
    private String initials;
    private String service;
    private String serviceType; // Room | Dish | Tour
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

  /** Dashboard room matrix tile. */
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

  /** Dashboard recent activity entry. */
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

  /** Dashboard checkout-tonight entry. */
  @Data
  @AllArgsConstructor
  public static class CheckoutMock {
    private String roomNumber;
    private String guestName;
    private boolean paid;
  }

  /** Audit log entry. */
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
