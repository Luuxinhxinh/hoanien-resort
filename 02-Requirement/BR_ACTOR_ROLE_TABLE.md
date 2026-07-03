# BẢNG QUY TẮC KINH DOANH & PHÂN QUYỀN — KAWAI RESORT

## CHANGELOG
| Ngày | Người thực hiện | Nội dung thay đổi |
|---|---|---|
| 2026-06-11 | Antigravity | Xác nhận và đồng bộ các Business Rules với luồng phát triển Hybrid Organization |
| 2026-06-09 | Antigravity | Khởi tạo bảng Quy tắc kinh doanh & Phân quyền |

---

## 1. Bộ Mã Quy Tắc Hệ Thống (System Prefix)

| Tiền tố | Bộ phận | Module |
|---------|--------|--------|
| `BR-SYS` | Quy tắc chung toàn hệ thống (Bảo mật, Tài khoản, Dữ liệu) | MOD1 |
| `BR-FO` | Quy tắc Tiền sảnh & Lưu trú (Front Office / Booking) | MOD2 |
| `BR-FB` | Quy tắc Nhà hàng & Dịch vụ phòng (F&B / POS) | MOD3 |
| `BR-TR` | Quy tắc Lữ hành & Phản hồi (Tour / Feedback) | MOD4 |
| `BR-HK` | Quy tắc Buồng phòng & Bảo trì (Housekeeping / Maintenance) | MOD2 |
| `BR-FIN` | Quy tắc Tài chính, Hóa đơn & Kiểm toán (Finance / Night Audit) | MOD5 |

---

## 2. Các Bảng Quy Tắc Kinh Doanh Chi Tiết

### Bảng 1: Quy tắc Hệ thống, Bảo mật & Dữ liệu gốc (Module 1)

| Mã BR | Tên Quy Tắc | Mô Tả Chi Tiết / Ràng Buộc Kỹ Thuật | Tác Nhân |
|-------|------------|--------------------------------------|----------|
| BR-SYS-01 | Mã hóa dữ liệu nhạy cảm | Toàn bộ thông tin định danh cá nhân (CCCD, Hộ chiếu) và mật khẩu bắt buộc phải mã hóa một chiều hoặc đối xứng (AES-256) trước khi lưu xuống DB. | Admin, System |
| BR-SYS-02 | Khóa tài khoản (Brute Force) | Tài khoản nhập sai mật khẩu quá 05 lần liên tiếp sẽ bị khóa tự động trong 15 phút. Mã OTP/Email Code có hiệu lực trong 03 phút. | Toàn bộ Actor |
| BR-SYS-03 | Thời gian hết hạn phiên | Hệ thống tự động đăng xuất tài khoản (Session Timeout) sau 15 phút không có tương tác đối với tài khoản nhân viên (để bảo vệ quầy). | Nhân viên nội bộ |
| BR-SYS-04 | Bắt buộc ghi Audit Log | Mọi thao tác thuộc nhóm (Hủy booking, Sửa hóa đơn, Đổi quyền, Thay đổi bảng giá) bắt buộc phải ghi lại: Ai làm, Làm lúc nào, Giá trị cũ, Giá trị mới. Không ai có quyền xóa log này. | Admin, System |
| BR-SYS-05 | Quyền được quên (Ẩn danh) | Khi khách yêu cầu xóa thông tin, hệ thống thực hiện Soft Delete bằng cách chuyển thông tin cá nhân thành chuỗi vô danh `ANONYMOUS_USER`, nhưng giữ nguyên ID phòng, mã hóa đơn và số tiền giao dịch. | Customer, Admin |
| BR-SYS-06 | Phân quyền truy cập RBAC | Mỗi API endpoint phải kiểm tra Role của người gọi. Nhân viên chỉ truy cập được chức năng thuộc Role được cấp. Mọi request không có JWT/Session hợp lệ đều bị trả về 401. | System |
| BR-SYS-07 | Độ mạnh mật khẩu | Mật khẩu phải có tối thiểu 8 ký tự, chứa ít nhất 1 chữ hoa, 1 chữ thường và 1 chữ số. Hệ thống không cho phép đặt lại mật khẩu trùng với mật khẩu cũ. | Toàn bộ Actor |

