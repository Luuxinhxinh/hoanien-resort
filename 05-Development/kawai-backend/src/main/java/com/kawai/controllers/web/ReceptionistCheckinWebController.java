package com.kawai.controllers.web;

import com.kawai.dto.CheckinSubmitFormDTO;
import com.kawai.dto.DependentRegistrationDTO;
import com.kawai.dto.TourRoomAllocationDTO;
import com.kawai.exceptions.BusinessException;
import com.kawai.models.RoomBookingDetail;
import com.kawai.models.TourBooking;
import com.kawai.services.interfaces.CheckinService;
import com.kawai.services.interfaces.DependentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Controller xử lý Form Submit Web MVC cho Lễ tân Check-in (Không dùng fetch
 * API)
 */
@Controller
@RequestMapping("/receptionist/checkin")
@PreAuthorize("hasAnyAuthority('OP_BOOKING', 'ROLE_ADMIN', 'ROLE_MANAGER')")
public class ReceptionistCheckinWebController {

    private static final Logger log = LoggerFactory.getLogger(ReceptionistCheckinWebController.class);

    private final CheckinService checkinService;
    private final DependentService dependentService;
    private final com.kawai.repositories.RoomBookingDetailRepository roomBookingDetailRepo;
    private final com.kawai.repositories.RoomRepository roomRepo;
    private final com.kawai.repositories.CustomerRepository customerRepo;
    private final com.kawai.repositories.BookingRepository bookingRepo;
    private final com.kawai.repositories.RoomGuestRepository roomGuestRepo;
    private final com.kawai.repositories.TourBookingRepository tourBookingRepo;

    public ReceptionistCheckinWebController(CheckinService checkinService,
            DependentService dependentService,
            com.kawai.repositories.RoomBookingDetailRepository roomBookingDetailRepo,
            com.kawai.repositories.RoomRepository roomRepo,
            com.kawai.repositories.CustomerRepository customerRepo,
            com.kawai.repositories.BookingRepository bookingRepo,
            com.kawai.repositories.RoomGuestRepository roomGuestRepo,
            com.kawai.repositories.TourBookingRepository tourBookingRepo) {
        this.checkinService = checkinService;
        this.dependentService = dependentService;
        this.roomBookingDetailRepo = roomBookingDetailRepo;
        this.roomRepo = roomRepo;
        this.customerRepo = customerRepo;
        this.bookingRepo = bookingRepo;
        this.roomGuestRepo = roomGuestRepo;
        this.tourBookingRepo = tourBookingRepo;
    }

