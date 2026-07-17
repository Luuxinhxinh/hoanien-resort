# ENGINEERING DOCUMENTATION STANDARD (EDS) & TESTING SPECIFICATION v2.0

## UC14 — QUẢN LÝ ĐƠN HÀNG F&B (TẠO ĐƠN ĂN TẠI BÀN — DINE-IN ORDER)

### HỆ THỐNG QUẢN LÝ NGHỈ DƯỠNG KAWAI RETREAT RESORT & HUB

| Field                    | Value                  |
| ------------------------ | ---------------------- |
| **Document ID**    | `KAWAI-EDS-UC14-001` |
| **Version**        | 1.0                    |
| **Date**           | 2026-07-02             |
| **Status**         | Approved               |
| **Document Owner** | Trịnh Minh Đức      |
| **Author**         | Trịnh Minh Đức      |
| **Reviewed by**    | Nguyễn Xuân Lưu     |
| **Based on EDS**   | KAWAI-ALL-EDS-001 v2.0 |

---

## CHANGELOG

| Ngày      | Người thực hiện | Nội dung thay đổi                                      |
| ---------- | ------------------- | --------------------------------------------------------- |
| 2026-07-02 | Trịnh Minh Đức   | Khởi tạo EDS Spec cho UC14 — Quản lý đơn hàng F&B |

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

| Field                         | Value                                                                 |
| ----------------------------- | --------------------------------------------------------------------- |
| **Use Case ID**         | UC-14                                                                 |
| **Use Case Name**       | Quản lý đơn hàng F&B — Tạo đơn ăn tại bàn (Dine-In Order) |
| **Module**              | Module 3 — POS & Nhà hàng                                          |
| **Primary Actor**       | F&B Staff (Nhân viên nhà hàng)                                    |
| **Secondary Actors**    | Kitchen Staff, System                                                 |
| **Bounded Context**     | F&B Order Management, Kitchen Display System (KDS), Table Management  |
| **Data Classification** | Internal / Operational                                                |
| **Compliance Scope**    | POS-001, POS-007, POS-008, POS-009, POS-010                           |
| **Priority**            | 🔴 Critical                                                           |

### Tóm tắt nghiệp vụ

Nhân viên F&B tạo đơn hàng ăn tại bàn (Dine-In) cho khách. Hệ thống kiểm tra tình trạng bàn, khung giờ hoạt động, tính sẵn có của món ăn, và xử lý hợp nhất đơn nếu bàn đang có đơn active.

**Quy tắc kinh doanh quan trọng:**

- **POS-001:** Đơn hàng không được rỗng (phải có ít nhất 1 món).
- **POS-007:** Chặn tạo đơn nếu bàn đang ở trạng thái `Cleaning` hoặc `Out_of_service`.
- **POS-008:** Chặn tạo đơn nếu bàn đã có lịch đặt trước trong vòng 2 giờ tới.
- **POS-009:** Không nhận Dine-In từ 23:00 → 08:00 sáng.
- **Hợp nhất đơn:** Nếu bàn đang có đơn active → tự động thêm món vào đơn hiện tại.
- Tự động chuyển trạng thái bàn sang `Occupied` khi tạo đơn thành công.

---

## 2. Ma trận Truy vết (Traceability Matrix)

