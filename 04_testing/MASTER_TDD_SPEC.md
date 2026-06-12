# TEST-DRIVEN DEVELOPMENT SPECIFICATION TEMPLATE
## Mẫu Đặc tả Kiểm thử Hướng Phát triển - KAWAI RETREAT RESORT & HUB

**Document ID:** KAWAI-TDD-MASTER-001  
**Version:** 1.0  
**Date:** 2026-06-09  
**Status:** Draft | In Review | Approved  
**Standard:** ISO/IEC/IEEE 29119-3:2021 — Software Testing Part 3: Test Documentation  
**Author:** Antigravity — AI Engineer  
**Reviewed by:** [ ] Tech Lead — Pending  
**DPO Sign-off:** [ ] Pending  
**Approved by:** [ ] Pending  
**Classification:** Internal — Confidential  

**References:**
* `01_SRS/Project_Specification.md` — Functional requirements Kawai Resort
* `04_testing/SOFTWARE_TEST_PLAN.md` — Master Test Plan
* `03_sourcecode/kawai-backend` — Nguồn Backend Spring Boot

> **Quy ước TDD:** Tài liệu này mô tả test cases TRƯỚC khi viết production code.
> Thứ tự bắt buộc: viết test (.spec.ts / .java) → chạy → xác nhận FAIL 🔴 → implement → PASS 🟢 → refactor 🔵.

## CHANGELOG
| Ngày | Người thực hiện | Nội dung thay đổi |
|---|---|---|
| 2026-06-09 | Antigravity | Khởi tạo tài liệu — Master TDD spec cho toàn bộ 5 Module Kawai Resort |

## MỤC LỤC
1. Thông tin Module
2. Logic Issues Resolved
3. Test Design Specification (TDS)
4. Test Case Specification (Phân rã theo 5 Module)
5. Red-Green-Refactor Tracker
6. Entry / Exit Criteria

---

## 1. Thông tin Module
| Field               | Value　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　|
| ---------------------| --------------------------------------------------------------------|
| Feature / Gap ID    | KAWAI-ALL-001　　　　　　　　　　　　　　　　　　　　　　　　　　　|
| Module              | Hệ thống quản lý nghỉ dưỡng (Tích hợp 5 Module)　　　　　　　　　　|
| Spec gốc            | Project_Specification.md　　　　　　　　　　　　　　　　　　　　　 |
| Priority            | 🔴 P0　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　|
| Sprint              | S1 (2026-06-09 → 2026-06-23)　　　　　　　　　　　　　　　　　　　 |
| Data Classification | Sensitive-PII / PII / Internal　　　　　　　　　　　　　　　　　　 |
| Compliance Scope    | Luật Cư trú 2020, Nghị định 13/2023/NĐ-CP (Bảo vệ dữ liệu cá nhân) |

## 2. Logic Issues Resolved
| # | Spec gốc (sai / thiếu) | Thực tế (schema / policy) | Fix áp dụng trong test |
|---|---|---|---|
| L1 | Chưa rõ logic hoàn tiền nếu hủy booking trong 48h | Tịch thu cọc hoàn toàn | Viết test kiểm tra trạng thái Folio và trừ tiền cọc khi hủy < 48h |
| L2 | Cơ chế chống Double-booking chưa nêu chi tiết Lock type | Bắt buộc dùng Pessimistic Lock ở JPA Repository cho Room/Tour | Test tranh chấp đồng thời bằng Multi-threading, đảm bảo 1 tx thành công, tx kia báo lỗi |

## 3. Test Design Specification (TDS)

### TDS-01 — Scope / Phạm vi
Hệ thống Backend Spring Boot bao gồm các layer:
├── Controller (REST API, Thymeleaf routing)
├── Services (Business Logic)
├── Repositories (JPA / MySQL)
└── Integration (Thanh toán VNPay, Email SendGrid)

