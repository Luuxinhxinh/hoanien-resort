# ĐẶC TẢ CHI TIẾT USE CASE (USE CASE DETAIL SPECIFICATIONS)

## HỆ THỐNG QUẢN LÝ NGHỈ DƯỠNG KAWAI RETREAT RESORT & HUB

| Field | Value |
|-------|-------|
| **Document ID** | `KAWAI-SRS-UC-DET-004` |
| **Version** | 4.0 (Đồng bộ `UC_MASTER_TABLE` v4.0 + codebase thực tế) |
| **Date** | 2026-06-28 |
| **Status** | Approved — Reflects implementation audit |
| **Author** | Nhóm Phát Triển SWP391 - G2 |
| **Codebase** | `05-Development/kawai-backend` |

---

## CHANGELOG

| Ngày | Người thực hiện | Nội dung thay đổi |
|------|-----------------|-------------------|
| 2026-06-28 | Nhóm G2 | Rà soát toàn bộ controller/service/template; đồng bộ số UC với Master v4.0; bổ sung UC29–UC31; ghi endpoint thực tế, trạng thái ✅/⚠️/❌, và gap còn lại |
| 2026-06-16 | Antigravity | Cập nhật UC11 auth modal order-food; đồng bộ 25 UC |
| 2026-06-13 | Nhóm G2 | Khởi tạo tài liệu UC Detail |

---

## KÝ HIỆU & QUY ƯỚC

| Ký hiệu | Ý nghĩa |
|---------|---------|
| ✅ | Triển khai hoàn chỉnh E2E |
| ⚠️ | Triển khai một phần / mock / bug / thiếu UI |
| ❌ | Chưa triển khai |

**Số UC trong tài liệu này khớp 1:1 với `UC_MASTER_TABLE.md` v4.0.**

---

## BẢNG ÁNH XẠ CODE CHÍNH

| Layer | Package / Path |
|-------|----------------|
| Web MVC | `com.kawai.controllers.web.*` |
| REST API | `com.kawai.controllers.api.*` |
| Business | `com.kawai.services.impl.*` |
| Persistence | `com.kawai.models.*`, `com.kawai.repositories.*` |
| Templates | `src/main/resources/templates/{admin,guest,receptionist,manager,f&bStaff,kitchenStaff,tour}/` |
| AI FaceID | `05-Development/kawai-ai-service/main.py` (CLI) |

---

## MỤC LỤC

