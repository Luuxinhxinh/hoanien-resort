# 📋 DANH SÁCH TEST CASE MOD2 — ĐẦY ĐỦ & CHI TIẾT
## Kawai Resort — Module 2: Quản lý Phòng, Nghiệp vụ Sảnh & Buồng Phòng

> **Chuẩn**: ISO/IEC/IEEE 29119-3:2021  
> **Cập nhật**: 2026-07-08 (sync Business Rules v2)  
> **Nguồn**: Đọc trực tiếp từ 7 file test Java trong project  
> **Vị trí source**: `05-Development/kawai-backend/src/test/java/com/kawai/`

---

## 📁 NGUỒN FILE TEST

| File Test | UC | Số TC |
|-----------|-----|-------|
| `BookingServiceUC10Test.java` | UC10 — Đặt phòng Online | 11 |
| `RoomServiceUC11Test.java` | UC11 — Dashboard phòng | 2 |
| `CheckinServiceUC12Test.java` | UC12 — Check-in có đặt trước | 13 |
| `WalkInCheckInServiceUC14Test.java` | UC14 — Walk-in Check-in | 12 |
| `DependentServiceUC16Test.java` | UC16 — Đăng ký khách đi kèm | 9 |
| `HousekeepingServiceUC13Test.java` | UC13 — Housekeeping & Bảo trì | 5 |
| `ChangeRoomCategoryServiceUC44Test.java` | UC44 — Đổi hạng phòng | 20 |
| `VnPayPaymentUCTest.java` | UC-PAY — Thanh toán VNPay | 6 |
| **TỔNG** | | **78 TC active** |

---

## 🔵 PHẦN 1: TÌM KIẾM & ĐẶT PHÒNG ONLINE — Customer

### UC10 — Đặt phòng & Thanh toán cọc (`BookingServiceUC10Test.java`)

| **TC ID** | **Method Test** | **Kịch bản** | **Expected** | **Business Rule** | **Severity** | **Status** |
|-----------|-----------------|--------------|--------------|-------------------|--------------|------------|
| **TC-M2-003** | `TC_M2_003_createBooking_success_returnsPendingWithCancellationDeadline` | Đặt phòng thành công — phòng R101, Deluxe, 5 đêm, cọc 3tr | `status="Pending"`, `cancellationDeadline = checkIn - 2 ngày (2026-07-13)`, `bookingId not-null`, `depositAmount` khớp | BR-RSV-07, BR-RSV-04 | CRITICAL | ✅ |
| **TC-M2-004** | `TC_M2_004_createBooking_concurrency_onlyOneConfirmed` | Concurrency: 2 user cùng đặt R101 đồng thời | `successCount=1` (trả `Pending`), `conflictCount=1` (nhận `RoomNotAvailableException`) | BR-RSV-05 (Pessimistic Lock) | CRITICAL | ✅ |
| **TC-M2-005** | `TC_M2_005_createBooking_checkOutEqualsCheckIn_throwsDateException` | `checkOut == checkIn` (0 đêm) | `IllegalArgumentException`, message chứa "checkout" hoặc "ngày" | BR-RSV-01 | CRITICAL | ✅ |
| **TC-M2-005b** | `TC_M2_005b_createBooking_checkOutBeforeCheckIn_throwsDateException` | `checkOut < checkIn` (ngược thời gian) | `IllegalArgumentException` | BR-RSV-01 | HIGH | ✅ |
| **TC-M2-006** | `TC_M2_006_cancelBooking_before48h_fullRefundAndCorrectStatus` | Hủy trước 48h — booking id=201, cọc 3.5tr, deadline = now+3 ngày | `status="Cancelled_Refunded"`, `depositAmount=3.500.000` hoàn lại đủ, `save()` được gọi | BR-FIN-01, BR-RSV-08 | HIGH | ✅ |
| **TC-M2-007** | `TC_M2_007_cancelBooking_within48h_zeroRefundAndForfeitedStatus` | Hủy trong 48h — deadline đã qua (now-1 ngày) | `status="Cancelled_Forfeited"`, `depositAmount=0` (tịch thu), `save()` được gọi | BR-FIN-01, BR-RSV-08 | HIGH | ✅ |
| **TC-M2-008** | `TC_M2_008_createBooking_validPromoCode_appliesDiscount` | Mã `SUMMER10` (10% off, 5 đêm × 2tr) | `discountedPrice=9.000.000`, `cancellationDeadline=checkIn-2` | BR-RSV-06 | MEDIUM | ✅ |
| **TC-M2-008b** | `TC_M2_008b_createBooking_20pctPromo_3nights_correctDiscount` | Mã `EARLYBIRD20` (20% off, 3 đêm × 2tr) | `discountedPrice=4.800.000`, deadline được set | BR-RSV-06 | MEDIUM | ✅ |
| **TC-M2-009a** | `TC_M2_009a_createBooking_inactivePromo_throwsWithErrorCode` | Mã `EXPIRED2020` (`isActive=false`) | `IllegalArgumentException`, message chứa `[ERR_PROMO_INACTIVE]` | BR-RSV-06, BR-ERR-01 | MEDIUM | ✅ |
| **TC-M2-009b** | `TC_M2_009b_createBooking_expiredByDate_throwsWithErrorCode` | Mã `XMAS2025` (`isActive=true` nhưng `validTo=2025-12-31`) | `IllegalArgumentException`, message chứa `[ERR_PROMO_EXPIRED]` | BR-RSV-06, BR-ERR-01 | MEDIUM | ✅ |
| **TC-M2-009c** | `TC_M2_009c_createBooking_unknownPromoCode_throwsWithNotFoundCode` | Mã `GHOST999` không tồn tại trong DB | `IllegalArgumentException`, message chứa `[ERR_PROMO_NOT_FOUND]` | BR-RSV-06, BR-ERR-01 | MEDIUM | ✅ |

