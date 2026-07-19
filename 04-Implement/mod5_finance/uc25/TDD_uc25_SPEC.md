# TEST-DRIVEN DEVELOPMENT SPECIFICATION
## Mẫu Đặc tả Kiểm thử Hướng Phát triển cho UC25

| Field | Value |
| --- | --- |
| **Document ID** | `KAWAI-MOD5-TDD-UC25` |
| **Version** | 1.0 |
| **Date** | 2026-06-21 |
| **Status** | Draft |
| **Standard** | ISO/IEC/IEEE 29119-3:2021 — Software Testing Part 3: Test Documentation |
| **Author** | `Antigravity AI` |
| **Reviewed by** | `[ ] Ngô Thị Ngọc Lan – Pending` |
| **DPO Sign-off** | `[ ] N/A` |
| **Approved by** | `[ ] Pending` |
| **Classification** | Internal – Confidential |

---

### References:
*   `EDS_UC25_SPEC.md` — Technical Specification

---

### CHANGELOG

| Ngày | Người thực hiện | Nội dung thay đổi |
| --- | --- | --- |
| 2026-06-21 | Antigravity AI | Khởi tạo tài liệu — TDD spec cho UC25 (Thu tiền) |

---

### MỤC LỤC
1. [Thông tin Module](#1-thong-tin-module)
2. [Logic Issues Resolved](#2-logic-issues-resolved)
3. [Test Design Specification (TDS)](#3-test-design-specification-tds)
4. [Test Case Specification](#4-test-case-specification)
5. [Red-Green-Refactor Tracker](#5-red-green-refactor-tracker)
6. [Entry / Exit Criteria](#6-entry--exit-criteria)

---

### 1. Thông tin Module

| Field | Value |
| --- | --- |
| **Feature / Gap ID** | `UC25` |
| **Module** | `MOD5 - Finance & Reports` |
| **Spec gốc** | `EDS_UC25_SPEC.md` |
| **Priority** | 🔴 P0 |
| **Sprint** | `S3` |
| **Data Classification** | Highly Financial |

---

### 2. Logic Issues Resolved
Không có vấn đề.

---

### 3. Test Design Specification (TDS)

#### TDS-01 — Scope / Phạm vi
`UC25` bao gồm Layer Service (`PaymentServiceImpl`).

#### TDS-02 — Test Basis / Cơ sở Kiểm thử
- `BR-FIN-06`: Thu tiền và lưu lại giao dịch.

#### TDS-03 — Test Conditions and Coverage Items
| Condition ID | Test Condition | Coverage Item | Test Cases |
| --- | --- | --- | --- |
| **TC-COND-001** | Lưu tx thành công | `recordPayment()` | `UC25-TC-001` |
| **TC-COND-002** | Invoice là null | `recordPayment()` | `UC25-TC-002` |

---

### 4. Test Case Specification

#### `UC25-TC-001` — Lưu PaymentTransaction thành công (Happy Path)

*   **Severity:** `CRITICAL`
*   **Feature Under Test:** `PaymentServiceImpl.recordPayment()`
*   **Test File:** `PaymentServiceUC25Test.java`
*   **TDD Phase:** 🔴 RED
*   **Condition Ref:** `TC-COND-001`

**Preconditions:**
- Mock `PaymentTransactionRepository.save()`.

**Test Steps:**
1. Khởi tạo `invoice`, `booking`, `amount`, `method`, v.v...
2. Gọi `recordPayment(invoice, booking, ...)`

**Expected Result (PASS):**
- Hàm thực thi thành công, trả về đối tượng `PaymentTransaction` chứa `createdAt` khác null.

**Current Status:** 🔴 Not written

---

#### `UC25-TC-002` — Báo lỗi nếu Invoice bị Null

*   **Severity:** `HIGH`
*   **Feature Under Test:** `PaymentServiceImpl.recordPayment()`
*   **Test File:** `PaymentServiceUC25Test.java`
*   **TDD Phase:** 🔴 RED
*   **Condition Ref:** `TC-COND-002`

**Test Steps:**
1. Gọi `recordPayment(null, booking, ...)`

**Expected Result (PASS):**
- Văng `IllegalArgumentException` với message `Invoice cannot be null for payment transaction`.

**Current Status:** 🔴 Not written

---

### 5. Red-Green-Refactor Tracker

| TC ID | Test File | 🔴 RED confirmed | 🟢 GREEN (commit) | 🔵 REFACTOR note |
| --- | --- | :---: | --- | --- |
| `UC25-TC-001` | `PaymentServiceUC25Test.java` | `[ ]` | `[ ]` | Chưa viết code |
| `UC25-TC-002` | `PaymentServiceUC25Test.java` | `[ ]` | `[ ]` | Chưa viết code |

---

### 6. Entry / Exit Criteria

#### Exit Criteria (Điều kiện kết thúc — DoD)
- [ ] Implement `PaymentServiceUC25Test.java` phủ >=80% lines của hàm `recordPayment`.
- [ ] Tests chạy qua 100% (Xanh).

---

### 7. Rollback Plan

**Revert implementation files (nếu test sai):**
`git checkout -- src/test/java/com/kawai/services/PaymentServiceUC25Test.java`
