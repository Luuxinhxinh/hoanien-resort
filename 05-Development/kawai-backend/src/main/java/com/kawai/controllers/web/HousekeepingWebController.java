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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Locale;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Controller
@RequestMapping("/housekeeping")
@PreAuthorize("hasAnyAuthority('OP_HOUSEKEEPING','ROLE_ADMIN','ROLE_HOUSEKEEPING','ROLE_MANAGER','OP_RECEPTION_WALKIN','OP_RECEPTION_CHECKIN','OP_RECEPTION_CHECKOUT','OP_RECEPTION_INHOUSE')")
public class HousekeepingWebController {
    private final HousekeepingService housekeepingService;
    private final HousekeepingTaskRepository housekeepingTaskRepo;
    private final RoomRepository roomRepository;
    private final EmployeeRepository employeeRepository;
    private final StaffScheduleRepository staffScheduleRepository;
    private final com.kawai.services.interfaces.ShiftService shiftService;

    public HousekeepingWebController(HousekeepingService hs,
            HousekeepingTaskRepository htr, RoomRepository rr, EmployeeRepository er, StaffScheduleRepository ssr,
            com.kawai.services.interfaces.ShiftService shiftService) {
        this.housekeepingService = hs; this.housekeepingTaskRepo = htr;
        this.roomRepository = rr; this.employeeRepository = er;
        this.staffScheduleRepository = ssr;
        this.shiftService = shiftService;
    }

    @ModelAttribute("todayLabel")
    public String todayLabel() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", new Locale("vi")));
    }

    private void populateCommonData(Model model, Authentication auth) {
        Employee currentStaff = employeeRepository.findByAccountUsername(auth.getName()).orElse(null);
        if (currentStaff != null) {
            model.addAttribute("currentStaff", currentStaff);
            LocalDate startOfWeek = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            LocalDate endOfWeek = startOfWeek.plusDays(6);
            var weeklySchedules = staffScheduleRepository.findByEmployeeIdAndWorkDateBetweenOrderByWorkDateAsc(currentStaff.getId(), startOfWeek, endOfWeek);
            model.addAttribute("myWeeklySchedules", weeklySchedules);
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session, 
            @RequestParam(required = false) String bypass,
            @RequestParam(required = false) String forceOutOfShift) {
        
        if ("true".equals(bypass)) {
            session.setAttribute("demoBypassShift", true);
        }
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isOnShift = false;
        
        if ("true".equals(forceOutOfShift)) {
            isOnShift = false;
        } else {
            isOnShift = shiftService.checkIsOnShift(auth, session);
        }
        
        populateCommonData(model, auth);
        
        model.addAttribute("isOnShift", isOnShift);
        
        if (isOnShift) {
            List<String> validTypes = List.of("ROOM_CHECK", "CHECKOUT_CLEAN", "GUEST_REQUEST", "URGENT_CLEAN");
            model.addAttribute("pendingTasks", housekeepingTaskRepo.findByOperationalTypesAndStatusSorted(validTypes, "Pending"));
            model.addAttribute("inProgressTasks", housekeepingTaskRepo.findByOperationalTypesAndStatusSorted(validTypes, "InProgress"));
            model.addAttribute("completedToday", housekeepingTaskRepo.findByOperationalTypesAndStatusSorted(validTypes, "Completed"));
            long dirtyRooms = roomRepository.countByRoomStatus("Vacant_Dirty") + roomRepository.countByRoomStatus("Occupied_Dirty");
            long cleanRooms = roomRepository.countByRoomStatus("Vacant_Clean") + roomRepository.countByRoomStatus("Occupied_Clean") + roomRepository.countByRoomStatus("Occupied");
            long occupiedRooms = roomRepository.countByRoomStatus("Occupied_Clean") + roomRepository.countByRoomStatus("Occupied_Dirty") + roomRepository.countByRoomStatus("Occupied");
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
    public String completeTask(@PathVariable Long taskId, @RequestParam(required=false) String notes, RedirectAttributes ra, HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!shiftService.checkIsOnShift(auth, session)) {
            ra.addFlashAttribute("errorMessage", "Lỗi: Bạn chưa vào ca làm việc.");
            return "redirect:/housekeeping/dashboard";
        }
        try { housekeepingService.updateRoomToClean(taskId, notes);
            ra.addFlashAttribute("successMessage","Đã đánh dấu phòng sạch thành công.");
        } catch(Exception e) { ra.addFlashAttribute("errorMessage","Lỗi: "+e.getMessage()); }
        return "redirect:/housekeeping/dashboard";
    }

    @PostMapping("/tasks/{taskId}/start")
    public String startTask(@PathVariable Long taskId, RedirectAttributes ra, HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!shiftService.checkIsOnShift(auth, session)) {
            ra.addFlashAttribute("errorMessage", "Lỗi: Bạn chưa vào ca làm việc.");
            return "redirect:/housekeeping/dashboard";
        }
        try { var t = housekeepingTaskRepo.findById(taskId).orElseThrow();
            t.setStatus("InProgress"); t.setStartedAt(java.time.LocalDateTime.now());
            housekeepingTaskRepo.save(t);
            ra.addFlashAttribute("successMessage","Đã bắt đầu dọn phòng.");
        } catch(Exception e) { ra.addFlashAttribute("errorMessage","Lỗi: "+e.getMessage()); }
        return "redirect:/housekeeping/dashboard";
    }

    @PostMapping("/report-damage")
    public String reportDamage(@RequestParam Long roomId, @RequestParam String notes,
            @RequestParam(required = false, defaultValue = "false") boolean isEmergency,
            @RequestParam(required = false) String taskType,
            @RequestParam(value = "image", required = false) MultipartFile image,
            Authentication auth, RedirectAttributes ra, HttpSession session) {
        if (!shiftService.checkIsOnShift(auth, session)) {
            ra.addFlashAttribute("errorMessage", "Lỗi: Bạn chưa vào ca làm việc.");
            return "redirect:/housekeeping/dashboard";
        }
        try {
            Long sid = null;
            if (auth != null && auth.getName() != null) {
                sid = employeeRepository.findByAccountUsername(auth.getName())
                        .map(com.kawai.models.Employee::getId)
                        .orElse(null);
            }

            boolean emergency = false;
            if ("GUEST_REQUEST".equalsIgnoreCase(taskType) || "URGENT_CLEAN".equalsIgnoreCase(taskType)) {
                emergency = true;
            } else if ("ROOM_CHECK".equalsIgnoreCase(taskType) || "CHECKOUT_CLEAN".equalsIgnoreCase(taskType)) {
                emergency = false;
            } else {
                emergency = isEmergency;
            }

            HotelOperation task = housekeepingService.createMaintenanceRequest(roomId, sid, notes, emergency);
            
            if (image != null && !image.isEmpty()) {
                String UPLOAD_DIR = com.kawai.utils.UploadPathResolver.resolvePath("uploads/");
                File directory = new File(UPLOAD_DIR);
                if (!directory.exists()) {
                    directory.mkdirs();
                }
                
                String originalFilename = image.getOriginalFilename();
                String extension = originalFilename != null && originalFilename.contains(".") ? 
                                   originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
                String newFilename = UUID.randomUUID().toString() + extension;
                
                Path filePath = Paths.get(UPLOAD_DIR + newFilename);
                Files.write(filePath, image.getBytes());
                
                task.setImageUrl("/uploads/" + newFilename);
                housekeepingTaskRepo.save(task);
            }
            
            ra.addFlashAttribute("successMessage", "Đã gửi yêu cầu bảo trì.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/housekeeping/dashboard";
    }
}

