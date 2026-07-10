sql = "\n\n-- FIX PAST CHECKED_IN BOOKINGS TO CURRENT DATE\n"
sql += "UPDATE Room_Bookings rb \n"
sql += "JOIN Bookings b ON rb.room_booking_id = b.booking_id \n"
sql += "SET rb.check_in_date = CURDATE(), \n"
sql += "    rb.check_out_date = DATE_ADD(CURDATE(), INTERVAL 3 DAY), \n"
sql += "    rb.cancellation_deadline = DATE_SUB(CURDATE(), INTERVAL 1 DAY) \n"
sql += "WHERE b.booking_status = 'Checked_In' AND rb.check_in_date < CURDATE();\n\n"

sql += "UPDATE Bookings b \n"
sql += "JOIN Room_Bookings rb ON b.booking_id = rb.room_booking_id \n"
sql += "SET b.booking_date = DATE_SUB(rb.check_in_date, INTERVAL 7 DAY) \n"
sql += "WHERE b.booking_status = 'Checked_In' AND b.booking_date < DATE_SUB(CURDATE(), INTERVAL 30 DAY);\n\n"

sql += "UPDATE Bookings SET booking_status = 'Checked_Out' WHERE customer_id = 1 AND booking_id = 52;\n"
sql += "UPDATE Room_Booking_Details SET detail_status = 'Checked_Out' WHERE room_booking_id = 52;\n"

with open('d:\\SWP391\\su26-swp391-se2023-g2\\05-Development\\kawai-backend\\src\\main\\resources\\data.sql', 'a', encoding='utf-8') as f:
    f.write(sql)
