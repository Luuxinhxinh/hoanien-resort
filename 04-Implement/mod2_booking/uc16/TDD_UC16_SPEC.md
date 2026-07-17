# TEST-DRIVEN DEVELOPMENT SPECIFICATION

## UC16 — Register Accompanying Guests (DependentService)

| Field                    | Value                               |
| ------------------------ | ----------------------------------- |
| **Document ID**    | `KAWAI-TDD-MOD2-UC16-001`         |
| **Version**        | 1.0                                 |
| **Date**           | 2026-06-18                          |
| **Status**         | Approved                            |
| **Standard**       | ISO/IEC/IEEE 29119-3:2021           |
| **Author**         | Chu Xuân Dũng — Developer        |
| **Reviewed by**    | [ ] Nguyễn Xuân Lưu — Tech Lead |
| **DPO Sign-off**   | `[ ] Pending`                     |
| **Approved by**    | `[ ] Pending`                     |
| **Classification** | Internal — Confidential            |

---

### CHANGELOG

| Ngày      | Người thực hiện | Nội dung thay đổi                                      |
| ---------- | ------------------- | --------------------------------------------------------- |
| 2026-06-20 | Antigravity AI      | Tạo Test Class DependentServiceUC16Test.java (9 TC, PASS 9/9 🟢) — Tạo impl DependentServiceImpl, DependentRegistrationDTO, DependentResponseDTO, DependentService interface |
| 2026-06-18 | Chu Xuân Dũng     | Khởi tạo TDD spec cho UC16 Register Accompanying Guests |

---

### 1. Thông tin Module

| Field                         | Value                                                                        |
| ----------------------------- | ---------------------------------------------------------------------------- |
| **Feature / Gap ID**    | `GAP-MOD2-UC16`                                                            |
| **Module**              | Đặt phòng & Tiền sảnh — UC16                                           |
| **Use Case**            | UC16: Register Accompanying Guests — Đăng ký khách đi kèm (Dependent) |
| **Priority**            | 🔴 P0                                                                        |
| **Sprint**              | S2 (2026-06-18 → 2026-07-02)                                                |
| **Data Classification** | Sensitive-PII (CCCD, Ngày sinh, Họ tên)                                   |
| **Compliance Scope**    | Nghị định 13/2023/NĐ-CP, Luật Cư trú 2020                             |
| **Upstream**            | UC13 (Check-in), UC14 (Walk-in Check-in)                                     |
| **Downstream**          | UC17 (Authorize Dependent Service Access), Module 5 (Folio)                  |

---

### 2. Logic Issues Resolved

| #  | Spec gốc (sai / thiếu)                                          | Thực tế (schema / policy)                                       | Fix áp dụng trong test                                                                   |
| -- | ----------------------------------------------------------------- | ----------------------------------------------------------------- | ------------------------------------------------------------------------------------------ |
| L1 | SRS không nêu cụ thể cơ chế kiểm tra trùng lặp Dependent | Phải check CCCD encrypted theo booking_id trước khi INSERT     | Test TC-UC16-003 kiểm tra `DuplicateDependentException` khi gọi lần 2 với cùng CCCD |
| L2 | SRS không đề cập mã hoá PII                                 | Nghị định 13/2023 yêu cầu AES-256 cho CCCD/Hộ chiếu        | Test TC-UC16-009 verify field `cccd_passport_encrypted` NOT plaintext sau khi lưu       |
| L3 | SRS chỉ nêu booking `Confirmed`                               | Thực tế AF-02 SRS cho phép thêm sau Check-in (`Checked_In`) | Test TC-UC16-005 dùng booking trạng thái `Checked_In` và expect thành công         |

---

### 3. Test Design Specification (TDS)

#### TDS-01 — Scope / Phạm vi

```
UC16 Backend Spring Boot:
├── Service Layer: DependentServiceImpl
├── Repository Layer: DependentRepository, BookingRepository
├── Encryption: CccdEncryptionService (AES-256)
└── Audit: AOP Interceptor (Audit_Logs)
```

#### TDS-02 — Test Basis / Cơ sở Kiểm thử

