package com.kawai.services;

import com.kawai.models.MenuItem;
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
@DisplayName("UC20 - Quản lý trạng thái món ăn")
class MenuServiceUC20Test {

    @Mock
    private FoodItemRepository foodItemRepository;

    @InjectMocks
    private TableServiceImpl tableService;

    @Test
    @DisplayName("TC-UC20-001 | Tạo món ăn mới hợp lệ")
    void testCreateMenuItem_Success() {
        // Arrange
        MenuItem mockItem = new MenuItem();
        mockItem.setItemName("Cà phê đen");
        mockItem.setPrice(new BigDecimal("35000"));
        mockItem.setCategory("Drink");
        mockItem.setIsAvailable(true);

        when(foodItemRepository.findAll()).thenReturn(Collections.emptyList());
        when(foodItemRepository.save(any(MenuItem.class))).thenReturn(mockItem);

        // Act
        MenuItem result = tableService.createMenuItem("Cà phê đen", new BigDecimal("35000"), "Drink");

        // Assert
        assertNotNull(result);
        assertEquals("Cà phê đen", result.getItemName());
        assertTrue(result.getIsAvailable());
        verify(foodItemRepository, times(1)).save(any(MenuItem.class));
    }

    @Test
    @DisplayName("TC-UC20-002 | Chặn tạo món ăn trùng tên")
    void testCreateMenuItem_DuplicateName_ThrowsException() {
        // Arrange
        MenuItem existingItem = new MenuItem();
        existingItem.setItemName("Phở Bò");

        when(foodItemRepository.findAll()).thenReturn(Collections.singletonList(existingItem));

        // Act & Assert
        assertThrows(DataIntegrityViolationException.class, () -> {
            tableService.createMenuItem("Phở Bò", new BigDecimal("50000"), "Main");
        });
    }

    @Test
    @DisplayName("TC-UC20-003 | Chặn tạo món ăn giá âm")
    void testCreateMenuItem_NegativePrice_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            tableService.createMenuItem("Trà Đá", new BigDecimal("-5000"), "Drink");
        });
    }

    @Test
    @DisplayName("TC-UC20-004 | Toggle Availability món ăn thành công")
    void testToggleMenuAvailability_Success() {
        // Arrange
        MenuItem item = new MenuItem();
        item.setId(10L);
        item.setIsAvailable(true);

        when(foodItemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(foodItemRepository.save(any())).thenReturn(item);

        // Act
        MenuItem updated = tableService.toggleMenuAvailability(10L, false);

        // Assert
        assertFalse(updated.getIsAvailable());
        verify(foodItemRepository, times(1)).save(item);
    }
}
