sql = "\n"
sql += "UPDATE Bookings SET total_price = 7500000 WHERE booking_id = 9;\n"
sql += "UPDATE Room_Bookings SET credit_limit = 5000000 WHERE room_booking_id = 9;\n"

with open('d:\\SWP391\\su26-swp391-se2023-g2\\05-Development\\kawai-backend\\src\\main\\resources\\data.sql', 'a', encoding='utf-8') as f:
    f.write(sql)
