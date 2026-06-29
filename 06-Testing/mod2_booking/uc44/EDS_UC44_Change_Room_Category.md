# ENGINEERING DOCUMENTATION STANDARD (EDS) & TESTING SPECIFICATION v2.0

## UC44 — CHANGE ROOM CATEGORY (ĐỔI HẠNG PHÒNG TRONG LƯU TRÚ)

### HỆ THỐNG QUẢN LÝ NGHỈ DƯỠNG KAWAI RETREAT RESORT & HUB

| Field                    | Value                  |
| ------------------------ | ---------------------- |
| **Document ID**    | `KAWAI-EDS-UC44-001` |
| **Version**        | 1.0                    |
| **Date**           | 2026-06-29             |
| **Status**         | Approved               |
| **Document Owner** | Chu Xuân Dũng        |
| **Author**         | Chu Xuân Dũng        |
| **Reviewed by**    | Tech Lead              |
| **Based on EDS**   | KAWAI-ALL-EDS-001 v2.0 |

---

## CHANGELOG

| Ngày      | Người thực hiện | Nội dung thay đổi                                 |
| ---------- | ------------------- | ---------------------------------------------------- |
| 2026-06-29 | Antigravity AI  | Cập nhật Đặc tả API Endpoint theo thực tế triển khai (ChangeRoomController)     |
| 2026-06-29 | Chu Xuân Dũng   | Khởi tạo EDS Spec cho UC44 — Change Room Category                              |

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

| Field                         | Value                                                                   |
| ----------------------------- | ----------------------------------------------------------------------- |
| **Use Case ID**         | UC-44                                                                   |
| **Use Case Name**       | Change Room Category (Đổi hạng phòng trong lưu trú)               |
| **Module**              | Module 2 — Quản lý Phòng & Lễ tân Vận hành                      |
| **Primary Actor**       | Receptionist (Lễ tân)                                                 |
| **Secondary Actors**    | Customer (Khách hàng), System                                         |
| **Bounded Context**     | Room Inventory Management, Folio & Billing, Audit & Compliance          |
| **Data Classification** | Internal / Financial (Folio charges, Room rates)                        |
| **Compliance Scope**    | BR-FO-10, BR-FO-11, BR-FO-12, BR-FO-13, BR-FIN-08, BR-FIN-09, BR-SYS-04 |
| **Priority**            | 🔴 Critical                                                             |

### Tóm tắt nghiệp vụ

Cho phép khách đang lưu trú tại resort thực hiện yêu cầu thay đổi sang một hạng phòng khác trong thời gian lưu trú. Lễ tân kiểm tra tình trạng phòng trống, tính toán khoản điều chỉnh giá (nếu có), và cập nhật phân công phòng sau khi khách xác nhận.

**Quy tắc kinh doanh quan trọng:**

- Upgrade (hạng cao hơn) → tính phụ phí chênh lệch cho các đêm còn lại (BR-FIN-08).
- Downgrade (hạng thấp hơn) → **không hoàn tiền** (BR-FIN-09).
- Phòng mới phải có trạng thái `Vacant_Clean` (BR-FO-11, BR-FO-04).
- Booking ID được giữ nguyên — chỉ cập nhật phân công phòng vật lý.

---

## 2. Ma trận Truy vết (Traceability Matrix)

| Requirement ID | Loại | Mô tả yêu cầu                                                       | Thành phần Code                                      | Compliance Target   |
| -------------- | ----- | ----------------------------------------------------------------------- | ------------------------------------------------------ | ------------------- |
| UC44-NF-01     | FR    | Lễ tân tìm đặt phòng active của khách đang lưu trú           | `BookingService.findActiveReservation()`             | BR-FO-10            |
| UC44-NF-02     | FR    | Hệ thống hiển thị phòng trống theo hạng yêu cầu                | `RoomService.findAvailableByCategory()`              | BR-FO-11, BR-FO-04  |
| UC44-NF-03     | FR    | Hệ thống tính toán chênh lệch giá cho upgrade                    | `DynamicPricingService.calculateRateAdjustment()`    | BR-FIN-08, BR-FO-13 |
| UC44-NF-04     | FR    | Downgrade không tạo FolioItem hoàn tiền                             | `FolioService.recordRoomCategoryCharge()`            | BR-FIN-09           |
| UC44-NF-05     | FR    | Cập nhật Room status sau khi đổi (cũ → DIRTY, mới → OCCUPIED)   | `RoomStatusStateMachine`                             | BR-FO-04            |
| UC44-NF-06     | FR    | Cập nhật booking detail và folio                                     | `RoomBookingDetailRepository.save()`                 | BR-FO-12            |
| UC44-NF-07     | BR    | Ghi Audit Log mọi ca đổi phòng (thành công và lý do thất bại) | `AuditLogRepository`                                 | BR-SYS-04           |
| UC44-NF-08     | NFR   | Toàn bộ thao tác phải atomic (transaction) — rollback nếu lỗi    | `@Transactional` trên `ChangeRoomCategoryService` | Data Integrity      |
| UC44-NF-09     | BR    | Chỉ booking IN_HOUSE mới được phép đổi hạng                    | `BookingStatusValidator`                             | BR-FO-10            |
| UC44-NF-10     | NFR   | Race condition: phòng phải được lock trong quá trình xử lý     | `@Lock(PESSIMISTIC_WRITE)` trên Room query          | Data Integrity      |

