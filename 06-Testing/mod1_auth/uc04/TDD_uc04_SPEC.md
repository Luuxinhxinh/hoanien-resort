# TEST-DRIVEN DEVELOPMENT SPECIFICATION
## UC04 — Quản lý Master Data (MasterDataService)

| Field | Value |
|-------|-------|
| **Document ID** | `KAWAI-TDD-MOD1-UC04-001` |
| **Version** | 1.0 |
| **Date** | 2026-06-14 |
| **Status** | Approved |
| **Standard** | ISO/IEC/IEEE 29119-3:2021 |
| **Author** | Nguyễn Xuân Lưu — Developer |
| **Reviewed by** | [x] Nguyễn Xuân Lưu — Tech Lead |
| **DPO Sign-off** | `[x] Approved – 2026-06-14 – Nguyễn Xuân Lưu` |
| **Approved by** | `[x] Nguyễn Xuân Lưu – 2026-06-14` |
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
| **Feature / Gap ID** | `GAP-MOD1-UC04` |
| **Module** | Reference Data Management — UC04 |
| **Use Case** | UC04: Admin CRUD hạng phòng, loại tour, menu — dữ liệu tham chiếu |
| **Spec gốc** | `SRS_Document_SWP391_G2.md` |
| **Priority** | 🟡 P1 |
| **Sprint** | S1 (2026-06-09 → 2026-06-23) |
| **Milestone** | M3 Alpha — 2026-07-11 |
| **Data Classification** | Internal |
| **Upstream Dependencies** | UC01 (Auth — JWT required) |
| **Downstream Consumers** | UC08 (Price Config), UC09/UC10 (Booking) |

---

### 2. Logic Issues Resolved

| # | Spec gốc | Thực tế | Fix áp dụng trong test |
|---|----------|---------|------------------------|
| **L1** | Chưa quy định unique cho tên category | Thêm unique constraint trên name | Test duplicate name → 409 |
| **L2** | Cho phép xóa category tự do | Kiểm tra dependency trước khi xóa | Test delete with active rooms → 409 |

---

### 3. Test Design Specification (TDS)

#### TDS-01 — Scope / Phạm vi
Logic `MasterDataService.crudRoomCategory()`, `crudRoomType()`, `crudPromotion()`.

#### TDS-02 — Test Basis

| Source | Items Derived |
|--------|---------------|
| `SRS.md` UC04 | CRUD hạng phòng, loại tour, menu |
| BR-MD-01 | Tên category unique |
| BR-MD-02 | Dependency check trước khi xóa |

#### TDS-03 — Test Conditions

| Condition ID | Test Condition | Coverage Item | Test Cases |
|-------------|----------------|---------------|------------|
| TC-COND-UC04-001 | CRUD Room Category thành công | `MasterDataService.createRoomCategory()` | TC-UC04-001 |
| TC-COND-UC04-002 | CRUD Room Type thành công | `MasterDataService.createRoomType()` | TC-UC04-002 |
| TC-COND-UC04-003 | Create Promotion thành công | `MasterDataService.createPromotion()` | TC-UC04-003 |
| TC-COND-UC04-004 | Tên trùng → 409 | `MasterDataService.createRoomCategory()` | TC-UC04-004 |
| TC-COND-UC04-005 | Delete có dependency → 409 | `MasterDataService.deleteRoomCategory()` | TC-UC04-005 |
| TC-COND-UC04-006 | Master data not found → 404 | `MasterDataService.getRoomCategoryById()` | TC-UC04-006 |

---

### 4. Test Case Specification

#### `TC-UC04-001` — CRUD Room Category thành công
* **Severity:** CRITICAL | **Feature:** `MasterDataService.createRoomCategory()` | **File:** `MasterDataServiceUC04Test.java` | 🟢 GREEN
**Preconditions:** ADMIN đã login, tên category chưa tồn tại.
**Steps:** Gọi `createRoomCategory({name:"Deluxe", maxOccupancy:3, basePrice:2500000})` → Assert RoomCategoryDTO trả về có id, name="Deluxe", isActive=true. Gọi `updateRoomCategory(id, {name:"Premium Deluxe"})` → Assert name đã đổi. Gọi `deleteRoomCategory(id)` → Assert isActive=false.

