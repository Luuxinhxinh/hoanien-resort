# ADR-01 — Kiến trúc Spring Boot MVC Layered & Technical Stack

**Status:** `Accepted`
**Date:** 2026-06-29
**Deciders:** Team Lead, Backend Developers, Group 2 — SWP391 SE2023
**Context tags:** #architecture #mvc #spring-boot #layered #security #aop #scheduling
**US References:** UC-01, UC-02, UC-11, UC-13, UC-15, UC-22, UC-30 (SRS_Document_SWP391_G2)
**BR References:** `BR-SYS-01`, `BR-SYS-02`, `BR-SYS-04`, `BR-SYS-07`, `BR-FO-01`, `BR-FO-02`, `BR-FIN-03`

---

## Context — Bối cảnh

Dự án **Kawai Retreat Resort & Hub** cần xây dựng một hệ thống quản lý nghỉ dưỡng tích hợp phục vụ đồng thời nhiều loại người dùng:

- **Khách hàng (Customer):** Đặt phòng, đặt tour, theo dõi hóa đơn qua giao diện web công khai
- **Nhân viên vận hành (Staff):** Lễ tân, Buồng phòng, Bếp, Nhà hàng, Hướng dẫn tour — mỗi nhóm có portal riêng
- **Quản lý (Admin/Manager):** Dashboard USALI, cấu hình hệ thống, báo cáo

**Ràng buộc kỹ thuật và nghiệp vụ tồn tại:**

1. Đội nhỏ (4–6 người), cần framework quen thuộc và có cộng đồng tốt
2. Giao diện web server-rendered (Thymeleaf) và REST API riêng biệt cho mobile/AJAX calls
3. Xác thực phức tạp: Form login + Google OAuth2 + 2FA OTP + Device authorization
4. RBAC với nhiều vai trò (Admin, Manager, Receptionist, F&B Staff, Kitchen, Housekeeping, Maintenance, Tour Guide, Customer)
5. Cần Audit Log toàn diện mọi thao tác nhạy cảm (BR-SYS-04)
6. Scheduled jobs: Night Audit 02:00 AM, Booking cleanup, Tour auto-cancel
7. Tích hợp bên ngoài: VNPay, SendGrid, OpenWeatherMap, Python AI service (FaceID)
8. Database: MySQL 8.x với Spring Data JPA/Hibernate, Optimistic Locking (`@Version`)

---

## Options Considered — Các lựa chọn đã xem xét

| Option                                                 | Mô tả                                                                                                      | Pros                                                                                                                                                   | Cons                                                                                                  |
| :----------------------------------------------------- | :----------------------------------------------------------------------------------------------------------- | :----------------------------------------------------------------------------------------------------------------------------------------------------- | :---------------------------------------------------------------------------------------------------- |
| **A — Spring Boot MVC Layered (được chọn)** | Spring Boot 3.2 + Thymeleaf (View) + REST API tách biệt + Spring Security + Spring AOP + Spring Scheduling | Quen thuộc, tài liệu phong phú; dễ onboard thành viên mới; Thymeleaf SSR tốt cho SEO; Spring Security mạnh cho RBAC; tích hợp JPA seamless | Tightly coupled hơn microservices; Thymeleaf templates khó maintain khi quy mô lớn                |
| **B — Spring Boot + React SPA**                 | Backend REST-only, Frontend React riêng                                                                     | Tách biệt rõ frontend/backend; Modern UX                                                                                                            | Cần team frontend riêng; thêm phức tạp CORS, token management; vượt scope SWP391               |
| **C — Microservices**                           | Mỗi module là service riêng (Booking Service, F&B Service, Tour Service…)                                | Scalable; mỗi team tự chủ                                                                                                                           | Quá phức tạp cho team 4-6 người; cần Kubernetes/Docker orchestration; overkill cho scope SWP391 |
| **D — Jakarta EE (WildFly/Payara)**             | Jakarta EE CDI + JSF + EJB                                                                                   | Tiêu chuẩn Java EE; enterprise-grade                                                                                                                 | Cấu hình nặng; ít phổ biến trong trường học; bootcamp dài hơn                              |

---

## Decision — Quyết định

**Chúng ta chọn Option A: Spring Boot 3.2 MVC Layered Architecture** với cấu trúc package:

