# TEST-DRIVEN DEVELOPMENT SPECIFICATION — UC18: CHỐT CA / BÁO CÁO F&B

## Mẫu Đặc tả Kiểm thử Hướng Phát triển — KAWAI RETREAT RESORT & HUB

| Field                    | Value                                                |
| ------------------------ | ---------------------------------------------------- |
| **Document ID**    | `KAWAI-TDD-UC18-001`                               |
| **Version**        | 1.0                                                  |
| **Date**           | 2026-07-02                                           |
| **Status**         | Approved                                             |
| **Standard**       | ISO/IEC/IEEE 29119-3:2021 — Software Testing Part 3 |
| **Author**         | Trịnh Minh Đức                                     |
| **Reviewed by**    | Nguyễn Xuân Lưu                                   |
| **Approved by**    | [ ] Pending                                          |
| **Classification** | Internal — Confidential                             |

**References:**
* `EDS_UC18_Chot_Ca_Bao_Cao_FnB.md`

> **Quy ước TDD:** Thứ tự bắt buộc: viết test (.java) → chạy → xác nhận FAIL 🔴 → implement → PASS 🟢 → refactor 🔵.

---

## CHANGELOG

| Ngày       | Người thực hiện | Nội dung thay đổi                                                              |
| ---------- | --------------- | ------------------------------------------------------------------------------- |
| 2026-07-02 | Trịnh Minh Đức  | Khởi tạo TDD Spec cho UC18 — Chốt ca / Báo cáo doanh thu F&B                  |

---

## MỤC LỤC
1. [Thông tin Module](#1-thong-tin-module)
2. [Logic Issues Resolved](#2-logic-issues-resolved)
3. [Test Case Specification](#3-test-case-specification)
4. [Red-Green-Refactor Tracker](#4-red-green-refactor-tracker)
5. [Entry / Exit Criteria](#5-entry--exit-criteria)
6. [Rollback Plan](#6-rollback-plan)

---

## 1. Thông tin Module

| Field                         | Value                                                              |
| ----------------------------- | ------------------------------------------------------------------ |
| **Feature / Gap ID**    | `GAP-MOD3-UC18`                                                  |
| **Use Case**            | UC-18 — Chốt ca / Báo cáo doanh thu F&B                     |
| **Compliance Scope**    | BR-FIN-15, BR-SYS-04                                              |

---

## 2. Logic Issues Resolved

| #  | Spec gốc (sai / thiếu)                                                                       | Thực tế (schema / policy)                                                             | Fix áp dụng trong test                                                                     |
| -- | ---------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------- |
| L1 | Chốt ca nhiều lần trong ngày                                                                   | 1 ngày chỉ có 1 báo cáo chốt (Idempotent guard)                                         | Assert ném lỗi khi cố chốt lần 2.                                                           |
| L2 | Tính tổng tiền cả đơn chưa thanh toán                                                          | Chỉ tính đơn `isPaidInPos = true`                                                       | Đưa logic filter vào test data để verify totalRevenue.                                      |

---

## 3. Test Case Specification

### TC-UC18-001 — Chốt ca thành công (Tính đúng tổng tiền)

**Severity:** CRITICAL
**CWE:** N/A
**Feature Under Test:** `FnBDailyReportServiceImpl.calculateRevenue() & save()`
**Test File:** `src/test/java/com/kawai/services/FnBDailyReportServiceUC18Test.java`
**TDD Phase:** 🟢 GREEN

**Preconditions:**
* DB chứa 3 đơn hàng hôm nay:
  * Đơn 1: 100k, `isPaidInPos = true`
  * Đơn 2: 50k, `isPaidInPos = true`
  * Đơn 3: 200k, `isPaidInPos = false` (chưa thanh toán)

**Test Steps:**
1. Mock Repository trả về danh sách 3 đơn trên theo ngày.
2. Gọi API hoặc Service thực hiện chốt ca `closeReport(today)`.
3. Assert Entity `FnBDailyReport` được tạo ra để lưu.
4. Assert `totalRevenue` của báo cáo phải bằng `150,000` (chỉ cộng đơn đã pay).

**Expected Result (PASS):**
* Lọc bỏ hoàn toàn đơn chưa thanh toán (isPaidInPos = false) ra khỏi báo cáo chốt doanh thu.

**Expected Result (FAIL):**
* Tổng doanh thu báo cáo sai lầm (ra 350,000 VND).

---

### TC-UC18-002 — Ngăn chốt ca 2 lần cùng ngày (Idempotent)

**Severity:** HIGH
**CWE:** CWE-362
**Feature Under Test:** `Idempotent validation`
**Test File:** `src/test/java/com/kawai/services/FnBDailyReportServiceUC18Test.java`
**TDD Phase:** 🟢 GREEN

**Preconditions:**
* Đã tồn tại một `FnBDailyReport` cho ngày hôm nay trong DB.

**Test Steps:**
1. Mock `FnBDailyReportRepository.existsByReportDate(today)` trả về `true`.
2. Gọi hàm `closeReport(today)`.
3. Assert bắt được Exception.

**Expected Result (PASS):**
* Ném lỗi `BusinessLogicException`: "FNB-RPT-001: Báo cáo ngày hôm nay đã được chốt".

**Expected Result (FAIL):**
* Lưu đè (overwrite) snapshot báo cáo cũ hoặc tạo đúp báo cáo cùng 1 ngày.

---

## 4. Red-Green-Refactor Tracker

| UC   | TC ID       | Mô tả ngắn                                                    | Test File                                    | 🔴 RED | 🟢 GREEN | 🔵 REFACTOR |
| ---- | ----------- | ------------------------------------------------------------- | -------------------------------------------- | ------ | -------- | ----------- |
| UC18 | TC-UC18-001 | Tính tổng tiền và lưu báo cáo thành công                       | `FnBDailyReportServiceUC18Test.java`             | [x]    | [x]      | [x]         |
| UC18 | TC-UC18-002 | Ngăn chốt nhiều lần trong ngày                                 | `FnBDailyReportServiceUC18Test.java`             | [x]    | [x]      | [x]         |

---

## 5. Entry / Exit Criteria

### Exit Criteria / Definition of Done (DoD)
- [x] **2/2 test cases** trong tracker chuyển sang trạng thái 🟢 GREEN.
- [x] Code coverage ≥ 80% cho Use Case.

---

## 6. Rollback Plan
| Tình huống                                           | Hành động                                                                        |
| ------------------------------------------------------ | ----------------------------------------------------------------------------------- |
| Lỗi Transaction khi lưu Snapshot                     | Bổ sung logic try/catch và log lỗi để tránh rollback toàn bộ hệ thống Pos.          |