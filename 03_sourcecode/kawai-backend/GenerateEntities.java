import java.nio.file.*;
import java.io.IOException;

public class GenerateEntities {
    public static void main(String[] args) throws IOException {
        String baseDir = "src/main/java/com/kawai/models/core/";
        Files.createDirectories(Paths.get(baseDir));

        write(baseDir + "Account.java", "package com.kawai.models.core;\nimport jakarta.persistence.*;\nimport lombok.Data;\nimport java.time.LocalDateTime;\n@Entity\n@Table(name = \"Accounts\")\n@Data\npublic class Account {\n    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;\n    @Column(unique = true, nullable = false) private String email;\n    @Column(name = \"password_hash\", nullable = false) private String passwordHash;\n    @ManyToOne @JoinColumn(name = \"role_id\") private Role role;\n    @Enumerated(EnumType.STRING) private Status status;\n    @Column(name = \"created_at\") private LocalDateTime createdAt;\n    public enum Status { ACTIVE, INACTIVE, BANNED }\n}");
        
        write(baseDir + "Employee.java", "package com.kawai.models.core;\nimport jakarta.persistence.*;\nimport lombok.Data;\nimport java.math.BigDecimal;\n@Entity\n@Table(name = \"Employees\")\n@Data\npublic class Employee {\n    @Id private Long accountId;\n    @OneToOne @MapsId @JoinColumn(name = \"account_id\") private Account account;\n    @Column(name = \"full_name\") private String fullName;\n    @Column(unique = true) private String cccd;\n    @Column(unique = true) private String phone;\n    private String address;\n    @Column(name = \"base_salary\") private BigDecimal baseSalary;\n}");

        write(baseDir + "Customer.java", "package com.kawai.models.core;\nimport jakarta.persistence.*;\nimport lombok.Data;\n@Entity\n@Table(name = \"Customers\")\n@Data\npublic class Customer {\n    @Id private Long accountId;\n    @OneToOne @MapsId @JoinColumn(name = \"account_id\") private Account account;\n    @Column(name = \"full_name\") private String fullName;\n    private String cccd;\n    private String passport;\n    private String phone;\n    @Column(name = \"loyalty_points\") private Integer loyaltyPoints;\n    private String tier;\n}");

        write(baseDir + "Dependent.java", "package com.kawai.models.core;\nimport jakarta.persistence.*;\nimport lombok.Data;\nimport java.time.LocalDate;\n@Entity\n@Table(name = \"Dependents\")\n@Data\npublic class Dependent {\n    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;\n    @ManyToOne @JoinColumn(name = \"primary_customer_id\") private Customer primaryCustomer;\n    @Column(name = \"full_name\") private String fullName;\n    private LocalDate dob;\n    private String cccd;\n    private String passport;\n}");

        write(baseDir + "RoomCategory.java", "package com.kawai.models.core;\nimport jakarta.persistence.*;\nimport lombok.Data;\nimport java.math.BigDecimal;\n@Entity\n@Table(name = \"Room_Categories\")\n@Data\npublic class RoomCategory {\n    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;\n    @Column(name = \"category_name\", unique = true) private String categoryName;\n    @Column(name = \"base_price\") private BigDecimal basePrice;\n    @Column(name = \"max_capacity\") private Integer maxCapacity;\n    private String description;\n}");

        write(baseDir + "Room.java", "package com.kawai.models.core;\nimport jakarta.persistence.*;\nimport lombok.Data;\n@Entity\n@Table(name = \"Rooms\")\n@Data\npublic class Room {\n    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;\n    @Column(name = \"room_number\", unique = true) private String roomNumber;\n    @ManyToOne @JoinColumn(name = \"category_id\") private RoomCategory category;\n    @Enumerated(EnumType.STRING) @Column(name = \"room_status\") private RoomStatus roomStatus;\n    public enum RoomStatus { VACANT_CLEAN, VACANT_DIRTY, OCCUPIED, MAINTENANCE }\n}");

        write(baseDir + "Booking.java", "package com.kawai.models.core;\nimport jakarta.persistence.*;\nimport lombok.Data;\nimport java.time.LocalDateTime;\nimport java.math.BigDecimal;\n@Entity\n@Table(name = \"Bookings\")\n@Inheritance(strategy = InheritanceType.JOINED)\n@Data\npublic abstract class Booking {\n    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;\n    @ManyToOne @JoinColumn(name = \"customer_id\") private Customer customer;\n    @Column(name = \"booking_date\") private LocalDateTime bookingDate;\n    @Column(name = \"total_price\") private BigDecimal totalPrice;\n    @Enumerated(EnumType.STRING) @Column(name = \"booking_status\") private BookingStatus bookingStatus;\n    @Enumerated(EnumType.STRING) private Source source;\n    @Version private Integer version;\n    public enum BookingStatus { PENDING, CONFIRMED, CANCELLED, COMPLETED }\n    public enum Source { WEBSITE, OTA, DIRECT }\n}");

        write(baseDir + "RoomBooking.java", "package com.kawai.models.core;\nimport jakarta.persistence.*;\nimport lombok.Data;\nimport java.time.LocalDate;\nimport java.math.BigDecimal;\n@Entity\n@Table(name = \"Room_Bookings\")\n@Data\npublic class RoomBooking extends Booking {\n    @Column(name = \"check_in_date\") private LocalDate checkInDate;\n    @Column(name = \"check_out_date\") private LocalDate checkOutDate;\n    @Column(name = \"deposit_amount\") private BigDecimal depositAmount;\n    @Column(name = \"credit_limit\") private BigDecimal creditLimit;\n    @Column(name = \"personal_pin_hash\") private String personalPinHash;\n}");

        write(baseDir + "RoomBookingDetail.java", "package com.kawai.models.core;\nimport jakarta.persistence.*;\nimport lombok.Data;\nimport java.math.BigDecimal;\n@Entity\n@Table(name = \"Room_Booking_Details\")\n@Data\npublic class RoomBookingDetail {\n    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;\n    @ManyToOne @JoinColumn(name = \"room_booking_id\") private RoomBooking roomBooking;\n    @ManyToOne @JoinColumn(name = \"room_id\") private Room room;\n    @ManyToOne @JoinColumn(name = \"guest_customer_id\") private Customer guestCustomer;\n    @Enumerated(EnumType.STRING) @Column(name = \"detail_status\") private DetailStatus detailStatus;\n    @Column(name = \"room_charge\") private BigDecimal roomCharge;\n    public enum DetailStatus { PENDING, CHECKED_IN, CHECKED_OUT }\n}");
        
        System.out.println("Entities generated successfully!");
    }

    private static void write(String path, String content) throws IOException {
        Files.writeString(Paths.get(path), content);
    }
}
