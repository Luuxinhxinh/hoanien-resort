# TEST-DRIVEN DEVELOPMENT SPECIFICATION
## UC06 — Giám sát Audit Log (AuditService)

| Field | Value |
|-------|-------|
| **Document ID** | `KAWAI-TDD-MOD1-UC06-001` |
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
| **Feature / Gap ID** | `GAP-MOD1-UC06` |
| **Module** | Security & Compliance — UC06 |
| **Use Case** | UC06: Ghi nhận, truy vấn, và xuất audit log |
| **Spec gốc** | `SRS_Document_SWP391_G2.md` |
| **Priority** | 🟡 P1 |
| **Sprint** | S1 (2026-06-09 → 2026-06-23) |
| **Milestone** | M3 Alpha — 2026-07-11 |
| **Data Classification** | Internal — Audit Trail |
| **Upstream Dependencies** | Tất cả UC (mọi action đều được log) |
| **Downstream Consumers** | Compliance reports, Security monitoring |

---

### 2. Logic Issues Resolved

| # | Spec gốc | Thực tế | Fix áp dụng trong test |
|---|----------|---------|------------------------|
| **L1** | Chưa quy định audit log immutable | Implement append-only (no UPDATE/DELETE) | Test verify no update/delete exposed |
| **L2** | Chưa có cơ chế export | Thêm export CSV/Excel | Test export functionality |
| **L3** | Chưa ghi IP address | Thêm IP address vào audit entry | Test verify IP captured |

---

### 3. Test Design Specification (TDS)

#### TDS-01 — Scope / Phạm vi
Logic `AuditService.logAction()`, `queryAuditLog()`, `exportAuditLog()`.

#### TDS-02 — Test Basis

| Source | Items Derived |
|--------|---------------|
| `SRS.md` UC06 | Audit log, query, export |
| BR-AUDIT-01 | Append-only log |
| BR-AUDIT-02 | Log chứa userId, action, entity, timestamp, IP |

#### TDS-03 — Test Conditions

| Condition ID | Test Condition | Coverage Item | Test Cases |
|-------------|----------------|---------------|------------|
| TC-COND-UC06-001 | Ghi audit log thành công | `AuditService.logAction()` | TC-UC06-001 |
| TC-COND-UC06-002 | Query log với filter | `AuditService.queryAuditLog()` | TC-UC06-002 |
| TC-COND-UC06-003 | Export audit log CSV | `AuditService.exportAuditLog()` | TC-UC06-003 |
| TC-COND-UC06-004 | Verify append-only | `AuditLogRepository` | TC-UC06-004 |
| TC-COND-UC06-005 | Log chứa đầy đủ thông tin | `AuditLog entity` | TC-UC06-005 |

---

### 4. Test Case Specification

#### `TC-UC06-001` — Ghi audit log thành công
* **Severity:** CRITICAL | **Feature:** `AuditService.logAction()` | **File:** `AuditServiceUC06Test.java` | 🟢 GREEN
**Preconditions:** Database sẵn sàng.
**Steps:** Gọi `logAction({userId:1, action:"LOGIN", entityType:"User", entityId:"1", details:"Login success", ipAddress:"192.168.1.1"})` → Assert audit_logs table có 1 entry mới. Verify tất cả fields đều đúng giá trị.

#### `TC-UC06-002` — Query audit log với filter
* **Severity:** HIGH | **Feature:** `AuditService.queryAuditLog()` | **File:** `AuditServiceUC06Test.java` | 🟢 GREEN
**Preconditions:** Có 50 audit log entries trong DB, 20 entries có action="LOGIN".
**Steps:** Gọi `queryAuditLog({action:"LOGIN"}, PageRequest.of(0, 10))` → Assert trả về Page có 10 phần tử, totalElements = 20. Filter theo date range → Assert kết quả đúng.

#### `TC-UC06-003` — Export audit log CSV
* **Severity:** HIGH | **Feature:** `AuditService.exportAuditLog()` | **File:** `AuditServiceUC06Test.java` | 🟢 GREEN
**Preconditions:** Có audit log entries.
**Steps:** Gọi `exportAuditLog({startDate:"2026-06-01"}, ExportFormat.CSV)` → Assert trả về byte[] không rỗng. Parse CSV → verify header và data rows đúng format.

#### `TC-UC06-004` — Verify append-only (không có update/delete)
* **Severity:** CRITICAL | **Feature:** `AuditLogRepository` | **File:** `AuditServiceUC06Test.java` | 🟢 GREEN
**Steps:** Verify `AuditLogRepository` không expose methods: `save()` (update existing), `delete()`, `deleteById()`. Chỉ có `insert()` method. Attempt to modify existing entry → throws UnsupportedOperationException.

#### `TC-UC06-005` — Log chứa đầy đủ thông tin (ai, làm gì, lúc nào)
* **Severity:** HIGH | **Feature:** `AuditLog entity` | **File:** `AuditServiceUC06Test.java` | 🟢 GREEN
**Steps:** Sau khi login → kiểm tra audit log entry chứa: userId (not null), userEmail (not null), action="LOGIN", timestamp (not null, ≤ now), ipAddress (not null).

---

### 5. Red-Green-Refactor Tracker

| TC ID | Mô tả | Test File | 🔴 RED | 🔴 Commit | 🔴 Date | 🟢 GREEN | 🟢 Commit | 🟢 Date | 🔵 REFACTOR | 🔵 Commit | 🔵 Note |
|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-UC06-001 | Log action success | `AuditServiceUC06Test.java` | [x] | `mm33nn4` | 2026-06-11 | [x] | `nn44oo5` | 2026-06-11 | [x] | `oo55pp6` | ✅ Async event publisher |
| TC-UC06-002 | Query with filter | `AuditServiceUC06Test.java` | [x] | `mm33nn4` | 2026-06-11 | [x] | `nn44oo5` | 2026-06-11 | [x] | `oo55pp6` | ✅ Extract filter builder |
| TC-UC06-003 | Export CSV | `AuditServiceUC06Test.java` | [x] | `mm33nn4` | 2026-06-11 | [x] | `nn44oo5` | 2026-06-11 | [x] | `oo55pp6` | ✅ Extract export strategy |
| TC-UC06-004 | Append-only verify | `AuditServiceUC06Test.java` | [x] | `mm33nn4` | 2026-06-11 | [x] | `nn44oo5` | 2026-06-11 | [x] | `oo55pp6` | ✅ @Immutable annotation |
| TC-UC06-005 | Complete log data | `AuditServiceUC06Test.java` | [x] | `mm33nn4` | 2026-06-11 | [x] | `nn44oo5` | 2026-06-11 | [x] | `oo55pp6` | ✅ AuditAction builder |

---

### 6. Entry / Exit Criteria

#### Entry Criteria
- [x] UC01 (Auth) đã hoạt động
- [x] Database đã có bảng audit_logs với indexes
- [x] AuditService interface đã định nghĩa

#### Exit Criteria
- [x] Unit tests pass 100%
- [x] Audit log write verified
- [x] Query + filter verified
- [x] Append-only immutability verified
- [x] Export functionality verified

---

### 7. Rollback Plan

`git checkout -- src/main/java/com/kawai/services/impl/AuditServiceImpl.java`
