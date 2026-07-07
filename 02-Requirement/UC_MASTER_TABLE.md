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
| **UC10**  | Tìm kiếm phòng trống & giá theo ngày                          | `Customer`,`Guest`        | P0          | ✅           |
| **UC11**  | Khóa giữ phòng tạm (Cart Lock /`holdExpiresAt` + job cleanup) | `Customer`, System          | P0          | ✅           |
| **UC12**  | **Nghiệp vụ Sảnh (Front Desk)**                            |                               |             |              |
| UC12.1          | Áp dụng mã khuyến mãi / voucher khi đặt phòng               | `Customer`                  | P1          | ✅           |
| UC12.2          | Khai báo hành khách & Primary Contact                            | `Customer`,`Receptionist` | P0          | ✅           |
| UC12.3          | Check-in sảnh (form web, gán phòng vật lý)                     | `Receptionist`              | P0          | ⚠️         |
| UC12.4          | Ủy quyền hạn mức chi tiêu Post-to-Room + PIN                   | `Receptionist`              | P1          | ✅           |
| UC12.5          | Đổi phòng vật lý (`CheckinService.transferRoom`)             | `Receptionist`              | P1          | ⚠️         |
| UC12.6          | Check-out sảnh + consolidated invoice                              | `Receptionist`              | P0          | ✅           |
| UC12.7          | Walk-in Check-in (khách vãng lai + VNPay)                         | `Receptionist`              | P0          | ✅           |
| UC12.8          | Dashboard lễ tân & Room Matrix                                    | `Receptionist`              | P0          | ✅           |
| UC12.9          | Danh sách In-house                                                 | `Receptionist`              | P0          | ✅           |
| **UC13**  | **Buồng phòng & Bảo trì**                                 |                               |             |              |
| UC13.1          | Tự động tạo task dọn phòng khi check-out                      | System                        | P0          | ⚠️         |
| UC13.2          | App nhân viên buồng — cập nhật tiến độ dọn                | `Housekeeper`               | P0          | ❌           |
| UC13.3          | Rush Room — ưu tiên dọn phòng                                  | `Receptionist`              | P1          | ⚠️         |
| UC13.4          | Báo hỏng thiết bị / tạo ticket bảo trì                       | `Housekeeper`               | P1          | ⚠️         |
| UC13.5          | Hoàn thành sửa chữa & mở khóa phòng                          | `Maintenance Staff`         | P1          | ⚠️         |

---

## 🟡 MOD3: F&B, POS & KDS

| **UC ID** | **Tên Use Case**                                     | **Actor**               | **P** | **TT** |
| --------------- | ----------------------------------------------------------- | ----------------------------- | ----------- | ------------ |
| **UC14**  | Đặt giữ bàn nhà hàng + gia hạn hold                  | `Customer`,`Receptionist` | P1          | ✅           |
| **UC15**  | Cấu hình thực đơn & nhãn dị ứng (admin master data) | `Admin`,`Manager`         | P1          | ✅           |
| **UC16**  | Đặt món Room Service / E-Menu online                     | `Customer`                  | P1          | ✅           |
| UC16.1          | Auth modal khi chưa login; redirect giữ giỏ hàng        | `Customer`,`Guest`        | P1          | ✅           |
| UC16.2          | Thanh toán VNPay đơn room service                        | `Customer`                  | P1          | ✅           |
| UC16.3          | Ghi nợ phòng (Charge-to-Room → Folio)                    | `Customer`                  | P1          | ✅           |
| **UC17**  | POS Dine-In — lên đơn tại bàn                         | `Cashier`                   | P0          | ✅           |
| **UC18**  | Tất toán POS / Post-to-Room tại quầy                    | `Cashier`                   | P0          | ✅           |
| UC18.1          | Quản lý bàn & trạng thái bàn POS                      | `Cashier`                   | P0          | ✅           |
| UC18.2          | Quản lý đơn Room Service (staff view)                   | `Cashier`                   | P1          | ✅           |
| UC18.3          | Báo cáo ca làm việc (Shift Report)                      | `Cashier`                   | P2          | ⚠️         |
| **UC19**  | **Màn hình bếp KDS**                               |                               |             |              |
| UC19.1          | Hiển thị vé món chờ (polling/API, không WebSocket)    | `Kitchen Staff`             | P0          | ✅           |
| UC19.2          | Cập nhật trạng thái món PENDING → COOKING → READY    | `Kitchen Staff`             | P0          | ✅           |
| UC19.3          | Báo hoàn thành & thông báo phục vụ                   | `Kitchen Staff`             | P0          | ⚠️         |
| UC19.4          | Khóa món hết hàng (`MenuItem.isAvailable`)            | `Kitchen Staff`             | P0          | ✅           |
| UC19.5          | E-Menu bếp (xem & toggle món)                             | `Kitchen Staff`             | P1          | ✅           |

---

## 🟢 MOD4: TOUR, ADD-ONS & ĐÁNH GIÁ

