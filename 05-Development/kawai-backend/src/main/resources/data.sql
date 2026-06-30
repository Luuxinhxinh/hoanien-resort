-- ============================================================
-- KAWAI RESORT & TOUR HUB — COMPREHENSIVE SAMPLE DATA (V3.3)
-- Automatically executed on Spring Boot startup
-- ============================================================

-- ── 1. Roles (10 rows) ───────────────────────────────────────
INSERT INTO Roles (role_id, role_name, permissions) VALUES (1, 'ADMIN', 'MASTER_DATA,AUDIT_LOG,DASHBOARD,BOOKING,FNB,TOUR,HOUSEKEEPING,MAINTENANCE,NIGHT_AUDIT,ANALYTICS,REVIEWS,PROMOTIONS,CRM,WORKFLOW');
INSERT INTO Roles (role_id, role_name, permissions) VALUES (2, 'RECEPTIONIST', 'DASHBOARD,BOOKING,NIGHT_AUDIT,CRM,REVIEWS');
INSERT INTO Roles (role_id, role_name, permissions) VALUES (3, 'F&B KITCHEN', 'DASHBOARD,FNB');
INSERT INTO Roles (role_id, role_name, permissions) VALUES (4, 'F&B POS', 'DASHBOARD,FNB');
INSERT INTO Roles (role_id, role_name, permissions) VALUES (5, 'HOUSEKEEPING', 'DASHBOARD,HOUSEKEEPING');
INSERT INTO Roles (role_id, role_name, permissions) VALUES (6, 'MAINTAINER', 'DASHBOARD,MAINTENANCE');
INSERT INTO Roles (role_id, role_name, permissions) VALUES (7, 'MANAGER', 'DASHBOARD,ANALYTICS,REVIEWS,PROMOTIONS,CRM,WORKFLOW');
INSERT INTO Roles (role_id, role_name, permissions) VALUES (8, 'TOURGUIDE', 'DASHBOARD,TOUR');
INSERT INTO Roles (role_id, role_name, permissions) VALUES (9, 'CUSTOMER VIP', '');
INSERT INTO Roles (role_id, role_name, permissions) VALUES (10, 'CUSTOMER NORMAL', '');

-- ── 2. Accounts (20 rows) ────────────────────────────────────
-- password hash for 'admin123': $2a$10$4bnThA4xQVw1rF2POQv78uAQll2KsUsgF32JaYiVG5d2d3Fhgk83q
-- password hash for 'staff123': $2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG
INSERT INTO Accounts (account_id, username, password_hash, is_active, role_id, created_at) VALUES 
(1, 'admin', '$2a$10$4bnThA4xQVw1rF2POQv78uAQll2KsUsgF32JaYiVG5d2d3Fhgk83q', TRUE, 1, CURRENT_TIMESTAMP),
(2, 'tphuong', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 3, CURRENT_TIMESTAMP),
(3, 'nmquan', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 3, CURRENT_TIMESTAMP),
(4, 'lelinh', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 2, CURRENT_TIMESTAMP),
(5, 'hoangnam', '$2a$10$4bnThA4xQVw1rF2POQv78uAQll2KsUsgF32JaYiVG5d2d3Fhgk83q', TRUE, 9, CURRENT_TIMESTAMP),
(6, 'vanan', '$2a$10$4bnThA4xQVw1rF2POQv78uAQll2KsUsgF32JaYiVG5d2d3Fhgk83q', TRUE, 9, CURRENT_TIMESTAMP),
(7, 'phamtuan', '$2a$10$4bnThA4xQVw1rF2POQv78uAQll2KsUsgF32JaYiVG5d2d3Fhgk83q', TRUE, 8, CURRENT_TIMESTAMP),
(8, 'thibich', '$2a$10$4bnThA4xQVw1rF2POQv78uAQll2KsUsgF32JaYiVG5d2d3Fhgk83q', TRUE, 9, CURRENT_TIMESTAMP),
(9, 'ngocthi', '$2a$10$4bnThA4xQVw1rF2POQv78uAQll2KsUsgF32JaYiVG5d2d3Fhgk83q', TRUE, 9, CURRENT_TIMESTAMP),
(10, 'mylinh', '$2a$10$4bnThA4xQVw1rF2POQv78uAQll2KsUsgF32JaYiVG5d2d3Fhgk83q', TRUE, 9, CURRENT_TIMESTAMP),
(11, 'hoanganh', '$2a$10$4bnThA4xQVw1rF2POQv78uAQll2KsUsgF32JaYiVG5d2d3Fhgk83q', TRUE, 8, CURRENT_TIMESTAMP),
(12, 'vuhung', '$2a$10$4bnThA4xQVw1rF2POQv78uAQll2KsUsgF32JaYiVG5d2d3Fhgk83q', TRUE, 8, CURRENT_TIMESTAMP),
(13, 'NguynNgoc', '$2a$10$4bnThA4xQVw1rF2POQv78uAQll2KsUsgF32JaYiVG5d2d3Fhgk83q', TRUE, 8, CURRENT_TIMESTAMP),
(14, 'guide2', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 7, CURRENT_TIMESTAMP),
(15, 'guide3', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 7, CURRENT_TIMESTAMP),
(16, 'housekeep1', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 5, CURRENT_TIMESTAMP),
(17, 'housekeep2', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 5, CURRENT_TIMESTAMP),
(18, 'pos1', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 4, CURRENT_TIMESTAMP),
(19, 'manager1', '$2a$10$4bnThA4xQVw1rF2POQv78uAQll2KsUsgF32JaYiVG5d2d3Fhgk83q', TRUE, 7, CURRENT_TIMESTAMP),
(20, 'maintain1', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 6, CURRENT_TIMESTAMP);

-- ── 3. Employees (10 rows) ───────────────────────────────────
INSERT INTO Employees (employee_id, account_id, full_name, gender, cccd, phone, email, salary) VALUES 
(1, 1, 'Nguyễn Quản Trị', 'Nam', '001234567890', '0912000001', 'admin@hoanien.vn', 15000000),
(2, 2, 'Trần Phương', 'Nữ', '001234567891', '0912000002', 'tphuong@hoanien.vn', 10000000),
(3, 3, 'Nguyễn Minh Quân', 'Nam', '001234567892', '0912000003', 'nmquan@hoanien.vn', 10000000),
(4, 4, 'Lê Linh', 'Nữ', '001234567893', '0912000004', 'lelinh@hoanien.vn', 9000000),
(5, 13, 'NguynNgoc', 'Nam', '001234567894', '0912000005', 'guide@hoanien.vn', 8000000),
(6, 14, 'Ngọc Lan', 'Nữ', '001234567895', '0912000006', 'guide2@hoanien.vn', 8500000),
(7, 15, 'Hoàng Nam', 'Nam', '001234567896', '0912000007', 'guide3@hoanien.vn', 8500000),
(8, 16, 'Lê Văn Tám', 'Nam', '001234567897', '0912000008', 'lvtam@hoanien.vn', 7000000),
(9, 17, 'Nguyễn Thị Hoa', 'Nữ', '001234567898', '0912000009', 'nthoa@hoanien.vn', 7000000),
(10, 20, 'Hoàng Bảo Trì', 'Nam', '001234567899', '0912000010', 'hbtri@hoanien.vn', 7500000);

-- ── 3.5. Membership Tiers (4 rows) ───────────────────────────────────
INSERT INTO membership_tiers (tier_id, tier_name, points_from, points_to, credit_limit, description) VALUES
(1, 'Regular', 0, 999, 5000000.00, 'Hạng thẻ mặc định'),
(2, 'Silver', 1000, 4999, 10000000.00, 'Hạng Bạc'),
(3, 'Gold', 5000, 9999, 20000000.00, 'Hạng Vàng'),
(4, 'Platinum', 10000, 99999, 50000000.00, 'Hạng Bạch kim');

-- ── 4. Customers (15 rows) ───────────────────────────────────
INSERT INTO Customers (customer_id, account_id, full_name, gender, cccd_passport_encrypted, phone, email, loyalty_points, membership_tier_id) VALUES 
(1, 5, 'Nguyễn Xuân Lưu', 'Nam', 'CCCD_101', '090101', 'nam101@test.com', 100, 1),
(2, 6, 'Ngọc Thị', 'Nam', 'CCCD_204', '090204', 'an204@test.com', 200, 2),
(3, 7, 'Phạm Tuấn', 'Nam', 'CCCD_308', '090308', 'tuan308@test.com', 500, 3),
(4, 8, 'Trần Thị Bích', 'Nữ', 'CCCD_104', '090104', 'bich104@test.com', 50, 1),
(5, 9, 'Ngọc Thị', 'Nữ', 'CCCD_106', '090106', 'quang106@test.com', 0, 1),
(6, 10, 'Đỗ Mỹ Linh', 'Nữ', 'CCCD_207', '090207', 'linh207@test.com', 150, 2),
(7, 11, 'Hoàng Anh', 'Nam', 'CCCD_210', '090210', 'anh210@test.com', 300, 3),
(8, 12, 'Vũ Hùng', 'Nam', 'CCCD_311', '090311', 'hung311@test.com', 800, 4),
(9, NULL, 'Lưu Đình Đức', 'Nam', 'CCCD_DEMO1', '0909990001', 'duc@test.com', 0, 1),
(10, NULL, 'Nguyễn Minh Đức', 'Nam', 'CCCD_DEMO2', '0909990002', 'duc2@test.com', 0, 1),
(11, NULL, 'Trần Thị Mai', 'Nữ', 'CCCD_DEMO3', '0909990003', 'mai@test.com', 0, 1),
(12, NULL, 'Phạm Hùng Anh', 'Nam', 'CCCD_DEMO4', '0909990004', 'phanh@test.com', 0, 1),
(13, NULL, 'Nguyễn Thanh Sơn', 'Nam', 'CCCD_DEMO5', '0909990005', 'ntson@test.com', 0, 1),
(14, NULL, 'Vũ Thị Thảo', 'Nữ', 'CCCD_DEMO6', '0909990006', 'vtthao@test.com', 0, 1),
(15, NULL, 'Đoàn Minh Khang', 'Nam', 'CCCD_DEMO7', '0909990007', 'dmkhang@test.com', 0, 1);

-- ── 5. Dependents (10 rows) ──────────────────────────────────
INSERT INTO Dependents (dependent_id, customer_id, dependent_name, birth_date, gender, cccd_passport_encrypted) VALUES 
(1, 1, 'Lê Hoàng Minh', '2018-05-12', 'Nam', NULL),
(2, 1, 'Lê Thị Hồng', '2020-09-20', 'Nữ', NULL),
(3, 2, 'Nguyễn Văn Bình', '2015-03-10', 'Nam', NULL),
(4, 3, 'Phạm Tuấn Hải', '2016-07-15', 'Nam', NULL),
(5, 4, 'Trần An Nhiên', '2019-11-01', 'Nữ', NULL),
(6, 6, 'Nguyễn Mỹ Anh', '2017-02-14', 'Nữ', NULL),
(7, 7, 'Hoàng Minh Khôi', '2014-06-25', 'Nam', NULL),
(8, 8, 'Vũ Gia Bảo', '2013-08-30', 'Nam', NULL),
(9, 12, 'Phạm Ngọc Trâm', '2021-10-05', 'Nữ', NULL),
(10, 13, 'Nguyễn Thanh Hà', '2022-12-25', 'Nữ', NULL);

-- ── 6. Room Categories (10 rows) ─────────────────────────────
-- Columns: category_id, category_name, cover_img_url, base_price, capacity, description,
--          base_adults, base_children, max_adults, max_children, extra_adult_surcharge, extra_child_surcharge, is_active,
--          bed_type, room_size, view_type, has_bathtub, has_balcony, complimentary_services, has_free_breakfast
INSERT INTO Room_Categories (
    category_id, category_name, cover_img_url, base_price, capacity, description,
    base_adults, base_children, max_adults, max_children, extra_adult_surcharge, extra_child_surcharge, is_active,
    bed_type, room_size, view_type, has_bathtub, has_balcony, complimentary_services, has_free_breakfast
) VALUES 
(1,  'Nipa Pool Villa',          'https://images.unsplash.com/photo-1540541338287-41700207dee6', 2500000,  2, 'Villa thanh tịnh bên hồ sen thơm mát.',             2, 0, 3, 1, 500000,  250000, TRUE, '1 Giường King 2m2', 65, 'Hướng hồ bơi', TRUE, TRUE, '2 chai nước suối, Trái cây tươi, Vang đỏ', TRUE),
(2,  'River Pool Villa',         'https://images.unsplash.com/photo-1566073771259-6a8506099945', 3500000,  3, 'Villa cao cấp ven sông Thu Bồn lộng gió.',          2, 0, 3, 2, 600000,  300000, TRUE, '1 Giường King 2m2', 80, 'Hướng sông Thu Bồn', TRUE, TRUE, '4 chai nước suối, Trái cây, Trà chiều', TRUE),
(3,  'Wellness Retreats',        'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4', 8000000,  4, 'Hành trình tĩnh lặng, chăm sóc sức khoẻ toàn diện.', 2, 0, 4, 2, 1000000, 500000, TRUE, '2 Giường King', 120, 'Hướng vườn thiền', TRUE, TRUE, 'Nước detox, Trái cây Organic, Trà thảo mộc', TRUE),
(4,  'Garden View Suite',        'https://images.unsplash.com/photo-1578683010236-d716f9a3f461', 2000000,  2, 'Suite hướng vườn nhiệt đới xanh mướt.',             2, 0, 3, 1, 400000,  200000, TRUE, '1 Giường Queen 1m8', 45, 'Hướng vườn nhiệt đới', FALSE, TRUE, '2 chai nước suối, Trà & Cà phê', TRUE),
(5,  'Presidential Ocean Suite', 'https://images.unsplash.com/photo-1590490360182-c33d57733427', 15000000, 6, 'Hạng phòng cao cấp bậc nhất hướng biển.',           4, 0, 6, 3, 2000000, 1000000, TRUE, '3 Giường King 2m2', 250, 'Hướng biển toàn cảnh', TRUE, TRUE, 'Minibar miễn phí, Rượu Champagne, Bánh ngọt', TRUE),
(6,  'Ocean View Bungalow',      'https://images.unsplash.com/photo-1582719508461-905c673771fd', 3000000,  2, 'Bungalow bãi cát đón gió biển tươi mát.',           2, 0, 3, 1, 600000,  300000, TRUE, '1 Giường King 2m2', 50, 'Hướng biển', TRUE, TRUE, '2 chai nước suối, Trái cây tươi', TRUE),
(7,  'Family Connecting Room',   'https://images.unsplash.com/photo-1568495248636-6432b97bd949', 4500000,  5, 'Phòng thông nhau phù hợp cho cả gia đình.',         2, 2, 4, 4, 500000,  250000, TRUE, '1 Giường King & 2 Giường Đơn', 90, 'Hướng vườn', FALSE, TRUE, '4 chai nước suối, Bánh quy, Trà', TRUE),
(8,  'Superior Mountain View',   'https://images.unsplash.com/photo-1596394516093-501ba68a0ba6', 1800000,  2, 'Phòng hướng núi thanh tịnh bình yên.',              2, 0, 2, 1, 350000,  150000, TRUE, '2 Giường Đơn 1m2', 40, 'Hướng núi đồi', FALSE, FALSE, '2 chai nước suối, Trà & Cà phê', TRUE),
(9,  'Luxury Penthouse',         'https://images.unsplash.com/photo-1502672260266-1c1ef2d93688', 12000000, 4, 'Căn hộ tầng mái đẳng cấp ngắm toàn cảnh resort.',  2, 0, 4, 2, 1500000, 750000, TRUE, '2 Giường King siêu lớn', 180, 'Toàn cảnh Resort', TRUE, TRUE, 'Rượu vang cao cấp, Trái cây nhập khẩu, Minibar', TRUE),
(10, 'Cozy Studio Room',         'https://images.unsplash.com/photo-1522771739844-6a9f6d5f14af', 1500000,  2, 'Phòng Studio nhỏ gọn, đầy đủ tiện nghi.',          2, 0, 2, 1, 300000,  150000, TRUE, '1 Giường Queen 1m8', 35, 'Hướng đường phố', FALSE, FALSE, '2 chai nước suối, Cà phê hòa tan', FALSE);

