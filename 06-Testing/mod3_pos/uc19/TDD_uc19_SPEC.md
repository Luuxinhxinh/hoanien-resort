# TEST-DRIVEN DEVELOPMENT SPECIFICATION — UC19: HỦY ĐƠN HÀNG (CANCEL FOOD ORDER)

## Mẫu Đặc tả Kiểm thử Hướng Phát triển — KAWAI RETREAT RESORT & HUB

| Field                    | Value                                                |
| ------------------------ | ---------------------------------------------------- |
| **Document ID**    | `KAWAI-TDD-UC19-001`                               |
| **Version**        | 1.0                                                  |
| **Date**           | 2026-07-02                                           |
| **Status**         | Approved                                             |
| **Standard**       | ISO/IEC/IEEE 29119-3:2021 — Software Testing Part 3 |
| **Author**         | Trịnh Minh Đức                                     |
| **Reviewed by**    | Nguyễn Xuân Lưu                                   |
| **Approved by**    | [ ] Pending                                          |
| **Classification** | Internal — Confidential                             |

**References:**
* `EDS_UC19_Huy_Don_Hang.md`

> **Quy ước TDD:** Thứ tự bắt buộc: viết test (.java) → chạy → xác nhận FAIL 🔴 → implement → PASS 🟢 → refactor 🔵.

---

## CHANGELOG

| Ngày       | Người thực hiện | Nội dung thay đổi                                                              |
| ---------- | --------------- | ------------------------------------------------------------------------------- |
| 2026-07-02 | Trịnh Minh Đức  | Khởi tạo TDD Spec cho UC19 — Hủy đơn hàng F&B (Cancel Food Order)             |

---