---

### UC11 — Front Desk Dashboard (`RoomServiceUC11Test.java`)

| **TC ID** | **Method Test** | **Kịch bản** | **Expected** | **Severity** | **Status** |
|-----------|-----------------|--------------|--------------|--------------|------------|
| **TC-M2-010a** | `TC_M2_010a_getRoomDashboard_withRooms_returnsCorrectList` | Dashboard có 2 phòng (R101 Vacant_Clean, R201 Occupied) | Danh sách đúng 2 phòng, `roomNumber`, `categoryName`, `roomStatus`, `pricePerNight`, `capacity` đúng | HIGH | ✅ |
| **TC-M2-010b** | `getRoomDashboard_emptyList_returnsEmptyNotNull` | DB không có phòng nào | Trả `empty list` — KHÔNG null | MEDIUM | ✅ |

---

## 🔵 PHẦN 2: THANH TOÁN VNPAY — Booking & Walk-in

### UC-PAY — Thanh toán VNPay (`VnPayPaymentUCTest.java`)

> ⚠️ **Chỉ lấy các test liên quan đến đặt phòng & Walk-in**

| **TC ID** | **Method Test** | **Kịch bản** | **Expected** | **Severity** | **Status** |
|-----------|-----------------|--------------|--------------|--------------|------------|
| **TC-PAY-001** | `testCreatePaymentUrl` | Tạo URL thanh toán cho booking id=123, deposit=150.000đ | URL bắt đầu bằng `https://sandbox.vnpayment.vn/...`, chứa `vnp_Amount=15000000` (×100), `vnp_TmnCode=77G0NGGT`, `vnp_SecureHash=<not null>` | HIGH | ✅ |
| **TC-PAY-002** | `testChecksumGeneration` | Sinh và xác thực chữ ký HMAC-SHA512 cho tập params booking | `secureHash` not-null, `validateSignature()` trả `true` | HIGH | ✅ |
| **TC-PAY-003** | `testVerifyIpn_InvalidChecksum` | IPN callback VNPay về với `vnp_SecureHash="wrong_hash_123"` | `RspCode="97"`, `Message="Invalid Checksum"` — HỦY booking | CRITICAL | ✅ |
| **TC-PAY-004** | `testVerifyIpn_AmountMismatch` | IPN về với `amount=200k` nhưng DB lưu `150k` (gian lận) | `RspCode="04"`, `Message="Invalid Amount"` — CHẶN xác nhận | CRITICAL | ✅ |
| **TC-PAY-005** | `testVerifyIpn_DuplicateIPN` | IPN callback trùng lặp — `txn.status=SUCCESS` đã xử lý rồi | `RspCode="02"`, `Message="Order already confirmed"` (idempotency guard) | HIGH | ✅ |
| **TC-PAY-006** | `testVerifyIpn_SuccessfulPayment` | IPN hợp lệ — `vnp_ResponseCode="00"`, amount khớp, txn=INIT | `RspCode="00"`, `Message="Confirm Success"`, `txn.status=SUCCESS`, `txn.paidAt` not-null, `booking.status="CONFIRMED"` | CRITICAL | ✅ |

---

## 🔵 PHẦN 3: CHECK-IN SẢNH (CÓ ĐẶT TRƯỚC) — Lễ tân

### UC12 — Check-in & Phân phòng vật lý (`CheckinServiceUC12Test.java`)

> **Lưu ý**: TC-M2-014, TC-M2-021, TC-M2-022 (liên quan `transferRoom`) đã được xóa khỏi file test vì chức năng đã migrate sang UC-44.

