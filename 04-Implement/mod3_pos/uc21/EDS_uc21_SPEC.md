# ENGINEERING DOCUMENTATION STANDARD (EDS) & TESTING SPECIFICATION v2.0

## UC21 — ĐẶT BÀN TRỰC TUYẾN (ONLINE TABLE RESERVATION)

### HỆ THỐNG QUẢN LÝ NGHỈ DƯỠNG KAWAI RETREAT RESORT & HUB

| Field                    | Value                  |
| ------------------------ | ---------------------- |
| **Document ID**    | `KAWAI-EDS-UC21-001` |
| **Version**        | 1.0                    |
| **Date**           | 2026-07-02             |
| **Status**         | Approved               |
| **Document Owner** | Trịnh Minh Đức      |
| **Author**         | Trịnh Minh Đức      |
| **Reviewed by**    | Nguyễn Xuân Lưu     |
| **Based on EDS**   | KAWAI-ALL-EDS-001 v2.0 |

---

## CHANGELOG

| Ngày      | Người thực hiện | Nội dung thay đổi                                    |
| ---------- | ------------------- | ------------------------------------------------------- |
| 2026-07-02 | Trịnh Minh Đức   | Khởi tạo EDS Spec cho UC21 — Đặt bàn trực tuyến |

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
| **Use Case ID**         | UC-21                                                            |
| **Use Case Name**       | Đặt bàn trực tuyến (Online Table Reservation)               |
| **Module**              | Module 3 — POS & Nhà hàng                                     |
| **Primary Actor**       | Customer (Khách đang check-in hoặc khách vãng lai)          |
| **Secondary Actors**    | F&B Staff, System                                                |
| **Bounded Context**     | Table Reservation Management, Email Notification                 |
| **Data Classification** | Internal / Operational                                           |
| **Compliance Scope**    | TABLE-002, TABLE-003, TABLE-005, TABLE-006, TABLE-007, TABLE-008 |
| **Priority**            | 🟡 High                                                          |

### Tóm tắt nghiệp vụ

Khách đặt bàn trực tuyến qua website. Hệ thống kiểm tra sức chứa, khung giờ hoạt động, tình trạng trùng lịch, và xác minh thông tin lưu trú (nếu chưa đăng nhập). Sau khi đặt thành công, hệ thống gửi email xác nhận.

**Quy tắc kinh doanh quan trọng:**

- **TABLE-002:** Số khách không được vượt sức chứa bàn.
- **TABLE-003:** Kiểm tra trùng lịch (conflict), buffer 15 phút giữa các lịch.
- **TABLE-005:** Nếu chưa đăng nhập — phải nhập số phòng và xác minh đang lưu trú.
- **TABLE-006:** Thời gian đặt tối thiểu 30 phút tính từ hiện tại.
- **TABLE-007:** Không đặt bàn đang `Occupied/Cleaning` trong 2 giờ tới.
- **TABLE-008:** Không nhận đặt bàn từ 23:00 → 08:00.
- Gửi email xác nhận sau khi đặt thành công.

---

## 2. Ma trận Truy vết (Traceability Matrix)

| Requirement ID | Loại | Mô tả yêu cầu                                                 | Thành phần Code                                   | Compliance Target |
| -------------- | ----- | ----------------------------------------------------------------- | --------------------------------------------------- | ----------------- |
| UC21-FR-01     | FR    | Khách đặt bàn với thông tin: bàn, ngày, giờ, số người | `TableReservationServiceImpl.createReservation()` | —                |
| UC21-BR-01     | BR    | Kiểm tra sức chứa bàn vs partySize                            | Capacity check                                      | TABLE-002         |
| UC21-BR-02     | BR    | Kiểm tra trùng lịch (buffer 15 phút)                          | Conflict check với existing reservations           | TABLE-003         |
| UC21-BR-03     | BR    | Xác minh lưu trú nếu khách chưa đăng nhập                | Room verification                                   | TABLE-005         |
| UC21-BR-04     | BR    | Thời gian đặt tối thiểu 30 phút                             | Time validation                                     | TABLE-006         |
| UC21-BR-05     | BR    | Chặn đặt bàn Occupied/Cleaning trong 2h tới                  | Table status check                                  | TABLE-007         |
| UC21-BR-06     | BR    | Không đặt bàn 23:00 → 08:00                                  | Business hours guard                                | TABLE-008         |
| UC21-FR-02     | FR    | Gửi email xác nhận đặt bàn thành công                     | `EmailService.sendTableBookingConfirmation()`     | —                |

