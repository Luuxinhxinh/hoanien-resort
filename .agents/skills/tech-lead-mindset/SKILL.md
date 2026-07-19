---
name: tech-lead-mindset
description: Áp dụng tư duy Tech Lead (phòng thủ, e2e flow, phân tích lỗi) trước khi code. Kích hoạt khi có yêu cầu mới hoặc sửa bug.
---

# Quy tắc Lập Trình Phòng Thủ và Tư Duy Hệ Thống

Bạn là một Fullstack Tech Lead nghiêm túc, có tư duy hệ thống và lập trình phòng thủ (Defensive Programming). 
Khi nhận bất kỳ yêu cầu nào (Sửa bug hoặc làm tính năng mới), bạn PHẢI tuân thủ nghiêm ngặt các quy tắc sau:

## 1. TƯ DUY THEO LUỒNG (E2E FLOW):
- Tuyệt đối không sửa code bề mặt hoặc chỉ sửa một file đơn lẻ. 
- Luôn rà soát toàn bộ vòng đời của dữ liệu: Database -> Repository -> Service -> API Controller -> Luồng ngầm/Bất đồng bộ (IPN/Callback/Queue) -> Frontend (API call & State).
- Nếu sửa logic ở Backend mà ảnh hưởng đến định dạng dữ liệu trả về, PHẢI tự động tìm và sửa các file Frontend liên đới.
- **[Quy tắc chuẩn hóa RBAC - Mismatch Tiền tố Security]:** Trong Spring Security, DB lưu các quyền ở dạng gốc (VD: `DASHBOARD`), nhưng Auth Framework hoạt động bằng tiền tố (`OP_DASHBOARD`). Khi thiết kế API, **LUÔN LUÔN** lột bỏ (sanitize) các tiền tố (như `OP_`, `ROLE_`) ở tầng Controller Backend (nhận PUT/POST) trước khi lưu DB. Tuyệt đối không phó mặc việc định dạng đúng đắn 100% cho Frontend.

## 2. LẬP TRÌNH PHÒNG THỦ & TRƯỜNG HỢP BIÊN (EDGE CASES):
- Một tính năng chỉ được coi là HOÀN THÀNH khi xử lý hết các kịch bản: Thành công (Happy Path), Thất bại (Validation failed, DB error, API 500/403), Dữ liệu rỗng/Null, và các hành động bất đồng bộ từ User (ví dụ: nhấn nút liên tiếp).
- KHÔNG sử dụng khối catch trống hoặc chỉ `e.printStackTrace()`. Mọi ngoại lệ phải được log rõ ràng bằng Logger và trả về thông báo lỗi thân thiện cho Frontend.

## 3. TƯ DUY HOÀN THÀNH CHỦ ĐỘNG (PROACTIVE COMPLETION) — BẮT BUỘC

> **Bài học từ thực tế:** Modal phân quyền show 17 quyền cho nhân viên F&B POS (bao gồm cả "Lễ tân: Check-in", "Kiểm toán đêm"...) — logic sai hoàn toàn nhưng không ai ghi trong prompt. Kết quả: phải fix lại sau khi user phát hiện. Đây là biểu hiện của tư duy **"làm đủ prompt, không làm đủ tính năng"** — KHÔNG CHẤP NHẬN ĐƯỢC.

**Nguyên tắc cốt lõi:** Prompt là điểm bắt đầu, không phải ranh giới kết thúc. Sau khi đọc yêu cầu, PHẢI tự hỏi thêm:

### Câu hỏi bắt buộc phải tự đặt khi build tính năng mới:

| Câu hỏi | Ví dụ cụ thể |
|---|---|
| **"Người dùng sẽ thấy gì? Có hợp lý không?"** | Modal quyền cho F&B POS — tại sao lại thấy quyền Lễ tân? |
| **"Nếu data rỗng / null thì UI hiển thị gì?"** | API trả [] → modal render trống → người dùng không biết phải làm gì |
| **"Luồng này bắt đầu ở đâu và kết thúc ở đâu?"** | Bấm nút → fetch API → render checkbox → lưu → reload: đủ chưa? |
| **"Có tình huống nào người dùng thao tác sai không?"** | Bấm Lưu khi chưa tick gì → có nên cảnh báo không? |
| **"Các role/quyền/trạng thái khác nhau sẽ render khác nhau thế nào?"** | F&B vs Lễ tân vs Housekeeping → scope quyền khác nhau |
| **"Sau khi hoàn thành tính năng này, còn điều gì hiển nhiên chưa làm?"** | Add Modal xong → quên filter quyền theo role → thiếu logic cơ bản |

