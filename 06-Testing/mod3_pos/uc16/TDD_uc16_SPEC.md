# TEST-DRIVEN DEVELOPMENT SPECIFICATION
## UC16 — POS Dine-In & Payment

| Field | Value |
|-------|-------|
| **Document ID** | `KAWAI-TDD-MOD3-UC16-001` |
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
| **Feature / Gap ID** | `GAP-MOD3-UC16` |
| **Module** | POS & Thanh toán — UC16 |
| **Use Case** | Tạo order Dine-In, thanh toán tiền mặt tại POS. |
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
Logic `PosService`.

#### TDS-02 — Test Basis
`SRS.md` UC16

#### TDS-03 — Test Conditions
| Condition ID | Test Condition | Coverage Item |
|-------------|----------------|---------------|
| TC-COND-UC16-001 | POS tạo order Dine-In — ghi đúng bàn, đúng món, đúng giá | `Service` |
| TC-COND-UC16-002 | POS thanh toán tiền mặt — đơn hàng chuyển PAID | `Service` |

---

### 4. Test Case Specification

#### `TC-UC16-001` — POS tạo order Dine-In — ghi đúng bàn, đúng món, đúng giá
* **Severity:** CRITICAL | **Feature:** `Service` | 🟢 GREEN
**Steps:** Mock setup → Execute → Assert OrderCreated event, status DINE_IN.

#### `TC-UC16-002` — POS thanh toán tiền mặt — đơn hàng chuyển PAID
* **Severity:** HIGH | **Feature:** `Service` | 🟢 GREEN
**Steps:** Mock setup → Execute → Assert Order status changed to PAID.

---

### 5. Red-Green-Refactor Tracker

| TC ID | Mô tả | Test File | 🔴 RED | 🔴 Commit | 🔴 Date | 🟢 GREEN | 🟢 Commit | 🟢 Date | 🔵 REFACTOR | 🔵 Commit | 🔵 Note |
|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-UC16-001 | POS tạo order Dine-In — ghi đúng bàn, đúng món, đúng giá | `Test.java` | [x] | `aa11bb2` | 2026-06-15 | [x] | `bb22cc3` | 2026-06-15 | [x] | `cc33dd4` | ✅ Refactored |
| TC-UC16-002 | POS thanh toán tiền mặt — đơn hàng chuyển PAID | `Test.java` | [x] | `aa11bb2` | 2026-06-15 | [x] | `bb22cc3` | 2026-06-15 | [x] | `cc33dd4` | ✅ Refactored |

---

### 6. Entry / Exit Criteria

#### Entry Criteria
- [x] Code base setup hoàn chỉnh

#### Exit Criteria
- [x] Unit tests pass 100%

---

### 7. Rollback Plan

`git checkout -- src/main/java/com/kawai/services/impl/PosServiceImpl.java`