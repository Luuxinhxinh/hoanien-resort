# BUSINESS RULES — KAWAI RETREAT RESORT & HUB

**Project:** SWP391 — Group 2 — SE2023-NET  
**Lecturer:** Nguyễn Mạnh Cường  
**Version:** 1.0  
**Last Updated:** 2026-06-29  
**Analyst:** Antigravity (Business Analyst Review)

---

## Mục lục

- [1. Giới thiệu](#1-giới-thiệu)
- [2. Phân loại Business Rules](#2-phân-loại-business-rules)
  - [BR-SYS — Bảo mật & Hệ thống](#br-sys--bảo-mật--hệ-thống)
  - [BR-FO — Front Office & Đặt phòng](#br-fo--front-office--đặt-phòng)
  - [BR-FB — F&B & Nhà hàng](#br-fb--fb--nhà-hàng)
  - [BR-TR — Tour & Đánh giá](#br-tr--tour--đánh-giá)
  - [BR-FIN — Tài chính & Thanh toán](#br-fin--tài-chính--thanh-toán)
  - [BR-HK — Housekeeping & Bảo trì](#br-hk--housekeeping--bảo-trì)
  - [BR-DAT — Quản lý Dữ liệu & Tuân thủ Pháp lý](#br-dat--quản-lý-dữ-liệu--tuân-thủ-pháp-lý)
- [3. Bảng Tổng hợp toàn bộ Business Rules](#3-bảng-tổng-hợp-toàn-bộ-business-rules)

---

## 1. Giới thiệu

Tài liệu này tổng hợp và phân tích toàn bộ **Business Rules (BR)** của hệ thống **Kawai Retreat Resort & Hub** — một hệ thống quản lý nghỉ dưỡng tích hợp bao gồm 5 phân hệ:

1. Xác thực, Hồ sơ & Dữ liệu gốc
2. Quản lý Phòng & Lễ tân
3. Dịch vụ Ẩm thực & Nhà hàng (F&B / POS / KDS)
4. Quản lý Lữ hành & Đánh giá
5. Kiểm toán đêm, Tài chính & Báo cáo

Business Rules được xác định dựa trên phân tích từ:
- `SRS_Document_SWP391_G2.md` — Đặc tả yêu cầu phần mềm
- `Project_Specification.md` — Đặc tả kỹ thuật chi tiết dự án

---

## 2. Phân loại Business Rules

---

### BR-SYS — Bảo mật & Hệ thống

> Nhóm quy tắc kiểm soát bảo mật tài khoản, phiên làm việc và nhật ký hệ thống.

---

#### BR-SYS-01 — Mã hóa thông tin định danh & Mật khẩu

| Thuộc tính | Giá trị |
|---|---|
| **ID** | BR-SYS-01 |
| **Tên** | Mã hóa thông tin định danh và mật khẩu |
| **Mức độ ưu tiên** | CRITICAL |
| **Phạm vi áp dụng** | Đăng ký, Đăng nhập, Quản lý hồ sơ |

**Định nghĩa:**  
Tất cả thông tin định danh cá nhân (CCCD, Hộ chiếu) và mật khẩu **bắt buộc phải được mã hóa** trước khi lưu trữ vào cơ sở dữ liệu.

**Chi tiết kỹ thuật:**
- Mật khẩu: sử dụng **BCrypt hashing** (độ phức tạp salt = 10)
- Giấy tờ tùy thân (CCCD/Hộ chiếu): sử dụng **AES-256 symmetric encryption**
- Khóa AES được cấu hình trên **biến môi trường hệ thống** (không hard-code trong source code)
- Giải mã chỉ được thực hiện khi được yêu cầu bởi người dùng hợp lệ hoặc Lễ tân có thẩm quyền

**Điều kiện kích hoạt:** Mọi thao tác INSERT/UPDATE dữ liệu định danh hoặc mật khẩu  
**Hệ quả vi phạm:** Từ chối lưu trữ — không được phép lưu dữ liệu dạng plaintext

---

#### BR-SYS-02 — Khóa tài khoản sau đăng nhập sai nhiều lần

| Thuộc tính | Giá trị |
|---|---|
| **ID** | BR-SYS-02 |
| **Tên** | Khóa tài khoản tạm thời do đăng nhập sai |
| **Mức độ ưu tiên** | HIGH |
| **Phạm vi áp dụng** | Đăng nhập hệ thống |

**Định nghĩa:**  
Tài khoản đăng nhập sai mật khẩu **quá 5 lần liên tiếp** sẽ bị **khóa tự động trong 15 phút**.

**Chi tiết:**
- Bộ đếm thất bại reset về 0 sau khi đăng nhập thành công
- Mã OTP/Email Code xác thực có hiệu lực **tối đa 3 phút** kể từ khi gửi
- Token đặt lại mật khẩu có hiệu lực **tối đa 15 phút** và chỉ dùng được **1 lần duy nhất**
- Sau khi hết thời gian khóa, tài khoản tự động mở lại

**Điều kiện kích hoạt:** Đăng nhập thất bại lần thứ 5 liên tiếp  
**Thông báo:** MSG13 — "Account locked for 15 minutes due to multiple failed login attempts"

---

#### BR-SYS-03 — Phiên làm việc nhân viên tự động hết hạn

| Thuộc tính | Giá trị |
|---|---|
| **ID** | BR-SYS-03 |
| **Tên** | Tự động hết phiên làm việc do không hoạt động |
| **Mức độ ưu tiên** | HIGH |
| **Phạm vi áp dụng** | Tất cả tài khoản nhân viên |

**Định nghĩa:**  
Phiên làm việc của nhân viên sẽ **tự động hết hạn sau 15 phút không có thao tác**.

**Chi tiết:**
- Áp dụng cho tất cả vai trò: Receptionist, F&B Staff, Kitchen Staff, Housekeeping, Maintenance, Tour Guide, Admin, Manager
- Sau khi hết phiên, người dùng phải đăng nhập lại để tiếp tục
- Không áp dụng cho tài khoản Customer (phiên làm việc theo chuẩn web thông thường)

**Điều kiện kích hoạt:** Không có hoạt động người dùng trong 15 phút liên tiếp

---

#### BR-SYS-04 — Ghi nhật ký kiểm toán bắt buộc cho hành động nhạy cảm

| Thuộc tính | Giá trị |
|---|---|
| **ID** | BR-SYS-04 |
| **Tên** | Audit Log bắt buộc — không thể xóa |
| **Mức độ ưu tiên** | CRITICAL |
| **Phạm vi áp dụng** | Toàn hệ thống — các hành động tài chính và phân quyền |

**Định nghĩa:**  
Tất cả các hành động nhạy cảm **bắt buộc phải được ghi nhật ký (Audit Log)** và **không được phép xóa**.

**Hành động bắt buộc ghi log:**
- Hủy đặt phòng / hủy booking
- Thay đổi hóa đơn, chỉnh sửa folio
- Thay đổi phân quyền người dùng
- Cập nhật bảng giá phòng (Dynamic Pricing)
- Đổi phòng vật lý (Room Transfer)
- Kiểm duyệt đánh giá (ẩn/hiện review)

**Dữ liệu ghi log bao gồm:** Bảng bị tác động, Giá trị cũ (JSON), Giá trị mới (JSON), Tài khoản thực hiện, Địa chỉ IP, Timestamp  
**Ràng buộc CSDL:** Bảng `Audit_Logs` chỉ cho phép `INSERT` và `SELECT` — cấm hoàn toàn `UPDATE` và `DELETE`

---

#### BR-SYS-05 — Quyền xóa dữ liệu cá nhân (GDPR / Nghị định 13/2023)

| Thuộc tính | Giá trị |
|---|---|
| **ID** | BR-SYS-05 |
| **Tên** | Ẩn danh hóa dữ liệu khách hàng theo yêu cầu |
| **Mức độ ưu tiên** | HIGH |
| **Phạm vi áp dụng** | Khách hàng đã hoàn thành chu kỳ lưu trú |

**Định nghĩa:**  
Yêu cầu xóa dữ liệu của khách hàng phải được xử lý theo cơ chế **Soft Deletion — Ẩn danh hóa (Anonymization)**:
- Thông tin cá nhân (tên, CCCD, SĐT, email) được thay thế bằng dữ liệu ẩn danh
- Hồ sơ giao dịch (Bookings, Payment_Transactions, Folio_Items) được **giữ nguyên** phục vụ kiểm toán

**Điều kiện:** Khách hàng đã Check-out và không có giao dịch đang pending

---

### BR-FO — Front Office & Đặt phòng

> Nhóm quy tắc điều hành nghiệp vụ đặt phòng, check-in và check-out.

---

#### BR-FO-01 — Chống đặt phòng chồng lấn (Anti-Overbooking)

| Thuộc tính | Giá trị |
|---|---|
| **ID** | BR-FO-01 |
| **Tên** | Ngăn chặn tình trạng Overbooking |
| **Mức độ ưu tiên** | CRITICAL |
| **Phạm vi áp dụng** | Đặt phòng, Check-in |

**Định nghĩa:**  
Hệ thống **phải ngăn chặn tuyệt đối** tình trạng một phòng vật lý được đặt trùng trong cùng một khoảng thời gian.

**Cơ chế triển khai:**
- Database transaction sử dụng `SELECT ... FOR UPDATE` (Pessimistic Lock)
- Optimistic Locking qua cột `@Version` trong JPA entity `Bookings`
- Cart Lock 15 phút: Khi khách bắt đầu thanh toán, số lượng phòng bị giữ tạm thời trong cache (TTL = 15 phút)
- Database Trigger `TRG_Prevent_Overbooking` hoạt động ở tầng CSDL

**Điều kiện kiểm tra:** Ngày nhận phòng >= Ngày hiện tại; Ngày trả phòng > Ngày nhận phòng (tối thiểu 1 ngày)

---

#### BR-FO-02 — Tự động hủy đặt phòng khi hết thời gian thanh toán cọc

| Thuộc tính | Giá trị |
|---|---|
| **ID** | BR-FO-02 |
| **Tên** | Tự động hủy booking nếu không thanh toán trong 15 phút |
| **Mức độ ưu tiên** | HIGH |
| **Phạm vi áp dụng** | Đặt phòng trực tuyến |

**Định nghĩa:**  
Đặt phòng trực tuyến chỉ được **giữ chỗ trong 15 phút**. Nếu không có xác nhận thanh toán cọc trong khoảng thời gian này, hệ thống **tự động hủy đặt phòng** và giải phóng phòng về kho.

**Cơ chế triển khai:**
- Task Scheduler chạy ngầm mỗi 5 phút để quét và hủy các Booking ở trạng thái `Pending` đã quá 15 phút
- Khi bị hủy: Trạng thái Booking -> `Cancelled`, phòng được giải phóng

**Thông báo:** "Booking expired due to payment timeout"

---

#### BR-FO-03 — Yêu cầu tuổi tối thiểu và định danh khi Check-in

| Thuộc tính | Giá trị |
|---|---|
| **ID** | BR-FO-03 |
| **Tên** | Điều kiện tuổi và định danh pháp lý khi Check-in |
| **Mức độ ưu tiên** | HIGH |
| **Phạm vi áp dụng** | Check-in tại sảnh |

**Định nghĩa:**  
Người đại diện đứng tên phòng (Primary Contact) khi làm thủ tục Check-in phải:
- **Ít nhất 18 tuổi** tại thời điểm Check-in
- **Xuất trình giấy tờ tùy thân hợp lệ** (CCCD hoặc Hộ chiếu còn hiệu lực)
- Mỗi phòng vật lý **bắt buộc phải có tối thiểu một người lớn** đứng tên đại diện pháp lý

**Dữ liệu thu thập bắt buộc:** Họ tên, Ngày sinh, CCCD/Hộ chiếu, Giới tính, Quốc tịch  
**Mục đích:** Tuân thủ Luật Cư trú 2020 — khai báo tạm trú với cơ quan Công an khu vực

---

#### BR-FO-04 — Vòng đời trạng thái phòng (Room State Machine)

| Thuộc tính | Giá trị |
|---|---|
| **ID** | BR-FO-04 |
| **Tên** | Quy trình chuyển đổi trạng thái phòng |
| **Mức độ ưu tiên** | CRITICAL |
| **Phạm vi áp dụng** | Toàn bộ vòng đời lưu trú |

**Định nghĩa:**  
Trạng thái phòng phải tuân theo **vòng đời cố định**:

```
Vacant_Clean -> Occupied -> Vacant_Dirty -> Vacant_Clean
                                |
                           Maintenance
```

**Quy tắc chuyển đổi:**

| Từ trạng thái | Điều kiện | Sang trạng thái |
|---|---|---|
| `Vacant_Clean` | Lễ tân xác nhận Check-in | `Occupied` |
| `Occupied` | Lễ tân xác nhận Check-out | `Vacant_Dirty` |
| `Vacant_Dirty` | Housekeeping hoàn thành dọn dẹp | `Vacant_Clean` |
| `Vacant_Dirty` / `Vacant_Clean` | Housekeeping báo hỏng | `Maintenance` |
| `Maintenance` | Maintenance hoàn thành sửa chữa | `Vacant_Dirty` hoặc `Vacant_Clean` |

**Ràng buộc QUAN TRỌNG:** Chỉ phòng ở trạng thái `Vacant_Clean` mới được phép gán cho khách khi Check-in

---

#### BR-FO-05 — Ưu tiên Rush Room

| Thuộc tính | Giá trị |
|---|---|
| **ID** | BR-FO-05 |
| **Tên** | Phòng Rush Room được ưu tiên dọn dẹp |
| **Mức độ ưu tiên** | MEDIUM |
| **Phạm vi áp dụng** | Housekeeping — Lễ tân |

**Định nghĩa:**  
Phòng được đánh dấu **Rush Room** (ưu tiên dọn gấp cho khách VIP check-in sớm) phải được **chuyển lên đầu hàng đợi** của danh sách dọn dẹp Housekeeping.

**Điều kiện kích hoạt:** Lễ tân đánh dấu phòng là `Urgent` priority trên hệ thống  
**Tác động:** Trường `priority` của tác vụ dọn phòng trong `Hotel_Operations` được cập nhật thành `Urgent`

---

### BR-FB — F&B & Nhà hàng

> Nhóm quy tắc điều hành nghiệp vụ ăn uống, đặt bàn và ký nợ phòng.

---

#### BR-FB-01 — Ký nợ phòng — Xác thực PIN và Hạn mức tín dụng

| Thuộc tính | Giá trị |
|---|---|
| **ID** | BR-FB-01 |
| **Tên** | Điều kiện ký nợ dịch vụ ăn uống về phòng (Post to Room) |
| **Mức độ ưu tiên** | CRITICAL |
| **Phạm vi áp dụng** | F&B POS — Folio Charging |

**Định nghĩa:**  
Chi phí F&B chỉ được phép **ký nợ về phòng** khi đáp ứng **đồng thời** tất cả các điều kiện:

1. Phòng đang ở trạng thái **đang lưu trú** (`detail_status = 'Checked_In'`)
2. Phòng có cờ **cho phép ký nợ** (`is_charge_to_room_allowed = true`)
3. Khách hàng **xác nhận đúng mã PIN 4 số** (so khớp BCrypt hash)
4. Tổng nợ mới (nợ hiện tại + đơn hàng mới) **không vượt quá hạn mức tín dụng** của phòng (`credit_limit`)

**Cơ chế bảo vệ:**
- Database Trigger `TRG_Folio_Credit_Limit_Check` kiểm tra hạn mức tự động tại tầng CSDL
- Vi phạm hạn mức -> giao dịch bị **rollback** và trả về lỗi 403

**Kết quả khi thành công:** Tạo bản ghi `Folio_Items` với `source_department = 'FB'`

---

#### BR-FB-02 — Đồng bộ trạng thái hết món real-time

| Thuộc tính | Giá trị |
|---|---|
| **ID** | BR-FB-02 |
| **Tên** | Khóa món ăn hết nguyên liệu trên toàn hệ thống |
| **Mức độ ưu tiên** | HIGH |
| **Phạm vi áp dụng** | Kitchen KDS — POS — Web Room Service |

**Định nghĩa:**  
Khi Kitchen Staff đánh dấu một món ăn là **hết nguyên liệu**, món đó phải **ngay lập tức bị vô hiệu hóa** trên toàn bộ điểm tiếp xúc của hệ thống.

**Phạm vi vô hiệu hóa đồng thời:**
- Tất cả màn hình POS tại sảnh nhà hàng
- Giao diện E-Menu Room Service của khách hàng (web/mobile)
- Màn hình KDS nhà bếp

**Cơ chế:** Cập nhật `Menu_Items.is_available = false` -> Broadcast WebSocket tới tất cả client  
**Hệ quả:** Không được phép đặt món đã hết cho đến khi được kích hoạt lại bởi Kitchen Staff

---

#### BR-FB-03 — Thời gian giữ bàn nhà hàng tối đa 30 phút

| Thuộc tính | Giá trị |
|---|---|
| **ID** | BR-FB-03 |
| **Tên** | Tự động giải phóng bàn đặt trước nếu khách trễ giờ |
| **Mức độ ưu tiên** | MEDIUM |
| **Phạm vi áp dụng** | Đặt bàn nhà hàng |

**Định nghĩa:**  
Bàn nhà hàng đã được đặt trước chỉ được **giữ tối đa 30 phút** sau giờ hẹn. Nếu khách hàng không đến trong khoảng thời gian này, **bàn được tự động giải phóng**.

**Điều kiện đặt bàn hợp lệ:** Thời điểm đặt bàn phải trước giờ dùng bữa **tối thiểu 1 tiếng**  
**Ràng buộc:** Không được đặt trùng bàn trong cùng khung thời gian giữ bàn quy định

---

### BR-TR — Tour & Đánh giá

> Nhóm quy tắc điều hành đặt vé tour, điểm danh AI và đánh giá dịch vụ.

---

#### BR-TR-01 — Chống đặt tour vượt sức chứa (Anti-Overbooking Tour)

| Thuộc tính | Giá trị |
|---|---|
| **ID** | BR-TR-01 |
| **Tên** | Ngăn chặn đặt vé tour vượt sức chứa |
| **Mức độ ưu tiên** | CRITICAL |
| **Phạm vi áp dụng** | Đặt vé tour |

**Định nghĩa:**  
Tổng số người đặt vé tham gia một chuyến xe Tour **không được vượt quá sức chứa tối đa** (`max_capacity`) của chuyến đó.

**Cơ chế bảo vệ:**
- Database Trigger `TRG_Tour_Capacity_Validator` chặn đứng giao dịch và báo lỗi nếu vượt sức chứa
- Optimistic Locking trên entity `Tour_Schedules` (cột `@Version`)

**Ràng buộc bổ sung:**  
- Không được phân công nhân viên (Hướng dẫn viên, Tài xế) cho hai chuyến xe tour trùng lịch (Double-booking Staff Prevention)

---

#### BR-TR-02 — Ngưỡng độ chính xác tối thiểu cho AI Face Scan

| Thuộc tính | Giá trị |
|---|---|
| **ID** | BR-TR-02 |
| **Tên** | Ngưỡng Cosine Similarity tối thiểu cho điểm danh AI |
| **Mức độ ưu tiên** | HIGH |
| **Phạm vi áp dụng** | Điểm danh tour bằng AI |

**Định nghĩa:**  
Kết quả nhận diện khuôn mặt từ AI chỉ được coi là **hợp lệ** khi **độ tương đồng Cosine Similarity >= 85%** (>= 0.85) so với ảnh gốc đã đăng ký.

**Khi dưới ngưỡng:** Hệ thống tự động **chuyển sang điểm danh thủ công** bởi Tour Guide  
**Điều kiện ảnh hợp lệ:** Đủ ánh sáng, một khuôn mặt trực diện, không mờ/nhòa

---

#### BR-TR-03 — Hạn chót gửi đánh giá dịch vụ

| Thuộc tính | Giá trị |
|---|---|
| **ID** | BR-TR-03 |
| **Tên** | Cửa sổ thời gian gửi đánh giá sau dịch vụ |
| **Mức độ ưu tiên** | MEDIUM |
| **Phạm vi áp dụng** | Đánh giá phòng và tour |

**Định nghĩa:**  
Khách hàng chỉ được phép gửi đánh giá **trong vòng 7 ngày** kể từ:
- Ngày Check-out khỏi phòng
- Ngày hoàn thành tour

**Ràng buộc chống spam:** Chỉ khách hàng đã **thực sự thanh toán và hoàn tất chu kỳ dịch vụ** mới được phép gửi đánh giá  
**Trạng thái sau gửi:** `moderation_status = 'Pending'` — chờ Admin phê duyệt

---

#### BR-TR-04 — Quyền kiểm duyệt đánh giá của Admin

| Thuộc tính | Giá trị |
|---|---|
| **ID** | BR-TR-04 |
| **Tên** | Giới hạn quyền kiểm duyệt đánh giá |
| **Mức độ ưu tiên** | MEDIUM |
| **Phạm vi áp dụng** | Quản trị hệ thống — Kiểm duyệt review |

**Định nghĩa:**  
Admin có quyền **ẩn hoặc hiện** đánh giá của khách hàng trên portal công khai, **nhưng không được phép chỉnh sửa nội dung** đánh giá dưới bất kỳ hình thức nào.

**Bắt buộc:** Mọi hành động kiểm duyệt (ẩn/hiện) phải kèm **lý do rõ ràng** và được **ghi vào Audit Log**

---

#### BR-TR-05 — Tự động hủy tour khi không đủ người tham gia

| Thuộc tính | Giá trị |
|---|---|
| **ID** | BR-TR-05 |
| **Tên** | Hủy tự động tour dưới ngưỡng tham gia tối thiểu |
| **Mức độ ưu tiên** | HIGH |
| **Phạm vi áp dụng** | Quản lý lịch trình Tour |

**Định nghĩa:**  
Nếu tổng số người đặt vé **thấp hơn ngưỡng tối thiểu** vào thời điểm **24 giờ trước giờ khởi hành**, chuyến tour sẽ **tự động bị hủy** và tất cả người đặt vé được **hoàn tiền 100%**.

**Cơ chế:**
- Scheduler tự động kiểm tra T-24h trước mỗi chuyến
- Gửi email thông báo hủy và xin lỗi tới tất cả khách đã đặt vé
- Hoàn tiền tự động qua VNPay Refund hoặc xóa khoản nợ Folio

---

### BR-FIN — Tài chính & Thanh toán

> Nhóm quy tắc điều hành thanh toán, hủy đặt phòng, kiểm toán đêm và báo cáo tài chính.

---

#### BR-FIN-01 — Điều kiện bắt buộc để Check-out

| Thuộc tính | Giá trị |
|---|---|
| **ID** | BR-FIN-01 |
| **Tên** | Bắt buộc tất toán toàn bộ nợ trước khi Check-out |
| **Mức độ ưu tiên** | CRITICAL |
| **Phạm vi áp dụng** | Check-out tại sảnh — Lễ tân |

**Định nghĩa:**  
Hệ thống **từ chối thực hiện Check-out** nếu trong hóa đơn tổng hợp của booking còn tồn tại bất kỳ:
- Giao dịch / Folio ở trạng thái `Pending`
- Số dư nợ tổng hợp chưa quy về = 0

**Cơ chế:** Kiểm tra real-time trước khi Lễ tân bấm xác nhận Check-out  
**Hệ quả:** Nút/chức năng Check-out bị **khóa vô hiệu hóa** khi còn công nợ

---

#### BR-FIN-02 — Chính sách hoàn tiền khi hủy đặt phòng

| Thuộc tính | Giá trị |
|---|---|
| **ID** | BR-FIN-02 |
| **Tên** | Quy tắc hoàn tiền cọc khi hủy phòng |
| **Mức độ ưu tiên** | HIGH |
| **Phạm vi áp dụng** | Hủy đặt phòng — Khách hàng |

**Định nghĩa:**

| Thời điểm hủy | Chính sách hoàn tiền |
|---|---|
| **Trước 48 giờ** so với ngày Check-in dự kiến | Hoàn trả **100% tiền cọc** |
| **Trong vòng 48 giờ** trước ngày Check-in | **Tịch thu toàn bộ** tiền cọc — không hoàn |
| **No-show** (không đến nhận phòng) | **Tịch thu toàn bộ** tiền cọc — không hoàn |

**Ghi chú kỹ thuật:** Giao dịch hoàn tiền được lưu với `transaction_type = 'REFUND'` trong `Payment_Transactions`

---

#### BR-FIN-03 — Night Audit tự động chạy lúc 02:00 AM

| Thuộc tính | Giá trị |
|---|---|
| **ID** | BR-FIN-03 |
| **Tên** | Kiểm toán đêm tự động mỗi ngày |
| **Mức độ ưu tiên** | CRITICAL |
| **Phạm vi áp dụng** | Hệ thống tài chính tổng thể |

**Định nghĩa:**  
Hệ thống tự động chạy tiến trình **Night Audit** vào lúc **02:00 AM hàng ngày** để thực hiện:

1. Quét toàn bộ phòng đang có khách (`detail_status = 'Checked_In'`)
2. Lấy giá phòng ngày hôm đó tại bảng `Daily_Rates`
3. Tạo dòng ghi nợ tiền phòng vào Folio (`source_department = 'ROOM'`)
4. Dịch chuyển ngày hoạt động sang ngày tiếp theo
5. Kết xuất dữ liệu cân đối doanh thu của ngày đã khóa sổ

**Điều kiện lỗi:** Nếu không có cấu hình giá phòng, hệ thống gửi **cảnh báo khẩn cấp** tới Quản lý  
**Ghi nhật ký:** Ghi log chi tiết toàn bộ tiến trình thực thi

---

#### BR-FIN-04 — Phân loại doanh thu theo chuẩn USALI

| Thuộc tính | Giá trị |
|---|---|
| **ID** | BR-FIN-04 |
| **Tên** | Phân loại doanh thu ba bộ phận theo USALI |
| **Mức độ ưu tiên** | HIGH |
| **Phạm vi áp dụng** | Báo cáo tài chính — Manager |

**Định nghĩa:**  
Mọi báo cáo doanh thu phải **phân loại rõ ràng thành 3 nhóm** theo tiêu chuẩn USALI:

1. **Room Revenue** — Doanh thu phòng nghỉ
2. **Food & Beverage Revenue** — Doanh thu ẩm thực và nhà hàng
3. **Tour Revenue** — Doanh thu lữ hành và tour

**Mục đích:** Tính toán GOP (Gross Operating Profit) và xuất báo cáo tài chính chuẩn quốc tế

---

#### BR-FIN-05 — Một Booking chỉ được áp dụng tối đa 1 mã khuyến mãi

| Thuộc tính | Giá trị |
|---|---|
| **ID** | BR-FIN-05 |
| **Tên** | Giới hạn áp dụng voucher khuyến mãi |
| **Mức độ ưu tiên** | MEDIUM |
| **Phạm vi áp dụng** | Đặt phòng — Thanh toán |

**Định nghĩa:**  
Mỗi Booking chỉ được phép áp dụng **tối đa 1 mã giảm giá (Promo Code)**.

**Điều kiện mã khuyến mãi hợp lệ:**
- `is_active = true`
- Ngày hiện tại nằm trong khoảng `valid_from` đến `valid_to`
- Số lần đã sử dụng chưa vượt `max_uses`

---

### BR-HK — Housekeeping & Bảo trì

> Nhóm quy tắc tự động hóa tác vụ dọn dẹp và bảo trì phòng.

---

#### BR-HK-01 — Tự động phát sinh tác vụ dọn phòng sau Check-out

| Thuộc tính | Giá trị |
|---|---|
| **ID** | BR-HK-01 |
| **Tên** | Tự động sinh task dọn phòng khi Check-out |
| **Mức độ ưu tiên** | HIGH |
| **Phạm vi áp dụng** | Housekeeping |

**Định nghĩa:**  
Khi Lễ tân xác nhận Check-out thành công, hệ thống phải **tự động**:
1. Chuyển trạng thái phòng vật lý -> `Vacant_Dirty`
2. Tạo một bản ghi tác vụ dọn phòng mới trong `Hotel_Operations` (loại `CHECKOUT_CLEAN`, trạng thái `Pending`, độ ưu tiên `High`)

**Cơ chế:** Database Trigger `TRG_Auto_Housekeeping_Task` tại tầng CSDL — không cần gọi qua code Java

---

#### BR-HK-02 — Tự động chuyển phòng sang trạng thái Bảo trì khi báo hỏng

| Thuộc tính | Giá trị |
|---|---|
| **ID** | BR-HK-02 |
| **Tên** | Tự động khóa phòng khi có yêu cầu bảo trì |
| **Mức độ ưu tiên** | HIGH |
| **Phạm vi áp dụng** | Housekeeping — Maintenance |

**Định nghĩa:**  
Khi Housekeeping báo hỏng thiết bị, hệ thống phải **ngay lập tức** chuyển trạng thái phòng sang `Maintenance`, ngăn Lễ tân gán phòng này cho khách Check-in mới.

**Cơ chế:** Database Trigger `TRG_Maintenance_Auto_Bridge`  
**Hệ quả:** Phòng `Maintenance` bị ẩn/vô hiệu trong danh sách phòng khả dụng trên Room Matrix

---

### BR-DAT — Quản lý Dữ liệu & Tuân thủ Pháp lý

> Nhóm quy tắc quản trị dữ liệu gốc và tuân thủ quy định pháp lý Việt Nam.

---

#### BR-DAT-01 — Không được xóa dữ liệu đang hoạt động

| Thuộc tính | Giá trị |
|---|---|
| **ID** | BR-DAT-01 |
| **Tên** | Cấm xóa dữ liệu đang trong vòng đời hoạt động |
| **Mức độ ưu tiên** | HIGH |
| **Phạm vi áp dụng** | Quản trị dữ liệu gốc — Admin |

**Định nghĩa:**  
Admin **không được phép xóa** các thực thể đang trong trạng thái hoạt động:

| Thực thể | Điều kiện cấm xóa |
|---|---|
| Phòng (`Rooms`) | Đang có khách lưu trú (`Occupied`) |
| Bàn nhà hàng (`Restaurant_Tables`) | Đang có đặt cọc chờ (`Confirmed reservation`) |
| Tour (`Tours` / `Tour_Schedules`) | Chuyến đang `Active` hoặc đã có đặt vé |

---

#### BR-DAT-02 — Nhân viên chỉ có một vai trò chính tại một thời điểm

| Thuộc tính | Giá trị |
|---|---|
| **ID** | BR-DAT-02 |
| **Tên** | Nguyên tắc phân quyền RBAC một vai trò |
| **Mức độ ưu tiên** | HIGH |
| **Phạm vi áp dụng** | Quản lý tài khoản nhân viên |

**Định nghĩa:**  
Mỗi nhân viên chỉ được gán **duy nhất một vai trò chính** (role) trong hệ thống tại một thời điểm. Vai trò xác định toàn bộ quyền truy cập API và màn hình của người dùng đó.

**Hệ thống RBAC:** Spring Security kiểm tra role_name để cấu hình URL redirect và giới hạn quyền API tự động

---

#### BR-DAT-03 — Giá phòng động không được chồng lấn khoảng ngày

| Thuộc tính | Giá trị |
|---|---|
| **ID** | BR-DAT-03 |
| **Tên** | Không được chồng lấn chính sách giá động |
| **Mức độ ưu tiên** | HIGH |
| **Phạm vi áp dụng** | Cấu hình Dynamic Pricing |

**Định nghĩa:**  
Khi thiết lập chính sách giá động (`Dynamic_Pricing`), khoảng ngày áp dụng (`start_date` đến `end_date`) **không được phép chồng lấn (overlap)** với bất kỳ chính sách giá động nào khác của cùng hạng phòng.

---

## 3. Bảng Tổng hợp toàn bộ Business Rules

| ID | Tên quy tắc | Nhóm | Mức độ | Use Cases liên quan | Nguồn tài liệu |
|---|---|---|---|---|---|
| BR-SYS-01 | Mã hóa thông tin định danh & Mật khẩu | Bảo mật | CRITICAL | UC01, UC03 | SRS §5.1, PS §7 |
| BR-SYS-02 | Khóa tài khoản sau đăng nhập sai | Bảo mật | HIGH | UC01.2, UC02 | SRS §5.1, PS §UC01 |
| BR-SYS-03 | Phiên làm việc nhân viên hết hạn sau 15 phút | Bảo mật | HIGH | Tất cả UC nhân viên | SRS §5.1 |
| BR-SYS-04 | Ghi Audit Log bắt buộc — không xóa được | Bảo mật | CRITICAL | UC04.2, UC08, UC09.3 | SRS §5.1, PS §UC04 |
| BR-SYS-05 | Ẩn danh hóa dữ liệu khách hàng theo yêu cầu | Bảo mật | HIGH | UC07 (SRS) | SRS §5.1, PS §7 |
| BR-FO-01 | Chống đặt phòng chồng lấn (Overbooking) | Front Office | CRITICAL | UC04 (SRS), UC07.1 | SRS §5.1, PS §UC07.3 |
| BR-FO-02 | Tự động hủy booking sau 15 phút không thanh toán | Front Office | HIGH | UC05 (SRS), UC07.1 | SRS §5.1, PS §UC07.1 |
| BR-FO-03 | Yêu cầu tuổi 18+ và định danh khi Check-in | Front Office | HIGH | UC07 (SRS), UC09.1 | SRS §5.1, PS §UC09.4 |
| BR-FO-04 | Vòng đời trạng thái phòng (State Machine) | Front Office | CRITICAL | UC07, UC08 (SRS), UC09, UC10 | SRS §5.1, PS §UC09, UC10 |
| BR-FO-05 | Ưu tiên Rush Room | Front Office | MEDIUM | UC10.3 | SRS §5.1, PS §UC10.3 |
| BR-FB-01 | Ký nợ phòng — Xác thực PIN & Hạn mức | F&B | CRITICAL | UC11 (SRS), UC15 | SRS §5.1, PS §UC15 |
| BR-FB-02 | Đồng bộ real-time trạng thái hết món | F&B | HIGH | UC10 (SRS), UC14.3 | SRS §5.1, PS §UC14.3 |
| BR-FB-03 | Giữ bàn nhà hàng tối đa 30 phút | F&B | MEDIUM | UC12 (SRS) | SRS §5.1, PS §UC12 |
| BR-TR-01 | Chống đặt tour vượt sức chứa | Tour | CRITICAL | UC12 (SRS), UC17.1 | SRS §5.1, PS §UC17.1 |
| BR-TR-02 | Ngưỡng AI Face Scan >= 85% | Tour | HIGH | UC24 (SRS), UC18 | SRS §5.1, PS §UC18 |
| BR-TR-03 | Gửi đánh giá trong vòng 7 ngày | Tour/Review | MEDIUM | UC15 (SRS), UC19 | SRS §5.1, PS §UC19 |
| BR-TR-04 | Quyền kiểm duyệt review — không sửa nội dung | Tour/Review | MEDIUM | UC26 (SRS), UC20 | SRS §5.1, PS §UC20 |
| BR-TR-05 | Tự động hủy tour dưới ngưỡng tối thiểu 24h | Tour | HIGH | UC23 (SRS), UC17.3 | SRS §5.1, PS §UC17.3 |
| BR-FIN-01 | Bắt buộc tất toán nợ trước khi Check-out | Tài chính | CRITICAL | UC08 (SRS), UC22.1 | SRS §5.1, PS §5 |
| BR-FIN-02 | Chính sách hoàn tiền khi hủy đặt phòng | Tài chính | HIGH | UC06 (SRS) | SRS §5.1, PS §5 |
| BR-FIN-03 | Night Audit tự động lúc 02:00 AM | Tài chính | CRITICAL | UC30 (SRS), UC21.4 | SRS §5.1, PS §UC21.4 |
| BR-FIN-04 | Phân loại doanh thu USALI 3 nhóm | Tài chính | HIGH | UC31, UC33 (SRS), UC24 | SRS §5.1, PS §UC24 |
| BR-FIN-05 | Tối đa 1 mã khuyến mãi mỗi Booking | Tài chính | MEDIUM | UC38 (SRS), UC07.2 | SRS §5.1, PS §UC07.2 |
| BR-HK-01 | Tự động sinh task dọn phòng sau Check-out | Housekeeping | HIGH | UC34 (SRS), UC10.1 | SRS §5.1, PS §UC10.1 |
| BR-HK-02 | Tự động khóa phòng khi báo hỏng | Housekeeping | HIGH | UC36 (SRS), UC10.4 | SRS §5.1, PS §UC10.4 |
| BR-DAT-01 | Cấm xóa thực thể đang hoạt động | Dữ liệu | HIGH | UC09 (SRS), UC05.1 | PS §UC05.1 |
| BR-DAT-02 | Nhân viên một vai trò tại một thời điểm | Dữ liệu | HIGH | UC08 (SRS), UC04.1 | PS §UC04.1 |
| BR-DAT-03 | Không chồng lấn chính sách giá động | Dữ liệu | HIGH | UC40 (SRS), UC05.2 | PS §UC05.2 |

---

*Tài liệu được phân tích bởi Antigravity — Business Analyst Review — ngày 2026-06-29*  
*Nguồn: SRS_Document_SWP391_G2.md + Project_Specification.md*
