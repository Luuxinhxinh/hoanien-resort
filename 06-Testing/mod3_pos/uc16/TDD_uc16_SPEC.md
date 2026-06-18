# TEST-DRIVEN DEVELOPMENT SPECIFICATION

## UC16 (File) — Gọi món Dine-In tại quầy (POS Nhân viên lên đơn tại bàn)

*(Theo cấu trúc thư mục dự án: folder uc16/ chứa nội dung Dine-In POS)*

| Field                    | Value                                               |
| ------------------------ | --------------------------------------------------- |
| **Document ID**    | `KAWAI-TDD-MOD3-UC16-001`                         |
| **Version**        | 2.0                                                 |
| **Date**           | 2026-06-17                                          |
| **Status**         | Approved                                            |
| **Standard**       | ISO/IEC/IEEE 29119-3:2021                           |
| **Author**         | Trịnh Minh Đức — Developer                      |
| **Reviewed by**    | `Nguyễn Xuân Lưu — Tech Lead`                 |
| **DPO Sign-off**   | `[x] Approved – 2026-06-17 – Trịnh Minh Đức` |
| **Approved by**    | `[x] Trịnh Minh Đức – 2026-06-17`             |
| **Classification** | Internal — Confidential                            |

---

### CHANGELOG

| Ngày      | Người thực hiện | Nội dung thay đổi                                                                                                                           |
| ---------- | ------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------- |
| 2026-06-17 | Trịnh Minh Đức   | **v2.0** — Cập nhật mã TC chuẩn (TC-M3-005/006), test file thực tế `PosApiControllerUC17Test.java`, Retroactive TDD hoàn tất. |
| 2026-06-15 | Trịnh Minh Đức   | v1.0 — Khởi tạo tài liệu                                                                                                                  |

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

| Field                      | Value                                                                                                 |
| -------------------------- | ----------------------------------------------------------------------------------------------------- |
| **Feature / Gap ID** | `GAP-MOD3-UC16`                                                                                     |
| **Module**           | POS Dine-In — UC16 (file)                                                                            |
| **Use Case**         | Thu ngân lên đơn gọi món Dine-In tại bàn, ghi nhận bàn ăn, giá snapshot, ghi chú khách. |
| **Spec gốc**        | `SRS_Document_SWP391_G2.md`                                                                         |
| **Priority**         | 🔴 P0                                                                                                 |
| **Sprint**           | S1 (2026-06-09 → 2026-06-23)                                                                         |
| **Milestone**        | M3 Alpha — 2026-07-11                                                                                |

---

### 2. Logic Issues Resolved

| #            | Spec gốc                                           | Thực tế                                                     | Fix áp dụng trong test                       |
| ------------ | --------------------------------------------------- | ------------------------------------------------------------- | ---------------------------------------------- |
| **L1** | Giá MenuItem có thể thay đổi sau khi gọi món | Snapshot giá vào `priceAtOrder` lúc lưu FoodOrderDetail | Test assert `priceAtOrder` == giá lúc gọi |
| **L2** | Ghi chú khách hàng cần format chuẩn            | Prefix `GUEST:<tên>                                          | ` trước nội dung ghi chú                   |

---

### 3. Test Design Specification (TDS)

#### TDS-01 — Scope / Phạm vi

`PosApiController.createOrder()` với `orderType != "room-svc"` và `PosApiController.payOrder()`.

#### TDS-02 — Test Basis

`SRS.md` UC17 (Dine-In Cashier), `PosApiController.java`

#### TDS-03 — Test Conditions

| Condition ID    | Test Condition                                                                                     | Coverage Item        |
| --------------- | -------------------------------------------------------------------------------------------------- | -------------------- |
| TC-COND-M3-005  | Thu ngân lên đơn Dine-In thành công → HTTP 200, bàn gán đúng, FoodOrderDetail lưu đủ | `PosApiController` |
| TC-COND-M3-006  | Thanh toán ngay tại POS →`orderStatus = PAID`, `isPaidInPos = true`                         | `PosApiController` |
| TC-COND-M3-006b | `priceAtOrder` là snapshot giá lúc gọi, `kotStatus = "Pending"`                            | `FoodOrderDetail`  |
| TC-COND-M3-006c | Tên khách và ghi chú đặc biệt được lưu đúng vào `note`                             | `FoodOrder.note`   |