### Bảng 2: Quy tắc Đặt phòng & Tiền sảnh (Module 2)

| Mã BR | Tên Quy Tắc | Mô Tả Chi Tiết / Ràng Buộc Kỹ Thuật | Tác Nhân |
|-------|------------|--------------------------------------|----------|
| BR-FO-01 | Chống đặt trùng phòng | Áp dụng Pessimistic Locking (Khóa bi quan) khi khách nhấn "Đặt phòng". Đảm bảo một mã phòng vật lý không được cấp cho 2 đơn đặt có khoảng thời gian [Checkin_1, Checkout_1] giao với [Checkin_2, Checkout_2]. | Customer, Lễ tân |
| BR-FO-02 | Ràng buộc đặt cọc phòng | Đơn đặt phòng trực tuyến của khách chỉ giữ trạng thái "Tạm giữ" trong vòng 2 phút. Nếu không nhận được webhook thanh toán cọc từ Stripe/VNPay, hệ thống tự động hủy đơn và giải phóng phòng. | Customer, System |
| BR-FO-03 | Ràng buộc tuổi Check-in | Người thực hiện thủ tục Check-in đại diện phòng phải từ 18 tuổi trở lên và có giấy tờ định danh hợp lệ (CCCD/Hộ chiếu). | Lễ tân |
| BR-FO-04 | Luân chuyển trạng thái phòng | Trạng thái phòng phải tuân thủ nghiêm ngặt vòng đời: Phòng khách Check-out tự động chuyển sang Trống/Bẩn → Nhân viên dọn xong mới chuyển sang Trống/Sạch → Lễ tân chỉ được Check-in khách vào phòng Trống/Sạch. | Lễ tân, Buồng phòng |
| BR-FO-05 | Ưu tiên dọn phòng (Rush Room) | Khi Lễ tân bật nhãn Rush Room cho phòng check-in sớm, hệ thống phải đẩy phòng đó lên đầu danh sách công việc của nhân viên buồng phòng thuộc tầng/khu vực đó và phát thông báo Real-time. | Lễ tân, Buồng phòng |
| BR-FO-06 | Hạn mức chi tiêu phòng (Credit Limit) | Khi Check-in, Lễ tân thiết lập Credit Limit cho phòng (mặc định = tiền cọc). Tổng nợ Folio phát sinh từ POS, Tour, Mini-bar không được vượt quá Credit Limit. Vượt hạn mức → hệ thống chặn Post to Room và yêu cầu khách nạp thêm. | Lễ tân, System |
| BR-FO-07 | Giới hạn số người trong phòng | Mỗi hạng phòng có số khách tối đa (Max Occupancy). Lễ tân không thể gán số khách + người đi cùng (Dependents) vượt quá giới hạn này khi Check-in. | Lễ tân |
| BR-FO-08 | Khai báo tạm trú | Bắt buộc thu thập đủ thông tin hành khách (CMND/CCCD/Hộ chiếu, quê quán) lúc Check-in để đồng bộ và xuất báo cáo lưu trú cho cơ quan Công an theo Luật Cư trú 2020. | Lễ tân |
| BR-FO-09 | Phụ phí Early Check-in / Late Check-out | Khách nhận phòng sớm hoặc trả phòng muộn so với khung giờ quy định (14:00 - 12:00) sẽ bị tính phụ phí theo tỷ lệ phần trăm giá phòng. Hệ thống tự động tính và cộng phụ phí vào Folio. | Lễ tân, System |

### Bảng 3: Quy tắc POS Nhà hàng & F&B (Module 3)

