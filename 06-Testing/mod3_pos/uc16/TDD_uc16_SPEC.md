# TEST-DRIVEN DEVELOPMENT SPECIFICATION

## UC-19: Create Dine-In Order (Gọi món tại bàn) — Đặc tả Kiểm thử Hướng Phát triển

| Field                  | Value                       |
| ---------------------- | --------------------------- |
| **Document ID**  | `KAWAI-TDD-MOD3-UC19-001` |
| **Version**      | 2.0                         |
| **Date**         | 2026-06-19                  |
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
| 2026-06-19 | Antigravity Agent | Đổi tên Use Case thành UC-19 (Create Dine-In Order). Viết lại cấu trúc theo đúng format chuẩn TDD của EDS v2.0. Bổ sung trọn bộ 6 Test Cases. |
| 2026-06-17 | Trịnh Minh Đức | Cập nhật luồng Dine-In, thêm test case cho ghi chú đặc biệt và lưu giá snapshot. |
| 2026-06-15 | Trịnh Minh Đức | Khởi tạo tài liệu. |

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
| **Feature / Gap ID**    | `GAP-MOD3-UC19`                                                               |
| **Use Case**            | UC-19 — Create Dine-In Order                                                   |
| **Module**              | MOD3 — Restaurant POS & F&B Operations                                  |
| **Priority**            | 🔴 P0 — Critical                                                               |
| **Sprint**              | S1 (2026-06-09 → 2026-06-23)                                                   |
| **Milestone**           | M3 Alpha — 2026-07-11                                                          |
| **Data Classification** | Internal (Dữ liệu nội bộ F&B, không chứa PII nhạy cảm)                      |
| **Compliance Scope**    | Nội bộ nhà hàng                                                               |
| **Primary Actor**       | Cashier / F&B Staff                                                             |
| **Secondary Actor**     | System / Kitchen Staff                                                          |

---

### 2. Logic Issues Resolved

| #  | Spec gốc (sai / thiếu)                                             | Thực tế (schema / policy)                                                                           | Fix áp dụng trong test                                                                            |
| -- | -------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------- |
| L1 | Chưa rõ xử lý Dine-In có kiểm tra Credit Limit hay không?   | Dine-In thu tiền tại quầy hoặc tính sau, KHÔNG dùng Credit Limit của phòng khách sạn        | TC-M3-006 xác minh không có tương tác nào với `RoomBookingRepository`                        |
| L2 | Thanh toán ngay tại lúc gọi món thì trạng thái là gì?             | Cập nhật orderStatus = "PAID", `isPaidInPos = true` ngay lập tức                          | TC-M3-006d kiểm tra behavior khi cờ `isPaid = true` được gửi lên                                |
| L3 | Giá thay đổi thì đơn cũ bị lỗi?                                   | Lấy giá trực tiếp từ DTO và gán thành `priceAtOrder` trong `FoodOrderDetail`              | TC-M3-006b verify giá được lưu chuẩn từ DTO gửi lên, bỏ qua giá gốc trong DB                   |
| L4 | Bàn ăn không tồn tại hoặc đã bị khóa thì sao? (E2)               | Trả về HTTP 400 và không cho phép lên đơn                                                          | TC-M3-006e kiểm tra validation với `restaurantTableRepository`                                  |

---

### 3. Test Design Specification (TDS)

#### TDS-01 — Scope / Phạm vi

```
Dine-In Order Backend — Spring Boot
├── Controller  : PosApiController.createOrder()
├── Repository  : FoodOrderRepository, FoodOrderDetailRepository
└── Integration : RestaurantTableRepository (để gán bàn)

NGOÀI PHẠM VI UC-19 (test riêng):
  ✖ KDS Status Update (Bếp đổi trạng thái) → Test riêng (Kitchen Module)
```

#### TDS-02 — Test Basis / Cơ sở Kiểm thử

| Source                | Items Derived                                                                      |
| --------------------- | ---------------------------------------------------------------------------------- |
| SRS §2.1.19 UC-19    | Normal Flow, AF-01 (Multiple Items), AF-02 (Special Request), AF-03 (Pay Immediate), E-01 (Table), E-02 (Menu) |

