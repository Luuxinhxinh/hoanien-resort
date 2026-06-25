// book-table.js

let flatpickrReserveDate, flatpickrFilterDate;
let flatpickrStartTime, flatpickrEndTime;
let flatpickrFilterStart, flatpickrFilterEnd;

document.addEventListener("DOMContentLoaded", () => {
    // Initialize Flatpickr for dates
    flatpickrFilterDate = flatpickr("#filterDate", {
        dateFormat: "Y-m-d",
        minDate: "today"
    });
    
    flatpickrReserveDate = flatpickr("#reserveDate", {
        dateFormat: "Y-m-d",
        minDate: "today"
    });

    // Initialize Flatpickr for times (24-hour format)
    const timeConfig = {
        enableTime: true,
        noCalendar: true,
        dateFormat: "H:i",
        time_24hr: true
    };

    flatpickrFilterStart = flatpickr("#filterStart", timeConfig);
    flatpickrFilterEnd = flatpickr("#filterEnd", timeConfig);
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

        // Update UI
        const tableCards = document.querySelectorAll('.table-card');
        tableCards.forEach(card => {
            const tableId = parseInt(card.getAttribute('data-table-id'));
            if (availableTableIds.includes(tableId)) {
                card.classList.remove('table-disabled');
            } else {
                card.classList.add('table-disabled');
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
document.getElementById('bookingForm').addEventListener('submit', async function(e) {
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

    if (startTime >= endTime) {
        alert('Giờ kết thúc phải lớn hơn Giờ bắt đầu.');
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
            const errorMsg = await response.text();
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
