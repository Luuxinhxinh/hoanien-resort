# KAWAI RESORT - SỔ TAY QUY TRÌNH LÀM VIỆC NHÓM (TEAM WORKFLOW GUIDE)

## 1. PHÂN CÔNG VAI TRÒ & GIỚI HẠN VÙNG CODE (CODE BOUNDARIES)

Dự án được chia thành 5 Module độc lập. **Nguyên tắc sống còn:** Việc ai nấy làm, code ai nấy sửa. Tuyệt đối không sửa code thuộc vùng của người khác nếu chưa được sự đồng ý.

### 🚫 Quy tắc giới hạn khu vực chỉnh sửa:
1. **Thư mục Code Chính (`src/main/java/com/kawai/...`)**:
   - Mỗi người chỉ được phép thêm/sửa/xóa các file (Service, Controller, Repository) nằm trong package tương ứng với Module của mình.
   - **CẤM:** Tự ý sửa code trong package `models` (Entity). Các Entity ánh xạ trực tiếp với cấu trúc Database, thay đổi tùy tiện sẽ làm hỏng Database Schema của toàn dự án. Nếu cần thêm trường/sửa bảng, phải bàn bạc với Nhóm trưởng để update file DB Schema trước.
   - **CẤM:** Tự ý sửa các file cấu hình chung (`application.yml`, `pom.xml`, `SecurityConfig`, `KawaiApplication.java`, thư mục `config`). Nếu cần thêm thư viện hoặc đổi cấu hình, phải nhờ Nhóm trưởng thực hiện.
2. **File Tài Liệu Chung (`01_SRS`, `04_testing`, DB Schema)**:
   - Các bảng tổng hợp (`UC_MASTER_TABLE`, `BR_ACTOR_ROLE_TABLE`): Chỉ xem, không tự ý thay đổi quy tắc hệ thống đã chốt.
   - Các bảng tiến độ (`TRACEABILITY_MATRIX`, `TC_MASTER_TABLE`, `MASTER_TDD/EDS`): **Chỉ được điền thông tin vào hàng/khu vực tương ứng với Module của mình**. Cấm sửa/xóa dòng của người khác.

### 👥 Phân công & Vùng hoạt động cụ thể:
* **Lưu (Module 1):** Xác thực, Quản lý PII, RBAC. (Vùng code: `com.kawai.auth`, `com.kawai.profile`, `com.kawai.core`)
* **Dũng (Module 2):** Đặt phòng, Tiền sảnh, Room Matrix. (Vùng code: `com.kawai.booking`, `com.kawai.frontdesk`)
* **Đức (Module 3):** POS Nhà hàng, KDS, Post to Room. (Vùng code: `com.kawai.pos`, `com.kawai.fb`)
* **Em Ngọc (Module 4):** Đặt Tour lữ hành, AI Face Scan, Đánh giá. (Vùng code: `com.kawai.tour`, `com.kawai.feedback`, AI Python Service)
* **Em Lan (Module 5):** Hóa đơn (Folio), Kiểm toán đêm, Báo cáo. (Vùng code: `com.kawai.finance`, `com.kawai.report`)
* **Liuxinhxinh (Tech Lead):** Nắm toàn quyền hệ thống. Quản lý thư mục `config`, file `pom.xml`, xử lý Merge Conflict, ghép code và chạy Test Tích hợp.

---

2. QUY TRÌNH PHÁT TRIỂN VỚI AI (ANTIGRAVITY) THEO CHUẨN TDD

Nhóm chúng ta sẽ áp dụng phương pháp **Test-Driven Development (TDD)**. Mỗi khi bắt đầu một Tính năng (Use Case) mới, mọi người hãy mở Antigravity và thực hiện theo 3 bước sau:

1. **Bước 1 (🔴 Viết Test trước):**
   > *"Đọc file `04_testing/MASTER_TDD_SPEC.md` phần [Mã TC của bạn]. Hãy viết JUnit Test cho Service của tính năng này. Chạy `mvn test` để đảm bảo test đang chạy và báo lỗi (Màu Đỏ)."*
   >
