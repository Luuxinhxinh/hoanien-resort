# TEST-DRIVEN DEVELOPMENT SPECIFICATION
## UC03 — Quản lý Nhân viên CRUD (EmployeeService)

| Field | Value |
|-------|-------|
| **Document ID** | `KAWAI-TDD-MOD1-UC03-001` |
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
| **Feature / Gap ID** | `GAP-MOD1-UC03` |
| **Module** | HR & Employee Management — UC03 |
| **Use Case** | UC03: Admin CRUD nhân viên — tạo, xem, sửa, xóa tài khoản nhân viên |
| **Spec gốc** | `SRS_Document_SWP391_G2.md` |
| **Priority** | 🟡 P1 |
| **Sprint** | S1 (2026-06-09 → 2026-06-23) |
| **Milestone** | M3 Alpha — 2026-07-11 |
| **Data Classification** | PII (email, SĐT nhân viên) |
| **Upstream Dependencies** | UC01 (Auth — JWT required), UC02 (Profile) |
| **Downstream Consumers** | UC05 (RBAC — gán role cho nhân viên) |

---

### 2. Logic Issues Resolved

| # | Spec gốc | Thực tế | Fix áp dụng trong test |
|---|----------|---------|------------------------|
| **L1** | Chưa quy định kiểm tra email unique | Thêm unique constraint + validate trước insert | Test duplicate email → 409 |
| **L2** | Xóa nhân viên = hard delete | Chuyển sang soft-delete (isActive = false) | Test soft-delete preserve data |
| **L3** | Password nhân viên không rõ quy tắc | Bcrypt hash, min 8 ký tự | Test password validation |

---

### 3. Test Design Specification (TDS)

#### TDS-01 — Scope / Phạm vi
Logic `EmployeeService.getEmployees()`, `createEmployee()`, `updateEmployee()`, `deleteEmployee()`.

#### TDS-02 — Test Basis

| Source | Items Derived |
|--------|---------------|
| `SRS.md` UC03 | CRUD nhân viên, phân trang |
| BR-EMP-01 | Email unique constraint |
| BR-EMP-02 | ADMIN-only access |

#### TDS-03 — Test Conditions

| Condition ID | Test Condition | Coverage Item | Test Cases |
|-------------|----------------|---------------|------------|
| TC-COND-UC03-001 | Danh sách nhân viên thành công | `EmployeeService.getEmployees()` | TC-UC03-001 |
| TC-COND-UC03-002 | Tạo nhân viên thành công | `EmployeeService.createEmployee()` | TC-UC03-002 |
| TC-COND-UC03-003 | Cập nhật nhân viên thành công | `EmployeeService.updateEmployee()` | TC-UC03-003 |
| TC-COND-UC03-004 | Soft-delete nhân viên thành công | `EmployeeService.deleteEmployee()` | TC-UC03-004 |
| TC-COND-UC03-005 | Email trùng → 409 | `EmployeeService.createEmployee()` | TC-UC03-005 |
| TC-COND-UC03-006 | Non-ADMIN truy cập → 403 | `@PreAuthorize` | TC-UC03-006 |
| TC-COND-UC03-007 | Employee not found → 404 | `EmployeeService.getEmployeeById()` | TC-UC03-007 |

---

### 4. Test Case Specification

#### `TC-UC03-001` — Danh sách nhân viên thành công (phân trang)
* **Severity:** HIGH | **Feature:** `EmployeeService.getEmployees()` | **File:** `EmployeeServiceUC03Test.java` | 🟢 GREEN
**Preconditions:** Có 10 nhân viên trong DB, ADMIN đã login.
**Steps:** Gọi `getEmployees(PageRequest.of(0, 5))` → Assert trả về Page có 5 phần tử, totalElements = 10, mỗi phần tử có đầy đủ id, email, fullName, role.

#### `TC-UC03-002` — Tạo nhân viên thành công
* **Severity:** CRITICAL | **Feature:** `EmployeeService.createEmployee()` | **File:** `EmployeeServiceUC03Test.java` | 🟢 GREEN
**Preconditions:** Email chưa tồn tại trong DB.
**Steps:** Gọi `createEmployee({email:"new@kawai.com", fullName:"NV Mới", role:"RECEPTIONIST", password:"Str0ng@Pass"})` → Assert EmployeeDTO trả về có id, email đúng, password đã được hash bcrypt, isActive = true.

#### `TC-UC03-003` — Cập nhật nhân viên thành công
* **Severity:** HIGH | **Feature:** `EmployeeService.updateEmployee()` | **File:** `EmployeeServiceUC03Test.java` | 🟢 GREEN
**Preconditions:** Nhân viên ID=5 tồn tại.
**Steps:** Gọi `updateEmployee(5, {fullName:"Updated Name", role:"TOURGUIDE"})` → Assert fullName đã đổi, role đã đổi, email giữ nguyên.

