package com.kawai.controllers.web;

import com.kawai.models.Customer;
import com.kawai.repositories.*;
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
    private com.kawai.repositories.AuthorizedDeviceRepository authorizedDeviceRepository;

    @MockBean
    private com.kawai.services.impl.CustomOAuth2UserService customOAuth2UserService;

    @MockBean
    private com.kawai.services.impl.OAuthAccountService oAuthAccountService;

    @MockBean
    private com.kawai.config.OAuth2SuccessHandler oAuth2SuccessHandler;

    @MockBean
    private com.kawai.repositories.PaymentTransactionRepository paymentTransactionRepository;

    @MockBean
    private com.kawai.repositories.FolioItemRepository folioItemRepository;

    @MockBean
    private com.kawai.repositories.RoomGuestRepository roomGuestRepository;


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
        
        com.kawai.models.MembershipTier tier = new com.kawai.models.MembershipTier();
        tier.setTierName("REGULAR");
        customer.setMembershipTier(tier);

        when(customerRepository.findByAccount_Username("hoangnam")).thenReturn(Optional.of(customer));
        when(roomBookingRepository.findByCustomerOrderByIdDesc(any())).thenReturn(java.util.Collections.emptyList());
        when(roomBookingDetailRepository.findByCustomer(any())).thenReturn(java.util.Collections.emptyList());
        when(tourBookingRepository.findByCustomer(any())).thenReturn(java.util.Collections.emptyList());
        when(foodOrderRepository.findByCustomer(any())).thenReturn(java.util.Collections.emptyList());
        when(dependentRepository.findByCustomer(any())).thenReturn(java.util.Collections.emptyList());
        when(tableReservationRepository.findByCustomerOrderByIdDesc(any())).thenReturn(java.util.Collections.emptyList());

        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("guest/profile"));
    }
}