| **UC ID** | **Tên Use Case**                                       | **Actor**               | **P** | **TT** |
| --------------- | ------------------------------------------------------------- | ----------------------------- | ----------- | ------------ |
| **UC20**  | Tìm tour + tích hợp thời tiết OpenWeather                | `Customer`,`Guest`        | P1          | ✅           |
| **UC21**  | Đặt vé tour (web + post-to-room / VNPay)                   | `Customer`,`Receptionist` | P0          | ✅           |
| **UC22**  | **Điều hành Tour**                                   |                               |             |              |
| UC22.1          | Đồng bộ khách combo phòng → tour booking                | System                        | P0          | ❌           |
| UC22.2          | Phân công HDV / nhân sự tour (`TourStaffAssignment`)    | `Admin`,`Manager`         | P1          | ⚠️         |
| UC22.3          | GPS real-time theo dõi xe tour                               | `Tour Guide`                | P2          | ❌           |
| UC22.4          | Cập nhật tiến độ hành trình (`Run_Itinerary_Status`) | `Tour Guide`                | P1          | ⚠️         |
| UC22.5          | Điểm danh AI FaceID tại checkpoint                         | `Tour Guide`                | P1          | ⚠️         |
| UC22.6          | Điểm danh thủ công fallback                               | `Tour Guide`                | P1          | ✅           |
| UC22.7          | Tour Guide Dashboard & trang tour theo loại                  | `Tour Guide`                | P1          | ✅           |
| **UC23**  | **Add-ons dịch vụ gia tăng**                         |                               |             |              |
| UC23.1          | CRUD catalog dịch vụ add-on (Spa, đưa đón…)            | `Admin`,`Manager`         | P1          | ❌           |
| UC23.2          | Đặt add-on & cấu hình JSON đặc biệt                    | `Customer`,`Receptionist` | P0          | ❌           |
| UC23.3          | Gói combo marketing (`MarketingService.createCombo`)       | `Admin`                     | P2          | ⚠️         |
| **UC24**  | Gửi đánh giá sao & feedback (phòng / tour)               | `Customer`                  | P2          | ⚠️         |
| **UC25**  | Kiểm duyệt review (admin)                                   | `Admin`                     | P2          | ✅           |

---

## 🟣 MOD5: FOLIO, TÀI CHÍNH & BÁO CÁO

| **UC ID** | **Tên Use Case**                                    | **Actor**               | **P** | **TT** |
| --------------- | ---------------------------------------------------------- | ----------------------------- | ----------- | ------------ |
| **UC26**  | **Folio Aggregation**                                |                               |             |              |
| UC26.1          | Tích lũy chi phí F&B/Tour về ví phòng                | System                        | P0          | ✅           |
| UC26.2          | Theo dõi dư nợ folio real-time                          | `Receptionist`,`Customer` | P0          | ✅           |
| UC26.3          | Lịch sử thanh toán đa đợt (`Payment_Transactions`) | System                        | P0          | ✅           |
| UC26.4          | Tách bill / split folio item                              | `Receptionist`              | P1          | ✅           |
| UC26.5          | Hóa đơn tổng hợp quyết toán (Consolidated Invoice)  | `Receptionist`              | P0          | ✅           |
| UC26.6          | Áp dụng hạng thành viên (Membership Tier) trên folio | System                        | P2          | ✅           |
| **UC27**  | **Night Audit & Thanh toán**                        |                               |             |              |
| UC27.1          | Night Audit — post room charge hàng đêm                | System                        | P0          | ⚠️         |
| UC27.2          | UI Night Audit lễ tân                                    | `Receptionist`              | P0          | ⚠️         |
| UC27.3          | Thanh toán VNPay (booking, food, folio, walk-in)          | `Customer`,`Receptionist` | P0          | ✅           |
| UC27.4          | Check-out & thu tiền cuối cùng tại sảnh               | `Receptionist`              | P0          | ✅           |
| UC27.5          | Gửi hóa đơn PDF qua email                              | System                        | P1          | ✅           |
| **UC28**  | **Dashboard Manager & Báo cáo**                    |                               |             |              |
| UC28.1          | Biểu đồ doanh thu lũy kế                              | `Manager`                   | P1          | ✅           |
| UC28.2          | Occupancy & khách in-house                                | `Manager`                   | P1          | ✅           |
| UC28.3          | Analytics tour / F&B / stay                                | `Manager`                   | P1          | ⚠️         |
| UC28.4          | Export báo cáo PDF/Excel                                 | `Manager`                   | P2          | ⚠️         |
| UC28.5          | Báo cáo USALI chuẩn quốc tế                           | `Manager`                   | P2          | ❌           |

---

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
| MOD4            | 14            | 4            | 6            | 4           |
| MOD5            | 16            | 12           | 3            | 1           |
| MOD6            | 9             | 9            | 0            | 0           |
| **TỔNG** | **108** | **73** | **27** | **8** |

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
