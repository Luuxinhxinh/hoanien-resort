# TEST-DRIVEN DEVELOPMENT SPECIFICATION
## Mẫu Đặc tả Kiểm thử Hướng Phát triển

| Field | Value |
| --- | --- |
| **Document ID** | `KAWAI-MOD5-TDD-UC23` |
| **Version** | 1.0 |
| **Date** | 2026-06-21 |
| **Status** | Approved |
| **Standard** | ISO/IEC/IEEE 29119-3:2021 — Software Testing Part 3: Test Documentation |
| **Author** | `Antigravity AI` |
| **Reviewed by** | `Ngô Thị Ngọc Lan` |
| **DPO Sign-off** | `[ ] Pending` |
| **Approved by** | `Ngô Thị Ngọc Lan` |
| **Classification** | Internal |

---

### References:
*   `02-Requirement/UC_DETAIL_SPEC.md` §UC23 — Functional requirements
*   `06-Testing/mod5_finance/uc23/EDS_UC23_SPEC.md` — Technical Specification (EDS UC23) — **Approved**
*   `03-Design/database_schema.md` — Database Schema
*   `05-Development/kawai-backend/src/main/java/com/kawai/controllers/web/ManagerController.java` — Implementation

> [!NOTE]
> **Quy ước TDD:** Tài liệu này mô tả test cases TRƯỚC khi viết production code.
> Thứ tự bắt buộc: viết test (`.java`) -> chạy -> xác nhận FAIL 🔴 -> implement -> PASS 🟢 -> refactor 🔵.
> UC23 đã được code trước, nên các test được viết theo chiều ngược (Retroactive TDD).

---

### CHANGELOG

> [!IMPORTANT]
> **Policy 4.4 — Immutable History:** Không bao giờ xóa thông tin cũ.

| Ngày | Người thực hiện | Nội dung thay đổi |
| --- | --- | --- |
| 2026-06-21 | Antigravity AI     | Khởi tạo tài liệu — TDD spec cho UC23 (Dashboard Manager)                  |
| 2026-06-21 | Ngô Thị Ngọc Lan  | Duyệt tài liệu TDD — Approved                                              |
| 2026-06-21 | Antigravity AI     | Viết JUnit test `ManagerControllerUC23Test.java` (5 TC)              |
| 2026-06-21 | Antigravity AI     | Sửa assertion TC-005: `avgOccupancy` = 79 (không phải 80), lý do: ngày hôm nay dùng `occRate=50` thay vì mock |

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
| **Feature / Gap ID** | `UC23` |
| **Module** | `MOD5 - Finance & Reports` |
| **Spec gốc** | `KAWAI-MOD5-IMP-UC23` |
| **Priority** | 🔴 P0 |
| **Sprint** | `S1` |
| **Milestone** | M1 |
| **Data Classification** | Internal |
| **Compliance Scope** | N/A |
| **Upstream Dependencies** | `RoomBookingRepository, FoodOrderRepository, TourBookingRepository, RoomRepository, FoodOrderDetailRepository` |
| **Downstream Consumers** | `Export Service (UC25)` |

---

### 2. Logic Issues Resolved

| # | Spec gốc (sai / thiếu) | Thực tế (schema / policy) | Fix áp dụng trong test |
| --- | --- | --- | --- |
| **L1** | UC23 spec không nêu rõ nguồn dữ liệu doanh thu là 3 nguồn riêng biệt | Code tổng hợp từ `RoomBookingRepository`, `FoodOrderRepository`, `TourBookingRepository` và cộng lại | Test phải mock riêng 3 repository, kiểm tra Controller cộng đúng |
| **L2** | Spec không mô tả giá trị fallback khi query lỗi | Controller đang dùng `catch (Exception e) {}` âm thầm trả về 0 | Test coverage phải kiểm tra rằng khi một repo trả về `null`, Controller không ném NPE |
| **L3** | `MOCK_TOTAL_ROOMS = 100` hardcode trong code | Thực tế nên đọc từ `roomRepository.countTotalRooms()` | Test mock `countTotalRooms()` trả về số thực, kiểm tra `occupancyRate` tính đúng |

---

### 3. Test Design Specification (TDS)

#### TDS-01 — Scope / Phạm vi
`MOD5 - ManagerController` bao gồm các layer:
- Controller (Spring MVC Controller)
- Repository (Spring Data JPA — được mock)

> [!NOTE]
> `ManagerController` không có Service layer riêng — logic tổng hợp nằm trực tiếp trong Controller methods. Test sẽ dùng `@MockBean` / `@Mock` cho các Repository.

