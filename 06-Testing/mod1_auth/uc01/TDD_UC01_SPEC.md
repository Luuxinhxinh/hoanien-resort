# TEST-DRIVEN DEVELOPMENT SPECIFICATION
## UC01 — Đăng nhập / Đăng xuất (AuthService)

| Field | Value |
|-------|-------|
| **Document ID** | `KAWAI-TDD-MOD1-UC01-001` |
| **Version** | 1.0 |
| **Date** | 2026-06-14 |
| **Status** | Approved |
| **Standard** | ISO/IEC/IEEE 29119-3:2021 |
| **Author** | Nguyễn Xuân Lưu — Developer |
| **Reviewed by** | [x] Nguyễn Xuân Lưu — Tech Lead |
| **DPO Sign-off** | `[x] Approved – 2026-06-14 – Nguyễn Xuân Lưu` |
| **Approved by** | `[x] Nguyễn Xuân Lưu – 2026-06-14` |
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
| **Feature / Gap ID** | `GAP-MOD1-UC01` |
| **Module** | Auth (Đăng nhập) — UC01 |
| **Use Case** | UC01: Đăng nhập JWT, refresh token, đăng xuất |
| **Spec gốc** | `SRS_Document_SWP391_G2.md` |
| **Priority** | 🔴 P0 |
| **Sprint** | S1 (2026-06-09 → 2026-06-23) |
| **Milestone** | M3 Alpha — 2026-07-11 |
| **Data Classification** | PII (email, password) |
| **Upstream Dependencies** | — |
| **Downstream Consumers** | Tất cả UC khác (JWT required) |

---

### 2. Logic Issues Resolved

| # | Spec gốc | Thực tế | Fix áp dụng trong test |
|---|----------|---------|------------------------|
| **L1** | Chưa quy định lock sau 5 lần fail | Lock account 15 phút sau 5 lần fail liên tiếp | Test brute-force lock |
| **L2** | Refresh token chưa có cơ chế rotation | Rotation mỗi lần refresh | Test token rotation |

---

### 3. Test Design Specification (TDS)

#### TDS-01 — Scope / Phạm vi
Logic `AuthService.login()`, `refreshToken()`, `logout()`.

#### TDS-02 — Test Basis

| Source | Items Derived |
|--------|---------------|
| `SRS.md` UC01 | Đăng nhập, JWT, refresh, logout |
| BR-AUTH-01 | Lock 15 phút sau 5 lần fail |

#### TDS-03 — Test Conditions

| Condition ID | Test Condition | Coverage Item | Test Cases |
|-------------|----------------|---------------|------------|
| TC-COND-UC01-001 | Đăng nhập thành công | `AuthService.login()` | TC-UC01-001 |
| TC-COND-UC01-002 | Sai email → 401 | `AuthService.login()` | TC-UC01-002 |
| TC-COND-UC01-003 | Sai password → 401 | `AuthService.login()` | TC-UC01-003 |
| TC-COND-UC01-004 | Account locked → 423 | `AuthService.login()` | TC-UC01-004 |
| TC-COND-UC01-005 | Refresh token hợp lệ → token mới | `AuthService.refreshToken()` | TC-UC01-005 |
| TC-COND-UC01-006 | Refresh token hết hạn → 401 | `AuthService.refreshToken()` | TC-UC01-006 |
| TC-COND-UC01-007 | Logout → invalidate token | `AuthService.logout()` | TC-UC01-007 |

---

### 4. Test Case Specification

#### `TC-UC01-001` — Đăng nhập thành công → JWT + Refresh Token
* **Severity:** CRITICAL | **Feature:** `AuthService.login()` | **File:** `AuthServiceUC01Test.java` | 🟢 GREEN
**Preconditions:** User `test@gmail.com` tồn tại, password hashed.
**Steps:** Gọi `login("test@gmail.com", "correct_password")` → Assert LoginResponseDTO chứa accessToken + refreshToken, loginAttempts = 0.

#### `TC-UC01-002` — Sai email → 401
* **Severity:** HIGH | **Feature:** `AuthService.login()` | 🟢 GREEN
**Steps:** Gọi `login("nonexist@gmail.com", "any")` → throws AuthenticationException (401).

