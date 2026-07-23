package com.kawai.services;

import com.kawai.dto.RoomDashboardDTO;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * JUnit Test — UC11: Front Desk Dashboard (Sơ đồ Matrix phòng)
 * MODULE 2: Quản lý Phòng & Lễ tân
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Standard : ISO/IEC/IEEE 29119-3:2021
 * TDD Phase : 🟢 GREEN — Toàn bộ test đã PASS sau khi implement đúng.
 *
 * Ánh xạ test case (TC_MASTER_TABLE.md — MOD2, UC11):
 * ┌──────────────┬─────────────────────────────────────────────────────────┬───────────┐
 * │ TC ID │ Kịch bản │ Status │
 * ├──────────────┼─────────────────────────────────────────────────────────┼───────────┤
 * │ TC-M2-010a │ Dashboard có phòng → trả đúng danh sách + status │ PASS │
 * │ TC-M2-010b │ Dashboard rỗng → trả empty list (không null) │ PASS │
 * └──────────────┴─────────────────────────────────────────────────────────┴───────────┘
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UC11 — Front Desk Dashboard (RoomService) | TDD 🟢 GREEN")
class RoomServiceDashboardTest {

        @InjectMocks
        private RoomServiceImpl roomService;

        @Mock
        private RoomRepository roomRepository;

        @Mock
        private RoomBookingRepository roomBookingRepository;

        // ── Test Fixtures ─────────────────────────────────────────────────────

        private Room createRoom(Long id, String roomNo, String categoryName,
                        BigDecimal price, int capacity, String status) {
                RoomCategory cat = new RoomCategory();
                cat.setId(id);
                cat.setCategoryName(categoryName);
                cat.setBasePrice(price);
                cat.setCapacity(capacity);

                Room room = new Room();
                room.setId(id);
                room.setRoomNumber(roomNo);
                room.setCategory(cat);
                room.setRoomStatus(status);
                return room;
        }

        // ══════════════════════════════════════════════════════════════════════════
        // TC-M2-010a | HIGH | Dashboard có phòng → trả đúng danh sách + trạng thái
        // ══════════════════════════════════════════════════════════════════════════

        /**
         * TC-M2-010a — Dashboard trả đúng danh sách phòng + trạng thái thời gian thực.
         *
         * Expected:
         * - Trả về danh sách có số phòng bằng số phòng trong DB.
         * - Mỗi phòng có roomNumber, categoryName, roomStatus, pricePerNight, capacity.
         * - Trạng thái phòng phải khớp với giá trị trong DB (Vacant_Clean, Occupied,
         * Dirty...)
         *
         * 🟢 GREEN — PASS (logic hiện tại xử lý đúng khi danh sách không rỗng).
         */
        @Test
        @DisplayName("TC-M2-010a | HIGH | Dashboard có phòng — trả đúng danh sách + trạng thái")
        void TC_M2_010a_getRoomDashboard_withRooms_returnsCorrectList() {
                // Arrange
                Room room1 = createRoom(1L, "R101", "Deluxe",
                                new BigDecimal("2000000"), 2, "Vacant_Clean");
                Room room2 = createRoom(2L, "R201", "Suite",
                                new BigDecimal("3500000"), 4, "Occupied");

                when(roomRepository.findAll()).thenReturn(Arrays.asList(room1, room2));

                // Act
                List<RoomDashboardDTO> result = roomService.getRoomDashboard();

                // Assert
                assertNotNull(result, "Danh sách kết quả không được null");
                assertEquals(2, result.size(), "Phải trả đúng 2 phòng");

                // Verify phòng R101
                RoomDashboardDTO r1 = result.stream()
                                .filter(r -> "R101".equals(r.getRoomNumber()))
                                .findFirst().orElse(null);
                assertNotNull(r1, "R101 phải có trong kết quả");
                assertEquals("Deluxe", r1.getCategoryName());
                assertEquals("Vacant_Clean", r1.getRoomStatus(),
                                "Trạng thái R101 phải là Vacant_Clean");
                assertEquals(new BigDecimal("2000000"), r1.getPricePerNight());
                assertEquals(Integer.valueOf(2), r1.getCapacity());

                // Verify phòng R201
                RoomDashboardDTO r2 = result.stream()
                                .filter(r -> "R201".equals(r.getRoomNumber()))
                                .findFirst().orElse(null);
                assertNotNull(r2, "R201 phải có trong kết quả");
                assertEquals("Suite", r2.getCategoryName());
                assertEquals("Occupied", r2.getRoomStatus(),
                                "Trạng thái R201 phải là Occupied");
                assertEquals(new BigDecimal("3500000"), r2.getPricePerNight());
                assertEquals(Integer.valueOf(4), r2.getCapacity());
        }

