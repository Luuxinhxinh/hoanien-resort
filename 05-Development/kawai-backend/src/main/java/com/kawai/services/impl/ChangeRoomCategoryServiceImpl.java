package com.kawai.services.impl;

import com.kawai.dto.roomchange.ChangeRoomCategoryRequest;
import com.kawai.dto.roomchange.ChangeRoomCategoryResponse;
import com.kawai.exceptions.BusinessException;
import com.kawai.models.*;
import com.kawai.repositories.*;
import com.kawai.services.interfaces.ChangeRoomCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class ChangeRoomCategoryServiceImpl implements ChangeRoomCategoryService {

    private final RoomBookingDetailRepository roomBookingDetailRepository;
    private final RoomRepository roomRepository;
    private final FolioItemRepository folioItemRepository;
    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChangeRoomCategoryResponse changeCategory(ChangeRoomCategoryRequest request) throws BusinessException {
        RoomBookingDetail detail = getRoomBookingDetail(request.getBookingDetailId());
        validateBookingStatus(detail);

        Room newRoom = getAndValidateNewRoom(request.getSelectedRoomId());
        RoomCategory newCategory = newRoom.getCategory();
        validatePricing(newCategory);

        BigDecimal newRate = newCategory.getBasePrice() != null ? newCategory.getBasePrice() : BigDecimal.ZERO;
        BigDecimal oldRate = detail.getCategory() != null && detail.getCategory().getBasePrice() != null
                ? detail.getCategory().getBasePrice()
                : (detail.getRoomCharge() != null ? detail.getRoomCharge() : BigDecimal.ZERO);

        Room oldRoom = detail.getRoom();
        updateRoomStatuses(oldRoom, newRoom, detail.getId());
        updateAndSaveBookingDetail(detail, newRoom, newCategory);

        Long folioItemId = handleSurchargeFolioItem(detail, newRate, oldRate, oldRoom, newCategory);
        createAuditLog(request.getReceptionistAccountId(), detail.getId(), oldRoom, newRoom, newCategory);

        return ChangeRoomCategoryResponse.builder()
                .folioItemId(folioItemId)
                .build();
    }

    @Override
    public void cancelPendingChange(Long bookingDetailId, Long receptionistAccountId) {
        roomBookingDetailRepository.findById(bookingDetailId);
    }

    // --- Private Helper Methods ---

    private RoomBookingDetail getRoomBookingDetail(Long detailId) throws BusinessException {
        return roomBookingDetailRepository.findById(detailId)
                .orElseThrow(() -> new BusinessException("ERR_NOT_FOUND", "Booking detail not found."));
    }

    private void validateBookingStatus(RoomBookingDetail detail) throws BusinessException {
        String bStatus = detail.getRoomBooking().getBookingStatus();
        String dStatus = detail.getDetailStatus();
        if (!"Checked_In".equalsIgnoreCase(bStatus) && !"Checked_In".equalsIgnoreCase(dStatus)) {
            throw new BusinessException("ERR_STATUS", "Chỉ khách đang lưu trú mới được phép thay đổi hạng phòng.");
        }
    }

    private Room getAndValidateNewRoom(Long roomId) throws BusinessException {
        Room newRoom = roomRepository.findByIdWithPessimisticLock(roomId)
                .orElseThrow(
                        () -> new BusinessException("ERR_NO_ROOM", "No available rooms in the selected category."));
        if (!"Vacant_Clean".equalsIgnoreCase(newRoom.getRoomStatus())) {
            throw new BusinessException("ERR_NO_ROOM", "No available rooms in the selected category.");
        }
        return newRoom;
    }

    private void validatePricing(RoomCategory newCategory) throws BusinessException {
        if (newCategory.getBasePrice() == null) {
            throw new BusinessException("ERR_PRICING",
                    "Không thể tính chênh lệch giá phòng do phòng không có giá cơ bản.");
        }
    }

    private void updateRoomStatuses(Room oldRoom, Room newRoom, Long detailId) {
        if (oldRoom != null) {
            oldRoom.setRoomStatus("Vacant_Dirty");
            oldRoom.setCurrentBookingDetailId(null);
            roomRepository.save(oldRoom);
        }
        newRoom.setRoomStatus("Occupied");
        newRoom.setCurrentBookingDetailId(detailId);
        roomRepository.save(newRoom);
    }

    private void updateAndSaveBookingDetail(RoomBookingDetail detail, Room newRoom, RoomCategory newCategory) {
        detail.setRoom(newRoom);
        detail.setCategory(newCategory);
        try {
            roomBookingDetailRepository.save(detail);
        } catch (org.springframework.dao.DataAccessException e) {
            throw new RuntimeException("Database error saving room booking detail", e);
        }
    }

    private Long handleSurchargeFolioItem(RoomBookingDetail detail, BigDecimal newRate, BigDecimal oldRate, Room oldRoom, RoomCategory newCategory) {
        return null; // Không sinh folio chênh lệch lúc đổi phòng, để Night Audit tự tính hằng đêm
    }

    private void createAuditLog(Long accountId, Long detailId, Room oldRoom, Room newRoom, RoomCategory newCategory) {
        AuditLog log = new AuditLog();
        Account account = new Account();
        account.setId(accountId);
        log.setAccount(account);
        log.setAction("ROOM_CATEGORY_CHANGED");
        log.setTableName("Room_Booking_Details");
        log.setRecordId(detailId);

        String oldVal = oldRoom != null
                ? "Room: " + oldRoom.getRoomNumber() + ", Category: " + oldRoom.getCategory().getCategoryName()
                : "None";
        String newVal = "Room: " + newRoom.getRoomNumber() + ", Category: " + newCategory.getCategoryName();

        log.setOldValue(oldVal);
        log.setNewValue(newVal);
        log.setIpAddress("System");

        auditLogRepository.save(log);
    }
}