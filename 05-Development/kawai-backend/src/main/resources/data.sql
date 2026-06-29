-- ============================================================
-- KAWAI RESORT & TOUR HUB �� COMPREHENSIVE SAMPLE DATA (V3.3)
-- Automatically executed on Spring Boot startup
-- ============================================================

-- ���� 1. Roles (10 rows) ������������������������������������������������������������������������������
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

-- ���� 2. Accounts (20 rows) ������������������������������������������������������������������������
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

-- ���� 3. Employees (10 rows) ����������������������������������������������������������������������
INSERT INTO Employees (employee_id, account_id, full_name, gender, cccd, phone, email, salary) VALUES 
(1, 1, 'Nguy廙� Qu廕τ Tr廙�', 'Nam', '001234567890', '0912000001', 'admin@hoanien.vn', 15000000),
(2, 2, 'Tr廕吵 Ph⑹①ng', 'N廙�', '001234567891', '0912000002', 'tphuong@hoanien.vn', 10000000),
(3, 3, 'Nguy廙� Minh Qu璽n', 'Nam', '001234567892', '0912000003', 'nmquan@hoanien.vn', 10000000),
(4, 4, 'L礙 Linh', 'N廙�', '001234567893', '0912000004', 'lelinh@hoanien.vn', 9000000),
(5, 13, 'NguynNgoc', 'Nam', '001234567894', '0912000005', 'guide@hoanien.vn', 8000000),
(6, 14, 'Ng廙㷼 Lan', 'N廙�', '001234567895', '0912000006', 'guide2@hoanien.vn', 8500000),
(7, 15, 'Ho�ng Nam', 'Nam', '001234567896', '0912000007', 'guide3@hoanien.vn', 8500000),
(8, 16, 'L礙 V�n T獺m', 'Nam', '001234567897', '0912000008', 'lvtam@hoanien.vn', 7000000),
(9, 17, 'Nguy廙� Th廙� Hoa', 'N廙�', '001234567898', '0912000009', 'nthoa@hoanien.vn', 7000000),
(10, 20, 'Ho�ng B廕υ Tr穫', 'Nam', '001234567899', '0912000010', 'hbtri@hoanien.vn', 7500000);

-- ���� 3.5. Membership Tiers (4 rows) ����������������������������������������������������������������������
INSERT INTO membership_tiers (tier_id, tier_name, points_from, points_to, credit_limit, description) VALUES
(1, 'Regular', 0, 999, 5000000.00, 'H廕》g th廕� m廕搾 �廙𡃉h'),
(2, 'Silver', 1000, 4999, 10000000.00, 'H廕》g B廕︷'),
(3, 'Gold', 5000, 9999, 20000000.00, 'H廕》g V�ng'),
(4, 'Platinum', 10000, 99999, 50000000.00, 'H廕》g B廕︷h kim');

-- ���� 4. Customers (15 rows) ����������������������������������������������������������������������
INSERT INTO Customers (customer_id, account_id, full_name, gender, cccd_passport_encrypted, phone, email, loyalty_points, membership_tier_id) VALUES 
(1, 5, 'Nguy廙� Xu璽n L⑹u', 'Nam', 'CCCD_101', '090101', 'nam101@test.com', 100, 1),
(2, 6, 'Ng廙㷼 Th廙�', 'Nam', 'CCCD_204', '090204', 'an204@test.com', 200, 2),
(3, 7, 'Ph廕《 Tu廕叩', 'Nam', 'CCCD_308', '090308', 'tuan308@test.com', 500, 3),
(4, 8, 'Tr廕吵 Th廙� B穩ch', 'N廙�', 'CCCD_104', '090104', 'bich104@test.com', 50, 1),
(5, 9, 'Ng廙㷼 Th廙�', 'N廙�', 'CCCD_106', '090106', 'quang106@test.com', 0, 1),
(6, 10, '�廙� M廙� Linh', 'N廙�', 'CCCD_207', '090207', 'linh207@test.com', 150, 2),
(7, 11, 'Ho�ng Anh', 'Nam', 'CCCD_210', '090210', 'anh210@test.com', 300, 3),
(8, 12, 'V觼 H羅ng', 'Nam', 'CCCD_311', '090311', 'hung311@test.com', 800, 4),
(9, NULL, 'L⑹u �穫nh �廙妾', 'Nam', 'CCCD_DEMO1', '0909990001', 'duc@test.com', 0, 1),
(10, NULL, 'Nguy廙� Minh �廙妾', 'Nam', 'CCCD_DEMO2', '0909990002', 'duc2@test.com', 0, 1),
(11, NULL, 'Tr廕吵 Th廙� Mai', 'N廙�', 'CCCD_DEMO3', '0909990003', 'mai@test.com', 0, 1),
(12, NULL, 'Ph廕《 H羅ng Anh', 'Nam', 'CCCD_DEMO4', '0909990004', 'phanh@test.com', 0, 1),
(13, NULL, 'Nguy廙� Thanh S①n', 'Nam', 'CCCD_DEMO5', '0909990005', 'ntson@test.com', 0, 1),
(14, NULL, 'V觼 Th廙� Th廕υ', 'N廙�', 'CCCD_DEMO6', '0909990006', 'vtthao@test.com', 0, 1),
(15, NULL, '�o�n Minh Khang', 'Nam', 'CCCD_DEMO7', '0909990007', 'dmkhang@test.com', 0, 1);

-- ���� 5. Dependents (10 rows) ��������������������������������������������������������������������
INSERT INTO Dependents (dependent_id, customer_id, dependent_name, birth_date, gender, cccd_passport_encrypted) VALUES 
(1, 1, 'L礙 Ho�ng Minh', '2018-05-12', 'Nam', NULL),
(2, 1, 'L礙 Th廙� H廙忛g', '2020-09-20', 'N廙�', NULL),
(3, 2, 'Nguy廙� V�n B穫nh', '2015-03-10', 'Nam', NULL),
(4, 3, 'Ph廕《 Tu廕叩 H廕ξ', '2016-07-15', 'Nam', NULL),
(5, 4, 'Tr廕吵 An Nhi礙n', '2019-11-01', 'N廙�', NULL),
(6, 6, 'Nguy廙� M廙� Anh', '2017-02-14', 'N廙�', NULL),
(7, 7, 'Ho�ng Minh Kh繫i', '2014-06-25', 'Nam', NULL),
(8, 8, 'V觼 Gia B廕υ', '2013-08-30', 'Nam', NULL),
(9, 12, 'Ph廕《 Ng廙㷼 Tr璽m', '2021-10-05', 'N廙�', NULL),
(10, 13, 'Nguy廙� Thanh H�', '2022-12-25', 'N廙�', NULL);

-- ���� 6. Room Categories (10 rows) ����������������������������������������������������������
-- Columns: category_id, category_name, cover_img_url, base_price, capacity, description,
--          base_adults, base_children, max_adults, max_children, extra_adult_surcharge, extra_child_surcharge, is_active,
--          bed_type, room_size, view_type, has_bathtub, has_balcony, complimentary_services, has_free_breakfast
INSERT INTO Room_Categories (
    category_id, category_name, cover_img_url, base_price, capacity, description,
    base_adults, base_children, max_adults, max_children, extra_adult_surcharge, extra_child_surcharge, is_active,
    bed_type, room_size, view_type, has_bathtub, has_balcony, complimentary_services, has_free_breakfast
) VALUES 
(1,  'Nipa Pool Villa',          'https://images.unsplash.com/photo-1540541338287-41700207dee6', 2500000,  2, 'Villa thanh t廙𡃉h b礙n h廙� sen th①m m獺t.',             2, 0, 3, 1, 500000,  250000, TRUE, '1 Gi⑹廙𩵚g King 2m2', 65, 'H⑹廙𣌟g h廙� b①i', TRUE, TRUE, '2 chai n⑹廙𢲷 su廙魀, Tr獺i c璽y t⑹①i, Vang �廙�', TRUE),
(2,  'River Pool Villa',         'https://images.unsplash.com/photo-1566073771259-6a8506099945', 3500000,  3, 'Villa cao c廕叼 ven s繫ng Thu B廙忛 l廙㷌g gi籀.',          2, 0, 3, 2, 600000,  300000, TRUE, '1 Gi⑹廙𩵚g King 2m2', 80, 'H⑹廙𣌟g s繫ng Thu B廙忛', TRUE, TRUE, '4 chai n⑹廙𢲷 su廙魀, Tr獺i c璽y, Tr� chi廙�', TRUE),
(3,  'Wellness Retreats',        'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4', 8000000,  4, 'H�nh tr穫nh t藺nh l廕搖g, ch�m s籀c s廙妾 kho廕� to�n di廙𡵞.', 2, 0, 4, 2, 1000000, 500000, TRUE, '2 Gi⑹廙𩵚g King', 120, 'H⑹廙𣌟g v⑹廙𩵚 thi廙�', TRUE, TRUE, 'N⑹廙𢲷 detox, Tr獺i c璽y Organic, Tr� th廕υ m廙緽', TRUE),
(4,  'Garden View Suite',        'https://images.unsplash.com/photo-1578683010236-d716f9a3f461', 2000000,  2, 'Suite h⑹廙𣌟g v⑹廙𩵚 nhi廙峼 �廙𢹂 xanh m⑹廙𣕧.',             2, 0, 3, 1, 400000,  200000, TRUE, '1 Gi⑹廙𩵚g Queen 1m8', 45, 'H⑹廙𣌟g v⑹廙𩵚 nhi廙峼 �廙𢹂', FALSE, TRUE, '2 chai n⑹廙𢲷 su廙魀, Tr� & C� ph礙', TRUE),
(5,  'Presidential Ocean Suite', 'https://images.unsplash.com/photo-1590490360182-c33d57733427', 15000000, 6, 'H廕》g ph簷ng cao c廕叼 b廕苞 nh廕另 h⑹廙𣌟g bi廙�.',           4, 0, 6, 3, 2000000, 1000000, TRUE, '3 Gi⑹廙𩵚g King 2m2', 250, 'H⑹廙𣌟g bi廙� to�n c廕τh', TRUE, TRUE, 'Minibar mi廙� ph穩, R⑹廙ㄆ Champagne, B獺nh ng廙㦉', TRUE),
(6,  'Ocean View Bungalow',      'https://images.unsplash.com/photo-1582719508461-905c673771fd', 3000000,  2, 'Bungalow b瓊i c獺t �籀n gi籀 bi廙� t⑹①i m獺t.',           2, 0, 3, 1, 600000,  300000, TRUE, '1 Gi⑹廙𩵚g King 2m2', 50, 'H⑹廙𣌟g bi廙�', TRUE, TRUE, '2 chai n⑹廙𢲷 su廙魀, Tr獺i c璽y t⑹①i', TRUE),
(7,  'Family Connecting Room',   'https://images.unsplash.com/photo-1568495248636-6432b97bd949', 4500000,  5, 'Ph簷ng th繫ng nhau ph羅 h廙φ cho c廕� gia �穫nh.',         2, 2, 4, 4, 500000,  250000, TRUE, '1 Gi⑹廙𩵚g King & 2 Gi⑹廙𩵚g �①n', 90, 'H⑹廙𣌟g v⑹廙𩵚', FALSE, TRUE, '4 chai n⑹廙𢲷 su廙魀, B獺nh quy, Tr�', TRUE),
(8,  'Superior Mountain View',   'https://images.unsplash.com/photo-1596394516093-501ba68a0ba6', 1800000,  2, 'Ph簷ng h⑹廙𣌟g n繳i thanh t廙𡃉h b穫nh y礙n.',              2, 0, 2, 1, 350000,  150000, TRUE, '2 Gi⑹廙𩵚g �①n 1m2', 40, 'H⑹廙𣌟g n繳i �廙𧗽', FALSE, FALSE, '2 chai n⑹廙𢲷 su廙魀, Tr� & C� ph礙', TRUE),
(9,  'Luxury Penthouse',         'https://images.unsplash.com/photo-1502672260266-1c1ef2d93688', 12000000, 4, 'C�n h廙� t廕吵g m獺i �廕軟g c廕叼 ng廕痂 to�n c廕τh resort.',  2, 0, 4, 2, 1500000, 750000, TRUE, '2 Gi⑹廙𩵚g King si礙u l廙𣌟', 180, 'To�n c廕τh Resort', TRUE, TRUE, 'R⑹廙ㄆ vang cao c廕叼, Tr獺i c璽y nh廕計 kh廕季, Minibar', TRUE),
(10, 'Cozy Studio Room',         'https://images.unsplash.com/photo-1522771739844-6a9f6d5f14af', 1500000,  2, 'Ph簷ng Studio nh廙� g廙㤔, �廕囤 �廙� ti廙𡵞 nghi.',          2, 0, 2, 1, 300000,  150000, TRUE, '1 Gi⑹廙𩵚g Queen 1m8', 35, 'H⑹廙𣌟g �⑹廙𩵚g ph廙�', FALSE, FALSE, '2 chai n⑹廙𢲷 su廙魀, C� ph礙 h簷a tan', FALSE);

