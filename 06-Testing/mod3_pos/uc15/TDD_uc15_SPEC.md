# TEST-DRIVEN DEVELOPMENT SPECIFICATION — UC15: XÁC NHẬN THANH TOÁN ĐƠN HÀNG (PAYMENT CONFIRMATION)

## Mẫu Đặc tả Kiểm thử Hướng Phát triển — KAWAI RETREAT RESORT & HUB

| Field                    | Value                                                |
| ------------------------ | ---------------------------------------------------- |
| **Document ID**    | `KAWAI-TDD-UC15-001`                               |
| **Version**        | 1.0                                                  |
| **Date**           | 2026-07-02                                           |
| **Status**         | Approved                                             |
| **Standard**       | ISO/IEC/IEEE 29119-3:2021 — Software Testing Part 3 |
| **Author**         | Trịnh Minh Đức                                     |
| **Reviewed by**    | Nguyễn Xuân Lưu                                   |
| **Approved by**    | [ ] Pending                                          |
| **Classification** | Internal — Confidential                             |

**References:**

* `06-Testing/mod3_pos/uc15/EDS_UC15_Xac_Nhan_Thanh_Toan.md` — UC15 EDS Spec
* `06-Testing/MASTER_TDD_SPEC.md` — Master TDD Spec
* `06-Testing/mod3_pos/TDD_MOD3_SPEC.md` — TDD Module 3

---

## CHANGELOG

| Ngày       | Người thực hiện | Nội dung thay đổi                                                              |
| ---------- | --------------- | ------------------------------------------------------------------------------- |
| 2026-07-02 | Trịnh Minh Đức  | Khởi tạo TDD Spec cho UC15 — Xác nhận thanh toán đơn hàng (Payment Confirmation)|

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
| **Feature / Gap ID**    | `GAP-MOD3-UC15`                                                  |
| **Use Case**            | UC-15 — Xác nhận thanh toán đơn hàng                        |
| **Module**              | Module 3 — POS & Nhà hàng                                    |
| **Priority**            | 🔴 P0 — Critical                                                  |
| **Sprint**              | S3                                                                |
| **Data Classification** | Internal / Financial                                               |
| **Compliance Scope**    | POS-006                                                           |

---

## 2. Logic Issues Resolved

| #  | Spec gốc (sai / thiếu)                                                                       | Thực tế (schema / policy)                                                             | Fix áp dụng trong test                                                                     |
| -- | ---------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------- |
| L1 | Đơn hàng sang thẳng Completed sau khi pay                                                     | Đơn AWAITING_PAYMENT thanh toán xong chuyển sang Pending (kitchen còn nấu)             | Assert orderStatus = Pending.                                                               |
| L2 | Trạng thái bàn sau thanh toán là Available                                                     | Trạng thái bàn là Cleaning để nhân viên dọn bàn                                         | Assert tableStatus = Cleaning.                                                              |
| L3 | Không liên kết tự động với Reservation                                                        | Cần tự động cập nhật Reservation (nếu có) sang Completed                                | Assert reservation status được set thành Completed nếu có lịch đặt trong ngày.              |

---

## 3. Test Design Specification (TDS)

### TDS-01 — Scope / Phạm vi

```
Feature: Payment Confirmation (UC-15)
├── Service Layer: PosServiceImpl.payOrder()
├── Repository:
│   ├── FoodOrderRepository
│   ├── RestaurantTableRepository (cập nhật Cleaning)
│   └── TableReservationRepository (cập nhật Completed)
```

### TDS-03 — Test Conditions and Coverage Items

| Condition ID    | Test Condition                                                      | Coverage Item                                  | Test Cases               |
| --------------- | ------------------------------------------------------------------- | ---------------------------------------------- | ------------------------ |
| TC-COND-UC15-01 | Thanh toán thành công đơn Dine-In                                  | `PosServiceImpl.payOrder()`                    | TC-UC15-001              |
| TC-COND-UC15-02 | Thanh toán đơn không tồn tại (POS-006)                             | Exception validation                           | TC-UC15-002              |

---

## 4. Test Case Specification

### TC-UC15-001 — Thanh toán thành công (Update table & reservation)

**Severity:** CRITICAL
**CWE:** N/A
**Feature Under Test:** `PosServiceImpl.payOrder()`
**Test File:** `src/test/java/com/kawai/services/PosServiceUC15Test.java`
**TDD Phase:** 🟢 GREEN

**Preconditions:**
* Order `ORD-100` đang có `orderStatus = AWAITING_PAYMENT`, `isPaidInPos = false`.
* Bàn "T10" liên kết với ORD-100 đang có trạng thái `OCCUPIED`.
* Có `TableReservation` "RSV-005" của bàn "T10" đang ở trạng thái `SEATED`.
<<<<<<< HEAD
* Method payment truyền lên là `CASH`.

