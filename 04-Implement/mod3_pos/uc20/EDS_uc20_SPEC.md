# ENGINEERING DOCUMENTATION STANDARD (EDS) & TESTING SPECIFICATION v2.0

## UC20 — QUẢN LÝ THỰC ĐƠN (MENU MANAGEMENT)

### HỆ THỐNG QUẢN LÝ NGHỈ DƯỠNG KAWAI RETREAT RESORT & HUB

| Field                    | Value                  |
| ------------------------ | ---------------------- |
| **Document ID**    | `KAWAI-EDS-UC20-001` |
| **Version**        | 1.0                    |
| **Date**           | 2026-07-02             |
| **Status**         | Approved               |
| **Document Owner** | Trịnh Minh Đức      |
| **Author**         | Trịnh Minh Đức      |
| **Reviewed by**    | Nguyễn Xuân Lưu     |
| **Based on EDS**   | KAWAI-ALL-EDS-001 v2.0 |

---

## CHANGELOG

| Ngày      | Người thực hiện | Nội dung thay đổi                                  |
| ---------- | ------------------- | ----------------------------------------------------- |
| 2026-07-02 | Trịnh Minh Đức   | Khởi tạo EDS Spec cho UC20 — Quản lý thực đơn |

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

| Field                         | Value                                        |
| ----------------------------- | -------------------------------------------- |
| **Use Case ID**         | UC-20                                        |
| **Use Case Name**       | Quản lý thực đơn (Menu Management)      |
| **Module**              | Module 3 — POS & Nhà hàng                 |
| **Primary Actor**       | Admin / Manager                              |
| **Secondary Actors**    | Kitchen Staff, F&B Staff, System             |
| **Bounded Context**     | Menu Item Management, Kitchen Display System |
| **Data Classification** | Internal / Operational                       |
| **Compliance Scope**    | —                                           |
| **Priority**            | 🟡 High                                      |

### Tóm tắt nghiệp vụ

Admin/Manager quản lý toàn bộ thực đơn F&B: thêm món mới, cập nhật thông tin món, bật/tắt tình trạng sẵn có theo ngày trong tuần, và xóa món (soft delete). Hệ thống hỗ trợ tính năng chia thực đơn theo ngày (`availableDays`) để linh hoạt trong vận hành.

**Quy tắc kinh doanh quan trọng:**

- Tên món không được trùng lặp.
- Giá không được âm.
- `isAvailable`: Bật/tắt món theo thời điểm thực tế (bếp hết nguyên liệu, v.v.).
- `isAlwaysAvailable`: Cờ đặc biệt — nếu true, món bán mọi ngày bất kể `availableDays`.
- `availableDays`: Danh sách thứ trong tuần [MONDAY, TUESDAY, ...] được phép bán.

---

## 2. Ma trận Truy vết (Traceability Matrix)

| Requirement ID | Loại | Mô tả yêu cầu                                 | Thành phần Code                              | Compliance Target |
| -------------- | ----- | ------------------------------------------------- | ---------------------------------------------- | ----------------- |
| UC20-FR-01     | FR    | Tạo menu item mới                               | `MenuServiceImpl.createMenuItem()`           | —                |
| UC20-FR-02     | BR    | Tên món không được trùng lặp              | Unique validation                              | —                |
| UC20-FR-03     | BR    | Giá không được âm                           | Price validation trong createMenuItem          | —                |
| UC20-FR-04     | FR    | Cập nhật thông tin món ăn                    | `MenuServiceImpl.updateMenuItem()`           | —                |
| UC20-FR-05     | FR    | Bật/tắt tình trạng sẵn có (`isAvailable`) | `MenuItemApiController.toggleAvailability()` | —                |
| UC20-FR-06     | FR    | Lấy danh sách món theo ngày trong tuần       | `MenuItemApiController.getByDay()`           | —                |
| UC20-FR-07     | FR    | Xóa món (soft delete — isActive = false)       | `MenuServiceImpl.deleteMenuItem()`           | —                |

