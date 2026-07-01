# Hướng Dẫn Cấu Hình Tính Năng Quét CCCD/FaceID Bằng Điện Thoại (Remote Scan)

Tính năng quét CCCD và nhận diện khuôn mặt yêu cầu trình duyệt phải cấp quyền truy cập Camera. Theo chính sách bảo mật mặc định của trình duyệt (Chrome, Safari, Edge...), Camera **chỉ được phép truy cập khi sử dụng giao thức bảo mật HTTPS** (hoặc chạy trên `localhost`).

Vì trong quá trình phát triển (Dev) chúng ta đang chạy backend trên `http` thông qua mạng LAN, điện thoại của bạn sẽ bị trình duyệt chặn không cho bật Camera. Để khắc phục, chúng ta cần đưa địa chỉ IP của máy tính vào danh sách ngoại lệ (Insecure Origins) của trình duyệt.

Vui lòng làm theo 2 bước sau đây trên một máy tính mới hoặc điện thoại mới:

---

## Bước 1: Lấy địa chỉ IPv4 của máy tính (Server)

1. Mở cửa sổ **Command Prompt (CMD)** hoặc **PowerShell** trên máy tính đang chạy Backend.
2. Gõ lệnh sau và nhấn Enter:
   ```Shell
   ipconfig
   ```
3. Tìm đến phần card mạng bạn đang sử dụng (thường là `Wireless LAN adapter Wi-Fi` hoặc `Ethernet adapter`).
4. Copy lại dải số ở dòng **IPv4 Address**.
   *Ví dụ: `192.168.1.15` hoặc `10.0.1.5`...*

> **Lưu ý Quan Trọng:** Máy tính (Server chạy code) và Điện thoại (Thiết bị quét) **BẮT BUỘC** phải kết nối chung một mạng Wi-Fi (Cùng chung mạng LAN).

---

## Bước 2: Bỏ qua cảnh báo bảo mật trên trình duyệt (Thiết lập Insecure Origins)

Bạn cần thực hiện bước này trên **điện thoại hoặc thiết bị sẽ dùng để quét Camera**.
Khuyến nghị sử dụng trình duyệt **Google Chrome** hoặc **Microsoft Edge**.

1. Mở trình duyệt Chrome (hoặc Edge) trên điện thoại.
2. Trên thanh địa chỉ (URL), gõ đường dẫn sau:
   - Nếu dùng Chrome: `chrome://flags`
   - Nếu dùng Edge: `edge://flags`
3. Tại ô tìm kiếm (Search flags) ở góc trên, gõ từ khóa: **`insecure`**
4. Bạn sẽ thấy mục có tên là: **"Insecure origins treated as secure"**
5. Tại ô văn bản ngay dưới mục đó, hãy điền địa chỉ IP của máy tính (lấy ở Bước 1) kèm theo cổng (Port).
   *Ví dụ: `http://192.168.1.15:8080`*
6. Đổi trạng thái nút bấm bên phải mục này từ **Disabled** sang **Enabled**.
7. Trình duyệt sẽ hiện một nút màu xanh ở góc dưới màn hình yêu cầu khởi động lại: **Relaunch**. Hãy bấm vào đó.

---

## Bước 3: Truy cập và sử dụng Camera

Sau khi trình duyệt khởi động lại thành công:

1. Bạn truy cập vào địa chỉ IP của lễ tân trên điện thoại. Ví dụ: `http://192.168.1.15:8080` (nhớ đăng nhập tài khoản lễ tân).
2. Khi bấm vào tính năng **Quét QR** hoặc **Thêm người đi kèm (Tự động)**, trình duyệt sẽ hiện popup hỏi quyền sử dụng Camera.
3. Bấm **Cho Phép (Allow)** là bạn đã có thể quét mã CCCD bình thường!

> **Xử lý sự cố (Troubleshooting):**
>
> - **Lỗi không kết nối được:** Hãy kiểm tra lại Firewall (Tường lửa) trên máy tính Windows, đảm bảo port 8080 đang được mở, hoặc tạm tắt Firewall (Domain/Private/Public) khi test mạng LAN. Đảm bảo điện thoại không dùng 3G/4G mà đang dùng chung WiFi.
> - **Lỗi vẫn bị chặn Camera:** Đảm bảo bạn gõ đầy đủ chữ `http://` trong thẻ flags, không thừa khoảng trắng.
