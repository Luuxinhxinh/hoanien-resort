# TEST-DRIVEN DEVELOPMENT SPECIFICATION — UC14: QUẢN LÝ ĐƠN HÀNG F&B (F&B ORDER MANAGEMENT)

## Mẫu Đặc tả Kiểm thử Hướng Phát triển — KAWAI RETREAT RESORT & HUB

| Field                    | Value                                                |
| ------------------------ | ---------------------------------------------------- |
| **Document ID**    | `KAWAI-TDD-UC14-001`                               |
| **Version**        | 1.0                                                  |
| **Date**           | 2026-07-02                                           |
| **Status**         | Approved                                             |
| **Standard**       | ISO/IEC/IEEE 29119-3:2021 — Software Testing Part 3 |
| **Author**         | Trịnh Minh Đức                                     |
| **Reviewed by**    | Nguyễn Xuân Lưu                                   |
| **Approved by**    | [ ] Pending                                          |
| **Classification** | Internal — Confidential                             |

**References:**

* `06-Testing/mod3_pos/uc14/EDS_UC14_Quan_Ly_Don_Hang_FnB.md` — UC14 EDS Spec
* `06-Testing/MASTER_TDD_SPEC.md` — Master TDD Spec
* `06-Testing/mod3_pos/TDD_MOD3_SPEC.md` — TDD Module 3
* `05-Development/kawai-backend` — Backend Spring Boot

> **Quy ước TDD:** Tài liệu này mô tả test cases TRƯỚC khi viết production code.
> Thứ tự bắt buộc: viết test (.java) → chạy → xác nhận FAIL 🔴 → implement → PASS 🟢 → refactor 🔵.

---

## CHANGELOG

| Ngày       | Người thực hiện | Nội dung thay đổi                                                              |
| ---------- | --------------- | ------------------------------------------------------------------------------- |
| 2026-07-02 | Trịnh Minh Đức  | Khởi tạo TDD Spec cho UC14 — Quản lý đơn hàng F&B (F&B Order Management)      |

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
| **Feature / Gap ID**    | `GAP-MOD3-UC14`                                                  |
| **Use Case**            | UC-14 — Quản lý đơn hàng F&B (F&B Order Management)         |
| **Module**              | Module 3 — POS & Nhà hàng                                    |
| **Priority**            | 🔴 P0 — Critical                                                  |
| **Sprint**              | S3 (2026-07-01 → 2026-07-15)                                      |
| **Milestone**           | M3 Alpha — 2026-07-11                                             |
| **Data Classification** | Internal / Operational                                             |
| **Compliance Scope**    | POS-001, POS-007, POS-008, POS-009                                |

---

## 2. Logic Issues Resolved

| #  | Spec gốc (sai / thiếu)                                                                       | Thực tế (schema / policy)                                                             | Fix áp dụng trong test                                                                     |
| -- | ---------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------- |
| L1 | Chưa rõ xử lý khi số khách bằng 0 hoặc âm                                                  | Party size phải > 0 (POS-008)                                                           | Thêm test chặn tạo đơn nếu số khách <= 0.                                                   |
| L2 | Thiếu ràng buộc về trạng thái bàn khi tạo đơn Dine-In                                         | Bàn phải ở trạng thái Available (POS-007)                                               | Thêm test chặn tạo đơn nếu bàn đang Occupied hoặc Out_of_service.                             |
| L3 | Thiếu ràng buộc thời gian tạo đơn Dine-In                                                    | Không nhận khách Dine-In từ 23:00 đến 08:00 (POS-009)                                   | Thêm test với mock time ngoài giờ hoạt động.                                                |
| L4 | Chưa quy định rõ trạng thái KOT của món ăn sau khi tạo đơn                                 | Mọi món trong đơn tạo mới đều có KOT status = Pending                                   | Assert tất cả FoodOrderDetail.kotStatus = Pending.                                          |

