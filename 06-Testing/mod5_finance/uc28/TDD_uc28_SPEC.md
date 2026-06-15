# TEST-DRIVEN DEVELOPMENT SPECIFICATION
## UC28 — Kết xuất dữ liệu PDF/Excel

| Field | Value |
|-------|-------|
| **Document ID** | `KAWAI-TDD-MOD5-UC28-001` |
| **Version** | 1.0 |
| **Date** | 2026-06-15 |
| **Status** | Approved |
| **Standard** | ISO/IEC/IEEE 29119-3:2021 |
| **Author** | Nguyễn Xuân Lưu — Tech Lead |
| **Reviewed by** | [x] Nguyễn Xuân Lưu — Tech Lead |
| **DPO Sign-off** | `[x] Approved – 2026-06-15 – Nguyễn Xuân Lưu` |
| **Approved by** | `[x] Nguyễn Xuân Lưu – 2026-06-15` |
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
| **Feature** | UC28: Kết xuất file |
| **Priority** | 🟢 P3 |

---

### 2. Logic Issues Resolved
Tránh OOM (Out Of Memory) bằng cách dùng Streaming cho file Excel lớn nếu cần.

---

### 3. Test Design Specification (TDS)
#### TDS-01 — Scope / Phạm vi
Test generation logic đảm bảo sinh ra stream file không lỗi.

---

### 4. Test Case Specification

#### `TC-UC28-001` — Kết xuất PDF
* **Severity:** MEDIUM | **Feature:** UC28
**Steps:** Gọi exportPdf -> Verify `byte[]` trả về bắt đầu bằng magic bytes của PDF (`%PDF-`).

#### `TC-UC28-002` — Kết xuất Excel
* **Severity:** MEDIUM | **Feature:** UC28
**Steps:** Gọi exportExcel -> Verify file sinh ra chứa các cột chuẩn.

---

### 5. Red-Green-Refactor Tracker

| TC ID | Mô tả | Test File | 🔴 RED | 🟢 GREEN | 🔵 REFACTOR |
|---|---|---|---|---|---|
| TC-UC28-001 | Export PDF | `ExportTest.java` | [x] | [x] | ✅ iText implementation |
| TC-UC28-002 | Export Excel| `ExportTest.java` | [x] | [x] | ✅ POI Workbook |

---

### 6. Entry / Exit Criteria
- **Exit:** Chạy Unit test không crash, byte length > 0.

---

### 7. Rollback Plan
N/A