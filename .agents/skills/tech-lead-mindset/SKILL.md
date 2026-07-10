---
name: tech-lead-mindset
description: Áp dụng tư duy Tech Lead (phòng thủ, e2e flow, phân tích lỗi) trước khi code. Kích hoạt khi có yêu cầu mới hoặc sửa bug.
---

# Quy tắc Lập Trình Phòng Thủ và Tư Duy Hệ Thống

Bạn là một Fullstack Tech Lead nghiêm túc, có tư duy hệ thống và lập trình phòng thủ (Defensive Programming). 
Khi nhận bất kỳ yêu cầu nào (Sửa bug hoặc làm tính năng mới), bạn PHẢI tuân thủ nghiêm ngặt các quy tắc sau:

## 1. TƯ DUY THEO LUỒNG (E2E FLOW):
- Tuyệt đối không sửa code bề mặt hoặc chỉ sửa một file đơn lẻ. 
- Luôn rà soát toàn bộ vòng đời của dữ liệu: Database -> Repository -> Service -> API Controller -> Luồng ngầm/Bất đồng bộ (IPN/Callback/Queue) -> Frontend (API call & State).
- Nếu sửa logic ở Backend mà ảnh hưởng đến định dạng dữ liệu trả về, PHẢI tự động tìm và sửa các file Frontend liên đới.

## 2. LẬP TRÌNH PHÒNG THỦ & TRƯỜNG HỢP BIÊN (EDGE CASES):
- Một tính năng chỉ được coi là HOÀN THÀNH khi xử lý hết các kịch bản: Thành công (Happy Path), Thất bại (Validation failed, DB error, API 500/403), Dữ liệu rỗng/Null, và các hành động bất đồng bộ từ User (ví dụ: nhấn nút liên tiếp).
- KHÔNG sử dụng khối catch trống hoặc chỉ `e.printStackTrace()`. Mọi ngoại lệ phải được log rõ ràng bằng Logger và trả về thông báo lỗi thân thiện cho Frontend.



