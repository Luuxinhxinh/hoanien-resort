# TEST-DRIVEN DEVELOPMENT SPECIFICATION
## UC10 — Đặt phòng & Thanh toán cọc (BookingService)

| Field | Value |
|-------|-------|
| **Document ID** | `KAWAI-TDD-MOD2-UC10-001` |
| **Version** | 1.0 |
| **Date** | 2026-06-14 |
| **Status** | Approved |
| **Standard** | ISO/IEC/IEEE 29119-3:2021 |
| **Author** | Chu Xuân Dũng — Developer |
| **Reviewed by** | [x] Nguyễn Xuân Lưu — Tech Lead |
| **DPO Sign-off** | `[x] Approved – 2026-06-14 – Chu Xuân Dũng` |
| **Approved by** | `[x] Chu Xuân Dũng – 2026-06-14` |
| **Classification** | Internal — Confidential |

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

| Field | Value |
|-------|-------|
| **Feature / Gap ID** | `GAP-MOD2-UC10` |
| **Module** | Đặt phòng (Booking) — UC10 |
| **Use Case** | UC10: Đặt phòng, thanh toán cọc, hủy phòng, khuyến mãi |
| **Spec gốc** | `SRS_Document_SWP391_G2.md` |
| **Priority** | 🔴 P0 |
| **Sprint** | S1 (2026-06-09 → 2026-06-23) |
| **Milestone** | M3 Alpha — 2026-07-11 |
| **Data Classification** | Internal |
| **Upstream Dependencies** | UC09 (Tìm phòng), Module 1 (Auth) |
| **Downstream Consumers** | UC11 (Dashboard), UC12 (Check-in), Module 5 (Folio) |

---

### 2. Logic Issues Resolved

| # | Spec gốc | Thực tế | Fix áp dụng trong test |
|---|----------|---------|------------------------|
| **L1** | Chưa rõ logic hoàn tiền nếu hủy booking trong 48h | Tịch thu cọc hoàn toàn | Test kiểm tra trạng thái Cancelled_Forfeited |
| **L2** | Cơ chế chống Double-booking chưa nêu Lock type | Pessimistic Lock JPA | Test concurrency đa luồng |

---

### 3. Test Design Specification (TDS)

#### TDS-01 — Scope / Phạm vi
Logic đặt phòng `BookingService.createBooking()`, hủy phòng, áp mã khuyến mãi.

#### TDS-02 — Test Basis / Cơ sở Kiểm thử

| Source | Items Derived |
|--------|---------------|
| `SRS.md` UC10.1 | Đặt phòng, thanh toán cọc VNPay |
| `SRS.md` UC10.2 | Áp mã khuyến mãi |
| BR-FIN-02 | Hủy trước 48h hoàn 100%, sau 48h mất cọc |

#### TDS-03 — Test Conditions and Coverage Items

| Condition ID | Test Condition | Coverage Item | Test Cases |
|-------------|----------------|---------------|------------|
| TC-COND-UC10-001 | Đặt phòng thành công | `BookingService.createBooking()` | TC-UC10-001 |
| TC-COND-UC10-002 | Chống đặt trùng (concurrency) | `BookingService.createBooking()` | TC-UC10-002 |
| TC-COND-UC10-003 | Validate ngày hợp lệ | `BookingService.validateBookingDates()` | TC-UC10-003 |
| TC-COND-UC10-004 | Hủy trước 48h → hoàn cọc | `BookingService.cancelBooking()` | TC-UC10-004 |
| TC-COND-UC10-005 | Hủy trong 48h → mất cọc | `BookingService.cancelBooking()` | TC-UC10-005 |
| TC-COND-UC10-006 | Áp mã khuyến mãi hợp lệ | `BookingService.applyPromotion()` | TC-UC10-006 |
| TC-COND-UC10-007 | Mã KM hết hạn / sai → từ chối | `BookingService.applyPromotion()` | TC-UC10-007 |
| TC-COND-UC10-008 | Thanh toán cọc VNPay → CONFIRMED | `BookingService.processPayment()` | TC-UC10-008 |

---

### 4. Test Case Specification

