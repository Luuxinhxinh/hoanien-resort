# ENGINEERING DOCUMENTATION STANDARD (EDS) & TESTING SPECIFICATION v2.0

## UC15 — XÁC NHẬN THANH TOÁN ĐƠN HÀNG (PAYMENT CONFIRMATION)

### HỆ THỐNG QUẢN LÝ NGHỈ DƯỠNG KAWAI RETREAT RESORT & HUB

| Field                    | Value                  |
| ------------------------ | ---------------------- |
| **Document ID**    | `KAWAI-EDS-UC15-001` |
| **Version**        | 1.0                    |
| **Date**           | 2026-07-02             |
| **Status**         | Approved               |
| **Document Owner** | Trịnh Minh Đức      |
| **Author**         | Trịnh Minh Đức      |
| **Reviewed by**    | Nguyễn Xuân Lưu     |
| **Based on EDS**   | KAWAI-ALL-EDS-001 v2.0 |

---

## CHANGELOG

| Ngày      | Người thực hiện | Nội dung thay đổi                                               |
| ---------- | ------------------- | ------------------------------------------------------------------ |
| 2026-07-02 | Trịnh Minh Đức   | Khởi tạo EDS Spec cho UC15 — Xác nhận thanh toán đơn hàng |

---

## MỤC LỤC

