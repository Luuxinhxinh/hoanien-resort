# ENGINEERING DOCUMENTATION STANDARD (EDS) & TESTING SPECIFICATION v2.0

## UC23 — CẬP NHẬT TRẠNG THÁI ĐƠN HÀNG / GỌI THÊM MÓN (ORDER STATUS & ADD ITEMS)

### HỆ THỐNG QUẢN LÝ NGHỈ DƯỠNG KAWAI RETREAT RESORT & HUB

| Field                    | Value                  |
| ------------------------ | ---------------------- |
| **Document ID**    | `KAWAI-EDS-UC23-001` |
| **Version**        | 1.0                    |
| **Date**           | 2026-07-02             |
| **Status**         | Approved               |
| **Document Owner** | Trịnh Minh Đức      |
| **Author**         | Trịnh Minh Đức      |
| **Reviewed by**    | Nguyễn Xuân Lưu     |
| **Based on EDS**   | KAWAI-ALL-EDS-001 v2.0 |

---

## CHANGELOG

| Ngày      | Người thực hiện | Nội dung thay đổi                                                                  |
| ---------- | ------------------- | ------------------------------------------------------------------------------------- |
| 2026-07-02 | Trịnh Minh Đức   | Khởi tạo EDS Spec cho UC23 — Cập nhật trạng thái đơn hàng & Gọi thêm món |

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

| Field                         | Value                                                                            |
| ----------------------------- | -------------------------------------------------------------------------------- |
| **Use Case ID**         | UC-23                                                                            |
| **Use Case Name**       | Cập nhật trạng thái đơn hàng & Gọi thêm món (Order Status & Add Items) |
| **Module**              | Module 3 — POS & Nhà hàng                                                     |
| **Primary Actor**       | F&B Staff / Kitchen Staff                                                        |
| **Secondary Actors**    | System                                                                           |
| **Bounded Context**     | F&B Order Management, Kitchen Display System (KDS)                               |
| **Data Classification** | Internal / Operational                                                           |
| **Compliance Scope**    | POS-004, POS-005, POS-006                                                        |
| **Priority**            | 🟡 High                                                                          |

### Tóm tắt nghiệp vụ

UC23 bao gồm 2 luồng chính:

1. **Cập nhật trạng thái đơn hàng** (Pending → Preparing → Ready → Served): F&B Staff và Kitchen Staff theo dõi và cập nhật tiến trình chế biến/phục vụ.
2. **Gọi thêm món vào đơn đang ăn** (Add Items): F&B Staff thêm món mới vào đơn Dine-In đang active.

**Quy tắc kinh doanh quan trọng:**

- **Vòng đời trạng thái:** `Pending → Preparing → Ready → Served`.
- Khi chuyển `Preparing`: cập nhật KOT tất cả món `Pending` → `Preparing`.
- Khi chuyển `Ready`: cập nhật KOT `Preparing/Pending` → `Ready`.
- Khi chuyển `Served`: cập nhật KOT `Ready` → `Served`; Room Service tự động mark `isPaidInPos = true`.
- **Gọi thêm món:** Không cho Room Service (phải tạo đơn mới) — **POS-005**.
- Không gọi thêm vào đơn đã thanh toán hoặc đã hủy — **POS-006**.
- **POS-004:** Không thể hủy/sửa món đã `Preparing` hoặc `Completed`.
- **POS-010:** Kiểm tra tính sẵn có của món theo ngày khi gọi thêm.

---

## 2. Ma trận Truy vết (Traceability Matrix)

