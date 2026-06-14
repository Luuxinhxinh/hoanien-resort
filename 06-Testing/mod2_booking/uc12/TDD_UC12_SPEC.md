# TEST-DRIVEN DEVELOPMENT SPECIFICATION
## UC12 — Check-in / Check-out / Đổi phòng (CheckinService)

| Field | Value |
|-------|-------|
| **Document ID** | `KAWAI-TDD-MOD2-UC12-001` |
| **Version** | 1.0 |
| **Date** | 2026-06-14 |
| **Status** | Approved |
| **Standard** | ISO/IEC/IEEE 29119-3:2021 |
| **Author** | Chu Xuân Dũng — Developer |
| **Reviewed by** | [x] Nguyễn Xuân Lưu — Tech Lead |
| **DPO Sign-off** | `[x] Approved – 2026-06-14 – Chu Xuân Dũng` |
| **Approved by** | `[x] Chu Xuân Dũng – 2026-06-14` |
| **Classification** | Internal — Confidential |

---

### 1. Thông tin Module
| Field | Value |
|-------|-------|
| **Feature / Gap ID** | `GAP-MOD2-UC12` |
| **Module** | Đặt phòng (Booking) — UC12 |
| **Use Case** | UC12: Check-in, check-out, đổi phòng, credit limit |
| **Priority** | 🔴 P0 |
| **Sprint** | S1 (2026-06-09 → 2026-06-23) |
| **Data Classification** | PII |
| **Upstream** | UC10 (Đặt phòng) |
| **Downstream** | UC13 (HK), Module 5 (Folio) |

### 2. Logic Issues Resolved
| L1 | Không có cơ chế tự động tạo Folio | Tạo Folio ngay khi check-in | Test kiểm tra Folio được tạo |

### 3. TDS
| Condition ID | Test Condition | Coverage Item | Test Cases |
|-------------|----------------|---------------|------------|
| TC-COND-UC12-001 | Check-in → OCCUPIED + Folio | `CheckinService.processCheckIn()` | TC-UC12-001 |
| TC-COND-UC12-002 | Check-in fail (phòng Dirty) | `CheckinService.processCheckIn()` | TC-UC12-002 |
| TC-COND-UC12-003 | Update Credit Limit | `CheckinService.updateCreditLimit()` | TC-UC12-003 |
| TC-COND-UC12-004 | Đổi phòng | `CheckinService.transferRoom()` | TC-UC12-004 |
| TC-COND-UC12-005 | Upgrade Dependent | `CheckinService.upgradeDependent()` | TC-UC12-005 |

### 4. Test Case Specification

#### TC-UC12-001 — Check-in → phòng OCCUPIED, tạo Folio
* **Severity:** CRITICAL | **Feature:** UC12.1 — `processCheckIn()` | **File:** `CheckinServiceUC12Test.java` | 🟢 GREEN
**Steps:** Gọi processCheckIn(BK-1001, guests) → Assert room OCCUPIED, Booking CHECKED_IN, Folio tạo

#### TC-UC12-002 — Check-in thất bại — phòng Dirty/Maintenance
* **Severity:** HIGH | **Feature:** UC12.1 | 🟢 GREEN
**Steps:** Set room DIRTY → processCheckIn → throws exception

#### TC-UC12-003 — Ủy quyền hạn mức — update Credit Limit
* **Severity:** MEDIUM | **Feature:** UC12.2 | 🟢 GREEN
**Steps:** updateCreditLimit(bookingId, newLimit) → Folio.creditLimit = newLimit

#### TC-UC12-004 — Đổi phòng
* **Severity:** HIGH | **Feature:** UC12.3 | 🟢 GREEN
**Steps:** transferRoom(bookingId, newRoomId) → phòng cũ DIRTY, phòng mới OCCUPIED

#### TC-UC12-005 — Nâng cấp Dependent → Customer + Account
* **Severity:** LOW | **Feature:** UC12.4 | 🟢 GREEN
**Steps:** upgradeDependent(dependentId) → Account/Customer mới được tạo

### 5. Red-Green-Refactor Tracker
| TC ID | Test File | 🔴 RED | 🔴 Commit | 🔴 Date | 🟢 GREEN | 🟢 Commit | 🟢 Date | 🔵 REFACTOR | 🔵 Commit | 🔵 Note |
|---|---|---|---|---|---|---|---|---|---|---|
| TC-UC12-001 | `CheckinServiceUC12Test.java` | [x] | `a1b2c3e` | 2026-06-12 | [x] | `b2c3d4f` | 2026-06-12 | [x] | `c3d4e5g` | ✅ Extract checkIn validation |
| TC-UC12-002 | `CheckinServiceUC12Test.java` | [x] | `a1b2c3e` | 2026-06-12 | [x] | `b2c3d4f` | 2026-06-12 | [x] | `c3d4e5g` | ✅ Illegal room status checks |
| TC-UC12-003 | `CheckinServiceUC12Test.java` | [x] | `a1b2c3e` | 2026-06-12 | [x] | `b2c3d4f` | 2026-06-12 | [x] | `c3d4e5g` | ✅ Validate credit limit updates |
| TC-UC12-004 | `CheckinServiceUC12Test.java` | [x] | `a1b2c3e` | 2026-06-12 | [x] | `b2c3d4f` | 2026-06-12 | [x] | `c3d4e5g` | ✅ Room transfer extract |
| TC-UC12-005 | `CheckinServiceUC12Test.java` | [x] | `a1b2c3e` | 2026-06-12 | [x] | `b2c3d4f` | 2026-06-12 | [x] | `c3d4e5g` | ✅ Account generator extract |

### 6. Rollback Plan
`git checkout -- src/main/java/com/kawai/services/impl/CheckinServiceImpl.java`