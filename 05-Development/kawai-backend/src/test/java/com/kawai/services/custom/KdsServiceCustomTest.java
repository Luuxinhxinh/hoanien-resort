package com.kawai.services.custom;

import com.kawai.models.*;
import com.kawai.repositories.*;
import com.kawai.services.impl.KdsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KdsServiceCustomTest {

    @Mock
    private TableReservationRepository tableReservationRepository;
    @Mock
    private RestaurantTableRepository restaurantTableRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private FoodOrderRepository foodOrderRepository;
    @Mock
    private FoodOrderDetailRepository foodOrderDetailRepository;
    @Mock
    private FoodItemRepository foodItemRepository;
    @Mock
    private RoomBookingDetailRepository roomBookingDetailRepository;
    @Mock
    private FolioItemRepository folioItemRepository;
    @Mock
    private AuditLogRepository auditLogRepository;
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private KdsServiceImpl kdsService;

    @Test
    void testCreateTableReservation() {
        Customer cust = new Customer();
        RestaurantTable table = new RestaurantTable();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(cust));
        when(restaurantTableRepository.findById(2L)).thenReturn(Optional.of(table));
        when(tableReservationRepository.save(any(TableReservation.class))).thenAnswer(i -> i.getArguments()[0]);

        TableReservation res = kdsService.createTableReservation(1L, 2L, LocalDate.now(), LocalTime.now(),
                BigDecimal.TEN);

        assertNotNull(res);
        assertEquals("Reserved", table.getTableStatus());
        verify(tableReservationRepository, times(1)).save(any(TableReservation.class));
    }

    @Test
    void testCheckAndCancelExpiredReservations() {
        RestaurantTable table = new RestaurantTable();
        table.setTableStatus("Reserved");

        TableReservation res = new TableReservation();
        res.setStatus("Pending");
        res.setTable(table);
        res.setReserveDate(LocalDate.now());
        res.setReserveTime(LocalTime.now().minusMinutes(45)); // expired (> 30 mins)

        when(tableReservationRepository.findAll()).thenReturn(Collections.singletonList(res));

        kdsService.checkAndCancelExpiredReservations();

        assertEquals("Hủy do quá hạn", res.getStatus());
        assertEquals("Vacant", table.getTableStatus());
        verify(tableReservationRepository, times(1)).save(res);
    }

    @Test
    void testCreateFoodOrder_Success() {
        Employee staff = new Employee();
        Room room = new Room();
        room.setRoomStatus("Occupied");

        RoomBookingDetail detail = new RoomBookingDetail();
        detail.setId(10L);
        detail.setRoom(room);
        detail.setSubCreditLimit(new BigDecimal("1000000"));

        RoomBooking roomBooking = new RoomBooking();
        roomBooking.setCustomer(new Customer());
        detail.setRoomBooking(roomBooking);

        MenuItem item = new MenuItem();
        item.setId(5L);
        item.setPrice(new BigDecimal("150000"));
        item.setIsAvailable(true);

        FoodOrderDetail od = new FoodOrderDetail();
        od.setMenuItem(item);
        od.setQuantity(2);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(staff));
        when(roomBookingDetailRepository.findById(10L)).thenReturn(Optional.of(detail));
        when(foodItemRepository.findById(5L)).thenReturn(Optional.of(item));
        when(foodOrderRepository.save(any(FoodOrder.class))).thenAnswer(i -> i.getArguments()[0]);
        when(foodOrderDetailRepository.save(any(FoodOrderDetail.class))).thenAnswer(i -> i.getArguments()[0]);

        FoodOrder order = kdsService.createFoodOrder(10L, null, "RoomService", Collections.singletonList(od), 1L);

        assertNotNull(order);
        verify(folioItemRepository, times(1)).save(any(FolioItem.class));
    }

    @Test
    void testCreateFoodOrder_CreditLimitExceeded() {
        Employee staff = new Employee();
        Room room = new Room();
        room.setRoomStatus("Occupied");

        RoomBookingDetail detail = new RoomBookingDetail();
        detail.setId(10L);
        detail.setRoom(room);
        detail.setSubCreditLimit(new BigDecimal("100")); // very low limit

        RoomBooking roomBooking = new RoomBooking();
        roomBooking.setCustomer(new Customer());
        detail.setRoomBooking(roomBooking);

        MenuItem item = new MenuItem();
        item.setId(5L);
        item.setPrice(new BigDecimal("150000"));
        item.setIsAvailable(true);

        FoodOrderDetail od = new FoodOrderDetail();
        od.setMenuItem(item);
        od.setQuantity(2);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(staff));
        when(roomBookingDetailRepository.findById(10L)).thenReturn(Optional.of(detail));
        when(foodItemRepository.findById(5L)).thenReturn(Optional.of(item));
        when(foodOrderRepository.save(any(FoodOrder.class))).thenAnswer(i -> i.getArguments()[0]);
        when(foodOrderDetailRepository.save(any(FoodOrderDetail.class))).thenAnswer(i -> i.getArguments()[0]);

        assertThrows(IllegalStateException.class, () -> {
            kdsService.createFoodOrder(10L, null, "RoomService", Collections.singletonList(od), 1L);
        });
    }
}
