# BUSINESS RULES CATALOG

## Kawai Retreat Resort & Hub — Hệ thống Quản lý Nghỉ dưỡng Tích hợp

**Phiên bản:** 2.0
**Ngày tạo:** 2026-06-29
**Ngày cập nhật:** 2026-07-02
**Người phân tích:** Business Analyst (AI-assisted)
**Nguồn tài liệu:** SRS_Document_SWP391_G2.md · Project_Specification.md · Codebase Scan 2026-07-02

---

## Mục lục

- [1. Phạm vi &amp; Mục đích](#1-phạm-vi--mục-đích)
- [2. Quy ước đặt tên](#2-quy-ước-đặt-tên)
- [3. BR-SYS — Bảo mật &amp; Xác thực Hệ thống](#3-br-sys--bảo-mật--xác-thực-hệ-thống)
- [4. BR-FO — Nghiệp vụ Tiền sảnh (Front Office)](#4-br-fo--nghiệp-vụ-tiền-sảnh-front-office)
- [5. BR-FB — Ẩm thực &amp; Nhà hàng (F&amp;B)](#5-br-fb--ẩm-thực--nhà-hàng-fb)
- [6. BR-TR — Lữ hành &amp; Đánh giá (Tour &amp; Review)](#6-br-tr--lữ-hành--đánh-giá-tour--review)
- [7. BR-FIN — Tài chính &amp; Thanh toán](#7-br-fin--tài-chính--thanh-toán)
- [8. BR-HK — Buồng phòng &amp; Bảo trì](#8-br-hk--buồng-phòng--bảo-trì)
- [9. BR-DATA — Quản trị Dữ liệu &amp; Tuân thủ Pháp lý](#9-br-data--quản-trị-dữ-liệu--tuân-thủ-pháp-lý)
- [10. BR-MEM — Hội viên &amp; Tích điểm Khách hàng](#10-br-mem--hội-viên--tích-điểm-khách-hàng)
- [11. BR-STAFF — Lịch làm việc Nhân viên](#11-br-staff--lịch-làm-việc-nhân-viên)
- [12. BR-WF — Workflow Engine Động](#12-br-wf--workflow-engine-động)
- [13. Phụ lục — Ma trận Tác nhân-Quyền hạn](#13-phụ-lục--ma-trận-tác-nhân-quyền-hạn)

---

## 1. Phạm vi & Mục đích

Tài liệu này liệt kê toàn bộ **Business Rules (Quy tắc Kinh doanh)** của hệ thống **Kawai Retreat Resort & Hub** — một nền tảng quản lý nghỉ dưỡng tích hợp cho khu nghỉ dưỡng hạng sang. Business Rules được phân tích và tổng hợp từ:

- SRS Document SWP391 G2 (Đặc tả Yêu cầu Phần mềm)
- Project Specification (Đặc tả Chi tiết Dự án)

Business Rules trong tài liệu này là các quy định kinh doanh **bắt buộc**, không phụ thuộc vào công nghệ triển khai, và có vai trò làm nền tảng để xây dựng logic nghiệp vụ hệ thống.

---

## 2. Quy ước đặt tên

| Prefix   | Nhóm nghiệp vụ                                              |
| -------- | -------------------------------------------------------------- |
| BR-SYS   | Bảo mật & Xác thực Hệ thống                              |
| BR-FO    | Tiền sảnh / Front Office (Đặt phòng, Check-in, Check-out) |
| BR-FB    | Ẩm thực & Nhà hàng (F&B / POS / KDS)                       |
| BR-TR    | Lữ hành & Đánh giá (Tour & Review)                        |
| BR-FIN   | Tài chính & Thanh toán                                      |
| BR-HK    | Buồng phòng & Bảo trì                                      |
| BR-DATA  | Quản trị Dữ liệu & Tuân thủ Pháp lý                    |
| BR-MEM   | Hội viên & Tích điểm Khách hàng                          |
| BR-STAFF | Lịch làm việc & Ca trực Nhân viên                        |
| BR-WF    | Workflow Engine Động (Business Rule Engine)               |

**Mức độ ưu tiên:**

- CRITICAL — Vi phạm gây sai lệch dữ liệu tài chính hoặc pháp lý nghiêm trọng
- HIGH — Vi phạm ảnh hưởng trực tiếp đến trải nghiệm khách hàng hoặc vận hành
- MEDIUM — Vi phạm gây gián đoạn vận hành nhưng có thể phục hồi
- LOW — Tốt hơn nếu tuân thủ, nhưng không gây hậu quả nghiêm trọng ngay lập tức

---

## 3. BR-SYS — Bảo mật & Xác thực Hệ thống

### BR-SYS-01 — Mã hóa Thông tin Nhạy cảm

**Mức độ:** CRITICAL

**Phát biểu:**
Toàn bộ thông tin định danh cá nhân (Căn cước công dân, Hộ chiếu) và mật khẩu tài khoản **phải được mã hóa trước khi lưu vào cơ sở dữ liệu**. Mật khẩu sử dụng thuật toán băm **BCrypt** (độ phức tạp factor = 10); thông tin CCCD/Hộ chiếu sử dụng mã hóa đối xứng **AES-256**.

**Chi tiết:**

- Mật khẩu không bao giờ được lưu dưới dạng plaintext.
- Khóa AES-256 phải được lưu trữ trong biến môi trường hệ thống (env variable), không hardcode trong mã nguồn.
- Giải mã thông tin CCCD/Hộ chiếu chỉ được thực hiện khi có yêu cầu từ người dùng hợp lệ hoặc Lễ tân có thẩm quyền.

**Nguồn:** SRS §5.1 · Project Specification §7

---

### BR-SYS-02 — Khóa Tài khoản & Hết hạn OTP

**Mức độ:** HIGH

**Phát biểu:**

- Tài khoản bị **khóa tự động 15 phút** nếu đăng nhập sai mật khẩu quá **5 lần liên tiếp**.
- Mã OTP / Email Code dùng để xác thực chỉ có hiệu lực tối đa **3 phút**.
- Token khôi phục mật khẩu (Reset Password Token) chỉ có hiệu lực **15 phút** và chỉ được sử dụng **một lần duy nhất**.

**Chi tiết:**

- Sau khi tài khoản bị khóa, người dùng phải chờ đủ 15 phút hoặc liên hệ Admin để mở khóa.
- Token reset mật khẩu phải bị vô hiệu hóa ngay sau khi đặt lại mật khẩu thành công.

**Nguồn:** SRS §5.1 · Project Specification §4 UC01.2

---

### BR-SYS-03 — Phiên Đăng nhập Nhân viên

**Mức độ:** HIGH

**Phát biểu:**
Phiên làm việc (Session) của nhân viên hệ thống phải **tự động hết hạn sau 15 phút không hoạt động** (inactivity timeout).

**Chi tiết:**

- Áp dụng cho tất cả các vai trò nhân viên: Receptionist, F&B Staff, Kitchen Staff, Housekeeping, Maintenance, Tour Guide, Admin, Manager.
- Khi phiên hết hạn, hệ thống phải chuyển hướng người dùng về trang đăng nhập.

**Nguồn:** SRS §5.1

---

### BR-SYS-04 — Ghi Audit Log Bắt buộc

**Mức độ:** CRITICAL

**Phát biểu:**Tất cả các hành động quan trọng sau **phải được ghi vào Audit Log** và **không thể bị xóa hoặc chỉnh sửa**:

- Hủy booking
- Sửa đổi hóa đơn
- Thay đổi phân quyền
- Cập nhật giá phòng
- Ký nợ Folio
- Đổi phòng

**Chi tiết:**

- Bảng `Audit_Logs` chỉ cho phép `INSERT` và `SELECT`, cấm hoàn toàn `UPDATE` và `DELETE`.
- Mỗi bản ghi log phải bao gồm: tên bảng bị tác động, giá trị cũ (JSON), giá trị mới (JSON), ID nhân viên, địa chỉ IP, và timestamp.
- Cơ chế ghi log được triển khai bằng Spring AOP.

**Nguồn:** SRS §5.1 · Project Specification §4 UC04.2

---

### BR-SYS-05 — Xóa Dữ liệu Cá nhân (Right to be Forgotten)

**Mức độ:** HIGH

**Phát biểu:**
Yêu cầu xóa thông tin cá nhân của khách hàng phải được xử lý bằng phương pháp **Soft Deletion / Anonymization** (Ẩn danh hóa), không xóa vật lý khỏi cơ sở dữ liệu.

**Chi tiết:**

- Thông tin bị ẩn danh hóa: họ tên, email, số điện thoại, CCCD/Hộ chiếu.
- Các bản ghi giao dịch tài chính phải được giữ nguyên.
- Chỉ khách hàng đã Check-out mới có thể yêu cầu xóa dữ liệu.
- Tuân thủ Nghị định 13/2023/NĐ-CP về Bảo vệ Dữ liệu Cá nhân.

**Nguồn:** SRS §1.3.2 UC07 · Project Specification §7

---

### BR-SYS-06 — Yêu cầu Mật khẩu Mạnh

**Mức độ:** HIGH

**Phát biểu:**Mật khẩu người dùng phải đáp ứng tối thiểu các tiêu chí:

- Tối thiểu **8 ký tự**.
- Chứa ít nhất **1 chữ hoa** (A-Z).
- Chứa ít nhất **1 chữ thường** (a-z).
- Chứa ít nhất **1 chữ số** (0-9).
- Số điện thoại đăng ký phải có **10-12 số**.

**Nguồn:** SRS §2.1.1 · Project Specification §4 UC01.1

---

### BR-SYS-07 — Phân quyền Vai trò (RBAC)

**Mức độ:** CRITICAL

**Phát biểu:**
Mỗi nhân viên chỉ có **duy nhất một vai trò chính** tại một thời điểm. Hệ thống phải tự động giới hạn quyền truy cập API và giao diện theo vai trò được gán.

**Chi tiết:**

- Nhân viên không thể tự thay đổi vai trò của mình.
- Chỉ Admin mới có quyền gán/thay đổi vai trò nhân viên.
- Vai trò được ánh xạ trực tiếp đến danh sách màn hình và API được phép truy cập.

**Nguồn:** SRS §1.4.2 · Project Specification §4 UC04.1

---

### BR-SYS-08 — Xác thực Thiết bị Nhân viên (Device Authorization 2FA)

**Mức độ:** HIGH

**Phát biểu:**
Cổng đăng nhập nhân viên (`/ops-login`) yêu cầu xác thực hai yếu tố thông qua **mã thiết bị được ủy quyền** (`device_code`). Thiết bị phải được Admin phê duyệt (`is_approved = true`) trước khi nhân viên có thể đăng nhập từ thiết bị đó.

**Chi tiết:**

- Thiết bị chưa được ủy quyền sẽ kích hoạt thêm bước xác minh (OTP hoặc liên hệ Admin).
- Mỗi `device_code` là duy nhất trong hệ thống (`UNIQUE` constraint trên `Authorized_Devices`).
- Admin có quyền thu hồi (`is_approved = false`) quyền truy cập của bất kỳ thiết bị nào.
- Khách hàng dùng cổng `/booking` với Google OAuth2 — không áp dụng quy tắc này.

**Nguồn:** Codebase `AuthorizedDevice.java` · `AuthorizedDeviceApiController.java` · ADR-01 §3

---

### BR-SYS-09 — Quản trị Workflow Động

**Mức độ:** HIGH

**Phát biểu:**
Chỉ Admin mới có quyền cấu hình, sửa đổi các quy trình workflow tự động. Việc cấu hình phải được thực hiện thông qua JSON hợp lệ.

**Chi tiết:**
- `Workflows` entity lưu trữ điều kiện (`conditions_json`) và hành động (`actions_json`).
- Workflow chỉ được kích hoạt nếu `is_active = true`.

**Nguồn:** Codebase `Workflow.java` · `WorkflowEngineServiceImpl.java`

---

### BR-SYS-10 — Nhật ký Xuất Dữ liệu (Export History Audit)

**Mức độ:** HIGH

**Phát biểu:**
Mọi hành động xuất dữ liệu (Export Excel/PDF) liên quan đến thông tin khách hàng hoặc báo cáo tài chính phải được hệ thống tự động ghi nhận vào nhật ký xuất dữ liệu (`ExportHistory`).

**Chi tiết:**
- Nhật ký ghi nhận: ID tài khoản thực hiện, thời gian, loại dữ liệu xuất, số lượng dòng dữ liệu, địa chỉ IP.
- Dữ liệu nhật ký này không được phép sửa đổi hoặc xóa bởi bất kỳ người dùng nào ngoại trừ System Admin.

**Nguồn:** Codebase `ExportHistory.java` · `ExportApiController.java`

---

## 4. BR-FO — Nghiệp vụ Tiền sảnh (Front Office)

### BR-FO-01 — Chống Overbooking Phòng

**Mức độ:** CRITICAL

**Phát biểu:**
Hệ thống phải **ngăn chặn tuyệt đối tình trạng đặt phòng chồng lấn** (Overbooking). Cùng một phòng vật lý không thể được gán cho hai đặt phòng có ngày lưu trú trùng nhau.

**Chi tiết:**

- Bắt buộc sử dụng cơ chế khóa lạc quan `SELECT ... FOR UPDATE` ở tầng database khi thực hiện đặt phòng.
- Database Trigger `TRG_Prevent_Overbooking` phải được kích hoạt ở tầng CSDL.
- Cột `@Version` trong entity `Bookings` phải được sử dụng cho Optimistic Locking.
- Tìm kiếm phòng trống phải loại trừ tất cả phòng có `Room_Booking_Details` ở trạng thái khác `Cancelled` trong khoảng ngày tìm kiếm.

**Nguồn:** SRS §5.1 · Project Specification §5

---

### BR-FO-02 — Thời hạn Thanh toán Cọc

**Mức độ:** CRITICAL

**Phát biểu:**
Đặt phòng trực tuyến chỉ được **giữ chỗ tối đa 2 phút**. Nếu không nhận được xác nhận thanh toán cọc trong vòng 2 phút, hệ thống **tự động hủy đặt phòng** và giải phóng phòng về kho.

**Chi tiết:**

- Hệ thống chạy Task Scheduler định kỳ (mỗi 1 phút hoặc 5 phút) để quét và hủy các booking `Pending` đã quá 2 phút.
- Cơ chế Cart Lock 2 phút ngăn khách hàng khác đặt cùng phòng trong thời gian giữ chỗ.
- Thông báo: "Booking expired due to payment timeout".

**Nguồn:** SRS §1.2.1 · §5.1 · Project Specification §4 UC07.3


---

### BR-FO-03 — Điều kiện Check-in Pháp lý

**Mức độ:** CRITICAL

**Phát biểu:**
Người đại diện thực hiện check-in **phải từ đủ 18 tuổi trở lên** và phải xuất trình **giấy tờ tùy thân hợp lệ** (Căn cước công dân hoặc Hộ chiếu còn hiệu lực).

**Chi tiết:**

- Lễ tân có trách nhiệm xác minh tuổi và giấy tờ tùy thân trước khi hoàn tất check-in.
- Mỗi phòng vật lý phải có ít nhất một người lớn đứng tên đại diện pháp lý (Primary Contact).
- Tuân thủ Luật Cư trú 2020 về khai báo tạm trú.

**Nguồn:** SRS §1.2.1 · §5.1 · Project Specification §4 UC09.4

---

### BR-FO-04 — Vòng đời Trạng thái Phòng

**Mức độ:** CRITICAL

**Phát biểu:**
Trạng thái phòng vật lý **phải tuân thủ đúng vòng đời** sau và không được bỏ qua bất kỳ bước nào:

```
Vacant_Clean --> Occupied_Clean --> Vacant_Dirty --> [Housekeeping] --> Vacant_Clean
                      |
                  Maintenance
```

**Chi tiết:**

- Chỉ phòng có trạng thái `Vacant_Clean` mới được phép gán cho khách khi Check-in.
- Khi Check-out: phòng tự động chuyển sang `Vacant_Dirty`.
- Khi có sự cố kỹ thuật: phòng chuyển sang `Maintenance` và không thể được gán check-in.
- Sau khi dọn dẹp hoàn thành: phòng chuyển về `Vacant_Clean`.

**Nguồn:** SRS §5.1 · Project Specification §4 UC09.1, UC10

---

### BR-FO-05 — Ưu tiên Dọn phòng Khẩn (Rush Room)

**Mức độ:** MEDIUM

**Phát biểu:**
Phòng được đánh dấu **Rush Room** phải được ưu tiên và đưa lên đầu hàng đợi dọn dẹp của nhân viên Housekeeping.

**Chi tiết:**

- Lễ tân có quyền đánh dấu Rush Room khi có khách VIP cần nhận phòng sớm.
- Độ ưu tiên Rush Room = `Urgent`, cao hơn độ ưu tiên `High` (check-out thông thường).

**Nguồn:** SRS §5.1 · Project Specification §4 UC10.3

---

### BR-FO-06 — Hạn mức Nợ Phòng (Credit Limit)

**Mức độ:** HIGH

**Phát biểu:**
Tổng hạn mức chi tiêu ghi nợ (`sub_credit_limit`) của tất cả phòng chi tiết trong một booking **không được vượt quá hạn mức nợ tổng** (`credit_limit`) của booking đó.

**Chi tiết:**

- Hạn mức được thiết lập bởi Lễ tân trong quá trình check-in.
- Mã PIN 4 số của khách được băm bằng BCrypt và lưu vào `personal_pin_hash`.
- Database Trigger `TRG_Folio_Credit_Limit_Check` kiểm tra hạn mức trước mỗi lần ghi nợ Folio.
- Nếu vi phạm hạn mức: giao dịch bị rollback và trả lỗi 403.

**Nguồn:** SRS §1.2.1 · Project Specification §4 UC09.2

---

### BR-FO-07 — Đổi Hạng Phòng (Change Room Category)

**Mức độ:** HIGH

**Phát biểu:**
Khách hàng có thể đổi sang hạng phòng khác nếu hạng phòng mới có phòng trống. Giá phòng sẽ được tính lại dựa trên chênh lệch giá giữa hạng mới và hạng cũ.

**Chi tiết:**

- Việc đổi phòng phải được thực hiện bởi Lễ tân.
- Nếu đổi sang hạng cao hơn (Upgrade), khách phải thanh toán thêm.

**Nguồn:** Codebase `ChangeRoomCategoryServiceImpl.java`

---

### BR-FO-08 — Khách Đến Trực Tiếp (Walk-in Check-in)

**Mức độ:** CRITICAL

**Phát biểu:**
Đối với khách đến trực tiếp (không đặt trước), hệ thống phải hỗ trợ quy trình tạo booking, gán phòng, và thanh toán cọc trong một luồng duy nhất để đảm bảo không bị chiếm phòng giữa chừng.

**Chi tiết:**

- Yêu cầu khai báo thông tin CCCD và thông tin liên lạc đầy đủ ngay tại thời điểm walk-in.
- Booking được tạo ở trạng thái `Confirmed` ngay sau khi thanh toán cọc hoặc thanh toán toàn bộ.

**Nguồn:** Codebase `WalkInCheckInServiceImpl.java`

---

## 5. BR-FB — Ẩm thực & Nhà hàng (F&B)

### BR-FB-01 — Điều kiện Ghi nợ Phòng (Post to Room)

**Mức độ:** CRITICAL

**Phát biểu:**Chi phí ăn uống và dịch vụ chỉ được phép ghi nợ về phòng nếu đáp ứng **đồng thời** các điều kiện:

1. Phòng đang ở trạng thái **Checked_In** (có khách lưu trú).
2. Phòng được **bật cờ cho phép ghi nợ** (`is_charge_to_room_allowed = true`).
3. Khách hàng nhập **đúng mã PIN 4 số** (`personal_pin_hash` khớp).
4. Số tiền ghi nợ mới cộng dồn **không vượt hạn mức** Credit Limit của phòng.

**Chi tiết:**

- Bằng chứng giao dịch (chữ ký hoặc PIN) phải được lưu vào `signature_img_url` của Folio_Items.
- Giao dịch Post-to-Room được lưu trong `Folio_Items` với `source_department = 'FB'`.

**Nguồn:** SRS §5.1 · Project Specification §4 UC15

---

### BR-FB-02 — Cập nhật Real-time Tình trạng Hết món

**Mức độ:** HIGH

**Phát biểu:**
Khi Nhân viên bếp đánh dấu một món ăn là **hết nguyên liệu**, món đó phải **ngay lập tức bị khóa** trên toàn bộ hệ thống POS sảnh và giao diện đặt món Room Service của khách.

**Chi tiết:**

- Cập nhật `Menu_Items.is_available = false` trong database.
- Sử dụng WebSocket hoặc Message Broker để broadcast real-time đến tất cả POS Terminal và màn hình khách hàng.

**Nguồn:** SRS §5.1 · Project Specification §4 UC14.3

---

### BR-FB-03 — Thời gian Giữ Bàn Đặt trước

**Mức độ:** MEDIUM

**Phát biểu:**
Bàn ăn được đặt trước chỉ được **giữ tối đa 30 phút** sau giờ đặt bàn đã hẹn. Nếu khách chưa đến sau 30 phút, bàn được **tự động giải phóng**.

**Chi tiết:**

- Thời gian đặt bàn phải trước giờ dùng bữa tối thiểu 1 tiếng.
- Không được đặt trùng bàn trong cùng khoảng thời gian giữ bàn.

**Nguồn:** SRS §5.1 · Project Specification §4 UC12

---

### BR-FB-04 — Quyền Nhân viên F&B Tại Bếp

**Mức độ:** MEDIUM

**Phát biểu:**
Nhân viên F&B chỉ được phép cập nhật trạng thái đơn hàng thực phẩm. Không được tạo đơn hàng tùy tiện và không được sửa giá niêm yết trên menu.

**Vòng đời trạng thái KOT:** `Pending → Cooking → Ready → Served`

**Nguồn:** SRS §2 UC19 · Project Specification §4 UC19.2

---

### BR-FB-05 — Dịch Vụ Bổ Sung (Add-On Services)

**Mức độ:** MEDIUM

**Phát biểu:**
Khách hàng có thể đặt thêm các dịch vụ bổ sung (`HotelService`) ngoài các dịch vụ tiêu chuẩn của phòng. Hệ thống phải tính phí các dịch vụ này vào hóa đơn Folio của khách.

**Chi tiết:**

- Mỗi dịch vụ có mức giá cơ bản (`basePrice`) và thuộc một bộ phận (`sourceDepartment`).
- Dịch vụ có thể bị vô hiệu hóa (`isAvailable = false`).

**Nguồn:** Codebase `HotelService.java`

---

### BR-FB-06 — Ưu Tiên Đơn Hàng Tại KDS (KDS Priority)

**Mức độ:** HIGH

**Phát biểu:**
Hệ thống KDS (Kitchen Display System) phải hiển thị ưu tiên các đơn hàng Room Service cao hơn các đơn hàng Dine-In để đảm bảo chất lượng dịch vụ phòng.

**Nguồn:** Codebase `KdsServiceImpl.java`

---

## 6. BR-TR — Lữ hành & Đánh giá (Tour & Review)

### BR-TR-01 — Chống Overbooking Tour

**Mức độ:** CRITICAL

**Phát biểu:**
Tổng số người đăng ký tham gia một chuyến xe Tour **không được vượt quá sức chứa tối đa** (`max_capacity`) của chuyến đó.

**Chi tiết:**

- Database Trigger `TRG_Tour_Capacity_Validator` chặn giao dịch và báo lỗi nếu số lượng ghế đặt vượt `max_capacity`.
- Trigger `TRG_Update_Tour_Booked_Seats` tự động cập nhật số ghế đã đặt trong `Tour_Schedules`.
- Kiểm tra nhân sự (Tài xế, Hướng dẫn viên) không trùng lịch khi lập lịch tour.

**Nguồn:** SRS §5.1 · Project Specification §4 UC17.1, UC17.2

---

### BR-TR-02 — Ngưỡng Nhận diện Khuôn mặt AI

**Mức độ:** HIGH

**Phát biểu:**
Kết quả điểm danh bằng AI Face Scan chỉ được coi là **hợp lệ** khi độ tương đồng khuôn mặt đạt tối thiểu **85% (Cosine Similarity >= 0.85)**.

**Chi tiết:**

- Kết quả dưới ngưỡng 85% phải chuyển sang **điểm danh thủ công** bởi Hướng dẫn viên.
- Ảnh chụp phải rõ nét, đủ ánh sáng, chỉ chứa duy nhất một khuôn mặt trực diện.
- Lưu trữ vector khuôn mặt dưới dạng 128-dimensional embedding.

**Nguồn:** SRS §5.1 · Project Specification §4 UC18

---

### BR-TR-03 — Thời hạn Gửi Đánh giá

**Mức độ:** MEDIUM

**Phát biểu:**
Khách hàng chỉ được phép gửi đánh giá **trong vòng 7 ngày** sau khi hoàn thành dịch vụ (Check-out phòng hoặc kết thúc tour).

**Chi tiết:**

- Hệ thống kiểm tra cơ sở dữ liệu để xác nhận khách hàng đã thực sự sử dụng dịch vụ.
- Đánh giá lưu ở trạng thái `moderation_status = 'Pending'` trước khi hiển thị công khai.
- Khách chỉ được đánh giá dịch vụ mà họ đã thực sự sử dụng.

**Nguồn:** SRS §5.1 · Project Specification §4 UC19

---

### BR-TR-04 — Kiểm duyệt Đánh giá (Content Moderation)

**Mức độ:** HIGH

**Phát biểu:**
Admin có thể **ẩn hoặc hiện** đánh giá trên cổng thông tin công khai, nhưng **tuyệt đối không được phép chỉnh sửa nội dung** đánh giá của khách hàng.

**Chi tiết:**

- Mỗi lần ẩn/hiện đánh giá, Admin phải nhập **lý do kiểm duyệt** và ghi vào Audit Log.
- Trạng thái đánh giá: `Pending` → `Approved` hoặc `Hidden`.

**Nguồn:** SRS §5.1 · Project Specification §4 UC20

---

### BR-TR-05 — Tự động Hủy Tour Dưới Ngưỡng Số người

**Mức độ:** HIGH

**Phát biểu:**
Nếu tổng số người đăng ký xác nhận của một chuyến tour **chưa đạt ngưỡng tối thiểu** trong vòng **24 giờ trước giờ khởi hành**, hệ thống **tự động hủy chuyến** và phát sinh hoàn tiền **100%** cho tất cả khách đã đăng ký.

**Chi tiết:**

- Trạng thái chuyến chuyển sang `Cancelled`.
- Hoàn tiền qua VNPay Refund hoặc xóa khoản nợ Folio tương ứng.
- Gửi email thông báo hủy tour đến tất cả khách đã đặt.

**Nguồn:** SRS §5.1 · Project Specification §4 UC17.3

---

### BR-TR-06 — Định vị Toạ độ Tour (GPS Tracking)

**Mức độ:** HIGH

**Phát biểu:**
Vị trí của Tour Guide trong quá trình thực hiện tour phải được ghi nhận và có thể theo dõi trực tiếp, sử dụng tọa độ GPS (`TourLocation`).

**Nguồn:** Codebase `TourLocation.java`

---

### BR-TR-07 — Lịch Trình Chi Tiết Tour (Itinerary Management)

**Mức độ:** MEDIUM

**Phát biểu:**
Mỗi Tour phải có lịch trình chi tiết theo từng ngày (`TourItinerary`) và từng hoạt động cụ thể (`TourItineraryDetail`), bao gồm thời gian bắt đầu, kết thúc, và địa điểm.

**Nguồn:** Codebase `TourItinerary.java`

---

### BR-TR-08 — Theo Dõi Tiến Độ Lịch Trình (Run Itinerary Status)

**Mức độ:** HIGH

**Phát biểu:**
Trạng thái thực thi của từng hoạt động trong lịch trình (`RunItineraryStatus`) phải được Tour Guide cập nhật theo thời gian thực (ví dụ: đang diễn ra, đã hoàn thành), kèm theo ghi chú thực tế.

**Nguồn:** Codebase `RunItineraryStatus.java`

---

### BR-TR-09 — Điểm Danh Theo Trạm (Checkpoint Attendance)

**Mức độ:** HIGH

**Phát biểu:**
Tại mỗi địa điểm trong lịch trình (Checkpoint), Tour Guide phải điểm danh sự hiện diện của từng khách hàng (`CheckpointAttendance`), đảm bảo không có khách nào bị bỏ sót.

**Nguồn:** Codebase `CheckpointAttendance.java`

---

### BR-TR-10 — Đăng ký người đi cùng (Companions) và Điểm danh bằng FaceID

**Mức độ:** HIGH

**Phát biểu:**
Đăng ký đặt tour cho nhóm khách từ 2 thành viên trở lên bắt buộc phải cung cấp thông tin người đi kèm bao gồm Họ và tên (bắt buộc, từ 2 từ trở lên), Độ tuổi (tối thiểu 12 tuổi), và Số điện thoại (nếu có, 10 chữ số). Khi đặt tour thành công, hệ thống tự động lưu thông tin người đi kèm vào bảng `Dependents` và sinh bản ghi điểm danh `TourAttendee` liên kết để Tour Guide điểm danh / quét FaceID cho thành viên đó.

**Chi tiết:**
- Giao diện đặt tour tự động mở rộng và sinh ô nhập thông tin người lớn đi cùng dựa trên tổng số người lớn (nếu adults > 1, sinh adults - 1 dòng nhập).
- Backend kiểm tra tính hợp lệ của thông tin và liên kết người đi kèm với `Customer` chính qua bảng `Dependents`.
- Tên người đi kèm hiển thị tại màn hình điểm danh `FaceID.html` thông qua trường `dependent.dependentName`.

**Nguồn:** Yêu cầu Nghiệp vụ mới (07/2026) · [tour-detail.html](file:///d:/SWP/SWP-Group02/su26-swp391-se2023-g2/05-Development/kawai-backend/src/main/resources/templates/guest/tour-detail.html) · [TourBookingServiceImpl.java](file:///d:/SWP/SWP-Group02/su26-swp391-se2023-g2/05-Development/kawai-backend/src/main/java/com/kawai/services/impl/TourBookingServiceImpl.java)

---

### BR-TR-11 — Luồng Nghiệp vụ Bảo hiểm du lịch cho Tour

**Mức độ:** HIGH

**Phát biểu:**
Đối với các tour có cấu hình bắt buộc mua bảo hiểm du lịch (`isInsuranceRequired = true`), khách hàng bắt buộc phải đồng ý mua bảo hiểm du lịch thì mới được tiến hành đặt tour (mã lỗi chặn `TOUR-INS-001`). Phí bảo hiểm tính trên đầu người sẽ được cộng vào hóa đơn thực tế và tổng số tiền thanh toán hiển thị trong popup thanh toán, đồng thời hệ thống tự động sinh mã bảo hiểm chung (`Policy Number`) cho ngày đi đó theo cấu trúc `INS-<Ngày>-SCH<ID>-<4 ký tự random>`.

**Chi tiết:**
- Giá tour hiển thị tại cột thông tin của trang chi tiết tour là giá thô cố định của tour (không tự động cộng dồn phí bảo hiểm vào). Phí bảo hiểm chỉ cộng vào tổng hóa đơn cuối cùng và hiển thị chi tiết tại popup thanh toán trước khi submit.
- Nếu không đồng ý mua bảo hiểm ở các tour bắt buộc, hệ thống ở Backend sẽ ném ra ngoại lệ và chặn giao dịch (mã lỗi `TOUR-INS-001`).

**Nguồn:** Yêu cầu Nghiệp vụ mới (07/2026) · [TourBookingServiceImpl.java](file:///d:/SWP/SWP-Group02/su26-swp391-se2023-g2/05-Development/kawai-backend/src/main/java/com/kawai/services/impl/TourBookingServiceImpl.java)

---

## 7. BR-FIN — Tài chính & Thanh toán

### BR-FIN-01 — Điều kiện Check-out Tài chính

**Mức độ:** CRITICAL

**Phát biểu:**
Check-out **tuyệt đối không được thực hiện** khi còn bất kỳ công nợ chưa được thanh toán trên hóa đơn tổng hợp (`Consolidated_Invoice`) của booking đó.

**Chi tiết:**

- Hệ thống khóa tính năng check-out nếu số dư nợ tổng hợp chưa bằng 0.
- Tất cả `Folio_Items` có trạng thái `Pending` phải được tất toán trước khi check-out.
- Lễ tân không thể bỏ qua bước kiểm tra này.

**Nguồn:** SRS §1.2.1 · §5.1 · Project Specification §5

---

### BR-FIN-02 — Chính sách Hoàn tiền Hủy Phòng

**Mức độ:** HIGH

**Phát biểu:**Chính sách hoàn tiền khi hủy đặt phòng:

- Hủy trước **48 giờ** so với ngày Check-in: **Hoàn trả 100% tiền cọc**.
- Hủy trong vòng **48 giờ** trước ngày Check-in hoặc **No-show**: **Mất toàn bộ tiền cọc**, không hoàn tiền.

**Chi tiết:**

- Giao dịch hoàn tiền được ghi vào `Payment_Transactions` với `transaction_type = 'REFUND'`.

**Nguồn:** SRS §5.1 · Project Specification §5

---

### BR-FIN-03 — Kiểm toán Đêm Tự động (Night Audit)

**Mức độ:** CRITICAL

**Phát biểu:**
Quy trình **Kiểm toán Đêm (Night Audit)** phải **tự động chạy lúc 02:00 AM mỗi ngày** để đóng sổ và chuyển sang ngày kinh doanh mới.

**Chi tiết — Các bước Night Audit:**

1. Quét toàn bộ phòng đang có khách (`detail_status = 'Checked_In'`).
2. Lấy giá phòng theo ngày từ bảng `Daily_Rates`.
3. Tạo dòng ghi nợ tiền phòng vào Folio (`Folio_Items`, `source_department = 'ROOM'`).
4. Tự động dịch chuyển ngày vận hành sang ngày tiếp theo.
5. Kết xuất dữ liệu cân đối doanh thu ngày đã khóa sổ.
6. Nếu không tìm thấy cấu hình giá: gửi cảnh báo khẩn cấp đến Quản lý.

**Nguồn:** SRS §5.1 · Project Specification §4 UC21.4

---

### BR-FIN-04 — Phân loại Doanh thu Chuẩn USALI

**Mức độ:** HIGH

**Phát biểu:**Toàn bộ doanh thu phải được **phân loại và tách biệt** thành 3 danh mục theo chuẩn USALI:

1. **Room Revenue** — Doanh thu phòng lưu trú
2. **Food & Beverage Revenue** — Doanh thu ẩm thực nhà hàng
3. **Tour Revenue** — Doanh thu bán vé lữ hành

**Chi tiết:**

- Mỗi bản ghi `Folio_Items` phải có `source_department` tương ứng (ROOM / FB / TOUR).
- Báo cáo USALI phải tính toán Lợi nhuận Hoạt động Gộp (GOP) cho từng bộ phận.

**Nguồn:** SRS §5.1 · Project Specification §4 UC24

---

### BR-FIN-05 — Tính Giá Phòng Động (Dynamic Pricing)

**Mức độ:** MEDIUM

**Phát biểu:**
Khoảng ngày áp dụng của các chính sách giá phòng động **không được chồng lấn** (overlap) với các chính sách giá khác của **cùng một hạng phòng**.

**Chi tiết:**

- Giá phòng được tính dựa trên bảng `Daily_Rates` (giá theo từng ngày cụ thể).
- Chỉ Admin và Manager mới có quyền cấu hình giá phòng động.
- Mỗi thay đổi giá phải được ghi vào Audit Log.

**Nguồn:** SRS §1.3.2 UC40 · Project Specification §4 UC05.2

---

### BR-FIN-06 — Mã Voucher Khuyến mãi

**Mức độ:** MEDIUM

**Phát biểu:**Mỗi booking chỉ được **áp dụng tối đa 1 mã giảm giá**. Trước khi áp dụng, hệ thống phải kiểm tra đồng thời:

- Mã `is_active = true`.
- Ngày hiện tại nằm trong khoảng `valid_to` còn hiệu lực.
- Số lượt dùng chưa vượt `max_uses`.
- Mã chỉ được sử dụng tại thời điểm xác nhận booking.

**Nguồn:** SRS §1.4.3 Non-UI#14 · Project Specification §4 UC07.2

---

### BR-FIN-07 — Quét CCCD Từ Xa (Remote Scan)

**Mức độ:** HIGH

**Phát biểu:**
Hệ thống cho phép khách hàng tự cung cấp thông tin định danh thông qua tính năng quét CCCD từ xa, giúp giảm tải thời gian xử lý tại quầy Lễ tân. Hình ảnh tải lên phải được mã hóa và bảo mật.

**Nguồn:** Codebase `RemoteScanApiController.java`

---

### BR-FIN-08 — Yêu Cầu Hoàn Tiền (Refund Request)

**Mức độ:** MEDIUM

**Phát biểu:**
Mọi yêu cầu hoàn tiền không tự động (`RefundRequest`) phải trải qua quy trình phê duyệt của Quản lý (Manager). Phải ghi nhận lý do hoàn tiền và minh chứng (nếu có).

**Nguồn:** Codebase `RefundRequest.java`

---

### BR-FIN-09 — Phụ Phí Phòng (Room Surcharge)

**Mức độ:** HIGH

**Phát biểu:**
Phụ phí phòng (`RoomSurcharge`) sẽ được tự động tính thêm vào giá phòng cơ sở nếu đối tượng khách (độ tuổi) nằm trong khoảng áp dụng phụ phí. Các phụ phí này phải được định nghĩa trước theo từng hạng phòng.

**Nguồn:** Codebase `RoomSurcharge.java`

---

## 8. BR-HK — Buồng phòng & Bảo trì

### BR-HK-01 — Tự động Tạo Task Dọn phòng

**Mức độ:** HIGH

**Phát biểu:**
Khi Lễ tân hoàn tất Check-out cho một phòng, hệ thống phải **tự động tạo task dọn phòng** và chuyển trạng thái phòng sang `Vacant_Dirty`.

**Chi tiết:**

- Database Trigger `TRG_Auto_Housekeeping_Task` kích hoạt ở tầng CSDL.
- Task được tạo với trạng thái `Pending` và độ ưu tiên `High`.
- Nhân viên buồng phòng nhận notification trên ứng dụng di động.

**Nguồn:** SRS §1.4.3 Non-UI#5 · Project Specification §4 UC10.1

---

### BR-HK-02 — Báo cáo Sự cố Kỹ thuật

**Mức độ:** HIGH

**Phát biểu:**Khi nhân viên Housekeeping báo cáo trang thiết bị hỏng, hệ thống phải **tự động**:

1. Tạo phiếu tác vụ kỹ thuật trong `Hotel_Operations` với `task_type = 'MAINTENANCE'`.
2. Chuyển trạng thái phòng sang `Maintenance`.

**Chi tiết:**

- Phòng ở trạng thái `Maintenance` không được phép gán cho khách mới check-in.
- Sau khi Maintainer hoàn thành: phòng chuyển về `Vacant_Dirty` hoặc `Vacant_Clean`.

**Nguồn:** SRS §1.4.3 Non-UI#12 · Project Specification §4 UC10.4, UC10.5

---

## 9. BR-DATA — Quản trị Dữ liệu & Tuân thủ Pháp lý

### BR-DATA-01 — Ràng buộc Xóa Dữ liệu Gốc

**Mức độ:** HIGH

**Phát biểu:**Hệ thống nghiêm cấm xóa các dữ liệu nền (Master Data) khi chúng đang được sử dụng:

- **Không xóa phòng** đang có khách lưu trú.
- **Không xóa bàn ăn** đang có đặt chỗ trước.
- **Không xóa tour** đang ở trạng thái Active/Scheduled.

**Nguồn:** Project Specification §4 UC05.1

---

### BR-DATA-02 — Khai báo Tạm trú

**Mức độ:** CRITICAL

**Phát biểu:**
Thông tin khai báo lưu trú phải thu thập đầy đủ cho mỗi khách: Họ tên, Ngày sinh, Số CCCD, Giới tính, Quốc tịch — nhằm đáp ứng yêu cầu theo **Luật Cư trú 2020**.

**Nguồn:** Project Specification §7

---

### BR-DATA-03 — Tính Nhất quán Giao dịch

**Mức độ:** CRITICAL

**Phát biểu:**
Quá trình tạo tài khoản nhân viên (bao gồm `Accounts` và `Employees`) phải được bao trong **một giao dịch CSDL duy nhất** (`@Transactional`). Nếu bất kỳ bước nào thất bại, toàn bộ giao dịch phải được **rollback**.

**Nguồn:** SRS §1.4.3 Non-UI#16

---

### BR-DATA-04 — Khai báo Khách trong Phòng (Room Guest Registry)

**Mức độ:** CRITICAL

**Phát biểu:**
Mỗi phòng vật lý được check-in **phải có ít nhất một người đại diện chính (Primary Contact)** được khai báo đầy đủ trong bảng `Room_Guests`. Mỗi `RoomBookingDetail` phải có đúng một bản ghi với `is_primary_contact = true`.

**Chi tiết:**

- Khách có thể là `Customer` (đã có tài khoản) hoặc `Dependent` (người đi cùng).
- `guest_type` phân loại: `ADULT`, `CHILD`, `INFANT`.
- Thông tin này phục vụ báo cáo khai báo tạm trú theo Luật Cư trú 2020.

**Nguồn:** Codebase `RoomGuest.java` · BR-DATA-02

---

### BR-DATA-05 — Phí Phụ trội Theo Độ tuổi (Room Surcharge)

**Mức độ:** MEDIUM

**Phát biểu:**
Hệ thống áp dụng hệ số giá phụ trội (`price_modifier`) theo độ tuổi khách khi tính toán giá phòng, dựa trên cấu hình `Room_Surcharges` gắn với từng hạng phòng.

**Chi tiết:**

- Surcharge được định nghĩa theo khoảng tuổi (`age_from`, `age_to`) và loại phụ trội (`surcharge_type`).
- Hệ số `price_modifier` nhân với giá cơ sở hoặc giá theo ngày.
- Chỉ áp dụng khi `is_active = true`.
- Chỉ Admin và Manager có quyền cấu hình Room Surcharge.

**Nguồn:** Codebase `RoomSurcharge.java`

---

## 10. BR-MEM — Hội viên & Tích điểm Khách hàng

### BR-MEM-01 — Tích điểm Loyalty và Phân hạng Thành viên

**Mức độ:** MEDIUM

**Phát biểu:**
Khách hàng tích lũy điểm qua các giao dịch dịch vụ. Điểm tích lũy xác định hạng thành viên (`MembershipTier`) và hạng thành viên xác định hạn mức tín dụng (`credit_limit`) mặc định khi check-in.

**Chi tiết:**

- Hệ thống tra cứu `MembershipTier` bằng khoảng `points_from` ≤ `loyaltyPoints` < `points_to`.
- Hạng thành viên cung cấp `credit_limit` mặc định khi Lễ tân thiết lập hạn mức phòng.
- Tên hạng: `STANDARD`, `SILVER`, `GOLD`, `PLATINUM` (tùy cấu hình Admin).
- Không được xóa `MembershipTier` đang được tham chiếu bởi khách hàng.

**Nguồn:** Codebase `MembershipTier.java` · `Customer.loyaltyPoints`

---

## 11. BR-STAFF — Lịch làm việc Nhân viên

### BR-STAFF-01 — Quản lý Ca trực Nhân viên

**Mức độ:** MEDIUM

**Phát biểu:**
Lịch làm việc (`StaffSchedule`) và ca trực (`Shift`) của nhân viên phải được quản lý và không được trùng lấn để đảm bảo nhân lực vận hành đầy đủ mọi thời điểm.

**Chi tiết:**

- Một nhân viên không thể được gán hai ca trực trùng thời gian.
- Chỉ Admin và Manager có quyền tạo/sửa lịch ca trực.
- Lịch ca trực được kiểm tra trước khi phân công nhân viên vào `TourStaffAssignment`.

**Nguồn:** Codebase `Shift.java` · `StaffSchedule.java`

---

## 12. BR-WF — Workflow Engine Động

### BR-WF-01 — Quy tắc Kích hoạt Workflow Engine

**Mức độ:** HIGH

**Phát biểu:**
Hệ thống Workflow Engine động (`WorkflowEngineService`) xử lý các luồng nghiệp vụ cấu hình được (`Workflow` entity) bao gồm điều kiện (`conditions_json`) và hành động (`actions_json`). Engine **phải đảm bảo idempotency** — cùng một sự kiện kích hoạt không được thực thi workflow hai lần.

**Chi tiết:**

- `trigger_event`: định nghĩa sự kiện kích hoạt (VD: `BOOKING_CONFIRMED`, `PROMOTION_EXCEEDED`).
- `conditions_json`: điều kiện dạng JSON kiểm tra trước khi thực thi.
- `actions_json`: danh sách hành động dạng JSON (gửi email, thay đổi trạng thái, cảnh báo).
- Chỉ Admin mới được phép tạo/sửa/xóa Workflow.
- Workflow bị vô hiệu hóa (`is_active = false`) không được kích hoạt.

**Nguồn:** Codebase `Workflow.java` · `WorkflowEngineServiceImpl.java`

---

### BR-WF-02 — Ủy quyền Phê duyệt Đặt phòng (Pending Approval)

**Mức độ:** HIGH

**Phát biểu:**
Khi một booking vi phạm ngưỡng giảm giá do Staff áp dụng (`PROMOTION_EXCEEDED`), Workflow Engine tự động chuyển booking sang trạng thái `Pending_Approval`. Booking này **phải được Manager phê duyệt hoặc từ chối** trước khi tiến hành check-in.

**Chi tiết:**

- Workflow `PROMOTION_EXCEEDED` được cấu hình trong bảng `Workflows`.
- Manager nhận thông báo và có thể phê duyệt (→ `Confirmed`) hoặc từ chối (→ `Cancelled`).
- Ghi Audit Log khi có quyết định phê duyệt.

**Nguồn:** Codebase `WorkflowEngineServiceImpl.java` · `WorkflowApiController.java`

---

## 13. Phụ lục — Ma trận Tác nhân-Quyền hạn

| Business Rule | Guest | Customer | Receptionist | F&B Staff | Kitchen | Housekeeping | Maintenance | Tour Guide | Admin | Manager |
| :------------ | :---: | :------: | :----------: | :-------: | :-----: | :----------: | :---------: | :--------: | :---: | :-----: |
| BR-SYS-01     |  ✓  |    ✓    |      ✓      |    ✓    |   ✓   |      ✓      |     ✓     |     ✓     |  ✓  |   ✓   |
| BR-SYS-02     |  ✓  |    ✓    |      ✓      |    ✓    |   ✓   |      ✓      |     ✓     |     ✓     |  ✓  |   ✓   |
| BR-SYS-03     |  —  |    —    |      ✓      |    ✓    |   ✓   |      ✓      |     ✓     |     ✓     |  ✓  |   ✓   |
| BR-SYS-04     |  —  |    —    |      ✓      |    —    |   —   |      —      |     —     |     —     |  ✓  |   ✓   |
| BR-SYS-05     |  —  |    ✓    |      —      |    —    |   —   |      —      |     —     |     —     |  ✓  |   —   |
| BR-SYS-07     |  —  |    —    |      —      |    —    |   —   |      —      |     —     |     —     |  ✓  |   —   |
| BR-SYS-08     |  —  |    —    |      ✓      |    ✓    |   ✓   |      ✓      |     ✓     |     ✓     |  ✓  |   ✓   |
| BR-SYS-09     |  —  |    —    |      —      |    —    |   —   |      —      |     —     |     —     |  ✓  |   —   |
| BR-SYS-10     |  —  |    —    |      —      |    —    |   —   |      —      |     —     |     —     |  ✓  |   ✓   |
| BR-FO-01      |  —  |    ✓    |      ✓      |    —    |   —   |      —      |     —     |     —     |  —  |   —   |
| BR-FO-02      |  —  |    ✓    |      ✓      |    —    |   —   |      —      |     —     |     —     |  —  |   —   |
| BR-FO-03      |  —  |    —    |      ✓      |    —    |   —   |      —      |     —     |     —     |  —  |   —   |
| BR-FO-04      |  —  |    —    |      ✓      |    —    |   —   |      ✓      |     ✓     |     —     |  —  |   —   |
| BR-FO-07      |  —  |    —    |      ✓      |    —    |   —   |      —      |     —     |     —     |  ✓  |   —   |
| BR-FO-08      |  ✓  |    ✓    |      ✓      |    —    |   —   |      —      |     —     |     —     |  ✓  |   —   |
| BR-FB-01      |  —  |    ✓    |      —      |    ✓    |   —   |      —      |     —     |     —     |  —  |   —   |
| BR-FB-02      |  —  |    ✓    |      ✓      |    ✓    |   ✓   |      —      |     —     |     —     |  —  |   —   |
| BR-FB-05      |  —  |    ✓    |      ✓      |    ✓    |   —   |      —      |     —     |     —     |  ✓  |   ✓   |
| BR-TR-01      |  —  |    ✓    |      —      |    —    |   —   |      —      |     —     |     ✓     |  ✓  |   —   |
| BR-TR-02      |  —  |    —    |      —      |    —    |   —   |      —      |     —     |     ✓     |  —  |   —   |
| BR-TR-04      |  —  |    —    |      —      |    —    |   —   |      —      |     —     |     —     |  ✓  |   —   |
| BR-TR-06      |  —  |    —    |      —      |    —    |   —   |      —      |     —     |     ✓     |  ✓  |   —   |
| BR-TR-07      |  —  |    —    |      —      |    —    |   —   |      —      |     —     |     ✓     |  ✓  |   —   |
| BR-FIN-01     |  —  |    —    |      ✓      |    —    |   —   |      —      |     —     |     —     |  —  |   —   |
| BR-FIN-02     |  —  |    ✓    |      ✓      |    —    |   —   |      —      |     —     |     —     |  —  |   —   |
| BR-FIN-03     |  —  |    —    |      —      |    —    |   —   |      —      |     —     |     —     |  ✓  |   ✓   |
| BR-FIN-04     |  —  |    —    |      —      |    —    |   —   |      —      |     —     |     —     |  —  |   ✓   |
| BR-FIN-07     |  —  |    ✓    |      ✓      |    —    |   —   |      —      |     —     |     —     |  ✓  |   ✓   |
| BR-HK-01      |  —  |    —    |      ✓      |    —    |   —   |      ✓      |     —     |     —     |  —  |   —   |
| BR-DATA-01    |  —  |    —    |      —      |    —    |   —   |      —      |     —     |     —     |  ✓  |   —   |
| BR-DATA-02    |  —  |    —    |      ✓      |    —    |   —   |      —      |     —     |     —     |  —  |   —   |
| BR-DATA-04    |  —  |    —    |      ✓      |    —    |   —   |      —      |     —     |     —     |  —  |   —   |
| BR-DATA-05    |  —  |    —    |      —      |    —    |   —   |      —      |     —     |     —     |  ✓  |   ✓   |
| BR-MEM-01     |  —  |    ✓    |      ✓      |    —    |   —   |      —      |     —     |     —     |  ✓  |   ✓   |
| BR-STAFF-01   |  —  |    —    |      —      |    —    |   —   |      —      |     —     |     —     |  ✓  |   ✓   |
| BR-WF-01      |  —  |    —    |      —      |    —    |   —   |      —      |     —     |     —     |  ✓  |   —   |
| BR-WF-02      |  —  |    —    |      —      |    —    |   —   |      —      |     —     |     —     |  ✓  |   ✓   |