-- ���� 7. Room Surcharges (10 rows) ����������������������������������������������������������
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

-- ���� 8. Rooms (50 rows) ������������������������������������������������������������������������������
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

-- ���� 9. Dynamic Pricing (10 rows) ����������������������������������������������������������
INSERT INTO Dynamic_Pricing (price_id, category_id, start_date, end_date, price_modifier, reason) VALUES 
(1, 1, '2026-06-01', '2026-06-30', 200000, 'M羅a cao �i廙� h癡'),
(2, 2, '2026-06-01', '2026-06-30', 300000, 'M羅a cao �i廙� h癡'),
(3, 3, '2026-06-01', '2026-06-30', 500000, 'M羅a cao �i廙� h癡'),
(4, 5, '2026-06-01', '2026-06-30', 1000000, 'M羅a cao �i廙� h癡'),
(5, 9, '2026-06-01', '2026-06-30', 800000, 'M羅a cao �i廙� h癡'),
(6, 1, '2026-09-01', '2026-09-30', -200000, '⑸u �瓊i m羅a thu'),
(7, 2, '2026-09-01', '2026-09-30', -300000, '⑸u �瓊i m羅a thu'),
(8, 3, '2026-09-01', '2026-09-30', -500000, '⑸u �瓊i m羅a thu'),
(9, 4, '2026-09-01', '2026-09-30', -150000, '⑸u �瓊i m羅a thu'),
(10, 6, '2026-09-01', '2026-09-30', -250000, '⑸u �瓊i m羅a thu');

-- ���� 10. Daily Rates (10 rows) ����������������������������������������������������������������
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

-- ���� 11. Promotions (10 rows) ������������������������������������������������������������������
INSERT INTO Promotions (promo_id, promo_code, discount_type, discount_value, valid_from, valid_to, max_uses, current_uses, is_active, description) VALUES 
(1, 'SUMMER2026', 'PERCENTAGE', 10.00, '2026-05-01 00:00:00', '2026-08-31 23:59:59', 1000, 15, TRUE, 'Gi廕σ gi獺 10% cho to�n b廙� d廙醶h v廙� �廕暗 ph簷ng h癡.'),
(2, 'WELCOMETOHOANIEN', 'FIXED_AMOUNT', 200000.00, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 5000, 120, TRUE, 'T廕搖g ngay 200,000 VND cho kh獺ch �廕暗 ph簷ng l廕吵 �廕吟.'),
(3, 'VIPGOLD', 'PERCENTAGE', 15.00, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 9999, 45, TRUE, '⑸u �瓊i �廕搾 bi廙峼 gi廕σ 15% cho th�nh vi礙n Gold.'),
(4, 'MIDWEEK20', 'PERCENTAGE', 20.00, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 200, 10, TRUE, 'Gi廕σ 20% �廕暗 ph簷ng t廙� th廙� 2 �廕積 th廙� 5.'),
(5, 'AUTUMNRETREAT', 'PERCENTAGE', 12.00, '2026-09-01 00:00:00', '2026-11-30 23:59:59', 500, 0, TRUE, 'Gi廕σ gi獺 12% ch�m s籀c s廙妾 kho廕� m羅a thu.'),
(6, 'HONEYMOON', 'FIXED_AMOUNT', 500000.00, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 100, 2, TRUE, 'G籀i tr�ng m廕負 ng廙㦉 ng�o gi廕σ ngay 500k.'),
(7, 'FESTIVE15', 'PERCENTAGE', 15.00, '2026-12-20 00:00:00', '2027-01-05 23:59:59', 1000, 0, TRUE, 'Ch�o �籀n gi獺ng sinh v� n�m m廙𢹂.'),
(8, 'VOUCHER100K', 'FIXED_AMOUNT', 100000.00, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 10000, 300, TRUE, 'Voucher 100k cho kh獺ch h�ng th璽n thi廕篙.'),
(9, 'EARLYBIRD', 'PERCENTAGE', 8.00, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 2000, 50, TRUE, '�廕暗 tr⑹廙𢲷 30 ng�y h⑹廙俲g ngay ⑹u �瓊i 8%.');

-- ���� 12. Bookings (20 rows) ����������������������������������������������������������������������
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

-- ���� 13. Room Bookings (10 rows) ������������������������������������������������������������
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

-- ���� 14. Room Booking Details (10 rows) ����������������������������������������������
INSERT INTO Room_Booking_Details (detail_id, room_booking_id, category_id, room_id, room_charge, detail_status, bed_preference, special_requests, is_charge_to_room_allowed, sub_credit_limit, billing_routing_strategy) VALUES
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

-- ���� 15. Room Guests (10 rows) ����������������������������������������������������������������
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

-- ���� 16. Restaurant Tables (20 rows) ����������������������������������������������������
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


-- ���� 17. Table Reservations (10 rows) ��������������������������������������������������
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

