package com.kawai.controllers.web;

import com.kawai.dto.CheckinSubmitFormDTO;
import com.kawai.dto.DependentRegistrationDTO;
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
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER', 'OP_RECEPTION_CHECKIN')")
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
    public String completeCheckin(@ModelAttribute CheckinSubmitFormDTO form, org.springframework.validation.BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            log.error("Lỗi binding dữ liệu form: {}", bindingResult.getAllErrors());
            redirectAttributes.addFlashAttribute("errorMessage", "Dữ liệu nhập vào không hợp lệ. Vui lòng kiểm tra lại (đặc biệt là ngày tháng).");
            return "redirect:/receptionist/check-in";
        }
        
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
                if (form.getDob() != null) {
                    customer.setBirthDate(form.getDob());
                }
                if (form.getGender() != null && !form.getGender().trim().isEmpty()) {
                    customer.setGender(form.getGender().trim());
                }
                // Save customer FaceID if provided in form
                if (form.getFaceVectorData() != null && !form.getFaceVectorData().isEmpty()) {
                    customer.setFaceVectorData(form.getFaceVectorData());
                }
                if (form.getFaceImageBase64() != null && !form.getFaceImageBase64().isEmpty()) {
                    try {
                        String[] parts = form.getFaceImageBase64().split(",");
                        String imageString = parts.length > 1 ? parts[1] : parts[0];
                        byte[] imageBytes = java.util.Base64.getDecoder().decode(imageString);

                        String fileName = "cust_" + customer.getId() + "_" + System.currentTimeMillis() + ".jpg";
                        java.nio.file.Path uploadPath = java.nio.file.Paths
                                .get(com.kawai.utils.UploadPathResolver.resolvePath("uploads/faces"));
                        if (!java.nio.file.Files.exists(uploadPath)) {
                            java.nio.file.Files.createDirectories(uploadPath);
                        }
                        java.nio.file.Path filePath = uploadPath.resolve(fileName);
                        java.nio.file.Files.write(filePath, imageBytes);
                        String publicUrl = "/uploads/faces/" + fileName;

                        customer.setFaceImgUrl(publicUrl);
                    } catch (Exception e) {
                        log.error("Failed to save FaceID image for customer {}", customer.getId(), e);
                    }
                }

                customerRepo.save(customer);
            }

            // Chuyển toàn bộ logic xử lý phòng, dependent, hạn mức, TourBooking sang
            // Service
            checkinService.processBulkCheckin(form, customer, booking);

            // Thành công: Gửi flash message và redirect
            redirectAttributes.addFlashAttribute("successMessage", "Check-in thành công !");
            return "redirect:/receptionist/check-in"; // Redirect về trang danh sách check-in

        } catch (BusinessException e) {
            log.error("Lỗi nghiệp vụ khi Check-in: {}", e.getMessage());
            // Thất bại: Gửi thông báo lỗi và redirect lại trang form cũ
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/receptionist/check-in";
        } catch (Exception e) {
            log.error("Lỗi hệ thống khi Check-in: ", e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Lỗi hệ thống! Vui lòng thử lại. Chi tiết: " + e.toString());
            return "redirect:/receptionist/check-in";
        }
    }

    @PostMapping("/upgrade-dependent/{dependentId}")
    @org.springframework.web.bind.annotation.ResponseBody
    public org.springframework.http.ResponseEntity<?> upgradeDependentToCustomer(
            @org.springframework.web.bind.annotation.PathVariable Long dependentId) {
        try {
            java.util.Map<String, Object> result = checkinService.upgradeDependentToCustomer(dependentId);
            com.kawai.models.Customer customer = (com.kawai.models.Customer) result.get("customer");
            String username = (String) result.get("username");
            String password = (String) result.get("password");

            // Lấy thông tin phòng để trả về UI (Logic unlink RoomGuest đã được làm trong
            // Service)
            com.kawai.models.RoomGuest rg = roomGuestRepo.findByCustomerIdAndGuestType(customer.getId(), "ADULT")
                    .orElse(null);

            String roomInfo = "";
            if (rg != null && rg.getRoomBookingDetail() != null) {
                com.kawai.models.RoomBookingDetail detail = rg.getRoomBookingDetail();
                String categoryName = detail.getCategory() != null ? detail.getCategory().getCategoryName()
                        : "Không xác định";
                String roomNumber = detail.getRoom() != null ? detail.getRoom().getRoomNumber() : "Chưa xếp phòng";
                roomInfo = " (Hạng phòng: " + categoryName + " - Số phòng: " + roomNumber + ")";
            }

            return org.springframework.http.ResponseEntity.ok(java.util.Map.of(
                    "success", true,
                    "message", "Nâng cấp thành công Khách hàng: " + customer.getFullName() + roomInfo + ". Tài khoản: "
                            + username + " - Mật khẩu: " + password));
        } catch (Exception e) {
            log.error("Error upgrading dependent", e);
            return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }
}