-- ── 7. Room Surcharges (10 rows) ─────────────────────────────
INSERT INTO Room_Surcharges (surcharge_id, category_id, surcharge_type, age_from, age_to, price_modifier, is_active) VALUES 
(1, 1, 'EXTRA_ADULT_BED', 12, 100, 500000, TRUE),
(2, 1, 'CHILD_WITH_BED', 6, 11, 250000, TRUE),
(3, 1, 'CHILD_WITHOUT_BED', 0, 5, 0, TRUE),
(4, 2, 'EXTRA_ADULT_BED', 12, 100, 600000, TRUE),
(5, 2, 'CHILD_WITH_BED', 6, 11, 300000, TRUE),
(6, 3, 'EXTRA_ADULT_BED', 12, 100, 1000000, TRUE),
(7, 3, 'CHILD_WITH_BED', 6, 11, 500000, TRUE),
(8, 4, 'EXTRA_ADULT_BED', 12, 100, 400000, TRUE),
(9, 5, 'EXTRA_ADULT_BED', 12, 100, 2000000, TRUE),
(10, 6, 'EXTRA_ADULT_BED', 12, 100, 600000, TRUE);

-- ── 8. Rooms (50 rows) ───────────────────────────────────────
INSERT INTO Rooms (room_id, room_number, category_id, room_status, current_booking_detail_id) VALUES 
(1, '101', 1, 'Occupied', 1),
(2, '102', 1, 'Vacant_Clean', NULL),
(3, '103', 1, 'Occupied', 2),
(4, '104', 1, 'Vacant_Clean', NULL),
(5, '105', 1, 'Occupied', 3),
(6, '106', 4, 'Vacant_Clean', NULL),
(7, '107', 4, 'Vacant_Clean', NULL),
(8, '108', 4, 'Vacant_Clean', NULL),
(9, '109', 4, 'Vacant_Clean', NULL),
(10, '110', 4, 'Vacant_Clean', NULL),
(11, '201', 2, 'Vacant_Clean', NULL),
(12, '202', 2, 'Vacant_Clean', NULL),
(13, '203', 2, 'Vacant_Clean', NULL),
(14, '204', 2, 'Vacant_Clean', NULL),
(15, '205', 2, 'Occupied', 7),
(16, '206', 10, 'Vacant_Clean', NULL),
(17, '207', 10, 'Vacant_Clean', NULL),
(18, '208', 10, 'Occupied', 8),
(19, '209', 10, 'Vacant_Clean', NULL),
(20, '210', 10, 'Vacant_Clean', NULL),
(21, '301', 7, 'Vacant_Clean', NULL),
(22, '302', 7, 'Vacant_Clean', NULL),
(23, '303', 7, 'Vacant_Clean', NULL),
(24, '304', 7, 'Vacant_Clean', NULL),
(25, '305', 7, 'Vacant_Clean', NULL),
(26, '306', 8, 'Vacant_Clean', NULL),
(27, '307', 8, 'Vacant_Clean', NULL),
(28, '308', 8, 'Vacant_Clean', NULL),
(29, '309', 8, 'Vacant_Clean', NULL),
(30, '310', 8, 'Vacant_Clean', NULL),
(31, '401', 6, 'Vacant_Clean', NULL),
(32, '402', 6, 'Vacant_Clean', NULL),
(33, '403', 6, 'Vacant_Clean', NULL),
(34, '404', 6, 'Vacant_Clean', NULL),
(35, '405', 6, 'Vacant_Clean', NULL),
(36, '406', 3, 'Vacant_Clean', NULL),
(37, '407', 3, 'Vacant_Clean', NULL),
(38, '408', 3, 'Vacant_Clean', NULL),
(39, '409', 3, 'Vacant_Clean', NULL),
(40, '410', 3, 'Vacant_Clean', NULL),
(41, '501', 5, 'Vacant_Clean', NULL),
(42, '502', 5, 'Vacant_Clean', NULL),
(43, '503', 5, 'Vacant_Clean', NULL),
(44, '504', 5, 'Vacant_Clean', NULL),
(45, '505', 5, 'Vacant_Clean', NULL),
(46, '506', 9, 'Vacant_Clean', NULL),
(47, '507', 9, 'Vacant_Clean', NULL),
(48, '508', 9, 'Vacant_Clean', NULL),
(49, '509', 9, 'Vacant_Clean', NULL),
(50, '510', 9, 'Vacant_Clean', NULL);

-- ── 9. Dynamic Pricing (10 rows) ─────────────────────────────
INSERT INTO Dynamic_Pricing (price_id, category_id, start_date, end_date, price_modifier, reason) VALUES 
(1, 1, '2026-06-01', '2026-06-30', 200000, 'Mùa cao điểm hè'),
(2, 2, '2026-06-01', '2026-06-30', 300000, 'Mùa cao điểm hè'),
(3, 3, '2026-06-01', '2026-06-30', 500000, 'Mùa cao điểm hè'),
(4, 5, '2026-06-01', '2026-06-30', 1000000, 'Mùa cao điểm hè'),
(5, 9, '2026-06-01', '2026-06-30', 800000, 'Mùa cao điểm hè'),
(6, 1, '2026-09-01', '2026-09-30', -200000, 'Ưu đãi mùa thu'),
(7, 2, '2026-09-01', '2026-09-30', -300000, 'Ưu đãi mùa thu'),
(8, 3, '2026-09-01', '2026-09-30', -500000, 'Ưu đãi mùa thu'),
(9, 4, '2026-09-01', '2026-09-30', -150000, 'Ưu đãi mùa thu'),
(10, 6, '2026-09-01', '2026-09-30', -250000, 'Ưu đãi mùa thu');

-- ── 10. Daily Rates (10 rows) ────────────────────────────────
INSERT INTO Daily_Rates (daily_rate_id, category_id, rate_date, computed_price, is_weekend, is_holiday) VALUES 
(1, 1, '2026-06-13', 2700000, TRUE, FALSE),
(2, 1, '2026-06-14', 2700000, TRUE, FALSE),
(3, 2, '2026-06-13', 3800000, TRUE, FALSE),
(4, 2, '2026-06-14', 3800000, TRUE, FALSE),
(5, 3, '2026-06-13', 8500000, TRUE, FALSE),
(6, 3, '2026-06-14', 8500000, TRUE, FALSE),
(7, 4, '2026-06-13', 2200000, TRUE, FALSE),
(8, 5, '2026-06-13', 16000000, TRUE, FALSE),
(9, 6, '2026-06-13', 3300000, TRUE, FALSE),
(10, 7, '2026-06-13', 4800000, TRUE, FALSE);

-- ── 11. Promotions (10 rows) ─────────────────────────────────
INSERT INTO Promotions (promo_id, promo_code, discount_type, discount_value, valid_from, valid_to, max_uses, current_uses, is_active, description) VALUES 
(1, 'SUMMER2026', 'PERCENTAGE', 10.00, '2026-05-01 00:00:00', '2026-08-31 23:59:59', 1000, 15, TRUE, 'Giảm giá 10% cho toàn bộ dịch vụ đặt phòng hè.'),
(2, 'WELCOMETOHOANIEN', 'FIXED_AMOUNT', 200000.00, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 5000, 120, TRUE, 'Tặng ngay 200,000 VND cho khách đặt phòng lần đầu.'),
(3, 'VIPGOLD', 'PERCENTAGE', 15.00, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 9999, 45, TRUE, 'Ưu đãi đặc biệt giảm 15% cho thành viên Gold.'),
(4, 'MIDWEEK20', 'PERCENTAGE', 20.00, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 200, 10, TRUE, 'Giảm 20% đặt phòng từ thứ 2 đến thứ 5.'),
(5, 'AUTUMNRETREAT', 'PERCENTAGE', 12.00, '2026-09-01 00:00:00', '2026-11-30 23:59:59', 500, 0, TRUE, 'Giảm giá 12% chăm sóc sức khoẻ mùa thu.'),
(6, 'HONEYMOON', 'FIXED_AMOUNT', 500000.00, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 100, 2, TRUE, 'Gói trăng mật ngọt ngào giảm ngay 500k.'),
(7, 'FESTIVE15', 'PERCENTAGE', 15.00, '2026-12-20 00:00:00', '2027-01-05 23:59:59', 1000, 0, TRUE, 'Chào đón giáng sinh và năm mới.'),
(8, 'VOUCHER100K', 'FIXED_AMOUNT', 100000.00, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 10000, 300, TRUE, 'Voucher 100k cho khách hàng thân thiết.'),
(9, 'EARLYBIRD', 'PERCENTAGE', 8.00, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 2000, 50, TRUE, 'Đặt trước 30 ngày hưởng ngay ưu đãi 8%.');

-- ── 12. Bookings (20 rows) ───────────────────────────────────
INSERT INTO Bookings (booking_id, customer_id, booking_date, total_price, booking_status, booking_source, applied_promotion_id, version) VALUES
(1, 1, '2026-06-01', 5000000, 'Confirmed', 'Direct_Web', 1, 1),
(2, 2, '2026-06-02', 7000000, 'Confirmed', 'Direct_Web', NULL, 1),
(3, 3, '2026-06-03', 16000000, 'Confirmed', 'OTA', NULL, 1),
(4, 4, '2026-06-04', 5000000, 'Confirmed', 'OTA', 2, 1),
(5, 5, '2026-06-05', 5000000, 'Confirmed', 'Direct_Web', NULL, 1),
(6, 6, '2026-06-06', 7000000, 'Confirmed', 'OTA', NULL, 1),
(7, 7, '2026-06-07', 7000000, 'Confirmed', 'Direct_Web', 3, 1),
(8, 8, '2026-06-08', 16000000, 'Confirmed', 'Direct_Web', NULL, 1),
(9, 1, '2026-06-10', 1200000, 'Confirmed', 'Direct_Web', NULL, 1),
(10, 2, '2026-06-10', 1200000, 'Confirmed', 'Direct_Web', NULL, 1),
(11, 9, '2026-06-12', 1200000, 'Confirmed', 'Direct_Web', NULL, 1),
(12, 10, '2026-06-12', 1200000, 'Confirmed', 'Direct_Web', NULL, 1),
(13, 11, '2026-06-12', 1200000, 'Confirmed', 'Direct_Web', NULL, 1),
(14, 12, '2026-06-12', 2500000, 'Confirmed', 'Direct_Web', NULL, 1),
(15, 13, '2026-06-12', 3500000, 'Confirmed', 'Direct_Web', NULL, 1),
(16, 14, '2026-06-12', 8000000, 'Confirmed', 'Direct_Web', NULL, 1),
(17, 15, '2026-06-12', 2000000, 'Confirmed', 'Direct_Web', NULL, 1),
(18, 3, '2026-06-12', 15000000, 'Confirmed', 'Direct_Web', NULL, 1),
(19, 4, '2026-06-12', 3000000, 'Confirmed', 'Direct_Web', NULL, 1),
(20, 5, '2026-06-12', 4500000, 'Confirmed', 'Direct_Web', NULL, 1),
(24, 1, '2026-06-01', 3000000, 'Confirmed', 'Direct_Web', NULL, 1),
(25, 2, '2026-06-02', 1200000, 'Confirmed', 'Direct_Web', NULL, 1),
(26, 3, '2026-06-03', 5400000, 'Confirmed', 'Direct_Web', NULL, 1),
(27, 4, '2026-06-04', 2500000, 'Confirmed', 'OTA', NULL, 1),
(28, 5, '2026-06-05', 2400000, 'Confirmed', 'Direct_Web', NULL, 1);

-- ── 13. Room Bookings (10 rows) ──────────────────────────────
INSERT INTO Room_Bookings (room_booking_id, check_in_date, check_out_date, deposit_amount, cancellation_deadline, credit_limit, personal_pin_hash) VALUES
(1, '2026-06-09', '2026-06-12', 1000000, '2026-06-05', 5000000, 'hash'),
(2, '2026-06-09', '2026-06-12', 1000000, '2026-06-05', 5000000, 'hash'),
(3, '2026-06-09', '2026-06-12', 2000000, '2026-06-05', 10000000, 'hash'),
(4, '2026-06-09', '2026-06-11', 1000000, '2026-06-05', 5000000, 'hash'),
(5, '2026-06-10', '2026-06-13', 1000000, '2026-06-06', 5000000, 'hash'),
(6, '2026-06-10', '2026-06-14', 1500000, '2026-06-06', 5000000, 'hash'),
(7, '2026-06-10', '2026-06-15', 1500000, '2026-06-06', 5000000, 'hash'),
(8, '2026-06-10', '2026-06-16', 3000000, '2026-06-06', 15000000, 'hash'),
(14, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 4 DAY), 1000000, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 5000000, 'hash'),
(15, '2026-07-01', '2026-07-05', 1000000, '2026-06-25', 5000000, 'hash');

-- ── 14. Room Booking Details (10 rows) ───────────────────────
INSERT INTO Room_Booking_Details (detail_id, room_booking_id, category_id, room_id, room_charge, detail_status, bed_preference, special_requests, is_charge_to_room_allowed, sub_credit_limit, billing_routing_strategy, number_of_adults, number_of_children) VALUES
(1, 1, 1, 1, 2500000, 'Checked_In', 'KING_SIZE', NULL, TRUE, 500000, 'BILL_TO_LEADER'),
(2, 2, 2, 3, 3500000, 'Checked_In', 'KING_SIZE', NULL, TRUE, 1500000, 'BILL_TO_LEADER'),
(3, 3, 3, 5, 8000000, 'Checked_In', 'KING_SIZE', NULL, TRUE, 2000000, 'BILL_TO_LEADER'),
(4, 4, 1, 7, 2500000, 'Pending', 'KING_SIZE', NULL, TRUE, 500000, 'BILL_TO_LEADER'),
(5, 5, 1, 9, 2500000, 'Pending', 'KING_SIZE', NULL, TRUE, 500000, 'BILL_TO_LEADER'),
(6, 6, 2, 12, 3500000, 'Pending', 'KING_SIZE', NULL, TRUE, 1500000, 'BILL_TO_LEADER'),
(7, 7, 2, 15, 3500000, 'Checked_In', 'KING_SIZE', NULL, TRUE, 1500000, 'BILL_TO_LEADER'),
(8, 8, 3, 18, 8000000, 'Checked_In', 'KING_SIZE', NULL, TRUE, 3000000, 'BILL_TO_LEADER'),
(9, 14, 1, 2, 2500000, 'Pending', 'KING_SIZE', NULL, TRUE, 500000, 'BILL_TO_LEADER'),
(10, 15, 2, 4, 3500000, 'Pending', 'TWIN_BED', NULL, TRUE, 1500000, 'BILL_TO_LEADER');

