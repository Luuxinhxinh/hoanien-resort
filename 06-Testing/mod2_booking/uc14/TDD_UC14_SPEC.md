# TEST-DRIVEN DEVELOPMENT SPECIFICATION

## UC-14: Walk-in Guest Check-in — Đặc tả Kiểm thử Hướng Phát triển

| Field                  | Value                       |
| ---------------------- | --------------------------- |
| **Document ID**  | `KAWAI-TDD-MOD2-UC14-001` |
| **Version**      | 1.1                         |
| **Date**         | 2026-06-19                  |
| **Status**       | Approved                    |
| **Standard**     | ISO/IEC/IEEE 29119-3:2021   |
| **Author**       | Chu Xuân Dũng             |
| **Reviewed by**  | Nguyễn Xuân Lưu          |
| **DPO Sign-off** |                             |
| **Approved by**  | [ ] Pending                 |
| **Based on EDS** | v2.0                        |

> **Quy ước TDD:** Tài liệu này mô tả test cases TRƯỚC khi viết production code.
> Thứ tự bắt buộc: viết test (`.java`) → chạy → xác nhận FAIL 🔴 → implement → PASS 🟢 → refactor 🔵.

---

### CHANGELOG

| Ngày      | Người thực hiện | Nội dung thay đổi                                                                                                                   |
| ---------- | ------------------- | -------------------------------------------------------------------------------------------------------------------------------------- |
| 2026-06-19 | Chu Xuân Dũng     | Refactor scope: xóa TC-M2-022 (trùng TC-M2-021), chuyển TC-M2-027 sang Security Suite, thu gọn E2E, bổ sung TC-M2-031/032/033/034 |
| 2026-06-19 | Chu Xuân Dũng     |  Khởi tạo tài liệu TDD cho UC-14 (Walk-in Guest Check-in)                                                                         |

---

### MỤC LỤC

