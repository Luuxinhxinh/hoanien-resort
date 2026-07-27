package com.hoanien.services.impl;

import com.hoanien.dto.roomchange.ChangeRoomCategoryRequest;
import com.hoanien.dto.roomchange.ChangeRoomCategoryResponse;
import com.hoanien.exceptions.BusinessException;
import com.hoanien.models.*;
import com.hoanien.repositories.*;
import com.hoanien.services.interfaces.ChangeRoomCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ChangeRoomCategoryServiceImpl implements ChangeRoomCategoryService {

    private final RoomBookingDetailRepository roomBookingDetailRepository;
    private final RoomRepository roomRepository;
    private final FolioItemRepository folioItemRepository;
    private final AuditLogRepository auditLogRepository;
    private final RoomGuestRepository roomGuestRepository;
    private final RoomSurchargeRepository roomSurchargeRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChangeRoomCategoryResponse changeCategory(ChangeRoomCategoryRequest request) throws BusinessException {
        RoomBookingDetail detail = getRoomBookingDetail(request.getBookingDetailId());
        validateBookingStatus(detail);

        Room newRoom = getAndValidateNewRoom(request.getSelectedRoomId());
        RoomCategory newCategory = newRoom.getCategory();
        validatePricing(newCategory);

        BigDecimal newRate = newCategory.getBasePrice() != null ? newCategory.getBasePrice() : BigDecimal.ZERO;
        RoomCategory oldCategory = detail.getCategory();
        BigDecimal oldRate = oldCategory != null && oldCategory.getBasePrice() != null
                ? oldCategory.getBasePrice()
                : (detail.getRoomCharge() != null ? detail.getRoomCharge() : BigDecimal.ZERO);
        BigDecimal oldExtraSurcharge = detail.getExtraSurcharge() != null ? detail.getExtraSurcharge() : BigDecimal.ZERO;

        Room oldRoom = detail.getRoom();
        updateRoomStatuses(oldRoom, newRoom, detail.getId());
        updateAndSaveBookingDetail(detail, newRoom, newCategory);

        BigDecimal newExtraSurcharge = detail.getExtraSurcharge() != null ? detail.getExtraSurcharge() : BigDecimal.ZERO;

        BigDecimal dailyRoomDiff = newRate.subtract(oldRate);
        BigDecimal dailySurchargeDiff = newExtraSurcharge.subtract(oldExtraSurcharge);
        BigDecimal totalDailyDiff = dailyRoomDiff.add(dailySurchargeDiff);

        long nights = 1;
        RoomBooking booking = detail.getRoomBooking();
        if (booking != null && booking.getCheckInDate() != null && booking.getCheckOutDate() != null) {
            long n = java.time.temporal.ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
            if (n > 0) nights = n;
        }

        BigDecimal totalUpgradeDiff = totalDailyDiff.multiply(BigDecimal.valueOf(nights)).setScale(0, java.math.RoundingMode.HALF_UP);

        Long folioItemId = handleSurchargeFolioItem(detail, totalUpgradeDiff, oldCategory, newCategory);
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

    private int calculateAge(java.time.LocalDate birthDate) {
        if (birthDate == null) {
            return 18; // default to adult
        }
        return java.time.Period.between(birthDate, java.time.LocalDate.now()).getYears();
    }

    private BigDecimal calculateNewExtraSurcharge(RoomBookingDetail detail, RoomCategory category) {
        java.util.List<RoomGuest> guests = roomGuestRepository.findByRoomBookingDetailId(detail.getId());
        if (guests == null || guests.isEmpty()) {
            return detail.getExtraSurcharge() != null ? detail.getExtraSurcharge() : BigDecimal.ZERO;
        }

        int adults = 0;
        java.util.List<Integer> childAges = new java.util.ArrayList<>();
        for (RoomGuest rg : guests) {
            java.time.LocalDate birthDate = null;
            if (rg.getCustomer() != null) {
                birthDate = rg.getCustomer().getBirthDate();
            } else if (rg.getDependent() != null) {
                birthDate = rg.getDependent().getBirthDate();
            }
            int age = calculateAge(birthDate);
            if (age >= 18) {
                adults++;
            } else {
                childAges.add(age);
            }
        }

        int maxAdults = category.getMaxAdults() != null ? category.getMaxAdults() : (category.getCapacity() != null ? category.getCapacity() : 2);
        int maxChildren = category.getMaxChildren() != null ? category.getMaxChildren() : 2;
        int baseAdults = category.getBaseAdults() != null ? category.getBaseAdults() : (category.getCapacity() != null ? category.getCapacity() : 2);
        int baseChildren = category.getBaseChildren() != null ? category.getBaseChildren() : 0;

        BigDecimal surcharge = BigDecimal.ZERO;
        int extraAdults = Math.max(0, adults - baseAdults);
        if (extraAdults > 0 && category.getExtraAdultSurcharge() != null) {
            surcharge = surcharge.add(category.getExtraAdultSurcharge().multiply(BigDecimal.valueOf(extraAdults)));
        }

        int chargeableChildren = Math.max(0, childAges.size() - baseChildren);
        if (chargeableChildren > 0) {
            java.util.Collections.sort(childAges);
            int skipCount = childAges.size() - chargeableChildren;
            for (int i = skipCount; i < childAges.size(); i++) {
                surcharge = surcharge.add(
                        roomSurchargeRepository.findSurchargeForAge(category, childAges.get(i))
                                .map(RoomSurcharge::getPriceModifier)
                                .orElse(BigDecimal.ZERO));
            }
        }
        return surcharge;
    }

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
        
        BigDecimal newExtraSurcharge = calculateNewExtraSurcharge(detail, newCategory);
        detail.setExtraSurcharge(newExtraSurcharge.compareTo(BigDecimal.ZERO) > 0 ? newExtraSurcharge : null);

        try {
            roomBookingDetailRepository.save(detail);
        } catch (org.springframework.dao.DataAccessException e) {
            throw new RuntimeException("Database error saving room booking detail", e);
        }
    }

    private Long handleSurchargeFolioItem(RoomBookingDetail detail, BigDecimal totalUpgradeDiff,
            RoomCategory oldCategory, RoomCategory newCategory) {
        java.util.List<FolioItem> existingItems = folioItemRepository.findByRoomBookingDetailId(detail.getId());
        if (existingItems != null) {
            for (FolioItem item : existingItems) {
                // Xóa các khoản chênh lệch gộp cũ (nếu có từ dữ liệu cũ/upfront)
                if (item.getDescription() != null && (item.getDescription().contains("gộp chênh lệch phụ thu") || (item.getDescription().contains("hạng phòng") && !item.getDescription().contains("(Đêm")))) {
                    folioItemRepository.delete(item);
                }
            }
        }

        // Theo quy định: Phụ phí / hoàn tiền nâng hạ hạng phòng KHÔNG cộng gộp ngay lập tức.
        // Khoản chênh lệch sẽ được Night Audit tự động tính và cộng dồn vào Folio theo từng đêm lưu trú thực tế.
        return null;
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