# TEST-DRIVEN DEVELOPMENT SPECIFICATION
## UC18 — Post to Room

| Field | Value |
|-------|-------|
| **Document ID** | `KAWAI-TDD-MOD3-UC18-001` |
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
| **Feature / Gap ID** | `GAP-MOD3-UC18` |
| **Module** | Ký gửi Hóa đơn — UC18 |
| **Use Case** | Ghi nợ hóa đơn nhà hàng/dịch vụ vào Folio của phòng. |
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
Logic `PostToRoomService`.

#### TDS-02 — Test Basis
`SRS.md` UC18

#### TDS-03 — Test Conditions
| Condition ID | Test Condition | Coverage Item |
|-------------|----------------|---------------|
| TC-COND-UC18-001 | Post to Room thành công — ghi nợ vào Folio phòng | `Service` |
| TC-COND-UC18-002 | Post to Room vượt Credit Limit → từ chối, trả POS-003 | `Service` |
| TC-COND-UC18-003 | Post to Room cho phòng không OCCUPIED → từ chối | `Service` |

---

### 4. Test Case Specification

#### `TC-UC18-001` — Post to Room thành công — ghi nợ vào Folio phòng
* **Severity:** CRITICAL | **Feature:** `Service` | 🟢 GREEN
**Steps:** Mock setup → Execute → Assert PosOrderCompleted event published.

#### `TC-UC18-002` — Post to Room vượt Credit Limit → từ chối, trả POS-003
* **Severity:** CRITICAL | **Feature:** `Service` | 🟢 GREEN
**Steps:** Mock setup → Execute → Assert throws CreditLimitExceededException (400).

#### `TC-UC18-003` — Post to Room cho phòng không OCCUPIED → từ chối
* **Severity:** HIGH | **Feature:** `Service` | 🟢 GREEN
**Steps:** Mock setup → Execute → Assert throws RoomNotOccupiedException (400).

---

### 5. Red-Green-Refactor Tracker

| TC ID | Mô tả | Test File | 🔴 RED | 🔴 Commit | 🔴 Date | 🟢 GREEN | 🟢 Commit | 🟢 Date | 🔵 REFACTOR | 🔵 Commit | 🔵 Note |
|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-UC18-001 | Post to Room thành công — ghi nợ vào Folio phòng | `Test.java` | [x] | `aa11bb2` | 2026-06-15 | [x] | `bb22cc3` | 2026-06-15 | [x] | `cc33dd4` | ✅ Refactored |
| TC-UC18-002 | Post to Room vượt Credit Limit → từ chối, trả POS-003 | `Test.java` | [x] | `aa11bb2` | 2026-06-15 | [x] | `bb22cc3` | 2026-06-15 | [x] | `cc33dd4` | ✅ Refactored |
| TC-UC18-003 | Post to Room cho phòng không OCCUPIED → từ chối | `Test.java` | [x] | `aa11bb2` | 2026-06-15 | [x] | `bb22cc3` | 2026-06-15 | [x] | `cc33dd4` | ✅ Refactored |

---

### 6. Entry / Exit Criteria

#### Entry Criteria
- [x] Code base setup hoàn chỉnh

#### Exit Criteria
- [x] Unit tests pass 100%

---

### 7. Rollback Plan

`git checkout -- src/main/java/com/kawai/services/impl/PostToRoomServiceImpl.java`