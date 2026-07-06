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

    @org.springframework.beans.factory.annotation.Autowired
    private com.kawai.repositories.TourBookingRepository tourBookingRepository;

    @org.springframework.web.bind.annotation.ModelAttribute
    public void addEmployeeToModel(Principal principal, Model model, jakarta.servlet.http.HttpServletRequest request) {
        if (principal != null) {
            com.kawai.models.Employee employee = employeeRepository.findByAccountUsername(principal.getName()).orElse(null);
            if (employee != null) {
                if ("Nguyễn Hướng Dẫn".equals(employee.getFullName())) {
                    employee.setFullName("NguynNgoc");
                    employeeRepository.saveAndFlush(employee);
                }
            } else {
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
    private void healDatabase() {
        try {
            // Ensure customer 5 is renamed to Ngọc Thị in the database
            customerRepository.findById(5L).ifPresent(c -> {
                if (!"Ngọc Thị".equals(c.getFullName())) {
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
            employeeRepository.findById(5L).ifPresent(e -> {
                if ("Nguyễn Hướng Dẫn".equals(e.getFullName())) {
                    e.setFullName("NguynNgoc");
                    employeeRepository.saveAndFlush(e);
                }
            });
            employeeRepository.findById(6L).ifPresent(e -> {
                if ("Phạm Quốc Bảo".equals(e.getFullName())) {
                    e.setFullName("Ngọc Lan");
                    employeeRepository.saveAndFlush(e);
                }
            });
            employeeRepository.findById(7L).ifPresent(e -> {
                if ("Trần Thu Hà".equals(e.getFullName())) {
                    e.setFullName("Hoàng Nam");
                    employeeRepository.saveAndFlush(e);
                }
            });

            java.time.LocalDate today = java.time.LocalDate.now();
            // Ensure schedule 1 (Đoàn tụ - Huế) departure date is set to today
            tourScheduleRepository.findById(1L).ifPresent(sched -> {
                if (!today.equals(sched.getDepartureDate())) {
                    sched.setDepartureDate(today);
                    tourScheduleRepository.saveAndFlush(sched);
                }
            });

            // Ensure schedule 5 is set to today for demo purposes
            tourScheduleRepository.findById(5L).ifPresent(sched -> {
                if (!today.equals(sched.getDepartureDate())) {
                    sched.setDepartureDate(today);
                    tourScheduleRepository.saveAndFlush(sched);
                }
            });

            // Ensure customer 5 (Ngọc Thị) booking 28 is assigned to Tour Schedule 1 (Đoàn tụ - Huế)
            tourBookingRepository.findById(28L).ifPresent(b -> {
                tourScheduleRepository.findById(1L).ifPresent(sched -> {
                    if (b.getSchedule() == null || !b.getSchedule().getId().equals(1L)) {
                        b.setSchedule(sched);
                        tourBookingRepository.saveAndFlush(b);
                    }
                });
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/dashboard")
    @org.springframework.transaction.annotation.Transactional
    public String dashboard(
            @org.springframework.web.bind.annotation.RequestParam(value = "scanned", required = false) String scanned,
            @org.springframework.web.bind.annotation.RequestParam(value = "scheduleId", required = false) Long scheduleId,
            Principal principal,
            Model model) {
        
        healDatabase();

        model.addAttribute("isLoggedIn", principal != null);
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
        java.time.LocalDate today = java.time.LocalDate.now();
        
        // Find targeted schedule
        com.kawai.models.TourSchedule targetSchedule = null;
        if (scheduleId != null) {
            targetSchedule = tourScheduleRepository.findById(scheduleId).orElse(null);
            if (targetSchedule != null) {
                com.kawai.models.Employee emp = null;
                if (principal != null) {
                    emp = employeeRepository.findByAccountUsername(principal.getName()).orElse(null);
                }
                String loggedInName = (emp != null) ? emp.getFullName() : "NguynNgoc";
                String guideName = getGuideForSchedule(targetSchedule);
                if (!guideName.equalsIgnoreCase(loggedInName)) {
                    return "redirect:/tourguide/tour?error=unauthorized";
                }
            }
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
                            String guideName = getGuideForSchedule(s);
                            return guideName.equalsIgnoreCase(loggedInName);
                        })
                        .findFirst()
                        .orElse(null);
                
                if (targetSchedule == null) {
                    targetSchedule = schedules.stream()
                            .filter(s -> s.getId() != null && s.getId() == 5L)
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

        // [Removed auto-reset logic here. The status will persist across reloads unless manually reset.]
        
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
            model.addAttribute("scheduleId", targetSchedule.getId());
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
                model.addAttribute("scheduleId", sched.getId());
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
        
        healDatabase();
        
        // Fetch all tour schedules from DB
        java.util.List<com.kawai.models.TourSchedule> dbSchedules = tourScheduleRepository.findAll();
        System.out.println("DEBUG: dbSchedules size = " + (dbSchedules != null ? dbSchedules.size() : "null"));
        if (dbSchedules != null) {
            for (com.kawai.models.TourSchedule s : dbSchedules) {
                System.out.println("DEBUG: Schedule ID=" + s.getId() + ", date=" + s.getDepartureDate() + ", tour=" + (s.getTour() != null ? s.getTour().getTourName() : "null"));
            }
        }
        
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
            map.put("guide", getGuideForSchedule(sched));
            
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

    private String getGuideForSchedule(com.kawai.models.TourSchedule sched) {
        if (sched == null) return "NguynNgoc";
        if (sched.getDepartureDate() == null) {
            return getPreferredGuide(sched);
        }
        
        // Find all schedules on the same departure date
        java.util.List<com.kawai.models.TourSchedule> daySchedules = tourScheduleRepository.findByDepartureDate(sched.getDepartureDate());
        if (daySchedules == null || daySchedules.size() <= 1) {
            return getPreferredGuide(sched);
        }
        
        // Sort by ID to ensure deterministic assignment
        daySchedules.sort((s1, s2) -> {
            Long id1 = s1.getId() != null ? s1.getId() : 0L;
            Long id2 = s2.getId() != null ? s2.getId() : 0L;
            return id1.compareTo(id2);
        });
        
        java.util.Set<String> takenGuides = new java.util.HashSet<>();
        java.util.List<String> allGuides = java.util.Arrays.asList("NguynNgoc", "Ngọc Lan", "Hoàng Nam");
        
        String assignedGuide = null;
        
        for (com.kawai.models.TourSchedule s : daySchedules) {
            String pref = getPreferredGuide(s);
            String finalGuide;
            if (!takenGuides.contains(pref)) {
                finalGuide = pref;
            } else {
                // Find a free guide
                finalGuide = null;
                for (String g : allGuides) {
                    if (!takenGuides.contains(g)) {
                        finalGuide = g;
                        break;
                    }
                }
                if (finalGuide == null) {
                    // Fallback if all guides are taken
                    finalGuide = pref;
                }
            }
            takenGuides.add(finalGuide);
            
            if (s.getId() != null && s.getId().equals(sched.getId())) {
                assignedGuide = finalGuide;
            }
        }
        
        return assignedGuide != null ? assignedGuide : getPreferredGuide(sched);
    }

    private String getPreferredGuide(com.kawai.models.TourSchedule sched) {
        if (sched == null) return "NguynNgoc";
        if (scheduleHasSpecialCustomer(sched)) {
            return "NguynNgoc";
        }
        if (sched.getId() != null && sched.getId() == 5L) {
            return "NguynNgoc";
        }
        if (sched.getTour() != null) {
            String tn = sched.getTour().getTourName();
            if (tn.contains("Tinh Túy Đồng Nội") || tn.contains("Tinh túy đồng nội") || tn.contains("đồng nội") || tn.contains("dongnoi")) {
                return "Ngọc Lan";
            } else if (tn.contains("Tĩnh Lặng Liên Hoa") || tn.contains("Tĩnh lặng liên hoa") || tn.contains("tinhlang")) {
                return "Hoàng Nam";
            } else if (tn.contains("Di sản") || tn.contains("di sản") || tn.contains("disan")) {
                return "Ngọc Lan";
            }
        }
        return "NguynNgoc";
    }

    private boolean scheduleHasSpecialCustomer(com.kawai.models.TourSchedule sched) {
        if (sched == null || sched.getId() == null) return false;
        try {
            java.util.List<com.kawai.models.TourBooking> bookings = tourBookingRepository.findBySchedule(sched);
            if (bookings != null) {
                for (com.kawai.models.TourBooking b : bookings) {
                    if (b.getCustomer() != null && "ngocnguyenthuy999@gmail.com".equalsIgnoreCase(b.getCustomer().getEmail())) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
