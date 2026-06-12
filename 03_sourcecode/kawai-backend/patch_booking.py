import re

file_path = r'd:\SWP391\su26-swp391-se2023-g2\03_sourcecode\kawai-backend\src\main\resources\templates\guest\booking.html'

with open(file_path, 'r', encoding='utf-8', errors='ignore') as f:
    content = f.read()

old_func = """        function confirmBooking() {
            const roomName = document.getElementById('detailRoomName').innerText;
            const totalVal = document.getElementById('detailRoomTotal').innerText;
            const guestsText = document.getElementById('detailRoomGuests').innerText;

            let checkInStr = document.getElementById('checkInDisplay') ? document.getElementById('checkInDisplay').innerText : '12 Jun 2026';
            let checkOutStr = document.getElementById('checkOutDisplay') ? document.getElementById('checkOutDisplay').innerText : '15 Jun 2026';
            
            document.getElementById('confirmRoomName').innerText = roomName;
            document.getElementById('confirmRoomDates').innerText = `${checkInStr} - ${checkOutStr}`;
            document.getElementById('confirmRoomGuests').innerText = guestsText;
            document.getElementById('confirmRoomTotal').innerText = totalVal;

            closeRoomDetailsModal();
            document.getElementById('bookingConfirmModal').style.display = 'flex';
            showToast("Đặt phòng thành công! Kawaii đã sẵn sàng chào đón quý khách.", "success");
        }"""

new_func = """        function confirmBooking() {
            const roomName = document.getElementById('detailRoomName').innerText;
            const totalVal = document.getElementById('detailRoomTotal').innerText;
            const guestsText = document.getElementById('detailRoomGuests').innerText;

            let checkInStr = document.getElementById('checkInDisplay') ? document.getElementById('checkInDisplay').innerText : '12 Jun 2026';
            let checkOutStr = document.getElementById('checkOutDisplay') ? document.getElementById('checkOutDisplay').innerText : '15 Jun 2026';
            
            const depositAmount = parseFloat(totalVal.replace(/[^0-9]/g, '')) || 1000000;
            const roomNumber = prompt("Nhập số phòng muốn đặt (VD: V101, R201):", "V101");
            if(!roomNumber) return;

            // Ensure bookingState dates exist or fallback
            const chkIn = bookingState.checkIn ? formatLocalDate(bookingState.checkIn) : '2026-06-12';
            const chkOut = bookingState.checkOut ? formatLocalDate(bookingState.checkOut) : '2026-06-15';

            const payload = {
                customerId: 1, 
                roomNumber: roomNumber,
                checkInDate: chkIn,
                checkOutDate: chkOut,
                depositAmount: depositAmount
            };

            fetch('/api/bookings', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            })
            .then(res => res.json())
            .then(data => {
                if (data.status === 'success') {
                    document.getElementById('confirmRoomName').innerText = roomName;
                    document.getElementById('confirmRoomDates').innerText = `${checkInStr} - ${checkOutStr}`;
                    document.getElementById('confirmRoomGuests').innerText = guestsText;
                    document.getElementById('confirmRoomTotal').innerText = totalVal;

                    closeRoomDetailsModal();
                    document.getElementById('bookingConfirmModal').style.display = 'flex';
                    showToast("Đặt phòng thành công! Booking ID: " + data.bookingId, "success");
                } else {
                    showToast("Lỗi đặt phòng: " + data.message, "error");
                }
            })
            .catch(err => {
                console.error(err);
                showToast("Lỗi kết nối Server!", "error");
            });
        }"""

# Since the file might have weird encoding, just doing string replace
content = content.replace(old_func, new_func)

# If it didn't replace, try to regex match
if new_func not in content:
    pattern = re.compile(r'function confirmBooking\(\) \{.*?\n        \}', re.DOTALL)
    content = pattern.sub(new_func, content)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Patched booking.html")
