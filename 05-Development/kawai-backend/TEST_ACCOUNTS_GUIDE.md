# Danh sách Tài khoản Test (Kawaii Resort & Tour Hub)

Tất cả các tài khoản bên dưới (ngoại trừ admin) đều có chung mật khẩu mặc định là: **`staff123`**

---

## 1. Tài khoản Nhân viên (Dành cho nội bộ / Cổng Admin)

| Tên đăng nhập | Mật khẩu | Quyền hạn (Role) | Chức năng chính để test |
| :--- | :--- | :--- | :--- |
| `admin` | `admin123` | **ADMIN** | Quản lý Master Data, Phân quyền, Xem Audit Logs, Cấu hình chung. |
| `lelinh` | `staff123` | **RECEPTIONIST** | Màn hình Check-in, Check-out, Gán phòng, Nhận diện khuôn mặt, Quản lý Folio. |
| `tphuong` | `staff123` | **F&B KITCHEN** | Quản lý đơn hàng nhà hàng, Chuyển trạng thái bếp nấu. |
| `nmquan` | `staff123` | **F&B POS** | Tạo order mới, Quản lý bàn ăn nhà hàng. |
| `phamtuan` | `staff123` | **TOURGUIDE** | Xem lịch trình tour, Danh sách khách đi tour. |

---

## 2. Tài khoản Khách hàng (Dành cho trang chủ Đặt phòng)

Dùng để mô phỏng End-user thực hiện các thao tác từ bên ngoài.

### 👑 `vip_customer` (Khách hàng VIP)
*   **Hạng:** DIAMOND (52,000 điểm).
*   **Trạng thái:** Đang có Booking **In-house** (Lưu trú tại phòng 801), đi kèm vợ và con nhỏ.
*   **Kịch bản Test:**
    *   Test Gọi đồ ăn lên phòng (Room Service) với tùy chọn thanh toán **Charge to Room** (nợ vào tiền phòng).
    *   Test dùng Lễ tân gán phòng, nhập thông tin và quét FaceID cho vợ/con (Dependent).
    *   Test thanh toán khi Checkout bằng điểm thưởng.

### 👤 `normal_customer` (Khách hàng Thông thường)
*   **Hạng:** BRONZE (95 điểm).
*   **Trạng thái:** 
    *   1 Booking sắp tới (Trạng thái **Confirmed**).
    *   1 Booking đang chờ duyệt (**Pending_Approval**) do xài mã giảm giá giảm sâu (> 30%).
*   **Kịch bản Test:**
    *   Lễ tân tìm kiếm và thực hiện quy trình Check-in cho khách tới nhận phòng từ đầu.
    *   Manager vào Admin phê duyệt Booking bị khóa do vượt ngưỡng chiết khấu.

### 🌱 `newbie_customer` (Khách hàng Mới)
*   **Hạng:** BRONZE (0 điểm).
*   **Trạng thái:** Chưa từng phát sinh giao dịch.
*   **Kịch bản Test:** 
    *   Làm luồng Đặt phòng (Room) kết hợp Đặt Tour hoàn toàn mới.
    *   Tạo tài khoản và luồng thanh toán giỏ hàng.

### 🚫 `banned_customer` (Khách hàng Nợ xấu)
*   **Trạng thái:** Bị hệ thống khóa (`is_active = false`).
*   **Kịch bản Test:**
    *   Test đăng nhập xem có bị văng lỗi không.
    *   Đảm bảo không thể đặt phòng hoặc các dịch vụ khác trên hệ thống.
