import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestDb {
    public static void main(String[] args) {
        try {
            Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/kawai_db", "root", "12345678");
            Statement s = c.createStatement();
            ResultSet rs = s.executeQuery("SELECT count(*) FROM Accounts");
            if(rs.next()) System.out.println("ACCOUNTS: " + rs.getInt(1));
            
            ResultSet rs2 = s.executeQuery("SELECT count(*) FROM Roles");
            if(rs2.next()) System.out.println("ROLES: " + rs2.getInt(1));
            c.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
