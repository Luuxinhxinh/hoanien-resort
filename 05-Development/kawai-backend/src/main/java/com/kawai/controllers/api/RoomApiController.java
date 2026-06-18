package com.kawai.controllers.api;

import com.kawai.dto.RoomInfoDto;
import com.kawai.models.Room;
import com.kawai.models.RoomBookingDetail;
import com.kawai.repositories.RoomRepository;
import com.kawai.repositories.RoomBookingDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kawai.dto.RoomSearchRequestDTO;
import com.kawai.dto.RoomSearchResponseDTO;
import com.kawai.services.interfaces.RoomService;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/rooms")
public class RoomApiController {

    private final RoomRepository roomRepository;
    private final RoomBookingDetailRepository roomBookingDetailRepository;
    private final RoomService roomService;

    @Autowired
    public RoomApiController(RoomRepository roomRepository,
            RoomBookingDetailRepository roomBookingDetailRepository,
            RoomService roomService) {
        this.roomRepository = roomRepository;
        this.roomBookingDetailRepository = roomBookingDetailRepository;
        this.roomService = roomService;
    }

    @GetMapping("/{roomNumber}/info")
    public ResponseEntity<RoomInfoDto> getRoomInfo(@PathVariable String roomNumber) {
        Optional<Room> roomOpt = roomRepository.findByRoomNumber(roomNumber);

        if (roomOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Room room = roomOpt.get();
        boolean isOccupied = "OCCUPIED".equalsIgnoreCase(room.getRoomStatus());

        RoomInfoDto dto = RoomInfoDto.builder()
                .roomNumber(room.getRoomNumber())
                .status(room.getRoomStatus())
                .occupied(isOccupied)
                .build();

        if (isOccupied && room.getCurrentBookingDetailId() != null) {
            Optional<RoomBookingDetail> detailOpt = roomBookingDetailRepository
                    .findById(room.getCurrentBookingDetailId());
            if (detailOpt.isPresent()) {
                RoomBookingDetail detail = detailOpt.get();
                if (detail.getRoomBooking() != null && detail.getRoomBooking().getCustomer() != null) {
                    dto.setGuestName(detail.getRoomBooking().getCustomer().getFullName());
                }
                dto.setLimitRemaining(detail.getSubCreditLimit());
            }
        }

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/search")
    public ResponseEntity<List<RoomSearchResponseDTO>> searchRooms(
            @RequestParam("checkIn") String checkInStr,
            @RequestParam("checkOut") String checkOutStr,
            @RequestParam(value = "categoryName", required = false) String categoryName,
            @RequestParam(value = "capacity", required = false) Integer capacity,
            @RequestParam(value = "minRooms", required = false) Integer minRooms) {

        LocalDate checkIn = LocalDate.parse(checkInStr);
        LocalDate checkOut = LocalDate.parse(checkOutStr);

        RoomSearchRequestDTO request = new RoomSearchRequestDTO(checkIn, checkOut);
        request.setCategoryName(categoryName);
        request.setMinCapacity(capacity);
        request.setMinRooms(minRooms);

        List<RoomSearchResponseDTO> availableRooms = roomService.searchAvailableRooms(request);
        return ResponseEntity.ok(availableRooms);
    }
}
