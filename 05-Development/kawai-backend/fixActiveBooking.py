sql = "\n\n-- CREATE AN ALWAYS-ACTIVE BOOKING FOR HOANG NAM (CUSTOMER_ID = 1)\n"
sql += "INSERT IGNORE INTO Bookings (booking_id, customer_id, booking_date, total_price, booking_status, booking_source, version) \n"
sql += "VALUES (9999, 1, DATE_SUB(CURDATE(), INTERVAL 5 DAY), 5000000, 'Checked_In', 'Direct_Web', 1);\n\n"

sql += "INSERT IGNORE INTO Room_Bookings (room_booking_id, check_in_date, check_out_date, deposit_amount, cancellation_deadline, credit_limit, personal_pin_hash) \n"
sql += "VALUES (9999, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 3 DAY), 2000000, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 5000000, 'hash9999');\n\n"

sql += "INSERT IGNORE INTO Room_Booking_Details (detail_id, room_booking_id, category_id, room_id, room_charge, detail_status, bed_preference, is_charge_to_room_allowed, sub_credit_limit, billing_routing_strategy, customer_id) \n"
sql += "VALUES (99991, 9999, 1, 5, 5000000, 'Checked_In', 'KING_SIZE', TRUE, 5000000, 'INDIVIDUAL', 1);\n\n"

sql += "INSERT IGNORE INTO Room_Guests (guest_id, detail_id, customer_id, guest_type, is_primary_contact) \n"
sql += "VALUES (999911, 99991, 1, 'ADULT', TRUE);\n\n"

sql += "UPDATE Rooms SET current_booking_detail_id = 99991, room_status = 'Occupied' WHERE room_id = 5;\n"

with open('d:\\SWP391\\su26-swp391-se2023-g2\\05-Development\\kawai-backend\\src\\main\\resources\\data.sql', 'a', encoding='utf-8') as f:
    f.write(sql)