| Mã BR | Tên Quy Tắc | Mô Tả Chi Tiết / Ràng Buộc Kỹ Thuật | Tác Nhân |
|-------|------------|--------------------------------------|----------|
| BR-FB-01 | Điều kiện Post to Room | Chỉ cho phép ký gửi hóa đơn nhà hàng/dịch vụ phòng vào phòng nếu: (1) Phòng đang ở trạng thái Có khách (Occupied), (2) Họ tên khách khớp với dữ liệu đăng ký lúc Check-in, và (3) Tổng nợ sau khi cộng không vượt Credit Limit (BR-FO-06). | Nhân viên POS, Lễ tân |
| BR-FB-02 | Đồng bộ trạng thái hết món | Khi Bếp nhấn "Hết món" trên màn hình KDS, trạng thái món ăn trong DB phải chuyển về `Available = False`, lập tức ẩn/khóa trên giao diện E-Menu của Khách và POS của Phục vụ. | Bếp, Phục vụ, Khách |
| BR-FB-03 | Giới hạn thời gian đặt bàn | Khách đặt bàn trước qua hệ thống được giữ bàn tối đa 30 phút so với giờ hẹn. Quá thời gian này, hệ thống tự động chuyển trạng thái bàn sang "Hủy do quá hạn" và giải phóng sơ đồ bàn. | Customer, Phục vụ |
| BR-FB-04 | Không chỉnh sửa order đã vào bếp | Sau khi order được gửi sang KDS và bếp đã nhận (trạng thái ≥ PREPARING), nhân viên POS không được sửa hoặc xóa món đó. Muốn hủy phải tạo phiếu "Hủy món" riêng với lý do, ghi vào Audit Log. | Phục vụ, Bếp |
| BR-FB-05 | Phí dịch vụ Room Service | Đơn hàng Room Service (giao lên phòng) tự động cộng thêm phí phục vụ 10% so với giá Dine-In. Phí này phải hiển thị rõ ràng trước khi khách xác nhận đặt. | Customer, System |

### Bảng 4: Quy tắc Đặt Tour & Phản hồi (Module 4)

| Mã BR | Tên Quy Tắc | Mô Tả Chi Tiết / Ràng Buộc Kỹ Thuật | Tác Nhân |
|-------|------------|--------------------------------------|----------|
| BR-TR-01 | Chống Double-booking Tour | Mỗi gói tour đều có giới hạn số lượng chỗ (Slot). Khi số lượng chỗ còn lại = 0, hệ thống lập tức đóng luồng đặt tour. Tiến trình xử lý đặt tour phải nằm trong một Database Transaction để tránh bán quá tải (Overbooking). | Customer, System |
| BR-TR-02 | Xác thực AI Face Scan | Ảnh quét khuôn mặt lúc lên xe của khách phải có độ trùng khớp ≥ 85% so với ảnh chân dung khách đã tải lên hệ thống lúc đặt tour thì hệ thống mới xác nhận Checked-in cho hành khách đó. | Hướng dẫn viên |
| BR-TR-03 | Thời hạn viết đánh giá | Khách hàng chỉ được quyền chấm điểm sao và viết nhận xét dịch vụ trong vòng 07 ngày kể từ ngày kết thúc tour hoặc ngày Check-out phòng. Quá 7 ngày, tính năng đánh giá của booking đó sẽ bị đóng. | Customer |
| BR-TR-04 | Minh bạch kiểm duyệt | Admin không được phép sửa nội dung đánh giá của khách. Admin chỉ có quyền Ẩn/Hiện và bắt buộc phải nhập lý do kiểm duyệt (Ví dụ: Chứa từ ngữ thô tục, spam quảng cáo). Lý do này sẽ lưu vào Audit Trail. | Admin |
| BR-TR-05 | Hủy tour do thời tiết / sự cố | Nếu tour bị hủy bởi phía Resort (sự cố, thời tiết xấu), khách được hoàn tiền 100% hoặc đổi sang ngày khác miễn phí. Nếu khách tự hủy trong vòng 24h trước giờ tour, mất 50% phí đặt cọc tour. | Admin, Tour Guide, Customer |
| BR-TR-06 | Số khách tối thiểu khởi hành | Mỗi gói tour có ngưỡng số khách tối thiểu (Minimum Pax). Nếu 24h trước giờ khởi hành mà chưa đủ số lượng, hệ thống cảnh báo Admin để quyết định: chạy tour lỗ hoặc hủy + hoàn tiền tất cả khách. | Admin, System |

### Bảng 5: Quy tắc Buồng phòng & Bảo trì (Module 2 — Housekeeping / Maintenance)

