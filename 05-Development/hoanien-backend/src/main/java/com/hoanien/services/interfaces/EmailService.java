package com.hoanien.services.interfaces;

import com.hoanien.models.ConsolidatedInvoice;

public interface EmailService {
    /**
     * Gửi email đính kèm hóa đơn PDF cho khách hàng
     * 
     * @param toEmail           Địa chỉ email người nhận
     * @param invoice           Thông tin hóa đơn
     * @param pdfAttachmentPath Đường dẫn tới file PDF
     */
    void sendInvoiceEmail(String toEmail, ConsolidatedInvoice invoice, String pdfAttachmentPath);

    /**
     * Gửi email OTP xác nhận đăng ký tài khoản
     * 
     * @param toEmail  Địa chỉ email người nhận
     * @param otpCode  Mã OTP 6 số
     * @param fullName Tên người dùng
     */
    void sendRegistrationOtpEmail(String toEmail, String otpCode, String fullName);

    /**
     * Gửi email chứa link đặt lại mật khẩu
     * 
     * @param toEmail   Địa chỉ email người nhận
     * @param resetLink Link đặt lại mật khẩu
     * @param fullName  Tên người dùng
     */
    void sendPasswordResetEmail(String toEmail, String resetLink, String fullName);

    /**
     * Gửi email cảnh báo SLA cho supervisor
     */
    void sendSlaWarningEmail(String toEmail, String taskName, int pendingMinutes, String roomNumber);

    /**
     * Gửi email tùy chỉnh từ Workflow Engine
     */
    void sendCustomWorkflowEmail(String fromEmail, String toEmail, String subject, String htmlContent);

    /**
     * Gửi email xác nhận đặt tour thành công.
     */
    void sendBookingConfirmation(com.hoanien.models.TourBooking booking, com.hoanien.models.Customer customer);

    /**
     * Gửi email xác nhận đặt tour thành công với thông tin ghi nợ vào phòng.
     */
    void sendBookingConfirmation(com.hoanien.models.TourBooking booking, com.hoanien.models.Customer customer, boolean postToRoom, String roomDetail);

    /**
     * Gửi email xác nhận đặt tour thành công với thông tin thanh toán chi tiết.
     */
    void sendBookingConfirmation(com.hoanien.models.TourBooking booking, com.hoanien.models.Customer customer, String paymentMethod, String paymentType, String roomDetail);

    /**
     * Gửi email thông báo hủy tour kèm số tiền hoàn.
     */
    void sendCancellationNotice(com.hoanien.models.TourBooking booking, com.hoanien.models.Customer customer, java.math.BigDecimal refundAmount, boolean cancelledByResort, String reason);

    /**
     * Gửi email xác nhận đặt món tại phòng (Room Service).
     */
    void sendRoomServiceConfirmation(com.hoanien.models.FoodOrder order, com.hoanien.models.Customer customer, String roomNumber);

    /**
     * Gửi email xác nhận đặt bàn (Table Reservation).
     */
    void sendTableBookingConfirmation(com.hoanien.models.TableReservation reservation, com.hoanien.models.Customer customer);

    /**
     * Gửi email thông báo gia hạn giữ bàn.
     */
    void sendExtendTableHold(com.hoanien.models.TableReservation reservation, com.hoanien.models.Customer customer, int extendMinutes, String latestCheckInTime);

    /**
     * Gửi email thông báo hủy bàn.
     */
    void sendCancelTableBooking(com.hoanien.models.TableReservation reservation, com.hoanien.models.Customer customer, String cancelledBy, String reason);

    /**
     * Gửi email thông báo hủy đơn hàng dịch vụ ẩm thực (F&B).
     */
    void sendCancelFoodOrderEmail(com.hoanien.models.FoodOrder order, com.hoanien.models.Customer customer, String cancelledBy, String reason, java.math.BigDecimal refundAmount, String bankName, String accountName, String accountLast3);

    /**
     * Gửi email thông báo hủy bàn tự động do khách trả phòng (Check-out).
     */
    void sendTableCancellationDueToCheckoutEmail(com.hoanien.models.TableReservation reservation, com.hoanien.models.Customer customer);
    /**
     * Gửi email HTML thô (phục vụ test preview).
     */
    void sendEmail(String toEmail, String subject, String htmlContent);

    /**
     * Gửi email thông báo hoàn tiền thành công kèm biên lai (ủy nhiệm chi).
     */
    void sendRefundSuccessEmail(com.hoanien.models.RefundRequest refundRequest, com.hoanien.models.Customer customer, String absoluteAttachmentPath);

    /**
     * Gửi email cảm ơn và hướng dẫn đánh giá sau khi tour kết thúc.
     */
    void sendTourFeedbackEmail(com.hoanien.models.TourBooking booking, com.hoanien.models.Customer customer);

    /**
     * Gửi email thông báo khi có phản hồi đánh giá từ tour tới khách hàng.
     */
    void sendFeedbackReplyEmail(com.hoanien.models.Review review, com.hoanien.models.Customer customer);

    /**
     * Gửi email thông báo khởi hành tour kèm lịch trình hoạt động chi tiết.
     */
    void sendTourDepartureEmail(com.hoanien.models.TourBooking booking, com.hoanien.models.Customer customer,
            java.util.List<com.hoanien.models.TourItineraryDetail> activities);

    /**
     * Gửi email thông báo nâng cấp tài khoản cho người phụ thuộc.
     */
    void sendDependentUpgradeEmail(com.hoanien.models.Customer masterCustomer, com.hoanien.models.Customer newCustomer, String username, String password);

    /**
     * Gửi email thông báo hủy đặt phòng (có hoặc không hoàn tiền).
     */
    void sendRoomCancellationEmail(com.hoanien.models.RoomBooking booking, com.hoanien.models.Customer customer, boolean isRefundable);

    /**
     * Gửi email thông báo đơn đặt phòng bị hủy tự động do khách không tới (No_Show).
     */
    void sendRoomNoShowEmail(com.hoanien.models.RoomBooking booking, com.hoanien.models.Customer customer);

    /**
     * Gửi email xác nhận Walk-in check-in.
     */
    void sendWalkInCheckInEmail(com.hoanien.models.RoomBooking booking, com.hoanien.models.RoomBookingDetail detail, com.hoanien.models.Customer customer, boolean isNewAccount, String username, String password);

    void sendProfileUpdateEmail(com.hoanien.models.Customer customer);

    /**
     * Gửi email thông báo lịch làm việc hàng tuần cho nhân viên.
     */
    void sendWeeklyScheduleEmail(String toEmail, String employeeName, java.util.List<com.hoanien.models.StaffSchedule> schedules);

    /**
     * Gửi email xác nhận đặt phòng thành công (sau khi thanh toán thành công).
     */
    void sendRoomBookingConfirmation(com.hoanien.models.RoomBooking booking, com.hoanien.models.Customer customer);
}
