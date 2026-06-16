# KAWAI RESORT & TOUR HUB - DATABASE SCHEMA (V3.3 - 41 TABLES)

Tài liệu này mô tả chi tiết kiến trúc Cơ sở dữ liệu cho dự án **Kawai Resort & Tour Hub**. Ở phiên bản V3.3 này, toàn bộ 41 bảng dữ liệu được cấu trúc hóa trường dữ liệu chi tiết theo đúng logic nghiệp vụ của resort và hệ thống nhận diện FaceID, đảm bảo tính nhất quán giữa mô hình Java Persistence API (JPA) và Cơ sở dữ liệu MySQL backend.

---

## PHẦN I: DANH SÁCH CHI TIẾT 40 BẢNG (TABLE STRUCTURES)

### PHẦN I: HỆ THỐNG NGƯỜI DÙNG, PHÂN QUYỀN & HỒ SƠ ẢNH FACEID GỐC

#### **1. Roles (Vai trò hệ thống)**
| Tên cột | Kiểu dữ liệu | Ràng buộc / Giá trị mặc định | Mô tả |
| :--- | :--- | :--- | :--- |
| `role_id` | `INT` | PK, AUTO_INCREMENT | Mã định danh vai trò |
| `role_name` | `VARCHAR(50)` | UNIQUE, NOT NULL | Tên vai trò |

#### **2. Accounts (Tài khoản đăng nhập)**
| Tên cột | Kiểu dữ liệu | Ràng buộc / Giá trị mặc định | Mô tả |
| :--- | :--- | :--- | :--- |
| `account_id` | `INT` | PK, AUTO_INCREMENT | Mã tài khoản đăng nhập |
| `username` | `VARCHAR(50)` | UNIQUE, NOT NULL | Tên đăng nhập hệ thống |
| `password_hash` | `VARCHAR(255)` | NOT NULL | Mật khẩu băm |
| `is_active` | `BOOLEAN` | NOT NULL, DEFAULT TRUE | Cờ hoạt động |
| `role_id` | `INT` | FK -> Roles.role_id, RESTRICT | Vai trò liên kết |
| `created_at` | `TIMESTAMP` | DEFAULT CURRENT_TIMESTAMP | Thời điểm tài khoản được tạo |

#### **3. Employees (Hồ sơ nhân viên)**
| Tên cột | Kiểu dữ liệu | Ràng buộc / Giá trị mặc định | Mô tả |
| :--- | :--- | :--- | :--- |
| `employee_id` | `INT` | PK, AUTO_INCREMENT | Mã định danh nhân viên |
| `account_id` | `INT` | UNIQUE, NULLABLE, FK -> Accounts.account_id, CASCADE | Tài khoản liên kết |
| `full_name` | `VARCHAR(100)` | NOT NULL | Họ và tên đầy đủ |
| `gender` | `VARCHAR(10)` | NOT NULL | Giới tính |
| `cccd` | `VARCHAR(20)` | UNIQUE, NOT NULL | Căn cước công dân |
| `phone` | `VARCHAR(20)` | UNIQUE, NOT NULL | Số điện thoại di động |
| `email` | `VARCHAR(100)` | UNIQUE, NOT NULL | Địa chỉ Email |
| `salary` | `DECIMAL(12,2)` | NOT NULL | Lương cơ bản |

#### **4. Customers (Hồ sơ khách hàng & Ảnh FaceID Đại diện)**
| Tên cột | Kiểu dữ liệu | Ràng buộc / Giá trị mặc định | Mô tả |
| :--- | :--- | :--- | :--- |
| `customer_id` | `INT` | PK, AUTO_INCREMENT | Mã định danh khách hàng |
| `account_id` | `INT` | UNIQUE, NULLABLE, FK -> Accounts.account_id, CASCADE | Tài khoản liên kết |
| `full_name` | `VARCHAR(100)` | NOT NULL | Họ và tên khách hàng |
| `gender` | `VARCHAR(10)` | NOT NULL | Giới tính |
| `cccd_passport_encrypted` | `VARCHAR(512)` | UNIQUE, NULLABLE | CCCD/Hộ chiếu (Mã hóa bảo mật) |
| `phone` | `VARCHAR(20)` | UNIQUE, NOT NULL | Số điện thoại di động |
| `email` | `VARCHAR(100)` | UNIQUE, NOT NULL | Địa chỉ Email |
| `avatar_url` | `VARCHAR(500)` | NULLABLE | Đường dẫn ảnh đại diện |
| `loyalty_points` | `INT` | NOT NULL, DEFAULT 0 | Điểm thưởng tích lũy |
| `membership_tier` | `VARCHAR(30)` | NOT NULL, DEFAULT 'Regular' | Hạng thành viên |

#### **5. Dependents (Người phụ thuộc & Ảnh FaceID Trẻ em)**
| Tên cột | Kiểu dữ liệu | Ràng buộc / Giá trị mặc định | Mô tả |
| :--- | :--- | :--- | :--- |
| `dependent_id` | `INT` | PK, AUTO_INCREMENT | Mã định danh người phụ thuộc |
| `customer_id` | `INT` | FK -> Customers.customer_id, CASCADE | Khách hàng bảo hộ chủ đoàn |
| `dependent_name` | `VARCHAR(100)` | NOT NULL | Họ và tên người phụ thuộc |
| `birth_date` | `DATE` | NOT NULL | Ngày sinh |
| `gender` | `VARCHAR(10)` | NOT NULL | Giới tính |
| `face_img_url` | `VARCHAR(500)` | NULLABLE | Ảnh FaceID |
| `cccd_passport_encrypted` | `VARCHAR(512)` | NULLABLE | CCCD/Hộ chiếu (nếu có, mã hóa bảo mật) |

#### **6. Audit_Logs (Nhật ký hệ thống)**
| Tên cột | Kiểu dữ liệu | Ràng buộc / Giá trị mặc định | Mô tả |
| :--- | :--- | :--- | :--- |
| `log_id` | `INT` | PK, AUTO_INCREMENT | Mã log định danh |
| `account_id` | `INT` | NULLABLE, FK -> Accounts.account_id, RESTRICT | Tài khoản thực hiện thao tác |
| `action` | `VARCHAR(255)` | NOT NULL | Hành động thao tác |
| `table_name` | `VARCHAR(100)` | NOT NULL | Bảng chịu tác động |
| `record_id` | `INT` | NOT NULL | ID dòng dữ liệu |
| `old_value` | `TEXT` | NULLABLE | Giá trị cũ trước thay đổi |
| `new_value` | `TEXT` | NULLABLE | Giá trị mới sau thay đổi |
| `ip_address` | `VARCHAR(50)` | NOT NULL | Địa chỉ IP máy thực hiện |
| `timestamp` | `DATETIME` | NOT NULL | Thời điểm thao tác |

---

### PHẦN II: QUẢN LÝ ĐẶT PHÒNG, ẢNH HIỂN THỊ & PHỤ THU THUẦN LƯU TRÚ (CORE HOTEL)

#### **7. Room_Categories (Hạng phòng & Ảnh Quảng bá Web)**
| Tên cột | Kiểu dữ liệu | Ràng buộc / Giá trị mặc định | Mô tả |
| :--- | :--- | :--- | :--- |
| `category_id` | `INT` | PK, AUTO_INCREMENT | Mã định danh hạng phòng |
| `category_name` | `VARCHAR(100)` | UNIQUE, NOT NULL | Tên hạng phòng |
| `cover_img_url` | `VARCHAR(500)` | NULLABLE | Ảnh đại diện hạng phòng hiển thị trên web |
| `base_price` | `DECIMAL(12,2)` | NOT NULL | Đơn giá phòng nền |
| `capacity` | `INT` | NOT NULL | Sức chứa tối đa phòng |
| `description` | `TEXT` | NULLABLE | Mô tả chi tiết tiện ích phòng |

