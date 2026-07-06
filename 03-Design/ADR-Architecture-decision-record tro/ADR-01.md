# ADR-01 — Kiến trúc MVC và Đặc tả Kỹ thuật Hệ thống Kawai Retreat Resort & Hub

**Status:** `Accepted`  
**Date:** 2026-06-29  
**Deciders:** Nhóm Phát Triển SWP391 - G2, Antigravity (Architect Review)  
**Context tags:** #mvc #layered-architecture #spring-boot #thymeleaf #security #cryptography  
**US References:** `US-001` (Auth), `US-002` (Booking), `US-003` (F&B POS), `US-004` (Tours), `US-005` (Finance)  
**BR References:** `BR-SYS-01`, `BR-SYS-02`, `BR-SYS-04`, `BR-FO-01`, `BR-FO-04`, `BR-FB-01`, `BR-TR-02`, `BR-FIN-01`, `BR-FIN-03`

---

## Context — Bối cảnh

Hệ thống **Kawai Retreat Resort & Hub** là một giải pháp quản lý nghỉ dưỡng tích hợp (PMS, F&B POS, KDS, Tour Booking, AI Face Scan, Finance). Việc triển khai đòi hỏi sự phân tách rạch ròi giữa các module nghiệp vụ và đảm bảo tính nhất quán của dữ liệu. Đồng thời, hệ thống có các ràng buộc kỹ thuật, pháp lý và bảo mật quan trọng:
1. **Ràng buộc Bảo mật & Pháp lý (Nghị định 13/2023/NĐ-CP):** Bắt buộc phải bảo vệ thông tin định danh cá nhân (PII) như CCCD/Hộ chiếu và mật khẩu tài khoản của khách hàng.
2. **Ràng buộc Nghiệp vụ thực tế:** Yêu cầu các tiến trình xử lý tự động chạy ngầm (Night Audit, Cart Lock cleanup, Table Reservation release) và các logic kiểm tra hạn mức thanh toán, chống đặt phòng/tour trùng (Overbooking).
3. **Trải nghiệm Đa Tác Nhân (Multi-role UX):** Hệ thống có 9 vai trò người dùng khác nhau (Admin, Manager, Customer, Guest, Receptionist, Housekeeper, Maintenance Staff, F&B Staff, Tour Guide). Giao diện và quyền truy cập API cần được phân tách chặt chẽ.

---

## Options Considered — Các lựa chọn đã xem xét

Để xây dựng hệ thống này, nhóm phát triển đã cân nhắc hai phương án kiến trúc chính:

| Option | Mô tả | Pros | Cons |
|--------|-------|------|------|
| **Option A (Monolithic MVC)** | Sử dụng **Spring Boot 3.2 + Thymeleaf** làm View Engine kết hợp REST API cho AJAX/Fetch. Tất cả mã nguồn nằm trong cùng một repository và chạy trên cùng một tiến trình JVM. | - Phát triển nhanh, dễ dàng chia sẻ Model.<br>- Tránh được lỗi CORS và các vấn đề xác thực phiên qua nhiều domain.<br>- Tích hợp mượt mà với cấu hình bảo mật Spring Security.<br>- Chỉ có 1 deployable artifact duy nhất. | - Giao diện HTML (Thymeleaf) và Backend Controller bị phụ thuộc chặt chẽ.<br>- Khó nâng cấp công nghệ front-end riêng biệt.<br>- Khó mở rộng tải (scaling) độc lập cho phần giao diện. |
| **Option B (Decoupled SPA + API)** | Tách biệt hoàn toàn Front-end (React/Vue/Next.js) và Back-end REST API (Spring Boot). | - Phân tách giao diện và nghiệp vụ rõ ràng.<br>- Trải nghiệm người dùng mượt mà hơn (Single Page Application).<br>- Dễ dàng tái sử dụng API cho ứng dụng di động sau này. | - Chi phí phát triển và cấu hình ban đầu cao.<br>- Phải xử lý các vấn đề bảo mật phức tạp như CORS, CSRF, và chia sẻ Session/JWT qua nhiều domain khác nhau.<br>- Quản lý nhiều deployable artifacts. |

---

## Decision — Quyết định

Chúng ta chọn **Option A (Monolithic MVC - Spring Boot 3.2.4 & Thymeleaf)** làm kiến trúc chủ đạo cho hệ thống.

---

## Rationale — Lý do

1. **Đồng bộ hóa Codebase Hiện tại:** Codebase của nhóm đã triển khai sẵn cấu trúc Thymeleaf kết hợp với các REST API Controllers để xử lý AJAX động.
2. **Tối ưu hóa Tài nguyên & Thời gian:** Đội ngũ phát triển có thể xây dựng giao diện nhanh chóng bằng cách kế thừa và chia sẻ trực tiếp các JPA Entities, DTOs mà không cần cấu hình API Gateway hoặc cơ chế định danh chéo phức tạp.
3. **Quản lý Role-Based Redirection Dễ dàng:** Spring Security 6 hỗ trợ quản lý cấu hình phân quyền và chuyển hướng người dùng (Redirect) theo Authority trực tiếp từ luồng MVC của Server.

