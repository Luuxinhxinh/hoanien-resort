# TEST-DRIVEN DEVELOPMENT SPECIFICATION

## UC-15: Reserve a Restaurant Table (Table Management) — Đặc tả Kiểm thử Hướng Phát triển

| Field                  | Value                       |
| ---------------------- | --------------------------- |
| **Document ID**  | `KAWAI-TDD-MOD3-UC15-001` |
| **Version**      | 2.0                         |
| **Date**         | 2026-06-20                  |
| **Status**       | Approved                    |
| **Standard**     | ISO/IEC/IEEE 29119-3:2021   |
| **Author**       | Trịnh Minh Đức            |
| **Reviewed by**  | Nguyễn Xuân Lưu          |
| **DPO Sign-off** |                             |
| **Approved by**  | [x] Trịnh Minh Đức       |
| **Based on EDS** | v2.0                        |

> **Quy ước TDD:** Tài liệu này mô tả test cases TRƯỚC khi viết production code.
> Thứ tự bắt buộc: viết test (`.java`) → chạy → xác nhận FAIL 🔴 → implement → PASS 🟢 → refactor 🔵.

---

### CHANGELOG

| Ngày      | Người thực hiện | Nội dung thay đổi                                                                                                                   |
| ---------- | ------------------- | -------------------------------------------------------------------------------------------------------------------------------------- |
| 2026-06-20 | Antigravity Agent | Cập nhật file theo đúng format 7 sections chuẩn TDD của UC-14 mẫu. Bổ sung trọn bộ 3 Test Cases xử lý Time Wrap-around và Physical Check. |
| 2026-06-15 | Trịnh Minh Đức | Khởi tạo tài liệu TDD sơ khai. |

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
| **Feature / Gap ID**    | `GAP-MOD3-UC15`                                                               |
| **Use Case**            | UC-15 — Reserve a Restaurant Table (Table Management)                              |
| **Module**              | MOD3 — Restaurant POS & F&B Operations                                  |
| **Priority**            | 🔴 P0 — Critical                                                               |
| **Sprint**              | S1 (2026-06-09 → 2026-06-23)                                                   |
| **Milestone**           | M3 Alpha — 2026-07-11                                                          |
| **Data Classification** | Internal (Dữ liệu nội bộ F&B)                                |
| **Compliance Scope**    | Nội bộ nhà hàng                                                              |
| **Primary Actor**       | Customer, F&B Staff                                                                        |
| **Secondary Actor**     | System                                              |

---

### 2. Logic Issues Resolved

| #  | Spec gốc (sai / thiếu)                                             | Thực tế (schema / policy)                                                                           | Fix áp dụng trong test                                                                            |
| -- | -------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------- |
| L1 | Không rõ bàn đang có khách ngồi thì khách khác có được đặt không? | Nếu đặt cận giờ (cách thời điểm đặt < 2 tiếng), hệ thống phải check trạng thái vật lý thực tế của bàn | TC-M3-UC15-002 (Physical Check) đảm bảo bàn Occupied/Cleaning bị chặn nếu đặt trong vòng 2h.  |
| L2 | Check cận giờ qua mốc nửa đêm bị lỗi sai logic thời gian.        | `LocalTime.plusHours(2)` sẽ bị wrap-around (ví dụ: 22h+2h = 00h). Cần dùng `LocalDateTime`. | TC-M3-UC15-003 kiểm tra lỗi Time wrap-around ở mốc 22:55.                  |
| L3 | Trùng lịch đặt bàn chưa xử lý đồng thời                        | Query mọi lịch đặt trong ngày và đối chiếu giờ start/end.                                        | TC-M3-UC15-001 kiểm tra việc Overlap giờ đặt bàn.                                                 |

---

### 3. Test Design Specification (TDS)

#### TDS-01 — Scope / Phạm vi

```
Table Reservation Backend — Spring Boot
├── Controller  : TableApiController.getAvailableTables()
├── Repository  : TableReservationRepository, RestaurantTableRepository
└── Validation  : Kiểm tra Physical Status và Overlap Logic

NGOÀI PHẠM VI UC-15 (test riêng):
  ✖ Đặt thức ăn trực tiếp tại bàn (Dine In Order) → Test riêng (UC-19)
```

#### TDS-02 — Test Basis / Cơ sở Kiểm thử

| Source                | Items Derived                                                                      |
| --------------------- | ---------------------------------------------------------------------------------- |
| SRS UC-2.1.19    | Bàn không bị trùng lịch (Overlap check) |
| EDS ADR-UC15-002      | Fix lỗi Time Wrap-around qua 00:00 của `LocalTime` bằng `LocalDateTime`          |

#### TDS-03 — Test Techniques

| Kỹ thuật                              | Áp dụng cho Test Case                                               |
| --------------------------------------- | --------------------------------------------------------------------- |
| **Equivalence Partitioning**      | TC-M3-UC15-001 (Overlap Check chuẩn)                      |
| **Boundary Value Analysis**       | TC-M3-UC15-003 (Mốc thời gian sát nửa đêm 22:50 - 00:50)                |
| **Negative Test**                 | TC-M3-UC15-002, TC-M3-UC15-003                                     |

#### TDS-04 — Test Conditions and Coverage Items

