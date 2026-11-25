# Hòa Niên Retreat & Resort

**Đồ án Môn học:** SWP391 - Software Development Project  
**Lớp:** SE2023-G2  
**Nhóm:** Group 2  
**Giảng viên hướng dẫn:** Nguyễn Mạnh Cường  

---

## 📖 Giới thiệu Dự án (Project Description)

**Hòa Niên Retreat & Resort** là một hệ thống phần mềm quản lý vận hành toàn diện dành cho Khu nghỉ dưỡng (Resort) tích hợp với trung tâm điều hành Tour du lịch. 
Dự án được thiết kế để số hóa và tự động hóa các quy trình từ Front-Desk (Lễ tân), F&B (Nhà hàng & Bếp), Dọn phòng (Housekeeping) cho đến Tour Operations. 
Với kiến trúc monolithic mạnh mẽ trên nền tảng Spring Boot kết hợp giao diện Thymeleaf, hệ thống giải quyết bài toán quản lý tập trung, nâng cao trải nghiệm khách hàng và tối ưu hóa hiệu suất làm việc của nhân viên.

---

## 🌟 Các phân hệ chức năng chính (Core Modules)

1. **Quản lý Lễ tân (Reception & Front Desk):** 
   - Check-in / Check-out cho khách Walk-in và In-house.
   - Gán phòng tự động, quản lý Folio (Hóa đơn lưu trú) đa dịch vụ.
2. **Quản lý Ẩm thực (F&B / POS):** 
   - Hệ thống POS dành riêng cho nhà hàng và đặt món tại phòng (Room Service).
   - Đặt bàn, quản lý menu, đồng bộ order với bếp.
3. **Quản lý Buồng phòng (Housekeeping):** 
   - Tự động tạo Task dọn phòng khi khách Check-out.
   - Quản lý trạng thái phòng (Clean, Dirty, Maintenance).
4. **Quản lý Tour Du lịch (Tour Operations):** 
   - Lên lịch trình, quản lý danh sách khách tham gia (Tour Attendees).
5. **Night Audit & Tài chính (Finance):**
   - Chạy tiến trình Night Audit để chốt doanh thu cuối ngày.
   - Quản lý thanh toán tích hợp VNPay, xuất hóa đơn gộp (Consolidated Invoice).
6. **Hệ thống Quy trình (Workflow Engine):**
   - Xử lý các quy trình phê duyệt nội bộ (VD: Phê duyệt hoàn tiền - Refund Request).

---

## 🚀 Công nghệ sử dụng (Tech Stack)

### Backend
- **Core Framework:** Java 17, Spring Boot 3.x
- **Data Access:** Spring Data JPA, Hibernate ORM
- **Security:** Spring Security (OAuth2, BCrypt Password Encoding)
- **Database:** MySQL 8.x
- **Mail/Communication:** JavaMailSender, SendGrid, Cloudinary (Lưu trữ ảnh)
- **Thanh toán:** Tích hợp cổng VNPay Sandbox

### Frontend
- **Template Engine:** Thymeleaf
- **Styling:** HTML5, CSS3, Bootstrap 5, Custom CSS
- **Interactivity:** Vanilla JavaScript, GSAP (Animations), Toastify (Notifications), SweetAlert2

---

## 📂 Cấu trúc Thư mục (Project Structure)

Dự án được tổ chức chặt chẽ theo chuẩn quy trình phát triển phần mềm:

- `00-Policy/`: Các chính sách và quy định quản lý dự án.
- `01-Planning/`: Tài liệu lên kế hoạch, quản lý timeline và task.
- `02-Requirement/`: Tài liệu Đặc tả Yêu cầu Phần mềm (SRS - Software Requirement Specification).
- `03-Design/`: Tài liệu Đặc tả Thiết kế Hệ thống (Database Schema, UI/UX Mockups, UML Diagrams).
- `04-Implement/`: Các quy chuẩn coding convention.
- `05-Development/hoanien-backend/`: **Chứa toàn bộ mã nguồn chính của ứng dụng Spring Boot (`com.hoanien`).**
  - `src/main/java/com/hoanien/`: Source code Java phân chia theo chuẩn MVC (Controllers, Services, Models, Repositories, Security, Config).
  - `src/main/resources/`: Cấu hình ứng dụng (`application.yml`), file giao diện Thymeleaf (`templates/`), static assets (`static/`), và dữ liệu mẫu (`schema.sql`, `data.sql`).
