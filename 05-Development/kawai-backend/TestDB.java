import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestDB {
    public static void main(String[] args) throws Exception {
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/kawai_db?user=root&password=password");
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT work_date, count(*) FROM staff_schedules GROUP BY work_date");
        while (rs.next()) {
            System.out.println(rs.getString(1) + " : " + rs.getInt(2));
        }
        conn.close();
    }
}
