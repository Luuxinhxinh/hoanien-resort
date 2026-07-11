# Workflow: `folio-detail.html` — Quản Lý Hóa Đơn Phòng (Folio)

> Module 5 · Phân hệ Tài chính & Thanh toán · Lễ tân

---

## Tổng quan

Trang `folio-detail.html` là giao diện chính để Lễ tân quản lý toàn bộ công nợ (Folio) của khách trong một lần lưu trú. Các chức năng bao gồm:

- **Xem hóa đơn** tổng hợp (phòng, ăn uống, tour, spa)
- **Tách hóa đơn** (Split) — tách riêng một số khoản để khách thanh toán riêng
- **Chuyển tuyến** (Route) — dịch chuyển khoản nợ giữa các phòng trong cùng một đoàn
- **Thanh toán & Trả phòng** (Checkout) — ghi nhận payment và đóng Folio

---

## 1. Luồng Khởi Tạo Trang (Page Load)

```
URL: /receptionist/folio/detail?id={roomBookingDetailId}
```

```
[Trình duyệt]
    │
    ├─► DOMContentLoaded → lấy ?id từ URL
    │
    ├─► fetchBookingGroup(id)
    │       └─► GET /api/folios/booking-group/{id}
    │               └─► Controller: FolioRestController
    │                       └─► Lấy danh sách các phòng cùng đoàn
    │                       └─► Trả về: { rooms: [{id, roomNumber},...] }
    │               └─► DB: Room_Booking_Details, Room_Bookings, Rooms
    │       └─► renderTabs(rooms) → vẽ các Tab chuyển phòng
    │
    └─► fetchFolioData(id)
            └─► GET /api/folios/room/{id}
                    └─► Controller: FolioRestController.getFolioByRoom()
                    └─► Service: NightAuditService.getFolioItems()
                    └─► Service: NightAuditService.calculateFolioBalance()
                    └─► Service: PaymentService.getPaymentsByBookingId() (lấy tiền cọc)
                    └─► DB: Folio_Items, Room_Booking_Details, Room_Bookings,
                                Customers, Rooms, Payment_Transactions
                    └─► Trả về JSON toàn bộ thông tin khách + các khoản nợ
            └─► renderFolio(data) → vẽ giao diện 2 cột (Hóa đơn gốc / Hóa đơn tách)
```

**Dữ liệu hiển thị lên giao diện:**

| Phần UI | Lấy từ trường nào |
| :--- | :--- |
| Tên khách (Avatar) | `data.guestName` ← `Customers.full_name` |
| Số phòng | `data.roomNumber` ← `Rooms.room_number` |
| Ngày lưu trú | `data.checkInDate` / `data.checkOutDate` ← `Room_Bookings` |
| Danh sách khoản nợ (cột trái) | `data.items[]` ← `Folio_Items` |
| Credit Limit | `data.subCreditLimit` ← `Room_Booking_Details.sub_credit_limit` |
| Tiền cọc đã cọc | `data.prePaidDeposit` ← `Payment_Transactions` |
| Tổng nợ (Outstanding) | `data.currentBalance` ← Tính bởi `NightAuditService` |

---

## 2. Luồng Tách Hóa Đơn (Split)

**Trigger:** Lễ tân tick chọn các món → Bấm nút **"Tách thanh toán (Split)"**

```
[Frontend]
    │
    ├─► openSplitModal()   → Hiện modal xác nhận
    └─► confirmSplit()
            │
            ├─► Với mỗi selectedItemId:
            │       PUT /api/folios/items/{id}/split
            │       Body: { isSettledSeparately: true }
            │
            └─► Controller: FolioRestController.splitFolioItem()
                    │
                    ├─► folioItemRepository.findById(id)
                    ├─► item.setIsSettledSeparately(true)
                    └─► folioItemRepository.save(item)
                            └─► DB: UPDATE Folio_Items SET is_settled_separately = 1
                    │
                    └─► Response: { success: true }
            │
            └─► fetchFolioData() → Tải lại trang
                    → Món hàng di chuyển sang hộp "Hóa đơn tách"
```

**Bảng DB tác động:** `Folio_Items` (cột `is_settled_separately`)

---

## 3. Luồng Chuyển Tuyến Hóa Đơn (Routing)

**Trigger:** Lễ tân chọn món → Bấm **"Chuyển phòng (Route)"** → Chọn phòng đích → Xác nhận

