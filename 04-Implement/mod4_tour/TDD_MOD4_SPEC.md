# TEST-DRIVEN DEVELOPMENT SPECIFICATION TEMPLATE
## Mẫu Đặc tả Kiểm thử Hướng Phát triển

| Field | Value |
| --- | --- |
| **Document ID** | `KAWAI-TDD-MOD4-001` |
| **Version** | 1.0 |
| **Date** | 2026-06-12 |
| **Status** | Approved |
| **Standard** | ISO/IEC/IEEE 29119-3:2021 |
| **Author** | Nguyễn Xuân Lưu - Tech Lead |

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
| **Feature / Gap ID** | `GAP-MOD4` |
| **Module** | Đặt Tour & Đánh giá |
| **Priority** | 🟡 P1 |
| **Milestone** | M3 Alpha — 2026-07-11 |
| **Data Classification** | Public / Internal |

---

### 2. Logic Issues Resolved
| # | Spec gốc | Thực tế | Fix áp dụng trong test |
| --- | --- | --- | --- |
| **L1** | Sập tour khi lỗi thời tiết | Bỏ qua lỗi (Graceful degradation) | Test mock Weather API timeout |

---

### 3. Test Design Specification (TDS)
#### TDS-01 — Scope / Phạm vi
Tích hợp API thứ 3 (AI, Weather), CRUD Review.

#### TDS-02 — Test Techniques
- Error Guessing (API Timeout).

---

### 4. Test Case Specification

#### `TC-M4-001` — Tìm kiếm tour — trả danh sách tour khả dụng + thông tin thời tiết
*   **Severity:** MEDIUM
*   **Feature Under Test:** UC19
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M4-002` — API thời tiết không phản hồi → vẫn trả tour, ẩn thông tin thời tiết
*   **Severity:** LOW
*   **Feature Under Test:** UC19
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M4-003` — Đặt tour thành công — tạo bản ghi Tour_Attendees
*   **Severity:** HIGH
*   **Feature Under Test:** UC20.1
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M4-004` — Tour hết slot — đặt thêm bị chặn, trả TOUR-001
*   **Severity:** CRITICAL
*   **Feature Under Test:** UC20.1
*   **Test Type:** Concurrency
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M4-005` — Đặt tour Post to Room — ghi nợ vào Folio phòng
*   **Severity:** HIGH
*   **Feature Under Test:** UC20.1
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M4-006` — Lập lịch chuyến tour — gán xe, tài xế, Tour Guide
*   **Severity:** MEDIUM
*   **Feature Under Test:** UC20.2
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M4-007` — Hủy tour — hoàn tiền theo chính sách hoặc đổi lịch
*   **Severity:** HIGH
*   **Feature Under Test:** UC20.3
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M4-008` — Gửi ảnh → AI Service trả match → Tour_Attendees cập nhật PRESENT
*   **Severity:** HIGH
*   **Feature Under Test:** UC21
*   **Test Type:** Integration
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M4-009` — AI Service không khả dụng → cho phép điểm danh thủ công
*   **Severity:** MEDIUM
*   **Feature Under Test:** UC21
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M4-010` — Khách gửi đánh giá 1-5 sao + nội dung text
*   **Severity:** LOW
*   **Feature Under Test:** UC22
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M4-011` — Chỉ khách đã sử dụng dịch vụ mới được đánh giá
*   **Severity:** MEDIUM
*   **Feature Under Test:** UC22
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.

#### `TC-M4-012` — Admin ẩn/hiện đánh giá toxic/spam
*   **Severity:** LOW
*   **Feature Under Test:** UC23
*   **Test Type:** Unit
*   **Test Steps:** TBD dựa trên Spec kỹ thuật.
*   **Expected Result (PASS):** Pass 100% assertions.


### 5. Red-Green-Refactor Tracker

| TC ID | Mô tả ngắn | Test File | 🔴 RED | 🟢 GREEN (commit) | 🔵 REFACTOR note |
| --- | --- | --- | --- | --- | --- |
| TC-M4-001 | Tìm kiếm tour — trả danh sách tour khả dụng + thông tin thời tiết | `TourServiceUC19Test.java` | [x] | [x] | ✅ Extract available tours mapping, use Java Streams |
| TC-M4-002 | API thời tiết không phản hồi → vẫn trả tour, ẩn thông tin thời tiết | `TourServiceUC19Test.java` | [x] | [x] | ✅ Extract weather API exception fallback handler |
| TC-M4-003 | Đặt tour thành công — tạo bản ghi Tour_Attendees | `TourBookingServiceUC20Test.java` | [x] | [x] | ✅ Extract attendee creation details verification |
| TC-M4-004 | Tour hết slot — đặt thêm bị chặn, trả TOUR-001 | `TourBookingServiceUC20Test.java` | [x] | [x] | ✅ Optimize concurrent slots check query |
| TC-M4-005 | Đặt tour Post to Room — ghi nợ vào Folio phòng | `TourBookingTddServiceUC20Test.java` | [x] | [x] | ✅ Standardize post to room billing strategy checks |
| TC-M4-006 | Lập lịch chuyến tour — gán xe, tài xế, Tour Guide | `TourBookingTddServiceUC20Test.java` | [x] | [x] | ✅ Extract guide & vehicle scheduling algorithm |
| TC-M4-007 | Hủy tour — hoàn tiền theo chính sách hoặc đổi lịch | `TourBookingTddServiceUC20Test.java` | [x] | [x] | ✅ Extract cancellation policy calculator |
| TC-M4-008 | Gửi ảnh → AI Service trả match → Tour_Attendees cập nhật PRESENT | `TourAttendanceServiceUC21Test.java` | [x] | [x] | ✅ Tách hàm getAttendeeById, thêm JavaDoc |
| TC-M4-009 | AI Service không khả dụng → cho phép điểm danh thủ công | `TourAttendanceServiceUC21Test.java` | [x] | [x] | ✅ Extract magic number, refactor Controller |
| TC-M4-010 | Khách gửi đánh giá 1-5 sao + nội dung text | `ReviewServiceUC22Test.java` | [x] | [x] | ✅ Tách hàm validate, thêm JavaDoc API |
| TC-M4-011 | Chỉ khách đã sử dụng dịch vụ mới được đánh giá | `ReviewServiceUC22Test.java` | [x] | [x] | ✅ Extract magic numbers, clean variables |
| TC-M4-012 | Admin ẩn/hiện đánh giá toxic/spam | `ReviewServiceUC22Test.java` | [x] | [x] | ✅ Tách hàm validateModerationReason, getReviewById, getAdminById; bổ sung JavaDoc |

### 6. Entry / Exit Criteria
- [x] Mock server hoạt động chuẩn xác.

---

### 7. Rollback Plan
Tắt cờ AI Face Scan nếu xảy ra memory leak.
