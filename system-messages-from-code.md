# 5.2 System Messages
**Kawai Retreat Resort & Hub** | SWP391 — Group 2

> **Actor Prefix:**
> `CUS` = Customer/Guest | `RC` = Receptionist | `FNB` = FnB Staff
> `ADM` = Admin | `MGR` = Manager | `TG` = Tour Guide
> `HK` = Housekeeping | `MT` = Maintenance | `SYS` = System


---

## CUS — Customer / Guest (Khách hàng)

### CUS-AUTH — Xác thực & Tài khoản

| # | Message Code | Message Type | Context | Content (VI) | Content (EN) |
|---|---|---|---|---|---|
| 1 | CUS-MSG01 | Exception (inline) | Register — mật khẩu quá ngắn | Mật khẩu phải có ít nhất 6 ký tự. | Password must be at least 6 characters. |
| 2 | CUS-MSG02 | Exception (inline) | Register — tên đăng nhập đã tồn tại | Tên đăng nhập đã tồn tại! | Username already exists! |
| 3 | CUS-MSG03 | Exception (inline) | Register — sai định dạng số điện thoại | Số điện thoại không đúng định dạng (phải là 10 chữ số bắt đầu bằng 0). | Invalid phone number format (must be 10 digits starting with 0). |
| 4 | CUS-MSG04 | Exception (inline) | Register — email đã được sử dụng | Email này đã được sử dụng! | This email is already in use! |
| 5 | CUS-MSG05 | Exception (inline) | Register — email đang chờ xác thực tài khoản khác | Email này đang chờ xác thực cho một tên đăng nhập khác! | This email is pending verification for another username! |
| 6 | CUS-MSG06 | Exception (toast error) | Login — tài khoản bị khóa do nhập sai nhiều lần | Tài khoản bị khóa do nhập sai quá {n} lần. | Account locked due to too many failed login attempts ({n} times). |
| 7 | CUS-MSG07 | Exception (inline) | Xác minh OTP — OTP hết hạn | OTP hết hạn. | OTP has expired. |
| 8 | CUS-MSG08 | Exception (inline) | Quên mật khẩu — email không tồn tại | Email không tồn tại. | Email does not exist in the system. |
| 9 | CUS-MSG09 | Exception (inline) | Đặt lại mật khẩu — token hết hạn hoặc không hợp lệ | Token reset hết hạn hoặc không hợp lệ. | Reset token has expired or is invalid. |
| 10 | CUS-MSG10 | Exception (inline) | Đặt lại mật khẩu — mật khẩu không đủ mạnh | Mật khẩu không đủ mạnh. | Password is not strong enough. |
| 11 | CUS-MSG11 | Exception (inline) | Đặt lại mật khẩu — trùng mật khẩu cũ | Mật khẩu mới không được trùng mật khẩu cũ. | New password must not be the same as the current password. |

### CUS-PROFILE — Hồ sơ cá nhân

| # | Message Code | Message Type | Context | Content (VI) | Content (EN) |
|---|---|---|---|---|---|
| 12 | CUS-MSG12 | Toast success | Cập nhật thông tin hồ sơ thành công | Cập nhật thông tin thành công! | Profile updated successfully! |
| 13 | CUS-MSG13 | Toast success | Đổi mật khẩu thành công | Đổi mật khẩu thành công! | Password changed successfully! |
| 14 | CUS-MSG14 | Toast error | Mật khẩu hiện tại không đúng | Mật khẩu cũ không chính xác! | Current password is incorrect! |
| 15 | CUS-MSG15 | Toast error | Ngày sinh sai định dạng | Ngày sinh không đúng định dạng! | Invalid date of birth format! |
| 16 | CUS-MSG16 | Toast success | Thêm người đi cùng thành công | Thêm người đi cùng thành công! | Companion added successfully! |
| 17 | CUS-MSG17 | Toast success | Xóa người đi cùng thành công | Xóa người đi cùng thành công! | Companion removed successfully! |
| 18 | CUS-MSG18 | Toast error | Không tìm thấy người đi cùng | Không tìm thấy người đi cùng! | Companion not found! |
| 19 | CUS-MSG19 | Toast success | Cập nhật ảnh đại diện thành công | Cập nhật ảnh đại diện thành công! | Avatar updated successfully! |
| 20 | CUS-MSG20 | Toast error | Tải ảnh đại diện thất bại | Lỗi tải lên ảnh đại diện: {message}. | Avatar upload failed: {message}. |

### CUS-BOOK — Đặt phòng

| # | Message Code | Message Type | Context | Content (VI) | Content (EN) |
|---|---|---|---|---|---|
| 21 | CUS-MSG21 | Toast error | Ngày check-out trước ngày check-in | Ngày check-out phải sau ngày check-in! | Check-out date must be after check-in date! |
| 22 | CUS-MSG22 | Toast error | Chưa chọn phòng | Vui lòng chọn ít nhất 1 phòng. | Please select at least 1 room. |
| 23 | CUS-MSG23 | Toast error | Ngày check-in đã qua | Ngày check-in không được ở quá khứ. | Check-in date cannot be in the past. |
| 24 | CUS-MSG24 | Exception (inline) | Mã giảm giá đã được dùng quá số lần cho phép | Khách hàng đã vượt quá số lần sử dụng mã giảm giá này (1 lần). | Customer has exceeded the usage limit for this promo code (1 use per customer). |

