# TEST-DRIVEN DEVELOPMENT SPECIFICATION TEMPLATE
## WF-05 — F&B / POS / Ghi nợ Folio

| Field | Value |
| --- | --- |
| **Document ID** | `KAWAI-TDD-WF05` |
| **Version** | 1.1 |
| **Date** | 2026-07-02 |
| **Status** | Approved |
| **Author** | Trịnh Minh Đức |

---

### 1. Thông tin Module

| Field | Value |
| --- | --- |
| **Module** | `Module 3 - F&B POS / Room Service` |
| **Spec gốc** | `EDS_WF05_SPEC.md` |
| **Priority** | CRITICAL |

---

### 2. Logic Issues Resolved

| # | Thực tế (schema / policy) | Fix áp dụng trong test |
| --- | --- | --- |
| **L1** | Cần check `room_status` = `Checked_In` và mã PIN theo BR-FB-01 | Mock `RoomBookingDetailRepository` để trả về phòng đang check-in. Khởi tạo `PasswordEncoder` mock để khớp hash của mã PIN. |
| **L2** | Vượt hạn mức Credit Limit sinh lỗi ở DB Trigger `TRG_Folio_Credit_Limit_Check` | Ở môi trường Unit Test Service, sẽ mock Exception bằng tay dựa trên logic hạn mức để bao phủ mã lỗi 403. |
| **L3** | Real-time KDS thông báo trạng thái thay đổi liên tục | Mock `SimpMessagingTemplate.convertAndSend` để verify message WebSocket đã được bắn đi đúng kênh. |

---

### 3. Test Design Specification (TDS)

#### TDS-01 — Scope / Phạm vi
- `PosServiceImpl.java` (Business Logic Layer)
- `PosApiController.java` (REST Interface)

#### TDS-02 — Test Basis
- **BR-FB-01**: Bắt buộc nhập mã PIN, phòng Checked-In, ghi nợ < Credit Limit.
- **BR-FB-02**: WebSocket cập nhật KDS theo thời gian thực.
- **BR-FB-04**: Vòng đời của Kitchen Order Ticket (KOT): `Pending` -> `Cooking` -> `Ready` -> `Served`.

---

### 4. Test Case Specification

#### `MOD3-TC-WF05-01` — Post-to-Room Thành công (Happy Path)
*   **Severity:** CRITICAL
*   **Feature Under Test:** `PosServiceImpl.chargeToRoom()`
*   **TDD Phase:** 🟢 GREEN

**Preconditions:**
- Mock `FoodOrder` ID 123 (tổng tiền 500,000 VND), `paymentType` = `DIRECT`.
- Mock `RoomBookingDetail` ID 456, `detail_status` = `Checked_In`, `is_charge_to_room_allowed` = `true`, `sub_credit_limit` = 2,000,000, `personal_pin_hash` = hash("1234").

**Test Steps:**
1. Setup `PasswordEncoder.matches("1234", hash)` -> `true`.
2. Setup `FolioRepository.save()` -> return mock FolioItem.
3. Call Service: `chargeToRoom(1234L, 456L, "1234")`.
4. Verify `FoodOrderRepository.save()` được gọi để đổi payment type.

**Expected Result (PASS):**
- Method trả về đối tượng `FolioItem` có `amount` = 500,000 VND và `source_department` = "FB".
- Đối tượng `FoodOrder` có `paymentType` = `Charge_To_Room`.

#### `MOD3-TC-WF05-02` — Lỗi Post-to-Room do Sai PIN
*   **Severity:** HIGH
*   **Feature Under Test:** `PosServiceImpl.chargeToRoom()`
*   **TDD Phase:** 🟢 GREEN

**Preconditions:**
- `RoomBookingDetail` tồn tại, cho phép ghi nợ.
- PIN cung cấp là "0000".

**Test Steps:**
1. Setup `PasswordEncoder.matches("0000", hash)` -> `false`.
2. Call Service: `chargeToRoom(1234L, 456L, "0000")`.

**Expected Result (PASS):**
- Throw `BusinessException` với code `FB-002` (Invalid PIN).
- Không gọi `FolioRepository.save()`.

#### `MOD3-TC-WF05-03` — Lỗi Post-to-Room do Vượt Hạn Mức (Credit Limit)
*   **Severity:** HIGH
*   **Feature Under Test:** `PosServiceImpl.chargeToRoom()`
*   **TDD Phase:** 🟢 GREEN

**Preconditions:**
- `RoomBookingDetail` có `sub_credit_limit` = 1,000,000 VND. Đã có dư nợ 800,000 VND.
- Đơn hàng hiện tại `FoodOrder` có tổng tiền là 500,000 VND (Vượt trần 300,000 VND).

**Test Steps:**
1. Setup `FolioRepository.sumAmountByRoomBookingDetailId` -> `800,000`.
2. Call Service: `chargeToRoom(...)`.

**Expected Result (PASS):**
- Throw `BusinessException` với code `FB-003` (Credit Limit Exceeded).

#### `MOD3-TC-WF05-04` — Gửi WebSocket khi tạo KOT mới
*   **Severity:** MEDIUM
*   **Feature Under Test:** `PosServiceImpl.createOrder()`
*   **TDD Phase:** 🟢 GREEN

**Preconditions:**
- Mock request payload tạo đơn hàng hợp lệ.
- Mock `SimpMessagingTemplate`.

**Test Steps:**
1. Call `createOrder(requestDto)`.
2. Verify: `SimpMessagingTemplate.convertAndSend("/topic/kds/orders", any())` được gọi chính xác 1 lần.

**Expected Result (PASS):**
- Hàm thực thi không ném lỗi.
- Event WebSocket được dispatch thành công.

---

### 5. Red-Green-Refactor Tracker

| TC ID | Test File | 🔴 RED confirmed | 🟢 GREEN (commit) | 🔵 REFACTOR note |
| --- | --- | :---: | --- | --- |
| `MOD3-TC-WF05-01` | `PosServiceUC18Test.java` | `[x]` | `[x]` | Đã pass, clean code. |
| `MOD3-TC-WF05-02` | `PosServiceUC18Test.java` | `[x]` | `[x]` | Đã pass. |
| `MOD3-TC-WF05-03` | `PosServiceUC18Test.java` | `[x]` | `[x]` | Đã pass. |
| `MOD3-TC-WF05-04` | `PosServiceUC18Test.java` | `[x]` | `[x]` | Đã pass. |

---

### 6. Entry / Exit Criteria

#### Entry Criteria
- [x] Schema DB đã có bảng `Food_Orders`, `Room_Booking_Details`.
- [x] Các luồng validation (BR-FB-01) đã được xác nhận ở mức requirement.

#### Exit Criteria
- [x] `mvn test` - xanh toàn bộ các case WF-05.
- [x] TDD phase chuyển sang 🟢 GREEN.
