
# 📑 BẢNG USE CASE TỔNG HỢP TỐI HẬU — KAWAI RESORT

## 👥 DANH SÁCH ACTOR CHUẨN HOÀN CHỈNH (9 ACTOR)

1. **`Admin`** : Quản trị viên tối cao (Cấp quyền nhân viên, cấu hình hệ thống, xem nhật ký).
2. **`Manager`** : Quản lý Resort/Nhà hàng (Cấu hình giá, combo, xem biểu đồ, USALI).
3. **`Customer`** : Khách hàng/Khách vãng lai (Đăng ký, đặt phòng, gọi món room service, feedback).
4. **`Receptionist`** : Nhân viên lễ tân sảnh (Check-in, đổi phòng, check-out, quản lý folio, thu tiền phòng).
5. **`Housekeeper`** : Nhân viên buồng phòng (Tiếp nhận và cập nhật trạng thái dọn dẹp phòng).
6. **`Maintenance Staff`** : Nhân viên bảo trì (Sửa chữa sự cố kỹ thuật vật lý trong phòng).
7. **`Cashier`** : Thu ngân nhà hàng/F&B (Tất toán POS ăn uống, xử lý hóa đơn gọi món tại bàn).
8. **`Kitchen Staff`** : Nhân viên bếp (Theo dõi màn hình KDS, nấu nướng, báo hết món).
9. **`Tour Guide`** : Hướng dẫn viên (Điều hành tour, quét FaceID điểm danh khách).

## 🔴 MOD1: HỆ THỐNG CỐT LÕI, XÁC THỰC & ADMIN CONFIG — Sinh viên 1

| **UC ID** | **Tên Use Case Chi Tiết**                                                | **Actor**               | **Priority** | **Trạng thái** |
| --------------- | -------------------------------------------------------------------------------- | ----------------------------- | ------------------ | ---------------------- |
| **UC01**  | **Quản lý Tài khoản & Xác thực hệ thống**                          |                               |                    |                        |
| UC01.1          | Đăng ký tài khoản khách hàng trực tuyến (Khách tự phục vụ)          | `Customer`                  | 🔴 P0              | ⬜                     |
| UC01.2          | Admin khởi tạo và cấp tài khoản cho nhân viên mới                       | `Admin`                     | 🔴 P0              | ⬜                     |
| UC01.3          | Đăng nhập hệ thống chung (Phân hệ bảo mật Spring Security/JWT)          | Toàn bộ 9 Actor             | 🔴 P0              | ⬜                     |
| **UC02**  | Đặt lại mật khẩu (Gửi Mail chứa Token giới hạn thời gian)              | Toàn bộ 9 Actor             | 🟠 P1              | ⬜                     |
| **UC03**  | Quản lý hồ sơ cá nhân & Mã hóa giấy tờ định danh (AES-256)           | `Customer`                  | 🟠 P1              | ⬜                     |
| **UC04**  | Đăng tải & Trích xuất dữ liệu khuôn mặt FaceID chân dung gốc          | `Customer`,`Receptionist` | 🔴 P0              | ⬜                     |
| **UC05**  | **Phân quyền & Kiểm soát an ninh nội bộ**                            |                               |                    |                        |
| UC05.1          | Quản lý phân quyền RBAC (Gán quyền Lễ tân, Bếp, Thu ngân, Guide)       | `Admin`                     | 🔴 P0              | ⬜                     |
| UC05.2          | Truy vết nhật ký hệ thống (Audit Log) chống gian lận nội bộ             | `Admin`                     | 🟠 P1              | ⬜                     |
| **UC06**  | **Quản lý Dữ liệu nền Hạng phòng & Phòng vật lý (CRUD Rooms)**   |                               |                    |                        |
| UC06.1          | Thêm mới / Cập nhật / Xóa thông tin Hạng phòng ảo (`Room_Categories`) | `Admin`                     | 🟠 P1              | ⬜                     |
| UC06.2          | Quản lý danh sách số phòng vật lý thực tế (`Rooms`)                   | `Admin`                     | 🟠 P1              | ⬜                     |
| **UC07**  | **Quản lý Dữ liệu nền Sơ đồ Bàn ăn (CRUD Tables)**               |                               |                    |                        |
| UC07.1          | Thêm mới / Cập nhật cấu hình số bàn, vị trí (`Restaurant_Tables`)    | `Admin`,`Manager`         | 🟠 P1              | ⬜                     |
| UC07.2          | Thay đổi sức chứa hoặc ngừng hoạt động bàn phục vụ                   | `Admin`,`Manager`         | 🟠 P1              | ⬜                     |
| **UC08**  | **Quản lý Dữ liệu nền Hành trình Tour (CRUD Tours)**                |                               |                    |                        |
| UC08.1          | Thêm mới / Sửa thông tin Gói trải nghiệm local ngắn ngày (`Tours`)    | `Admin`,`Manager`         | 🟠 P1              | ⬜                     |
| UC08.2          | Cấu hình chi tiết lịch trình theo khung giờ (`Tour_Itineraries`)         | `Admin`,`Manager`         | 🟠 P1              | ⬜                     |
| **UC09**  | **Cấu hình Chiến lược Giá & Marketing nâng cao**                    |                               |                    |                        |
| UC09.1          | Cấu hình giá phòng động biến động theo mùa/ngày lễ                   | `Admin`,`Manager`         | 🟠 P1              | ⬜                     |
| UC09.2          | Khởi chạy lệnh nạp/cập nhật bảng giá phòng tĩnh hàng ngày            | `Admin`                     | 🔴 P0              | ⬜                     |
| UC09.3          | Cấu hình phụ thu hạng phòng theo khung tuổi trẻ em                        | `Admin`,`Manager`         | 🟠 P1              | ⬜                     |
| UC09.4          | Thiết lập chiến dịch Marketing & Đóng gói Combo dạng JSON                | `Admin`,`Manager`         | 🟠 P1              | ⬜                     |

