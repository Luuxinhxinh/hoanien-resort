package com.kawai.controllers.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.ui.Model;
import java.security.Principal;

import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@RequestMapping("/tourguide")
@PreAuthorize("hasAnyAuthority('OP_TOUR', 'ROLE_ADMIN', 'ROLE_MANAGER')")
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

    @org.springframework.beans.factory.annotation.Autowired
    private com.kawai.repositories.CustomerRepository customerRepository;

    @org.springframework.web.bind.annotation.ModelAttribute
    public void addEmployeeToModel(Principal principal, Model model, jakarta.servlet.http.HttpServletRequest request) {
        if (principal != null) {
            com.kawai.models.Employee employee = employeeRepository.findByAccountUsername(principal.getName()).orElse(null);
            if (employee == null) {
                employee = new com.kawai.models.Employee();
                employee.setFullName("NguynNgoc");
                employee.setGender("Nam");
                employee.setPhone("0987654321");
                employee.setEmail(principal.getName());
            }
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
        String cleanUrl = redirectUrl.replaceAll("[&?]toast=[^&]*", "");
        String separator = cleanUrl.contains("?") ? "&" : "?";
        return "redirect:" + cleanUrl + separator + "toast=create_success";
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
        String cleanUrl = redirectUrl.replaceAll("[&?]toast=[^&]*", "");
        String separator = cleanUrl.contains("?") ? "&" : "?";
        return "redirect:" + cleanUrl + separator + "toast=update_success";
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
        String cleanUrl = redirectUrl.replaceAll("[&?]toast=[^&]*", "");
        String separator = cleanUrl.contains("?") ? "&" : "?";
        return "redirect:" + cleanUrl + separator + "toast=delete_success";
    }
    @GetMapping("/dashboard")
    @org.springframework.transaction.annotation.Transactional
    public String dashboard(
            @org.springframework.web.bind.annotation.RequestParam(value = "scanned", required = false) String scanned,
            @org.springframework.web.bind.annotation.RequestParam(value = "scheduleId", required = false) Long scheduleId,
            Principal principal,
            Model model) {
        
        // Ensure customer 5 is renamed to Ngọc Thị in the database
        try {
            customerRepository.findById(5L).ifPresent(c -> {
                if ("Lê Quang".equals(c.getFullName())) {
                    c.setFullName("Ngọc Thị");
                    c.setGender("Nữ");
                    customerRepository.saveAndFlush(c);
                }
            });
            accountRepository.findById(9L).ifPresent(a -> {
                if ("lequang".equals(a.getUsername())) {
                    a.setUsername("ngocthi");
                    accountRepository.saveAndFlush(a);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        model.addAttribute("isLoggedIn", principal != null);
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
        java.time.LocalDate today = java.time.LocalDate.now();
        
        // Find targeted schedule
        com.kawai.models.TourSchedule targetSchedule = null;
        if (scheduleId != null) {
            targetSchedule = tourScheduleRepository.findById(scheduleId).orElse(null);
        }
        
        if (targetSchedule == null) {
            // Find default schedule based on logged-in guide profile
            java.util.List<com.kawai.models.TourSchedule> schedules = tourScheduleRepository.findAll();
            if (!schedules.isEmpty()) {
                com.kawai.models.Employee emp = null;
                if (principal != null) {
                    emp = employeeRepository.findByAccountUsername(principal.getName()).orElse(null);
                    if (emp == null) {
                        emp = new com.kawai.models.Employee();
                        emp.setFullName("NguynNgoc");
                    }
                }
                final String loggedInName = (emp != null) ? emp.getFullName() : "NguynNgoc";
                
                targetSchedule = schedules.stream()
                        .filter(s -> {
                            String guideName = "NguynNgoc";
                            if (s.getTour() != null) {
                                String tn = s.getTour().getTourName();
                                if (tn.contains("Tinh Túy Đồng Nội") || tn.contains("Tinh túy đồng nội")) {
                                    guideName = "Ngọc Lan";
                                } else if (tn.contains("Tĩnh Lặng Liên Hoa") || tn.contains("Tĩnh lặng liên hoa")) {
                                    guideName = "Hoàng Nam";
                                }
                            }
                            return guideName.equalsIgnoreCase(loggedInName);
                        })
                        .findFirst()
                        .orElse(null);
                
                if (targetSchedule == null) {
                    targetSchedule = schedules.stream()
                            .filter(s -> s.getId() != null && s.getId() == 1L)
                            .findFirst()
                            .orElse(schedules.get(0));
                }
                
                if (targetSchedule != null && !today.equals(targetSchedule.getDepartureDate())) {
                    targetSchedule.setDepartureDate(today);
                    tourScheduleRepository.saveAndFlush(targetSchedule);
                }
            }
        }
        
        java.util.List<com.kawai.models.TourAttendee> attendees = new java.util.ArrayList<>();
        if (targetSchedule != null) {
            attendees = tourAttendeeRepository.findByTourBooking_Schedule_Id(targetSchedule.getId());
        }
        
        if (attendees.isEmpty()) {
            attendees = tourAttendeeRepository.findByTourBooking_Schedule_DepartureDate(today);
        }

        // Nếu không phải là load sau khi quét thành công (scanned == true), tự động reset trạng thái chờ FaceID
        if (!"true".equals(scanned)) {
            boolean didReset = false;
            for (com.kawai.models.TourAttendee attendee : attendees) {
                if (!"Not_Show".equals(attendee.getStatus())) {
                    attendee.setStatus("Not_Show");
                    attendee.setFaceMatchedAt(null);
                    tourAttendeeRepository.saveAndFlush(attendee);
                    didReset = true;
                }
            }
            if (didReset && targetSchedule != null) {
                attendees = tourAttendeeRepository.findByTourBooking_Schedule_Id(targetSchedule.getId());
                if (attendees.isEmpty()) {
                    attendees = tourAttendeeRepository.findByTourBooking_Schedule_DepartureDate(today);
                }
            }
        }
        
        model.addAttribute("attendees", attendees);

        // Lấy thông tin tour
        if (targetSchedule != null) {
            model.addAttribute("tourName",
                    targetSchedule.getTour() != null ? targetSchedule.getTour().getTourName() : "Tour hôm nay");
            model.addAttribute("departureTime",
                    targetSchedule.getDepartureTime() != null ? targetSchedule.getDepartureTime().toString().substring(0, 5)
                            : "--:--");
            model.addAttribute("departureDate",
                    targetSchedule.getDepartureDate() != null ? targetSchedule.getDepartureDate().toString() : today.toString());
            model.addAttribute("scheduleStatus", targetSchedule.getScheduleStatus());
            model.addAttribute("maxCapacity", targetSchedule.getTour() != null ? targetSchedule.getTour().getMaxCapacity() : 0);
        } else if (!attendees.isEmpty()) {
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
        
        // Ensure schedule 5 is set to today for demo purposes
        java.time.LocalDate today = java.time.LocalDate.now();
        tourScheduleRepository.findById(5L).ifPresent(sched -> {
            if (!today.equals(sched.getDepartureDate())) {
                sched.setDepartureDate(today);
                tourScheduleRepository.saveAndFlush(sched);
            }
        });
        
        // Fetch all tour schedules from DB
        java.util.List<com.kawai.models.TourSchedule> dbSchedules = tourScheduleRepository.findAll();
        
        // Ensure schedule 5 is at the top of the list for demo
        dbSchedules.sort((s1, s2) -> {
            if (s1.getId() != null && s1.getId() == 5L) return -1;
            if (s2.getId() != null && s2.getId() == 5L) return 1;
            if (s1.getDepartureDate() == null || s2.getDepartureDate() == null) return 0;
            return s2.getDepartureDate().compareTo(s1.getDepartureDate());
        });

        java.util.List<java.util.Map<String, Object>> dbToursList = new java.util.ArrayList<>();
        
        for (com.kawai.models.TourSchedule sched : dbSchedules) {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", sched.getId());
            map.put("name", sched.getTour() != null ? sched.getTour().getTourName() : "Chưa xác định");
            
            // Format time
            String timeStr = "08:00 - 12:00";
            if (sched.getDepartureTime() != null) {
                java.time.LocalTime depTime = sched.getDepartureTime();
                java.time.LocalTime endTime = depTime.plusHours(4); // Default tour duration 4 hours
                timeStr = depTime.toString().substring(0, 5) + " - " + endTime.toString().substring(0, 5);
            }
            map.put("time", timeStr);
            
            // Format guide name
            String guideName = "NguynNgoc";
            if (sched.getTour() != null) {
                String tn = sched.getTour().getTourName();
                if (sched.getId() != null && sched.getId() == 5L) {
                    guideName = "NguynNgoc";
                } else if (tn.contains("Tinh Túy Đồng Nội") || tn.contains("Tinh túy đồng nội")) {
                    guideName = "Ngọc Lan";
                } else if (tn.contains("Tĩnh Lặng Liên Hoa") || tn.contains("Tĩnh lặng liên hoa")) {
                    guideName = "Hoàng Nam";
                }
            }
            map.put("guide", guideName);
            
            String dbStatus = sched.getScheduleStatus();
            String mappedStatus = "upcoming";
            if (dbStatus != null) {
                if ("completed".equalsIgnoreCase(dbStatus) || "finish".equalsIgnoreCase(dbStatus) || "done".equalsIgnoreCase(dbStatus)) {
                    mappedStatus = "completed";
                }
            }
            map.put("status", mappedStatus);
            map.put("date", sched.getDepartureDate() != null ? sched.getDepartureDate().toString() : "2026-06-27");
            
            // Match image
            String image = "/AnhTour/z7930565879475_de7864577660b0726376ea947dbe94de.jpg";
            if (sched.getTour() != null) {
                String name = sched.getTour().getTourName();
                if (name.contains("Tinh Túy Đồng Nội") || name.contains("Tinh túy đồng nội")) {
                    image = "/AnhTour/com.jfif";
                } else if (name.contains("Tĩnh Lặng Liên Hoa") || name.contains("Tĩnh lặng liên hoa")) {
                    image = "/AnhTour/ThapMuoi.jpg";
                } else if (name.contains("Di sản")) {
                    image = "/AnhTour/ThuCong.jpg";
                }
            }
            map.put("image", image);
            map.put("type", "Cao cấp");
            
            // Count total bookings/attendees dynamically from database
            int count = tourAttendeeRepository.findByTourBooking_Schedule_Id(sched.getId()).size();
            map.put("guests", count + " khách");
            
            dbToursList.add(map);
        }
        
        model.addAttribute("dbTours", dbToursList);
        
        return "tour/Tour";
    }

    @GetMapping("/feedback")
    public String feedback(Principal principal, Model model) {
        model.addAttribute("isLoggedIn", principal != null);
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
        return "tour/Feedback";
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
