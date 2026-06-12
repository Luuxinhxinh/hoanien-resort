# TEST-DRIVEN DEVELOPMENT SPECIFICATION TEMPLATE
## Mẫu Đặc tả Kiểm thử Hướng Phát triển

| Field | Value |
| --- | --- |
| **Document ID** | `KAWAI-TDD-MOD2-001` |
| **Version** | 1.0 |
| **Date** | 2026-06-12 |
| **Status** | Draft / In Review / Approved |
| **Standard** | ISO/IEC/IEEE 29119-3:2021 — Software Testing Part 3: Test Documentation |
| **Author** | `Nguyễn Xuân Lưu - Tech Lead` |
| **Reviewed by** | `[ ] Nguyễn Xuân Lưu – Pending` |
| **DPO Sign-off** | `[ ] Pending` |
| **Approved by** | `[ ] Pending` |
| **Classification** | Internal – Confidential |

---

### References:
*   `04_testing/SOFTWARE_TEST_PLAN.md` (FPT-EDU-STP-001 v2.0) — Master Test Plan
*   `01_Requirements/SRS.md` — Functional requirements
*   `03_implement/[TECH-SPEC-ID]_[FeatureName].md` — Technical Specification
*   `02_Design/ADR/ADR-0XX` — Architecture Decision Records liên quan
*   `[Điều luật]` — Legal basis (Luật 91/2025, NĐ 356/2025)

> [!NOTE]
> **Quy ước TDD:** Tài liệu này mô tả test cases TRƯỚC khi viết production code.
> Thứ tự bắt buộc: viết test (`.spec.ts`) -> chạy -> xác nhận FAIL 🔴 -> implement -> PASS 🟢 -> refactor 🔵.
> Không mark test là 🟢 nếu `npm test` chưa xanh.
> Test data dùng tenant `fpt-edu` (QA). Không dùng PII thật.

---

### CHANGELOG

> [!IMPORTANT]
> **Policy 4.4 — Immutable History:** Không bao giờ xóa thông tin cũ.

| Ngày | Người thực hiện | Nội dung thay đổi |
| --- | --- | --- |
| 2026-06-12 | `Nguyễn Xuân Lưu` | Khởi tạo tài liệu — TDD spec cho Đặt phòng & Tiền sảnh vận hành |

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
| **Feature / Gap ID** | `GAP-MOD2` |
| **Module** | `Đặt phòng & Tiền sảnh vận hành` |
| **Spec gốc** | `KAWAI-EDS-MOD2-001` |
| **Priority** | 🔴 P0 |
| **Sprint** | `S[N]` (2026-06-12 -> 2026-06-12) |
| **Milestone** | M3 Alpha — 2026-07-11 |
| **Data Classification** | Confidential / PII |
| **Compliance Scope** | Luật du lịch Việt Nam 2017 |
| **Upstream Dependencies** | `[Service/Module phụ thuộc]` |
| **Downstream Consumers** | `[Service/Module tiêu thụ kết quả]` |

---

### 2. Logic Issues Resolved

*Bắt buộc điền trước khi viết test. Liệt kê mọi sai lệch giữa spec thiết kế và schema/policy/codebase thực tế. Test cases sẽ encode hành vi đã sửa, không phải hành vi trong spec gốc.*

| # | Spec gốc (sai / thiếu) | Thực tế (schema / policy) | Fix áp dụng trong test |
| --- | --- | --- | --- |
| **L1** | *(đoạn code / logic trong spec)* | *(field name / enum / `CLAUDE.md` policy)* | *(hành vi đúng cần test)* |
| **L2** | | | |

---

### 3. Test Design Specification (TDS)

#### TDS-01 — Scope / Phạm vi
Mô tả phạm vi của TDD spec này: component nào được kiểm thử, layer nào được bao phủ.

`[Module]` bao gồm các layer:
- Domain (pure logic – no deps)
- Application / Use Cases (mock Prisma inline)
- Services (mock Prisma inline)
- Controller (mock use cases)
- Integration (Testcontainers PostgreSQL + Redis)