## 🔵 MOD2: QUẢN LÝ PHÒNG, NGHIỆP VỤ SẢNH & BUỒNG PHÒNG — Sinh viên 2

| **UC ID** | **Tên Use Case Chi Tiết**                                               | **Actor**               | **Priority** | **Trạng thái** |
| --------------- | ------------------------------------------------------------------------------- | ----------------------------- | ------------------ | ---------------------- |
| **UC10**  | Tìm kiếm phòng trống thời gian thực & Nhặt giỏ hàng đa hạng          | `Customer`                  | 🔴 P0              | ⬜                     |
| **UC11**  | Thực hiện khóa quỹ giữ phòng tạm thời (Room Cart Lock 15')              | `Customer`                  | 🔴 P0              | ⬜                     |
| **UC12**  | **Nghiệp vụ Sảnh (Front Desk Operations)**                             |                               |                    |                        |
| UC12.1          | Áp dụng mã chiến dịch khuyến mãi / Voucher giảm giá                    | `Customer`                  | 🟠 P1              | ⬜                     |
| UC12.2          | Khai báo lưu trú & Chỉ định người lớn đứng tên phòng (Primary)     | `Customer`,`Receptionist` | 🔴 P0              | ⬜                     |
| UC12.3          | Quy trình Check-In sảnh (Quét OCR CCCD tự động điền form)               | `Receptionist`              | 🔴 P0              | ⬜                     |
| UC12.4          | Ủy quyền hạn mức chi tiêu ví nợ phòng phát sinh                        | `Receptionist`              | 🟠 P1              | ⬜                     |
| UC12.5          | Điều phối đổi phòng vật lý linh hoạt cho khách lưu trú              | `Receptionist`              | 🟠 P1              | ⬜                     |
| UC12.6          | Thực hiện quy trình Check-out sảnh & Yêu cầu thanh toán phòng           | `Receptionist`,`Customer` | 🔴 P0              | ⬜                     |
| **UC13**  | **Tác vụ Buồng phòng nội bộ & Bảo trì thiết bị (Housekeeping)** |                               |                    |                        |
| UC13.1          | Khởi tạo lệnh tác vụ dọn phòng khi khách Check-out                      | `Receptionist`              | 🔴 P0              | ⬜                     |
| UC13.2          | Tiếp nhận tác vụ & Cập nhật tiến độ dọn dẹp trên App nhân viên    | `Housekeeper`               | 🔴 P0              | ⬜                     |
| UC13.3          | Nghiệm thu phòng sạch & Tự động đồng bộ trạng thái về lễ tân      | `Housekeeper`               | 🔴 P0              | ⬜                     |
| UC13.4          | Ghi nhận yêu cầu sửa chữa cơ sở vật chất (Báo hỏng thiết bị)       | `Housekeeper`               | 🟠 P1              | ⬜                     |
| UC13.5          | Khắc phục sự cố kỹ thuật phòng vật lý (Báo hoàn thành bảo trì)    | `Maintenance Staff`         | 🟠 P1              | ⬜                     |

## 🟡 MOD3: DỊCH VỤ ẨM THỰC, NHÀ HÀNG & MÀN HÌNH BẾP KDS — Sinh viên 3

| **UC ID** | **Tên Use Case Chi Tiết**                                                 | **Actor**               | **Priority** | **Trạng thái** |
| --------------- | --------------------------------------------------------------------------------- | ----------------------------- | ------------------ | ---------------------- |
| **UC14**  | Đặt giữ trước bàn ăn tại sảnh nhà hàng của resort                     | `Customer`,`Receptionist` | 🟠 P1              | ⬜                     |
| **UC15**  | Cấu hình danh mục thực đơn (`Menu_Items`) & Nhãn dị ứng                | `Manager`                   | 🟠 P1              | ⬜                     |
| **UC16**  | Đặt món trực tuyến lên phòng nghỉ (Room Service Order)                    | `Customer`                  | 🟠 P1              | ⬜                     |
| **UC17**  | Gọi món Dine-In tại quầy (Nhân viên POS lên đơn tại bàn)               | `Cashier`                   | 🔴 P0              | ⬜                     |
| **UC18**  | Tất toán POS nhà hàng trực tiếp hoặc Ký nợ chi phí ăn uống về phòng | `Cashier`                   | 🔴 P0              | ⬜                     |
| **UC19**  | **Màn hình nhà bếp KDS Real-time (WebSocket)**                          |                               |                    |                        |
| UC19.1          | Tiếp nhận vé gọi món hàng đợi sắp xếp gộp mẻ thông minh (PENDING)    | `Kitchen Staff`             | 🔴 P0              | ⬜                     |
| UC19.2          | Cập nhật tiến độ nấu nướng (Bếp bấm chuyển trạng thái COOKING)       | `Kitchen Staff`             | 🔴 P0              | ⬜                     |
| UC19.3          | Báo cáo hoàn thành món ăn & Gọi nhân viên bưng bê (READY)              | `Kitchen Staff`             | 🔴 P0              | ⬜                     |
| UC19.4          | Kích hoạt báo hết món ăn (Tự động khóa thực đơn trên POS/Web)       | `Kitchen Staff`             | 🔴 P0              | ⬜                     |

## 🟢 MOD4: QUẢN LÝ LỮ HÀNH, ADD-ONS & ĐÁNH GIÁ — Sinh viên 4

| **UC ID** | **Tên Use Case Chi Tiết**                                                | **Actor**               | **Priority** | **Trạng thái** |
| --------------- | -------------------------------------------------------------------------------- | ----------------------------- | ------------------ | ---------------------- |
| **UC20**  | Tìm kiếm hành trình trải nghiệm local ngắn giờ (Tích hợp thời tiết)  | `Customer`                  | 🟠 P1              | ⬜                     |
| **UC21**  | Đặt vé hành trình trải nghiệm lẻ trực tiếp trên Web hoặc tại quầy  | `Customer`,`Receptionist` | 🔴 P0              | ⬜                     |
| **UC22**  | **Điều hành & Quản lý lịch trình Tour lữ hành**                   |                               |                    |                        |
| UC22.1          | Đồng bộ phôi khách hàng từ gói Combo phòng sang danh sách Tour         | `Receptionist`              | 🔴 P0              | ⬜                     |
| UC22.2          | Phân công điều động nhân sự chạy Tour (Tài xế & Hướng dẫn viên)   | `Admin`,`Manager`         | 🟠 P1              | ⬜                     |
| UC22.3          | Cập nhật tọa độ GPS Real-time theo dõi vị trí chuyến xe Tour            | `Tour Guide`                | 🟡 P2              | ⬜                     |
| UC22.4          | Cập nhật tiến độ lượt chạy thực tế (`Run_Itinerary_Status`)          | `Tour Guide`                | 🟠 P1              | ⬜                     |
| UC22.5          | Điểm danh hành khách bằng AI quét khuôn mặt FaceID tại Checkpoint       | `Tour Guide`                | 🟠 P1              | ⬜                     |
| **UC23**  | **Quản lý danh mục gói Add-ons dịch vụ gia tăng**                   |                               |                    |                        |
| UC23.1          | Cấu hình danh mục dịch vụ add-on niêm yết (Spa, Xe đưa đón...)        | `Admin`,`Manager`         | 🟠 P1              | ⬜                     |
| UC23.2          | Tiếp nhận đơn mua add-on & Đóng gói cấu hình yêu cầu đặc biệt JSON | `Customer`,`Receptionist` | 🔴 P0              | ⬜                     |
| **UC24**  | Gửi đánh giá bằng sao & feedback văn bản (Về phòng / Tour)              | `Customer`                  | 🟡 P2              | ⬜                     |
| **UC25**  | Kiểm duyệt nội dung đánh giá của khách (Ẩn bình luận toxic/spam)      | `Admin`                     | 🟡 P2              | ⬜                     |

## 🟣 MOD5: KIỂM TOÁN ĐÊM, TÀI CHÍNH & BÁO CÁO — Sinh viên 5

| **UC ID** | **Tên Use Case Chi Tiết**                                                 | **Actor**               | **Priority** | **Trạng thái** |
| --------------- | --------------------------------------------------------------------------------- | ----------------------------- | ------------------ | ---------------------- |
| **UC26**  | **Folio Aggregation — Gom hóa đơn tích lũy tự động**               |                               |                    |                        |
| UC26.1          | Tích lũy phát sinh chi phí tự động từ F&B/Tour/Spa về ví phòng         | `Receptionist`              | 🔴 P0              | ⬜                     |
| UC26.2          | Theo dõi kiểm soát dư nợ phòng lẻ thời gian thực (Ví Folio)             | `Receptionist`,`Customer` | 🔴 P0              | ⬜                     |
| UC26.3          | Ghi vết lưu lịch sử luồng tiền nhiều đợt (Ứng trước, tạm ứng)       | `Receptionist`              | 🔴 P0              | ⬜                     |
| UC26.4          | Tách ví, chuyển routing nợ (Xử lý tách bill công ty/khách lẻ)           | `Receptionist`              | 🟠 P1              | ⬜                     |
| UC26.5          | Tổng hợp hóa đơn quyết toán tổng (Gom đa dịch vụ thành 1 cục)        | `Receptionist`              | 🔴 P0              | ⬜                     |
| **UC27**  | **Kiểm toán đêm & Tất toán hóa đơn tài chính sảnh**             |                               |                    |                        |
| UC27.1          | Thực hiện nghiệp vụ Kiểm toán đêm (Night Audit) lúc**02:00 AM**    | `Receptionist`              | 🔴 P0              | ⬜                     |
| UC27.2          | Đóng gói hóa đơn tài chính tổng hợp & Tiếp nhận xuất hóa đơn đỏ | `Receptionist`              | 🔴 P0              | ⬜                     |
| UC27.3          | Điều hướng giao dịch qua Cổng trực tuyến (VNPay) & Lưu Ref ID            | `Customer`,`Receptionist` | 🔴 P0              | ⬜                     |
| UC27.4          | Xử lý thanh toán tất toán tài chính cuối cùng khi khách rời đi        | `Receptionist`              | 🔴 P0              | ⬜                     |
| UC27.5          | Tự động phát hành hóa đơn điện tử e-Invoice gửi về Email khách      | `Receptionist`              | 🟠 P1              | ⬜                     |
| **UC28**  | **Dashboard Manager — Kết xuất báo cáo quản trị cấp cao**           |                               |                    |                        |
| UC28.1          | Giám sát biểu đồ phân tích tài chính doanh thu luỹ kế                  | `Manager`                   | 🟠 P1              | ⬜                     |
| UC28.2          | Kiểm soát công suất phòng & Số khách đang lưu trú (Occupancy)           | `Manager`                   | 🟠 P1              | ⬜                     |
| UC28.3          | Kết xuất báo cáo tài chính vận hành khách sạn chuẩn quốc tế USALI    | `Manager`                   | 🟠 P1              | ⬜                     |
| UC28.4          | Trích xuất báo cáo định dạng file tài liệu cứng PDF / Excel             | `Manager`                   | 🟡 P2              | ⬜                     |

## 📊 MA TRẬN PHÂN BỔ SỐ LƯỢNG SAU KHI SỬA ĐỔI CHUẨN CHỈ

| **Module lớn** | **Số lượng Use Case con** | **Phân công phụ trách** | **Actor tương tác trực tiếp trong phân hệ**                           |
| --------------------- | ---------------------------------- | --------------------------------- | ---------------------------------------------------------------------------------- |
| **MOD1**        | **13 UC**                    | **Sinh viên 1**            | `Admin`,`Manager`,`Customer`                                                 |
| **MOD2**        | **13 UC**                    | **Sinh viên 2**            | `Customer`,`Receptionist`,`Housekeeper`,`Maintenance Staff`                |
| **MOD3**        | **9 UC**                     | **Sinh viên 3**            | `Customer`,`Manager`,`Cashier`  *(Chỉ thu ngân bếp)* ,`Kitchen Staff` |
| **MOD4**        | **11 UC**                    | **Sinh viên 4**            | `Customer`,`Admin`,`Manager`,`Receptionist`,`Tour Guide`                 |
| **MOD5**        | **14 UC**                    | **Sinh viên 5**            | `Receptionist`  *(Kiêm tất toán tổng)* ,`Customer`,`Manager`           |
| **TỔNG KHỚP** | **60 UC**                    | **Nhóm 4**                 | **Cấu trúc tối ưu, sạch sẽ 100%**                                      |