| **TC ID** | **Method Test** | **Kịch bản** | **Expected** | **Business Rule** | **Severity** | **Status** |
|-----------|-----------------|--------------|--------------|-------------------|--------------|------------|
| **TC-M2-011** | `checkIn_Success_RoomBecomesOccupied_FolioCreated` | Check-in thành công — booking CONFIRMED, phòng Vacant_Clean | `detail.status="CHECKED_IN"`, `room.status="Occupied"`, `save()` được gọi cho cả room & detail | BR-FO-07, BR-FIN-03 | CRITICAL | ✅ |
| **TC-M2-012a** | `checkIn_Fail_RoomDirty_ShouldThrowException` | Phòng R102 đang `Vacant_Dirty` | `IllegalStateException`, message chứa "dirty", KHÔNG gọi `save()` | BR-FO-07, MOD2-002 | HIGH | 🔴 RED |
| **TC-M2-012b** | `checkIn_Fail_RoomMaintenance_ShouldThrowException` | Phòng R103 đang `Maintenance` | `IllegalStateException`, message chứa "maintenance", KHÔNG gọi `save()` | BR-FO-07, MOD2-002 | HIGH | ✅ |
| **TC-M2-013** | `updateCreditLimit_Success_ShouldUpdateCreditLimit` | Cập nhật credit limit mới = 3tr (≤ master 5tr) | `subCreditLimit` đổi đúng giá trị mới, `findByRoomBookingId()` & `save()` được gọi | BR-FO-03 | MEDIUM | ✅ |
| **TC-M2-016** | `checkIn_BookingDetailNotFound_ShouldThrowException` | `bookingDetailId=9999` không tồn tại | `RuntimeException` (MOD2-003), KHÔNG gọi `roomRepository.findById()` | MOD2-003 | HIGH | ✅ |
| **TC-M2-017** | `checkIn_RoomNotFound_ShouldThrowException` | `roomId=8888` không tồn tại | `RuntimeException` (MOD2-003), KHÔNG gọi `save()` | MOD2-003 | HIGH | ✅ |
| **TC-M2-018** | `checkIn_BookingNotConfirmed_ShouldThrowException` | Booking ở trạng thái `Pending` (chưa cọc) | `IllegalStateException`, message chứa "confirmed" hoặc "pending", KHÔNG gọi `save()` | SRS UC-13 Preconditions | HIGH | ✅ |
| **TC-M2-019** | `checkIn_DetailAlreadyCheckedIn_ShouldThrowException` | BookingDetail đã `CHECKED_IN` — double check-in | `IllegalStateException`, message chứa "already" hoặc "checked_in" | Idempotency Guard | HIGH | ✅ |
| **TC-M2-020** | `checkIn_RoomOccupied_ShouldThrowException` | Phòng R104 đang `Occupied` (khách khác đang ở) | `IllegalStateException`, message chứa "occupied", KHÔNG gọi `save()` | MOD2-002 | HIGH | ✅ |
| **TC-M2-023** | `updateCreditLimit_NegativeValue_ShouldThrowException` | Credit limit = -1.000.000 (âm) | `IllegalArgumentException`, message chứa "âm"/"negative"/"limit" | MOD2-001 | MEDIUM | ✅ |
| **TC-M2-023b** | `updateCreditLimit_ZeroValue_ShouldSucceed` | Credit limit = 0 (reset hạn mức) | KHÔNG ném exception, `subCreditLimit = 0` | BR-FO-06 edge case | LOW | ✅ |
| **TC-M2-024** | `updateCreditLimit_DetailNotFound_ShouldThrowException` | `bookingDetailId=9999` không tồn tại | `RuntimeException` (MOD2-003), KHÔNG gọi `roomBookingRepository.save()` | MOD2-003 | MEDIUM | ✅ |
| **TC-M2-026** | `updateCreditLimit_ExceedsMasterLimit_ShouldThrowException` | Credit limit mới = 6tr > master limit 5tr | `BusinessException` với `errorCode="MOD2-UC14-016"`, KHÔNG gọi `save()` | Master Credit Limit | MEDIUM | ✅ |

---

## 🔵 PHẦN 4: WALK-IN CHECK-IN — Lễ tân

### UC14 — Walk-in Guest Check-in (`WalkInCheckInServiceUC14Test.java`)

