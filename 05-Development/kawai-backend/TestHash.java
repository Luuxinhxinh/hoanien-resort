import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
public class TestHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println("admin123 matches? " + encoder.matches("admin123", "$2a$10$4bnThA4xQVw1rF2POQv78uAQll2KsUsgF32JaYiVG5d2d3Fhgk83q"));
        System.out.println("staff123 matches? " + encoder.matches("staff123", "$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG"));
    }
}
