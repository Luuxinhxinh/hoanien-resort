import java.sql.*;
public class CheckDb {
    public static void main(String[] args) throws Exception {
        String[] passwords = {"123456", "Luu14102005@", ""};
        Connection conn = null;
        for (String pwd : passwords) {
            try {
                conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/kawai_db?user=root&password=" + pwd);
                System.out.println("Connected successfully with password: " + pwd);
                break;
            } catch (Exception e) {
                System.out.println("Failed with password '" + pwd + "': " + e.getMessage());
            }
        }
        
        if (conn == null) {
            System.out.println("Could not connect to database.");
            return;
        }
        
        try {
            String[] tables = {
                "Accounts", "Roles", "Rooms", "Room_Categories", 
                "Room_Bookings", "Room_Booking_Details", "Room_Guests", "Bookings",
                "Food_Orders", "Food_Order_Details", "Tours", "Tour_Bookings"
            };
            for (String t : tables) {
                try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + t)) {
                    if (rs.next()) System.out.println(t + ": " + rs.getInt(1));
                } catch (Exception e) { System.out.println(t + ": ERROR " + e.getMessage()); }
            }
        } finally {
            conn.close();
        }
    }
}