-- ���� 18. Menu Items (34 rows) ������������������������������������������������������������������
INSERT INTO Menu_Items (item_id, item_name, price, category, is_available, description, image_url, allergy_tags, is_always_available) VALUES 
(1, 'S繳p B穩 �廙� Kem T⑹①i Truffle', 180000, 'Khai v廙�', TRUE, 'S繳p b穩 �廙� b矇o ng廕軌 k廕篙 h廙φ kem t⑹①i v� d廕吟 truffle nguy礙n ch廕另.', 'https://images.unsplash.com/photo-1547592166-23ac45744acd?w=500&q=80', 'S廙畝', FALSE),
(2, 'G廙蒨 Cu廙𤉋 T繫m Th廙𡑕', 95000, 'Khai v廙�', TRUE, 'G廙蒨 cu廙𤉋 t繫m th廙𡑕 t⑹①i ngon k癡m rau s廙𤉋g v� t⑹①ng �廕赴 ph廙㷌g.', 'https://www.cet.edu.vn/wp-content/uploads/2018/11/goi-cuon-tom-thit.jpg', '�廕赴 ph廙㷌g, H廕ξ s廕τ', FALSE),
(3, 'Ch廕� Gi簷 H廕ξ S廕τ', 110000, 'Khai v廙�', TRUE, 'Ch廕� gi簷 chi礙n gi簷n nh璽n h廕ξ s廕τ t⑹①i s廙𤉋g.', 'https://cdn.tgdd.vn/2022/01/CookDish/2-cach-lam-cha-gio-hai-san-don-gian-gion-thom-beo-ngay-ai-avt-1200x676.jpg', 'H廕ξ s廕τ', FALSE),
(4, 'Salad C獺 H廙𧗽 X繫ng Kh籀i', 150000, 'Khai v廙�', TRUE, 'Salad rau xanh t⑹①i m獺t k廕篙 h廙φ c獺 h廙𧗽 x繫ng kh籀i nh廕計 kh廕季.', 'https://file.hstatic.net/200000356095/file/salad_ca_hoi__xong_khoi__3__59eaf296ddc644849699579b210c7855.jpg', 'H廕ξ s廕τ', FALSE),
(5, 'S繳p H廕ξ S廕τ M�ng T璽y', 130000, 'Khai v廙�', TRUE, 'S繳p h廕ξ s廕τ n廕只 c羅ng m�ng t璽y t⑹①i ngon ng廙㦉.', 'https://cdn.tgdd.vn/Files/2020/10/09/1297483/tro-tai-voi-mon-sup-tom-mang-tay-vi-lau-thai-vua-la-vua-quen-ai-an-cung-tam-tac-khen-202010091742198445.jpg', 'H廕ξ s廕τ', FALSE),
(6, 'B獺nh M穫 B① T廙蒨', 65000, 'Khai v廙�', TRUE, 'B獺nh m穫 Ph獺p n⑹廙𣌟g gi簷n ph廕篙 b① t廙蒨 th①m l廙南g.', 'https://www.lorca.vn/wp-content/uploads/2021/10/Cach-lam-mong-banh-mi-bo-toi-phomai-bang-lo-nuong.jpg', 'Gluten, S廙畝', FALSE),
(7, 'Nem Chua R獺n', 75000, 'Khai v廙�', TRUE, 'Nem chua r獺n gi簷n r廙叮 ch廕叮 t⑹①ng 廙𣕧.', 'https://trumfood.vn/wp-content/uploads/2022/09/trumfood_decor00865.jpg', NULL, FALSE),
(8, 'Ho�nh Th獺nh Chi礙n Gi簷n', 85000, 'Khai v廙�', TRUE, 'Ho�nh th獺nh chi礙n gi簷n nh璽n t繫m th廙𡑕.', 'https://cdn.tgdd.vn/2020/09/CookProduct/Untitled-2-1200x676-1.jpg', 'Gluten, H廕ξ s廕τ', FALSE),
(9, 'B簷 B穩t T廕篙 Wagyu K癡m S廙𩾷 Ti礙u Xanh', 850000, 'M籀n ch穩nh', TRUE, 'B簷 Wagyu Nh廕負 B廕τ 獺p ch廕υ s廙𩾷 ti礙u xanh, k癡m rau c廙� n⑹廙𣌟g.', 'https://live.staticflickr.com/65535/50489573886_fa160b7292_b.jpg', NULL, FALSE),
(10, 'C獺 H廙𧗽 N⑹廙𣌟g S廙𩾷 Miso Nh廕負 B廕τ', 520000, 'M籀n ch穩nh', TRUE, 'C獺 h廙𧗽 t⑹①i n⑹廙𣌟g s廙𩾷 miso thanh nh廕�, �n k癡m c①m tr廕疸g.', 'https://www.theforkbite.com/wp-content/uploads/2024/02/Teriyaki-Salmon-featured-2.9.24-500x500.jpg', 'H廕ξ s廕τ', FALSE),
(11, 'Ph廙� B簷 Truy廙� Th廙𤉋g', 120000, 'M籀n ch穩nh', TRUE, 'Ph廙� b簷 n⑹廙𢲷 d羅ng �廕衫 ��, th廙𡑕 b簷 t獺i ch穩n m廙�.', 'https://daotaobeptruong.vn/wp-content/uploads/2020/03/cach-nau-pho-bo.jpg', NULL, FALSE),
(12, 'C①m Chi礙n D⑹①ng Ch璽u', 85000, 'M籀n ch穩nh', TRUE, 'C①m chi礙n d⑹①ng ch璽u th廕計 c廕姓 t繫m, l廕︾ x⑹廙俲g, tr廙姊g.', 'https://images.unsplash.com/photo-1603133872878-684f208fb84b?w=500&q=80', 'H廕ξ s廕τ, Tr廙姊g', FALSE),
(13, 'S⑹廙𩵚 Heo N⑹廙𣌟g BBQ', 250000, 'M籀n ch穩nh', TRUE, 'S⑹廙𩵚 heo non n⑹廙𣌟g s廙𩾷 BBQ �廕衫 v廙�, �n k癡m khoai t璽y chi礙n.', 'https://images.unsplash.com/photo-1544025162-d76694265947?w=500&q=80', NULL, FALSE),
(14, 'M穫 � S廙𩾷 B簷 B�m', 140000, 'M籀n ch穩nh', TRUE, 'M穫 � spaghetti s廙𩾷 bolognese th廙𡑕 b簷 b�m.', 'https://meoeva.com/wp-content/uploads/2019/05/Spaghetti.jpg', 'Gluten', FALSE),
(15, 'G� N⑹廙𣌟g M廕負 Ong', 180000, 'M籀n ch穩nh', TRUE, '�羅i g� n⑹廙𣌟g m廕負 ong th①m ng廙㦉, �n k癡m salad.', 'https://lh3.googleusercontent.com/p/AF1QipOYKsd6yLm0iH-NJGsbyVcSl6woaZT1DpHGG1LM=s680-w680-h510', NULL, FALSE),
(16, 'L廕季 Th獺i H廕ξ S廕τ', 350000, 'M籀n ch穩nh', TRUE, 'L廕季 Th獺i chua cay h廕ξ s廕τ t⑹①i s廙𤉋g, k癡m b繳n t⑹①i.', 'https://i.ytimg.com/vi/p1ejp7z4mc4/sddefault.jpg', 'H廕ξ s廕τ', FALSE),
(17, 'B繳n Ch廕� H� N廙耥', 95000, 'M籀n ch穩nh', TRUE, 'B繳n ch廕� H� N廙耥 th廙𡑕 n⑹廙𣌟g than h廙忛g, n⑹廙𢲷 m廕痂 chua ng廙㦉.', 'https://www.seriouseats.com/thmb/J0g7JWjk9r6CHESo1CIrD1BfGd0=/1500x0/filters:no_upscale():max_bytes(150000):strip_icc()/20231204-SEA-VyTran-BunChaHanoi-19-f623913c6ef34a9185bcd6e5680c545f.jpg', NULL, FALSE),
(18, 'Pizza H廕ξ S廕τ', 210000, 'M籀n ch穩nh', TRUE, 'Pizza �廕� m廙萏g nh璽n h廕ξ s廕τ ph繫 mai mozzarella.', 'https://doiduavang.vn/wp-content/uploads/2021/01/pizza-nhan-hai-san-doi-dua-vang-scaled.jpg', 'Gluten, H廕ξ s廕τ, S廙畝', FALSE),
(19, 'C①m G� H廕ξ Nam', 110000, 'M籀n ch穩nh', TRUE, 'C①m g� H廕ξ Nam n⑹廙𢲷 d羅ng g� thanh ng廙㦉.', 'https://cdn.tgdd.vn/Files/2021/08/16/1375575/cach-nau-com-ga-hai-nam-don-gian-ga-chin-vang-uom-da-gion-dung-chuan-202112281045139511.jpg', NULL, FALSE),
(20, 'M廙帷 廙酧g Nh廙𧗽 Th廙𡑕', 170000, 'M籀n ch穩nh', TRUE, 'M廙帷 廙𤉋g nh廙𧗽 th廙𡑕 chi礙n gi簷n, ch廕叮 s廙𩾷 t⑹①ng xo�i.', 'https://cdn.tgdd.vn/2021/03/CookProduct/1200-1200x676-31.jpg', 'H廕ξ s廕τ', FALSE),
(21, 'B獺nh Tiramisu Truy廙� Th廙𤉋g �', 120000, 'Tr獺ng mi廙𡵞g', FALSE, 'B獺nh Tiramisu � nguy礙n b廕τ v廙� c� ph礙, kem mascarpone.', 'https://thermomixvietnam.vn/wp-content/uploads/2021/08/tiramisu-truyen-thong.jpg', 'S廙畝, Gluten', FALSE),
(22, 'Ch癡 Xo�i D廙冠 T⑹①i', 65000, 'Tr獺ng mi廙𡵞g', TRUE, 'Ch癡 xo�i ch穩n ng廙㦉 k廕篙 h廙φ n⑹廙𢲷 c廙𩾷 d廙冠 b矇o ng廕軌.', 'https://img.freepik.com/premium-photo/mango-cheese-milka-dessert-made-from-jelly-nata-de-coco-basil-seed-mango-cream-cheese-milk_583400-4287.jpg', NULL, FALSE),
(23, 'Kem X繫i D廙冠', 55000, 'Tr獺ng mi廙𡵞g', TRUE, 'Kem x繫i d廙冠 m獺t l廕》h, topping d廙冠 n廕︽ s廕句.', 'https://beptruong.edu.vn/wp-content/uploads/2016/02/kem-xoi-dua.jpg', 'S廙畝', FALSE),
(24, 'B獺nh Flan Caramel', 45000, 'Tr獺ng mi廙𡵞g', TRUE, 'B獺nh flan caramel m廙� m廙𡃉, th①m ngon.', 'https://img.freepik.com/premium-photo/cream-caramel-pudding_599862-23796.jpg', 'Tr廙姊g, S廙畝', FALSE),
(25, 'Panna Cotta D璽u T璽y', 75000, 'Tr獺ng mi廙𡵞g', TRUE, 'Panna cotta � s廙𩾷 d璽u t璽y t⑹①i m獺t.', 'https://bloganchoi.com/wp-content/uploads/2022/06/cach-lam-panna-cotta.jpg', 'S廙畝', FALSE),
(26, 'Tr獺i C璽y Th廕計 C廕姓', 110000, 'Tr獺ng mi廙𡵞g', TRUE, '�藺a tr獺i c璽y t⑹①i th廕計 c廕姓 theo m羅a.', 'https://bolcereales.com.ar/wp-content/uploads/2021/01/alimentos-con-cobre-frutas.jpeg', NULL, FALSE),
(27, 'B獺nh Mousse Chocolate', 90000, 'Tr獺ng mi廙𡵞g', TRUE, 'B獺nh mousse chocolate B廙� m廙𡃉 m�ng, �廕疸g nh廕�.', 'https://i.ytimg.com/vi/pESVrDm6yIM/maxresdefault.jpg', 'S廙畝, Tr廙姊g', FALSE),
(28, 'N⑹廙𢲷 Cam T⑹①i �p L廕》h', 95000, '�廙� u廙𤉋g', TRUE, 'N⑹廙𢲷 cam t⑹①i nguy礙n ch廕另 矇p l廕》h.', 'https://www.sieuthidonglanh.com/wp-content/uploads/2023/03/Nuoc-ep-cam-giup-chong-lao-hoa-da-hieu-qua.png', NULL, TRUE),
(29, 'C� Ph礙 Phin Vi廙峼 Nam', 55000, '�廙� u廙𤉋g', TRUE, 'C� ph礙 phin Vi廙峼 Nam �廕衫 �� truy廙� th廙𤉋g.', 'https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=500&q=80', NULL, TRUE),
(30, 'Tr� ��o Cam S廕�', 65000, '�廙� u廙𤉋g', TRUE, 'Tr� ��o cam s廕� t⑹①i m獺t gi廕ξ nhi廙峼.', 'https://img.meta.com.vn/Data/image/2021/05/20/tra-dao-cam-sa-2.jpg', NULL, TRUE),
(31, 'Sinh T廙� B①', 75000, '�廙� u廙𤉋g', TRUE, 'Sinh t廙� b① t⑹①i xay nhuy廙� b矇o ng廕軌.', 'https://img.freepik.com/premium-photo/avocado-smoothie-with-avocado-wooden-board-dark-background_490636-2675.jpg', 'S廙畝', TRUE),
(32, 'Mojito Chanh B廕︷ H�', 85000, '�廙� u廙𤉋g', TRUE, 'Mojito kh繫ng c廙忛 chanh b廕︷ h� t⑹①i m獺t.', 'https://img.freepik.com/premium-photo/refreshing-glass-coconut-water-garnished-with-mint-leaves-slice-lime_198067-290311.jpg', NULL, TRUE),
(33, 'Bia Heineken', 45000, '�廙� u廙𤉋g', TRUE, 'Bia Heineken nh廕計 kh廕季 l廕》h.', 'https://tse2.mm.bing.net/th/id/OIP.h3ptwUpaWsyCB7MAVaLGhwHaEK?w=680&h=382&rs=1&pid=ImgDetMain&o=7&rm=3', 'Gluten', TRUE),
(34, 'N⑹廙𢲷 Kho獺ng Evian', 40000, '�廙� u廙𤉋g', TRUE, 'N⑹廙𢲷 kho獺ng Evian Ph獺p.', 'https://gangnamkong.co.kr/web/upload/NNEditor/20230424/33378a1a3af8ff1869c4b1faf8da8863.jpg', NULL, TRUE);

