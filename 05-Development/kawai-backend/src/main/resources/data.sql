-- ============================================================
-- KAWAI RESORT & TOUR HUB   COMPREHENSIVE SAMPLE DATA (V3.3)
-- Automatically executed on Spring Boot startup
-- ============================================================

--    1. Roles (10 rows)                                        
INSERT INTO Roles (role_id, role_name, permissions) VALUES (1, 'ADMIN', 'MASTER_DATA,AUDIT_LOG,DASHBOARD,RECEPTION_WALKIN,RECEPTION_INHOUSE,RECEPTION_CHECKIN,RECEPTION_CHECKOUT,FNB,FNB_ORDER,FNB_TABLE,FNB_ROOM_SERVICE,FNB_REPORT,TOUR,HOUSEKEEPING,MAINTENANCE,NIGHT_AUDIT,ANALYTICS,REVIEWS,PROMOTIONS,CRM,WORKFLOW');
INSERT INTO Roles (role_id, role_name, permissions) VALUES (2, 'RECEPTIONIST', 'DASHBOARD,RECEPTION_WALKIN,RECEPTION_INHOUSE,RECEPTION_CHECKIN,RECEPTION_CHECKOUT');
INSERT INTO Roles (role_id, role_name, permissions) VALUES (3, 'F&B KITCHEN', 'DASHBOARD,FNB,FNB_ORDER');
INSERT INTO Roles (role_id, role_name, permissions) VALUES (4, 'F&B POS', 'DASHBOARD,FNB,FNB_ORDER,FNB_TABLE,FNB_ROOM_SERVICE,FNB_REPORT');
INSERT INTO Roles (role_id, role_name, permissions) VALUES (5, 'HOUSEKEEPING', 'DASHBOARD,HOUSEKEEPING');
INSERT INTO Roles (role_id, role_name, permissions) VALUES (6, 'MAINTAINER', 'DASHBOARD,MAINTENANCE');
INSERT INTO Roles (role_id, role_name, permissions) VALUES (7, 'MANAGER', 'DASHBOARD,RECEPTION_WALKIN,RECEPTION_INHOUSE,RECEPTION_CHECKIN,RECEPTION_CHECKOUT,FNB,FNB_ORDER,FNB_TABLE,FNB_ROOM_SERVICE,FNB_REPORT,TOUR,HOUSEKEEPING,MAINTENANCE,NIGHT_AUDIT,ANALYTICS,REVIEWS,PROMOTIONS,CRM,WORKFLOW');
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
(13, 'Nguyễn Ngọc', '$2a$10$4bnThA4xQVw1rF2POQv78uAQll2KsUsgF32JaYiVG5d2d3Fhgk83q', TRUE, 8, CURRENT_TIMESTAMP),
(14, 'guide2', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 7, CURRENT_TIMESTAMP),
(15, 'guide3', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 7, CURRENT_TIMESTAMP),
(16, 'housekeep1', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 5, CURRENT_TIMESTAMP),
(17, 'housekeep2', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 5, CURRENT_TIMESTAMP),
(18, 'pos1', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 4, CURRENT_TIMESTAMP),
(19, 'manager1', '$2a$10$4bnThA4xQVw1rF2POQv78uAQll2KsUsgF32JaYiVG5d2d3Fhgk83q', TRUE, 7, CURRENT_TIMESTAMP),
(20, 'maintain1', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 6, CURRENT_TIMESTAMP),
(21, 'receptionist1_21', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 2, CURRENT_TIMESTAMP),
(22, 'receptionist2_22', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 2, CURRENT_TIMESTAMP),
(23, 'receptionist3_23', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 2, CURRENT_TIMESTAMP),
(24, 'receptionist4_24', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 2, CURRENT_TIMESTAMP),
(25, 'receptionist5_25', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 2, CURRENT_TIMESTAMP),
(26, 'fnb_kitchen1_26', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 3, CURRENT_TIMESTAMP),
(27, 'fnb_kitchen2_27', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 3, CURRENT_TIMESTAMP),
(28, 'fnb_kitchen3_28', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 3, CURRENT_TIMESTAMP),
(29, 'fnb_kitchen4_29', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 3, CURRENT_TIMESTAMP),
(30, 'fnb_pos1_30', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 4, CURRENT_TIMESTAMP),
(31, 'fnb_pos2_31', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 4, CURRENT_TIMESTAMP),
(32, 'fnb_pos3_32', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 4, CURRENT_TIMESTAMP),
(33, 'fnb_pos4_33', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 4, CURRENT_TIMESTAMP),
(34, 'fnb_pos5_34', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 4, CURRENT_TIMESTAMP),
(35, 'fnb_pos6_35', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 4, CURRENT_TIMESTAMP),
(36, 'housekeeping1_36', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 5, CURRENT_TIMESTAMP),
(37, 'housekeeping2_37', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 5, CURRENT_TIMESTAMP),
(38, 'housekeeping3_38', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 5, CURRENT_TIMESTAMP),
(39, 'housekeeping4_39', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 5, CURRENT_TIMESTAMP),
(40, 'housekeeping5_40', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 5, CURRENT_TIMESTAMP),
(41, 'housekeeping6_41', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 5, CURRENT_TIMESTAMP),
(42, 'housekeeping7_42', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 5, CURRENT_TIMESTAMP),
(43, 'housekeeping8_43', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 5, CURRENT_TIMESTAMP),
(44, 'housekeeping9_44', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 5, CURRENT_TIMESTAMP),
(45, 'housekeeping10_45', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 5, CURRENT_TIMESTAMP),
(46, 'maintainer1_46', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 6, CURRENT_TIMESTAMP),
(47, 'maintainer2_47', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 6, CURRENT_TIMESTAMP),
(48, 'manager1_48', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 7, CURRENT_TIMESTAMP),
(49, 'tourguide1_49', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 8, CURRENT_TIMESTAMP),
(50, 'tourguide2_50', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 8, CURRENT_TIMESTAMP);

-- ── 3. Employees (10 rows) ───────────────────────────────────
INSERT INTO Employees (employee_id, account_id, full_name, gender, cccd, phone, email, salary) VALUES 

(1, 1, 'Nguyễn Quản Trị', 'Nam', '001234567890', '0912000001', 'admin@hoanien.vn', 15000000),
(2, 2, 'Trần Phương', 'Nữ', '001234567891', '0912000002', 'tphuong@hoanien.vn', 10000000),
(3, 3, 'Nguyễn Minh Quân', 'Nam', '001234567892', '0912000003', 'nmquan@hoanien.vn', 10000000),
(4, 4, 'Lê Linh', 'Nữ', '001234567893', '0912000004', 'liungu2005@gmail.com', 9000000),
(5, 13, 'NguynNgoc', 'Nam', '001234567894', '0912000005', 'guide@hoanien.vn', 8000000),

(6, 14, 'Ngọc Lan', 'Nữ', '001234567895', '0912000006', 'guide2@hoanien.vn', 8500000),
(7, 15, 'Hoàng Nam', 'Nam', '001234567896', '0912000007', 'luu14102005@gmail.com', 8500000),
(8, 16, 'Lê Văn Tám', 'Nam', '001234567897', '0912000008', 'lvtam@hoanien.vn', 7000000),
(9, 17, 'Nguyễn Thị Hoa', 'Nữ', '001234567898', '0912000009', 'nthoa@hoanien.vn', 7000000),

(10, 20, 'Hoàng Bảo Trì', 'Nam', '001234567899', '0912000010', 'hbtri@hoanien.vn', 7500000),
(13, 21, 'Hoàng Quang Sơn', 'Nữ', '001291438783', '0912214052', 'receptionist1_21@hoanien.vn', 7000000),
(14, 22, 'Ngô Minh Yến', 'Nữ', '001274457804', '0912427979', 'receptionist2_22@hoanien.vn', 8500000),
(15, 23, 'Đặng Văn Vy', 'Nữ', '001287403917', '0912924835', 'receptionist3_23@hoanien.vn', 8500000),
(16, 24, 'Huỳnh Hải Vy', 'Nữ', '001210459433', '0912860249', 'receptionist4_24@hoanien.vn', 7000000),
(17, 25, 'Trần Hoài Hiếu', 'Nữ', '001297670572', '0912327850', 'receptionist5_25@hoanien.vn', 8000000),
(18, 26, 'Ngô Minh Trang', 'Nam', '001299142362', '0912917189', 'fnb_kitchen1_26@hoanien.vn', 9500000),
(19, 27, 'Trần Thu Yến', 'Nữ', '001281794004', '0912993331', 'fnb_kitchen2_27@hoanien.vn', 9500000),
(20, 28, 'Lê Ngọc Phương', 'Nam', '001228960997', '0912806263', 'fnb_kitchen3_28@hoanien.vn', 9000000),
(21, 29, 'Hồ Xuân Hoa', 'Nam', '001247301090', '0912254882', 'fnb_kitchen4_29@hoanien.vn', 7500000),
(22, 30, 'Phạm Hoài Tuấn', 'Nữ', '001292983782', '0912237687', 'fnb_pos1_30@hoanien.vn', 7500000),
(23, 31, 'Dương Tuấn Phương', 'Nữ', '001214218583', '0912804468', 'fnb_pos2_31@hoanien.vn', 9000000),
(24, 32, 'Võ Tuấn Phương', 'Nam', '001223330902', '0912104837', 'fnb_pos3_32@hoanien.vn', 9500000),
(25, 33, 'Phạm Hoài Tuấn', 'Nữ', '001250592270', '0912535003', 'fnb_pos4_33@hoanien.vn', 8500000),
(26, 34, 'Vũ Thu Phương', 'Nam', '001233901201', '0912342220', 'fnb_pos5_34@hoanien.vn', 8500000),
(27, 35, 'Nguyễn Bảo Lan', 'Nam', '001225738851', '0912423012', 'fnb_pos6_35@hoanien.vn', 8500000),
(28, 36, 'Vũ Hoài Châu', 'Nam', '001280501250', '0912469338', 'housekeeping1_36@hoanien.vn', 8500000),
(29, 37, 'Hoàng Bảo Uyên', 'Nữ', '001248562980', '0912821768', 'housekeeping2_37@hoanien.vn', 7000000),
(30, 38, 'Vũ Văn Bình', 'Nam', '001290326073', '0912988381', 'housekeeping3_38@hoanien.vn', 9500000),
(31, 39, 'Ngô Hữu Sơn', 'Nam', '001250101108', '0912649827', 'housekeeping4_39@hoanien.vn', 9500000),
(32, 40, 'Phạm Hữu Yến', 'Nam', '001247314145', '0912775515', 'housekeeping5_40@hoanien.vn', 8500000),
(33, 41, 'Bùi Văn Long', 'Nam', '001240456351', '0912587255', 'housekeeping6_41@hoanien.vn', 8500000),
(34, 42, 'Đặng Xuân Long', 'Nam', '001272372242', '0912240864', 'housekeeping7_42@hoanien.vn', 7500000),
(35, 43, 'Đỗ Bảo Yến', 'Nam', '001236341626', '0912504030', 'housekeeping8_43@hoanien.vn', 8500000),
(36, 44, 'Lê Minh Quân', 'Nữ', '001260222812', '0912983308', 'housekeeping9_44@hoanien.vn', 7000000),
(37, 45, 'Phan Xuân Việt', 'Nữ', '001293043538', '0912880965', 'housekeeping10_45@hoanien.vn', 8500000),
(38, 46, 'Phan Hoài Nga', 'Nam', '001260902961', '0912875663', 'maintainer1_46@hoanien.vn', 9000000),
(39, 47, 'Hoàng Hữu Nga', 'Nữ', '001271550498', '0912778014', 'maintainer2_47@hoanien.vn', 9000000),
(40, 48, 'Phạm Ngọc Việt', 'Nam', '001214604610', '0912238661', 'manager1_48@hoanien.vn', 8000000),
(41, 49, 'Đỗ Thu Bình', 'Nữ', '001268274071', '0912201144', 'tourguide1_49@hoanien.vn', 8000000),
(42, 50, 'Ngô Minh Lan', 'Nữ', '001229629943', '0912733244', 'tourguide2_50@hoanien.vn', 9000000);

-- ── 3.5. Membership Tiers (4 rows) ───────────────────────────────────
INSERT INTO membership_tiers (tier_id, tier_name, points_from, points_to, credit_limit, description) VALUES

(1, 'Regular', 0, 999, 5000000.00, 'Hạng thẻ mặc định'),

(2, 'Silver', 1000, 4999, 10000000.00, 'Hạng Bạc'),
(3, 'Gold', 5000, 9999, 20000000.00, 'Hạng Vàng'),
(4, 'Platinum', 10000, 99999, 50000000.00, 'Hạng Bạch kim');

-- ── 4. Customers (15 rows) ───────────────────────────────────
INSERT INTO Customers (customer_id, account_id, full_name, gender, cccd_passport_encrypted, phone, email, loyalty_points, membership_tier_id) VALUES 

(1, 5, 'Nguyễn Xuân Lưu', 'Nam', 'w3bBnx1QkqmC6TsRijDTyQ==', '0900000101', 'luu14102005@gmail.com', 100, 1),
(2, 6, 'Ngọc Thị', 'Nam', 'EVfujNqVKXtMAe5+YOnQug==', '0900000204', 'an204@test.com', 200, 2),
(3, 7, 'Phạm Tuấn', 'Nam', '9/Q+y3ZbaeA5FII8xIuL7Q==', '0900000308', 'tuan308@test.com', 500, 3),
(4, 8, 'Trần Thị Bích', 'Nữ', 'M/MlYxn9cKfE7aIurv4RyQ==', '0900000104', 'bich104@test.com', 50, 1),
(5, 9, 'Ngọc Thị', 'Nữ', 'aSFMtcXFScUI9tMJpcg8Rw==', '0900000106', 'quang106@test.com', 0, 1),
(6, 10, 'Đỗ Mỹ Linh', 'Nữ', 'm2I9IZODG7gQw4TsTgg2CQ==', '0900000207', 'linh207@test.com', 150, 2),

(7, 11, 'Hoàng Anh', 'Nam', '+DcrwqgOs34v6la2vB57Qg==', '0900000210', 'anh210@test.com', 300, 3),
(8, 12, 'Vũ Hùng', 'Nam', 'z3cHOvEWjSWhly6LTkb3mA==', '0900000311', 'hung311@test.com', 800, 4),
(9, NULL, 'Lưu Đình Đức', 'Nam', 'YTZ6KpDQsEJPEHfwR+3vNw==', '0909990001', 'duc@test.com', 0, 1),
(10, NULL, 'Nguyễn Minh Đức', 'Nam', 'L+rfuZSEQrLxAyYmz4xp9Q==', '0909990002', 'duc2@test.com', 0, 1),
(11, NULL, 'Trần Thị Mai', 'Nữ', 'Rdq+O8/+wOLP5PTwDytFRQ==', '0909990003', 'mai@test.com', 0, 1),

(12, NULL, 'Phạm Hùng Anh', 'Nam', 'AhHxE7tp68ehbFA5NT7Hvg==', '0909990004', 'phanh@test.com', 0, 1),

(13, NULL, 'Nguyễn Thanh Sơn', 'Nam', 'dWZuUAfHthTI8+2iM0V06g==', '0909990005', 'ntson@test.com', 0, 1),
(14, NULL, 'Vũ Thị Thảo', 'Nữ', 'W2E4Zu7gUvOwLsWbO5TkUA==', '0909990006', 'vtthao@test.com', 0, 1),
(15, NULL, 'Đoàn Minh Khang', 'Nam', 'z56ecl9PpUvWv6uTk1OMcQ==', '0909990007', 'dmkhang@test.com', 0, 1);

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
(1,  'Nipa Pool Villa',          '/guest/images/rooms/nipa-view.png,/guest/images/rooms/nipa-bed.png,/guest/images/rooms/nipa-bathroom.png', 2500000,  2, 'Villa thanh tịnh bên hồ sen thơm mát.',             2, 0, 3, 1, 500000,  250000, TRUE, '1 Giường King 2m2', 65, 'Hướng hồ bơi', TRUE, TRUE, '2 chai nước suối, Trái cây tươi, Vang đỏ', TRUE),
(2,  'River Pool Villa',         'https://images.unsplash.com/photo-1618773928121-c32242e63f39?w=800&q=80,https://images.unsplash.com/photo-1584622650111-993a426fbf0a?w=800&q=80,https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=800&q=80', 3500000,  3, 'Villa cao cấp ven sông Thu Bồn lộng gió.',          2, 0, 3, 2, 600000,  300000, TRUE, '1 Giường King 2m2', 80, 'Hướng sông Thu Bồn', TRUE, TRUE, '4 chai nước suối, Trái cây, Trà chiều', TRUE),
(3,  'Wellness Retreats',        'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=800&q=80,https://images.unsplash.com/photo-1544161515-4ab6ce6db874?w=800&q=80,https://images.unsplash.com/photo-1540555700478-4be289fbecef?w=800&q=80', 8000000,  4, 'Hành trình tĩnh lặng, chăm sóc sức khoẻ toàn diện.', 2, 0, 4, 2, 1000000, 500000, TRUE, '2 Giường King', 120, 'Hướng vườn thiền', TRUE, TRUE, 'Nước detox, Trái cây Organic, Trà thảo mộc', TRUE),
(4,  'Garden View Suite',        'https://images.unsplash.com/photo-1578683010236-d716f9a3f461,https://images.unsplash.com/photo-1566665797739-1674de7a421a?w=800&q=80,https://images.unsplash.com/photo-1596394516093-501ba68a0ba6?w=800&q=80', 2000000,  2, 'Suite hướng vườn nhiệt đới xanh mướt.',             2, 0, 3, 1, 400000,  200000, TRUE, '1 Giường Queen 1m8', 45, 'Hướng vườn nhiệt đới', FALSE, TRUE, '2 chai nước suối, Trà & Cà phê', TRUE),
(5,  'Presidential Ocean Suite', 'https://images.unsplash.com/photo-1590490360182-c33d57733427,https://images.unsplash.com/photo-1566665797739-1674de7a421a?w=800&q=80,https://images.unsplash.com/photo-1596394516093-501ba68a0ba6?w=800&q=80', 15000000, 6, 'Hạng phòng cao cấp bậc nhất hướng biển.',           4, 0, 6, 3, 2000000, 1000000, TRUE, '3 Giường King 2m2', 250, 'Hướng biển toàn cảnh', TRUE, TRUE, 'Minibar miễn phí, Rượu Champagne, Bánh ngọt', TRUE),
(6,  'Ocean View Bungalow',      'https://images.unsplash.com/photo-1582719508461-905c673771fd,https://images.unsplash.com/photo-1566665797739-1674de7a421a?w=800&q=80,https://images.unsplash.com/photo-1596394516093-501ba68a0ba6?w=800&q=80', 3000000,  2, 'Bungalow bãi cát đón gió biển tươi mát.',           2, 0, 3, 1, 600000,  300000, TRUE, '1 Giường King 2m2', 50, 'Hướng biển', TRUE, TRUE, '2 chai nước suối, Trái cây tươi', TRUE),
(7,  'Family Connecting Room',   'https://images.unsplash.com/photo-1568495248636-6432b97bd949,https://images.unsplash.com/photo-1566665797739-1674de7a421a?w=800&q=80,https://images.unsplash.com/photo-1596394516093-501ba68a0ba6?w=800&q=80', 4500000,  5, 'Phòng thông nhau phù hợp cho cả gia đình.',         2, 2, 4, 4, 500000,  250000, TRUE, '1 Giường King & 2 Giường Đơn', 90, 'Hướng vườn', FALSE, TRUE, '4 chai nước suối, Bánh quy, Trà', TRUE),
(8,  'Superior Mountain View',   'https://images.unsplash.com/photo-1596394516093-501ba68a0ba6,https://images.unsplash.com/photo-1566665797739-1674de7a421a?w=800&q=80,https://images.unsplash.com/photo-1571501443899-2a9c3722a8a8?w=800&q=80', 1800000,  2, 'Phòng hướng núi thanh tịnh bình yên.',              2, 0, 2, 1, 350000,  150000, TRUE, '2 Giường Đơn 1m2', 40, 'Hướng núi đồi', FALSE, FALSE, '2 chai nước suối, Trà & Cà phê', TRUE),

(9,  'Luxury Penthouse',         'https://images.unsplash.com/photo-1502672260266-1c1ef2d93688,https://images.unsplash.com/photo-1566665797739-1674de7a421a?w=800&q=80,https://images.unsplash.com/photo-1596394516093-501ba68a0ba6?w=800&q=80', 12000000, 4, 'Căn hộ tầng mái đẳng cấp ngắm toàn cảnh resort.',  2, 0, 4, 2, 1500000, 750000, TRUE, '2 Giường King siêu lớn', 180, 'Toàn cảnh Resort', TRUE, TRUE, 'Rượu vang cao cấp, Trái cây nhập khẩu, Minibar', TRUE),
(10, 'Cozy Studio Room',         'https://images.unsplash.com/photo-1522771739844-6a9f6d5f14af,https://images.unsplash.com/photo-1566665797739-1674de7a421a?w=800&q=80,https://images.unsplash.com/photo-1596394516093-501ba68a0ba6?w=800&q=80', 1500000,  2, 'Phòng Studio nhỏ gọn, đầy đủ tiện nghi.',          2, 0, 2, 1, 300000,  150000, TRUE, '1 Giường Queen 1m8', 35, 'Hướng đường phố', FALSE, FALSE, '2 chai nước suối, Cà phê hòa tan', FALSE);

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
-- Trạng thái phòng được mô hình hóa đúng:
--   Occupied = có detail Checked_In gắn vào (current_booking_detail_id != NULL)
--   Vacant_Clean / Vacant_Dirty = sẵn sàng giao cho booking Pending
-- Category mapping: 1=Nipa Pool Villa, 2=River Pool Villa, 3=Wellness Retreats,
--   4=Garden View Suite, 5=Presidential Ocean Suite, 6=Ocean View Bungalow,
--   7=Family Connecting Room, 8=Superior Mountain View, 9=Luxury Penthouse, 10=Cozy Studio
--
-- Booking Checked_In (booking_id=1, room_booking=1): detail_id=1, cat=1 → phòng 101 Occupied
-- Booking Checked_In (booking_id=1, room_booking=1): detail_id=3, cat=3 → phòng 105 dùng cat 3 nhưng detail gắn cat 3 → đổi sang phòng 406 (cat 3)
-- Booking Checked_In (room_booking=7):  detail_id=7, cat=2 → phòng 201 Occupied
-- Booking Checked_In (room_booking=8):  detail_id=8, cat=3 → phòng 406 Occupied
INSERT INTO Rooms (room_id, room_number, category_id, room_status, current_booking_detail_id) VALUES
-- Category 1 - Nipa Pool Villa (rooms 101-105): 1 Occupied (detail 1), còn lại Vacant
-- Room 101: Vacant_Clean (booking 1 của Hoàng Nam đã Checked_Out)
(1,  '101', 1, 'Vacant_Clean', NULL),
(2,  '102', 1, 'Vacant_Clean', NULL),
(3,  '103', 1, 'Maintenance',  NULL),
(4,  '104', 1, 'Vacant_Clean', NULL),
(5,  '105', 1, 'Vacant_Clean', NULL),
-- Category 4 - Garden View Suite (rooms 106-110): tất cả Vacant
(6,  '106', 4, 'Vacant_Clean', NULL),
(7,  '107', 4, 'Vacant_Clean', NULL),
(8,  '108', 4, 'Vacant_Clean', NULL),
(9,  '109', 4, 'Vacant_Clean', NULL),
(10, '110', 4, 'Maintenance',  NULL),
-- Category 2 - River Pool Villa (rooms 201-205): 1 Occupied (detail 7), còn lại Vacant
(11, '201', 2, 'Occupied',     7),
(12, '202', 2, 'Vacant_Clean', NULL),
(13, '203', 2, 'Vacant_Dirty', NULL),
(14, '204', 2, 'Vacant_Clean', NULL),
(15, '205', 2, 'Vacant_Clean', NULL),
-- Category 10 - Cozy Studio (rooms 206-210): tất cả Vacant
(16, '206', 10, 'Vacant_Clean', NULL),
(17, '207', 10, 'Maintenance',  NULL),
(18, '208', 10, 'Vacant_Clean', NULL),
(19, '209', 10, 'Vacant_Clean', NULL),
(20, '210', 10, 'Vacant_Clean', NULL),
-- Category 7 - Family Connecting Room (rooms 301-305): tất cả Vacant
(21, '301', 7, 'Vacant_Clean', NULL),
(22, '302', 7, 'Vacant_Clean', NULL),
(23, '303', 7, 'Vacant_Clean', NULL),
(24, '304', 7, 'Vacant_Clean', NULL),
(25, '305', 7, 'Vacant_Clean', NULL),
-- Category 8 - Superior Mountain View (rooms 306-310): tất cả Vacant
(26, '306', 8, 'Vacant_Clean', NULL),
(27, '307', 8, 'Vacant_Clean', NULL),
(28, '308', 8, 'Vacant_Clean', NULL),
(29, '309', 8, 'Vacant_Clean', NULL),
(30, '310', 8, 'Vacant_Clean', NULL),
-- Category 6 - Ocean View Bungalow (rooms 401-405): tất cả Vacant
(31, '401', 6, 'Vacant_Clean', NULL),
(32, '402', 6, 'Vacant_Clean', NULL),
(33, '403', 6, 'Vacant_Clean', NULL),
(34, '404', 6, 'Vacant_Clean', NULL),
(35, '405', 6, 'Vacant_Clean', NULL),
-- Category 3 - Wellness Retreats (rooms 406-410): 1 Occupied (detail 8)
(36, '406', 3, 'Vacant_Dirty', NULL),
(37, '407', 3, 'Vacant_Clean', NULL),
(38, '408', 3, 'Vacant_Clean', NULL),
(39, '409', 3, 'Vacant_Clean', NULL),
(40, '410', 3, 'Vacant_Clean', NULL),
-- Category 5 - Presidential Ocean Suite (rooms 501-505): tất cả Vacant
(41, '501', 5, 'Vacant_Clean', NULL),
(42, '502', 5, 'Vacant_Clean', NULL),
(43, '503', 5, 'Vacant_Clean', NULL),
(44, '504', 5, 'Vacant_Clean', NULL),
(45, '505', 5, 'Vacant_Clean', NULL),
-- Category 9 - Luxury Penthouse (rooms 506-510): tất cả Vacant
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
(909, 'EARLYBIRD', 'PERCENTAGE', 8.00, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 2000, 50, TRUE, 'Đặt trước 30 ngày hưởng ngay ưu đãi 8%.');
-- ── 12. Bookings (20 rows) ───────────────────────────────────