- `07-Reports/`: Báo cáo tiến độ và kết quả giai đoạn.
- `08-Document-References/`: Tài liệu tham khảo thêm.

---

## ⚙️ Hướng dẫn Cài đặt & Chạy dự án (Getting Started)

### Yêu cầu hệ thống:
- Java JDK 17+
- Maven 3.8+
- MySQL Server 8.0+
- IDE: IntelliJ IDEA, Eclipse, hoặc VS Code.

### Các bước triển khai:

1. **Clone repository:**
   ```bash
   git clone git@gitlab.com:manhnc2/su26-swp391-se2023-g2.git
   cd su26-swp391-se2023-g2/05-Development/hoanien-backend
   ```

2. **Cấu hình Cơ sở dữ liệu:**
   - Tạo mới database trong MySQL có tên `hoanien_db` (hoặc để Spring Boot tự động tạo thông qua cấu hình URL).
   - Mở file `src/main/resources/application.yml` hoặc thiết lập biến môi trường:
     ```yaml
     spring:
       datasource:
         url: ${SPRING_DATASOURCE_URL:jdbc:mysql://localhost:3306/hoanien_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=Asia/Ho_Chi_Minh&allowPublicKeyRetrieval=true&useUnicode=true}
         username: ${SPRING_DATASOURCE_USERNAME:root}
         password: ${SPRING_DATASOURCE_PASSWORD:12345678}
     ```

3. **Thiết lập biến môi trường (Environment Variables) (Tùy chọn nhưng khuyến nghị):**
   - `SENDGRID_API_KEY`: Key của SendGrid để gửi mail.
   - `MAIL_USERNAME`, `MAIL_PASSWORD`: Cấu hình SMTP Gmail dự phòng.
   - `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET`, `CLOUDINARY_CLOUD_NAME`: Cấu hình Cloudinary.

4. **Khởi tạo dữ liệu mẫu (Dummy Data):**
   - Hệ thống tự động chạy script `src/main/resources/schema.sql` và `src/main/resources/data.sql` khi khởi động (`spring.sql.init.mode=always`). Danh sách tài khoản nhân viên, phòng, tour, danh mục, v.v. được nạp sẵn để demo ngay lập tức (xem đầy đủ tại `DEMO_SHIFT_ACCOUNTS_GUIDE.md`).
   - **Tài khoản demo mặc định:**
     - **Admin**: `nguyenquantri` / `admin123` *(đăng nhập tại `/ops-login`)*
     - **Manager**: `lequanly` / `admin123` *(đăng nhập tại `/ops-login`)*
     - **Lễ tân**: `lelinh` / `staff123` *(đăng nhập tại `/ops-login`)*
     - **POS (Nhà hàng)**: `pos1` / `staff123` *(đăng nhập tại `/ops-login`)*
     - **Khách hàng**: `nguyenxuanluu` / `admin123` *(đăng nhập trên cổng khách `/living` hoặc `/booking`)*

5. **Build và Chạy ứng dụng:**
   Sử dụng Maven Wrapper có sẵn trong project (hoặc `mvn`):
   ```bash
   ./mvnw spring-boot:run
   ```
   *Ứng dụng mặc định chạy ở cổng `8080` (có thể đổi bằng `SERVER_PORT=8081`). Truy cập trình duyệt tại: `http://localhost:8080` (Cổng vận hành nội bộ: `http://localhost:8080/ops-login`).*
