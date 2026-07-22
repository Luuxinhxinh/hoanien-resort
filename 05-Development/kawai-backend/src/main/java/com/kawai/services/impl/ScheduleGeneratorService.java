package com.kawai.services.impl;

import com.kawai.models.Employee;
import com.kawai.models.Shift;
import com.kawai.models.StaffSchedule;
import com.kawai.repositories.EmployeeRepository;
import com.kawai.repositories.ShiftRepository;
import com.kawai.repositories.StaffScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleGeneratorService {

    private final StaffScheduleRepository staffScheduleRepository;
    private final EmployeeRepository employeeRepository;
    private final ShiftRepository shiftRepository;

    private final Map<Long, Set<LocalDate>> mockPreferences = new ConcurrentHashMap<>();

    @Transactional
    public void addMockPreference(Long employeeId, LocalDate date) {
        mockPreferences.computeIfAbsent(employeeId, k -> new HashSet<>()).add(date);
        LocalDate today = LocalDate.now();
        generateWeeklySchedule(today, today.plusDays(14));
    }

    @EventListener(ApplicationReadyEvent.class)
    @org.springframework.core.annotation.Order(1)
    @Transactional
    public void onApplicationReady() {
        LocalDate today = LocalDate.now();
        generateWeeklySchedule(today, today.plusDays(14));
        System.out.println("[ScheduleGenerator] Deterministic schedule generated for the next 14 days.");
    }

    @Transactional
    public void generateWeeklySchedule(LocalDate startDate, LocalDate endDate) {
        // 1. Clear old schedules
        staffScheduleRepository.deleteByWorkDateBetween(startDate, endDate);

        List<Employee> allEmployees = employeeRepository.findAll();
        List<Shift> allShifts = shiftRepository.findAll();

        if (allShifts.isEmpty() || allEmployees.isEmpty()) {
            System.out.println("[ScheduleGenerator] Database not fully populated yet. Skipping schedule generation.");
            return;
        }

        Shift morningShift = getShiftById(allShifts, 1L);
        Shift afternoonShift = getShiftById(allShifts, 2L);
        Shift nightShift = getShiftById(allShifts, 3L);

        if (morningShift == null || afternoonShift == null || nightShift == null) {
            System.out.println("[ScheduleGenerator] Missing required shifts. Skipping.");
            return;
        }

        Map<String, List<Employee>> staffByRole = allEmployees.stream()
                .filter(e -> e.getAccount() != null && e.getAccount().getRole() != null)
                .collect(Collectors.groupingBy(e -> e.getAccount().getRole().getRoleName()));

        List<StaffSchedule> newSchedules = new ArrayList<>();

        // Definition of quotas: Map<RoleName, Map<Shift, Integer>>
        Map<String, Map<Shift, Integer>> quotas = new HashMap<>();

        // RECEPTIONIST: Sáng 3, Chiều 3, Đêm 2
        quotas.put("RECEPTIONIST", Map.of(morningShift, 3, afternoonShift, 3, nightShift, 2));

        // HOUSEKEEPING: Sáng 6, Chiều 6, Đêm 3
        quotas.put("HOUSEKEEPING", Map.of(morningShift, 6, afternoonShift, 6, nightShift, 3));

        // F&B POS: Sáng 4, Chiều 4, Đêm 2
        quotas.put("F&B POS", Map.of(morningShift, 4, afternoonShift, 4, nightShift, 2));

        // F&B KITCHEN: Sáng 3, Chiều 3, Đêm 1
        quotas.put("F&B KITCHEN", Map.of(morningShift, 3, afternoonShift, 3, nightShift, 1));

        // MAINTAINER: Sáng 2, Chiều 1, Đêm 1
        quotas.put("MAINTAINER", Map.of(morningShift, 2, afternoonShift, 1, nightShift, 1));

        // TOURGUIDE: Sáng 2, Chiều 2, Đêm 1
        quotas.put("TOURGUIDE", Map.of(morningShift, 2, afternoonShift, 2, nightShift, 1));

        // MANAGER / ADMIN: Sáng 1, Chiều 1, Đêm 1
        quotas.put("MANAGER", Map.of(morningShift, 1, afternoonShift, 1, nightShift, 1));
        quotas.put("ADMIN", Map.of(morningShift, 1, afternoonShift, 1, nightShift, 1));

        // State maps for CSP constraints
        Map<Employee, Integer> weeklyShiftCount = new HashMap<>(); // Track max 6 shifts/week
        Map<Employee, Integer> nightShiftCount = new HashMap<>(); // Track night shifts for fairness
        Map<Employee, Boolean> workedNightYesterday = new HashMap<>(); // Track rest >= 12h

        int currentWeekNum = -1;

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {

            // Reset weekly counts on Monday
            int weekNum = date.get(ChronoField.ALIGNED_WEEK_OF_YEAR);
            if (weekNum != currentWeekNum) {
                weeklyShiftCount.clear();
                currentWeekNum = weekNum;
            }

            Map<Employee, Boolean> workedNightToday = new HashMap<>();
            final Map<Employee, Boolean> finalWorkedNightYesterday = workedNightYesterday;

            for (Map.Entry<String, List<Employee>> entry : staffByRole.entrySet()) {
                String roleName = entry.getKey();
                List<Employee> allRoleStaff = new ArrayList<>(entry.getValue());

                Map<Shift, Integer> roleQuota = quotas.getOrDefault(roleName, new HashMap<>());

                // Sort shifts: Night first to allocate them fairly, then Morning, then
                // Afternoon
                List<Shift> shiftOrder = Arrays.asList(nightShift, morningShift, afternoonShift);

                Map<Employee, Boolean> assignedToday = new HashMap<>();

                for (Shift shift : shiftOrder) {
                    if (shift == null)
                        continue;
                    int required = roleQuota.getOrDefault(shift, 0);
                    if (required == 0)
                        continue;

                    // Apply constraints and sort (Greedy/CSP logic)
                    LocalDate finalDate = date;
                    List<Employee> candidates = allRoleStaff.stream()
                            .filter(e -> !assignedToday.getOrDefault(e, false)) // Không trùng lịch 1 ngày
                            .filter(e -> weeklyShiftCount.getOrDefault(e, 0) < 6) // Max 48h (6 ca)
                            .filter(e -> !(shift.equals(morningShift)
                                    && finalWorkedNightYesterday.getOrDefault(e, false))) // Nghỉ >= 12h
                            .filter(e -> !isMockPersonalPreferenceRestricted(e, finalDate, shift)) // Nguyện vọng cá
                                                                                                   // nhân
                            .sorted(Comparator.comparingInt((Employee e) -> {
                                // Tính công bằng: chia đều ca đêm
                                if (shift.equals(nightShift)) {
                                    return nightShiftCount.getOrDefault(e, 0);
                                }
                                // Các ca khác: deterministic sort
                                return (int) ((e.getId() + finalDate.toEpochDay()) % allRoleStaff.size());
                            }))
                            .collect(Collectors.toList());

                    int assigned = 0;
                    for (Employee selected : candidates) {
                        if (assigned >= required)
                            break;

                        StaffSchedule schedule = new StaffSchedule();
                        schedule.setEmployee(selected);
                        schedule.setShift(shift);
                        schedule.setWorkDate(date);
                        schedule.setStatus("Published");
                        newSchedules.add(schedule);

                        assignedToday.put(selected, true);
                        weeklyShiftCount.put(selected, weeklyShiftCount.getOrDefault(selected, 0) + 1);

                        if (shift.equals(nightShift)) {
                            nightShiftCount.put(selected, nightShiftCount.getOrDefault(selected, 0) + 1);
                            workedNightToday.put(selected, true);
                        }

                        assigned++;
                    }
                }
            }
            workedNightYesterday = workedNightToday;
        }

        System.out.println("[ScheduleGenerator] Saving new schedules: " + newSchedules.size());
        staffScheduleRepository.saveAll(newSchedules);
    }

    private Shift getShiftById(List<Shift> shifts, Long id) {
        return shifts.stream().filter(s -> s.getId().equals(id)).findFirst().orElse(null);
    }

    private boolean isMockPersonalPreferenceRestricted(Employee e, LocalDate date, Shift shift) {
        Set<LocalDate> dates = mockPreferences.get(e.getId());
        if (dates != null && dates.contains(date)) {
            return true;
        }
        return false;
    }
}
