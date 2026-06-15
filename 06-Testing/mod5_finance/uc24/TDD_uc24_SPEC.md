# TEST-DRIVEN DEVELOPMENT SPECIFICATION
## UC24 — Quản lý Folio & Kiểm toán đêm

| Field | Value |
|-------|-------|
| **Document ID** | `KAWAI-TDD-MOD5-UC24-001` |
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
| **Feature** | UC24: Quản lý Folio & Night Audit |
| **Priority** | 🔴 P0 |

---

### 2. Logic Issues Resolved
| # | Spec gốc | Fix áp dụng trong test |
|---|----------|------------------------|
| **L1** | Thiếu khóa chống chạy ngầm nhiều lần | Dùng ShedLock/Redis Mock trong Test |
| **L2** | Sai số do Float/Double | Validate tính toán bằng `BigDecimal` |

---

### 3. Test Design Specification (TDS)
#### TDS-01 — Scope / Phạm vi
Test logic Folio cộng dồn phí và NightAudit tính tiền phòng lúc 2h sáng.

#### TDS-02 — Test Basis
- SRS UC24.

---

### 4. Test Case Specification

#### `TC-UC24-001` — Folio hiển thị đúng danh sách
* **Severity:** HIGH | **Feature:** UC24.1
**Steps:** Get Folio của Reservation -> Verify trả về danh sách `FolioItem` đúng.

#### `TC-UC24-002` — Ghi nhận luồng tiền nhiều đợt
* **Severity:** HIGH | **Feature:** UC24.2
**Steps:** Gọi add charge cho dịch vụ ăn uống -> Balance tăng.

#### `TC-UC24-003` — Gom hóa đơn (BigDecimal)
* **Severity:** CRITICAL | **Feature:** UC24.3
**Steps:** Add 3 khoản phí lẻ -> Verify `totalAmount` tổng đúng tuyệt đối không lệch thập phân.

#### `TC-UC24-004` — Night Audit cộng tiền
* **Severity:** CRITICAL | **Feature:** UC24.4
**Steps:** Mock 2 phòng Occupied -> Run audit -> Verify 2 phòng có thêm phí phòng qua đêm.

#### `TC-UC24-005` — Night Audit chuyển ngày
* **Severity:** HIGH | **Feature:** UC24.4
**Steps:** Run audit -> Verify `BusinessDate` được cộng 1 ngày.

---

### 5. Red-Green-Refactor Tracker

| TC ID | Mô tả | Test File | 🔴 RED | 🟢 GREEN | 🔵 REFACTOR |
|---|---|---|---|---|---|
| TC-UC24-001 | List FolioItems | `FolioTest.java` | [x] | [x] | ✅ Extract logic |
| TC-UC24-002 | Add charge | `FolioTest.java` | [x] | [x] | ✅ Use BigDecimal |
| TC-UC24-003 | Sum calculation | `FolioTest.java` | [x] | [x] | ✅ Optimize stream sum |
| TC-UC24-004 | Audit Charge | `NightAuditTest.java` | [x] | [x] | ✅ Batch Insert |
| TC-UC24-005 | Audit Date Change | `NightAuditTest.java` | [x] | [x] | ✅ Date Utils |

---

### 6. Entry / Exit Criteria
- **Exit:** Unit tests cho Folio và Night Audit đạt 100% pass. ShedLock test pass ở môi trường concurrency.

---

### 7. Rollback Plan
Revert code `NightAuditScheduler` về trước S3 nếu ShedLock gây lỗi môi trường.