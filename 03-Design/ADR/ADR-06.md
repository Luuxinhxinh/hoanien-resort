# ADR-06: Kiến trúc Quét CCCD Từ xa và Kết nối Thời gian thực (Remote Scan & WebSocket Architecture)

* **Trạng thái:** Confirmed
* **Tác giả:** Nhóm phát triển Kawai Retreat
* **Ngày quyết định:** 2026-07-01
* **Lĩnh vực:** Truyền thông Thời gian thực, WebSocket, Microservice OCR, Bảo mật

---

## 1. Bối cảnh (Context)

Một trong những trải nghiệm nghẽn nhất tại quầy lễ tân resort là khâu khai báo thông tin lưu trú. Lễ tân phải mượn CCCD/Passport vật lý của từng thành viên trong đoàn và gõ thủ công từng trường thông tin vào hệ thống, dễ xảy ra sai sót và mất thời gian (khoảng 3-5 phút/khách).

Để giải quyết bài toán này, hệ thống thiết kế tính năng **Quét CCCD từ xa (Remote Scan)**:
* Lễ tân click nút khởi tạo trên màn hình máy tính Desktop.
* Hệ thống sinh ra một mã QR. Khách hàng dùng điện thoại cá nhân quét mã QR để mở ra một trang chụp ảnh CCCD.
* Khách tự chụp ảnh CCCD của mình. Hệ thống nhận ảnh, nhận diện ký tự (OCR) và tự động điền (Autofill) toàn bộ thông tin vào màn hình đăng ký của Lễ tân.

Thách thức đặt ra là làm thế nào để truyền tải thông tin OCR từ điện thoại của Khách về màn hình máy tính của Lễ tân lập tức, không bắt Lễ tân phải reload trang (F5), đồng thời phải bảo mật tuyệt đối ảnh chụp CCCD của khách.

## 2. Quyết định (Decision)

Chúng tôi quyết định triển khai giải pháp kiến trúc dựa trên WebSocket và Python OCR Microservice như sau:

### 2.1 Truyền thông Thời gian thực với WebSocket & STOMP Broker
* Sử dụng giao thức **WebSocket** kết hợp giao thức phụ **STOMP** (Simple Text Oriented Messaging Protocol) tích hợp sẵn trong Spring Boot.
* Máy tính của Lễ tân đăng ký lắng nghe (Subscribe) vào một kênh (Topic) cá nhân định danh bằng Session ID: `/topic/scan/{sessionId}`.
* Khi khách hàng quét QR, điện thoại của khách cũng kết nối vào session tương tự để đồng bộ trạng thái (ví dụ: màn hình lễ tân chuyển sang trạng thái "Khách đang chụp ảnh...").

### 2.2 Quy trình xử lý Ảnh & Tích hợp OCR Service
* Điện thoại khách hàng gửi ảnh CCCD lên Spring Boot backend thông qua một REST API được bảo vệ bằng HTTPS.
* Spring Boot đóng vai trò Gateway, lập tức chuyển tiếp (Forward) file ảnh này sang **Python OCR Microservice** thông qua kết nối HTTP nội bộ (gọi API RestTemplate/WebClient bảo mật giữa các container).
  * Python OCR Service sử dụng thư viện chuyên dụng (như EasyOCR hoặc Google Vision API) để trích xuất văn bản từ ảnh.
* Sau khi nhận kết quả JSON (gồm: họ tên, số CCCD, ngày sinh, giới tính) từ Python OCR, Spring Boot thực hiện:
  1. Gửi kết quả dạng JSON về kênh WebSocket của Lễ tân `/topic/scan/{sessionId}`.
  2. Xóa vĩnh viễn file ảnh CCCD tạm thời khỏi bộ nhớ đệm của cả Java và Python server (Không lưu ảnh vật lý vào ổ cứng để tránh rủi ro rò rỉ dữ liệu).

### 2.3 Quản lý Phiên quét (Session Lifecycle & TTL)
* Phiên quét (`RemoteScanSession`) được quản lý bằng cache nội bộ trong Spring Boot với thời hạn tồn tại (**TTL - Time To Live**) cố định là **2 phút**.
* QR code hiển thị trên màn hình Lễ tân chỉ có hiệu lực trong 2 phút. Quá thời gian này, WebSocket sẽ gửi tín hiệu hết hạn (Expired), yêu cầu lễ tân sinh mã QR mới nếu khách chưa kịp quét.
* Cơ chế này ngăn chặn việc mã QR bị chụp lại và khai thác trái phép sau đó.

## 3. Hệ quả (Consequences)

### Tích cực (Pros)
* **Trải nghiệm khách hàng vượt trội:** Thời gian làm thủ tục check-in giảm từ 5 phút xuống còn dưới 30 giây cho mỗi khách. Khách hàng cảm thấy an tâm vì tự mình giữ và chụp giấy tờ cá nhân.
* **Tự động điền dữ liệu chính xác:** Giảm tối đa sai sót chính tả do nhập liệu thủ công, đảm bảo chất lượng dữ liệu để khai báo tạm trú với cơ quan chức năng.
* **Bảo mật dữ liệu tối đa:** Việc không lưu trữ ảnh gốc CCCD trên server giúp loại bỏ rủi ro pháp lý về quyền riêng tư và rò rỉ thông tin cá nhân.

### Hạn chế (Cons)
* **Độ ổn định của kết nối:** WebSocket yêu cầu đường truyền mạng ổn định. Nếu mạng wifi của resort chập chờn, kết nối WebSocket có thể bị ngắt giữa chừng.
  * *Biện pháp:* Client (màn hình Lễ tân) phải cài đặt cơ chế tự động kết nối lại (Auto-reconnect) bằng thư viện `SockJS` và hiển thị trạng thái kết nối rõ ràng để Lễ tân chủ động xử lý.
* **Phụ thuộc vào Microservice ngoài:** Nếu Python OCR microservice bị lỗi hoặc quá tải, tính năng quét từ xa sẽ bị dừng.
  * *Biện pháp:* Xây dựng cơ chế cô lập lỗi (Circuit Breaker). Nếu OCR lỗi, ứng dụng Spring Boot trả về lỗi chi tiết cho điện thoại khách để báo khách nhập tay hoặc chuyển sang quét tại quầy cho lễ tân.
