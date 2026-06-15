package com.kawai.controllers.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.ui.Model;
import java.security.Principal;

@Controller
@RequestMapping("/tourguide")
public class TourGuideController {

    @org.springframework.beans.factory.annotation.Autowired
    private com.kawai.repositories.TourAttendeeRepository tourAttendeeRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private com.kawai.repositories.TourScheduleRepository tourScheduleRepository;

    @GetMapping("/dashboard")
    public String dashboard(Principal principal, Model model) {
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
        java.time.LocalDate today = java.time.LocalDate.now();
        
        java.util.List<com.kawai.models.TourAttendee> attendees = tourAttendeeRepository
                .findByTourBooking_Schedule_DepartureDate(today);
        
        // Đảm bảo luôn có tour và khách của hôm nay để test FaceID
        if (attendees.isEmpty()) {
            java.util.List<com.kawai.models.TourSchedule> schedules = tourScheduleRepository.findAll();
            if (!schedules.isEmpty()) {
                com.kawai.models.TourSchedule targetSchedule = schedules.stream()
                        .filter(s -> s.getId() != null && s.getId() == 5L)
                        .findFirst()
                        .orElse(schedules.get(0));
                targetSchedule.setDepartureDate(today);
                tourScheduleRepository.save(targetSchedule);
                
                // Truy vấn lại
                attendees = tourAttendeeRepository.findByTourBooking_Schedule_DepartureDate(today);
            }
        }
        
        model.addAttribute("attendees", attendees);

        // Lấy thông tin tour từ attendee đầu tiên
        if (!attendees.isEmpty()) {
            com.kawai.models.TourBooking tb = attendees.get(0).getTourBooking();
            if (tb != null && tb.getSchedule() != null) {
                com.kawai.models.TourSchedule sched = tb.getSchedule();
                model.addAttribute("tourName",
                        sched.getTour() != null ? sched.getTour().getTourName() : "Tour hôm nay");
                model.addAttribute("departureTime",
                        sched.getDepartureTime() != null ? sched.getDepartureTime().toString().substring(0, 5)
                                : "--:--");
                model.addAttribute("departureDate",
                        sched.getDepartureDate() != null ? sched.getDepartureDate().toString() : today.toString());
                model.addAttribute("scheduleStatus", sched.getScheduleStatus());
                model.addAttribute("maxCapacity", sched.getTour() != null ? sched.getTour().getMaxCapacity() : 0);
            }
        }

        // Đếm số khách đã điểm danh
        long checkedIn = attendees.stream()
                .filter(a -> "Checked_In".equals(a.getStatus()))
                .count();
        model.addAttribute("checkedInCount", checkedIn);
        model.addAttribute("totalAttendees", attendees.size());

        return "tour/FaceID";
    }

    @GetMapping("/tour")
    public String tour(Principal principal, Model model) {
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
        return "tour/Tour";
    }

    @GetMapping("/doantu")
    public String doanTu(Principal principal, Model model) {
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
        return "tour/DoanTu";
    }

    @GetMapping("/dongnoi")
    public String dongNoi(Principal principal, Model model) {
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
        return "tour/DongNoi";
    }

    @GetMapping("/disan")
    public String diSan(Principal principal, Model model) {
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
        return "tour/DiSan";
    }

    @GetMapping("/tinhlang")
    public String tinhLang(Principal principal, Model model) {
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
        return "tour/TinhLang";
    }
}