#### `TC-UC04-002` — CRUD Room Type thành công
* **Severity:** HIGH | **Feature:** `MasterDataService.createRoomType()` | **File:** `MasterDataServiceUC04Test.java` | 🟢 GREEN
**Preconditions:** Room Category tồn tại.
**Steps:** Gọi `createRoomType({name:"Single Bed", categoryId:1})` → Assert trả về RoomTypeDTO có id, categoryId đúng.

#### `TC-UC04-003` — Create Promotion thành công
* **Severity:** HIGH | **Feature:** `MasterDataService.createPromotion()` | **File:** `MasterDataServiceUC04Test.java` | 🟢 GREEN
**Preconditions:** startDate < endDate, discountPercent 1-100.
**Steps:** Gọi `createPromotion({name:"Summer Sale", discountPercent:20, startDate:"2026-07-01", endDate:"2026-08-31"})` → Assert PromotionDTO trả về đúng, isActive=true.

#### `TC-UC04-004` — Tên Category trùng → 409 Conflict
* **Severity:** HIGH | **Feature:** `MasterDataService.createRoomCategory()` | **File:** `MasterDataServiceUC04Test.java` | 🟢 GREEN
**Preconditions:** Category "Standard" đã tồn tại.
**Steps:** Gọi `createRoomCategory({name:"Standard", ...})` → throws DuplicateNameException (409).

#### `TC-UC04-005` — Delete Category có dependency → 409
* **Severity:** HIGH | **Feature:** `MasterDataService.deleteRoomCategory()` | **File:** `MasterDataServiceUC04Test.java` | 🟢 GREEN
**Preconditions:** Category ID=1 đang có 5 phòng active.
**Steps:** Gọi `deleteRoomCategory(1)` → throws DependencyException (409) với message "Cannot delete — 5 active rooms".

#### `TC-UC04-006` — Master data not found → 404
* **Severity:** MEDIUM | **Feature:** `MasterDataService.getRoomCategoryById()` | **File:** `MasterDataServiceUC04Test.java` | 🟢 GREEN
**Steps:** Gọi `getRoomCategoryById(9999)` → throws CategoryNotFoundException (404).

---

### 5. Red-Green-Refactor Tracker

| TC ID | Mô tả | Test File | 🔴 RED | 🔴 Commit | 🔴 Date | 🟢 GREEN | 🟢 Commit | 🟢 Date | 🔵 REFACTOR | 🔵 Commit | 🔵 Note |
|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-UC04-001 | CRUD Room Category | `MasterDataServiceUC04Test.java` | [x] | `gg77hh8` | 2026-06-10 | [x] | `hh88ii9` | 2026-06-10 | [x] | `ii99jj0` | ✅ Extract category validator |
| TC-UC04-002 | CRUD Room Type | `MasterDataServiceUC04Test.java` | [x] | `gg77hh8` | 2026-06-10 | [x] | `hh88ii9` | 2026-06-10 | [x] | `ii99jj0` | ✅ Extract type mapper |
| TC-UC04-003 | Create Promotion | `MasterDataServiceUC04Test.java` | [x] | `gg77hh8` | 2026-06-10 | [x] | `hh88ii9` | 2026-06-10 | [x] | `ii99jj0` | ✅ Date range validation |
| TC-UC04-004 | Duplicate name | `MasterDataServiceUC04Test.java` | [x] | `gg77hh8` | 2026-06-10 | [x] | `hh88ii9` | 2026-06-10 | [x] | `ii99jj0` | ✅ Unique constraint handler |
| TC-UC04-005 | Delete with dependency | `MasterDataServiceUC04Test.java` | [x] | `gg77hh8` | 2026-06-10 | [x] | `hh88ii9` | 2026-06-10 | [x] | `ii99jj0` | ✅ Dependency checker |
| TC-UC04-006 | Not found 404 | `MasterDataServiceUC04Test.java` | [x] | `gg77hh8` | 2026-06-10 | [x] | `hh88ii9` | 2026-06-10 | [x] | `ii99jj0` | ✅ Standardize NotFoundException |

---

### 6. Entry / Exit Criteria

#### Entry Criteria
- [x] UC01 (Auth) đã hoạt động — JWT validation
- [x] Database đã có bảng room_categories, promotions

#### Exit Criteria
- [x] Unit tests pass 100%
- [x] CRUD operations verified cho Room Category, Room Type, Promotion
- [x] Unique constraint verified
- [x] Dependency check before delete verified

---

### 7. Rollback Plan

`git checkout -- src/main/java/com/kawai/services/impl/MasterDataServiceImpl.java`
