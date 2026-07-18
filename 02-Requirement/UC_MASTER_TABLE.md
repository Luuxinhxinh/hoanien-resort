# 📑 BẢNG USE CASE TỔNG HỢP — KAWAI RESORT & TOUR HUB

| Field                 | Value                                                                       |
| --------------------- | --------------------------------------------------------------------------- |
| **Document ID** | `KAWAI-SRS-UC-MASTER-005`                                                 |
| **Version**     | 5.0                                                                         |
| **Date**        | 2026-07-16                                                                  |
| **Status**      | Đồng bộ với codebase`05-Development/kawai-backend` và `danhmay.md` |
| **Author**      | Nhóm SWP391 — G2                                                          |

---

## CHANGELOG

| Ngày      | Người thực hiện | Nội dung                                                                                                                                                 |
| ---------- | ------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 2026-07-16 | Antigravity         | Đồng bộ hóa toàn bộ UC ID (UC01-UC52) theo`danhmay.md`; phân chia lại 6 module (MOD1-MOD5 + MOD_SYS); chuẩn hóa tên & description khớp 1:1. |
| 2026-07-02 | Antigravity         | Đồng bộ hóa toàn diện tài liệu yêu cầu khớp 1:1 với codebase thực tế.                                                                       |
| 2026-06-28 | Nhóm G2            | Rà soát toàn bộ codebase; cập nhật trạng thái; bổ sung UC cho nghiệp vụ phát sinh.                                                            |
| 2026-06-16 | Antigravity         | Chuẩn hóa 60 UC con, 9 Actor, 5 Module                                                                                                                  |
| 2026-06-09 | Nhóm G2            | Khởi tạo bảng UC Master V3                                                                                                                             |

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
6. **`Housekeeper`** — Dọn phòng, báo sạch/bẩn.
7. **`Maintenance Staff`** — Sửa chữa thiết bị phòng.
8. **`Cashier` / `F&B Staff`** — POS dine-in, quản lý bàn, room service, post-to-room.
9. **`Tour Guide`** — Dashboard tour, điểm danh FaceID, cập nhật hành trình.

---

## 🔴 MOD1: HỆ THỐNG CỐT LÕI, XÁC THỰC & ADMIN CONFIG (UC01 – UC09)