```
com.kawai/
├── KawaiApplication.java          ← Entry point (@SpringBootApplication)
├── config/                        ← Cross-cutting configuration
│   ├── SecurityConfig.java        ← Spring Security RBAC
│   ├── AuditLogAspect.java        ← Spring AOP Audit interceptor
│   ├── DynamicJobManager.java     ← Scheduling engine
│   ├── WebMvcConfig.java          ← MVC resource handlers
│   ├── VnPayConfig.java           ← VNPay gateway config
│   └── OAuth2SuccessHandler.java  ← OAuth2 post-auth routing
├── controllers/
│   ├── web/                       ← @Controller (Thymeleaf, returns View name)
│   └── api/                       ← @RestController (returns JSON)
├── services/
│   ├── interfaces/                ← Service contracts (Interfaces)
│   └── impl/                      ← Business logic implementations
├── repositories/                  ← Spring Data JPA Repositories
├── models/                        ← JPA Entities (@Entity, @Table)
├── dto/                           ← Data Transfer Objects (Request/Response)
├── security/                      ← UserDetailsService, RolePermissionConstants
├── exceptions/                    ← Custom exceptions (BusinessException)
├── events/ & listeners/           ← Spring Application Events
└── utils/                         ← @LogActivity annotation, helpers
```

---

## Rationale — Lý do

### 1. Dual Controller Layer (Web + API)

Codebase phân tách rõ:

- **`controllers/web/`** — 20 web controllers dùng `@Controller`, return Thymeleaf view names để render HTML server-side. Đây là nơi các role-specific portals được serve: `AdminController`, `ManagerController`, `ReceptionistController`, `TourGuideController`, `HousekeepingWebController`, `MaintenanceWebController`...
- **`controllers/api/`** — 30 REST controllers dùng `@RestController`, return JSON cho AJAX, mobile, hoặc integrations: `BookingApiController`, `FolioRestController`, `TourBookingApiController`, `NightAuditRestController`...

Điều này cho phép cùng một `Service` layer phục vụ cả hai kênh mà không bị duplicate business logic.

### 2. Service Interface + Implementation Pattern

Mỗi domain có:

- Interface trong `services/interfaces/` (e.g., `BookingService`, `AuthService`, `FolioService`)
- Implementation trong `services/impl/` (e.g., `BookingServiceImpl`, `AuthServiceImpl`)

Lợi ích: dễ mock trong unit test; dễ swap implementation sau này.

Ví dụ thực tế từ codebase:

- `BookingServiceImpl.java` — 57KB, xử lý toàn bộ booking lifecycle
- `VnPayServiceImpl.java` — 30KB, toàn bộ VNPay payment flow
- `WalkInCheckInServiceImpl.java` — 33KB, walk-in check-in complex flow
- `WorkflowEngineServiceImpl.java` — 21KB, dynamic workflow engine

### 3. Spring Security RBAC

Từ `SecurityConfig.java` và `RolePermissionConstants.java`, hệ thống có:

**Roles chính:**

| Role                                       | Portal                                       | Permissions                                |
| :----------------------------------------- | :------------------------------------------- | :----------------------------------------- |
| `ROLE_ADMIN`                             | `/admin/**`                                | Toàn bộ                                  |
| `ROLE_MANAGER`                           | `/manager/**`                              | Dashboard, Analytics, Reports, Night Audit |
| `ROLE_RECEPTIONIST`                      | `/receptionist/**`                         | Booking, Check-in, Check-out, Folio        |
| `ROLE_FB_STAFF`                          | `/fbStaff/**`                              | F&B Orders, POS, Tables                    |
| `ROLE_HOUSEKEEPING`                      | `/housekeeping/**`                         | Room cleaning tasks                        |
| `ROLE_MAINTENANCE` / `ROLE_MAINTAINER` | `/maintenance/**`                          | Repair tickets                             |
| `ROLE_TOURGUIDE`                         | `/tourguide/**`                            | Tour attendance, GPS tracking              |
| Customer (default)                         | `/profile/**`, `/booking`, `/tours/**` | Self-service                               |

