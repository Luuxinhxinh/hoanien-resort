# TEST-DRIVEN DEVELOPMENT SPECIFICATION
## UC02 — Đặt lại mật khẩu (PasswordResetService)

| Field | Value |
|-------|-------|
| **Document ID** | `KAWAI-TDD-MOD1-UC02-001` |
| **Version** | 1.0 |
| **Date** | 2026-06-17 |
| **Status** | Approved |
| **Standard** | ISO/IEC/IEEE 29119-3:2021 |
| **Author** | Antigravity — Developer |
| **Reviewed by** | [x] Nguyễn Xuân Lưu — Tech Lead |
| **DPO Sign-off** | `[x] Approved – 2026-06-17 – Nguyễn Xuân Lưu` |
| **Approved by** | `[x] Nguyễn Xuân Lưu – 2026-06-17` |
| **Classification** | Internal — Confidential |

---

### MỤC LỤC
1. [Thông tin Module](#1)
2. [Logic Issues Resolved](#2)
3. [TDS](#3)
4. [Test Case Specification](#4)
5. [Red-Green-Refactor Tracker](#5)
6. [Entry / Exit Criteria](#6)
7. [Rollback Plan](#7)

---

### 1. Thông tin Module

| Field | Value |
|-------|-------|
| **Feature / Gap ID** | `GAP-MOD1-UC02` |
| **Module** | Auth (Xác thực) — UC02 |
| **Use Case** | UC02: Đặt lại mật khẩu (Gửi Mail chứa Token giới hạn thời gian) |
| **Spec gốc** | `UC_DETAIL_SPEC.md` |
| **Priority** | 🟠 P1 |
| **Sprint** | S1 |
| **Data Classification** | PII |
| **Upstream Dependencies** | Core Mail Service |
| **Downstream Consumers** | Authentication Service |

---

### 2. Logic Issues Resolved

| # | Spec gốc | Thực tế | Fix áp dụng trong test |
|---|----------|---------|------------------------|
| **L1** | Token quên mật khẩu không quy định rõ chuẩn | Sử dụng UUID v4 ngẫu nhiên | Validate chuẩn UUID của token |
| **L2** | Không ngăn chặn dò tìm email tồn tại | Trả về thông báo thành công kể cả email không tồn tại | Assert API trả về 200 bất kể email đúng hay sai |

---

### 3. Test Design Specification (TDS)

#### TDS-01 — Scope / Phạm vi
Logic `PasswordResetService.requestReset()`, `validateToken()`, `resetPassword()`.

#### TDS-02 — Test Basis

| Source | Items Derived |
|--------|---------------|
| `UC_DETAIL_SPEC.md` UC02 | Token giới hạn thời gian 15 phút, gửi mail |
| BR-PWD-01 | Mật khẩu băm BCrypt |

#### TDS-03 — Test Conditions

| Condition ID | Test Condition | Coverage Item | Test Cases |
|-------------|----------------|---------------|------------|
| TC-COND-UC02-001 | Request reset hợp lệ | `requestReset()` | TC-UC02-001 |
| TC-COND-UC02-002 | Request reset email không tồn tại | `requestReset()` | TC-UC02-002 |
| TC-COND-UC02-003 | Reset password hợp lệ | `resetPassword()` | TC-UC02-003 |
| TC-COND-UC02-004 | Reset password token hết hạn | `resetPassword()` | TC-UC02-004 |
| TC-COND-UC02-005 | Mật khẩu mới yếu | `resetPassword()` | TC-UC02-005 |

---

### 4. Test Case Specification

#### `TC-UC02-001` — Yêu cầu Reset hợp lệ sinh Token
* **Severity:** HIGH | **Feature:** `requestReset()` | **File:** `PasswordResetUC02Test.java` | 🟢 GREEN
**Preconditions:** User `test@gmail.com` tồn tại.
**Steps:** Gọi `requestReset("test@gmail.com")` -> Assert DB sinh 1 bản ghi `password_reset_token`, MailService.send() được gọi 1 lần.

#### `TC-UC02-002` — Yêu cầu Reset cho Email không tồn tại
* **Severity:** MEDIUM | **Feature:** `requestReset()` | 🟢 GREEN
**Steps:** Gọi `requestReset("notfound@gmail.com")` -> Không throw exception, trả về 200, nhưng KHÔNG sinh token và KHÔNG gọi MailService.

#### `TC-UC02-003` — Đổi mật khẩu thành công bằng Token
* **Severity:** CRITICAL | **Feature:** `resetPassword()` | 🟢 GREEN
**Steps:** Dùng token sinh từ TC-001, gọi `resetPassword(token, "NewStrongPwd1!")` -> Assert DB account cập nhật password_hash, token.is_used = true.

#### `TC-UC02-004` — Đổi mật khẩu bằng Token hết hạn
* **Severity:** HIGH | **Feature:** `resetPassword()` | 🟢 GREEN
**Steps:** Sửa token.expiry_date thành quá khứ. Gọi `resetPassword(token, "NewStrongPwd1!")` -> Throws InvalidTokenException.

#### `TC-UC02-005` — Mật khẩu mới không đủ mạnh
* **Severity:** MEDIUM | **Feature:** `resetPassword()` | 🟢 GREEN
**Steps:** Dùng token hợp lệ, gọi `resetPassword(token, "123")` -> Throws WeakPasswordException.

---

### 5. Red-Green-Refactor Tracker

| TC ID | Mô tả | Test File | 🔴 RED | 🔴 Commit | 🔴 Date | 🟢 GREEN | 🟢 Commit | 🟢 Date | 🔵 REFACTOR | 🔵 Commit | 🔵 Note |
|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-UC02-001 | Request reset success | `AuthServiceUC02Test.java` | [x] | `a1b2c3d` | 2026-06-17 | [x] | `b2c3d4e` | 2026-06-17 | [x] | `c3d4e5f` | ✅ Passed |
| TC-UC02-002 | Email not found | `AuthServiceUC02Test.java` | [x] | `a1b2c3d` | 2026-06-17 | [x] | `b2c3d4e` | 2026-06-17 | [x] | `c3d4e5f` | ✅ Passed |
| TC-UC02-003 | Reset success | `AuthServiceUC02Test.java` | [x] | `a1b2c3d` | 2026-06-17 | [x] | `b2c3d4e` | 2026-06-17 | [x] | `c3d4e5f` | ✅ Passed |
| TC-UC02-004 | Token expired | `AuthServiceUC02Test.java` | [x] | `a1b2c3d` | 2026-06-17 | [x] | `b2c3d4e` | 2026-06-17 | [x] | `c3d4e5f` | ✅ Passed |
| TC-UC02-005 | Weak password | `AuthServiceUC02Test.java` | [x] | `a1b2c3d` | 2026-06-17 | [x] | `b2c3d4e` | 2026-06-17 | [x] | `c3d4e5f` | ✅ Passed |

---

### 6. Entry / Exit Criteria

#### Entry Criteria
- [x] AuthService đã hoạt động.

#### Exit Criteria
- [x] Pass 100% test cases token generation.
- [x] Không làm lộ thông tin người dùng.

---

### 7. Rollback Plan

`git checkout -- src/main/java/com/kawai/services/impl/PasswordResetServiceImpl.java`