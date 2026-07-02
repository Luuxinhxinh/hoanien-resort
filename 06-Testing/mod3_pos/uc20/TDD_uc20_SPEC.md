# TEST-DRIVEN DEVELOPMENT SPECIFICATION — UC20: QUẢN LÝ THỰC ĐƠN (MENU MANAGEMENT)

## Mẫu Đặc tả Kiểm thử Hướng Phát triển — KAWAI RETREAT RESORT & HUB

| Field                    | Value                                                |
| ------------------------ | ---------------------------------------------------- |
| **Document ID**    | `KAWAI-TDD-UC20-001`                               |
| **Version**        | 1.0                                                  |
| **Date**           | 2026-07-02                                           |
| **Status**         | Approved                                             |
| **Standard**       | ISO/IEC/IEEE 29119-3:2021 — Software Testing Part 3 |
| **Author**         | Trịnh Minh Đức                                     |
| **Reviewed by**    | Nguyễn Xuân Lưu                                   |
| **Approved by**    | [ ] Pending                                          |
| **Classification** | Internal — Confidential                             |

**References:**
* `EDS_UC20_Quan_Ly_Thuc_Don.md`

> **Quy ước TDD:** Thứ tự bắt buộc: viết test (.java) → chạy → xác nhận FAIL 🔴 → implement → PASS 🟢 → refactor 🔵.

---

## CHANGELOG

| Ngày       | Người thực hiện | Nội dung thay đổi                                                              |
| ---------- | --------------- | ------------------------------------------------------------------------------- |
| 2026-07-02 | Trịnh Minh Đức  | Khởi tạo TDD Spec cho UC20 — Quản lý thực đơn (Menu Management)               |

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
| **Feature / Gap ID**    | `GAP-MOD3-UC20`                                                  |
| **Use Case**            | UC-20 — Quản lý thực đơn                                    |
| **Compliance Scope**    | N/A                                                               |

---

## 2. Logic Issues Resolved

| #  | Spec gốc (sai / thiếu)                                                                       | Thực tế (schema / policy)                                                             | Fix áp dụng trong test                                                                     |
| -- | ---------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------- |
| L1 | Filter menu theo ngày thiếu case `isAlwaysAvailable`                                           | Món nước, cơm trắng luôn hiển thị bất kể ngày                                           | Test GET API phải trả về món có `isAlwaysAvailable=true` + món theo ngày.                   |
| L2 | Xóa menu item                                                                                  | Chỉ soft delete (isActive = false)                                                      | Assert field isActive.                                                                      |

---

## 3. Test Case Specification

### TC-UC20-001 — Tạo món hợp lệ

**Severity:** HIGH
**CWE:** N/A
**Feature Under Test:** `MenuServiceImpl.createMenuItem()`
**Test File:** `src/test/java/com/kawai/services/MenuServiceUC20Test.java`
**TDD Phase:** 🟢 GREEN

**Preconditions:**
* DTO tạo món mới với tên "Thịt bò Kobe", giá 500k, `isAvailable = true`.

**Test Steps:**
1. Mock Repository save đối tượng.
2. Gọi hàm tạo món ăn.
3. Assert DTO trả về giống với request và có ID được gen.

**Expected Result (PASS):**
* Lưu món thành công. Dữ liệu chuẩn xác.

**Expected Result (FAIL):**
* Dữ liệu lưu vào DB bị thiếu field hoặc field boolean nhận null.

---

### TC-UC20-002 — Trùng tên món

**Severity:** HIGH
**CWE:** CWE-89
**Feature Under Test:** `Menu Name Unique Constraint`
**Test File:** `src/test/java/com/kawai/services/MenuServiceUC20Test.java`
**TDD Phase:** 🟢 GREEN

**Preconditions:**
* Món "Phở bò" đã tồn tại trong DB.

**Test Steps:**
1. Mock Repository trả về true cho `existsByName("Phở bò")`.
2. Gọi tạo món ăn mới tên "Phở bò".
3. Assert Exception.

**Expected Result (PASS):**
* Ném lỗi "Tên món ăn đã tồn tại".

**Expected Result (FAIL):**
* Hệ thống lưu đè hoặc DB văng DataIntegrityViolationException.

---

### TC-UC20-003 — Lọc thực đơn theo ngày

**Severity:** CRITICAL
**CWE:** N/A
**Feature Under Test:** `MenuServiceImpl.getMenuByDay()`
**Test File:** `src/test/java/com/kawai/services/MenuServiceUC20Test.java`
**TDD Phase:** 🟢 GREEN

**Preconditions:**
* Mock System day = `MONDAY`.
* Có 1 món chỉ bán thứ 2, 1 món chỉ bán thứ 3, 1 món `isAlwaysAvailable = true`.

**Test Steps:**
1. Gọi hàm get thực đơn hôm nay.
2. Assert danh sách trả về.

**Expected Result (PASS):**
* Danh sách trả về chứa món bán thứ 2 + món always available. Tổng = 2 món.

**Expected Result (FAIL):**
* API filter sai ngày hoặc thiếu mất món `isAlwaysAvailable = true` (VD: nước lọc, cơm).

---

## 4. Red-Green-Refactor Tracker

| UC   | TC ID       | Mô tả ngắn                                                    | Test File                                    | 🔴 RED | 🟢 GREEN | 🔵 REFACTOR |
| ---- | ----------- | ------------------------------------------------------------- | -------------------------------------------- | ------ | -------- | ----------- |
| UC20 | TC-UC20-001 | Tạo món hợp lệ                                                 | `MenuServiceUC20Test.java`                   | [x]    | [x]      | [x]         |
| UC20 | TC-UC20-002 | Trùng tên món                                                  | `MenuServiceUC20Test.java`                   | [x]    | [x]      | [x]         |
| UC20 | TC-UC20-003 | Lọc đúng món theo ngày                                         | `MenuServiceUC20Test.java`                   | [x]    | [x]      | [x]         |

---

## 5. Entry / Exit Criteria

### Exit Criteria / Definition of Done (DoD)
- [x] **3/3 test cases** trong tracker chuyển sang trạng thái 🟢 GREEN.
- [x] Code coverage ≥ 80% cho Use Case.

---

## 6. Rollback Plan
| Tình huống                                           | Hành động                                                                        |
| ------------------------------------------------------ | ----------------------------------------------------------------------------------- |
| API trả về món đã soft-delete                        | Bổ sung logic WHERE is_active = true vào mọi câu Query. |
