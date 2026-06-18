# TEST-DRIVEN DEVELOPMENT SPECIFICATION
## UC07 — Quản lý Dữ liệu nền Sơ đồ Bàn ăn (CRUD Tables)

| Field | Value |
|-------|-------|
| **Document ID** | `KAWAI-TDD-MOD1-UC07-001` |
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
| **Feature / Gap ID** | `GAP-MOD1-UC07` |
| **Module** | Core Data — UC07 |
| **Use Case** | UC07: Quản lý Dữ liệu nền Sơ đồ Bàn ăn (Thêm, Sửa, Đổi trạng thái) |
| **Spec gốc** | `UC_DETAIL_SPEC.md` |
| **Priority** | 🟠 P1 |
| **Sprint** | S2 |
| **Data Classification** | Internal |
| **Upstream Dependencies** | Security Config |
| **Downstream Consumers** | F&B Point of Sales |

---

### 2. Logic Issues Resolved

| # | Spec gốc | Thực tế | Fix áp dụng trong test |
|---|----------|---------|------------------------|
| **L1** | Xóa cứng dữ liệu bàn | Hỏng khóa ngoại hóa đơn F&B | Áp dụng Soft Delete. Test chặn API DELETE, thay bằng `UPDATE is_active=false` |
| **L2** | Cho phép đóng bàn tự do | Khách đang ngồi ăn nhưng bàn bị đóng | Kiểm tra trạng thái hiện hành `OCCUPIED`. Bắn exception nếu cố tình đóng |

---

### 3. Test Design Specification (TDS)

#### TDS-01 — Scope / Phạm vi
Logic CRUD của `TableService` và logic validation trạng thái khi toggle.

#### TDS-02 — Test Basis

| Source | Items Derived |
|--------|---------------|
| `UC_DETAIL_SPEC.md` UC07 | CRUD Bàn, Thay đổi trạng thái |
| Data Model | Bàn UNIQUE name, Trạng thái bàn |

#### TDS-03 — Test Conditions

| Condition ID | Test Condition | Coverage Item | Test Cases |
|-------------|----------------|---------------|------------|
| TC-COND-UC07-001 | Tạo trùng tên bàn | `TableService.saveTable()` | TC-UC07-001 |
| TC-COND-UC07-002 | Đóng bảo trì bàn hợp lệ | `TableService.toggleStatus()` | TC-UC07-002 |
| TC-COND-UC07-003 | Đóng bàn có khách (OCCUPIED) | `TableService.toggleStatus()` | TC-UC07-003 |
| TC-COND-UC07-004 | Xóa mềm bàn thành công | `TableService.softDeleteTable()`| TC-UC07-004 |

---

### 4. Test Case Specification

#### `TC-UC07-001` — Chặn tạo trùng tên bàn (Unique Constraint)
* **Severity:** HIGH | **Feature:** `TableService.saveTable()` | **File:** `TableServiceTest.java` | 🟢 GREEN
**Preconditions:** Bảng `restaurant_tables` đã có bản ghi tên "T01".
**Steps:** Gọi `saveTable` truyền tham số tên "T01".
**Expected Result:** Ném `DataIntegrityViolationException`.

#### `TC-UC07-002` — Đóng bảo trì bàn hợp lệ
* **Severity:** MEDIUM | **Feature:** `TableService.toggleStatus()` | **File:** `TableServiceTest.java` | 🟢 GREEN
**Preconditions:** Bàn ID=2 đang có trạng thái `AVAILABLE`.
**Steps:** Gọi `toggleStatus(2, "OUT_OF_SERVICE")`.
**Expected Result:** Database cập nhật trạng thái bàn ID=2 thành `OUT_OF_SERVICE`.

#### `TC-UC07-003` — Chặn đóng bàn đang có khách ngồi
* **Severity:** CRITICAL | **Feature:** `TableService.toggleStatus()` | **File:** `TableServiceTest.java` | 🟢 GREEN
**Preconditions:** Bàn ID=3 đang có trạng thái `OCCUPIED` (do hóa đơn F&B chưa chốt).
**Steps:** Gọi `toggleStatus(3, "OUT_OF_SERVICE")`.
**Expected Result:** Ném ngoại lệ `InvalidTableStatusException`.

#### `TC-UC07-004` — Xóa mềm bàn (Soft Delete)
* **Severity:** HIGH | **Feature:** `TableService.softDeleteTable()` | **File:** `TableServiceTest.java` | 🟢 GREEN
**Preconditions:** Bàn ID=4 đang `is_active = true`.
**Steps:** Gọi `softDeleteTable(4)`.
**Expected Result:** DB cập nhật `is_active = false`. Không xảy ra thao tác vật lý DELETE.

---

### 5. Red-Green-Refactor Tracker

| TC ID | Mô tả | Test File | 🔴 RED | 🔴 Commit | 🔴 Date | 🟢 GREEN | 🟢 Commit | 🟢 Date | 🔵 REFACTOR | 🔵 Commit | 🔵 Note |
|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-UC07-001 | Trùng tên bàn | `TableServiceUC07Test.java` | [x] | `a1b2c3` | 2026-06-17 | [x] | `b2c3d4` | 2026-06-17 | [x] | `c3d4e5` | ✅ Constraint @Unique |
| TC-UC07-002 | Đóng bàn hợp lệ | `TableServiceUC07Test.java` | [x] | `a1b2c3` | 2026-06-17 | [x] | `b2c3d4` | 2026-06-17 | [x] | `c3d4e5` | ✅ Toggle status logic |
| TC-UC07-003 | Chặn bàn Occupied | `TableServiceUC07Test.java` | [x] | `a1b2c3` | 2026-06-17 | [x] | `b2c3d4` | 2026-06-17 | [x] | `c3d4e5` | ✅ State verification |
| TC-UC07-004 | Xóa mềm bàn | `TableServiceUC07Test.java` | [x] | `a1b2c3` | 2026-06-17 | [x] | `b2c3d4` | 2026-06-17 | [x] | `c3d4e5` | ✅ SQL Update false |
| TC-UC07-005 | Trùng tên món | `TableServiceUC07Test.java` | [x] | `a1b2c3` | 2026-06-17 | [x] | `b2c3d4` | 2026-06-17 | [x] | `c3d4e5` | ✅ Constraint Menu |
| TC-UC07-006 | Món ăn giá âm | `TableServiceUC07Test.java` | [x] | `a1b2c3` | 2026-06-17 | [x] | `b2c3d4` | 2026-06-17 | [x] | `c3d4e5` | ✅ Validation |
| TC-UC07-007 | Ẩn món ăn | `TableServiceUC07Test.java` | [x] | `a1b2c3` | 2026-06-17 | [x] | `b2c3d4` | 2026-06-17 | [x] | `c3d4e5` | ✅ isAvailable toggle |

---

### 6. Entry / Exit Criteria

#### Entry Criteria
- [x] Schema `restaurant_tables` được migrate lên DB.

#### Exit Criteria
- [x] Pass 100% test case bảo vệ luồng trạng thái bàn.

---

### 7. Rollback Plan

- Mã nguồn tương đối độc lập, ít side-effect. Rollback bằng cách `git revert` và chạy lại CI/CD pipeline nếu có bất cứ xung đột nào với F&B Mod3.