| **TC ID** | **Method Test** | **Kịch bản** | **Expected** | **Business Rule** | **Severity** | **Status** |
|-----------|-----------------|--------------|--------------|-------------------|--------------|------------|
| **TC-M2-021** | `TC_M2_021_walkIn_happyPath_allStateMachinesTransitioned` | Happy path đầy đủ — CCCD "001234567890", phòng R301 Vacant_Clean, khách mới | `bookingId` not-null, `roomNumber="R301"`, `status="Checked_In"`, `isNewCustomer=true`; Room→OCCUPIED, Booking→CHECKED_IN (WALK_IN), Detail→CHECKED_IN; `accountRepository.save()` 1 lần, `passwordHash` not-null | BR-UC14-01~10 | CRITICAL | ✅ |
| **TC-M2-023/1** | `invalidCccdFormat_throwsMOD2UC14003` | CCCD = "INVALID_12" (không đủ 12 chữ số) | `BusinessException [MOD2-UC14-003]`, message "invalid identification", KHÔNG tạo Customer/Booking | BR-UC14-01 | HIGH | ✅ |
| **TC-M2-023/2** | `nullDateOfBirth_throwsValidationError` | `dateOfBirth = null` | `BusinessException`, message "Date of birth is required", KHÔNG tạo Customer/Booking | Validation | HIGH | ✅ |
| **TC-M2-024** | `TC_M2_024_noAvailableRoom_throwsMOD2UC14004` | `roomId=9999` không tồn tại trong DB | `BusinessException [MOD2-UC14-004]`, message "No available rooms", KHÔNG tạo Booking/Customer | BR-UC14-02 | HIGH | ✅ |
| **TC-M2-025** | `TC_M2_025_runtimeException_throwsMOD2UC14005_eventNotPublished` | `accountRepository.save()` throw RuntimeException giữa chừng (phòng R305) | `BusinessException [MOD2-UC14-005]`, message "transaction rolled back"/"failed" — ACID rollback toàn bộ | ADR-UC14-003, BR-ATOMIC-01 | CRITICAL | ✅ |
| **TC-M2-028** | `TC_M2_028_existingCustomer_reuseProfile_noDuplicate` | Khách đã có profile — CCCD "001200009999" → customer_id=99, phòng R308 | `status="Checked_In"`, `isNewCustomer=false`, `customerId=99`; `customerRepository.save()` KHÔNG được gọi; Room→OCCUPIED | BR-06 (no duplicate PII) | MEDIUM | ✅ |
| **TC-M2-029** | `TC_M2_029_accompaniedGuest_dependentRecordCreated` | Walk-in 1 primary + 1 khách đi kèm "Nguyen Thi B", phòng R309 cap=3 | `accompaniedGuestCount=1`, `dependentRepository.save()` 1 lần, `dependentName="Nguyen Thi B"`, Dependent link primary customer | BR-UC14-07 (AF-03) | MEDIUM | ✅ |
| **TC-M2-030** | `TC_M2_030_unavailableRoomStatus_throwsMOD2UC14006` *(Parameterized: DIRTY, MAINTENANCE)* | Phòng R310 DIRTY hoặc R311 MAINTENANCE | `BusinessException [MOD2-UC14-006]`, message "not available"/"selected room", KHÔNG tạo Booking | BR-02, ADR-UC14-002 | HIGH | ✅ |
| **TC-M2-031** | `TC_M2_031_newGuest_accountAutoCreated_linkedToReservation` | Khách mới CCCD "001999888777", phòng R312 | `isNewCustomer=true`; `accountRepository.save()` 1 lần; `passwordHash` not-null & not-blank; Booking CHECKED_IN được lưu link customer mới | BR-08 (auto account), BR-09 (default pw), BR-10 (link reservation) | HIGH | ✅ |
| **TC-M2-033** | `TC_M2_033_guestsExceedBaseCapacity_surchargeApplied` | 3 adults, `baseAdults=2`, `maxAdults=4`, surcharge=500k/người | `status="Checked_In"`, `RoomBookingDetail.extraSurcharge = 500.000` | Soft Capacity Rule | MEDIUM | ✅ |
| **TC-M2-035** | `TC_M2_035_guestsExceedMaxCapacity_throwsMOD2UC14009` | 5 adults, `maxAdults=4` | `BusinessException [MOD2-UC14-009]`, KHÔNG tạo Booking | Hard Capacity Rule | MEDIUM | ✅ |
| **TC-M2-036** | `TC_M2_036_allocatedCreditLimitExceedsMaster_throwsMOD2UC14016` | `allocatedCreditLimit=6.000.000` > master `5.000.000` (Regular tier) | `BusinessException [MOD2-UC14-016]` | Credit Limit Validation | MEDIUM | ✅ |

---

## 🔵 PHẦN 5: ĐĂNG KÝ KHÁCH ĐI KÈM — Lễ tân

### UC16 — Register Accompanying Guests (`DependentServiceUC16Test.java`)

| **TC ID** | **Method Test** | **Kịch bản** | **Expected** | **Business Rule / Error Code** | **Severity** | **Status** |
|-----------|-----------------|--------------|--------------|--------------------------------|--------------|------------|
| **TC-UC16-001** | `TC_UC16_001_registerDependent_happyPath_returnsRegisteredDTO` | Booking BK-12 (Confirmed), CCCD "034095012345" hợp lệ, chưa có duplicate | `dependentId=7`, `fullName="Nguyen Van B"`, `status="REGISTERED"`; `save()` 1 lần; `encrypt()` 1 lần | BR-FO-07 | CRITICAL | ✅ |
| **TC-UC16-002** | `TC_UC16_002_registerDependent_cancelledBooking_throwsMOD2015` | Booking BK-99 trạng thái `Cancelled` | `BusinessException [MOD2-015]`, message "not active"/"reservation", KHÔNG gọi `save()` | MOD2-015 (Reservation not active) | HIGH | ✅ |
| **TC-UC16-002b** | `TC_UC16_002b_registerDependent_checkedOutBooking_throwsMOD2015` | Booking trạng thái `Checked_Out` | `BusinessException [MOD2-015]`, KHÔNG gọi `save()` | MOD2-015 | HIGH | ✅ |
| **TC-UC16-003** | `TC_UC16_003_registerDependent_duplicateCccd_throwsMOD2016` | CCCD "034095012345" đã có trong BK-12 (`countDuplicate=1`) | `BusinessException [MOD2-016]`, message "already registered"/"duplicate", KHÔNG gọi `save()` | MOD2-016 (Duplicate guest), ADR-002 | HIGH | ✅ |
| **TC-UC16-004** | `TC_UC16_004_registerDependent_invalidCccdFormat_throwsMOD2017` | CCCD = "123" (chỉ 3 ký tự — < 9 ký tự VN) | `BusinessException [MOD2-017]`, message "invalid identification"/"cccd", KHÔNG gọi `save()` | MOD2-017 (Invalid ID doc) | MEDIUM | ✅ |
| **TC-UC16-004b** | `TC_UC16_004b_registerDependent_nullCccd_throwsMOD2017` | CCCD = `null` | `BusinessException [MOD2-017]`, KHÔNG gọi `save()` | MOD2-017 | MEDIUM | ✅ |
| **TC-UC16-005** | `TC_UC16_005_registerDependent_checkedInBooking_success` | Booking BK-55 trạng thái `Checked_In` (khách đang ở) | `status="REGISTERED"` thành công — Checked_In được phép thêm dependent | SRS AF-02 | HIGH | ✅ |
| **TC-UC16-009** | `TC_UC16_009_registerDependent_cccdMustBeEncrypted_notPlaintext` | Đăng ký thành công — kiểm tra CCCD trong DB | `dependent.cccdPassportEncrypted = "ENCRYPTED_AES256_VALUE"`, KHÔNG phải plaintext "034095012345"; `encrypt()` 1 lần | BR-SYS-01 (AES-256, Nghị định 13/2023) | CRITICAL | ✅ |
| **TC-UC16-010** | `TC_UC16_010_registerDependent_bookingNotFound_throwsMOD2003` | `bookingId=9999` không tồn tại trong DB | `BusinessException [MOD2-003]`, message "not found"/"booking", KHÔNG gọi `save()` | MOD2-003 (Booking not found) | HIGH | ✅ |

