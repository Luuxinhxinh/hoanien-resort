package com.kawai.controllers.web;

import com.kawai.dto.CheckinSubmitFormDTO;
import com.kawai.dto.DependentRegistrationDTO;
import com.kawai.exceptions.BusinessException;
import com.kawai.models.RoomBookingDetail;
import com.kawai.services.interfaces.CheckinService;
import com.kawai.services.interfaces.DependentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller xử lý Form Submit Web MVC cho Lễ tân Check-in (Không dùng fetch
 * API)
 */
@Controller
@RequestMapping("/receptionist/checkin")
public class ReceptionistCheckinWebController {

    private static final Logger log = LoggerFactory.getLogger(ReceptionistCheckinWebController.class);

    private final CheckinService checkinService;
    private final DependentService dependentService;
    private final com.kawai.repositories.RoomBookingDetailRepository roomBookingDetailRepo;
    private final com.kawai.repositories.RoomRepository roomRepo;

    public ReceptionistCheckinWebController(CheckinService checkinService,
            DependentService dependentService,
            com.kawai.repositories.RoomBookingDetailRepository roomBookingDetailRepo,
            com.kawai.repositories.RoomRepository roomRepo) {
        this.checkinService = checkinService;
        this.dependentService = dependentService;
        this.roomBookingDetailRepo = roomBookingDetailRepo;
        this.roomRepo = roomRepo;
    }

    @PostMapping("/complete")
    public String completeCheckin(@ModelAttribute CheckinSubmitFormDTO form, RedirectAttributes redirectAttributes) {
        log.info("Bắt đầu xử lý Form Check-in bulk. BookingId: {}, Số phòng gán: {}, Số người đi kèm: {}",
                form.getBookingId(),
                form.getAssignedRoomNumbers() != null ? form.getAssignedRoomNumbers().size() : 0,
                form.getDependents() != null ? form.getDependents().size() : 0);

        try {
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

            // Bước 1: Giao từng phòng (Mapping room -> detail)
            int roomIndex = 0;

            for (com.kawai.models.RoomBookingDetail detail : details) {
                if ("CHECKED_IN".equalsIgnoreCase(detail.getDetailStatus())) {
                    continue; // Bỏ qua phòng đã giao
                }
                if (roomIndex >= form.getAssignedRoomNumbers().size()) {
                    break; // Đã giao hết các phòng khách chọn trên UI
                }

                String roomNumber = form.getAssignedRoomNumbers().get(roomIndex);
                com.kawai.models.Room room = roomRepo.findByRoomNumber(roomNumber)
                        .orElseThrow(() -> new com.kawai.exceptions.BusinessException("CHECKIN-003",
                                "Không tìm thấy phòng số " + roomNumber));

                // Gọi service lõi để Check-in 1 phòng bằng roomId thực sự
                checkinService.checkIn(detail.getId(), room.getId());

                roomIndex++;
            }

            // Bước 2: THÊM NGƯỜI ĐI KÈM
            if (form.getDependents() != null) {
                for (com.kawai.dto.DependentRegistrationDTO dependentDTO : form.getDependents()) {
                    if (dependentDTO != null && dependentDTO.getFullName() != null && !dependentDTO.getFullName().trim().isEmpty()) {
                        dependentService.registerDependent(form.getBookingId(), dependentDTO);
                    }
                }
            }

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
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hệ thống! Vui lòng thử lại.");
            return "redirect:/receptionist/check-in";
        }
    }
}
