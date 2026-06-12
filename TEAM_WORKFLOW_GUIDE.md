# KAWAI RESORT - SỔ TAY QUY TRÌNH LÀM VIỆC NHÓM (TEAM WORKFLOW GUIDE)

## 1. PHÂN CÔNG VAI TRÒ & MODULE

Dự án được chia thành 5 Module độc lập cho 5 thành viên. Nguyên tắc quan trọng nhất: **Chỉ chỉnh sửa code (Entity, Service, Controller) thuộc Module của mình.** Nếu cần thay đổi code ở Module của người khác, hãy thảo luận với Liuxinhxinh và người phụ trách Module đó trước.

* **Lưu (Module 1):** Xác thực (Auth), Quản lý PII (Hồ sơ), Phân quyền RBAC.
* **Dũng (Module 2):** Đặt phòng (Booking), Tiền sảnh, Quản lý sơ đồ phòng (Room Matrix).
* **Đức (Module 3):** POS Nhà hàng, Order bếp (KDS), Ghi nợ phòng (Post to Room).
* **Em Ngọc(Module 4):** Đặt Tour lữ hành, Điểm danh AI Face Scan, Đánh giá.
* **Em Lan (Module 5 & Tích hợp):** Hóa đơn tổng hợp (Folio), Kiểm toán đêm (Night Audit), Báo cáo.
* **Liuxinhxinh (Tech Lead / Reviewer):** Hỗ trợ giải quyết khó khăn, Review Code, xử lý Merge Conflict, chạy Test Tích hợp toàn hệ thống và duyệt Pull Request ghép code vào nhánh chung.

---

## 2. QUY TRÌNH PHÁT TRIỂN VỚI AI (ANTIGRAVITY) THEO CHUẨN TDD

Nhóm chúng ta áp dụng phương pháp **Test-Driven Development (TDD)**. Mỗi khi bắt đầu một Use Case mới, mọi người mở Antigravity và làm theo **4 bước** dưới đây. Mỗi bước đều có hướng dẫn cụ thể **prompt cho AI** và **điền gì vào file log**.

### Bước 0: 📋 Xác định phạm vi (Trước khi code)

Trước tiên, mở file `01_SRS/UC_MASTER_TABLE.md` và `01_SRS/TRACEABILITY_MATRIX.md` để xác định:
- UC nào mình sẽ làm? (Ví dụ: UC20.1)
- TC nào tương ứng? (Ví dụ: TC-M4-003, TC-M4-004)
- BR nào liên quan? (Ví dụ: BR-TR-01)

> **📝 Điền vào file:**
> - Mở `01_SRS/TRACEABILITY_MATRIX.md`, tìm dòng UC của mình → Cập nhật cột `Trạng thái` từ `⬜` sang `🟡 IN PROGRESS`.

---

### Bước 1: 🔴 Viết Test trước (Pha Đỏ)

Copy prompt dưới đây vào Antigravity, thay `[...]` bằng thông tin của mình:

> *"Đọc file `04_testing/MASTER_TDD_SPEC.md` phần Module [số module] của tôi — tìm kịch bản test `[MODx-TC-xxx]` tương ứng với Use Case `[UCxx]`. Dựa theo mô tả Preconditions, Test Steps và Expected Result trong đó, hãy viết class JUnit 5 Test đặt trong package `com.kawai.services`, sử dụng Mockito để mock Repository. Đặt tên method theo format `TC_Mx_xxx_moTaNgan`. Sau đó chạy `mvn test -pl 03_sourcecode/kawai-backend` để xác nhận test đang FAIL (Màu Đỏ)."*

> **📝 Điền vào file:**
> - Mở `04_testing/MASTER_TDD_SPEC.md`, kéo xuống mục **5. Red-Green-Refactor Tracker** → Tìm dòng TC của mình → Tick `[x]` vào cột `🔴 RED confirmed`.
> - Điền tên file Test vừa tạo vào cột `Test File` (ví dụ: `TourBookingServiceTest.java`).

---

### Bước 2: 🟢 Viết Code để Test PASS (Pha Xanh)

> *"Bây giờ hãy implement code thực tế (Service, Repository, Controller) cho Use Case `[UCxx]` để làm cho toàn bộ Test vừa viết chạy PASS 100%. Tuân thủ Business Rule `[BR-xx-xx]` đã quy định trong file `01_SRS/BR_ACTOR_ROLE_TABLE.md`. Chạy `mvn test` để xác nhận Màu Xanh."*

> **📝 Điền vào file:**
> - Mở `04_testing/MASTER_TDD_SPEC.md` mục **5. Red-Green-Refactor Tracker** → Tick `[x]` vào cột `🟢 GREEN` + ghi commit hash (ví dụ: `a1b2c3d`).
> - Mở `01_SRS/TRACEABILITY_MATRIX.md` → Điền cột `Class / Method` bằng tên class và method thực tế vừa viết (ví dụ: `TourBookingService.bookTour()`).

---

### Bước 3: 🔵 Refactor & Cập nhật toàn bộ tài liệu

> *"Hãy refactor code vừa viết cho chuẩn Clean Code (đặt tên biến rõ ràng, tách method nếu quá dài, thêm JavaDoc comment). Sau đó giúp tôi cập nhật các file tài liệu sau:*
> - *Mở `04_testing/MASTER_TDD_SPEC.md` mục 5 → Điền cột `🔵 REFACTOR note` ghi chú những gì đã cải thiện.*
> - *Mở `04_testing/MASTER_EDS_SPEC.md` → Bổ sung API endpoint mới vào đúng mục Module của tôi (method, URL, request/response JSON, error code, authorization matrix)."*

> **📝 Tổng kết điền file sau cả 3 bước:**
>
> | File | Mục cần điền | Nội dung |
> |------|-------------|---------|
> | `04_testing/MASTER_TDD_SPEC.md` | Mục 5: Red-Green-Refactor Tracker | `🔴 [x]` → `🟢 [x] + commit hash` → `🔵 ghi chú refactor` |
> | `04_testing/MASTER_EDS_SPEC.md` | Mục 6+: API Specification | Endpoint, Request/Response JSON, Error Code, Auth Matrix |
> | `01_SRS/TRACEABILITY_MATRIX.md` | Dòng UC của Module mình | Cột `Class / Method` + cột `Trạng thái` → ✅ |

---

### ⚠️ Lưu ý quan trọng:
- **Mỗi lần chỉ làm 1 UC**, không ôm đồm nhiều UC cùng lúc.
- **Chỉ điền vào dòng/khu vực của Module mình** trong các file log, tuyệt đối không sửa dòng của người khác.
- Nếu test FAIL do phụ thuộc vào Module khác (ví dụ: cần Entity từ MOD1), hãy báo Nhóm trưởng để phối hợp.

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
