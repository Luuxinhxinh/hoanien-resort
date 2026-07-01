# ENGINEERING DOCUMENTATION STANDARD (EDS) & TESTING SPECIFICATION v2.0

## UC19 — HỦY ĐƠN HÀNG F&B (CANCEL FOOD ORDER)

### HỆ THỐNG QUẢN LÝ NGHỈ DƯỠNG KAWAI RETREAT RESORT & HUB

| Field                    | Value                  |
| ------------------------ | ---------------------- |
| **Document ID**    | `KAWAI-EDS-UC19-001` |
| **Version**        | 1.0                    |
| **Date**           | 2026-07-02             |
| **Status**         | Approved               |
| **Document Owner** | Trịnh Minh Đức      |
| **Author**         | Trịnh Minh Đức      |
| **Reviewed by**    | Nguyễn Xuân Lưu     |
| **Based on EDS**   | KAWAI-ALL-EDS-001 v2.0 |

---

## CHANGELOG

| Ngày      | Người thực hiện | Nội dung thay đổi                                 |
| ---------- | ------------------- | ---------------------------------------------------- |
| 2026-07-02 | Trịnh Minh Đức   | Khởi tạo EDS Spec cho UC19 — Hủy đơn hàng F&B |

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

| Field                         | Value                                                    |
| ----------------------------- | -------------------------------------------------------- |
| **Use Case ID**         | UC-19                                                    |
| **Use Case Name**       | Hủy đơn hàng F&B (Cancel Food Order)                 |
| **Module**              | Module 3 — POS & Nhà hàng                             |
| **Primary Actor**       | F&B Staff                                                |
| **Secondary Actors**    | System, Customer (nhận hoàn tiền nếu applicable)     |
| **Bounded Context**     | F&B Order Management, Folio & Billing, Refund Management |
| **Data Classification** | Internal / Financial                                     |
| **Compliance Scope**    | POS-004, POS-006                                         |
| **Priority**            | 🟡 High                                                  |

### Tóm tắt nghiệp vụ

F&B Staff hủy đơn hàng chỉ khi đơn đang ở trạng thái `Pending`. Hệ thống hủy KOT cho các món, xử lý hoàn tiền theo phương thức thanh toán (xóa FolioItem cho Charge to Room, tạo RefundRequest cho VNPAY/Card).

**Quy tắc kinh doanh quan trọng:**

- Chỉ hủy được đơn đang ở trạng thái `Pending` — **không hủy được đơn đang nấu hoặc đã phục vụ**.
- **POS-004:** Không thể hủy/sửa món đã vào bếp (`Preparing` hoặc `Completed`).
- Nếu `CHARGE_TO_ROOM`: xóa `FolioItem` tương ứng (trả lại hạn mức tín dụng).
- Nếu `VNPAY`/Card: tạo `RefundRequest` với thông tin ngân hàng.
- **POS-006:** Báo lỗi nếu đơn không tồn tại.

---

## 2. Ma trận Truy vết (Traceability Matrix)

| Requirement ID | Loại | Mô tả yêu cầu                               | Thành phần Code                    | Compliance Target |
| -------------- | ----- | ----------------------------------------------- | ------------------------------------ | ----------------- |
| UC19-FR-01     | FR    | Hủy đơn chỉ khi status = Pending            | `PosServiceImpl.cancelOrder()`     | —                |
| UC19-BR-01     | BR    | Chặn hủy đơn không ở trạng thái Pending | Status check trong`cancelOrder()`  | POS-004           |
| UC19-FR-02     | FR    | Hủy tất cả KOT trong đơn                   | `FoodOrderDetailRepository.save()` | —                |
| UC19-FR-03     | FR    | Charge to Room → xóa FolioItem tương ứng   | `FolioItemRepository.delete()`     | —                |
| UC19-FR-04     | FR    | VNPAY/Card → tạo RefundRequest                | `RefundRequestRepository.save()`   | —                |
| UC19-BR-02     | BR    | Báo lỗi khi đơn không tồn tại            | `FoodOrderRepository.findById()`   | POS-006           |

---

## 3. Architecture Decision Records (ADR)

### ADR-UC19-001 — Phân nhánh hoàn tiền theo phương thức

**Quyết định:**

- `CHARGE_TO_ROOM` → Xóa `FolioItem` → credit limit phòng tự động tăng lại.
- `VNPAY/CARD` → Tạo `RefundRequest` → kế toán xử lý hoàn tiền thủ công qua cổng thanh toán.
- `CASH` → Không cần action hệ thống (hoàn tiền mặt trực tiếp).

**Hệ quả:** Không cần tích hợp callback hoàn tiền VNPAY tự động — giảm complexity.

### ADR-UC19-002 — Không hủy một phần

**Quyết định:** Chỉ hủy toàn bộ đơn — không hủy từng món riêng lẻ. Nếu cần hủy 1 món, dùng UC riêng (Cancel Item by Kitchen).

---

## 4. Non-Functional Requirements & SLA

### 4.1. Sự Toàn vẹn Dữ liệu (Data Integrity)

| Category  | Requirement                                     | Target       | Verification Method |
| --------- | ----------------------------------------------- | ------------ | ------------------- |
| Financial | Xóa FolioItem khi cancel Charge-to-Room        | 100% done    | TC-UC19-02          |
| Safety    | Không được hủy đơn đang Preparing/Ready | 100% blocked | TC-UC19-03          |
| Audit     | RefundRequest tạo đúng khi cancel VNPAY      | 100%         | TC-UC19-04          |

---

## 5. Domain Event Catalog