| Mã BR | Tên Quy Tắc | Mô Tả Chi Tiết / Ràng Buộc Kỹ Thuật | Tác Nhân |
|-------|------------|--------------------------------------|----------|
| BR-HK-01 | Ghi nhận Mini-bar vào Folio | Khi nhân viên buồng phòng kiểm tra phòng và phát hiện khách đã sử dụng đồ uống/thực phẩm có phí trong Mini-bar, phải nhập số lượng tiêu dùng vào hệ thống. Hệ thống tự động cộng chi phí tương ứng vào Folio phòng. | Buồng phòng, System |
| BR-HK-02 | Báo cáo hư hỏng tài sản | Nếu nhân viên buồng phòng phát hiện tài sản phòng bị hỏng (khăn, remote, ly cốc), phải tạo phiếu ghi nhận hư hỏng kèm ảnh chụp. Chi phí đền bù (nếu có) sẽ được hệ thống tự động đẩy vào Folio phòng. | Buồng phòng, System |
| BR-HK-03 | Chặn Check-in phòng đang bảo trì | Phòng có trạng thái `MAINTENANCE` (đang sửa chữa) bị khóa hoàn toàn khỏi luồng đặt phòng và Check-in. Chỉ khi nhân viên kỹ thuật xác nhận hoàn thành và phòng chuyển về `AVAILABLE`, phòng mới xuất hiện lại trên hệ thống. | Maintenance, Lễ tân |
| BR-HK-04 | Phân công dọn phòng theo tầng | Mỗi nhân viên buồng phòng được phân công phụ trách một tầng/khu vực cụ thể. Danh sách phòng cần dọn chỉ hiển thị các phòng thuộc khu vực được gán, tránh xung đột công việc giữa các nhân viên. | Buồng phòng, Admin |

### Bảng 6: Quy tắc Hóa đơn, Thanh toán & Tài chính (Module 5)

| Mã BR | Tên Quy Tắc | Mô Tả Chi Tiết / Ràng Buộc Kỹ Thuật | Tác Nhân |
|-------|------------|--------------------------------------|----------|
| BR-FIN-01 | Ràng buộc Tất toán Folio | Hệ thống tuyệt đối không cho phép Lễ tân đóng lệnh Check-out nếu số dư công nợ (Balance) của Hóa đơn tổng phòng ≠ 0. Toàn bộ chi phí phòng, POS, Tour, Mini-bar đính kèm phải được chuyển về trạng thái Đã thanh toán. | Lễ tân, System |
| BR-FIN-02 | Chính sách Hủy phòng & Hoàn tiền | Hủy trước ngày Check-in > 48 giờ: Hệ thống tự động tạo lệnh hoàn tiền cọc 100%. Hủy trong vòng ≤ 48 giờ hoặc Không đến (No-show): Tiền cọc không được hoàn lại và tự động hạch toán vào doanh thu phạt của Resort. | Customer, Lễ tân |
| BR-FIN-03 | Thời gian chốt Kiểm toán đêm | Cron Job tự động chạy vào lúc 02:00 AM hàng ngày. Hệ thống sẽ khóa toàn bộ sổ sách giao dịch của ngày hôm trước, tự động tính toán và cộng phí phòng của đêm đó vào Folio khách đang lưu trú, và chuyển ngày vận hành hệ thống. | System |
| BR-FIN-04 | Phân loại doanh thu USALI | Doanh thu kết xuất ra báo cáo Excel/PDF bắt buộc phải bóc tách riêng biệt thành 3 mã doanh thu nguồn: `REV-ROOM` (Tiền phòng, minibar), `REV-FB` (Nhà hàng, Room Service) và `REV-TOUR` (Gói lữ hành). | Manager, System |
| BR-FIN-05 | Làm tròn tiền tệ (Rounding) | Mọi phép tính tiền trong hệ thống phải sử dụng kiểu dữ liệu `BigDecimal` với chế độ làm tròn `HALF_UP`, đơn vị nhỏ nhất là 1 VNĐ. Nghiêm cấm dùng `float` hoặc `double` cho trường tiền tệ để tránh sai lệch tài chính. | System |
| BR-FIN-06 | Thanh toán nhiều hình thức | Một lần tất toán Folio cho phép kết hợp nhiều hình thức thanh toán (Tiền mặt + Thẻ + Chuyển khoản). Hệ thống ghi nhận từng khoản (Payment Split) và tổng các khoản phải bằng chính xác số dư Folio. | Lễ tân |
| BR-FIN-07 | Khóa sổ sau Night Audit | Sau khi tiến trình Night Audit hoàn tất chốt ngày, toàn bộ dữ liệu giao dịch (hóa đơn, booking, folio) của ngày hôm đó sẽ bị khóa cứng. Tuyệt đối không ai (kể cả Admin) được sửa đổi hoặc xóa dữ liệu đã chốt để ngăn chặn gian lận. | Admin, System |