### CUS-CHECKIN — Tự check-in (online)

| # | Message Code | Message Type | Context | Content (VI) | Content (EN) |
|---|---|---|---|---|---|
| 25 | CUS-MSG25 | Exception (toast error) | Không tìm thấy thông tin phòng trong đơn | Không tìm thấy thông tin phòng cho đơn này! | No room information found for this booking! |
| 26 | CUS-MSG26 | Exception (toast error) | Chưa chọn phòng vật lý để giao cho khách | Bạn chưa chọn phòng vật lý nào để giao cho khách! | No physical room has been selected to assign to the guest! |
| 27 | CUS-MSG27 | Exception (toast error) | Không tìm thấy phòng theo số phòng nhập | Không tìm thấy phòng số {roomNumber}. | Room number {roomNumber} not found in the system. |
| 28 | CUS-MSG28 | Exception (toast error) | Hạng phòng không khớp với đơn đặt | Phòng {number} thuộc hạng {cat} không khớp với bất kỳ hạng phòng nào đang chờ check-in của đơn này! | Room {number} (category: {cat}) does not match any pending check-in category in this booking! |
| 29 | CUS-MSG29 | Exception (toast error) | Số lượng phòng phân bổ chưa đủ | Bạn phải phân đủ {n} phòng trước khi hoàn tất check-in! | You must assign all {n} rooms before completing check-in! |
| 30 | CUS-MSG30 | Exception (toast error) | Phòng phải có đúng 1 người đứng đầu | Phòng {number} phải có đúng 1 người đứng đầu! | Room {number} must have exactly 1 primary contact! |
| 31 | CUS-MSG31 | Exception (toast error) | Tổng hạn mức vượt quá hạn mức tài khoản | Tổng hạn mức cấp cho các phòng vượt quá hạn mức tài khoản (Master: {limit}). | Total allocated credit limit exceeds the master credit limit (Master: {limit}). |
| 32 | CUS-MSG32 | Exception (inline) | Hạn mức không được là số âm | Hạn mức không được là số âm! | Credit limit cannot be a negative number! |

### CUS-TABLE — Đặt bàn nhà hàng

| # | Message Code | Message Type | Context | Content (VI) | Content (EN) |
|---|---|---|---|---|---|
| 33 | CUS-MSG33 | Exception (toast error) | Số khách vượt sức chứa của bàn | Số lượng khách vượt quá sức chứa của bàn. | Party size exceeds the table's maximum capacity. |
| 34 | CUS-MSG34 | Exception (toast error) | Đặt bàn trong khung giờ nhà hàng đóng cửa | Nhà hàng không nhận đặt bàn từ 23:00 đến 08:00. Vui lòng dùng dịch vụ gọi món lên phòng. | The restaurant does not accept reservations between 23:00 and 08:00. Please use room service instead. |
| 35 | CUS-MSG35 | Exception (toast error) | Thời gian đặt bàn quá sát giờ (dưới 30 phút) | Thời gian đặt bàn tối thiểu là 30 phút. | Reservations must be made at least 30 minutes in advance. |
| 36 | CUS-MSG36 | Exception (toast error) | Bàn đang bận (dọn dẹp hoặc có khách) trong 2 giờ tới | Bàn hiện đang {status}. Không thể đặt trong vòng 2 giờ tới. | The table is currently {status}. It cannot be reserved within the next 2 hours. |
| 37 | CUS-MSG37 | Exception (toast error) | Khung giờ yêu cầu đã có đặt bàn khác | Bàn đã có đặt chỗ cho khung giờ này. | This table is already reserved for the requested time slot. |
| 38 | CUS-MSG38 | Exception (inline) | Chưa nhập số phòng (chỉ áp dụng cho khách lưu trú) | Vui lòng nhập số phòng. Chỉ áp dụng cho khách đang lưu trú. | Please enter your room number. Table reservations are available for in-house guests only. |
| 39 | CUS-MSG39 | Exception (toast error) | Không có lịch lưu trú hợp lệ tại thời điểm đặt bàn | Bạn cần có lịch lưu trú hợp lệ tại thời điểm đặt bàn. | You must have a valid stay at the resort at the time of the reservation. |
| 40 | CUS-MSG40 | Exception (toast error) | Khách chưa check-in không được đặt bàn | Chỉ khách đang lưu trú (đã Check-in) mới được đặt bàn. | Table reservations are only available for guests who have already checked in. |
| 41 | CUS-MSG41 | Exception (toast error) | Hủy đặt bàn ở trạng thái không hợp lệ | Chỉ có thể hủy khi trạng thái là Chờ xác nhận hoặc Chờ sử dụng. | Reservation can only be cancelled when its status is Pending Confirmation or Waiting to Use. |

### CUS-TOUR — Đặt tour

| # | Message Code | Message Type | Context | Content (VI) | Content (EN) |
|---|---|---|---|---|---|
| 42 | CUS-MSG42 | Exception (toast error) | Tour đã hết chỗ | Hết chỗ. Chỉ còn {n} chỗ trống. | This tour is fully booked. Only {n} slot(s) remaining. |
| 43 | CUS-MSG43 | Exception (toast error) | Tour bắt buộc mua bảo hiểm nhưng khách từ chối | Tour này bắt buộc mua bảo hiểm du lịch. Vui lòng đồng ý mua bảo hiểm để tiếp tục đặt chỗ. | This tour requires travel insurance. Please agree to purchase insurance to continue with your booking. |
| 44 | CUS-MSG44 | Exception (toast error) | Chọn ghi nợ vào phòng nhưng chưa chọn phòng | Vui lòng chọn phòng để ghi nợ. Phòng phải đã được check-in. | Please select a checked-in room to charge the tour cost to. |

