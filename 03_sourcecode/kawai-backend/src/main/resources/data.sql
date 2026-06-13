-- ============================================================
-- HOANIEN RESORT & TOUR HUB — H2 Sample Data
-- Chạy tự động sau khi Hibernate tạo schema (defer-datasource-initialization: true)
-- ============================================================
-- ── 1. Roles ─────────────────────────────────────────────────
INSERT INTO Roles (role_id, role_name) VALUES (1, 'ADMIN');
INSERT INTO Roles (role_id, role_name) VALUES (2, 'RECEPTIONIST');
INSERT INTO Roles (role_id, role_name) VALUES (3, 'F&B KITCHEN');
INSERT INTO Roles (role_id, role_name) VALUES (4, 'F&B POS');
INSERT INTO Roles (role_id, role_name) VALUES (5, 'HOUSEKEEPING');
INSERT INTO Roles (role_id, role_name) VALUES (6, 'MANAGER');
INSERT INTO Roles (role_id, role_name) VALUES (7, 'TOURGUIDE');
INSERT INTO Roles (role_id, role_name) VALUES (8, 'CUSTOMER VIP');
INSERT INTO Roles (role_id, role_name) VALUES (9, 'CUSTOMER NORMAL');
-- ── 2. Accounts ──────────────────────────────────────────────
-- Mật khẩu: "admin123" (BCrypt hash)
INSERT INTO Accounts (account_id, username, password_hash, is_active, role_id, created_at)
VALUES (1, 'admin', '$2a$10$4bnThA4xQVw1rF2POQv78uAQll2KsUsgF32JaYiVG5d2d3Fhgk83q', TRUE, 1, CURRENT_TIMESTAMP);
-- Mật khẩu: "staff123" (BCrypt hash)
INSERT INTO Accounts (account_id, username, password_hash, is_active, role_id, created_at)
VALUES (2, 'tphuong', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 3, CURRENT_TIMESTAMP);
INSERT INTO Accounts (account_id, username, password_hash, is_active, role_id, created_at)
VALUES (3, 'nmquan', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 3, CURRENT_TIMESTAMP);
INSERT INTO Accounts (account_id, username, password_hash, is_active, role_id, created_at)
VALUES (4, 'lelinh', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 2, CURRENT_TIMESTAMP);
-- Mật khẩu cho Khách: "admin123"
INSERT INTO Accounts (account_id, username, password_hash, is_active, role_id, created_at)
VALUES (5, 'hoangnam', '$2a$10$4bnThA4xQVw1rF2POQv78uAQll2KsUsgF32JaYiVG5d2d3Fhgk83q', TRUE, 9, CURRENT_TIMESTAMP);
INSERT INTO Accounts (account_id, username, password_hash, is_active, role_id, created_at)
VALUES (6, 'vanan', '$2a$10$4bnThA4xQVw1rF2POQv78uAQll2KsUsgF32JaYiVG5d2d3Fhgk83q', TRUE, 9, CURRENT_TIMESTAMP);
INSERT INTO Accounts (account_id, username, password_hash, is_active, role_id, created_at)
VALUES (7, 'phamtuan', '$2a$10$4bnThA4xQVw1rF2POQv78uAQll2KsUsgF32JaYiVG5d2d3Fhgk83q', TRUE, 8, CURRENT_TIMESTAMP);
INSERT INTO Accounts (account_id, username, password_hash, is_active, role_id, created_at)
VALUES (8, 'thibich', '$2a$10$4bnThA4xQVw1rF2POQv78uAQll2KsUsgF32JaYiVG5d2d3Fhgk83q', TRUE, 9, CURRENT_TIMESTAMP);
INSERT INTO Accounts (account_id, username, password_hash, is_active, role_id, created_at)
VALUES (9, 'lequang', '$2a$10$4bnThA4xQVw1rF2POQv78uAQll2KsUsgF32JaYiVG5d2d3Fhgk83q', TRUE, 9, CURRENT_TIMESTAMP);
INSERT INTO Accounts (account_id, username, password_hash, is_active, role_id, created_at)
VALUES (10, 'mylinh', '$2a$10$4bnThA4xQVw1rF2POQv78uAQll2KsUsgF32JaYiVG5d2d3Fhgk83q', TRUE, 9, CURRENT_TIMESTAMP);
INSERT INTO Accounts (account_id, username, password_hash, is_active, role_id, created_at)
VALUES (11, 'hoanganh', '$2a$10$4bnThA4xQVw1rF2POQv78uAQll2KsUsgF32JaYiVG5d2d3Fhgk83q', TRUE, 8, CURRENT_TIMESTAMP);
INSERT INTO Accounts (account_id, username, password_hash, is_active, role_id, created_at)
VALUES (12, 'vuhung', '$2a$10$4bnThA4xQVw1rF2POQv78uAQll2KsUsgF32JaYiVG5d2d3Fhgk83q', TRUE, 8, CURRENT_TIMESTAMP);
-- ── 3. Employees ─────────────────────────────────────────────
INSERT INTO Employees (employee_id, account_id, full_name, gender, cccd, phone, email, salary)
VALUES (1, 1, 'Nguyễn Quản Trị', 'Nam', '001234567890', '0912000001', 'admin@hoanien.vn', 15000000);
INSERT INTO Employees (employee_id, account_id, full_name, gender, cccd, phone, email, salary)
VALUES (2, 2, 'Trần Phương', 'Nữ', '001234567891', '0912000002', 'tphuong@hoanien.vn', 10000000);
INSERT INTO Employees (employee_id, account_id, full_name, gender, cccd, phone, email, salary)
VALUES (3, 3, 'Nguyễn Minh Quân', 'Nam', '001234567892', '0912000003', 'nmquan@hoanien.vn', 10000000);
INSERT INTO Employees (employee_id, account_id, full_name, gender, cccd, phone, email, salary)
VALUES (4, 4, 'Lê Linh', 'Nữ', '001234567893', '0912000004', 'lelinh@hoanien.vn', 9000000);
-- ── 4. Room Categories ───────────────────────────────────────
INSERT INTO Room_Categories (category_id, category_name, base_price, capacity)
VALUES (1, 'Deluxe Room', 2500000, 2);
INSERT INTO Room_Categories (category_id, category_name, base_price, capacity)
VALUES (2, 'Superior Room', 3500000, 3);
INSERT INTO Room_Categories (category_id, category_name, base_price, capacity)
VALUES (3, 'Villa Suite', 8000000, 4);
-- ── 5. Rooms ─────────────────────────────────────────────────
INSERT INTO Rooms (room_id, room_number, category_id, room_status, current_booking_detail_id) VALUES (1, '101', 1, 'Occupied', NULL);
INSERT INTO Rooms (room_id, room_number, category_id, room_status, current_booking_detail_id) VALUES (2, '102', 1, 'Vacant_Clean', NULL);
INSERT INTO Rooms (room_id, room_number, category_id, room_status, current_booking_detail_id) VALUES (3, '204', 2, 'Occupied', NULL);
INSERT INTO Rooms (room_id, room_number, category_id, room_status, current_booking_detail_id) VALUES (4, '205', 2, 'Vacant_Dirty', NULL);
INSERT INTO Rooms (room_id, room_number, category_id, room_status, current_booking_detail_id) VALUES (5, '308', 3, 'Occupied', NULL);
-- Thêm 15 phòng mới
INSERT INTO Rooms (room_id, room_number, category_id, room_status, current_booking_detail_id) VALUES (6, '103', 1, 'Vacant_Clean', NULL);
INSERT INTO Rooms (room_id, room_number, category_id, room_status, current_booking_detail_id) VALUES (7, '104', 1, 'Occupied', NULL);
INSERT INTO Rooms (room_id, room_number, category_id, room_status, current_booking_detail_id) VALUES (8, '105', 1, 'Vacant_Dirty', NULL);
INSERT INTO Rooms (room_id, room_number, category_id, room_status, current_booking_detail_id) VALUES (9, '106', 1, 'Occupied', NULL);
INSERT INTO Rooms (room_id, room_number, category_id, room_status, current_booking_detail_id) VALUES (10, '107', 1, 'Vacant_Clean', NULL);
INSERT INTO Rooms (room_id, room_number, category_id, room_status, current_booking_detail_id) VALUES (11, '206', 2, 'Vacant_Clean', NULL);
INSERT INTO Rooms (room_id, room_number, category_id, room_status, current_booking_detail_id) VALUES (12, '207', 2, 'Occupied', NULL);
INSERT INTO Rooms (room_id, room_number, category_id, room_status, current_booking_detail_id) VALUES (13, '208', 2, 'Vacant_Dirty', NULL);
INSERT INTO Rooms (room_id, room_number, category_id, room_status, current_booking_detail_id) VALUES (14, '209', 2, 'Vacant_Clean', NULL);
INSERT INTO Rooms (room_id, room_number, category_id, room_status, current_booking_detail_id) VALUES (15, '210', 2, 'Occupied', NULL);
INSERT INTO Rooms (room_id, room_number, category_id, room_status, current_booking_detail_id) VALUES (16, '309', 3, 'Vacant_Clean', NULL);
INSERT INTO Rooms (room_id, room_number, category_id, room_status, current_booking_detail_id) VALUES (17, '310', 3, 'Vacant_Dirty', NULL);
INSERT INTO Rooms (room_id, room_number, category_id, room_status, current_booking_detail_id) VALUES (18, '311', 3, 'Occupied', NULL);
INSERT INTO Rooms (room_id, room_number, category_id, room_status, current_booking_detail_id) VALUES (19, '312', 3, 'Vacant_Clean', NULL);
INSERT INTO Rooms (room_id, room_number, category_id, room_status, current_booking_detail_id) VALUES (20, '314', 3, 'Vacant_Clean', NULL);

