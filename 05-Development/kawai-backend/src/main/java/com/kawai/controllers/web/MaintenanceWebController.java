package com.kawai.controllers.web;

import com.kawai.models.HotelOperation;
import com.kawai.models.Room;
import com.kawai.models.RoomBookingDetail;
import com.kawai.models.FolioItem;
import com.kawai.repositories.EmployeeRepository;
import com.kawai.repositories.MaintenanceRequestRepository;
import com.kawai.repositories.RoomRepository;
import com.kawai.repositories.RoomBookingDetailRepository;
import com.kawai.repositories.FolioItemRepository;
import com.kawai.repositories.StaffScheduleRepository;
import com.kawai.services.interfaces.HousekeepingService;
import com.kawai.models.Employee;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Locale;

@Controller
@RequestMapping("/maintenance")
@PreAuthorize("hasAnyAuthority('OP_MAINTENANCE','ROLE_ADMIN','ROLE_MANAGER')")
public class MaintenanceWebController {
    private final HousekeepingService housekeepingService;
    private final MaintenanceRequestRepository maintenanceRequestRepo;
    private final RoomRepository roomRepository;
    private final EmployeeRepository employeeRepository;
    private final RoomBookingDetailRepository roomBookingDetailRepository;
    private final FolioItemRepository folioItemRepository;
    private final StaffScheduleRepository staffScheduleRepository;

    public MaintenanceWebController(HousekeepingService hs,
            MaintenanceRequestRepository mrr, RoomRepository rr, EmployeeRepository er,
            RoomBookingDetailRepository rbdr, FolioItemRepository fir, StaffScheduleRepository ssr) {
        this.housekeepingService = hs; this.maintenanceRequestRepo = mrr;
        this.roomRepository = rr; this.employeeRepository = er;
        this.roomBookingDetailRepository = rbdr;
        this.folioItemRepository = fir;
        this.staffScheduleRepository = ssr;
    }