### CUS-DEP — Người đi kèm (Dependent)

| # | Message Code | Message Type | Context | Content (VI) | Content (EN) |
|---|---|---|---|---|---|
| 45 | CUS-MSG45 | Exception (inline) | Giấy tờ tùy thân sai định dạng | Giấy tờ tùy thân không hợp lệ: phải là CCCD 12 số hoặc hộ chiếu hợp lệ. | Invalid identification document: must be a 12-digit CCCD or a valid passport. |
| 46 | CUS-MSG46 | Exception (inline) | Ngày sinh ở tương lai | Ngày sinh không được ở tương lai. | Date of birth cannot be a future date. |
| 47 | CUS-MSG47 | Exception (inline) | Không tìm thấy đơn đặt phòng | Không tìm thấy đơn đặt phòng với ID đã cho. | No booking found with the provided ID. |
| 48 | CUS-MSG48 | Exception (inline) | Đơn đặt phòng không ở trạng thái hợp lệ | Đơn đặt phòng không ở trạng thái hợp lệ (phải là Confirmed hoặc Checked_In). Trạng thái hiện tại: {status}. | Booking is not in a valid status (must be Confirmed or Checked_In). Current status: {status}. |
| 49 | CUS-MSG49 | Exception (system) | Lỗi nội bộ khi mã hóa CCCD | Lỗi nội bộ khi mã hoá CCCD. | An internal error occurred while encrypting the CCCD. |
| 50 | CUS-MSG50 | Exception (inline) | CCCD trùng với người khác trong cùng đơn | Căn cước bị trùng với người khác trong cùng một đơn đặt phòng. | This ID document conflicts with another guest in the same booking. |
| 51 | CUS-MSG51 | Exception (inline) | CCCD trùng với người đặt phòng chính | Căn cước bị trùng với người đặt phòng chính trong cùng một đơn đặt phòng. | This ID document conflicts with the primary booker of this booking. |
| 52 | CUS-MSG52 | Exception (inline) | Không tìm thấy người đi kèm để cập nhật | Không tìm thấy người đi kèm với ID đã cho. | No companion/dependent found with the provided ID. |
| 53 | CUS-MSG53 | Exception (inline) | Số người lớn vượt sức chứa tối đa phòng | Số lượng người lớn vượt quá sức chứa tối đa của phòng. Tối đa: {n} người lớn. | Number of adults exceeds the room's maximum capacity. Maximum allowed: {n} adult(s). |
| 54 | CUS-MSG54 | Exception (inline) | Số trẻ em vượt sức chứa tối đa phòng | Số lượng trẻ em vượt quá sức chứa tối đa của phòng. Tối đa: {n} trẻ em. | Number of children exceeds the room's maximum capacity. Maximum allowed: {n} child(ren). |

---

## RC — Receptionist (Lễ tân)

### RC-CHECKIN — Check-in tại quầy

