package com.kawai.controllers.api;

import com.kawai.models.StaffSchedule;
import com.kawai.repositories.StaffScheduleRepository;
import com.kawai.services.impl.ScheduleNotificationJob;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import com.kawai.services.impl.ScheduleGeneratorService;
import java.util.Map;

@RestController
@RequestMapping("/api/manager/schedules")
@RequiredArgsConstructor
public class ManagerScheduleApiController {

    private final StaffScheduleRepository staffScheduleRepository;
    private final ScheduleGeneratorService scheduleGeneratorService;

    // The schedule is now generated deterministically on application start.
    @GetMapping
    public ResponseEntity<List<StaffScheduleDTO>> getSchedulesByDate(
            @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        if (date == null) {
            date = LocalDate.now();
        }

        List<StaffSchedule> schedules = staffScheduleRepository.findByWorkDate(date);
        
        List<StaffScheduleDTO> dtoList = schedules.stream().map(s -> {
            StaffScheduleDTO dto = new StaffScheduleDTO();
            dto.setId(s.getId());
            dto.setWorkDate(s.getWorkDate().toString());
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

        return ResponseEntity.ok(dtoList);
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
}
