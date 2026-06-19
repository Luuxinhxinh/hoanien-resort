# TEST-DRIVEN DEVELOPMENT SPECIFICATION

## UC-17: Order Food Online (Room Service / E-Menu) — Đặc tả Kiểm thử Hướng Phát triển

| Field                  | Value                       |
| ---------------------- | --------------------------- |
| **Document ID**  | `KAWAI-TDD-MOD3-UC17-001` |
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
| 2026-06-19 | Antigravity Agent | Đổi tên tài liệu từ UC16/14 thành UC-17 cho đúng SRS. Viết lại cấu trúc theo đúng format 7 sections chuẩn TDD của UC-14 mẫu. Bổ sung trọn bộ 6 Test Cases. |
| 2026-06-17 | Trịnh Minh Đức | Cập nhật file cho PosApiController, sửa tên mã Test. |
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
| **Feature / Gap ID**    | `GAP-MOD3-UC17`                                                               |
| **Use Case**            | UC-17 — Order Food Online (Room Service / E-Menu)                              |
| **Module**              | MOD3 — Restaurant POS & F&B Operations                                  |
| **Priority**            | 🔴 P0 — Critical                                                               |
| **Sprint**              | S1 (2026-06-09 → 2026-06-23)                                                   |
| **Milestone**           | M3 Alpha — 2026-07-11                                                          |
| **Data Classification** | Internal (Dữ liệu nội bộ F&B, không chứa PII)                                |
| **Compliance Scope**    | Nội bộ khách sạn                                                              |
| **Primary Actor**       | Customer                                                                        |
| **Secondary Actor**     | System / F&B Staff / Kitchen Staff                                              |

---

### 2. Logic Issues Resolved

| #  | Spec gốc (sai / thiếu)                                             | Thực tế (schema / policy)                                                                           | Fix áp dụng trong test                                                                            |
| -- | -------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------- |
| L1 | Chưa rõ giá món ăn lúc đặt và sau này đổi thì tính sao?      | Giá tại thời điểm đặt (priceAtOrder) phải được snapshot và lưu thẳng vào FoodOrderDetail     | TC-M3-001 verify priceAtOrder được lưu chuẩn theo giá gửi lên từ Cart request                  |
| L2 | Trạng thái KOT của món ăn sau khi gọi là gì?                      | Tất cả món ăn sau khi đặt thành công đều có kotStatus = "Pending" chờ bếp tiếp nhận        | TC-M3-001 verify trường kotStatus = "Pending"                                                 |
| L3 | Phí phục vụ (Service Fee) chưa được tính rõ ràng                | Charge to Room luôn cộng thêm 5% service fee vào subtotal để trừ credit limit của phòng             | TC-M3-001 và TC-M3-001b tính toán công thức subtotal + 5% và so sánh với CreditLimit còn lại        |
| L4 | Khách viết ghi chú (AF2) thì lưu ở đâu?                            | Lưu vào trường `note` của bảng `FoodOrder`, kèm theo tên khách (nếu có)                      | TC-M3-001c (AF2) kiểm tra nội dung ghi chú và tên khách được lưu thành công                       |
| L5 | Nếu Credit Limit < Total Amount thì sao? (E1)                        | Báo lỗi 400 Bad Request ngay, không tạo đơn hàng, không trừ tiền                                    | TC-M3-001d (E1) xác minh lỗi HTTP 400 và không có thay đổi DB                                        |
| L6 | Nếu phòng đã checkout hoặc số phòng sai (E2)?                       | Báo lỗi 400 Bad Request "Phòng không tồn tại!"                                                 | TC-M3-001e (E2) kiểm tra validation roomNumber                                                  |

---

### 3. Test Design Specification (TDS)

#### TDS-01 — Scope / Phạm vi

