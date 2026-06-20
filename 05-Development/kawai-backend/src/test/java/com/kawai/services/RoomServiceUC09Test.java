package com.kawai.services;

import com.kawai.dto.RoomSearchRequestDTO;
import com.kawai.dto.RoomSearchResponseDTO;
import com.kawai.models.Room;
import com.kawai.models.RoomCategory;
import com.kawai.repositories.RoomBookingRepository;
import com.kawai.repositories.RoomRepository;
import com.kawai.services.impl.RoomServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * JUnit Test — UC09: Tìm kiếm phòng trống thời gian thực
 * MODULE 2: Quản lý Phòng & Lễ tân
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Standard : ISO/IEC/IEEE 29119-3:2021
 * TDD Phase : 🔴 RED — Toàn bộ test phải FAIL trước khi implement.
 *
 * Ánh xạ test case (TC_MASTER_TABLE.md — MOD2, UC09):
 * ┌──────────────┬─────────────────────────────────────────────────────────┬──────────┐
 * │ TC ID │ Kịch bản │ RED Why │
 * ├──────────────┼─────────────────────────────────────────────────────────┼──────────┤
 * │ TC-M2-001 │ Tìm phòng trống đúng theo ngày nhận/trả — trả danh sách │
 * COMPILE │
 * │ TC-M2-002 │ Không có phòng trống — trả danh sách rỗng │ FAIL │
 * └──────────────┴─────────────────────────────────────────────────────────┴──────────┘
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UC09 — Tìm kiếm phòng trống (RoomService) | TDD 🔴 RED")
class RoomServiceUC09Test {

        // ── SUT ───────────────────────────────────────────────────────────────────
        @InjectMocks
        private RoomServiceImpl roomService;

        // ── Mocks ─────────────────────────────────────────────────────────────────
        @Mock
        private RoomRepository roomRepository;

        @Mock
        private RoomBookingRepository roomBookingRepository;

        @Mock
        private com.kawai.repositories.RoomCategoryRepository roomCategoryRepository;

        // ── Test Fixtures ─────────────────────────────────────────────────────────
        private static final LocalDate CHECK_IN = LocalDate.of(2026, 8, 1);
        private static final LocalDate CHECK_OUT = LocalDate.of(2026, 8, 5);

        private Room createRoom(Long id, String roomNo, String categoryName, BigDecimal price, int capacity) {
                RoomCategory cat = new RoomCategory();
                cat.setId(id);
                cat.setCategoryName(categoryName);
                cat.setBasePrice(price);
                cat.setCapacity(capacity);

                Room room = new Room();
                room.setId(id);
                room.setRoomNumber(roomNo);
                room.setCategory(cat);
                room.setRoomStatus("Vacant_Clean");
                return room;
        }

        // ══════════════════════════════════════════════════════════════════════════
        // TC-M2-001 | HIGH | Tìm phòng trống — có kết quả
        // ══════════════════════════════════════════════════════════════════════════

        /**
         * TC-M2-001 — Tìm phòng trống đúng theo ngày nhận/trả.
         *
         * Expected:
         * - Trả về danh sách các phòng trống trong khoảng [checkIn, checkOut)
         * - Mỗi phòng có thông tin: roomNumber, categoryName, pricePerNight, capacity
         * - Giá phòng lấy từ DailyRate.computedPrice
         *
         * 🔴 RED — COMPILE ERROR:
         * RoomServiceImpl chưa có method searchAvailableRooms().
         * → Trình biên dịch báo: cannot find symbol
         *
         * Fix cần làm:
         * Thêm method searchAvailableRooms() vào RoomService interface
         * và RoomServiceImpl.
         */
        @Test
        @DisplayName("TC-M2-001 | HIGH | Tìm phòng trống — trả danh sách phòng khả dụng")
        void TC_M2_001_searchAvailableRooms_returnsAvailableRooms() {
                // Arrange
                RoomSearchRequestDTO request = new RoomSearchRequestDTO(CHECK_IN, CHECK_OUT);

                RoomCategory cat1 = new RoomCategory();
                cat1.setId(1L);
                cat1.setCategoryName("Deluxe");
                cat1.setBasePrice(new BigDecimal("2000000"));
                cat1.setCapacity(2);
                
                RoomCategory cat2 = new RoomCategory();
                cat2.setId(2L);
                cat2.setCategoryName("Suite");
                cat2.setBasePrice(new BigDecimal("3500000"));
                cat2.setCapacity(4);

                when(roomCategoryRepository.findAll()).thenReturn(Arrays.asList(cat1, cat2));
                when(roomRepository.countActiveRoomsByCategoryName("Deluxe")).thenReturn(1L);
                when(roomRepository.countActiveRoomsByCategoryName("Suite")).thenReturn(1L);
                
                when(roomBookingRepository.countOverlappingBookingsByCategoryWithoutExclude(eq("Deluxe"), any(), any()))
                                .thenReturn(0L);
                when(roomBookingRepository.countOverlappingBookingsByCategoryWithoutExclude(eq("Suite"), any(), any()))
                                .thenReturn(0L);

                // Act
                List<RoomSearchResponseDTO> result = roomService.searchAvailableRooms(request);

                // Assert
                assertNotNull(result, "Danh sách kết quả không được null");
                assertEquals(2, result.size(), "Phải trả 2 phòng trống");

                // Verify phòng Deluxe
                RoomSearchResponseDTO r1 = result.stream()
                                .filter(r -> "Deluxe".equals(r.getCategoryName()))
                                .findFirst().orElse(null);
                assertNotNull(r1, "Deluxe phải có trong kết quả");
                assertEquals(new BigDecimal("2000000"), r1.getPricePerNight(),
                                "Giá Deluxe phải là 2,000,000/đêm");
                assertEquals(Integer.valueOf(2), r1.getCapacity(), "Sức chứa Deluxe phải là 2");

                // Verify phòng Suite
                RoomSearchResponseDTO r2 = result.stream()
                                .filter(r -> "Suite".equals(r.getCategoryName()))
                                .findFirst().orElse(null);
                assertNotNull(r2, "Suite phải có trong kết quả");
                assertEquals(new BigDecimal("3500000"), r2.getPricePerNight(),
                                "Giá Suite phải là 3,500,000/đêm");
                assertEquals(Integer.valueOf(4), r2.getCapacity(), "Sức chứa Suite phải là 4");

                // Verify dates
                assertEquals(CHECK_IN, r1.getCheckInDate(), "CheckIn phải được set");
                assertEquals(CHECK_OUT, r1.getCheckOutDate(), "CheckOut phải được set");
        }