---

## 🔵 PHẦN 6: HOUSEKEEPING & BẢO TRÌ BUỒNG PHÒNG

### UC13 — Housekeeping & Maintenance (`HousekeepingServiceUC13Test.java`)

| **TC ID** | **Method Test** | **Kịch bản** | **Expected** | **Business Rule** | **Severity** | **Status** |
|-----------|-----------------|--------------|--------------|-------------------|--------------|------------|
| **TC-M2-HK-016** | `autoCreateHousekeepingTask_AfterCheckout_ShouldCreateTask` | Check-out → auto-create housekeeping task (roomId=1, staffId=10) | `operationalType="CHECKOUT_CLEAN"`, `priority="High"`, `status="Pending"`, task gắn đúng room; `save()` được gọi | BR-FO-04 | HIGH | ✅ |
| **TC-M2-HK-017** | `updateRoomToClean_DirtyRoom_ShouldMakeVacantClean` | Nhân viên hoàn thành dọn phòng (taskId=100) | `room.status="Vacant_Clean"`, `task.status="Completed"`; `roomRepo.save()` & `taskRepo.save()` được gọi | BR-FO-04 | HIGH | ✅ |
| **TC-M2-HK-018** | `getPendingOperations_ShouldReturnPendingTasks` | Lễ tân xem danh sách task đang Pending | Trả về 2 tasks, tất cả `status="Pending"`, không null | Functional | MEDIUM | ✅ |
| **TC-M2-HK-019** | `createMaintenanceRequest_ShouldChangeRoomToMaintenance` | Báo hỏng tivi phòng R101 — ghi chú "Dieu hoa phong R101 khong lanh" | `operationalType="MAINTENANCE"`, `status="Pending"`, `notes` khớp, `room.status="Maintenance"`; cả 2 `save()` được gọi | BR-HK-02 (BR-HK-03) | MEDIUM | ✅ |
| **TC-M2-HK-020** | `completeMaintenance_ShouldMakeRoomAvailable` | Bảo trì xong (taskId=200) | `room.status="Vacant_Clean"`, `task.status="Completed"`; `roomRepo.save()` & `maintenanceRepo.save()` được gọi | BR-FO-04 | MEDIUM | ✅ |

---

## 🔵 PHẦN 7: ĐỔI HẠNG PHÒNG — Lễ tân (UC44)

### UC44 — Change Room Category (`ChangeRoomCategoryServiceUC44Test.java`)

#### 🔹 TC-UC44-001 — Upgrade có phụ phí (CRITICAL)

| **Sub-TC** | **Method Test** | **Kịch bản** | **Expected** | **Severity** | **Status** |
|------------|-----------------|--------------|--------------|--------------|------------|
| **001-A** | `upgrade_shouldCreateFolioItemWithCorrectAmount` | Upgrade Deluxe→Suite, 3 đêm còn lại (3.5tr - 2tr) × 3 | `FolioItem.amount = 4.500.000` | CRITICAL | ✅ |
| **001-B** | `upgrade_oldRoomShouldBeVacantDirty` | Phòng cũ R201 sau khi đổi | `room[R201].status = "Vacant_Dirty"` | CRITICAL | ✅ |
| **001-C** | `upgrade_newRoomShouldBeOccupied` | Phòng mới R301 sau khi đổi | `room[R301].status = "Occupied"` | CRITICAL | ✅ |
| **001-D** | `upgrade_roomBookingDetailShouldReferenceNewRoom` | RoomBookingDetail phải trỏ về phòng mới | `detail.room.id = 301` | HIGH | ✅ |
| **001-E** | `upgrade_shouldCreateAuditLog` | AuditLog sau mỗi ca đổi phòng | `auditLog.action = "ROOM_CATEGORY_CHANGED"`, `timestamp` not-null | HIGH | ✅ |