-- ���� 18.5 Menu Item Days (Ph璽n b廙� th廙帷 �①n theo ng�y) ����������������������
INSERT INTO Menu_Item_Days (item_id, day_of_week) VALUES
-- MONDAY (Th廙� 2)
(1, 'MONDAY'), (2, 'MONDAY'), (5, 'MONDAY'), (8, 'MONDAY'),
(9, 'MONDAY'), (10, 'MONDAY'), (13, 'MONDAY'), (16, 'MONDAY'),
(21, 'MONDAY'), (22, 'MONDAY'), (25, 'MONDAY'), (27, 'MONDAY'),

-- TUESDAY (Th廙� 3)
(3, 'TUESDAY'), (4, 'TUESDAY'), (6, 'TUESDAY'), (7, 'TUESDAY'),
(11, 'TUESDAY'), (12, 'TUESDAY'), (14, 'TUESDAY'), (17, 'TUESDAY'),
(23, 'TUESDAY'), (24, 'TUESDAY'), (26, 'TUESDAY'), (22, 'TUESDAY'),

-- WEDNESDAY (Th廙� 4)
(1, 'WEDNESDAY'), (3, 'WEDNESDAY'), (5, 'WEDNESDAY'), (8, 'WEDNESDAY'),
(15, 'WEDNESDAY'), (18, 'WEDNESDAY'), (19, 'WEDNESDAY'), (20, 'WEDNESDAY'),
(21, 'WEDNESDAY'), (23, 'WEDNESDAY'), (25, 'WEDNESDAY'), (27, 'WEDNESDAY'),

-- THURSDAY (Th廙� 5)
(2, 'THURSDAY'), (4, 'THURSDAY'), (6, 'THURSDAY'), (7, 'THURSDAY'),
(9, 'THURSDAY'), (12, 'THURSDAY'), (16, 'THURSDAY'), (18, 'THURSDAY'),
(22, 'THURSDAY'), (24, 'THURSDAY'), (26, 'THURSDAY'), (21, 'THURSDAY'),

-- FRIDAY (Th廙� 6)
(1, 'FRIDAY'), (2, 'FRIDAY'), (3, 'FRIDAY'), (4, 'FRIDAY'),
(10, 'FRIDAY'), (11, 'FRIDAY'), (14, 'FRIDAY'), (20, 'FRIDAY'),
(21, 'FRIDAY'), (23, 'FRIDAY'), (25, 'FRIDAY'), (27, 'FRIDAY'),

-- SATURDAY (Th廙� 7)
(5, 'SATURDAY'), (6, 'SATURDAY'), (7, 'SATURDAY'), (8, 'SATURDAY'),
(9, 'SATURDAY'), (13, 'SATURDAY'), (16, 'SATURDAY'), (19, 'SATURDAY'),
(22, 'SATURDAY'), (24, 'SATURDAY'), (26, 'SATURDAY'), (25, 'SATURDAY'),

-- SUNDAY (Ch廙� Nh廕負)
(1, 'SUNDAY'), (2, 'SUNDAY'), (3, 'SUNDAY'), (4, 'SUNDAY'),
(10, 'SUNDAY'), (12, 'SUNDAY'), (15, 'SUNDAY'), (18, 'SUNDAY'),
(21, 'SUNDAY'), (23, 'SUNDAY'), (26, 'SUNDAY'), (27, 'SUNDAY');

-- ���� 19. Food Orders (19 rows) ����������������������������������������������������������������
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

-- ���� 20. Food Order Details (21 rows) ��������������������������������������������������
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

-- ���� 21. Hotel Services (10 rows) ����������������������������������������������������������
INSERT INTO Hotel_Services (service_id, service_name, base_price, source_department, is_available, description) VALUES 
(1, '�籀n ti廙� s璽n bay b廕彫g xe Limousine', 800000, 'TRANSPORTATION', TRUE, 'Xe Limousine 9 ch廙� �籀n �⑹a s璽n bay sang tr廙㤔g.'),
(3, 'Gi廕暗 s廕句 qu廕吵 獺o l廕句 nhanh', 150000, 'LAUNDRY', TRUE, 'Gi廕暗 h廕叼 s廕句 kh繫 qu廕吵 獺o giao tr廕� trong 4 gi廙�.'),
(4, 'Decor ph簷ng t璽n h繫n l瓊ng m廕》', 500000, 'FLORIST', TRUE, 'Trang tr穩 ph簷ng b廕彫g hoa t⑹①i h廙忛g �廙� v� n廕積 th①m n廙忛g n�n.'),
(5, 'Thu礙 xe m獺y tay ga t廙� l獺i', 200000, 'TRANSPORTATION', TRUE, 'Thu礙 xe ga Honda Vision 110cc t廙� l獺i kh獺m ph獺 �廕υ ng廙㷼.'),
(8, 'Gi廕暗 kh繫 �廙� vest/�廕吮 d廕� h廙耥', 250000, 'LAUNDRY', TRUE, 'Gi廕暗 kh繫 l� h①i �廙� vest v� v獺y c⑹廙𢹂 cao c廕叼.'),
(9, 'B籀 hoa t⑹①i ch繳c m廙南g sinh nh廕負', 600000, 'FLORIST', TRUE, 'B籀 hoa h⑹廙𣌟g d⑹①ng k廕篙 h廙φ hoa h廙忛g t⑹①i r廙帷 r廙�.'),
(10, 'Thu礙 xe 繫 t繫 7 ch廙� k癡m t�i x廕�', 1500000, 'TRANSPORTATION', TRUE, 'Thu礙 xe Toyota Fortuner �i tham quan �廕υ tr廙㤔 ng�y.');

-- ���� 22. Booking Services (10 rows) ������������������������������������������������������
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

-- ���� 23. Hotel Operations (10 rows) ������������������������������������������������������
INSERT INTO Hotel_Operations (task_id, room_id, staff_id, supervisor_id, operational_type, priority, status, created_at, started_at, completed_at, notes) VALUES 
-- HOUSEKEEPING (CHECKOUT_CLEAN)
(1, 2, 8, 4, 'CHECKOUT_CLEAN', 'High', 'Pending', '2026-06-28 08:00:00', NULL, NULL, '[Check-out] Kh獺ch ph簷ng 102 v廙冠 tr廕� ph簷ng, d廙㤔 g廕叼 �廙� �籀n �o�n 2h chi廙�.'),
(2, 4, 9, 4, 'CHECKOUT_CLEAN', 'Normal', 'Pending', '2026-06-28 09:00:00', NULL, NULL, '[Check-out] D廙㤔 d廕雷 s廕︷h s璽u, thay to�n b廙� ga gi⑹廙𩵚g v� x廙𡑕 th①m ph簷ng.'),
(3, 8, 8, 4, 'CHECKOUT_CLEAN', 'Normal', 'InProgress', '2026-06-28 09:30:00', '2026-06-28 10:15:00', NULL, '[Stay-over] Kh獺ch y礙u c廕吟 th礙m 2 kh�n t廕痂 v� 1 chai n⑹廙𢲷 su廙魀.'),
(4, 13, 9, 4, 'CHECKOUT_CLEAN', 'High', 'InProgress', '2026-06-28 10:00:00', '2026-06-28 10:20:00', NULL, '[Arrival] Kh獺ch VIP s廕皰 nh廕要 ph簷ng, chu廕姊 b廙� s廕登 gi廙� tr獺i c璽y t⑹①i tr礙n b�n.'),
(5, 1, 9, 4, 'CHECKOUT_CLEAN', 'Normal', 'Completed', '2026-06-28 07:00:00', '2026-06-28 07:15:00', '2026-06-28 08:45:00', '[Check-out] �瓊 d廙㤔 xong, ph獺t hi廙𡵞 qu礙n m廙脌 chi廕盧 s廕︷ �i廙𡵞 tho廕【 tr礙n b�n.'),

-- MAINTENANCE (MAINTENANCE)
(6, 17, 10, 4, 'MAINTENANCE', 'High', 'Pending', '2026-06-28 10:30:00', NULL, NULL, 'Housekeeping b獺o: �i廙� h簷a ch廕ㄊ n⑹廙𢲷 ⑹廙𣕧 c廕� s�n g廙�, ph簷ng 310.'),
(7, 3, 10, 4, 'MAINTENANCE', 'Normal', 'Pending', '2026-06-28 12:00:00', NULL, NULL, 'Kh獺ch ph�n n�n: V簷i hoa sen b廙� ngh廕靖, n⑹廙𢲷 ch廕ㄊ r廕另 y廕簑.'),
(8, 5, 10, 4, 'MAINTENANCE', 'Normal', 'InProgress', '2026-06-28 13:00:00', '2026-06-28 13:15:00', NULL, 'Ki廙� tra h廙� th廙𤉋g �癡n ban c繫ng, 1 b籀ng b廙� ch獺y.'),
(9, 10, 10, 4, 'MAINTENANCE', 'High', 'Paused', '2026-06-28 09:00:00', '2026-06-28 09:10:00', NULL, 'S廙苔 k矇t s廕眩 kh繫ng m廙� �⑹廙θ. \n[T廕《 d廙南g]: Ch廙� mua pin m廙𢹂 lo廕【 9V �廙� thay m廕τg m廕︷h.'),
(10, 16, 10, 4, 'MAINTENANCE', 'Low', 'Completed', '2026-06-28 08:00:00', '2026-06-28 08:05:00', '2026-06-28 08:20:00', 'Thay pin tay n廕痂 c廙苔 ph簷ng 309. \n[�瓊 s廙苔]: �瓊 thay 4 c廙卉 pin AA Panasonic.');

