---
name: context-first-quality
description: Ràng buộc đầu ra toàn diện cho mọi request — thu thập đủ ngữ cảnh trước khi xử lý, đảm bảo tính toàn vẹn liên đới cho code/sửa đổi, hiển thị UI đúng & đủ, và tài liệu chi tiết theo mẫu. Kích hoạt với MỌI prompt mới.
---

# Skill: Context-First Quality Assurance

Skill này được kích hoạt cho **MỌI request mới**, bất kể nội dung là gì.

### 🔄 Agent tự phân loại request (BẮT BUỘC)

Agent KHÔNG được hỏi user "request thuộc loại nào?". Thay vào đó, **tự đọc và quyết định** dựa trên các tiêu chí sau:

| Loại | Tiêu chí nhận biết tự động | Action |
|---|---|---|
| **🔍 Chỉ đọc** | Request không yêu cầu thay đổi file — chỉ hỏi, debug, explain, document | Scan tối thiểu → trả lời |
| **🔧 Sửa nhỏ** | Request yêu cầu sửa nhưng scope rõ ràng, ≤2 file, ≤5 dòng thay đổi, không ảnh hưởng logic/DB | Scan scope hẹp → sửa luôn |
| **🐛 Fix bug** | Request yêu cầu "fix bug", "sửa lỗi", "bug ở..." — có thể nhỏ hoặc lớn | Scan scope trước. Nếu ảnh hưởng >2 file hoặc DB/logic/state → **tự động nâng lên Sửa lớn**, làm Impact Analysis. Nếu ≤2 file, 1 dòng fix → sửa luôn. |
| **🏗️ Sửa lớn** | Request yêu cầu thêm chức năng mới, sửa flow, thay đổi DB/model/service, >2 file hoặc >5 dòng | LÀM ĐẦY ĐỦ Impact Analysis + trình checklist cho user duyệt |
| **♻️ Refactor** | Không thêm tính năng, không sửa bug — tái cấu trúc code, đổi tên, tách class, chuẩn hóa | Nếu đổi interface/DB/API contract → treat như **Sửa lớn**. Nếu chỉ rename/extract method nội bộ → treat như **Sửa nhỏ**. |
| **📝 Tài liệu** | Request yêu cầu viết spec, test case, hướng dẫn, template | Làm theo mẫu → trình kết quả cho user review |
| **❓ Mơ hồ** | Request chỉ mô tả mong muốn, không nói rõ "sửa" hay "không sửa" — ví dụ: "Em muốn booking có thể hủy sau 24h" | Scan scope → trình Impact Analysis → hỏi user xác nhận ý định implement |
| **⚡ Multi-task** | Request gộp ≥2 yêu cầu khác loại — ví dụ: "Sửa bug login VÀ thêm export CSV" | Tách thành từng task riêng. Xử lý task nặng nhất trước. Mỗi task áp dụng loại tương ứng. |

**Cách agent quyết định:**
1. Đọc request → phân tích nội dung (từ khóa hành động: "sửa", "thêm", "tạo", "debug", "giải thích", "refactor"...)
2. Nếu có ≥2 yêu cầu khác nhau → xếp **Multi-task**, tách riêng xử lý tuần tự
3. Nếu không có từ khóa hành động rõ ràng → xếp **Mơ hồ**
4. Nếu còn nghi ngờ về scope → scan nhanh (search_files/list_definition_names) để xác định mức độ ảnh hưởng
5. Dựa trên kết quả scan → tự chọn loại

**Quy tắc hỏi user:**
- Về **kỹ thuật** (file nào cần đọc, cách implement): **KHÔNG HỎI** → tự scan để biết
- Về **business intent** (anh muốn X hay Y, có cần implement không): **ĐƯỢC PHÉP HỎI** — ngắn gọn, 1 câu

**Exception "làm luôn" mode:**
> Nếu user dùng từ "làm luôn", "triển khai ngay", "không cần hỏi", "cứ làm đi" → **SKIP Bước 2** (user duyệt Impact Analysis). Tuy nhiên vẫn PHẢI thực hiện Impact Analysis nội bộ, và báo cáo tóm tắt ở Bước 5 (Bàn giao).

---

## ⚡ CHIẾN LƯỢC TỐI ƯU TOKEN (BẮT BUỘC)

**Không đọc toàn bộ file một cách tràn lan.** Áp dụng các kỹ thuật sau để tiết kiệm token:

| Kỹ thuật | Mô tả | Khi nào dùng |
|---|---|---|
| **Scan ngữ cảnh nhanh** | Dùng `search_files` với regex để tìm pattern cụ thể thay vì đọc cả file | Khi cần kiểm tra xử lý 1 field/biến qua nhiều file |
| **Đọc 1 phần file** | Dùng `read_file` với `start_line`/`end_line` để đọc chỉ section cần | Khi file lớn, chỉ cần method/class cụ thể |
| **Đọc header/imports trước** | Đọc 10-30 dòng đầu file để biết dependencies, annotation | Khi chưa rõ file có liên quan hay không |
| **List định nghĩa** | Dùng `list_code_definition_names` để xem nhanh cấu trúc file | Khi cần hiểu tổng quan file không quen |
| **On-demand reading** | Chỉ đọc file khi thực sự cần sửa, không đọc trước | Khi scope ảnh hưởng rộng nhưng chỉ sửa 1-2 file |

**Nguyên tắc token:**
- Ưu tiên `search_files` + regex hơn `read_file` nguyên cả file
- Nếu file >100 dòng, không đọc toàn bộ trừ khi bắt buộc
- Dùng `list_code_definition_names` trước để định hướng, đọc chi tiết sau
- Nếu không chắc file nào liên quan → scan trước (`search_files`), không hỏi user về kỹ thuật

**Thứ tự ưu tiên giữa các section (khi xung đột):**
> Bảng phân loại (Section đầu) → Chiến lược token → Impact Analysis → Tiêu chuẩn đầu ra → Quy trình
> Nếu 2 quy tắc mâu thuẫn: quy tắc nào **specific hơn** (gần với loại request hơn) thì ưu tiên hơn.

---

## 1. PHÂN TÍCH TÁC ĐỘNG & THU THẬP NGỮ CẢNH (BẮT BUỘC)

Trước khi sửa bất kỳ dòng code nào, PHẢI thực hiện **phân tích tác động (Impact Analysis)** như sau:

### Bước 1A: Xác định scope nghi ngờ
- Dùng `search_files` với từ khóa liên quan đến request
- Dùng `list_code_definition_names` cho các thư mục nghi vấn

### Bước 1B: Xác nhận scope ảnh hưởng
- Đọc header/imports (tối đa 30 dòng đầu) của file nghi ngờ
- Nếu thấy có liên quan → mới đọc chi tiết phần cần sửa
- Nếu không liên quan → bỏ qua, không đọc tiếp

### Bước 1C: Đọc phần cần thiết
- Đọc cụ thể method/class cần sửa bằng `start_line`/`end_line`
- Đọc file liên đới (Repository, DTO, v.v.) chỉ phần được gọi

> Luôn tự hỏi: "Mình có cần đọc cả file này không, hay chỉ cần 1 method?"

---

## 2. MANDATORY IMPACT ANALYSIS CHECKLIST (BẮT BUỘC)

Sau khi xác định scope, **PHẢI trình bày checklist này với user trước khi code** (trừ khi user đã nói "làm luôn" — xem Exception ở trên).

**Format chuẩn khi trình bày** (luôn dùng đúng format này, không tự ý thay đổi):

```
## 📋 Impact Analysis — [Tên chức năng / Bug]

**Loại request:** 🏗️ Sửa lớn  
**File ảnh hưởng chính:** [liệt kê file]

| Layer | Ảnh hưởng? | Ghi chú |
|---|---|---|
| Entity/Model | ✅ Có / ❌ Không | [mô tả nếu có] |
| Repository | ✅ Có / ❌ Không | |
| Service/Impl | ✅ Có / ❌ Không | |
| Controller | ✅ Có / ❌ Không | |
| DTO | ✅ Có / ❌ Không | |
| Validation | ✅ Có / ❌ Không | |
| Exception handling | ✅ Có / ❌ Không | |
| Frontend/JS | ✅ Có / ❌ Không | |
| Template (Thymeleaf) | ✅ Có / ❌ Không | |
| Email template | ✅ Có / ❌ Không | |
| Security/RBAC | ✅ Có / ❌ Không | |
| Async/Job | ✅ Có / ❌ Không | |
| Audit/Log | ✅ Có / ❌ Không | |
| Test | ✅ Có / ❌ Không | |

**Kế hoạch thực hiện:**
1. [Bước 1]
2. [Bước 2]
...

➡️ Anh/chị xác nhận để em bắt đầu?
```

**Cách kiểm tra nhanh từng mục bằng search (tiết kiệm token):**

| Câu hỏi | Regex search |
|---|---|
| Class A được gọi ở đâu? | `A\.(methodName\|classSimpleName)` |
| Field X được dùng ở đâu? | `\bgetX\(\)\|\bsetX\(\)\|\bX\b` trong file liên quan |
| API endpoint Y ai gọi? | `"/api/.../y"` hoặc `'api/.../y'` |
| Template nào render data Z? | `th:text\|th:value\|th:each.*Z` |
| Interface/Impl không đồng bộ? | So sánh interface method với impl method |

