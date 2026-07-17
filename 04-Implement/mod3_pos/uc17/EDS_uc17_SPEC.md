# ENGINEERING DOCUMENTATION STANDARD (EDS) & TESTING SPECIFICATION v2.0

## UC17 — ĐẶT MÓN ONLINE (ORDER FOOD ONLINE — ROOM SERVICE & E-MENU)

### HỆ THỐNG QUẢN LÝ NGHỈ DƯỠNG KAWAI RETREAT RESORT & HUB

| Field                    | Value                  |
| ------------------------ | ---------------------- |
| **Document ID**    | `KAWAI-EDS-UC17-001` |
| **Version**        | 1.0                    |
| **Date**           | 2026-07-02             |
| **Status**         | Approved               |
| **Document Owner** | Trịnh Minh Đức      |
| **Author**         | Trịnh Minh Đức      |
| **Reviewed by**    | Nguyễn Xuân Lưu     |
| **Based on EDS**   | KAWAI-ALL-EDS-001 v2.0 |

---

## CHANGELOG

| Ngày      | Người thực hiện | Nội dung thay đổi                                             |
| ---------- | ------------------- | ---------------------------------------------------------------- |
| 2026-07-02 | Trịnh Minh Đức   | Khởi tạo EDS Spec cho UC17 — Đặt món online (Room Service) |

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

| Field                         | Value                                                          |
| ----------------------------- | -------------------------------------------------------------- |
| **Use Case ID**         | UC-17                                                          |
| **Use Case Name**       | Đặt món online — Room Service & E-Menu (Order Food Online) |
| **Module**              | Module 3 — POS & Nhà hàng                                   |
| **Primary Actor**       | Customer (Khách đang check-in)                               |
| **Secondary Actors**    | F&B Staff, Kitchen Staff, System                               |
| **Bounded Context**     | F&B Order Management, Folio & Billing, Room Service            |
| **Data Classification** | Internal / Financial (Folio charges, Room Service fee)         |
| **Compliance Scope**    | POS-001, POS-002, POS-005, POS-009, POS-010                    |
| **Priority**            | 🔴 Critical                                                    |

### Tóm tắt nghiệp vụ

Khách đang check-in tại resort có thể gọi món ăn qua web (Room Service hoặc ăn tại nhà hàng với hình thức E-Menu). Hệ thống xác minh phòng, tính phụ phí 5%, và cho phép thanh toán qua nhiều phương thức: Charge to Room, VNPAY, hoặc tiền mặt.

**Quy tắc kinh doanh quan trọng:**

- **Phụ phí Room Service:** +5% tổng hóa đơn khi gọi Room Service (Charge to Room).
- **POS-005:** Báo lỗi nếu hạn mức tín dụng phòng không đủ.
- **POS-009:** Không nhận Dine-In từ 23:00 → 08:00 (chỉ áp dụng cho Dine-In tại bàn).
- **Charge to Room:** Tạo `FolioItem` trong folio phòng với nguồn `F&B`.
- **Gửi email xác nhận** đơn Room Service khi đặt thành công.

---

## 2. Ma trận Truy vết (Traceability Matrix)

| Requirement ID | Loại | Mô tả yêu cầu                                       | Thành phần Code                              | Compliance Target |
| -------------- | ----- | ------------------------------------------------------- | ---------------------------------------------- | ----------------- |
| UC17-FR-01     | FR    | Khách xem thực đơn theo ngày trong tuần           | `OrderFoodController.showMenu()`             | —                |
| UC17-FR-02     | FR    | Khách tạo đơn Room Service với danh sách món     | `PosApiController.createOrder()`             | POS-001           |
| UC17-FR-03     | FR    | Xác minh phòng tồn tại và đang có booking        | `RoomRepository.findByRoomNumber()`          | POS-002           |
| UC17-FR-04     | BR    | Tính phụ phí 5% khi paymentType = CHARGE_TO_ROOM     | `PosServiceImpl.createOrder()`               | —                |
| UC17-FR-05     | BR    | Kiểm tra và trừ hạn mức tín dụng phòng          | `FolioItemRepository` + Credit Limit check   | POS-005           |
| UC17-FR-06     | FR    | Tạo FolioItem trong folio phòng với nguồn F&B       | `FolioItemRepository.save()`                 | —                |
| UC17-FR-07     | FR    | Gửi email xác nhận đơn Room Service                | `EmailService.sendRoomServiceConfirmation()` | —                |
| UC17-BR-01     | BR    | Chặn Dine-In ngoài giờ hoạt động (23:00 → 08:00) | `PosServiceImpl` business hours guard        | POS-009           |