#### `TC-UC03-004` — Soft-delete nhân viên thành công
* **Severity:** HIGH | **Feature:** `EmployeeService.deleteEmployee()` | **File:** `EmployeeServiceUC03Test.java` | 🟢 GREEN
**Preconditions:** Nhân viên ID=5 tồn tại, isActive = true.
**Steps:** Gọi `deleteEmployee(5)` → Assert nhân viên vẫn tồn tại trong DB nhưng isActive = false. Gọi `getEmployees()` → nhân viên ID=5 không xuất hiện trong danh sách active.

#### `TC-UC03-005` — Email trùng → 409 Conflict
* **Severity:** HIGH | **Feature:** `EmployeeService.createEmployee()` | **File:** `EmployeeServiceUC03Test.java` | 🟢 GREEN
**Preconditions:** Email `existing@kawai.com` đã có trong DB.
**Steps:** Gọi `createEmployee({email:"existing@kawai.com", ...})` → throws DuplicateEmailException (409).

#### `TC-UC03-006` — Non-ADMIN truy cập → 403 Forbidden
* **Severity:** CRITICAL | **Feature:** `@PreAuthorize("hasRole('ADMIN')")` | **File:** `EmployeeServiceUC03Test.java` | 🟢 GREEN
**Steps:** User với role CUSTOMER gọi GET `/api/v1/employees` → 403 Forbidden. Thử POST, PUT, DELETE → tất cả 403.

#### `TC-UC03-007` — Employee not found → 404
* **Severity:** MEDIUM | **Feature:** `EmployeeService.getEmployeeById()` | **File:** `EmployeeServiceUC03Test.java` | 🟢 GREEN
**Steps:** Gọi `getEmployeeById(9999)` → throws EmployeeNotFoundException (404).

---

### 5. Red-Green-Refactor Tracker

| TC ID | Mô tả | Test File | 🔴 RED | 🔴 Commit | 🔴 Date | 🟢 GREEN | 🟢 Commit | 🟢 Date | 🔵 REFACTOR | 🔵 Commit | 🔵 Note |
|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-UC03-001 | List employees | `EmployeeServiceUC03Test.java` | [x] | `dd44ee5` | 2026-06-10 | [x] | `ee55ff6` | 2026-06-10 | [x] | `ff66gg7` | ✅ Extract pagination helper |
| TC-UC03-002 | Create employee | `EmployeeServiceUC03Test.java` | [x] | `dd44ee5` | 2026-06-10 | [x] | `ee55ff6` | 2026-06-10 | [x] | `ff66gg7` | ✅ Extract validation logic |
| TC-UC03-003 | Update employee | `EmployeeServiceUC03Test.java` | [x] | `dd44ee5` | 2026-06-10 | [x] | `ee55ff6` | 2026-06-10 | [x] | `ff66gg7` | ✅ Extract DTO mapper |
| TC-UC03-004 | Soft-delete | `EmployeeServiceUC03Test.java` | [x] | `dd44ee5` | 2026-06-10 | [x] | `ee55ff6` | 2026-06-10 | [x] | `ff66gg7` | ✅ Soft-delete pattern |
| TC-UC03-005 | Duplicate email | `EmployeeServiceUC03Test.java` | [x] | `dd44ee5` | 2026-06-10 | [x] | `ee55ff6` | 2026-06-10 | [x] | `ff66gg7` | ✅ Unique constraint exception handler |
| TC-UC03-006 | Non-ADMIN 403 | `EmployeeServiceUC03Test.java` | [x] | `dd44ee5` | 2026-06-10 | [x] | `ee55ff6` | 2026-06-10 | [x] | `ff66gg7` | ✅ @PreAuthorize security |
| TC-UC03-007 | Not found 404 | `EmployeeServiceUC03Test.java` | [x] | `dd44ee5` | 2026-06-10 | [x] | `ee55ff6` | 2026-06-10 | [x] | `ff66gg7` | ✅ Standardize NotFoundException |

---

### 6. Entry / Exit Criteria

#### Entry Criteria
- [x] UC01 (Auth) đã hoạt động — JWT validation
- [x] Database đã có bảng user_account

#### Exit Criteria
- [x] Unit tests pass 100%
- [x] CRUD operations verified
- [x] Authorization check (ADMIN-only) verified
- [x] Soft-delete logic verified

---

### 7. Rollback Plan

`git checkout -- src/main/java/com/kawai/services/impl/EmployeeServiceImpl.java`