INSERT INTO Bookings (booking_id, customer_id, booking_date, total_price, booking_status, booking_source, applied_promotion_id, version) VALUES
(1, 1, '2026-06-01', 5000000, 'Checked_Out', 'Direct_Web', 1, 1), -- Hoàng Nam đã trả phòng
(2, 2, '2026-06-02', 7000000, 'Checked_Out', 'Direct_Web', NULL, 1),
(3, 3, '2026-06-03', 16000000, 'Confirmed', 'OTA', NULL, 1),
(4, 4, '2026-06-04', 5000000, 'Confirmed', 'OTA', 2, 1),
(5, 5, '2026-06-05', 5000000, 'Confirmed', 'Direct_Web', NULL, 1),
(6, 6, '2026-06-06', 7000000, 'Confirmed', 'OTA', NULL, 1),
(7, 7, '2026-06-07', 7000000, 'Confirmed', 'Direct_Web', 3, 1),
(8, 8, '2026-06-08', 16000000, 'Checked_Out', 'Direct_Web', NULL, 1),
(9, 1, '2026-06-10', 1200000, 'Confirmed', 'Direct_Web', NULL, 1),
(10, 2, '2026-06-10', 1200000, 'Confirmed', 'Direct_Web', NULL, 1),
(11, 9, '2026-06-12', 1200000, 'Confirmed', 'Direct_Web', NULL, 1),
(12, 10, '2026-06-12', 1200000, 'Confirmed', 'Direct_Web', NULL, 1),
(13, 11, '2026-06-12', 1200000, 'Confirmed', 'Direct_Web', NULL, 1),
(14, 12, '2026-06-12', 2500000, 'Checked_Out', 'Direct_Web', NULL, 1),
(15, 13, '2026-06-12', 3500000, 'Cancelled', 'Direct_Web', NULL, 1),
(16, 14, '2026-06-12', 8000000, 'Confirmed', 'Direct_Web', NULL, 1),
(17, 15, '2026-06-12', 2000000, 'Confirmed', 'Direct_Web', NULL, 1),
(18, 3, '2026-06-12', 15000000, 'Confirmed', 'Direct_Web', NULL, 1),
(19, 4, '2026-06-12', 3000000, 'Confirmed', 'Direct_Web', NULL, 1),
(20, 5, '2026-06-12', 4500000, 'Confirmed', 'Direct_Web', NULL, 1),
(24, 1, '2026-06-01', 3000000, 'Confirmed', 'Direct_Web', NULL, 1),
(25, 2, '2026-06-02', 1200000, 'Cancelled', 'Direct_Web', NULL, 1),
(26, 3, '2026-06-03', 5400000, 'Confirmed', 'Direct_Web', NULL, 1),
(27, 4, '2026-06-04', 2500000, 'Confirmed', 'OTA', NULL, 1),
(28, 5, '2026-06-05', 2400000, 'Confirmed', 'Direct_Web', NULL, 1);

-- ── 13. Room Bookings (10 rows) ──────────────────────────────
INSERT INTO Room_Bookings (room_booking_id, check_in_date, check_out_date, deposit_amount, cancellation_deadline, credit_limit, personal_pin_hash) VALUES
-- Room_Booking 1: ngày quá khứ (khách đã checkout, không dùng CURDATE)
(1, DATE_SUB(CURDATE(), INTERVAL 10 DAY), DATE_SUB(CURDATE(), INTERVAL 7 DAY), 1000000, DATE_SUB(CURDATE(), INTERVAL 15 DAY), 5000000, 'hash'),
(2, DATE_SUB(CURDATE(), INTERVAL 5 DAY), DATE_SUB(CURDATE(), INTERVAL 2 DAY), 1000000, DATE_SUB(CURDATE(), INTERVAL 8 DAY), 5000000, 'hash'),
(3, '2026-06-09', '2026-06-12', 2000000, '2026-06-05', 10000000, 'hash'),
(4, '2026-06-09', '2026-06-11', 1000000, '2026-06-05', 5000000, 'hash'),
(5, '2026-06-10', '2026-06-13', 1000000, '2026-06-06', 5000000, 'hash'),
(6, '2026-06-10', '2026-06-14', 1500000, '2026-06-06', 5000000, 'hash'),
(7, '2026-06-10', '2026-06-15', 1500000, '2026-06-06', 5000000, 'hash'),
(8, '2026-06-10', '2026-06-16', 3000000, '2026-06-06', 15000000, 'hash'),
(9, DATE_ADD(CURDATE(), INTERVAL 5 DAY), DATE_ADD(CURDATE(), INTERVAL 8 DAY), 500000, DATE_ADD(CURDATE(), INTERVAL 2 DAY), 5000000, 'hash'),
(14, DATE_SUB(CURDATE(), INTERVAL 10 DAY), DATE_SUB(CURDATE(), INTERVAL 6 DAY), 1000000, DATE_SUB(CURDATE(), INTERVAL 15 DAY), 5000000, 'hash'),
(15, DATE_ADD(CURDATE(), INTERVAL 20 DAY), DATE_ADD(CURDATE(), INTERVAL 25 DAY), 1000000, DATE_ADD(CURDATE(), INTERVAL 10 DAY), 5000000, 'hash');

-- ── 14. Room Booking Details (10 rows) ───────────────────────
-- Nguyên tắc nhất quán:
--   detail_status='Checked_In' → room_id PHẢI != NULL và phòng đó phải Occupied
--   detail_status='Pending'    → room_id = NULL (chưa được gán phòng vật lý)
--   detail_status='Checked_Out'/ 'Cancelled' → room_id có thể NULL (đã giải phóng)
INSERT INTO Room_Booking_Details (detail_id, room_booking_id, category_id, room_id, room_charge, detail_status, bed_preference, special_requests, is_charge_to_room_allowed, sub_credit_limit, billing_routing_strategy) VALUES
-- Booking 1 (Checked_Out): customer 1 - Hoàng Nam đã trả phòng 101; room_id=NULL sau checkout
(1,  1,  1, NULL, 2500000, 'Checked_Out', 'KING_SIZE', NULL, TRUE,  500000,  'BILL_TO_LEADER'),
-- Booking 2 (Checked_Out): customer 2 đã trả phòng, room_id NULL
(2,  2,  2, NULL, 3500000, 'Checked_Out', 'KING_SIZE', NULL, TRUE,  1500000, 'BILL_TO_LEADER'),
-- Booking 3 (Confirmed, Pending): customer 3 đặt Wellness Retreats, chưa gán phòng
(3,  3,  3, NULL, 8000000, 'Pending',     'KING_SIZE', NULL, TRUE,  2000000, 'BILL_TO_LEADER'),
-- Booking 4 (Confirmed, Pending): customer 4 đặt Nipa Pool Villa, chưa gán phòng
(4,  4,  1, NULL, 2500000, 'Pending',     'KING_SIZE', NULL, TRUE,  500000,  'BILL_TO_LEADER'),
-- Booking 5 (Confirmed, Pending): customer 5 đặt Nipa Pool Villa, chưa gán phòng
(5,  5,  1, NULL, 2500000, 'Pending',     'KING_SIZE', NULL, TRUE,  500000,  'BILL_TO_LEADER'),
-- Booking 6 (Confirmed, Pending): customer 6 đặt River Pool Villa, chưa gán phòng
(6,  6,  2, NULL, 3500000, 'Pending',     'KING_SIZE', NULL, TRUE,  1500000, 'BILL_TO_LEADER'),
-- Booking 7 (Checked_In): customer 7 đang ở phòng 201 (cat 2 - River Pool Villa)
(7,  7,  2, 11,   3500000, 'Checked_In',  'KING_SIZE', NULL, TRUE,  1500000, 'BILL_TO_LEADER'),
-- Booking 8 (Checked_Out): customer 8 đã trả phòng 406 (cat 3 - Wellness Retreats)
(8,  8,  3, NULL, 8000000, 'Checked_Out', 'KING_SIZE', NULL, TRUE,  3000000, 'BILL_TO_LEADER'),
-- Booking 14 (Checked_Out): đã trả phòng
(9,  14, 1, NULL, 2500000, 'Checked_Out', 'KING_SIZE', NULL, TRUE,  500000,  'BILL_TO_LEADER'),
-- Booking 15 (Cancelled)
(10, 15, 2, NULL, 3500000, 'Cancelled',   'TWIN_BED',  NULL, TRUE,  1500000, 'BILL_TO_LEADER'),
-- Booking 9 (Confirmed, Pending): room_booking_id=9, cat 1, 3 đêm × 2,500,000 = 7,500,000
(11, 9,  1, NULL, 7500000, 'Pending',     'KING_SIZE', NULL, TRUE,  500000,  'BILL_TO_LEADER');

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
(909, 909, 12, NULL, 'ADULT', TRUE),
(10, 10, 13, NULL, 'ADULT', TRUE),
(11, 1, 1, 1, 'CHILD', FALSE),
(12, 1, 1, 2, 'CHILD', FALSE),
(13, 11, 1, NULL, 'ADULT', TRUE);

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


-- Không cần UPDATE thêm vì current_booking_detail_id đã được khai báo đúng trong INSERT ở trên.
-- Chỉ verify: room 1 → detail 1 (Checked_In), room 11 → detail 7 (Checked_In), room 36 → detail 8 (Checked_In)

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
(909, 12, 11, '2026-06-13', '19:00:00', '21:00:00', 100000, 'Confirmed'),
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


