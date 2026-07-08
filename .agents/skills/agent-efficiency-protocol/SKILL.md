---
name: agent-efficiency-protocol
description: Tối ưu hóa việc sử dụng công cụ (Tool Usage) để đọc, sửa code và debug nhằm tiết kiệm token và tăng tốc độ xử lý. Kích hoạt khi cần đọc file lớn, sửa đổi nhiều file hoặc tìm kiếm diện rộng.
---

# Giao thức Tối ưu hóa Công cụ & Token (Agent Efficiency Protocol)

## 1. Tối ưu Đọc & Tìm kiếm (Reading & Searching)
- **KHÔNG dùng lệnh bash terminal (`cat`, `grep`, `ls`)**: BẮT BUỘC dùng các tool chuyên dụng có sẵn như `view_file`, `grep_search`, `list_dir`. Các tool native này trả về kết quả cấu trúc chuẩn (JSON), tiết kiệm lượng token khổng lồ so với việc bắt AI đọc raw text từ console.
- **Fallback an toàn**: Nếu tool được nêu tên ở đây không tồn tại trong danh sách tool hiện có (do môi trường hoặc Antigravity update), hãy dùng tool tương đương gần nhất đang có sẵn — tuyệt đối không được tự ý gọi/bịa tool không tồn tại gây lỗi hệ thống.
- **Dùng `view_file` có chiến thuật**: Trừ những file cực nhỏ, ĐỪNG BAO GIỜ gọi `view_file` mà không truyền vào `StartLine` và `EndLine`. 
  - *Quy trình chuẩn:* Dùng `grep_search` để tìm chính xác số dòng chứa nội dung cần quan tâm. Sau đó gọi `view_file` với biên độ khoảng 50 dòng quanh khu vực đó.

## 2. Tối ưu Chỉnh sửa Code (Code Editing)
- **Git Safety Net**: Trước khi thực hiện Sửa lớn/Refactor, PHẢI kiểm tra `git status` — nếu có thay đổi chưa commit không liên quan, cảnh báo user trước khi tiếp tục để đảm bảo khả năng rollback an toàn.
- **Cấm lạm dụng `write_to_file`**: Tuyệt đối KHÔNG ghi đè toàn bộ file (Overwrite) chỉ để sửa một vài dòng. Việc này tiêu tốn token khủng khiếp và dễ gây mất mát code nếu model sinh thiếu (truncation).
- **Sử dụng Tool Edit thông minh**: 
  - Chỉ dùng `replace_file_content` (sửa 1 chỗ) hoặc `multi_replace_file_content` (sửa nhiều chỗ rải rác).
  - Dữ liệu đưa vào trường `TargetContent` phải chính xác đến từng dấu cách, ký tự xuống dòng (copy y hệt kết quả đọc từ `view_file`).
  - Phải truyền vào `StartLine` và `EndLine` để giới hạn phạm vi quét của tool, giúp edit thành công chỉ trong tíc tắc.
- **KHÔNG dùng `sed` hay `awk` để sửa code qua Terminal**: Các tool chuyên dụng có cơ chế safety/diff review an toàn hơn nhiều. Trừ phi bạn cần chạy một đoạn regex đổi tên/sửa hàng loạt trên hàng chục file cùng lúc thì mới được viết script Python/PowerShell tự động.

## 3. Tối ưu Compile & Debug (Build & Feedback Loop)
- **Chạy Test chọn lọc**: Thay vì gõ lệnh `mvn test` hay `npm run test` quét toàn bộ project mất hàng phút, hãy chạy đích danh file test liên quan (ví dụ: `mvn test -Dtest=ClassName`) để vòng lặp phản hồi (feedback loop) chỉ mất vài giây.
- **Gom nhóm sửa đổi**: Hãy phân tích liên đới toàn diện, sửa một lúc các file liên quan (Controller -> Service -> Repo -> UI), rà soát logic thật kỹ rồi mới chạy lệnh Build/Test. Việc sửa 1 file -> Build -> Lỗi -> Sửa file khác -> Build lại là một anti-pattern cực kỳ lãng phí thời gian chờ (idle time).

## 4. Dọn dẹp Workspace (Clean Workspace)
- **Xóa ngay file tạm/scripts**: Mọi file script viết tạm (ví dụ python, powershell script) dùng để fix bug, patch data hoặc test thử logic PHẢI ĐƯỢC XÓA NGAY LẬP TỨC bằng lệnh terminal (`Remove-Item` hoặc `rm`) ngay khi sử dụng xong. 
- Việc để lại các file rác này trong source code (workspace) là hành vi cẩu thả, gây nhiễu cho user và rác repo. Môi trường làm việc lúc hoàn thành task phải sạch sẽ đúng như lúc bắt đầu.
