# BẢNG USE CASE TỔNG HỢP — KAWAI RESORT

## CHANGELOG
| Ngày | Người thực hiện | Nội dung thay đổi |
|---|---|---|
| 2026-06-11 | Antigravity | Xác nhận và đồng bộ các Use Cases với luồng phát triển Hybrid Organization |
| 2026-06-09 | Antigravity | Khởi tạo bảng Use Case tổng hợp cho 5 Module |

> Dùng ID cột `UC ID` để điền vào mọi báo cáo, bảng truy vết, test case.

---

* 🔴 MOD1: HỆ THỐNG CỐT LÕI, XÁC THỰC & TÀI KHOẢN — Sinh viên 1

| UC ID          | Tên Use Case                                                                            | Actor           | Priority | Trạng thái |
| -------------- | ---------------------------------------------------------------------------------------- | --------------- | -------- | ------------ |
| **UC01** | **Đăng ký & Đăng nhập (BCrypt)**                                             |                 |          |              |
| UC01.1         | Đăng ký tài khoản                                                                   | Customer        | 🔴 P0    | ⬜           |
| UC01.2         | Đăng nhập hệ thống                                                                  | Customer, User  | 🔴 P0    | ⬜           |
| UC02           | Xác thực 2FA qua OTP                                                                   | Customer, User  | 🔴 P0    | ⬜           |
| UC03           | Đặt lại mật khẩu (Gửi mail / Token giới hạn thời gian)                          | Customer, User  | 🟠 P1    | ⬜           |
| UC04           | Quản lý hồ sơ cá nhân & Tải giấy tờ định danh (AES-256)                       | Customer        | 🟠 P1    | ⬜           |
| **UC05** | **Quản lý tài khoản nhân viên & Kiểm soát rủi ro**                        |                 |          |              |
| UC05.1         | Quản lý tài khoản & Phân quyền RBAC (Admin cấp quyền Lễ tân, Bếp, Tour Guide) | Admin           | 🔴 P0    | ⬜           |
| UC05.2         | Truy vết nhật ký Audit Log chống gian lận nội bộ                                  | Admin           | 🟠 P1    | ⬜           |
| **UC06** | **Quản lý Core Data**                                                            |                 |          |              |
| UC06.1         | Quản lý dữ liệu gốc (Thêm/Sửa/Xóa cấu hình hệ thống)                         | Admin           | 🟠 P1    | ⬜           |
| UC06.2         | Cấu hình giá phòng động linh hoạt (Theo mùa / ngày lễ)                         | Admin, Manager  | 🟠 P1    | ⬜           |
| **UC07** | Ẩn danh hóa dữ liệu cá nhân — Quyền được quên (Nghị định 13/2023)         | Customer, Admin | 🟠 P1    | ⬜           |
| **UC08** | Tự động đăng xuất khi hết phiên làm việc (Session Timeout)                     | All Users       | 🟡 P2    | ⬜           |

---

## 🔵 MOD2: QUẢN LÝ PHÒNG & LỄ TÂN VẬN HÀNH — Sinh viên 2

| UC ID          | Tên Use Case                                                            | Actor                  | Priority | Trạng thái |
| -------------- | ------------------------------------------------------------------------ | ---------------------- | -------- | ------------ |
| **UC09** | Tìm kiếm phòng trống thời gian thực                                | Customer, Guest        | 🔴 P0    | ⬜           |
| **UC10** | **Đặt phòng & Thanh toán cọc trực tuyến**                   |                        |          |              |
| UC10.1         | Đặt phòng & Đặt cọc trực tuyến (Tích hợp VNPay)                | Customer               | 🔴 P0    | ⬜           |
| UC10.2         | Áp dụng mã chiến dịch khuyến mãi                                  | Customer               | 🟠 P1    | ⬜           |
| **UC11** | Xem sơ đồ Matrix phòng trống (Front Desk Dashboard)                 | Receptionist           | 🔴 P0    | ⬜           |
| **UC12** | **Check-in / Check-out / Đổi phòng**                            |                        |          |              |
| UC12.1         | Check-In & Gán phòng vật lý (Quét CCCD tự động điền form)      | Receptionist           | 🔴 P0    | ⬜           |
| UC12.2         | Ủy quyền hạn mức chi tiêu (Đặt cọc thêm cho dịch vụ)          | Receptionist           | 🟠 P1    | ⬜           |
| UC12.3         | Đổi phòng vật lý cho khách                                         | Receptionist           | 🟠 P1    | ⬜           |
| UC12.4         | Nâng cấp người đi cùng thành khách hàng (Dependents → Account) | Receptionist           | 🟡 P2    | ⬜           |
| **UC13** | **Quản lý sơ đồ phòng vật lý (Room Matrix)**               |                        |          |              |
| UC13.1         | Tự động kích hoạt lệnh dọn phòng (Khi Check-out hoặc yêu cầu) | System, Receptionist   | 🔴 P0    | ⬜           |
| UC13.2         | Cập nhật tiến độ dọn phòng (Báo Sạch / Bẩn)                    | **Housekeeping** | 🔴 P0    | ⬜           |
| UC13.3         | Xem danh sách yêu cầu dọn, sửa phòng                               | Receptionist           | 🟠 P1    | ⬜           |
| UC13.4         | Ghi nhận yêu cầu sửa chữa bảo trì (Báo hỏng thiết bị)         | **Housekeeping** | 🟠 P1    | ⬜           |
| UC13.5         | Khắc phục sự cố kỹ thuật phòng (Báo hoàn thành bảo trì)      | **Maintenance**  | 🟠 P1    | ⬜           |

