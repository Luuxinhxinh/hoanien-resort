# ĐẶC TẢ CHI TIẾT USE CASE (USE CASE DETAIL SPECIFICATIONS)

## HỆ THỐNG QUẢN LÝ NGHỈ DƯỠNG KAWAI RETREAT RESORT & HUB

| Field                 | Value                                                                |
| --------------------- | -------------------------------------------------------------------- |
| **Document ID** | `KAWAI-SRS-UC-DET-005`                                             |
| **Version**     | 5.0 (Đồng bộ`UC_MASTER_TABLE` v5.0 + `danhmay.md` + codebase) |
| **Date**        | 2026-07-16                                                           |
| **Status**      | Approved — Reflects implementation audit                            |
| **Author**      | Nhóm Phát Triển SWP391 - G2                                       |
| **Codebase**    | `05-Development/kawai-backend`                                     |

---

## CHANGELOG

| Ngày      | Người thực hiện | Nội dung thay đổi                                                                                                        |
| ---------- | ------------------- | --------------------------------------------------------------------------------------------------------------------------- |
| 2026-07-16 | Antigravity         | Đồng bộ hóa UC ID (UC01-UC52) theo`danhmay.md`; phân module lại (MOD1-MOD5+MOD_SYS); cập nhật tên & description. |
| 2026-06-28 | Nhóm G2            | Rà soát toàn bộ controller/service/template; đồng bộ số UC với Master v4.0; bổ sung UC29-UC31.                    |
| 2026-06-16 | Antigravity         | Cập nhật UC11 auth modal order-food; đồng bộ 25 UC                                                                     |
| 2026-06-13 | Nhóm G2            | Khởi tạo tài liệu UC Detail                                                                                             |

---

## KÝ HIỆU & QUY ƯỚC

| Ký hiệu | Ý nghĩa                                       |
| --------- | ----------------------------------------------- |
| ✅        | Triển khai hoàn chỉnh E2E                    |
| ⚠️      | Triển khai một phần / mock / bug / thiếu UI |
| ❌        | Chưa triển khai                               |

