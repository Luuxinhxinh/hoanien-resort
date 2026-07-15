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
import java.time.temporal.ChronoUnit;
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
            List<FolioItem> existingItems = getFolioItems(detail.getId());
            boolean alreadyHasRoomCharge = false;
            if (existingItems != null) {
                String targetDesc = "Room Charge - Night " + auditDate.toString();
                alreadyHasRoomCharge = existingItems.stream()
                        .anyMatch(item -> item.getDescription() != null && item.getDescription().equals(targetDesc));
            }

            if (!alreadyHasRoomCharge) {
                FolioItem item = createRoomChargeItem(detail, auditDate);
                if (item != null) {
                    folioItemRepository.save(item);
                }
            } else {
                logger.info("[NIGHT AUDIT] Đã tồn tại Room Charge cho RoomBookingDetail ID: {} ngày {}, bỏ qua tạo trùng.", detail.getId(), auditDate);
            }

            // TỰ ĐỘNG TÍNH CHÊNH LỆCH ĐỔI HẠNG PHÒNG ĐÊM NAY
            handleNightAuditRoomUpgradeSurcharge(detail, auditDate);
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
        long roomCount = roomBookingDetailRepository.findByRoomBookingId(booking.getId()).size();
        item.setRevenueCode(roomCount > 1 ? "ROOM_GROUP" : "ROOM_TRANSIENT");
        return item;
    }

    private void handleNightAuditRoomUpgradeSurcharge(RoomBookingDetail detail, LocalDate auditDate) {
        BigDecimal baseRate = detail.getRoomCharge() != null ? detail.getRoomCharge() : BigDecimal.ZERO;
        RoomBooking booking = detail.getRoomBooking();
        if (booking != null) {
            long nights = ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
            if (nights > 0 && "DIRECT_WEB".equalsIgnoreCase(booking.getBookingSource())) {
                baseRate = baseRate.divide(BigDecimal.valueOf(nights), 2, RoundingMode.HALF_UP);
            }
        }

        BigDecimal currentRate = detail.getCategory() != null && detail.getCategory().getBasePrice() != null 
                ? detail.getCategory().getBasePrice() 
                : baseRate;
        
        BigDecimal dailyDifference = currentRate.subtract(baseRate);
        if (dailyDifference.compareTo(BigDecimal.ZERO) == 0) {
            return; // Đêm nay không có chênh lệch so với giá gốc
        }

        // Kiểm tra xem đã có phụ phí nâng/hạ hạng phòng cho đêm này chưa
        List<FolioItem> existingItems = getFolioItems(detail.getId());
        if (existingItems != null) {
            String suffix = " (Đêm " + auditDate.toString() + ")";
            boolean alreadyHasSurcharge = existingItems.stream()
                    .anyMatch(item -> item.getDescription() != null && item.getDescription().endsWith(suffix));
            if (alreadyHasSurcharge) {
                logger.info("[NIGHT AUDIT] Đã tồn tại phụ phí đổi hạng phòng cho RoomBookingDetail ID: {} ngày {}, bỏ qua tạo trùng.", detail.getId(), auditDate);
                return;
            }
        }

        FolioItem folioItem = new FolioItem();
        folioItem.setBooking(booking);
        folioItem.setRoomBookingDetail(detail);
        folioItem.setPayerCustomer(booking != null ? booking.getCustomer() : null);
        folioItem.setSourceDepartment("FRONT_DESK");
        folioItem.setAmount(dailyDifference.setScale(0, RoundingMode.HALF_UP)); 
        folioItem.setIsSettledSeparately(false);
        long roomCount = booking != null ? roomBookingDetailRepository.findByRoomBookingId(booking.getId()).size() : 1;
        folioItem.setRevenueCode(roomCount > 1 ? "ROOM_GROUP" : "ROOM_TRANSIENT");

        String categoryName = detail.getCategory() != null ? detail.getCategory().getCategoryName() : "";
        if (dailyDifference.compareTo(BigDecimal.ZERO) > 0) {
            folioItem.setDescription("Phụ phí nâng hạng phòng lên " + categoryName + " (Đêm " + auditDate.toString() + ")");
        } else {
            folioItem.setDescription("Hoàn tiền chênh lệch hạ hạng phòng xuống " + categoryName + " (Đêm " + auditDate.toString() + ")");
        }

        folioItemRepository.save(folioItem);
    }

    @Override
    public LocalDate getNextBusinessDate(LocalDate auditDate) {
        if (auditDate == null) {
            return LocalDate.now().plusDays(1);
        }
        return auditDate.plusDays(1);
    }
}
