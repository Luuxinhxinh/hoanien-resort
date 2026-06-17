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
                        .param("phone", "090101")
                        .param("cccd", "********"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attribute("success", "Cập nhật thông tin thành công!"));
    }
}
