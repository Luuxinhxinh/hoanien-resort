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

        // Group rooms by category
        java.util.Map<Long, List<Room>> roomsByCatId = allRooms.stream()
                .filter(r -> r.getCategory() != null)
                .collect(java.util.stream.Collectors.groupingBy(r -> r.getCategory().getId()));

        List<RoomCategory> allCategories = roomCategoryRepository.findAll();
        List<RoomSearchResponseDTO> available = new ArrayList<>();
        boolean isTodayOrPast = !checkIn.isAfter(LocalDate.now());

        for (RoomCategory cat : allCategories) {
            // Apply filters
            if (request.getCategoryName() != null && !request.getCategoryName().trim().isEmpty()) {
                String searchCategoryName = request.getCategoryName().trim().toLowerCase();
                if (!cat.getCategoryName().toLowerCase().contains(searchCategoryName)) {
                    continue;
                }
            }
            if (request.getMinCapacity() != null && cat.getCapacity() < request.getMinCapacity()) {
                continue;
            }
            if (request.getMaxPricePerNight() != null && cat.getBasePrice() != null
                    && cat.getBasePrice().compareTo(request.getMaxPricePerNight()) > 0) {
                continue;
            }

            List<Room> roomsInCat = roomsByCatId.getOrDefault(cat.getId(), new ArrayList<>());
            if (roomsInCat.isEmpty()) {
                continue;
            }

            // Count total overlapping bookings for this category
            long overlappingBookings = roomBookingRepository.countOverlappingBookingsByCategoryWithoutExclude(
                    cat.getCategoryName(), checkIn, checkOut);

            long calculatedAvailable = roomsInCat.size() - overlappingBookings;

            // Handle physical status constraints (BR-FO-04)
            if (isTodayOrPast) {
                // If checking in today, we cannot book rooms that are physically Occupied or in
                // Maintenance.
                long physicallyAvailableToday = roomsInCat.stream()
                        .filter(r -> !"Occupied".equalsIgnoreCase(r.getRoomStatus())
                                && !"Maintenance".equalsIgnoreCase(r.getRoomStatus()))
                        .count();

                // For today, if a booking is Checked_In, it's already "Occupied" physically.
                // We only need to subtract overlapping bookings that have NOT checked in yet.
                long overlappingNotCheckedIn = roomBookingRepository.countOverlappingNotCheckedIn(
                        cat.getCategoryName(), checkIn, checkOut);

                calculatedAvailable = physicallyAvailableToday - overlappingNotCheckedIn;
            } else {
                // If future, Maintenance rooms might still be excluded
                long physicallyAvailableFuture = roomsInCat.stream()
                        .filter(r -> !"Maintenance".equalsIgnoreCase(r.getRoomStatus()))
                        .count();
                calculatedAvailable = Math.min(calculatedAvailable, physicallyAvailableFuture);
            }

            if (request.getMinRooms() != null && calculatedAvailable < request.getMinRooms()) {
                continue;
            }

            if (calculatedAvailable > 0) {
                // Generate 'calculatedAvailable' DTOs with dummy room numbers so the frontend
                // can group them properly
                for (int i = 0; i < calculatedAvailable; i++) {
                    String dummyRoomNumber = cat.getCategoryName().replaceAll("\\s+", "") + "-" + (i + 1);
                    RoomSearchResponseDTO dto = toSearchResult(cat, checkIn, checkOut, dummyRoomNumber);
                    dto.setRoomId(cat.getId());
                    dto.setAvailableCount((int) calculatedAvailable);
                    available.add(dto);
                }
            }
        }
        return available;
    }

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

            // Legacy fields maintained to avoid breaking existing code
            dto.setBeds(null);
            dto.setSize(null);
            dto.setView(null);
            // New fields
            dto.setBedType(cat.getBedType());
            dto.setRoomSize(cat.getRoomSize());
            dto.setViewType(cat.getViewType());
            dto.setHasBathtub(cat.getHasBathtub());
            dto.setHasBalcony(cat.getHasBalcony());
            dto.setComplimentaryServices(cat.getComplimentaryServices());
            dto.setHasFreeBreakfast(cat.getHasFreeBreakfast());
            dto.setCoverImgUrl(cat.getCoverImgUrl());
        }
        return dto;
    }
}
