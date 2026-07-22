---
name: db-seeding-integrity
description: Đảm bảo tính toàn vẹn nghiệp vụ và nhất quán dữ liệu khi thêm, chỉnh sửa hoặc nạp dữ liệu khởi tạo (seeding data) vào cơ sở dữ liệu.
---

# Quy Tắc Đảm Bảo Tính Nhất Quán Dữ Liệu Khởi Tạo (Seeding Data Integrity)

Mỗi khi bạn thực hiện thêm mới, chỉnh sửa hoặc tối ưu hóa các tệp dữ liệu khởi tạo của dự án (như dữ liệu SQL nạp ban đầu khi khởi động), bạn **bắt buộc** phải tuân thủ các quy tắc nghiệp vụ sau để tránh tình trạng dữ liệu "lệch pha" hoặc xung đột logic:

## 1. NGUYÊN TẮC TOÀN VẸN NGHIỆP VỤ (BUSINESS LOGIC INTEGRITY):
- **Nhất quán giá trị Enum/Key định danh:** Mọi mã định danh (ví dụ `tour_type`: `doantu`, `dongnoi`...) hoặc giá trị cờ nghiệp vụ (`is_insurance_required = TRUE`) sử dụng ở Backend (Controller/Service) và Frontend (JS/Thymeleaf) bắt buộc phải nhất quán 100% với dữ liệu được khai báo trong `data.sql` và các file Java Seeder. Tuyệt đối không dùng dữ liệu giả/legacy (như `CULTURAL`, `RELAX`...) trong seeder, tránh việc hệ thống không truy vấn được bản ghi phù hợp dẫn đến ẩn giao diện hoặc crash nghiệp vụ.
- **Luồng phê duyệt (Approval Workflows):** Nếu thêm một thực thể ở trạng thái chờ duyệt (ví dụ: `Pending_Approval`), bắt buộc phải có bản ghi tương ứng trong bảng theo dõi vận hành/phê duyệt của người quản lý (ví dụ: `Hotel_Operations` với loại `Manager_Approval`) ở trạng thái chờ xử lý (`Pending`).
- **Luồng hoàn tiền (Refund Workflows):** Nếu thêm một yêu cầu hoàn tiền (`RefundRequest`), thực thể giao dịch hoặc đơn hàng gốc liên kết (ví dụ: `Booking`, `FoodOrder`, `TourBooking`) **phải được đặt ở trạng thái đã hủy (`Cancelled`)** để phản ánh đúng thực tế tài chính và nghiệp vụ.
- **Mã giảm giá (Promotions):** Số lần sử dụng thực tế phải khớp hoặc được kiểm soát hợp lý so với số lượng đơn hàng liên kết đã áp dụng mã đó.
- **Toàn vẹn Dữ liệu Cấp dưới (Parent-Child Completeness):** Khi thiết lập một thực thể cha ở các trạng thái đã xử lý / quá khứ (ví dụ: `Checked_Out`, `Cancelled`, `Completed`), **BẮT BUỘC** phải đảm bảo thực thể đó đã được khai báo đầy đủ các bản ghi ở bảng con liên đới (ví dụ: `Room_Bookings`, `Room_Booking_Details`). Việc thiếu dữ liệu chi tiết sẽ gây ra lỗi `NullPointerException` nghiêm trọng trên UI hoặc Business Logic.
- **Toàn vẹn Dữ liệu Tham chiếu ngược (Reverse Referential Integrity):** Khi xóa hoặc ẩn một bản ghi cha trong dữ liệu mồi (ví dụ: xóa một `Booking` cũ), **BẮT BUỘC** phải reset các giá trị tham chiếu tại các bảng liên đới bị ảnh hưởng (ví dụ: trả `room_status` về `Vacant_Clean` và `current_booking_detail_id` về `NULL` trong bảng `Rooms`). Việc để sót "rác dữ liệu" (orphaned foreign keys) sẽ gây ra lỗi thiếu hụt tài nguyên ảo (VD: hết phòng dù thực tế không có booking).
- **Mô phỏng chính xác thao tác codebase — QUY TRÌNH BẮT BUỘC:**

  > **Nguyên tắc cốt lõi:** Mọi trường text tự do (`notes`, `description`, `message`...) trong seed data BẮT BUỘC phải là chuỗi được **sao chép nguyên văn từ source Java** — tức là chuỗi mà service/controller thực sự `setNotes(...)` khi chạy trong production. **KHÔNG được tự sáng tạo nội dung, dù có chứa keyword đúng.**

  **Quy trình ĐÚNG khi viết notes cho seed data:**
  1. **Đọc source code trước** — Tìm service/controller tạo ra loại bản ghi đó (ví dụ: `CHECKOUT_CLEAN` → đọc `HousekeepingServiceImpl.java`)
  2. **Tìm chính xác dòng `setNotes(...)`** — Copy nguyên văn chuỗi đó, kể cả format `\n`, `[TAG]` nếu có
  3. **Paste vào seed** — Không thêm bớt chữ nào

  **Lý do:** Chuỗi notes không chỉ là text hiển thị — chúng còn được **parse bởi JS regex** (`/^\[([^\]]+)\]([\s\S]*)$/`), **check bởi Java logic** (`priority == 'Lễ tân báo dọn khẩn'`), và **trigger UI state**. Viết sai format = tính năng im lặng không hoạt động, không có lỗi compile để phát hiện.

  **❌ LỖI HAY GẶP — "Chém gió có keyword":** Agent viết notes trông _có vẻ_ đúng vì chứa từ `[Check-out]`, `[Arrival]` nhưng thêm nội dung bịa đặt:
  ```
  -- SAI: tự thêm "đón đoàn 2h chiều", "giỏ trái cây", "quên sạc điện thoại"
  '[Check-out] Khách phòng 102 vừa trả phòng, dọn gấp để đón đoàn 2h chiều.'
  '[Arrival] Khách VIP sắp nhận phòng, chuẩn bị sẵn giỏ trái cây tươi trên bàn.'
  ```
  **✅ ĐÚNG — lấy nguyên văn từ `HousekeepingServiceImpl.java` và `HousekeepingApiController.java`:**
  ```
  '[Check-out] Dọn phòng sau khi khách trả phòng.'
  '[Arrival] Dọn phòng khẩn để đón khách nhận phòng. \n[Khẩn cấp] Lễ tân hối thúc dọn ưu tiên để khách Check-in!'
  'Lễ tân yêu cầu dọn phòng khẩn cấp.'
  '[Khách Yêu Cầu Dọn Phòng] - <nội dung khách nhập>'
  '\n[Ghi chú hoàn thành]: <nội dung staff nhập>'
  '\n[Tạm dừng]: <lý do>'
  '\n[Đã sửa]: <nội dung>'
  ```