(1, 'Gỏi cá mai', 185000, 'Khai vị', TRUE, 'Đặc sản Mũi Né với cá mai tươi, rau thơm và nước chấm chua ngọt hài hòa.', 'https://cdn.zsoft.solutions/poseidon-web/app/media/Kham-pha-am-thuc/03.2024/18324-goi-ca-mai-1.jpg', 'Fish, Peanut', FALSE),
(2, 'Bánh căn hải sản', 95000, 'Khai vị', TRUE, 'Bánh căn nóng giòn kết hợp tôm, mực tươi và nước chấm đậm đà.', 'https://media.thuonghieucongluan.vn/uploads/2026/03/01/banh-can-thumb-1772322285.jpg', 'Shellfish, Egg', FALSE),
(3, 'Bánh xèo Mũi Né', 110000, 'Khai vị', TRUE, 'Bánh xèo vàng giòn với nhân tôm thịt và rau sống tươi ngon.', 'https://i-giadinh.vnecdn.net/2023/09/19/Bc10Thnhphm11-1695107510-2493-1695107555.jpg', 'Shellfish, Gluten', FALSE),
(4, 'Súp cua', 120000, 'Khai vị', TRUE, 'Súp cua thơm ngọt với thịt cua, trứng và bắp non.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcT8zh8s2hEoOtqeCKJD9AUdyuYcpMXLIbUMlzdFDIw1FQ&s=10', 'Shellfish, Egg', FALSE),
(5, 'Súp hải sản', 145000, 'Khai vị', TRUE, 'Súp hải sản thanh ngọt từ tôm, mực và rau củ tươi.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRMVM6DWLLebjQTPWe7VkBltCuqH4bgqGxF1tL5UolVlw&s=10', 'Shellfish', FALSE),
(6, 'Súp gà nấm hương', 95000, 'Khai vị', TRUE, 'Súp gà mềm ngọt hòa quyện cùng nấm hương thơm tự nhiên.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSWn21CZqwUZCo8466Eqh4pbrRcTb-Vk4oFNL9fZqYXMg&s=10', NULL, FALSE),
(7, 'Súp bí đỏ kem tươi', 85000, 'Khai vị', TRUE, 'Bí đỏ xay mịn kết hợp kem tươi, béo nhẹ và thanh vị.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQUKVE6Brt_re0D5tnTCoqbBsJttHZpqZzOV-vXeu4vlA&s=10', 'Milk', FALSE),
(8, 'Gỏi ngó sen tôm thịt', 165000, 'Khai vị', TRUE, 'Ngó sen giòn tươi hòa quyện cùng tôm thịt và nước sốt chua ngọt.', 'https://cooponline.vn/tin-tuc/wp-content/uploads/2025/10/Avatar-2.png', 'Shellfish', FALSE),
(9, 'Gỏi gà xé phay', 135000, 'Khai vị', TRUE, 'Gà xé mềm trộn rau thơm và hành tây, thanh mát và hấp dẫn.', 'https://www.huongnghiepaau.com/wp-content/uploads/2025/05/cach-lam-goi-ga-xe-phay.jpg', NULL, FALSE),
(10, 'Gỏi bò bóp thấu', 175000, 'Khai vị', TRUE, 'Thịt bò mềm kết hợp rau củ tươi và nước sốt đậm vị.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTzllA_ZP9TSD2nQoyOFZcO_tSm6pjC330U4ZrDEEtC5g&s=10', NULL, FALSE),
(11, 'Gỏi xoài tôm khô', 145000, 'Khai vị', TRUE, 'Xoài xanh giòn chua nhẹ kết hợp tôm khô đậm đà.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcStIHf9pX74-zFf2s1VD39X4noHVXdIMVy1lwIJafLINQ&s=10', 'Shellfish', FALSE),
(12, 'Gỏi hải sản', 195000, 'Khai vị', TRUE, 'Hải sản tươi trộn rau củ cùng nước sốt chua cay đặc trưng.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSJqlK-m2m3xYxNQIkzS8YY-py6aaS0zjoUkf2MmTum8w&s=10', 'Shellfish', FALSE),
(13, 'Salad rau củ dầu giấm', 95000, 'Khai vị', TRUE, 'Rau củ tươi theo mùa trộn cùng sốt dầu giấm thanh nhẹ.', 'https://winefood.com.vn/wp-content/uploads/2024/06/z5512961476611_be635db082cdac788fca5636858378e3.jpg', NULL, FALSE),
(14, 'Salad cá ngừ', 185000, 'Khai vị', TRUE, 'Cá ngừ áp chảo kết hợp rau xanh tươi, cà chua bi và sốt mè rang thanh nhẹ.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTl8cQq2uQtbwh_kgPSN8NVX9E21FAjvQMbNIIrsap37Q&s=10', 'Fish, Sesame', FALSE),
(15, 'Mực một nắng nướng muối ớt', 395000, 'Món chính', TRUE, 'Mực một nắng nướng thơm, giữ trọn vị ngọt tự nhiên của biển.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTMosFGfsoMVWXSgYpus4wExPxCmHG6AHRsxT86UnQsRg&s=10', 'Shellfish', FALSE),
(16, 'Lẩu cá bớp', 520000, 'Món chính', TRUE, 'Cá bớp tươi trong nước lẩu chua thanh, dùng kèm rau và bún.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRK6eKKuhAudZfD5u7gclDml2jb4iJQU3ZjOyRzzYA0Lw&s=10', 'Fish', FALSE),
(17, 'Bánh canh chả cá', 125000, 'Món chính', TRUE, 'Bánh canh dai mềm cùng chả cá và nước dùng đậm đà.', 'https://lalago.vn/wp-content/uploads/2025/05/banh-canh-cha-ca-phan-thiet-6.jpg', 'Fish, Gluten', FALSE),
(18, 'Cá bóp kho tộ', 245000, 'Món chính', TRUE, 'Cá bóp kho theo phong cách truyền thống với hương vị đậm đà.', 'https://i-giadinh.vnecdn.net/2023/09/16/Bc4Thnhphm11-1694856717-6271-1694856861.jpg', 'Fish, Soy', FALSE),
(19, 'Tôm nướng mọi', 385000, 'Món chính', TRUE, 'Tôm tươi nướng nguyên con, giữ vị ngọt tự nhiên.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcS1NeG6yA1EQWEt_55ym0xMlWGcxItK_LWpiJyangoyFA&s=10', 'Shellfish', FALSE),
(20, 'Ghẹ hấp sả', 595000, 'Món chính', TRUE, 'Ghẹ tươi hấp cùng sả, thơm dịu và ngọt thịt.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQdqcrP2iDZvCfLVdvinGmUR-rayFu9lJtr0ELRyfSu3g&s=10', 'Shellfish', FALSE),
(21, 'Sò điệp nướng mỡ hành', 285000, 'Món chính', TRUE, 'Sò điệp nướng béo thơm cùng mỡ hành và đậu phộng.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcS668yHo5tseb6H2oxdx3x3NIvMn7UKgwSN-ayuIsEoRQ&s=10', 'Shellfish, Peanut', FALSE),
(22, 'Mực lá nướng sa tế', 355000, 'Món chính', TRUE, 'Mực lá nướng cùng sốt sa tế cay nhẹ, đậm hương vị.', 'https://cdn.tgdd.vn/Files/2019/03/23/1156454/3-cach-lam-muc-nuong-sa-te-han-quoc-va-chao-thom-ngon-kho-cuong-202110301600389672.jpg', 'Shellfish', FALSE),
(23, 'Ốc hương rang muối', 325000, 'Món chính', TRUE, 'Ốc hương rang muối giòn thơm, hấp dẫn trong từng miếng.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSUG-QP5tovwnkSjC4RPxvvW64vT5usWe5zRKiKd9qzcQ&s=10', 'Shellfish', FALSE),
(24, 'Cá thu sốt cà chua', 225000, 'Món chính', TRUE, 'Cá thu áp chảo kết hợp sốt cà chua chua ngọt ăn cùng với cơm trắng.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQIOU1DQo7Y23Epa8Mz24iU5FLrcYb1Y4Ui7ZUQTax2_Q&s=10', 'Fish', FALSE),
(25, 'Cá hồi áp chảo sốt chanh dây', 395000, 'Món chính', TRUE, 'Cá hồi áp chảo vàng đều, kết hợp sốt chanh dây chua ngọt, mang đến hương vị tinh tế và hấp dẫn.', 'https://chefstudio.vn/uploads/r/cach-lam-ca-hoi-sot-chanh-leo-thom-ngon.jpg', 'Fish, Milk', FALSE),
(26, 'Cháo hải sản', 195000, 'Món chính', TRUE, 'Cháo nóng hổi với hải sản tươi và hương vị thanh ngọt.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTtnzVLq_O4-f6Q7M5h5IkuoVxIDPI4UezW6HwYFQULPw&s=10', 'Shellfish', FALSE),
(27, 'Cơm chiên hải sản', 165000, 'Món chính', TRUE, 'Cơm chiên vàng đều cùng tôm, mực và trứng thơm ngon.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ4D2lMJP1Y0Aleu0jslS_vP0GfFdw1mmkYg3Spjk-1eA&s=10', 'Shellfish, Egg', FALSE),
(28, 'Tôm sú hấp nước dừa', 425000, 'Món chính', TRUE, 'Tôm sú hấp nước dừa giữ trọn vị ngọt và hương thơm tự nhiên.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRRMYT-f7lrpfv2Zj5uYcHIGwKOzr81XkNcid9NB_8YlA&s=10', 'Shellfish', FALSE),
(29, 'Tôm chiên bơ tỏi', 315000, 'Món chính', TRUE, 'Tôm chiên vàng giòn hòa quyện cùng bơ tỏi thơm béo.', 'https://cdn-i.vtcnews.vn/resize/th/upload/2024/10/03/tomchienbotois-11580919.png', 'Shellfish, Milk', FALSE),
(30, 'Mực xào chua ngọt', 265000, 'Món chính', TRUE, 'Mực tươi xào rau củ với sốt chua ngọt hài hòa.', 'https://i-giadinh.vnecdn.net/2021/03/31/muc1-1617182767-1435-1617182779.jpg', 'Shellfish', FALSE),
(31, 'Cá mú hấp Hồng Kông', 565000, 'Món chính', TRUE, 'Cá mú hấp xì dầu và gừng theo phong cách Hồng Kông.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR76WAMp8bA3-YSzofjutAfHTiTTvwuES0NEnLlfiz23A&s=10', 'Fish, Soy', FALSE),
(32, 'Ngao sốt thái', 215000, 'Món chính', TRUE, 'Ngao tươi sốt thái, thơm dịu và giữ nguyên vị ngọt, nước sốt thái chua ngọt đậm đà.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQvs0dElDV9HpnEs0tYYitzSpaVXMA15kPW2g4N4cLnGg&s=10', 'Shellfish', FALSE),
(33, 'Sò huyết nướng mỡ hành', 265000, 'Món chính', TRUE, 'Sò huyết nướng thơm béo cùng mỡ hành hấp dẫn.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcT1f0N22gTd5krNaOWL9boDwUnK1Sqj4UHYRd86sfOhEg&s=10', 'Shellfish, Peanut', FALSE),
(34, 'Hàu nướng phô mai', 245000, 'Món chính', TRUE, 'Hàu tươi phủ phô mai béo ngậy, nướng vàng hấp dẫn.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQMnWtYtmDJ_752ljmrFLSDcySv0FONNUYZEGiohb_XYw&s=10', 'Shellfish, Milk', FALSE),
(35, 'Cua rang me', 625000, 'Món chính', TRUE, 'Cua rang cùng sốt me chua ngọt đậm đà.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSqVibdAbutiMSSGvl-MOuqw5jLGCPimM3rZsP2xEBBmQ&s=10', 'Shellfish', FALSE),
(36, 'Lẩu hải sản chua cay', 595000, 'Món chính', TRUE, 'Lẩu hải sản với nước dùng chua cay và nguyên liệu tươi ngon.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR_3wUpu2uCMtzIqaFfIYPR_vnzxRhE3GHscuAAepFTfA&s=10', 'Shellfish', FALSE),
(37, 'Bạch tuộc nướng', 335000, 'Món chính', TRUE, 'Bạch tuộc nướng than hoa với độ giòn ngọt tự nhiên.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSlA-ERuGgo7gmv4KheNoQ3niCPD50eUYEzemipIhI5Yg&s=10', 'Shellfish', FALSE),
(38, 'Phở bò', 135000, 'Món chính', TRUE, 'Phở bò truyền thống với nước dùng hầm xương đậm vị.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRp63hqqANZeXyRQKFP_zSNKqvaT4iSdS0Lya21HtzS0A&s=10', 'Gluten', FALSE),
(39, 'Phở gà', 125000, 'Món chính', TRUE, 'Phở gà thanh ngọt cùng thịt gà mềm và rau thơm.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRyHPQAc0yF7zypjDfPfcEahLm5FjSoCcDoXhZh_OaCJA&s=10', 'Gluten', FALSE),
(40, 'Bún bò Huế', 125000, 'Món chính', TRUE, 'Bún bò Huế cay nhẹ với hương vị đặc trưng miền Trung.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcS0ZAe9xT_Kc5K0OMyRMqY5BswDwQYI9Wc0lNu9-H2rpw&s=10', NULL, FALSE),
(41, 'Bún chả Hà Nội', 145000, 'Món chính', TRUE, 'Thịt nướng thơm lừng ăn cùng bún và nước chấm truyền thống.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSMo3e19Uddx_I9WVoMfn6sRNQRb139oI2wrIxmQ_SlAg&s=10', 'Soy', FALSE),
(42, 'Bún thịt nướng', 135000, 'Món chính', TRUE, 'Thịt nướng đậm vị kết hợp bún tươi và rau sống.', 'https://cooponline.vn/tin-tuc/wp-content/uploads/2025/10/cach-lam-bun-thit-nuong-chuan-vi-sai-gon-thom-ngon-dam-da-kho-cuong.png', 'Peanut', FALSE),
(43, 'Cơm tấm sườn bì chả', 155000, 'Món chính', TRUE, 'Cơm tấm truyền thống với sườn nướng, bì và chả.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRbEqgR3k6WTSMqqYT0oBvseNzbV_dlNjnleBNoAGzJKQ&s=10', 'Egg', FALSE),
(44, 'Cơm gà xối mỡ', 145000, 'Món chính', TRUE, 'Gà chiên giòn rụm dùng cùng cơm trắng nóng.', 'https://static.vinwonders.com/production/2025/02/com-ga-xoi-mo-sai-gon-ut-minh.jpg', NULL, FALSE),
(45, 'Cơm chiên Dương Châu', 155000, 'Món chính', TRUE, 'Cơm chiên cùng hải sản, trứng và rau củ đầy màu sắc.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSaEx810OnvI1ucAZhMW5JlgMa8LRSc0RvQ4ViskrCAvQ&s=10', 'Shellfish, Egg', FALSE),
(46, 'Bò lúc lắc', 285000, 'Món chính', TRUE, 'Thịt bò mềm áp chảo cùng rau củ và tiêu đen.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQFtHwjMKEffc64iIpjsrUMsR1ITNs9qk4dVApiDQV4bw&s=10', 'Soy', FALSE),
(47, 'Bò né', 245000, 'Món chính', TRUE, 'Bò áp chảo nóng hổi dùng cùng trứng và bánh mì.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTcFFUwfBdCahr8pewDJpRaNkQTpm-S_tlKWX9KDX2CqQ&s=10', 'Gluten, Egg', FALSE),
(48, 'Gà nướng mật ong', 255000, 'Món chính', TRUE, 'Gà nướng vàng óng với lớp sốt mật ong thơm ngọt.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQxGMNUJ1xcNkE7iN_eHpbEflgW8lTbZ0Lrilfea6fPCw&s=10', NULL, FALSE),
(49, 'Gà chiên nước mắm', 235000, 'Món chính', TRUE, 'Gà chiên giòn phủ nước mắm tỏi đậm đà.', 'https://tiki.vn/blog/wp-content/uploads/2023/07/thumb-1.jpeg', NULL, FALSE),
(50, 'Canh chua cá', 215000, 'Món chính', TRUE, 'Canh chua miền Nam với cá tươi và rau đặc trưng ăn cùng với cơm trắng.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQTxbS393egPaZuOA-lQ0GVTYWNCpsSi5s8LYHGDkF9eQ&s=10', 'Fish', FALSE),
(51, 'Pizza Hải sản', 325000, 'Món chính', TRUE, 'Pizza giòn thơm phủ hải sản tươi và phô mai mozzarella.', 'https://img.dominos.vn/Pizzaminsea-Hai-San-Nhiet-Doi-Xot-Tieu.jpg', 'Gluten, Milk, Shellfish', FALSE),
(52, 'Pizza Margherita', 255000, 'Món chính', TRUE, 'Pizza cổ điển với sốt cà chua và phô mai mozzarella.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRdKA-zTX8iiBW-l_rjHpWArsTBf22VRLM_DqA4wPzKmw&s=10', 'Gluten, Milk', FALSE),
(53, 'Spaghetti Seafood', 295000, 'Món chính', TRUE, 'Mì Ý sốt hải sản với hương vị đậm đà và tươi ngon.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSu2y44Agc3YwXdClTx6HMLU9-azbDJJOdNDXteLv-x5g&s=10', 'Gluten, Shellfish', FALSE),
(54, 'Chè ba màu', 55000, 'Tráng miệng', TRUE, 'Món chè truyền thống với nhiều loại đậu và nước cốt dừa.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTu6LrFBziAPnqKrnP4cbenKZc2K75Ep-GBYgo-ls4_bg&s=10', NULL, FALSE),
(55, 'Chè khúc bạch', 65000, 'Tráng miệng', TRUE, 'Chè khúc bạch thanh mát dùng cùng nhãn và hạnh nhân.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTSPecn767GitLnV9QGuMVOcbSR0PGcQIw-d5PYeaIPyw&s=10', 'Milk', FALSE),
(56, 'Kem dừa', 75000, 'Tráng miệng', TRUE, 'Kem dừa mịn màng với vị béo nhẹ tự nhiên.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSYbXMHINuADHtuYZQxJ20Iw-Lxt0jyBwvnTN11fD7fXw&s=10', 'Milk', FALSE),
(57, 'Bánh flan', 55000, 'Tráng miệng', TRUE, 'Bánh flan mềm mịn với vị caramel ngọt dịu.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRSnF8AD_3JTqWnqbUx4VZYpKY3SRKremr3FM1t2dB5gg&s=10', 'Egg, Milk', FALSE),
(58, 'Trái cây theo mùa', 95000, 'Tráng miệng', TRUE, 'Tuyển chọn trái cây tươi ngon theo mùa.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSRjevrsK_VXNK_uASyCcCJxwmQUiTsh7JUjfS800aZ4A&s=10', NULL, FALSE),
(59, 'Kem vani', 75000, 'Tráng miệng', TRUE, 'Kem vani mịn béo với hương thơm dịu nhẹ.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQd3fhIwXSVsshPxkGb9gkC441QnIMjfILTSi-8lZl8PA&s=10', 'Milk', FALSE),
(60, 'Sữa chua', 45000, 'Tráng miệng', TRUE, 'Sữa chua việt quất chua ngọt tự nhiên, thanh mát và dễ thưởng thức.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSkXL1FGqhj4YVCK6uaMQe_7oIdDZwLEnQSG1dt8qWcAQ&s=10', 'Milk', FALSE),
(61, 'Bánh Mousse Chocolate', 90000, 'Tráng miệng', TRUE, 'Bánh mousse chocolate Bỉ mịn màng, đắng nhẹ.', 'https://i.ytimg.com/vi/pESVrDm6yIM/maxresdefault.jpg', 'Milk, egg', FALSE),
(62, 'Bánh Tiramisu', 95000, 'Tráng miệng', TRUE, 'Bánh Tiramisu mềm mịn với hương cà phê nhẹ, lớp kem mascarpone béo ngậy và bột cacao thơm, mang đến dư vị ngọt ngào sau bữa ăn.', 'https://thermomixvietnam.vn/wp-content/uploads/2021/08/tiramisu-truyen-thong.jpg', 'Milk, Egg, Gluten', FALSE),
(63, 'Nước dừa tươi', 55000, 'Đồ uống', TRUE, 'Nước dừa tươi mát được phục vụ nguyên trái.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRh0FZGaA6xavPbCzXxS7QyFohDVnGsLZk2zosFje_3eA&s=10', NULL, TRUE),
(64, 'Cam ép', 65000, 'Đồ uống', TRUE, 'Nước cam ép nguyên chất, giàu vitamin C.', 'https://www.sieuthidonglanh.com/wp-content/uploads/2023/03/Nuoc-ep-cam-giup-chong-lao-hoa-da-hieu-qua.png', NULL, TRUE),
(65, 'Nước chanh dây', 60000, 'Đồ uống', TRUE, 'Nước chanh dây chua ngọt, tươi mát.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcT_VJrLPs5FB0AHsHZmJk8Bwy6Kb7dyTztazL_8SPSHkQ&s=10', NULL, TRUE),
(66, 'Nước ép dưa hấu', 65000, 'Đồ uống', TRUE, 'Nước ép dưa hấu nguyên chất, giải nhiệt hiệu quả.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTvHhFBXMrB3U9D5o-XWF9B0pY-bm444AO3WBbSwD6pXQ&s=10', NULL, TRUE),
(67, 'Sinh tố xoài', 75000, 'Đồ uống', TRUE, 'Sinh tố xoài sánh mịn với vị ngọt tự nhiên.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTJc0QXQ4IqYKElXcUr1GTWVxnWPhB2TK_OpOpQMMDhRw&s=10', 'Milk', TRUE),
(68, 'Sinh tố bơ', 80000, 'Đồ uống', TRUE, 'Sinh tố bơ ngọt nhẹ với vị béo ngậy cuốn hút.', 'https://img.freepik.com/premium-photo/avocado-smoothie-with-avocado-wooden-board-dark-background_490636-2675.jpg', 'Milk', TRUE),
(69, 'Sinh tố mãng cầu', 80000, 'Đồ uống', TRUE, 'Sinh tố mãng cầu chua ngọt hài hòa, mát lạnh', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQliOZ99dtOiJEG_cP78-xLuhfShPa1PSpmQXAsZaamKA&s=10', 'Milk', TRUE),
(70, 'Cà phê đen', 45000, 'Đồ uống', TRUE, 'Cà phê pha phin đậm đà theo phong cách Việt Nam.', 'https://vinbarista.com/uploads/news/10-loi-ich-bat-ngo-khi-uong-ca-phe-den-nguyen-chat-202504021427.jpg', NULL, TRUE),
(71, 'Cà phê sữa', 50000, 'Đồ uống', TRUE, 'Cà phê pha cùng sữa đặc, cân bằng vị đắng và ngọt.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRnSg_F7K27YeJ5JcEOsW2QIdzpYQur2npcsOUgIxOQFw&s=10', 'Milk', TRUE),
(72, 'Cappuccino', 75000, 'Đồ uống', TRUE, 'Espresso kết hợp bọt sữa mịn theo phong cách Ý.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQB2t7LYP_hjvl-vBCTTS4NebRJGnX-OBlKcDnONH7X8Q&s=10', 'Milk', TRUE),
(73, 'Matcha Latte', 80000, 'Đồ uống', TRUE, 'Matcha Nhật Bản hòa quyện cùng sữa tươi và lớp foam mịn, mang đến hương vị thanh nhẹ, béo ngậy và cân bằng.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcS2_gzK--VFUUdnjc-htZ4Kv2JUuX3MkY0X3rC6C4u9eA&s=10', 'Milk', TRUE),
(74, 'Trà đào', 55000, 'Đồ uống', TRUE, 'Trà đào thơm nhẹ với vị ngọt thanh dễ uống.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR4CCqp_lnl-X0wM_hqWJm2ZVdgXD8agHWfxzRnsWuHpQ&s=10', NULL, TRUE),
(75, 'Trà chanh', 45000, 'Đồ uống', TRUE, 'Trà chanh tươi mát với vị chua nhẹ sảng khoái.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSqW5vUkX8PBxW9d5FL8_IQhmmOT8Km_VG4-Pi9RgNz4w&s=10', NULL, TRUE),
(76, 'Coca-Cola', 35000, 'Đồ uống', TRUE, 'Nước ngọt có ga phục vụ lạnh.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSecAEC3Qp39IHdyjLvoy2NTFBAfUjPQs4Ky2WY76V7Mg&s=10', NULL, TRUE),
(77, 'Pepsi', 35000, 'Đồ uống', TRUE, 'Nước ngọt có ga với hương vị quen thuộc.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSsbc5nPVtoJLUe7lTKlv5cpym3yvsQDrfe906ys17Psg&s=10', NULL, TRUE),
(78, 'Sprite', 35000, 'Đồ uống', TRUE, 'Nước ngọt vị chanh tươi mát.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSf33j4JK4YbOEIf0XD6cH9PfgyfqvFictJ-JyqfoAywA&s=10', NULL, TRUE),
(79, 'Soda chanh', 50000, 'Đồ uống', TRUE, 'Soda kết hợp nước cốt chanh tươi, sảng khoái.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcS0Qcpx_WfKUzQZVhpr26ocdX_3ol7Ibl9TmgpbSOFCHg&s=10', NULL, TRUE),
(80, 'Tiger Beer', 55000, 'Đồ uống', TRUE, 'Bia Tiger phục vụ lạnh, thích hợp cùng hải sản.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcT9YsRq1ocwyGXOUIFAUym39raCZxQ7GDixULXAo4vD7A&s=10', 'Gluten', TRUE),
(81, 'Heineken', 65000, 'Đồ uống', TRUE, 'Bia Heineken nhập khẩu với hương vị cân bằng.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRSIa9ayaH4xjGBGJk5vJVAqQF8o-sN3sOYOO1MaX5YNw&s=10', 'Gluten', TRUE),
(82, 'Bia thủ công', 95000, 'Đồ uống', TRUE, 'Bia thủ công với hương vị đặc trưng theo mùa.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSIlCLAN6vfvnzSB-PjslKfnpGOvOfbIv8mtG45I3y2gA&s=10', 'Gluten', TRUE),
(83, 'Nước khoáng Evian', 25000, 'Đồ uống', TRUE, 'Nước khoáng Evian Pháp tinh khiết và mát lạnh.', 'https://gangnamkong.co.kr/web/upload/NNEditor/20230424/33378a1a3af8ff1869c4b1faf8da8863.jpg', NULL, TRUE);


-- ── 18.5 Menu Item Days (Phân bổ thực đơn theo ngày) ───────────
INSERT INTO Menu_Item_Days (item_id, day_of_week) VALUES
-- MONDAY
(1, 'MONDAY'), (2, 'MONDAY'), (3, 'MONDAY'), (4, 'MONDAY'), (15, 'MONDAY'), (16, 'MONDAY'), (17, 'MONDAY'), (18, 'MONDAY'), (19, 'MONDAY'), (20, 'MONDAY'), (21, 'MONDAY'), (22, 'MONDAY'), (23, 'MONDAY'), (24, 'MONDAY'), (54, 'MONDAY'), (55, 'MONDAY'), (56, 'MONDAY'),

-- TUESDAY
(5, 'TUESDAY'), (6, 'TUESDAY'), (7, 'TUESDAY'), (8, 'TUESDAY'), (25, 'TUESDAY'), (26, 'TUESDAY'), (27, 'TUESDAY'), (28, 'TUESDAY'), (29, 'TUESDAY'), (30, 'TUESDAY'), (31, 'TUESDAY'), (32, 'TUESDAY'), (33, 'TUESDAY'), (34, 'TUESDAY'), (57, 'TUESDAY'), (58, 'TUESDAY'), (59, 'TUESDAY'),

-- WEDNESDAY
(9, 'WEDNESDAY'), (10, 'WEDNESDAY'), (11, 'WEDNESDAY'), (12, 'WEDNESDAY'), (35, 'WEDNESDAY'), (36, 'WEDNESDAY'), (37, 'WEDNESDAY'), (38, 'WEDNESDAY'), (39, 'WEDNESDAY'), (40, 'WEDNESDAY'), (41, 'WEDNESDAY'), (42, 'WEDNESDAY'), (43, 'WEDNESDAY'), (44, 'WEDNESDAY'), (60, 'WEDNESDAY'), (61, 'WEDNESDAY'), (62, 'WEDNESDAY'),

-- THURSDAY
(13, 'THURSDAY'), (14, 'THURSDAY'), (1, 'THURSDAY'), (2, 'THURSDAY'), (45, 'THURSDAY'), (46, 'THURSDAY'), (47, 'THURSDAY'), (48, 'THURSDAY'), (49, 'THURSDAY'), (50, 'THURSDAY'), (51, 'THURSDAY'), (52, 'THURSDAY'), (53, 'THURSDAY'), (15, 'THURSDAY'), (54, 'THURSDAY'), (55, 'THURSDAY'), (56, 'THURSDAY'),

-- FRIDAY
(3, 'FRIDAY'), (4, 'FRIDAY'), (5, 'FRIDAY'), (6, 'FRIDAY'), (16, 'FRIDAY'), (17, 'FRIDAY'), (18, 'FRIDAY'), (19, 'FRIDAY'), (20, 'FRIDAY'), (21, 'FRIDAY'), (22, 'FRIDAY'), (23, 'FRIDAY'), (24, 'FRIDAY'), (25, 'FRIDAY'), (57, 'FRIDAY'), (58, 'FRIDAY'), (59, 'FRIDAY'),

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
(2, 1, 9, 1, 135000, 'Preparing'),
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
(20, 18, 9, 2, 135000, 'Preparing'),
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
(909, 14, 1, 1, 800000, '2026-07-01 15:00:00', 'PENDING', '{"flight_number": "QH224"}'),
(10, 15, 9, 1, 600000, '2026-07-02 09:00:00', 'PENDING', '{"card_note": "Happy Birthday Leader Nam"}');

-- ── 23. Hotel Operations (10 rows) ───────────────────────────
INSERT INTO Hotel_Operations (task_id, room_id, staff_id, supervisor_id, operational_type, priority, status, created_at, started_at, completed_at, notes) VALUES 
-- HOUSEKEEPING (CHECKOUT_CLEAN)

(1, 2, 8, 4, 'CHECKOUT_CLEAN', 'High', 'Pending', '2026-06-28 08:00:00', NULL, NULL, '[Check-out] Khách phòng 102 vừa trả phòng, dọn gấp để đón đoàn 2h chiều.'),
(2, 4, 9, 4, 'CHECKOUT_CLEAN', 'Normal', 'Pending', '2026-06-28 09:00:00', NULL, NULL, '[Check-out] Dọn dẹp sạch sâu, thay toàn bộ ga giường và xịt thơm phòng.'),
(3, 3, 8, 4, 'GUEST_REQUEST', 'Normal', 'Completed', '2026-06-28 09:30:00', '2026-06-28 10:15:00', '2026-06-28 11:00:00', '[Stay-over] Khách yêu cầu thêm 2 khăn tắm và 1 chai nước suối.'),
(4, 13, 9, 4, 'URGENT_CLEAN', 'High', 'InProgress', '2026-06-28 10:00:00', '2026-06-28 10:20:00', NULL, '[Arrival] Khách VIP sắp nhận phòng, chuẩn bị sẵn giỏ trái cây tươi trên bàn.'),
(5, 1, 9, 4, 'CHECKOUT_CLEAN', 'Normal', 'Completed', '2026-06-28 07:00:00', '2026-06-28 07:15:00', '2026-06-28 08:45:00', '[Check-out] Đã dọn xong, phát hiện quên một chiếc sạc điện thoại trên bàn.'),

-- MAINTENANCE (MAINTENANCE)
(6, 17, 10, 4, 'MAINTENANCE', 'High', 'Pending', '2026-06-28 10:30:00', NULL, NULL, 'Housekeeping báo: Điều hòa chảy nước ướt cả sàn gỗ, phòng 207.'),
(7, 3, 10, 4, 'MAINTENANCE', 'Normal', 'Pending', '2026-06-28 12:00:00', NULL, NULL, 'Khách phàn nàn: Vòi hoa sen bị nghẹt, nước chảy rất yếu.'),
(8, 5, 10, 4, 'MAINTENANCE', 'Normal', 'InProgress', '2026-06-28 13:00:00', '2026-06-28 13:15:00', NULL, 'Kiểm tra hệ thống đèn ban công, 1 bóng bị cháy.'),
(909, 10, 10, 4, 'MAINTENANCE', 'High', 'Paused', '2026-06-28 09:00:00', '2026-06-28 09:10:00', NULL, 'Sửa két sắt không mở được. \n[Tạm dừng]: Chờ mua pin mới loại 9V để thay mảng mạch.'),
(10, 16, 10, 4, 'MAINTENANCE', 'Low', 'Completed', '2026-06-28 08:00:00', '2026-06-28 08:05:00', '2026-06-28 08:20:00', 'Thay pin tay nắm cửa phòng 309. \n[Đã sửa]: Đã thay 4 cục pin AA Panasonic.');

-- ── 24. Folio Items (10 rows) ────────────────────────────────
INSERT INTO Folio_Items (folio_item_id, booking_id, room_booking_detail_id, payer_customer_id, source_department, amount, description, is_settled_separately, created_by_staff_id, created_at, signature_img_url) VALUES 
(2, 1, 1, 1, 'F&B', 180000, 'Súp Bí Đỏ Truffle Room Service', FALSE, 2, CURRENT_TIMESTAMP, NULL),
(3, 2, 2, 2, 'TRANSPORTATION', 800000, 'Xe đón tiễn Limousine sân bay', FALSE, 4, CURRENT_TIMESTAMP, NULL);

-- ── 27. Tours (10 rows) ──────────────────────────────────────
INSERT INTO Tours (tour_id, tour_name, tour_type, duration, base_price, max_capacity, short_quote, description, handbook_spec, handbook_logistics, handbook_explanations, created_at, is_active, duration_hours, is_insurance_required, insurance_price) VALUES 
(1, 'Hành Trình Đoàn Tụ – Cố Đô Huế', 'doantu', '10 Giờ', 7500000, 15, 'Tìm về hơi ấm vẹn nguyên của lòng biết ơn và sự gắn kết.', 'Chuyến đi đưa bạn ngược dòng thời gian về với nét đẹp trầm mặc của cố đô, khơi mở những câu chuyện di sản và thưởng thức phong vị ẩm thực cung đình xưa.', '10 Giờ (08:00 - 18:00) | Sức chứa: 15 khách.', '["Trang phục: Nhắc khách mặc quần áo lịch sự, che vai và qua đầu gối khi tham quan Điện Thái Hòa, Thế Miếu (Đại Nội).","Sức khỏe: Chuẩn bị sẵn nhiều dù cao cấp và quạt tay vì thời tiết Huế mùa hè rất nắng gắt.","Dịch vụ ngầm: Gọi nhà thuyền kiểm tra dàn nhạc Nhã nhạc trước 1 tiếng, đảm bảo trà Cung Đình tại Duyệt Thị Đường được phục vụ đúng lúc khách vừa ngồi xuống"]', '[{"cat":"Lịch sử","content":"Triều Nguyễn (1802 - 1945) là triều đại phong kiến cuối cùng của Việt Nam với 13 vị vua. Đại Nội là nơi sống và làm việc của hoàng gia, được xây dựng theo thuyết phong thủy phương Đông: Tựa lưng vào núi Ngự Bình, lấy cồn Hến và cồn Dã Viên trên sông Hương làm tả thanh long, hữu bạch hổ."},{"cat":"Văn hóa cốt lõi","content":"Giải thích ý nghĩa châm ngôn \"Đoàn tụ/Biết ơn\": Thế Miếu là nơi thờ các vị vua triều Nguyễn, nhắc nhở thế hệ sau về cội nguồn. Nhã nhạc cung đình Huế là Di sản văn hóa phi vật thể đầu tiên của Việt Nam được UNESCO công nhận."}]', CURRENT_TIMESTAMP, TRUE, 10.0, FALSE, 0),
(2, 'Tinh Túy Đồng Nội – Quảng Nam', 'dongnoi', '10 Giờ', 5800000, 12, 'Lắng nghe nhịp điệu mộc mạc của đất mẹ và hồn quê xứ Quảng.', 'Hành trình chạm vào những điều bình dị mà sâu lắng của đất Quảng: từ những con phố rêu phong ở Hội An, hương đất nung bên dòng sông Thu Bồn đến vị mặn mòi của một làng rau lâu đời.', '10 Giờ (08:30 - 18:30) | Sức chứa: 12 khách.', '["Trang phục: Khách sẽ tham gia xới đất tại Trà Quế và xoay gốm tại Thanh Hà, nhắc khách mặc đồ thoải mái, dễ giặt, mang dép hoặc giày dễ tháo rời.","Hậu cần: Chuẩn bị sẵn khăn ướt, tạp dề cao cấp cho khách khi làm gốm và nón lá che nắng tại vườn rau"]', '[{"cat":"Địa lý & Lịch sử","content":"Làng gốm Thanh Hà (thế kỷ 16) và Phố cổ Hội An từng là thương cảng quốc tế sầm uất bậc nhất Đông Nam Á, nơi giao thương của các thương thuyền Nhật Bản, Trung Hoa và phương Tây. Dòng sông Thu Bồn là mạch máu bồi đắp phù sa cho các làng nghề này."},{"cat":"Văn hóa","content":"Kỹ thuật nung gốm Thanh Hà hoàn toàn bằng củi và không tráng men, sản phẩm có màu đỏ hồng đặc trưng của đất sét lòng sông. Rau Trà Quế ngon nhờ người dân dùng một loại rong đặc biệt dưới lòng sông Cổ Cò để bón, tạo nên vị thơm cay thanh nhẹ tự nhiên."}]', CURRENT_TIMESTAMP, TRUE, 10.0, FALSE, 0),
(3, 'Di Sản Thủ Công – Ninh Bình', 'disan', '10 Giờ', 6500000, 12, 'Chạm vào hồn cốt của thời gian qua những tạo tác từ đôi bàn tay nghệ nhân.', 'Hành trình tìm về vẻ đẹp sơn thủy hữu tình của vùng đất cố đô cổ kính, nơi bạn được trò chuyện và cùng các nghệ nhân lưu giữ những làng nghề truyền thống trăm năm.', '10 Giờ (09:00 - 19:00) | Sức chứa: 12 khách.', '["An toàn đường thủy: Chặng đi thuyền nan tại đầm Vân Long bắt buộc khách phải mặc áo phao. Nhắc khách giữ thăng bằng khi lên xuống thuyền.","Bảo mật trải nghiệm: Nhắc khách không nói lớn tiếng tại Vân Long để tránh làm chim muông và thú quý giật mình bay mất"]', '[{"cat":"Địa lý","content":"Đầm Vân Long là khu bảo tồn thiên nhiên ngập nước lớn nhất vùng đồng bằng Bắc Bộ, sở hữu bức tranh địa chất \"vịnh không sóng\" vì mặt nước phẳng lặng như một tấm gương khổng lồ soi bóng núi đá vôi Karst ngàn năm."},{"cat":"Văn hóa & Nghệ thuật","content":"Làng thêu Văn Lâm có tuổi đời hơn 700 năm (từ thời nhà Trần). Điểm đắt giá của thêu Văn Lâm là kỹ thuật \"thêu trắng\" (những đường thêu uyển chuyển trên nền vải trắng bằng chỉ trắng) đòi hỏi đôi tay cực kỳ tài hoa của nghệ nhân. Chiếu Kim Sơn nổi tiếng nhờ sợi cói dai mịn, dệt đều tay, đượm hương nắng trời Bắc Bộ."}]', CURRENT_TIMESTAMP, TRUE, 10.0, FALSE, 0),
(4, 'Tĩnh Lặng Liên Hoa – Tháp Mười', 'tinhlang', '10 Giờ', 8200000, 8, 'Sự thanh lọc thuần khiết cho thân - tâm - trí giữa vùng sông nước mờ sương.', 'Một ngày trốn khỏi phố thị để về với đại đầm sen, mượn hương hoa và sóng nước miền Tây làm dịu lại tâm hồn, tìm lại sự bình an sâu lắng bên trong bạn.', '10 Giờ (08:00 - 18:00) | Sức chứa: 8 khách.', '["Không gian tĩnh lặng: Đây là tour chữa lành (Wellness/Zen), yêu cầu khách chuyển điện thoại sang chế độ rung, không bật loa ngoài.","Trang phục: Khách đi bộ thiền hành trên cầu gỗ, nhắc khách mang giày bệt hoặc đi chân trần theo hướng dẫn. Chuẩn bị sẵn kem chống muỗi/côn trùng vùng sông nước"]', '[{"cat":"Địa lý & Thiên nhiên","content":"Đồng Tháp Mười là vùng đất ngập nước đặc trưng của miền Tây Nam Bộ. Hoa sen ở đây nở quanh năm nhưng rực rỡ nhất vào mùa nước nổi. Đất phèn đặc trưng của vùng đồng bằng sông Cửu Long lại là dưỡng chất hoàn hảo giúp hoa sen Tháp Mười có hương thơm đậm và giữ được độ tươi rất lâu."},{"cat":"Văn hóa & Triết lý","content":"Sen là biểu tượng cho sự thuần khiết trong văn hóa Việt Nam (\"Gần bùn mà chẳng hôi tanh mùi bùn\"). Giới thiệu về nghệ thuật ủ trà sen: Người ta đón sương đêm, cho trà vào trong búp sen từ chiều hôm trước để trà hấp thụ trọn vẹn tinh túy và hương thơm của hoa lúc nửa đêm."}]', CURRENT_TIMESTAMP, TRUE, 10.0, FALSE, 0),
(5, 'Tiếng Vọng Sóng Xanh – Hạ Long', 'halong', '10 Giờ', 7500000, 15, 'Du thuyền lướt nhẹ giữa ngàn khơi, thưởng ngoạn kỳ quan thiên nhiên rực rỡ.', 'Chuyến hải trình thong dong đưa bạn lướt qua những đảo đá nhấp nhô của vịnh Bắc Bộ, khám phá thế giới thạch nhũ kỳ vĩ được tạo hóa giấu kín ngàn năm và đón hoàng hôn buông trên mặt biển.', '10 Giờ (08:00 - 18:00) | Sức chứa: 15 khách.', '["Chuẩn bị: Nhắc khách mang theo đồ bơi, quần áo dự phòng để thay sau khi chèo Kayak hoặc tắm biển.","An toàn: Kiểm tra túi chống nước cho điện thoại của khách. Chuẩn bị sẵn thuốc say sóng phòng trường hợp khách nhạy cảm với sóng biển"]', '[{"cat":"Địa lý (Kiến tạo Karst)","content":"Vịnh Hạ Long là Di sản Thiên nhiên Thế giới được UNESCO công nhận, sở hữu giá trị địa chất độc độc đáo trải qua hơn 500 triệu năm kiến tạo để tạo nên ngàn đảo đá vôi trùng điệp."},{"cat":"Lịch sử & Huyền thoại","content":"Hạ Long nghĩa là rồng đáp xuống. Truyền thuyết kể rằng Ngọc Hoàng đã sai Rồng Mẹ mang theo một đàn Rồng Con xuống hạ giới giúp người Việt đánh giặc ngoại xâm. Đàn rồng phun ra muôn ngàn châu ngọc, hóa thành các đảo đá dựng thành lũy vững chắc ngăn chặn thuyền giặc. Động Thiên Cung chính là cung điện lộng lẫy nơi diễn ra đám cưới của Vua Rồng xưa kia."}]', CURRENT_TIMESTAMP, TRUE, 10.0, FALSE, 0),
(6, 'Nẻo Đường Sương Phủ – Sapa', 'sapa', '11 Giờ', 8200000, 10, 'Gặp gỡ những nụ cười hồn hậu giữa vùng mây trời sương phủ.', 'Hành trình dạo bước qua những nấc thang ruộng bậc thang xanh mướt tại bản Cát Cát, tìm hiểu cuộc sống mộc mạc của đồng bào người H''Mông và lắng lại tâm hồn giữa núi rừng Tây Bắc.', '11 Giờ (08:00 - 19:00) | Sức chứa: 10 khách.', '["Thể lực: Tour này tản bộ nhiều dốc (bản Cát Cát), nhắc khách mang giày trekking hoặc giày thể thao có độ bám tốt.","Thời tiết: Thời tiết Sapa thay đổi liên tục, luôn nhắc khách mang theo một chiếc áo khoác nhẹ (phòng lạnh về chiều) và ô/áo mưa bỏ túi"]', '[{"cat":"Địa lý","content":"Sapa nằm ở độ cao trung bình 1,500m - 1,800m so với mực nước biển, thuộc dãy Hoàng Liên Sơn hùng vĩ. Thác Tiên Sa là nguồn nước mát lạnh đổ trực tiếp từ trên núi cao xuống, quanh năm tung bọt trắng xóa."},{"cat":"Văn hóa","content":"Bản Cát Cát là nơi sinh sống lâu đời của người H''Mông đen. Hãy thuyết minh về chiếc cối xay nước tận dụng sức nước của dòng suối để giã gạo, và nghệ thuật vẽ hoa văn bằng sáp ong bướm trên vải trước khi mang đi nhuộm chàm – một nét văn hóa độc đáo giúp trang phục của họ có màu xanh đen đặc trưng bền bỉ với thời gian."}]', CURRENT_TIMESTAMP, TRUE, 11.0, FALSE, 0),
(7, 'Nhịp Đập Rừng Già – Cát Tiên', 'cattien', '14 Giờ', 9500000, 8, 'Lắng nghe tiếng gọi thì thầm từ đại ngàn xanh thẳm.', 'Chuyến băng rừng rậm Nam Cát Tiên đầy cảm xúc, đưa bạn ghé thăm đầm lầy bảo tồn tự nhiên, thưởng thức bữa tối bên rừng và trải nghiệm ngắm thú đêm hoang dã.', '14 Giờ (08:00 - 22:00) | Sức chứa: 8 khách.', '["An toàn tuyệt đối: Bắt buộc khách mặc quần áo dài, mang tất cao cổ. Bạn phải chuẩn bị sẵn thuốc chống vắt và xịt côn trùng cao cấp cho cả đoàn trước khi vào rừng.","Quy định rừng: Khi đi xe mui trần xem thú đêm, tuyệt đối nhắc khách giữ im lặng, không dùng đèn pin cá nhân chiếu vào mắt thú, không bật flash chụp ảnh"]', '[{"cat":"Địa lý & Sinh thái","content":"Vườn quốc gia Cát Tiên là Khu dự trữ sinh quyển thế giới. Bàu Sấu là vùng đất ngập nước Ramsar tầm cỡ quốc tế, nơi bảo tồn loài Cá Sấu Xiêm thuần chủng của Việt Nam."},{"cat":"Hành vi động vật (Thú đêm)","content":"Giải thích cho khách biết lý do xem thú đêm: Ban ngày rừng rậm rất nóng, các loài thú móng guốc như nai, móp, mển thường ẩn nấp. Khi đêm xuống, nhiệt độ hạ, chúng mới ra các trảng cỏ trống để tìm thức ăn và muối khoáng."}]', CURRENT_TIMESTAMP, TRUE, 14.0, FALSE, 0),
(8, 'Bình Minh Cồn Cát – Mũi Né', 'muine', '11 Giờ', 6500000, 10, 'Đón những vệt nắng đầu ngày rực rỡ trên sa mạc cát mênh mông.', 'Trải nghiệm cảm giác phẩn khích vượt đồi cát bằng xe ATV đón bình minh, khám phá dòng suối Tiên huyền thoại và thưởng thức mỹ vị biển khơi.', '11 Giờ (04:30 - 15:30) | Sức chứa: 10 khách.', '["Giờ giấc: Khởi hành rất sớm (04:30), nhắc khách ngủ sớm từ tối hôm trước.","Trang phục: Mang theo kính râm, kem chống nắng đầy đủ vì đồi cát bắt nắng rất mạnh. Khi lội Suối Tiên phải đi chân trần, nhắc khách mang dép dễ tháo rời"]', '[{"cat":"Địa lý & Địa chất","content":"Bàu Trắng (Đồi Cát Trắng) là một sa mạc thu nhỏ được bao quanh bởi hồ nước ngọt tự nhiên khổng lồ. Điểm kỳ thú ở đây là gió thổi liên tục làm thay đổi hình dáng của các đụn cát theo từng giờ (\"đồi cát di động\")."},{"cat":"Suối Tiên","content":"Bản chất không phải là một dòng suối thông thường, mà là một khe nước nhỏ chảy khuất sau những đồi cát, trải qua năm tháng bào mòn các vách đất sét, tạo nên những tháp cát màu đỏ cam rực rỡ như những lâu đài thạch nhũ lộ thiên."}]', CURRENT_TIMESTAMP, TRUE, 11.0, FALSE, 0),
(9, 'Khúc Ca San Hô – Phú Quốc', 'phuquoc', '10 Giờ', 8500000, 12, 'Hòa mình vào làn nước xanh lục bảo và vũ điệu rực rỡ dưới lòng đại dương.', 'Đồng hành cùng cano cao cấp lướt qua những hòn đảo hoang sơ, lặn ngắm rạn san hô đa sắc màu và tận hưởng bữa chiều ngắm hoàng hôn lãng mạn trên biển.', '10 Giờ (08:30 - 18:30) | Sức chứa: 12 khách.', '["Hậu cần biển: Kiểm tra kỹ áo phao, kính lặn ống thở xem có vừa vặn với kích thước của từng khách hay không.","Bảo vệ môi trường: Nhắc nhở khách tuyệt đối không dẫm đạp lên san hô, không nhặt san hô sống hoặc sao biển mang về đất liền"]', '[{"cat":"Địa lý biển","content":"Vùng biển phía Nam Phú Quốc (quần đảo An Thới) nằm trong vịnh Thái Lan, khu vực biển ấm, ít sóng lớn, tạo điều kiện hoàn hảo cho các rạn san hô phiến và san hô gạc nai phát triển mạnh mẽ."},{"cat":"Văn hóa bản địa","content":"Phú Quốc còn gọi là Đảo Ngọc vì vùng biển này có độ mặn và nhiệt độ lý tưởng cho nghề nuôi cấy ngọc trai cao cấp."}]', CURRENT_TIMESTAMP, TRUE, 10.0, FALSE, 0),
(10, 'Hương Sắc Miệt Vườn – Cần Thơ', 'cantho', '10 Giờ', 6800000, 12, 'Tròn vị ngọt ngào của trái chín trĩu cành miền sông nước.', 'Đón bình minh trên chiếc ghe máy mộc mạc phục vụ riêng, hòa mình vào chợ nổi Cái Răng sầm uất, khám phá miệt vườn trĩu quả và học làm bánh dân gian Nam Bộ.', '10 Giờ (05:30 - 15:30) | Sức chứa: 12 khách.', '["An toàn bến bãi: Lúc bước từ bờ xuống ghe máy tại bến tàu sương sớm rất dễ trơn trượt, Guide luôn phải đứng ở mạn thuyền để đỡ tay cho khách.","Vệ sinh an toàn thực phẩm: Khi khách ăn hủ tiếu trên sông, đảm bảo đũa muỗng sạch sẽ, chuẩn bị sẵn khăn giấy cao cấp"]', '[{"cat":"Văn hóa sông nước","content":"Chợ nổi Cái Răng hình thành từ thời chưa có đường bộ, người dân lấy sông ngòi làm đường đi, ghe xuồng làm nhà. Điểm nhấn là \"Cây bẹo\" (gồm một cây sào tre cắm trước mũi ghe, treo sản vật gì lên đó là thông báo cho người mua biết ghe mình bán món đó - \"treo gì bán nấy\")."},{"cat":"Ẩm thực & Con người","content":"Giới thiệu phong vị hào sảng, hiếu khách của người miền Tây qua cách họ làm bánh dân gian hay chế biến món cá lóc nướng trui (cá bắt dưới sông lên, xiên bằng thanh tre, phủ rơm đốt rụi rồi cạo lớp vảy đen ra thịt trắng thơm phức)."}]', CURRENT_TIMESTAMP, TRUE, 10.0, FALSE, 0);


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
(909, 909, 'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4', TRUE),
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

(909, 909, 1, 'Đại Dương Phú Quốc', 'Lặn cano 4 đảo nhỏ hoang sơ hoà vào san hô rực rỡ.'),
(10, 10, 1, 'Sông Nước Cần Thơ', 'Ăn sáng hủ tiếu chợ nổi Cái Răng sôi động.');

-- ── 31. Tour Itinerary Details (10 rows) ─────────────────────
INSERT INTO Tour_Itinerary_Details (detail_id, itinerary_id, start_time, end_time, location_id, activity_title, activity_description, meal_type) VALUES
(1, 1, '08:00:00', '11:30:00', NULL, 'Dấu xưa rêu phong – Khám phá Đại Nội', 'Dạo bước qua những lầu son gác tía, lắng nghe tiếng vọng của thời gian tại Điện Thái Hòa, Thế Miếu – nơi lưu giữ trọn vẹn nét uy nghi một thuở hoàng kim.', NULL),
(2, 1, '11:30:00', '14:00:00', NULL, 'Vị đượm cung đình – Thưởng yến và nếm trà', 'Thưởng thức bữa trưa được tái hiện theo phong cách cung đình. Sau đó, ghé thăm Duyệt Thị Đường để nhấp ngụm trà thảo mộc thanh mát, được pha chế theo công thức tiến vua ngày trước.', NULL),
(3, 1, '14:00:00', '18:00:00', NULL, 'Sông Hương bảng lảng – Thuyền rồng đón hoàng hôn', 'Ngồi trên mạn thuyền rồng, thong dong ngắm dòng sông Hương êm đềm, thả hồn theo điệu hò xứ Huế và đón ánh chiều tà buông chậm xuống những mái ngói cổ kính.', NULL),
(4, 2, '08:30:00', '11:00:00', NULL, 'Hồn của đất – Làng gốm Thanh Hà', 'Bên dòng sông Thu Bồn lộng gió, bạn sẽ được ngắm nhìn các nghệ nhân thổi hồn vào đất và tự tay xoay vần, nhào nặn nên một món quà kỷ niệm của riêng mình.', NULL),
(5, 2, '11:00:00', '13:00:00', NULL, 'Hương đồng cỏ nội – Làng rau Trà Quế', 'Hòa mình vào không gian xanh mướt của một trong những vườn rau lâu đời nhất. Trải nghiệm một ngày làm nông nhẹ nhàng, xới đất gieo mầm và thưởng thức ngụm nước hạt sen thanh mát.', NULL),
(6, 2, '13:00:00', '15:30:00', NULL, 'Vị quê đậm đà – Ẩm thực sân vườn', 'Thưởng thức bữa trưa ấm cúng với những món ăn đặc sản xứ Quảng, được chế biến từ chính nguồn nông sản tươi ngon vừa thu hoạch tại vườn.', NULL),
(7, 2, '15:30:00', '18:30:00', NULL, 'Hoài phố rêu phong – Tản bộ và thưởng trà chiều', 'Di chuyển về trung tâm Phố cổ Hội An, thong thả dạo bước qua những nếp nhà vàng cổ kính và dừng chân thưởng thức trà thảo mộc ngắm hoàng hôn buông trên dòng sông Hoài.', NULL),
(8, 3, '09:00:00', '11:30:00', NULL, 'Sợi cói dệt nắng – Làng chiếu Kim Sơn', 'Ngắm nhìn quy trình dệt nên những chiếc chiếu cói tự nhiên đượm hương đồng nội. Bạn sẽ hiểu vì sao những sợi cỏ mềm mại qua tay người thợ lại trở thành những tác phẩm tinh xảo đến vậy.', NULL),
(9, 3, '11:30:00', '14:00:00', NULL, 'Hương vị cố đô – Thưởng thức đặc sản', 'Ghé thăm nhà hàng cao cấp ven sông để thưởng thức bữa trưa đậm đà vị núi rừng với thịt dê truyền thống, cơm cháy giòn rụm và các sản vật địa phương.', NULL),
(10, 3, '14:00:00', '16:30:00', NULL, 'Đốm hoa trên gấm – Làng thêu Văn Lâm', 'Thu mình vào không gian yên ắng của làng thêu cổ, lắng nghe những câu chuyện xưa và học cách đưa thoi, điểm nhụy để tự tay dệt nên những nét hoa văn truyền thống tinh tế.', NULL),
(11, 3, '16:30:00', '19:00:00', NULL, 'Sơn thủy hữu tình – Thuyền nan lướt bóng hoàng hôn', 'Di chuyển đến đầm bảo tồn Vân Long, ngồi trên chiếc thuyền nan mộc mạc rẽ nước qua những hốc đá, ngắm nhìn đàn voọc quý hiếm và đón chiều tà buông xuống lòng thung lũng tĩnh mịch.', NULL),
(12, 4, '08:00:00', '11:00:00', NULL, 'Đón sương sớm – Thả thuyền ngắm sen', 'Ngồi trên chiếc xuồng ba lá lướt nhẹ giữa đầm sương, ngắm nhìn những đóa sen hồng bung nở trong nắng sớm và hít hà hương thơm tinh khôi của đất trời.', NULL),
(13, 4, '11:00:00', '13:30:00', NULL, 'Tâm an giữa đầm – Thiền hành và thưởng trà', 'Thong thả dạo bước trên những cây cầu gỗ nhỏ giữa lòng đầm, rũ bỏ những lo toan. Sau đó, tĩnh lặng thưởng thức ngụm trà sen ủ qua đêm ngọt hậu, thơm dịu.', NULL),
(14, 4, '13:30:00', '16:00:00', NULL, 'Hương sen kết tinh – Mỹ tiệc bách hoa', 'Thưởng thức bữa trưa độc đáo với thực đơn sáng tạo trọn vẹn từ sen: từ cơm hấp lá sen dẻo thơm, súp hạt sen bổ dưỡng cho đến đĩa gỏi ngó sen thanh mát, giòn rụm.', NULL),
(15, 4, '16:00:00', '18:00:00', NULL, 'Sông nước tịch mịch – Nghỉ ngơi tĩnh tại', 'Thư giãn tự do bên hiên nhà tre lộng gió, ngắm hoàng hôn nhuộm đỏ cánh đồng sen và lắng nghe tiếng chim bay về tổ, khép lại ngày bình yên.', NULL),
(16, 5, '08:00:00', '09:30:00', NULL, 'Hải trình sóng vỗ – Rẽ nước thưởng ngoạn', 'Du thuyền hạng sang khởi hành đưa bạn hòa mình vào không gian bao la của vịnh Hạ Long, ngắm nhìn những đảo đá vôi mang muôn vàn hình dáng kỳ thú.', NULL),
(17, 5, '09:30:00', '12:00:00', NULL, 'Tuyệt tác giấu kín – Khám phá động Thiên Cung', 'Dạo bước vào lòng động cùng hướng dẫn viên riêng, chiêm ngưỡng những khối thạch nhũ lấp lánh và lắng nghe những câu chuyện huyền thoại cổ xưa.', NULL),
(18, 5, '12:00:00', '14:30:00', NULL, 'Mỹ vị giữa trùng khơi – Thưởng thức tiệc trưa', 'Dùng bữa trưa hải sản cao cấp được chế biến tinh tế bởi đầu bếp trên tàu, trong khi du thuyền chầm chậm lướt qua những vùng vịnh tĩnh lặng, hoang sơ.', NULL),
(19, 5, '14:30:00', '16:30:00', NULL, 'Góc nhỏ tĩnh lặng – Chèo thuyền Kayak & Tắm biển', 'Tàu neo lại tại một bãi biển hoang vắng, bạn tự do chèo thuyền kayak len lỏi qua các hang luồn hoặc ngâm mình trong làn nước xanh mát.', NULL),
(20, 5, '16:30:00', '18:00:00', NULL, 'Khúc ca lộng gió – Tiệc trà ngắm hoàng hôn', 'Thư giãn trên boong tàu đón gió biển mát lành, thưởng thức tiệc trà chiều, bánh ngọt và ngắm ánh chiều tà vàng rực buông chậm xuống vịnh biển ngàn năm.', NULL),
(21, 6, '08:00:00', '11:30:00', NULL, 'Nẻo đường mây phủ – Thăm bản Cát Cát', 'Thong thả đi bộ dọc theo những lối mòn dẫn vào bản, ngắm nhìn những chiếc cối xay nước khổng lồ quay đều bên suối và trải nghiệm nghệ thuật nhuộm chàm cùng nghệ nhân bản địa.', NULL),
(22, 6, '11:30:00', '14:00:00', NULL, 'Bếp lửa vùng cao – Ẩm thực Tây Bắc', 'Dừng chân nghỉ ngơi tại không gian nhà hàng cao cấp view toàn cảnh thung lũng, thưởng thức bữa trưa ấm cúng với những món ăn đậm đà hương vị núi rừng.', NULL),
(23, 6, '14:00:00', '16:30:00', NULL, 'Tiếng hát của dòng suối – Thác Tiên Sa', 'Check-in dòng thác Tiên Sa bọt tung trắng xóa, tản bộ dọc bờ suối và lắng nghe những giai điệu khèn môi mộc mạc của người bản địa.', NULL),
(24, 6, '16:30:00', '19:00:00', NULL, 'Tĩnh lặng đại ngàn – Thưởng trà chiều ngắm hoàng hôn', 'Di chuyển về một quán trà bản địa không gian mở trên đỉnh đồi, thưởng thức ngụm trà cổ thụ, ngắm nhìn thung lũng Mường Hoa chìm dần vào sương mờ hoàng hôn.', NULL),
(25, 7, '08:00:00', '12:00:00', NULL, 'Dưới bóng đại ngàn – Trekking Bàu Sấu', 'Lội bộ dưới những tán cây cổ thụ trăm tuổi, hít hà bầu không khí trong lành của rừng nguyên sinh để đến với vùng đầm lầy Bàu Sấu hoang sơ.', NULL),
(26, 7, '12:00:00', '14:30:00', NULL, 'Bên bờ đầm vắng – Trưa xanh tĩnh lặng', 'Thưởng thức bữa trưa tinh tế được chuẩn bị riêng bên trạm kiểm lâm, ngắm nhìn những chú chim quý hiếm và cá sấu tự nhiên bơi lội dưới làn nước.', NULL),
(27, 7, '14:30:00', '18:00:00', NULL, 'Khúc giao mùa rừng già – Tản bộ lượt về', 'Quay trở lại trung tâm bằng đường rừng, đón ánh nắng chiều xuyên qua những kẽ lá và nghỉ ngơi, ngâm chân thảo mộc thư giãn tại resort bìa rừng.', NULL),
(28, 7, '18:00:00', '20:00:00', NULL, 'Hương vị đại ngàn – Bữa tối lãng mạn', 'Thưởng thức bữa tối ấm cúng tại nhà hàng ven sông của resort với các món ăn mang phong vị địa phương, lắng nghe tiếng côn trùng reo vang.', NULL),
(29, 7, '20:00:00', '22:00:00', NULL, 'Thanh âm bóng tối – Đi xe mui trần xem thú đêm', 'Lên xe chuyên dụng mui trần len lỏi vào bìa rừng, dưới ánh đèn chuyên dụng, bạn sẽ được tận mắt ngắm nhìn cuộc sống kiếm ăn ban đêm đầy thú vị của các loài thú hoang dã.', NULL),
(30, 8, '04:30:00', '05:30:00', NULL, 'Đường chạy ban mai – Hành trình ra đồi cát', 'Xe hạng sang đón bạn từ sớm khi trời còn đẫm sương để kịp di chuyển ra Đồi Cát Trắng xinh đẹp.', NULL),
(31, 8, '05:30:00', '07:30:00', NULL, 'Nắng ấm sa mạc – Xe địa hình & Đón bình minh', 'Riêng tư trải nghiệm xe địa hình ATV lao vút qua những đụn cát nhấp nhô, ngắm mặt trời nhô lên từ phía biển và chơi trượt cát bằng máng từ đỉnh đồi.', NULL),
(32, 8, '07:30:00', '09:30:00', NULL, 'Bức họa của gió – Đồi Cát Đỏ & Suối Tiên', 'Tiếp tục check-in Đồi Cát Đỏ lộng gió và dạo bước chân trần dưới làn nước mát rượi của dòng Suối Tiên, ngắm nhìn những bức vách đất sét màu cam rực rỡ.', NULL),
(33, 8, '09:30:00', '12:30:00', NULL, 'Sóng vỗ rì rào – Thư giãn tại Beach Club', 'Di chuyển về một resort sát biển cao cấp, tự do tắm hồ bơi, tắm biển hoặc nằm nghỉ ngơi dưới bóng mát, thưởng thức ly nước dừa mát lạnh.', NULL),
(34, 8, '12:30:00', '15:30:00', NULL, 'Vị mặn biển khơi – Bữa trưa hải sản thượng hạng', 'Thưởng thức bữa trưa với thực đơn hải sản tươi sống được đánh bắt trong ngày tại nhà hàng view biển sang trọng trước khi xe đưa bạn về lại điểm đón ban đầu.', NULL),
(35, 9, '08:30:00', '10:30:00', NULL, 'Lướt sóng trùng khơi – Khám phá các hòn đảo hoang', 'Cano tốc độ cao thế mới đưa bạn rời bến cảng, băng qua làn nước xanh ngắt để ghé thăm những hòn đảo nhỏ còn giữ nguyên nét hoang sơ.', NULL),
(36, 9, '10:30:00', '13:00:00', NULL, 'Vũ điệu đại dương – Bơi lặn ngắm san hô', 'Đeo ống thở và kính lặn chuyên dụng, thỏa thích hòa mình vào làn nước trong vắt để ngắm nhìn những rạn san hô đung đưa cùng từng đàn cá nhỏ rực rỡ sắc màu.', NULL),
(37, 9, '13:00:00', '15:30:00', NULL, 'Vị mặn của biển – Bữa trưa trên đảo hoang', 'Thưởng thức bữa trưa hải sản cao cấp tươi ngon ngay trên bãi biển cát trắng mịn, ngả lưng dưới bóng dừa mát rượi.', NULL),
(38, 9, '15:30:00', '18:30:00', NULL, 'Đảo ngọc hoàng hôn – Tiệc cocktail chiều tà', 'Cano đưa bạn đến một hòn đảo ngắm hoàng hôn đẹp nhất. Tại đây, bạn có thể thong thả đi dạo, thưởng thức ly cocktail mát lạnh và đón khoảnh khắc mặt trời lặn nhuộm hồng cả đường chân trời trước khi trở về bến cảng lúc 18:30.', NULL),
(39, 10, '05:30:00', '06:00:00', NULL, 'Sương sớm trên sông – Xuống bến xuống tàu', 'Đón ngày mới khi trời còn mờ sương, xuống chiếc ghe máy truyền thống được chuẩn bị riêng để bắt đầu chuyến hành trình dọc theo dòng sông Hậu.', NULL),
(40, 10, '06:00:00', '08:30:00', NULL, 'Âm thanh ngày mới – Chợ nổi Cái Răng', 'Len lỏi qua những chiếc ghe đơm đầy hoa trái, lắng nghe tiếng rao của người dân miền Tây và thưởng thức bữa sáng với tô hủ tiếu nóng hổi, chòng chành ngay trên mặt nước từ ghe hậu cần riêng của tour.', NULL),
(41, 10, '08:30:00', '11:30:00', NULL, 'Trái ngọt trĩu cành – Thăm vườn cây ăn trái', 'Ghé thăm một miệt vườn cây ăn trái sum suê, thong thả dạo bước dưới bóng mát, tự tay hái và thưởng thức quả chín mọng ngọt lịm ngay tại vườn.', NULL),
(42, 10, '11:30:00', '13:30:00', NULL, 'Trải nghiệm làm bánh dân gian', 'Đến với gian nhà cổ Nam Bộ, cùng các nghệ nhân miệt vườn học cách đổ bánh xèo, làm bánh tằm se tay và lắng nghe những câu chuyện đời sống mộc mạc.', NULL),
(43, 10, '13:30:00', '15:30:00', NULL, 'Phong vị sông nước – Bữa trưa điền dã', 'Thưởng thức bữa trưa đậm chất miền Tây với cá lóc nướng trui, lẩu mắm đậm đà tại nhà hàng lộng gió ven sông, nghỉ ngơi tĩnh tại trên những chiếc võng trước khi kết thúc hành trình.', NULL)

-- ── 32. Tour Prices (20 rows) ────────────────────────────────
INSERT INTO Tour_Prices (tour_price_id, tour_id, age_from, age_to, ticket_price, combo_discount_price, is_active) VALUES 
(1, 1, 12, 100, 7500000, 6800000, TRUE),
(2, 1, 2, 11, 3750000, 3400000, TRUE),
(3, 2, 12, 100, 5800000, 5200000, TRUE),
(4, 2, 2, 11, 2900000, 2600000, TRUE),
(5, 3, 12, 100, 6500000, 5800000, TRUE),
(6, 3, 2, 11, 3250000, 2900000, TRUE),
(7, 4, 12, 100, 8200000, 7500000, TRUE),
(8, 4, 2, 11, 4100000, 3750000, TRUE),
(9, 5, 12, 100, 7500000, 6800000, TRUE),
(10, 5, 2, 11, 3750000, 3400000, TRUE),
(11, 6, 12, 100, 8200000, 7500000, TRUE),
(12, 6, 2, 11, 4100000, 3750000, TRUE),
(13, 7, 12, 100, 9500000, 8600000, TRUE),
(14, 7, 2, 11, 4750000, 4300000, TRUE),
(15, 8, 12, 100, 6500000, 5800000, TRUE),
(16, 8, 2, 11, 3250000, 2900000, TRUE),
(17, 9, 12, 100, 8500000, 7800000, TRUE),
(18, 9, 2, 11, 4250000, 3900000, TRUE),
(19, 10, 12, 100, 6800000, 6000000, TRUE),
(20, 10, 2, 11, 3400000, 3000000, TRUE);

-- ── 33. Tour Schedules (10 rows) ─────────────────────────────
INSERT INTO Tour_Schedules (schedule_id, tour_id, departure_date, departure_time, booked_seats, schedule_status, is_insurance_processed) VALUES 
(1, 1, '2026-06-14', '08:00:00.000000', 0, 'Open', FALSE),
(2, 2, '2026-06-15', '14:00:00.000000', 0, 'Open', FALSE),
(3, 3, '2026-06-15', '17:00:00.000000', 0, 'Open', FALSE),
(4, 4, '2026-06-16', '09:00:00.000000', 0, 'Open', FALSE),
(5, 2, '2026-06-13', '08:00:00.000000', 5, 'Open', FALSE),
(6, 5, '2026-06-17', '08:00:00.000000', 0, 'Open', FALSE),
(7, 6, '2026-06-18', '09:00:00.000000', 0, 'Open', FALSE),
(8, 7, '2026-06-19', '07:30:00.000000', 0, 'Open', FALSE),
(909, 8, '2026-06-20', '05:00:00.000000', 0, 'Open', FALSE),
(10, 9, '2026-06-21', '09:00:00.000000', 0, 'Open', FALSE),
(11, 2, '2026-07-10', '08:00:00.000000', 0, 'Open', FALSE),
(12, 4, '2026-07-08', '07:30:00.000000', 0, 'Open', FALSE),
(13, 1, '2026-07-11', '07:30:00.000000', 0, 'Open', FALSE),
(14, 7, '2026-07-06', '19:00:00.000000', 0, 'Completed', FALSE),
(15, 8, '2026-07-13', '04:00:00.000000', 0, 'Cancelled', FALSE);

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
(10, 8, 7, 'GUIDE'),
(11, 11, 6, 'GUIDE'),
(12, 12, 6, 'GUIDE'),
(13, 13, 7, 'GUIDE'),
(14, 14, 7, 'GUIDE'),
(15, 15, 6, 'GUIDE');

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
INSERT IGNORE INTO Customers (customer_id, account_id, full_name, gender, cccd_passport_encrypted, phone, email, loyalty_points, membership_tier_id) VALUES
(16, 21, 'Nguyễn Minh Test', 'Nam', 'YTZ6KpDQsEJPEHfwR+3vNw==', '0911000001', 'testguest1@test.com', 50, 1),
(17, 22, 'Trần Thị Test', 'Nữ', 'L+rfuZSEQrLxAyYmz4xp9Q==', '0911000002', 'testguest2@test.com', 100, 2),
(18, 23, 'Lê Văn Test', 'Nam', 'Rdq+O8/+wOLP5PTwDytFRQ==', '0911000003', 'testguest3@test.com', 200, 3);

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
-- Test details: 'Active' không phải enum hợp lệ, phải dùng 'Checked_In'
-- detail 12: đổi room_id 11 → 14 (phòng 204, cat 2) – tránh double-claim với booking 505 (Ngọc Lan)
-- detail 13: đổi room_id 16 → 37 (phòng 407, cat 3) – đồng bộ logic phòng Wellness Retreats
INSERT IGNORE INTO Room_Booking_Details (detail_id, room_booking_id, category_id, room_id, room_charge, detail_status, bed_preference, special_requests, is_charge_to_room_allowed, sub_credit_limit, billing_routing_strategy) VALUES
(11, 21, 4,  6,  2000000, 'Checked_In', 'KING_SIZE', NULL, TRUE, 500000,  'BILL_TO_LEADER'),
(12, 22, 2,  14, 3500000, 'Checked_In', 'KING_SIZE', NULL, TRUE, 1500000, 'BILL_TO_LEADER'),
(13, 23, 3,  37, 8000000, 'Checked_In', 'TWIN_BED',  NULL, TRUE, 2000000, 'BILL_TO_LEADER');

-- ── 47. Test Room Guests ─────────────────────────────────────
INSERT IGNORE INTO Room_Guests (guest_id, detail_id, customer_id, dependent_id, guest_type, is_primary_contact) VALUES
(11, 11, 16, NULL, 'ADULT', TRUE),
(12, 12, 17, NULL, 'ADULT', TRUE),
(13, 13, 18, NULL, 'ADULT', TRUE);

-- ── 48. Cập nhật Rooms hiện tại đang được test guest ở ──────
UPDATE Rooms SET current_booking_detail_id = 11, room_status = 'Occupied' WHERE room_id = 6;
UPDATE Rooms SET current_booking_detail_id = 12, room_status = 'Occupied' WHERE room_id = 14;
UPDATE Rooms SET current_booking_detail_id = 13, room_status = 'Occupied' WHERE room_id = 37;

-- ── 51. Lê Hoàng Nam đặt 3 phòng 1 lượt ─────────────────────────────────────
-- RoomBooking IS-A Booking (Table-Per-Class Inheritance):
--   room_booking_id = booking_id (cùng một PK, bản ghi cha trong Bookings, con trong Room_Bookings)
-- Nam đặt 3 phòng riêng, check-in 2026-06-25, check-out 2026-06-28

-- Bước 1: 3 bản ghi Bookings (bảng cha) — mỗi cái là 1 phòng của Nam
INSERT IGNORE INTO Bookings (booking_id, customer_id, booking_date, total_price, booking_status, booking_source, applied_promotion_id, version) VALUES
-- Bookings 50,51,52: Hoàng Nam đặt 3 phòng, ngày check-in 25/06 đã qua → Checked_Out
(50, 1, '2026-06-22', 4500000, 'Checked_Out', 'Direct_Web', NULL, 1),
(51, 1, '2026-06-22', 4500000, 'Checked_Out', 'Direct_Web', NULL, 1),
(52, 1, '2026-06-22', 4500000, 'Checked_Out', 'Direct_Web', NULL, 1);

-- Bước 2: 3 bản ghi Room_Bookings (bảng con) với cùng ID → kế thừa từ Bookings
INSERT IGNORE INTO Room_Bookings (room_booking_id, check_in_date, check_out_date, deposit_amount, cancellation_deadline, credit_limit, personal_pin_hash) VALUES
(50, '2026-06-25', '2026-06-28', 1000000, '2026-06-23', 6000000, 'hash'),
(51, '2026-06-25', '2026-06-28', 1000000, '2026-06-23', 6000000, 'hash'),
(52, '2026-06-25', '2026-06-28', 1000000, '2026-06-23', 6000000, 'hash');

-- Bước 3: 3 Room_Booking_Details — bookings 50,51,52 đã Checked_Out, room_id=NULL
INSERT IGNORE INTO Room_Booking_Details (detail_id, room_booking_id, category_id, room_id, room_charge, detail_status, bed_preference, special_requests, is_charge_to_room_allowed, sub_credit_limit, billing_routing_strategy, customer_id) VALUES
(50, 50, 7, NULL, 4500000, 'Checked_Out', 'KING_SIZE', 'Cần thêm giường phụ cho trẻ em', TRUE, 1000000, 'BILL_TO_LEADER', 1),
(51, 51, 7, NULL, 4500000, 'Checked_Out', 'TWIN_BED',  NULL,                              TRUE, 1000000, 'BILL_TO_LEADER', 2),
(52, 52, 7, NULL, 4500000, 'Checked_Out', 'TWIN_BED',  'Tầng cao, view đẹp',              TRUE, 1000000, 'BILL_TO_LEADER', 3);

-- Không cập nhật Rooms 21,22,23 vì bookings 50,51,52 đã Checked_Out → phòng vẫn Vacant_Clean

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

-- Tour bookings 200,201: đổi từ room_booking_id=14 (Checked_Out) sang 23 (Confirmed — hợp lý cho phân bổ khi check-in)
INSERT IGNORE INTO Tour_Bookings (booking_id, schedule_id, participant_count, tour_charge, room_booking_id, room_booking_detail_id, is_walk_in_tour) VALUES
(200, 1, 2, 1200000, 23, NULL, FALSE),
(201, 2, 5, 3000000, 23, NULL, FALSE),
(202, 3, 2, 1200000, 23, NULL, FALSE);

ALTER TABLE Tour_Bookings AUTO_INCREMENT = 300;


-- ── 49. Export History (Mock Data) ───────────────────────────

INSERT IGNORE INTO Export_History (id, report_name, format, exported_at, exported_by, file_size) VALUES
(1, 'Doanh thu tháng 5/2026', 'Excel', '2026-06-01 09:15:00', 'Manager ', '2.4 MB'),
(2, 'Tỷ lệ lấp đầy Q2', 'PDF', '2026-05-30 14:30:00', 'Manager ', '1.1 MB'),
(3, 'Báo cáo tour tháng 4', 'CSV', '2026-05-02 10:00:00', 'Manager ', '320 KB'),
(4, 'Doanh thu năm 2025', 'Excel', '2026-01-15 08:45:00', 'Manager ', '5.8 MB');


-- ── 50. Mock Data for YoY Comparison (Năm 2025) ───────────────────────────
INSERT INTO Bookings (booking_id, customer_id, booking_date, total_price, booking_status, booking_source, applied_promotion_id, version) VALUES
-- YoY booking 991: Hoàng Nam (customer 1), đã lưu trú năm 2025 → Checked_Out
(991, 1, '2025-06-01', 65000000, 'Checked_Out', 'Direct_Web', NULL, 1),
(992, 2, '2025-06-05', 45000000, 'Confirmed',   'OTA',        NULL, 1);

INSERT INTO Room_Bookings (room_booking_id, check_in_date, check_out_date, deposit_amount, cancellation_deadline, credit_limit, personal_pin_hash) VALUES
(991, '2025-06-10', '2025-06-15', 5000000, '2025-06-05', 10000000, 'hash');

INSERT INTO Room_Booking_Details (detail_id, room_booking_id, category_id, room_id, room_charge, detail_status, bed_preference, special_requests, is_charge_to_room_allowed, sub_credit_limit, billing_routing_strategy, customer_id) VALUES
(991, 991, 1, 1, 65000000, 'Checked_Out', 'KING_SIZE', NULL, TRUE, 500000, 'BILL_TO_LEADER', 1);

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


INSERT IGNORE INTO Dependents (dependent_id, customer_id, dependent_name, birth_date, gender, cccd_passport_encrypted) VALUES 
(501, 501, 'Wife Nguyễn Thị B', '1995-08-15', 'Nữ', NULL),
(502, 501, 'Kid Nguyễn Văn C', '2015-05-20', 'Nam', NULL);

-- Additional Rooms (just in case) - Floor 8 rooms deleted

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
-- detail 499: cat 5 (Presidential) đã Checked_Out → room_id=NULL (không giữ số phòng sau checkout, vả lại room 15 thuộc cat 2 ≠ cat 5)
(499, 499, 5, NULL, 52000000, 'Checked_Out', 'KING_SIZE', 'Tuần trăng mật', TRUE, 5000000, 'BILL_TO_LEADER', 501);

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
-- 1. Checked_In for VIP (Room 801) - Deleted

-- 2. Confirmed for Normal Customer (Ready for Check-in)
INSERT IGNORE INTO Bookings (booking_id, customer_id, booking_date, total_price, booking_status, booking_source, applied_promotion_id, version) VALUES
(502, 502, '2026-06-28', 1800000, 'Confirmed', 'Direct_Web', NULL, 1);
INSERT IGNORE INTO Room_Bookings (room_booking_id, check_in_date, check_out_date, deposit_amount, cancellation_deadline, credit_limit, personal_pin_hash) VALUES
(502, '2026-07-15', '2026-07-17', 500000, '2026-07-10', 2000000, 'hash');
INSERT IGNORE INTO Room_Booking_Details (detail_id, room_booking_id, category_id, room_id, room_charge, detail_status, bed_preference, special_requests, is_charge_to_room_allowed, sub_credit_limit, billing_routing_strategy, customer_id) VALUES
-- detail 502: booking đang Confirmed chưa check-in → detail_status phải là 'Pending' (không phải 'Confirmed')
(502, 502, 2, NULL, 1800000, 'Pending', 'TWIN_BED', 'Phòng yên tĩnh', TRUE, 2000000, 'INDIVIDUAL', 502);

-- 3. Pending_Approval (Triggered workflow)
INSERT IGNORE INTO Bookings (booking_id, customer_id, booking_date, total_price, booking_status, booking_source, applied_promotion_id, version) VALUES
(503, 502, '2026-06-29', 5000000, 'Pending_Approval', 'Direct_Web', 501, 1);
INSERT IGNORE INTO Room_Bookings (room_booking_id, check_in_date, check_out_date, deposit_amount, cancellation_deadline, credit_limit, personal_pin_hash) VALUES
(503, '2026-08-01', '2026-08-05', 0, '2026-07-25', 0, 'hash');
INSERT IGNORE INTO Room_Booking_Details (detail_id, room_booking_id, category_id, room_id, room_charge, detail_status, bed_preference, special_requests, is_charge_to_room_allowed, sub_credit_limit, billing_routing_strategy, customer_id) VALUES
(503, 503, 1, NULL, 5000000, 'Pending', 'KING_SIZE', 'Áp mã giảm sâu', TRUE, 0, 'INDIVIDUAL', 502);

-- F&B Orders (Lifecycles)
INSERT IGNORE INTO Food_Orders (order_id, booking_id, guest_id, staff_id, order_type, table_number, total_amount, order_status, payment_status, notes) VALUES
(504, NULL, NULL, 3, 'DINE_IN', 'T07', 700000, 'PAID', 'PAID', 'Khách vãng lai'),
(505, NULL, NULL, 2, 'DINE_IN', 'T08', 0, 'CANCELLED', 'UNPAID', 'Khách đổi ý');


INSERT IGNORE INTO Food_Order_Details (detail_id, order_id, item_id, quantity, unit_price, subtotal) VALUES
(506, 504, 2, 2, 350000, 700000);

-- Folios linked - Deleted

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

-- UPDATE Room_Booking_Details SET number_of_adults = 2, number_of_children = 0 WHERE number_of_adults IS NULL;


-- ============================================================
-- APPENDED BOOKING 103: MULTIPLE ROOMS + PRE-REGISTERED GUESTS
-- ============================================================
INSERT INTO Bookings (booking_id, customer_id, booking_date, total_price, booking_status, booking_source, applied_promotion_id, version) VALUES
(103, 1, '2026-06-27', 18000000, 'Confirmed', 'Direct_Web', NULL, 1);

INSERT INTO Room_Bookings (room_booking_id, check_in_date, check_out_date, deposit_amount, cancellation_deadline, credit_limit, personal_pin_hash) VALUES
(103, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 2 DAY), 2000000, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 10000000, 'hash103');


