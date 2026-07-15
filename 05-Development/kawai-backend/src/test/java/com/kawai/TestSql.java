package com.kawai;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class TestSql {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void testDatabase() {
        System.out.println("TEST_SQL_OUTPUT_START");
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            "SELECT d.detail_id, d.room_booking_id, b.booking_id, " +
            "CASE WHEN tb.booking_id IS NOT NULL THEN 'TourBooking' " +
            "WHEN rb.room_booking_id IS NOT NULL THEN 'RoomBooking' " +
            "ELSE 'BookingOnly' END as booking_type " +
            "FROM Room_Booking_Details d " +
            "JOIN Bookings b ON d.room_booking_id = b.booking_id " +
            "LEFT JOIN Tour_Bookings tb ON b.booking_id = tb.booking_id " +
            "LEFT JOIN Room_Bookings rb ON b.booking_id = rb.room_booking_id " +
            "WHERE tb.booking_id IS NOT NULL"
        );
        for (Map<String, Object> row : rows) {
            System.out.println("Found invalid detail: " + row);
        }
        System.out.println("TEST_SQL_OUTPUT_END");
    }
}
