// book-table.js

let flatpickrReserveDate, flatpickrFilterDate;
let flatpickrStartTime, flatpickrEndTime;
let flatpickrFilterStart, flatpickrFilterEnd;

document.addEventListener("DOMContentLoaded", () => {
    let minDateVal = "today";
    let maxDateVal = null;

    if (typeof HAS_VALID_BOOKING !== 'undefined' && HAS_VALID_BOOKING) {
        if (typeof VALID_CHECK_IN !== 'undefined' && VALID_CHECK_IN) {
            const checkInDate = new Date(VALID_CHECK_IN);
            const today = new Date();
            today.setHours(0, 0, 0, 0);
            minDateVal = checkInDate > today ? VALID_CHECK_IN : "today";
        }
        if (typeof VALID_CHECK_OUT !== 'undefined' && VALID_CHECK_OUT) {
            maxDateVal = VALID_CHECK_OUT;
        }
    }

    // Initialize Flatpickr for dates
    flatpickrFilterDate = flatpickr("#filterDate", {
        dateFormat: "Y-m-d",
        minDate: minDateVal,
        maxDate: maxDateVal || new Date().fp_incr(7),
        defaultDate: "today"
    });

    flatpickrReserveDate = flatpickr("#reserveDate", {
        dateFormat: "Y-m-d",
        minDate: minDateVal,
        maxDate: maxDateVal || new Date().fp_incr(7),
        defaultDate: "today"
    });

    // Initialize Flatpickr for times (24-hour format)
    const timeConfig = {
        enableTime: true,
        noCalendar: true,
        dateFormat: "H:i",
        time_24hr: true
    };

    const now = new Date();
    const currentHour = String(now.getHours()).padStart(2, '0');
    const currentMin = String(now.getMinutes()).padStart(2, '0');
    const currentTimeStr = `${currentHour}:${currentMin}`;

    flatpickrFilterStart = flatpickr("#filterStart", Object.assign({}, timeConfig, { defaultDate: currentTimeStr }));
    flatpickrFilterEnd = flatpickr("#filterEnd", Object.assign({}, timeConfig, { defaultDate: "23:59" }));
    flatpickrStartTime = flatpickr("#startTime", timeConfig);
    flatpickrEndTime = flatpickr("#endTime", timeConfig);
});

function openBookingModal(tableId, tableNumber, capacity) {
    if (typeof IS_LOGGED_IN !== 'undefined' && !IS_LOGGED_IN) {
        if (typeof openLoginModal === 'function') {
            openLoginModal();
        } else {
            window.location.href = '/ops-login?redirect_to=/book-table';
        }
        return;
    }

    document.getElementById('tableId').value = tableId;
    document.getElementById('modalTableNumber').innerText = "Bàn " + tableNumber;
    document.getElementById('modalTableCapacity').innerText = capacity;

    // Pre-fill from filter if available
    const filterDate = document.getElementById('filterDate').value;
    const filterStart = document.getElementById('filterStart').value;
    const filterEnd = document.getElementById('filterEnd').value;

    if (filterDate && filterStart && filterEnd) {
        flatpickrReserveDate.setDate(filterDate);
        flatpickrStartTime.setDate(filterStart);
        flatpickrEndTime.setDate(filterEnd);

        // Disable inputs if pre-filled
        document.getElementById('reserveDate').disabled = true;
        document.getElementById('startTime').disabled = true;
        document.getElementById('endTime').disabled = true;
    } else {
        document.getElementById('reserveDate').disabled = false;
        document.getElementById('startTime').disabled = false;
        document.getElementById('endTime').disabled = false;
    }

    document.getElementById('bookingModal').style.display = 'flex';
}

function closeBookingModal() {
    document.getElementById('bookingModal').style.display = 'none';
    document.getElementById('bookingForm').reset();
}

function closeSuccessModal() {
    document.getElementById('successModal').style.display = 'none';
}

