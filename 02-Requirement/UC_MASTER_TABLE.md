# 📑 BẢNG USE CASE TỔNG HỢP — KAWAI RESORT & TOUR HUB

| Field                 | Value                                                    |
| --------------------- | -------------------------------------------------------- |
| **Document ID** | `KAWAI-SRS-UC-MASTER-004`                              |
| **Version**     | 4.0                                                      |
| **Date**        | 2026-07-02                                               |
| **Status**      | Đồng bộ với codebase`05-Development/kawai-backend` |
| **Author**      | Nhóm SWP391 — G2                                       |

---

## CHANGELOG

| Ngày      | Người thực hiện | Nội dung                                                                                                                                                                                               |
| ---------- | ------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 2026-07-02 | Antigravity         | Đồng bộ hóa toàn diện tài liệu yêu cầu (Project Spec, SRS, Business Rules, Workflows, RTM) khớp 1:1 với codebase thực tế.                                                                 |
| 2026-06-28 | Nhóm G2            | Rà soát toàn bộ codebase; cập nhật trạng thái triển khai; bổ sung UC29–UC37 cho nghiệp vụ phát sinh (Walk-in, Workflow, Cronjob, Export/Import, Device Auth, Envers, Email, Dependents…) |
| 2026-06-16 | Antigravity         | Chuẩn hóa 60 UC con, 9 Actor, 5 Module                                                                                                                                                                |
| 2026-06-09 | Nhóm G2            | Khởi tạo bảng UC Master V3                                                                                                                                                                           |

---

## KÝ HIỆU TRẠNG THÁI TRIỂN KHAI

| Ký hiệu | Ý nghĩa                                                                                             |
| --------- | ----------------------------------------------------------------------------------------------------- |
| ✅        | **Hoàn thiện** — Luồng E2E chạy được trên production/demo                              |
| ⚠️      | **Một phần** — Backend hoặc UI có nhưng thiếu tích hợp / mock / bug / chưa expose API |
| ❌        | **Chưa làm** — Không có code hoặc chỉ có trong spec/test                                |

**Bằng chứng:** `05-Development/kawai-backend` (Spring Boot 3.2, port 8080)

---

## 👥 DANH SÁCH ACTOR (9 ACTOR)

1. **`Admin`** — Quản trị hệ thống, master data, audit, workflow, cronjob, phân quyền.
2. **`Manager`** — Báo cáo doanh thu, analytics, cấu hình giá/marketing.
3. **`Customer`** — Khách đã đăng ký: đặt phòng/tour, profile, order food, review.
4. **`Guest`** — Khách vãng lai: xem landing, tìm phòng (chưa login).
5. **`Receptionist`** — Check-in/out, walk-in, folio, in-house, night audit.
6. **`Housekeeper`** — Dọn phòng, báo sạch/bẩn *(service có, chưa có UI riêng)*.
7. **`Maintenance Staff`** — Sửa chữa thiết bị phòng *(service có, chưa có UI riêng)*.
8. **`Cashier` / `F&B Staff`** — POS dine-in, quản lý bàn, room service, post-to-room.
9. **`Tour Guide`** — Dashboard tour, điểm danh FaceID, cập nhật hành trình.

---

## 🔴 MOD1: HỆ THỐNG CỐT LÕI, XÁC THỰC & ADMIN CONFIG

