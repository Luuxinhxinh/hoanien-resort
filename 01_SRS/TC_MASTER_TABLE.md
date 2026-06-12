# BẢNG TEST CASE TỔNG HỢP — KAWAI RESORT

## CHANGELOG
| Ngày | Người thực hiện | Nội dung thay đổi |
|---|---|---|
| 2026-06-11 | Antigravity | Xác nhận và đồng bộ các Test Cases tổng hợp với luồng phát triển Hybrid Organization |
| 2026-06-09 | Antigravity | Khởi tạo bảng danh mục Test Cases tổng hợp |
> Đây là bảng gốc chứa toàn bộ Test Case ID. Các tài liệu khác (Traceability Matrix, TDD Spec, EDS Spec) sẽ tham chiếu ID từ bảng này.

---

## 🔴 MOD1: XÁC THỰC & TÀI KHOẢN

| TC ID     | UC tham chiếu | Loại Test   | Mô tả kịch bản                                                         | Severity | Trạng thái |
| -----------| ---------------| -------------| ------------------------------------------------------------------------| ----------| ------------|
| TC-M1-001 | UC01.1        | Unit        | Đăng ký thành công — mật khẩu được hash BCrypt, Role mặc định CUSTOMER | HIGH     | ⬜          |
| TC-M1-002 | UC01.1        | Unit        | Đăng ký thất bại — username/email đã tồn tại → trả 409                 | MEDIUM   | ⬜          |
| TC-M1-003 | UC01.1        | Unit        | Đăng ký thất bại — thiếu field bắt buộc → trả 400                      | MEDIUM   | ⬜          |
| TC-M1-004 | UC01.2        | Unit        | Đăng nhập thành công — trả về JWT/Session hợp lệ                       | HIGH     | ⬜          |
| TC-M1-005 | UC01.2        | Unit        | Đăng nhập thất bại — sai mật khẩu → trả 401                            | HIGH     | ⬜          |
| TC-M1-006 | UC01.2        | Security    | Brute-force login — khóa tài khoản sau 5 lần sai liên tiếp             | CRITICAL | ⬜          |
| TC-M1-007 | UC02          | Unit        | Gửi OTP thành công qua email/SMS                                       | HIGH     | ⬜          |
| TC-M1-008 | UC02          | Unit        | Xác thực OTP đúng → cho phép đăng nhập                                 | HIGH     | ⬜          |
| TC-M1-009 | UC02          | Unit        | OTP hết hạn hoặc sai → từ chối, trả AUTH-003                           | MEDIUM   | ⬜          |
| TC-M1-010 | UC03          | Unit        | Gửi link reset mật khẩu qua email — Token có thời hạn                  | MEDIUM   | ⬜          |
| TC-M1-011 | UC03          | Unit        | Đặt lại mật khẩu với Token hợp lệ → cập nhật hash mới                  | MEDIUM   | ⬜          |
| TC-M1-012 | UC03          | Unit        | Token hết hạn hoặc sai → từ chối                                       | MEDIUM   | ⬜          |
| TC-M1-013 | UC04          | Unit        | Cập nhật hồ sơ thành công — CCCD được mã hóa AES-256                   | HIGH     | ⬜          |
| TC-M1-014 | UC04          | Security    | CCCD/Hộ chiếu không lưu plaintext trong DB                             | CRITICAL | ⬜          |
| TC-M1-015 | UC05.1        | Unit        | Admin tạo tài khoản nhân viên với Role chỉ định                        | HIGH     | ⬜          |
| TC-M1-016 | UC05.1        | Security    | Non-Admin truy cập API quản lý nhân viên → bị chặn 403                 | CRITICAL | ⬜          |
| TC-M1-017 | UC05.2        | Unit        | Audit Log ghi nhận đầy đủ: ai, làm gì, lúc nào                         | MEDIUM   | ⬜          |
| TC-M1-018 | UC06.1        | Unit        | Admin CRUD hạng phòng / loại tour / menu thành công                    | MEDIUM   | ⬜          |
| TC-M1-019 | UC06.2        | Unit        | Cấu hình giá phòng theo ngày — giá đúng khi tìm kiếm                   | MEDIUM   | ⬜          |
| TC-M1-020 | UC07          | Unit        | Ẩn danh hóa PII — hash CCCD/Tên/SĐT, giữ nguyên booking history        | HIGH     | ⬜          |
| TC-M1-021 | UC07          | Integration | Sau khi ẩn danh, login bằng tài khoản cũ → thất bại                    | HIGH     | ⬜          |
| TC-M1-022 | UC08          | Unit        | Session hết hạn → redirect về trang login                              | LOW      | ⬜          |