async function checkAvailability() {
    const filterDate = document.getElementById('filterDate').value;
    const filterStart = document.getElementById('filterStart').value;
    const filterEnd = document.getElementById('filterEnd').value;

    if (!filterDate || !filterStart || !filterEnd) {
        alert("Vui lòng chọn đầy đủ Ngày, Giờ bắt đầu và Giờ kết thúc để lọc.");
        return;
    }

    if (filterStart >= filterEnd) {
        alert("Giờ kết thúc phải lớn hơn Giờ bắt đầu.");
        return;
    }

    try {
        const response = await fetch(`/api/v1/tables/availability?date=${filterDate}&start=${filterStart}&end=${filterEnd}`);
        if (!response.ok) throw new Error("Network response was not ok");
        const availableTableIds = await response.json();

        // Update UI Sơ đồ SVG
        const svgTables = document.querySelectorAll('.map-table');
        svgTables.forEach(card => {
            const tableId = parseInt(card.getAttribute('data-table-id'));
            if (availableTableIds.includes(tableId)) {
                // Bàn trống -> sáng màu Xanh ngọc, cho phép click
                card.classList.remove('occupied');
                card.classList.add('available');
            } else {
                // Bàn bận -> tối màu, vô hiệu hóa
                card.classList.remove('available');
                card.classList.add('occupied');
            }
        });

    } catch (error) {
        console.error("Failed to fetch availability:", error);
        alert("Đã có lỗi xảy ra khi kiểm tra lịch trống. Vui lòng thử lại sau.");
    }
}

// ── Custom confirm dialog for missing endTime ──────────────────────────────
function showEndTimeConfirm(onConfirm) {
    document.getElementById('endTimeConfirmModal').style.display = 'flex';
    // Store callback
    window._endTimeConfirmCallback = onConfirm;
}

function confirmEndTimeAuto() {
    document.getElementById('endTimeConfirmModal').style.display = 'none';
    if (typeof window._endTimeConfirmCallback === 'function') {
        window._endTimeConfirmCallback();
        window._endTimeConfirmCallback = null;
    }
}

function cancelEndTimeAuto() {
    document.getElementById('endTimeConfirmModal').style.display = 'none';
    window._endTimeConfirmCallback = null;
    // Re-focus endTime input so guest can fill it in
    const endTimeInput = document.getElementById('endTime');
    if (endTimeInput && !endTimeInput.disabled) {
        endTimeInput.focus();
        if (flatpickrEndTime) flatpickrEndTime.open();
    }
}

// Handle form submission
document.getElementById('bookingForm').addEventListener('submit', async function (e) {
    e.preventDefault();

    const tableId = document.getElementById('tableId').value;
    const reserveDate = document.getElementById('reserveDate').value;
    const startTime = document.getElementById('startTime').value;
    let endTime = document.getElementById('endTime').value;
    const partySize = document.getElementById('partySize').value;
    const specialRequests = document.getElementById('specialRequests').value;

    // Validate required fields (except endTime)
    if (!reserveDate || !startTime) {
        alert('Vui lòng nhập đầy đủ Ngày sử dụng và Giờ bắt đầu.');
        return;
    }

    // If endTime is missing → show custom confirm
    if (!endTime) {
        showEndTimeConfirm(async () => {
            // Auto-calculate endTime = startTime + 1 hour
            const [h, m] = startTime.split(':').map(Number);
            const autoEnd = new Date(2000, 0, 1, h + 1, m);
            const autoEndStr = String(autoEnd.getHours()).padStart(2, '0') + ':' + String(autoEnd.getMinutes()).padStart(2, '0');

            // Set it visually on the flatpickr
            if (flatpickrEndTime) flatpickrEndTime.setDate(autoEndStr);
            document.getElementById('endTime').value = autoEndStr;

            await submitReservation(tableId, reserveDate, startTime, autoEndStr, partySize, specialRequests);
        });
        return;
    }

    const [startH, startM] = startTime.split(':').map(Number);
    const [endH, endM] = endTime.split(':').map(Number);
    let startMins = startH * 60 + startM;
    let endMins = endH * 60 + endM;

    if (endMins <= startMins) {
        endMins += 24 * 60;
    }

    if (endMins - startMins > 12 * 60) {
        alert('Thời gian đặt bàn quá dài hoặc giờ kết thúc không hợp lệ.');
        return;
    }

    await submitReservation(tableId, reserveDate, startTime, endTime, partySize, specialRequests);
});

