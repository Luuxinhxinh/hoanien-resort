# REQUIREMENTS TRACEABILITY MATRIX (RTM)

## Kawai Retreat Resort & Hub — Hệ thống Quản lý Nghỉ dưỡng Tích hợp

**Phiên bản:** 1.0  
**Ngày tạo:** 2026-06-29  
**Người phân tích:** Business Analyst (AI-assisted)  
**Nguồn tài liệu:** SRS_Document_SWP391_G2.md · Project_Specification.md · BusinessRule.md

---

## Mục lục

- [1. Giới thiệu](#1-giới-thiệu)
- [2. Ma trận RTM — Use Cases ↔ Business Rules ↔ Functional Requirements](#2-ma-trận-rtm--use-cases--business-rules--functional-requirements)
- [3. Ma trận RTM — Business Rules ↔ Non-UI System Functions](#3-ma-trận-rtm--business-rules--non-ui-system-functions)
- [4. Ma trận RTM — Use Cases ↔ Database Entities](#4-ma-trận-rtm--use-cases--database-entities)
- [5. Ma trận Phủ sóng Business Rules theo Use Case](#5-ma-trận-phủ-sóng-business-rules-theo-use-case)
- [6. Phân tích Khoảng trống (Gap Analysis)](#6-phân-tích-khoảng-trống-gap-analysis)
- [7. Tổng kết Phân phối theo Mức độ ưu tiên](#7-tổng-kết-phân-phối-theo-mức-độ-ưu-tiên)

---

## 1. Giới thiệu

### Mục đích
Requirements Traceability Matrix (RTM) đảm bảo mọi **yêu cầu nghiệp vụ** đều được:
1. Ánh xạ đến ít nhất một **Use Case** thực thi nó.
2. Được bảo vệ bởi ít nhất một **Business Rule** kiểm soát nó.
3. Được hỗ trợ bởi ít nhất một **Functional Requirement** (màn hình / chức năng).
4. Được lưu trữ trong ít nhất một **Database Entity**.

### Quy ước

| Ký hiệu | Ý nghĩa |
|---------|---------|
| UC-xx | Use Case ID từ SRS |
| BR-xxx-xx | Business Rule ID từ BusinessRule.md |
| UC[Spec]xx | Use Case từ Project Specification |
| FR-xx | Functional Requirement (Screen/Function) |
| NF-xx | Non-UI System Function |
| CRITICAL | Mức ưu tiên cao nhất — vi phạm gây hậu quả pháp lý/tài chính nghiêm trọng |
| HIGH | Mức ưu tiên cao — ảnh hưởng vận hành và trải nghiệm khách |
| MEDIUM | Mức ưu tiên trung bình |

---

## 2. Ma trận RTM — Use Cases ↔ Business Rules ↔ Functional Requirements

### 2.1 Phân hệ Xác thực & Quản trị Tài khoản

| STT | Use Case | UC ID (SRS) | UC ID (Spec) | Business Rules Liên quan | Actors | Mức độ | Ghi chú |
|:---:|:---------|:-----------:|:------------:|:------------------------|:------:|:------:|:--------|
| 1 | Đăng ký Tài khoản | UC-02 | UC01.1 | BR-SYS-01, BR-SYS-06 | Guest | HIGH | BCrypt mật khẩu; validate email, phone |
| 2 | Đăng nhập Hệ thống | UC-01 | UC01.2 | BR-SYS-01, BR-SYS-02, BR-SYS-03, BR-SYS-07 | All Users | CRITICAL | Session timeout; khóa sau 5 lần sai; RBAC routing |
| 3 | Quên / Đặt lại Mật khẩu | UC-04 | UC02 | BR-SYS-01, BR-SYS-02 | Customer, All Users | HIGH | Token 15 phút, 1 lần dùng |
| 4 | Xác thực OTP | UC-03 | UC01.2 | BR-SYS-02 | All Users | HIGH | OTP hết hạn 3 phút |
| 5 | Quản lý Hồ sơ & CCCD | UC-05 | UC03.1 | BR-SYS-01, BR-DATA-02 | Customer | HIGH | AES-256 mã hóa CCCD/Passport |
| 6 | Đăng tải FaceID | UC-05 | UC03.2 | BR-TR-02 | Customer, Receptionist | HIGH | 128-dim face vector; ảnh 1 mặt, rõ nét |
| 7 | Quản lý Tài khoản Nhân viên | UC-08 | UC04.1 | BR-SYS-07, BR-DATA-03 | Admin | CRITICAL | RBAC; @Transactional khi tạo Account+Employee |
| 8 | Theo dõi Audit Log | UC-32 | UC04.2 | BR-SYS-04 | Admin | CRITICAL | INSERT-only; AOP interceptor |
| 9 | Quản lý Dữ liệu Gốc (CRUD) | UC-09 | UC05.1 | BR-DATA-01 | Admin | HIGH | Không xóa phòng/bàn/tour đang hoạt động |
| 10 | Cấu hình Giá Động | UC-40 | UC05.2 | BR-FIN-05, BR-SYS-04 | Admin, Manager | MEDIUM | Không overlap ngày; ghi audit log |
| 11 | Thiết lập Khuyến mãi | UC-38 | UC05.3 | BR-FIN-06 | Admin, Manager | MEDIUM | 1 booking 1 voucher; kiểm tra validity |
| 12 | Yêu cầu Xóa Dữ liệu Cá nhân | UC-07 | — | BR-SYS-05 | Customer | HIGH | Anonymization; giữ lại transaction records |
| 13 | Nâng cấp Dependent → Customer | UC-06 | — | BR-FO-03 | Receptionist | MEDIUM | Người >= 18 tuổi mới được nâng cấp |

---

### 2.2 Phân hệ Đặt phòng & Tiền sảnh

| STT | Use Case | UC ID (SRS) | UC ID (Spec) | Business Rules Liên quan | Actors | Mức độ | Ghi chú |
|:---:|:---------|:-----------:|:------------:|:------------------------|:------:|:------:|:--------|
| 14 | Tìm kiếm Phòng trống | UC-10 | UC06 | BR-FO-01 | Guest, Customer | HIGH | Loại trừ phòng đã book trong khoảng ngày |
| 15 | Đặt phòng & Thanh toán Cọc | UC-11 | UC07.1 | BR-FO-01, BR-FO-02 | Customer | CRITICAL | Cart Lock 15'; VNPay; Optimistic Locking |
| 16 | Áp dụng Voucher | UC-38 | UC07.2 | BR-FIN-06 | Customer | MEDIUM | Max 1 voucher/booking; kiểm tra is_active & validity |
| 17 | Giữ chỗ Tạm thời | — | UC07.3 | BR-FO-01, BR-FO-02 | System | CRITICAL | TTL 15 phút; tự động hủy nếu hết hạn |
| 18 | Hủy Đặt phòng | UC-06 | — | BR-FIN-02 | Customer | HIGH | >48h: hoàn 100%; <=48h: mất cọc |
| 19 | Xem Sơ đồ Phòng (Room Matrix) | UC-12 | UC08 | BR-FO-04 | Receptionist | HIGH | Realtime WebSocket; màu theo trạng thái |
| 20 | Check-in & Gán Phòng | UC-13 | UC09.1 | BR-FO-03, BR-FO-04, BR-DATA-02 | Receptionist | CRITICAL | OCR CCCD; chỉ gán phòng Vacant_Clean |
| 21 | Ủy quyền Hạn mức Chi tiêu | UC-14 | UC09.2 | BR-FO-06, BR-FB-01 | Receptionist | HIGH | sub_credit_limit <= credit_limit; PIN BCrypt |
| 22 | Đổi Phòng | — | UC09.3 | BR-FO-04, BR-SYS-04 | Receptionist | MEDIUM | Phòng mới phải Vacant_Clean; ghi audit log |
| 23 | Khai báo Người lưu trú | — | UC09.4 | BR-FO-03, BR-DATA-02 | Customer, Receptionist | CRITICAL | Bắt buộc >= 1 người lớn; thu thập đủ CCCD |
| 24 | Thanh toán Cọc Online | UC-14 | — | BR-FO-02 | Customer | CRITICAL | VNPay Sandbox; verify webhook signature |
| 25 | Check-out & Thanh toán Cuối | UC-08 | UC22.1 | BR-FIN-01, BR-FO-04, BR-HK-01 | Receptionist | CRITICAL | Phải tất toán hết Folio; trigger dọn phòng |

---

### 2.3 Phân hệ F&B / POS / KDS

| STT | Use Case | UC ID (SRS) | UC ID (Spec) | Business Rules Liên quan | Actors | Mức độ | Ghi chú |
|:---:|:---------|:-----------:|:------------:|:------------------------|:------:|:------:|:--------|
| 26 | Đặt món Room Service | UC-15 | UC11 | BR-FB-01, BR-FO-06 | Customer | CRITICAL | Checked_In + PIN + Credit Limit check |
| 27 | Đặt Bàn trước | UC-19 | UC12 | BR-FB-03 | Customer | MEDIUM | Trước >= 1h; giữ tối đa 30 phút |
| 28 | Gọi món Dine-In (POS) | UC-16 | UC13 | BR-FB-04 | F&B Staff | HIGH | F&B Staff chỉ gọi món, không sửa giá |
| 29 | Theo dõi KDS (Kitchen) | UC-17 | UC14.1 | BR-FB-02, BR-FB-04 | Kitchen Staff | HIGH | Realtime WebSocket; ưu tiên Room Service |
| 30 | Cập nhật Trạng thái Chế biến | UC-17 | UC14.2 | BR-FB-04 | Kitchen Staff | HIGH | Pending → Cooking → Ready → Served |
| 31 | Báo Hết món | UC-18 | UC14.3 | BR-FB-02 | Kitchen Staff | HIGH | is_available=false; broadcast realtime toàn bộ POS |
| 32 | Ký nợ Hóa đơn về Phòng (Post to Room) | UC-20 | UC15 | BR-FB-01, BR-FO-06 | F&B Staff | CRITICAL | Kiểm tra PIN + Credit Limit; ghi Folio_Items |
| 33 | Thanh toán Trực tiếp (Dine-In) | UC-11 | UC11 (Pay Food) | — | F&B Staff | HIGH | Thanh toán ngay tại POS |

---

### 2.4 Phân hệ Lữ hành & Đánh giá

| STT | Use Case | UC ID (SRS) | UC ID (Spec) | Business Rules Liên quan | Actors | Mức độ | Ghi chú |
|:---:|:---------|:-----------:|:------------:|:------------------------|:------:|:------:|:--------|
| 34 | Tìm kiếm Tour (với thời tiết) | UC-22 | UC16 | — | Guest, Customer | MEDIUM | Gọi API OpenWeather |
| 35 | Đặt Tour & Thanh toán | UC-22 | UC17.1 | BR-TR-01 | Customer | CRITICAL | TRG_Tour_Capacity_Validator; không vượt max_capacity |
| 36 | Lập lịch Chuyến xe Tour | UC-21 | UC17.2 | BR-TR-01, BR-SYS-04 | Admin, Tour Guide | HIGH | Chống Double-booking nhân sự; ghi audit |
| 37 | Hủy Chuyến Tour (Khẩn cấp) | UC-23 | UC17.3 | BR-TR-05, BR-SYS-04 | Admin, Tour Guide | HIGH | Hoàn tiền 100%; gửi email; ghi audit |
| 38 | Tự động Hủy Tour Dưới ngưỡng | UC-23 | — | BR-TR-05 | System | HIGH | Kiểm tra 24h trước khởi hành |
| 39 | Đồng bộ Combo → Tour | — | UC17.4 | BR-TR-01 | System | MEDIUM | Auto-create Tour_Bookings từ Combo booking |
| 40 | Điểm danh AI Face Scan | UC-24 | UC18 | BR-TR-02 | Tour Guide | HIGH | Cosine Similarity >= 0.85; fallback manual |
| 41 | Gửi Đánh giá | UC-25 | UC19 | BR-TR-03, BR-TR-04 | Customer | MEDIUM | Trong 7 ngày; chỉ với dịch vụ đã dùng |
| 42 | Kiểm duyệt Đánh giá | UC-26 | UC20 | BR-TR-04, BR-SYS-04 | Admin | HIGH | Chỉ ẩn/hiện; không sửa nội dung; ghi audit |

---

### 2.5 Phân hệ Buồng phòng & Bảo trì

| STT | Use Case | UC ID (SRS) | UC ID (Spec) | Business Rules Liên quan | Actors | Mức độ | Ghi chú |
|:---:|:---------|:-----------:|:------------:|:------------------------|:------:|:------:|:--------|
| 43 | Auto Tạo Task Dọn phòng | UC-34 | UC10.1 | BR-HK-01, BR-FO-04 | System | HIGH | Trigger TRG_Auto_Housekeeping_Task |
| 44 | Cập nhật Tiến độ Dọn phòng | UC-35 | UC10.2 | BR-FO-04 | Housekeeping | HIGH | Task Complete → Room = Vacant_Clean |
| 45 | Điều phối Ưu tiên Dọn phòng | — | UC10.3 | BR-FO-05 | Receptionist | MEDIUM | Rush Room = Urgent priority |
| 46 | Báo hỏng Thiết bị | UC-36 | UC10.4 | BR-HK-02, BR-FO-04 | Housekeeping | HIGH | Room → Maintenance; tạo phiếu kỹ thuật |
| 47 | Sửa chữa & Giải phóng Phòng | UC-37 | UC10.5 | BR-HK-02, BR-FO-04 | Maintenance | HIGH | Hoàn thành → Vacant_Dirty hoặc Vacant_Clean |

---

### 2.6 Phân hệ Tài chính & Báo cáo

| STT | Use Case | UC ID (SRS) | UC ID (Spec) | Business Rules Liên quan | Actors | Mức độ | Ghi chú |
|:---:|:---------|:-----------:|:------------:|:------------------------|:------:|:------:|:--------|
| 48 | Theo dõi Folio (Ví nợ phòng) | UC-27 | UC21.1 | BR-FIN-01, BR-FO-06 | Customer, Receptionist | HIGH | Hiển thị chi tiết Folio_Items realtime |
| 49 | Ghi vết Lịch sử Giao dịch | UC-39 | UC21.2 | BR-SYS-04, BR-FIN-04 | System | CRITICAL | Payment_Transactions; phân loại nguồn thu |
| 50 | Tổng hợp Hóa đơn Checkout | UC-28 | UC21.3 | BR-FIN-01, BR-FIN-04 | System | CRITICAL | Gom Folio + Phòng + Tour; trừ cọc đã đặt |
| 51 | Kiểm toán Đêm (Night Audit) | UC-30 | UC21.4 | BR-FIN-03, BR-FIN-04 | System | CRITICAL | Chạy 02:00 AM; post room charges; đóng sổ |
| 52 | Tách Hóa đơn (Split Folio) | — | UC21.5 | BR-FIN-01 | Receptionist | MEDIUM | is_settled_separately flag; tách hóa đơn phụ |
| 53 | Thanh toán Check-out Cuối | UC-29 | UC22.1 | BR-FIN-01, BR-FO-04, BR-HK-01 | Receptionist | CRITICAL | Số dư = 0 mới cho checkout; trigger dọn phòng |
| 54 | Phát hành e-Invoice | — | UC22.2 | BR-FIN-04 | System | MEDIUM | PDF chữ ký số; gửi email tự động |
| 55 | Dashboard Tài chính | UC-31 | UC23.1 | BR-FIN-04 | Manager | HIGH | USALI: Room / F&B / Tour revenue charts |
| 56 | Theo dõi Công suất Phòng | — | UC23.2 | BR-FIN-04 | Manager | MEDIUM | Occupancy Rate = Occupied/Total * 100% |
| 57 | Báo cáo USALI | UC-33 | UC24 | BR-FIN-04, BR-SYS-04 | Manager | HIGH | GOP theo từng bộ phận; chuẩn quốc tế |
| 58 | Xuất Báo cáo PDF/Excel | UC-33 | UC25 | — | Manager | MEDIUM | Apache POI (.xlsx); OpenPDF (.pdf) |
| 59 | Theo dõi Tổng Doanh thu | UC-31 | UC23.1 | BR-FIN-03, BR-FIN-04 | Manager | HIGH | Theo ngày/tháng/năm |

---

## 3. Ma trận RTM — Business Rules ↔ Non-UI System Functions

| Business Rule | Non-UI Function ID | Tên System Function | Mô tả kỹ thuật |
|:---|:---:|:---|:---|
| BR-SYS-01 | NF-01 | Password_Encryption_Filter | BCrypt hashing cho mật khẩu; AES-256 cho CCCD/Passport |
| BR-SYS-02 | NF-01 | Password_Encryption_Filter | Xác thực BCrypt khi đăng nhập; account lockout |
| BR-SYS-03 | NF-02 | Role_Based_Routing_Engine | Spring Security filter; session timeout |
| BR-SYS-04 | NF-15 | AOP_Data_Interception_Logger | Spring AOP intercept; ghi Audit_Logs |
| BR-SYS-07 | NF-02 | Role_Based_Routing_Engine | RBAC routing theo role_name trong JWT Token |
| BR-FO-01 | NF-03 | Pending_Booking_Auto_Cancellation | SELECT...FOR UPDATE; @Version Optimistic Lock |
| BR-FO-02 | NF-03 | Pending_Booking_Auto_Cancellation | Scheduler 5 phút; hủy booking Pending > 15 phút |
| BR-FO-04 | NF-05, NF-11 | Housekeeping_Task_Trigger, Room_Status_State_Machine | DB Trigger kích hoạt khi checkout; state machine phòng |
| BR-FO-05 | NF-11 | Room_Status_State_Machine | Rush Room = Urgent priority trong task queue |
| BR-FO-06 | NF-06 | Credit_Limit_Realtime_Validator | Constraint Validator; kiểm tra tổng Folio + order mới |
| BR-FB-01 | NF-06 | Credit_Limit_Realtime_Validator | Kiểm tra PIN hash + Credit Limit trước khi post Folio |
| BR-FB-02 | NF-07, NF-08 | Kitchen_Notification_Dispatcher, Menu_Item_Availability_Sync | WebSocket broadcast; is_available=false đồng bộ toàn hệ thống |
| BR-FO-03 | NF-04 | Dependent_Account_Auto_Generation | Tự động tạo Account+Customer khi nâng cấp Dependent |
| BR-TR-01 | NF-03 | Pending_Booking_Auto_Cancellation | TRG_Tour_Capacity_Validator DB Trigger |
| BR-TR-02 | NF-09, NF-10 | AI_Face_Vector_Matching_Service, Attendance_Status_Synchronizer | Python microservice; Cosine Similarity >= 0.85 |
| BR-TR-05 | NF-03 | Pending_Booking_Auto_Cancellation | Automated cancellation 24h trước khởi hành |
| BR-FIN-01 | NF-13 | USALI_Revenue_Decomposer | Kiểm tra số dư Folio = 0 trước khi cho phép checkout |
| BR-FIN-03 | NF-13 | USALI_Revenue_Decomposer | Night Audit scheduler 02:00 AM; đóng sổ ngày |
| BR-FIN-04 | NF-13 | USALI_Revenue_Decomposer | Phân bổ Room / F&B / Tour revenue theo USALI |
| BR-FIN-06 | NF-14 | Voucher_Validity_Checker | Kiểm tra is_active, valid_to, max_uses, minimum order |
| BR-HK-01 | NF-05 | Housekeeping_Task_Trigger | TRG_Auto_Housekeeping_Task DB trigger |
| BR-HK-02 | NF-12 | Maintenance_Request_Auto_Bridge | Tự động tạo MAINTENANCE task + Room → Maintenance |
| BR-DATA-03 | NF-16 | Employee_Account_Transactional_Link | @Transactional; rollback nếu INSERT lỗi |

---

## 4. Ma trận RTM — Use Cases ↔ Database Entities

| Use Case | Database Entities Bị tác động | Loại thao tác |
|:---------|:------------------------------|:--------------|
| UC01.1 — Đăng ký | Accounts, Customers | INSERT |
| UC01.2 — Đăng nhập | Accounts, Audit_Logs | SELECT, INSERT |
| UC02 — Reset Password | Accounts, Password_Reset_Tokens | SELECT, UPDATE, INSERT, DELETE |
| UC03.1 — Quản lý Hồ sơ | Customers | UPDATE |
| UC03.2 — FaceID | Customers, Tour_Attendees | UPDATE |
| UC04.1 — Quản lý Nhân viên | Accounts, Employees, Roles | INSERT, UPDATE |
| UC04.2 — Audit Log | Audit_Logs | INSERT, SELECT |
| UC05.1 — Core Data CRUD | Rooms, Restaurant_Tables, Tours, Menu_Items | INSERT, UPDATE, DELETE |
| UC05.2 — Dynamic Pricing | Dynamic_Pricing, Daily_Rates | INSERT, UPDATE |
| UC05.3 — Promotions | Promotions | INSERT, UPDATE |
| UC06 — Tìm phòng trống | Room_Booking_Details, Daily_Rates, Rooms | SELECT |
| UC07.1 — Đặt phòng & Cọc | Bookings, Room_Bookings, Room_Booking_Details, Payment_Transactions | INSERT, UPDATE |
| UC07.2 — Voucher | Promotions | SELECT, UPDATE |
| UC07.3 — Cart Lock | Bookings | UPDATE |
| UC08 — Room Matrix | Rooms, Room_Booking_Details | SELECT |
| UC09.1 — Check-in | Room_Booking_Details, Rooms | UPDATE |
| UC09.2 — Credit Limit | Room_Booking_Details | UPDATE |
| UC09.3 — Đổi phòng | Room_Booking_Details, Rooms, Audit_Logs | UPDATE, INSERT |
| UC09.4 — Khai báo lưu trú | Room_Booking_Details, Dependents, Customers | UPDATE |
| UC10.1 — Auto Housekeeping | Hotel_Operations, Rooms | INSERT, UPDATE |
| UC10.2 — Cập nhật dọn phòng | Hotel_Operations, Rooms | UPDATE |
| UC10.3 — Rush Room | Hotel_Operations | UPDATE |
| UC10.4 — Báo hỏng | Hotel_Operations, Rooms | INSERT, UPDATE |
| UC10.5 — Sửa chữa xong | Hotel_Operations, Rooms | UPDATE |
| UC11 — Room Service | Food_Orders, Food_Order_Details | INSERT |
| UC12 — Đặt bàn | Table_Reservations, Restaurant_Tables | INSERT, SELECT |
| UC13 — Dine-In POS | Food_Orders, Food_Order_Details | INSERT |
| UC14.1 — KDS | Food_Order_Details | SELECT |
| UC14.2 — KDS Update | Food_Order_Details | UPDATE |
| UC14.3 — Hết món | Menu_Items | UPDATE |
| UC15 — Post to Room | Folio_Items, Food_Orders | INSERT, UPDATE |
| UC16 — Tìm Tour | Tours, Tour_Schedules | SELECT |
| UC17.1 — Đặt Tour | Tour_Bookings, Tour_Attendees, Tour_Schedules | INSERT, UPDATE |
| UC17.2 — Lập lịch Tour | Tour_Schedules, Tour_Staff_Assignments | INSERT |
| UC17.3 — Hủy Tour khẩn | Tour_Schedules, Tour_Bookings, Payment_Transactions | UPDATE, INSERT |
| UC17.4 — Combo → Tour | Tour_Bookings, Tour_Attendees | INSERT |
| UC18 — AI Face Scan | Tour_Attendees | UPDATE |
| UC19 — Gửi Đánh giá | Reviews | INSERT |
| UC20 — Kiểm duyệt Review | Reviews, Audit_Logs | UPDATE, INSERT |
| UC21.1 — Theo dõi Folio | Folio_Items | SELECT |
| UC21.2 — Lịch sử GD | Payment_Transactions | INSERT |
| UC21.3 — Gom hóa đơn | Folio_Items, Room_Booking_Details, Consolidated_Invoices | SELECT, INSERT |
| UC21.4 — Night Audit | Folio_Items, Room_Booking_Details, Daily_Rates | INSERT, SELECT |
| UC21.5 — Split Folio | Folio_Items | UPDATE |
| UC22.1 — Checkout Cuối | Consolidated_Invoices, Room_Booking_Details, Rooms, Payment_Transactions | UPDATE, INSERT |
| UC22.2 — e-Invoice | Consolidated_Invoices | SELECT |
| UC23.1 — Dashboard | Consolidated_Invoices, Folio_Items | SELECT |
| UC23.2 — Occupancy | Rooms, Room_Booking_Details | SELECT |
| UC24 — USALI Report | Consolidated_Invoices, Folio_Items, Employees | SELECT |
| UC25 — Export File | Consolidated_Invoices, Folio_Items | SELECT |

---

## 5. Ma trận Phủ sóng Business Rules theo Use Case

Bảng dưới đây thể hiện các Business Rules được thực thi trong mỗi Use Case chính.  
**Ký hiệu:** ✅ = BR bắt buộc áp dụng trong UC này | 〇 = BR có liên quan gián tiếp | — = Không áp dụng

| Business Rule | UC Đặt phòng | UC Check-in | UC Check-out | UC Room Service | UC Post-to-Room | UC Tour Booking | UC Night Audit | UC Review |
|:---|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| BR-SYS-01 | 〇 | 〇 | 〇 | 〇 | 〇 | 〇 | — | — |
| BR-SYS-02 | 〇 | 〇 | 〇 | 〇 | 〇 | 〇 | — | — |
| BR-SYS-03 | — | ✅ | ✅ | — | ✅ | — | — | — |
| BR-SYS-04 | ✅ | ✅ | ✅ | — | ✅ | ✅ | ✅ | ✅ |
| BR-SYS-07 | 〇 | 〇 | 〇 | 〇 | 〇 | 〇 | 〇 | 〇 |
| BR-FO-01 | ✅ | — | — | — | — | — | — | — |
| BR-FO-02 | ✅ | — | — | — | — | — | — | — |
| BR-FO-03 | — | ✅ | — | — | — | — | — | — |
| BR-FO-04 | — | ✅ | ✅ | — | — | — | — | — |
| BR-FO-05 | — | — | — | — | — | — | — | — |
| BR-FO-06 | — | ✅ | ✅ | ✅ | ✅ | — | — | — |
| BR-FB-01 | — | — | — | ✅ | ✅ | — | — | — |
| BR-FB-02 | — | — | — | ✅ | — | — | — | — |
| BR-FB-03 | — | — | — | — | — | — | — | — |
| BR-TR-01 | — | — | — | — | — | ✅ | — | — |
| BR-TR-02 | — | — | — | — | — | 〇 | — | — |
| BR-TR-03 | — | — | — | — | — | — | — | ✅ |
| BR-TR-04 | — | — | — | — | — | — | — | ✅ |
| BR-TR-05 | — | — | — | — | — | ✅ | — | — |
| BR-FIN-01 | — | — | ✅ | — | — | — | — | — |
| BR-FIN-02 | ✅ | — | — | — | — | — | — | — |
| BR-FIN-03 | — | — | — | — | — | — | ✅ | — |
| BR-FIN-04 | — | — | ✅ | — | ✅ | ✅ | ✅ | — |
| BR-FIN-05 | 〇 | — | — | — | — | — | — | — |
| BR-FIN-06 | ✅ | — | — | — | — | — | — | — |
| BR-HK-01 | — | — | ✅ | — | — | — | — | — |
| BR-HK-02 | — | — | — | — | — | — | — | — |
| BR-DATA-01 | 〇 | — | — | — | — | — | — | — |
| BR-DATA-02 | — | ✅ | — | — | — | — | — | — |
| BR-DATA-03 | — | — | — | — | — | — | — | — |

---

## 6. Phân tích Khoảng trống (Gap Analysis)

### 6.1 Yêu cầu Chưa có Use Case Cụ thể

| Business Rule | Vấn đề | Khuyến nghị |
|:---|:---|:---|
| BR-FO-05 (Rush Room) | Chưa có UC specification riêng | Bổ sung UC "Đánh dấu Rush Room" trong SRS §2 |
| BR-FB-03 (Giữ bàn 30 phút) | Logic tự động giải phóng bàn chưa có NF function | Bổ sung Scheduler kiểm tra bàn hết hạn |
| BR-TR-05 (Hủy tour dưới ngưỡng) | UC-23 đề cập nhưng chưa detail luồng 24h trước | Bổ sung UC specification chi tiết cho auto-cancel tour |
| BR-FIN-03 (Night Audit) | UC21.4 có nhưng chưa có FR (screen) hiển thị kết quả | Bổ sung màn hình "Night Audit Result Dashboard" |
| BR-SYS-05 (Anonymization) | UC-07 mô tả nhưng chưa có detail trigger conditions | Cần bổ sung điều kiện: chỉ sau check-out |

### 6.2 Use Cases Chưa có Business Rule Bảo vệ

| Use Case | Thiếu BR | Khuyến nghị |
|:---|:---|:---|
| UC-24 (AI Face Scan) | Chưa có BR cho trường hợp mất dữ liệu face vector | Bổ sung BR: "Khách chưa đăng ký face vector phải điểm danh thủ công" |
| UC-22.2 (e-Invoice) | Chưa có BR về thời hạn phát hành e-Invoice | Bổ sung BR: "e-Invoice phải được gửi trong vòng 24h sau checkout" |
| UC-17.2 (Lập lịch tour) | Chưa có BR rõ về thời hạn tối thiểu lập lịch trước | Bổ sung BR: "Tour schedule phải được tạo tối thiểu 24h trước giờ khởi hành" |
| UC-05.3 (Combo) | Chưa có BR về cấu trúc JSON combo và rollback | Bổ sung BR về validation schema JSON combo |

### 6.3 Rủi ro Kỹ thuật Cần Chú ý

| Rủi ro | Business Rule Liên quan | Khuyến nghị |
|:---|:---|:---|
| Race condition khi 2 khách cùng đặt 1 phòng | BR-FO-01 | Kiểm tra @Version Optimistic Lock được implement đúng |
| Night Audit chạy sai giờ hoặc lỗi giữa chừng | BR-FIN-03 | Cần cơ chế retry + alert + idempotency check |
| WebSocket mất kết nối khi cập nhật trạng thái phòng | BR-FB-02, BR-FO-04 | Cần fallback polling mechanism |
| AI Face Scan timeout/lỗi service | BR-TR-02 | Cần timeout handling + manual fallback luôn khả dụng |
| VNPay webhook bị miss/trùng | BR-FO-02 | Cần idempotency key cho webhook processing |

---

## 7. Tổng kết Phân phối theo Mức độ ưu tiên

### 7.1 Tổng số Requirements

| Loại | Số lượng |
|:-----|:--------:|
| Business Rules (BR) | 27 |
| Use Cases (SRS) | 40 |
| Use Cases (Project Specification) | 25 |
| Use Cases trong RTM | 59 |
| Non-UI System Functions | 16 |
| Database Entities chính | 27 |

### 7.2 Phân phối Business Rules theo Mức độ ưu tiên

| Mức độ | Số BR | Danh sách |
|:-------|:-----:|:----------|
| CRITICAL | 11 | BR-SYS-01, BR-SYS-04, BR-SYS-07, BR-FO-01, BR-FO-02, BR-FO-03, BR-FO-04, BR-FB-01, BR-TR-01, BR-FIN-01, BR-FIN-03, BR-DATA-02, BR-DATA-03 |
| HIGH | 12 | BR-SYS-02, BR-SYS-03, BR-SYS-05, BR-SYS-06, BR-FO-06, BR-FB-02, BR-TR-02, BR-TR-04, BR-TR-05, BR-FIN-02, BR-FIN-04, BR-HK-01, BR-HK-02, BR-DATA-01 |
| MEDIUM | 4 | BR-FO-05, BR-FB-03, BR-FB-04, BR-TR-03, BR-FIN-05, BR-FIN-06 |

### 7.3 Phân phối Use Cases theo Phân hệ

| Phân hệ | Số UC | Tỷ lệ |
|:--------|:-----:|:------:|
| Xác thực & Quản trị | 13 | 22% |
| Đặt phòng & Tiền sảnh | 12 | 20% |
| F&B / POS / KDS | 8 | 14% |
| Lữ hành & Đánh giá | 9 | 15% |
| Buồng phòng & Bảo trì | 5 | 8% |
| Tài chính & Báo cáo | 12 | 20% |
| **Tổng** | **59** | **100%** |

### 7.4 Phân phối Use Cases theo Mức độ ưu tiên

| Mức độ | Số UC | Ví dụ đặc trưng |
|:-------|:-----:|:----------------|
| CRITICAL | 14 | Đặt phòng, Check-in, Check-out, Post-to-Room, Night Audit, Tour Booking |
| HIGH | 29 | Tìm kiếm phòng, Audit Log, Room Matrix, KDS, USALI Report |
| MEDIUM | 16 | Rush Room, Đặt bàn, Tìm tour, e-Invoice, Export file |

---

### Ghi chú Phân tích

> **BR phủ sóng cao nhất (xuất hiện trong nhiều UC nhất):**
> - BR-SYS-04 (Audit Log) — xuất hiện trong 8+ UC
> - BR-FO-04 (Vòng đời phòng) — xuất hiện trong 5+ UC
> - BR-FIN-04 (USALI) — xuất hiện trong 4+ UC
>
> **UC có nhiều BR nhất (phức tạp nhất về nghiệp vụ):**
> - UC Check-out: BR-FIN-01 + BR-FO-04 + BR-HK-01 + BR-SYS-04
> - UC Check-in: BR-FO-03 + BR-FO-04 + BR-FO-06 + BR-SYS-04 + BR-DATA-02
> - UC Đặt phòng: BR-FO-01 + BR-FO-02 + BR-FIN-06 + BR-SYS-04

---

*Tài liệu RTM được tổng hợp và phân tích dựa trên SRS_Document_SWP391_G2.md, Project_Specification.md, và BusinessRule.md của dự án Kawai Retreat Resort & Hub, Group 2 — SWP391 SE2023.*