---

## 3. Architecture Decision Records (ADR)

### ADR-UC17-001 — Room Service Fee Policy

**Quyết định:** Phụ phí 5% được tính trên `subtotal` trước khi tạo `FolioItem`. Phụ phí này áp dụng duy nhất cho phương thức thanh toán `CHARGE_TO_ROOM`.

**Hệ quả:** Nếu khách chọn VNPAY hoặc tiền mặt, phụ phí không được tính vào Folio (phụ phí xử lý ở tầng frontend/payment gateway riêng).

### ADR-UC17-002 — Credit Limit vs SubCreditLimit

**Quyết định:** Khi kiểm tra hạn mức, hệ thống ưu tiên `subCreditLimit` của `RoomBookingDetail`. Nếu `subCreditLimit = null`, fallback về `creditLimit` của `RoomBooking`.

**Hệ quả:** Booking nhóm có thể phân quyền hạn mức tín dụng riêng cho từng phòng.

### ADR-UC17-003 — Dine-In vs Room Service

**Quyết định:** `orderType = "room-svc"` → Room Service (không yêu cầu bàn, yêu cầu phòng). `orderType = "dine-in"` → Dine-In tại bàn (yêu cầu bàn, không yêu cầu phòng).

---

## 4. Non-Functional Requirements & SLA

### 4.1. Sự Toàn vẹn Dữ liệu (Data Integrity)

| Category  | Requirement                                          | Target            | Verification Method |
| --------- | ---------------------------------------------------- | ----------------- | ------------------- |
| Financial | Phụ phí 5% tính chính xác                       | ±0 VND sai lệch | TC-UC17-01          |
| ACID      | Tạo đơn + FolioItem là 1 transaction             | RPO = 0           | `@Transactional`  |
| Credit    | Credit limit được kiểm tra trước khi ghi Folio | 100% enforced     | TC-UC17-06          |

### 4.2. Hiệu năng (Performance)

| Metric        | Target   | Verification         |
| ------------- | -------- | -------------------- |
| Response time | ≤ 500ms | Kiểm tra thủ công |

---

## 5. Domain Event Catalog

| Event Name                  | Publisher          | Subscriber(s)    | Action                                     |
| --------------------------- | ------------------ | ---------------- | ------------------------------------------ |
| `RoomServiceOrderCreated` | `PosServiceImpl` | `EmailService` | Gửi email xác nhận cho khách           |
| `FolioItemCreated`        | `PosServiceImpl` | `FolioService` | Cập nhật số dư Folio phòng            |
| `KitchenOrderReceived`    | `PosServiceImpl` | `KdsService`   | Hiển thị KOT lên Kitchen Display System |

---

## 6. Kịch bản Kiểm thử Chi tiết (Gherkin)

### KỊCH BẢN 1: LUỒNG CHÍNH — ĐẶT ROOM SERVICE THÀNH CÔNG, CHARGE TO ROOM

**TC-UC17-E2E-001**