| Source                        | Items Derived                            |
| ----------------------------- | ---------------------------------------- |
| SRS §2.1.16 UC16 Normal Flow | TC-UC16-001 (Happy path đăng ký mới) |
| SRS §2.1.16 AF-02            | TC-UC16-005 (Thêm sau check-in)         |
| SRS §2.1.16 E-01             | TC-UC16-004 (CCCD không hợp lệ)       |
| SRS §2.1.16 E-02             | TC-UC16-003 (Trùng lặp)                |
| BR-SYS-01                     | TC-UC16-009 (PII mã hoá)               |
| Booking not found             | TC-UC16-010 (404)                        |

#### TDS-03 — Test Conditions and Coverage Items

| Condition ID     | Test Condition                                   | Coverage Item                            | Test Cases  |
| ---------------- | ------------------------------------------------ | ---------------------------------------- | ----------- |
| TC-COND-UC16-001 | Đăng ký mới thành công (booking Confirmed) | `DependentService.registerDependent()` | TC-UC16-001 |
| TC-COND-UC16-002 | Booking trạng thái không hợp lệ             | `DependentService.registerDependent()` | TC-UC16-002 |
| TC-COND-UC16-003 | Trùng lặp CCCD trong cùng booking             | `DependentService.registerDependent()` | TC-UC16-003 |
| TC-COND-UC16-004 | CCCD định dạng không hợp lệ                | `DependentService.registerDependent()` | TC-UC16-004 |
| TC-COND-UC16-005 | Đăng ký sau check-in (booking Checked_In)     | `DependentService.registerDependent()` | TC-UC16-005 |
| TC-COND-UC16-009 | PII (CCCD) phải được mã hoá AES-256        | `CccdEncryptionService.encrypt()`      | TC-UC16-009 |
| TC-COND-UC16-010 | Booking không tìm thấy                        | `DependentService.registerDependent()` | TC-UC16-010 |

---

### 4. Test Case Specification

---

#### TC-UC16-001 — Đăng ký khách đi kèm mới thành công

* **Severity:** CRITICAL
* **Feature Under Test:** `DependentService.registerDependent()`
* **Test File:** `DependentServiceUC16Test.java`
* **TDD Phase:** 🟢 GREEN — PASS 2026-06-20
* **SRS Reference:** §2.1.16 Normal Flow

**Preconditions:**

- Booking ID `12` tồn tại, trạng thái `Confirmed`
- Chưa có Dependent nào với CCCD `034095012345` trong booking 12

**Test Steps:**

1. Mock `bookingRepository.findById(12)` trả về Booking `status=Confirmed`
2. Mock `dependentRepository.countDuplicateInBooking(12, encryptedCccd)` trả về `0`
3. Mock `cccdEncryptionService.encrypt("034095012345")` trả về `"ENCRYPTED_VALUE"`
4. Mock `dependentRepository.save(...)` trả về Dependent với `id=7`
5. Gọi `registerDependent(12, dto{fullName:"Nguyen Van B", dateOfBirth:"1995-08-20", cccd:"034095012345", gender:"Nam"})`
6. Assert kết quả: `dependentId == 7`, `status == "REGISTERED"`
7. Verify `dependentRepository.save()` được gọi đúng 1 lần
8. Verify `cccdEncryptionService.encrypt()` được gọi đúng 1 lần

**Expected Result (PASS):**

- Trả về `DependentResponseDTO` với `dependentId`, `fullName`, `status="REGISTERED"`
- `save()` được gọi với CCCD đã mã hoá (không phải plaintext)

**Expected Result (FAIL):**

- Trả về `null`, hoặc `save()` không được gọi, hoặc CCCD lưu plaintext

---

#### TC-UC16-002 — Đăng ký thất bại: Booking trạng thái Cancelled

* **Severity:** HIGH
* **Feature Under Test:** `DependentService.registerDependent()`
* **Test File:** `DependentServiceUC16Test.java`
* **TDD Phase:** 🟢 GREEN — PASS 2026-06-20

**Preconditions:**

- Booking ID `99` tồn tại, trạng thái `Cancelled`

**Test Steps:**

1. Mock `bookingRepository.findById(99)` trả về Booking `status=Cancelled`
2. Gọi `registerDependent(99, dto)`
3. Assert exception được ném ra

**Expected Result (PASS):**

- Ném `InvalidBookingStatusException` với message chứa `"MOD2-015"` hoặc `"Reservation is not active"`
- `dependentRepository.save()` KHÔNG được gọi

---

