package com.kawai.controllers.web;

import com.kawai.models.HotelOperation;
import com.kawai.repositories.EmployeeRepository;
import com.kawai.repositories.MaintenanceRequestRepository;
import com.kawai.repositories.RoomRepository;
import com.kawai.services.interfaces.HousekeepingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Controller
@RequestMapping("/maintenance")
@PreAuthorize("hasAnyAuthority('OP_MAINTENANCE','ROLE_ADMIN','ROLE_MAINTENANCE','ROLE_MAINTAINER','MAINTENANCE')")
public class MaintenanceWebController {
    private final HousekeepingService housekeepingService;
    private final MaintenanceRequestRepository maintenanceRequestRepo;
    private final RoomRepository roomRepository;
    private final EmployeeRepository employeeRepository;

    public MaintenanceWebController(HousekeepingService hs,
            MaintenanceRequestRepository mrr, RoomRepository rr, EmployeeRepository er) {
        this.housekeepingService = hs; this.maintenanceRequestRepo = mrr;
        this.roomRepository = rr; this.employeeRepository = er;
    }

    @ModelAttribute("todayLabel")
    public String todayLabel() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", new Locale("vi")));
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("pendingTasks", maintenanceRequestRepo.findByOperationalTypeAndStatus("MAINTENANCE","Pending"));
        model.addAttribute("inProgressTasks", maintenanceRequestRepo.findByOperationalTypeAndStatus("MAINTENANCE","InProgress"));
        model.addAttribute("pausedTasks", maintenanceRequestRepo.findByOperationalTypeAndStatus("MAINTENANCE","Paused"));
        long underMaintenance = roomRepository.countByRoomStatus("Maintenance");
        model.addAttribute("maintenanceRooms", underMaintenance);
        var completed = maintenanceRequestRepo.findByOperationalTypeAndStatus("MAINTENANCE","Completed");
        model.addAttribute("completedTasks", completed);
        model.addAttribute("completedCount", completed.size());
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
}
