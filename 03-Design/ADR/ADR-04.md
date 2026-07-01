# ADR-04: Chiến lược Quản lý Transaction và Khóa dữ liệu (Transaction & Locking Strategy)

* **Trạng thái:** Confirmed
* **Tác giả:** Nhóm phát triển Kawai Retreat
* **Ngày quyết định:** 2026-07-01
* **Lĩnh vực:** Quản lý Giao dịch, Toàn vẹn Dữ liệu, Hiệu năng

---

## 1. Bối cảnh (Context)

Dự án Kawai Retreat tuân thủ nghiêm ngặt mô hình thiết kế **CASE 2.0 (Service-first)**. Theo đó, Controller chỉ đóng vai trò định tuyến và tiếp nhận đầu vào, mọi logic nghiệp vụ (business logic) phải được thực hiện tại tầng Service, sử dụng DTO thay thế Entity trực tiếp khi giao tiếp với bên ngoài.

Tuy nhiên, các nghiệp vụ đặc thù của ngành dịch vụ khách sạn rất phức tạp và liên đới nhiều bảng cơ sở dữ liệu:
* **Tạo nhân sự mới:** Yêu cầu INSERT đồng thời vào bảng `accounts` và `employees`. Nếu một bảng lỗi, bảng kia phải được rollback để tránh tạo ra tài khoản mồ côi (orphan accounts).
* **Quy trình Check-in:** Yêu cầu cập nhật trạng thái phòng vật lý, lưu thông tin khách lưu trú thực tế (`RoomGuest`), tạo hoặc cập nhật folio ký nợ.
* **Đặt chỗ tạm thời (Hold Room):** Đóng khóa phòng trong 15 phút để khách hàng hoàn thành thanh toán. Yêu cầu tính đồng thời cao, tránh bán vượt số lượng phòng (overbooking).

Nếu không quản lý transaction và lock dữ liệu chặt chẽ, hệ thống sẽ gặp các lỗi nghiêm trọng về mất nhất quán dữ liệu (Dirty Read, Non-repeatable Read, Phantom Read) hoặc deadlock cơ sở dữ liệu.

## 2. Quyết định (Decision)

Chúng tôi quyết định áp dụng các nguyên tắc quản lý giao dịch nghiệp vụ sau:

### 2.1 Cấu hình Giao dịch Giao tiếp (Transactional Propagation & Rollback)
* Chỉ đặt khai báo `@Transactional` tại tầng **Service Impl**, tuyệt đối không đặt tại tầng Controller hay Repository.
* Luôn chỉ định rõ thuộc tính `rollbackFor` để đảm bảo Spring rollback giao dịch đối với cả unchecked exceptions và checked exceptions:
  ```java
  @Service
  @Transactional(rollbackFor = Exception.class)
  public class AccountServiceImpl implements AccountService { ... }
  ```
* Mặc định sử dụng `Propagation.REQUIRED` cho tất cả các service viết (write operations) để gom tất cả các bước con vào trong một transaction duy nhất được khởi tạo ở service cha.
* Đối với các service chỉ đọc (read-only operations), khai báo `@Transactional(readOnly = true)` để Hibernate tối ưu hóa bộ nhớ cache và bỏ qua bước kiểm tra thay đổi (dirty checking), giúp tăng tốc độ truy vấn.

### 2.2 Xử lý Khóa Dữ liệu Đồng thời (Concurrency Locking)
Chúng tôi phối hợp linh hoạt giữa hai cơ chế khóa dữ liệu:

1. **Khóa Lạc quan (Optimistic Locking - Khuyến nghị chính):**
   * Sử dụng cột `@Version` trên các thực thể trạng thái (`Room`, `Booking`, `TableReservation`).
   * Tránh gây nghẽn kết nối cơ sở dữ liệu, tối ưu cho các luồng nghiệp vụ ít xảy ra va chạm ghi đồng thời.
2. **Khóa Bi quan (Pessimistic Locking - Trường hợp đặc biệt):**
   * Sử dụng `LockModeType.PESSIMISTIC_WRITE` thông qua annotation `@Lock` của Spring Data JPA đối với luồng giữ chỗ tạm thời (`RoomBookingDetail`) khi khách hàng bấm "Đặt phòng":
     ```java
     @Lock(LockModeType.PESSIMISTIC_WRITE)
     @Query("SELECT rbd FROM RoomBookingDetail rbd WHERE rbd.id = :id")
     Optional<RoomBookingDetail> findAndLockById(@Param("id") Long id);
     ```
   * Giúp giữ phòng ngay lập tức và ngăn chặn các transaction khác đọc/ghi vào bản ghi này cho đến khi transaction hiện tại commit hoặc rollback.
   * Thiết lập thời hạn timeout cho khóa bi quan (`javax.persistence.lock.timeout` = 3000ms) để giải phóng kết nối nhanh chóng nếu có sự cố xảy ra.

### 2.3 Phân tách Logic Ngoại lệ Nghiệp vụ (Business Exception Handling)
* Sử dụng các ngoại lệ tùy chỉnh tự định nghĩa (Custom Runtime Exceptions) như `RoomNotAvailableException`, `InsufficientCreditLimitException`, `InvalidVoucherException` để điều khiển luồng rollback nghiệp vụ.
* Tầng Controller sử dụng `@ControllerAdvice` để đón các ngoại lệ này và trả về mã lỗi HTTP tương ứng (ví dụ: 400 Bad Request) thay vì trả về lỗi hệ thống 500 Internal Server Error làm lộ cấu trúc hệ thống.

## 3. Hệ quả (Consequences)

### Tích cực (Pros)
* **Tính nhất quán dữ liệu tuyệt đối:** Đảm bảo nguyên tắc Atomicity của ACID. Không bao giờ tồn tại bản ghi rác hoặc rỗng nửa vời trong database.
* **An toàn khi nâng tải:** Hệ thống tự tin xử lý hàng trăm request đặt phòng/đặt bàn đồng thời mà không bị overbooking.
* **Tối ưu hóa hiệu năng đọc:** Nhờ việc áp dụng `readOnly = true` cho các API xem dữ liệu, dashboard báo cáo chạy mượt mà hơn.

### Hạn chế (Cons)
* **Nguy cơ Deadlock:** Nếu thiết kế các bước ghi dữ liệu không theo đúng thứ tự ưu tiên giữa các luồng chạy song song, DB dễ xảy ra Deadlock.
  * *Biện pháp:* Đảm bảo quy tắc ghi dữ liệu luôn đi từ cha (`Booking`) đến con (`RoomBookingDetail`), không ghi ngược hoặc chéo.
* **Hạn chế băng thông ghi:** Việc sử dụng Pessimistic Lock làm chậm tốc độ đáp ứng của API đặt phòng do các thread phải xếp hàng chờ đợi. Cần giám sát kỹ thông số Connection Pool để cấu hình tăng tải phù hợp.