#### TDS-02 — Test Basis / Cơ sở Kiểm thử
Điều kiện kiểm thử được derive từ các nguồn sau:

| Source | Items Derived |
| --- | --- |
| `SRS.md` `UC-XX` | *(hành vi người dùng / business rule)* |
| `ADR-0XX` | *(architecture constraint)* |
| `BR-XXXX-001` | *(business rule số hiệu)* |
| Luật 91/2025 Điều X | *(yêu cầu pháp lý)* |
| NĐ 356/2025 Điều X | *(yêu cầu nghị định)* |
| `[TECH-SPEC-ID]` §X | *(algorithm / logic từ spec kỹ thuật)* |

#### TDS-03 — Test Conditions and Coverage Items
Mỗi condition map sang >= 1 test case cụ thể.

| Condition ID | Test Condition | Coverage Item | Test Cases |
| --- | --- | --- | --- |
| **TC-COND-001** | *(điều kiện nghiệp vụ)* | *(method / class / API endpoint)* | `MOD2-TC-001` |
| **TC-COND-002** | | | |

#### TDS-04 — Test Techniques / Kỹ thuật Kiểm thử

| Technique (ISO 29119-4) | Applied To | Rationale |
| --- | --- | --- |
| **Equivalence Partitioning** | *(input domain)* | *(lý do)* |
| **Boundary Value Analysis** | *(boundary case)* | *(lý do)* |
| **State Transition Testing** | *(FSM / status enum)* | *(lý do)* |
| **Error Guessing** | *(security / attack vectors)* | *(lý do)* |

#### TDS-05 — Test Data Requirements

| Fixture ID | Type | Value / Logic | Mục đích |
| --- | --- | --- | --- |
| **FX-001** | DB seed | `{ id, status: 'APPROVED', ... }` | Happy path |
| **FX-002** | DB seed | `{ id, status: 'SUPERSEDED' }` | Version reject |
| **FX-003** | env | `HMAC_SECRET=test-secret-32chars` | HMAC computation |
| **FX-004** | JWT | `{ sub: 'sub-001', role: 'DPO' }` | Auth context |

---

### 4. Test Case Specification

*   **TC ID format:** `MOD2-TC-[NNN]`
*   **Severity:** CRITICAL / HIGH / MEDIUM / LOW (theo CVSS)
*   **Status:** 🔴 Not written / 🟡 Written-failing / 🟢 Passing

#### `MOD2-TC-001` — [Tên test case ngắn gọn]

*   **Severity:** `CRITICAL / HIGH / MEDIUM / LOW`
*   **CWE:** `CWE-XXX` – [Tên CWE nếu là security test]
*   **Legal:** `[Luật / Nghị định / ADR áp dụng nếu có]`
*   **Feature Under Test:** `[ClassName.methodName() / API endpoint / React component]`
*   **Test File:** `[đường dẫn file .spec.ts hoặc .test.tsx]`
*   **TDD Phase:** 🔴 RED — chưa implement
*   **Condition Ref:** `TC-COND-001`

**Preconditions:**
- *(trạng thái DB / service / env cần có trước khi chạy test)*
- *(fixture ID cần thiết: FX-001, FX-003, ...)*

**Test Steps:**
1. *(Arrange: chuẩn bị mock / seed data)*
2. *(Act: gọi method / API endpoint)*
3. *(Assert: kiểm tra kết quả)*

**Expected Result (PASS — hành vi đúng):**
- *(kết quả trả về / DB state / exception message)*

**Expected Result (FAIL — dấu hiệu lỗi):**
- *(điều gì xảy ra nếu implementation sai)*

**Current Status:** 🔴 Not written
**Implementation Note:** *(ghi chú cho developer khi implement để pass test này)*

---

#### `MOD2-TC-002` — ...
*(Lặp lại block trên cho mỗi test case)*

---

### SECURITY TEST CASES
*Test cases kiểm tra attack vectors — điền thêm field OWASP và CWE.*

#### `MOD2-TC-0XX` — [Tên attack vector]

