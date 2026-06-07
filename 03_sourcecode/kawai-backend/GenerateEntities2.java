import java.nio.file.*;
import java.io.IOException;

public class GenerateEntities2 {
    public static void main(String[] args) throws IOException {
        String baseDir = "src/main/java/com/kawai/models/core/";
        Files.createDirectories(Paths.get(baseDir));

        write(baseDir + "Tour.java", "package com.kawai.models.core;\nimport jakarta.persistence.*;\nimport lombok.Data;\nimport java.math.BigDecimal;\n@Entity\n@Table(name = \"Tours\")\n@Data\npublic class Tour {\n    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;\n    @Column(name = \"tour_name\", unique = true) private String tourName;\n    private String description;\n    @Column(name = \"price_per_person\") private BigDecimal pricePerPerson;\n    @Column(name = \"max_capacity\") private Integer maxCapacity;\n}");

        write(baseDir + "TourSchedule.java", "package com.kawai.models.core;\nimport jakarta.persistence.*;\nimport lombok.Data;\nimport java.time.LocalDate;\nimport java.time.LocalTime;\n@Entity\n@Table(name = \"Tour_Schedules\")\n@Data\npublic class TourSchedule {\n    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;\n    @ManyToOne @JoinColumn(name = \"tour_id\") private Tour tour;\n    @Column(name = \"departure_date\") private LocalDate departureDate;\n    @Column(name = \"departure_time\") private LocalTime departureTime;\n    @Column(name = \"booked_seats\") private Integer bookedSeats;\n    @Enumerated(EnumType.STRING) @Column(name = \"schedule_status\") private ScheduleStatus scheduleStatus;\n    public enum ScheduleStatus { SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED }\n}");

        write(baseDir + "TourStaffAssignment.java", "package com.kawai.models.core;\nimport jakarta.persistence.*;\nimport lombok.Data;\n@Entity\n@Table(name = \"Tour_Staff_Assignments\")\n@Data\npublic class TourStaffAssignment {\n    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;\n    @ManyToOne @JoinColumn(name = \"schedule_id\") private TourSchedule schedule;\n    @ManyToOne @JoinColumn(name = \"employee_id\") private Employee employee;\n    @Column(name = \"role_in_tour\") private String roleInTour;\n}");

        write(baseDir + "TourBooking.java", "package com.kawai.models.core;\nimport jakarta.persistence.*;\nimport lombok.Data;\n@Entity\n@Table(name = \"Tour_Bookings\")\n@Data\npublic class TourBooking {\n    @Id private Long bookingId;\n    @OneToOne @MapsId @JoinColumn(name = \"booking_id\") private Booking booking;\n    @ManyToOne @JoinColumn(name = \"schedule_id\") private TourSchedule schedule;\n    @Column(name = \"participant_count\") private Integer participantCount;\n}");

        write(baseDir + "TourAttendee.java", "package com.kawai.models.core;\nimport jakarta.persistence.*;\nimport lombok.Data;\nimport java.time.LocalDateTime;\n@Entity\n@Table(name = \"Tour_Attendees\")\n@Data\npublic class TourAttendee {\n    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;\n    @ManyToOne @JoinColumn(name = \"tour_booking_id\") private TourBooking tourBooking;\n    @ManyToOne @JoinColumn(name = \"customer_id\") private Customer customer;\n    @ManyToOne @JoinColumn(name = \"dependent_id\") private Dependent dependent;\n    @Enumerated(EnumType.STRING) @Column(name = \"attendance_status\") private AttendanceStatus attendanceStatus;\n    @Column(name = \"face_matched_at\") private LocalDateTime faceMatchedAt;\n    public enum AttendanceStatus { PENDING, ATTENDED, NO_SHOW }\n}");

        write(baseDir + "MenuItem.java", "package com.kawai.models.core;\nimport jakarta.persistence.*;\nimport lombok.Data;\nimport java.math.BigDecimal;\n@Entity\n@Table(name = \"Menu_Items\")\n@Data\npublic class MenuItem {\n    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;\n    @Column(name = \"item_name\", unique = true) private String itemName;\n    private String category;\n    private BigDecimal price;\n    @Column(name = \"is_available\") private Boolean isAvailable;\n    private String description;\n}");

        write(baseDir + "RestaurantTable.java", "package com.kawai.models.core;\nimport jakarta.persistence.*;\nimport lombok.Data;\n@Entity\n@Table(name = \"Restaurant_Tables\")\n@Data\npublic class RestaurantTable {\n    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;\n    @Column(name = \"table_number\") private String tableNumber;\n    private Integer capacity;\n    @Enumerated(EnumType.STRING) private TableStatus status;\n    public enum TableStatus { AVAILABLE, OCCUPIED, RESERVED }\n}");
        
        System.out.println("Entities 2 generated successfully!");
    }

    private static void write(String path, String content) throws IOException {
        Files.writeString(Paths.get(path), content);
    }
}