---

## 3. Architecture Decision Records (ADR)

### ADR-UC44-001 — Locking Strategy cho Đổi Phòng

**Bối cảnh:** Hai lễ tân có thể cùng lúc chọn cùng một phòng `Vacant_Clean` cho 2 khách khác nhau.

**Quyết định:** Sử dụng **Pessimistic Locking** (`@Lock(LockModeType.PESSIMISTIC_WRITE)`) tại `RoomRepository.findAvailableByCategory()`. Phòng được lock trong suốt transaction đổi phòng.

**Hệ quả:**

- Đảm bảo tính toàn vẹn inventory phòng — không có double-assignment.
- Trade-off: giảm throughput khi có nhiều lễ tân thao tác đồng thời, nhưng resort không có lưu lượng siêu cao nên chấp nhận được.

### ADR-UC44-002 — Transaction Boundary

**Quyết định:** Toàn bộ logic đổi phòng (kiểm tra phòng → tính giá → cập nhật booking detail → cập nhật room status → ghi Folio → ghi Audit Log) phải nằm trong **1 database transaction** duy nhất (`@Transactional`).

**Hệ quả:** Nếu bất kỳ bước nào thất bại, toàn bộ thao tác rollback — không có partial state.

### ADR-UC44-003 — Folio Item Policy

**Quyết định:**

- Upgrade → tạo `FolioItem` với type = `ROOM_UPGRADE_CHARGE` và amount = `(newRate - oldRate) × remainingNights`.
- Downgrade → **không tạo FolioItem** nào (theo BR-FIN-09).
- Same rate → **không tạo FolioItem** nào (theo AF3).

**Hệ quả:** Cần guard condition `if (rateAdjustment > 0)` trước khi gọi `folioService.recordCharge()`.

---

## 4. Non-Functional Requirements & SLA

### 4.1. Sự Toàn vẹn Dữ liệu (Data Integrity)

| Category    | Requirement                                             | Target            | Verification Method                              |
| ----------- | ------------------------------------------------------- | ----------------- | ------------------------------------------------ |
| ACID        | Đổi phòng là 1 atomic transaction                   | RPO = 0           | `@Transactional` + TC-UC44-008 (rollback test) |
| Concurrency | Chống double room assignment                           | 0% lỗi           | Multi-thread test TC-UC44-006                    |
| Audit       | Mọi ca đổi phòng đều có Audit Log                | 100% coverage     | TC-UC44-010                                      |
| Financial   | Phụ phí upgrade tính đúng theo số đêm còn lại | ±0 VND sai lệch | TC-UC44-001, BigDecimal scale 0 HALF_UP          |

### 4.2. Hiệu năng (Performance)

| Metric         | Target                      | Verification                        |
| -------------- | --------------------------- | ----------------------------------- |
| Response time  | ≤ 2s P95                   | Kiểm tra với load thông thường |
| Concurrent ops | ≥ 10 lễ tân đồng thời | JMeter stress test                  |

---

## 5. Domain Event Catalog

Khi UC44 hoàn thành thành công, hệ thống phát ra các Domain Events sau:

| Event Name                   | Publisher                     | Subscriber(s)           | Action                                                     |
| ---------------------------- | ----------------------------- | ----------------------- | ---------------------------------------------------------- |
| `RoomCategoryChanged`      | `ChangeRoomCategoryService` | `AuditLogListener`    | Ghi Audit Log đầy đủ với old/new room info            |
| `RoomCategoryChanged`      | `ChangeRoomCategoryService` | `FolioService`        | Tạo FolioItem phụ phí upgrade (nếu rateAdjustment > 0) |
| `RoomStatusUpdated` (×2)  | `RoomStatusStateMachine`    | `HousekeepingService` | Tạo task dọn phòng cho phòng cũ (VACANT_DIRTY)        |
| `RoomCategoryChangeFailed` | `ChangeRoomCategoryService` | `AuditLogListener`    | Ghi Audit Log lý do thất bại (nếu applicable)          |

---

## 6. Kịch bản Kiểm thử Chi tiết (Gherkin)

### KỊCH BẢN 1: LUỒNG CHÍNH — ĐỒNG Ý UPGRADE VÀ THANH TOÁN PHỤ PHÍ

**TC-UC44-E2E-001**

```gherkin
Feature: UC44 — Đổi hạng phòng trong lưu trú (Change Room Category)

  Background:
    Given Hệ thống có booking "BK-2001" đang ở trạng thái "IN_HOUSE"
    And Phòng hiện tại là "R201" hạng "Deluxe" với giá 2,000,000 VND/đêm
    And Ngày check-out còn 3 đêm
    And Phòng "R301" hạng "Suite" (3,500,000 VND/đêm) đang ở trạng thái "Vacant_Clean"
    And Lễ tân đã đăng nhập với role "RECEPTIONIST"

  Scenario: Khách yêu cầu upgrade lên Suite — lễ tân xác nhận
    When Khách yêu cầu đổi sang hạng "Suite"
    And Lễ tân tìm booking của khách trên hệ thống
    Then Hệ thống hiển thị phòng "R301" còn trống ở hạng "Suite"
    And Hệ thống tính chênh lệch = (3,500,000 - 2,000,000) × 3 = 4,500,000 VND
    And Hệ thống hiển thị thông báo phụ phí 4,500,000 VND cho lễ tân
    When Khách xác nhận chấp nhận phụ phí
    And Lễ tân duyệt yêu cầu đổi phòng trên hệ thống
    Then Phòng "R201" chuyển trạng thái thành "Vacant_Dirty"
    And Phòng "R301" chuyển trạng thái thành "Occupied"
    And Bản ghi "RoomBookingDetail" được cập nhật: room_id = "R301", category = "Suite"
    And FolioItem mới được tạo với amount = 4,500,000 VND, type = "ROOM_UPGRADE_CHARGE"
    And Booking ID "BK-2001" được giữ nguyên (không tạo booking mới)
    And Audit Log được ghi với action = "ROOM_CATEGORY_CHANGED"
    And Lễ tân thấy thông báo thành công
```

### KỊCH BẢN 2: DOWNGRADE — KHÔNG HOÀN TIỀN

**TC-UC44-E2E-002**

```gherkin
  Scenario: Khách yêu cầu downgrade xuống Standard — không hoàn tiền
    Given Booking "BK-2003" IN_HOUSE, phòng "R401" hạng "Suite" (3,500,000 VND/đêm)
    And Phòng "R202" hạng "Standard" (1,500,000 VND/đêm) trạng thái "Vacant_Clean"
    When Khách yêu cầu đổi xuống hạng "Standard"
    And Lễ tân thực hiện yêu cầu và khách xác nhận
    Then Phòng "R401" chuyển thành "Vacant_Dirty"
    And Phòng "R202" chuyển thành "Occupied"
    And KHÔNG có FolioItem hoàn tiền nào được tạo
    And KHÔNG có credit nào được ghi vào Folio
    And Audit Log ghi nhận ca downgrade
    And Hệ thống hiển thị "Đổi phòng thành công. Không có khoản hoàn tiền cho việc hạ hạng phòng."
```

### KỊCH BẢN 3: KHÔNG CÒN PHÒNG TRỐNG (AF1)

**TC-UC44-E2E-003**

```gherkin
  Scenario: Không còn phòng trống ở hạng yêu cầu
    Given Không có phòng nào hạng "Suite" có trạng thái "Vacant_Clean"
    When Lễ tân chọn hạng "Suite" cho khách
    Then Hệ thống hiển thị lỗi: "No available rooms in the selected category."
    And Booking và folio của khách KHÔNG thay đổi
    And Lễ tân có thể chọn hạng phòng khác hoặc hủy yêu cầu
```

### KỊCH BẢN 4: KHÁCH TỪ CHỐI SAU KHI THẤY PHỤ PHÍ (AF2)