#### **8. Room_Surcharges (Cấu hình phụ thu PHÒNG thuần túy)**
| Tên cột | Kiểu dữ liệu | Ràng buộc / Giá trị mặc định | Mô tả |
| :--- | :--- | :--- | :--- |
| `surcharge_id` | `INT` | PK, AUTO_INCREMENT | Mã phụ thu phòng |
| `category_id` | `INT` | FK -> Room_Categories.category_id, CASCADE | Hạng phòng áp dụng |
| `surcharge_type` | `VARCHAR(50)` | NOT NULL | Kiểu phụ thu (`'EXTRA_ADULT_BED'`, `'CHILD_WITH_BED'`, `'CHILD_WITHOUT_BED'`) |
| `age_from` | `INT` | NOT NULL | Độ tuổi từ |
| `age_to` | `INT` | NOT NULL | Độ tuổi đến |
| `price_modifier` | `DECIMAL(12,2)` | NOT NULL | Mức tiền phụ thu phát sinh |
| `is_active` | `BOOLEAN` | NOT NULL, DEFAULT TRUE | Cờ kích hoạt |

#### **9. Rooms (Phòng vật lý)**
| Tên cột | Kiểu dữ liệu | Ràng buộc / Giá trị mặc định | Mô tả |
| :--- | :--- | :--- | :--- |
| `room_id` | `INT` | PK, AUTO_INCREMENT | Mã định danh phòng vật lý |
| `room_number` | `VARCHAR(20)` | UNIQUE, NOT NULL | Số hiệu phòng |
| `category_id` | `INT` | FK -> Room_Categories.category_id, RESTRICT | Hạng phòng |
| `room_status` | `VARCHAR(50)` | NOT NULL, DEFAULT 'Vacant_Clean' | Trạng thái phòng dọn dẹp và lưu trú |
| `current_booking_detail_id` | `INT` | NULLABLE | ID chi tiết booking hiện tại |

#### **10. Dynamic_Pricing (Chiến dịch giá biến động phòng)**
| Tên cột | Kiểu dữ liệu | Ràng buộc / Giá trị mặc định | Mô tả |
| :--- | :--- | :--- | :--- |
| `price_id` | `INT` | PK, AUTO_INCREMENT | Mã điều chỉnh giá |
| `category_id` | `INT` | FK -> Room_Categories.category_id, CASCADE | Hạng phòng áp dụng |
| `start_date` | `DATE` | NOT NULL | Ngày bắt đầu điều chỉnh |
| `end_date` | `DATE` | NOT NULL | Ngày kết thúc điều chỉnh |
| `price_modifier` | `DECIMAL(12,2)` | NOT NULL | Giá trị thay đổi (cộng/trừ) |
| `reason` | `VARCHAR(255)` | NULLABLE | Lý do áp dụng |

#### **11. Daily_Rates (Bảng tĩnh kết quả giá phòng theo ngày chạy cuốn chiếu)**
| Tên cột | Kiểu dữ liệu | Ràng buộc / Giá trị mặc định | Mô tả |
| :--- | :--- | :--- | :--- |
| `daily_rate_id` | `INT` | PK, AUTO_INCREMENT | Mã tính giá hàng ngày |
| `category_id` | `INT` | FK -> Room_Categories.category_id, CASCADE | Hạng phòng |
| `rate_date` | `DATE` | NOT NULL | Ngày tính giá |
| `computed_price` | `DECIMAL(12,2)` | NOT NULL | Đơn giá sau cùng |
| `is_weekend` | `BOOLEAN` | NOT NULL | Xác định ngày cuối tuần |
| `is_holiday` | `BOOLEAN` | NOT NULL | Xác định ngày nghỉ lễ |

#### **12. Promotions (Chương trình Khuyến mãi / Voucher)**
| Tên cột | Kiểu dữ liệu | Ràng buộc / Giá trị mặc định | Mô tả |
| :--- | :--- | :--- | :--- |
| `promo_id` | `INT` | PK, AUTO_INCREMENT | Mã khuyến mãi |
| `promo_code` | `VARCHAR(50)` | UNIQUE, NOT NULL | Mã định danh gói (ví dụ: 'COMBO_SUMMER_PQ') |
| `discount_type` | `VARCHAR(30)` | NOT NULL | Phân loại ('PERCENTAGE', 'FIXED_AMOUNT', 'COMBO_PACKAGE') |
| `discount_value` | `DECIMAL(10,2)` | NOT NULL | Mức ưu đãi / giảm giá |
| `valid_from` | `DATETIME` | NOT NULL | Ngày có hiệu lực |
| `valid_to` | `DATE` | NOT NULL | Ngày hết hiệu lực |
| `max_uses` | `INT` | NOT NULL | Lượt dùng tối đa |
| `current_uses` | `INT` | NOT NULL, DEFAULT 0 | Số lần đã sử dụng |
| `is_active` | `BOOLEAN` | NOT NULL, DEFAULT TRUE | Trạng thái kích hoạt |
| `description` | `TEXT` | NULLABLE | Mô tả khuyến mãi hoặc cấu trúc JSON cho COMBO_PACKAGE |

> **Ví dụ về Cấu trúc JSON Combo lưu ở cột `description`**:
> ```json
> {
>   "combo_name": "Combo Đảo Ngọc Hè Rực Rỡ 3N2D",
>   "cover_img_url": "https://cdn.resort.com/promotions/combo-he-phu-quoc.jpg",
>   "short_summary": "Trọn gói 2 đêm phòng hạng sang kèm vé Tour đảo ngắm san hô cao cấp.",
>   "highlights": [
>     "02 đêm nghỉ dưỡng tại Hạng phòng Deluxe Ocean View",
>     "01 Vé Tour cano 4 đảo, lặn ngắm san hô trọn gói cho cả đoàn",
>     "Miễn phí Buffet sáng mỗi ngày",
>     "Tặng voucher giảm 10% dịch vụ Spa tại Resort"
>   ],
>   "applicable_room_categories": [1, 2],
>   "applicable_tours": [5, 6]
> }
> ```

#### **13. Bookings (Đơn đặt chỗ gốc / Đơn hàng tổng)**
| Tên cột | Kiểu dữ liệu | Ràng buộc / Giá trị mặc định | Mô tả |
| :--- | :--- | :--- | :--- |
| `booking_id` | `INT` | PK, AUTO_INCREMENT | Mã đơn hàng tổng |
| `customer_id` | `INT` | FK -> Customers.customer_id, RESTRICT | Khách hàng chủ đơn |
| `booking_date` | `DATE` | NOT NULL | Ngày tạo đơn đặt |
| `total_price` | `DECIMAL(12,2)` | NOT NULL | Tổng giá trị đơn hàng |
| `booking_status` | `VARCHAR(50)` | NOT NULL, DEFAULT 'Pending' | Trạng thái thanh toán/giữ chỗ |
| `booking_source` | `VARCHAR(50)` | NOT NULL, DEFAULT 'Direct_Web' | Nguồn đặt |
| `applied_promotion_id` | `INT` | NULLABLE, FK -> Promotions.promo_id, SET NULL | Chương trình ưu đãi áp dụng |
| `version` | `INT` | NOT NULL, DEFAULT 1 | Cột phiên bản hỗ trợ Optimistic Locking |

#### **14. Room_Bookings (Đơn đặt phòng chi tiết)**
| Tên cột | Kiểu dữ liệu | Ràng buộc / Giá trị mặc định | Mô tả |
| :--- | :--- | :--- | :--- |
| `room_booking_id` | `INT` | PK, AUTO_INCREMENT | Mã đặt phòng |
| `booking_id` | `INT` | FK -> Bookings.booking_id, CASCADE | Đơn hàng tổng |
| `check_in_date` | `DATE` | NOT NULL | Ngày nhận phòng |
| `check_out_date` | `DATE` | NOT NULL | Ngày trả phòng |
| `deposit_amount` | `DECIMAL(12,2)` | NOT NULL | Tiền đặt cọc trước |
| `cancellation_deadline` | `DATE` | NOT NULL | Hạn chót hủy không mất phí cọc |
| `credit_limit` | `DECIMAL(12,2)` | NOT NULL, DEFAULT 5000000.00 | Hạn mức cho phép ký nợ về phòng |
| `personal_pin_hash` | `VARCHAR(255)` | NOT NULL | Mã PIN ký nợ phòng đã băm |

