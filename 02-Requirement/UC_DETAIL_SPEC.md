# ĐẶC TẢ CHI TIẾT USE CASE (USE CASE DETAIL SPECIFICATIONS)

## HỆ THỐNG QUẢN LÝ NGHỈ DƯỠNG KAWAI RETREAT RESORT & HUB

| Field                 | Value                                                         |
| --------------------- | ------------------------------------------------------------- |
| **Document ID** | `KAWAI-SRS-UC-DET-004`                                      |
| **Version**     | 4.0 (Đồng bộ`UC_MASTER_TABLE` v4.0 + codebase thực tế) |
| **Date**        | 2026-06-28                                                    |
| **Status**      | Approved — Reflects implementation audit                     |
| **Author**      | Nhóm Phát Triển SWP391 - G2                                |
| **Codebase**    | `05-Development/kawai-backend`                              |

---

## CHANGELOG

| Ngày      | Người thực hiện | Nội dung thay đổi                                                                                                                                                         |
| ---------- | ------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 2026-06-28 | Nhóm G2            | Rà soát toàn bộ controller/service/template; đồng bộ số UC với Master v4.0; bổ sung UC29–UC31; ghi endpoint thực tế, trạng thái ✅/⚠️/❌, và gap còn lại |
| 2026-06-16 | Antigravity         | Cập nhật UC11 auth modal order-food; đồng bộ 25 UC                                                                                                                      |
| 2026-06-13 | Nhóm G2            | Khởi tạo tài liệu UC Detail                                                                                                                                              |

---

## KÝ HIỆU & QUY ƯỚC

| Ký hiệu | Ý nghĩa                                       |
| --------- | ----------------------------------------------- |
| ✅        | Triển khai hoàn chỉnh E2E                    |
| ⚠️      | Triển khai một phần / mock / bug / thiếu UI |
| ❌        | Chưa triển khai                               |

**Số UC trong tài liệu này khớp 1:1 với `UC_MASTER_TABLE.md` v4.0.**

---

## BẢNG ÁNH XẠ CODE CHÍNH

| Layer       | Package / Path                                                                                  |
| ----------- | ----------------------------------------------------------------------------------------------- |
| Web MVC     | `com.kawai.controllers.web.*`                                                                 |
| REST API    | `com.kawai.controllers.api.*`                                                                 |
| Business    | `com.kawai.services.impl.*`                                                                   |
| Persistence | `com.kawai.models.*`, `com.kawai.repositories.*`                                            |
| Templates   | `src/main/resources/templates/{admin,guest,receptionist,manager,f&bStaff,kitchenStaff,tour}/` |
| AI FaceID   | `05-Development/kawai-ai-service/main.py` (CLI)                                               |

---

## MỤC LỤC