---

## 3. Architecture Decision Records (ADR)

### ADR-UC21-001 — Buffer 15 phút giữa các lịch

**Quyết định:** Khi kiểm tra conflict, một lịch đặt bàn kết thúc lúc T thì lịch tiếp theo chỉ được bắt đầu từ T+15 phút. Điều này đảm bảo thời gian dọn bàn.

### ADR-UC21-002 — Guest Authentication

**Quyết định:**

- Nếu đã đăng nhập: tự động liên kết với tài khoản.
- Nếu chưa đăng nhập: yêu cầu nhập số phòng + xác minh thông tin lưu trú để đảm bảo chỉ khách đang ở tại resort mới được đặt bàn (an ninh và ưu tiên).

---

## 4. Non-Functional Requirements & SLA

| Category    | Requirement                                    | Target      | Verification Method |
| ----------- | ---------------------------------------------- | ----------- | ------------------- |
| Concurrency | Không để 2 đặt chỗ cùng bàn cùng giờ | 0% conflict | TC-UC21-03          |
| Email       | Email xác nhận gửi trong vòng 30 giây     | 100%        | E2E test            |

---

## 5. Domain Event Catalog

| Event Name                  | Publisher                       | Subscriber(s)    | Action                           |
| --------------------------- | ------------------------------- | ---------------- | -------------------------------- |
| `TableReservationCreated` | `TableReservationServiceImpl` | `EmailService` | Gửi email xác nhận đặt bàn |

---

## 6. Kịch bản Kiểm thử Chi tiết (Gherkin)

### KỊCH BẢN 1: ĐẶT BÀN THÀNH CÔNG

**TC-UC21-E2E-001**

```gherkin
Feature: UC21 — Đặt bàn trực tuyến

  Background:
    Given Bàn "T05" sức chứa 6 người, trạng thái "Available"
    And Không có lịch đặt nào cho "T05" lúc 19:00 hôm nay
    And Thời gian hiện tại là 17:00

  Scenario: Khách đặt bàn thành công
    When Khách đặt bàn "T05" lúc 19:00, 4 người
    Then Hệ thống tạo TableReservation với status = "Confirmed"
    And Khách nhận email xác nhận đặt bàn
    And HTTP 200 OK
```

### KỊCH BẢN 2: VƯỢT SỨC CHỨA (TABLE-002)

**TC-UC21-E2E-002**

```gherkin
  Scenario: Đặt 8 người cho bàn sức chứa 6
    When Khách đặt bàn "T05" với partySize = 8
    Then Hệ thống từ chối với lỗi "TABLE-002: Số khách vượt sức chứa bàn"
```

### KỊCH BẢN 3: TRÙNG LỊCH (TABLE-003)

**TC-UC21-E2E-003**

```gherkin
  Scenario: Bàn đã có lịch trong khung giờ yêu cầu
    Given Bàn "T05" đã có lịch 19:00-20:00 hôm nay
    When Khách đặt bàn "T05" lúc 19:30 (trong buffer)
    Then Hệ thống từ chối với lỗi "TABLE-003: Trùng lịch đặt bàn"
```

### KỊCH BẢN 4: ĐẶT BÀN NGOÀI GIỜ HOẠT ĐỘNG (TABLE-008)

**TC-UC21-E2E-004**

```gherkin
  Scenario: Đặt bàn lúc 23:30
    Given Thời gian hiện tại là 23:30
    When Khách cố đặt bàn
    Then Hệ thống từ chối với lỗi "TABLE-008: Không nhận đặt bàn từ 23:00 đến 08:00"
```

### KỊCH BẢN 5: KHÁCH CHƯA ĐĂNG NHẬP CẦN XÁC MINH PHÒNG (TABLE-005)

**TC-UC21-E2E-005**

```gherkin
  Scenario: Khách chưa đăng nhập đặt bàn không có booking
    Given Khách chưa đăng nhập
    When Khách nhập số phòng "999" (không có booking active)
    Then Hệ thống từ chối với lỗi "TABLE-005: Không tìm thấy thông tin lưu trú"
```