```
Room Service Order Backend — Spring Boot
├── Controller  : PosApiController.createOrder()
├── Repository  : FoodOrderRepository, FoodOrderDetailRepository
├── Integration : RoomBookingRepository (để trừ CreditLimit)
└── Event       : FoodOrderCreatedEvent (ApplicationEventPublisher - sẽ implement)

NGOÀI PHẠM VI UC-17 (test riêng):
  ✖ KDS Status Update (Bếp đổi trạng thái) → Test riêng (Kitchen Module)
  ✖ Checkout / Settlement Folio            → Test riêng (Checkout Module)
```

#### TDS-02 — Test Basis / Cơ sở Kiểm thử

| Source                | Items Derived                                                                      |
| --------------------- | ---------------------------------------------------------------------------------- |
| SRS §2.1.17 UC-17    | Normal Flow, AF-01 (Multiple Items), AF-02 (Special Request), E-01 (Credit Limit), E-02, E-03 |
| EDS ADR-UC17-001      | Xử lý Charge to Room và Credit Limit tại Controller (cho response nhanh)          |
| EDS ADR-UC17-002      | Snapshot Pricing: `priceAtOrder` là hằng số theo thời điểm đặt                 |

#### TDS-03 — Test Techniques

| Kỹ thuật                              | Áp dụng cho Test Case                                               |
| --------------------------------------- | --------------------------------------------------------------------- |
| **Equivalence Partitioning**      | TC-M3-001 (data chuẩn), TC-M3-001e (sai mã phòng)                      |
| **Boundary Value Analysis**       | TC-M3-001d (Credit Limit sát mép hoặc nhỏ hơn số tiền)                |
| **Negative Test**                 | TC-M3-001d, TC-M3-001e, TC-M3-001f                                     |

#### TDS-04 — Test Conditions and Coverage Items

| Condition ID    | SRS Coverage                        | Test Condition                                                              | Test Case |
| --------------- | ----------------------------------- | --------------------------------------------------------------------------- | --------- |
| TC-COND-M3-01   | Normal Flow                         | Gọi món Room Service thành công, Credit Limit trừ chuẩn (có phí 5%)      | TC-M3-001 |
| TC-COND-M3-02   | AF-01                               | Gọi nhiều món cùng lúc (qty > 1, nhiều items), tính tổng tiền chính xác  | TC-M3-001b |
| TC-COND-M3-03   | AF-02                               | Thêm ghi chú đặc biệt vào đơn hàng                                       | TC-M3-001c |
| TC-COND-M3-04   | E-01                                | Tổng đơn hàng vượt hạn mức tín dụng còn lại → Reject đơn hàng        | TC-M3-001d |
| TC-COND-M3-05   | E-02                                | Phòng không tồn tại trong hệ thống → Reject đơn hàng                   | TC-M3-001e |
| TC-COND-M3-06   | E-03                                | Món ăn không còn tồn tại trong DB → Reject đơn hàng                     | TC-M3-001f |

---

### 4. Test Case Specification

---

#### `TC-M3-001` — Normal Flow: Room Service thành công, Credit Limit trừ đúng

**Severity:** 🔴 CRITICAL
**Feature Under Test:** UC-17 Normal Flow
**SRS Coverage:** Normal Flow Steps 1-16
**TDD Phase:** 🟢 GREEN (Retroactive)
**Test File:** `PosApiControllerUC17Test.java`
**Test Data Classification:** SYNTHETIC

**Preconditions:**
- Khách ở phòng `101`, credit limit ban đầu: `500,000 VND`.
- Món ăn ID=1 có giá `100,000 VND` sẵn sàng.

**Test Steps:**
1. Khách gọi `POST /api/pos/orders` qua app E-Menu: đặt 2 món ID=1, Charge to Room.
2. Assert HTTP 200 OK, `status = "success"`.
3. Truy vấn DB mock: `foodOrderRepository.save()` được gọi 1 lần với orderType = "Room Service".
4. Truy vấn DB mock: `foodOrderDetailRepository.save()` được gọi 1 lần với priceAtOrder = 100000.
5. Truy vấn DB mock: `roomBookingRepository.save()` được gọi 1 lần với CreditLimit cập nhật.
6. Xác minh CreditLimit mới: 500k - (200k + 5% fee) = `290,000 VND`.

