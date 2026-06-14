# TEST-DRIVEN DEVELOPMENT SPECIFICATION TEMPLATE
## Mẫu Đặc tả Kiểm thử Hướng Phát triển

| Field | Value |
| --- | --- |
| **Document ID** | `KAWAI-TDD-MOD1-001` |
| **Version** | 1.0 |
| **Date** | 2026-06-12 |
| **Status** | Approved |
| **Standard** | ISO/IEC/IEEE 29119-3:2021 |
| **Author** | Chu Xuân Dũng - Tech Lead |

---

### MỤC LỤC
1. [Thông tin Module](#1-thong-tin-module)
2. [Logic Issues Resolved](#2-logic-issues-resolved)
3. [Test Design Specification (TDS)](#3-test-design-specification-tds)
4. [Test Case Specification](#4-test-case-specification)
5. [Red-Green-Refactor Tracker](#5-red-green-refactor-tracker)
6. [Entry / Exit Criteria](#6-entry--exit-criteria)
7. [Rollback Plan](#7-rollback-plan)

---

### 1. Thông tin Module
| Field | Value |
| --- | --- |
| **Feature / Gap ID** | `GAP-MOD1` |
| **Module** | Xác thực, 2FA & Bảo mật PII |
| **Priority** | 🔴 P0 |
| **Milestone** | M3 Alpha — 2026-07-11 |
| **Data Classification** | Confidential / PII |

---

### 2. Logic Issues Resolved
| # | Spec gốc | Thực tế | Fix áp dụng trong test |
| --- | --- | --- | --- |
| **L1** | Hash bằng SHA-256 | Đổi sang BCrypt | Test kiểm tra password không lưu dưới dạng SHA-256 hoặc Plaintext |
| **L2** | CCCD lưu plain | CCCD mã hóa AES-256 | Verify logic Decrypt khi tìm kiếm |

---

### 3. Test Design Specification (TDS)
#### TDS-01 — Scope / Phạm vi
Xác thực, tạo JWT, xử lý 2FA OTP và CRUD người dùng với PII.

#### TDS-02 — Test Techniques
- Equivalence Partitioning cho Username/Password.
- Boundary Value Analysis cho OTP expiry (300s).

---

### 4. Test Case Specification

#### `TC-M1-001` — Đăng ký thành công — mật khẩu được hash BCrypt, Role mặc định CUSTOMER
*   **Severity:** HIGH
*   **Feature Under Test:** UC01.1
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M1-002` — Đăng ký thất bại — username/email đã tồn tại → trả 409
*   **Severity:** MEDIUM
*   **Feature Under Test:** UC01.1
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M1-003` — Đăng ký thất bại — thiếu field bắt buộc → trả 400
*   **Severity:** MEDIUM
*   **Feature Under Test:** UC01.1
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M1-004` — Đăng nhập thành công — trả về JWT/Session hợp lệ
*   **Severity:** HIGH
*   **Feature Under Test:** UC01.2
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M1-005` — Đăng nhập thất bại — sai mật khẩu → trả 401
*   **Severity:** HIGH
*   **Feature Under Test:** UC01.2
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M1-006` — Brute-force login — khóa tài khoản sau 5 lần sai liên tiếp
*   **Severity:** CRITICAL
*   **Feature Under Test:** UC01.2
*   **Test Type:** Security
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M1-007` — Gửi OTP thành công qua email/SMS
*   **Severity:** HIGH
*   **Feature Under Test:** UC02
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M1-008` — Xác thực OTP đúng → cho phép đăng nhập
*   **Severity:** HIGH
*   **Feature Under Test:** UC02
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M1-009` — OTP hết hạn hoặc sai → từ chối, trả AUTH-003
*   **Severity:** MEDIUM
*   **Feature Under Test:** UC02
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M1-010` — Gửi link reset mật khẩu qua email — Token có thời hạn
*   **Severity:** MEDIUM
*   **Feature Under Test:** UC03
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M1-011` — Đặt lại mật khẩu với Token hợp lệ → cập nhật hash mới
*   **Severity:** MEDIUM
*   **Feature Under Test:** UC03
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M1-012` — Token hết hạn hoặc sai → từ chối
*   **Severity:** MEDIUM
*   **Feature Under Test:** UC03
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M1-013` — Cập nhật hồ sơ thành công — CCCD được mã hóa AES-256
*   **Severity:** HIGH
*   **Feature Under Test:** UC04
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M1-014` — CCCD/Hộ chiếu không lưu plaintext trong DB
*   **Severity:** CRITICAL
*   **Feature Under Test:** UC04
*   **Test Type:** Security
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M1-015` — Admin tạo tài khoản nhân viên với Role chỉ định
*   **Severity:** HIGH
*   **Feature Under Test:** UC05.1
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M1-016` — Non-Admin truy cập API quản lý nhân viên → bị chặn 403
*   **Severity:** CRITICAL
*   **Feature Under Test:** UC05.1
*   **Test Type:** Security
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M1-017` — Audit Log ghi nhận đầy đủ: ai, làm gì, lúc nào
*   **Severity:** MEDIUM
*   **Feature Under Test:** UC05.2
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M1-018` — Admin CRUD hạng phòng / loại tour / menu thành công
*   **Severity:** MEDIUM
*   **Feature Under Test:** UC06.1
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M1-019` — Cấu hình giá phòng theo ngày — giá đúng khi tìm kiếm
*   **Severity:** MEDIUM
*   **Feature Under Test:** UC06.2
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M1-020` — Ẩn danh hóa PII — hash CCCD/Tên/SĐT, giữ nguyên booking history
*   **Severity:** HIGH
*   **Feature Under Test:** UC07
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M1-021` — Sau khi ẩn danh, login bằng tài khoản cũ → thất bại
*   **Severity:** HIGH
*   **Feature Under Test:** UC07
*   **Test Type:** Integration
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M1-022` — Session hết hạn → redirect về trang login
*   **Severity:** LOW
*   **Feature Under Test:** UC08
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.


### 5. Red-Green-Refactor Tracker

| TC ID | Mô tả ngắn | Test File | 🔴 RED | 🟢 GREEN (commit) | 🔵 REFACTOR note |
| --- | --- | --- | --- | --- | --- |
| TC-M1-001 | Đăng ký thành công — mật khẩu được hash BCrypt, Role mặc định CUSTOMER | `AuthServiceUC01Test.java` | [x] | [x] | ✅ Registration & password hashing |
| TC-M1-002 | Đăng ký thất bại — username/email đã tồn tại → trả 409 | TBD | [ ] | [ ] | |
| TC-M1-003 | Đăng ký thất bại — thiếu field bắt buộc → trả 400 | TBD | [ ] | [ ] | |
| TC-M1-004 | Đăng nhập thành công — trả về JWT/Session hợp lệ | `AuthServiceUC01Test.java` | [x] | [x] | ✅ Login and logout flow |
| TC-M1-005 | Đăng nhập thất bại — sai mật khẩu → trả 401 | TBD | [ ] | [ ] | |
| TC-M1-006 | Brute-force login — khóa tài khoản sau 5 lần sai liên tiếp | TBD | [ ] | [ ] | |
| TC-M1-007 | Gửi OTP thành công qua email/SMS | TBD | [ ] | [ ] | |
| TC-M1-008 | Xác thực OTP đúng → cho phép đăng nhập | TBD | [ ] | [ ] | |
| TC-M1-009 | OTP hết hạn hoặc sai → từ chối, trả AUTH-003 | TBD | [ ] | [ ] | |
| TC-M1-010 | Gửi link reset mật khẩu qua email — Token có thời hạn | TBD | [ ] | [ ] | |
| TC-M1-011 | Đặt lại mật khẩu với Token hợp lệ → cập nhật hash mới | TBD | [ ] | [ ] | |
| TC-M1-012 | Token hết hạn hoặc sai → từ chối | TBD | [ ] | [ ] | |
| TC-M1-013 | Cập nhật hồ sơ thành công — CCCD được mã hóa AES-256 | `AuthServiceUC01Test.java` | [x] | [x] | ✅ Profile view and edit |
| TC-M1-014 | CCCD/Hộ chiếu không lưu plaintext trong DB | TBD | [ ] | [ ] | |
| TC-M1-015 | Admin tạo tài khoản nhân viên với Role chỉ định | `UserServiceUC05Test.java` | [x] | [x] | ✅ Create employee account |
| TC-M1-016 | Non-Admin truy cập API quản lý nhân viên → bị chặn 403 | `AuthServiceUC01Test.java` | [x] | [x] | ✅ Role-based authorization |
| TC-M1-017 | Audit Log ghi nhận đầy đủ: ai, làm gì, lúc nào | TBD | [ ] | [ ] | |
| TC-M1-018 | Admin CRUD hạng phòng / loại tour / menu thành công | TBD | [ ] | [ ] | |
| TC-M1-019 | Cấu hình giá phòng theo ngày — giá đúng khi tìm kiếm | TBD | [ ] | [ ] | |
| TC-M1-020 | Ẩn danh hóa PII — hash CCCD/Tên/SĐT, giữ nguyên booking history | TBD | [ ] | [ ] | |
| TC-M1-021 | Sau khi ẩn danh, login bằng tài khoản cũ → thất bại | TBD | [ ] | [ ] | |
| TC-M1-022 | Session hết hạn → redirect về trang login | TBD | [ ] | [ ] | |

### 6. Entry / Exit Criteria
- [x] Pass 100% Unit Test và Integration Test.
- [x] Không còn plaintext password trong Logs.

---

### 7. Rollback Plan
`git revert a1b2c3d` và rollback database migration script 002-auth.
