import java.nio.file.*;
import java.io.IOException;

public class GenModels2 {
    public static void main(String[] args) throws IOException {
        String b = "src/main/java/com/kawai/models/core/";

        w(b+"DynamicPricing.java", """
package com.kawai.models.core;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
@Entity @Table(name="Dynamic_Pricing") @Data
public class DynamicPricing {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="price_id") private Long id;
    @ManyToOne @JoinColumn(name="category_id", nullable=false) private RoomCategory category;
    @Column(name="start_date", nullable=false) private LocalDate startDate;
    @Column(name="end_date", nullable=false) private LocalDate endDate;
    @Column(name="price_modifier", nullable=false) private BigDecimal priceModifier;
}
""");

        w(b+"HotelOperation.java", """
package com.kawai.models.core;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
@Entity @Table(name="Hotel_Operations") @Data
public class HotelOperation {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="task_id") private Long id;
    @ManyToOne @JoinColumn(name="room_id", nullable=false) private Room room;
    @ManyToOne @JoinColumn(name="staff_id", nullable=false) private Employee staff;
    @ManyToOne @JoinColumn(name="supervisor_id", nullable=false) private Employee supervisor;
    @Column(name="operational_type", nullable=false) private String operationalType;
    @Column(nullable=false) private String priority = "Normal";
    @Column(nullable=false) private String status = "Pending";
    @Column(name="started_at") private LocalDateTime startedAt;
    @Column(name="completed_at") private LocalDateTime completedAt;
    @Column(columnDefinition="TEXT") private String notes;
}
""");

        w(b+"TourStaffAssignment.java", """
package com.kawai.models.core;
import jakarta.persistence.*;
import lombok.Data;
@Entity @Table(name="Tour_Staff_Assignments") @Data
public class TourStaffAssignment {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="assignment_id") private Long id;
    @ManyToOne @JoinColumn(name="schedule_id", nullable=false) private TourSchedule schedule;
    @ManyToOne @JoinColumn(name="employee_id", nullable=false) private Employee employee;
    @Column(name="staff_role", nullable=false) private String staffRole;
}
""");

        w(b+"RestaurantTable.java", """
package com.kawai.models.core;
import jakarta.persistence.*;
import lombok.Data;
@Entity @Table(name="Restaurant_Tables") @Data
public class RestaurantTable {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="table_id") private Long id;
    @Column(name="table_number", unique=true, nullable=false) private String tableNumber;
    @Column(nullable=false) private Integer capacity;
    @Column(name="table_status", nullable=false) private String tableStatus = "Vacant";
}
""");

        w(b+"TableReservation.java", """
package com.kawai.models.core;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import java.math.BigDecimal;
@Entity @Table(name="Table_Reservations") @Data
public class TableReservation {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="reservation_id") private Long id;
    @ManyToOne @JoinColumn(name="customer_id", nullable=false) private Customer customer;
    @ManyToOne @JoinColumn(name="table_id", nullable=false) private RestaurantTable table;
    @Column(name="reserve_date", nullable=false) private LocalDate reserveDate;
    @Column(name="reserve_time", nullable=false) private LocalTime reserveTime;
    @Column(name="deposit_amount", nullable=false) private BigDecimal depositAmount = BigDecimal.ZERO;
    @Column(nullable=false) private String status = "Pending";
}
""");

        w(b+"MenuItem.java", """
package com.kawai.models.core;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
@Entity @Table(name="Menu_Items") @Data
public class MenuItem {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="item_id") private Long id;
    @Column(name="item_name", nullable=false) private String itemName;
    @Column(nullable=false) private BigDecimal price;
    @Column(nullable=false) private String category;
    @Column(name="is_available", nullable=false) private Boolean isAvailable = true;
}
""");

        w(b+"FoodOrder.java", """
package com.kawai.models.core;
import jakarta.persistence.*;
import lombok.Data;
@Entity @Table(name="Food_Orders") @Data
public class FoodOrder {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="order_id") private Long id;
    @ManyToOne @JoinColumn(name="booking_id") private Booking booking;
    @ManyToOne @JoinColumn(name="room_booking_detail_id") private RoomBookingDetail roomBookingDetail;
    @ManyToOne @JoinColumn(name="table_id") private RestaurantTable table;
    @Column(name="order_type", nullable=false) private String orderType;
    @Column(name="kot_status", nullable=false) private String kotStatus = "Pending";
    @Column(name="payment_type", nullable=false) private String paymentType;
    @Column(name="is_paid_in_pos", nullable=false) private Boolean isPaidInPos = false;
    @ManyToOne @JoinColumn(name="created_by_staff_id", nullable=false) private Employee createdByStaff;
    @ManyToOne @JoinColumn(name="kitchen_processed_by_id") private Employee kitchenProcessedBy;
}
""");

        w(b+"FoodOrderDetail.java", """
package com.kawai.models.core;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
@Entity @Table(name="Food_Order_Details") @Data
public class FoodOrderDetail {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="detail_id") private Long id;
    @ManyToOne @JoinColumn(name="order_id", nullable=false) private FoodOrder foodOrder;
    @ManyToOne @JoinColumn(name="item_id", nullable=false) private MenuItem menuItem;
    @Column(nullable=false) private Integer quantity;
    @Column(name="price_at_order", nullable=false) private BigDecimal priceAtOrder;
}
""");

        w(b+"FolioItem.java", """
package com.kawai.models.core;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
@Entity @Table(name="Folio_Items") @Data
public class FolioItem {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="folio_item_id") private Long id;
    @ManyToOne @JoinColumn(name="booking_id", nullable=false) private Booking booking;
    @ManyToOne @JoinColumn(name="room_booking_detail_id") private RoomBookingDetail roomBookingDetail;
    @ManyToOne @JoinColumn(name="payer_customer_id", nullable=false) private Customer payerCustomer;
    @Column(name="source_department", nullable=false) private String sourceDepartment;
    @Column(nullable=false) private BigDecimal amount;
    @Column(nullable=false) private String description;
    @Column(name="is_settled_separately", nullable=false) private Boolean isSettledSeparately = false;
    @ManyToOne @JoinColumn(name="created_by_staff_id") private Employee createdByStaff;
    @Column(name="signature_img_url", length=500) private String signatureImgUrl;
}
""");

        w(b+"ConsolidatedInvoice.java", """
package com.kawai.models.core;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity @Table(name="Consolidated_Invoices") @Data
public class ConsolidatedInvoice {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="invoice_id") private Long id;
    @Column(name="invoice_number", unique=true, nullable=false) private String invoiceNumber;
    @OneToOne @JoinColumn(name="booking_id", unique=true, nullable=false) private Booking booking;
    @Column(name="subtotal_before_vat", nullable=false) private BigDecimal subtotalBeforeVat;
    @Column(name="vat_amount", nullable=false) private BigDecimal vatAmount;
    @Column(name="total_amount", nullable=false) private BigDecimal totalAmount;
    @ManyToOne @JoinColumn(name="promo_id") private Promotion promo;
    @Column(name="invoice_status", nullable=false) private String invoiceStatus = "Draft";
    @Column(name="issued_at", nullable=false) private LocalDateTime issuedAt = LocalDateTime.now();
}
""");

        w(b+"PaymentTransaction.java", """
package com.kawai.models.core;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
@Entity @Table(name="Payment_Transactions") @Data
public class PaymentTransaction {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="transaction_id") private Long id;
    @ManyToOne @JoinColumn(name="invoice_id", nullable=false) private ConsolidatedInvoice invoice;
    @Column(nullable=false) private BigDecimal amount;
    @Column(name="payment_method", nullable=false) private String paymentMethod;
    @Column(name="transaction_status", nullable=false) private String transactionStatus = "Pending";
}
""");

        w(b+"Promotion.java", """
package com.kawai.models.core;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
@Entity @Table(name="Promotions") @Data
public class Promotion {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="promo_id") private Long id;
    @Column(name="promo_code", unique=true, nullable=false) private String promoCode;
    @Column(name="discount_type", nullable=false) private String discountType;
    @Column(name="discount_value", nullable=false) private BigDecimal discountValue;
    @Column(name="valid_to", nullable=false) private LocalDate validTo;
    @Column(name="is_active", nullable=false) private Boolean isActive = true;
}
""");

        w(b+"Review.java", """
package com.kawai.models.core;
import jakarta.persistence.*;
import lombok.Data;
@Entity @Table(name="Reviews") @Data
public class Review {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="review_id") private Long id;
    @ManyToOne @JoinColumn(name="customer_id", nullable=false) private Customer customer;
    @ManyToOne @JoinColumn(name="room_booking_detail_id") private RoomBookingDetail roomBookingDetail;
    @ManyToOne @JoinColumn(name="tour_booking_id") private TourBooking tourBooking;
    @Column(name="rating_service", nullable=false) private Integer ratingService;
    @Column(columnDefinition="TEXT") private String comment;
}
""");

        System.out.println("Part 2 Generated");
    }
    private static void w(String p, String c) throws IOException { Files.writeString(Paths.get(p), c); }
}