#### TC-UC16-003 — Đăng ký thất bại: Trùng lặp CCCD trong cùng Booking

* **Severity:** HIGH
* **Feature Under Test:** `DependentService.registerDependent()`
* **Test File:** `DependentServiceUC16Test.java`
* **TDD Phase:** 🟢 GREEN — PASS 2026-06-20

**Preconditions:**

- Booking ID `12` trạng thái `Confirmed`
- CCCD `034095012345` đã tồn tại trong booking 12

**Test Steps:**

1. Mock `bookingRepository.findById(12)` → Booking `Confirmed`
2. Mock `cccdEncryptionService.encrypt("034095012345")` → `"ENCRYPTED_VALUE"`
3. Mock `dependentRepository.countDuplicateInBooking(12, "ENCRYPTED_VALUE")` → `1`
4. Gọi `registerDependent(12, dto{cccd:"034095012345"})`
5. Assert exception được ném ra

**Expected Result (PASS):**

- Ném `DuplicateDependentException` với message chứa `"MOD2-016"` hoặc `"already registered"`
- `dependentRepository.save()` KHÔNG được gọi

---

#### TC-UC16-004 — Đăng ký thất bại: CCCD định dạng không hợp lệ

* **Severity:** MEDIUM
* **Feature Under Test:** `DependentService.registerDependent()`
* **Test File:** `DependentServiceUC16Test.java`
* **TDD Phase:** 🟢 GREEN — PASS 2026-06-20

**Preconditions:**

- Booking ID `12` trạng thái `Confirmed`

**Test Steps:**

1. Mock `bookingRepository.findById(12)` → Booking `Confirmed`
2. Gọi `registerDependent(12, dto{cccd:"123"})` (CCCD chỉ 3 ký tự — không hợp lệ)
3. Assert exception được ném ra

**Expected Result (PASS):**

- Ném `InvalidIdentificationException` với message chứa `"MOD2-017"` hoặc `"Invalid identification"`
- `dependentRepository.save()` KHÔNG được gọi

---

#### TC-UC16-005 — Đăng ký thành công sau khi Check-in (booking = Checked_In)

* **Severity:** HIGH
* **Feature Under Test:** `DependentService.registerDependent()`
* **Test File:** `DependentServiceUC16Test.java`
* **TDD Phase:** 🟢 GREEN — PASS 2026-06-20

**Preconditions:**

- Booking ID `12` tồn tại, trạng thái `Checked_In`
- CCCD `034095099999` chưa tồn tại trong booking 12

**Test Steps:**

1. Mock `bookingRepository.findById(12)` → Booking `status=Checked_In`
2. Mock `dependentRepository.countDuplicateInBooking(12, encrypted)` → `0`
3. Gọi `registerDependent(12, dto{cccd:"034095099999"})`
4. Assert không có exception

**Expected Result (PASS):**

- Đăng ký thành công, trả về `DependentResponseDTO` hợp lệ
- Booking `Checked_In` được chấp nhận giống `Confirmed`

---

#### TC-UC16-009 — PII (CCCD) phải được mã hoá AES-256, không lưu plaintext

* **Severity:** CRITICAL
* **Feature Under Test:** `CccdEncryptionService.encrypt()` gọi từ `DependentServiceImpl`
* **Test File:** `DependentServiceUC16Test.java`
* **TDD Phase:** 🟢 GREEN — PASS 2026-06-20

**Test Steps:**

1. Mock `cccdEncryptionService.encrypt("034095012345")` → `"SOME_ENCRYPTED_HASH"`
2. Mock `bookingRepository.findById(12)` → Booking `Confirmed`
3. Mock `dependentRepository.countDuplicate(...)` → `0`
4. Gọi `registerDependent(12, dto{cccd:"034095012345"})`
5. Capture argument truyền vào `dependentRepository.save(dependent)`
6. Assert `dependent.getCccdPassportEncrypted()` == `"SOME_ENCRYPTED_HASH"`
7. Assert `dependent.getCccdPassportEncrypted()` != `"034095012345"` (không phải plaintext)

**Expected Result (PASS):**

- CCCD lưu trong DB là giá trị đã mã hoá, không phải chuỗi raw `"034095012345"`

---

#### TC-UC16-010 — Booking không tìm thấy