-- ── 5.1. Customers & Bookings cho các phòng Occupied ──
INSERT INTO Customers (customer_id, account_id, full_name, gender, cccd_passport_encrypted, phone, email, loyalty_points, membership_tier) VALUES 
(1, 5, 'Lê Hoàng Nam', 'Nam', 'CCCD_101', '090101', 'nam101@test.com', 100, 'Regular'),
(2, 6, 'Nguyễn Văn An', 'Nam', 'CCCD_204', '090204', 'an204@test.com', 200, 'Silver'),
(3, 7, 'Phạm Tuấn', 'Nam', 'CCCD_308', '090308', 'tuan308@test.com', 500, 'Gold'),
(4, 8, 'Trần Thị Bích', 'Nữ', 'CCCD_104', '090104', 'bich104@test.com', 50, 'Regular'),
(5, 9, 'Lê Quang', 'Nam', 'CCCD_106', '090106', 'quang106@test.com', 0, 'Regular'),
(6, 10, 'Đỗ Mỹ Linh', 'Nữ', 'CCCD_207', '090207', 'linh207@test.com', 150, 'Silver'),
(7, 11, 'Hoàng Anh', 'Nam', 'CCCD_210', '090210', 'anh210@test.com', 300, 'Gold'),
(8, 12, 'Vũ Hùng', 'Nam', 'CCCD_311', '090311', 'hung311@test.com', 800, 'Platinum');