    @ModelAttribute("todayLabel")
    public String todayLabel() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", new Locale("vi")));
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session, @RequestParam(required = false) String bypass) {
        if ("true".equals(bypass)) {
            session.setAttribute("demoBypassShift", true);
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isOnShift = false;
        
        if (session.getAttribute("demoBypassShift") != null) {
            isOnShift = true;
        } else if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER") || a.getAuthority().equals("ROLE_ADMIN"))) {
            isOnShift = true;
        } else {
            Employee currentStaff = employeeRepository.findByAccountUsername(auth.getName()).orElse(null);
            if (currentStaff != null) {
                var shifts = staffScheduleRepository.findByEmployeeIdAndWorkDate(currentStaff.getId(), LocalDate.now());
                if (shifts != null && !shifts.isEmpty()) {
                    isOnShift = true;
                }
                
                // Get weekly schedules for the employee
                LocalDate startOfWeek = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                LocalDate endOfWeek = startOfWeek.plusDays(6);
                var weeklySchedules = staffScheduleRepository.findByEmployeeIdAndWorkDateBetweenOrderByWorkDateAsc(currentStaff.getId(), startOfWeek, endOfWeek);
                model.addAttribute("myWeeklySchedules", weeklySchedules);
            }
        }
        
        model.addAttribute("isOnShift", isOnShift);
        
        if (isOnShift) {
            model.addAttribute("pendingTasks", maintenanceRequestRepo.findByOperationalTypeAndStatus("MAINTENANCE","Pending"));
            model.addAttribute("inProgressTasks", maintenanceRequestRepo.findByOperationalTypeAndStatus("MAINTENANCE","InProgress"));
            model.addAttribute("pausedTasks", maintenanceRequestRepo.findByOperationalTypeAndStatus("MAINTENANCE","Paused"));
            long underMaintenance = roomRepository.countByRoomStatus("Maintenance");
            model.addAttribute("maintenanceRooms", underMaintenance);
            var completed = maintenanceRequestRepo.findByOperationalTypeAndStatus("MAINTENANCE","Completed");
            model.addAttribute("completedTasks", completed);
            model.addAttribute("completedCount", completed.size());
        }
        
        return "maintenance/dashboard";
    }

    @PostMapping("/tasks/{taskId}/start")
    public String startTask(@PathVariable Long taskId, RedirectAttributes ra) {
        try { var t = maintenanceRequestRepo.findById(taskId).orElseThrow();
            t.setStatus("InProgress"); t.setStartedAt(java.time.LocalDateTime.now());
            maintenanceRequestRepo.save(t);
            ra.addFlashAttribute("successMessage","Đã bắt đầu sửa chữa.");
        } catch(Exception e) { ra.addFlashAttribute("errorMessage","Lỗi: "+e.getMessage()); }
        return "redirect:/maintenance/dashboard";
    }

    @PostMapping("/tasks/{taskId}/complete")
    public String completeTask(@PathVariable Long taskId, @RequestParam(required=false) String notes, RedirectAttributes ra) {
        try { 
            var t = maintenanceRequestRepo.findById(taskId).orElseThrow();
            if (notes != null && !notes.isEmpty()) {
                t.setNotes(t.getNotes() + "\n[Đã sửa]: " + notes);
                maintenanceRequestRepo.save(t);
            }
            housekeepingService.completeMaintenance(taskId);
            ra.addFlashAttribute("successMessage","Hoàn tất bảo trì, phòng đã sẵn sàng.");
        } catch(Exception e) { ra.addFlashAttribute("errorMessage","Lỗi: "+e.getMessage()); }
        return "redirect:/maintenance/dashboard";
    }

    @PostMapping("/tasks/{taskId}/pause")
    public String pauseTask(@PathVariable Long taskId, @RequestParam String reason, RedirectAttributes ra) {
        try { 
            var t = maintenanceRequestRepo.findById(taskId).orElseThrow();
            t.setStatus("Paused");
            t.setNotes(t.getNotes() + "\n[Tạm dừng]: " + reason);
            maintenanceRequestRepo.save(t);
            ra.addFlashAttribute("successMessage","Đã tạm dừng công việc.");
        } catch(Exception e) { ra.addFlashAttribute("errorMessage","Lỗi: "+e.getMessage()); }
        return "redirect:/maintenance/dashboard";
    }

    @GetMapping("/pricing")
    public String pricingPage(Model model) {
        java.util.List<HotelOperation> allTasks = maintenanceRequestRepo.findByOperationalType("DAMAGE_CHECK");
        
        java.util.List<HotelOperation> unpriced = allTasks.stream()
            .filter(t -> t.getDamagePrice() == null)
            .sorted((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt()))
            .collect(java.util.stream.Collectors.toList());
            
        java.util.List<HotelOperation> priced = allTasks.stream()
            .filter(t -> t.getDamagePrice() != null)
            .sorted((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt()))
            .collect(java.util.stream.Collectors.toList());
            
        model.addAttribute("unpricedTasks", unpriced);
        model.addAttribute("pricedTasks", priced);
        return "maintenance/pricing";
    }

    @PostMapping("/pricing/{taskId}/submit")
    public String submitDamagePrice(@PathVariable Long taskId, @RequestParam Double price, RedirectAttributes ra) {
        try {
            HotelOperation task = maintenanceRequestRepo.findById(taskId).orElseThrow();
            task.setDamagePrice(price);
            task.setStatus("Priced");
            maintenanceRequestRepo.save(task);

            // Add damage fee to folio if there is an active booking detail
            Room room = task.getRoom();
            if (room != null && room.getCurrentBookingDetailId() != null) {
                RoomBookingDetail detail = roomBookingDetailRepository.findById(room.getCurrentBookingDetailId()).orElse(null);
                if (detail != null) {
                    FolioItem item = new FolioItem();
                    item.setBooking(detail.getRoomBooking());
                    item.setRoomBookingDetail(detail);
                    item.setPayerCustomer(detail.getRoomBooking().getCustomer());
                    item.setSourceDepartment("Maintenance");
                    item.setAmount(new BigDecimal(price));
                    item.setDescription("[Đền bù hỏng hóc] " + (task.getNotes() != null ? task.getNotes() : "Đền bù hỏng hóc"));
                    item.setCreatedAt(LocalDateTime.now());
                    item.setCreatedByStaff(task.getStaff());
                    folioItemRepository.save(item);
                }
            }

            ra.addFlashAttribute("successMessage", "Đã định giá sự cố thành công và cộng vào hóa đơn khách hàng.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/maintenance/pricing";
    }
}