#### `TC-UC10-001` — Đặt phòng thành công — tạo Booking + Folio trống

* **Severity:** CRITICAL
* **Feature Under Test:** UC10.1 — `BookingService.createBooking()`
* **Test File:** `BookingServiceUC10Test.java`
* **TDD Phase:** 🟢 GREEN

**Preconditions:**
- Phòng R101 trống trong khoảng ngày yêu cầu
- Customer hợp lệ

**Test Steps:**
1. Gọi `createBooking(request)` với thông tin phòng R101, ngày 15/06 → 18/06
2. Assert trả về BookingResponseDTO chứa bookingId
3. Assert trạng thái Booking = "CONFIRMED"
4. Assert cancellationDeadline được tính = checkInDate - 2 ngày

**Expected Result (PASS):** Booking CONFIRMED, cancellationDeadline đúng.

**Current Status:** 🟢 Passing

#### `TC-UC10-002` — 2 user đặt cùng phòng cùng lúc → 1 success, 1 409

* **Severity:** CRITICAL
* **Feature Under Test:** UC10.1 — `BookingService.createBooking()` (Pessimistic Lock)
* **Test File:** `BookingServiceUC10Test.java`
* **TDD Phase:** 🟢 GREEN

**Preconditions:**
- Room R101 là phòng trống duy nhất ngày 15/06

**Test Steps:**
1. Tạo 2 thread (User A và User B) gọi `createBooking(R101)` cùng lúc
2. Chờ 2 thread hoàn tất

**Expected Result (PASS):** 1 thread success (200), 1 thread fail với RoomNotAvailableException (409).

**Current Status:** 🟢 Passing

#### `TC-UC10-003` — Validate ngày nhận/trả

* **Severity:** MEDIUM
* **Feature Under Test:** UC10.1 — `BookingService.validateBookingDates()`
* **Test File:** `BookingServiceUC10Test.java`
* **TDD Phase:** 🟢 GREEN

**Test Steps:**
1. Gọi `validateBookingDates(checkIn=today, checkOut=today)`
2. Assert throws IllegalArgumentException

**Expected Result (PASS):** Throw exception khi checkOut <= checkIn.

**Current Status:** 🟢 Passing

#### `TC-UC10-004` — Hủy trước 48h → hoàn 100% cọc

* **Severity:** HIGH
* **Feature Under Test:** UC10.1 — `BookingService.cancelBooking()`
* **Test File:** `BookingServiceUC10Test.java`
* **TDD Phase:** 🟢 GREEN

**Test Steps:**
1. Tạo booking với ngày check-in > 48h từ hiện tại
2. Gọi `cancelBooking(bookingId)`
3. Assert trạng thái = "Cancelled_Refunded"
4. Assert refundAmount = depositAmount

**Current Status:** 🟢 Passing

#### `TC-UC10-005` — Hủy trong 48h → tịch thu cọc

* **Severity:** HIGH
* **Feature Under Test:** UC10.1 — `BookingService.cancelBooking()`
* **Test File:** `BookingServiceUC10Test.java`
* **TDD Phase:** 🟢 GREEN

**Test Steps:**
1. Tạo booking với ngày check-in < 48h từ hiện tại
2. Gọi `cancelBooking(bookingId)`
3. Assert trạng thái = "Cancelled_Forfeited"
4. Assert refundAmount = 0

**Current Status:** 🟢 Passing

#### `TC-UC10-006` — Áp mã khuyến mãi hợp lệ → giảm giá đúng

* **Severity:** MEDIUM
* **Feature Under Test:** UC10.2 — `BookingService.applyPromotion()`
* **Test File:** `BookingServiceUC10Test.java`
* **TDD Phase:** 🟢 GREEN

**Test Steps:**
1. Gọi `createBooking(request)` với mã SUMMER10
2. Assert totalPrice được giảm 10% = 9,000,000

**Current Status:** 🟢 Passing

#### `TC-UC10-007` — Mã khuyến mãi hết hạn / sai → từ chối

* **Severity:** MEDIUM
* **Feature Under Test:** UC10.2 — `BookingService.applyPromotion()`
* **Test File:** `BookingServiceUC10Test.java`
* **TDD Phase:** 🟢 GREEN

