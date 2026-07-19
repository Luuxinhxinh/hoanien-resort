# ENGINEERING DOCUMENTATION STANDARD (EDS) & TESTING SPECIFICATION v2.0

## UC22 — ĐẶT MÓN TRỰC TUYẾN (ONLINE FOOD ORDER — VNPAY)

### HỆ THỐNG QUẢN LÝ NGHỈ DƯỠNG KAWAI RETREAT RESORT & HUB

| Field                    | Value                  |
| ------------------------ | ---------------------- |
| **Document ID**    | `KAWAI-EDS-UC22-001` |
| **Version**        | 1.0                    |
| **Date**           | 2026-07-02             |
| **Status**         | Approved               |
| **Document Owner** | Trịnh Minh Đức      |
| **Author**         | Trịnh Minh Đức      |
| **Reviewed by**    | Nguyễn Xuân Lưu     |
| **Based on EDS**   | KAWAI-ALL-EDS-001 v2.0 |

---

## CHANGELOG

| Ngày      | Người thực hiện | Nội dung thay đổi                                              |
| ---------- | ------------------- | ----------------------------------------------------------------- |
| 2026-07-02 | Trịnh Minh Đức   | Khởi tạo EDS Spec cho UC22 — Đặt món trực tuyến qua VNPAY |

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

| Field                         | Value                                                            |
| ----------------------------- | ---------------------------------------------------------------- |
| **Use Case ID**         | UC-22                                                            |
| **Use Case Name**       | Đặt món trực tuyến qua VNPAY (Online Food Order with VNPAY) |
| **Module**              | Module 3 — POS & Nhà hàng                                     |
| **Primary Actor**       | Customer (Khách đang check-in)                                 |
| **Secondary Actors**    | VNPAY Payment Gateway, F&B Staff, System                         |
| **Bounded Context**     | F&B Order Management, Payment Gateway Integration                |
| **Data Classification** | Internal / Financial                                             |
| **Compliance Scope**    | POS-001, POS-002                                                 |
| **Priority**            | 🔴 Critical                                                      |

### Tóm tắt nghiệp vụ

Khách đặt món qua web và thanh toán bằng VNPAY (thay vì Charge to Room). Đơn hàng được tạo với trạng thái `AWAITING_PAYMENT` và chỉ chuyển sang `Pending` sau khi VNPAY callback xác nhận thanh toán thành công.

**Quy tắc kinh doanh quan trọng:**

- Đơn tạo với `status = AWAITING_PAYMENT` — chờ VNPAY callback.
- Sau VNPAY callback thành công → gọi `payOrder()` → đơn chuyển sang `Pending`.
- Không có phụ phí 5% khi thanh toán VNPAY (phụ phí chỉ áp dụng cho Charge to Room).
- Nếu VNPAY callback không đến (timeout) → đơn có thể bị hủy tự động.

---

## 2. Ma trận Truy vết (Traceability Matrix)

| Requirement ID | Loại | Mô tả yêu cầu                                      | Thành phần Code                   | Compliance Target |
| -------------- | ----- | ------------------------------------------------------ | ----------------------------------- | ----------------- |
| UC22-FR-01     | FR    | Tạo đơn với status AWAITING_PAYMENT                | `PosServiceImpl.createOrder()`    | —                |
| UC22-FR-02     | FR    | Redirect khách đến trang thanh toán VNPAY          | `VNPayService.createPaymentUrl()` | —                |
| UC22-FR-03     | FR    | Nhận VNPAY IPN callback và xác thực chữ ký       | `VnPayPaymentController.ipn()`    | —                |
| UC22-FR-04     | FR    | Chuyển đơn AWAITING_PAYMENT → Pending sau callback | `PosServiceImpl.payOrder()`       | —                |
| UC22-BR-01     | BR    | Xác thực chữ ký VNPAY trước khi xử lý          | HMAC-SHA512 verification            | —                |
| UC22-BR-02     | BR    | Chỉ chấp nhận callback với responseCode = "00"     | VNPAY response code check           | —                |

---

## 3. Architecture Decision Records (ADR)

### ADR-UC22-001 — AWAITING_PAYMENT State

**Bối cảnh:** Cần phân biệt đơn đã đặt nhưng chưa trả tiền vs đơn đã thanh toán và đang chế biến.

**Quyết định:** Sử dụng trạng thái `AWAITING_PAYMENT` như một trạng thái trung gian. Đơn ở trạng thái này không gửi KOT đến bếp — chỉ khi chuyển sang `Pending` mới Kitchen mới nhận order.

### ADR-UC22-002 — IPN vs Return URL

**Quyết định:** Sử dụng **IPN (Instant Payment Notification)** của VNPAY để xử lý phía server, không dựa vào Return URL (có thể bị chặn bởi ad-blocker hoặc khách đóng tab). IPN đảm bảo đơn luôn được cập nhật dù khách không redirect về.

---

## 4. Non-Functional Requirements & SLA

| Category  | Requirement                                                   | Target | Verification Method |
| --------- | ------------------------------------------------------------- | ------ | ------------------- |
| Security  | Xác thực chữ ký VNPAY HMAC-SHA512 mọi callback           | 100%   | TC-UC22-02          |
| Integrity | Đơn chuyển Pending chỉ khi responseCode = "00"            | 100%   | TC-UC22-01          |
| Timeout   | Đơn AWAITING_PAYMENT không xử lý callback → không treo | —     | Monitor             |

---

## 5. Domain Event Catalog

| Event Name                | Publisher                  | Subscriber(s)  | Action                     |
| ------------------------- | -------------------------- | -------------- | -------------------------- |
| `VNPayPaymentConfirmed` | `VnPayPaymentController` | `PosService` | Chuyển đơn sang Pending |
| `OrderPaid`             | `PosServiceImpl`         | `KdsService` | Gửi KOT đến Kitchen     |