-- ============================================================
-- DATA TEST NGHIỆP VỤ MANAGER APPROVALS & REFUNDS
-- ============================================================

-- 1. Thêm Booking chờ duyệt vượt hạn mức chiết khấu (Booking ID: 301)
INSERT INTO Bookings (booking_id, customer_id, booking_date, total_price, booking_status, booking_source, applied_promotion_id, version) VALUES
(301, 1, '2026-06-28', 3000000, 'Pending_Approval', 'Direct_Web', 1, 1);

INSERT INTO Room_Bookings (room_booking_id, check_in_date, check_out_date, deposit_amount, cancellation_deadline, credit_limit, personal_pin_hash) VALUES
(301, '2026-07-10', '2026-07-12', 1000000, '2026-07-08', 5000000, 'hash');

INSERT INTO Room_Booking_Details (detail_id, room_booking_id, category_id, room_id, room_charge, detail_status, bed_preference, special_requests, is_charge_to_room_allowed, sub_credit_limit, billing_routing_strategy, number_of_adults, number_of_children) VALUES
(3011, 301, 1, 4, 3000000, 'Pending', 'KING_SIZE', 'Cần duyệt chiết khấu vượt hạn mức 35%', TRUE, 5000000, 'BILL_TO_LEADER', 2, 0);