#### TDS-03 — Test Techniques

| Kỹ thuật                              | Áp dụng cho Test Case                                               |
| --------------------------------------- | --------------------------------------------------------------------- |
| **Equivalence Partitioning**      | TC-M3-006 (dữ liệu hợp lệ), TC-M3-006e (sai mã bàn)                    |
| **State Transition**              | TC-M3-006d (Trạng thái PAID khi thanh toán ngay)                       |
| **Negative Test**                 | TC-M3-006e, TC-M3-006f                                                 |

#### TDS-04 — Test Conditions and Coverage Items

| Condition ID    | SRS Coverage                        | Test Condition                                                              | Test Case |
| --------------- | ----------------------------------- | --------------------------------------------------------------------------- | --------- |
| TC-COND-M3-06   | Normal Flow                         | Tạo order Dine-In thành công, gán đúng bàn và lưu số lượng, giá          | TC-M3-006 |
| TC-COND-M3-07   | BR-UC19-03                          | Kiểm tra giá snapshot (priceAtOrder) được lưu chính xác                   | TC-M3-006b |
| TC-COND-M3-08   | AF-02                               | Thêm ghi chú đặc biệt và tên khách vào đơn hàng                           | TC-M3-006c |
| TC-COND-M3-09   | AF-03 / BR-UC19-04                  | Thu ngân chọn "Thanh toán ngay" → Cập nhật status thành PAID              | TC-M3-006d |
| TC-COND-M3-10   | E-01                                | Bàn không tồn tại → Reject đơn hàng 400                                  | TC-M3-006e |
| TC-COND-M3-11   | E-02                                | Món ăn không tồn tại → Reject đơn hàng 400                                | TC-M3-006f |

---

### 4. Test Case Specification

---

#### `TC-M3-006` — Normal Flow: Tạo order Dine-In thành công

**Severity:** 🔴 CRITICAL
**Feature Under Test:** UC-19 Normal Flow
**TDD Phase:** 🟢 GREEN (Retroactive)
**Test File:** `PosApiControllerUC19Test.java`

**Preconditions:**
- Bàn `A5` có ID `5L` tồn tại trong hệ thống.
- Các món ăn khách gọi đang active.

**Test Steps:**
1. Thu ngân gửi `POST /api/pos/orders` với `orderType="dine-in"`, `tableId=5`, danh sách items.
2. Assert HTTP 200 OK.
3. Assert body `status = "success"`.
4. Verify `foodOrderRepository.save()` chạy 1 lần.
5. Verify `roomBookingRepository` KHÔNG được gọi (Dine-In độc lập với phòng).

---

#### `TC-M3-006b` — Verify giá snapshot (priceAtOrder)

**Severity:** 🟡 MEDIUM
**Feature Under Test:** UC-19 Business Rule
**TDD Phase:** 🟢 GREEN (Retroactive)
**Test File:** `PosApiControllerUC19Test.java`

**Test Steps:**
1. Thu ngân gửi đơn hàng có món giá `120,000 VND`.
2. Capture Object `FoodOrderDetail` được truyền vào `save()`.
3. Assert `priceAtOrder` là 120,000 VND, `quantity` là đúng, `kotStatus` = "Pending".

---

#### `TC-M3-006c` — AF-02: Dine-In với ghi chú đặc biệt

**Severity:** 🟡 MEDIUM
**Feature Under Test:** UC-19 AF-02 (Ghi chú khách)
**TDD Phase:** 🟢 GREEN (Retroactive)
**Test File:** `PosApiControllerUC19Test.java`

**Test Steps:**
1. Đặt thuộc tính `guestName` và `note` trong payload.
2. Assert `FoodOrder` lưu vào DB có chứa cả tên khách và nội dung ghi chú.
3. Assert `orderType` là "Dine In".

---

#### `TC-M3-006d` — AF-03: Thanh toán ngay tại POS

