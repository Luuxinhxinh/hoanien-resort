package com.hoanien.controllers.api;

import com.hoanien.models.*;
import com.hoanien.repositories.*;
import com.hoanien.services.impl.CustomOAuth2UserService;
import com.hoanien.services.impl.OAuthAccountService;
import com.hoanien.services.impl.ScheduleGeneratorService;
import com.hoanien.config.OAuth2SuccessHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ManagerScheduleApiController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ManagerScheduleApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StaffScheduleRepository staffScheduleRepository;

    @MockBean
    private EmployeeRepository employeeRepository;

    @MockBean
    private TourScheduleRepository tourScheduleRepository;

    @MockBean
    private TourRepository tourRepository;

    @MockBean
    private TourBookingRepository tourBookingRepository;

    @MockBean
    private TourStaffAssignmentRepository tourStaffAssignmentRepository;

    @MockBean
    private ScheduleGeneratorService scheduleGeneratorService;

    @MockBean
    private com.hoanien.services.interfaces.ShiftService shiftService;

    @MockBean
    private ShiftRepository shiftRepository;

    // SecurityConfig dependencies
    @MockBean
    private UserDetailsService userDetailsService;
    @MockBean
    private AuthorizedDeviceRepository authorizedDeviceRepository;
    @MockBean
    private AccountRepository accountRepository;
    @MockBean
    private CustomOAuth2UserService customOAuth2UserService;
    @MockBean
    private OAuthAccountService oAuthAccountService;
    @MockBean
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @Test
    public void testGetSchedulesByDate_Success() throws Exception {
        LocalDate today = LocalDate.now();

        // Mock Role
        Role tourGuideRole = new Role();
        tourGuideRole.setId(8L);
        tourGuideRole.setRoleName("TOURGUIDE");

        // Mock Account
        Account guideAccount = new Account();
        guideAccount.setId(13L);
        guideAccount.setUsername("nguyenngoc");
        guideAccount.setRole(tourGuideRole);

        // Mock Employee
        Employee guide = new Employee();
        guide.setId(5L);
        guide.setFullName("Nguyễn Ngọc");
        guide.setEmail("guide@hoanien.vn");
        guide.setPhone("0912000005");
        guide.setAccount(guideAccount);

        // Mock Tour
        Tour tour = new Tour();
        tour.setId(1L);
        tour.setTourName("Hành trình Cố đô");

        // Mock TourSchedule
        TourSchedule tourSchedule = new TourSchedule();
        tourSchedule.setId(1L);
        tourSchedule.setTour(tour);
        tourSchedule.setDepartureDate(today);
        tourSchedule.setDepartureTime(LocalTime.of(8, 0));
        tourSchedule.setBookedSeats(8);

        // Mock TourStaffAssignment
        TourStaffAssignment assignment = new TourStaffAssignment();
        assignment.setId(1L);
        assignment.setSchedule(tourSchedule);
        assignment.setEmployee(guide);
        assignment.setStaffRole("GUIDE");

        // Mock TourBooking
        TourBooking booking = new TourBooking();
        booking.setId(1L);
        booking.setSchedule(tourSchedule);
        booking.setParticipantCount(8);
        booking.setBookingStatus("CONFIRMED");

        when(employeeRepository.findAll()).thenReturn(List.of(guide));
        when(tourRepository.findAll()).thenReturn(List.of(tour));
        when(tourScheduleRepository.findByDepartureDate(any(LocalDate.class))).thenReturn(List.of(tourSchedule));
        when(tourStaffAssignmentRepository.findByEmployeeId(5L)).thenReturn(List.of(assignment));
        when(tourBookingRepository.findByScheduleId(1L)).thenReturn(List.of(booking));
        when(staffScheduleRepository.findByWorkDate(any(LocalDate.class))).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/manager/schedules")
                .param("date", today.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tourGuides[0].name").value("Nguyễn Ngọc"))
                .andExpect(jsonPath("$.tourGuides[0].activeTour").value("Hành trình Cố đô"))
                .andExpect(jsonPath("$.tourGuides[0].guests").value("8 Khách"));
    }
}