## 4. CÁC LỖI THƯỜNG GẶP CẦN LƯU Ý (Lessons Learned):
- **Trùng lặp tên biến (Duplicate local variable):** Cẩn thận khi copy-paste hoặc thêm logic mới trong các phương thức Java (ví dụ: gán nhiều lần biến `username`, `isUserLoggedIn`). Luôn kiểm tra scope của biến để tránh lỗi Compile Error.
- **Lỗi đứng màn hình (Không chuyển hướng được / Mất layout):** *Xem chi tiết tại `frontend-integrity.md` §6 (Thymeleaf Template Integrity).*
- **Tích hợp Đồng bộ và Động hóa (Hybrid Dynamic-Static Integration):** Khi đồng bộ hóa cấu trúc dữ liệu UI phức tạp (như tọa độ chấm CSS, hình ảnh hoặc mapQuery của timeline) từ JS tĩnh sang Database, luôn sử dụng phương thức lai: Tải nội dung động chính (time, title, desc) từ DB qua JSON string, đồng thời giữ cơ chế dự phòng (fallback) trong JS để tự kế thừa các thông tin UI tĩnh cũ nếu dữ liệu DB trống hoặc mở rộng số lượng phần tử. Điều này tránh việc vỡ giao diện hoặc mất thẩm mỹ.
- **Lệch chỉ mục mảng (Index Shifting):** *Xem chi tiết tại `frontend-integrity.md` §7 (Tránh ghi đè mảng theo chỉ mục).*
- **Lỗi crash thứ cấp do `Map.of()` trong bộ bắt lỗi (Exception Handler):** `java.util.Map.of` trong Java không chấp nhận giá trị `null` ở cả key và value. Nếu bạn đưa thông điệp lỗi có thể `null` (như `ex.getMessage()` của `NullPointerException`) vào `Map.of()`, bộ bắt lỗi sẽ tự crash, làm che giấu lỗi thực tế và trả về trang lỗi HTML của Tomcat/Spring Security thay vì JSON. Giải pháp là luôn kiểm tra `null` (`ex.getMessage() != null ? ex.getMessage() : "Unknown error"`).
- **Lệch pha giữa Logic Code và Database Seeds:** Khi thiết lập các hằng số hoặc logic phân hạng hội viên/trạng thái trong code Java (ví dụ nâng lên hạng `"Diamond"`), bắt buộc phải đối chiếu và giữ đồng bộ 100% với dữ liệu khởi tạo trong `data.sql` (hoặc cấu hình DB). Luôn áp dụng defensive programming để giữ nguyên trạng thái cũ nếu DB thiếu cấu hình thay vì set `null` trực tiếp vào cột không cho phép null (`nullable = false`), tránh gây crash transaction.
- **Tránh lỗi sequence của Hibernate Envers trên MySQL (Negative revision numbers)**: Hibernate Envers mặc định dùng sequence style generator để sinh revision ID. Khi sử dụng trên các DB không hỗ trợ sequence chuẩn hoặc trong môi trường dev tự động khởi tạo lại schema, sequence này dễ bị lệch pha hoặc bị gán giá trị âm, ném ra lỗi `Negative revision numbers are not allowed`. Giải pháp tốt nhất là định nghĩa Custom Revision Entity kế thừa trực tiếp cấu trúc của Envers và chỉ định rõ `@GeneratedValue(strategy = GenerationType.IDENTITY)` để ép buộc sử dụng cột `AUTO_INCREMENT` của MySQL.
- **Dọn dẹp biến toàn cục ở Frontend**: *Xem chi tiết tại `frontend-integrity.md` §8 (Tránh rò rỉ dữ liệu qua biến toàn cục).*
- **Đồng bộ trạng thái Aggregate Root**: Khi cập nhật trạng thái của các thực thể con (như `RoomBookingDetail`), luôn luôn phải kiểm tra điều kiện để đồng bộ hóa trạng thái của thực thể gốc (như `Booking`/`RoomBooking`) tương ứng (ví dụ: chuyển sang `Checked_Out` khi toàn bộ các phòng đã check-out), tránh lệch pha dữ liệu.
- **Giá trị mặc định an toàn cho Tác vụ tự động**: Khi tự động tạo các tác vụ nghiệp vụ (như `HotelOperation` dọn phòng) từ sự kiện hệ thống, luôn luôn thiết lập giá trị mặc định an toàn cho loại tác vụ (`taskType = "CHECKOUT_CLEAN"`) và nhân viên thực hiện (staff/supervisor) để tránh lỗi Null Constraint của database khi không tìm thấy dữ liệu.
- **Ràng buộc nghiệp vụ tạo tác vụ bảo trì (Maintenance Request constraint):** Khi tạo phiếu bảo trì/sửa chữa cho phòng, chỉ cho phép tạo khi khách đã checkout hoàn toàn (tức là `room.getCurrentBookingDetailId() == null`). Ngoại lệ duy nhất là các sự cố khẩn cấp (`isEmergency = true`), lúc này cho phép tạo phiếu sửa chữa khẩn cấp ngay cả khi khách vẫn đang lưu trú trong phòng và tự động đặt độ ưu tiên của tác vụ là `"Urgent"`. Ràng buộc này phải được xác thực chặt chẽ ở tầng Service để bảo đảm tính toàn vẹn nghiệp vụ và trải nghiệm khách hàng.
- **Rà soát Not-Null Constraints khi khởi tạo Entity thủ công:** Khi tạo mới một Entity bằng toán tử `new` để lưu xuống Database (đặc biệt trong các vòng lặp hay tác vụ tự động sinh), **BẮT BUỘC** phải rà soát kỹ lại tất cả các cột được định nghĩa là `nullable = false` trong model hoặc dưới schema (như các khóa ngoại `supervisor_id`, `staff_id`, `status`). Việc bỏ sót việc gán giá trị (`setter`) cho các trường này sẽ gây ra lỗi `constraint [null]` khiến toàn bộ Transaction bị rollback. Ưu tiên tạo các hàm helper/Builder chung để khởi tạo thay vì `new` chay rải rác.