| # | Message Code | Message Type | Context | Content (VI) | Content (EN) |
|---|---|---|---|---|---|
| 55 | RC-MSG01 | Exception (toast error) | Booking detail đã được check-in rồi | BookingDetail đã CHECKED_IN không được check-in lại. | This booking detail has already been checked in and cannot be checked in again. |
| 56 | RC-MSG02 | Exception (toast error) | Booking chưa được xác nhận | Booking chưa CONFIRMED không được phép check-in. | Booking must be in CONFIRMED status before check-in is allowed. |
| 57 | RC-MSG03 | Exception (toast error) | Phòng đang bảo trì | Phòng đang MAINTENANCE, đang bảo trì. Không thể check-in. | Room is currently under MAINTENANCE and cannot be checked in. |
| 58 | RC-MSG04 | Exception (toast error) | Phòng đang có khách | Phòng đang Occupied, không thể check-in. | Room is currently Occupied and cannot be checked in. |
| 59 | RC-MSG05 | Exception (toast error) | Phòng đang bẩn, chưa dọn | Phòng đang Dirty, không thể check-in. | Room is currently Dirty and cannot be checked in. |
| 60 | RC-MSG06 | Exception (toast error) | Phòng có task bảo trì đang chờ xử lý | Phòng đang có task bảo trì/kiểm tra chờ xử lý, không thể check-in. | Room has a pending maintenance or inspection task and cannot be checked in. |
| 61 | RC-MSG07 | Flash success | Check-in hoàn tất | Check-in thành công! | Check-in completed successfully! |
| 62 | RC-MSG08 | Flash error | Dữ liệu nhập không hợp lệ (đặc biệt là ngày) | Dữ liệu nhập vào không hợp lệ. Vui lòng kiểm tra lại (đặc biệt là ngày tháng). | Invalid input data. Please review your entries (especially dates). |
| 63 | RC-MSG38 | Flash error | Không tìm thấy booking theo ID khi submit check-in | Không tìm thấy Đơn hàng với ID đã cho. | Booking not found for the provided ID. |
| 64 | RC-MSG39 | Flash error | Số điện thoại khách chính không hợp lệ (CHECKIN-VAL-01) | Số điện thoại không hợp lệ — phải bắt đầu bằng 0 và có đúng 10 số! | Invalid phone number — must start with 0 and be exactly 10 digits! |
| 65 | RC-MSG40 | Flash error | CCCD/Hộ chiếu khách chính không hợp lệ (CHECKIN-VAL-02) | CCCD/Hộ chiếu không hợp lệ — CCCD phải gồm 12 số, hộ chiếu 6–15 ký tự chữ/số. | Invalid ID document — CCCD must be 12 digits; passport must be 6–15 alphanumeric characters. |
| 66 | RC-MSG41 | Flash error | Controller catch generic Exception — lỗi hệ thống không xác định trong check-in | Lỗi hệ thống! Vui lòng thử lại. | System error! Please try again. |
| 67 | RC-MSG42 | System log (ERROR) | Upload ảnh FaceID lên Cloudinary thất bại khi check-in | Lỗi lưu ảnh FaceID lên Cloudinary cho khách ID={id}. Check-in vẫn tiếp tục. | Failed to save FaceID image to Cloudinary for customer ID={id}. Check-in continues. |
| 68 | RC-MSG43 | JSON success | upgradeDependentToCustomer() — Nâng cấp Dependent thành tài khoản Customer mới | Nâng cấp thành công: {name} ({roomInfo}). Tài khoản: {username} — Mật khẩu tạm: {password} | Successfully upgraded: {name} ({roomInfo}). Account: {username} — Temporary password: {password} |
| 69 | RC-MSG44 | JSON success | upgradeDependentToCustomer() — Liên kết Dependent với Customer đã có tài khoản | Liên kết thành công! {name} đã được gán làm người đại diện chính. (Tài khoản hiện có: {username}) | Successfully linked! {name} has been set as the primary contact. (Existing account: {username}) |
| 70 | RC-MSG45 | JSON error | upgradeDependentToCustomer() — Nâng cấp Dependent thất bại | Không thể nâng cấp người đi kèm: {error} | Failed to upgrade accompanying guest: {error} |
| 71 | RC-MSG46 | System log (WARN) | processBulkCheckin() — kích hoạt Workflow ROOM_CHECKIN thất bại; check-in vẫn hoàn tất | Cảnh báo: Không thể kích hoạt Workflow ROOM_CHECKIN cho phòng {roomNumber}. Check-in đã hoàn tất. | Warning: Failed to trigger ROOM_CHECKIN workflow for room {roomNumber}. Check-in completed. |

### RC-WALKIN — Walk-in Check-in

| # | Message Code | Message Type | Context | Content (VI) | Content (EN) |
|---|---|---|---|---|---|
| 63 | RC-MSG09 | Exception (inline) | Ngày sinh bắt buộc nhập | Ngày sinh là bắt buộc. | Date of birth is required. |
| 64 | RC-MSG10 | Exception (inline) | CCCD phải đúng 12 chữ số | Giấy tờ tùy thân không hợp lệ: CCCD phải là 12 chữ số. | Invalid identification document: CCCD must be exactly 12 digits. |
| 65 | RC-MSG11 | Exception (inline) | Chưa chọn phòng nào | Không có phòng nào được chọn. | No room has been selected. |
| 66 | RC-MSG12 | Exception (toast error) | Phòng được chọn không khả dụng để check-in | Phòng được chọn không khả dụng để check-in. Trạng thái hiện tại: {status}. | The selected room is not available for check-in. Current status: {status}. |
| 67 | RC-MSG13 | Exception (toast error) | Phòng được chọn có task bảo trì đang chờ | Phòng được chọn có task bảo trì/kiểm tra chưa hoàn tất. Không thể check-in. | The selected room has a pending maintenance or damage check task. Check-in is not allowed. |
| 68 | RC-MSG14 | Exception (toast error) | Không tìm thấy phòng theo ID | Không tìm thấy phòng khả dụng với ID: {id}. | No available room found for the provided room ID: {id}. |
| 69 | RC-MSG15 | Exception (toast error) | Số khách vượt sức chứa tối đa của phòng | Số lượng khách vượt quá sức chứa tối đa của phòng. Tối đa: {n} người lớn, {n} trẻ em. | Number of guests exceeds the room's maximum capacity. Maximum: {n} adult(s), {n} child(ren). |
| 70 | RC-MSG16 | Exception (toast error) | Email đã được đăng ký cho tài khoản khác | Email '{email}' đã được đăng ký cho một tài khoản khác. Vui lòng tìm kiếm (Check Existing) hoặc dùng email khác. | Email '{email}' is already registered to another account. Please use the search function (Check Existing) or provide a different email. |
| 71 | RC-MSG17 | Exception (toast error) | Số điện thoại đã được đăng ký cho tài khoản khác | Số điện thoại '{phone}' đã được đăng ký cho một tài khoản khác. Vui lòng tìm kiếm hoặc dùng số khác. | Phone number '{phone}' is already registered to another account. Please search for the existing account or provide a different number. |
| 72 | RC-MSG18 | Exception (inline) | Tổng hạn mức phòng vượt hạn mức tài khoản tổng | Tổng hạn mức các phòng ({total}) vượt quá hạn mức tài khoản tổng ({master}). | The combined credit limit for all rooms ({total}) exceeds the account's master credit limit ({master}). |
| 73 | RC-MSG19 | Exception (toast error) | Phòng phải có đúng 1 người đứng đầu | Phòng {number} phải có đúng 1 người đứng đầu! | Room {number} must have exactly 1 designated primary contact. |
| 74 | RC-MSG20 | Exception (system) | Walk-in thất bại, transaction bị rollback | Walk-in check-in thất bại. Transaction đã bị rollback: {error}. | Walk-in check-in failed. The transaction has been rolled back: {error}. |
| 75 | RC-MSG21 | Exception (toast error) | Hủy walk-in — chỉ áp dụng cho Pending_Payment | Chỉ có thể hủy đơn Walk-in đang ở trạng thái Pending_Payment qua API này. | Only Walk-in bookings in Pending_Payment status can be cancelled through this action. |
| 76 | RC-MSG22 | Exception (toast error) | Hủy walk-in — không tìm thấy đơn | Không tìm thấy đơn đặt phòng. | Booking not found. |
| 77 | RC-MSG47 | JSON success | `escalateTask()` — Đã nâng ưu tiên task don phòng hiện có lên `URGENT_CLEAN` | Đã nâng độ ưu tiên dọn khẩn cấp cho phòng {number}. | Priority escalated to urgent for room {number}. |
| 78 | RC-MSG48 | JSON success | `escalateTask()` — Không có task sẵn, tạo mới task `URGENT_CLEAN` và gửi HK | Yêu cầu dọn khẩn cấp đã gửi cho bộ phận Buồng phòng. | Urgent cleaning request has been sent to Housekeeping. |
| 79 | RC-MSG49 | JSON error (500) | `escalateTask()` — Không tìm thấy nhân viên HK trong hệ thống (lỗi cấu hình) | Lỗi cấu hình hệ thống (không có nhân viên). | System configuration error (no staff found). |

