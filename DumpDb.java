import java.sql.*;
public class DumpDb {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://localhost:3306/kawai_db?serverTimezone=UTC&allowPublicKeyRetrieval=true&useSSL=false";
        try (Connection conn = DriverManager.getConnection(url, "root", "12345678");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT reservation_id, table_id, reserve_date, reserve_time, status FROM Table_Reservations")) {
            while (rs.next()) {
                System.out.println("ID: " + rs.getLong("reservation_id") +
                                   ", Table: " + rs.getLong("table_id") +
                                   ", Date: " + rs.getDate("reserve_date") +
                                   ", Time: " + rs.getTime("reserve_time") +
                                   ", Status: " + rs.getString("status"));
            }
        }
    }
}
