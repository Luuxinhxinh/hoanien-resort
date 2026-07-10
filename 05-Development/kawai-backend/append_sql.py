import datetime

sql = "\n\n-- ============================================================\n"
sql += "-- APPENDED BOOKINGS FOR 10 CUSTOMERS (601-610) FOR MORE REALISTIC DATA\n"
sql += "-- ============================================================\n"

# Accounts
sql += "INSERT IGNORE INTO Accounts (account_id, username, password_hash, is_active, role_id, created_at) VALUES\n"
accounts = []
for i in range(601, 611):
    accounts.append(f"({i}, 'customer{i}', '/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG', TRUE, 10, CURRENT_TIMESTAMP)")
sql += ",\n".join(accounts) + ";\n\n"

# Customers
sql += "INSERT IGNORE INTO Customers (customer_id, account_id, full_name, email, phone, gender, cccd_passport_encrypted, loyalty_points, membership_tier_id) VALUES\n"
customers = []
for i in range(601, 611):
    customers.append(f"({i}, {i}, 'Khách Hàng {i}', 'customer{i}@example.com', '0909000{i}', 'Nam', 'CCCD{i}', 100, 1)")
sql += ",\n".join(customers) + ";\n\n"

# Dependents
sql += "INSERT IGNORE INTO Dependents (dependent_id, customer_id, dependent_name, birth_date, gender) VALUES\n"
dependents = []
for i in range(601, 611):
    dependents.append(f"({i}, {i}, 'Người Thân {i}', '2010-01-01', 'Nữ')")
sql += ",\n".join(dependents) + ";\n\n"

# Bookings (5 Checked_In, 5 Confirmed)
sql += "INSERT IGNORE INTO Bookings (booking_id, customer_id, booking_date, total_price, booking_status, booking_source, version) VALUES\n"
bookings = []
for i in range(601, 606): # Checked_In
    bookings.append(f"({i}, {i}, '2026-07-01', 5000000, 'Checked_In', 'Direct_Web', 1)")
for i in range(606, 611): # Confirmed
    bookings.append(f"({i}, {i}, '2026-07-01', 5000000, 'Confirmed', 'Direct_Web', 1)")
sql += ",\n".join(bookings) + ";\n\n"

# Room_Bookings
sql += "INSERT IGNORE INTO Room_Bookings (room_booking_id, check_in_date, check_out_date, deposit_amount, cancellation_deadline, credit_limit, personal_pin_hash) VALUES\n"
room_bookings = []
for i in range(601, 606): # Checked_In (today to +2 days)
    room_bookings.append(f"({i}, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 2 DAY), 1000000, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 5000000, 'hash{i}')")
for i in range(606, 611): # Confirmed (tomorrow to +3 days)
    room_bookings.append(f"({i}, DATE_ADD(CURDATE(), INTERVAL 1 DAY), DATE_ADD(CURDATE(), INTERVAL 3 DAY), 1000000, CURDATE(), 5000000, 'hash{i}')")
sql += ",\n".join(room_bookings) + ";\n\n"

# Room_Booking_Details
sql += "INSERT IGNORE INTO Room_Booking_Details (detail_id, room_booking_id, category_id, room_id, room_charge, detail_status, bed_preference, is_charge_to_room_allowed, sub_credit_limit, billing_routing_strategy, customer_id) VALUES\n"
details = []
# Vacant_Clean rooms: 6, 7, 8, 9, 14, 16, 19, 20
rooms = [6, 7, 8, 9, 14]
for idx, i in enumerate(range(601, 606)): # Checked_In -> needs room_id
    details.append(f"({i}1, {i}, 4, {rooms[idx]}, 5000000, 'Checked_In', 'KING_SIZE', TRUE, 5000000, 'BILL_TO_LEADER', {i})")
for i in range(606, 611): # Confirmed -> no room_id yet
    details.append(f"({i}1, {i}, 4, NULL, 5000000, 'Confirmed', 'KING_SIZE', TRUE, 5000000, 'BILL_TO_LEADER', {i})")
sql += ",\n".join(details) + ";\n\n"

# Room_Guests
sql += "INSERT IGNORE INTO Room_Guests (guest_id, detail_id, customer_id, dependent_id, guest_type, is_primary_contact) VALUES\n"
guests = []
for i in range(601, 606):
    guests.append(f"({i}11, {i}1, {i}, NULL, 'ADULT', TRUE)")
    guests.append(f"({i}12, {i}1, NULL, {i}, 'CHILD', FALSE)")
sql += ",\n".join(guests) + ";\n\n"

# Payment_Transactions
sql += "INSERT IGNORE INTO Payment_Transactions (id, booking_id, amount, status, transaction_type, payment_method, gateway_status, transaction_ref, created_at, paid_at) VALUES\n"
payments = []
for i in range(601, 611):
    payments.append(f"({i}1, {i}, 1000000, 'SUCCESS', 'Deposit', 'VNPAY', 'SUCCESS', 'DEP_{i}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)")
sql += ",\n".join(payments) + ";\n\n"

# Update Rooms for checked_in
sql += "UPDATE Rooms SET current_booking_detail_id = CASE room_id\n"
for idx, i in enumerate(range(601, 606)):
    sql += f"  WHEN {rooms[idx]} THEN {i}1\n"
sql += "  ELSE current_booking_detail_id END,\n"
sql += "room_status = CASE room_id\n"
for idx, i in enumerate(range(601, 606)):
    sql += f"  WHEN {rooms[idx]} THEN 'Occupied'\n"
sql += "  ELSE room_status END\n"
sql += "WHERE room_id IN (6, 7, 8, 9, 14);\n\n"


with open('d:\\SWP391\\su26-swp391-se2023-g2\\05-Development\\kawai-backend\\src\\main\\resources\\data.sql', 'a', encoding='utf-8') as f:
    f.write(sql)
