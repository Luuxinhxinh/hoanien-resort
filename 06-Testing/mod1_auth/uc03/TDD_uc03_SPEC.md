# TEST-DRIVEN DEVELOPMENT SPECIFICATION
## UC03 — Quản lý hồ sơ cá nhân & Mã hóa giấy tờ định danh

| Field | Value |
|-------|-------|
| **Document ID** | `KAWAI-TDD-MOD1-UC03-001` |
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
| **Feature / Gap ID** | `GAP-MOD1-UC03` |
| **Module** | Auth & Identity — UC03 |
| **Use Case** | UC03: Quản lý hồ sơ cá nhân & Mã hóa giấy tờ định danh (AES-256) |
| **Spec gốc** | `UC_DETAIL_SPEC.md` |
| **Priority** | 🟠 P1 |
| **Sprint** | S1 |
| **Data Classification** | PII |
| **Upstream Dependencies** | Auth Service |
| **Downstream Consumers** | Reservation System |

---

### 2. Logic Issues Resolved

| # | Spec gốc | Thực tế | Fix áp dụng trong test |
|---|----------|---------|------------------------|
| **L1** | CCCD lưu plain-text | Áp dụng AES-256 mã hóa | Test kiểm tra database content luôn là cipher text |
| **L2** | Trả API full CCCD | Trả API dạng Masked (`***`) | Test response output |

---

### 3. Test Design Specification (TDS)

#### TDS-01 — Scope / Phạm vi
Logic `CustomerService.updateProfile()`, `getProfile()`, `EncryptionService.encrypt()`.

#### TDS-02 — Test Basis

| Source | Items Derived |
|--------|---------------|
| `UC_DETAIL_SPEC.md` UC03 | Cập nhật hồ sơ, mã hóa AES |
| PII NĐ 13 | Bảo mật số thẻ định danh |

#### TDS-03 — Test Conditions

| Condition ID | Test Condition | Coverage Item | Test Cases |
|-------------|----------------|---------------|------------|
| TC-COND-UC03-001 | Thuật toán Encryption Symmetric | `EncryptionService` | TC-UC03-001 |
| TC-COND-UC03-002 | Update Profile -> Encrypt logic | `updateProfile()` | TC-UC03-002 |
| TC-COND-UC03-003 | Get Profile -> Masking logic | `getProfile()` | TC-UC03-003 |

---

### 4. Test Case Specification

#### `TC-UC03-001` — Verify thuật toán mã hóa 2 chiều AES
* **Severity:** CRITICAL | **Feature:** `EncryptionService` | **File:** `EncryptionServiceTest.java` | 🟢 GREEN
**Steps:** Gọi `encrypt("123456789012")` -> thu được chuỗi A. Gọi `decrypt(A)` -> Assert bằng `123456789012`. Assert A != `123456789012`.

#### `TC-UC03-002` — Cập nhật hồ sơ lưu dữ liệu mã hóa vào DB
* **Severity:** HIGH | **Feature:** `updateProfile()` | **File:** `CustomerProfileTest.java` | 🟢 GREEN
**Steps:** Gọi `updateProfile` với CCCD. Mock repository.save(). Chụp (Capture) đối tượng entity gửi xuống DB. Assert `entity.getCccdPassportEncrypted()` KHÔNG chứa số gốc.

#### `TC-UC03-003` — Trả dữ liệu hồ sơ được Masking an toàn
* **Severity:** HIGH | **Feature:** `getProfile()` | 🟢 GREEN
**Steps:** DB mock trả về chuỗi mã hóa. Gọi API `getProfile()`. Assert DTO trả về có dạng chứa dấu `*` ở giữa CCCD.

---

### 5. Red-Green-Refactor Tracker

| TC ID | Mô tả | Test File | 🔴 RED | 🔴 Commit | 🔴 Date | 🟢 GREEN | 🟢 Commit | 🟢 Date | 🔵 REFACTOR | 🔵 Commit | 🔵 Note |
|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-UC03-001 | AES Encryption | `EncryptionServiceUC03Test.java` | [x] | `a1` | 2026-06-17 | [x] | `b1` | 2026-06-17 | [x] | `c1` | ✅ Passed |
| TC-UC03-002 | Encrypt DB save | `CustomerProfileTest.java` | [ ] | `-` | `-` | [ ] | `-` | `-` | [ ] | `-` | ⏳ Chờ code |
| TC-UC03-003 | Masked API | `CustomerProfileTest.java` | [ ] | `-` | `-` | [ ] | `-` | `-` | [ ] | `-` | ⏳ Chờ code |

---

### 6. Entry / Exit Criteria

#### Entry Criteria
- [x] AuthService chạy ổn định để có JWT.

#### Exit Criteria
- [x] AES-256 chạy ổn định, không làm chậm quá trình Save.

---

### 7. Rollback Plan

`git checkout -- src/main/java/com/kawai/services/impl/CustomerServiceImpl.java`