#### **15. Room_Booking_Details (Chi tiết căn phòng vật lý đặt)**
| Tên cột | Kiểu dữ liệu | Ràng buộc / Giá trị mặc định | Mô tả |
| :--- | :--- | :--- | :--- |
| `detail_id` | `INT` | PK, AUTO_INCREMENT | Mã chi tiết dòng phòng |
| `room_booking_id` | `INT` | FK -> Room_Bookings.room_booking_id, CASCADE | Đơn đặt phòng |
| `category_id` | `INT` | FK -> Room_Categories.category_id, RESTRICT | Hạng phòng |
| `room_id` | `INT` | NULLABLE, FK -> Rooms.room_id, RESTRICT | Phòng vật lý phân bổ thực tế |
| `room_charge` | `DECIMAL(12,2)` | NOT NULL | Giá tiền phòng tổng sau phụ thu |
| `detail_status` | `VARCHAR(50)` | NOT NULL, DEFAULT 'Pending' | Trạng thái chi tiết phòng lưu trú |
| `bed_preference` | `VARCHAR(20)` | NOT NULL, DEFAULT 'KING_SIZE' | Gu chọn giường (`'KING_SIZE'`, `'TWIN_BED'`) |
| `special_requests` | `VARCHAR(500)` | NULLABLE | Yêu cầu đặc biệt |
| `is_charge_to_room_allowed` | `BOOLEAN` | NOT NULL, DEFAULT TRUE | Cho phép ký nợ về phòng |
| `sub_credit_limit` | `DECIMAL(12,2)` | NOT NULL, DEFAULT 0.00 | Hạn mức nợ con của riêng phòng này |
| `billing_routing_strategy` | `VARCHAR(30)` | NOT NULL, DEFAULT 'BILL_TO_LEADER' | Phương án thanh toán công nợ |

#### **16. Room_Guests (Danh sách khách lưu trú thực tế tại căn phòng)**
| Tên cột | Kiểu dữ liệu | Ràng buộc / Giá trị mặc định | Mô tả |
| :--- | :--- | :--- | :--- |
| `guest_id` | `INT` | PK, AUTO_INCREMENT | Mã định danh khách ở phòng |
| `detail_id` | `INT` | FK -> Room_Booking_Details.detail_id, CASCADE | Dòng chi tiết đặt phòng |
| `customer_id` | `INT` | NULLABLE, FK -> Customers.customer_id, RESTRICT | Khách hàng đăng ký tài khoản |
| `dependent_id` | `INT` | NULLABLE, FK -> Dependents.dependent_id, RESTRICT | Khách đi kèm (Người phụ thuộc) |
| `guest_type` | `VARCHAR(20)` | NOT NULL | Phân loại độ tuổi (`'ADULT'`, `'CHILD'`) |
| `is_primary_contact` | `BOOLEAN` | NOT NULL, DEFAULT FALSE | Khách đại diện phòng để liên hệ |

---

### PHẦN III: MÔ-ĐUN TOUR TRẢI NGHIỆM ĐA PHƯƠNG TIỆN, GIÁ THEO TUỔI & TRACKING VẬN HÀNH

#### **17. Tours (Danh mục các Gói Trải Nghiệm / Hành Trình Lẻ)**
| Tên cột | Kiểu dữ liệu | Ràng buộc / Giá trị mặc định | Mô tả |
| :--- | :--- | :--- | :--- |
| `tour_id` | `INT` | PK, AUTO_INCREMENT | Mã định danh dịch vụ Tour |
| `tour_name` | `VARCHAR(150)` | UNIQUE, NOT NULL | Tên chuyến trải nghiệm |
| `tour_type` | `VARCHAR(50)` | NOT NULL | Loại hình hành trình |
| `duration` | `VARCHAR(100)` | NOT NULL | Thời lượng hiển thị trên UI (Ví dụ: "3 Tiếng") |
| `base_price` | `DECIMAL(12,2)` | NOT NULL | Giá vé gốc của Tour |
| `max_capacity` | `INT` | NOT NULL, DEFAULT 30 | Sức chứa tối đa đầu khách khởi hành |
| `short_quote` | `VARCHAR(500)` | NOT NULL | Trích dẫn mô tả bay bổng hiển thị ở UI |
| `description` | `TEXT` | NULLABLE | Nội dung chi tiết lịch trình |
| `created_at` | `TIMESTAMP` | DEFAULT CURRENT_TIMESTAMP | Ngày khởi tạo hệ thống |

#### **18. Tour_Images (Thư viện Album ảnh Slider quảng cáo Tour)**
| Tên cột | Kiểu dữ liệu | Ràng buộc / Giá trị mặc định | Mô tả |
| :--- | :--- | :--- | :--- |
| `image_id` | `INT` | PK, AUTO_INCREMENT | Mã ảnh album |
| `tour_id` | `INT` | FK -> Tours.tour_id, CASCADE | Tour liên kết |
| `image_url` | `VARCHAR(500)` | NOT NULL | Đường dẫn URL file ảnh |
| `is_primary` | `BOOLEAN` | NOT NULL, DEFAULT FALSE | Ảnh đại diện chính |

#### **19. Tour_Locations (Danh mục điểm dừng chân / Địa điểm check-in)**
| Tên cột | Kiểu dữ liệu | Ràng buộc / Giá trị mặc định | Mô tả |
| :--- | :--- | :--- | :--- |
| `location_id` | `INT` | PK, AUTO_INCREMENT | Mã điểm đến |
| `location_name` | `VARCHAR(150)` | NOT NULL | Tên địa danh dừng chân |
| `latitude` | `DECIMAL(10, 8)` | NULLABLE | Tọa độ Vĩ độ GPS |
| `longitude` | `DECIMAL(11, 8)` | NULLABLE | Tọa độ Kinh độ GPS |
| `description` | `TEXT` | NULLABLE | Mô tả điểm tham quan |
| `is_active` | `BOOLEAN` | NOT NULL, DEFAULT TRUE | Trạng thái hoạt động |

#### **20. Tour_Itineraries (Quản lý Ngày trong lịch trình Tour)**
| Tên cột | Kiểu dữ liệu | Ràng buộc / Giá trị mặc định | Mô tả |
| :--- | :--- | :--- | :--- |
| `itinerary_id` | `INT` | PK, AUTO_INCREMENT | Mã ngày lịch trình |
| `tour_id` | `INT` | FK -> Tours.tour_id, CASCADE | Tour liên kết |
| `day_number` | `INT` | NOT NULL | Số ngày tự tăng (Ngày 1, Ngày 2,...) |
| `day_title` | `VARCHAR(255)` | NOT NULL | Tiêu đề ngày hành trình |
| `summary` | `TEXT` | NULLABLE | Mô tả tóm tắt ngày |

#### **21. Tour_Itinerary_Details (Chi tiết hoạt động theo khung giờ gốc - Timeline)**
| Tên cột | Kiểu dữ liệu | Ràng buộc / Giá trị mặc định | Mô tả |
| :--- | :--- | :--- | :--- |
| `detail_id` | `INT` | PK, AUTO_INCREMENT | Mã dòng thời gian |
| `itinerary_id` | `INT` | FK -> Tour_Itineraries.itinerary_id, CASCADE | Ngày lịch trình tương ứng |
| `start_time` | `TIME` | NOT NULL | Giờ bắt đầu hoạt động |
| `end_time` | `TIME` | NULLABLE | Giờ kết thúc hoạt động |
| `location_id` | `INT` | NULLABLE, FK -> Tour_Locations.location_id, SET NULL | Điểm dừng chân |
| `activity_title` | `VARCHAR(150)` | NOT NULL | Tên hoạt động |
| `activity_description` | `TEXT` | NOT NULL | Mô tả chi tiết hoạt động |
| `meal_type` | `VARCHAR(50)` | NULLABLE | Bữa ăn phục vụ kèm theo |

