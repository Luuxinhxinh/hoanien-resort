# TEST-DRIVEN DEVELOPMENT SPECIFICATION TEMPLATE
## Mẫu Đặc tả Kiểm thử Hướng Phát triển

| Field | Value |
| --- | --- |
| **Document ID** | `KAWAI-TDD-MOD2-001` |
| **Version** | 1.0 |
| **Date** | 2026-06-12 |
| **Status** | Approved |
| **Standard** | ISO/IEC/IEEE 29119-3:2021 |
| **Author** | Nguyễn Xuân Lưu - Tech Lead |

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
| --- | --- |
| **Feature / Gap ID** | `GAP-MOD2` |
| **Module** | Đặt phòng (Booking) |
| **Priority** | 🔴 P0 |
| **Milestone** | M3 Alpha — 2026-07-11 |
| **Data Classification** | Internal |

---

### 2. Logic Issues Resolved
| # | Spec gốc | Thực tế | Fix áp dụng trong test |
| --- | --- | --- | --- |
| **L1** | Không xử lý Lock | Dùng Pessimistic Lock | Bổ sung test concurrency (đa luồng) |

---

### 3. Test Design Specification (TDS)
#### TDS-01 — Scope / Phạm vi
Logic Đặt phòng, Folio, Housekeeping.

#### TDS-02 — Test Techniques
- Stress Test đa luồng chống Double Booking.
- State Transition: CONFIRMED -> CHECKED_IN.

---

### 4. Test Case Specification