**Sub-permissions (fine-grained):**
`RECEPTION_CHECKIN`, `RECEPTION_CHECKOUT`, `RECEPTION_WALKIN`, `FNB_ORDER`, `FNB_TABLE`, `FNB_ROOM_SERVICE`, `NIGHT_AUDIT`, `ANALYTICS`, `WORKFLOW`...

Hai login portal:

- `/booking` → Customer login (Google OAuth2 supported)
- `/ops-login` → Staff/Admin login (Device-based 2FA)

### 4. Spring AOP — Audit Logging

`AuditLogAspect.java` sử dụng `@Around` advice trên annotation `@LogActivity`:

- Tự động lấy username từ `SecurityContextHolder`
- Lấy IP từ `HttpServletRequest`
- INSERT vào `AuditLog` entity sau khi method thực thi thành công
- Không ảnh hưởng logic chính (cross-cutting concern tách biệt)

### 5. Spring Scheduling — DynamicJobManager

`DynamicJobManager.java` với `@EnableScheduling` + `TaskScheduler`:

- `audit_cleanup` — Dọn Audit Log > 90 ngày: `"0 0 2 * * ?"`
- `reservation_cleanup` — Hủy đặt bàn No-show mỗi 15 phút: `"0 0/15 * * * ?"`
- Jobs có thể start/stop/reschedule runtime qua Admin UI (không cần restart server)

Night Audit thực thi tự động lúc 02:00 AM, post room charges cho tất cả phòng đang Checked_In.

### 6. Optimistic Locking — Chống Overbooking

`Booking.java` có `@Version private Integer version = 1` — Hibernate tự động tăng version mỗi UPDATE. Nếu 2 transaction cùng modify 1 Booking, transaction sau sẽ nhận `OptimisticLockException` → business layer bắt và xử lý appropriate.

### 7. Hibernate Envers — Entity Auditing

`Account.java` có `@Audited` — Hibernate Envers tự động tạo bảng `Accounts_AUD` ghi lại mọi thay đổi với revision tracking. `CustomRevisionEntity.java` + `CustomRevisionListener.java` mở rộng revision metadata.

### 8. OAuth2 + Google Login

`CustomOAuth2UserService` + `OAuthAccountService` xử lý Google OAuth2 flow:

- Nhận Google user info sau callback
- Tìm hoặc tạo `Account` tương ứng
- `OAuth2SuccessHandler` routing sau auth thành công

---

## Consequences — Hệ quả

**Positive:**

- Codebase có cấu trúc rõ ràng, dễ navigate cho team mới
- Spring Security xử lý RBAC mà không cần viết filter thủ công
- AOP Audit Log không xâm phạm business logic
- Thymeleaf SSR tốt cho các trang dashboard complex
- Spring Data JPA giảm thiểu boilerplate SQL
- `DynamicJobManager` cho phép quản lý scheduled jobs runtime — linh hoạt hơn `@Scheduled` cứng

**Negative (trade-offs accepted):**

- Thymeleaf templates có thể khó maintain khi số lượng views tăng (hiện ~20 web controllers, mỗi controller nhiều views)
- Server-side rendering không optimal cho real-time features (WebSocket cần xử lý riêng)
- Hai loại controller (web + api) có thể gây nhầm lẫn nếu không có naming convention rõ
- `@Autowired` field injection (thay vì constructor injection) ở nhiều chỗ — khó unit test hơn

**Risks:**

- `BookingServiceImpl.java` 57KB và `WalkInCheckInServiceImpl.java` 33KB — có nguy cơ trở thành God Class, cần refactor theo Single Responsibility Principle
- CSRF disabled (`csrf.disable()`) — cần đảm bảo toàn bộ state-changing endpoints có authentication
- Nhiều `@Autowired` direct repositories trong Controllers (e.g., `BookingApiController` inject `CustomerRepository` trực tiếp) — vi phạm separation of concerns, nên chuyển qua Service layer
- Các debug controller như `BackdoorController.java`, `DebugSqlController.java`, `TestDebugController.java` tuyệt đối không được đưa vào production build do rủi ro bảo mật nghiêm trọng (đã xóa trong bản cập nhật 2026-07-02).

**Compliance Impact:**

- Audit Log qua AOP (`BR-SYS-04`) đảm bảo mọi thao tác nhạy cảm được ghi lại
- BCrypt password encoding (`BR-SYS-01`) được enforce bởi Spring Security `PasswordEncoder` bean
- Device-based 2FA cho Staff portal giảm thiểu rủi ro credential theft
- `@Version` Optimistic Lock (`BR-FO-01`) tuân thủ yêu cầu chống overbooking

