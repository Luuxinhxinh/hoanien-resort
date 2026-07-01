# ENGINEERING DOCUMENTATION STANDARD (EDS) & TESTING SPECIFICATION v2.0

## UC16 — QUẢN LÝ BÀN ĂN (TABLE MANAGEMENT)

### HỆ THỐNG QUẢN LÝ NGHỈ DƯỠNG KAWAI RETREAT RESORT & HUB

| Field                    | Value                  |
| ------------------------ | ---------------------- |
| **Document ID**    | `KAWAI-EDS-UC16-001` |
| **Version**        | 1.0                    |
| **Date**           | 2026-07-02             |
| **Status**         | Approved               |
| **Document Owner** | Trịnh Minh Đức      |
| **Author**         | Trịnh Minh Đức      |
| **Reviewed by**    | Nguyễn Xuân Lưu     |
| **Based on EDS**   | KAWAI-ALL-EDS-001 v2.0 |

---

## CHANGELOG

| Ngày      | Người thực hiện | Nội dung thay đổi                               |
| ---------- | ------------------- | -------------------------------------------------- |
| 2026-07-02 | Trịnh Minh Đức   | Khởi tạo EDS Spec cho UC16 — Quản lý bàn ăn |

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

| Field                         | Value                                 |
| ----------------------------- | ------------------------------------- |
| **Use Case ID**         | UC-16                                 |
| **Use Case Name**       | Quản lý bàn ăn (Table Management) |
| **Module**              | Module 3 — POS & Nhà hàng          |
| **Primary Actor**       | Admin / Manager                       |
| **Secondary Actors**    | F&B Staff, System                     |
| **Bounded Context**     | Table Inventory Management            |
| **Data Classification** | Internal / Operational                |
| **Compliance Scope**    | TABLE-001, TABLE-004                  |
| **Priority**            | 🟡 High                               |

### Tóm tắt nghiệp vụ

Admin/Manager quản lý danh sách bàn ăn trong nhà hàng: tạo bàn mới, thay đổi trạng thái bàn (đóng bảo trì, mở lại), và quản lý menu items. Hệ thống áp dụng các rule ngăn thao tác không hợp lệ như đóng bàn đang có khách hoặc trùng tên bàn.

**Quy tắc kinh doanh quan trọng:**

- **TABLE-001:** Không được tạo bàn trùng tên (Unique Constraint).
- **TABLE-004:** Không được đóng bàn (`Out_of_service`) khi bàn đang `Occupied`.
- Soft delete bàn bằng cách set `isActive = false` thay vì xóa vật lý.
- Menu item: kiểm tra tên trùng và giá không âm khi tạo mới.

---

## 2. Ma trận Truy vết (Traceability Matrix)

| Requirement ID | Loại | Mô tả yêu cầu                              | Thành phần Code                             | Compliance Target |
| -------------- | ----- | ---------------------------------------------- | --------------------------------------------- | ----------------- |
| UC16-FR-01     | FR    | Tạo bàn mới với tên và sức chứa        | `TableServiceImpl.saveTable()`              | TABLE-001         |
| UC16-FR-02     | BR    | Chặn tạo bàn trùng tên                    | Unique validation trong`saveTable()`        | TABLE-001         |
| UC16-FR-03     | FR    | Thay đổi trạng thái bàn (toggle status)   | `TableServiceImpl.toggleStatus()`           | TABLE-004         |
| UC16-FR-04     | BR    | Không đóng bàn đang Occupied              | Kiểm tra trong`toggleStatus()`             | TABLE-004         |
| UC16-FR-05     | FR    | Soft delete bàn (isActive = false)            | `TableServiceImpl.softDeleteTable()`        | —                |
| UC16-FR-06     | FR    | Tạo menu item mới                            | `TableServiceImpl.createMenuItem()`         | —                |
| UC16-FR-07     | BR    | Menu item: tên không trùng, giá không âm | Validation trong`createMenuItem()`          | —                |
| UC16-FR-08     | FR    | Bật/tắt tình trạng sẵn có của menu item | `TableServiceImpl.toggleMenuAvailability()` | —                |