-- ���� 24. Folio Items (10 rows) ����������������������������������������������������������������
INSERT INTO Folio_Items (folio_item_id, booking_id, room_booking_detail_id, payer_customer_id, source_department, amount, description, is_settled_separately, created_by_staff_id, created_at, signature_img_url) VALUES 
(2, 1, 1, 1, 'F&B', 180000, 'S繳p B穩 �廙� Truffle Room Service', FALSE, 2, CURRENT_TIMESTAMP, NULL),
(3, 2, 2, 2, 'TRANSPORTATION', 800000, 'Xe �籀n ti廙� Limousine s璽n bay', FALSE, 4, CURRENT_TIMESTAMP, NULL),
(5, 4, 4, 4, 'LAUNDRY', 150000, 'Gi廕暗 s廕句 qu廕吵 獺o l廕句 nhanh', FALSE, 4, CURRENT_TIMESTAMP, NULL),
(6, 5, 5, 5, 'TRANSPORTATION', 200000, 'Thu礙 xe m獺y Honda Vision t廙� l獺i', FALSE, 4, CURRENT_TIMESTAMP, NULL),
(7, 6, 6, 6, 'F&B', 350000, 'L廕季 Th獺i H廕ξ S廕τ t廕【 ph簷ng', FALSE, 2, CURRENT_TIMESTAMP, NULL),
(8, 7, 7, 7, 'FLORIST', 500000, 'Trang tr穩 ph簷ng tr�ng m廕負', FALSE, 4, CURRENT_TIMESTAMP, NULL),
(10, 14, 9, 12, 'TRANSPORTATION', 800000, '�籀n ti廙� s璽n bay Limousine', FALSE, 4, CURRENT_TIMESTAMP, NULL);

-- ���� 25. Consolidated Invoices (10 rows) ��������������������������������������������
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

-- ���� 26. Payment Transactions (10 rows) ����������������������������������������������
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

-- ���� 27. Tours (10 rows) ����������������������������������������������������������������������������
INSERT INTO Tours (tour_id, tour_name, tour_type, duration, base_price, max_capacity, short_quote, description, created_at, is_active) VALUES 
(1, '�o�n t廙� - Hu廕�', 'doantu', '7 Gi廙�', 50000, 20, 'T穫m v廙� h①i 廕叮 v廕雉 nguy礙n c廙吧 l簷ng bi廕篙 ①n v� s廙� g廕疸 k廕篙.', 'H�nh tr穫nh di s廕τ c廙� �繫 Hu廕� tr廕ξ nghi廙寛 v�n ho獺 廕姓 th廙帷 cung �穫nh Hu廕�.', CURRENT_TIMESTAMP, TRUE),
(2, 'Tinh t繳y �廙忛g n廙耥 - Qu廕τg Nam', 'dongnoi', '7 Gi廙�', 70000, 15, 'L廕疸g nghe nh廙𠹸 �i廙杮 m廙緽 m廕︷ c廙吧 �廕另 m廕� v� h廙忛 qu礙 x廙� Qu廕τg.', 'Tham quan ph廙� c廙� H廙耥 An, l�ng rau Tr� Qu廕� v� l�ng g廙鮎 Thanh H�.', CURRENT_TIMESTAMP, TRUE),
(3, 'Di s廕τ th廙� c繫ng - Ninh B穫nh', 'disan', '7 Gi廙�', 50000, 15, 'Ch廕《 v�o h廙忛 c廙𩾷 c廙吧 th廙𩥉 gian qua nh廙疸g t廕︽ t獺c t廙� �繫i b�n tay ngh廙� nh璽n.', 'Kh獺m ph獺 �廕吮 V璽n Long v� l�ng ngh廙� th礙u ren truy廙� th廙𤉋g V�n L璽m.', CURRENT_TIMESTAMP, TRUE),
(4, 'T藺nh l廕搖g li礙n hoa - Th獺p M⑹廙𩥉', 'tinhlang', '8 Gi廙�', 70000, 10, 'S廙� thanh l廙㷼 thu廕吵 khi廕篙 cho th璽n - t璽m - tr穩 gi廙畝 v羅ng s繫ng n⑹廙𢲷 m廙� s⑹①ng.', '�i xu廙忛g ba l獺 ng廕痂 sen n廙� r廙� �廙忛g Th獺p M⑹廙𩥉 thi廙� �廙𡃉h th⑹ th獺i.', CURRENT_TIMESTAMP, TRUE),
(5, 'Huy廙� tho廕【 v廙𡃉h xanh - H廕� Long', 'Half-Day', '4 Gi廙�', 10000, 30, 'Du thuy廙� sang tr廙㤔g ng廕痂 k廙� quan thi礙n nhi礙n th廕� gi廙𢹂.', 'L⑹廙𣕧 s籀ng v廙𡃉h B廕畚 B廙� ng廕痂 �廙㷌g Thi礙n Cung ho�nh tr獺ng.', CURRENT_TIMESTAMP, TRUE),
(6, 'B穫nh y礙n b廕τ nh廙� - Sapa', 'Full-Day', '8 Gi廙�', 13000, 20, 'G廕搆 g廙� n廙� c⑹廙𩥉 h廙忛 h廕赴 v羅ng cao m璽y ph廙�.', 'Leo ru廙㷌g b廕苞 thang b廕τ C獺t C獺t tr廕ξ nghi廙寛 v�n ho獺 �廙忛g b�o H�联繫ng.', CURRENT_TIMESTAMP, TRUE),
(7, 'Nh廙𠹸 �廕計 hoang d瓊 - C獺t Ti礙n', 'Full-Day', '14 Gi廙�', 22000, 12, 'L廕疸g nghe ti廕積g g廙幂 r廙南g xanh th廕軛 huy廙� b穩.', 'Xem th繳 ban �礙m r廙南g Nam C獺t Ti礙n ng廕痂 chim mu繫ng k穫 th繳.', CURRENT_TIMESTAMP, TRUE),
(8, 'B穫nh minh c廙忛 c獺t - M觼i N矇', 'Half-Day', '5 Gi廙�', 9000, 15, 'Tr⑹廙ㄅ c獺t �籀n m廕暗 tr廙𩥉 m廙㷼 r廙帷 r廙�.', 'Kh獺m ph獺 �廙𧗽 C獺t Tr廕疸g, �廙𧗽 C獺t �廙� M觼i N矇 b廕彫g xe �廙卟 h穫nh ATV.', CURRENT_TIMESTAMP, TRUE),
(9, 'S籀ng h獺t san h繫 - Ph繳 Qu廙倴', 'Half-Day', '6 Gi廙�', 14000, 25, 'Ho� m穫nh v�o l�n n⑹廙𢲷 xanh l廙卉 b廕υ 籀ng 獺nh.', 'L廕搖 cano 4 �廕υ ng廕痂 san h繫 thi礙n nhi礙n r廙帷 r廙� Ph繳 Qu廙倴.', CURRENT_TIMESTAMP, TRUE),
(10, 'H⑹①ng s廕畚 mi廙峼 v⑹廙𩵚 - C廕吵 Th①', 'Half-Day', '5 Gi廙�', 8000, 20, 'Ng廙㦉 l廙𢞵 tr獺i ch穩n tr藺u c�nh mi廙� T璽y s繫ng n⑹廙𢲷.', '�i ch廙� n廙飃 C獺i R�ng th⑹廙俲g th廙妾 b廙畝 s獺ng tr礙n ghe thuy廙� m廙緽 m廕︷.', CURRENT_TIMESTAMP, TRUE);

-- ���� 28. Tour Images (10 rows) ����������������������������������������������������������������
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

-- ���� 29. Tour Locations (10 rows) ����������������������������������������������������������
INSERT INTO Tour_Locations (location_id, location_name, latitude, longitude, description, is_active) VALUES 
(1, '�廕【 N廙耥 Hu廕�', 16.4678, 107.5789, 'Ho�ng cung tri廙� Nguy廙� c廙� k穩nh, uy nghi礙m.', TRUE),
(2, 'Ph廙� c廙� H廙耥 An', 15.8801, 108.3380, 'Di s廕τ v�n ho獺 th廕� gi廙𢹂 th⑹①ng c廕τg �癡n l廙忛g r廙帷 r廙�.', TRUE),
(3, '�廕吮 V璽n Long', 20.3621, 105.9087, 'Khu b廕υ t廙忛 thi礙n nhi礙n ng廕計 n⑹廙𢲷 l廙𣌟 nh廕另 v廙𡃉h B廕畚 B廙�.', TRUE),
(4, '�廙忛g Th獺p M⑹廙𩥉', 10.4500, 105.7000, 'X廙� s廙� sen h廙忛g ng�o ng廕﹀ ng繳t ng�n t廕吮 m廕眩.', TRUE),
(5, '�廕υ Ti T廙鯿 H廕� Long', 20.8654, 107.0812, 'B瓊i t廕痂 c獺t tr廕疸g tuy廙峼 �廕雷 t廙帶 l⑹ng v獺ch �獺.', TRUE),
(6, 'B廕τ C獺t C獺t Sapa', 22.3289, 103.8415, 'B廕τ l�ng m廙緽 m廕︷ b礙n th獺c n⑹廙𢲷 m獺t r⑹廙ξ.', TRUE),
(7, 'B�u S廕只 C獺t Ti礙n', 11.4589, 107.3654, 'V羅ng �廕吮 l廕囤 b廕υ t廙忛 c獺 s廕只 t廙� nhi礙n qu羸 hi廕禦.', TRUE),
(8, '�廙𧗽 C獺t Tr廕疸g M觼i N矇', 10.9500, 108.2800, 'Ti廙� sa m廕︷ c獺t m礙nh m繫ng l廕叼 l獺nh 獺nh v�ng.', TRUE),
(9, 'H簷n Th①m Ph繳 Qu廙倴', 9.9543, 104.0152, 'Thi礙n �⑹廙𩵚g �廕υ ng廙㷼 c獺t tr廕疸g n⑹廙𢲷 trong v廕眩.', TRUE),
(10, 'Ch廙� n廙飃 C獺i R�ng', 9.9989, 105.7489, 'Ch廙� �廕吟 m廙魀 mua b獺n tr獺i c璽y s廕吮 u廕另 tr礙n s繫ng.', TRUE);