---

### 4. Test Case Specification

#### `TC-M3-005` — Tạo order Dine-In thành công

* **Severity:** CRITICAL | **Feature:** `PosApiController` | 🟢 GREEN
* **Pre-condition:** Bàn A5 (ID=5) tồn tại, MenuItem 10 và 11 tồn tại.
* **Steps:**
  1. Mock `restaurantTableRepository.findById(5)` → trả về Table A5.
  2. Mock `foodItemRepository.findById(10/11)` → trả về MenuItem.
  3. Mock `foodOrderRepository.save()` → gán `id = 77`.
  4. Gọi `posApiController.createOrder(request, principal)`.
* **Expected:** HTTP 200, `status = "success"`, `orderId = 77`, `foodOrderDetailRepository.save()` được gọi 2 lần, `roomBookingRepository.save()` KHÔNG được gọi.

#### `TC-M3-006` — Thanh toán ngay tại POS → PAID

* **Severity:** HIGH | **Feature:** `PosApiController` | 🟢 GREEN
* **Steps:**
  1. Set `request.setIsPaid(true)`.
  2. Gọi `createOrder()`.
* **Expected:** `orderStatus = "PAID"`, `isPaidInPos = true`.

#### `TC-M3-006b` — Giá snapshot (priceAtOrder) đúng

* **Severity:** HIGH | **Feature:** `FoodOrderDetail` | 🟢 GREEN
* **Expected:** `priceAtOrder = 120,000`, `quantity = 2`, `kotStatus = "Pending"`.

#### `TC-M3-006c` — Ghi chú + tên khách lưu đúng

* **Severity:** MEDIUM | **Feature:** `FoodOrder.note` | 🟢 GREEN
* **Expected:** `note` chứa `"Nguyễn Văn A"` và `"dị ứng hải sản"`.

---

### 5. Red-Green-Refactor Tracker

| TC ID      | Mô tả                                                                                | Test File                         | 🔴 RED | 🔴 Commit   | 🔴 Date    | 🟢 GREEN | 🟢 Commit   | 🟢 Date    | 🔵 REFACTOR | 🔵 Commit   | 🔵 Note               |
| ---------- | -------------------------------------------------------------------------------------- | --------------------------------- | ------ | ----------- | ---------- | -------- | ----------- | ---------- | ----------- | ----------- | --------------------- |
| TC-M3-005  | Thu ngân lên đơn Dine-In thành công — ghi đúng bàn, đúng món, đúng giá | `PosApiControllerUC17Test.java` | [x]    | `PENDING` | 2026-06-17 | [x]      | `PENDING` | 2026-06-17 | [x]         | `PENDING` | ✅ Retroactive TDD    |
| TC-M3-006  | Thanh toán ngay tại POS → orderStatus = PAID                                        | `PosApiControllerUC17Test.java` | [x]    | `PENDING` | 2026-06-17 | [x]      | `PENDING` | 2026-06-17 | [x]         | `PENDING` | ✅ isPaidInPos = true |
| TC-M3-006b | priceAtOrder là snapshot giá lúc gọi món                                          | `PosApiControllerUC17Test.java` | [x]    | `PENDING` | 2026-06-17 | [x]      | `PENDING` | 2026-06-17 | [x]         | `PENDING` | ✅ Verify snapshot    |
| TC-M3-006c | Ghi chú + tên khách lưu đúng vào note                                           | `PosApiControllerUC17Test.java` | [x]    | `PENDING` | 2026-06-17 | [x]      | `PENDING` | 2026-06-17 | [x]         | `PENDING` | ✅ Note format        |

---

### 6. Entry / Exit Criteria

#### Entry Criteria

- [X] Code base setup hoàn chỉnh
- [X] Bảng `food_orders`, `food_order_details`, `restaurant_tables` tồn tại

#### Exit Criteria

- [X] 4/4 Unit tests pass 100% (BUILD SUCCESS)
- [X] Tài liệu TDD/EDS cập nhật đầy đủ

---

### 7. Rollback Plan

`git checkout -- src/main/java/com/kawai/controllers/api/PosApiController.java`