| Event Name         | Publisher          | Subscriber(s)     | Action                               |
| ------------------ | ------------------ | ----------------- | ------------------------------------ |
| `OrderCancelled` | `PosServiceImpl` | `FolioService`  | Xóa FolioItem (nếu Charge to Room) |
| `OrderCancelled` | `PosServiceImpl` | `RefundService` | Tạo RefundRequest (nếu VNPAY/Card) |

---

## 6. Kịch bản Kiểm thử Chi tiết (Gherkin)

### KỊCH BẢN 1: HỦY ĐƠN THÀNH CÔNG — CHARGE TO ROOM

**TC-UC19-E2E-001**

```gherkin
Feature: UC19 — Hủy đơn hàng F&B

  Background:
    Given Đơn hàng ORD-200 đang ở trạng thái "Pending"
    And ORD-200 có paymentType = "CHARGE_TO_ROOM"
    And Đã có FolioItem FI-100 liên kết với ORD-200, amount = 105,000 VND

  Scenario: F&B Staff hủy đơn Charge to Room
    When Nhân viên gọi API hủy đơn ORD-200
    Then ORD-200.orderStatus = "Cancelled"
    And Tất cả KOT (FoodOrderDetail) của ORD-200 bị hủy
    And FolioItem FI-100 bị xóa khỏi folio phòng
    And Credit limit phòng tăng lại 105,000 VND
    And HTTP 200 OK
```

### KỊCH BẢN 2: HỦY ĐƠN VNPAY — TẠO REFUND REQUEST

**TC-UC19-E2E-002**

```gherkin
  Scenario: Hủy đơn đã thanh toán qua VNPAY
    Given Đơn hàng ORD-201 đang ở trạng thái "Pending"
    And ORD-201 có paymentType = "VNPAY", total = 350,000 VND
    When Nhân viên hủy ORD-201
    Then ORD-201.orderStatus = "Cancelled"
    And Hệ thống tạo RefundRequest với amount = 350,000 VND
    And RefundRequest chứa thông tin ngân hàng khách hàng
```

### KỊCH BẢN 3: KHÔNG THỂ HỦY ĐƠN ĐANG NẤU (POS-004)

**TC-UC19-E2E-003**

```gherkin
  Scenario: Cố hủy đơn đang ở trạng thái Preparing
    Given Đơn hàng ORD-202 đang ở trạng thái "Preparing"
    When Nhân viên gọi API hủy đơn ORD-202
    Then Hệ thống từ chối với lỗi "Không thể hủy đơn đang được chế biến"
    And ORD-202 không thay đổi trạng thái
```

### KỊCH BẢN 4: ĐƠN KHÔNG TỒN TẠI (POS-006)

**TC-UC19-E2E-004**

```gherkin
  Scenario: Hủy đơn không tồn tại
    When Nhân viên gọi API hủy đơn ORD-999
    Then Hệ thống trả về lỗi "POS-006: Đơn hàng không tồn tại"
    And HTTP 404
```

---

## 7. Đặc tả API Chi tiết

### 7.1. MODULE 3 — UC19: Hủy đơn hàng

#### Endpoint Chính

| Method | Path                            | Auth Level | Required Roles            | Rate Limit | Idempotent? |
| ------ | ------------------------------- | ---------- | ------------------------- | ---------- | ----------- |
| POST   | `/api/pos/orders/{id}/cancel` | Protected  | ROLE_FB_STAFF, ROLE_ADMIN | 30/min     | No          |

---

### 7.2. Request & Response Specification

#### [POST] Hủy đơn hàng

```bash
POST /api/pos/orders/200/cancel
Authorization: Bearer [FB_STAFF_TOKEN]
```

**Response (200 OK):**

```json
{
  "success": true,
  "orderId": 200,
  "orderStatus": "Cancelled",
  "message": "Đơn hàng đã được hủy thành công"
}
```

**Response (400 — Đơn đang nấu):**

```json
{
  "error": "POS-004",
  "message": "Không thể hủy đơn đang được chế biến"
}
```

---

### 7.3. Error Codes & Business Rules

| Code        | HTTP Status | Message                                             | Trigger Condition        |
| ----------- | ----------- | --------------------------------------------------- | ------------------------ |
| `POS-004` | 400         | `Không thể hủy đơn đang được chế biến` | orderStatus ≠ Pending   |
| `POS-006` | 404         | `Đơn hàng không tồn tại`                    | orderId không tồn tại |

---

## 8. Phương pháp Xác minh (API Verification Samples)

### 8.1. Hủy đơn thành công — cURL

```bash
curl -X POST "https://api.kawairesort.com/api/pos/orders/200/cancel" \
  -H "Authorization: Bearer [FB_STAFF_TOKEN]"

# Expected Response (200 OK):
# - orderStatus = "Cancelled"
# - FolioItem bị xóa (nếu Charge to Room)
```

### 8.2. Kiểm tra DB — SQL Inspection

```sql
-- Kiểm tra đơn đã hủy
SELECT fo.id, fo.order_status FROM food_orders fo WHERE fo.id = 200;
-- Expected: order_status = 'Cancelled'

-- Kiểm tra FolioItem đã bị xóa (Charge to Room)
SELECT COUNT(*) FROM folio_items fi
WHERE fi.description LIKE '%Order #200%';
-- Expected: COUNT = 0

-- Kiểm tra RefundRequest (VNPAY)
SELECT rr.amount, rr.status FROM refund_requests rr
WHERE rr.order_id = 201;
-- Expected: amount = 350000, status = 'Pending'
```

---

## BẢNG TỔNG HỢP PHÂN QUYỀN UC19 (Authorization Matrix)

| Tác vụ                    | GUEST | CUSTOMER | F&B STAFF | ADMIN / MANAGER |
| --------------------------- | ----- | -------- | --------- | --------------- |
| Hủy đơn hàng (`POST`) | ❌    | ❌       | ✅        | ✅              |
