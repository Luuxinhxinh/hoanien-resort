# Kawai Resort & Tour Hub

**Đồ án Môn học:** SWP391 - Software Development Project
**Lớp:** SE2003-NET
**Nhóm:** Group 2
**Giảng viên hướng dẫn:** Nguyễn Mạnh Cường

---

## 📖 Giới thiệu Dự án (Project Description)

Kawai Resort & Tour Hub là một hệ thống phần mềm quản lý vận hành toàn diện dành cho Khu nghỉ dưỡng (Resort) tích hợp với trung tâm điều hành Tour du lịch. Dự án được thiết kế để số hóa và tự động hóa các quy trình từ Front-Desk (Lễ tân), F&B (Nhà hàng & Bếp), Dọn phòng (Housekeeping) cho đến Tour Operations (tích hợp AI Face Scan điểm danh khách).

## 🚀 Công nghệ sử dụng (Tech Stack)

- **Backend Core:** Java, Spring Boot, Spring Data JPA, Spring Security, Hibernate.
- **AI Service:** Python, Flask/FastAPI, OpenCV, dlib (Nhận diện khuôn mặt).
- **Frontend:** HTML, CSS, JavaScript, Bootstrap, Spring MVC (Thymeleaf/JSP).
- **Database:** MySQL.
- **Tích hợp bên thứ 3:** Cổng thanh toán VNPay Sandbox, OpenWeather API, Email SMTP.

## 📂 Cấu trúc Thư mục (Project Structure)

Dự án được tổ chức theo chuẩn quy trình phát triển phần mềm:

- `01_SRS/`: Chứa tài liệu Đặc tả Yêu cầu Phần mềm (Software Requirement Specification).
- `02_SDS/`: Chứa tài liệu Đặc tả Thiết kế Hệ thống (Software Design Specification).
- `sourcecode/`: Chứa mã nguồn dự án.
  - `database/`: Chứa các script SQL và schema database.

## ⚙️ Hướng dẫn Cài đặt & Chạy dự án (Getting Started)

*(Phần này sẽ được cập nhật khi dự án bắt đầu giai đoạn Code)*

1. Clone dự án.
2. Mở file `application.properties` để cấu hình chuỗi kết nối MySQL.
3. Chạy script SQL trong `sourcecode/database/` để khởi tạo bảng.
4. Chạy service Python AI (cần cài đặt thư viện OpenCV, dlib).
5. Build và Run project Spring Boot.