1. [MOD1 — Auth, Admin, Master Data](#mod1)
2. [MOD2 — Booking & Lễ tân](#mod2)
3. [MOD3 — F&B, POS, KDS](#mod3)
4. [MOD4 — Tour & Review](#mod4)
5. [MOD5 — Folio, Tài chính, Báo cáo](#mod5)
6. [MOD6 — Hệ thống & Tích hợp](#mod6)
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

### **UC10 — Tìm kiếm phòng trống** ✅

* **Actor:** Customer, Guest
* **API:** `GET /api/rooms/search?checkInDate&checkOutDate&guestsCount`
* **Controller:** `RoomApiController`
* **Service:** `RoomServiceImpl` — loại trừ phòng đã book, cộng `DailyRate`
* **Template:** `guest/booking.html`, `static/guest/js/booking.js`

---

### **UC11 — Cart Lock / Hold phòng** ✅

* **Actor:** Customer, System
* **Flow:**
  1. `POST /api/bookings` — tạo `RoomBooking` status `Pending`/`Pending_Payment`, set `holdExpiresAt`
  2. Job `booking_cleanup` (`DynamicJobManager`) — hủy booking quá hạn
* **Service:** `BookingServiceImpl`
* **Ghi chú:** Soft lock DB-based, không dùng Redis

---

### **UC12 — Nghiệp vụ Sảnh**

#### UC12.1 — Áp dụng voucher ✅
* **API:** `POST /api/bookings/{bookingId}/apply-coupon`
* **Validation:** active, date range, max uses trong `BookingServiceImpl`

#### UC12.2 — Khai báo hành khách ✅
* **API:** `GET/POST /api/bookings/{bookingId}/guests`
* **Model:** `RoomGuest`, Primary contact trên `RoomBookingDetail`

#### UC12.3 — Check-in sảnh ⚠️
* **Actor:** Receptionist
* **Web:** `ReceptionistController` `/receptionist/check-in`
* **Complete:** `ReceptionistCheckinWebController` `POST /receptionist/checkin/complete`
* **Service:** `CheckinServiceImpl.checkIn`
* **Gap:** **Không có OCR CCCD** như spec gốc — form nhập tay

#### UC12.4 — Hạn mức Post-to-Room + PIN ✅
* **API:** `POST /api/bookings/{bookingId}/rooms/{detailId}/credit-limit`
* **Fields:** `isChargeToRoomAllowed`, `subCreditLimit`, `personalPinHash`
* **Verify PIN:** `RoomApiController` `/api/rooms/{roomNumber}/verify-guest`

#### UC12.5 — Đổi phòng ⚠️
* **Service:** `CheckinServiceImpl.transferRoom` (có unit test `CheckinServiceUC12Test`)
* **Gap:** **Không expose** qua controller/template cho lễ tân

#### UC12.6 — Check-out ✅
* **API:** `POST /api/folios/room/{roomBookingDetailId}/checkout`
* **Controller:** `FolioRestController`
* **Output:** `ConsolidatedInvoice`, PDF qua `InvoicePdfService`, email invoice

#### UC12.7 — Walk-in Check-in ✅
* **Actor:** Receptionist
* **Web:** `ReceptionistController` `/receptionist/walk-in`
* **API:** `WalkInCheckInApiController`
  * `POST /api/walk-in/checkin`
  * `POST /calculate-surcharge`
  * `GET /search-customer`
* **Payment:** VNPay prefix `WALKIN_` trong `PaymentApiController`

#### UC12.8 — Dashboard & Room Matrix ✅
* **Route:** `/receptionist/dashboard`
* **Template:** `receptionist/dashboard.html`

#### UC12.9 — In-house ✅
* **Route:** `/receptionist/in-house`
* **Template:** `receptionist/in-house.html`

---

### **UC13 — Buồng phòng & Bảo trì**

#### UC13.1 — Auto housekeeping task ⚠️
* **Service:** `HousekeepingServiceImpl` — tạo `HotelOperation` khi checkout
* **Workflow:** `WorkflowEngineServiceImpl` on checkout event
* **Gap:** Không có DB trigger như spec; logic trong Java service

#### UC13.2 — App buồng phòng ❌
* **Gap:** Không có route `/staff/housekeeping` hay mobile app

#### UC13.3 — Rush Room ⚠️
* **Spec:** Priority `Urgent` trên `Hotel_Operations`
* **Gap:** Service có thể hỗ trợ; UI receptionist chưa có nút Rush rõ ràng

#### UC13.4 / UC13.5 — Bảo trì ⚠️
* **Model:** `HotelOperation` type MAINTENANCE
* **Gap:** Không có controller/API riêng cho Housekeeper/Maintenance staff UI

---

<a id="mod3"></a>
## 🟡 MOD3: F&B, POS & KDS

### **UC14 — Đặt bàn nhà hàng** ✅

* **Actor:** Customer
* **Web:** `BookingController` `/book-table`
* **API:** `TableApiController`
  * `GET /api/v1/tables/availability`
  * `POST /api/v1/tables/reservations`
  * `PUT /api/v1/tables/reservations/{id}/hold` — gia hạn giữ bàn
* **Email:** `email/booking-table.html`, `email/extend-hold.html`

---

### **UC15 — Cấu hình thực đơn** ✅

* Xem UC09.5 — Master data menu items & categories

---

### **UC16 — Room Service / E-Menu** ✅

#### UC16.1 — Auth modal & giữ giỏ ✅
* **Route:** `/order-food` (`OrderFoodController`)
* **Template:** `f&bStaff`-style guest `order-food.html` / guest templates
* **Flow:** Xem menu không login → Place Order → modal auth → redirect `/auth/login?redirect_to=/order-food`

#### UC16.2 — VNPay food order ✅
* **API:** `PaymentApiController` `/api/v1/payments/food-order/{id}/vnpay`

#### UC16.3 — Charge to Room ✅
* **Service:** `PosServiceImpl` — payment type `CHARGE_TO_ROOM` → insert `FolioItem`

---

### **UC17 — POS Dine-In** ✅

* **Actor:** Cashier / F&B Staff
* **Web:** `PosController` `/fbStaff/create-food-order`
* **API:** `POST /api/pos/orders` — type Dine-In, gán `RestaurantTable`
* **Service:** `PosServiceImpl`

---

### **UC18 — Thanh toán POS & Post-to-Room** ✅

#### UC18.1 — Quản lý bàn ✅
* **Route:** `/fbStaff/table-management`

#### UC18.2 — Room Service (staff) ✅
* **Route:** `/fbStaff/room-service`, `/room-service-detail`

#### UC18.3 — Shift Report ⚠️
* **Route:** `/fbStaff/shift-report`
* **Gap:** `shift-report.js` dùng **dữ liệu mock** — chưa nối API thật

* **Thanh toán chung:**
  * `POST /api/pos/orders/{id}/pay`
  * `POST /api/pos/orders/{id}/add-items`
  * `PUT /api/pos/orders/{id}/status`
  * `POST /api/pos/batch-update-status`

---

### **UC19 — KDS Bếp**

#### UC19.1 — Hiển thị vé món ✅
* **Route:** `/kitchenStaff/kitchen`
* **Data:** `PosWebFacadeServiceImpl.getKitchenData()`
* **Ghi chú:** Polling/API refresh — **không WebSocket** như spec gốc

#### UC19.2 — Cập nhật trạng thái món ✅
* **Field:** `FoodOrderDetail.kotStatus` — PENDING → COOKING → READY → SERVED
* **API:** Dùng chung `PosApiController`

#### UC19.3 — Thông báo phục vụ ⚠️
* **Gap:** Không có push notification real-time tới POS; staff refresh manual

#### UC19.4 — Khóa món hết hàng ✅
* **API:** `POST /api/menu-items/{id}/toggle`
* **Effect:** `MenuItem.isAvailable = false`

#### UC19.5 — E-Menu bếp ✅
* **Route:** `/kitchenStaff/emenu`

---

<a id="mod4"></a>
## 🟢 MOD4: TOUR, ADD-ONS & ĐÁNH GIÁ

### **UC20 — Tìm tour + thời tiết** ✅

* **Actor:** Customer, Guest
* **Routes:** `TourController` `/tours`, `/tours/search`, `/tours/detail`
* **Weather API:** `GET /api/weather` — `WeatherApiClient`
* **Templates:** `guest/tours.html`, `tour/tour-detail.html`

---

### **UC21 — Đặt vé tour** ✅

* **API:** `TourBookingApiController` — `POST /api/tour-bookings`
* **Service:** `TourBookingServiceImpl` — VNPay hoặc post-to-room
* **Email:** tour confirmation templates

---

### **UC22 — Điều hành Tour**

#### UC22.1 — Combo sync ❌
* **Gap:** `MarketingServiceImpl.createCombo` chỉ trong test; không auto-book tour khi mua combo phòng

#### UC22.2 — Phân công nhân sự ⚠️
* **Model:** `TourStaffAssignment`
* **Gap:** CRUD phân công qua admin tour-schedules hạn chế

#### UC22.3 — GPS ❌

#### UC22.4 — Run Itinerary Status ⚠️
* **Model:** `RunItineraryStatus`
* **Gap:** Cập nhật qua tour guide UI một phần

#### UC22.5 — FaceID ⚠️
* Xem UC04.2

#### UC22.6 — Điểm danh thủ công ✅
* **API:** `POST /api/v1/tour-attendance/{attendeeId}/manual`

#### UC22.7 — Tour Guide Dashboard ✅
* **Controller:** `TourGuideController` — `/tourguide/dashboard`, `/doantu`, `/dongnoi`, `/disan`, `/tinhlang`
* **Template:** `tour/Tour.html`

---

### **UC23 — Add-ons** ❌ / ⚠️

#### UC23.1 / UC23.2 ❌
* Catalog add-on Spa/transfer — chưa có entity UI riêng ngoài `HotelService` generic

#### UC23.3 — Marketing Combo ⚠️
* **Service:** `MarketingServiceImpl.createCombo` — test only

---

### **UC24 — Gửi đánh giá** ⚠️

* **Actor:** Customer
* **API:** `POST /api/v1/reviews/tour` (`ReviewRestController`)
* **Gap:** **Không có UI guest** gọi API; review phòng chưa expose endpoint riêng

---

### **UC25 — Kiểm duyệt review** ⚠️

* **Actor:** Admin
* **API:** `PUT /api/v1/reviews/{reviewId}/moderate` ✅
* **UI:** `admin/reviews.html` + `reviews.js` — **chỉ thao tác DOM**, chưa gọi API moderate
* **Gap:** Cần wire frontend admin → `ReviewRestController`

---

<a id="mod5"></a>
## 🟣 MOD5: FOLIO, TÀI CHÍNH & BÁO CÁO

### **UC26 — Folio Aggregation**

#### UC26.1 — Tích lũy chi phí về phòng ✅
* Nguồn: F&B (`FolioItem` source FB), Tour, Room charges
* **Service:** `PosServiceImpl`, `TourBookingServiceImpl`, `NightAuditServiceImpl`

#### UC26.2 — Xem folio real-time ✅
* **API:** `GET /api/folios/room/{roomBookingDetailId}`, `/active`
* **Web:** `/receptionist/folio`, `/receptionist/folio/detail`

#### UC26.3 — Payment Transactions ✅
* **Model:** `PaymentTransaction`
* **Service:** `PaymentServiceImpl`, `VnPayServiceImpl`

#### UC26.4 — Split folio ✅
* **API:** `PUT /api/folios/items/{folioItemId}/split`

#### UC26.5 — Consolidated Invoice ✅
* **Model:** `ConsolidatedInvoice`
* Tạo khi checkout trong `FolioRestController`

#### UC26.6 — Membership Tier ✅
* **Inject:** `MembershipTierRepository` trong `FolioRestController`

---

### **UC27 — Night Audit & Thanh toán**

#### UC27.1 — Night Audit auto-post ⚠️
* **API:** `POST /api/v1/audit/night-audit` (`NightAuditRestController`)
* **Service:** `NightAuditServiceImpl.runNightAudit`
* **BUG đã biết:** Query status `"OCCUPIED"` trong khi check-in set `"Checked_In"` → cần align enum

#### UC27.2 — UI Night Audit ⚠️
* **Template:** `receptionist/night-audit.html`

#### UC27.3 — VNPay đa luồng ✅
* **Return/IPN:** `/api/v1/payments/vnpay-return`, `/vnpay-ipn`
* **Prefixes:** booking thường, `FOLIO_`, `WALKIN_`, food order
* **Util:** `VnPayUtil`, config `application.yml` → `vnpay.*`

#### UC27.4 — Checkout thu tiền ✅
* Xem UC12.6

#### UC27.5 — Email hóa đơn PDF ✅
* **Service:** `EmailServiceImpl.sendInvoiceEmail`
* **PDF:** `InvoicePdfService`
* **Template:** `email/invoice.html`

---

### **UC28 — Manager Dashboard & Báo cáo**

#### UC28.1 — Doanh thu ✅
* **Routes:** `/manager/revenue/daily|monthly|yearly`
* **Data:** `PaymentTransactionRepository`, booking/food/tour repos

#### UC28.2 — Occupancy ✅
* **Route:** `/manager/analytics/occupancy`

#### UC28.3 — Analytics tour/food/stay ⚠️
* **Routes:** `/manager/analytics/tour`, `/food`, `/stay`
* **Gap:** Một số biểu đồ dùng mock data (vd. `analytics-stay.js`)

#### UC28.4 — Export báo cáo ⚠️
* **Route:** `/manager/export`
* **Gap:** `export.js` fetch API bị comment — UI demo

#### UC28.5 — USALI ❌
* Chưa có báo cáo chuẩn USALI tách department

---

<a id="mod6"></a>
## 🆕 MOD6: HỆ THỐNG & TÍCH HỢP

### **UC29 — Email Notifications** ✅

| Sự kiện | Template / Method |
|---------|-------------------|
| OTP đăng ký | `email/registration-otp.html` |
| Reset password | `AuthServiceImpl.sendPasswordResetEmail` |
| Booking confirm/cancel | `EmailServiceImpl` + booking templates |
| Tour confirm | tour email templates |
| Đặt bàn / gia hạn hold | `booking-table.html`, extend-hold |
| Room service / invoice | `invoice.html` |
| Workflow SLA | workflow notification |
| Preview (dev) | `EmailPreviewController` |

---

### **UC30 — Scheduled Jobs** ✅

| Job ID | Mô tả |
|--------|-------|
| `booking_cleanup` | Hủy booking Pending quá `holdExpiresAt` |
| `reservation_cleanup` | Dọn table reservation hết hạn |
| `table_cleanup` | Reset trạng thái bàn |
| `audit_cleanup` | Dọn audit log cũ (`AuditCleanupTask`) |
| `workflow_processor` | Quét SLA workflow |

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

| # | Vấn đề | UC liên quan | Mức |
|---|--------|--------------|-----|
| 1 | Night audit query status `OCCUPIED` vs `Checked_In` | UC27.1 | 🔴 Bug |
| 2 | Admin reviews UI không gọi API moderate | UC25 | 🟠 Gap |
| 3 | `transferRoom` chưa expose cho lễ tân | UC12.5 | 🟠 Gap |
| 4 | Housekeeping/Maintenance không có UI riêng | UC13 | 🟠 Gap |
| 5 | Dynamic pricing & combo marketing chưa runtime | UC09.1, UC23 | 🟠 Gap |
| 6 | Shift report & một số manager chart mock | UC18.3, UC28.3 | 🟡 Mock |
| 7 | FaceID demo hardcode references | UC04.2 | 🟡 Demo |
| 8 | OCR CCCD check-in chưa làm | UC12.3 | 🟡 Spec drift |
| 9 | WebSocket KDS — dùng polling thay thế | UC19 | 🟡 Spec drift |
| 10 | Backdoor `/admin-backdoor`, Debug controllers | — | ⚠️ Dev only |

---

*Tài liệu này phản ánh trạng thái codebase tại ngày 2026-06-28. Khi merge tính năng mới, cập nhật đồng thời `UC_MASTER_TABLE.md` và mục tương ứng trong file này.*
