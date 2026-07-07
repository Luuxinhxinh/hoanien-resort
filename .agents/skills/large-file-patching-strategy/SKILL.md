---
name: large-file-patching-strategy
description: Chiến thuật thao tác an toàn với các file lớn (Large Files). Tránh các lỗi mất ngữ cảnh, lỗi thiếu ký tự/khoảng trắng khi replace, và hỏng cấu trúc syntax (dấu ngoặc, thẻ HTML) thường gặp ở các file dài.
---

# Chiến Thuật Vá Lỗi File Lớn (Large File Patching Strategy)

Làm việc với các file code dung lượng lớn rất dễ gặp rủi ro: Tool replace thất bại do sai khoảng trắng, phá hỏng block code do AI sinh thiếu dòng, hoặc AI "ảo giác" do context bị cắt cụt. Hãy tuân thủ nghiêm ngặt cẩm nang sinh tồn sau:

## 1. Định Vị Điểm Nóng (Hotspot Locating)
- **Tuyệt đối cấm** yêu cầu đọc toàn bộ một file có hàng nghìn dòng.
- Khi cần tìm nơi sửa:
  1. Dùng `grep_search` với từ khóa đặc trưng (tên hàm, class, ID thẻ, biến đang bị lỗi). Trả về cùng thông số `MatchPerLine=true` để lấy số dòng (Line Number).
  2. Từ số dòng tìm được, dùng công cụ `view_file` kết hợp với `StartLine` và `EndLine` (quét biên độ khoảng 50 - 100 dòng quanh điểm nóng). Việc này đảm bảo bạn lấy đủ trọn vẹn bối cảnh của 1 function mà không bị nhiễu.

## 2. Kỹ thuật Lấy Mẫu Mã Nguồn Gốc (Exact Target Extraction)
- **Nguyên nhân cốt lõi gây lỗi:** 90% các lần thay thế code bị thất bại (hoặc không tìm thấy đoạn target) là do `TargetContent` được truyền vào bị lệch khoảng trắng (indentation) so với file gốc.
- **Quy tắc Vàng:** TRƯỚC KHI thực hiện hành động replace, phải dùng `view_file` đọc và copy **nguyên văn từng dấu cách, ký tự xuống dòng** của đoạn cần sửa để truyền vào `TargetContent`. KHÔNG TỰ Ý GÕ LẠI, không tự đoán thụt lề, không bỏ bớt dòng trống.

## 3. Vá Lỗi Cục Bộ & Giới Hạn Dòng (Chunk Replacement)
- **Chỉ thay thế những gì thực sự thay đổi:** Thay vì nạp một khối `TargetContent` khổng lồ dài 300 dòng (chắc chắn sẽ gây lỗi thiếu dòng do AI generate nửa chừng), hãy chia nhỏ. Chỉ gói gọn `TargetContent` vào khối 10-30 dòng trực tiếp chứa đoạn logic cần thay đổi.
- **Luôn truyền Line Limits:** Khi gọi tool replace, BẮT BUỘC phải truyền `StartLine` và `EndLine`. Nó giúp tool giới hạn phạm vi tìm kiếm, quét nhanh hơn và loại trừ các khối code trùng lặp ở nơi khác.
- Đảm bảo khối replace phải "trọn vẹn" về mặt cú pháp (đủ số lượng dấu mở/đóng ngoặc `{ }`, đủ thẻ `<th:block>...</th:block>`).

## 4. Kiểm Định Hậu Phẫu (Post-Patch Verification)
- Sau khi replace file thành công, đừng bao giờ tự tin 100%. Rất có thể bạn vừa làm mất một dấu `}` hoặc `</div>` ở viền của đoạn thay thế.
- **Action:** Hãy chạy lệnh biên dịch (`mvn compile`, `npm run build`) hoặc mở terminal gọi unit test của file đó lên để kiểm tra ngay lập tức.
- Nếu lỡ làm hỏng cấu trúc, bình tĩnh gọi lệnh `git diff` để xem đoạn code vừa chèn bị lệch cú pháp ở dòng nào, sau đó có thể vá lại hoặc `git checkout -- <file>` để làm lại từ đầu cẩn thận hơn.