| **UC ID** | **Tên Use Case**                                            | **Actor**             | **P** | **TT** |
| --------------- | ------------------------------------------------------------------ | --------------------------- | ----------- | ------------ |
| **UC01**  | **Quản lý Tài khoản & Xác thực**                       |                             |             |              |
| UC01.1          | Đăng ký khách hàng trực tuyến + xác thực OTP email        | `Customer`                | P0          | ✅           |
| UC01.2          | Admin khởi tạo tài khoản nhân viên / khách CRM              | `Admin`                   | P0          | ✅           |
| UC01.3          | Đăng nhập Guest (modal) + OAuth2 Google                         | `Customer`,`Guest`      | P0          | ✅           |
| UC01.4          | Đăng nhập nhân viên Ops (`/ops-login`) + redirect theo role | Toàn bộ nhân viên       | P0          | ✅           |
| UC01.5          | Khóa tài khoản tự động sau n lần đăng nhập sai           | System                      | P1          | ✅           |
| **UC02**  | Đặt lại mật khẩu (email token)                                | Toàn bộ User              | P1          | ✅           |
| **UC03**  | Quản lý hồ sơ cá nhân & avatar                               | `Customer`                | P1          | ✅           |
| UC03.1          | Cập nhật thông tin profile, đổi mật khẩu                    | `Customer`                | P1          | ✅           |
| UC03.2          | Upload avatar (`/api/v1/upload`)                                 | `Customer`                | P2          | ✅           |
| UC03.3          | Quản lý người phụ thuộc (Dependents)                         | `Customer`                | P1          | ✅           |
| **UC04**  | FaceID — Đăng ký & nhận diện khuôn mặt tour                | `Customer`,`Tour Guide` | P0          | ⚠️         |
| UC04.1          | Upload ảnh chân dung / vector khuôn mặt vào profile           | `Customer`                | P0          | ⚠️         |
| UC04.2          | Quét FaceID tại checkpoint tour (Python + face-api.js)           | `Tour Guide`              | P0          | ⚠️         |
| **UC05**  | **Phân quyền & An ninh nội bộ**                          |                             |             |              |
| UC05.1          | RBAC — Gán role & permission (`RolePermissionConstants`)       | `Admin`                   | P0          | ✅           |
| UC05.2          | Activity Audit Log (`AuditLog`, `@LogActivity`)                | `Admin`                   | P1          | ✅           |
| UC05.3          | Envers — Lịch sử thay đổi entity & rollback                   | `Admin`                   | P1          | ✅           |
| UC05.4          | Quản lý thiết bị ủy quyền Ops (`AuthorizedDevice`)         | `Admin`                   | P2          | ⚠️         |
| **UC06**  | **Master Data — Hạng phòng & Phòng vật lý**            |                             |             |              |
| UC06.1          | CRUD`Room_Categories`                                            | `Admin`                   | P1          | ✅           |
| UC06.2          | CRUD`Rooms`                                                      | `Admin`                   | P1          | ✅           |
| **UC07**  | **Master Data — Sơ đồ bàn ăn**                         |                             |             |              |
| UC07.1          | CRUD`Restaurant_Tables`                                          | `Admin`,`Manager`       | P1          | ✅           |
| UC07.2          | Cập nhật trạng thái / sức chứa bàn                          | `Admin`,`F&B Staff`     | P1          | ✅           |
| **UC08**  | **Master Data — Tour & Lịch trình**                       |                             |             |              |
| UC08.1          | CRUD`Tours` + toggle danh mục tour                              | `Admin`,`Manager`       | P1          | ✅           |
| UC08.2          | CRUD`Tour_Schedules` (xem admin, chưa CRUD API đầy đủ)      | `Admin`,`Manager`       | P1          | ⚠️         |
| UC08.3          | Cấu hình`Tour_Itineraries` / điểm dừng                      | `Admin`,`Manager`       | P1          | ⚠️         |
| **UC09**  | **Giá, Marketing & Vận hành Admin**                       |                             |             |              |
| UC09.1          | Cấu hình giá phòng động (`Dynamic_Pricing`)                | `Admin`,`Manager`       | P1          | ⚠️         |
| UC09.2          | CRUD bảng giá ngày (`Daily_Rates` / pricing tab)              | `Admin`                   | P0          | ✅           |
| UC09.3          | Phụ thu trẻ em theo khung tuổi (`Room_Surcharge`)             | `Admin`,`Manager`       | P1          | ❌           |
| UC09.4          | CRUD chiến dịch khuyến mãi (`Promotions`)                    | `Admin`,`Manager`       | P1          | ✅           |
| UC09.5          | CRUD thực đơn F&B (`Menu_Items`, toggle category)             | `Admin`,`Manager`       | P1          | ✅           |
| UC09.6          | Export Master Data CSV                                             | `Admin`                   | P2          | ✅           |
| UC09.7          | Import CSV (Promotions, Menu)                                      | `Admin`                   | P2          | ⚠️         |
| UC09.8          | Workflow phê duyệt nghiệp vụ (promo threshold, SLA)            | `Admin`                   | P1          | ⚠️         |
| UC09.9          | Quản lý Cronjob hệ thống (DynamicJobManager)                   | `Admin`                   | P1          | ✅           |
| UC09.10         | Admin Dashboard KPI (một số chart mock)                          | `Admin`                   | P1          | ⚠️         |