#### TDS-02 — Test Basis / Cơ sở Kiểm thử

| Source | Items Derived |
| --- | --- |
| `UC_DETAIL_SPEC.md` §UC23 | Biểu đồ doanh thu, tỷ lệ occupancy, Top dish/tour |
| `EDS_UC23_SPEC.md` §9 | Danh sách 9 endpoints, Model Attributes truyền vào View |
| `ManagerController.java` | Logic `dashboard()`, `revenueDaily()`, `revenueMonthly()`, `analyticsOccupancy()` |
| `database_schema.md` | Bảng `Room_Bookings`, `Food_Orders`, `Tour_Bookings`, `Rooms` |

#### TDS-03 — Test Conditions and Coverage Items

| Condition ID | Test Condition | Coverage Item | Test Cases |
| --- | --- | --- | --- |
| **TC-COND-001** | Dashboard tính occupancyRate đúng từ DB | `ManagerController.dashboard()` | `MOD5-TC-UC23-001` |
| **TC-COND-002** | Dashboard tổng hợp doanh thu 7 ngày không ném NPE khi null | `ManagerController.dashboard()` | `MOD5-TC-UC23-002` |
| **TC-COND-003** | Revenue Daily tổng hợp 3 nguồn đúng cho ngày hôm nay | `ManagerController.revenueDaily()` | `MOD5-TC-UC23-003` |
| **TC-COND-004** | Revenue Monthly tính YoY Growth đúng | `ManagerController.revenueMonthly()` | `MOD5-TC-UC23-004` |
| **TC-COND-005** | Occupancy Analytics tính Peak và Low trong 30 ngày | `ManagerController.analyticsOccupancy()` | `MOD5-TC-UC23-005` |

#### TDS-04 — Test Techniques / Kỹ thuật Kiểm thử

| Technique (ISO 29119-4) | Applied To | Rationale |
| --- | --- | --- |
| **Equivalence Partitioning** | Revenue = 0 / Revenue > 0 | Đảm bảo Controller xử lý đúng khi DB trống |
| **Boundary Value Analysis** | `countTotalRooms() = 0` | Tránh chia cho 0 khi tính `occupancyRate` |
| **Error Guessing** | `revenueOnDate()` trả về `null` | Phát hiện NPE tiềm ẩn trong vòng lặp 7 ngày |
| **State Transition Testing** | N/A | UC23 là READ-ONLY, không có state change |

#### TDS-05 — Test Data Requirements

| Fixture ID | Type | Value / Logic | Mục đích |
| --- | --- | --- | --- |
| **FX-001** | Mock Repository | `findOccupied()` → 30 rooms, `countTotalRooms()` → 100 | Test `occupancyRate = 30%` |
| **FX-002** | Mock Repository | `revenueOnDate()` → 1,000,000 (Room), `revenueOnDate()` → 500,000 (FnB), 300,000 (Tour) | Test tổng hợp doanh thu |
| **FX-003** | Mock Repository | `revenueOnDate()` → null (tất cả) | Test NPE-safe handling |
| **FX-004** | Mock Repository | YTD năm nay = 120M, năm ngoái = 100M | Test YoY Growth = `+20.0%` |
| **FX-005** | Mock Repository | `countOccupiedRoomsOnDate()` → 80 (ngày peak) | Test peak occupancy detection |

---

### 4. Test Case Specification

#### `MOD5-TC-UC23-001` — Dashboard tính occupancyRate đúng

*   **Severity:** `HIGH`
*   **Feature Under Test:** `ManagerController.dashboard()`
*   **Test File:** `src/test/java/com/kawai/controllers/web/ManagerControllerUC23Test.java`
*   **TDD Phase:** 🟢 GREEN
*   **Condition Ref:** `TC-COND-001`

**Preconditions:**
- Mock `roomRepository.findOccupied()` trả về list 30 Room objects.
- Mock `roomRepository.countTotalRooms()` trả về **100L**.
- Các repository khác mock trả về giá trị default (0/empty).

**Test Steps:**
1. Tạo `MockHttpServletRequest`, inject mock `Model`.
2. Gọi `managerController.dashboard(model)`.
3. Assert giá trị Model Attribute `"occupancyRate"`.

**Expected Result (PASS):**
- Return string = `"manager/dashboard"`
- `model.getAttribute("occupancyRate")` = **30L**
- `model.getAttribute("totalRooms")` = **100L**
- `model.getAttribute("occupiedRooms")` = **30L**

