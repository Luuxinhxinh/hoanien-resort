---
name: code-cleanup-protocol
description: Quy trình dọn dẹp mã nguồn (Refactoring / Cleanup). Kích hoạt khi user yêu cầu "dọn dẹp code", "xóa code rác", "tối ưu import", hoặc thực hiện các khuyến nghị từ bản Audit.
---

# Giao Thức Dọn Dẹp Mã Nguồn (Code Cleanup Protocol)

## 1. Nguyên Tắc An Toàn (Safety First)
- **Git Check (Bắt Buộc):** TRƯỚC KHI tiến hành bất kỳ hành động dọn dẹp nào, hãy chạy lệnh `git status`. Nếu có file code (không liên quan) đang làm dở chưa commit, bạn PHẢI cảnh báo user commit hoặc stash trước khi tiến hành dọn dẹp để đảm bảo có thể rollback dễ dàng nếu dọn dẹp gây lỗi.
- **Chia để trị (Chunking):** Tuyệt đối không nhồi nhét dọn dẹp 10 file cùng lúc bằng 1 cú Replace khổng lồ. Hãy dọn dẹp theo nhóm (Ví dụ: Dọn xong nhóm Controller, test thành công, rồi mới sang nhóm Service).

## 2. Các Hạng Mục Dọn Dẹp Cốt Lõi
- **Unused Imports & Variables:** Xóa các thư viện/class được import nhưng không bao giờ sử dụng trong tệp; xóa các biến cục bộ khai báo nhưng không dùng.
- **Dead Code (Code Chết):** Xóa các method, class được khai báo nhưng không ai gọi. 
  - *Cảnh báo:* Trừ khi hàm đó có `@EventListener` hoặc `@RequestMapping`, nếu là hàm thường, bạn BẮT BUỘC dùng `grep_search` tên hàm quét toàn dự án. Nếu xác minh chắc chắn 100% kết quả là 0 nơi gọi -> Mới được xóa.
- **Magic Numbers & Hardcode Strings:** Đưa các chuỗi/số bị lặp lại nhiều lần ra thành hằng số `public static final` hoặc file `application.properties`.
- **Chuẩn hóa Format:** Xóa bớt khoảng trắng (blank lines) thừa thãi, sửa lại thụt lề (indentation). *Lưu ý: Không format lại toàn bộ các dòng không liên quan trong 1 file lớn để tránh làm phình to lịch sử Git diff vô ích.*

## 3. Cách Thức Triển Khai
- Luôn ưu tiên dùng công cụ `multi_replace_file_content` (dành cho file đơn) hoặc `replace_file_content`.
- Nếu công việc dọn dẹp quá lớn (ví dụ đổi tên 1 class lan ra 20 file), **không làm tay thủ công**. Hãy viết một đoạn script tự động (PowerShell/Python/Node) để xử lý thay cho AI tool.
- **Post-Cleanup Verification:** Dọn xong 1 nhóm file BẮT BUỘC gọi lệnh build (`mvn compile` hoặc `npm run build`) để xác minh việc dọn dẹp (đặc biệt là xóa import/dead code) không làm sập project!
