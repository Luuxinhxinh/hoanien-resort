---
name: system-audit-protocol
description: Giao thức rà soát toàn diện hệ thống (System Audit / Global Health Check). Kích hoạt khi user yêu cầu "khám bệnh", rà soát lỗi bảo mật, nợ kỹ thuật (technical debt), hiệu năng, hoặc chuẩn kiến trúc trên diện rộng.
---

# Giao Thức Rà Soát Hệ Thống Tổng Thể

## 1. Mục tiêu (Read-Only)
- Skill này thuần túy là **Đọc và Phân tích (Read-Only)**. Tuyệt đối không được tự ý sửa code trong lúc đang rà soát trừ khi user yêu cầu "vừa khám vừa chữa" (gộp chung với `code-cleanup-protocol`).
- Kết quả đầu ra PHẢI là một file Artifact Markdown có tên `System_Audit_Report.md`.

## 2. Các Hạng Mục Cần Quét (Audit Checklist)
Khi chạy Audit, sử dụng công cụ `grep_search` kết hợp regex (cờ `IsRegex: true`) để rà soát nhanh toàn dự án theo các nhóm tiêu chí sau:

### 2.1. Bảo mật & Phân quyền (Security & Permissions)
- **Thiếu phân quyền:** Tìm các Controller API nhưng quên gắn annotation `@PreAuthorize` hoặc cấu hình sai trong `SecurityConfig`.
- **Hardcode nhạy cảm:** Quét từ khóa `password`, `secret`, `token`, `key` trong toàn bộ `.java` và `.properties` để tìm các chỗ hardcode.
- **Rò rỉ PII qua Log:** Tìm lệnh `log.trace` hoặc `log.debug` xem có in ra object chứa tham số cá nhân không.
- **Lỗ hổng XSS (Frontend):** Quét các thẻ HTML có gắn `onclick` hoặc nối chuỗi JS trực tiếp (ví dụ: `onclick="...${var}..."`) mà chưa dùng hàm escape của Thymeleaf.

### 2.2. Chuẩn Kiến Trúc (Architecture Health)
- **Controller bẩn (Fat Controller):** Quét bằng mắt các Controller. Nếu Controller chứa logic tính toán nghiệp vụ (if/else phức tạp, vòng lặp) thay vì gọi `Service` → Đánh dấu là lỗi chuẩn kiến trúc.
- **Bypass Service:** Controller gọi trực tiếp `Repository` (`@Autowired ...Repository`).
- **Entity Leak:** Lộ trực tiếp Entity ra View/Response thay vì thông qua DTO.

### 2.3. Nợ Kỹ Thuật, File Rác & Hiệu Năng (Tech Debt & Missing Impl)
- **File/Class không sử dụng (Dead Files):** Dùng `grep_search` để rà quét các class (đặc biệt là DTO, Repository, Service) xem có được `@Autowired` hoặc khởi tạo ở đâu không. Nếu không ai gọi -> Đánh dấu là file rác cần xóa.
- **Thiếu Code (Missing Implementation):** Tìm các `// TODO`, `// FIXME`, các hàm rỗng (empty body), hoặc các hàm chỉ có `throw new UnsupportedOperationException()`.
- **Hàm/Class quá khổ:** Tìm các class quá lớn (trên 1000 dòng) hoặc method quá phức tạp (trên 100 dòng).
- **Vòng lặp chọc Database:** Dùng `grep_search` để rà soát các đoạn code gọi `repository.find...()` bên trong vòng lặp `for` hoặc `while`. Đây là Anti-pattern gây nghẽn cổ chai.
- **N+1 Query:** Quét các cấu trúc `@OneToMany` hoặc `@ManyToOne` không dùng `fetch = FetchType.LAZY` hoặc thiếu `@EntityGraph` (nếu cần load eager).

## 3. Định dạng Báo Cáo (`System_Audit_Report.md`)
Trình bày kết quả theo mức độ nghiêm trọng:
- 🔴 **HIGH (Nghiêm trọng):** Các lỗi bảo mật, rò rỉ dữ liệu, N+1 query nặng, logic hổng.
- 🟡 **MEDIUM (Cần xử lý):** Sai chuẩn kiến trúc, chọc DB trong vòng lặp, hardcode thông tin.
- 🟢 **LOW (Khuyến nghị):** Code lặp lại, hàm dài, unused imports, tên biến/hàm khó hiểu.

*(Cuối báo cáo, hãy gợi ý user dùng lệnh `/clean` hoặc áp dụng `code-cleanup-protocol` để bắt tay vào sửa các lỗi này).*