1. [Tổng quan Use Case](#1-tong-quan-use-case)
2. [Ma trận Truy vết (Traceability Matrix)](#2-ma-tran-truy-vet)
3. [Architecture Decision Records (ADR)](#3-architecture-decision-records)
4. [Non-Functional Requirements &amp; SLA](#4-non-functional-requirements--sla)
5. [Domain Event Catalog](#5-domain-event-catalog)
6. [Kịch bản Kiểm thử Chi tiết (Gherkin)](#6-kich-ban-kiem-thu-chi-tiet)
7. [Đặc tả API Chi tiết](#7-dac-ta-api-chi-tiet)
8. [Phương pháp Xác minh (API Verification Samples)](#8-phuong-phap-xac-minh)

---

## 1. Tổng quan Use Case

| Field                         | Value                                                     |
| ----------------------------- | --------------------------------------------------------- |
| **Use Case ID**         | UC-15                                                     |
| **Use Case Name**       | Xác nhận thanh toán đơn hàng (Payment Confirmation) |
| **Module**              | Module 3 — POS & Nhà hàng                              |
| **Primary Actor**       | F&B Staff (Nhân viên nhà hàng)                        |
| **Secondary Actors**    | System, Customer                                          |
| **Bounded Context**     | F&B Order Management, Table Management                    |
| **Data Classification** | Internal / Financial                                      |
| **Compliance Scope**    | POS-006                                                   |
| **Priority**            | 🔴 Critical                                               |

### Tóm tắt nghiệp vụ

Nhân viên F&B xác nhận khách đã thanh toán tại POS (tiền mặt/thẻ). Hệ thống đánh dấu đơn hàng là đã thanh toán, tự động chuyển bàn sang trạng thái `Cleaning`, và cập nhật lịch đặt bàn liên quan sang `Completed`.

**Quy tắc kinh doanh quan trọng:**

- `isPaidInPos = true` được set sau khi thanh toán xong.
- Bàn tự động chuyển sang `Cleaning` và ghi lại `cleaningStartTime`.
- Lịch đặt bàn (TableReservation) `Seated` → `Completed` + ghi `endTime`.
- **POS-006:** Đơn không tồn tại hoặc đã kết thúc → báo lỗi.

---

## 2. Ma trận Truy vết (Traceability Matrix)

| Requirement ID | Loại | Mô tả yêu cầu                                           | Thành phần Code                     | Compliance Target |
| -------------- | ----- | ----------------------------------------------------------- | ------------------------------------- | ----------------- |
| UC15-FR-01     | FR    | Đánh dấu đơn đã thanh toán (`isPaidInPos = true`) | `PosServiceImpl.payOrder()`         | —                |
| UC15-FR-02     | FR    | Chuyển trạng thái bàn sang`Cleaning`                  | `RestaurantTableRepository.save()`  | —                |
| UC15-FR-03     | FR    | Ghi`cleaningStartTime` cho bàn                           | `RestaurantTable.cleaningStartTime` | —                |
| UC15-FR-04     | FR    | Cập nhật lịch đặt bàn`Seated` → `Completed`      | `TableReservationRepository.save()` | —                |
| UC15-FR-05     | FR    | Ghi`endTime` cho TableReservation                         | `TableReservation.endTime`          | —                |
| UC15-BR-01     | BR    | Báo lỗi khi đơn không tồn tại                        | `FoodOrderRepository.findById()`    | POS-006           |

---

## 3. Architecture Decision Records (ADR)

### ADR-UC15-001 — Trạng thái bàn sau thanh toán

**Bối cảnh:** Sau khi khách thanh toán, cần xác định trạng thái bàn tiếp theo.

**Quyết định:** Bàn tự động chuyển sang `Cleaning` (không phải `Available`) để đảm bảo nhân viên dọn bàn trước khi đón khách tiếp theo. Housekeeping sẽ đổi sang `Available` sau khi dọn xong.

**Hệ quả:** Giảm rủi ro đón khách vào bàn chưa sạch.

### ADR-UC15-002 — Đơn AWAITING_PAYMENT vs Pending

**Quyết định:** Sau khi `payOrder()` được gọi, đơn hàng chuyển từ `AWAITING_PAYMENT` → `Pending` (đơn đang chờ kitchen xử lý) thay vì thẳng sang `Completed`. Điều này đảm bảo kitchen vẫn tiếp tục chế biến sau khi thanh toán.

---

## 4. Non-Functional Requirements & SLA

### 4.1. Sự Toàn vẹn Dữ liệu (Data Integrity)

| Category  | Requirement                                                  | Target        | Verification Method |
| --------- | ------------------------------------------------------------ | ------------- | ------------------- |
| ACID      | Thanh toán và cập nhật bàn là 1 transaction            | RPO = 0       | `@Transactional`  |
| Financial | `isPaidInPos` phải được lưu đúng trước khi return | 100% accurate | TC-UC15-01          |
| Audit     | `cleaningStartTime` phải được ghi đúng thời điểm  | 100% coverage | TC-UC15-01          |

### 4.2. Hiệu năng (Performance)

| Metric        | Target   | Verification         |
| ------------- | -------- | -------------------- |
| Response time | ≤ 300ms | Kiểm tra thủ công |

---

## 5. Domain Event Catalog

| Event Name                | Publisher          | Subscriber(s)          | Action                                     |
| ------------------------- | ------------------ | ---------------------- | ------------------------------------------ |
| `OrderPaid`             | `PosServiceImpl` | `TableService`       | Chuyển bàn sang Cleaning                 |
| `TableReservationEnded` | `PosServiceImpl` | `ReservationService` | Đánh dấu lịch đặt bàn là Completed |

---

## 6. Kịch bản Kiểm thử Chi tiết (Gherkin)

### KỊCH BẢN 1: LUỒNG CHÍNH — THANH TOÁN THÀNH CÔNG

**TC-UC15-E2E-001**

```gherkin
Feature: UC15 — Xác nhận thanh toán đơn hàng

  Background:
    Given Đơn hàng ORD-100 đang ở trạng thái "AWAITING_PAYMENT"
    And Bàn "T10" liên kết với ORD-100 đang "Occupied"
    And Có lịch đặt bàn RSV-005 trạng thái "Seated" cho bàn "T10" hôm nay

  Scenario: Nhân viên xác nhận thanh toán thành công
    When Nhân viên gọi API thanh toán cho ORD-100
    Then ORD-100.isPaidInPos = true
    And ORD-100.orderStatus chuyển sang "Pending"
    And Bàn "T10" chuyển sang trạng thái "Cleaning"
    And T10.cleaningStartTime được ghi lại
    And RSV-005.status chuyển sang "Completed"
    And RSV-005.endTime được ghi lại
```

### KỊCH BẢN 2: ĐƠN KHÔNG TỒN TẠI (POS-006)

**TC-UC15-E2E-002**

```gherkin
  Scenario: Thanh toán đơn không tồn tại
    When Nhân viên gọi API thanh toán cho ORD-999 (không tồn tại)
    Then Hệ thống trả về lỗi "POS-006: Đơn hàng không tồn tại"
    And Không có thay đổi nào trong DB
```

### KỊCH BẢN 3: BÀN KHÔNG CÓ LỊCH ĐẶT LIÊN QUAN

**TC-UC15-E2E-003**

```gherkin
  Scenario: Thanh toán đơn — bàn không có lịch đặt trong ngày
    Given Đơn hàng ORD-101 tại bàn "T11" không có TableReservation nào
    When Nhân viên gọi API thanh toán cho ORD-101
    Then ORD-101.isPaidInPos = true
    And Bàn "T11" chuyển sang "Cleaning"
    And Không có lỗi phát sinh (không bắt buộc phải có reservation)
```

---

## 7. Đặc tả API Chi tiết

### 7.1. MODULE 3 — UC15: Xác nhận thanh toán

#### Endpoint Chính

| Method | Path                         | Auth Level | Required Roles            | Rate Limit | Idempotent? |
| ------ | ---------------------------- | ---------- | ------------------------- | ---------- | ----------- |
| POST   | `/api/pos/orders/{id}/pay` | Protected  | ROLE_FB_STAFF, ROLE_ADMIN | 60/min     | No          |

---

### 7.2. Request & Response Specification

#### [POST] Xác nhận thanh toán đơn hàng

```bash
POST /api/pos/orders/100/pay
Authorization: Bearer [TOKEN]
```

**Response (200 OK — Thành công):**

```json
{
  "id": 100,
  "isPaidInPos": true,
  "orderStatus": "Pending",
  "table": {
    "id": 10,
    "tableStatus": "Cleaning"
  }
}
```

**Response (400/404 Bad Request):**

```json
{
  "error": "POS-006",
  "message": "Đơn hàng không tồn tại hoặc đã kết thúc."
}
```

---

### 7.3. Error Codes & Business Rules

| Code        | HTTP Status | Message                          | Trigger Condition                 |
| ----------- | ----------- | -------------------------------- | --------------------------------- |
| `POS-006` | 404         | `Đơn hàng không tồn tại` | orderId không tồn tại trong DB |

---

## 8. Phương pháp Xác minh (API Verification Samples)

### 8.1. Thanh toán thành công — cURL

```bash
curl -X POST "https://api.kawairesort.com/api/pos/orders/100/pay" \
  -H "Authorization: Bearer [FB_STAFF_TOKEN]"

# Expected Response (200 OK):
# - isPaidInPos = true
# - Bàn chuyển sang Cleaning
```

### 8.2. Kiểm tra DB sau thanh toán — SQL Inspection

```sql
-- Kiểm tra đơn đã thanh toán
SELECT fo.id, fo.is_paid_in_pos, fo.order_status
FROM food_orders fo WHERE fo.id = 100;
-- Expected: is_paid_in_pos = true, order_status = 'Pending'

-- Kiểm tra trạng thái bàn
SELECT rt.table_status, rt.cleaning_start_time
FROM restaurant_tables rt WHERE rt.id = 10;
-- Expected: table_status = 'Cleaning', cleaning_start_time != null

-- Kiểm tra lịch đặt bàn
SELECT tr.status, tr.end_time
FROM table_reservations tr WHERE tr.id = 5;
-- Expected: status = 'Completed', end_time != null
```

---

## BẢNG TỔNG HỢP PHÂN QUYỀN UC15 (Authorization Matrix)

| Tác vụ / Endpoint                        | GUEST | CUSTOMER | F&B STAFF | KITCHEN | ADMIN / MANAGER |
| ------------------------------------------ | ----- | -------- | --------- | ------- | --------------- |
| Xác nhận thanh toán tại POS (`POST`) | ❌    | ❌       | ✅        | ❌      | ✅              |
