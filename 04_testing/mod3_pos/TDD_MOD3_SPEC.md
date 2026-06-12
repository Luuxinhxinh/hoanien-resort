# TEST-DRIVEN DEVELOPMENT SPECIFICATION TEMPLATE
## Mẫu Đặc tả Kiểm thử Hướng Phát triển

| Field | Value |
| --- | --- |
| **Document ID** | `KAWAI-TDD-MOD3-001` |
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
Module POS & F&B

---

### 2. Logic Issues Resolved
Sửa lỗi vượt credit limit.

---

### 3. Test Design Specification (TDS)
Event-driven testing.

---

### 4. Test Case Specification

#### `TC-M3-001` — Khách quét QR → hiển thị E-Menu, đặt Room Service thành công
*   **Severity:** MEDIUM
*   **Feature Under Test:** UC14
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M3-002` — Room Service cho phòng không OCCUPIED → từ chối
*   **Severity:** MEDIUM
*   **Feature Under Test:** UC14
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M3-003` — Đặt bàn thành công — bàn chuyển RESERVED
*   **Severity:** MEDIUM
*   **Feature Under Test:** UC15
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M3-004` — 2 khách đặt cùng bàn cùng giờ → 1 thành công, 1 báo lỗi
*   **Severity:** HIGH
*   **Feature Under Test:** UC15
*   **Test Type:** Concurrency
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M3-005` — POS tạo order Dine-In — ghi đúng bàn, đúng món, đúng giá
*   **Severity:** CRITICAL
*   **Feature Under Test:** UC16
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M3-006` — POS thanh toán tiền mặt — đơn hàng chuyển PAID
*   **Severity:** HIGH
*   **Feature Under Test:** UC16
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M3-007` — KDS nhận order mới → hiển thị KOT trên màn hình bếp
*   **Severity:** HIGH
*   **Feature Under Test:** UC17.1
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M3-008` — Bếp cập nhật từng món PREPARING → READY
*   **Severity:** HIGH
*   **Feature Under Test:** UC17.1
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M3-009` — Bếp báo hết món → POS/E-Menu tự động khóa món đó
*   **Severity:** HIGH
*   **Feature Under Test:** UC17.2
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M3-010` — Post to Room thành công — ghi nợ vào Folio phòng
*   **Severity:** CRITICAL
*   **Feature Under Test:** UC18
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M3-011` — Post to Room vượt Credit Limit → từ chối, trả POS-003 (BR-FO-06)
*   **Severity:** CRITICAL
*   **Feature Under Test:** UC18
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M3-012` — Post to Room cho phòng không OCCUPIED → từ chối
*   **Severity:** HIGH
*   **Feature Under Test:** UC18
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.


### 5. Red-Green-Refactor Tracker

| TC ID | Mô tả ngắn | Test File | 🔴 RED | 🟢 GREEN (commit) | 🔵 REFACTOR note |
| --- | --- | --- | --- | --- | --- |
| TC-M3-001 | Khách quét QR → hiển thị E-Menu, đặt Room Service thành công | TBD | [ ] | [ ] | |
| TC-M3-002 | Room Service cho phòng không OCCUPIED → từ chối | TBD | [ ] | [ ] | |
| TC-M3-003 | Đặt bàn thành công — bàn chuyển RESERVED | TBD | [ ] | [ ] | |
| TC-M3-004 | 2 khách đặt cùng bàn cùng giờ → 1 thành công, 1 báo lỗi | TBD | [ ] | [ ] | |
| TC-M3-005 | POS tạo order Dine-In — ghi đúng bàn, đúng món, đúng giá | TBD | [ ] | [ ] | |
| TC-M3-006 | POS thanh toán tiền mặt — đơn hàng chuyển PAID | TBD | [ ] | [ ] | |
| TC-M3-007 | KDS nhận order mới → hiển thị KOT trên màn hình bếp | TBD | [ ] | [ ] | |
| TC-M3-008 | Bếp cập nhật từng món PREPARING → READY | TBD | [ ] | [ ] | |
| TC-M3-009 | Bếp báo hết món → POS/E-Menu tự động khóa món đó | TBD | [ ] | [ ] | |
| TC-M3-010 | Post to Room thành công — ghi nợ vào Folio phòng | TBD | [ ] | [ ] | |
| TC-M3-011 | Post to Room vượt Credit Limit → từ chối, trả POS-003 (BR-FO-06) | TBD | [ ] | [ ] | |
| TC-M3-012 | Post to Room cho phòng không OCCUPIED → từ chối | TBD | [ ] | [ ] | |

### 6. Entry / Exit Criteria
- [x] 100% integration tests pass.

---

### 7. Rollback Plan
Revert PosService