---

## 3. Architecture Decision Records (ADR)

### ADR-UC20-001 — isAlwaysAvailable vs availableDays

**Bối cảnh:** Một số món bán cố định mọi ngày (nước lọc, cơm trắng), trong khi các món khác chỉ bán theo thứ (thực đơn xoay vòng).

**Quyết định:**

- `isAlwaysAvailable = true` → Bỏ qua `availableDays` — luôn hiển thị.
- `isAlwaysAvailable = false` → Chỉ hiển thị nếu ngày hiện tại nằm trong `availableDays`.

### ADR-UC20-002 — Soft Delete

**Quyết định:** Không xóa vật lý menu item vì có thể đã được tham chiếu bởi `FoodOrderDetail` lịch sử. Dùng `isActive = false`.

---

## 4. Non-Functional Requirements & SLA

| Category | Requirement                                 | Target       | Verification Method |
| -------- | ------------------------------------------- | ------------ | ------------------- |
| Unique   | Tên món phải unique                      | 0% trùng    | TC-UC20-01          |
| Price    | Giá không âm                             | 100% guard   | TC-UC20-02          |
| Filter   | API lọc đúng món theo ngày trong tuần | 100% correct | TC-UC20-03          |

---

## 5. Domain Event Catalog

| Event Name           | Publisher           | Subscriber(s)  | Action                               |
| -------------------- | ------------------- | -------------- | ------------------------------------ |
| `MenuItemUpdated`  | `MenuServiceImpl` | `KdsService` | Cập nhật danh sách menu trên KDS |
| `MenuItemDisabled` | `MenuServiceImpl` | `PosService` | Ẩn món khỏi danh sách order      |

---

## 6. Kịch bản Kiểm thử Chi tiết (Gherkin)

### KỊCH BẢN 1: TẠO MÓN TRÙNG TÊN — BỊ CHẶN

**TC-UC20-E2E-001**

```gherkin
Feature: UC20 — Quản lý thực đơn

  Background:
    Given Đã có món "Phở bò" trong thực đơn

  Scenario: Tạo món trùng tên
    When Admin tạo món mới tên "Phở bò" giá 90,000 VND
    Then Hệ thống từ chối với lỗi "Tên món ăn đã tồn tại"
    And Không có MenuItem nào được tạo
```

### KỊCH BẢN 2: GIÁ ÂM — BỊ CHẶN

**TC-UC20-E2E-002**

```gherkin
  Scenario: Tạo món với giá âm
    When Admin tạo món "Nước suối" với giá -5000 VND
    Then Hệ thống từ chối với lỗi "Giá không được âm"
```

### KỊCH BẢN 3: LỌC THỰC ĐƠN THEO NGÀY

**TC-UC20-E2E-003**

```gherkin
  Scenario: Lấy thực đơn thứ Hai
    Given Có món "Bún bò" (availableDays = [MONDAY, WEDNESDAY])
    And Có món "Cơm trắng" (isAlwaysAvailable = true)
    And Có món "Bánh cuốn" (availableDays = [TUESDAY, THURSDAY])
    When Client gọi GET /api/menu-items?day=MONDAY
    Then Hệ thống trả về "Bún bò" và "Cơm trắng"
    And "Bánh cuốn" KHÔNG xuất hiện trong kết quả
```

### KỊCH BẢN 4: TẠO MÓN MỚI HỢP LỆ

**TC-UC20-E2E-004**

```gherkin
  Scenario: Admin tạo món mới hợp lệ
    When Admin tạo món "Chả giò hải sản" giá 80,000 VND, ngày bán: MONDAY, FRIDAY
    Then MenuItem được tạo với isAvailable = true, isActive = true
    And Món xuất hiện trong thực đơn thứ Hai và thứ Sáu
```

---

## 7. Đặc tả API Chi tiết

