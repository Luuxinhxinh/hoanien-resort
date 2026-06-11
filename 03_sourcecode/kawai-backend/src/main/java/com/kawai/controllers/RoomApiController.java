package com.kawai.controllers;

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

import java.util.Optional;

@RestController
@RequestMapping("/api/rooms")
public class RoomApiController {

    private final RoomRepository roomRepository;
    private final RoomBookingDetailRepository roomBookingDetailRepository;

    @Autowired
    public RoomApiController(RoomRepository roomRepository, RoomBookingDetailRepository roomBookingDetailRepository) {
        this.roomRepository = roomRepository;
        this.roomBookingDetailRepository = roomBookingDetailRepository;
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
            Optional<RoomBookingDetail> detailOpt = roomBookingDetailRepository.findById(room.getCurrentBookingDetailId());
            if (detailOpt.isPresent()) {
                RoomBookingDetail detail = detailOpt.get();
                if (detail.getCustomer() != null) {
                    dto.setGuestName(detail.getCustomer().getFullName());
                }
                dto.setLimitRemaining(detail.getSubCreditLimit());
            }
        }

        return ResponseEntity.ok(dto);
    }
}