---

## 3. Bảng Tác Nhân & Phân Quyền (Actor / Role Matrix)

| ROLE ID | Tên Role | Mô tả | UC được phép |
|---------|---------|-------|-------------|
| ROLE-01 | Guest | Khách vãng lai chưa đăng nhập | UC09, UC19 (chỉ xem) |
| ROLE-02 | Customer | Khách đã đăng ký tài khoản | UC01–UC04, UC09–UC10, UC14–UC15, UC19–UC20, UC22 |
| ROLE-03 | Receptionist | Lễ tân tiền sảnh | UC11–UC13, UC25 |
| ROLE-04 | F&B Staff | Nhân viên phục vụ / Thu ngân | UC14, UC16, UC18 |
| ROLE-05 | Kitchen Staff | Nhân viên bếp | UC17 |
| ROLE-06 | Housekeeping | Nhân viên buồng phòng | UC13.2, UC13.4 |
| ROLE-07 | Maintenance | Nhân viên kỹ thuật | UC13.5 |
| ROLE-08 | Tour Guide | Hướng dẫn viên | UC21 |
| ROLE-09 | Admin | Quản trị viên hệ thống | UC05–UC06, UC23 + toàn quyền |
| ROLE-10 | Manager | Quản lý cấp cao | UC26–UC28 |
| ROLE-11 | System | Tác nhân tự động (Cron, Trigger) | UC24 (Night Audit) |

---

## 4. Bảng Mã Lỗi Chuẩn (Error Codes)

| Error Code | HTTP | Mô tả (VI) | BR / UC liên quan |
|-----------|------|-----------|-------------------|
| AUTH-001 | 400 | Dữ liệu đăng ký không hợp lệ | UC01, BR-SYS-07 |
| AUTH-002 | 401 | Sai tên đăng nhập hoặc mật khẩu | UC01 |
| AUTH-003 | 401 | OTP hết hạn hoặc sai | UC02, BR-SYS-02 |
| AUTH-004 | 403 | Không đủ quyền truy cập | BR-SYS-06 |
| AUTH-005 | 429 | Tài khoản bị khóa do nhập sai quá 5 lần | BR-SYS-02 |
| BOOK-001 | 409 | Phòng không còn trống (khoảng ngày bị trùng) | UC10, BR-FO-01 |
| BOOK-002 | 408 | Hết thời gian tạm giữ 2 phút — đơn bị hủy | BR-FO-02 |
| BOOK-003 | 400 | Ngày nhận/trả phòng không hợp lệ | UC10 |
| BOOK-004 | 400 | Hủy trong 48h — không đủ điều kiện hoàn cọc | BR-FIN-02 |
| ROOM-001 | 400 | Phòng đang DIRTY hoặc MAINTENANCE — không thể Check-in | BR-FO-04, BR-HK-03 |
| ROOM-002 | 400 | Vượt số khách tối đa cho hạng phòng | BR-FO-07 |
| POS-001 | 400 | Đơn hàng trống — không có món | UC16 |
| POS-002 | 409 | Món đã hết — không thể order | UC17, BR-FB-02 |
| POS-003 | 400 | Vượt hạn mức tín dụng phòng (Credit Limit) | BR-FO-06, BR-FB-01 |
| POS-004 | 400 | Không thể sửa/xóa — order đã vào bếp | BR-FB-04 |
| TOUR-001 | 409 | Tour đã đủ số lượng khách | BR-TR-01 |
| TOUR-002 | 400 | Hủy tour trong 24h — mất 50% cọc | BR-TR-05 |
| FOLIO-001 | 400 | Hóa đơn chưa thanh toán hết — không thể Check-out | BR-FIN-01 |
| FOLIO-002 | 404 | Không tìm thấy hóa đơn | UC24 |
| SYS-001 | 500 | Lỗi hệ thống nội bộ | — |
| SYS-002 | 503 | Dịch vụ bên thứ ba không khả dụng (VNPay, AI, SendGrid) | — |
