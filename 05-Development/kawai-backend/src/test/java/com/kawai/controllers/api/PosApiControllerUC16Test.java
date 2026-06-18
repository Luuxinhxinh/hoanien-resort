package com.kawai.controllers.api;

import com.kawai.dto.CartItemDto;
import com.kawai.dto.CreateFoodOrderRequest;
import com.kawai.models.*;
import com.kawai.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PosApiControllerUC16Test {

    @Mock
    private FoodOrderRepository foodOrderRepository;
    @Mock
    private FoodOrderDetailRepository foodOrderDetailRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private RoomBookingDetailRepository roomBookingDetailRepository;
    @Mock
    private FoodItemRepository foodItemRepository;
    @Mock
    private RoomBookingRepository roomBookingRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private AccountRepository accountRepository; // Bắt buộc mock
    
    @InjectMocks
    private PosApiController posApiController;

    private CreateFoodOrderRequest request;
    private RoomBooking roomBooking;
    private RoomBookingDetail roomBookingDetail;
    private Room room;
    private MenuItem menuItem;

    @BeforeEach
    void setUp() {
        request = new CreateFoodOrderRequest();
        request.setOrderType("room-svc");
        request.setRoomNumber("101");
        request.setPaymentType("CHARGE_TO_ROOM");

        CartItemDto item = new CartItemDto();
        item.setId(1L);
        item.setPrice(new BigDecimal("100000"));
        item.setQty(2);
        
        List<CartItemDto> items = new ArrayList<>();
        items.add(item);
        request.setItems(items);

        roomBooking = new RoomBooking();
        roomBooking.setId(1L);
        roomBooking.setCreditLimit(new BigDecimal("500000")); // 500k limit

        roomBookingDetail = new RoomBookingDetail();
        roomBookingDetail.setId(1L);
        roomBookingDetail.setRoomBooking(roomBooking);

        room = new Room();
        room.setRoomNumber("101");
        room.setCurrentBookingDetailId(1L);
        
        menuItem = new MenuItem();
        menuItem.setId(1L);
        menuItem.setPrice(new BigDecimal("100000"));
    }

    @Test
    void testCreateOrder_ChargeToRoom_Success() {
        // Arrange
        when(roomRepository.findByRoomNumber("101")).thenReturn(Optional.of(room));
        when(roomBookingDetailRepository.findById(1L)).thenReturn(Optional.of(roomBookingDetail));
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty()); // Fallback
        when(accountRepository.findByUsername("testUser")).thenReturn(Optional.empty());

        when(foodOrderRepository.save(any(FoodOrder.class))).thenAnswer(i -> {
            FoodOrder fo = i.getArgument(0);
            fo.setId(99L);
            return fo;
        });
        when(foodItemRepository.findById(1L)).thenReturn(Optional.of(menuItem));

        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("testUser");

        // Act
        ResponseEntity<?> response = posApiController.createOrder(request, principal);

        // Assert
        assertEquals(200, response.getStatusCodeValue(), "Phải trả về HTTP 200 OK");
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("success", body.get("status"));
        
        // Verify credit limit was updated
        // 200k + 5% (10k) = 210k. 500k - 210k = 290k.
        verify(roomBookingRepository, times(1)).save(roomBooking);
        assertEquals(0, new BigDecimal("290000").compareTo(roomBooking.getCreditLimit()), 
                "Hạn mức tín dụng mới phải là 290,000 VND");
    }

    @Test
    void testCreateOrder_ChargeToRoom_CreditLimitExceeded() {
        // Arrange
        // Set credit limit to 200k (less than 210k total)
        roomBooking.setCreditLimit(new BigDecimal("200000"));

        when(roomRepository.findByRoomNumber("101")).thenReturn(Optional.of(room));
        when(roomBookingDetailRepository.findById(1L)).thenReturn(Optional.of(roomBookingDetail));
        when(accountRepository.findByUsername("testUser")).thenReturn(Optional.empty());

        when(foodOrderRepository.save(any(FoodOrder.class))).thenAnswer(i -> {
            FoodOrder fo = i.getArgument(0);
            fo.setId(99L);
            return fo;
        });
        when(foodItemRepository.findById(1L)).thenReturn(Optional.of(menuItem));

        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("testUser");

        // Act
        ResponseEntity<?> response = posApiController.createOrder(request, principal);

        // Assert
        assertEquals(400, response.getStatusCodeValue(), "Phải trả về HTTP 400 Bad Request");
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("error", body.get("status"));
        assertTrue(body.get("message").toString().contains("Hạn mức tín dụng của phòng không đủ"));

        // Verify credit limit was NOT updated
        verify(roomBookingRepository, never()).save(roomBooking);
        assertEquals(0, new BigDecimal("200000").compareTo(roomBooking.getCreditLimit()), 
                "Hạn mức tín dụng không được thay đổi");
    }
}
