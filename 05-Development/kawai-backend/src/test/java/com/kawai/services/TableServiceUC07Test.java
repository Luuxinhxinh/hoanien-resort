package com.kawai.services;

import com.kawai.models.RestaurantTable;
import com.kawai.models.MenuItem;
import com.kawai.repositories.RestaurantTableRepository;
import com.kawai.repositories.FoodItemRepository;
import com.kawai.services.impl.TableServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UC07 - Table & Menu Core Data CRUD (FnBService)")
public class TableServiceUC07Test {

    @Mock
    private RestaurantTableRepository tableRepository;

    @Mock
    private FoodItemRepository foodItemRepository;

    @InjectMocks
    private TableServiceImpl tableService;

    @Test
    @DisplayName("TC-UC07-001 | Chặn tạo trùng tên bàn (Unique Constraint)")
    void testSaveTable_DuplicateName_ThrowsException() {
        RestaurantTable existingTable = new RestaurantTable();
        existingTable.setTableNumber("T01");

        when(tableRepository.findAll()).thenReturn(Collections.singletonList(existingTable));

        assertThrows(DataIntegrityViolationException.class, () -> {
            tableService.saveTable("T01", 4);
        });
    }

    @Test
    @DisplayName("TC-UC07-002 | Đóng bảo trì bàn hợp lệ")
    void testToggleStatus_Valid_Success() {
        RestaurantTable table = new RestaurantTable();
        table.setId(2L);
        table.setTableStatus("AVAILABLE");

        when(tableRepository.findById(2L)).thenReturn(Optional.of(table));
        when(tableRepository.save(any())).thenReturn(table);

        RestaurantTable updated = tableService.toggleStatus(2L, "OUT_OF_SERVICE");

        assertEquals("OUT_OF_SERVICE", updated.getTableStatus());
        verify(tableRepository, times(1)).save(table);
    }

    @Test
    @DisplayName("TC-UC07-003 | Chặn đóng bàn đang có khách ngồi")
    void testToggleStatus_Occupied_ThrowsException() {
        RestaurantTable table = new RestaurantTable();
        table.setId(3L);
        table.setTableStatus("OCCUPIED");

        when(tableRepository.findById(3L)).thenReturn(Optional.of(table));

        assertThrows(IllegalStateException.class, () -> {
            tableService.toggleStatus(3L, "OUT_OF_SERVICE");
        });
    }

    @Test
    @DisplayName("TC-UC07-004 | Xóa mềm bàn (Soft Delete)")
    void testSoftDeleteTable_Success() {
        RestaurantTable table = new RestaurantTable();
        table.setId(4L);
        table.setIsActive(true);

        when(tableRepository.findById(4L)).thenReturn(Optional.of(table));
        
        tableService.softDeleteTable(4L);

        assertFalse(table.getIsActive());
        verify(tableRepository, times(1)).save(table);
    }

    @Test
    @DisplayName("TC-UC07-005 | Chặn tạo món ăn trùng tên")
    void testCreateMenuItem_DuplicateName_ThrowsException() {
        MenuItem existingItem = new MenuItem();
        existingItem.setItemName("Phở Bò");

        when(foodItemRepository.findAll()).thenReturn(Collections.singletonList(existingItem));

        assertThrows(DataIntegrityViolationException.class, () -> {
            tableService.createMenuItem("Phở Bò", new BigDecimal("50000"), "Main");
        });
    }

    @Test
    @DisplayName("TC-UC07-006 | Chặn tạo món ăn giá âm")
    void testCreateMenuItem_NegativePrice_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            tableService.createMenuItem("Trà Đá", new BigDecimal("-5000"), "Drink");
        });
    }

    @Test
    @DisplayName("TC-UC07-007 | Toggle Availability món ăn thành công")
    void testToggleMenuAvailability_Success() {
        MenuItem item = new MenuItem();
        item.setId(10L);
        item.setIsAvailable(true);

        when(foodItemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(foodItemRepository.save(any())).thenReturn(item);

        MenuItem updated = tableService.toggleMenuAvailability(10L, false);

        assertFalse(updated.getIsAvailable());
        verify(foodItemRepository, times(1)).save(item);
    }
}