        // ══════════════════════════════════════════════════════════════════════════
        // TC-M2-010b | MEDIUM | Dashboard rỗng → trả empty list (không null)
        // ══════════════════════════════════════════════════════════════════════════

        /**
         * TC-M2-010b — Dashboard khi hệ thống chưa có phòng nào → trả danh sách rỗng.
         *
         * 🟢 GREEN — PASS:
         * Implementation hiện tại đã trả về empty list khi allRooms.isEmpty().
         */
        @Test
        @DisplayName("TC-M2-010b | MEDIUM | Dashboard rỗng — trả empty list, không null")
        void TC_M2_010b_getRoomDashboard_noRooms_returnsEmptyList() {
                // Arrange
                when(roomRepository.findAll()).thenReturn(Collections.emptyList());

                // Act
                List<RoomDashboardDTO> result = roomService.getRoomDashboard();

                // Assert — 🟢 PASS: trả về empty list
                assertNotNull(result, "Kết quả không được null — phải trả empty list");
                assertTrue(result.isEmpty(), "Danh sách phải rỗng khi hệ thống chưa có phòng");
        }

        // ══════════════════════════════════════════════════════════════════════════
        // TC-M2-010c | HIGH | Dashboard nhiều loại status — hiển thị đúng từng loại
        // ══════════════════════════════════════════════════════════════════════════

        /**
         * TC-M2-010c — Dashboard hiển thị đúng các loại trạng thái khác nhau.
         *
         * Kiểm tra: Vacant_Clean, Occupied, Dirty, Out_of_Order đều xuất hiện đúng.
         */
        @Test
        @DisplayName("TC-M2-010c | HIGH | Dashboard hiển thị đúng nhiều loại trạng thái")
        void TC_M2_010c_getRoomDashboard_multipleStatuses_showsAll() {
                // Arrange
                Room r1 = createRoom(1L, "R101", "Deluxe",
                                new BigDecimal("2000000"), 2, "Vacant_Clean");
                Room r2 = createRoom(2L, "R102", "Deluxe",
                                new BigDecimal("2000000"), 2, "Occupied");
                Room r3 = createRoom(3L, "R103", "Suite",
                                new BigDecimal("3500000"), 4, "Dirty");
                Room r4 = createRoom(4L, "R104", "Standard",
                                new BigDecimal("1500000"), 2, "Out_of_Order");

                when(roomRepository.findAll()).thenReturn(Arrays.asList(r1, r2, r3, r4));

                // Act
                List<RoomDashboardDTO> result = roomService.getRoomDashboard();

                // Assert
                assertEquals(4, result.size(), "Phải trả đúng 4 phòng");

                // Verify mỗi phòng có đúng status
                assertRoomStatus(result, "R101", "Vacant_Clean");
                assertRoomStatus(result, "R102", "Occupied");
                assertRoomStatus(result, "R103", "Dirty");
                assertRoomStatus(result, "R104", "Out_of_Order");
        }

        // ── Helpers ───────────────────────────────────────────────────────────

        private void assertRoomStatus(List<RoomDashboardDTO> dashboard,
                        String roomNumber, String expectedStatus) {
                RoomDashboardDTO dto = dashboard.stream()
                                .filter(r -> roomNumber.equals(r.getRoomNumber()))
                                .findFirst().orElse(null);
                assertNotNull(dto, roomNumber + " phải có trong dashboard");
                assertEquals(expectedStatus, dto.getRoomStatus(),
                                roomNumber + " phải có trạng thái " + expectedStatus);
        }
}