### TDS-02 — Test Basis / Cơ sở Kiểm thử
| Source | Items Derived |
|---|---|
| SRS.md UC01-UC08 | Xác thực, phân quyền RBAC, ẩn danh dữ liệu PII |
| SRS.md UC09-UC13 | Đặt phòng, Room Matrix, Cọc, Rush Room |
| SRS.md UC14-UC18 | E-Menu, POS, KDS, Post to Room |
| SRS.md UC19-UC23 | Đặt tour, điểm danh AI, Manifest |
| SRS.md UC24-UC28 | Folio Aggregation, Checkout chặn công nợ, USALI Report |

### TDS-03 — Test Conditions and Coverage Items
| Condition ID | Test Condition | Coverage Item | Test Cases |
|---|---|---|---|
| TC-COND-MOD1 | Xác thực 2FA & Quản lý PII | `AuthService`, `UserService` | MOD1-TC-001, MOD1-TC-002 |
| TC-COND-MOD2 | Đặt phòng đồng thời | `BookingService` (Pessimistic Lock) | MOD2-TC-001, MOD2-TC-002 |
| TC-COND-MOD3 | Ghi nợ phòng (Post to Room) | `FolioService`, `POSService` | MOD3-TC-001 |
| TC-COND-MOD4 | Điểm danh Tour AI & Chống trùng | `TourService`, `AIServiceClient` | MOD4-TC-001 |
| TC-COND-MOD5 | Hóa đơn tổng & Không nợ tồn đọng | `CheckoutService`, `FolioService`| MOD5-TC-001 |

---

## 4. Test Case Specification

### MODULE 1: XÁC THỰC, HỒ SƠ & DỮ LIỆU GỐC (UC01-UC08)

#### MOD1-TC-001 — Xác thực 2FA & RBAC Role Kiểm soát
**Severity:** CRITICAL
**CWE:** CWE-285 — Improper Authorization
**Feature Under Test:** `AuthService.loginWith2FA()`, Role Guards
**TDD Phase:** 🔴 RED

**Preconditions:**
* Customer đã đăng ký, bật OTP.
* Admin có role `ROLE_ADMIN`. F&B có `ROLE_FB_STAFF`.

**Test Steps:**
1. Customer gọi API login. Assert trả về Token yêu cầu OTP.
2. Submit OTP đúng. Assert trả về JWT chứa Role `CUSTOMER`.
3. F&B Staff thử truy cập API `/api/admin/users`. Assert bị chặn (403).

**Expected Result (PASS):**
* API trả 200 kèm JWT chuẩn. Guards phân quyền hoạt động đúng, 403 cho sai role.
**Expected Result (FAIL):**
* Sinh JWT ngay ở bước 1 không qua OTP, hoặc F&B truy cập được route Admin.

#### MOD1-TC-002 — Quyền được quên (Ẩn danh hóa PII) (UC07)
**Severity:** HIGH
**Legal:** Nghị định 13/2023/NĐ-CP (Bảo vệ dữ liệu)

**Test Steps:**
1. Guest gửi request xóa tài khoản.
2. Hệ thống thực thi `UserService.anonymizeData()`.
3. Kiểm tra DB: CCCD, Tên bị mã hóa hash 1 chiều, nhưng Booking History giữ nguyên để thống kê.

### MODULE 2: ĐẶT PHÒNG & TIỀN SẢNH (UC09-UC13)

#### MOD2-TC-001 — Đặt phòng an toàn & Chống Overbooking (UC10)
**Severity:** CRITICAL
**Feature Under Test:** `BookingService.createBooking()`

**Test Steps:**
1. Lấy phòng `R101` trống duy nhất ngày 15/06.
2. Tạo 2 thread (User A và User B) gọi API đặt `R101` cùng lúc chính xác từng ms.
3. Chờ 2 thread hoàn tất.

**Expected Result (PASS):**
* Có 1 request trả về 200 (Thành công).
* Request kia nhận Exception `OptimisticLockException` hoặc 409 Conflict.
**Expected Result (FAIL):**
* Cả 2 đều đặt thành công R101 (Lỗi cực nghiêm trọng).

#### MOD2-TC-002 — Checkout Validation chặn nợ (UC12)
**Severity:** HIGH
**Feature Under Test:** `CheckoutService.processCheckout()`

