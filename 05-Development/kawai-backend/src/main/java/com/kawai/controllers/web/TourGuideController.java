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

    @org.springframework.beans.factory.annotation.Autowired
    private com.kawai.repositories.TourRepository tourRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private com.kawai.repositories.EmployeeRepository employeeRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private com.kawai.repositories.AccountRepository accountRepository;

    @org.springframework.web.bind.annotation.ModelAttribute
    public void addEmployeeToModel(Principal principal, Model model, jakarta.servlet.http.HttpServletRequest request) {
        if (principal != null) {
            com.kawai.models.Employee employee = employeeRepository.findByAccountUsername(principal.getName()).orElse(null);
            model.addAttribute("employee", employee);
        }
        if (request != null) {
            model.addAttribute("requestUri", request.getRequestURI());
        }
    }

    @org.springframework.web.bind.annotation.PostMapping("/profile/create")
    public String createProfile(
            Principal principal,
            @org.springframework.web.bind.annotation.RequestParam("fullName") String fullName,
            @org.springframework.web.bind.annotation.RequestParam("gender") String gender,
            @org.springframework.web.bind.annotation.RequestParam("cccd") String cccd,
            @org.springframework.web.bind.annotation.RequestParam("phone") String phone,
            @org.springframework.web.bind.annotation.RequestParam("email") String email,
            @org.springframework.web.bind.annotation.RequestParam("redirectUrl") String redirectUrl) {
        
        if (principal != null) {
            com.kawai.models.Account account = accountRepository.findByUsername(principal.getName()).orElse(null);
            if (account != null) {
                com.kawai.models.Employee employee = new com.kawai.models.Employee();
                employee.setAccount(account);
                employee.setFullName(fullName);
                employee.setGender(gender);
                employee.setCccd(cccd);
                employee.setPhone(phone);
                employee.setEmail(email);
                employee.setSalary(java.math.BigDecimal.ZERO);
                employeeRepository.save(employee);
            }
        }
        return "redirect:" + redirectUrl;
    }

    @org.springframework.web.bind.annotation.PostMapping("/profile/update")
    public String updateProfile(
            Principal principal,
            @org.springframework.web.bind.annotation.RequestParam("fullName") String fullName,
            @org.springframework.web.bind.annotation.RequestParam("gender") String gender,
            @org.springframework.web.bind.annotation.RequestParam("cccd") String cccd,
            @org.springframework.web.bind.annotation.RequestParam("phone") String phone,
            @org.springframework.web.bind.annotation.RequestParam("email") String email,
            @org.springframework.web.bind.annotation.RequestParam("redirectUrl") String redirectUrl) {
        
        if (principal != null) {
            employeeRepository.findByAccountUsername(principal.getName()).ifPresent(employee -> {
                employee.setFullName(fullName);
                employee.setGender(gender);
                employee.setCccd(cccd);
                employee.setPhone(phone);
                employee.setEmail(email);
                employeeRepository.save(employee);
            });
        }
        return "redirect:" + redirectUrl;
    }

    @org.springframework.web.bind.annotation.PostMapping("/profile/delete")
    public String deleteProfile(
            Principal principal,
            @org.springframework.web.bind.annotation.RequestParam("redirectUrl") String redirectUrl) {
        
        if (principal != null) {
            employeeRepository.findByAccountUsername(principal.getName()).ifPresent(employee -> {
                employeeRepository.delete(employee);
            });
        }
        return "redirect:" + redirectUrl;
    }
    @GetMapping("/dashboard")
    public String dashboard(Principal principal, Model model) {
        model.addAttribute("isLoggedIn", principal != null);
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
        model.addAttribute("isLoggedIn", principal != null);
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
        return "tour/Tour";
    }

    @GetMapping("/doantu")
    public String doanTu(Principal principal, Model model) {
        model.addAttribute("isLoggedIn", principal != null);
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
        com.kawai.models.Tour tour = tourRepository.findFirstByTourTypeAndIsActiveTrueOrderByIdDesc("doantu").orElse(null);
        if (tour == null) {
            tour = tourRepository.findByTourType("doantu").orElse(null);
        }
        if (tour != null) {
            model.addAttribute("tour", tour);
        }
        return "tour/DoanTu";
    }

    @GetMapping("/dongnoi")
    public String dongNoi(Principal principal, Model model) {
        model.addAttribute("isLoggedIn", principal != null);
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
        com.kawai.models.Tour tour = tourRepository.findFirstByTourTypeAndIsActiveTrueOrderByIdDesc("dongnoi").orElse(null);
        if (tour == null) {
            tour = tourRepository.findByTourType("dongnoi").orElse(null);
        }
        if (tour != null) {
            model.addAttribute("tour", tour);
        }
        return "tour/DongNoi";
    }

    @GetMapping("/disan")
    public String diSan(Principal principal, Model model) {
        model.addAttribute("isLoggedIn", principal != null);
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
        com.kawai.models.Tour tour = tourRepository.findFirstByTourTypeAndIsActiveTrueOrderByIdDesc("disan").orElse(null);
        if (tour == null) {
            tour = tourRepository.findByTourType("disan").orElse(null);
        }
        if (tour != null) {
            model.addAttribute("tour", tour);
        }
        return "tour/DiSan";
    }

    @GetMapping("/tinhlang")
    public String tinhLang(Principal principal, Model model) {
        model.addAttribute("isLoggedIn", principal != null);
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
        com.kawai.models.Tour tour = tourRepository.findFirstByTourTypeAndIsActiveTrueOrderByIdDesc("tinhlang").orElse(null);
        if (tour == null) {
            tour = tourRepository.findByTourType("tinhlang").orElse(null);
        }
        if (tour != null) {
            model.addAttribute("tour", tour);
        }
        return "tour/TinhLang";
    }

    @org.springframework.web.bind.annotation.PostMapping("/tour/update")
    public String updateTour(
            @org.springframework.web.bind.annotation.RequestParam("id") Long id,
            @org.springframework.web.bind.annotation.RequestParam("tourName") String tourName,
            @org.springframework.web.bind.annotation.RequestParam("description") String description,
            @org.springframework.web.bind.annotation.RequestParam("duration") String duration,
            @org.springframework.web.bind.annotation.RequestParam("basePrice") java.math.BigDecimal basePrice,
            @org.springframework.web.bind.annotation.RequestParam("shortQuote") String shortQuote,
            @org.springframework.web.bind.annotation.RequestParam("tourType") String tourType) {
        
        com.kawai.models.Tour tour = tourRepository.findById(id).orElse(new com.kawai.models.Tour());
        tour.setTourName(tourName);
        tour.setDescription(description);
        tour.setDuration(duration);
        tour.setBasePrice(basePrice);
        tour.setShortQuote(shortQuote);
        tour.setTourType(tourType);
        tourRepository.save(tour);
        
        return "redirect:/tourguide/" + tourType;
    }

    @org.springframework.web.bind.annotation.PostMapping("/tour/delete")
    public String deleteTour(
            @org.springframework.web.bind.annotation.RequestParam("id") Long id,
            @org.springframework.web.bind.annotation.RequestParam("tourType") String tourType) {
        
        tourRepository.findById(id).ifPresent(t -> {
            t.setIsActive(false);
            tourRepository.save(t);
        });
        
        return "redirect:/tourguide/" + tourType;
    }

    @org.springframework.web.bind.annotation.PostMapping("/tour/reactivate")
    public String reactivateTour(
            @org.springframework.web.bind.annotation.RequestParam("id") Long id,
            @org.springframework.web.bind.annotation.RequestParam("tourType") String tourType) {
        
        tourRepository.findById(id).ifPresent(t -> {
            t.setIsActive(true);
            tourRepository.save(t);
        });
        
        return "redirect:/tourguide/" + tourType;
    }

    @org.springframework.web.bind.annotation.PostMapping("/tour/create")
    public String createTour(
            @org.springframework.web.bind.annotation.RequestParam("tourName") String tourName,
            @org.springframework.web.bind.annotation.RequestParam("description") String description,
            @org.springframework.web.bind.annotation.RequestParam("duration") String duration,
            @org.springframework.web.bind.annotation.RequestParam("basePrice") java.math.BigDecimal basePrice,
            @org.springframework.web.bind.annotation.RequestParam("shortQuote") String shortQuote,
            @org.springframework.web.bind.annotation.RequestParam("tourType") String tourType) {
        
        // Deactivate existing tour of this type so only one is active at a time
        tourRepository.findByTourType(tourType).ifPresent(t -> {
            t.setIsActive(false);
            tourRepository.save(t);
        });

        com.kawai.models.Tour tour = new com.kawai.models.Tour();
        tour.setTourName(tourName);
        tour.setDescription(description);
        tour.setDuration(duration);
        tour.setBasePrice(basePrice);
        tour.setShortQuote(shortQuote);
        tour.setTourType(tourType);
        tour.setIsActive(true);
        tour.setCreatedAt(java.time.LocalDateTime.now());
        tourRepository.save(tour);
        
        return "redirect:/tourguide/" + tourType;
    }
}