-- ── 15. Room Guests (10 rows) ────────────────────────────────
INSERT INTO Room_Guests (guest_id, detail_id, customer_id, dependent_id, guest_type, is_primary_contact) VALUES
(1, 1, 1, NULL, 'ADULT', TRUE),
(2, 2, 2, NULL, 'ADULT', TRUE),
(3, 3, 3, NULL, 'ADULT', TRUE),
(4, 4, 4, NULL, 'ADULT', TRUE),
(5, 5, 5, NULL, 'ADULT', TRUE),
(6, 6, 6, NULL, 'ADULT', TRUE),
(7, 7, 7, NULL, 'ADULT', TRUE),
(8, 8, 8, NULL, 'ADULT', TRUE),
(9, 9, 12, NULL, 'ADULT', TRUE),
(10, 10, 13, NULL, 'ADULT', TRUE);

-- Force update tphuong to POS role in case DB already exists
UPDATE Accounts SET role_id = 4 WHERE username = 'tphuong';

-- Restore hoangnam password back to admin123 (was accidentally changed to staff123)
UPDATE Accounts SET password_hash = '$2a$10$4bnThA4xQVw1rF2POQv78uAQll2KsUsgF32JaYiVG5d2d3Fhgk83q' WHERE username = 'hoangnam';

-- Delete tmduc account and customer record
DELETE FROM Customers WHERE account_id = 98;
DELETE FROM Accounts WHERE username = 'tmduc';

-- Add ducbeo account if not exists (safe insert)
INSERT IGNORE INTO Accounts (account_id, username, password_hash, is_active, role_id, created_at)
VALUES (99, 'ducbeo', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 3, CURRENT_TIMESTAMP);


-- Force update tphuong to POS role in case DB already exists
UPDATE Accounts SET role_id = 4 WHERE username = 'tphuong';

-- Restore hoangnam password back to admin123 (was accidentally changed to staff123)
UPDATE Accounts SET password_hash = '$2a$10$4bnThA4xQVw1rF2POQv78uAQll2KsUsgF32JaYiVG5d2d3Fhgk83q' WHERE username = 'hoangnam';

-- Delete tmduc account and customer record
DELETE FROM Customers WHERE account_id = 98;
DELETE FROM Accounts WHERE username = 'tmduc';

-- Add ducbeo account if not exists (safe insert)
INSERT IGNORE INTO Accounts (account_id, username, password_hash, is_active, role_id, created_at)
VALUES (99, 'ducbeo', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 3, CURRENT_TIMESTAMP);


-- Update Room's current booking detail links
UPDATE Rooms SET current_booking_detail_id = 1 WHERE room_id = 1;
UPDATE Rooms SET current_booking_detail_id = 2 WHERE room_id = 3;
UPDATE Rooms SET current_booking_detail_id = 3 WHERE room_id = 5;
UPDATE Rooms SET current_booking_detail_id = 4 WHERE room_id = 7;
UPDATE Rooms SET current_booking_detail_id = 5 WHERE room_id = 9;
UPDATE Rooms SET current_booking_detail_id = 6 WHERE room_id = 12;
UPDATE Rooms SET current_booking_detail_id = 7 WHERE room_id = 15;
UPDATE Rooms SET current_booking_detail_id = 8 WHERE room_id = 18;
UPDATE Rooms SET current_booking_detail_id = 9 WHERE room_id = 2;
UPDATE Rooms SET current_booking_detail_id = 10 WHERE room_id = 4;

-- ── 16. Restaurant Tables (20 rows) ──────────────────────────
INSERT INTO Restaurant_Tables (table_id, table_number, capacity, table_status, is_active) VALUES 
(1, 'T01', 4, 'Occupied', TRUE),
(2, 'T02', 2, 'Available', TRUE),
(3, 'T03', 6, 'Occupied', TRUE),
(4, 'T04', 4, 'Available', TRUE),
(5, 'T05', 8, 'Available', TRUE),
(6, 'T06', 4, 'Available', TRUE),
(7, 'T07', 2, 'Occupied', TRUE),
(8, 'T08', 4, 'Available', TRUE),
(9, 'T09', 10, 'Available', TRUE),
(10, 'T10', 2, 'Available', TRUE),
(11, 'T11', 4, 'Occupied', TRUE),
(12, 'T12', 6, 'Available', TRUE),
(13, 'T13', 4, 'Available', TRUE),
(14, 'T14', 8, 'Available', TRUE),
(15, 'T15', 2, 'Occupied', TRUE),
(16, 'T16', 4, 'Available', TRUE),
(17, 'T17', 4, 'Available', TRUE),
(18, 'T18', 6, 'Occupied', TRUE),
(19, 'T19', 2, 'Available', TRUE),
(20, 'T20', 12, 'Available', TRUE);


-- ── 17. Table Reservations (10 rows) ─────────────────────────
INSERT INTO Table_Reservations (reservation_id, customer_id, table_id, reserve_date, reserve_time, end_time, deposit_amount, status) VALUES 
(1, 1, 1, '2026-06-13', '18:30:00', '20:30:00', 100000, 'Confirmed'),
(2, 2, 3, '2026-06-13', '19:00:00', '21:00:00', 100000, 'Confirmed'),
(3, 3, 4, '2026-06-13', '19:30:00', '21:30:00', 200000, 'Confirmed'),
(4, 4, 8, '2026-06-14', '18:00:00', '20:00:00', 100000, 'Pending'),
(5, 5, 14, '2026-06-14', '19:00:00', '21:00:00', 200000, 'Pending'),
(6, 6, 20, '2026-06-14', '20:00:00', '22:00:00', 300000, 'Confirmed'),
(7, 7, 7, '2026-06-13', '21:00:00', '23:00:00', 100000, 'Confirmed'),
(8, 8, 15, '2026-06-13', '20:30:00', '22:30:00', 100000, 'Confirmed'),
(9, 12, 11, '2026-06-13', '19:00:00', '21:00:00', 100000, 'Confirmed'),
(10, 13, 18, '2026-06-13', '18:00:00', '20:00:00', 150000, 'Confirmed'),
(11, 14, 1, '2026-06-20', '18:00:00', '20:00:00', 100000, 'Confirmed'),
(12, 15, 2, '2026-06-20', '19:00:00', '21:00:00', 150000, 'Confirmed'),
(13, 1, 3, '2026-06-20', '12:00:00', '14:00:00', 100000, 'Confirmed'),
(14, 2, 4, '2026-06-20', '20:00:00', '22:30:00', 200000, 'Pending'),
(15, 3, 5, '2026-06-21', '08:00:00', '10:00:00', 150000, 'Confirmed'),
(16, 4, 6, '2026-06-21', '11:30:00', '13:30:00', 100000, 'Confirmed'),
(17, 5, 7, '2026-06-21', '19:00:00', '21:00:00', 200000, 'Confirmed'),
(18, 6, 8, '2026-06-22', '18:30:00', '20:30:00', 100000, 'Pending'),
(19, 7, 9, '2026-06-22', '20:00:00', '22:00:00', 300000, 'Confirmed'),
(20, 8, 10, '2026-06-23', '09:00:00', '11:00:00', 100000, 'Confirmed'),
(21, 9, 11, '2026-06-23', '12:00:00', '14:00:00', 150000, 'Confirmed'),
(22, 10, 12, '2026-06-24', '18:00:00', '21:00:00', 200000, 'Pending'),
(23, 11, 13, '2026-06-24', '19:30:00', '21:30:00', 100000, 'Confirmed'),
(24, 12, 14, '2026-06-25', '11:00:00', '13:00:00', 300000, 'Confirmed'),
(25, 13, 15, '2026-06-25', '20:00:00', '22:00:00', 100000, 'Confirmed'),
(26, 14, 16, '2026-06-26', '18:00:00', '20:00:00', 100000, 'Confirmed'),
(27, 15, 17, '2026-06-26', '19:00:00', '21:00:00', 100000, 'Pending'),
(28, 1, 18, '2026-06-27', '08:30:00', '10:30:00', 150000, 'Confirmed'),
(29, 2, 19, '2026-06-27', '12:30:00', '14:30:00', 100000, 'Confirmed'),
(30, 3, 20, '2026-06-28', '19:00:00', '21:00:00', 400000, 'Confirmed'),
(31, 4, 1, '2026-06-28', '20:00:00', '22:00:00', 100000, 'Pending'),
(32, 5, 2, '2026-06-29', '18:00:00', '20:30:00', 100000, 'Confirmed'),
(33, 6, 3, '2026-06-29', '19:00:00', '21:00:00', 100000, 'Confirmed'),
(34, 7, 4, '2026-06-30', '11:00:00', '13:00:00', 200000, 'Confirmed'),
(35, 8, 5, '2026-06-30', '18:30:00', '20:30:00', 150000, 'Confirmed'),
(36, 9, 6, '2026-07-01', '12:00:00', '14:00:00', 100000, 'Pending'),
(37, 10, 7, '2026-07-01', '18:00:00', '20:00:00', 100000, 'Confirmed'),
(38, 11, 8, '2026-07-01', '19:00:00', '21:00:00', 100000, 'Confirmed');

