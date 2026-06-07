# KAWAI RESORT & TOUR HUB - DATABASE SCHEMA (V3.1 - FULL CONSTRAINTS)

Tài liệu này mô tả chi tiết kiến trúc Cơ sở dữ liệu cho dự án Kawai Resort & Tour Hub. Ở phiên bản V3.1 này, toàn bộ cấu trúc bảng được chuẩn hóa trường dữ liệu theo đặc tả nghiệp vụ 2026, đồng thời hệ thống Ràng buộc (Constraints) và Triggers nghiệp vụ được thiết kế chi tiết để bẻ gãy mọi rủi ro dữ liệu ngay từ tầng Database Engine.

## PHẦN I: CẤU TRÚC BẢNG (TABLE STRUCTURES)

### 1. Hệ thống Người dùng & Phân quyền (Authentication & Profiles)
* **Roles**: `role_id` (INT, PK, AUTO_INCREMENT), `role_name` (VARCHAR(50), UNIQUE, NOT NULL)
  * *Dữ liệu chuẩn hóa*: 'ADMIN', 'MANAGER', 'RECEPTIONIST', 'FB_POS_STAFF', 'FB_KITCHEN_STAFF', 'HOUSEKEEPING', 'MAINTENANCE', 'TOUR_GUIDE', 'CUSTOMER'
* **Accounts**: `account_id` (INT, PK, AUTO_INCREMENT), `username` (VARCHAR(50), UNIQUE, NOT NULL), `password_hash` (VARCHAR(255), NOT NULL), `is_active` (BOOLEAN, NOT NULL), `role_id` (INT, FK -> Roles), `created_at` (TIMESTAMP DEFAULT CURRENT_TIMESTAMP)
* **Employees**: `employee_id` (INT, PK, AUTO_INCREMENT), `account_id` (INT, FK -> Accounts, UNIQUE, NULLABLE), `full_name` (VARCHAR(100), NOT NULL), `gender` (VARCHAR(10), NOT NULL), `cccd` (VARCHAR(20), UNIQUE, NOT NULL), `phone` (VARCHAR(20), NOT NULL), `email` (VARCHAR(100), UNIQUE, NOT NULL), `salary` (DECIMAL(12,2), NOT NULL)
* **Customers**: `customer_id` (INT, PK, AUTO_INCREMENT), `account_id` (INT, FK -> Accounts, UNIQUE, NULLABLE), `full_name` (VARCHAR(100), NOT NULL), `gender` (VARCHAR(10), NOT NULL), `cccd_passport_encrypted` (VARCHAR(512), UNIQUE, NULLABLE), `phone` (VARCHAR(20), NOT NULL), `email` (VARCHAR(100), UNIQUE, NOT NULL), `loyalty_points` (INT, NOT NULL), `membership_tier` (VARCHAR(30), NOT NULL)
* **Dependents**: `dependent_id` (INT, PK, AUTO_INCREMENT), `customer_id` (INT, FK -> Customers, NOT NULL), `dependent_name` (VARCHAR(100), NOT NULL), `birth_date` (DATE, NOT NULL), `gender` (VARCHAR(10), NOT NULL), `cccd_passport_encrypted` (VARCHAR(512), NULLABLE)
* **Audit_Logs**: `log_id` (INT, PK, AUTO_INCREMENT), `account_id` (INT, FK -> Accounts, NULLABLE), `action` (VARCHAR(255), NOT NULL), `table_name` (VARCHAR(100), NOT NULL), `record_id` (INT, NOT NULL), `old_value` (TEXT, NULLABLE), `new_value` (TEXT, NULLABLE), `ip_address` (VARCHAR(50), NOT NULL), `timestamp` (DATETIME, NOT NULL)

### 2. Quản lý Đặt phòng & Lưu trú (Core Hotel Booking)
* **Room_Categories**: `category_id` (INT, PK, AUTO_INCREMENT), `category_name` (VARCHAR(100), UNIQUE, NOT NULL), `base_price` (DECIMAL(12,2), NOT NULL), `capacity` (INT, NOT NULL), `description` (TEXT, NULLABLE)
* **Rooms**: `room_id` (INT, PK, AUTO_INCREMENT), `room_number` (VARCHAR(20), UNIQUE, NOT NULL), `category_id` (INT, FK -> Room_Categories, NOT NULL), `room_status` (VARCHAR(50), NOT NULL), `current_booking_detail_id` (INT, NULLABLE)
  * *Lưu ý*: `current_booking_detail_id` là con trỏ hiệu năng cấp ứng dụng phục vụ hiển thị Room Matrix Dashboard < 10ms.
