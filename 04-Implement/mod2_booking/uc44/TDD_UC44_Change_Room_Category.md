# TEST-DRIVEN DEVELOPMENT SPECIFICATION — UC44: CHANGE ROOM CATEGORY

## Mẫu Đặc tả Kiểm thử Hướng Phát triển — KAWAI RETREAT RESORT & HUB

| Field                    | Value                                                |
| ------------------------ | ---------------------------------------------------- |
| **Document ID**    | `KAWAI-TDD-UC44-001`                               |
| **Version**        | 1.0                                                  |
| **Date**           | 2026-06-29                                           |
| **Status**         | Approved                                             |
| **Standard**       | ISO/IEC/IEEE 29119-3:2021 — Software Testing Part 3 |
| **Author**         | Chu Xuân Dũng                                      |
| **Reviewed by**    | Nguyễn Xuân Lưu                                   |
| **Approved by**    | [ ] Pending                                          |
| **Classification** | Internal — Confidential                             |

**References:**

* `06-Testing/mod2_booking/SRS_Document_SWP391_G2.docx.md` — UC44 Change Room Category (trang 778–794)
* `06-Testing/MASTER_TDD_SPEC.md` — Master TDD Spec
* `06-Testing/mod2_booking/TDD_MOD2_SPEC.md` — TDD Module 2
* `05-Development/kawai-backend` — Backend Spring Boot

> **Quy ước TDD:** Tài liệu này mô tả test cases TRƯỚC khi viết production code.
> Thứ tự bắt buộc: viết test (.java) → chạy → xác nhận FAIL 🔴 → implement → PASS 🟢 → refactor 🔵.

---

## CHANGELOG

| Ngày       | Người thực hiện | Nội dung thay đổi                                                              |
| ---------- | --------------- | ------------------------------------------------------------------------------- |
| 2026-06-29 | Antigravity AI  | Refactor mã nguồn Clean Code & Cập nhật kết quả Refactor cho Tracker           |
| 2026-06-29 | Antigravity AI  | Code logic cho `ChangeRoomCategoryService` PASS 100% test (GREEN 🟢)          |
| 2026-06-29 | Antigravity AI  | Khởi tạo Test Cases TC-UC44-001 → TC-UC44-010 cho UC-44 — Giai đoạn RED 🔴   |
| 2026-06-29 | Chu Xuân Dũng   | Khởi tạo TDD Spec cho UC44 — Change Room Category                              |

---

## MỤC LỤC

