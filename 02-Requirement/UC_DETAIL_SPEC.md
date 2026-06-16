# ĐẶC TẢ CHI TIẾT USE CASE (USE CASE DETAIL SPECIFICATIONS)

## HỆ THỐNG QUẢN LÝ NGHỈ DƯỠNG KAWAI RETREAT RESORT & HUB

| Field          | Value                                 |
| -------------- | ------------------------------------- |
| **Document ID**| `KAWAI-SRS-UC-DET-001`                |
| **Version**    | 3.2 (Đồng bộ UC_MASTER_TABLE V3)      |
| **Date**       | 2026-06-16                            |
| **Status**     | Approved                              |
| **Author**     | Nhóm Phát Triển SWP391 - G2           |
| **Reviewed by**| Tech Lead                             |

---

## CHANGELOG

| Ngày | Người thực hiện | Nội dung thay đổi |
| 2026-06-16 | Antigravity | Cập nhật UC11 bổ sung luồng đặt món không bắt buộc đăng nhập trước, hiển thị Auth Modal khi đặt hàng và điều hướng thông minh. Sửa đồng bộ liên kết logo HoaNien về trang chủ. |
| 2026-06-16 | Antigravity | Đồng bộ hóa toàn diện tài liệu đặc tả chi tiết 25 Use Cases, bổ sung chi tiết I/O, API Endpoint, validation và tác động DB phục vụ Code Generator |
| 2026-06-13 | Nhóm Phát Triển | Khởi tạo tài liệu đặc tả chi tiết cho toàn bộ 28 Use Cases cũ |

---

