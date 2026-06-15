# TEST-DRIVEN DEVELOPMENT SPECIFICATION
## UC25 — Thanh toán & Check-out

| Field | Value |
|-------|-------|
| **Document ID** | `KAWAI-TDD-MOD5-UC25-001` |
| **Version** | 1.0 |
| **Date** | 2026-06-15 |
| **Status** | Approved |
| **Standard** | ISO/IEC/IEEE 29119-3:2021 |
| **Author** | Nguyễn Xuân Lưu — Tech Lead |
| **Reviewed by** | [x] Nguyễn Xuân Lưu — Tech Lead |
| **DPO Sign-off** | `[x] Approved – 2026-06-15 – Nguyễn Xuân Lưu` |
| **Approved by** | `[x] Nguyễn Xuân Lưu – 2026-06-15` |
| **Classification** | Internal — Confidential |

---

### MỤC LỤC
1. [Thông tin Module](#1)
2. [Logic Issues Resolved](#2)
3. [TDS](#3)
4. [Test Case Specification](#4)
5. [Red-Green-Refactor Tracker](#5)
6. [Entry / Exit Criteria](#6)
7. [Rollback Plan](#7)

---

### 1. Thông tin Module
| Field | Value |
|-------|-------|
| **Feature** | UC25: Thanh toán & Check-out |
| **Priority** | 🔴 P0 |

---

### 2. Logic Issues Resolved
| # | Spec gốc | Fix áp dụng trong test |
|---|----------|------------------------|
| **L1** | Checkout thiếu kiểm tra dư nợ | Thêm rule BR-FIN-01 ném Exception FOLIO-001 |
| **L2** | Gửi email chậm gây timeout API checkout | Áp dụng `@Async` và mock test timeout |

---

### 3. Test Design Specification (TDS)
#### TDS-01 — Scope / Phạm vi
Test validation khi Checkout và tích hợp Async Event.

#### TDS-02 — Test Basis
- BR-FIN-01: Không cho checkout khi Folio > 0.

---

### 4. Test Case Specification

#### `TC-UC25-001` — Check-out khi Folio = 0
* **Severity:** CRITICAL | **Feature:** UC25.1
**Steps:** Checkout phòng có Balance = 0 -> Pass, Room state -> DIRTY.

#### `TC-UC25-002` — Check-out khi Folio > 0 → Chặn
* **Severity:** CRITICAL | **Feature:** UC25.1
**Steps:** Checkout phòng có Balance > 0 -> throws `UnsettledFolioException` (FOLIO-001).

#### `TC-UC25-003` — Tất toán Folio → SETTLED
* **Severity:** HIGH | **Feature:** UC25.1
**Steps:** Make payment bằng Balance -> Folio state -> SETTLED.

#### `TC-UC25-004` — Sau tất toán → Gửi e-Invoice
* **Severity:** MEDIUM | **Feature:** UC25.2
**Steps:** Checkout thành công -> Verify event `FolioSettled` được catch và gọi `EmailService`.

---

### 5. Red-Green-Refactor Tracker

| TC ID | Mô tả | Test File | 🔴 RED | 🟢 GREEN | 🔵 REFACTOR |
|---|---|---|---|---|---|
| TC-UC25-001 | Checkout (0) | `CheckoutTest.java` | [x] | [x] | ✅ State switch |
| TC-UC25-002 | Checkout (>0) | `CheckoutTest.java` | [x] | [x] | ✅ Exception handling |
| TC-UC25-003 | Pay Folio | `PaymentTest.java` | [x] | [x] | ✅ Validate amount |
| TC-UC25-004 | Async e-Invoice | `InvoiceEventTest.java`| [x] | [x] | ✅ Mockito verifications |

---

### 6. Entry / Exit Criteria
- **Exit:** Đảm bảo Checkout throws đúng exception và Async event hoạt động độc lập không block Main thread.

---

### 7. Rollback Plan
Revert logic Checkout validation nếu gây kẹt luồng khách hàng hợp lệ (false positives).