**Expected Result (PASS):**
- Đơn hàng tạo thành công, giá tiền lưu chuẩn snapshot, Credit Limit trừ đúng 210,000 VND.

---

#### `TC-M3-001b` — AF-01: Đặt nhiều món (Multiple Items)

**Severity:** 🟡 MEDIUM
**Feature Under Test:** UC-17 AF-01
**SRS Coverage:** AF-01 "Multiple Items Ordered"
**TDD Phase:** 🟢 GREEN (Retroactive)
**Test File:** `PosApiControllerUC17Test.java`
**Test Data Classification:** SYNTHETIC

**Preconditions:**
- Món ID=1 (100k), Món ID=2 (80k). Credit limit phòng: 500k.

**Test Steps:**
1. Khách gọi `POST /api/pos/orders` với mảng items gồm: 2 phần món ID=1 (200k) và 3 phần món ID=2 (240k).
2. Assert HTTP 200 OK.
3. Xác minh tổng đơn: 440k + 5% (22k) = 462,000 VND.
4. Xác minh Credit Limit mới: 500k - 462k = `38,000 VND`.

**Expected Result (PASS):**
- Thuật toán tính tổng tiền (kết hợp số lượng và giá) chính xác cho nhiều phần tử.

---

#### `TC-M3-001c` — AF-02: Ghi chú đặc biệt (Special Request)

**Severity:** 🟡 MEDIUM
**Feature Under Test:** UC-17 AF-02
**SRS Coverage:** AF-02 "Special Request"
**TDD Phase:** 🟢 GREEN (Retroactive)
**Test File:** `PosApiControllerUC17Test.java`
**Test Data Classification:** SYNTHETIC

**Test Steps:**
1. Khách thêm thuộc tính `note="Không hành, dị ứng hải sản"` và `guestName="Nguyễn Thị B"` vào Request.
2. Assert HTTP 200 OK.
3. Assert FoodOrder lưu trữ thành công có chứa chuỗi "Dị ứng hải sản" và "Nguyễn Thị B".

**Expected Result (PASS):**
- Note được đính kèm thành công vào FoodOrder phục vụ bếp.

---

#### `TC-M3-001d` — E-01: Credit Limit Exceeded (Hạn mức không đủ)

**Severity:** 🟠 HIGH
**Feature Under Test:** UC-17 E-01
**SRS Coverage:** E-01 "Credit Limit Exceeded"
**TDD Phase:** 🟢 GREEN (Retroactive)
**Test File:** `PosApiControllerUC17Test.java`
**Test Data Classification:** SYNTHETIC

**Preconditions:**
- Credit Limit của phòng chỉ còn `100,000 VND`. Tổng đơn khách đặt tốn `210,000 VND`.

**Test Steps:**
1. Khách gọi `POST /api/pos/orders`.
2. Assert HTTP 400 Bad Request.
3. Assert JSON response chứa `"Hạn mức tín dụng của phòng không đủ"`.
4. Assert method `save` của FoodOrder và RoomBooking KHÔNG ĐƯỢC GỌI.

**Expected Result (PASS):**
- Giao dịch bị hủy, Credit Limit giữ nguyên `100,000 VND`.

---

#### `TC-M3-001e` — E-02: Phòng không tồn tại (Room Not Eligible)

**Severity:** 🟠 HIGH
**Feature Under Test:** UC-17 E-02
**SRS Coverage:** E-02 "Room Not Eligible"
**TDD Phase:** 🟢 GREEN (Retroactive)
**Test File:** `PosApiControllerUC17Test.java`
**Test Data Classification:** SYNTHETIC

**Test Steps:**
1. Khách điền `roomNumber="999"` (không tồn tại trong hệ thống DB).
2. Assert HTTP 400 Bad Request, message `"Phòng không tồn tại!"`.
3. Assert không có FoodOrder nào được tạo.

**Expected Result (PASS):**
- Validate thất bại, trả lỗi sớm.

---

