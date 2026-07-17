# TEST-DRIVEN DEVELOPMENT SPECIFICATION — UC22: ĐẶT MÓN VNPAY

## Mẫu Đặc tả Kiểm thử Hướng Phát triển — KAWAI RETREAT RESORT & HUB

| Field                    | Value                                                |
| ------------------------ | ---------------------------------------------------- |
| **Document ID**    | `KAWAI-TDD-UC22-001`                               |
| **Version**        | 1.0                                                  |
| **Date**           | 2026-07-02                                           |
| **Status**         | Approved                                             |
| **Standard**       | ISO/IEC/IEEE 29119-3:2021 — Software Testing Part 3 |
| **Author**         | Trịnh Minh Đức                                     |
| **Reviewed by**    | Nguyễn Xuân Lưu                                   |
| **Approved by**    | [ ] Pending                                          |
| **Classification** | Internal — Confidential                             |

**References:**
* `EDS_UC22_Dat_Mon_Truc_Tuyen_VNPAY.md`

> **Quy ước TDD:** Thứ tự bắt buộc: viết test (.java) → chạy → xác nhận FAIL 🔴 → implement → PASS 🟢 → refactor 🔵.

---

## CHANGELOG

| Ngày       | Người thực hiện | Nội dung thay đổi                                                              |
| ---------- | --------------- | ------------------------------------------------------------------------------- |
| 2026-07-02 | Trịnh Minh Đức  | Khởi tạo TDD Spec cho UC22 — Đặt món trực tuyến qua VNPAY                     |

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
| **Feature / Gap ID**    | `GAP-MOD3-UC22`                                                  |
| **Use Case**            | UC-22 — Đặt món trực tuyến qua VNPAY                        |
| **Compliance Scope**    | VNPAY HMAC-SHA512                                                 |

---

## 2. Logic Issues Resolved

| #  | Spec gốc (sai / thiếu)                                                                       | Thực tế (schema / policy)                                                             | Fix áp dụng trong test                                                                     |
| -- | ---------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------- |
| L1 | IPN update không an toàn                                                                       | Phải xác thực chữ ký (SecureHash)                                                       | Assert lỗi "Invalid Signature" khi gửi SecureHash sai.                                      |

---

## 3. Test Case Specification

### TC-UC22-001 — VNPAY Callback Thành Công (Response = 00)

**Severity:** CRITICAL
**CWE:** N/A
**Feature Under Test:** `VnPayPaymentController.ipn()`
**Test File:** `src/test/java/com/kawai/services/PosOnlineOrderUC22Test.java`
**TDD Phase:** 🟢 GREEN

**Preconditions:**
* Đơn hàng `ORD-999` đang ở trạng thái `AWAITING_PAYMENT`, `isPaidInPos = false`.
* Request IPN từ VNPAY có `vnp_ResponseCode = "00"` và `vnp_SecureHash` hợp lệ.

**Test Steps:**
1. Dựng mock params gửi vào IPN, ký hash SHA512 đúng với secret key.
2. Gửi request vào `/api/vnpay/ipn`.
3. Assert DB cập nhật đơn hàng thành `PENDING` và `isPaidInPos = true`.
4. Assert IPN trả về response `{"RspCode": "00", "Message": "Confirm Success"}`.

**Expected Result (PASS):**
* Hệ thống ghi nhận thanh toán thành công và báo lại cho VNPAY.

**Expected Result (FAIL):**
* API trả về 500 hoặc lưu trạng thái không đồng bộ (trả 00 cho VNPAY nhưng đơn chưa update).

---

### TC-UC22-002 — VNPAY Chữ Ký Sai

**Severity:** HIGH
**CWE:** CWE-347 (Improper Verification of Cryptographic Signature)
**Feature Under Test:** `SecureHash Validation`
**Test File:** `src/test/java/com/kawai/services/PosOnlineOrderUC22Test.java`
**TDD Phase:** 🟢 GREEN

**Preconditions:**
* Đơn hàng `ORD-999` đang `AWAITING_PAYMENT`.
* Request IPN bị hacker giả mạo (truyền sai `vnp_SecureHash`).

**Test Steps:**
1. Dựng mock params với `vnp_ResponseCode = "00"` nhưng `vnp_SecureHash` giả.
2. Gửi request vào `/api/vnpay/ipn`.
3. Assert đơn hàng không đổi trạng thái.
4. Assert IPN trả về `{"RspCode": "97", "Message": "Invalid Checksum"}`.

**Expected Result (PASS):**
* Chặn đứng việc gọi giả mạo, đơn hàng vẫn `AWAITING_PAYMENT`.

**Expected Result (FAIL):**
* Bị bypass chữ ký, hệ thống cập nhật đơn hàng thành PENDING mặc dù tiền chưa vào tài khoản.

---

## 4. Red-Green-Refactor Tracker

| UC   | TC ID       | Mô tả ngắn                                                    | Test File                                    | 🔴 RED | 🟢 GREEN | 🔵 REFACTOR |
| ---- | ----------- | ------------------------------------------------------------- | -------------------------------------------- | ------ | -------- | ----------- |
| UC22 | TC-UC22-001 | IPN Thành công                                                 | `PosOnlineOrderUC22Test.java`                | [x]    | [x]      | [x]         |
| UC22 | TC-UC22-002 | Sai chữ ký                                                     | `PosOnlineOrderUC22Test.java`                | [x]    | [x]      | [x]         |

---

## 5. Entry / Exit Criteria

### Exit Criteria / Definition of Done (DoD)
- [x] **2/2 test cases** trong tracker chuyển sang trạng thái 🟢 GREEN.
- [x] Code coverage ≥ 80% cho Use Case.

---

## 6. Rollback Plan
| Tình huống                                           | Hành động                                                                        |
| ------------------------------------------------------ | ----------------------------------------------------------------------------------- |
| Chữ ký VNPAY không xác thực được do config sai       | Kiểm tra lại vnp_HashSecret trong biến môi trường và đồng bộ với Sandbox VNPAY. |
