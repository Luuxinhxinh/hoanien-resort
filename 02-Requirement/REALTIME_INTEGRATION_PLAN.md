# KẾ HOẠCH TÍCH HỢP HỆ THỐNG REAL-TIME (THỜI GIAN THỰC)
**Dự án:** Kawai Resort Management System
**Giai đoạn:** Đề xuất mở rộng tương lai (Future Extension)
**Công nghệ dự kiến:** Spring WebSockets, STOMP, SockJS

---

## 1. Mục tiêu & Phạm vi (Objectives & Scope)

### Mục tiêu
Cải thiện luồng vận hành bằng cách thông báo ngay lập tức (Real-time) cho các bộ phận chuyên môn khi có giao dịch hoặc sự kiện mới xảy ra trên hệ thống mà không yêu cầu người dùng phải chủ động tải lại trang (F5).

### Phạm vi tích hợp (Use Cases tiềm năng)
- **Bộ phận Lễ tân (Front Desk):** Nhận "chuông thông báo" khi khách hàng thanh toán cọc thành công hoặc có Booking mới.
- **Bộ phận F&B (Nhà hàng):** Bếp/Pha chế nhận ngay bản in Order khi khách order món ăn tại bàn.
- **Khách hàng (Client-side):** Trạng thái bàn được cập nhật ngay lập tức nếu bàn vừa bị khách khác đặt (Ngăn chặn Overbooking giao diện).

---

## 2. Kiến trúc Hệ thống (Architecture Design)

Hệ thống sẽ sử dụng kiến trúc **Pub/Sub (Publisher / Subscriber)** thông qua giao thức **STOMP (Simple Text Oriented Messaging Protocol)** chạy trên nền **WebSockets**.

- **Broker:** Sử dụng Simple In-Memory Message Broker của Spring Boot (đủ đáp ứng cho quy mô resort).
- **Endpoint kết nối gốc:** `/kawai-websocket`
- **Topics (Kênh phát sóng):**
  - `/topic/admin/bookings` : Bắn Notification về Đơn phòng.
  - `/topic/fnb/orders` : Bắn Notification về Đơn đồ ăn.
  - `/topic/system/alerts` : Cảnh báo hệ thống (VD: Lỗi thanh toán).

---

## 3. Lộ trình Triển khai (Implementation Steps)

### Phase 1: Backend Setup (Spring Boot)
1. **Cập nhật Dependencies:** 
   Bổ sung `spring-boot-starter-websocket` vào `pom.xml`.
2. **Cấu hình WebSocketConfig:** 
   Tạo class `WebSocketConfig` implements `WebSocketMessageBrokerConfigurer`.
   - Đăng ký endpoint `/kawai-websocket` hỗ trợ fallback `SockJS`.
   - Cấu hình MessageBroker với tiền tố `/topic` và `/app`.
3. **Cấu hình Security (Rất Quan Trọng):**
   - Viết cấu hình chặn (Interceptor) trong quá trình Handshake, ép buộc người dùng phải gửi Bearer Token (JWT) hoặc đang có Session (JSESSIONID) hợp lệ thì mới cho phép mở liên kết WebSocket.
   - Phân quyền: User mang role `CUSTOMER` không được phép subscribe vào `/topic/admin/bookings`.

### Phase 2: Backend Logic Integration
1. **Chèn `SimpMessagingTemplate`:**
   Tại các file như `BookingServiceImpl.java` hoặc `OrderServiceImpl.java`.
2. **Bắn Message theo Event:**
   - Sử dụng `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)`.
   - *Lý do:* Phải đảm bảo Database đã Commit xong dữ liệu thì mới bắn Message. Nếu bắn Message sớm mà Database bị rollback thì màn hình Lễ tân sẽ hiển thị thông tin rác ("Bóng ma dữ liệu").

### Phase 3: Frontend Integration (Thymeleaf / JS)
1. **Nhúng thư viện:** Import `sockjs.min.js` và `stomp.min.js` thông qua CDN hoặc thư mục tĩnh.
2. **Tạo `realtime-worker.js`:** 
   - Viết hàm `connect()` khởi tạo kết nối khi phát hiện Admin/Lễ tân vừa Login thành công.
   - Đăng ký `stompClient.subscribe('/topic/admin/bookings', function(payload) {...})`.
3. **Xử lý UI/UX:**
   - Khi nhận được `payload.body`, sử dụng thư viện như `SweetAlert2` hoặc `Toastr` để hiện Popup âm thanh "Ting ting" góc phải màn hình.
   - (Nâng cao): Dùng JavaScript (hoặc JQuery) chèn trực tiếp 1 dòng (Row) vào thẻ `<tbody>` của bảng quản lý đơn hàng.

---

## 4. Rủi ro & Giải pháp rủi ro (Risks & Mitigations)

| Rủi ro (Risk) | Tác động | Giải pháp (Mitigation) |
|---|---|---|
| **Rò rỉ dữ liệu (Data Leak)** | Nghiêm trọng | Nếu không cấu hình Security WebSocket, hacker có thể mở F12, tự kết nối vào `/topic/admin/bookings` và nghe lén dữ liệu khách hàng. **Bắt buộc phải chặn Handshake Channel.** |
| **Tiêu tốn Tài nguyên (Memory)**| Trung bình | WebSocket giữ connection liên tục. Cần cấu hình timeout tự ngắt kết nối đối với các Session đã Idle (Không thao tác) quá 30 phút. |
| **Bóng ma Dữ liệu (Ghost Data)** | Trung bình | Dùng `AFTER_COMMIT` event listener thay vì gọi trực tiếp `messagingTemplate` bên trong lòng hàm xử lý Business. |

---

## 5. Tiêu chí Nghiệm thu (Entry / Exit Criteria)

- **Entry:** Core Backend (CRUD, Auth) và Core Frontend đã hoàn thiện 100% và chạy ổn định. Không làm WebSocket song song khi chưa xong nền tảng.
- **Exit:** 
  - Khách bấm Submit Booking -> Màn hình Admin hiện Popup trong vòng < 0.5s.
  - Guest/Khách hàng bình thường không thể mở kết nối tới `/topic/admin/bookings`.