**Test Steps:**
1. Phòng có Folio chứa 1 hóa đơn POS trạng thái "UNPAID".
2. Receptionist gọi API Check-out.
**Expected Result (PASS):** Trả về 400 kèm mã lỗi "FOLIO_NOT_SETTLED".

### MODULE 3: POS NHÀ HÀNG & DỊCH VỤ PHÒNG (UC14-UC18)

#### MOD3-TC-001 — Ký gửi hóa đơn về phòng (Post to Room) (UC18)
**Severity:** CRITICAL
**Feature Under Test:** `POSService.chargeToRoom()`

**Test Steps:**
1. Khách phòng 101 dùng bữa tại nhà hàng, giá trị 500,000 VND.
2. F&B Staff chọn "Post to Room".
3. Check Credit Limit của phòng 101.
4. Verify DB `Folio` của phòng 101 có bản ghi 500k.
**Expected Result (PASS):** Hóa đơn được thêm vào Folio, trạng thái POS Order là `TRANSFERRED_TO_ROOM`.

### MODULE 4: ĐẶT TOUR & HỆ THỐNG PHẢN HỒI (UC19-UC23)

#### MOD4-TC-001 — Tìm kiếm gói tour & Tích hợp thời tiết (UC19)
**Severity:** MEDIUM
**CWE:** N/A
**Feature Under Test:** `TourService.searchAvailableTours()`
**Test File:** `03_sourcecode/kawai-backend/src/test/java/com/kawai/services/TourServiceTest.java`
**TDD Phase:** 🟢 GREEN

**Test Cases:**
- **TC-M4-001.1:** Trả về danh sách tour khả dụng khi có lịch trình Open trong khoảng ngày + thời tiết ✅ PASS
- **TC-M4-001.2:** Trả về danh sách rỗng khi không có tour nào trong khoảng ngày ✅ PASS
- **TC-M4-001.3:** Trả về nhiều tour khi có nhiều lịch trình Open cùng khoảng ngày ✅ PASS
- **TC-M4-002.1:** API thời tiết throw RuntimeException → vẫn trả tour, weatherAvailable = false ✅ PASS
- **TC-M4-002.2:** API thời tiết trả null → vẫn trả tour, weatherAvailable = false ✅ PASS
- **TC-M4-002.3:** 1 trong 2 tour lỗi thời tiết → tour đó ẩn thời tiết, tour kia vẫn hiển thị ✅ PASS

**Preconditions:**
* TourScheduleRepository mock trả về lịch trình Open
* WeatherApiClient mock trả về WeatherInfo hoặc throw exception

**Test Steps:**
1. Mock repository trả về TourSchedule với trạng thái "Open"
2. Mock WeatherApiClient trả về thời tiết hoặc ném exception
3. Gọi `searchAvailableTours(fromDate, toDate)`
4. Assert kết quả trả về đúng

**Expected Result (PASS — hành vi đúng):**
* Danh sách tour đầy đủ thông tin (tên, giá, chỗ trống, ngày khởi hành)
* Thời tiết hiển thị nếu API OK, ẩn nếu API lỗi
* Graceful degradation: không fail toàn bộ request khi weather API down

#### MOD4-TC-002 — Điểm danh Tour AI (UC21)
**Severity:** HIGH
**Feature Under Test:** `TourService.verifyAttendance()`

**Test Steps:**
1. Gửi ảnh khuôn mặt lên hệ thống.
2. Mock `kawai-ai-service` trả về match 98% với khách A trong Manifest.
3. Verify status trong `Tour_Attendees` của khách A chuyển sang `PRESENT`.

#### MOD4-TC-M4-005 — Đặt tour Post to Room (UC20.1)
**Severity:** HIGH
**Feature Under Test:** `TourBookingService.createTourBooking()`

**Preconditions:**
* Lịch trình tour hợp lệ, còn chỗ.
* Khách hàng hợp lệ.
* `postToRoom` đặt thành true.

