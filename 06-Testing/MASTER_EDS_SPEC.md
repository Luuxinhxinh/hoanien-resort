# ENGINEERING DOCUMENTATION STANDARD (EDS) & TESTING SPECIFICATION v2.0

## HỆ THỐNG QUẢN LÝ NGHỈ DƯỠNG KAWAI RETREAT RESORT & HUB

| Field          | Value                        |
| -------------- | ---------------------------- |
| Document ID    | KAWAI-ALL-EDS-001            |
| Version        | 1.0                          |
| Date           | 2026-06-09                   |
| Status         | Draft / In Review / Approved |
| Document Owner | Trưởng Nhóm Phát Triển  |
| Author         | Antigravity                  |
| Reviewed by    | Tech Lead                    |
| Based on EDS   | v2.0                         |

## CHANGELOG

| Ngày      | Người thực hiện | Nội dung thay đổi                                                    |
| ---------- | ------------------- | ----------------------------------------------------------------------- |
| 2026-06-11 | Lưu                | Đồng bộ mục lục, bổ sung đặc tả API chi tiết cho cả 5 Module |
| 2026-06-09 | Lưu                | Khởi tạo tài liệu EDS cho toàn bộ hệ thống Kawai Resort         |

## MỤC LỤC

1. Tổng quan Hệ thống
2. Ma trận Truy vết (Traceability Matrix)
3. Architecture Decision Records (ADR)
4. Non-Functional Requirements & SLA
5. Domain Event Catalog (Events Giao tiếp Giữa 5 Modules)
6. Kịch bản Kiểm thử Chi tiết (Đặc tả Test)
7. Đặc tả API Chi tiết (API Specification)
8. Phương pháp Xác minh (API Verification Samples)

---

## 1. Tổng quan Hệ thống

| Field               | Value                                                     |
| ------------------- | --------------------------------------------------------- |
| Module Name         | KAWAI RETREAT RESORT SYSTEM (5 Modules)                   |
| Bounded Context     | Quản lý lưu trú, F&B, Tour, Khách hàng, Thanh toán |
| Data Classification | Sensitive-PII (CCCD, Hộ chiếu) / Internal (Doanh thu)   |
| Compliance Scope    | Nghị định 13/2023/NĐ-CP, USALI (Báo cáo Kế toán)  |

---

## 2. Ma trận Truy vết (Traceability Matrix)

| Requirement ID | Loại | Mô tả yêu cầu                                            | Thành phần Code                           | Compliance Target           |
| -------------- | ----- | ------------------------------------------------------------ | ------------------------------------------- | --------------------------- |
| UC07           | BR    | Ẩn danh hóa dữ liệu người dùng (Quyền được quên) | `UserService.anonymizeUser()`             | Nghị định 13/2023/NĐ-CP |
| UC10, UC20     | BR    | Chống Double-booking cho Phòng và Tour                    | `BookingRepository` (Pessimistic Locking) | Data Integrity              |
| UC24           | BR    | Night Audit tự động gom bill và chốt ngày              | `NightAuditScheduler`                     | Chuẩn Kế toán USALI      |

---

## 3. Architecture Decision Records (ADR)

### ADR-001 — Cơ chế Khóa (Locking Mechanism) cho Booking

**Bối cảnh:** Ngăn chặn nhiều khách hàng hoặc nhân viên đặt cùng một phòng/ghế tour trong cùng 1 mili-giây.
**Quyết định:** Sử dụng **Pessimistic Locking (Khóa bi quan)** qua JPA `@Lock(LockModeType.PESSIMISTIC_WRITE)` ở tầng Repository khi query phòng trống.
**Hệ quả:**

- Đảm bảo tính toàn vẹn 100% cho Inventory.
- Trade-off: Giảm Throughput khi request cùng tranh chấp 1 phòng, nhưng hệ thống Resort không có lượng traffic siêu khủng như TMĐT nên mức giảm này có thể chấp nhận.

---

## 4. Non-Functional Requirements & SLA

### 4.1. Sự Toàn vẹn Dữ liệu (Data Integrity)

| Category    | Requirement        | Target  | Verification Method                       |
| ----------- | ------------------ | ------- | ----------------------------------------- |
| ACID        | Folio & Checkout   | RPO = 0 | Transaction Management `@Transactional` |
| Concurrency | Chống Overbooking | 0% lỗi | JMeter Multi-thread Test                  |