INSERT INTO Bookings (booking_id, customer_id, booking_date, total_price, booking_status, booking_source, version) VALUES
(1, 1, '2026-06-01', 5000000, 'Confirmed', 'Direct_Web', 1),
(2, 2, '2026-06-02', 7000000, 'Confirmed', 'Direct_Web', 1),
(3, 3, '2026-06-03', 16000000, 'Confirmed', 'OTA', 1),
(4, 4, '2026-06-04', 5000000, 'Confirmed', 'OTA', 1),
(5, 5, '2026-06-05', 5000000, 'Confirmed', 'Direct_Web', 1),
(6, 6, '2026-06-06', 7000000, 'Confirmed', 'OTA', 1),
(7, 7, '2026-06-07', 7000000, 'Confirmed', 'Direct_Web', 1),
(8, 8, '2026-06-08', 16000000, 'Confirmed', 'Direct_Web', 1);

INSERT INTO Room_Bookings (booking_id, check_in_date, check_out_date, deposit_amount, cancellation_deadline, credit_limit, personal_pin_hash) VALUES
(1, '2026-06-09', '2026-06-12', 1000000, '2026-06-05', 5000000, 'hash'),
(2, '2026-06-09', '2026-06-12', 1000000, '2026-06-05', 5000000, 'hash'),
(3, '2026-06-09', '2026-06-12', 2000000, '2026-06-05', 10000000, 'hash'),
(4, '2026-06-09', '2026-06-11', 1000000, '2026-06-05', 5000000, 'hash'),
(5, '2026-06-10', '2026-06-13', 1000000, '2026-06-06', 5000000, 'hash'),
(6, '2026-06-10', '2026-06-14', 1500000, '2026-06-06', 5000000, 'hash'),
(7, '2026-06-10', '2026-06-15', 1500000, '2026-06-06', 5000000, 'hash'),
(8, '2026-06-10', '2026-06-16', 3000000, '2026-06-06', 15000000, 'hash');

INSERT INTO Room_Booking_Details (detail_id, booking_id, category_id, room_id, customer_id, room_charge, detail_status, is_charge_to_room_allowed, sub_credit_limit, billing_routing_strategy) VALUES
(1, 1, 1, 1, 1, 2500000, 'Pending', TRUE, 500000, 'BILL_TO_LEADER'),
(2, 2, 2, 3, 2, 3500000, 'Pending', TRUE, 1500000, 'BILL_TO_LEADER'),
(3, 3, 3, 5, 3, 8000000, 'Pending', TRUE, 2000000, 'BILL_TO_LEADER'),
(4, 4, 1, 7, 4, 2500000, 'Pending', TRUE, 500000, 'BILL_TO_LEADER'),
(5, 5, 1, 9, 5, 2500000, 'Pending', TRUE, 500000, 'BILL_TO_LEADER'),
(6, 6, 2, 12, 6, 3500000, 'Pending', TRUE, 1500000, 'BILL_TO_LEADER'),
(7, 7, 2, 15, 7, 3500000, 'Pending', TRUE, 1500000, 'BILL_TO_LEADER'),
(8, 8, 3, 18, 8, 8000000, 'Pending', TRUE, 3000000, 'BILL_TO_LEADER');

