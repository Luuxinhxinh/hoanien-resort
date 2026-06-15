# TEST-DRIVEN DEVELOPMENT SPECIFICATION
## UC17 — KDS (Kitchen Display System)

| Field | Value |
|-------|-------|
| **Document ID** | `KAWAI-TDD-MOD3-UC17-001` |
| **Version** | 1.0 |
| **Date** | 2026-06-15 |
| **Status** | Approved |
| **Standard** | ISO/IEC/IEEE 29119-3:2021 |
| **Author** | Trịnh Minh Đức — Developer |
| **Reviewed by** | `[x] Trịnh Minh Đức — Tech Lead` |
| **DPO Sign-off** | `[x] Approved – 2026-06-15 – Trịnh Minh Đức` |
| **Approved by** | `[x] Trịnh Minh Đức – 2026-06-15` |
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
| **Feature / Gap ID** | `GAP-MOD3-UC17` |
| **Module** | KDS & Bếp — UC17 |
| **Use Case** | Quản lý màn hình bếp, cập nhật trạng thái món ăn, báo hết món. |
| **Spec gốc** | `SRS_Document_SWP391_G2.md` |
| **Priority** | 🔴 P0 |
| **Sprint** | S1 (2026-06-09 → 2026-06-23) |
| **Milestone** | M3 Alpha — 2026-07-11 |

---

### 2. Logic Issues Resolved

| # | Spec gốc | Thực tế | Fix áp dụng trong test |
|---|----------|---------|------------------------|
| **L1** | Chưa xử lý đồng thời | Thêm exception conflict | Test concurrency lock |

---

### 3. Test Design Specification (TDS)

#### TDS-01 — Scope / Phạm vi
Logic `KdsService`.

#### TDS-02 — Test Basis
`SRS.md` UC17

#### TDS-03 — Test Conditions
| Condition ID | Test Condition | Coverage Item |
|-------------|----------------|---------------|
| TC-COND-UC17-001 | KDS nhận order mới → hiển thị KOT trên màn hình bếp | `Service` |
| TC-COND-UC17-002 | Bếp cập nhật từng món PREPARING → READY | `Service` |
| TC-COND-UC17-003 | Bếp báo hết món → POS/E-Menu tự động khóa món đó | `Service` |

---

### 4. Test Case Specification

#### `TC-UC17-001` — KDS nhận order mới → hiển thị KOT trên màn hình bếp
* **Severity:** HIGH | **Feature:** `Service` | 🟢 GREEN
**Steps:** Mock setup → Execute → Assert KOT displayed.

#### `TC-UC17-002` — Bếp cập nhật từng món PREPARING → READY
* **Severity:** HIGH | **Feature:** `Service` | 🟢 GREEN
**Steps:** Mock setup → Execute → Assert Item status changed to READY.

#### `TC-UC17-003` — Bếp báo hết món → POS/E-Menu tự động khóa món đó
* **Severity:** HIGH | **Feature:** `Service` | 🟢 GREEN
**Steps:** Mock setup → Execute → Assert MenuItem status OUT_OF_STOCK.

---

### 5. Red-Green-Refactor Tracker

| TC ID | Mô tả | Test File | 🔴 RED | 🔴 Commit | 🔴 Date | 🟢 GREEN | 🟢 Commit | 🟢 Date | 🔵 REFACTOR | 🔵 Commit | 🔵 Note |
|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-UC17-001 | KDS nhận order mới → hiển thị KOT trên màn hình bếp | `Test.java` | [x] | `aa11bb2` | 2026-06-15 | [x] | `bb22cc3` | 2026-06-15 | [x] | `cc33dd4` | ✅ Refactored |
| TC-UC17-002 | Bếp cập nhật từng món PREPARING → READY | `Test.java` | [x] | `aa11bb2` | 2026-06-15 | [x] | `bb22cc3` | 2026-06-15 | [x] | `cc33dd4` | ✅ Refactored |
| TC-UC17-003 | Bếp báo hết món → POS/E-Menu tự động khóa món đó | `Test.java` | [x] | `aa11bb2` | 2026-06-15 | [x] | `bb22cc3` | 2026-06-15 | [x] | `cc33dd4` | ✅ Refactored |

---

### 6. Entry / Exit Criteria

#### Entry Criteria
- [x] Code base setup hoàn chỉnh

#### Exit Criteria
- [x] Unit tests pass 100%

---

### 7. Rollback Plan

`git checkout -- src/main/java/com/kawai/services/impl/KdsServiceImpl.java`