---

## 5. Domain Event Catalog (Events Giao tiếp Giữa 5 Modules)

Hệ thống sử dụng ApplicationEventPublisher (Spring) để giao tiếp nội bộ giữa các module.

### Events Published (Phát ra)

| Event Name              | Publisher              | Subscriber(s)                                  | Action                                           |
| ----------------------- | ---------------------- | ---------------------------------------------- | ------------------------------------------------ |
| `PosOrderCompleted`   | Module 3 (POS)         | Module 5 (Folio)                               | Ghi nợ `Post to Room` vào hóa đơn phòng. |
| `RoomCheckedOut`      | Module 2 (Tiền sảnh) | Module 5 (Hóa đơn), Module 3 (Housekeeping) | Kích hoạt dọn phòng & Đóng Folio.          |
| `NightAuditTriggered` | Hệ thống Cron        | Tất cả Modules                               | Chốt sổ giao dịch ngày.                      |

---

## 6. Kịch bản Kiểm thử Chi tiết (Đặc tả Test)

### KỊCH BẢN 1: INTEGRATION E2E - LUỒNG CHECK-IN, POST TO ROOM & CHECK-OUT TRỌN VẸN

**TC-E2E-FULLFLOW-001**

```gherkin
Feature: Luồng vận hành lõi tích hợp 3 Modules (Front Desk, POS, Folio)
  Background:
    Given Hệ thống có phòng R202 trống.
    And test data classification: SYNTHETIC

  Scenario: Khách Check-in, ăn nhà hàng ghi nợ và Check-out thanh toán 1 lần
    # Module 2
    Given Lễ tân thực hiện Check-in cho khách vào R202
    Then Trạng thái phòng R202 chuyển thành OCCUPIED
    And Một Folio trống được khởi tạo cho R202

    # Module 3
    When Khách xuống nhà hàng gọi món hết 1,000,000 VND
    And Nhân viên POS chọn thanh toán "Post to Room" cho phòng R202
    Then Hệ thống ghi nhận 1,000,000 VND vào Folio của R202

    # Module 5
    When Khách làm thủ tục Check-out
    Then Hệ thống chặn Check-out và báo nợ 1,000,000 VND
    When Lễ tân thực hiện thanh toán tất toán Folio (Tiền mặt/Thẻ)
    Then Folio chuyển trạng thái SETTLED
    And Trạng thái phòng R202 chuyển thành DIRTY (Chờ dọn dẹp)
```

### KỊCH BẢN 2: BẢO MẬT PII & ẨN DANH DỮ LIỆU

**TC-SECURITY-PII-001**

```gherkin
  Scenario: Chạy tiến trình ẩn danh dữ liệu khách hàng
    Given Tài khoản khách hàng có ID "USR-001" với CCCD "012345678900"
    When DPO hoặc Khách hàng yêu cầu xóa tài khoản
    Then Hệ thống cập nhật bản ghi "USR-001"
    And Cột CCCD, Số điện thoại, Tên bị băm (hashed) thành chuỗi vô nghĩa
    And Booking lịch sử của "USR-001" vẫn được giữ nguyên để kiểm toán tài chính
```

### KỊCH BẢN 3: NIGHT AUDIT KIỂM TOÁN ĐÊM CHỐT DOANH THU

**TC-INT-AUDIT-001**

```gherkin
  Scenario: Hệ thống chạy Night Audit vào 2h sáng
    Given 5 phòng đang OCCUPIED
    When Job NightAuditScheduler kích hoạt
    Then Giá phòng của 5 phòng được tự động cộng thêm 1 ngày vào Folio tương ứng
    And Báo cáo doanh thu ngày hiện tại được chốt (Freezed)
    And System Business Date tiến lên 1 ngày
```

---

## 7. Đặc tả API Chi tiết (API Specification)

### 7.1. MODULE 4 — QUẢN LÝ LỮ HÀNH: UC19 - Tìm kiếm Gói Tour

### 6.1. Endpoint

| Method | Path              | Auth Level                | Required Roles  | Rate Limit | Idempotent? |
| ------ | ----------------- | ------------------------- | --------------- | ---------- | ----------- |
| GET    | `/tours/search` | Public (Guest / Customer) | Guest, Customer | 100/min    | Yes         |

### 6.2. Request Parameters

