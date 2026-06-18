import java.sql.*;
public class CheckDb {
    public static void main(String[] args) throws Exception {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/kawai_db?user=root&password=Luu14102005@")) {
            String[] tables = {"Accounts", "Roles", "Rooms", "Room_Categories", "Room_Bookings", "Room_Booking_Details", "Room_Guests", "Bookings"};
            for (String t : tables) {
                try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM "+t)) {
                    if(rs.next()) System.out.println(t + ": " + rs.getInt(1));
                } catch(Exception e) { System.out.println(t + ": ERROR " + e.getMessage()); }
            }
        }
    }
}
