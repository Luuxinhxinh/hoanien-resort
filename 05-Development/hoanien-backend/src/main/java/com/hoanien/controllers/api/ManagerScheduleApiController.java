package com.hoanien.controllers.api;

import com.hoanien.models.Employee;
import com.hoanien.models.StaffSchedule;
import com.hoanien.models.Tour;
import com.hoanien.models.TourSchedule;
import com.hoanien.repositories.EmployeeRepository;
import com.hoanien.repositories.StaffScheduleRepository;
import com.hoanien.repositories.TourRepository;
import com.hoanien.repositories.TourScheduleRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/manager/schedules")
@RequiredArgsConstructor
public class ManagerScheduleApiController {

    private final StaffScheduleRepository staffScheduleRepository;
    private final EmployeeRepository employeeRepository;
    private final TourScheduleRepository tourScheduleRepository;
    private final TourRepository tourRepository;
    private final com.hoanien.repositories.TourBookingRepository tourBookingRepository;
    private final com.hoanien.repositories.TourStaffAssignmentRepository tourStaffAssignmentRepository;
    private final com.hoanien.services.impl.ScheduleGeneratorService scheduleGeneratorService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getSchedulesByDate(
            @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        final LocalDate targetDate = (date != null) ? date : LocalDate.now();

        // 1. Matrix staff schedules
        List<StaffSchedule> schedules = staffScheduleRepository != null ? staffScheduleRepository.findByWorkDate(targetDate)
                : new ArrayList<>();
        if (schedules == null || schedules.isEmpty()) {
            if (scheduleGeneratorService != null && targetDate != null) {
                try {
                    scheduleGeneratorService.generateWeeklySchedule(targetDate, targetDate.plusDays(14));
                    schedules = staffScheduleRepository != null ? staffScheduleRepository.findByWorkDate(targetDate) : new ArrayList<>();
                } catch (Exception e) {
                    System.err.println("[ManagerScheduleApiController] Auto-generate schedule error: " + e.getMessage());
                }
            }
            if (schedules == null) {
                schedules = new ArrayList<>();
            }
        }

        List<StaffScheduleDTO> dtoList = schedules.stream().map(s -> {
            StaffScheduleDTO dto = new StaffScheduleDTO();
            if (s == null)
                return dto;
            dto.setId(s.getId());
            dto.setWorkDate(s.getWorkDate() != null ? s.getWorkDate().toString() : "");
            dto.setStatus(s.getStatus());

            if (s.getShift() != null) {
                dto.setShiftId(s.getShift().getId());
                dto.setShiftName(s.getShift().getShiftName());
                dto.setStartTime(s.getShift().getStartTime() != null ? s.getShift().getStartTime().toString() : "");
                dto.setEndTime(s.getShift().getEndTime() != null ? s.getShift().getEndTime().toString() : "");
            }

            if (s.getEmployee() != null) {
                dto.setEmployeeId(s.getEmployee().getId());
                dto.setFullName(s.getEmployee().getFullName());
                dto.setPhone(s.getEmployee().getPhone());
                dto.setEmail(s.getEmployee().getEmail());
                if (s.getEmployee().getAccount() != null && s.getEmployee().getAccount().getRole() != null) {
                    dto.setRoleName(s.getEmployee().getAccount().getRole().getRoleName());
                } else {
                    dto.setRoleName("UNKNOWN");
                }
            }
            return dto;
        }).collect(Collectors.toList());

        // 2. Fetch REAL TourGuide employees and REAL Tours directly from DB
        List<Employee> allEmployees = employeeRepository != null ? employeeRepository.findAll() : new ArrayList<>();
        if (allEmployees == null)
            allEmployees = new ArrayList<>();
        List<Employee> tourGuides = allEmployees.stream()
                .filter(e -> e != null && e.getAccount() != null && e.getAccount().getRole() != null
                        && "TOURGUIDE".equalsIgnoreCase(e.getAccount().getRole().getRoleName()))
                .collect(Collectors.toList());

        List<Tour> realToursInDb = tourRepository != null ? tourRepository.findAll() : new ArrayList<>();
        if (realToursInDb == null)
            realToursInDb = new ArrayList<>();

        List<TourSchedule> tourSchedulesOnDate = (tourScheduleRepository != null && targetDate != null)
                ? tourScheduleRepository.findByDepartureDate(targetDate)
                : new ArrayList<>();
        if (tourSchedulesOnDate == null)
            tourSchedulesOnDate = new ArrayList<>();

        LocalDate today = LocalDate.now();
        java.time.LocalTime now = java.time.LocalTime.now();

        List<TourGuideScheduleDTO> tourGuideDTOs = new ArrayList<>();
        int guideIdx = 0;
        for (Employee guide : tourGuides) {
            if (guide == null)
                continue;
            TourGuideScheduleDTO dto = new TourGuideScheduleDTO();
            dto.setEmployeeId(guide.getId());
            dto.setName(guide.getFullName());
            dto.setEmail(guide.getEmail());
            dto.setPhone(guide.getPhone() != null && !guide.getPhone().isEmpty() ? guide.getPhone() : "Chưa cập nhật");
            dto.setLevel(guide.getAccount() != null && guide.getAccount().getRole() != null
                    ? guide.getAccount().getRole().getRoleName()
                    : "TOURGUIDE");

            // 1. Kiểm tra TourStaffAssignment thực tế của TourGuide này
            Tour assignedTour = null;
            TourSchedule targetSchedule = null;

            if (tourStaffAssignmentRepository != null) {
                List<com.hoanien.models.TourStaffAssignment> assignments = tourStaffAssignmentRepository.findByEmployeeId(guide.getId());
                if (assignments != null && !assignments.isEmpty()) {
                    com.hoanien.models.TourStaffAssignment matchedAssign = assignments.stream()
                            .filter(a -> a.getSchedule() != null && targetDate != null && targetDate.equals(a.getSchedule().getDepartureDate()))
                            .findFirst()
                            .orElse(assignments.get(0));
                    if (matchedAssign != null && matchedAssign.getSchedule() != null) {
                        targetSchedule = matchedAssign.getSchedule();
                        assignedTour = targetSchedule.getTour();
                    }
                }
            }

            // Fallback nếu chưa có assignment trong DB
            if (assignedTour == null && !realToursInDb.isEmpty()) {
                assignedTour = realToursInDb.get(guideIdx % realToursInDb.size());
            }

            String tourName = assignedTour != null && assignedTour.getTourName() != null ? assignedTour.getTourName()
                    : "Tour Trải Nghiệm Khám Phá";
            Long tourId = assignedTour != null ? assignedTour.getId() : null;

            // Khung giờ khởi hành theo Schedule hoặc ca mặc định
            java.time.LocalTime startTime = (targetSchedule != null && targetSchedule.getDepartureTime() != null)
                    ? targetSchedule.getDepartureTime()
                    : ((guideIdx % 2 == 0) ? java.time.LocalTime.of(8, 0) : java.time.LocalTime.of(13, 30));
            java.time.LocalTime endTime = startTime.plusHours(4);

            // ĐẾM SỐ KHÁCH THỰC TẾ TỪ DATABASE
            int realBookedGuests = 0;
            if (targetSchedule != null) {
                int booked = targetSchedule.getBookedSeats() != null ? targetSchedule.getBookedSeats() : 0;
                List<com.hoanien.models.TourBooking> bookingsForSched = (tourBookingRepository != null && targetSchedule.getId() != null)
                        ? tourBookingRepository.findByScheduleId(targetSchedule.getId())
                        : null;
                int bookingPaxSum = 0;
                if (bookingsForSched != null) {
                    bookingPaxSum = bookingsForSched.stream()
                            .filter(b -> b != null && !"Cancelled".equalsIgnoreCase(b.getBookingStatus()))
                            .mapToInt(b -> b.getParticipantCount() != null ? b.getParticipantCount() : 1)
                            .sum();
                }
                realBookedGuests = Math.max(booked, bookingPaxSum);
            } else {
                for (TourSchedule ts : tourSchedulesOnDate) {
                    if (ts != null && ts.getTour() != null) {
                        if ((tourId != null && tourId.equals(ts.getTour().getId())) ||
                                (ts.getTour().getTourName() != null
                                        && ts.getTour().getTourName().equalsIgnoreCase(tourName))) {
                            int booked = ts.getBookedSeats() != null ? ts.getBookedSeats() : 0;
                            List<com.hoanien.models.TourBooking> bookingsForSched = (tourBookingRepository != null
                                    && ts.getId() != null)
                                            ? tourBookingRepository.findByScheduleId(ts.getId())
                                            : null;
                            int bookingPaxSum = 0;
                            if (bookingsForSched != null) {
                                bookingPaxSum = bookingsForSched.stream()
                                        .filter(b -> b != null && !"Cancelled".equalsIgnoreCase(b.getBookingStatus()))
                                        .mapToInt(b -> b.getParticipantCount() != null ? b.getParticipantCount() : 1)
                                        .sum();
                            }
                            realBookedGuests += Math.max(booked, bookingPaxSum);
                        }
                    }
                }
            }

            dto.setActiveTour(tourName);
            dto.setTime(startTime.toString() + " - " + endTime.toString());
            dto.setGuests(realBookedGuests + " Khách");

            // LOGIC TRẠNG THÁI THEO THỜI GIAN THỰC & SỐ KHÁCH THỰC TẾ
            if (realBookedGuests == 0) {
                dto.setStatus("Chưa có khách đặt");
                dto.setBadgeBg("#F3F4F6");
                dto.setBadgeColor("#6B7280");
            } else if (targetDate != null && targetDate.isBefore(today)) {
                dto.setStatus("Hoàn thành trong ngày");
                dto.setBadgeBg("#D1FAE5");
                dto.setBadgeColor("#059669");
            } else if (targetDate != null && targetDate.isAfter(today)) {
                dto.setStatus("Chuẩn bị Đón Đoàn");
                dto.setBadgeBg("#DBEAFE");
                dto.setBadgeColor("#2563EB");
            } else {
                // TODAY
                if (now.isAfter(java.time.LocalTime.of(19, 0)) || now.isBefore(java.time.LocalTime.of(6, 0))) {
                    dto.setStatus("Đã hoàn thành (Nghỉ đêm)");
                    dto.setBadgeBg("#F3F4F6");
                    dto.setBadgeColor("#4B5563");
                    dto.setTime("Đã kết thúc ca ngày");
                } else if (now.isBefore(startTime)) {
                    dto.setStatus("Chuẩn bị Đón Đoàn");
                    dto.setBadgeBg("#DBEAFE");
                    dto.setBadgeColor("#2563EB");
                } else if (now.isAfter(endTime)) {
                    dto.setStatus("Hoàn thành Ca Tour");
                    dto.setBadgeBg("#D1FAE5");
                    dto.setBadgeColor("#059669");
                } else {
                    dto.setStatus("Đang dẫn Tour");
                    dto.setBadgeBg("#FEF3C7");
                    dto.setBadgeColor("#D97706");
                }
            }

            tourGuideDTOs.add(dto);
            guideIdx++;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("schedules", dtoList);
        result.put("tourGuides", tourGuideDTOs);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/mock-preference")
    public ResponseEntity<Map<String, Object>> submitMockPreference(@RequestBody Map<String, Object> payload) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long employeeId = payload.get("employeeId") != null ? Long.valueOf(payload.get("employeeId").toString())
                    : null;
            String dateStr = (String) payload.get("date");
            LocalDate date = dateStr != null && !dateStr.isEmpty() ? LocalDate.parse(dateStr) : LocalDate.now();

            if (employeeId != null && scheduleGeneratorService != null) {
                scheduleGeneratorService.addMockPreference(employeeId, date);
            }
            response.put("status", "success");
            response.put("message", "Đã đăng ký nghỉ phép và xếp lại lịch thành công!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @Data
    public static class StaffScheduleDTO {
        private Long id;
        private String workDate;
        private String status;

        private Long shiftId;
        private String shiftName;
        private String startTime;
        private String endTime;

        private Long employeeId;
        private String fullName;
        private String phone;
        private String email;
        private String roleName;
    }

    @Data
    public static class TourGuideScheduleDTO {
        private Long employeeId;
        private String name;
        private String email;
        private String phone;
        private String level;
        private String activeTour;
        private String time;
        private String guests;
        private String status;
        private String badgeBg;
        private String badgeColor;
    }
}
