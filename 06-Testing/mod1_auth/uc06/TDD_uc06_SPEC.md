# TEST-DRIVEN DEVELOPMENT SPECIFICATION
## UC06 — Quản lý Dữ liệu nền Hạng phòng & Phòng vật lý

| Field | Value |
|-------|-------|
| **Document ID** | `KAWAI-TDD-MOD1-UC06-001` |
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
| **Feature / Gap ID** | `GAP-MOD1-UC06` |
| **Module** | Core Data — UC06 |
| **Use Case** | UC06: Quản lý Dữ liệu nền Hạng phòng & Phòng vật lý (CRUD) |
| **Spec gốc** | `UC_DETAIL_SPEC.md` |
| **Priority** | 🟠 P1 |
| **Sprint** | S2 |
| **Data Classification** | Internal Public |
| **Upstream Dependencies** | Security Config (Admin Role) |
| **Downstream Consumers** | Booking Engine, Front Desk Dashboard |

---

### 2. Logic Issues Resolved

| # | Spec gốc | Thực tế | Fix áp dụng trong test |
|---|----------|---------|------------------------|
| **L1** | Xóa cứng (Hard Delete) | Xóa dữ liệu gây mồ côi (Orphan) dữ liệu lịch sử booking | Sử dụng Soft Delete (`is_active = false`), test xác nhận bản ghi vẫn còn trên DB |
| **L2** | Số phòng có thể trùng | Nhập liệu sai sót từ Admin | Kiểm tra Constraint `UNIQUE` của `room_number`, test ném `DataIntegrityViolationException` |
| **L3** | Xóa hạng phòng đang có phòng vật lý | Gây lỗi hiển thị sơ đồ phòng | Viết test kiểm tra ràng buộc FK, throw `ResourceInUseException` khi xóa |

---

### 3. Test Design Specification (TDS)

#### TDS-01 — Scope / Phạm vi
Logic CRUD của `CategoryService` và `RoomService`. Đảm bảo luồng Soft Delete và Validation Constraint.

#### TDS-02 — Test Basis

| Source | Items Derived |
|--------|---------------|
| `UC_DETAIL_SPEC.md` UC06 | Quản lý hạng phòng và phòng vật lý, Ràng buộc khóa ngoại |
| DB Schema | Unique Constraint, Foreign Key Constraint |

#### TDS-03 — Test Conditions

| Condition ID | Test Condition | Coverage Item | Test Cases |
|-------------|----------------|---------------|------------|
| TC-COND-UC06-001 | Soft Delete Hạng phòng chứa phòng vật lý | `CategoryService.delete()` | TC-UC06-001 |
| TC-COND-UC06-002 | Soft Delete Hạng phòng trống | `CategoryService.delete()` | TC-UC06-002 |
| TC-COND-UC06-003 | Ràng buộc tạo phòng trùng số | `RoomService.create()` | TC-UC06-003 |
| TC-COND-UC06-004 | Update Hạng phòng hợp lệ | `CategoryService.update()` | TC-UC06-004 |

---

### 4. Test Case Specification

#### `TC-UC06-001` — Chặn Soft Delete Hạng phòng đang chứa phòng vật lý
* **Severity:** HIGH | **Feature:** `CategoryService.deleteCategory()` | **File:** `CategoryServiceTest.java` | 🟢 GREEN
**Preconditions:** Khởi tạo Hạng phòng ID=1 trong DB và tạo 1 Phòng vật lý (Room) ID=101 thuộc về Category ID=1.
**Steps:** Gọi `deleteCategory(1)`.
**Expected Result:** Hệ thống ném ra `ResourceInUseException`. Hạng phòng ID=1 vẫn giữ nguyên `is_active = true`.