---

## 🔵 MOD2: QUẢN LÝ PHÒNG & LỄ TÂN

| TC ID     | UC tham chiếu | Loại Test   | Mô tả kịch bản                                                              | Severity | Trạng thái |
| -----------| ---------------| -------------| -----------------------------------------------------------------------------| ----------| ------------|
| TC-M2-001 | UC09          | Unit        | Tìm phòng trống đúng theo ngày nhận/trả                                     | HIGH     | ⬜          |
| TC-M2-002 | UC09          | Unit        | Không có phòng trống → trả danh sách rỗng                                   | MEDIUM   | ⬜          |
| TC-M2-003 | UC10.1        | Unit        | Đặt phòng thành công — tạo Booking + Folio trống                            | CRITICAL | ⬜          |
| TC-M2-004 | UC10.1        | Concurrency | 2 user đặt cùng phòng cùng lúc → 1 thành công, 1 trả 409 (Pessimistic Lock) | CRITICAL | ⬜          |
| TC-M2-005 | UC10.1        | Integration | Thanh toán cọc VNPay → callback xác nhận → trạng thái Booking = CONFIRMED   | CRITICAL | ⬜          |
| TC-M2-006 | UC10.1        | Unit        | Hủy trước 48h → hoàn 100% cọc (BR-FIN-02)                                   | HIGH     | ⬜          |
| TC-M2-007 | UC10.1        | Unit        | Hủy trong 48h → tịch thu cọc (BR-FIN-02)                                    | HIGH     | ⬜          |
| TC-M2-008 | UC10.2        | Unit        | Áp mã khuyến mãi hợp lệ → giảm giá đúng                                     | MEDIUM   | ⬜          |
| TC-M2-009 | UC10.2        | Unit        | Mã khuyến mãi hết hạn / sai → từ chối                                       | MEDIUM   | ⬜          |
| TC-M2-010 | UC11          | Unit        | Dashboard trả đúng danh sách phòng + trạng thái thời gian thực              | HIGH     | ⬜          |
| TC-M2-011 | UC12.1        | Unit        | Check-in thành công — phòng chuyển OCCUPIED, tạo Folio                      | CRITICAL | ⬜          |
| TC-M2-012 | UC12.1        | Unit        | Check-in thất bại — phòng đang DIRTY hoặc MAINTENANCE → báo lỗi             | HIGH     | ⬜          |
| TC-M2-013 | UC12.2        | Unit        | Ủy quyền hạn mức — cập nhật Credit Limit thành công                         | MEDIUM   | ⬜          |
| TC-M2-014 | UC12.3        | Unit        | Đổi phòng — chuyển Folio sang phòng mới, phòng cũ → DIRTY                   | HIGH     | ⬜          |
| TC-M2-015 | UC12.4        | Unit        | Nâng cấp Dependent thành Customer — tạo Account mới                         | LOW      | ⬜          |
| TC-M2-016 | UC13.1        | Integration | Check-out → tự động sinh yêu cầu dọn phòng (Trigger DB)                     | HIGH     | ⬜          |
| TC-M2-017 | UC13.2        | Unit        | Housekeeping cập nhật phòng DIRTY → CLEAN                                   | HIGH     | ⬜          |
| TC-M2-018 | UC13.3        | Unit        | Lễ tân xem danh sách yêu cầu dọn/sửa phòng                                  | MEDIUM   | ⬜          |
| TC-M2-019 | UC13.4        | Unit        | Housekeeping tạo phiếu sửa chữa → phòng chuyển MAINTENANCE                  | MEDIUM   | ⬜          |
| TC-M2-020 | UC13.5        | Unit        | Maintenance hoàn thành → phòng chuyển AVAILABLE                             | MEDIUM   | ⬜          |

---

## 🟡 MOD3: DỊCH VỤ ẨM THỰC & NHÀ HÀNG