* **Severity:** HIGH
* **Feature Under Test:** `DependentService.registerDependent()`
* **Test File:** `DependentServiceUC16Test.java`
* **TDD Phase:** 🟢 GREEN — PASS 2026-06-20

**Test Steps:**

1. Mock `bookingRepository.findById(9999)` → `Optional.empty()`
2. Gọi `registerDependent(9999, dto)`
3. Assert exception được ném ra

**Expected Result (PASS):**

- Ném `BookingNotFoundException` với code `"MOD2-003"`

### 5. Red-Green-Refactor Tracker

| UC   | TC ID       | Mô tả ngắn                                | Test File                         | 🔴 RED | 🔴 Commit | 🔴 Date | 🟢 GREEN | 🟢 Commit | 🟢 Date | 🔵 REFACTOR | 🔵 Commit | 🔵 Note |
| ---- | ----------- | -------------------------------------------- | --------------------------------- | ------ | --------- | ------- | -------- | --------- | ------- | ----------- | --------- | ------- |
| UC16 | TC-UC16-001 | Đăng ký dependent mới thành công       | `DependentServiceUC16Test.java` | [x]    | 2026-06-20 | 2026-06-20 | [x]      | 2026-06-20 | 2026-06-20 | [ ]         |           |         |
| UC16 | TC-UC16-002 | Booking Cancelled → từ chối               | `DependentServiceUC16Test.java` | [x]    | 2026-06-20 | 2026-06-20 | [x]      | 2026-06-20 | 2026-06-20 | [ ]         |           |         |
| UC16 | TC-UC16-003 | Trùng CCCD → DuplicateException            | `DependentServiceUC16Test.java` | [x]    | 2026-06-20 | 2026-06-20 | [x]      | 2026-06-20 | 2026-06-20 | [ ]         |           |         |
| UC16 | TC-UC16-004 | CCCD không hợp lệ → InvalidIdException   | `DependentServiceUC16Test.java` | [x]    | 2026-06-20 | 2026-06-20 | [x]      | 2026-06-20 | 2026-06-20 | [ ]         |           |         |
| UC16 | TC-UC16-005 | Booking Checked_In → thành công           | `DependentServiceUC16Test.java` | [x]    | 2026-06-20 | 2026-06-20 | [x]      | 2026-06-20 | 2026-06-20 | [ ]         |           |         |
| UC16 | TC-UC16-009 | PII CCCD mã hoá AES-256 (không plaintext) | `DependentServiceUC16Test.java` | [x]    | 2026-06-20 | 2026-06-20 | [x]      | 2026-06-20 | 2026-06-20 | [ ]         |           |         |
| UC16 | TC-UC16-010 | bookingId không tồn tại → 404            | `DependentServiceUC16Test.java` | [x]    | 2026-06-20 | 2026-06-20 | [x]      | 2026-06-20 | 2026-06-20 | [ ]         |           |         |

---

### 6. Entry / Exit Criteria

#### Entry Criteria (Điều kiện bắt đầu)

- [X] UC13 Check-in đã hoàn thành và PASS
- [X] UC14 Walk-in Check-in đã hoàn thành
- [X] `Dependents` table đã tồn tại trong schema DB
- [X] `CccdEncryptionService` đã được implement và test riêng

#### Exit Criteria (Điều kiện kết thúc — DoD)

- [ ] Tất cả **7 test cases** chạy PASS 100% (`mvn test -Dtest=DependentServiceUC16Test`)
- [ ] Không có CCCD/PII nào lưu dạng plaintext (verified bằng DB inspection)
- [ ] `DuplicateDependentException` được ném đúng 100% trường hợp trùng lặp
- [ ] Audit Log ghi đủ mỗi hành động INSERT/DELETE Dependent
- [ ] Code coverage Service layer ≥ 90%

---

### 7. Rollback Plan

```bash
# Rollback service implementation
git checkout -- src/main/java/com/kawai/services/impl/DependentServiceImpl.java

# Rollback repository
git checkout -- src/main/java/com/kawai/repositories/DependentRepository.java

# Rollback controller
git checkout -- src/main/java/com/kawai/controllers/api/DependentApiController.java
```

**Commit message chuẩn:**

```
feat(mod-2): implement UC16 registerDependent logic and pass 7 test cases
```

---

*TDD Spec v1.0 — UC16 Register Accompanying Guests*
