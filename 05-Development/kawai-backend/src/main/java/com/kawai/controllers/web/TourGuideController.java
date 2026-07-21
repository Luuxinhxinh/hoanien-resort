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
    private com.kawai.services.interfaces.ShiftService shiftService;

    @org.springframework.beans.factory.annotation.Autowired
    private com.kawai.repositories.AccountRepository accountRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private com.kawai.repositories.CustomerRepository customerRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private com.kawai.repositories.TourBookingRepository tourBookingRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private com.kawai.repositories.TourItineraryDetailRepository tourItineraryDetailRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private com.kawai.repositories.ReviewRepository reviewRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private com.kawai.repositories.HotelOperationRepository hotelOperationRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private com.kawai.services.interfaces.EmailService emailService;

    @org.springframework.beans.factory.annotation.Autowired
    private com.kawai.services.interfaces.SystemNotificationService systemNotificationService;

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
            Model model,
            jakarta.servlet.http.HttpSession session) {
        
        model.addAttribute("isLoggedIn", principal != null);
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
        java.time.LocalDate today = java.time.LocalDate.now();

        // Quản lý activeScheduleId trong session để tránh mất tour đang thao tác khi mất param
        if (scheduleId != null) {
            session.setAttribute("activeScheduleId", scheduleId);
        } else {
            Long sessionScheduleId = (Long) session.getAttribute("activeScheduleId");
            if (sessionScheduleId != null) {
                scheduleId = sessionScheduleId;
            }
        }
        
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
                
                if (targetSchedule != null) {
                    session.setAttribute("activeScheduleId", targetSchedule.getId());
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
        long absentCount = attendees.stream()
                .filter(a -> "Absent".equals(a.getStatus()))
                .count();
        model.addAttribute("checkedInCount", checkedIn);
        model.addAttribute("absentCount", absentCount);
        model.addAttribute("totalAttendees", attendees.size());

        // Tổng hợp và parse ghi chú của các đặt tour cho hành trình này
        String parsedBookingNotes = "";
        if (!attendees.isEmpty()) {
            for (com.kawai.models.TourAttendee attendee : attendees) {
                com.kawai.models.TourBooking tb = attendee.getTourBooking();
                if (tb != null && tb.getNotes() != null && !tb.getNotes().isBlank()) {
                    String cleanNote = parseCustomerNotes(tb.getNotes());
                    if (cleanNote != null && !cleanNote.isBlank() && !cleanNote.equalsIgnoreCase("null")) {
                        if (parsedBookingNotes.isEmpty()) {
                            parsedBookingNotes = cleanNote;
                        } else if (!parsedBookingNotes.contains(cleanNote)) {
                            parsedBookingNotes += " | " + cleanNote;
                        }
                    }
                }
            }
        }
        model.addAttribute("tourBookingNotes", parsedBookingNotes);

        // Load detailed activities and handbook for the active tour schedule dynamically
        com.kawai.models.Tour currentTour = null;
        if (targetSchedule != null) {
            currentTour = targetSchedule.getTour();
        } else if (!attendees.isEmpty()) {
            com.kawai.models.TourBooking tb = attendees.get(0).getTourBooking();
            if (tb != null && tb.getSchedule() != null) {
                currentTour = tb.getSchedule().getTour();
            }
        }

        java.util.List<java.util.Map<String, Object>> actList = new java.util.ArrayList<>();
        java.util.Map<String, Object> hbMap = new java.util.HashMap<>();
        String tourTypeKey = "doantu";

        if (currentTour != null) {
            tourTypeKey = currentTour.getTourType();
            hbMap.put("spec", currentTour.getHandbookSpec());

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Object logisticsObj = null;
            Object explanationsObj = null;
            try {
                if (currentTour.getHandbookLogistics() != null) {
                    logisticsObj = mapper.readValue(currentTour.getHandbookLogistics(), Object.class);
                }
                if (currentTour.getHandbookExplanations() != null) {
                    explanationsObj = mapper.readValue(currentTour.getHandbookExplanations(), Object.class);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            hbMap.put("logistics", logisticsObj);
            hbMap.put("explanations", explanationsObj);

            java.util.List<com.kawai.models.TourItineraryDetail> details = tourItineraryDetailRepository.findByItineraryTourId(currentTour.getId());
            for (com.kawai.models.TourItineraryDetail d : details) {
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                String timeStr = "";
                if (d.getStartTime() != null) {
                    timeStr = d.getStartTime().toString().substring(0, 5);
                    if (d.getEndTime() != null) {
                        timeStr += " - " + d.getEndTime().toString().substring(0, 5);
                    }
                }
                map.put("time", timeStr);
                map.put("title", d.getActivityTitle());
                map.put("desc", d.getActivityDescription());
                actList.add(map);
            }
        } else {
            hbMap.put("spec", "");
            hbMap.put("logistics", new java.util.ArrayList<>());
            hbMap.put("explanations", new java.util.ArrayList<>());
        }

        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            model.addAttribute("tourActivitiesJson", mapper.writeValueAsString(actList));
            model.addAttribute("tourHandbookJson", mapper.writeValueAsString(hbMap));
        } catch (Exception e) {
            model.addAttribute("tourActivitiesJson", "[]");
            model.addAttribute("tourHandbookJson", "{}");
            e.printStackTrace();
        }
        model.addAttribute("tourType", tourTypeKey);
        model.addAttribute("dynamicNotifications", getDynamicNotifications());

        return "tour/FaceID";
    }

    @GetMapping("/tour")
    public String tour(Principal principal, Model model) {
        model.addAttribute("isLoggedIn", principal != null);
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
        
        
        
        // Fetch all tour schedules from DB
        java.util.List<com.kawai.models.TourSchedule> dbSchedules = tourScheduleRepository.findAll();
        System.out.println("DEBUG: dbSchedules size = " + (dbSchedules != null ? dbSchedules.size() : "null"));
        if (dbSchedules != null) {
            for (com.kawai.models.TourSchedule s : dbSchedules) {
                System.out.println("DEBUG: Schedule ID=" + s.getId() + ", date=" + s.getDepartureDate() + ", tour=" + (s.getTour() != null ? s.getTour().getTourName() : "null"));
            }
        }
        
        // Sắp xếp các tour đặt thành công mới nhất lên đầu danh sách (ID lớn nhất lên trước)
        if (dbSchedules != null) {
            dbSchedules.sort((s1, s2) -> {
                Long id1 = s1.getId() != null ? s1.getId() : 0L;
                Long id2 = s2.getId() != null ? s2.getId() : 0L;
                return id2.compareTo(id1);
            });

            // Lọc danh sách schedule:
            // - Chỉ giữ lại tối đa 3 tour cố định (ID < 100) của các hướng dẫn viên khác để demo.
            // - Giữ lại tất cả các tour mới (ID >= 100).
            java.util.List<com.kawai.models.TourSchedule> filteredSchedules = new java.util.ArrayList<>();
            int demoCount = 0;
            for (com.kawai.models.TourSchedule s : dbSchedules) {
                if (s.getId() != null && s.getId() < 100L) {
                    if (demoCount < 3) {
                        filteredSchedules.add(s);
                        demoCount++;
                    }
                } else {
                    filteredSchedules.add(s);
                }
            }
            dbSchedules = filteredSchedules;
        }

        java.util.List<java.util.Map<String, Object>> dbToursList = new java.util.ArrayList<>();
        
        for (com.kawai.models.TourSchedule sched : dbSchedules) {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", sched.getId());
            map.put("name", sched.getTour() != null ? sched.getTour().getTourName() : "Chưa xác định");
            
            String timeStr = "08:00 - 12:00";
            if (sched.getDepartureTime() != null) {
                java.time.LocalTime depTime = sched.getDepartureTime();
                java.time.LocalTime endTime = depTime.plusHours(4);
                timeStr = depTime.toString().substring(0, 5) + " - " + endTime.toString().substring(0, 5);
            }
            map.put("time", timeStr);
            map.put("guide", getGuideForSchedule(sched));
            
            String dbStatus = sched.getScheduleStatus();
            String mappedStatus;
            java.time.LocalDate today2 = java.time.LocalDate.now();
            java.time.LocalDate depDate2 = sched.getDepartureDate();

            if (dbStatus != null && ("completed".equalsIgnoreCase(dbStatus) || "finish".equalsIgnoreCase(dbStatus)
                    || "done".equalsIgnoreCase(dbStatus))) {
                mappedStatus = "completed";
            } else if (dbStatus != null && "cancelled".equalsIgnoreCase(dbStatus)) {
                mappedStatus = "cancelled";
            } else if (dbStatus != null && "ongoing".equalsIgnoreCase(dbStatus)) {
                mappedStatus = "ongoing";
            } else {
                if (depDate2 != null && depDate2.isBefore(today2)) {
                    mappedStatus = "completed";
                } else {
                    mappedStatus = "upcoming";
                }
            }
            map.put("status", mappedStatus);
            map.put("date", sched.getDepartureDate() != null ? sched.getDepartureDate().toString() : "2026-06-27");
            
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
            map.put("type", "");
            
            int count = tourAttendeeRepository.findByTourBooking_Schedule_Id(sched.getId()).size();
            map.put("guests", count + " khách");
            
            dbToursList.add(map);
        }
        
        model.addAttribute("dbTours", dbToursList);

        java.util.List<java.util.Map<String, Object>> dynamicNotifications = getDynamicNotifications();
        java.util.List<java.util.Map<String, Object>> dynamicNotes = new java.util.ArrayList<>();
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.format.DateTimeFormatter dateFormatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (com.kawai.models.TourSchedule s : dbSchedules) {
            if (s.getTour() == null) continue;
            String tourName = s.getTour().getTourName();
            java.time.LocalDate depDate = s.getDepartureDate();
            String formattedDepDate = depDate != null ? depDate.format(dateFormatter) : today.format(dateFormatter);
            
            java.util.List<com.kawai.models.TourBooking> bookings = tourBookingRepository.findBySchedule(s);
            if (bookings != null) {
                for (com.kawai.models.TourBooking b : bookings) {
                    if ("Confirmed".equalsIgnoreCase(b.getBookingStatus()) || 
                        "Checked_In".equalsIgnoreCase(b.getBookingStatus()) || 
                        "Completed".equalsIgnoreCase(b.getBookingStatus())) {
                        
                        String custName = b.getCustomer() != null ? b.getCustomer().getFullName() : "Khách hàng";
                        
                        if (b.getNotes() != null) {
                            String parsedNote = parseCustomerNotes(b.getNotes());
                            if (!parsedNote.isEmpty()) {
                                String noteContent = "Khách hàng " + custName + " (Tour \"" + tourName + "\") ghi chú: \"" + parsedNote + "\"";
                                if (depDate != null && depDate.equals(today)) {
                                    java.util.Map<String, Object> todayNote = new java.util.HashMap<>();
                                    todayNote.put("content", noteContent);
                                    todayNote.put("icon", "info");
                                    dynamicNotes.add(todayNote);
                                }
                            }
                        }
                        
                        java.util.List<com.kawai.models.TourAttendee> attendees = tourAttendeeRepository.findByTourBookingId(b.getId());
                        if (attendees != null) {
                            java.util.List<String> childNames = new java.util.ArrayList<>();
                            for (com.kawai.models.TourAttendee att : attendees) {
                                if (att.getDependent() != null) {
                                    com.kawai.models.Dependent dep = att.getDependent();
                                    boolean isChild = false;
                                    if (dep.getBirthDate() != null) {
                                        int age = java.time.Period.between(dep.getBirthDate(), java.time.LocalDate.now()).getYears();
                                        if (age < 12) {
                                            isChild = true;
                                        }
                                    }
                                    if (dep.getCccdPassportEncrypted() != null && dep.getCccdPassportEncrypted().contains("AUTO_CHILD")) {
                                        isChild = true;
                                    }
                                    if (isChild) {
                                        childNames.add(dep.getDependentName());
                                    }
                                }
                            }
                            if (!childNames.isEmpty()) {
                                String childrenStr = String.join(", ", childNames);
                                String childContent = "Tour \"" + tourName + "\" ngày " + formattedDepDate + " có trẻ em tham gia (" + childrenStr + ").";
                                if (depDate != null && depDate.equals(today)) {
                                    java.util.Map<String, Object> todayChildNote = new java.util.HashMap<>();
                                    todayChildNote.put("content", childContent + " Vui lòng lưu ý và chuẩn bị các biện pháp an toàn phù hợp.");
                                    todayChildNote.put("icon", "child_care");
                                    dynamicNotes.add(todayChildNote);
                                }
                            }
                        }
                    }
                }
            }
        }

        if (dynamicNotes.isEmpty()) {
            java.util.Map<String, Object> fallbackNote = new java.util.HashMap<>();
            fallbackNote.put("content", "Hôm nay không có ghi chú đặc biệt nào từ các đoàn khách.");
            fallbackNote.put("icon", "info");
            dynamicNotes.add(fallbackNote);
        }

        model.addAttribute("dynamicNotifications", dynamicNotifications);
        model.addAttribute("dynamicNotes", dynamicNotes);

        return "tour/Tour";
    }

    @GetMapping("/feedback")
    public String feedback(Principal principal, Model model) {
        model.addAttribute("isLoggedIn", principal != null);
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }

        java.util.List<com.kawai.models.Review> tourReviews = new java.util.ArrayList<>();

        // Query real reviews from DB for the logged-in tour guide
        if (principal != null) {
            com.kawai.models.Employee guide = employeeRepository.findByAccountUsername(principal.getName()).orElse(null);
            if (guide != null) {
                try {
                    tourReviews = reviewRepository.findApprovedTourReviewsByGuideId(guide.getId());
                } catch (Exception e) {
                    // Fallback: lay tat ca approved tour reviews neu query fail
                    tourReviews = reviewRepository.findApprovedTourReviews();
                }
            }
        }

        int total = tourReviews.size();
        double avg = 0.0;
        int satisfactionCount = 0;
        int negativeCount = 0;

        if (total > 0) {
            double sum = 0.0;
            for (com.kawai.models.Review r : tourReviews) {
                int rating = r.getRatingTour() != null ? r.getRatingTour() : 5;
                sum += rating;
                if (rating >= 4) {
                    satisfactionCount++;
                }
                if (rating <= 2) {
                    negativeCount++;
                }
            }
            avg = sum / total;
        } else {
            avg = 5.0;
        }

        int satisfactionRate = total > 0 ? (int) Math.round((double) satisfactionCount / total * 100) : 100;

        model.addAttribute("tourReviews", tourReviews);
        model.addAttribute("totalReviews", total);
        model.addAttribute("averageRating", String.format(java.util.Locale.US, "%.1f", avg));
        model.addAttribute("satisfactionRate", satisfactionRate + "%");
        model.addAttribute("negativeReviewsCount", negativeCount);
        model.addAttribute("dynamicNotifications", getDynamicNotifications());

        return "tour/Feedback";
    }

    @org.springframework.web.bind.annotation.PostMapping("/feedback/reply")
    public String replyFeedback(
            java.security.Principal principal,
            @org.springframework.web.bind.annotation.RequestParam("reviewId") Long reviewId,
            @org.springframework.web.bind.annotation.RequestParam("replyText") String replyText,
            jakarta.servlet.http.HttpServletRequest request) {
        
        com.kawai.models.Employee loggedInEmployee = null;
        if (principal != null) {
            loggedInEmployee = employeeRepository.findByAccountUsername(principal.getName()).orElse(null);
        }

        // Chỉ lưu reply khi reviewId dương (review trong DB)
        if (reviewId != null && reviewId > 0) {
            final com.kawai.models.Employee replier = loggedInEmployee;
            reviewRepository.findById(reviewId).ifPresent(review -> {
                review.setReplyText(replyText);
                review.setRepliedBy(replier);
                reviewRepository.saveAndFlush(review);
                
                // Gửi email thông báo cho khách hàng khi có phản hồi
                if (emailService != null && review.getCustomer() != null) {
                    emailService.sendFeedbackReplyEmail(review, review.getCustomer());
                }
            });
        }
        
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank()) {
            String cleanUrl = referer.replaceAll("[&?]toast=[^&]*", "");
            String separator = cleanUrl.contains("?") ? "&" : "?";
            return "redirect:" + cleanUrl + separator + "toast=reply_success";
        }
        return "redirect:/tourguide/feedback?toast=reply_success";
    }

    @org.springframework.web.bind.annotation.PostMapping("/feedback/report")
    public String reportFeedback(
            @org.springframework.web.bind.annotation.RequestParam("reviewId") Long reviewId,
            @org.springframework.web.bind.annotation.RequestParam("reportReason") String reportReason,
            jakarta.servlet.http.HttpServletRequest request) {
        
        // Chỉ lưu report khi reviewId dương
        if (reviewId != null && reviewId > 0) {
            reviewRepository.findById(reviewId).ifPresent(review -> {
                review.setIsReported(true);
                review.setReportReason(reportReason);
                reviewRepository.saveAndFlush(review);
            });
        }
        
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank()) {
            String cleanUrl = referer.replaceAll("[&?]toast=[^&]*", "");
            String separator = cleanUrl.contains("?") ? "&" : "?";
            return "redirect:" + cleanUrl + separator + "toast=report_success";
        }
        return "redirect:/tourguide/feedback?toast=report_success";
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
        if (sched == null) return "Nguyễn Ngọc";
        if (sched.getDepartureDate() == null) {
            return getPreferredGuide(sched);
        }
        
        // Try to assign using the shift service first (new logic from dev)
        if (shiftService != null) {
            try {
                com.kawai.models.Employee assigned = shiftService.assignGuideToTour(sched);
                if (assigned != null && assigned.getFullName() != null) {
                    return assigned.getFullName();
                }
            } catch (Exception e) {
                // fallback
            }
        }
        
        // Fallback to deterministic guide assignment logic (local feature)
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
        java.util.List<String> allGuides = java.util.Arrays.asList("Nguyễn Ngọc", "Ngọc Lan", "Hoàng Nam");
        
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
        if (sched == null) return "Nguyễn Ngọc";
        if (scheduleHasSpecialCustomer(sched)) {
            return "Nguyễn Ngọc";
        }
        if (sched.getId() != null && sched.getId() == 5L) {
            return "Nguyễn Ngọc";
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
        return "Nguyễn Ngọc";
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

    private String parseCustomerNotes(String rawNotes) {
        if (rawNotes == null) return "";
        rawNotes = rawNotes.trim();
        if (rawNotes.contains("customerNotes=")) {
            String[] parts = rawNotes.split(";");
            for (String part : parts) {
                part = part.trim();
                if (part.startsWith("customerNotes=")) {
                    String val = part.substring("customerNotes=".length()).trim();
                    if (val.startsWith("\"") && val.endsWith("\"") && val.length() > 1) {
                        val = val.substring(1, val.length() - 1);
                    }
                    return val;
                }
            }
        }
        return rawNotes;
    }

    @org.springframework.web.bind.annotation.PostMapping("/start-tour")
    public String startTour(
            @org.springframework.web.bind.annotation.RequestParam("scheduleId") Long scheduleId,
            jakarta.servlet.http.HttpServletRequest request) {

        com.kawai.models.TourSchedule schedule = tourScheduleRepository.findById(scheduleId).orElse(null);
        if (schedule != null && !"completed".equalsIgnoreCase(schedule.getScheduleStatus())) {
            schedule.setScheduleStatus("ongoing");
            schedule.setActualStartTime(java.time.LocalDateTime.now());
            tourScheduleRepository.saveAndFlush(schedule);

            // Load activities for this tour
            java.util.List<com.kawai.models.TourItineraryDetail> activities = new java.util.ArrayList<>();
            if (schedule.getTour() != null) {
                try {
                    activities = tourItineraryDetailRepository.findByItineraryTourId(schedule.getTour().getId());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Send departure notification email to all customers in this schedule
            java.util.List<com.kawai.models.TourBooking> bookings = tourBookingRepository.findBySchedule(schedule);
            if (bookings != null) {
                final java.util.List<com.kawai.models.TourItineraryDetail> actsFinal = activities;
                java.util.Set<Long> sentCustomerIds = new java.util.HashSet<>();
                for (com.kawai.models.TourBooking booking : bookings) {
                    if (booking.getCustomer() != null && booking.getCustomer().getId() != null) {
                        Long custId = booking.getCustomer().getId();
                        if (!sentCustomerIds.contains(custId)) {
                            sentCustomerIds.add(custId);
                            try {
                                emailService.sendTourDepartureEmail(booking, booking.getCustomer(), actsFinal);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }

        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank()) {
            String cleanUrl = referer.replaceAll("[&?]toast=[^&]*", "");
            String separator = cleanUrl.contains("?") ? "&" : "?";
            return "redirect:" + cleanUrl + separator + "toast=start_success";
        }
        return "redirect:/tourguide/dashboard?toast=start_success&scheduleId=" + scheduleId;
    }

    @org.springframework.web.bind.annotation.PostMapping("/finish-tour")
    public String finishTour(
            @org.springframework.web.bind.annotation.RequestParam("scheduleId") Long scheduleId,
            jakarta.servlet.http.HttpServletRequest request) {
        
        com.kawai.models.TourSchedule schedule = tourScheduleRepository.findById(scheduleId).orElse(null);
        if (schedule != null) {
            schedule.setScheduleStatus("completed");
            tourScheduleRepository.saveAndFlush(schedule);

            // Update all bookings on this schedule to Completed
            java.util.List<com.kawai.models.TourBooking> bookings = tourBookingRepository.findBySchedule(schedule);
            if (bookings != null) {
                java.util.Set<Long> sentCustomerIds = new java.util.HashSet<>();
                for (com.kawai.models.TourBooking booking : bookings) {
                    booking.setBookingStatus("Completed");
                    tourBookingRepository.saveAndFlush(booking);

                    // Send email to the customer
                    if (booking.getCustomer() != null && booking.getCustomer().getId() != null) {
                        Long custId = booking.getCustomer().getId();
                        if (!sentCustomerIds.contains(custId)) {
                            sentCustomerIds.add(custId);
                            try {
                                emailService.sendTourFeedbackEmail(booking, booking.getCustomer());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }

        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank()) {
            String cleanUrl = referer.replaceAll("[&?]toast=[^&]*", "");
            String separator = cleanUrl.contains("?") ? "&" : "?";
            return "redirect:" + cleanUrl + separator + "toast=finish_success";
        }
        return "redirect:/tourguide/dashboard?toast=finish_success&scheduleId=" + scheduleId;
    }

    /**
     * Hoàn tác trạng thái tour (dành cho trường hợp ấn nhầm).
     * - ongoing  → Open    : xoá actualStartTime, khôi phục về Chờ Bắt Đầu
     * - completed → ongoing : khôi phục booking status về Confirmed
     */
    @org.springframework.web.bind.annotation.PostMapping("/reset-tour")
    public String resetTour(
            @org.springframework.web.bind.annotation.RequestParam("scheduleId") Long scheduleId,
            @org.springframework.web.bind.annotation.RequestParam("targetStatus") String targetStatus,
            jakarta.servlet.http.HttpServletRequest request) {

        com.kawai.models.TourSchedule schedule = tourScheduleRepository.findById(scheduleId).orElse(null);
        if (schedule != null) {
            String currentStatus = schedule.getScheduleStatus();

            if ("Open".equalsIgnoreCase(targetStatus)
                    && "ongoing".equalsIgnoreCase(currentStatus)) {
                // Hoàn tác bắt đầu: ongoing → Open
                schedule.setScheduleStatus("Open");
                schedule.setActualStartTime(null);
                tourScheduleRepository.saveAndFlush(schedule);

            } else if ("ongoing".equalsIgnoreCase(targetStatus)
                    && "completed".equalsIgnoreCase(currentStatus)) {
                // Hoàn tác hoàn thành: completed → ongoing
                schedule.setScheduleStatus("ongoing");
                tourScheduleRepository.saveAndFlush(schedule);

                // Khôi phục trạng thái đặt chỗ về Confirmed
                java.util.List<com.kawai.models.TourBooking> bookings =
                        tourBookingRepository.findBySchedule(schedule);
                if (bookings != null) {
                    for (com.kawai.models.TourBooking booking : bookings) {
                        booking.setBookingStatus("Confirmed");
                        tourBookingRepository.saveAndFlush(booking);
                    }
                }
            }
        }

        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank()) {
            String cleanUrl = referer.replaceAll("[&?]toast=[^&]*", "");
            String separator = cleanUrl.contains("?") ? "&" : "?";
            return "redirect:" + cleanUrl + separator + "toast=reset_success";
        }
        return "redirect:/tourguide/dashboard?toast=reset_success&scheduleId=" + scheduleId;
    }

    private java.util.List<java.util.Map<String, Object>> getDynamicNotifications() {
        java.util.List<java.util.Map<String, Object>> dynamicNotifications = new java.util.ArrayList<>();
        
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return dynamicNotifications;
        }
        
        String username = auth.getName();
        if (auth instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) {
            org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken oauthToken =
                    (org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) auth;
            username = oauthToken.getPrincipal().getAttribute("email");
        }

        try {
            if (systemNotificationService != null) {
                java.util.List<com.kawai.models.SystemNotification> notifs = systemNotificationService.getUnreadNotifications(username);
                for (com.kawai.models.SystemNotification n : notifs) {
                    java.util.Map<String, Object> map = new java.util.HashMap<>();
                    map.put("title", n.getTitle());
                    map.put("content", n.getMessage());
                    map.put("targetUrl", n.getTargetUrl());
                    
                    String icon = "info";
                    if ("TOUR_CANCELLED".equals(n.getType())) icon = "cancel";
                    if ("TOUR_ASSIGNED".equals(n.getType())) icon = "assignment";
                    map.put("icon", icon);
                    
                    long diffMins = java.time.Duration.between(n.getCreatedAt(), java.time.LocalDateTime.now()).toMinutes();
                    if (diffMins < 60) map.put("timeText", diffMins + " phút trước");
                    else map.put("timeText", (diffMins / 60) + " giờ trước");
                    
                    dynamicNotifications.add(map);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dynamicNotifications;
    }
}