### RC-TRANSFER — Đổi phòng trong lưu trú

| # | Message Code | Message Type | Context | Content (VI) | Content (EN) |
|---|---|---|---|---|---|
| 77 | RC-MSG23 | Exception (toast error) | Booking detail chưa được gán phòng | Booking detail chưa được gán phòng, không thể đổi phòng. | This booking detail has no assigned room and cannot be transferred. |
| 78 | RC-MSG24 | Exception (toast error) | Booking detail chưa ở trạng thái CHECKED_IN | Booking detail chưa CHECKED_IN, không thể đổi phòng. | This booking detail is not in CHECKED_IN status and cannot be transferred. |
| 79 | RC-MSG25 | Exception (toast error) | Phòng mới không ở trạng thái Vacant_Clean | Phòng mới không khả dụng (trạng thái: {status}). Chỉ được đổi sang phòng Vacant_Clean. | The new room is not available (status: {status}). Room transfers are only allowed to Vacant_Clean rooms. |

### RC-CHANGECATEGORY — Đổi hạng phòng (in-house)

| # | Message Code | Message Type | Context | Content (VI) | Content (EN) |
|---|---|---|---|---|---|
| 80 | RC-MSG26 | Exception (toast error) | Khách chưa check-in, không được đổi hạng | Chỉ khách đang lưu trú mới được phép thay đổi hạng phòng. | Room category changes are only permitted for guests who are currently checked in. |
| 81 | RC-MSG27 | Exception (toast error) | Không có phòng khả dụng trong hạng được chọn | Không có phòng nào khả dụng trong hạng phòng đã chọn. | No available rooms found in the selected room category. |
| 82 | RC-MSG28 | Exception (toast error) | Phòng chưa được cấu hình giá cơ bản | Không thể tính chênh lệch giá do phòng không có giá cơ bản. | Cannot calculate the price difference because the room has no base price configured. |
| 83 | RC-MSG29 | Exception (toast error) | Không tìm thấy booking detail | Không tìm thấy thông tin chi tiết đặt phòng. | Booking detail not found. |

### RC-CREDITS — Hạn mức tín dụng

| # | Message Code | Message Type | Context | Content (VI) | Content (EN) |
|---|---|---|---|---|---|
| 84 | RC-MSG30 | Exception (inline) | Hạn mức null hoặc âm không hợp lệ | Hạn mức không được là null hoặc số âm. | Credit limit cannot be null or a negative number. |
| 85 | RC-MSG31 | Exception (toast error) | Tổng hạn mức phân bổ vượt hạn mức tổng | Tổng hạn mức phân bổ vượt quá hạn mức tổng của đơn. | The total allocated credit limit exceeds the booking's master credit limit. |

### RC-TABLE — Quản lý đặt bàn

| # | Message Code | Message Type | Context | Content (VI) | Content (EN) |
|---|---|---|---|---|---|
| 86 | RC-MSG32 | Exception (toast error) | Phòng trống, không có khách lưu trú | Phòng đang trống, không có khách lưu trú. | The room is currently vacant with no checked-in guest. |
| 87 | RC-MSG33 | Exception (toast error) | Không tìm thấy thông tin khách lưu trú | Không tìm thấy thông tin khách lưu trú. | Checked-in guest information could not be found. |
| 88 | RC-MSG34 | Exception (toast error) | Trạng thái đặt bàn không hợp lệ để check-in | Trạng thái đặt bàn không hợp lệ để thực hiện check-in bàn. | The table reservation is not in a valid status for check-in. |
| 89 | RC-MSG35 | Exception (inline) | Thời gian giữ bàn phải từ 0–30 phút | Thời gian giữ bàn phải từ 0 đến 30 phút. | Table hold duration must be between 0 and 30 minutes. |
| 90 | RC-MSG36 | Exception (toast error) | Trạng thái đặt bàn không hợp lệ để giữ bàn | Trạng thái đặt bàn không hợp lệ để thực hiện giữ bàn. | The table reservation is not in a valid status to be placed on hold. |