#### **22. Tour_Prices (Cấu hình Giá vé Tour chi tiết theo Khung tuổi)**
| Tên cột | Kiểu dữ liệu | Ràng buộc / Giá trị mặc định | Mô tả |
| :--- | :--- | :--- | :--- |
| `tour_price_id` | `INT` | PK, AUTO_INCREMENT | Mã định cấu hình giá vé |
| `tour_id` | `INT` | FK -> Tours.tour_id, CASCADE | Tour liên kết |
| `age_from` | `INT` | NOT NULL | Từ độ tuổi |
| `age_to` | `INT` | NOT NULL | Đến độ tuổi |
| `ticket_price` | `DECIMAL(12,2)` | NOT NULL | Đơn giá vé tương ứng |
| `combo_discount_price` | `DECIMAL(12,2)` | NOT NULL, DEFAULT 0.00 | Đơn giá khi đi kèm Combo |
| `is_active` | `BOOLEAN` | NOT NULL, DEFAULT TRUE | Trạng thái kích hoạt |

#### **23. Tour_Schedules (Lịch trình khởi hành chuyến Tour)**
| Tên cột | Kiểu dữ liệu | Ràng buộc / Giá trị mặc định | Mô tả |
| :--- | :--- | :--- | :--- |
| `schedule_id` | `INT` | PK, AUTO_INCREMENT | Mã lịch chuyến đi |
| `tour_id` | `INT` | FK -> Tours.tour_id, RESTRICT | Tour liên kết |
| `departure_date` | `DATE` | NOT NULL | Ngày khởi hành |
| `departure_time` | `TIME` | NOT NULL | Giờ khởi hành |
| `booked_seats` | `INT` | NOT NULL, DEFAULT 0 | Số lượng vé đã được đặt |
| `schedule_status` | `VARCHAR(50)` | NOT NULL, DEFAULT 'Open' | Trạng thái đăng ký bán vé |

#### **24. Tour_Staff_Assignments (Phân công nhân sự & GPS Định vị Realtime)**
| Tên cột | Kiểu dữ liệu | Ràng buộc / Giá trị mặc định | Mô tả |
| :--- | :--- | :--- | :--- |
| `assignment_id` | `INT` | PK, AUTO_INCREMENT | Mã phân công công việc chuyến đi |
| `schedule_id` | `INT` | FK -> Tour_Schedules.schedule_id, CASCADE | Chuyến đi cụ thể |
| `employee_id` | `INT` | FK -> Employees.employee_id, RESTRICT | Nhân viên tham gia |
| `staff_role` | `VARCHAR(50)` | NOT NULL | Vai trò công vụ được giao |
| `is_lead_guide` | `BOOLEAN` | NOT NULL, DEFAULT FALSE | Là hướng dẫn viên trưởng đoàn |
| `current_gps_lat` | `DECIMAL(10, 8)` | NULLABLE | Vĩ độ định vị thực tế của nhân sự |
| `current_gps_lng` | `DECIMAL(11, 8)` | NULLABLE | Kinh độ định vị thực tế của nhân sự |

#### **25. Run_Itinerary_Status (Quản lý tiến độ lịch trình lượt chạy thực tế)**
| Tên cột | Kiểu dữ liệu | Ràng buộc / Giá trị mặc định | Mô tả |
| :--- | :--- | :--- | :--- |
| `run_status_id` | `INT` | PK, AUTO_INCREMENT | Mã trạng thái chạy lịch trình |
| `schedule_id` | `INT` | FK -> Tour_Schedules.schedule_id, CASCADE | Lịch khởi hành |
| `detail_id` | `INT` | FK -> Tour_Itinerary_Details.detail_id, CASCADE | Chi tiết hoạt động gốc |
| `actual_start_time` | `DATETIME` | NULLABLE | Thời điểm bắt đầu thực tế |
| `actual_end_time` | `DATETIME` | NULLABLE | Thời điểm kết thúc thực tế |
| `current_stage_status` | `VARCHAR(50)` | NOT NULL, DEFAULT 'NOT_STARTED' | Tiến độ hoạt động |
| `guide_notes` | `TEXT` | NULLABLE | Ghi nhận sự cố hay thông tin từ HDV |

#### **26. Checkpoint_Attendance (Điểm danh AI/Thủ công tại từng điểm dừng chân)**
| Tên cột | Kiểu dữ liệu | Ràng buộc / Giá trị mặc định | Mô tả |
| :--- | :--- | :--- | :--- |
| `checkpoint_id` | `INT` | PK, AUTO_INCREMENT | Mã log điểm danh check-point |
| `schedule_id` | `INT` | FK -> Tour_Schedules.schedule_id, CASCADE | Lịch khởi hành |
| `attendee_id` | `INT` | FK -> Tour_Attendees.attendee_id, CASCADE | Hành khách được điểm danh |
| `detail_id` | `INT` | FK -> Tour_Itinerary_Details.detail_id, CASCADE | Hoạt động dừng chân check-point |
| `scan_status` | `VARCHAR(30)` | NOT NULL | Trạng thái scan nhận diện |
| `scanned_at` | `TIMESTAMP` | DEFAULT CURRENT_TIMESTAMP | Thời điểm scan |
| `scanned_by_staff_id` | `INT` | FK -> Employees.employee_id, RESTRICT | Nhân viên ghi nhận scan |

#### **27. Tour_Bookings (Nghiệp vụ đặt Tour chi tiết trong Đơn hàng)**
| Tên cột | Kiểu dữ liệu | Ràng buộc / Giá trị mặc định | Mô tả |
| :--- | :--- | :--- | :--- |
| `booking_id` | `INT` | PK, FK -> Bookings.booking_id, CASCADE | Kế thừa khóa chính đơn hàng tổng |
| `schedule_id` | `INT` | FK -> Tour_Schedules.schedule_id, RESTRICT | Lịch chuyến đi được đặt |
| `participant_count` | `INT` | NOT NULL | Số khách đăng ký |
| `tour_charge` | `DECIMAL(12,2)` | NOT NULL, DEFAULT 0.00 | Chi phí đặt tour |
| `is_walk_in_tour` | `BOOLEAN` | NOT NULL, DEFAULT FALSE | Đặt lẻ trực tiếp (Không ở phòng resort) |

#### **28. Tour_Attendees (Danh sách hành khách lên xe và Cổng AI FaceID gốc)**
| Tên cột | Kiểu dữ liệu | Ràng buộc / Giá trị mặc định | Mô tả |
| :--- | :--- | :--- | :--- |
| `attendee_id` | `INT` | PK, AUTO_INCREMENT | Mã hành khách chuyến đi |
| `tour_booking_id` | `INT` | FK -> Tour_Bookings.booking_id, CASCADE | Đơn đặt tour |
| `customer_id` | `INT` | NULLABLE, FK -> Customers.customer_id, RESTRICT | Danh tính khách hàng |
| `dependent_id` | `INT` | NULLABLE, FK -> Dependents.dependent_id, RESTRICT | Danh tính người đi kèm |
| `detail_id` | `INT` | NULLABLE, FK -> Room_Booking_Details.detail_id, SET NULL | Số phòng lưu trú liên quan |
| `attendance_status` | `VARCHAR(50)` | NOT NULL, DEFAULT 'Not_Show' | Tiến trình điểm danh lên xe |
| `face_matched_at` | `TIMESTAMP` | NULLABLE | Thời điểm máy quét FaceID thành công |
| `face_vector_data` | `TEXT` | NULLABLE | Dữ liệu nhúng Vector khuôn mặt lưu trữ gốc |

---

### PHẦN IV: DANH MỤC ADD-ONS TRỰC TUYẾN & THỰC ĐƠN NHÀ HÀNG HÌNH ẢNH

