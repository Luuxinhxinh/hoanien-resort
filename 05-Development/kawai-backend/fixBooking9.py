sql = "\n\n-- FIX BOOKING 9 VALUES TO BE REALISTIC\n"
sql += "UPDATE Room_Booking_Details SET room_charge = 7500000, sub_credit_limit = 5000000 WHERE room_booking_id = 9;\n"
sql += "UPDATE Room_Bookings SET deposit_amount = 3000000 WHERE room_booking_id = 9;\n"

with open('d:\\SWP391\\su26-swp391-se2023-g2\\05-Development\\kawai-backend\\src\\main\\resources\\data.sql', 'a', encoding='utf-8') as f:
    f.write(sql)