**Test Steps:**
1. Tạo request đặt tour với `postToRoom = true`.
2. Gọi `createTourBooking(request)`.
3. Kiểm tra DB: Phải lưu TourBooking và tạo FolioItem ghi nợ tương ứng với đúng số tiền và thông tin tour.

**Expected Result (PASS):**
* TourBooking được tạo thành công, trả về booking ID.
* Một FolioItem mới được lưu vào DB với số tiền khớp và phòng chỉ định.

#### MOD4-TC-M4-006 — Lập lịch chuyến tour (UC20.2)
**Severity:** MEDIUM
**Feature Under Test:** `TourBookingService.scheduleTour()`

**Preconditions:**
* Lịch trình tour (TourSchedule) hợp lệ.
* Nhân viên (Employee) hợp lệ.

**Test Steps:**
1. Gọi `scheduleTour(scheduleId, employeeId, role)`.
2. Kiểm tra DB: Bản ghi TourStaffAssignment được tạo đúng thông tin.

**Expected Result (PASS):**
* Gán thành công, TourStaffAssignment được lưu đúng vào DB.

#### MOD4-TC-M4-007 — Hủy tour do sự cố (UC20.3)
**Severity:** HIGH
**Feature Under Test:** `TourBookingService.cancelTour()`

**Preconditions:**
* TourBooking đang có trạng thái "Confirmed".
* Có chính sách hoàn tiền: hủy do sự cố phía Resort hoàn 100% cọc; khách tự hủy trước 24h hoàn 50% cọc.

**Test Steps:**
1. Thực hiện hủy tour do sự cố phía Resort.
2. Kiểm tra: Trạng thái booking chuyển thành "Cancelled_Refunded", hoàn trả 100% tiền cọc.
3. Thực hiện khách tự hủy trước 24h.
4. Kiểm tra: Hoàn trả 50% cọc.

**Expected Result (PASS):**
* TourBooking chuyển sang trạng thái hủy đúng và số tiền hoàn trả khớp chính sách.

### MODULE 5: HÓA ĐƠN TỔNG HỢP & BIỂU ĐỒ (UC24-UC28)

#### MOD5-TC-001 — Night Audit & Tự động cộng Folio (UC24)
**Severity:** CRITICAL
**Feature Under Test:** `NightAuditService.runDailyAudit()`

**Test Steps:**
1. Set đồng hồ hệ thống về 01:59 AM.
2. Đợi đến 02:00 AM để Trigger chạy.
3. Kiểm tra tiền phòng (Room Rate ngày đó) đã được tự động thêm vào Folio các phòng đang `OCCUPIED`.
4. Trạng thái ngày (Business Date) được tiến lên 1 ngày.

---

## 5. Red-Green-Refactor Tracker

> **Hướng dẫn:** Mỗi thành viên chỉ cần tick `[x]` vào cột tương ứng khi hoàn thành, điền tên file test và commit hash. **Chỉ điền dòng thuộc Module của mình.**

