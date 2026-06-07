import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DropDb {
    public static void main(String[] args) throws Exception {
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC", "root", "Luu14102005@");
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("DROP DATABASE IF EXISTS kawai_db");
        stmt.executeUpdate("CREATE DATABASE kawai_db");
        System.out.println("Database reset successfully!");
        stmt.close();
        conn.close();
    }
}
