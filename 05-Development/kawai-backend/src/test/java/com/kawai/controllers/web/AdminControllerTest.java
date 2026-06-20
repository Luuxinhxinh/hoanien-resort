package com.kawai.controllers.web;

import com.kawai.models.Account;
import com.kawai.models.Employee;
import com.kawai.models.Role;
import com.kawai.repositories.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeRepository employeeRepository;

    @MockBean
    private CustomerRepository customerRepository;

    @MockBean
    private RoleRepository roleRepository;

    @MockBean
    private RoomCategoryRepository roomCategoryRepository;

    @MockBean
    private RoomRepository roomRepository;

    @MockBean
    private FoodItemRepository foodItemRepository;

    @MockBean
    private TourRepository tourRepository;

    @MockBean
    private PromotionRepository promotionRepository;

    @MockBean
    private DailyRateRepository dailyRateRepository;

    @MockBean
    private BookingRepository bookingRepository;

    @MockBean
    private AuditLogRepository auditLogRepository;

    @MockBean
    private ReviewRepository reviewRepository;

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

    @Test
    @WithMockUser(username="admin", roles={"ADMIN"})
    public void testMasterDataAccountManagementReturnsDbData() throws Exception {
        // Arrange
        Role role = new Role();
        role.setRoleName("Tourguide");

        Account account = new Account();
        account.setId(1L);
        account.setUsername("testuser");
        account.setIsActive(true);
        account.setRole(role);

        Employee employee = new Employee();
        employee.setId(1L);
        employee.setAccount(account);
        employee.setFullName("Lê Văn Test");
        employee.setEmail("test@kawai.vn");
        employee.setGender("MALE");
        employee.setCccd("123456789");
        employee.setPhone("0987654321");
        employee.setSalary(BigDecimal.valueOf(10000000));

        when(employeeRepository.findAll()).thenReturn(List.of(employee));

        // Act & Assert
        mockMvc.perform(get("/admin/master-data").param("tab", "Account Management"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/master-data"))
                .andExpect(model().attributeExists("rows"))
                .andDo(result -> {
                    List<Map<String, String>> rows = (List<Map<String, String>>) result.getModelAndView().getModel().get("rows");
                    // Kiểm tra xem dữ liệu trả về có lấy từ Mock DB hay không
                    // Hiện tại code cũ đang dùng dữ liệu cứng ("Linh Nguyễn", "Minh Trần",...) nên sẽ KHÔNG có "Lê Văn Test"
                    boolean found = rows.stream().anyMatch(row -> "Lê Văn Test".equals(row.get("name")));
                    assertTrue(found, "Dữ liệu trả về phải chứa Nhân viên lấy từ Database, nhưng hiện tại chỉ trả về dữ liệu mẫu (mock)!");
                });
    }
}