| Requirement ID | Loại | Mô tả yêu cầu                                             | Thành phần Code                                | Compliance Target |
| -------------- | ----- | ------------------------------------------------------------- | ------------------------------------------------ | ----------------- |
| UC14-FR-01     | FR    | F&B Staff tạo đơn mới với danh sách món                | `PosServiceImpl.createOrder()`                 | POS-001           |
| UC14-FR-02     | FR    | Kiểm tra bàn không ở trạng thái Cleaning/Out_of_service | `PosServiceImpl.createOrder()` validation      | POS-007           |
| UC14-FR-03     | FR    | Kiểm tra không có lịch đặt bàn trong 2 giờ tới       | `TableReservationRepository.findConflicts()`   | POS-008           |
| UC14-FR-04     | BR    | Chặn Dine-In trong khung 23:00 → 08:00                      | `PosServiceImpl` — giờ hoạt động          | POS-009           |
| UC14-FR-05     | FR    | Hợp nhất đơn nếu bàn đang có đơn active             | `FoodOrderRepository.findActiveOrderByTable()` | —                |
| UC14-FR-06     | FR    | Chuyển bàn sang trạng thái`Occupied` sau khi tạo đơn | `RestaurantTableRepository.save()`             | —                |
| UC14-FR-07     | BR    | Kiểm tra tính sẵn có của món theo ngày trong tuần     | `FoodItemRepository.findByIdAndAvailableDay()` | POS-010           |
| UC14-NFR-01    | NFR   | Tạo đơn phải atomic — rollback nếu lỗi DB              | `@Transactional` trên `PosServiceImpl`      | Data Integrity    |

---

## 3. Architecture Decision Records (ADR)

### ADR-UC14-001 — Order Merge Strategy

**Bối cảnh:** Nếu bàn đang có đơn active, cần quyết định tạo đơn mới hay gộp vào đơn cũ.

**Quyết định:** Tự động **hợp nhất (merge)** — thêm các món mới vào đơn active hiện tại thay vì tạo đơn mới. Điều này tránh tình trạng 1 bàn có nhiều đơn đồng thời.

**Hệ quả:** F&B Staff không cần tra cứu ID đơn hiện tại — hệ thống tự xử lý.

### ADR-UC14-002 — Business Hours Guard

**Quyết định:** Kiểm tra giờ hoạt động ở tầng Service (`PosServiceImpl`) thay vì Controller để đảm bảo rule được thực thi ngay cả khi gọi API trực tiếp.

**Hệ quả:** Không thể bypass bằng cách gọi API thay vì dùng UI.

### ADR-UC14-003 — Kitchen Order Ticket (KOT)

**Quyết định:** Mỗi `FoodOrderDetail` được tạo với `kotStatus = "Pending"` để Kitchen Display System nhận lệnh bếp ngay khi order được tạo.

---

## 4. Non-Functional Requirements & SLA

### 4.1. Sự Toàn vẹn Dữ liệu (Data Integrity)

| Category    | Requirement                                        | Target        | Verification Method            |
| ----------- | -------------------------------------------------- | ------------- | ------------------------------ |
| ACID        | Tạo đơn là 1 atomic transaction                | RPO = 0       | `@Transactional` + unit test |
| Business    | Không tạo đơn ngoài giờ hoạt động         | 100% enforced | TC-UC14-03                     |
| Concurrency | Merge order đúng khi bàn đang có đơn active | 0% duplicates | TC-UC14-04                     |

### 4.2. Hiệu năng (Performance)

| Metric        | Target   | Verification         |
| ------------- | -------- | -------------------- |
| Response time | ≤ 500ms | Kiểm tra thủ công |

---

## 5. Domain Event Catalog

| Event Name             | Publisher          | Subscriber(s)    | Action                                       |
| ---------------------- | ------------------ | ---------------- | -------------------------------------------- |
| `FoodOrderCreated`   | `PosServiceImpl` | `KdsService`   | Hiển thị KOT lên Kitchen Display System   |
| `TableStatusUpdated` | `PosServiceImpl` | `PosWebFacade` | Cập nhật tình trạng bàn trên dashboard |

---

## 6. Kịch bản Kiểm thử Chi tiết (Gherkin)

### KỊCH BẢN 1: LUỒNG CHÍNH — TẠO ĐƠN DINE-IN THÀNH CÔNG

**TC-UC14-E2E-001**

```gherkin
Feature: UC14 — Tạo đơn ăn tại bàn (Dine-In Order)

  Background:
    Given Bàn "T01" đang ở trạng thái "Available"
    And Không có lịch đặt bàn "T01" trong vòng 2 giờ tới
    And Thời gian hiện tại là 12:00 (trong khung hoạt động)
    And Nhân viên F&B đã đăng nhập

  Scenario: Tạo đơn mới thành công
    When Nhân viên chọn bàn "T01" và thêm món "Phở bò" (1 phần, 80,000 VND)
    And Nhân viên xác nhận tạo đơn
    Then Hệ thống tạo FoodOrder mới với status "Pending"
    And FoodOrderDetail được tạo với kotStatus = "Pending"
    And Bàn "T01" chuyển trạng thái sang "Occupied"
    And Nhân viên nhận thông báo tạo đơn thành công
```