* **Dynamic_Pricing**: `price_id` (INT, PK, AUTO_INCREMENT), `category_id` (INT, FK -> Room_Categories, NOT NULL), `start_date` (DATE, NOT NULL), `end_date` (DATE, NOT NULL), `price_modifier` (DECIMAL(12,2), NOT NULL), `reason` (VARCHAR(255), NULLABLE)
* **Daily_Rates** (Materialized View / Bảng Tĩnh): `daily_rate_id` (INT, PK, AUTO_INCREMENT), `category_id` (INT, FK -> Room_Categories, NOT NULL), `rate_date` (DATE, NOT NULL), `computed_price` (DECIMAL(12,2), NOT NULL), `is_weekend` (BOOLEAN, NOT NULL), `is_holiday` (BOOLEAN, NOT NULL)
* **Promotions**: `promo_id` (INT, PK, AUTO_INCREMENT), `promo_code` (VARCHAR(50), UNIQUE, NOT NULL), `discount_type` (VARCHAR(30), NOT NULL), `discount_value` (DECIMAL(10,2), NOT NULL), `valid_from` (DATETIME, NOT NULL), `valid_to` (DATE, NOT NULL), `max_uses` (INT, NOT NULL), `current_uses` (INT, NOT NULL), `is_active` (BOOLEAN, NOT NULL)
* **Bookings** (Abstract/Parent): `booking_id` (INT, PK, AUTO_INCREMENT), `customer_id` (INT, FK -> Customers, NOT NULL), `booking_date` (DATE, NOT NULL), `total_price` (DECIMAL(12,2), NOT NULL), `booking_status` (VARCHAR(50), NOT NULL), `booking_source` (VARCHAR(50), NOT NULL), `applied_promotion_id` (INT, FK -> Promotions, NULLABLE), `version` (INT, NOT NULL)
* **Room_Bookings**: `booking_id` (INT, PK, FK -> Bookings), `check_in_date` (DATE, NOT NULL), `check_out_date` (DATE, NOT NULL), `deposit_amount` (DECIMAL(12,2), NOT NULL), `cancellation_deadline` (DATE, NOT NULL), `credit_limit` (DECIMAL(12,2), NOT NULL), `personal_pin_hash` (VARCHAR(255), NOT NULL)
* **Room_Booking_Details**: `detail_id` (INT, PK, AUTO_INCREMENT), `booking_id` (INT, FK -> Room_Bookings, NOT NULL), `category_id` (INT, FK -> Room_Categories, NOT NULL), `room_id` (INT, FK -> Rooms, NULLABLE), `customer_id` (INT, FK -> Customers, NULLABLE), `dependent_id` (INT, FK -> Dependents, NULLABLE), `room_charge` (DECIMAL(12,2), NOT NULL), `detail_status` (VARCHAR(50), NOT NULL), `is_charge_to_room_allowed` (BOOLEAN, NOT NULL), `sub_credit_limit` (DECIMAL(12,2), NOT NULL), `billing_routing_strategy` (VARCHAR(30), NOT NULL)