| **UC ID** | **Tên Use Case**                                            | **Actor**              | **P** | **TT** |
| --------------- | ------------------------------------------------------------------ | ---------------------------- | ----------- | ------------ |
| **UC01**  | **Manage Identity and Access**                               |                              |             |              |
| UC01.1          | Đăng ký khách hàng trực tuyến + xác thực OTP email        | `Customer`                 | P0          | ✅           |
| UC01.2          | Admin khởi tạo tài khoản nhân viên / khách CRM              | `Admin`                    | P0          | ✅           |
| UC01.3          | Đăng nhập Guest (modal) + OAuth2 Google                         | `Customer`, `Guest`      | P0          | ✅           |
| UC01.4          | Đăng nhập nhân viên Ops (`/ops-login`) + redirect theo role | Toàn bộ nhân viên        | P0          | ✅           |
| UC01.5          | Khóa tài khoản tự động sau n lần đăng nhập sai           | System                       | P1          | ✅           |
| **UC02**  | **Reset Password**                                           | Toàn bộ User               | P1          | ✅           |
| **UC03**  | **Manage Personal Profile**                                  | `Customer`                 | P1          | ✅           |
| UC03.1          | Cập nhật thông tin profile, đổi mật khẩu                    | `Customer`                 | P1          | ✅           |
| UC03.2          | Upload avatar (`/api/v1/upload`)                                 | `Customer`                 | P2          | ✅           |
| UC03.3          | Quản lý người phụ thuộc (Dependents)                         | `Customer`                 | P1          | ✅           |
| **UC04**  | **Scan FaceID**                                              | `Customer`, `Tour Guide` | P0          | ⚠️         |
| UC04.1          | Upload ảnh chân dung / vector khuôn mặt vào profile           | `Customer`                 | P0          | ⚠️         |
| UC04.2          | Quét FaceID tại checkpoint tour (Python + face-api.js)           | `Tour Guide`               | P0          | ⚠️         |
| **UC05**  | **Manage Access Control and Security**                       |                              |             |              |
| UC05.1          | RBAC — Gán role & permission (`RolePermissionConstants`)       | `Admin`                    | P0          | ✅           |
| UC05.2          | Activity Audit Log (`AuditLog`, `@LogActivity`)                | `Admin`                    | P1          | ✅           |
| UC05.3          | Envers — Lịch sử thay đổi entity & rollback                   | `Admin`                    | P1          | ✅           |
| UC05.4          | Quản lý thiết bị ủy quyền Ops (`AuthorizedDevice`)         | `Admin`                    | P2          | ⚠️         |
| **UC06**  | **Manage Room Categories and Rooms**                         |                              |             |              |
| UC06.1          | CRUD`Room_Categories`                                            | `Admin`                    | P1          | ✅           |
| UC06.2          | CRUD`Rooms`                                                      | `Admin`                    | P1          | ✅           |
| **UC07**  | **Manage Dining Tables** (Master Data)                       |                              |             |              |
| UC07.1          | CRUD`Restaurant_Tables`                                          | `Admin`, `Manager`       | P1          | ✅           |
| UC07.2          | Cập nhật trạng thái / sức chứa bàn                          | `Admin`, `F&B Staff`     | P1          | ✅           |
| **UC08**  | **Manage Tours** (Master Data)                               |                              |             |              |
| UC08.1          | CRUD`Tours` + toggle danh mục tour                              | `Admin`, `Manager`       | P1          | ✅           |
| UC08.2          | CRUD`Tour_Schedules` (chưa CRUD API đầy đủ)                 | `Admin`, `Manager`       | P1          | ⚠️         |
| UC08.3          | Cấu hình`Tour_Itineraries` / điểm dừng                      | `Admin`, `Manager`       | P1          | ⚠️         |
| **UC09**  | **Manage Pricing, Marketing, and Operations**                |                              |             |              |
| UC09.1          | Cấu hình giá phòng động (`Dynamic_Pricing`)                | `Admin`, `Manager`       | P1          | ⚠️         |
| UC09.2          | CRUD bảng giá ngày (`Daily_Rates` / pricing tab)              | `Admin`                    | P0          | ✅           |
| UC09.3          | Phụ thu trẻ em theo khung tuổi (`Room_Surcharge`)             | `Admin`, `Manager`       | P1          | ❌           |
| UC09.4          | CRUD chiến dịch khuyến mãi (`Promotions`)                    | `Admin`, `Manager`       | P1          | ✅           |
| UC09.5          | CRUD thực đơn F&B (`Menu_Items`, toggle category)             | `Admin`, `Manager`       | P1          | ✅           |
| UC09.6          | Export Master Data CSV                                             | `Admin`                    | P2          | ✅           |
| UC09.7          | Import CSV (Promotions, Menu)                                      | `Admin`                    | P2          | ⚠️         |
| UC09.8          | Workflow phê duyệt nghiệp vụ (promo threshold, SLA)            | `Admin`                    | P1          | ⚠️         |
| UC09.9          | Quản lý Cronjob hệ thống (DynamicJobManager)                   | `Admin`                    | P1          | ✅           |
| UC09.10         | Admin Dashboard KPI (một số chart mock)                          | `Admin`                    | P1          | ⚠️         |

---

## 🔵 MOD2: QUẢN LÝ PHÒNG, LỄ TÂN & BUỒNG PHÒNG (UC10 – UC21)

| **UC ID** | **Tên Use Case**                                      | **Actor**  | **P** | **TT** |
| --------------- | ------------------------------------------------------------ | ---------------- | ----------- | ------------ |
| **UC10**  | **Search Available Rooms**                             | `Customer`     | P0          | ✅           |
| **UC11**  | **Book Room & Pay Online Deposit**                     | `Customer`     | P0          | ✅           |
| **UC12**  | **Cancel Booking**                                     | `Customer`     | P1          | ⚠️         |
| **UC13**  | **Manage Accompanying/Dependent Guests**               | `Customer`     | P1          | ✅           |
| **UC14**  | **View Profile & Booking History**                     | `Customer`     | P1          | ✅           |
| **UC15**  | **Check-In & Allocate Physical Rooms**                 |                  |             |              |
| UC15.1          | Check-In thường & gán phòng vật lý                     | `Receptionist` | P0          | ⚠️         |
| UC15.2          | Xử lý phụ thu Early Check-in / Late Check-out             | `Receptionist` | P1          | ✅           |
| UC15.3          | Xử lý yêu cầu đổi phòng trong thời gian lưu trú    | `Receptionist` | P1          | ⚠️         |
| **UC16**  | **Walk-in Guest Check-in**                             | `Receptionist` | P0          | ✅           |
| **UC17**  | **Register Accompanying Guests**                       | `Receptionist` | P0          | ✅           |
| **UC18**  | **Upgrade Dependent to Customer**                      | `Receptionist` | P1          | ✅           |
| **UC19**  | **Change Room Category/Room Type**                     | `Receptionist` | P1          | ⚠️         |
| **UC20**  | **Room Matrix / Dashboard Monitoring**                 | `Receptionist` | P0          | ✅           |
| **UC21**  | **Request Emergency Cleaning (Rush Room Preparation)** | `Receptionist` | P1          | ⚠️         |