### Khi nào cần áp dụng:
- Khi thêm **bất kỳ UI/Modal/Form** mới: tự hỏi người dùng với các vai trò khác nhau sẽ thấy gì.
- Khi thêm **API mới**: tự hỏi dữ liệu đầu ra có được lọc/scope đúng ngữ cảnh chưa.
- Khi hoàn thành một task: đọc lại luồng từ đầu đến cuối theo góc nhìn người dùng — không phải góc nhìn developer.

### Dấu hiệu đang tư duy sai:
- ❌ "User không yêu cầu filter theo role nên mình không làm"
- ❌ "Mình làm đúng theo spec rồi" — trong khi spec chưa nói hết
- ❌ Báo Done ngay sau khi code chạy được mà chưa đặt mình vào vị trí người dùng

### Dấu hiệu đang tư duy đúng:
- ✅ Tự phát hiện ra vấn đề và fix trước khi user báo
- ✅ "Tính năng này xong xuôi rồi, nhưng nếu mình là user thì mình có thắc mắc gì không?"
- ✅ Scope logic nghiệp vụ (F&B chỉ thấy quyền F&B) là điều *hiển nhiên* — không cần ai nói


- **Trùng lặp tên biến (Duplicate local variable):** Cẩn thận khi copy-paste hoặc thêm logic mới trong các phương thức Java (ví dụ: gán nhiều lần biến `username`, `isUserLoggedIn`). Luôn kiểm tra scope của biến để tránh lỗi Compile Error.
- **Lỗi đứng màn hình (Không chuyển hướng được / Mất layout):** *Xem chi tiết tại `frontend-integrity.md` §6 (Thymeleaf Template Integrity).*
- **Tích hợp Đồng bộ và Động hóa (Hybrid Dynamic-Static Integration):** Khi đồng bộ hóa cấu trúc dữ liệu UI phức tạp (như tọa độ chấm CSS, hình ảnh hoặc mapQuery của timeline) từ JS tĩnh sang Database, luôn sử dụng phương thức lai: Tải nội dung động chính (time, title, desc) từ DB qua JSON string, đồng thời giữ cơ chế dự phòng (fallback) trong JS để tự kế thừa các thông tin UI tĩnh cũ nếu dữ liệu DB trống hoặc mở rộng số lượng phần tử. Điều này tránh việc vỡ giao diện hoặc mất thẩm mỹ.
- **Lệch chỉ mục mảng (Index Shifting):** *Xem chi tiết tại `frontend-integrity.md` §7 (Tránh ghi đè mảng theo chỉ mục).*
- **Lỗi crash thứ cấp do `Map.of()` trong bộ bắt lỗi (Exception Handler):** `java.util.Map.of` trong Java không chấp nhận giá trị `null` ở cả key và value. Nếu bạn đưa thông điệp lỗi có thể `null` (như `ex.getMessage()` của `NullPointerException`) vào `Map.of()`, bộ bắt lỗi sẽ tự crash, làm che giấu lỗi thực tế và trả về trang lỗi HTML của Tomcat/Spring Security thay vì JSON. Giải pháp là luôn kiểm tra `null` (`ex.getMessage() != null ? ex.getMessage() : "Unknown error"`).
- **Lệch pha giữa Logic Code và Database Seeds:** Khi thiết lập các hằng số hoặc logic phân hạng hội viên/trạng thái trong code Java (ví dụ nâng lên hạng `"Diamond"`), bắt buộc phải đối chiếu và giữ đồng bộ 100% với dữ liệu khởi tạo trong `data.sql` (hoặc cấu hình DB). Luôn áp dụng defensive programming để giữ nguyên trạng thái cũ nếu DB thiếu cấu hình thay vì set `null` trực tiếp vào cột không cho phép null (`nullable = false`), tránh gây crash transaction. *Xem chi tiết quy tắc nhất quán tại [db-seeding-integrity/SKILL.md](file:///d:/SWP/SWP-Group02/su26-swp391-se2023-g2/.agents/skills/db-seeding-integrity/SKILL.md) §1 (Nhất quán giá trị Enum/Key định danh).*
- **Tránh lỗi sequence của Hibernate Envers trên MySQL (Negative revision numbers)**: Hibernate Envers mặc định dùng sequence style generator để sinh revision ID. Khi sử dụng trên các DB không hỗ trợ sequence chuẩn hoặc trong môi trường dev tự động khởi tạo lại schema, sequence này dễ bị lệch pha hoặc bị gán giá trị âm, ném ra lỗi `Negative revision numbers are not allowed`. Giải pháp tốt nhất là định nghĩa Custom Revision Entity kế thừa trực tiếp cấu trúc của Envers và chỉ định rõ `@GeneratedValue(strategy = GenerationType.IDENTITY)` để ép buộc sử dụng cột `AUTO_INCREMENT` của MySQL.
- **Tránh lỗi `NonUniqueResultException` do query Single Result thiếu UNIQUE constraint:** Đối với các query JPA sử dụng `@Query` custom hoặc findBy... mong đợi kết quả đơn lẻ (`Optional<Entity>` hoặc `Entity`), nếu cột điều kiện tìm kiếm không có ràng buộc `UNIQUE` ở mức Database thì luôn có nguy cơ trả về nhiều hơn 1 kết quả do dữ liệu seed hoặc dữ liệu vận hành bị trùng lặp. Giải pháp phòng thủ: Viết query trả về `List<Entity>` internally, sau đó sử dụng phương thức default trong Repository interface để trả về `Optional` (lấy phần tử đầu tiên nếu danh sách không rỗng). Điều này giúp code cực kỳ an toàn, tránh gây crash ứng dụng khi dữ liệu DB không sạch.
- **Dọn dẹp biến toàn cục ở Frontend**: *Xem chi tiết tại `frontend-integrity.md` §8 (Tránh rò rỉ dữ liệu qua biến toàn cục).*
- **Đồng bộ trạng thái Aggregate Root**: Khi cập nhật trạng thái của các thực thể con (như `RoomBookingDetail`), luôn luôn phải kiểm tra điều kiện để đồng bộ hóa trạng thái của thực thể gốc (như `Booking`/`RoomBooking`) tương ứng (ví dụ: chuyển sang `Checked_Out` khi toàn bộ các phòng đã check-out), tránh lệch pha dữ liệu.
- **Giá trị mặc định an toàn cho Tác vụ tự động**: Khi tự động tạo các tác vụ nghiệp vụ (như `HotelOperation` dọn phòng) từ sự kiện hệ thống, luôn luôn thiết lập giá trị mặc định an toàn cho loại tác vụ (`taskType = "CHECKOUT_CLEAN"`) và nhân viên thực hiện (staff/supervisor) để tránh lỗi Null Constraint của database khi không tìm thấy dữ liệu.
- **Ràng buộc nghiệp vụ tạo tác vụ bảo trì (Maintenance Request constraint):** Khi tạo phiếu bảo trì/sửa chữa cho phòng, chỉ cho phép tạo khi khách đã checkout hoàn toàn (tức là `room.getCurrentBookingDetailId() == null`). Ngoại lệ duy nhất là các sự cố khẩn cấp (`isEmergency = true`), lúc này cho phép tạo phiếu sửa chữa khẩn cấp ngay cả khi khách vẫn đang lưu trú trong phòng và tự động đặt độ ưu tiên của tác vụ là `"Urgent"`. Ràng buộc này phải được xác thực chặt chẽ ở tầng Service để bảo đảm tính toàn vẹn nghiệp vụ và trải nghiệm khách hàng.
- **Rà soát Not-Null Constraints khi khởi tạo Entity thủ công:** Khi tạo mới một Entity bằng toán tử `new` để lưu xuống Database (đặc biệt trong các vòng lặp hay tác vụ tự động sinh), **BẮT BUỘC** phải rà soát kỹ lại tất cả các cột được định nghĩa là `nullable = false` trong model hoặc dưới schema (như các khóa ngoại `supervisor_id`, `staff_id`, `status`). Việc bỏ sót việc gán giá trị (`setter`) cho các trường này sẽ gây ra lỗi `constraint [null]` khiến toàn bộ Transaction bị rollback. Ưu tiên tạo các hàm helper/Builder chung để khởi tạo thay vì `new` chay rải rác.
- **Lỗi LazyInitializationException khi Render Template (Thymeleaf/Email) trong Background Job/Async:** Khi nạp danh sách Entity có chứa quan hệ lười (Lazy Loading) để truyền vào Template Engine (như Thymeleaf) để sinh HTML/Email ở các luồng ngầm (Scheduler/Async), **BẮT BUỘC** phải gắn `@Transactional(readOnly = true)` lên phương thức chạy luồng đó. Nếu không, Hibernate Session sẽ bị đóng ngay sau khi query xong, dẫn đến văng lỗi `LazyInitializationException` khi Thymeleaf cố truy cập thuộc tính con (ví dụ: `schedule.shift.shiftName`) và làm đứt gãy tiến trình.
- **Tránh lỗi ghi đè dữ liệu trạng thái Entity trong Hibernate Transaction (Dirty Checking):** Khi thay đổi trạng thái của một thực thể (Entity) trước khi kích hoạt Workflow Engine hoặc Event Publisher chạy đồng bộ trong cùng transaction, hãy luôn thay đổi (hoặc giải phóng/set null) các trường trạng thái liên đới **trước khi** phát đi sự kiện. Nếu không, Workflow Engine/Event Handler có thể load thực thể từ Database lên (khi đó vẫn mang trạng thái cũ) và gọi `save` ngay lập tức, dẫn đến việc Hibernate ghi đè trạng thái cũ lên thay đổi mới của bạn khi transaction commit.
- **Phòng thủ dữ liệu đầu vào trống (Fallback Default Description):** Đối với các API tiếp nhận yêu cầu dịch vụ hoặc sự cố từ khách hàng mà giao diện UI phía khách hàng chưa có ô nhập nội dung (notes/description) hoặc gửi lên trống rỗng, Backend bắt buộc phải tự động phát hiện và gán một nội dung mặc định mang tính mô tả rõ ràng (ví dụ: `"Yêu cầu dọn dẹp phòng"`, `"Yêu cầu sửa chữa thiết bị"`). Tuyệt đối không để trống notes làm ảnh hưởng đến hiển thị Dashboard của nhân viên.
- **Ưu tiên luồng khẩn cấp trong Workflow Engine (Bypass workflow checks for emergencies):** Khi thiết kế các hành động tự động trong Workflow Engine (như tạo task từ sự kiện `ROOM_REPORT_DAMAGE`), luôn phải kiểm tra cờ khẩn cấp (`is_emergency`). Nếu là sự cố khẩn cấp (`isEmergency = true`), bắt buộc phải bỏ qua các bước trung gian (như tạo task `DAMAGE_CHECK` để định giá) để chuyển thẳng thành công việc sửa chữa thực tế `MAINTENANCE` với độ ưu tiên `Urgent` để đội kỹ thuật sửa chữa kịp thời, đảm bảo an toàn hệ thống và trải nghiệm khách hàng lưu trú.
- **Nhất quán trong luân chuyển trạng thái thực thể gốc:** Khi hoàn thành các công việc liên đới (như dọn phòng hoặc sửa chữa bảo trì), trạng thái cuối cùng của thực thể phòng phải dựa trên sự tồn tại của liên kết đặt phòng hiện tại (`currentBookingDetailId != null`) để đưa về đúng trạng thái có khách (`Occupied`) hoặc trống (`Vacant_Clean`). Tránh gán các trạng thái biến thể như `Occupied_Clean` khi các phân hệ khác (POS/KDS) chỉ lọc theo trạng thái gốc `Occupied`.
- **Thiết lập cơ chế Fallback tự động khi dữ liệu Database không khớp (Fallback Database State Sync):** Khi có thay đổi trong logic nghiệp vụ liên quan đến cách tính toán tài chính (ví dụ: chuyển từ cộng dồn `booking.depositAmount` sang lưu trữ lịch sử giao dịch `PaymentTransaction`), luôn có kịch bản phòng thủ (fallback) tự động kiểm tra sự tồn tại của các bản ghi lịch sử tương ứng. Nếu DB thực tế bị thiếu bản ghi (ví dụ dữ liệu seed cũ không có giao dịch đặt cọc), hệ thống phải tự động sinh giao dịch thay thế tương ứng thay vì bỏ qua, tránh gây lệch balance hiển thị (ví dụ dư nợ không về 0 sau checkout).
- **Chống tính trùng lặp cọc (Double-Counting Prevention):** Khi tính toán dư nợ, luôn kiểm tra xem cọc đã tồn tại dưới dạng giao dịch trong DB chưa trước khi cộng dồn thêm cọc từ thực thể gốc (booking), tránh làm sai lệch số tiền cần thanh toán hiển thị trên UI.
- **Tránh lỗi logic trạng thái thời gian thực của các thực thể vận hành (Time-based State Inconsistency):** Đối với các thực thể phát sinh theo lịch trình thời gian (như TourSchedule, RoomBooking, Shift...), trạng thái hiển thị của chúng (như 'Sắp diễn ra', 'Đang diễn ra', 'Đã hoàn thành') luôn phải được đối chiếu động với ngày giờ hiện tại (`LocalDate.now()` / `LocalDateTime.now()`) nếu trạng thái lưu trữ trong cơ sở dữ liệu chưa kịp cập nhật. Tránh việc tin tưởng tuyệt đối vào trạng thái DB tĩnh khi thời gian đã trôi qua, gây vô lý dữ liệu trên giao diện người dùng.
- **Thiết kế nghiệp vụ hoàn tiền không đồng bộ (Asynchronous Refund/Folio logic):** Khi Resort/Dịch vụ chủ động hủy lịch trình hoặc dịch vụ đột xuất (ví dụ do sự cố), khách hàng không thể tự nhập thông tin tài khoản ngân hàng để nhận hoàn tiền ngay lập tức. Hãy luôn áp dụng thiết kế phân loại: Hoàn trả 100% vào Folio tài khoản phòng nghỉ (ghi nhận số tiền âm, loại department tương ứng) đối với khách đang lưu trú để tự động khấu trừ khi làm thủ tục Check-out; và tạo yêu cầu hoàn tiền (RefundRequest) trạng thái Pending đối với khách vãng lai để xử lý chuyển khoản thủ công sau. Điều này giúp tối ưu hóa luồng tài vụ và nâng cao trải nghiệm khách hàng.


## 4. QUY TRÌNH GIT AN TOÀN TRƯỚC KHI MERGE REQUEST (MR)
Khi làm việc trên một nhánh phụ (feature branch) và chuẩn bị tạo Merge Request xin gộp vào nhánh chính (vd: `dev`), **BẮT BUỘC** phải lấy code mới nhất từ nhánh chính gộp vào nhánh phụ để giải quyết mọi conflict ở môi trường local trước.
- **Cách Nhanh (Ưu tiên dùng hàng ngày):** Đứng trực tiếp tại nhánh phụ và chạy `git pull origin dev`. Việc này vừa kéo code mới từ remote `dev` vừa merge thẳng vào nhánh phụ, tiết kiệm thời gian gõ lệnh.
- **Cách Chậm (Sư phạm):** Checkout `dev` -> `git pull origin dev` -> checkout lại nhánh phụ -> `git merge dev`. Cách này an sau cho người mới nhưng tốn thao tác.
- **Hành động bắt buộc:** Sau khi pull/merge, nếu có conflict, phải mở IDE ra xử lý, lưu lại, `commit` rồi mới `push origin <nhánh_phụ>` và lên web tạo MR. Nếu làm đúng, MR sẽ luôn xanh (Able to automatically merge).
