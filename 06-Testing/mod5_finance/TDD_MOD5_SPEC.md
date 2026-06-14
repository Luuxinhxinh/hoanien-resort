# TEST-DRIVEN DEVELOPMENT SPECIFICATION TEMPLATE
## Mẫu Đặc tả Kiểm thử Hướng Phát triển

| Field | Value |
| --- | --- |
| **Document ID** | `KAWAI-TDD-MOD5-001` |
| **Version** | 1.0 |
| **Date** | 2026-06-12 |
| **Status** | Approved |
| **Standard** | ISO/IEC/IEEE 29119-3:2021 |
| **Author** | Nguyễn Xuân Lưu - Tech Lead |

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
Module Finance & Night Audit

---

### 2. Logic Issues Resolved
Sửa lặp audit do double trigger.

---

### 3. Test Design Specification (TDS)
Testing @Scheduled components.

---

### 4. Test Case Specification

#### `TC-M5-001` — Folio hiển thị đúng danh sách nợ phòng theo từng dịch vụ
*   **Severity:** HIGH
*   **Feature Under Test:** UC24.1
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M5-002` — Ghi nhận luồng tiền nhiều đợt — ứng trước, trả thêm, hoàn tiền
*   **Severity:** HIGH
*   **Feature Under Test:** UC24.2
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M5-003` — Gom hóa đơn — tiền phòng + ăn uống + tour = tổng chính xác (BigDecimal)
*   **Severity:** CRITICAL
*   **Feature Under Test:** UC24.3
*   **Test Type:** Integration
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M5-004` — Night Audit 02:00 AM — cộng phí phòng ngày vào Folio các phòng OCCUPIED
*   **Severity:** CRITICAL
*   **Feature Under Test:** UC24.4
*   **Test Type:** Integration
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M5-005` — Night Audit chuyển Business Date lên 1 ngày
*   **Severity:** HIGH
*   **Feature Under Test:** UC24.4
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M5-006` — Check-out khi Folio = 0 → thành công, phòng chuyển DIRTY
*   **Severity:** CRITICAL
*   **Feature Under Test:** UC25.1
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M5-007` — Check-out khi Folio > 0 → chặn, trả FOLIO-001 (BR-FIN-01)
*   **Severity:** CRITICAL
*   **Feature Under Test:** UC25.1
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M5-008` — Thanh toán tất toán Folio (Tiền mặt/Thẻ) → Folio = SETTLED
*   **Severity:** HIGH
*   **Feature Under Test:** UC25.1
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M5-009` — Sau tất toán → tự động gửi e-Invoice qua email (SendGrid)
*   **Severity:** MEDIUM
*   **Feature Under Test:** UC25.2
*   **Test Type:** Integration
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M5-010` — Dashboard trả dữ liệu biểu đồ tài chính đúng
*   **Severity:** MEDIUM
*   **Feature Under Test:** UC26.1
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M5-011` — Occupancy Rate = (phòng OCCUPIED / tổng phòng) × 100% — tính đúng
*   **Severity:** MEDIUM
*   **Feature Under Test:** UC26.2
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M5-012` — Báo cáo USALI phân tách doanh thu Rooms / F&B / Tours đúng (BR-FIN-04)
*   **Severity:** HIGH
*   **Feature Under Test:** UC27
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M5-013` — Kết xuất PDF — file không rỗng, đúng format
*   **Severity:** MEDIUM
*   **Feature Under Test:** UC28
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M5-014` — Kết xuất Excel — dữ liệu khớp với DB
*   **Severity:** MEDIUM
*   **Feature Under Test:** UC28
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.


### 5. Red-Green-Refactor Tracker

| TC ID | Mô tả ngắn | Test File | 🔴 RED | 🟢 GREEN (commit) | 🔵 REFACTOR note |
| --- | --- | --- | --- | --- | --- |
| TC-M5-001 | Folio hiển thị đúng danh sách nợ phòng theo từng dịch vụ | TBD | [ ] | [ ] | |
| TC-M5-002 | Ghi nhận luồng tiền nhiều đợt — ứng trước, trả thêm, hoàn tiền | TBD | [ ] | [ ] | |
| TC-M5-003 | Gom hóa đơn — tiền phòng + ăn uống + tour = tổng chính xác (BigDecimal) | TBD | [ ] | [ ] | |
| TC-M5-004 | Night Audit 02:00 AM — cộng phí phòng ngày vào Folio các phòng OCCUPIED | TBD | [ ] | [ ] | |
| TC-M5-005 | Night Audit chuyển Business Date lên 1 ngày | TBD | [ ] | [ ] | |
| TC-M5-006 | Check-out khi Folio = 0 → thành công, phòng chuyển DIRTY | TBD | [ ] | [ ] | |
| TC-M5-007 | Check-out khi Folio > 0 → chặn, trả FOLIO-001 (BR-FIN-01) | TBD | [ ] | [ ] | |
| TC-M5-008 | Thanh toán tất toán Folio (Tiền mặt/Thẻ) → Folio = SETTLED | TBD | [ ] | [ ] | |
| TC-M5-009 | Sau tất toán → tự động gửi e-Invoice qua email (SendGrid) | TBD | [ ] | [ ] | |
| TC-M5-010 | Dashboard trả dữ liệu biểu đồ tài chính đúng | TBD | [ ] | [ ] | |
| TC-M5-011 | Occupancy Rate = (phòng OCCUPIED / tổng phòng) × 100% — tính đúng | TBD | [ ] | [ ] | |
| TC-M5-012 | Báo cáo USALI phân tách doanh thu Rooms / F&B / Tours đúng (BR-FIN-04) | TBD | [ ] | [ ] | |
| TC-M5-013 | Kết xuất PDF — file không rỗng, đúng format | TBD | [ ] | [ ] | |
| TC-M5-014 | Kết xuất Excel — dữ liệu khớp với DB | TBD | [ ] | [ ] | |

### 6. Entry / Exit Criteria
- [x] Cron jobs test pass.

---

### 7. Rollback Plan
Restore Business Date
