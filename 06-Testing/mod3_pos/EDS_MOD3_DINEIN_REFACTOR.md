**ENGINEERING DOCUMENTATION STANDARD (EDS)** 

**v2.0** 

**Quy chuẩn Tài liệu Kỹ thuật và Đặc tả Hiện thực** 

**Field** | **Value**
---|---
**Document ID** | KAWAI - MOD3 - IMP - 003
**Version** | 1.0
**Date** | 2026-06-22
**Status** | Approved
**Document Owner** | F&B Squad
**Author** | AI Developer
**Reviewed by** | Tech Lead
**Approved by** | Principal Architect
**Last Review** | 2026-06-22

**CHANGELOG** 

| **Ngày** | **Người thực hiện** | **Nội dung thay đổi** |
|---|---|---|
| 2026-06-22 | AI Developer | Khởi tạo tài liệu và mô tả hoàn thiện State Machine cho nghiệp vụ Dine-In |

---

## 1. Tổng quan Module

| **Field** | **Value** |
|---|---|
| **Module Name** | Point of Sale (POS) - Dine In Order |
| **Bounded Context** | Food & Beverage (F&B) |
| **Data Classification** | Internal |
| **Compliance Scope** | Internal Auditing |
| **Upstream Dependencies** | Table Management |
| **Downstream Consumers** | Kitchen Display System (KDS), Night Audit |

---

## 2. Ma trận Truy vết (Traceability Matrix)

| **Requirement ID** | **Loại** | **Mô tả yêu cầu** | **Thành phần Code** | **Compliance Target** | **ADR liên quan** |
|---|---|---|---|---|---|
| BR-DNI-001 | Business Rule | Phải kiểm tra bàn rảnh trước khi tạo đơn Dine In | `PosServiceImpl.createOrder` | Tránh nhầm lẫn dữ liệu | ADR-003 |
| BR-DNI-002 | Business Rule | Chuyển trạng thái bàn thành Occupied khi có đơn | `PosServiceImpl.createOrder` | Realtime Tracking | ADR-003 |
| BR-DNI-003 | Business Rule | Giải phóng bàn (thành Cleaning) sau khi thanh toán xong | `PosServiceImpl.payOrder` | Turn-around time | ADR-003 |

---

## 3. Architecture Decision Records (ADR)

**ADR-003 — Liên kết Trạng thái Đơn hàng và Trạng thái Bàn (Dine-In State Machine)**

| **Field** | **Value** |
|---|---|
| **Status** | Accepted |
| **Deciders** | AI Developer, Tech Lead |
| **Date** | 2026-06-22 |

**Bối cảnh (Context)**
Trong thiết kế ban đầu, việc tạo đơn hàng Dine-In chỉ sinh ra bản ghi `FoodOrder` mà bỏ quên việc cập nhật trạng thái của `RestaurantTable`. Điều này dẫn đến lỗi nghiệp vụ: nhân viên khác vẫn thấy bàn đang trống (`Available`) và có thể tạo tiếp đơn hàng thứ 2 đè lên bàn đó, hoặc khách khứa có thể đặt bàn online vào đúng cái bàn đang có người ngồi.

**Các phương án đã xem xét (Options Considered)**

| **Phương án** | **Mô tả** | **Ưu điểm** | **Nhược điểm** |
|---|---|---|---|
| A | Yêu cầu nhân viên thao tác "Mở bàn" thủ công | Nhân viên bấm Open Table trước, rồi mới Add Order. | Linh hoạt, nhân viên có thể xí chỗ bàn trước khi khách gọi món. | Thêm bước thao tác thừa. Khả năng nhân viên quên mở bàn rất cao. |
| B | Cập nhật tự động (Auto-sync) khi tạo đơn | Gắn logic chuyển `TableStatus` trực tiếp vào hàm `PosServiceImpl.createOrder`. Tự giải phóng ở hàm `payOrder`. | Tự động hóa hoàn toàn, giảm thiểu sai sót con người. Tối ưu trải nghiệm POS. | Code bị ràng buộc chặt chẽ hơn giữa Order và Table. |

**Quyết định (Decision)**
Chọn **Phương án B**. Với một hệ thống POS hiện đại, mọi thao tác thừa thãi của con người cần được giảm thiểu.

**Hệ quả (Consequences)**
- **Tích cực**: Quản lý rủi ro tuyệt đối, tránh conflict 2 đơn hàng chung 1 bàn.
- **Tiêu cực**: Trong tương lai nếu có nghiệp vụ "Gộp bàn" (Merge Tables) hoặc "Chuyển bàn" (Transfer Table), ta sẽ phải viết thêm khá nhiều code để đổi `TableStatus` ngược lại.

---

## 4. Dynamic Modeling (Mô hình Động)

### 4.1. State Machine của Bàn (Table Status)
*(Chỉ xét trong ngữ cảnh tương tác với POS Dine-In)*

| Trạng thái hiện tại | Trigger (Hành động) | Điều kiện kiểm tra | Trạng thái tiếp theo |
|---|---|---|---|
| `AVAILABLE` / `VACANT` | Nhân viên tạo đơn Dine-In (`createOrder`) | Bàn chưa ai ngồi | `OCCUPIED` |
| `OCCUPIED` | Nhân viên thanh toán (`payOrder`) | Hóa đơn được duyệt trả đủ | `CLEANING` |
| `OCCUPIED` | Nhân viên tạo đơn Dine-In MỚI (`createOrder`) | - | *Throw Exception POS-007* (Bị chặn) |

---

## 5. API Specification & Error Codes

Mã lỗi bổ sung được thêm vào `PosServiceImpl`:

| **Code** | **Message (VI)** | **Trigger Condition** |
|---|---|---|
| POS-007 | Bàn hiện đang không trống! | Nhân viên cố tình truyền Table ID của một bàn đang `Occupied` hoặc `Cleaning` để mở Order mới. |