---

## Consequences — Hệ quả

### Positive:
- **Tốc độ phát triển nhanh:** Dễ dàng bind dữ liệu từ Model Spring MVC vào View Thymeleaf.
- **Bảo mật tập trung:** Spring Security bảo vệ toàn bộ ứng dụng ở mức lọc Filter Chain, chống rò rỉ session.
- **Dễ dàng cấu hình AOP Audit:** Sử dụng Spring AOP để ghi nhận log hành vi của người dùng trực tiếp qua request context mà không lo mất đồng bộ.

### Negative (trade-offs accepted):
- Khó tách biệt hoàn toàn công việc của Front-end Developer và Back-end Developer.
- Mã nguồn giao diện (HTML/JS) bị đóng gói chung trong tệp `.jar` của Spring Boot.

### Risks:
- Tải của máy chủ có thể tăng cao khi vừa phải render HTML vừa phải xử lý logic nghiệp vụ nặng (ví dụ: Night Audit, AI Face Scan).
- Rủi ro nghẽn luồng ghi DB khi kiểm tra Cart Lock 15 phút do không dùng Redis.

### Compliance Impact:
- **Nghị định 13/2023/NĐ-CP:** Dữ liệu nhạy cảm (CCCD/Passport) được tự động mã hóa AES-256 thông qua `EncryptionUtils` trước khi lưu vào database. Mật khẩu được mã hóa BCrypt.
- **Nguyên tắc "Quyền được quên":** Hệ thống hỗ trợ ẩn danh hóa thông tin cá nhân khách hàng thành `ANONYMOUS_USER` khi nhận được yêu cầu xóa dữ liệu (Soft Delete), đồng thời giữ nguyên mã hóa đơn để phục vụ kiểm toán tài chính.

### Decisions unlocked (ADR tiếp theo có thể viết):
- ADR-02: Cơ chế tích hợp Python AI Face Scan Service.
- ADR-03: Giải pháp chống Overbooking sử dụng Pessimistic Locking và Database Triggers.

---

## §AI Prompt Constraint ⭐ CASE 2.0 — BẮT BUỘC

> **Đóng góp cốt lõi của CASE 2.0.** Đoạn text dưới đây sẽ được **inject trực tiếp** vào AI prompt khi thực hiện code/refactor các module liên quan.
> Mỗi constraint dưới đây là bắt buộc và phải tuân thủ nghiêm ngặt trong suốt vòng đời dự án.

```
Theo ADR-01:
1. Tầng Model: Định nghĩa các JPA Entities trong package 'com.kawai.models' và Repositories trong 'com.kawai.repositories'. Không được viết logic nghiệp vụ phức tạp trong lớp Entity.
2. Tầng Controller:
   - Các Web Controllers nằm trong 'com.kawai.controllers.web', sử dụng annotation @Controller và TRẢ VỀ VIEW THYMELEAF (String path).
   - Các API Controllers nằm trong 'com.kawai.controllers.api', sử dụng annotation @RestController và TRẢ VỀ JSON/ResponseEntity.
3. Tầng Service:
   - Mọi logic nghiệp vụ, tính toán tiền tệ, và tích hợp VNPay/SendGrid phải nằm trong package 'com.kawai.services.impl' và kế thừa interface tương ứng trong 'com.kawai.services.interfaces'.
   - KHÔNG được gọi trực tiếp Repositories từ View hoặc Controller (trừ các lookup dữ liệu tĩnh đơn giản).
4. Quy tắc An toàn & Bảo mật Dữ liệu:
   - Trước khi lưu thông tin cccd/passport của Customer vào DB, PHẢI gọi EncryptionUtils.encrypt(value) để mã hóa AES-256.
   - Trước khi đọc hoặc hiển thị, PHẢI giải mã bằng EncryptionUtils.decrypt(value).
   - Mọi hàm thay đổi cấu hình hoặc giao dịch tiền phải được đánh dấu @LogActivity để AOP tự động ghi nhận vào Audit Log.
5. Quản lý Phiên & Quyền:
   - Lấy thông tin user hiện tại thông qua đối tượng Principal hoặc SecurityContextHolder.
   - Tuyệt đối KHÔNG hardcode quyền hạn hoặc vai trò của nhân viên trong code. Sử dụng phân quyền tập trung trong SecurityConfig.java.
```

---
*Template version 2.0 — PrivacyOps Architecture Team — Tích hợp CASE 2.0*
*Section đánh dấu ⭐ là bổ sung mới từ CASE 2.0 methodology.*