### 3. Quản lý Tour & Điểm danh AI (Tour Hub)
* **Tours**: `tour_id` (INT, PK, AUTO_INCREMENT), `tour_name` (VARCHAR(150), UNIQUE, NOT NULL), `tour_type` (VARCHAR(30), NOT NULL), `base_price` (DECIMAL(12,2), NOT NULL), `max_capacity` (INT, NOT NULL), `description` (TEXT, NULLABLE)
* **Tour_Schedules**: `schedule_id` (INT, PK, AUTO_INCREMENT), `tour_id` (INT, FK -> Tours, NOT NULL), `departure_date` (DATE, NOT NULL), `departure_time` (TIME, NOT NULL), `booked_seats` (INT, NOT NULL), `schedule_status` (VARCHAR(50), NOT NULL)
* **Tour_Staff_Assignments**: `assignment_id` (INT, PK, AUTO_INCREMENT), `schedule_id` (INT, FK -> Tour_Schedules, NOT NULL), `employee_id` (INT, FK -> Employees, NOT NULL), `staff_role` (VARCHAR(50), NOT NULL)
* **Tour_Bookings**: `booking_id` (INT, PK, FK -> Bookings), `schedule_id` (INT, FK -> Tour_Schedules, NOT NULL), `participant_count` (INT, NOT NULL), `is_walk_in_tour` (BOOLEAN, NOT NULL)
* **Tour_Attendees**: `attendee_id` (INT, PK, AUTO_INCREMENT), `tour_booking_id` (INT, FK -> Tour_Bookings, NOT NULL), `customer_id` (INT, FK -> Customers, NULLABLE), `dependent_id` (INT, FK -> Dependents, NULLABLE), `attendance_status` (VARCHAR(50), NOT NULL), `face_matched_at` (TIMESTAMP, NULLABLE), `face_vector_data` (TEXT, NULLABLE)

### 4. Quản lý Nhà hàng & Bếp (Restaurant POS & KDS)
* **Restaurant_Tables**: `table_id` (INT, PK, AUTO_INCREMENT), `table_number` (VARCHAR(20), UNIQUE, NOT NULL), `capacity` (INT, NOT NULL), `table_status` (VARCHAR(50), NOT NULL)
* **Table_Reservations**: `reservation_id` (INT, PK, AUTO_INCREMENT), `customer_id` (INT, FK -> Customers, NOT NULL), `table_id` (INT, FK -> Restaurant_Tables, NOT NULL), `reserve_date` (DATE, NOT NULL), `reserve_time` (TIME, NOT NULL), `deposit_amount` (DECIMAL(12,2), NOT NULL), `status` (VARCHAR(50), NOT NULL)
* **Menu_Items**: `item_id` (INT, PK, AUTO_INCREMENT), `item_name` (VARCHAR(150), UNIQUE, NOT NULL), `price` (DECIMAL(12,2), NOT NULL), `category` (VARCHAR(50), NOT NULL), `is_available` (BOOLEAN, NOT NULL), `description` (TEXT, NULLABLE)
* **Food_Orders**: `order_id` (INT, PK, AUTO_INCREMENT), `booking_id` (INT, FK -> Bookings, NULLABLE), `room_booking_detail_id` (INT, FK -> Room_Booking_Details, NULLABLE), `table_id` (INT, FK -> Restaurant_Tables, NULLABLE), `order_type` (VARCHAR(50), NOT NULL), `order_status` (VARCHAR(50), NOT NULL), `payment_type` (VARCHAR(50), NOT NULL), `is_paid_in_pos` (BOOLEAN, NOT NULL), `created_by_staff_id` (INT, FK -> Employees, NOT NULL), `kitchen_processed_by_id` (INT, FK -> Employees, NULLABLE)
* **Food_Order_Details**: `detail_id` (INT, PK, AUTO_INCREMENT), `food_order_id` (INT, FK -> Food_Orders, NOT NULL), `menu_item_id` (INT, FK -> Menu_Items, NOT NULL), `quantity` (INT, NOT NULL), `price_at_order` (DECIMAL(12,2), NOT NULL), `kot_status` (VARCHAR(50), NOT NULL)

