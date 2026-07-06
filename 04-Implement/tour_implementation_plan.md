# KẾ HOẠCH THỰC THI CODE LUỒNG ĐẶT TOUR VÀ ĐIỂM DANH AI FACEID (MODULE 4)

## CHUẨN FULLSTACK MVC & TDD SPECIFICATION

| Field         | Value                                                          |
| ------------- | -------------------------------------------------------------- |
| Document ID   | KAWAI-MOD4-IMP-001                                             |
| Version       | 1.0                                                            |
| Date          | 2026-06-29                                                     |
| Status        | **APPROVED**                                             |
| Deciders      | Nhóm Phát Triển SWP391 - G2, Antigravity (Architect Review) |
| Standard      | ISO/IEC/IEEE 29119-3:2021 & Standard EDS v2.0                  |
| Target Folder | `04-Implement`                                               |

---

## CHANGELOG

| Ngày      | Người thực hiện | Nội dung thay đổi                                                                                              |
| ---------- | ------------------- | ----------------------------------------------------------------------------------------------------------------- |
| 2026-06-29 | Antigravity         | Thiết lập tài liệu kế hoạch thực thi tích hợp Module 4 (Tour & FaceID) theo tiêu chuẩn MVC, EDS & TDD. |

---

## MỤC LỤC

