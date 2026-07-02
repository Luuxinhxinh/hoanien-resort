# TEST-DRIVEN DEVELOPMENT SPECIFICATION — UC23: TRẠNG THÁI & GỌI THÊM

## Mẫu Đặc tả Kiểm thử Hướng Phát triển — KAWAI RETREAT RESORT & HUB

| Field                    | Value                                                |
| ------------------------ | ---------------------------------------------------- |
| **Document ID**    | `KAWAI-TDD-UC23-001`                               |
| **Version**        | 1.0                                                  |
| **Date**           | 2026-07-02                                           |
| **Status**         | Approved                                             |
| **Standard**       | ISO/IEC/IEEE 29119-3:2021 — Software Testing Part 3 |
| **Author**         | Trịnh Minh Đức                                     |
| **Reviewed by**    | Nguyễn Xuân Lưu                                   |
| **Approved by**    | [ ] Pending                                          |
| **Classification** | Internal — Confidential                             |

**References:**
* `EDS_UC23_Cap_Nhat_Don_Hang_Goi_Them_Mon.md`

> **Quy ước TDD:** Thứ tự bắt buộc: viết test (.java) → chạy → xác nhận FAIL 🔴 → implement → PASS 🟢 → refactor 🔵.

---

## CHANGELOG

| Ngày       | Người thực hiện | Nội dung thay đổi                                                              |
| ---------- | --------------- | ------------------------------------------------------------------------------- |
| 2026-07-02 | Trịnh Minh Đức  | Khởi tạo TDD Spec cho UC23 — Cập nhật trạng thái đơn hàng & Gọi thêm món      |

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
| **Feature / Gap ID**    | `GAP-MOD3-UC23`                                                  |
| **Use Case**            | UC-23 — Cập nhật trạng thái & Gọi thêm món                  |
| **Compliance Scope**    | POS-004, POS-005, POS-006, POS-010                                |

---

## 2. Logic Issues Resolved

| #  | Spec gốc (sai / thiếu)                                                                       | Thực tế (schema / policy)                                                             | Fix áp dụng trong test                                                                     |
| -- | ---------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------- |
| L1 | Cho phép gọi thêm vào Room Service                                                             | Room Service đã tính tiền, gọi thêm phải tạo đơn mới (POS-005)                          | Assert lỗi khi add items vào đơn room-svc.                                                  |
| L2 | Trạng thái KOT không đồng bộ                                                                   | Cập nhật cascade OrderStatus -> KOT Status                                              | Assert danh sách FoodOrderDetail cập nhật đúng.                                             |

---

## 3. Test Case Specification

### TC-UC23-001 — Cascade Status Pending -> Preparing

**Severity:** CRITICAL
**CWE:** N/A
**Feature Under Test:** `PosServiceImpl.updateOrderStatus()`
**Test File:** `src/test/java/com/kawai/services/PosServiceUC23Test.java`
**TDD Phase:** 🟢 GREEN

**Preconditions:**
* Đơn hàng có `orderStatus = PENDING`.
* Kèm theo 3 `FoodOrderDetail` (KOT) bên trong đang có `kotStatus = PENDING`.

**Test Steps:**
1. Gọi service chuyển trạng thái đơn hàng sang `PREPARING`.
2. Lấy đơn hàng từ DB lên.
3. Assert `orderStatus` thành `PREPARING`.
4. Assert tất cả 3 `FoodOrderDetail` chuyển thành `PREPARING`.

**Expected Result (PASS):**
* Order và toàn bộ KOT chuyển sang Preparing đồng loạt (Cascade Update).

**Expected Result (FAIL):**
* Đơn đổi trạng thái nhưng bếp (KOT) vẫn kẹt ở Pending.

---

### TC-UC23-002 — Gọi thêm món cho Room Service (POS-005)

**Severity:** HIGH
**CWE:** N/A
**Feature Under Test:** `PosServiceImpl.addItemsToOrder()`
**Test File:** `src/test/java/com/kawai/services/PosServiceUC23Test.java`
**TDD Phase:** 🟢 GREEN

**Preconditions:**
* Đơn hàng đang ở dạng `orderType = ROOM_SERVICE`.

**Test Steps:**
1. Gọi API add thêm món ăn vào đơn hàng này.
2. Assert Exception.

**Expected Result (PASS):**
* Ném lỗi: "POS-005: Không hỗ trợ gọi thêm món cho đơn hàng Room Service. Vui lòng tạo đơn mới".

**Expected Result (FAIL):**
* Cho phép gọi thêm nhưng số tiền không được trừ vào hạn mức phòng (vì FolioItem cũ đã fix số tiền).

---

### TC-UC23-003 — Room Service Served tự thanh toán

**Severity:** HIGH
**CWE:** N/A
**Feature Under Test:** `PosServiceImpl.updateOrderStatus()`
**Test File:** `src/test/java/com/kawai/services/PosServiceUC23Test.java`
**TDD Phase:** 🟢 GREEN

**Preconditions:**
* Đơn hàng Room Service đang `PREPARING`. `isPaidInPos = false`.

**Test Steps:**
1. Chuyển trạng thái sang `SERVED`.
2. Assert trạng thái đơn là `SERVED`.
3. Assert field `isPaidInPos` tự động bật lên `true`.

**Expected Result (PASS):**
* Room service đã Served là coi như hoàn tất chu trình thanh toán (vì tiền gán vào phòng).

**Expected Result (FAIL):**
* Đơn vẫn `isPaidInPos = false`, gây khó khăn cho việc lọc doanh thu hoặc báo cáo.

---

## 4. Red-Green-Refactor Tracker

| UC   | TC ID       | Mô tả ngắn                                                    | Test File                                    | 🔴 RED | 🟢 GREEN | 🔵 REFACTOR |
| ---- | ----------- | ------------------------------------------------------------- | -------------------------------------------- | ------ | -------- | ----------- |
| UC23 | TC-UC23-001 | Cascade status cập nhật KOT                                    | `PosServiceUC23Test.java`                    | [x]    | [x]      | [x]         |
| UC23 | TC-UC23-002 | Chặn Add items Room Service (POS-005)                          | `PosServiceUC23Test.java`                    | [x]    | [x]      | [x]         |
| UC23 | TC-UC23-003 | Auto isPaidInPos khi Room Service Served                       | `PosServiceUC23Test.java`                    | [x]    | [x]      | [x]         |

---

## 5. Entry / Exit Criteria

### Exit Criteria / Definition of Done (DoD)
- [x] **3/3 test cases** trong tracker chuyển sang trạng thái 🟢 GREEN.
- [x] Code coverage ≥ 80% cho Use Case.

---

## 6. Rollback Plan
| Tình huống                                           | Hành động                                                                        |
| ------------------------------------------------------ | ----------------------------------------------------------------------------------- |
| Cascade update mất quá nhiều tài nguyên database     | Chuyển sang Native Query Update thay vì lấy từng record lên và update. |
