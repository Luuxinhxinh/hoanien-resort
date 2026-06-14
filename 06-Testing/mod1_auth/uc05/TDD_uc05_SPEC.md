# TEST-DRIVEN DEVELOPMENT SPECIFICATION
## UC05 — Phân quyền RBAC (RoleService)

| Field | Value |
|-------|-------|
| **Document ID** | `KAWAI-TDD-MOD1-UC05-001` |
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
| **Feature / Gap ID** | `GAP-MOD1-UC05` |
| **Module** | Authorization & Access Control — UC05 |
| **Use Case** | UC05: Gán role, gỡ role, kiểm tra quyền RBAC |
| **Spec gốc** | `SRS_Document_SWP391_G2.md` |
| **Priority** | 🔴 P0 |
| **Sprint** | S1 (2026-06-09 → 2026-06-23) |
| **Milestone** | M3 Alpha — 2026-07-11 |
| **Data Classification** | Internal |
| **Upstream Dependencies** | UC01 (Auth), UC03 (Employee) |
| **Downstream Consumers** | Tất cả UC khác (authorization check) |

---

### 2. Logic Issues Resolved

| # | Spec gốc | Thực tế | Fix áp dụng trong test |
|---|----------|---------|------------------------|
| **L1** | Chưa có cơ chế bảo vệ ADMIN cuối cùng | Thêm check: không cho gỡ role ADMIN nếu chỉ còn 1 | Test remove last admin → 409 |
| **L2** | Permission check chưa rõ logic hierarchy | Implement role-based comparison | Test hasPermission với các role khác nhau |

---

### 3. Test Design Specification (TDS)

#### TDS-01 — Scope / Phạm vi
Logic `RoleService.assignRole()`, `removeRole()`, `hasPermission()`.

#### TDS-02 — Test Basis

| Source | Items Derived |
|--------|---------------|
| `SRS.md` UC05 | Gán role, gỡ role, check permission |
| BR-RBAC-01 | ADMIN-only cho assign/remove |
| BR-RBAC-02 | Bảo vệ ADMIN cuối cùng |

#### TDS-03 — Test Conditions

| Condition ID | Test Condition | Coverage Item | Test Cases |
|-------------|----------------|---------------|------------|
| TC-COND-UC05-001 | Assign role thành công | `RoleService.assignRole()` | TC-UC05-001 |
| TC-COND-UC05-002 | Remove role thành công | `RoleService.removeRole()` | TC-UC05-002 |
| TC-COND-UC05-003 | Check permission — có quyền | `RoleService.hasPermission()` | TC-UC05-003 |
| TC-COND-UC05-004 | Check permission — không có quyền | `RoleService.hasPermission()` | TC-UC05-004 |
| TC-COND-UC05-005 | Role name không hợp lệ → 400 | `RoleService.assignRole()` | TC-UC05-005 |
| TC-COND-UC05-006 | Gỡ ADMIN cuối cùng → 409 | `RoleService.assignRole()` | TC-UC05-006 |

---

### 4. Test Case Specification

#### `TC-UC05-001` — Assign role thành công
* **Severity:** CRITICAL | **Feature:** `RoleService.assignRole()` | **File:** `RoleServiceUC05Test.java` | 🟢 GREEN
**Preconditions:** User ID=5 tồn tại, role hiện tại = CUSTOMER, ADMIN đã login.
**Steps:** Gọi `assignRole(5, "RECEPTIONIST")` → Assert user.role = "RECEPTIONIST". Kiểm tra audit log ghi nhận thay đổi role.

#### `TC-UC05-002` — Remove role thành công (reset to CUSTOMER)
* **Severity:** HIGH | **Feature:** `RoleService.removeRole()` | **File:** `RoleServiceUC05Test.java` | 🟢 GREEN
**Preconditions:** User ID=5, role = RECEPTIONIST.
**Steps:** Gọi `removeRole(5)` → Assert user.role = "CUSTOMER" (reset về mặc định).