    @PostMapping("/complete")
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public String completeCheckin(@ModelAttribute CheckinSubmitFormDTO form, RedirectAttributes redirectAttributes) {
        log.info("Bắt đầu xử lý Form Check-in bulk. BookingId: {}, Số phòng gán: {}, Số người đi kèm: {}",
                form.getBookingId(),
                form.getAssignedRoomNumbers() != null ? form.getAssignedRoomNumbers().size() : 0,
                form.getDependents() != null ? form.getDependents().size() : 0);

        try {
            // Lấy Booking và cập nhật Customer info nếu có thay đổi
            com.kawai.models.Booking booking = bookingRepo.findById(form.getBookingId())
                    .orElseThrow(() -> new BusinessException("CHECKIN-001", "Không tìm thấy Đơn hàng!"));

            com.kawai.models.Customer customer = booking.getCustomer();
            if (customer != null) {
                if (form.getGuestName() != null && !form.getGuestName().trim().isEmpty()) {
                    customer.setFullName(form.getGuestName().trim());
                }
                if (form.getPhone() != null && !form.getPhone().trim().isEmpty()) {
                    if (!com.kawai.utils.ValidationUtils.isValidPhone(form.getPhone())) {
                        throw new BusinessException("CHECKIN-VAL-01",
                                "Số điện thoại không hợp lệ (phải bắt đầu bằng 0 và có đúng 10 số)!");
                    }
                    customer.setPhone(form.getPhone().trim());
                }
                if (form.getCccd() != null && !form.getCccd().trim().isEmpty()) {
                    if (!com.kawai.utils.ValidationUtils.isValidDocument(form.getCccd())) {
                        throw new BusinessException("CHECKIN-VAL-02",
                                "CCCD/Hộ chiếu không hợp lệ (CCCD phải gồm 12 số, hộ chiếu 6-15 ký tự chữ/số)!");
                    }
                    try {
                        String encrypted = com.kawai.utils.EncryptionUtils.encrypt(form.getCccd().trim());
                        customer.setCccdPassportEncrypted(encrypted);
                    } catch (Exception e) {
                        customer.setCccdPassportEncrypted(form.getCccd().trim());
                    }
                }
                customerRepo.save(customer);
            }

            // Lấy danh sách Detail của Booking này
            java.util.List<com.kawai.models.RoomBookingDetail> details = roomBookingDetailRepo
                    .findByRoomBookingId(form.getBookingId());
            if (details == null || details.isEmpty()) {
                throw new com.kawai.exceptions.BusinessException("CHECKIN-001",
                        "Không tìm thấy thông tin phòng cho Đơn này!");
            }
            if (form.getAssignedRoomNumbers() == null || form.getAssignedRoomNumbers().isEmpty()) {
                throw new com.kawai.exceptions.BusinessException("CHECKIN-002",
                        "Bạn chưa chọn phòng vật lý nào để giao cho khách!");
            }

            // Lọc ra các detail chưa check-in để đem đi gán
            java.util.List<com.kawai.models.RoomBookingDetail> pendingDetails = new java.util.ArrayList<>();
            for (com.kawai.models.RoomBookingDetail d : details) {
                if (!"CHECKED_IN".equalsIgnoreCase(d.getDetailStatus())) {
                    pendingDetails.add(d);
                }
            }

            if (form.getAssignedRoomNumbers() == null) {
                form.setAssignedRoomNumbers(new java.util.ArrayList<>());
            }
            form.getAssignedRoomNumbers().removeIf(String::isEmpty);

            java.math.BigDecimal totalRequested = java.math.BigDecimal.ZERO;
            if (form.getAllocatedCreditLimits() != null) {
                for (java.math.BigDecimal limit : form.getAllocatedCreditLimits()) {
                    if (limit != null) totalRequested = totalRequested.add(limit);
                }
            }
            java.math.BigDecimal existingUsed = java.math.BigDecimal.ZERO;
            for (com.kawai.models.RoomBookingDetail d : details) {
                if (!pendingDetails.contains(d) && d.getSubCreditLimit() != null) {
                    existingUsed = existingUsed.add(d.getSubCreditLimit());
                }
            }
            // RoomBooking extends Booking — cast an toàn vì bookingRepo dùng JOINED inheritance
            com.kawai.models.RoomBooking roomBooking = (com.kawai.models.RoomBooking) booking;
            java.math.BigDecimal masterCreditLimit = roomBooking.getCreditLimit() != null
                    ? roomBooking.getCreditLimit()
                    : new java.math.BigDecimal("5000000.00");
            if (existingUsed.add(totalRequested).compareTo(masterCreditLimit) > 0) {
                throw new com.kawai.exceptions.BusinessException("CHECKIN-007",
                        "Tổng hạn mức cấp cho các phòng vượt quá hạn mức của tài khoản (Master: " + masterCreditLimit + ")");
            }

              if (form.getAssignedRoomNumbers().size() != pendingDetails.size()) {
                throw new com.kawai.exceptions.BusinessException("CHECKIN-005",
                        "Bạn phải phân đủ " + pendingDetails.size() + " phòng trước khi hoàn tất Check-in!");
            }

            // Bước 1: Giao từng phòng (Mapping room -> detail có cùng hạng phòng)
            java.util.Map<String, Long> roomNumberToDetailIdMap = new java.util.HashMap<>();

            for (String roomNumber : form.getAssignedRoomNumbers()) {
                com.kawai.models.Room room = roomRepo.findByRoomNumber(roomNumber)
                        .orElseThrow(() -> new com.kawai.exceptions.BusinessException("CHECKIN-003",
                                "Không tìm thấy phòng số " + roomNumber));

                // Tìm detail có cùng Category với phòng vật lý này
                com.kawai.models.RoomBookingDetail matchedDetail = null;
                for (com.kawai.models.RoomBookingDetail d : pendingDetails) {
                    if (d.getCategory().getId().equals(room.getCategory().getId())) {
                        matchedDetail = d;
                        break;
                    }
                }

                if (matchedDetail == null) {
                    throw new com.kawai.exceptions.BusinessException("CHECKIN-004",
                            "Phòng " + roomNumber + " thuộc hạng " + room.getCategory().getCategoryName()
                                    + " không khớp với bất kỳ hạng phòng nào đang chờ check-in của đơn này!");
                }

                // Xóa detail đã được gán khỏi danh sách chờ để không bị gán trùng
                pendingDetails.remove(matchedDetail);

                // Gọi service lõi để Check-in 1 phòng bằng roomId thực sự
                java.math.BigDecimal allocatedLimit = null;
                int roomIdx = form.getAssignedRoomNumbers().indexOf(roomNumber);
                if (form.getAllocatedCreditLimits() != null && roomIdx < form.getAllocatedCreditLimits().size()) {
                    allocatedLimit = form.getAllocatedCreditLimits().get(roomIdx);
                }
                checkinService.checkIn(matchedDetail.getId(), room.getId(), allocatedLimit);
                roomNumberToDetailIdMap.put(roomNumber, matchedDetail.getId());
            }
            if (form.getAssignedRoomNumbers() != null && !form.getAssignedRoomNumbers().isEmpty()) {
                String firstRoom = form.getAssignedRoomNumbers().get(0);
                Long firstDetailId = roomNumberToDetailIdMap.get(firstRoom);

                if (firstDetailId != null && customer != null) {
                    com.kawai.models.RoomBookingDetail firstDetail = roomBookingDetailRepo.findById(firstDetailId)
                            .orElse(null);
                    if (firstDetail != null) {
                        // Tìm MasterGuest đã được tạo sẵn từ lúc đặt phòng
                        com.kawai.models.RoomGuest existingMasterGuest = null;
                        for (com.kawai.models.RoomBookingDetail detail : details) {
                            java.util.List<com.kawai.models.RoomGuest> detailGuests = roomGuestRepo
                                    .findByRoomBookingDetailId(detail.getId());
                            for (com.kawai.models.RoomGuest rg : detailGuests) {
                                if (rg.getCustomer() != null && rg.getCustomer().getId().equals(customer.getId())) {
                                    existingMasterGuest = rg;
                                    break;
                                }
                            }
                            if (existingMasterGuest != null)
                                break;
                        }

                        if (existingMasterGuest != null) {
                            // Dời sang phòng đầu tiên được chọn
                            existingMasterGuest.setRoomBookingDetail(firstDetail);
                            existingMasterGuest.setIsPrimaryContact(true);
                            roomGuestRepo.saveAndFlush(existingMasterGuest);
                        } else {
                            // Tạo mới nếu chưa có (ví dụ Booking tạo từ nguồn khác không có stub)
                            com.kawai.models.RoomGuest masterGuest = new com.kawai.models.RoomGuest();
                            masterGuest.setRoomBookingDetail(firstDetail);
                            masterGuest.setCustomer(customer);
                            masterGuest.setGuestType("ADULT");
                            masterGuest.setIsPrimaryContact(true);
                            roomGuestRepo.saveAndFlush(masterGuest);
                        }
                    }
                }
            }

            // Bước 3: THÊM NGƯỜI ĐI KÈM
            if (form.getDependents() != null) {
                for (com.kawai.dto.DependentRegistrationDTO dependentDTO : form.getDependents()) {
                    if (dependentDTO != null && dependentDTO.getFullName() != null
                            && !dependentDTO.getFullName().trim().isEmpty()) {
                        // Nếu lễ tân gán dependent vào 1 phòng vật lý cụ thể, ta set detailId tương ứng
                        if (dependentDTO.getAssignedPhysicalRoomNumber() != null
                                && !dependentDTO.getAssignedPhysicalRoomNumber().isEmpty()) {
                            Long detailId = roomNumberToDetailIdMap.get(dependentDTO.getAssignedPhysicalRoomNumber());
                            if (detailId != null) {
                                dependentDTO.setRoomBookingDetailId(detailId);
                            }
                        }
                        dependentService.registerDependent(form.getBookingId(), dependentDTO);
                    }
                }
            }
            // Backend Validation: Check if every room has exactly 1 primary contact
            if (form.getAssignedRoomNumbers() != null && !form.getAssignedRoomNumbers().isEmpty()) {
                for (String roomNumber : form.getAssignedRoomNumbers()) {
                    Long detailId = roomNumberToDetailIdMap.get(roomNumber);
                    if (detailId != null) {
                        java.util.List<com.kawai.models.RoomGuest> guests = roomGuestRepo
                                .findByRoomBookingDetailId(detailId);
                        long primaryCount = guests.stream()
                                .filter(g -> Boolean.TRUE.equals(g.getIsPrimaryContact()))
                                .count();
                        if (primaryCount != 1) {
                            throw new com.kawai.exceptions.BusinessException("CHECKIN-006",
                                    "Phòng " + roomNumber + " phải có đúng 1 người đứng đầu!");
                        }
                    }
                }
            }

            // Bước 4: Phân bổ TourBookings vào phòng vật lý (nếu có)
            // Chỉ xử lý các TourBooking có roomBookingDetail == null (chưa phân bổ)
            String tourAllocationMode = form.getTourAllocationMode();
            if ("PER_TOUR".equalsIgnoreCase(tourAllocationMode)
                    && form.getTourAllocations() != null
                    && !form.getTourAllocations().isEmpty()) {
                for (TourRoomAllocationDTO allocation : form.getTourAllocations()) {
                    if (allocation.getTourBookingId() == null || allocation.getRoomNumber() == null
                            || allocation.getRoomNumber().isEmpty()) {
                        continue;
                    }
                    TourBooking tourBooking = tourBookingRepo.findById(allocation.getTourBookingId())
                            .orElse(null);
                    if (tourBooking == null) continue;

                    Long detailId = roomNumberToDetailIdMap.get(allocation.getRoomNumber());
                    if (detailId == null) {
                        log.warn("Không tìm thấy detail cho phòng {} khi phân bổ tour {}",
                                allocation.getRoomNumber(), allocation.getTourBookingId());
                        continue;
                    }
                    RoomBookingDetail detail = roomBookingDetailRepo.findById(detailId).orElse(null);
                    if (detail != null) {
                        tourBooking.setRoomBookingDetail(detail);
                        tourBookingRepo.save(tourBooking);
                        log.info("Phân bổ TourBooking {} vào phòng {} (detail {})",
                                tourBooking.getId(), allocation.getRoomNumber(), detailId);
                    }
                }
            }
            // Nếu ALL → giữ roomBookingDetail = null (gộp chung toàn booking, không gán phòng cụ thể)

            // Thành công: Gửi flash message và redirect
            redirectAttributes.addFlashAttribute("successMessage", "Check-in thành công !");
            return "redirect:/receptionist/check-in"; // Redirect về trang danh sách check-in

        } catch (BusinessException e) {
            org.springframework.transaction.interceptor.TransactionAspectSupport.currentTransactionStatus()
                    .setRollbackOnly();
            log.error("Lỗi nghiệp vụ khi Check-in: {}", e.getMessage());
            // Thất bại: Gửi thông báo lỗi và redirect lại trang form cũ
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/receptionist/check-in";
        } catch (Exception e) {
            org.springframework.transaction.interceptor.TransactionAspectSupport.currentTransactionStatus()
                    .setRollbackOnly();
            log.error("Lỗi hệ thống khi Check-in: ", e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Lỗi hệ thống! Vui lòng thử lại. Chi tiết: " + e.toString());
            return "redirect:/receptionist/check-in";
        }
    }
}