---

## 🔵 MOD2: QUẢN LÝ PHÒNG, LỄ TÂN & BUỒNG PHÒNG

| **UC ID** | **Tên Use Case**                                             | **Actor**               | **P** | **TT** |
| --------------- | ------------------------------------------------------------------- | ----------------------------- | ----------- | ------------ |
| **UC10**  | **Room Booking (Customer)**                                |                               |             |              |
| UC10.1    | Search Available Rooms                                     | `Customer`                  | P0          | ✅           |
| UC10.2    | Book Room & Pay Online Deposit                             | `Customer`                  | P0          | ✅           |
| UC10.3    | Cancel Booking                                             | `Customer`                  | P1          | ⚠️         |
| UC10.4    | Manage Accompanying/Dependent Guests                       | `Customer`                  | P1          | ✅           |
| UC10.5    | View Profile & Booking History                             | `Customer`                  | P1          | ✅           |
| **UC11**  | **Front Desk Operations (Receptionist)**                   |                               |             |              |
| UC11.1    | Check-In & Allocate Physical Rooms                         | `Receptionist`              | P0          | ⚠️         |
| UC11.2    | Walk-in Guest Check-in                                     | `Receptionist`              | P0          | ✅           |
| UC11.3    | Register Accompanying Guests                               | `Receptionist`              | P0          | ✅           |
| UC11.4    | Authorize Dependent Service Access                         | `Receptionist`              | P1          | ✅           |
| UC11.5    | Change Room Category/Room Type                             | `Receptionist`              | P1          | ⚠️         |
| UC11.6    | Room Matrix / Dashboard Monitoring                         | `Receptionist`              | P0          | ✅           |
| UC11.7    | Request Emergency Cleaning (Rush Room Preparation)         | `Receptionist`              | P1          | ⚠️         |

## 🟡 MOD3: F&B, POS & KDS

| **UC ID** | **Tên Use Case**                                       | **Actor**               | **P** | **TT** |
| --------------- | ------------------------------------------------------------- | ----------------------------- | ----------- | ------------ |
| **UC1**   | F&B Order Management                                      | `F&B Staff`             | P1 | ⚠️ |
| UC1.1     | View order details                                        | `F&B Staff`             | P1 | ✅ |
| UC1.2     | Add items to order (For Dine-in orders)                   | `F&B Staff`             | P1 | ⚠️ |
| UC1.3     | Update service status                                     | `F&B Staff`             | P1 | ✅ |
| UC1.4     | Print receipt / bill                                      | `F&B Staff`             | P2 | ⚠️ |
| **UC2**   | Confirm Order Payment                                     | `F&B Staff`             | P1 | ✅ |
| **UC3**   | **Table Management**                                      |                         |    |    |
| UC3.1     | Hold table (Time extension)                               | `F&B Staff`             | P1 | ✅ |
| UC3.2     | Pre-book table for in-house guests                        | `F&B Staff`             | P1 | ✅ |
| UC3.3     | Create Dine-in order for walk-in guests                   | `F&B Staff`             | P1 | ✅ |
| UC3.4     | Table check-in for pre-booked guests                      | `F&B Staff`             | P1 | ✅ |
| **UC4**   | **Room Service Order Management**                         |                         |    |    |
| UC4.1     | Create Room Service order                                 | `F&B Staff`             | P1 | ✅ |
| **UC5**   | Shift Reporting                                           | `F&B Staff`             | P1 | ⚠️ |
| **UC6**   | Update Individual Dish Status (KOT)                       | `Kitchen Staff`         | P1 | ✅ |
| **UC7**   | Manage Dish Availability (Available/Out of Stock)         | `Kitchen Staff`         | P1 | ✅ |
| **UC8**   | Online Table Reservation                                  | `Customer`              | P1 | ✅ |
| **UC9**   | Online Ordering                                           | `Customer`              | P1 | ✅ |
| **UC10**  | Cancel Order                                              | `F&B Staff`, `Customer` | P1 | ✅ |

## 🟢 MOD4: TOUR MANAGEMENT & ATTENDANCE