---

## 3. Architecture Decision Records (ADR)

### ADR-UC16-001 — Soft Delete vs Hard Delete

**Bối cảnh:** Bàn có thể đã có lịch sử đơn hàng — xóa vật lý sẽ phá vỡ referential integrity.

**Quyết định:** Sử dụng **Soft Delete** (`isActive = false`) thay vì xóa vật lý. Bàn bị xóa sẽ không xuất hiện trên màn hình hoạt động nhưng vẫn giữ nguyên lịch sử.

### ADR-UC16-002 — Unique Constraint trên tên bàn

**Quyết định:** Kiểm tra unique ở tầng Service (không chỉ dựa vào DB constraint) để trả về error message thân thiện thay vì `DataIntegrityViolationException` raw.

---

## 4. Non-Functional Requirements & SLA

### 4.1. Sự Toàn vẹn Dữ liệu (Data Integrity)

| Category | Requirement                                    | Target     | Verification Method |
| -------- | ---------------------------------------------- | ---------- | ------------------- |
| Unique   | Tên bàn phải unique trong hệ thống        | 0% trùng  | TC-UC16-001         |
| Safety   | Không xóa/đóng bàn đang phục vụ khách | 100% guard | TC-UC16-004         |

---

## 5. Domain Event Catalog

| Event Name             | Publisher            | Subscriber(s)    | Action                                 |
| ---------------------- | -------------------- | ---------------- | -------------------------------------- |
| `TableStatusChanged` | `TableServiceImpl` | `PosWebFacade` | Cập nhật dashboard trạng thái bàn |

---

## 6. Kịch bản Kiểm thử Chi tiết (Gherkin)

### KỊCH BẢN 1: TẠO BÀN TRÙNG TÊN — BỊ CHẶN

**TC-UC16-E2E-001**

```gherkin
Feature: UC16 — Quản lý bàn ăn

  Background:
    Given Đã có bàn "T01" trong hệ thống

  Scenario: Tạo bàn với tên đã tồn tại
    When Admin tạo bàn mới với tên "T01" và sức chứa 4
    Then Hệ thống từ chối với lỗi "Tên bàn đã tồn tại trong hệ thống"
    And Không có bàn mới nào được tạo
```

### KỊCH BẢN 2: TẠO BÀN MỚI HỢP LỆ

**TC-UC16-E2E-002**

```gherkin
  Scenario: Tạo bàn mới với tên hợp lệ
    When Admin tạo bàn "T05" với sức chứa 6
    Then Bàn "T05" được tạo thành công với isActive = true
    And tableStatus = "Available"
```

### KỊCH BẢN 3: ĐÓNG BẢO TRÌ BÀN HỢP LỆ

**TC-UC16-E2E-003**

```gherkin
  Scenario: Đóng bảo trì bàn đang Available
    Given Bàn "T02" đang ở trạng thái "AVAILABLE"
    When Admin thay đổi trạng thái bàn "T02" sang "OUT_OF_SERVICE"
    Then tableStatus của "T02" = "OUT_OF_SERVICE"
    And Bàn được lưu vào DB
```

### KỊCH BẢN 4: KHÔNG ĐƯỢC ĐÓNG BÀN ĐANG CÓ KHÁCH

**TC-UC16-E2E-004**

```gherkin
  Scenario: Cố đóng bàn đang Occupied
    Given Bàn "T03" đang ở trạng thái "Occupied"
    When Admin thay đổi trạng thái bàn "T03" sang "OUT_OF_SERVICE"
    Then Hệ thống từ chối với lỗi "Không thể đóng bàn đang có khách"
    And Trạng thái bàn "T03" không thay đổi
```

### KỊCH BẢN 5: TẠO MENU ITEM HỢP LỆ

**TC-UC16-E2E-005**