        // ══════════════════════════════════════════════════════════════════════════
        // TC-M2-002 | MEDIUM | Không có phòng trống → danh sách rỗng
        // ══════════════════════════════════════════════════════════════════════════

        /**
         * TC-M2-002 — Không có phòng trống → trả danh sách rỗng.
         *
         * Expected:
         * - Trả về danh sách rỗng (không null)
         *
         * 🔴 RED — RUNTIME FAILURE:
         * Implementation trả về null thay vì Collections.emptyList()
         * → assertNotNull hoặc kiểm tra size sẽ FAIL.
         *
         * Fix cần làm:
         * Đảm bảo luôn trả về empty list (không null) khi không có phòng trống.
         */
        @Test
        @DisplayName("TC-M2-002 | MEDIUM | Không có phòng trống → trả danh sách rỗng")
        void TC_M2_002_searchAvailableRooms_noRooms_returnsEmptyList() {
                // Arrange
                RoomSearchRequestDTO request = new RoomSearchRequestDTO(CHECK_IN, CHECK_OUT);

                when(roomCategoryRepository.findAll()).thenReturn(Collections.emptyList());

                // Act
                List<RoomSearchResponseDTO> result = roomService.searchAvailableRooms(request);

                // Assert
                assertNotNull(result, "Kết quả không được null");
                assertTrue(result.isEmpty(), "Danh sách phải rỗng khi không có phòng nào");
        }

        // ══════════════════════════════════════════════════════════════════════════
        // TC-M2-002b | MEDIUM | Tất cả phòng đều bị booked → danh sách rỗng
        // ══════════════════════════════════════════════════════════════════════════

        /**
         * TC-M2-002b — Tất cả phòng đều có booking overlapping → danh sách rỗng.
         *
         * 🔴 RED — RUNTIME FAILURE:
         * Implementation không lọc booking overlapping đúng cách.
         */
        @Test
        @DisplayName("TC-M2-002b | MEDIUM | Tất cả phòng đều bận → trả danh sách rỗng")
        void TC_M2_002b_searchAvailableRooms_allRoomsBooked_returnsEmptyList() {
                // Arrange
                RoomSearchRequestDTO request = new RoomSearchRequestDTO(CHECK_IN, CHECK_OUT);

                RoomCategory cat1 = new RoomCategory();
                cat1.setId(1L);
                cat1.setCategoryName("Deluxe");
                cat1.setBasePrice(new BigDecimal("2000000"));
                cat1.setCapacity(2);

                when(roomCategoryRepository.findAll()).thenReturn(Arrays.asList(cat1));
                when(roomRepository.countActiveRoomsByCategoryName("Deluxe")).thenReturn(1L);
                
                when(roomBookingRepository.countOverlappingBookingsByCategoryWithoutExclude(eq("Deluxe"), any(), any()))
                                .thenReturn(1L);

                // Act
                List<RoomSearchResponseDTO> result = roomService.searchAvailableRooms(request);

                // Assert
                assertNotNull(result, "Kết quả không được null");
                assertTrue(result.isEmpty(), "Phải trả danh sách rỗng vì Deluxe đã có booking");
        }
}