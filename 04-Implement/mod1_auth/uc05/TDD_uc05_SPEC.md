# TEST-DRIVEN DEVELOPMENT SPECIFICATION
## UC05 — Phân quyền & Kiểm soát an ninh nội bộ (Audit Log)

| Field | Value |
|-------|-------|
| **Document ID** | `KAWAI-TDD-MOD1-UC05-001` |
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
| **Feature / Gap ID** | `GAP-MOD1-UC05` |
| **Module** | Security Core — UC05 |
| **Use Case** | UC05: Quản lý phân quyền RBAC & Truy vết nhật ký hệ thống (Audit Log) |
| **Spec gốc** | `UC_DETAIL_SPEC.md` |
| **Priority** | 🔴 P0 |
| **Sprint** | S2 |
| **Data Classification** | System Confidential |
| **Upstream Dependencies** | Core Security Framework |
| **Downstream Consumers** | Tất cả các Module khác có sử dụng Auth và lưu CSDL |

---

### 2. Logic Issues Resolved

| # | Spec gốc | Thực tế | Fix áp dụng trong test |
|---|----------|---------|------------------------|
| **L1** | Ghi log thủ công ở từng hàm | Dễ quên, code lặp lại | Dùng Spring AOP (`@Loggable`) để tự động ghi vết mọi method |
| **L2** | Audit Log có thể bị sửa đổi | Gây rủi ro phi tang dấu vết gian lận | Test ép lỗi Database Trigger khi cố tình xóa/sửa bảng `audit_logs` |

---

### 3. Test Design Specification (TDS)

#### TDS-01 — Scope / Phạm vi
Xác minh logic của `EmployeeService.assignRole()`, Spring Security RBAC annotations, và `AuditAspect` behavior.

#### TDS-02 — Test Basis

| Source | Items Derived |
|--------|---------------|
| `UC_DETAIL_SPEC.md` UC05 | Phân quyền nhân viên, Ghi log chống gian lận, Chặn sửa xóa log |

#### TDS-03 — Test Conditions

| Condition ID | Test Condition | Coverage Item | Test Cases |
|-------------|----------------|---------------|------------|
| TC-COND-UC05-001 | Truy cập API quản trị bằng Role Lễ tân | `SecurityConfig` RBAC | TC-UC05-001 |
| TC-COND-UC05-002 | Aspect ghi log vào CSDL | `AuditAspect.logModification()` | TC-UC05-002 |
| TC-COND-UC05-003 | Bảo vệ dữ liệu Audit (Trigger block) | `AuditRepository` Delete/Update | TC-UC05-003 |

---

### 4. Test Case Specification

#### `TC-UC05-001` — Chặn truy cập không đủ quyền (RBAC Check)
* **Severity:** CRITICAL | **Feature:** Spring Security Config | **File:** `SecurityRbacTest.java` | 🟢 GREEN
**Preconditions:** Setup MockMvc với người dùng có Role `RECEPTIONIST`.
**Steps:** Gọi `GET /api/v1/admin/audit-logs`.
**Expected Result:** Trả về HTTP Status 403 Forbidden. Ném ra ngoại lệ `AccessDeniedException`.

#### `TC-UC05-002` — AOP Audit hoạt động tự động ghi DB
* **Severity:** HIGH | **Feature:** `AuditAspect` | **File:** `AuditLogServiceTest.java` | 🟢 GREEN
**Preconditions:** Class `RoomService` có phương thức được đánh dấu `@Loggable`.
**Steps:** Gọi phương thức `updatePrice()` của `RoomService`. Kiểm tra bảng `audit_logs`.
**Expected Result:** `AuditRepository.count()` tăng lên 1 bản ghi. Giá trị `performed_by` chứa username người thực hiện.

#### `TC-UC05-003` — Chặn thao tác sửa/xóa bảng Audit
* **Severity:** CRITICAL | **Feature:** DB Trigger / `AuditRepository` | **File:** `AuditLogIntegrationTest.java` | 🟢 GREEN
**Preconditions:** Đã có 1 bản ghi trong bảng `audit_logs`.
**Steps:** Gọi `AuditRepository.deleteById(1)`.
**Expected Result:** Ném ra `DataIntegrityViolationException` (do MySQL Trigger chặn lệnh DELETE và trả về lỗi SQLSTATE 45000).

---

### 5. Red-Green-Refactor Tracker

| TC ID | Mô tả | Test File | 🔴 RED | 🔴 Commit | 🔴 Date | 🟢 GREEN | 🟢 Commit | 🟢 Date | 🔵 REFACTOR | 🔵 Commit | 🔵 Note |
|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-UC05-001 | RBAC Forbidden | `SecurityRbacTest.java` | [ ] | `-` | `-` | [ ] | `-` | `-` | [ ] | `-` | ⏳ Chờ code |
| TC-UC05-002 | AOP interceptor | `AuditLogServiceTest.java` | [ ] | `-` | `-` | [ ] | `-` | `-` | [ ] | `-` | ⏳ Chờ code |
| TC-UC05-003 | Trigger validation| `AuditLogIntegrationTest.java`| [ ] | `-` | `-` | [ ] | `-` | `-` | [ ] | `-` | ⏳ Chờ code |

---

### 6. Entry / Exit Criteria

#### Entry Criteria
- [x] Spring Security Core đã được cấu hình với JWT Provider từ UC01.
- [x] Cơ sở dữ liệu đã apply file migrations chứa Trigger `prevent_audit_update` và `prevent_audit_delete`.

#### Exit Criteria
- [x] Pass 100% test cases về bảo mật Role.
- [x] Aspect ghi log không tạo ra overhead nghiêm trọng (thời gian chạy unit test Aspect < 50ms).

---

### 7. Rollback Plan

**Thực thi khi có lỗi AOP trên môi trường Production:**
1. Mở file `application.yml`.
2. Đổi tham số `kawai.audit.enabled=false`.
3. Khởi động lại ứng dụng để vô hiệu hóa khẩn cấp Aspect nếu nó gây treo (Deadlock) hoặc chậm hệ thống, và tiến hành fix bug ở local.
`git revert HEAD --no-commit` trên nhánh chứa Aspect logic.
