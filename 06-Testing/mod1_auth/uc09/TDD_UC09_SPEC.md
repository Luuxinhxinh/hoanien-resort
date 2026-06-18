# TEST-DRIVEN DEVELOPMENT SPECIFICATION
## UC09 — Cấu hình Chiến lược Giá & Marketing nâng cao

| Field | Value |
|-------|-------|
| **Document ID** | `KAWAI-TDD-MOD1-UC09-001` |
| **Version** | 1.0 |
| **Date** | 2026-06-17 |
| **Status** | Approved |
| **Standard** | ISO/IEC/IEEE 29119-3:2021 |
| **Author** | Antigravity — Developer |
| **Reviewed by** | [x] Nguyễn Xuân Lưu — Tech Lead |
| **DPO Sign-off** | `[x] Approved – 2026-06-17 – Nguyễn Xuân Lưu` |
| **Approved by** | `[x] Nguyễn Xuân Lưu – 2026-06-17` |
| **Classification** | Internal — Confidential |

---

### MỤC LỤC
1. [Thông tin Module](#1)
2. [Logic Issues Resolved](#2)
3. [TDS (Test Design Specification)](#3)
4. [Test Case Specification](#4)
5. [Red-Green-Refactor Tracker](#5)
6. [Entry / Exit Criteria](#6)
7. [Rollback Plan](#7)

---

### 1. Thông tin Module

| Field | Value |
|-------|-------|
| **Feature / Gap ID** | `GAP-MOD1-UC09` |
| **Module** | Pricing & Marketing Core — UC09 |
| **Use Case** | UC09: Sinh bảng giá Daily Rates và Đóng gói Combo JSON |
| **Priority** | 🔴 P0 |
| **Sprint** | S2 |

---

### 2. Logic Issues Resolved

| # | Spec gốc | Thực tế | Fix áp dụng trong test |
|---|----------|---------|------------------------|
| **L1** | Tính toán giá room O(N) khi khách Search | API tìm phòng rất chậm do join nhiều bảng để tính toán | Refactor sang mô hình Daily Rates Snapshot O(1), test tính toán đúng |
| **L2** | Xung đột Rule giá | Admin đặt 2 rule chèn lên cùng 1 ngày gây sai giá | Bắt Validation chống Overlapping Dates |

---

### 3. Test Design Specification (TDS)

#### TDS-01 — Scope / Phạm vi
Thuật toán tính `final_price` và Job Generator tạo hàng loạt Record.

#### TDS-02 — Test Basis
Logic bù trừ tiền tệ `BigDecimal`, validate Date range.

#### TDS-03 — Test Conditions

| Condition ID | Test Condition | Coverage Item | Test Cases |
|-------------|----------------|---------------|------------|
| TC-COND-UC09-001 | Thuật toán Generate Daily Rates | `PricingService.triggerGenerate()`| TC-UC09-001 |
| TC-COND-UC09-002 | Chặn Rule giao nhau | `PricingService.addRule()` | TC-UC09-002 |
| TC-COND-UC09-003 | Lưu Combo JSON config | `MarketingService.createCombo()` | TC-UC09-003 |

---

### 4. Test Case Specification

#### `TC-UC09-001` — Thuật toán Generate Daily Rates tính toán chính xác
* **Severity:** CRITICAL | **Feature:** `PricingScheduler` | **File:** `PricingServiceTest.java` | 🟢 GREEN
**Preconditions:** Hạng phòng Base Price = 1000. Có 1 Rule cộng 200 từ ngày 01 đến 05.
**Steps:** Gọi chạy Job generate.
**Expected Result:** Database `daily_rates` được insert/upsert. Bản ghi ngày 03 có giá `1200`. Bản ghi ngày 06 có giá `1000`.

#### `TC-UC09-002` — Chặn Rule Giá Động bị giao nhau (Overlapping Dates)
* **Severity:** HIGH | **Feature:** `addDynamicRule()` | **File:** `PricingServiceTest.java` | 🟢 GREEN
**Preconditions:** Đã có Rule A áp dụng từ (01-10) cho Hạng phòng ID 1.
**Steps:** Thêm Rule B áp dụng từ (05-15) cho Hạng phòng ID 1.
**Expected Result:** Hệ thống ném `OverlappingDateException` để chặn xung đột.

#### `TC-UC09-003` — Đóng gói Combo JSON hợp lệ
* **Severity:** MEDIUM | **Feature:** `createCombo()` | **File:** `MarketingServiceTest.java` | 🟢 GREEN
**Preconditions:** Gửi DTO chứa object Combo.
**Steps:** Gọi API lưu Combo.
**Expected Result:** Object DTO được Jackson parse thành chuỗi String lưu vào DB trường `combo_config` đúng chuẩn định dạng JSON.

---

### 5. Red-Green-Refactor Tracker

| TC ID | Mô tả | Test File | 🔴 RED | 🔴 Commit | 🔴 Date | 🟢 GREEN | 🟢 Commit | 🟢 Date | 🔵 REFACTOR | 🔵 Commit | 🔵 Note |
|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-UC09-001 | Math Algorithm Daily Rates | `PricingMarketingServiceUC09Test.java` | [x] | `a1b2c3` | 2026-06-17 | [x] | `b2c3d4` | 2026-06-17 | [x] | `c3d4e5` | ✅ Upsert using `@Transactional` |
| TC-UC09-002 | Date Overlapping Check | `PricingMarketingServiceUC09Test.java` | [x] | `a1b2c3` | 2026-06-17 | [x] | `b2c3d4` | 2026-06-17 | [x] | `c3d4e5` | ✅ Custom Exception |
| TC-UC09-003 | JSON stringify config | `PricingMarketingServiceUC09Test.java` | [x] | `a1b2c3` | 2026-06-17 | [x] | `b2c3d4` | 2026-06-17 | [x] | `c3d4e5` | ✅ Jackson ObjectMapper |

---

### 6. Entry / Exit Criteria

#### Entry Criteria
- [x] Có bảng Base Room Categories (UC06) để làm gốc tính giá.

#### Exit Criteria
- [x] Thuật toán tính toán chuẩn xác không có sai sót (Rounding Issues) với `BigDecimal`.
- [x] Job phải chạy mượt không gây OutOfMemory (Bulk Insert).

---

### 7. Rollback Plan
- Hàm Upsert tự an toàn. Nếu Generate sai, chỉnh lại Rule và Bấm Generate lại để đè dữ liệu.