### 5. Tác vụ Nội bộ & Kế toán (Hotel Operations & Finance)
* **Hotel_Operations**: `task_id` (INT, PK, AUTO_INCREMENT), `room_id` (INT, FK -> Rooms, NOT NULL), `staff_id` (INT, FK -> Employees, NOT NULL), `supervisor_id` (INT, FK -> Employees, NOT NULL), `operational_type` (VARCHAR(50), NOT NULL), `priority` (VARCHAR(30), NOT NULL), `status` (VARCHAR(50), NOT NULL), `created_at` (TIMESTAMP DEFAULT CURRENT_TIMESTAMP), `started_at` (DATETIME, NULLABLE), `completed_at` (DATETIME, NULLABLE), `notes` (TEXT, NULLABLE)
* **Folio_Items**: `folio_item_id` (INT, PK, AUTO_INCREMENT), `booking_id` (INT, FK -> Bookings, NOT NULL), `room_booking_detail_id` (INT, FK -> Room_Booking_Details, NULLABLE), `payer_customer_id` (INT, FK -> Customers, NOT NULL), `source_department` (VARCHAR(50), NOT NULL), `amount` (DECIMAL(12,2), NOT NULL), `description` (VARCHAR(255), NOT NULL), `created_at` (TIMESTAMP DEFAULT CURRENT_TIMESTAMP), `signature_img_url` (VARCHAR(500), NULLABLE), `is_settled_separately` (BOOLEAN, NOT NULL), `created_by_staff_id` (INT, FK -> Employees, NULLABLE)
* **Consolidated_Invoices**: `invoice_id` (INT, PK, AUTO_INCREMENT), `invoice_number` (VARCHAR(50), UNIQUE, NOT NULL), `booking_id` (INT, UNIQUE, FK -> Bookings, NOT NULL), `subtotal_before_vat` (DECIMAL(12,2), NOT NULL), `vat_amount` (DECIMAL(12,2), NOT NULL), `total_amount` (DECIMAL(12,2), NOT NULL), `promo_id` (INT, FK -> Promotions, NULLABLE), `invoice_status` (VARCHAR(50), NOT NULL), `created_at` (TIMESTAMP DEFAULT CURRENT_TIMESTAMP), `issued_at` (DATETIME, NOT NULL)
* **Payment_Transactions**: `transaction_id` (INT, PK, AUTO_INCREMENT), `invoice_id` (INT, FK -> Consolidated_Invoices, NOT NULL), `booking_id` (INT, FK -> Bookings, NOT NULL), `amount` (DECIMAL(12,2), NOT NULL), `transaction_type` (VARCHAR(50), NOT NULL), `payment_method` (VARCHAR(50), NOT NULL), `gateway_status` (VARCHAR(50), NOT NULL), `transaction_ref` (VARCHAR(100), UNIQUE, NOT NULL), `created_at` (TIMESTAMP DEFAULT CURRENT_TIMESTAMP)
* **Reviews**: `review_id` (INT, PK, AUTO_INCREMENT), `customer_id` (INT, FK -> Customers, NOT NULL), `room_booking_detail_id` (INT, FK -> Room_Booking_Details, NULLABLE), `tour_booking_id` (INT, FK -> Tour_Bookings, NULLABLE), `rating_service` (INT, NOT NULL), `review_text` (TEXT, NULLABLE), `created_at` (TIMESTAMP DEFAULT CURRENT_TIMESTAMP), `moderation_status` (VARCHAR(50), NOT NULL), `moderated_by` (INT, FK -> Employees, NULLABLE), `moderation_reason` (TEXT, NULLABLE)

## PHẦN II: HỆ THỐNG RÀNG BUỘC CƠ SỞ DỮ LIỆU CHI TIẾT (COMPREHENSIVE CONSTRAINTS)

### 1. RÀNG BUỘC KHÓA NGOẠI (FOREIGN KEY CONSTRAINTS)
* **FK_Accounts_Roles**: Cấm xóa Role đang có Account sử dụng (`ON DELETE RESTRICT`, `ON UPDATE CASCADE`).
* **FK_Employees_Accounts** & **FK_Customers_Accounts**: Khi xóa Account, tự động giải phóng hoặc Cascade xóa hồ sơ thông tin Employee/Customer tương ứng (`ON DELETE CASCADE`).
* **FK_Dependents_Customers**: Xóa Customer chủ đoàn thì tự động xóa danh sách người phụ thuộc đi kèm (`ON DELETE CASCADE`).
* **FK_AuditLogs_Accounts**: Cấm xóa hoàn toàn Account nếu Account này đã phát sinh log trong hệ thống (`ON DELETE RESTRICT`).
* **FK_Rooms_Categories**: Không cho phép xóa hạng phòng nếu vẫn còn tồn tại thực thể phòng vật lý thuộc hạng đó (`ON DELETE RESTRICT`).
* **FK_DynamicPricing_Categories**: Khi cấu hình Hạng phòng bị xóa, các chiến dịch giá biến động của hạng đó sẽ bị xóa theo (`ON DELETE CASCADE`).
* **FK_Bookings_Promotions**: Nếu chương trình Khuyến mãi bị xóa, các đơn hàng tổng từng áp dụng mã này sẽ bị Set Null khóa ngoại để giữ nguyên vẹn hóa đơn gốc (`ON DELETE SET NULL`).
* **FK_RoomBookingDetails_Rooms**: Cấm tuyệt đối việc xóa một phòng vật lý khỏi danh mục nếu phòng đó đã từng được gán lịch sử lưu trú vào bất kỳ Booking nào (`ON DELETE RESTRICT`).
* **FK_HotelOps_Staff**: Nếu nhân viên vận hành bị xóa Account, tác vụ họ đã thực hiện sẽ được giữ lại lịch sử nhưng ẩn định danh (`ON DELETE SET NULL`).
* **FK_TourBookings_Schedules**: Nếu lịch trình xe Tour đã có khách đăng ký mua vé, cấm xóa chuyến đi đó (`ON DELETE RESTRICT`).
* **FK_FolioItems_Bookings**: Khi một Booking bị hủy sâu hoặc xóa bỏ, toàn bộ ví folio nợ phát sinh đi kèm sẽ bị dọn sạch (`ON DELETE CASCADE`).

