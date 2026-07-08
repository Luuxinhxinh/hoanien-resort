import java.sql.*;

public class CheckDB {
    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/kawai_db", "root", "12345678");
             Statement stmt = conn.createStatement()) {
            
            ResultSet rs = stmt.executeQuery("SELECT count(*) FROM accounts");
            if (rs.next()) {
                System.out.println("ACCOUNTS COUNT: " + rs.getInt(1));
            }
            
            rs = stmt.executeQuery("SELECT count(*) FROM employees");
            if (rs.next()) {
                System.out.println("EMPLOYEES COUNT: " + rs.getInt(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