### RC-CHECKOUT — Check-out & Folio

| # | Message Code | Message Type | Context | Content (VI) | Content (EN) |
|---|---|---|---|---|---|
| 91 | RC-MSG37 | Exception (toast error) | Folio còn hóa đơn chưa thanh toán | Hóa đơn chưa thanh toán hết — không thể Check-out. | There are outstanding unpaid charges on the folio — check-out is not possible. |

---

## FNB — Restaurant Staff / KDS (Nhân viên FnB)

### FNB-ORDER — Tạo & quản lý đơn hàng

| # | Message Code | Message Type | Context | Content (VI) | Content (EN) |
|---|---|---|---|---|---|
| 92 | FNB-MSG01 | Exception (toast error) | Đơn hàng không có món nào | Đơn hàng trống — không có món. | Order is empty — no items have been added. |
| 93 | FNB-MSG02 | Exception (toast error) | Phòng không ở trạng thái OCCUPIED | Phòng không ở trạng thái OCCUPIED. | The room is not in OCCUPIED status. |
| 94 | FNB-MSG03 | Exception (toast error) | Món ăn đã hết, không thể order | Món đã hết — không thể order. | This item is sold out and cannot be ordered. |
| 95 | FNB-MSG04 | Exception (toast error) | Vượt hạn mức tín dụng phòng | Vượt hạn mức tín dụng phòng (Credit Limit). | The room's credit limit has been exceeded. |
| 96 | FNB-MSG05 | Exception (toast error) | Không thể hủy/sửa vì order đã vào bếp | Không thể sửa/xóa — order đã vào bếp. | This item cannot be edited or cancelled — it has already been sent to the kitchen. |
| 97 | FNB-MSG06 | Exception (toast error) | Chỉ hủy được đơn ở trạng thái Pending | Chỉ có thể hủy đơn hàng ở trạng thái Pending. | Only orders in Pending status can be cancelled. |
| 98 | FNB-MSG07 | Exception (toast error) | Không có quyền hủy đơn này | Bạn không có quyền hủy đơn hàng này. | You are not authorized to cancel this order. |
| 99 | FNB-MSG08 | Exception (toast error) | Không tìm thấy thông tin khách hàng | Không tìm thấy thông tin khách hàng. | Customer information could not be found. |

### FNB-BILL — Thanh toán & ký bill

| # | Message Code | Message Type | Context | Content (VI) | Content (EN) |
|---|---|---|---|---|---|
| 100 | FNB-MSG09 | Exception (toast error) | Bàn đang dọn hoặc bảo trì, không thể tạo bill | Bàn đang được dọn hoặc bảo trì, không thể tạo hóa đơn! | The table is currently being cleaned or under maintenance — a bill cannot be created. |
| 101 | FNB-MSG10 | Exception (toast error) | Hạn mức phòng không đủ để thanh toán | Hạn mức chi tiêu phòng không đủ để thanh toán! | The room's credit limit is insufficient to cover this bill. |
| 102 | FNB-MSG11 | Exception (toast error) | Không tìm thấy phòng để ký bill | Không tìm thấy phòng để ký bill! | No room found for bill signing. |
| 103 | FNB-MSG12 | Exception (toast error) | Phòng không ở trạng thái OCCUPIED để ghi nợ | Phòng không ở trạng thái OCCUPIED. | The room is not in OCCUPIED status and cannot be charged. |
| 104 | FNB-MSG13 | Exception (toast error) | Hạn mức phòng không đủ (vượt Credit Limit) | Hạn mức chi tiêu phòng không đủ để thanh toán (vượt Credit Limit). | The room's spending limit is insufficient — the Credit Limit has been exceeded. |

---

## ADM — Admin (Quản trị viên)

### ADM-USER — Quản lý tài khoản nhân viên

| # | Message Code | Message Type | Context | Content (VI) | Content (EN) |
|---|---|---|---|---|---|
| 105 | ADM-MSG01 | Exception (inline, red) | Tạo tài khoản — tên đăng nhập đã tồn tại | Tên đăng nhập đã tồn tại. | Username already exists in the system. |
| 106 | ADM-MSG02 | Exception (inline, red) | Tạo tài khoản — CCCD đã tồn tại | CCCD đã tồn tại trong hệ thống. | This CCCD is already registered in the system. |
| 107 | ADM-MSG03 | Exception (inline, red) | Tạo tài khoản — email đã tồn tại | Email đã tồn tại. | This email is already registered in the system. |
| 108 | ADM-MSG04 | Exception (inline, red) | Tạo/sửa tài khoản — sai định dạng số điện thoại | Số điện thoại không đúng định dạng. | Invalid phone number format. |
| 109 | ADM-MSG05 | Exception (inline, red) | Tạo/sửa tài khoản — sai định dạng CCCD | Định dạng CCCD không hợp lệ. | Invalid CCCD format. |
| 110 | ADM-MSG06 | Exception (toast error) | Sửa tài khoản — không được thay đổi role Admin | Không thể thay đổi vai trò của tài khoản Admin. | The role of an Admin account cannot be changed. |
| 111 | ADM-MSG07 | Exception (toast error) | Sửa tài khoản — không được vô hiệu hóa Admin | Không thể vô hiệu hóa tài khoản Admin. | An Admin account cannot be deactivated. |
| 112 | ADM-MSG08 | Exception (toast error) | Kiểm tra phân quyền — tài khoản không có role | Tài khoản không có vai trò được gán. | This account has no role assigned. |

