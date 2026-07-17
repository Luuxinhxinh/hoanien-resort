# TEST-DRIVEN DEVELOPMENT SPECIFICATION
## UC11 — Xem sơ đồ Matrix phòng trống (RoomService Dashboard)

| Field | Value |
|-------|-------|
| **Document ID** | `KAWAI-TDD-MOD2-UC11-001` |
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
|-------|-------|
| **Feature / Gap ID** | `GAP-MOD2-UC11` |
| **Module** | Đặt phòng (Booking) — UC11 |
| **Use Case** | UC11: Dashboard Room Matrix |
| **Spec gốc** | `SRS_Document_SWP391_G2.md` |
| **Priority** | 🟡 P2 |
| **Sprint** | S1 (2026-06-09 → 2026-06-23) |
| **Milestone** | M3 Alpha — 2026-07-11 |
| **Data Classification** | Internal |
| **Upstream Dependencies** | UC10 (Đặt phòng), UC12 (Check-in), UC13 (Housekeeping) |
| **Downstream Consumers** | Receptionist UI, Manager UI |

---

### 2. Logic Issues Resolved

| # | Spec gốc | Thực tế | Fix áp dụng trong test |
|---|----------|---------|------------------------|
| **L1** | Không quy định DTO cho dashboard | Tạo `RoomDashboardDTO` | Extract `toDashboardDTO()` helper method, dùng Java Streams |

---

### 3. Test Design Specification (TDS)

#### TDS-01 — Scope / Phạm vi
Logic `RoomService.getDashboardData()` — truy vấn tổng hợp trạng thái các phòng, trả về DTO cho màn hình Dashboard của lễ tân/quản lý.

#### TDS-02 — Test Basis / Cơ sở Kiểm thử

| Source | Items Derived |
|--------|---------------|
| `SRS.md` UC11 | Dashboard hiển thị trạng thái từng phòng, thống kê occupancy rate |

#### TDS-03 — Test Conditions and Coverage Items

| Condition ID | Test Condition | Coverage Item | Test Cases |
|-------------|----------------|---------------|------------|
| TC-COND-UC11-001 | Dashboard trả đúng danh sách phòng + trạng thái | `RoomService.getDashboardData()` | TC-UC11-001 |

---

### 4. Test Case Specification

#### `TC-UC11-001` — Dashboard trả đúng danh sách phòng + trạng thái

* **Severity:** HIGH
* **Feature Under Test:** UC11 — `RoomService.getDashboardData()`
* **Test File:** `RoomServiceUC11Test.java`
* **TDD Phase:** 🟢 GREEN

**Preconditions:**
- DB có 3 phòng: R101 (OCCUPIED), R102 (VACANT_CLEAN), R103 (DIRTY)
- R101 có khách đang ở, R102 trống sạch, R103 đang chờ dọn

**Test Steps:**
1. Gọi `getDashboardData()`
2. Assert danh sách trả về đủ 3 phòng
3. Assert trạng thái từng phòng đúng (R101=Occupied, R102=Vacant_Clean, R103=Dirty)
4. Assert thống kê: totalRooms=3, occupied=1, vacant=1, dirty=1

**Expected Result (PASS):** Dashboard chứa thông tin đầy đủ trạng thái real-time của tất cả phòng.

**Current Status:** 🟢 Passing

---

### 5. Red-Green-Refactor Tracker

| TC ID | Mô tả ngắn | Test File | 🔴 RED | 🔴 Commit | 🔴 Date | 🟢 GREEN | 🟢 Commit | 🟢 Date | 🔵 REFACTOR | 🔵 Commit | 🔵 Note |
|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-UC11-001 | Dashboard Room Matrix | `RoomServiceUC11Test.java` | [x] | `da47c4d` | 2026-06-12 | [x] | `da47c4d` | 2026-06-12 | [x] | `e5f6g7h` | ✅ Extract `toDashboardDTO`, use Java Streams, add JavaDoc |

---

### 6. Entry / Exit Criteria

#### Entry Criteria
- [x] Các service RoomService, CheckinService, HousekeepingService đã hoạt động

#### Exit Criteria
- [x] Unit tests pass 100%

---

### 7. Rollback Plan

`git checkout -- src/main/java/com/kawai/services/impl/RoomServiceImpl.java`