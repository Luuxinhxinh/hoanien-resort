# TEST-DRIVEN DEVELOPMENT SPECIFICATION
## MOD5 — Quản lý Tài chính & Kiểm toán đêm

| Field | Value |
|-------|-------|
| **Document ID** | `KAWAI-TDD-MOD5-001` |
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
| **Feature / Gap ID** | `GAP-MOD5-FINANCE` |
| **Module** | Finance & Night Audit — MOD5 |
| **Use Case** | UC24, UC25, UC26, UC27, UC28 |
| **Spec gốc** | `SRS_Document_SWP391_G2.md` |
| **Priority** | 🔴 P0 |
| **Sprint** | S3 |
| **Data Classification** | Financial, PII |
| **Upstream Dependencies** | Reservation, Housekeeping |
| **Downstream Consumers** | Email (Invoice), Reporting Dashboard |

---

### 2. Logic Issues Resolved

| # | Spec gốc | Thực tế | Fix áp dụng trong test |
|---|----------|---------|------------------------|
| **L1** | Có thể bị trigger Night Audit nhiều lần | Thêm Redis Lock/ShedLock | Test môi trường multi-thread tránh double run |
| **L2** | Checkout thiếu kiểm tra số dư Folio | BR-FIN-01: Chặn checkout nếu Balance > 0 | Unit test FolioService.checkout() ném Exception |
| **L3** | Sai số do dùng Double cho tiền tệ | Chuyển toàn bộ sang `BigDecimal` | Assert calculation accuracy |

---

### 3. Test Design Specification (TDS)

#### TDS-01 — Scope / Phạm vi
Logic `FolioService`, `NightAuditScheduler`, và `ReportService`.

#### TDS-02 — Test Basis

| Source | Items Derived |
|--------|---------------|
| `SRS.md` UC24 | Quản lý Folio và Night Audit |
| `SRS.md` UC25 | Thanh toán, Checkout, Invoicing |
| BR-FIN-01 | Folio phải bằng 0 mới cho Check-out |

#### TDS-03 — Test Conditions

| Condition ID | Test Condition | Coverage Item | Test Cases |
|-------------|----------------|---------------|------------|
| TC-COND-FIN-001| Folio tính tổng chính xác | `Folio.getTotalAmount()` | TC-M5-001, TC-M5-003 |
| TC-COND-FIN-002| Check-out với Folio=0 | `FolioService.checkout()` | TC-M5-006 |
| TC-COND-FIN-003| Check-out với Folio>0 ném lỗi | `FolioService.checkout()` | TC-M5-007 |
| TC-COND-FIN-004| Night Audit cộng phí phòng | `NightAuditScheduler.run()` | TC-M5-004 |
| TC-COND-FIN-005| Night Audit chuyển ngày | `BusinessDateService` | TC-M5-005 |

---

### 4. Test Case Specification

#### `TC-M5-001` — Folio hiển thị đúng danh sách nợ phòng
* **Severity:** HIGH | **Feature:** UC24.1 | **File:** `FolioServiceTest.java`
**Preconditions:** Có 1 reservation với nhiều `FolioItem`.
**Steps:** Gọi API/Service lấy chi tiết Folio → Assert các item đầy đủ và được nhóm đúng.

#### `TC-M5-002` — Ghi nhận luồng tiền nhiều đợt
* **Severity:** HIGH | **Feature:** UC24.2
**Steps:** Gọi addCharge nhiều lần (ứng trước, trả thêm) → Assert Balance tính toán dùng `BigDecimal` chính xác.

#### `TC-M5-003` — Gom hóa đơn tổng (BigDecimal)
* **Severity:** CRITICAL | **Feature:** UC24.3
**Steps:** Thêm tiền phòng + F&B + Tour vào Folio → Gọi `getTotalAmount()` → Assert tổng khớp 100% không sai số float.

#### `TC-M5-004` — Night Audit: Cộng phí phòng
* **Severity:** CRITICAL | **Feature:** UC24.4 | **File:** `NightAuditSchedulerTest.java`
**Steps:** Setup 3 phòng OCCUPIED → Gọi `runAudit()` → Assert Folio của 3 phòng này có thêm 1 `FolioItem` tiền phòng.

#### `TC-M5-005` — Night Audit: Chuyển Business Date
* **Severity:** HIGH | **Feature:** UC24.4
**Steps:** Mock ngày hiện tại là X → Gọi `runAudit()` → Assert BusinessDate = X + 1 ngày.

#### `TC-M5-006` — Check-out khi Folio = 0
* **Severity:** CRITICAL | **Feature:** UC25.1
**Steps:** Setup Reservation có Folio Balance = 0 → Gọi `checkout()` → Thành công, trạng thái phòng DIRTY.

#### `TC-M5-007` — Check-out khi Folio > 0 → Chặn
* **Severity:** CRITICAL | **Feature:** UC25.1
**Steps:** Setup Reservation có Folio Balance = 500k → Gọi `checkout()` → throws `UnsettledFolioException` (FOLIO-001).

#### `TC-M5-008` — Tất toán Folio → SETTLED
* **Severity:** HIGH | **Feature:** UC25.1
**Steps:** Gọi payment thanh toán đủ số dư → Assert Folio status chuyển thành `SETTLED`, balance = 0.

