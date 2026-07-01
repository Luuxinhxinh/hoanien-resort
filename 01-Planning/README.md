# 01-Planning — Kế hoạch Dự án & Phân công Công việc

Thư mục này quản lý toàn bộ tài nguyên liên quan đến việc lập kế hoạch, theo dõi tiến độ và quản lý tác vụ của nhóm **Group 2 — SWP391 (Kawai Retreat Resort & Hub)**.

---

## 1. Cơ cấu Nhóm & Phân chia Vai trò (Team Roles)

| Họ và tên | Vai trò (Role) | Trách nhiệm chính |
|:---|:---|:---|
| **Nguyễn Cường Mạnh** | Project Manager (PM) & FE Dev | Quản lý dự án, theo dõi tiến độ, phát triển giao diện (Frontend) |
| **Đỗ Văn Tiến** | Tech Lead & BE Dev | Thiết kế kiến trúc hệ thống, phát triển APIs (Backend), Security, DB |
| **Trần Hoàng Nam** | Business Analyst (BA) | Khảo sát yêu cầu, viết đặc tả SRS, tài liệu Business Rules |
| **Lê Thị Ngọc** | Quality Assurance (QA) | Lập kịch bản kiểm thử (TDD), thực hiện Test Cases, kiểm thử UI/API |

---

## 2. Kế hoạch Phát triển Sprint (Sprint Backlog & WBS)

Dự án được chia làm 4 Sprint chính:

```mermaid
gantt
    title Kawai Retreat Project Timeline
    dateFormat  YYYY-MM-DD
    section Requirement & Setup
    Phân tích yêu cầu & Thiết kế DB :active, 2026-06-01, 14d
    section Sprint 1: Core Core
    Module 1: Authentication & Staff (WF-01, WF-12) : 2026-06-15, 14d
    section Sprint 2: Booking & FO
    Module 2: Front-Office & POS (WF-02, WF-03, WF-05) : 2026-06-29, 14d
    section Sprint 3: Finance & Tour
    Module 5 & 6: Checkout, Auto Jobs & Tour (WF-04, WF-06, WF-08) : 2026-07-13, 14d
    section Sprint 4: Hardening & QA
    Kiểm thử tích hợp, tối ưu & Deployment : 2026-07-27, 14d
```

### Bảng theo dõi phân rã công việc (Work Breakdown Structure - WBS):

*Xem chi tiết các task cụ thể và trạng thái thực tế tại bảng Trello / Jira của nhóm.*

| Sprint | Hạng mục công việc (Task) | Phân công | Trạng thái |
|:---:|:---|:---:|:---:|
| **Sprint 1** | Cấu hình Spring Security & Phân quyền RBAC | Đỗ Văn Tiến | 🟢 Done |
| **Sprint 1** | Viết Tài liệu IMP cho Module 1 (Auth/Admin) | Cả nhóm | 🟢 Done |
| **Sprint 2** | Giao diện Đặt phòng trực tuyến (Booking Engine) | Nguyễn Cường Mạnh | 🟡 In Progress |
| **Sprint 2** | API tính tiền phòng & Ghi nợ POS về phòng | Đỗ Văn Tiến | 🟡 In Progress |
| **Sprint 3** | Tích hợp cổng thanh toán trực tuyến VNPay | Đỗ Văn Tiến | 🔴 Todo |
| **Sprint 3** | Thiết kế Job kiểm toán đêm (Night Audit) | Đỗ Văn Tiến | 🔴 Todo |
| **Sprint 4** | Kiểm thử tự động Cypress & Viết tài liệu báo cáo | Lê Thị Ngọc | 🔴 Todo |

---

## 3. Quy trình làm việc hàng ngày (Daily Workflow)
1. **Daily Standup:** Mỗi sáng 5-10 phút cập nhật: *Hôm qua làm gì? Hôm nay làm gì? Có blocker nào không?*
2. **Git Flow:**
   - Nhánh `main`: Chỉ chứa code stable đã qua test kỹ càng.
   - Nhánh `dev`: Nhánh tích hợp code của cả nhóm.
   - Nhánh feature: `feature/WF-XX-name` (mỗi người tự tạo từ `dev` để phát triển tính năng riêng).