-- ── 18. Menu Items (34 rows) ─────────────────────────────────
INSERT INTO Menu_Items (item_id, item_name, price, category, is_available, description, image_url, allergy_tags, is_always_available) VALUES 
(1, 'Súp Bí Đỏ Kem Tươi Truffle', 180000, 'Khai vị', TRUE, 'Súp bí đỏ béo ngậy kết hợp kem tươi và dầu truffle nguyên chất.', 'https://images.unsplash.com/photo-1547592166-23ac45744acd?w=500&q=80', 'Sữa', FALSE),
(2, 'Gỏi Cuốn Tôm Thịt', 95000, 'Khai vị', TRUE, 'Gỏi cuốn tôm thịt tươi ngon kèm rau sống và tương đậu phộng.', 'https://www.cet.edu.vn/wp-content/uploads/2018/11/goi-cuon-tom-thit.jpg', 'Đậu phộng, Hải sản', FALSE),
(3, 'Chả Giò Hải Sản', 110000, 'Khai vị', TRUE, 'Chả giò chiên giòn nhân hải sản tươi sống.', 'https://cdn.tgdd.vn/2022/01/CookDish/2-cach-lam-cha-gio-hai-san-don-gian-gion-thom-beo-ngay-ai-avt-1200x676.jpg', 'Hải sản', FALSE),
(4, 'Salad Cá Hồi Xông Khói', 150000, 'Khai vị', TRUE, 'Salad rau xanh tươi mát kết hợp cá hồi xông khói nhập khẩu.', 'https://file.hstatic.net/200000356095/file/salad_ca_hoi__xong_khoi__3__59eaf296ddc644849699579b210c7855.jpg', 'Hải sản', FALSE),
(5, 'Súp Hải Sản Măng Tây', 130000, 'Khai vị', TRUE, 'Súp hải sản nấu cùng măng tây tươi ngon ngọt.', 'https://cdn.tgdd.vn/Files/2020/10/09/1297483/tro-tai-voi-mon-sup-tom-mang-tay-vi-lau-thai-vua-la-vua-quen-ai-an-cung-tam-tac-khen-202010091742198445.jpg', 'Hải sản', FALSE),
(6, 'Bánh Mì Bơ Tỏi', 65000, 'Khai vị', TRUE, 'Bánh mì Pháp nướng giòn phết bơ tỏi thơm lừng.', 'https://www.lorca.vn/wp-content/uploads/2021/10/Cach-lam-mong-banh-mi-bo-toi-phomai-bang-lo-nuong.jpg', 'Gluten, Sữa', FALSE),
(7, 'Nem Chua Rán', 75000, 'Khai vị', TRUE, 'Nem chua rán giòn rụm chấm tương ớt.', 'https://trumfood.vn/wp-content/uploads/2022/09/trumfood_decor00865.jpg', NULL, FALSE),
(8, 'Hoành Thánh Chiên Giòn', 85000, 'Khai vị', TRUE, 'Hoành thánh chiên giòn nhân tôm thịt.', 'https://cdn.tgdd.vn/2020/09/CookProduct/Untitled-2-1200x676-1.jpg', 'Gluten, Hải sản', FALSE),
(9, 'Bò Bít Tết Wagyu Kèm Sốt Tiêu Xanh', 850000, 'Món chính', TRUE, 'Bò Wagyu Nhật Bản áp chảo sốt tiêu xanh, kèm rau củ nướng.', 'https://live.staticflickr.com/65535/50489573886_fa160b7292_b.jpg', NULL, FALSE),
(10, 'Cá Hồi Nướng Sốt Miso Nhật Bản', 520000, 'Món chính', TRUE, 'Cá hồi tươi nướng sốt miso thanh nhẹ, ăn kèm cơm trắng.', 'https://www.theforkbite.com/wp-content/uploads/2024/02/Teriyaki-Salmon-featured-2.9.24-500x500.jpg', 'Hải sản', FALSE),
(11, 'Phở Bò Truyền Thống', 120000, 'Món chính', TRUE, 'Phở bò nước dùng đậm đà, thịt bò tái chín mềm.', 'https://daotaobeptruong.vn/wp-content/uploads/2020/03/cach-nau-pho-bo.jpg', NULL, FALSE),
(12, 'Cơm Chiên Dương Châu', 85000, 'Món chính', TRUE, 'Cơm chiên dương châu thập cẩm tôm, lạp xưởng, trứng.', 'https://images.unsplash.com/photo-1603133872878-684f208fb84b?w=500&q=80', 'Hải sản, Trứng', FALSE),
(13, 'Sườn Heo Nướng BBQ', 250000, 'Món chính', TRUE, 'Sườn heo non nướng sốt BBQ đậm vị, ăn kèm khoai tây chiên.', 'https://images.unsplash.com/photo-1544025162-d76694265947?w=500&q=80', NULL, FALSE),
(14, 'Mì Ý Sốt Bò Băm', 140000, 'Món chính', TRUE, 'Mì Ý spaghetti sốt bolognese thịt bò băm.', 'https://meoeva.com/wp-content/uploads/2019/05/Spaghetti.jpg', 'Gluten', FALSE),
(15, 'Gà Nướng Mật Ong', 180000, 'Món chính', TRUE, 'Đùi gà nướng mật ong thơm ngọt, ăn kèm salad.', 'https://lh3.googleusercontent.com/p/AF1QipOYKsd6yLm0iH-NJGsbyVcSl6woaZT1DpHGG1LM=s680-w680-h510', NULL, FALSE),
(16, 'Lẩu Thái Hải Sản', 350000, 'Món chính', TRUE, 'Lẩu Thái chua cay hải sản tươi sống, kèm bún tươi.', 'https://i.ytimg.com/vi/p1ejp7z4mc4/sddefault.jpg', 'Hải sản', FALSE),
(17, 'Bún Chả Hà Nội', 95000, 'Món chính', TRUE, 'Bún chả Hà Nội thịt nướng than hồng, nước mắm chua ngọt.', 'https://www.seriouseats.com/thmb/J0g7JWjk9r6CHESo1CIrD1BfGd0=/1500x0/filters:no_upscale():max_bytes(150000):strip_icc()/20231204-SEA-VyTran-BunChaHanoi-19-f623913c6ef34a9185bcd6e5680c545f.jpg', NULL, FALSE),
(18, 'Pizza Hải Sản', 210000, 'Món chính', TRUE, 'Pizza đế mỏng nhân hải sản phô mai mozzarella.', 'https://doiduavang.vn/wp-content/uploads/2021/01/pizza-nhan-hai-san-doi-dua-vang-scaled.jpg', 'Gluten, Hải sản, Sữa', FALSE),
(19, 'Cơm Gà Hải Nam', 110000, 'Món chính', TRUE, 'Cơm gà Hải Nam nước dùng gà thanh ngọt.', 'https://cdn.tgdd.vn/Files/2021/08/16/1375575/cach-nau-com-ga-hai-nam-don-gian-ga-chin-vang-uom-da-gion-dung-chuan-202112281045139511.jpg', NULL, FALSE),
(20, 'Mực Ống Nhồi Thịt', 170000, 'Món chính', TRUE, 'Mực ống nhồi thịt chiên giòn, chấm sốt tương xoài.', 'https://cdn.tgdd.vn/2021/03/CookProduct/1200-1200x676-31.jpg', 'Hải sản', FALSE),
(21, 'Bánh Tiramisu Truyền Thống Ý', 120000, 'Tráng miệng', FALSE, 'Bánh Tiramisu Ý nguyên bản vị cà phê, kem mascarpone.', 'https://thermomixvietnam.vn/wp-content/uploads/2021/08/tiramisu-truyen-thong.jpg', 'Sữa, Gluten', FALSE),
(22, 'Chè Xoài Dừa Tươi', 65000, 'Tráng miệng', TRUE, 'Chè xoài chín ngọt kết hợp nước cốt dừa béo ngậy.', 'https://img.freepik.com/premium-photo/mango-cheese-milka-dessert-made-from-jelly-nata-de-coco-basil-seed-mango-cream-cheese-milk_583400-4287.jpg', NULL, FALSE),
(23, 'Kem Xôi Dừa', 55000, 'Tráng miệng', TRUE, 'Kem xôi dừa mát lạnh, topping dừa nạo sấy.', 'https://beptruong.edu.vn/wp-content/uploads/2016/02/kem-xoi-dua.jpg', 'Sữa', FALSE),
(24, 'Bánh Flan Caramel', 45000, 'Tráng miệng', TRUE, 'Bánh flan caramel mềm mịn, thơm ngon.', 'https://img.freepik.com/premium-photo/cream-caramel-pudding_599862-23796.jpg', 'Trứng, Sữa', FALSE),
(25, 'Panna Cotta Dâu Tây', 75000, 'Tráng miệng', TRUE, 'Panna cotta Ý sốt dâu tây tươi mát.', 'https://bloganchoi.com/wp-content/uploads/2022/06/cach-lam-panna-cotta.jpg', 'Sữa', FALSE),
(26, 'Trái Cây Thập Cẩm', 110000, 'Tráng miệng', TRUE, 'Đĩa trái cây tươi thập cẩm theo mùa.', 'https://bolcereales.com.ar/wp-content/uploads/2021/01/alimentos-con-cobre-frutas.jpeg', NULL, FALSE),
(27, 'Bánh Mousse Chocolate', 90000, 'Tráng miệng', TRUE, 'Bánh mousse chocolate Bỉ mịn màng, đắng nhẹ.', 'https://i.ytimg.com/vi/pESVrDm6yIM/maxresdefault.jpg', 'Sữa, Trứng', FALSE),
(28, 'Nước Cam Tươi Ép Lạnh', 95000, 'Đồ uống', TRUE, 'Nước cam tươi nguyên chất ép lạnh.', 'https://www.sieuthidonglanh.com/wp-content/uploads/2023/03/Nuoc-ep-cam-giup-chong-lao-hoa-da-hieu-qua.png', NULL, TRUE),
(29, 'Cà Phê Phin Việt Nam', 55000, 'Đồ uống', TRUE, 'Cà phê phin Việt Nam đậm đà truyền thống.', 'https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=500&q=80', NULL, TRUE),
(30, 'Trà Đào Cam Sả', 65000, 'Đồ uống', TRUE, 'Trà đào cam sả tươi mát giải nhiệt.', 'https://img.meta.com.vn/Data/image/2021/05/20/tra-dao-cam-sa-2.jpg', NULL, TRUE),
(31, 'Sinh Tố Bơ', 75000, 'Đồ uống', TRUE, 'Sinh tố bơ tươi xay nhuyễn béo ngậy.', 'https://img.freepik.com/premium-photo/avocado-smoothie-with-avocado-wooden-board-dark-background_490636-2675.jpg', 'Sữa', TRUE),
(32, 'Mojito Chanh Bạc Hà', 85000, 'Đồ uống', TRUE, 'Mojito không cồn chanh bạc hà tươi mát.', 'https://img.freepik.com/premium-photo/refreshing-glass-coconut-water-garnished-with-mint-leaves-slice-lime_198067-290311.jpg', NULL, TRUE),
(33, 'Bia Heineken', 45000, 'Đồ uống', TRUE, 'Bia Heineken nhập khẩu lạnh.', 'https://tse2.mm.bing.net/th/id/OIP.h3ptwUpaWsyCB7MAVaLGhwHaEK?w=680&h=382&rs=1&pid=ImgDetMain&o=7&rm=3', 'Gluten', TRUE),
(34, 'Nước Khoáng Evian', 40000, 'Đồ uống', TRUE, 'Nước khoáng Evian Pháp.', 'https://gangnamkong.co.kr/web/upload/NNEditor/20230424/33378a1a3af8ff1869c4b1faf8da8863.jpg', NULL, TRUE);

-- ── 18.5 Menu Item Days (Phân bổ thực đơn theo ngày) ───────────
INSERT INTO Menu_Item_Days (item_id, day_of_week) VALUES
-- MONDAY (Thứ 2)
(1, 'MONDAY'), (2, 'MONDAY'), (5, 'MONDAY'), (8, 'MONDAY'),
(9, 'MONDAY'), (10, 'MONDAY'), (13, 'MONDAY'), (16, 'MONDAY'),
(21, 'MONDAY'), (22, 'MONDAY'), (25, 'MONDAY'), (27, 'MONDAY'),

-- TUESDAY (Thứ 3)
(3, 'TUESDAY'), (4, 'TUESDAY'), (6, 'TUESDAY'), (7, 'TUESDAY'),
(11, 'TUESDAY'), (12, 'TUESDAY'), (14, 'TUESDAY'), (17, 'TUESDAY'),
(23, 'TUESDAY'), (24, 'TUESDAY'), (26, 'TUESDAY'), (22, 'TUESDAY'),

-- WEDNESDAY (Thứ 4)
(1, 'WEDNESDAY'), (3, 'WEDNESDAY'), (5, 'WEDNESDAY'), (8, 'WEDNESDAY'),
(15, 'WEDNESDAY'), (18, 'WEDNESDAY'), (19, 'WEDNESDAY'), (20, 'WEDNESDAY'),
(21, 'WEDNESDAY'), (23, 'WEDNESDAY'), (25, 'WEDNESDAY'), (27, 'WEDNESDAY'),

-- THURSDAY (Thứ 5)
(2, 'THURSDAY'), (4, 'THURSDAY'), (6, 'THURSDAY'), (7, 'THURSDAY'),
(9, 'THURSDAY'), (12, 'THURSDAY'), (16, 'THURSDAY'), (18, 'THURSDAY'),
(22, 'THURSDAY'), (24, 'THURSDAY'), (26, 'THURSDAY'), (21, 'THURSDAY'),

-- FRIDAY (Thứ 6)
(1, 'FRIDAY'), (2, 'FRIDAY'), (3, 'FRIDAY'), (4, 'FRIDAY'),
(10, 'FRIDAY'), (11, 'FRIDAY'), (14, 'FRIDAY'), (20, 'FRIDAY'),
(21, 'FRIDAY'), (23, 'FRIDAY'), (25, 'FRIDAY'), (27, 'FRIDAY'),

-- SATURDAY (Thứ 7)
(5, 'SATURDAY'), (6, 'SATURDAY'), (7, 'SATURDAY'), (8, 'SATURDAY'),
(9, 'SATURDAY'), (13, 'SATURDAY'), (16, 'SATURDAY'), (19, 'SATURDAY'),
(22, 'SATURDAY'), (24, 'SATURDAY'), (26, 'SATURDAY'), (25, 'SATURDAY'),

-- SUNDAY (Chủ Nhật)
(1, 'SUNDAY'), (2, 'SUNDAY'), (3, 'SUNDAY'), (4, 'SUNDAY'),
(10, 'SUNDAY'), (12, 'SUNDAY'), (15, 'SUNDAY'), (18, 'SUNDAY'),
(21, 'SUNDAY'), (23, 'SUNDAY'), (26, 'SUNDAY'), (27, 'SUNDAY');

-- ── 19. Food Orders (19 rows) ────────────────────────────────
INSERT INTO Food_Orders (order_id, booking_id, room_booking_detail_id, table_id, order_type, order_status, payment_type, is_paid_in_pos, created_by_staff_id, kitchen_processed_by_id) VALUES 
(1, NULL, NULL, 1, 'Dine-In', 'Completed', 'Pay at Counter', TRUE, 2, 3),
(2, NULL, NULL, NULL, 'Room Service', 'Pending', 'Post to Room', FALSE, 2, NULL),
(3, NULL, NULL, 3, 'Dine-In', 'Completed', 'Pay at Counter', TRUE, 2, 3),
(4, NULL, NULL, 2, 'Dine-In', 'Completed', 'Pay at Counter', TRUE, 2, NULL),
(5, NULL, NULL, 4, 'Dine-In', 'Completed', 'Post to Room', TRUE, 2, 3),
(6, NULL, NULL, 5, 'Dine-In', 'Completed', 'Pay at Counter', TRUE, 3, 2),
(7, NULL, NULL, NULL, 'Takeaway', 'Completed', 'Pay at Counter', TRUE, 2, 3),
(8, NULL, NULL, 7, 'Dine-In', 'Completed', 'Pay at Counter', TRUE, 3, 2),
(9, NULL, NULL, NULL, 'Room Service', 'Pending', 'Post to Room', FALSE, 2, NULL),
(10, NULL, NULL, 9, 'Dine-In', 'Completed', 'Pay at Counter', TRUE, 3, 2),
(11, NULL, NULL, 11, 'Dine-In', 'Completed', 'Pay at Counter', TRUE, 2, NULL),
(12, NULL, NULL, 12, 'Dine-In', 'Completed', 'Pay at Counter', TRUE, 3, 2),
(13, NULL, NULL, NULL, 'Room Service', 'Ready', 'Post to Room', FALSE, 2, 3),
(14, NULL, NULL, 14, 'Dine-In', 'Completed', 'Pay at Counter', TRUE, 2, 3),
(15, NULL, NULL, 15, 'Dine-In', 'Completed', 'Pay at Counter', TRUE, 3, 2),
(16, NULL, NULL, 16, 'Dine-In', 'Completed', 'Pay at Counter', TRUE, 2, NULL),
(17, NULL, NULL, NULL, 'Takeaway', 'Completed', 'Pay at Counter', TRUE, 3, 2),
(18, NULL, NULL, 18, 'Dine-In', 'Completed', 'Pay at Counter', TRUE, 2, 3),
(19, NULL, NULL, NULL, 'Room Service', 'Pending', 'Post to Room', FALSE, 2, NULL);

-- ── 20. Food Order Details (21 rows) ─────────────────────────
INSERT INTO Food_Order_Details (detail_id, order_id, menu_item_id, quantity, price_at_order, kot_status) VALUES 
(1, 1, 1, 2, 180000, 'Preparing'),
(2, 1, 9, 1, 850000, 'Preparing'),
(3, 3, 2, 4, 95000, 'Served'),
(4, 3, 16, 1, 350000, 'Served'),
(5, 2, 9, 2, 95000, 'Pending'),
(6, 4, 11, 2, 120000, 'Pending'),
(7, 5, 2, 1, 95000, 'Served'),
(8, 6, 16, 1, 350000, 'Ready'),
(9, 7, 28, 2, 95000, 'Served'),
(10, 8, 4, 1, 150000, 'Preparing'),
(11, 9, 10, 1, 520000, 'Pending'),
(12, 10, 12, 3, 85000, 'Served'),
(13, 11, 22, 2, 65000, 'Pending'),
(14, 12, 33, 5, 45000, 'Served'),
(15, 13, 30, 1, 65000, 'Ready'),
(16, 14, 15, 2, 180000, 'Preparing'),
(17, 15, 34, 4, 40000, 'Served'),
(18, 16, 5, 2, 130000, 'Pending'),
(19, 17, 18, 1, 210000, 'Served'),
(20, 18, 9, 2, 850000, 'Preparing'),
(21, 19, 1, 2, 180000, 'Pending');

-- ── 21. Hotel Services (10 rows) ─────────────────────────────
INSERT INTO Hotel_Services (service_id, service_name, base_price, source_department, is_available, description) VALUES 
(1, 'Đón tiễn sân bay bằng xe Limousine', 800000, 'TRANSPORTATION', TRUE, 'Xe Limousine 9 chỗ đón đưa sân bay sang trọng.'),
(3, 'Giặt sấy quần áo lấy nhanh', 150000, 'LAUNDRY', TRUE, 'Giặt hấp sấy khô quần áo giao trả trong 4 giờ.'),
(4, 'Decor phòng tân hôn lãng mạn', 500000, 'FLORIST', TRUE, 'Trang trí phòng bằng hoa tươi hồng đỏ và nến thơm nồng nàn.'),
(5, 'Thuê xe máy tay ga tự lái', 200000, 'TRANSPORTATION', TRUE, 'Thuê xe ga Honda Vision 110cc tự lái khám phá đảo ngọc.'),
(8, 'Giặt khô đồ vest/đầm dạ hội', 250000, 'LAUNDRY', TRUE, 'Giặt khô là hơi đồ vest và váy cưới cao cấp.'),
(9, 'Bó hoa tươi chúc mừng sinh nhật', 600000, 'FLORIST', TRUE, 'Bó hoa hướng dương kết hợp hoa hồng tươi rực rỡ.'),
(10, 'Thuê xe ô tô 7 chỗ kèm tài xế', 1500000, 'TRANSPORTATION', TRUE, 'Thuê xe Toyota Fortuner đi tham quan đảo trọn ngày.');

