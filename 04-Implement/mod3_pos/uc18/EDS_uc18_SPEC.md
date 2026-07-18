# ENGINEERING DOCUMENTATION STANDARD (EDS) & TESTING SPECIFICATION v2.0

## UC18 — CHỐT CA / BÁO CÁO DOANH THU NGÀY F&B (F&B DAILY REPORT)

### HỆ THỐNG QUẢN LÝ NGHỈ DƯỠNG KAWAI RETREAT RESORT & HUB

| Field                    | Value                  |
| ------------------------ | ---------------------- |
| **Document ID**    | `KAWAI-EDS-UC18-001` |
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
| 2026-07-02 | Trịnh Minh Đức   | Khởi tạo EDS Spec cho UC18 — Chốt ca báo cáo doanh thu F&B |

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

| Field                         | Value                                                       |
| ----------------------------- | ----------------------------------------------------------- |
| **Use Case ID**         | UC-18                                                       |
| **Use Case Name**       | Chốt ca / Báo cáo doanh thu ngày F&B (F&B Daily Report) |
| **Module**              | Module 3 — POS & Nhà hàng                                |
| **Primary Actor**       | F&B Staff / Manager                                         |
| **Secondary Actors**    | System, Admin                                               |
| **Bounded Context**     | F&B Revenue Reporting, Shift Management                     |
| **Data Classification** | Internal / Financial (Revenue snapshot)                     |
| **Compliance Scope**    | BR-FIN-15, BR-SYS-04                                        |
| **Priority**            | 🟡 High                                                     |

### Tóm tắt nghiệp vụ

Cuối ca làm việc, nhân viên F&B thực hiện chốt ca: xem preview doanh thu, kiểm tra số liệu, và lưu snapshot báo cáo vào DB. Hệ thống tổng hợp doanh thu theo phương thức thanh toán (Tiền mặt / VNPAY / Ghi nợ phòng) và ngăn chốt nhiều lần trong cùng ngày.

**Quy tắc kinh doanh quan trọng:**

- Chỉ tính đơn đã hoàn tất tài chính: `isPaidInPos = true` VÀ status trong {`Completed`, `Paid`, `Served`}.
- **Ngăn chốt nhiều lần cùng ngày:** Kiểm tra xem đã có báo cáo cho ngày hôm nay chưa.
- Liệt kê toàn bộ bill trong ngày (kể cả Cancelled) để đối soát.
- Ghi chú của nhân viên (thiếu tiền két, sự cố...) được lưu vào DB.

---

## 2. Ma trận Truy vết (Traceability Matrix)

| Requirement ID | Loại | Mô tả yêu cầu                                     | Thành phần Code                                | Compliance Target |
| -------------- | ----- | ----------------------------------------------------- | ------------------------------------------------ | ----------------- |
| UC18-FR-01     | FR    | Xem preview doanh thu trước khi chốt               | `FnBDailyReportServiceImpl.previewReport()`    | —                |
| UC18-FR-02     | FR    | Tổng hợp doanh thu theo phương thức thanh toán  | `FnBDailyReportServiceImpl.calculateRevenue()` | —                |
| UC18-FR-03     | BR    | Chỉ tính đơn isPaidInPos=true và status hợp lệ | Filter logic trong service                       | —                |
| UC18-FR-04     | FR    | Chốt ngày — lưu snapshot vào Fnb_Daily_Reports   | `FnBDailyReportRepository.save()`              | —                |
| UC18-BR-01     | BR    | Ngăn chốt nhiều lần cùng ngày                   | Kiểm tra existsByDate trong service             | —                |
| UC18-FR-05     | FR    | Lưu ghi chú nhân viên vào báo cáo              | `FnBDailyReport.staffNote`                     | —                |
| UC18-FR-06     | FR    | Liệt kê tất cả bill trong ngày để đối soát  | `FoodOrderRepository.findByDate()`             | BR-SYS-04         |

---

## 3. Architecture Decision Records (ADR)

### ADR-UC18-001 — Snapshot vs Live Query

**Bối cảnh:** Báo cáo doanh thu có thể được tra cứu lại sau khi chốt ca. Dữ liệu live có thể thay đổi.