#### `TC-M2-001` — Tìm phòng trống đúng theo ngày nhận/trả
*   **Severity:** HIGH
*   **Feature Under Test:** UC09
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M2-002` — Không có phòng trống → trả danh sách rỗng
*   **Severity:** MEDIUM
*   **Feature Under Test:** UC09
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M2-003` — Đặt phòng thành công — tạo Booking + Folio trống
*   **Severity:** CRITICAL
*   **Feature Under Test:** UC10.1
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M2-004` — 2 user đặt cùng phòng cùng lúc → 1 thành công, 1 trả 409 (Pessimistic Lock)
*   **Severity:** CRITICAL
*   **Feature Under Test:** UC10.1
*   **Test Type:** Concurrency
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M2-005` — Thanh toán cọc VNPay → callback xác nhận → trạng thái Booking = CONFIRMED
*   **Severity:** CRITICAL
*   **Feature Under Test:** UC10.1
*   **Test Type:** Integration
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M2-006` — Hủy trước 48h → hoàn 100% cọc (BR-FIN-02)
*   **Severity:** HIGH
*   **Feature Under Test:** UC10.1
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M2-007` — Hủy trong 48h → tịch thu cọc (BR-FIN-02)
*   **Severity:** HIGH
*   **Feature Under Test:** UC10.1
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M2-008` — Áp mã khuyến mãi hợp lệ → giảm giá đúng
*   **Severity:** MEDIUM
*   **Feature Under Test:** UC10.2
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M2-009` — Mã khuyến mãi hết hạn / sai → từ chối
*   **Severity:** MEDIUM
*   **Feature Under Test:** UC10.2
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M2-010` — Dashboard trả đúng danh sách phòng + trạng thái thời gian thực
*   **Severity:** HIGH
*   **Feature Under Test:** UC11
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M2-011` — Check-in thành công — phòng chuyển OCCUPIED, tạo Folio
*   **Severity:** CRITICAL
*   **Feature Under Test:** UC12.1
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M2-012` — Check-in thất bại — phòng đang DIRTY hoặc MAINTENANCE → báo lỗi
*   **Severity:** HIGH
*   **Feature Under Test:** UC12.1
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M2-013` — Ủy quyền hạn mức — cập nhật Credit Limit thành công
*   **Severity:** MEDIUM
*   **Feature Under Test:** UC12.2
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M2-014` — Đổi phòng — chuyển Folio sang phòng mới, phòng cũ → DIRTY
*   **Severity:** HIGH
*   **Feature Under Test:** UC12.3
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M2-015` — Nâng cấp Dependent thành Customer — tạo Account mới
*   **Severity:** LOW
*   **Feature Under Test:** UC12.4
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M2-016` — Check-out → tự động sinh yêu cầu dọn phòng (Trigger DB)
*   **Severity:** HIGH
*   **Feature Under Test:** UC13.1
*   **Test Type:** Integration
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M2-017` — Housekeeping cập nhật phòng DIRTY → CLEAN
*   **Severity:** HIGH
*   **Feature Under Test:** UC13.2
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M2-018` — Lễ tân xem danh sách yêu cầu dọn/sửa phòng
*   **Severity:** MEDIUM
*   **Feature Under Test:** UC13.3
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M2-019` — Housekeeping tạo phiếu sửa chữa → phòng chuyển MAINTENANCE
*   **Severity:** MEDIUM
*   **Feature Under Test:** UC13.4
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M2-020` — Maintenance hoàn thành → phòng chuyển AVAILABLE
*   **Severity:** MEDIUM
*   **Feature Under Test:** UC13.5
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.


### 5. Red-Green-Refactor Tracker

| TC ID | Mô tả ngắn | Test File | 🔴 RED | 🟢 GREEN (commit) | 🔵 REFACTOR note |
| --- | --- | --- | --- | --- | --- |
| TC-M2-001 | Tìm phòng trống đúng theo ngày nhận/trả | `RoomServiceUC09Test.java` | [x] | [x] | ✅ Extract available room validation logic |
| TC-M2-002 | Không có phòng trống → trả danh sách rỗng | `RoomServiceUC09Test.java` | [x] | [x] | ✅ Handle empty list gracefully |
| TC-M2-003 | Đặt phòng thành công — tạo Booking + Folio trống | `BookingServiceUC10Test.java` | [x] | [x] | ✅ Extract cancellationDeadline calc, add JavaDoc |
| TC-M2-004 | 2 user đặt cùng phòng cùng lúc → 1 thành công, 1 trả 409 (Pessimistic Lock) | `BookingServiceUC10Test.java` | [x] | [x] | ✅ Optimize Pessimistic Locking query |
| TC-M2-005 | Thanh toán cọc VNPay → callback xác nhận → trạng thái Booking = CONFIRMED | `BookingServiceUC10Test.java` | [x] | [x] | ✅ Extract payment status updating workflow |
| TC-M2-006 | Hủy trước 48h → hoàn 100% cọc (BR-FIN-02) | `BookingServiceUC10Test.java` | [x] | [x] | ✅ Extract refund policy checker |
| TC-M2-007 | Hủy trong 48h → tịch thu cọc (BR-FIN-02) | `BookingServiceUC10Test.java` | [x] | [x] | ✅ Handle forfeiture logic cleanly |
| TC-M2-008 | Áp mã khuyến mãi hợp lệ → giảm giá đúng | `BookingServiceUC10Test.java` | [x] | [x] | ✅ Extract promotion discount calculator |
| TC-M2-009 | Mã khuyến mãi hết hạn / sai → từ chối | `BookingServiceUC10Test.java` | [x] | [x] | ✅ Standardize promo error exceptions |
| TC-M2-010 | Dashboard trả đúng danh sách phòng + trạng thái thời gian thực | `RoomServiceUC11Test.java` | [x] | [x] | ✅ Extract toDashboardDTO helper method, use Java Streams, add JavaDoc |
| TC-M2-011 | Check-in thành công — phòng chuyển OCCUPIED, tạo Folio | `CheckinServiceUC12Test.java` | [x] | [x] | ✅ Extract checkIn validation and room allocation |
| TC-M2-012 | Check-in thất bại — phòng đang DIRTY hoặc MAINTENANCE → báo lỗi | `CheckinServiceUC12Test.java` | [x] | [x] | ✅ Extract illegal room status checks |
| TC-M2-013 | Ủy quyền hạn mức — cập nhật Credit Limit thành công | `CheckinServiceUC12Test.java` | [x] | [x] | ✅ Validate credit limit updates |
| TC-M2-014 | Đổi phòng — chuyển Folio sang phòng mới, phòng cũ → DIRTY | `CheckinServiceUC12Test.java` | [x] | [x] | ✅ Room transfer: extract old/new status flow |
| TC-M2-015 | Nâng cấp Dependent thành Customer — tạo Account mới | `CheckinServiceUC12Test.java` | [x] | [x] | ✅ Upgrade Dependent: extract account generator |
| TC-M2-016 | Check-out → tự động sinh yêu cầu dọn phòng (Trigger DB) | `HousekeepingServiceUC13Test.java` | [x] | [x] | ✅ Optimize checkout cleaning trigger |
| TC-M2-017 | Housekeeping cập nhật phòng DIRTY → CLEAN | `HousekeepingServiceUC13Test.java` | [x] | [x] | ✅ Clean status conversion logic |
| TC-M2-018 | Lễ tân xem danh sách yêu cầu dọn/sửa phòng | `HousekeepingServiceUC13Test.java` | [x] | [x] | ✅ Use Java Streams to filter pending operations |
| TC-M2-019 | Housekeeping tạo phiếu sửa chữa → phòng chuyển MAINTENANCE | `HousekeepingServiceUC13Test.java` | [x] | [x] | ✅ Standardize maintenance creation logs |
| TC-M2-020 | Maintenance hoàn thành → phòng chuyển AVAILABLE | `HousekeepingServiceUC13Test.java` | [x] | [x] | ✅ Complete maintenance: restore availability cleanly |

### 6. Entry / Exit Criteria
- [x] Đạt 100% pass rate cho kịch bản lock DB.

---

### 7. Rollback Plan
Rollback service version và reset state của Room R102.