| Parameter    | Type                     | Required | Default               | Description                              |
| ------------ | ------------------------ | -------- | --------------------- | ---------------------------------------- |
| `fromDate` | `LocalDate` (ISO 8601) | No       | Today                 | Ngày bắt đầu tìm kiếm (yyyy-MM-dd) |
| `toDate`   | `LocalDate` (ISO 8601) | No       | `fromDate + 7 days` | Ngày kết thúc tìm kiếm (yyyy-MM-dd) |

### 6.3. Response — 200 OK (Happy Path)

```json
{
  "tours": [
    {
      "tourId": 1,
      "tourName": "Vịnh Hạ Long - 1 Ngày",
      "tourType": "DAY_TRIP",
      "basePrice": 1500000,
      "maxCapacity": 30,
      "availableSlots": 15,
      "departureDate": "2026-06-20",
      "scheduleStatus": "Open",
      "weatherAvailable": true,
      "weatherDescription": "Sunny",
      "temperature": 32.5
    }
  ],
  "fromDate": "2026-06-15",
  "toDate": "2026-06-25"
}
```

### 6.4. Response — 200 OK (Weather API Unavailable)

```json
{
  "tours": [
    {
      "tourId": 1,
      "tourName": "Vịnh Hạ Long - 1 Ngày",
      "tourType": "DAY_TRIP",
      "basePrice": 1500000,
      "maxCapacity": 30,
      "availableSlots": 15,
      "departureDate": "2026-06-20",
      "scheduleStatus": "Open",
      "weatherAvailable": false,
      "weatherDescription": null,
      "temperature": null
    }
  ],
  "fromDate": "2026-06-15",
  "toDate": "2026-06-25"
}
```

### 6.5. Business Rules

| Rule ID         | Description                                                                 | Implementation                                                                                       |
| --------------- | --------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------- |
| BR-TR-01        | Chỉ hiển thị lịch trình có trạng thái "Open"                        | `TourScheduleRepository.findByDepartureDateBetweenAndScheduleStatus(fromDate, toDate, "Open")`     |
| GRACEFUL-DEG-01 | Nếu OpenWeather API lỗi, tour vẫn hiển thị, ẩn thông tin thời tiết | `TourServiceImpl.enrichWithWeatherData()` catch RuntimeException, set `weatherAvailable = false` |

### 6.6. Error Codes

| Code         | HTTP Status | Message                               | Trigger Condition                                      |
| ------------ | ----------- | ------------------------------------- | ------------------------------------------------------ |
| `TOUR-001` | 400         | `fromDate must not be null`         | Thiếu tham số fromDate (validation ở Service layer) |
| `TOUR-002` | 400         | `fromDate must not be after toDate` | fromDate > toDate                                      |

### 7.2. MODULE 1 — XÁC THỰC, HỒ SƠ & DỮ LIỆU GỐC (UC01-UC08)

#### Endpoint: POST `/api/v1/auth/login`

* **Mô tả:** Đăng nhập hệ thống bằng tên tài khoản và mật khẩu, yêu cầu OTP (2FA) ở bước sau.
* **Request Body:**
  ```json
  {
    "username": "guest_user",
    "password": "Password123"
  }
  ```
* **Response (200 OK):**
  ```json
  {
    "status": "SUCCESS",
    "message": "OTP_REQUIRED",
    "tempToken": "temp_token_xyz"
  }
  ```

#### Endpoint: POST `/api/v1/users/anonymize`

* **Mô tả:** Ẩn danh hóa thông tin PII theo quyền được quên (Nghị định 13/2023/NĐ-CP).
* **Request Body:**
  ```json
  {
    "userId": 123,
    "reason": "Yêu cầu xóa tài khoản từ phía khách hàng"
  }
  ```
* **Response (200 OK):**
  ```json
  {
    "status": "SUCCESS",
    "message": "User data successfully anonymized."
  }
  ```

### 7.3. MODULE 2 — QUẢN LÝ PHÒNG & LỄ TÂN VẬN HÀNH (UC09-UC13)

#### Endpoint: POST `/api/v1/rooms/search`

* **Mô tả:** Tìm kiếm phòng trống theo khoảng thời gian và hạng phòng.
* **Request Body:**
  ```json
  {
    "checkInDate": "2026-06-15",
    "checkOutDate": "2026-06-18",
    "category": "DELUXE"
  }
  ```