*   **Severity:** `CRITICAL`
*   **OWASP:** `A0X:2021` – [Category]
*   **CWE:** `CWE-XXX` – Nguyễn Xuân Lưu
*   **Legal:** `[Compliance requirement bị vi phạm nếu test FAIL]`
*   **Feature Under Test:** `[Endpoint / Guard / Service]`
*   **Test File:** `[file]`
*   **TDD Phase:** 🔴 RED

**Preconditions:**
- *(trạng thái cho phép tấn công)*

**Test Steps (Attack Simulation):**
1. *(Chuẩn bị điều kiện tấn công)*
2. *(Thực hiện tấn công)*
3. *(Kiểm tra response / DB state)*

**Expected Result (PASS = hệ thống an toàn):**
- `403 Forbidden` hoặc exception cụ thể

**Expected Result (FAIL = lỗ hổng tồn tại):**
- *(mô tả hành vi nguy hiểm nếu guard không hoạt động)*

**Current Status:** 🔴 Not written

---

### INTEGRATION TEST CASES
*Dùng Testcontainers (PostgreSqlContainer + RedisContainer). Timeout: 120s.*

#### `MOD2-TC-INT-001` — [Tên scenario end-to-end]

*   **Severity:** `HIGH`
*   **Feature Under Test:** Full flow: [bước đầu -> bước cuối]
*   **Test File:** `apps/core-api/test/modules/[module]/[feature].integration-spec.ts`
*   **TDD Phase:** 🔴 RED
*   **Condition Ref:** `TC-COND-XXX`

**Preconditions:**
- PostgreSQL container running (Testcontainers auto-start)
- `prisma db push --skip-generate` applied
- Seed: *(fixtures cần thiết)*

**Test Steps:**
1. *(seed minimal data)*
2. *(call API step 1)*
3. *(call API step 2)*
4. *(assert DB state)*

**Expected Result (PASS):**
- *(DB assertion: count, field values)*
- *(API response shape)*

**Expected Result (FAIL):**
- *(dấu hiệu lỗi integration)*

**DB Assertion:**
```typescript
const record = await prisma.client.[model].findUnique({ where: { id } });
expect(record).not.toBeNull();
expect(record.status).toBe('[EXPECTED_STATUS]');
```

**Current Status:** 🔴 Not written

---

### 5. Red-Green-Refactor Tracker

| TC ID | Test File | 🔴 RED confirmed | 🟢 GREEN (commit) | 🔵 REFACTOR note |
| --- | --- | :---: | --- | --- |
| `MOD2-TC-001` | `[path].spec.ts:line` | `[ ]` | `[hash]` | *(extract method, typing, etc.)* |

---

### 6. Entry / Exit Criteria

#### Entry Criteria (Điều kiện bắt đầu)
- [ ] Spec kỹ thuật `[TECH-SPEC-ID]` đã được review và approve
- [ ] Logic Issues (Section 2) đã được confirm với Principal Architect
- [ ] Prisma schema migration cho feature này đã được approved
- [ ] Test fixtures (Section 3 TDS-05) đã được chuẩn bị

#### Exit Criteria (Điều kiện kết thúc — DoD)
- [ ] `npm test` — tất cả unit tests xanh (không có skip)
- [ ] `npm run test:integration` — tất cả integration tests xanh
- [ ] Test coverage >= 80% lines cho các file mới tạo
- [ ] Không có `any` type trong production code liên quan
- [ ] `subjectId` không xuất hiện plaintext trong logs
- [ ] *(tiêu chí nghiệp vụ cụ thể của feature này)*

#### Suspension Criteria (Điều kiện tạm dừng)
- Blocker dependency chưa sẵn sàng (migration, external service)
- Phát hiện lỗi kiến trúc mới cần Principal Architect review
- CI pipeline bị broken bởi thay đổi khác

---

### 7. Rollback Plan

**Revert migration (dev only – KHÔNG chạy trên production):**
`npx prisma migrate reset`

**Revert implementation files:**
`git checkout -- [path/to/changed/files]`

**Gap vẫn OPEN -> giữ nguyên entry trong PHASE_GAP_ANALYSIS.md**