#### **29. Hotel_Services (Danh mục dịch vụ Add-on niêm yết tại Resort)**
| Tên cột | Kiểu dữ liệu | Ràng buộc / Giá trị mặc định | Mô tả |
| :--- | :--- | :--- | :--- |
| `service_id` | `INT` | PK, AUTO_INCREMENT | Mã dịch vụ Add-on |
| `service_name` | `VARCHAR(150)` | UNIQUE, NOT NULL | Tên dịch vụ Add-on |
| `base_price` | `DECIMAL(12,2)` | NOT NULL | Đơn giá mặc định |
| `source_department` | `VARCHAR(50)` | NOT NULL | Bộ phận phụ trách (`'TRANSPORTATION'`, `'FLORIST'`, `'SPA'`, `'LAUNDRY'`) |
| `is_available` | `BOOLEAN` | NOT NULL, DEFAULT TRUE | Trạng thái còn mở bán dịch vụ hay tạm dừng |
| `description` | `TEXT` | NULLABLE | Mô tả chi tiết dịch vụ |

#### **30. Booking_Services (Chi tiết dịch vụ Add-on khách mua đi kèm đơn đặt Web)**
| Tên cột | Kiểu dữ liệu | Ràng buộc / Giá trị mặc định | Mô tả |
| :--- | :--- | :--- | :--- |
| `booking_service_id` | `INT` | PK, AUTO_INCREMENT | Mã đặt dịch vụ đi kèm đơn hàng |
| `booking_id` | `INT` | FK -> Bookings.booking_id, CASCADE | Hóa đơn đơn hàng tổng |
| `service_id` | `INT` | FK -> Hotel_Services.service_id, RESTRICT | Dịch vụ Add-on chọn mua |
| `quantity` | `INT` | NOT NULL, DEFAULT 1 | Số lượng dịch vụ |
| `unit_price` | `DECIMAL(12,2)` | NOT NULL | Đơn giá mua ghi nhận |
| `execution_date` | `DATETIME` | NOT NULL | Ngày thực hiện cung cấp |
| `status` | `VARCHAR(50)` | NOT NULL, DEFAULT 'PENDING' | Trạng thái thực thi công việc dịch vụ |
| `specific_requests_json` | `TEXT` | NULLABLE | Dữ liệu cấu hình JSON riêng cho dịch vụ (Ví dụ: thông tin số hiệu chuyến bay, loại hoa yêu cầu...) |

> **Ví dụ về Cấu trúc JSON lưu ở cột `specific_requests_json`**:
> * Đưa đón sân bay: `{"flight_number": "VN123", "arrival_time": "14:30"}`
> * Decor buồng phòng: `{"flower_type": "Roses", "card_note": "Happy Anniversary"}`

#### **31. Restaurant_Tables (Danh mục Bàn ăn nhà hàng)**
| Tên cột | Kiểu dữ liệu | Ràng buộc / Giá trị mặc định | Mô tả |
| :--- | :--- | :--- | :--- |
| `table_id` | `INT` | PK, AUTO_INCREMENT | Mã định danh bàn |
| `table_number` | `VARCHAR(20)` | UNIQUE, NOT NULL | Số bàn |
| `capacity` | `INT` | NOT NULL | Sức chứa tối đa |
| `table_status` | `VARCHAR(50)` | NOT NULL, DEFAULT 'Vacant' | Trạng thái dọn dẹp và sử dụng bàn ăn |

#### **32. Table_Reservations (Lịch đặt bàn trước của khách)**
| Tên cột | Kiểu dữ liệu | Ràng buộc / Giá trị mặc định | Mô tả |
| :--- | :--- | :--- | :--- |
| `reservation_id` | `INT` | PK, AUTO_INCREMENT | Mã lượt đặt bàn |
| `customer_id` | `INT` | FK -> Customers.customer_id, RESTRICT | Danh tính khách hàng |
| `table_id` | `INT` | FK -> Restaurant_Tables.table_id, RESTRICT | Bàn ăn đăng ký giữ chỗ |
| `reserve_date` | `DATE` | NOT NULL | Ngày đặt hẹn dùng bữa |
| `reserve_time` | `TIME` | NOT NULL | Giờ đặt hẹn |
| `deposit_amount` | `DECIMAL(12,2)` | NOT NULL | Số tiền đặt cọc bàn |
| `status` | `VARCHAR(50)` | NOT NULL | Trạng thái đặt chỗ giữ bàn |

#### **33. Menu_Items (Danh mục Thực đơn món ăn hiển thị ảnh)**
| Tên cột | Kiểu dữ liệu | Ràng buộc / Giá trị mặc định | Mô tả |
| :--- | :--- | :--- | :--- |
| `item_id` | `INT` | PK, AUTO_INCREMENT | Mã định danh món ăn thực đơn |
| `item_name` | `VARCHAR(150)` | UNIQUE, NOT NULL | Tên món ăn |
| `image_url` | `VARCHAR(500)` | NULLABLE | File ảnh món phục vụ xem online |
| `price` | `DECIMAL(12,2)` | NOT NULL | Giá bán món ăn |
| `category` | `VARCHAR(50)` | NOT NULL | Danh mục món ăn |
| `is_available` | `BOOLEAN` | NOT NULL, DEFAULT TRUE | Trạng thái món ăn còn phục vụ |
| `description` | `TEXT` | NULLABLE | Mô tả chi tiết món ăn |
| `allergy_tags` | `VARCHAR(255)` | NULLABLE | Nhãn dị ứng món ăn (Ví dụ: `'PEANUT, SEAFOOD, GLUTEN'`) |

#### **34. Food_Orders (Hóa đơn gọi món ăn)**
| Tên cột | Kiểu dữ liệu | Ràng buộc / Giá trị mặc định | Mô tả |
| :--- | :--- | :--- | :--- |
| `order_id` | `INT` | PK, AUTO_INCREMENT | Mã hóa đơn gọi món |
| `booking_id` | `INT` | NULLABLE, FK -> Bookings.booking_id, SET NULL | Liên kết hóa đơn đặt phòng tổng |
| `room_booking_detail_id` | `INT` | NULLABLE, FK -> Room_Booking_Details.detail_id, SET NULL | Liên kết phòng chi tiết ký nợ |
| `table_id` | `INT` | NULLABLE, FK -> Restaurant_Tables.table_id, RESTRICT | Bàn ăn dùng món tại chỗ |
| `reservation_id` | `INT` | NULLABLE, FK -> Table_Reservations.reservation_id, SET NULL | Lịch đặt bàn dùng bữa tương ứng |
| `table_number_cache` | `VARCHAR(20)` | NULLABLE | Lưu nhanh số hiệu bàn |
| `room_number_cache` | `VARCHAR(20)` | NULLABLE | Lưu nhanh số hiệu phòng |
| `order_type` | `VARCHAR(50)` | NOT NULL | Phân loại gọi món (`'IN_RESTAURANT'`, `'ROOM_SERVICE'`, `'WALK_IN_RESTAURANT'`, `'PRE_ORDER_RESTAURANT'`) |
| `order_status` | `VARCHAR(50)` | NOT NULL, DEFAULT 'Pending' | Trạng thái tiến trình đơn gọi món |
| `payment_type` | `VARCHAR(50)` | NOT NULL | Hình thức (`'CASH'`, `'CARD'`, `'CHARGE_TO_ROOM'`) |
| `is_paid_in_pos` | `BOOLEAN` | NOT NULL, DEFAULT FALSE | Đã thanh toán tiền tại quầy chưa |
| `created_by_staff_id` | `INT` | FK -> Employees.employee_id, RESTRICT | Nhân viên ghi nhận hóa đơn |
| `kitchen_processed_by_id` | `INT` | NULLABLE, FK -> Employees.employee_id, SET NULL | Nhân viên nhà bếp dọn món |
| `scheduled_delivery_time` | `DATETIME` | NULLABLE | Giờ giao món Room Service hẹn trước |
| `customer_notes` | `TEXT` | NULLABLE | Yêu cầu dị ứng hoặc dặn dò riêng của khách |