UPDATE Rooms SET current_booking_detail_id = 1 WHERE room_id = 1;
UPDATE Rooms SET current_booking_detail_id = 2 WHERE room_id = 3;
UPDATE Rooms SET current_booking_detail_id = 3 WHERE room_id = 5;
UPDATE Rooms SET current_booking_detail_id = 4 WHERE room_id = 7;
UPDATE Rooms SET current_booking_detail_id = 5 WHERE room_id = 9;
UPDATE Rooms SET current_booking_detail_id = 6 WHERE room_id = 12;
UPDATE Rooms SET current_booking_detail_id = 7 WHERE room_id = 15;
UPDATE Rooms SET current_booking_detail_id = 8 WHERE room_id = 18;
-- ── 6. Restaurant Tables ─────────────────────────────────────
INSERT INTO Restaurant_Tables (table_id, table_number, capacity, table_status) VALUES (1, 'T01', 4, 'Occupied');
INSERT INTO Restaurant_Tables (table_id, table_number, capacity, table_status) VALUES (2, 'T02', 2, 'Vacant');
INSERT INTO Restaurant_Tables (table_id, table_number, capacity, table_status) VALUES (3, 'T03', 6, 'Occupied');
INSERT INTO Restaurant_Tables (table_id, table_number, capacity, table_status) VALUES (4, 'T04', 4, 'Reserved');
INSERT INTO Restaurant_Tables (table_id, table_number, capacity, table_status) VALUES (5, 'T05', 8, 'Vacant');
INSERT INTO Restaurant_Tables (table_id, table_number, capacity, table_status) VALUES (6, 'T06', 4, 'Vacant');
INSERT INTO Restaurant_Tables (table_id, table_number, capacity, table_status) VALUES (7, 'T07', 2, 'Occupied');
INSERT INTO Restaurant_Tables (table_id, table_number, capacity, table_status) VALUES (8, 'T08', 4, 'Reserved');
INSERT INTO Restaurant_Tables (table_id, table_number, capacity, table_status) VALUES (9, 'T09', 10, 'Vacant');
INSERT INTO Restaurant_Tables (table_id, table_number, capacity, table_status) VALUES (10, 'T10', 2, 'Vacant');
INSERT INTO Restaurant_Tables (table_id, table_number, capacity, table_status) VALUES (11, 'T11', 4, 'Occupied');
INSERT INTO Restaurant_Tables (table_id, table_number, capacity, table_status) VALUES (12, 'T12', 6, 'Vacant');
INSERT INTO Restaurant_Tables (table_id, table_number, capacity, table_status) VALUES (13, 'T13', 4, 'Vacant');
INSERT INTO Restaurant_Tables (table_id, table_number, capacity, table_status) VALUES (14, 'T14', 8, 'Reserved');
INSERT INTO Restaurant_Tables (table_id, table_number, capacity, table_status) VALUES (15, 'T15', 2, 'Occupied');
INSERT INTO Restaurant_Tables (table_id, table_number, capacity, table_status) VALUES (16, 'T16', 4, 'Vacant');
INSERT INTO Restaurant_Tables (table_id, table_number, capacity, table_status) VALUES (17, 'T17', 4, 'Vacant');
INSERT INTO Restaurant_Tables (table_id, table_number, capacity, table_status) VALUES (18, 'T18', 6, 'Occupied');
INSERT INTO Restaurant_Tables (table_id, table_number, capacity, table_status) VALUES (19, 'T19', 2, 'Vacant');
INSERT INTO Restaurant_Tables (table_id, table_number, capacity, table_status) VALUES (20, 'T20', 12, 'Reserved');
-- ── 7. Menu Items ────────────────────────────────────────────
-- Khai vị
INSERT INTO Menu_Items (item_id, item_name, price, category, is_available) VALUES (1, 'Súp Bí Đỏ Kem Tươi Truffle', 180000, 'Khai vị', TRUE);
INSERT INTO Menu_Items (item_id, item_name, price, category, is_available) VALUES (2, 'Gỏi Cuốn Tôm Thịt', 95000, 'Khai vị', TRUE);
INSERT INTO Menu_Items (item_id, item_name, price, category, is_available) VALUES (3, 'Chả Giò Hải Sản', 110000, 'Khai vị', TRUE);
INSERT INTO Menu_Items (item_id, item_name, price, category, is_available) VALUES (4, 'Salad Cá Hồi Xông Khói', 150000, 'Khai vị', TRUE);
INSERT INTO Menu_Items (item_id, item_name, price, category, is_available) VALUES (5, 'Súp Hải Sản Măng Tây', 130000, 'Khai vị', TRUE);
INSERT INTO Menu_Items (item_id, item_name, price, category, is_available) VALUES (6, 'Bánh Mì Bơ Tỏi', 65000, 'Khai vị', TRUE);
INSERT INTO Menu_Items (item_id, item_name, price, category, is_available) VALUES (7, 'Nem Chua Rán', 75000, 'Khai vị', TRUE);
INSERT INTO Menu_Items (item_id, item_name, price, category, is_available) VALUES (8, 'Hoành Thánh Chiên Giòn', 85000, 'Khai vị', TRUE);
-- Món chính
INSERT INTO Menu_Items (item_id, item_name, price, category, is_available) VALUES (9, 'Bò Bít Tết Wagyu Kèm Sốt Tiêu Xanh', 850000, 'Món chính', TRUE);
INSERT INTO Menu_Items (item_id, item_name, price, category, is_available) VALUES (10, 'Cá Hồi Nướng Sốt Miso Nhật Bản', 520000, 'Món chính', TRUE);
INSERT INTO Menu_Items (item_id, item_name, price, category, is_available) VALUES (11, 'Phở Bò Truyền Thống', 120000, 'Món chính', TRUE);
INSERT INTO Menu_Items (item_id, item_name, price, category, is_available) VALUES (12, 'Cơm Chiên Dương Châu', 85000, 'Món chính', TRUE);
INSERT INTO Menu_Items (item_id, item_name, price, category, is_available) VALUES (13, 'Sườn Heo Nướng BBQ', 250000, 'Món chính', TRUE);
INSERT INTO Menu_Items (item_id, item_name, price, category, is_available) VALUES (14, 'Mì Ý Sốt Bò Băm', 140000, 'Món chính', TRUE);
INSERT INTO Menu_Items (item_id, item_name, price, category, is_available) VALUES (15, 'Gà Nướng Mật Ong', 180000, 'Món chính', TRUE);
INSERT INTO Menu_Items (item_id, item_name, price, category, is_available) VALUES (16, 'Lẩu Thái Hải Sản', 350000, 'Món chính', TRUE);
INSERT INTO Menu_Items (item_id, item_name, price, category, is_available) VALUES (17, 'Bún Chả Hà Nội', 95000, 'Món chính', TRUE);
INSERT INTO Menu_Items (item_id, item_name, price, category, is_available) VALUES (18, 'Pizza Hải Sản', 210000, 'Món chính', TRUE);
INSERT INTO Menu_Items (item_id, item_name, price, category, is_available) VALUES (19, 'Cơm Gà Hải Nam', 110000, 'Món chính', TRUE);
INSERT INTO Menu_Items (item_id, item_name, price, category, is_available) VALUES (20, 'Mực Ống Nhồi Thịt', 170000, 'Món chính', TRUE);
-- Tráng miệng
INSERT INTO Menu_Items (item_id, item_name, price, category, is_available) VALUES (21, 'Bánh Tiramisu Truyền Thống Ý', 120000, 'Tráng miệng', FALSE);
INSERT INTO Menu_Items (item_id, item_name, price, category, is_available) VALUES (22, 'Chè Xoài Dừa Tươi', 65000, 'Tráng miệng', TRUE);
INSERT INTO Menu_Items (item_id, item_name, price, category, is_available) VALUES (23, 'Kem Xôi Dừa', 55000, 'Tráng miệng', TRUE);
INSERT INTO Menu_Items (item_id, item_name, price, category, is_available) VALUES (24, 'Bánh Flan Caramel', 45000, 'Tráng miệng', TRUE);
INSERT INTO Menu_Items (item_id, item_name, price, category, is_available) VALUES (25, 'Panna Cotta Dâu Tây', 75000, 'Tráng miệng', TRUE);
INSERT INTO Menu_Items (item_id, item_name, price, category, is_available) VALUES (26, 'Trái Cây Thập Cẩm', 110000, 'Tráng miệng', TRUE);
INSERT INTO Menu_Items (item_id, item_name, price, category, is_available) VALUES (27, 'Bánh Mousse Chocolate', 90000, 'Tráng miệng', TRUE);
-- Đồ uống
INSERT INTO Menu_Items (item_id, item_name, price, category, is_available) VALUES (28, 'Nước Cam Tươi Ép Lạnh', 95000, 'Đồ uống', TRUE);
INSERT INTO Menu_Items (item_id, item_name, price, category, is_available) VALUES (29, 'Cà Phê Phin Việt Nam', 55000, 'Đồ uống', TRUE);
INSERT INTO Menu_Items (item_id, item_name, price, category, is_available) VALUES (30, 'Trà Đào Cam Sả', 65000, 'Đồ uống', TRUE);
INSERT INTO Menu_Items (item_id, item_name, price, category, is_available) VALUES (31, 'Sinh Tố Bơ', 75000, 'Đồ uống', TRUE);
INSERT INTO Menu_Items (item_id, item_name, price, category, is_available) VALUES (32, 'Mojito Chanh Bạc Hà', 85000, 'Đồ uống', TRUE);
INSERT INTO Menu_Items (item_id, item_name, price, category, is_available) VALUES (33, 'Bia Heineken', 45000, 'Đồ uống', TRUE);
INSERT INTO Menu_Items (item_id, item_name, price, category, is_available) VALUES (34, 'Nước Khoáng Evian', 40000, 'Đồ uống', TRUE);
-- ── 8. Food Orders ───────────────────────────────────────────
-- ORD-001: Dine-in, Bàn T01, Đang chế biến
INSERT INTO Food_Orders (order_id, booking_id, room_booking_detail_id, table_id, order_type, order_status, payment_type, is_paid_in_pos, created_by_staff_id, kitchen_processed_by_id)
VALUES (1, NULL, NULL, 1, 'Dine-In', 'Preparing', 'Pay at Counter', FALSE, 2, 3);
-- ORD-002: Room Service, Phòng 308, Pending
INSERT INTO Food_Orders (order_id, booking_id, room_booking_detail_id, table_id, order_type, order_status, payment_type, is_paid_in_pos, created_by_staff_id, kitchen_processed_by_id)
VALUES (2, NULL, NULL, NULL, 'Room Service', 'Pending', 'Post to Room', FALSE, 2, NULL);
-- ORD-003: Dine-in, Bàn T03, Served
INSERT INTO Food_Orders (order_id, booking_id, room_booking_detail_id, table_id, order_type, order_status, payment_type, is_paid_in_pos, created_by_staff_id, kitchen_processed_by_id)
VALUES (3, NULL, NULL, 3, 'Dine-In', 'Served', 'Pay at Counter', TRUE, 2, 3);
-- ORD-004
INSERT INTO Food_Orders (order_id, booking_id, room_booking_detail_id, table_id, order_type, order_status, payment_type, is_paid_in_pos, created_by_staff_id, kitchen_processed_by_id)
VALUES (4, NULL, NULL, 2, 'Dine-In', 'Pending', 'Pay at Counter', FALSE, 2, NULL);
-- ORD-005
INSERT INTO Food_Orders (order_id, booking_id, room_booking_detail_id, table_id, order_type, order_status, payment_type, is_paid_in_pos, created_by_staff_id, kitchen_processed_by_id)
VALUES (5, NULL, NULL, 4, 'Dine-In', 'Served', 'Post to Room', TRUE, 2, 3);
-- ORD-006
INSERT INTO Food_Orders (order_id, booking_id, room_booking_detail_id, table_id, order_type, order_status, payment_type, is_paid_in_pos, created_by_staff_id, kitchen_processed_by_id)
VALUES (6, NULL, NULL, 5, 'Dine-In', 'Ready', 'Pay at Counter', FALSE, 3, 2);
-- ORD-007
INSERT INTO Food_Orders (order_id, booking_id, room_booking_detail_id, table_id, order_type, order_status, payment_type, is_paid_in_pos, created_by_staff_id, kitchen_processed_by_id)
VALUES (7, NULL, NULL, NULL, 'Takeaway', 'Completed', 'Pay at Counter', TRUE, 2, 3);
-- ORD-008
INSERT INTO Food_Orders (order_id, booking_id, room_booking_detail_id, table_id, order_type, order_status, payment_type, is_paid_in_pos, created_by_staff_id, kitchen_processed_by_id)
VALUES (8, NULL, NULL, 7, 'Dine-In', 'Preparing', 'Pay at Counter', FALSE, 3, 2);
-- ORD-009
INSERT INTO Food_Orders (order_id, booking_id, room_booking_detail_id, table_id, order_type, order_status, payment_type, is_paid_in_pos, created_by_staff_id, kitchen_processed_by_id)
VALUES (9, NULL, NULL, NULL, 'Room Service', 'Pending', 'Post to Room', FALSE, 2, NULL);
-- ORD-010
INSERT INTO Food_Orders (order_id, booking_id, room_booking_detail_id, table_id, order_type, order_status, payment_type, is_paid_in_pos, created_by_staff_id, kitchen_processed_by_id)
VALUES (10, NULL, NULL, 9, 'Dine-In', 'Served', 'Pay at Counter', TRUE, 3, 2);
-- ORD-011
INSERT INTO Food_Orders (order_id, booking_id, room_booking_detail_id, table_id, order_type, order_status, payment_type, is_paid_in_pos, created_by_staff_id, kitchen_processed_by_id)
VALUES (11, NULL, NULL, 11, 'Dine-In', 'Pending', 'Pay at Counter', FALSE, 2, NULL);
-- ORD-012
INSERT INTO Food_Orders (order_id, booking_id, room_booking_detail_id, table_id, order_type, order_status, payment_type, is_paid_in_pos, created_by_staff_id, kitchen_processed_by_id)
VALUES (12, NULL, NULL, 12, 'Dine-In', 'Completed', 'Pay at Counter', TRUE, 3, 2);
-- ORD-013
INSERT INTO Food_Orders (order_id, booking_id, room_booking_detail_id, table_id, order_type, order_status, payment_type, is_paid_in_pos, created_by_staff_id, kitchen_processed_by_id)
VALUES (13, NULL, NULL, NULL, 'Room Service', 'Ready', 'Post to Room', FALSE, 2, 3);
-- ORD-014
INSERT INTO Food_Orders (order_id, booking_id, room_booking_detail_id, table_id, order_type, order_status, payment_type, is_paid_in_pos, created_by_staff_id, kitchen_processed_by_id)
VALUES (14, NULL, NULL, 14, 'Dine-In', 'Preparing', 'Pay at Counter', FALSE, 2, 3);
-- ORD-015
INSERT INTO Food_Orders (order_id, booking_id, room_booking_detail_id, table_id, order_type, order_status, payment_type, is_paid_in_pos, created_by_staff_id, kitchen_processed_by_id)
VALUES (15, NULL, NULL, 15, 'Dine-In', 'Served', 'Pay at Counter', TRUE, 3, 2);
-- ORD-016
INSERT INTO Food_Orders (order_id, booking_id, room_booking_detail_id, table_id, order_type, order_status, payment_type, is_paid_in_pos, created_by_staff_id, kitchen_processed_by_id)
VALUES (16, NULL, NULL, 16, 'Dine-In', 'Pending', 'Pay at Counter', FALSE, 2, NULL);
-- ORD-017
INSERT INTO Food_Orders (order_id, booking_id, room_booking_detail_id, table_id, order_type, order_status, payment_type, is_paid_in_pos, created_by_staff_id, kitchen_processed_by_id)
VALUES (17, NULL, NULL, NULL, 'Takeaway', 'Completed', 'Pay at Counter', TRUE, 3, 2);
-- ORD-018
INSERT INTO Food_Orders (order_id, booking_id, room_booking_detail_id, table_id, order_type, order_status, payment_type, is_paid_in_pos, created_by_staff_id, kitchen_processed_by_id)
VALUES (18, NULL, NULL, 18, 'Dine-In', 'Preparing', 'Pay at Counter', FALSE, 2, 3);
-- ── 9. Food Order Details ────────────────────────────────────
-- ORD-001 items
INSERT INTO Food_Order_Details (detail_id, order_id, item_id, quantity, price_at_order, kot_status)
VALUES (5, 2, 9, 2, 95000, 'Pending');
-- ORD-004 items
INSERT INTO Food_Order_Details (detail_id, order_id, item_id, quantity, price_at_order, kot_status)
VALUES (6, 4, 11, 2, 120000, 'Pending');
-- ORD-005 items
INSERT INTO Food_Order_Details (detail_id, order_id, item_id, quantity, price_at_order, kot_status)
VALUES (7, 5, 2, 1, 95000, 'Served');
-- ORD-006 items
INSERT INTO Food_Order_Details (detail_id, order_id, item_id, quantity, price_at_order, kot_status)
VALUES (8, 6, 16, 1, 350000, 'Ready');
-- ORD-007 items
INSERT INTO Food_Order_Details (detail_id, order_id, item_id, quantity, price_at_order, kot_status)
VALUES (9, 7, 28, 2, 95000, 'Served');
-- ORD-008 items
INSERT INTO Food_Order_Details (detail_id, order_id, item_id, quantity, price_at_order, kot_status)
VALUES (10, 8, 4, 1, 150000, 'Preparing');
-- ORD-009 items
INSERT INTO Food_Order_Details (detail_id, order_id, item_id, quantity, price_at_order, kot_status)
VALUES (11, 9, 10, 1, 520000, 'Pending');
-- ORD-010 items
INSERT INTO Food_Order_Details (detail_id, order_id, item_id, quantity, price_at_order, kot_status)
VALUES (12, 10, 12, 3, 85000, 'Served');
-- ORD-011 items
INSERT INTO Food_Order_Details (detail_id, order_id, item_id, quantity, price_at_order, kot_status)
VALUES (13, 11, 22, 2, 65000, 'Pending');
-- ORD-012 items
INSERT INTO Food_Order_Details (detail_id, order_id, item_id, quantity, price_at_order, kot_status)
VALUES (14, 12, 33, 5, 45000, 'Served');
-- ORD-013 items
INSERT INTO Food_Order_Details (detail_id, order_id, item_id, quantity, price_at_order, kot_status)
VALUES (15, 13, 30, 1, 65000, 'Ready');
-- ORD-014 items
INSERT INTO Food_Order_Details (detail_id, order_id, item_id, quantity, price_at_order, kot_status)
VALUES (16, 14, 15, 2, 180000, 'Preparing');
-- ORD-015 items
INSERT INTO Food_Order_Details (detail_id, order_id, item_id, quantity, price_at_order, kot_status)
VALUES (17, 15, 34, 4, 40000, 'Served');
-- ORD-016 items
INSERT INTO Food_Order_Details (detail_id, order_id, item_id, quantity, price_at_order, kot_status)
VALUES (18, 16, 5, 2, 130000, 'Pending');
-- ORD-017 items
INSERT INTO Food_Order_Details (detail_id, order_id, item_id, quantity, price_at_order, kot_status)
VALUES (19, 17, 18, 1, 210000, 'Served');
-- ORD-018 items
INSERT INTO Food_Order_Details (detail_id, order_id, item_id, quantity, price_at_order, kot_status)
VALUES (20, 18, 9, 2, 850000, 'Preparing');

