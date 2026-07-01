# ADR-02: Thiết kế Database Schema, Ràng buộc dữ liệu & Chiến lược Audit

* **Trạng thái:** Confirmed
* **Tác giả:** Nhóm phát triển Kawai Retreat
* **Ngày quyết định:** 2026-07-01
* **Lĩnh vực:** Cơ sở dữ liệu, Bảo mật, Audit Log

---

## 1. Bối cảnh (Context)

Hệ thống quản lý khu nghỉ dưỡng Kawai Retreat & Resort sở hữu cơ sở dữ liệu phức tạp gồm 49 JPA Entities có mối quan hệ chặt chẽ. Hệ thống phục vụ đồng thời khách hàng đặt phòng trực tuyến và nhân sự vận hành tại resort (Lễ tân, F&B, Buồng phòng, Quản lý). 

Các thách thức kỹ thuật đặt ra:
1. **Truy vết thay đổi dữ liệu:** Bắt buộc phải ghi nhận chính xác ai (nhân viên nào), vào lúc nào, từ IP nào đã thay đổi các dữ liệu nhạy cảm (như trạng thái phòng, đổi tiền phòng, sửa hóa đơn, duyệt hoàn tiền).
2. **Tránh tranh chấp tài nguyên (Race Condition):** Xảy ra khi hai khách hàng cùng lúc cố gắng đặt một phòng cuối cùng, hoặc lễ tân cập nhật trạng thái phòng khi hệ thống dọn phòng đang hoàn tất task.
3. **Bảo mật dữ liệu nhạy cảm:** Số định danh cá nhân (CCCD/Passport) của khách hàng không được lưu trữ dưới dạng văn bản rõ (plain text) nhằm tuân thủ quy định bảo vệ dữ liệu.

## 2. Quyết định (Decision)

Chúng tôi quyết định triển khai các giải pháp kiến trúc cơ sở dữ liệu sau:

### 2.1 Chiến lược Audit dữ liệu tự động với Hibernate Envers & CustomRevisionEntity
* Sử dụng thư viện **Hibernate Envers** để tự động tạo ra các bảng audit (`*_AUD`) cho mọi thực thể được đánh dấu `@Audited`.
* Xây dựng thực thể tùy chỉnh `CustomRevisionEntity` kế thừa `DefaultRevisionEntity` để lưu thêm thông tin ngữ cảnh nghiệp vụ:
  ```java
  @Entity
  @Table(name = "revinfo")
  @RevisionEntity(CustomRevisionListener.class)
  public class CustomRevisionEntity extends DefaultRevisionEntity {
      private String username;
      private String ipAddress;
      // Getters & Setters
  }
  ```
* Triển khai `CustomRevisionListener` kết nối với Spring Security Context để lấy thông tin nhân viên đăng nhập hiện tại và IP client thực hiện transaction.
* Đối với các hành động mang tính chất nghiệp vụ rõ ràng (như xuất dữ liệu Excel/PDF), hệ thống ghi trực tiếp vào bảng `ExportHistory` để dễ dàng hiển thị trên dashboard quản trị.

### 2.2 Phòng chống Race Condition bằng Optimistic Locking
* Áp dụng cơ chế **Optimistic Locking** thông qua annotation `@Version` của JPA trên các thực thể cốt lõi:
  * `Booking`, `RoomBookingDetail`: Tránh trùng lịch đặt phòng.
  * `Room`: Tránh xung đột cập nhật trạng thái dọn phòng / bảo trì.
  * `FoodOrder`, `TableReservation`: Tránh đặt bàn trùng giờ.
* Khi có xung đột cập nhật, Spring Boot sẽ ném ra `ObjectOptimisticLockingFailureException`. Hệ thống sẽ bắt exception này ở tầng Controller/Service để thông báo lỗi thân thiện cho người dùng hoặc tự động retry (đối với luồng đặt chỗ tạm thời).

### 2.3 Ràng buộc Toàn vẹn & Chỉ mục (Indexes)
* **Khóa duy nhất (Unique constraints):**
  * `account.username` và `account.email` để đảm bảo định danh tài khoản duy nhất.
  * `authorized_device.device_code` để xác định duy nhất thiết bị nhân viên.
  * `customer.cccd_encrypted` để ngăn một khách hàng tạo nhiều tài khoản ảo.
* **Chỉ mục hiệu năng (Indexes):**
  * Đánh index trên cột `room_booking_detail.check_in_date` và `check_out_date` để tăng tốc độ truy vấn tìm phòng trống.
  * Đánh index trên `folio_item.folio_id` để tối ưu hóa thời gian tính toán hóa đơn check-out.

### 2.4 Mã hóa Dữ liệu Nhạy cảm tại tầng JPA
* Áp dụng **AttributeConverter** của JPA để tự động mã hóa AES-256 các cột `cccd` / `passport` của thực thể `Customer` và `Dependent` trước khi INSERT xuống Database, và tự động giải mã khi SELECT lên.
* Khóa mật mã hóa được quản lý thông qua biến môi trường của hệ thống ứng dụng, không lưu cứng trong code.

## 3. Hệ quả (Consequences)

### Tích cực (Pros)
* **Truy vết tuyệt đối:** Mọi hành vi sửa đổi dữ liệu nhạy cảm đều có thể đối soát ngược lại người dùng và địa chỉ IP cụ thể, tăng tính minh bạch và an toàn cho hệ thống.
* **Không bị ghi đè mất dữ liệu:** Cơ chế Optimistic Lock đảm bảo tính toàn vẹn của giao dịch, loại bỏ nguy cơ trùng lặp phòng hoặc bàn ăn.
* **Tuân thủ bảo mật:** Dữ liệu cá nhân khách hàng được bảo vệ an toàn ngay cả khi database bị rò rỉ.

### Hạn chế (Cons)
* **Phình to dung lượng:** Các bảng `*_AUD` sẽ lưu trữ lịch sử vô hạn làm dung lượng database tăng nhanh.
  * *Biện pháp khắc phục:* Thiết lập scheduler chạy định kỳ vào mùa thấp điểm để nén và di chuyển dữ liệu audit cũ (trên 1 năm) sang kho lưu trữ lạnh (Cold Storage).
* **Ảnh hưởng hiệu năng nhỏ:** Quá trình mã hóa/giải mã AES-256 và ghi audit làm tăng nhẹ độ trễ ghi dữ liệu (khoảng 2-5ms mỗi transaction). Tuy nhiên, mức ảnh hưởng này hoàn toàn chấp nhận được so với lợi ích bảo mật mang lại.