### 2. RÀNG BUỘC DUY NHẤT (UNIQUE CONSTRAINTS)
* **UQ_Accounts_Username**: `UNIQUE (username)` - Tên đăng nhập hệ thống không trùng lặp.
* **UQ_Employees_CCCD**: `UNIQUE (cccd)` - Một CCCD chỉ gắn liền với một mã nhân sự duy nhất.
* **UQ_Employees_Email**: `UNIQUE (email)`.
* **UQ_Employees_Phone**: `UNIQUE (phone)`.
* **UQ_Customers_Email**: `UNIQUE (email)`.
* **UQ_Rooms_Number**: `UNIQUE (room_number)` - Đảm bảo tính định danh duy nhất của số phòng vật lý trong Resort.
* **UQ_RoomCategories_Name**: `UNIQUE (category_name)`.
* **UQ_Promotions_Code**: `UNIQUE (promo_code)` - Mã Voucher chiến dịch là độc nhất.
* **UQ_Tours_Name**: `UNIQUE (tour_name)`.
* **UQ_Menu_Items**: `UNIQUE (item_name)`.
* **UQ_Payment_Ref**: `UNIQUE (transaction_ref)` - Chống duplicate giao dịch webhook từ cổng thanh toán bên thứ ba (VNPay/Momo).
* **UQ_Consolidated_Invoices_Num**: `UNIQUE (invoice_number)` - Mã định dạng pháp lý báo cáo Cơ quan Thuế là duy nhất.

**Ràng buộc kết hợp (Composite Unique):**
* **UQ_RoomBookingDetails_Room_Booking**: `UNIQUE (booking_id, room_id)` - Khóa chống việc một đơn đặt phòng cố tình add trùng một phòng vật lý hai lần.
* **UQ_TourStaff_Schedule**: `UNIQUE (schedule_id, employee_id)` - Chống việc một nhân sự bị phân công trùng lặp hai vai trò trong cùng một chuyến xe Tour.
* **UQ_Review_Booking**: `UNIQUE (customer_id, booking_id)` - Mỗi khách hàng chỉ được gửi đánh giá phản hồi cho một đơn đặt phòng đúng một lần duy nhất.

### 3. RÀNG BUỘC KIỂM TRA ĐIỀU KIỆN (CHECK CONSTRAINTS)
**Ràng buộc Định dạng (Format Checks):**
* **CHK_Email_Format**: `CHECK (email LIKE '%_@__%.__%')`
* **CHK_Phone_Format**: `CHECK (phone REGEXP '^[0-9]{10,12}$')`
* **CHK_Customer_Identity**: `CHECK (cccd_passport_encrypted IS NOT NULL OR account_id IS NOT NULL)`
* **CHK_TourAttendee_Identity**: `CHECK (customer_id IS NOT NULL OR dependent_id IS NOT NULL)` - Thành viên lên xe Tour phải xác định rõ danh tính tài khoản hoặc mã đi kèm.
* **CHK_RoomGuest_Identity**: `CHECK (customer_id IS NOT NULL OR dependent_id IS NOT NULL)` - Khách lưu trú phòng bắt buộc phải xác định rõ danh tính tài khoản hoặc mã đi kèm.
* **CHK_ReviewSource**: `CHECK ((room_booking_detail_id IS NOT NULL AND tour_booking_id IS NULL) OR (room_booking_detail_id IS NULL AND tour_booking_id IS NOT NULL))` - Đảm bảo dữ liệu đánh giá trỏ chính xác về phòng hoặc tour lẻ, loại bỏ mô hình đa hình chuỗi thô sơ.