**Decisions unlocked (ADR tiếp theo có thể viết):**

- ADR-02: Database Schema & Trigger Strategy (TRG_Auto_Housekeeping_Task, TRG_Prevent_Overbooking…)
- ADR-03: VNPay Payment Gateway Integration Pattern
- ADR-04: AI Face Recognition Service Integration (Python microservice via HTTP)
- ADR-05: Email Notification Strategy (SendGrid + Spring Mail)
- ADR-06: Night Audit Scheduling & Idempotency Strategy
- ADR-07: Dynamic Workflow Engine Design Pattern

---

## Technical Specification

### Tech Stack Chi tiết

| Thành phần                  | Technology                      | Version                            | Ghi chú                                   |
| :---------------------------- | :------------------------------ | :--------------------------------- | :----------------------------------------- |
| **Language**            | Java                            | 17 (LTS)                           | `java.version=17` trong pom.xml          |
| **Framework**           | Spring Boot                     | 3.2.4                              | `spring-boot-starter-parent`             |
| **Web MVC**             | Spring Web MVC + Thymeleaf      | Boot-managed                       | SSR + REST                                 |
| **Security**            | Spring Security + OAuth2 Client | Boot-managed                       | RBAC + Google Login                        |
| **ORM**                 | Spring Data JPA + Hibernate     | Boot-managed                       | MySQL dialect                              |
| **Auditing**            | Hibernate Envers                | `spring-data-envers`             | Entity revision tracking                   |
| **AOP**                 | Spring AOP                      | `spring-boot-starter-aop`        | Audit Log interceptor                      |
| **Validation**          | Spring Validation               | `spring-boot-starter-validation` | Bean Validation                            |
| **Database (Prod)**     | MySQL 8.x                       | `mysql-connector-j` runtime      | Primary database                           |
| **Database (Dev/Test)** | H2 In-Memory                    | `h2` runtime                     | Test / dev sandbox                         |
| **Email**               | Spring Mail + SendGrid          | `sendgrid-java 4.9.3`            | Transactional emails                       |
| **Excel Export**        | Apache POI                      | `poi-ooxml 5.2.5`                | USALI report export                        |
| **Boilerplate**         | Lombok                          | 1.18.36                            | @Getter, @Setter, @RequiredArgsConstructor |
| **Payment**             | VNPay                           | Custom integration                 | HMAC-SHA512 signature                      |
| **AI Service**          | Python FastAPI (external)       | N/A                                | FaceID via HTTP client                     |
| **Weather API**         | OpenWeatherMap                  | N/A                                | Tour scheduling context                    |
| **Build**               | Maven                           | 3.x                                | `mvnw` wrapper                           |
| **Scheduling**          | Spring Scheduling               | `@EnableScheduling`              | `DynamicJobManager`                      |
| **Async**               | Spring Async                    | `@EnableAsync`                   | Email sending async                        |

### Package Structure — Chi tiết từng Layer

#### Layer 1: Models (Persistence Layer)

**49 JPA Entities** được map 1-1 với database tables:

