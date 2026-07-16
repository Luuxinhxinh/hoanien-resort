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
- [4. BR-RSV &amp; BR-FO — Đặt phòng &amp; Tiền sảnh (Reservation &amp; Front Office)](#4-br-rsv--br-fo--đặt-phòng--tiền-sảnh-reservation--front-office)
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

| Prefix   | Nhóm nghiệp vụ                                          |
| -------- | ---------------------------------------------------------- |
| BR-SYS   | Bảo mật & Xác thực Hệ thống                          |
| BR-RSV   | Đặt phòng & Đăng ký Dịch vụ (Reservation)          |
| BR-FO    | Tiền sảnh / Front Office (Check-in, Check-out, Hỗ trợ) |
| BR-FB    | Ẩm thực & Nhà hàng (F&B / POS / KDS)                   |
| BR-TR    | Lữ hành & Đánh giá (Tour & Review)                    |
| BR-FIN   | Tài chính & Thanh toán                                  |
| BR-HK    | Buồng phòng & Bảo trì                                  |
| BR-DATA  | Quản trị Dữ liệu & Tuân thủ Pháp lý                |
| BR-MEM   | Hội viên & Tích điểm Khách hàng                     |
| BR-STAFF | Lịch làm việc & Ca trực Nhân viên                    |
| BR-WF    | Workflow Engine Động (Business Rule Engine)              |

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

---

### BR-SYS-03 — Phiên Đăng nhập Nhân viên

**Mức độ:** HIGH

**Phát biểu:**
Phiên làm việc (Session) của nhân viên hệ thống phải **tự động hết hạn sau 15 phút không hoạt động** (inactivity timeout).

**Chi tiết:**

- Áp dụng cho tất cả các vai trò nhân viên: Receptionist, F&B Staff, Kitchen Staff, Housekeeping, Maintenance, Tour Guide, Admin, Manager.
- Khi phiên hết hạn, hệ thống phải chuyển hướng người dùng về trang đăng nhập.

---

### BR-SYS-04 — Ghi Audit Log Bắt buộc

**Mức độ:** CRITICAL

**Phát biểu:**
Tất cả các hành động quan trọng sau **phải được ghi vào Audit Log** và **không thể bị xóa hoặc chỉnh sửa**:

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

---

### BR-SYS-06 — Yêu cầu Mật khẩu Mạnh

**Mức độ:** HIGH

**Phát biểu:**
Mật khẩu người dùng phải đáp ứng tối thiểu các tiêu chí:

- Tối thiểu **8 ký tự**.
- Chứa ít nhất **1 chữ hoa** (A-Z).
- Chứa ít nhất **1 chữ thường** (a-z).
- Chứa ít nhất **1 chữ số** (0-9).
- Số điện thoại đăng ký phải có **10-12 số**.

---

### BR-SYS-07 — Phân quyền Vai trò (RBAC)

**Mức độ:** CRITICAL

**Phát biểu:**
Mỗi nhân viên chỉ có **duy nhất một vai trò chính** tại một thời điểm. Hệ thống phải tự động giới hạn quyền truy cập API và giao diện theo vai trò được gán.

**Chi tiết:**

- Nhân viên không thể tự thay đổi vai trò của mình.
- Chỉ Admin mới có quyền gán/thay đổi vai trò nhân viên.
- Vai trò được ánh xạ trực tiếp đến danh sách màn hình và API được phép truy cập.

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

---

### BR-SYS-09 — Quản trị Workflow Động

**Mức độ:** HIGH

**Phát biểu:**
Chỉ Admin mới có quyền cấu hình, sửa đổi các quy trình workflow tự động. Việc cấu hình phải được thực hiện thông qua JSON hợp lệ.

**Chi tiết:**

- `Workflows` entity lưu trữ điều kiện (`conditions_json`) và hành động (`actions_json`).
- Workflow chỉ được kích hoạt nếu `is_active = true`.

---

### BR-SYS-10 — Nhật ký Xuất Dữ liệu (Export History Audit)

**Mức độ:** HIGH

**Phát biểu:**
Mọi hành động xuất dữ liệu (Export Excel/PDF) liên quan đến thông tin khách hàng hoặc báo cáo tài chính phải được hệ thống tự động ghi nhận vào nhật ký xuất dữ liệu (`ExportHistory`).

**Chi tiết:**

- Nhật ký ghi nhận: ID tài khoản thực hiện, thời gian, loại dữ liệu xuất, số lượng dòng dữ liệu, địa chỉ IP.
- Dữ liệu nhật ký này không được phép sửa đổi hoặc xóa bởi bất kỳ người dùng nào ngoại trừ System Admin.

---

## 4. BR-RSV & BR-FO — Đặt phòng & Tiền sảnh (Reservation & Front Office)

### Nhóm BR-RSV — Đặt phòng & Phân bổ

### BR-RSV-01 — Kiểm tra hợp lệ ngày tháng và số lượng phòng

**Mức độ:** HIGH

**Phát biểu:**
Kiểm tra tính hợp lệ của ngày nhận/trả phòng (không được nằm trong quá khứ; ngày trả > ngày nhận) và đảm bảo số lượng phòng yêu cầu nhỏ hơn hoặc bằng số lượng phòng trống trong danh sách.

### BR-RSV-02 — Hiển thị phòng trống theo điều kiện

**Mức độ:** HIGH

**Phát biểu:**
Hệ thống chỉ hiển thị các phòng trống thỏa mãn điều kiện tìm kiếm (loại trừ các phòng đang bị khóa tạm thời - soft-locked).

### BR-RSV-03 — Bắt buộc đăng nhập để đặt phòng

**Mức độ:** HIGH

**Phát biểu:**
Bắt buộc phải đăng nhập để đặt phòng. Nếu chưa đăng nhập, người dùng sẽ bị chuyển hướng đến trang Đăng nhập/Đăng ký.

### BR-RSV-04 — Yêu cầu đặt cọc và Hủy tự động

**Mức độ:** HIGH

**Phát biểu:**
Tất cả các đặt phòng trực tuyến đều yêu cầu thanh toán tiền cọc trước khi được xác nhận. Đơn đặt phòng sẽ giữ trạng thái "Chờ thanh toán" trong 2 phút; nếu không nhận được tiền cọc trong khoảng thời gian này, hệ thống sẽ tự động hủy đơn và giải phóng phòng trả lại kho.

### BR-RSV-05 — Ngăn chặn đặt phòng trùng lặp (Overbooking)

**Mức độ:** HIGH

**Phát biểu:**
Hệ thống phải ngăn chặn tình trạng đặt quá số lượng (overbooking) bằng cách đảm bảo rằng cùng một phòng vật lý không thể được gán cho các đơn đặt phòng có thời gian lưu trú trùng lặp.

### BR-RSV-06 — Giá phòng tại thời điểm tìm kiếm

**Mức độ:** HIGH

**Phát biểu:**
Giá hiển thị là mức giá áp dụng tại thời điểm tìm kiếm (đã bao gồm phụ phí ngày lễ, cuối tuần và các chương trình khuyến mãi).

### BR-RSV-07 — Xác nhận đặt phòng sau khi thanh toán

**Mức độ:** HIGH

**Phát biểu:**
Trạng thái đặt phòng chuyển từ Chờ thanh toán sang Đã xác nhận khi thanh toán thành công, đồng thời kích hoạt email xác nhận gửi đến khách hàng.

### BR-RSV-08 — Quyền hủy đơn đặt phòng

**Mức độ:** HIGH

**Phát biểu:**
Chỉ khách hàng sở hữu đơn đặt phòng mới có quyền hủy đơn đặt phòng đó.

### BR-RSV-09 — Chặn hủy phòng đối với đơn đã check-in

**Mức độ:** HIGH

**Phát biểu:**
Không thể hủy các đơn đặt phòng đang ở trạng thái Đã nhận phòng (Checked-In) hoặc Đã hủy (Cancelled).

### BR-RSV-10 — Giải phóng phòng khi hủy đơn

**Mức độ:** HIGH

**Phát biểu:**
Khi một đơn đặt phòng bị hủy, toàn bộ số lượng phòng đã giữ sẽ được giải phóng ngay lập tức để cho phép đặt phòng trong tương lai.

### BR-RSV-11 — Quản lý khách đi cùng

**Mức độ:** HIGH

**Phát biểu:**
Khách hàng chỉ được phép thêm, xóa và gán khách đi cùng (accompanying/dependent guests) vào phòng thuộc một đơn đặt phòng hợp lệ hiện có. Khách đi cùng phải được đăng ký vào đơn đặt phòng trước khi gán phòng, và phòng được gán phải tuân thủ giới hạn sức chứa tối đa.

### Nhóm BR-FO — Lễ tân & Trải nghiệm lưu trú

### BR-FO-01 — Representative guest must be ≥...

**Mức độ:** HIGH

**Phát biểu:**
Representative guest must be ≥ 18 years old and provide valid identification (ID/Passport).

### BR-FO-02 — Rush Rooms have Vacant_Dirty are...

**Mức độ:** HIGH

**Phát biểu:**
Rush Rooms have Vacant_Dirty are prioritized and moved to the top of the housekeeping task queue.

### BR-FO-03 — Only the Primary Guest (Representative)...

**Mức độ:** HIGH

**Phát biểu:**
Only the Primary Guest (Representative) of the reservation is authorized to request account upgrades, service authorizations, or administrative changes for the booking. Dependents are restricted from these actions.

### BR-FO-04 — Reservations not checked in by...

**Mức độ:** HIGH

**Phát biểu:**
Reservations not checked in by 00:00 of the next day are marked No-Show with 100% deposit retention.

### BR-FO-05 — Only guests with an active...

**Mức độ:** HIGH

**Phát biểu:**
Only guests with an active In-House reservation may request a room category change. Room category change requires availability and Vacant_Clean status in the new category.

### BR-FO-06 — Walk-in guests must provide valid...

**Mức độ:** HIGH

**Phát biểu:**
Walk-in guests must provide valid identification before check-in.

### BR-FO-07 — Each reservation must be assigned...

**Mức độ:** HIGH

**Phát biểu:**
Each reservation must be assigned to a specific room at check-in and update reservation status to "Checked-In" after successful check-in.

### BR-FO-08 — Walk-in guests without an account...

**Mức độ:** HIGH

**Phát biểu:**
Walk-in guests without an account automatic generates a customer account and a temporary password for walk-in guests, then notifies the customer to update their credentials.

### BR-FO-09 — Guests must be registered for...

**Mức độ:** HIGH

**Phát biểu:**
Guests must be registered for temporary residence reporting per local regulations.

### BR-FO-10 — The reservation shall be linked...

**Mức độ:** HIGH

**Phát biểu:**
The reservation shall be linked to the customer account.

### BR-FO-11 — A dependent guest shall not...

**Mức độ:** HIGH

**Phát biểu:**
A dependent guest shall not be registered more than once under the same reservation. The system shall prevent the registration of a dependent guest whose identification document number (CCCD/Passport) already exists in the Booking_Guests list of that reservation.

### BR-FO-12 — Room category changes shall only...

**Mức độ:** HIGH

**Phát biểu:**
Room category changes shall only be finalized after a specific room in the requested category has been assigned to the reservation.

## 5. BR-FB — Ẩm thực & Nhà hàng (F&B)

### BR-FB-01 — Room Service and Table Reservation...

**Mức độ:** HIGH

**Phát biểu:**
Room Service and Table Reservation services are strictly applicable to guests with an active and valid stay status at the hotel.

### BR-FB-02 — For walk-in guests, the system...

**Mức độ:** HIGH

**Phát biểu:**
For walk-in guests, the system only supports Dine-in services, which must be facilitated by on-site staff.

### BR-FB-03 — When a guest searches for...

**Mức độ:** HIGH

**Phát biểu:**
When a guest searches for a table by date and time, the system shall only display available tables. Tables that are already reserved or have conflicting schedules during the searched timeframe will be grayed out (disabled) on the user interface.

### BR-FB-04 — The system shall reject table...

**Mức độ:** HIGH

**Phát biểu:**
The system shall reject table reservation requests if the number of guests exceeds the designated maximum capacity of the selected table.

### BR-FB-05 — Upon accessing the Room Service...

**Mức độ:** HIGH

**Phát biểu:**
Upon accessing the Room Service interface, the system defaults to displaying the menu applicable for the current day.

### BR-FB-06 — Guests are permitted to browse...

**Mức độ:** HIGH

**Phát biểu:**
Guests are permitted to browse the menu for all days of the week. However, order placement is restricted strictly to currently available items on the current day's menu.

### BR-FB-07 — For Room Service orders utilizing...

**Mức độ:** HIGH

**Phát biểu:**
For Room Service orders utilizing the CHARGE_TO_ROOM payment method, the system will only process the order if the room's Credit Limit is greater than the total order value.

### BR-FB-08 — If a table reservation request...

**Mức độ:** HIGH

**Phát biểu:**
If a table reservation request lacks an end time, the system will automatically assign a default duration of 60 minutes calculated from the start time.

### BR-FB-09 — Upon successful payment of a...

**Mức độ:** HIGH

**Phát biểu:**
Upon successful payment of a Dine-in order (payOrder), the system automatically updates the reservation status to Complete. Concurrently, the physical table status shifts to Cleaning, and the system logs the start time of the cleaning process.

### BR-FB-10 — Once a Room Service order...

**Mức độ:** HIGH

**Phát biểu:**
Once a Room Service order is confirmed as delivered (status changed to Served), the system automatically flags the order as paid (isPaidInPos = true), as the financial liability is transferred to the master room folio.

### BR-FB-11 — Order cancellation by either the...

**Mức độ:** HIGH

**Phát biểu:**
Order cancellation by either the guest or staff is only permitted when the order is in the Pending status.

### BR-FB-12 — In the event of an...

**Mức độ:** HIGH

**Phát biểu:**
In the event of an order cancellation:

- If the CHARGE_TO_ROOM method was selected, the system automatically reverses the charge on the room folio.
- If payment was made online or via bank transfer, the system prompts for bank details and automatically generates a RefundRequest routed to the Manager for approval.

### BR-FB-13 — When creating a Room Service...

**Mức độ:** HIGH

**Phát biểu:**
When creating a Room Service order via the POS interface, F&B Staff are required to verify and input the last 4 digits of the guest's ID card for security purposes. The system allows order creation only if the room's Credit Limit exceeds the total order value.

### BR-FB-14 — When staff place a table...

**Mức độ:** HIGH

**Phát biểu:**
When staff place a table reservation for a guest, inputting the room number is mandatory. The reservation request will be denied if the specified room does not hold a valid, active guest stay.

### BR-FB-15 — For guests with an existing...

**Mức độ:** HIGH

**Phát biểu:**
For guests with an existing reservation, F&B staff must perform check-in verification using the guest's ID upon arrival at the restaurant. Only after successful verification will the table status update to Seated.

### BR-FB-16 — Add-on orders are only applicable...

**Mức độ:** HIGH

**Phát biểu:**
Add-on orders are only applicable to Dine-in services. The system automatically consolidates add-ons into the master Food Order while generating independent Order Detail lines to assist the Kitchen in tracking preparation progress per batch.

### BR-FB-17 — The system automatically applies a...

**Mức độ:** HIGH

**Phát biểu:**
The system automatically applies a 5% service charge to the total invoice prior to posting for CHARGE_TO_ROOM orders. Conversely, a 2% service charge discount is applied if the guest prepays via a digital payment gateway (e-wallet/card).

### BR-FB-18 — During available table suggestions or...

**Mức độ:** HIGH

**Phát biểu:**
During available table suggestions or reservation conflict checks, the system automatically appends a 15-minute buffer after the preceding reservation's end time to allow staff sufficient turnaround (cleaning) time.

### BR-FB-19 — For Room Service orders that...

**Mức độ:** HIGH

**Phát biểu:**
For Room Service orders that have reached the Complete (preparation finished) status, F&B staff must execute the "Deliver" and "Confirm Delivery" actions within the system to finalize the order workflow.

### BR-FB-20 — The Estimated Time of Arrival...

**Mức độ:** HIGH

**Phát biểu:**
The Estimated Time of Arrival (etaMins) for an order is calculated using the following formula: [Preparation time of the longest item] + [2 minutes x (Total items - 2)] + [5 minutes transit time]. The base preparation time is hardcoded by category: Main = 20 mins, Beverage = 5 mins, and other items = 10 mins.

### BR-FB-21 — When staff enter a name...

**Mức độ:** HIGH

**Phát biểu:**
When staff enter a name for a walk-in guest, the system automatically prepends Guest: to the notes field. Similarly, when a table is placed on Hold, the system parses and inserts a [HELD: Xm] tag into the Special Requests field for optimal visibility by F&B staff.

### BR-FB-22 — The system enforces a strict...

**Mức độ:** HIGH

**Phát biểu:**
The system enforces a strict state synchronization constraint: the status of individual Kitchen Order Tickets (KOT) automatically inherits the status of the master order. (e.g., If an order transitions to Preparing, all associated items currently in Pending will automatically shift to Preparing).

### BR-FB-23 — Role-Based Access Control (RBAC) restricts...

**Mức độ:** HIGH

**Phát biểu:**
Role-Based Access Control (RBAC) restricts Kitchen Staff permissions to solely updating item availability (In Stock/Out of Stock). This role is denied edit access to other configuration data, such as listed prices or serving dates.

### BR-FB-24 — The system only allows staff...

**Mức độ:** HIGH

**Phát biểu:**
The system only allows staff to create Dine-in orders and accept Table Reservations within the designated operating hours from 08:00 to 22:59 daily.

### BR-FB-25 — Tables currently in "Cleaning", "Out...

**Mức độ:** HIGH

**Phát biểu:**
Tables currently in "Cleaning", "Out of service", or "Occupied/Seated" statuses cannot be used to create new, independent Dine-in orders or accept Table Reservations within the next 2 hours. (Note: For "Occupied" tables, any attempt to order items will be treated as an Add-on and automatically merged into the current guest's active bill).

### BR-FB-26 — When creating a Dine-in order...

**Mức độ:** HIGH

**Phát biểu:**
When creating a Dine-in order for walk-in guests, the system automatically cross-checks the table's reservation schedule for the day. The request to open the table will be rejected if there is a confirmed reservation with a starting time (Reserve Time) within 2 hours from the current time.

## 6. BR-TR — Lữ hành & Đánh giá (Tour & Review)

### 1. Tour Booking & Payment Constraints (UC20.1)

### BR-TR-01 — Capacity Control / Double-Booking Prevention

**Mức độ:** HIGH

**Phát biểu:**
The system strictly enforces vacancy checks (availableSlots = maxCapacity - confirmedSeats). If the requested participant count exceeds remaining slots, booking is rejected with exception TOUR-001 (Out of seats).

### BR-TR-04 — Age-Based Discount Rules

**Mức độ:** HIGH

**Phát biểu:**
Tour ticket prices are calculated dynamically based on passenger age groups:

- Infants (Under 2 years old): 100% Free.
- Children (2 - 11 years old): 50% discount on the base price (basePrice * 0.5).
- Adults (12 years old and above): 100% full price (basePrice).

### BR-TR-07 — Mandatory Travel Insurance

**Mức độ:** HIGH

**Phát biểu:**
For active adventure tours (isInsuranceRequired = true), guests must purchase travel insurance (acceptInsurance = true). Refusal triggers exception TOUR-INS-001. The insurance fee (insurancePrice * participantCount) is appended to the total price, and the system auto-generates a policy number: INS-YYYYMMDD-SCH{id}-{UUID}.

### BR-TR-08 — Post to Room Stay Requirement

**Mức độ:** HIGH

**Phát biểu:**
To charge tour expenses directly to a room folio, a valid checked-in room's detail ID (roomBookingDetailId) must be provided. Missing room detail triggers error TOUR-004; invalid IDs trigger error TOUR-005.

### BR-TR-09 — Folio Credit Limit Validation

**Mức độ:** HIGH

**Phát biểu:**
When selecting Post to Room payment, the system validates the room's remaining credit limit (subCreditLimit - usedAmount). If the tour's total price exceeds this limit, booking is blocked, throwing an exception TOUR-LIMIT. Guests must pay off existing debts or choose online payment.

### BR-TR-11 — Promotion Usage Limitation

**Mức độ:** HIGH

**Phát biểu:**
Each promotional code can only be used by a customer exactly once (uses >= 1 throws [ERR_PROMO_USAGE_EXCEEDED]). Promotions must be active and within their expiration range (validTo >= LocalDate.now()).

### 2. Attendance & Tour Operation Constraints (UC21 & UC20.2)

### BR-TR-02 — AI Face Match Score Threshold

**Mức độ:** HIGH

**Phát biểu:**
During AI Face Scan attendance verification, the matched face score returned from JavaScript comparison must meet the minimum 85% threshold (MIN_MATCH_SCORE_FOR_ATTENDANCE = 0.85) to automatically update status to PRESENT / Checked_In.

### BR-TR-03 — Mandatory Full Attendance Before Departure

**Mức độ:** HIGH

**Phát biểu:**
A tour guide is blocked from starting a tour schedule (startTour) if there is any passenger with a status other than Checked_In (e.g. Not_Show). If incomplete, departure is blocked, returning a toast message start_failed_pax.

### BR-TR-06 — Minimum Passenger Warning - Minimum Pax

**Mức độ:** HIGH

**Phát biểu:**
The system scans schedules 24 hours prior to departure. If booking count does not meet the minimum pax threshold, the admin is warned, though staff/vehicle assignments can still proceed.

### BR-TR-10 — Special Name Mapping - FaceID Fallback

**Mức độ:** HIGH

**Phát biểu:**
To account for scanner precision variations in variable lighting, the system maps "Ngọc Thị" and "Lê Quang" interchangeably within the isNameMatch name comparison logic.

### 3. Cancellation & Tour Modification Constraints (UC20.3 & UC08)

### BR-TR-05 — Cancellation & Refund Rules

**Mức độ:** HIGH

**Phát biểu:**

- Resort-Initiated Cancellation: 100% full refund to the customer. Booking status updates to Cancelled_Refunded.

  - Guest-Initiated Cancellation: If cancelled within 24 hours prior to departure, a 50% deposit penalty is charged (only 50% is refunded). Booking status updates to Cancelled_Forfeited.

### BR-TR-13 — Active Tour Editing & Deletion Restriction

**Mức độ:** HIGH

**Phát biểu:**
Modification of base prices or soft deleting a Tour is strictly forbidden if that Tour is associated with at least one active schedule in Open status. Violating actions trigger a ResourceInUseException.

## 7. BR-FIN — Tài chính & Thanh toán

### BR-FIN-01 — Chính sách hoàn tiền khi hủy phòng

**Mức độ:** HIGH

**Phát biểu:**
Các đơn đặt phòng hủy trước ít nhất 48 giờ (≥ 48 giờ) so với thời gian nhận phòng dự kiến sẽ được hoàn lại 100% tiền cọc. Các đơn đặt phòng hủy trước dưới 48 giờ, khách không đến (no-show), hoặc các đơn đã hoàn tất nhận phòng sẽ không được hoàn tiền.

### BR-FIN-02 — Hỗ trợ thanh toán chuyển khoản trực tuyến

**Mức độ:** HIGH

**Phát biểu:**
Hệ thống hỗ trợ thanh toán chuyển khoản cho các đơn đặt phòng trực tuyến. Đơn đặt phòng chỉ được coi là đã thanh toán sau khi giao dịch được cổng thanh toán xác nhận thành công.

### BR-FIN-03 — Xác minh thanh toán trước khi nhận phòng

**Mức độ:** HIGH

**Phát biểu:**
Thủ tục nhận phòng chỉ được hoàn tất sau khi tiền cọc hoặc khoản đảm bảo thanh toán yêu cầu đã được xác minh thành công theo chính sách của khách sạn.

### BR-FIN-04 — Tính phí khi nâng hạng phòng

**Mức độ:** HIGH

**Phát biểu:**
Khi khách hàng nâng cấp lên hạng phòng cao hơn trong thời gian lưu trú, hệ thống sẽ tính toán khoản phí phát sinh dựa trên chênh lệch giữa giá phòng hiện tại và giá phòng mới cho tất cả các đêm còn lại của kỳ lưu trú.

### BR-FIN-05 — Không hoàn tiền khi hạ hạng phòng

**Mức độ:** HIGH

**Phát biểu:**
Việc hạ cấp xuống hạng phòng thấp hơn sau khi nhận phòng sẽ không được hoàn tiền, cấp tín dụng hoặc giảm trừ các khoản phí phòng đã thỏa thuận trước đó.

### BR-FIN-06 — Quy tắc xác định nâng hạng và hạ hạng phòng

**Mức độ:** HIGH

**Phát biểu:**
Hệ thống xác định việc nâng hạng và hạ hạng phòng dựa trên giá phòng. Việc chuyển sang hạng phòng có giá cao hơn được coi là nâng hạng, trong khi chuyển sang hạng phòng có giá thấp hơn được coi là hạ hạng.

## 8. Mod 5 — Quản lý Vận hành & Báo cáo (Operations, Housekeeping, Manager)

### 1. Folio Aggregation (Gom hóa đơn & Ký nợ phòng)

### BR-FO-01 — Điều kiện Ký nợ Post-to-Room

**Mức độ:** HIGH

**Phát biểu:**
Khách hàng chỉ được phép ký nợ hóa đơn dịch vụ (F&B, Tour) vào ví phòng (Folio) khi trạng thái lưu trú đang là Checked_In.

### BR-REC-02 — Ràng buộc Hạn mức Tín dụng Tổng dư nợ hiện tại...

**Mức độ:** HIGH

**Phát biểu:**
Tổng dư nợ hiện tại cộng với giao dịch Ký nợ mới không được phép vượt quá hạn mức nợ (Credit Limit) của phòng. Nếu vượt, giao dịch bị từ chối trừ khi được Lễ tân hoặc Manager can thiệp nâng hạn mức.

### BR-FO-03 — Xác thực Giao dịch Khách hàng bắt buộc phải...

**Mức độ:** HIGH

**Phát biểu:**
Khách hàng bắt buộc phải nhập mã PIN (so khớp mã băm) hoặc ký tên xác nhận (lưu vào signature_img_url) khi thực hiện Post-to-Room từ các điểm dịch vụ.

### BR-FO-04 — Gom Hóa Đơn Tự Động Checkout

**Mức độ:** HIGH

**Phát biểu:**
Khi khách trả phòng, hệ thống tự động quét và cộng dồn toàn bộ Folio_Items chưa được thanh toán riêng. Sau khi trừ đi khoản tiền cọc, hệ thống tính ra tổng tiền dư nợ thực tế cần thanh toán.

### BR-REC-05 — Bắt buộc Kiểm phòng Room Check

**Mức độ:** HIGH

**Phát biểu:**
Không thể xuất hóa đơn cuối cùng nếu Housekeeping chưa hoàn thành việc kiểm tra phòng (Minibar/Hỏng hóc). Lệnh ROOM_CHECK phải chuyển sang trạng thái Completed.

### 2. Night Audit & Thanh toán (Kiểm toán đêm)

### BR-FIN-01b | Kiểm toán Đêm Tự Động — Night Audit

**Mức độ:** HIGH

**Phát biểu:**
Quy trình đóng sổ phải được chạy ngầm tự động vào lúc 02:00 AM mỗi ngày. Hệ thống thực hiện quét các phòng Checked_In, tự động sinh một dòng tiền phòng mới vào hóa đơn tổng, và dịch chuyển ngày vận hành của hệ thống sang ngày tiếp theo.

### BR-FIN-02b | Chốt chặn Check-out — Check-out tuyệt đối bị khóa...

**Mức độ:** HIGH

**Phát biểu:**
Check-out tuyệt đối bị khóa chặn nếu hóa đơn tổng hợp chưa có số dư nợ bằng 0. Tất cả các Folio_Items có trạng thái Pending bắt buộc phải được tất toán xong xuôi.

### BR-FIN-03b | Chính sách Hoàn tiền — Refund

**Mức độ:** HIGH

**Phát biểu:**

- Hủy trước 48 giờ so với ngày Check-in: Hoàn 100% cọc.

  - Hủy trong vòng 48 giờ hoặc No-show: Mất 100% cọc.

### BR-FIN-04b | Phê duyệt Hoàn tiền thủ công — Mọi yêu cầu hoàn tiền...

**Mức độ:** HIGH

**Phát biểu:**
Mọi yêu cầu hoàn tiền không thuộc luồng tự động (do sự cố, thiên tai) phải tạo RefundRequest và được Manager phê duyệt.

### 3. Manager Dashboard & Báo cáo

### BR-RPT-01 — Phân loại Doanh thu chuẩn USALI Doanh thu hệ thống bắt...

**Mức độ:** HIGH

**Phát biểu:**
Doanh thu hệ thống bắt buộc phải được bóc tách làm 3 luồng riêng biệt: Doanh thu Phòng (Room), Ẩm thực (F&B) và Lữ hành (Tour).

### BR-RPT-02 — Thống kê Dashboard Thời Gian Thực Manager Dashboard tính toán và...

**Mức độ:** HIGH

**Phát biểu:**
Manager Dashboard tính toán và hiển thị các chỉ số cốt lõi: Tỷ lệ lấp đầy (Occupancy Rate), tỷ lệ bán món ăn, tỷ lệ bán tour. Hệ thống phải vẽ đồ thị doanh thu lũy kế dựa theo các bộ lọc thời gian.

### BR-RPT-03 — Báo cáo Lợi Nhuận Gộp GOP

**Mức độ:** HIGH

**Phát biểu:**
Hệ thống cung cấp báo cáo tính toán Lợi nhuận Hoạt động Gộp cho từng bộ phận để Manager theo dõi hiệu suất tài chính tổng thể.

### 4. Buồng phòng & Bảo trì (Housekeeping & Maintenance)

### BR-HK-01 — Khởi tạo Task Dọn phòng Tự động Ngay khi Lễ tân hoàn...

**Mức độ:** HIGH

**Phát biểu:**
Ngay khi Lễ tân hoàn tất Check-out, tự động sinh một công việc dọn dẹp và chuyển trạng thái phòng sang Vacant_Dirty.

### BR-HK-02 — Ghi nhận Tiêu dùng Minibar Nhân viên buồng phòng khi...

**Mức độ:** HIGH

**Phát biểu:**
Nhân viên buồng phòng khi kiểm tra phòng check-out phải khai báo số lượng đồ uống/snack đã sử dụng. Hệ thống lập tức đẩy khoản phí này vào Folio của khách hàng theo đơn giá niêm yết.

### BR-HK-03 — Yêu cầu Dọn khẩn cấp Rush Room

**Mức độ:** HIGH

**Phát biểu:**
Khi Lễ tân đánh dấu một phòng là "Rush Room", độ ưu tiên của Task Housekeeping được nâng lên mức cao nhất, kích hoạt thông báo Real-time (Push Notification/Toast) đẩy về màn hình của Housekeeping.

### BR-HK-04 — Báo cáo Đồ thất lạc Lost & Found

**Mức độ:** HIGH

**Phát biểu:**
Tài sản khách để quên phải được khai báo tài sản khách để quên, có ảnh chụp minh chứng và lưu kho chờ bộ phận CSKH xử lý (thường là 30 - 90 ngày).

### BR-HK-06 — Báo cáo Hỏng hóc Create Ticket

**Mức độ:** HIGH

**Phát biểu:**
Khi phát hiện thiết bị hỏng, Housekeeping tạo báo cáo. Hệ thống tự động sinh phiếu MAINTENANCE và chuyển phòng sang trạng thái chờ bảo trì. Nếu lỗi do khách làm hỏng (Vỡ ly, cháy thảm), Housekeeping có thể đính kèm phí đền bù (Damage Fee) đẩy thẳng vào Folio.

### BR-MT-01 — Ràng buộc Trạng thái Phòng Out of Order

**Mức độ:** HIGH

**Phát biểu:**
Phòng đang ở trạng thái bảo trì Maintenance sẽ bị đóng băng. Hệ thống Booking Engine hoặc Lễ tân không thể nhìn thấy hoặc gán phòng này cho khách Check-in để ngăn chặn Overbooking.

### BR-MT-03 — Khôi phục Trạng thái Sau Bảo trì Khi nhân viên báo cáo...

**Mức độ:** HIGH

**Phát biểu:**
Khi nhân viên báo cáo hoàn thành sửa chữa, trạng thái phòng KHÔNG được tự động chuyển thành Vacant_Clean, mà phải chuyển về Vacant_Dirty để Housekeeping vào dọn dẹp vệ sinh bụi bẩn sau sửa chữa trước khi đón khách.

### 5. Check-out & Rời phòng (Front Office & Finance)

### BR-CO-01 — Điều kiện Tiên quyết Zero Balance

**Mức độ:** HIGH

**Phát biểu:**
Thủ tục Check-out bị khóa hoàn toàn nếu tổng hóa đơn (Consolidated_Invoice) chưa được thanh toán sạch. Hệ thống tự động chặn và trả về mã lỗi FOLIO-001 nếu dư nợ > 0.

### BR-CO-02 — Phát hành Hóa đơn Điện tử e-Invoice

**Mức độ:** HIGH

**Phát biểu:**
Ngay sau khi Lễ tân xác nhận Check-out thành công và số dư Folio = SETTLED, hệ thống tự động sinh hóa đơn điện tử định dạng PDF và kích hoạt luồng gửi Email (qua SendGrid) tới khách hàng.

### BR-CO-03 — Đóng vòng đời Lưu trú & Kích hoạt Dọn dẹp Check-out thành công sẽ tự...

**Mức độ:** HIGH

**Phát biểu:**
Check-out thành công sẽ tự động cập nhật trạng thái phòng thành Vacant_Dirty và sinh task dọn phòng cho Housekeeping (đã đề cập ở BR-HK-01).

### BR-CO-04 — Quyền lợi Đánh giá Review Deadline

**Mức độ:** HIGH

**Phát biểu:**
Chỉ những khách hàng đã thực hiện Check-out thành công mới được quyền viết đánh giá (Review). Thời hạn cho phép gửi đánh giá là 7 ngày kể từ ngày Check-out (review_deadline = completed_date + 7 days).

### 6. Quản lý (Manager) & Vận hành Hệ thống (System)

### BR-MNG-01 — Phân loại Doanh thu USALI Mọi khoản thu trong hệ...

**Mức độ:** HIGH

**Phát biểu:**
Mọi khoản thu trong hệ thống bắt buộc phải được bóc tách làm 3 luồng riêng biệt: Doanh thu Phòng (Room), Doanh thu Ẩm thực (F&B) và Doanh thu Lữ hành (Tour) dựa trên trường source_department để xuất báo cáo lợi nhuận gộp chuẩn USALI.

### BR-MNG-02 — Thẩm quyền Phê duyệt Manager Approval

**Mức độ:** HIGH

**Phát biểu:**
Các tác vụ rủi ro cao liên quan đến tài chính và vận hành bắt buộc phải có sự phê duyệt của Manager mới được thực thi.

### BR-MNG-03 — Kiểm toán Đêm Night Audit - Định kỳ

**Mức độ:** HIGH

**Phát biểu:**
Quy trình Night Audit phải chạy ngầm tự động bằng Cronjob vào lúc 02:00 AM mỗi ngày. Hệ thống tính toán tiền phòng của ngày hôm đó cộng vào Folio của các phòng đang Checked_In và đóng sổ chuyển sang ngày mới.

### BR-MNG-04 — Chốt chặn Đóng ca trước Night Audit Tiến trình Night Audit sẽ...

**Mức độ:** HIGH

**Phát biểu:**
Tiến trình Night Audit sẽ báo lỗi hoặc tạm dừng nếu phát hiện nhân viên F&B/POS chưa chốt sổ bán hàng trong ngày (End of Day). Manager có quyền thực thi "Cưỡng chế đóng ca" (Force Close) để Night Audit tiếp tục chạy.

### BR-MNG-05 — Tính toàn vẹn Dữ liệu Hủy Cancellation Consistency

**Mức độ:** HIGH

**Phát biểu:**
Khi một Yêu cầu Hoàn tiền (Refund Request) được khởi tạo và phê duyệt, bản ghi dịch vụ gốc (Booking, FoodOrder, TourBooking) bắt buộc phải chuyển trạng thái sang Cancelled để đảm bảo báo cáo doanh thu cuối tháng không bị ảo.

## 9. BR-DATA — Quản trị Dữ liệu & Tuân thủ Pháp lý

### BR-DATA-01 — Ràng buộc Xóa Dữ liệu Gốc

**Mức độ:** HIGH

**Phát biểu:**
Hệ thống nghiêm cấm xóa các dữ liệu nền (Master Data) khi chúng đang được sử dụng:

- **Không xóa phòng** đang có khách lưu trú.
- **Không xóa bàn ăn** đang có đặt chỗ trước.
- **Không xóa tour** đang ở trạng thái Active/Scheduled.

---

### BR-DATA-02 — Khai báo Tạm trú

**Mức độ:** CRITICAL

**Phát biểu:**
Thông tin khai báo lưu trú phải thu thập đầy đủ cho mỗi khách: Họ tên, Ngày sinh, Số CCCD, Giới tính, Quốc tịch — nhằm đáp ứng yêu cầu theo **Luật Cư trú 2020**.

---

### BR-DATA-03 — Tính Nhất quán Giao dịch

**Mức độ:** CRITICAL

**Phát biểu:**
Quá trình tạo tài khoản nhân viên (bao gồm `Accounts` và `Employees`) phải được bao trong **một giao dịch CSDL duy nhất** (`@Transactional`). Nếu bất kỳ bước nào thất bại, toàn bộ giao dịch phải được **rollback**.

---

### BR-DATA-04 — Khai báo Khách trong Phòng (Room Guest Registry)

**Mức độ:** CRITICAL

**Phát biểu:**
Mỗi phòng vật lý được check-in **phải có ít nhất một người đại diện chính (Primary Contact)** được khai báo đầy đủ trong bảng `Room_Guests`. Mỗi `RoomBookingDetail` phải có đúng một bản ghi với `is_primary_contact = true`.

**Chi tiết:**

- Khách có thể là `Customer` (đã có tài khoản) hoặc `Dependent` (người đi cùng).
- `guest_type` phân loại: `ADULT`, `CHILD`, `INFANT`.
- Thông tin này phục vụ báo cáo khai báo tạm trú theo Luật Cư trú 2020.

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

---

## 12. BR-WF — Workflow Engine Động

### BR-WF-01 — **Tour Management**	**Tour Management****Tour Management****Tour Management****Tour Management**e

**Mức độ:** HIGH

**Phát biểu:**
Hệ thống Workflow Engine động (`WorkflowEngineService`) xử lý các luồng nghiệp vụ cấu hình được (`Workflow` entity) bao gồm điều kiện (`conditions_json`) và hành động (`actions_json`). Engine **phải đảm bảo idempotency** — cùng một sự kiện kích hoạt không được thực thi workflow hai lần.

**Chi tiết:**

- `trigger_event`: định nghĩa sự kiện kích hoạt (VD: `BOOKING_CONFIRMED`, `PROMOTION_EXCEEDED`).
- `conditions_json`: điều kiện dạng JSON kiểm tra trước khi thực thi.
- `actions_json`: danh sách hành động dạng JSON (gửi email, thay đổi trạng thái, cảnh báo).
- Chỉ Admin mới được phép tạo/sửa/xóa Workflow.
- Workflow bị vô hiệu hóa (`is_active = false`) không được kích hoạt.

---

### BR-WF-02 — Ủy quyền Phê duyệt Đặt phòng (Pending Approval)

**Mức độ:** HIGH

**Phát biểu:**
Khi một booking vi phạm ngưỡng giảm giá do Staff áp dụng (`PROMOTION_EXCEEDED`), Workflow Engine tự động chuyển booking sang trạng thái `Pending_Approval`. Booking này **phải được Manager phê duyệt hoặc từ chối** trước khi tiến hành check-in.

**Chi tiết:**

- Workflow `PROMOTION_EXCEEDED` được cấu hình trong bảng `Workflows`.
- Manager nhận thông báo và có thể phê duyệt (→ `Confirmed`) hoặc từ chối (→ `Cancelled`).
- Ghi Audit Log khi có quyết định phê duyệt.

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

## 14. CÁC QUY TẮC BỔ SUNG KHÁC (Từ File Mod 5)

### Nhóm BR-RPT

### BR-RPT-01 — Phân loại Doanh thu chuẩn USALI

**Mức độ:** HIGH
**Phát biểu:** Doanh thu hệ thống bắt buộc phải được bóc tách làm 3 luồng riêng biệt: Doanh thu Phòng (Room), Ẩm thực (F&B) và Lữ hành (Tour)

### BR-RPT-02 — Thống kê Dashboard Thời Gian Thực

**Mức độ:** HIGH
**Phát biểu:** Manager Dashboard tính toán và hiển thị các chỉ số cốt lõi: Tỷ lệ lấp đầy (Occupancy Rate), tỷ lệ bán món ăn, tỷ lệ bán tour. Hệ thống phải vẽ đồ thị doanh thu lũy kế dựa theo các bộ lọc thời gian.

### BR-RPT-03 — Báo cáo Lợi Nhuận Gộp (GOP)

**Mức độ:** HIGH
**Phát biểu:** Hệ thống cung cấp báo cáo tính toán Lợi nhuận Hoạt động Gộp cho từng bộ phận để Manager theo dõi hiệu suất tài chính tổng thể.

### Nhóm BR-CO

### BR-CO-01 — Điều kiện Tiên quyết (Zero Balance)

**Mức độ:** HIGH
**Phát biểu:** Thủ tục Check-out bị khóa hoàn toàn nếu tổng hóa đơn (Consolidated_Invoice) chưa được thanh toán sạch. Hệ thống tự động chặn và trả về mã lỗi FOLIO-001 nếu dư nợ > 0.

### BR-CO-02 — Phát hành Hóa đơn Điện tử (e-Invoice)

**Mức độ:** HIGH
**Phát biểu:** Ngay sau khi Lễ tân xác nhận Check-out thành công và số dư Folio = SETTLED, hệ thống tự động sinh hóa đơn điện tử định dạng PDF và kích hoạt luồng gửi Email (qua SendGrid) tới khách hàng.

### BR-CO-03 — Đóng vòng đời Lưu trú & Kích hoạt Dọn dẹp

**Mức độ:** HIGH
**Phát biểu:** Check-out thành công sẽ tự động cập nhật trạng thái phòng thành Vacant_Dirty và sinh task dọn phòng cho Housekeeping (đã đề cập ở BR-HK-01).

### BR-CO-04 — Quyền lợi Đánh giá (Review Deadline)

**Mức độ:** HIGH
**Phát biểu:** Chỉ những khách hàng đã thực hiện Check-out thành công mới được quyền viết đánh giá (Review). Thời hạn cho phép gửi đánh giá là 7 ngày kể từ ngày Check-out (review_deadline = completed_date + 7 days).

### Nhóm BR-MT

### BR-MT-01 — Ràng buộc Trạng thái Phòng (Out of Order)

**Mức độ:** HIGH
**Phát biểu:** Phòng đang ở trạng thái bảo trì Maintenance sẽ bị đóng băng. Hệ thống Booking Engine hoặc Lễ tân không thể nhìn thấy hoặc gán phòng này cho khách Check-in để ngăn chặn Overbooking.

### BR-MT-03 — Khôi phục Trạng thái Sau Bảo trì

**Mức độ:** HIGH
**Phát biểu:** Khi nhân viên báo cáo hoàn thành sửa chữa, trạng thái phòng KHÔNG được tự động chuyển thành Vacant_Clean, mà phải chuyển về Vacant_Dirty để Housekeeping vào dọn dẹp vệ sinh bụi bẩn sau sửa chữa trước khi đón khách

### Nhóm BR-MNG

### BR-MNG-01 — Phân loại Doanh thu USALI

**Mức độ:** HIGH
**Phát biểu:** Mọi khoản thu trong hệ thống bắt buộc phải được bóc tách làm 3 luồng riêng biệt: Doanh thu Phòng (Room), Doanh thu Ẩm thực (F&B) và Doanh thu Lữ hành (Tour) dựa trên trường source_department để xuất báo cáo lợi nhuận gộp chuẩn USALI.

### BR-MNG-02 — Thẩm quyền Phê duyệt (Manager Approval)

**Mức độ:** HIGH
**Phát biểu:** Các tác vụ rủi ro cao liên quan đến tài chính và vận hành bắt buộc phải có sự phê duyệt của Manager mới được thực thi

### BR-MNG-03 — Kiểm toán Đêm (Night Audit) - Định kỳ

**Mức độ:** HIGH
**Phát biểu:** Quy trình Night Audit phải chạy ngầm tự động bằng Cronjob vào lúc 02:00 AM mỗi ngày. Hệ thống tính toán tiền phòng của ngày hôm đó cộng vào Folio của các phòng đang Checked_In và đóng sổ chuyển sang ngày mới.

### BR-MNG-04 — Chốt chặn Đóng ca trước Night Audit

**Mức độ:** HIGH
**Phát biểu:** Tiến trình Night Audit sẽ báo lỗi hoặc tạm dừng nếu phát hiện nhân viên F&B/POS chưa chốt sổ bán hàng trong ngày (End of Day). Manager có quyền thực thi "Cưỡng chế đóng ca" (Force Close) để Night Audit tiếp tục chạy.

### BR-MNG-05 — Tính toàn vẹn Dữ liệu Hủy (Cancellation Consistency)

**Mức độ:** HIGH
**Phát biểu:** Khi một Yêu cầu Hoàn tiền (Refund Request) được khởi tạo và phê duyệt, bản ghi dịch vụ gốc (Booking, FoodOrder, TourBooking) bắt buộc phải chuyển trạng thái sang Cancelled để đảm bảo báo cáo doanh thu cuối tháng không bị ảo.

### Nhóm BR-REC

### BR-REC-02 — Ràng buộc Hạn mức Tín dụng

**Mức độ:** HIGH
**Phát biểu:** Tổng dư nợ hiện tại cộng với giao dịch Ký nợ mới không được phép vượt quá hạn mức nợ (Credit Limit) của phòng. Nếu vượt, giao dịch bị từ chối trừ khi được Lễ tân hoặc Manager can thiệp nâng hạn mức.

### BR-REC-05 — Bắt buộc Kiểm phòng (Room Check)

**Mức độ:** HIGH
**Phát biểu:** Không thể xuất hóa đơn cuối cùng nếu Housekeeping chưa hoàn thành việc kiểm tra phòng (Minibar/Hỏng hóc). Lệnh ROOM_CHECK phải chuyển sang trạng thái Completed.

### Nhóm BR-FO

### BR-FO-01 — Điều kiện Ký nợ (Post-to-Room)

**Mức độ:** HIGH
**Phát biểu:** Khách hàng chỉ được phép ký nợ hóa đơn dịch vụ (F&B, Tour) vào ví phòng (Folio) khi trạng thái lưu trú đang là Checked_In.

### BR-FO-03 — Xác thực Giao dịch

**Mức độ:** HIGH
**Phát biểu:** Khách hàng bắt buộc phải nhập mã PIN (so khớp mã băm) hoặc ký tên xác nhận (lưu vào signature_img_url) khi thực hiện Post-to-Room từ các điểm dịch vụ.

### BR-FO-04 — Gom Hóa Đơn Tự Động (Checkout)

**Mức độ:** HIGH
**Phát biểu:** Khi khách trả phòng, hệ thống tự động quét và cộng dồn toàn bộ Folio_Items chưa được thanh toán riêng. Sau khi trừ đi khoản tiền cọc, hệ thống tính ra tổng tiền dư nợ thực tế cần thanh toán.
