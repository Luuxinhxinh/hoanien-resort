package com.kawai.controllers.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kawai.dto.CreateEmployeeDTO;
import com.kawai.models.Employee;
import com.kawai.services.interfaces.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@WebMvcTest(AdminAccountRestController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AdminAccountRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private com.kawai.repositories.RoleRepository roleRepository;

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
    public void testCreateEmployeeAccount_ReturnsCreated() throws Exception {
        // Arrange
        CreateEmployeeDTO dto = new CreateEmployeeDTO();
        dto.setUsername("newtourguide");
        dto.setPassword("pass123");
        dto.setFullName("Lê Trọng Tour");
        dto.setEmail("tour@kawai.vn");
        dto.setRoleId(6L); // Giả sử ID 6 là Tourguide
        dto.setPhone("0999888777");
        dto.setGender("MALE");
        dto.setCccd("001122334455");
        dto.setSalary(BigDecimal.valueOf(15000000));

        Employee savedEmployee = new Employee();
        savedEmployee.setId(10L);

        when(userService.createEmployeeAccount(any(CreateEmployeeDTO.class))).thenReturn(savedEmployee);

        // Act & Assert
        // API hiện tại chưa tồn tại nên sẽ báo lỗi 404 Not Found (Màu Đỏ)
        mockMvc.perform(post("/admin/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    public void testUpdateEmployeeAccount_ReturnsOk() throws Exception {
        // Arrange
        java.util.Map<String, String> payload = new java.util.HashMap<>();
        payload.put("name", "Updated Name");
        payload.put("phone", "0999111222");

        Employee updatedEmployee = new Employee();
        updatedEmployee.setId(1L);
        updatedEmployee.setFullName("Updated Name");

        when(userService.updateEmployeeAccount(org.mockito.ArgumentMatchers.eq(1L), any(java.util.Map.class)))
                .thenReturn(updatedEmployee);

        // Act & Assert
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/admin/api/v1/accounts/E-001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.fullName").value("Updated Name"));
    }

    @Test
    public void testUpdateCustomerAccount_ReturnsOk() throws Exception {
        // Arrange
        java.util.Map<String, String> payload = new java.util.HashMap<>();
        payload.put("name", "Updated Customer");

        com.kawai.models.Customer updatedCustomer = new com.kawai.models.Customer();
        updatedCustomer.setId(2L);
        updatedCustomer.setFullName("Updated Customer");

        when(userService.updateCustomerAccount(org.mockito.ArgumentMatchers.eq(2L), any(java.util.Map.class)))
                .thenReturn(updatedCustomer);

        // Act & Assert
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/admin/api/v1/accounts/C-002")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.fullName").value("Updated Customer"));
    }
}