| Entity                  | Table                     | Ghi chú                                              |
| :---------------------- | :------------------------ | :---------------------------------------------------- |
| `Account`             | `Accounts`              | `@Audited` Envers; `@Version`-less                |
| `Booking`             | `Bookings`              | `@Version` Optimistic Lock; `@Inheritance JOINED` |
| `RoomBooking`         | `Room_Bookings`         | Extends Booking                                       |
| `RoomBookingDetail`   | `Room_Booking_Details`  | PIN hash, Credit Limit                                |
| `Customer`            | `Customers`             | CCCD encrypted, Face vector                           |
| `Employee`            | `Employees`             | Salary, Department                                    |
| `Role`                | `Roles`                 | RBAC role                                             |
| `Room`                | `Rooms`                 | Room status state                                     |
| `RoomCategory`        | `Room_Categories`       | Base price, max capacity                              |
| `DailyRate`           | `Daily_Rates`           | Per-day price for Night Audit                         |
| `FoodOrder`           | `Food_Orders`           | KOT status, payment type                              |
| `FoodOrderDetail`     | `Food_Order_Details`    | KOT per item                                          |
| `MenuItem`            | `Menu_Items`            | is_available flag                                     |
| `Tour`                | `Tours`                 | Tour catalog                                          |
| `TourSchedule`        | `Tour_Schedules`        | Departure date, booked seats                          |
| `TourBooking`         | `Tour_Bookings`         | Participant count                                     |
| `TourAttendee`        | `Tour_Attendees`        | Face vector, cosine similarity                        |
| `ConsolidatedInvoice` | `Consolidated_Invoices` | Final checkout invoice                                |
| `FolioItem`           | `Folio_Items`           | Line items per department                             |
| `PaymentTransaction`  | `Payment_Transactions`  | VNPay transaction log                                 |
| `HotelOperation`      | `Hotel_Operations`      | Housekeeping + Maintenance tasks                      |
| `AuditLog`            | `Audit_Logs`            | INSERT-only audit trail                               |
| `Promotion`           | `Promotions`            | Voucher/discount                                      |
| `Workflow`            | `Workflows`             | Dynamic workflow engine                               |
| ...                     |                           | 49 entities tổng cộng                               |

#### Layer 2: Repositories (Data Access Layer)

**44 Spring Data JPA Repositories** với pattern:

```java
public interface BookingRepository extends JpaRepository<Booking, Long> {
    // Custom JPQL / native queries
    @Query("SELECT b FROM Booking b WHERE b.customer = :customer ...")
    List<Booking> findByCustomer(@Param("customer") Customer customer);
}
```

Key repositories phức tạp:

- `RoomBookingRepository` — 5.9KB: queries cho availability, conflict detection
- `TourBookingRepository` — 3.5KB: capacity checks, cancellation flows
- `FoodOrderRepository` — 2.8KB: KOT queries, POS aggregations
- `VnPayServiceImpl` — 30KB: HMAC generation, webhook verification

#### Layer 3: Services (Business Logic Layer)

**35 Service Interfaces + 37 Service Implementations**

Các service lớn và phức tạp nhất:

| Service                       | Size | Responsibility                                                              |
| :---------------------------- | :--- | :-------------------------------------------------------------------------- |
| `BookingServiceImpl`        | 57KB | Đặt phòng end-to-end: availability check, cart lock, VNPay, cancellation |
| `WalkInCheckInServiceImpl`  | 33KB | Walk-in check-in với OCR CCCD, room assignment, credit setup               |
| `VnPayServiceImpl`          | 30KB | Payment URL tạo, webhook verify, refund processing                         |
| `PosServiceImpl`            | 23KB | F&B POS: order creation, KOT routing, post-to-room                          |
| `TourBookingServiceImpl`    | 23KB | Tour booking: capacity validation, attendance, AI face scan                 |
| `WorkflowEngineServiceImpl` | 21KB | Dynamic BPMN-like workflow execution                                        |
| `MasterDataServiceImpl`     | 31KB | CRUD cho tất cả master data: rooms, categories, tours, staff              |
| `AdminViewServiceImpl`      | 36KB | Data aggregation cho Admin dashboard views                                  |
| `AuthServiceImpl`           | 15KB | Login, OTP, 2FA, device auth, password reset                                |
| `EmailServiceImpl`          | 28KB | Tất cả email templates: booking confirm, OTP, invoice, tour ticket        |

#### Layer 4: Controllers (Presentation Layer)

**Web Controllers (19)** — `@Controller`, return Thymeleaf view names:

| Controller                           | URL Prefix                   | Role           | Responsibility                                    |
| :----------------------------------- | :--------------------------- | :------------- | :------------------------------------------------ |
| `AdminController`                  | `/admin/**`                | ADMIN, MANAGER | User management, audit log, workflow config       |
| `ManagerController`                | `/manager/**`              | MANAGER        | Dashboard, reports, night audit, analytics (34KB) |
| `ReceptionistController`           | `/receptionist/**`         | RECEPTIONIST   | Room matrix, in-house operations, folio (26KB)    |
| `ReceptionistCheckinWebController` | `/receptionist/checkin/**` | RECEPTIONIST   | Check-in wizard với CCCD OCR (20KB)              |
| `ProfileController`                | `/profile/**`              | Customer       | Self-service profile, booking history (19KB)      |
| `TourGuideController`              | `/tourguide/**`            | TOUR GUIDE     | Tour attendance, AI face scan, GPS (28KB)         |
| `OrderFoodController`              | `/order-food/**`           | Customer       | Room service ordering                             |
| `HousekeepingWebController`        | `/housekeeping/**`         | HOUSEKEEPING   | Task list, room status update                     |
| `MaintenanceWebController`         | `/maintenance/**`          | MAINTENANCE    | Repair tickets management                         |
| `TourController`                   | `/tours/**`                | Customer       | Tour browsing với weather integration            |
| `BookingController`                | `/booking/**`              | Customer       | Booking wizard                                    |
| `PosController`                    | `/fbStaff/**`              | FB_STAFF       | POS terminal                                      |
| `KitchenController`                | `/kitchenStaff/**`         | FB_STAFF       | KDS display                                       |
| `AuthController`                   | `/auth/**`                 | All            | Login, register, logout pages                     |
| `PaymentController`                | `/payment/**`              | Customer       | VNPay redirect handling                           |

**API Controllers (30)** — `@RestController`, return JSON:

| Controller                     | URL Prefix              | Responsibility                        |
| :----------------------------- | :---------------------- | :------------------------------------ |
| `BookingApiController`       | `/api/bookings`       | Booking CRUD + availability           |
| `FolioRestController`        | `/api/folio/**`       | Folio management — 49KB (lớn nhất) |
| `TourBookingApiController`   | `/api/tour-bookings`  | Tour booking operations — 19KB       |
| `RoomApiController`          | `/api/rooms/**`       | Room status, assignment               |
| `AuthApiController`          | `/api/v1/auth/**`     | Login/register REST                   |
| `FaceIdApiController`        | `/api/faceid/**`      | AI face scan operations — 14KB       |
| `AuditApiController`         | `/api/v1/audit/**`    | Audit log queries — 12KB             |
| `PaymentApiController`       | `/api/v1/payments/**` | VNPay webhook, refund                 |
| `NightAuditRestController`   | `/api/night-audit/**` | Night audit trigger + status          |
| `WalkInCheckInApiController` | `/api/walk-in/**`     | Walk-in check-in REST                 |
| `ManagerReportApiController` | `/api/manager/**`     | Report data APIs                      |
| `WorkflowApiController`      | `/api/workflow/**`    | Workflow engine REST                  |
| `ImportApiController`        | `/api/import/**`      | Bulk data import                      |
| `ExportApiController`        | `/api/export/**`      | Excel/PDF export                      |
| ...                            |                         | 30 APIs tổng cộng                   |

#### Layer 5: DTOs (Data Transfer Objects)

**25+ DTOs** tách biệt API contract khỏi Entity:

```
BookingRequestDTO    ← Incoming booking request
BookingResponseDTO   ← Booking confirmation response
RoomSearchRequestDTO ← Room search filters
RoomSearchResponseDTO← Available rooms list
TourBookingRequest   ← Tour booking with attendees
CheckinSubmitFormDTO ← Check-in form data
DependentRegistrationDTO ← Family member registration
WeatherInfo          ← OpenWeatherMap response
...
```

#### Cross-Cutting Concerns

**Spring AOP (`AuditLogAspect`):**

```java
@Around("@annotation(com.kawai.utils.LogActivity)")
public Object logActivity(ProceedingJoinPoint joinPoint) {
    // Execute method
    Object result = joinPoint.proceed();
    // Extract user from SecurityContextHolder
    // Extract IP from HttpServletRequest
    // INSERT AuditLog
}
```

**Spring Events (`events/` + `listeners/`):**

- Application events cho async processing (email sending, notification)

**Exception Handling (`exceptions/`):**

- `BusinessException` — custom exception với error code + message

### Configuration Files

| File                            | Mục đích                                                       |
| :------------------------------ | :---------------------------------------------------------------- |
| `SecurityConfig.java`         | Spring Security: URL authorization, login/logout handlers, OAuth2 |
| `AuditLogAspect.java`         | AOP:`@LogActivity` annotation processor                         |
| `DynamicJobManager.java`      | Scheduling: Dynamic cron job management                           |
| `WebMvcConfig.java`           | MVC: Resource handlers, static files                              |
| `VnPayConfig.java`            | VNPay: Payment gateway credentials                                |
| `OAuth2SuccessHandler.java`   | OAuth2: Post-login routing by role                                |
| `RolePermissionSeeder.java`   | DB seed: Default role permissions                                 |
| `CustomRevisionListener.java` | Envers: Custom revision entity populator                          |