---

## 🟡 MOD3: DỊCH VỤ ẨM THỰC & NHÀ HÀNG (F&B / POS) — Sinh viên 3

| UC ID          | Tên Use Case                                                                                 | Actor         | Priority | Trạng thái |
| -------------- | --------------------------------------------------------------------------------------------- | ------------- | -------- | ------------ |
| **UC14** | Đặt món trên web (Room Service / E-Menu trực tuyến)                                           | Customer      | 🟠 P1    | ⬜           |
| **UC15** | Đặt trước bàn ăn tại nhà hàng                                                        | Customer      | 🟠 P1    | ⬜           |
| **UC16** | Gọi món tại quầy Dine-In (Nhân viên POS lên đơn tại bàn)                           | F&B Staff     | 🔴 P0    | ⬜           |
| **UC17** | **KDS bếp — Quản lý trạng thái món**                                             |               |          |              |
| UC17.1         | Theo dõi vé gọi món nhà bếp KOT (Màn hình bếp nhận đơn)                           | Kitchen Staff | 🔴 P0    | ⬜           |
| UC17.2         | Báo hết món ăn (Tự động khóa món trên POS / E-Menu)                                 | Kitchen Staff | 🔴 P0    | ⬜           |
| **UC18** | Ký nợ hóa đơn ăn uống về phòng — Post to Room (Tính gộp vào Folio khi check-out) | F&B Staff     | 🔴 P0    | ⬜           |

---

## 🟢 MOD4: QUẢN LÝ LỮ HÀNH & ĐÁNH GIÁ — Sinh viên 4

| UC ID    | Tên Use Case                                                           | Actor             | Priority | Trạng thái |
| ----------| ------------------------------------------------------------------------| -------------------| ----------| ------------|
| **UC19** | Tìm kiếm gói tour (Tích hợp API thời tiết)                             | Customer, Guest   | 🟠 P1　　 | ⬜          |
| **UC20** | **Đặt tour & Chống Double-booking**                                    |                   | 　　　　 |            |
| UC20.1   | Duyệt & Đặt Tour du lịch                                               | Customer          | 🔴 P0　　| ⬜          |
| UC20.2   | Lập lịch chạy các chuyến xe Tour (Điều hành xe và tài xế)              | Admin, Tour Guide | 🟠 P1　　 | ⬜          |
| UC20.3   | Hủy tour do sự cố (Hoàn tiền hoặc đổi lịch cho khách)                  | Admin, Tour Guide | 🟠 P1　　 | ⬜          |
| **UC21** | Điểm danh bằng AI quét mặt — Manifest & Báo sự cố (Gọi FastAPI Python) | Tour Guide        | 🟠 P1　　 | ⬜          |
| **UC22** | Gửi đánh giá bằng sao (Khách feedback về phòng / tour)                 | Customer          | 🟡 P2　　 | ⬜          |
| **UC23** | Kiểm duyệt đánh giá của khách (Ẩn bình luận toxic / spam)              | Admin             | 🟡 P2　　 | ⬜          |

---

## 🟣 MOD5: KIỂM TOÁN ĐÊM, TÀI CHÍNH & BÁO CÁO — Sinh viên 5