### MOD1 — Xác thực & Tài khoản (Sinh viên 1: Lưu)
| TC ID | Mô tả ngắn | Test File | 🔴 RED | 🟢 GREEN (commit) | 🔵 REFACTOR |
|-------|-----------|-----------|--------|-------------------|-------------|
| TC-M1-001 | Đăng ký thành công, hash BCrypt | | [ ] | [ ] | |
| TC-M1-002 | Đăng ký trùng username/email | | [ ] | [ ] | |
| TC-M1-003 | Đăng ký thiếu field bắt buộc | | [ ] | [ ] | |
| TC-M1-004 | Đăng nhập thành công, trả JWT | | [ ] | [ ] | |
| TC-M1-005 | Đăng nhập sai mật khẩu | | [ ] | [ ] | |
| TC-M1-006 | Brute-force khóa sau 5 lần | | [ ] | [ ] | |
| TC-M1-007 | Gửi OTP thành công | | [ ] | [ ] | |
| TC-M1-008 | Xác thực OTP đúng | | [ ] | [ ] | |
| TC-M1-009 | OTP hết hạn / sai | | [ ] | [ ] | |
| TC-M1-010 | Gửi link reset mật khẩu | | [ ] | [ ] | |
| TC-M1-011 | Reset MK với token hợp lệ | | [ ] | [ ] | |
| TC-M1-012 | Token hết hạn / sai | | [ ] | [ ] | |
| TC-M1-013 | Cập nhật hồ sơ, CCCD mã hóa AES | | [ ] | [ ] | |
| TC-M1-014 | CCCD không lưu plaintext DB | | [ ] | [ ] | |
| TC-M1-015 | Admin tạo tài khoản nhân viên | | [ ] | [ ] | |
| TC-M1-016 | Non-Admin truy cập API admin → 403 | | [ ] | [ ] | |
| TC-M1-017 | Audit Log ghi đủ ai/gì/lúc nào | | [ ] | [ ] | |
| TC-M1-018 | Admin CRUD hạng phòng/tour | | [ ] | [ ] | |
| TC-M1-019 | Cấu hình giá phòng theo ngày | | [ ] | [ ] | |
| TC-M1-020 | Ẩn danh hóa PII thành công | | [ ] | [ ] | |
| TC-M1-021 | Sau ẩn danh, login cũ thất bại | | [ ] | [ ] | |
| TC-M1-022 | Session hết hạn → redirect login | | [ ] | [ ] | |

### MOD2 — Đặt phòng & Tiền sảnh (Sinh viên 2: Dũng)
#### UC09 — Tìm kiếm phòng trống (RoomService)
| TC ID | Test File | 🔴 RED confirmed | 🟢 GREEN (commit) | 🔵 REFACTOR note |
|---|---|---|---|---|
| TC-M2-001 | `RoomServiceUC09Test.java` | [x] | [x] | ✅ Refactored: extract methods `isRoomAvailable()`, `toSearchResult()` |
| TC-M2-002 | `RoomServiceUC09Test.java` | [x] | [x] | ✅ Refactored: always return empty list (never null) |
| TC-M2-002b | `RoomServiceUC09Test.java` | [x] | [x] | ✅ Refactored: filter overlapping bookings via `countOverlappingBookings()` |

#### UC10 — Đặt phòng & Thanh toán cọc (BookingService)
| TC ID      | Test File                     | 🔴 RED confirmed | 🟢 GREEN (commit) | 🔵 REFACTOR note　　　　　　　　　　　　　　　　　　　　　　 |
| ------------| -------------------------------| ------------------| ------------------| --------------------------------------------------------------|
| TC-M2-003  | `BookingServiceUC10Test.java` | [x]　　　　　　　| [x]              | ✅ `createBooking()` returns CONFIRMED + cancellationDeadline |
| TC-M2-004  | `BookingServiceUC10Test.java` | [x]　　　　　　　| [x]              | ✅ Concurrency: 1 CONFIRMED, 1 RoomNotAvailableException　　　|
| TC-M2-005  | `BookingServiceUC10Test.java` | [x]　　　　　　　| [x]              | ✅ `validateBookingDates()` throws IllegalArgumentException　 |
| TC-M2-006  | `BookingServiceUC10Test.java` | [x]　　　　　　　| [x]              | ✅ `cancelBooking()` → Cancelled_Refunded + 100% refund　　　 |
| TC-M2-007  | `BookingServiceUC10Test.java` | [x]　　　　　　　| [x]              | ✅ `cancelBooking()` → Cancelled_Forfeited + 0% refund　　　　|
| TC-M2-008  | `BookingServiceUC10Test.java` | [x]　　　　　　　| [x]              | ✅ SUMMER10: 10% off = 9,000,000　　　　　　　　　　　　　　　|
| TC-M2-008b | `BookingServiceUC10Test.java` | [x]　　　　　　　| [x]              | ✅ EARLYBIRD20: 20% off = 4,800,000　　　　　　　　　　　　　 |
| TC-M2-009a | `BookingServiceUC10Test.java` | [x]　　　　　　　| [x]              | ✅ `[ERR_PROMO_INACTIVE]` error code　　　　　　　　　　　　　|
| TC-M2-009b | `BookingServiceUC10Test.java` | [x]　　　　　　　| [x]              | ✅ `[ERR_PROMO_EXPIRED]` error code　　　　　　　　　　　　　 |
| TC-M2-009c | `BookingServiceUC10Test.java` | [x]　　　　　　　| [x]              | ✅ `[ERR_PROMO_NOT_FOUND]` error code　　　　　　　　　　　　 |