| **UC ID** | **Tên Use Case**                                       | **Actor**               | **P** | **TT** |
| --------------- | ------------------------------------------------------------- | ----------------------------- | ----------- | ------------ |
| **UC08**  | Manage Tour Core Data                                   | `Admin`                 | P1 | ⚠️ |
| **UC19**  | Search Available Tours                                   | `Customer`              | P1 | ✅ |
| **UC20**  | **Tour Booking & Operations**                            |                         |    |    |
| UC20.1    | Book Tour & Pay Online                                   | `Customer`,`Receptionist`| P0 | ✅ |
| UC20.2    | Assign Staff and Vehicle to Tour Schedule                | `Admin`,`Coordinator`     | P1 | ⚠️ |
| UC20.3    | Cancel Tour Booking                                      | `Customer`,`Resort`       | P1 | ⚠️ |
| UC20.4    | Post Tour Charge to Room                                 | `Customer`,`Receptionist`| P0 | ✅ |
| **UC21**  | **Tour Attendance & Checking**                           |                         |    |    |
| UC21.1    | AI Face Scan Attendance Check-In                         | `Customer`,`Tour Guide`   | P0 | ⚠️ |
| UC21.2    | Manual Tour Check-In                                     | `Tour Guide`              | P1 | ✅ |
| UC21.3    | Start & Conclude Tour Schedule                           | `Tour Guide`              | P1 | ⚠️ |
| UC21.4    | Reset Tour Status                                        | `Tour Guide`              | P2 | ✅ |

## 🟣 MOD5: FOLIO, VẬN HÀNH & BÁO CÁO

| **UC ID** | **Tên Use Case**                                             | **Actor**               | **P** | **TT** |
| --------------- | ------------------------------------------------------------------- | ----------------------------- | ----------- | ------------ |
| **UC_REC**| **Lễ tân (Folio & Check-out)**                             |                               |             |              |
| UC_REC.1  | Theo dõi dư nợ Folio (Real-time)                           | `Receptionist`                | P0          | ✅           |
| UC_REC.2  | Ghi nhận Charge-to-Room                                    | `Receptionist`                | P0          | ✅           |
| UC_REC.3  | Tách / Gộp hóa đơn (Split/Merge Folio)                     | `Receptionist`                | P1          | ✅           |
| UC_REC.4  | Thêm Phụ thu (Surcharge) & Giảm giá                        | `Receptionist`                | P1          | ✅           |
| UC_REC.5  | Khởi tạo quy trình Check-out                               | `Receptionist`                | P0          | ✅           |
| UC_REC.7  | Thanh toán Consolidated Invoice                            | `Receptionist`                | P0          | ✅           |
| UC_REC.9  | Hoàn tất Check-out                                         | `Receptionist`                | P0          | ✅           |
| **UC_HK** | **Buồng phòng (Housekeeping)**                             |                               |             |              |
| UC_HK.1   | Xem danh sách Task dọn phòng                               | `Housekeeper`                 | P1          | ⚠️         |
| UC_HK.2   | Cập nhật tiến độ dọn dẹp                                   | `Housekeeper`                 | P1          | ⚠️         |
| UC_HK.3   | Nhận thông báo dọn khẩn (Rush Room)                        | `Housekeeper`                 | P1          | ⚠️         |
| UC_HK.4   | Kiểm phòng Check-out (Room Check)                          | `Housekeeper`                 | P0          | ⚠️         |
| UC_HK.5   | Kiểm tra Minibar & Bổ sung tiện ích                        | `Housekeeper`                 | P1          | ❌           |
| UC_HK.6   | Báo cáo hỏng hóc (Create Ticket)                           | `Housekeeper`                 | P1          | ⚠️         |
| UC_HK.7   | Ghi nhận đồ thất lạc (Lost & Found)                        | `Housekeeper`                 | P2          | ❌           |
| **UC_MT** | **Bảo trì (Maintenance)**                                  |                               |             |              |
| UC_MT.1   | Tiếp nhận Ticket bảo trì                                   | `Maintenance Staff`           | P1          | ⚠️         |
| UC_MT.2   | Cập nhật trạng thái sửa chữa                               | `Maintenance Staff`           | P1          | ⚠️         |
| UC_MT.5   | Hoàn tất & Mở khóa phòng                                   | `Maintenance Staff`           | P1          | ⚠️         |
| **UC_MNG**| **Quản lý (Manager)**                                      |                               |             |              |
| UC_MNG.1  | Phân tích Doanh thu & Công suất                            | `Manager`                     | P1          | ⚠️         |
| UC_MNG.2  | Phân tích Chéo (Cross-selling)                             | `Manager`                     | P2          | ❌           |
| UC_MNG.3  | Phê duyệt Ngoại lệ (Manager Approval)                      | `Manager`                     | P1          | ⚠️         |
| UC_MNG.6  | Trích xuất Báo cáo Kế toán (USALI)                         | `Manager`                     | P1          | ⚠️         |