| TC ID     | UC tham chiếu | Loại Test   | Mô tả kịch bản                                                   | Severity | Trạng thái |
| -----------| ---------------| -------------| ------------------------------------------------------------------| ----------| ------------|
| TC-M3-001 | UC14          | Unit        | Khách quét QR → hiển thị E-Menu, đặt Room Service thành công     | MEDIUM   | ⬜          |
| TC-M3-002 | UC14          | Unit        | Room Service cho phòng không OCCUPIED → từ chối                  | MEDIUM   | ⬜          |
| TC-M3-003 | UC15          | Unit        | Đặt bàn thành công — bàn chuyển RESERVED                         | MEDIUM   | ⬜          |
| TC-M3-004 | UC15          | Concurrency | 2 khách đặt cùng bàn cùng giờ → 1 thành công, 1 báo lỗi          | HIGH     | ⬜          |
| TC-M3-005 | UC16          | Unit        | POS tạo order Dine-In — ghi đúng bàn, đúng món, đúng giá         | CRITICAL | ⬜          |
| TC-M3-006 | UC16          | Unit        | POS thanh toán tiền mặt — đơn hàng chuyển PAID                   | HIGH     | ⬜          |
| TC-M3-007 | UC17.1        | Unit        | KDS nhận order mới → hiển thị KOT trên màn hình bếp              | HIGH     | ⬜          |
| TC-M3-008 | UC17.1        | Unit        | Bếp cập nhật từng món PREPARING → READY                          | HIGH     | ⬜          |
| TC-M3-009 | UC17.2        | Unit        | Bếp báo hết món → POS/E-Menu tự động khóa món đó                 | HIGH     | ⬜          |
| TC-M3-010 | UC18          | Unit        | Post to Room thành công — ghi nợ vào Folio phòng                 | CRITICAL | ⬜          |
| TC-M3-011 | UC18          | Unit        | Post to Room vượt Credit Limit → từ chối, trả POS-003 (BR-FO-06) | CRITICAL | ⬜          |
| TC-M3-012 | UC18          | Unit        | Post to Room cho phòng không OCCUPIED → từ chối                  | HIGH     | ⬜          |

---

## 🟢 MOD4: QUẢN LÝ LỮ HÀNH & ĐÁNH GIÁ

| TC ID     | UC tham chiếu | Loại Test   | Mô tả kịch bản                                                      | Severity | Trạng thái |
| -----------| ---------------| -------------| ---------------------------------------------------------------------| ----------| ------------|
| TC-M4-001 | UC19          | Unit        | Tìm kiếm tour — trả danh sách tour khả dụng + thông tin thời tiết   | MEDIUM   | ⬜          |
| TC-M4-002 | UC19          | Unit        | API thời tiết không phản hồi → vẫn trả tour, ẩn thông tin thời tiết | LOW      | ⬜          |
| TC-M4-003 | UC20.1        | Unit        | Đặt tour thành công — tạo bản ghi Tour_Attendees                    | HIGH     | ⬜          |
| TC-M4-004 | UC20.1        | Concurrency | Tour hết slot — đặt thêm bị chặn, trả TOUR-001                      | CRITICAL | ⬜          |
| TC-M4-005 | UC20.1        | Unit        | Đặt tour Post to Room — ghi nợ vào Folio phòng                      | HIGH     | ⬜          |
| TC-M4-006 | UC20.2        | Unit        | Lập lịch chuyến tour — gán xe, tài xế, Tour Guide                   | MEDIUM   | ⬜          |
| TC-M4-007 | UC20.3        | Unit        | Hủy tour — hoàn tiền theo chính sách hoặc đổi lịch                  | HIGH     | ⬜          |
| TC-M4-008 | UC21          | Integration | Gửi ảnh → AI Service trả match → Tour_Attendees cập nhật PRESENT    | HIGH     | ⬜          |
| TC-M4-009 | UC21          | Unit        | AI Service không khả dụng → cho phép điểm danh thủ công             | MEDIUM   | ⬜          |
| TC-M4-010 | UC22          | Unit        | Khách gửi đánh giá 1-5 sao + nội dung text                          | LOW      | ⬜          |
| TC-M4-011 | UC22          | Unit        | Chỉ khách đã sử dụng dịch vụ mới được đánh giá                      | MEDIUM   | ⬜          |
| TC-M4-012 | UC23          | Unit        | Admin ẩn/hiện đánh giá toxic/spam                                   | LOW      | ⬜          |

---

