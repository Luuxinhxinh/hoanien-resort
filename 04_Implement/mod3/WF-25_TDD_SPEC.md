# TEST-DRIVEN DEVELOPMENT SPECIFICATION TEMPLATE
## WF-25 — Chốt Ca & Báo Cáo F&B

| Field | Value |
| --- | --- |
| **Document ID** | `KAWAI-TDD-WF25` |
| **Version** | 1.1 |
| **Date** | 2026-07-02 |
| **Status** | Approved |
| **Author** | Trịnh Minh Đức |

---

### 1. Thông tin Module

| Field | Value |
| --- | --- |
| **Module** | `Module 3 - F&B Shift/Report` |
| **Spec gốc** | `EDS_WF25_SPEC.md` |
| **Priority** | HIGH |

---

### 2. Test Design Specification (TDS)

#### TDS-01 — Scope / Phạm vi
- `ShiftReportServiceImpl.closeShift()`
- `FoodOrderRepository` (Query đơn hàng)
- `FbShiftReportRepository` (Lưu lịch sử chốt ca)

#### TDS-02 — Test Basis
- **Tính toán Doanh Thu**: Gom tất cả hóa đơn thanh toán trực tiếp tại POS (Cash, Card) và thống kê hóa đơn Charge-to-Room kể từ lần chốt ca cuối cùng.
- **Tính Idempotent**: Nếu gọi lại chốt ca liên tiếp mà không có giao dịch (đơn) nào phát sinh mới, thì hệ thống phải báo lỗi hoặc trả về report rỗng không hợp lệ (Không có dữ liệu để chốt).

---

### 3. Test Case Specification

#### `MOD3-TC-WF25-01` — Chốt ca thành công có dữ liệu
*   **Severity:** HIGH
*   **Feature Under Test:** `ShiftReportServiceImpl.closeShift()`
*   **TDD Phase:** 🟢 GREEN

**Preconditions:**
- Mock `FoodOrderRepository.findOrdersSinceLastShift()` trả về list gồm:
  - Order 1: 500,000 VND (Cash)
  - Order 2: 300,000 VND (Card)
  - Order 3: 400,000 VND (Charge_To_Room)
- Có 1 user đang thực hiện thao tác (F&B Staff).

**Test Steps:**
1. Call `closeShift()`.
2. Kiểm tra Object `FbShiftReport` trả về.

**Expected Result (PASS):**
- Tổng Cash = 500,000.
- Tổng Card = 300,000.
- Tổng Charge_To_Room = 400,000.
- `FbShiftReportRepository.save()` được gọi.

#### `MOD3-TC-WF25-02` — Chốt ca không có đơn hàng mới (Empty Data)
*   **Severity:** LOW
*   **Feature Under Test:** `ShiftReportServiceImpl.closeShift()`
*   **TDD Phase:** 🟢 GREEN

**Preconditions:**
- Mock `FoodOrderRepository.findOrdersSinceLastShift()` trả về List rỗng (0 order).

**Test Steps:**
1. Call `closeShift()`.

**Expected Result (PASS):**
- Throw `BusinessException` với nội dung "Không có giao dịch mới để chốt ca" (HTTP 400).

---

### 4. Red-Green-Refactor Tracker

| TC ID | Test File | 🔴 RED confirmed | 🟢 GREEN (commit) | 🔵 REFACTOR note |
| --- | --- | :---: | --- | --- |
| `MOD3-TC-WF25-01` | `ShiftReportServiceTest.java` | `[x]` | `[x]` | Đã pass, kiểm tra tính toán tổng đúng. |
| `MOD3-TC-WF25-02` | `ShiftReportServiceTest.java` | `[x]` | `[x]` | Đã pass. |

---

### 5. Entry / Exit Criteria

#### Exit Criteria
- [x] Code đã thực thi thành công, test qua toàn bộ (`mvn test` pass).
- [x] Đã hoàn thành phase GREEN và refactor.
