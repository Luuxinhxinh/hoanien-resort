# Cấu trúc cơ sở dữ liệu (Database Schema)

### 1.3.1 Accounts

[Bảng lưu trữ thông tin về Accounts]
[Table storing information about Accounts]

[Provide the detailed table fields description using below table format

* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field                 | PK | FK | UN | NN | Description                                                                                                                                                                    |
| -- | --------------------- | -- | -- | -- | -- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| 01 | account_id            | x  |    |    | x  | - Unique identifier for the Accounts record- Định danh duy nhất (ID) cho bản ghi Accounts- Kiểu dữ liệu: **BIGINT**                                               |
| 02 | username              |    |    | x  | x  | - The specific name or title associated with the Accounts- Tên gọi hoặc tiêu đề cụ thể của Accounts- Kiểu dữ liệu: **VARCHAR(255)**                          |
| 03 | password_hash         |    |    |    | x  | - Secure or encrypted data representing password hash- Dữ liệu bảo mật hoặc mã hóa đại diện cho password hash- Kiểu dữ liệu: **VARCHAR(255)**               |
| 04 | is_active             |    |    |    | x  | - Boolean flag indicating whether the record is active- Cờ trạng thái (đúng/sai) để xác định is active- Kiểu dữ liệu: **BIT**                               |
| 05 | role_id               |    | x  |    | x  | - Foreign key reference to the related role record- Khóa ngoại tham chiếu đến dữ liệu role liên quan- Kiểu dữ liệu: **BIGINT**                                |
| 06 | created_at            |    |    |    |    | - Timestamp indicating when the record was initially created- Thời điểm bản ghi được tạo ra lần đầu trên hệ thống- Kiểu dữ liệu: **DATETIME**           |
| 07 | failed_login_attempts |    |    |    |    | - Numeric value indicating the failed login attempts- Giá trị số lượng thể hiện failed login attempts- Kiểu dữ liệu: **INT**                                   |
| 08 | lockout_time          |    |    |    |    | - The specific lockout time for the event or record- Mốc thời gian hoặc ngày tháng cụ thể của lockout time- Kiểu dữ liệu: **DATETIME**                        |
| 09 | two_factor_code       |    |    |    |    | - Secure or encrypted data representing two factor code- Dữ liệu bảo mật hoặc mã hóa đại diện cho two factor code- Kiểu dữ liệu: **VARCHAR(255)**           |
| 10 | two_factor_expiry     |    |    |    |    | - Detailed information about the two factor expiry- Dữ liệu chi tiết về two factor expiry của hệ thống- Kiểu dữ liệu: **DATETIME**                             |
| 11 | reset_password_token  |    |    |    |    | - Secure or encrypted data representing reset password token- Dữ liệu bảo mật hoặc mã hóa đại diện cho reset password token- Kiểu dữ liệu: **VARCHAR(255)** |
| 12 | reset_password_expiry |    |    |    |    | - Secure or encrypted data representing reset password expiry- Dữ liệu bảo mật hoặc mã hóa đại diện cho reset password expiry- Kiểu dữ liệu: **DATETIME**   |

### 1.3.2 Audit_Logs

[Bảng lưu trữ thông tin về Audit Logs]
[Table storing information about Audit Logs]

[Provide the detailed table fields description using below table format

* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field      | PK | FK | UN | NN | Description                                                                                                                                               |
| -- | ---------- | -- | -- | -- | -- | --------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 01 | log_id     | x  |    |    | x  | - Unique identifier for the Audit Logs record- Định danh duy nhất (ID) cho bản ghi Audit Logs- Kiểu dữ liệu: **BIGINT**                      |
| 02 | account_id |    | x  |    |    | - Foreign key reference to the related account record- Khóa ngoại tham chiếu đến dữ liệu account liên quan- Kiểu dữ liệu: **BIGINT**     |
| 03 | action     |    |    |    |    | - Detailed information about the action- Dữ liệu chi tiết về action của hệ thống- Kiểu dữ liệu: **VARCHAR(255)**                          |
| 04 | table_name |    |    |    |    | - The specific name or title associated with the Audit Logs- Tên gọi hoặc tiêu đề cụ thể của Audit Logs- Kiểu dữ liệu: **VARCHAR(255)** |
| 05 | record_id  |    |    |    |    | - ID value referencing the record record (Logical relation)- Trường ID tham chiếu logic đến dữ liệu record- Kiểu dữ liệu: **BIGINT**      |
| 06 | old_value  |    |    |    |    | - Detailed information about the old value- Dữ liệu chi tiết về old value của hệ thống- Kiểu dữ liệu: **TEXT**                            |
| 07 | new_value  |    |    |    |    | - Detailed information about the new value- Dữ liệu chi tiết về new value của hệ thống- Kiểu dữ liệu: **TEXT**                            |
| 08 | ip_address |    |    |    |    | - User contact information (ip address)- Thông tin liên lạc cá nhân (ip address)- Kiểu dữ liệu: **VARCHAR(255)**                            |
| 09 | timestamp  |    |    |    |    | - The specific timestamp for the event or record- Mốc thời gian hoặc ngày tháng cụ thể của timestamp- Kiểu dữ liệu: **DATETIME**         |

### 1.3.3 Authorized_Devices

[Bảng lưu trữ thông tin về Authorized Devices]
[Table storing information about Authorized Devices]

[Provide the detailed table fields description using below table format

* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field       | PK | FK | UN | NN | Description                                                                                                                                                  |
| -- | ----------- | -- | -- | -- | -- | ------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| 01 | id          | x  |    |    | x  | - Unique identifier for the Authorized Devices record- Định danh duy nhất (ID) cho bản ghi Authorized Devices- Kiểu dữ liệu: **BIGINT**         |
| 02 | device_code |    |    | x  | x  | - Secure or encrypted data representing device code- Dữ liệu bảo mật hoặc mã hóa đại diện cho device code- Kiểu dữ liệu: **VARCHAR(255)** |
| 03 | is_approved |    |    |    | x  | - Boolean flag indicating whether the record is approved- Cờ trạng thái (đúng/sai) để xác định is approved- Kiểu dữ liệu: **BIT**         |

### 1.3.4 Bookings

[Bảng lưu trữ thông tin về Bookings]
[Table storing information about Bookings]

[Provide the detailed table fields description using below table format

* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field                | PK | FK | UN | NN | Description                                                                                                                                                               |
| -- | -------------------- | -- | -- | -- | -- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 01 | booking_id           | x  |    |    | x  | - Unique identifier for the Bookings record- Định danh duy nhất (ID) cho bản ghi Bookings- Kiểu dữ liệu: **BIGINT**                                          |
| 02 | customer_id          |    | x  |    | x  | - Foreign key reference to the related customer record- Khóa ngoại tham chiếu đến dữ liệu customer liên quan- Kiểu dữ liệu: **BIGINT**                   |
| 03 | booking_date         |    |    |    | x  | - The specific booking date for the event or record- Mốc thời gian hoặc ngày tháng cụ thể của booking date- Kiểu dữ liệu: **DATE**                       |
| 04 | total_price          |    |    |    | x  | - Financial value representing the total price- Giá trị tiền tệ thể hiện total price- Kiểu dữ liệu: **DECIMAL(10,2)**                                      |
| 05 | booking_status       |    |    |    | x  | - The current operational status or state of the record- Trạng thái hoạt động hoặc tình trạng hiện tại của bản ghi- Kiểu dữ liệu: **VARCHAR(255)**   |
| 06 | booking_source       |    |    |    | x  | - Detailed information about the booking source- Dữ liệu chi tiết về booking source của hệ thống- Kiểu dữ liệu: **VARCHAR(255)**                          |
| 07 | hold_expires_at      |    |    |    |    | - Detailed information about the hold expires at- Dữ liệu chi tiết về hold expires at của hệ thống- Kiểu dữ liệu: **DATETIME**                            |
| 08 | applied_promotion_id |    | x  |    |    | - Foreign key reference to the related applied promotion record- Khóa ngoại tham chiếu đến dữ liệu applied promotion liên quan- Kiểu dữ liệu: **BIGINT** |
| 09 | version              |    |    |    | x  | - Detailed information about the version- Dữ liệu chi tiết về version của hệ thống- Kiểu dữ liệu: **INT**                                                 |
| 10 | notes                |    |    |    |    | - Additional text description or notes for the record- Mô tả chi tiết bằng văn bản hoặc các ghi chú bổ sung- Kiểu dữ liệu: **TEXT**                    |

### 1.3.5 Booking_Services

[Bảng lưu trữ thông tin về Booking Services]
[Table storing information about Booking Services]

[Provide the detailed table fields description using below table format

* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field                  | PK | FK | UN | NN | Description                                                                                                                                                             |
| -- | ---------------------- | -- | -- | -- | -- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 01 | booking_service_id     | x  |    |    | x  | - Unique identifier for the Booking Services record- Định danh duy nhất (ID) cho bản ghi Booking Services- Kiểu dữ liệu: **BIGINT**                        |
| 02 | booking_id             |    | x  |    | x  | - Foreign key reference to the related booking record- Khóa ngoại tham chiếu đến dữ liệu booking liên quan- Kiểu dữ liệu: **BIGINT**                   |
| 03 | service_id             |    | x  |    | x  | - Foreign key reference to the related service record- Khóa ngoại tham chiếu đến dữ liệu service liên quan- Kiểu dữ liệu: **BIGINT**                   |
| 04 | quantity               |    |    |    | x  | - Numeric value indicating the quantity- Giá trị số lượng thể hiện quantity- Kiểu dữ liệu: **INT**                                                      |
| 05 | unit_price             |    |    |    | x  | - Financial value representing the unit price- Giá trị tiền tệ thể hiện unit price- Kiểu dữ liệu: **DECIMAL(10,2)**                                      |
| 06 | execution_date         |    |    |    | x  | - The specific execution date for the event or record- Mốc thời gian hoặc ngày tháng cụ thể của execution date- Kiểu dữ liệu: **DATETIME**             |
| 07 | status                 |    |    |    | x  | - The current operational status or state of the record- Trạng thái hoạt động hoặc tình trạng hiện tại của bản ghi- Kiểu dữ liệu: **VARCHAR(255)** |
| 08 | specific_requests_json |    |    |    |    | - Detailed information about the specific requests json- Dữ liệu chi tiết về specific requests json của hệ thống- Kiểu dữ liệu: **TEXT**                |

### 1.3.6 Checkpoint_Attendance

[Bảng lưu trữ thông tin về Checkpoint Attendance]
[Table storing information about Checkpoint Attendance]

[Provide the detailed table fields description using below table format

* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field               | PK | FK | UN | NN | Description                                                                                                                                                             |
| -- | ------------------- | -- | -- | -- | -- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 01 | checkpoint_id       | x  |    |    | x  | - Unique identifier for the Checkpoint Attendance record- Định danh duy nhất (ID) cho bản ghi Checkpoint Attendance- Kiểu dữ liệu: **BIGINT**              |
| 02 | schedule_id         |    | x  |    | x  | - Foreign key reference to the related schedule record- Khóa ngoại tham chiếu đến dữ liệu schedule liên quan- Kiểu dữ liệu: **BIGINT**                 |
| 03 | attendee_id         |    | x  |    | x  | - Foreign key reference to the related attendee record- Khóa ngoại tham chiếu đến dữ liệu attendee liên quan- Kiểu dữ liệu: **BIGINT**                 |
| 04 | detail_id           |    | x  |    | x  | - Foreign key reference to the related detail record- Khóa ngoại tham chiếu đến dữ liệu detail liên quan- Kiểu dữ liệu: **BIGINT**                     |
| 05 | scan_status         |    |    |    | x  | - The current operational status or state of the record- Trạng thái hoạt động hoặc tình trạng hiện tại của bản ghi- Kiểu dữ liệu: **VARCHAR(255)** |
| 06 | scanned_at          |    |    |    | x  | - Detailed information about the scanned at- Dữ liệu chi tiết về scanned at của hệ thống- Kiểu dữ liệu: **DATETIME**                                    |
| 07 | scanned_by_staff_id |    | x  |    | x  | - Foreign key reference to the related scanned by staff record- Khóa ngoại tham chiếu đến dữ liệu scanned by staff liên quan- Kiểu dữ liệu: **BIGINT** |

### 1.3.7 Consolidated_Invoices

[Bảng lưu trữ thông tin về Consolidated Invoices]
[Table storing information about Consolidated Invoices]

[Provide the detailed table fields description using below table format

* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field               | PK | FK | UN | NN | Description                                                                                                                                                             |
| -- | ------------------- | -- | -- | -- | -- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 01 | invoice_id          | x  |    |    | x  | - Unique identifier for the Consolidated Invoices record- Định danh duy nhất (ID) cho bản ghi Consolidated Invoices- Kiểu dữ liệu: **BIGINT**              |
| 02 | invoice_number      |    |    |    |    | - Detailed information about the invoice number- Dữ liệu chi tiết về invoice number của hệ thống- Kiểu dữ liệu: **VARCHAR(255)**                        |
| 03 | booking_id          |    | x  |    |    | - Foreign key reference to the related booking record- Khóa ngoại tham chiếu đến dữ liệu booking liên quan- Kiểu dữ liệu: **BIGINT**                   |
| 04 | subtotal_before_vat |    |    |    |    | - Detailed information about the subtotal before vat- Dữ liệu chi tiết về subtotal before vat của hệ thống- Kiểu dữ liệu: **DECIMAL(10,2)**             |
| 05 | vat_amount          |    |    |    |    | - Financial value representing the vat amount- Giá trị tiền tệ thể hiện vat amount- Kiểu dữ liệu: **DECIMAL(10,2)**                                      |
| 06 | total_amount        |    |    |    |    | - Financial value representing the total amount- Giá trị tiền tệ thể hiện total amount- Kiểu dữ liệu: **DECIMAL(10,2)**                                  |
| 07 | promo_id            |    | x  |    |    | - Foreign key reference to the related promo record- Khóa ngoại tham chiếu đến dữ liệu promo liên quan- Kiểu dữ liệu: **BIGINT**                       |
| 08 | invoice_status      |    |    |    |    | - The current operational status or state of the record- Trạng thái hoạt động hoặc tình trạng hiện tại của bản ghi- Kiểu dữ liệu: **VARCHAR(255)** |
| 09 | created_at          |    |    |    |    | - Timestamp indicating when the record was initially created- Thời điểm bản ghi được tạo ra lần đầu trên hệ thống- Kiểu dữ liệu: **DATETIME**    |
| 10 | issued_at           |    |    |    |    | - Detailed information about the issued at- Dữ liệu chi tiết về issued at của hệ thống- Kiểu dữ liệu: **DATETIME**                                      |

### 1.3.8 Customers

[Bảng lưu trữ thông tin về Customers]
[Table storing information about Customers]

[Provide the detailed table fields description using below table format

* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field                   | PK | FK | UN | NN | Description                                                                                                                                                                          |
| -- | ----------------------- | -- | -- | -- | -- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| 01 | customer_id             | x  |    |    | x  | - Unique identifier for the Customers record- Định danh duy nhất (ID) cho bản ghi Customers- Kiểu dữ liệu: **BIGINT**                                                   |
| 02 | account_id              |    | x  | x  |    | - Foreign key reference to the related account record- Khóa ngoại tham chiếu đến dữ liệu account liên quan- Kiểu dữ liệu: **BIGINT**                                |
| 03 | full_name               |    |    |    | x  | - The specific name or title associated with the Customers- Tên gọi hoặc tiêu đề cụ thể của Customers- Kiểu dữ liệu: **VARCHAR(255)**                              |
| 04 | gender                  |    |    |    | x  | - Categorization or classification type (gender)- Loại, danh mục hoặc phân loại của dữ liệu (gender)- Kiểu dữ liệu: **VARCHAR(255)**                                |
| 05 | cccd_passport_encrypted |    |    | x  |    | - Secure or encrypted data representing cccd passport encrypted- Dữ liệu bảo mật hoặc mã hóa đại diện cho cccd passport encrypted- Kiểu dữ liệu: **VARCHAR(255)** |
| 06 | phone                   |    |    |    | x  | - User contact information (phone)- Thông tin liên lạc cá nhân (phone)- Kiểu dữ liệu: **VARCHAR(255)**                                                                 |
| 07 | email                   |    |    | x  | x  | - User contact information (email)- Thông tin liên lạc cá nhân (email)- Kiểu dữ liệu: **VARCHAR(255)**                                                                 |
| 08 | birth_date              |    |    |    |    | - The specific birth date for the event or record- Mốc thời gian hoặc ngày tháng cụ thể của birth date- Kiểu dữ liệu: **DATE**                                      |
| 09 | loyalty_points          |    |    |    | x  | - Detailed information about the loyalty points- Dữ liệu chi tiết về loyalty points của hệ thống- Kiểu dữ liệu: **INT**                                              |
| 10 | membership_tier_id      |    | x  |    | x  | - Foreign key reference to the related membership tier record- Khóa ngoại tham chiếu đến dữ liệu membership tier liên quan- Kiểu dữ liệu: **BIGINT**                |
| 11 | avatar_url              |    |    |    |    | - Direct URL or system data for the avatar url file- Dữ liệu hoặc đường dẫn (URL) tới avatar url- Kiểu dữ liệu: **VARCHAR(255)**                                    |
| 12 | face_vector_data        |    |    |    |    | - Direct URL or system data for the face vector data file- Dữ liệu hoặc đường dẫn (URL) tới face vector data- Kiểu dữ liệu: **TEXT**                                |
| 13 | face_img_url            |    |    |    |    | - Direct URL or system data for the face img url file- Dữ liệu hoặc đường dẫn (URL) tới face img url- Kiểu dữ liệu: **VARCHAR(500)**                                |
| 14 | address                 |    |    |    |    | - User contact information (address)- Thông tin liên lạc cá nhân (address)- Kiểu dữ liệu: **VARCHAR(255)**                                                             |

### 1.3.9 revinfo_custom

[Bảng lưu trữ thông tin về revinfo custom]
[Table storing information about revinfo custom]

[Provide the detailed table fields description using below table format

* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field    | PK | FK | UN | NN | Description                                                                                                                                               |
| -- | -------- | -- | -- | -- | -- | --------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 01 | id       | x  |    |    | x  | - Unique identifier for the revinfo custom record- Định danh duy nhất (ID) cho bản ghi revinfo custom- Kiểu dữ liệu: **INT**                 |
| 02 | REVTSTMP |    |    |    |    | - Detailed information about the REVTSTMP- Dữ liệu chi tiết về REVTSTMP của hệ thống- Kiểu dữ liệu: **BIGINT**                            |
| 03 | username |    |    |    |    | - The specific name or title associated with the revinfo custom- Tên gọi hoặc tiêu đề cụ thể của revinfo custom- Kiểu dữ liệu: **TEXT** |

### 1.3.10 Daily_Rates

[Bảng lưu trữ thông tin về Daily Rates]
[Table storing information about Daily Rates]

[Provide the detailed table fields description using below table format

* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field          | PK | FK | UN | NN | Description                                                                                                                                             |
| -- | -------------- | -- | -- | -- | -- | ------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 01 | daily_rate_id  | x  |    |    | x  | - Unique identifier for the Daily Rates record- Định danh duy nhất (ID) cho bản ghi Daily Rates- Kiểu dữ liệu: **BIGINT**                  |
| 02 | category_id    |    | x  |    | x  | - Foreign key reference to the related category record- Khóa ngoại tham chiếu đến dữ liệu category liên quan- Kiểu dữ liệu: **BIGINT** |
| 03 | rate_date      |    |    |    | x  | - The specific rate date for the event or record- Mốc thời gian hoặc ngày tháng cụ thể của rate date- Kiểu dữ liệu: **DATE**           |
| 04 | computed_price |    |    |    | x  | - Financial value representing the computed price- Giá trị tiền tệ thể hiện computed price- Kiểu dữ liệu: **DECIMAL(10,2)**              |
| 05 | is_weekend     |    |    |    | x  | - Boolean flag indicating whether the record is weekend- Cờ trạng thái (đúng/sai) để xác định is weekend- Kiểu dữ liệu: **BIT**      |
| 06 | is_holiday     |    |    |    | x  | - Boolean flag indicating whether the record is holiday- Cờ trạng thái (đúng/sai) để xác định is holiday- Kiểu dữ liệu: **BIT**      |

### 1.3.11 Dependents

[Bảng lưu trữ thông tin về Dependents]
[Table storing information about Dependents]

[Provide the detailed table fields description using below table format

* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field                   | PK | FK | UN | NN | Description                                                                                                                                                                          |
| -- | ----------------------- | -- | -- | -- | -- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| 01 | dependent_id            | x  |    |    | x  | - Unique identifier for the Dependents record- Định danh duy nhất (ID) cho bản ghi Dependents- Kiểu dữ liệu: **BIGINT**                                                 |
| 02 | customer_id             |    | x  |    | x  | - Foreign key reference to the related customer record- Khóa ngoại tham chiếu đến dữ liệu customer liên quan- Kiểu dữ liệu: **BIGINT**                              |
| 03 | dependent_name          |    |    |    |    | - The specific name or title associated with the Dependents- Tên gọi hoặc tiêu đề cụ thể của Dependents- Kiểu dữ liệu: **VARCHAR(255)**                            |
| 04 | birth_date              |    |    |    | x  | - The specific birth date for the event or record- Mốc thời gian hoặc ngày tháng cụ thể của birth date- Kiểu dữ liệu: **DATE**                                      |
| 05 | gender                  |    |    |    |    | - Categorization or classification type (gender)- Loại, danh mục hoặc phân loại của dữ liệu (gender)- Kiểu dữ liệu: **VARCHAR(255)**                                |
| 06 | cccd_passport_encrypted |    |    |    |    | - Secure or encrypted data representing cccd passport encrypted- Dữ liệu bảo mật hoặc mã hóa đại diện cho cccd passport encrypted- Kiểu dữ liệu: **VARCHAR(255)** |
| 07 | face_vector_data        |    |    |    |    | - Direct URL or system data for the face vector data file- Dữ liệu hoặc đường dẫn (URL) tới face vector data- Kiểu dữ liệu: **TEXT**                                |
| 08 | face_img_url            |    |    |    |    | - Direct URL or system data for the face img url file- Dữ liệu hoặc đường dẫn (URL) tới face img url- Kiểu dữ liệu: **VARCHAR(500)**                                |
| 09 | is_deleted              |    |    |    |    | - Boolean flag indicating whether the record is deleted- Cờ trạng thái (đúng/sai) để xác định is deleted- Kiểu dữ liệu: **BIT**                                   |

### 1.3.12 Dynamic_Pricing

[Bảng lưu trữ thông tin về Dynamic Pricing]
[Table storing information about Dynamic Pricing]

[Provide the detailed table fields description using below table format

* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field          | PK | FK | UN | NN | Description                                                                                                                                             |
| -- | -------------- | -- | -- | -- | -- | ------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 01 | price_id       | x  |    |    | x  | - Unique identifier for the Dynamic Pricing record- Định danh duy nhất (ID) cho bản ghi Dynamic Pricing- Kiểu dữ liệu: **BIGINT**          |
| 02 | category_id    |    | x  |    |    | - Foreign key reference to the related category record- Khóa ngoại tham chiếu đến dữ liệu category liên quan- Kiểu dữ liệu: **BIGINT** |
| 03 | start_date     |    |    |    |    | - The specific start date for the event or record- Mốc thời gian hoặc ngày tháng cụ thể của start date- Kiểu dữ liệu: **DATE**         |
| 04 | end_date       |    |    |    |    | - The specific end date for the event or record- Mốc thời gian hoặc ngày tháng cụ thể của end date- Kiểu dữ liệu: **DATE**             |
| 05 | price_modifier |    |    |    |    | - Financial value representing the price modifier- Giá trị tiền tệ thể hiện price modifier- Kiểu dữ liệu: **DECIMAL(10,2)**              |
| 06 | reason         |    |    |    |    | - Detailed information about the reason- Dữ liệu chi tiết về reason của hệ thống- Kiểu dữ liệu: **VARCHAR(255)**                        |

### 1.3.13 Employees

[Bảng lưu trữ thông tin về Employees]
[Table storing information about Employees]

[Provide the detailed table fields description using below table format

* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field       | PK | FK | UN | NN | Description                                                                                                                                             |
| -- | ----------- | -- | -- | -- | -- | ------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 01 | employee_id | x  |    |    | x  | - Unique identifier for the Employees record- Định danh duy nhất (ID) cho bản ghi Employees- Kiểu dữ liệu: **BIGINT**                      |
| 02 | account_id  |    | x  | x  |    | - Foreign key reference to the related account record- Khóa ngoại tham chiếu đến dữ liệu account liên quan- Kiểu dữ liệu: **BIGINT**   |
| 03 | full_name   |    |    |    | x  | - The specific name or title associated with the Employees- Tên gọi hoặc tiêu đề cụ thể của Employees- Kiểu dữ liệu: **VARCHAR(255)** |
| 04 | gender      |    |    |    | x  | - Categorization or classification type (gender)- Loại, danh mục hoặc phân loại của dữ liệu (gender)- Kiểu dữ liệu: **VARCHAR(255)**   |
| 05 | cccd        |    |    | x  | x  | - Detailed information about the cccd- Dữ liệu chi tiết về cccd của hệ thống- Kiểu dữ liệu: **VARCHAR(255)**                            |
| 06 | phone       |    |    |    | x  | - User contact information (phone)- Thông tin liên lạc cá nhân (phone)- Kiểu dữ liệu: **VARCHAR(255)**                                    |
| 07 | email       |    |    | x  | x  | - User contact information (email)- Thông tin liên lạc cá nhân (email)- Kiểu dữ liệu: **VARCHAR(255)**                                    |
| 08 | salary      |    |    |    | x  | - Financial value representing the salary- Giá trị tiền tệ thể hiện salary- Kiểu dữ liệu: **DECIMAL(10,2)**                              |

### 1.3.14 Export_History

[Bảng lưu trữ thông tin về Export History]
[Table storing information about Export History]

[Provide the detailed table fields description using below table format

* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field       | PK | FK | UN | NN | Description                                                                                                                                                       |
| -- | ----------- | -- | -- | -- | -- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 01 | id          | x  |    |    | x  | - Unique identifier for the Export History record- Định danh duy nhất (ID) cho bản ghi Export History- Kiểu dữ liệu: **BIGINT**                      |
| 02 | report_name |    |    |    |    | - The specific name or title associated with the Export History- Tên gọi hoặc tiêu đề cụ thể của Export History- Kiểu dữ liệu: **VARCHAR(255)** |
| 03 | format      |    |    |    |    | - Detailed information about the format- Dữ liệu chi tiết về format của hệ thống- Kiểu dữ liệu: **VARCHAR(255)**                                  |
| 04 | exported_at |    |    |    |    | - Detailed information about the exported at- Dữ liệu chi tiết về exported at của hệ thống- Kiểu dữ liệu: **DATETIME**                            |
| 05 | exported_by |    |    |    |    | - Detailed information about the exported by- Dữ liệu chi tiết về exported by của hệ thống- Kiểu dữ liệu: **VARCHAR(255)**                        |
| 06 | file_size   |    |    |    |    | - Numeric value indicating the file size- Giá trị số lượng thể hiện file size- Kiểu dữ liệu: **VARCHAR(255)**                                     |

### 1.3.15 Fnb_Daily_Reports

[Bảng lưu trữ thông tin về Fnb Daily Reports]
[Table storing information about Fnb Daily Reports]

[Provide the detailed table fields description using below table format

* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field                        | PK | FK | UN | NN | Description                                                                                                                                                                   |
| -- | ---------------------------- | -- | -- | -- | -- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 01 | report_id                    | x  |    |    | x  | - Unique identifier for the Fnb Daily Reports record- Định danh duy nhất (ID) cho bản ghi Fnb Daily Reports- Kiểu dữ liệu: **BIGINT**                            |
| 02 | report_date                  |    |    | x  | x  | - The specific report date for the event or record- Mốc thời gian hoặc ngày tháng cụ thể của report date- Kiểu dữ liệu: **DATE**                             |
| 03 | closed_by_staff_id           |    | x  |    | x  | - Foreign key reference to the related closed by staff record- Khóa ngoại tham chiếu đến dữ liệu closed by staff liên quan- Kiểu dữ liệu: **BIGINT**         |
| 04 | total_dine_in_orders         |    |    |    | x  | - Detailed information about the total dine in orders- Dữ liệu chi tiết về total dine in orders của hệ thống- Kiểu dữ liệu: **INT**                           |
| 05 | total_room_service_orders    |    |    |    | x  | - Detailed information about the total room service orders- Dữ liệu chi tiết về total room service orders của hệ thống- Kiểu dữ liệu: **INT**                 |
| 06 | total_cash_revenue           |    |    |    | x  | - Detailed information about the total cash revenue- Dữ liệu chi tiết về total cash revenue của hệ thống- Kiểu dữ liệu: **DECIMAL(10,2)**                     |
| 07 | total_vnpay_revenue          |    |    |    | x  | - Detailed information about the total vnpay revenue- Dữ liệu chi tiết về total vnpay revenue của hệ thống- Kiểu dữ liệu: **DECIMAL(10,2)**                   |
| 08 | total_charge_to_room_revenue |    |    |    | x  | - Detailed information about the total charge to room revenue- Dữ liệu chi tiết về total charge to room revenue của hệ thống- Kiểu dữ liệu: **DECIMAL(10,2)** |
| 09 | total_revenue                |    |    |    | x  | - Detailed information about the total revenue- Dữ liệu chi tiết về total revenue của hệ thống- Kiểu dữ liệu: **DECIMAL(10,2)**                               |
| 10 | closed_at                    |    |    |    | x  | - Detailed information about the closed at- Dữ liệu chi tiết về closed at của hệ thống- Kiểu dữ liệu: **DATETIME**                                            |
| 11 | notes                        |    |    |    |    | - Additional text description or notes for the record- Mô tả chi tiết bằng văn bản hoặc các ghi chú bổ sung- Kiểu dữ liệu: **TEXT**                        |

### 1.3.16 Folio_Items

[Bảng lưu trữ thông tin về Folio Items]
[Table storing information about Folio Items]

[Provide the detailed table fields description using below table format

* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field                  | PK | FK | UN | NN | Description                                                                                                                                                                   |
| -- | ---------------------- | -- | -- | -- | -- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 01 | folio_item_id          | x  |    |    | x  | - Unique identifier for the Folio Items record- Định danh duy nhất (ID) cho bản ghi Folio Items- Kiểu dữ liệu: **BIGINT**                                        |
| 02 | booking_id             |    | x  |    |    | - Foreign key reference to the related booking record- Khóa ngoại tham chiếu đến dữ liệu booking liên quan- Kiểu dữ liệu: **BIGINT**                         |
| 03 | room_booking_detail_id |    | x  |    |    | - Foreign key reference to the related room booking detail record- Khóa ngoại tham chiếu đến dữ liệu room booking detail liên quan- Kiểu dữ liệu: **BIGINT** |
| 04 | payer_customer_id      |    | x  |    |    | - Foreign key reference to the related payer customer record- Khóa ngoại tham chiếu đến dữ liệu payer customer liên quan- Kiểu dữ liệu: **BIGINT**           |
| 05 | source_department      |    |    |    |    | - Detailed information about the source department- Dữ liệu chi tiết về source department của hệ thống- Kiểu dữ liệu: **VARCHAR(255)**                        |
| 06 | amount                 |    |    |    |    | - Financial value representing the amount- Giá trị tiền tệ thể hiện amount- Kiểu dữ liệu: **DECIMAL(10,2)**                                                    |
| 07 | description            |    |    |    |    | - Additional text description or notes for the record- Mô tả chi tiết bằng văn bản hoặc các ghi chú bổ sung- Kiểu dữ liệu: **VARCHAR(255)**                |
| 08 | is_settled_separately  |    |    |    |    | - Boolean flag indicating whether the record is settled separately- Cờ trạng thái (đúng/sai) để xác định is settled separately- Kiểu dữ liệu: **BIT**      |
| 09 | created_by_staff_id    |    | x  |    |    | - Foreign key reference to the related created by staff record- Khóa ngoại tham chiếu đến dữ liệu created by staff liên quan- Kiểu dữ liệu: **BIGINT**       |
| 10 | created_at             |    |    |    |    | - Timestamp indicating when the record was initially created- Thời điểm bản ghi được tạo ra lần đầu trên hệ thống- Kiểu dữ liệu: **DATETIME**          |
| 11 | signature_img_url      |    |    |    |    | - Direct URL or system data for the signature img url file- Dữ liệu hoặc đường dẫn (URL) tới signature img url- Kiểu dữ liệu: **VARCHAR(500)**               |
| 12 | revenue_code           |    |    |    |    | - Secure or encrypted data representing revenue code- Dữ liệu bảo mật hoặc mã hóa đại diện cho revenue code- Kiểu dữ liệu: **VARCHAR(255)**                |

### 1.3.17 Food_Orders

[Bảng lưu trữ thông tin về Food Orders]
[Table storing information about Food Orders]

[Provide the detailed table fields description using below table format

* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field                   | PK | FK | UN | NN | Description                                                                                                                                                                     |
| -- | ----------------------- | -- | -- | -- | -- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 01 | order_id                | x  |    |    | x  | - Unique identifier for the Food Orders record- Định danh duy nhất (ID) cho bản ghi Food Orders- Kiểu dữ liệu: **BIGINT**                                          |
| 02 | booking_id              |    | x  |    |    | - Foreign key reference to the related booking record- Khóa ngoại tham chiếu đến dữ liệu booking liên quan- Kiểu dữ liệu: **BIGINT**                           |
| 03 | room_booking_detail_id  |    | x  |    |    | - Foreign key reference to the related room booking detail record- Khóa ngoại tham chiếu đến dữ liệu room booking detail liên quan- Kiểu dữ liệu: **BIGINT**   |
| 04 | table_id                |    | x  |    |    | - Foreign key reference to the related table record- Khóa ngoại tham chiếu đến dữ liệu table liên quan- Kiểu dữ liệu: **BIGINT**                               |
| 05 | order_type              |    |    |    | x  | - Categorization or classification type (order type)- Loại, danh mục hoặc phân loại của dữ liệu (order type)- Kiểu dữ liệu: **VARCHAR(255)**                   |
| 06 | order_status            |    |    |    | x  | - The current operational status or state of the record- Trạng thái hoạt động hoặc tình trạng hiện tại của bản ghi- Kiểu dữ liệu: **VARCHAR(255)**         |
| 07 | payment_type            |    |    |    | x  | - Categorization or classification type (payment type)- Loại, danh mục hoặc phân loại của dữ liệu (payment type)- Kiểu dữ liệu: **VARCHAR(255)**               |
| 08 | is_paid_in_pos          |    |    |    | x  | - Boolean flag indicating whether the record is paid in pos- Cờ trạng thái (đúng/sai) để xác định is paid in pos- Kiểu dữ liệu: **BIT**                      |
| 09 | created_by_staff_id     |    | x  |    | x  | - Foreign key reference to the related created by staff record- Khóa ngoại tham chiếu đến dữ liệu created by staff liên quan- Kiểu dữ liệu: **BIGINT**         |
| 10 | kitchen_processed_by_id |    | x  |    |    | - Foreign key reference to the related kitchen processed by record- Khóa ngoại tham chiếu đến dữ liệu kitchen processed by liên quan- Kiểu dữ liệu: **BIGINT** |
| 11 | note                    |    |    |    |    | - Additional text description or notes for the record- Mô tả chi tiết bằng văn bản hoặc các ghi chú bổ sung- Kiểu dữ liệu: **VARCHAR(500)**                  |

### 1.3.18 Food_Order_Details

[Bảng lưu trữ thông tin về Food Order Details]
[Table storing information about Food Order Details]

[Provide the detailed table fields description using below table format

* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field          | PK | FK | UN | NN | Description                                                                                                                                                             |
| -- | -------------- | -- | -- | -- | -- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 01 | detail_id      | x  |    |    | x  | - Unique identifier for the Food Order Details record- Định danh duy nhất (ID) cho bản ghi Food Order Details- Kiểu dữ liệu: **BIGINT**                    |
| 02 | order_id       |    | x  |    |    | - Foreign key reference to the related order record- Khóa ngoại tham chiếu đến dữ liệu order liên quan- Kiểu dữ liệu: **BIGINT**                       |
| 03 | menu_item_id   |    | x  |    |    | - Foreign key reference to the related menu item record- Khóa ngoại tham chiếu đến dữ liệu menu item liên quan- Kiểu dữ liệu: **BIGINT**               |
| 04 | quantity       |    |    |    |    | - Numeric value indicating the quantity- Giá trị số lượng thể hiện quantity- Kiểu dữ liệu: **INT**                                                      |
| 05 | price_at_order |    |    |    |    | - Financial value representing the price at order- Giá trị tiền tệ thể hiện price at order- Kiểu dữ liệu: **DECIMAL(10,2)**                              |
| 06 | kot_status     |    |    |    |    | - The current operational status or state of the record- Trạng thái hoạt động hoặc tình trạng hiện tại của bản ghi- Kiểu dữ liệu: **VARCHAR(255)** |
| 07 | note           |    |    |    |    | - Additional text description or notes for the record- Mô tả chi tiết bằng văn bản hoặc các ghi chú bổ sung- Kiểu dữ liệu: **VARCHAR(255)**          |

### 1.3.19 Hotel_Operations

[Bảng lưu trữ thông tin về Hotel Operations]
[Table storing information about Hotel Operations]

[Provide the detailed table fields description using below table format

* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field            | PK | FK | UN | NN | Description                                                                                                                                                               |
| -- | ---------------- | -- | -- | -- | -- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 01 | task_id          | x  |    |    | x  | - Unique identifier for the Hotel Operations record- Định danh duy nhất (ID) cho bản ghi Hotel Operations- Kiểu dữ liệu: **BIGINT**                          |
| 02 | room_id          |    | x  |    |    | - Foreign key reference to the related room record- Khóa ngoại tham chiếu đến dữ liệu room liên quan- Kiểu dữ liệu: **BIGINT**                           |
| 03 | staff_id         |    | x  |    |    | - Foreign key reference to the related staff record- Khóa ngoại tham chiếu đến dữ liệu staff liên quan- Kiểu dữ liệu: **BIGINT**                         |
| 04 | supervisor_id    |    | x  |    |    | - Foreign key reference to the related supervisor record- Khóa ngoại tham chiếu đến dữ liệu supervisor liên quan- Kiểu dữ liệu: **BIGINT**               |
| 05 | operational_type |    |    |    | x  | - Categorization or classification type (operational type)- Loại, danh mục hoặc phân loại của dữ liệu (operational type)- Kiểu dữ liệu: **VARCHAR(255)** |
| 06 | priority         |    |    |    | x  | - Detailed information about the priority- Dữ liệu chi tiết về priority của hệ thống- Kiểu dữ liệu: **VARCHAR(255)**                                      |
| 07 | status           |    |    |    | x  | - The current operational status or state of the record- Trạng thái hoạt động hoặc tình trạng hiện tại của bản ghi- Kiểu dữ liệu: **VARCHAR(255)**   |
| 08 | created_at       |    |    |    | x  | - Timestamp indicating when the record was initially created- Thời điểm bản ghi được tạo ra lần đầu trên hệ thống- Kiểu dữ liệu: **DATETIME**      |
| 09 | started_at       |    |    |    |    | - Detailed information about the started at- Dữ liệu chi tiết về started at của hệ thống- Kiểu dữ liệu: **DATETIME**                                      |
| 10 | completed_at     |    |    |    |    | - Detailed information about the completed at- Dữ liệu chi tiết về completed at của hệ thống- Kiểu dữ liệu: **DATETIME**                                  |
| 11 | notes            |    |    |    |    | - Additional text description or notes for the record- Mô tả chi tiết bằng văn bản hoặc các ghi chú bổ sung- Kiểu dữ liệu: **TEXT**                    |
| 12 | image_url        |    |    |    |    | - Direct URL or system data for the image url file- Dữ liệu hoặc đường dẫn (URL) tới image url- Kiểu dữ liệu: **VARCHAR(255)**                           |
| 13 | damage_price     |    |    |    |    | - Financial value representing the damage price- Giá trị tiền tệ thể hiện damage price- Kiểu dữ liệu: **DECIMAL(10,2)**                                    |
| 14 | is_escalated     |    |    |    |    | - Boolean flag indicating whether the record is escalated- Cờ trạng thái (đúng/sai) để xác định is escalated- Kiểu dữ liệu: **BIT**                    |

### 1.3.20 Hotel_Services

[Bảng lưu trữ thông tin về Hotel Services]
[Table storing information about Hotel Services]

[Provide the detailed table fields description using below table format

* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field             | PK | FK | UN | NN | Description                                                                                                                                                       |
| -- | ----------------- | -- | -- | -- | -- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 01 | service_id        | x  |    |    | x  | - Unique identifier for the Hotel Services record- Định danh duy nhất (ID) cho bản ghi Hotel Services- Kiểu dữ liệu: **BIGINT**                      |
| 02 | service_name      |    |    | x  | x  | - The specific name or title associated with the Hotel Services- Tên gọi hoặc tiêu đề cụ thể của Hotel Services- Kiểu dữ liệu: **VARCHAR(150)** |
| 03 | base_price        |    |    |    | x  | - Financial value representing the base price- Giá trị tiền tệ thể hiện base price- Kiểu dữ liệu: **DECIMAL(10,2)**                                |
| 04 | source_department |    |    |    | x  | - Detailed information about the source department- Dữ liệu chi tiết về source department của hệ thống- Kiểu dữ liệu: **VARCHAR(50)**             |
| 05 | is_available      |    |    |    | x  | - Boolean flag indicating whether the record is available- Cờ trạng thái (đúng/sai) để xác định is available- Kiểu dữ liệu: **BIT**            |
| 06 | description       |    |    |    |    | - Additional text description or notes for the record- Mô tả chi tiết bằng văn bản hoặc các ghi chú bổ sung- Kiểu dữ liệu: **TEXT**            |

### 1.3.21 membership_tiers

[Bảng lưu trữ thông tin về membership tiers]
[Table storing information about membership tiers]

[Provide the detailed table fields description using below table format

* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field        | PK | FK | UN | NN | Description                                                                                                                                                           |
| -- | ------------ | -- | -- | -- | -- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 01 | tier_id      | x  |    |    | x  | - Unique identifier for the membership tiers record- Định danh duy nhất (ID) cho bản ghi membership tiers- Kiểu dữ liệu: **BIGINT**                      |
| 02 | tier_name    |    |    | x  | x  | - The specific name or title associated with the membership tiers- Tên gọi hoặc tiêu đề cụ thể của membership tiers- Kiểu dữ liệu: **VARCHAR(255)** |
| 03 | points_from  |    |    |    | x  | - Detailed information about the points from- Dữ liệu chi tiết về points from của hệ thống- Kiểu dữ liệu: **INT**                                     |
| 04 | points_to    |    |    |    | x  | - Detailed information about the points to- Dữ liệu chi tiết về points to của hệ thống- Kiểu dữ liệu: **INT**                                         |
| 05 | credit_limit |    |    |    | x  | - Numeric value indicating the credit limit- Giá trị số lượng thể hiện credit limit- Kiểu dữ liệu: **DECIMAL(10,2)**                                  |
| 06 | description  |    |    |    |    | - Additional text description or notes for the record- Mô tả chi tiết bằng văn bản hoặc các ghi chú bổ sung- Kiểu dữ liệu: **VARCHAR(255)**        |

### 1.3.22 Menu_Items

[Bảng lưu trữ thông tin về Menu Items]
[Table storing information about Menu Items]

[Provide the detailed table fields description using below table format

* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field               | PK | FK | UN | NN | Description                                                                                                                                                          |
| -- | ------------------- | -- | -- | -- | -- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 01 | item_id             | x  |    |    | x  | - Unique identifier for the Menu Items record- Định danh duy nhất (ID) cho bản ghi Menu Items- Kiểu dữ liệu: **BIGINT**                                 |
| 02 | item_name           |    |    |    |    | - The specific name or title associated with the Menu Items- Tên gọi hoặc tiêu đề cụ thể của Menu Items- Kiểu dữ liệu: **VARCHAR(255)**            |
| 03 | price               |    |    |    |    | - Financial value representing the price- Giá trị tiền tệ thể hiện price- Kiểu dữ liệu: **DECIMAL(10,2)**                                             |
| 04 | category            |    |    |    |    | - Categorization or classification type (category)- Loại, danh mục hoặc phân loại của dữ liệu (category)- Kiểu dữ liệu: **VARCHAR(255)**            |
| 05 | is_available        |    |    |    |    | - Boolean flag indicating whether the record is available- Cờ trạng thái (đúng/sai) để xác định is available- Kiểu dữ liệu: **BIT**               |
| 06 | description         |    |    |    |    | - Additional text description or notes for the record- Mô tả chi tiết bằng văn bản hoặc các ghi chú bổ sung- Kiểu dữ liệu: **TEXT**               |
| 07 | image_url           |    |    |    |    | - Direct URL or system data for the image url file- Dữ liệu hoặc đường dẫn (URL) tới image url- Kiểu dữ liệu: **VARCHAR(255)**                      |
| 08 | allergy_tags        |    |    |    |    | - Detailed information about the allergy tags- Dữ liệu chi tiết về allergy tags của hệ thống- Kiểu dữ liệu: **VARCHAR(255)**                         |
| 09 | is_always_available |    |    |    |    | - Boolean flag indicating whether the record is always available- Cờ trạng thái (đúng/sai) để xác định is always available- Kiểu dữ liệu: **BIT** |

### 1.3.23 Payment_Transactions

[Bảng lưu trữ thông tin về Payment Transactions]
[Table storing information about Payment Transactions]

[Provide the detailed table fields description using below table format

* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field              | PK | FK | UN | NN | Description                                                                                                                                                               |
| -- | ------------------ | -- | -- | -- | -- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 01 | id                 | x  |    |    | x  | - Unique identifier for the Payment Transactions record- Định danh duy nhất (ID) cho bản ghi Payment Transactions- Kiểu dữ liệu: **BIGINT**                  |
| 02 | booking_id         |    | x  |    |    | - Foreign key reference to the related booking record- Khóa ngoại tham chiếu đến dữ liệu booking liên quan- Kiểu dữ liệu: **BIGINT**                     |
| 03 | food_order_id      |    | x  |    |    | - Foreign key reference to the related food order record- Khóa ngoại tham chiếu đến dữ liệu food order liên quan- Kiểu dữ liệu: **BIGINT**               |
| 04 | invoice_id         |    | x  |    |    | - Foreign key reference to the related invoice record- Khóa ngoại tham chiếu đến dữ liệu invoice liên quan- Kiểu dữ liệu: **BIGINT**                     |
| 05 | amount             |    |    |    |    | - Financial value representing the amount- Giá trị tiền tệ thể hiện amount- Kiểu dữ liệu: **DECIMAL(10,2)**                                                |
| 06 | status             |    |    |    | x  | - The current operational status or state of the record- Trạng thái hoạt động hoặc tình trạng hiện tại của bản ghi- Kiểu dữ liệu: **INT**            |
| 07 | transaction_type   |    |    |    |    | - Categorization or classification type (transaction type)- Loại, danh mục hoặc phân loại của dữ liệu (transaction type)- Kiểu dữ liệu: **VARCHAR(255)** |
| 08 | payment_method     |    |    |    |    | - Detailed information about the payment method- Dữ liệu chi tiết về payment method của hệ thống- Kiểu dữ liệu: **VARCHAR(255)**                          |
| 09 | gateway_status     |    |    |    |    | - The current operational status or state of the record- Trạng thái hoạt động hoặc tình trạng hiện tại của bản ghi- Kiểu dữ liệu: **VARCHAR(255)**   |
| 10 | transaction_ref    |    |    |    |    | - Detailed information about the transaction ref- Dữ liệu chi tiết về transaction ref của hệ thống- Kiểu dữ liệu: **VARCHAR(255)**                        |
| 11 | vnp_transaction_no |    |    |    |    | - Detailed information about the vnp transaction no- Dữ liệu chi tiết về vnp transaction no của hệ thống- Kiểu dữ liệu: **VARCHAR(255)**                  |
| 12 | response_code      |    |    |    |    | - Secure or encrypted data representing response code- Dữ liệu bảo mật hoặc mã hóa đại diện cho response code- Kiểu dữ liệu: **VARCHAR(255)**          |
| 13 | created_at         |    |    |    | x  | - Timestamp indicating when the record was initially created- Thời điểm bản ghi được tạo ra lần đầu trên hệ thống- Kiểu dữ liệu: **DATETIME**      |
| 14 | paid_at            |    |    |    |    | - Detailed information about the paid at- Dữ liệu chi tiết về paid at của hệ thống- Kiểu dữ liệu: **DATETIME**                                            |

### 1.3.24 Promotions

[Bảng lưu trữ thông tin về Promotions]
[Table storing information about Promotions]

[Provide the detailed table fields description using below table format

* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field                          | PK | FK | UN | NN | Description                                                                                                                                                             |
| -- | ------------------------------ | -- | -- | -- | -- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 01 | promo_id                       | x  |    |    | x  | - Unique identifier for the Promotions record- Định danh duy nhất (ID) cho bản ghi Promotions- Kiểu dữ liệu: **BIGINT**                                    |
| 02 | promo_code                     |    |    | x  | x  | - Secure or encrypted data representing promo code- Dữ liệu bảo mật hoặc mã hóa đại diện cho promo code- Kiểu dữ liệu: **VARCHAR(255)**              |
| 03 | discount_type                  |    |    |    | x  | - Numeric value indicating the discount type- Giá trị số lượng thể hiện discount type- Kiểu dữ liệu: **VARCHAR(255)**                                   |
| 04 | discount_value                 |    |    |    | x  | - Numeric value indicating the discount value- Giá trị số lượng thể hiện discount value- Kiểu dữ liệu: **DECIMAL(10,2)**                                |
| 05 | valid_from                     |    |    |    | x  | - Detailed information about the valid from- Dữ liệu chi tiết về valid from của hệ thống- Kiểu dữ liệu: **DATETIME**                                    |
| 06 | valid_to                       |    |    |    | x  | - Detailed information about the valid to- Dữ liệu chi tiết về valid to của hệ thống- Kiểu dữ liệu: **DATE**                                            |
| 07 | max_uses                       |    |    |    | x  | - Detailed information about the max uses- Dữ liệu chi tiết về max uses của hệ thống- Kiểu dữ liệu: **INT**                                             |
| 08 | current_uses                   |    |    |    | x  | - Detailed information about the current uses- Dữ liệu chi tiết về current uses của hệ thống- Kiểu dữ liệu: **INT**                                     |
| 09 | is_active                      |    |    |    | x  | - Boolean flag indicating whether the record is active- Cờ trạng thái (đúng/sai) để xác định is active- Kiểu dữ liệu: **BIT**                        |
| 10 | description                    |    |    |    |    | - Additional text description or notes for the record- Mô tả chi tiết bằng văn bản hoặc các ghi chú bổ sung- Kiểu dữ liệu: **VARCHAR(1000)**         |
| 11 | combo_config                   |    |    |    |    | - Detailed information about the combo config- Dữ liệu chi tiết về combo config của hệ thống- Kiểu dữ liệu: **TEXT**                                    |
| 12 | max_discount_value_vnd         |    |    |    |    | - Numeric value indicating the max discount value vnd- Giá trị số lượng thể hiện max discount value vnd- Kiểu dữ liệu: **DECIMAL(10,2)**                |
| 13 | max_uses_per_customer          |    |    |    |    | - Detailed information about the max uses per customer- Dữ liệu chi tiết về max uses per customer của hệ thống- Kiểu dữ liệu: **INT**                   |
| 14 | manager_approval_threshold_pct |    |    |    |    | - Detailed information about the manager approval threshold pct- Dữ liệu chi tiết về manager approval threshold pct của hệ thống- Kiểu dữ liệu: **INT** |

### 1.3.25 Refund_Requests

[Bảng lưu trữ thông tin về Refund Requests]
[Table storing information about Refund Requests]

[Provide the detailed table fields description using below table format

* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field              | PK | FK | UN | NN | Description                                                                                                                                                             |
| -- | ------------------ | -- | -- | -- | -- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 01 | id                 | x  |    |    | x  | - Unique identifier for the Refund Requests record- Định danh duy nhất (ID) cho bản ghi Refund Requests- Kiểu dữ liệu: **BIGINT**                          |
| 02 | order_id           |    | x  |    |    | - Foreign key reference to the related order record- Khóa ngoại tham chiếu đến dữ liệu order liên quan- Kiểu dữ liệu: **BIGINT**                       |
| 03 | room_booking_id    |    | x  |    |    | - Foreign key reference to the related room booking record- Khóa ngoại tham chiếu đến dữ liệu room booking liên quan- Kiểu dữ liệu: **BIGINT**         |
| 04 | tour_booking_id    |    | x  |    |    | - Foreign key reference to the related tour booking record- Khóa ngoại tham chiếu đến dữ liệu tour booking liên quan- Kiểu dữ liệu: **BIGINT**         |
| 05 | bank_name          |    |    |    |    | - The specific name or title associated with the Refund Requests- Tên gọi hoặc tiêu đề cụ thể của Refund Requests- Kiểu dữ liệu: **VARCHAR(255)**     |
| 06 | account_number     |    |    |    |    | - Numeric value indicating the account number- Giá trị số lượng thể hiện account number- Kiểu dữ liệu: **VARCHAR(255)**                                 |
| 07 | account_name       |    |    |    |    | - The specific name or title associated with the Refund Requests- Tên gọi hoặc tiêu đề cụ thể của Refund Requests- Kiểu dữ liệu: **VARCHAR(255)**     |
| 08 | phone_number       |    |    |    |    | - User contact information (phone number)- Thông tin liên lạc cá nhân (phone number)- Kiểu dữ liệu: **VARCHAR(255)**                                      |
| 09 | amount             |    |    |    | x  | - Financial value representing the amount- Giá trị tiền tệ thể hiện amount- Kiểu dữ liệu: **DECIMAL(10,2)**                                              |
| 10 | status             |    |    |    | x  | - The current operational status or state of the record- Trạng thái hoạt động hoặc tình trạng hiện tại của bản ghi- Kiểu dữ liệu: **VARCHAR(255)** |
| 11 | manager_note       |    |    |    |    | - Detailed information about the manager note- Dữ liệu chi tiết về manager note của hệ thống- Kiểu dữ liệu: **VARCHAR(500)**                            |
| 12 | evidence_image_url |    |    |    |    | - Direct URL or system data for the evidence image url file- Dữ liệu hoặc đường dẫn (URL) tới evidence image url- Kiểu dữ liệu: **VARCHAR(1000)**      |
| 13 | created_at         |    |    |    | x  | - Timestamp indicating when the record was initially created- Thời điểm bản ghi được tạo ra lần đầu trên hệ thống- Kiểu dữ liệu: **DATETIME**    |
| 14 | completed_at       |    |    |    |    | - Detailed information about the completed at- Dữ liệu chi tiết về completed at của hệ thống- Kiểu dữ liệu: **DATETIME**                                |

### 1.3.26 Restaurant_Tables

[Bảng lưu trữ thông tin về Restaurant Tables]
[Table storing information about Restaurant Tables]

[Provide the detailed table fields description using below table format

* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field        | PK | FK | UN | NN | Description                                                                                                                                                             |
| -- | ------------ | -- | -- | -- | -- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 01 | table_id     | x  |    |    | x  | - Unique identifier for the Restaurant Tables record- Định danh duy nhất (ID) cho bản ghi Restaurant Tables- Kiểu dữ liệu: **BIGINT**                      |
| 02 | table_number |    |    |    |    | - Detailed information about the table number- Dữ liệu chi tiết về table number của hệ thống- Kiểu dữ liệu: **VARCHAR(255)**                            |
| 03 | capacity     |    |    |    |    | - Numeric value indicating the capacity- Giá trị số lượng thể hiện capacity- Kiểu dữ liệu: **INT**                                                      |
| 04 | table_status |    |    |    |    | - The current operational status or state of the record- Trạng thái hoạt động hoặc tình trạng hiện tại của bản ghi- Kiểu dữ liệu: **VARCHAR(255)** |
| 05 | is_active    |    |    |    |    | - Boolean flag indicating whether the record is active- Cờ trạng thái (đúng/sai) để xác định is active- Kiểu dữ liệu: **BIT**                        |

### 1.3.27 Reviews

[Bảng lưu trữ thông tin về Reviews]
[Table storing information about Reviews]

[Provide the detailed table fields description using below table format

* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field                  | PK | FK | UN | NN | Description                                                                                                                                                                   |
| -- | ---------------------- | -- | -- | -- | -- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 01 | review_id              | x  |    |    | x  | - Unique identifier for the Reviews record- Định danh duy nhất (ID) cho bản ghi Reviews- Kiểu dữ liệu: **BIGINT**                                                |
| 02 | customer_id            |    | x  |    |    | - Foreign key reference to the related customer record- Khóa ngoại tham chiếu đến dữ liệu customer liên quan- Kiểu dữ liệu: **BIGINT**                       |
| 03 | room_booking_detail_id |    | x  |    |    | - Foreign key reference to the related room booking detail record- Khóa ngoại tham chiếu đến dữ liệu room booking detail liên quan- Kiểu dữ liệu: **BIGINT** |
| 04 | tour_booking_id        |    | x  |    |    | - Foreign key reference to the related tour booking record- Khóa ngoại tham chiếu đến dữ liệu tour booking liên quan- Kiểu dữ liệu: **BIGINT**               |
| 05 | rating_service         |    |    |    |    | - Detailed information about the rating service- Dữ liệu chi tiết về rating service của hệ thống- Kiểu dữ liệu: **INT**                                       |
| 06 | rating_tour            |    |    |    |    | - Detailed information about the rating tour- Dữ liệu chi tiết về rating tour của hệ thống- Kiểu dữ liệu: **INT**                                             |
| 07 | rating_room_dining     |    |    |    |    | - Detailed information about the rating room dining- Dữ liệu chi tiết về rating room dining của hệ thống- Kiểu dữ liệu: **INT**                               |
| 08 | review_text            |    |    |    |    | - Detailed information about the review text- Dữ liệu chi tiết về review text của hệ thống- Kiểu dữ liệu: **TEXT**                                            |
| 09 | created_at             |    |    |    |    | - Timestamp indicating when the record was initially created- Thời điểm bản ghi được tạo ra lần đầu trên hệ thống- Kiểu dữ liệu: **DATETIME**          |
| 10 | moderation_status      |    |    |    |    | - The current operational status or state of the record- Trạng thái hoạt động hoặc tình trạng hiện tại của bản ghi- Kiểu dữ liệu: **VARCHAR(255)**       |
| 11 | moderated_by           |    | x  |    |    | - Foreign key reference to the related moderated by record- Khóa ngoại tham chiếu đến dữ liệu moderated by liên quan- Kiểu dữ liệu: **BIGINT**               |
| 12 | replied_by             |    | x  |    |    | - Foreign key reference to the related replied by record- Khóa ngoại tham chiếu đến dữ liệu replied by liên quan- Kiểu dữ liệu: **BIGINT**                   |
| 13 | moderation_reason      |    |    |    |    | - Detailed information about the moderation reason- Dữ liệu chi tiết về moderation reason của hệ thống- Kiểu dữ liệu: **TEXT**                                |
| 14 | reply_text             |    |    |    |    | - Detailed information about the reply text- Dữ liệu chi tiết về reply text của hệ thống- Kiểu dữ liệu: **TEXT**                                              |
| 15 | is_reported            |    |    |    |    | - Boolean flag indicating whether the record is reported- Cờ trạng thái (đúng/sai) để xác định is reported- Kiểu dữ liệu: **BIT**                          |
| 16 | report_reason          |    |    |    |    | - Detailed information about the report reason- Dữ liệu chi tiết về report reason của hệ thống- Kiểu dữ liệu: **TEXT**                                        |
| 17 | guide_name             |    |    |    |    | - The specific name or title associated with the Reviews- Tên gọi hoặc tiêu đề cụ thể của Reviews- Kiểu dữ liệu: **VARCHAR(255)**                           |
| 18 | tour_name_resolved     |    |    |    |    | - The specific name or title associated with the Reviews- Tên gọi hoặc tiêu đề cụ thể của Reviews- Kiểu dữ liệu: **VARCHAR(255)**                           |

### 1.3.28 Roles

[Bảng lưu trữ thông tin về Roles]
[Table storing information about Roles]

[Provide the detailed table fields description using below table format

* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field       | PK | FK | UN | NN | Description                                                                                                                                            |
| -- | ----------- | -- | -- | -- | -- | ------------------------------------------------------------------------------------------------------------------------------------------------------ |
| 01 | role_id     | x  |    |    | x  | - Unique identifier for the Roles record- Định danh duy nhất (ID) cho bản ghi Roles- Kiểu dữ liệu: **BIGINT**                             |
| 02 | role_name   |    |    | x  | x  | - The specific name or title associated with the Roles- Tên gọi hoặc tiêu đề cụ thể của Roles- Kiểu dữ liệu: **VARCHAR(255)**        |
| 03 | permissions |    |    |    |    | - Detailed information about the permissions- Dữ liệu chi tiết về permissions của hệ thống- Kiểu dữ liệu: **TEXT**                     |
| 04 | description |    |    |    |    | - Additional text description or notes for the record- Mô tả chi tiết bằng văn bản hoặc các ghi chú bổ sung- Kiểu dữ liệu: **TEXT** |

### 1.3.29 Rooms

[Bảng lưu trữ thông tin về Rooms]
[Table storing information about Rooms]

[Provide the detailed table fields description using below table format

* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field                     | PK | FK | UN | NN | Description                                                                                                                                                                          |
| -- | ------------------------- | -- | -- | -- | -- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| 01 | room_id                   | x  |    |    | x  | - Unique identifier for the Rooms record- Định danh duy nhất (ID) cho bản ghi Rooms- Kiểu dữ liệu: **BIGINT**                                                           |
| 02 | room_number               |    |    | x  | x  | - Detailed information about the room number- Dữ liệu chi tiết về room number của hệ thống- Kiểu dữ liệu: **VARCHAR(255)**                                           |
| 03 | category_id               |    | x  |    | x  | - Foreign key reference to the related category record- Khóa ngoại tham chiếu đến dữ liệu category liên quan- Kiểu dữ liệu: **BIGINT**                              |
| 04 | room_status               |    |    |    | x  | - The current operational status or state of the record- Trạng thái hoạt động hoặc tình trạng hiện tại của bản ghi- Kiểu dữ liệu: **VARCHAR(255)**              |
| 05 | current_booking_detail_id |    |    |    |    | - ID value referencing the current booking detail record (Logical relation)- Trường ID tham chiếu logic đến dữ liệu current booking detail- Kiểu dữ liệu: **BIGINT** |

### 1.3.30 Room_Bookings

[Bảng lưu trữ thông tin về Room Bookings]
[Table storing information about Room Bookings]

[Provide the detailed table fields description using below table format

* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field                 | PK | FK | UN | NN | Description                                                                                                                                                              |
| -- | --------------------- | -- | -- | -- | -- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| 01 | check_in_date         |    |    |    | x  | - The specific check in date for the event or record- Mốc thời gian hoặc ngày tháng cụ thể của check in date- Kiểu dữ liệu: **DATE**                    |
| 02 | check_out_date        |    |    |    | x  | - The specific check out date for the event or record- Mốc thời gian hoặc ngày tháng cụ thể của check out date- Kiểu dữ liệu: **DATE**                  |
| 03 | deposit_amount        |    |    |    | x  | - Financial value representing the deposit amount- Giá trị tiền tệ thể hiện deposit amount- Kiểu dữ liệu: **DECIMAL(10,2)**                               |
| 04 | cancellation_deadline |    |    |    | x  | - Detailed information about the cancellation deadline- Dữ liệu chi tiết về cancellation deadline của hệ thống- Kiểu dữ liệu: **DATETIME**               |
| 05 | credit_limit          |    |    |    | x  | - Numeric value indicating the credit limit- Giá trị số lượng thể hiện credit limit- Kiểu dữ liệu: **DECIMAL(10,2)**                                     |
| 06 | personal_pin_hash     |    |    |    | x  | - Secure or encrypted data representing personal pin hash- Dữ liệu bảo mật hoặc mã hóa đại diện cho personal pin hash- Kiểu dữ liệu: **VARCHAR(255)** |

### 1.3.31 Room_Booking_Details

[Bảng lưu trữ thông tin về Room Booking Details]
[Table storing information about Room Booking Details]

[Provide the detailed table fields description using below table format

* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field                     | PK | FK | UN | NN | Description                                                                                                                                                                      |
| -- | ------------------------- | -- | -- | -- | -- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 01 | detail_id                 | x  |    |    | x  | - Unique identifier for the Room Booking Details record- Định danh duy nhất (ID) cho bản ghi Room Booking Details- Kiểu dữ liệu: **BIGINT**                         |
| 02 | room_booking_id           |    | x  |    | x  | - Foreign key reference to the related room booking record- Khóa ngoại tham chiếu đến dữ liệu room booking liên quan- Kiểu dữ liệu: **BIGINT**                  |
| 03 | category_id               |    | x  |    | x  | - Foreign key reference to the related category record- Khóa ngoại tham chiếu đến dữ liệu category liên quan- Kiểu dữ liệu: **BIGINT**                          |
| 04 | room_id                   |    | x  |    |    | - Foreign key reference to the related room record- Khóa ngoại tham chiếu đến dữ liệu room liên quan- Kiểu dữ liệu: **BIGINT**                                  |
| 05 | room_charge               |    |    |    | x  | - Detailed information about the room charge- Dữ liệu chi tiết về room charge của hệ thống- Kiểu dữ liệu: **DECIMAL(10,2)**                                      |
| 06 | detail_status             |    |    |    | x  | - The current operational status or state of the record- Trạng thái hoạt động hoặc tình trạng hiện tại của bản ghi- Kiểu dữ liệu: **VARCHAR(255)**          |
| 07 | bed_preference            |    |    |    | x  | - Detailed information about the bed preference- Dữ liệu chi tiết về bed preference của hệ thống- Kiểu dữ liệu: **VARCHAR(255)**                                 |
| 08 | special_requests          |    |    |    |    | - Detailed information about the special requests- Dữ liệu chi tiết về special requests của hệ thống- Kiểu dữ liệu: **VARCHAR(500)**                             |
| 09 | is_charge_to_room_allowed |    |    |    | x  | - Boolean flag indicating whether the record is charge to room allowed- Cờ trạng thái (đúng/sai) để xác định is charge to room allowed- Kiểu dữ liệu: **BIT** |
| 10 | sub_credit_limit          |    |    |    | x  | - Numeric value indicating the sub credit limit- Giá trị số lượng thể hiện sub credit limit- Kiểu dữ liệu: **DECIMAL(10,2)**                                     |
| 11 | billing_routing_strategy  |    |    |    | x  | - Detailed information about the billing routing strategy- Dữ liệu chi tiết về billing routing strategy của hệ thống- Kiểu dữ liệu: **VARCHAR(255)**             |
| 12 | number_of_adults          |    |    |    |    | - Detailed information about the number of adults- Dữ liệu chi tiết về number of adults của hệ thống- Kiểu dữ liệu: **INT**                                      |
| 13 | number_of_children        |    |    |    |    | - Detailed information about the number of children- Dữ liệu chi tiết về number of children của hệ thống- Kiểu dữ liệu: **INT**                                  |
| 14 | extra_surcharge           |    |    |    |    | - Detailed information about the extra surcharge- Dữ liệu chi tiết về extra surcharge của hệ thống- Kiểu dữ liệu: **DECIMAL(10,2)**                              |
| 15 | customer_id               |    | x  |    |    | - Foreign key reference to the related customer record- Khóa ngoại tham chiếu đến dữ liệu customer liên quan- Kiểu dữ liệu: **BIGINT**                          |

### 1.3.32 Room_Categories

[Bảng lưu trữ thông tin về Room Categories]
[Table storing information about Room Categories]

[Provide the detailed table fields description using below table format

* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field                  | PK | FK | UN | NN | Description                                                                                                                                                         |
| -- | ---------------------- | -- | -- | -- | -- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 01 | category_id            | x  |    |    | x  | - Unique identifier for the Room Categories record- Định danh duy nhất (ID) cho bản ghi Room Categories- Kiểu dữ liệu: **BIGINT**                      |
| 02 | category_name          |    |    | x  | x  | - The specific name or title associated with the Room Categories- Tên gọi hoặc tiêu đề cụ thể của Room Categories- Kiểu dữ liệu: **VARCHAR(255)** |
| 03 | base_price             |    |    |    | x  | - Financial value representing the base price- Giá trị tiền tệ thể hiện base price- Kiểu dữ liệu: **DECIMAL(10,2)**                                  |
| 04 | capacity               |    |    |    | x  | - Numeric value indicating the capacity- Giá trị số lượng thể hiện capacity- Kiểu dữ liệu: **INT**                                                  |
| 05 | description            |    |    |    |    | - Additional text description or notes for the record- Mô tả chi tiết bằng văn bản hoặc các ghi chú bổ sung- Kiểu dữ liệu: **TEXT**              |
| 06 | base_adults            |    |    |    | x  | - Detailed information about the base adults- Dữ liệu chi tiết về base adults của hệ thống- Kiểu dữ liệu: **INT**                                   |
| 07 | base_children          |    |    |    | x  | - Detailed information about the base children- Dữ liệu chi tiết về base children của hệ thống- Kiểu dữ liệu: **INT**                               |
| 08 | max_adults             |    |    |    | x  | - Detailed information about the max adults- Dữ liệu chi tiết về max adults của hệ thống- Kiểu dữ liệu: **INT**                                     |
| 09 | max_children           |    |    |    | x  | - Detailed information about the max children- Dữ liệu chi tiết về max children của hệ thống- Kiểu dữ liệu: **INT**                                 |
| 10 | cover_img_url          |    |    |    |    | - Direct URL or system data for the cover img url file- Dữ liệu hoặc đường dẫn (URL) tới cover img url- Kiểu dữ liệu: **VARCHAR(500)**             |
| 11 | extra_adult_surcharge  |    |    |    |    | - Detailed information about the extra adult surcharge- Dữ liệu chi tiết về extra adult surcharge của hệ thống- Kiểu dữ liệu: **DECIMAL(10,2)**     |
| 12 | extra_child_surcharge  |    |    |    |    | - Detailed information about the extra child surcharge- Dữ liệu chi tiết về extra child surcharge của hệ thống- Kiểu dữ liệu: **DECIMAL(10,2)**     |
| 13 | is_active              |    |    |    | x  | - Boolean flag indicating whether the record is active- Cờ trạng thái (đúng/sai) để xác định is active- Kiểu dữ liệu: **BIT**                    |
| 14 | bed_type               |    |    |    |    | - Categorization or classification type (bed type)- Loại, danh mục hoặc phân loại của dữ liệu (bed type)- Kiểu dữ liệu: **VARCHAR(100)**           |
| 15 | room_size              |    |    |    |    | - Numeric value indicating the room size- Giá trị số lượng thể hiện room size- Kiểu dữ liệu: **INT**                                                |
| 16 | view_type              |    |    |    |    | - Categorization or classification type (view type)- Loại, danh mục hoặc phân loại của dữ liệu (view type)- Kiểu dữ liệu: **VARCHAR(100)**         |
| 17 | has_bathtub            |    |    |    |    | - Boolean flag indicating whether the record has bathtub- Cờ trạng thái (đúng/sai) để xác định has bathtub- Kiểu dữ liệu: **BIT**                |
| 18 | has_balcony            |    |    |    |    | - Boolean flag indicating whether the record has balcony- Cờ trạng thái (đúng/sai) để xác định has balcony- Kiểu dữ liệu: **BIT**                |
| 19 | complimentary_services |    |    |    |    | - Detailed information about the complimentary services- Dữ liệu chi tiết về complimentary services của hệ thống- Kiểu dữ liệu: **VARCHAR(255)**    |
| 20 | has_free_breakfast     |    |    |    |    | - Boolean flag indicating whether the record has free breakfast- Cờ trạng thái (đúng/sai) để xác định has free breakfast- Kiểu dữ liệu: **BIT**  |

### 1.3.33 Room_Guests

[Bảng lưu trữ thông tin về Room Guests]
[Table storing information about Room Guests]

[Provide the detailed table fields description using below table format

* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field              | PK | FK | UN | NN | Description                                                                                                                                                        |
| -- | ------------------ | -- | -- | -- | -- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| 01 | guest_id           | x  |    |    | x  | - Unique identifier for the Room Guests record- Định danh duy nhất (ID) cho bản ghi Room Guests- Kiểu dữ liệu: **BIGINT**                             |
| 02 | detail_id          |    | x  |    | x  | - Foreign key reference to the related detail record- Khóa ngoại tham chiếu đến dữ liệu detail liên quan- Kiểu dữ liệu: **BIGINT**                |
| 03 | customer_id        |    | x  |    |    | - Foreign key reference to the related customer record- Khóa ngoại tham chiếu đến dữ liệu customer liên quan- Kiểu dữ liệu: **BIGINT**            |
| 04 | dependent_id       |    | x  |    |    | - Foreign key reference to the related dependent record- Khóa ngoại tham chiếu đến dữ liệu dependent liên quan- Kiểu dữ liệu: **BIGINT**          |
| 05 | guest_type         |    |    |    | x  | - Categorization or classification type (guest type)- Loại, danh mục hoặc phân loại của dữ liệu (guest type)- Kiểu dữ liệu: **VARCHAR(255)**      |
| 06 | is_primary_contact |    |    |    | x  | - Boolean flag indicating whether the record is primary contact- Cờ trạng thái (đúng/sai) để xác định is primary contact- Kiểu dữ liệu: **BIT** |

### 1.3.34 Room_Surcharges

[Bảng lưu trữ thông tin về Room Surcharges]
[Table storing information about Room Surcharges]

[Provide the detailed table fields description using below table format

* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field          | PK | FK | UN | NN | Description                                                                                                                                                           |
| -- | -------------- | -- | -- | -- | -- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 01 | surcharge_id   | x  |    |    | x  | - Unique identifier for the Room Surcharges record- Định danh duy nhất (ID) cho bản ghi Room Surcharges- Kiểu dữ liệu: **BIGINT**                        |
| 02 | category_id    |    | x  |    | x  | - Foreign key reference to the related category record- Khóa ngoại tham chiếu đến dữ liệu category liên quan- Kiểu dữ liệu: **BIGINT**               |
| 03 | surcharge_type |    |    |    | x  | - Categorization or classification type (surcharge type)- Loại, danh mục hoặc phân loại của dữ liệu (surcharge type)- Kiểu dữ liệu: **VARCHAR(255)** |
| 04 | age_from       |    |    |    | x  | - Detailed information about the age from- Dữ liệu chi tiết về age from của hệ thống- Kiểu dữ liệu: **INT**                                           |
| 05 | age_to         |    |    |    | x  | - Detailed information about the age to- Dữ liệu chi tiết về age to của hệ thống- Kiểu dữ liệu: **INT**                                               |
| 06 | price_modifier |    |    |    | x  | - Financial value representing the price modifier- Giá trị tiền tệ thể hiện price modifier- Kiểu dữ liệu: **DECIMAL(10,2)**                            |
| 07 | is_active      |    |    |    | x  | - Boolean flag indicating whether the record is active- Cờ trạng thái (đúng/sai) để xác định is active- Kiểu dữ liệu: **BIT**                      |

### 1.3.35 Run_Itinerary_Status

[Bảng lưu trữ thông tin về Run Itinerary Status]
[Table storing information about Run Itinerary Status]

[Provide the detailed table fields description using below table format

* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field                | PK | FK | UN | NN | Description                                                                                                                                                             |
| -- | -------------------- | -- | -- | -- | -- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 01 | run_status_id        | x  |    |    | x  | - Unique identifier for the Run Itinerary Status record- Định danh duy nhất (ID) cho bản ghi Run Itinerary Status- Kiểu dữ liệu: **BIGINT**                |
| 02 | schedule_id          |    | x  |    | x  | - Foreign key reference to the related schedule record- Khóa ngoại tham chiếu đến dữ liệu schedule liên quan- Kiểu dữ liệu: **BIGINT**                 |
| 03 | detail_id            |    | x  |    | x  | - Foreign key reference to the related detail record- Khóa ngoại tham chiếu đến dữ liệu detail liên quan- Kiểu dữ liệu: **BIGINT**                     |
| 04 | actual_start_time    |    |    |    |    | - The specific actual start time for the event or record- Mốc thời gian hoặc ngày tháng cụ thể của actual start time- Kiểu dữ liệu: **DATETIME**       |
| 05 | actual_end_time      |    |    |    |    | - The specific actual end time for the event or record- Mốc thời gian hoặc ngày tháng cụ thể của actual end time- Kiểu dữ liệu: **DATETIME**           |
| 06 | current_stage_status |    |    |    | x  | - The current operational status or state of the record- Trạng thái hoạt động hoặc tình trạng hiện tại của bản ghi- Kiểu dữ liệu: **VARCHAR(255)** |
| 07 | guide_notes          |    |    |    |    | - Detailed information about the guide notes- Dữ liệu chi tiết về guide notes của hệ thống- Kiểu dữ liệu: **TEXT**                                      |

### 1.3.36 Shifts

[Bảng lưu trữ thông tin về Shifts]
[Table storing information about Shifts]

[Provide the detailed table fields description using below table format

* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field       | PK | FK | UN | NN | Description                                                                                                                                                    |
| -- | ----------- | -- | -- | -- | -- | -------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 01 | shift_id    | x  |    |    | x  | - Unique identifier for the Shifts record- Định danh duy nhất (ID) cho bản ghi Shifts- Kiểu dữ liệu: **BIGINT**                                   |
| 02 | shift_name  |    |    |    | x  | - The specific name or title associated with the Shifts- Tên gọi hoặc tiêu đề cụ thể của Shifts- Kiểu dữ liệu: **VARCHAR(255)**              |
| 03 | start_time  |    |    |    | x  | - The specific start time for the event or record- Mốc thời gian hoặc ngày tháng cụ thể của start time- Kiểu dữ liệu: **INT**                 |
| 04 | end_time    |    |    |    | x  | - The specific end time for the event or record- Mốc thời gian hoặc ngày tháng cụ thể của end time- Kiểu dữ liệu: **INT**                     |
| 05 | description |    |    |    |    | - Additional text description or notes for the record- Mô tả chi tiết bằng văn bản hoặc các ghi chú bổ sung- Kiểu dữ liệu: **VARCHAR(255)** |

### 1.3.37 Staff_Schedules

[Bảng lưu trữ thông tin về Staff Schedules]
[Table storing information about Staff Schedules]

[Provide the detailed table fields description using below table format

* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field       | PK | FK | UN | NN | Description                                                                                                                                                             |
| -- | ----------- | -- | -- | -- | -- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 01 | schedule_id | x  |    |    | x  | - Unique identifier for the Staff Schedules record- Định danh duy nhất (ID) cho bản ghi Staff Schedules- Kiểu dữ liệu: **BIGINT**                          |
| 02 | employee_id |    | x  |    | x  | - Foreign key reference to the related employee record- Khóa ngoại tham chiếu đến dữ liệu employee liên quan- Kiểu dữ liệu: **BIGINT**                 |
| 03 | shift_id    |    | x  |    | x  | - Foreign key reference to the related shift record- Khóa ngoại tham chiếu đến dữ liệu shift liên quan- Kiểu dữ liệu: **BIGINT**                       |
| 04 | work_date   |    |    |    | x  | - The specific work date for the event or record- Mốc thời gian hoặc ngày tháng cụ thể của work date- Kiểu dữ liệu: **DATE**                           |
| 05 | status      |    |    |    | x  | - The current operational status or state of the record- Trạng thái hoạt động hoặc tình trạng hiện tại của bản ghi- Kiểu dữ liệu: **VARCHAR(255)** |
| 06 | is_closed   |    |    |    | x  | - Boolean flag indicating whether the record is closed- Cờ trạng thái (đúng/sai) để xác định is closed- Kiểu dữ liệu: **BIT**                        |

### 1.3.38 SystemNotifications

[Bảng lưu trữ thông tin về SystemNotifications]
[Table storing information about SystemNotifications]

[Provide the detailed table fields description using below table format

* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field                | PK | FK | UN | NN | Description                                                                                                                                                                 |
| -- | -------------------- | -- | -- | -- | -- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 01 | id                   | x  |    |    | x  | - Unique identifier for the SystemNotifications record- Định danh duy nhất (ID) cho bản ghi SystemNotifications- Kiểu dữ liệu: **BIGINT**                      |
| 02 | recipient_account_id |    | x  |    | x  | - Foreign key reference to the related recipient account record- Khóa ngoại tham chiếu đến dữ liệu recipient account liên quan- Kiểu dữ liệu: **BIGINT**   |
| 03 | title                |    |    |    | x  | - The specific name or title associated with the SystemNotifications- Tên gọi hoặc tiêu đề cụ thể của SystemNotifications- Kiểu dữ liệu: **VARCHAR(255)** |
| 04 | message              |    |    |    | x  | - Detailed information about the message- Dữ liệu chi tiết về message của hệ thống- Kiểu dữ liệu: **VARCHAR(1000)**                                         |
| 05 | notification_type    |    |    |    |    | - Categorization or classification type (notification type)- Loại, danh mục hoặc phân loại của dữ liệu (notification type)- Kiểu dữ liệu: **VARCHAR(255)** |
| 06 | target_url           |    |    |    |    | - Direct URL or system data for the target url file- Dữ liệu hoặc đường dẫn (URL) tới target url- Kiểu dữ liệu: **VARCHAR(255)**                           |
| 07 | is_read              |    |    |    | x  | - Boolean flag indicating whether the record is read- Cờ trạng thái (đúng/sai) để xác định is read- Kiểu dữ liệu: **BIT**                                |
| 08 | created_at           |    |    |    | x  | - Timestamp indicating when the record was initially created- Thời điểm bản ghi được tạo ra lần đầu trên hệ thống- Kiểu dữ liệu: **DATETIME**        |

### 1.3.39 Table_Reservations

[Bảng lưu trữ thông tin về Table Reservations]
[Table storing information about Table Reservations]

[Provide the detailed table fields description using below table format

* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field            | PK | FK | UN | NN | Description                                                                                                                                                             |
| -- | ---------------- | -- | -- | -- | -- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 01 | reservation_id   | x  |    |    | x  | - Unique identifier for the Table Reservations record- Định danh duy nhất (ID) cho bản ghi Table Reservations- Kiểu dữ liệu: **BIGINT**                    |
| 02 | customer_id      |    | x  |    |    | - Foreign key reference to the related customer record- Khóa ngoại tham chiếu đến dữ liệu customer liên quan- Kiểu dữ liệu: **BIGINT**                 |
| 03 | table_id         |    | x  |    |    | - Foreign key reference to the related table record- Khóa ngoại tham chiếu đến dữ liệu table liên quan- Kiểu dữ liệu: **BIGINT**                       |
| 04 | reserve_date     |    |    |    |    | - The specific reserve date for the event or record- Mốc thời gian hoặc ngày tháng cụ thể của reserve date- Kiểu dữ liệu: **DATE**                     |
| 05 | reserve_time     |    |    |    |    | - The specific reserve time for the event or record- Mốc thời gian hoặc ngày tháng cụ thể của reserve time- Kiểu dữ liệu: **INT**                      |
| 06 | end_time         |    |    |    |    | - The specific end time for the event or record- Mốc thời gian hoặc ngày tháng cụ thể của end time- Kiểu dữ liệu: **INT**                              |
| 07 | deposit_amount   |    |    |    |    | - Financial value representing the deposit amount- Giá trị tiền tệ thể hiện deposit amount- Kiểu dữ liệu: **DECIMAL(10,2)**                              |
| 08 | status           |    |    |    |    | - The current operational status or state of the record- Trạng thái hoạt động hoặc tình trạng hiện tại của bản ghi- Kiểu dữ liệu: **VARCHAR(255)** |
| 09 | party_size       |    |    |    |    | - Numeric value indicating the party size- Giá trị số lượng thể hiện party size- Kiểu dữ liệu: **INT**                                                  |
| 10 | special_requests |    |    |    |    | - Detailed information about the special requests- Dữ liệu chi tiết về special requests của hệ thống- Kiểu dữ liệu: **VARCHAR(500)**                    |

### 1.3.40 Tours

[Bảng lưu trữ thông tin về Tours]
[Table storing information about Tours]

[Provide the detailed table fields description using below table format

* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field                 | PK | FK | UN | NN | Description                                                                                                                                                              |
| -- | --------------------- | -- | -- | -- | -- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| 01 | tour_id               | x  |    |    | x  | - Unique identifier for the Tours record- Định danh duy nhất (ID) cho bản ghi Tours- Kiểu dữ liệu: **BIGINT**                                               |
| 02 | tour_name             |    |    |    |    | - The specific name or title associated with the Tours- Tên gọi hoặc tiêu đề cụ thể của Tours- Kiểu dữ liệu: **VARCHAR(255)**                          |
| 03 | tour_type             |    |    |    |    | - Categorization or classification type (tour type)- Loại, danh mục hoặc phân loại của dữ liệu (tour type)- Kiểu dữ liệu: **VARCHAR(255)**              |
| 04 | base_price            |    |    |    |    | - Financial value representing the base price- Giá trị tiền tệ thể hiện base price- Kiểu dữ liệu: **DECIMAL(10,2)**                                       |
| 05 | max_capacity          |    |    |    |    | - Numeric value indicating the max capacity- Giá trị số lượng thể hiện max capacity- Kiểu dữ liệu: **INT**                                               |
| 06 | description           |    |    |    |    | - Additional text description or notes for the record- Mô tả chi tiết bằng văn bản hoặc các ghi chú bổ sung- Kiểu dữ liệu: **TEXT**                   |
| 07 | duration              |    |    |    |    | - Detailed information about the duration- Dữ liệu chi tiết về duration của hệ thống- Kiểu dữ liệu: **VARCHAR(255)**                                     |
| 08 | duration_hours        |    |    |    |    | - Detailed information about the duration hours- Dữ liệu chi tiết về duration hours của hệ thống- Kiểu dữ liệu: **DECIMAL(10,2)**                        |
| 09 | is_insurance_required |    |    |    |    | - Boolean flag indicating whether the record is insurance required- Cờ trạng thái (đúng/sai) để xác định is insurance required- Kiểu dữ liệu: **BIT** |
| 10 | insurance_price       |    |    |    |    | - Financial value representing the insurance price- Giá trị tiền tệ thể hiện insurance price- Kiểu dữ liệu: **DECIMAL(10,2)**                             |
| 11 | image_url             |    |    |    |    | - Direct URL or system data for the image url file- Dữ liệu hoặc đường dẫn (URL) tới image url- Kiểu dữ liệu: **VARCHAR(500)**                          |
| 12 | short_quote           |    |    |    |    | - Detailed information about the short quote- Dữ liệu chi tiết về short quote của hệ thống- Kiểu dữ liệu: **VARCHAR(255)**                               |
| 13 | handbook_spec         |    |    |    |    | - Detailed information about the handbook spec- Dữ liệu chi tiết về handbook spec của hệ thống- Kiểu dữ liệu: **VARCHAR(255)**                           |
| 14 | handbook_logistics    |    |    |    |    | - Detailed information about the handbook logistics- Dữ liệu chi tiết về handbook logistics của hệ thống- Kiểu dữ liệu: **TEXT**                         |
| 15 | handbook_explanations |    |    |    |    | - Detailed information about the handbook explanations- Dữ liệu chi tiết về handbook explanations của hệ thống- Kiểu dữ liệu: **TEXT**                   |
| 16 | is_active             |    |    |    |    | - Boolean flag indicating whether the record is active- Cờ trạng thái (đúng/sai) để xác định is active- Kiểu dữ liệu: **BIT**                         |

### 1.3.41 Tour_Attendees

[Bảng lưu trữ thông tin về Tour Attendees]
[Table storing information about Tour Attendees]

[Provide the detailed table fields description using below table format

* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field             | PK | FK | UN | NN | Description                                                                                                                                                             |
| -- | ----------------- | -- | -- | -- | -- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 01 | attendee_id       | x  |    |    | x  | - Unique identifier for the Tour Attendees record- Định danh duy nhất (ID) cho bản ghi Tour Attendees- Kiểu dữ liệu: **BIGINT**                            |
| 02 | tour_booking_id   |    | x  |    | x  | - Foreign key reference to the related tour booking record- Khóa ngoại tham chiếu đến dữ liệu tour booking liên quan- Kiểu dữ liệu: **BIGINT**         |
| 03 | customer_id       |    | x  |    |    | - Foreign key reference to the related customer record- Khóa ngoại tham chiếu đến dữ liệu customer liên quan- Kiểu dữ liệu: **BIGINT**                 |
| 04 | dependent_id      |    | x  |    |    | - Foreign key reference to the related dependent record- Khóa ngoại tham chiếu đến dữ liệu dependent liên quan- Kiểu dữ liệu: **BIGINT**               |
| 05 | attendance_status |    |    |    | x  | - The current operational status or state of the record- Trạng thái hoạt động hoặc tình trạng hiện tại của bản ghi- Kiểu dữ liệu: **VARCHAR(255)** |
| 06 | face_matched_at   |    |    |    |    | - Detailed information about the face matched at- Dữ liệu chi tiết về face matched at của hệ thống- Kiểu dữ liệu: **DATETIME**                          |
| 07 | face_vector_data  |    |    |    |    | - Direct URL or system data for the face vector data file- Dữ liệu hoặc đường dẫn (URL) tới face vector data- Kiểu dữ liệu: **TEXT**                   |
| 08 | absent_reason     |    |    |    |    | - Detailed information about the absent reason- Dữ liệu chi tiết về absent reason của hệ thống- Kiểu dữ liệu: **VARCHAR(500)**                          |

### 1.3.42 Tour_Bookings

[Bảng lưu trữ thông tin về Tour Bookings]
[Table storing information about Tour Bookings]

[Provide the detailed table fields description using below table format

* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field                  | PK | FK | UN | NN | Description                                                                                                                                                                   |
| -- | ---------------------- | -- | -- | -- | -- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 01 | schedule_id            |    | x  |    | x  | - Foreign key reference to the related schedule record- Khóa ngoại tham chiếu đến dữ liệu schedule liên quan- Kiểu dữ liệu: **BIGINT**                       |
| 02 | participant_count      |    |    |    | x  | - Numeric value indicating the participant count- Giá trị số lượng thể hiện participant count- Kiểu dữ liệu: **INT**                                          |
| 03 | is_walk_in_tour        |    |    |    | x  | - Boolean flag indicating whether the record is walk in tour- Cờ trạng thái (đúng/sai) để xác định is walk in tour- Kiểu dữ liệu: **BIT**                  |
| 04 | room_booking_id        |    | x  |    |    | - Foreign key reference to the related room booking record- Khóa ngoại tham chiếu đến dữ liệu room booking liên quan- Kiểu dữ liệu: **BIGINT**               |
| 05 | room_booking_detail_id |    | x  |    |    | - Foreign key reference to the related room booking detail record- Khóa ngoại tham chiếu đến dữ liệu room booking detail liên quan- Kiểu dữ liệu: **BIGINT** |

### 1.3.43 Tour_Images

[Bảng lưu trữ thông tin về Tour Images]
[Table storing information about Tour Images]

[Provide the detailed table fields description using below table format

* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field      | PK | FK | UN | NN | Description                                                                                                                                        |
| -- | ---------- | -- | -- | -- | -- | -------------------------------------------------------------------------------------------------------------------------------------------------- |
| 01 | image_id   | x  |    |    | x  | - Unique identifier for the Tour Images record- Định danh duy nhất (ID) cho bản ghi Tour Images- Kiểu dữ liệu: **BIGINT**             |
| 02 | tour_id    |    | x  |    | x  | - Foreign key reference to the related tour record- Khóa ngoại tham chiếu đến dữ liệu tour liên quan- Kiểu dữ liệu: **BIGINT**    |
| 03 | image_url  |    |    |    | x  | - Direct URL or system data for the image url file- Dữ liệu hoặc đường dẫn (URL) tới image url- Kiểu dữ liệu: **VARCHAR(500)**    |
| 04 | is_primary |    |    |    | x  | - Boolean flag indicating whether the record is primary- Cờ trạng thái (đúng/sai) để xác định is primary- Kiểu dữ liệu: **BIT** |

### 1.3.44 Tour_Itineraries

[Bảng lưu trữ thông tin về Tour Itineraries]
[Table storing information about Tour Itineraries]

[Provide the detailed table fields description using below table format

* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field        | PK | FK | UN | NN | Description                                                                                                                                      |
| -- | ------------ | -- | -- | -- | -- | ------------------------------------------------------------------------------------------------------------------------------------------------ |
| 01 | itinerary_id | x  |    |    | x  | - Unique identifier for the Tour Itineraries record- Định danh duy nhất (ID) cho bản ghi Tour Itineraries- Kiểu dữ liệu: **BIGINT** |
| 02 | tour_id      |    | x  |    | x  | - Foreign key reference to the related tour record- Khóa ngoại tham chiếu đến dữ liệu tour liên quan- Kiểu dữ liệu: **BIGINT**  |
| 03 | day_number   |    |    |    | x  | - Detailed information about the day number- Dữ liệu chi tiết về day number của hệ thống- Kiểu dữ liệu: **INT**                  |
| 04 | day_title    |    |    |    | x  | - Detailed information about the day title- Dữ liệu chi tiết về day title của hệ thống- Kiểu dữ liệu: **VARCHAR(255)**           |
| 05 | summary      |    |    |    |    | - Detailed information about the summary- Dữ liệu chi tiết về summary của hệ thống- Kiểu dữ liệu: **TEXT**                       |

### 1.3.45 Tour_Itinerary_Details

[Bảng lưu trữ thông tin về Tour Itinerary Details]
[Table storing information about Tour Itinerary Details]

[Provide the detailed table fields description using below table format

* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field                | PK | FK | UN | NN | Description                                                                                                                                                  |
| -- | -------------------- | -- | -- | -- | -- | ------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| 01 | detail_id            | x  |    |    | x  | - Unique identifier for the Tour Itinerary Details record- Định danh duy nhất (ID) cho bản ghi Tour Itinerary Details- Kiểu dữ liệu: **BIGINT** |
| 02 | itinerary_id         |    | x  |    | x  | - Foreign key reference to the related itinerary record- Khóa ngoại tham chiếu đến dữ liệu itinerary liên quan- Kiểu dữ liệu: **BIGINT**    |
| 03 | start_time           |    |    |    | x  | - The specific start time for the event or record- Mốc thời gian hoặc ngày tháng cụ thể của start time- Kiểu dữ liệu: **INT**               |
| 04 | end_time             |    |    |    |    | - The specific end time for the event or record- Mốc thời gian hoặc ngày tháng cụ thể của end time- Kiểu dữ liệu: **INT**                   |
| 05 | location_id          |    | x  |    |    | - Foreign key reference to the related location record- Khóa ngoại tham chiếu đến dữ liệu location liên quan- Kiểu dữ liệu: **BIGINT**      |
| 06 | activity_title       |    |    |    | x  | - Detailed information about the activity title- Dữ liệu chi tiết về activity title của hệ thống- Kiểu dữ liệu: **VARCHAR(150)**             |
| 07 | activity_description |    |    |    | x  | - Detailed information about the activity description- Dữ liệu chi tiết về activity description của hệ thống- Kiểu dữ liệu: **TEXT**         |
| 08 | meal_type            |    |    |    |    | - Categorization or classification type (meal type)- Loại, danh mục hoặc phân loại của dữ liệu (meal type)- Kiểu dữ liệu: **VARCHAR(50)**   |

### 1.3.46 Tour_Locations

[Bảng lưu trữ thông tin về Tour Locations]
[Table storing information about Tour Locations]

[Provide the detailed table fields description using below table format

* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field         | PK | FK | UN | NN | Description                                                                                                                                                       |
| -- | ------------- | -- | -- | -- | -- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 01 | location_id   | x  |    |    | x  | - Unique identifier for the Tour Locations record- Định danh duy nhất (ID) cho bản ghi Tour Locations- Kiểu dữ liệu: **BIGINT**                      |
| 02 | location_name |    |    |    | x  | - The specific name or title associated with the Tour Locations- Tên gọi hoặc tiêu đề cụ thể của Tour Locations- Kiểu dữ liệu: **VARCHAR(150)** |
| 03 | latitude      |    |    |    |    | - Detailed information about the latitude- Dữ liệu chi tiết về latitude của hệ thống- Kiểu dữ liệu: **DECIMAL(10,2)**                             |
| 04 | longitude     |    |    |    |    | - Detailed information about the longitude- Dữ liệu chi tiết về longitude của hệ thống- Kiểu dữ liệu: **DECIMAL(10,2)**                           |
| 05 | description   |    |    |    |    | - Additional text description or notes for the record- Mô tả chi tiết bằng văn bản hoặc các ghi chú bổ sung- Kiểu dữ liệu: **TEXT**            |
| 06 | is_active     |    |    |    | x  | - Boolean flag indicating whether the record is active- Cờ trạng thái (đúng/sai) để xác định is active- Kiểu dữ liệu: **BIT**                  |

### 1.3.47 Tour_Prices

[Bảng lưu trữ thông tin về Tour Prices]
[Table storing information about Tour Prices]

[Provide the detailed table fields description using below table format

* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field                | PK | FK | UN | NN | Description                                                                                                                                            |
| -- | -------------------- | -- | -- | -- | -- | ------------------------------------------------------------------------------------------------------------------------------------------------------ |
| 01 | tour_price_id        | x  |    |    | x  | - Unique identifier for the Tour Prices record- Định danh duy nhất (ID) cho bản ghi Tour Prices- Kiểu dữ liệu: **BIGINT**                 |
| 02 | tour_id              |    | x  |    | x  | - Foreign key reference to the related tour record- Khóa ngoại tham chiếu đến dữ liệu tour liên quan- Kiểu dữ liệu: **BIGINT**        |
| 03 | age_from             |    |    |    | x  | - Detailed information about the age from- Dữ liệu chi tiết về age from của hệ thống- Kiểu dữ liệu: **INT**                            |
| 04 | age_to               |    |    |    | x  | - Detailed information about the age to- Dữ liệu chi tiết về age to của hệ thống- Kiểu dữ liệu: **INT**                                |
| 05 | ticket_price         |    |    |    | x  | - Financial value representing the ticket price- Giá trị tiền tệ thể hiện ticket price- Kiểu dữ liệu: **DECIMAL(10,2)**                 |
| 06 | combo_discount_price |    |    |    | x  | - Financial value representing the combo discount price- Giá trị tiền tệ thể hiện combo discount price- Kiểu dữ liệu: **DECIMAL(10,2)** |
| 07 | is_active            |    |    |    | x  | - Boolean flag indicating whether the record is active- Cờ trạng thái (đúng/sai) để xác định is active- Kiểu dữ liệu: **BIT**       |

### 1.3.48 Tour_Schedules

[Bảng lưu trữ thông tin về Tour Schedules]
[Table storing information about Tour Schedules]

[Provide the detailed table fields description using below table format

* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field                   | PK | FK | UN | NN | Description                                                                                                                                                                |
| -- | ----------------------- | -- | -- | -- | -- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 01 | schedule_id             | x  |    |    | x  | - Unique identifier for the Tour Schedules record- Định danh duy nhất (ID) cho bản ghi Tour Schedules- Kiểu dữ liệu: **BIGINT**                               |
| 02 | tour_id                 |    | x  |    | x  | - Foreign key reference to the related tour record- Khóa ngoại tham chiếu đến dữ liệu tour liên quan- Kiểu dữ liệu: **BIGINT**                            |
| 03 | departure_date          |    |    |    | x  | - The specific departure date for the event or record- Mốc thời gian hoặc ngày tháng cụ thể của departure date- Kiểu dữ liệu: **DATE**                    |
| 04 | departure_time          |    |    |    | x  | - The specific departure time for the event or record- Mốc thời gian hoặc ngày tháng cụ thể của departure time- Kiểu dữ liệu: **INT**                     |
| 05 | booked_seats            |    |    |    | x  | - Detailed information about the booked seats- Dữ liệu chi tiết về booked seats của hệ thống- Kiểu dữ liệu: **INT**                                        |
| 06 | schedule_status         |    |    |    | x  | - The current operational status or state of the record- Trạng thái hoạt động hoặc tình trạng hiện tại của bản ghi- Kiểu dữ liệu: **VARCHAR(255)**    |
| 07 | is_insurance_processed  |    |    |    | x  | - Boolean flag indicating whether the record is insurance processed- Cờ trạng thái (đúng/sai) để xác định is insurance processed- Kiểu dữ liệu: **BIT** |
| 08 | insurance_policy_number |    |    |    |    | - Detailed information about the insurance policy number- Dữ liệu chi tiết về insurance policy number của hệ thống- Kiểu dữ liệu: **VARCHAR(255)**         |

### 1.3.49 Tour_Staff_Assignments

[Bảng lưu trữ thông tin về Tour Staff Assignments]
[Table storing information about Tour Staff Assignments]

[Provide the detailed table fields description using below table format

* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field           | PK | FK | UN | NN | Description                                                                                                                                                   |
| -- | --------------- | -- | -- | -- | -- | ------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 01 | assignment_id   | x  |    |    | x  | - Unique identifier for the Tour Staff Assignments record- Định danh duy nhất (ID) cho bản ghi Tour Staff Assignments- Kiểu dữ liệu: **BIGINT**  |
| 02 | schedule_id     |    | x  |    |    | - Foreign key reference to the related schedule record- Khóa ngoại tham chiếu đến dữ liệu schedule liên quan- Kiểu dữ liệu: **BIGINT**       |
| 03 | employee_id     |    | x  |    |    | - Foreign key reference to the related employee record- Khóa ngoại tham chiếu đến dữ liệu employee liên quan- Kiểu dữ liệu: **BIGINT**       |
| 04 | staff_role      |    |    |    |    | - Categorization or classification type (staff role)- Loại, danh mục hoặc phân loại của dữ liệu (staff role)- Kiểu dữ liệu: **VARCHAR(255)** |
| 05 | is_lead_guide   |    |    |    |    | - Boolean flag indicating whether the record is lead guide- Cờ trạng thái (đúng/sai) để xác định is lead guide- Kiểu dữ liệu: **BIT**      |
| 06 | current_gps_lat |    |    |    |    | - Detailed information about the current gps lat- Dữ liệu chi tiết về current gps lat của hệ thống- Kiểu dữ liệu: **DECIMAL(10,2)**           |
| 07 | current_gps_lng |    |    |    |    | - Detailed information about the current gps lng- Dữ liệu chi tiết về current gps lng của hệ thống- Kiểu dữ liệu: **DECIMAL(10,2)**           |

### 1.3.50 workflows

[Bảng lưu trữ thông tin về workflows]
[Table storing information about workflows]

[Provide the detailed table fields description using below table format

* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field           | PK | FK | UN | NN | Description                                                                                                                                                 |
| -- | --------------- | -- | -- | -- | -- | ----------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 01 | workflow_id     | x  |    |    | x  | - Unique identifier for the workflows record- Định danh duy nhất (ID) cho bản ghi workflows- Kiểu dữ liệu: **BIGINT**                          |
| 02 | workflow_name   |    |    |    | x  | - The specific name or title associated with the workflows- Tên gọi hoặc tiêu đề cụ thể của workflows- Kiểu dữ liệu: **VARCHAR(255)**     |
| 03 | trigger_event   |    |    |    | x  | - Detailed information about the trigger event- Dữ liệu chi tiết về trigger event của hệ thống- Kiểu dữ liệu: **VARCHAR(100)**              |
| 04 | conditions_json |    |    |    |    | - Detailed information about the conditions json- Dữ liệu chi tiết về conditions json của hệ thống- Kiểu dữ liệu: **TEXT**                  |
| 05 | actions_json    |    |    |    |    | - Detailed information about the actions json- Dữ liệu chi tiết về actions json của hệ thống- Kiểu dữ liệu: **TEXT**                        |
| 06 | is_active       |    |    |    | x  | - Boolean flag indicating whether the record is active- Cờ trạng thái (đúng/sai) để xác định is active- Kiểu dữ liệu: **BIT**            |
| 07 | updated_at      |    |    |    |    | - Timestamp indicating when the record was last updated- Thời điểm dữ liệu bản ghi được cập nhật lần cuối- Kiểu dữ liệu: **DATETIME** |
