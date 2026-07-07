# General Project Rules

## Continuous Learning Mechanism (Cơ chế đúc kết bài học liên tục)
Là một trợ lý AI, sau khi giải quyết xong một bug khó, hoàn thành một tính năng phức tạp, hoặc thống nhất được một thiết kế kiến trúc mới với user, bạn (AI) BẮT BUỘC PHẢI tuân thủ các bước sau trước khi kết thúc tác vụ:

1. **Phân tích Root Cause & Giải pháp:** Tóm tắt ngắn gọn nguyên nhân gốc rễ của vấn đề và cách đã giải quyết.
2. **Đánh giá tính tái sử dụng:** Nếu bài học này có khả năng lặp lại hoặc cần lưu ý cho các tính năng sau, hãy tự động trích xuất thành một nguyên tắc ngắn gọn.
3. **Chủ động Cập nhật Skill:** Đề xuất với user việc bổ sung nguyên tắc này vào một trong các skill hiện có trong thư mục `.agents/skills/` (như `tech-lead-mindset`, `frontend-integrity`, v.v.). Nếu user đồng ý, tiến hành sửa file `SKILL.md` tương ứng.
3.5. **Chống trùng lặp (Deduplication):** Trước khi ghi bài học vào một `SKILL.md`, PHẢI kiểm tra xem nội dung tương tự đã tồn tại ở skill khác chưa (bằng cách dùng tool grep_search). Nếu có → hợp nhất vào một nơi duy nhất (nơi chuyên biệt nhất), skill còn lại chỉ giữ 1 dòng tham chiếu kiểu: "Xem chi tiết tại `xxx.md` §Y". Không bao giờ chép nguyên văn sang 2 file.
4. **Nhắc nhở công cụ `/learn`:** Đôi khi, hãy nhắc nhở user rằng họ có thể sử dụng slash command `/learn` bất kỳ lúc nào trong chat để tự động hóa hoàn toàn việc lưu trữ kiến thức này cho các đoạn chat sau.

## Skill Roles & Workflow Single Source of Truth
Để tránh việc áp dụng quy trình trùng lặp gây tốn kém token và yêu cầu user duyệt nhiều lần, hệ thống skill được phân quyền khắt khe như sau:

| Skill | Vai trò |
|---|---|
| `context-first-quality` | **Quy trình tổng** + phân loại request + Impact Analysis format (DUY NHẤT). Mọi bước checklist hoàn thành đều dồn vào đây. |
| `tech-lead-mindset` | **Nguyên tắc tư duy** (E2E, defensive) — áp dụng bên trong quy trình tổng (không tự định nghĩa quy trình riêng). |
| `agent-efficiency-protocol` | **Quy tắc CÁCH gọi tool** (đọc/sửa) trong mọi bước. |
| `large-file-patching-strategy` | Chi tiết hóa bước sửa khi file >150 dòng. |
| `frontend-integrity` / `db-seeding-integrity` | Checklist chuyên biệt, chỉ nạp khi đụng đúng vùng (UI / seed data). |
