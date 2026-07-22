# Package Descriptions

> Tài liệu mô tả các package trong hệ thống **Kawai Hotel Management System**.  
> Dựa trên sơ đồ cấu trúc package package_corrected.drawio và codebase thực tế.

---

## Package Descriptions Table

| No | Package | Description |
|:--:|---------|-------------|
| 01 | `com.kawai.config` | Contains Spring configuration beans: `SecurityConfig` (HTTP security, OAuth2, session management), `AuditLogAspect` (AOP logging), `CloudinaryConfig` (file upload), `VnPayConfig` (payment gateway), `WebMvcConfig`, `WebSocketConfig`, `GlobalSessionAdvice`, `DynamicJobManager`, `OAuth2SuccessHandler`, and data seeders. |
| 02 | `com.kawai.security` | Implements Spring Security custom components: `UserDetailsServiceImpl` (loads user by username via `AccountRepository`), `AuthenticationEvents` (handles login success/failure events), and `RolePermissionConstants` (permission constants registry). |
| 03 | `com.kawai.controllers.web` | Handles browser-facing HTTP requests and returns Thymeleaf views (`ModelAndView` / String). Contains 22 controllers covering: `ManagerController`, `ReceptionistController`, `TourGuideController`, `HousekeepingWebController`, `MaintenanceWebController`, `PosController`, `FeedbackController`, `ProfileController`, `AuthController`, `BookingController`, `OrderFoodController`, etc. |
| 04 | `com.kawai.controllers.api` | Exposes REST API endpoints (`@RestController`) returning JSON responses. Handles booking, payment (VNPay), folio, night audit, manager approvals/refunds/reports, master data, walk-in check-in, housekeeping, KDS, POS, tour booking, and system notifications. |
| 05 | `com.kawai.services.interfaces` | Declares service contracts as Java interfaces (`BookingService`, `FolioService`, `RoomService`, `CheckinService`, `WalkInCheckInService`, `NightAuditService`, `WorkflowEngineService`, `EmailService`, etc.). Acts as the abstraction boundary between controllers and implementations. Uses `models` and `dto` as method signatures. |
| 06 | `com.kawai.services.impl` | Provides concrete implementations of all service interfaces. Contains core business logic including workflow engine (`WorkflowEngineServiceImpl`), booking (`BookingServiceImpl`), payment (`VnPayServiceImpl`, `PaymentServiceImpl`), folio (`FolioServiceImpl`), email (`EmailServiceImpl`), POS, tour, housekeeping, etc. Directly uses `repositories`, `dto`, `events`, `exceptions`, and `utils`. |
| 07 | `com.kawai.repositories` | Contains Spring Data JPA repository interfaces (`JpaRepository` / `JpaSpecificationExecutor` extensions) for all entities. Provides CRUD operations and custom JPQL / Native SQL queries. Depends solely on `models`. |
| 08 | `com.kawai.models` | Defines all JPA `@Entity` classes mapped to database tables (e.g., `Booking`, `Room`, `Account`, `Employee`, `FolioItem`, `RoomBooking`, `Tour`, `WorkflowStatus`). Core domain layer — has no dependencies on other application packages. |
| 09 | `com.kawai.dto` | Contains Data Transfer Objects used to exchange data between layers without exposing entities. Organized with subdirectories: `fnb/` (F&B orders), `roomchange/` (room change requests), `walkin/` (walk-in check-in). Key examples: `BookingRequestDTO`, `RoomSearchRequestDTO`, `CheckinSubmitFormDTO`, `TourBookingRequest`, `SystemNotificationDTO`. |
| 10 | `com.kawai.dtos` | Secondary DTO package containing `CancelOrderRequestDTO`. (Note: a separate package from `com.kawai.dto` in the codebase.) |
| 11 | `com.kawai.events` | Defines custom Spring Application Events published via `ApplicationEventPublisher`: `ManagerApprovalEvent`, `SystemEmailEvent`, `CustomerCheckedOutEvent`. Also contains `CustomerCheckedOutEventListener` — an event listener co-located in this package. |
| 12 | `com.kawai.listeners` | Contains `@EventListener` / `@TransactionalEventListener` classes reacting to application events: `ApprovalFinancialListener` (handles folio/financial side of approvals), `ApprovalInventoryListener` (handles inventory updates), `SystemEmailEventListener` (sends email notifications). Depends on `services.interfaces` and `repositories`. |
| 13 | `com.kawai.schedulers` | Houses Spring `@Scheduled` jobs: currently `NightAuditJob` triggers automated nightly audit processing. Depends on `services.interfaces` (`NightAuditService`). |
| 14 | `com.kawai.exceptions` | Defines custom exception classes: `BusinessException`, `RoomNotAvailableException`, `PaymentGatewayException`, and the centralized `GlobalExceptionHandler` (`@ControllerAdvice`) that handles both REST (JSON) and Web (Thymeleaf) error responses. |
| 15 | `com.kawai.utils` | Provides stateless utility / helper classes: `VnPayUtil` (payment signature), `EncryptionUtils` (data encryption), `SecurityUtils` (authentication context), `ValidationUtils` (input validation), `UploadPathResolver` (file path resolution), `LogActivity` (annotation for audit logging). |
| 16 | `templates` | Thymeleaf HTML template files under `src/main/resources/templates/`. Organized by role: `manager/`, `receptionist/`, `housekeeping/`, `kitchenStaff/`, `f&bStaff/`, `maintenance/`, `tour/`, `admin/`, `guest/`, `email/` (email templates), `error/` (error pages). Also includes `ops-login.html` at root level. |

---

*Cross-verified against codebase at `com.kawai.*` and `src/main/resources/templates/` — Kawai Hotel Management System*