**Ràng buộc Số học (Numeric Bounds):**
* **CHK_Employee_Salary**: `CHECK (salary >= 0)`
* **CHK_Prices**: `CHECK (base_price >= 0)`, `CHECK (price_modifier <> 0)`, `CHECK (discount_value > 0)`, `CHECK (total_price >= 0)`, `CHECK (deposit_amount >= 0)`, `CHECK (room_charge >= 0)`, `CHECK (amount > 0)`, `CHECK (subtotal_before_vat >= 0)`, `CHECK (vat_amount >= 0)`.
* **CHK_Capacities**: `CHECK (capacity > 0)`, `CHECK (max_capacity > 0)`, `CHECK (participant_count > 0)`.
* **CHK_Loyalty_Points**: `CHECK (loyalty_points >= 0)`
* **CHK_Promo_Uses**: `CHECK (current_uses <= max_uses)`
* **CHK_Review_Rating**: `CHECK (rating_service BETWEEN 1 AND 5)`

**Ràng buộc Thời gian (Temporal Logic):**
* **CHK_DynamicPrice_Dates**: `CHECK (end_date >= start_date)`
* **CHK_Promo_Dates**: `CHECK (valid_to >= valid_from)`
* **CHK_Booking_Dates**: `CHECK (check_out_date > check_in_date)` - Quy định thời gian lưu trú tối thiểu 1 đêm tại Resort.

**Ràng buộc Nguồn gốc Đơn hàng:**
* **CHK_FoodOrder_Source**: `CHECK (room_booking_detail_id IS NOT NULL OR table_id IS NOT NULL OR order_type = 'WALK_IN_RESTAURANT')` - Lệnh gọi món ăn bắt buộc phải có địa điểm bàn ăn hoặc mã phòng dịch vụ.

### 4. RÀNG BUỘC GIÁ TRỊ MẶC ĐỊNH (DEFAULT CONSTRAINTS)
* **DF_Account_Status**: `DEFAULT TRUE`
* **DF_Customer_Points**: `DEFAULT 0`
* **DF_Customer_Tier**: `DEFAULT 'Regular'`
* **DF_Room_Status**: `DEFAULT 'Vacant_Clean'`
* **DF_Promo_Uses**: `DEFAULT 0`
* **DF_Booking_Status**: `DEFAULT 'Pending'`
* **DF_Booking_Source**: `DEFAULT 'Direct_Web'`
* **DF_Room_Booking_Credit**: `DEFAULT 5000000.00`
* **DF_Detail_Status**: `DEFAULT 'Pending'`
* **DF_Charge_Allowed**: `DEFAULT TRUE`
* **DF_Sub_Credit_Limit**: `DEFAULT 0.00`
* **DF_Routing_Strategy**: `DEFAULT 'BILL_TO_LEADER'`
* **DF_Tour_Max_Cap**: `DEFAULT 30`
* **DF_Tour_Seats**: `DEFAULT 0`
* **DF_Tour_Schedule_Status**: `DEFAULT 'Open'`
* **DF_Tour_Booking_Attendance**: `DEFAULT 'Not_Show'`
* **DF_Is_Walk_In_Tour**: `DEFAULT FALSE`
* **DF_Table_Status**: `DEFAULT 'Vacant'`
* **DF_Menu_Is_Available**: `DEFAULT TRUE`
* **DF_Order_Status**: `DEFAULT 'Pending'`
* **DF_KOT_Status**: `DEFAULT 'Pending'`
* **DF_Order_Is_Paid_POS**: `DEFAULT FALSE`
* **DF_Task_Priority**: `DEFAULT 'Normal'`
* **DF_Task_Status**: `DEFAULT 'Pending'`
* **DF_Folio_Is_Settled_Sep**: `DEFAULT FALSE`
* **DF_Invoice_Status**: `DEFAULT 'Draft'`
* **DF_Review_Moderation**: `DEFAULT 'Pending'`

## PHẦN III: HỆ THỐNG TRIGGERS NGHIỆP VỤ PHỨC TẠP (DATABASE TRIGGERS)