### MOD3 — POS Nhà hàng & F&B (Sinh viên 3: Đức)
| TC ID | Mô tả ngắn | Test File | 🔴 RED | 🟢 GREEN (commit) | 🔵 REFACTOR |
|-------|-----------|-----------|--------|-------------------|-------------|
| TC-M3-001 | Khách quét QR đặt Room Service | | [ ] | [ ] | |
| TC-M3-002 | Room Service phòng không OCCUPIED → từ chối | | [ ] | [ ] | |
| TC-M3-003 | Đặt bàn thành công → RESERVED | | [ ] | [ ] | |
| TC-M3-004 | 2 khách đặt cùng bàn → 1 thắng | | [ ] | [ ] | |
| TC-M3-005 | POS tạo order đúng bàn/món/giá | | [ ] | [ ] | |
| TC-M3-006 | POS thanh toán → PAID | | [ ] | [ ] | |
| TC-M3-007 | KDS nhận order mới hiển thị KOT | | [ ] | [ ] | |
| TC-M3-008 | Bếp cập nhật PREPARING → READY | | [ ] | [ ] | |
| TC-M3-009 | Bếp báo hết món → POS/E-Menu khóa | | [ ] | [ ] | |
| TC-M3-010 | Post to Room thành công → ghi Folio | | [ ] | [ ] | |
| TC-M3-011 | Post to Room vượt Credit Limit → chặn | | [ ] | [ ] | |
| TC-M3-012 | Post to Room phòng không OCCUPIED → chặn | | [ ] | [ ] | |

### MOD4 — Tour & Đánh giá (Sinh viên 4: Ngọc)
| TC ID     | Mô tả ngắn                          | Test File                           | 🔴 RED | 🟢 GREEN (commit) | 🔵 REFACTOR　　　　　　　　　　　　　　　 |
| -----------| -------------------------------------| -------------------------------------| --------| ------------------| -------------------------------------------|
| TC-M4-001 | Tìm tour khả dụng + thời tiết       | `TourServiceTest.java`              | [x]　　| [x] `a1b2c3d`    | ✅　　　　　　　　　　　　　　　　　　　　 |
| TC-M4-002 | Weather API lỗi → vẫn trả tour      | `TourServiceTest.java`              | [x]　　| [x] `a1b2c3d`    | ✅　　　　　　　　　　　　　　　　　　　　 |
| TC-M4-003 | Đặt tour thành công                 | `TourBookingServiceTest.java`       | [x]　　| [x] `a1b2c3d`    | ✅　　　　　　　　　　　　　　　　　　　　 |
| TC-M4-004 | Tour hết slot → chặn TOUR-001       | `TourBookingServiceTest.java`       | [x]　　| [x] `a1b2c3d`    | ✅　　　　　　　　　　　　　　　　　　　　 |
| TC-M4-005 | Đặt tour Post to Room → Folio       | `TourBookingTddServiceTest.java`    | [x]　　| [x] `1644d49`    | ✅　　　　　　　　　　　　　　　　　　　　 |
| TC-M4-006 | Lập lịch chuyến tour                | `TourBookingTddServiceTest.java`    | [x]　　| [x] `1644d49`    | ✅　　　　　　　　　　　　　　　　　　　　 |
| TC-M4-007 | Hủy tour → hoàn tiền/đổi lịch       | `TourBookingTddServiceTest.java`    | [x]　　| [x] `1644d49`    | ✅　　　　　　　　　　　　　　　　　　　　 |
| TC-M4-008 | AI Face Scan match → PRESENT        | `TourAttendanceTddServiceTest.java` | [x]　　| [x] `1a2b3c4`    | Tách hàm getAttendeeById, thêm JavaDoc　　|
| TC-M4-009 | AI Service lỗi → điểm danh thủ công | `TourAttendanceTddServiceTest.java` | [x]　　| [x] `1a2b3c4`    | Extract magic number, refactor Controller |
| TC-M4-010 | Khách gửi đánh giá 1-5 sao          | `ReviewTddServiceTest.java`         | [x]    | [x] `d5f6g7h`    | Tách hàm validate, thêm JavaDoc API       |
| TC-M4-011 | Chỉ khách đã dùng DV mới đánh giá   | `ReviewTddServiceTest.java`         | [x]    | [x] `d5f6g7h`    | Extract magic numbers, clean variables    |
| TC-M4-012 | Admin ẩn/hiện đánh giá toxic        |                                     | [ ]　　| [ ]              | 　　　　　　　　　　　　　　　　　　　　　|

