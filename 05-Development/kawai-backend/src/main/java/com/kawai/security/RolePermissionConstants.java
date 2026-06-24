package com.kawai.security;

import java.util.*;

/**
 * Định nghĩa bộ quyền mặc định cho từng vai trò trong hệ thống.
 * Mỗi quyền tương ứng với một module / tính năng nghiệp vụ.
 *
 * Khi Admin tạo một Role mới hoặc khi Account được tạo với một Role,
 * hệ thống sẽ tự động gán quyền mặc định dựa trên bảng này.
 * Admin có thể tước bỏ bất kỳ quyền nào sau đó.
 */
public class RolePermissionConstants {

    // ── Danh sách tất cả quyền hợp lệ trong hệ thống ──────────────────────────
    public static final String DASHBOARD       = "DASHBOARD";
    public static final String MASTER_DATA     = "MASTER_DATA";
    public static final String AUDIT_LOG       = "AUDIT_LOG";
    public static final String REVIEWS         = "REVIEWS";
    public static final String BOOKING         = "BOOKING";
    public static final String FNB             = "FNB";
    public static final String HOUSEKEEPING    = "HOUSEKEEPING";
    public static final String MAINTENANCE     = "MAINTENANCE";
    public static final String WORKFLOW        = "WORKFLOW";
    public static final String CRM             = "CRM";
    public static final String PROMOTIONS      = "PROMOTIONS";
    public static final String NIGHT_AUDIT     = "NIGHT_AUDIT";
    public static final String TOUR            = "TOUR";
    public static final String ANALYTICS       = "ANALYTICS";

    // ── Mô tả hiển thị (cho UI Admin) ─────────────────────────────────────────
    public static final Map<String, String> PERMISSION_LABELS;
    static {
        Map<String, String> m = new LinkedHashMap<>();
        m.put(DASHBOARD,    "Dashboard Tổng Quan");
        m.put(BOOKING,      "Quản lý Đặt phòng");
        m.put(FNB,          "Quản lý F&B (Nhà hàng)");
        m.put(TOUR,         "Quản lý Tour");
        m.put(HOUSEKEEPING, "Buồng phòng (Housekeeping)");
        m.put(MAINTENANCE,  "Bảo trì & Sự cố");
        m.put(NIGHT_AUDIT,  "Đêm kiểm (Night Audit)");
        m.put(ANALYTICS,    "Báo cáo & Phân tích");
        m.put(REVIEWS,      "Quản lý Đánh giá");
        m.put(MASTER_DATA,  "Cấu hình Dữ liệu (Master Data)");
        m.put(CRM,          "Hồ sơ Khách hàng (CRM)");
        m.put(PROMOTIONS,   "Mã giảm giá & Khuyến mại");
        m.put(WORKFLOW,     "Kích hoạt Quy trình");
        m.put(AUDIT_LOG,    "Nhật ký Audit Log");
        PERMISSION_LABELS = Collections.unmodifiableMap(m);
    }

    // ── Bộ quyền mặc định theo tên vai trò ────────────────────────────────────
    // Key: chứa một phần tên role (case-insensitive match)
    // Value: danh sách quyền được trao tự động khi tạo
    private static final Map<String, List<String>> ROLE_DEFAULT_PERMISSIONS;
    static {
        Map<String, List<String>> m = new LinkedHashMap<>();

        // ADMIN — được hết
        m.put("admin", Arrays.asList(
            DASHBOARD, MASTER_DATA, AUDIT_LOG, REVIEWS, BOOKING,
            FNB, HOUSEKEEPING, MAINTENANCE, WORKFLOW, CRM,
            PROMOTIONS, NIGHT_AUDIT, TOUR, ANALYTICS
        ));

        // MANAGER — gần giống Admin, không có phân quyền RBAC
        m.put("manager", Arrays.asList(
            DASHBOARD, BOOKING, FNB, TOUR, HOUSEKEEPING,
            MAINTENANCE, NIGHT_AUDIT, ANALYTICS, REVIEWS, CRM,
            PROMOTIONS, WORKFLOW
        ));

        // RECEPTIONIST — lễ tân, tập trung check-in/out và folio
        m.put("receptionist", Arrays.asList(
            DASHBOARD, BOOKING, HOUSEKEEPING, NIGHT_AUDIT, REVIEWS
        ));

        // F&B KITCHEN / FNB — thu ngân bếp, chỉ nhà hàng
        m.put("f&b", Arrays.asList(
            DASHBOARD, FNB
        ));
        m.put("fnb", Arrays.asList(
            DASHBOARD, FNB
        ));
        m.put("kitchen", Arrays.asList(
            DASHBOARD, FNB
        ));

        // HOUSEKEEPING — buồng phòng
        m.put("housekeeping", Arrays.asList(
            DASHBOARD, HOUSEKEEPING
        ));

        // TOURGUIDE — hướng dẫn viên
        m.put("tourguide", Arrays.asList(
            DASHBOARD, TOUR
        ));

        // CUSTOMER / GUEST — khách hàng không có quyền ops
        m.put("customer", Collections.emptyList());
        m.put("khách", Collections.emptyList());

        ROLE_DEFAULT_PERMISSIONS = Collections.unmodifiableMap(m);
    }

    /**
     * Trả về chuỗi permissions mặc định cho một role name.
     * VD: "DASHBOARD,FNB" cho thu ngân bếp.
     */
    public static String getDefaultPermissionsFor(String roleName) {
        if (roleName == null) return "";
        String lower = roleName.toLowerCase();
        for (Map.Entry<String, List<String>> entry : ROLE_DEFAULT_PERMISSIONS.entrySet()) {
            if (lower.contains(entry.getKey())) {
                return String.join(",", entry.getValue());
            }
        }
        // Default cho role mới không tên quen: chỉ DASHBOARD
        return DASHBOARD;
    }

    /**
     * Trả về toàn bộ keys quyền hợp lệ.
     */
    public static Set<String> getAllPermissions() {
        return PERMISSION_LABELS.keySet();
    }
}