#### 🔹 TC-UC44-002 — Downgrade không hoàn tiền (HIGH, BR-FIN-05)

| **Sub-TC** | **Method Test** | **Kịch bản** | **Expected** | **Severity** | **Status** |
|------------|-----------------|--------------|--------------|--------------|------------|
| **002-A** | `downgrade_shouldNOT_createFolioItem` | Downgrade Suite→Deluxe — BR-FIN-05 | `folioItemRepository.save()` KHÔNG được gọi, `response.folioItemId = null` | HIGH | ✅ |
| **002-B** | `downgrade_shouldUpdateRoomStatuses` | Room state machine khi downgrade | R401→`Vacant_Dirty`, R202→`Occupied` | HIGH | ✅ |

#### 🔹 TC-UC44-003 — Không có phòng trống (HIGH, AF1)

| **Sub-TC** | **Method Test** | **Kịch bản** | **Expected** | **Severity** | **Status** |
|------------|-----------------|--------------|--------------|--------------|------------|
| **003-A** | `noAvailableRoom_shouldThrowBusinessException` | Phòng yêu cầu không tồn tại (`Optional.empty()`) | `BusinessException`, message "No available rooms in the selected category." | HIGH | ✅ |
| **003-B** | `noAvailableRoom_shouldNOT_modifyBookingOrFolio` | DB không bị ảnh hưởng khi không có phòng | `roomBookingDetailRepository.save()` KHÔNG gọi, `folioItemRepository.save()` KHÔNG gọi | HIGH | ✅ |

#### 🔹 TC-UC44-004 — Khách từ chối / Hủy yêu cầu (MEDIUM, AF2)

| **Sub-TC** | **Method Test** | **Kịch bản** | **Expected** | **Severity** | **Status** |
|------------|-----------------|--------------|--------------|--------------|------------|
| **004-A** | `cancelPendingChange_shouldKeepOriginalRoom` | `cancelPendingChange()` — giữ nguyên phòng cũ | `folioItemRepository.save()` KHÔNG gọi, detail KHÔNG đổi room | MEDIUM | ✅ |
| **004-B** | `cancelPendingChange_bookingStatusRemainsInHouse` | Booking status sau khi hủy | `roomBooking.status = "Checked_In"` (không thay đổi) | MEDIUM | ✅ |

#### 🔹 TC-UC44-005 — Same-rate category change (MEDIUM, AF3)

| **Sub-TC** | **Method Test** | **Kịch bản** | **Expected** | **Severity** | **Status** |
|------------|-----------------|--------------|--------------|--------------|------------|
| **005-A** | `sameRate_shouldNOT_createFolioItem` | Deluxe→Deluxe Garden View (cùng giá 2tr) | `folioItemRepository.save()` KHÔNG gọi, `folioItemId = null` | MEDIUM | ✅ |
| **005-B** | `sameRate_shouldUpdateRoomStatuses` | Room state machine khi same-rate | R201→`Vacant_Dirty`, R203→`Occupied` | MEDIUM | ✅ |

#### 🔹 TC-UC44-006 — Race Condition (CRITICAL, EX1, CWE-362)

| **Sub-TC** | **Method Test** | **Kịch bản** | **Expected** | **Severity** | **Status** |
|------------|-----------------|--------------|--------------|--------------|------------|
| **006** | `raceCondition_onlyOneThreadSucceeds` | 2 lễ tân đồng thời đổi sang cùng 1 phòng R302 Suite — Pessimistic Lock | `successCount=1`, `failCount=1` — không double-assignment | CRITICAL | ✅ |

#### 🔹 TC-UC44-007 — Booking không IN-HOUSE (HIGH, EX3, BR-FO-05)

| **Sub-TC** | **Method Test** | **Kịch bản** | **Expected** | **Severity** | **Status** |
|------------|-----------------|--------------|--------------|--------------|------------|
| **007-A** | `notInHouse_CONFIRMED_shouldReject` | Booking `status="CONFIRMED"` (chưa check-in) | `BusinessException`, message "Chỉ khách đang lưu trú mới được phép thay đổi hạng phòng." | HIGH | ✅ |
| **007-B** | `notInHouse_CHECKED_OUT_shouldReject` | Booking `status="CHECKED_OUT"` (đã trả phòng) | `BusinessException` | HIGH | ✅ |
| **007-C** | `notInHouse_shouldNOT_modifyDatabase` | Booking `Pending` bị từ chối | `roomBookingDetailRepository.save()`, `roomRepository.save()`, `folioItemRepository.save()`, `auditLogRepository.save()` KHÔNG được gọi | HIGH | ✅ |

#### 🔹 TC-UC44-008 — Lỗi DB → Transaction Rollback (HIGH, EX2)

