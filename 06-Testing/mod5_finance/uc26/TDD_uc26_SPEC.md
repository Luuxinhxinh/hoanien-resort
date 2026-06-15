# TEST-DRIVEN DEVELOPMENT SPECIFICATION
## UC26 — Dashboard Tài chính

| Field | Value |
|-------|-------|
| **Document ID** | `KAWAI-TDD-MOD5-UC26-001` |
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
| **Feature** | UC26: Dashboard Tài chính |
| **Priority** | 🟡 P2 |

---

### 2. Logic Issues Resolved
- Tối ưu query DB (Group By SQL) thay vì xử lý bằng for loop trong Java.

---

### 3. Test Design Specification (TDS)
#### TDS-01 — Scope / Phạm vi
Test logic tính toán Aggregate data của ReportService.

---

### 4. Test Case Specification

#### `TC-UC26-001` — Dashboard dữ liệu biểu đồ
* **Severity:** MEDIUM | **Feature:** UC26.1
**Steps:** Mock DB trả về 3 transaction -> Gọi API getRevenue -> Verify list DTO gom nhóm doanh thu theo ngày đúng.

#### `TC-UC26-002` — Tính Occupancy Rate
* **Severity:** MEDIUM | **Feature:** UC26.2
**Steps:** Mock Repo trả 20 phòng tổng, 5 phòng OCCUPIED -> Verify rate trả về là 25.0%.

---

### 5. Red-Green-Refactor Tracker

| TC ID | Mô tả | Test File | 🔴 RED | 🟢 GREEN | 🔵 REFACTOR |
|---|---|---|---|---|---|
| TC-UC26-001 | Revenue Data | `DashboardTest.java` | [x] | [x] | ✅ Native Query Optimization |
| TC-UC26-002 | Occupancy | `DashboardTest.java` | [x] | [x] | ✅ Double division safe |

---

### 6. Entry / Exit Criteria
- **Exit:** Unit tests cho ReportService pass.

---

### 7. Rollback Plan
N/A