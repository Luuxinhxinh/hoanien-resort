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
| Field | Value |
|---|---|
| Feature / Gap ID | KAWAI-ALL-001 |
| Module | Hệ thống quản lý nghỉ dưỡng (Tích hợp 5 Module) |
| Spec gốc | Project_Specification.md |
| Priority | 🔴 P0 |
| Sprint | S1 (2026-06-09 → 2026-06-23) |
| Data Classification | Sensitive-PII / PII / Internal |
| Compliance Scope | Luật Cư trú 2020, Nghị định 13/2023/NĐ-CP (Bảo vệ dữ liệu cá nhân) |

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
| TC ID | Test File | 🔴 RED confirmed | 🟢 GREEN (commit) | 🔵 REFACTOR note |
|---|---|---|---|---|
| MOD1-TC-001 | `AuthIntegrationTest.java` | [ ] | [ ] | |
| MOD2-TC-001 | `BookingConcurrencyTest.java` | [ ] | [ ] | |
| MOD3-TC-001 | `FolioChargeTest.java` | [ ] | [ ] | |
| MOD5-TC-001 | `NightAuditSchedulerTest.java` | [ ] | [ ] | |

## 6. Entry / Exit Criteria

### Exit Criteria (Điều kiện kết thúc — DoD)
- [ ] Mọi test concurrency (Double-booking) phải PASS ổn định (flaky < 1%).
- [ ] Module Folio (Module 5) không bị sai lệch số thập phân (BigDecimal dùng chính xác).
- [ ] Chạy lệnh `mvn test` bao phủ 100% Core Business Logic các service.