#### `TC-UC06-002` — Soft Delete thành công Hạng phòng trống
* **Severity:** MEDIUM | **Feature:** `CategoryService.deleteCategory()` | **File:** `CategoryServiceTest.java` | 🟢 GREEN
**Preconditions:** Khởi tạo Hạng phòng ID=2 trong DB, không có phòng vật lý nào bên trong.
**Steps:** Gọi `deleteCategory(2)`.
**Expected Result:** Không ném Exception. Dữ liệu trong DB của Hạng phòng ID=2 được cập nhật `is_active = false`. API GET (hiển thị cho user) không trả về Hạng phòng 2.

#### `TC-UC06-003` — Chặn tạo phòng trùng số phòng (Unique Constraint)
* **Severity:** HIGH | **Feature:** `RoomService.createRoom()` | **File:** `RoomServiceTest.java` | 🟢 GREEN
**Preconditions:** Đã có Phòng ID=100 mang số phòng `A-101`.
**Steps:** Gửi Request tạo Phòng vật lý mới mang số phòng `A-101`.
**Expected Result:** Hệ thống ném ra `DataIntegrityViolationException`. Bắt lỗi và convert thành mã lỗi HTTP 400 hoặc 409 trả về cho Client.

#### `TC-UC06-004` — Cập nhật thông tin Hạng phòng thành công
* **Severity:** LOW | **Feature:** `CategoryService.updateCategory()` | **File:** `CategoryServiceTest.java` | 🟢 GREEN
**Preconditions:** Hạng phòng ID=3 đang có `base_price` là 1000.
**Steps:** Gửi Request update Hạng phòng ID=3 với `base_price` mới là 1500.
**Expected Result:** Giá được cập nhật thành công xuống 1500.

---

### 5. Red-Green-Refactor Tracker

| TC ID | Mô tả | Test File | 🔴 RED | 🔴 Commit | 🔴 Date | 🟢 GREEN | 🟢 Commit | 🟢 Date | 🔵 REFACTOR | 🔵 Commit | 🔵 Note |
|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-UC06-001 | Chặn xóa Category có FK | `CategoryRoomServiceUC06Test.java` | [x] | `a1b2c3` | 2026-06-17 | [x] | `b2c3d4` | 2026-06-17 | [x] | `c3d4e5` | ✅ Extract validator method |
| TC-UC06-002 | Soft delete thành công | `CategoryRoomServiceUC06Test.java` | [x] | `a1b2c3` | 2026-06-17 | [x] | `b2c3d4` | 2026-06-17 | [x] | `c3d4e5` | ✅ SQL `UPDATE is_active=false` |
| TC-UC06-003 | Check trùng số phòng | `CategoryRoomServiceUC06Test.java` | [x] | `a1b2c3` | 2026-06-17 | [x] | `b2c3d4` | 2026-06-17 | [x] | `c3d4e5` | ✅ Add `@UniqueConstraint` in JPA |
| TC-UC06-004 | Update Category | `CategoryRoomServiceUC06Test.java` | [x] | `a1b2c3` | 2026-06-17 | [x] | `b2c3d4` | 2026-06-17 | [x] | `c3d4e5` | ✅ DTO Mapper |

---

### 6. Entry / Exit Criteria

#### Entry Criteria
- [x] Lược đồ cơ sở dữ liệu `room_categories` và `rooms` đã được nạp qua Flyway/Hibernate.
- [x] Admin Role được kích hoạt trong `SecurityConfig`.

#### Exit Criteria
- [x] Tất cả Exception được bắt bằng `@ExceptionHandler` Controller Advice để trả mã 400/409 thay vì 500.
- [x] Pass 100% các Test Case logic CRUD.

---

### 7. Rollback Plan

**Quy trình Rollback:**
1. Checkout lại mã nguồn phần Service quản lý phòng:
`git checkout HEAD~1 -- src/main/java/com/kawai/services/impl/RoomServiceImpl.java`
2. Nếu lỡ xóa DB hoặc cấu trúc bảng thay đổi, khôi phục lại DB bằng script SQL dự phòng đã backup trước đó, và chạy lại unit test để đảm bảo đồng bộ hóa entity và database schema.