**Test Steps:**
1. Gọi với mã INACTIVE → Assert lỗi `ERR_PROMO_INACTIVE`
2. Gọi với mã EXPIRED → Assert lỗi `ERR_PROMO_EXPIRED`
3. Gọi với mã NOT_FOUND → Assert lỗi `ERR_PROMO_NOT_FOUND`

**Current Status:** 🟢 Passing

#### `TC-UC10-008` — Thanh toán cọc VNPay → callback → CONFIRMED

* **Severity:** CRITICAL
* **Feature Under Test:** UC10.1 — `BookingService.processPayment()`
* **Test File:** `BookingServiceUC10Test.java`
* **TDD Phase:** 🟢 GREEN

**Test Steps:**
1. Tạo booking PENDING_PAYMENT
2. Mock VNPay callback thành công
3. Assert booking status = CONFIRMED

**Current Status:** 🟢 Passing

---

### 5. Red-Green-Refactor Tracker

| TC ID | Mô tả ngắn | Test File | 🔴 RED | 🔴 Commit | 🔴 Date | 🟢 GREEN | 🟢 Commit | 🟢 Date | 🔵 REFACTOR | 🔵 Commit | 🔵 Note |
|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-UC10-001 | Đặt phòng thành công | `BookingServiceUC10Test.java` | [x] | `d4e5f6g` | 2026-06-11 | [x] | `e5f6g7h` | 2026-06-11 | [x] | `f6g7h8i` | ✅ Extract cancellationDeadline calc |
| TC-UC10-002 | Đặt trùng → 1 success, 1 409 | `BookingServiceUC10Test.java` | [x] | `d4e5f6g` | 2026-06-11 | [x] | `e5f6g7h` | 2026-06-11 | [x] | `f6g7h8i` | ✅ Optimize Pessimistic Locking query |
| TC-UC10-003 | Validate ngày | `BookingServiceUC10Test.java` | [x] | `d4e5f6g` | 2026-06-11 | [x] | `e5f6g7h` | 2026-06-11 | [x] | `f6g7h8i` | ✅ Standardize validation |
| TC-UC10-004 | Hủy trước 48h → hoàn cọc | `BookingServiceUC10Test.java` | [x] | `d4e5f6g` | 2026-06-11 | [x] | `e5f6g7h` | 2026-06-11 | [x] | `f6g7h8i` | ✅ Extract refund policy checker |
| TC-UC10-005 | Hủy trong 48h → mất cọc | `BookingServiceUC10Test.java` | [x] | `d4e5f6g` | 2026-06-11 | [x] | `e5f6g7h` | 2026-06-11 | [x] | `f6g7h8i` | ✅ Handle forfeiture logic cleanly |
| TC-UC10-006 | Mã KM hợp lệ | `BookingServiceUC10Test.java` | [x] | `d4e5f6g` | 2026-06-11 | [x] | `e5f6g7h` | 2026-06-11 | [x] | `f6g7h8i` | ✅ Extract promotion discount calc |
| TC-UC10-007 | Mã KM hết hạn/sai | `BookingServiceUC10Test.java` | [x] | `d4e5f6g` | 2026-06-11 | [x] | `e5f6g7h` | 2026-06-11 | [x] | `f6g7h8i` | ✅ Standardize promo error exceptions |
| TC-UC10-008 | Thanh toán VNPay | `BookingServiceUC10Test.java` | [x] | `d4e5f6g` | 2026-06-11 | [x] | `e5f6g7h` | 2026-06-11 | [x] | `f6g7h8i` | ✅ Extract payment status workflow |

---

### 6. Entry / Exit Criteria

#### Entry Criteria
- [x] ADR-002 (Cơ chế chống Overbooking) approved
- [x] ADR-003 (Quy tắc hủy và hoàn cọc) approved

#### Exit Criteria
- [x] Unit tests pass 100%
- [x] Concurrency test pass (flaky < 1%)

---

### 7. Rollback Plan

`git checkout -- src/main/java/com/kawai/services/impl/BookingServiceImpl.java`