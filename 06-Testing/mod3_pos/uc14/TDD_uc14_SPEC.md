# TEST-DRIVEN DEVELOPMENT SPECIFICATION
## UC14 — Menu and E-Menu / Room Service

| Field | Value |
|-------|-------|
| **Document ID** | `KAWAI-TDD-MOD3-UC14-001` |
| **Version** | 2.0 |
| **Date** | 2026-06-17 |
| **Status** | Approved |
| **Standard** | ISO/IEC/IEEE 29119-3:2021 |
| **Author** | Trịnh Minh Đức — Developer |
| **Reviewed by** | `[x] Trịnh Minh Đức — Tech Lead` |
| **DPO Sign-off** | `[x] Approved – 2026-06-15 – Trịnh Minh Đức` |
| **Approved by** | `[x] Trịnh Minh Đức – 2026-06-15` |
| **Classification** | Internal — Confidential |

---

### CHANGELOG
| Ngày | Người thực hiện | Nội dung thay đổi |
|------|-----------------|-------------------|
| 2026-06-17 | Trịnh Minh Đức | **v2.0** — Để đúng Use Case: UC14 = Đặt bàn nhà hàng. File test này được chia sẻ bởi UC16 (Room Service) vì các test case TC-M3-004 và TC-M3-005 thuộc về Room Service. Cập nhật mã TC chuẩn và Test File thực tế. |
| 2026-06-15 | Trịnh Minh Đức | v1.0 — Khởi tạo tài liệu theo chuẩn TDD |

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
| **Feature / Gap ID** | `GAP-MOD3-UC14` |
| **Module** | Menu & E-Menu — UC14 |
| **Use Case** | Khách quét QR hiển thị E-Menu, đặt Room Service. |
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
Logic `MenuService`.

#### TDS-02 — Test Basis
`SRS.md` UC14

#### TDS-03 — Test Conditions
| Condition ID | Test Condition | Coverage Item |
|-------------|----------------|---------------|
| TC-COND-M3-004 | Khách quét QR phòng 101 → gọi món Room Service thành công, CreditLimit bị trừ đúng | `PosApiController` |
| TC-COND-M3-005 | Gọi món Room Service khi CreditLimit không đủ → hệ thống từ chối HTTP 400 | `PosApiController` |

---

### 4. Test Case Specification

#### `TC-M3-004` — Khách quét QR → hiển thị E-Menu, đặt Room Service thành công
* **Severity:** HIGH | **Feature:** `Service` | 🟢 GREEN
**Steps:** Mock setup → Execute → Assert Pass 100% assertions.

#### `TC-M3-005` — Room Service cho phòng không OCCUPIED → từ chối
* **Severity:** MEDIUM | **Feature:** `Service` | 🟢 GREEN
**Steps:** Mock setup → Execute → Assert throws RoomNotOccupiedException (400).

---

### 5. Red-Green-Refactor Tracker

| TC ID | Mô tả | Test File | 🔴 RED | 🔴 Commit | 🔴 Date | 🟢 GREEN | 🟢 Commit | 🟢 Date | 🔵 REFACTOR | 🔵 Commit | 🔵 Note |
|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-M3-004 | Khách quét QR → hiển thị E-Menu, đặt Room Service thành công | `PosApiControllerUC16Test.java` | [x] | `PENDING` | 2026-06-17 | [x] | `PENDING` | 2026-06-17 | [x] | `PENDING` | ✅ Retroactive TDD |
| TC-M3-005 | Room Service cho phòng không OCCUPIED / Vượt hạn mức → từ chối | `PosApiControllerUC16Test.java` | [x] | `PENDING` | 2026-06-17 | [x] | `PENDING` | 2026-06-17 | [x] | `PENDING` | ✅ Validate logic |

---

### 6. Entry / Exit Criteria

#### Entry Criteria
- [x] Code base setup hoàn chỉnh

#### Exit Criteria
- [x] Unit tests pass 100%

---

### 7. Rollback Plan

`git checkout -- src/main/java/com/kawai/controllers/api/PosApiController.java`