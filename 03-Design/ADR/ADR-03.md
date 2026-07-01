# ADR-03: Cấu hình Spring Security & Phân quyền Hệ thống (RBAC/Permission Gating)

* **Trạng thái:** Confirmed
* **Tác giả:** Nhóm phát triển Kawai Retreat
* **Ngày quyết định:** 2026-07-01
* **Lĩnh vực:** Bảo mật, Phân quyền

---

## 1. Bối cảnh (Context)

Hệ thống Kawai Retreat phục vụ hai nhóm đối tượng tách biệt:
1. **Khách hàng bên ngoài:** Sử dụng website để đặt phòng, đặt dịch vụ và thanh toán qua cổng VNPay. Xác thực qua Google OAuth2 hoặc Email/Mật khẩu truyền thống.
2. **Nhân viên nội bộ:** Sử dụng cổng quản trị (Lễ tân, F&B Staff, Buồng phòng, Quản lý, Admin). Yêu cầu kiểm soát truy cập nghiêm ngặt và hỗ trợ xác thực thiết bị được ủy quyền (Device 2FA).

Nếu chỉ sử dụng phân quyền theo Role cơ bản (`ROLE_ADMIN`, `ROLE_RECEPTIONIST`), hệ thống sẽ mất đi tính linh hoạt khi cần thay đổi quyền hạn cụ thể (ví dụ: cho phép Lễ tân làm thay việc của F&B Staff vào giờ cao điểm). Hơn nữa, việc hardcode role trong Controller endpoints vi phạm nguyên lý thiết kế bền vững.

## 2. Quyết định (Decision)

Chúng tôi quyết định cấu hình kiến trúc Spring Security và phân quyền như sau:

### 2.1 Kiến trúc Xác thực Kép (Dual Authentication Filter Chain)
Cấu hình Spring Security với hai SecurityFilterChain riêng biệt:
1. **Cổng Web Operations (Nhân viên - `/ops-login`, `/admin/**`, `/receptionist/**`):** Sử dụng Stateful Session dựa trên Cookie bảo mật (`HttpOnly`, `Secure`, `SameSite=Strict`).
2. **Cổng API REST (`/api/**`):** Sử dụng Stateless JWT (JSON Web Token) để phục vụ các yêu cầu từ Mobile App (dành cho Tour Guide / Housekeeping Staff) và bên thứ ba.

### 2.2 Phân quyền Granular dựa trên Quyền hạn (Permission-Based Authority Gating)
* Không sử dụng trực tiếp vai trò (Role) để khóa cứng API. Tất cả quyền truy cập được ánh xạ qua danh sách **Permissions** trong `RolePermissionConstants`.
* Chuyển đổi Roles thành danh sách các GrantedAuthority cụ thể tại thời điểm xác thực. Ví dụ, vai trò `RECEPTIONIST` sẽ mang các authority: `ROOM_VIEW`, `CHECKIN_PERFORM`, `CHECKOUT_PERFORM`, `FOLIO_MANAGE`.
* Sử dụng `@EnableMethodSecurity` và áp dụng kiểm tra quyền ở cấp độ method / controller:
  ```java
  @RestController
  @RequestMapping("/api/bookings")
  public class BookingApiController {
      @PreAuthorize("hasAuthority('CHECKIN_PERFORM')")
      @PostMapping("/{id}/checkin")
      public ResponseEntity<?> checkIn(@PathVariable Long id) { ... }
  }
  ```
* Phía giao diện (Thymeleaf), sử dụng thẻ `sec:authorize="hasAuthority('...')"` để ẩn/hiện động các chức năng trên menu điều hướng tương ứng với quyền của nhân viên đăng nhập.

### 2.3 Bảo vệ trước các Lỗ hổng Phổ biến (CORS, CSRF, XSS, Session Fixation)
* **CSRF (Cross-Site Request Forgery):**
  * Kích hoạt bảo vệ CSRF trên cổng Web Operations sử dụng `CookieCsrfTokenRepository.withHttpOnlyFalse()`.
  * Các API Stateless REST sử dụng JWT được tắt CSRF do không lưu session trên cookie.
* **CORS (Cross-Origin Resource Sharing):**
  * CORS được cấu hình chặt chẽ chỉ cho phép các Domain có trong danh sách trắng (Whitelist) cấu hình qua biến môi trường. Tuyệt đối không sử dụng `allowedOrigins("*")` trong môi trường Production.
* **Xác thực mật khẩu:** Sử dụng `BCryptPasswordEncoder(strength = 12)` để băm (hash) mật khẩu nhân viên, đảm bảo khả năng chống tấn công brute-force.

### 2.4 Device Authorization 2FA cho Nhân viên
* Triển khai bộ lọc Custom Filter (`DeviceAuthorizationFilter`) đứng trước `UsernamePasswordAuthenticationFilter` đối với cổng `/ops-login`.
* Bộ lọc kiểm tra mã thiết bị gửi lên (`device_code`) và đối chiếu với bảng `AuthorizedDevice`. Nếu thiết bị chưa được Admin duyệt (`is_approved = false`), yêu cầu đăng nhập lập tức bị từ chối và chuyển hướng sang trang yêu cầu cấp quyền thiết bị.

## 3. Hệ quả (Consequences)

### Tích cực (Pros)
* **Tính linh hoạt cực cao:** Có thể thay đổi quyền hạn của một vai trò trong database (Role-Permission Mapping) mà không cần thay đổi hay build lại mã nguồn.
* **An toàn cao:** Tách biệt môi trường API và Web MVC giúp cô lập các rủi ro bảo mật liên quan đến rò rỉ session hoặc token.
* **Không hardcode role:** Codebase sạch và dễ bảo trì hơn theo tiêu chuẩn phát triển phần mềm hiện đại.

### Hạn chế (Cons)
* **Phức tạp khi cấu hình:** Tích hợp đồng thời Stateful và Stateless trong Spring Security đòi hỏi sự tỉ mỉ trong việc định tuyến và cấu hình Filter Chain, dễ dẫn đến lỗi 403 / 401 nếu cấu hình sai thứ tự bộ lọc.
* **Quản lý token:** Đòi hỏi phía Client (Mobile App) phải xử lý lưu trữ token an toàn trong Secure Storage để tránh bị đánh cắp.