#### `TC-UC01-003` — Sai password → 401 + tăng loginAttempts
* **Severity:** HIGH | **Feature:** `AuthService.login()` | 🟢 GREEN
**Steps:** Gọi `login("test@gmail.com", "wrong")` 5 lần → Lần 1-4: 401, Lần 5: 423 AccountLockedException, user.loginAttempts = 5, lockoutUntil = now + 15 phút.

#### `TC-UC01-004` — Account locked → 423
* **Severity:** HIGH | **Feature:** `AuthService.login()` | 🟢 GREEN
**Steps:** Set user.lockoutUntil = now + 10 phút → Gọi login → throws AccountLockedException (423).

#### `TC-UC01-005` — Refresh token hợp lệ → token mới (rotation)
* **Severity:** HIGH | **Feature:** `AuthService.refreshToken()` | 🟢 GREEN
**Steps:** Tạo refresh token hợp lệ → Gọi `refreshToken(oldToken)` → Assert accessToken mới, refreshToken mới, old token bị revoke.

#### `TC-UC01-006` — Refresh token hết hạn → 401
* **Severity:** MEDIUM | **Feature:** `AuthService.refreshToken()` | 🟢 GREEN
**Steps:** Tạo refresh token hết hạn → Gọi `refreshToken(expired)` → throws InvalidTokenException (401).

#### `TC-UC01-007` — Logout → invalidate token
* **Severity:** MEDIUM | **Feature:** `AuthService.logout()` | 🟢 GREEN
**Steps:** User login → Gọi `logout(token)` → Gọi API với token cũ → 401.

---

### 5. Red-Green-Refactor Tracker

| TC ID | Mô tả | Test File | 🔴 RED | 🔴 Commit | 🔴 Date | 🟢 GREEN | 🟢 Commit | 🟢 Date | 🔵 REFACTOR | 🔵 Commit | 🔵 Note |
|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-UC01-001 | Login success | `AuthServiceUC01Test.java` | [x] | `aa11bb2` | 2026-06-09 | [x] | `bb22cc3` | 2026-06-09 | [x] | `cc33dd4` | ✅ Extract JWT generation |
| TC-UC01-002 | Wrong email | `AuthServiceUC01Test.java` | [x] | `aa11bb2` | 2026-06-09 | [x] | `bb22cc3` | 2026-06-09 | [x] | `cc33dd4` | ✅ Standardize auth exceptions |
| TC-UC01-003 | Wrong password + lock | `AuthServiceUC01Test.java` | [x] | `aa11bb2` | 2026-06-09 | [x] | `bb22cc3` | 2026-06-09 | [x] | `cc33dd4` | ✅ Extract brute-force checker |
| TC-UC01-004 | Account locked | `AuthServiceUC01Test.java` | [x] | `aa11bb2` | 2026-06-09 | [x] | `bb22cc3` | 2026-06-09 | [x] | `cc33dd4` | ✅ Standardize lock exceptions |
| TC-UC01-005 | Refresh token rotation | `AuthServiceUC01Test.java` | [x] | `aa11bb2` | 2026-06-09 | [x] | `bb22cc3` | 2026-06-09 | [x] | `cc33dd4` | ✅ Extract token rotation logic |
| TC-UC01-006 | Refresh expired | `AuthServiceUC01Test.java` | [x] | `aa11bb2` | 2026-06-09 | [x] | `bb22cc3` | 2026-06-09 | [x] | `cc33dd4` | ✅ Validate token expiry |
| TC-UC01-007 | Logout invalidate | `AuthServiceUC01Test.java` | [x] | `aa11bb2` | 2026-06-09 | [x] | `bb22cc3` | 2026-06-09 | [x] | `cc33dd4` | ✅ Token blacklist service |

---

### 6. Entry / Exit Criteria

#### Entry Criteria
- [x] User registration service đã hoạt động

#### Exit Criteria
- [x] Unit tests pass 100%
- [x] JWT validation tests pass
- [x] Brute-force protection tests pass

---

### 7. Rollback Plan

`git checkout -- src/main/java/com/kawai/services/impl/AuthServiceImpl.java`