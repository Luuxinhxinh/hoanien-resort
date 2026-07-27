package com.hoanien.controllers.web;

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
    private com.hoanien.repositories.TourAttendeeRepository tourAttendeeRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private com.hoanien.repositories.TourScheduleRepository tourScheduleRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private com.hoanien.repositories.TourRepository tourRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private com.hoanien.repositories.EmployeeRepository employeeRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private com.hoanien.services.interfaces.ShiftService shiftService;

    @org.springframework.beans.factory.annotation.Autowired
    private com.hoanien.repositories.AccountRepository accountRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private com.hoanien.repositories.CustomerRepository customerRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private com.hoanien.repositories.TourBookingRepository tourBookingRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private com.hoanien.repositories.TourItineraryDetailRepository tourItineraryDetailRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private com.hoanien.repositories.ReviewRepository reviewRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private com.hoanien.repositories.HotelOperationRepository hotelOperationRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private com.hoanien.services.interfaces.EmailService emailService;

    @org.springframework.beans.factory.annotation.Autowired
    private com.hoanien.services.interfaces.SystemNotificationService systemNotificationService;

    @org.springframework.web.bind.annotation.ModelAttribute
    public void addEmployeeToModel(Principal principal, Model model, jakarta.servlet.http.HttpServletRequest request) {
        if (principal != null) {
            com.hoanien.models.Employee employee = employeeRepository.findByAccountUsername(principal.getName()).orElse(null);
            if (employee != null) {
                if ("Nguyễn Hướng Dẫn".equals(employee.getFullName())) {
                    employee.setFullName("NguynNgoc");
                    employeeRepository.saveAndFlush(employee);
                }
            } else {
                employee = new com.hoanien.models.Employee();
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
            com.hoanien.models.Account account = accountRepository.findByUsername(principal.getName()).orElse(null);
            if (account != null) {
                com.hoanien.models.Employee employee = new com.hoanien.models.Employee();
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
        com.hoanien.models.TourSchedule targetSchedule = null;
        if (scheduleId != null) {
            targetSchedule = tourScheduleRepository.findById(scheduleId).orElse(null);
            if (targetSchedule != null) {
                com.hoanien.models.Employee emp = null;
                if (principal != null) {
                    emp = employeeRepository.findByAccountUsername(principal.getName()).orElse(null);
                }
                String loggedInName = (emp != null) ? emp.getFullName() : "NguynNgoc";
                String guideName = getGuideForSchedule(targetSchedule);

                org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                boolean isAdminOrManager = auth != null && auth.getAuthorities().stream().anyMatch(a ->
                        a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_MANAGER")
                );

                if (!isAdminOrManager && !guideName.equalsIgnoreCase(loggedInName)) {
                    return "redirect:/tourguide/tour?error=unauthorized";
                }
            }
        }
        
        if (targetSchedule == null) {
            // Find default schedule based on logged-in guide profile
            java.util.List<com.hoanien.models.TourSchedule> schedules = tourScheduleRepository.findAll();
            if (!schedules.isEmpty()) {
                com.hoanien.models.Employee emp = null;
                if (principal != null) {
                    emp = employeeRepository.findByAccountUsername(principal.getName()).orElse(null);
                    if (emp == null) {
                        emp = new com.hoanien.models.Employee();
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
        
        java.util.List<com.hoanien.models.TourAttendee> attendees = new java.util.ArrayList<>();
        if (targetSchedule != null) {
            attendees = tourAttendeeRepository.findByTourBooking_Schedule_Id(targetSchedule.getId());
            java.util.List<com.hoanien.models.TourBooking> scheduleBookings = tourBookingRepository.findByScheduleId(targetSchedule.getId());
            boolean hasNewAttendee = false;
            for (com.hoanien.models.TourBooking tb : scheduleBookings) {
                if ("Cancelled".equalsIgnoreCase(tb.getBookingStatus())) continue;
                boolean found = attendees.stream().anyMatch(a -> a.getTourBooking() != null && a.getTourBooking().getId().equals(tb.getId()));
                if (!found) {
                    com.hoanien.models.TourAttendee newAttendee = new com.hoanien.models.TourAttendee();
                    newAttendee.setTourBooking(tb);
                    newAttendee.setCustomer(tb.getCustomer());
                    newAttendee.setStatus("Not_Checked_In");
                    tourAttendeeRepository.saveAndFlush(newAttendee);
                    hasNewAttendee = true;
                }
            }
            if (hasNewAttendee) {
                attendees = tourAttendeeRepository.findByTourBooking_Schedule_Id(targetSchedule.getId());
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
            model.addAttribute("scheduleId", targetSchedule.getId());
            model.addAttribute("maxCapacity", targetSchedule.getTour() != null ? targetSchedule.getTour().getMaxCapacity() : 0);
        } else if (!attendees.isEmpty()) {
            com.hoanien.models.TourBooking tb = attendees.get(0).getTourBooking();
            if (tb != null && tb.getSchedule() != null) {
                com.hoanien.models.TourSchedule sched = tb.getSchedule();
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
            for (com.hoanien.models.TourAttendee attendee : attendees) {
                com.hoanien.models.TourBooking tb = attendee.getTourBooking();
                if (tb != null && tb.getNotes() != null && !tb.getNotes().isBlank()) {
                    String cleanNote = parseCustomerNotes(tb.getNotes());
                    if (cleanNote != null && !cleanNote.isBlank() && !cleanNote.equalsIgnoreCase("null")) {
                        if (parsedBookingNotes.isEmpty()) {
                            parsedBookingNotes = cleanNote;
                        } else if (!parsedBookingNotes.contains(cleanNote)) {
                            parsedBookingNotes += "\n• " + cleanNote;
                        }
                    }
                }
            }
        }
        model.addAttribute("tourBookingNotes", parsedBookingNotes);

        // Load detailed activities and handbook for the active tour schedule dynamically
        com.hoanien.models.Tour currentTour = null;
        if (targetSchedule != null) {
            currentTour = targetSchedule.getTour();
        } else if (!attendees.isEmpty()) {
            com.hoanien.models.TourBooking tb = attendees.get(0).getTourBooking();
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

            java.util.List<com.hoanien.models.TourItineraryDetail> details = tourItineraryDetailRepository.findByItineraryTourId(currentTour.getId());
            for (com.hoanien.models.TourItineraryDetail d : details) {
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
        java.util.List<com.hoanien.models.TourSchedule> dbSchedules = tourScheduleRepository.findAll();
        System.out.println("DEBUG: dbSchedules size = " + (dbSchedules != null ? dbSchedules.size() : "null"));
        if (dbSchedules != null) {
            for (com.hoanien.models.TourSchedule s : dbSchedules) {
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

            // Lọc danh sách schedule: Giữ các chuyến đi có khách HOẶC có đơn đặt/hủy tour
            java.util.List<com.hoanien.models.TourSchedule> filteredSchedules = new java.util.ArrayList<>();
            for (com.hoanien.models.TourSchedule s : dbSchedules) {
                java.util.List<com.hoanien.models.TourBooking> bList = tourBookingRepository.findByScheduleId(s.getId());
                boolean hasBookings = bList != null && !bList.isEmpty();
                if ((s.getBookedSeats() != null && s.getBookedSeats() > 0) || hasBookings) {
                    filteredSchedules.add(s);
                }
            }
            dbSchedules = filteredSchedules;
        }

        java.util.List<java.util.Map<String, Object>> dbToursList = new java.util.ArrayList<>();
        
        for (com.hoanien.models.TourSchedule sched : dbSchedules) {
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
            java.util.List<com.hoanien.models.TourBooking> schedBookings = tourBookingRepository.findByScheduleId(sched.getId());
            boolean isAllCancelled = schedBookings != null && !schedBookings.isEmpty() && schedBookings.stream()
                    .allMatch(b -> b.getBookingStatus() != null && b.getBookingStatus().toLowerCase().contains("cancel"));

            String mappedStatus;
            java.time.LocalDate today2 = java.time.LocalDate.now();
            java.time.LocalDate depDate2 = sched.getDepartureDate();

            if ("cancelled".equalsIgnoreCase(dbStatus) || isAllCancelled) {
                mappedStatus = "cancelled";
            } else if (dbStatus != null && ("completed".equalsIgnoreCase(dbStatus) || "finish".equalsIgnoreCase(dbStatus)
                    || "done".equalsIgnoreCase(dbStatus))) {
                mappedStatus = "completed";
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
            
            String image = "https://res.cloudinary.com/r2g91yqt/image/upload/hoanien_assets/AnhTour/z7930565879475_de7864577660b0726376ea947dbe94de.jpg"; // Default for Đoàn Tụ
            if (sched.getTour() != null) {
                if (sched.getTour().getImageUrl() != null && !sched.getTour().getImageUrl().isBlank()) {
                    image = sched.getTour().getImageUrl();
                } else {
                    String name = sched.getTour().getTourName().toLowerCase();
                    if (name.contains("đồng nội") || name.contains("dongnoi")) {
                        image = "/AnhTour/com.jfif";
                    } else if (name.contains("liên hoa") || name.contains("tháp mười") || name.contains("tinhlang")) {
                        image = "https://res.cloudinary.com/r2g91yqt/image/upload/hoanien_assets/AnhTour/ThapMuoi.jpg";
                    } else if (name.contains("di sản") || name.contains("thủ công") || name.contains("disan")) {
                        image = "https://res.cloudinary.com/r2g91yqt/image/upload/hoanien_assets/AnhTour/ThuCong.jpg";
                    } else if (name.contains("sóng xanh") || name.contains("hạ long") || name.contains("halong")) {
                        image = "https://res.cloudinary.com/r2g91yqt/image/upload/hoanien_assets/AnhTour/1.SongXanh-HaLong-HoangHon.png";
                    } else if (name.contains("sương phủ") || name.contains("sapa") || name.contains("suongphu")) {
                        image = "https://res.cloudinary.com/r2g91yqt/image/upload/hoanien_assets/AnhTour/2.SuongPhu-SaPa-RuongBacThang.png";
                    } else if (name.contains("nhịp đập") || name.contains("cát tiên") || name.contains("cattien") || name.contains("nhipdap")) {
                        image = "https://res.cloudinary.com/r2g91yqt/image/upload/hoanien_assets/AnhTour/3.NhipDap-CatTien-Rung.png";
                    }
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

        for (com.hoanien.models.TourSchedule s : dbSchedules) {
            if (s.getTour() == null) continue;
            String tourName = s.getTour().getTourName();
            java.time.LocalDate depDate = s.getDepartureDate();
            String formattedDepDate = depDate != null ? depDate.format(dateFormatter) : today.format(dateFormatter);
            
            java.util.List<com.hoanien.models.TourBooking> bookings = tourBookingRepository.findBySchedule(s);
            if (bookings != null) {
                for (com.hoanien.models.TourBooking b : bookings) {
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
                        
                        java.util.List<com.hoanien.models.TourAttendee> attendees = tourAttendeeRepository.findByTourBookingId(b.getId());
                        if (attendees != null) {
                            java.util.List<String> childNames = new java.util.ArrayList<>();
                            for (com.hoanien.models.TourAttendee att : attendees) {
                                if (att.getDependent() != null) {
                                    com.hoanien.models.Dependent dep = att.getDependent();
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

        // fallbackNote removed to avoid rendering empty note card

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

        java.util.List<com.hoanien.models.Review> tourReviews = new java.util.ArrayList<>();

        // Query real reviews from DB for the logged-in tour guide
        if (principal != null) {
            com.hoanien.models.Employee guide = employeeRepository.findByAccountUsername(principal.getName()).orElse(null);
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
            for (com.hoanien.models.Review r : tourReviews) {
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
        
        com.hoanien.models.Employee loggedInEmployee = null;
        if (principal != null) {
            loggedInEmployee = employeeRepository.findByAccountUsername(principal.getName()).orElse(null);
        }

        // Chỉ lưu reply khi reviewId dương (review trong DB)
        if (reviewId != null && reviewId > 0) {
            final com.hoanien.models.Employee replier = loggedInEmployee;
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
        com.hoanien.models.Tour tour = tourRepository.findFirstByTourTypeAndIsActiveTrueOrderByIdDesc("doantu").orElse(null);
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
        com.hoanien.models.Tour tour = tourRepository.findFirstByTourTypeAndIsActiveTrueOrderByIdDesc("dongnoi").orElse(null);
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
        com.hoanien.models.Tour tour = tourRepository.findFirstByTourTypeAndIsActiveTrueOrderByIdDesc("disan").orElse(null);
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
        com.hoanien.models.Tour tour = tourRepository.findFirstByTourTypeAndIsActiveTrueOrderByIdDesc("tinhlang").orElse(null);
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
        
        com.hoanien.models.Tour tour = tourRepository.findById(id).orElse(new com.hoanien.models.Tour());
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

        com.hoanien.models.Tour tour = new com.hoanien.models.Tour();
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

    private String getGuideForSchedule(com.hoanien.models.TourSchedule sched) {
        if (sched == null) return "Nguyễn Ngọc";
        if (sched.getDepartureDate() == null) {
            return getPreferredGuide(sched);
        }
        
        // Try to assign using the shift service first (new logic from dev)
        if (shiftService != null) {
            try {
                com.hoanien.models.Employee assigned = shiftService.assignGuideToTour(sched);
                if (assigned != null && assigned.getFullName() != null) {
                    return assigned.getFullName();
                }
            } catch (Exception e) {
                // fallback
            }
        }
        
        // Fallback to deterministic guide assignment logic (local feature)
        java.util.List<com.hoanien.models.TourSchedule> daySchedules = tourScheduleRepository.findByDepartureDate(sched.getDepartureDate());
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
        java.util.List<String> allGuides = java.util.Arrays.asList(
                "NguynNgoc", "Đỗ Thu Bình", "Ngô Minh Lan", "Phan Thanh Hải", 
                "Nguyễn Quỳnh Anh", "Lê Hoàng Long", "Vũ Thu Trang");
        
        String assignedGuide = null;
        
        for (com.hoanien.models.TourSchedule s : daySchedules) {
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

    private String getPreferredGuide(com.hoanien.models.TourSchedule sched) {
        if (sched == null) return "NguynNgoc";
        if (scheduleHasSpecialCustomer(sched)) {
            return "NguynNgoc";
        }
        if (sched.getId() != null && sched.getId() == 5L) {
            return "NguynNgoc";
        }
        if (sched.getTour() != null) {
            String tn = sched.getTour().getTourName().toLowerCase();
            if (tn.contains("đoàn tụ") || tn.contains("doantu")) {
                return "NguynNgoc";
            } else if (tn.contains("đồng nội") || tn.contains("dongnoi")) {
                return "Đỗ Thu Bình";
            } else if (tn.contains("di sản") || tn.contains("thủ công") || tn.contains("disan")) {
                return "Ngô Minh Lan";
            } else if (tn.contains("liên hoa") || tn.contains("tháp mười") || tn.contains("tinhlang")) {
                return "Phan Thanh Hải";
            } else if (tn.contains("sóng xanh") || tn.contains("hạ long") || tn.contains("halong")) {
                return "Nguyễn Quỳnh Anh";
            } else if (tn.contains("sương phủ") || tn.contains("sapa") || tn.contains("suongphu")) {
                return "Lê Hoàng Long";
            } else if (tn.contains("nhịp đập") || tn.contains("cát tiên") || tn.contains("cattien") || tn.contains("nhipdap")) {
                return "Vũ Thu Trang";
            }
        }
        return "NguynNgoc";
    }

    private boolean scheduleHasSpecialCustomer(com.hoanien.models.TourSchedule sched) {
        if (sched == null || sched.getId() == null) return false;
        try {
            java.util.List<com.hoanien.models.TourBooking> bookings = tourBookingRepository.findBySchedule(sched);
            if (bookings != null) {
                for (com.hoanien.models.TourBooking b : bookings) {
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

        com.hoanien.models.TourSchedule schedule = tourScheduleRepository.findById(scheduleId).orElse(null);
        if (schedule != null && !"completed".equalsIgnoreCase(schedule.getScheduleStatus())) {
            schedule.setScheduleStatus("ongoing");
            schedule.setActualStartTime(java.time.LocalDateTime.now());
            tourScheduleRepository.saveAndFlush(schedule);

            // Load activities for this tour
            java.util.List<com.hoanien.models.TourItineraryDetail> activities = new java.util.ArrayList<>();
            if (schedule.getTour() != null) {
                try {
                    activities = tourItineraryDetailRepository.findByItineraryTourId(schedule.getTour().getId());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Send departure notification email to all customers in this schedule
            java.util.List<com.hoanien.models.TourBooking> bookings = tourBookingRepository.findBySchedule(schedule);
            if (bookings != null) {
                final java.util.List<com.hoanien.models.TourItineraryDetail> actsFinal = activities;
                java.util.Set<Long> sentCustomerIds = new java.util.HashSet<>();
                for (com.hoanien.models.TourBooking booking : bookings) {
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
        
        com.hoanien.models.TourSchedule schedule = tourScheduleRepository.findById(scheduleId).orElse(null);
        if (schedule != null) {
            schedule.setScheduleStatus("completed");
            tourScheduleRepository.saveAndFlush(schedule);

            // Update all bookings on this schedule to Completed
            java.util.List<com.hoanien.models.TourBooking> bookings = tourBookingRepository.findBySchedule(schedule);
            if (bookings != null) {
                java.util.Set<Long> sentCustomerIds = new java.util.HashSet<>();
                for (com.hoanien.models.TourBooking booking : bookings) {
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

        com.hoanien.models.TourSchedule schedule = tourScheduleRepository.findById(scheduleId).orElse(null);
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
                java.util.List<com.hoanien.models.TourBooking> bookings =
                        tourBookingRepository.findBySchedule(schedule);
                if (bookings != null) {
                    for (com.hoanien.models.TourBooking booking : bookings) {
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
                java.util.List<com.hoanien.models.SystemNotification> notifs = systemNotificationService.getUnreadNotifications(username);
                for (com.hoanien.models.SystemNotification n : notifs) {
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

    // Static memory store for TourGuide checkpoint states (scheduleId -> List<Boolean>)
    private static final java.util.Map<Long, java.util.List<Boolean>> CHECKPOINT_STATES_STORE = new java.util.concurrent.ConcurrentHashMap<>();

    public static java.util.List<Boolean> getScheduleCheckpointStates(Long scheduleId) {
        if (scheduleId == null) return java.util.Collections.emptyList();
        return CHECKPOINT_STATES_STORE.getOrDefault(scheduleId, java.util.Collections.emptyList());
    }

    @org.springframework.web.bind.annotation.PostMapping("/checkpoints/update")
    @org.springframework.web.bind.annotation.ResponseBody
    public org.springframework.http.ResponseEntity<?> updateCheckpointStates(@org.springframework.web.bind.annotation.RequestBody java.util.Map<String, Object> payload) {
        try {
            if (payload == null || !payload.containsKey("scheduleId") || !payload.containsKey("states")) {
                return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("message", "Invalid payload"));
            }
            Long scheduleId = Long.parseLong(payload.get("scheduleId").toString());
            java.util.List<?> rawList = (java.util.List<?>) payload.get("states");
            java.util.List<Boolean> states = new java.util.ArrayList<>();
            for (Object obj : rawList) {
                states.add(Boolean.TRUE.equals(obj) || "true".equalsIgnoreCase(String.valueOf(obj)));
            }
            CHECKPOINT_STATES_STORE.put(scheduleId, states);
            return org.springframework.http.ResponseEntity.ok(java.util.Map.of("success", true, "scheduleId", scheduleId, "states", states));
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : e.getClass().getName();
            e.printStackTrace(); // Log error to console for debugging
            return org.springframework.http.ResponseEntity.internalServerError().body(java.util.Map.of("message", msg));
        }
    }
}