INSERT INTO Room_Guests (guest_id, detail_id, customer_id, dependent_id, guest_type, is_primary_contact) VALUES
(30111, 3011, 1, NULL, 'ADULT', TRUE);

-- Tác vụ phê duyệt dành cho Manager
INSERT INTO Hotel_Operations (task_id, room_id, staff_id, supervisor_id, operational_type, priority, status, created_at, started_at, completed_at, notes) VALUES 
(901, 4, 1, 1, 'Manager_Approval', 'High', 'Pending', CURRENT_TIMESTAMP, NULL, NULL, 'Mã giảm giá SUMMER2026 áp dụng vượt ngưỡng (15.0% > 10.0%). Yêu cầu phê duyệt cho booking ID: 301'),
(902, 1, 1, 1, 'Late_Checkout_Waiver', 'Normal', 'Pending', CURRENT_TIMESTAMP, NULL, NULL, 'Khách trả phòng trễ 3 tiếng do trời mưa bão. Xin miễn phí phụ thu trả phòng trễ cho booking ID: 1'),
(903, 2, 1, 1, 'Cancellation_Fee_Waiver', 'High', 'Pending', CURRENT_TIMESTAMP, NULL, NULL, 'Khách gặp tai nạn không thể đến nhận phòng. Xin miễn 100% phí phạt hủy cho booking ID: 2'),
(904, 3, 1, 1, 'Room_Downgrade_Refund', 'High', 'Pending', CURRENT_TIMESTAMP, NULL, NULL, 'Máy lạnh phòng Deluxe hỏng, khách đồng ý xuống hạng Superior. Xin duyệt hoàn tiền chênh lệch 500k cho booking ID: 3');


