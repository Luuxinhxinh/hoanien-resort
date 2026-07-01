# REQUIREMENTS TRACEABILITY MATRIX (RTM)

## Kawai Retreat Resort & Hub — Hệ thống Quản lý Nghỉ dưỡng Tích hợp

**Phiên bản:** 4.0  
**Ngày cập nhật:** 2026-07-02  
**Người phân tích:** Lead Architect & Tech Lead  
**Nguồn tài liệu đối chiếu:** [UC_MASTER_TABLE.md](file:///d:/SWP391/su26-swp391-se2023-g2/02-Requirement/UC_MASTER_TABLE.md) (v4.0) · [BusinessRule.md](file:///d:/SWP391/su26-swp391-se2023-g2/02-Requirement/BusinessRule.md) (v4.0) · Codebase thực tế (`/src/main/java/com/kawai/`)

---

## Mục lục

- [1. Giới thiệu](#1-giới-thiệu)
- [2. Ma trận RTM — Use Cases ↔ Business Rules ↔ Functional Modules](#2-ma-trận-rtm--use-cases--business-rules--functional-modules)
- [3. Ma trận RTM — Business Rules ↔ Non-UI System Functions](#3-ma-trận-rtm--business-rules--non-ui-system-functions)
- [4. Ma trận RTM — Use Cases ↔ Database Entities](#4-ma-trận-rtm--use-cases--database-entities)
- [5. Ma trận Phủ sóng Business Rules theo Use Case](#5-ma-trận-phủ-sóng-business-rules-theo-use-case)
- [6. Phân tích Khoảng trống (Gap Analysis)](#6-phân-tích-khoảng-trống-gap-analysis)
- [7. Tổng kết Phân phối theo Mức độ ưu tiên](#7-tổng-kết-phân-phối-theo-mức-độ-ưu-tiên)

---

## 1. Giới thiệu

### Mục đích
Requirements Traceability Matrix (RTM) phiên bản 4.0 được thiết kế để kết nối trực tiếp **Codebase hiện hữu** với **Tài liệu Đặc tả nghiệp vụ**. Mục tiêu chính là đảm bảo mọi Use Case được triển khai trong hệ thống:
1. Được bảo vệ bởi ít nhất một **Business Rule** tương ứng.
2. Được hỗ trợ bởi các **Non-UI System Functions** (các Filter, Scheduler, Service).
3. Được lưu trữ và ghi nhận trong đúng các **Database Entities** (trong số 49 thực thể JPA).

### Quy ước định danh
* **UCxx** / **UCxx.x**: Định danh Use Case và Sub-Use Case lấy trực tiếp từ [UC_MASTER_TABLE.md](file:///d:/SWP391/su26-swp391-se2023-g2/02-Requirement/UC_MASTER_TABLE.md).
* **BR-xxx-xx**: Business Rule ID từ [BusinessRule.md](file:///d:/SWP391/su26-swp391-se2023-g2/02-Requirement/BusinessRule.md).
* **NF-xx**: Non-UI System Function (Các cơ chế xử lý ngầm, logic bảo mật, bộ lọc).

---

## 2. Ma trận RTM — Use Cases ↔ Business Rules ↔ Functional Modules

### 2.1 MOD1: Hệ thống Cốt lõi, Xác thực & Admin Config

| Use Case ID | Tên Use Case | Business Rules Liên quan | Actors | Mức độ | Ghi chú kỹ thuật (Codebase) |
|:---|:---|:---|:---|:---:|:---|
| **UC01** | Quản lý Tài khoản & Xác thực | BR-SYS-01, BR-SYS-02, BR-SYS-03, BR-SYS-06, BR-SYS-07, BR-SYS-08 | All Users, Guest, Admin | CRITICAL | Custom Web Filter, `SecurityConfig.java`, BCrypt hash |
| **UC02** | Đặt lại mật khẩu | BR-SYS-01, BR-SYS-02 | All Users | HIGH | Email Reset Token, `PasswordResetToken` entity |
| **UC03** | Quản lý hồ sơ cá nhân & avatar | BR-SYS-01, BR-DATA-02 | Customer | HIGH | `DependentService.java`, JPA mapping |
| **UC04** | FaceID — Đăng ký & nhận diện | BR-TR-02, BR-FIN-07 | Customer, Tour Guide | HIGH | Python Face vector matching, `FaceIdApiController` |
| **UC05** | Phân quyền & An ninh nội bộ | BR-SYS-04, BR-SYS-07, BR-SYS-08 | Admin | CRITICAL | `@LogActivity`, Custom revision listener (Envers), JWT |
| **UC06** | Master Data — Hạng phòng & Phòng | BR-DATA-01, BR-FO-04 | Admin | HIGH | `RoomApiController.java`, validation ràng buộc xóa |
| **UC07** | Master Data — Sơ đồ bàn ăn | BR-DATA-01, BR-FB-03 | Admin, Manager, F&B | MEDIUM | `TableApiController.java`, CRUD & state update |
| **UC08** | Master Data — Tour & Lịch trình | BR-DATA-01, BR-TR-01 | Admin, Manager | HIGH | `TourController.java`, itinerary configurations |
| **UC09** | Giá, Marketing & Vận hành Admin | BR-FIN-05, BR-FIN-06, BR-SYS-09, BR-SYS-10 | Admin, Manager | HIGH | Dynamic pricing scheduler, workflow approval engines |

### 2.2 MOD2: Quản lý Phòng, Lễ tân & Buồng phòng

| Use Case ID | Tên Use Case | Business Rules Liên quan | Actors | Mức độ | Ghi chú kỹ thuật (Codebase) |
|:---|:---|:---|:---|:---:|:---|
| **UC10** | Tìm kiếm phòng trống & giá | BR-FO-01 | Guest, Customer | HIGH | `RoomService.java` query phòng theo ngày |
| **UC11** | Khóa giữ phòng tạm (Cart Lock) | BR-FO-01, BR-FO-02 | Customer, System | CRITICAL | `holdExpiresAt` timestamp, Cronjob clean-up |
| **UC12** | Nghiệp vụ Sảnh (Front Desk) | BR-FO-03, BR-FO-04, BR-FO-06, BR-FO-07, BR-FO-08, BR-FIN-01, BR-FIN-06 | Receptionist, Customer | CRITICAL | Walk-in check-in, Room Matrix, OCR CCCD, Folio, Đổi phòng |
| **UC13** | Buồng phòng & Bảo trì | BR-HK-01, BR-HK-02, BR-FO-04, BR-FO-05 | Housekeeper, Maintainer | HIGH | `HousekeepingService.java`, Task creation triggers |

### 2.3 MOD3: F&B, POS & KDS

| Use Case ID | Tên Use Case | Business Rules Liên quan | Actors | Mức độ | Ghi chú kỹ thuật (Codebase) |
|:---|:---|:---|:---|:---:|:---|
| **UC14** | Đặt giữ bàn nhà hàng | BR-FB-03 | Customer, Receptionist | MEDIUM | `TableReservationService.java`, hold table 30 mins |
| **UC15** | Cấu hình thực đơn & nhãn dị ứng | BR-DATA-01 | Admin, Manager | MEDIUM | Allergy flags, items toggle status |
| **UC16** | Đặt món Room Service / E-Menu | BR-FB-01, BR-FO-06 | Customer, Guest | HIGH | Guest validation, VNPay, Post-to-Room |
| **UC17** | POS Dine-In — lên đơn tại bàn | BR-FB-04 | F&B Staff, Cashier | HIGH | Order creation via `PosApiController.java` |
| **UC18** | Tất toán POS / Post-to-Room | BR-FB-01, BR-FB-04, BR-FO-06 | Cashier, F&B Staff | HIGH | Credit limit check, Folio items writing |
| **UC19** | Màn hình bếp KDS | BR-FB-02, BR-FB-04 | Kitchen Staff | HIGH | REST Polling, updates pending/cooking/ready status |

### 2.4 MOD4: Tour, Add-ons & Đánh giá

| Use Case ID | Tên Use Case | Business Rules Liên quan | Actors | Mức độ | Ghi chú kỹ thuật (Codebase) |
|:---|:---|:---|:---|:---:|:---|
| **UC20** | Tìm tour + weather | — | Guest, Customer | MEDIUM | OpenWeather API client integration |
| **UC21** | Đặt vé tour | BR-TR-01 | Customer, Receptionist | HIGH | Capacity validator check trước khi book |
| **UC22** | Điều hành Tour | BR-TR-01, BR-TR-02, BR-TR-05, BR-TR-06, BR-TR-07, BR-TR-08, BR-TR-09 | Tour Guide, System | HIGH | GPS logging, Route checkpoints status, FaceID, Manual log |
| **UC23** | Add-ons dịch vụ gia tăng | BR-FB-05, BR-FO-06 | Customer, Receptionist | MEDIUM | `AddOnServiceApiController.java`, Spa/Transfer bookings |
| **UC24** | Gửi đánh giá sao & feedback | BR-TR-03, BR-TR-04 | Customer | MEDIUM | Check validity (đã checkout mới cho đánh giá) |
| **UC25** | Kiểm duyệt review | BR-TR-04, BR-SYS-04 | Admin | HIGH | Toggle `isVisible` review status, audit log |

### 2.5 MOD5: Folio, Tài chính & Báo cáo

| Use Case ID | Tên Use Case | Business Rules Liên quan | Actors | Mức độ | Ghi chú kỹ thuật (Codebase) |
|:---|:---|:---|:---|:---:|:---|
| **UC26** | Folio Aggregation | BR-FIN-01, BR-FO-06, BR-MEM-01 | Customer, Receptionist | CRITICAL | Ví nợ phòng, nợ trần, Loyalty upgrade, Split Folio |
| **UC27** | Night Audit & Thanh toán | BR-FIN-01, BR-FIN-02, BR-FIN-03, BR-FIN-04, BR-FIN-08 | Receptionist, System | CRITICAL | Night audit posting, VNPay return/IPN, PDF invoice mail |
| **UC28** | Dashboard Manager & Báo cáo | BR-FIN-03, BR-FIN-04, BR-SYS-10 | Manager | HIGH | USALI reports, PDF/Excel export history |

### 2.6 MOD6: Hệ thống & Tích hợp

| Use Case ID | Tên Use Case | Business Rules Liên quan | Actors | Mức độ | Ghi chú kỹ thuật (Codebase) |
|:---|:---|:---|:---|:---:|:---|
| **UC29** | Hệ thống email thông báo | BR-SYS-01, BR-SYS-02 | System, Admin | HIGH | `EmailServiceImpl.java` (SendGrid / SMTP) |
| **UC30** | Scheduled Jobs tự động | BR-FO-02, BR-FB-03 | System | HIGH | `CronjobApiController.java`, `DynamicJobManager` |
| **UC31** | Landing pages & trải nghiệm | — | Guest, Customer | HIGH | Guest view templates, online booking history portal |

---

## 3. Ma trận RTM — Business Rules ↔ Non-UI System Functions

| Business Rule | Non-UI Function ID | Tên System Function | Mô tả kỹ thuật (Backend implementation) |
|:---|:---:|:---|:---|
| **BR-SYS-01** | NF-01 | Password_Encryption_Filter | BCrypt hashing cho mật khẩu; AES-256 cho CCCD/Passport |
| **BR-SYS-02** | NF-01 | Password_Encryption_Filter | Xác thực BCrypt khi đăng nhập; account lockout |
| **BR-SYS-03** | NF-02 | Role_Based_Routing_Engine | Spring Security filter; session timeout |
| **BR-SYS-04** | NF-15 | AOP_Data_Interception_Logger | Spring AOP intercept; ghi Audit_Logs |
| **BR-SYS-07** | NF-02 | Role_Based_Routing_Engine | RBAC routing theo role_name trong GrantedAuthority |
| **BR-SYS-08** | NF-17 | Device_Auth_Filter | Custom Security filter kiểm tra MAC/Device Code |
| **BR-SYS-09** | NF-18 | Workflow_Rule_Evaluator | Spring Expression Language (SpEL) parse điều kiện JSON |
| **BR-SYS-10** | NF-19 | Audit_Export_Logger | Ghi nhật ký vào bảng `ExportHistory` khi xuất báo cáo |
| **BR-FO-01** | NF-03 | Hold_Room_Expiry_Scanner | SELECT...FOR UPDATE; @Version Optimistic Lock |
| **BR-FO-02** | NF-03 | Hold_Room_Expiry_Scanner | Scheduler 5 phút; hủy booking Pending > 15 phút |
| **BR-FO-04** | NF-05, NF-11 | Housekeeping_Task_Trigger, Room_Status_State_Machine | Event listener / DB Trigger tự sinh task dọn dẹp |
| **BR-FO-05** | NF-11 | Room_Status_State_Machine | Rush Room = Set priority thành `URGENT` |
| **BR-FO-06** | NF-06 | Credit_Limit_Realtime_Validator | Constraint Validator; check tổng Folio + đơn mới |
| **BR-FO-07** | NF-11 | Room_Status_State_Machine | Đổi hạng phòng: recalculate daily rate + surcharges |
| **BR-FO-08** | NF-12 | Walkin_Booking_Processor | Tạo hóa đơn và post 100% tiền ngay lập tức |
| **BR-FB-01** | NF-06 | Credit_Limit_Realtime_Validator | Kiểm tra PIN hash + Credit Limit trước khi post Folio |
| **BR-FB-02** | NF-07, NF-08 | Kitchen_Notification_Dispatcher, Menu_Item_Availability_Sync | WebSocket broadcast; is_available=false đồng bộ KDS |
| **BR-TR-01** | NF-03 | Hold_Room_Expiry_Scanner | capacity validator trước khi lưu tour booking |
| **BR-TR-02** | NF-09, NF-10 | AI_Face_Vector_Matching_Service, Attendance_Status_Synchronizer | Python microservice; Cosine Similarity >= 0.85 |
| **BR-TR-05** | NF-03 | Hold_Room_Expiry_Scanner | Automated cancellation 24h trước khởi hành nếu thiếu khách |
| **BR-TR-06** | NF-21 | GPS_Location_Logger | Thu thập tọa độ thiết bị của HDV gửi về qua REST API |
| **BR-TR-07/08/09**| NF-22 | Itinerary_Checkpoint_Tracker | Ghi nhận mốc hành trình và trạng thái checkpoint tour |
| **BR-MEM-01** | NF-23 | Loyalty_Points_Calculator | Tự động cộng điểm và cập nhật rank tier thành viên |
| **BR-FIN-01** | NF-13 | USALI_Revenue_Decomposer | Kiểm tra số dư Folio = 0 trước khi cho phép checkout |
| **BR-FIN-02** | NF-13 | USALI_Revenue_Decomposer | Hủy cọc: Refund request generator |
| **BR-FIN-03** | NF-14 | Night_Audit_Core_Engine | Chạy 02:00 AM; post room charges; chốt ngày kinh doanh |
| **BR-FIN-04** | NF-13 | USALI_Revenue_Decomposer | Ghi chép doanh thu phân rã theo phòng/F&B/tour |
| **BR-FIN-05** | NF-24 | Dynamic_Price_Evaluator | Áp dụng giá trị phụ trội theo mùa/cuối tuần |
| **BR-FIN-06** | NF-24 | Dynamic_Price_Evaluator | Kiểm tra tính hiệu lực và giá trị giảm giá voucher |
| **BR-FIN-07** | NF-20 | CCCD_OCR_Client | Python OCR integration via HTTP client & websocket broadcast |
| **BR-FIN-08** | NF-24 | Refund_Workflow_Bridge | Manager dashboard duyệt tiền hoàn, đổi trạng thái booking |
| **BR-HK-01** | NF-05 | Housekeeping_Task_Trigger | Đánh giá trạng thái phòng sau task dọn dẹp hoàn tất |
| **BR-HK-02** | NF-11 | Room_Status_State_Machine | Room -> Maintenance state; block booking |
| **BR-DATA-01** | NF-15 | AOP_Data_Interception_Logger | Chặn xóa các danh mục đang có hoạt động giao dịch |
| **BR-DATA-02** | NF-01 | Password_Encryption_Filter | AES-256 mã hóa số CCCD trước khi ghi xuống DB |
| **BR-DATA-03** | NF-02 | Role_Based_Routing_Engine | @Transactional đảm bảo an toàn ghi cụm Account+Employee |

---

## 4. Ma trận RTM — Use Cases ↔ Database Entities

| Use Case ID | Database Entities Bị tác động | Loại thao tác |
|:---|:---|:---|
| **UC01** | Accounts, Customers, AuthorizedDevices, Roles, AuditLogs | INSERT, SELECT, UPDATE |
| **UC02** | Accounts, PasswordResetTokens | SELECT, UPDATE, INSERT |
| **UC03** | Customers, Dependents | INSERT, UPDATE, SELECT |
| **UC04** | Customers, TourAttendees | SELECT, UPDATE |
| **UC05** | Roles, AuditLogs, CustomRevisionEntities, AuthorizedDevices | INSERT, SELECT |
| **UC06** | RoomCategories, Rooms, AuditLogs | INSERT, UPDATE, SELECT |
| **UC07** | RestaurantTables | INSERT, UPDATE, SELECT |
| **UC08** | Tours, TourSchedules, TourItineraries, TourLocations | INSERT, UPDATE, SELECT |
| **UC09** | DynamicPricing, DailyRates, RoomSurcharges, Promotions, MenuItems, Workflows | INSERT, UPDATE, SELECT |
| **UC10** | Rooms, DailyRates, RoomBookingDetails | SELECT |
| **UC11** | Bookings, RoomBookingDetails | INSERT, UPDATE |
| **UC12** | Bookings, RoomBookings, RoomBookingDetails, Customers, Rooms, PaymentTransactions, RoomGuests, RoomSurcharges, ConsolidatedInvoices, FolioItems | INSERT, UPDATE, SELECT |
| **UC13** | Rooms, StaffSchedules, Shifts | UPDATE, INSERT, SELECT |
| **UC14** | TableReservations, RestaurantTables | INSERT, UPDATE, SELECT |
| **UC15** | MenuItems | INSERT, UPDATE |
| **UC16** | FoodOrders, FoodOrderDetails, BookingServices, FolioItems | INSERT, UPDATE |
| **UC17** | FoodOrders, FoodOrderDetails | INSERT, SELECT |
| **UC18** | FoodOrders, FolioItems, PaymentTransactions | INSERT, UPDATE |
| **UC19** | FoodOrderDetails | SELECT, UPDATE |
| **UC20** | Tours | SELECT |
| **UC21** | TourBookings, TourAttendees, PaymentTransactions | INSERT, UPDATE |
| **UC22** | TourSchedules, TourAttendees, CheckpointAttendance, TourItineraries, TourItineraryDetails, TourLocations, RunItineraryStatuses, TourImages, TourPrices, TourStaffAssignments | INSERT, UPDATE, SELECT |
| **UC23** | HotelServices, BookingServices, FolioItems | INSERT, UPDATE, SELECT |
| **UC24** | Reviews | INSERT |
| **UC25** | Reviews, AuditLogs | UPDATE, INSERT |
| **UC26** | FolioItems, ConsolidatedInvoices, MembershipTiers, Customers | INSERT, UPDATE, SELECT |
| **UC27** | FolioItems, ConsolidatedInvoices, PaymentTransactions, RefundRequests, Bookings | INSERT, UPDATE, SELECT |
| **UC28** | ConsolidatedInvoices, FolioItems, ExportHistories | SELECT, INSERT |
| **UC29** | Accounts | SELECT |
| **UC30** | Bookings, TableReservations, AuditLogs | UPDATE, DELETE |
| **UC31** | Bookings | SELECT |

---

## 5. Ma trận Phủ sóng Business Rules theo Use Case

Bảng dưới đây thể hiện các Business Rules được thực thi trong mỗi Use Case chính.  
**Ký hiệu:** ✅ = BR bắt buộc áp dụng trong UC này | 〇 = BR có liên quan gián tiếp | — = Không áp dụng

| Business Rule | UC01 / UC05 | UC11 (Cart Lock) | UC12 (Front Desk) | UC13 (Housekeep) | UC16 / UC18 (POS/Folio) | UC22 (Tour Ops) | UC27 (Night Audit) | UC28 (Reports) |
|:---|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| **BR-SYS-01** | ✅ | — | 〇 | — | 〇 | — | — | — |
| **BR-SYS-02** | ✅ | — | — | — | — | — | — | — |
| **BR-SYS-03** | ✅ | — | ✅ | — | ✅ | — | — | — |
| **BR-SYS-04** | ✅ | 〇 | ✅ | 〇 | ✅ | ✅ | ✅ | ✅ |
| **BR-SYS-07** | ✅ | — | 〇 | — | 〇 | 〇 | 〇 | 〇 |
| **BR-SYS-08** | ✅ | — | 〇 | — | — | — | — | — |
| **BR-SYS-09** | ✅ | — | ✅ | — | — | — | — | — |
| **BR-SYS-10** | — | — | — | — | — | — | — | ✅ |
| **BR-FO-01**  | — | ✅ | — | — | — | — | — | — |
| **BR-FO-02**  | — | ✅ | — | — | — | — | — | — |
| **BR-FO-03**  | — | — | ✅ | — | — | — | — | — |
| **BR-FO-04**  | — | — | ✅ | ✅ | — | — | — | — |
| **BR-FO-05**  | — | — | 〇 | ✅ | — | — | — | — |
| **BR-FO-06**  | — | — | ✅ | — | ✅ | — | — | — |
| **BR-FO-07**  | — | — | ✅ | — | — | — | — | — |
| **BR-FO-08**  | — | — | ✅ | — | — | — | — | — |
| **BR-FB-01**  | — | — | — | — | ✅ | — | — | — |
| **BR-FB-02**  | — | — | — | — | 〇 | — | — | — |
| **BR-FB-03**  | — | — | — | — | — | — | — | — |
| **BR-TR-01**  | — | — | — | — | — | ✅ | — | — |
| **BR-TR-02**  | — | — | — | — | — | ✅ | — | — |
| **BR-TR-05**  | — | — | — | — | — | ✅ | — | — |
| **BR-TR-06**  | — | — | — | — | — | ✅ | — | — |
| **BR-TR-07/8/9**| — | — | — | — | — | ✅ | — | — |
| **BR-MEM-01** | — | — | 〇 | — | ✅ | — | 〇 | — |
| **BR-FIN-01** | — | — | ✅ | — | — | — | ✅ | — |
| **BR-FIN-02** | — | — | ✅ | — | — | — | ✅ | — |
| **BR-FIN-03** | — | — | — | — | — | — | ✅ | — |
| **BR-FIN-04** | — | — | ✅ | — | ✅ | ✅ | ✅ | 〇 |
| **BR-FIN-05** | — | — | 〇 | — | — | — | — | — |
| **BR-FIN-06** | — | — | ✅ | — | — | — | — | — |
| **BR-FIN-07** | — | — | ✅ | — | — | — | — | — |
| **BR-FIN-08** | — | — | ✅ | — | — | — | ✅ | — |
| **BR-HK-01**  | — | — | — | ✅ | — | — | — | — |
| **BR-HK-02**  | — | — | — | ✅ | — | — | — | — |
| **BR-DATA-01**| 〇 | — | — | — | — | — | — | — |
| **BR-DATA-02**| — | — | ✅ | — | — | — | — | — |
| **BR-DATA-03**| ✅ | — | — | — | — | — | — | — |

---

## 6. Phân tích Khoảng trống (Gap Analysis)

### 6.1 Yêu cầu Nghiệp vụ Chưa có Use Case Phụ trợ
* **BR-FO-05 (Rush Room):** Codebase đã hỗ trợ ưu tiên dọn dẹp trong `HousekeepingService`, tuy nhiên trên luồng Web UI cần một màn hình thao tác "Đánh dấu dọn gấp" cụ thể thuộc về `UC13` để nhân viên lễ tân gửi lệnh xuống buồng phòng.
* **BR-FB-03 (Tự động giải phóng bàn):** Logic scheduler hủy bàn sau 30 phút chưa được expose ra REST API cấu hình. Cần bổ sung UI cấu hình tham số thời gian giữ bàn trong Master Data nhà hàng (`UC15`).

### 6.2 Use Cases Chưa có Business Rule Bảo vệ Đầy đủ
* **UC04 (AI Face Scan):** Chưa có BR quy định độ tương đồng tối đa giữa vector khuôn mặt cũ và vector cập nhật mới để tránh việc tráo người. Khuyến nghị bổ sung BR: *"Độ tương đồng khuôn mặt khi đăng ký lại vector mới phải >= 70% so với ảnh CCCD ban đầu"*.
* **UC27 (Phát hành e-Invoice):** Chưa có BR ràng buộc thời gian xuất hóa đơn tối đa sau khi checkout. Khuyến nghị bổ sung BR: *"Hóa đơn e-Invoice bắt buộc phải được ký số và gửi email trong vòng 24h sau khi checkout thành công"*.

---

## 7. Tổng kết Phân phối theo Mức độ ưu tiên

### 7.1 Tổng số Requirements

| Loại | Số lượng |
|:-----|:--------:|
| Business Rules (BR) | 44 |
| Use Cases chính (Master Table) | 31 |
| Sub-Use Cases chi tiết | 108 |
| Non-UI System Functions | 25 |
| Database Entities chính | 49 |

### 7.2 Phân phối Business Rules theo Mức độ ưu tiên

| Mức độ | Số BR | Danh sách |
|:-------|:-----:|:----------|
| CRITICAL | 11 | BR-SYS-01, BR-SYS-04, BR-SYS-07, BR-FO-01, BR-FO-02, BR-FO-03, BR-FO-04, BR-FB-01, BR-TR-01, BR-FIN-01, BR-FIN-03, BR-DATA-02, BR-DATA-03 |
| HIGH | 14 | BR-SYS-02, BR-SYS-03, BR-SYS-05, BR-SYS-06, BR-SYS-08, BR-SYS-09, BR-SYS-10, BR-FO-06, BR-FB-02, BR-TR-02, BR-TR-04, BR-TR-05, BR-FIN-02, BR-FIN-04, BR-HK-01, BR-HK-02, BR-DATA-01 |
| MEDIUM | 4 | BR-FO-05, BR-FB-03, BR-FB-04, BR-TR-03, BR-FIN-05, BR-FIN-06 |

### 7.3 Phân phối Use Cases chính theo Phân hệ (Modules)

| Phân hệ (Module) | Số UC chính | Tỷ lệ |
|:--------|:-----:|:------:|
| MOD1: Core & Auth | 9 | 29.0% |
| MOD2: Phòng & Lễ tân | 4 | 12.9% |
| MOD3: F&B & POS | 6 | 19.4% |
| MOD4: Tour & Đánh giá | 6 | 19.4% |
| MOD5: Tài chính & Báo cáo | 3 | 9.7% |
| MOD6: Hệ thống & Tích hợp | 3 | 9.7% |
| **Tổng** | **31** | **100%** |

---

*Tài liệu RTM v4.0 được tổng hợp và phân tích trực quan hóa dựa trên codebase thực tế, loại bỏ hoàn toàn các tài liệu mô tả cũ để đảm bảo tính đồng bộ tuyệt đối cho dự án Kawai Retreat Resort & Hub, Group 2 — SWP391 SE2023.*