### KỊCH BẢN 2: CHẶN ĐƠN NGOÀI GIỜ (POS-009)

**TC-UC14-E2E-002**

```gherkin
  Scenario: Tạo đơn Dine-In lúc 23:30 — hệ thống từ chối
    Given Thời gian hiện tại là 23:30
    When Nhân viên cố tạo đơn Dine-In cho bàn "T01"
    Then Hệ thống từ chối với lỗi "POS-009: Nhà hàng đóng cửa từ 23:00 đến 08:00"
    And Không có FoodOrder nào được tạo
    And Bàn "T01" giữ nguyên trạng thái
```

### KỊCH BẢN 3: CHẶN KHI BÀN ĐANG DỌN (POS-007)

**TC-UC14-E2E-003**

```gherkin
  Scenario: Bàn đang ở trạng thái Cleaning — từ chối tạo đơn
    Given Bàn "T02" đang ở trạng thái "Cleaning"
    When Nhân viên cố tạo đơn cho bàn "T02"
    Then Hệ thống từ chối với lỗi "POS-007: Bàn đang được dọn dẹp"
    And Không có đơn nào được tạo
```

### KỊCH BẢN 4: HỢP NHẤT ĐƠN KHI BÀN ĐANG CÓ KHÁCH

**TC-UC14-E2E-004**

```gherkin
  Scenario: Bàn "T03" đang có đơn active — thêm món vào đơn cũ
    Given Bàn "T03" đang ở trạng thái "Occupied" với đơn active ORD-001
    When Nhân viên thêm "Nước cam" (50,000 VND) cho bàn "T03"
    Then Hệ thống thêm FoodOrderDetail vào đơn ORD-001 thay vì tạo đơn mới
    And ORD-001 vẫn là đơn duy nhất của bàn "T03"
```

### KỊCH BẢN 5: ĐƠN RỖNG — TỪ CHỐI (POS-001)

**TC-UC14-E2E-005**

```gherkin
  Scenario: Tạo đơn không có món nào
    When Nhân viên gửi yêu cầu tạo đơn với danh sách món rỗng
    Then Hệ thống từ chối với lỗi "POS-001: Đơn hàng không được rỗng"
```

---

## 7. Đặc tả API Chi tiết

### 7.1. MODULE 3 — UC14: Tạo đơn F&B Dine-In

#### Endpoint Chính

| Method | Path                | Auth Level | Required Roles            | Rate Limit | Idempotent? |
| ------ | ------------------- | ---------- | ------------------------- | ---------- | ----------- |
| POST   | `/api/pos/orders` | Protected  | ROLE_FB_STAFF, ROLE_ADMIN | 120/min    | No          |

#### Authorization Matrix

| Tác vụ / Endpoint                 | GUEST | CUSTOMER | F&B STAFF | KITCHEN | ADMIN / MANAGER |
| ----------------------------------- | ----- | -------- | --------- | ------- | --------------- |
| Tạo đơn hàng Dine-In (`POST`) | ❌    | ❌       | ✅        | ❌      | ✅              |

---

### 7.2. Request & Response Specification

#### [POST] Tạo đơn ăn tại bàn

```bash
POST /api/pos/orders
Authorization: Bearer [TOKEN]
Content-Type: application/json
```

**Request Body:**

```json
{
  "orderType": "dine-in",
  "tableId": 1,
  "items": [
    { "id": 10, "qty": 2, "price": 80000 },
    { "id": 11, "qty": 1, "price": 50000 }
  ],
  "note": "Ít cay"
}
```

**Response (200 OK — Thành công):**

```json
{
  "success": true,
  "orderId": 1001,
  "message": "Tạo đơn thành công"
}
```

