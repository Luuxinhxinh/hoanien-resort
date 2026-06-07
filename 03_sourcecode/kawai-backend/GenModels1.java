import java.nio.file.*;
import java.io.IOException;

public class GenModels1 {
    public static void main(String[] args) throws IOException {
        String b = "src/main/java/com/kawai/models/core/";
        Files.createDirectories(Paths.get(b));

        w(b+"Role.java", """
package com.kawai.models.core;
import jakarta.persistence.*;
import lombok.Data;
@Entity @Table(name="Roles") @Data
public class Role {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="role_id") private Long id;
    @Column(name="role_name", unique=true, nullable=false) private String roleName;
}
""");

        w(b+"Account.java", """
package com.kawai.models.core;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
@Entity @Table(name="Accounts") @Data
public class Account {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="account_id") private Long id;
    @Column(unique=true, nullable=false) private String username;
    @Column(name="password_hash", nullable=false) private String passwordHash;
    @Column(name="is_active", nullable=false) private Boolean isActive = true;
    @ManyToOne @JoinColumn(name="role_id", nullable=false) private Role role;
    @Column(name="created_at") private LocalDateTime createdAt = LocalDateTime.now();
}
""");

        w(b+"Employee.java", """
package com.kawai.models.core;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
@Entity @Table(name="Employees") @Data
public class Employee {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="employee_id") private Long id;
    @OneToOne @JoinColumn(name="account_id", unique=true) private Account account;
    @Column(name="full_name", nullable=false) private String fullName;
    @Column(nullable=false) private String gender;
    @Column(unique=true, nullable=false) private String cccd;
    @Column(nullable=false) private String phone;
    @Column(unique=true, nullable=false) private String email;
    @Column(nullable=false) private BigDecimal salary;
}
""");

        w(b+"Customer.java", """
package com.kawai.models.core;
import jakarta.persistence.*;
import lombok.Data;
@Entity @Table(name="Customers") @Data
public class Customer {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="customer_id") private Long id;
    @OneToOne @JoinColumn(name="account_id", unique=true) private Account account;
    @Column(name="full_name", nullable=false) private String fullName;
    @Column(nullable=false) private String gender;
    @Column(name="cccd_passport_encrypted", unique=true) private String cccdPassportEncrypted;
    @Column(nullable=false) private String phone;
    @Column(unique=true, nullable=false) private String email;
    @Column(name="loyalty_points", nullable=false) private Integer loyaltyPoints = 0;
    @Column(name="membership_tier", nullable=false) private String membershipTier = "Regular";
}
""");

        w(b+"Dependent.java", """
package com.kawai.models.core;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
@Entity @Table(name="Dependents") @Data
public class Dependent {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="dependent_id") private Long id;
    @ManyToOne @JoinColumn(name="customer_id", nullable=false) private Customer customer;
    @Column(name="dependent_name", nullable=false) private String dependentName;
    @Column(name="birth_date", nullable=false) private LocalDate birthDate;
    @Column(nullable=false) private String gender;
    @Column(name="cccd_passport_encrypted") private String cccdPassportEncrypted;
}
""");

        w(b+"AuditLog.java", """
package com.kawai.models.core;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
@Entity @Table(name="Audit_Logs") @Data
public class AuditLog {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="log_id") private Long id;
    @ManyToOne @JoinColumn(name="account_id") private Account account;
    @Column(nullable=false) private String action;
    @Column(name="table_name", nullable=false) private String tableName;
    @Column(name="record_id", nullable=false) private Long recordId;
    @Column(name="old_value", columnDefinition="TEXT") private String oldValue;
    @Column(name="new_value", columnDefinition="TEXT") private String newValue;
    @Column(name="ip_address", nullable=false) private String ipAddress;
    @Column(nullable=false) private LocalDateTime timestamp = LocalDateTime.now();
}
""");

        w(b+"Booking.java", """
package com.kawai.models.core;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.math.BigDecimal;
@Entity @Table(name="Bookings") @Inheritance(strategy=InheritanceType.JOINED) @Data
public class Booking {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="booking_id") private Long id;
    @ManyToOne @JoinColumn(name="customer_id", nullable=false) private Customer customer;
    @Column(name="booking_date", nullable=false) private LocalDate bookingDate;
    @Column(name="total_price", nullable=false) private BigDecimal totalPrice;
    @Column(name="booking_status", nullable=false) private String bookingStatus = "Pending";
    @Column(name="booking_source", nullable=false) private String bookingSource = "Direct_Web";
    @Column(nullable=false) private Integer version = 1;
}
""");

        w(b+"RoomBooking.java", """
package com.kawai.models.core;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.math.BigDecimal;
@Entity @Table(name="Room_Bookings") @Data
public class RoomBooking extends Booking {
    @Column(name="check_in_date", nullable=false) private LocalDate checkInDate;
    @Column(name="check_out_date", nullable=false) private LocalDate checkOutDate;
    @Column(name="deposit_amount", nullable=false) private BigDecimal depositAmount;
    @Column(name="cancellation_deadline", nullable=false) private LocalDate cancellationDeadline;
    @Column(name="credit_limit", nullable=false) private BigDecimal creditLimit = new BigDecimal("5000000.00");
    @Column(name="personal_pin_hash", nullable=false) private String personalPinHash;
}
""");

        w(b+"RoomCategory.java", """
package com.kawai.models.core;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
@Entity @Table(name="Room_Categories") @Data
public class RoomCategory {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="category_id") private Long id;
    @Column(name="category_name", nullable=false) private String categoryName;
    @Column(name="base_price", nullable=false) private BigDecimal basePrice;
    @Column(nullable=false) private Integer capacity;
}
""");

        w(b+"Room.java", """
package com.kawai.models.core;
import jakarta.persistence.*;
import lombok.Data;
@Entity @Table(name="Rooms") @Data
public class Room {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="room_id") private Long id;
    @Column(name="room_number", unique=true, nullable=false) private String roomNumber;
    @ManyToOne @JoinColumn(name="category_id", nullable=false) private RoomCategory category;
    @Column(name="room_status", nullable=false) private String roomStatus = "Vacant_Clean";
    @Column(name="current_booking_detail_id") private Long currentBookingDetailId;
}
""");

        w(b+"RoomBookingDetail.java", """
package com.kawai.models.core;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
@Entity @Table(name="Room_Booking_Details") @Data
public class RoomBookingDetail {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="detail_id") private Long id;
    @ManyToOne @JoinColumn(name="booking_id", nullable=false) private RoomBooking roomBooking;
    @ManyToOne @JoinColumn(name="category_id", nullable=false) private RoomCategory category;
    @ManyToOne @JoinColumn(name="room_id") private Room room;
    @ManyToOne @JoinColumn(name="guest_customer_id", nullable=false) private Customer guestCustomer;
    @Column(name="room_charge", nullable=false) private BigDecimal roomCharge;
    @Column(name="detail_status", nullable=false) private String detailStatus = "Pending";
    @Column(name="is_charge_to_room_allowed", nullable=false) private Boolean isChargeToRoomAllowed = true;
    @Column(name="sub_credit_limit", nullable=false) private BigDecimal subCreditLimit = BigDecimal.ZERO;
    @Column(name="billing_routing_strategy", nullable=false) private String billingRoutingStrategy = "BILL_TO_LEADER";
}
""");

        w(b+"Tour.java", """
package com.kawai.models.core;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
@Entity @Table(name="Tours") @Data
public class Tour {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="tour_id") private Long id;
    @Column(name="tour_name", nullable=false) private String tourName;
    @Column(name="tour_type", nullable=false) private String tourType;
    @Column(name="base_price", nullable=false) private BigDecimal basePrice;
    @Column(name="max_capacity", nullable=false) private Integer maxCapacity = 30;
}
""");

        w(b+"TourSchedule.java", """
package com.kawai.models.core;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
@Entity @Table(name="Tour_Schedules") @Data
public class TourSchedule {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="schedule_id") private Long id;
    @ManyToOne @JoinColumn(name="tour_id", nullable=false) private Tour tour;
    @Column(name="departure_date", nullable=false) private LocalDate departureDate;
    @Column(name="available_slots", nullable=false) private Integer availableSlots;
    @Column(name="schedule_status", nullable=false) private String scheduleStatus = "Open";
}
""");

        w(b+"TourBooking.java", """
package com.kawai.models.core;
import jakarta.persistence.*;
import lombok.Data;
@Entity @Table(name="Tour_Bookings") @Data
public class TourBooking extends Booking {
    @ManyToOne @JoinColumn(name="schedule_id", nullable=false) private TourSchedule schedule;
    @Column(name="participant_count", nullable=false) private Integer participantCount;
    @Column(name="attendance_status", nullable=false) private String attendanceStatus = "Not_Show";
    @Column(name="is_walk_in_tour", nullable=false) private Boolean isWalkInTour = false;
}
""");

        System.out.println("Part 1 Generated");
    }
    private static void w(String p, String c) throws IOException { Files.writeString(Paths.get(p), c); }
}
