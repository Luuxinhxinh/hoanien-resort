# TEST-DRIVEN DEVELOPMENT SPECIFICATION
## UC27 — Báo cáo USALI

| Field | Value |
|-------|-------|
| **Document ID** | `KAWAI-TDD-MOD5-UC27-001` |
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
| **Feature** | UC27: Báo cáo USALI |
| **Priority** | 🟡 P2 |

---

### 2. Logic Issues Resolved
N/A

---

### 3. Test Design Specification (TDS)
#### TDS-01 — Scope / Phạm vi
Categorization logic cho các loại phí (Charge Type) mapping sang bảng USALI.

---

### 4. Test Case Specification

#### `TC-UC27-001` — Báo cáo USALI phân tách doanh thu
* **Severity:** HIGH | **Feature:** UC27
**Steps:** Mock FolioItems với đủ loại (Room, Food, Tour) -> getReport -> Verify 3 bucket tương ứng được sum đúng.

---

### 5. Red-Green-Refactor Tracker

| TC ID | Mô tả | Test File | 🔴 RED | 🟢 GREEN | 🔵 REFACTOR |
|---|---|---|---|---|---|
| TC-UC27-001 | USALI Category | `UsaliReportTest.java` | [x] | [x] | ✅ Enum mapping |

---

### 6. Entry / Exit Criteria
- **Exit:** Logic map bucket chạy đúng 100%.

---

### 7. Rollback Plan
N/A