**Response (400 Bad Request — Lỗi nghiệp vụ):**

```json
{
  "error": "POS-009",
  "message": "Nhà hàng đóng cửa từ 23:00 đến 08:00. Vui lòng quay lại sau."
}
```

---

### 7.3. Error Codes & Business Rules

| Code        | HTTP Status | Message                                                          | Trigger Condition                              |
| ----------- | ----------- | ---------------------------------------------------------------- | ---------------------------------------------- |
| `POS-001` | 400         | `Đơn hàng phải có ít nhất 1 món`                       | Danh sách items rỗng                         |
| `POS-007` | 400         | `Bàn đang dọn dẹp hoặc bảo trì, không thể tạo đơn` | Trạng thái bàn là Cleaning/Out_of_service  |
| `POS-008` | 400         | `Bàn đã có khách đặt trước trong 2 giờ tới`         | Có TableReservation conflict                  |
| `POS-009` | 400         | `Nhà hàng đóng cửa từ 23:00 đến 08:00`                 | Giờ hiện tại nằm ngoài khung hoạt động |
| `POS-010` | 400         | `Món ăn không phục vụ vào ngày hôm nay`                | MenuItem không available theo ngày           |
| `POS-002` | 404         | `Bàn ăn không tồn tại trong hệ thống`                   | tableId không hợp lệ                        |

---

## 8. Phương pháp Xác minh (API Verification Samples)

### 8.1. Tạo đơn Dine-In thành công — cURL

```bash
curl -X POST "https://api.kawairesort.com/api/pos/orders" \
  -H "Authorization: Bearer [FB_STAFF_TOKEN]" \
  -H "Content-Type: application/json" \
  -d '{
    "orderType": "dine-in",
    "tableId": 1,
    "items": [
      { "id": 10, "qty": 2, "price": 80000 }
    ]
  }'

# Expected Response (200 OK):
# - orderId != null
# - Bàn chuyển sang "Occupied"
# - FoodOrderDetail được tạo với kotStatus = "Pending"
```

### 8.2. Kiểm tra DB sau khi tạo đơn — SQL Inspection

```sql
-- Kiểm tra đơn hàng mới
SELECT fo.id, fo.order_type, fo.order_status, fo.is_paid_in_pos
FROM food_orders fo
WHERE fo.id = 1001;
-- Expected: order_type = 'dine-in', order_status = 'Pending'

-- Kiểm tra KOT chi tiết
SELECT fod.menu_item_id, fod.quantity, fod.kot_status
FROM food_order_details fod
WHERE fod.order_id = 1001;
-- Expected: kot_status = 'Pending' cho mọi món

-- Kiểm tra trạng thái bàn
SELECT rt.table_number, rt.table_status
FROM restaurant_tables rt
WHERE rt.id = 1;
-- Expected: table_status = 'Occupied'
```

### 8.3. Test chặn giờ hoạt động — cURL

```bash
# Giả lập tạo đơn lúc 23:30 (cần set system time hoặc mock)
curl -X POST "https://api.kawairesort.com/api/pos/orders" \
  -H "Authorization: Bearer [FB_STAFF_TOKEN]" \
  -H "Content-Type: application/json" \
  -d '{ "orderType": "dine-in", "tableId": 1, "items": [...] }'

# Expected Response (400 Bad Request):
# { "error": "POS-009", "message": "Nhà hàng đóng cửa từ 23:00 đến 08:00" }
```

---

## BẢNG TỔNG HỢP PHÂN QUYỀN UC14 (Authorization Matrix)

| Tác vụ / Endpoint            | GUEST | CUSTOMER | F&B STAFF | KITCHEN | ADMIN / MANAGER |
| ------------------------------ | ----- | -------- | --------- | ------- | --------------- |
| Tạo đơn Dine-In (`POST`)  | ❌    | ❌       | ✅        | ❌      | ✅              |
| Xem danh sách đơn (`GET`) | ❌    | ❌       | ✅        | ✅      | ✅              |
| Cập nhật trạng thái đơn  | ❌    | ❌       | ✅        | ✅      | ✅              |