| **Sub-TC** | **Method Test** | **Kịch bản** | **Expected** | **Severity** | **Status** |
|------------|-----------------|--------------|--------------|--------------|------------|
| **008-A** | `dbError_shouldRollbackAndThrow` | `roomBookingDetailRepository.save()` ném `DataAccessResourceFailureException` | `RuntimeException` được ném (trigger `@Transactional` rollback) | HIGH | ✅ |
| **008-B** | `dbError_shouldNOT_createFolioItem` | Sau DB lỗi | `folioItemRepository.save()` KHÔNG được gọi (rollback toàn bộ) | HIGH | ✅ |

#### 🔹 TC-UC44-009 — Pricing config lỗi (MEDIUM, EX4)

| **Sub-TC** | **Method Test** | **Kịch bản** | **Expected** | **Severity** | **Status** |
|------------|-----------------|--------------|--------------|--------------|------------|
| **009** | `pricingError_shouldThrowBusinessException` | Phòng `basePrice = null` (chưa cấu hình giá) | `BusinessException`, message "Không thể tính chênh lệch giá phòng" | MEDIUM | ✅ |

#### 🔹 TC-UC44-010 — Audit Log đầy đủ (HIGH, BR-SYS-04)

| **Sub-TC** | **Method Test** | **Kịch bản** | **Expected** | **Severity** | **Status** |
|------------|-----------------|--------------|--------------|--------------|------------|
| **010-A** | `auditLog_shouldContainAllRequiredFields` | Upgrade thành công — kiểm tra AuditLog | `action="ROOM_CATEGORY_CHANGED"`, `recordId` not-null, `oldValue` not-null, `newValue` not-null, `timestamp` not-null, `ipAddress` not-null | HIGH | ✅ |
| **010-B** | `auditLog_savedExactlyOncePerSuccessfulChange` | Đổi phòng thành công — audit count | `auditLogRepository.save()` được gọi đúng 1 lần | HIGH | ✅ |

---

## 📊 BẢNG TỔNG KẾT MOD2

| **UC / Nhóm** | **File Test** | **Tổng TC** | **CRITICAL** | **HIGH** | **MEDIUM** | **LOW** | **Pass** | **🔴 RED** |
|---------------|--------------|-------------|--------------|----------|------------|---------|----------|-----------|
| UC10 — Đặt phòng Online | `BookingServiceUC10Test` | 11 | 3 | 2 | 6 | 0 | 11 | 0 |
| UC11 — Dashboard phòng | `RoomServiceUC11Test` | 2 | 0 | 1 | 1 | 0 | 2 | 0 |
| UC-PAY — VNPay Payment | `VnPayPaymentUCTest` | 6 | 3 | 2 | 1 | 0 | 6 | 0 |
| UC12 — Check-in Pre-book | `CheckinServiceUC12Test` | 13 | 1 | 7 | 4 | 1 | 12 | 1 (TC-M2-012a) |
| UC14 — Walk-in Check-in | `WalkInCheckInServiceUC14Test` | 12 | 2 | 4 | 6 | 0 | 12 | 0 |
| UC16 — Đăng ký Dependent | `DependentServiceUC16Test` | 9 | 2 | 5 | 2 | 0 | 9 | 0 |
| UC13 — Housekeeping | `HousekeepingServiceUC13Test` | 5 | 0 | 2 | 3 | 0 | 5 | 0 |
| UC44 — Đổi hạng phòng | `ChangeRoomCategoryServiceUC44Test` | 20 | 2 | 13 | 5 | 0 | 20 | 0 |
| **TỔNG MOD2** | | **78** | **13** | **36** | **28** | **1** | **77** | **1** |

> [!NOTE]
> **TC-M2-012a** (phòng DIRTY → exception) đang ở trạng thái 🔴 RED — `CheckinServiceImpl` chưa implement guard check room status. Đây là TDD GREEN task cần hoàn thiện.

---

## 🔑 BUSINESS RULES THAM CHIẾU

> **Ghi chú mapping**: Bảng dưới dùng tên BR chính thức (v2). Các tên cũ (BR-DATE-01, BR-BOOK-01, v.v.) là alias nội bộ TDD và đã được thay thế.

### 📌 Booking Status State Machine (BR-RSV-04, BR-RSV-07)

| **Status** | **Giai đoạn** | **Trigger** | **Thời hạn** |
|------------|---------------|-------------|---------------|
| `Pending` | Vừa tạo booking, chưa thanh toán | `createBooking()` | Không giới hạn |
| `Pending_Payment` | User đang thanh toán qua VNPay, phòng bị soft-lock | `confirmBooking(VNPAY)` | **2 phút** (BR-RSV-04) — hết hạn → `Cancelled_Payment` |
| `Confirmed` | VNPay IPN callback thành công | `verifyIpn(ResponseCode=00)` | — |
| `Cancelled_Payment` | Hết timeout 2 phút chưa thanh toán | `cleanupStaleHolds()` Cron | — |
| `Cancelled_Refunded` | Hủy trước 48h → hoàn 100% cọc | `cancelBooking()` trước deadline | — |
| `Cancelled_Forfeited` | Hủy trong 48h → tịch thu cọc | `cancelBooking()` sau deadline | — |
| `Checked_In` | Check-in thành công | `checkIn()` / walk-in | — |
| `Checked_Out` | Trả phòng | `checkOut()` | — |

