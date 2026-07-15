import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestBcrypt {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = "$2a$10$4bnThA4xQVw1rF2POQv78uAQll2KsUsgF32JaYiVG5d2d3Fhgk83q";
        boolean matches = encoder.matches("admin123", hash);
        System.out.println("admin123 matches hash: " + matches);
        
        String staffHash = "$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG";
        boolean matchesStaff = encoder.matches("staff123", staffHash);
        System.out.println("staff123 matches staffHash: " + matchesStaff);
    }
}