#### **35. Food_Order_Details (Chi tiết các món ăn gọi trong hóa đơn)**
| Tên cột | Kiểu dữ liệu | Ràng buộc / Giá trị mặc định | Mô tả |
| :--- | :--- | :--- | :--- |
| `detail_id` | `INT` | PK, AUTO_INCREMENT | Mã dòng món chi tiết |
| `food_order_id` | `INT` | FK -> Food_Orders.order_id, CASCADE | Hóa đơn gọi món chính |
| `menu_item_id` | `INT` | FK -> Menu_Items.item_id, RESTRICT | Món ăn thực đơn được chọn |
| `quantity` | `INT` | NOT NULL | Số lượng gọi món |
| `price_at_order` | `DECIMAL(12,2)` | NOT NULL | Đơn giá món thời điểm gọi |
| `kot_status` | `VARCHAR(50)` | NOT NULL, DEFAULT 'PENDING' | Trạng thái chế biến hiển thị trên KDS (`'PENDING'`, `'COOKING'`, `'READY'`, `'SERVED'`) |

---

### PHẦN V: TÁC VỤ NỘI BỘ & KẾ TOÁN TÀI CHÍNH (FINANCE & OPERATIONS)

#### **36. Hotel_Operations (Tác vụ vận hành nội bộ / Buồng phòng)**
| Tên cột | Kiểu dữ liệu | Ràng buộc / Giá trị mặc định | Mô tả |
| :--- | :--- | :--- | :--- |
| `task_id` | `INT` | PK, AUTO_INCREMENT | Mã tác vụ công việc nội bộ |
| `room_id` | `INT` | FK -> Rooms.room_id, CASCADE | Phòng vật lý liên quan |
| `staff_id` | `INT` | FK -> Employees.employee_id, SET NULL | Nhân viên thực hiện |
| `supervisor_id` | `INT` | FK -> Employees.employee_id, SET NULL | Người quản lý/Giám sát duyệt |
| `operational_type` | `VARCHAR(50)` | NOT NULL | Loại công vụ vận hành |
| `priority` | `VARCHAR(30)` | NOT NULL, DEFAULT 'Normal' | Độ khẩn cấp |
| `status` | `VARCHAR(50)` | NOT NULL, DEFAULT 'Pending' | Trạng thái tiến độ |
| `created_at` | `TIMESTAMP` | DEFAULT CURRENT_TIMESTAMP | Ngày tạo giao việc |
| `started_at` | `DATETIME` | NULLABLE | Ngày giờ thực tế bắt đầu thực thi |
| `completed_at` | `DATETIME` | NULLABLE | Ngày giờ thực tế hoàn thành |
| `notes` | `TEXT` | NULLABLE | Ghi nhận báo cáo kết quả |

#### **37. Folio_Items (Hạng mục ví nợ tích lũy phát sinh của Đơn phòng)**
| Tên cột | Kiểu dữ liệu | Ràng buộc / Giá trị mặc định | Mô tả |
| :--- | :--- | :--- | :--- |
| `folio_item_id` | `INT` | PK, AUTO_INCREMENT | Mã dòng ghi nợ Folio |
| `booking_id` | `INT` | FK -> Bookings.booking_id, CASCADE | Đơn hàng tổng |
| `room_booking_detail_id` | `INT` | NULLABLE, FK -> Room_Booking_Details.detail_id, SET NULL | Phòng chi tiết ghi nhận nợ |
| `payer_customer_id` | `INT` | FK -> Customers.customer_id, RESTRICT | Khách hàng đại diện thanh toán |
| `source_department` | `VARCHAR(50)` | NOT NULL | Bộ phận phát sinh chi phí |
| `amount` | `DECIMAL(12,2)` | NOT NULL | Giá trị ghi nợ |
| `description` | `VARCHAR(255)` | NOT NULL | Mô tả nội dung khoản thu nợ |
| `created_at` | `TIMESTAMP` | DEFAULT CURRENT_TIMESTAMP | Thời điểm phát sinh nợ |
| `signature_img_url` | `VARCHAR(500)` | NULLABLE | URL ảnh chữ ký khách hàng ký xác nhận |
| `is_settled_separately` | `BOOLEAN` | NOT NULL, DEFAULT FALSE | Thanh toán riêng ngay lập tức (Không chờ checkout) |
| `created_by_staff_id` | `INT` | NULLABLE, FK -> Employees.employee_id, SET NULL | Nhân viên ghi nhận công nợ |

#### **38. Consolidated_Invoices (Hóa đơn tài chính tổng hợp tất toán)**
| Tên cột | Kiểu dữ liệu | Ràng buộc / Giá trị mặc định | Mô tả |
| :--- | :--- | :--- | :--- |
| `invoice_id` | `INT` | PK, AUTO_INCREMENT | Mã hóa đơn tất toán tài chính |
| `invoice_number` | `VARCHAR(50)` | UNIQUE, NOT NULL | Số hóa đơn xuất thuế |
| `booking_id` | `INT` | UNIQUE, NOT NULL, FK -> Bookings.booking_id, RESTRICT | Mã đơn hàng tổng |
| `subtotal_before_vat` | `DECIMAL(12,2)` | NOT NULL | Doanh thu trước thuế VAT |
| `vat_amount` | `DECIMAL(12,2)` | NOT NULL | Mức thuế suất VAT quy định |
| `total_amount` | `DECIMAL(12,2)` | NOT NULL | Tổng tiền thanh toán sau thuế |
| `promo_id` | `INT` | NULLABLE, FK -> Promotions.promo_id, SET NULL | Mã Voucher giảm trừ thêm hóa đơn |
| `invoice_status` | `VARCHAR(50)` | NOT NULL, DEFAULT 'Draft' | Trạng thái tất toán hóa đơn |
| `created_at` | `TIMESTAMP` | DEFAULT CURRENT_TIMESTAMP | Ngày tạo hóa đơn nháp |
| `issued_at` | `DATETIME` | NOT NULL | Ngày giờ ký phát hành thuế |
| `is_vat_requested` | `BOOLEAN` | NOT NULL, DEFAULT FALSE | Cờ yêu cầu xuất hóa đơn VAT |
| `company_name` | `VARCHAR(255)` | NULLABLE | Tên công ty xuất hóa đơn đỏ |
| `tax_code` | `VARCHAR(50)` | NULLABLE | Mã số thuế doanh nghiệp |
| `company_address` | `VARCHAR(500)` | NULLABLE | Địa chỉ công ty đăng ký thuế |

#### **39. Payment_Transactions (Giao dịch cổng thanh toán tài chính)**
| Tên cột | Kiểu dữ liệu | Ràng buộc / Giá trị mặc định | Mô tả |
| :--- | :--- | :--- | :--- |
| `transaction_id` | `INT` | PK, AUTO_INCREMENT | Mã giao dịch |
| `invoice_id` | `INT` | FK -> Consolidated_Invoices.invoice_id, RESTRICT | Hóa đơn liên kết tất toán |
| `booking_id` | `INT` | FK -> Bookings.booking_id, CASCADE | Đơn hàng tổng |
| `amount` | `DECIMAL(12,2)` | NOT NULL | Số tiền thanh toán thực |
| `transaction_type` | `VARCHAR(50)` | NOT NULL | Loại giao dịch |
| `payment_method` | `VARCHAR(50)` | NOT NULL | Phương thức thanh toán cổng VNPay/Thẻ |
| `gateway_status` | `VARCHAR(50)` | NOT NULL | Trạng thái phản hồi cổng thanh toán |
| `expired_at` | `DATETIME` | NULLABLE | Giờ hết hạn thanh toán đơn |
| `transaction_ref` | `VARCHAR(100)` | UNIQUE, NOT NULL | Mã tham chiếu giao dịch độc nhất phản hồi |
| `vnp_txn_ref` | `VARCHAR(100)` | UNIQUE, NULLABLE | Mã VNPay Txn Ref tham chiếu |
| `vnp_transaction_no` | `VARCHAR(100)` | NULLABLE | Số giao dịch ghi nhận trên cổng VNPay |
| `created_at` | `TIMESTAMP` | DEFAULT CURRENT_TIMESTAMP | Thời điểm giao dịch khởi tạo |
| `paid_at` | `DATETIME` | NULLABLE | Thời điểm giao dịch ghi nhận thành công |

