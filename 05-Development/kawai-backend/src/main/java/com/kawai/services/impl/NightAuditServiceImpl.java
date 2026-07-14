package com.kawai.services.impl;

import com.kawai.models.Booking;
import com.kawai.models.RoomBooking;
import com.kawai.models.FolioItem;
import com.kawai.models.RoomBookingDetail;
import com.kawai.repositories.FolioItemRepository;
import com.kawai.repositories.RoomBookingDetailRepository;
import com.kawai.repositories.StaffScheduleRepository;
import com.kawai.services.interfaces.NightAuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(NightAuditServiceImpl.class);

    private final FolioItemRepository folioItemRepository;
    private final RoomBookingDetailRepository roomBookingDetailRepository;
    private final StaffScheduleRepository staffScheduleRepository;

    @Autowired
    public NightAuditServiceImpl(FolioItemRepository folioItemRepository,
            RoomBookingDetailRepository roomBookingDetailRepository,
            StaffScheduleRepository staffScheduleRepository) {
        this.folioItemRepository = folioItemRepository;
        this.roomBookingDetailRepository = roomBookingDetailRepository;
        this.staffScheduleRepository = staffScheduleRepository;
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
        // Bước 1: Xử lý Pending Arrivals (Đổi thành No-Show)
        processNoShows(auditDate);

        // Bước 2: Kiểm tra tiền kiện (Shift Closure, Pending Departures)
        validatePreconditions(auditDate);

        // Bước 3: Tính tiền phòng (Room occupancy is tracked on Room.roomStatus. Booking details are checked in)
        List<RoomBookingDetail> checkedInRooms = roomBookingDetailRepository
                .findByDetailStatusIn(List.of("Checked_In", "CHECKED_IN"));

        for (RoomBookingDetail detail : checkedInRooms) {
            FolioItem item = createRoomChargeItem(detail, auditDate);
            if (item == null) {
                // Dữ liệu thiếu Booking hợp lệ -> bỏ qua, không tạo charge sai lệch
                continue;
            }
            folioItemRepository.save(item);
        }
    }

    private void processNoShows(LocalDate auditDate) {
        List<RoomBookingDetail> pendingArrivals = roomBookingDetailRepository
                .findByRoomBooking_CheckInDateAndDetailStatus(auditDate, "Pending");
        
        for (RoomBookingDetail detail : pendingArrivals) {
            detail.setDetailStatus("No_Show");
            roomBookingDetailRepository.save(detail);
            logger.info("[NIGHT AUDIT] Tự động chuyển trạng thái No-Show cho RoomBookingDetail ID: {}", detail.getId());
        }
    }

    private void validatePreconditions(LocalDate auditDate) {
        // 1. Kiểm tra Khách chưa Check-out (Pending Departures)
        long pendingDepartures = roomBookingDetailRepository.countByRoomBooking_CheckOutDateAndDetailStatus(auditDate, "Checked_In");
        if (pendingDepartures > 0) {
            throw new IllegalStateException("Tiền kiện thất bại: Còn " + pendingDepartures + " khách có lịch check-out hôm nay nhưng chưa trả phòng hoặc chưa gia hạn. Vui lòng xử lý trước khi đóng ngày.");
        }
    }

    /**
     * Helper: Tạo FolioItem tiền phòng cho một phòng đang OCCUPIED.
     * Trả về null nếu dữ liệu thiếu Booking hợp lệ (cần Manager xử lý thủ công).
     */
    private FolioItem createRoomChargeItem(RoomBookingDetail detail, LocalDate auditDate) {
        BigDecimal roomCharge = detail.getRoomCharge();
        if (roomCharge == null) {
            roomCharge = BigDecimal.ZERO;
        }
        BigDecimal extraSurcharge = detail.getExtraSurcharge();
        if (extraSurcharge != null && extraSurcharge.compareTo(BigDecimal.ZERO) > 0) {
            roomCharge = roomCharge.add(extraSurcharge);
        }

        RoomBooking roomBooking = detail.getRoomBooking();
        Booking booking = roomBooking;

        if (booking == null || booking.getId() == null) {
            logger.error("[NIGHT AUDIT] RoomBookingDetail id={} thiếu Booking hợp lệ, bỏ qua tạo room charge.",
                    detail.getId());
            return null;
        }

        FolioItem item = new FolioItem();
        item.setRoomBookingDetail(detail);
        item.setBooking(booking);
        item.setPayerCustomer(booking.getCustomer());
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