* **Response (200 OK):**
  ```json
  {
    "checkInDate": "2026-06-15",
    "checkOutDate": "2026-06-18",
    "availableRooms": [
      {
        "roomId": "R102",
        "category": "DELUXE",
        "basePrice": 2000000,
        "status": "CLEAN"
      }
    ]
  }
  ```

#### Endpoint: POST `/api/v1/bookings`

* **Mô tả:** Tạo đơn đặt phòng mới và khóa phòng bi quan (Pessimistic Locking).
* **Request Body:**
  ```json
  {
    "roomId": "R102",
    "checkInDate": "2026-06-15",
    "checkOutDate": "2026-06-18",
    "promoCode": "SUMMER10"
  }
  ```
* **Response (201 Created):**
  ```json
  {
    "bookingId": "BK-1001",
    "status": "CONFIRMED",
    "cancellationDeadline": "2026-06-13T12:00:00Z"
  }
  ```

#### Endpoint: POST `/api/v1/reception/check-in`

* **Mô tả:** Lễ tân thực hiện check-in và tự động tạo Folio nợ trống.
* **Request Body:**
  ```json
  {
    "bookingId": "BK-1001",
    "guests": [
      {
        "fullName": "Nguyen Van A",
        "cccd": "012345678901"
      }
    ]
  }
  ```
* **Response (200 OK):**
  ```json
  {
    "status": "SUCCESS",
    "folioId": "FOL-1001",
    "roomNumber": "102"
  }
  ```

### 7.4. MODULE 3 — DỊCH VỤ ẨM THỰC & NHÀ HÀNG (UC14-UC18)

#### Endpoint: POST `/api/v1/pos/post-to-room`

* **Mô tả:** Ký gửi nợ từ hóa đơn POS nhà hàng về Folio của phòng đang lưu trú.
* **Request Body:**
  ```json
  {
    "roomId": "R102",
    "amount": 500000,
    "description": "Bữa tối nhà hàng Buffet",
    "posOrderId": "POS-888"
  }
  ```
* **Response (200 OK):**
  ```json
  {
    "status": "SUCCESS",
    "folioId": "FOL-1001",
    "amountCharged": 500000,
    "currentBalance": 500000
  }
  ```

### 7.5. MODULE 4 — QUẢN LÝ LỮ HÀNH & ĐÁNH GIÁ (UC20)

#### Endpoint: POST `/api/v1/tours/bookings`

* **Mô tả:** Đặt chuyến tour cho khách lưu trú và chống Double-booking.
* **Request Body:**
  ```json
  {
    "tourScheduleId": 5,
    "numberOfAttendees": 2
  }
  ```
* **Response (201 Created):**
  ```json
  {
    "tourBookingId": "TBK-2002",
    "status": "BOOKED",
    "seatsConfirmed": 2
  }
  ```

### 7.6. MODULE 5 — KIỂM TOÁN ĐÊM, TÀI CHÍNH & BÁO CÁO (UC24-UC28)

#### Endpoint: GET `/api/v1/folios/{id}`

* **Mô tả:** Lấy báo cáo dư nợ tổng hợp (Folio Aggregation) của phòng phục vụ check-out.
* **Response (200 OK):**
  ```json
  {
    "folioId": "FOL-1001",
    "roomId": "R102",
    "roomCharges": 4000000,
    "serviceCharges": 500000,
    "paidAmount": 0,
    "balanceDue": 4500000,
    "status": "UNPAID"
  }
  ```

#### Endpoint: POST `/api/v1/audit/night-audit`

* **Mô tả:** Thủ công kích hoạt tiến trình Night Audit để chốt sổ kế toán ngày cũ và cộng phí phòng.
* **Response (200 OK):**
  ```json
  {
    "status": "SUCCESS",
    "businessDate": "2026-06-12",
    "roomsAudited": 12,
    "totalRevenueAggregated": 24500000
  }
  ```

---

## 7.7. API Specification — UC21: Điểm danh AI Face Scan (Module 4)

### 7.7.1. Endpoints

| Method | Path | Auth Level | Required Roles | Rate Limit | Idempotent? |
|--------|------|------------|----------------|------------|-------------|
| POST | `/api/v1/tour-attendance/{attendeeId}/verify` | Protected | Tour_Guide, Admin | 50/min | No |
| POST | `/api/v1/tour-attendance/{attendeeId}/manual` | Protected | Tour_Guide, Admin | 50/min | No |