-- ���� 30. Tour Itineraries (10 rows) ������������������������������������������������������
INSERT INTO Tour_Itineraries (itinerary_id, tour_id, day_number, day_title, summary) VALUES 
(1, 1, 1, 'H�nh tr穫nh C廙� �繫', 'Kh獺m ph獺 �廕【 N廙耥 Hu廕� v� l�ng t廕姓 ho�ng cung tri廙� Nguy廙�.'),
(2, 2, 1, 'H廙忛 Qu礙 X廙� Qu廕τg', 'Tham quan H廙耥 An c廙� k穩nh v� tr廕ξ nghi廙寛 c�y c廕句 l�ng Tr� Qu廕�.'),
(3, 3, 1, 'H廙忛 �廕另 V�n L璽m', '�i thuy廙� V璽n Long ng廕痂 c廕τh s①n thu廙� v� xem d廙峼 th礙u th廙� c繫ng.'),
(4, 4, 1, 'Thi廙� Gi廙畝 H⑹①ng Sen', 'Ng廕痂 sen n廙� m廙� s⑹①ng v� t廕計 yoga thi廙� tr礙n s繫ng n⑹廙𢲷.'),
(5, 5, 1, 'V廙𡃉h Xanh K穫 V藺', 'T�u sang l⑹廙𣕧 s籀ng ng廕痂 hang lu廙忛, h簷n tr廙𤉋g m獺i.'),
(6, 6, 1, 'Sapa M璽y M羅', 'Th�m b廕τ H�联繫ng c廙� x⑹a, t穫m hi廙� ngh廙� thu廕負 nhu廙联 ch�m.'),
(7, 7, 1, 'Kh獺m Ph獺 R廙南g Xanh', '�i b廙� xuy礙n r廙南g ng廕痂 b廕彫g l�ng c廙� th廙� k穫 v藺.'),
(8, 8, 1, 'C獺t V�ng M觼i N矇', 'Ng廕痂 b穫nh minh c廙忛 c獺t tr廕疸g, tham quan Su廙魀 Ti礙n.'),
(9, 9, 1, '�廕【 D⑹①ng Ph繳 Qu廙倴', 'L廕搖 cano 4 �廕υ nh廙� hoang s① ho� v�o san h繫 r廙帷 r廙�.'),
(10, 10, 1, 'S繫ng N⑹廙𢲷 C廕吵 Th①', '�n s獺ng h廙� ti廕簑 ch廙� n廙飃 C獺i R�ng s繫i �廙㷌g.');

-- ���� 31. Tour Itinerary Details (10 rows) ������������������������������������������
INSERT INTO Tour_Itinerary_Details (detail_id, itinerary_id, start_time, end_time, location_id, activity_title, activity_description, meal_type) VALUES 
(1, 1, '08:00:00', '11:30:00', 1, 'Tham quan �廕【 N廙耥', 'Chi礙m ng⑹廙》g Ng廙� M繫n, �i廙𡵞 Th獺i Ho� v� nghe thuy廕篙 minh ho�ng tri廙�.', NULL),
(2, 2, '09:00:00', '12:00:00', 2, 'D廕︽ b⑹廙𢲷 ph廙� c廙�', 'Th�m Ch羅a C廕吟, h廙耥 qu獺n Qu廕τg �繫ng l廕叼 l獺nh �癡n l廙忛g.', 'Lunch'),
(3, 3, '14:00:00', '17:00:00', 3, 'Du thuy廙� V璽n Long', '�i thuy廙� nan ng廕痂 ��n vo廙㷼 qu廕吵 �羅i tr廕疸g chuy廙� c�nh v獺ch �獺.', NULL),
(4, 4, '06:00:00', '08:30:00', 4, 'Thi廙� h�nh �籀n n廕疸g', 'T廕計 yoga t藺nh t璽m tr礙n nh� ch簷i g廙� gi廙畝 �廕吮 sen l廙㷌g gi籀.', 'Breakfast'),
(5, 5, '08:30:00', '11:30:00', 5, 'Kh獺m ph獺 hang �廙㷌g', 'Tham quan �廙㷌g Thi礙n Cung ho�nh tr獺ng th廕︷h nh觼 k穫 v藺.', NULL),
(6, 6, '09:00:00', '12:00:00', 6, 'Th�m b廕τ C獺t C獺t', '�i b廙� ng廕痂 c廙魀 xay n⑹廙𢲷 kh廙葓g l廙� v� check-in th獺c Ti礙n Sa.', 'Lunch'),
(7, 7, '08:00:00', '15:00:00', 7, 'Trekking B�u S廕只', '�i b廙� 5km xuy礙n r廙南g r廕衫 Nam C獺t Ti礙n �廕積 �廕吮 l廕囤 b廕υ t廙忛.', 'Lunch'),
(8, 8, '05:30:00', '08:30:00', 8, '�籀n b穫nh minh c廙忛 c獺t', 'Tr⑹廙ㄅ c獺t �廙𧗽 c獺t tr廕疸g b廕彫g m獺ng tr⑹廙ㄅ v� m繫 t繫 ATV ch廕『 �廙卟 h穫nh.', NULL),
(9, 9, '09:00:00', '15:00:00', 9, 'L廕搖 ng廕痂 san h繫', 'T廕痂 bi廙� H簷n Th①m l廕搖 bi廙� 廙𤉋g th廙� ng廕痂 r廕》 san h繫 t廙� nhi礙n �廕雷 nh廕另 Ph繳 Qu廙倴.', 'Lunch'),
(10, 10, '06:00:00', '08:30:00', 10, 'Ch廙� n廙飃 C獺i R�ng', 'L礙n thuy廙� ng廕痂 ch廙� n廙飃 nh廙㷌 nh廙𠹸, �n h廙� ti廕簑 n籀ng h廙飃 ch簷ng ch�nh tr礙n s繫ng.', 'Breakfast');

-- ���� 32. Tour Prices (10 rows) ����������������������������������������������������������������
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

-- ���� 33. Tour Schedules (10 rows) ����������������������������������������������������������
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

-- ���� 34. Tour Staff Assignments (10 rows) ������������������������������������������
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

-- ���� 35. Run Itinerary Status (10 rows) ����������������������������������������������
INSERT INTO Run_Itinerary_Status (run_status_id, schedule_id, detail_id, actual_start_time, actual_end_time, current_stage_status, guide_notes) VALUES 
(1, 5, 2, '2026-06-13 09:05:00', '2026-06-13 12:10:00', 'COMPLETED', 'L⑹廙τg xe c廙� H廙耥 An �繫ng, �o�n di chuy廙� ch廕衫 5 ph繳t.'),
(2, 1, 1, NULL, NULL, 'NOT_STARTED', NULL),
(3, 2, 2, NULL, NULL, 'NOT_STARTED', NULL),
(4, 3, 3, NULL, NULL, 'NOT_STARTED', NULL),
(5, 4, 4, NULL, NULL, 'NOT_STARTED', NULL),
(6, 5, 2, NULL, NULL, 'NOT_STARTED', NULL),
(7, 6, 5, NULL, NULL, 'NOT_STARTED', NULL),
(8, 7, 6, NULL, NULL, 'NOT_STARTED', NULL),
(9, 8, 7, NULL, NULL, 'NOT_STARTED', NULL),
(10, 10, 10, NULL, NULL, 'NOT_STARTED', NULL);

-- ���� 36. Tour Bookings (10 rows) ������������������������������������������������������������
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

-- ���� 37. Tour Attendees (10 rows) ����������������������������������������������������������
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

-- ���� 38. Checkpoint Attendance (10 rows) ��������������������������������������������
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

-- ���� 39. Reviews (10 rows) ������������������������������������������������������������������������
INSERT INTO Reviews (review_id, customer_id, room_booking_detail_id, tour_booking_id, rating_service, review_text, moderation_status, moderated_by, moderation_reason, created_at) VALUES 
(1, 1, 1, NULL, 5, 'Ph簷ng Nipa Villa tuy廙峼 h廕υ, m獺t m廕�, nh璽n vi礙n bu廙忛g d廙㤔 r廕另 s廕︷h.', 'Approved', 4, '�獺nh gi獺 t穩ch c廙帷 h廙φ l廙�', CURRENT_TIMESTAMP),
(2, 2, 2, NULL, 4, 'Ph簷ng River Villa �廕雷, view s繫ng th① m廙㷌g, �廙� �n room service h①i ch廕衫.', 'Approved', 4, '�獺nh gi獺 x璽y d廙彫g h廙φ l廙�', CURRENT_TIMESTAMP),
(3, 3, 3, NULL, 5, 'Kh籀a tu Wellness Retreats gi繳p t繫i t廙𡃉h t璽m, ph廙卉 h廙𧗽 s廙妾 kho廕� r廕另 nhi廙�.', 'Approved', 4, '�獺nh gi獺 t廙𩾷 ch廕另 l⑹廙τg cao', CURRENT_TIMESTAMP),
(4, 4, 4, NULL, 4, 'Ph簷ng 104 s廕︷h s廕�, b廙忛 t廕痂 r廙㷌g r瓊i, decor bu廙忛g c⑹廙𢹂 r廕另 t廙� m廙�.', 'Approved', 4, '�獺nh gi獺 t廙𩾷', CURRENT_TIMESTAMP),
(5, 5, 5, NULL, 5, 'R廕另 h�i l簷ng v廙𢹂 k廙� ngh廙� t廕【 resort, b瓊i c廙� xanh ng獺t, �廙� �n buffet ngon.', 'Approved', 4, '�獺nh gi獺 t廙𩾷', CURRENT_TIMESTAMP),
(6, 6, 6, NULL, 4, 'C廕τh quan xanh m獺t, ph簷ng 207 view h廙� b①i r廙㷌g r瓊i tuy廙峼 v廙𩥉.', 'Approved', 4, '�獺nh gi獺 t廙𩾷', CURRENT_TIMESTAMP),
(7, 7, 7, NULL, 5, 'H廙耥 An Tour do HDV H⑹廙𣌟g D廕南 thuy廕篙 minh r廕另 sinh �廙㷌g, xe �i 礙m.', 'Approved', 4, '�獺nh gi獺 t廙𩾷', CURRENT_TIMESTAMP),
(8, 8, 8, NULL, 5, 'B廙畝 t廙魀 Wagyu t廕【 nh� h�ng c廙帷 ngon, th廙𡑕 m廙� m廙㤔g s廙𩾷 ti礙u th①m.', 'Approved', 4, '�獺nh gi獺 t廙𩾷', CURRENT_TIMESTAMP),
(10, 13, 10, NULL, 4, 'L廙醶h tr穫nh tr①n tru, nh璽n vi礙n th璽n thi廙𡵞 hi廕簑 kh獺ch nhi廙峼 t穫nh.', 'Approved', 4, '�獺nh gi獺 t廙𩾷', CURRENT_TIMESTAMP);