---

## 🟡 MOD3: F&B, POS & KDS (UC22 – UC32)

| **UC ID** | **Tên Use Case**                  | **Actor**             | **P** | **TT** |
| --------------- | ---------------------------------------- | --------------------------- | ----------- | ------------ |
| **UC22**  | **Manage F&B Orders**              |                             |             |              |
| UC22.1          | View order details                       | `F&B Staff`               | P1          | ✅           |
| UC22.2          | Add items to order (Dine-in)             | `F&B Staff`               | P1          | ⚠️         |
| UC22.3          | Update service status                    | `F&B Staff`               | P1          | ✅           |
| UC22.4          | Print receipt / bill                     | `F&B Staff`               | P2          | ⚠️         |
| **UC23**  | **Confirm Payment**                | `F&B Staff`               | P1          | ✅           |
| **UC24**  | **Manage Dining Tables** (F&B POS) |                             |             |              |
| UC24.1          | Hold table (Time extension)              | `F&B Staff`               | P1          | ✅           |
| UC24.2          | Pre-book table for in-house guests       | `F&B Staff`               | P1          | ✅           |
| UC24.3          | Create Dine-in order for walk-in guests  | `F&B Staff`               | P1          | ✅           |
| UC24.4          | Table check-in for pre-booked guests     | `F&B Staff`               | P1          | ✅           |
| **UC25**  | **Manage Room Service Orders**     |                             |             |              |
| UC25.1          | Create Room Service order                | `F&B Staff`               | P1          | ✅           |
| **UC26**  | **Generate Shift Report**          | `F&B Staff`               | P1          | ⚠️         |
| **UC27**  | **Update Dish Status (KOT)**       | `Kitchen Staff`           | P1          | ✅           |
| **UC28**  | **Manage Dish Availability**       | `Kitchen Staff`           | P1          | ✅           |
| **UC29**  | **Reserve Table Online**           | `Customer`                | P1          | ✅           |
| **UC30**  | **Place Order Online**             | `Customer`                | P1          | ✅           |
| **UC31**  | **Cancel Order**                   | `F&B Staff`, `Customer` | P1          | ✅           |
| **UC32**  | **Cancel Table Reservation**       | `Customer`, `F&B Staff` | P1          | ✅           |

---

## 🟢 MOD4: TOUR MANAGEMENT & ATTENDANCE (UC33 – UC42)

| **UC ID** | **Tên Use Case**                                     | **Actor**                | **P** | **TT** |
| --------------- | ----------------------------------------------------------- | ------------------------------ | ----------- | ------------ |
| **UC33**  | **Manage Tour Core Data**                             | `Admin`                      | P1          | ⚠️         |
| **UC34**  | **Search Available Tours**                            | `Customer`                   | P1          | ✅           |
| **UC35**  | **Book Tour and Process Payment**                     |                                |             |              |
| UC35.1          | Book Tour & Pay Online                                      | `Customer`, `Receptionist` | P0          | ✅           |
| UC35.2          | Tính giá theo chính sách tuổi (infant free, child 50%) | `Customer`                   | P0          | ✅           |
| UC35.3          | Áp mã khuyến mãi (Promo Code)                           | `Customer`                   | P1          | ✅           |
| UC35.4          | Thanh toán tiền mặt / Post to Room                       | `Receptionist`               | P1          | ✅           |
| **UC36**  | **Assign Staff and Vehicles to Tour Schedule**        | `Admin`, `Coordinator`     | P1          | ⚠️         |
| **UC37**  | **Cancel Tour Booking**                               | `Customer`, `Resort`       | P1          | ⚠️         |
| **UC38**  | **Post Tour Charges to Room**                         | `Customer`, `Receptionist` | P0          | ✅           |
| **UC39**  | **Check In Tour Participants Using Face Recognition** |                                |             |              |
| UC39.1          | AI Face Scan Attendance Check-In                            | `Customer`, `Tour Guide`   | P0          | ⚠️         |
| **UC40**  | **Check In Tour Participants Manually**               | `Tour Guide`                 | P1          | ✅           |
| **UC41**  | **Start and Complete Tour Schedule**                  | `Tour Guide`                 | P1          | ⚠️         |
| **UC42**  | **Reset Tour Status**                                 | `Tour Guide`                 | P2          | ✅           |