**Số UC trong tài liệu này khớp 1:1 với `UC_MASTER_TABLE.md` v5.0 và `danhmay.md`.**

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
2. [MOD2 — Room Booking &amp; Lễ tân](#mod2)
3. [MOD3 — F&amp;B, POS, KDS](#mod3)
4. [MOD4 — Tour Management](#mod4)
5. [MOD5 — Folio, Vận hành &amp; Báo cáo](#mod5)
6. [MOD_SYS — Hệ thống &amp; Tích hợp](#mod-sys)
7. [Phụ lục — Endpoint tổng hợp](#phu-luc)

---

<a id=mod1></a>

## 🔴 MOD1: HỆ THỐNG CỐT LÕI, XÁC THỰC & ADMIN CONFIG (UC01 – UC09)

### **UC01 — Manage Identity and Access** ✅

**Description:** Manages account registration, staff provisioning, secure multi-platform login, and automated security policies like account lockout.

#### UC01.1 — Đăng ký khách hàng + OTP email ✅

* **Actor:** Customer, Guest
* **Endpoint:** `POST /api/v1/auth/register`, `POST /api/v1/auth/verify-otp`
* **Service:** `AuthServiceImpl`
* **Template:** `guest/fragments/auth.html`, `email/registration-otp.html`
* **DB:** `Accounts` (INSERT), `Customers` (INSERT), role CUSTOMER

#### UC01.2 — Admin tạo tài khoản nhân viên / khách CRM ✅

* **Actor:** Admin
* **Endpoint:** `POST /admin/api/v1/employees`, `/customers`, `/accounts`
* **Controller:** `AdminAccountRestController`
* **Template:** `admin/fragments/md-account-management.html`

#### UC01.3 — Đăng nhập Guest + Google OAuth ✅

* **Guest login:** Modal → `POST /auth/login` (Spring Security form)
* **Google OAuth:** `GET /auth/google-login` → OAuth2 flow tại `/booking`
* **Service:** `CustomOAuth2UserService`, `OAuth2SuccessHandler`

#### UC01.4 — Đăng nhập nhân viên Ops ✅

* **Endpoint:** `GET /ops-login`, `POST /auth/login`
* **Redirect:** `SecurityConfig.roleBasedSuccessHandler()` → dashboard theo role

#### UC01.5 — Khóa tài khoản tự động ✅

* **Service:** `AuthServiceImpl`, `AuthenticationEvents`
* **Model:** `Account.lockoutTime`, failed attempt counter

---

### **UC02 — Reset Password** ✅

**Description:** Allows users to securely recover their accounts by requesting a time-limited reset token via email to establish a new password.

* **Flow:**
  1. `POST /api/v1/auth/forgot-password` — sinh token, gửi email
  2. `GET /auth/reset-password?token=...` — form (`guest/reset-password-form.html`)
  3. `POST /api/v1/auth/reset-password` — BCrypt hash mật khẩu mới
* **Exceptions:** Token hết hạn → `guest/reset-password-error.html`

---

### **UC03 — Manage Personal Profile** ✅

**Description:** Allows customers to manage personal information, update passwords, upload identity documents/avatars, and manage accompanying dependents for reservations.

#### UC03.1 — Cập nhật profile & đổi mật khẩu ✅

* **Controller:** `ProfileController`
* **Template:** `guest/profile.html`

#### UC03.2 — Upload avatar ✅

* **Endpoint:** `POST /api/v1/upload`
* **Controller:** `FileUploadController`

#### UC03.3 — Quản lý Dependents ✅

* **Web:** `ProfileController` `/dependents/add`, `/dependents/delete`
* **API:** `GET/POST /api/bookings/{bookingId}/guests`
* **Service:** `DependentServiceImpl`

---

### **UC04 — Scan FaceID** ⚠️

**Description:** Enables customers to register their facial data and allows Tour Guides to perform AI-based FaceID scans at checkpoints for quick and secure attendance verification.

#### UC04.1 — Upload ảnh chân dung / vector ⚠️

* **Gap:** Không có endpoint profile upload riêng; vector lưu qua luồng tour attendance.

#### UC04.2 — Quét FaceID checkpoint tour ⚠️

* **API:** `FaceIdApiController` — `/api/faceid/scan`, `/verify`, `/references`
* **AI:** Spawn `python main.py` từ `kawai-ai-service`; fallback `face-api.js`
* **Template:** `tour/FaceID.html`
* **Gap:** Reference hardcode demo; ngưỡng confidence 85% (BR-TR-02).

---

### **UC05 — Manage Access Control and Security**

**Description:** Provides comprehensive security controls including RBAC, activity audit logging, data history tracking with rollback, and authorized device management.

#### UC05.1 — RBAC ✅

* **Constants:** `RolePermissionConstants`; **Seeder:** `RolePermissionSeeder`
* **Admin UI:** `admin/fragments/md-role-management.html`

#### UC05.2 — Activity Audit Log ✅

* **Aspect:** `@LogActivity` trên master data mutations
* **Template:** `admin/audit-log.html`, `static/admin/js/audit-log.js`

#### UC05.3 — Envers history & rollback ✅

* **API:** `AuditApiController` — `GET/POST /admin/api/v1/audit/{entityType}/{id}/history|rollback`

#### UC05.4 — Thiết bị ủy quyền Ops ⚠️

* **Model:** `AuthorizedDevice`; **UI:** `admin/devices.html`
* **Gap:** Thiết bị mới auto-approve trong môi trường dev.

---

### **UC06 — Manage Room Categories and Rooms** ✅

**Description:** Administrators perform CRUD operations on room categories and physical rooms, including strict business validations to prevent deletion of rooms with active bookings.

#### UC06.1 — CRUD Room Categories ✅

* **API:** `POST/PUT/DELETE /admin/api/v1/room-categories[/{id}]`
* **UI:** `md-room-categories.html`

#### UC06.2 — CRUD Rooms ✅

* **API:** entity `rooms`; **UI:** `md-rooms.html`
* **Ràng buộc delete:** Kiểm tra booking active trước khi xóa.

---

### **UC07 — Manage Dining Tables** ✅ (Master Data)

**Description:** Administrators and managers perform CRUD operations on restaurant tables and dynamically update their real-time availability statuses to support F&B and POS operations.

#### UC07.1 — CRUD Restaurant Tables ✅

* **POS UI:** `f&bStaff/table-management.html`

#### UC07.2 — Trạng thái bàn ✅

* **API:** `PUT /api/v1/tables/{id}/status`; **Controller:** `TableApiController`

---

### **UC08 — Manage Tours** ⚠️ (Master Data)

**Description:** Administrators and managers perform CRUD operations to manage the core tour catalog, including tour details, operational schedules, and specific routing itineraries.

#### UC08.1 — CRUD Tours ✅

* **API:** entity `tours`, toggle `tour-categories`
* **UI:** `md-tours.html`, `md-tour-categories.html`

#### UC08.2 — Tour Schedules ⚠️

* **Gap:** Tab master-data hiển thị read-only; chưa có CRUD API đầy đủ.

#### UC08.3 — Tour Itineraries ⚠️

* **Model:** `TourItinerary`, `TourItineraryDetail`, `TourLocation`
* **Gap:** UI admin hạn chế; quản lý chủ yếu qua seed `data.sql`.

---

### **UC09 — Manage Pricing, Marketing, and Operations**

**Description:** Provides comprehensive tools to manage dynamic pricing, daily rates, and marketing promotions. Includes F&B menu config, CSV import/export, automated workflows, cronjob scheduling, and KPI dashboard.

#### UC09.1 — Dynamic Pricing ⚠️

* **Gap:** Chỉ có unit test; không có API/UI runtime. Booking thực tế dùng `DailyRate`.

#### UC09.2 — Daily Rates ✅

* **API:** entity `pricing`; **UI:** `md-pricing-management.html`

#### UC09.3 — Phụ thu trẻ em ❌

* **Gap:** `RoomSurcharge` tồn tại trong schema nhưng chưa có service/controller expose.

#### UC09.4 — Promotions ✅

* **API:** entity `promotions`; **UI:** `md-promotions.html`
* **Áp dụng:** `BookingApiController /{id}/apply-coupon`, `FolioRestController /promo/validate`

#### UC09.5 — Menu Items ✅

* **API:** entity `menu-items`, toggle `menu-categories`
* **Kitchen toggle:** `POST /api/menu-items/{id}/toggle`

#### UC09.6 — Export CSV ✅

* **API:** `GET /admin/api/v1/export/csv?entityType=...`

#### UC09.7 — Import CSV ⚠️

* **API:** `POST /admin/api/v1/import` (multipart)
* **Hỗ trợ:** Promotions, Restaurant Menu only

#### UC09.8 — Workflow Engine ⚠️

* **API:** `WorkflowApiController`; **Service:** `WorkflowEngineServiceImpl`
* **UI:** `admin/workflows.html`

#### UC09.9 — Cronjob Management ✅

* **UI:** `/admin/cronjobs`; **API:** `CronjobApiController`; **Manager:** `DynamicJobManager`

#### UC09.10 — Admin Dashboard ⚠️

* **Route:** `/admin/dashboard`; **Service:** `AdminViewServiceImpl`
* **Gap:** Chart doanh thu 7 ngày dùng dữ liệu mock trong một số build.

---

<a id=mod2></a>

## 🔵 MOD2: QUẢN LÝ PHÒNG & LỄ TÂN VẬN HÀNH (UC10 – UC21)

### **UC10 — Search Available Rooms** ✅

**Description:** Customers filter and search for vacant rooms based on selected date range, guest count, and room type.

* **Actor:** Customer
* **Endpoint:** `GET /api/rooms/search`
* **Controller:** `RoomApiController`
* **Template:** `guest/booking.html`

---

### **UC11 — Book Room & Pay Online Deposit** ✅

**Description:** Customers book multiple rooms in one order and securely process a deposit payment via VNPay Sandbox.

* **Actor:** Customer
* **Endpoint:** `POST /api/bookings`, `POST /api/v1/payments/vnpay-return`
* **Controller:** `BookingApiController`, `PaymentApiController`
* **Service:** `BookingServiceImpl`, `VNPayService`

---

### **UC12 — Cancel Booking** ✅

**Description:** Allows customers to cancel a confirmed room booking before the check-in date. The system automatically calculates refund eligibility based on the 48-hour policy and initiates a refund transaction if applicable.

* **Actor:** Customer
* **Endpoint:** `POST /api/bookings/{id}/cancel`
* **Business Rule:** Hoàn tiền 100% nếu hủy trước 48 giờ; không hoàn nếu hủy muộn hơn.

---

### **UC13 — Manage Accompanying/Dependent Guests** ✅

**Description:** Allows the customer to manage accompanying/dependent guests under an existing reservation.

* **Actor:** Customer
* **Endpoint:** `GET/POST /api/bookings/{bookingId}/guests`
* **Service:** `DependentServiceImpl`
* **DB:** `Dependents`, `RoomGuest`

---

### **UC14 — View Profile & Booking History** ✅

**Description:** Allows customers to view their profile information, review booking history (Confirmed, Checked-in, Checked-out, Cancelled), track payment status, and check their remaining available credit limit.

* **Actor:** Customer
* **Route:** `ProfileController` → `/bookings`
* **Template:** `guest/booking-history.html`, `guest/profile.html`
* **API hủy:** `POST /api/bookings/{id}/cancel`

---

### **UC15 — Check-In & Allocate Physical Rooms**

**Description:** Includes handling Check-in/Late Check-out surcharges based on hotel policies, processing room change requests during the stay, and automatically updating the Room Matrix state to Occupied/Clean.

#### UC15.1 — Check-In thường & gán phòng vật lý

* **Actor:** Receptionist
* **Controller:** `ReceptionistCheckinWebController`, `ReceptionistController`

#### UC15.2 — Phụ thu Early Check-in / Late Check-out ✅

* **Endpoint:** `POST /api/walk-in/calculate-surcharge`
* **Service:** `SurchargeService`

### **UC16 — Walk-in Guest Check-in** ✅

**Description:** Handles the process of accommodating a guest who arrives at the hotel without a prior reservation. Supports real-time room availability checking, creation of a new reservation, assignment of a physical room, and immediate check-in.

* **Actor:** Receptionist
* **Controller:** `WalkInCheckInApiController`
* **Endpoint:** `POST /api/walk-in/checkin`
* **Template:** `receptionist/walk-in-checkin.html`

---

### **UC17 — Register Accompanying Guests** ✅

**Description:** Receptionists register accompanying/dependent guests under an existing reservation for temporary residence compliance and guest management purposes.

* **Actor:** Receptionist
* **Controller:** `ReceptionistController`
* **API:** `POST /api/bookings/{bookingId}/guests`

---

### **UC18 — **Upgrade Dependent to Customer**** ✅

**Description:** To manage payments and expenses independently, an accompanying guest must be registered as a Customer, assigned as the primary occupant of a room, and have a valid phone number or email address for OTP verification. They are then responsible for their own Food & Beverage and Tour charges.

* **Actor:** Receptionist
* **Service:** `DependentServiceImpl`
* **Template:** `receptionist/in-house.html`

---

### **UC19 — Change Room Category/Room Type** ⚠️

**Description:** Allows a guest to request an upgrade or change to a different room category during an active stay.

* **Actor:** Receptionist
* **Controller:** `ChangeRoomController`
* **Template:** `receptionist/change-room.html`
* **Gap:** Luồng chưa hoàn chỉnh; cần expose `transferRoom` API.

---

### **UC20 — Room Matrix / Dashboard Monitoring** ✅

**Description:** Allows receptionists to monitor the hotel's room status in real time through the Room Matrix dashboard. Displays Vacant Clean, Occupied, Maintenance status.

* **Actor:** Receptionist
* **Route:** `/receptionist/room-matrix`
* **Controller:** `ReceptionistController`
* **Template:** `receptionist/room-matrix.html`

---

### **UC21 — Request Emergency Cleaning (Rush Room Preparation)**

**Description:** Sends emergency cleaning requests to Housekeeping for early arrivals, providing real-time alerts to the receptionist when the room is ready.

* **Actor:** Receptionist
* **Controller:** `HousekeepingApiController` — Rush Room endpoint
* **Gap:** Real-time notification (toast/bell) chưa hoàn chỉnh; đang dùng polling.

---

<a id=mod3></a>

## 🟡 MOD3: F&B, POS & KDS (UC22 – UC32)

### **UC22 — Manage F&B Orders** ⚠️

**Description:** Manages F&B orders, including viewing details, adding dine-in items, updating service statuses, and printing receipts.

#### UC22.1 — View order details ✅

* **Actor:** F&B Staff
* **Controller:** `PosController`, `PosApiController`
* **Template:** `f&bStaff/pos-orders.html`

#### UC22.2 — Add items to order (Dine-in) ⚠️

* **Endpoint:** `POST /api/pos/orders/{id}/add-items`

#### UC22.3 — Update service status ✅

* **Endpoint:** `PUT /api/pos/orders/{id}/status`; `POST /api/pos/batch-update-status`

#### UC22.4 — Print receipt / bill ⚠️

* **Gap:** Tính năng in bill qua browser print dialog; chưa tích hợp máy in vật lý.

---

### **UC23 — Confirm Payment** ✅

**Description:** Confirms payments for F&B orders and automatically updates the order statuses to paid.

* **Actor:** F&B Staff
* **Endpoint:** `POST /api/pos/orders/{id}/pay`
* **Controller:** `PosApiController`

---

### **UC24 — Manage Dining Tables** ✅ (F&B POS)

**Description:** Manages restaurant tables, including holding time, pre-booking for in-house guests, and assigning tables to walk-ins.

#### UC24.1 — Hold table (Time extension) ✅

* **Controller:** `TableApiController`; **Template:** `f&bStaff/table-management.html`

#### UC24.2 — Pre-book table for in-house guests ✅

* **Endpoint:** `POST /api/v1/tables/reservations`

#### UC24.3 — Create Dine-in order for walk-in guests ✅

* **Endpoint:** `POST /api/pos/orders` (dine-in type)

#### UC24.4 — Table check-in for pre-booked guests ✅

* **Endpoint:** `PUT /api/v1/tables/{id}/status`

---

### **UC25 — Manage Room Service Orders** ✅

**Description:** Allows F&B staff to create and manage room service orders for in-house guests.

#### UC25.1 — Create Room Service order ✅

* **Actor:** F&B Staff
* **Controller:** `OrderFoodController`
* **Template:** `f&bStaff/room-service.html`

---

### **UC26 — Generate Shift Report** ⚠️

**Description:** Generates end-of-shift reports summarizing POS sales revenue and staff activities.

* **Actor:** F&B Staff
* **Controller:** `FnBDailyReportController`
* **Gap:** Một số số liệu chưa chính xác 100%.

---

### **UC27 — Update Dish Status (KOT)** ✅

**Description:** Updates the real-time preparation status of individual dishes (Kitchen Order Tickets) for kitchen tracking.

* **Actor:** Kitchen Staff
* **Controller:** `KitchenController`, `PosApiController`
* **Template:** `kitchenStaff/kitchen-display.html`

---

### **UC28 — Manage Dish Availability** ✅

**Description:** Allows kitchen staff to mark dishes as out-of-stock, automatically syncing availability with the POS and customer menus.

* **Actor:** Kitchen Staff
* **Endpoint:** `POST /api/menu-items/{id}/toggle`
* **Controller:** `MenuItemApiController`

---

### **UC29 — Reserve Table Online** ✅

**Description:** Enables customers to conveniently reserve restaurant tables online via the website.

* **Actor:** Customer
* **Endpoint:** `POST /api/v1/tables/reservations`
* **Template:** `guest/dining.html`

---

### **UC30 — Place Order Online** ✅

**Description:** Allows customers to place F&B or room service orders online without contacting the front desk.

* **Actor:** Customer
* **Controller:** `OrderFoodController`
* **Template:** `guest/order-food.html`

---

### **UC31 — Cancel Order** ✅

**Description:** Allows customers or F&B staff to safely cancel an order as long as it remains in the pending state.

* **Actor:** F&B Staff, Customer
* **Endpoint:** `PUT /api/pos/orders/{id}/status` (CANCELLED)
* **Business Rule:** Chỉ hủy được khi order còn ở trạng thái Pending.

---

### **UC32 — Cancel Table Reservation** ✅

**Description:** Allows customers or F&B staff to cancel an existing table reservation, automatically releasing the table and updating its real-time availability status.

* **Actor:** Customer, F&B Staff
* **Endpoint:** `DELETE /api/v1/tables/reservations/{id}`
* **Controller:** `TableApiController`

---

<a id=mod4></a>

## 🟢 MOD4: TOUR MANAGEMENT & ATTENDANCE (UC33 – UC42)

### **UC33 — Manage Tour Core Data** ⚠️

**Description:** Allows administrators to create new tours, update base prices, modify itinerary activities, and perform soft delete on tours that have no active schedules.

* **Actor:** Admin
* **API:** entity `tours`, toggle `tour-categories`
* **UI:** `md-tours.html`, `md-tour-categories.html`
* **Gap:** Quản lý itinerary và schedule CRUD còn hạn chế.

---

### **UC34 — Search Available Tours** ✅

**Description:** Customers filter and search for active tour schedules based on selected date range, integrating third-party OpenWeather API forecasts for the departure date with graceful degradation.

* **Actor:** Customer
* **Endpoint:** `GET /api/weather`, `GET /api/tour-schedules/search`
* **Controller:** `TourController`
* **Template:** `guest/experiences.html`

---

### **UC35 — Book Tour and Process Payment** ✅

**Description:** Customers book tour schedules, automatically calculate pricing based on age policies (free for infants under 2, 50% off for children 2-11), apply promo codes, and process payments either via online payment, cash counter, or room billing (Post to Room).

#### UC35.1 — Book Tour & Pay Online ✅

* **Actor:** Customer, Receptionist
* **Endpoint:** `POST /api/tour-bookings`
* **Controller:** `TourBookingApiController`
* **Payment:** VNPay Sandbox

#### UC35.2 — Tính giá theo chính sách tuổi ✅

* **Business Rule:** Infant (<2 tuổi) free; Child (2-11 tuổi) 50% off; Adult full price.
* **Service:** `TourBookingServiceImpl`

#### UC35.3 — Áp mã khuyến mãi ✅

* **Endpoint:** `POST /api/tour-bookings/{id}/apply-coupon`

#### UC35.4 — Thanh toán tiền mặt / Post to Room ✅

* **Endpoint:** `POST /api/tour-bookings/{id}/pay-cash` hoặc `/post-to-room`

---

### **UC36 — Assign Staff and Vehicles to Tour Schedule** ⚠️

**Description:** Admin assigns tour guides, drivers, and vehicles to confirmed tour schedules, independent of the minimum pax capacity checks.

* **Actor:** Admin, Coordinator
* **Controller:** `ManagerScheduleApiController`, `EmployeeScheduleApiController`
* **UI:** `manager/tour-assignment.html`
* **Gap:** Phân công xe chưa kết nối với inventory xe thực tế.

---

### **UC37 — Cancel Tour Booking** ⚠️

**Description:** Allows customers or the resort to cancel a tour booking. The system automatically computes refund eligibility based on the cancellation origin (100% refund for resort cancellations, 50% penalty for guest cancellations within 24 hours of departure) and sends email notifications.

* **Actor:** Customer, Resort
* **Endpoint:** `POST /api/tour-bookings/{id}/cancel`
* **Controller:** `TourBookingApiController`
* **Gap:** Một số trường hợp refund cần manual approval từ Manager.

---

### **UC38 — Post Tour Charges to Room** ✅

**Description:** Allows charging tour booking expenses directly to an active, checked-in room's folio, verifying room status and validating remaining spending credit limits (Credit Limit) to prevent overspending.

* **Actor:** Customer, Receptionist
* **Endpoint:** `POST /api/tour-bookings/{id}/post-to-room`
* **Controller:** `TourBookingApiController`
* **Validation:** Kiểm tra room status CHECKED_IN + Credit Limit còn đủ.

---

### **UC39 — Check In Tour Participants Using Face Recognition** ⚠️

**Description:** Passengers verify their attendance before tour departure by scanning their faces via the browser camera (using face-api.js). The backend updates status to Checked_In if face vector match score meets the minimum 85% threshold.

#### UC39.1 — AI Face Scan Attendance Check-In ⚠️

* **Actor:** Customer, Tour Guide
* **API:** `FaceIdApiController` — `/api/faceid/scan`, `/verify`, `/references`
* **Endpoint:** `POST /api/v1/tour-attendance/{id}/verify`
* **Template:** `tour/FaceID.html`
* **Gap:** Ngưỡng confidence 85%; reference hardcode demo khi DB trống.

---

### **UC40 — Check In Tour Participants Manually** ✅

**Description:** Allows tour guides to manually check-in passengers on the interface in case of AI recognition failure, hardware issues, or quick demo purposes.

* **Actor:** Tour Guide
* **Endpoint:** `POST /api/v1/tour-attendance/{id}/manual`
* **Controller:** `TourAttendanceRestController`
* **Template:** `tour/FaceID.html` (manual tab)

---

### **UC41 — Start and Complete Tour Schedule** ⚠️

**Description:** Allows tour guides to start a tour (requires 100% passenger check-in) and finish a tour, automatically updating booking statuses to Completed and triggering asynchronous departure/feedback emails.

* **Actor:** Tour Guide
* **Controller:** `TourGuideController`
* **Template:** `tour/tour-guide-dashboard.html`
* **Gap:** 100% check-in requirement có thể block nếu FaceID fails; fallback manual cần confirm.

---

### **UC42 — Reset Tour Status** ✅

**Description:** Allows tour guides to roll back a tour status in case of misclicks (e.g., reverting ongoing to Open, or completed to ongoing), restoring related bookings to Confirmed.

* **Actor:** Tour Guide
* **Controller:** `TourGuideController`
* **Template:** `tour/tour-guide-dashboard.html`

---

<a id=mod5></a>

## 🟣 MOD5: BILLING, VẬN HÀNH & BÁO CÁO (UC43 – UC49)

### **UC43 — Manage Guest Folio & Charges** ✅

**Description:** Allows receptionists to monitor real-time room expenses, process charge-to-room requests from other outlets (F&B, Tours), apply surcharges or discounts, and split/merge invoices as needed.

#### UC43.1 — Theo dõi dư nợ Folio (Real-time) ✅

* **Endpoint:** `GET /api/folios/room/{id}`, `/api/folios/active`
* **Controller:** `FolioRestController`

#### UC43.2 — Ghi nhận Charge-to-Room ✅

* **Mô tả:** Tiếp nhận tự động các khoản nợ từ POS/Tour khi khách ký nợ về phòng. Kiểm tra Credit Limit.

#### UC43.3 — Tách / Gộp hóa đơn (Split/Merge Folio) ✅

* **Endpoint:** `PUT /api/folios/items/{id}/split`
* **Controller:** `FolioRestController`

#### UC43.4 — Thêm Phụ thu (Surcharge) & Giảm giá ✅

* **Endpoint:** `POST /api/folios/room/{id}/surcharge`; `POST /api/folios/room/{id}/promo/validate`

---

### **UC44 — Process Check-out & Payments** ✅

**Description:** Handles the complete check-out workflow, including triggering Housekeeping room checks, generating the final consolidated invoice, processing payments, and updating the room status to dirty.

#### UC44.1 — Khởi tạo quy trình Check-out ✅

* **Mô tả:** Bấm nút Check-out → hệ thống bắn tín hiệu ROOM_CHECK sang Housekeeping.
* **Endpoint:** `POST /api/folios/room/{id}/checkout`

#### UC44.2 — Thanh toán Consolidated Invoice ✅

* **Mô tả:** Sau kiểm phòng, chốt tổng hóa đơn, yêu cầu khách thanh toán (Tiền mặt/Thẻ/VNPay).

#### UC44.3 — Hoàn tất Check-out ✅

* **Mô tả:** Ghi nhận thanh toán, chuyển trạng thái phòng thành Vacant_Dirty hoặc Checkout_Clean.

---

### **UC45 — Manage Cleaning Tasks & Status** ⚠️

**Description:** Allows housekeepers to monitor daily cleaning tasks, receive real-time alerts for emergency (rush) rooms, and update room statuses from dirty to clean upon completion.

#### UC45.1 — Xem danh sách Task dọn phòng ⚠️

* **Controller:** `HousekeepingWebController`
* **Template:** `housekeeping/dashboard.html`

#### UC45.2 — Cập nhật tiến độ dọn dẹp ⚠️

* **Endpoint:** `PUT /api/housekeeping/tasks/{id}/status`
* **Controller:** `HousekeepingApiController`

#### UC45.3 — Nhận thông báo dọn khẩn (Rush Room) ⚠️

* **Gap:** Real-time pop-up/toast chưa hoàn chỉnh; đang dùng polling.

---

### **UC46 — Room Inspection & Incident Reporting** ⚠️

**Description:** Equips housekeepers to perform check-out room inspections, record minibar consumption, create maintenance tickets for damages, and log lost-and-found items.

#### UC46.1 — Kiểm phòng Check-out (Room Check) ⚠️

* **Mô tả:** Nhận lệnh kiểm tra phòng tức thời khi khách làm thủ tục. Phải hoàn thành trước khi Lễ tân chốt bill.

#### UC46.2 — Kiểm tra Minibar & Bổ sung tiện ích ❌

* **Gap:** Model `MinibarItem` tồn tại nhưng chưa có UI/API expose.

#### UC46.3 — Báo cáo hỏng hóc (Create Ticket) ⚠️

* **Controller:** `HousekeepingApiController`
* **Gap:** Đính kèm phí đền bù vào Folio chưa hoàn chỉnh.

#### UC46.4 — Ghi nhận đồ thất lạc (Lost & Found) ❌

* **Gap:** Chưa có model/service/controller cho Lost & Found.

---

### **UC47 — Manage Maintenance Workflow** ⚠️

**Description:** Allows maintenance staff to receive incident tickets from housekeeping, track and update repair statuses (Pending, In Progress, Paused, Completed), and finalize repairs to automatically unlock and update room availability.

#### UC47.1 — Tiếp nhận Ticket bảo trì ⚠️

* **Controller:** `MaintenanceWebController`
* **Template:** `maintenance/dashboard.html`

#### UC47.2 — Cập nhật trạng thái sửa chữa ⚠️

* **Flow:** Pending → In Progress → Paused (Chờ vật tư) → Completed

#### UC47.3 — Hoàn tất & Mở khóa phòng ⚠️

* **Mô tả:** Báo cáo sửa chữa thành công. Phòng tự động chuyển sang trạng thái khả dụng.

---

### **UC48 — Manage Hotel Analytics** ⚠️

**Description:** Provides managers with real-time dashboards for occupancy, revenue, and cross-selling performance, along with capabilities to export USALI-standardized financial reports.

#### UC48.1 — Phân tích Doanh thu & Công suất ⚠️

* **Controller:** `ManagerController`, `ManagerReportApiController`
* **Template:** `manager/dashboard.html`
* **Mô tả:** Xem Dashboard biểu đồ Occupancy, RevPAR, ADR và doanh thu lũy kế theo thời gian thực.

#### UC48.2 — Trích xuất Báo cáo Kế toán (USALI) ⚠️

* **Controller:** `ManagerReportApiController`
* **Gap:** Export PDF/Excel chưa chuẩn hóa hoàn toàn theo USALI.

---

### **UC49 — Manage Hotel Operations** ⚠️ (Exception Approvals)

**Description:** Enables managers to receive notifications and approve or reject out-of-policy operational requests such as free cancellations, refunds, complimentary services, and folio discounts over 15%.

#### UC49.1 — Phê duyệt Ngoại lệ ⚠️

* **Controller:** `ManagerApprovalApiController`, `ManagerRefundApiController`, `ManagerRequestApiController`
* **UI:** `manager/approvals.html`
* **Gap:** Một số trigger event từ Workflow Engine chưa đầy đủ.

---

<a id=mod-sys></a>

## 🆕 MOD_SYS: HỆ THỐNG & TÍCH HỢP (UC50 – UC52)

### **UC50 — Send Email Notifications** ✅

**Description:** Manages the generation, preview, and delivery of automated transactional emails across the platform, including user authentication (OTPs, password resets), guest reservations (rooms, tours, dining), billing invoices, and internal workflow SLA alerts.

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

### **UC51 — Manage Scheduled Jobs** ✅

**Description:** Allows system administrators to monitor, schedule, and dynamically manage automated background tasks (cron jobs) through a dedicated admin UI, without requiring application restarts.

| Job ID                  | Mô tả                                    |
| ----------------------- | ------------------------------------------ |
| `booking_cleanup`     | Hủy booking Pending quá`holdExpiresAt` |
| `reservation_cleanup` | Dọn table reservation hết hạn           |
| `table_cleanup`       | Reset trạng thái bàn                    |
| `audit_cleanup`       | Dọn audit log cũ (`AuditCleanupTask`)  |
| `workflow_processor`  | Quét SLA workflow                         |

**Quản lý:** `DynamicJobManager`, UI `/admin/cronjobs`

---

### **UC52 — View Landing and Booking History** ✅

**Description:** Provides public landing pages for discovering hotel services (living, dining, wellbeing), and an authenticated profile dashboard for guests to view their reservation history and cancel active bookings.

#### UC52.1 — Landing pages ✅

* **Routes:** `/`, `/booking`, `/living`, `/wellbeing`, `/dining`, `/experiences`
* **Controller:** `BookingController`

#### UC52.2 — Booking history ✅

* **Route:** `ProfileController` → `/bookings`
* **Template:** `guest/booking-history.html`
* **API hủy:** `POST /api/bookings/{id}/cancel`

---

<a id=phu-luc></a>

## PHỤ LỤC — ENDPOINT TỔNG HỢP THEO MODULE

### Auth

`POST /api/v1/auth/register|verify-otp|forgot-password|reset-password POST /auth/login GET  /auth/google-login|reset-password|check-session|logout GET  /ops-login`

### Booking & Rooms

`GET  /api/rooms/search|/{roomNumber}/info|by-cccd POST /api/bookings|/{id}/cancel|confirm|apply-coupon|guests GET  /api/bookings/{id}|/{id}/folios|/{id}/guests POST /api/walk-in/checkin|calculate-surcharge`

### Folio & Payment

`GET  /api/folios/room/{id}|/active|/promo/validate PUT  /api/folios/items/{id}/split POST /api/folios/room/{id}/checkout GET  /api/v1/payments/vnpay-return POST /api/v1/payments/vnpay-ipn`

### POS & F&B

`POST /api/pos/orders|/{id}/pay|/{id}/add-items PUT  /api/pos/orders/{id}/status POST /api/pos/batch-update-status GET  /api/v1/tables/availability POST /api/v1/tables/reservations`

### Admin

`POST/PUT/DELETE /admin/api/v1/{entityType}[/{id}] GET  /admin/api/v1/export/csv POST /admin/api/v1/import GET  /admin/api/v1/audit/{entity}/{id}/history POST /admin/api/v1/cronjobs/{id}/run`

### Tour & FaceID

`POST /api/tour-bookings POST /api/faceid/scan|verify|checkin-manual POST /api/v1/tour-attendance/{id}/verify|manual GET  /api/weather`

---

## GHI CHÚ TRIỂN KHAI & TECH DEBT (2026-07-16)

| #  | Vấn đề                                                | UC liên quan  | Mức          |
| -- | -------------------------------------------------------- | -------------- | ------------- |
| 1  | Night audit query status OCCUPIED vs Checked_In          | UC44           | 🔴 Bug        |
| 2  | Admin reviews UI không gọi API moderate                | UC48           | 🟠 Gap        |
| 3  | transferRoom chưa expose cho lễ tân                   | UC19           | 🟠 Gap        |
| 4  | Housekeeping/Maintenance không có UI riêng đầy đủ | UC45, UC47     | 🟠 Gap        |
| 5  | Dynamic pricing chưa runtime                            | UC09.1         | 🟠 Gap        |
| 6  | Shift report & một số manager chart mock               | UC26, UC48     | 🟡 Mock       |
| 7  | FaceID demo hardcode references                          | UC39.1, UC04.2 | 🟡 Demo       |
| 8  | OCR CCCD check-in chưa làm                             | UC15.1         | 🟡 Spec drift |
| 9  | WebSocket KDS — dùng polling thay thế                 | UC27           | 🟡 Spec drift |
| 10 | Backdoor /admin-backdoor, Debug controllers              | —             | ⚠️ Dev only |

---

*Tài liệu này phản ánh trạng thái codebase tại ngày 2026-07-16. Khi merge tính năng mới, cập nhật đồng thời `UC_MASTER_TABLE.md` và mục tương ứng trong file này.*