2. **Bước 2 (🟢 Viết Code sau):**
   > *"Bây giờ hãy implement code thực tế cho tính năng này để làm cho file Test vừa nãy chạy PASS 100% (Màu Xanh)."*
   >
3. **Bước 3 (🔵 Tái cấu trúc & Cập nhật Tài liệu):**
   > *"Hãy refactor đoạn code vừa viết cho chuẩn Clean Code. Sau đó đánh dấu [x] vào TC vừa làm trong file `MASTER_TDD_SPEC.md` và cập nhật spec API tương ứng vào `MASTER_EDS_SPEC.md`."*
   >

---

## 3. QUY TRÌNH QUẢN LÝ SOURCE CODE VỚI GIT

**Cấu trúc Nhánh (Branch):**

* `main`: Nhánh chứa bản release cuối cùng dùng để báo cáo. (Chúng ta sẽ không code trực tiếp trên nhánh này).
* `dev`: Nhánh phát triển chung. Tất cả code hoàn thiện sẽ được ghép về đây qua Pull Request.

### 👩‍💻 Dành cho Các Thành Viên (Developer Workflow)

Mỗi ngày trước khi bắt đầu code, mọi người làm theo thứ tự sau:

```bash
# 1. Chuyển về nhánh dev và cập nhật code mới nhất từ team để tránh code trên bản cũ
git checkout dev
git pull origin dev

# 2. Tạo nhánh riêng cho tính năng chuẩn bị làm (Quy tắc tên: feature/[tên-module]-[tính-năng])
git checkout -b feature/mod2-booking

# -> [BẮT ĐẦU CODE BẰNG ANTIGRAVITY VỚI 3 BƯỚC TDD] <-

# 3. Khi code xong, chạy test báo Xanh 100%, tiến hành lưu lại (Commit)
git add .
git commit -m "feat(booking): implement logic dat phong va chong double booking"

# 4. Đẩy nhánh của mình lên Github/Gitlab
git push origin feature/mod2-booking
```

Cuối cùng, mọi người lên Github tạo một **Pull Request (PR)** từ nhánh của mình (`feature/mod2-booking`) vào nhánh `dev`, và tag Nhóm trưởng vào để review nhé.

### 👑 Dành cho Nhóm trưởng (Reviewer Workflow)

Quy trình duyệt code hàng ngày của Nhóm trưởng:

```bash
# 1. Cập nhật nhánh mới của thành viên về máy
git fetch
git checkout feature/mod2-booking

# 2. Dùng Antigravity hỗ trợ review và chạy toàn bộ Test
# Lệnh cho AI: "Hãy chạy lệnh 'mvn test'. Nếu tất cả PASS và code không phá vỡ Module khác thì báo cho tôi."

# 3. Nếu mọi thứ hoạt động tốt, tiến hành ghép code vào nhánh chung
git checkout dev
git merge feature/mod2-booking --no-ff
git push origin dev
```

*Ghi chú: Sau khi có code mới cập nhật lên nhánh `dev`, Nhóm trưởng sẽ thông báo vào nhóm để mọi người kịp thời chạy lệnh `git pull origin dev` cập nhật nhánh của mình.*

---

## 4. HƯỚNG DẪN XỬ LÝ XUNG ĐỘT (MERGE CONFLICT)

Sẽ có những lúc 2 người cùng sửa chung 1 file (ví dụ Entity `User.java`) và gây ra Conflict (cảnh báo đỏ khi dùng Git).
**Cách xử lý:** Mọi người hãy bình tĩnh, **không được tự ý xóa code của bạn khác**. Hãy mở Antigravity lên và nhờ hỗ trợ:

> *"Tôi đang bị Git Conflict ở file `User.java`. Hãy phân tích file hiện tại và giúp tôi Merge code an toàn, đảm bảo giữ lại nguyên vẹn logic của thành viên kia và thêm tính năng mới của tôi vào."*