**TC-UC44-E2E-004**

```gherkin
  Scenario: Khách từ chối trả phụ phí sau khi xem thông tin
    Given Hệ thống đã tính phụ phí upgrade và hiển thị cho lễ tân
    When Khách quyết định không đồng ý với khoản phụ phí
    And Lễ tân hủy yêu cầu trên hệ thống
    Then Phòng tạm giữ (nếu có) được giải phóng về "Vacant_Clean"
    And Booking giữ nguyên phòng cũ
    And KHÔNG có FolioItem nào được tạo
    And Hệ thống xác nhận hủy thành công
```

### KỊCH BẢN 5: ĐỔI PHÒNG CÙNG HẠNG GIÁ (AF3)

**TC-UC44-E2E-005**

```gherkin
  Scenario: Đổi sang hạng phòng có cùng giá tiền
    Given Hạng "Deluxe River View" và hạng "Deluxe Garden View" cùng giá 2,000,000 VND/đêm
    And Phòng "R203" hạng "Deluxe Garden View" trạng thái "Vacant_Clean"
    When Khách yêu cầu chuyển từ "Deluxe River View" sang "Deluxe Garden View"
    And Lễ tân xác nhận
    Then Hệ thống tính chênh lệch = 0 VND
    And KHÔNG có FolioItem nào được tạo
    And Phòng cũ → "Vacant_Dirty", phòng mới → "Occupied"
    And Hệ thống thông báo đổi phòng thành công, không có điều chỉnh phụ phí
```

### KỊCH BẢN 6: RACE CONDITION — PHÒNG BỊ LẤY MẤT (EX1)

**TC-UC44-E2E-006**

```gherkin
  Scenario: Phòng bị lấy mất trong lúc xử lý (EX1 — Race Condition)
    Given Chỉ có 1 phòng "R302" hạng "Suite" trạng thái "Vacant_Clean"
    And Lễ tân A đang xử lý đổi phòng cho khách 1 → Suite
    When Lễ tân B đồng thời duyệt đổi phòng cho khách 2 → Suite (cùng phòng R302)
    Then Một trong hai yêu cầu thành công (assign R302)
    And Yêu cầu còn lại nhận lỗi: "Phòng đã được đặt hoặc không còn khả dụng. Vui lòng chọn phòng khác."
    And DB: Phòng R302 chỉ được gán cho đúng 1 booking
    And KHÔNG xảy ra double-assignment
```

### KỊCH BẢN 7: BOOKING KHÔNG Ở TRẠNG THÁI IN-HOUSE (EX3)

**TC-UC44-E2E-007**

```gherkin
  Scenario: Yêu cầu đổi phòng cho booking chưa check-in (EX3)
    Given Booking "BK-2004" có trạng thái "CONFIRMED" (chưa check-in)
    When Lễ tân cố gắng thực hiện đổi hạng phòng cho BK-2004
    Then Hệ thống từ chối với lỗi: "Chỉ khách đang lưu trú mới được phép thay đổi hạng phòng."
    And Không có thay đổi nào được áp dụng
```

---

## 7. Đặc tả API Chi tiết

### 7.1. MODULE 2 — UC44: Change Room Category

#### Endpoint Chính

| Method | Path                                                              | Auth Level | Required Roles                           | Rate Limit | Idempotent? |
| ------ | ----------------------------------------------------------------- | ---------- | ---------------------------------------- | ---------- | ----------- |
| GET    | `/receptionist/in-house/categories-available`                     | Protected  | OP_BOOKING, ROLE_ADMIN, ROLE_MANAGER     | 100/min    | Yes         |
| GET    | `/receptionist/in-house/rooms-by-category`                        | Protected  | OP_BOOKING, ROLE_ADMIN, ROLE_MANAGER     | 100/min    | Yes         |
| POST   | `/receptionist/in-house/transfer-room`                            | Protected  | OP_BOOKING, ROLE_ADMIN, ROLE_MANAGER     | 60/min     | No          |

#### Authorization Matrix

| Tác vụ / Endpoint                      | GUEST | CUSTOMER | RECEPTIONIST | F&B STAFF | ADMIN / MANAGER |
| ---------------------------------------- | ----- | -------- | ------------ | --------- | --------------- |
| Xem danh sách hạng phòng (`GET`)        | ❌    | ❌       | ✅           | ❌        | ✅              |
| Xem phòng trống theo hạng (`GET`)       | ❌    | ❌       | ✅           | ❌        | ✅              |
| Đổi hạng phòng (`POST`)                 | ❌    | ❌       | ✅           | ❌        | ✅              |