| Requirement ID | Loại | Mô tả yêu cầu                                                       | Thành phần Code                          | Compliance Target |
| -------------- | ----- | ----------------------------------------------------------------------- | ------------------------------------------ | ----------------- |
| UC23-FR-01     | FR    | Cập nhật trạng thái đơn hàng (Pending→Preparing→Ready→Served) | `PosServiceImpl.updateOrderStatus()`     | —                |
| UC23-FR-02     | FR    | Cập nhật KOT tương ứng khi đổi trạng thái đơn                | `FoodOrderDetailRepository.save()`       | —                |
| UC23-BR-01     | BR    | Room Service Served → tự động isPaidInPos = true                    | `PosServiceImpl.updateOrderStatus()`     | —                |
| UC23-FR-03     | FR    | Gọi thêm món vào đơn Dine-In đang active                         | `PosServiceImpl.addItemsToOrder()`       | —                |
| UC23-BR-02     | BR    | Chặn gọi thêm món cho đơn Room Service                            | orderType check trong`addItemsToOrder()` | POS-005           |
| UC23-BR-03     | BR    | Chặn gọi thêm vào đơn đã thanh toán/hủy                       | status check trong`addItemsToOrder()`    | POS-006           |
| UC23-BR-04     | BR    | Kiểm tra isAvailable của món khi gọi thêm                          | `FoodItemRepository.findById()` + check  | POS-010           |
| UC23-FR-04     | FR    | Batch update trạng thái nhiều đơn cùng lúc                       | `PosApiController.batchUpdateStatus()`   | —                |

---

## 3. Architecture Decision Records (ADR)

### ADR-UC23-001 — Tự động thanh toán Room Service khi Served

**Quyết định:** Khi F&B Staff cập nhật đơn Room Service sang `Served`, hệ thống tự động set `isPaidInPos = true`. Điều này vì Room Service đã được thanh toán trước (Charge to Room) hoặc VNPAY — không cần thu tiền lúc giao.

### ADR-UC23-002 — KOT Cascade Update

**Quyết định:** Khi đổi trạng thái đơn, hệ thống cập nhật cascade toàn bộ `FoodOrderDetail` có `kotStatus` phù hợp. Ví dụ: Pending→Preparing cập nhật tất cả detail có `kotStatus = "Pending"`.

### ADR-UC23-003 — Add Items chỉ cho Dine-In

**Bối cảnh:** Room Service đã hoàn tất việc tính tiền khi tạo đơn (Charge to Room). Thêm món sau sẽ phức tạp logic hoàn tiền.

**Quyết định:** Không hỗ trợ Add Items cho Room Service — yêu cầu tạo đơn mới. Điều này đơn giản hóa logic tài chính.

---

## 4. Non-Functional Requirements & SLA

| Category    | Requirement                                   | Target | Verification Method |
| ----------- | --------------------------------------------- | ------ | ------------------- |
| Consistency | KOT status phải đồng bộ với Order status | 100%   | TC-UC23-01          |
| Safety      | Không thêm món vào đơn Room Service     | 100%   | TC-UC23-03          |
| Safety      | Không thêm món đã hết hàng             | 100%   | TC-UC23-04          |

---

## 5. Domain Event Catalog

| Event Name               | Publisher          | Subscriber(s)    | Action                                           |
| ------------------------ | ------------------ | ---------------- | ------------------------------------------------ |
| `OrderStatusUpdated`   | `PosServiceImpl` | `KdsService`   | Cập nhật KOT trên Kitchen Display System      |
| `RoomServiceDelivered` | `PosServiceImpl` | `FolioService` | Xác nhận đơn đã giao (room service served) |
| `ItemsAdded`           | `PosServiceImpl` | `KdsService`   | Gửi KOT bổ sung lên bếp                      |

---

## 6. Kịch bản Kiểm thử Chi tiết (Gherkin)

### KỊCH BẢN 1: CẬP NHẬT TRẠNG THÁI ĐƠN (Pending → Preparing)

**TC-UC23-E2E-001**

