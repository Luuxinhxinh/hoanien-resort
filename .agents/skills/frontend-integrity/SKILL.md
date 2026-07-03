---
name: frontend-integrity
description: Đảm bảo tính toàn vẹn của Frontend (Giao diện và Tương tác), kiểm tra kỹ class CSS, sự kiện JS, và payload khi thêm hoặc sửa đổi UI. Kích hoạt khi có thay đổi liên quan đến frontend (HTML, JS, CSS, Modals, Forms).
---

# Frontend Integrity Guidelines

## 1. CSS & Styling Checks
- Khi tạo mới một component (Modal, Button, Card...), **TUYỆT ĐỐI** không tự bịa ra class CSS. Phải `grep_search` hoặc đọc file HTML hiện tại để tái sử dụng các class có sẵn (ví dụ: `split-modal-overlay` thay vì `modal-overlay`).
- Kiểm tra trạng thái hiển thị ban đầu. Đối với Modal/Popup, phải có `style="display: none;"` nếu không sử dụng CSS class để ẩn mặc định.
- Luôn kiểm tra layout xem có gây vỡ giao diện trên các thiết bị hoặc gây xung đột với các elements lân cận không.

## 2. JavaScript & Interaction Checks
- Kiểm tra tên hàm (Function Name) khi gọi từ `onclick=""` hoặc Event Listener phải khớp chính xác với định nghĩa trong thẻ `<script>`.
- Đảm bảo các biến sử dụng trong script (ví dụ: `currentBookingId`, `currentPaymentRoomId`) đã được định nghĩa trong Global Scope hoặc được truyền vào đúng cách. TUYỆT ĐỐI KHÔNG tự định nghĩa hoặc suy đoán các biến toàn cục không tồn tại trong mã nguồn hiện có.
- Khi tạo API Request gửi đi, bắt buộc phải kiểm tra trong file HTML/JS hiện có xem ID hay dữ liệu thực tế được quản lý dưới tên biến toàn cục nào (như `currentRoomBookingDetailId` thay vì tự đặt tên `currentBookingId`).
- Ràng buộc dữ liệu (Validation): Luôn kiểm tra đầu vào (Null, Empty, khoảng trắng) trước khi gửi Request. Có thông báo lỗi rõ ràng.
- Kiểm tra sự tồn tại của thư viện ngoài: Khi sử dụng các thư viện như SweetAlert2 (`Swal`), Toastr, Bootstrap... TRƯỚC HẾT phải kiểm tra xem file HTML hoặc file layout dùng chung có thực sự import thư viện đó chưa. Nếu chưa, hãy dùng native browser API (ví dụ `alert()`, `confirm()`) hoặc tự viết giao diện đơn giản thay vì giả định thư viện đã có sẵn.

## 3. Data & API Payload Integrity
- Khi Fetch API, kiểm tra phương thức (GET, POST, PUT, DELETE) có đúng với Backend không.
- Đối chiếu các Key trong JSON payload gửi đi có khớp 100% với DTO hoặc `Map<String, Object>` mà Backend Controller kỳ vọng hay không.
- Đối chiếu các giá trị (Enum values, Type strings) được định nghĩa ở thẻ `<select>` hoặc `<input>` của Frontend xem Backend có thực sự hỗ trợ xử lý và truy vấn giá trị đó hay không (ví dụ: `Manager_Approval` thay vì `Discount_Approval` để tránh trường hợp lưu thành công nhưng không hiển thị trên Dashboard).
- Xử lý các trạng thái Promise (then/catch) và hiển thị UI thông báo tương ứng (Loading, Success, Error).

## 4. Kiểm tra chéo (Cross-check) trước khi báo cáo hoàn thành
- Tôi đã kiểm tra class CSS của element chưa?
- Element có hiển thị hoặc ẩn đúng thời điểm không?
- Payload dữ liệu có bị null hay sai kiểu không?
- Đã check Console Log / Swal alert nếu API trả về lỗi chưa?

## 5. End-to-End Flow & Data Consistency
- Đảm bảo bất kỳ luồng chức năng nào cũng phải thông suốt từ lúc bắt đầu (giao diện gửi yêu cầu) đến lúc kết thúc (nơi nhận/xem yêu cầu hiển thị và cập nhật trạng thái).
- Khi thêm hoặc chỉnh sửa trạng thái/dữ liệu ở một màn hình (ví dụ: gửi Yêu cầu Phê duyệt từ Lễ tân):
  - Kiểm tra xem các màn hình liên quan của người dùng khác (ví dụ: trang Duyệt yêu cầu của Manager) có hiển thị đúng, đủ các thông tin và trạng thái tương ứng hay không.
  - Đảm bảo không bị lệch kiểu dữ liệu hoặc giá trị nhãn (như enum, type string) khiến dữ liệu bị bỏ sót khi truy vấn lọc ở màn hình đích.
  - Khi thực thi các hành động phê duyệt/từ chối, các thay đổi trạng thái phải được cập nhật đồng bộ và chính xác xuống database cũng như các thực thể liên quan (ví dụ: trạng thái đơn đặt phòng Booking chuyển sang `Pending_Approval` khi gửi yêu cầu, và trả lại `Pending`/`CANCELLED` sau khi Manager xử lý).