-- ── 22. Booking Services (10 rows) ───────────────────────────
INSERT INTO Booking_Services (booking_service_id, booking_id, service_id, quantity, unit_price, execution_date, status, specific_requests_json) VALUES 
(1, 1, 1, 1, 800000, '2026-06-09 14:00:00', 'COMPLETED', '{"flight_number": "VN117", "arrival_time": "13:30"}'),
(2, 2, 2, 2, 1200000, '2026-06-10 16:00:00', 'PENDING', NULL),
(3, 3, 4, 1, 500000, '2026-06-09 10:00:00', 'COMPLETED', '{"card_note": "Happy Anniversary"}'),
(4, 4, 3, 3, 150000, '2026-06-10 09:00:00', 'COMPLETED', NULL),
(5, 5, 5, 1, 200000, '2026-06-11 08:00:00', 'PENDING', NULL),
(6, 6, 6, 2, 300000, '2026-06-11 15:00:00', 'COMPLETED', NULL),
(7, 7, 7, 1, 450000, '2026-06-12 10:00:00', 'PENDING', NULL),
(8, 8, 8, 2, 250000, '2026-06-11 11:00:00', 'COMPLETED', NULL),
(9, 14, 1, 1, 800000, '2026-07-01 15:00:00', 'PENDING', '{"flight_number": "QH224"}'),
(10, 15, 9, 1, 600000, '2026-07-02 09:00:00', 'PENDING', '{"card_note": "Happy Birthday Leader Nam"}');

-- ── 23. Hotel Operations (10 rows) ───────────────────────────
INSERT INTO Hotel_Operations (task_id, room_id, staff_id, supervisor_id, operational_type, priority, status, created_at, started_at, completed_at, notes) VALUES 
-- HOUSEKEEPING (CHECKOUT_CLEAN)
(1, 2, 8, 4, 'CHECKOUT_CLEAN', 'High', 'Pending', '2026-06-28 08:00:00', NULL, NULL, '[Check-out] Khách phòng 102 vừa trả phòng, dọn gấp để đón đoàn 2h chiều.'),
(2, 4, 9, 4, 'CHECKOUT_CLEAN', 'Normal', 'Pending', '2026-06-28 09:00:00', NULL, NULL, '[Check-out] Dọn dẹp sạch sâu, thay toàn bộ ga giường và xịt thơm phòng.'),
(3, 8, 8, 4, 'CHECKOUT_CLEAN', 'Normal', 'InProgress', '2026-06-28 09:30:00', '2026-06-28 10:15:00', NULL, '[Stay-over] Khách yêu cầu thêm 2 khăn tắm và 1 chai nước suối.'),
(4, 13, 9, 4, 'CHECKOUT_CLEAN', 'High', 'InProgress', '2026-06-28 10:00:00', '2026-06-28 10:20:00', NULL, '[Arrival] Khách VIP sắp nhận phòng, chuẩn bị sẵn giỏ trái cây tươi trên bàn.'),
(5, 1, 9, 4, 'CHECKOUT_CLEAN', 'Normal', 'Completed', '2026-06-28 07:00:00', '2026-06-28 07:15:00', '2026-06-28 08:45:00', '[Check-out] Đã dọn xong, phát hiện quên một chiếc sạc điện thoại trên bàn.'),

-- MAINTENANCE (MAINTENANCE)
(6, 17, 10, 4, 'MAINTENANCE', 'High', 'Pending', '2026-06-28 10:30:00', NULL, NULL, 'Housekeeping báo: Điều hòa chảy nước ướt cả sàn gỗ, phòng 310.'),
(7, 3, 10, 4, 'MAINTENANCE', 'Normal', 'Pending', '2026-06-28 12:00:00', NULL, NULL, 'Khách phàn nàn: Vòi hoa sen bị nghẹt, nước chảy rất yếu.'),
(8, 5, 10, 4, 'MAINTENANCE', 'Normal', 'InProgress', '2026-06-28 13:00:00', '2026-06-28 13:15:00', NULL, 'Kiểm tra hệ thống đèn ban công, 1 bóng bị cháy.'),
(9, 10, 10, 4, 'MAINTENANCE', 'High', 'Paused', '2026-06-28 09:00:00', '2026-06-28 09:10:00', NULL, 'Sửa két sắt không mở được. \n[Tạm dừng]: Chờ mua pin mới loại 9V để thay mảng mạch.'),
(10, 16, 10, 4, 'MAINTENANCE', 'Low', 'Completed', '2026-06-28 08:00:00', '2026-06-28 08:05:00', '2026-06-28 08:20:00', 'Thay pin tay nắm cửa phòng 309. \n[Đã sửa]: Đã thay 4 cục pin AA Panasonic.');

-- ── 24. Folio Items (10 rows) ────────────────────────────────
INSERT INTO Folio_Items (folio_item_id, booking_id, room_booking_detail_id, payer_customer_id, source_department, amount, description, is_settled_separately, created_by_staff_id, created_at, signature_img_url) VALUES 
(2, 1, 1, 1, 'F&B', 180000, 'Súp Bí Đỏ Truffle Room Service', FALSE, 2, CURRENT_TIMESTAMP, NULL),
(3, 2, 2, 2, 'TRANSPORTATION', 800000, 'Xe đón tiễn Limousine sân bay', FALSE, 4, CURRENT_TIMESTAMP, NULL),
(5, 4, 4, 4, 'LAUNDRY', 150000, 'Giặt sấy quần áo lấy nhanh', FALSE, 4, CURRENT_TIMESTAMP, NULL),
(6, 5, 5, 5, 'TRANSPORTATION', 200000, 'Thuê xe máy Honda Vision tự lái', FALSE, 4, CURRENT_TIMESTAMP, NULL),
(7, 6, 6, 6, 'F&B', 350000, 'Lẩu Thái Hải Sản tại phòng', FALSE, 2, CURRENT_TIMESTAMP, NULL),
(8, 7, 7, 7, 'FLORIST', 500000, 'Trang trí phòng trăng mật', FALSE, 4, CURRENT_TIMESTAMP, NULL),
(10, 14, 9, 12, 'TRANSPORTATION', 800000, 'Đón tiễn sân bay Limousine', FALSE, 4, CURRENT_TIMESTAMP, NULL);

-- ── 25. Consolidated Invoices (10 rows) ──────────────────────
INSERT INTO Consolidated_Invoices (invoice_id, invoice_number, booking_id, subtotal_before_vat, vat_amount, total_amount, promo_id, invoice_status, created_at, issued_at) VALUES 
(1, 'INV-2026-0001', 1, 4500000, 450000, 4950000, 1, 'Settled', '2026-06-12 11:00:00', '2026-06-12 11:30:00'),
(2, 'INV-2026-0002', 2, 6300000, 630000, 6930000, NULL, 'Settled', '2026-06-12 12:00:00', '2026-06-12 12:15:00'),
(3, 'INV-2026-0003', 3, 14400000, 1440000, 15840000, NULL, 'Settled', '2026-06-12 13:00:00', '2026-06-12 13:10:00'),
(4, 'INV-2026-0004', 4, 4500000, 450000, 4950000, 2, 'Settled', '2026-06-11 10:00:00', '2026-06-11 10:30:00'),
(5, 'INV-2026-0005', 5, 4500000, 450000, 4950000, NULL, 'Settled', '2026-06-13 12:00:00', '2026-06-13 12:15:00'),
(6, 'INV-2026-0006', 6, 6300000, 630000, 6930000, NULL, 'Settled', '2026-06-14 11:00:00', '2026-06-14 11:20:00'),
(7, 'INV-2026-0007', 7, 6300000, 630000, 6930000, 3, 'Settled', '2026-06-15 10:00:00', '2026-06-15 10:15:00'),
(8, 'INV-2026-0008', 8, 14400000, 1440000, 15840000, NULL, 'Settled', '2026-06-16 11:00:00', '2026-06-16 11:30:00'),
(9, 'INV-2026-0009', 14, 2272727, 227273, 2500000, NULL, 'Draft', '2026-06-16 14:00:00', '2026-06-16 14:00:00'),
(10, 'INV-2026-0010', 15, 3181818, 318182, 3500000, NULL, 'Draft', '2026-06-16 14:00:00', '2026-06-16 14:00:00');

-- ── 26. Payment Transactions (10 rows) ───────────────────────
INSERT INTO Payment_Transactions (transaction_id, invoice_id, booking_id, amount, transaction_type, payment_method, gateway_status, transaction_ref, created_at) VALUES 
(1, 1, 1, 4950000, 'Checkout_Settlement', 'VNPAY', 'SUCCESS', 'TXN-0001-SUCCESS', '2026-06-12 11:30:00'),
(2, 2, 2, 6930000, 'Checkout_Settlement', 'VNPAY', 'SUCCESS', 'TXN-0002-SUCCESS', '2026-06-12 12:15:00'),
(3, 3, 3, 15840000, 'Checkout_Settlement', 'VNPAY', 'SUCCESS', 'TXN-0003-SUCCESS', '2026-06-12 13:10:00'),
(4, 4, 4, 4950000, 'Checkout_Settlement', 'BANK_TRANSFER', 'SUCCESS', 'TXN-0004-SUCCESS', '2026-06-11 10:30:00'),
(5, 5, 5, 4950000, 'Checkout_Settlement', 'VNPAY', 'SUCCESS', 'TXN-0005-SUCCESS', '2026-06-13 12:15:00'),
(6, 6, 6, 6930000, 'Checkout_Settlement', 'CASH', 'SUCCESS', 'TXN-0006-SUCCESS', '2026-06-14 11:20:00'),
(7, 7, 7, 6930000, 'Checkout_Settlement', 'VNPAY', 'SUCCESS', 'TXN-0007-SUCCESS', '2026-06-15 10:15:00'),
(8, 8, 8, 15840000, 'Checkout_Settlement', 'VNPAY', 'SUCCESS', 'TXN-0008-SUCCESS', '2026-06-16 11:30:00'),
(9, 9, 14, 1000000, 'Deposit', 'VNPAY', 'SUCCESS', 'TXN-0009-DEP', '2026-06-16 14:05:00'),
(10, 10, 15, 1000000, 'Deposit', 'VNPAY', 'SUCCESS', 'TXN-0010-DEP', '2026-06-16 14:06:00');

-- ── 27. Tours (10 rows) ──────────────────────────────────────
INSERT INTO Tours (tour_id, tour_name, tour_type, duration, base_price, max_capacity, short_quote, description, created_at, is_active) VALUES 
(1, 'Đoàn tụ - Huế', 'doantu', '7 Giờ', 50000, 20, 'Tìm về hơi ấm vẹn nguyên của lòng biết ơn và sự gắn kết.', 'Hành trình di sản cố đô Huế trải nghiệm văn hoá ẩm thực cung đình Huế.', CURRENT_TIMESTAMP, TRUE),
(2, 'Tinh túy đồng nội - Quảng Nam', 'dongnoi', '7 Giờ', 70000, 15, 'Lắng nghe nhịp điệu mộc mạc của đất mẹ và hồn quê xứ Quảng.', 'Tham quan phố cổ Hội An, làng rau Trà Quế và làng gốm Thanh Hà.', CURRENT_TIMESTAMP, TRUE),
(3, 'Di sản thủ công - Ninh Bình', 'disan', '7 Giờ', 50000, 15, 'Chạm vào hồn cốt của thời gian qua những tạo tác từ đôi bàn tay nghệ nhân.', 'Khám phá đầm Vân Long và làng nghề thêu ren truyền thống Văn Lâm.', CURRENT_TIMESTAMP, TRUE),
(4, 'Tĩnh lặng liên hoa - Tháp Mười', 'tinhlang', '8 Giờ', 70000, 10, 'Sự thanh lọc thuần khiết cho thân - tâm - trí giữa vùng sông nước mờ sương.', 'Đi xuồng ba lá ngắm sen nở rộ Đồng Tháp Mười thiền định thư thái.', CURRENT_TIMESTAMP, TRUE),
(5, 'Huyền thoại vịnh xanh - Hạ Long', 'Half-Day', '4 Giờ', 10000, 30, 'Du thuyền sang trọng ngắm kỳ quan thiên nhiên thế giới.', 'Lướt sóng vịnh Bắc Bộ ngắm động Thiên Cung hoành tráng.', CURRENT_TIMESTAMP, TRUE),
(6, 'Bình yên bản nhỏ - Sapa', 'Full-Day', '8 Giờ', 13000, 20, 'Gặp gỡ nụ cười hồn hậu vùng cao mây phủ.', 'Leo ruộng bậc thang bản Cát Cát trải nghiệm văn hoá đồng bào H’mông.', CURRENT_TIMESTAMP, TRUE),
(7, 'Nhịp đập hoang dã - Cát Tiên', 'Full-Day', '14 Giờ', 22000, 12, 'Lắng nghe tiếng gọi rừng xanh thẳm huyền bí.', 'Xem thú ban đêm rừng Nam Cát Tiên ngắm chim muông kì thú.', CURRENT_TIMESTAMP, TRUE),
(8, 'Bình minh cồn cát - Mũi Né', 'Half-Day', '5 Giờ', 9000, 15, 'Trượt cát đón mặt trời mọc rực rỡ.', 'Khám phá Đồi Cát Trắng, Đồi Cát Đỏ Mũi Né bằng xe địa hình ATV.', CURRENT_TIMESTAMP, TRUE),
(9, 'Sóng hát san hô - Phú Quốc', 'Half-Day', '6 Giờ', 14000, 25, 'Hoà mình vào làn nước xanh lục bảo óng ánh.', 'Lặn cano 4 đảo ngắm san hô thiên nhiên rực rỡ Phú Quốc.', CURRENT_TIMESTAMP, TRUE),
(10, 'Hương sắc miệt vườn - Cần Thơ', 'Half-Day', '5 Giờ', 8000, 20, 'Ngọt lịm trái chín trĩu cành miền Tây sông nước.', 'Đi chợ nổi Cái Răng thưởng thức bữa sáng trên ghe thuyền mộc mạc.', CURRENT_TIMESTAMP, TRUE);