```gherkin
  Scenario: Admin tạo menu item mới
    When Admin tạo món "Chả giò hải sản" giá 80,000 VND
    Then MenuItem được lưu với isAvailable = true
    And Món xuất hiện trong danh sách menu
```

---

## 7. Đặc tả API Chi tiết

### 7.1. MODULE 3 — UC16: Quản lý bàn ăn

#### Endpoints Chính

| Method | Path                           | Auth Level | Required Roles | Rate Limit | Idempotent? |
| ------ | ------------------------------ | ---------- | -------------- | ---------- | ----------- |
| POST   | `/api/v1/tables`             | Protected  | ROLE_ADMIN     | 30/min     | No          |
| PUT    | `/api/v1/tables/{id}/status` | Protected  | ROLE_ADMIN     | 60/min     | Yes         |
| DELETE | `/api/v1/tables/{id}`        | Protected  | ROLE_ADMIN     | 10/min     | Yes         |

#### Authorization Matrix

| Tác vụ / Endpoint                    | GUEST | CUSTOMER | F&B STAFF | ADMIN / MANAGER |
| -------------------------------------- | ----- | -------- | --------- | --------------- |
| Tạo bàn mới (`POST`)              | ❌    | ❌       | ❌        | ✅              |
| Thay đổi trạng thái bàn (`PUT`) | ❌    | ❌       | ❌        | ✅              |
| Xóa bàn (soft delete)                | ❌    | ❌       | ❌        | ✅              |

---

### 7.2. Request & Response Specification

#### [POST] Tạo bàn mới

```bash
POST /api/v1/tables
Authorization: Bearer [ADMIN_TOKEN]
Content-Type: application/json
```

**Request Body:**

```json
{
  "tableNumber": "T05",
  "capacity": 6
}
```

**Response (200 OK):**

```json
{
  "id": 5,
  "tableNumber": "T05",
  "capacity": 6,
  "tableStatus": "Available",
  "isActive": true
}
```

---

### 7.3. Error Codes & Business Rules

| Code          | HTTP Status | Message                                                       | Trigger Condition                        |
| ------------- | ----------- | ------------------------------------------------------------- | ---------------------------------------- |
| `TABLE-001` | 400         | `Tên bàn đã tồn tại trong hệ thống`                 | Trùng tableNumber                       |
| `TABLE-004` | 400         | `Không thể thay đổi trạng thái bàn đang có khách` | Bàn đang Occupied có đơn chưa xong |

---

## 8. Phương pháp Xác minh (API Verification Samples)

### 8.1. Tạo bàn mới — cURL

```bash
curl -X POST "https://api.kawairesort.com/api/v1/tables" \
  -H "Authorization: Bearer [ADMIN_TOKEN]" \
  -H "Content-Type: application/json" \
  -d '{ "tableNumber": "T05", "capacity": 6 }'

# Expected: id != null, tableStatus = "Available"
```

### 8.2. Kiểm tra DB — SQL Inspection

```sql
-- Kiểm tra bàn mới
SELECT rt.table_number, rt.capacity, rt.table_status, rt.is_active
FROM restaurant_tables rt
WHERE rt.table_number = 'T05';
-- Expected: is_active = true, table_status = 'Available'

-- Kiểm tra unique constraint
SELECT COUNT(*) FROM restaurant_tables WHERE table_number = 'T01';
-- Expected: COUNT = 1 (không có duplicate)
```

---

## BẢNG TỔNG HỢP PHÂN QUYỀN UC16 (Authorization Matrix)

| Tác vụ                     | GUEST | CUSTOMER | F&B STAFF      | ADMIN / MANAGER |
| ---------------------------- | ----- | -------- | -------------- | --------------- |
| Tạo / Sửa / Xóa bàn      | ❌    | ❌       | ❌             | ✅              |
| Xem danh sách bàn          | ❌    | ❌       | ✅             | ✅              |
| Thay đổi trạng thái bàn | ❌    | ❌       | ✅ (hạn chế) | ✅              |
