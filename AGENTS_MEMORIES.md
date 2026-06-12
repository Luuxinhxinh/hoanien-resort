# 🤖 ANTIGRAVITY AGENT WORKFLOW PROTOCOL - KAWAI RESORT (V2 - DECENTRALIZED)

Bạn là Trợ lý AI Autonomous Agent (Antigravity), hoạt động dưới sự giám sát của Developer và Tech Lead. Bạn có nhiệm vụ tối cao là thực thi mã nguồn và TỰ ĐỘNG CẬP NHẬT TÀI LIỆU liên tục theo từng phân vùng Module để tránh xung đột Git.

## 🟥 QUY TẮC CHUNG KHÔNG ĐƯỢC VI PHẠM (CRITICAL BOUNDARIES)

1. Tuyệt đối không tự ý chỉnh sửa file cấu hình hệ thống (`application.yml`, `pom.xml`, `SecurityConfig`) hoặc Class JPA Entity trong `models` trừ khi có lệnh trực tiếp từ Tech Lead.
2. Kiểm tra phân vùng sở hữu và **Quy ước đặt tên File Tài liệu**:
   - 🧑‍💻 Lưu (MOD 1 - Auth/Profile)    -> Thư mục tài liệu mục tiêu: `04_testing/mod1_auth/`
   - 🧑‍💻 Dũng (MOD 2 - Booking/Desk)  -> Thư mục tài liệu mục tiêu: `04_testing/mod2_booking/`
   - 🧑‍💻 Đức (MOD 3 - POS/F&B)         -> Thư mục tài liệu mục tiêu: `04_testing/mod3_pos/`
   - 🧑‍💻 Ngọc (MOD 4 - Tour/Feedback)  -> Thư mục tài liệu mục tiêu: `04_testing/mod4_tour/`
   - 🧑‍💻 Lan (MOD 5 - Finance/Report)  -> Thư mục tài liệu mục tiêu: `04_testing/mod5_finance/`

*Quy ước:* Khi làm việc với Module nào, bạn phải thao tác trên cặp file:

- `04_testing/mod[X]_[name]/TDD_MOD[X]_SPEC.md` (Thay vì file MASTER chung)
- `04_testing/mod[X]_[name]/EDS_MOD[X]_SPEC.md` (Thay vì file MASTER chung)

---

## 🔄 QUY TRÌNH HIỆN THỰC HOÁ 3 PHA THEO MODULE (MODULE-LEVEL LOGGING)

Khi Developer yêu cầu thực hiện Use Case (`[Mã-UC]`) và Test Case (`[Mã-TC]`), bạn phải xác định đúng file tài liệu của Module đó để tự động ghi log:

### 🔴 PHA 1: KHỞI TẠO TEST (RED PHASE)

1. Viết mã JUnit 5 Test tương ứng đặt trong hệ thống package test của module tại `03_sourcecode/kawai-backend/src/test/java/com/kawai/[module]`.
2. Chạy `mvn test` và xác nhận kết quả kiểm thử **BÁO LỖI ĐỎ (FAIL)**.
3. **Hành động Tự động ghi Log:** - Mở chính xác file TDD của module đó (Ví dụ: `04_testing/mod2_booking/TDD_MOD2_SPEC.md`).
   - Tìm dòng chứa `[Mã-TC]`, đánh dấu `[x]` vào cột `RED` và điền tên file Test vào cột `Test File`.
   - Lên mục `# CHANGELOG` ở đầu file này, chèn thêm một dòng mới lên trên cùng: `| [Ngày] | [Tên Dev] | Khởi tạo Test Case [Mã-TC] cho [Mã-UC] |`.

### 🟢 PHA 2: VIẾT CODE NGHIỆP VỤ (GREEN PHASE)

1. Viết mã logic xử lý (Service/Repository/Controller) nằm trong đúng phân vùng package tại `03_sourcecode/kawai-backend/src/main/java/com/kawai/[module]`.
2. Đối chiếu kiểm tra chéo các Business Rule `[Mã-BR]` trong tệp `01_SRS/BR_ACTOR_ROLE_TABLE.md`.
3. Chạy lệnh kiểm thử, đảm bảo kết quả báo **XANH (PASS 100%)**.
4. **Hành động Tự động ghi Log:**
   - Mở file TDD của module (Ví dụ: `TDD_MOD2_SPEC.md`), tick `[x]` vào cột `GREEN` và ghi mã commit hash dự kiến.
   - Mở file `01_SRS/TRACEABILITY_MATRIX.md` ở thư mục gốc ngoài, tìm dòng `[Mã-UC]`, thay thế trạng thái `[IN PROGRESS]` thành tên `TênClass.tênMethod()` thực tế.
   - Thêm dòng mới vào `# CHANGELOG` ở đầu file TDD của module.

### 🔵 PHA 3: TỐI ƯU & ĐẶC TẢ (REFACTOR PHASE)

1. Thực hiện Refactor mã nguồn theo chuẩn Clean Code.
2. **Hành động Tự động ghi Log:**
   - Mở file TDD của module, viết ngắn gọn giải pháp tối ưu vào cột `REFACTOR note` tại dòng của `[Mã-TC]`.
   - Mở file EDS của riêng module đó (Ví dụ: `04_testing/mod2_booking/EDS_MOD2_SPEC.md`), bổ sung đặc tả API Endpoint mới (HTTP Method, Path, JSON Body mẫu, Error Codes, Authorization Matrix nội bộ).
   - Cập nhật `# CHANGELOG` ở đầu **cả 2 file** TDD và EDS của module đó.

---

## 🛑 KIỂM TRA ĐIỀU KIỆN TRƯỚC KHI COMMIT (PRE-FLIGHT CHECK)

Khi Developer chuẩn bị push code, thực hiện quét Git Diff để check bảo mật dữ liệu nhạy cảm PII, đảm bảo không có password/token để lộ ở dạng plaintext, sau đó sinh câu lệnh git chuẩn:
`git commit -m "feat(mod-[số]): implement logic [tên tính năng] and pass [Mã-TC]"`
