# ADR-07: Thiết kế Công cụ quy trình động (Dynamic Workflow Engine Design)

* **Trạng thái:** Confirmed
* **Tác giả:** Nhóm phát triển Kawai Retreat
* **Ngày quyết định:** 2026-07-01
* **Lĩnh vực:** Kiến trúc Phần mềm, Workflow Engine, Phê duyệt Động

---

## 1. Bối cảnh (Context)

Khu nghỉ dưỡng Kawai Retreat vận hành với nhiều quy tắc kinh doanh thay đổi linh hoạt. Một trong những bài toán phức tạp nhất là kiểm soát và phê duyệt các ngoại lệ nghiệp vụ:
* **Khuyến mãi vượt hạn mức (Promotion Exceeded):** Khách đặt phòng áp dụng voucher giảm giá lớn hơn tỷ lệ quy định của hệ thống (ví dụ: giảm >30% tổng hóa đơn). Hệ thống không được phép tự động chặn đơn hàng mà phải chuyển trạng thái đặt phòng thành `Pending_Approval` và chuyển tiếp yêu cầu phê duyệt tới Manager.
* **Yêu cầu hoàn tiền thủ công (Refund Request):** Mọi giao dịch hoàn tiền trực tiếp bằng tiền mặt hoặc chuyển khoản ngân hàng do Lễ tân đề xuất phải qua bước xét duyệt của Manager trước khi thủ quỹ thực hiện chi trả.

Nếu viết cứng (hardcode) các luồng phê duyệt này bằng các câu lệnh `if-else` trong tầng Service, hệ thống sẽ rất khó mở rộng và bảo trì khi ban quản lý thay đổi chính sách kinh doanh (ví dụ: nâng hạn mức tự động duyệt từ 30% lên 40%). Chúng tôi cần một công cụ xử lý quy trình động (Workflow Engine) cấu hình được bằng cấu trúc dữ liệu mà không cần chỉnh sửa mã nguồn.

## 2. Quyết định (Decision)

Chúng tôi quyết định thiết kế và xây dựng một **Workflow Engine động nội bộ** tích hợp sẵn trong Spring Boot:

### 2.1 Cấu hình Quy trình dựa trên JSON (Data-Driven Workflow Config)
* Quy trình được lưu trữ trong bảng `workflows` gồm hai cột dữ liệu cấu trúc chính:
  * `conditions_json`: Định nghĩa các điều kiện kích hoạt workflow dưới dạng JSONPath hoặc Spring Expression Language (SpEL).
  * `actions_json`: Định nghĩa các hành động hệ thống cần thực hiện khi điều kiện thỏa mãn.
* *Ví dụ cấu hình JSON điều kiện vượt hạn mức khuyến mãi:*
  ```json
  {
    "trigger_event": "BOOKING_CREATED",
    "rules": [
      {
        "field": "discount_percentage",
        "operator": "GREATER_THAN",
        "value": 30.0
      }
    ]
  }
  ```

### 2.2 Động cơ Thực thi (Execution Engine) sử dụng SpEL & Spring Events
* Triển khai cơ chế lập trình hướng khía cạnh (AOP) và Spring Application Events. Khi một Booking được tạo, hệ thống sẽ phát đi một `BookingCreatedEvent`.
* `WorkflowEngineService` lắng nghe event này, lấy toàn bộ danh sách workflow đang hoạt động (`is_active = true`), sử dụng **Spring Expression Language (SpEL)** để phân tích (evaluate) dữ liệu của booking dựa trên `conditions_json`.
* Nếu điều kiện khớp:
  1. Trạng thái của Booking bị chặn và chuyển sang `Pending_Approval`.
  2. Tạo một thực thể `WorkflowInstance` để theo dõi tiến trình phê duyệt.
  3. Gửi thông báo WebSocket / Email tới tất cả các Manager có thẩm quyền phê duyệt.

### 2.3 Quản lý Vòng đời Trạng thái Workflow (State Machine)
Workflow Engine kiểm soát chặt chẽ trạng thái của `WorkflowInstance` thông qua mô hình State Machine:
```
  [CREATED] ──> [RUNNING] ──> [PENDING_APPROVAL] ──┬──> [APPROVED] ──> [COMPLETED]
                                                   └──> [REJECTED] ──> [CANCELLED]
```
* **APPROVED:** Khi Manager nhấn Approve, Workflow Engine kích hoạt các hành động trong `actions_json` (ví dụ: chuyển Booking sang `Confirmed`, gửi email xác nhận cho khách, ghi nhận vào Audit Log).
* **REJECTED:** Nếu bị từ chối, Booking chuyển sang `Cancelled`, giải phóng phòng đang giữ tạm thời.

## 3. Hệ quả (Consequences)

### Tích cực (Pros)
* **Tính linh hoạt nghiệp vụ cao:** Admin/Quản lý có thể cấu hình lại các hạn mức duyệt, thêm điều kiện mới hoặc thay đổi người phê duyệt bằng cách chỉnh sửa JSON trong bảng `workflows` mà không cần code lại Java hay deploy lại server.
* **Bảo vệ tài chính khách sạn:** Ngăn chặn tuyệt đối các sai sót hoặc hành vi gian lận voucher từ phía nhân viên hoặc khách hàng nhờ cơ chế phê duyệt chéo.
* **Truy vết đầy đủ:** Vòng đời của từng yêu cầu phê duyệt được ghi nhận chi tiết (ai duyệt, duyệt lúc nào, lý do là gì) trong lịch sử workflow phục vụ công tác thanh tra tài chính.

### Hạn chế (Cons)
* **Yêu cầu kỹ thuật khi cấu hình:** Người cấu hình (System Admin) cần hiểu rõ cú pháp JSON và các trường dữ liệu hệ thống để viết điều kiện chính xác. Cú pháp sai có thể dẫn đến lỗi Runtime khi đánh giá biểu thức SpEL.
  * *Biện pháp:* Xây dựng một module UI quản lý Workflow trên trang Admin có tính năng kiểm tra lỗi cú pháp (Syntax Validation) và chạy thử nghiệm (Dry Run) trước khi lưu cấu hình chính thức.
* **Độ trễ xử lý sự kiện:** Việc đánh giá động qua SpEL và lưu vết state machine làm tăng nhẹ thời gian phản hồi khi đặt phòng. Tuy nhiên, do luồng chạy phê duyệt là bất đồng bộ (Asynchronous) đối với người dùng cuối, trải nghiệm của khách hàng không bị ảnh hưởng trực tiếp.