## 🟣 MOD5: BILLING, VẬN HÀNH & BÁO CÁO (UC43 – UC49)

| **UC ID** | **Tên Use Case**                                                      | **Actor**       | **P** | **TT** |
| --------------- | ---------------------------------------------------------------------------- | --------------------- | ----------- | ------------ |
| **UC43**  | **Manage Guest Folio & Charges**                                       |                       |             |              |
| UC43.1          | Theo dõi dư nợ Folio (Real-time)                                          | `Receptionist`      | P0          | ✅           |
| UC43.2          | Ghi nhận Charge-to-Room                                                     | `Receptionist`      | P0          | ✅           |
| UC43.3          | Tách / Gộp hóa đơn (Split/Merge Folio)                                  | `Receptionist`      | P1          | ✅           |
| UC43.4          | Thêm Phụ thu (Surcharge) & Giảm giá                                      | `Receptionist`      | P1          | ✅           |
| **UC44**  | **Process Check-out & Payments**                                       |                       |             |              |
| UC44.1          | Khởi tạo quy trình Check-out (trigger Housekeeping)                       | `Receptionist`      | P0          | ✅           |
| UC44.2          | Thanh toán Consolidated Invoice                                             | `Receptionist`      | P0          | ✅           |
| UC44.3          | Hoàn tất Check-out (cập nhật trạng thái phòng)                        | `Receptionist`      | P0          | ✅           |
| **UC45**  | **Manage Cleaning Tasks & Status**                                     |                       |             |              |
| UC45.1          | Xem danh sách Task dọn phòng                                              | `Housekeeper`       | P1          | ⚠️         |
| UC45.2          | Cập nhật tiến độ dọn dẹp                                              | `Housekeeper`       | P1          | ⚠️         |
| UC45.3          | Nhận thông báo dọn khẩn (Rush Room)                                     | `Housekeeper`       | P1          | ⚠️         |
| **UC46**  | **Room Inspection & Incident Reporting**                               |                       |             |              |
| UC46.1          | Kiểm phòng Check-out (Room Check)                                          | `Housekeeper`       | P0          | ⚠️         |
| UC46.2          | Kiểm tra Minibar & Bổ sung tiện ích                                      | `Housekeeper`       | P1          | ❌           |
| UC46.3          | Báo cáo hỏng hóc (Create Ticket)                                         | `Housekeeper`       | P1          | ⚠️         |
| UC46.4          | Ghi nhận đồ thất lạc (Lost & Found)                                     | `Housekeeper`       | P2          | ❌           |
| **UC47**  | **Manage Maintenance Workflow**                                        |                       |             |              |
| UC47.1          | Tiếp nhận Ticket bảo trì                                                 | `Maintenance Staff` | P1          | ⚠️         |
| UC47.2          | Cập nhật trạng thái sửa chữa (Pending → In Progress → Completed)     | `Maintenance Staff` | P1          | ⚠️         |
| UC47.3          | Hoàn tất & Mở khóa phòng                                                | `Maintenance Staff` | P1          | ⚠️         |
| **UC48**  | **Manage Hotel Analytics**                                             |                       |             |              |
| UC48.1          | Phân tích Doanh thu & Công suất (Occupancy, RevPAR, ADR)                 | `Manager`           | P1          | ⚠️         |
| UC48.2          | Phân tích Chéo (Cross-selling)                                            | `Manager`           | P2          | ❌           |
| UC48.3          | Trích xuất Báo cáo Kế toán (USALI)                                     | `Manager`           | P1          | ⚠️         |
| **UC49**  | **Manage Hotel Operations** (Exception Approvals)                      |                       |             |              |
| UC49.1          | Phê duyệt Ngoại lệ (hủy miễn phí, hoàn tiền, giảm giá Folio >15%) | `Manager`           | P1          | ⚠️         |

