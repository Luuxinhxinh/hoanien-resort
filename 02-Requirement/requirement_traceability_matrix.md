# REQUIREMENTS TRACEABILITY MATRIX (RTM)
# MA TRẬN TRUY VẾT YÊU CẦU

**Project:** Kawai Retreat Resort & Hub — SWP391 Group 2 — SE2023-NET  
**Lecturer:** Nguyễn Mạnh Cường  
**Version:** 1.0  
**Last Updated:** 2026-06-29  
**Analyst:** Antigravity (Business Analyst Review)

---

## Mục lục

- [1. Giới thiệu](#1-giới-thiệu)
- [2. RTM Phân hệ 1 — Xác thực, Hồ sơ & Dữ liệu gốc](#2-rtm-phân-hệ-1--xác-thực-hồ-sơ--dữ-liệu-gốc)
- [3. RTM Phân hệ 2 — Quản lý Phòng & Lễ tân](#3-rtm-phân-hệ-2--quản-lý-phòng--lễ-tân)
- [4. RTM Phân hệ 3 — F&B / POS / KDS](#4-rtm-phân-hệ-3--fb--pos--kds)
- [5. RTM Phân hệ 4 — Lữ hành & Đánh giá](#5-rtm-phân-hệ-4--lữ-hành--đánh-giá)
- [6. RTM Phân hệ 5 — Tài chính & Báo cáo](#6-rtm-phân-hệ-5--tài-chính--báo-cáo)
- [7. RTM Business Rules vs Use Cases](#7-rtm-business-rules-vs-use-cases)
- [8. RTM Non-Functional Requirements](#8-rtm-non-functional-requirements)
- [9. Bảng Phủ sóng Tổng hợp](#9-bảng-phủ-sóng-tổng-hợp)

---

## 1. Giới thiệu

### 1.1 Mục đích

Ma trận Truy vết Yêu cầu (RTM) giúp:
- Đảm bảo **mọi yêu cầu** từ tài liệu SRS đều được **triển khai** trong ít nhất một Use Case
- Theo dõi **luồng liên kết** từ Yêu cầu nghiệp vụ → Use Case → Business Rule → Thực thể CSDL → Màn hình/Chức năng
- Phát hiện sớm các **yêu cầu chưa được phủ sóng** hoặc **không cần thiết**

### 1.2 Ký hiệu

| Ký hiệu | Ý nghĩa |
|---|---|
| UC-xx | Use Case trong SRS |
| PS-UCxx | Use Case trong Project Specification |
| BR-xxx-xx | Business Rule |
| FR-xxx | Functional Requirement |
| NFR-xxx | Non-Functional Requirement |
| ✅ | Đã phủ sóng / Đã triển khai |
| ⚠️ | Cần xem xét thêm |
| ❌ | Chưa phủ sóng |

---

## 2. RTM Phân hệ 1 — Xác thực, Hồ sơ & Dữ liệu gốc

### 2.1 UC01 — Đăng ký & Đăng nhập

| Req ID | Mô tả Yêu cầu | SRS UC | PS UC | Business Rule | Thực thể DB | Màn hình | Trạng thái |
|---|---|---|---|---|---|---|---|
| FR-AUTH-01 | Khách vãng lai có thể đăng ký tài khoản Customer | UC-02 | PS-UC01.1 | BR-SYS-01 | Accounts, Customers | User Registration Form | ✅ |
| FR-AUTH-02 | Đăng ký bằng Google OAuth2 | UC-02 | PS-UC01.2 | BR-SYS-01 | Accounts, Customers | User Registration Form | ✅ |
| FR-AUTH-03 | Mật khẩu phải tối thiểu 8 ký tự, có chữ hoa, thường, số | UC-02 | PS-UC01.1 | BR-SYS-01 | Accounts | User Registration Form | ✅ |
| FR-AUTH-04 | Email phải duy nhất trong hệ thống | UC-02 | PS-UC01.1 | — | Accounts | User Registration Form | ✅ |
| FR-AUTH-05 | Tài khoản được tạo ở trạng thái `is_active = true` | UC-02 | PS-UC01.1 | — | Accounts | — | ✅ |
| FR-AUTH-06 | Người dùng đăng nhập bằng email và mật khẩu | UC-01 | PS-UC01.2 | BR-SYS-01, BR-SYS-02 | Accounts | Single Login Portal | ✅ |
| FR-AUTH-07 | Khóa tài khoản sau 5 lần đăng nhập sai liên tiếp | UC-01 | PS-UC01.2 | BR-SYS-02 | Accounts | Single Login Portal | ✅ |
| FR-AUTH-08 | Redirect theo role sau khi đăng nhập thành công | UC-01 | PS-UC01.2 | — | Roles | Single Login Portal | ✅ |
| FR-AUTH-09 | Ghi Audit Log khi đăng nhập | UC-01 | PS-UC01.2 | BR-SYS-04 | Audit_Logs | — | ✅ |
| FR-AUTH-10 | OTP xác thực hết hạn sau 3 phút | UC-03 | PS-UC02 | BR-SYS-02 | — | Single Login Portal | ✅ |

### 2.2 UC02 — Đặt lại mật khẩu

| Req ID | Mô tả Yêu cầu | SRS UC | PS UC | Business Rule | Thực thể DB | Màn hình | Trạng thái |
|---|---|---|---|---|---|---|---|
| FR-PWD-01 | Gửi link đặt lại mật khẩu qua email | UC-04 | PS-UC02 | BR-SYS-02 | Accounts | Login Portal | ✅ |
| FR-PWD-02 | Token reset mật khẩu hết hạn sau 15 phút | UC-04 | PS-UC02 | BR-SYS-02 | Password_Reset_Tokens | — | ✅ |
| FR-PWD-03 | Token reset chỉ được dùng 1 lần | UC-04 | PS-UC02 | BR-SYS-02 | Password_Reset_Tokens | — | ✅ |
| FR-PWD-04 | Mật khẩu mới được mã hóa bằng BCrypt | UC-04 | PS-UC02 | BR-SYS-01 | Accounts | — | ✅ |

### 2.3 UC03 — Quản lý Hồ sơ & Định danh FaceID

| Req ID | Mô tả Yêu cầu | SRS UC | PS UC | Business Rule | Thực thể DB | Màn hình | Trạng thái |
|---|---|---|---|---|---|---|---|
| FR-PRF-01 | Customer cập nhật thông tin cá nhân | UC-05 | PS-UC03.1 | BR-SYS-01 | Customers | Member Dashboard | ✅ |
| FR-PRF-02 | Mã hóa AES-256 cho CCCD/Hộ chiếu | UC-05 | PS-UC03.1 | BR-SYS-01 | Customers | Member Dashboard | ✅ |
| FR-PRF-03 | Upload ảnh khuôn mặt để trích xuất face vector | UC-05 | PS-UC03.2 | BR-TR-02 | Tour_Attendees, Customers | Member Dashboard | ✅ |
| FR-PRF-04 | AI service xử lý face vector (128-dim embedding) | UC-24 | PS-UC03.2 | BR-TR-02 | Customers | — | ✅ |
| FR-PRF-05 | Khách hàng gửi yêu cầu xóa dữ liệu cá nhân | UC-07 | PS-§7 | BR-SYS-05 | Customers | Member Dashboard | ✅ |
| FR-PRF-06 | Lễ tân nâng cấp người phụ thuộc lên Customer | UC-06 | PS-UC09.4 | BR-FO-03 | Accounts, Customers | Check-in Processing Form | ✅ |

### 2.4 UC04 — Quản lý Tài khoản Nhân viên

| Req ID | Mô tả Yêu cầu | SRS UC | PS UC | Business Rule | Thực thể DB | Màn hình | Trạng thái |
|---|---|---|---|---|---|---|---|
| FR-ADM-01 | Admin CRUD tài khoản nhân viên | UC-08 | PS-UC04.1 | BR-DAT-02 | Accounts, Employees | Staff Directory Management | ✅ |
| FR-ADM-02 | Gán vai trò RBAC cho nhân viên | UC-08 | PS-UC04.1 | BR-DAT-02 | Roles, Accounts | Staff Directory Management | ✅ |
| FR-ADM-03 | Nhân viên chỉ có 1 vai trò tại một thời điểm | UC-08 | PS-UC04.1 | BR-DAT-02 | Roles | — | ✅ |
| FR-ADM-04 | Tạo tài khoản nhân viên trong @Transactional block | UC-08 | PS-UC04.1 | — | Accounts, Employees | — | ✅ |
| FR-ADM-05 | Admin xem Audit Log lịch sử thao tác nhạy cảm | UC-32 | PS-UC04.2 | BR-SYS-04 | Audit_Logs | Config & Audit Log Panel | ✅ |
| FR-ADM-06 | Audit_Logs chỉ INSERT và SELECT — cấm UPDATE/DELETE | UC-32 | PS-UC04.2 | BR-SYS-04 | Audit_Logs | — | ✅ |

### 2.5 UC05 — Quản lý Cấu hình Hệ thống

| Req ID | Mô tả Yêu cầu | SRS UC | PS UC | Business Rule | Thực thể DB | Màn hình | Trạng thái |
|---|---|---|---|---|---|---|---|
| FR-CFG-01 | Admin CRUD phòng vật lý, bàn ăn, tour | UC-09 | PS-UC05.1 | BR-DAT-01 | Rooms, Restaurant_Tables, Tours | Config Panel | ✅ |
| FR-CFG-02 | Không xóa phòng đang có khách ở | UC-09 | PS-UC05.1 | BR-DAT-01 | Rooms | Config Panel | ✅ |
| FR-CFG-03 | Cấu hình giá phòng động theo mùa/lễ | UC-40 | PS-UC05.2 | BR-DAT-03 | Dynamic_Pricing, Daily_Rates | Config Panel | ✅ |
| FR-CFG-04 | Giá phòng động không được chồng lấn khoảng ngày | UC-40 | PS-UC05.2 | BR-DAT-03 | Dynamic_Pricing | — | ✅ |
| FR-CFG-05 | Tạo mã Voucher khuyến mãi | UC-38 | PS-UC05.3 | BR-FIN-05 | Promotions | Config Panel | ✅ |
| FR-CFG-06 | CRUD menu món ăn | UC-09 | PS-UC05.1 | BR-FB-02 | Menu_Items | Config Panel | ✅ |

---

## 3. RTM Phân hệ 2 — Quản lý Phòng & Lễ tân

### 3.1 UC06/UC04 — Tìm kiếm Phòng trống

| Req ID | Mô tả Yêu cầu | SRS UC | PS UC | Business Rule | Thực thể DB | Màn hình | Trạng thái |
|---|---|---|---|---|---|---|---|
| FR-SRCH-01 | Guest/Customer tìm kiếm phòng theo ngày và số khách | UC-10 | PS-UC06 | BR-FO-01 | Room_Booking_Details, Daily_Rates, Rooms | Public Home Page | ✅ |
| FR-SRCH-02 | Ngày nhận phòng phải >= ngày hiện tại | UC-10 | PS-UC06 | BR-FO-01 | — | Public Home Page | ✅ |
| FR-SRCH-03 | Ngày trả phòng phải sau ngày nhận phòng ít nhất 1 ngày | UC-10 | PS-UC06 | BR-FO-01 | — | Public Home Page | ✅ |
| FR-SRCH-04 | Hiển thị danh sách phòng trống với giá thực tế | UC-10 | PS-UC06 | — | Room_Categories, Daily_Rates | Public Home Page | ✅ |
| FR-SRCH-05 | Tính tổng tiền dự kiến dựa vào Daily_Rates | UC-10 | PS-UC06 | — | Daily_Rates | Public Home Page | ✅ |
| FR-SRCH-06 | Không cần đăng nhập để tìm kiếm phòng | UC-10 | PS-UC06 | — | — | Public Home Page | ✅ |

### 3.2 UC05/UC07 — Đặt phòng & Thanh toán cọc

| Req ID | Mô tả Yêu cầu | SRS UC | PS UC | Business Rule | Thực thể DB | Màn hình | Trạng thái |
|---|---|---|---|---|---|---|---|
| FR-BOOK-01 | Customer đặt một hoặc nhiều phòng trong một lần | UC-11 | PS-UC07.1 | BR-FO-01, BR-FO-02 | Bookings, Room_Bookings, Room_Booking_Details | Member Dashboard | ✅ |
| FR-BOOK-02 | Tạo Booking ở trạng thái Pending trước khi thanh toán | UC-11 | PS-UC07.1 | BR-FO-02 | Bookings | — | ✅ |
| FR-BOOK-03 | Cart Lock 15 phút khi khách bắt đầu thanh toán | UC-11 | PS-UC07.3 | BR-FO-01, BR-FO-02 | Bookings | — | ✅ |
| FR-BOOK-04 | Tích hợp VNPay Sandbox để thanh toán cọc | UC-11, UC-14 | PS-UC07.1 | — | Payment_Transactions | — | ✅ |
| FR-BOOK-05 | Booking chuyển Confirmed sau khi thanh toán thành công | UC-11, UC-14 | PS-UC07.1 | — | Bookings | — | ✅ |
| FR-BOOK-06 | Tự động hủy Booking sau 15 phút nếu chưa thanh toán | UC-11 | PS-UC07.3 | BR-FO-02 | Bookings | — | ✅ |
| FR-BOOK-07 | Áp dụng tối đa 1 mã Voucher mỗi Booking | UC-38 | PS-UC07.2 | BR-FIN-05 | Promotions, Bookings | — | ✅ |
| FR-BOOK-08 | Khách hàng hủy đặt phòng Confirmed trước Check-in | UC-06 (SRS) | — | BR-FIN-02 | Bookings | Booking History | ✅ |
| FR-BOOK-09 | Hoàn 100% cọc nếu hủy trước 48 giờ | UC-06 (SRS) | PS-§5 | BR-FIN-02 | Payment_Transactions | — | ✅ |
| FR-BOOK-10 | Tịch thu cọc nếu hủy trong 48 giờ hoặc no-show | UC-06 (SRS) | PS-§5 | BR-FIN-02 | — | — | ✅ |

### 3.3 UC08/UC09 — Dashboard Lễ tân & Check-in

| Req ID | Mô tả Yêu cầu | SRS UC | PS UC | Business Rule | Thực thể DB | Màn hình | Trạng thái |
|---|---|---|---|---|---|---|---|
| FR-FO-01 | Lễ tân xem Room Matrix với trạng thái phòng real-time | UC-12 | PS-UC08 | BR-FO-04 | Rooms, Room_Booking_Details | Room Matrix Dashboard | ✅ |
| FR-FO-02 | Room Matrix cập nhật qua WebSocket | UC-12 | PS-UC08 | BR-FO-04 | Rooms | Room Matrix Dashboard | ✅ |
| FR-FO-03 | Lễ tân thực hiện Check-in cho khách đã đặt phòng Confirmed | UC-13 | PS-UC09.1 | BR-FO-03, BR-FO-04 | Room_Booking_Details, Rooms | Check-in Processing Form | ✅ |
| FR-FO-04 | Quét OCR CCCD để tự động điền form Check-in | UC-13 | PS-UC09.1 | BR-FO-03 | Customers | Check-in Processing Form | ✅ |
| FR-FO-05 | Chỉ phòng Vacant_Clean mới được gán khi Check-in | UC-13 | PS-UC09.1 | BR-FO-04 | Rooms | Check-in Processing Form | ✅ |
| FR-FO-06 | Lễ tân cấu hình Credit Limit và PIN cho phòng | UC-14 | PS-UC09.2 | BR-FB-01 | Room_Booking_Details | Check-in Processing Form | ✅ |
| FR-FO-07 | PIN được mã hóa BCrypt trước khi lưu | UC-14 | PS-UC09.2 | BR-SYS-01, BR-FB-01 | Room_Booking_Details | — | ✅ |
| FR-FO-08 | Hạn mức nợ sub-room không vượt hạn mức Booking tổng | UC-14 | PS-UC09.2 | BR-FB-01 | Room_Booking_Details | — | ✅ |
| FR-FO-09 | Lễ tân thực hiện đổi phòng vật lý cho khách đang lưu trú | — | PS-UC09.3 | BR-FO-04 | Room_Booking_Details, Rooms, Audit_Logs | Room Matrix Dashboard | ✅ |
| FR-FO-10 | Ghi Audit Log khi đổi phòng | — | PS-UC09.3 | BR-SYS-04 | Audit_Logs | — | ✅ |
| FR-FO-11 | Khai báo thành viên lưu trú và chỉ định Primary Contact | — | PS-UC09.4 | BR-FO-03 | Room_Booking_Details, Dependents | Check-in Processing Form | ✅ |

### 3.4 UC08 — Check-out

| Req ID | Mô tả Yêu cầu | SRS UC | PS UC | Business Rule | Thực thể DB | Màn hình | Trạng thái |
|---|---|---|---|---|---|---|---|
| FR-CO-01 | Lễ tân xem tổng hóa đơn trước khi Check-out | UC-08 (SRS) | PS-UC22.1 | BR-FIN-01 | Folio_Items, Consolidated_Invoices | Checkout & Bill Workspace | ✅ |
| FR-CO-02 | Hệ thống chặn Check-out nếu còn nợ tồn đọng | UC-08 (SRS) | PS-UC22.1 | BR-FIN-01 | Consolidated_Invoices | Checkout & Bill Workspace | ✅ |
| FR-CO-03 | Xử lý thanh toán cuối (tiền mặt / chuyển khoản) | UC-29 | PS-UC22.1 | — | Payment_Transactions | Checkout & Bill Workspace | ✅ |
| FR-CO-04 | Phòng chuyển Vacant_Dirty sau Check-out | UC-08 (SRS) | PS-UC22.1 | BR-FO-04 | Rooms | — | ✅ |
| FR-CO-05 | Tự động sinh task dọn phòng sau Check-out | UC-34 | PS-UC10.1 | BR-HK-01 | Hotel_Operations | — | ✅ |
| FR-CO-06 | Hệ thống tự động gửi e-Invoice PDF qua email | UC-28 | PS-UC22.2 | — | Consolidated_Invoices | — | ✅ |

### 3.5 UC10 — Housekeeping & Bảo trì

| Req ID | Mô tả Yêu cầu | SRS UC | PS UC | Business Rule | Thực thể DB | Màn hình | Trạng thái |
|---|---|---|---|---|---|---|---|
| FR-HK-01 | DB Trigger tự động sinh task dọn khi Check-out | UC-34 | PS-UC10.1 | BR-HK-01 | Hotel_Operations, Rooms | — | ✅ |
| FR-HK-02 | Housekeeping cập nhật trạng thái dọn dẹp trên app | UC-35 | PS-UC10.2 | BR-FO-04 | Hotel_Operations, Rooms | Housekeeping Task Grid | ✅ |
| FR-HK-03 | Phòng chuyển Vacant_Clean sau khi hoàn thành dọn | UC-35 | PS-UC10.2 | BR-FO-04 | Rooms | — | ✅ |
| FR-HK-04 | Lễ tân đánh dấu Rush Room để ưu tiên dọn | — | PS-UC10.3 | BR-FO-05 | Hotel_Operations | Room Matrix Dashboard | ✅ |
| FR-HK-05 | Housekeeping báo hỏng thiết bị | UC-36 | PS-UC10.4 | BR-HK-02 | Hotel_Operations, Rooms | Housekeeping Task Grid | ✅ |
| FR-HK-06 | Phòng chuyển Maintenance khi có báo hỏng | UC-36 | PS-UC10.4 | BR-HK-02, BR-FO-04 | Rooms | — | ✅ |
| FR-HK-07 | Maintenance hoàn thành sửa chữa — phòng được giải phóng | UC-37 | PS-UC10.5 | BR-FO-04 | Hotel_Operations, Rooms | Maintenance Request Queue | ✅ |

---

## 4. RTM Phân hệ 3 — F&B / POS / KDS

### 4.1 UC11/UC09 — Đặt món Room Service

| Req ID | Mô tả Yêu cầu | SRS UC | PS UC | Business Rule | Thực thể DB | Màn hình | Trạng thái |
|---|---|---|---|---|---|---|---|
| FR-FB-01 | Customer đặt món ăn Room Service qua QR code | UC-15 | PS-UC11 | BR-FB-01 | Food_Orders, Food_Order_Details | Room Service Menu | ✅ |
| FR-FB-02 | Chỉ khách đang Check-in mới đặt được Room Service | UC-15 | PS-UC11 | BR-FB-01 | Room_Booking_Details | Room Service Menu | ✅ |
| FR-FB-03 | Kiểm tra hạn mức tín dụng phòng trước khi đặt | UC-15 | PS-UC11 | BR-FB-01 | Room_Booking_Details | Room Service Menu | ✅ |
| FR-FB-04 | Order được gửi real-time xuống màn hình KDS | UC-15, UC-17 | PS-UC11 | — | Food_Orders | Kitchen Status Dashboard | ✅ |

### 4.2 UC19 — Đặt bàn nhà hàng

| Req ID | Mô tả Yêu cầu | SRS UC | PS UC | Business Rule | Thực thể DB | Màn hình | Trạng thái |
|---|---|---|---|---|---|---|---|
| FR-RES-01 | Customer đặt bàn theo ngày, giờ và số khách | UC-19 | PS-UC12 | BR-FB-03 | Table_Reservations, Restaurant_Tables | Member Dashboard | ✅ |
| FR-RES-02 | Thời điểm đặt bàn phải trước giờ ăn ít nhất 1 tiếng | UC-19 | PS-UC12 | BR-FB-03 | — | — | ✅ |
| FR-RES-03 | Không được đặt trùng bàn trong cùng khung giờ | UC-19 | PS-UC12 | BR-FB-03 | Table_Reservations | — | ✅ |
| FR-RES-04 | Bàn được giải phóng sau 30 phút nếu khách không đến | UC-19 | PS-UC12 | BR-FB-03 | Table_Reservations | — | ✅ |

### 4.3 UC16 — POS Dine-In

| Req ID | Mô tả Yêu cầu | SRS UC | PS UC | Business Rule | Thực thể DB | Màn hình | Trạng thái |
|---|---|---|---|---|---|---|---|
| FR-POS-01 | F&B Staff lên đơn Dine-In cho khách tại bàn | UC-16 | PS-UC13 | — | Food_Orders, Food_Order_Details | Restaurant POS Terminal | ✅ |
| FR-POS-02 | Đơn Dine-In được đẩy xuống KDS nhà bếp | UC-16 | PS-UC13 | — | Food_Orders | Kitchen Status Dashboard | ✅ |
| FR-POS-03 | F&B Staff xử lý thanh toán trực tiếp hoặc ký nợ phòng | UC-20 | PS-UC15 | BR-FB-01 | Folio_Items, Food_Orders | Payment Verification Gate | ✅ |
| FR-POS-04 | Ký nợ phòng yêu cầu xác thực PIN | UC-20 | PS-UC15 | BR-FB-01 | Room_Booking_Details | Payment Verification Gate | ✅ |
| FR-POS-05 | Trigger chặn ký nợ vượt Credit Limit | UC-20 | PS-UC15 | BR-FB-01 | Room_Booking_Details, Folio_Items | — | ✅ |

### 4.4 UC17/UC18 — KDS & Quản lý Món

| Req ID | Mô tả Yêu cầu | SRS UC | PS UC | Business Rule | Thực thể DB | Màn hình | Trạng thái |
|---|---|---|---|---|---|---|---|
| FR-KDS-01 | Kitchen Staff xem và quản lý đơn hàng trên KDS | UC-17 | PS-UC14.1 | — | Food_Orders, Food_Order_Details | Kitchen Status Dashboard | ✅ |
| FR-KDS-02 | Kitchen Staff cập nhật trạng thái món: Pending->Cooking->Ready | UC-17 | PS-UC14.2 | — | Food_Order_Details | Kitchen Status Dashboard | ✅ |
| FR-KDS-03 | Cập nhật trạng thái món real-time qua WebSocket | UC-17 | PS-UC14.2 | — | Food_Order_Details | Restaurant POS Terminal | ✅ |
| FR-KDS-04 | Kitchen Staff đánh dấu hết món | UC-18 | PS-UC14.3 | BR-FB-02 | Menu_Items | Kitchen Status Dashboard | ✅ |
| FR-KDS-05 | Hết món => ẩn trên toàn bộ POS và Room Service | UC-18 | PS-UC14.3 | BR-FB-02 | Menu_Items | Restaurant POS Terminal, Room Service Menu | ✅ |

---

## 5. RTM Phân hệ 4 — Lữ hành & Đánh giá

### 5.1 UC22/UC16 — Tìm kiếm & Đặt Tour

| Req ID | Mô tả Yêu cầu | SRS UC | PS UC | Business Rule | Thực thể DB | Màn hình | Trạng thái |
|---|---|---|---|---|---|---|---|
| FR-TOUR-01 | Customer/Guest tìm kiếm tour theo ngày | UC-22 | PS-UC16 | — | Tours, Tour_Schedules | Public Home Page | ✅ |
| FR-TOUR-02 | Tích hợp OpenWeather API hiển thị dự báo thời tiết | UC-22 | PS-UC16 | — | — | Public Home Page | ✅ |
| FR-TOUR-03 | Customer đặt vé tour cho nhiều người | UC-22 | PS-UC17.1 | BR-TR-01 | Tour_Bookings, Tour_Attendees | Member Dashboard | ✅ |
| FR-TOUR-04 | Trigger chặn đặt vé vượt sức chứa chuyến | UC-22 | PS-UC17.1 | BR-TR-01 | Tour_Schedules | — | ✅ |
| FR-TOUR-05 | Thanh toán tour bằng VNPay hoặc ký nợ phòng | UC-22 | PS-UC17.1 | BR-FB-01 | Payment_Transactions, Folio_Items | — | ✅ |
| FR-TOUR-06 | Admin/Manager lập lịch chuyến xe tour | UC-21 | PS-UC17.2 | BR-TR-01 | Tour_Schedules, Tour_Staff_Assignments | Config Panel | ✅ |
| FR-TOUR-07 | Hệ thống cảnh báo nếu nhân viên bị đặt trùng lịch | UC-21 | PS-UC17.2 | BR-TR-01 | Tour_Staff_Assignments | — | ✅ |
| FR-TOUR-08 | Hủy chuyến tour khẩn cấp và hoàn tiền tự động | UC-23 | PS-UC17.3 | BR-TR-05 | Tour_Schedules, Tour_Bookings, Payment_Transactions | — | ✅ |
| FR-TOUR-09 | Tự động hủy tour nếu dưới ngưỡng tối thiểu tại T-24h | UC-23 | PS-UC17.3 | BR-TR-05 | Tour_Schedules | — | ✅ |
| FR-TOUR-10 | Gửi email thông báo khách khi tour bị hủy | UC-23 | PS-UC17.3 | BR-TR-05 | — | — | ✅ |
| FR-TOUR-11 | Customer hủy đặt vé tour | UC-13 (SRS) | PS-UC17.3 | BR-TR-01 | Tour_Bookings, Payment_Transactions | Booking History | ✅ |

### 5.2 UC24 — Điểm danh AI Face Scan

| Req ID | Mô tả Yêu cầu | SRS UC | PS UC | Business Rule | Thực thể DB | Màn hình | Trạng thái |
|---|---|---|---|---|---|---|---|
| FR-AI-01 | Tour Guide chụp ảnh khách để điểm danh | UC-24 | PS-UC18 | BR-TR-02 | Tour_Attendees | AI Face Scan Attendance | ✅ |
| FR-AI-02 | Python AI service so khớp Cosine Similarity khuôn mặt | UC-24 | PS-UC18 | BR-TR-02 | Tour_Attendees | — | ✅ |
| FR-AI-03 | Ngưỡng khớp khuôn mặt >= 85% mới hợp lệ | UC-24 | PS-UC18 | BR-TR-02 | Tour_Attendees | — | ✅ |
| FR-AI-04 | Điểm danh thủ công khi AI thất bại | UC-24 | PS-UC18 | BR-TR-02 | Tour_Attendees | AI Face Scan Attendance | ✅ |

### 5.3 UC25/UC26 — Đánh giá & Kiểm duyệt

| Req ID | Mô tả Yêu cầu | SRS UC | PS UC | Business Rule | Thực thể DB | Màn hình | Trạng thái |
|---|---|---|---|---|---|---|---|
| FR-REV-01 | Customer gửi đánh giá trong vòng 7 ngày sau dịch vụ | UC-25 | PS-UC19 | BR-TR-03 | Reviews | Service Feedback Form | ✅ |
| FR-REV-02 | Chỉ khách đã hoàn tất dịch vụ mới gửi được đánh giá | UC-25 | PS-UC19 | BR-TR-03 | Reviews, Room_Booking_Details | Service Feedback Form | ✅ |
| FR-REV-03 | Đánh giá mới ở trạng thái Pending, chờ Admin duyệt | UC-25 | PS-UC19 | BR-TR-04 | Reviews | — | ✅ |
| FR-REV-04 | Admin ẩn/hiện đánh giá nhưng không sửa nội dung | UC-26 | PS-UC20 | BR-TR-04 | Reviews | Config & Audit Log Panel | ✅ |
| FR-REV-05 | Ghi Audit Log khi kiểm duyệt đánh giá | UC-26 | PS-UC20 | BR-SYS-04, BR-TR-04 | Audit_Logs | — | ✅ |

---

## 6. RTM Phân hệ 5 — Tài chính & Báo cáo

### 6.1 UC27/UC21 — Quản lý Folio

| Req ID | Mô tả Yêu cầu | SRS UC | PS UC | Business Rule | Thực thể DB | Màn hình | Trạng thái |
|---|---|---|---|---|---|---|---|
| FR-FIN-01 | Customer xem bảng kê chi tiêu Folio real-time | UC-27 | PS-UC21.1 | — | Folio_Items | Booking History & Folio | ✅ |
| FR-FIN-02 | Hệ thống ghi lịch sử mọi giao dịch tiền (đặt cọc, hoàn tiền) | UC-39 | PS-UC21.2 | — | Payment_Transactions | — | ✅ |
| FR-FIN-03 | Folio tổng hợp phòng + F&B + Tour khi Check-out | UC-28 | PS-UC21.3 | BR-FIN-01 | Folio_Items, Room_Booking_Details | Checkout & Bill Workspace | ✅ |
| FR-FIN-04 | Night Audit chạy tự động lúc 02:00 AM mỗi ngày | UC-30 | PS-UC21.4 | BR-FIN-03 | Folio_Items, Room_Booking_Details, Daily_Rates | — | ✅ |
| FR-FIN-05 | Night Audit ghi nợ tiền phòng vào Folio mỗi đêm | UC-30 | PS-UC21.4 | BR-FIN-03 | Folio_Items | — | ✅ |
| FR-FIN-06 | Tách hóa đơn Folio theo yêu cầu khách | — | PS-UC21.5 | — | Folio_Items | Checkout & Bill Workspace | ✅ |

### 6.2 UC29/UC22 — Thanh toán Check-out & Hóa đơn

| Req ID | Mô tả Yêu cầu | SRS UC | PS UC | Business Rule | Thực thể DB | Màn hình | Trạng thái |
|---|---|---|---|---|---|---|---|
| FR-INV-01 | Hệ thống tạo Consolidated Invoice tổng hợp | UC-28 | PS-UC22.1 | BR-FIN-01 | Consolidated_Invoices | Checkout & Bill Workspace | ✅ |
| FR-INV-02 | Lễ tân xử lý thanh toán cuối và xác nhận Check-out | UC-29 | PS-UC22.1 | BR-FIN-01 | Payment_Transactions, Consolidated_Invoices | Checkout & Bill Workspace | ✅ |
| FR-INV-03 | Phát hành e-Invoice PDF gửi qua email khách | UC-28 | PS-UC22.2 | — | Consolidated_Invoices | — | ✅ |
| FR-INV-04 | Áp dụng mã Voucher tại thời điểm Check-out | UC-38 | PS-UC07.2 | BR-FIN-05 | Promotions, Consolidated_Invoices | Checkout & Bill Workspace | ✅ |

### 6.3 UC30-33/UC23-25 — Dashboard & Báo cáo

| Req ID | Mô tả Yêu cầu | SRS UC | PS UC | Business Rule | Thực thể DB | Màn hình | Trạng thái |
|---|---|---|---|---|---|---|---|
| FR-RPT-01 | Manager xem Dashboard tài chính doanh thu theo ngày/tháng/năm | UC-31 | PS-UC23.1 | BR-FIN-04 | Consolidated_Invoices, Folio_Items | USALI Financial Analytics | ✅ |
| FR-RPT-02 | Doanh thu phân loại: Room, F&B, Tour (USALI) | UC-31 | PS-UC24 | BR-FIN-04 | Consolidated_Invoices | USALI Financial Analytics | ✅ |
| FR-RPT-03 | Manager xem Occupancy Rate phòng theo ngày | UC-31 | PS-UC23.2 | — | Rooms, Room_Booking_Details | USALI Financial Analytics | ✅ |
| FR-RPT-04 | Xuất báo cáo tài chính dạng Excel (.xlsx) | UC-33 | PS-UC25 | — | — | USALI Financial Analytics | ✅ |
| FR-RPT-05 | Xuất báo cáo tài chính dạng PDF | UC-33 | PS-UC25 | — | — | USALI Financial Analytics | ✅ |

---

## 7. RTM Business Rules vs Use Cases

Ma trận này thể hiện mỗi Business Rule được phủ sóng bởi những Use Cases nào.

| Business Rule | Mô tả ngắn | SRS Use Cases | PS Use Cases | Triển khai kỹ thuật |
|---|---|---|---|---|
| BR-SYS-01 | Mã hóa mật khẩu BCrypt & CCCD AES-256 | UC-01, UC-02, UC-05 | PS-UC01, UC03 | BCrypt filter, AES-256 encryption service |
| BR-SYS-02 | Khóa tài khoản 15 phút sau 5 lần sai | UC-01, UC-03, UC-04 | PS-UC01.2, UC02 | Login attempt counter + Lock scheduler |
| BR-SYS-03 | Phiên nhân viên hết hạn sau 15 phút inactive | UC-01 | Tất cả UC nhân viên | Spring Security session timeout |
| BR-SYS-04 | Audit Log bắt buộc, không xóa được | UC-32 | PS-UC04.2, UC09.3 | AOP_Data_Interception_Logger (Spring AOP) |
| BR-SYS-05 | Ẩn danh hóa dữ liệu khách khi xóa | UC-07 | — | Soft delete / anonymization service |
| BR-FO-01 | Chống Overbooking phòng | UC-10, UC-11 | PS-UC06, UC07.3 | SELECT FOR UPDATE + @Version + TRG_Prevent_Overbooking |
| BR-FO-02 | Hủy booking sau 15 phút không thanh toán | UC-11, UC-14 | PS-UC07.1, UC07.3 | Pending_Booking_Auto_Cancellation Scheduler |
| BR-FO-03 | Check-in yêu cầu 18 tuổi + ID hợp lệ | UC-13 | PS-UC09.1, UC09.4 | Validation trong Check-in form |
| BR-FO-04 | State Machine trạng thái phòng | UC-13, UC-08 SRS | PS-UC09, UC10 | Room_Status_State_Machine |
| BR-FO-05 | Rush Room ưu tiên | — | PS-UC10.3 | Priority update trong Hotel_Operations |
| BR-FB-01 | Ký nợ phòng: PIN + Credit Limit | UC-20 | PS-UC15 | Credit_Limit_Realtime_Validator + TRG_Folio_Credit_Limit_Check |
| BR-FB-02 | Hết món đồng bộ real-time toàn hệ thống | UC-18 | PS-UC14.3 | Menu_Item_Availability_Sync + WebSocket |
| BR-FB-03 | Giữ bàn nhà hàng tối đa 30 phút | UC-19 | PS-UC12 | Table reservation timeout scheduler |
| BR-TR-01 | Chống Overbooking tour | UC-22, UC-23 | PS-UC17.1, UC17.2 | TRG_Tour_Capacity_Validator + @Version |
| BR-TR-02 | AI Face Scan ngưỡng >= 85% | UC-24 | PS-UC18 | AI_Face_Vector_Matching_Service (Python) |
| BR-TR-03 | Gửi review trong 7 ngày | UC-25 | PS-UC19 | Review eligibility validator |
| BR-TR-04 | Admin không sửa nội dung review | UC-26 | PS-UC20 | Review moderation API restrictions |
| BR-TR-05 | Auto-hủy tour dưới ngưỡng T-24h | UC-23 | PS-UC17.3 | Auto-Cancel scheduler + Refund processor |
| BR-FIN-01 | Bắt buộc tất toán trước Check-out | UC-08 SRS, UC-29 | PS-UC22.1 | Checkout validation service |
| BR-FIN-02 | Chính sách hoàn tiền 48h | UC-06 SRS | PS-§5 | Cancellation policy calculator |
| BR-FIN-03 | Night Audit 02:00 AM | UC-30 | PS-UC21.4 | @Scheduled Task - Night Audit Service |
| BR-FIN-04 | USALI Revenue Classification | UC-31, UC-33 | PS-UC24 | USALI_Revenue_Decomposer |
| BR-FIN-05 | Tối đa 1 Voucher mỗi Booking | UC-38 | PS-UC07.2 | Voucher_Validity_Checker |
| BR-HK-01 | Auto-sinh task dọn sau Check-out | UC-34 | PS-UC10.1 | TRG_Auto_Housekeeping_Task (DB Trigger) |
| BR-HK-02 | Auto-khóa phòng khi báo hỏng | UC-36 | PS-UC10.4 | TRG_Maintenance_Auto_Bridge (DB Trigger) |
| BR-DAT-01 | Cấm xóa thực thể đang hoạt động | UC-09 | PS-UC05.1 | Delete validation guards |
| BR-DAT-02 | Nhân viên một vai trò | UC-08 | PS-UC04.1 | Role assignment validation |
| BR-DAT-03 | Giá động không chồng lấn ngày | UC-40 | PS-UC05.2 | Date overlap validator |

---

## 8. RTM Non-Functional Requirements

| NFR ID | Loại | Mô tả Yêu cầu | Business Rule liên quan | Thực hiện |
|---|---|---|---|---|
| NFR-SEC-01 | Bảo mật | Mã hóa dữ liệu nhạy cảm | BR-SYS-01 | BCrypt + AES-256 |
| NFR-SEC-02 | Bảo mật | Kiểm soát truy cập theo vai trò (RBAC) | BR-DAT-02 | Spring Security + JWT |
| NFR-SEC-03 | Bảo mật | Ghi nhật ký kiểm toán đầy đủ | BR-SYS-04 | Spring AOP Interceptor |
| NFR-SEC-04 | Bảo mật | Tuân thủ Nghị định 13/2023/NĐ-CP | BR-SYS-05 | Anonymization service |
| NFR-PERF-01 | Hiệu năng | Real-time cập nhật trạng thái phòng | BR-FO-04 | WebSocket connections |
| NFR-PERF-02 | Hiệu năng | Real-time KDS và POS sync | BR-FB-02 | WebSocket + Message Broker |
| NFR-PERF-03 | Hiệu năng | Chống đồng thời đặt phòng trùng | BR-FO-01 | Optimistic Locking + DB Lock |
| NFR-REL-01 | Độ tin cậy | Night Audit tự động không lỗi | BR-FIN-03 | Scheduled Task + Error alerting |
| NFR-REL-02 | Độ tin cậy | Trigger CSDL bảo vệ toàn vẹn dữ liệu | BR-FO-01, BR-FB-01, BR-HK-01, BR-TR-01 | Database Triggers |
| NFR-LEG-01 | Pháp lý | Khai báo lưu trú đầy đủ | BR-FO-03 | Check-in form mandatory fields |
| NFR-LEG-02 | Pháp lý | Chuẩn USALI cho báo cáo tài chính | BR-FIN-04 | USALI Revenue Decomposer |
| NFR-LEG-03 | Pháp lý | Chu kỳ AHLEI chuẩn quốc tế | — | System workflow design |
| NFR-INT-01 | Tích hợp | VNPay Sandbox Payment | BR-FO-02, BR-TR-05 | VNPay SDK integration |
| NFR-INT-02 | Tích hợp | SendGrid Email Service | BR-FO-02, BR-TR-05 | Mail Service integration |
| NFR-INT-03 | Tích hợp | OpenWeather API | — | Weather API integration |
| NFR-INT-04 | Tích hợp | AI Face Scan Microservice | BR-TR-02 | Python FastAPI + Spring Boot |

---

## 9. Bảng Phủ sóng Tổng hợp

### 9.1 Thống kê phủ sóng theo Phân hệ

| Phân hệ | Tổng Req | Đã phủ | Cần xem xét | Chưa phủ | Tỷ lệ phủ |
|---|---|---|---|---|---|
| Phân hệ 1 — Xác thực & Dữ liệu gốc | 31 | 31 | 0 | 0 | 100% |
| Phân hệ 2 — Phòng & Lễ tân | 37 | 37 | 0 | 0 | 100% |
| Phân hệ 3 — F&B / POS / KDS | 18 | 18 | 0 | 0 | 100% |
| Phân hệ 4 — Lữ hành & Đánh giá | 21 | 21 | 0 | 0 | 100% |
| Phân hệ 5 — Tài chính & Báo cáo | 16 | 16 | 0 | 0 | 100% |
| Non-Functional Requirements | 16 | 16 | 0 | 0 | 100% |
| **TỔNG CỘNG** | **139** | **139** | **0** | **0** | **100%** |

### 9.2 Thống kê Business Rules theo Mức độ ưu tiên

| Mức độ | Số lượng BR | Danh sách |
|---|---|---|
| **CRITICAL** | 7 | BR-SYS-01, BR-SYS-04, BR-FO-01, BR-FO-04, BR-FB-01, BR-TR-01, BR-FIN-01, BR-FIN-03 |
| **HIGH** | 15 | BR-SYS-02, BR-SYS-03, BR-SYS-05, BR-FO-02, BR-FO-03, BR-FB-02, BR-TR-02, BR-TR-05, BR-FIN-02, BR-FIN-04, BR-HK-01, BR-HK-02, BR-DAT-01, BR-DAT-02, BR-DAT-03 |
| **MEDIUM** | 6 | BR-FO-05, BR-FB-03, BR-TR-03, BR-TR-04, BR-FIN-05, và các BR phụ trợ |

### 9.3 Phân bổ DB Triggers tự động hóa

| Trigger Name | Kích hoạt khi | Business Rule | Tác động |
|---|---|---|---|
| `TRG_Auto_Housekeeping_Task` | Booking chuyển Checked_Out | BR-HK-01 | INSERT Hotel_Operations, UPDATE Rooms |
| `TRG_Prevent_Overbooking` | INSERT Room_Booking_Details | BR-FO-01 | Rollback nếu chồng lấn ngày |
| `TRG_Tour_Capacity_Validator` | INSERT Tour_Bookings | BR-TR-01 | Rollback nếu vượt max_capacity |
| `TRG_Folio_Credit_Limit_Check` | INSERT Folio_Items | BR-FB-01 | Rollback nếu vượt credit_limit |
| `TRG_Maintenance_Auto_Bridge` | Housekeeping báo hỏng | BR-HK-02 | INSERT Hotel_Operations, UPDATE Rooms->Maintenance |

---

*Tài liệu được phân tích bởi Antigravity — Business Analyst Review — ngày 2026-06-29*  
*Nguồn: SRS_Document_SWP391_G2.md + Project_Specification.md + business_rule.md*
