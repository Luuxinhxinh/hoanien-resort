# 📊 Hướng dẫn Kết nối & Sử dụng Báo cáo Doanh thu (Manager)

**Kawai Retreat Resort & Hub** | SWP391 — Group 2  
Phiên bản: 1.0 | Cập nhật: 2026-07-18

---

## 📋 Mục lục

1. [Tổng quan hệ thống báo cáo](#1-tổng-quan-hệ-thống-báo-cáo)
2. [Yêu cầu tiên quyết](#2-yêu-cầu-tiên-quyết)
3. [Hướng dẫn kết nối từng bước](#3-hướng-dẫn-kết-nối-từng-bước)
4. [Các trang báo cáo & chức năng](#4-các-trang-báo-cáo--chức-năng)
5. [API Endpoints (cho Developer)](#5-api-endpoints-cho-developer)
6. [Lỗi hay gặp & Cách fix](#6-lỗi-hay-gặp--cách-fix)
7. [Checklist kiểm tra nhanh](#7-checklist-kiểm-tra-nhanh)

---

## 1. Tổng quan hệ thống báo cáo

Hệ thống cung cấp **4 loại báo cáo doanh thu** dành riêng cho tài khoản **Manager**:

| Báo cáo | URL | Mô tả |
|---|---|---|
| **Tổng quan Doanh thu** | `/manager/revenue` | Dashboard doanh thu hôm nay, 7 ngày, tháng hiện tại |
| **Doanh thu theo Ngày** | `/manager/revenue/daily` | Chi tiết 9 ngày gần nhất: Phòng / F&B / Tour |
| **Doanh thu theo Tháng** | `/manager/revenue/monthly` | So sánh tháng hiện tại vs cùng kỳ năm ngoái (YoY) |
| **Báo cáo USALI / Xuất Excel** | `/manager/export` | Xuất Excel định dạng chuẩn USALI (Phòng / F&B / Tour / KPI) |

**Nguồn dữ liệu tổng hợp:**
- 🛏️ **Doanh thu Phòng** — từ `room_booking` (đã Checked_In / Checked_Out)
- 🍽️ **Doanh thu F&B** — từ `food_order` (đã `isPaidInPos = true`)
- 🗺️ **Doanh thu Tour** — từ `tour_booking` (đã hoàn thành)

---

## 2. Yêu cầu tiên quyết

### 2.1 Hệ thống phải đang chạy

```
Backend Server:  http://localhost:8080
Database:        MySQL tại localhost:3306/kawai_db
```

### 2.2 Tài khoản đăng nhập

Tài khoản phải được gán **một trong các authority** sau:

| Authority | Quyền truy cập |
|---|---|
| `ROLE_MANAGER` | ✅ Toàn quyền xem & xuất báo cáo |
| `ROLE_ADMIN` | ✅ Toàn quyền xem & xuất báo cáo |
| `OP_ANALYTICS` | ✅ Xem báo cáo (quyền phân tích) |

> ⚠️ **Lưu ý:** Tài khoản `ROLE_RECEPTIONIST`, `ROLE_FB_STAFF`, `ROLE_TOURGUIDE`... **KHÔNG có quyền** truy cập các trang `/manager/**`. Sẽ bị redirect sang trang 403.

### 2.3 Điều kiện dữ liệu để báo cáo có số liệu

- Đã có booking ở trạng thái `Confirmed` hoặc `Checked_Out`
- Night Audit đã chạy ít nhất 1 lần (để có bản ghi `FolioItem` tiền phòng)
- F&B có đơn hàng với `isPaidInPos = true` (nhân viên đã chốt ca)
- Tour có booking đã hoàn thành

---

## 3. Hướng dẫn kết nối từng bước

### Bước 1 — Khởi động backend server

```bash
# Di chuyển vào thư mục backend
cd 05-Development/kawai-backend

# Build và chạy
mvn spring-boot:run
```

Chờ console log hiển thị:
```
Started KawaiBackendApplication in X.XXX seconds (JVM running for Y.YYY)
Tomcat started on port(s): 8080 (http) with context path ''
```

### Bước 2 — Truy cập trang đăng nhập

Mở trình duyệt và điều hướng đến:

```
http://localhost:8080/login
```

### Bước 3 — Đăng nhập với tài khoản Manager

Điền thông tin tài khoản Manager vào form đăng nhập. Sau khi thành công, hệ thống **tự động redirect** đến:

```
http://localhost:8080/manager/dashboard
```

> 💡 **Nếu bị redirect sang trang khác** (ví dụ `/receptionist/dashboard`) → tài khoản không có role Manager. Xem [Lỗi 1](#-lỗi-1--http-403-forbidden-khi-truy-cập-manager).

### Bước 4 — Truy cập Báo cáo Doanh thu

Từ sidebar menu Manager, chọn mục **"Doanh thu"** hoặc truy cập trực tiếp:

```
http://localhost:8080/manager/revenue
```

### Bước 5 — Chọn loại báo cáo cần xem

```
Tổng quan doanh thu:     http://localhost:8080/manager/revenue
Chi tiết theo ngày:      http://localhost:8080/manager/revenue/daily
Chi tiết theo tháng:     http://localhost:8080/manager/revenue/monthly
Xuất báo cáo USALI:      http://localhost:8080/manager/export
```

---

## 4. Các trang báo cáo & chức năng

### 4.1 Tổng quan Doanh thu — `/manager/revenue`

Hiển thị các KPI nhanh theo thời gian thực:

| KPI | Mô tả | Ghi chú |
|---|---|---|
| **Doanh thu hôm nay** | Tổng (Phòng + F&B + Tour) ngày hiện tại | Cập nhật real-time |
| **7 ngày qua** | Tổng doanh thu 7 ngày gần nhất | Rolling window |
| **So với hôm qua** | % tăng/giảm so với ngày hôm qua | Xanh = tăng, Đỏ = giảm |
| **Biểu đồ 7 ngày** | Biểu đồ cột doanh thu từng ngày | Clickable để xem chi tiết |

### 4.2 Doanh thu theo Ngày — `/manager/revenue/daily`

Bảng chi tiết **9 ngày gần nhất** với breakdown theo từng nguồn:

| Ngày | Doanh thu Phòng | Doanh thu F&B | Doanh thu Tour | Tổng ngày |
|---|---|---|---|---|
| dd/MM/yyyy | VND | VND | VND | **VND** |

### 4.3 Doanh thu theo Tháng — `/manager/revenue/monthly`

- **Tháng hiện tại:** Tổng từ ngày 1 tháng hiện tại đến hôm nay
- **Cả năm (YTD):** Tổng từ 01/01 đến cuối tháng hiện tại
- **YoY Growth:** % tăng/giảm so với cùng kỳ năm trước
- **Bảng chi tiết:** Từng tháng trong năm với doanh thu Phòng / F&B / Tour

### 4.4 Xuất báo cáo USALI — `/manager/export`

Cấu trúc báo cáo chuẩn ngành khách sạn (Uniform System of Accounts for the Lodging Industry):

```
I.   DOANH THU PHÒNG
       - Khách lẻ (Transient)            → revenue_code = ROOM_TRANSIENT
       - Khách đoàn (Group)              → revenue_code = ROOM_GROUP
     ─────────────────────────────────
     Tổng doanh thu phòng

II.  DOANH THU ẨM THỰC (F&B)
       - Đồ ăn (Food)                    → revenue_code = REV-FB-FOOD
       - Đồ uống (Beverage)              → revenue_code = REV-FB-BEVERAGE
       - Minibar                         → revenue_code = REV-FB-MINIBAR
       - Ăn uống tại phòng (Room Svc)    → revenue_code = REV-FB-ROOMSERVICE
     ─────────────────────────────────
     Tổng doanh thu F&B

III. DOANH THU BỘ PHẬN KHÁC
       - Tour du lịch
       - Dịch vụ Spa / Giặt là / Khác
     ─────────────────────────────────
     Tổng bộ phận khác

     ═════════════════════════════════
     TỔNG DOANH THU HOẠT ĐỘNG

IV.  KPIs (Chỉ số Hoạt động Chủ chốt)
       - Tỷ lệ lấp đầy (Occupancy %)
       - ADR — Giá phòng trung bình
       - RevPAR — Doanh thu trên phòng trống
       - TrevPAR — Tổng DT trên phòng trống
```

**Cách tải file Excel:**
1. Vào trang `/manager/export`
2. Chọn khoảng thời gian (From / To)
3. Nhấn nút **"Xuất Excel (.xlsx)"**
4. File tải về có tên: `usali-report-{from}-{to}.xlsx`

---

## 5. API Endpoints (cho Developer)

**Base URL:** `http://localhost:8080`

> 🔐 Tất cả endpoints yêu cầu **session cookie hợp lệ** (đăng nhập trước qua `/login`). Không có Bearer Token hay API Key riêng.

### 5.1 Lấy dữ liệu USALI dạng JSON

```http
GET /manager/api/reports/usali?from={YYYY-MM-DD}&to={YYYY-MM-DD}
```

**Ví dụ request:**
```bash
curl -b "JSESSIONID=<your_session_id>" \
  "http://localhost:8080/manager/api/reports/usali?from=2026-07-01&to=2026-07-18"
```

**Response 200 OK:**
```json
{
  "REV-ROOM":           45000000,
  "REV-ROOM-TRANSIENT": 30000000,
  "REV-ROOM-GROUP":     15000000,
  "REV-FB":             12000000,
  "REV-FB-FOOD":         8000000,
  "REV-FB-BEVERAGE":     4000000,
  "REV-TOUR":            8000000,
  "TOTAL":              65000000,
  "KPI-OCCUPANCY":          75.5,
  "KPI-ADR":            1200000,
  "KPI-REVPAR":          906000,
  "KPI-TREVPAR":        1082000
}
```

**Response 400 Bad Request:**
```json
{"message": "from must be before or equal to to"}
```

### 5.2 Xuất file Excel (.xlsx)

```http
GET /manager/api/reports/export?type=usali&from={YYYY-MM-DD}&to={YYYY-MM-DD}&format=xlsx
```

| Tham số | Bắt buộc | Giá trị | Mặc định |
|---|---|---|---|
| `type` | Không | `usali` | `usali` |
| `from` | **Có** | `YYYY-MM-DD` | — |
| `to` | **Có** | `YYYY-MM-DD` | — |
| `format` | Không | `xlsx` *(chỉ xlsx)* | `xlsx` |

**Response:** Binary stream `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`

---

## 6. Lỗi hay gặp & Cách fix

### ❌ LỖI 1 — HTTP 403 Forbidden khi truy cập `/manager/**`

**Triệu chứng:**
```
Bạn không có quyền truy cập trang này.
```

**Nguyên nhân gốc rễ:**
- Tài khoản đang đăng nhập không có authority `ROLE_MANAGER` hoặc `ROLE_ADMIN`
- Session đã hết hạn

**Cách fix:**
1. Đăng xuất → đăng nhập lại bằng đúng tài khoản Manager
2. Kiểm tra role trong DB:
   ```sql
   SELECT a.username, r.role_name
   FROM account a
   JOIN role r ON a.role_id = r.id
   WHERE a.username = 'ten_tai_khoan';
   ```
3. Nếu `role_name` không phải `Manager` → Nhờ Admin vào `/admin` để đổi role

---

### ❌ LỖI 2 — Tất cả số liệu doanh thu hiển thị 0

**Triệu chứng:**
- Các ô doanh thu hiện `0` hoặc `0M`
- Không có lỗi, trang load bình thường

**Nguyên nhân gốc rễ:**
- Night Audit chưa chạy → chưa sinh bản ghi `FolioItem` tiền phòng
- Chưa có dữ liệu booking/order trong khoảng thời gian được chọn

**Cách fix:**
1. Kiểm tra FolioItem:
   ```sql
   SELECT COUNT(*) FROM folio_item WHERE revenue_code LIKE 'ROOM%';
   ```
2. Kiểm tra đơn F&B đã tính doanh thu:
   ```sql
   SELECT COUNT(*) FROM food_order
   WHERE is_paid_in_pos = true AND status != 'CANCELLED';
   ```
3. Nếu `COUNT = 0` → Chạy Night Audit hoặc tạo test data
4. Chạy Night Audit thủ công: `POST /api/night-audit/run?date={YYYY-MM-DD}`

---

### ❌ LỖI 3 — HTTP 400 Bad Request khi gọi API USALI

**Triệu chứng:**
```json
{"message": "from must be before or equal to to"}
```

**Nguyên nhân gốc rễ:**
- Tham số `from` lớn hơn `to` (khoảng ngày không hợp lệ)

**Cách fix:**
```
✅ Đúng:  ?from=2026-07-01&to=2026-07-18
❌ Sai:   ?from=2026-07-18&to=2026-07-01
```

---

### ❌ LỖI 4 — Xuất Excel thất bại hoặc file tải về bị lỗi

**Triệu chứng:**
```
Hệ thống chỉ hỗ trợ xuất báo cáo định dạng Excel (.xlsx)
```
hoặc file `.xlsx` tải về không mở được.

**Nguyên nhân gốc rễ:**
1. Truyền `format=pdf` hoặc `format=csv` — hệ thống **chỉ hỗ trợ xlsx**
2. Dependency Apache POI thiếu hoặc sai version
3. Khoảng thời gian quá rộng gây OutOfMemoryError

**Cách fix:**
1. Luôn dùng `format=xlsx`
2. Kiểm tra `pom.xml` có đủ dependency:
   ```xml
   <dependency>
       <groupId>org.apache.poi</groupId>
       <artifactId>poi-ooxml</artifactId>
   </dependency>
   ```
3. Giới hạn khoảng thời gian xuất ≤ 12 tháng để tránh OOM

---

### ❌ LỖI 5 — Trang load chậm hoặc timeout khi xem báo cáo

**Triệu chứng:**
- Trang mất > 10 giây để load
- Browser hiển thị `ERR_EMPTY_RESPONSE` hoặc timeout

**Nguyên nhân gốc rễ:**
- Không có index trên cột ngày tháng → full table scan
- Nhiều booking/order → N+1 query tích lũy

**Cách fix ngắn hạn:**
- Thu hẹp khoảng thời gian xem báo cáo

**Cách fix dài hạn:**
```sql
CREATE INDEX idx_rbd_checkout  ON room_booking_detail (check_out_date);
CREATE INDEX idx_fo_created    ON food_order (created_at);
CREATE INDEX idx_tb_date       ON tour_booking (booking_date);
```

---

### ❌ LỖI 6 — Bị redirect về `/login` ngay sau khi vào trang báo cáo

**Triệu chứng:**
- Truy cập `/manager/revenue` → tự redirect về `/login`
- Đã đăng nhập nhưng không giữ được session

**Nguyên nhân gốc rễ:**
- Dùng trình duyệt **Incognito** — cookie session bị block
- Session server-side đã expire (timeout mặc định 30 phút)
- CSRF token không hợp lệ

**Cách fix:**
1. Thoát Incognito, dùng cửa sổ trình duyệt thường
2. Xóa cookie `localhost:8080` rồi đăng nhập lại
3. (Nếu cần session dài hơn) thêm vào `application.yml`:
   ```yaml
   server:
     servlet:
       session:
         timeout: 60m
   ```

---

### ❌ LỖI 7 — Doanh thu F&B hiển thị 0 dù đã có đơn hàng

**Triệu chứng:**
- Biết chắc có đơn F&B nhưng Dashboard vẫn hiện F&B = 0

**Nguyên nhân gốc rễ (từ code `FnBDailyReportServiceImpl`):**

Hệ thống **chỉ tính đơn hàng thỏa đủ 2 điều kiện đồng thời:**
```
✅ is_paid_in_pos = true   (đã hoàn tất vòng đời tài chính tại POS)
✅ status ≠ CANCELLED      (không bị hủy)
```

Nếu nhân viên F&B **chưa bấm "Chốt ca" / "Close Shift"** thì `is_paid_in_pos` vẫn là `false` → không được tính vào doanh thu.

**Cách kiểm tra:**
```sql
-- Tìm đơn F&B chưa được tính doanh thu
SELECT id, created_at, status, is_paid_in_pos, total_amount
FROM food_order
WHERE is_paid_in_pos = false
  AND status NOT IN ('CANCELLED', 'PENDING')
ORDER BY created_at DESC;
```

**Cách fix:**
- Nhân viên F&B vào trang POS → **Chốt ca (Close Shift / End of Day)**
- Sau khi chốt, hệ thống set `is_paid_in_pos = true` và doanh thu sẽ được cập nhật

---

## 7. Checklist kiểm tra nhanh

Sử dụng checklist này mỗi khi gặp sự cố kết nối đến báo cáo:

```
□ Backend server đang chạy tại http://localhost:8080 ?
□ MySQL database đang chạy tại localhost:3306/kawai_db ?
□ Đăng nhập bằng đúng tài khoản có role ROLE_MANAGER hoặc ROLE_ADMIN ?
□ Trình duyệt không ở chế độ Incognito ?
□ URL truy cập đúng (bắt đầu bằng /manager/revenue) ?
□ Khoảng thời gian báo cáo hợp lệ (from <= to) ?
□ Format xuất là xlsx (không phải pdf hay csv) ?
□ Night Audit đã chạy ít nhất 1 lần ?
□ Nhân viên F&B đã chốt ca (is_paid_in_pos = true) cho ngày cần xem ?
□ Có dữ liệu booking/order trong khoảng thời gian được chọn ?
```

---

## 📎 Tài liệu liên quan

| Tài liệu | Đường dẫn |
|---|---|
| Business Rules | `02-Requirement/BusinessRule.md` |
| SRS Document | `02-Requirement/SRS_Document_SWP391_G2.md` |
| System Messages | Downloads/system-messages-from-code.md |
| SecurityConfig | `.../config/SecurityConfig.java` (dòng 91) |
| ManagerController | `.../controllers/web/ManagerController.java` (dòng 377, 919, 951) |
| ManagerReportApiController | `.../controllers/api/ManagerReportApiController.java` |
| ReportServiceImpl | `.../services/impl/ReportServiceImpl.java` |
| FnBDailyReportServiceImpl | `.../services/impl/FnBDailyReportServiceImpl.java` (dòng 57-79) |

---

*Tài liệu được tổng hợp từ source code thực tế của hệ thống Kawai Retreat Resort & Hub.*  
*Cập nhật lần cuối: 2026-07-18*
