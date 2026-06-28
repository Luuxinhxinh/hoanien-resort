# 📋 KẾ HOẠCH NÂNG CẤP ADMIN (100% PRODUCTION)

*Trạng thái: Chờ duyệt (Pending Approval)*

Theo nguyên tắc của Tech Lead, việc làm cùng lúc 4 tính năng lớn (Biểu đồ, Export, Bulk Action, Phân trang Server) là một **Epic (Gói công việc lớn)**. Nó chạm vào rất nhiều tầng kiến trúc của hệ thống.

Tuyệt đối **KHÔNG code ngay lập tức** để tránh gãy đổ hệ thống. Dưới đây là phân tích liên đới và giải pháp cho từng mục để bạn duyệt trước khi tôi bắt tay vào gõ code.

---

## 🛠️ BƯỚC 1: PHÂN TÍCH LIÊN ĐỚI (CHECKLIST CÁC FILE CẦN SỬA)

### 1. Vẽ Biểu đồ Dashboard (Chart.js)

* **Backend:**
  * `AdminViewService.java` & `AdminViewServiceImpl.java`: Thêm logic query doanh thu hoặc số lượng booking theo chuỗi thời gian (ví dụ: 7 ngày gần nhất).
  * `AdminController.java`: Bổ sung dữ liệu chuỗi thời gian vào Model.
* **Frontend:**
  * `dashboard.html`: Thêm thẻ `<canvas>`, import thư viện Chart.js qua CDN và viết script render biểu đồ.

### 2. Export Dữ liệu (Excel/CSV)

* **Backend:**
  * Tạo mới `ExportApiController.java` (hoặc nhúng vào REST Controller hiện tại).
  * Sử dụng thư viện như Apache POI (nếu là Excel) hoặc OpenCSV/String builder (nếu là CSV).
* **Frontend:**
  * `master-data.html`: Thêm nút "Xuất File" (Export).
  * `master-data.js` (hoặc script trong template): Viết hàm gọi API download file blob.

### 3. Thao tác Hàng loạt (Bulk Actions)

* **Backend:**
  * `AdminAccountRestController.java` (và các API tương đương): Thêm các endpoint dạng `POST /bulk-delete` hoặc `POST /bulk-toggle`. Nhận mảng `List<Long> ids`.
* **Frontend:**
  * `master-data.html`: Thêm 1 cột Checkbox ở đầu bảng (cột Check All trên header).
  * Thêm thanh công cụ (Toolbar) hiện ra khi có ít nhất 1 dòng được check (chứa nút Xóa/Khóa hàng loạt).
  * Script gom mảng ID và gọi API.

### 4. Phân trang Server & Lọc (Server-side Pagination)

* *Lưu ý:* Đây là tính năng thay đổi "khung xương" lớn nhất.
* **Backend:**
  * Các Repository (`AccountRepository`, `RoomRepository`...) phải kế thừa `PagingAndSortingRepository` và dùng đối tượng `Pageable`.
  * `AdminViewService`: Sửa `getMasterDataRows` từ trả về toàn bộ List sang trả về đối tượng `Page` (kèm theo tổng số trang, trang hiện tại).
* **Frontend:**
  * Giao diện hiển thị nút chuyển trang `[1] [2] [3]...`
  * Query string trên URL phải thêm `?page=0&size=10&sortBy=id`.

---

## 🛡️ BƯỚC 2: GIẢI PHÁP PHÒNG THỦ & XỬ LÝ TRƯỜNG HỢP BIÊN (EDGE CASES)

| Tính năng              | Trường hợp biên có thể gây sập (Edge Cases)                                                    | Cách Tech Lead xử lý (Giải pháp code)                                                                                                                                          |
| :----------------------- | :----------------------------------------------------------------------------------------------------- | :---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **1. Biểu đồ**  | Không có data trong khoảng thời gian được chọn -> API trả về mảng rỗng làm sập Chart.js. | Lót mảng mạc định`[0,0,0...]` và hiển thị nhãn "Chưa có dữ liệu" trên giao diện thay vì báo lỗi đỏ console.                                                   |
| **2. Export CSV**  | Nếu bảng có 100,000 dòng, query 1 lần sẽ gây tràn RAM (OOM - Out of Memory) sập Backend.      | Không dùng`findAll()`. Nếu data lớn, phải dùng `Cursor` hoặc phân trang ngầm để ghi file stream từ từ. Tạm thời giới hạn export 5000 dòng gần nhất.         |
| **3. Bulk Action** | Chọn xóa 10 ID, nhưng có 2 ID bị lỗi (do ràng buộc khóa ngoại DB).                           | Backend phải bọc trong Transaction. Hoặc trả về kết quả chi tiết: "Xóa thành công 8, thất bại 2 do đang có dữ liệu ràng buộc" thay vì ném lỗi 500 toàn cục. |
| **4. Pagination**  | User cố tình sửa URL`?page=-1` hoặc `?page=99999` (vượt quá dữ liệu).                     | Bọc Try-Catch và ép kiểu dữ liệu. Nếu page < 0 -> set về 0. Nếu page > max -> trả về mảng rỗng hoặc báo trang không tồn tại.                                      |

---

## 🎯 BƯỚC 3: XIN Ý KIẾN DUYỆT (USER APPROVAL)

Vì khối lượng công việc cực kỳ lớn, nếu làm cùng lúc 4 mục sẽ dễ gây conflict code và làm sập giao diện hiện tại của bạn. Tôi đề xuất chia làm 2 Giai đoạn (Sprint):

* **Sprint 1:** Làm **Tính năng 1 (Biểu đồ)** và **Tính năng 3 (Bulk Action - Checkbox)** trước. Đây là 2 thứ đập vào mắt người dùng rõ nhất để lấy điểm ngay.
* **Sprint 2:** Đục lại Backend để làm **Tính năng 5 (Phân trang Server)** và **Tính năng 2 (Export File)**.

**Bạn có đồng ý với Checklist và Giải pháp xử lý lỗi trên không? Hãy phản hồi "Duyệt Sprint 1" để tôi bắt đầu code ngay vào dự án của bạn!**
