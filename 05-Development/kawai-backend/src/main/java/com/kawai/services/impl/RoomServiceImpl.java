package com.kawai.services.impl;

import com.kawai.dto.RoomSearchRequestDTO;
import com.kawai.dto.RoomSearchResponseDTO;
import com.kawai.dto.RoomDashboardDTO;
import com.kawai.models.Room;
import com.kawai.models.RoomCategory;
import com.kawai.repositories.RoomBookingRepository;
import com.kawai.repositories.RoomRepository;
import com.kawai.services.interfaces.RoomService;
import org.springframework.stereotype.Service;

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
    private final com.kawai.repositories.RoomCategoryRepository roomCategoryRepository;

    public RoomServiceImpl(RoomRepository roomRepository,
            RoomBookingRepository roomBookingRepository,
            com.kawai.repositories.RoomCategoryRepository roomCategoryRepository) {
        this.roomRepository = roomRepository;
        this.roomBookingRepository = roomBookingRepository;
        this.roomCategoryRepository = roomCategoryRepository;
    }

    /**
     * Tìm phòng trống trong khoảng [checkInDate, checkOutDate).
     * Trả về danh sách rỗng (không null) nếu không có phòng khả dụng.
     */
    @Override
    public List<RoomSearchResponseDTO> searchAvailableRooms(RoomSearchRequestDTO request) {
        LocalDate checkIn = request.getCheckInDate();
        LocalDate checkOut = request.getCheckOutDate();

        // ── Input Validation ───────────────────────────────────────────────
        if (checkIn == null || checkOut == null) {
            throw new IllegalArgumentException("Check-in and check-out dates must not be null");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Check-in date cannot be in the past");
        }
        if (checkIn.isAfter(checkOut) || checkIn.equals(checkOut)) {
            throw new IllegalArgumentException("Check-in date must be before check-out date");
        }
        if (request.getMinCapacity() != null && request.getMinCapacity() < 0) {
            throw new IllegalArgumentException("Min capacity must be non-negative");
        }
        if (request.getMaxPricePerNight() != null
                && request.getMaxPricePerNight().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Max price per night must be non-negative");
        }
        if (request.getPage() != null && request.getPage() < 0) {
            throw new IllegalArgumentException("Page index must be non-negative");
        }
        if (request.getSize() != null && request.getSize() <= 0) {
            throw new IllegalArgumentException("Page size must be positive");
        }

        List<Room> allRooms = roomRepository.findAll();
        if (allRooms == null) {
            allRooms = new ArrayList<>();
        }

        List<Room> availableRooms = new ArrayList<>();
        for (Room room : allRooms) {
            if (!isRoomAvailable(room.getRoomNumber(), checkIn, checkOut)) {
                continue;
            }

            RoomCategory cat = room.getCategory();
            if (cat == null) {
                continue;
            }

            if (request.getCategoryName() != null && !request.getCategoryName().trim().isEmpty()) {
                String searchCategoryName = request.getCategoryName().trim().toLowerCase();
                if (!cat.getCategoryName().toLowerCase().contains(searchCategoryName)) {
                    continue;
                }
            }
            if (request.getMinCapacity() != null) {
                if (cat.getCapacity() < request.getMinCapacity()) {
                    continue;
                }
            }
            if (request.getMaxPricePerNight() != null) {
                if (cat.getBasePrice() != null && cat.getBasePrice().compareTo(request.getMaxPricePerNight()) > 0) {
                    continue;
                }
            }

            availableRooms.add(room);
        }

        // Đếm số phòng trống theo từng hạng phòng để set availableCount
        java.util.Map<String, Long> categoryCounts = availableRooms.stream()
                .filter(r -> r.getCategory() != null)
                .collect(java.util.stream.Collectors.groupingBy(
                        r -> r.getCategory().getCategoryName(),
                        java.util.stream.Collectors.counting()
                ));

        List<RoomSearchResponseDTO> available = new ArrayList<>();
        for (Room room : availableRooms) {
            RoomCategory cat = room.getCategory();
            RoomSearchResponseDTO dto = toSearchResult(cat, checkIn, checkOut, room.getRoomNumber());
            dto.setRoomId(room.getId());
            long count = categoryCounts.getOrDefault(cat.getCategoryName(), 0L);
            dto.setAvailableCount((int) count);
            available.add(dto);
        }

        // ── Pagination ─────────────────────────────────────────────────────
        if (request.getPage() != null && request.getSize() != null) {
            int page = request.getPage();
            int size = request.getSize();
            return available.stream()
                    .skip((long) page * size)
                    .limit(size)
                    .toList();
        }

        return available;
    }

    /**
     * Lấy sơ đồ phòng (Room Matrix) thời gian thực cho Front Desk Dashboard.
     * Cung cấp cái nhìn tổng quan về trạng thái phòng, giá trị và thông tin cơ bản
     * (UC11).
     * 
     * @return Danh sách RoomDashboardDTO, rỗng nếu không có dữ liệu
     */
    @Override
    public List<RoomDashboardDTO> getRoomDashboard() {
        List<Room> allRooms = roomRepository.findAll();
        if (allRooms == null || allRooms.isEmpty()) {
            return new ArrayList<>();
        }

        return allRooms.stream()
                .map(room -> toDashboardDTO(room))
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public Room createRoom(String roomNumber, Long categoryId) {
        // Check for duplicate room number (Unique Constraint validation)
        boolean exists = roomRepository.findAll().stream()
                .anyMatch(r -> r.getRoomNumber().equalsIgnoreCase(roomNumber));
        if (exists) {
            throw new org.springframework.dao.DataIntegrityViolationException(
                    "Duplicate entry '" + roomNumber + "' for key 'room_number'");
        }

        Room room = new Room();
        room.setRoomNumber(roomNumber);
        RoomCategory category = new RoomCategory();
        category.setId(categoryId);
        room.setCategory(category);
        room.setRoomStatus("Available");
        return roomRepository.save(room);
    }

    // ── Private helpers ────────────────────────────────────────────────────

    private boolean isRoomAvailable(String roomNumber, LocalDate checkIn, LocalDate checkOut) {
        long overlappingCount = roomBookingRepository.countOverlappingBookings(
                roomNumber, checkIn, checkOut);
        return overlappingCount == NO_OVERLAPPING;
    }

    private RoomDashboardDTO toDashboardDTO(Room room) {
        RoomDashboardDTO dto = new RoomDashboardDTO();
        dto.setRoomId(room.getId());
        dto.setRoomNumber(room.getRoomNumber());
        dto.setRoomStatus(room.getRoomStatus());

        RoomCategory cat = room.getCategory();
        if (cat != null) {
            dto.setCategoryName(cat.getCategoryName());
            if (cat.getBasePrice() != null) {
                dto.setPricePerNight(cat.getBasePrice().setScale(0, java.math.RoundingMode.HALF_UP));
            }
            dto.setCapacity(cat.getCapacity());
        }
        return dto;
    }

    private RoomSearchResponseDTO toSearchResult(RoomCategory cat,
            LocalDate checkIn,
            LocalDate checkOut,
            String dummyRoomNumber) {
        RoomSearchResponseDTO dto = new RoomSearchResponseDTO();
        dto.setRoomId(cat.getId()); // Use category ID as roomId for frontend compatibility if needed
        dto.setRoomNumber(dummyRoomNumber);
        dto.setCheckInDate(checkIn);
        dto.setCheckOutDate(checkOut);

        if (cat != null) {
            dto.setCategoryName(cat.getCategoryName());
            dto.setPricePerNight(cat.getBasePrice().setScale(0, java.math.RoundingMode.HALF_UP));
            dto.setCapacity(cat.getCapacity());

            dto.setBaseAdults(cat.getBaseAdults());
            dto.setBaseChildren(cat.getBaseChildren());
            dto.setMaxAdults(cat.getMaxAdults());
            dto.setMaxChildren(cat.getMaxChildren());
            dto.setExtraAdultSurcharge(cat.getExtraAdultSurcharge());
            dto.setExtraChildSurcharge(cat.getExtraChildSurcharge());

            dto.setDescription(cat.getDescription());
            dto.setBeds(null);
            dto.setSize(null);
            dto.setView(null);
            dto.setAmenities(new java.util.ArrayList<>());
        }
        return dto;
    }
}
