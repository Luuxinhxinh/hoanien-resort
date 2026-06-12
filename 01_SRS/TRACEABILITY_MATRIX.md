# BẢNG TRUY VẾT YÊU CẦU — KAWAI RESORT (TRACEABILITY MATRIX)
> Ánh xạ: `UC ID` → `Class/Method` → `TC ID` → `BR ID`
> - UC ID lấy từ `01_SRS/UC_MASTER_TABLE.md`
> - TC ID lấy từ `04_testing/TC_MASTER_TABLE.md`
> - BR ID lấy từ `01_SRS/BR_ACTOR_ROLE_TABLE.md`
>
> Mỗi thành viên điền cột `Class / Method` sau khi code xong.

---

## MOD1 — Xác thực, Hồ sơ & Core Data (Sinh viên 1)

| UC ID  | Mô tả                   | Class / Method        | TC ID               | BR                   |
| --------| -------------------------| -----------------------| ---------------------| ----------------------|
| UC01.1 | Đăng ký tài khoản       | *(điền sau khi code)* | TC-M1-001, 002, 003 | BR-SYS-01, BR-SYS-07 |
| UC01.2 | Đăng nhập               | *(điền sau khi code)* | TC-M1-004, 005, 006 | BR-SYS-02            |
| UC02   | Xác thực 2FA / OTP      | *(điền sau khi code)* | TC-M1-007, 008, 009 | BR-SYS-02            |
| UC03   | Đặt lại mật khẩu        | *(điền sau khi code)* | TC-M1-010, 011, 012 | BR-SYS-07            |
| UC04   | Hồ sơ cá nhân (AES-256) | *(điền sau khi code)* | TC-M1-013, 014      | BR-SYS-01            |
| UC05.1 | Quản lý nhân viên RBAC  | *(điền sau khi code)* | TC-M1-015, 016      | BR-SYS-06            |
| UC05.2 | Audit Log               | *(điền sau khi code)* | TC-M1-017           | BR-SYS-04            |
| UC06.1 | CRUD Core Data          | *(điền sau khi code)* | TC-M1-018           | —                    |
| UC06.2 | Giá phòng động          | *(điền sau khi code)* | TC-M1-019           | —                    |
| UC07   | Ẩn danh hóa PII         | *(điền sau khi code)* | TC-M1-020, 021      | BR-SYS-05            |
| UC08   | Session Timeout         | *(điền sau khi code)* | TC-M1-022           | BR-SYS-03            |

---

## MOD2 — Đặt phòng & Tiền sảnh (Sinh viên 2)

| UC ID | Mô tả | Class / Method | TC ID | BR |
|-------|-------|---------------|-------|-----|
| UC09 | Tìm phòng trống | *(điền sau khi code)* | TC-M2-001, 002 | — |
| UC10.1 | Đặt phòng & Cọc VNPay | *(điền sau khi code)* | TC-M2-003, 004, 005, 006, 007 | BR-FO-01, BR-FO-02, BR-FIN-02 |
| UC10.2 | Mã khuyến mãi | *(điền sau khi code)* | TC-M2-008, 009 | — |
| UC11 | Front Desk Dashboard | `RoomServiceImpl.getRoomDashboard()` | TC-M2-010 | BR-FO-04 |
| UC12.1 | Check-in (quét CCCD) | *(điền sau khi code)* | TC-M2-011, 012 | BR-SYS-01, BR-FO-03, BR-FO-08, BR-FO-09 |
| UC12.2 | Hạn mức chi tiêu | *(điền sau khi code)* | TC-M2-013 | BR-FO-06 |
| UC12.3 | Đổi phòng | *(điền sau khi code)* | TC-M2-014 | — |
| UC12.4 | Nâng cấp Dependents | *(điền sau khi code)* | TC-M2-015 | BR-FO-07 |
| UC13.1 | Tự động lệnh dọn phòng | *(điền sau khi code)* | TC-M2-016 | BR-FO-04 |
| UC13.2 | HK cập nhật dọn phòng | *(điền sau khi code)* | TC-M2-017 | BR-FO-04 |
| UC13.3 | Xem yêu cầu dọn/sửa | *(điền sau khi code)* | TC-M2-018 | BR-HK-04 |
| UC13.4 | HK báo hỏng thiết bị | *(điền sau khi code)* | TC-M2-019 | BR-HK-02 |
| UC13.5 | MT hoàn thành bảo trì | *(điền sau khi code)* | TC-M2-020 | BR-HK-03 |

---

## MOD3 — POS Nhà hàng & F&B (Sinh viên 3)