#### **40. Reviews (Đánh giá phản hồi dịch vụ)**
| Tên cột | Kiểu dữ liệu | Ràng buộc / Giá trị mặc định | Mô tả |
| :--- | :--- | :--- | :--- |
| `review_id` | `INT` | PK, AUTO_INCREMENT | Mã log đánh giá |
| `customer_id` | `INT` | FK -> Customers.customer_id, CASCADE | Khách hàng viết nhận xét |
| `room_booking_detail_id` | `INT` | NULLABLE, FK -> Room_Booking_Details.detail_id, SET NULL | Lượt phòng được đánh giá |
| `tour_booking_id` | `INT` | NULLABLE, FK -> Tour_Bookings.booking_id, SET NULL | Lượt tour được đánh giá |
| `rating_service` | `INT` | NOT NULL | Điểm số (Thang sao: 1 - 5) |
| `review_text` | `TEXT` | NULLABLE | Nội dung góp ý phản hồi |
| `created_at` | `TIMESTAMP` | DEFAULT CURRENT_TIMESTAMP | Ngày giờ đăng tải |
| `moderation_status` | `VARCHAR(50)` | NOT NULL, DEFAULT 'Pending' | Trạng thái duyệt của Ban quản lý |
| `moderated_by` | `INT` | NULLABLE, FK -> Employees.employee_id, SET NULL | Nhân viên kiểm duyệt |
| `moderation_reason` | `TEXT` | NULLABLE | Lý do phê duyệt hoặc từ chối ẩn |

#### **41. Authorized_Devices (Thiết bị đăng nhập hợp lệ của nhân viên)**
| Tên cột | Kiểu dữ liệu | Ràng buộc / Giá trị mặc định | Mô tả |
| :--- | :--- | :--- | :--- |
| `id` | `INT` | PK, AUTO_INCREMENT | Mã định danh bản ghi thiết bị |
| `device_code` | `VARCHAR(255)` | UNIQUE, NOT NULL | Mã định danh duy nhất của thiết bị |
| `is_approved` | `BOOLEAN` | NOT NULL, DEFAULT TRUE | Trạng thái được duyệt đăng nhập |

---

## PHẦN II: HỆ THỐNG RÀNG BUỘC CƠ SỞ DỮ LIỆU CHI TIẾT (COMPREHENSIVE CONSTRAINTS)

### 1. RÀNG BUỘC KHÓA NGOẠI (FOREIGN KEY CONSTRAINTS)

* **`FK_Accounts_Roles`**: Cấm xóa vai trò (Role) đang có tài khoản sử dụng nhằm tránh trạng thái cô lập phân quyền.
  > `ON DELETE RESTRICT`, `ON UPDATE CASCADE`
* **`FK_Employees_Accounts`** & **`FK_Customers_Accounts`**: Khi tài khoản (Account) bị xóa, tự động giải phóng hoặc xóa dọn dẹp tương ứng hồ sơ cá nhân.
  > `ON DELETE CASCADE`
* **`FK_Dependents_Customers`**: Xóa khách hàng chủ đoàn sẽ tự động dọn dẹp các hồ sơ hành khách phụ thuộc đi kèm.
  > `ON DELETE CASCADE`
* **`FK_AuditLogs_Accounts`**: Cấm xóa hoàn toàn tài khoản nếu tài khoản này đã từng phát sinh vết trong Log nghiệp vụ.
  > `ON DELETE RESTRICT`
* **`FK_Rooms_Categories`**: Không cho phép xóa hạng phòng nếu vẫn còn tồn tại các phòng vật lý đang được gán hạng đó.
  > `ON DELETE RESTRICT`
* **`FK_DynamicPricing_Categories`**: Khi Hạng phòng bị xóa bỏ, các chiến dịch cấu hình giá động của hạng đó bị xóa theo.
  > `ON DELETE CASCADE`
* **`FK_Bookings_Promotions`**: Nếu Voucher bị xóa, các đơn hàng đặt phòng áp dụng mã sẽ giữ nguyên vẹn hóa đơn gốc, chỉ thiết lập rỗng ID khuyến mãi liên kết.
  > `ON DELETE SET NULL`
* **`FK_RoomBookingDetails_Rooms`**: Cấm tuyệt đối hành vi xóa phòng vật lý khỏi cơ sở dữ liệu nếu phòng này đã phát sinh ít nhất một lượt lịch sử lưu trú.
  > `ON DELETE RESTRICT`
* **`FK_HotelOps_Staff`**: Nếu nhân viên thực hiện tác vụ bị xóa khỏi hệ thống, tác vụ buồng phòng đã lưu vết được giữ lại lịch sử nhưng để trống ID nhân sự thực hiện.
  > `ON DELETE SET NULL`
* **`FK_TourBookings_Schedules`**: Nếu một lịch khởi hành chuyến Tour đã được khách hàng đăng ký mua ghế thành công, cấm hoàn toàn hành vi xóa chuyến đi.
  > `ON DELETE RESTRICT`
* **`FK_FolioItems_Bookings`**: Khi một Booking bị hủy bỏ hoàn toàn hoặc xóa khỏi hệ thống, toàn bộ ví nợ dịch vụ phát sinh folio tương ứng bị xóa sạch.
  > `ON DELETE CASCADE`

### 2. RÀNG BUỘC DUY NHẤT (UNIQUE CONSTRAINTS)

* **`UQ_Accounts_Username`**: `UNIQUE (username)` - Tránh trùng lặp tên tài khoản đăng nhập.
* **`UQ_Employees_CCCD`**: `UNIQUE (cccd)` - Một CCCD chỉ thuộc sở hữu của một nhân sự duy nhất.
* **`UQ_Employees_Email`**: `UNIQUE (email)`.
* **`UQ_Employees_Phone`**: `UNIQUE (phone)`.
* **`UQ_Customers_Email`**: `UNIQUE (email)`.
* **`UQ_Rooms_Number`**: `UNIQUE (room_number)` - Đảm bảo tính độc nhất của mã số phòng vật lý trong Resort.
* **`UQ_RoomCategories_Name`**: `UNIQUE (category_name)`.
* **`UQ_Promotions_Code`**: `UNIQUE (promo_code)` - Mã Voucher chiến dịch là duy nhất.
* **`UQ_Tours_Name`**: `UNIQUE (tour_name)`.
* **`UQ_Menu_Items`**: `UNIQUE (item_name)`.
* **`UQ_Payment_Ref`**: `UNIQUE (transaction_ref)` - Chống duplicate giao dịch webhook từ cổng thanh toán bên thứ ba (VNPay/Momo).
* **`UQ_Consolidated_Invoices_Num`**: `UNIQUE (invoice_number)` - Mã định dạng pháp lý báo cáo Cơ quan Thuế là duy nhất.
* **`UQ_AuthorizedDevices_Code`**: `UNIQUE (device_code)` - Tránh trùng lặp mã định danh thiết bị.

> [!NOTE]
> **Ràng buộc kết hợp (Composite Unique Constraints):**
> * **`UQ_RoomBookingDetails_Room_Booking`**: `UNIQUE (booking_id, room_id)` - Chống việc một đơn đặt phòng cố tình add trùng một phòng vật lý hai lần.
> * **`UQ_TourStaff_Schedule`**: `UNIQUE (schedule_id, employee_id)` - Chống việc một nhân sự bị phân công trùng lặp hai vai trò trong cùng một chuyến xe Tour.
> * **`UQ_Review_Booking`**: `UNIQUE (customer_id, booking_id)` - Mỗi khách hàng chỉ được gửi đánh giá phản hồi cho một đơn đặt phòng đúng một lần duy nhất.

### 3. RÀNG BUỘC KIỂM TRA ĐIỀU KIỆN (CHECK CONSTRAINTS)