### 7.7.2. Authorization Matrix
| Tác vụ / Endpoint | GUEST | CUSTOMER | RECEPTIONIST | F&B STAFF | ADMIN / TOUR_GUIDE |
| --- | --- | --- | --- | --- | --- |
| Xác thực khuôn mặt (`POST /verify`) | ❌ | ❌ | ❌ | ❌ | ✅ |
| Điểm danh thủ công (`POST /manual`) | ❌ | ❌ | ❌ | ❌ | ✅ |

### 7.7.3. Request / Response Sample
**[POST] Xác thực AI Face Scan**
```bash
curl -X POST https://api.kawairesort.com/api/v1/tour-attendance/1/verify \
  -H "Authorization: Bearer [TOUR_GUIDE_TOKEN]" \
  -F "image=@/path/to/face.jpg"

# Expected Response (200 OK):
"Attendance verified successfully"

# Expected Response (400 Bad Request):
"Face match score below threshold"
```

**[POST] Điểm danh thủ công**
```bash
curl -X POST https://api.kawairesort.com/api/v1/tour-attendance/1/manual?status=PRESENT \
  -H "Authorization: Bearer [TOUR_GUIDE_TOKEN]"

# Expected Response (200 OK):
"Attendance marked manually"
```

### 7.7.4. Error Codes & Business Rules
| Rule/Code | HTTP Status | Message/Logic | Trigger Condition |
|------|-------------|---------|------------------|
| `BR-TR-02` | 200 | Cập nhật `PRESENT` | Độ trùng khớp AI >= 0.85 |
| `ERR-AI-01` | 400 | `Face match score below threshold` | Độ trùng khớp AI < 0.85 |
| `ERR-AI-02` | 503 | `AI Service unavailable` | Dịch vụ AI bên thứ ba bị sập |
| `ERR-TOUR-01`| 400 | `Attendee not found` | `attendeeId` không tồn tại |

## 7.8. API Specification — UC22 & UC23: Đánh giá dịch vụ & Kiểm duyệt (Module 4)

### 7.8.1. Endpoints

| Method | Path | Auth Level | Required Roles | Rate Limit | Idempotent? |
|--------|------|------------|----------------|------------|-------------|
| POST | `/api/v1/reviews/tour` | Protected | Customer | 10/min | No |
| PUT | `/api/v1/reviews/{reviewId}/moderate` | Protected | Admin | 50/min | Yes |

### 7.8.2. Authorization Matrix
| Tác vụ / Endpoint | GUEST | CUSTOMER | RECEPTIONIST | F&B STAFF | ADMIN / TOUR_GUIDE |
| --- | --- | --- | --- | --- | --- |
| Gửi đánh giá tour (`POST /tour`) | ❌ | ✅ | ❌ | ❌ | ❌ |
| Kiểm duyệt đánh giá (`PUT /{reviewId}/moderate`) | ❌ | ❌ | ❌ | ❌ | ✅ (Chỉ Admin) |

### 7.8.3. Request / Response Sample
**[POST] Gửi đánh giá Tour**
```bash
curl -X POST "https://api.kawairesort.com/api/v1/reviews/tour?customerId=1&tourBookingId=100&rating=5&reviewText=Excellent!" \
  -H "Authorization: Bearer [CUSTOMER_TOKEN]"

# Expected Response (200 OK):
{
  "id": 1,
  "ratingService": 5,
  "reviewText": "Excellent!",
  "moderationStatus": "Pending",
  "createdAt": "2026-06-12T10:00:00"
}

# Expected Response (400 Bad Request):
"Tour is not completed yet"
```

**[PUT] Admin kiểm duyệt đánh giá**
```bash
curl -X PUT "https://api.kawairesort.com/api/v1/reviews/1/moderate?adminId=99&newStatus=Hidden&reason=Ngôn từ thô tục" \
  -H "Authorization: Bearer [ADMIN_TOKEN]"

# Expected Response (200 OK):
{
  "id": 1,
  "ratingService": 1,
  "reviewText": "Tệ hại",
  "moderationStatus": "Hidden",
  "moderationReason": "Ngôn từ thô tục",
  "moderatedBy": {
    "id": 99,
    "fullName": "Admin Tran"
  }
}

# Expected Response (400 Bad Request):
"Moderation reason is strictly required according to BR-TR-04"
```