1. [Tổng quan Luồng Nghiệp vụ (System Overview)](#1-tong-quan)
2. [Ma trận Truy vết Yêu cầu (Traceability Matrix)](#2-traceability)
3. [Thiết kế Kiến trúc MVC (MVC Architecture Spec)](#3-mvc)
4. [Đặc tả API Chi tiết &amp; Database Trigger (Engineering Spec)](#4-engineering)
5. [Đặc tả Kịch bản Kiểm thử TDD (TDD Test Specification)](#5-tdd)
6. [Kế hoạch Triển khai Chi tiết (Implementation Roadmap)](#6-roadmap)

---

<a id="1-tong-quan"></a>

## 1. Tổng quan Luồng Nghiệp vụ (System Overview)

Module 4 (Quản lý Lữ hành & Đánh giá) đảm nhận vai trò quản lý vòng đời dịch vụ du lịch gia tăng của khách lưu trú hoặc khách lẻ vãng lai tại resort. Luồng nghiệp vụ cốt lõi gồm:

- **Tìm kiếm & Đặt Tour:** Khách hàng tìm kiếm lịch trình chuyến xe tour, xem dự báo thời tiết real-time (tích hợp Weather API), thực hiện đặt chỗ với số lượng ghế mong muốn và thực hiện thanh toán trực tuyến (VNPay) hoặc ký nợ phòng (Post-to-Room).
- **Điểm danh hành khách bằng AI Face Scan:** Khi xe tour xuất phát, Hướng dẫn viên sử dụng thiết bị Ops để quét ảnh chân dung khách hàng, hệ thống gửi ảnh đến Microservice Python để so khớp đặc trưng khuôn mặt (128-dim embedding) qua khoảng cách Cosine Similarity.
- **Điểm danh thủ công Fallback:** Cho phép Hướng dẫn viên xác thực và điểm danh thủ công nếu cơ chế quét AI gặp sự cố kỹ thuật hoặc ảnh chụp có độ tin cậy < 85%.
- **Chốt tối thiểu chuyến (T-24h):** Tiến trình tự động quét và kiểm duyệt số chỗ đặt. Nếu không đạt số khách tối thiểu (`minimumPax`), hệ thống tự động hủy tour và hoàn trả 100% tiền cọc cho khách hàng.

---

<a id="2-traceability"></a>

## 2. Ma trận Truy vết Yêu cầu (Traceability Matrix)

| Requirement ID | UC ID | Business Rule | Code Component (JPA Entity / Service)                                 | View Template             | Trạng thái              |
| -------------- | ----- | ------------- | --------------------------------------------------------------------- | ------------------------- | ------------------------- |
| FR-TOUR-01     | UC20  | —            | `TourScheduleRepository.findByDepartureDate` / `WeatherApiClient` | `guest/tours.html`      | ✅ Code có sẵn          |
| FR-TOUR-02     | UC20  | —            | `WeatherApiClient.getWeatherForecast()`                             | `tour/tour-detail.html` | ✅ Code có sẵn          |
| FR-TOUR-03     | UC21  | BR-TR-01      | `TourBookingServiceImpl.bookTour()`                                 | `guest/booking.html`    | ✅ Code có sẵn          |
| FR-TOUR-04     | UC21  | BR-TR-01      | Trigger`TRG_Tour_Capacity_Validator`                                | —                        | ✅ Code có sẵn          |
| FR-TOUR-05     | UC21  | BR-FB-01      | `PosServiceImpl.chargeToRoom()` / `VnPayServiceImpl`              | `guest/booking.html`    | ✅ Code có sẵn          |
| FR-TOUR-09     | UC22  | BR-TR-05      | `@Scheduled` job `minimum_pax_check` T-24h                        | —                        | ⚠️ Chưa đồng bộ     |
| FR-AI-01       | UC22  | BR-TR-02      | `FaceIdApiController.scanFace()`                                    | `tour/FaceID.html`      | ⚠️ Demo mock data       |
| FR-AI-02       | UC22  | BR-TR-02      | `PythonAiServiceClient.compareFaces()`                              | `tour/FaceID.html`      | ⚠️ Chưa nối API thật |
| FR-AI-04       | UC22  | BR-TR-02      | `TourAttendanceRestController.manualCheckIn()`                      | `tour/Tour.html`        | ✅ Code có sẵn          |

---

<a id="3-mvc"></a>

## 3. Thiết kế Kiến trúc MVC (MVC Architecture Spec)

### 3.1 Tầng Model (M)

#### A. Các JPA Entities & Ánh xạ CSDL

Mọi thực thể của Module 4 được lưu tại package `com.kawai.models` và ánh xạ trực tiếp đến bảng cơ sở dữ liệu MySQL tương ứng:

- **`Tour` (@Entity):** Chứa thông tin gốc của tour (tên, giá cơ sở, độ khó, mô tả).
- **`TourSchedule` (@Entity):** Lưu lịch khởi hành cụ thể (ngày đi, giờ đi, số chỗ tối đa `maxCapacity`, số chỗ tối thiểu `minimumPax`, số chỗ đã bán `bookedSeats`, trạng thái `status`).
- **`TourBooking` (@Entity):** Kế thừa lớp `Booking` tổng thể, lưu trữ liên kết với `TourSchedule`, số khách tham gia (`participantCount`) và tổng tiền đặt tour (`tourCharge`).
- **`TourAttendee` (@Entity):** Lưu danh sách hành khách đi tour, liên kết với `Customer` hoặc `Dependent`, trạng thái điểm danh (`attendanceStatus`), vector khuôn mặt (`faceVectorData`) và mốc thời gian điểm danh thành công (`faceMatchedAt`).
- **`CheckpointAttendance` (@Entity):** Điểm danh chi tiết tại từng điểm dừng chân của lịch trình.

#### B. Cơ chế Concurrency Lock tại Repository

Để ngăn chặn tình trạng đặt vé tour vượt quá sức chứa tối đa của chuyến xe (`maxCapacity`) dưới áp lực nhiều request đồng thời, ta định nghĩa cơ chế **Lock bi quan (Pessimistic Write Lock)** tại `TourScheduleRepository.java`:

```java
package com.kawai.repositories;

import com.kawai.models.TourSchedule;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface TourScheduleRepository extends JpaRepository<TourSchedule, Long> {
  
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ts FROM TourSchedule ts WHERE ts.id = :id")
    Optional<TourSchedule> findByIdWithLock(@Param("id") Long id);
}
```

---

### 3.2 Tầng Controller (C)

Tầng Controller chịu trách nhiệm định tuyến view Thymeleaf cho các tác vụ của Hướng dẫn viên và xử lý các API RESTful bất đồng bộ.

#### A. Web Controllers (`com.kawai.controllers.web`)

- **`TourGuideController.java`:**
  - `GET /tourguide/dashboard`: Trả về giao diện dashboard của HDV (`tour/Tour.html`).
  - `GET /tourguide/face-scan?scheduleId={id}`: Trả về giao diện quét camera FaceID (`tour/FaceID.html`).
- **`TourController.java`:**
  - `GET /tours`: Hiển thị danh sách các gói tour.
  - `GET /tours/detail?id={id}`: Hiển thị trang chi tiết tour và dự báo thời tiết (`tour/tour-detail.html`).

#### B. REST API Controllers (`com.kawai.controllers.api`)

- **`TourBookingApiController.java`:**
  - `POST /api/tour-bookings`: Tạo mới đơn đặt tour, kiểm tra dung lượng ghế trống, tính toán phụ phí.
- **`FaceIdApiController.java`:**
  - `POST /api/faceid/scan`: Tiếp nhận tệp ảnh chụp camera từ client gửi lên, chuyển tiếp đến Python AI service, nhận kết quả và ghi nhận điểm danh.
- **`TourAttendanceRestController.java`:**
  - `POST /api/v1/tour-attendance/{attendeeId}/manual`: API ghi nhận điểm danh thủ công khi quét AI thất bại.

---

### 3.3 Tầng View (V)

- **`tour/Tour.html` (Thymeleaf):**
  - Chứa danh sách hành khách đi tour theo lịch trình được chọn.
  - Tích hợp gọi API `/api/v1/tour-attendance/{id}/manual` qua Fetch API để cập nhật nhanh trạng thái điểm danh lên xe của khách mà không cần reload trang.
- **`tour/FaceID.html` (Thymeleaf):**
  - Sử dụng API WebRTC (`navigator.mediaDevices.getUserMedia`) mở webcam trên trình duyệt của HDV để chụp ảnh khách hàng thời gian thực.
  - Gửi ảnh qua đối tượng `FormData` lên endpoint `/api/faceid/scan` để thực hiện đối khớp vector.

---

<a id="4-engineering"></a>

## 4. Đặc tả API Chi tiết & Database Trigger (Engineering Spec)

### 4.1. API Specification

#### A. Endpoint: POST `/api/tour-bookings`

- **Mô tả:** Đăng ký đặt tour cho nhóm khách lưu trú.
- **Request Body:**

```json
{
  "scheduleId": 105,
  "roomBookingDetailId": 240,
  "participantCount": 3,
  "attendees": [
    { "customerId": 12, "dependentId": null },
    { "customerId": null, "dependentId": 5 },
    { "customerId": null, "dependentId": 6 }
  ],
  "paymentMethod": "CHARGE_TO_ROOM",
  "personalPin": "8888"
}
```

- **Response — 200 OK (Happy Path):**

```json
{
  "status": "SUCCESS",
  "bookingId": 4012,
  "totalCharge": 450000.00,
  "message": "Đã đặt tour thành công và ký nợ vào phòng."
}
```

- **Response — 409 Conflict (Overbooking / Hết chỗ):**

```json
{
  "errorCode": "TOUR-001",
  "message": "Không đủ chỗ trống. Số chỗ còn lại: 1. Số lượng đăng ký: 3."
}
```

#### B. Endpoint: POST `/api/faceid/scan`

- **Mô tả:** Chụp ảnh hành khách tại cửa xe và kiểm tra tính hợp lệ bằng AI.
- **Request Parameters (Multipart Form-Data):**
  - `image`: File ảnh chụp (Binary)
  - `attendeeId`: Long (Mã hành khách cần so khớp)
- **Response — 200 OK (Khớp khuôn mặt >= 85%):**

```json
{
  "match": true,
  "confidence": 0.912,
  "attendeeName": "Nguyễn Văn A",
  "attendanceStatus": "Boarded",
  "message": "Điểm danh FaceID thành công."
}
```

- **Response — 200 OK (Không khớp khuôn mặt / < 85%):**

```json
{
  "match": false,
  "confidence": 0.620,
  "attendeeName": "Nguyễn Văn A",
  "attendanceStatus": "Unmatched",
  "message": "Khuôn mặt không khớp với cơ sở dữ liệu. Vui lòng chuyển sang điểm danh thủ công."
}
```

---

### 4.2. Database Triggers (Bảo vệ toàn vẹn dữ liệu)

Để đảm bảo tính toàn vẹn tuyệt đối ở mức cơ sở dữ liệu, Trigger `TRG_Tour_Capacity_Validator` sẽ kiểm tra số ghế của chuyến xe trước khi thêm bản ghi đặt tour mới:

```sql
DELIMITER //

CREATE TRIGGER TRG_Tour_Capacity_Validator
BEFORE INSERT ON Tour_Bookings
FOR EACH ROW
BEGIN
    DECLARE v_max_capacity INT;
    DECLARE v_booked_seats INT;
  
    -- Lấy thông tin số chỗ từ bảng Tour_Schedules
    SELECT max_capacity, booked_seats 
    INTO v_max_capacity, v_booked_seats
    FROM Tour_Schedules
    WHERE schedule_id = NEW.schedule_id;
  
    -- Kiểm tra nếu số chỗ đặt mới vượt quá chỗ còn trống
    IF (v_booked_seats + NEW.participant_count) > v_max_capacity THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'TOUR_OVERBOOKING_ERROR: Số lượng đăng ký vượt quá sức chứa tối đa của xe tour!';
    END IF;
END;
//

DELIMITER ;
```

---

### 4.3. Cronjob T-24h (Kiểm tra điều kiện tối thiểu khởi hành)

Sử dụng Spring Task Scheduler chạy ngầm định kỳ quét các chuyến tour khởi hành trong vòng 24 giờ tới:

```java
package com.kawai.services.jobs;

import com.kawai.models.TourSchedule;
import com.kawai.repositories.TourScheduleRepository;
import com.kawai.services.interfaces.TourBookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class TourMinimumPaxJob {

    private final TourScheduleRepository scheduleRepo;
    private final TourBookingService tourBookingService;

    @Scheduled(cron = "0 0 * * * *") // Chạy mỗi giờ
    @Transactional
    public void checkMinimumPax() {
        LocalDateTime deadline = LocalDateTime.now().plusHours(24);
        List<TourSchedule> upcomingTours = scheduleRepo.findUpcomingPendingSchedules(deadline);

        for (TourSchedule schedule : upcomingTours) {
            if (schedule.getBookedSeats() < schedule.getMinimumPax()) {
                log.warn("Tour schedule {} has only {} booked seats (min {}). Triggering auto-cancellation.",
                        schedule.getId(), schedule.getBookedSeats(), schedule.getMinimumPax());
                try {
                    tourBookingService.cancelTourScheduleBySystem(schedule.getId(), "Không đủ số khách tối thiểu để khởi hành");
                } catch (Exception e) {
                    log.error("Failed to cancel tour schedule {}: {}", schedule.getId(), e.getMessage());
                }
            }
        }
    }
}
```

---

<a id="5-tang-tdd"></a>

## 5. Đặc tả Kịch bản Kiểm thử TDD (TDD Test Specification)

### 5.1. Logic Issues Resolved

| #  | Vấn đề nghiệp vụ (Spec gốc thiếu)                           | Hướng giải quyết thực tế                                           | Cách thức cài đặt trong kiểm thử (Test fix)                                                                                          |
| -- | ------------------------------------------------------------------ | ------------------------------------------------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------- |
| L1 | Chưa mô tả cụ thể cơ chế khóa khi tranh chấp mua vé tour | Bắt buộc dùng`Pessimistic Write Lock` trong DB Transaction.         | Dùng`ExecutorService` đẩy đồng thời 5 luồng đặt tour cùng 1 chuyến xe chỉ còn 2 chỗ. Xác nhận chỉ 1 luồng thành công. |
| L2 | Điểm danh FaceID thất bại do ngoại cảnh (nắng, góc mờ)    | Cho phép HDV override ghi nhận thủ công có đối chiếu nhân viên | Ghi nhận ID của Tour Guide thực hiện ghi đè, cập nhật`attendance_status = 'Boarded_Manual'`.                                      |

---

### 5.2. Test Cases Specification

#### MOD4-TC-001 — Tranh chấp mua vé Tour đồng thời (Pessimistic Lock)

- **Severity:** **CRITICAL**
- **CWE:** CWE-362 — Concurrent Execution using Shared Resource with Improper Synchronization
- **Feature Under Test:** `TourBookingServiceImpl.bookTour()`
- **TDD Phase:** 🔴 RED
- **Preconditions:**
  - `TourSchedule` ID 105 có `maxCapacity = 10`, `bookedSeats = 8` (còn trống 2 chỗ).
- **Test Steps:**
  1. Khởi tạo 2 tiến trình con chạy song song (sử dụng `CountDownLatch`).
  2. Tiến trình 1 gọi `bookTour` đặt 2 chỗ.
  3. Tiến trình 2 gọi `bookTour` đặt 1 chỗ.
  4. Đồng thời kích hoạt cả 2 luồng gửi request lên server.
- **Expected Result (PASS):**
  - Chỉ 1 trong 2 luồng thành công ghi nhận giao dịch (đặt 2 chỗ hoặc đặt 1 chỗ).
  - Luồng còn lại bị ném ngoại lệ `BusinessException` với mã lỗi `TOUR-001` (Không đủ chỗ) hoặc `PessimisticLockException`.
  - Số chỗ `bookedSeats` trong CSDL không được vượt quá 10.
- **Expected Result (FAIL):**
  - Cả 2 luồng đều thành công, `bookedSeats` tăng lên thành 11 (vượt quá dung lượng xe 10 chỗ).

#### MOD4-TC-002 — Xác thực AI Face Scan khớp tự động (>= 85%)

- **Severity:** HIGH
- **Feature Under Test:** `FaceIdServiceImpl.scanFace()`
- **Preconditions:**
  - Khách hàng đã có vector khuôn mặt chuẩn trong DB.
- **Test Steps:**
  1. Gửi tệp ảnh khuôn mặt hợp lệ đến `/api/faceid/scan`.
  2. Mock dịch vụ AI trả về độ tin cậy `confidence = 0.89` (>= 0.85).
  3. Kiểm tra DB của `TourAttendee`. Assert `attendance_status == 'Boarded'`.
- **Expected Result (PASS):**
  - Trạng thái hành khách chuyển sang `Boarded`, lưu vết mốc thời gian nhận diện thành công.

#### MOD4-TC-003 — Điểm danh thủ công Fallback khi AI thất bại (< 85%)

- **Severity:** HIGH
- **Feature Under Test:** `FaceIdServiceImpl.manualCheckIn()`
- **Test Steps:**
  1. Gửi tệp ảnh mờ đến `/api/faceid/scan`.
  2. Dịch vụ AI trả về độ tin cậy `confidence = 0.55` (nhỏ hơn ngưỡng 0.85).
  3. Trạng thái hành khách giữ nguyên `Not_Show`.
  4. Hướng dẫn viên gọi API `POST /api/v1/tour-attendance/24/manual` để xác thực bằng tay.
- **Expected Result (PASS):**
  - Hệ thống cập nhật trạng thái `attendance_status = 'Boarded_Manual'` và lưu mã định danh nhân viên thực hiện thao tác.

#### MOD4-TC-004 — Tự động hủy tour khi không đủ khách khởi hành (T-24h)

- **Severity:** HIGH
- **Feature Under Test:** `TourMinimumPaxJob`
- **Preconditions:**
  - Lịch trình tour khởi hành vào ngày mai (còn 23 giờ).
  - `minimumPax = 5`, `bookedSeats = 3` (chỉ có 3 khách đặt).
- **Test Steps:**
  1. Chạy tiến trình cron `checkMinimumPax()`.
  2. Kiểm tra trạng thái của `TourSchedule` và các `TourBooking` liên quan.
- **Expected Result (PASS):**
  - Trạng thái TourSchedule chuyển sang `Cancelled`.
  - Các Booking liên quan được tự động hoàn tiền và gửi email thông báo qua SendGrid.

---

<a id="6-roadmap"></a>

## 6. Kế hoạch Triển khai Chi tiết (Implementation Roadmap)

Để hoàn thiện luồng nghiệp vụ Module 4 theo chuẩn fullstack MVC, đội ngũ phát triển cần triển khai qua 6 bước cụ thể:

### Bước 1: Khai báo Cơ sở Dữ liệu & Thực thể JPA

- **Công việc:**
  - Khai báo các cột và ánh xạ `@Entity` cho `TourSchedule`, `TourBooking`, `TourAttendee`, `CheckpointAttendance` trong package `com.kawai.models`.
  - Đăng ký khóa ngoại và ràng buộc kiểm tra logic dữ liệu.
- **File tác động:** `TourSchedule.java`, `TourBooking.java`, `TourAttendee.java`.

### Bước 2: Thiết lập Tầng Repository & Khóa Bi quan (Pessimistic Lock)

- **Công việc:**
  - Viết truy vấn `@Lock(LockModeType.PESSIMISTIC_WRITE)` trong `TourScheduleRepository.java`.
  - Viết SQL Script tạo Database Trigger `TRG_Tour_Capacity_Validator` vào file `schema.sql` để CSDL tự động chạy mỗi khi khởi chạy ứng dụng.
- **File tác động:** `TourScheduleRepository.java`, `schema.sql`.

### Bước 3: Cài đặt Tầng Nghiệp vụ (Service Layer Implementation)

- **Công việc:**
  - Cài đặt hàm `bookTour` trong `TourBookingServiceImpl.java`, đóng gói trong annotation `@Transactional`.
  - Thiết lập REST client `PythonAiServiceClient.java` sử dụng `WebClient` hoặc `RestTemplate` để kết nối trực tiếp đến microservice Python AI xử lý ảnh.
  - Cài đặt Cronjob `TourMinimumPaxJob.java` chạy tự động hàng giờ quét điều kiện khởi hành.
- **File tác động:** `TourBookingServiceImpl.java`, `FaceIdServiceImpl.java`, `TourMinimumPaxJob.java`.

### Bước 4: Xây dựng các Controllers (MVC Controllers)

- **Công việc:**
  - Viết Web Controller trong `TourGuideController.java` để phân quyền và trả về view Thymeleaf (`tour/Tour.html`, `tour/FaceID.html`).
  - Viết REST API Controllers trong `FaceIdApiController.java` tiếp nhận file ảnh từ client gửi lên để gửi đi so khớp AI.
- **File tác động:** `TourGuideController.java`, `FaceIdApiController.java`, `TourAttendanceRestController.java`.

### Bước 5: Hoàn thiện Giao diện & Gọi API AJAX (View Layer)

- **Công việc:**
  - Nhúng thư viện JavaScript điều khiển camera, lấy frame ảnh tĩnh từ thẻ `<video>` chuyển thành dạng file Blob.
  - Viết script gửi request AJAX `POST /api/faceid/scan` kèm dữ liệu ảnh dạng `multipart/form-data`.
  - Cập nhật trạng thái giao diện (đổi màu thẻ khách hàng từ Đỏ sang Xanh) bằng DOM Manipulation sau khi nhận callback thành công từ API.
- **File tác động:** `FaceID.html`, `Tour.html`, `tour-faceid-app.js`.

### Bước 6: Kiểm thử TDD & Đo đạc Chỉ số

- **Công việc:**
  - Viết các test case trong package `com.kawai.services.mod4` sử dụng Mockito và Spring Security Test.
  - Chạy `mvn clean test` đo lường mức độ phủ sóng code (mục tiêu Coverage >= 85%).
- **File tác động:** `TourBookingServiceTest.java`, `FaceIdServiceTest.java`.

---

*Tài liệu kế hoạch thực thi kỹ thuật được thiết lập bởi Antigravity — ngày 2026-06-29.*
*Tuân thủ kiến trúc MVC Spring Boot và cấu trúc kiểm thử hướng TDD.*