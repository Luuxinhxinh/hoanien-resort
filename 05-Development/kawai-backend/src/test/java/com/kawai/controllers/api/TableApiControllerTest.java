package com.kawai.controllers.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kawai.dto.TableReservationRequest;
import com.kawai.models.Customer;
import com.kawai.models.RestaurantTable;
import com.kawai.models.TableReservation;
import com.kawai.repositories.RestaurantTableRepository;
import com.kawai.repositories.TableReservationRepository;
import com.kawai.repositories.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class TableApiControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TableReservationRepository tableReservationRepository;

    @Mock
    private RestaurantTableRepository restaurantTableRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private com.kawai.repositories.CustomerRepository customerRepository;

    @InjectMocks
    private TableApiController tableApiController;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(tableApiController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    public void testReserveTable_Success() throws Exception {
        TableReservationRequest request = new TableReservationRequest();
        request.setTableId(1L);
        request.setReserveDate(LocalDate.now().plusDays(1));
        request.setStartTime(LocalTime.of(19, 0));
        request.setEndTime(LocalTime.of(21, 0));
        request.setPartySize(4);
        request.setSpecialRequests("Window seat");

        RestaurantTable mockTable = new RestaurantTable();
        mockTable.setId(1L);
        mockTable.setCapacity(6);
        mockTable.setIsActive(true);

        when(restaurantTableRepository.findById(1L)).thenReturn(Optional.of(mockTable));
        when(tableReservationRepository.findByTable_IdAndReserveDateOrderByReserveTimeAsc(any(Long.class), any(LocalDate.class))).thenReturn(Collections.emptyList());
        when(customerRepository.findById(1L)).thenReturn(Optional.of(new Customer()));
        when(tableReservationRepository.save(any(TableReservation.class))).thenAnswer(i -> {
            TableReservation r = i.getArgument(0);
            r.setId(1L);
            return r;
        });

        mockMvc.perform(post("/api/v1/tables/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()); // Currently will fail because it returns badRequest
    }
    
    @Test
    public void testReserveTable_CapacityExceeded() throws Exception {
        TableReservationRequest request = new TableReservationRequest();
        request.setTableId(1L);
        request.setReserveDate(LocalDate.now().plusDays(1));
        request.setStartTime(LocalTime.of(19, 0));
        request.setEndTime(LocalTime.of(21, 0));
        request.setPartySize(10); // Exceeds capacity

        RestaurantTable mockTable = new RestaurantTable();
        mockTable.setId(1L);
        mockTable.setCapacity(6); // Capacity is only 6
        mockTable.setIsActive(true);

        when(restaurantTableRepository.findById(1L)).thenReturn(Optional.of(mockTable));

        mockMvc.perform(post("/api/v1/tables/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testReserveTable_Conflict() throws Exception {
        TableReservationRequest request = new TableReservationRequest();
        request.setTableId(1L);
        request.setReserveDate(LocalDate.now().plusDays(1));
        request.setStartTime(LocalTime.of(19, 0));
        request.setEndTime(LocalTime.of(21, 0));
        request.setPartySize(4);

        RestaurantTable mockTable = new RestaurantTable();
        mockTable.setId(1L);
        mockTable.setCapacity(6);
        mockTable.setIsActive(true);

        TableReservation existingReservation = new TableReservation();
        existingReservation.setTable(mockTable);
        existingReservation.setReserveDate(request.getReserveDate());
        existingReservation.setReserveTime(LocalTime.of(18, 0));
        existingReservation.setEndTime(LocalTime.of(20, 0)); // Overlaps with 19:00 - 21:00
        existingReservation.setStatus("Confirmed");

        when(restaurantTableRepository.findById(1L)).thenReturn(Optional.of(mockTable));
        when(tableReservationRepository.findByTable_IdAndReserveDateOrderByReserveTimeAsc(any(Long.class), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(existingReservation));

        mockMvc.perform(post("/api/v1/tables/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