```
[Frontend]
    │
    ├─► openRouteModal()   → Hiện Modal chọn phòng đích
    └─► confirmRoute()
            │
            ├─► Với mỗi selectedItemId:
            │       PUT /api/folios/items/{id}/route
            │       Body: { targetRoomBookingDetailId: {id_phòng_đích} }
            │
            └─► Controller: FolioRestController.routeFolioItem()
                    │
                    ├─► folioItemRepository.findById(id)
                    ├─► roomBookingDetailRepository.findById(targetId)  ← kiểm tra phòng đích
                    ├─► item.setRoomBookingDetail(targetRoom)
                    └─► folioItemRepository.save(item)
                            └─► DB: UPDATE Folio_Items SET room_booking_detail_id = {target}
                    │
                    └─► Response: { success: true }
            │
            └─► fetchFolioData(currentRoomBookingDetailId) → Tải lại trang hiện tại
                    → Món hàng biến mất. Chuyển sang Tab phòng đích sẽ thấy món xuất hiện.
```

**Bảng DB tác động:** `Folio_Items` (cột `room_booking_detail_id`)

---

## 4. Luồng Thanh Toán & Trả Phòng (Checkout / Payment)

**Trigger:** Bấm **"Thanh toán HĐ Gốc"** hoặc **"Thanh toán HĐ Tách"**

```
[Frontend]
    │
    ├─► openPaymentModal(amount)  → Modal chọn phương thức (Cash / VNPAY)
    └─► processPayment()
            │
            └─► POST /api/folios/room/{id}/checkout
                Body: { paymentAmount: X, paymentMethod: "CASH" | "VNPAY" }
            │
            └─► Controller: FolioRestController.checkoutFolio()
                    │
                    ├─► [Validate]
                    │       roomBookingDetailRepository.findById(id)
                    │       NightAuditService.calculateFolioBalance(id)
                    │       → Nếu số tiền đưa < nợ: trả lỗi 400
                    │
                    ├─► [Cập nhật trạng thái]
                    │       detail.setDetailStatus("Checked_Out")
                    │       room.setRoomStatus("Vacant_Dirty")
                    │       DB: Room_Booking_Details, Rooms
                    │
                    ├─► [Lập hóa đơn tổng]
                    │       ConsolidatedInvoice invoice = new Invoice("INV-XXXX")
                    │       invoice.setSubtotal(total / 1.1)
                    │       invoice.setVat(total - subtotal)
                    │       DB: Consolidated_Invoices
                    │
                    ├─► [Ghi giao dịch]
                    │       PaymentService.recordPayment(invoice, amount, method)
                    │       DB: Payment_Transactions (status = SUCCESS)
                    │
                    ├─► [Nếu VNPAY]
                    │       VnPayService.createPaymentUrl(txn, remoteAddr)
                    │       → Response: { paymentUrl: "https://sandbox.vnpayment.vn/..." }
                    │       → Frontend redirect sang cổng VNPay
                    │
                    └─► [Nếu CASH / BANK_TRANSFER]
                            InvoicePdfService.generateInvoicePdf(invoice)
                            EmailService.sendInvoiceEmail(email, invoice, pdfPath)
                            → Response: { success: true, invoiceNumber: "INV-XXXX" }
                    │
                    └─► Frontend: showSuccessModal(data)  → Modal xanh lá + nút In hóa đơn
```

**Bảng DB tác động:**

| Bảng | Thay đổi |
| :--- | :--- |
| `Room_Booking_Details` | `detail_status = 'Checked_Out'` |
| `Rooms` | `room_status = 'Vacant_Dirty'`, `current_booking_detail_id = NULL` |
| `Consolidated_Invoices` | Thêm mới dòng hóa đơn tổng |
| `Payment_Transactions` | Thêm mới dòng ghi nhận giao dịch |

---

## 5. Danh Sách Services Liên Quan

| Service | File | Vai trò |
| :--- | :--- | :--- |
| `NightAuditService` | `NightAuditServiceImpl.java` | Tính tổng nợ Folio (cộng charges, trừ deposit) |
| `PaymentService` | `PaymentServiceImpl.java` | Tạo và lưu lịch sử giao dịch `Payment_Transactions` |
| `VnPayService` | `VnPayServiceImpl.java` | Sinh link thanh toán qua cổng VNPay (mã hóa SHA512) |
| `InvoicePdfService` | `InvoicePdfServiceImpl.java` | Render HTML/Thymeleaf → xuất file `.pdf` |
| `EmailService` | `EmailServiceImpl.java` | Gửi email đính kèm PDF hóa đơn cho khách tự động |

---

## 6. Sơ Đồ Liên Kết Bảng (Dữ liệu hóa đơn được tổng hợp từ)

```
Room_Booking_Details (id = URL param)
    │
    ├──► Rooms               → room_number (số phòng hiển thị)
    │
    ├──► Room_Bookings       → check_in_date, check_out_date, customer_id
    │         └──► Customers → full_name (tên khách), email
    │
    ├──► Folio_Items         → amount, description, source_department,
    │                          is_settled_separately (Split hay không)
    │
    └──► Payment_Transactions → amount (tiền đã cọc, type='DEPOSIT', status='SUCCESS')
```
