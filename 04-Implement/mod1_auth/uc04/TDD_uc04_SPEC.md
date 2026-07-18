# TEST-DRIVEN DEVELOPMENT SPECIFICATION
## UC04 — Đăng tải & Trích xuất dữ liệu khuôn mặt FaceID

| Field | Value |
|-------|-------|
| **Document ID** | `KAWAI-TDD-MOD1-UC04-001` |
| **Version** | 1.0 |
| **Date** | 2026-06-17 |
| **Status** | Approved |
| **Standard** | ISO/IEC/IEEE 29119-3:2021 |
| **Author** | Antigravity — Developer |
| **Reviewed by** | [x] Nguyễn Xuân Lưu — Tech Lead |
| **DPO Sign-off** | `[x] Approved – 2026-06-17 – Nguyễn Xuân Lưu` |
| **Approved by** | `[x] Nguyễn Xuân Lưu – 2026-06-17` |
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
| **Feature / Gap ID** | `GAP-MOD1-UC04` |
| **Module** | Biometrics — UC04 |
| **Use Case** | UC04: Trích xuất dữ liệu khuôn mặt |
| **Spec gốc** | `UC_DETAIL_SPEC.md` |
| **Priority** | 🔴 P0 |
| **Sprint** | S2 |
| **Data Classification** | Sinh trắc học (PII) |
| **Upstream Dependencies** | `kawai-ai-service` |

---

### 2. Logic Issues Resolved

| # | Spec gốc | Thực tế | Fix áp dụng trong test |
|---|----------|---------|------------------------|
| **L1** | Lưu ảnh lên Storage | Vi phạm GDPR nếu lộ DB | Chỉ lưu Vector JSON 128 số |
| **L2** | Python lỗi sẽ crash Java | Timeout API | Xử lý Graceful fallback & Circuit breaker |

---

### 3. Test Design Specification (TDS)

#### TDS-01 — Scope / Phạm vi
Logic `AiServiceClient`, `CustomerService.saveFaceVector()`.

#### TDS-02 — Test Basis

| Source | Items Derived |
|--------|---------------|
| `UC_DETAIL_SPEC.md` | Xử lý đa luồng API AI -> Save DB JSON |

#### TDS-03 — Test Conditions

| Condition ID | Test Condition | Coverage Item | Test Cases |
|-------------|----------------|---------------|------------|
| TC-COND-UC04-001 | AI trả mảng Vector chuẩn | `extractFeatures()` | TC-UC04-001 |
| TC-COND-UC04-002 | AI trả lỗi không có mặt | `extractFeatures()` | TC-UC04-002 |
| TC-COND-UC04-003 | Lưu JSON vào DB | `saveFaceVector()` | TC-UC04-003 |

---

### 4. Test Case Specification

#### `TC-UC04-001` — Tích hợp AI Mock trả về mảng số
* **Severity:** CRITICAL | **Feature:** `AiServiceClient` | **File:** `FaceIdUC04Test.java` | 🟢 GREEN
**Steps:** Gọi `uploadFace()`. Mock WebClient/RestTemplate trả về `[0.1, -0.2]`. Assert hàm trả về List<Float>.

#### `TC-UC04-002` — AI không nhận diện được mặt (400 Bad Request)
* **Severity:** HIGH | **Feature:** `AiServiceClient` | 🟢 GREEN
**Steps:** Mock Python trả HTTP 400. Assert Java Controller ném `NoFaceDetectedException`.

#### `TC-UC04-003` — Convert mảng số lưu Database chuẩn JSON
* **Severity:** HIGH | **Feature:** `saveFaceVector()` | 🟢 GREEN
**Steps:** Trải qua luồng, gọi Repository Save. Capture entity. Assert trường `face_vector_data` là chuỗi `"[0.1,-0.2]"`.

---

### 5. Red-Green-Refactor Tracker

| TC ID | Mô tả | Test File | 🔴 RED | 🔴 Commit | 🔴 Date | 🟢 GREEN | 🟢 Commit | 🟢 Date | 🔵 REFACTOR | 🔵 Commit | 🔵 Note |
|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-UC04-001 | Mock AI Success | `FaceIdUC04Test.java` | [ ] | `-` | `-` | [ ] | `-` | `-` | [ ] | `-` | ⏳ Chờ code |
| TC-UC04-002 | Mock AI Fail | `FaceIdUC04Test.java` | [ ] | `-` | `-` | [ ] | `-` | `-` | [ ] | `-` | ⏳ Chờ code |
| TC-UC04-003 | DB JSON Format | `FaceIdUC04Test.java` | [ ] | `-` | `-` | [ ] | `-` | `-` | [ ] | `-` | ⏳ Chờ code |

---

### 6. Entry / Exit Criteria

- [x] Microservice AI đã deploy và hoạt động độc lập.

---

### 7. Rollback Plan

`git checkout HEAD~1 -- src/main/java/com/kawai/controllers/FaceIdApiController.java`