---

## 7. Đặc tả API Chi tiết

### 7.1. MODULE 3 — UC21: Đặt bàn trực tuyến

#### Endpoint Chính

| Method | Path                            | Auth Level | Required Roles | Rate Limit | Idempotent? |
| ------ | ------------------------------- | ---------- | -------------- | ---------- | ----------- |
| POST   | `/api/v1/tables/reservations` | Public     | —             | 30/min     | No          |
| GET    | `/api/v1/tables/availability` | Public     | —             | 60/min     | Yes         |

---

### 7.2. Request & Response Specification

#### [POST] Đặt bàn

```bash
POST /api/v1/tables/reservations
Content-Type: application/json
```

**Request Body:**

```json
{
  "tableId": 5,
  "reserveDate": "2026-07-10",
  "reserveTime": "19:00",
  "partySize": 4,
  "customerName": "Nguyễn Văn A",
  "roomNumber": "101",
  "specialRequests": "Cần ghế cao cho trẻ em"
}
```

**Response (200 OK):**

```json
{
  "reservationId": 3001,
  "tableNumber": "T05",
  "reserveDate": "2026-07-10",
  "reserveTime": "19:00",
  "status": "Confirmed",
  "message": "Đặt bàn thành công! Email xác nhận đã được gửi."
}
```

---

### 7.3. Error Codes & Business Rules

| Code          | HTTP Status | Message                                                 | Trigger Condition                    |
| ------------- | ----------- | ------------------------------------------------------- | ------------------------------------ |
| `TABLE-002` | 400         | `Số khách vượt sức chứa bàn`                   | partySize > table.capacity           |
| `TABLE-003` | 409         | `Trùng lịch đặt bàn trong khung giờ này`       | Conflict với reservation khác      |
| `TABLE-005` | 403         | `Chỉ khách đang lưu trú mới được đặt bàn` | Không tìm thấy booking active     |
| `TABLE-006` | 400         | `Thời gian đặt phải trước ít nhất 30 phút`   | reserveTime < now + 30min            |
| `TABLE-007` | 400         | `Bàn đang có khách hoặc đang dọn`              | Bàn Occupied/Cleaning trong 2h tới |
| `TABLE-008` | 400         | `Không nhận đặt bàn từ 23:00 đến 08:00`       | reserveTime ngoài giờ hoạt động |

---

## 8. Phương pháp Xác minh (API Verification Samples)

### 8.1. Đặt bàn thành công — cURL

```bash
curl -X POST "https://api.kawairesort.com/api/v1/tables/reservations" \
  -H "Content-Type: application/json" \
  -d '{
    "tableId": 5,
    "reserveDate": "2026-07-10",
    "reserveTime": "19:00",
    "partySize": 4,
    "roomNumber": "101"
  }'

# Expected: reservationId != null, status = "Confirmed"
```

### 8.2. Kiểm tra DB — SQL Inspection

```sql
-- Kiểm tra lịch đặt bàn
SELECT tr.id, tr.reserve_date, tr.reserve_time, tr.party_size, tr.status
FROM table_reservations tr WHERE tr.id = 3001;
-- Expected: status = 'Confirmed'

-- Kiểm tra chống trùng lịch
SELECT COUNT(*) FROM table_reservations
WHERE table_id = 5 AND reserve_date = '2026-07-10'
AND reserve_time BETWEEN '18:45' AND '20:15'
AND status NOT IN ('Cancelled');
-- Expected: Chỉ 1 reservation trong khung giờ này
```

---

## BẢNG TỔNG HỢP PHÂN QUYỀN UC21 (Authorization Matrix)

| Tác vụ                      | GUEST | CUSTOMER | F&B STAFF | ADMIN |
| ----------------------------- | ----- | -------- | --------- | ----- |
| Xem bàn trống (`GET`)     | ✅    | ✅       | ✅        | ✅    |
| Đặt bàn (`POST`)         | ❌    | ✅       | ✅        | ✅    |
| Hủy lịch đặt bàn         | ❌    | ✅       | ✅        | ✅    |
| Xem tất cả lịch đặt bàn | ❌    | ❌       | ✅        | ✅    |
