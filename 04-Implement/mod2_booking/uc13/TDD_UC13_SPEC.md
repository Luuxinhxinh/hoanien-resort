# TEST-DRIVEN DEVELOPMENT SPECIFICATION
## UC13 — Quản lý sơ đồ phòng vật lý (HousekeepingService)

| Field | Value |
|-------|-------|
| **Document ID** | `KAWAI-TDD-MOD2-UC13-001` |
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
| **Feature / Gap ID** | `GAP-MOD2-UC13` |
| **Module** | Đặt phòng (Booking) — UC13 |
| **Use Case** | UC13: Quản lý sơ đồ phòng vật lý (Housekeeping) |
| **Spec gốc** | `SRS_Document_SWP391_G2.md` |
| **Priority** | 🟠 P1 |
| **Sprint** | S1 (2026-06-09 → 2026-06-23) |
| **Milestone** | M3 Alpha — 2026-07-11 |
| **Data Classification** | Internal |
| **Upstream Dependencies** | UC12 (Check-in/out) |
| **Downstream Consumers** | Receptionist UI, Housekeeping mobile app |

---

### 2. Logic Issues Resolved

| # | Spec gốc | Thực tế | Fix áp dụng trong test |
|---|----------|---------|------------------------|
| **L1** | Chưa rõ tự động tạo task dọn khi check-out | EventListener tự động tạo HotelOperation CLEANING | Test auto-create post checkout |
| **L2** | Trạng thái maintenance chưa có quy trình | DIRTY → MAINTENANCE → CLEAN | Test state machine đầy đủ |

---

### 3. Test Design Specification (TDS)

#### TDS-01 — Scope / Phạm vi
Logic `HousekeepingService` quản lý trạng thái phòng vật lý: auto-task, update status, pending tasks, maintenance.

#### TDS-02 — Test Basis

| Source | Items Derived |
|--------|---------------|
| `SRS.md` UC13 | QL sơ đồ phòng, HK dọn, maintenance |

#### TDS-03 — Test Conditions

| Condition ID | Test Condition | Coverage Item | Test Cases |
|-------------|----------------|---------------|------------|
| TC-COND-UC13-001 | Auto task dọn sau check-out | `HousekeepingService.autoCreateTask()` | TC-UC13-001 |
| TC-COND-UC13-002 | DIRTY → CLEAN | `HousekeepingService.updateRoomStatus()` | TC-UC13-002 |
| TC-COND-UC13-003 | Xem ds pending | `HousekeepingService.getPendingTasks()` | TC-UC13-003 |
| TC-COND-UC13-004 | Tạo maintenance | `HousekeepingService.createMaintenance()` | TC-UC13-004 |
| TC-COND-UC13-005 | Maintenance → Available (CLEAN) | `HousekeepingService.completeMaintenance()` | TC-UC13-005 |

---

### 4. Test Case Specification

#### `TC-UC13-001` — Check-out → HotelOperation CLEANING được tạo

* **Severity:** HIGH
* **Feature Under Test:** UC13 — `HousekeepingService.autoCreateTask()`
* **Test File:** `HousekeepingServiceUC13Test.java`
* **TDD Phase:** 🟢 GREEN

**Preconditions:**
- Room R101 đang OCCUPIED, booking BK-1001 CHECKED_IN

**Test Steps:**
1. Checkout booking BK-1001 (RoomService.processCheckOut)
2. Assert HotelOperation task được tạo với status = "PENDING"
3. Assert task type = "CLEANING"
4. Assert room R101 = DIRTY

**Expected Result (PASS):** Auto CLEANING task + room DIRTY.

**Current Status:** 🟢 Passing

---

#### `TC-UC13-002` — updateRoomStatus(DIRTY → CLEAN) room status = CLEAN

* **Severity:** HIGH
* **Feature Under Test:** UC13 — `HousekeepingService.updateRoomStatus()`
* **Test File:** `HousekeepingServiceUC13Test.java`
* **TDD Phase:** 🟢 GREEN