| UC ID | Mô tả | Class / Method | TC ID | BR |
|-------|-------|---------------|-------|-----|
| UC14 | Room Service (QR) | *(điền sau khi code)* | TC-M3-001, 002 | BR-FB-05 |
| UC15 | Đặt bàn nhà hàng | *(điền sau khi code)* | TC-M3-003, 004 | BR-FB-03 |
| UC16 | POS tạo order Dine-In | *(điền sau khi code)* | TC-M3-005, 006 | BR-FB-04 |
| UC17.1 | KDS — KOT bếp nhận đơn | *(điền sau khi code)* | TC-M3-007, 008 | BR-FB-04 |
| UC17.2 | Báo hết món | *(điền sau khi code)* | TC-M3-009 | BR-FB-02 |
| UC18 | Post to Room (Folio) | *(điền sau khi code)* | TC-M3-010, 011, 012 | BR-FO-06, BR-FB-01 |

---

## MOD4 — Tour & Đánh giá (Sinh viên 4)

| UC ID  | Mô tả                     | Class / Method                               | TC ID               | BR       |
| --------| ---------------------------| ----------------------------------------------| ---------------------| ----------|
| UC19   | Tìm kiếm tour + thời tiết | `TourServiceImpl.searchAvailableTours()`     | TC-M4-001, 002      | —        |
| UC20.1 | Đặt tour                  | `TourBookingServiceImpl.createTourBooking()` | TC-M4-003, 004, 005 | BR-TR-01 |
| UC20.2 | Lập lịch chuyến tour      | `TourBookingServiceImpl.scheduleTour()`      | TC-M4-006           | BR-TR-06 |
| UC20.3 | Hủy tour do sự cố         | `TourBookingServiceImpl.cancelTour()`        | TC-M4-007           | BR-TR-05 |
| UC21   | Điểm danh AI Face Scan    | `TourServiceImpl.verifyAttendance()`         | TC-M4-008, 009      | BR-TR-02 |
| UC22   | Đánh giá dịch vụ          | `ReviewServiceImpl.submitTourReview()`       | TC-M4-010, 011      | BR-TR-03 |
| UC23   | Kiểm duyệt đánh giá       | `ReviewServiceImpl.moderateReview()`         | TC-M4-012           | BR-TR-04 |

---

## MOD5 — Hóa đơn & Báo cáo (Sinh viên 5)

| UC ID  | Mô tả                  | Class / Method        | TC ID               | BR                   |
| --------| ------------------------| -----------------------| ---------------------| ----------------------|
| UC24.1 | Folio dư nợ phòng      | *(điền sau khi code)* | TC-M5-001           | BR-FB-01             |
| UC24.2 | Luồng tiền nhiều đợt   | *(điền sau khi code)* | TC-M5-002           | BR-FIN-06            |
| UC24.3 | Gom hóa đơn quyết toán | *(điền sau khi code)* | TC-M5-003           | BR-FIN-01            |
| UC24.4 | Night Audit 02:00 AM   | *(điền sau khi code)* | TC-M5-004, 005      | BR-FIN-03, BR-FIN-07 |
| UC25.1 | Thanh toán Check-out   | *(điền sau khi code)* | TC-M5-006, 007, 008 | BR-FIN-01, BR-FO-09  |
| UC25.2 | e-Invoice tự động      | *(điền sau khi code)* | TC-M5-009           | —                    |
| UC26.1 | Biểu đồ tài chính      | *(điền sau khi code)* | TC-M5-010           | —                    |
| UC26.2 | Occupancy Rate         | *(điền sau khi code)* | TC-M5-011           | —                    |
| UC27   | Báo cáo USALI          | *(điền sau khi code)* | TC-M5-012           | BR-FIN-04            |
| UC28   | Xuất PDF / Excel       | *(điền sau khi code)* | TC-M5-013, 014      | —                    |

---

## CROSS-MODULE: E2E

| TC ID | Luồng UC | Mô tả |
|-------|---------|-------|
| TC-E2E-001 | UC10→UC12→UC18→UC25 | Đặt phòng → Check-in → Ăn Post to Room → Check-out |
| TC-E2E-002 | UC10→UC20→UC24→UC25 | Đặt phòng + Tour → Night Audit → Tất toán |
| TC-E2E-003 | UC01→UC05.1→UC16 | Admin tạo F&B Staff → Đăng nhập → Tạo order POS |

**Hướng dẫn:** Cột `Class / Method` ghi tên class và method thực tế sau khi implement xong. Ví dụ: `BookingService.createBooking()`