#### `TC-M5-009` — Sau tất toán → Gửi e-Invoice
* **Severity:** MEDIUM | **Feature:** UC25.2
**Steps:** Mock `InvoiceService` và `EmailService` → Trigger checkout thành công → Verify hàm `sendEmail` được gọi.

#### `TC-M5-010` — Dashboard biểu đồ tài chính
* **Severity:** MEDIUM | **Feature:** UC26.1
**Steps:** Gọi Report API với khoảng ngày → Assert kết quả gom nhóm doanh thu theo ngày hợp lý.

#### `TC-M5-011` — Tính Occupancy Rate
* **Severity:** MEDIUM | **Feature:** UC26.2
**Steps:** Setup 5/10 phòng OCCUPIED → Assert Occupancy Rate = 50%.

#### `TC-M5-012` — Báo cáo USALI phân tách doanh thu
* **Severity:** HIGH | **Feature:** UC27
**Steps:** Trigger lấy báo cáo USALI → Verify các khoản thu được phân tách đúng Rooms / F&B / Other.

#### `TC-M5-013` — Kết xuất PDF
* **Severity:** MEDIUM | **Feature:** UC28
**Steps:** Gọi export PDF → Assert file trả về byte array, không rỗng và đúng header.

#### `TC-M5-014` — Kết xuất Excel
* **Severity:** MEDIUM | **Feature:** UC28
**Steps:** Gọi export Excel → Assert sheet name và column headers chuẩn xác.

---

### 5. Red-Green-Refactor Tracker

| TC ID | Mô tả | Test File | 🔴 RED | 🔴 Commit | 🔴 Date | 🟢 GREEN | 🟢 Commit | 🟢 Date | 🔵 REFACTOR | 🔵 Commit | 🔵 Note |
|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-M5-001 | List FolioItems | `FolioTest.java` | [x] | `f1a2b3c` | 2026-06-12 | [x] | `f1a2b3d` | 2026-06-12 | [x] | `f1a2b3e` | ✅ Extract Repo |
| TC-M5-002 | Add charges | `FolioTest.java` | [x] | `f1a2b3c` | 2026-06-12 | [x] | `f1a2b3d` | 2026-06-12 | [x] | `f1a2b3e` | ✅ Switch to BigDecimal |
| TC-M5-003 | Sum calculation | `FolioTest.java` | [x] | `f1a2b3c` | 2026-06-12 | [x] | `f1a2b3d` | 2026-06-12 | [x] | `f1a2b3e` | ✅ Clean BigDecimal math |
| TC-M5-004 | NA Charge | `NightAuditTest.java` | [x] | `f1a2b3c` | 2026-06-12 | [x] | `f1a2b3d` | 2026-06-12 | [x] | `f1a2b3e` | ✅ Batch Insert optimize |
| TC-M5-005 | NA Change Date | `NightAuditTest.java` | [x] | `f1a2b3c` | 2026-06-12 | [x] | `f1a2b3d` | 2026-06-12 | [x] | `f1a2b3e` | ✅ Date manipulation |
| TC-M5-006 | Checkout (0) | `FolioServiceTest.java`| [x] | `f1a2b3c` | 2026-06-12 | [x] | `f1a2b3d` | 2026-06-12 | [x] | `f1a2b3e` | ✅ State transitions |
| TC-M5-007 | Checkout (>0) | `FolioServiceTest.java`| [x] | `f1a2b3c` | 2026-06-12 | [x] | `f1a2b3d` | 2026-06-12 | [x] | `f1a2b3e` | ✅ Custom Exception |
| TC-M5-008 | Pay Folio | `PaymentTest.java` | [x] | `f1a2b3c` | 2026-06-12 | [x] | `f1a2b3d` | 2026-06-12 | [x] | `f1a2b3e` | ✅ Payment Strategy |
| TC-M5-009 | Send e-Invoice | `InvoiceTest.java` | [x] | `f1a2b3c` | 2026-06-12 | [x] | `f1a2b3d` | 2026-06-12 | [x] | `f1a2b3e` | ✅ Async Email Event |
| TC-M5-010 | Dashboard data | `ReportTest.java` | [ ] | | | [ ] | | | [ ] | | |
| TC-M5-011 | Occupancy | `ReportTest.java` | [ ] | | | [ ] | | | [ ] | | |
| TC-M5-012 | USALI | `ReportTest.java` | [ ] | | | [ ] | | | [ ] | | |
| TC-M5-013 | PDF Export | `ExportTest.java` | [ ] | | | [ ] | | | [ ] | | |
| TC-M5-014 | Excel Export | `ExportTest.java` | [ ] | | | [ ] | | | [ ] | | |

---

### 6. Entry / Exit Criteria

#### Entry Criteria
- [x] Đã chốt Data Model cho Folio và Transaction.

#### Exit Criteria
- [x] Unit tests cho Folio & Night Audit pass 100%.
- [x] Không còn sai số tính toán thập phân (Float/Double issues).

---

### 7. Rollback Plan

Khôi phục trạng thái `BusinessDate` từ snapshot, đồng thời đánh dấu `FolioItem` sinh ra bởi luồng lỗi thành trạng thái `VOID`.