---

## 3. Test Design Specification (TDS)

### TDS-01 — Scope / Phạm vi

```
Feature: F&B Order Management (UC-14)
├── Service Layer: PosServiceImpl.createOrder()
├── Repository:
│   ├── FoodOrderRepository
│   ├── FoodOrderDetailRepository
│   ├── RestaurantTableRepository (kiểm tra status bàn)
│   └── MenuItemRepository (lấy giá trị menu items)
└── Integration:
    ├── Table Management (cập nhật status)
    └── Business Rules validation (POS-001, 007, 008, 009)
```

### TDS-02 — Test Basis / Cơ sở Kiểm thử

| Source                        | Items Derived                                                                                             |
| ----------------------------- | --------------------------------------------------------------------------------------------------------- |
| SRS UC14 — Normal Flow       | Tạo đơn hàng, cập nhật bàn sang Occupied, lưu chi tiết món ăn (KOT)                              |
| SRS UC14 — Exceptions        | EX1 (items rỗng), EX2 (bàn không trống), EX3 (khách <= 0), EX4 (ngoài giờ làm việc)            |
| Business Rules                | POS-001, POS-007, POS-008, POS-009                                                                        |

### TDS-03 — Test Conditions and Coverage Items

| Condition ID    | Test Condition                                                      | Coverage Item                                  | Test Cases               |
| --------------- | ------------------------------------------------------------------- | ---------------------------------------------- | ------------------------ |
| TC-COND-UC14-01 | Tạo đơn thành công                                                 | `PosServiceImpl.createOrder()`                 | TC-UC14-001              |
| TC-COND-UC14-02 | Tạo đơn khi items rỗng (POS-001)                                   | Item list validation                           | TC-UC14-002              |
| TC-COND-UC14-03 | Tạo đơn Dine-In khi bàn không trống (POS-007)                      | Table status validation                        | TC-UC14-003              |
| TC-COND-UC14-04 | Số khách <= 0 (POS-008)                                            | Party size validation                          | TC-UC14-004              |
| TC-COND-UC14-05 | Tạo đơn Dine-In từ 23:00-08:00 (POS-009)                           | Business hours validation                      | TC-UC14-005              |

---

## 4. Test Case Specification

### TC-UC14-001 — Tạo đơn hàng Dine-In thành công

**Severity:** CRITICAL
**CWE:** N/A
**Feature Under Test:** `PosServiceImpl.createOrder()`, `RestaurantTableRepository`
**Test File:** `src/test/java/com/kawai/services/PosServiceUC14Test.java`
**TDD Phase:** 🟢 GREEN

**Preconditions:**
* Bàn "T01" (capacity = 4) đang có trạng thái `AVAILABLE`.
* Món "Phở bò" (giá 100,000 VND) đang có sẵn (`isAvailable = true`).
* Request Body: `partySize = 2`, chọn bàn T01, thêm 2 bát Phở bò.
* Thời gian hệ thống: 10:00 AM (trong giờ hoạt động nhà hàng).

**Test Steps:**
1. Khởi tạo mock `RestaurantTableRepository.findById(1L)` trả về bàn T01.
2. Khởi tạo mock `MenuItemRepository.findById(10L)` trả về đối tượng MenuItem "Phở bò" (price = 100,000).
3. Gọi hàm `posService.createOrder(requestDto)`.
4. Dùng `ArgumentCaptor` để hứng đối tượng `FoodOrder` được truyền vào `foodOrderRepository.save()`.
5. Kiểm tra trạng thái của bàn xem có được set thành `OCCUPIED` không (gọi `restaurantTableRepository.save()`).

**Expected Result (PASS):**
* `foodOrder.getOrderStatus()` bằng `PENDING`.
* `foodOrder.getTotalAmount()` bằng `200,000` VND.
* `foodOrder.getFoodOrderDetails().size()` bằng 2, và mỗi chi tiết đều có `kotStatus` = `PENDING`.
* Bàn chuyển trạng thái thành `OCCUPIED`.

