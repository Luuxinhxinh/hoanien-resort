# TEST-DRIVEN DEVELOPMENT SPECIFICATION TEMPLATE
## WF-23 — Đặt Bàn Trực Tuyến

| Field | Value |
| --- | --- |
| **Document ID** | `KAWAI-TDD-WF23` |
| **Version** | 1.1 |
| **Date** | 2026-07-02 |
| **Status** | Approved |
| **Author** | Trịnh Minh Đức |

---

### 1. Thông tin Module

| Field | Value |
| --- | --- |
| **Module** | `Module 3 - Table Reservation (F&B)` |
| **Spec gốc** | `EDS_WF23_SPEC.md` |
| **Priority** | HIGH |

---

### 2. Test Design Specification (TDS)

#### TDS-01 — Scope / Phạm vi
- `TableReservationServiceImpl.java`
- `TableReservationRepository.java` (Query kiểm tra xung đột / overlap lịch đặt bàn)

#### TDS-02 — Test Basis
- **BR-FB-03**: Bàn được đặt trước giữ tối đa 30 phút. Các booking trên cùng một bàn phải cách nhau một khoảng an toàn (ít nhất 2 tiếng).
- Sức chứa (Capacity): Số lượng khách đặt bàn không được vượt quá `capacity` của `RestaurantTable`.

---

### 3. Test Case Specification

#### `MOD3-TC-WF23-01` — Đặt bàn thành công (Happy Path, không trùng lịch)
*   **Severity:** HIGH
*   **Feature Under Test:** `TableReservationServiceImpl.reserveTable()`
*   **TDD Phase:** 🟢 GREEN

**Preconditions:**
- DB tồn tại `RestaurantTable` ID = 1 (capacity = 4).
- Bàn chưa có bất kỳ reservation nào trong ngày "2026-10-10".

**Test Steps:**
1. Setup mock `TableReservationRepository.findOverlappingReservations(...)` -> return empty list.
2. Call `reserveTable(tableId=1, date="2026-10-10", time="19:00", guests=4)`.

**Expected Result (PASS):**
- Trả về đối tượng `TableReservation`.
- `status` = "Confirmed".
- `heldUntil` = "2026-10-10T19:30:00" (Cộng thêm 30 phút giữ bàn).

#### `MOD3-TC-WF23-02` — Lỗi do Overlap lịch (Bị trùng giờ)
*   **Severity:** HIGH
*   **Feature Under Test:** `TableReservationServiceImpl.reserveTable()`
*   **TDD Phase:** 🟢 GREEN

**Preconditions:**
- Bàn ID = 1 đã có người đặt lúc 19:00.

**Test Steps:**
1. Mock repository `findOverlappingReservations` -> return danh sách có 1 reservation.
2. User call request đặt cùng bàn ID = 1 lúc 19:30.

**Expected Result (PASS):**
- Throw `BusinessException` với code `FB-010` (Table already reserved in this time slot).
- DB không insert reservation mới.

#### `MOD3-TC-WF23-03` — Lỗi vượt Capacity (Số người > Sức chứa)
*   **Severity:** MEDIUM
*   **Feature Under Test:** `TableReservationServiceImpl.reserveTable()`
*   **TDD Phase:** 🟢 GREEN

**Preconditions:**
- Bàn ID = 2 có sức chứa tối đa (`capacity`) = 2 người.

**Test Steps:**
1. Call `reserveTable(tableId=2, ..., guests=4)`.

**Expected Result (PASS):**
- Throw `BusinessException` với code `FB-011` (Invalid guests count).

---

### 4. Red-Green-Refactor Tracker

| TC ID | Test File | 🔴 RED confirmed | 🟢 GREEN (commit) | 🔵 REFACTOR note |
| --- | --- | :---: | --- | --- |
| `MOD3-TC-WF23-01` | `TableReservationServiceTest.java` | `[x]` | `[x]` | Đã pass. |
| `MOD3-TC-WF23-02` | `TableReservationServiceTest.java` | `[x]` | `[x]` | Đã pass. |
| `MOD3-TC-WF23-03` | `TableReservationServiceTest.java` | `[x]` | `[x]` | Đã pass. |

---

### 5. Entry / Exit Criteria

#### Exit Criteria
- [x] Code đã thực thi thành công, test qua toàn bộ (`mvn test` pass).
- [x] Đã hoàn thành phase GREEN và refactor.
