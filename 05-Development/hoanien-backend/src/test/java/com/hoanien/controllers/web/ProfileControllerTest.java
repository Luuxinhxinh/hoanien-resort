package com.hoanien.controllers.web;

import com.hoanien.models.Customer;
import com.hoanien.repositories.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProfileController.class)
public class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountRepository accountRepository;

    @MockBean
    private CustomerRepository customerRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private RoomBookingRepository roomBookingRepository;

    @MockBean
    private TourBookingRepository tourBookingRepository;

    @MockBean
    private FoodOrderRepository foodOrderRepository;

    @MockBean
    private RoomBookingDetailRepository roomBookingDetailRepository;

    @MockBean
    private DependentRepository dependentRepository;

    @MockBean
    private TableReservationRepository tableReservationRepository;

    // SecurityConfig dependencies
    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @MockBean
    private com.hoanien.repositories.AuthorizedDeviceRepository authorizedDeviceRepository;

    @MockBean
    private com.hoanien.services.impl.CustomOAuth2UserService customOAuth2UserService;

    @MockBean
    private com.hoanien.services.impl.OAuthAccountService oAuthAccountService;

    @MockBean
    private com.hoanien.config.OAuth2SuccessHandler oAuth2SuccessHandler;

    @MockBean
    private com.hoanien.repositories.PaymentTransactionRepository paymentTransactionRepository;

    @MockBean
    private com.hoanien.repositories.FolioItemRepository folioItemRepository;

    @MockBean
    private com.hoanien.repositories.BookingRepository bookingRepository;

    @MockBean
    private com.hoanien.services.interfaces.TourBookingService tourBookingService;

    @MockBean
    private com.hoanien.services.interfaces.EmailService emailService;

    @MockBean
    private com.hoanien.repositories.HousekeepingTaskRepository housekeepingTaskRepo;

    @MockBean
    private com.hoanien.services.impl.FileUploadService fileUploadService;

    @MockBean
    private com.hoanien.repositories.RoomRepository roomRepo;

    @MockBean
    private com.hoanien.repositories.RoomGuestRepository roomGuestRepository;

    @MockBean
    private com.hoanien.repositories.EmployeeRepository employeeRepository;

    @MockBean
    private com.hoanien.services.interfaces.ShiftService shiftService;


    @org.junit.jupiter.api.BeforeEach
    public void setUp() {
        when(shiftService.checkIsOnShift(any(), any())).thenReturn(true);
    }

    @Test
    @WithMockUser(username = "hoangnam")
    public void testGetProfileUpdateRedirectsToProfile() throws Exception {
        mockMvc.perform(get("/profile/update"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"));
    }

    @Test
    @WithMockUser(username = "hoangnam")
    public void testPostProfileUpdateSavesAndRedirects() throws Exception {
        Customer customer = new Customer();
        customer.setFullName("Le Hoang Nam");
        customer.setEmail("nam101@test.com");

        when(customerRepository.findByAccount_Username("hoangnam")).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        mockMvc.perform(post("/profile/update")
                        .with(csrf())
                        .param("fullName", "New Name")
                        .param("email", "nam101@test.com")
                        .param("gender", "Male")
                        .param("phone", "0901234567")
                        .param("cccd", "********"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attribute("success", "Cập nhật thông tin thành công!"));
    }

    @Test
    @WithMockUser(username = "hoangnam")
    public void testGetProfileRendersSuccessfully() throws Exception {
        Customer customer = new Customer();
        customer.setFullName("Le Hoang Nam");
        customer.setEmail("nam101@test.com");
        customer.setGender("MALE");
        customer.setPhone("0901234567");
        customer.setLoyaltyPoints(100);
        
        com.hoanien.models.MembershipTier tier = new com.hoanien.models.MembershipTier();
        tier.setTierName("REGULAR");
        customer.setMembershipTier(tier);

        when(customerRepository.findByAccount_Username("hoangnam")).thenReturn(Optional.of(customer));
        when(roomBookingRepository.findByCustomerOrderByIdDesc(any())).thenReturn(java.util.Collections.emptyList());
        when(roomBookingDetailRepository.findByCustomer(any())).thenReturn(java.util.Collections.emptyList());
        when(tourBookingRepository.findTourBookingsByCustomerId(any())).thenReturn(java.util.Collections.emptyList());
        when(foodOrderRepository.findByCustomer(any())).thenReturn(java.util.Collections.emptyList());
        when(dependentRepository.findByCustomer(any())).thenReturn(java.util.Collections.emptyList());
        when(tableReservationRepository.findByCustomerOrderByIdDesc(any())).thenReturn(java.util.Collections.emptyList());

        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("guest/profile"));
    }

    @Test
    @WithMockUser(username = "hoangnam")
    public void testGetProfileWithFullComplexDataRendersSuccessfully() throws Exception {
        Customer customer = new Customer();
        customer.setId(505L);
        customer.setFullName("Le Hoang Nam");
        customer.setEmail("nam101@test.com");
        customer.setGender("MALE");
        customer.setPhone("0901234567");
        customer.setLoyaltyPoints(1500);
        customer.setCccdPassportEncrypted("001099000505");
        
        com.hoanien.models.MembershipTier tier = new com.hoanien.models.MembershipTier();
        tier.setTierName("SILVER");
        customer.setMembershipTier(tier);

        com.hoanien.models.RoomBooking booking1 = new com.hoanien.models.RoomBooking();
        booking1.setId(505L);
        booking1.setCustomer(customer);
        booking1.setBookingStatus("CHECKED_IN");
        booking1.setBookingDate(java.time.LocalDate.now().minusDays(5));
        booking1.setCheckInDate(java.time.LocalDate.now());
        booking1.setCheckOutDate(java.time.LocalDate.now().plusDays(2));
        booking1.setDepositAmount(new java.math.BigDecimal("1000000.00"));
        booking1.setCreditLimit(new java.math.BigDecimal("5000000.00"));
        booking1.setCancellationDeadline(java.time.LocalDateTime.now().plusDays(1));

        com.hoanien.models.RoomCategory category = new com.hoanien.models.RoomCategory();
        category.setId(1L);
        category.setCategoryName("Deluxe Ocean View");
        category.setCoverImgUrl("https://example.com/img1.jpg,https://example.com/img2.jpg");

        com.hoanien.models.Room room = new com.hoanien.models.Room();
        room.setId(101L);
        room.setRoomNumber("P.201");

        com.hoanien.models.RoomBookingDetail detail1 = new com.hoanien.models.RoomBookingDetail();
        detail1.setId(5051L);
        detail1.setRoomBooking(booking1);
        detail1.setCustomer(customer);
        detail1.setCategory(category);
        detail1.setRoom(room);
        detail1.setSubCreditLimit(new java.math.BigDecimal("2500000.00"));

        com.hoanien.models.FolioItem item1 = new com.hoanien.models.FolioItem();
        item1.setId(10L);
        item1.setDescription("Dịch vụ Spa");
        item1.setAmount(new java.math.BigDecimal("500000.00"));
        item1.setIsSettledSeparately(false);

        com.hoanien.models.Dependent dep1 = new com.hoanien.models.Dependent();
        dep1.setId(1L);
        dep1.setCustomer(customer);
        dep1.setDependentName("Nguyễn Văn A");
        dep1.setGender("Male");
        dep1.setBirthDate(java.time.LocalDate.of(2000, 1, 1));
        dep1.setCccdPassportEncrypted("001099000506");

        com.hoanien.models.FoodOrder foodOrder = new com.hoanien.models.FoodOrder();
        foodOrder.setId(100L);
        foodOrder.setRoomBookingDetail(detail1);
        foodOrder.setOrderStatus("CONFIRMED");
        foodOrder.setOrderTime(java.time.LocalDateTime.now());
        foodOrder.setTotalAmount(new java.math.BigDecimal("150000.00"));

        com.hoanien.models.TableReservation tableRes = new com.hoanien.models.TableReservation();
        tableRes.setId(200L);
        tableRes.setCustomer(customer);
        tableRes.setReserveDate(java.time.LocalDate.now().plusDays(1));
        tableRes.setReserveTime(java.time.LocalTime.of(19, 0));
        tableRes.setPartySize(4);
        tableRes.setStatus("CONFIRMED");

        when(customerRepository.findByAccount_Username("hoangnam")).thenReturn(Optional.of(customer));
        when(roomBookingRepository.findByCustomerOrderByIdDesc(any())).thenReturn(java.util.List.of(booking1));
        when(roomBookingDetailRepository.findByCustomer(any())).thenReturn(java.util.List.of(detail1));
        when(roomBookingRepository.findById(505L)).thenReturn(Optional.of(booking1));
        when(roomBookingDetailRepository.findByRoomBookingId(505L)).thenReturn(java.util.List.of(detail1));
        when(folioItemRepository.findByRoomBookingDetailId(5051L)).thenReturn(java.util.List.of(item1));
        when(roomGuestRepository.findByRoomBookingDetailId(any())).thenReturn(java.util.Collections.emptyList());
        when(tourBookingRepository.findTourBookingsByCustomerId(any())).thenReturn(java.util.Collections.emptyList());
        when(tourBookingRepository.findByRoomBookingId(any())).thenReturn(java.util.Collections.emptyList());
        when(foodOrderRepository.findByCustomer(any())).thenReturn(java.util.List.of(foodOrder));
        when(dependentRepository.findByCustomer(any())).thenReturn(java.util.List.of(dep1));
        when(tableReservationRepository.findByCustomerOrderByIdDesc(any())).thenReturn(java.util.List.of(tableRes));

        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("guest/profile"));
    }

    @Test
    @WithMockUser(username = "hoangnam")
    public void testGetProfileWithNullEdgeCaseTourAndFoodOrdersRendersSuccessfully() throws Exception {
        Customer customer = new Customer();
        customer.setId(506L);
        customer.setFullName("Le Hoang Nam");
        customer.setEmail("nam102@test.com");

        // Tour booking with null bookingStatus, null schedule, null participantCount
        com.hoanien.models.TourBooking nullStatusTourBooking = new com.hoanien.models.TourBooking();
        nullStatusTourBooking.setId(999L);
        nullStatusTourBooking.setCustomer(customer);
        nullStatusTourBooking.setBookingStatus(null);
        nullStatusTourBooking.setSchedule(null);
        nullStatusTourBooking.setParticipantCount(null);

        // Food order with null orderStatus
        com.hoanien.models.FoodOrder nullStatusFoodOrder = new com.hoanien.models.FoodOrder();
        nullStatusFoodOrder.setId(888L);
        nullStatusFoodOrder.setOrderStatus(null);

        when(customerRepository.findByAccount_Username("hoangnam")).thenReturn(Optional.of(customer));
        when(roomBookingRepository.findByCustomerOrderByIdDesc(any())).thenReturn(java.util.Collections.emptyList());
        when(roomBookingDetailRepository.findByCustomer(any())).thenReturn(java.util.Collections.emptyList());
        when(bookingRepository.findByCustomerId(any())).thenReturn(java.util.List.of(nullStatusTourBooking));
        when(tourBookingRepository.findTourBookingsByCustomerId(any())).thenReturn(java.util.List.of(nullStatusTourBooking));
        when(foodOrderRepository.findByCustomer(any())).thenReturn(java.util.List.of(nullStatusFoodOrder));
        when(dependentRepository.findByCustomer(any())).thenReturn(java.util.Collections.emptyList());
        when(tableReservationRepository.findByCustomerOrderByIdDesc(any())).thenReturn(java.util.Collections.emptyList());

        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("guest/profile"));
    }
}