**Expected Result (FAIL):**
* Đơn được tạo nhưng bàn không đổi trạng thái.
* Tổng tiền tính sai do lấy sai giá từ DB.

---

### TC-UC14-002 — Bàn không trống (POS-007)

**Severity:** HIGH
**CWE:** CWE-841 (Improper Enforcement of Behavioral Workflow)
**Feature Under Test:** `Table Status Validation`
**Test File:** `src/test/java/com/kawai/services/PosServiceUC14Test.java`
**TDD Phase:** 🟢 GREEN

**Preconditions:**
* Bàn "T02" đang có trạng thái "OCCUPIED" hoặc "CLEANING".

**Test Steps:**
1. Mock `RestaurantTableRepository.findById(2)` trả về bàn T02 (OCCUPIED).
2. Gọi `createOrder` với bàn T02.
3. Assert Exception được ném ra với message phù hợp.

**Expected Result (PASS):**
* Ném lỗi `BusinessLogicException`: "POS-007: Bàn không ở trạng thái khả dụng".
* Không có đơn hàng nào được tạo trong DB.

**Expected Result (FAIL):**
* Vẫn tạo đơn cho bàn đang OCCUPIED dẫn đến conflict dữ liệu bàn.

---

### TC-UC14-003 — Đặt Dine-in ngoài giờ hoạt động (POS-009)

**Severity:** HIGH
**CWE:** N/A
**Feature Under Test:** `Business Hours Validation`
**Test File:** `src/test/java/com/kawai/services/PosServiceUC14Test.java`
**TDD Phase:** 🟢 GREEN

**Preconditions:**
* Thời gian hiện tại hệ thống là `23:30` (ngoài giờ phục vụ).

**Test Steps:**
1. Dùng library mock thời gian (vd: `Clock` hoặc fixed time) về 23:30.
2. Gọi `createOrder` với loại đơn `DINE_IN`.
3. Assert Exception được ném ra.

**Expected Result (PASS):**
* Ném lỗi `BusinessLogicException`: "POS-009: Không nhận đặt bàn / phục vụ tại nhà hàng từ 23:00 đến 08:00".

**Expected Result (FAIL):**
* Hệ thống vẫn nhận đơn Dine-In lúc nửa đêm.

---

## 5. Red-Green-Refactor Tracker

| UC   | TC ID       | Mô tả ngắn                                                    | Test File                                    | 🔴 RED | 🟢 GREEN | 🔵 REFACTOR | 🔵 Note |
| ---- | ----------- | ------------------------------------------------------------- | -------------------------------------------- | ------ | -------- | ----------- | ------- |
| UC14 | TC-UC14-001 | Tạo đơn thành công, cập nhật trạng thái bàn                    | `PosServiceUC14Test.java`                    | [x]    | [x]      | [x]         |         |
| UC14 | TC-UC14-002 | Bàn không khả dụng (POS-007)                                   | `PosServiceUC14Test.java`                    | [x]    | [x]      | [x]         |         |
| UC14 | TC-UC14-003 | Dine-in ngoài giờ (POS-009)                                    | `PosServiceUC14Test.java`                    | [x]    | [x]      | [x]         |         |

---

## 6. Entry / Exit Criteria

### Exit Criteria / Definition of Done (DoD)
- [x] **3/3 test cases** trong tracker chuyển sang trạng thái 🟢 GREEN.
- [x] Code coverage ≥ 80% cho Use Case.

---

## 7. Rollback Plan
| Tình huống                                           | Hành động                                                                        |
| ------------------------------------------------------ | ----------------------------------------------------------------------------------- |
| Tạo KOT thất bại do Timeout Database                 | Đảm bảo `@Transactional` sẽ rollback toàn bộ việc tạo Order và Table Status.       |