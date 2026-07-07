---
name: security-baseline
description: Nguyên tắc bảo mật cơ bản cho log dữ liệu nhạy cảm (PII) và escape dữ liệu trên frontend. Kích hoạt khi cấu hình log, in log, hoặc chèn dữ liệu động vào thẻ HTML qua Thymeleaf.
---

# Quy tắc Bảo Mật & Data Sanitization Cơ Bản

## 1. Không log PII (Dữ liệu nhận dạng cá nhân)
- KHÔNG BAO GIỜ đặt level TRACE/DEBUG cho Hibernate binding trong môi trường khác ngoài local/dev thuần túy.
- Việc in ra TRACE log sẽ làm lộ toàn bộ tham số (parameters) như mật khẩu, mã PIN hash, số điện thoại, email của khách thật lên màn hình console hoặc file log. Đây là thói quen cực kỳ nguy hiểm.
- Khi tự viết log (`log.info()`), hãy bọc (mask) hoặc che đi dữ liệu nhạy cảm.

## 2. Escape dữ liệu khi nhúng vào Thymeleaf
- **Lỗ hổng tiềm ẩn:** Chèn dữ liệu động (như tên khách hàng, mô tả) trực tiếp vào thuộc tính của thẻ HTML như `onclick="..."` bằng phép nối chuỗi (string concatenation) sẽ làm vỡ HTML hoặc sinh ra lỗi XSS nếu dữ liệu chứa dấu nháy đơn (`'`) hoặc nháy kép (`"`).
- **Action:** Dữ liệu chèn vào thuộc tính event PHẢI được escape. Trong Thymeleaf, hãy dùng `th:attr` hoặc cú pháp `th:onclick` thay vì chèn trực tiếp.
  - Sai: `onclick="openModal('${guest.name}')"`
  - Đúng: `th:onclick="|openModal('${guest.name}')|"` (Thymeleaf tự động xử lý an toàn) hoặc truyền qua `data-*` attribute.