-- ���� 40. Authorized Devices (10 rows) ��������������������������������������������������
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

-- ���� 41. Audit Logs (10 rows) ������������������������������������������������������������������
INSERT INTO Audit_Logs (log_id, account_id, action, table_name, record_id, old_value, new_value, ip_address, timestamp) VALUES 
(1, 1, 'UPDATE_ROOM_STATUS', 'Rooms', 1, 'Vacant_Clean', 'Occupied', '192.168.1.10', CURRENT_TIMESTAMP),
(2, 1, 'CREATE_BOOKING', 'Bookings', 14, NULL, 'New Booking Created', '192.168.1.10', CURRENT_TIMESTAMP),
(3, 4, 'CHECKIN_GUEST', 'Room_Guests', 1, NULL, 'Guest Checked In Room 101', '192.168.1.15', CURRENT_TIMESTAMP),
(4, 2, 'ORDER_FOOD', 'Food_Orders', 19, NULL, 'New Room Service Order Created', '192.168.1.22', CURRENT_TIMESTAMP),
(5, 4, 'APPROVE_DEVICE', 'Authorized_Devices', 2, 'false', 'true', '192.168.1.10', CURRENT_TIMESTAMP),
(6, 13, 'START_TOUR', 'Run_Itinerary_Status', 1, 'NOT_STARTED', 'COMPLETED', '192.168.1.5', CURRENT_TIMESTAMP),
(7, 4, 'MODERATE_REVIEW', 'Reviews', 1, 'Pending', 'Approved', '192.168.1.10', CURRENT_TIMESTAMP),
(9, 4, 'UPDATE_INVOICE', 'Consolidated_Invoices', 1, 'Draft', 'Settled', '192.168.1.10', CURRENT_TIMESTAMP);

-- ���� 42. Test Accounts cho kh獺ch ��ng nh廕計 test ������������������������������
-- password: admin123  |  hash: $2a$10$4bnThA4xQVw1rF2POQv78uAQll2KsUsgF32JaYiVG5d2d3Fhgk83q
INSERT IGNORE INTO Accounts (account_id, username, password_hash, is_active, role_id, created_at) VALUES
(21, 'testguest1', '$2a$10$4bnThA4xQVw1rF2POQv78uAQll2KsUsgF32JaYiVG5d2d3Fhgk83q', TRUE, 10, CURRENT_TIMESTAMP),
(22, 'testguest2', '$2a$10$4bnThA4xQVw1rF2POQv78uAQll2KsUsgF32JaYiVG5d2d3Fhgk83q', TRUE, 10, CURRENT_TIMESTAMP),
(23, 'testguest3', '$2a$10$4bnThA4xQVw1rF2POQv78uAQll2KsUsgF32JaYiVG5d2d3Fhgk83q', TRUE, 10, CURRENT_TIMESTAMP);

-- ���� 43. Test Customers li礙n k廕篙 Account ��������������������������������������������
INSERT IGNORE INTO Customers (customer_id, account_id, full_name, gender, cccd_passport_encrypted, phone, email, loyalty_points, membership_tier) VALUES
(16, 21, 'Nguy廙� Minh Test', 'Nam', 'CCCD_TEST01', '0911000001', 'testguest1@test.com', 50, 'Regular'),
(17, 22, 'Tr廕吵 Th廙� Test', 'N廙�', 'CCCD_TEST02', '0911000002', 'testguest2@test.com', 100, 'Silver'),
(18, 23, 'L礙 V�n Test', 'Nam', 'CCCD_TEST03', '0911000003', 'testguest3@test.com', 200, 'Gold');

-- ���� 44. Test Bookings (Confirmed + Checked_In) ����������������������������
INSERT IGNORE INTO Bookings (booking_id, customer_id, booking_date, total_price, booking_status, booking_source, applied_promotion_id, version) VALUES
(21, 16, '2026-06-14', 5000000, 'Checked_In', 'Direct_Web', NULL, 1),
(22, 17, '2026-06-15', 7000000, 'Checked_In', 'Direct_Web', NULL, 1),
(23, 18, '2026-06-10', 16000000, 'Confirmed', 'Direct_Web', NULL, 1);

-- ���� 45. Test Room Bookings ����������������������������������������������������������������������
INSERT IGNORE INTO Room_Bookings (room_booking_id, check_in_date, check_out_date, deposit_amount, cancellation_deadline, credit_limit, personal_pin_hash) VALUES
(21, '2026-06-14', '2026-06-20', 1000000, '2026-06-12', 8000000, 'hash'),
(22, '2026-06-15', '2026-06-18', 1500000, '2026-06-13', 10000000, 'hash'),
(23, '2026-06-10', '2026-06-16', 3000000, '2026-06-08', 15000000, 'hash');

-- ���� 46. Test Room Booking Details ��������������������������������������������������������
INSERT IGNORE INTO Room_Booking_Details (detail_id, room_booking_id, category_id, room_id, room_charge, detail_status, bed_preference, special_requests, is_charge_to_room_allowed, sub_credit_limit, billing_routing_strategy) VALUES
(11, 21, 4, 6, 2000000, 'Active', 'KING_SIZE', NULL, TRUE, 500000, 'BILL_TO_LEADER'),
(12, 22, 2, 11, 3500000, 'Active', 'KING_SIZE', NULL, TRUE, 1500000, 'BILL_TO_LEADER'),
(13, 23, 3, 16, 8000000, 'Active', 'TWIN_BED', NULL, TRUE, 2000000, 'BILL_TO_LEADER');

-- ���� 47. Test Room Guests ��������������������������������������������������������������������������
INSERT IGNORE INTO Room_Guests (guest_id, detail_id, customer_id, dependent_id, guest_type, is_primary_contact) VALUES
(11, 11, 16, NULL, 'ADULT', TRUE),
(12, 12, 17, NULL, 'ADULT', TRUE),
(13, 13, 18, NULL, 'ADULT', TRUE);

-- ���� 48. C廕計 nh廕負 Rooms hi廙𡵞 t廕【 �ang �⑹廙θ test guest 廙� ������������
UPDATE Rooms SET current_booking_detail_id = 11, room_status = 'Occupied' WHERE room_id = 6;
UPDATE Rooms SET current_booking_detail_id = 12, room_status = 'Occupied' WHERE room_id = 11;
UPDATE Rooms SET current_booking_detail_id = 13, room_status = 'Occupied' WHERE room_id = 16;

-- ���� 51. L礙 Ho�ng Nam �廕暗 3 ph簷ng 1 l⑹廙ㄅ ��������������������������������������������������������������������������
-- RoomBooking IS-A Booking (Table-Per-Class Inheritance):
--   room_booking_id = booking_id (c羅ng m廙脌 PK, b廕τ ghi cha trong Bookings, con trong Room_Bookings)
-- Nam �廕暗 3 ph簷ng ri礙ng, check-in 2026-06-25, check-out 2026-06-28

-- B⑹廙𢲷 1: 3 b廕τ ghi Bookings (b廕τg cha) �� m廙𡟙 c獺i l� 1 ph簷ng c廙吧 Nam
INSERT IGNORE INTO Bookings (booking_id, customer_id, booking_date, total_price, booking_status, booking_source, applied_promotion_id, version) VALUES
(50, 1, '2026-06-22', 4500000, 'Confirmed', 'Direct_Web', NULL, 1),
(51, 1, '2026-06-22', 4500000, 'Confirmed', 'Direct_Web', NULL, 1),
(52, 1, '2026-06-22', 4500000, 'Confirmed', 'Direct_Web', NULL, 1);

-- B⑹廙𢲷 2: 3 b廕τ ghi Room_Bookings (b廕τg con) v廙𢹂 c羅ng ID �� k廕� th廙冠 t廙� Bookings
INSERT IGNORE INTO Room_Bookings (room_booking_id, check_in_date, check_out_date, deposit_amount, cancellation_deadline, credit_limit, personal_pin_hash) VALUES
(50, '2026-06-25', '2026-06-28', 1000000, '2026-06-23', 6000000, 'hash'),
(51, '2026-06-25', '2026-06-28', 1000000, '2026-06-23', 6000000, 'hash'),
(52, '2026-06-25', '2026-06-28', 1000000, '2026-06-23', 6000000, 'hash');

-- B⑹廙𢲷 3: 3 Room_Booking_Details �� ph簷ng 301, 302, 303 (Family Connecting Room, category_id=7)
INSERT IGNORE INTO Room_Booking_Details (detail_id, room_booking_id, category_id, room_id, room_charge, detail_status, bed_preference, special_requests, is_charge_to_room_allowed, sub_credit_limit, billing_routing_strategy, customer_id) VALUES
(50, 50, 7, 21, 4500000, 'Checked_In', 'KING_SIZE', 'C廕吵 th礙m gi⑹廙𩵚g ph廙� cho tr廕� em', TRUE, 1000000, 'BILL_TO_LEADER', 1),
(51, 51, 7, 22, 4500000, 'Checked_In', 'TWIN_BED', NULL,                              TRUE, 1000000, 'BILL_TO_LEADER', 2),
(52, 52, 7, 23, 4500000, 'Checked_In', 'TWIN_BED', 'T廕吵g cao, view �廕雷',              TRUE, 1000000, 'BILL_TO_LEADER', 3);

-- C廕計 nh廕負 tr廕》g th獺i v� g獺n current_booking cho 3 ph簷ng n�y
UPDATE Rooms SET current_booking_detail_id = 50, room_status = 'Occupied' WHERE room_id = 21;
UPDATE Rooms SET current_booking_detail_id = 51, room_status = 'Occupied' WHERE room_id = 22;
UPDATE Rooms SET current_booking_detail_id = 52, room_status = 'Occupied' WHERE room_id = 23;

-- B⑹廙𢲷 4: Room Guests �� M廙𡟙 ph簷ng 1 ng⑹廙𩥉 �廕【 di廙𡵞 (L礙 Ho�ng Nam, Nguy廙� V�n An, Ph廕《 Tu廕叩)
INSERT IGNORE INTO Room_Guests (guest_id, detail_id, customer_id, dependent_id, guest_type, is_primary_contact) VALUES
(50, 50, 1,    NULL, 'ADULT', TRUE),
(51, 50, NULL,    1, 'CHILD', FALSE),
(52, 50, NULL,    2, 'CHILD', FALSE),
(53, 51, 2,    NULL, 'ADULT', TRUE),
(54, 52, 3,    NULL, 'ADULT', TRUE);