#### `TC-M3-001f` — E-03: Món ăn không tồn tại (Menu Item Unavailable)

**Severity:** 🟡 MEDIUM
**Feature Under Test:** UC-17 E-03
**SRS Coverage:** E-03 "Menu Item Unavailable"
**TDD Phase:** 🟢 GREEN (Retroactive)
**Test File:** `PosApiControllerUC17Test.java`
**Test Data Classification:** SYNTHETIC

**Test Steps:**
1. Khách chọn mua món ID=1, nhưng món ID=1 đã bị xoá khỏi `menu_items` DB trong lúc đang đặt hàng.
2. Assert HTTP 400 Bad Request, message `"Món ăn không tồn tại!"`.
3. Assert không có FoodOrderDetail nào được lưu.

**Expected Result (PASS):**
- Validate item thất bại, từ chối tạo dòng chi tiết.

---

### 5. Red-Green-Refactor Tracker

> **Format 3-phase:** Mỗi test case ghi đầy đủ trạng thái 🔴 RED, 🟢 GREEN, 🔵 REFACTOR kèm commit hash + ngày thực hiện.
> *Note: Các test này được thực hiện theo Retroactive TDD.*

| UC    | TC ID      | Mô tả ngắn                                                        | Test File                         | 🔴 RED | 🔴 Date | 🟢 GREEN | 🟢 Date | 🔵 REFACTOR | 🔵 Note |
| ----- | ---------- | -------------------------------------------------------------------- | --------------------------------- | ------ | ------- | -------- | ------- | ----------- | ------- |
| UC-17 | TC-M3-001  | Normal Flow: Room Service đặt thành công, Credit Limit trừ đúng | `PosApiControllerUC17Test.java` | [x]    | 26-06-19| [x]      | 26-06-19| [x]         | ✅ Retroactive |
| UC-17 | TC-M3-001b | AF-01: Đặt nhiều món cùng lúc tính tổng chính xác               | `PosApiControllerUC17Test.java` | [x]    | 26-06-19| [x]      | 26-06-19| [x]         | ✅ Retroactive |
| UC-17 | TC-M3-001c | AF-02: Ghi chú đặc biệt lưu vào đơn hàng                        | `PosApiControllerUC17Test.java` | [x]    | 26-06-19| [x]      | 26-06-19| [x]         | ✅ Retroactive |
| UC-17 | TC-M3-001d | E-01: Credit Limit không đủ → Reject HTTP 400                    | `PosApiControllerUC17Test.java` | [x]    | 26-06-19| [x]      | 26-06-19| [x]         | ✅ Retroactive |
| UC-17 | TC-M3-001e | E-02: Phòng không tồn tại → Reject HTTP 400                      | `PosApiControllerUC17Test.java` | [x]    | 26-06-19| [x]      | 26-06-19| [x]         | ✅ Retroactive |
| UC-17 | TC-M3-001f | E-03: Món ăn không tồn tại → Reject HTTP 400                    | `PosApiControllerUC17Test.java` | [x]    | 26-06-19| [x]      | 26-06-19| [x]         | ✅ Retroactive |

---

### 6. Entry / Exit Criteria

#### Entry Criteria
- [x] Code base setup hoàn chỉnh.
- [x] RoomBooking repository đã có hàm lấy CreditLimit.

#### Exit Criteria — Definition of Done (DoD)
- [x] Unit tests pass 100% (6/6 test cases cho UC-17).
- [x] Không có lỗi compilation, không có FIXME trong các test code.
- [x] `mvn test -Dtest=PosApiControllerUC17Test` trả về SUCCESS.

---

### 7. Rollback Plan

Nếu phát hiện lỗi logic sai lệch CreditLimit trên production do UC-17:

```bash
git checkout -- src/main/java/com/kawai/controllers/api/PosApiController.java
```

**Khắc phục nhanh dữ liệu sai:**
Cộng ngược lại số tiền trừ sai vào bảng `room_bookings.credit_limit`. Xóa các `food_orders` phát sinh do lỗi.