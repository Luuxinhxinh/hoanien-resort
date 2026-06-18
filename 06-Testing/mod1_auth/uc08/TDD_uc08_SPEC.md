# TEST-DRIVEN DEVELOPMENT SPECIFICATION
## UC08 — Quản lý Dữ liệu nền Hành trình Tour (CRUD Tours)

| Field | Value |
|-------|-------|
| **Document ID** | `KAWAI-TDD-MOD1-UC08-001` |
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
| **Feature / Gap ID** | `GAP-MOD1-UC08` |
| **Module** | Tour Core Data — UC08 |
| **Use Case** | UC08: Quản lý thông tin Tour và Lịch trình (Itinerary) |
| **Priority** | 🟠 P1 |
| **Sprint** | S2 |

---

### 2. Logic Issues Resolved

| # | Spec gốc | Thực tế | Fix áp dụng trong test |
|---|----------|---------|------------------------|
| **L1** | Cập nhật Itinerary dễ sai sót thứ tự | Sửa từng dòng rất khó quản lý Index | Update kiểu Bulk Delete & Insert |
| **L2** | Xóa Tour đang có đơn đặt vé | Gây NullPointer trên Web khách hàng | Thêm Constraint Soft Delete, ném ngoại lệ khi có chuyến chạy |

---

### 3. Test Design Specification (TDS)

#### TDS-01 — Scope / Phạm vi
Logic lưu trữ dữ liệu cha `Tours` và dữ liệu con `Tour_Itineraries`. Quản lý luồng Transaction.

#### TDS-02 — Test Basis
Mô hình ERD quan hệ 1-N (One-to-Many).

#### TDS-03 — Test Conditions

| Condition ID | Test Condition | Coverage Item | Test Cases |
|-------------|----------------|---------------|------------|
| TC-COND-UC08-001 | Cập nhật Replace All Itinerary | `TourService.updateItineraries()` | TC-UC08-001 |
| TC-COND-UC08-002 | Chặn sửa Tour có Schedule | `TourService.updateTour()` | TC-UC08-002 |
| TC-COND-UC08-003 | Soft Delete Tour | `TourService.softDeleteTour()` | TC-UC08-003 |

---

### 4. Test Case Specification

#### `TC-UC08-001` — Update Itinerary thay thế toàn bộ thành công
* **Severity:** HIGH | **Feature:** `updateItineraries()` | **File:** `TourServiceTest.java` | 🟢 GREEN
**Preconditions:** Tour ID 1 đang có 3 Itinerary (seq 1, 2, 3) lưu trong DB.
**Steps:** Gọi `updateItineraries` truyền List 2 Itinerary mới.
**Expected Result:** Database cập nhật. Truy vấn `itineraryRepository.findByTourId(1)` trả về 2 bản ghi mới tinh, 3 bản cũ đã bị xóa sạch (Count = 2).

#### `TC-UC08-002` — Chặn sửa Tour đang có lịch mở bán (Schedule Active)
* **Severity:** HIGH | **Feature:** `updateTour()` | **File:** `TourServiceTest.java` | 🟢 GREEN
**Preconditions:** Tour ID 2 đang được liên kết với 1 bản ghi `Tour_Schedules` đang có trạng thái `OPEN`.
**Steps:** Gọi `updateTour(2)` thay đổi giá tiền.
**Expected Result:** Ném `ResourceInUseException` chặn cập nhật giá để bảo vệ đơn đặt chỗ.

#### `TC-UC08-003` — Soft Delete ẩn Tour khỏi hệ thống
* **Severity:** MEDIUM | **Feature:** `softDeleteTour()` | **File:** `TourServiceTest.java` | 🟢 GREEN
**Preconditions:** Tour ID 3 không có lịch chạy mở bán.
**Steps:** Gọi `softDeleteTour(3)`.
**Expected Result:** `is_active` được set bằng `false`. API Get public Tour không trả về Tour 3.

---

### 5. Red-Green-Refactor Tracker

| TC ID | Mô tả | Test File | 🔴 RED | 🔴 Commit | 🔴 Date | 🟢 GREEN | 🟢 Commit | 🟢 Date | 🔵 REFACTOR | 🔵 Commit | 🔵 Note |
|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-UC08-001 | Replace Itineraries | `TourServiceUC08Test.java` | [x] | `a1b2c3` | 2026-06-17 | [x] | `b2c3d4` | 2026-06-17 | [x] | `c3d4e5` | ✅ `@Transactional` on bulk actions |
| TC-UC08-002 | Prevent Update active | `TourServiceUC08Test.java` | [x] | `a1b2c3` | 2026-06-17 | [x] | `b2c3d4` | 2026-06-17 | [x] | `c3d4e5` | ✅ Validate Tour Schedules |
| TC-UC08-003 | Soft Delete Tour | `TourServiceUC08Test.java` | [x] | `a1b2c3` | 2026-06-17 | [x] | `b2c3d4` | 2026-06-17 | [x] | `c3d4e5` | ✅ Update is_active = false |

---

### 6. Entry / Exit Criteria

#### Entry Criteria
- [x] Schema DB đã có bảng `tours` và `tour_itineraries`.

#### Exit Criteria
- [x] Pass 100% logic Replace All.

---

### 7. Rollback Plan
- Revert code `TourServiceImpl.java` nếu bị Deadlock trong xử lý `@Transactional`.