## MỤC LỤC
1. [Thông tin Module](#1-thong-tin-module)
2. [Logic Issues Resolved](#2-logic-issues-resolved)
3. [Test Case Specification](#3-test-case-specification)
4. [Red-Green-Refactor Tracker](#4-red-green-refactor-tracker)
5. [Entry / Exit Criteria](#5-entry--exit-criteria)
6. [Rollback Plan](#6-rollback-plan)

---

## 1. Thông tin Module

| Field                         | Value                                                              |
| ----------------------------- | ------------------------------------------------------------------ |
| **Feature / Gap ID**    | `GAP-MOD3-UC19`                                                  |
| **Use Case**            | UC-19 — Hủy đơn hàng F&B                                    |
| **Compliance Scope**    | POS-004, POS-006                                                  |

---

## 2. Logic Issues Resolved

| #  | Spec gốc (sai / thiếu)                                                                       | Thực tế (schema / policy)                                                             | Fix áp dụng trong test                                                                     |
| -- | ---------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------- |
| L1 | Chưa rõ hoàn tiền thế nào cho Charge to Room                                                   | Charge to Room không hoàn bằng tiền mặt mà tự động xoá FolioItem trả lại hạn mức        | Assert Repository.delete(folioItem).                                                        |
| L2 | Hủy đơn đang nấu                                                                               | POS-004 cấm hủy nếu đơn hoặc KOT đang Preparing                                         | Assert Exception khi hủy đơn Preparing.                                                     |

---

## 3. Test Case Specification

### TC-UC19-001 — Hủy đơn Charge to Room thành công
**Severity:** CRITICAL
**CWE:** N/A
**Feature Under Test:** `PosServiceImpl.cancelOrder()`
**Test File:** `src/test/java/com/kawai/services/PosServiceUC19Test.java`
**TDD Phase:** 🟢 GREEN

**Preconditions:**
* Bảng `Food_Orders` có bản ghi `ORD-001` đang ở trạng thái `PENDING`.
* Cột `paymentType` của đơn hàng này là `CHARGE_TO_ROOM`.
* Bảng `Folio_Items` có bản ghi `FL-100` liên kết trực tiếp với đơn `ORD-001` thông qua `source_id` hoặc mapping table, với số tiền 500,000 VND.

**Test Steps:**
1. Khởi tạo mock `FoodOrderRepository.findById(1L)` trả về `ORD-001`.
2. Khởi tạo mock `FolioRepository.findByFoodOrderId(1L)` trả về `FL-100`.
3. Khởi tạo mock `SimpMessagingTemplate` để theo dõi WebSocket event.
4. Gọi method `posService.cancelOrder(1L, "Khách đổi ý")`.
5. Sử dụng Mockito `verify()` để đảm bảo `folioRepository.delete(FL-100)` được gọi chính xác 1 lần.
6. Sử dụng Mockito `verify()` để đảm bảo thông điệp hủy được bắn qua WebSocket vào topic `/topic/kds/orders`.

**Expected Result (PASS):**
* Hàm không quăng ngoại lệ.
* `order.getOrderStatus()` bằng `CANCELLED`.
* `folioItem` bị xóa (trả lại hạn mức).

---

### TC-UC19-002 — Hủy đơn VNPAY thành công (Yêu cầu Refund)
**Severity:** HIGH
**CWE:** N/A
**Feature Under Test:** `PosServiceImpl.cancelOrder()` và `VnpayService` Integration
**Test File:** `src/test/java/com/kawai/services/PosServiceUC19Test.java`
**TDD Phase:** 🟢 GREEN

**Preconditions:**
* Đơn `ORD-002` (giá trị 1,000,000 VND) đã thanh toán qua VNPAY (`paymentType = VNPAY`, `isPaidInPos = true`).
* Trạng thái đơn: `PENDING`.
* Cổng thanh toán VNPAY mock server trả về `200 OK` cho Refund Request.

**Test Steps:**
1. Khởi tạo mock `FoodOrderRepository.findById(2L)` trả về `ORD-002`.
2. Khởi tạo mock `VnpayService.processRefund(...)` trả về `RefundResponse(status=SUCCESS)`.
3. Gọi method `posService.cancelOrder(2L, "Khách bận việc")`.
4. Assert method `vnpayService.processRefund` được gọi với đúng tham số (`orderId=2, amount=1000000`).

**Expected Result (PASS):**
* `order.getOrderStatus()` bằng `CANCELLED`.
* Refund Request ghi nhận thành công, khách hàng nhận lại tiền qua gateway.

---

### TC-UC19-003 — Chặn hủy đơn khi bếp đang nấu (POS-004)
**Severity:** HIGH
**CWE:** CWE-841 (Improper Enforcement of Behavioral Workflow)
**Feature Under Test:** Kiểm soát vòng đời đơn hàng (Order Lifecycle)
**Test File:** `src/test/java/com/kawai/services/PosServiceUC19Test.java`
**TDD Phase:** 🟢 GREEN

**Preconditions:**
* Đơn `ORD-003` có trạng thái hiện tại là `PREPARING` (Bếp đã nhận order và đang làm món).

**Test Steps:**
1. Khởi tạo mock `FoodOrderRepository.findById(3L)` trả về `ORD-003` (status = PREPARING).
2. Dùng hàm `assertThrows` của JUnit 5 bao bọc lời gọi `posService.cancelOrder(3L, "Đợi lâu quá")`.
3. Kiểm tra Exception trả về.

**Expected Result (PASS):**
* Ném lỗi `BusinessLogicException`.
* Message lỗi chứa mã: `POS-004: Không thể hủy đơn đang được chế biến`.
* Không có thao tác ghi Database nào (như đổi status hay xoá Folio) xảy ra.

---

### TC-UC19-004 — Chặn hủy đơn đã thanh toán nhưng chưa có Refund Gateway (Fall-back)
**Severity:** MEDIUM
**CWE:** N/A
**Feature Under Test:** Validations
**Test File:** `src/test/java/com/kawai/services/PosServiceUC19Test.java`
**TDD Phase:** 🟢 GREEN

**Test Steps:**
1. Gọi hủy đơn `ORD-004` (paymentType = CASH) nhưng status là `COMPLETED`.
2. Hệ thống phải ném lỗi cấm hủy đơn đã hoàn thành.

**Expected Result (PASS):**
* Ném lỗi `BusinessLogicException: "Không thể hủy đơn đã hoàn thành và xuất hóa đơn."`

---

## 4. Red-Green-Refactor Tracker

| UC   | TC ID       | Mô tả ngắn                                                    | Test File                                    | 🔴 RED | 🟢 GREEN | 🔵 REFACTOR |
| ---- | ----------- | ------------------------------------------------------------- | -------------------------------------------- | ------ | -------- | ----------- |
| UC19 | TC-UC19-001 | Hủy đơn Charge to Room, xóa FolioItem                          | `PosServiceUC19Test.java`                    | [x]    | [x]      | [x]         |
| UC19 | TC-UC19-002 | Hủy đơn VNPAY, tạo RefundRequest                               | `PosServiceUC19Test.java`                    | [x]    | [x]      | [x]         |
| UC19 | TC-UC19-003 | Ngăn hủy đơn đang Preparing (POS-004)                          | `PosServiceUC19Test.java`                    | [x]    | [x]      | [x]         |

---

## 5. Entry / Exit Criteria

### Exit Criteria / Definition of Done (DoD)
- [x] **3/3 test cases** trong tracker chuyển sang trạng thái 🟢 GREEN.
- [x] Code coverage ≥ 80% cho Use Case.

---

## 6. Rollback Plan
| Tình huống                                           | Hành động                                                                        |
| ------------------------------------------------------ | ----------------------------------------------------------------------------------- |
| Không xóa được FolioItem khi hủy đơn                 | Kiểm tra CascadeType và mapping logic giữa FoodOrder và FolioItem. |