---

## 6. Kịch bản Kiểm thử Chi tiết (Gherkin)

### KỊCH BẢN 1: THANH TOÁN VNPAY THÀNH CÔNG

**TC-UC22-E2E-001**

```gherkin
Feature: UC22 — Đặt món trực tuyến qua VNPAY

  Background:
    Given Khách đã đăng nhập, đang ở phòng "101"
    And Đơn hàng ORD-300 được tạo với status = "AWAITING_PAYMENT"

  Scenario: VNPAY callback thành công
    When VNPAY gửi IPN với responseCode = "00" và chữ ký hợp lệ
    Then Hệ thống xác thực chữ ký HMAC-SHA512 thành công
    And Gọi payOrder(ORD-300)
    And ORD-300.orderStatus = "Pending"
    And ORD-300.isPaidInPos = true
    And Kitchen nhận KOT
    And HTTP 200 OK với body "00" (theo chuẩn VNPAY IPN)
```

### KỊCH BẢN 2: VNPAY CALLBACK CHỮ KÝ SAI

**TC-UC22-E2E-002**

```gherkin
  Scenario: VNPAY IPN với chữ ký không hợp lệ
    When VNPAY gửi IPN với chữ ký sai
    Then Hệ thống từ chối với lỗi "97" (Invalid signature - theo chuẩn VNPAY)
    And ORD-300 không thay đổi trạng thái
    And Không có KOT nào được tạo
```

### KỊCH BẢN 3: VNPAY CALLBACK THẤT BẠI (responseCode ≠ "00")

**TC-UC22-E2E-003**

```gherkin
  Scenario: Khách huỷ thanh toán trên trang VNPAY
    When VNPAY gửi IPN với responseCode = "24" (Khách hủy giao dịch)
    Then Hệ thống không cập nhật đơn hàng
    And ORD-300 vẫn ở trạng thái "AWAITING_PAYMENT"
```

### KỊCH BẢN 4: TẠO ĐƠN VNPAY THÀNH CÔNG

**TC-UC22-E2E-004**

```gherkin
  Scenario: Khách tạo đơn và nhận link thanh toán VNPAY
    When Khách đặt "Phở bò" x2 với paymentType = "VNPAY"
    Then Hệ thống tạo đơn với orderStatus = "AWAITING_PAYMENT"
    And Hệ thống trả về URL redirect đến trang thanh toán VNPAY
    And HTTP 200 OK
```

---

## 7. Đặc tả API Chi tiết

### 7.1. MODULE 3 — UC22: Đặt món trực tuyến VNPAY

#### Endpoints Chính

| Method | Path                | Auth Level | Required Roles | Rate Limit | Idempotent? |
| ------ | ------------------- | ---------- | -------------- | ---------- | ----------- |
| POST   | `/api/pos/orders` | Protected  | ROLE_USER      | 30/min     | No          |
| GET    | `/api/vnpay/ipn`  | Public     | —             | Unlimited  | No          |

---

### 7.2. Request & Response Specification

#### [POST] Tạo đơn với VNPAY

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
  "paymentType": "VNPAY",
  "items": [
    { "id": 10, "qty": 2, "price": 100000 }
  ]
}
```

**Response (200 OK):**

```json
{
  "success": true,
  "orderId": 300,
  "orderStatus": "AWAITING_PAYMENT",
  "paymentUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Amount=20000000&..."
}
```

---

### 7.3. Error Codes & Business Rules

| Code        | HTTP Status | Message                                    | Trigger Condition              |
| ----------- | ----------- | ------------------------------------------ | ------------------------------ |
| `POS-001` | 400         | `Đơn hàng phải có ít nhất 1 món` | items rỗng                    |
| `POS-002` | 400         | `Phòng không tồn tại`                | roomNumber không hợp lệ     |
| `VNP-97`  | 400         | `Invalid signature`                      | Chữ ký VNPAY không hợp lệ |

---

## 8. Phương pháp Xác minh (API Verification Samples)

### 8.1. Tạo đơn VNPAY — cURL

```bash
curl -X POST "https://api.kawairesort.com/api/pos/orders" \
  -H "Authorization: Bearer [CUSTOMER_TOKEN]" \
  -H "Content-Type: application/json" \
  -d '{
    "orderType": "room-svc",
    "roomNumber": "101",
    "paymentType": "VNPAY",
    "items": [{ "id": 10, "qty": 2, "price": 100000 }]
  }'

# Expected: paymentUrl != null, orderStatus = "AWAITING_PAYMENT"
```

### 8.2. Kiểm tra DB sau VNPAY callback — SQL Inspection

```sql
-- Trước callback
SELECT fo.order_status, fo.is_paid_in_pos FROM food_orders fo WHERE fo.id = 300;
-- Expected: order_status = 'AWAITING_PAYMENT', is_paid_in_pos = false

-- Sau callback thành công
SELECT fo.order_status, fo.is_paid_in_pos FROM food_orders fo WHERE fo.id = 300;
-- Expected: order_status = 'Pending', is_paid_in_pos = true
```

---

## BẢNG TỔNG HỢP PHÂN QUYỀN UC22 (Authorization Matrix)

| Tác vụ                           | GUEST | CUSTOMER | F&B STAFF | ADMIN |
| ---------------------------------- | ----- | -------- | --------- | ----- |
| Tạo đơn VNPAY (`POST`)        | ❌    | ✅       | ✅        | ✅    |
| Nhận VNPAY IPN callback (`GET`) | ✅    | ✅       | ✅        | ✅    |
