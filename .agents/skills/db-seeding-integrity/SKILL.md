---
name: db-seeding-integrity
description: Đảm bảo tính toàn vẹn nghiệp vụ và nhất quán dữ liệu khi thêm, chỉnh sửa hoặc nạp dữ liệu khởi tạo (seeding data) vào cơ sở dữ liệu.
---

# Quy Tắc Đảm Bảo Tính Nhất Quán Dữ Liệu Khởi Tạo (Seeding Data Integrity)

Mỗi khi bạn thực hiện thêm mới, chỉnh sửa hoặc tối ưu hóa các tệp dữ liệu khởi tạo của dự án (như dữ liệu SQL nạp ban đầu khi khởi động), bạn **bắt buộc** phải tuân thủ các quy tắc nghiệp vụ sau để tránh tình trạng dữ liệu "lệch pha" hoặc xung đột logic:

## 1. NGUYÊN TẮC TOÀN VẸN NGHIỆP VỤ (BUSINESS LOGIC INTEGRITY):
- **Luồng phê duyệt (Approval Workflows):** Nếu thêm một thực thể ở trạng thái chờ duyệt (ví dụ: `Pending_Approval`), bắt buộc phải có bản ghi tương ứng trong bảng theo dõi vận hành/phê duyệt của người quản lý (ví dụ: `Hotel_Operations` với loại `Manager_Approval`) ở trạng thái chờ xử lý (`Pending`).
- **Luồng hoàn tiền (Refund Workflows):** Nếu thêm một yêu cầu hoàn tiền (`RefundRequest`), thực thể giao dịch hoặc đơn hàng gốc liên kết (ví dụ: `Booking`, `FoodOrder`, `TourBooking`) **phải được đặt ở trạng thái đã hủy (`Cancelled`)** để phản ánh đúng thực tế tài chính và nghiệp vụ.
- **Mã giảm giá (Promotions):** Số lần sử dụng thực tế phải khớp hoặc được kiểm soát hợp lý so với số lượng đơn hàng liên kết đã áp dụng mã đó.

## 2. NGUYÊN TẮC TOÀN VẸN DỮ LIỆU VẬT LÝ (REFERENTIAL INTEGRITY):
- Đảm bảo tất cả các khóa ngoại (Foreign Keys) như `customer_id`, `room_id`, `employee_id`, `promotion_id`... trỏ đến các thực thể đã được khai báo và tồn tại trước đó trong tập dữ liệu.
- Tránh trùng lặp khóa chính (`PRIMARY KEY`) hoặc vi phạm các ràng buộc duy nhất (`UNIQUE CONSTRAINTS`).
- Đối với các phòng vật lý được chỉ định trong trạng thái chờ nhận phòng, hãy chắc chắn phòng đó ở trạng thái trống (`Vacant_Clean` hoặc tương tự) để tránh tranh chấp phòng với các khách đang ở thực tế.

## 3. QUY TRÌNH KIỂM TRA BẮT BUỘC:
- **Bước 1:** Đối chiếu chéo tất cả các bảng liên quan đến luồng nghiệp vụ chuẩn bị thêm dữ liệu.
- **Bước 2:** Cập nhật đồng bộ trạng thái của tất cả thực thể liên đới (ví dụ: chạy câu lệnh UPDATE trạng thái đơn hàng cũ khi thêm thực thể phạt cọc/hoàn tiền).
- **Bước 3:** Chạy biên dịch và khởi động thử dự án để kiểm tra lỗi cú pháp SQL hoặc xung đột khóa ngoại lúc nạp dữ liệu.