### 7.1. MODULE 3 — UC20: Menu Management

#### Endpoints Chính

| Method | Path                                       | Auth Level | Required Roles           | Rate Limit | Idempotent? |
| ------ | ------------------------------------------ | ---------- | ------------------------ | ---------- | ----------- |
| GET    | `/api/menu-items?day=MONDAY`             | Public     | —                       | 100/min    | Yes         |
| POST   | `/api/master-data?entityType=menu-items` | Protected  | ROLE_ADMIN               | 30/min     | No          |
| POST   | `/api/menu-items/{id}/toggle`            | Protected  | ROLE_ADMIN, ROLE_KITCHEN | 60/min     | Yes         |

#### Authorization Matrix

| Tác vụ                    | GUEST | CUSTOMER | F&B STAFF | KITCHEN | ADMIN |
| --------------------------- | ----- | -------- | --------- | ------- | ----- |
| Xem thực đơn (`GET`)   | ✅    | ✅       | ✅        | ✅      | ✅    |
| Tạo / Sửa món ăn        | ❌    | ❌       | ❌        | ❌      | ✅    |
| Bật/Tắt tình trạng món | ❌    | ❌       | ❌        | ✅      | ✅    |

---

### 7.2. Request & Response Specification

#### [GET] Lấy thực đơn theo ngày

```bash
GET /api/menu-items?day=MONDAY
```

**Response (200 OK):**

```json
[
  {
    "id": 10,
    "itemName": "Phở bò",
    "price": 80000,
    "category": "Main",
    "isAvailable": true,
    "imageUrl": "/images/pho-bo.jpg"
  }
]
```

#### [POST] Bật/tắt tình trạng sẵn có

```bash
POST /api/menu-items/10/toggle?isAvailable=false
Authorization: Bearer [KITCHEN_TOKEN]
```

**Response (200 OK):**

```json
{
  "id": 10,
  "itemName": "Phở bò",
  "isAvailable": false,
  "message": "Tình trạng món ăn đã được cập nhật"
}
```

---

### 7.3. Error Codes & Business Rules

| Code         | HTTP Status | Message                          | Trigger Condition          |
| ------------ | ----------- | -------------------------------- | -------------------------- |
| `MENU-001` | 400         | `Tên món ăn đã tồn tại` | Trùng itemName            |
| `MENU-002` | 400         | `Giá không được âm`      | price < 0                  |
| `MENU-003` | 404         | `Món ăn không tồn tại`    | menuItemId không hợp lệ |

---

## 8. Phương pháp Xác minh (API Verification Samples)

### 8.1. Lấy thực đơn theo ngày — cURL

```bash
curl "https://api.kawairesort.com/api/menu-items?day=MONDAY"
# Expected: Danh sách món available thứ Hai + isAlwaysAvailable=true
```

### 8.2. Kiểm tra DB — SQL Inspection

```sql
-- Kiểm tra món theo ngày
SELECT mi.item_name, mi.is_available, mi.is_always_available
FROM menu_items mi
JOIN menu_item_days mid ON mi.id = mid.menu_item_id
WHERE mid.day_of_week = 'MONDAY' AND mi.is_active = true;

-- Kiểm tra unique tên món
SELECT COUNT(*) FROM menu_items WHERE item_name = 'Phở bò' AND is_active = true;
-- Expected: COUNT = 1
```

---

## BẢNG TỔNG HỢP PHÂN QUYỀN UC20 (Authorization Matrix)

| Tác vụ                | GUEST | CUSTOMER | F&B STAFF | KITCHEN | ADMIN |
| ----------------------- | ----- | -------- | --------- | ------- | ----- |
| Xem thực đơn         | ✅    | ✅       | ✅        | ✅      | ✅    |
| Tạo / Sửa / Xóa món | ❌    | ❌       | ❌        | ❌      | ✅    |
| Bật/Tắt sẵn có món | ❌    | ❌       | ❌        | ✅      | ✅    |
