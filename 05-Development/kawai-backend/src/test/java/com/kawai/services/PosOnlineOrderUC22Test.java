package com.kawai.services;

import com.kawai.dto.CreateFoodOrderRequest;
import com.kawai.models.Account;
import com.kawai.models.Customer;
import com.kawai.models.FoodOrder;
import com.kawai.models.RoomBooking;
import com.kawai.repositories.AccountRepository;
import com.kawai.repositories.CustomerRepository;
import com.kawai.repositories.EmployeeRepository;
import com.kawai.repositories.FoodOrderRepository;
import com.kawai.repositories.RoomBookingRepository;
import com.kawai.services.impl.PosServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * JUnit Test — UC22: Đặt món trực tuyến (PosService)
 * MODULE 3: POS & Nhà hàng
 * ═══════════════════════════════════════════════════════════════════════════
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UC22 — Đặt món trực tuyến")
class PosOnlineOrderUC22Test {

    @InjectMocks
    private PosServiceImpl posService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private RoomBookingRepository roomBookingRepository;

    @Mock
    private com.kawai.repositories.RoomRepository roomRepository;

    @Mock
    private FoodOrderRepository foodOrderRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Test
    @DisplayName("TC-UC22-01 | Đặt món trực tuyến tự động liên kết với RoomBooking hiện tại của Khách")
    void testCreateOrder_OnlineOrder_LinksToActiveBooking() {
        java.time.LocalTime now = java.time.LocalTime.now();
        org.junit.jupiter.api.Assumptions.assumeTrue(now.isAfter(java.time.LocalTime.of(8, 0)) && now.isBefore(java.time.LocalTime.of(23, 0)));

        // Arrange
        CreateFoodOrderRequest request = new CreateFoodOrderRequest();
        request.setOrderType("dine-in");
        String userIdentifier = "customer@gmail.com";

        Account mockAccount = new Account();
        mockAccount.setUsername(userIdentifier);

        Customer mockCustomer = new Customer();
        mockCustomer.setId(10L);

        RoomBooking mockBooking = new RoomBooking();
        mockBooking.setId(100L);
        mockBooking.setBookingStatus("Checked_In");

        when(accountRepository.findByUsername(userIdentifier)).thenReturn(Optional.of(mockAccount));
        when(customerRepository.findByAccount_Username(userIdentifier)).thenReturn(Optional.of(mockCustomer));
        when(roomBookingRepository.findByCustomerOrderByBookingDateDesc(mockCustomer))
                .thenReturn(Collections.singletonList(mockBooking));
        when(foodOrderRepository.save(any(FoodOrder.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        FoodOrder result = posService.createOrder(request, userIdentifier);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getBooking(), "Đơn đặt món online phải tự động liên kết với Booking đang Check-In của khách hàng");
        assertEquals(100L, result.getBooking().getId());
        verify(foodOrderRepository, times(1)).save(any(FoodOrder.class));
    }

    @Test
    @DisplayName("TC-UC22-02 | Đặt món trực tuyến khi khách không có Booking (Walk-in Customer Online)")
    void testCreateOrder_OnlineOrder_NoActiveBooking() {
        java.time.LocalTime now = java.time.LocalTime.now();
        org.junit.jupiter.api.Assumptions.assumeTrue(now.isAfter(java.time.LocalTime.of(8, 0)) && now.isBefore(java.time.LocalTime.of(23, 0)));

        // Arrange
        CreateFoodOrderRequest request = new CreateFoodOrderRequest();
        request.setOrderType("dine-in");
        String userIdentifier = "walkin@gmail.com";

        Account mockAccount = new Account();
        mockAccount.setUsername(userIdentifier);

        Customer mockCustomer = new Customer();
        mockCustomer.setId(11L);

        when(accountRepository.findByUsername(userIdentifier)).thenReturn(Optional.of(mockAccount));
        when(customerRepository.findByAccount_Username(userIdentifier)).thenReturn(Optional.of(mockCustomer));
        when(roomBookingRepository.findByCustomerOrderByBookingDateDesc(mockCustomer))
                .thenReturn(Collections.emptyList()); // Không có booking nào
        when(foodOrderRepository.save(any(FoodOrder.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        FoodOrder result = posService.createOrder(request, userIdentifier);

        // Assert
        assertNotNull(result);
        assertNull(result.getBooking(), "Khách không có booking thì đơn hàng sẽ không liên kết booking");
        verify(foodOrderRepository, times(1)).save(any(FoodOrder.class));
    }
}