### 7.8.4. Error Codes & Business Rules
| Rule/Code | HTTP Status | Message/Logic | Trigger Condition |
|------|-------------|---------|------------------|
| `BR-TR-03` | 400 | `Review period has expired (7 days limit)` | Quá 7 ngày từ ngày khởi hành tour |
| `BR-TR-04` | 400 | `Moderation reason is strictly required...` | Không nhập lý do kiểm duyệt (reason rỗng/null) |
| `ERR-REV-01`| 400 | `Customer does not own this booking` | ID khách không khớp với ID đặt tour |
| `ERR-REV-02`| 400 | `Tour is not completed yet` | Khách đánh giá tour chưa đi |
| `ERR-REV-03`| 400 | `Review not found` | Review ID không tồn tại |
| `ERR-REV-04`| 400 | `Admin not found` | Admin ID không tồn tại |

---

## 8. Phương pháp Xác minh (API Verification Samples)

### 8.1. UC09 — Tìm kiếm phòng trống (RoomService.searchAvailableRooms)

**[POST] Tìm phòng trống**

```bash
# [POST] Tìm phòng trống theo khoảng ngày
curl -X POST https://api.kawairesort.com/api/v1/rooms/search \
  -H "Authorization: Bearer [CUSTOMER_TOKEN]" \
  -H "Content-Type: application/json" \
  -d '{
    "checkInDate": "2026-08-01",
    "checkOutDate": "2026-08-05",
    "categoryName": "Deluxe",
    "minCapacity": 2
  }'

# Expected Response (200):
{
  "status": "SUCCESS",
  "rooms": [
    {
      "roomId": 1,
      "roomNumber": "R101",
      "categoryName": "Deluxe",
      "pricePerNight": 2000000,
      "capacity": 2,
      "checkInDate": "2026-08-01",
      "checkOutDate": "2026-08-05"
    }
  ]
}
```

**Business Rules:**

* BR-FO-01: Chỉ hiển thị phòng không có booking trùng ngày
* BR-FO-04: Trạng thái phòng Vacant_Clean hoặc Available
* BR-FIN-05: Giá phòng BigDecimal scale 0, HALF_UP

**Implementation:**

* Interface: `RoomService.searchAvailableRooms(RoomSearchRequestDTO)`
* Repository: `RoomRepository.findAll()`, `RoomBookingRepository.countOverlappingBookings()`

---

### 7.1.b UC11 — Xem sơ đồ Matrix phòng trống (RoomService.getRoomDashboard)

**[GET] Lấy danh sách sơ đồ phòng**
```bash
# [GET] Lấy danh sách sơ đồ phòng Front Desk
curl -X GET https://api.kawairesort.com/api/v1/rooms/dashboard \
  -H "Authorization: Bearer [RECEPTIONIST_TOKEN]" \
  -H "Content-Type: application/json"

# Expected Response (200):
{
  "status": "SUCCESS",
  "rooms": [
    {
      "roomId": 1,
      "roomNumber": "R101",
      "categoryName": "Deluxe River View",
      "roomStatus": "Vacant",
      "pricePerNight": 2800000,
      "capacity": 2
    }
  ]
}
```

**Business Rules:**
* BR-FO-04: Trạng thái phòng phải tuân thủ nghiêm ngặt vòng đời phòng (Vacant, Occupied, Dirty, Maintenance).
* BR-FIN-05: Giá phòng phải là `BigDecimal` scale 0, `HALF_UP`.

**Error Codes:**
* `SYS-001` (500) - `Lỗi hệ thống nội bộ`

**Implementation:**
* Interface: `RoomService.getRoomDashboard()`
* Repository: `RoomRepository.findAll()`

---

### 7.2. UC10 — Đặt phòng & Thanh toán cọc (BookingService)

**[POST] Đặt phòng mới**

```bash
# [POST] Đặt phòng
curl -X POST https://api.kawairesort.com/api/v1/bookings \
  -H "Authorization: Bearer [CUSTOMER_TOKEN]" \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "roomNumber": "R101",
    "checkInDate": "2026-08-15",
    "checkOutDate": "2026-08-20",
    "depositAmount": 2000000,
    "promotionCode": "SUMMER10"
  }'

# Expected Response (201):
{
  "bookingId": 12345,
  "bookingStatus": "CONFIRMED",
  "depositAmount": 2000000,
  "discountedPrice": 9000000,
  "checkInDate": "2026-08-15",
  "checkOutDate": "2026-08-20",
  "cancellationDeadline": "2026-08-13"
}
```