**Severity:** 🟠 HIGH
**Feature Under Test:** UC-19 AF-03
**TDD Phase:** 🟢 GREEN (Retroactive)
**Test File:** `PosApiControllerUC19Test.java`

**Test Steps:**
1. Thu ngân chọn thanh toán luôn tại quầy (`isPaid = true`).
2. Assert HTTP 200 OK.
3. Capture `FoodOrder` và assert `orderStatus` là `PAID`.
4. Assert `isPaidInPos` là `true`.

---

#### `TC-M3-006e` — E-01: Bàn không tồn tại (Table Not Found)

**Severity:** 🟠 HIGH
**Feature Under Test:** UC-19 E-01
**TDD Phase:** 🟢 GREEN (Retroactive)
**Test File:** `PosApiControllerUC19Test.java`

**Test Steps:**
1. Gửi request với `tableId = 999`.
2. Assert HTTP 400 Bad Request.
3. Assert response message chứa chuỗi `"Bàn ăn không tồn tại!"`.

---

#### `TC-M3-006f` — E-02: Món ăn không tồn tại

**Severity:** 🟡 MEDIUM
**Feature Under Test:** UC-19 E-02
**TDD Phase:** 🟢 GREEN (Retroactive)
**Test File:** `PosApiControllerUC19Test.java`

**Test Steps:**
1. Gửi request chứa `itemId = 11` (không tồn tại trong DB mockup).
2. Assert HTTP 400 Bad Request.
3. Assert response message chứa chuỗi `"Món ăn không tồn tại!"`.

---

### 5. Red-Green-Refactor Tracker

| UC    | TC ID      | Mô tả ngắn                                                        | Test File                         | 🔴 RED | 🔴 Date | 🟢 GREEN | 🟢 Date | 🔵 REFACTOR | 🔵 Note |
| ----- | ---------- | -------------------------------------------------------------------- | --------------------------------- | ------ | ------- | -------- | ------- | ----------- | ------- |
| UC-19 | TC-M3-006  | Normal Flow: Dine-In đặt thành công, lưu đúng số lượng, giá | `PosApiControllerUC19Test.java` | [x]    | 26-06-19| [x]      | 26-06-19| [x]         | ✅ Retroactive |
| UC-19 | TC-M3-006b | Giá tại thời điểm gọi món (priceAtOrder) được lưu chuẩn         | `PosApiControllerUC19Test.java` | [x]    | 26-06-19| [x]      | 26-06-19| [x]         | ✅ Retroactive |
| UC-19 | TC-M3-006c | Ghi chú đặc biệt lưu vào đơn hàng                               | `PosApiControllerUC19Test.java` | [x]    | 26-06-19| [x]      | 26-06-19| [x]         | ✅ Retroactive |
| UC-19 | TC-M3-006d | Thanh toán ngay tại POS: status PAID                            | `PosApiControllerUC19Test.java` | [x]    | 26-06-19| [x]      | 26-06-19| [x]         | ✅ Retroactive |
| UC-19 | TC-M3-006e | Lỗi 400 khi Bàn ăn không tồn tại                                | `PosApiControllerUC19Test.java` | [x]    | 26-06-19| [x]      | 26-06-19| [x]         | ✅ Retroactive |
| UC-19 | TC-M3-006f | Lỗi 400 khi Món ăn không tồn tại                                | `PosApiControllerUC19Test.java` | [x]    | 26-06-19| [x]      | 26-06-19| [x]         | ✅ Retroactive |

---

### 6. Entry / Exit Criteria

#### Entry Criteria
- [x] Database tables cho nhà hàng (`restaurant_tables`) đã sẵn sàng.
- [x] PosApiController đã chuẩn bị xong logic rẽ nhánh cho `orderType != "room-svc"`.

#### Exit Criteria — Definition of Done (DoD)
- [x] Unit tests pass 100% (6/6 test cases cho UC-19).
- [x] `mvn test -Dtest=PosApiControllerUC19Test` trả về SUCCESS.

---

### 7. Rollback Plan

Nếu xảy ra lỗi lên đơn trên production:
```bash
git checkout -- src/main/java/com/kawai/controllers/api/PosApiController.java
```
