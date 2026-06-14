
# KAWAI RESORT - SỔ TAY QUY TRÌNH LÀM VIỆC NHÓM TÍCH HỢP AI (AI-HUMAN COLLABORATIVE WORKFLOW)

Tài liệu này định nghĩa quy trình phối hợp chặt chẽ giữa **Lập trình viên (Developer)**, **Trợ lý AI (Antigravity)**, và **Trưởng nhóm kỹ thuật (Tech Lead)** nhằm đảm bảo tốc độ phát triển nhanh, chất lượng code cao và Tech Lead dễ dàng giám sát tiến độ thực tế.

---

## 1. PHÂN CHIA VAI TRÒ & QUYỀN HẠN (RESPONSIBILITY & VERIFICATION MATRIX)

Để tối đa hóa hiệu suất, các tác vụ được phân bổ rõ ràng giữa AI, Người và Tech Lead:

| Giai đoạn / Tác vụ                                         | 👩‍💻 Người làm (Developer)                                 | 🤖 AI làm (Antigravity)                                                                                                                                                                                      | 👑 Tech Lead duyệt (Reviewer)                                                                              |
| :------------------------------------------------------------- | :-------------------------------------------------------------- | :------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | :---------------------------------------------------------------------------------------------------------- |
| **1. Khởi tạo & Lên kế hoạch**                      | - Chọn Use Case cần làm từ `UC_MASTER_TABLE`.             | - Kiểm tra chéo quy tắc nghiệp vụ trong `BR_ACTOR_ROLE_TABLE`.                                                                                                                                         | - Giám sát trạng thái thông qua `TRACEABILITY_MATRIX`.                                               |
| **2. Viết Test (Pha Đỏ 🔴)**                          | - Ra lệnh cho AI tạo test và chạy thử lệnh test.          | -**Tự động viết file JUnit Test**.`<br>`- Chạy thử test lỗi.`<br>`- **Tự động tick `🔴 RED`** vào `MASTER_TDD_SPEC.md`.                                                        | - (Không cần duyệt bước này, hệ thống tự động lưu vết).                                        |
| **3. Viết Code (Pha Xanh 🟢)**                          | - Nhận xét, tinh chỉnh logic nghiệp vụ phức tạp của AI. | -**Tự động sinh code nghiệp vụ** đúng chuẩn.`<br>`- Chạy `mvn test` xác minh pass 100%.`<br>`- **Tự động tick `🟢 GREEN`** + điền commit hash vào `MASTER_TDD_SPEC.md`. | - (Xem báo cáo xanh của AI trên GitHub PR).                                                             |
| **4. Tối ưu & Viết tài liệu (Pha Xanh Dương 🔵)** | - Yêu cầu AI tối ưu hóa những phần chưa ưng ý.        | -**Tự động Refactor** theo Clean Code.`<br>`- **Tự động cập nhật API** sang `MASTER_EDS_SPEC.md`.                                                                                     | - Đọc và kiểm tra chéo các API Contract trong `MASTER_EDS_SPEC.md`.                                 |
| **5. Commit & Tạo Pull Request**                        | - Chạy git commit, push nhánh và tạo PR trên GitHub.       | - Kiểm tra lỗi bảo mật PII, kiểm tra syntax và check boundaries trước khi commit.                                                                                                                     | -**Kiểm tra & Merge PR** vào nhánh `dev` sau khi xác nhận các test case đều đã PASS xanh. |

---

## 2. GIỚI HẠN VÙNG CODE (CODE BOUNDARIES)

**Nguyên tắc sống còn:** Việc ai nấy làm, code ai nấy sửa.

1. **Thư mục Code Chính (`src/main/java/com/kawai/...`)**:
   * Mỗi thành viên chỉ thêm/sửa code trong package thuộc Module được phân công.
   * **CẤM:** Tự ý sửa các Entity trong package `models` (sẽ làm hỏng Database Schema chung). Mọi thay đổi Database Schema phải được **Tech Lead phê duyệt**.
   * **CẤM:** Tự ý sửa các file cấu hình chung (`application.yml`, `pom.xml`, các file cấu hình bảo mật `SecurityConfig`).