| UC ID    | Tên Use Case                                                            | Actor                  | Priority | Trạng thái |
| ----------| -------------------------------------------------------------------------| ------------------------| ----------| ------------|
| **UC24** | **Folio Aggregation — Gom hóa đơn tự động**                             |                        | 　　　　 |            |
| UC24.1   | Theo dõi dư nợ phòng lẻ (Folio)                                         | Receptionist, Customer | 🔴 P0　　| ⬜          |
| UC24.2   | Lưu vết lịch sử luồng tiền nhiều đợt (Ứng trước, trả thêm, hoàn tiền)   | System                 | 🔴 P0　　| ⬜          |
| UC24.3   | Tổng hợp hóa đơn quyết toán (Gom phòng + ăn uống + tour thành 1 cục)    | System                 | 🔴 P0　　| ⬜          |
| UC24.4   | Kiểm toán đêm & Chốt ngày ca trực (Tự động chạy lúc **02:00 AM**)       | System                 | 🔴 P0　　| ⬜          |
| **UC25** | **Tất toán & e-Invoice**                                                |                        | 　　　　 |            |
| UC25.1   | Xử lý thanh toán Check-out cuối cùng (Thu nốt tiền và cho khách rời đi) | Receptionist           | 🔴 P0　　| ⬜          |
| UC25.2   | Tự động kích hoạt e-Invoice (Xuất hóa đơn điện tử gửi email khách)      | System                 | 🟠 P1　　 | ⬜          |
| **UC26** | **Dashboard Manager**                                                   |                        | 　　　　 |            |
| UC26.1   | Giám sát biểu đồ tài chính                                              | Manager                | 🟠 P1　　 | ⬜          |
| UC26.2   | Xem công suất phòng & số khách đang lưu trú (Occupancy Rate)            | Manager                | 🟠 P1　　 | ⬜          |
| **UC27** | Xuất báo cáo tài chính vận hành chuẩn USALI                             | Manager                | 🟠 P1　　 | ⬜          |
| **UC28** | Kết xuất file báo cáo PDF hóa đơn / Excel doanh thu                     | Manager                | 🟡 P2　　 | ⬜          |


---

## THỐNG KÊ

| Module          | UC (không tách con) | Tổng mục (gồm sub-UC) |
| --------------- | --------------------- | ------------------------ |
| MOD1            | 8                     | 12                       |
| MOD2            | 5                     | 14                       |
| MOD3            | 5                     | 7                        |
| MOD4            | 5                     | 8                        |
| MOD5            | 5                     | 11                       |
| **Tổng** | **28**          | **52**             |

**Chú thích:** ⬜ TODO | 🟡 IN PROGRESS | ✅ DONE

---

## PHẦN II: MÔ TẢ CHI TIẾT USE CASE (USE CASE SPECIFICATIONS)

Dưới đây là đặc tả chi tiết của các Use Cases cốt lõi thuộc Module 4 (Tour & Đánh giá) và liên kết liên module.

### 1. Đặc tả Chi tiết UC20.1 — Duyệt & Đặt Tour Du Lịch

| Thành phần | Đặc tả chi tiết |
| --- | --- |
| **Use Case ID & Tên** | **UC20.1 - Duyệt & Đặt Tour du lịch** |
| **Actor chính** | Customer (Khách hàng) |
| **Actor phụ** | System (Hệ thống), Folio Service (Ghi nợ phòng) |
| **Mục tiêu** | Khách hàng đặt chỗ tour lữ hành thành công và thực hiện thanh toán cọc hoặc ghi nợ vào hóa đơn phòng. |
| **Điều kiện tiên quyết (Preconditions)** | 1. Khách hàng đã đăng nhập tài khoản guest/customer.<br>2. Lịch trình Tour (`TourSchedule`) ở trạng thái `Open` và còn chỗ trống. |
| **Điều kiện sau khi thực hiện (Postconditions)** | 1. Một bản ghi `TourBooking` mới được tạo với trạng thái `Confirmed` (nếu ký nợ) hoặc `Pending` (chờ thanh toán cọc qua VNPay).<br>2. Số ghế đã đặt (`bookedSeats`) của lịch trình tăng lên tương ứng.<br>3. Danh sách khách tham quan (`TourAttendee`) được tạo tự động với trạng thái mặc định là `Not_Show`. |
| **Luồng sự kiện chính (Basic Flow)** | 1. Khách hàng chọn tour và xem chi tiết lịch trình mong muốn.<br>2. Khách hàng nhập số lượng người tham gia (`participantCount`).<br>3. Hệ thống hiển thị số lượng chỗ khả dụng và kiểm tra thời tiết tích hợp.<br>4. Khách chọn hình thức thanh toán:<br>&nbsp;&nbsp;&nbsp;&nbsp;- *Option A: Thanh toán cọc trực tuyến* qua cổng VNPay.<br>&nbsp;&nbsp;&nbsp;&nbsp;- *Option B: Ghi nợ phòng (Post to Room)*.<br>5. Khách hàng ấn xác nhận đặt tour.<br>6. Hệ thống thực hiện khóa bi quan (Pessimistic Lock) để chống overbooking và tạo giao dịch.<br>7. Hệ thống chuyển khách đến trang thanh toán (hoặc xác nhận thành công và tạo FolioItem nợ phòng).<br>8. Sau khi thanh toán/ghi nợ hoàn tất, hệ thống chuyển trạng thái booking thành `Confirmed`, cập nhật chỗ trống và Manifest đoàn. |
| **Luồng thay thế (Alternative Flows)** | **Alt-1: Khách vãng lai đặt tại quầy (Walk-in Tour Booking):**<br>- Tour Guide hoặc Lễ tân thay mặt khách tạo booking trên dashboard.<br>- Phương thức thanh toán được chọn là tiền mặt hoặc chuyển khoản trực tiếp, trạng thái cập nhật `Confirmed` lập tức. |
| **Ngoại lệ (Exceptions)** | **EX-1: Tour hết chỗ trống (TOUR-001):**<br>- Nếu tại bước 6, số chỗ trống không đủ cho đoàn khách đặt, hệ thống trả về mã lỗi `TOUR-001`, hủy giao dịch và thông báo cho khách hàng.<br>**EX-2: Ghi nợ vượt hạn mức chi tiêu (Credit Limit Exceeded):**<br>- Nếu khách chọn ghi nợ phòng nhưng tổng chi phí tour làm vượt quá hạn mức chi tiêu của phòng khách sạn (`creditLimit`), hệ thống báo lỗi và yêu cầu khách chọn hình thức thanh toán trực tiếp qua VNPay hoặc tiền mặt. |