---

## MVC Pattern — Luồng Request Thực tế

### Luồng Web Request (Thymeleaf SSR)

```
HTTP Request
     │
     ▼
[Spring Security Filter Chain]
  ├── Authentication check
  ├── Authorization check (URL-based RBAC)
  └── Device authorization (for /ops-login portal)
     │
     ▼
[Web Controller] (@Controller)
  ├── Nhận request, extract params
  ├── Resolve current user (Principal → Account → Customer/Employee)
  ├── Gọi Service(s)
  ├── Populate Model attributes
  └── Return view name (e.g., "receptionist/room-matrix")
     │
     ▼
[Service Layer] (Interface → Implementation)
  ├── Business logic & validation
  ├── Optimistic Lock handling (@Version)
  ├── @Transactional boundaries
  └── Gọi Repository / External Services
     │
     ▼
[Repository Layer] (Spring Data JPA)
  ├── JPA queries (JPQL / native SQL)
  ├── Hibernate ORM → MySQL
  └── Return Entity objects
     │
     ▼
[Service → Controller → Model]
     │
     ▼
[Thymeleaf Template Engine]
  └── Render HTML with Model data → HTTP Response
```

### Luồng API Request (REST JSON)

```
HTTP Request (AJAX / Mobile)
     │
     ▼
[Spring Security Filter]
     │
     ▼
[REST Controller] (@RestController)
  ├── @RequestBody → DTO deserialization
  ├── @Valid Bean Validation
  ├── Gọi Service
  └── Return ResponseEntity<DTO>
     │
     ▼
[Service Layer]
  ├── Business logic
  ├── External API calls (VNPay, OpenWeather, AI FaceID)
  └── Repository operations
     │
     ▼
[Repository] → MySQL
     │
     ▼
[Jackson serialization → JSON Response]
```

### AOP Audit Log Flow

```
Controller Method với @LogActivity
     │
     ▼
[AuditLogAspect.@Around]
  ├── joinPoint.proceed() ← Execute method
  ├── SecurityContextHolder → get username
  ├── RequestContextHolder → get IP
  └── auditLogRepository.save(auditLog)
```

### Scheduling Flow (DynamicJobManager)

```
Application Startup
     │
     ▼
[@PostConstruct] DynamicJobManager.init()
  ├── Register: audit_cleanup (02:00 AM daily)
  ├── Register: reservation_cleanup (every 15 min)
  ├── Register: booking_hold_cleanup
  └── Register: night_audit_job (02:00 AM daily)
     │
     ▼
[TaskScheduler] — Spring's managed thread pool
  └── Execute Runnable at CronTrigger time
```

---

## §AI Prompt Constraint ⭐ CASE 2.0 — BẮT BUỘC

> **Đóng góp cốt lõi của CASE 2.0.** Đoạn text dưới đây sẽ được **inject trực tiếp** vào AI prompt khi implement tại Phase 5.
> Mỗi constraint phải **specific** và **actionable** — không generic.
> ADR thiếu section này = ADR chưa hoàn chỉnh theo CASE 2.0.

```
Theo ADR-01 (Spring Boot MVC Layered Architecture):

1. PHẢI đặt business logic vào Service layer (services/impl/), KHÔNG viết logic trong Controller.
   Controller chỉ được phép: extract params, call service, populate model, return view/response.

2. KHÔNG được inject Repository trực tiếp vào Controller.
   Mọi data access phải đi qua Service Interface (services/interfaces/).

3. PHẢI sử dụng DTO (dto/) để truyền dữ liệu giữa Controller và Service.
   KHÔNG trả Entity trực tiếp từ @RestController endpoints.

4. Mọi API endpoint có tác động thay đổi dữ liệu (POST/PUT/DELETE) ĐỀU PHẢI:
   - Được annotate với @PreAuthorize hoặc được khai báo trong SecurityConfig.java
   - Nếu cần audit: annotate method Service với @LogActivity (action, module)

5. KHÔNG được hardcode role string trong Controller. Dùng constants từ RolePermissionConstants.java
   hoặc Spring Security annotations (@PreAuthorize("hasAuthority('ROLE_ADMIN')")).

6. Identity của user hiện tại PHẢI lấy từ SecurityContextHolder hoặc Principal parameter.
   KHÔNG dùng session attribute trực tiếp để lấy user identity.

7. Layer chịu trách nhiệm: SecurityConfig = URL authorization; @PreAuthorize/@PostAuthorize = method-level.
   AuditLogAspect = cross-cutting audit. DynamicJobManager = scheduled automation.
```

