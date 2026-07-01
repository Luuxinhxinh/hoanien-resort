# ADR-05: Thiết kế Tác vụ chạy ngầm & Lập lịch tự động (Task Scheduling & Background Jobs)

* **Trạng thái:** Confirmed
* **Tác giả:** Nhóm phát triển Kawai Retreat
* **Ngày quyết định:** 2026-07-01
* **Lĩnh vực:** Lập lịch tác vụ, Kiểm toán đêm, Idempotency

---

## 1. Bối cảnh (Context)

Một resort vận hành 24/7 đòi hỏi rất nhiều tác vụ tự động chạy ngầm (background jobs) để giảm tải cho con người và hạn chế sai sót:
1. **Kiểm toán đêm (Night Audit):** Chạy vào lúc 02:00 AM hàng ngày để tự động chốt doanh thu ngày cũ, ghi nhận tiền phòng của khách đang lưu trú vào folio (Room Charge posting), và chuyển ngày kinh doanh của resort sang ngày mới. Đây là tác vụ mang tính sống còn đối với tài chính của khách sạn.
2. **Hủy booking hết hạn giữ chỗ:** Tự động giải phóng các phòng bị khóa tạm thời (`RoomBookingDetail`) nếu khách hàng không thanh toán cọc trong vòng 15 phút kể từ lúc đặt.
3. **Hủy chuyến lữ hành dưới ngưỡng:** 24 giờ trước khi khởi hành Tour, hệ thống phải kiểm tra số lượng khách đăng ký. Nếu nhỏ hơn `min_capacity` quy định, tự động hủy tour, sinh yêu cầu hoàn tiền 100% (`RefundRequest`), và gửi email xin lỗi khách hàng.

Nếu các background jobs này bị lỗi giữa chừng, chạy trùng lặp (ví dụ: post tiền phòng 2 lần trong 1 đêm), hoặc không thể khôi phục khi ứng dụng khởi động lại, hậu quả sẽ cực kỳ nghiêm trọng đối với hoạt động kinh doanh và tài chính.

## 2. Quyết định (Decision)

Chúng tôi quyết định thiết lập kiến trúc lập lịch tác vụ nền như sau:

### 2.1 Sử dụng Spring Scheduler kết hợp DynamicJobManager
* **Tác vụ chu kỳ cố định:** Sử dụng annotation `@Scheduled` của Spring Core cho các job quét định kỳ đơn giản (như quét hủy booking quá hạn 15 phút chạy 5 phút/lần).
* **Tác vụ động (Dynamic Scheduling):** Xây dựng dịch vụ `DynamicJobManager` bao gói `ThreadPoolTaskScheduler` của Spring. Cho phép Lễ tân/Quản lý lập lịch, hủy lịch hoặc thay đổi thời gian chạy của một tác vụ cụ thể (như lịch dọn phòng, lịch bảo trì phòng, giờ xuất phát của một xe Tour) trực tiếp từ giao diện Admin.

### 2.2 Đảm bảo tính Idempotency cho Kiểm toán đêm (Night Audit)
* Quy trình Night Audit phải được thiết kế dạng **Idempotent** — bảo đảm chạy nhiều lần cho cùng một ngày kinh doanh vẫn cho ra kết quả duy nhất, không tạo ra giao dịch trùng lặp.
* Sử dụng bảng kiểm soát trạng thái `NightAuditHistory` làm chốt chặn. Trước khi chạy bất kỳ bước nào (ví dụ: `postRoomCharges()`, `closeBusinessDay()`), hệ thống phải kiểm tra trạng thái của ngày kinh doanh hiện tại. Nếu bước đó đã thành công, hệ thống lập tức bỏ qua (Bypass).
* Toàn bộ quy trình chạy trong cơ chế checkpoint:
  ```java
  public void executeNightAudit(LocalDate businessDate) {
      if (!auditHistoryRepository.isStepCompleted(businessDate, "POST_ROOM_CHARGES")) {
          postRoomCharges(businessDate);
          auditHistoryRepository.markStepCompleted(businessDate, "POST_ROOM_CHARGES");
      }
      if (!auditHistoryRepository.isStepCompleted(businessDate, "CLOSE_DAY")) {
          closeBusinessDay(businessDate);
          auditHistoryRepository.markStepCompleted(businessDate, "CLOSE_DAY");
      }
  }
  ```

### 2.3 Cơ chế phục hồi sự cố (Disaster Recovery & Self-Healing)
* Khi server bị tắt đột ngột (crash/restart) khi job đang chạy, hệ thống ghi nhận trạng thái Job là `FAILED` hoặc `INTERRUPTED` trong bảng `JobExecutionHistory`.
* Khi ứng dụng khởi động lại (Startup listener), hệ thống tự động quét bảng `JobExecutionHistory` để tìm các job quan trọng chưa hoàn thành và kích hoạt chạy lại (Resume) từ checkpoint bị lỗi cuối cùng.

### 2.4 Cấu hình Thread Pool chuyên dụng
* Cấm sử dụng Single-threaded Scheduler mặc định của Spring cho các tác vụ nặng.
* Cấu hình một `TaskScheduler` thread pool chuyên dụng với kích thước tối thiểu là 5 threads, đảm bảo các tác vụ dài hơi (như xuất file báo cáo tài chính lớn hoặc gửi email hàng loạt) không block các tác vụ thời gian thực ngắn (như hủy phòng giữ chỗ 15 phút).

## 3. Hệ quả (Consequences)

### Tích cực (Pros)
* **Tự động hóa an toàn:** Toàn bộ nghiệp vụ vận hành đêm và kiểm soát tài chính diễn ra trơn tru mà không cần sự can thiệp thủ công của nhân viên trực đêm.
* **Độ tin cậy cao:** Khả năng idempotency và self-healing giúp hệ thống phục hồi nhanh chóng sau sự cố phần cứng mà không làm sai lệch số liệu doanh thu.
* **Theo dõi trực quan:** Admin có thể giám sát hiệu năng và lịch sử chạy của tất cả các background jobs thông qua màn hình Audit Log trên trang quản trị.

### Hạn chế (Cons)
* **Tiêu tốn tài nguyên DB Connection:** Các job chạy định kỳ liên tục thực hiện truy vấn SELECT/UPDATE có thể làm cạn kiệt Connection Pool của Database.
  * *Biện pháp:* Tối ưu hóa câu lệnh SQL, sử dụng chỉ mục (Indexes) hợp lý và giải phóng connection ngay sau khi hoàn thành tác vụ con.
* **Khó khăn khi scale ngang (Clustering):** Nếu chạy nhiều instance của ứng dụng Spring Boot trên các server khác nhau, job có thể bị chạy trùng lặp ở cả hai server.
  * *Định hướng tương lai:* Nếu triển khai hệ thống đa instance (Clustered Environment), bắt buộc phải tích hợp **ShedLock** hoặc **Quartz Scheduler** sử dụng database lock để đảm bảo chỉ có duy nhất 1 instance được thực thi job tại một thời điểm.