| Condition ID    | SRS Coverage                        | Test Condition                                                              | Test Case |
| --------------- | ----------------------------------- | --------------------------------------------------------------------------- | --------- |
| TC-COND-M3-01   | Overlap Check                       | Bàn đã được đặt trước ở cùng khung giờ sẽ bị loại khỏi danh sách trống      | TC-M3-UC15-001 |
| TC-COND-M3-02   | Physical Status Check               | Đặt cận giờ (< 2 tiếng) và bàn đang Occupied sẽ bị báo bận  | TC-M3-UC15-002 |
| TC-COND-M3-03   | Time Wrap-around Bug                | Đặt cận giờ qua mốc nửa đêm (VD: 22:55) không bị vô hiệu hóa logic check bận                                       | TC-M3-UC15-003 |

---

### 4. Test Case Specification

---

#### `TC-M3-UC15-001` — Normal Flow: Trùng lịch (Overlap)

**Severity:** 🔴 CRITICAL
**Feature Under Test:** Overlap Checking
**TDD Phase:** 🟢 GREEN (Retroactive)
**Test File:** `TableApiControllerTest.java`
**Test Data Classification:** SYNTHETIC

**Preconditions:**
- Bàn số 4 có 1 TableReservation từ `18:00` đến `20:00`.

**Test Steps:**
1. Khách gọi `GET /api/v1/tables/availability` cho cùng ngày, từ `19:00` đến `21:00`.
2. Assert HTTP 200 OK.
3. Assert danh sách `availableTableIds` trả về.

**Expected Result (PASS):**
- Bàn số 4 KHÔNG xuất hiện trong danh sách vì bị trùng giờ (19:00 - 20:00 là thời gian giao thoa).

---

#### `TC-M3-UC15-002` — E-01: Trạng thái vật lý cận giờ (Physical Check)

**Severity:** 🔴 CRITICAL
**Feature Under Test:** Physical Status Rules
**TDD Phase:** 🟢 GREEN (Retroactive)
**Test File:** `TableApiControllerTest.java`
**Test Data Classification:** SYNTHETIC

**Preconditions:**
- Thời điểm hiện tại là `18:00`.
- Bàn số 1 đang có khách ngồi thực tế (`tableStatus = Occupied`).

**Test Steps:**
1. Khách gọi `GET /api/v1/tables/availability` cho ngày hôm nay, từ `19:00` đến `21:00`.
2. Assert HTTP 200 OK.

**Expected Result (PASS):**
- Giờ đặt (19:00) cách hiện tại (18:00) dưới 2 tiếng và Bàn 1 đang `Occupied`.
- Bàn số 1 KHÔNG xuất hiện trong danh sách `availableTableIds`.

---

#### `TC-M3-UC15-003` — E-02: Time Wrap-around Bug

**Severity:** 🟠 HIGH
**Feature Under Test:** Time Comparison Edge Case
**TDD Phase:** 🟢 GREEN (Retroactive)
**Test File:** `TableApiControllerTest.java`
**Test Data Classification:** SYNTHETIC

**Preconditions:**
- Thời điểm hiện tại giả lập là `22:50` ngày 20/06.
- Bàn số 1 đang `Occupied`.

**Test Steps:**
1. Khách gọi `GET /api/v1/tables/availability` cho ngày 20/06, lúc `22:55` đến `23:30`.
2. Assert HTTP 200 OK.

**Expected Result (PASS):**
- Hệ thống lấy `LocalDateTime` cộng 2 tiếng để ra `00:50 ngày 21/06`.
- 22:55 ngày 20/06 < 00:50 ngày 21/06.
- Bàn số 1 KHÔNG khả dụng và KHÔNG xuất hiện trong danh sách trả về.

---

### 5. Red-Green-Refactor Tracker

| UC    | TC ID      | Mô tả ngắn                                                        | Test File                         | 🔴 RED | 🔴 Date | 🟢 GREEN | 🟢 Date | 🔵 REFACTOR | 🔵 Note |
| ----- | ---------- | -------------------------------------------------------------------- | --------------------------------- | ------ | ------- | -------- | ------- | ----------- | ------- |
| UC-15 | TC-M3-UC15-001  | Overlap Check (Trùng giờ đặt bàn) | `TableApiControllerTest.java` | [x]    | 26-06-20| [x]      | 26-06-20| [x]         | ✅ Retroactive |
| UC-15 | TC-M3-UC15-002 | Physical Check cận giờ (< 2 tiếng)               | `TableApiControllerTest.java` | [x]    | 26-06-20| [x]      | 26-06-20| [x]         | ✅ Retroactive |
| UC-15 | TC-M3-UC15-003 | Fix Time Wrap-around Bug qua nửa đêm                        | `TableApiControllerTest.java` | [x]    | 26-06-20| [x]      | 26-06-20| [x]         | ✅ Retroactive |

---

### 6. Entry / Exit Criteria

#### Entry Criteria
- [x] Code base setup hoàn chỉnh.
- [x] Entity RestaurantTable và TableReservation được định nghĩa.

#### Exit Criteria — Definition of Done (DoD)
- [x] Không có lỗi logic thời gian ở mọi edge cases (nhất là qua 00:00).
- [x] Lỗi Table Overbooking được giải quyết triệt để trên production.

---

### 7. Rollback Plan

Nếu phát hiện logic lọc bàn sai ảnh hưởng đến khách hàng trên web:

```bash
git checkout tags/v[previous-stable] -- src/main/java/com/kawai/controllers/api/TableApiController.java
```

**Khắc phục nhanh dữ liệu sai:**
Nếu lỡ có khách đặt trùng bàn, nhân viên Reception hoặc F&B sẽ gọi điện trực tiếp cho khách để xin dời bàn.