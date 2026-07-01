# TEST-DRIVEN DEVELOPMENT SPECIFICATION TEMPLATE
## WF-24 — Hủy Đơn Hàng F&B & Hoàn Tiền

| Field | Value |
| --- | --- |
| **Document ID** | `KAWAI-TDD-WF24` |
| **Version** | 1.1 |
| **Date** | 2026-07-02 |
| **Status** | Approved |
| **Author** | Trịnh Minh Đức |

---

### 1. Thông tin Module

| Field | Value |
| --- | --- |
| **Module** | `Module 3 - F&B Cancel/Refund` |
| **Spec gốc** | `EDS_WF24_SPEC.md` |
| **Priority** | HIGH |

---

### 2. Test Design Specification (TDS)

#### TDS-01 — Scope / Phạm vi
- `PosServiceImpl.cancelOrder()`
- `FolioRepository` (Dùng để hoàn nguyên giao dịch ký nợ phòng)
- `VnPayServiceImpl` (Dùng để gửi yêu cầu hoàn tiền cho VNPay)

#### TDS-02 — Test Basis
- **Quy tắc Trạng Thái (KOT Status)**: Bếp đã bắt đầu nấu (`Cooking`), đã xong (`Ready`), hoặc đã phục vụ (`Served`) thì tuyệt đối KHÔNG cho phép hủy đơn. Chỉ đơn `Pending` mới được hủy.
- **Quy tắc Hoàn Tiền**: 
  - Nếu đã thanh toán trực tiếp qua VNPay (`paymentType` = `VNPay`), hệ thống cần gọi VNPay Refund API.
  - Nếu thanh toán ký nợ (`paymentType` = `Charge_To_Room`), hệ thống cần xóa dòng nợ trong Folio (`FolioItem`) để hoàn hạn mức.

---

### 3. Test Case Specification

#### `MOD3-TC-WF24-01` — Hủy đơn Post-to-Room thành công
*   **Severity:** HIGH
*   **Feature Under Test:** `PosServiceImpl.cancelOrder()`
*   **TDD Phase:** 🟢 GREEN

**Preconditions:**
- Mock `FoodOrder` ID = 111 với `paymentType` = "Charge_To_Room", `kotStatus` = "Pending".
- Mock `FolioItem` ID = 222 liên kết với `FoodOrder` ID 111.

**Test Steps:**
1. Setup repository trả về `FoodOrder` khi `findById(111L)`.
2. Mock `FolioRepository.findByFoodOrderId(111L)` trả về `FolioItem` ID 222.
3. Call `cancelOrder(111L)`.

**Expected Result (PASS):**
- Hàm thực thi không lỗi.
- `FolioRepository.delete(folioItem)` được gọi (hoặc đổi trạng thái sang Deleted).
- Trạng thái `FoodOrder.kotStatus` được cập nhật thành "Cancelled".
- `SimpMessagingTemplate` bắn event hủy món xuống KDS.

#### `MOD3-TC-WF24-02` — Chặn hủy đơn khi bếp đang nấu
*   **Severity:** MEDIUM
*   **Feature Under Test:** `PosServiceImpl.cancelOrder()`
*   **TDD Phase:** 🟢 GREEN

**Preconditions:**
- Mock `FoodOrder` ID = 112 với `kotStatus` = "Cooking".

**Test Steps:**
1. Call `cancelOrder(112L)`.

**Expected Result (PASS):**
- Throw `BusinessException` với code `FB-020` (Cannot cancel order in progress).
- KHÔNG gọi repository để xóa Folio hoặc hoàn tiền VNPay.

#### `MOD3-TC-WF24-03` — Hủy đơn thanh toán VNPay và gọi Refund API
*   **Severity:** HIGH
*   **Feature Under Test:** `PosServiceImpl.cancelOrder()`
*   **TDD Phase:** 🟢 GREEN

**Preconditions:**
- Mock `FoodOrder` ID = 113 với `paymentType` = "VNPay", `kotStatus` = "Pending".
- Mock PaymentTransaction liên kết với order.

**Test Steps:**
1. Mock `VnPayServiceImpl.processRefund(...)` trả về `true`.
2. Call `cancelOrder(113L)`.

**Expected Result (PASS):**
- `VnPayServiceImpl.processRefund` được gọi.
- `FoodOrder.kotStatus` chuyển thành "Cancelled".

---

### 4. Red-Green-Refactor Tracker

| TC ID | Test File | 🔴 RED confirmed | 🟢 GREEN (commit) | 🔵 REFACTOR note |
| --- | --- | :---: | --- | --- |
| `MOD3-TC-WF24-01` | `PosServiceUC19Test.java` | `[x]` | `[x]` | Đã pass, kiểm tra xóa Folio thành công. |
| `MOD3-TC-WF24-02` | `PosServiceUC19Test.java` | `[x]` | `[x]` | Đã pass, exception văng ra chính xác. |
| `MOD3-TC-WF24-03` | `PosServiceUC19Test.java` | `[x]` | `[x]` | Đã pass logic gọi VNPay. |

---

### 5. Entry / Exit Criteria

#### Exit Criteria
- [x] Code đã thực thi thành công, test qua toàn bộ (`mvn test` pass).
- [x] Đã hoàn thành phase GREEN và refactor.
