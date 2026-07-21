package com.kawai.controllers.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kawai.models.*;
import com.kawai.repositories.*;
import com.kawai.services.interfaces.TourBookingService;
import com.kawai.services.interfaces.VnPayService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(TourBookingApiController.class)
@AutoConfigureMockMvc(addFilters = false)
public class TourBookingApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TourBookingService tourBookingService;

    @MockBean
    private TourRepository tourRepository;

    @MockBean
    private TourAttendeeRepository tourAttendeeRepository;

    @MockBean
    private TourScheduleRepository tourScheduleRepository;

    @MockBean
    private CustomerRepository customerRepository;

    @MockBean
    private MembershipTierRepository membershipTierRepository;

    @MockBean
    private RoomRepository roomRepository;

    @MockBean
    private RoomBookingDetailRepository roomBookingDetailRepository;

    @MockBean
    private TourBookingRepository tourBookingRepository;

    @MockBean
    private VnPayService vnPayService;

    // SecurityConfig dependencies
    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private com.kawai.repositories.AuthorizedDeviceRepository authorizedDeviceRepository;

    @MockBean
    private com.kawai.repositories.AccountRepository accountRepository;

    @MockBean
    private com.kawai.services.impl.CustomOAuth2UserService customOAuth2UserService;

    @MockBean
    private com.kawai.services.impl.OAuthAccountService oAuthAccountService;

    @MockBean
    private com.kawai.config.OAuth2SuccessHandler oAuth2SuccessHandler;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testGetTourBookingDetail_ReturnsOk() throws Exception {
        // Arrange
        TourBooking tb = new TourBooking();
        tb.setId(10L);
        tb.setBookingStatus("Confirmed");
        tb.setParticipantCount(2);
        tb.setTourCharge(BigDecimal.valueOf(200000));

        TourSchedule schedule = new TourSchedule();
        schedule.setId(100L);
        schedule.setDepartureDate(LocalDate.of(2026, 7, 2));
        schedule.setDepartureTime(LocalTime.of(8, 0));

        Tour tour = new Tour();
        tour.setTourName("Đoàn tụ - Huế");
        tour.setDuration("7 Giờ");
        tour.setDescription("Tour di sản Huế");
        schedule.setTour(tour);
        tb.setSchedule(schedule);

        Principal principal = () -> "testuser";

        when(tourBookingRepository.findById(10L)).thenReturn(Optional.of(tb));

        // Act & Assert
        mockMvc.perform(get("/api/tour-bookings/10")
                .principal(principal)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.bookingStatus").value("Confirmed"))
                .andExpect(jsonPath("$.tourName").value("Đoàn tụ - Huế"));
    }

    @Test
    public void testGetTourBookingDetail_ReturnsUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/tour-bookings/10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetTourBookingDetail_ReturnsNotFound() throws Exception {
        // Arrange
        Principal principal = () -> "testuser";
        when(tourBookingRepository.findById(10L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/tour-bookings/10")
                .principal(principal)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
