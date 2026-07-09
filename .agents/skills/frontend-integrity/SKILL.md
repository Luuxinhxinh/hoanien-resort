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
- Thay vì sử dụng danh sách kiểm tra độc lập ở đây, **BẮT BUỘC** gộp các tiêu chí kiểm tra (CSS hiển thị, biến lặp, payload null) vào mục **Kiểm tra toàn diện (Definition of Done)** tại file `context-first-quality.md` §4. Việc rà soát cuối cùng sẽ được thực hiện tại một nơi duy nhất.

## 5. End-to-End Flow & Data Consistency
- Đảm bảo bất kỳ luồng chức năng nào cũng phải thông suốt từ lúc bắt đầu (giao diện gửi yêu cầu) đến lúc kết thúc (nơi nhận/xem yêu cầu hiển thị và cập nhật trạng thái).
- Khi thêm hoặc chỉnh sửa trạng thái/dữ liệu ở một màn hình (ví dụ: gửi Yêu cầu Phê duyệt từ Lễ tân):
  - Kiểm tra xem các màn hình liên quan của người dùng khác (ví dụ: trang Duyệt yêu cầu của Manager) có hiển thị đúng, đủ các thông tin và trạng thái tương ứng hay không.
  - Đảm bảo không bị lệch kiểu dữ liệu hoặc giá trị nhãn (như enum, type string) khiến dữ liệu bị bỏ sót khi truy vấn lọc ở màn hình đích.
  - Khi thực thi các hành động phê duyệt/từ chối, các thay đổi trạng thái phải được cập nhật đồng bộ và chính xác xuống database cũng như các thực thể liên quan (ví dụ: trạng thái đơn đặt phòng Booking chuyển sang `Pending_Approval` khi gửi yêu cầu, và trả lại `Pending`/`CANCELLED` sau khi Manager xử lý).

## 6. Thymeleaf Template Integrity & Chunked Encoding Debugging
- **Tránh đóng block sớm (Premature Closure)**: Khi lặp với `<th:block th:each="...">`, thẻ đóng `</th:block>` phải được đặt ở cuối cùng, sau khi tất cả các thẻ con bên trong nó (ví dụ: thẻ bao `booking-item`, `details`, v.v.) đã đóng hoàn toàn.
- **Phạm vi của biến lặp**: Hãy chắc chắn rằng biến lặp (ví dụ: `booking.id`) chỉ được truy cập ở bên trong phạm vi lặp của `th:block`. Truy cập ngoài phạm vi sẽ dẫn đến biến bị `null` ở Server-side, làm gián đoạn luồng HTTP (`ERR_INCOMPLETE_CHUNKED_ENCODING`).
- **Tránh trùng lặp vòng lặp**: Không khai báo lặp lại biến vòng lặp trùng tên lồng nhau (như thẻ cha lặp `booking` và thẻ con cũng lặp `booking`).
- **Khắc phục sự cố tải thiếu trang**: Khi màn hình tải dở dang và bị đứng/không điều hướng được tab, hãy tìm ngay log lỗi Java tại backend Server hoặc viết/chạy Unit Test MockMvc render view để định vị dòng lệnh bị ném ngoại lệ trong template thay vì cố debug JS client.