**Quyết định:** Lưu **Snapshot** tại thời điểm chốt ca vào bảng `Fnb_Daily_Reports`. Không dùng live query khi tra cứu báo cáo lịch sử.

**Hệ quả:** Dữ liệu báo cáo bất biến — không bị ảnh hưởng bởi chỉnh sửa đơn hàng sau đó.

### ADR-UC18-002 — Idempotency Guard

**Quyết định:** Kiểm tra `existsByReportDate(today)` trước khi cho phép chốt. Nếu đã tồn tại báo cáo ngày hôm nay → từ chối với message rõ ràng.

---

## 4. Non-Functional Requirements & SLA

### 4.1. Sự Toàn vẹn Dữ liệu (Data Integrity)

| Category   | Requirement                                                        | Target       | Verification Method |
| ---------- | ------------------------------------------------------------------ | ------------ | ------------------- |
| Financial  | Doanh thu tính đúng theo filter isPaidInPos                     | ±0 VND      | TC-UC18-01          |
| Idempotent | Không thể chốt 2 lần cùng ngày                               | 100% blocked | TC-UC18-03          |
| Audit      | Snapshot được lưu đủ thông tin (ngày, doanh thu, ghi chú) | 100%         | TC-UC18-02          |

---

## 5. Domain Event Catalog

| Event Name            | Publisher                     | Subscriber(s) | Action                 |
| --------------------- | ----------------------------- | ------------- | ---------------------- |
| `DailyReportClosed` | `FnBDailyReportServiceImpl` | `AuditLog`  | Ghi Audit Log ca chốt |

---

## 6. Kịch bản Kiểm thử Chi tiết (Gherkin)

### KỊCH BẢN 1: PREVIEW DOANH THU

**TC-UC18-E2E-001**

```gherkin
Feature: UC18 — Chốt ca F&B Daily Report

  Background:
    Given Ngày hôm nay có 5 đơn hàng:
      - ORD-001: Dine-In, Cash, isPaidInPos=true, status=Completed, total=200,000
      - ORD-002: Dine-In, VNPAY, isPaidInPos=true, status=Paid, total=350,000
      - ORD-003: Room Service, Charge-to-Room, isPaidInPos=true, status=Served, total=105,000
      - ORD-004: Dine-In, Cash, isPaidInPos=false (chưa thanh toán)
      - ORD-005: Dine-In, Cash, isPaidInPos=true, status=Cancelled

  Scenario: Xem preview doanh thu trước khi chốt
    When F&B Staff gọi API preview báo cáo ngày hôm nay
    Then Hệ thống chỉ tính ORD-001, ORD-002, ORD-003 (filter đúng)
    And Tổng doanh thu = 655,000 VND
    And Phân loại: Cash = 200,000 | VNPAY = 350,000 | Room Charge = 105,000
    And ORD-004 và ORD-005 KHÔNG được tính vào doanh thu
    And Danh sách đầy đủ 5 đơn (kể cả Cancelled) hiển thị để đối soát
```

### KỊCH BẢN 2: CHỐT CA THÀNH CÔNG

**TC-UC18-E2E-002**

```gherkin
  Scenario: Chốt ca — lưu snapshot thành công
    When F&B Staff gọi API chốt ca với ghi chú "Thiếu 50,000 VND tiền lẻ"
    Then Hệ thống lưu FnBDailyReport với:
      - reportDate = today
      - totalRevenue = 655,000 VND
      - staffNote = "Thiếu 50,000 VND tiền lẻ"
    And HTTP 200 OK
    And Báo cáo hiển thị trong lịch sử
```

### KỊCH BẢN 3: NGĂN CHỐT LẦN 2 CÙNG NGÀY

**TC-UC18-E2E-003**

```gherkin
  Scenario: Cố chốt ca lần thứ 2 trong ngày
    Given Đã có báo cáo cho ngày hôm nay
    When F&B Staff cố chốt ca lần thứ 2
    Then Hệ thống từ chối với lỗi "Báo cáo ngày hôm nay đã được chốt"
    And Không có báo cáo mới nào được tạo
```

---

## 7. Đặc tả API Chi tiết

### 7.1. MODULE 3 — UC18: F&B Daily Report

#### Endpoints Chính

