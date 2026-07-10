sql = "\n\n-- RECONCILE AGGREGATED DATA TOTALS\n"
sql += "-- 1. Reconcile Bookings total_price for Room Bookings\n"
sql += "UPDATE Bookings b SET total_price = (\n"
sql += "    SELECT COALESCE(SUM(room_charge), 0) \n"
sql += "    FROM Room_Booking_Details \n"
sql += "    WHERE room_booking_id = b.booking_id\n"
sql += ") WHERE EXISTS (SELECT 1 FROM Room_Bookings rb WHERE rb.room_booking_id = b.booking_id);\n\n"

sql += "-- 2. Reconcile Bookings total_price for Tour Bookings\n"
sql += "UPDATE Bookings b SET total_price = (\n"
sql += "    SELECT COALESCE(SUM(tour_charge), 0) \n"
sql += "    FROM Tour_Bookings \n"
sql += "    WHERE booking_id = b.booking_id\n"
sql += ") WHERE EXISTS (SELECT 1 FROM Tour_Bookings tb WHERE tb.booking_id = b.booking_id);\n\n"

sql += "-- 3. Reconcile Room_Bookings credit_limit based on sub_credit_limit\n"
sql += "UPDATE Room_Bookings rb SET credit_limit = (\n"
sql += "    SELECT COALESCE(SUM(sub_credit_limit), 0) \n"
sql += "    FROM Room_Booking_Details \n"
sql += "    WHERE room_booking_id = rb.room_booking_id\n"
sql += ");\n"

with open('d:\\SWP391\\su26-swp391-se2023-g2\\05-Development\\kawai-backend\\src\\main\\resources\\data.sql', 'a', encoding='utf-8') as f:
    f.write(sql)
