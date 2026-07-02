package com.kawai.services;

import com.kawai.dto.TableReservationRequest;
import com.kawai.exceptions.BusinessException;
import com.kawai.models.Customer;
import com.kawai.models.RestaurantTable;
import com.kawai.models.TableReservation;
import com.kawai.repositories.CustomerRepository;
import com.kawai.repositories.RestaurantTableRepository;
import com.kawai.repositories.RoomBookingDetailRepository;
import com.kawai.repositories.RoomRepository;
import com.kawai.repositories.TableReservationRepository;
import com.kawai.services.impl.TableReservationServiceImpl;
import com.kawai.services.interfaces.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * JUnit Test — UC21: Đặt bàn trực tuyến (TableReservationService)
 * MODULE 3: POS & Nhà hàng
 * ═══════════════════════════════════════════════════════════════════════════
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UC21 — Đặt bàn trực tuyến")
class TableReservationServiceUC21Test {

    @InjectMocks
    private TableReservationServiceImpl tableReservationService;

    @Mock
    private TableReservationRepository tableReservationRepository;

    @Mock
    private RestaurantTableRepository restaurantTableRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoomBookingDetailRepository roomBookingDetailRepository;

    @Mock
    private EmailService emailService;

    @Test
    @DisplayName("TC-UC21-01 | Đặt bàn thành công qua Portal -> Status Pending")
    void testCreateReservation_Success() {
        // Arrange
        TableReservationRequest request = new TableReservationRequest();
        request.setTableId(1L);
        request.setReserveDate(LocalDate.now().plusDays(1)); // Đặt cho ngày mai
        request.setStartTime(LocalTime.of(19, 0));
        request.setEndTime(LocalTime.of(21, 0));
        request.setPartySize(2);

        RestaurantTable mockTable = new RestaurantTable();
        mockTable.setId(1L);
        mockTable.setCapacity(4);

        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("test@gmail.com");

        Customer mockCustomer = new Customer();
        mockCustomer.setId(10L);

        when(restaurantTableRepository.findById(1L)).thenReturn(Optional.of(mockTable));
        when(customerRepository.findByAccount_Username("test@gmail.com")).thenReturn(Optional.of(mockCustomer));
        when(tableReservationRepository.findByTable_IdAndReserveDateOrderByReserveTimeAsc(1L, request.getReserveDate()))
                .thenReturn(Collections.emptyList());

        when(tableReservationRepository.save(any(TableReservation.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        TableReservation result = tableReservationService.createReservation(request, mockPrincipal);

        // Assert
        assertNotNull(result);
        assertEquals("Pending", result.getStatus());
        assertEquals(1L, result.getTable().getId());
        assertEquals(mockCustomer, result.getCustomer());
        verify(tableReservationRepository, times(1)).save(any(TableReservation.class));
    }

    @Test
    @DisplayName("TC-UC21-02 | Exception khi số người vượt sức chứa của bàn")
    void testCreateReservation_CapacityExceeded() {
        // Arrange
        TableReservationRequest request = new TableReservationRequest();
        request.setTableId(1L);
        request.setPartySize(10); // Quá sức chứa

        RestaurantTable mockTable = new RestaurantTable();
        mockTable.setId(1L);
        mockTable.setCapacity(4); // Sức chứa chỉ 4

        when(restaurantTableRepository.findById(1L)).thenReturn(Optional.of(mockTable));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            tableReservationService.createReservation(request, null);
        });

        assertEquals("TABLE-002", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Party size exceeds table capacity"));
    }

    @Test
    @DisplayName("TC-UC21-03 | Exception khi trùng lịch đặt bàn")
    void testCreateReservation_TimeConflict() {
        // Arrange
        TableReservationRequest request = new TableReservationRequest();
        request.setTableId(1L);
        request.setReserveDate(LocalDate.now().plusDays(1));
        request.setStartTime(LocalTime.of(19, 0));
        request.setEndTime(LocalTime.of(21, 0));
        request.setPartySize(2);

        RestaurantTable mockTable = new RestaurantTable();
        mockTable.setId(1L);
        mockTable.setCapacity(4);

        TableReservation existingRes = new TableReservation();
        existingRes.setReserveTime(LocalTime.of(18, 30));
        existingRes.setEndTime(LocalTime.of(20, 0)); // Trùng giờ với đơn mới (19:00 - 21:00)
        existingRes.setStatus("Confirmed");

        when(restaurantTableRepository.findById(1L)).thenReturn(Optional.of(mockTable));
        when(tableReservationRepository.findByTable_IdAndReserveDateOrderByReserveTimeAsc(1L, request.getReserveDate()))
                .thenReturn(Collections.singletonList(existingRes));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            tableReservationService.createReservation(request, null);
        });

        assertEquals("TABLE-003", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("already reserved"));
    }
}
