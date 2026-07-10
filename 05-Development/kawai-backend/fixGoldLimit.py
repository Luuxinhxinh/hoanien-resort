sql = "\n"
sql += "UPDATE Room_Booking_Details SET sub_credit_limit = 20000000 WHERE room_booking_id IN (9, 9999);\n"
sql += "UPDATE Room_Bookings SET credit_limit = 20000000 WHERE room_booking_id IN (9, 9999);\n"

with open('d:\\SWP391\\su26-swp391-se2023-g2\\05-Development\\kawai-backend\\src\\main\\resources\\data.sql', 'a', encoding='utf-8') as f:
    f.write(sql)