## MỤC LỤC
* [🔴 MODULE 1: HỆ THỐNG CỐT LÕI, XÁC THỰC & TÀI KHOẢN](#module-1-he-thong-cot-loi-xac-thuc-tai-khoan)
* [🔵 MODULE 2: QUẢN LÝ PHÒNG & LỄ TÂN VẬN HÀNH](#module-2-quan-ly-phong-le-tan-van-hanh)
* [🟡 MODULE 3: DỊCH VỤ ẨM THỰC & NHÀ HÀNG (F&B / POS / KDS)](#module-3-dich-vu-am-thuc-nha-hang-fb-pos-kds)
* [🟢 MODULE 4: QUẢN LÝ LỮ HÀNH & ĐÁNH GIÁ](#module-4-quan-ly-lu-hanh-danh-gia)
* [🟣 MODULE 5: KIỂM TOÁN ĐÊM, TÀI CHÍNH & BÁO CÁO](#module-5-kiem-toan-dem-tai-chinh-bao-cao)

---

## 🔴 MODULE 1: HỆ THỐNG CỐT LÕI, XÁC THỰC & TÀI KHOẢN

### **UC01 — Đăng ký & Đăng nhập (BCrypt)**

#### **UC01.1 — Đăng ký tài khoản khách hàng trực tuyến**
*   **Actor:** Customer (Guest)
*   **Mô tả:** Khách hàng vãng lai tạo tài khoản trực tuyến trên website để thực hiện giao dịch đặt phòng, đặt tour.
*   **Preconditions:** Thiết bị kết nối Internet, truy cập trang đăng ký `/register`.
*   **Postconditions:** Tạo thành công bản ghi Account và Customer. Gửi email chào mừng.
*   **Luồng xử lý chính (Basic Flow):**
    1. Khách hàng điền form đăng ký trực tuyến gửi dữ liệu POST tới `/api/auth/register`.
    2. Server nhận DTO, thực hiện Validate định dạng Email, Phone (Regex) và Password (tối thiểu 8 ký tự, có hoa, thường, số).
    3. Service kiểm tra trùng lặp `username`, `email`, `phone` trong bảng `Accounts` và `Customers`.
    4. Service dùng BCrypt băm mật khẩu: `password_hash = BCrypt.hashpw(password, BCrypt.gensalt(10))`.
    5. Lưu bản ghi vào bảng `Accounts` (trạng thái `is_active = true`, `role_id = 9` - CUSTOMER) và bảng `Customers` (điểm tích lũy = 0, hạng = 'Regular').
    6. Kích hoạt Mail Service gửi email chào mừng và chuyển hướng người dùng về trang `/login`.
*   **Luồng ngoại lệ (Exceptions):**
    *   *EX1 (Trùng lặp dữ liệu):* Username/Email/Phone đã tồn tại -> Trả về JSON lỗi `400 Bad Request` kèm chi tiết trường bị trùng.
    *   *EX2 (Lỗi Validate):* Dữ liệu sai định dạng -> Trả về mã lỗi `400 Bad Request` chứa thông điệp lỗi của từng trường.

#### **UC01.2 — Đăng nhập hệ thống (Form truyền thống & OAuth2 Google)**
*   **Actor:** Customer, User (Nhân viên, Lễ tân, Quản lý, Admin)
*   **Mô tả:** Đăng nhập hệ thống để thực hiện các chức năng nghiệp vụ tương ứng theo phân quyền.
*   **Preconditions:** Tài khoản đã được kích hoạt hoạt động.
*   **Postconditions:** Tạo lập phiên làm việc (Session) hoặc cấp mã JWT Token cho client.
*   **Luồng xử lý chính (Basic Flow):**
    *   *Đăng nhập truyền thống:*
        1. Người dùng gửi thông tin đăng nhập POST tới `/api/auth/login`.
        2. Spring Security `AuthenticationManager` tiếp nhận, gọi `UserDetailsService` để tải thông tin Account và Role tương ứng.
        3. So sánh mật khẩu bằng `BCryptPasswordEncoder`. Nếu khớp, cấp phát Token JWT hoặc lưu trữ Auth Session.
        4. Ghi nhận log đăng nhập vào `Audit_Logs`. Redirect về màn hình Dashboard tương ứng với Role.
    *   *Đăng nhập OAuth2 Google:*
        1. Khách hàng click "Đăng nhập với Google" -> Chuyển hướng tới Google Auth Server.
        2. Sau khi xác thực, hệ thống nhận về OAuth2 Authorization Code tại `/login/oauth2/code/google`.
        3. Server gửi yêu cầu lấy thông tin Profile Google (Email, Name).
        4. Kiểm tra Email trong DB. Nếu chưa tồn tại, tự động tạo mới bản ghi `Accounts` (is_active = true, role = CUSTOMER) và `Customers` với mật khẩu ngẫu nhiên băm BCrypt.
        5. Cấp quyền truy cập tương tự luồng truyền thống.
*   **Luồng ngoại lệ (Exceptions):**
    *   *EX1 (Sai thông tin):* Sai username hoặc password -> Trả về lỗi `401 Unauthorized` kèm thông báo "Sai tài khoản hoặc mật khẩu".
    *   *EX2 (Tài khoản bị khóa):* Cột `is_active = false` -> Trả về lỗi `403 Forbidden` thông báo "Tài khoản đang tạm ngưng hoạt động".

---

### **UC02 — Đặt lại mật khẩu (Gửi Mail chứa Token giới hạn thời gian)**
*   **Actor:** Customer, User
*   **Mô tả:** Cho phép người dùng lấy lại mật khẩu khi quên bằng đường link xác nhận gửi qua email.
*   **Preconditions:** Email yêu cầu phải tồn tại trong CSDL.
*   **Postconditions:** Đổi mật khẩu thành công, vô hiệu hóa Token.
*   **Luồng xử lý chính (Basic Flow):**
    1. Người dùng nhấn nút "Quên mật khẩu" tại `/forgot-password`, điền Email nhận link.
    2. Server nhận Email, sinh Token ngẫu nhiên dạng UUID. Lưu thông tin Token vào DB/Cache kèm thời gian hết hạn (15 phút kể từ thời điểm tạo).
    3. Gọi Mail Service gửi email chứa link xác minh dạng: `/reset-password?token=UUID_TOKEN`.
    4. Người dùng click vào link, hệ thống validate token còn hạn và chưa sử dụng -> Trả về view nhập mật khẩu mới.
    5. Người dùng gửi mật khẩu mới POST `/api/auth/reset-password`. Server băm mật khẩu mới bằng BCrypt, cập nhật cột `password_hash` của Account tương ứng và xóa bỏ Token trong DB.
*   **Luồng ngoại lệ (Exceptions):**
    *   *EX1:* Email nhập vào không tồn tại -> Trả về lỗi `404 Not Found`.
    *   *EX2:* Token không tồn tại hoặc đã quá 15 phút hiệu lực -> Trả về lỗi `400 Bad Request` báo "Mã xác nhận không hợp lệ hoặc đã hết hạn".

---

### **UC03 — Quản lý thông tin hồ sơ & Định danh FaceID**

#### **UC03.1 — Quản lý hồ sơ cá nhân & Mã hóa giấy tờ (AES-256)**
*   **Actor:** Customer
*   **Mô tả:** Khách hàng cập nhật thông tin cá nhân và số giấy tờ định danh (CCCD/Passport) được mã hóa bảo mật.
*   **Preconditions:** Người dùng đã đăng nhập hệ thống.
*   **Postconditions:** Thông tin hồ sơ được cập nhật, tài liệu CCCD được mã hóa an toàn trong DB.
*   **Luồng xử lý chính (Basic Flow):**
    1. Khách hàng gửi yêu cầu PUT tới `/api/customer/profile`.
    2. Server nhận thông tin DTO, thực hiện mã hóa cột `cccd_passport_encrypted` bằng thuật toán AES-256 sử dụng khóa bí mật (Secret Key) cấu hình ở môi trường.
    3. Thực hiện câu lệnh UPDATE dữ liệu vào bảng `Customers`.
    4. Trả về thông báo thành công. Khi hiển thị ra ngoài, thông tin CCCD sẽ được giải mã và che đi phần lớn số (masking, e.g. `123xxxxxxx56`).
*   **Tác động Database:** `Customers` (UPDATE).

#### **UC03.2 — Đăng tải, trích xuất dữ liệu khuôn mặt và chuyển đổi Vector**
*   **Actor:** Customer, Lễ tân
*   **Mô tả:** Khách hàng tải ảnh chân dung lên để phục vụ điểm danh tự động bằng FaceID khi tham gia Tour.
*   **Preconditions:** Đã đăng nhập tài khoản.
*   **Postconditions:** Dữ liệu Vector khuôn mặt (128 số) được lưu trữ thành công.
*   **Luồng xử lý chính (Basic Flow):**
    1. Khách hàng chụp ảnh/upload ảnh chân dung gửi POST `/api/customer/face-upload` (Multipart File).
    2. Server nhận file ảnh, thực hiện kiểm tra định dạng và dung lượng (< 5MB) -> Chuyển tiếp ảnh qua API REST của microservice Python `kawai-ai-service` tại `/extract-features`.
    3. Dịch vụ AI (Python FastAPI) dùng thư viện dlib/OpenCV phát hiện vị trí khuôn mặt, trích xuất vector embedding (128 số float). Trả về mảng số dạng JSON.
    4. Server Spring Boot tiếp nhận mảng số, chuyển thành chuỗi JSON String và lưu vào cột `face_vector_data` của khách hàng.
*   **Luồng ngoại lệ (Exceptions):**
    *   *EX1 (Ảnh lỗi):* Microservice không tìm thấy khuôn mặt hoặc ảnh quá mờ -> Trả về lỗi `400 Bad Request` báo "Không nhận diện được khuôn mặt, vui lòng thử lại bằng ảnh rõ nét hơn".

---

### **UC04 — Quản lý tài khoản nhân viên & Kiểm soát rủi ro**

#### **UC04.1 — Quản lý tài khoản & Phân quyền RBAC (Lễ tân, Bếp, Guide)**
*   **Actor:** Admin
*   **Mô tả:** Admin thực hiện CRUD tài khoản nhân sự và phân chia quyền hạn theo vai trò công việc.
*   **Preconditions:** Đăng nhập quyền Admin.
*   **Luồng xử lý chính (Basic Flow):**
    1. Admin truy cập màn hình `/admin/employees` gửi yêu cầu POST (tạo mới) hoặc PUT (sửa) nhân sự.
    2. Server thực hiện tạo bản ghi trong `Accounts` và `Employees`.
    3. Gán Role liên kết tương ứng (`role_id` trỏ tới `Roles` tương ứng: `RECEPTIONIST`, `FB_POS_STAFF`, `TOUR_GUIDE`...).
    4. Spring Security tự động phân cấp quyền truy cập dựa trên Authority được nạp từ Role của tài khoản.
*   **Tác động Database:** `Accounts` (INSERT/UPDATE), `Employees` (INSERT/UPDATE).

#### **UC04.2 — Truy vết lịch sử nhật ký hệ thống (Audit Log) chống gian lận**
*   **Actor:** Admin
*   **Mô tả:** Hệ thống tự động ghi nhật ký mọi thao tác thay đổi dữ liệu nhạy cảm để Admin giám sát.
*   **Preconditions:** Các hoạt động INSERT/UPDATE/DELETE nhạy cảm diễn ra.
*   **Luồng xử lý chính (Basic Flow):**
    1. Sử dụng Spring AOP (Aspect Oriented Programming) hoặc Database Trigger để bắt các sự kiện chỉnh sửa CSDL (như đổi giá phòng, điều chỉnh nợ folio, xóa order...).
    2. Chụp thông tin: Tên bảng, ID bản ghi, Giá trị cũ (Old Value JSON), Giá trị mới (New Value JSON), Tài khoản thực hiện, Địa chỉ IP, Thời điểm chính xác.
    3. Thực hiện INSERT bản ghi vào bảng `Audit_Logs`.
    4. Admin xem danh sách log tại `/admin/audit-logs`.
*   **Ràng buộc:** Bảng `Audit_Logs` chặn hoàn toàn lệnh UPDATE và DELETE từ tất cả các actor bao gồm cả Admin.

---

### **UC05 — Quản lý Cấu hình Hệ thống (Core Data Config)**

#### **UC05.1 — Quản lý dữ liệu nền (Thêm/Sửa/Xóa cấu hình phòng, bàn, tour)**
*   **Actor:** Admin
*   **Mô tả:** Quản lý danh mục sản phẩm cốt lõi của resort.
*   **Preconditions:** Tài khoản Admin.
*   **Luồng xử lý chính (Basic Flow):**
    1. Admin thực hiện CRUD dữ liệu phòng (`Rooms`), bàn ăn (`Restaurant_Tables`), các tour (`Tours`) qua các endpoint `/api/admin/...`.
    2. Trước khi thực thi lệnh DELETE, Service kiểm tra khóa ngoại: Nếu tồn tại Booking/Đặt bàn/Đặt tour liên quan đang hoạt động -> Chặn xóa và thông báo lỗi.
*   **Tác động Database:** `Rooms`, `Restaurant_Tables`, `Tours`, `Menu_Items` (INSERT/UPDATE/DELETE).

#### **UC05.2 — Cấu hình chiến dịch giá phòng động biến động (Mùa/Lễ tết)**
*   **Actor:** Admin, Manager
*   **Mô tả:** Cấu hình tự động tăng/giảm giá phòng theo khoảng thời gian thực tế (ngày lễ, cuối tuần).
*   **Preconditions:** Đăng nhập quyền Admin/Manager.
*   **Luồng xử lý chính (Basic Flow):**
    1. Manager gửi yêu cầu POST `/api/admin/pricing-rules` thiết lập quy tắc giá động (Hạng phòng, Khoảng ngày, số tiền chênh lệch `price_modifier`, lý do).
    2. Service thực hiện lưu quy tắc vào `Dynamic_Pricing`.
    3. Hệ thống chạy ngầm tự động tính toán lại giá thực tế từng ngày cụ thể và cập nhật vào bảng dữ liệu tĩnh `Daily_Rates` để phục vụ tối ưu câu lệnh tìm kiếm phòng trống.
*   **Ràng buộc:** Khoảng ngày áp dụng không được phép giao nhau giữa các quy tắc của cùng một hạng phòng.

#### **UC05.3 — Thiết lập chiến dịch Marketing & Đóng gói Combo dạng JSON**
*   **Actor:** Admin, Manager
*   **Mô tả:** Tạo mới Voucher và đóng gói các combo phòng nghỉ tích hợp dịch vụ ẩm thực, tour du lịch.
*   **Luồng xử lý chính (Basic Flow):**
    1. Tạo chương trình khuyến mãi, sinh code lưu vào bảng `Promotions`.
    2. Tạo Combo dịch vụ: Nhập thông số combo phòng nghỉ kèm tour đi kèm -> Hệ thống chuyển đổi cấu hình combo thành định dạng JSON string (ví dụ: `{"room_category_id": 1, "days": 3, "tours": [2]}`) lưu vào DB. Khi khách hàng mua combo, hệ thống tự động bóc tách chuỗi JSON để đặt phòng và giữ chỗ tour tự động.

---

## 🔵 MODULE 2: QUẢN LÝ PHÒNG & LỄ TÂN VẬN HÀNH

### **UC06 — Tìm kiếm hạng phòng trống thời gian thực theo bộ lọc**
*   **Actor:** Customer, Guest
*   **Mô tả:** Tìm kiếm và hiển thị các hạng phòng còn trống kèm đơn giá thực tế trong khoảng ngày lưu trú.
*   **Preconditions:** Ngày nhận phòng và ngày trả phòng hợp lệ.
*   **Luồng xử lý chính (Basic Flow):**
    1. Người dùng gửi yêu cầu GET tới `/api/rooms/search` kèm `checkInDate`, `checkOutDate` và `guestsCount`.
    2. Service truy xuất danh sách `Room_Booking_Details` để tìm các phòng vật lý đã được gán hoặc đặt trước trùng lịch (ngày lưu trú giao nhau).
    3. Tính toán số lượng phòng trống thực tế của từng hạng phòng: `Số phòng trống = Tổng số phòng vật lý của hạng - Số phòng đã đặt`.
    4. Tính tổng tiền tiền phòng bằng cách cộng dồn đơn giá từng ngày từ bảng `Daily_Rates`.
    5. Trả về danh sách hạng phòng còn khả dụng kèm thông tin chi tiết.
*   **Dữ liệu Đầu vào (Inputs):** `checkInDate` (Date), `checkOutDate` (Date), `guestsCount` (Integer).
*   **Dữ liệu Đầu ra (Outputs):** Danh sách hạng phòng trống và đơn giá tổng thể.

---

### **UC07 — Đặt phòng & Quản lý quỹ giao dịch trực tuyến**

#### **UC07.1 — Đặt phòng & Đặt cọc trực tuyến đa hạng (Tích hợp VNPay)**
*   **Actor:** Customer
*   **Mô tả:** Khách hàng đặt phòng trực tuyến và thanh toán cọc thông qua cổng VNPay để xác nhận giữ phòng.
*   **Preconditions:** Đã chọn được phòng trống khả dụng từ UC06.
*   **Luồng xử lý chính (Basic Flow):**
    1. Khách điền thông tin người ở, áp voucher -> Gửi POST `/api/bookings/create`.
    2. Hệ thống kiểm tra lại phòng trống thời gian thực (Sử dụng khóa lạc quan `@Version` trên `Bookings` hoặc khóa bi quan `SELECT FOR UPDATE` trên Rooms).
    3. Tạo bản ghi Booking trạng thái `Pending`, sinh hạn chót thanh toán (Cart Lock 15 phút).
    4. Gọi API VNPay Sandbox sinh Payment URL gửi trả về Client để chuyển hướng người dùng sang cổng thanh toán.
    5. VNPay Webhook (IPN) gửi kết quả thanh toán về `/api/bookings/vnpay-callback`.
    6. So khớp chữ ký bảo mật (checksum). Nếu thanh toán thành công -> Cập nhật Booking sang trạng thái `Confirmed`, sinh bản ghi giao dịch trong `Payment_Transactions`, gửi email thông báo và giải phóng Cart Lock.
*   **Luồng ngoại lệ (Exceptions):**
    *   *EX1 (Thanh toán thất bại / Hủy):* VNPay trả về mã lỗi hoặc quá 15 phút không thanh toán -> Hủy Booking, giải phóng phòng bị giữ.

#### **UC07.2 — Áp dụng mã chiến dịch khuyến mãi / Voucher giảm giá**
*   **Actor:** Customer
*   **Mô tả:** Áp dụng Voucher giảm giá khi đặt phòng.
*   **Luồng xử lý chính (Basic Flow):**
    1. Khách điền mã Voucher trong form thanh toán.
    2. Hệ thống kiểm tra: Voucher tồn tại, `is_active = true`, ngày hiện hành nằm trong khoảng hiệu lực, số lượt sử dụng chưa đạt trần `max_uses`.
    3. Tính toán mức tiền được giảm (theo phần trăm hoặc số tiền cố định) -> Trừ trực tiếp vào tổng tiền Booking.

#### **UC07.3 — Kích hoạt khóa quỹ giữ phòng tạm thời (Room Cart Lock 15')**
*   **Actor:** System
*   **Mô tả:** Giữ phòng tạm thời trong thời gian khách thực hiện giao dịch thanh toán để tránh Overbooking.
*   **Luồng xử lý chính (Basic Flow):**
    1. Khi khách nhấn thanh toán đặt phòng, hệ thống ghi nhận mã giữ phòng kèm thời hạn hết hạn là `15 phút`.
    2. Trong 15 phút này, số lượng phòng trống khả dụng của hạng phòng đó khi tìm kiếm sẽ tự động trừ đi số phòng đang bị giữ.
    3. Hệ thống chạy Scheduler tự động quét mỗi 1 phút: Tìm các Booking nháp (`Pending`) đã quá 15 phút không phát sinh thanh toán thành công -> Đánh dấu hủy đơn và giải phóng quỹ phòng về CSDL.

---

### **UC08 — Xem sơ đồ trạng thái phòng thời gian thực (Front Desk Dashboard)**
*   **Actor:** Receptionist
*   **Mô tả:** Giao diện lưới Room Matrix hiển thị trực quan thông tin trạng thái toàn bộ phòng vật lý tại Resort phục vụ Lễ tân sảnh.
*   **Luồng xử lý chính (Basic Flow):**
    1. Lễ tân truy cập màn hình sơ đồ phòng `/staff/room-matrix`.
    2. Server trả về danh sách tất cả các phòng vật lý (`Rooms`).
    3. Mỗi phòng hiển thị: Số phòng, Trạng thái buồng phòng (`Vacant_Clean`, `Vacant_Dirty`), Trạng thái lưu trú (`Occupied`, `Maintenance`), và liên kết thông tin khách đang ở (nếu có).
    4. Trạng thái phòng tự động cập nhật thời gian thực không cần reload trang nhờ kết nối WebSockets.

---

### **UC09 — N nghiệp vụ Sảnh (Check-in / Check-out / Điều phối đoàn)**

#### **UC09.1 — Quy trình Check-In sảnh (Quét OCR CCCD tự động điền form)**
*   **Actor:** Receptionist
*   **Mô tả:** Tiếp đón khách hàng tại sảnh và hoàn tất thủ tục nhận phòng vật lý nhanh chóng bằng công nghệ quét ảnh OCR CCCD.
*   **Preconditions:** Đơn đặt phòng có trạng thái `Confirmed`.
*   **Luồng xử lý chính (Basic Flow):**
    1. Lễ tân lấy CCCD của khách lưu trú, thực hiện tải ảnh CCCD lên endpoint `/api/staff/ocr-scan`.
    2. Server gửi ảnh tới API dịch vụ OCR -> Trích xuất dữ liệu: Số CCCD, Họ tên, Ngày sinh, Quê quán, Giới tính -> Trả về form điền sẵn.
    3. Lễ tân kiểm tra, chọn phòng vật lý trống sạch (`Vacant_Clean`) trên sơ đồ để gán cho khách.
    4. Cập nhật `Room_Booking_Details.detail_status = 'Checked_In'`, gán `room_id`.
    5. Trigger `TRG_Auto_Housekeeping_Task` (hoặc logic code) tự động cập nhật trạng thái phòng vật lý thành `Occupied`.
    6. Hệ thống tạo tài khoản nợ `Folio` liên kết với phòng để ghi nợ dịch vụ.

#### **UC09.2 — Ủy quyền hạn mức chi tiêu ví nợ phòng phát sinh**
*   **Actor:** Receptionist
*   **Mô tả:** Cho phép khách lưu trú ký nợ dịch vụ (ẩm thực, tour) vào phòng nghỉ để thanh toán một lần khi trả phòng.
*   **Preconditions:** Khách đã làm thủ tục nhận phòng (Checked-In).
*   **Luồng xử lý chính (Basic Flow):**
    1. Lễ tân cấu hình ví nợ phòng tại giao diện `/staff/folio-config`: Bật cờ `is_charge_to_room_allowed = true`, cài đặt hạn mức nợ con (`sub_credit_limit`).
    2. Khách hàng nhập mã PIN 4 số trên thiết bị mã hóa -> Server lưu mã pin đã băm vào `personal_pin_hash`.
    3. Kể từ thời điểm này, mọi đơn hàng ăn uống/tour tại resort có thể chọn hình thức ghi nợ phòng nếu nhập đúng mã PIN và không vượt quá hạn mức nợ trần.

#### **UC09.3 — Điều phối đổi phòng vật lý linh hoạt cho khách lưu trú**
*   **Actor:** Receptionist
*   **Mô tả:** Đổi phòng vật lý cho khách lưu trú khi có yêu cầu chuyển đổi phòng.
*   **Preconditions:** Phòng mới phải thuộc cùng hạng phòng của Booking và ở trạng thái trống sạch (`Vacant_Clean`).
*   **Luồng xử lý chính (Basic Flow):**
    1. Lễ tân chọn phòng hiện tại của khách -> Chọn tính năng Đổi phòng -> Chọn phòng mới trống trên sơ đồ.
    2. Hệ thống kiểm tra tính hợp lệ -> Cập nhật `room_id` mới vào `Room_Booking_Details`.
    3. Chuyển trạng thái phòng cũ thành `Vacant_Dirty` (cần dọn).
    4. Chuyển trạng thái phòng mới thành `Occupied`.
    5. Chuyển toàn bộ danh mục dư nợ Folio liên kết từ phòng cũ sang phòng mới. Ghi nhật ký vào Audit Log.

#### **UC09.4 — Khai báo lưu trú & Chỉ định người lớn đứng tên phòng (Primary Contact)**
*   **Actor:** Customer, Lễ tân
*   **Mô tả:** Khai báo đầy đủ danh sách hành khách ở chung trong một phòng và chỉ định người chịu trách nhiệm tài chính đại diện.
*   **Ràng buộc:** Bắt buộc phải có ít nhất một người lớn làm Primary Contact đứng tên hóa đơn cho mỗi phòng lưu trú.

---

### **UC10 — Tác vụ buồng phòng nội bộ & Bảo trì thiết bị (Housekeeping)**

#### **UC10.1 — Tự động phát lệnh tác vụ dọn phòng khi khách Check-out**
*   **Actor:** System
*   **Mô tả:** Tự động tạo việc dọn dẹp khi khách trả phòng nhằm tối ưu tốc độ quay vòng phòng.
*   **Preconditions:** Lễ tân bấm Check-out thành công cho phòng.
*   **Luồng xử lý chính (Basic Flow):**
    1. Database Trigger `TRG_Auto_Housekeeping_Task` phát hiện trạng thái phòng chi tiết đổi sang `Checked_Out`.
    2. Trigger tự động cập nhật trạng thái phòng vật lý tại bảng Rooms thành `Vacant_Dirty`.
    3. Tự động INSERT bản ghi tác vụ mới vào bảng `Hotel_Operations` (loại `HOUSEKEEPING`, trạng thái `Pending`, độ ưu tiên `High`).

#### **UC10.2 — Cập nhật tiến độ dọn dẹp trực tiếp trên App (Báo Sạch / Bẩn)**
*   **Actor:** Housekeeping
*   **Mô tả:** Nhân viên buồng phòng báo cáo hoàn thành dọn dẹp phòng để sảnh có thể giao phòng tiếp cho khách.
*   **Luồng xử lý chính (Basic Flow):**
    1. Nhân viên buồng xem danh sách tác vụ được phân công trên app di động `/staff/housekeeping`.
    2. Sau khi dọn sạch phòng vật lý, nhân viên bấm nút "Hoàn thành dọn dẹp".
    3. Server nhận yêu cầu, cập nhật trạng thái tác vụ trong `Hotel_Operations` thành `Completed`.
    4. Cập nhật trạng thái phòng vật lý thành `Vacant_Clean`.

#### **UC10.3 — Xem giám sát danh sách yêu cầu dọn, sửa phòng khẩn cấp**
*   **Actor:** Receptionist
*   **Mô tả:** Giám sát tiến độ buồng phòng tại sảnh và đẩy nhanh tiến độ dọn dẹp đối với phòng khách VIP (Rush Room).
*   **Luồng xử lý chính (Basic Flow):**
    1. Lễ tân mở giao diện sảnh buồng phòng.
    2. Click chọn phòng cần dọn gấp -> Nhấn "Rush Room" -> Hệ thống cập nhật độ ưu tiên tác vụ trong bảng `Hotel_Operations` thành `Urgent`, đưa tác vụ dọn phòng này lên vị trí đầu tiên trong danh sách chờ của nhân viên buồng.

#### **UC10.4 — Ghi nhận yêu cầu sửa chữa cơ sở vật chất (Báo hỏng thiết bị)**
*   **Actor:** Housekeeping
*   **Mô tả:** Nhân viên buồng phòng báo cáo trang thiết bị trong phòng bị hỏng hóc cần kỹ thuật can thiệp.
*   **Luồng xử lý chính (Basic Flow):**
    1. Nhân viên buồng phát hiện thiết bị hỏng -> Lên app tạo phiếu báo hỏng gửi POST `/api/operations/maintenance-report`.
    2. Hệ thống tạo tác vụ mới trong `Hotel_Operations` (loại `MAINTENANCE`, trạng thái `Pending`).
    3. Đồng thời cập nhật trạng thái phòng vật lý thành `Maintenance` để khóa phòng không cho thuê.

#### **UC10.5 — Khắc phục sự cố kỹ thuật phòng vật lý (Báo hoàn thành bảo trì)**
*   **Actor:** Maintenance
*   **Mô tả:** Nhân viên sửa chữa xác nhận đã khắc phục xong sự cố để mở khóa phòng đưa vào vận hành.
*   **Luồng xử lý chính (Basic Flow):**
    1. Nhân viên kỹ thuật tiếp nhận phiếu sửa chữa -> Tiến hành khắc phục sự cố phần cứng.
    2. Nhấn nút "Hoàn tất sửa chữa" trên app -> Cập nhật trạng thái tác vụ bảo trì thành `Completed`.
    3. Hệ thống tự động chuyển đổi trạng thái phòng vật lý trở lại thành `Vacant_Clean` (hoặc `Vacant_Dirty` tùy cấu hình) để lễ tân tiếp tục giao phòng.

---

## 🟡 MODULE 3: DỊCH VỤ ẨM THỰC & NHÀ HÀNG (F&B / POS / KDS)

### **UC11 — Đặt món trực tuyến (Room Service / E-Menu)**
*   **Actor:** Customer (Guest & Authenticated Customer)
*   **Mô tả:** Khách hàng duyệt thực đơn và đặt đồ ăn uống giao tận phòng nghỉ thông qua mã QR được dán tại phòng hoặc liên kết trực tuyến.
*   **Preconditions:** Không yêu cầu đăng nhập đối với việc xem thực đơn và thêm món vào giỏ hàng. Yêu cầu đăng nhập tài khoản khách hàng khi tiến hành đặt món (Place Order).
*   **Luồng xử lý chính (Basic Flow):**
    1. Khách truy cập trang đặt món `/order-food` (hoặc quét mã QR tại phòng). Hệ thống cho phép hiển thị đầy đủ danh sách món ăn, giá tiền, mô tả và phân loại món mà không bắt buộc đăng nhập ngay.
    2. Khách thực hiện chọn món, điều chỉnh số lượng và thêm vào giỏ hàng cá nhân.
    3. Khách nhấn nút "Xác nhận đặt món" (Place Order).
    4. Hệ thống kiểm tra trạng thái xác thực (`isLoggedIn` thông qua Thymeleaf / Session).
        *   **Trường hợp đã đăng nhập:** Hệ thống tiếp tục xử lý đơn hàng, kiểm tra số phòng lưu trú và ví nợ Folio phòng (nếu dùng hình thức Post to Room) hoặc cho phép thanh toán tiền mặt/chuyển khoản.
        *   **Trường hợp chưa đăng nhập:** Hệ thống chặn luồng đặt món và kích hoạt hiển thị cửa sổ đăng nhập dạng modal (`authRequiredModal`) ngay trên giao diện.
    5. Khách hàng bấm chọn đăng nhập trên Modal -> Chuyển hướng sang trang `/login?redirect_to=/order-food` để lưu trữ ngữ cảnh đặt hàng.
    6. Khách đăng nhập thành công -> Hệ thống tự động chuyển hướng khách trở lại trang `/order-food` để hoàn tất giao dịch đặt món mà không bị mất dữ liệu giỏ hàng.
*   **Luồng ngoại lệ (Exceptions):**
    *   *EX1:* Phòng đã check-out hoặc không cho phép ký nợ phòng -> Chặn tạo đơn và yêu cầu khách thanh toán bằng phương thức trực tuyến hoặc tiền mặt.
    *   *EX2:* Khách từ chối đăng nhập -> Modal đóng lại, giỏ hàng được giữ nguyên để khách tiếp tục tham khảo thực đơn.

---

### **UC12 — Đặt giữ trước bàn ăn tại sảnh nhà hàng của resort**
*   **Actor:** Customer
*   **Mô tả:** Khách hàng đăng ký giữ chỗ bàn ăn trước khi đến dùng bữa tại nhà hàng của Resort.
*   **Luồng xử lý chính (Basic Flow):**
    1. Khách hàng truy cập `/restaurant/booking`, điền form thông tin: Ngày dùng bữa, Khung giờ, Số người, Vị trí bàn mong muốn.
    2. Hệ thống kiểm tra tình trạng trống khả dụng của bàn vật lý trong khung giờ đó.
    3. Tạo bản ghi đặt bàn trong `Table_Reservations` (trạng thái `Confirmed`). Gửi email xác nhận kèm mã QR bàn ăn.

---

### **UC13 — Gọi món Dine-In tại quầy (Nhân viên POS lên đơn tại bàn)**
*   **Actor:** F&B Staff
*   **Mô tả:** Nhân viên phục vụ nhà hàng dùng Tablet POS ghi nhận món ăn trực tiếp khi phục vụ khách ăn tại chỗ.
*   **Luồng xử lý chính (Basic Flow):**
    1. Nhân viên phục vụ chọn số bàn trên giao diện POS sảnh ăn `/staff/pos`.
    2. Chọn các món ăn khách yêu cầu -> Nhấn "Gửi bếp" -> Hệ thống tạo đơn hàng `Food_Orders` (loại `Dine_In`) và các chi tiết `Food_Order_Details`.
    3. Kích hoạt WebSocket gửi order tức thì xuống KDS bếp.

---

### **UC14 — Màn hình nhà bếp KDS Real-time — Điều phối trạng thái món**

#### **UC14.1 — Theo dõi hiển thị vé gọi món nhà bếp tập trung (Màn hình KOT)**
*   **Actor:** Kitchen Staff
*   **Mô tả:** Bếp trưởng và đầu bếp xem danh sách hàng đợi các món cần nấu theo thời gian thực trên màn hình KDS bếp.
*   **Luồng xử lý chính (Basic Flow):**
    1. Giao diện KDS bếp tự động cập nhật hiển thị các vé món ăn mới gửi từ quầy POS hoặc Room Service dưới dạng các thẻ (KOT Card) được sắp xếp thứ tự ưu tiên thời gian gọi món.

#### **UC14.2 — Cập nhật tiến độ nấu nướng (Bếp bấm chuyển COOKING / READY)**
*   **Actor:** Kitchen Staff
*   **Mô tả:** Đầu bếp cập nhật tiến trình nấu của từng món ăn riêng biệt giúp nhân viên chạy bàn theo dõi kịp thời.
*   **Luồng xử lý chính (Basic Flow):**
    1. Đầu bếp click vào món ăn trên màn hình KDS để chuyển trạng thái dòng chi tiết order `Food_Order_Details.kot_status` từ `Pending` sang `Cooking`.
    2. Khi nấu xong, đầu bếp click chuyển sang `Ready` -> Hệ thống bắn thông báo real-time tới thiết bị POS của nhân viên chạy bàn để bê món phục vụ khách. Khi đã phục vụ xong, chuyển thành `Served`.

#### **UC14.3 — Kích hoạt báo hết món ăn (Tự động khóa thực đơn trên POS/Web)**
*   **Actor:** Kitchen Staff
*   **Mô tả:** Đầu bếp báo hết món trực tiếp trên KDS để hệ thống tự động tạm đóng món ăn trên menu.
*   **Luồng xử lý chính (Basic Flow):**
    1. Khi phát hiện nguyên liệu làm món đã hết, Đầu bếp click "Khóa món" tương ứng với món ăn.
    2. Hệ thống cập nhật `Menu_Items.is_available = false` -> Món ăn ngay lập tức bị vô hiệu hóa đặt hàng trên tất cả ứng dụng POS nhân viên và E-Menu của khách hàng lưu trú.

---

### **UC15 — Ký nợ hóa đơn ăn uống về phòng nghỉ — Post to Room (Gom Folio)**
*   **Actor:** F&B Staff
*   **Mô tả:** Thực hiện chuyển hóa đơn ăn uống nhà hàng vào tài khoản nợ của phòng nghỉ.
*   **Preconditions:** Khách hàng lưu trú đã đăng ký kích hoạt tính năng ký nợ phòng (UC09.2).
*   **Luồng xử lý chính (Basic Flow):**
    1. Khi khách ăn xong tại bàn chọn hình thức ký nợ phòng -> Nhân viên POS nhập số phòng của khách.
    2. Hệ thống kiểm tra trạng thái phòng phải là `Checked_In` và cờ cho phép nợ `is_charge_to_room_allowed = true`.
    3. Khách nhập mã PIN 4 số trên máy POS -> Hệ thống so khớp mã băm mật khẩu `personal_pin_hash`.
    4. Kiểm tra nợ trần: Tổng dư nợ Folio hiện tại + Tiền món nợ mới <= hạn mức nợ của phòng.
    5. Tạo bản ghi nợ trong `Folio_Items` (nguồn `source_department = 'FB'`, số tiền bằng giá đơn món).
    6. Trạng thái đơn hàng F&B được đóng ở trạng thái `Completed`, cờ `is_paid_in_pos` chuyển sang `false` (ghi nhận sẽ thu tiền lúc Check-out).
*   **Luồng ngoại lệ (Exceptions):**
    *   *EX1 (Vượt hạn mức):* Tổng nợ vượt quá hạn mức cho phép -> Database Trigger `TRG_Folio_Credit_Limit_Check` ném lỗi và từ chối ghi nhận giao dịch nợ, bắt buộc khách thanh toán bằng tiền mặt/thẻ trực tiếp tại POS nhà hàng.

---

## 🟢 MODULE 4: QUẢN LÝ LỮ HÀNH & ĐÁNH GIÁ

### **UC16 — Tìm kiếm hành trình trải nghiệm local ngắn giờ (Tích hợp thời tiết)**
*   **Actor:** Customer, Guest
*   **Mô tả:** Tìm kiếm các Tour lữ hành trải nghiệm ngắn giờ được resort cung cấp kèm hiển thị thông tin thời tiết.
*   **Luồng xử lý chính (Basic Flow):**
    1. Khách hàng xem danh sách Tour trải nghiệm tại `/tours`.
    2. Chọn ngày muốn khởi hành -> Backend Spring Boot thực hiện gọi API của OpenWeather lấy dự báo thời tiết cho ngày đó tại địa điểm tổ chức Tour.
    3. Hiển thị thông số thời tiết và cảnh báo trực quan tới khách hàng (đẹp trời, mưa bão nên hủy...).
*   **Thiết kế Fail-safe:** Nếu API thời tiết bên thứ ba bị lỗi hoặc mất kết nối, hệ thống vẫn hiển thị danh sách Tour bình thường, chỉ ẩn đi phần thông tin thời tiết để tránh làm gián đoạn trải nghiệm người dùng.

---

### **UC17 — Điều hành & Đặt lịch chuyến xe Tour (Chống Double-booking)**

#### **UC17.1 — Duyệt và đặt lệnh mua vé gói hành trình trải nghiệm**
*   **Actor:** Customer
*   **Mô tả:** Khách hàng đặt mua vé tham gia Tour và thanh toán trực tuyến hoặc ghi nợ về phòng nghỉ.
*   **Luồng xử lý chính (Basic Flow):**
    1. Khách chọn Tour và chọn chuyến xe chạy `schedule_id` -> Chọn số lượng vé đăng ký.
    2. Chọn hình thức thanh toán (Thanh toán cổng VNPay hoặc Ký nợ phòng nghỉ Post to Room tương tự UC15).
    3. Kiểm tra ghế trống thời gian thực bằng khóa Pessimistic Lock trên dòng `Tour_Schedules`.
    4. Ghi nhận đơn hàng vào `Tour_Bookings`, tạo danh sách thành viên trong `Tour_Attendees`.
*   **Tác động Database:** `Tour_Bookings` (INSERT), `Tour_Attendees` (INSERT). Trigger `TRG_Update_Tour_Booked_Seats` tự động cộng dồn số ghế đã bán vào `Tour_Schedules.booked_seats`.

#### **UC17.2 — Lập lịch chạy các chuyến xe (Phân công tài xế & Hướng dẫn viên)**
*   **Actor:** Admin, Tour Guide
*   **Mô tả:** Lập lịch điều động chuyến xe chạy và phân công Hướng dẫn viên, tài xế phụ trách.
*   **Luồng xử lý chính (Basic Flow):**
    1. Admin truy cập trang `/admin/tour-schedule-config` tạo chuyến chạy mới.
    2. Chọn phân công xe và Nhân sự phụ trách (Tài xế, Hướng dẫn viên du lịch).
    3. Hệ thống kiểm tra chéo lịch hoạt động của tài xế và hướng dẫn viên đó trong khung giờ chạy: Nếu phát hiện bị trùng lịch chạy ở một Tour khác cùng khung giờ -> Báo lỗi Double-booking nhân sự và chặn lưu.

#### **UC17.3 — Hủy chuyến Tour do sự cố khẩn cấp (Xử lý hoàn tiền/đổi lịch)**
*   **Actor:** Admin, Tour Guide
*   **Mô tả:** Hủy chuyến chạy Tour do các nguyên nhân khách quan (thiên tai, hỏng xe...) và hoàn trả quyền lợi cho khách.
*   **Luồng xử lý chính (Basic Flow):**
    1. Admin click hủy chuyến chạy trên lịch trình -> Hệ thống chuyển trạng thái `schedule_status = 'Closed'`.
    2. Tìm kiếm toàn bộ các đơn đặt `Tour_Bookings` thuộc về chuyến chạy đó.
    3. Tự động kích hoạt luồng hoàn tiền: Gọi VNPay Refund đối với đơn thanh toán trực tuyến hoặc tự động sinh bản ghi Folio âm (`amount = -tiền_vé`) để khấu trừ nợ trong Folio phòng nghỉ của khách.
    4. Gửi email/SMS thông báo khẩn cấp tới toàn bộ khách hàng của chuyến đi.

#### **UC17.4 — Tự động đồng bộ phôi khách hàng từ gói Combo phòng sang Tour**
*   **Actor:** System
*   **Mô tả:** Tự động tạo đặt chỗ Tour khi khách hàng mua gói Combo lưu trú kèm tour du lịch trải nghiệm.
*   **Luồng xử lý chính (Basic Flow):**
    1. Ngay khi giao dịch đặt phòng Combo hoàn tất thanh toán thành công -> Hệ thống tự động phân tích cấu trúc JSON cấu hình combo.
    2. Tự động gọi Service đặt chỗ sinh đơn đặt trong `Tour_Bookings` -> Trích xuất dữ liệu danh sách khách đăng ký lưu trú phòng tự động chèn dữ liệu sang danh sách `Tour_Attendees` đi kèm.

---

### **UC18 — Điểm danh hành khách bằng AI quét mặt khuôn mặt tại Checkpoint**
*   **Actor:** Tour Guide
*   **Mô tả:** Hướng dẫn viên thực hiện điểm danh danh sách hành khách lên xe Tour tự động bằng nhận diện camera khuôn mặt.
*   **Preconditions:** Khách hàng đã đăng ký ảnh chân dung FaceID ở UC03.2.
*   **Luồng xử lý chính (Basic Flow):**
    1. Hướng dẫn viên mở app di động điểm danh `/staff/tour-checkin` -> Click mở camera chụp ảnh hành khách tại điểm đón.
    2. Gửi ảnh lên backend Spring Boot `/api/tour/face-checkin` -> Server chuyển tiếp ảnh sang Microservice Python `kawai-ai-service`.
    3. AI service giải mã ảnh, lấy vector khuôn mặt, so khớp cosine similarity với vector khuôn mặt gốc đã lưu của các hành khách trong chuyến đi.
    4. Nếu độ khớp vượt ngưỡng 0.8 -> Trả về ID hành khách khớp thành công -> Cập nhật trạng thái `Tour_Attendees.attendance_status = 'Boarded'` và lưu thời gian `face_matched_at`.
*   **Luồng thay thế (Manual Fallback):** Nếu quét ảnh lỗi hoặc khách chưa đăng ký FaceID, Hướng dẫn viên tích chọn điểm danh thủ công bằng tay trên màn hình danh sách hành khách.

---

### **UC19 — Gửi đánh giá bằng sao & feedback văn bản (Về phòng / Tour)**
*   **Actor:** Customer
*   **Mô tả:** Khách hàng gửi phản hồi chất lượng phòng nghỉ hoặc Tour trải nghiệm sau khi hoàn tất sử dụng.
*   **Preconditions:** Đơn đặt phòng phải ở trạng thái đã check-out, đơn đặt tour phải ở trạng thái đã hoàn thành.
*   **Luồng xử lý chính (Basic Flow):**
    1. Khách gửi đánh giá POST `/api/customer/reviews` chứa số sao (1-5) và văn bản nhận xét.
    2. Lưu bản ghi vào bảng `Reviews` (mặc định trạng thái `moderation_status = 'Pending'`).

---

### **UC20 — Kiểm duyệt nội dung đánh giá của khách (Ẩn bình luận toxic/spam)**
*   **Actor:** Admin
*   **Mô tả:** Admin duyệt các đánh giá của khách hàng trước khi hiển thị công khai lên Landing Page.
*   **Luồng xử lý chính (Basic Flow):**
    1. Admin xem danh sách đánh giá chờ duyệt `/admin/reviews`.
    2. Bấm "Phê duyệt" -> Cập nhật trạng thái thành `Approved` (review hiển thị công khai).
    3. Bấm "Ẩn review" -> Nhập lý do ẩn (Spam, ngôn từ không phù hợp...) -> Cập nhật trạng thái thành `Hidden` (bị ẩn khỏi giao diện công khai).

---

## 🟣 MODULE 5: KIỂM TOÁN ĐÊM, TÀI CHÍNH & BÁO CÁO

### **UC21 — Folio Aggregation — Gom hóa đơn tích lũy tự động**

#### **UC21.1 — Theo dõi kiểm soát dư nợ phòng lẻ thời gian thực (Ví Folio)**
*   **Actor:** Receptionist, Customer
*   **Mô tả:** Xem toàn bộ các khoản phát sinh chi phí của phòng lưu trú và dư nợ khả dụng.
*   **Tác động Database:** `Folio_Items` (SELECT).

#### **UC21.2 — Ghi vết lưu lịch sử luồng tiền nhiều đợt (Ứng trước, hoàn tiền)**
*   **Actor:** System
*   **Mô tả:** Ghi nhận chi tiết tất cả các đợt đóng tiền cọc, nạp tiền mặt trung gian hay hoàn tiền vào `Payment_Transactions` để đối soát.
*   **Tác động Database:** `Payment_Transactions` (INSERT).

#### **UC21.3 — Tổng hợp hóa đơn quyết toán tổng (Gom phòng + F&B + Tour thành 1)**
*   **Actor:** System
*   **Mô tả:** Gom toàn bộ tiền phòng vật lý lưu trú cùng các dịch vụ ăn uống, lữ hành khách ký nợ chưa thanh toán riêng lẻ thành một hóa đơn tổng quyết toán duy nhất khi Check-out.
*   **Tác động Database:** `Folio_Items` (SELECT), `Room_Booking_Details` (SELECT).

#### **UC21.4 — Chạy lệnh Kiểm toán đêm (Night Audit) tự động khóa sổ (02:00 AM)**
*   **Actor:** System
*   **Mô tả:** Tiến trình chạy ngầm tự động lúc 02:00 AM hàng ngày để tính toán tiền phòng của ngày hôm đó vào ví Folio và dịch chuyển ngày hoạt động của Resort sang ngày tiếp theo.
*   **Luồng xử lý chính (Basic Flow):**
    1. Scheduler tự động kích hoạt vào **02:00 AM** mỗi đêm.
    2. Quét danh sách các phòng chi tiết đang ở trạng thái lưu trú `detail_status = 'Checked_In'`.
    3. Với mỗi phòng, truy vấn đơn giá phòng áp dụng cho ngày hôm đó từ bảng giá ngày `Daily_Rates`.
    4. Chèn một bản ghi nợ tiền phòng vào bảng ví nợ `Folio_Items` (nguồn bộ phận `ROOM`, số tiền bằng giá phòng ngày).
    5. Cập nhật ngày hoạt động của khách sạn tiến lên 1 ngày.
    6. Tính toán doanh thu lũy kế ngày và lưu lại bảng lịch sử báo cáo ngày.
*   **Luồng ngoại lệ (Exceptions):**
    *   *EX1 (Lỗi cấu hình giá phòng):* Nếu không tìm thấy giá phòng ngày hôm đó tại bảng `Daily_Rates` -> Ghi nhận lỗi vào nhật ký sự kiện kiểm toán đêm (System Log) và gửi cảnh báo đỏ khẩn cấp tới email của Manager để xử lý thủ công, tạm dừng tự động khóa sổ.

#### **UC21.5 — Điều hướng dòng nợ & Tách hóa đơn tách ví Folio nâng cao**
*   **Actor:** Kế toán, Lễ tân
*   **Mô tả:** Tách hóa đơn dịch vụ ra khỏi hóa đơn phòng theo yêu cầu đặc biệt của khách hàng đoàn hoặc khách đi công tác.
*   **Luồng xử lý chính (Basic Flow):**
    1. Lễ tân chọn các dòng chi phí ăn uống/tour của khách cần tách hóa đơn.
    2. Nhấn nút "Tách thanh toán riêng" -> Cập nhật cờ `is_settled_separately = true` cho các dòng folio đó.
    3. Hệ thống tạo hóa đơn phụ riêng cho các dịch vụ này để khách thanh toán trực tiếp, phần tiền phòng vẫn được lưu lại thanh toán trên hóa đơn tổng khi check-out.

---

### **UC22 — Tất toán tài chính & Phát hành e-Invoice**

#### **UC22.1 — Xử lý thanh toán Check-out tài chính cuối cùng tại sảnh**
*   **Actor:** Receptionist
*   **Mô tả:** Thu tiền dư nợ cuối cùng của khách hàng và hoàn tất thủ tục trả phòng trên hệ thống.
*   **Preconditions:** Khách hàng phải thanh toán đầy đủ toàn bộ dư nợ ví nợ phòng để dư nợ Folio quy về bằng 0.
*   **Luồng xử lý chính (Basic Flow):**
    1. Lễ tân in hóa đơn tổng hợp quyết toán, khách tiến hành thanh toán tiền mặt/thẻ.
    2. Lễ tân xác nhận thanh toán -> Server cập nhật `Consolidated_Invoices.invoice_status = 'Paid'`.
    3. Tạo bản ghi giao dịch thanh toán cuối cùng trong `Payment_Transactions` (loại `FINAL_PAYMENT`).
    4. Cập nhật `Room_Booking_Details.detail_status = 'Checked_Out'`.
    5. Database Trigger `TRG_Auto_Housekeeping_Task` tự động chuyển đổi phòng vật lý tương ứng thành `Vacant_Dirty` và tạo phiếu dọn phòng cho Housekeeper.
*   **Luồng ngoại lệ (Exceptions):**
    *   *EX1:* Dư nợ Folio vẫn còn lớn hơn 0 -> Hệ thống khóa chặn và báo lỗi, không cho phép xác nhận Check-out thành công.

#### **UC22.2 — Tự động phát hành hóa đơn điện tử e-Invoice gửi về Email khách**
*   **Actor:** System
*   **Mô tả:** Tự động tạo tài liệu hóa đơn điện tử PDF và gửi thư điện tử báo cáo chi phí tới khách hàng sau thanh toán.
*   **Luồng xử lý chính (Basic Flow):**
    1. Ngay khi hóa đơn chuyển trạng thái `Paid` -> Hệ thống gọi template sinh file PDF hóa đơn chính thức chứa mã tra cứu thuế.
    2. Kích hoạt Mail Service gửi email đính kèm file PDF hóa đơn tổng tới hòm thư của Primary Contact đứng tên phòng.

---

### **UC23 — Dashboard Manager — Báo cáo quản trị cấp cao**

#### **UC23.1 — Giám sát biểu đồ phân tích tài chính doanh thu luỹ kế**
*   **Actor:** Manager
*   **Mô tả:** Quản lý xem các biểu đồ trực quan hóa doanh thu lũy kế theo thời gian thực để đưa ra các định hướng kinh doanh.
*   **Tác động Database:** `Consolidated_Invoices` (SELECT), `Folio_Items` (SELECT).

#### **UC23.2 — Kiểm soát công suất phòng & Số khách đang lưu trú (Occupancy)**
*   **Actor:** Manager
*   **Mô tả:** Giám sát tỷ lệ lấp đầy phòng vật lý và tổng số lượng hành khách thực tế đang lưu trú tại Resort sảnh.
*   **Tác động Database:** `Rooms` (SELECT), `Room_Booking_Details` (SELECT).

---

### **UC24 — Kết xuất báo cáo tài chính vận hành khách sạn chuẩn quốc tế USALI**
*   **Actor:** Manager
*   **Mô tả:** Xuất báo cáo tài chính phân tách rõ rệt doanh thu và chi phí hoạt động trực tiếp của từng bộ phận (Phòng, Nhà hàng F&B, Tour du lịch) theo chuẩn kế toán khách sạn quốc tế USALI.
*   **Tác động Database:** `Consolidated_Invoices` (SELECT), `Folio_Items` (SELECT), `Employees` (SELECT).

---

### **UC25 — Trích xuất báo cáo định dạng file tài liệu cứng PDF / Excel**
*   **Actor:** Manager
*   **Mô tả:** Tải các tài liệu báo cáo tài chính, báo cáo công suất dưới dạng file cứng Excel hoặc PDF về máy để phục vụ kiểm toán nội bộ.
*   **Luồng xử lý chính (Basic Flow):**
    1. Manager click chọn "Xuất Excel" hoặc "Xuất PDF" tại màn hình báo cáo.
    2. Server nhận yêu cầu, gọi thư viện Java Apache POI (cho Excel) hoặc OpenPDF (cho PDF) kết xuất dữ liệu thành luồng byte truyền thẳng về trình duyệt tải xuống.
*   **Dữ liệu Đầu vào (Inputs):** `reportType`, `startDate`, `endDate`.
*   **Dữ liệu Đầu ra (Outputs):** File cứng định dạng `.xlsx` hoặc `.pdf`.