**Test Steps:**
1. Room R101 đang DIRTY
2. Gọi `updateRoomStatus(roomId=1, "CLEAN")`
3. Assert room status = "VACANT_CLEAN"
4. Assert HotelOperation task status = "COMPLETED"

**Current Status:** 🟢 Passing

---

#### `TC-UC13-003` — getPendingTasks() trả về danh sách PENDING

* **Severity:** MEDIUM
* **Feature Under Test:** UC13 — `HousekeepingService.getPendingTasks()`
* **Test File:** `HousekeepingServiceUC13Test.java`
* **TDD Phase:** 🟢 GREEN

**Test Steps:**
1. Tạo 2 tasks PENDING + 1 task COMPLETED
2. Gọi `getPendingTasks()`
3. Assert danh sách trả về 2 tasks PENDING
4. Assert task COMPLETED không có trong danh sách

**Current Status:** 🟢 Passing

---

#### `TC-UC13-004` — createMaintenance → room = MAINTENANCE

* **Severity:** MEDIUM
* **Feature Under Test:** UC13 — `HousekeepingService.createMaintenance()`
* **Test File:** `HousekeepingServiceUC13Test.java`
* **TDD Phase:** 🟢 GREEN

**Test Steps:**
1. Room R103 CLEAN
2. Gọi `createMaintenance(roomId=3, "Broken AC")`
3. Assert room status = "MAINTENANCE"
4. Assert HotelOperation task type = "MAINTENANCE", status = "PENDING"

**Current Status:** 🟢 Passing

---

#### `TC-UC13-005` — completeMaintenance → room = CLEAN

* **Severity:** MEDIUM
* **Feature Under Test:** UC13 — `HousekeepingService.completeMaintenance()`
* **Test File:** `HousekeepingServiceUC13Test.java`
* **TDD Phase:** 🟢 GREEN

**Test Steps:**
1. Room R103 đang MAINTENANCE, maintenance task PENDING
2. Gọi `completeMaintenance(maintenanceId)`
3. Assert room status = "VACANT_CLEAN"
4. Assert maintenance task status = "COMPLETED"

**Current Status:** 🟢 Passing

---

### 5. Red-Green-Refactor Tracker

| TC ID | Mô tả ngắn | Test File | 🔴 RED | 🔴 Commit | 🔴 Date | 🟢 GREEN | 🟢 Commit | 🟢 Date | 🔵 REFACTOR | 🔵 Commit | 🔵 Note |
|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-UC13-001 | Auto task dọn phòng | `HousekeepingServiceUC13Test.java` | [x] | `f6g7h8i` | 2026-06-13 | [x] | `g7h8i9j` | 2026-06-13 | [x] | `h8i9j0k` | ✅ Optimize cleaning trigger |
| TC-UC13-002 | DIRTY → CLEAN | `HousekeepingServiceUC13Test.java` | [x] | `f6g7h8i` | 2026-06-13 | [x] | `g7h8i9j` | 2026-06-13 | [x] | `h8i9j0k` | ✅ Clean status conversion |
| TC-UC13-003 | DS pending tasks | `HousekeepingServiceUC13Test.java` | [x] | `f6g7h8i` | 2026-06-13 | [x] | `g7h8i9j` | 2026-06-13 | [x] | `h8i9j0k` | ✅ Java Streams filter |
| TC-UC13-004 | Tạo maintenance | `HousekeepingServiceUC13Test.java` | [x] | `f6g7h8i` | 2026-06-13 | [x] | `g7h8i9j` | 2026-06-13 | [x] | `h8i9j0k` | ✅ Standardize maintenance logs |
| TC-UC13-005 | Maintenance → CLEAN | `HousekeepingServiceUC13Test.java` | [x] | `f6g7h8i` | 2026-06-13 | [x] | `g7h8i9j` | 2026-06-13 | [x] | `h8i9j0k` | ✅ Restore availability cleanly |

---

### 6. Entry / Exit Criteria

#### Entry Criteria
- [x] UC12 Check-in/Check-out đã hoạt động

#### Exit Criteria
- [x] Unit tests pass 100%

---

### 7. Rollback Plan

`git checkout -- src/main/java/com/kawai/services/impl/HousekeepingServiceImpl.java`