## 7. String Normalization & Client-Side Mapping
- **Chuẩn hóa chuỗi trước khi so khớp:** Khi sử dụng các chuỗi động lấy từ Server/Database (như tên thực thể, loại tour, tên phòng) để tra cứu dữ liệu tĩnh trên giao diện khách hoặc nội bộ, bắt buộc phải viết hàm chuẩn hóa ký tự (`normalizeKey`) để loại bỏ dấu tiếng Việt, ký tự đặc biệt, chuyển thành chữ thường và xóa khoảng trắng dư thừa trước khi so khớp.
- **Ánh xạ từ khóa linh hoạt:** Cấu hình bộ quy tắc so khớp lỏng (`includes` hoặc regex) thay vì so khớp tuyệt đối (`===`) để hỗ trợ tìm kiếm theo cả từ khóa rút gọn, giúp giao diện không bị lỗi hoặc hiển thị trống khi dữ liệu trên DB có thay đổi nhỏ về tiêu đề.
- **Cơ chế Fallback an toàn:** Khi so khớp dựa trên chuỗi văn bản không thành công (không tìm thấy key phù hợp), **BẮT BUỘC** phải có cơ chế fallback tự động sử dụng thuộc tính định danh chính gốc từ database (như ID, loại định danh `tourType`, `roomCategoryCode`) được nạp sẵn để hiển thị thông tin chính xác, tránh việc giao diện im lặng bỏ qua (silent failure) hoặc hiển thị trống rỗng.
- **Tránh ghi đè mảng theo chỉ mục (Index Shifting):** Khi hợp nhất danh sách dữ liệu động từ Server (ví dụ: danh sách hoạt động tour bao gồm cả điểm đón/trả) vào danh sách tĩnh chứa siêu dữ liệu UI (toạ độ chấm CSS, hình ảnh bản đồ...), **TUYỆT ĐỐI KHÔNG** gán đè đơn giản theo chỉ mục (`dynamicList[i]` → `staticList[i]`) nếu kích thước hai mảng khác nhau. Hậu quả: toạ độ và hình ảnh của phần tử giữa bị lệch sang phần tử sai, gây hiển thị sai vị trí trên bản đồ. **Giải pháp:** Dùng so khớp theo thuộc tính tương đồng (tiêu đề, ID hoặc từ khoá). Các phần tử đặc biệt (ví dụ: "Đón khách", "Trả khách") cần được map cứng (hard-coded) vào toạ độ xác định; phần tử còn lại so khớp linh hoạt với danh sách tĩnh để kế thừa đúng siêu dữ liệu UI.

## 8. Tránh rò rỉ dữ liệu qua biến toàn cục (Global Variable Cleanup)
- **Reset biến toàn cục ở đầu hàm load:** Khi viết/chỉnh sửa mã JavaScript trên giao diện chi tiết hoặc các màn hình dùng chung biến toàn cục (như `bookingGroupData`, `appliedPromoCode`, `globalDeposit`), bắt buộc phải reset sạch sẽ các biến này về giá trị mặc định (`null`, `0`, `{}`) ở đầu hàm `fetch/load` dữ liệu mới.
- **Rủi ro rò rỉ dữ liệu:** Nếu không reset, khi người dùng chuyển nhanh giữa các bản ghi khác nhau (ví dụ: đổi từ xem chi tiết Booking của khách A sang khách B), dữ liệu của khách cũ (như mã giảm giá hoặc tiền cọc đã nạp) vẫn bị giữ lại trong bộ nhớ client và đè lên cách tính toán/hiển thị của khách mới, gây sai lệch nghiêm trọng thông tin thanh toán.
=======
## 8. Phân biệt giao dịch tài chính (Financial Transaction Consistency)
- **Tuyệt đối không gộp nhóm giao dịch chỉ bằng dấu (+/-):** Khi Frontend nhận danh sách `FolioItem` (hoặc Transaction) từ Backend, nếu cần tính tổng tiền nạp, tiền cọc, hoặc tiền hoàn (Refund), **KHÔNG ĐƯỢC** quét mọi khoản tiền âm (`amount < 0`) rồi tự động cộng dồn vào cùng một biến hiển thị. Điều này sẽ dẫn đến việc cộng nhầm Tiền cọc phòng (Pre-paid Deposit) với Tiền nạp hạn mức (Credit Deposit) và gây hiện tượng "Double-count" (cấn trừ đúp).
- **Phân loại dựa trên Metadata/Description:** Luôn phải dựa vào thuộc tính `description` (ví dụ chứa cụm từ `"nạp tiền nâng hạn mức"`), `sourceDepartment`, hoặc `transactionType` để lọc chính xác đúng loại giao dịch cần hiển thị trên UI. Sự phân loại này trên Frontend **PHẢI** luôn khớp 100% với điều kiện truy vấn tại Backend Repository (ví dụ: `FolioItemRepository.findCreditDepositAmountsByDetailId`).
