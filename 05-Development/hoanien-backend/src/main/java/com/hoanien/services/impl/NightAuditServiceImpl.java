package com.hoanien.services.impl;

import com.hoanien.models.Booking;
import com.hoanien.models.RoomBooking;
import com.hoanien.models.FolioItem;
import com.hoanien.models.RoomBookingDetail;
import com.hoanien.repositories.FolioItemRepository;
import com.hoanien.repositories.RoomBookingDetailRepository;
import com.hoanien.repositories.StaffScheduleRepository;
import com.hoanien.services.interfaces.NightAuditService;
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
    private com.hoanien.services.interfaces.PricingService pricingService;

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
        // Bước 2: Kiểm tra tiền kiện (Shift Closure, Pending Departures)
        validatePreconditions(auditDate);

        // Bước 3: Tính tiền phòng (Room occupancy is tracked on Room.roomStatus.
        // Booking details are checked in)
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
                logger.info(
                        "[NIGHT AUDIT] Đã tồn tại Room Charge cho RoomBookingDetail ID: {} ngày {}, bỏ qua tạo trùng.",
                        detail.getId(), auditDate);
            }

            // TỰ ĐỘNG TÍNH CHÊNH LỆCH ĐỔI HẠNG PHÒNG ĐÊM NAY
            handleNightAuditRoomUpgradeSurcharge(detail, auditDate);
        }
    }

    private void validatePreconditions(LocalDate auditDate) {
        // 1. Kiểm tra Khách chưa Check-out (Pending Departures)
        long pendingDepartures = roomBookingDetailRepository.countByRoomBooking_CheckOutDateAndDetailStatus(auditDate,
                "Checked_In");
        if (pendingDepartures > 0) {
            throw new IllegalStateException("Tiền kiện thất bại: Còn " + pendingDepartures
                    + " khách có lịch check-out hôm nay nhưng chưa trả phòng hoặc chưa gia hạn. Vui lòng xử lý trước khi đóng ngày.");
        }
    }

    /**
     * Helper: Tạo FolioItem tiền phòng cho một phòng đang OCCUPIED.
     * Trả về null nếu dữ liệu thiếu Booking hợp lệ (cần Manager xử lý thủ công).
     */
    private FolioItem createRoomChargeItem(RoomBookingDetail detail, LocalDate auditDate) {
        BigDecimal roomCharge = pricingService.getPriceForDate(detail.getCategory(), auditDate);
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

    @Autowired
    private com.hoanien.repositories.RoomGuestRepository roomGuestRepository;

    @Autowired
    private com.hoanien.repositories.RoomSurchargeRepository roomSurchargeRepository;

    @Autowired
    private com.hoanien.repositories.RoomCategoryRepository roomCategoryRepository;

    private BigDecimal calculateExtraSurchargeForCategory(RoomBookingDetail detail, com.hoanien.models.RoomCategory category) {
        if (category == null || roomGuestRepository == null) {
            return BigDecimal.ZERO;
        }
        java.util.List<com.hoanien.models.RoomGuest> guests = roomGuestRepository.findByRoomBookingDetailId(detail.getId());
        if (guests == null || guests.isEmpty()) {
            return BigDecimal.ZERO;
        }

        int adults = 0;
        java.util.List<Integer> childAges = new java.util.ArrayList<>();
        for (com.hoanien.models.RoomGuest rg : guests) {
            java.time.LocalDate birthDate = null;
            if (rg.getCustomer() != null) {
                birthDate = rg.getCustomer().getBirthDate();
            } else if (rg.getDependent() != null) {
                birthDate = rg.getDependent().getBirthDate();
            }
            int age = birthDate != null ? java.time.Period.between(birthDate, java.time.LocalDate.now()).getYears() : 18;
            if (age >= 18) {
                adults++;
            } else {
                childAges.add(age);
            }
        }

        int baseAdults = category.getBaseAdults() != null ? category.getBaseAdults() : (category.getCapacity() != null ? category.getCapacity() : 2);
        int baseChildren = category.getBaseChildren() != null ? category.getBaseChildren() : 0;

        BigDecimal surcharge = BigDecimal.ZERO;
        int extraAdults = Math.max(0, adults - baseAdults);
        if (extraAdults > 0 && category.getExtraAdultSurcharge() != null) {
            surcharge = surcharge.add(category.getExtraAdultSurcharge().multiply(BigDecimal.valueOf(extraAdults)));
        }

        int chargeableChildren = Math.max(0, childAges.size() - baseChildren);
        if (chargeableChildren > 0 && roomSurchargeRepository != null) {
            java.util.Collections.sort(childAges);
            int skipCount = childAges.size() - chargeableChildren;
            for (int i = skipCount; i < childAges.size(); i++) {
                surcharge = surcharge.add(
                        roomSurchargeRepository.findSurchargeForAge(category, childAges.get(i))
                                .map(com.hoanien.models.RoomSurcharge::getPriceModifier)
                                .orElse(BigDecimal.ZERO));
            }
        }
        return surcharge;
    }

    @Autowired
    private com.hoanien.repositories.AuditLogRepository auditLogRepository;

    private void handleNightAuditRoomUpgradeSurcharge(RoomBookingDetail detail, LocalDate auditDate) {
        if (auditLogRepository == null) return;

        // CHỈ xử lý phụ thu/hoàn tiền nâng hạ hạng phòng NẾU phòng này ĐÃ TỪNG CÓ THAO TÁC NÂNG HẠ HẠNG PHÒNG trong AuditLog
        com.hoanien.models.AuditLog categoryChangeLog = auditLogRepository.findAll().stream()
                .filter(log -> "ROOM_CATEGORY_CHANGED".equals(log.getAction())
                        && "Room_Booking_Details".equals(log.getTableName())
                        && detail.getId().equals(log.getRecordId()))
                .findFirst()
                .orElse(null);

        if (categoryChangeLog == null) {
            // Phòng này CHƯA TỪNG bị đổi hạng phòng -> KHÔNG tạo bất kỳ khoản phụ thu/hoàn tiền chênh lệch nào!
            return;
        }

        RoomBooking booking = detail.getRoomBooking();

        // Parse hạng phòng gốc từ auditLog (được lưu với định dạng "Room: X, Category: Y")
        String originalCategoryName = null;
        if (categoryChangeLog.getOldValue() != null) {
            String oldValue = categoryChangeLog.getOldValue();
            int catIndex = oldValue.indexOf("Category: ");
            if (catIndex != -1) {
                originalCategoryName = oldValue.substring(catIndex + "Category: ".length()).trim();
            }
        }

        com.hoanien.models.RoomCategory origCat = null;
        if (originalCategoryName != null && roomCategoryRepository != null) {
            final String targetName = originalCategoryName;
            origCat = roomCategoryRepository.findAll().stream()
                    .filter(cat -> cat.getCategoryName().equalsIgnoreCase(targetName))
                    .findFirst()
                    .orElse(null);
        }

        BigDecimal origRate = origCat != null && origCat.getBasePrice() != null ? origCat.getBasePrice() : BigDecimal.ZERO;
        BigDecimal currentRate = detail.getCategory() != null && detail.getCategory().getBasePrice() != null ? detail.getCategory().getBasePrice() : origRate;

        // 1. Chênh lệch tiền phòng cơ bản 1 đêm
        BigDecimal dailyRoomDiff = currentRate.subtract(origRate);

        // 2. Chênh lệch phụ thu người lớn/trẻ em 1 đêm theo hạng phòng mới vs hạng phòng gốc
        BigDecimal origExtraSurcharge = origCat != null ? calculateExtraSurchargeForCategory(detail, origCat) : BigDecimal.ZERO;
        BigDecimal currentExtraSurcharge = calculateExtraSurchargeForCategory(detail, detail.getCategory());
        BigDecimal dailySurchargeDiff = currentExtraSurcharge.subtract(origExtraSurcharge);

        List<FolioItem> existingItems = getFolioItems(detail.getId());
        String dateSuffix = " (Đêm " + auditDate.toString() + ")";

        // -------------------------------------------------------------
        // Khoản 1: Phụ phí / Hoàn tiền đổi hạng phòng cơ bản (nếu có chênh lệch)
        // -------------------------------------------------------------
        if (dailyRoomDiff.compareTo(BigDecimal.ZERO) != 0) {
            boolean alreadyHasRoomDiff = existingItems != null && existingItems.stream()
                    .anyMatch(item -> item.getDescription() != null && item.getDescription().endsWith(dateSuffix)
                            && (item.getDescription().contains("nâng hạng phòng") || item.getDescription().contains("hạ hạng phòng")));

            if (!alreadyHasRoomDiff) {
                FolioItem folioItem = new FolioItem();
                folioItem.setBooking(booking);
                folioItem.setRoomBookingDetail(detail);
                folioItem.setPayerCustomer(booking != null ? booking.getCustomer() : null);
                folioItem.setSourceDepartment("FRONT_DESK");
                folioItem.setAmount(dailyRoomDiff.setScale(0, RoundingMode.HALF_UP));
                folioItem.setIsSettledSeparately(false);
                long roomCount = booking != null ? roomBookingDetailRepository.findByRoomBookingId(booking.getId()).size() : 1;
                folioItem.setRevenueCode(roomCount > 1 ? "ROOM_GROUP" : "ROOM_TRANSIENT");

                String categoryName = detail.getCategory() != null ? detail.getCategory().getCategoryName() : "";
                if (dailyRoomDiff.compareTo(BigDecimal.ZERO) > 0) {
                    folioItem.setDescription("Phụ phí nâng hạng phòng lên " + categoryName + dateSuffix);
                } else {
                    folioItem.setDescription("Hoàn tiền chênh lệch hạ hạng phòng xuống " + categoryName + dateSuffix);
                }
                folioItemRepository.save(folioItem);
            }
        }

        // -------------------------------------------------------------
        // Khoản 2: Chênh lệch phụ thu người lớn & trẻ em theo hạng phòng mới (nếu có chênh lệch)
        // -------------------------------------------------------------
        if (dailySurchargeDiff.compareTo(BigDecimal.ZERO) != 0) {
            boolean alreadyHasGuestDiff = existingItems != null && existingItems.stream()
                    .anyMatch(item -> item.getDescription() != null && item.getDescription().endsWith(dateSuffix)
                            && item.getDescription().contains("phụ thu người lớn/trẻ em"));

            if (!alreadyHasGuestDiff) {
                FolioItem surchargeItem = new FolioItem();
                surchargeItem.setBooking(booking);
                surchargeItem.setRoomBookingDetail(detail);
                surchargeItem.setPayerCustomer(booking != null ? booking.getCustomer() : null);
                surchargeItem.setSourceDepartment("FRONT_DESK");
                surchargeItem.setAmount(dailySurchargeDiff.setScale(0, RoundingMode.HALF_UP));
                surchargeItem.setIsSettledSeparately(false);
                long roomCount = booking != null ? roomBookingDetailRepository.findByRoomBookingId(booking.getId()).size() : 1;
                surchargeItem.setRevenueCode(roomCount > 1 ? "ROOM_GROUP" : "ROOM_TRANSIENT");

                String categoryName = detail.getCategory() != null ? detail.getCategory().getCategoryName() : "";
                if (dailySurchargeDiff.compareTo(BigDecimal.ZERO) > 0) {
                    surchargeItem.setDescription("Phụ thu thêm người lớn/trẻ em theo hạng phòng mới " + categoryName + dateSuffix);
                } else {
                    surchargeItem.setDescription("Giảm trừ phụ thu người lớn/trẻ em theo hạng phòng mới " + categoryName + dateSuffix);
                }
                folioItemRepository.save(surchargeItem);
            }
        }
    }

    @Override
    public LocalDate getNextBusinessDate(LocalDate auditDate) {
        if (auditDate == null) {
            return LocalDate.now().plusDays(1);
        }
        return auditDate.plusDays(1);
    }
}