1. [MOD1 — Auth, Admin, Master Data](#mod1)
2. [MOD2 — Booking &amp; Lễ tân](#mod2)
3. [MOD3 — F&amp;B, POS, KDS](#mod3)
4. [MOD4 — Tour &amp; Review](#mod4)
5. [MOD5 — Folio, Tài chính, Báo cáo](#mod5)
6. [MOD6 — Hệ thống &amp; Tích hợp](#mod6)
7. [Phụ lục — Endpoint tổng hợp](#phu-luc)

---

<a id="mod1"></a>

## 🔴 MOD1: HỆ THỐNG CỐT LÕI, XÁC THỰC & ADMIN CONFIG

### **UC01 — Quản lý Tài khoản & Xác thực** ✅

#### UC01.1 — Đăng ký khách hàng + OTP email ✅

* **Actor:** Customer, Guest
* **Mô tả:** Khách đăng ký qua modal trên trang booking; hệ thống gửi OTP qua email trước khi kích hoạt tài khoản.
* **Endpoint:** `POST /api/v1/auth/register`, `POST /api/v1/auth/verify-otp`
* **Service:** `AuthServiceImpl`
* **Template:** `guest/fragments/auth.html`, `email/registration-otp.html`
* **DB:** `Accounts` (INSERT), `Customers` (INSERT), role CUSTOMER
* **Postconditions:** Tài khoản active sau verify OTP; email chào mừng.

#### UC01.2 — Admin tạo tài khoản nhân viên / khách CRM ✅

* **Actor:** Admin
* **Endpoint:** `POST /admin/api/v1/employees`, `/customers`, `/accounts`; `PUT/DELETE /admin/api/v1/accounts/{id}`
* **Controller:** `AdminAccountRestController`
* **Template:** `admin/fragments/md-account-management.html`

#### UC01.3 — Đăng nhập Guest + Google OAuth ✅

* **Actor:** Customer, Guest
* **Guest login:** Modal → `POST /auth/login` (Spring Security form)
* **Google OAuth:** `GET /auth/google-login` → OAuth2 flow tại `/booking`
* **Service:** `CustomOAuth2UserService`, `OAuthAccountService`, `OAuth2SuccessHandler`
* **Session check:** `GET /auth/check-session`

#### UC01.4 — Đăng nhập nhân viên Ops ✅

* **Actor:** Admin, Manager, Receptionist, F&B Staff, Kitchen Staff, Tour Guide
* **Endpoint:** `GET /ops-login`, `POST /auth/login`
* **Redirect:** `SecurityConfig.roleBasedSuccessHandler()` → dashboard theo role
* **Template:** `ops-login.html`

#### UC01.5 — Khóa tài khoản tự động ✅

* **Actor:** System
* **Service:** `AuthServiceImpl`, `AuthenticationEvents`
* **Model:** `Account.lockoutTime`, failed attempt counter
* **Exception:** `LockedException` → redirect `error_type=locked`

---

### **UC02 — Đặt lại mật khẩu** ✅

* **Actor:** Customer, User
* **Flow:**
  1. `POST /api/v1/auth/forgot-password` — sinh token, gửi email
  2. `GET /auth/reset-password?token=...` — form (`guest/reset-password-form.html`)
  3. `POST /api/v1/auth/reset-password` — BCrypt hash mật khẩu mới
* **Service:** `AuthServiceImpl.requestPasswordReset`, `sendPasswordResetEmail`
* **Exceptions:** Token hết hạn → `guest/reset-password-error.html`

---

### **UC03 — Quản lý hồ sơ cá nhân** ✅

#### UC03.1 — Cập nhật profile & đổi mật khẩu ✅

* **Actor:** Customer
* **Controller:** `ProfileController`
* **Template:** `guest/profile.html`
* **Ghi chú:** Mã hóa AES-256 CCCD theo spec gốc — **cần xác minh** field `cccd_passport_encrypted` trong runtime.

#### UC03.2 — Upload avatar ✅

* **Endpoint:** `POST /api/v1/upload`
* **Controller:** `FileUploadController`
* **Lưu trữ:** `uploads/` (static serve `/uploads/**`)

#### UC03.3 — Quản lý Dependents ✅

* **Actor:** Customer
* **Web:** `ProfileController` `/dependents/add`, `/dependents/delete`
* **API booking:** `GET/POST /api/bookings/{bookingId}/guests`
* **Service:** `DependentServiceImpl`
* **DB:** `Dependents`, `RoomGuest`

---

### **UC04 — FaceID** ⚠️

#### UC04.1 — Upload ảnh chân dung / vector ⚠️

* **Spec gốc:** POST `/api/customer/face-upload` → Python FastAPI `/extract-features`
* **Thực tế:** Vector lưu qua luồng tour attendance; **không có endpoint profile upload riêng** như spec cũ.
* **Gap:** Cần bổ sung upload FaceID tại profile nếu yêu cầu nghiệp vụ bắt buộc trước tour.

#### UC04.2 — Quét FaceID checkpoint tour ⚠️

* **Actor:** Tour Guide
* **API chính:**
  * `FaceIdApiController` — `/api/faceid/scan`, `/verify`, `/references`, `/checkin-manual`, `/reset`
  * `TourAttendanceRestController` — `POST /api/v1/tour-attendance/{id}/verify|manual`
* **AI:** Spawn `python main.py` từ `kawai-ai-service`; fallback `face-api.js` trên browser
* **Template:** `tour/FaceID.html`
* **Gap:** Một số ảnh reference hardcode demo khi DB trống; ngưỡng confidence 85% (BR-TR-02).

---

### **UC05 — Phân quyền & An ninh**

#### UC05.1 — RBAC ✅

* **Constants:** `RolePermissionConstants` — map role → permissions (MASTER_DATA, FNB, BOOKING, TOUR…)
* **Seeder:** `RolePermissionSeeder`
* **Admin UI:** `admin/fragments/md-role-management.html`
* **API:** Master data entity `roles` qua `MasterDataApiController`

#### UC05.2 — Activity Audit Log ✅

* **Controller:** `AdminController` `/admin/audit-log`
* **Aspect:** `@LogActivity` trên master data mutations
* **Repository:** `AuditLogRepository`
* **Template:** `admin/audit-log.html`, `static/admin/js/audit-log.js`

#### UC05.3 — Envers history & rollback ✅

* **Entities @Audited:** Account, Room, RoomCategory, MenuItem, Tour, Promotion…
* **API:** `AuditApiController`
  * `GET /admin/api/v1/audit/{entityType}/{id}/history`
  * `POST .../rollback/{revisionId}`
  * `POST /admin/api/v1/audit/backup`

#### UC05.4 — Thiết bị ủy quyền Ops ⚠️

* **Model:** `AuthorizedDevice`
* **API:** `AuthorizedDeviceApiController` — toggle/delete device
* **UI:** `admin/devices.html`
* **Gap:** Thiết bị mới auto-approve trong môi trường dev; kiểm tra `device_id` cookie tại `SecurityConfig`.

---

### **UC06 — Master Data: Hạng phòng & Phòng** ✅

#### UC06.1 — CRUD Room Categories ✅

* **API:** `POST/PUT/DELETE /admin/api/v1/room-categories[/{id}]`
* **Service:** `MasterDataServiceImpl`
* **UI:** `md-room-categories.html`

#### UC06.2 — CRUD Rooms ✅

* **API:** entity `rooms`
* **UI:** `md-rooms.html`
* **Ràng buộc delete:** Kiểm tra booking active trước khi xóa

---

### **UC07 — Master Data: Bàn ăn** ✅

#### UC07.1 — CRUD Restaurant Tables ✅

* **Master data:** entity `restaurant-tables` (read qua admin view)
* **POS UI:** `f&bStaff/table-management.html`

#### UC07.2 — Trạng thái bàn ✅

* **API:** `PUT /api/v1/tables/{id}/status`
* **Controller:** `TableApiController`

---

### **UC08 — Master Data: Tour** ⚠️

#### UC08.1 — CRUD Tours ✅

* **API:** entity `tours`, toggle `tour-categories`
* **UI:** `md-tours.html`, `md-tour-categories.html`

#### UC08.2 — Tour Schedules ⚠️

* **View:** `AdminController` `/admin/tour-schedules`, fragment `md-tour-schedules.html`
* **Gap:** Tab master-data hiển thị read-only; **chưa có CRUD API đầy đủ** trong `MasterDataServiceImpl`

#### UC08.3 — Tour Itineraries ⚠️

* **Model:** `TourItinerary`, `TourItineraryDetail`, `TourLocation`
* **Gap:** Quản lý itinerary chủ yếu qua seed `data.sql`; UI admin hạn chế

---

### **UC09 — Giá, Marketing & Vận hành Admin**

#### UC09.1 — Dynamic Pricing ⚠️

* **Service:** `PricingServiceImpl`, model `DynamicPricing`
* **Gap:** Chỉ có unit test `PricingMarketingServiceUC09Test`; **không có API/UI** runtime
* **Booking thực tế:** Dùng `DailyRate` + base price từ `RoomCategory`

#### UC09.2 — Daily Rates / Pricing tab ✅

* **API:** entity `pricing`
* **UI:** `md-pricing-management.html`

#### UC09.3 — Phụ thu trẻ em ❌

* **Model:** `RoomSurcharge` tồn tại trong schema
* **Gap:** Chưa có service/controller expose

#### UC09.4 — Promotions ✅

* **API:** entity `promotions`
* **UI:** `md-promotions.html`
* **Áp dụng:** `BookingApiController` `/{id}/apply-coupon`, `FolioRestController` `/promo/validate`

#### UC09.5 — Menu Items ✅

* **API:** entity `menu-items`, toggle `menu-categories`
* **UI:** `md-restaurant-menu.html`
* **Kitchen toggle:** `POST /api/menu-items/{id}/toggle`

#### UC09.6 — Export CSV ✅

* **API:** `GET /admin/api/v1/export/csv?entityType=...`
* **Controller:** `ExportApiController`

#### UC09.7 — Import CSV ⚠️

* **API:** `POST /admin/api/v1/import` (multipart)
* **Controller:** `ImportApiController`
* **Hỗ trợ:** Promotions, Restaurant Menu only

#### UC09.8 — Workflow Engine ⚠️

* **API:** `WorkflowApiController` — CRUD workflow, list employees
* **Service:** `WorkflowEngineServiceImpl` — trigger events, SLA scan
* **UI:** `admin/workflows.html`
* **Gap:** Một số trigger chỉ cover promo approval threshold

#### UC09.9 — Cronjob Management ✅

* **UI:** `AdminController` `/admin/cronjobs`
* **API:** `CronjobApiController` — run, toggle, config, logs
* **Manager:** `DynamicJobManager`

#### UC09.10 — Admin Dashboard ⚠️

* **Route:** `/admin/dashboard`
* **Service:** `AdminViewServiceImpl` — KPI từ DB
* **Gap:** Chart doanh thu 7 ngày dùng dữ liệu mock (`Math.random`) trong một số build

---

<a id="mod2"></a>

## 🔵 MOD2: QUẢN LÝ PHÒNG & LỄ TÂN VẬN HÀNH

### **UC10 — Room Booking (Customer)**

#### UC10.1 — Search Available Rooms

* **Actor:** Customer
* **Mô tả:** Customers filter and search for vacant rooms based on selected date range, guest count, and room type.

#### UC10.2 — Book Room & Pay Online Deposit

* **Actor:** Customer
* **Mô tả:** Customers book multiple rooms in one order and securely process a deposit payment via VNPay Sandbox.

#### UC10.3 — Cancel Booking

* **Actor:** Customer
* **Mô tả:** Allows customers to cancel a confirmed room booking before the check-in date. The system automatically calculates refund eligibility based on the 48-hour policy and initiates a refund transaction if applicable.

#### UC10.4 — Manage Accompanying/Dependent Guests

* **Actor:** Customer
* **Mô tả:** Allows the customer to manage accompanying/dependent guests under an existing reservation. The customer can add, update, remove, and assign dependent guests to available rooms within the reservation.

#### UC10.5 — View Profile & Booking History

* **Actor:** Customer
* **Mô tả:** Allows customers to view their profile information, review booking history (Confirmed, Checked-in, Checked-out, Cancelled), track payment status, and check their remaining available credit limit for future reservations and services.

### **UC11 — Front Desk Operations (Receptionist)**

#### UC11.1 — Check-In & Allocate Physical Rooms

* **Actor:** Receptionist
* **Mô tả:** Includes handling Early Check-in/Late Check-out surcharges based on hotel policies, processing room change requests during the stay, and automatically updating the Room Matrix state to 'Occupied/Clean'.

#### UC11.2 — Walk-in Guest Check-in

* **Actor:** Receptionist
* **Mô tả:** Handles the process of accommodating a guest who arrives at the hotel without a prior reservation. The system supports real-time room availability checking, creation of a new reservation, assignment of a physical room, and immediate check-in.

#### UC11.3 — Register Accompanying Guests

* **Actor:** Receptionist
* **Mô tả:** Receptionists register accompanying/dependent guests under an existing reservation for temporary residence compliance and guest management purposes.

#### UC11.4 — Authorize Dependent Service Access

* **Actor:** Receptionist
* **Mô tả:** Allows the Receptionist to grant independent service booking permissions to a registered dependent guest while ensuring all expenses remain linked to the Master Folio.

#### UC11.5 — Change Room Category/Room Type

* **Actor:** Receptionist
* **Mô tả:** Allows a guest to request an upgrade or change to a different room category during an active stay. The receptionist verifies room availability and updates the room assignment accordingly.

#### UC11.6 — Room Matrix / Dashboard Monitoring

* **Actor:** Receptionist
* **Mô tả:** Allows receptionists to monitor the hotel's room status in real time through the Room Matrix dashboard. The system displays the current status of each room, including Vacant Clean, Vacant Dirty, Occupied, and Maintenance, enabling efficient room allocation, guest check-in coordination, and operational oversight.

#### UC11.7 — Request Emergency Cleaning (Rush Room Preparation)

* **Actor:** Receptionist
* **Mô tả:** Allows receptionists to submit an emergency room cleaning request to Housekeeping when a walk-in guest arrives or a guest requests early check-in. The system notifies Housekeeping to prioritize the room and provides real-time alerts (e.g., bell notification or toast message) to the receptionist once the room has been cleaned and is ready for occupancy.

<a id="mod3"></a>

## 🟡 MOD3: F&B, POS & KDS

### **UC1 — F&B Order Management**

* **Actor:** F&B Staff
* **Mô tả:** General management of F&B orders including viewing order details (UC1.1), adding items to Dine-in orders (UC1.2), updating service status (UC1.3), and printing receipts/bills (UC1.4).

### **UC2 — Confirm Order Payment**

* **Actor:** F&B Staff
* **Mô tả:** Confirms payment for an order and updates the order status to paid.

### **UC3 — Table Management**

* **Actor:** F&B Staff
* **Mô tả:** Management of restaurant tables. Includes holding tables/time extension (UC3.1), pre-booking tables for in-house guests (UC3.2), creating Dine-in orders for walk-in guests (UC3.3), and checking in guests who pre-booked (UC3.4).

### **UC4 — Room Service Order Management**

* **Actor:** F&B Staff
* **Mô tả:** Manage room service orders, including creating new room service orders (UC4.1).

### **UC5 — Shift Reporting**

* **Actor:** F&B Staff
* **Mô tả:** Generation of shift reports summarizing sales and activities during a staff's shift.

### **UC6 — Update Individual Dish Status (KOT)**

* **Actor:** Kitchen Staff
* **Mô tả:** Updates the preparation status of individual dishes (Kitchen Order Tickets) for tracking in the kitchen.

### **UC7 — Manage Dish Availability (Available/Out of Stock)**

* **Actor:** Kitchen Staff
* **Mô tả:** Allows kitchen staff to mark dishes as available or out of stock, automatically reflecting on the POS and customer menus.

### **UC8 — Online Table Reservation**

* **Actor:** Customer
* **Mô tả:** Allows customers to book a table at the restaurant online.

### **UC9 — Online Ordering**

* **Actor:** Customer
* **Mô tả:** Allows customers to place an F&B order online.

### **UC10 — Cancel Order**

* **Actor:** F&B Staff + Customer
* **Mô tả:** Allows cancelling an order if it is still in the pending state.

<a id="mod4"></a>

## 🟢 MOD4: TOUR MANAGEMENT & ATTENDANCE

### **UC08 — Manage Tour Core Data**

* **Actor:** Admin
* **Mô tả:** Allows administrators to create new tours, update base prices, modify itinerary activities, and perform soft delete on tours that have no active schedules.

### **UC19 — Search Available Tours**

* **Actor:** Customer
* **Mô tả:** Customers filter and search for active tour schedules based on selected date range, integrating third-party OpenWeather API forecasts for the departure date with graceful degradation.

### **UC20 — Tour Booking & Operations**

#### UC20.1 — Book Tour & Pay Online

* **Actor:** Customer, Receptionist
* **Mô tả:** Customers book tour schedules, automatically calculate pricing based on age policies (free for infants under 2, 50% off for children 2-11), apply promo codes, and process payments either via online payment, cash counter, or room billing (Post to Room).

#### UC20.2 — Assign Staff and Vehicle to Tour Schedule

* **Actor:** Admin, Coordinator
* **Mô tả:** Admin assigns tour guides, drivers, and vehicles to confirmed tour schedules, independent of the minimum pax capacity checks.

#### UC20.3 — Cancel Tour Booking

* **Actor:** Customer, Resort
* **Mô tả:** Allows customers or the resort to cancel a tour booking. The system automatically computes refund eligibility based on the cancellation origin (100% refund for resort cancellations, 50% penalty for guest cancellations within 24 hours of departure) and sends email notifications.

#### UC20.4 — Post Tour Charge to Room

* **Actor:** Customer, Receptionist
* **Mô tả:** Allows charging tour booking expenses directly to an active, checked-in room's folio, verifying room status and validating remaining spending credit limits (Credit Limit) to prevent overspending.

### **UC21 — Tour Attendance & Checking**

#### UC21.1 — AI Face Scan Attendance Check-In

* **Actor:** Customer, Tour Guide
* **Mô tả:** Passengers verify their attendance before tour departure by scanning their faces via the browser camera (using face-api.js). The backend updates status to Checked_In if face vector match score meets the minimum 85% threshold.

#### UC21.2 — Manual Tour Check-In

* **Actor:** Tour Guide
* **Mô tả:** Allows tour guides to manually check-in passengers on the interface in case of AI recognition failure, hardware issues, or quick demo purposes.

#### UC21.3 — Start & Conclude Tour Schedule

* **Actor:** Tour Guide
* **Mô tả:** Allows tour guides to start a tour (requires 100% passenger check-in) and finish a tour, automatically updating booking statuses to Completed and triggering asynchronous departure/feedback emails.

#### UC21.4 — Reset Tour Status

* **Actor:** Tour Guide
* **Mô tả:** Allows tour guides to roll back a tour status in case of misclicks (e.g., reverting ongoing to Open, or completed to ongoing), restoring related bookings to Confirmed.

<a id="mod5"></a>

## 🟣 MOD5: FOLIO, VẬN HÀNH & BÁO CÁO

### **UC_REC — Lễ tân (Folio & Check-out)**

#### UC_REC.1 — Theo dõi dư nợ Folio (Real-time)

* **Actor:** Receptionist
* **Mô tả:** Xem tổng dư nợ hiện tại của phòng, liệt kê chi tiết từng khoản chi tiêu từ tiền phòng, F&B, Tour, Giặt là đến Minibar.

#### UC_REC.2 — Ghi nhận Charge-to-Room

* **Actor:** Receptionist
* **Mô tả:** Tiếp nhận tự động các khoản nợ từ POS Nhà hàng hoặc Quầy Tour khi khách yêu cầu "Ký nợ về phòng". Hệ thống tự động kiểm tra hạn mức tín dụng (Credit Limit).

#### UC_REC.3 — Tách / Gộp hóa đơn (Split/Merge Folio)

* **Actor:** Receptionist
* **Mô tả:** Tách một số dòng chi phí (VD: tiền ăn) ra thành một bill riêng theo phòng, hoặc gộp chi phí của nhiều phòng gia đình vào chung một Folio chính.

#### UC_REC.4 — Thêm Phụ thu (Surcharge) & Giảm giá

* **Actor:** Receptionist
* **Mô tả:** Áp dụng các khoản phụ thu (Check-out muộn, thêm người) hoặc mã giảm giá trực tiếp vào Folio.

#### UC_REC.5 — Khởi tạo quy trình Check-out

* **Actor:** Receptionist
* **Mô tả:** Bấm nút Check-out, hệ thống tự động bắn tín hiệu ROOM_CHECK (Yêu cầu kiểm phòng) sang màn hình của Housekeeping.

#### UC_REC.7 — Thanh toán Consolidated Invoice

* **Actor:** Receptionist
* **Mô tả:** Sau khi có kết quả kiểm phòng (bao gồm phí Minibar/Hỏng hóc), chốt tổng hóa đơn cuối cùng, yêu cầu khách thanh toán (Tiền mặt/Thẻ/VNPay).

#### UC_REC.9 — Hoàn tất Check-out

* **Actor:** Receptionist
* **Mô tả:** Ghi nhận thanh toán thành công, chuyển trạng thái phòng thành Vacant_Dirty hoặc Checkout_Clean để buồng phòng dọn dẹp sâu.

### **UC_HK — Buồng phòng (Housekeeping)**

#### UC_HK.1 — Xem danh sách Task dọn phòng

* **Actor:** Housekeeper
* **Mô tả:** Theo dõi các phòng cần dọn được phân loại: Đang ở (Stay-over), Khách vừa đi (Checkout_Clean), hoặc Khẩn cấp (Rush Clean).

#### UC_HK.2 — Cập nhật tiến độ dọn dẹp

* **Actor:** Housekeeper
* **Mô tả:** Bấm "Bắt đầu dọn" và "Hoàn thành" trên app/dashboard. Trạng thái phòng tự động đổi từ Dirty sang Clean.

#### UC_HK.3 — Nhận thông báo dọn khẩn (Rush Room)

* **Actor:** Housekeeper
* **Mô tả:** Màn hình hiển thị Pop-up/Toast cảnh báo thời gian thực khi Lễ tân đánh dấu một phòng cần dọn gấp cho khách đang muốn checkIn.

#### UC_HK.4 — Kiểm phòng Check-out (Room Check)

* **Actor:** Housekeeper
* **Mô tả:** Nhận lệnh kiểm tra phòng tức thời khi khách làm thủ tục. Phải hoàn thành task này trước khi Lễ tân chốt bill.

#### UC_HK.5 — Kiểm tra Minibar & Bổ sung tiện ích

* **Actor:** Housekeeper
* **Mô tả:** Ghi nhận đồ uống/snack khách đã dùng và số lượng đồ dùng (bàn chải, khăn) cấp mới qua Modal. Phí Minibar tự động cộng vào Folio của khách.

#### UC_HK.6 — Báo cáo hỏng hóc (Create Ticket)

* **Actor:** Housekeeper
* **Mô tả:** Ghi nhận thiết bị hỏng (VD: vỡ ly, cháy bóng đèn). Nếu là do khách, có thể đính kèm phí đền bù (Damage Fee) đẩy vào Folio. Hệ thống tự động đẩy Ticket sang Maintenance.

#### UC_HK.7 — Ghi nhận đồ thất lạc (Lost & Found)

* **Actor:** Housekeeper
* **Mô tả:** Khai báo tài sản khách để quên, đính kèm hình ảnh và bàn giao cho bộ phận liên quan xử lý.

### **UC_MT — Bảo trì (Maintenance)**

#### UC_MT.1 — Tiếp nhận Ticket bảo trì

* **Actor:** Maintenance Staff
* **Mô tả:** Nhận danh sách sự cố được chuyển từ Housekeeping.

#### UC_MT.2 — Cập nhật trạng thái sửa chữa

* **Actor:** Maintenance Staff
* **Mô tả:** Chuyển trạng thái Ticket: Pending -> In Progress -> Paused (Chờ vật tư) -> Completed.

#### UC_MT.5 — Hoàn tất & Mở khóa phòng

* **Actor:** Maintenance Staff
* **Mô tả:** Báo cáo sửa chữa thành công. Phòng tự động chuyển sang trạng thái khả dụng (Vacant_Clean hoặc Vacant_Dirty chờ dọn lại).

### **UC_MNG — Quản lý (Manager)**

#### UC_MNG.1 — Phân tích Doanh thu & Công suất

* **Actor:** Manager
* **Mô tả:** Xem Dashboard biểu đồ Occupancy, RevPAR, ADR và doanh thu lũy kế theo thời gian thực.

#### UC_MNG.2 — Phân tích Chéo (Cross-selling)

* **Actor:** Manager
* **Mô tả:** Đánh giá hiệu suất bán dịch vụ (Khách ở phòng mua thêm bao nhiêu Tour, ăn Nhà hàng bao nhiêu tiền).

#### UC_MNG.3 — Phê duyệt Ngoại lệ (Manager Approval)

* **Actor:** Manager
* **Mô tả:** Nhận thông báo và phê duyệt (Approve/Reject) các yêu cầu vượt thẩm quyền: Miễn phí hủy phòng, Hoàn tiền, Tặng kèm dịch vụ, Giảm giá Folio trên 15%.

#### UC_MNG.6 — Trích xuất Báo cáo Kế toán (USALI)

* **Actor:** Manager
* **Mô tả:** Xuất báo cáo Excel/PDF về lợi nhuận gộp (GOP) chuẩn hóa theo chuẩn quản trị khách sạn quốc tế USALI phục vụ kiểm toán ngoại bộ.

<a id="mod6"></a>

## 🆕 MOD6: HỆ THỐNG & TÍCH HỢP

### **UC29 — Email Notifications** ✅

| Sự kiện                  | Template / Method                          |
| -------------------------- | ------------------------------------------ |
| OTP đăng ký             | `email/registration-otp.html`            |
| Reset password             | `AuthServiceImpl.sendPasswordResetEmail` |
| Booking confirm/cancel     | `EmailServiceImpl` + booking templates   |
| Tour confirm               | tour email templates                       |
| Đặt bàn / gia hạn hold | `booking-table.html`, extend-hold        |
| Room service / invoice     | `invoice.html`                           |
| Workflow SLA               | workflow notification                      |
| Preview (dev)              | `EmailPreviewController`                 |

---

### **UC30 — Scheduled Jobs** ✅

| Job ID                  | Mô tả                                    |
| ----------------------- | ------------------------------------------ |
| `booking_cleanup`     | Hủy booking Pending quá`holdExpiresAt` |
| `reservation_cleanup` | Dọn table reservation hết hạn           |
| `table_cleanup`       | Reset trạng thái bàn                    |
| `audit_cleanup`       | Dọn audit log cũ (`AuditCleanupTask`)  |
| `workflow_processor`  | Quét SLA workflow                         |

**Quản lý:** `DynamicJobManager`, UI `/admin/cronjobs`

---

### **UC31 — Landing & Booking History** ✅

#### UC31.1 — Landing pages ✅

* **Routes:** `/`, `/booking`, `/living`, `/wellbeing`, `/dining`, `/experiences`
* **Controller:** `BookingController`

#### UC31.2 — Booking history ✅

* **Route:** `ProfileController` → `/bookings`
* **Template:** `guest/booking-history.html`
* **API hủy:** `POST /api/bookings/{id}/cancel`

---

<a id="phu-luc"></a>

## PHỤ LỤC — ENDPOINT TỔNG HỢP THEO MODULE

### Auth

```
POST /api/v1/auth/register|verify-otp|forgot-password|reset-password
POST /auth/login
GET  /auth/google-login|reset-password|check-session|logout
GET  /ops-login
```

### Booking & Rooms

```
GET  /api/rooms/search|/{roomNumber}/info|by-cccd
POST /api/bookings|/{id}/cancel|confirm|apply-coupon|guests
GET  /api/bookings/{id}|/{id}/folios|/{id}/guests
POST /api/walk-in/checkin|calculate-surcharge
```

### Folio & Payment

```
GET  /api/folios/room/{id}|/active|/promo/validate
PUT  /api/folios/items/{id}/split
POST /api/folios/room/{id}/checkout
GET  /api/v1/payments/vnpay-return
POST /api/v1/payments/vnpay-ipn
```

### POS & F&B

```
POST /api/pos/orders|/{id}/pay|/{id}/add-items
PUT  /api/pos/orders/{id}/status
POST /api/pos/batch-update-status
GET  /api/v1/tables/availability
POST /api/v1/tables/reservations
```

### Admin

```
POST/PUT/DELETE /admin/api/v1/{entityType}[/{id}]
GET  /admin/api/v1/export/csv
POST /admin/api/v1/import
GET  /admin/api/v1/audit/{entity}/{id}/history
POST /admin/api/v1/cronjobs/{id}/run
```

### Tour & FaceID

```
POST /api/tour-bookings
POST /api/faceid/scan|verify|checkin-manual
POST /api/v1/tour-attendance/{id}/verify|manual
GET  /api/weather
```

---

## GHI CHÚ TRIỂN KHAI & TECH DEBT (2026-06-28)

| #  | Vấn đề                                              | UC liên quan  | Mức          |
| -- | ------------------------------------------------------ | -------------- | ------------- |
| 1  | Night audit query status`OCCUPIED` vs `Checked_In` | UC27.1         | 🔴 Bug        |
| 2  | Admin reviews UI không gọi API moderate              | UC25           | 🟠 Gap        |
| 3  | `transferRoom` chưa expose cho lễ tân             | UC12.5         | 🟠 Gap        |
| 4  | Housekeeping/Maintenance không có UI riêng          | UC13           | 🟠 Gap        |
| 5  | Dynamic pricing & combo marketing chưa runtime        | UC09.1, UC23   | 🟠 Gap        |
| 6  | Shift report & một số manager chart mock             | UC18.3, UC28.3 | 🟡 Mock       |
| 7  | FaceID demo hardcode references                        | UC04.2         | 🟡 Demo       |
| 8  | OCR CCCD check-in chưa làm                           | UC12.3         | 🟡 Spec drift |
| 9  | WebSocket KDS — dùng polling thay thế               | UC19           | 🟡 Spec drift |
| 10 | Backdoor`/admin-backdoor`, Debug controllers         | —             | ⚠️ Dev only |

---

*Tài liệu này phản ánh trạng thái codebase tại ngày 2026-06-28. Khi merge tính năng mới, cập nhật đồng thời `UC_MASTER_TABLE.md` và mục tương ứng trong file này.*