---

*Template version 2.0 — PrivacyOps Architecture Team — Tích hợp CASE 2.0*
*Section đánh dấu ⭐ là bổ sung mới từ CASE 2.0 methodology.*

---

## Phụ lục — Cấu trúc Thư mục Thực tế (Scan từ Codebase)

```
05-Development/kawai-backend/
├── pom.xml                    ← Maven build descriptor
├── src/main/java/com/kawai/
│   ├── KawaiApplication.java  ← @SpringBootApplication @EnableScheduling @EnableAsync
│   ├── config/                ← 8 files
│   │   ├── SecurityConfig.java           (14.9KB)
│   │   ├── AuditLogAspect.java           (3.5KB)
│   │   ├── DynamicJobManager.java        (6.5KB)
│   │   ├── VnPayConfig.java              (0.9KB)
│   │   ├── WebMvcConfig.java             (1.0KB)
│   │   ├── OAuth2SuccessHandler.java     (3.3KB)
│   │   ├── RolePermissionSeeder.java     (2.1KB)
│   │   └── CustomRevisionListener.java   (1.1KB)
│   ├── controllers/
│   │   ├── api/               ← 30 REST controllers
│   │   │   ├── FolioRestController.java       (49.7KB) ← largest
│   │   │   ├── BookingApiController.java      (25.6KB)
│   │   │   ├── TourBookingApiController.java  (19.1KB)
│   │   │   ├── FaceIdApiController.java       (14.4KB)
│   │   │   ├── RoomApiController.java         (12.7KB)
│   │   │   ├── AuditApiController.java        (12.3KB)
│   │   │   └── ... (24 more)
│   │   ├── web/               ← 20 Thymeleaf controllers
│   │   │   ├── ManagerController.java              (34KB) ← largest
│   │   │   ├── TourGuideController.java            (28.7KB)
│   │   │   ├── ReceptionistController.java         (26.5KB)
│   │   │   ├── ReceptionistCheckinWebController.java (20.2KB)
│   │   │   ├── ProfileController.java              (19.2KB)
│   │   │   └── ... (15 more)
│   │   └── EmailPreviewController.java
│   ├── services/
│   │   ├── interfaces/        ← 35 interfaces
│   │   └── impl/              ← 37 implementations
│   │       ├── BookingServiceImpl.java          (57KB) ← largest
│   │       ├── AdminViewServiceImpl.java        (36.6KB)
│   │       ├── WalkInCheckInServiceImpl.java    (33.9KB)
│   │       ├── VnPayServiceImpl.java            (30.8KB)
│   │       ├── EmailServiceImpl.java            (28.6KB)
│   │       ├── PosServiceImpl.java              (23.7KB)
│   │       ├── TourBookingServiceImpl.java      (23.8KB)
│   │       ├── WorkflowEngineServiceImpl.java   (21.3KB)
│   │       └── ... (29 more)
│   ├── repositories/          ← 44 Spring Data JPA repos
│   ├── models/                ← 49 JPA entities
│   ├── dto/                   ← 25+ DTOs
│   ├── security/              ← 4 files (UserDetails, RoleConstants, AuthEvents)
│   ├── exceptions/            ← BusinessException
│   ├── events/                ← Spring Application Events
│   ├── listeners/             ← Event Listeners
│   └── utils/                 ← @LogActivity annotation, helpers
```

---

*Tài liệu ADR-01 được phân tích từ codebase thực tế tại `05-Development/kawai-backend/` của dự án **Kawai Retreat Resort & Hub**, Group 2 — SWP391 SE2023. Ngày: 2026-06-29.*