2. **Quyền sở hữu Module:**
   * **Lưu (MOD 1):** Xác thực, 2FA, mã hóa PII. (`com.kawai.auth`, `com.kawai.profile`, `com.kawai.core`)
   * **Dũng (MOD 2):** Đặt phòng, Tiền sảnh, Room Matrix. (`com.kawai.booking`, `com.kawai.frontdesk`)
   * **Đức (MOD 3):** POS Nhà hàng, KDS, Post to Room. (`com.kawai.pos`, `com.kawai.fb`)
   * **Ngọc (MOD 4):** Đặt Tour, AI Face Scan, Đánh giá. (`com.kawai.tour`, `com.kawai.feedback`, AI Python Service)
   * **Lan (MOD 5):** Folio nợ, Kiểm toán đêm (Night Audit), Báo cáo. (`com.kawai.finance`, `com.kawai.report`)
   * **Tech Lead:** Quản lý cấu hình chung (`config`, `pom.xml`), duyệt và merge PR.

---

## 3. QUY TRÌNH CODE TÍCH HỢP AI 5 BƯỚC (AI-DEVELOPMENT LIFECYCLE)

Lập trình viên và AI phối hợp theo luồng khép kín dưới đây. Tech Lead sẽ theo dõi qua sự thay đổi của các file tài liệu.

### Bước 1: Khởi động & Nhận diện (Developer + AI)

* **Developer:** Mở file `01_SRS/TRACEABILITY_MATRIX.md`, cập nhật cột `Class / Method` của UC mình chuẩn bị làm thành `[IN PROGRESS]`.
* **Mục đích:** Tech Lead chỉ cần mở Traceability Matrix là biết ai đang code tính năng nào trong thời gian thực.

### Bước 2: Pha Đỏ - Viết Test trước 🔴 (AI tự động hóa)

* **Developer:** Copy-paste prompt dưới đây gửi cho AI:

  > *"Hãy đọc file `04_testing/MASTER_TDD_SPEC.md` phần Module [số module] của tôi và tìm kịch bản test `[Mã-TC]` tương ứng với Use Case `[Mã-UC]`. Dựa trên đặc tả của nó, hãy viết class JUnit 5 Test tương ứng đặt trong package test của module. Hãy mock các Repository liên quan bằng Mockito. Sau đó chạy lệnh test để xác nhận test đang FAIL (Màu đỏ). Khi test fail đỏ thành công, hãy tự động cập nhật file `04_testing/MASTER_TDD_SPEC.md`:
  >
  > 1. Tick `[x]` vào cột RED của `[Mã-TC]` ở bảng Red-Green-Refactor Tracker.
  > 2. Điền tên file Test vừa tạo vào cột Test File.
  > 3. Cập nhật bảng CHANGELOG ở đầu tài liệu: thêm dòng mới ghi ngày hôm nay, tên tôi [Tên của bạn] và nội dung 'Khởi tạo Test Case [Mã-TC] cho [Mã-UC]'."*
  >
* **AI thực hiện:**

  1. Viết code file Test JUnit.
  2. Chạy test và xác nhận test báo lỗi đỏ.
  3. Cập nhật `MASTER_TDD_SPEC.md`: tick `🔴 [x]`, ghi tên file Test, và thêm dòng mới vào bảng `CHANGELOG`.

### Bước 3: Pha Xanh - Viết Code nghiệp vụ 🟢 (AI thực hiện + Developer giám sát)

* **Developer:** Copy-paste prompt dưới đây gửi cho AI:

  > *"Bây giờ hãy viết code logic (Service, Repository, Controller) cho Use Case `[Mã-UC]` để làm cho test vừa viết chạy PASS 100%. Hãy tuân thủ nghiêm ngặt các Business Rule `[Mã-BR]` trong file `01_SRS/BR_ACTOR_ROLE_TABLE.md`. Chạy lệnh test của Maven để xác nhận test đã pass (Màu xanh). Khi toàn bộ test đã xanh, hãy:
  >
  > 1. Cập nhật file `04_testing/MASTER_TDD_SPEC.md`: tick `[x]` vào cột GREEN và điền commit hash dự kiến.
  > 2. Cập nhật file `01_SRS/TRACEABILITY_MATRIX.md`: đổi trạng thái `[IN PROGRESS]` thành tên `Class.method()` thực tế.
  > 3. Cập nhật bảng CHANGELOG trong `MASTER_TDD_SPEC.md`: thêm dòng mới ghi ngày hôm nay, tên tôi [Tên của bạn] và nội dung 'Implement code logic cho [Mã-UC], tất cả test case đã pass GREEN'."*
  >
* **AI thực hiện:**

  1. Viết logic Code.
  2. Chạy test Maven để xác nhận PASS.
  3. Cập nhật `MASTER_TDD_SPEC.md` (tick `🟢 [x]` và điền commit hash dự kiến, thêm dòng mới vào bảng `CHANGELOG`).
  4. Cập nhật `01_SRS/TRACEABILITY_MATRIX.md` (điền tên Class/Method đã viết).

