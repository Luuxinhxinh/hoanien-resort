package com.kawai.controllers.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.kawai.services.interfaces.AdminViewService;

@Controller
@RequestMapping("/admin")
@AllArgsConstructor
public class AdminController {

  private final AdminViewService adminViewService;

  // =========================================================================
  // Dashboard
  // =========================================================================

  @GetMapping("/dashboard")
  public String dashboard(Model model) {
    Map<String, Object> metrics = adminViewService.getDashboardMetrics();
    model.addAllAttributes(metrics);

    String dateLabel = LocalDate.now()
        .format(DateTimeFormatter.ofPattern("EEEE, dd 'tháng' M, yyyy", new Locale("vi")));
    model.addAttribute("todayLabel", "Overview — " + dateLabel);

    model.addAttribute("roomsByFloor", adminViewService.getRoomsByFloor());
    model.addAttribute("activities", adminViewService.getRecentActivities());

    List<AdminViewService.CheckoutMock> checkouts = adminViewService.getCheckouts();
    model.addAttribute("checkouts", checkouts);
    model.addAttribute("checkoutCount", checkouts.size());
    return "admin/dashboard";
  }

  // =========================================================================
  // Operations (Bookings, F&B, Tours)
  // =========================================================================

  @GetMapping("/bookings")
  public String bookings() {
    return "admin/bookings";
  }

  @GetMapping("/fnb-orders")
  public String fnbOrders() {
    return "admin/fnb-orders";
  }

  @GetMapping("/tour-schedules")
  public String tourSchedules() {
    return "admin/tour-schedules";
  }

  // =========================================================================
  // Master Data
  // =========================================================================

  @GetMapping("/master-data")
  public String masterData(@RequestParam(value = "tab", defaultValue = "Room Categories") String tab, Model model) {
    List<String> roomsGroup = Arrays.asList("Room Categories", "Rooms", "Pricing Management", "Bookings");
    List<String> fnbGroup = Arrays.asList("Menu Categories", "Restaurant Menu", "F&B Orders");
    List<String> tourGroup = Arrays.asList("Tour Categories", "Tours", "Tour Schedules");
    List<String> rbacGroup = Arrays.asList("Role Management");
    List<String> crmGroup = Arrays.asList("Account Management");
    List<String> promoGroup = Arrays.asList("Promotions");

    List<String> currentTabs;
    if (roomsGroup.contains(tab)) {
        currentTabs = roomsGroup;
    } else if (fnbGroup.contains(tab)) {
        currentTabs = fnbGroup;
    } else if (tourGroup.contains(tab)) {
        currentTabs = tourGroup;
    } else if (rbacGroup.contains(tab)) {
        currentTabs = rbacGroup;
    } else if (crmGroup.contains(tab)) {
        currentTabs = crmGroup;
    } else if (promoGroup.contains(tab)) {
        currentTabs = promoGroup;
    } else {
        currentTabs = roomsGroup;
        tab = "Room Categories";
    }

    model.addAttribute("tabs", currentTabs);
    model.addAttribute("activeTab", tab);

    model.addAttribute("columns", adminViewService.getMasterDataColumns(tab));
    List<Map<String, String>> rows = adminViewService.getMasterDataRows(tab);
    model.addAttribute("rows", rows);
    model.addAttribute("totalRows", rows.size());

    Map<String, List<String>> formOptions = adminViewService.getFormOptions();
    model.addAttribute("formOptions", formOptions);

    if ("Account Management".equals(tab)) {
      model.addAttribute("staffRoles", formOptions.get("staffRoles"));
      model.addAttribute("guestRoles", formOptions.get("guestRoles"));
    }
    return "admin/master-data";
  }

  @GetMapping("/audit-log")
  public String auditLog(Model model) {
    model.addAttribute("logs", adminViewService.getAuditLogs());
    model.addAttribute("employees", adminViewService.getAuditEmployees());
    model.addAttribute("modules", adminViewService.getAuditModules());
    return "admin/audit-log";
  }

  @GetMapping("/reviews")
  public String reviews(Model model) {
    List<AdminViewService.ReviewMock> reviews = adminViewService.getReviews();
    model.addAttribute("reviews", reviews);
    model.addAttribute("reviewCount", reviews.size());
    return "admin/reviews";
  }

  @GetMapping("/workflows")
  public String workflows(Model model) {
    return "admin/workflows";
  }
}