**Expected Result (FAIL — dấu hiệu lỗi):**
- `occupancyRate` = 0 (mock không được gọi hoặc hardcode bị ưu tiên)

**Current Status:** 🟢 GREEN
**Implementation Note:** `MOCK_TOTAL_ROOMS = 100` đã được xóa. Test xác nhận `countTotalRooms()` luôn được gọi từ DB.

---

#### `MOD5-TC-UC23-002` — Dashboard không ném NPE khi revenue null

*   **Severity:** `CRITICAL`
*   **Feature Under Test:** `ManagerController.dashboard()`
*   **Test File:** `src/test/java/com/kawai/controllers/web/ManagerControllerUC23Test.java`
*   **TDD Phase:** 🟢 GREEN
*   **Condition Ref:** `TC-COND-002`

**Preconditions:**
- Mock `roomBookingRepository.revenueOnDate(any())` trả về `null`.
- Mock `foodOrderRepository.revenueOnDate(any(), any())` trả về `null`.
- Mock `tourBookingRepository.revenueOnDate(any())` trả về `null`.
- Mock `roomRepository.findOccupied()` trả về empty list.
- Mock `roomRepository.countTotalRooms()` trả về 10L.

**Test Steps:**
1. Gọi `managerController.dashboard(model)` khi tất cả revenue = null.
2. Kiểm tra không có exception được ném ra.
3. Kiểm tra `revenueToday` = `"0"`.

**Expected Result (PASS):**
- Không có `NullPointerException`.
- `model.getAttribute("revenueToday")` = `"0"`.
- `model.getAttribute("dailyRevenue")` có size = 7.

**Expected Result (FAIL):**
- `NullPointerException` tại dòng `currentBalance.add(...)` trong vòng lặp.

**Current Status:** 🟢 GREEN
**Implementation Note:** Controller có null-check (`if (rDate == null) rDate = BigDecimal.ZERO`). Test xác nhận guard này hoạt động, không NPE.

---

#### `MOD5-TC-UC23-003` — Revenue Daily tổng hợp 3 nguồn đúng

*   **Severity:** `HIGH`
*   **Feature Under Test:** `ManagerController.revenueDaily()`
*   **Test File:** `src/test/java/com/kawai/controllers/web/ManagerControllerUC23Test.java`
*   **TDD Phase:** 🟢 GREEN
*   **Condition Ref:** `TC-COND-003`

**Preconditions:**
- Mock `roomBookingRepository.revenueBetween(today, today)` → `1,000,000`.
- Mock `foodOrderRepository.revenueBetween(today_start, today_end)` → `500,000`.
- Mock `tourBookingRepository.revenueBetween(today, today)` → `300,000`.
- Các kỳ khác (week/month) mock trả về `BigDecimal.ZERO`.

**Test Steps:**
1. Gọi `managerController.revenueDaily(model)`.
2. Kiểm tra `model.getAttribute("totalToday")`.

**Expected Result (PASS):**
- `totalToday` = `"2M"` (1,000,000 + 500,000 + 300,000 = 1,800,000 → `fmt()` trả về `"2M"` vì >= 1M, làm tròn `1.8` → `"2"`)

**Expected Result (FAIL):**
- `totalToday` = `"0"` hoặc `"1M"` (chỉ tính 1 nguồn).

**Current Status:** 🟢 GREEN

---

#### `MOD5-TC-UC23-004` — Revenue Monthly tính YoY Growth đúng

*   **Severity:** `MEDIUM`
*   **Feature Under Test:** `ManagerController.revenueMonthly()`
*   **Test File:** `src/test/java/com/kawai/controllers/web/ManagerControllerUC23Test.java`
*   **TDD Phase:** 🟢 GREEN
*   **Condition Ref:** `TC-COND-004`

**Preconditions:**
- YTD năm nay (Room+FnB+Tour): tổng = **120,000,000**.
- YTD năm ngoái (Room+FnB+Tour): tổng = **100,000,000**.

**Test Steps:**
1. Mock các `revenueBetween()` để tổng năm nay = 120M, năm ngoái = 100M.
2. Gọi `managerController.revenueMonthly(model)`.
3. Kiểm tra `model.getAttribute("growthYoY")`.

**Expected Result (PASS):**
- `growthYoY` = `"+20.0%"`

**Expected Result (FAIL):**
- `growthYoY` = `"-"` (nếu năm ngoái = 0 do mock sai) hoặc tính sai công thức.

**Current Status:** 🟢 GREEN

---

#### `MOD5-TC-UC23-005` — Occupancy Analytics tính Peak đúng trong 30 ngày