#### `TC-UC05-003` — Check permission — user có quyền
* **Severity:** CRITICAL | **Feature:** `RoleService.hasPermission()` | **File:** `RoleServiceUC05Test.java` | 🟢 GREEN
**Steps:** User role = ADMIN → Gọi `hasPermission(userId, "ADMIN")` → Assert true. User role = ADMIN → Gọi `hasPermission(userId, "RECEPTIONIST")` → Assert true (ADMIN > RECEPTIONIST).

#### `TC-UC05-004` — Check permission — user không có quyền
* **Severity:** HIGH | **Feature:** `RoleService.hasPermission()` | **File:** `RoleServiceUC05Test.java` | 🟢 GREEN
**Steps:** User role = CUSTOMER → Gọi `hasPermission(userId, "ADMIN")` → Assert false. User role = RECEPTIONIST → Gọi `hasPermission(userId, "ADMIN")` → Assert false.

#### `TC-UC05-005` — Role name không hợp lệ → 400
* **Severity:** MEDIUM | **Feature:** `RoleService.assignRole()` | **File:** `RoleServiceUC05Test.java` | 🟢 GREEN
**Steps:** Gọi `assignRole(5, "SUPERADMIN")` → throws InvalidRoleException (400).

#### `TC-UC05-006` — Gỡ ADMIN cuối cùng → 409
* **Severity:** CRITICAL | **Feature:** `RoleService.assignRole()` | **File:** `RoleServiceUC05Test.java` | 🟢 GREEN
**Preconditions:** Chỉ còn 1 user có role = ADMIN (ID=1).
**Steps:** Gọi `assignRole(1, "CUSTOMER")` → throws LastAdminException (409) với message "Cannot remove last admin".

---

### 5. Red-Green-Refactor Tracker

| TC ID | Mô tả | Test File | 🔴 RED | 🔴 Commit | 🔴 Date | 🟢 GREEN | 🟢 Commit | 🟢 Date | 🔵 REFACTOR | 🔵 Commit | 🔵 Note |
|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-UC05-001 | Assign role | `RoleServiceUC05Test.java` | [x] | `jj00kk1` | 2026-06-10 | [x] | `kk11ll2` | 2026-06-10 | [x] | `ll22mm3` | ✅ Extract role validator |
| TC-UC05-002 | Remove role | `RoleServiceUC05Test.java` | [x] | `jj00kk1` | 2026-06-10 | [x] | `kk11ll2` | 2026-06-10 | [x] | `ll22mm3` | ✅ Default role constant |
| TC-UC05-003 | Has permission (true) | `RoleServiceUC05Test.java` | [x] | `jj00kk1` | 2026-06-10 | [x] | `kk11ll2` | 2026-06-10 | [x] | `ll22mm3` | ✅ Role hierarchy comparator |
| TC-UC05-004 | Has permission (false) | `RoleServiceUC05Test.java` | [x] | `jj00kk1` | 2026-06-10 | [x] | `kk11ll2` | 2026-06-10 | [x] | `ll22mm3` | ✅ Negative test pattern |
| TC-UC05-005 | Invalid role | `RoleServiceUC05Test.java` | [x] | `jj00kk1` | 2026-06-10 | [x] | `kk11ll2` | 2026-06-10 | [x] | `ll22mm3` | ✅ Enum validation |
| TC-UC05-006 | Last admin protection | `RoleServiceUC05Test.java` | [x] | `jj00kk1` | 2026-06-10 | [x] | `kk11ll2` | 2026-06-10 | [x] | `ll22mm3` | ✅ Safety guard |

---

### 6. Entry / Exit Criteria

#### Entry Criteria
- [x] UC01 (Auth) đã hoạt động — JWT validation
- [x] UC03 (Employee) đã hoạt động
- [x] Role enum đã định nghĩa

#### Exit Criteria
- [x] Unit tests pass 100%
- [x] Role assignment/removal verified
- [x] Permission check logic verified
- [x] Last admin protection verified

---

### 7. Rollback Plan

`git checkout -- src/main/java/com/kawai/services/impl/RoleServiceImpl.java`