---

## MGR — Manager (Quản lý)

### MGR-TOUR — Quản lý Tour

| # | Message Code | Message Type | Context | Content (VI) | Content (EN) |
|---|---|---|---|---|---|
| 113 | MGR-MSG01 | Exception (toast error) | Cập nhật giá tour — đang có lịch hoạt động | Không thể cập nhật giá tour đang có lịch trình đang mở. | Cannot update the tour price while there are active schedules. |
| 114 | MGR-MSG02 | Exception (toast error) | Xóa tour — đang có lịch hoạt động | Không thể xóa tour đang có lịch trình đang mở. | Cannot delete a tour that has active schedules. |
| 115 | MGR-MSG03 | Exception (inline) | Tìm kiếm tour — khoảng ngày không hợp lệ | Ngày bắt đầu không được sau ngày kết thúc. | The start date must not be later than the end date. |
| 116 | MGR-MSG04 | Exception (toast error) | CRUD tour — không tìm thấy tour | Không tìm thấy tour. | Tour not found. |

---

## TG — Tour Guide (Hướng dẫn viên)

### TG-ATTEND — Điểm danh

| # | Message Code | Message Type | Context | Content (VI) | Content (EN) |
|---|---|---|---|---|---|
| 117 | TG-MSG01 | Exception (toast error) | Xác minh điểm danh — không tìm thấy attendee | Không tìm thấy người tham gia tour với ID đã cho. | No tour attendee found with the provided ID. |

---

## HK — Housekeeping (Buồng phòng)

### HK-ROOM — Quản lý trạng thái phòng

| # | Message Code | Message Type | Context | Content (VI) | Content (EN) |
|---|---|---|---|---|---|
| 118 | HK-MSG01 | Flash success | Đánh dấu phòng sạch thành công | Đã đánh dấu phòng sạch thành công. | Room has been marked as clean successfully. |
| 119 | HK-MSG02 | Flash success | Bắt đầu dọn phòng thành công | Đã bắt đầu dọn phòng. | Room cleaning has been started. |
| 120 | HK-MSG03 | Flash success | Gửi yêu cầu bảo trì thành công | Đã gửi yêu cầu bảo trì. | Maintenance request has been submitted. |
| 121 | HK-MSG04 | Flash error | Thao tác housekeeping thất bại | Lỗi: {message}. | Error: {message}. |

---

## MT — Maintenance (Bảo trì)

### MT-REPAIR — Sửa chữa

| # | Message Code | Message Type | Context | Content (VI) | Content (EN) |
|---|---|---|---|---|---|
| 122 | MT-MSG01 | Flash success | Bắt đầu sửa chữa thành công | Đã bắt đầu sửa chữa. | Repair work has been started. |
| 123 | MT-MSG02 | Flash success | Hoàn tất bảo trì, phòng sẵn sàng | Hoàn tất bảo trì, phòng đã sẵn sàng. | Maintenance completed. The room is now ready. |
| 124 | MT-MSG03 | Flash success | Tạm dừng công việc thành công | Đã tạm dừng công việc. | Work has been paused. |
| 125 | MT-MSG04 | Flash success | Định giá sự cố thành công | Đã định giá sự cố thành công. | Incident has been priced successfully. |
| 126 | MT-MSG05 | Flash error | Thao tác bảo trì thất bại | Lỗi: {message}. | Error: {message}. |

---

## SYS — System / Scheduler (Hệ thống tự động)

### SYS-AUDIT — Night Audit (log nội bộ, không hiển thị UI)

| # | Message Code | Message Type | Context | Content (VI) | Content (EN) |
|---|---|---|---|---|---|
| 127 | SYS-MSG01 | System log | Night Audit — booking không Checked_In (bỏ qua) | Booking {id} bị bỏ qua: trạng thái không phải Checked_In. | Booking {id} skipped: status is not Checked_In. |
| 128 | SYS-MSG02 | System log | Night Audit — không có folio item (hủy bỏ) | Night Audit hủy: không tìm thấy folio item phòng nào. | Night Audit aborted: no room charge folio items were found. |
| 129 | SYS-MSG03 | System log | Night Audit — chưa cấu hình giá đêm | Night Audit hủy: chưa cấu hình giá phòng đêm. | Night Audit aborted: night room rate has not been configured. |
| 130 | SYS-MSG04 | System log | Night Audit — đã chạy hôm nay rồi | Night Audit đã được chạy cho ngày hôm nay. | Night Audit has already been executed for today. |

### SYS-GLOBAL — Global Exception Handler

| # | Message Code | Message Type | Context | Content (VI) | Content (EN) |
|---|---|---|---|---|---|
| 131 | SYS-MSG05 | HTTP 400 (JSON response) | Bất kỳ API — BusinessException được ném ra | {exception.message} | {exception.message} |
| 132 | SYS-MSG06 | HTTP 403 (error page) | Bất kỳ trang — người dùng không có quyền truy cập | Bạn không có quyền truy cập trang này. | You do not have permission to access this page. |
| 133 | SYS-MSG07 | HTTP 500 (error page) | Bất kỳ trang — lỗi hệ thống không được xử lý | Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau. | A system error has occurred. Please try again later. |
| 134 | SYS-MSG08 | System log (WARN) | CheckinServiceImpl — mã hóa AES-256 cho CCCD thất bại, dữ liệu được lưu dưới dạng fallback | ⚠️ Cảnh báo bảo mật: Mã hóa CCCD thất bại cho khách ID={id}. Dữ liệu được lưu dạng fallback không mã hóa. Vi phạm BR-SYS-01. | ⚠️ Security warning: CCCD encryption failed for customer ID={id}. Data stored as unencrypted fallback. Violates BR-SYS-01. |