-- 2. Thêm các yêu cầu hoàn tiền (Refund Requests)
INSERT INTO Refund_Requests (id, order_id, room_booking_id, tour_booking_id, bank_name, account_number, account_name, phone_number, amount, status, manager_note, evidence_image_url, created_at, completed_at) VALUES
(1, NULL, 15, NULL, 'Vietcombank', '10129384829', 'NGUYEN XUAN LOC', '0900000101', 1000000.00, 'Pending', NULL, NULL, CURRENT_TIMESTAMP, NULL),
-- Refund id=2: sửa amount 2,500,000 → 1,000,000 (không hoàn vượt số tiền cọc đã thu của booking 14 = 1,000,000)
(2, NULL, 14, NULL, 'Techcombank', '19033482938', 'TRAN THI BICH', '0900000104', 1000000.00, 'COMPLETED', 'Đã chuyển khoản hoàn tiền cọc qua ứng dụng ngân hàng', 'https://images.unsplash.com/photo-1554415707-6e8cfc93fe23?w=500', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO Room_Booking_Details (detail_id, room_booking_id, category_id, room_id, room_charge, detail_status, bed_preference, special_requests, is_charge_to_room_allowed, sub_credit_limit, billing_routing_strategy, number_of_adults, number_of_children) VALUES
(1031, 103, 8, NULL, 4500000, 'Pending', 'KING_SIZE', 'Gần thang máy', TRUE, 1000000, 'BILL_TO_LEADER', 2, 1),
(1032, 103, 8, NULL, 4500000, 'Pending', 'TWIN_BED', 'Gần phòng 26', TRUE, 1000000, 'BILL_TO_LEADER', 2, 1);

INSERT INTO Room_Guests (guest_id, detail_id, customer_id, dependent_id, guest_type, is_primary_contact) VALUES
(10311, 1031, 1, NULL, 'ADULT', TRUE),
(10312, 1031, NULL, 1, 'CHILD', FALSE), -- Lê Hoàng Minh
(10321, 1032, NULL, 2, 'CHILD', FALSE); -- Lê Thị Hồng

-- ============================================================
-- APPENDED BOOKING 505: NGOC LAN CHECKED-IN WITH 2 ROOMS (201 & 202)
-- ============================================================
INSERT IGNORE INTO Accounts (account_id, username, password_hash, is_active, role_id, created_at) VALUES 
(505, 'ngoclan_customer', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 10, CURRENT_TIMESTAMP);

INSERT IGNORE INTO Customers (customer_id, account_id, full_name, email, phone, gender, cccd_passport_encrypted, loyalty_points, membership_tier_id) VALUES
(505, 505, 'Ngọc Lan', 'ngoclan@example.com', '0999888775', 'Nữ', '001099000505', 100, 1);

INSERT IGNORE INTO Dependents (dependent_id, customer_id, dependent_name, birth_date, gender, cccd_passport_encrypted) VALUES 
(505, 505, 'Nguyễn Văn Bạn', '1996-03-10', 'Nam', NULL),
(506, 505, 'Trần Thị Bạn', '1998-11-25', 'Nữ', NULL);

INSERT IGNORE INTO Bookings (booking_id, customer_id, booking_date, total_price, booking_status, booking_source, applied_promotion_id, version) VALUES
(505, 505, '2026-07-01', 7000000, 'Checked_In', 'Direct_Web', NULL, 1);

INSERT IGNORE INTO Room_Bookings (room_booking_id, check_in_date, check_out_date, deposit_amount, cancellation_deadline, credit_limit, personal_pin_hash) VALUES
(505, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 2 DAY), 1000000.00, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 5000000.00, 'hash505');

INSERT IGNORE INTO Room_Booking_Details (detail_id, room_booking_id, category_id, room_id, room_charge, detail_status, bed_preference, special_requests, is_charge_to_room_allowed, sub_credit_limit, billing_routing_strategy, customer_id, number_of_adults, number_of_children) VALUES
(5051, 505, 2, 12, 3500000, 'Checked_In', 'KING_SIZE', 'Ngọc Lan phòng 1', TRUE, 2500000, 'BILL_TO_LEADER', 505, 2, 0),
(5052, 505, 2, 13, 3500000, 'Checked_In', 'TWIN_BED', 'Ngọc Lan phòng 2', TRUE, 2500000, 'BILL_TO_LEADER', 505, 2, 0);

INSERT IGNORE INTO Room_Guests (guest_id, detail_id, customer_id, dependent_id, guest_type, is_primary_contact) VALUES
(50511, 5051, 505, NULL, 'ADULT', TRUE),
(50512, 5051, NULL, 505, 'ADULT', FALSE),
(50521, 5052, 505, NULL, 'ADULT', TRUE),
(50522, 5052, NULL, 506, 'ADULT', FALSE);

INSERT IGNORE INTO Payment_Transactions (id, booking_id, amount, status, transaction_type, payment_method, gateway_status, transaction_ref, created_at, paid_at) VALUES
(5051, 505, 1000000.00, 'SUCCESS', 'Deposit', 'VNPAY', 'SUCCESS', 'DEP505_1783478433300', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

UPDATE Rooms SET current_booking_detail_id = 5051, room_status = 'Occupied' WHERE room_id = 12;
UPDATE Rooms SET current_booking_detail_id = 5052, room_status = 'Occupied' WHERE room_id = 13;



-- FIX HOANGNAM blocks đã được loại bỏ: booking 1, 50, 51, 52, 991 đã được fix trực tiếp tại các INSERT bên trên.
-- Không dùng UPDATE patches để vá dữ liệu (nguyên tắc nhất quán seed data).


-- FIX GUEST REQUEST TASK OPERATIONAL TYPE
UPDATE Hotel_Operations SET operational_type = 'GUEST_REQUEST' WHERE operational_type = 'CHECKOUT_CLEAN' AND notes LIKE '%[Khách Yêu Cầu Dọn Phòng]%';


-- RECALCULATE LOYALTY POINTS BASED ON MOCK DATA
UPDATE Customers c SET loyalty_points = (
    COALESCE((
        SELECT SUM(b.total_price) 
        FROM Bookings b 
        WHERE b.customer_id = c.customer_id AND b.booking_status IN ('Checked_Out', 'Completed')
    ), 0) + 
    COALESCE((
        SELECT SUM(fi.amount) 
        FROM Folio_Items fi 
        JOIN Bookings b ON fi.booking_id = b.booking_id
        WHERE b.customer_id = c.customer_id AND b.booking_status = 'Checked_Out'
    ), 0)
) / 10000;

-- AUTO UPGRADE TIERS BASED ON RECALCULATED POINTS
UPDATE Customers c SET membership_tier_id = (
    SELECT tier_id FROM Membership_Tiers 
    WHERE c.loyalty_points >= points_from AND c.loyalty_points <= points_to 
    ORDER BY points_from DESC LIMIT 1
);


-- RECONCILE AGGREGATED DATA TOTALS
-- 1. Reconcile Bookings total_price for Room Bookings
UPDATE Bookings b SET total_price = (
    SELECT COALESCE(SUM(room_charge), 0) 
    FROM Room_Booking_Details 
    WHERE room_booking_id = b.booking_id
) WHERE EXISTS (SELECT 1 FROM Room_Bookings rb WHERE rb.room_booking_id = b.booking_id);

-- 2. Reconcile Bookings total_price for Tour Bookings
UPDATE Bookings b SET total_price = (
    SELECT COALESCE(SUM(tour_charge), 0) 
    FROM Tour_Bookings 
    WHERE booking_id = b.booking_id
) WHERE EXISTS (SELECT 1 FROM Tour_Bookings tb WHERE tb.booking_id = b.booking_id);

-- 3. Reconcile Room_Bookings credit_limit based on sub_credit_limit
UPDATE Room_Bookings rb SET credit_limit = (
    SELECT COALESCE(SUM(sub_credit_limit), 0) 
    FROM Room_Booking_Details 
    WHERE room_booking_id = rb.room_booking_id
);


-- FIX PAST CHECKED_IN BOOKINGS TO CURRENT DATE
UPDATE Room_Bookings rb 
JOIN Bookings b ON rb.room_booking_id = b.booking_id 
SET rb.check_in_date = CURDATE(), 
    rb.check_out_date = DATE_ADD(CURDATE(), INTERVAL 3 DAY), 
    rb.cancellation_deadline = DATE_SUB(CURDATE(), INTERVAL 1 DAY) 
WHERE b.booking_status = 'Checked_In' AND rb.check_in_date < CURDATE();

UPDATE Bookings b 
JOIN Room_Bookings rb ON b.booking_id = rb.room_booking_id 
SET b.booking_date = DATE_SUB(rb.check_in_date, INTERVAL 7 DAY) 
WHERE b.booking_status = 'Checked_In' AND b.booking_date < DATE_SUB(CURDATE(), INTERVAL 30 DAY);

-- Booking 52 đã được fix tại INSERT (Checked_Out, room_id=NULL) – không cần UPDATE patches nữa.


-- CREATE AN ALWAYS-ACTIVE BOOKING FOR HOANG NAM (CUSTOMER_ID = 1)
INSERT IGNORE INTO Bookings (booking_id, customer_id, booking_date, total_price, booking_status, booking_source, version) 
VALUES (9999, 1, DATE_SUB(CURDATE(), INTERVAL 5 DAY), 5000000, 'Checked_In', 'Direct_Web', 1);

INSERT IGNORE INTO Room_Bookings (room_booking_id, check_in_date, check_out_date, deposit_amount, cancellation_deadline, credit_limit, personal_pin_hash) 
VALUES (9999, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 3 DAY), 2000000, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 5000000, 'hash9999');

INSERT IGNORE INTO Room_Booking_Details (detail_id, room_booking_id, category_id, room_id, room_charge, detail_status, bed_preference, is_charge_to_room_allowed, sub_credit_limit, billing_routing_strategy, customer_id, number_of_adults, number_of_children) 
VALUES (99991, 9999, 1, 5, 5000000, 'Checked_In', 'KING_SIZE', TRUE, 5000000, 'INDIVIDUAL', 1, 1, 0);

INSERT IGNORE INTO Room_Guests (guest_id, detail_id, customer_id, guest_type, is_primary_contact) 
VALUES (999911, 99991, 1, 'ADULT', TRUE);

UPDATE Rooms SET current_booking_detail_id = 99991, room_status = 'Occupied' WHERE room_id = 5;


-- FIX BOOKING 9: đã sửa room_charge trong INSERT (detail 11, room_booking_id=9).
-- Reconcile tự động cập nhật total_price. UPDATE dưới dùng để đảm bảo deposit khớp.
UPDATE Room_Bookings SET deposit_amount = 3000000 WHERE room_booking_id = 9;

-- DYNAMICALLY ASSIGN SUB CREDIT LIMIT BASED ON MEMBERSHIP TIER FOR ALL ACTIVE/CONFIRMED BOOKINGS
UPDATE Room_Booking_Details rbd
JOIN Room_Bookings rb ON rbd.room_booking_id = rb.room_booking_id
JOIN Bookings b ON rb.room_booking_id = b.booking_id
JOIN Customers c ON b.customer_id = c.customer_id
JOIN Membership_Tiers mt ON c.membership_tier_id = mt.tier_id
SET rbd.sub_credit_limit = mt.credit_limit
WHERE b.booking_status IN ('Checked_In', 'Confirmed');

-- RECALCULATE TOTAL CREDIT LIMIT FOR THE BOOKING
UPDATE Room_Bookings rb
JOIN Bookings b ON rb.room_booking_id = b.booking_id
SET rb.credit_limit = (
    SELECT COALESCE(SUM(sub_credit_limit), 0) 
    FROM Room_Booking_Details 
    WHERE room_booking_id = rb.room_booking_id
)
WHERE b.booking_status IN ('Checked_In', 'Confirmed');

-- FALLBACK UPDATE FOR NUMBER OF GUESTS IF NULL
UPDATE Room_Booking_Details SET number_of_adults = 2, number_of_children = 0 WHERE number_of_adults IS NULL;
-- Reset AUTO_INCREMENT để tránh trùng khóa chính khi lưu đánh giá mới
ALTER TABLE Reviews AUTO_INCREMENT = 100;

-- -- XX. Shifts & Staff Schedules -----------------------------------
INSERT INTO Shifts (shift_id, shift_name, start_time, end_time, description) VALUES
(1, 'Ca Sáng', '06:00:00', '14:00:00', 'Ca làm việc buổi sáng'),
(2, 'Ca Chiều', '14:00:00', '22:00:00', 'Ca làm việc buổi chiều'),
(3, 'Ca Đêm', '22:00:00', '06:00:00', 'Ca trực đêm');

INSERT INTO Staff_Schedules (schedule_id, employee_id, shift_id, work_date, status) VALUES
(1, 1, 1, DATE_ADD(CURRENT_DATE, INTERVAL -7 DAY), 'Published'),
(2, 2, 2, DATE_ADD(CURRENT_DATE, INTERVAL -7 DAY), 'Published'),
(3, 3, 2, DATE_ADD(CURRENT_DATE, INTERVAL -7 DAY), 'Published'),
(4, 4, 1, DATE_ADD(CURRENT_DATE, INTERVAL -7 DAY), 'Published'),
(5, 6, 2, DATE_ADD(CURRENT_DATE, INTERVAL -7 DAY), 'Published'),
(6, 7, 1, DATE_ADD(CURRENT_DATE, INTERVAL -7 DAY), 'Published'),
(7, 8, 3, DATE_ADD(CURRENT_DATE, INTERVAL -7 DAY), 'Published'),
(8, 9, 2, DATE_ADD(CURRENT_DATE, INTERVAL -7 DAY), 'Published'),
(9, 11, 1, DATE_ADD(CURRENT_DATE, INTERVAL -7 DAY), 'Published'),
(10, 12, 1, DATE_ADD(CURRENT_DATE, INTERVAL -7 DAY), 'Published'),
(11, 13, 1, DATE_ADD(CURRENT_DATE, INTERVAL -7 DAY), 'Published'),
(12, 16, 2, DATE_ADD(CURRENT_DATE, INTERVAL -7 DAY), 'Published'),
(13, 17, 1, DATE_ADD(CURRENT_DATE, INTERVAL -7 DAY), 'Published'),
(14, 18, 2, DATE_ADD(CURRENT_DATE, INTERVAL -7 DAY), 'Published'),
(15, 20, 1, DATE_ADD(CURRENT_DATE, INTERVAL -7 DAY), 'Published'),
(16, 21, 1, DATE_ADD(CURRENT_DATE, INTERVAL -7 DAY), 'Published'),
(17, 23, 2, DATE_ADD(CURRENT_DATE, INTERVAL -7 DAY), 'Published'),
(18, 24, 2, DATE_ADD(CURRENT_DATE, INTERVAL -7 DAY), 'Published'),
(19, 25, 1, DATE_ADD(CURRENT_DATE, INTERVAL -7 DAY), 'Published'),
(20, 26, 2, DATE_ADD(CURRENT_DATE, INTERVAL -7 DAY), 'Published'),
(21, 27, 1, DATE_ADD(CURRENT_DATE, INTERVAL -7 DAY), 'Published'),
(22, 28, 1, DATE_ADD(CURRENT_DATE, INTERVAL -7 DAY), 'Published'),
(23, 29, 2, DATE_ADD(CURRENT_DATE, INTERVAL -7 DAY), 'Published'),
(24, 30, 2, DATE_ADD(CURRENT_DATE, INTERVAL -7 DAY), 'Published'),
(25, 31, 2, DATE_ADD(CURRENT_DATE, INTERVAL -7 DAY), 'Published'),
(26, 32, 2, DATE_ADD(CURRENT_DATE, INTERVAL -7 DAY), 'Published'),
(27, 33, 2, DATE_ADD(CURRENT_DATE, INTERVAL -7 DAY), 'Published'),
(28, 35, 2, DATE_ADD(CURRENT_DATE, INTERVAL -7 DAY), 'Published'),
(29, 36, 2, DATE_ADD(CURRENT_DATE, INTERVAL -7 DAY), 'Published'),
(30, 38, 2, DATE_ADD(CURRENT_DATE, INTERVAL -7 DAY), 'Published'),
(31, 39, 2, DATE_ADD(CURRENT_DATE, INTERVAL -7 DAY), 'Published'),
(32, 40, 1, DATE_ADD(CURRENT_DATE, INTERVAL -7 DAY), 'Published'),
(33, 41, 1, DATE_ADD(CURRENT_DATE, INTERVAL -7 DAY), 'Published'),
(34, 42, 2, DATE_ADD(CURRENT_DATE, INTERVAL -7 DAY), 'Published'),
(35, 1, 1, DATE_ADD(CURRENT_DATE, INTERVAL -6 DAY), 'Published'),
(36, 2, 1, DATE_ADD(CURRENT_DATE, INTERVAL -6 DAY), 'Published'),
(37, 3, 1, DATE_ADD(CURRENT_DATE, INTERVAL -6 DAY), 'Published'),
(38, 4, 2, DATE_ADD(CURRENT_DATE, INTERVAL -6 DAY), 'Published'),
(39, 5, 1, DATE_ADD(CURRENT_DATE, INTERVAL -6 DAY), 'Published'),
(40, 6, 1, DATE_ADD(CURRENT_DATE, INTERVAL -6 DAY), 'Published'),
(41, 7, 2, DATE_ADD(CURRENT_DATE, INTERVAL -6 DAY), 'Published'),
(42, 8, 1, DATE_ADD(CURRENT_DATE, INTERVAL -6 DAY), 'Published'),
(43, 10, 2, DATE_ADD(CURRENT_DATE, INTERVAL -6 DAY), 'Published'),
(44, 11, 1, DATE_ADD(CURRENT_DATE, INTERVAL -6 DAY), 'Published'),
(45, 12, 1, DATE_ADD(CURRENT_DATE, INTERVAL -6 DAY), 'Published'),
(46, 13, 1, DATE_ADD(CURRENT_DATE, INTERVAL -6 DAY), 'Published'),
(47, 14, 3, DATE_ADD(CURRENT_DATE, INTERVAL -6 DAY), 'Published'),
(48, 15, 1, DATE_ADD(CURRENT_DATE, INTERVAL -6 DAY), 'Published'),
(49, 16, 3, DATE_ADD(CURRENT_DATE, INTERVAL -6 DAY), 'Published'),
(50, 17, 2, DATE_ADD(CURRENT_DATE, INTERVAL -6 DAY), 'Published'),
(51, 18, 1, DATE_ADD(CURRENT_DATE, INTERVAL -6 DAY), 'Published'),
(52, 20, 1, DATE_ADD(CURRENT_DATE, INTERVAL -6 DAY), 'Published'),
(53, 21, 2, DATE_ADD(CURRENT_DATE, INTERVAL -6 DAY), 'Published'),
(54, 22, 2, DATE_ADD(CURRENT_DATE, INTERVAL -6 DAY), 'Published'),
(55, 23, 1, DATE_ADD(CURRENT_DATE, INTERVAL -6 DAY), 'Published'),
(56, 24, 2, DATE_ADD(CURRENT_DATE, INTERVAL -6 DAY), 'Published'),
(57, 26, 1, DATE_ADD(CURRENT_DATE, INTERVAL -6 DAY), 'Published'),
(58, 27, 1, DATE_ADD(CURRENT_DATE, INTERVAL -6 DAY), 'Published'),
(59, 29, 2, DATE_ADD(CURRENT_DATE, INTERVAL -6 DAY), 'Published'),
(60, 30, 1, DATE_ADD(CURRENT_DATE, INTERVAL -6 DAY), 'Published'),
(61, 32, 2, DATE_ADD(CURRENT_DATE, INTERVAL -6 DAY), 'Published'),
(62, 33, 2, DATE_ADD(CURRENT_DATE, INTERVAL -6 DAY), 'Published'),
(63, 35, 1, DATE_ADD(CURRENT_DATE, INTERVAL -6 DAY), 'Published'),
(64, 38, 1, DATE_ADD(CURRENT_DATE, INTERVAL -6 DAY), 'Published'),
(65, 39, 1, DATE_ADD(CURRENT_DATE, INTERVAL -6 DAY), 'Published'),
(66, 41, 2, DATE_ADD(CURRENT_DATE, INTERVAL -6 DAY), 'Published'),
(67, 42, 1, DATE_ADD(CURRENT_DATE, INTERVAL -6 DAY), 'Published'),
(68, 1, 1, DATE_ADD(CURRENT_DATE, INTERVAL -5 DAY), 'Published'),
(69, 2, 2, DATE_ADD(CURRENT_DATE, INTERVAL -5 DAY), 'Published'),
(70, 4, 2, DATE_ADD(CURRENT_DATE, INTERVAL -5 DAY), 'Published'),
(71, 5, 1, DATE_ADD(CURRENT_DATE, INTERVAL -5 DAY), 'Published'),
(72, 6, 2, DATE_ADD(CURRENT_DATE, INTERVAL -5 DAY), 'Published'),
(73, 7, 1, DATE_ADD(CURRENT_DATE, INTERVAL -5 DAY), 'Published'),
(74, 8, 1, DATE_ADD(CURRENT_DATE, INTERVAL -5 DAY), 'Published'),
(75, 9, 1, DATE_ADD(CURRENT_DATE, INTERVAL -5 DAY), 'Published'),
(76, 11, 2, DATE_ADD(CURRENT_DATE, INTERVAL -5 DAY), 'Published'),
(77, 12, 1, DATE_ADD(CURRENT_DATE, INTERVAL -5 DAY), 'Published'),
(78, 13, 2, DATE_ADD(CURRENT_DATE, INTERVAL -5 DAY), 'Published'),
(79, 15, 1, DATE_ADD(CURRENT_DATE, INTERVAL -5 DAY), 'Published'),
(80, 16, 1, DATE_ADD(CURRENT_DATE, INTERVAL -5 DAY), 'Published'),
(81, 17, 2, DATE_ADD(CURRENT_DATE, INTERVAL -5 DAY), 'Published'),
(82, 19, 1, DATE_ADD(CURRENT_DATE, INTERVAL -5 DAY), 'Published'),
(83, 22, 2, DATE_ADD(CURRENT_DATE, INTERVAL -5 DAY), 'Published'),
(84, 23, 2, DATE_ADD(CURRENT_DATE, INTERVAL -5 DAY), 'Published'),
(85, 24, 1, DATE_ADD(CURRENT_DATE, INTERVAL -5 DAY), 'Published'),
(86, 26, 2, DATE_ADD(CURRENT_DATE, INTERVAL -5 DAY), 'Published'),
(87, 28, 2, DATE_ADD(CURRENT_DATE, INTERVAL -5 DAY), 'Published'),
(88, 29, 2, DATE_ADD(CURRENT_DATE, INTERVAL -5 DAY), 'Published'),
(89, 30, 1, DATE_ADD(CURRENT_DATE, INTERVAL -5 DAY), 'Published'),
(90, 31, 1, DATE_ADD(CURRENT_DATE, INTERVAL -5 DAY), 'Published'),
(91, 32, 1, DATE_ADD(CURRENT_DATE, INTERVAL -5 DAY), 'Published'),
(92, 33, 1, DATE_ADD(CURRENT_DATE, INTERVAL -5 DAY), 'Published'),
(93, 35, 1, DATE_ADD(CURRENT_DATE, INTERVAL -5 DAY), 'Published'),
(94, 36, 2, DATE_ADD(CURRENT_DATE, INTERVAL -5 DAY), 'Published'),
(95, 37, 1, DATE_ADD(CURRENT_DATE, INTERVAL -5 DAY), 'Published'),
(96, 38, 3, DATE_ADD(CURRENT_DATE, INTERVAL -5 DAY), 'Published'),
(97, 39, 3, DATE_ADD(CURRENT_DATE, INTERVAL -5 DAY), 'Published'),
(98, 40, 1, DATE_ADD(CURRENT_DATE, INTERVAL -5 DAY), 'Published'),
(99, 2, 1, DATE_ADD(CURRENT_DATE, INTERVAL -4 DAY), 'Published'),
(100, 3, 1, DATE_ADD(CURRENT_DATE, INTERVAL -4 DAY), 'Published');