- **Quy tắc đặc biệt cho cột `notes` của bảng `Hotel_Operations`:**
  Đây là quy tắc CỨNG, PHẢI ĐỌC KỸ TRƯỚC KHI VIẾT NOTES. Cột `notes` KHÔNG phải là ô để kể chuyện hay mô tả tình huống.
  
  **Cơ chế hoạt động thực tế:** Cột `notes` được đọc bởi hàm JavaScript `applyGuestNoteBadges()` trong `housekeeping/dashboard.html`. Hàm này dùng regex `^\[([^\]]+)\]([\s\S]*)$` để bắt phần `[TAG]` ở đầu chuỗi và tô màu badge tự động.
  
  **Bảng keyword → màu badge (do JS quyết định, KHÔNG THAY ĐỔI ĐƯỢC):**
  | Keyword trong `[TAG]` | Màu badge | Icon |
  |---|---|---|
  | `arrival` / `checkin` / `check-in` | 🟤 Sand (pearl) | fa-user-check |
  | `checkout` / `check-out` | 🟡 Gold bronze | fa-suitcase-rolling |
  | `stayover` / `stay-over` / `stay` | 🩶 Grey | fa-bed |
  | `urgent` / `khẩn` | 🔴 Red | fa-triangle-exclamation |

  **Các chuỗi được phép dùng (lấy NGUYÊN VĂN từ Java source):**
  - `[Check-out] Dọn phòng sau khi khách trả phòng.` — task `CHECKOUT_CLEAN` mới sinh
  - `[Arrival] Dọn phòng khẩn để đón khách nhận phòng. \n[Khẩn cấp] Lễ tân hối thúc dọn ưu tiên để khách Check-in!` — task `URGENT_CLEAN` do lễ tân escalate (`HousekeepingServiceImpl.java:159,172`)
  - `Lễ tân yêu cầu dọn phòng khẩn cấp.` — task `URGENT_CLEAN` tạo mới (`HousekeepingApiController.java:218`)
  - `[Khách Yêu Cầu Dọn Phòng] - <mô tả>` — task `GUEST_REQUEST` (`GuestRequestApiController.java:76`)
  - `[Khách Yêu Cầu Sửa Chữa] - <mô tả>` — task `MAINTENANCE` do khách yêu cầu
  - `\n[Ghi chú hoàn thành]: <nội dung>` — append khi hoàn thành (`HousekeepingServiceImpl.java:120`)
  - `\n[Tạm dừng]: <lý do>` — append khi tạm dừng (`MaintenanceWebController.java:132`)
  - `\n[Đã sửa]: <nội dung>` — append khi hoàn thành sửa chữa (`MaintenanceWebController.java:118`)
  - `[Cần sửa chữa - Đền bù hỏng hóc] <nội dung> | Chi phí đền bù: <số> VNĐ` — task từ DAMAGE_CHECK
  - Priority `Lễ tân báo dọn khẩn` — **BẮT BUỘC** dùng nguyên văn này cho `URGENT_CLEAN` do lễ tân báo (check trong `HousekeepingServiceImpl.java:155`)

  **❌ SAI (bịa đặt, không có trong code):**
  ```
  '[Check-out] Khách phòng 102 vừa trả phòng, dọn gấp để đón đoàn 2h chiều.'
  '[Arrival] Khách VIP sắp nhận phòng, chuẩn bị sẵn giỏ trái cây tươi trên bàn.'
  '[Check-out] Đã dọn xong, phát hiện quên một chiếc sạc điện thoại trên bàn.'
  '[Stay-over] Khách yêu cầu thêm 2 khăn tắm và 1 chai nước suối.'
  ```
  **✅ ĐÚNG (lấy từ code):**
  ```
  '[Check-out] Dọn phòng sau khi khách trả phòng.'
  '[Arrival] Dọn phòng khẩn để đón khách nhận phòng. \n[Khẩn cấp] Lễ tân hối thúc dọn ưu tiên để khách Check-in!'
  '[Check-out] Dọn phòng sau khi khách trả phòng.\n[Ghi chú hoàn thành]: Phòng đã dọn sạch và sẵn sàng.'
  '[Khách Yêu Cầu Dọn Phòng] - Bổ sung khăn tắm và nước uống.\n[Ghi chú hoàn thành]: Đã thực hiện.'
  ```