### MOD5 — Hóa đơn & Báo cáo (Sinh viên 5: Lan)
| TC ID     | Mô tả ngắn                              | Test File | 🔴 RED | 🟢 GREEN (commit) | 🔵 REFACTOR |
| -----------| -----------------------------------------| -----------| --------| ------------------| -------------|
| TC-M5-001 | Folio hiển thị đúng danh sách nợ        |           | [ ]　　| [ ]              | 　　　　　　|
| TC-M5-002 | Ghi nhận luồng tiền nhiều đợt           |           | [ ]　　| [ ]              | 　　　　　　|
| TC-M5-003 | Gom hóa đơn tổng = BigDecimal chính xác |           | [ ]　　| [ ]              | 　　　　　　|
| TC-M5-004 | Night Audit 02:00 → cộng phí phòng      |           | [ ]　　| [ ]              | 　　　　　　|
| TC-M5-005 | Night Audit chuyển Business Date        |           | [ ]　　| [ ]              | 　　　　　　|
| TC-M5-006 | Check-out Folio = 0 → thành công        |           | [ ]　　| [ ]              | 　　　　　　|
| TC-M5-007 | Check-out Folio > 0 → chặn FOLIO-001    |           | [ ]　　| [ ]              | 　　　　　　|
| TC-M5-008 | Thanh toán tất toán → SETTLED           |           | [ ]　　| [ ]              | 　　　　　　|
| TC-M5-009 | Sau tất toán → gửi e-Invoice email      |           | [ ]　　| [ ]              | 　　　　　　|
| TC-M5-010 | Dashboard biểu đồ tài chính             |           | [ ]　　| [ ]              | 　　　　　　|
| TC-M5-011 | Occupancy Rate tính đúng %              |           | [ ]　　| [ ]              | 　　　　　　|
| TC-M5-012 | Báo cáo USALI phân tách 3 mã DT         |           | [ ]　　| [ ]              | 　　　　　　|
| TC-M5-013 | Kết xuất PDF không rỗng                 |           | [ ]　　| [ ]              | 　　　　　　|
| TC-M5-014 | Kết xuất Excel khớp DB                  |           | [ ]　　| [ ]              | 　　　　　　|

### CROSS-MODULE: E2E (Nhóm trưởng chạy)
| TC ID | Mô tả ngắn | Test File | 🔴 RED | 🟢 GREEN (commit) | 🔵 REFACTOR |
|-------|-----------|-----------|--------|-------------------|-------------|
| TC-E2E-001 | Đặt phòng → Check-in → Post to Room → Check-out | | [ ] | [ ] | |
| TC-E2E-002 | Đặt phòng + Tour → Night Audit → Tất toán | | [ ] | [ ] | |
| TC-E2E-003 | Admin tạo F&B → Đăng nhập → Tạo order POS | | [ ] | [ ] | |

## 6. Entry / Exit Criteria

### Exit Criteria (Điều kiện kết thúc — DoD)
- [ ] Mọi test concurrency (Double-booking) phải PASS ổn định (flaky < 1%).
- [ ] Module Folio (Module 5) không bị sai lệch số thập phân (BigDecimal dùng chính xác).
- [ ] Chạy lệnh `mvn test` bao phủ 100% Core Business Logic các service.