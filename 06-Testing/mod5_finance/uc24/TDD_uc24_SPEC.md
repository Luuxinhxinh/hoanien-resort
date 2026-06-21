# TEST-DRIVEN DEVELOPMENT SPECIFICATION
## Mẫu Đặc tả Kiểm thử Hướng Phát triển cho UC24

| Field | Value |
| --- | --- |
| **Document ID** | `KAWAI-MOD5-TDD-UC24` |
| **Version** | 1.0 |
| **Date** | 2026-06-21 |
| **Status** | Draft |
| **Standard** | ISO/IEC/IEEE 29119-3:2021 — Software Testing Part 3: Test Documentation |
| **Author** | `Antigravity AI` |
| **Reviewed by** | `[ ] Ngô Thị Ngọc Lan – Pending` |
| **DPO Sign-off** | `[ ] Pending` |
| **Approved by** | `[ ] Pending` |
| **Classification** | Internal – Confidential |

---

### References:
*   `EDS_UC24_SPEC.md` — Technical Specification

---

### CHANGELOG

| Ngày | Người thực hiện | Nội dung thay đổi |
| --- | --- | --- |
| 2026-06-21 | Antigravity AI | Khởi tạo tài liệu — TDD spec cho UC24 (Night Audit) |

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
| **Feature / Gap ID** | `UC24` |
| **Module** | `MOD5 - Finance & Reports` |
| **Spec gốc** | `EDS_UC24_SPEC.md` |
| **Priority** | 🔴 P0 |
| **Sprint** | `S3` |
| **Data Classification** | Sensitive-PII |
| **Compliance Scope** | Kiểm toán |

---

### 2. Logic Issues Resolved
Không có vấn đề bất đồng logic.

---

### 3. Test Design Specification (TDS)

#### TDS-01 — Scope / Phạm vi
`UC24` bao gồm Layer Service (`NightAuditServiceImpl`).

#### TDS-02 — Test Basis / Cơ sở Kiểm thử
- `BR-FIN-03`: Cộng phí phòng vào Folio.
- `BR-FIN-05`: Gom hóa đơn chính xác.
- `BR-FB-01`: Hiển thị đúng nợ.

#### TDS-03 — Test Conditions and Coverage Items
| Condition ID | Test Condition | Coverage Item | Test Cases |
| --- | --- | --- | --- |
| **TC-COND-001** | Lấy items chưa settled | `calculateFolioBalance()` | `UC24-TC-001` |
| **TC-COND-002** | Xử lý DB empty | `calculateFolioBalance()` | `UC24-TC-002` |

---

### 4. Test Case Specification

#### `UC24-TC-001` — Tính tổng Folio loại trừ Item đã thanh toán

*   **Severity:** `CRITICAL`
*   **Feature Under Test:** `NightAuditServiceImpl.calculateFolioBalance()`
*   **Test File:** `NightAuditServiceUC24Test.java`
*   **TDD Phase:** 🟢 GREEN (Đã implement)
*   **Condition Ref:** `TC-COND-001`

**Preconditions:**
- `FolioItemRepository` mock trả về 2 items (1 cái giá 100k đã settled, 1 cái giá 200k chưa settled).

**Test Steps:**
1. Gọi `calculateFolioBalance(1L)`.

**Expected Result (PASS):**
- Hàm trả về `200000.00` (bỏ qua item 100k đã settled).

**Current Status:** 🟢 GREEN

---

#### `UC24-TC-002` — Tính tổng Folio khi không có dư nợ

*   **Severity:** `HIGH`
*   **Feature Under Test:** `NightAuditServiceImpl.calculateFolioBalance()`
*   **Test File:** `NightAuditServiceUC24Test.java`
*   **TDD Phase:** 🟢 GREEN
*   **Condition Ref:** `TC-COND-002`

**Preconditions:**
- Mock trả về mảng rỗng.

**Test Steps:**
1. Gọi `calculateFolioBalance(1L)`.

**Expected Result (PASS):**
- Trả về `BigDecimal.ZERO`.

**Current Status:** 🟢 GREEN

---

### 5. Red-Green-Refactor Tracker

| TC ID | Test File | 🔴 RED confirmed | 🟢 GREEN (commit) | 🔵 REFACTOR note |
| --- | --- | :---: | --- | --- |
| `UC24-TC-001` | `NightAuditServiceUC24Test.java` | `[x]` | `[DONE]` | Đã có sẵn |
| `UC24-TC-002` | `NightAuditServiceUC24Test.java` | `[x]` | `[DONE]` | Đã có sẵn |

---

### 6. Entry / Exit Criteria

#### Exit Criteria (Điều kiện kết thúc — DoD)
- [x] Unit Tests cho UC24 chạy Pass 100%.
- [x] Code tuân thủ nguyên tắc không văng Exception thừa khi mảng rỗng.

---

### 7. Rollback Plan

**Revert implementation files (nếu test sai):**
`git checkout -- src/test/java/com/kawai/services/NightAuditServiceUC24Test.java`