async function submitReservation(tableId, reserveDate, startTime, endTime, partySize, specialRequests) {
    const payload = {
        tableId: parseInt(tableId),
        reserveDate: reserveDate,
        startTime: startTime,
        endTime: endTime,
        partySize: parseInt(partySize),
        specialRequests: specialRequests
    };

    try {
        const response = await fetch('/api/v1/tables/reservations', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            let errorMsg = await response.text();
            try {
                const parsed = JSON.parse(errorMsg);
                if (parsed.message) errorMsg = parsed.message;
            } catch (e) { }
            alert('Lỗi đặt bàn: ' + errorMsg);
            return;
        }

        // Hide booking modal and show success modal
        closeBookingModal();
        document.getElementById('successModal').style.display = 'flex';
        checkAvailability(); // Refresh table status
    } catch (error) {
        console.error('Failed to submit reservation:', error);
        alert('Đã có lỗi xảy ra. Vui lòng thử lại.');
    }
}

// Scroll effect for navbar (though it's set to solid initially, we keep the logic)
const guestNav = document.querySelector('.guest-nav');
const onScroll = () => {
    const scrolled = window.scrollY > 10;
    if (guestNav) {
        if (scrolled) guestNav.classList.add('scrolled');
        else guestNav.classList.remove('scrolled');
    }
};
window.addEventListener('scroll', onScroll, { passive: true });
onScroll();

// Khởi tạo Sơ đồ Bàn (SVG Map)
document.addEventListener('DOMContentLoaded', () => {
    if (typeof SERVER_TABLES !== 'undefined' && SERVER_TABLES.length > 0) {
        const svgTables = document.querySelectorAll('.map-table');
        svgTables.forEach(svgTable => {
            const num = parseInt(svgTable.getAttribute('data-table-num'));
            // Tìm thông tin bàn tương ứng từ mảng data của Server (lọc bỏ các ký tự chữ cái, ví dụ "T01" -> 1)
            const tableData = SERVER_TABLES.find(t => {
                const tableNumStr = String(t.tableNumber).replace(/[^0-9]/g, '');
                return parseInt(tableNumStr) === num;
            });

            if (tableData) {
                // Gắn dữ liệu ID và Sức chứa vào thẻ SVG
                svgTable.setAttribute('data-table-id', tableData.id);
                svgTable.setAttribute('data-capacity', tableData.capacity);

                // Quy ước hình dáng và kích thước theo số lượng khách
                const cap = parseInt(tableData.capacity);
                let shapeHtml = '';
                if (cap <= 2) {
                    // Bàn 2 người: hình vuông nhỏ
                    shapeHtml = `<rect x="-25" y="-25" width="50" height="50" rx="8" />`;
                } else if (cap <= 4) {
                    // Bàn 4 người: hình tròn nhỏ
                    shapeHtml = `<circle cx="0" cy="0" r="30" />`;
                } else if (cap <= 6) {
                    // Bàn 6 người: hình vuông trung bình
                    shapeHtml = `<rect x="-35" y="-35" width="70" height="70" rx="10" />`;
                } else if (cap <= 8) {
                    // Bàn 8 người: hình tròn trung bình
                    shapeHtml = `<circle cx="0" cy="0" r="45" />`;
                } else if (cap <= 10) {
                    // Bàn 10 người: hình tròn to
                    shapeHtml = `<circle cx="0" cy="0" r="55" />`;
                } else {
                    // Bàn 12 người (trở lên): hình vuông to
                    shapeHtml = `<rect x="-45" y="-45" width="90" height="90" rx="12" />`;
                }

                // Chữ hiển thị trên bàn (Mã bàn và số lượng khách)
                const displayNum = tableData.tableNumber;
                const textHtml = `
                    <text x="0" y="-8" text-anchor="middle" dominant-baseline="central" font-weight="bold" font-size="16" fill="#fff">${displayNum}</text>
                    <text x="0" y="12" text-anchor="middle" dominant-baseline="central" font-size="11" fill="rgba(255,255,255,0.8)">${cap} pax</text>
                `;

                svgTable.innerHTML = shapeHtml + textHtml;

                // Mặc định lúc mới vào trang (chưa lọc) thì các bàn đều khả dụng
                svgTable.classList.add('available');

                // Gắn sự kiện click mở form đặt bàn
                svgTable.addEventListener('click', function () {
                    // Chỉ cho click nếu bàn có class available
                    if (this.classList.contains('available')) {
                        openBookingModal(tableData.id, tableData.tableNumber, tableData.capacity);
                    }
                });
            } else {
                // Nếu trên sơ đồ vẽ dư bàn nhưng DB không có, thì ẩn bàn đó đi
                svgTable.style.display = 'none';
            }
        });
    }
});