---

### 7.2. Request & Response Specification

#### [GET] Xem danh sách hạng phòng và chênh lệch giá

```bash
GET /receptionist/in-house/categories-available?excludeDetailId={id}
Authorization: Bearer [TOKEN]
```

**Response (200 OK):**

```json
[
  {
    "categoryName": "Deluxe",
    "basePrice": 2000000.00,
    "vacantCount": 5,
    "isCurrent": true,
    "priceDiff": 0.00
  },
  {
    "categoryName": "Suite",
    "basePrice": 3500000.00,
    "vacantCount": 2,
    "isCurrent": false,
    "priceDiff": 1500000.00
  }
]
```

---

#### [GET] Xem phòng trống theo hạng

```bash
GET /receptionist/in-house/rooms-by-category?categoryName=Suite
Authorization: Bearer [TOKEN]
```

**Response (200 OK):**

```json
[
  {
    "id": 301,
    "roomNumber": "R301"
  },
  {
    "id": 302,
    "roomNumber": "R302"
  }
]
```

---

#### [POST] Thực hiện đổi hạng phòng

```bash
POST /receptionist/in-house/transfer-room
Authorization: Bearer [TOKEN]
Content-Type: application/x-www-form-urlencoded
```

**Request Params:**

- `bookingDetailId` (Long): ID của RoomBookingDetail hiện tại
- `newRoomId` (Long): ID của phòng mới được chọn

**Response (200 OK — Thành công):**

```json
{
  "success": true,
  "message": "Đổi phòng thành công!"
}
```

**Response (400 Bad Request — Lỗi nghiệp vụ):**

```json
{
  "success": false,
  "message": "Lỗi: Chỉ khách đang lưu trú mới được phép thay đổi hạng phòng."
}
```

---

### 7.3. Error Codes & Business Rules

| Code / Rule    | HTTP Status | Message                                                                                    | Trigger Condition                                                  |
| -------------- | ----------- | ------------------------------------------------------------------------------------------ | ------------------------------------------------------------------ |
| `BR-FO-10`   | 400         | `Chỉ khách đang lưu trú mới được phép thay đổi hạng phòng.`                | Booking không ở trạng thái`IN_HOUSE`                         |
| `BR-FO-11`   | 200         | `No available rooms in the selected category.`                                           | Không có phòng`Vacant_Clean` ở hạng yêu cầu               |
| `ERR-RC-001` | 409         | `Phòng đã được đặt hoặc không còn khả dụng. Vui lòng chọn phòng khác.`  | Race condition — phòng bị chiếm trước khi confirm            |
| `ERR-RC-002` | 500         | `Có lỗi xảy ra trong quá trình cập nhật dữ liệu. Vui lòng thử lại sau.`      | Lỗi DB / Transaction rollback (EX2)                               |
| `ERR-RC-003` | 503         | `Không thể tính chênh lệch giá phòng. Vui lòng liên hệ quản trị hệ thống.` | Lỗi DynamicPricingService (EX4)                                   |
| `ERR-RC-004` | 404         | `Booking không tồn tại hoặc không có quyền truy cập.`                            | bookingDetailId không tồn tại hoặc không thuộc về lễ tân  |
| `ERR-RC-005` | 400         | `Yêu cầu đổi phòng không hợp lệ: thiếu thông tin bắt buộc.`                  | Request body thiếu`targetCategoryName` hoặc `selectedRoomId` |

---

## 8. Phương pháp Xác minh (API Verification Samples)

### 8.1. Xác minh Upgrade thành công — cURL

```bash
# Bước 1: Xem phòng trống hạng Suite
curl -X GET "https://api.kawairesort.com/api/v1/reception/rooms/available-by-category?categoryName=Suite" \
  -H "Authorization: Bearer [RECEPTIONIST_TOKEN]"

# Expected: Trả về danh sách phòng Suite còn trống

# Bước 2: Thực hiện đổi phòng (Upgrade)
curl -X POST "https://api.kawairesort.com/api/v1/reception/bookings/5001/change-room" \
  -H "Authorization: Bearer [RECEPTIONIST_TOKEN]" \
  -H "Content-Type: application/json" \
  -d '{
    "targetCategoryName": "Suite",
    "selectedRoomId": 301,
    "guestConfirmed": true,
    "receptionistNote": "Khách yêu cầu upgrade"
  }'

# Expected Response (200 OK):
# - previousRoom.newStatus = "Vacant_Dirty"
# - newRoom.newStatus = "Occupied"
# - folioItem.amount = 4500000
# - auditLogId != null
```

