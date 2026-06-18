package com.kawai.services;

import com.kawai.models.Room;
import com.kawai.models.RoomCategory;
import com.kawai.repositories.RoomCategoryRepository;
import com.kawai.repositories.RoomRepository;
import com.kawai.services.impl.CategoryServiceImpl;
import com.kawai.services.impl.RoomServiceImpl;
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
@DisplayName("UC06 - Category & Room Core Data CRUD")
public class CategoryRoomServiceUC06Test {

    @Mock
    private RoomCategoryRepository categoryRepository;

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @InjectMocks
    private RoomServiceImpl roomService;

    @Test
    @DisplayName("TC-UC06-001 | Chặn xóa Category đang chứa phòng vật lý")
    void testDeleteCategory_HasRooms_ThrowsResourceInUseException() {
        RoomCategory category = new RoomCategory();
        category.setId(1L);
        category.setIsActive(true);

        Room room = new Room();
        room.setId(101L);
        room.setCategory(category);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(roomRepository.findAll()).thenReturn(Collections.singletonList(room));

        assertThrows(IllegalStateException.class, () -> {
            categoryService.deleteCategory(1L);
        });
        
        // Assert remains true
        assertTrue(category.getIsActive());
    }

    @Test
    @DisplayName("TC-UC06-002 | Soft delete thành công Hạng phòng trống")
    void testDeleteCategory_EmptyCategory_Success() {
        RoomCategory category = new RoomCategory();
        category.setId(2L);
        category.setIsActive(true);

        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category));
        when(roomRepository.findAll()).thenReturn(Collections.emptyList());

        categoryService.deleteCategory(2L);

        assertFalse(category.getIsActive());
        verify(categoryRepository, times(1)).save(category);
    }

    @Test
    @DisplayName("TC-UC06-003 | Chặn tạo phòng trùng số phòng (Unique Constraint)")
    void testCreateRoom_DuplicateRoomNumber_ThrowsDataIntegrityViolationException() {
        Room existingRoom = new Room();
        existingRoom.setRoomNumber("A-101");

        when(roomRepository.findAll()).thenReturn(Collections.singletonList(existingRoom));

        assertThrows(DataIntegrityViolationException.class, () -> {
            roomService.createRoom("A-101", 1L);
        });
    }

    @Test
    @DisplayName("TC-UC06-004 | Cập nhật thông tin Hạng phòng thành công")
    void testUpdateCategory_Success() {
        RoomCategory category = new RoomCategory();
        category.setId(3L);
        category.setBasePrice(new BigDecimal("1000"));

        when(categoryRepository.findById(3L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any())).thenReturn(category);

        RoomCategory updated = categoryService.updateCategory(3L, new BigDecimal("1500"));

        assertEquals(new BigDecimal("1500"), updated.getBasePrice());
        verify(categoryRepository, times(1)).save(category);
    }
}