### Bước 4: Pha Xanh Dương - Tối ưu & Tài liệu hóa 🔵 (AI thực hiện + Tech Lead kiểm tra)

* **Developer:** Copy-paste prompt dưới đây gửi cho AI:

  > *"Hãy tiến hành refactor code vừa viết để đảm bảo sạch sẽ (Clean Code, không trùng lặp, thêm JavaDoc). Sau đó, hãy:
  >
  > 1. Mở `04_testing/MASTER_TDD_SPEC.md` và ghi chú tóm tắt nội dung đã tối ưu vào cột 'REFACTOR note' của Test Case này.
  > 2. Mở `04_testing/MASTER_EDS_SPEC.md` và bổ sung thiết kế API Endpoint mới vào mục Module của tôi (bao gồm method, path, request/response JSON mẫu, error code và phân quyền).
  > 3. Cập nhật bảng CHANGELOG ở đầu cả hai file `MASTER_TDD_SPEC.md` và `MASTER_EDS_SPEC.md`: thêm dòng mới ghi ngày hôm nay, tên tôi [Tên của bạn] và nội dung lần lượt là 'Refactor code cho [Mã-UC]' và 'Đặc tả API endpoint cho [Mã-UC] trong Module [số module]'."*
  >
* **AI thực hiện:**

  1. Cải tiến cấu trúc code cho tối ưu nhất.
  2. Ghi chú vào cột `REFACTOR note` trong `MASTER_TDD_SPEC.md`.
  3. Viết tài liệu đặc tả API đầy đủ vào mục tương ứng trong `MASTER_EDS_SPEC.md`.
  4. Cập nhật bảng `CHANGELOG` ở đầu cả hai file `MASTER_TDD_SPEC.md` và `MASTER_EDS_SPEC.md`.

---

## 4. QUY TRÌNH GIT VÀ QUẢN LÝ PULL REQUEST

### 👩‍💻 Quy trình của Lập trình viên khi kết thúc code:

1. Trước khi commit, ra lệnh cho AI:
   > *"Hãy rà soát lại toàn bộ thay đổi của tôi. Kiểm tra xem có vi phạm Code Boundaries (sửa file cấu hình chung, sửa Entity mà chưa xin phép) hay để lộ dữ liệu nhạy cảm PII ở dạng plaintext không."*
   >
2. Sau khi AI xác nhận an toàn, Developer chạy các lệnh Git:
   ```bash
   git add .
   git commit -m "feat(module-x): implement logic [tên tính năng] and pass [Mã-TC]"
   git push origin feature/mod[x]-[tên-tính-năng]
   ```
3. Tạo Pull Request (PR) từ nhánh feature vào nhánh `dev` trên GitHub, đính kèm link đến phần Module của mình trong `MASTER_TDD_SPEC.md` để Tech Lead kiểm tra.

### 👑 Quy trình duyệt PR của Tech Lead (Tech Lead Verification):

Tech Lead thực hiện duyệt PR theo các bước nghiêm ngặt sau:

1. Mở file `MASTER_TDD_SPEC.md` kiểm tra xem các test case tương ứng với PR này đã có đủ tick `🔴 RED`, `🟢 GREEN`, và commit hash khớp với PR chưa.
2. Kiểm tra `MASTER_EDS_SPEC.md` để đảm bảo API được ghi nhận đúng chuẩn, format JSON hợp lệ.
3. Kéo nhánh feature về máy local và chạy lệnh test toàn bộ dự án:
   ```bash
   mvn test
   ```
4. Nếu tất cả các test case đều PASS xanh và code sạch, Tech Lead merge PR vào nhánh `dev` và thông báo cho cả nhóm.

---

## 5. CÁCH XỬ LÝ XUNG ĐỘT CODE (MERGE CONFLICT)

Khi hai thành viên cùng chỉnh sửa một file chung dẫn đến xung đột (Conflict):

1. **Tuyệt đối không được tự ý xóa code của người khác.**
2. Mở Antigravity lên và dán prompt sau:
   > *"Tôi gặp xung đột Git tại file [đường dẫn file]. Hãy phân tích các phần code bị xung đột và thực hiện merge an toàn, giữ lại nguyên vẹn tính năng của cả hai người và đảm bảo không phá vỡ bất kỳ test case nào sẵn có."*
   >
3. AI sẽ tự động giải quyết conflict và chạy thử test để đảm bảo không phát sinh lỗi mới trước khi bạn commit lại.
