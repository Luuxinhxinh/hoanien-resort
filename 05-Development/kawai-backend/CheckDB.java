import java.sql.*;

public class CheckDB {
    public static void main(String[] args) {
        String[] passwords = {"123456", "12345678"};
        Connection conn = null;
        for (String pwd : passwords) {
            try {
                conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/kawai_db?useSSL=false&serverTimezone=UTC", "root", pwd);
                System.out.println("Connected to MySQL with password: " + pwd);
                break;
            } catch (Exception e) {
                // Try next password
            }
        }
        
        if (conn == null) {
            System.err.println("Could not connect to database.");
            return;
        }

        try (Statement stmt = conn.createStatement()) {
            // Cập nhật dữ liệu thực tế cho detail 99991 giống data.sql mới sửa
            stmt.executeUpdate("UPDATE Room_Booking_Details SET number_of_adults = 1, number_of_children = 0 WHERE detail_id = 99991");
            // Cập nhật các bản ghi null khác nếu có
            stmt.executeUpdate("UPDATE Room_Booking_Details SET number_of_adults = 2, number_of_children = 0 WHERE number_of_adults IS NULL");
            System.out.println("Updated Room_Booking_Details in database successfully.");

            System.out.println("\n=== 2. Verification - Room Booking Details ===");
            ResultSet rs = stmt.executeQuery(
                "SELECT rbd.detail_id, rbd.room_booking_id, rbd.room_id, rbd.number_of_adults, rbd.number_of_children, rbd.detail_status " +
                "FROM Room_Booking_Details rbd " +
                "WHERE rbd.room_booking_id IN (9999, 505) OR rbd.detail_status = 'Checked_In'"
            );
            while (rs.next()) {
                System.out.printf("Detail ID: %d | Booking ID: %d | Room ID: %s | Adults: %s | Children: %s | Status: %s\n",
                    rs.getInt("detail_id"), rs.getInt("room_booking_id"), rs.getString("room_id"),
                    rs.getObject("number_of_adults"), rs.getObject("number_of_children"),
                    rs.getString("detail_status"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }
}