## 🟣 MOD5: KIỂM TOÁN ĐÊM, TÀI CHÍNH & BÁO CÁO

| TC ID | UC tham chiếu | Loại Test | Mô tả kịch bản | Severity | Trạng thái |
|-------|-------------|-----------|----------------|----------|------------|
| TC-M5-001 | UC24.1 | Unit | Folio hiển thị đúng danh sách nợ phòng theo từng dịch vụ | HIGH | ⬜ |
| TC-M5-002 | UC24.2 | Unit | Ghi nhận luồng tiền nhiều đợt — ứng trước, trả thêm, hoàn tiền | HIGH | ⬜ |
| TC-M5-003 | UC24.3 | Integration | Gom hóa đơn — tiền phòng + ăn uống + tour = tổng chính xác (BigDecimal) | CRITICAL | ⬜ |
| TC-M5-004 | UC24.4 | Integration | Night Audit 02:00 AM — cộng phí phòng ngày vào Folio các phòng OCCUPIED | CRITICAL | ⬜ |
| TC-M5-005 | UC24.4 | Unit | Night Audit chuyển Business Date lên 1 ngày | HIGH | ⬜ |
| TC-M5-006 | UC25.1 | Unit | Check-out khi Folio = 0 → thành công, phòng chuyển DIRTY | CRITICAL | ⬜ |
| TC-M5-007 | UC25.1 | Unit | Check-out khi Folio > 0 → chặn, trả FOLIO-001 (BR-FIN-01) | CRITICAL | ⬜ |
| TC-M5-008 | UC25.1 | Unit | Thanh toán tất toán Folio (Tiền mặt/Thẻ) → Folio = SETTLED | HIGH | ⬜ |
| TC-M5-009 | UC25.2 | Integration | Sau tất toán → tự động gửi e-Invoice qua email (SendGrid) | MEDIUM | ⬜ |
| TC-M5-010 | UC26.1 | Unit | Dashboard trả dữ liệu biểu đồ tài chính đúng | MEDIUM | ⬜ |
| TC-M5-011 | UC26.2 | Unit | Occupancy Rate = (phòng OCCUPIED / tổng phòng) × 100% — tính đúng | MEDIUM | ⬜ |
| TC-M5-012 | UC27 | Unit | Báo cáo USALI phân tách doanh thu Rooms / F&B / Tours đúng (BR-FIN-04) | HIGH | ⬜ |
| TC-M5-013 | UC28 | Unit | Kết xuất PDF — file không rỗng, đúng format | MEDIUM | ⬜ |
| TC-M5-014 | UC28 | Unit | Kết xuất Excel — dữ liệu khớp với DB | MEDIUM | ⬜ |

---

## CROSS-MODULE: TEST TÍCH HỢP E2E

| TC ID | UC tham chiếu | Loại Test | Mô tả kịch bản | Severity | Trạng thái |
|-------|-------------|-----------|----------------|----------|------------|
| TC-E2E-001 | UC10→UC12→UC18→UC25 | E2E | Luồng đầy đủ: Đặt phòng → Check-in → Ăn nhà hàng Post to Room → Check-out chặn nợ → Thanh toán → Trả phòng | CRITICAL | ⬜ |
| TC-E2E-002 | UC10→UC20→UC24→UC25 | E2E | Đặt phòng + Đặt tour → Night Audit cộng phí → Folio gom đủ → Tất toán | CRITICAL | ⬜ |
| TC-E2E-003 | UC01→UC05.1→UC16 | E2E | Admin tạo tài khoản F&B → F&B đăng nhập → Tạo order POS thành công | HIGH | ⬜ |

---

## THỐNG KÊ

| Module | Số Test Case | CRITICAL | HIGH | MEDIUM | LOW |
|--------|-------------|----------|------|--------|-----|
| MOD1 | 22 | 3 | 7 | 10 | 2 |
| MOD2 | 20 | 4 | 8 | 7 | 1 |
| MOD3 | 12 | 3 | 5 | 3 | 1 |
| MOD4 | 12 | 1 | 4 | 4 | 3 |
| MOD5 | 14 | 4 | 5 | 5 | 0 |
| E2E | 3 | 2 | 1 | 0 | 0 |
| **Tổng** | **83** | **17** | **30** | **29** | **7** |

**Chú thích:** ⬜ TODO | 🟡 IN PROGRESS | ✅ PASS | ❌ FAIL
