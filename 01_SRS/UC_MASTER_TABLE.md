# BẢNG USE CASE TỔNG HỢP — KAWAI RESORT

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

| UC ID          | Tên Use Case                                                                    | Actor             | Priority | Trạng thái |
| -------------- | -------------------------------------------------------------------------------- | ----------------- | -------- | ------------ |
| **UC19** | Tìm kiếm gói tour (Tích hợp API thời tiết)                                | Customer, Guest   | 🟠 P1    | ⬜           |
| **UC20** | **Đặt tour & Chống Double-booking**                                     |                   |          |              |
| UC20.1         | Duyệt & Đặt Tour du lịch                                                     | Customer          | 🔴 P0    | ⬜           |
| UC20.2         | Lập lịch chạy các chuyến xe Tour (Điều hành xe và tài xế)             | Admin, Tour Guide | 🟠 P1    | ⬜           |
| UC20.3         | Hủy tour do sự cố (Hoàn tiền hoặc đổi lịch cho khách)                  | Admin, Tour Guide | 🟠 P1    | ⬜           |
| **UC21** | Điểm danh bằng AI quét mặt — Manifest & Báo sự cố (Gọi FastAPI Python) | Tour Guide        | 🟠 P1    | ⬜           |
| **UC22** | Gửi đánh giá bằng sao (Khách feedback về phòng / tour)                   | Customer          | 🟡 P2    | ⬜           |
| **UC23** | Kiểm duyệt đánh giá của khách (Ẩn bình luận toxic / spam)              | Admin             | 🟡 P2    | ⬜           |

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
