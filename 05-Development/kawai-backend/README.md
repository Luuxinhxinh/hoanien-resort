# 🏨 Hệ thống Quản trị Resort & Khách sạn Hoa Niên (Kawai Resort)

## 📌 Giới thiệu dự án
Đây là hệ thống phần mềm quản lý tổng thể dành cho Khách sạn & Resort Hoa Niên (Kawai Resort). Hệ thống bao quát toàn bộ quy trình vận hành thực tế của một resort quy mô lớn, từ việc khách hàng đặt phòng trực tuyến, lễ tân đón khách, quản lý nhà hàng (F&B), điều hành Tour du lịch, cho đến các luồng phê duyệt tài chính khép kín của cấp Quản lý.

## 🏗 Phân quyền và Chức năng chính (Roles)
Hệ thống được thiết kế với nhiều phân hệ (module) chuyên sâu theo từng vị trí việc làm:

1. **Guest / Customer (Khách hàng):** 
   - Đặt phòng, mua tour trực tuyến.
   - Xem hồ sơ cá nhân (Profile) với lịch sử đơn hàng rõ ràng.
2. **Receptionist (Lễ tân):** 
   - Quản lý khách Walk-in và khách In-house (đang lưu trú).
   - Thao tác Check-in, Check-out, Đổi phòng (Transfer room).
   - Quản lý Hóa đơn tổng (Folio) và gửi yêu cầu đền bù/giảm giá.
3. **Manager (Quản lý):** 
   - Duyệt các yêu cầu vượt thẩm quyền của Lễ tân (giảm giá, miễn phí phạt huỷ, hoàn tiền).
   - Xem bảng điều khiển phân tích (Analytics) và báo cáo doanh thu.
4. **Tourguide (Hướng dẫn viên):** 
   - Xem lịch trình các tour được phân công.
   - Quản lý danh sách khách (Manifest) và điểm danh.
5. **F&B / Housekeeping (Nhà hàng & Buồng phòng):** 
   - Quản lý order món ăn, trạng thái dọn dẹp phòng trực tiếp liên kết với Lễ tân.

## 🛠 Công nghệ sử dụng
- **Backend:** Java 17, Spring Boot 3.2 (Spring MVC, Spring Security, Spring Data JPA).
- **Frontend:** HTML5, Thymeleaf, Tailwind CSS (Giao diện hiện đại, responsive).
- **Cơ sở dữ liệu:** MySQL (cho môi trường thật) hoặc H2 Database (cho môi trường test).
- **Công cụ hỗ trợ:** Hibernate Envers (lưu Audit log), SMTP/SendGrid (Gửi Email tự động).

---

## 🚀 Hướng dẫn cài đặt và khởi chạy (Cơ bản nhất)

Phần này hướng dẫn bạn cách tải code về và chạy lên ngay lập tức với các cấu hình có sẵn.

### Yêu cầu môi trường:
- Java JDK 17
- Maven 3.8+
- (Tùy chọn) MySQL Server nếu muốn lưu dữ liệu vĩnh viễn.

### Bước 1: Thiết lập cấu hình (Configuration)
1. Tải mã nguồn về máy.
2. Mở file `src/main/resources/application.yml`.
3. Kiểm tra thông tin kết nối Database. 
   > 💡 **Mẹo:** Nếu bạn không muốn cài MySQL, dự án đã có sẵn thư viện **H2 Database**. Spring Boot sẽ tự động tạo database trên RAM để bạn test thử các chức năng mà không cần setup rườm rà.

### Bước 2: Dữ liệu mẫu (Seeding)
Bạn không cần tự tạo dữ liệu bằng tay. Khi ứng dụng khởi động, nó sẽ tự động đọc file `src/main/resources/data.sql` và nạp sẵn hàng loạt dữ liệu mẫu bao gồm: Danh sách phòng, Tour du lịch, Menu đồ ăn, và các tài khoản test.

### Bước 3: Chạy ứng dụng
Mở Terminal / Command Prompt tại thư mục gốc của dự án (nơi có file `pom.xml`) và chạy lệnh sau:
```bash
mvn spring-boot:run
```
Sau khi terminal báo `Started KawaiBackendApplication`, bạn mở trình duyệt và truy cập:
👉 **http://localhost:8080**

---

## 🔑 Danh sách Tài khoản Test
Toàn bộ tài khoản dưới đây dùng chung một mật khẩu là: **`admin123`**

| Vai trò (Role) | Username (Tài khoản) | Khu vực truy cập chính |
| :--- | :--- | :--- |
| **Admin** | `admin` | `/admin/dashboard` |
| **Manager** | `manager1` | `/manager/dashboard` |
| **Lễ tân (FO)** | `tphuong` | `/receptionist/dashboard` |
| **Hướng dẫn viên** | `phamtuan`, `hoanganh` | `/tourguide/dashboard` |
| **Khách hàng** | `hoangnam`, `vanan` | `/` (Trang chủ mua sắm) |