> Nếu còn mục nào đánh dấu là nghi ngờ → search để verify. KHÔNG được bỏ qua.

---

## 3. TIÊU CHUẨN ĐẦU RA THEO LOẠI CÔNG VIỆC

### 3.1. Code mới / Sửa đổi code — Đảm bảo tính toàn vẹn liên đới

Khi thêm hoặc sửa chức năng, **không dừng lại ở file đang sửa**. Phải rà soát và cập nhật tất cả:

| Khía cạnh | Yêu cầu | Cách search verify |
|---|---|---|
| **Dữ liệu liên quan** | Mọi thay đổi dữ liệu ở chức năng A phải cập nhật các bảng/state liên quan | Search tên entity trong toàn project |
| **Trạng thái hệ thống** | Tất cả trạng thái liên đới phải được cập nhật đồng bộ | Search `status.*=` trong service/template |
| **API/Controller** | Nếu sửa Service → kiểm tra Controller có cần sửa response/status code không | Search `@RequestMapping\|@GetMapping\|@PostMapping` có gọi service |
| **Frontend** | Nếu API response thay đổi → cập nhật frontend (JS gọi API, state, UI render) | Search URL endpoint trong thư mục static/templates |
| **Repository/DB** | Nếu logic thay đổi → kiểm tra query/repo có cần sửa không | Search tên entity + Repository trong project |
| **Validation** | Kiểm tra validation đầu vào/đầu ra có cần cập nhật không | Search `@Valid\|@NotBlank\|@NotNull` trong DTO/Controller |
| **Exception handling** | Có cần thêm try-catch, xử lý lỗi mới không? | Search `try\|catch\|throw` trong method liên quan |

**Nguyên tắc vàng:** 
> "Một chức năng chỉ hoàn thành khi MỌI khối ảnh hưởng của nó đã được cập nhật đầy đủ và đồng bộ."

### 3.2. Giao diện (UI/Frontend) — Đảm bảo hiển thị đúng & đủ

| Tiêu chí | Mô tả |
|---|---|
| **Hiển thị không lỗi** | Không console error, không 404 assets, không layout shift |
| **Đầy đủ dữ liệu** | Tất cả field cần thiết đều hiển thị, không thiếu trường |
| **Đúng chức năng** | Click/input/submit hoạt động đúng business logic |
| **Tương tác hàm đúng mong đợi** | Event handler gọi đúng API, đúng tham số, xử lý response đúng |
| **Responsive (nếu cần)** | Hiển thị tốt trên các kích thước màn hình |
| **Loading/Error/Empty states** | Xử lý cả 3 trạng thái, không chỉ happy path |

### 3.3. Tài liệu — Chi tiết, đúng mẫu

| Yêu cầu | Mô tả |
|---|---|
| **Có mẫu (template) sẵn?** | PHẢI tuân theo mẫu có sẵn 100% (không bỏ section, không thay đổi format) |
| **Không có mẫu?** | Làm theo cấu trúc rõ ràng, đầy đủ thông tin |
| **Chi tiết, không hàng loạt** | Làm từng file/từng phần tử cụ thể, không gộp chung đại diện |
| **Ví dụ cụ thể** | Kèm ví dụ minh họa cho mỗi bước/hướng dẫn |

---

## 4. QUY TRÌNH LÀM VIỆC BẮT BUỘC

### Bước 1: Scan nhanh ngữ cảnh + Impact Analysis
- Dùng `search_files` + regex để xác định scope
- Dùng `list_code_definition_names` để định hướng
- **KHÔNG đọc cả file trừ khi cần**
- **Tạo Impact Analysis Checklist và show cho user**

### Bước 2: User duyệt → mới code
- Chờ user xác nhận Impact Analysis
- Nếu user góp ý thêm → cập nhật scope
- CHỈ khi user OK mới bắt đầu code

### Bước 3: Thực hiện — từng việc một
- Làm từng việc, kiểm tra xong mới chuyển
- CHỈ đọc file chi tiết khi bắt đầu sửa file đó
- KHÔNG đọc trước tất cả file rồi mới sửa

### Bước 4: Kiểm tra toàn diện
- Kiểm tra tính toàn vẹn liên đới (dùng search để verify)
- Kiểm tra edge cases
- Kiểm tra frontend (nếu có UI)
- Kiểm tra tài liệu (nếu có)

### Bước 5: Bàn giao
- Tóm tắt những gì đã làm
- Nêu rõ những gì chưa làm (nếu có)
- Nêu rủi ro tiềm ẩn (nếu có)