---

## RC — Receptionist (Tiếp tân) - Bổ sung

### RC-DEPENDENT — Khách đi kèm (Trong luồng Check-in / Walk-in)

| # | Message Code | Message Type | Context | Content (VI) | Content (EN) |
|---|---|---|---|---|---|
| 135 | RC-MSG50 | Exception (inline) | `validateDateOfBirthNotFuture()` — Ngày sinh là ngày tương lai | Ngày sinh không hợp lệ: không được là ngày trong tương lai. | Invalid Date of Birth: Cannot be in the future. |
| 136 | RC-MSG51 | Exception (toast error) | `findBookingOrThrow()` — Không tìm thấy booking theo ID | Không tìm thấy đặt phòng với ID đã cho. | Booking not found with the provided ID. |
| 137 | RC-MSG52 | System log (ERROR) | `encryptCccd()` — Mã hóa AES-256 thất bại nội bộ | Lỗi hệ thống nội bộ trong quá trình mã hóa CCCD. | Internal system error during CCCD encryption. |
| 138 | RC-MSG53 | Exception (toast error) | `assertBookingIsActive()` — Booking không ở trạng thái Confirmed/Checked_In | Đặt phòng không ở trạng thái hoạt động (phải là Confirmed hoặc Checked_In). Trạng thái hiện tại: {status}. | Reservation is not active (must be Confirmed or Checked_In). Current status: {status}. |
| 139 | RC-MSG54 | Exception (toast error) | `assertNoDuplicateCccd()` — CCCD trùng với dependent khác hoặc khách chính trong cùng booking | Căn cước bị trùng với người khác trong cùng một đơn đặt phòng. | ID document conflicts with another guest in this booking. |
| 140 | RC-MSG55 | Exception (inline) | `validateCccdIfPresent()` — CCCD/Hộ chiếu không hợp lệ | Giấy tờ tùy thân không hợp lệ: CCCD phải gồm 12 chữ số hoặc Hộ chiếu hợp lệ. | Invalid identification document: Must be a 12-digit CCCD or a valid Passport. |
| 141 | RC-MSG56 | Exception (toast error) | `loadExistingDependent()` — Không tìm thấy Dependent theo ID khi cập nhật | Không tìm thấy khách đi kèm với ID đã cho. | Accompanying guest not found with the provided ID. |
| 142 | RC-MSG57 | Exception (toast error) | `linkGuestToRoomDetail()` — Không tìm thấy RoomBookingDetail theo ID | Không tìm thấy chi tiết đặt phòng theo ID phòng. | Room booking detail not found for the provided ID. |
| 143 | RC-MSG58 | Exception (toast error) | `applyGuestCountAndCalculateSurcharge()` — Số người lớn vượt maxAdults | Số lượng người lớn vượt quá sức chứa tối đa của phòng. Tối đa: {n} người lớn. | Number of adults exceeds the room's maximum capacity. Maximum: {n} adult(s). |
| 144 | RC-MSG59 | Exception (toast error) | `applyGuestCountAndCalculateSurcharge()` — Số trẻ em vượt maxChildren | Số lượng trẻ em vượt quá sức chứa tối đa của phòng. Tối đa: {n} trẻ em. | Number of children exceeds the room's maximum capacity. Maximum: {n} child(ren). |




---

## 📊 Summary

| Actor | Message Code Range | Total |
|---|---|---|
| CUS — Customer/Guest | CUS-MSG01 → CUS-MSG54 | 54 |
| RC — Receptionist | RC-MSG01 → RC-MSG59 | 59 |
| FNB — FnB Staff | FNB-MSG01 → FNB-MSG13 | 13 |
| ADM — Admin | ADM-MSG01 → ADM-MSG08 | 8 |
| MGR — Manager | MGR-MSG01 → MGR-MSG04 | 4 |
| TG — Tour Guide | TG-MSG01 | 1 |
| HK — Housekeeping | HK-MSG01 → HK-MSG04 | 4 |
| MT — Maintenance | MT-MSG01 → MT-MSG05 | 5 |
| SYS — System | SYS-MSG01 → SYS-MSG08 | 8 |

| **TOTAL** | | **156** |

---

> **Message Type Legend:**
> | Type | Mô tả |
> |---|---|
> | `Exception (inline)` | Hiển thị ngay dưới ô nhập liệu, màu đỏ |
> | `Exception (toast error)` | Hiển thị dạng toast/popup thông báo lỗi |
> | `Toast success` | Toast/banner thông báo thành công (xanh lá) |
> | `Flash success` | Flash attribute sau redirect (banner xanh lá) |
> | `Flash error` | Flash attribute sau redirect (banner đỏ) |
> | `System log` | Log nội bộ, không hiển thị ra UI người dùng |
> | `HTTP 4xx/5xx` | Trang lỗi đầy đủ do GlobalExceptionHandler xử lý |