---

## 🆕 MOD_SYS: HỆ THỐNG & TÍCH HỢP (UC50 – UC52)

| **UC ID** | **Tên Use Case**                                   | **Actor**         | **P** | **TT** |
| --------------- | --------------------------------------------------------- | ----------------------- | ----------- | ------------ |
| **UC50**  | **Send Email Notifications**                        | System                  | P0          | ✅           |
| UC50.1          | OTP đăng ký, reset password                            | System                  | P0          | ✅           |
| UC50.2          | Xác nhận booking / tour / đặt bàn / hủy             | System                  | P0          | ✅           |
| UC50.3          | Hóa đơn, gia hạn giữ bàn, SLA workflow              | System                  | P1          | ✅           |
| UC50.4          | Preview template email (dev)                              | `Admin`               | P3          | ✅           |
| **UC51**  | **Manage Scheduled Jobs**                           | System /`Admin`       | P0          | ✅           |
| UC51.1          | `booking_cleanup` — giải phóng hold phòng           | System                  | P0          | ✅           |
| UC51.2          | `reservation_cleanup` / `table_cleanup`               | System                  | P1          | ✅           |
| UC51.3          | `audit_cleanup` / `workflow_processor`                | System                  | P1          | ✅           |
| **UC52**  | **View Landing and Booking History**                | `Guest`, `Customer` | P1          | ✅           |
| UC52.1          | Landing pages (Living / Wellbeing / Dining / Experiences) | `Guest`               | P2          | ✅           |
| UC52.2          | Lịch sử booking & quản lý booking online              | `Customer`            | P1          | ✅           |

---

## 📊 THỐNG KÊ PHỦ SÓNG (2026-07-16)

| Module             | UC Chính    | UC Con       | ✅           | ⚠️         | ❌          |
| ------------------ | ------------ | ------------ | ------------ | ------------ | ----------- |
| MOD1 (UC01–09)    | 9            | 21           | 15           | 5            | 1           |
| MOD2 (UC10–21)    | 12           | 16           | 10           | 5            | 1           |
| MOD3 (UC22–32)    | 11           | 15           | 12           | 3            | 0           |
| MOD4 (UC33–42)    | 10           | 14           | 7            | 5            | 2           |
| MOD5 (UC43–49)    | 7            | 18           | 8            | 8            | 2           |
| MOD_SYS (UC50–52) | 3            | 9            | 9            | 0            | 0           |
| **TỔNG**    | **52** | **93** | **61** | **26** | **6** |

**Tỷ lệ có code:** ~94% (✅ + ⚠️) | **Hoàn thiện E2E:** ~65% (✅)

---

## 📋 MA TRẬN PHÂN CÔNG (Tham chiếu)

| Module  | UC chính  | Phụ trách gốc | Ghi chú triển khai                                        |
| ------- | ---------- | ---------------- | ----------------------------------------------------------- |
| MOD1    | UC01–UC09 | SV1              | Master data + admin mở rộng (export/import/workflow/cron) |
| MOD2    | UC10–UC21 | SV2              | Walk-in, folio mạnh; housekeeping UI thiếu                |
| MOD3    | UC22–UC32 | SV3              | POS/KDS hoàn chỉnh; shift report mock                     |
| MOD4    | UC33–UC42 | SV4              | Tour + FaceID demo; add-ons chưa làm                      |
| MOD5    | UC43–UC49 | SV5              | Folio/checkout hoàn chỉnh; HK/MT UI thiếu                |
| MOD_SYS | UC50–UC52 | Chung            | Email + jobs + landing — cross-cutting concerns            |

---

## 🔗 TÀI LIỆU LIÊN QUAN

- Chi tiết luồng & API: `UC_DETAIL_SPEC.md` (v5.0)
- Danh mục UC nguồn: `danhmay.md`
- Đặc tả dự án: `Project_Specification.md`
- Traceability: `TRACEABILITY_MATRIX.md`
- Test spec: `06-Testing/MASTER_TDD_SPEC.md`