INSERT INTO Staff_Schedules (schedule_id, employee_id, shift_id, work_date, status) VALUES
(101, 4, 3, DATE_ADD(CURRENT_DATE, INTERVAL -4 DAY), 'Published'),
(102, 5, 2, DATE_ADD(CURRENT_DATE, INTERVAL -4 DAY), 'Published'),
(103, 6, 1, DATE_ADD(CURRENT_DATE, INTERVAL -4 DAY), 'Published'),
(104, 7, 1, DATE_ADD(CURRENT_DATE, INTERVAL -4 DAY), 'Published'),
(105, 8, 2, DATE_ADD(CURRENT_DATE, INTERVAL -4 DAY), 'Published'),
(106, 10, 1, DATE_ADD(CURRENT_DATE, INTERVAL -4 DAY), 'Published'),
(107, 11, 2, DATE_ADD(CURRENT_DATE, INTERVAL -4 DAY), 'Published'),
(108, 13, 3, DATE_ADD(CURRENT_DATE, INTERVAL -4 DAY), 'Published'),
(109, 14, 2, DATE_ADD(CURRENT_DATE, INTERVAL -4 DAY), 'Published'),
(110, 15, 2, DATE_ADD(CURRENT_DATE, INTERVAL -4 DAY), 'Published'),
(111, 17, 2, DATE_ADD(CURRENT_DATE, INTERVAL -4 DAY), 'Published'),
(112, 18, 2, DATE_ADD(CURRENT_DATE, INTERVAL -4 DAY), 'Published'),
(113, 20, 2, DATE_ADD(CURRENT_DATE, INTERVAL -4 DAY), 'Published'),
(114, 22, 2, DATE_ADD(CURRENT_DATE, INTERVAL -4 DAY), 'Published'),
(115, 23, 2, DATE_ADD(CURRENT_DATE, INTERVAL -4 DAY), 'Published'),
(116, 24, 2, DATE_ADD(CURRENT_DATE, INTERVAL -4 DAY), 'Published'),
(117, 26, 2, DATE_ADD(CURRENT_DATE, INTERVAL -4 DAY), 'Published'),
(118, 27, 2, DATE_ADD(CURRENT_DATE, INTERVAL -4 DAY), 'Published'),
(119, 28, 2, DATE_ADD(CURRENT_DATE, INTERVAL -4 DAY), 'Published'),
(120, 29, 1, DATE_ADD(CURRENT_DATE, INTERVAL -4 DAY), 'Published'),
(121, 31, 1, DATE_ADD(CURRENT_DATE, INTERVAL -4 DAY), 'Published'),
(122, 33, 2, DATE_ADD(CURRENT_DATE, INTERVAL -4 DAY), 'Published'),
(123, 34, 2, DATE_ADD(CURRENT_DATE, INTERVAL -4 DAY), 'Published'),
(124, 35, 2, DATE_ADD(CURRENT_DATE, INTERVAL -4 DAY), 'Published'),
(125, 36, 2, DATE_ADD(CURRENT_DATE, INTERVAL -4 DAY), 'Published'),
(126, 37, 1, DATE_ADD(CURRENT_DATE, INTERVAL -4 DAY), 'Published'),
(127, 38, 1, DATE_ADD(CURRENT_DATE, INTERVAL -4 DAY), 'Published'),
(128, 40, 1, DATE_ADD(CURRENT_DATE, INTERVAL -4 DAY), 'Published'),
(129, 41, 1, DATE_ADD(CURRENT_DATE, INTERVAL -4 DAY), 'Published'),
(130, 42, 1, DATE_ADD(CURRENT_DATE, INTERVAL -4 DAY), 'Published'),
(131, 1, 1, DATE_ADD(CURRENT_DATE, INTERVAL -3 DAY), 'Published'),
(132, 3, 1, DATE_ADD(CURRENT_DATE, INTERVAL -3 DAY), 'Published'),
(133, 4, 2, DATE_ADD(CURRENT_DATE, INTERVAL -3 DAY), 'Published'),
(134, 5, 1, DATE_ADD(CURRENT_DATE, INTERVAL -3 DAY), 'Published'),
(135, 7, 2, DATE_ADD(CURRENT_DATE, INTERVAL -3 DAY), 'Published'),
(136, 8, 2, DATE_ADD(CURRENT_DATE, INTERVAL -3 DAY), 'Published'),
(137, 9, 1, DATE_ADD(CURRENT_DATE, INTERVAL -3 DAY), 'Published'),
(138, 10, 3, DATE_ADD(CURRENT_DATE, INTERVAL -3 DAY), 'Published'),
(139, 11, 1, DATE_ADD(CURRENT_DATE, INTERVAL -3 DAY), 'Published'),
(140, 12, 1, DATE_ADD(CURRENT_DATE, INTERVAL -3 DAY), 'Published'),
(141, 13, 1, DATE_ADD(CURRENT_DATE, INTERVAL -3 DAY), 'Published'),
(142, 14, 1, DATE_ADD(CURRENT_DATE, INTERVAL -3 DAY), 'Published'),
(143, 15, 1, DATE_ADD(CURRENT_DATE, INTERVAL -3 DAY), 'Published'),
(144, 16, 3, DATE_ADD(CURRENT_DATE, INTERVAL -3 DAY), 'Published'),
(145, 17, 3, DATE_ADD(CURRENT_DATE, INTERVAL -3 DAY), 'Published'),
(146, 22, 1, DATE_ADD(CURRENT_DATE, INTERVAL -3 DAY), 'Published'),
(147, 23, 2, DATE_ADD(CURRENT_DATE, INTERVAL -3 DAY), 'Published'),
(148, 24, 1, DATE_ADD(CURRENT_DATE, INTERVAL -3 DAY), 'Published'),
(149, 25, 2, DATE_ADD(CURRENT_DATE, INTERVAL -3 DAY), 'Published'),
(150, 26, 1, DATE_ADD(CURRENT_DATE, INTERVAL -3 DAY), 'Published'),
(151, 27, 2, DATE_ADD(CURRENT_DATE, INTERVAL -3 DAY), 'Published'),
(152, 28, 2, DATE_ADD(CURRENT_DATE, INTERVAL -3 DAY), 'Published'),
(153, 29, 1, DATE_ADD(CURRENT_DATE, INTERVAL -3 DAY), 'Published'),
(154, 30, 2, DATE_ADD(CURRENT_DATE, INTERVAL -3 DAY), 'Published'),
(155, 32, 3, DATE_ADD(CURRENT_DATE, INTERVAL -3 DAY), 'Published'),
(156, 33, 1, DATE_ADD(CURRENT_DATE, INTERVAL -3 DAY), 'Published'),
(157, 34, 2, DATE_ADD(CURRENT_DATE, INTERVAL -3 DAY), 'Published'),
(158, 35, 1, DATE_ADD(CURRENT_DATE, INTERVAL -3 DAY), 'Published'),
(159, 36, 1, DATE_ADD(CURRENT_DATE, INTERVAL -3 DAY), 'Published'),
(160, 37, 1, DATE_ADD(CURRENT_DATE, INTERVAL -3 DAY), 'Published'),
(161, 38, 2, DATE_ADD(CURRENT_DATE, INTERVAL -3 DAY), 'Published'),
(162, 39, 1, DATE_ADD(CURRENT_DATE, INTERVAL -3 DAY), 'Published'),
(163, 40, 1, DATE_ADD(CURRENT_DATE, INTERVAL -3 DAY), 'Published'),
(164, 1, 1, DATE_ADD(CURRENT_DATE, INTERVAL -2 DAY), 'Published'),
(165, 2, 2, DATE_ADD(CURRENT_DATE, INTERVAL -2 DAY), 'Published'),
(166, 3, 2, DATE_ADD(CURRENT_DATE, INTERVAL -2 DAY), 'Published'),
(167, 4, 2, DATE_ADD(CURRENT_DATE, INTERVAL -2 DAY), 'Published'),
(168, 5, 1, DATE_ADD(CURRENT_DATE, INTERVAL -2 DAY), 'Published'),
(169, 6, 2, DATE_ADD(CURRENT_DATE, INTERVAL -2 DAY), 'Published'),
(170, 7, 2, DATE_ADD(CURRENT_DATE, INTERVAL -2 DAY), 'Published'),
(171, 8, 2, DATE_ADD(CURRENT_DATE, INTERVAL -2 DAY), 'Published'),
(172, 9, 2, DATE_ADD(CURRENT_DATE, INTERVAL -2 DAY), 'Published'),
(173, 10, 2, DATE_ADD(CURRENT_DATE, INTERVAL -2 DAY), 'Published'),
(174, 11, 1, DATE_ADD(CURRENT_DATE, INTERVAL -2 DAY), 'Published'),
(175, 12, 1, DATE_ADD(CURRENT_DATE, INTERVAL -2 DAY), 'Published'),
(176, 13, 2, DATE_ADD(CURRENT_DATE, INTERVAL -2 DAY), 'Published'),
(177, 14, 2, DATE_ADD(CURRENT_DATE, INTERVAL -2 DAY), 'Published'),
(178, 15, 2, DATE_ADD(CURRENT_DATE, INTERVAL -2 DAY), 'Published'),
(179, 16, 1, DATE_ADD(CURRENT_DATE, INTERVAL -2 DAY), 'Published'),
(180, 17, 2, DATE_ADD(CURRENT_DATE, INTERVAL -2 DAY), 'Published'),
(181, 18, 1, DATE_ADD(CURRENT_DATE, INTERVAL -2 DAY), 'Published'),
(182, 19, 1, DATE_ADD(CURRENT_DATE, INTERVAL -2 DAY), 'Published'),
(183, 20, 2, DATE_ADD(CURRENT_DATE, INTERVAL -2 DAY), 'Published'),
(184, 21, 1, DATE_ADD(CURRENT_DATE, INTERVAL -2 DAY), 'Published'),
(185, 22, 1, DATE_ADD(CURRENT_DATE, INTERVAL -2 DAY), 'Published'),
(186, 23, 1, DATE_ADD(CURRENT_DATE, INTERVAL -2 DAY), 'Published'),
(187, 24, 2, DATE_ADD(CURRENT_DATE, INTERVAL -2 DAY), 'Published'),
(188, 25, 2, DATE_ADD(CURRENT_DATE, INTERVAL -2 DAY), 'Published'),
(189, 26, 1, DATE_ADD(CURRENT_DATE, INTERVAL -2 DAY), 'Published'),
(190, 27, 2, DATE_ADD(CURRENT_DATE, INTERVAL -2 DAY), 'Published'),
(191, 28, 1, DATE_ADD(CURRENT_DATE, INTERVAL -2 DAY), 'Published'),
(192, 29, 2, DATE_ADD(CURRENT_DATE, INTERVAL -2 DAY), 'Published'),
(193, 30, 2, DATE_ADD(CURRENT_DATE, INTERVAL -2 DAY), 'Published'),
(194, 31, 3, DATE_ADD(CURRENT_DATE, INTERVAL -2 DAY), 'Published'),
(195, 32, 1, DATE_ADD(CURRENT_DATE, INTERVAL -2 DAY), 'Published'),
(196, 33, 2, DATE_ADD(CURRENT_DATE, INTERVAL -2 DAY), 'Published'),
(197, 34, 1, DATE_ADD(CURRENT_DATE, INTERVAL -2 DAY), 'Published'),
(198, 35, 2, DATE_ADD(CURRENT_DATE, INTERVAL -2 DAY), 'Published'),
(199, 36, 1, DATE_ADD(CURRENT_DATE, INTERVAL -2 DAY), 'Published'),
(200, 37, 2, DATE_ADD(CURRENT_DATE, INTERVAL -2 DAY), 'Published');

INSERT INTO Staff_Schedules (schedule_id, employee_id, shift_id, work_date, status) VALUES
(201, 38, 1, DATE_ADD(CURRENT_DATE, INTERVAL -2 DAY), 'Published'),
(202, 39, 3, DATE_ADD(CURRENT_DATE, INTERVAL -2 DAY), 'Published'),
(203, 41, 2, DATE_ADD(CURRENT_DATE, INTERVAL -2 DAY), 'Published'),
(204, 1, 1, DATE_ADD(CURRENT_DATE, INTERVAL -1 DAY), 'Published'),
(205, 2, 1, DATE_ADD(CURRENT_DATE, INTERVAL -1 DAY), 'Published'),
(206, 3, 2, DATE_ADD(CURRENT_DATE, INTERVAL -1 DAY), 'Published'),
(207, 4, 2, DATE_ADD(CURRENT_DATE, INTERVAL -1 DAY), 'Published'),
(208, 5, 1, DATE_ADD(CURRENT_DATE, INTERVAL -1 DAY), 'Published'),
(209, 6, 2, DATE_ADD(CURRENT_DATE, INTERVAL -1 DAY), 'Published'),
(210, 7, 1, DATE_ADD(CURRENT_DATE, INTERVAL -1 DAY), 'Published'),
(211, 8, 1, DATE_ADD(CURRENT_DATE, INTERVAL -1 DAY), 'Published'),
(212, 9, 1, DATE_ADD(CURRENT_DATE, INTERVAL -1 DAY), 'Published'),
(213, 10, 3, DATE_ADD(CURRENT_DATE, INTERVAL -1 DAY), 'Published'),
(214, 11, 2, DATE_ADD(CURRENT_DATE, INTERVAL -1 DAY), 'Published'),
(215, 12, 1, DATE_ADD(CURRENT_DATE, INTERVAL -1 DAY), 'Published'),
(216, 13, 3, DATE_ADD(CURRENT_DATE, INTERVAL -1 DAY), 'Published'),
(217, 14, 1, DATE_ADD(CURRENT_DATE, INTERVAL -1 DAY), 'Published'),
(218, 16, 2, DATE_ADD(CURRENT_DATE, INTERVAL -1 DAY), 'Published'),
(219, 17, 3, DATE_ADD(CURRENT_DATE, INTERVAL -1 DAY), 'Published'),
(220, 18, 2, DATE_ADD(CURRENT_DATE, INTERVAL -1 DAY), 'Published'),
(221, 19, 1, DATE_ADD(CURRENT_DATE, INTERVAL -1 DAY), 'Published'),
(222, 20, 2, DATE_ADD(CURRENT_DATE, INTERVAL -1 DAY), 'Published'),
(223, 21, 1, DATE_ADD(CURRENT_DATE, INTERVAL -1 DAY), 'Published'),
(224, 22, 2, DATE_ADD(CURRENT_DATE, INTERVAL -1 DAY), 'Published'),
(225, 23, 1, DATE_ADD(CURRENT_DATE, INTERVAL -1 DAY), 'Published'),
(226, 26, 1, DATE_ADD(CURRENT_DATE, INTERVAL -1 DAY), 'Published'),
(227, 27, 2, DATE_ADD(CURRENT_DATE, INTERVAL -1 DAY), 'Published'),
(228, 29, 1, DATE_ADD(CURRENT_DATE, INTERVAL -1 DAY), 'Published'),
(229, 30, 2, DATE_ADD(CURRENT_DATE, INTERVAL -1 DAY), 'Published'),
(230, 31, 2, DATE_ADD(CURRENT_DATE, INTERVAL -1 DAY), 'Published'),
(231, 32, 3, DATE_ADD(CURRENT_DATE, INTERVAL -1 DAY), 'Published'),
(232, 33, 1, DATE_ADD(CURRENT_DATE, INTERVAL -1 DAY), 'Published'),
(233, 34, 1, DATE_ADD(CURRENT_DATE, INTERVAL -1 DAY), 'Published'),
(234, 36, 1, DATE_ADD(CURRENT_DATE, INTERVAL -1 DAY), 'Published'),
(235, 37, 2, DATE_ADD(CURRENT_DATE, INTERVAL -1 DAY), 'Published'),
(236, 39, 2, DATE_ADD(CURRENT_DATE, INTERVAL -1 DAY), 'Published'),
(237, 40, 1, DATE_ADD(CURRENT_DATE, INTERVAL -1 DAY), 'Published'),
(238, 42, 2, DATE_ADD(CURRENT_DATE, INTERVAL -1 DAY), 'Published'),
(239, 3, 2, CURRENT_DATE, 'Published'),
(240, 4, 1, CURRENT_DATE, 'Published'),
(241, 5, 1, CURRENT_DATE, 'Published'),
(242, 7, 2, CURRENT_DATE, 'Published'),
(243, 8, 1, CURRENT_DATE, 'Published'),
(244, 9, 1, CURRENT_DATE, 'Published'),
(245, 10, 3, CURRENT_DATE, 'Published'),
(246, 11, 1, CURRENT_DATE, 'Published'),
(247, 12, 1, CURRENT_DATE, 'Published'),
(248, 13, 2, CURRENT_DATE, 'Published'),
(249, 14, 2, CURRENT_DATE, 'Published'),
(250, 15, 1, CURRENT_DATE, 'Published'),
(251, 16, 1, CURRENT_DATE, 'Published'),
(252, 19, 2, CURRENT_DATE, 'Published'),
(253, 20, 2, CURRENT_DATE, 'Published'),
(254, 21, 2, CURRENT_DATE, 'Published'),
(255, 24, 2, CURRENT_DATE, 'Published'),
(256, 26, 2, CURRENT_DATE, 'Published'),
(257, 27, 1, CURRENT_DATE, 'Published'),
(258, 28, 1, CURRENT_DATE, 'Published'),
(259, 29, 2, CURRENT_DATE, 'Published'),
(260, 30, 2, CURRENT_DATE, 'Published'),
(261, 32, 2, CURRENT_DATE, 'Published'),
(262, 33, 1, CURRENT_DATE, 'Published'),
(263, 34, 2, CURRENT_DATE, 'Published'),
(264, 35, 2, CURRENT_DATE, 'Published'),
(265, 36, 1, CURRENT_DATE, 'Published'),
(266, 37, 2, CURRENT_DATE, 'Published'),
(267, 38, 1, CURRENT_DATE, 'Published'),
(268, 39, 1, CURRENT_DATE, 'Published'),
(269, 40, 1, CURRENT_DATE, 'Published'),
(270, 41, 1, CURRENT_DATE, 'Published'),
(271, 42, 1, CURRENT_DATE, 'Published'),
(272, 1, 1, DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), 'Published'),
(273, 5, 1, DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), 'Published'),
(274, 6, 2, DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), 'Published'),
(275, 7, 1, DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), 'Published'),
(276, 8, 2, DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), 'Published'),
(277, 9, 2, DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), 'Published'),
(278, 10, 3, DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), 'Published'),
(279, 11, 2, DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), 'Published'),
(280, 12, 1, DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), 'Published'),
(281, 15, 1, DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), 'Published'),
(282, 17, 1, DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), 'Published'),
(283, 18, 2, DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), 'Published'),
(284, 19, 1, DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), 'Published'),
(285, 20, 1, DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), 'Published'),
(286, 21, 1, DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), 'Published'),
(287, 22, 1, DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), 'Published'),
(288, 23, 2, DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), 'Published'),
(289, 24, 2, DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), 'Published'),
(290, 25, 1, DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), 'Published'),
(291, 26, 1, DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), 'Published'),
(292, 27, 2, DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), 'Published'),
(293, 28, 3, DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), 'Published'),
(294, 29, 2, DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), 'Published'),
(295, 30, 3, DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), 'Published'),
(296, 31, 3, DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), 'Published'),
(297, 32, 1, DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), 'Published'),
(298, 33, 3, DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), 'Published'),
(299, 34, 3, DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), 'Published'),
(300, 35, 2, DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), 'Published');