```gherkin
Feature: UC17 — Đặt món online (Room Service)

  Background:
    Given Khách đã đăng nhập, đang check-in tại phòng "101"
    And Phòng "101" có hạn mức tín dụng còn lại 500,000 VND
    And Thực đơn có món "Phở bò" giá 100,000 VND (available hôm nay)

  Scenario: Đặt 2 phần Phở bò qua Room Service, Charge to Room
    When Khách chọn 2 phần "Phở bò" và phương thức "CHARGE_TO_ROOM"
    And Khách xác nhận đặt hàng
    Then Hệ thống tạo FoodOrder với orderType = "room-svc"
    And Phụ phí 5%: subtotal = 200,000, fee = 10,000, total = 210,000 VND
    And FolioItem được tạo với amount = 210,000 VND, sourceDepartment = "F&B"
    And Credit limit phòng được kiểm tra: 500,000 >= 210,000 ✅
    And Khách nhận email xác nhận đơn hàng
    And Hệ thống trả về HTTP 200 OK
```

### KỊCH BẢN 2: VƯỢT HẠN MỨC TÍN DỤNG (POS-005)

**TC-UC17-E2E-002**

```gherkin
  Scenario: Đặt món khi hạn mức tín dụng không đủ
    Given Phòng "102" có hạn mức tín dụng còn lại 50,000 VND
    When Khách đặt 3 phần "Bò beefsteak" (150,000 VND/phần) = 450,000 VND + 5% = 472,500 VND
    Then Hệ thống từ chối với lỗi "POS-005: Hạn mức tín dụng không đủ"
    And Không có FoodOrder hay FolioItem nào được tạo
    And Hệ thống trả về HTTP 400 Bad Request
```

### KỊCH BẢN 3: PHÒNG KHÔNG TỒN TẠI (POS-002)

**TC-UC17-E2E-003**

```gherkin
  Scenario: Khách nhập sai số phòng
    When Khách đặt Room Service cho phòng "999" (không tồn tại)
    Then Hệ thống từ chối với lỗi "POS-002: Phòng không tồn tại"
    And HTTP 400 Bad Request
```

### KỊCH BẢN 4: DINE-IN NGOÀI GIỜ HOẠT ĐỘNG (POS-009)

**TC-UC17-E2E-004**

```gherkin
  Scenario: Khách cố đặt Dine-In lúc 23:30
    Given Thời gian hiện tại là 23:30
    When Khách đặt đơn với orderType = "dine-in"
    Then Hệ thống từ chối với lỗi "POS-009: Nhà hàng đóng cửa"
```

### KỊCH BẢN 5: ĐẶT NHIỀU MÓN — TỔNG TIỀN TÍNH CHÍNH XÁC

**TC-UC17-E2E-005**

```gherkin
  Scenario: Đặt 2 món khác nhau, kiểm tra tổng tiền
    When Khách đặt: 2 phần "Phở bò" (100,000 VND) + 3 phần "Nước cam" (80,000 VND)
    Then subtotal = 200,000 + 240,000 = 440,000 VND
    And fee (5%) = 22,000 VND
    And totalAmount = 462,000 VND
    And FolioItem.amount = 462,000 VND
```

---

## 7. Đặc tả API Chi tiết

### 7.1. MODULE 3 — UC17: Đặt món online

#### Endpoint Chính

| Method | Path                | Auth Level | Required Roles           | Rate Limit | Idempotent? |
| ------ | ------------------- | ---------- | ------------------------ | ---------- | ----------- |
| POST   | `/api/pos/orders` | Protected  | ROLE_USER, ROLE_FB_STAFF | 30/min     | No          |
| GET    | `/order-food`     | Public     | —                       | 60/min     | Yes         |

#### Authorization Matrix

| Tác vụ / Endpoint                 | GUEST | CUSTOMER (check-in) | F&B STAFF | ADMIN |
| ----------------------------------- | ----- | ------------------- | --------- | ----- |
| Xem thực đơn (`GET`)           | ✅    | ✅                  | ✅        | ✅    |
| Đặt Room Service (`POST`)       | ❌    | ✅                  | ✅        | ✅    |
| Charge to Room (`CHARGE_TO_ROOM`) | ❌    | ✅ (có booking)    | ✅        | ✅    |