-- ORD-019 (Mới tạo từ yêu cầu)
INSERT INTO Food_Orders (order_id, booking_id, room_booking_detail_id, table_id, order_type, order_status, payment_type, is_paid_in_pos, created_by_staff_id, kitchen_processed_by_id)
VALUES (19, NULL, NULL, NULL, 'Room Service', 'Pending', 'Post to Room', FALSE, 2, NULL);

-- ORD-019 items
INSERT INTO Food_Order_Details (detail_id, order_id, item_id, quantity, price_at_order, kot_status)
VALUES (21, 19, 1, 2, 180000, 'Pending');

ALTER TABLE Roles ALTER COLUMN role_id RESTART WITH 100;
ALTER TABLE Accounts ALTER COLUMN account_id RESTART WITH 100;
ALTER TABLE Employees ALTER COLUMN employee_id RESTART WITH 100;
ALTER TABLE Room_Categories ALTER COLUMN category_id RESTART WITH 100;
ALTER TABLE Rooms ALTER COLUMN room_id RESTART WITH 100;
ALTER TABLE Customers ALTER COLUMN customer_id RESTART WITH 100;
ALTER TABLE Bookings ALTER COLUMN booking_id RESTART WITH 100;
ALTER TABLE Room_Booking_Details ALTER COLUMN detail_id RESTART WITH 100;
ALTER TABLE Restaurant_Tables ALTER COLUMN table_id RESTART WITH 100;
ALTER TABLE Menu_Items ALTER COLUMN item_id RESTART WITH 100;
ALTER TABLE Food_Orders ALTER COLUMN order_id RESTART WITH 100;
ALTER TABLE Food_Order_Details ALTER COLUMN detail_id RESTART WITH 100;