```gherkin
Feature: UC23 — Cập nhật trạng thái đơn hàng

  Background:
    Given Đơn hàng ORD-400 (Dine-In) đang ở trạng thái "Pending"
    And ORD-400 có 2 FoodOrderDetail đều kotStatus = "Pending"

  Scenario: Kitchen Staff bắt đầu nấu
    When Kitchen Staff cập nhật ORD-400 sang trạng thái "Preparing"
    Then ORD-400.orderStatus = "Preparing"
    And Tất cả FoodOrderDetail của ORD-400 có kotStatus = "Preparing"
```

### KỊCH BẢN 2: ROOM SERVICE SERVED → TỰ ĐỘNG THANH TOÁN

**TC-UC23-E2E-002**

```gherkin
  Scenario: Giao đơn Room Service thành công
    Given Đơn hàng ORD-401 (Room Service) đang ở trạng thái "Ready"
    When F&B Staff cập nhật ORD-401 sang "Served"
    Then ORD-401.orderStatus = "Served"
    And ORD-401.isPaidInPos = true (tự động mark thanh toán)
    And Tất cả KOT của ORD-401 = "Served"
```

### KỊCH BẢN 3: GỌI THÊM MÓN CHO ROOM SERVICE — BỊ CHẶN (POS-005)

**TC-UC23-E2E-003**

```gherkin
  Scenario: Cố gọi thêm món cho đơn Room Service
    Given Đơn hàng ORD-402 là "room-svc" đang ở trạng thái "Pending"
    When F&B Staff cố thêm "Nước cam" vào ORD-402
    Then Hệ thống từ chối với lỗi "POS-005: Không hỗ trợ gọi thêm món cho đơn Room Service"
```

### KỊCH BẢN 4: GỌI THÊM MÓN HẾT HÀNG — BỊ CHẶN (POS-010)

**TC-UC23-E2E-004**

```gherkin
  Scenario: Gọi thêm món không còn phục vụ
    Given Đơn hàng ORD-403 (Dine-In) đang "Pending"
    And Món "Bún bò Huế" isAvailable = false
    When F&B Staff thêm "Bún bò Huế" vào ORD-403
    Then Hệ thống từ chối với lỗi "POS-010: Món ăn hiện không phục vụ"
```

### KỊCH BẢN 5: GỌI THÊM MÓN HỢP LỆ

**TC-UC23-E2E-005**

```gherkin
  Scenario: Gọi thêm món thành công
    Given Đơn hàng ORD-404 (Dine-In) đang "Pending"
    And Món "Chả giò" isAvailable = true
    When F&B Staff thêm 2 phần "Chả giò" vào ORD-404
    Then FoodOrderDetail mới được tạo với kotStatus = "Pending"
    And Kitchen nhận KOT bổ sung
```

---

## 7. Đặc tả API Chi tiết

### 7.1. MODULE 3 — UC23: Cập nhật trạng thái & Gọi thêm món

#### Endpoints Chính

| Method | Path                               | Auth Level | Required Roles              | Rate Limit | Idempotent? |
| ------ | ---------------------------------- | ---------- | --------------------------- | ---------- | ----------- |
| PUT    | `/api/pos/orders/{id}/status`    | Protected  | ROLE_FB_STAFF, ROLE_KITCHEN | 120/min    | No          |
| POST   | `/api/pos/orders/{id}/add-items` | Protected  | ROLE_FB_STAFF               | 60/min     | No          |
| POST   | `/api/pos/batch-update-status`   | Protected  | ROLE_FB_STAFF               | 30/min     | No          |

#### Authorization Matrix

| Tác vụ                      | GUEST | CUSTOMER | F&B STAFF | KITCHEN | ADMIN |
| ----------------------------- | ----- | -------- | --------- | ------- | ----- |
| Cập nhật trạng thái đơn | ❌    | ❌       | ✅        | ✅      | ✅    |
| Gọi thêm món (`POST`)    | ❌    | ❌       | ✅        | ❌      | ✅    |
| Batch update nhiều đơn     | ❌    | ❌       | ✅        | ❌      | ✅    |

---

### 7.2. Request & Response Specification

#### [PUT] Cập nhật trạng thái đơn

