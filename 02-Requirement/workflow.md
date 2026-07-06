# BUSINESS WORKFLOWS

## Kawai Retreat Resort & Hub — Luồng Nghiệp vụ Hệ thống

**Phiên bản:** 1.0
**Ngày tạo:** 2026-06-29
**Chuẩn:** BPMN-inspired Mermaid Flowchart
**Nguồn:** BusinessRule.md · RequirementsTraceabilityMatrix.md · SRS_Document_SWP391_G2.md

---

## Mục lục

|   #   | Workflow                                                                                  | Phân hệ      | Mức độ |
| :---: | :---------------------------------------------------------------------------------------- | :------------- | :-------: |
| WF-01 | [Xác thực &amp; Đăng ký Tài khoản](#wf-01--xác-thực--đăng-ký-tài-khoản)      | Authentication |   HIGH   |
| WF-02 | [Đặt phòng &amp; Thanh toán Cọc](#wf-02--đặt-phòng--thanh-toán-cọc-trực-tuyến) | Front Office   | CRITICAL |
| WF-03 | [Check-in Tiền sảnh](#wf-03--check-in-tiền-sảnh)                                       | Front Office   | CRITICAL |
| WF-04 | [Check-out &amp; Tổng hợp Hóa đơn](#wf-04--check-out--tổng-hợp-hóa-đơn)          | Finance        | CRITICAL |
| WF-05 | [F&amp;B / POS / Post-to-Room](#wf-05--fb--pos--ghi-nợ-folio)                             | F&B            | CRITICAL |
| WF-06 | [Đặt Tour &amp; Điểm danh AI](#wf-06--đặt-tour--điểm-danh-ai)                      | Tour           | CRITICAL |
| WF-07 | [Room Status Lifecycle](#wf-07--room-status-lifecycle)                                     | Housekeeping   |   HIGH   |
| WF-08 | [Night Audit](#wf-08--night-audit-kiểm-toán-đêm)                                       | Finance        | CRITICAL |
| WF-09 | [Hủy Đặt phòng &amp; Hoàn tiền](#wf-09--hủy-đặt-phòng--hoàn-tiền)              | Front Office   |   HIGH   |
| WF-10 | [Hủy Tour &amp; Hoàn tiền Tự động](#wf-10--hủy-tour--hoàn-tiền-tự-động)        | Tour           |   HIGH   |
| WF-11 | [Đánh giá &amp; Kiểm duyệt](#wf-11--đánh-giá-dịch-vụ--kiểm-duyệt)              | Review         |  MEDIUM  |
| WF-12 | [Quản lý Nhân viên &amp; Phân quyền](#wf-12--quản-lý-nhân-viên--phân-quyền)    | Admin          |   HIGH   |
| WF-13 | [Master Flow — Vận hành Tổng thể](#wf-13--master-flow--vận-hành-tổng-thể)         | All            |    —    |
| WF-14 | [Walk-in Check-in](#wf-14--walk-in-check-in)                                       | Front Office   | CRITICAL |
| WF-15 | [Remote CCCD Scan](#wf-15--remote-cccd-scan)                                       | Front Office   |   HIGH   |
| WF-16 | [Tour Itinerary & GPS Checkpoint](#wf-16--tour-itinerary--gps-checkpoint)      | Tour           |   HIGH   |
| WF-17 | [Đổi Hạng Phòng](#wf-17--đổi-hạng-phòng)                                           | Front Office   |   HIGH   |
| WF-18 | [Membership Tier & Loyalty Points](#wf-18--membership-tier--loyalty-points)     | Membership     |  MEDIUM  |
| WF-19 | [Refund Request Flow (Manual)](#wf-19--refund-request-flow-manual)                 | Finance        |  MEDIUM  |
| WF-20 | [Add-On Hotel Service](#wf-20--add-on-hotel-service)                               | F&B            |  MEDIUM  |
| WF-21 | [Staff Scheduling](#wf-21--staff-scheduling)                                       | Admin          |  MEDIUM  |
| WF-22 | [Workflow Engine (Dynamic BPMN)](#wf-22--workflow-engine-dynamic-bpmn)             | System         |   HIGH   |
| WF-23 | [Đặt Bàn Trực Tuyến](#wf-23--đặt-bàn-trực-tuyến)                                 | F&B            |   HIGH   |
| WF-24 | [Hủy Đơn Hàng F&B & Hoàn Tiền](#wf-24--hủy-đơn-hàng-fb--hoàn-tiền)     | F&B            |   HIGH   |
| WF-25 | [Chốt Ca & Báo Cáo F&B](#wf-25--chốt-ca--báo-cáo-fb)                         | F&B            |   HIGH   |
| WF-26 | [Phê duyệt Thiết bị Vận hành](#wf-26--phê-duyệt-thiết-bị-vận-hành)               | Admin          |  MEDIUM  |

---

## WF-01 — Xác thực & Đăng ký Tài khoản

**Use Cases:** UC01, UC02, UC03
**Business Rules:** BR-SYS-01, BR-SYS-02, BR-SYS-06, BR-SYS-07
**Actors:** Guest, All Users, System

```mermaid
flowchart TD
    START([Bắt đầu]) --> Q1{Người dùng\ncó tài khoản?}

    %% ---------- REGISTER PATH ----------
    Q1 -->|Chưa có| REG[Form Đăng ký]
    REG --> R1[Nhập: email, password,\nfullName, phone, gender]
    R1 --> R2{Validate format:\nemail hợp lệ,\nphone đúng định dạng}
    R2 -->|Lỗi| R1
    R2 -->|OK| R3{Email đã\ntồn tại?}
    R3 -->|Đã tồn tại| ERR_DUP[Lỗi: Email đã\nđược sử dụng]
    ERR_DUP --> R1
    R3 -->|Chưa| R4{Mật khẩu đủ mạnh?\n≥8 ký tự, Hoa+Thường+Số\nBR-SYS-06}
    R4 -->|Yếu| ERR_PWD[Lỗi: Mật khẩu không\nđáp ứng yêu cầu]
    ERR_PWD --> R1
    R4 -->|Đạt| HASH[BCrypt hash password\nfactor=10 - BR-SYS-01]
    HASH --> INS[INSERT Accounts\n+ INSERT Customers\n@Transactional]
    INS --> MAIL[Gửi email chào mừng]
    MAIL --> LOGIN_FORM

    %% ---------- LOGIN PATH ----------
    Q1 -->|Đã có| LOGIN_FORM[Màn hình Đăng nhập]
    LOGIN_FORM --> L1[Nhập email + password]
    LOGIN_FORM --> L_OAUTH[Đăng nhập qua Google OAuth2]
    
    L_OAUTH --> L_OAUTH_CHK{Tài khoản\ntồn tại?}
    L_OAUTH_CHK -->|Chưa| L_OAUTH_REG[Tự động tạo Customer mới\nPassword: OAUTH2_GOOGLE_PLACEHOLDER\nisActive = true]
    L_OAUTH_REG --> JWT
    L_OAUTH_CHK -->|Rồi| L4
    
    L1 --> L2{Validate format}
    L2 -->|Lỗi| L1
    L2 -->|OK| L3{Email tồn\ntại trong DB?}
    L3 -->|Không| ERR_CRED[Sai email/mật khẩu]
    ERR_CRED --> L1
    L3 -->|Có| L4{Account\nActive?}
    L4 -->|Bị khóa/Inactive| ERR_LOCK[Tài khoản bị khóa\nhoặc vô hiệu hóa]
    ERR_LOCK --> DONE_FAIL([Kết thúc - Thất bại])
    L4 -->|Active| L5{BCrypt.verify\npassword}
    L5 -->|Sai| L6[failed_login_count++]
    L6 --> L7{Sai ≥ 5\nlần liên tiếp?\nBR-SYS-02}
    L7 -->|Có| LOCK[Khóa account 15 phút\nBR-SYS-02]
    LOCK --> ERR_LOCK
    L7 -->|Chưa| ERR_CRED
    L5 -->|Đúng| L8[Reset failed_login_count = 0]
    L8 --> L9{Thiết bị\nkhông quen?}
    L9 -->|Có| OTP_SEND[Gửi OTP 6 số\nTTL = 3 phút - BR-SYS-02]
    OTP_SEND --> OTP_INPUT[Nhập OTP]
    OTP_INPUT --> OTP_CHK{OTP hợp lệ\nvà chưa hết hạn?}
    OTP_CHK -->|Hết hạn / Sai| OTP_ERR[Lỗi OTP]
    OTP_ERR --> OTP_INPUT
    OTP_CHK -->|Đúng| JWT
    L9 -->|Không| JWT[Tạo JWT Token / Session]
    JWT --> AUDIT[INSERT Audit_Logs - BR-SYS-04]
    AUDIT --> ROUTE{Routing theo Role\nBR-SYS-07}
    ROUTE -->|Customer| D_CUST[Member Dashboard]
    ROUTE -->|Receptionist| D_REC[Room Matrix]
    ROUTE -->|F&B Staff| D_FB[Restaurant POS]
    ROUTE -->|Kitchen| D_KDS[KDS Dashboard]
    ROUTE -->|Housekeeping| D_HK[Housekeeping Tasks]
    ROUTE -->|Tour Guide| D_TG[Tour Schedules]
    ROUTE -->|Admin/Manager| D_ADM[Admin Panel]
    D_CUST & D_REC & D_FB & D_KDS & D_HK & D_TG & D_ADM --> DONE_OK([Kết thúc - Đăng nhập OK])

    style HASH fill:#ff9900,color:#fff
    style LOCK fill:#ff4444,color:#fff
    style JWT fill:#00aa44,color:#fff
    style INS fill:#0066cc,color:#fff
```

> **Business Rules áp dụng:**
>
> - `BR-SYS-01` — BCrypt hash password; AES-256 mã hóa CCCD
> - `BR-SYS-02` — Khóa 15 phút sau 5 lần sai; OTP TTL 3 phút; Google OAuth2 bỏ qua OTP
> - `BR-SYS-06` — Mật khẩu ≥ 8 ký tự, có Hoa + Thường + Số
> - `BR-SYS-07` — Routing theo RBAC role sau login


---

## WF-02 — Đặt phòng & Thanh toán Cọc trực tuyến

**Use Cases:** UC10, UC11, UC12.1
**Business Rules:** BR-FO-01, BR-FO-02, BR-FIN-06
**Actors:** Customer, System, VNPay Gateway

```mermaid
flowchart TD
    START([Khách tìm phòng]) --> B1[Nhập bộ lọc:\ncheckin / checkout / số khách]
    B1 --> B2{Validate ngày:\ncheckin >= hôm nay\ncheckout > checkin}
    B2 -->|Không hợp lệ| B1
    B2 -->|OK| B3[Query Room Availability\nSELECT...FOR UPDATE\nBR-FO-01]
    B3 --> B4{Có phòng\ntrống?}
    B4 -->|Không| NO_ROOM[Thông báo:\nHết phòng trong khoảng ngày]
    NO_ROOM --> DONE_NOROOM([Kết thúc])
    B4 -->|Có| B5[Hiển thị danh sách\nhạng phòng + giá Daily_Rates]

    B5 --> B6[Khách chọn hạng phòng\nvà số lượng phòng]
    B6 --> B7{Đã đăng nhập?}
    B7 -->|Chưa| B7L[Chuyển Login / Register]
    B7L --> B7
    B7 -->|Rồi| B8[Nhập: số khách,\nyêu cầu đặc biệt]

    B8 --> VOC{Nhập mã\nVoucher?}
    VOC -->|Có| VOC1[Kiểm tra Promotions:\nis_active, valid_to, max_uses\nBR-FIN-06]
    VOC1 -->|Hợp lệ| VOC2[Áp dụng giảm giá]
    VOC1 -->|Không hợp lệ| VOC_ERR[Lỗi: Voucher không\nhợp lệ hoặc hết hạn]
    VOC_ERR --> VOC
    VOC2 --> TOTAL
    VOC -->|Không| TOTAL[Tính tổng tiền\nvà tiền cọc 30%]

    TOTAL --> CONFIRM[Khách xác nhận\nthông tin booking]
    CONFIRM --> INS[INSERT Bookings status=Pending\n+ Room_Booking_Details]
    INS --> LOCK[Kích hoạt Cart Lock 2 phút\nBR-FO-02: TTL 2 phút]
    LOCK --> VNP[Tạo VNPay Payment URL\n+ HMAC Signature]
    VNP --> PAY_PAGE[Chuyển sang VNPay\nPayment Page]

    PAY_PAGE --> TIMEOUT{Khách thanh toán\ntrong 2 phút?}
    TIMEOUT -->|Quá 2 phút| SCHED[Scheduler phát hiện\nBooking Pending > 2 phút]
    SCHED --> CANCEL[UPDATE Bookings\nstatus = Cancelled]
    CANCEL --> FREE[Giải phóng Cart Lock]
    FREE --> NOTIFY_CANCEL[Thông báo: Booking hết hạn\ndo chưa thanh toán]
    NOTIFY_CANCEL --> DONE_TIMEOUT([Kết thúc - Hủy tự động])

    TIMEOUT -->|Trong 2 phút| VNPAY_PROC[VNPay xử lý giao dịch]
    VNPAY_PROC --> WEBHOOK{VNPay Webhook\nCallback}
    WEBHOOK -->|Thất bại| PAY_FAIL[Hiển thị lỗi thanh toán]
    PAY_FAIL --> PAY_PAGE
    WEBHOOK -->|Thành công| VERIFY[Verify HMAC Signature\nWebhook VNPay]
    VERIFY --> TX[INSERT Payment_Transactions\ntransaction_type=DEPOSIT]
    TX --> CONFIRMED[UPDATE Bookings\nstatus = Confirmed]
    CONFIRMED --> MAIL[Email xác nhận\nBooking thành công]
    MAIL --> DONE_OK([Kết thúc - Đặt phòng OK])

    style INS fill:#0066cc,color:#fff
    style LOCK fill:#ff9900,color:#fff
    style CANCEL fill:#ff4444,color:#fff
    style CONFIRMED fill:#00aa44,color:#fff
    style VERIFY fill:#ff9900,color:#fff
```

> **Business Rules áp dụng:**
>
> - `BR-FO-01` — SELECT...FOR UPDATE; @Version Optimistic Lock chống overbooking
> - `BR-FO-02` — Cart Lock 2 phút; Scheduler tự động hủy Booking Pending quá hạn
> - `BR-FIN-06` — 1 voucher/booking; kiểm tra is_active, valid_to, max_uses


---

## WF-03 — Check-in Tiền sảnh

**Use Cases:** UC12.2, UC12.3, UC12.4
**Business Rules:** BR-FO-03, BR-FO-04, BR-FO-06, BR-DATA-02
**Actors:** Receptionist, Customer

```mermaid
flowchart TD
    START([Khách đến Tiền sảnh]) --> S1[Lễ tân tìm Booking\ntheo tên / mã booking]
    S1 --> S2{Tìm thấy\nBooking?}
    S2 -->|Không| ERR1[Không tìm thấy\nđặt phòng]
    ERR1 --> S1
    S2 -->|Có| S3{Booking status\n= Confirmed?}
    S3 -->|Không| ERR2[Booking chưa xác nhận\nhoặc đã hủy]
    ERR2 --> DONE_FAIL([Kết thúc])

    S3 -->|Có| OCR[Upload ảnh CCCD\nlên OCR Service]
    OCR --> EXTRACT[OCR trích xuất:\nHọ tên, CCCD số,\nNgày sinh, Giới tính]
    EXTRACT --> FILL[Auto điền Form\nkhai báo lưu trú]
    FILL --> AGE{Người đại diện\n≥ 18 tuổi?\nBR-FO-03}
    AGE -->|Dưới 18| ERR3[Từ chối check-in:\nNgười đại diện phải ≥ 18 tuổi]
    ERR3 --> DONE_FAIL

    AGE -->|Đủ tuổi| GUEST[Khai báo danh sách\nngười lưu trú\nCustomers + Dependents\nBR-DATA-02]
    GUEST --> ROOM_SEL[Lễ tân chọn phòng\nvật lý từ Room Matrix]
    ROOM_SEL --> ROOM_CHK{Phòng được chọn\nstatus = Vacant_Clean?\nBR-FO-04}
    ROOM_CHK -->|Không| ERR4[Cảnh báo:\nPhòng không sẵn sàng\nChọn phòng khác]
    ERR4 --> ROOM_SEL
    ROOM_CHK -->|Có| ASSIGN[Gán Phòng vào\nRoom_Booking_Details]

    ASSIGN --> CL[Thiết lập Credit Limit\nvà PIN cho phòng\nBR-FO-06]
    CL --> CL2[Nhập sub_credit_limit]
    CL2 --> CL3{sub_credit_limit ≤\nbooking credit_limit?\nBR-FO-06}
    CL3 -->|Vượt| ERR5[Vượt hạn mức nợ tổng]
    ERR5 --> CL2
    CL3 -->|OK| PIN[Khách thiết lập PIN 4 số]
    PIN --> BRYPT[BCrypt hash PIN\nLưu personal_pin_hash]

    BRYPT --> CHECKIN[Lễ tân xác nhận Check-in]
    CHECKIN --> UPD1[UPDATE Room_Booking_Details\ndetail_status = Checked_In]
    UPD1 --> UPD2[UPDATE Bookings\nbooking_status = Checked_In]
    UPD2 --> UPD3[UPDATE Rooms\nroom_status = Occupied_Clean\nBR-FO-04]
    UPD3 --> AUDIT[INSERT Audit_Log - BR-SYS-04]
    AUDIT --> DONE_OK([Check-in thành công\nSố phòng được cấp])

    style AGE fill:#ff9900,color:#fff
    style ROOM_CHK fill:#ff9900,color:#fff
    style CL3 fill:#ff9900,color:#fff
    style BRYPT fill:#ff9900,color:#fff
    style UPD1 fill:#0066cc,color:#fff
    style UPD3 fill:#00aa44,color:#fff
```

> **Business Rules áp dụng:**
>
> - `BR-FO-03` — Người đại diện ≥ 18 tuổi; xuất trình giấy tờ hợp lệ
> - `BR-FO-04` — Chỉ gán phòng Vacant_Clean; cập nhật → Occupied_Clean
> - `BR-FO-06` — sub_credit_limit ≤ credit_limit; BCrypt hash PIN
> - `BR-DATA-02` — Thu thập đủ: Họ tên, Ngày sinh, CCCD, Giới tính, Quốc tịch

---

## WF-04 — Check-out & Tổng hợp Hóa đơn

**Use Cases:** UC12.6, UC26.5, UC27.4
**Business Rules:** BR-FIN-01, BR-FO-04, BR-HK-01
**Actors:** Receptionist, Customer, System

```mermaid
flowchart TD
    START([Khách yêu cầu Check-out]) --> C1[Lễ tân tìm Booking của khách]
    C1 --> C2{Booking status\n= Checked_In?}
    C2 -->|Không| ERR1[Lỗi: Booking không\nở trạng thái Checked_In]
    ERR1 --> DONE_FAIL([Kết thúc])

    C2 -->|Có| C3[Truy xuất tất cả Folio_Items\nchưa thanh toán]
    C3 --> C4[Tổng hợp hóa đơn:\n- Room charges Daily_Rates\n- F&B charges Folio_Items\n- Tour charges Folio_Items\n- Trừ tiền cọc đã đặt]

    C4 --> SPLIT{Khách yêu cầu\ntách hóa đơn?}
    SPLIT -->|Có| SPLIT1[Đánh dấu FolioItems\nis_settled_separately=true]
    SPLIT1 --> SPLIT2[Tách hóa đơn phụ\ncho F&B / Tour]
    SPLIT2 --> BAL_CHK
    SPLIT -->|Không| BAL_CHK{Số dư nợ = 0?\nBR-FIN-01}

    BAL_CHK -->|Còn nợ| PAY[Khách thanh toán\nsố dư còn lại]
    PAY --> PAY_TYPE{Hình thức\nthanh toán}
    PAY_TYPE -->|Tiền mặt| CASH[Lễ tân nhập\nsố tiền nhận]
    PAY_TYPE -->|Thẻ/CK| CARD[VNPay / Ngân hàng]
    CASH & CARD --> PAY_TX[INSERT Payment_Transactions\ntransaction_type=FINAL_PAYMENT]
    PAY_TX --> BAL_CHK

    BAL_CHK -->|Đã = 0| CONFIRM[Lễ tân xác nhận Check-out]
    CONFIRM --> INV[UPDATE ConsolidatedInvoice\nstatus = Paid]
    INV --> UPD1[UPDATE Room_Booking_Details\ndetail_status = Checked_Out]
    UPD1 --> UPD2[UPDATE Bookings\nbooking_status = Checked_Out]
    UPD2 --> UPD3[UPDATE Rooms\nroom_status = Vacant_Dirty\nBR-FO-04]

    UPD3 --> TRIG[DB Trigger:\nTRG_Auto_Housekeeping_Task\nBR-HK-01]
    TRIG --> HK[INSERT Hotel_Operations\ntask_type=HOUSEKEEPING\npriority=High, status=Pending]

    HK --> EINV[Render e-Invoice PDF]
    EINV --> EMAIL[Gửi e-Invoice\nqua Email khách]
    EMAIL --> DONE_OK([Check-out thành công])

    style BAL_CHK fill:#ff4444,color:#fff
    style UPD3 fill:#ff9900,color:#fff
    style TRIG fill:#9933ff,color:#fff
    style HK fill:#9933ff,color:#fff
    style INV fill:#00aa44,color:#fff
```

> **Business Rules áp dụng:**
>
> - `BR-FIN-01` — Số dư nợ phải = 0 trước khi cho phép check-out
> - `BR-FO-04` — Phòng chuyển sang Vacant_Dirty sau check-out
> - `BR-HK-01` — DB Trigger tự động tạo task dọn phòng priority = High

---

## WF-05 — F&B / POS / Ghi nợ Folio

**Use Cases:** UC14, UC16, UC17, UC18, UC19
**Business Rules:** BR-FB-01, BR-FB-02, BR-FB-04, BR-FO-06
**Actors:** Customer, F&B Staff, Kitchen Staff

```mermaid
flowchart TD
    START([Mở màn hình Order]) --> TYPE{Loại Order?}

    TYPE -->|Room Service\nKhách tự đặt| RS[Khách quét QR\nxác định Room Number]
    RS --> RS_CHK{Khách đang\nChecked_In?\nBR-FB-01}
    RS_CHK -->|Không| ERR_RS[Lỗi: Chỉ khách đang\nlưu trú mới đặt được]
    ERR_RS --> DONE_FAIL([Kết thúc])
    RS_CHK -->|Có| MENU

    TYPE -->|Dine-In\nF&B Staff| DI[F&B Staff chọn\nTable Number trên POS]
    DI --> MENU[Hiển thị E-Menu\nchỉ món is_available=true]

    MENU --> SEL[Chọn món và số lượng]
    SEL --> CALC[Tính tổng đơn hàng]
    CALC --> PAY{Hình thức\nthanh toán?}

    PAY -->|Thanh toán Ngay| DIRECT[Thanh toán trực tiếp\ntiền mặt / thẻ tại POS]
    DIRECT --> DIR_INS[INSERT Food_Orders\npayment_type=DIRECT\nis_paid_in_pos=true]
    DIR_INS --> KOT

    PAY -->|Post-to-Room\nKý nợ về Phòng| PTR[Nhập Số Phòng khách]
    PTR --> PTR1{Phòng đang\nChecked_In? BR-FB-01}
    PTR1 -->|Không| ERR_PTR[Lỗi: Phòng không\nhợp lệ để ký nợ]
    ERR_PTR --> DONE_FAIL
    PTR1 -->|Có| PTR2{is_charge_to_room\n_allowed = true?\nBR-FB-01}
    PTR2 -->|Không| ERR_CHARGE[Lỗi: Phòng chưa\nbật quyền ký nợ]
    ERR_CHARGE --> DONE_FAIL
    PTR2 -->|Có| PTR3[Khách nhập PIN 4 số]
    PTR3 --> PTR4{BCrypt verify PIN?\nBR-FB-01}
    PTR4 -->|Sai| ERR_PIN[Lỗi PIN sai]
    ERR_PIN --> PTR3
    PTR4 -->|Đúng| PTR5{Tổng nợ mới ≤\nCredit Limit?\nBR-FO-06}
    PTR5 -->|Vượt hạn mức| ERR_CREDIT[Lỗi 403:\nVượt hạn mức nợ phòng\nTransaction Rollback]
    ERR_CREDIT --> DONE_FAIL
    PTR5 -->|OK| FOLIO[INSERT Folio_Items\nsource_department=FB]
    FOLIO --> PTR_INS[UPDATE Food_Orders\npayment_type=Charge_To_Room]
    PTR_INS --> KOT

    KOT[INSERT Food_Orders + Food_Order_Details\nkot_status = Pending] --> WS[WebSocket broadcast\nxuống KDS Bếp - BR-FB-02]
    WS --> KDS[Kitchen Staff nhận\nđơn mới trên KDS]
    KDS --> K1[Click chuyển → Cooking]
    K1 --> K2[UPDATE kot_status = Cooking]
    K2 --> K3[Chế biến xong\nclick Ready]
    K3 --> K4[UPDATE kot_status = Ready]
    K4 --> K5[WebSocket notify\nF&B Staff bê món]
    K5 --> K6[Bê món đến khách]
    K6 --> K7[UPDATE kot_status = Served]
    K7 --> DONE_OK([Order hoàn thành])

    MENU --> OOS_BTN{Bếp báo\nHết món}
    OOS_BTN -->|Kitchen click hết| OOS[UPDATE MenuItem\nis_available = false]
    OOS --> OOS_WS[WebSocket broadcast\ntoàn bộ POS - BR-FB-02]
    OOS_WS --> OOS_UI[Món hiển thị Unavailable\ntrên tất cả màn hình]

    style PTR4 fill:#ff9900,color:#fff
    style PTR5 fill:#ff4444,color:#fff
    style FOLIO fill:#0066cc,color:#fff
    style WS fill:#9933ff,color:#fff
    style OOS fill:#ff4444,color:#fff
```

> **Business Rules áp dụng:**
>
> - `BR-FB-01` — Checked_In + is_charge_allowed + PIN verify + Credit Limit check
> - `BR-FB-02` — WebSocket broadcast KOT; real-time khi món hết hàng
> - `BR-FO-06` — TRG_Folio_Credit_Limit_Check rollback nếu vượt hạn mức

---

## WF-06 — Đặt Tour & Điểm danh AI

**Use Cases:** UC20, UC21, UC22
**Business Rules:** BR-TR-01, BR-TR-02, BR-TR-05
**Actors:** Customer, Tour Guide, Admin, System

```mermaid
flowchart TD
    START([Khách tìm Tour]) --> T1[Nhập ngày muốn đi]
    T1 --> T2[Gọi OpenWeather API\nlấy dự báo thời tiết]
    T2 --> T3[Hiển thị danh sách Tour\n+ thông tin thời tiết]
    T3 --> T4[Khách chọn Tour + Schedule]
    T4 --> T5{Đã đăng nhập?}
    T5 -->|Chưa| LOGIN[Chuyển Login]
    LOGIN --> T5
    T5 -->|Rồi| T6[Nhập số vé\nvà thông tin hành khách]
    T6 --> CAP{Số ghế đặt ≤\nmax_capacity - booked_seats?\nTRG_Tour_Capacity_Validator\nBR-TR-01}
    CAP -->|Vượt sức chứa| ERR_CAP[Lỗi: Hết chỗ\nTRG_Tour_Capacity_Validator]
    ERR_CAP --> T4
    CAP -->|Còn chỗ| PAY{Hình thức\nthanh toán?}

    PAY -->|VNPay trực tiếp| VNP[Chuyển VNPay Payment]
    VNP --> VNP2{Thành công?}
    VNP2 -->|Thất bại| ERR_PAY[Lỗi thanh toán]
    ERR_PAY --> PAY
    VNP2 -->|Thành công| CONF

    PAY -->|Post-to-Room| PTR[PIN + Credit Limit\nBR-FB-01]
    PTR --> PTR2{Hợp lệ?}
    PTR2 -->|Không| ERR_PTR[Lỗi xác thực]
    ERR_PTR --> PAY
    PTR2 -->|Có| CONF

    CONF[INSERT Tour_Bookings\n+ INSERT Tour_Attendees\nstatus = Confirmed] --> SEATS[UPDATE Tour_Schedules\nbooked_seats += count\nTRG_Update_Tour_Booked_Seats]
    SEATS --> ETICKET[Gửi e-Ticket qua Email]
    ETICKET --> DONE_BOOK([Đặt Tour thành công])

    subgraph ATTEND["Điểm danh ngày khởi hành - BR-TR-02"]
        A1([Tour Guide mở\nCamera AI]) --> A2[Chụp ảnh hành khách]
        A2 --> A3[Upload ảnh lên\nkawai-ai-service Python]
        A3 --> A4[AI trích xuất\n128-dim face vector]
        A4 --> A5{Cosine Similarity\n≥ 0.85?\nBR-TR-02}
        A5 -->|≥ 0.85| A6[UPDATE Tour_Attendees\nattendance_status = Boarded\nfaceMatchedAt = now]
        A5 -->|< 0.85 hoặc lỗi| A7[Fallback:\nĐiểm danh thủ công]
        A7 --> A6
        A6 --> A8([Điểm danh OK])
    end

    style CAP fill:#ff4444,color:#fff
    style CONF fill:#0066cc,color:#fff
    style A5 fill:#ff9900,color:#fff
```

> **Business Rules áp dụng:**
>
> - `BR-TR-01` — TRG_Tour_Capacity_Validator chặn overbooking tour
> - `BR-TR-02` — Cosine Similarity ≥ 0.85; fallback manual điểm danh
> - `BR-TR-05` — Auto-cancel 24h trước nếu dưới ngưỡng min_participants

---

## WF-07 — Room Status Lifecycle

**Use Cases:** UC13
**Business Rules:** BR-FO-04, BR-FO-05, BR-HK-01, BR-HK-02
**Actors:** Housekeeping, Maintenance, Receptionist, System

### State Machine — Vòng đời Trạng thái Phòng

```mermaid
stateDiagram-v2
    [*] --> Vacant_Clean : Phòng khởi tạo / Dọn xong

    Vacant_Clean --> Occupied_Clean : Check-in OK\n(BR-FO-04)
    Occupied_Clean --> Vacant_Dirty : Check-out hoàn tất\n(TRG_Auto_Housekeeping_Task)
    Occupied_Clean --> Maintenance : Báo hỏng khi đang có khách\n(BR-HK-02)
    Vacant_Dirty --> Maintenance : Phát hiện hỏng khi dọn phòng\n(BR-HK-02)
    Vacant_Dirty --> Vacant_Clean : Dọn phòng hoàn thành\n(UC10.2)
    Maintenance --> Vacant_Dirty : Sửa xong, cần dọn lại
    Maintenance --> Vacant_Clean : Sửa xong, phòng đã sạch
```

### Luồng chi tiết Housekeeping & Bảo trì

```mermaid
flowchart TD
    TRIGGER([Trigger: Check-out\nhoàn tất]) --> TRG[TRG_Auto_Housekeeping_Task\nBR-HK-01]
    TRG --> INS[INSERT Hotel_Operations\ntask_type=HOUSEKEEPING\npriority=High, status=Pending]
    INS --> DIRTY[Rooms → Vacant_Dirty\nBR-FO-04]
    DIRTY --> RUSH{Lễ tân đánh dấu\nRush Room?\nBR-FO-05}
    RUSH -->|VIP Guest| URG[UPDATE priority=Urgent\nĐưa lên đầu hàng đợi]
    RUSH -->|Không| ASSIGN
    URG --> ASSIGN[HK nhận task\ntrên Mobile App]
    ASSIGN --> CLEAN[Thực hiện dọn phòng]
    CLEAN --> BROKEN{Phát hiện\nthiết bị hỏng?\nBR-HK-02}
    BROKEN -->|Có| MAINT_INS[INSERT Hotel_Operations\ntask_type=MAINTENANCE]
    MAINT_INS --> MAINT_ROOM[UPDATE Rooms\nroom_status=Maintenance]
    MAINT_ROOM --> NOTIFY[Thông báo đội Kỹ thuật]
    NOTIFY --> MAINT_WORK[Maintainer sửa chữa]
    MAINT_WORK --> MAINT_DONE{Sửa xong\nphòng đã sạch?}
    MAINT_DONE -->|Cần dọn lại| MAINT_DIRTY[Rooms → Vacant_Dirty\nTạo task HK mới]
    MAINT_DONE -->|Phòng sạch rồi| CLEAN_DONE
    MAINT_DIRTY --> ASSIGN
    BROKEN -->|Không| DONE_HK[UPDATE Hotel_Operations\nstatus=Completed]
    DONE_HK --> CLEAN_DONE[UPDATE Rooms\nroom_status=Vacant_Clean]
    CLEAN_DONE --> MATRIX[Lễ tân thấy phòng xanh\ntrên Room Matrix]
    MATRIX --> DONE_OK([Phòng sẵn sàng đón khách mới])

    style TRG fill:#9933ff,color:#fff
    style URG fill:#ff9900,color:#fff
    style MAINT_INS fill:#ff4444,color:#fff
    style CLEAN_DONE fill:#00aa44,color:#fff
```

> **Business Rules áp dụng:**
>
> - `BR-FO-04` — Vòng đời phòng Vacant_Clean → Occupied → Vacant_Dirty → Vacant_Clean
> - `BR-FO-05` — Rush Room ưu tiên priority = Urgent
> - `BR-HK-01` — Trigger tự động tạo task dọn phòng sau check-out
> - `BR-HK-02` — Báo hỏng tự động chuyển phòng sang Maintenance

---

## WF-08 — Night Audit (Kiểm toán Đêm)

**Use Cases:** UC27.1, UC27.2
**Business Rules:** BR-FIN-03, BR-FIN-04
**Actors:** System (Automated Scheduler)

```mermaid
flowchart TD
    SCHED([02:00 AM\nScheduler kích hoạt\nBR-FIN-03]) --> FETCH[Lấy tất cả Room_Booking_Details\ndetail_status = Checked_In]
    FETCH --> CHK{Có phòng\nđang có khách?}
    CHK -->|Không| DONE_EMPTY([Kết thúc - Không có GD])

    CHK -->|Có| LOOP[Với mỗi phòng Checked_In]
    LOOP --> RATE[Tra giá phòng hôm nay\nDailyRates theo category_id + date]
    RATE --> RATE_CHK{Tìm thấy\ngiá phòng?}
    RATE_CHK -->|Không tìm thấy| ALERT[Gửi cảnh báo khẩn cấp\ncho Manager qua email]
    ALERT --> LOOP
    RATE_CHK -->|Có| POST[INSERT Folio_Items\nsource_department=ROOM\namount=daily_rate]
    POST --> LOG[Ghi nhật ký\nnight audit execution]
    LOG --> MORE{Còn phòng\nkhác chưa xử lý?}
    MORE -->|Có| LOOP
    MORE -->|Không| USALI[Tổng hợp doanh thu\nphân bổ theo USALI\nBR-FIN-04]
    USALI --> DECOMP[Room Revenue\nF&B Revenue\nTour Revenue]
    DECOMP --> ROLLOVER[Cập nhật ngày vận hành\nhệ thống sang ngày tiếp]
    ROLLOVER --> REPORT[Lưu Night Audit Report\nvà Balance Sheet]
    REPORT --> DONE_OK([Night Audit hoàn thành])

    style SCHED fill:#1a1a2e,color:#fff
    style POST fill:#0066cc,color:#fff
    style USALI fill:#00aa44,color:#fff
    style ALERT fill:#ff4444,color:#fff
```

> **Business Rules áp dụng:**
>
> - `BR-FIN-03` — Chạy tự động lúc 02:00 AM; đóng sổ ngày; post room charges
> - `BR-FIN-04` — Phân bổ doanh thu theo USALI: Room / F&B / Tour

---

## WF-09 — Hủy Đặt phòng & Hoàn tiền

**Use Cases:** UC12 (Hủy đặt phòng), UC27
**Business Rules:** BR-FIN-02
**Actors:** Customer

```mermaid
flowchart TD
    START([Khách mở\nLịch sử Booking]) --> B1[Chọn Booking muốn hủy]
    B1 --> B2{Booking status\nhợp lệ để hủy?\nKhác Checked_In\nvà Cancelled}
    B2 -->|Không hợp lệ| ERR1[Không thể hủy:\nBooking đang lưu trú\nhoặc đã hủy]
    ERR1 --> DONE_FAIL([Kết thúc])

    B2 -->|Hợp lệ| B3[Hiển thị chính sách\nhoàn tiền]
    B3 --> CONFIRM[Khách xác nhận hủy]
    CONFIRM --> CALC[Tính khoảng cách\ngiờ check-in dự kiến]
    CALC --> POLICY{Hủy trước\n≥ 48 giờ so với\ncheck-in?\nBR-FIN-02}

    POLICY -->|Đúng — > 48h| REFUND[Hoàn tiền 100%\ntiền cọc đã đặt]
    REFUND --> TX[INSERT Payment_Transactions\ntransaction_type=REFUND\namount=deposit_amount]
    TX --> VNP[Gọi VNPay Refund API]
    VNP --> VNP2{VNPay Refund\nthành công?}
    VNP2 -->|Thất bại| ERR_REF[Ghi lỗi hoàn tiền\nAdmin xử lý thủ công]
    VNP2 -->|Thành công| CANCEL[UPDATE Bookings\nstatus = Cancelled]
    CANCEL --> FREE[Giải phóng phòng\nvề kho hàng]
    FREE --> EMAIL_REFUND[Email xác nhận:\nhủy và hoàn tiền]
    EMAIL_REFUND --> DONE_REFUND([Kết thúc - Hủy & Hoàn tiền OK])

    POLICY -->|Không — ≤ 48h\nhoặc No-show| FORFEIT[Tịch thu toàn bộ tiền cọc\nKhông hoàn tiền - BR-FIN-02]
    FORFEIT --> CANCEL2[UPDATE Bookings\nstatus = Cancelled]
    CANCEL2 --> FREE2[Giải phóng phòng]
    FREE2 --> EMAIL_NREF[Email xác nhận:\nhủy KHÔNG hoàn tiền]
    EMAIL_NREF --> DONE_NREF([Kết thúc - Hủy không hoàn])

    style POLICY fill:#ff9900,color:#fff
    style REFUND fill:#00aa44,color:#fff
    style FORFEIT fill:#ff4444,color:#fff
```

> **Business Rules áp dụng:**
>
> - `BR-FIN-02` — Hủy > 48h: hoàn 100%; Hủy ≤ 48h hoặc No-show: mất cọc

---

## WF-10 — Hủy Tour & Hoàn tiền Tự động

**Use Cases:** UC22.4, UC27
**Business Rules:** BR-TR-05
**Actors:** System (Scheduler), Admin, Tour Guide

```mermaid
flowchart TD
    SCHED([Scheduler chạy\n24h trước mỗi\nTour Schedule\nBR-TR-05]) --> FETCH[Lấy Tour_Schedules\nstatus=Active sắp khởi hành]
    FETCH --> CHK{confirmed_bookings\n≥ min_participants?}

    CHK -->|Đủ số người| OK[Tour diễn ra bình thường]
    OK --> DONE_OK([Kết thúc - Tour OK])

    CHK -->|Không đủ| CANCEL_SCH[UPDATE Tour_Schedules\nstatus = Cancelled]
    CANCEL_SCH --> BOOKINGS[Lấy tất cả Tour_Bookings\ncủa chuyến này]
    BOOKINGS --> EACH[Với mỗi Tour_Booking]
    EACH --> PAY_TYPE{Loại thanh toán?}
    PAY_TYPE -->|VNPay trực tiếp| REFUND[Gọi VNPay Refund API\n100% tiền vé]
    REFUND --> TX[INSERT Payment_Transactions\ntransaction_type=REFUND]
    PAY_TYPE -->|Post-to-Room| CREDIT[CREDIT/DELETE\nFolio_Items liên quan]
    TX & CREDIT --> CANCEL_BK[UPDATE Tour_Bookings\nstatus = Cancelled]
    CANCEL_BK --> EMAIL[Gửi email xin lỗi\nvà thông báo hủy]
    EMAIL --> MORE{Còn booking\nkhác chưa xử lý?}
    MORE -->|Có| EACH
    MORE -->|Không| RESET[UPDATE Tour_Schedules\nbooked_seats = 0]
    RESET --> DONE_CANCEL([Kết thúc - Tour Hủy OK])

    subgraph EMERGENCY["Hủy Tour Khẩn cấp - Admin/Tour Guide"]
        E1([Admin nhận\ntình huống khẩn]) --> E2[Click Hủy Tour\nNhập lý do]
        E2 --> E3[Thực hiện cùng\nluồng hoàn tiền]
        E3 --> E4[INSERT Audit_Log\nBR-SYS-04]
        E4 --> E5([Hoàn thành])
    end

    style CANCEL_SCH fill:#ff4444,color:#fff
    style REFUND fill:#00aa44,color:#fff
    style CREDIT fill:#00aa44,color:#fff
```

> **Business Rules áp dụng:**
>
> - `BR-TR-05` — Kiểm tra 24h trước; auto-cancel nếu < min_participants; hoàn 100%

---

## WF-11 — Đánh giá Dịch vụ & Kiểm duyệt

**Use Cases:** UC24, UC25
**Business Rules:** BR-TR-03, BR-TR-04, BR-SYS-04
**Actors:** Customer, Admin

```mermaid
flowchart TD
    START([Khách hoàn thành\nCheck-out hoặc Tour]) --> DL[Hệ thống tính\nreview_deadline =\ncompleted_date + 7 ngày\nBR-TR-03]
    DL --> CHK{Còn trong\n7 ngày?}
    CHK -->|Quá hạn| EXPIRED[Link đánh giá hết hạn]
    EXPIRED --> DONE_FAIL([Kết thúc])
    CHK -->|Còn hạn| FORM[Hiển thị Form Đánh giá]
    FORM --> INPUT[Nhập:\n- Rating 1-5 sao\n- Review text\n- Ảnh đính kèm tùy chọn]
    INPUT --> VALIDATE{Khách đã thực sự\nsử dụng dịch vụ?}
    VALIDATE -->|Không có\ngiao dịch thực tế| ERR[Lỗi: Chưa sử dụng\ndịch vụ này]
    ERR --> DONE_FAIL
    VALIDATE -->|Có| INS[INSERT Reviews\nmoderation_status=Pending]
    INS --> DONE_OK([Gửi review thành công])

    subgraph MOD["Kiểm duyệt Review - Admin"]
        M1([Admin xem\nDanh sách Pending]) --> M2[Đọc nội dung review]
        M2 --> M3{Quyết định Admin}
        M3 -->|Phê duyệt| M4[UPDATE Reviews\nmoderation_status=Approved]
        M4 --> M5[Review hiện\ntrên Landing Page]
        M3 -->|Ẩn - Spam/Toxic| M6[Nhập lý do ẩn - bắt buộc]
        M6 --> M7[UPDATE Reviews\nmoderation_status=Hidden]
        M7 --> M8[INSERT Audit_Logs\nghi lý do - BR-SYS-04]
        M3 -->|Sửa nội dung| BLOCK[BỊ CHẶN:\nAdmin không được sửa\nnội dung review - BR-TR-04]
        M4 & M8 --> M9([Kiểm duyệt xong])
    end

    style BLOCK fill:#ff4444,color:#fff
    style M8 fill:#9933ff,color:#fff
    style INS fill:#0066cc,color:#fff
```

> **Business Rules áp dụng:**
>
> - `BR-TR-03` — Chỉ review trong 7 ngày sau hoàn thành dịch vụ
> - `BR-TR-04` — Admin CHỈ ẩn/hiện; không sửa nội dung; ghi audit log
> - `BR-SYS-04` — Mọi hành động kiểm duyệt phải ghi Audit_Logs

---

## WF-12 — Quản lý Nhân viên & Phân quyền

**Use Cases:** UC01.2, UC05.1, UC05.2
**Business Rules:** BR-SYS-04, BR-SYS-07, BR-DATA-03
**Actors:** Admin

```mermaid
flowchart TD
    START([Admin truy cập\nStaff Management]) --> ACT{Hành động}

    ACT -->|Tạo Nhân viên mới| C1[Nhập thông tin:\nusername, password, roleId,\nfullName, CCCD, salary]
    C1 --> C2{Validate đầu vào}
    C2 -->|Lỗi| C1
    C2 -->|OK| TRANS[BEGIN @Transactional\nBR-DATA-03]
    TRANS --> INS_ACC[INSERT Accounts\nrole=selected, is_active=true]
    INS_ACC --> INS_EMP[INSERT Employees\nlinked accountId]
    INS_EMP --> TX_CHK{Transaction OK?}
    TX_CHK -->|Lỗi bất kỳ bước| ROLLBACK[ROLLBACK hoàn toàn\nKhông tạo nửa vời]
    ROLLBACK --> ERR[Hiển thị lỗi\nAdmin kiểm tra lại]
    TX_CHK -->|OK| COMMIT[COMMIT Transaction]
    COMMIT --> AUDIT_C[INSERT Audit_Logs\nBR-SYS-04]
    AUDIT_C --> DONE_CREATE([Tạo nhân viên OK])

    ACT -->|Cập nhật Role/Lock| U1[Chọn nhân viên]
    U1 --> U2{Loại cập nhật}
    U2 -->|Đổi Role| U3[Chọn Role mới\n1 user 1 role - BR-SYS-07]
    U3 --> U4[UPDATE Accounts\nrole_id = new_role]
    U4 --> U5[Spring Security\ntự động cập nhật\nquyền truy cập API]
    U2 -->|Lock Account| U6[UPDATE Accounts\nis_active = false]
    U5 & U6 --> AUDIT_U[INSERT Audit_Logs\nold_value, new_value\nadmin_id, ip, timestamp\nBR-SYS-04]
    AUDIT_U --> DONE_UPD([Cập nhật OK])

    ACT -->|Xem Audit Log| AL1[Filter:\nDate Range, Account, Table]
    AL1 --> AL2[SELECT Audit_Logs\nCHỈ READ - không UPDATE/DELETE\nBR-SYS-04]
    AL2 --> AL3[Hiển thị danh sách\ndòng log truy vết]

    style TRANS fill:#9933ff,color:#fff
    style ROLLBACK fill:#ff4444,color:#fff
    style AUDIT_C fill:#ff9900,color:#fff
    style AUDIT_U fill:#ff9900,color:#fff
```

> **Business Rules áp dụng:**
>
> - `BR-SYS-07` — Mỗi nhân viên chỉ có 1 role; RBAC routing tự động
> - `BR-SYS-04` — Ghi Audit_Log cho mọi thay đổi phân quyền
> - `BR-DATA-03` — @Transactional bắt buộc khi tạo Account + Employee

---

## WF-13 — Master Flow — Vận hành Tổng thể
| WF-14 | [Walk-in Check-in](#wf-14--walk-in-check-in)                                       | Front Office   | CRITICAL |
| WF-15 | [Remote CCCD Scan](#wf-15--remote-cccd-scan)                                       | Front Office   |   HIGH   |
| WF-16 | [Tour Itinerary & GPS Checkpoint](#wf-16--tour-itinerary--gps-checkpoint)      | Tour           |   HIGH   |
| WF-17 | [Đổi Hạng Phòng](#wf-17--đổi-hạng-phòng)                                           | Front Office   |   HIGH   |
| WF-18 | [Membership Tier & Loyalty Points](#wf-18--membership-tier--loyalty-points)     | Membership     |  MEDIUM  |
| WF-19 | [Refund Request Flow (Manual)](#wf-19--refund-request-flow-manual)                 | Finance        |  MEDIUM  |
| WF-20 | [Add-On Hotel Service](#wf-20--add-on-hotel-service)                               | F&B            |  MEDIUM  |
| WF-21 | [Staff Scheduling](#wf-21--staff-scheduling)                                       | Admin          |  MEDIUM  |
| WF-22 | [Workflow Engine (Dynamic BPMN)](#wf-22--workflow-engine-dynamic-bpmn)             | System         |   HIGH   |
| WF-23 | [Đặt Bàn Trực Tuyến](#wf-23--đặt-bàn-trực-tuyến)                                 | F&B            |   HIGH   |
| WF-24 | [Hủy Đơn Hàng F&B & Hoàn Tiền](#wf-24--hủy-đơn-hàng-fb--hoàn-tiền)     | F&B            |   HIGH   |
| WF-25 | [Chốt Ca & Báo Cáo F&B](#wf-25--chốt-ca--báo-cáo-fb)                         | F&B            |   HIGH   |
| WF-26 | [Phê duyệt Thiết bị Vận hành](#wf-26--phê-duyệt-thiết-bị-vận-hành)               | Admin          |  MEDIUM  |

```mermaid
flowchart LR
    subgraph PRE["🌐 PRE-ARRIVAL"]
        P1[Guest tìm phòng\ntrực tuyến] --> P2[Đặt phòng\n+ Cọc VNPay]
        P2 --> P3[Booking = Confirmed]
    end

    subgraph ARRIVE["🏨 ARRIVAL"]
        A1[Khách đến\nTiền sảnh] --> A2[Check-in\nOCR CCCD]
        A2 --> A3[Gán phòng\nVacant_Clean]
        A3 --> A4[Credit Limit\nvà PIN]
        A4 --> A5[Phòng →\nOccupied_Clean]
    end

    subgraph STAY["🛎 OCCUPANCY"]
        O1[Room Service\nF&B Orders] --> O2[KOT → Bếp\nWebSocket]
        O2 --> O3[Post-to-Room\nFolio Charges]
        O3 --> O4[Đặt Tour\nAI Điểm danh]
        O4 --> O5[Night Audit\n02:00 AM]
        O5 --> O6[Folio tích lũy\ntheo ngày]
    end

    subgraph DEPART["🚪 DEPARTURE"]
        D1[Check-out\nLễ tân] --> D2{Folio = 0?}
        D2 -->|Còn nợ| D3[Thanh toán\nsố dư]
        D3 --> D2
        D2 -->|Tất toán| D4[e-Invoice\n→ Email]
        D4 --> D5[Phòng →\nVacant_Dirty]
        D5 --> D6[Trigger:\nHK Task]
        D6 --> D7[Phòng →\nVacant_Clean]
        D7 --> D8[Sẵn sàng\nđón khách mới]
    end

    subgraph POST["⭐ POST-STAY"]
        PS1[Form Đánh giá\n7 ngày] --> PS2[Khách submit Review]
        PS2 --> PS3[Admin Kiểm duyệt]
        PS3 --> PS4[Review hiện\nWebsite]
    end

    PRE --> ARRIVE --> STAY --> DEPART --> POST

    style PRE fill:#1a237e,color:#fff
    style ARRIVE fill:#1b5e20,color:#fff
    style STAY fill:#4a148c,color:#fff
    style DEPART fill:#b71c1c,color:#fff
    style POST fill:#e65100,color:#fff
```

---

## WF-14 — Walk-in Check-in

**Use Cases:** Use Case "Walk-in Check-in"
**Business Rules:** BR-FO-08, BR-FO-03, BR-FO-04, BR-FO-06
**Actors:** Receptionist

```mermaid
flowchart TD
    START([Khách vãng lai đến quầy]) --> S1[Lễ tân tìm phòng trống trực tiếp]
    S1 --> S2{Có phòng trống phù hợp?}
    S2 -->|Không| END_N([Từ chối/Hết phòng])
    S2 -->|Có| S3[Chọn phòng, nhập số lượng khách & ngày đi]
    S3 --> S4[Quét CCCD bằng đầu đọc tại quầy hoặc Remote Scan]
    S4 --> S5[Validate thông tin OCR & xác nhận thủ công]
    S5 --> S6[Thanh toán toàn bộ tiền phòng ngay lập tức]
    S6 --> S7[Tạo Booking trực tiếp trên hệ thống]
    S7 --> S8[Gán phòng vật lý Vacant_Clean]
    S8 --> S9[Thiết lập Credit Limit & Mã PIN ký nợ phòng]
    S9 --> S10[Phòng chuyển sang Occupied_Clean]
    S10 --> END_S([Hoàn tất check-in & Giao chìa khóa])

    style S6 fill:#b71c1c,color:#fff
    style S10 fill:#1b5e20,color:#fff
```

> **Business Rules áp dụng:**
>
> - `BR-FO-08` — Walk-in phải thanh toán ngay lập tức trước khi nhận phòng.
> - `BR-FO-04` — Chỉ gán phòng ở trạng thái Vacant_Clean.
> - `BR-FO-06` — Thiết lập hạn mức chi tiêu phụ (sub_credit_limit) và mã PIN.

---

## WF-15 — Remote CCCD Scan

**Use Cases:** Use Case "Remote CCCD Scan"
**Business Rules:** BR-FIN-07
**Actors:** Customer, Receptionist

```mermaid
flowchart TD
    START([Lễ tân kích hoạt Remote Scan]) --> S1[Hệ thống sinh Session ID & QR Code có TTL 2 phút]
    S1 --> S2[Khách quét QR Code bằng điện thoại cá nhân]
    S2 --> S3[Trang web bảo mật mở ra trên thiết bị khách]
    S3 --> S4[Khách chụp ảnh mặt trước/sau CCCD]
    S4 --> S5[Gửi ảnh lên Python OCR Microservice]
    S5 --> S6[Trích xuất dữ liệu: Họ tên, Số CCCD, Ngày sinh, Giới tính]
    S6 --> S7[WebSocket gửi dữ liệu thời gian thực về màn hình Lễ tân]
    S7 --> S8[Lễ tân đối chiếu với người thực tế & nhấn Approve]
    S8 --> S9[Hệ thống tự động điền vào Form lưu trú]
    S9 --> END([Đóng phiên quét & Lưu thông tin])

    style S1 fill:#1a237e,color:#fff
    style S7 fill:#e65100,color:#fff
```

> **Business Rules áp dụng:**
>
> - `BR-FIN-07` — Dữ liệu CCCD được mã hóa AES-256; Phiên quét tự hủy sau TTL 2 phút để bảo mật.

---

## WF-16 — Tour Itinerary & GPS Checkpoint

**Use Cases:** Use Case "Tour Attendance & GPS Tracking"
**Business Rules:** BR-TR-02, BR-TR-06, BR-TR-07, BR-TR-08, BR-TR-09
**Actors:** Tour Guide, Tour Attendee, System

```mermaid
flowchart TD
    START([Đến giờ khởi hành Tour]) --> S1[Tour Guide nhấn Start Tour trên App]
    S1 --> S2[Hệ thống chuyển trạng thái Tour Schedule sang RUNNING]
    S2 --> S3[Di chuyển đến Checkpoint đầu tiên trong Itinerary]
    S3 --> S4[Gọi AI Face Scan để điểm danh thành viên]
    S4 --> S5{Cosine Similarity >= 0.85?}
    S5 -->|Có| S6[Cập nhật Attendance Status = BOARDED]
    S5 -->|Không| S7[Yêu cầu chụp lại hoặc điểm danh thủ công]
    S7 --> S8[Ghi chú lý do điểm danh thủ công vào hệ thống]
    S6 & S8 --> S9[App gửi tọa độ GPS định kỳ 5 phút về Backend]
    S9 --> S10[Backend đối chiếu tọa độ với Checkpoint Location]
    S10 --> S11{Hoàn thành toàn bộ lộ trình?}
    S11 -->|Chưa| S3
    S11 -->|Rồi| S12[Tour Guide nhấn End Tour]
    S12 --> END([Tour Schedule = COMPLETED])

    style S4 fill:#4a148c,color:#fff
    style S5 fill:#ff9900,color:#fff
```

> **Business Rules áp dụng:**
>
> - `BR-TR-02` — Điểm danh AI Face Scan với ngưỡng khớp vector >= 0.85.
> - `BR-TR-06/07/08/09` — Đảm bảo định vị GPS chính xác, tuân thủ lịch trình và điểm danh tại mỗi trạm dừng.

---

## WF-17 — Đổi Hạng Phòng (Change Room Category)

**Use Cases:** Use Case "Change Room Category"
**Business Rules:** BR-FO-07, BR-FO-04
**Actors:** Receptionist, Customer

```mermaid
flowchart TD
    START([Khách yêu cầu đổi phòng]) --> S1[Kiểm tra tính khả dụng của hạng phòng mới]
    S1 --> S2{Có phòng trống?}
    S2 -->|Không| END_N([Từ chối & giải thích])
    S2 -->|Có| S3{Loại đổi phòng?}
    S3 -->|Nâng cấp/Hạ cấp| S4[Tính toán chênh lệch giá bằng DailyRate & RoomSurcharge]
    S3 -->|Cùng hạng| S5[Chênh lệch giá = 0]
    S4 & S5 --> S6[Lễ tân chọn phòng vật lý Vacant_Clean mới]
    S6 --> S7[Cập nhật RoomBookingDetail với roomId & categoryId mới]
    S7 --> S8[Ghi nhận phụ thu hoặc hoàn tiền vào Folio]
    S8 --> S9[Chuyển trạng thái phòng cũ sang Vacant_Dirty]
    S9 --> S10[Chuyển trạng thái phòng mới sang Occupied_Clean]
    S10 --> END_S([Giao chìa khóa phòng mới & ghi Audit Log])

    style S4 fill:#ff9900,color:#fff
    style S9 fill:#b71c1c,color:#fff
    style S10 fill:#1b5e20,color:#fff
```

> **Business Rules áp dụng:**
>
> - `BR-FO-07` — Đổi hạng phòng phải tính toán phụ thu dựa trên daily_rates của hạng mới.
> - `BR-FO-04` — Phòng mới phải ở trạng thái Vacant_Clean.

---

## WF-18 — Membership Tier & Loyalty Points

**Use Cases:** Use Case "Membership Tier Calculation"
**Business Rules:** BR-MEM-01
**Actors:** System, Customer

```mermaid
flowchart TD
    START([Hóa đơn Booking được tất toán]) --> S1[Hệ thống lấy tổng tiền thanh toán thực tế]
    S1 --> S2[Tính điểm Loyalty tích lũy mới: 100,000 VND = 1 điểm]
    S2 --> S3[UPDATE loyalty_points trong bảng Customers]
    S3 --> S4[Kiểm tra ngưỡng thăng hạng thành viên]
    S4 --> S5{Đủ điều kiện lên hạng?}
    S5 -->|Có| S6[Cập nhật membership_tier: Silver -> Gold -> Platinum]
    S6 --> S7[Gửi email chúc mừng và thông báo quyền lợi mới]
    S5 -->|Không| S8[Giữ nguyên hạng hiện tại]
    S7 & S8 --> END([Hoàn tất cập nhật tài khoản])

    style S6 fill:#1b5e20,color:#fff
```

> **Business Rules áp dụng:**
>
> - `BR-MEM-01` — Tích điểm tự động dựa trên hóa đơn checkout và nâng hạng thành viên theo thang điểm cố định.

---

## WF-19 — Refund Request Flow (Manual)

**Use Cases:** Use Case "Refund Processing"
**Business Rules:** BR-FIN-08, BR-FIN-02
**Actors:** Receptionist, Manager, System

```mermaid
flowchart TD
    START([Yêu cầu hủy đặt phòng/dịch vụ]) --> S1[Lễ tân/Khách tạo Refund Request]
    S1 --> S2[Hệ thống tính toán số tiền được hoàn theo BR-FIN-02]
    S2 --> S3[Tạo bản ghi RefundRequest ở trạng thái PENDING]
    S3 --> S4[Gửi thông báo phê duyệt tới Manager]
    S4 --> S5{Manager duyệt?}
    S5 -->|Từ chối| S6[Chuyển trạng thái sang REJECTED & thông báo lý do]
    S5 -->|Đồng ý| S7[Chuyển trạng thái sang APPROVED]
    S7 --> S8{Phương thức thanh toán?}
    S8 -->|VNPay| S9[Gọi VNPay API hoàn tiền tự động]
    S8 -->|Tiền mặt/Chuyển khoản| S10[Thực hiện hoàn thủ công tại quầy]
    S9 & S10 --> S11[UPDATE PaymentTransaction & đóng hồ sơ]
    S6 & S11 --> END([Kết thúc quy trình hoàn tiền])

    style S3 fill:#e65100,color:#fff
    style S5 fill:#ff9900,color:#fff
```

> **Business Rules áp dụng:**
>
> - `BR-FIN-02` — Quy định thời gian hủy phòng để tính tỷ lệ hoàn tiền (100% hoặc mất cọc).
> - `BR-FIN-08` — Mọi yêu cầu hoàn tiền thủ công phải được Manager phê duyệt trực tiếp.

---

## WF-20 — Add-On Hotel Service

**Use Cases:** Use Case "Add-On Service Order"
**Business Rules:** BR-FB-05, BR-FO-06
**Actors:** Customer, Receptionist, F&B Staff

```mermaid
flowchart TD
    START([Khách yêu cầu dịch vụ gia tăng]) --> S1[Tìm kiếm dịch vụ: Spa, Thuê xe, Hồ bơi...]
    S1 --> S2[Chọn dịch vụ & số lượng]
    S2 --> S3{Phương thức thanh toán?}
    S3 -->|Thanh toán ngay| S4[Tạo VNPay Payment Link hoặc thu tiền mặt]
    S4 --> S5[Giao dịch thành công -> Tạo BookingService]
    S3 -->|Ký nợ về phòng| S6[Xác thực mã PIN của phòng]
    S6 --> S7[Kiểm tra Credit Limit của phòng]
    S7 --> S8{Còn hạn mức?}
    S8 -->|Không| S9[Từ chối ký nợ, yêu cầu thanh toán ngay]
    S8 -->|Có| S10[Tạo FolioItem cho dịch vụ & cộng vào ví nợ phòng]
    S9 --> S3
    S5 & S10 --> END([Cung cấp dịch vụ & Ghi log])

    style S8 fill:#ff9900,color:#fff
```

> **Business Rules áp dụng:**
>
> - `BR-FB-05` — Quản lý dịch vụ gia tăng của khách sạn (Add-On Services).
> - `BR-FO-06` — Enforce Credit Limit khi ký nợ dịch vụ về phòng.

---

## WF-21 — Staff Scheduling

**Use Cases:** Use Case "Staff Shift Scheduling"
**Business Rules:** BR-STAFF-01
**Actors:** Manager, Admin, Employees

```mermaid
flowchart TD
    START([Manager truy cập Staff Scheduling]) --> S1[Chọn Tuần / Tháng cần lên lịch]
    S1 --> S2[Chọn nhân viên & gán vào Shift tương ứng]
    S2 --> S3[Hệ thống kiểm tra xung đột lịch trình]
    S3 --> S4{Có xung đột? \n(Overlapping/Double-booking)}
    S4 -->|Có| S5[Hiển thị cảnh báo xung đột & yêu cầu đổi lịch]
    S5 --> S2
    S4 -->|Không| S6[Lưu lịch làm việc ở trạng thái DRAFT]
    S6 --> S7[Manager nhấn Publish Schedule]
    S7 --> S8[Gửi thông báo lịch làm việc mới cho nhân viên]
    S8 --> END([Nhân viên xem lịch làm việc trên portal])

    style S4 fill:#ff9900,color:#fff
```

> **Business Rules áp dụng:**
>
> - `BR-STAFF-01` — Ràng buộc lịch làm việc của nhân viên, chống trùng lịch làm việc hoặc trùng chuyến đi tour.

---

## WF-22 — Workflow Engine (Dynamic BPMN)

**Use Cases:** Use Case "Workflow Configuration & Execution"
**Business Rules:** BR-WF-01, BR-WF-02, BR-SYS-09
**Actors:** System, Admin

```mermaid
flowchart TD
    START([Hệ thống phát sinh sự kiện]) --> S1[Workflow Engine nhận Event Signal]
    S1 --> S2[Tải cấu hình JSON của Workflow đang hoạt động]
    S2 --> S3[Phân tích điều kiện conditions_json]
    S3 --> S4{Điều kiện thỏa mãn?}
    S4 -->|Không| END_N([Bỏ qua & thực hiện luồng mặc định])
    S4 -->|Có| S5[Tạo tiến trình chạy Workflow Instance]
    S5 --> S6[Thực thi actions_json: chuyển trạng thái thành Pending_Approval]
    S6 --> S7[Gửi thông báo phê duyệt tới Manager/Admin]
    S7 --> S8{Approver duyệt?}
    S8 -->|Từ chối| S9[Chuyển trạng thái sang REJECTED & Rollback/Hủy]
    S8 -->|Phê duyệt| S10[Chuyển trạng thái sang APPROVED & Commit giao dịch]
    S9 & S10 --> END_S([Ghi nhật ký Workflow Instance và hoàn tất])

    style S4 fill:#ff9900,color:#fff
    style S8 fill:#ff9900,color:#fff
```

> **Business Rules áp dụng:**
>
> - `BR-SYS-09` — Chỉ Admin được cấu hình Workflow qua JSON.
> - `BR-WF-01/02` — Tự động hóa quy trình phê duyệt các giao dịch vượt hạn mức khuyến mãi hoặc ngoại lệ hệ thống.

---

## WF-23 — Đặt Bàn Trực Tuyến

<<<<<<< HEAD
<<<<<<< HEAD
**Use Cases:** UC21, UC16
**Business Rules:** TABLE-002, TABLE-003, TABLE-005, TABLE-008
=======
**Use Cases:** UC21, UC16  
**Business Rules:** TABLE-002, TABLE-003, TABLE-005, TABLE-008  
>>>>>>> 7414e299dc9443033140483710eb1236086b60de
=======
**Use Cases:** UC21, UC16
**Business Rules:** TABLE-002, TABLE-003, TABLE-005, TABLE-008
>>>>>>> origin/dev
**Actors:** Customer, F&B Staff, System

```mermaid
flowchart TD
    START([Khách tìm bàn ăn]) --> B1[Nhập số khách\nvà ngày giờ mong muốn]
    B1 --> B2[Hiển thị danh sách bàn\ntrạng thái Available]
    B2 --> B3[Khách chọn bàn và xác nhận]
    B3 --> V1{Số khách vượt\nCapacity bàn?\nTABLE-002}
    V1 -->|Vượt quá| E1[Lỗi: Số khách vượt\nsức chứa của bàn]
    E1 --> B2
    V1 -->|OK| V2{Thời gian bị\ntrùng lịch Overlap?\nTABLE-003}
    V2 -->|Trùng lịch| E2[Lỗi: Thời gian đặt bàn\nbị trùng lặp với lịch đã có]
    E2 --> B2
    V2 -->|OK| SAVE[INSERT Table_Reservations\nstatus = Confirmed]
    SAVE --> MAIL[Gửi Email xác nhận đặt bàn\nTABLE-008]
    MAIL --> DONE_OK([Đặt bàn thành công])

    subgraph ARRIVE["Khi khách đến nhà hàng"]
        A1([F&B Staff kiểm tra mã đặt bàn]) --> A2[UPDATE Table_Reservations\nstatus = Seated]
        A2 --> A3[UPDATE Restaurant_Tables\nstatus = Occupied]
        A3 --> A4([Check-in bàn hoàn tất])
    end

    style V1 fill:#ff9900,color:#fff
    style V2 fill:#ff4444,color:#fff
    style SAVE fill:#00aa44,color:#fff
    style MAIL fill:#0066cc,color:#fff
```

> **Business Rules áp dụng:**
<<<<<<< HEAD
<<<<<<< HEAD
>
=======
>>>>>>> 7414e299dc9443033140483710eb1236086b60de
=======
>
>>>>>>> origin/dev
> - `TABLE-002` — Không cho phép đặt bàn nếu số khách vượt quá sức chứa (capacity).
> - `TABLE-003` — Chặn đặt bàn nếu thời gian bị trùng lặp (Overlap) với lịch đã có.
> - `TABLE-008` — Gửi email xác nhận tự động sau khi đặt bàn thành công.

---

## WF-24 — Hủy Đơn Hàng F&B & Hoàn Tiền

<<<<<<< HEAD
<<<<<<< HEAD
**Use Cases:** UC19
**Business Rules:** POS-004, POS-006
=======
**Use Cases:** UC19  
**Business Rules:** POS-004, POS-006  
>>>>>>> 7414e299dc9443033140483710eb1236086b60de
=======
**Use Cases:** UC19
**Business Rules:** POS-004, POS-006
>>>>>>> origin/dev
**Actors:** Customer, F&B Staff, System

```mermaid
flowchart TD
    START([Yêu cầu Hủy Đơn Hàng]) --> Q1[Lấy thông tin Order từ hệ thống]
    Q1 --> Q2{Order đang\nPREPARING?\nPOS-004}
    Q2 -->|Có - Bếp đang nấu| ERR1[Lỗi 403: Không thể hủy\nđơn đang được chế biến]
    ERR1 --> DONE_FAIL([Kết thúc - Từ chối Hủy])

    Q2 -->|Không| Q3{Phương thức\nthanh toán?}

    Q3 -->|CHARGE_TO_ROOM| R1[Lấy Folio_Items\nliên kết với đơn hàng]
    R1 --> R2[DELETE Folio_Items\nhoàn lại Credit Limit cho phòng]
    R2 --> R3[Cập nhật sub_credit_limit\ncộng lại số tiền đã ghi nợ]
    R3 --> CANCEL

    Q3 -->|VNPay| V1[Tính số tiền đã thanh toán]
    V1 --> V2[Gọi VNPay Refund API\ntạo RefundRequest]
    V2 --> V3{Refund\nthành công?}
    V3 -->|Thất bại| ERR2[Ghi lỗi Refund\nAdmin xử lý thủ công]
    ERR2 --> DONE_FAIL
    V3 -->|Thành công| CANCEL

    Q3 -->|Chưa thanh toán| CANCEL

    CANCEL[UPDATE Food_Orders\norder_status = Cancelled] --> KOT[UPDATE Food_Order_Details\nkot_status = Cancelled]
    KOT --> WS[WebSocket broadcast\nHủy KOT xuống bếp - BR-FB-02]
    WS --> DONE_OK([Hủy đơn thành công])

    style Q2 fill:#ff4444,color:#fff
    style R2 fill:#0066cc,color:#fff
    style V2 fill:#0066cc,color:#fff
    style CANCEL fill:#ff9900,color:#fff
    style WS fill:#9933ff,color:#fff
```

> **Business Rules áp dụng:**
<<<<<<< HEAD
<<<<<<< HEAD
>
=======
>>>>>>> 7414e299dc9443033140483710eb1236086b60de
=======
>
>>>>>>> origin/dev
> - `POS-004` — Không được hủy đơn nếu trạng thái đang là `PREPARING` (bếp đang nấu).
> - `POS-006` — Đảm bảo tính nhất quán hoàn tiền: Charge-to-Room xóa Folio; VNPay gọi Refund API.

---

## WF-25 — Chốt Ca & Báo Cáo F&B

<<<<<<< HEAD
<<<<<<< HEAD
**Use Cases:** UC18
**Business Rules:** BR-FIN-15, BR-SYS-04
=======
**Use Cases:** UC18  
**Business Rules:** BR-FIN-15, BR-SYS-04  
>>>>>>> 7414e299dc9443033140483710eb1236086b60de
=======
**Use Cases:** UC18
**Business Rules:** BR-FIN-15, BR-SYS-04
>>>>>>> origin/dev
**Actors:** F&B Manager

```mermaid
flowchart TD
    START([F&B Manager bấm Chốt Ca]) --> S1[Nhập ngày cần chốt\nMặc định: hôm nay]
    S1 --> S2{Ngày này đã\nchốt trước đó?\nIdempotent Guard}
    S2 -->|Đã chốt rồi| ERR1[Lỗi: Báo cáo ngày\nhôm nay đã được chốt]
    ERR1 --> DONE_FAIL([Kết thúc - Bị chặn])

    S2 -->|Chưa chốt| S3[Query toàn bộ Food_Orders\ncủa ngày được chọn]
    S3 --> S4[Lọc đơn hàng\nis_paid_in_pos = true\nBR-FIN-15]
    S4 --> S5[Tính tổng doanh thu\ntotal_revenue = SUM đơn đã lọc]
    S5 --> S6[INSERT FnB_Daily_Reports\nLưu Snapshot cố định]
    S6 --> AUDIT[INSERT Audit_Logs\nghi nhận hành động chốt ca\nBR-SYS-04]
    AUDIT --> DONE_OK([Chốt ca thành công])

    style S2 fill:#ff9900,color:#fff
    style S4 fill:#ff9900,color:#fff
    style S6 fill:#00aa44,color:#fff
    style AUDIT fill:#9933ff,color:#fff
```

<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> origin/dev
> **Business Rules áp dụng:**
>
> - `BR-FIN-15` — Báo cáo doanh thu chỉ tính đơn hàng `isPaidInPos = true`.
> - Idempotent: 1 ngày chỉ được chốt 1 lần duy nhất, tránh ghi đúp dữ liệu.
> - `BR-SYS-04` — Mọi hành động chốt ca ảnh hưởng tài chính phải ghi Audit_Logs.

<<<<<<< HEAD
## WF-26 — Phê duyệt Thiết bị Vận hành (Ops Device Authorization)

**Use Cases:** UC05.4
**Business Rules:** BR-SYS-02, BR-SYS-04
=======
## WF-26 — Phê duyệt Thiết bị Vận hành (Ops Device Authorization)

**Use Cases:** UC05.4  
**Business Rules:** BR-SYS-02, BR-SYS-04  
>>>>>>> 7414e299dc9443033140483710eb1236086b60de
=======
## WF-26 — Phê duyệt Thiết bị Vận hành (Ops Device Authorization)

**Use Cases:** UC05.4
**Business Rules:** BR-SYS-02, BR-SYS-04
>>>>>>> origin/dev
**Actors:** Admin, Toàn bộ nhân viên Ops, System

```mermaid
flowchart TD
    START([Nhân viên đăng nhập /ops-login]) --> S1[Nhập username + password\n+ gửi device_id tự động]
    S1 --> S2{Tài khoản thuộc\nnhóm Ops/Nhân viên?\nROLE_ADMIN, ROLE_RECEPTIONIST...}
    S2 -->|Không - Là Khách| S3[Bỏ qua check thiết bị\nĐăng nhập theo luồng thường]
    S3 --> DONE_FAIL([Đăng nhập OK])
<<<<<<< HEAD
<<<<<<< HEAD
  
    S2 -->|Có - Là Ops| S4{Thiết bị device_id\nđã được đăng ký?}
    S4 -->|Chưa đăng ký| S5[Hệ thống tự động đăng ký thiết bị\nisApproved = true - Chạy Dev]
    S5 --> DONE_OK([Đăng nhập thành công])
  
=======
    
    S2 -->|Có - Là Ops| S4{Thiết bị device_id\nđã được đăng ký?}
    S4 -->|Chưa đăng ký| S5[Hệ thống tự động đăng ký thiết bị\nisApproved = true - Chạy Dev]
    S5 --> DONE_OK([Đăng nhập thành công])
    
>>>>>>> 7414e299dc9443033140483710eb1236086b60de
=======
  
    S2 -->|Có - Là Ops| S4{Thiết bị device_id\nđã được đăng ký?}
    S4 -->|Chưa đăng ký| S5[Hệ thống tự động đăng ký thiết bị\nisApproved = true - Chạy Dev]
    S5 --> DONE_OK([Đăng nhập thành công])
  
>>>>>>> origin/dev
    S4 -->|Đã đăng ký| S6{Được duyệt?\nisApproved = true}
    S6 -->|Đã duyệt| DONE_OK
    S6 -->|Chưa duyệt/Bị khóa| S7["Hủy Session và Logout ngay lập tức\nrequest.getSession().invalidate()"]
    S7 --> S8[Redirect về /ops-login\nHiển thị lỗi thiết bị không được cấp phép]
    S8 --> DONE_FAIL

    subgraph ADMIN_CONTROL["Quản lý Thiết bị - Admin"]
        A1([Admin vào Device Management]) --> A2[Xem danh sách thiết bị Ops]
        A2 --> ACT{Hành động}
        ACT -->|Duyệt/Khóa| A3[UPDATE AuthorizedDevice\nisApproved = !isApproved]
        ACT -->|Xóa| A4[DELETE AuthorizedDevice]
        A3 & A4 --> AUDIT[INSERT Audit_Logs\nBR-SYS-04]
    end

    style S6 fill:#ff9900,color:#fff
    style S7 fill:#ff4444,color:#fff
    style S5 fill:#00aa44,color:#fff
    style A3 fill:#ff9900,color:#fff
    style A4 fill:#ff4444,color:#fff
```

> **Business Rules áp dụng:**
<<<<<<< HEAD
<<<<<<< HEAD
>
=======
>>>>>>> 7414e299dc9443033140483710eb1236086b60de
=======
>
>>>>>>> origin/dev
> - `BR-SYS-02` — Chỉ thiết bị Ops được phê duyệt (Approved) mới có quyền duy trì Session làm việc.
> - `BR-SYS-04` — Ghi log mọi hành động thay đổi quyền phê duyệt thiết bị của Admin.

---

## Bảng Tóm tắt — Tất cả Workflows

<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> origin/dev
| Workflow                                | UC Liên quan                  | Business Rules chính           | Actors                 | Mức độ |
| :-------------------------------------- | :----------------------------- | :------------------------------ | :--------------------- | :-------: |
| WF-01 Xác thực & Đăng ký           | UC01, UC02, UC03               | BR-SYS-01,02,06,07              | Guest, All Users       |   HIGH   |
| WF-02 Đặt phòng & Cọc               | UC10, UC11, UC12.1             | BR-FO-01,02; BR-FIN-06          | Customer, System       | CRITICAL |
| WF-03 Check-in                          | UC12.2, UC12.3, UC12.4         | BR-FO-03,04,06; BR-DATA-02      | Receptionist           | CRITICAL |
| WF-04 Check-out & Invoice               | UC12.6, UC26.5, UC27.4         | BR-FIN-01; BR-FO-04; BR-HK-01   | Receptionist           | CRITICAL |
| WF-05 F&B / Post-to-Room                | UC14, UC16, UC17, UC18, UC19   | BR-FB-01,02,04; BR-FO-06        | Customer, F&B, Kitchen | CRITICAL |
| WF-06 Tour & AI Attendance              | UC20, UC21, UC22               | BR-TR-01,02,05                  | Customer, Tour Guide   | CRITICAL |
| WF-07 Room Lifecycle                    | UC13                           | BR-FO-04,05; BR-HK-01,02        | HK, Maintenance        |   HIGH   |
| WF-08 Night Audit                       | UC27.1, UC27.2                 | BR-FIN-03,04                    | System (Scheduler)     | CRITICAL |
| WF-09 Hủy Phòng & Hoàn tiền         | UC12 (Hủy đặt phòng), UC27 | BR-FIN-02                       | Customer               |   HIGH   |
| WF-10 Hủy Tour & Hoàn tiền           | UC22.4, UC27                   | BR-TR-05                        | System, Admin          |   HIGH   |
| WF-11 Review & Kiểm duyệt             | UC24, UC25                     | BR-TR-03,04; BR-SYS-04          | Customer, Admin        |  MEDIUM  |
| WF-12 Quản lý Nhân viên             | UC01.2, UC05.1, UC05.2         | BR-SYS-04,07; BR-DATA-03        | Admin                  |   HIGH   |
| WF-13 Master Flow                       | All                            | All                             | All Actors             |    —    |
<<<<<<< HEAD
| WF-14 Walk-in Check-in                  | UC12.7                         | BR-FO-08                        | Receptionist           | CRITICAL |
| WF-15 Remote CCCD Scan                  | UC04.2, UC12.3                 | BR-FIN-07                       | Customer               |   HIGH   |
| WF-16 Tour GPS & Itinerary              | UC22.3, UC22.4                 | BR-TR-06,07,08,09               | Tour Guide             |   HIGH   |
| WF-17 Đổi Hạng Phòng                | UC12.5, UC09.3                 | BR-FO-07                        | Receptionist           |   HIGH   |
| WF-18 Membership Tier & Loyalty         | UC26.6                         | BR-MEM-01                       | System                 |  MEDIUM  |
| WF-19 Refund Request                    | UC27                           | BR-FIN-08                       | Manager                |  MEDIUM  |
| WF-20 Add-On Services                   | UC23                           | BR-FB-05                        | Customer               |  MEDIUM  |
| WF-21 Staff Scheduling                  | UC22.2                         | BR-STAFF-01                     | Admin, Manager         |  MEDIUM  |
| WF-22 Workflow Engine                   | UC09.8                         | BR-WF-01,02                     | System, Admin          |   HIGH   |
| WF-23 Đặt bàn trực tuyến           | UC21, UC16                     | TABLE-002, TABLE-003, TABLE-008 | Customer, F&B Staff    |   HIGH   |
| WF-24 Hủy đơn F&B & Hoàn tiền      | UC19                           | POS-004, POS-006                | Customer, F&B Staff    |   HIGH   |
| WF-25 Chốt ca & Báo cáo F&B          | UC18                           | BR-FIN-15, BR-SYS-04            | F&B Manager            |   HIGH   |
| WF-26 Phê duyệt Thiết bị Vận hành | UC05.4                         | BR-SYS-02, BR-SYS-04            | Admin                  |  MEDIUM  |
=======
| Workflow | UC Liên quan | Business Rules chính | Actors | Mức độ |
|:---------|:------------|:--------------------|:-------|:------:|
| WF-01 Xác thực & Đăng ký | UC01, UC02, UC03 | BR-SYS-01,02,06,07 | Guest, All Users | HIGH |
| WF-02 Đặt phòng & Cọc | UC10, UC11, UC12.1 | BR-FO-01,02; BR-FIN-06 | Customer, System | CRITICAL |
| WF-03 Check-in | UC12.2, UC12.3, UC12.4 | BR-FO-03,04,06; BR-DATA-02 | Receptionist | CRITICAL |
| WF-04 Check-out & Invoice | UC12.6, UC26.5, UC27.4 | BR-FIN-01; BR-FO-04; BR-HK-01 | Receptionist | CRITICAL |
| WF-05 F&B / Post-to-Room | UC14, UC16, UC17, UC18, UC19 | BR-FB-01,02,04; BR-FO-06 | Customer, F&B, Kitchen | CRITICAL |
| WF-06 Tour & AI Attendance | UC20, UC21, UC22 | BR-TR-01,02,05 | Customer, Tour Guide | CRITICAL |
| WF-07 Room Lifecycle | UC13 | BR-FO-04,05; BR-HK-01,02 | HK, Maintenance | HIGH |
| WF-08 Night Audit | UC27.1, UC27.2 | BR-FIN-03,04 | System (Scheduler) | CRITICAL |
| WF-09 Hủy Phòng & Hoàn tiền | UC12 (Hủy đặt phòng), UC27 | BR-FIN-02 | Customer | HIGH |
| WF-10 Hủy Tour & Hoàn tiền | UC22.4, UC27 | BR-TR-05 | System, Admin | HIGH |
| WF-11 Review & Kiểm duyệt | UC24, UC25 | BR-TR-03,04; BR-SYS-04 | Customer, Admin | MEDIUM |
| WF-12 Quản lý Nhân viên | UC01.2, UC05.1, UC05.2 | BR-SYS-04,07; BR-DATA-03 | Admin | HIGH |
| WF-13 Master Flow | All | All | All Actors | — |
| WF-14 Walk-in Check-in | UC12.7 | BR-FO-08 | Receptionist | CRITICAL |
| WF-15 Remote CCCD Scan | UC04.2, UC12.3 | BR-FIN-07 | Customer | HIGH |
| WF-16 Tour GPS & Itinerary | UC22.3, UC22.4 | BR-TR-06,07,08,09 | Tour Guide | HIGH |
| WF-17 Đổi Hạng Phòng | UC12.5, UC09.3 | BR-FO-07 | Receptionist | HIGH |
| WF-18 Membership Tier & Loyalty | UC26.6 | BR-MEM-01 | System | MEDIUM |
| WF-19 Refund Request | UC27 | BR-FIN-08 | Manager | MEDIUM |
| WF-20 Add-On Services | UC23 | BR-FB-05 | Customer | MEDIUM |
| WF-21 Staff Scheduling | UC22.2 | BR-STAFF-01 | Admin, Manager | MEDIUM |
| WF-22 Workflow Engine | UC09.8 | BR-WF-01,02 | System, Admin | HIGH |
| WF-23 Đặt bàn trực tuyến | UC21, UC16 | TABLE-002, TABLE-003, TABLE-008 | Customer, F&B Staff | HIGH |
| WF-24 Hủy đơn F&B & Hoàn tiền | UC19 | POS-004, POS-006 | Customer, F&B Staff | HIGH |
| WF-25 Chốt ca & Báo cáo F&B | UC18 | BR-FIN-15, BR-SYS-04 | F&B Manager | HIGH |
| WF-26 Phê duyệt Thiết bị Vận hành | UC05.4 | BR-SYS-02, BR-SYS-04 | Admin | MEDIUM |
>>>>>>> 7414e299dc9443033140483710eb1236086b60de
=======
| WF-14 | [Walk-in Check-in](#wf-14--walk-in-check-in)                                       | Front Office   | CRITICAL |
| WF-15 | [Remote CCCD Scan](#wf-15--remote-cccd-scan)                                       | Front Office   |   HIGH   |
| WF-16 | [Tour Itinerary & GPS Checkpoint](#wf-16--tour-itinerary--gps-checkpoint)      | Tour           |   HIGH   |
| WF-17 | [Đổi Hạng Phòng](#wf-17--đổi-hạng-phòng)                                           | Front Office   |   HIGH   |
| WF-18 | [Membership Tier & Loyalty Points](#wf-18--membership-tier--loyalty-points)     | Membership     |  MEDIUM  |
| WF-19 | [Refund Request Flow (Manual)](#wf-19--refund-request-flow-manual)                 | Finance        |  MEDIUM  |
| WF-20 | [Add-On Hotel Service](#wf-20--add-on-hotel-service)                               | F&B            |  MEDIUM  |
| WF-21 | [Staff Scheduling](#wf-21--staff-scheduling)                                       | Admin          |  MEDIUM  |
| WF-22 | [Workflow Engine (Dynamic BPMN)](#wf-22--workflow-engine-dynamic-bpmn)             | System         |   HIGH   |
| WF-23 | [Đặt Bàn Trực Tuyến](#wf-23--đặt-bàn-trực-tuyến)                                 | F&B            |   HIGH   |
| WF-24 | [Hủy Đơn Hàng F&B & Hoàn Tiền](#wf-24--hủy-đơn-hàng-fb--hoàn-tiền)     | F&B            |   HIGH   |
| WF-25 | [Chốt Ca & Báo Cáo F&B](#wf-25--chốt-ca--báo-cáo-fb)                         | F&B            |   HIGH   |
| WF-26 | [Phê duyệt Thiết bị Vận hành](#wf-26--phê-duyệt-thiết-bị-vận-hành)               | Admin          |  MEDIUM  |
>>>>>>> origin/dev

---

*Tài liệu Business Workflows được xây dựng từ BusinessRule.md và RequirementsTraceabilityMatrix.md của dự án **Kawai Retreat Resort & Hub**, Group 2 — SWP391 SE2023.*
