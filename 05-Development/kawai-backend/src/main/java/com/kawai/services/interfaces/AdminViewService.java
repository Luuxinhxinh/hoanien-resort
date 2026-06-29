package com.kawai.services.interfaces;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface AdminViewService {

    Map<String, Object> getDashboardMetrics();

    Map<String, List<RoomMock>> getRoomsByFloor();

    List<ActivityMock> getRecentActivities();

    List<CheckoutMock> getCheckouts();

    List<MasterDataColumn> getMasterDataColumns(String tab);

    List<Map<String, String>> getMasterDataRows(String tab);

    Map<String, List<String>> getFormOptions();

    List<AuditLogMock> getAuditLogs();

    List<ReviewMock> getReviews();

    // New methods for audit log filters
    default Set<String> getAuditEmployees() {
        return Set.of();
    }

    default Set<String> getAuditModules() {
        return Set.of();
    }

    // ================= DTOs =================
    @Data
    @AllArgsConstructor
    class MasterDataColumn {
        private String key;
        private String label;
        private String renderType;
    }

    @Data
    @AllArgsConstructor
    class ReviewMock {
        private String id;
        private String guest;
        private String initials;
        private String service;
        private String serviceType;
        private int rating;
        private String text;
        private String date;
        private boolean visible;
        private String hiddenBy;
        private String hiddenDate;
        private String hiddenReason;

        public String getServiceTypeBg() {
            return switch (serviceType) {
                case "Room" -> "rgba(61,74,46,0.15)";
                case "Dish" -> "rgba(122,78,26,0.15)";
                case "Tour" -> "rgba(45,107,122,0.15)";
                default -> "rgba(0,0,0,0.07)";
            };
        }

        public String getServiceTypeColor() {
            return switch (serviceType) {
                case "Room" -> "#3D4A2E";
                case "Dish" -> "#7A4E1A";
                case "Tour" -> "#1D5B6A";
                default -> "#2C2A1E";
            };
        }

        public String getServiceTypeLabel() {
            return switch (serviceType) {
                case "Room" -> "Phòng";
                case "Dish" -> "Món ăn";
                case "Tour" -> "Tour";
                default -> serviceType;
            };
        }
    }

        @Data
    class RoomMock {
        private String roomNumber;
        private String status;
        private String issueDescription;
        private String roomCategory;
        private String guestName;
        private String guestRequests;
        private int currentGuests;

        public RoomMock(String roomNumber, String status, String issueDescription, String roomCategory, String guestName, String guestRequests, int currentGuests) {
            this.roomNumber = roomNumber;
            this.status = status;
            this.issueDescription = issueDescription;
            this.roomCategory = roomCategory;
            this.guestName = guestName;
            this.guestRequests = guestRequests;
            this.currentGuests = currentGuests;
        }

        public RoomMock(String roomNumber, String status) {
            this(roomNumber, status, "", "Standard", "", "", 0);
        }

        public RoomMock(String roomNumber, String status, String issueDescription) {
            this(roomNumber, status, issueDescription, "Standard", "", "", 0);
        }

        public String getBgColor() {
            return switch (status) {
                case "vacant" -> "#E5E7EB";
                case "occupied" -> "#DBEAFE";
                case "dirty" -> "#FEF3C7";
                case "broken" -> "#FEE2E2";
                default -> "#E5E7EB";
            };
        }

        public String getDotColor() {
            return switch (status) {
                case "vacant" -> "#9CA3AF";
                case "occupied" -> "#3B82F6";
                case "dirty" -> "#F59E0B";
                case "broken" -> "#EF4444";
                default -> "#9CA3AF";
            };
        }

        public String getTextColor() {
            return switch (status) {
                case "vacant" -> "#374151";
                case "occupied" -> "#1E40AF";
                case "dirty" -> "#92400E";
                case "broken" -> "#991B1B";
                default -> "#374151";
            };
        }

        public String getStatusLabel() {
            return switch (status) {
                case "vacant" -> "Trống";
                case "occupied" -> "Có Khách";
                case "dirty" -> "Bẩn";
                case "broken" -> "HỎNG";
                default -> "Trống";
            };
        }
    }

    @Data
    @AllArgsConstructor
    class ActivityMock {
        private String time;
        private String initials;
        private String name;
        private String role;
        private String action;
        private String badgeLabel;

        public String getAvatarBg() {
            return switch (role) {
                case "Receptionist" -> "#2D6B7A";
                case "F&B" -> "#7A4E1A";
                case "Housekeeping" -> "#5A3A6B";
                case "Manager" -> "#8B7020";
                default -> "#3A3526";
            };
        }

        public String getRoleBadgeBg() {
            return switch (role) {
                case "Receptionist" -> "rgba(45,107,122,0.15)";
                case "F&B" -> "rgba(122,78,26,0.15)";
                case "Housekeeping" -> "rgba(90,58,107,0.15)";
                case "Manager" -> "rgba(139,112,32,0.20)";
                default -> "rgba(44,42,30,0.15)";
            };
        }

        public String getRoleBadgeColor() {
            return switch (role) {
                case "Receptionist" -> "#1D5B6A";
                case "F&B" -> "#7A4E1A";
                case "Housekeeping" -> "#5A3A6B";
                case "Manager" -> "#7A6010";
                default -> "#2C2A1E";
            };
        }
    }

    @Data
    @AllArgsConstructor
    class CheckoutMock {
        private String roomNumber;
        private String guestName;
        private boolean paid;
    }

    @Data
    class AuditLogMock {
        private String time;
        private String name;
        private String initials;
        private String role;
        private String action;
        private List<String> modules;
        private String ip;
        private String severity;
        private Long recordId;
        private String rawTableName;
        private String date; // YYYY-MM-DD format for filtering

        public AuditLogMock(String time, String name, String initials, String role, String action,
                List<String> modules, String ip, String severity, Long recordId,
                String rawTableName, String date) {
            this.time = time;
            this.name = name;
            this.initials = initials;
            this.role = role;
            this.action = action;
            this.modules = modules;
            this.ip = ip;
            this.severity = severity;
            this.recordId = recordId;
            this.rawTableName = rawTableName;
            this.date = date;
        }
    }
}