---

### 2. Đặc tả Chi tiết UC21 — Điểm Danh Tour Bằng AI Quét Mặt (FaceID)

| Thành phần | Đặc tả chi tiết |
| --- | --- |
| **Use Case ID & Tên** | **UC21 - Điểm danh bằng AI quét mặt** |
| **Actor chính** | Tour Guide (Hướng dẫn viên) |
| **Actor phụ** | FastAPI AI Service (Hệ thống nhận diện khuôn mặt), Customer (Khách hàng) |
| **Mục tiêu** | Hướng dẫn viên thực hiện quét mặt khách hàng để tự động điểm danh và xác nhận sự hiện diện của khách trước khi tour khởi hành. |
| **Điều kiện tiên quyết (Preconditions)** | 1. Tour Guide đã đăng nhập và truy cập trang quản trị tour (Guide Dashboard).<br>2. Chuyến tour đã được lập lịch và ở trạng thái chuẩn bị xuất phát.<br>3. Khách hàng đã được đăng ký trong danh sách đoàn (`TourAttendee`). |
| **Điều kiện sau khi thực hiện (Postconditions)** | 1. Bản ghi `TourAttendee` của khách hàng được cập nhật cột `attendanceStatus = Present`.<br>2. Cột thời gian khớp mặt `faceMatchedAt` lưu thời điểm điểm danh thực tế. |
| **Luồng sự kiện chính (Basic Flow)** | 1. Tour Guide chọn lịch trình chuyến đi trên Guide Dashboard và nhấn "Điểm danh FaceID".<br>2. Giao diện mở camera thiết bị di động/kiosk của Tour Guide.<br>3. Khách hàng đứng trước camera thực hiện quét.<br>4. Trình duyệt bắt khung hình, chuyển thành chuỗi ảnh Base64 và gửi về `FaceIdApiController`.<br>5. API Controller gọi dịch vụ Python AI Service (`nhandien.py`).<br>6. AI Service phân tích ảnh, so khớp vector khuôn mặt với cơ sở dữ liệu khuôn mặt đã lưu trữ.<br>7. Nếu khớp với độ tương đồng >= 0.6 và kiểm tra liveness thành công, AI Service trả về thông tin khách hàng.<br>8. Hệ thống cập nhật trạng thái `TourAttendee` thành `Present`, ghi nhận thời gian quét và hiển thị thông báo thành công màu xanh trên màn hình. |
| **Luồng thay thế (Alternative Flows)** | **Alt-1: Điểm danh thủ công (Manual Fallback):**<br>- Nếu quét mặt thất bại liên tục hoặc hệ thống AI không phản hồi (Timeout >3s), giao diện hiển thị tùy chọn điểm danh thủ công.<br>- Tour Guide tích chọn trực tiếp vào tên khách trong danh sách Manifest để chuyển trạng thái thành `Present`. |
| **Ngoại lệ (Exceptions)** | **EX-1: Không nhận diện được khuôn mặt (FACE-001):**<br>- Ảnh chụp mờ hoặc không đủ ánh sáng khiến AI không phát hiện được mặt, hệ thống báo lỗi đề nghị khách hàng thử lại hoặc Tour Guide chuyển sang điểm danh bằng tay.<br>**EX-2: Sai lệch thông tin (Mạo danh):**<br>- Điểm danh trả về khách hàng không nằm trong danh sách đặt tour này, hệ thống báo lỗi không hợp lệ. |

