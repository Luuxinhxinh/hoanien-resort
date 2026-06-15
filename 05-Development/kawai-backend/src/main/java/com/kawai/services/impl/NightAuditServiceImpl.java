package com.kawai.services.impl;

import com.kawai.models.Booking;
import com.kawai.models.FolioItem;
import com.kawai.models.RoomBookingDetail;
import com.kawai.repositories.FolioItemRepository;
import com.kawai.repositories.RoomBookingDetailRepository;
import com.kawai.services.interfaces.NightAuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Triển khai dịch vụ Night Audit & Folio (UC24).
 * Tuân thủ BR-FIN-03, BR-FIN-05, BR-FIN-06, BR-FIN-07, BR-FB-01.
 */
@Service
public class NightAuditServiceImpl implements NightAuditService {

    private final FolioItemRepository folioItemRepository;
    private final RoomBookingDetailRepository roomBookingDetailRepository;

    @Autowired
    public NightAuditServiceImpl(FolioItemRepository folioItemRepository,
            RoomBookingDetailRepository roomBookingDetailRepository) {
        this.folioItemRepository = folioItemRepository;
        this.roomBookingDetailRepository = roomBookingDetailRepository;
    }

    @Override
    public List<FolioItem> getFolioItems(Long roomBookingDetailId) {
        return folioItemRepository.findByRoomBookingDetailId(roomBookingDetailId);
    }

    @Override
    public BigDecimal calculateFolioBalance(Long roomBookingDetailId) {
        List<FolioItem> items = getFolioItems(roomBookingDetailId);
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return calculateSum(items.stream()
                .filter(item -> !item.getIsSettledSeparately()));
    }

    @Override
    public BigDecimal aggregateFolioTotal(Long roomBookingDetailId) {
        List<FolioItem> items = getFolioItems(roomBookingDetailId);
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return calculateSum(items.stream());
    }

    /**
     * Helper: Tính tổng số tiền từ Stream các FolioItem (áp dụng BR-FIN-05).
     */
    private BigDecimal calculateSum(java.util.stream.Stream<FolioItem> itemStream) {
        return itemStream
                .map(FolioItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(0, RoundingMode.HALF_UP);
    }

    @Override
    @Transactional
    public void runNightAudit(LocalDate auditDate) {
        // Tìm các phòng đang OCCUPIED
        List<RoomBookingDetail> occupiedRooms = roomBookingDetailRepository.findByDetailStatus("OCCUPIED");

        for (RoomBookingDetail detail : occupiedRooms) {
            FolioItem item = createRoomChargeItem(detail, auditDate);
            folioItemRepository.save(item);
        }
    }

    /**
     * Helper: Tạo FolioItem tiền phòng cho một phòng đang OCCUPIED.
     */
    private FolioItem createRoomChargeItem(RoomBookingDetail detail, LocalDate auditDate) {
        BigDecimal roomCharge = detail.getRoomCharge();
        if (roomCharge == null) {
            roomCharge = BigDecimal.ZERO;
        }

        FolioItem item = new FolioItem();
        item.setRoomBookingDetail(detail);

        Booking booking = detail.getRoomBooking();
        if (booking == null) {
            booking = new Booking(); // Fallback an toàn
        }
        item.setBooking(booking);
        item.setPayerCustomer(detail.getCustomer());

        item.setSourceDepartment("ROOM");
        item.setAmount(roomCharge.setScale(0, RoundingMode.HALF_UP));
        item.setDescription("Room Charge - Night " + auditDate.toString());
        item.setIsSettledSeparately(false);

        return item;
    }

    @Override
    public LocalDate getNextBusinessDate(LocalDate auditDate) {
        if (auditDate == null) {
            return LocalDate.now().plusDays(1);
        }
        return auditDate.plusDays(1);
    }
}