**[DELETE] Hủy booking**

```bash
# [DELETE] Hủy booking trước 48h
curl -X DELETE https://api.kawairesort.com/api/v1/bookings/12345 \
  -H "Authorization: Bearer [CUSTOMER_TOKEN]"

# Expected Response (200):
{
  "refundAmount": 2000000,
  "bookingStatus": "Cancelled_Refunded"
}
```

**Business Rules:**

* BR-DATE-01: checkOutDate > checkInDate
* BR-FO-01: Pessimistic Locking chống overbooking
* BR-FIN-02: Hủy trước 48h → hoàn 100%; trong 48h → tịch thu cọc
* BR-STATUS-01: Booking tạo mới → "CONFIRMED"
* BR-STATUS-02: Hủy trước 48h → "Cancelled_Refunded"; sau → "Cancelled_Forfeited"
* BR-ERR-01: Exception message chứa error code `[ERR_PROMO_XXX]`

**Implementation:**

* Interface: `BookingService.createBooking()`, `BookingService.cancelBooking()`
* Repository: `RoomBookingRepository.countOverlappingBookings()`, `PromotionRepository.findByPromoCode()`

---

### 7.3. Chống Over-Booking (DB Inspection)

Mô phỏng 2 threads gọi API `/api/bookings` cùng lúc:

```sql
-- Kiểm tra DB sau khi chạy JMeter Stress Test
SELECT room_id, COUNT(*) as booking_count 
FROM bookings 
WHERE check_in = '2026-06-15' AND status = 'CONFIRMED'
GROUP BY room_id HAVING booking_count > 1;
-- Expected: Empty result (Không có phòng nào bị book 2 lần cùng ngày)
```

### 8.2. Ghi nợ phòng (Post to Room) - API Call

```bash
# [POST] POS gửi yêu cầu ghi nợ vào phòng R202
curl -X POST https://api.kawairesort.com/api/v1/pos/post-to-room \
  -H "Authorization: Bearer [POS_STAFF_TOKEN]" \
  -H "Content-Type: application/json" \
  -d '{
    "roomId": "R202",
    "amount": 1000000,
    "description": "Bữa tối nhà hàng Seafood",
    "posOrderId": "POS-999"
  }'

# Expected Response (201):
{
  "status": "SUCCESS",
  "folioId": "FOL-202-A",
  "addedAmount": 1000000,
  "newTotalBalance": 1000000
}
```

### 8.3. Tạo tài khoản nhân viên mới (Quản lý RBAC) - API Call

```bash
# [POST] Admin tạo tài khoản nhân viên (UC05.1 - TC-M1-015)
curl -X POST https://api.kawairesort.com/api/v1/admin/employees \
  -H "Authorization: Bearer [ADMIN_TOKEN]" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "staff_new",
    "password": "password123",
    "roleId": 2,
    "fullName": "Nguyen Van Staff",
    "gender": "MALE",
    "cccd": "012345678912",
    "phone": "0987654321",
    "email": "staff@example.com",
    "salary": 10000000
  }'

# Expected Response (201):
{
  "status": "SUCCESS",
  "employeeId": 1,
  "accountId": 1,
  "role": "ROLE_FB_STAFF"
}
```

---

**BẢNG TỔNG HỢP PHÂN QUYỀN (Authorization Matrix)**
| Tác vụ / Endpoint | GUEST | CUSTOMER | RECEPTIONIST | F&B STAFF | ADMIN / MANAGER |
| --- | --- | --- | --- | --- | --- |
| Đặt phòng (`POST /bookings`) | ❌ | ✅ | ✅ | ❌ | ✅ |
| Xem sơ đồ phòng (`GET /rooms/dashboard`) | ❌ | ❌ | ✅ | ❌ | ✅ |
| Post to Room (`POST /pos/charge`) | ❌ | ❌ | ❌ | ✅ | ✅ |
| Night Audit (`POST /audit/run`) | ❌ | ❌ | ❌ | ❌ | ✅ |
| Đổi mật khẩu (`PUT /users/me`) | ❌ | ✅ Own | ✅ Own | ✅ Own | ✅ All |
