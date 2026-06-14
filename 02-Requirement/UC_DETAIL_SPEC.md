# ĐẶC TẢ CHI TIẾT USE CASE (USE CASE DETAIL SPECIFICATIONS)

## HỆ THỐNG QUẢN LÝ NGHỈ DƯỠNG KAWAI RETREAT RESORT & HUB

| Field          | Value                                 |
| -------------- | ------------------------------------- |
| **Document ID**| `KAWAI-SRS-UC-DET-001`                |
| **Version**    | 1.0                                   |
| **Date**       | 2026-06-13                            |
| **Status**     | Approved                              |
| **Author**     | Nhóm Phát Triển SWP391 - G2           |
| **Reviewed by**| Tech Lead                             |

---

## CHANGELOG

| Ngày      | Người thực hiện   | Nội dung thay đổi                                     |
| ---------- | ----------------- | ----------------------------------------------------- |
| 2026-06-13 | Nhóm Phát Triển   | Khởi tạo tài liệu đặc tả chi tiết cho toàn bộ 28 Use Cases |

---

## MỤC LỤC
* [MODULE 1: HỆ THỐNG CỐT LÕI, XÁC THỰC & TÀI KHOẢN](#module-1-he-thong-cot-loi-xac-thuc-tai-khoan)
* [MODULE 2: QUẢN LÝ PHÒNG & LỄ TÂN VẬN HÀNH](#module-2-quan-ly-phong-le-tan-van-hanh)
* [MODULE 3: DỊCH VỤ ẨM THỰC & NHÀ HÀNG (F&B / POS)](#module-3-dich-vu-am-thuc-nha-hang-fb-pos)
* [MODULE 4: QUẢN LÝ LỮ HÀNH & ĐÁNH GIÁ](#module-4-quan-ly-lu-hanh-danh-gia)
* [MODULE 5: KIỂM TOÁN ĐÊM, TÀI CHÍNH & BÁO CÁO](#module-5-kiem-toan-dem-tai-chinh-bao-cao)

---

## MODULE 1: HỆ THỐNG CỐT LÕI, XÁC THỰC & TÀI KHOẢN

### UC01 — Đăng ký & Đăng nhập
*   **Actor:** Customer, User (Nhân viên, Quản lý, Admin)
*   **Mô tả:** Khách hàng đăng ký tài khoản mới; Người dùng đăng nhập hệ thống để sử dụng các chức năng theo phân quyền.
*   **Preconditions:** Thiết bị kết nối internet, truy cập vào đúng địa chỉ website resort.
*   **Basic Flow:**
    1. Người dùng chọn Đăng ký hoặc Đăng nhập.
    2. Điền thông tin yêu cầu (Username/Email, Password).
    3. Hệ thống xác thực dữ liệu nhập vào (sử dụng mã hóa BCrypt cho Password).
    4. Trả về thông báo thành công và điều hướng về trang tương ứng với Role của tài khoản.
*   **Exceptions:**
    *   Tên đăng nhập hoặc Email đã tồn tại.
    *   Mật khẩu không khớp hoặc sai thông tin đăng nhập.

### UC02 — Xác thực 2FA qua OTP
*   **Actor:** Customer, User
*   **Mô tả:** Tăng cường bảo mật bằng cách gửi mã OTP qua email/SMS khi đăng nhập từ thiết bị lạ hoặc thực hiện đổi thông tin nhạy cảm.
*   **Preconditions:** Đăng nhập bước 1 thành công.
*   **Basic Flow:**
    1. Hệ thống tự động gửi mã OTP 6 số đến email/số điện thoại đã đăng ký.
    2. Khách hàng nhập mã OTP trên màn hình xác thực.
    3. Hệ thống kiểm tra tính hợp lệ và thời gian hiệu lực của mã.
    4. Trực quan chuyển hướng người dùng vào hệ thống chính thức.
*   **Exceptions:**
    *   Mã OTP sai hoặc đã hết hạn hiệu lực (quá 3 phút).

### UC03 — Đặt lại mật khẩu
*   **Actor:** Customer, User
*   **Mô tả:** Cho phép người dùng lấy lại mật khẩu khi quên bằng link xác minh thời gian giới hạn gửi qua Email.
*   **Preconditions:** Tài khoản Email đã đăng ký tồn tại trên hệ thống.
*   **Basic Flow:**
    1. Người dùng nhấn "Quên mật khẩu", nhập Email.
    2. Hệ thống tạo Token reset và gửi Email chứa đường dẫn đổi mật khẩu (hạn dùng 15 phút).
    3. Người dùng click vào đường dẫn, nhập mật khẩu mới.
    4. Hệ thống cập nhật mật khẩu mới đã hash BCrypt vào Database.
*   **Exceptions:**
    *   Email không tồn tại.
    *   Token reset hết hạn hoặc không hợp lệ.

### UC04 — Quản lý hồ sơ cá nhân & Tải giấy tờ định danh (CCCD/Passport)
*   **Actor:** Customer
*   **Mô tả:** Khách cập nhật thông tin cá nhân và tải ảnh giấy tờ tùy thân định danh (mã hóa dữ liệu nhạy cảm PII bằng AES-256).
*   **Preconditions:** Khách hàng đã đăng nhập.
*   **Basic Flow:**
    1. Khách hàng vào phần "Hồ sơ cá nhân", điền thông tin và upload ảnh CCCD.
    2. Hệ thống thực hiện mã hóa dữ liệu nhạy cảm trước khi lưu xuống Database.
    3. Hiển thị thông báo lưu thành công.
*   **Exceptions:**
    *   File ảnh định dạng không đúng (chỉ cho phép JPG/PNG) hoặc vượt quá dung lượng cho phép.

### UC05 — Quản lý tài khoản nhân viên & Kiểm soát rủi ro
*   **Actor:** Admin
*   **Mô tả:** Quản lý vòng đời tài khoản nhân viên (Lễ tân, Bếp, Hướng dẫn viên) và truy vết Audit Log.
*   **Preconditions:** Tài khoản có quyền Admin.
*   **Basic Flow:**
    1. Admin vào "Quản lý nhân sự", thực hiện CRUD nhân viên và gán Role.
    2. Mọi tác vụ nghiệp vụ quan trọng đều ghi log tự động vào bảng `AuditLogs`.
*   **Exceptions:**
    *   Tự xóa tài khoản của chính mình (chặn phía server).

### UC06 — Quản lý Core Data & Giá phòng động
*   **Actor:** Admin, Manager
*   **Mô tả:** Quản lý giá phòng, thiết lập cấu hình hệ thống và thiết lập quy tắc giá động theo mùa/ngày lễ.
*   **Preconditions:** Đăng nhập quyền Admin/Manager.
*   **Basic Flow:**
    1. Manager chọn hạng phòng, cấu hình quy tắc tăng/giảm giá theo ngày cụ thể.
    2. Hệ thống áp dụng công thức tính toán giá phòng tự động khi khách đặt phòng.

### UC07 — Ẩn danh hóa dữ liệu cá nhân (Quyền được quên)
*   **Actor:** Customer, Admin
*   **Mô tả:** Thực hiện ẩn danh hóa thông tin cá nhân (họ tên, CCCD, sđt) của khách hàng theo Luật bảo vệ dữ liệu cá nhân nhưng giữ lại lịch sử giao dịch kế toán.
*   **Preconditions:** Có yêu cầu từ khách hàng và được Admin/DPO phê duyệt.
*   **Basic Flow:**
    1. Hệ thống băm (hash) toàn bộ thông tin PII của Customer thành chuỗi vô nghĩa.
    2. Các liên kết hóa đơn tài chính được giữ nguyên ID nhưng không còn thông tin cá nhân plaintext.

### UC08 — Tự động đăng xuất (Session Timeout)
*   **Actor:** System
*   **Mô tả:** Tự động hủy session làm việc của người dùng khi không phát sinh tương tác sau một khoảng thời gian thiết lập trước (ví dụ 30 phút).

---

## MODULE 2: QUẢN LÝ PHÒNG & LỄ TÂN VẬN HÀNH

### UC09 — Tìm kiếm phòng trống thời gian thực
*   **Actor:** Customer, Guest
*   **Mô tả:** Tìm kiếm hạng phòng khả dụng theo khoảng ngày Check-in/Check-out.
*   **Preconditions:** Ngày Check-in phải lớn hơn hoặc bằng ngày hiện tại.
*   **Basic Flow:**
    1. Khách nhập ngày nhận, ngày trả và số lượng người.
    2. Hệ thống lọc phòng trống, áp dụng công thức giá động và trả về danh sách kèm hình ảnh.

### UC10 — Đặt phòng & Đặt cọc trực tuyến
*   **Actor:** Customer
*   **Mô tả:** Đặt hạng phòng, áp dụng mã giảm giá và thanh toán tiền cọc qua cổng VNPay.
*   **Preconditions:** Đã chọn phòng trống ở UC09.
*   **Basic Flow:**
    1. Khách nhập thông tin liên hệ, mã giảm giá (nếu có).
    2. Hệ thống áp dụng khóa Pessimistic Lock, chuyển hướng sang cổng VNPay.
    3. Nhận callback từ VNPay: Cập nhật trạng thái `Confirmed`, gửi email xác nhận.
*   **Exceptions:**
    *   Giao dịch VNPay bị hủy hoặc lỗi cổng thanh toán.
    *   Overbooking do người khác đã đặt phòng đó trước 1 giây (được hoàn tiền/chặn đặt).

### UC11 — Xem sơ đồ Matrix phòng trống (Front Desk Dashboard)
*   **Actor:** Receptionist
*   **Mô tả:** Lễ tân xem trực quan tình trạng tất cả các phòng vật lý tại resort theo thời gian thực (Trống, Sạch, Bẩn, Đang sửa).

### UC12 — Check-in / Check-out / Đổi phòng
*   **Actor:** Receptionist
*   **Mô tả:** Tiếp đón khách hàng, quét CCCD để hoàn tất thủ tục nhận phòng vật lý và xử lý đổi phòng khi có yêu cầu.
*   **Preconditions:** Booking tồn tại trên hệ thống.
*   **Basic Flow:**
    1. Khách trình CCCD, lễ tân thực hiện Check-in trên Matrix.
    2. Trạng thái phòng chuyển sang `Occupied`, hệ thống tự động khởi tạo tài khoản nợ `Folio` cho phòng.
    3. Đổi phòng: Chuyển dữ liệu nợ Folio sang phòng mới, cập nhật phòng cũ thành `Dirty`.

### UC13 — Quản lý dọn phòng & Bảo trì (Housekeeping/Maintenance)
*   **Actor:** Housekeeping Staff, Maintenance Staff
*   **Mô tả:** Nhân viên buồng phòng báo cáo trạng thái dọn dẹp (Sạch/Bẩn); Kỹ thuật sửa chữa báo trạng thái bảo trì phòng.
*   **Preconditions:** Lệnh dọn dẹp tự động sinh ra sau khi khách check-out.
*   **Basic Flow:**
    1. Dọn phòng cập nhật phòng từ `Dirty` thành `Clean` trên hệ thống.
    2. Phát hiện hỏng hóc: Chuyển phòng thành `Maintenance` và tạo phiếu sửa chữa.

---

## MODULE 3: DỊCH VỤ ẨM THỰC & NHÀ HÀNG (F&B / POS)

### UC14 — Đặt món Room Service (E-Menu trực tuyến)
*   **Actor:** Customer
*   **Mô tả:** Khách hàng quét mã QR tại phòng để đặt đồ ăn/nước uống mang tận phòng.
*   **Preconditions:** Khách đang ở phòng có trạng thái `Occupied`.
*   **Basic Flow:**
    1. Khách quét QR, xem menu, chọn món.
    2. Xác nhận phòng đang ở và đặt hàng.
    3. Thông tin gửi đến KDS bếp.

### UC15 — Đặt trước bàn ăn tại nhà hàng
*   **Actor:** Customer
*   **Mô tả:** Đặt trước bàn ăn theo khung giờ mong muốn tại nhà hàng của resort.
*   **Basic Flow:** Khách chọn ngày giờ, số lượng khách, số bàn và thanh toán cọc giữ bàn (nếu có).

### UC16 — Gọi món tại quầy Dine-In
*   **Actor:** F&B Staff
*   **Mô tả:** Nhân viên nhà hàng ghi nhận order trực tiếp từ khách ăn tại chỗ thông qua thiết bị POS cầm tay.

### UC17 — KDS Bếp — Quản lý trạng thái món ăn
*   **Actor:** Kitchen Staff
*   **Mô tả:** Màn hình KDS hiển thị danh sách món ăn cần chế biến và cho phép báo hết món để tự động khóa trên hệ thống E-Menu/POS.

### UC18 — Ký nợ hóa đơn ăn uống về phòng (Post to Room)
*   **Actor:** F&B Staff
*   **Mô tả:** Khách ăn uống tại nhà hàng chọn thanh toán bằng cách ghi nợ vào phòng lưu trú để trả một lần khi checkout.
*   **Preconditions:** Phòng của khách đang `Occupied`, chưa vượt hạn mức chi tiêu `creditLimit`.
*   **Basic Flow:** Hệ thống kiểm tra số phòng, tạo bản ghi `FolioItem` ghi nợ trực tiếp vào hóa đơn Folio của phòng đó.

---

## MODULE 4: QUẢN LÝ LỮ HÀNH & ĐÁNH GIÁ

### UC19 — Tìm kiếm gói tour (Tích hợp API thời tiết)
*   **Actor:** Customer, Guest
*   **Mô tả:** Tìm kiếm tour và hiển thị thông tin thời tiết thời gian thực tại địa điểm tham quan.
*   **Basic Flow:**
    1. Người dùng tìm kiếm tour.
    2. Hệ thống gọi Weather API lấy thông tin thời tiết điểm đến.
    3. Trả về thông tin tour kèm thời tiết (nếu API lỗi, vẫn trả về danh sách tour và ẩn thời tiết - Fail-safe).

### UC20 — Đặt tour & Lập lịch / Hủy tour
*   **Actor:** Customer, Admin, Tour Guide
*   **Mô tả:** Khách đặt tour trực tuyến; Admin gán hướng dẫn viên/tài xế cho chuyến xe; Xử lý hủy chuyến do sự cố.
*   **Basic Flow:**
    1. Đặt tour thành công -> Cập nhật `bookedSeats` trên `TourSchedule` (dùng Pessimistic Lock).
    2. Hủy tour do sự cố (chủ quan/khách quan) -> Tính toán tiền hoàn trả theo chính sách hệ thống.

### UC21 — Điểm danh đoàn tour bằng AI quét mặt (FaceID)
*   **Actor:** Tour Guide, Customer
*   **Mô tả:** Điểm danh sinh trắc học khuôn mặt nhanh chóng tại xe tour hoặc quầy.
*   **Basic Flow:**
    1. Hướng dẫn viên bật camera quét mặt khách hàng.
    2. Gửi ảnh base64 đến Python AI service so khớp confidence >= 0.6.
    3. Khớp thành công -> Cập nhật trạng thái `TourAttendee` thành `Present`.
*   **Alternative Flow:** Điểm danh thủ công (Manual Fallback) tích chọn bằng tay khi AI lỗi/mất mạng.

### UC22 — Gửi đánh giá bằng sao (Review)
*   **Actor:** Customer
*   **Mô tả:** Khách hàng gửi đánh giá từ 1-5 sao kèm nhận xét về chất lượng phòng hoặc tour đã trải nghiệm.
*   **Preconditions:** Khách hàng đã thực hiện sử dụng dịch vụ tương ứng.

### UC23 — Kiểm duyệt đánh giá của khách
*   **Actor:** Admin
*   **Mô tả:** Tự động lọc từ ngữ thô tục hoặc cho phép Admin ẩn các đánh giá spam/toxic trên giao diện public.

---

## MODULE 5: KIỂM TOÁN ĐÊM, TÀI CHÍNH & BÁO CÁO

### UC24 — Folio Aggregation — Gom hóa đơn tự động
*   **Actor:** System, Receptionist
*   **Mô tả:** Tự động gom mọi hóa đơn phát sinh lẻ (tiền phòng, đồ ăn uống F&B, tour dịch vụ) vào tài khoản hóa đơn tổng của phòng (Folio).
*   **Night Audit:** Chốt sổ giao dịch ngày tự động chạy lúc 02:00 AM mỗi ngày, tự động tính tiền phòng hôm đó vào Folio và tiến Business Date lên 1 ngày.

### UC25 — Tất toán & e-Invoice
*   **Actor:** Receptionist, System
*   **Mô tả:** Xử lý thanh toán số dư nợ Folio khi check-out và tự động gửi hóa đơn điện tử e-Invoice qua email khách hàng.
*   **Preconditions:** Số dư nợ Folio phải bằng 0 (`Settled`) trước khi cho phép xác nhận Check-out thành công.

### UC26 — Dashboard Manager (Báo cáo trực quan)
*   **Actor:** Manager
*   **Mô tả:** Giám sát biểu đồ tài chính thực tế, công suất phòng hiện tại (Occupancy Rate) và số lượng khách lưu trú tại resort.

### UC27 — Xuất báo cáo tài chính vận hành chuẩn USALI
*   **Actor:** Manager
*   **Mô tả:** Hệ thống kết xuất báo cáo tài chính phân loại theo 3 mã doanh thu cốt lõi (Phòng nghỉ, F&B, Dịch vụ khác/Tour) theo chuẩn kế toán khách sạn quốc tế USALI.

### UC28 — Kết xuất báo cáo PDF/Excel
*   **Actor:** Manager
*   **Mô tả:** Xuất file báo cáo tài chính dạng PDF hoặc Excel từ hệ thống để lưu trữ và kiểm toán.