| Method | Path                                  | Auth Level | Required Roles            | Rate Limit | Idempotent? |
| ------ | ------------------------------------- | ---------- | ------------------------- | ---------- | ----------- |
| GET    | `/api/v1/fnb/daily-reports/preview` | Protected  | ROLE_FB_STAFF, ROLE_ADMIN | 10/min     | Yes         |
| POST   | `/api/v1/fnb/daily-reports/close`   | Protected  | ROLE_FB_STAFF, ROLE_ADMIN | 5/min      | No          |

#### Authorization Matrix

| Tác vụ / Endpoint             | GUEST | CUSTOMER | F&B STAFF | ADMIN / MANAGER |
| ------------------------------- | ----- | -------- | --------- | --------------- |
| Xem preview báo cáo (`GET`) | ❌    | ❌       | ✅        | ✅              |
| Chốt ca (`POST`)             | ❌    | ❌       | ✅        | ✅              |

---

### 7.2. Request & Response Specification

#### [GET] Preview doanh thu

```bash
GET /api/v1/fnb/daily-reports/preview?date=2026-07-02
Authorization: Bearer [FB_STAFF_TOKEN]
```

**Response (200 OK):**

```json
{
  "reportDate": "2026-07-02",
  "totalRevenue": 655000,
  "cashRevenue": 200000,
  "vnpayRevenue": 350000,
  "roomChargeRevenue": 105000,
  "totalOrders": 3,
  "cancelledOrders": 1,
  "allBills": [...]
}
```

#### [POST] Chốt ca

```bash
POST /api/v1/fnb/daily-reports/close
Authorization: Bearer [FB_STAFF_TOKEN]
Content-Type: application/json
```

**Request Body:**

```json
{
  "reportDate": "2026-07-02",
  "staffNote": "Thiếu 50,000 VND tiền lẻ"
}
```

**Response (200 OK):**

```json
{
  "success": true,
  "reportId": 501,
  "message": "Chốt ca thành công"
}
```

---

### 7.3. Error Codes & Business Rules

| Code            | HTTP Status | Message                                         | Trigger Condition              |
| --------------- | ----------- | ----------------------------------------------- | ------------------------------ |
| `FNB-RPT-001` | 409         | `Báo cáo ngày hôm nay đã được chốt` | Đã có report cho ngày đó |
| `FNB-RPT-002` | 400         | `Ngày báo cáo không hợp lệ`             | reportDate trong tương lai   |

---

## 8. Phương pháp Xác minh (API Verification Samples)

### 8.1. Preview trước khi chốt — cURL

```bash
curl -X GET "https://api.kawairesort.com/api/v1/fnb/daily-reports/preview?date=2026-07-02" \
  -H "Authorization: Bearer [FB_STAFF_TOKEN]"
# Expected: Tổng doanh thu đúng với filter isPaidInPos=true
```

### 8.2. Chốt ca — cURL

```bash
curl -X POST "https://api.kawairesort.com/api/v1/fnb/daily-reports/close" \
  -H "Authorization: Bearer [FB_STAFF_TOKEN]" \
  -H "Content-Type: application/json" \
  -d '{ "reportDate": "2026-07-02", "staffNote": "Ca bình thường" }'
# Expected: reportId != null, success = true
```

### 8.3. Kiểm tra DB — SQL Inspection

```sql
-- Kiểm tra báo cáo đã chốt
SELECT fdr.report_date, fdr.total_revenue, fdr.staff_note
FROM fnb_daily_reports fdr
WHERE fdr.report_date = '2026-07-02';
-- Expected: 1 row với đúng total_revenue

-- Kiểm tra chống chốt nhiều lần
SELECT COUNT(*) FROM fnb_daily_reports WHERE report_date = '2026-07-02';
-- Expected: COUNT = 1 (chỉ 1 báo cáo/ngày)
```

---

## BẢNG TỔNG HỢP PHÂN QUYỀN UC18 (Authorization Matrix)

| Tác vụ                  | GUEST | CUSTOMER | F&B STAFF | ADMIN / MANAGER |
| ------------------------- | ----- | -------- | --------- | --------------- |
| Xem preview báo cáo     | ❌    | ❌       | ✅        | ✅              |
| Chốt ca / Lưu báo cáo | ❌    | ❌       | ✅        | ✅              |
| Xem lịch sử báo cáo   | ❌    | ❌       | ✅        | ✅              |