---

### 7.2. Request & Response Specification

#### [POST] Tạo đơn Room Service

```bash
POST /api/pos/orders
Authorization: Bearer [CUSTOMER_TOKEN]
Content-Type: application/json
```

**Request Body:**

```json
{
  "orderType": "room-svc",
  "roomNumber": "101",
  "paymentType": "CHARGE_TO_ROOM",
  "items": [
    { "id": 10, "qty": 2, "price": 100000 }
  ],
  "note": "Giao trước 12:00"
}
```

**Response (200 OK):**

```json
{
  "success": true,
  "orderId": 2001,
  "totalAmount": 210000,
  "message": "Đặt món thành công! Đơn hàng đang được xử lý."
}
```

**Response (400 — Credit Limit):**

```json
{
  "error": "POS-005",
  "message": "Hạn mức tín dụng của phòng không đủ để thanh toán!"
}
```

---

### 7.3. Error Codes & Business Rules

| Code        | HTTP Status | Message                                           | Trigger Condition                    |
| ----------- | ----------- | ------------------------------------------------- | ------------------------------------ |
| `POS-001` | 400         | `Đơn hàng phải có ít nhất 1 món`        | items rỗng                          |
| `POS-002` | 400         | `Phòng không tồn tại trong hệ thống`      | roomNumber không tồn tại          |
| `POS-005` | 400         | `Hạn mức tín dụng của phòng không đủ`  | creditLimit < totalAmount            |
| `POS-009` | 400         | `Nhà hàng đóng cửa từ 23:00 đến 08:00`  | Dine-In ngoài giờ hoạt động     |
| `POS-010` | 400         | `Món ăn không phục vụ vào ngày hôm nay` | MenuItem không available theo ngày |

---

## 8. Phương pháp Xác minh (API Verification Samples)

### 8.1. Đặt Room Service thành công — cURL

```bash
curl -X POST "https://api.kawairesort.com/api/pos/orders" \
  -H "Authorization: Bearer [CUSTOMER_TOKEN]" \
  -H "Content-Type: application/json" \
  -d '{
    "orderType": "room-svc",
    "roomNumber": "101",
    "paymentType": "CHARGE_TO_ROOM",
    "items": [{ "id": 10, "qty": 2, "price": 100000 }]
  }'

# Expected Response (200 OK):
# - orderId != null
# - totalAmount = 210000 (200000 + 5% fee)
# - FolioItem tạo với amount = 210000
```

### 8.2. Kiểm tra DB — SQL Inspection

```sql
-- Kiểm tra đơn Room Service
SELECT fo.id, fo.order_type, fo.order_status, fo.room_number
FROM food_orders fo WHERE fo.id = 2001;
-- Expected: order_type = 'room-svc'

-- Kiểm tra FolioItem được tạo
SELECT fi.amount, fi.source_department, fi.description
FROM folio_items fi
WHERE fi.description LIKE '%Order #2001%';
-- Expected: amount = 210000, source_department = 'F&B'

-- Kiểm tra credit limit còn lại
SELECT rbd.sub_credit_limit, rb.credit_limit
FROM room_booking_details rbd
JOIN room_bookings rb ON rbd.room_booking_id = rb.booking_id
WHERE rbd.id = 1;
```

---

## BẢNG TỔNG HỢP PHÂN QUYỀN UC17 (Authorization Matrix)

| Tác vụ / Endpoint                | GUEST | CUSTOMER | F&B STAFF | ADMIN / MANAGER |
| ---------------------------------- | ----- | -------- | --------- | --------------- |
| Xem thực đơn (`GET`)          | ✅    | ✅       | ✅        | ✅              |
| Đặt món Room Service (`POST`) | ❌    | ✅       | ✅        | ✅              |
| Charge to Room                     | ❌    | ✅       | ✅        | ✅              |
| Thanh toán VNPAY                  | ❌    | ✅       | ✅        | ✅              |
