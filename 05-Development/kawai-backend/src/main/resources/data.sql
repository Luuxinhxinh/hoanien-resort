-- ============================================================
-- KAWAI RESORT & TOUR HUB  COMPREHENSIVE SAMPLE DATA (V4.0)
-- Full Test Coverage: Bookings, Tours, F&B, Roles, Edge Cases
-- ============================================================

-- 1. Roles (10 rows)
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

-- 2. Accounts (password: admin123 / staff123)
INSERT INTO Accounts (account_id, username, password_hash, is_active, role_id, created_at) VALUES 
(1, 'admin', '$2a$10$4bnThA4xQVw1rF2POQv78uAQll2KsUsgF32JaYiVG5d2d3Fhgk83q', TRUE, 1, CURRENT_TIMESTAMP),
(2, 'tphuong', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 3, CURRENT_TIMESTAMP),
(3, 'nmquan', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 3, CURRENT_TIMESTAMP),
(4, 'lelinh', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 2, CURRENT_TIMESTAMP),
(5, 'hoangnam', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 9, CURRENT_TIMESTAMP),
(6, 'vanan', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 9, CURRENT_TIMESTAMP),
(7, 'phamtuan', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 8, CURRENT_TIMESTAMP),
(8, 'thibich', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 9, CURRENT_TIMESTAMP),
(9, 'ngocthi', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 9, CURRENT_TIMESTAMP),
(10, 'hunganh', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 10, CURRENT_TIMESTAMP),
(11, 'hathanh', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 10, CURRENT_TIMESTAMP),
(12, 'banned_user', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', FALSE, 10, CURRENT_TIMESTAMP),
(13, 'newbie_user', '$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 10, CURRENT_TIMESTAMP);

-- 3. Employees
INSERT INTO Employees (employee_id, account_id, full_name, email, phone, gender, cccd, hire_date, salary, is_resigned) VALUES
(1, 1, 'Admin System', 'admin@hoanien.com', '0901234567', 'MALE', '001099000001', '2023-01-01', 50000000, FALSE),
(2, 2, 'Trần Phương', 'tphuong@hoanien.com', '0912345678', 'FEMALE', '001099000002', '2024-03-15', 18000000, FALSE),
(3, 3, 'Nguyễn Minh Quân', 'nmquan@hoanien.com', '0987654321', 'MALE', '001099000003', '2024-04-01', 15000000, FALSE),
(4, 4, 'Lê Linh', 'lelinh@hoanien.com', '0933445566', 'FEMALE', '001099000004', '2023-08-10', 12000000, FALSE),
(5, 7, 'Phạm Tuấn', 'ptuan@hoanien.com', '0944556677', 'MALE', '001099000005', '2023-11-20', 20000000, FALSE);

-- 4. Customers (VIP, Normal, Newbie, Banned)
INSERT INTO Customers (customer_id, account_id, full_name, email, phone, gender, cccd_passport_encrypted, loyalty_points, customer_tier) VALUES
(1, 5, 'Lê Hoàng Nam', 'hoangnam@example.com', '0901112233', 'MALE', '001088000001', 52000, 'DIAMOND'),
(2, 6, 'Nguyễn Văn An', 'vanan@example.com', '0902223344', 'MALE', '001088000002', 15000, 'GOLD'),
(3, 8, 'Thị Bích', 'thibich@example.com', '0904445566', 'FEMALE', '001088000004', 35000, 'PLATINUM'),
(4, 9, 'Ngọc Thi', 'ngocthi@example.com', '0905556677', 'FEMALE', '001088000005', 4000, 'SILVER'),
(5, 10, 'Phạm Hùng Anh', 'hunganh@example.com', '0906667788', 'MALE', '001088000006', 95, 'BRONZE'),
(6, 11, 'Hà Thanh', 'hathanh@example.com', '0907778899', 'FEMALE', '001088000007', 200, 'BRONZE'),
(7, 12, 'Bad User', 'banned@example.com', '0908889900', 'MALE', '001088000008', 0, 'BRONZE'),
(8, 13, 'Newbie User', 'newbie@example.com', '0909990011', 'MALE', '001088000009', 0, 'BRONZE');

-- Dependents for Lê Hoàng Nam (VIP - In-house test)
INSERT INTO Dependents (dependent_id, customer_id, full_name, date_of_birth, relationship, face_vector_data, face_image_url) VALUES 
(1, 1, 'Trần Thu Thủy', '1995-08-15', 'Vợ', NULL, NULL),
(2, 1, 'Lê Nam Sơn', '2015-05-20', 'Con trai', NULL, NULL),
(3, 1, 'Lê Nam Hải', '2018-09-10', 'Con trai', NULL, NULL);

-- 5. Room Categories
INSERT INTO Room_Categories (category_id, category_name, description, base_price, capacity, base_adults, base_children, max_adults, max_children, extra_adult_surcharge, extra_child_surcharge, cover_img_url, is_active) VALUES
(1, 'Standard Room', 'Phòng tiêu chuẩn, tiện nghi cơ bản', 1200000, 2, 2, 1, 3, 2, 300000, 150000, 'https://example.com/img/standard.jpg', TRUE),
(2, 'Deluxe Room', 'Phòng rộng rãi, view vườn', 1800000, 2, 2, 1, 3, 2, 400000, 200000, 'https://example.com/img/deluxe.jpg', TRUE),
(3, 'Suite', 'Phòng cao cấp, không gian sang trọng', 3500000, 2, 2, 1, 3, 2, 500000, 250000, 'https://example.com/img/suite.jpg', TRUE),
(4, 'Family Room', 'Phòng cho gia đình, có bếp nhỏ', 2500000, 4, 4, 2, 6, 4, 400000, 200000, 'https://example.com/img/family.jpg', TRUE),
(5, 'Villa 2 Bedrooms', 'Biệt thự 2 phòng ngủ, hồ bơi riêng', 6000000, 4, 4, 2, 6, 4, 800000, 400000, 'https://example.com/img/villa2.jpg', TRUE),
(6, 'Presidential Suite', 'Phòng Tổng thống siêu cấp vip', 15000000, 2, 2, 1, 4, 2, 1000000, 500000, 'https://example.com/img/president.jpg', TRUE);

-- 6. Rooms (20 Rooms)
INSERT INTO Rooms (room_id, room_number, category_id, room_status) VALUES
(1, '101', 1, 'Vacant_Clean'),
(2, '102', 1, 'Vacant_Clean'),
(3, '103', 1, 'Vacant_Clean'),
(4, '104', 1, 'Vacant_Dirty'),
(5, '105', 1, 'Vacant_Dirty'),
(6, '201', 2, 'Vacant_Clean'),
(7, '202', 2, 'Vacant_Clean'),
(8, '203', 2, 'Occupied'), 
(9, '204', 2, 'OutOfOrder'), -- Hỏng để test
(10, '205', 2, 'Maintenance'), -- Bảo trì
(11, '301', 3, 'Occupied'),
(12, '302', 3, 'Occupied'),
(13, '303', 4, 'Vacant_Clean'),
(14, '304', 4, 'Vacant_Clean'),
(15, '401', 5, 'Vacant_Clean'),
(16, '402', 5, 'Vacant_Clean'),
(17, '501', 6, 'Vacant_Clean'),
(18, '502', 1, 'Vacant_Clean'),
(19, '503', 2, 'Vacant_Clean'),
(20, '504', 3, 'Vacant_Clean');

-- 7. Promotions
INSERT INTO Promotions (promotion_id, promo_code, discount_type, discount_value, max_discount_value_vnd, min_order_value_vnd, applicable_scope, max_uses, current_uses, valid_from, valid_to, is_active, manager_approval_threshold_pct) VALUES
(1, 'SUMMER30', 'PERCENTAGE', 30, 1000000, 2000000, 'ALL', 100, 5, '2026-06-01', '2026-08-31', TRUE, 25),
(2, 'VIP500K', 'FIXED_AMOUNT', 500000, NULL, 1500000, 'ROOM', 50, 10, '2026-01-01', '2026-12-31', TRUE, NULL),
(3, 'FNB10', 'PERCENTAGE', 10, 200000, 500000, 'FNB', 200, 20, '2026-06-01', '2026-07-31', TRUE, 15),
(4, 'EXPIRED20', 'PERCENTAGE', 20, 500000, 0, 'ALL', 100, 10, '2025-01-01', '2025-12-31', FALSE, NULL),
(5, 'MAXREACHED', 'FIXED_AMOUNT', 100000, NULL, 0, 'TOUR', 1, 1, '2026-01-01', '2026-12-31', TRUE, NULL);

-- 8. Bookings (Test Cases)
-- BK 1: Checked_Out (Lịch sử)
-- BK 2: Checked_In (Lê Hoàng Nam - In-house, cần folios, F&B)
-- BK 3: Confirmed (Phạm Hùng Anh - Sắp Check-in)
-- BK 4: Pending_Approval (Vượt mức km)
-- BK 5: Checked_Out (Lịch sử có Review)
INSERT INTO Bookings (booking_id, customer_id, booking_date, total_price, booking_status, booking_source, applied_promotion_id, version) VALUES
(1, 2, '2026-05-10', 3600000, 'Checked_Out', 'OTA', NULL, 1),
(2, 1, '2026-06-25', 10500000, 'Checked_In', 'Direct_Web', NULL, 1),
(3, 5, '2026-06-28', 1800000, 'Confirmed', 'Direct_Web', 2, 1),
(4, 3, '2026-06-29', 5000000, 'Pending_Approval', 'Direct_Web', 1, 1),
(5, 4, '2026-04-15', 7000000, 'Checked_Out', 'Direct_Web', NULL, 1);

-- Room Bookings
INSERT INTO Room_Bookings (room_booking_id, check_in_date, check_out_date, deposit_amount, cancellation_deadline, credit_limit, personal_pin_hash) VALUES
(1, '2026-05-15', '2026-05-17', 1000000, '2026-05-13', 3000000, 'hash'),
(2, '2026-06-28', '2026-07-01', 2000000, '2026-06-26', 10000000, 'hash'),
(3, '2026-06-30', '2026-07-02', 500000, '2026-06-28', 2000000, 'hash'),
(4, '2026-07-10', '2026-07-12', 0, '2026-07-08', 5000000, 'hash'),
(5, '2026-04-20', '2026-04-22', 1500000, '2026-04-18', 5000000, 'hash');

-- Room Booking Details
INSERT INTO Room_Booking_Details (detail_id, room_booking_id, category_id, room_id, room_charge, detail_status, bed_preference, special_requests, is_charge_to_room_allowed, sub_credit_limit, billing_routing_strategy, customer_id) VALUES
(1, 1, 2, 7, 3600000, 'Checked_Out', 'KING_SIZE', NULL, TRUE, 1000000, 'INDIVIDUAL', 2),
(2, 2, 3, 11, 7000000, 'Checked_In', 'KING_SIZE', 'Gần thang máy', TRUE, 5000000, 'BILL_TO_LEADER', 1),
(3, 2, 3, 12, 3500000, 'Checked_In', 'TWIN_BED', 'Trẻ em', TRUE, 5000000, 'BILL_TO_LEADER', 1),
(4, 3, 2, NULL, 1800000, 'Confirmed', 'KING_SIZE', 'Phòng yên tĩnh', TRUE, 2000000, 'INDIVIDUAL', 5),
(5, 4, 3, NULL, 5000000, 'Pending', 'KING_SIZE', 'Áp mã SUMMER30', TRUE, 5000000, 'INDIVIDUAL', 3),
(6, 5, 3, 11, 7000000, 'Checked_Out', 'KING_SIZE', 'Kỉ niệm ngày cưới', TRUE, 2000000, 'INDIVIDUAL', 4);

-- Link Room Status to current checkin
UPDATE Rooms SET current_booking_detail_id = 2, room_status = 'Occupied' WHERE room_id = 11;
UPDATE Rooms SET current_booking_detail_id = 3, room_status = 'Occupied' WHERE room_id = 12;

-- Room Guests
-- Checked-in guests (Lê Hoàng Nam in RM 11, Dependents in RM 12)
INSERT INTO Room_Guests (guest_id, detail_id, customer_id, dependent_id, guest_type, is_primary_contact) VALUES
(1, 2, 1, NULL, 'ADULT', TRUE),
(2, 2, NULL, 1, 'ADULT', FALSE),
(3, 3, NULL, 2, 'CHILD', FALSE),
(4, 3, NULL, 3, 'CHILD', FALSE);

-- 9. Tours & Schedules
INSERT INTO Tours (tour_id, tour_name, tour_type, base_price, duration, max_capacity, short_quote, description, image_url, is_active) VALUES
(1, 'Hạ Long 1 Ngày', 'Biển đảo', 1200000, '1 Ngày', 30, 'Khám phá di sản', 'Vịnh Hạ Long tuyến 2', 'https://example.com/halong.jpg', TRUE),
(2, 'Trekking Sapa', 'Mạo hiểm', 3000000, '2 Ngày 1 Đêm', 15, 'Chinh phục Fansipan', 'Trekking Fansipan', 'https://example.com/sapa.jpg', TRUE),
(3, 'Food Tour Hà Nội', 'Ẩm thực', 500000, 'Nửa ngày', 20, 'Thưởng thức đặc sản', 'Đi bộ và ăn vặt', 'https://example.com/food.jpg', TRUE);

INSERT INTO Tour_Schedules (schedule_id, tour_id, start_date, end_date, available_slots, status) VALUES
(1, 1, '2026-06-30 08:00:00', '2026-06-30 17:00:00', 30, 'OPEN'),
(2, 2, '2026-07-05 07:00:00', '2026-07-06 18:00:00', 0, 'FULL'), -- Đã full
(3, 3, '2026-05-15 15:00:00', '2026-05-15 20:00:00', 10, 'COMPLETED'); -- Quá khứ

-- Tour Bookings
INSERT INTO Tour_Bookings (booking_id, schedule_id, participant_count, tour_charge, is_walk_in_tour, room_booking_id) VALUES
(6, 1, 4, 4800000, FALSE, 2); -- Lê Hoàng Nam đặt tour kèm phòng

-- 10. F&B - Restaurant & Menus
INSERT INTO Menu_Items (item_id, item_name, category, description, price, is_available, image_url) VALUES
(1, 'Gỏi cuốn Tôm Thịt', 'Khai vị', 'Gỏi cuốn tươi ngon', 85000, TRUE, 'https://example.com/goicuon.jpg'),
(2, 'Bò bít tết', 'Món chính', 'Bò Úc sốt tiêu đen', 350000, TRUE, 'https://example.com/steak.jpg'),
(3, 'Cá hồi áp chảo', 'Món chính', 'Cá hồi Nauy sốt chanh leo', 280000, FALSE, 'https://example.com/salmon.jpg'), -- Hết hàng
(4, 'Nước ép Cam', 'Đồ uống', 'Cam vắt nguyên chất', 65000, TRUE, 'https://example.com/orange.jpg'),
(5, 'Bánh Tiramisu', 'Tráng miệng', 'Bánh ngọt phong cách Ý', 95000, TRUE, 'https://example.com/tiramisu.jpg');

-- Food Orders
INSERT INTO Food_Orders (order_id, booking_id, guest_id, staff_id, order_type, table_number, total_amount, order_status, payment_status, notes) VALUES
(1, 2, 1, 2, 'ROOM_SERVICE', 'RM11', 415000, 'PENDING', 'UNPAID', 'Phòng 301, không hành'), -- Lê Hoàng Nam gọi lên phòng, chưa làm
(2, 2, 1, 3, 'DINE_IN', 'T01', 500000, 'COOKING', 'UNPAID', 'Ít đá'), -- Đang nấu
(3, 2, 1, 2, 'DINE_IN', 'T02', 85000, 'SERVED', 'UNPAID', ''), -- Đã phục vụ nhưng chưa trả (sẽ charge to room)
(4, NULL, NULL, 3, 'DINE_IN', 'T05', 700000, 'PAID', 'PAID', 'Khách vãng lai'),
(5, NULL, NULL, 2, 'DINE_IN', 'T10', 0, 'CANCELLED', 'UNPAID', 'Khách đổi ý');

-- Food Order Details
INSERT INTO Food_Order_Details (detail_id, order_id, item_id, quantity, unit_price, subtotal) VALUES
(1, 1, 2, 1, 350000, 350000),
(2, 1, 4, 1, 65000, 65000),
(3, 2, 2, 1, 350000, 350000),
(4, 2, 5, 1, 95000, 95000),
(5, 3, 1, 1, 85000, 85000),
(6, 4, 2, 2, 350000, 700000);

-- 11. Folio (Chi phí phát sinh cho In-house)
INSERT INTO Folio_Items (folio_item_id, booking_id, room_booking_detail_id, payer_customer_id, source_department, amount, description, is_settled_separately, created_by_staff_id, created_at) VALUES 
(1, 2, 2, 1, 'LAUNDRY', 150000, 'Giặt ủi ngày 26/06', FALSE, 4, CURRENT_TIMESTAMP),
(2, 2, 3, 1, 'MINIBAR', 80000, 'Nước suối và snack', FALSE, 4, CURRENT_TIMESTAMP),
(3, 2, 2, 1, 'FNB', 85000, 'Order Nhà hàng T02', FALSE, 2, CURRENT_TIMESTAMP); -- Charge to room từ Order 3

-- 12. Reviews
INSERT INTO Reviews (review_id, customer_id, booking_id, rating_service, rating_cleanliness, rating_value, rating_amenities, review_text, moderation_status, created_at) VALUES
(1, 2, 1, 4, 4, 4, 4, 'Khách sạn sạch sẽ, dịch vụ tốt.', 'Approved', '2026-05-18 10:00:00'),
(2, 4, 5, 5, 5, 5, 5, 'Kì nghỉ kỉ niệm ngày cưới tuyệt vời!', 'Approved', '2026-04-25 15:30:00'),
(3, 2, 1, 2, 3, 2, 3, 'Đồ ăn nhà hàng phục vụ hơi chậm.', 'Pending', '2026-05-18 11:00:00'); -- Pending để duyệt

-- 13. Audit Logs
INSERT INTO Audit_Logs (log_id, account_id, action, table_name, record_id, old_value, new_value, ip_address, timestamp) VALUES
(1, 1, 'UPDATE_ROLE', 'Roles', 10, '{"permissions":""}', '{"permissions":"DASHBOARD"}', '192.168.1.1', '2026-06-25 08:00:00'),
(2, 2, 'CREATE_ORDER', 'Food_Orders', 1, NULL, '{"order_type":"ROOM_SERVICE"}', '192.168.1.5', '2026-06-28 12:30:00'),
(3, 3, 'APPROVE_REVIEW', 'Reviews', 1, '{"moderation_status":"Pending"}', '{"moderation_status":"Approved"}', '192.168.1.2', '2026-06-28 14:00:00');

-- 14. Reset Sequences
ALTER TABLE Roles AUTO_INCREMENT = 100;
ALTER TABLE Accounts AUTO_INCREMENT = 100;
ALTER TABLE Employees AUTO_INCREMENT = 100;
ALTER TABLE Customers AUTO_INCREMENT = 100;
ALTER TABLE Dependents AUTO_INCREMENT = 100;
ALTER TABLE Room_Categories AUTO_INCREMENT = 100;
ALTER TABLE Rooms AUTO_INCREMENT = 100;
ALTER TABLE Promotions AUTO_INCREMENT = 100;
ALTER TABLE Bookings AUTO_INCREMENT = 100;
ALTER TABLE Room_Bookings AUTO_INCREMENT = 100;
ALTER TABLE Room_Booking_Details AUTO_INCREMENT = 100;
ALTER TABLE Room_Guests AUTO_INCREMENT = 100;
ALTER TABLE Tours AUTO_INCREMENT = 100;
ALTER TABLE Tour_Schedules AUTO_INCREMENT = 100;
ALTER TABLE Tour_Bookings AUTO_INCREMENT = 100;
ALTER TABLE Menu_Items AUTO_INCREMENT = 100;
ALTER TABLE Food_Orders AUTO_INCREMENT = 100;
ALTER TABLE Food_Order_Details AUTO_INCREMENT = 100;
ALTER TABLE Folio_Items AUTO_INCREMENT = 100;
ALTER TABLE Reviews AUTO_INCREMENT = 100;
ALTER TABLE Audit_Logs AUTO_INCREMENT = 100;