### TRG_Auto_Housekeeping_Task (Housekeeping Automation)
* **Event**: `AFTER UPDATE ON Room_Booking_Details`
* **Logic**: Khi `detail_status` chuyển từ trạng thái `CHECKED_IN` sang `CHECKED_OUT`, Trigger tự động khởi tạo và chèn một bản ghi mới vào bảng `Hotel_Operations` (`operational_type = 'CHECKOUT_CLEAN'`, `status = 'Pending'`, `priority = 'High'`) gán cho `room_id` tương ứng, đồng thời cập nhật trạng thái phòng vật lý `Rooms.room_status = 'Vacant_Dirty'`.

### TRG_Prevent_Overbooking (Realtime Overbooking Guard)
* **Event**: `BEFORE INSERT OR UPDATE ON Room_Booking_Details`
* **Logic**: Kiểm tra khoảng thời gian lưu trú (`check_in_date` đến `check_out_date`) của phòng vật lý sắp được gán. Nếu phát hiện có bất kỳ dòng dữ liệu nào của phòng đó bị giao nhau (overlap) trong bảng chi tiết và trạng thái đơn hàng khác `Cancelled`, thực hiện `SIGNAL SQLSTATE` để `RAISE ERROR`, hủy bỏ giao dịch lập tức.

### TRG_Tour_Capacity_Validator (Tour Seats Safe Check)
* **Event**: `BEFORE INSERT ON Tour_Bookings`
* **Logic**: Truy xuất tổng `booked_seats` hiện tại của mã chuyến đi `Tour_Schedules` cộng với số lượng người đăng ký mới `participant_count`. Nếu tổng số lượng vượt quá `max_capacity` được định nghĩa trong danh mục `Tours`, hệ thống tự động bẻ gãy giao dịch bằng một ngoại lệ lỗi quá tải ghế.

### TRG_Update_Tour_Booked_Seats (Realtime Seats Aggregation)
* **Event**: `AFTER INSERT OR UPDATE ON Tour_Bookings`
* **Logic**: Tính toán tổng số ghế đã đặt (`SUM(participant_count)`) theo `schedule_id` và tiến hành đồng bộ ghi đè trực tiếp vào cột dữ liệu chạy thời gian thực `Tour_Schedules.booked_seats`.

### TRG_Folio_Credit_Limit_Check (Realtime Credit Guard)
* **Event**: `BEFORE INSERT ON Folio_Items`
* **Logic**: Khi nhân viên POS hoặc hệ thống tự động thực hiện đẩy một khoản nợ dịch vụ phát sinh vào phòng (`source_department <> 'ROOM'`), Trigger sẽ truy vấn bản ghi chi tiết phòng `Room_Booking_Details`. Nếu cờ `is_charge_to_room_allowed = FALSE` hoặc tổng lượng nợ hiện tại cộng với số tiền mới vượt quá hạn mức nợ con (`sub_credit_limit`), hệ thống sẽ trả về mã lỗi 403 Forbidden để khóa tính năng ký nợ, ép thanh toán trực tiếp tại chỗ.

### TRG_Menu_Availability_Sync (POS-KDS Realtime Lock)
* **Event**: `BEFORE INSERT ON Food_Order_Details`
* **Logic**: Kiểm tra trạng thái sẵn có của món ăn trong bảng thực đơn `Menu_Items.is_available`. Nếu Bếp trưởng đã chuyển trạng thái sang `FALSE` (do hết nguyên liệu đột xuất), Trigger sẽ chặn đứng mọi hành động chèn món ăn này từ các thiết bị đầu cuối POS của Nhân viên Thu ngân.

### TRG_Invoice_Aggregation (Automated Financial Compounding)
* **Event**: `AFTER INSERT OR UPDATE ON Folio_Items`
* **Logic**: Tự động thực hiện hàm gộp `SUM(amount)` của toàn bộ các dịch vụ chưa tất toán lẻ (`is_settled_separately = FALSE`) thuộc về `booking_id` tổng, cập nhật giá trị vào trường doanh thu thuần `subtotal_before_vat` của bảng `Consolidated_Invoices`, đồng thời tự động tính toán lại mức thuế quy định `vat_amount` và tổng giá trị sau thuế `total_amount` thời gian thực.