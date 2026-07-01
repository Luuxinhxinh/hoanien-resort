# TEST-DRIVEN DEVELOPMENT SPECIFICATION — UC16: QUẢN LÝ BÀN ĂN (TABLE MANAGEMENT)

## Mẫu Đặc tả Kiểm thử Hướng Phát triển — KAWAI RETREAT RESORT & HUB

| Field                    | Value                                                |
| ------------------------ | ---------------------------------------------------- |
| **Document ID**    | `KAWAI-TDD-UC16-001`                               |
| **Version**        | 1.0                                                  |
| **Date**           | 2026-07-02                                           |
| **Status**         | Approved                                             |
| **Standard**       | ISO/IEC/IEEE 29119-3:2021 — Software Testing Part 3 |
| **Author**         | Trịnh Minh Đức                                     |
| **Reviewed by**    | Nguyễn Xuân Lưu                                   |
| **Approved by**    | [ ] Pending                                          |
| **Classification** | Internal — Confidential                             |

**References:**
* `06-Testing/mod3_pos/uc16/EDS_UC16_Quan_Ly_Ban_An.md`

> **Quy ước TDD:**
> Thứ tự bắt buộc: viết test (.java) → chạy → xác nhận FAIL 🔴 → implement → PASS 🟢 → refactor 🔵.

---

## CHANGELOG

| Ngày       | Người thực hiện | Nội dung thay đổi                                                              |
| ---------- | --------------- | ------------------------------------------------------------------------------- |
| 2026-07-02 | Trịnh Minh Đức  | Khởi tạo TDD Spec cho UC16 — Quản lý bàn ăn (Table Management)                |

---

## MỤC LỤC
1. [Thông tin Module](#1-thong-module)
2. [Logic Issues Resolved](#2-logic-issues-resolved)
3. [Test Case Specification](#3-test-case-specification)
4. [Red-Green-Refactor Tracker](#4-red-green-refactor-tracker)
5. [Entry / Exit Criteria](#5-entry--exit-criteria)
6. [Rollback Plan](#6-rollback-plan)

---

## 1. Thông tin Module

| Field                         | Value                                                              |
| ----------------------------- | ------------------------------------------------------------------ |
| **Feature / Gap ID**    | `GAP-MOD3-UC16`                                                  |
| **Use Case**            | UC-16 — Quản lý bàn ăn                                      |
| **Compliance Scope**    | TABLE-001, TABLE-004                                              |

---

## 2. Logic Issues Resolved

| #  | Spec gốc (sai / thiếu)                                                                       | Thực tế (schema / policy)                                                             | Fix áp dụng trong test                                                                     |
| -- | ---------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------- |
| L1 | Xóa bàn vật lý khỏi DB                                                                         | Sử dụng soft delete (isActive = false) để giữ lịch sử order                             | Assert table.isActive = false, repository không mất row.                                    |
| L2 | Đóng bảo trì bàn tùy ý                                                                         | Không được đóng bàn đang có khách (Occupied) - TABLE-004                                | Thêm test kiểm tra trạng thái trước khi cho đổi sang Out_of_service.                        |

---

## 3. Test Case Specification

### TC-UC16-001 — Tạo bàn mới hợp lệ

**Severity:** HIGH
**CWE:** N/A
**Feature Under Test:** `TableServiceImpl.saveTable()`
**Test File:** `src/test/java/com/kawai/services/TableServiceUC16Test.java`
**TDD Phase:** 🟢 GREEN

**Preconditions:**
* Bàn với `tableNumber = "T05"` chưa tồn tại trong DB.
* Payload gửi lên: `capacity = 6`.

**Test Steps:**
1. Gọi `saveTable` truyền vào DTO tạo bàn mới T05.
2. Assert Repository lưu đối tượng thành công.
3. Assert đối tượng trả về có `tableStatus = AVAILABLE`, `isActive = true`.

**Expected Result (PASS):**
* Bàn được lưu và thiết lập trạng thái mặc định chuẩn xác.

**Expected Result (FAIL):**
* `isActive` mặc định là `false` hoặc `tableStatus` là NULL.

---

### TC-UC16-002 — Trùng tên bàn (TABLE-001)

**Severity:** HIGH
**CWE:** CWE-89
**Feature Under Test:** `Unique Name Validation`
**Test File:** `src/test/java/com/kawai/services/TableServiceUC16Test.java`
**TDD Phase:** 🟢 GREEN

**Preconditions:**
* Bàn với `tableNumber = "T01"` đã tồn tại.

**Test Steps:**
1. Mock Repository trả về bàn `T01` khi tìm theo `tableNumber`.
2. Gọi `saveTable` truyền vào DTO muốn tạo bàn `T01`.
3. Assert bắt được Exception.

**Expected Result (PASS):**
* Ném lỗi `BusinessLogicException`: "TABLE-001: Tên bàn đã tồn tại trong hệ thống".

**Expected Result (FAIL):**
* Lưu đè bàn cũ hoặc ném SQL ConstraintViolation không thân thiện.

---

### TC-UC16-003 — Đóng bàn đang có khách (TABLE-004)

**Severity:** HIGH
**CWE:** N/A
**Feature Under Test:** `TableServiceImpl.toggleStatus()`
**Test File:** `src/test/java/com/kawai/services/TableServiceUC16Test.java`
**TDD Phase:** 🟢 GREEN

**Preconditions:**
* Bàn `T03` đang có trạng thái `OCCUPIED`.

**Test Steps:**
1. Mock Repository trả về bàn `T03` (OCCUPIED).
2. Gọi `toggleStatus(tableId, "OUT_OF_SERVICE")`.
3. Assert bắt được Exception.

**Expected Result (PASS):**
* Ném lỗi `BusinessLogicException`: "TABLE-004: Không thể đóng bàn đang có khách".

**Expected Result (FAIL):**
* Cho phép đóng bàn khiến hệ thống hỏng quy trình tiếp theo cho đơn hàng đang chạy.

---

## 4. Red-Green-Refactor Tracker

| UC   | TC ID       | Mô tả ngắn                                                    | Test File                                    | 🔴 RED | 🟢 GREEN | 🔵 REFACTOR |
| ---- | ----------- | ------------------------------------------------------------- | -------------------------------------------- | ------ | -------- | ----------- |
| UC16 | TC-UC16-001 | Tạo bàn hợp lệ                                                 | `TableServiceUC16Test.java`                  | [x]    | [x]      | [x]         |
| UC16 | TC-UC16-002 | Tạo bàn trùng tên (TABLE-001)                                  | `TableServiceUC16Test.java`                  | [x]    | [x]      | [x]         |
| UC16 | TC-UC16-003 | Đóng bàn Occupied (TABLE-004)                                  | `TableServiceUC16Test.java`                  | [x]    | [x]      | [x]         |

---

## 5. Entry / Exit Criteria

### Exit Criteria / Definition of Done (DoD)
- [x] **3/3 test cases** trong tracker chuyển sang trạng thái 🟢 GREEN.
- [x] Code coverage ≥ 80% cho Use Case.

---

## 6. Rollback Plan
| Tình huống                                           | Hành động                                                                        |
| ------------------------------------------------------ | ----------------------------------------------------------------------------------- |
| Soft delete table nhưng không query được               | Kiểm tra `@Where(clause = "is_active=true")` trên Entity |