```bash
PUT /api/pos/orders/400/status
Authorization: Bearer [FB_STAFF_TOKEN]
Content-Type: application/json
```

**Request Body:**

```json
{
  "newStatus": "Preparing"
}
```

**Response (200 OK):**

```json
{
  "id": 400,
  "orderStatus": "Preparing",
  "message": "Cập nhật trạng thái thành công"
}
```

#### [POST] Gọi thêm món

```bash
POST /api/pos/orders/404/add-items
Authorization: Bearer [FB_STAFF_TOKEN]
Content-Type: application/json
```

**Request Body:**

```json
{
  "items": [
    { "id": 15, "qty": 2, "price": 60000 }
  ]
}
```

**Response (200 OK):**

```json
{
  "orderId": 404,
  "addedItems": 2,
  "message": "Thêm món thành công"
}
```

---

### 7.3. Error Codes & Business Rules

| Code        | HTTP Status | Message                                                    | Trigger Condition                           |
| ----------- | ----------- | ---------------------------------------------------------- | ------------------------------------------- |
| `POS-004` | 400         | `Không thể sửa món đã vào bếp`                   | kotStatus = Preparing/Completed             |
| `POS-005` | 400         | `Không hỗ trợ gọi thêm món cho đơn Room Service` | orderType = room-svc                        |
| `POS-006` | 400         | `Đơn hàng đã kết thúc hoặc bị hủy`             | orderStatus = Cancelled/Paid                |
| `POS-010` | 400         | `Món ăn hiện không phục vụ`                        | isAvailable = false hoặc không theo ngày |

---

## 8. Phương pháp Xác minh (API Verification Samples)

### 8.1. Cập nhật trạng thái — cURL

```bash
curl -X PUT "https://api.kawairesort.com/api/pos/orders/400/status" \
  -H "Authorization: Bearer [FB_STAFF_TOKEN]" \
  -H "Content-Type: application/json" \
  -d '{ "newStatus": "Preparing" }'

# Expected: orderStatus = "Preparing", KOT cập nhật
```

### 8.2. Kiểm tra DB — SQL Inspection

```sql
-- Kiểm tra trạng thái đơn
SELECT fo.id, fo.order_status, fo.is_paid_in_pos
FROM food_orders fo WHERE fo.id = 400;
-- Expected: order_status = 'Preparing'

-- Kiểm tra KOT cascade
SELECT fod.kot_status FROM food_order_details fod
WHERE fod.order_id = 400;
-- Expected: tất cả kot_status = 'Preparing'

-- Kiểm tra Room Service tự động thanh toán khi Served
SELECT fo.is_paid_in_pos FROM food_orders fo WHERE fo.id = 401;
-- Expected (sau khi Served): is_paid_in_pos = true
```

### 8.3. Batch update — cURL

```bash
curl -X POST "https://api.kawairesort.com/api/pos/batch-update-status" \
  -H "Authorization: Bearer [FB_STAFF_TOKEN]" \
  -H "Content-Type: application/json" \
  -d '{ "orderIds": [401, 402, 403], "newStatus": "Served" }'

# Expected: Tất cả đơn trong orderIds chuyển sang Served
```

---

## BẢNG TỔNG HỢP PHÂN QUYỀN UC23 (Authorization Matrix)

| Tác vụ                             | GUEST | CUSTOMER | F&B STAFF | KITCHEN | ADMIN |
| ------------------------------------ | ----- | -------- | --------- | ------- | ----- |
| Cập nhật trạng thái đơn hàng  | ❌    | ❌       | ✅        | ✅      | ✅    |
| Gọi thêm món vào đơn           | ❌    | ❌       | ✅        | ❌      | ✅    |
| Batch update nhiều đơn cùng lúc | ❌    | ❌       | ✅        | ❌      | ✅    |
| Xem chi tiết đơn hàng            | ❌    | ❌       | ✅        | ✅      | ✅    |