## 2. NGUYÊN TẮC TOÀN VẸN DỮ LIỆU VẬT LÝ (REFERENTIAL INTEGRITY):
- Đảm bảo tất cả các khóa ngoại (Foreign Keys) như `customer_id`, `room_id`, `employee_id`, `promotion_id`... trỏ đến các thực thể đã được khai báo và tồn tại trước đó trong tập dữ liệu.
- Tránh trùng lặp khóa chính (`PRIMARY KEY`) hoặc vi phạm các ràng buộc duy nhất (`UNIQUE CONSTRAINTS`).
- Đối với các phòng vật lý được chỉ định trong trạng thái chờ nhận phòng, hãy chắc chắn phòng đó ở trạng thái trống (`Vacant_Clean` hoặc tương tự) để tránh tranh chấp phòng với các khách đang ở thực tế.
- **Ràng buộc thuộc tính thực thể (Entity Attributes Alignment):** Khi chèn bản ghi khởi tạo SQL, bắt buộc các cột và tên trường phải khớp chính xác với Java Entity Class tương ứng. Ví dụ: Đối với thực thể `Dependent` (bảng `Dependents`), sử dụng đúng các trường `dependent_name`, `birth_date`, `gender`, `cccd_passport_encrypted` thay vì nhầm lẫn với các trường không tồn tại trong class như `full_name`, `date_of_birth`, `relationship`.

## 3. QUY TRÌNH KIỂM TRA BẮT BUỘC:
- **Bước 1:** Đối chiếu chéo tất cả các bảng liên quan đến luồng nghiệp vụ chuẩn bị thêm dữ liệu.
- **Bước 2:** Cập nhật đồng bộ trạng thái của tất cả thực thể liên đới (ví dụ: chạy câu lệnh UPDATE trạng thái đơn hàng cũ khi thêm thực thể phạt cọc/hoàn tiền).
- **Bước 3:** Chạy biên dịch và khởi động thử dự án để kiểm tra lỗi cú pháp SQL hoặc xung đột khóa ngoại lúc nạp dữ liệu.

## 4. BÀI HỌC KINH NGHIỆM (Lessons Learned):
- **Đồng bộ logic code với dữ liệu nạp (Seed Data Alignment):** *Xem chi tiết tại [tech-lead-mindset/SKILL.md](file:///d:/SWP/SWP-Group02/su26-swp391-se2023-g2/.agents/skills/tech-lead-mindset/SKILL.md) §59 (Lệch pha giữa Logic Code và Database Seeds).*
- **Lỗi cascading rollback và các lỗi ẩn khi chạy INSERT gộp:**
  1. *Lỗi trùng lặp cột UNIQUE:* Khi dùng `INSERT IGNORE` với các bản ghi trùng lặp khóa duy nhất (như CCCD, email), bản ghi cha sẽ bị bỏ qua và kéo theo toàn bộ các bảng con liên đới (`Bookings` -> `Room_Bookings` -> `Room_Booking_Details`) thất bại khóa ngoại. Luôn đảm bảo dữ liệu UNIQUE của khách hàng test không được trùng lặp.
  2. *Lỗi rollback cả cụm lệnh:* Khi viết một câu lệnh `INSERT INTO Table (...) VALUES (...), (...);` gộp nhiều bản ghi, chỉ cần một dòng bị sai khóa ngoại (như typo ID `909` thay vì `9`), toàn bộ câu lệnh sẽ bị rollback, khiến toàn bộ các bản ghi đúng khác cũng bị mất.
  3. *Lỗi thiếu bản ghi bảng con kế thừa:* Đối với các thực thể JPA dùng chiến lược `JOINED` inheritance, bắt buộc phải chèn đầy đủ bản ghi ở bảng cha (`Bookings`) lẫn bảng con (`Room_Bookings`) trước khi chèn chi tiết (`Room_Booking_Details`).
