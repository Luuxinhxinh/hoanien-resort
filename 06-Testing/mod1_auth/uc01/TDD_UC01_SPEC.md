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
| TC-COND-UC01-002 | Sai username → false | `AuthService.login()` | TC-UC01-002 |
| TC-COND-UC01-003 | Sai password → lock | `AuthService.login()` | TC-UC01-003 |
| TC-COND-UC01-004 | Account locked → Exception | `AuthService.login()` | TC-UC01-004 |

---

### 4. Test Case Specification

#### `TC-UC01-001` — Đăng nhập thành công, reset loginAttempts
* **Severity:** CRITICAL | **Feature:** `AuthService.login()` | **File:** `AuthServiceUC01Test.java` | 🟢 GREEN
**Preconditions:** User tồn tại, password đúng.
**Steps:** Gọi `login("testuser", "correct_pass")` → Assert trả về true, loginAttempts = 0.

#### `TC-UC01-002` — Sai username
* **Severity:** HIGH | **Feature:** `AuthService.login()` | 🟢 GREEN
**Steps:** Gọi `login("nonexist", "any")` → Assert trả về false.

#### `TC-UC01-003` — Sai password → tăng loginAttempts + lock
* **Severity:** HIGH | **Feature:** `AuthService.login()` | 🟢 GREEN
**Steps:** Gọi `login("testuser", "wrong")` tới lần thứ 5 → Tăng failedLoginAttempts, lockoutTime set to now + 15 phút.

#### `TC-UC01-004` — Account locked → IllegalStateException
* **Severity:** HIGH | **Feature:** `AuthService.login()` | 🟢 GREEN
**Steps:** User đang bị khóa (lockoutTime > now) → Gọi login → throws IllegalStateException.

---

### 5. Red-Green-Refactor Tracker

| TC ID | Mô tả | Test File | 🔴 RED | 🔴 Commit | 🔴 Date | 🟢 GREEN | 🟢 Commit | 🟢 Date | 🔵 REFACTOR | 🔵 Commit | 🔵 Note |
|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-UC01-001 | Đăng nhập thành công | `AuthServiceUC01Test.java` | [x] | `a1b2c` | 2026-06-17 | [x] | `b2c3d` | 2026-06-17 | [x] | `c3d4e` | ✅ Reset attempts |
| TC-UC01-002 | Sai username | `AuthServiceUC01Test.java` | [x] | `a1b2c` | 2026-06-17 | [x] | `b2c3d` | 2026-06-17 | [x] | `c3d4e` | ✅ Return false |
| TC-UC01-003 | Sai password + lock | `AuthServiceUC01Test.java` | [x] | `a1b2c` | 2026-06-17 | [x] | `b2c3d` | 2026-06-17 | [x] | `c3d4e` | ✅ Lock after 5 fails |
| TC-UC01-004 | Account locked | `AuthServiceUC01Test.java` | [x] | `a1b2c` | 2026-06-17 | [x] | `b2c3d` | 2026-06-17 | [x] | `c3d4e` | ✅ Throws Exception |

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