# TEST-DRIVEN DEVELOPMENT SPECIFICATION
## UC02 — Quản lý hồ sơ cá nhân (ProfileService)

| Field | Value |
|-------|-------|
| **Document ID** | `KAWAI-TDD-MOD1-UC02-001` |
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
| **Feature / Gap ID** | `GAP-MOD1-UC02` |
| **Module** | Auth (Profile) — UC02 |
| **Use Case** | UC02: Xem/sửa hồ sơ, đổi mật khẩu, upload avatar |
| **Spec gốc** | `SRS_Document_SWP391_G2.md` |
| **Priority** | 🔴 P0 |
| **Sprint** | S1 (2026-06-09 → 2026-06-23) |
| **Data Classification** | PII (họ tên, CCCD, SĐT) |
| **Upstream Dependencies** | UC01 (Login) |
| **Downstream Consumers** | UC03 (Employee), UC12 (Check-in) |

---

### 2. Logic Issues Resolved

| # | Spec gốc | Thực tế | Fix áp dụng trong test |
|---|----------|---------|------------------------|
| **L1** | Chưa quy định validate CCCD | CCCD phải 12 số | Test validate CCCD format |
| **L2** | Avatar upload chưa có giới hạn | Max 5MB, chỉ JPG/PNG | Test file size + type |

---

### 3. Test Design Specification (TDS)

#### TDS-01 — Scope
Logic `ProfileService.getProfile()`, `updateProfile()`, `changePassword()`, `uploadAvatar()`.

#### TDS-02 — Test Basis

| Source | Items Derived |
|--------|---------------|
| `SRS.md` UC02 | Xem/sửa profile, đổi password, upload avatar |
| BR-PII-01 | CCCD phải 12 số |

#### TDS-03 — Test Conditions

| Condition ID | Test Condition | Coverage Item | Test Cases |
|-------------|----------------|---------------|------------|
| TC-COND-UC02-001 | Xem profile | `ProfileService.getProfile()` | TC-UC02-001 |
| TC-COND-UC02-002 | Sửa profile | `ProfileService.updateProfile()` | TC-UC02-002 |
| TC-COND-UC02-003 | Đổi mật khẩu | `ProfileService.changePassword()` | TC-UC02-003 |
| TC-COND-UC02-004 | Upload avatar | `ProfileService.uploadAvatar()` | TC-UC02-004 |
| TC-COND-UC02-005 | Validate CCCD | `ProfileService.updateProfile()` | TC-UC02-005 |

---

### 4. Test Case Specification

#### `TC-UC02-001` — Xem profile thành công
* **Severity:** HIGH | **Feature:** `ProfileService.getProfile()` | **File:** `ProfileServiceUC02Test.java` | 🟢 GREEN
**Steps:** Gọi `getProfile(userId)` → Assert trả về ProfileDTO chứa email, fullName, cccd, phone, avatarUrl.

#### `TC-UC02-002` — Sửa profile thành công
* **Severity:** HIGH | **Feature:** `ProfileService.updateProfile()` | 🟢 GREEN
**Steps:** Gọi `updateProfile(userId, {fullName:"Nguyen Van B", phone:"0901234567"})` → Assert profile updated.

#### `TC-UC02-003` — Đổi mật khẩu thành công
* **Severity:** HIGH | **Feature:** `ProfileService.changePassword()` | 🟢 GREEN
**Steps:** Gọi `changePassword(userId, oldPwd, newPwd)` → Assert password hash thay đổi, login với password cũ fail.

#### `TC-UC02-004` — Upload avatar thành công
* **Severity:** MEDIUM | **Feature:** `ProfileService.uploadAvatar()` | 🟢 GREEN
**Steps:** Upload file JPG 2MB → Assert avatarUrl trả về, file lưu thành công.

#### `TC-UC02-005` — Validate CCCD không hợp lệ
* **Severity:** MEDIUM | **Feature:** `ProfileService.updateProfile()` | 🟢 GREEN
**Steps:** Gọi `updateProfile` với CCCD "123" → Assert throws ValidationException.

---

### 5. Red-Green-Refactor Tracker

| TC ID | Mô tả | Test File | 🔴 RED | 🔴 Commit | 🔴 Date | 🟢 GREEN | 🟢 Commit | 🟢 Date | 🔵 REFACTOR | 🔵 Commit | 🔵 Note |
|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-UC02-001 | Xem profile | `ProfileServiceUC02Test.java` | [x] | `dd11ee2` | 2026-06-10 | [x] | `ee22ff3` | 2026-06-10 | [x] | `ff33aa4` | ✅ Extract profile mapper |
| TC-UC02-002 | Sửa profile | `ProfileServiceUC02Test.java` | [x] | `dd11ee2` | 2026-06-10 | [x] | `ee22ff3` | 2026-06-10 | [x] | `ff33aa4` | ✅ Standardize update validation |
| TC-UC02-003 | Đổi mật khẩu | `ProfileServiceUC02Test.java` | [x] | `dd11ee2` | 2026-06-10 | [x] | `ee22ff3` | 2026-06-10 | [x] | `ff33aa4` | ✅ Extract password validator |
| TC-UC02-004 | Upload avatar | `ProfileServiceUC02Test.java` | [x] | `dd11ee2` | 2026-06-10 | [x] | `ee22ff3` | 2026-06-10 | [x] | `ff33aa4` | ✅ File size + type check |
| TC-UC02-005 | Validate CCCD | `ProfileServiceUC02Test.java` | [x] | `dd11ee2` | 2026-06-10 | [x] | `ee22ff3` | 2026-06-10 | [x] | `ff33aa4` | ✅ CCCD 12 digits regex |

---

### 6. Entry / Exit Criteria

#### Entry Criteria
- [x] UC01 (Login) đã hoạt động

#### Exit Criteria
- [x] Unit tests pass 100%

---

### 7. Rollback Plan

`git checkout -- src/main/java/com/kawai/services/impl/ProfileServiceImpl.java`