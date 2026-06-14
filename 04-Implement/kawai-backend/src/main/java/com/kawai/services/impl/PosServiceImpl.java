package com.kawai.services.impl;

import com.kawai.exceptions.BusinessException;
import com.kawai.models.FolioItem;
import com.kawai.models.Room;
import com.kawai.models.RoomBookingDetail;
import com.kawai.repositories.FolioItemRepository;
import com.kawai.repositories.RoomBookingDetailRepository;
import com.kawai.repositories.RoomRepository;
import com.kawai.services.interfaces.PosService;

import java.math.BigDecimal;

public class PosServiceImpl implements PosService {

    private final RoomRepository roomRepository;
    private final RoomBookingDetailRepository roomBookingDetailRepository;
    private final FolioItemRepository folioItemRepository;

    public PosServiceImpl(RoomRepository roomRepository,
            RoomBookingDetailRepository roomBookingDetailRepository,
            FolioItemRepository folioItemRepository) {
        this.roomRepository = roomRepository;
        this.roomBookingDetailRepository = roomBookingDetailRepository;
        this.folioItemRepository = folioItemRepository;
    }

    @Override
    public void chargeToRoom(String roomNumber, BigDecimal amount) {
        Room room = getOccupiedRoom(roomNumber);
        RoomBookingDetail detail = getBookingDetail(room.getCurrentBookingDetailId());

        validateCreditLimit(detail, amount);

        postChargeToFolio(detail, amount);
    }

    private Room getOccupiedRoom(String roomNumber) {
        Room room = roomRepository.findByRoomNumber(roomNumber)
                .orElseThrow(() -> new BusinessException("ROOM_NOT_FOUND", "Phòng không tồn tại"));

        if (!"OCCUPIED".equalsIgnoreCase(room.getRoomStatus())) {
            throw new BusinessException("ROOM_NOT_OCCUPIED", "Phòng không ở trạng thái OCCUPIED");
        }
        return room;
    }

    private RoomBookingDetail getBookingDetail(Long bookingDetailId) {
        return roomBookingDetailRepository.findById(bookingDetailId)
                .orElseThrow(() -> new BusinessException("BOOKING_NOT_FOUND", "Không tìm thấy thông tin đặt phòng"));
    }

    private void validateCreditLimit(RoomBookingDetail detail, BigDecimal amount) {
        if (detail.getSubCreditLimit().compareTo(amount) < 0) {
            throw new BusinessException("POS-003", "Post to Room vượt Credit Limit");
        }
    }

    private void postChargeToFolio(RoomBookingDetail detail, BigDecimal amount) {
        FolioItem folioItem = new FolioItem();
        folioItem.setRoomBookingDetail(detail);
        folioItem.setSourceDepartment("POS");
        folioItem.setAmount(amount);
        folioItem.setDescription("Ký gửi hóa đơn từ nhà hàng");

        folioItemRepository.save(folioItem);
    }
}