### 8.2. Xác minh DB sau đổi phòng — SQL Inspection

```sql
-- Kiểm tra trạng thái phòng sau đổi
SELECT room_number, room_status
FROM rooms
WHERE room_number IN ('R201', 'R301');
-- Expected:
-- R201 → Vacant_Dirty
-- R301 → Occupied

-- Kiểm tra RoomBookingDetail đã update
SELECT rbd.room_id, rbd.category_id, r.room_number
FROM room_booking_details rbd
JOIN rooms r ON rbd.room_id = r.room_id
WHERE rbd.id = 5001;
-- Expected: room_number = 'R301' (đã đổi sang phòng mới)

-- Kiểm tra FolioItem phụ phí upgrade
SELECT fi.amount, fi.description, fi.item_type
FROM folio_items fi
WHERE fi.booking_id = (SELECT booking_id FROM room_booking_details WHERE id = 5001)
  AND fi.item_type = 'ROOM_UPGRADE_CHARGE';
-- Expected: amount = 4500000 VND

-- Kiểm tra KHÔNG có FolioItem khi downgrade
SELECT COUNT(*) FROM folio_items
WHERE booking_id = [BK-2003_ID]
  AND item_type IN ('ROOM_UPGRADE_CHARGE', 'CREDIT');
-- Expected: COUNT = 0

-- Kiểm tra Audit Log
SELECT al.action, al.actor_id, al.old_value, al.new_value, al.timestamp
FROM audit_logs al
WHERE al.action = 'ROOM_CATEGORY_CHANGED'
ORDER BY al.timestamp DESC LIMIT 5;
-- Expected: Có bản ghi với đầy đủ old_room, new_room, actor_id, timestamp

-- Kiểm tra chống double-assignment (sau race condition test)
SELECT room_id, COUNT(*) AS assignment_count
FROM room_booking_details
WHERE room_id = 301
  AND detail_status = 'ACTIVE'
GROUP BY room_id HAVING assignment_count > 1;
-- Expected: Empty result (không có double-assignment)
```

### 8.3. Kiểm tra Downgrade không hoàn tiền — cURL

```bash
# Thực hiện downgrade
curl -X POST "https://api.kawairesort.com/api/v1/reception/bookings/5003/change-room" \
  -H "Authorization: Bearer [RECEPTIONIST_TOKEN]" \
  -H "Content-Type: application/json" \
  -d '{
    "targetCategoryName": "Standard",
    "selectedRoomId": 202,
    "guestConfirmed": true
  }'

# Expected Response (200 OK):
# - rateAdjustment.type = "NO_CHARGE"
# - folioItem = null
# - message chứa "không có khoản hoàn tiền"
```

### 8.4. Kiểm tra Booking không IN-HOUSE — cURL

```bash
# Thử đổi phòng cho booking chưa check-in
curl -X POST "https://api.kawairesort.com/api/v1/reception/bookings/9999/change-room" \
  -H "Authorization: Bearer [RECEPTIONIST_TOKEN]" \
  -H "Content-Type: application/json" \
  -d '{
    "targetCategoryName": "Suite",
    "selectedRoomId": 301,
    "guestConfirmed": true
  }'

# Expected Response (400 Bad Request):
# {
#   "error": "ERR-RC-BR-FO-10",
#   "message": "Chỉ khách đang lưu trú mới được phép thay đổi hạng phòng."
# }
```

---

## BẢNG TỔNG HỢP PHÂN QUYỀN UC44 (Authorization Matrix)

| Tác vụ / Endpoint                      | GUEST | CUSTOMER | RECEPTIONIST | F&B STAFF | ADMIN / MANAGER |
| ---------------------------------------- | ----- | -------- | ------------ | --------- | --------------- |
| Xem phòng trống theo hạng (`GET`)   | ❌    | ❌       | ✅           | ❌        | ✅              |
| Yêu cầu đổi hạng phòng (`POST`)  | ❌    | ❌       | ✅           | ❌        | ✅              |
| Hủy yêu cầu đổi phòng (`DELETE`) | ❌    | ❌       | ✅           | ❌        | ✅              |
| Xem Audit Log đổi phòng               | ❌    | ❌       | ❌           | ❌        | ✅ (Chỉ Admin) |