## 🆕 MOD6: HỆ THỐNG & TÍCH HỢP (Phát sinh sau SRS gốc)

| **UC ID** | **Tên Use Case**                         | **Actor**        | **P** | **TT** |
| --------------- | ----------------------------------------------- | ---------------------- | ----------- | ------------ |
| **UC29**  | Hệ thống email thông báo đa sự kiện      | System                 | P0          | ✅           |
| UC29.1          | OTP đăng ký, reset password                  | System                 | P0          | ✅           |
| UC29.2          | Xác nhận booking / tour / đặt bàn / hủy   | System                 | P0          | ✅           |
| UC29.3          | Hóa đơn, gia hạn giữ bàn, SLA workflow    | System                 | P1          | ✅           |
| UC29.4          | Preview template email (dev)                    | `Admin`              | P3          | ✅           |
| **UC30**  | Scheduled Jobs tự động                       | System                 | P0          | ✅           |
| UC30.1          | `booking_cleanup` — giải phóng hold phòng | System                 | P0          | ✅           |
| UC30.2          | `reservation_cleanup` / `table_cleanup`     | System                 | P1          | ✅           |
| UC30.3          | `audit_cleanup` / `workflow_processor`      | System                 | P1          | ✅           |
| **UC31**  | Landing pages & trải nghiệm khách            | `Guest`,`Customer` | P1          | ✅           |
| UC31.1          | Trang Living / Wellbeing / Dining / Experiences | `Guest`              | P2          | ✅           |
| UC31.2          | Lịch sử booking & quản lý booking online    | `Customer`           | P1          | ✅           |

---

## 📊 THỐNG KÊ PHỦ SÓNG (2026-06-28)

| Module          | Tổng UC con  | ✅           | ⚠️         | ❌          |
| --------------- | ------------- | ------------ | ------------ | ----------- |
| MOD1            | 35            | 24           | 9            | 2           |
| MOD2            | 18            | 11           | 6            | 1           |
| MOD3            | 16            | 13           | 3            | 0           |
| MOD4            | 17            | 7            | 6            | 4           |
| MOD5            | 16            | 12           | 3            | 1           |
| MOD6            | 9             | 9            | 0            | 0           |
| **TỔNG** | **111** | **76** | **27** | **8** |

**Tỷ lệ có code:** ~93% (✅ + ⚠️) | **Hoàn thiện E2E:** ~68% (✅)

---

## 📋 MA TRẬN PHÂN CÔNG (Tham chiếu)

| Module | UC chính  | Phụ trách gốc | Ghi chú triển khai                                        |
| ------ | ---------- | ---------------- | ----------------------------------------------------------- |
| MOD1   | UC01–UC09 | SV1              | Master data + admin mở rộng (export/import/workflow/cron) |
| MOD2   | UC10–UC13 | SV2              | Walk-in, folio mạnh; housekeeping UI thiếu                |
| MOD3   | UC14–UC19 | SV3              | POS/KDS hoàn chỉnh; shift report mock                     |
| MOD4   | UC20–UC25 | SV4              | Tour + FaceID demo; add-ons chưa làm                      |
| MOD5   | UC26–UC28 | SV5              | VNPay đa luồng; night audit cần sửa bug status          |
| MOD6   | UC29–UC31 | Chung            | Email + jobs + landing — phát sinh sau SRS                |

---

## 🔗 TÀI LIỆU LIÊN QUAN

- Chi tiết luồng & API: `UC_DETAIL_SPEC.md` (v4.0)
- Đặc tả dự án: `Project_Specification.md`
- Traceability: `TRACEABILITY_MATRIX.md`
- Test spec: `06-Testing/MASTER_TDD_SPEC.md`