-- ── 10. Tours & Schedules ────────────────────────────────────
INSERT INTO Tours (tour_id, tour_name, tour_type, base_price, max_capacity, description)
VALUES (1, 'Đoàn tụ - Huế', 'Full-Day', 1500000, 20, 'Tìm về hơi ấm vẹn nguyên của lòng biết ơn và sự gắn kết.');

INSERT INTO Tours (tour_id, tour_name, tour_type, base_price, max_capacity, description)
VALUES (2, 'Tinh túy đồng nội - Quảng Nam', 'Half-Day', 1200000, 15, 'Lắng nghe nhịp điệu mộc mạc của đất mẹ và hồn quê xứ Quảng.');

INSERT INTO Tours (tour_id, tour_name, tour_type, base_price, max_capacity, description)
VALUES (3, 'Di sản thủ công - Ninh Bình', 'Half-Day', 1800000, 15, 'Chạm vào hồn cốt của thời gian qua những tạo tác từ đôi bàn tay nghệ nhân.');

INSERT INTO Tours (tour_id, tour_name, tour_type, base_price, max_capacity, description)
VALUES (4, 'Tĩnh lặng liên hoa - Tháp Mười', 'Full-Day', 2500000, 10, 'Sự thanh lọc thuần khiết cho thân - tâm - trí giữa vùng sông nước mờ sương.');

-- Thêm lịch trình cho vài ngày tới (dùng cứng ngày tháng sáu 2026)
INSERT INTO Tour_Schedules (schedule_id, tour_id, departure_date, departure_time, booked_seats, schedule_status)
VALUES (1, 1, '2026-06-14', '08:00:00', 0, 'Open');

INSERT INTO Tour_Schedules (schedule_id, tour_id, departure_date, departure_time, booked_seats, schedule_status)
VALUES (2, 2, '2026-06-15', '14:00:00', 0, 'Open');

INSERT INTO Tour_Schedules (schedule_id, tour_id, departure_date, departure_time, booked_seats, schedule_status)
VALUES (3, 3, '2026-06-15', '17:00:00', 0, 'Open');

INSERT INTO Tour_Schedules (schedule_id, tour_id, departure_date, departure_time, booked_seats, schedule_status)
VALUES (4, 4, '2026-06-16', '09:00:00', 0, 'Open');

ALTER TABLE Tours ALTER COLUMN tour_id RESTART WITH 100;
ALTER TABLE Tour_Schedules ALTER COLUMN schedule_id RESTART WITH 100;