-- ── 28. Tour Images (10 rows) ────────────────────────────────
INSERT INTO Tour_Images (image_id, tour_id, image_url, is_primary) VALUES 
(1, 1, 'https://images.unsplash.com/photo-1571003123894-1f0594d2b5d9', TRUE),
(2, 2, 'https://images.unsplash.com/photo-1555939594-58d7cb561ad1', TRUE),
(3, 3, 'https://images.unsplash.com/photo-1528127269322-539801943592', TRUE),
(4, 4, 'https://images.unsplash.com/photo-1504609773096-104ff2c73ba4', TRUE),
(5, 5, 'https://images.unsplash.com/photo-1528127269322-539801943592', TRUE),
(6, 6, 'https://images.unsplash.com/photo-1504609773096-104ff2c73ba4', TRUE),
(7, 7, 'https://images.unsplash.com/photo-1540541338287-41700207dee6', TRUE),
(8, 8, 'https://images.unsplash.com/photo-1566073771259-6a8506099945', TRUE),
(9, 9, 'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4', TRUE),
(10, 10, 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461', TRUE);

-- ── 29. Tour Locations (10 rows) ─────────────────────────────
INSERT INTO Tour_Locations (location_id, location_name, latitude, longitude, description, is_active) VALUES 
(1, 'Đại Nội Huế', 16.4678, 107.5789, 'Hoàng cung triều Nguyễn cổ kính, uy nghiêm.', TRUE),
(2, 'Phố cổ Hội An', 15.8801, 108.3380, 'Di sản văn hoá thế giới thương cảng đèn lồng rực rỡ.', TRUE),
(3, 'Đầm Vân Long', 20.3621, 105.9087, 'Khu bảo tồn thiên nhiên ngập nước lớn nhất vịnh Bắc Bộ.', TRUE),
(4, 'Đồng Tháp Mười', 10.4500, 105.7000, 'Xứ sở sen hồng ngào ngạt ngút ngàn tầm mắt.', TRUE),
(5, 'Đảo Ti Tốp Hạ Long', 20.8654, 107.0812, 'Bãi tắm cát trắng tuyệt đẹp tựa lưng vách đá.', TRUE),
(6, 'Bản Cát Cát Sapa', 22.3289, 103.8415, 'Bản làng mộc mạc bên thác nước mát rượi.', TRUE),
(7, 'Bàu Sấu Cát Tiên', 11.4589, 107.3654, 'Vùng đầm lầy bảo tồn cá sấu tự nhiên quý hiếm.', TRUE),
(8, 'Đồi Cát Trắng Mũi Né', 10.9500, 108.2800, 'Tiểu sa mạc cát mênh mông lấp lánh ánh vàng.', TRUE),
(9, 'Hòn Thơm Phú Quốc', 9.9543, 104.0152, 'Thiên đường đảo ngọc cát trắng nước trong vắt.', TRUE),
(10, 'Chợ nổi Cái Răng', 9.9989, 105.7489, 'Chợ đầu mối mua bán trái cây sầm uất trên sông.', TRUE);

-- ── 30. Tour Itineraries (10 rows) ───────────────────────────
INSERT INTO Tour_Itineraries (itinerary_id, tour_id, day_number, day_title, summary) VALUES 
(1, 1, 1, 'Hành trình Cố đô', 'Khám phá Đại Nội Huế và lăng tẩm hoàng cung triều Nguyễn.'),
(2, 2, 1, 'Hồn Quê Xứ Quảng', 'Tham quan Hội An cổ kính và trải nghiệm cày cấy làng Trà Quế.'),
(3, 3, 1, 'Hồn Đất Văn Lâm', 'Đi thuyền Vân Long ngắm cảnh sơn thuỷ và xem dệt thêu thủ công.'),
(4, 4, 1, 'Thiền Giữa Hương Sen', 'Ngắm sen nở mờ sương và tập yoga thiền trên sông nước.'),
(5, 5, 1, 'Vịnh Xanh Kì Vĩ', 'Tàu sang lướt sóng ngắm hang luồn, hòn trống mái.'),
(6, 6, 1, 'Sapa Mây Mù', 'Thăm bản H’mông cổ xưa, tìm hiểu nghệ thuật nhuộm chàm.'),
(7, 7, 1, 'Khám Phá Rừng Xanh', 'Đi bộ xuyên rừng ngắm bằng lăng cổ thụ kì vĩ.'),
(8, 8, 1, 'Cát Vàng Mũi Né', 'Ngắm bình minh cồn cát trắng, tham quan Suối Tiên.'),
(9, 9, 1, 'Đại Dương Phú Quốc', 'Lặn cano 4 đảo nhỏ hoang sơ hoà vào san hô rực rỡ.'),
(10, 10, 1, 'Sông Nước Cần Thơ', 'Ăn sáng hủ tiếu chợ nổi Cái Răng sôi động.');

-- ── 31. Tour Itinerary Details (10 rows) ─────────────────────
INSERT INTO Tour_Itinerary_Details (detail_id, itinerary_id, start_time, end_time, location_id, activity_title, activity_description, meal_type) VALUES 
(1, 1, '08:00:00', '11:30:00', 1, 'Tham quan Đại Nội', 'Chiêm ngưỡng Ngọ Môn, Điện Thái Hoà và nghe thuyết minh hoàng triều.', NULL),
(2, 2, '09:00:00', '12:00:00', 2, 'Dạo bước phố cổ', 'Thăm Chùa Cầu, hội quán Quảng Đông lấp lánh đèn lồng.', 'Lunch'),
(3, 3, '14:00:00', '17:00:00', 3, 'Du thuyền Vân Long', 'Đi thuyền nan ngắm đàn voọc quần đùi trắng chuyền cành vách đá.', NULL),
(4, 4, '06:00:00', '08:30:00', 4, 'Thiền hành đón nắng', 'Tập yoga tĩnh tâm trên nhà chòi gỗ giữa đầm sen lộng gió.', 'Breakfast'),
(5, 5, '08:30:00', '11:30:00', 5, 'Khám phá hang động', 'Tham quan động Thiên Cung hoành tráng thạch nhũ kì vĩ.', NULL),
(6, 6, '09:00:00', '12:00:00', 6, 'Thăm bản Cát Cát', 'Đi bộ ngắm cối xay nước khổng lồ và check-in thác Tiên Sa.', 'Lunch'),
(7, 7, '08:00:00', '15:00:00', 7, 'Trekking Bàu Sấu', 'Đi bộ 5km xuyên rừng rậm Nam Cát Tiên đến đầm lầy bảo tồn.', 'Lunch'),
(8, 8, '05:30:00', '08:30:00', 8, 'Đón bình minh cồn cát', 'Trượt cát đồi cát trắng bằng máng trượt và mô tô ATV chạy địa hình.', NULL),
(9, 9, '09:00:00', '15:00:00', 9, 'Lặn ngắm san hô', 'Tắm biển Hòn Thơm lặn biển ống thở ngắm rạn san hô tự nhiên đẹp nhất Phú Quốc.', 'Lunch'),
(10, 10, '06:00:00', '08:30:00', 10, 'Chợ nổi Cái Răng', 'Lên thuyền ngắm chợ nổi nhộn nhịp, ăn hủ tiếu nóng hổi chòng chành trên sông.', 'Breakfast');

-- ── 32. Tour Prices (10 rows) ────────────────────────────────
INSERT INTO Tour_Prices (tour_price_id, tour_id, age_from, age_to, ticket_price, combo_discount_price, is_active) VALUES 
(1, 1, 12, 100, 1500000, 1300000, TRUE),
(2, 1, 4, 11, 750000, 650000, TRUE),
(3, 2, 12, 100, 1200000, 1000000, TRUE),
(4, 2, 4, 11, 600000, 500000, TRUE),
(5, 3, 12, 100, 1800000, 1500000, TRUE),
(6, 4, 12, 100, 2500000, 2200000, TRUE),
(7, 5, 12, 100, 1000000, 850000, TRUE),
(8, 6, 12, 100, 1300000, 1100000, TRUE),
(9, 7, 12, 100, 2200000, 2000000, TRUE),
(10, 9, 12, 100, 1400000, 1200000, TRUE);

-- ── 33. Tour Schedules (10 rows) ─────────────────────────────
INSERT INTO Tour_Schedules (schedule_id, tour_id, departure_date, departure_time, booked_seats, schedule_status) VALUES 
(1, 1, '2026-06-14', '08:00:00', 0, 'Open'),
(2, 2, '2026-06-15', '14:00:00', 0, 'Open'),
(3, 3, '2026-06-15', '17:00:00', 0, 'Open'),
(4, 4, '2026-06-16', '09:00:00', 0, 'Open'),
(5, 2, '2026-06-13', '08:00:00', 5, 'Open'),
(6, 5, '2026-06-17', '08:00:00', 0, 'Open'),
(7, 6, '2026-06-18', '09:00:00', 0, 'Open'),
(8, 7, '2026-06-19', '07:30:00', 0, 'Open'),
(9, 8, '2026-06-20', '05:00:00', 0, 'Open'),
(10, 9, '2026-06-21', '09:00:00', 0, 'Open');

-- ── 34. Tour Staff Assignments (10 rows) ─────────────────────
INSERT INTO Tour_Staff_Assignments (assignment_id, schedule_id, employee_id, staff_role) VALUES 
(1, 5, 5, 'GUIDE'),
(2, 5, 6, 'DRIVER'),
(3, 1, 7, 'GUIDE'),
(4, 1, 8, 'DRIVER'),
(5, 2, 5, 'GUIDE'),
(6, 3, 6, 'GUIDE'),
(7, 4, 7, 'GUIDE'),
(8, 6, 5, 'GUIDE'),
(9, 7, 6, 'GUIDE'),
(10, 8, 7, 'GUIDE');

-- ── 35. Run Itinerary Status (10 rows) ───────────────────────
INSERT INTO Run_Itinerary_Status (run_status_id, schedule_id, detail_id, actual_start_time, actual_end_time, current_stage_status, guide_notes) VALUES 
(1, 5, 2, '2026-06-13 09:05:00', '2026-06-13 12:10:00', 'COMPLETED', 'Lượng xe cộ Hội An đông, đoàn di chuyển chậm 5 phút.'),
(2, 1, 1, NULL, NULL, 'NOT_STARTED', NULL),
(3, 2, 2, NULL, NULL, 'NOT_STARTED', NULL),
(4, 3, 3, NULL, NULL, 'NOT_STARTED', NULL),
(5, 4, 4, NULL, NULL, 'NOT_STARTED', NULL),
(6, 5, 2, NULL, NULL, 'NOT_STARTED', NULL),
(7, 6, 5, NULL, NULL, 'NOT_STARTED', NULL),
(8, 7, 6, NULL, NULL, 'NOT_STARTED', NULL),
(9, 8, 7, NULL, NULL, 'NOT_STARTED', NULL),
(10, 10, 10, NULL, NULL, 'NOT_STARTED', NULL);

-- ── 36. Tour Bookings (10 rows) ──────────────────────────────
INSERT INTO Tour_Bookings (booking_id, schedule_id, participant_count, tour_charge, is_walk_in_tour) VALUES 
(9, 5, 1, 1200000, FALSE),
(10, 5, 1, 1200000, FALSE),
(11, 5, 1, 1200000, FALSE),
(12, 5, 1, 1200000, FALSE),
(13, 5, 1, 1200000, FALSE),
(24, 1, 2, 3000000, FALSE),
(25, 2, 1, 1200000, FALSE),
(26, 3, 3, 5400000, FALSE),
(27, 4, 1, 2500000, FALSE),
(28, 5, 2, 2400000, FALSE);

-- ── 37. Tour Attendees (10 rows) ─────────────────────────────
INSERT INTO Tour_Attendees (attendee_id, tour_booking_id, customer_id, dependent_id, attendance_status, face_matched_at, face_vector_data) VALUES 
(1, 9, 1, NULL, 'Not_Show', NULL, NULL),
(2, 10, 2, NULL, 'Not_Show', NULL, NULL),
(3, 11, 9, NULL, 'Not_Show', NULL, NULL),
(4, 12, 10, NULL, 'Not_Show', NULL, NULL),
(5, 13, 11, NULL, 'Not_Show', NULL, NULL),
(6, 24, 1, NULL, 'Not_Show', NULL, NULL),
(7, 25, 2, NULL, 'Not_Show', NULL, NULL),
(8, 26, 3, NULL, 'Not_Show', NULL, NULL),
(9, 27, 4, NULL, 'Not_Show', NULL, NULL),
(10, 28, 5, NULL, 'Not_Show', NULL, NULL);

-- ── 38. Checkpoint Attendance (10 rows) ──────────────────────
INSERT INTO Checkpoint_Attendance (checkpoint_id, schedule_id, attendee_id, detail_id, scan_status, scanned_by_staff_id) VALUES 
(1, 5, 1, 2, 'SUCCESS', 5),
(2, 5, 2, 2, 'SUCCESS', 5),
(3, 5, 3, 2, 'SUCCESS', 5),
(4, 5, 4, 2, 'SUCCESS', 5),
(5, 5, 5, 2, 'SUCCESS', 5),
(6, 1, 6, 1, 'PENDING', 7),
(7, 2, 7, 2, 'PENDING', 5),
(8, 3, 8, 3, 'PENDING', 6),
(9, 4, 9, 4, 'PENDING', 7),
(10, 6, 10, 5, 'PENDING', 5);

-- ── 39. Reviews (10 rows) ────────────────────────────────────
INSERT INTO Reviews (review_id, customer_id, room_booking_detail_id, tour_booking_id, rating_service, review_text, moderation_status, moderated_by, moderation_reason, created_at) VALUES 
(1, 1, 1, NULL, 5, 'Phòng Nipa Villa tuyệt hảo, mát mẻ, nhân viên buồng dọn rất sạch.', 'Approved', 4, 'Đánh giá tích cực hợp lệ', CURRENT_TIMESTAMP),
(2, 2, 2, NULL, 4, 'Phòng River Villa đẹp, view sông thơ mộng, đồ ăn room service hơi chậm.', 'Approved', 4, 'Đánh giá xây dựng hợp lệ', CURRENT_TIMESTAMP),
(3, 3, 3, NULL, 5, 'Khóa tu Wellness Retreats giúp tôi tịnh tâm, phục hồi sức khoẻ rất nhiều.', 'Approved', 4, 'Đánh giá tốt chất lượng cao', CURRENT_TIMESTAMP),
(4, 4, 4, NULL, 4, 'Phòng 104 sạch sẽ, bồn tắm rộng rãi, decor buồng cưới rất tỉ mỉ.', 'Approved', 4, 'Đánh giá tốt', CURRENT_TIMESTAMP),
(5, 5, 5, NULL, 5, 'Rất hài lòng với kỳ nghỉ tại resort, bãi cỏ xanh ngát, đồ ăn buffet ngon.', 'Approved', 4, 'Đánh giá tốt', CURRENT_TIMESTAMP),
(6, 6, 6, NULL, 4, 'Cảnh quan xanh mát, phòng 207 view hồ bơi rộng rãi tuyệt vời.', 'Approved', 4, 'Đánh giá tốt', CURRENT_TIMESTAMP),
(7, 7, 7, NULL, 5, 'Hội An Tour do HDV Hướng Dẫn thuyết minh rất sinh động, xe đi êm.', 'Approved', 4, 'Đánh giá tốt', CURRENT_TIMESTAMP),
(8, 8, 8, NULL, 5, 'Bữa tối Wagyu tại nhà hàng cực ngon, thịt mềm mọng sốt tiêu thơm.', 'Approved', 4, 'Đánh giá tốt', CURRENT_TIMESTAMP),
(10, 13, 10, NULL, 4, 'Lịch trình trơn tru, nhân viên thân thiện hiếu khách nhiệt tình.', 'Approved', 4, 'Đánh giá tốt', CURRENT_TIMESTAMP);

-- ── 40. Authorized Devices (10 rows) ─────────────────────────
INSERT INTO Authorized_Devices (device_code, is_approved) VALUES 
('HN-2ZMIZIICB-6W7ITHEVD', true),
('HN-DEV-LAPTOP002', true),
('HN-DEV-TABLET003', true),
('HN-DEV-PHONE004', true),
('HN-DEV-KIOSK005', true),
('HN-DEV-LAPTOP006', true),
('HN-DEV-TABLET007', true),
('HN-DEV-PHONE008', true),
('HN-DEV-KIOSK009', true),
('HN-DEV-LAPTOP010', true);

-- ── 41. Audit Logs (10 rows) ─────────────────────────────────
INSERT INTO Audit_Logs (log_id, account_id, action, table_name, record_id, old_value, new_value, ip_address, timestamp) VALUES 
(1, 1, 'UPDATE_ROOM_STATUS', 'Rooms', 1, 'Vacant_Clean', 'Occupied', '192.168.1.10', CURRENT_TIMESTAMP),
(2, 1, 'CREATE_BOOKING', 'Bookings', 14, NULL, 'New Booking Created', '192.168.1.10', CURRENT_TIMESTAMP),
(3, 4, 'CHECKIN_GUEST', 'Room_Guests', 1, NULL, 'Guest Checked In Room 101', '192.168.1.15', CURRENT_TIMESTAMP),
(4, 2, 'ORDER_FOOD', 'Food_Orders', 19, NULL, 'New Room Service Order Created', '192.168.1.22', CURRENT_TIMESTAMP),
(5, 4, 'APPROVE_DEVICE', 'Authorized_Devices', 2, 'false', 'true', '192.168.1.10', CURRENT_TIMESTAMP),
(6, 13, 'START_TOUR', 'Run_Itinerary_Status', 1, 'NOT_STARTED', 'COMPLETED', '192.168.1.5', CURRENT_TIMESTAMP),
(7, 4, 'MODERATE_REVIEW', 'Reviews', 1, 'Pending', 'Approved', '192.168.1.10', CURRENT_TIMESTAMP),
(9, 4, 'UPDATE_INVOICE', 'Consolidated_Invoices', 1, 'Draft', 'Settled', '192.168.1.10', CURRENT_TIMESTAMP);

-- ── 42. Test Accounts cho khách đăng nhập test ───────────────
-- password: admin123  |  hash: $2a$10$4bnThA4xQVw1rF2POQv78uAQll2KsUsgF32JaYiVG5d2d3Fhgk83q
INSERT IGNORE INTO Accounts (account_id, username, password_hash, is_active, role_id, created_at) VALUES
(21, 'testguest1', '$2a$10$4bnThA4xQVw1rF2POQv78uAQll2KsUsgF32JaYiVG5d2d3Fhgk83q', TRUE, 10, CURRENT_TIMESTAMP),
(22, 'testguest2', '$2a$10$4bnThA4xQVw1rF2POQv78uAQll2KsUsgF32JaYiVG5d2d3Fhgk83q', TRUE, 10, CURRENT_TIMESTAMP),
(23, 'testguest3', '$2a$10$4bnThA4xQVw1rF2POQv78uAQll2KsUsgF32JaYiVG5d2d3Fhgk83q', TRUE, 10, CURRENT_TIMESTAMP);

-- ── 43. Test Customers liên kết Account ──────────────────────
INSERT IGNORE INTO Customers (customer_id, account_id, full_name, gender, cccd_passport_encrypted, phone, email, loyalty_points, membership_tier) VALUES
(16, 21, 'Nguyễn Minh Test', 'Nam', 'CCCD_TEST01', '0911000001', 'testguest1@test.com', 50, 'Regular'),
(17, 22, 'Trần Thị Test', 'Nữ', 'CCCD_TEST02', '0911000002', 'testguest2@test.com', 100, 'Silver'),
(18, 23, 'Lê Văn Test', 'Nam', 'CCCD_TEST03', '0911000003', 'testguest3@test.com', 200, 'Gold');

-- ── 44. Test Bookings (Confirmed + Checked_In) ──────────────
INSERT IGNORE INTO Bookings (booking_id, customer_id, booking_date, total_price, booking_status, booking_source, applied_promotion_id, version) VALUES
(21, 16, '2026-06-14', 5000000, 'Checked_In', 'Direct_Web', NULL, 1),
(22, 17, '2026-06-15', 7000000, 'Checked_In', 'Direct_Web', NULL, 1),
(23, 18, '2026-06-10', 16000000, 'Confirmed', 'Direct_Web', NULL, 1);

-- ── 45. Test Room Bookings ───────────────────────────────────
INSERT IGNORE INTO Room_Bookings (room_booking_id, check_in_date, check_out_date, deposit_amount, cancellation_deadline, credit_limit, personal_pin_hash) VALUES
(21, '2026-06-14', '2026-06-20', 1000000, '2026-06-12', 8000000, 'hash'),
(22, '2026-06-15', '2026-06-18', 1500000, '2026-06-13', 10000000, 'hash'),
(23, '2026-06-10', '2026-06-16', 3000000, '2026-06-08', 15000000, 'hash');

-- ── 46. Test Room Booking Details ────────────────────────────
INSERT IGNORE INTO Room_Booking_Details (detail_id, room_booking_id, category_id, room_id, room_charge, detail_status, bed_preference, special_requests, is_charge_to_room_allowed, sub_credit_limit, billing_routing_strategy) VALUES
(11, 21, 4, 6, 2000000, 'Active', 'KING_SIZE', NULL, TRUE, 500000, 'BILL_TO_LEADER'),
(12, 22, 2, 11, 3500000, 'Active', 'KING_SIZE', NULL, TRUE, 1500000, 'BILL_TO_LEADER'),
(13, 23, 3, 16, 8000000, 'Active', 'TWIN_BED', NULL, TRUE, 2000000, 'BILL_TO_LEADER');

-- ── 47. Test Room Guests ─────────────────────────────────────
INSERT IGNORE INTO Room_Guests (guest_id, detail_id, customer_id, dependent_id, guest_type, is_primary_contact) VALUES
(11, 11, 16, NULL, 'ADULT', TRUE),
(12, 12, 17, NULL, 'ADULT', TRUE),
(13, 13, 18, NULL, 'ADULT', TRUE);

-- ── 48. Cập nhật Rooms hiện tại đang được test guest ở ──────
UPDATE Rooms SET current_booking_detail_id = 11, room_status = 'Occupied' WHERE room_id = 6;
UPDATE Rooms SET current_booking_detail_id = 12, room_status = 'Occupied' WHERE room_id = 11;
UPDATE Rooms SET current_booking_detail_id = 13, room_status = 'Occupied' WHERE room_id = 16;

-- ── 51. Lê Hoàng Nam đặt 3 phòng 1 lượt ─────────────────────────────────────
-- RoomBooking IS-A Booking (Table-Per-Class Inheritance):
--   room_booking_id = booking_id (cùng một PK, bản ghi cha trong Bookings, con trong Room_Bookings)
-- Nam đặt 3 phòng riêng, check-in 2026-06-25, check-out 2026-06-28

-- Bước 1: 3 bản ghi Bookings (bảng cha) — mỗi cái là 1 phòng của Nam
INSERT IGNORE INTO Bookings (booking_id, customer_id, booking_date, total_price, booking_status, booking_source, applied_promotion_id, version) VALUES
(50, 1, '2026-06-22', 4500000, 'Confirmed', 'Direct_Web', NULL, 1),
(51, 1, '2026-06-22', 4500000, 'Confirmed', 'Direct_Web', NULL, 1),
(52, 1, '2026-06-22', 4500000, 'Confirmed', 'Direct_Web', NULL, 1);

-- Bước 2: 3 bản ghi Room_Bookings (bảng con) với cùng ID → kế thừa từ Bookings
INSERT IGNORE INTO Room_Bookings (room_booking_id, check_in_date, check_out_date, deposit_amount, cancellation_deadline, credit_limit, personal_pin_hash) VALUES
(50, '2026-06-25', '2026-06-28', 1000000, '2026-06-23', 6000000, 'hash'),
(51, '2026-06-25', '2026-06-28', 1000000, '2026-06-23', 6000000, 'hash'),
(52, '2026-06-25', '2026-06-28', 1000000, '2026-06-23', 6000000, 'hash');

-- Bước 3: 3 Room_Booking_Details — phòng 301, 302, 303 (Family Connecting Room, category_id=7)
INSERT IGNORE INTO Room_Booking_Details (detail_id, room_booking_id, category_id, room_id, room_charge, detail_status, bed_preference, special_requests, is_charge_to_room_allowed, sub_credit_limit, billing_routing_strategy, customer_id) VALUES
(50, 50, 7, 21, 4500000, 'Checked_In', 'KING_SIZE', 'Cần thêm giường phụ cho trẻ em', TRUE, 1000000, 'BILL_TO_LEADER', 1),
(51, 51, 7, 22, 4500000, 'Checked_In', 'TWIN_BED', NULL,                              TRUE, 1000000, 'BILL_TO_LEADER', 2),
(52, 52, 7, 23, 4500000, 'Checked_In', 'TWIN_BED', 'Tầng cao, view đẹp',              TRUE, 1000000, 'BILL_TO_LEADER', 3);

-- Cập nhật trạng thái và gán current_booking cho 3 phòng này
UPDATE Rooms SET current_booking_detail_id = 50, room_status = 'Occupied' WHERE room_id = 21;
UPDATE Rooms SET current_booking_detail_id = 51, room_status = 'Occupied' WHERE room_id = 22;
UPDATE Rooms SET current_booking_detail_id = 52, room_status = 'Occupied' WHERE room_id = 23;

-- Bước 4: Room Guests — Mỗi phòng 1 người đại diện (Lê Hoàng Nam, Nguyễn Văn An, Phạm Tuấn)
INSERT IGNORE INTO Room_Guests (guest_id, detail_id, customer_id, dependent_id, guest_type, is_primary_contact) VALUES
(50, 50, 1,    NULL, 'ADULT', TRUE),
(51, 50, NULL,    1, 'CHILD', FALSE),
(52, 50, NULL,    2, 'CHILD', FALSE),
(53, 51, 2,    NULL, 'ADULT', TRUE),
(54, 52, 3,    NULL, 'ADULT', TRUE);

-- Bước 5: Thêm Folio Items (Fake dịch vụ sử dụng) cho 3 phòng của Lê Hoàng Nam
INSERT IGNORE INTO Folio_Items (folio_item_id, booking_id, room_booking_detail_id, payer_customer_id, source_department, amount, description, is_settled_separately, created_by_staff_id, created_at) VALUES 
(50, 50, 50, 1, 'F&B', 850000, 'Ăn tối tại nhà hàng - Set menu', FALSE, 2, CURRENT_TIMESTAMP),
(51, 50, 50, 1, 'LAUNDRY', 120000, 'Giặt sấy quần áo', FALSE, 4, CURRENT_TIMESTAMP),
(52, 51, 51, 1, 'F&B', 150000, 'Đồ uống minibar - Phòng 302', FALSE, 2, CURRENT_TIMESTAMP),
(53, 51, 51, 1, 'SPA', 800000, 'Massage thư giãn 60 phút', FALSE, 3, CURRENT_TIMESTAMP),
(54, 52, 52, 1, 'TRANSPORTATION', 350000, 'Thuê xe máy 2 ngày', FALSE, 4, CURRENT_TIMESTAMP);

-- ── Reset Auto-Increment Sequences (MySQL syntax) ────────────
ALTER TABLE Roles AUTO_INCREMENT = 100;
ALTER TABLE Accounts AUTO_INCREMENT = 100;
ALTER TABLE Employees AUTO_INCREMENT = 100;
ALTER TABLE Room_Categories AUTO_INCREMENT = 100;
ALTER TABLE Rooms AUTO_INCREMENT = 100;
ALTER TABLE Customers AUTO_INCREMENT = 100;
ALTER TABLE Bookings AUTO_INCREMENT = 100;
ALTER TABLE Room_Bookings AUTO_INCREMENT = 100;
ALTER TABLE Room_Booking_Details AUTO_INCREMENT = 100;
ALTER TABLE Room_Guests AUTO_INCREMENT = 100;
ALTER TABLE Restaurant_Tables AUTO_INCREMENT = 100;
ALTER TABLE Menu_Items AUTO_INCREMENT = 100;
ALTER TABLE Food_Orders AUTO_INCREMENT = 100;
ALTER TABLE Food_Order_Details AUTO_INCREMENT = 100;
ALTER TABLE Tours AUTO_INCREMENT = 100;
ALTER TABLE Tour_Schedules AUTO_INCREMENT = 100;
ALTER TABLE Tour_Attendees AUTO_INCREMENT = 100;

-- ── 48b. Tour Bookings seed — TourBooking liên kết với RoomBooking nhưng CHƯA phân phòng ──────
-- Các bản ghi này có room_booking_id != NULL nhưng room_booking_detail_id = NULL
-- → lễ tân sẽ thấy và phân bổ tour vào phòng cụ thể khi check-in

-- Booking gốc cho TourBooking (kiểu cha Bookings)
INSERT IGNORE INTO Bookings (booking_id, customer_id, booking_date, total_price, booking_status, booking_source, applied_promotion_id, version) VALUES
(200, 12, CURDATE(), 1200000, 'Confirmed', 'Direct_Web', NULL, 1),
(201, 12, CURDATE(), 3000000, 'Confirmed', 'Direct_Web', NULL, 1),
(202, 18, CURDATE(), 1200000, 'Confirmed', 'Direct_Web', NULL, 1);

-- TourBookings: liên kết với RoomBooking 14 (Phạm Hùng Anh) và 23 (test Confirmed)
-- room_booking_detail_id = NULL vì chưa check-in / chưa phân phòng
INSERT IGNORE INTO Tour_Bookings (booking_id, schedule_id, participant_count, tour_charge, room_booking_id, room_booking_detail_id) VALUES
(200, 1, 2, 1200000, 14, NULL),
(201, 2, 5, 3000000, 14, NULL),
(202, 3, 2, 1200000, 23, NULL);

ALTER TABLE Tour_Bookings AUTO_INCREMENT = 300;

-- ── 49. Export History (Mock Data) ───────────────────────────

INSERT IGNORE INTO Export_History (id, report_name, format, exported_at, exported_by, file_size) VALUES
(1, 'Doanh thu tháng 5/2026', 'Excel', '2026-06-01 09:15:00', 'Manager ', '2.4 MB'),
(2, 'Tỷ lệ lấp đầy Q2', 'PDF', '2026-05-30 14:30:00', 'Manager ', '1.1 MB'),
(3, 'Báo cáo tour tháng 4', 'CSV', '2026-05-02 10:00:00', 'Manager ', '320 KB'),
(4, 'Doanh thu năm 2025', 'Excel', '2026-01-15 08:45:00', 'Manager ', '5.8 MB');

-- ── 50. Mock Data for YoY Comparison (Năm 2025) ───────────────────────────
INSERT INTO Bookings (booking_id, customer_id, booking_date, total_price, booking_status, booking_source, applied_promotion_id, version) VALUES
(991, 1, '2025-06-01', 65000000, 'Confirmed', 'Direct_Web', NULL, 1),
(992, 2, '2025-06-05', 45000000, 'Confirmed', 'OTA', NULL, 1);

INSERT INTO Room_Bookings (room_booking_id, check_in_date, check_out_date, deposit_amount, cancellation_deadline, credit_limit, personal_pin_hash) VALUES
(991, '2025-06-10', '2025-06-15', 5000000, '2025-06-05', 10000000, 'hash');

INSERT INTO Room_Booking_Details (detail_id, room_booking_id, category_id, room_id, room_charge, detail_status, bed_preference, special_requests, is_charge_to_room_allowed, sub_credit_limit, billing_routing_strategy, number_of_adults, number_of_children) VALUES
(991, 991, 1, 1, 65000000, 'Checked_Out', 'KING_SIZE', NULL, TRUE, 500000, 'BILL_TO_LEADER');

INSERT INTO Tour_Bookings (booking_id, schedule_id, participant_count, tour_charge, is_walk_in_tour) VALUES 
(992, 1, 4, 45000000, FALSE);

-- Self-healing database name update for customer Ngọc Thị (formerly Lê Quang)
UPDATE Customers SET full_name = 'Ngọc Thị', gender = 'Nữ' WHERE customer_id = 5;
UPDATE Accounts SET username = 'ngocthi' WHERE account_id = 9;


-- ============================================================
-- 52. COMPREHENSIVE E2E MOCK DATA APPENDED (IDs > 500)
-- ============================================================

-- Additional Customers
INSERT IGNORE INTO Accounts (account_id, username, password_hash, is_active, role_id, created_at) VALUES 
(501, 'vip_customer', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 9, CURRENT_TIMESTAMP),
(502, 'normal_customer', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 10, CURRENT_TIMESTAMP),
(503, 'newbie_customer', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 10, CURRENT_TIMESTAMP),
(504, 'banned_customer', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', FALSE, 10, CURRENT_TIMESTAMP);

INSERT IGNORE INTO Customers (customer_id, account_id, full_name, email, phone, gender, cccd_passport_encrypted, loyalty_points, membership_tier_id) VALUES
(501, 501, 'VIP Nguyễn Văn A', 'vipa@example.com', '0999888771', 'Nam', '001099000501', 52000, 4),
(502, 502, 'Normal Trần B', 'normalb@example.com', '0999888772', 'Nữ', '001099000502', 95, 1),
(503, 503, 'Newbie Lê C', 'newbiec@example.com', '0999888773', 'Nam', '001099000503', 0, 1),
(504, 504, 'Banned Phạm D', 'bannedd@example.com', '0999888774', 'Nữ', '001099000504', 0, 1);

INSERT IGNORE INTO Dependents (dependent_id, customer_id, full_name, date_of_birth, relationship, face_vector_data, face_image_url) VALUES 
(501, 501, 'Wife Nguyễn Thị B', '1995-08-15', 'Vợ', NULL, NULL),
(502, 501, 'Kid Nguyễn Văn C', '2015-05-20', 'Con', NULL, NULL);

-- Additional Rooms (just in case)
INSERT IGNORE INTO Rooms (room_id, room_number, category_id, room_status) VALUES
(501, '801', 3, 'Occupied'),
(502, '802', 1, 'Vacant_Dirty'),
(503, '803', 2, 'OutOfOrder'),
(504, '804', 4, 'Maintenance');

-- Promotions
INSERT IGNORE INTO Promotions (promotion_id, promo_code, discount_type, discount_value, max_discount_value_vnd, min_order_value_vnd, applicable_scope, max_uses, current_uses, valid_from, valid_to, is_active, manager_approval_threshold_pct) VALUES
(501, 'TEST30', 'PERCENTAGE', 35, 2000000, 0, 'ALL', 100, 0, '2026-01-01', '2026-12-31', TRUE, 30),
(502, 'TEST500K', 'FIXED_AMOUNT', 500000, NULL, 0, 'ROOM', 50, 50, '2026-01-01', '2026-12-31', TRUE, NULL);

-- ============================================================
-- PAST BOOKINGS FOR POINTS JUSTIFICATION
-- ============================================================
-- VIP Customer has 52,000 pts. Let's say 1 point = 1,000 VND spent (or similar). So 52M VND spent.
-- We add an old Booking with a very high total_price that is Checked_Out.
INSERT IGNORE INTO Bookings (booking_id, customer_id, booking_date, total_price, booking_status, booking_source, applied_promotion_id, version) VALUES
(499, 501, '2025-12-01', 52000000, 'Checked_Out', 'Direct_Web', NULL, 1);
INSERT IGNORE INTO Room_Bookings (room_booking_id, check_in_date, check_out_date, deposit_amount, cancellation_deadline, credit_limit, personal_pin_hash) VALUES
(499, '2025-12-20', '2025-12-25', 10000000, '2025-12-15', 20000000, 'hash');
INSERT IGNORE INTO Room_Booking_Details (detail_id, room_booking_id, category_id, room_id, room_charge, detail_status, bed_preference, special_requests, is_charge_to_room_allowed, sub_credit_limit, billing_routing_strategy, customer_id) VALUES
(499, 499, 5, 15, 52000000, 'Checked_Out', 'KING_SIZE', 'Tuần trăng mật', TRUE, 5000000, 'BILL_TO_LEADER', 501);

-- Normal Customer has 95 pts (95,000 VND).
INSERT IGNORE INTO Bookings (booking_id, customer_id, booking_date, total_price, booking_status, booking_source, applied_promotion_id, version) VALUES
(500, 502, '2025-10-01', 95000, 'Checked_Out', 'Direct_Web', NULL, 1);
-- (Maybe just a F&B walk-in or something, but we'll assign a tiny booking)
INSERT IGNORE INTO Room_Bookings (room_booking_id, check_in_date, check_out_date, deposit_amount, cancellation_deadline, credit_limit, personal_pin_hash) VALUES
(500, '2025-10-10', '2025-10-11', 0, '2025-10-05', 0, 'hash');
INSERT IGNORE INTO Room_Booking_Details (detail_id, room_booking_id, category_id, room_id, room_charge, detail_status, bed_preference, special_requests, is_charge_to_room_allowed, sub_credit_limit, billing_routing_strategy, customer_id) VALUES
(500, 500, 1, 1, 95000, 'Checked_Out', 'TWIN_BED', '', FALSE, 0, 'INDIVIDUAL', 502);


-- ============================================================
-- E2E Bookings (CURRENT)
-- ============================================================
-- 1. Checked_In for VIP (Room 801)
INSERT IGNORE INTO Bookings (booking_id, customer_id, booking_date, total_price, booking_status, booking_source, applied_promotion_id, version) VALUES
(501, 501, '2026-06-25', 10500000, 'Checked_In', 'Direct_Web', NULL, 1);
INSERT IGNORE INTO Room_Bookings (room_booking_id, check_in_date, check_out_date, deposit_amount, cancellation_deadline, credit_limit, personal_pin_hash) VALUES
(501, '2026-06-28', '2026-07-02', 2000000, '2026-06-25', 10000000, 'hash');
INSERT IGNORE INTO Room_Booking_Details (detail_id, room_booking_id, category_id, room_id, room_charge, detail_status, bed_preference, special_requests, is_charge_to_room_allowed, sub_credit_limit, billing_routing_strategy, customer_id) VALUES
(501, 501, 3, 501, 10500000, 'Checked_In', 'KING_SIZE', 'Gần thang máy', TRUE, 5000000, 'BILL_TO_LEADER', 501);
UPDATE Rooms SET current_booking_detail_id = 501, room_status = 'Occupied' WHERE room_id = 501;

INSERT IGNORE INTO Room_Guests (guest_id, detail_id, customer_id, dependent_id, guest_type, is_primary_contact) VALUES
(501, 501, 501, NULL, 'ADULT', TRUE),
(502, 501, NULL, 501, 'ADULT', FALSE);

-- 2. Confirmed for Normal Customer (Ready for Check-in)
INSERT IGNORE INTO Bookings (booking_id, customer_id, booking_date, total_price, booking_status, booking_source, applied_promotion_id, version) VALUES
(502, 502, '2026-06-28', 1800000, 'Confirmed', 'Direct_Web', NULL, 1);
INSERT IGNORE INTO Room_Bookings (room_booking_id, check_in_date, check_out_date, deposit_amount, cancellation_deadline, credit_limit, personal_pin_hash) VALUES
(502, '2026-07-15', '2026-07-17', 500000, '2026-07-10', 2000000, 'hash');
INSERT IGNORE INTO Room_Booking_Details (detail_id, room_booking_id, category_id, room_id, room_charge, detail_status, bed_preference, special_requests, is_charge_to_room_allowed, sub_credit_limit, billing_routing_strategy, customer_id) VALUES
(502, 502, 2, NULL, 1800000, 'Confirmed', 'TWIN_BED', 'Phòng yên tĩnh', TRUE, 2000000, 'INDIVIDUAL', 502);

-- 3. Pending_Approval (Triggered workflow)
INSERT IGNORE INTO Bookings (booking_id, customer_id, booking_date, total_price, booking_status, booking_source, applied_promotion_id, version) VALUES
(503, 502, '2026-06-29', 5000000, 'Pending_Approval', 'Direct_Web', 501, 1);
INSERT IGNORE INTO Room_Bookings (room_booking_id, check_in_date, check_out_date, deposit_amount, cancellation_deadline, credit_limit, personal_pin_hash) VALUES
(503, '2026-08-01', '2026-08-05', 0, '2026-07-25', 0, 'hash');
INSERT IGNORE INTO Room_Booking_Details (detail_id, room_booking_id, category_id, room_id, room_charge, detail_status, bed_preference, special_requests, is_charge_to_room_allowed, sub_credit_limit, billing_routing_strategy, customer_id) VALUES
(503, 503, 1, NULL, 5000000, 'Pending', 'KING_SIZE', 'Áp mã giảm sâu', TRUE, 0, 'INDIVIDUAL', 502);

-- F&B Orders (Lifecycles)
INSERT IGNORE INTO Food_Orders (order_id, booking_id, guest_id, staff_id, order_type, table_number, total_amount, order_status, payment_status, notes) VALUES
(501, 501, 501, 2, 'ROOM_SERVICE', 'RM801', 415000, 'PENDING', 'UNPAID', 'Phòng 801 VIP'),
(502, 501, 501, 3, 'DINE_IN', 'T05', 500000, 'COOKING', 'UNPAID', 'Ít đá'),
(503, 501, 501, 2, 'DINE_IN', 'T06', 85000, 'SERVED', 'UNPAID', 'Charge to room'),
(504, NULL, NULL, 3, 'DINE_IN', 'T07', 700000, 'PAID', 'PAID', 'Khách vãng lai'),
(505, NULL, NULL, 2, 'DINE_IN', 'T08', 0, 'CANCELLED', 'UNPAID', 'Khách đổi ý');

INSERT IGNORE INTO Food_Order_Details (detail_id, order_id, item_id, quantity, unit_price, subtotal) VALUES
(501, 501, 2, 1, 350000, 350000),
(502, 501, 4, 1, 65000, 65000),
(503, 502, 2, 1, 350000, 350000),
(504, 502, 5, 1, 95000, 95000),
(505, 503, 1, 1, 85000, 85000),
(506, 504, 2, 2, 350000, 700000);

-- Folios linked
INSERT IGNORE INTO Folio_Items (folio_item_id, booking_id, room_booking_detail_id, payer_customer_id, source_department, amount, description, is_settled_separately, created_by_staff_id, created_at) VALUES 
(501, 501, 501, 501, 'LAUNDRY', 150000, 'Giặt ủi VIP', FALSE, 4, CURRENT_TIMESTAMP),
(502, 501, 501, 501, 'FNB', 85000, 'Order Nhà hàng T06 (SERVED)', FALSE, 2, CURRENT_TIMESTAMP);


-- ============================================================
-- APPENDED NEW BOOKINGS FOR MULTIPLE ROOM TESTING
-- ============================================================
INSERT INTO Bookings (booking_id, customer_id, booking_date, total_price, booking_status, booking_source, applied_promotion_id, version) VALUES
(101, 3, '2026-06-25', 15000000, 'Confirmed', 'Direct_Web', NULL, 1),
(102, 4, '2026-06-26', 22000000, 'Confirmed', 'OTA', NULL, 1);

INSERT INTO Room_Bookings (room_booking_id, check_in_date, check_out_date, deposit_amount, cancellation_deadline, credit_limit, personal_pin_hash) VALUES
(101, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 2 DAY), 2000000, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 10000000, 'hash101'),
(102, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 3 DAY), 3000000, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 15000000, 'hash102');

INSERT INTO Room_Booking_Details (detail_id, room_booking_id, category_id, room_id, room_charge, detail_status, bed_preference, special_requests, is_charge_to_room_allowed, sub_credit_limit, billing_routing_strategy, number_of_adults, number_of_children) VALUES
-- Booking 101: 2 rooms
(1011, 101, 7, NULL, 2500000, 'Pending', 'KING_SIZE', NULL, TRUE, 500000, 'BILL_TO_LEADER', 2, 0),
(1012, 101, 7, NULL, 3500000, 'Pending', 'TWIN_BED', NULL, TRUE, 1000000, 'BILL_TO_LEADER', 2, 2),
-- Booking 102: 3 rooms
(1021, 102, 7, NULL, 2500000, 'Pending', 'KING_SIZE', NULL, TRUE, 500000, 'BILL_TO_LEADER', 1, 0),
(1022, 102, 7, NULL, 8000000, 'Pending', 'KING_SIZE', NULL, TRUE, 2000000, 'BILL_TO_LEADER', 3, 1),
(1023, 102, 7, NULL, 2500000, 'Pending', 'TWIN_BED', NULL, TRUE, 500000, 'BILL_TO_LEADER', 2, 1);

UPDATE Room_Booking_Details SET number_of_adults = 2, number_of_children = 0 WHERE number_of_adults IS NULL;


-- ============================================================
-- APPENDED BOOKING 103: MULTIPLE ROOMS + PRE-REGISTERED GUESTS
-- ============================================================
INSERT INTO Bookings (booking_id, customer_id, booking_date, total_price, booking_status, booking_source, applied_promotion_id, version) VALUES
(103, 1, '2026-06-27', 18000000, 'Confirmed', 'Direct_Web', NULL, 1);

INSERT INTO Room_Bookings (room_booking_id, check_in_date, check_out_date, deposit_amount, cancellation_deadline, credit_limit, personal_pin_hash) VALUES
(103, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 2 DAY), 2000000, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 10000000, 'hash103');

INSERT INTO Room_Booking_Details (detail_id, room_booking_id, category_id, room_id, room_charge, detail_status, bed_preference, special_requests, is_charge_to_room_allowed, sub_credit_limit, billing_routing_strategy, number_of_adults, number_of_children) VALUES
(1031, 103, 8, NULL, 4500000, 'Pending', 'KING_SIZE', 'G?n thang m�y', TRUE, 1000000, 'BILL_TO_LEADER', 2, 1),
(1032, 103, 8, NULL, 4500000, 'Pending', 'TWIN_BED', 'G?n ph�ng 26', TRUE, 1000000, 'BILL_TO_LEADER', 2, 1);

INSERT INTO Room_Guests (guest_id, detail_id, customer_id, dependent_id, guest_type, is_primary_contact) VALUES
(10311, 1031, 1, NULL, 'ADULT', TRUE),
(10312, 1031, NULL, 1, 'CHILD', FALSE), -- L� Ho�ng Minh
(10321, 1032, NULL, 2, 'CHILD', FALSE); -- L� Th? H?ng



