sql = "\n\n-- FIX HOANGNAM UNREALISTIC ACTIVE BOOKINGS\n"
sql += "UPDATE Bookings SET booking_status = 'Checked_Out' WHERE customer_id = 1 AND booking_id IN (50, 51, 103, 201);\n"
sql += "UPDATE Room_Bookings SET check_in_date = '2026-06-01', check_out_date = '2026-06-05' WHERE room_booking_id IN (50, 51, 103, 201);\n"

with open('d:\\SWP391\\su26-swp391-se2023-g2\\05-Development\\kawai-backend\\src\\main\\resources\\data.sql', 'a', encoding='utf-8') as f:
    f.write(sql)
