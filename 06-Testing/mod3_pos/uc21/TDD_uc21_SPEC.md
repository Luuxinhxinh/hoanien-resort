# TEST-DRIVEN DEVELOPMENT SPECIFICATION — UC21: ĐẶT BÀN TRỰC TUYẾN

## Mẫu Đặc tả Kiểm thử Hướng Phát triển — KAWAI RETREAT RESORT & HUB

| Field                    | Value                                                |
| ------------------------ | ---------------------------------------------------- |
| **Document ID**    | `KAWAI-TDD-UC21-001`                               |
| **Version**        | 1.0                                                  |
| **Date**           | 2026-07-02                                           |
| **Status**         | Approved                                             |
| **Standard**       | ISO/IEC/IEEE 29119-3:2021 — Software Testing Part 3 |
| **Author**         | Trịnh Minh Đức                                     |
| **Reviewed by**    | Nguyễn Xuân Lưu                                   |
| **Approved by**    | [ ] Pending                                          |
| **Classification** | Internal — Confidential                             |

**References:**
* `EDS_UC21_Dat_Ban_Truc_Tuyen.md`

> **Quy ước TDD:** Thứ tự bắt buộc: viết test (.java) → chạy → xác nhận FAIL 🔴 → implement → PASS 🟢 → refactor 🔵.

---

## CHANGELOG

| Ngày       | Người thực hiện | Nội dung thay đổi                                                              |
| ---------- | --------------- | ------------------------------------------------------------------------------- |
| 2026-07-02 | Trịnh Minh Đức  | Khởi tạo TDD Spec cho UC21 — Đặt bàn trực tuyến (Online Table Reservation)    |

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
| **Feature / Gap ID**    | `GAP-MOD3-UC21`                                                  |
| **Use Case**            | UC-21 — Đặt bàn trực tuyến                                  |
| **Compliance Scope**    | TABLE-002, TABLE-003, TABLE-005, TABLE-006, TABLE-008             |

---

## 2. Logic Issues Resolved

| #  | Spec gốc (sai / thiếu)                                                                       | Thực tế (schema / policy)                                                             | Fix áp dụng trong test                                                                     |
| -- | ---------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------- |
| L1 | Không xử lý overlap giờ                                                                        | 1 bàn không nhận 2 booking cách nhau < buffer time (15-30p)                             | Assert test overlap logic conflict.                                                         |

---

## 3. Test Case Specification

### TC-UC21-001 — Đặt bàn thành công

**Severity:** CRITICAL
**CWE:** N/A
**Feature Under Test:** `TableReservationServiceImpl.createReservation()`
**Test File:** `src/test/java/com/kawai/services/TableReservationServiceTest.java`
**TDD Phase:** 🟢 GREEN

**Preconditions:**
* Dữ liệu request: Số lượng 4 người, Bàn "T10" (capacity 6).
* Thời gian: 19:00 ngày mai. Không có lịch đặt nào trùng.

**Test Steps:**
1. Mock Repository không có lịch trùng.
2. Gọi service tạo Reservation.
3. Assert Entity lưu với trạng thái `CONFIRMED`.
4. Assert hàm gửi EmailService được trigger.

**Expected Result (PASS):**
* Đặt bàn được lưu. Hệ thống gửi email cho khách.

**Expected Result (FAIL):**
* Lưu được nhưng Email không gửi, hoặc trạng thái mặc định không phải `CONFIRMED`.

---

### TC-UC21-002 — Trùng lịch (TABLE-003)

**Severity:** HIGH
**CWE:** CWE-362
**Feature Under Test:** `Overlap Validation`
**Test File:** `src/test/java/com/kawai/services/TableReservationServiceTest.java`
**TDD Phase:** 🟢 GREEN

**Preconditions:**
* Bàn "T10" đã có khách đặt từ 18:30 đến 20:30 ngày mai.
* Dữ liệu request mới: Đặt bàn "T10" lúc 19:30 ngày mai.

**Test Steps:**
1. Mock Repository trả về 1 record overlap.
2. Gọi API đặt bàn.
3. Assert Exception.

**Expected Result (PASS):**
* Ném lỗi "TABLE-003: Thời gian đặt bàn bị trùng lặp".

**Expected Result (FAIL):**
* Hệ thống nhận booking dẫn đến 2 khách tới tranh 1 bàn.

---

### TC-UC21-003 — Vượt sức chứa (TABLE-002)

**Severity:** MEDIUM
**CWE:** N/A
**Feature Under Test:** `Capacity Validation`
**Test File:** `src/test/java/com/kawai/services/TableReservationServiceTest.java`
**TDD Phase:** 🟢 GREEN

**Preconditions:**
* Khách chọn bàn "T01" có `capacity = 2`.
* Nhưng nhập số lượng khách `partySize = 4`.

**Test Steps:**
1. Mock Repository load bàn T01.
2. Gọi API đặt bàn.
3. Assert Exception.

**Expected Result (PASS):**
* Ném lỗi "TABLE-002: Số lượng khách vượt quá sức chứa của bàn".

**Expected Result (FAIL):**
* Đặt bàn thành công khiến bàn không đủ chỗ ngồi cho khách.

---

## 4. Red-Green-Refactor Tracker

| UC   | TC ID       | Mô tả ngắn                                                    | Test File                                    | 🔴 RED | 🟢 GREEN | 🔵 REFACTOR |
| ---- | ----------- | ------------------------------------------------------------- | -------------------------------------------- | ------ | -------- | ----------- |
| UC21 | TC-UC21-001 | Đặt bàn thành công, email gửi                                  | `TableReservationServiceTest.java`           | [x]    | [x]      | [x]         |
| UC21 | TC-UC21-002 | Trùng lịch (TABLE-003)                                         | `TableReservationServiceTest.java`           | [x]    | [x]      | [x]         |
| UC21 | TC-UC21-003 | Quá số người quy định (TABLE-002)                              | `TableReservationServiceTest.java`           | [x]    | [x]      | [x]         |

---

## 5. Entry / Exit Criteria

### Exit Criteria / Definition of Done (DoD)
- [x] **3/3 test cases** trong tracker chuyển sang trạng thái 🟢 GREEN.
- [x] Code coverage ≥ 80% cho Use Case.

---

## 6. Rollback Plan
| Tình huống                                           | Hành động                                                                        |
| ------------------------------------------------------ | ----------------------------------------------------------------------------------- |
| Trùng lịch bàn khi nhiều user cùng book đồng thời   | Sử dụng Pessimistic Locking hoặc cơ chế Queue để xử lý request booking. |
