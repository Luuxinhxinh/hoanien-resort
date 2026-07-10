UPDATE Bookings SET booking_status = 'Checked_Out' WHERE customer_id = 1 AND booking_id IN (50, 51, 103, 201);
UPDATE Room_Bookings SET check_in_date = '2026-06-01', check_out_date = '2026-06-05' WHERE room_booking_id IN (50, 51, 103, 201);