---

### 📋 Bảng BR Chính Thức (v2)

| **Rule** | **Mô tả** |
|----------|-----------|
| **BR-RSV-01** | checkOutDate phải SAU checkInDate, không đặt quá khứ (alias cũ: BR-DATE-01) |
| **BR-RSV-02** | Chỉ hiển thị phòng Vacant, loại trừ phòng đang soft-locked |
| **BR-RSV-03** | Phải đăng nhập mới được đặt phòng |
| **BR-RSV-04** | Booking `Pending_Payment` timeout **2 phút** — hết hạn tự huỷ & giải phóng phòng |
| **BR-RSV-05** | Chống overbooking — Pessimistic Lock trên RoomCategory (alias cũ: BR-BOOK-01) |
| **BR-RSV-06** | Giá áp dụng tại thời điểm tìm kiếm (gồm promo, holiday, weekend) (alias cũ: BR-FIN-01 promo) |
| **BR-RSV-07** | Booking chuyển `Pending` → `Confirmed` CHỈ khi VNPay IPN xác nhận thành công |
| **BR-RSV-08** | Chỉ customer sở hữu booking mới được hủy |
| **BR-RSV-09** | Booking `Checked_In` hoặc `Cancelled` không thể hủy |
| **BR-RSV-10** | Hủy booking → giải phóng tất cả phòng ngay lập tức |
| **BR-RSV-11** | Customer add/remove/assign dependent; dependent đăng ký trước khi assign phòng |
| **BR-FIN-01** | Hủy ≥48h trước check-in → hoàn 100% cọc; < 48h / No-Show / đã Checked-In → không hoàn (alias cũ: BR-FIN-02) |
| **BR-FIN-02** | Thanh toán online xác nhận qua payment gateway (VNPay IPN) |
| **BR-FIN-03** | Check-in phải có deposit đã xác minh |
| **BR-FIN-04** | Upgrade: chênh lệch giá × remaining nights |
| **BR-FIN-05** | Downgrade KHÔNG hoàn tiền / giảm phí (alias cũ: BR-FIN-09) |
| **BR-FIN-06** | Upgrade/Downgrade xác định bằng so sánh room rate |
| **BR-FO-01** | Khách đại diện phải ≥ 18 tuổi, có giấy tờ hợp lệ |
| **BR-FO-02** | Rush Room (Vacant_Dirty) ưu tiên lên đầu hàng đợi Housekeeping (alias cũ: BR-HK-02) |
| **BR-FO-03** | Chỉ Primary Guest được yêu cầu nâng cấp / thay đổi nghiệp vụ |
| **BR-FO-04** | No-Show sau 00:00 hôm sau → retention 100% cọc |
| **BR-FO-05** | Chỉ khách đang IN-HOUSE được đổi hạng; phòng mới phải Vacant_Clean (alias cũ: BR-FO-10) |
| **BR-FO-06** | Walk-in phải xuất trình giấy tờ hợp lệ |
| **BR-FO-07** | Mỗi booking phải gán phòng vật lý cụ thể tại check-in (alias cũ: BR-FO-04 state machine) |
| **BR-FO-08** | Walk-in không có account → tự động tạo account + mật khẩu tạm (alias cũ: BR-UC14-08/09) |
| **BR-FO-09** | Khai báo tạm trú theo quy định (alias cũ: BR-FO-08) |
| **BR-FO-10** | Booking phải được link với customer account |
| **BR-FO-11** | Dependent không được đăng ký trùng CCCD/Passport trong cùng booking |
| **BR-FO-12** | Đổi hạng phòng chỉ finalize sau khi assign phòng cụ thể |
| **BR-ERR-01** | Exception message phải chứa error code dạng [ERR_PROMO_XXX] (nội bộ TDD) |
| **BR-SYS-01** | CCCD/Hộ chiếu mã hoá AES-256 trước khi lưu DB (Nghị định 13/2023) |
| **BR-SYS-04** | Audit Log ghi đầy đủ: action, recordId, oldValue, newValue, timestamp, ipAddress |
| **BR-ATOMIC-01** | Toàn bộ walk-in là 1 @Transactional — ADR-UC14-003 |

---

## ⚠️ TEST CASE CÒN THIẾU / CẦN BỔ SUNG

| **TC ID** | **Mô tả** | **Lý do thiếu** | **Ưu tiên** |
|-----------|-----------|-----------------|-------------|
| TC-M2-012a (GREEN) | Implement guard phòng DIRTY trong `CheckinServiceImpl.checkIn()` | Code chưa implement room status validation | 🔴 URGENT |
| TC-M2-004 (Cron) | `cleanupStaleHolds()` tự giải phóng phòng sau **2 phút** timeout — BR-RSV-04 | Cần test `@Scheduled` / Spring Integration Test | HIGH |
| TC-M2-034 | ResidenceReporting fail → check-in vẫn OK (best-effort ADR-UC14-004) | Chưa có test fault tolerance | MEDIUM |