INSERT INTO Staff_Schedules (schedule_id, employee_id, shift_id, work_date, status) VALUES
(301, 36, 1, DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), 'Published'),
(302, 38, 2, DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), 'Published'),
(303, 39, 2, DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), 'Published'),
(304, 40, 1, DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), 'Published'),
(305, 41, 2, DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), 'Published'),
(306, 42, 1, DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), 'Published'),
(307, 2, 1, DATE_ADD(CURRENT_DATE, INTERVAL 2 DAY), 'Published'),
(308, 3, 1, DATE_ADD(CURRENT_DATE, INTERVAL 2 DAY), 'Published'),
(309, 5, 1, DATE_ADD(CURRENT_DATE, INTERVAL 2 DAY), 'Published'),
(310, 6, 1, DATE_ADD(CURRENT_DATE, INTERVAL 2 DAY), 'Published'),
(311, 7, 1, DATE_ADD(CURRENT_DATE, INTERVAL 2 DAY), 'Published'),
(312, 8, 1, DATE_ADD(CURRENT_DATE, INTERVAL 2 DAY), 'Published'),
(313, 9, 1, DATE_ADD(CURRENT_DATE, INTERVAL 2 DAY), 'Published'),
(314, 10, 2, DATE_ADD(CURRENT_DATE, INTERVAL 2 DAY), 'Published'),
(315, 11, 1, DATE_ADD(CURRENT_DATE, INTERVAL 2 DAY), 'Published'),
(316, 13, 1, DATE_ADD(CURRENT_DATE, INTERVAL 2 DAY), 'Published'),
(317, 14, 2, DATE_ADD(CURRENT_DATE, INTERVAL 2 DAY), 'Published'),
(318, 15, 2, DATE_ADD(CURRENT_DATE, INTERVAL 2 DAY), 'Published'),
(319, 16, 1, DATE_ADD(CURRENT_DATE, INTERVAL 2 DAY), 'Published'),
(320, 18, 2, DATE_ADD(CURRENT_DATE, INTERVAL 2 DAY), 'Published'),
(321, 19, 2, DATE_ADD(CURRENT_DATE, INTERVAL 2 DAY), 'Published'),
(322, 20, 2, DATE_ADD(CURRENT_DATE, INTERVAL 2 DAY), 'Published'),
(323, 21, 2, DATE_ADD(CURRENT_DATE, INTERVAL 2 DAY), 'Published'),
(324, 22, 1, DATE_ADD(CURRENT_DATE, INTERVAL 2 DAY), 'Published'),
(325, 23, 2, DATE_ADD(CURRENT_DATE, INTERVAL 2 DAY), 'Published'),
(326, 24, 2, DATE_ADD(CURRENT_DATE, INTERVAL 2 DAY), 'Published'),
(327, 26, 1, DATE_ADD(CURRENT_DATE, INTERVAL 2 DAY), 'Published'),
(328, 27, 1, DATE_ADD(CURRENT_DATE, INTERVAL 2 DAY), 'Published'),
(329, 28, 1, DATE_ADD(CURRENT_DATE, INTERVAL 2 DAY), 'Published'),
(330, 29, 1, DATE_ADD(CURRENT_DATE, INTERVAL 2 DAY), 'Published'),
(331, 30, 2, DATE_ADD(CURRENT_DATE, INTERVAL 2 DAY), 'Published'),
(332, 31, 1, DATE_ADD(CURRENT_DATE, INTERVAL 2 DAY), 'Published'),
(333, 32, 1, DATE_ADD(CURRENT_DATE, INTERVAL 2 DAY), 'Published'),
(334, 33, 1, DATE_ADD(CURRENT_DATE, INTERVAL 2 DAY), 'Published'),
(335, 35, 2, DATE_ADD(CURRENT_DATE, INTERVAL 2 DAY), 'Published'),
(336, 37, 1, DATE_ADD(CURRENT_DATE, INTERVAL 2 DAY), 'Published'),
(337, 38, 1, DATE_ADD(CURRENT_DATE, INTERVAL 2 DAY), 'Published'),
(338, 39, 2, DATE_ADD(CURRENT_DATE, INTERVAL 2 DAY), 'Published'),
(339, 40, 1, DATE_ADD(CURRENT_DATE, INTERVAL 2 DAY), 'Published'),
(340, 41, 1, DATE_ADD(CURRENT_DATE, INTERVAL 2 DAY), 'Published'),
(341, 3, 1, DATE_ADD(CURRENT_DATE, INTERVAL 3 DAY), 'Published'),
(342, 5, 1, DATE_ADD(CURRENT_DATE, INTERVAL 3 DAY), 'Published'),
(343, 6, 2, DATE_ADD(CURRENT_DATE, INTERVAL 3 DAY), 'Published'),
(344, 7, 1, DATE_ADD(CURRENT_DATE, INTERVAL 3 DAY), 'Published'),
(345, 8, 1, DATE_ADD(CURRENT_DATE, INTERVAL 3 DAY), 'Published'),
(346, 9, 1, DATE_ADD(CURRENT_DATE, INTERVAL 3 DAY), 'Published'),
(347, 10, 2, DATE_ADD(CURRENT_DATE, INTERVAL 3 DAY), 'Published'),
(348, 13, 1, DATE_ADD(CURRENT_DATE, INTERVAL 3 DAY), 'Published'),
(349, 14, 2, DATE_ADD(CURRENT_DATE, INTERVAL 3 DAY), 'Published'),
(350, 15, 1, DATE_ADD(CURRENT_DATE, INTERVAL 3 DAY), 'Published'),
(351, 16, 1, DATE_ADD(CURRENT_DATE, INTERVAL 3 DAY), 'Published'),
(352, 17, 3, DATE_ADD(CURRENT_DATE, INTERVAL 3 DAY), 'Published'),
(353, 18, 2, DATE_ADD(CURRENT_DATE, INTERVAL 3 DAY), 'Published'),
(354, 19, 1, DATE_ADD(CURRENT_DATE, INTERVAL 3 DAY), 'Published'),
(355, 20, 2, DATE_ADD(CURRENT_DATE, INTERVAL 3 DAY), 'Published'),
(356, 21, 2, DATE_ADD(CURRENT_DATE, INTERVAL 3 DAY), 'Published'),
(357, 23, 2, DATE_ADD(CURRENT_DATE, INTERVAL 3 DAY), 'Published'),
(358, 25, 2, DATE_ADD(CURRENT_DATE, INTERVAL 3 DAY), 'Published'),
(359, 27, 2, DATE_ADD(CURRENT_DATE, INTERVAL 3 DAY), 'Published'),
(360, 28, 1, DATE_ADD(CURRENT_DATE, INTERVAL 3 DAY), 'Published'),
(361, 29, 2, DATE_ADD(CURRENT_DATE, INTERVAL 3 DAY), 'Published'),
(362, 30, 2, DATE_ADD(CURRENT_DATE, INTERVAL 3 DAY), 'Published'),
(363, 31, 2, DATE_ADD(CURRENT_DATE, INTERVAL 3 DAY), 'Published'),
(364, 32, 1, DATE_ADD(CURRENT_DATE, INTERVAL 3 DAY), 'Published'),
(365, 33, 1, DATE_ADD(CURRENT_DATE, INTERVAL 3 DAY), 'Published'),
(366, 34, 1, DATE_ADD(CURRENT_DATE, INTERVAL 3 DAY), 'Published'),
(367, 36, 3, DATE_ADD(CURRENT_DATE, INTERVAL 3 DAY), 'Published'),
(368, 37, 2, DATE_ADD(CURRENT_DATE, INTERVAL 3 DAY), 'Published'),
(369, 38, 2, DATE_ADD(CURRENT_DATE, INTERVAL 3 DAY), 'Published'),
(370, 39, 2, DATE_ADD(CURRENT_DATE, INTERVAL 3 DAY), 'Published'),
(371, 41, 1, DATE_ADD(CURRENT_DATE, INTERVAL 3 DAY), 'Published'),
(372, 42, 2, DATE_ADD(CURRENT_DATE, INTERVAL 3 DAY), 'Published'),
(373, 1, 1, DATE_ADD(CURRENT_DATE, INTERVAL 4 DAY), 'Published'),
(374, 2, 1, DATE_ADD(CURRENT_DATE, INTERVAL 4 DAY), 'Published'),
(375, 3, 1, DATE_ADD(CURRENT_DATE, INTERVAL 4 DAY), 'Published'),
(376, 6, 2, DATE_ADD(CURRENT_DATE, INTERVAL 4 DAY), 'Published'),
(377, 8, 1, DATE_ADD(CURRENT_DATE, INTERVAL 4 DAY), 'Published'),
(378, 9, 2, DATE_ADD(CURRENT_DATE, INTERVAL 4 DAY), 'Published'),
(379, 10, 1, DATE_ADD(CURRENT_DATE, INTERVAL 4 DAY), 'Published'),
(380, 11, 2, DATE_ADD(CURRENT_DATE, INTERVAL 4 DAY), 'Published'),
(381, 12, 1, DATE_ADD(CURRENT_DATE, INTERVAL 4 DAY), 'Published'),
(382, 15, 3, DATE_ADD(CURRENT_DATE, INTERVAL 4 DAY), 'Published'),
(383, 16, 3, DATE_ADD(CURRENT_DATE, INTERVAL 4 DAY), 'Published'),
(384, 17, 2, DATE_ADD(CURRENT_DATE, INTERVAL 4 DAY), 'Published'),
(385, 18, 1, DATE_ADD(CURRENT_DATE, INTERVAL 4 DAY), 'Published'),
(386, 19, 1, DATE_ADD(CURRENT_DATE, INTERVAL 4 DAY), 'Published'),
(387, 23, 2, DATE_ADD(CURRENT_DATE, INTERVAL 4 DAY), 'Published'),
(388, 27, 1, DATE_ADD(CURRENT_DATE, INTERVAL 4 DAY), 'Published'),
(389, 28, 1, DATE_ADD(CURRENT_DATE, INTERVAL 4 DAY), 'Published'),
(390, 29, 1, DATE_ADD(CURRENT_DATE, INTERVAL 4 DAY), 'Published'),
(391, 30, 2, DATE_ADD(CURRENT_DATE, INTERVAL 4 DAY), 'Published'),
(392, 31, 1, DATE_ADD(CURRENT_DATE, INTERVAL 4 DAY), 'Published'),
(393, 32, 1, DATE_ADD(CURRENT_DATE, INTERVAL 4 DAY), 'Published'),
(394, 34, 2, DATE_ADD(CURRENT_DATE, INTERVAL 4 DAY), 'Published'),
(395, 35, 1, DATE_ADD(CURRENT_DATE, INTERVAL 4 DAY), 'Published'),
(396, 36, 1, DATE_ADD(CURRENT_DATE, INTERVAL 4 DAY), 'Published'),
(397, 37, 2, DATE_ADD(CURRENT_DATE, INTERVAL 4 DAY), 'Published'),
(398, 38, 2, DATE_ADD(CURRENT_DATE, INTERVAL 4 DAY), 'Published'),
(399, 40, 1, DATE_ADD(CURRENT_DATE, INTERVAL 4 DAY), 'Published'),
(400, 42, 2, DATE_ADD(CURRENT_DATE, INTERVAL 4 DAY), 'Published');

INSERT INTO Staff_Schedules (schedule_id, employee_id, shift_id, work_date, status) VALUES
(401, 2, 2, DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), 'Published'),
(402, 3, 2, DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), 'Published'),
(403, 5, 1, DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), 'Published'),
(404, 7, 1, DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), 'Published'),
(405, 8, 2, DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), 'Published'),
(406, 9, 1, DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), 'Published'),
(407, 11, 1, DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), 'Published'),
(408, 12, 1, DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), 'Published'),
(409, 13, 1, DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), 'Published'),
(410, 14, 2, DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), 'Published'),
(411, 15, 1, DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), 'Published'),
(412, 18, 2, DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), 'Published'),
(413, 19, 2, DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), 'Published'),
(414, 20, 2, DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), 'Published'),
(415, 21, 2, DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), 'Published'),
(416, 22, 2, DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), 'Published'),
(417, 23, 2, DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), 'Published'),
(418, 24, 1, DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), 'Published'),
(419, 25, 2, DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), 'Published'),
(420, 26, 1, DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), 'Published'),
(421, 27, 1, DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), 'Published'),
(422, 28, 3, DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), 'Published'),
(423, 29, 1, DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), 'Published'),
(424, 30, 3, DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), 'Published'),
(425, 32, 1, DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), 'Published'),
(426, 33, 1, DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), 'Published'),
(427, 35, 2, DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), 'Published'),
(428, 36, 2, DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), 'Published'),
(429, 37, 1, DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), 'Published'),
(430, 38, 2, DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), 'Published'),
(431, 39, 2, DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), 'Published'),
(432, 41, 2, DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), 'Published'),
(433, 42, 2, DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), 'Published'),
(434, 1, 1, DATE_ADD(CURRENT_DATE, INTERVAL 6 DAY), 'Published'),
(435, 2, 2, DATE_ADD(CURRENT_DATE, INTERVAL 6 DAY), 'Published'),
(436, 4, 1, DATE_ADD(CURRENT_DATE, INTERVAL 6 DAY), 'Published'),
(437, 5, 2, DATE_ADD(CURRENT_DATE, INTERVAL 6 DAY), 'Published'),
(438, 6, 1, DATE_ADD(CURRENT_DATE, INTERVAL 6 DAY), 'Published'),
(439, 7, 2, DATE_ADD(CURRENT_DATE, INTERVAL 6 DAY), 'Published'),
(440, 8, 1, DATE_ADD(CURRENT_DATE, INTERVAL 6 DAY), 'Published'),
(441, 9, 1, DATE_ADD(CURRENT_DATE, INTERVAL 6 DAY), 'Published'),
(442, 10, 2, DATE_ADD(CURRENT_DATE, INTERVAL 6 DAY), 'Published'),
(443, 11, 1, DATE_ADD(CURRENT_DATE, INTERVAL 6 DAY), 'Published'),
(444, 12, 1, DATE_ADD(CURRENT_DATE, INTERVAL 6 DAY), 'Published'),
(445, 13, 1, DATE_ADD(CURRENT_DATE, INTERVAL 6 DAY), 'Published'),
(446, 14, 2, DATE_ADD(CURRENT_DATE, INTERVAL 6 DAY), 'Published'),
(447, 15, 1, DATE_ADD(CURRENT_DATE, INTERVAL 6 DAY), 'Published'),
(448, 16, 1, DATE_ADD(CURRENT_DATE, INTERVAL 6 DAY), 'Published'),
(449, 17, 1, DATE_ADD(CURRENT_DATE, INTERVAL 6 DAY), 'Published'),
(450, 19, 2, DATE_ADD(CURRENT_DATE, INTERVAL 6 DAY), 'Published'),
(451, 20, 2, DATE_ADD(CURRENT_DATE, INTERVAL 6 DAY), 'Published'),
(452, 21, 1, DATE_ADD(CURRENT_DATE, INTERVAL 6 DAY), 'Published'),
(453, 22, 1, DATE_ADD(CURRENT_DATE, INTERVAL 6 DAY), 'Published'),
(454, 24, 1, DATE_ADD(CURRENT_DATE, INTERVAL 6 DAY), 'Published'),
(455, 25, 1, DATE_ADD(CURRENT_DATE, INTERVAL 6 DAY), 'Published'),
(456, 28, 2, DATE_ADD(CURRENT_DATE, INTERVAL 6 DAY), 'Published'),
(457, 29, 1, DATE_ADD(CURRENT_DATE, INTERVAL 6 DAY), 'Published'),
(458, 30, 2, DATE_ADD(CURRENT_DATE, INTERVAL 6 DAY), 'Published'),
(459, 31, 3, DATE_ADD(CURRENT_DATE, INTERVAL 6 DAY), 'Published'),
(460, 33, 3, DATE_ADD(CURRENT_DATE, INTERVAL 6 DAY), 'Published'),
(461, 35, 2, DATE_ADD(CURRENT_DATE, INTERVAL 6 DAY), 'Published'),
(462, 36, 1, DATE_ADD(CURRENT_DATE, INTERVAL 6 DAY), 'Published'),
(463, 37, 1, DATE_ADD(CURRENT_DATE, INTERVAL 6 DAY), 'Published'),
(464, 38, 2, DATE_ADD(CURRENT_DATE, INTERVAL 6 DAY), 'Published'),
(465, 39, 2, DATE_ADD(CURRENT_DATE, INTERVAL 6 DAY), 'Published'),
(466, 40, 1, DATE_ADD(CURRENT_DATE, INTERVAL 6 DAY), 'Published'),
(467, 41, 2, DATE_ADD(CURRENT_DATE, INTERVAL 6 DAY), 'Published');





-- MORE EMPLOYEES ADDED 
INSERT INTO Accounts (account_id, username, password_hash, is_active, role_id, created_at) VALUES
(55, 'receptionist10_55', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 2, CURRENT_TIMESTAMP),
(56, 'receptionist11_56', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 2, CURRENT_TIMESTAMP),
(57, 'fnb_kitchen10_57', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 3, CURRENT_TIMESTAMP),
(58, 'fnb_kitchen11_58', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 3, CURRENT_TIMESTAMP),
(59, 'fnb_pos10_59', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 4, CURRENT_TIMESTAMP),
(60, 'fnb_pos11_60', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 4, CURRENT_TIMESTAMP),
(61, 'housekeeping10_61', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 5, CURRENT_TIMESTAMP),
(62, 'housekeeping11_62', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 5, CURRENT_TIMESTAMP),
(63, 'housekeeping12_63', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 5, CURRENT_TIMESTAMP),
(64, 'housekeeping13_64', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 5, CURRENT_TIMESTAMP),
(65, 'maintainer10_65', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 6, CURRENT_TIMESTAMP),
(66, 'tourguide10_66', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 8, CURRENT_TIMESTAMP);

INSERT INTO Employees (employee_id, account_id, full_name, gender, cccd, phone, email, salary) VALUES
(45, 55, 'Vu Thu Hieu', 'Nam', '001290986315', '0912819524', 'receptionist10_55@hoanien.vn', 8500000),
(46, 56, 'Vu Hoai Hieu', 'Nu', '001239575875', '0912670267', 'receptionist11_56@hoanien.vn', 8000000),
(47, 57, 'Tran Van Duong', 'Nam', '001271232303', '0912699445', 'fnb_kitchen10_57@hoanien.vn', 7000000),
(48, 58, 'Huynh Hai Chau', 'Nu', '001225660157', '0912472207', 'fnb_kitchen11_58@hoanien.vn', 9000000),
(49, 59, 'Pham Xuan Nam', 'Nam', '001258744925', '0912713441', 'fnb_pos10_59@hoanien.vn', 7500000),
(50, 60, 'Vo Hai Trang', 'Nu', '001293637810', '0912503247', 'fnb_pos11_60@hoanien.vn', 9000000),
(51, 61, 'Duong Xuan Duong', 'Nam', '001294643391', '0912530786', 'housekeeping10_61@hoanien.vn', 7000000),
(52, 62, 'Le Xuan Hoa', 'Nam', '001273629534', '0912796510', 'housekeeping11_62@hoanien.vn', 7500000),
(53, 63, 'Hoang Xuan Long', 'Nu', '001285079496', '0912741337', 'housekeeping12_63@hoanien.vn', 8500000),
(54, 64, 'Vu Hai Hieu', 'Nam', '001289893281', '0912153324', 'housekeeping13_64@hoanien.vn', 9000000),
(55, 65, 'Phan Ngoc Hoa', 'Nu', '001231874828', '0912680787', 'maintainer10_65@hoanien.vn', 8000000),
(56, 66, 'Vo Huu Viet', 'Nam', '001272208878', '0912779852', 'tourguide10_66@hoanien.vn', 8000000);
-- Workflows Seed Data
INSERT INTO workflows (workflow_name, trigger_event, conditions_json, actions_json, is_active, updated_at) VALUES 
('Room Checkout Automation', 'ROOM_CHECKOUT', '{}', '[{"type":"UPDATE_ROOM_STATUS","value":"Vacant_Dirty"},{"type":"CREATE_OPERATION_TASK","value":"CHECKOUT_CLEAN"}]', true, NOW()),
('Room Report Damage Automation', 'ROOM_REPORT_DAMAGE', '{}', '[{"type":"CREATE_OPERATION_TASK","value":"Maintenance","priority":"High"}]', true, NOW()),
('Promotion Exceeded Automation', 'PROMOTION_EXCEEDED', '{}', '[{"type":"REQUIRE_MANAGER_APPROVAL"}]', true, NOW()),
('SLA Escalation Automation', 'SLA_ESCALATE', '{}', '[{"type":"SEND_EMAIL","target_email":"{{email}}","email_subject":"SLA Warning for {{taskName}}","email_body_html":"sla-warning"}]', true, NOW()),
('Account Security OTP', 'ACCOUNT_SECURITY', '{}', '[{"type":"SEND_EMAIL","target_email":"{{email}}","email_subject":"Security Alert","email_body_html":"security-alert"}]', true, NOW()),
('User Registration OTP', 'USER_REGISTRATION_OTP', '{}', '[{"type":"SEND_EMAIL","target_email":"{{email}}","email_subject":"Your OTP Code","email_body_html":"otp-email"}]', true, NOW()),
('User Password Reset', 'USER_PASSWORD_RESET', '{}', '[{"type":"SEND_EMAIL","target_email":"{{email}}","email_subject":"Password Reset Request","email_body_html":"reset-password"}]', true, NOW());
-- Kịch bản 1: Khách VIP PLATINUM Check-in -> Task F&B Welcome Fruit
INSERT INTO workflows (workflow_name, trigger_event, conditions_json, actions_json, is_active, updated_at) VALUES 
('VIP Welcome Package', 'ROOM_CHECKIN', '{"customer_tier": "PLATINUM"}', '[{"type": "CREATE_OPERATION_TASK", "value": "F&B_Welcome_Fruit", "priority": "High"}, {"type": "SEND_EMAIL", "target_email": "gm@kawai.com", "email_subject": "Khách PLATINUM đã tới!", "email_body_html": "Khách {{customer_name}} hạng PLATINUM đã check-in vào phòng {{room_number}}."}]', true, NOW());

-- Kịch bản 2: Upselling sau khi đặt phòng nếu lưu trú > 3 đêm
INSERT INTO workflows (workflow_name, trigger_event, conditions_json, actions_json, is_active, updated_at) VALUES 
('Delayed Upsell Email', 'BOOKING_CREATED', '{"stay_nights_gt": 3}', '[{"type": "SEND_EMAIL", "delay_minutes": 60, "target_email": "{{customer_email}}", "email_subject": "Món quà đặc biệt dành riêng cho kỳ nghỉ dài của bạn!", "email_body_html": "Cảm ơn {{customer_name}} đã đặt phòng. Nhận ngay voucher Spa 20%!"}]', true, NOW());

-- Kịch bản 3: Ưu tiên dọn phòng khẩn cấp vào mùa cao điểm (Tháng 7)
INSERT INTO workflows (workflow_name, trigger_event, conditions_json, actions_json, is_active, updated_at) VALUES 
('Peak Season Urgent Checkout', 'ROOM_CHECKOUT', '{"month": "7"}', '[{"type": "UPDATE_ROOM_STATUS", "value": "Vacant_Dirty"}, {"type": "CREATE_OPERATION_TASK", "value": "CHECKOUT_CLEAN", "priority": "Urgent"}]', true, NOW());

-- Thêm Mock Reviews (dữ liệu thật với Integrity Checks)

-- Thêm data test Unreviewed cho Customer 1 (ID 9001, 9002)

INSERT IGNORE INTO Bookings (booking_id, customer_id, booking_date, total_price, booking_status, booking_source) VALUES
(9001, 1, '2026-07-10', 1200000.00, 'Completed', 'Direct_Web');
INSERT IGNORE INTO Tour_Bookings (booking_id, schedule_id, participant_count, tour_charge) VALUES
(9001, 1, 2, 1200000.00);

INSERT IGNORE INTO Bookings (booking_id, customer_id, booking_date, total_price, booking_status, booking_source) VALUES
(9002, 1, '2026-07-05', 4500000.00, 'Checked_Out', 'Direct_Web');
INSERT IGNORE INTO Room_Bookings (room_booking_id, check_in_date, check_out_date, expected_check_in, expected_check_out) VALUES
(9002, '2026-07-06 14:00:00', '2026-07-08 12:00:00', '2026-07-06 14:00:00', '2026-07-08 12:00:00');
INSERT IGNORE INTO Room_Booking_Details (detail_id, room_booking_id, room_id, room_charge, detail_status, bed_preference) VALUES
(9002, 9002, 1, 4500000.00, 'Checked_Out', 'KING_SIZE');


-- -----------------------------------------------------------------------------

-- -----------------------------------------------------------------------------

-- =======================================================
-- PATCH REVIEW DATA FOR TESTING
-- =======================================================

-- Update 10 Tours to Completed for existing bookings
UPDATE Bookings SET booking_status = 'Completed' WHERE booking_id IN (1, 2, 3, 4, 6, 7, 8, 11, 12, 13);
UPDATE Bookings SET booking_status = 'Checked_Out' WHERE booking_id IN (8, 909, 9201, 11, 12, 13, 14, 15);
UPDATE Room_Booking_Details SET detail_status = 'Checked_Out' WHERE room_booking_id IN (8, 909, 9201, 11, 12, 13, 14, 15);

INSERT IGNORE INTO Reviews (customer_id, tour_booking_id, room_booking_detail_id, rating_service, rating_tour, rating_room_dining, review_text, created_at, moderation_status, is_reported) VALUES
(2, 9, NULL, 5, 5, NULL, 'Tour trải nghiệm rất tuyệt vời.', NOW(), 'Approved', false),
(3, 10, NULL, 5, 5, NULL, 'Thiền hành giúp tôi cân bằng cuộc sống.', NOW(), 'Approved', false),
(4, 11, NULL, 4, 5, NULL, 'Cảnh quan sông Hậu cực kỳ nên thơ.', NOW(), 'Approved', false),
(5, 12, NULL, 5, 4, NULL, 'HDV vô cùng chu đáo.', NOW(), 'Approved', false),
(6, 13, NULL, 5, 5, NULL, 'Gia đình tôi đã có kỷ niệm rất đáng nhớ.', NOW(), 'Approved', false),
(7, 24, NULL, 5, 5, NULL, 'Chắc chắn sẽ giới thiệu cho bạn bè.', NOW(), 'Approved', false),
(8, 25, NULL, 5, 5, NULL, 'Một chuyến đi chữa lành thực sự.', NOW(), 'Approved', false),
(9, 26, NULL, 4, 4, NULL, 'Rất đáng tiền, món ăn cũng ngon.', NOW(), 'Approved', false),
(10, 27, NULL, 5, 5, NULL, 'Con người ở đây vô cùng hiếu khách.', NOW(), 'Approved', false),
(11, 28, NULL, 5, 5, NULL, 'Mọi thứ vượt quá mong đợi.', NOW(), 'Approved', false),

(2, NULL, 8, 5, NULL, 5, 'Phòng sạch sẽ, view đẹp, ăn sáng ngon.', NOW(), 'Approved', false),
(3, NULL, 11, 5, NULL, 4, 'Đồ ăn phục vụ rất nhanh.', NOW(), 'Approved', false),
(4, NULL, 12, 5, NULL, 5, 'Dịch vụ phòng tuyệt hảo.', NOW(), 'Approved', false),
(5, NULL, 13, 4, NULL, 4, 'Hơi xa trung tâm nhưng rất yên tĩnh.', NOW(), 'Approved', false),
(6, NULL, 14, 5, NULL, 5, 'Hồ bơi đẹp tuyệt.', NOW(), 'Approved', false),
(7, NULL, 15, 5, NULL, 5, 'Các món đặc sản miền Tây rất lạ miệng.', NOW(), 'Approved', false),
(8, NULL, 9002, 5, NULL, 5, 'Nhân viên lễ tân rất tận tâm.', NOW(), 'Approved', false),

(6, NULL, NULL, 5, NULL, NULL, 'Dịch vụ toàn diện.', NOW(), 'Approved', false),
(7, NULL, NULL, 4, NULL, NULL, 'Không gian thiền rất sâu lắng.', NOW(), 'Approved', false),
(8, NULL, NULL, 5, NULL, NULL, 'Chuyến đi ý nghĩa.', NOW(), 'Approved', false),
(9, NULL, NULL, 5, NULL, NULL, 'Tuyệt vời.', NOW(), 'Approved', false),
(10, NULL, NULL, 5, NULL, NULL, 'Rất hài lòng.', NOW(), 'Approved', false),
(11, NULL, NULL, 4, NULL, NULL, 'Resort yên bình, không khí trong lành.', NOW(), 'Approved', false),
(12, NULL, NULL, 5, NULL, NULL, 'Xứng đáng với giá tiền.', NOW(), 'Approved', false),
(13, NULL, NULL, 5, NULL, NULL, 'Nhất định sẽ quay lại.', NOW(), 'Approved', false),
(14, NULL, NULL, 5, NULL, NULL, 'Đội ngũ chuyên nghiệp.', NOW(), 'Approved', false),
(15, NULL, NULL, 5, NULL, NULL, '10 điểm không có nhưng.', NOW(), 'Approved', false);

-- Insert Test Account with completed Bookings without Review
-- Khach hang 1: hoangnam, pass: admin123
-- Let's make sure Khach 1 has some completed bookings but no review!
UPDATE Bookings SET booking_status = 'Completed' WHERE booking_id = 9;
UPDATE Bookings SET booking_status = 'Checked_Out' WHERE booking_id = 10;
-- We know booking 9 is Tour (if not modified) and 10 is Room. We don't add reviews for them.
