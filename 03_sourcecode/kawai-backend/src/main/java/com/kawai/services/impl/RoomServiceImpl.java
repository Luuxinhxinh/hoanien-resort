package com.kawai.services.impl;

import com.kawai.dto.RoomSearchRequestDTO;
import com.kawai.dto.RoomSearchResponseDTO;
import com.kawai.models.Room;
import com.kawai.models.RoomCategory;
import com.kawai.repositories.RoomBookingRepository;
import com.kawai.repositories.RoomRepository;
import com.kawai.services.interfaces.RoomService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * RoomServiceImpl — UC09: Tìm kiếm phòng trống thời gian thực.
 *
 * Business Rules:
 * BR-FO-01 : Chỉ hiển thị phòng không có booking trùng ngày
 * BR-FO-04 : Trạng thái phòng ảnh hưởng đến kết quả tìm kiếm
 * BR-FIN-05 : Giá phòng tính bằng BigDecimal, scale 0, HALF_UP
 */
@Service
public class RoomServiceImpl implements RoomService {

    private static final int NO_OVERLAPPING = 0;

    private final RoomRepository roomRepository;
    private final RoomBookingRepository roomBookingRepository;

    public RoomServiceImpl(RoomRepository roomRepository,
            RoomBookingRepository roomBookingRepository) {
        this.roomRepository = roomRepository;
        this.roomBookingRepository = roomBookingRepository;
    }

    /**
     * Tìm phòng trống trong khoảng [checkInDate, checkOutDate).
     * Trả về danh sách rỗng (không null) nếu không có phòng khả dụng.
     */
    @Override
    public List<RoomSearchResponseDTO> searchAvailableRooms(RoomSearchRequestDTO request) {
        LocalDate checkIn = request.getCheckInDate();
        LocalDate checkOut = request.getCheckOutDate();

        List<Room> allRooms = roomRepository.findAll();
        List<RoomSearchResponseDTO> available = new ArrayList<>();

        for (Room room : allRooms) {
            if (isRoomAvailable(room.getRoomNumber(), checkIn, checkOut)) {
                available.add(toSearchResult(room, checkIn, checkOut));
            }
        }
        return available;
    }

    // ── Private helpers ────────────────────────────────────────────────────

    private boolean isRoomAvailable(String roomNumber, LocalDate checkIn, LocalDate checkOut) {
        long overlappingCount = roomBookingRepository.countOverlappingBookings(
                roomNumber, checkIn, checkOut);
        return overlappingCount == NO_OVERLAPPING;
    }

    private RoomSearchResponseDTO toSearchResult(Room room,
            LocalDate checkIn,
            LocalDate checkOut) {
        RoomSearchResponseDTO dto = new RoomSearchResponseDTO();
        dto.setRoomId(room.getId());
        dto.setRoomNumber(room.getRoomNumber());
        dto.setCheckInDate(checkIn);
        dto.setCheckOutDate(checkOut);

        RoomCategory cat = room.getCategory();
        if (cat != null) {
            dto.setCategoryName(cat.getCategoryName());
            dto.setPricePerNight(cat.getBasePrice().setScale(0, java.math.RoundingMode.HALF_UP));
            dto.setCapacity(cat.getCapacity());
        }
        return dto;
    }
}