-- B⑹廙𢲷 5: Th礙m Folio Items (Fake d廙醶h v廙� s廙� d廙叩g) cho 3 ph簷ng c廙吧 L礙 Ho�ng Nam
INSERT IGNORE INTO Folio_Items (folio_item_id, booking_id, room_booking_detail_id, payer_customer_id, source_department, amount, description, is_settled_separately, created_by_staff_id, created_at) VALUES 
(50, 50, 50, 1, 'F&B', 850000, '�n t廙魀 t廕【 nh� h�ng - Set menu', FALSE, 2, CURRENT_TIMESTAMP),
(51, 50, 50, 1, 'LAUNDRY', 120000, 'Gi廕暗 s廕句 qu廕吵 獺o', FALSE, 4, CURRENT_TIMESTAMP),
(52, 51, 51, 1, 'F&B', 150000, '�廙� u廙𤉋g minibar - Ph簷ng 302', FALSE, 2, CURRENT_TIMESTAMP),
(53, 51, 51, 1, 'SPA', 800000, 'Massage th⑹ gi瓊n 60 ph繳t', FALSE, 3, CURRENT_TIMESTAMP),
(54, 52, 52, 1, 'TRANSPORTATION', 350000, 'Thu礙 xe m獺y 2 ng�y', FALSE, 4, CURRENT_TIMESTAMP);

-- ���� Reset Auto-Increment Sequences (MySQL syntax) ������������������������
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

-- ���� 48b. Tour Bookings seed �� TourBooking li礙n k廕篙 v廙𢹂 RoomBooking nh⑹ng CH⑸A ph璽n ph簷ng ������������
-- C獺c b廕τ ghi n�y c籀 room_booking_id != NULL nh⑹ng room_booking_detail_id = NULL
-- �� l廙� t璽n s廕� th廕句 v� ph璽n b廙� tour v�o ph簷ng c廙� th廙� khi check-in

-- Booking g廙倴 cho TourBooking (ki廙� cha Bookings)
INSERT IGNORE INTO Bookings (booking_id, customer_id, booking_date, total_price, booking_status, booking_source, applied_promotion_id, version) VALUES
(200, 12, CURDATE(), 1200000, 'Confirmed', 'Direct_Web', NULL, 1),
(201, 12, CURDATE(), 3000000, 'Confirmed', 'Direct_Web', NULL, 1),
(202, 18, CURDATE(), 1200000, 'Confirmed', 'Direct_Web', NULL, 1);

-- TourBookings: li礙n k廕篙 v廙𢹂 RoomBooking 14 (Ph廕《 H羅ng Anh) v� 23 (test Confirmed)
-- room_booking_detail_id = NULL v穫 ch⑹a check-in / ch⑹a ph璽n ph簷ng
INSERT IGNORE INTO Tour_Bookings (booking_id, schedule_id, participant_count, tour_charge, room_booking_id, room_booking_detail_id) VALUES
(200, 1, 2, 1200000, 14, NULL),
(201, 2, 5, 3000000, 14, NULL),
(202, 3, 2, 1200000, 23, NULL);

ALTER TABLE Tour_Bookings AUTO_INCREMENT = 300;

-- ���� 49. Export History (Mock Data) ������������������������������������������������������

INSERT IGNORE INTO Export_History (id, report_name, format, exported_at, exported_by, file_size) VALUES
(1, 'Doanh thu th獺ng 5/2026', 'Excel', '2026-06-01 09:15:00', 'Manager ', '2.4 MB'),
(2, 'T廙� l廙� l廕叼 �廕囤 Q2', 'PDF', '2026-05-30 14:30:00', 'Manager ', '1.1 MB'),
(3, 'B獺o c獺o tour th獺ng 4', 'CSV', '2026-05-02 10:00:00', 'Manager ', '320 KB'),
(4, 'Doanh thu n�m 2025', 'Excel', '2026-01-15 08:45:00', 'Manager ', '5.8 MB');

-- ���� 50. Mock Data for YoY Comparison (N�m 2025) ������������������������������������������������������
INSERT INTO Bookings (booking_id, customer_id, booking_date, total_price, booking_status, booking_source, applied_promotion_id, version) VALUES
(991, 1, '2025-06-01', 65000000, 'Confirmed', 'Direct_Web', NULL, 1),
(992, 2, '2025-06-05', 45000000, 'Confirmed', 'OTA', NULL, 1);

INSERT INTO Room_Bookings (room_booking_id, check_in_date, check_out_date, deposit_amount, cancellation_deadline, credit_limit, personal_pin_hash) VALUES
(991, '2025-06-10', '2025-06-15', 5000000, '2025-06-05', 10000000, 'hash');

INSERT INTO Room_Booking_Details (detail_id, room_booking_id, category_id, room_id, room_charge, detail_status, bed_preference, special_requests, is_charge_to_room_allowed, sub_credit_limit, billing_routing_strategy) VALUES
(991, 991, 1, 1, 65000000, 'Checked_Out', 'KING_SIZE', NULL, TRUE, 500000, 'BILL_TO_LEADER');

INSERT INTO Tour_Bookings (booking_id, schedule_id, participant_count, tour_charge, is_walk_in_tour) VALUES 
(992, 1, 4, 45000000, FALSE);

-- Self-healing database name update for customer Ng廙㷼 Th廙� (formerly L礙 Quang)
UPDATE Customers SET full_name = 'Ng廙㷼 Th廙�', gender = 'N廙�' WHERE customer_id = 5;
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
(501, 501, 'VIP Nguyen Van A', 'vipa@example.com', '0999888771', 'MALE', '001099000501', 52000, 4),
(502, 502, 'Normal Tran B', 'normalb@example.com', '0999888772', 'FEMALE', '001099000502', 95, 1),
(503, 503, 'Newbie Le C', 'newbiec@example.com', '0999888773', 'MALE', '001099000503', 0, 1),
(504, 504, 'Banned Pham D', 'bannedd@example.com', '0999888774', 'FEMALE', '001099000504', 0, 1);

INSERT IGNORE INTO Dependents (dependent_id, customer_id, full_name, date_of_birth, relationship, face_vector_data, face_image_url) VALUES 
(501, 501, 'Wife Nguyen Thi B', '1995-08-15', 'Vo', NULL, NULL),
(502, 501, 'Kid Nguyen Van C', '2015-05-20', 'Con', NULL, NULL);

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

-- E2E Bookings
-- 1. Checked_In for VIP (Room 801)
INSERT IGNORE INTO Bookings (booking_id, customer_id, booking_date, total_price, booking_status, booking_source, applied_promotion_id, version) VALUES
(501, 501, '2026-06-25', 10500000, 'Checked_In', 'Direct_Web', NULL, 1);
INSERT IGNORE INTO Room_Bookings (room_booking_id, check_in_date, check_out_date, deposit_amount, cancellation_deadline, credit_limit, personal_pin_hash) VALUES
(501, '2026-06-28', '2026-07-02', 2000000, '2026-06-25', 10000000, 'hash');
INSERT IGNORE INTO Room_Booking_Details (detail_id, room_booking_id, category_id, room_id, room_charge, detail_status, bed_preference, special_requests, is_charge_to_room_allowed, sub_credit_limit, billing_routing_strategy, customer_id) VALUES
(501, 501, 3, 501, 10500000, 'Checked_In', 'KING_SIZE', 'Near elevator', TRUE, 5000000, 'BILL_TO_LEADER', 501);
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
(502, 502, 2, NULL, 1800000, 'Confirmed', 'TWIN_BED', 'Quiet room', TRUE, 2000000, 'INDIVIDUAL', 502);

-- 3. Pending_Approval (Triggered workflow)
INSERT IGNORE INTO Bookings (booking_id, customer_id, booking_date, total_price, booking_status, booking_source, applied_promotion_id, version) VALUES
(503, 502, '2026-06-29', 5000000, 'Pending_Approval', 'Direct_Web', 501, 1);
INSERT IGNORE INTO Room_Bookings (room_booking_id, check_in_date, check_out_date, deposit_amount, cancellation_deadline, credit_limit, personal_pin_hash) VALUES
(503, '2026-08-01', '2026-08-05', 0, '2026-07-25', 0, 'hash');
INSERT IGNORE INTO Room_Booking_Details (detail_id, room_booking_id, category_id, room_id, room_charge, detail_status, bed_preference, special_requests, is_charge_to_room_allowed, sub_credit_limit, billing_routing_strategy, customer_id) VALUES
(503, 503, 1, NULL, 5000000, 'Pending', 'KING_SIZE', 'TEST30 Applied', TRUE, 0, 'INDIVIDUAL', 502);

-- F&B Orders (Lifecycles)
INSERT IGNORE INTO Food_Orders (order_id, booking_id, guest_id, staff_id, order_type, table_number, total_amount, order_status, payment_status, notes) VALUES
(501, 501, 501, 2, 'ROOM_SERVICE', 'RM801', 415000, 'PENDING', 'UNPAID', 'Ph犥g 801 VIP'),
(502, 501, 501, 3, 'DINE_IN', 'T05', 500000, 'COOKING', 'UNPAID', '疘 d�'),
(503, 501, 501, 2, 'DINE_IN', 'T06', 85000, 'SERVED', 'UNPAID', 'Charge to room'),
(504, NULL, NULL, 3, 'DINE_IN', 'T07', 700000, 'PAID', 'PAID', 'Kh塶h v緋g lai'),
(505, NULL, NULL, 2, 'DINE_IN', 'T08', 0, 'CANCELLED', 'UNPAID', 'Kh塶h d?i �');

INSERT IGNORE INTO Food_Order_Details (detail_id, order_id, item_id, quantity, unit_price, subtotal) VALUES
(501, 501, 2, 1, 350000, 350000),
(502, 501, 4, 1, 65000, 65000),
(503, 502, 2, 1, 350000, 350000),
(504, 502, 5, 1, 95000, 95000),
(505, 503, 1, 1, 85000, 85000),
(506, 504, 2, 2, 350000, 700000);

-- Folios linked
INSERT IGNORE INTO Folio_Items (folio_item_id, booking_id, room_booking_detail_id, payer_customer_id, source_department, amount, description, is_settled_separately, created_by_staff_id, created_at) VALUES 
(501, 501, 501, 501, 'LAUNDRY', 150000, 'Gi?t ?i VIP', FALSE, 4, CURRENT_TIMESTAMP),
(502, 501, 501, 501, 'FNB', 85000, 'Order Nh� h跣g T06 (SERVED)', FALSE, 2, CURRENT_TIMESTAMP);

