# TEST-DRIVEN DEVELOPMENT SPECIFICATION — UC17: ĐẶT MÓN ONLINE (ROOM SERVICE)

## Mẫu Đặc tả Kiểm thử Hướng Phát triển — KAWAI RETREAT RESORT & HUB

| Field                    | Value                                                |
| ------------------------ | ---------------------------------------------------- |
| **Document ID**    | `KAWAI-TDD-UC17-001`                               |
| **Version**        | 1.0                                                  |
| **Date**           | 2026-07-02                                           |
| **Status**         | Approved                                             |
| **Standard**       | ISO/IEC/IEEE 29119-3:2021 — Software Testing Part 3 |
| **Author**         | Trịnh Minh Đức                                     |
| **Reviewed by**    | Nguyễn Xuân Lưu                                   |
| **Approved by**    | [ ] Pending                                          |
| **Classification** | Internal — Confidential                             |

**References:**
* `EDS_UC17_Dat_Mon_Online.md`
* `PosApiControllerUC17Test.java`

> **Quy ước TDD:** Thứ tự bắt buộc: viết test (.java) → chạy → xác nhận FAIL 🔴 → implement → PASS 🟢 → refactor 🔵.

---

## CHANGELOG

| Ngày       | Người thực hiện | Nội dung thay đổi                                                              |
| ---------- | --------------- | ------------------------------------------------------------------------------- |
| 2026-07-02 | Trịnh Minh Đức  | Khởi tạo TDD Spec cho UC17 — Đặt món online (Room Service & E-Menu)           |

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
| **Feature / Gap ID**    | `GAP-MOD3-UC17`                                                  |
| **Use Case**            | UC-17 — Đặt món online (Room Service)                       |
| **Compliance Scope**    | POS-002, POS-005, POS-009                                         |

---

## 2. Logic Issues Resolved

| #  | Spec gốc (sai / thiếu)                                                                       | Thực tế (schema / policy)                                                             | Fix áp dụng trong test                                                                     |
| -- | ---------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------- |
| L1 | Chưa rõ mức hạn mức tín dụng được dùng                                                         | Ưu tiên `subCreditLimit`, nếu không có mới dùng `creditLimit`                           | Viết test kiểm tra mock `RoomBookingDetail.subCreditLimit` so với tổng tiền Room Service.     |
| L2 | Tính phụ phí Room Service thiếu 5%                                                             | Room Service (CHARGE_TO_ROOM) phải cộng thêm 5% service fee                             | Assert FolioItem amount = subtotal + 5%.                                                    |

---

## 3. Test Case Specification

### TC-UC17-001 — Đặt Room Service (Charge to Room) thành công

**Severity:** CRITICAL
**CWE:** N/A
**Feature Under Test:** `PosServiceImpl.createOrder()`, `FolioItemRepository`
**Test File:** `src/test/java/com/kawai/controllers/api/PosApiControllerUC17Test.java`
**TDD Phase:** 🟢 GREEN

**Preconditions:**
* Có RoomBookingDetail "101" với hạn mức khả dụng 500,000 VND.
* Giỏ hàng có 2 món giá tổng = 200,000 VND. Payment: `CHARGE_TO_ROOM`.

**Test Steps:**
1. Mock dữ liệu booking và hạn mức phòng.
2. Gọi `createOrder` (type = `room-svc`, payment = `CHARGE_TO_ROOM`).
3. Assert hệ thống tính tổng phụ phí `5% * 200,000 = 10,000`.
4. Assert tổng tiền tạo ra trên `FolioItem` là `210,000 VND`.
5. Assert hạn mức tín dụng đủ và không có Exception.

**Expected Result (PASS):**
* Đơn tạo thành công, `FolioItem` được ghi nhận với `amount = 210,000`.

**Expected Result (FAIL):**
* Hệ thống quên cộng phụ phí 5% vào hoá đơn Room Service.
* Ghi nhầm `sourceDepartment` trên FolioItem.

---

### TC-UC17-002 — Vượt hạn mức tín dụng (POS-005)

**Severity:** HIGH
**CWE:** CWE-841
**Feature Under Test:** `Credit Limit Validation`
**Test File:** `src/test/java/com/kawai/controllers/api/PosApiControllerUC17Test.java`
**TDD Phase:** 🟢 GREEN

**Preconditions:**
* Phòng "102" có hạn mức khả dụng: `50,000 VND`.
* Giá trị giỏ hàng: `450,000 VND`.

**Test Steps:**
1. Mock RoomBookingDetail trả về subCreditLimit = 50,000.
2. Gọi `createOrder` qua API với tổng tiền tính toán lớn hơn hạn mức.
3. Assert bắt được `BusinessLogicException`.

**Expected Result (PASS):**
* Ném lỗi: "POS-005: Hạn mức tín dụng của phòng không đủ để thanh toán".

**Expected Result (FAIL):**
* Vượt rào tạo đơn thành công, gây rủi ro thất thoát doanh thu.

---

### TC-UC17-003 — Đặt Dine-in ngoài giờ hoạt động (POS-009)

**Severity:** HIGH
**CWE:** N/A
**Feature Under Test:** `PosServiceImpl` business hours check
**Test File:** `src/test/java/com/kawai/controllers/api/PosApiControllerUC17Test.java`
**TDD Phase:** 🟢 GREEN

**Preconditions:**
* Hệ thống mock thời gian là 23:30 (nhà hàng đóng).
* Loại đặt món: `dine-in`.

**Test Steps:**
1. Gọi `createOrder` với `orderType = dine-in`.
2. Assert hệ thống kiểm tra giờ làm việc.

**Expected Result (PASS):**
* Ném lỗi "POS-009: Nhà hàng đóng cửa từ 23:00 đến 08:00".

**Expected Result (FAIL):**
* API vẫn pass vì check logic giờ hoạt động lỏng lẻo.

---

## 4. Red-Green-Refactor Tracker

| UC   | TC ID       | Mô tả ngắn                                                    | Test File                                    | 🔴 RED | 🟢 GREEN | 🔵 REFACTOR |
| ---- | ----------- | ------------------------------------------------------------- | -------------------------------------------- | ------ | -------- | ----------- |
| UC17 | TC-UC17-001 | Tạo Room Service Charge-to-Room thành công                     | `PosApiControllerUC17Test.java`              | [x]    | [x]      | [x]         |
| UC17 | TC-UC17-002 | Hạn mức tín dụng không đủ (POS-005)                            | `PosApiControllerUC17Test.java`              | [x]    | [x]      | [x]         |
| UC17 | TC-UC17-003 | Dine-in ngoài giờ (POS-009)                                    | `PosApiControllerUC17Test.java`              | [x]    | [x]      | [x]         |

---

## 5. Entry / Exit Criteria

### Exit Criteria / Definition of Done (DoD)
- [x] **3/3 test cases** trong tracker chuyển sang trạng thái 🟢 GREEN.
- [x] Code coverage ≥ 80% cho Use Case.

---

## 6. Rollback Plan
| Tình huống                                           | Hành động                                                                        |
| ------------------------------------------------------ | ----------------------------------------------------------------------------------- |
| Mock Repository không tiêm vào được PosApiController   | Dùng ReflectionTestUtils để ép kiểu thay vì @MockBean. |