#### Ràng buộc Định dạng (Format Checks):
* **`CHK_Email_Format`**: `CHECK (email LIKE '%_@__%.__%')` - Kiểm tra cấu trúc hòm thư điện tử hợp lệ.
* **`CHK_Phone_Format`**: `CHECK (phone REGEXP '^[0-9]{10,12}$')` - Số điện thoại chứa từ 10 - 12 ký tự số.
* **`CHK_Customer_Identity`**: `CHECK (cccd_passport_encrypted IS NOT NULL OR account_id IS NOT NULL)` - Khách hàng phải đăng nhập tài khoản hoặc khai báo CCCD định danh.
* **`CHK_TourAttendee_Identity`**: `CHECK (customer_id IS NOT NULL OR dependent_id IS NOT NULL)` - Thành viên lên xe Tour phải xác định rõ danh tính tài khoản hoặc mã đi kèm.
* **`CHK_RoomGuest_Identity`**: `CHECK (customer_id IS NOT NULL OR dependent_id IS NOT NULL)` - Khách lưu trú phòng bắt buộc phải xác định rõ danh tính tài khoản hoặc mã đi kèm.
* **`CHK_ReviewSource`**: `CHECK ((room_booking_detail_id IS NOT NULL AND tour_booking_id IS NULL) OR (room_booking_detail_id IS NULL AND tour_booking_id IS NOT NULL))` - Đảm bảo dữ liệu đánh giá trỏ chính xác về phòng hoặc tour lẻ, loại bỏ mô hình đa hình chuỗi thô sơ.

#### Ràng buộc Số học (Numeric Bounds):
* **`CHK_Employee_Salary`**: `CHECK (salary >= 0)` - Lương nhân viên không âm.
* **`CHK_Prices`**: `CHECK (base_price >= 0)`, `CHECK (price_modifier <> 0)`, `CHECK (discount_value > 0)`, `CHECK (total_price >= 0)`, `CHECK (deposit_amount >= 0)`, `CHECK (room_charge >= 0)`, `CHECK (amount > 0)`, `CHECK (subtotal_before_vat >= 0)`, `CHECK (vat_amount >= 0)`.
* **`CHK_Capacities`**: `CHECK (capacity > 0)`, `CHECK (max_capacity > 0)`, `CHECK (participant_count > 0)`.
* **`CHK_Loyalty_Points`**: `CHECK (loyalty_points >= 0)`
* **`CHK_Promo_Uses`**: `CHECK (current_uses <= max_uses)` - Giới hạn số lượt dùng voucher.
* **`CHK_Review_Rating`**: `CHECK (rating_service BETWEEN 1 AND 5)` - Thang sao từ 1 đến 5.

#### Ràng buộc Thời gian (Temporal Logic):
* **`CHK_DynamicPrice_Dates`**: `CHECK (end_date >= start_date)`
* **`CHK_Promo_Dates`**: `CHECK (valid_to >= valid_from)`
* **`CHK_Booking_Dates`**: `CHECK (check_out_date > check_in_date)` - Quy định thời gian lưu trú tối thiểu 1 đêm tại Resort.

#### Ràng buộc Nguồn gốc Đơn hàng:
* **`CHK_FoodOrder_Source`**: `CHECK (room_booking_detail_id IS NOT NULL OR table_id IS NOT NULL OR order_type = 'WALK_IN_RESTAURANT')` - Lệnh gọi món ăn bắt buộc phải có địa điểm bàn ăn hoặc mã phòng dịch vụ.

---

## PHẦN III: HỆ THỐNG TRIGGERS NGHIỆP VỤ PHỨC TẠP (DATABASE TRIGGERS)

### 1. **`TRG_Auto_Housekeeping_Task`** (Tự động tạo tác vụ buồng phòng)
* **Sự kiện kích hoạt**: `AFTER UPDATE ON Room_Booking_Details`
* **Nghiệp vụ áp dụng**: Khi trạng thái phòng `detail_status` chuyển từ trạng thái `CHECKED_IN` sang `CHECKED_OUT` (khách hoàn tất thủ tục trả phòng), Trigger tự động thực hiện:
  1. Thêm mới một bản ghi vào bảng `Hotel_Operations` (`operational_type = 'CHECKOUT_CLEAN'`, `status = 'Pending'`, `priority = 'High'`) để phân công nhân viên buồng dọn dẹp phòng vật lý.
  2. Đồng thời, tự động chuyển đổi trạng thái phòng vật lý tại bảng Rooms thành `Rooms.room_status = 'Vacant_Dirty'`.

### 2. **`TRG_Prevent_Overbooking`** (Ngăn chặn Overbooking thời gian thực)
* **Sự kiện kích hoạt**: `BEFORE INSERT OR UPDATE ON Room_Booking_Details`
* **Nghiệp vụ áp dụng**: Trước khi gán một phòng vật lý cụ thể cho khách, trigger truy xuất khoảng thời gian lưu trú (`check_in_date` đến `check_out_date`) của phòng đó trong bảng. Nếu phát hiện ra có sự giao nhau (overlap) về thời gian lưu trú với một Booking khác đang hoạt động (trạng thái khác `Cancelled`), trigger tự động phát tín hiệu lỗi `SIGNAL SQLSTATE` để hủy bỏ giao dịch lập tức.

### 3. **`TRG_Tour_Capacity_Validator`** (Kiểm tra ghế Tour khởi hành)
* **Sự kiện kích hoạt**: `BEFORE INSERT ON Tour_Bookings`
* **Nghiệp vụ áp dụng**: Trước khi đồng ý ghi nhận đặt vé Tour của khách, trigger thực hiện lấy tổng số chỗ đã bán (`booked_seats`) cộng với số lượng ghế đặt mới (`participant_count`). Nếu tổng số lượng vượt quá sức chứa tối đa quy định của Tour (`max_capacity`), trigger sẽ chặn đứng giao dịch và ném lỗi quá số lượng cho phép.

### 4. **`TRG_Update_Tour_Booked_Seats`** (Đồng bộ số ghế Tour thực tế)
* **Sự kiện kích hoạt**: `AFTER INSERT OR UPDATE ON Tour_Bookings`
* **Nghiệp vụ áp dụng**: Mỗi khi đơn đặt tour thành công hoặc thay đổi số lượng, trigger tự động tính toán lại tổng số ghế đã bán (`SUM(participant_count)`) theo `schedule_id` tương ứng và thực hiện cập nhật đồng bộ trực tiếp vào trường dữ liệu `Tour_Schedules.booked_seats`.

### 5. **`TRG_Folio_Credit_Limit_Check`** (Kiểm soát hạn mức ghi nợ phòng)
* **Sự kiện kích hoạt**: `BEFORE INSERT ON Folio_Items`
* **Nghiệp vụ áp dụng**: Khi có yêu cầu ghi nợ một dịch vụ phát sinh ngoại vi vào phòng (như ăn uống tại nhà hàng, spa...), trigger sẽ kiểm tra cờ ghi nợ `is_charge_to_room_allowed`. Nếu cờ ở trạng thái `FALSE` hoặc tổng nợ folio hiện tại cộng thêm khoản mới vượt quá hạn mức nợ của phòng (`sub_credit_limit`), trigger tự động chặn giao dịch đẩy lỗi `403 Forbidden`, bắt buộc thanh toán tại quầy dịch vụ.

### 6. **`TRG_Menu_Availability_Sync`** (Đồng bộ nguyên liệu bếp thời gian thực)
* **Sự kiện kích hoạt**: `BEFORE INSERT ON Food_Order_Details`
* **Nghiệp vụ áp dụng**: Kiểm tra trạng thái món ăn trong thực đơn `Menu_Items.is_available`. Nếu món ăn đã chuyển trạng thái sang `FALSE` (do Bếp trưởng thông báo hết nguyên liệu đột xuất trên hệ thống KDS), trigger lập tức chặn các tác vụ thêm món này từ màn hình POS của nhân viên thu ngân.

### 7. **`TRG_Invoice_Aggregation`** (Cộng dồn tự động hóa đơn tổng)
* **Sự kiện kích hoạt**: `AFTER INSERT OR UPDATE ON Folio_Items`
* **Nghiệp vụ áp dụng**: Tự động thực hiện tính tổng `SUM(amount)` của tất cả các dịch vụ chưa tất toán lẻ (`is_settled_separately = FALSE`) thuộc về `booking_id` tương ứng. Giá trị này được cập nhật vào cột doanh thu trước thuế `subtotal_before_vat` tại bảng `Consolidated_Invoices`, đồng thời tính lại `vat_amount` và tổng chi phí sau thuế `total_amount` thời gian thực.