**Test Steps:**
1. Khởi tạo mock `FoodOrderRepository.findById(100L)` trả về `ORD-100`.
2. Khởi tạo mock `RestaurantTableRepository.findById(10L)` trả về bàn `T10`.
3. Khởi tạo mock `TableReservationRepository.findByTable_IdAndStatus(...)` trả về `RSV-005`.
4. Gọi `posService.payOrder(100L, PaymentMethod.CASH)`.
5. Sử dụng `verify` để đảm bảo `tableReservationRepository.save()` được gọi với đối tượng mang trạng thái `COMPLETED`.
6. Sử dụng `verify` để đảm bảo `restaurantTableRepository.save()` được gọi với trạng thái bàn là `CLEANING`.
7. Sử dụng `verify` để đảm bảo `foodOrderRepository.save()` lưu lại đơn đã được đánh dấu là đã trả tiền.

**Expected Result (PASS):**
* `order.isPaidInPos()` trả về `true`.
* `order.getOrderStatus()` trả về `PENDING` (chờ bếp làm xong món, nếu có món, hoặc hoàn tất luôn nếu xong).
* `table.getStatus()` trả về `CLEANING`.
* `reservation.getStatus()` trả về `COMPLETED`.
=======

**Test Steps:**
1. Mock dữ liệu trả về cho `ORD-100`, bàn `T10`, và reservation `RSV-005`.
2. Gọi `payOrder(100, paymentMethod)`.
3. Assert `Order.isPaidInPos == true` và `Order.orderStatus == PENDING`.
4. Assert Bàn T10 `status = CLEANING` và `cleaningStartTime` được ghi nhận.
5. Assert Reservation RSV-005 `status = COMPLETED` và `endTime` được ghi nhận.

**Expected Result (PASS):**
* Thanh toán thành công, thay đổi trạng thái đồng loạt cho Order, Table và Reservation.

**Expected Result (FAIL):**
* Trạng thái Bàn không được chuyển sang CLEANING dẫn đến khách mới ngồi vào bàn chưa dọn.
* `isPaidInPos` không bật true khiến báo cáo tài chính sai.
>>>>>>> 7414e299dc9443033140483710eb1236086b60de

---

### TC-UC15-002 — Thanh toán đơn không tồn tại (POS-006)

**Severity:** HIGH
**CWE:** CWE-20 (Improper Input Validation)
**Feature Under Test:** `PosServiceImpl.payOrder()`
**Test File:** `src/test/java/com/kawai/services/PosServiceUC15Test.java`
**TDD Phase:** 🟢 GREEN

**Preconditions:**
* Nhập `orderId` là `999` (không tồn tại trong hệ thống).

**Test Steps:**
1. Mock `FoodOrderRepository.findById(999)` trả về `Optional.empty()`.
2. Gọi `payOrder(999, paymentMethod)`.
3. Assert Exception ném ra với mã lỗi `POS-006`.

**Expected Result (PASS):**
* Ném lỗi `ResourceNotFoundException`: "POS-006: Đơn hàng không tồn tại hoặc đã kết thúc."

**Expected Result (FAIL):**
* Hệ thống bị NullPointerException hoặc trả về lỗi không rõ ràng.

---

## 5. Red-Green-Refactor Tracker

| UC   | TC ID       | Mô tả ngắn                                                    | Test File                                    | 🔴 RED | 🟢 GREEN | 🔵 REFACTOR |
| ---- | ----------- | ------------------------------------------------------------- | -------------------------------------------- | ------ | -------- | ----------- |
| UC15 | TC-UC15-001 | Thanh toán thành công, cập nhật Bàn và Lịch đặt                | `PosServiceUC15Test.java`                    | [x]    | [x]      | [x]         |
| UC15 | TC-UC15-002 | Thanh toán đơn không tồn tại (POS-006)                         | `PosServiceUC15Test.java`                    | [x]    | [x]      | [x]         |

---

## 6. Entry / Exit Criteria

### Exit Criteria / Definition of Done (DoD)
- [x] **2/2 test cases** trong tracker chuyển sang trạng thái 🟢 GREEN.
- [x] Code coverage ≥ 80% cho Use Case.

---

## 7. Rollback Plan
| Tình huống                                           | Hành động                                                                        |
| ------------------------------------------------------ | ----------------------------------------------------------------------------------- |
| Lỗi cập nhật TableStatus sau khi đã update Order       | Đảm bảo method `payOrder` có `@Transactional` để rollback toàn bộ logic.           |