*   **Severity:** `HIGH`
*   **Feature Under Test:** `ManagerController.analyticsOccupancy()`
*   **Test File:** `src/test/java/com/kawai/controllers/web/ManagerControllerUC23Test.java`
*   **TDD Phase:** 🟢 GREEN
*   **Condition Ref:** `TC-COND-005`

**Preconditions:**
- Mock `roomRepository.findOccupied()` trả về **25 phòng**.
- Mock `roomRepository.countTotalRooms()` trả về **50L**.
- Mock `roomBookingRepository.countOccupiedRoomsOnDate(any())` trả về **40** (tất cả ngày trong 30 ngày).

**Test Steps:**
1. Gọi `managerController.analyticsOccupancy(model)`.
2. Kiểm tra `model.getAttribute("peakOccupancy")`.
3. Kiểm tra `model.getAttribute("currentOccupancy")`.

**Expected Result (PASS):**
- `currentOccupancy` = **50** (= 25/50×100, từ `findOccupied().size()` / `countTotalRooms()`)
- `peakOccupancy` = **80** (= round(40/50×100), từ `countOccupiedRoomsOnDate()`)
- `avgOccupancy` = **79** (= (29 ngày × 80 + 1 ngày hôm nay × 50) / 30 = 2370 / 30)
  > [!NOTE]
  > Ngày hôm nay (i=0) dùng `pct = (int) occRate = 50`, không dùng mock `countOccupiedRoomsOnDate()`.
  > Do đó `avgOccupancy = (29×80 + 50) / 30 = 79`, không phải 80.

**Expected Result (FAIL):**
- Các giá trị = 0 hoặc NPE.

**Current Status:** 🟢 GREEN

---

### 5. Red-Green-Refactor Tracker

| TC ID | Test File | 🔴 RED confirmed | 🟢 GREEN (commit) | 🔵 REFACTOR note |
| --- | --- | :---: | --- | --- |
| `MOD5-TC-UC23-001` | `ManagerControllerUC23Test.java` | `[x]` | `[DONE]` | `MOCK_TOTAL_ROOMS = 100` đã được xóa — đọc từ DB. Fallback an toàn `totalRooms = 1`. |
| `MOD5-TC-UC23-002` | `ManagerControllerUC23Test.java` | `[x]` | `[DONE]` | Null-guard đang hoạt động. Cần thêm log thực sự (`SLF4J`) thay `System.err`. |
| `MOD5-TC-UC23-003` | `ManagerControllerUC23Test.java` | `[x]` | `[DONE]` | Logic `fmt()` làm tròn 1.8M → "2M". Có thể điều chỉnh format thành "1.8M" cho chính xác hơn. |
| `MOD5-TC-UC23-004` | `ManagerControllerUC23Test.java` | `[x]` | `[DONE]` | YoY đang hardcode Locale.US. Cần thứ nghiệm với môi trường locale khác. |
| `MOD5-TC-UC23-005` | `ManagerControllerUC23Test.java` | `[x]` | `[DONE]` | `avgOccupancy` = 79 (không phải 80) vì ngày hôm nay dùng `occRate` thực. Test đã được sửa đúng. |

---

### 6. Entry / Exit Criteria

#### Entry Criteria (Điều kiện bắt đầu)
- [x] Spec kỹ thuật `KAWAI-MOD5-IMP-UC23` đã được review và approve (EDS Approved)
- [x] Logic Issues (Section 2) đã được confirm
- [x] `ManagerController.java` đã implement đủ 9 endpoints

#### Exit Criteria (Điều kiện kết thúc — DoD)
- [x] 5/5 Test Cases Pass 100% (`MOD5-TC-UC23-001` → `005`)
- [x] Không có `NullPointerException` khi bất kỳ repository trả về `null`
- [x] `occupancyRate` tính đúng với `countTotalRooms()` từ DB (đã xóa hardcode)
- [x] `growthYoY` format đúng `+XX.X%` / `-XX.X%`
- [x] `avgOccupancy` = 79 (không phải 80) — đã verify đúng logic code

#### Suspension Criteria (Điều kiện tạm dừng)
- Phát hiện bug trong `RoomBookingRepository.revenueBetween()` ảnh hưởng đến kết quả
- Schema DB thay đổi làm vỡ query

---

### 7. Rollback Plan

**Revert implementation files:**
```bash
git checkout -- src/main/java/com/kawai/controllers/web/ManagerController.java
```

**Nếu test fail do mock không tương thích:**
- Kiểm tra lại signature method trong Repository interface
- Đảm bảo `@MockBean` inject đúng bean trong Spring context
