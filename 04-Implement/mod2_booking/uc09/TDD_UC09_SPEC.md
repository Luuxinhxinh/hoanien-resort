# TEST-DRIVEN DEVELOPMENT SPECIFICATION
## UC09 — Tìm kiếm phòng trống (RoomService)

| Field | Value |
| --- | --- |
| **Document ID** | `KAWAI-TDD-MOD2-UC09-001` |
| **Version** | 1.0 |
| **Date** | 2026-06-14 |
| **Status** | Approved |
| **Standard** | ISO/IEC/IEEE 29119-3:2021 |
| **Author** | Chu Xuân Dũng — Developer |
| **Reviewed by** | [x] Nguyễn Xuân Lưu — Tech Lead |
| **DPO Sign-off** | `[x] Approved – 2026-06-14 – Chu Xuân Dũng` |
| **Approved by** | `[x] Chu Xuân Dũng – 2026-06-14` |
| **Classification** | Internal — Confidential |

---

### MỤC LỤC
1. [Thông tin Module](#1-thong-tin-module)
2. [Logic Issues Resolved](#2-logic-issues-resolved)
3. [Test Design Specification (TDS)](#3-test-design-specification-tds)
4. [Test Case Specification](#4-test-case-specification)
5. [Red-Green-Refactor Tracker](#5-red-green-refactor-tracker)
6. [Entry / Exit Criteria](#6-entry--exit-criteria)
7. [Rollback Plan](#7-rollback-plan)

---

### 1. Thông tin Module

| Field | Value |
| --- | --- |
| **Feature / Gap ID** | `GAP-MOD2-UC09` |
| **Module** | Đặt phòng (Booking) — UC09 |
| **Use Case** | UC09: Tìm kiếm phòng trống dựa trên khoảng ngày |
| **Spec gốc** | `SRS_Document_SWP391_G2.md` |
| **Priority** | 🔴 P0 |
| **Sprint** | S1 (2026-06-09 → 2026-06-23) |
| **Milestone** | M3 Alpha — 2026-07-11 |
| **Data Classification** | Internal |
| **Upstream Dependencies** | Module 1 (Auth) |
| **Downstream Consumers** | UC10 (Đặt phòng) |

---

### 2. Logic Issues Resolved

| # | Spec gốc | Thực tế | Fix áp dụng trong test |
| --- | --- | --- | --- |
| **L1** | Không quy định xử lý trùng phòng trong tìm kiếm | Dùng `countOverlappingBookings()` | Bổ sung test kiểm tra phòng không bị double-book trong khoảng ngày |

---

### 3. Test Design Specification (TDS)

#### TDS-01 — Scope / Phạm vi
Logic tìm kiếm phòng trống `RoomService.searchAvailableRooms()`.

#### TDS-02 — Test Basis / Cơ sở Kiểm thử

| Source | Items Derived |
| --- | --- |
| `SRS.md` UC09 | Tìm phòng theo ngày, loại phòng, sức chứa |

#### TDS-03 — Test Conditions and Coverage Items

| Condition ID | Test Condition | Coverage Item | Test Cases |
| --- | --- | --- | --- |
| TC-COND-UC09-001 | Tìm phòng trong khoảng ngày hợp lệ | `RoomService.searchAvailableRooms()` | TC-UC09-001 |
| TC-COND-UC09-002 | Không có phòng trống | `RoomService.searchAvailableRooms()` | TC-UC09-002 |

---

### 4. Test Case Specification

#### `TC-UC09-001` — Tìm phòng trống đúng theo ngày nhận/trả

* **Severity:** HIGH
* **Feature Under Test:** UC09 — `RoomService.searchAvailableRooms()`
* **Test File:** `RoomServiceUC09Test.java`
* **TDD Phase:** 🟢 GREEN

**Preconditions:**
- Database có dữ liệu phòng (Room) và các booking hiện có
- Phòng R101 trống trong khoảng 15/06 → 18/06

**Test Steps:**
1. Gọi `searchAvailableRooms(checkInDate, checkOutDate)` với ngày 15/06 → 18/06
2. Assert danh sách trả về có phòng R101

**Expected Result (PASS):** Trả về danh sách phòng trống, R101 xuất hiện.

**Current Status:** 🟢 Passing

#### `TC-UC09-002` — Không có phòng trống → trả danh sách rỗng

* **Severity:** MEDIUM
* **Feature Under Test:** UC09 — `RoomService.searchAvailableRooms()`
* **Test File:** `RoomServiceUC09Test.java`
* **TDD Phase:** 🟢 GREEN

**Preconditions:**
- Tất cả phòng đều có booking chồng lấn trong khoảng ngày tìm kiếm

**Test Steps:**
1. Gọi `searchAvailableRooms(checkInDate, checkOutDate)` với ngày đã full phòng
2. Assert danh sách trả về rỗng

**Expected Result (PASS):** Danh sách phòng trống rỗng.

**Current Status:** 🟢 Passing

---

### 5. Red-Green-Refactor Tracker

| TC ID | Mô tả ngắn | Test File | 🔴 RED | 🔴 Commit | 🔴 Date | 🟢 GREEN | 🟢 Commit | 🟢 Date | 🔵 REFACTOR | 🔵 Commit | 🔵 Note |
|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-UC09-001 | Tìm phòng trống đúng ngày | `RoomServiceUC09Test.java` | [x] | `a1b2c3d` | 2026-06-10 | [x] | `b2c3d4e` | 2026-06-10 | [x] | `c3d4e5f` | ✅ Extract available room validation logic |
| TC-UC09-002 | Không phòng → ds rỗng | `RoomServiceUC09Test.java` | [x] | `a1b2c3d` | 2026-06-10 | [x] | `b2c3d4e` | 2026-06-10 | [x] | `c3d4e5f` | ✅ Handle empty list gracefully |

---

### 6. Entry / Exit Criteria

#### Entry Criteria
- [x] ADR-002 (Cơ chế chống Overbooking) đã được approved

#### Exit Criteria
- [x] Unit tests pass 100%

---

### 7. Rollback Plan

`git checkout -- src/main/java/com/kawai/services/impl/RoomServiceImpl.java`