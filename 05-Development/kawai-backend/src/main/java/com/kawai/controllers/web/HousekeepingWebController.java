package com.kawai.controllers.web;

import com.kawai.models.HotelOperation;
import com.kawai.repositories.EmployeeRepository;
import com.kawai.repositories.HousekeepingTaskRepository;
import com.kawai.repositories.RoomRepository;
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
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping("/housekeeping")
@PreAuthorize("hasAnyAuthority('OP_HOUSEKEEPING','ROLE_ADMIN','ROLE_MANAGER')")
public class HousekeepingWebController {
    private final HousekeepingService housekeepingService;
    private final HousekeepingTaskRepository housekeepingTaskRepo;
    private final RoomRepository roomRepository;
    private final EmployeeRepository employeeRepository;
    private final StaffScheduleRepository staffScheduleRepository;

    public HousekeepingWebController(HousekeepingService hs,
            HousekeepingTaskRepository htr, RoomRepository rr, EmployeeRepository er, StaffScheduleRepository ssr) {
        this.housekeepingService = hs; this.housekeepingTaskRepo = htr;
        this.roomRepository = rr; this.employeeRepository = er;
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
            List<String> validTypes = List.of("ROOM_CHECK", "CHECKOUT_CLEAN", "GUEST_REQUEST", "URGENT_CLEAN");
            model.addAttribute("pendingTasks", housekeepingTaskRepo.findByOperationalTypesAndStatusSorted(validTypes, "Pending"));
            model.addAttribute("inProgressTasks", housekeepingTaskRepo.findByOperationalTypesAndStatusSorted(validTypes, "InProgress"));
            model.addAttribute("completedToday", housekeepingTaskRepo.findByOperationalTypesAndStatusSorted(validTypes, "Completed"));
            long dirtyRooms = roomRepository.countByRoomStatus("Vacant_Dirty") + roomRepository.countByRoomStatus("Occupied_Dirty");
            long cleanRooms = roomRepository.countByRoomStatus("Vacant_Clean") + roomRepository.countByRoomStatus("Occupied_Clean");
            long occupiedRooms = roomRepository.countByRoomStatus("Occupied_Clean") + roomRepository.countByRoomStatus("Occupied_Dirty");
            long vacantRooms = roomRepository.countByRoomStatus("Vacant_Clean") + roomRepository.countByRoomStatus("Vacant_Dirty");
            model.addAttribute("dirtyRooms", dirtyRooms);
            model.addAttribute("cleanRooms", cleanRooms);
            model.addAttribute("occupiedRooms", occupiedRooms);
            model.addAttribute("vacantRooms", vacantRooms);
            model.addAttribute("maintenanceRooms", roomRepository.countByRoomStatus("Maintenance"));
        }
        
        return "housekeeping/dashboard";
    }

    @PostMapping("/tasks/{taskId}/complete")
    public String completeTask(@PathVariable Long taskId, @RequestParam(required=false) String notes, RedirectAttributes ra) {
        try { housekeepingService.updateRoomToClean(taskId, notes);
            ra.addFlashAttribute("successMessage","Đã đánh dấu phòng sạch thành công.");
        } catch(Exception e) { ra.addFlashAttribute("errorMessage","Lỗi: "+e.getMessage()); }
        return "redirect:/housekeeping/dashboard";
    }

    @PostMapping("/tasks/{taskId}/start")
    public String startTask(@PathVariable Long taskId, RedirectAttributes ra) {
        try { var t = housekeepingTaskRepo.findById(taskId).orElseThrow();
            t.setStatus("InProgress"); t.setStartedAt(java.time.LocalDateTime.now());
            housekeepingTaskRepo.save(t);
            ra.addFlashAttribute("successMessage","Đã bắt đầu dọn phòng.");
        } catch(Exception e) { ra.addFlashAttribute("errorMessage","Lỗi: "+e.getMessage()); }
        return "redirect:/housekeeping/dashboard";
    }

    @PostMapping("/report-damage")
    public String reportDamage(@RequestParam Long roomId, @RequestParam String notes,
            Authentication auth, RedirectAttributes ra) {
        try { Long sid = auth!=null && auth.getName()!=null ?
            employeeRepository.findByAccountUsername(auth.getName()).map(com.kawai.models.Employee::getId).orElse(null) : null;
            housekeepingService.createMaintenanceRequest(roomId,sid,notes);
            ra.addFlashAttribute("successMessage","Đã gửi yêu cầu bảo trì.");
        } catch(Exception e) { ra.addFlashAttribute("errorMessage","Lỗi: "+e.getMessage()); }
        return "redirect:/housekeeping/dashboard";
    }
}

