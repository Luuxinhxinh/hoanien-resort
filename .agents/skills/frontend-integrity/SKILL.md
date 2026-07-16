---
name: frontend-integrity
description: Đảm bảo tính toàn vẹn của Frontend (Giao diện và Tương tác), kiểm tra kỹ class CSS, sự kiện JS, và payload khi thêm hoặc sửa đổi UI. Kích hoạt khi có thay đổi liên quan đến frontend (HTML, JS, CSS, Modals, Forms).
---

# Frontend Integrity Guidelines

## 1. CSS & Styling Checks
- Khi tạo mới một component (Modal, Button, Card...), **TUYỆT ĐỐI** không tự bịa ra class CSS. Phải `grep_search` hoặc đọc file HTML hiện tại để tái sử dụng các class có sẵn (ví dụ: `split-modal-overlay` thay vì `modal-overlay`).
- Kiểm tra trạng thái hiển thị ban đầu. Đối với Modal/Popup, phải có `style="display: none;"` nếu không sử dụng CSS class để ẩn mặc định.
- Luôn kiểm tra layout xem có gây vỡ giao diện trên các thiết bị hoặc gây xung đột với các elements lân cận không.

## 2. JavaScript & Interaction Checks
- Kiểm tra tên hàm (Function Name) khi gọi từ `onclick=""` hoặc Event Listener phải khớp chính xác với định nghĩa trong thẻ `<script>`.
- Đảm bảo các biến sử dụng trong script (ví dụ: `currentBookingId`, `currentPaymentRoomId`) đã được định nghĩa trong Global Scope hoặc được truyền vào đúng cách. TUYỆT ĐỐI KHÔNG tự định nghĩa hoặc suy đoán các biến toàn cục không tồn tại trong mã nguồn hiện có.
- **Tránh dùng ký tự escape (`\`) cho dấu nháy đơn/kép trong thuộc tính sự kiện HTML (như `onclick`, `onchange`):** Khi viết mã JS inline trong thuộc tính HTML (ví dụ: `onclick="window.location.href='/logout'"`), **TUYỆT ĐỐI KHÔNG** sử dụng dấu gạch chéo ngược (`\`) để escape các dấu nháy đơn hoặc nháy kép (như `onclick="window.location.href=\'/logout\'"`). HTML attribute parser không tự động xử lý/bỏ dấu escape này, khiến JavaScript engine trên trình duyệt nhận nguyên ký tự `\` ngoài chuỗi và ném lỗi cú pháp `Unterminated string literal` gây sập script.
- Khi tạo API Request gửi đi, bắt buộc phải kiểm tra trong file HTML/JS hiện có xem ID hay dữ liệu thực tế được quản lý dưới tên biến toàn cục nào (như `currentRoomBookingDetailId` thay vì tự đặt tên `currentBookingId`).
- Ràng buộc dữ liệu (Validation): Luôn kiểm tra đầu vào (Null, Empty, khoảng trắng) trước khi gửi Request. Có thông báo lỗi rõ ràng.
- Kiểm tra sự tồn tại của thư viện ngoài: Khi sử dụng các thư viện như SweetAlert2 (`Swal`), Toastr, Bootstrap... TRƯỚC HẾT phải kiểm tra xem file HTML hoặc file layout dùng chung có thực sự import thư viện đó chưa. Nếu chưa, hãy dùng native browser API (ví dụ `alert()`, `confirm()`) hoặc tự viết giao diện đơn giản thay vì giả định thư viện đã có sẵn.

## 3. Data & API Payload Integrity
- Khi Fetch API, kiểm tra phương thức (GET, POST, PUT, DELETE) có đúng với Backend không.
- Phân tách Endpoint theo Role (Guest vs Admin/Staff): Khi gọi API từ giao diện Guest/Customer, BẮT BUỘC kiểm tra xem URL endpoint có đúng là dành cho Guest không (thường có tiền tố `/guest/` hoặc được cấu hình `permitAll`/`hasRole('GUEST')`). Nếu gọi nhầm endpoint của Staff (ví dụ `/api/pos/orders/` thay vì `/api/pos/guest/orders/`), Spring Security sẽ chặn bằng HTTP 403 Forbidden, khiến frontend không parse được JSON lỗi chuẩn và rơi vào fallback hiển thị sai lệch "Lỗi hệ thống".
- Đối chiếu các Key trong JSON payload gửi đi có khớp 100% với DTO hoặc `Map<String, Object>` mà Backend Controller kỳ vọng hay không.
- Đối chiếu các giá trị (Enum values, Type strings) được định nghĩa ở thẻ `<select>` hoặc `<input>` của Frontend xem Backend có thực sự hỗ trợ xử lý và truy vấn giá trị đó hay không (ví dụ: `Manager_Approval` thay vì `Discount_Approval` để tránh trường hợp lưu thành công nhưng không hiển thị trên Dashboard).
- Xử lý các trạng thái Promise (then/catch) và hiển thị UI thông báo tương ứng (Loading, Success, Error).

## 4. Kiểm tra chéo (Cross-check) trước khi báo cáo hoàn thành
- Thay vì sử dụng danh sách kiểm tra độc lập ở đây, **BẮT BUỘC** gộp các tiêu chí kiểm tra (CSS hiển thị, biến lặp, payload null) vào mục **Kiểm tra toàn diện (Definition of Done)** tại file `context-first-quality.md` §4. Việc rà soát cuối cùng sẽ được thực hiện tại một nơi duy nhất.

## 5. End-to-End Flow & Data Consistency
- Đảm bảo bất kỳ luồng chức năng nào cũng phải thông suốt từ lúc bắt đầu (giao diện gửi yêu cầu) đến lúc kết thúc (nơi nhận/xem yêu cầu hiển thị và cập nhật trạng thái).
- Khi thêm hoặc chỉnh sửa trạng thái/dữ liệu ở một màn hình (ví dụ: gửi Yêu cầu Phê duyệt từ Lễ tân):
  - Kiểm tra xem các màn hình liên quan của người dùng khác (ví dụ: trang Duyệt yêu cầu của Manager) có hiển thị đúng, đủ các thông tin và trạng thái tương ứng hay không.
  - Đảm bảo không bị lệch kiểu dữ liệu hoặc giá trị nhãn (như enum, type string) khiến dữ liệu bị bỏ sót khi truy vấn lọc ở màn hình đích.
  - Khi thực thi các hành động phê duyệt/từ chối, các thay đổi trạng thái phải được cập nhật đồng bộ và chính xác xuống database cũng như các thực thể liên quan (ví dụ: trạng thái đơn đặt phòng Booking chuyển sang `Pending_Approval` khi gửi yêu cầu, và trả lại `Pending`/`CANCELLED` sau khi Manager xử lý).
- **[Quy tắc Ảo giác "Bảng rỗng" do Auto-Filter]:** Khi thiết kế màn hình danh sách (Danh sách Booking, Audit Log, History,...), **tuyệt đối không** tự động gán cứng giá trị mặc định cho các ô lọc (Ví dụ: `filterDate.value = new Date()`) ngay khi tải trang (Initial Load) trừ khi có yêu cầu nghiệp vụ bắt buộc. Việc này rất dễ che giấu dữ liệu cũ, làm sai lệch kết quả từ API (ví dụ API fallback trả data trống ngày) và khiến người dùng tưởng hệ thống bị lỗi "mất dữ liệu". Luôn ưu tiên để trống bộ lọc và hiển thị toàn bộ data ở lần tải đầu tiên.

## 6. Thymeleaf Template Integrity & Chunked Encoding Debugging
- **Tránh đóng block sớm (Premature Closure)**: Khi lặp với `<th:block th:each="...">`, thẻ đóng `</th:block>` phải được đặt ở cuối cùng, sau khi tất cả các thẻ con bên trong nó (ví dụ: thẻ bao `booking-item`, `details`, v.v.) đã đóng hoàn toàn.
- **Phạm vi của biến lặp**: Hãy chắc chắn rằng biến lặp (ví dụ: `booking.id`) chỉ được truy cập ở bên trong phạm vi lặp của `th:block`. Truy cập ngoài phạm vi sẽ dẫn đến biến bị `null` ở Server-side, làm gián đoạn luồng HTTP (`ERR_INCOMPLETE_CHUNKED_ENCODING`).
- **Tránh trùng lặp vòng lặp**: Không khai báo lặp lại biến vòng lặp trùng tên lồng nhau (như thẻ cha lặp `booking` và thẻ con cũng lặp `booking`).
- **Khắc phục sự cố tải thiếu trang**: Khi màn hình tải dở dang và bị đứng/không điều hướng được tab, hãy tìm ngay log lỗi Java tại backend Server hoặc viết/chạy Unit Test MockMvc render view để định vị dòng lệnh bị ném ngoại lệ trong template thay vì cố debug JS client.

## 7. String Normalization & Client-Side Mapping
- **Chuẩn hóa chuỗi trước khi so khớp:** Khi sử dụng các chuỗi động lấy từ Server/Database (như tên thực thể, loại tour, tên phòng) để tra cứu dữ liệu tĩnh trên giao diện khách hoặc nội bộ, bắt buộc phải viết hàm chuẩn hóa ký tự (`normalizeKey`) để loại bỏ dấu tiếng Việt, ký tự đặc biệt, chuyển thành chữ thường và xóa khoảng trắng dư thừa trước khi so khớp.
- **Ánh xạ từ khóa linh hoạt:** Cấu hình bộ quy tắc so khớp lỏng (`includes` hoặc regex) thay vì so khớp tuyệt đối (`===`) để hỗ trợ tìm kiếm theo cả từ khóa rút gọn, giúp giao diện không bị lỗi hoặc hiển thị trống khi dữ liệu trên DB có thay đổi nhỏ về tiêu đề.
- **Cơ chế Fallback an toàn:** Khi so khớp dựa trên chuỗi văn bản không thành công (không tìm thấy key phù hợp), **BẮT BUỘC** phải có cơ chế fallback tự động sử dụng thuộc tính định danh chính gốc từ database (như ID, loại định danh `tourType`, `roomCategoryCode`) được nạp sẵn để hiển thị thông tin chính xác, tránh việc giao diện im lặng bỏ qua (silent failure) hoặc hiển thị trống rỗng.
- **Tránh ghi đè mảng theo chỉ mục (Index Shifting):** Khi hợp nhất danh sách dữ liệu động từ Server (ví dụ: danh sách hoạt động tour bao gồm cả điểm đón/trả) vào danh sách tĩnh chứa siêu dữ liệu UI (toạ độ chấm CSS, hình ảnh bản đồ...), **TUYỆT ĐỐI KHÔNG** gán đè đơn giản theo chỉ mục (`dynamicList[i]` → `staticList[i]`) nếu kích thước hai mảng khác nhau. Hậu quả: toạ độ và hình ảnh của phần tử giữa bị lệch sang phần tử sai, gây hiển thị sai vị trí trên bản đồ. **Giải pháp:** Dùng so khớp theo thuộc tính tương đồng (tiêu đề, ID hoặc từ khoá). Các phần tử đặc biệt (ví dụ: "Đón khách", "Trả khách") cần được map cứng (hard-coded) vào toạ độ xác định; phần tử còn lại so khớp linh hoạt với danh sách tĩnh để kế thừa đúng siêu dữ liệu UI.
- **Hiển thị phân bổ chi tiết dịch vụ phức tạp có cấu trúc phân cấp (Parent-Child Breakdown):** Khi thiết kế hiển thị chi tiết các dịch vụ phức tạp có phân bổ đối tượng con bên trong (ví dụ: F&B có món ăn cụ thể, Tour có người lớn/trẻ em/em bé):
  - Trên màn hình chi tiết Folio: Cần dùng bảng mở rộng (sub-table) hoặc dạng cây thụt đầu dòng (↳) để thể hiện chi tiết số lượng, đơn giá sau chiết khấu và thành tiền của từng loại đối tượng.
  - Trên Hóa đơn in tổng hợp (Print Preview): Cần đồng bộ cấu trúc phân cấp này bằng cách ánh xạ chúng vào danh sách các mục in (`groupedItems`) dưới dạng phần tử con (`children`) và cập nhật hàm sinh bảng in (`tbodyHtml`) để tự động vẽ các hàng con thụt lề, định dạng cột giá vé tương ứng (ví dụ: hiển thị "Miễn phí" cho các dịch vụ miễn phí như em bé).

## 8. Tránh rò rỉ dữ liệu qua biến toàn cục (Global Variable Cleanup)
- **Reset biến toàn cục ở đầu hàm load:** Khi viết/chỉnh sửa mã JavaScript trên giao diện chi tiết hoặc các màn hình dùng chung biến toàn cục (như `bookingGroupData`, `appliedPromoCode`, `globalDeposit`), bắt buộc phải reset sạch sẽ các biến này về giá trị mặc định (`null`, `0`, `{}`) ở đầu hàm `fetch/load` dữ liệu mới.
- **Rủi ro rò rỉ dữ liệu:** Nếu không reset, khi người dùng chuyển nhanh giữa các bản ghi khác nhau (ví dụ: đổi từ xem chi tiết Booking của khách A sang khách B), dữ liệu của khách cũ (như mã giảm giá hoặc tiền cọc đã nạp) vẫn bị giữ lại trong bộ nhớ client và đè lên cách tính toán/hiển thị của khách mới, gây sai lệch nghiêm trọng thông tin thanh toán.

## 9. Thymeleaf Fragment Scope — Quy tắc cứng (BẮT BUỘC)

> **Bài học từ bug thực tế:** Modal đặt sai ngoài fragment → build thành công → runtime hoàn toàn im lặng → nút không có tác dụng.

- **Trước khi báo Done với bất kỳ element HTML nào (Modal, Button, Form...)**, PHẢI trả lời câu hỏi: *"Element này đến tay người dùng qua đường nào?"*
  - Nếu nằm trong một file `fragments/xxx.html` → phải nằm **bên trong** `th:fragment="tên"` được gọi qua `th:replace="~{...:: tên}"`.
  - Nếu muốn luôn render (không phụ thuộc fragment) → đặt trực tiếp trong file layout/template cha.
- **Trace bắt buộc:** Mở file template cha (ví dụ `master-data.html`), grep tên fragment (`:: tên`), xác nhận element đang thêm nằm **bên trong** fragment đó. Không tìm thấy → element **KHÔNG tồn tại trong DOM**.
- **Modal đặc biệt:** Modal nên đặt trong template cha (cùng nơi với các modal khác như `delete-modal`, `entity-modal`) thay vì trong fragment, trừ khi fragment đó được include dưới dạng `th:insert` (không phải `th:replace` một phần).
- **Kiểm tra nhanh bằng grep:** Sau khi thêm element, chạy `grep_search` với `id="element-id"` trên toàn bộ file template cha để xác nhận nó xuất hiện sau khi Thymeleaf xử lý.

## 10. E2E Trace Checklist — Bắt buộc trước khi báo Done với tính năng có Modal/API

> **Mục tiêu:** `mvn compile` chỉ bắt lỗi Java. Lỗi giao diện và logic luồng phải trace thủ công theo 5 mắt xích sau:

| Mắt xích | Câu hỏi phải trả lời được | Cách kiểm tra |
|---|---|---|
| **1. HTML/DOM** | Element có thật trong DOM không? | Trace `th:replace`/`th:insert` từ template cha |
| **2. JS Binding** | Event listener tìm thấy element không? | `getElementById("id")` → null là lỗi; grep tên hàm xem có định nghĩa không |
| **3. API Call** | URL, method, body có khớp Controller không? | So `fetch("/path/${id}")` vs `@GetMapping("/path/{id}")`, kiểm tra format id (E-5 vs 5) |
| **4. Backend** | Service/Repository xử lý được không? | Đọc method service, kiểm tra exception path |
| **5. Response → UI** | JS nhận response và cập nhật UI đúng không? | Đọc `.then(res => ...)`, kiểm tra field name khớp |

Nếu bất kỳ mắt xích nào chưa được kiểm tra → **KHÔNG được báo Done**.

## 11. Phân biệt giao dịch tài chính (Financial Transaction Consistency)
- **Tuyệt đối không gộp nhóm giao dịch chỉ bằng dấu (+/-):** Khi Frontend nhận danh sách `FolioItem` (hoặc Transaction) từ Backend, nếu cần tính tổng tiền nạp, tiền cọc, hoặc tiền hoàn (Refund), **KHÔNG ĐƯỢC** quét mọi khoản tiền âm (`amount < 0`) rồi tự động cộng dồn vào cùng một biến hiển thị. Điều này sẽ dẫn đến việc cộng nhầm Tiền cọc phòng (Pre-paid Deposit) với Tiền nạp hạn mức (Credit Deposit) và gây hiện tượng "Double-count" (cấn trừ đúp).
- **Phân loại dựa trên Metadata/Description:** Luôn phải dựa vào thuộc tính `description` (ví dụ chứa cụm từ `"nạp tiền nâng hạn mức"`), `sourceDepartment`, hoặc `transactionType` để lọc chính xác đúng loại giao dịch cần hiển thị trên UI. Sự phân loại này trên Frontend **PHẢI** luôn khớp 100% với điều kiện truy vấn tại Backend Repository (ví dụ: `FolioItemRepository.findCreditDepositAmountsByDetailId`).

## 12. Thymeleaf SpEL Field-Name Mismatch — EL1008E (BẮT BUỘC ĐỌC TRƯỚC KHI VIẾT TEMPLATE)

> **Bài học từ bug thực tế (2026-07-09):** Trang `/profile` bị đứt ngang HTML, mất toàn bộ tab, script và modal. Root cause là field name trong template sai so với entity Java, gây `SpelEvaluationException` khi render.

### Cơ chế sập trang (Mid-render Crash)
Khi Thymeleaf ném exception giữa chừng (Response đã `committed`):
- Server **KHÔNG THỂ** redirect về trang lỗi 500.
- Trình duyệt nhận HTML bị cắt cụt tại đúng dòng lỗi.
- **Hậu quả UI:** Trang render một nửa, mất hết tab/script/modal, không click được Header nav, trông như bị "đơ/đứng" — thực chất là DOM hỏng.

### Hai lỗi EL1008E đã gặp

| Field sai trong Template | Entity thực tế | Field đúng |
|---|---|---|
| `${rg.guestName}` | `com.kawai.models.RoomGuest` | Không có `guestName`; dùng `rg.dependent.dependentName` hoặc `rg.customer.fullName` |
| `${fItem.itemType}` | `com.kawai.models.FolioItem` | Không có `itemType`; dùng `fItem.sourceDepartment` |

### Quy tắc phòng thủ — TRƯỚC KHI viết `${obj.someField}` trong Thymeleaf

1. **Luôn xác minh field name từ Java entity**, không đoán mò:
   ```bash
   # Chạy lệnh này để xem tất cả field thực tế của entity
   Get-Content "src/main/java/com/kawai/models/TênEntity.java" | Select-String "(private|public.*get)"
   ```
2. **Các entity hay bị nhầm field name trong project này:**
   - `RoomGuest` → KHÔNG có `guestName`; tên khách phải lấy từ `rg.dependent.dependentName` (nếu là người thân) hoặc `rg.customer.fullName` (nếu là khách đăng ký).
   - `FolioItem` → KHÔNG có `itemType`; tên loại phải lấy từ `fItem.sourceDepartment`.
   - `Dependent` → KHÔNG có `fullName`; tên đúng là `dependent.dependentName`.
3. **Luôn kiểm tra null trước khi gọi chained property:**
   ```html
   <!-- SAI: Nếu rg.dependent == null sẽ crash -->
   th:text="${rg.dependent.dependentName}"
   <!-- ĐÚNG: Có null-guard -->
   th:text="${rg.dependent != null ? rg.dependent.dependentName : rg.customer.fullName}"
   ```

### Kỹ thuật debug Mid-Render Crash (Bypass Login)

Khi trang đứt giữa chừng nhưng log server bị ẩn, thêm **diagnostic endpoint** để render không cần login và gọi kiểm tra từ PowerShell:

**Bước 1 — Thêm endpoint test vào ProfileController:**
```java
// TẠM THỜI — XÓA SAU KHI FIX XONG
@GetMapping("/profile-test")
public String viewProfileTest(Model model) {
    Customer customer = customerRepository.findById(1L).orElse(null);
    // Gọi cùng hàm render logic với viewProfile
    return renderProfileInternal(customer, model);
}
```

**Bước 2 — Cho phép truy cập trong SecurityConfig (permitAll):**
```java
"/profile", "/profile/profile-test",  // thêm vào dòng này
```

**Bước 3 — Gọi thử và lưu HTML output:**
```powershell
$env:SERVER_PORT="8081" ; .\mvnw.cmd spring-boot:run
# Sau khi server khởi động xong:
Invoke-WebRequest -Uri "http://localhost:8081/profile/profile-test" -UseBasicParsing | Select-Object -ExpandProperty Content | Out-File "target\test-render.html"
```

**Bước 4 — Đọc log để tìm exception gốc:**
```powershell
Get-Content "<log-path>.log" | Select-String -Pattern "(SpelEvaluationException|EL1008|TemplateProcessingException|Caused by)" -Context 0,5
```

**Bước 5 — Xác nhận fix thành công:**
```powershell
# Nếu render hoàn chỉnh, output sẽ là hàng nghìn dòng HTML
$res = Invoke-WebRequest -Uri "http://localhost:8081/profile/profile-test" -UseBasicParsing
Write-Host "Lines: $($res.Content.Split([Environment]::NewLine).Length)"
```

**Bước 6 — Dọn dẹp:** Xóa endpoint test và revert SecurityConfig sau khi fix xong.

## 13. Tự động liên kết dữ liệu thiếu ID (Auto-association Fallback)
- **Bài học từ lỗi gửi feedback**: Khi khách hàng gửi đánh giá hoặc gửi dữ liệu từ một biểu mẫu chung (không đi qua đường dẫn chứa ID của bản ghi cụ thể), hãy thực hiện cơ chế tự động tìm kiếm đối tượng đã hoàn thành gần nhất của khách hàng đó từ cơ sở dữ liệu để tự động thiết lập liên kết (`tourBooking`, `roomBooking`, v.v.). Điều này đảm bảo dữ liệu luôn có liên kết hợp lệ, tránh việc các câu truy vấn lọc chặt chẽ (`IS NOT NULL`) vô tình bỏ qua bản ghi này.

## 14. Quy tắc loại trừ chọn lọc khi ép font chữ hệ thống
- **Khi sử dụng selector `*` để thay đổi font chữ toàn diện**:
  - **Bắt buộc** loại trừ font icon hệ thống (ví dụ: `:not(.material-symbols-outlined)`) để tránh làm mất hiển thị của icon.
  - **Bắt buộc** loại trừ các phần tử mang thương hiệu (như `.brand`, `.logo-text`, `.resort-name`, v.v. và các phần tử hiển thị tên thương hiệu) và thiết lập lại font-family, font-style đứng (`normal`), font-weight chuẩn cho chúng nhằm bảo vệ tính nhận diện thương hiệu nguyên bản của dự án.

## 15. Đồng bộ và bảo toàn số lượng thành viên đi tour khi gán phòng (Post to Room)
- **Phía Frontend:** Số người phân bổ gán cho các phòng luôn phải mặc định và bị giới hạn khớp hoàn toàn với số lượng người đăng ký đi tour tại form chính. Tuyệt đối không tự động lấy toàn bộ số người ở phòng gán để ghi đè lên đơn đặt tour.
- **Phía Backend:** Không tự động ghi đè số người đi tour của request bằng tổng số lượng người đặt phòng gán, trừ khi có logic phân bổ cụ thể được người dùng xác nhận và validate trùng khớp từ frontend gửi lên.

## 16. Quy trình Hủy đặt chỗ (Booking Cancellation Flow) từ khách hàng
- **Xác nhận UI:** Khi khách hàng bấm hủy đơn dịch vụ (như đặt tour) tại trang cá nhân, bắt buộc hiển thị popup xác nhận rõ ràng (ví dụ qua SweetAlert2) để tránh bấm nhầm.
- **Gửi Email đồng bộ:** Email thông báo hủy tour gửi về cho khách hàng phải sử dụng chung giao diện, CSS và các thông tin chi tiết hóa đơn (Breakdown) giống hệt email đặt tour thành công ban đầu, chỉ cập nhật trạng thái đơn thành 'Đã hủy' và hiển thị rõ số tiền được hoàn cọc.

## 17. Truyền dữ liệu Thymeleaf sang JavaScript qua HTML5 Data-Attributes
- **Vấn đề:** Khi muốn truyền các chuỗi ký tự động (có chứa nháy đơn, nháy kép, xuống dòng từ DB) vào các hàm JavaScript trực tiếp trong thuộc tính sự kiện (ví dụ: `th:onclick="'openModal(' + ${r.text} + ')'"`), việc sử dụng các hàm giả định như `#strings.escapeJavaScript` sẽ làm crash bộ render template của Spring Boot, dẫn đến hiện tượng sập trang (trắng trang).
- **Giải pháp (BẮT BUỘC):**
  1. Tránh truyền chuỗi thô qua biểu thức nối chuỗi của Thymeleaf trong các thẻ sự kiện JS.
  2. Lưu trữ dữ liệu động vào các thuộc tính HTML5 custom data-attributes (`th:data-id`, `th:data-text`, `th:data-rating`).
  3. Sử dụng JavaScript thuần để bắt sự kiện (`onclick="openModalFromButton(this)"`) và trích xuất dữ liệu: `const text = btn.getAttribute('data-text')`. Cơ chế này đảm bảo an toàn chuỗi và tương thích 100% với trình phân tích cú pháp HTML của trình duyệt.

## 18. Hiệu ứng Domino của lỗi JavaScript & Thymeleaf (Script/Render Crash Domino)
Lỗi "Nút bấm không phản hồi" hoặc báo lỗi `Function is not defined` trên Console (như không chuyển được tab, không mở được modal) thường bị hiểu nhầm là do code của chính hàm đó sai. Thực tế, gốc rễ thường đến từ 2 nguyên nhân "hiệu ứng Domino":
- **Thymeleaf Render Crash (`ERR_INCOMPLETE_CHUNKED_ENCODING`):** Thiếu kiểm tra Null-safety trong SpEL (ví dụ: in ra `tb.schedule.tour.tourName` khi `tb.schedule` là null do data rác), Server sẽ văng lỗi NullPointerException và đứt gãy kết nối giữa chừng. HTML bị cắt cụt khiến trình duyệt không bao giờ tải tới đoạn `<script>` ở cuối trang.
  - **Quy tắc:** Luôn dùng Null-safety đầy đủ (VD: `${tb.schedule != null and tb.schedule.tour != null ? tb.schedule.tour.tourName : 'N/A'}`) khi duyệt mảng hoặc render đối tượng đa tầng có rủi ro null.
- **JavaScript Khởi tạo Crash:** Một hàm phụ (như reset form, init UI) chạy trong `DOMContentLoaded` hoặc ngay khi load trang cố gắng thao tác với một DOM element không tồn tại (do element đó bị ẩn đi bởi `th:if` ở một trạng thái khác của trang). Lỗi `Cannot set properties of null` xảy ra làm Javascript engine sập luồng thực thi ngay lập tức, bỏ qua mọi logic khai báo hàm hay gắn Event Listener ở phía sau.
  - **Quy tắc:** Luôn kiểm tra tồn tại của Element `if (!element) return;` trước khi thao tác (như `.value`, `.classList`), đặc biệt đối với các element phụ thuộc vào điều kiện render của Thymeleaf.
 
 
## 19. Giải quyết xung đột giao diện và nhất quán ngôn ngữ (UI/CSS & Language Conflict Resolution)
- **Ưu tiên thiết kế có tính thẩm mỹ cao hơn:** Khi giải quyết merge conflict giữa các phiên bản style (CSS/HTML) cũ và mới, luôn ưu tiên lựa chọn các style mềm mại hơn (ví dụ: `border-radius` lớn 8px/16px, shadow mờ) đúng theo định hướng thiết kế Premium của dự án thay vì giữ phong cách thô cứng cũ (ví dụ: border 1px thô, border-radius 2px/4px).
- **Căn chỉnh dữ liệu tài chính chuyên nghiệp:** Khi hiển thị số tiền thanh toán (Total Payments), luôn cấu hình căn lề phải (`text-align: right`) để thẳng cột và dễ so sánh giá trị theo hàng dọc, thay vì căn giữa hay căn trái tự do.
- **Nhất quán ngôn ngữ chính (Tiếng Việt) cho nút hành động:** Để tránh giao diện bị lai căng Anh-Việt lộn xộn, đối với các nút hành động tương tác chính (như `XEM CHI TIẾT`, `KIỂM TRA PHÒNG`), nếu trang web nội bộ đang dùng tiếng Việt thì bắt buộc phải dịch các text tiếng Anh từ incoming branch (như `VIEW DETAILS`) sang tiếng Việt tương ứng.
- **Bảo toàn cấu trúc thẻ đóng HTML và ngoặc Javascript:** Khi xóa các Git conflict markers (`<<<<<<< HEAD`, `=======`, `>>>>>>>`), phải kiểm tra thật kỹ các cặp thẻ đóng (`</td>`, `</tr>`, `</div>`) hoặc các dấu ngoặc đóng nhọn `})` của block JavaScript ở xung quanh để tránh làm crash DOM render hoặc sập JS Engine.