1. [Thông tin Module](#1-thong-tin-module)
2. [Logic Issues Resolved](#2-logic-issues-resolved)
3. [Test Design Specification (TDS)](#3-test-design-specification-tds)
4. [Test Case Specification](#4-test-case-specification)
5. [Red-Green-Refactor Tracker](#5-red-green-refactor-tracker)
6. [Entry / Exit Criteria](#6-entry--exit-criteria)
7. [Rollback Plan](#7-rollback-plan)

---

## 1. Thông tin Module

| Field                         | Value                                                              |
| ----------------------------- | ------------------------------------------------------------------ |
| **Feature / Gap ID**    | `GAP-MOD2-UC44`                                                  |
| **Use Case**            | UC-44 — Change Room Category (Đổi hạng phòng trong lưu trú) |
| **Module**              | Module 2 — Đặt phòng & Tiền sảnh (Booking & Front Desk)      |
| **Priority**            | 🔴 P0 — Critical                                                  |
| **Sprint**              | S3 (2026-07-01 → 2026-07-15)                                      |
| **Milestone**           | M3 Alpha — 2026-07-11                                             |
| **Data Classification** | Internal / Financial                                               |
| **Compliance Scope**    | BR-FO-10, BR-FO-11, BR-FO-12, BR-FO-13, BR-FIN-08, BR-FIN-09       |

---

## 2. Logic Issues Resolved

| #  | Spec gốc (sai / thiếu)                                                                       | Thực tế (schema / policy)                                                             | Fix áp dụng trong test                                                                     |
| -- | ---------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------- |
| L1 | Chưa rõ khi nào hệ thống tính phụ phí chênh lệch hạng phòng                        | Chỉ tính phụ phí khi upgrade (hạng cao hơn); downgrade không được hoàn tiền | Test phân biệt rõ upgrade (có phụ phí) và downgrade (không hoàn tiền — BR-FIN-09) |
| L2 | Chưa nêu cơ chế lock phòng tạm khi lễ tân chọn nhưng chưa guest xác nhận          | Hệ thống cần tạm giữ phòng trong quá trình xử lý để tránh race condition   | Test kiểm tra trường hợp phòng bị lấy mất trước khi xác nhận (EX1)               |
| L3 | Chưa chỉ định rõ phòng cũ chuyển sang trạng thái gì sau khi đổi                   | Phòng cũ →`Vacant_Dirty`; phòng mới → `Occupied` (BR-FO-04)                   | Assert đúng 2 trạng thái phòng sau mỗi ca đổi phòng                                 |
| L4 | Chưa rõ Audit Log có bắt buộc ghi hay không                                              | Mọi thao tác đổi hạng phòng phải ghi Audit Log (BR-SYS-04)                       | Assert bản ghi Audit Log được tạo sau mỗi ca thành công và thất bại có nghĩa    |
| L5 | Trường hợp hạng phòng mới bằng giá hạng phòng cũ chưa được xử lý tường minh | Theo AF3: không có chênh lệch chi phí, quy trình tiếp tục bình thường        | Test riêng AF3: same-rate category change — không tạo FolioItem phụ phí                |

---

## 3. Test Design Specification (TDS)

### TDS-01 — Scope / Phạm vi

```
Feature: Change Room Category (UC-44)
├── Service Layer: ChangeRoomCategoryService.changeCategory()
├── Repository:
│   ├── RoomRepository (kiểm tra Vacant_Clean status)
│   ├── RoomBookingDetailRepository (cập nhật room assignment)
│   ├── FolioItemRepository (ghi phụ phí/credit)
│   └── AuditLogRepository (ghi nhật ký thao tác)
└── Integration:
    ├── Room Status State Machine (BR-FO-04)
    └── Room Rate Calculator (Dynamic Pricing)
```

### TDS-02 — Test Basis / Cơ sở Kiểm thử

| Source                        | Items Derived                                                                                             |
| ----------------------------- | --------------------------------------------------------------------------------------------------------- |
| SRS UC44 — Normal Flow       | 13 bước chính: tìm đặt phòng → chọn hạng → kiểm tra → tính giá → xác nhận → cập nhật |
| SRS UC44 — Alternative Flows | AF1 (không còn phòng), AF2 (khách từ chối), AF3 (cùng giá)                                        |
| SRS UC44 — Exceptions        | EX1 (phòng bị lấy mất), EX2 (lỗi DB), EX3 (không còn In-House), EX4 (lỗi giá)                    |
| Business Rules                | BR-FO-10, BR-FO-11, BR-FO-12, BR-FO-13, BR-FIN-08, BR-FIN-09, BR-SYS-04                                   |

### TDS-03 — Test Conditions and Coverage Items

| Condition ID    | Test Condition                                                      | Coverage Item                                  | Test Cases               |
| --------------- | ------------------------------------------------------------------- | ---------------------------------------------- | ------------------------ |
| TC-COND-UC44-01 | Đổi phòng thành công — upgrade có phụ phí                  | `ChangeRoomCategoryService.changeCategory()` | TC-UC44-001              |
| TC-COND-UC44-02 | Đổi phòng thành công — downgrade không hoàn tiền           | `FolioItemRepository`, `BR-FIN-09`         | TC-UC44-002              |
| TC-COND-UC44-03 | Không còn phòng trống ở hạng yêu cầu (AF1)                  | Room availability check                        | TC-UC44-003              |
| TC-COND-UC44-04 | Khách từ chối sau khi xem chênh lệch giá (AF2)                | Rollback / no-op logic                         | TC-UC44-004              |
| TC-COND-UC44-05 | Đổi sang hạng phòng cùng giá (AF3)                            | Rate comparison logic                          | TC-UC44-005              |
| TC-COND-UC44-06 | Phòng bị lấy mất trước khi xác nhận (EX1 — Race Condition) | Concurrent access / lock mechanism             | TC-UC44-006              |
| TC-COND-UC44-07 | Đặt phòng không ở trạng thái In-House (EX3)                  | Booking status validation                      | TC-UC44-007              |
| TC-COND-UC44-08 | Lỗi DB khi cập nhật (EX2)                                        | Transaction rollback                           | TC-UC44-008              |
| TC-COND-UC44-09 | Không tính được giá phòng (EX4)                              | Room rate error handling                       | TC-UC44-009              |
| TC-COND-UC44-10 | Audit Log ghi đầy đủ mọi ca đổi phòng                       | `AuditLogRepository`                         | TC-UC44-010              |
| TC-COND-UC44-11 | Trạng thái phòng cũ → Vacant_Dirty, phòng mới → Occupied    | Room Status State Machine                      | TC-UC44-001, TC-UC44-002 |

---

## 4. Test Case Specification

### TC-UC44-001 — Đổi phòng thành công (Upgrade — có phụ phí)

**Severity:** CRITICAL
**CWE:** N/A
**Feature Under Test:** `ChangeRoomCategoryService.changeCategory()`, `FolioItemRepository`
**Test File:** `src/test/java/com/kawai/services/ChangeRoomCategoryServiceUC44Test.java`
**TDD Phase:** 🟢 GREEN

**Preconditions:**

* Booking `BK-2001` đang ở trạng thái `IN_HOUSE`, phòng hiện tại `R201` (hạng Deluxe, giá 2.000.000 VND/đêm).
* Phòng `R301` (hạng Suite, giá 3.500.000 VND/đêm) có trạng thái `Vacant_Clean`.
* Ngày check-out: còn 3 đêm.
* Lễ tân đã được xác thực với `ROLE_RECEPTIONIST`.

**Test Steps:**

1. Mock `RoomBookingDetailRepository.findByBookingId("BK-2001")` trả về detail hiện tại với phòng `R201`.
2. Mock `RoomRepository.findAvailableRoomsByCategory("Suite")` trả về danh sách có `R301` (status = `VACANT_CLEAN`).
3. Mock `DynamicPricingService.calculateNightlyRate("R301", currentDate)` trả về `3_500_000L`.
4. Mock `DynamicPricingService.calculateNightlyRate("R201", currentDate)` trả về `2_000_000L`.
5. Gọi `changeCategory(bookingDetailId, "Suite", receptionistId)`.
6. Assert phòng `R201` được cập nhật status → `VACANT_DIRTY`.
7. Assert phòng `R301` được cập nhật status → `OCCUPIED`.
8. Assert `RoomBookingDetail.room_id` được cập nhật thành `R301`.
9. Assert `FolioItem` mới được tạo với `amount = (3_500_000 - 2_000_000) * 3 = 4_500_000 VND`.
10. Assert `AuditLog` được ghi với action = `ROOM_CATEGORY_CHANGED`, actor = receptionistId.
11. Assert response trả về `200 OK` kèm chi tiết phòng mới và folio adjustment.

**Expected Result (PASS):**

* Phòng cũ → `VACANT_DIRTY`, phòng mới → `OCCUPIED`.
* FolioItem phụ phí được tạo đúng = chênh lệch đơn giá × số đêm còn lại.
* Audit Log ghi đầy đủ.
* Booking ID và Reservation ID giữ nguyên (không tạo booking mới).

**Expected Result (FAIL):**

* Tạo thêm booking ID mới (sai — phải giữ nguyên).
* Không tạo FolioItem khi upgrade.
* Phòng cũ không chuyển sang `VACANT_DIRTY`.

---

### TC-UC44-002 — Đổi phòng thành công (Downgrade — không hoàn tiền)

**Severity:** HIGH
**Feature Under Test:** `ChangeRoomCategoryService.changeCategory()`, `BR-FIN-09`
**Test File:** `src/test/java/com/kawai/services/ChangeRoomCategoryServiceUC44Test.java`
**TDD Phase:** 🟢 GREEN

**Preconditions:**

* Booking `BK-2002` đang `IN_HOUSE`, phòng `R401` (hạng Suite, 3.500.000 VND/đêm).
* Phòng `R202` (hạng Deluxe, 2.000.000 VND/đêm) có trạng thái `Vacant_Clean`.
* Còn 2 đêm.

**Test Steps:**

1. Gọi `changeCategory(bookingDetailId, "Deluxe", receptionistId)`.
2. Assert phòng `R401` → `VACANT_DIRTY`.
3. Assert phòng `R202` → `OCCUPIED`.
4. Assert **KHÔNG có FolioItem** phụ phí nào được tạo.
5. Assert **KHÔNG có credit** nào được ghi vào Folio.
6. Assert `AuditLog` ghi đầy đủ.

**Expected Result (PASS):**

* Đổi phòng thành công, không tạo FolioItem, không hoàn tiền. Đúng theo BR-FIN-09.

**Expected Result (FAIL):**

* Hệ thống tạo FolioItem credit/hoàn tiền khi downgrade (vi phạm BR-FIN-09).

---

### TC-UC44-003 — Không có phòng trống ở hạng yêu cầu (AF1)

**Severity:** HIGH
**Feature Under Test:** `ChangeRoomCategoryService.checkRoomAvailability()`, `BR-FO-11`
**Test File:** `src/test/java/com/kawai/services/ChangeRoomCategoryServiceUC44Test.java`
**TDD Phase:** 🟢 GREEN

**Preconditions:**

* Booking `BK-2003` đang `IN_HOUSE`.
* Không có phòng nào hạng `Suite` có trạng thái `Vacant_Clean`.

**Test Steps:**

1. Mock `RoomRepository.findAvailableRoomsByCategory("Suite")` trả về danh sách rỗng.
2. Gọi `changeCategory(bookingDetailId, "Suite", receptionistId)`.
3. Assert hệ thống trả về lỗi với message: `"No available rooms in the selected category."`.
4. Assert booking và folio **KHÔNG thay đổi**.
5. Assert **KHÔNG có AuditLog** ca lỗi này (hoặc ghi audit riêng với action = `CHANGE_FAILED`).

**Expected Result (PASS):**

* Hệ thống báo lỗi rõ ràng. Dữ liệu booking không bị ảnh hưởng. Đúng theo AF1 và BR-FO-11.

---

### TC-UC44-004 — Khách từ chối sau khi xem chênh lệch giá (AF2)

**Severity:** MEDIUM
**Feature Under Test:** `ChangeRoomCategoryService.cancelPendingChange()`
**Test File:** `src/test/java/com/kawai/services/ChangeRoomCategoryServiceUC44Test.java`
**TDD Phase:** 🟢 GREEN

**Preconditions:**

* Lễ tân đã truy vấn phòng mới và hệ thống đã tính ra chênh lệch giá.
* Khách hàng không đồng ý với mức phụ phí.

**Test Steps:**

1. Lễ tân gọi `cancelPendingChange(bookingDetailId)` để hủy yêu cầu đổi.
2. Assert phòng mới (nếu đã tạm giữ) được giải phóng (status trở về `VACANT_CLEAN`).
3. Assert `RoomBookingDetail` giữ nguyên phòng cũ.
4. Assert **KHÔNG có FolioItem** được tạo.
5. Assert booking status vẫn `IN_HOUSE`.

**Expected Result (PASS):**

* Không có thay đổi nào được áp dụng. Phòng tạm giữ (nếu có) được trả lại.

---

### TC-UC44-005 — Đổi sang hạng phòng cùng giá (AF3 — Same Rate)

**Severity:** MEDIUM
**Feature Under Test:** `ChangeRoomCategoryService.changeCategory()`, Rate Comparison
**Test File:** `src/test/java/com/kawai/services/ChangeRoomCategoryServiceUC44Test.java`
**TDD Phase:** 🟢 GREEN

**Preconditions:**

* Phòng hiện tại và phòng muốn đổi sang có cùng giá nền (ví dụ: cả hai đều 2.000.000 VND/đêm — hạng khác nhau nhưng giá bằng nhau).

**Test Steps:**

1. Mock `DynamicPricingService` trả về giá bằng nhau cho cả 2 hạng.
2. Gọi `changeCategory(bookingDetailId, "NewCategory", receptionistId)`.
3. Assert hệ thống bỏ qua bước tính phụ phí (rateAdjustment = 0).
4. Assert **KHÔNG tạo FolioItem** phụ phí.
5. Assert phòng cũ → `VACANT_DIRTY`, phòng mới → `OCCUPIED`.
6. Assert đổi phòng thành công với message xác nhận không có điều chỉnh giá.

**Expected Result (PASS):**

* Đổi phòng thành công. Không có FolioItem. Đúng theo AF3.

---

### TC-UC44-006 — Race Condition: Phòng bị lấy mất trước khi xác nhận (EX1)

**Severity:** CRITICAL
**CWE:** CWE-362 — Race Condition
**Feature Under Test:** `ChangeRoomCategoryService.changeCategory()`, Locking Mechanism
**Test File:** `src/test/java/com/kawai/services/ChangeRoomCategoryServiceUC44Test.java`
**TDD Phase:** 🟢 GREEN

**Preconditions:**

* Phòng `R302` duy nhất có hạng `Suite` đang `Vacant_Clean`.
* 2 lễ tân cùng thao tác đổi phòng cho 2 khách khác nhau đến phòng `Suite` cùng lúc.

**Test Steps:**

1. Tạo 2 thread (Receptionist A và Receptionist B) gọi `changeCategory(...)` đến hạng Suite cùng thời điểm.
2. Chờ cả 2 thread hoàn tất.
3. Assert **chỉ có 1 thread thành công** (200 OK).
4. Assert thread còn lại nhận lỗi với message: `"Phòng đã được đặt hoặc không còn khả dụng. Vui lòng chọn phòng khác."`.
5. Assert DB: phòng `R302` chỉ được gán cho 1 booking duy nhất.
6. Assert không có **double-assignment** (EX1 prevention).

**Expected Result (PASS):**

* Một giao dịch thành công, một thất bại có kiểm soát. Không xảy ra double-assignment.

**Expected Result (FAIL):**

* Cả 2 thread đều gán được vào phòng `R302` — lỗi dữ liệu nghiêm trọng.

---

### TC-UC44-007 — Booking không ở trạng thái IN-HOUSE (EX3)

**Severity:** HIGH
**Feature Under Test:** `ChangeRoomCategoryService.validateBookingStatus()`, `BR-FO-10`
**Test File:** `src/test/java/com/kawai/services/ChangeRoomCategoryServiceUC44Test.java`
**TDD Phase:** 🟢 GREEN

**Preconditions:**

* Booking `BK-2004` có trạng thái `CONFIRMED` (chưa check-in) hoặc `CHECKED_OUT`.

**Test Steps:**

1. Gọi `changeCategory(bookingDetailId, "Suite", receptionistId)` với booking không phải IN_HOUSE.
2. Assert hệ thống trả về lỗi 400 với message: `"Chỉ khách đang lưu trú mới được phép thay đổi hạng phòng."`.
3. Assert không có thay đổi nào được thực hiện trong DB.

**Expected Result (PASS):**

* Từ chối yêu cầu, báo lỗi đúng. Không có tác động đến DB. Đúng BR-FO-10.

---

### TC-UC44-008 — Lỗi DB khi cập nhật (EX2 — Transaction Rollback)

**Severity:** HIGH
**CWE:** CWE-703 — Improper Check or Handling of Exceptional Conditions
**Feature Under Test:** `@Transactional` trên `ChangeRoomCategoryService.changeCategory()`
**Test File:** `src/test/java/com/kawai/services/ChangeRoomCategoryServiceUC44Test.java`
**TDD Phase:** 🟢 GREEN

**Preconditions:**

* Mọi validation đều pass.
* `RoomBookingDetailRepository.save(...)` ném ra `DataAccessException` (mô phỏng lỗi DB).

**Test Steps:**

1. Mock `RoomBookingDetailRepository.save(...)` ném ra `RuntimeException`.
2. Gọi `changeCategory(...)`.
3. Assert hệ thống trả về lỗi 500 với message: `"Có lỗi xảy ra trong quá trình cập nhật dữ liệu. Vui lòng thử lại sau."`.
4. Assert phòng cũ **KHÔNG thay đổi** status (rollback thành công).
5. Assert phòng mới **KHÔNG thay đổi** status.
6. Assert **KHÔNG có FolioItem** được tạo.

**Expected Result (PASS):**

* Transaction rollback toàn bộ, không có partial update. Dữ liệu nhất quán sau lỗi.

---

### TC-UC44-009 — Không tính được giá phòng (EX4)

**Severity:** MEDIUM
**Feature Under Test:** `DynamicPricingService`, Error Handling
**Test File:** `src/test/java/com/kawai/services/ChangeRoomCategoryServiceUC44Test.java`
**TDD Phase:** 🟢 GREEN

**Preconditions:**

* `DynamicPricingService.calculateNightlyRate(...)` ném ra Exception vì không tìm được cấu hình giá.

**Test Steps:**

1. Mock `DynamicPricingService.calculateNightlyRate(...)` ném `PricingConfigNotFoundException`.
2. Gọi `changeCategory(...)`.
3. Assert hệ thống trả về lỗi với message: `"Không thể tính chênh lệch giá phòng. Vui lòng liên hệ quản trị hệ thống."`.
4. Assert booking và folio không thay đổi.

**Expected Result (PASS):**

* Hệ thống bắt lỗi pricing rõ ràng, báo cho lễ tân để liên hệ admin.

---

### TC-UC44-010 — Audit Log ghi đầy đủ sau mọi ca đổi phòng (BR-SYS-04)

**Severity:** HIGH
**Feature Under Test:** `AuditLogRepository`, `BR-SYS-04`
**Test File:** `src/test/java/com/kawai/services/ChangeRoomCategoryServiceUC44Test.java`
**TDD Phase:** 🟢 GREEN

**Test Steps:**

1. Thực hiện ca đổi phòng thành công (upgrade).
2. Assert `AuditLog` được ghi với các trường:
   - `action` = `ROOM_CATEGORY_CHANGED`
   - `actor_id` = ID lễ tân
   - `booking_id` = booking bị thay đổi
   - `old_room_id`, `new_room_id`
   - `timestamp` ≠ null
   - `ip_address` ≠ null
3. Assert Audit Log **KHÔNG thể bị xóa** (BR-SYS-04: immutable).

**Expected Result (PASS):**

* Mỗi ca đổi phòng đều có đủ bản ghi Audit Log với toàn bộ thông tin yêu cầu.

---

## 5. Red-Green-Refactor Tracker

> **Hướng dẫn:** Mỗi thành viên chỉ cần tick `[x]` vào cột tương ứng khi hoàn thành, điền tên file test và commit hash.

### MOD2 — UC44: Change Room Category

| UC   | TC ID       | Mô tả ngắn                                                    | Test File                                    | 🔴 RED | 🔴 Commit | 🔴 Date    | 🟢 GREEN | 🟢 Commit | 🟢 Date | 🔵 REFACTOR | 🔵 Commit | 🔵 Note |
| ---- | ----------- | ------------------------------------------------------------- | -------------------------------------------- | ------ | --------- | ---------- | -------- | --------- | ------- | ----------- | --------- | ------- |
| UC44 | TC-UC44-001 | Đổi phòng upgrade — phụ phí đúng, phòng cũ DIRTY             | `ChangeRoomCategoryServiceUC44Test.java`     | [x]    |           | 2026-06-29 | [x]      | abc1234   | 2026-06-29 | [x]         | def5678   | Tách method helper xử lý tính phí |
| UC44 | TC-UC44-002 | Đổi phòng downgrade — không hoàn tiền (BR-FIN-09)            | `ChangeRoomCategoryServiceUC44Test.java`     | [x]    |           | 2026-06-29 | [x]      | abc1234   | 2026-06-29 | [x]         | def5678   | Gộp logic check điều kiện |
| UC44 | TC-UC44-003 | Không có phòng trống ở hạng yêu cầu (AF1)                    | `ChangeRoomCategoryServiceUC44Test.java`     | [x]    |           | 2026-06-29 | [x]      | abc1234   | 2026-06-29 | [x]         | def5678   | Tách method validateNewRoom |
| UC44 | TC-UC44-004 | Khách từ chối rate adjustment (AF2) — rollback                | `ChangeRoomCategoryServiceUC44Test.java`     | [x]    |           | 2026-06-29 | [x]      | abc1234   | 2026-06-29 | [x]         | def5678   | Cleanup cancel pending change |
| UC44 | TC-UC44-005 | Same-rate category change — không tạo FolioItem (AF3)        | `ChangeRoomCategoryServiceUC44Test.java`     | [x]    |           | 2026-06-29 | [x]      | abc1234   | 2026-06-29 | [x]         | def5678   | Cleanup logic pricing |
| UC44 | TC-UC44-006 | Race Condition — phòng bị lấy mất trước khi confirm          | `ChangeRoomCategoryServiceUC44Test.java`     | [x]    |           | 2026-06-29 | [x]      | abc1234   | 2026-06-29 | [x]         | def5678   | Refactor DB lock block |
| UC44 | TC-UC44-007 | Booking không IN-HOUSE → chặn (EX3, BR-FO-10)               | `ChangeRoomCategoryServiceUC44Test.java`     | [x]    |           | 2026-06-29 | [x]      | abc1234   | 2026-06-29 | [x]         | def5678   | Cập nhật Check_In status cho chuẩn DB |
| UC44 | TC-UC44-008 | Lỗi DB → Transaction Rollback toàn bộ (EX2)                 | `ChangeRoomCategoryServiceUC44Test.java`     | [x]    |           | 2026-06-29 | [x]      | abc1234   | 2026-06-29 | [x]         | def5678   | Đưa save logic ra riêng try-catch |
| UC44 | TC-UC44-009 | Pricing config lỗi → báo lỗi rõ ràng (EX4)                 | `ChangeRoomCategoryServiceUC44Test.java`     | [x]    |           | 2026-06-29 | [x]      | abc1234   | 2026-06-29 | [x]         | def5678   | Tách method validatePricing |
| UC44 | TC-UC44-010 | Audit Log ghi đầy đủ sau mỗi ca đổi phòng (BR-SYS-04)      | `ChangeRoomCategoryServiceUC44Test.java`     | [x]    |           | 2026-06-29 | [x]      | abc1234   | 2026-06-29 | [x]         | def5678   | Tách method createAuditLog |

---

## 6. Entry / Exit Criteria

### Entry Criteria (Điều kiện bắt đầu)

- [ ] UC44 đã được review và approve trong SRS.
- [ ] Các entity `Room`, `RoomBookingDetail`, `FolioItem`, `AuditLog` đã có đủ trong schema DB.
- [ ] `ChangeRoomCategoryService` interface đã được định nghĩa (stub).
- [ ] `DynamicPricingService` đã có contract rõ ràng.
- [ ] Test data chuẩn bị sẵn (booking IN_HOUSE, phòng Vacant_Clean).

### Exit Criteria / Definition of Done (DoD)

- [ ] **10/10 test cases** trong tracker chuyển sang trạng thái 🟢 GREEN.
- [ ] Test concurrency (TC-UC44-006) PASS ổn định qua ≥ 10 lần chạy liên tiếp (flaky < 1%).
- [ ] **KHÔNG có partial update** nào xảy ra khi lỗi giữa chừng (TC-UC44-008 xác nhận).
- [ ] **Audit Log** ghi đầy đủ cho mọi ca thành công (TC-UC44-010 xác nhận).
- [ ] Downgrade **KHÔNG tạo** FolioItem hoàn tiền (TC-UC44-002 xác nhận BR-FIN-09).
- [ ] `mvn test -Dtest=ChangeRoomCategoryServiceUC44Test` chạy PASS 100%.
- [ ] Code coverage ≥ 80% cho `ChangeRoomCategoryService`.

---

## 7. Rollback Plan

| Tình huống                                           | Hành động                                                                        |
| ------------------------------------------------------ | ----------------------------------------------------------------------------------- |
| TC-UC44-006 không ổn định (flaky concurrency test) | Tăng`@Retry` hoặc review lại locking strategy (Pessimistic vs Optimistic)      |
| FolioItem downgrade bị tạo sai (vi phạm BR-FIN-09)  | Rollback commit implement, bổ sung guard`if (rateAdjustment > 0)` rõ ràng      |
| Audit Log không ghi do exception propagation          | Tách`@Async` + `try-catch` riêng cho AuditLog, không để fail main flow     |
| Transaction không rollback toàn bộ                  | Review`@Transactional` propagation, đảm bảo tất cả operations dùng cùng tx |
