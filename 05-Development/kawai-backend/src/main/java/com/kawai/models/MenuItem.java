package com.kawai.models;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import org.hibernate.envers.Audited;

@Entity 
@Audited 
@Table(name="Menu_Items") 
@Data
public class MenuItem {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="item_id") private Long id;
    @Column(name="item_name", nullable=false) private String itemName;
    @Column(nullable=false) private BigDecimal price;
    @Column(nullable=false) private String category;
    @Column(name="is_available", nullable=false) private Boolean isAvailable = true;
    @Column(columnDefinition="TEXT") private String description;
    @Column(name="image_url") private String imageUrl;
    @Column(name="allergy_tags") private String allergenTags;

    // ==========================================
    // CÁC TRƯỜNG DỮ LIỆU DÀNH CHO TÍNH NĂNG "CHIA THỰC ĐƠN THEO NGÀY"
    // ==========================================

    /**
     * Cờ đánh dấu các món ăn CỐ ĐỊNH ngày nào cũng bán (ví dụ: nước ngọt, trà đá, cơm thêm).
     * Mặc định là 'true' để khi hệ thống cũ cập nhật lên, các món cũ không bị biến mất khỏi menu.
     */
    @Column(name="is_always_available", nullable=false) 
    private Boolean isAlwaysAvailable = true; 

    /**
     * Danh sách các ngày trong tuần (MONDAY -> SUNDAY) mà món ăn này được phép bán.
     * Hibernate sẽ tự động tạo một bảng phụ tên là `Menu_Item_Days` trong Database 
     * để lưu trữ mối quan hệ 1-Nhiều (1 Món ăn - Nhiều Ngày) này.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "Menu_Item_Days", joinColumns = @JoinColumn(name = "item_id"))
    @Column(name = "day_of_week")
    @Enumerated(EnumType.STRING)
    private java.util.Set<java.time.DayOfWeek> availableDays = new java.util.HashSet<>();
}
