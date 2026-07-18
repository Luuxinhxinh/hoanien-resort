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

## 🛑 HARD-STOP RULE (Ràng buộc cứng cho Agent)
**ĐỌC KỸ VÀ TUÂN THỦ TUYỆT ĐỐI VỚI MỌI REQUEST MỚI:**
1. Trừ khi đang tiếp tục một task dang dở, nếu nhận một prompt mới từ user bắt đầu một task, tool call ĐẦU TIÊN của bạn BẮT BUỘC phải là `view_file` để đọc `context-first-quality/SKILL.md`. 
2. Tuyệt đối KHÔNG ĐƯỢC phép gọi các lệnh terminal (`run_command`), không được đọc source code, không sửa file cho đến khi bạn đã nạp đủ ngữ cảnh từ các skill liên quan.
3. Việc vi phạm nguyên tắc này (nhảy vào chạy lệnh luôn) là KHÔNG THỂ CHẤP NHẬN ĐƯỢC.

## 🚧 Safe Git Push Rule (Quy tắc Push An toàn - Chống đè code)
**Bắt buộc áp dụng khi được yêu cầu đẩy code (`git push`) hoặc tạo Merge Request:**
Do các thành viên thường quên cập nhật code mới, dẫn đến lỗi "Stale Branch Merge" (code cũ đè mất giao diện mới trên `dev`), Agent KHÔNG ĐƯỢC chỉ hỏi suông mà BẮT BUỘC PHẢI TỰ ĐỘNG thực hiện các bước sau:

1. **Tự động Đồng bộ (Auto-Fetch):** Tự động chạy `git fetch origin` để lấy thông tin mới nhất.
2. **Tự động Kéo code (Auto-Pull/Merge):** Kiểm tra trạng thái. Nếu nhánh hiện tại thiếu code so với `origin/dev`, Agent phải TỰ ĐỘNG chạy lệnh `git merge origin/dev` (hoặc `git pull origin dev`) để ép cập nhật code mới vào nhánh hiện tại trước khi push.
3. **Giám sát Conflict Giao diện (Cực kỳ quan trọng):** 
   - Nếu xảy ra conflict (đặc biệt ở các file HTML/CSS/JS), Agent phải lập tức cảnh báo người dùng: *"Báo động: Đang có conflict giao diện! Tuyệt đối KHÔNG chọn 'Accept Incoming Change' (từ dev) vì sẽ làm đè mất giao diện mới sửa của nhánh này!"*.
   - Agent phải chủ động đề xuất hỗ trợ user resolve conflict (chọn "Accept Current" hoặc "Accept Both" và sửa tay).
4. **Push an toàn:** Chỉ chạy lệnh `git push` sau khi quá trình merge dev đã hoàn tất thành công và không còn conflict.
