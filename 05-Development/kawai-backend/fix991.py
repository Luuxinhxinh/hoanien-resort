sql = "\n\n-- FIX HOANGNAM BOOKING 991\n"
sql += "UPDATE Bookings SET booking_status = 'Checked_Out' WHERE customer_id = 1 AND booking_id = 991;\n"
sql += "UPDATE Room_Bookings SET check_in_date = '2025-06-10', check_out_date = '2025-06-15' WHERE room_booking_id = 991;\n"

with open('d:\\SWP391\\su26-swp391-se2023-g2\\05-Development\\kawai-backend\\src\\main\\resources\\data.sql', 'a', encoding='utf-8') as f:
    f.write(sql)