1. [Thông tin Module](#1-thong-tin-module)
2. [Logic Issues Resolved](#2-logic-issues-resolved)
3. [Test Design Specification (TDS)](#3-test-design-specification-tds)
4. [Test Case Specification](#4-test-case-specification)
5. [Red-Green-Refactor Tracker](#5-red-green-refactor-tracker)
6. [Entry / Exit Criteria](#6-entry--exit-criteria)
7. [Rollback Plan](#7-rollback-plan)

---

### 1. Thông tin Module

| Field                         | Value                                                                           |
| ----------------------------- | ------------------------------------------------------------------------------- |
| **Feature / Gap ID**    | `GAP-MOD2-UC14`                                                               |
| **Use Case**            | UC-14 — Walk-in Guest Check-in                                                 |
| **Module**              | MOD2 — Đặt phòng & Tiền sảnh vận hành                                   |
| **Priority**            | 🔴 P0 — Critical                                                               |
| **Sprint**              | S2 (2026-06-19 → 2026-07-03)                                                   |
| **Milestone**           | M3 Alpha — 2026-07-11                                                          |
| **Data Classification** | Sensitive-PII (CCCD, Hộ chiếu, Ngày sinh)                                    |
| **Compliance Scope**    | Luật cư trú 2020, Luật du lịch Việt Nam 2017, Nghị định 13/2023/NĐ-CP |
| **Primary Actor**       | Receptionist                                                                    |
| **Secondary Actor**     | System / Customer                                                               |

---

### 2. Logic Issues Resolved

| #  | Spec gốc (sai / thiếu)                                             | Thực tế (schema / policy)                                                                           | Fix áp dụng trong test                                                                            |
| -- | -------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------- |
| L1 | Walk-in không cần kiểm tra phòng trước khi tạo booking        | Phải kiểm tra phòng Vacant_Clean TRƯỚC khi tạo Booking để tránh race condition               | Test concurrency: 2 lễ tân chọn cùng phòng walk-in → 1 thành công, 1 thất bại (TC-M2-026) |
| L2 | Walk-in tạo booking với status gì?                                | SRS: tạo booking status =`Confirmed` và NGAY LẬP TỨC chuyển sang `Checked_In`                | TC-M2-021 kiểm tra state transition trong cùng 1 happy-path assertion block                       |
| L3 | Deposit/Credit Limit cho Walk-in chưa rõ                           | Walk-in phải thiết lập Credit Limit và thu tiền tại quầy (không qua VNPay)                    | TC-M2-021 xác nhận `creditLimit` được gán giá trị hợp lệ sau check-in thành công      |
| L4 | PII encryption là trách nhiệm của UC-14 hay CustomerService?     | PII encryption là responsibility của CustomerService / Security Module — không phải UC-14        | TC-M2-027 tách ra khỏi UC-14, chuyển sang `CustomerServicePIISecurityTest.java`                |
| L5 | Phòng DIRTY/MAINTENANCE có thể walk-in không?                    | Tuyệt đối không — chỉ `Vacant_Clean` mới hợp lệ                                            | TC-M2-030: chọn phòng Dirty/Maintenance → hệ thống trả lỗi                                   |
| L6 | Nếu khách đã có account → xử lý thế nào?                   | Tái sử dụng Customer profile, KHÔNG tạo duplicate; System auto-link Reservation vào account cũ | TC-M2-032: reuse account cũ, tạo booking mới liên kết customer_id cũ                          |
| L7 | Walk-in có auto-tạo Customer Account không? (BR-08, BR-09, BR-10) | Có — hệ thống tự sinh account với default password; khách nhận thông báo sau                | TC-M2-031: verify account được tạo và link với Reservation                                    |
| L8 | Số lượng khách vượt capacity phòng → hành vi?               | Hệ thống phải reject; không tạo Reservation                                                      | TC-M2-033: numberOfGuests > roomCapacity → 400 validation error                                    |
| L9 | Temporary Residence Reporting fail → rollback hay log?              | Phải xác định rõ: Option A (rollback) hay Option B (log + retry queue)                           | TC-M2-034: test hành vi đã được quyết định theo ADR-UC14-004                               |

---

### 3. Test Design Specification (TDS)

#### TDS-01 — Scope / Phạm vi

```
Walk-in Check-in Service Backend — Spring Boot
├── Controller  : ReceptionistController.processWalkInCheckIn()
├── Service     : WalkInCheckInService.createWalkInBookingAndCheckIn()
├── Repository  : RoomRepository, BookingRepository, CustomerRepository, DependentRepository
└── Event       : WalkInCheckInCompletedEvent, RoomCheckedInEvent (ApplicationEventPublisher)

NGOÀI PHẠM VI UC-14 (test riêng):
  ✖ AES-256 Encryption Algorithm  → CustomerServicePIISecurityTest.java
  ✖ POS / Post-to-Room            → POSIntegrationTest.java
  ✖ Finance Debt / Folio Balance  → FolioServiceTest.java
  ✖ Checkout Workflow             → CheckoutServiceUC15Test.java
```

#### TDS-02 — Test Basis / Cơ sở Kiểm thử

| Source                | Items Derived                                                                      |
| --------------------- | ---------------------------------------------------------------------------------- |
| SRS §2.1.14 UC-14    | Normal Flow (16 bước), AF-01/02/03, E-01/02/03                                   |
| BR-01 → BR-10 (UC14) | Nhận dạng, phòng trống, trạng thái, PII storage, cư trú, account, capacity |
| EDS ADR-UC14-001      | PII phải mã hóa — kiểm tra ở Customer module level, không ở UC-14          |
| EDS ADR-UC14-002      | Pessimistic Lock chống overbooking Walk-in                                        |
| EDS ADR-UC14-003      | ACID Transaction Boundary cho toàn bộ Walk-in flow                               |
| EDS ADR-UC14-004      | Temporary Residence Failure Behavior (Option B: log + retry — xem §2 L9)         |

#### TDS-03 — Test Techniques

| Kỹ thuật                              | Áp dụng cho Test Case                                               |
| --------------------------------------- | --------------------------------------------------------------------- |
| **Equivalence Partitioning**      | TC-M2-021 (dữ liệu hợp lệ), TC-M2-023 (dữ liệu không hợp lệ) |
| **Boundary Value Analysis**       | TC-M2-023 (CCCD đúng 12 số vs sai format), TC-M2-033 (capacity)    |
| **State Transition**              | TC-M2-021 (Vacant_Clean → OCCUPIED, Booking → CHECKED_IN)           |
| **Concurrency Test**              | TC-M2-026 (race condition Walk-in — Pessimistic Lock)                |
| **Negative Test**                 | TC-M2-023, TC-M2-024, TC-M2-025, TC-M2-030, TC-M2-033                 |
| **Integration Test (giới hạn)** | TC-M2-021 (Walk-in → OCCUPIED + Folio khởi tạo)                    |

#### TDS-04 — Test Conditions and Coverage Items

| Condition ID    | SRS Coverage                        | Test Condition                                                              | Test Case |
| --------------- | ----------------------------------- | --------------------------------------------------------------------------- | --------- |
| TC-COND-UC14-01 | Normal Flow (toàn bộ)             | Walk-in happy path: dữ liệu hợp lệ, phòng Vacant_Clean                 | TC-M2-021 |
| TC-COND-UC14-02 | E-01                                | CCCD/Passport không hợp lệ → từ chối, không tạo Reservation         | TC-M2-023 |
| TC-COND-UC14-03 | AF-01                               | Không có phòng trống → báo lỗi, Receptionist phải chọn loại khác | TC-M2-024 |
| TC-COND-UC14-04 | E-03                                | Transaction rollback khi lỗi giữa chừng                                  | TC-M2-025 |
| TC-COND-UC14-05 | BR-02 (chống overbooking)          | 2 lễ tân chọn cùng phòng đồng thời → Pessimistic Lock              | TC-M2-026 |
| TC-COND-UC14-06 | AF-02                               | Khách đã có profile → reuse, không tạo duplicate, reuse account cũ  | TC-M2-028 |
| TC-COND-UC14-07 | AF-03, BR-07                        | Thêm khách đi kèm → đăng ký tạm trú, lưu Dependent               | TC-M2-029 |
| TC-COND-UC14-08 | E-02, BR-02                         | Phòng DIRTY/MAINTENANCE → Walk-in bị chặn                               | TC-M2-030 |
| TC-COND-UC14-09 | Normal Flow Step 11-13, BR-08/09/10 | Walk-in tự động tạo Customer Account cho khách mới                    | TC-M2-031 |
| TC-COND-UC14-10 | AF-02                               | Khách đã có account → không tạo mới, Reservation link account cũ   | TC-M2-032 |
| TC-COND-UC14-11 | Normal Flow Step 5, BR-02           | numberOfGuests vượt capacity phòng → reject validation                  | TC-M2-033 |
| TC-COND-UC14-12 | BR-07, ADR-UC14-004                 | Temporary Residence Reporting fail → check-in vẫn thành công, log lỗi  | TC-M2-034 |

> [!NOTE]
> **TC-M2-022 ĐÃ XÓA:** State transition (Vacant_Clean → OCCUPIED, Booking → CHECKED_IN) đã được kiểm tra đầy đủ trong TC-M2-021 Step 4-5. Không cần test case riêng biệt.
>
> **TC-M2-027 ĐÃ CHUYỂN:** AES-256 encryption algorithm verification → `CustomerServicePIISecurityTest.java` (Customer Module). Trong UC-14 chỉ cần biết Customer được tạo thành công — không cần verify cipher logic.

---

### 4. Test Case Specification

---

#### `TC-M2-021` — Walk-in Check-in thành công: Booking + Room OCCUPIED + Folio khởi tạo

**Severity:** 🔴 CRITICAL
**Feature Under Test:** UC-14 Normal Flow (toàn bộ 16 bước)
**SRS Coverage:** Normal Flow, BR-01, BR-02, BR-03, BR-04, BR-05, BR-06
**TDD Phase:** 🔴 RED
**Test File:** `WalkInCheckInServiceUC14Test.java`
**Test Data Classification:** SYNTHETIC

**Preconditions:**

- Phòng `R301` trạng thái `Vacant_Clean`, capacity = 2, hạng `Standard`.
- Không có Customer nào có CCCD `001234567890` trong hệ thống.
- Receptionist đã đăng nhập, có quyền `ROLE_RECEPTIONIST`.

**Test Steps:**

1. Lễ tân gọi `POST /api/v1/reception/walk-in` với payload hợp lệ:
   ```json
   {
     "fullName": "Nguyen Van Test",
     "dateOfBirth": "1990-05-15",
     "cccd": "001234567890",
     "phone": "0901234567",
     "checkInDate": "2026-06-19",
     "checkOutDate": "2026-06-21",
     "numberOfGuests": 1,
     "roomId": 301,
     "creditLimit": 5000000
   }
   ```
2. Assert HTTP response status = `201 Created`.
3. Assert response body chứa `bookingId` (not null), `roomNumber = "R301"`, `status = "CHECKED_IN"`.
4. **[State Transition]** Truy vấn DB: `rooms.room_status = "OCCUPIED"` cho room R301.
5. **[State Transition]** Truy vấn DB: `room_bookings.booking_status = "CHECKED_IN"` cho booking vừa tạo.
6. **[State Transition]** Truy vấn DB: `room_booking_details.detail_status = "CHECKED_IN"` cho detail vừa tạo.
7. Truy vấn DB: Customer mới tồn tại với `full_name = "Nguyen Van Test"` (không cần verify cipher — đó là trách nhiệm CustomerService test).
8. **[Folio Init]** Truy vấn DB: `folio_items` có record liên kết với `bookingId` vừa tạo; `folioId` trong response không null.
9. **[Credit Limit]** Truy vấn DB: `room_bookings.credit_limit = 5000000`.
10. Assert sự kiện `WalkInCheckInCompletedEvent` được publish (kiểm tra qua mock EventPublisher).

**Expected Result (PASS):**

- API trả về `201 Created` kèm `bookingId`, `roomNumber`, `status = CHECKED_IN`.
- Room R301: `OCCUPIED`. Booking: `CHECKED_IN`. Detail: `CHECKED_IN`.
- Folio được khởi tạo (folioId not null); không kiểm tra balance hay debt logic.
- Customer mới tồn tại trong DB.
- `creditLimit` = 5,000,000 được gán đúng.
- `WalkInCheckInCompletedEvent` được publish.

**Expected Result (FAIL):**

- Phòng không chuyển `OCCUPIED` sau khi tạo booking.
- Booking status = `CONFIRMED` mà không chuyển sang `CHECKED_IN`.
- Folio không được khởi tạo (folioId null hoặc không tồn tại record).
- Partial commit: Customer được lưu nhưng Booking không tồn tại.

---

#### `TC-M2-023` — E-01: CCCD/Passport không hợp lệ → từ chối tạo Walk-in

**Severity:** 🟠 HIGH
**Feature Under Test:** UC-14 Step 4, E-01
**SRS Coverage:** BR-01 (Identification required and valid)
**TDD Phase:** 🔴 RED
**Test File:** `WalkInCheckInServiceUC14Test.java`
**CWE:** CWE-20 — Improper Input Validation
**Test Data Classification:** SYNTHETIC

**Preconditions:**

- Phòng `R303` trạng thái `Vacant_Clean`.

**Test Steps:**

1. **Sub-test 1 (CCCD sai format):** Gọi API với `cccd = "INVALID_12"`.
   - Assert HTTP 400, mã lỗi `MOD2-UC14-003`, message: `"Invalid identification document"`.
2. **Sub-test 2 (CCCD null):** Gọi API với `cccd = null`.
   - Assert HTTP 400, mã lỗi `MOD2-UC14-001`, message chứa `"required"`.
3. **Sub-test 3 (fullName rỗng):** Gọi API với `fullName = ""`.
   - Assert HTTP 400, message chứa `"Full name is required"`.
4. **Sub-test 4 (dateOfBirth null):** Gọi API với `dateOfBirth = null`.
   - Assert HTTP 400, message chứa `"Date of birth is required"`.
5. Truy vấn DB: Không có Customer, Booking nào được tạo trong toàn bộ 4 sub-test.

**Expected Result (PASS):**

- Hệ thống từ chối với mã lỗi chính xác cho toàn bộ 4 sub-test.
- Không có side effect nào trong DB.

---

#### `TC-M2-024` — AF-01: Không có phòng trống → Walk-in bị chặn

**Severity:** 🟠 HIGH
**Feature Under Test:** UC-14 AF-01 (No Available Room)
**SRS Coverage:** BR-02 (Check-in only if room available)
**TDD Phase:** 🔴 RED
**Test File:** `WalkInCheckInServiceUC14Test.java`
**Test Data Classification:** SYNTHETIC

**Preconditions:**

- Tất cả phòng đều có trạng thái `OCCUPIED`, `DIRTY`, hoặc `MAINTENANCE`.
- Không có phòng nào `Vacant_Clean`.

**Test Steps:**

1. Gọi `GET /api/v1/reception/walk-in/rooms` → Assert response `availableRooms = []`, `totalAvailable = 0`.
2. Gọi `POST /api/v1/reception/walk-in` (không chỉ định `roomId`, để hệ thống tự tìm).
3. Assert HTTP 409, mã lỗi `MOD2-UC14-004`, message: `"No available rooms for the requested stay period"`.
4. Truy vấn DB: Không có Booking nào được tạo.

**Expected Result (PASS):**

- Hệ thống từ chối Walk-in khi không có phòng trống.
- Không có Booking nào được tạo.

---

#### `TC-M2-025` — E-03: Transaction Rollback khi lỗi giữa chừng trong Walk-in

**Severity:** 🔴 CRITICAL
**Feature Under Test:** UC-14 E-03, ADR-UC14-003
**SRS Coverage:** E-03 (Reservation Creation Failure — Transaction rolled back)
**TDD Phase:** 🔴 RED
**Test File:** `WalkInCheckInServiceUC14Test.java`
**Test Data Classification:** SYNTHETIC

**Preconditions:**

- Phòng `R305` trạng thái `Vacant_Clean`.
- Mock `folioService.initializeFolio()` để throw `RuntimeException` sau khi Booking đã được save().

**Test Steps:**

1. Gọi service walk-in check-in với data hợp lệ, roomId = R305.
2. Xác nhận `folioService.initializeFolio()` throw RuntimeException.
3. Assert HTTP 500, mã lỗi `MOD2-UC14-005`, message: `"Walk-in check-in failed. Transaction rolled back"`.
4. Truy vấn DB:
   - `SELECT COUNT(*) FROM room_bookings WHERE room_id = 305` → phải = 0.
   - `SELECT COUNT(*) FROM customers WHERE full_name = 'TC-M2-025-Guest'` → phải = 0 (nếu khách mới).
   - `SELECT room_status FROM rooms WHERE id = 305` → phải = `"Vacant_Clean"` (không đổi).

**Expected Result (PASS):**

- `@Transactional` rollback toàn bộ: không Booking, không Customer mới, phòng giữ `Vacant_Clean`.
- Đây là kiểm tra tính ACID của toàn bộ Walk-in flow.

**Expected Result (FAIL):**

- Booking được lưu partial mà Folio không được tạo — partial commit nghiêm trọng.

---

#### `TC-M2-026` — Concurrency: 2 Lễ tân Walk-in cùng chọn phòng → chỉ 1 thành công

**Severity:** 🔴 CRITICAL
**Feature Under Test:** UC-14 + ADR-UC14-002 (Pessimistic Locking)
**SRS Coverage:** BR-02 (one room — one active booking)
**TDD Phase:** 🔴 RED
**Test File:** `WalkInCheckInConcurrencyUC14Test.java`
**Test Data Classification:** SYNTHETIC

**Preconditions:**

- Phòng `R306` trạng thái `Vacant_Clean`.
- 2 thread sử dụng 2 CCCD khác nhau: `001111111111` (Thread A) và `002222222222` (Thread B).

**Test Steps:**

1. Tạo `CountDownLatch(1)` để 2 thread bắt đầu đồng thời.
2. Thread A: `POST /api/v1/reception/walk-in` với `roomId = 306`, `cccd = "001111111111"`.
3. Thread B: `POST /api/v1/reception/walk-in` với `roomId = 306`, `cccd = "002222222222"`.
4. Kích hoạt cả 2 thread cùng lúc, chờ hoàn thành.
5. Assert: Đúng 1 response có status `201 Created`.
6. Assert: Response còn lại có status `409 Conflict`, mã lỗi `MOD2-UC14-004` hoặc `MOD2-UC14-006`.
7. Truy vấn DB: `SELECT COUNT(*) FROM room_booking_details WHERE room_id = 306 AND detail_status = 'CHECKED_IN'` → phải = 1.
8. Truy vấn DB: `SELECT room_status FROM rooms WHERE id = 306` → `"OCCUPIED"`.

**Expected Result (PASS):**

- Pessimistic Lock hoạt động: chỉ 1 trong 2 walk-in thành công.
- Không có double booking.

**Expected Result (FAIL):**

- Cả 2 walk-in đều thành công → Double booking nghiêm trọng.

---

#### `TC-M2-028` — AF-02: Khách đã có Customer Profile → Reuse Profile, tạo Booking mới

**Severity:** 🟡 MEDIUM
**Feature Under Test:** UC-14 AF-02 (Existing Guest Record Found)
**SRS Coverage:** AF-02, BR-06 (PII security — không duplicate)
**TDD Phase:** 🔴 RED
**Test File:** `WalkInCheckInServiceUC14Test.java`
**Test Data Classification:** SYNTHETIC

**Preconditions:**

- Customer `customer_id = 99` đã tồn tại trong DB (CCCD `001200009999` đã được mã hóa và lưu).
- Phòng `R308` trạng thái `Vacant_Clean`.

**Test Steps:**

1. Gọi Walk-in API với `cccd = "001200009999"` (khách đã có profile trong DB).
2. Assert HTTP 201 — check-in thành công.
3. Truy vấn DB: `SELECT COUNT(*) FROM customers WHERE id = 99` → phải = 1 (không tạo Customer mới).
4. Truy vấn DB: `SELECT customer_id FROM room_bookings WHERE [bookingId]` → phải = 99 (reuse).
5. Truy vấn DB: Phòng R308 có `room_status = "OCCUPIED"`.

**Expected Result (PASS):**

- Hệ thống tái sử dụng Customer profile cũ.
- Không tạo Customer mới — không duplicate.
- Booking mới liên kết đúng `customer_id = 99`.

**Expected Result (FAIL):**

- Tạo Customer thứ 2 với cùng CCCD → Duplicate data, vi phạm BR-06.

---

#### `TC-M2-029` — AF-03: Thêm khách đi kèm trong lúc Walk-in Check-in

**Severity:** 🟡 MEDIUM
**Feature Under Test:** UC-14 AF-03 (Additional Guests Registered)
**SRS Coverage:** BR-07 (Temporary residence compliance — all guests)
**TDD Phase:** 🔴 RED
**Test File:** `WalkInCheckInServiceUC14Test.java`
**Test Data Classification:** SYNTHETIC

**Preconditions:**

- Phòng `R309` trạng thái `Vacant_Clean`, capacity = 3.

**Test Steps:**

1. Gọi Walk-in API với `accompaniedGuests`:
   ```json
   {
     "fullName": "Nguyen Van A",
     "cccd": "001234567892",
     "checkInDate": "2026-06-19",
     "checkOutDate": "2026-06-21",
     "numberOfGuests": 2,
     "roomId": 309,
     "accompaniedGuests": [
       { "fullName": "Nguyen Thi B", "dateOfBirth": "1995-03-20", "cccd": "001234567893" }
     ]
   }
   ```
2. Assert HTTP 201 — check-in thành công.
3. Truy vấn DB: Bảng `dependents` có 1 record liên kết `primary_customer_id` của khách chính.
4. Truy vấn DB: `dependents.dependent_name = "Nguyen Thi B"`.
5. Assert `accompaniedGuestCount = 1` trong response body.

**Expected Result (PASS):**

- Khách đi kèm được đăng ký đúng theo BR-07 (temporary residence compliance).
- Dependent record liên kết với Customer chính.

---

#### `TC-M2-030` — E-02: Phòng DIRTY / MAINTENANCE → Walk-in bị chặn

**Severity:** 🟠 HIGH
**Feature Under Test:** UC-14 Step 7-8, E-02 (Room Becomes Unavailable)
**SRS Coverage:** BR-02 (room must be available for check-in)
**TDD Phase:** 🔴 RED
**Test File:** `WalkInCheckInServiceUC14Test.java`
**Test Data Classification:** SYNTHETIC

**Preconditions:**

- Phòng `R310` trạng thái `DIRTY`.
- Phòng `R311` trạng thái `MAINTENANCE`.

**Test Steps:**

1. **Sub-test 1:** Gọi API với `roomId = 310` (DIRTY).
   - Assert HTTP 409, mã lỗi `MOD2-UC14-006`, message: `"Selected room is not available for check-in"`.
2. **Sub-test 2:** Gọi API với `roomId = 311` (MAINTENANCE).
   - Assert HTTP 409, mã lỗi `MOD2-UC14-006`, message giống sub-test 1.
3. Truy vấn DB: Không có Booking nào được tạo cho cả 2 sub-test.

**Expected Result (PASS):**

- Hệ thống chặn check-in vào phòng không Vacant_Clean theo ADR-UC14-002.

---

#### `TC-M2-031` — Normal Flow Step 11-13: Auto-Create Customer Account cho khách Walk-in mới

**Severity:** 🟠 HIGH
**Feature Under Test:** UC-14 Normal Flow Step 11 (System creates reservation), Step 12-13
**SRS Coverage:** BR-08 (System auto-generates account), BR-09 (Default password policy), BR-10 (Account linked to Reservation)
**TDD Phase:** 🔴 RED
**Test File:** `WalkInCheckInServiceUC14Test.java`
**Test Data Classification:** SYNTHETIC

**Preconditions:**

- Không có Customer nào có CCCD `001999888777` trong hệ thống.
- Phòng `R312` trạng thái `Vacant_Clean`.

**Test Steps:**

1. Gọi Walk-in API với `cccd = "001999888777"`, `phone = "0912345678"` (khách hoàn toàn mới).
2. Assert HTTP 201 — check-in thành công.
3. Truy vấn DB: `SELECT id FROM customers WHERE full_name = 'TC-M2-031-Guest'` → tồn tại 1 record (customer_id = X).
4. **[BR-08]** Truy vấn DB: `SELECT id FROM accounts WHERE customer_id = X` → tồn tại 1 account record.
5. **[BR-09]** Truy vấn DB: `accounts.password_hash` không null, không rỗng (default password đã được gán và hash).
6. **[BR-10]** Truy vấn DB: `room_bookings.customer_id = X` → Reservation liên kết đúng account mới.
7. Assert response `isNewCustomer = true`.

**Expected Result (PASS):**

- Account mới được tạo tự động (BR-08).
- Default password được gán và lưu dạng hash (BR-09) — không kiểm tra cipher algorithm.
- Reservation liên kết đúng Customer Account (BR-10).

**Expected Result (FAIL):**

- Account không được tạo sau walk-in.
- Reservation không liên kết account.
- `password_hash` null.

---

#### `TC-M2-032` — AF-02: Khách đã có Account → Không tạo Account mới, Reservation link Account cũ

**Severity:** 🟡 MEDIUM
**Feature Under Test:** UC-14 AF-02 (Existing Guest Record Found)
**SRS Coverage:** AF-02, BR-10 (Account linked to Reservation)
**TDD Phase:** 🔴 RED
**Test File:** `WalkInCheckInServiceUC14Test.java`
**Test Data Classification:** SYNTHETIC

**Preconditions:**

- Customer `customer_id = 150` đã tồn tại, có Account `account_id = 200` liên kết.
- CCCD của customer này đã được mã hóa và lưu trong DB.
- Phòng `R313` trạng thái `Vacant_Clean`.

**Test Steps:**

1. Gọi Walk-in API với CCCD khớp với customer_id = 150.
2. Assert HTTP 201 — check-in thành công.
3. **[No Duplicate Account]** Truy vấn DB: `SELECT COUNT(*) FROM accounts WHERE customer_id = 150` → phải = 1.
4. **[Reuse Account]** Truy vấn DB: `SELECT account_id FROM room_bookings WHERE [bookingId]` → phải = 200 (account cũ).
5. Truy vấn DB: Phòng R313 `OCCUPIED`.
6. Assert response `isNewCustomer = false`.

**Expected Result (PASS):**

- Không tạo Account mới (chỉ có 1 account cho customer_id = 150).
- Reservation liên kết đúng account_id = 200 (cũ).

**Expected Result (FAIL):**

- Tạo Account thứ 2 cho customer_id = 150 → Duplicate account.

---

#### `TC-M2-033` — Normal Flow Step 5: numberOfGuests vượt Room Capacity → Reject

**Severity:** 🟡 MEDIUM
**Feature Under Test:** UC-14 Normal Flow Step 5 (Enter stay details — Number of guests)
**SRS Coverage:** BR-02 (room availability includes capacity validation)
**TDD Phase:** 🔴 RED
**Test File:** `WalkInCheckInServiceUC14Test.java`
**Test Data Classification:** SYNTHETIC

**Preconditions:**

- Phòng `R314` trạng thái `Vacant_Clean`, `capacity = 2`.

**Test Steps:**

1. Gọi Walk-in API với `roomId = 314`, `numberOfGuests = 4` (vượt capacity = 2).
2. Assert HTTP 400, mã lỗi `MOD2-UC14-009`, message: `"Number of guests exceeds room capacity"`.
3. Truy vấn DB: Không có Booking nào được tạo cho R314.

**Expected Result (PASS):**

- Hệ thống reject với lỗi validation rõ ràng.
- Không có Reservation được tạo.

**Expected Result (FAIL):**

- Tạo Booking với số khách vượt capacity phòng.

---

#### `TC-M2-034` — BR-07: Temporary Residence Reporting Failure → Check-in vẫn thành công + Log lỗi

**Severity:** 🟡 MEDIUM
**Feature Under Test:** UC-14 Normal Flow Step 15 (System records temporary residence information)
**SRS Coverage:** BR-07 (All staying guests must be registered for temporary residence)
**TDD Phase:** 🔴 RED
**Test File:** `WalkInCheckInServiceUC14Test.java`
**Test Data Classification:** SYNTHETIC

> [!IMPORTANT]
> **Quyết định hành vi (ADR-UC14-004):** Temporary Residence Reporting là best-effort service (Option B).
> Check-in thành công KHÔNG phụ thuộc vào kết quả của reporting service.
> Khi fail → ghi error log + đưa vào retry queue. Không rollback booking.
>
> *Lý do:* Rollback toàn bộ booking chỉ vì failure của một external/optional reporting service
> gây UX nghiêm trọng cho khách và lễ tân. Audit trail đủ để xử lý thủ công sau.

**Preconditions:**

- Phòng `R315` trạng thái `Vacant_Clean`.
- Mock `residenceReportingService.registerTemporaryResidence()` để throw `ResidenceReportingException`.

**Test Steps:**

1. Gọi Walk-in API với data hợp lệ, `roomId = 315`.
2. `residenceReportingService.registerTemporaryResidence()` throw `ResidenceReportingException`.
3. **[Option B]** Assert HTTP `201 Created` — check-in vẫn thành công.
4. Truy vấn DB: Booking tồn tại với `booking_status = "CHECKED_IN"`.
5. Truy vấn DB: Phòng R315 có `room_status = "OCCUPIED"`.
6. Assert error log chứa `"[WARN] Temporary residence reporting failed for booking [bookingId]"`.
7. Assert retry queue nhận được 1 entry cho `bookingId` vừa tạo (nếu retry mechanism được implement).

**Expected Result (PASS):**

- Check-in thành công (Option B: non-blocking).
- Lỗi reporting được ghi log, không rollback.
- Retry queue có entry để xử lý lại.

**Expected Result (FAIL):**

- Check-in fail do reporting exception → UX không chấp nhận được (sẽ implement Option A nếu quyết định thay đổi thì update ADR-UC14-004).

---

### 5. Red-Green-Refactor Tracker

> **Format 3-phase:** Mỗi test case ghi đầy đủ trạng thái 🔴 RED, 🟢 GREEN, 🔵 REFACTOR kèm commit hash + ngày thực hiện.

| UC    | TC ID     | Mô tả ngắn                                                        | Test File                                 | 🔴 RED | 🔴 Commit | 🔴 Date | 🟢 GREEN | 🟢 Commit | 🟢 Date | 🔵 REFACTOR | 🔵 Commit | 🔵 Note |
| ----- | --------- | -------------------------------------------------------------------- | ----------------------------------------- | ------ | --------- | ------- | -------- | --------- | ------- | ----------- | --------- | ------- |
| UC-14 | TC-M2-021 | Walk-in thành công: Booking + OCCUPIED + Folio init                | `WalkInCheckInServiceUC14Test.java`     | [ ]    |           |         | [ ]      |           |         | [ ]         |           |         |
| UC-14 | TC-M2-023 | E-01: CCCD/Passport không hợp lệ → từ chối (4 sub-tests)       | `WalkInCheckInServiceUC14Test.java`     | [ ]    |           |         | [ ]      |           |         | [ ]         |           |         |
| UC-14 | TC-M2-024 | AF-01: Không có phòng trống → Walk-in bị chặn                 | `WalkInCheckInServiceUC14Test.java`     | [ ]    |           |         | [ ]      |           |         | [ ]         |           |         |
| UC-14 | TC-M2-025 | E-03: Transaction Rollback khi lỗi giữa chừng                     | `WalkInCheckInServiceUC14Test.java`     | [ ]    |           |         | [ ]      |           |         | [ ]         |           |         |
| UC-14 | TC-M2-026 | Concurrency: 2 Lễ tân chọn cùng phòng → 1 thành công, 1 lỗi | `WalkInCheckInConcurrencyUC14Test.java` | [ ]    |           |         | [ ]      |           |         | [ ]         |           |         |
| UC-14 | TC-M2-028 | AF-02: Khách đã có profile → Reuse profile, tạo Booking mới   | `WalkInCheckInServiceUC14Test.java`     | [ ]    |           |         | [ ]      |           |         | [ ]         |           |         |
| UC-14 | TC-M2-029 | AF-03: Thêm khách đi kèm trong Walk-in → Dependent record       | `WalkInCheckInServiceUC14Test.java`     | [ ]    |           |         | [ ]      |           |         | [ ]         |           |         |
| UC-14 | TC-M2-030 | E-02: Phòng DIRTY/MAINTENANCE → Walk-in bị chặn                  | `WalkInCheckInServiceUC14Test.java`     | [ ]    |           |         | [ ]      |           |         | [ ]         |           |         |
| UC-14 | TC-M2-031 | Auto-Create Customer Account cho khách Walk-in mới (BR-08/09/10)   | `WalkInCheckInServiceUC14Test.java`     | [ ]    |           |         | [ ]      |           |         | [ ]         |           |         |
| UC-14 | TC-M2-032 | AF-02: Khách đã có Account → không tạo mới, link Account cũ | `WalkInCheckInServiceUC14Test.java`     | [ ]    |           |         | [ ]      |           |         | [ ]         |           |         |
| UC-14 | TC-M2-033 | numberOfGuests > roomCapacity → reject validation                   | `WalkInCheckInServiceUC14Test.java`     | [ ]    |           |         | [ ]      |           |         | [ ]         |           |         |
| UC-14 | TC-M2-034 | BR-07: ResidenceReporting fail → check-in OK + log lỗi (Opt B)     | `WalkInCheckInServiceUC14Test.java`     | [ ]    |           |         | [ ]      |           |         | [ ]         |           |         |


---

### 6. Entry / Exit Criteria

#### Entry Criteria (Điều kiện bắt đầu)

- [ ] Môi trường test sẵn sàng (DB test, Testcontainers MySQL).
- [ ] `TDD_UC14_SPEC.md` và `EDS_UC14_SPEC.md` đã được Tech Lead review và approve.
- [ ] DPO đã sign-off (module xử lý PII: CCCD, Passport — TC-M2-028, TC-M2-029).
- [ ] ADR-UC14-004 (Option B cho Residence Reporting) đã được Team confirm trước khi viết TC-M2-034.
- [ ] Test data synthetic đã chuẩn bị: phòng R301 → R315, customer mẫu.

#### Exit Criteria — Definition of Done (DoD)

- [ ] Tất cả **12 test case** (TC-M2-021, 023-026, 028-034) phải ở trạng thái 🟢 GREEN.
- [ ] TC-M2-026 (Concurrency) PASS ổn định qua ít nhất **10 lần chạy liên tiếp** (flaky < 1%).
- [ ] TC-M2-025 (ACID Rollback) PASS bắt buộc trước khi merge.
- [ ] TC-M2-031 (Auto Account) + TC-M2-032 (Account Reuse) PASS đồng thời.
- [ ] `mvn test -Dtest="*UC14*"` chạy không có failure.
- [ ] Coverage cho `WalkInCheckInService` đạt >= **85% line coverage**.
- [ ] Không có TODO/FIXME còn lại trong production code.
- [ ] PII security được verify bởi `CustomerServicePIISecurityTest.java` (test suite riêng, chạy trong CI).

#### SRS Coverage Checklist cuối cùng

| SRS Item    | Covered By                                 | Status |
| ----------- | ------------------------------------------ | ------ |
| Normal Flow | TC-M2-021                                  | [ ]    |
| AF-01       | TC-M2-024                                  | [ ]    |
| AF-02       | TC-M2-028, TC-M2-032                       | [ ]    |
| AF-03       | TC-M2-029                                  | [ ]    |
| E-01        | TC-M2-023                                  | [ ]    |
| E-02        | TC-M2-024, TC-M2-030                       | [ ]    |
| E-03        | TC-M2-025                                  | [ ]    |
| BR-01       | TC-M2-023                                  | [ ]    |
| BR-02       | TC-M2-024, TC-M2-026, TC-M2-030, TC-M2-033 | [ ]    |
| BR-03       | TC-M2-021                                  | [ ]    |
| BR-04       | TC-M2-021                                  | [ ]    |
| BR-05       | TC-M2-021                                  | [ ]    |
| BR-06       | TC-M2-028 (no duplicate)                   | [ ]    |
| BR-07       | TC-M2-029, TC-M2-034                       | [ ]    |
| BR-08       | TC-M2-031                                  | [ ]    |
| BR-09       | TC-M2-031                                  | [ ]    |
| BR-10       | TC-M2-031, TC-M2-032                       | [ ]    |

---

### 7. Rollback Plan

Nếu phát hiện lỗi critical sau khi deploy:

```bash
# Bước 1: Revert về phiên bản ổn định
git checkout tags/v[previous-stable-tag]
mvn clean package -DskipTests
java -jar target/kawai-backend-1.0.jar

# Bước 2: Kiểm tra health
curl -X GET http://localhost:8080/actuator/health
# Expected: {"status":"UP"}

# Bước 3: Kiểm tra DB không có overbooking Walk-in
SELECT room_id, COUNT(*) as cnt
FROM room_booking_details
WHERE detail_status = 'CHECKED_IN'
GROUP BY room_id HAVING cnt > 1;
-- Expected: 0 rows (không có double booking)

# Bước 4: Kiểm tra partial commit
SELECT rb.id FROM room_bookings rb
LEFT JOIN folio_items fi ON fi.booking_id = rb.id
WHERE rb.booking_source = 'WALK_IN' AND fi.id IS NULL
AND rb.created_at >= DATE_SUB(NOW(), INTERVAL 1 HOUR);
-- Expected: 0 rows (không có Booking thiếu Folio)
```

**Notification Protocol:**

- **Ngay khi phát hiện:** Slack `#incident` → `"🚨 [UC14-WALKIN] Double booking / Partial commit detected"`
- **Trong 30 phút:** Email DPO nếu PII bị rò rỉ theo Nghị định 13/2023.

---

*Commit convention: `feat(mod2-uc14): implement walk-in check-in and pass TC-M2-021,023-026,028-034`*
*TDD Spec v1.1 — Module 2 — UC-14 Walk-in Guest Check-in*
