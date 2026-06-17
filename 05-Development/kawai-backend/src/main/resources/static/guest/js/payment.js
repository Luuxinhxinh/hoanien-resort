/**
 * checkout.js — Trang hoàn tất đặt phòng (UC10)
 *
 * Flow:
 *  1. Lấy bookingId từ URL param
 *  2. Gọi GET /api/bookings/{id} để lấy chi tiết booking
 *  3. Render Order Summary (bên phải)
 *  4. Submit form → PUT /api/bookings/{id}/customer-info → redirect VNPay hoặc profile
 *  5. Áp mã coupon → POST /api/bookings/{id}/apply-coupon
 *  6. Nút Hủy → DELETE/cancel booking → redirect /booking
 */

// ── URL helpers ──────────────────────────────────────────────────────────────

function getBookingIdFromUrl() {
    const params = new URLSearchParams(window.location.search);
    const id = params.get('bookingId');
    return id ? parseInt(id, 10) : null;
}

// ── Toast ────────────────────────────────────────────────────────────────────

function coToast(msg, type = 'success') {
    const container = document.getElementById('co-toast-container');
    if (!container) return;
    const el = document.createElement('div');
    el.className = `co-toast ${type}`;
    el.innerHTML = `
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="${type === 'success' ? '#2e7d3a' : '#c0392b'}" stroke-width="2.5">
            ${type === 'success'
            ? '<path d="M20 6L9 17l-5-5"/>'
            : '<path d="M18 6L6 18M6 6l12 12"/>'}
        </svg>
        <span>${msg}</span>`;
    container.appendChild(el);
    requestAnimationFrame(() => el.classList.add('show'));
    setTimeout(() => {
        el.classList.remove('show');
        setTimeout(() => el.remove(), 350);
    }, 4000);
}

// ── Format helpers ───────────────────────────────────────────────────────────

const VI_DAYS = ['Chủ Nhật', 'Thứ Hai', 'Thứ Ba', 'Thứ Tư', 'Thứ Năm', 'Thứ Sáu', 'Thứ Bảy'];
const VI_MONTHS = ['Tháng 1', 'Tháng 2', 'Tháng 3', 'Tháng 4', 'Tháng 5', 'Tháng 6',
    'Tháng 7', 'Tháng 8', 'Tháng 9', 'Tháng 10', 'Tháng 11', 'Tháng 12'];

function formatVnDate(dateStr) {
    // dateStr: "2025-06-20"
    if (!dateStr) return '—';
    const d = new Date(dateStr + 'T00:00:00');
    return `${VI_DAYS[d.getDay()]}, ${d.getDate()} ${VI_MONTHS[d.getMonth()]} ${d.getFullYear()}`;
}

function fmtVnd(amount) {
    if (amount == null) return '—';
    return Number(amount).toLocaleString('vi-VN') + ' ₫';
}

function nightsBetween(checkIn, checkOut) {
    if (!checkIn || !checkOut) return 0;
    const msPerDay = 86400000;
    return Math.max(0, Math.round(
        (new Date(checkOut + 'T00:00:00') - new Date(checkIn + 'T00:00:00')) / msPerDay
    ));
}

// ── State ────────────────────────────────────────────────────────────────────

let bookingData = null;   // data từ API
let appliedCoupon = null;   // { code, discountAmount }
let paymentTimerInterval = null;
let isPaymentSubmitted = false; // Add flag to detect intended navigation

// ── Load Booking Detail ───────────────────────────────────────────────────────

async function loadBookingDetail(bookingId) {
    try {
        const res = await fetch(`/api/bookings/${bookingId}`);
        if (!res.ok) {
            if (res.status === 404) {
                coToast('Không tìm thấy đơn đặt phòng. Vui lòng quay lại.', 'error');
                setTimeout(() => window.location.href = '/booking', 2000);
                return;
            }
            throw new Error('HTTP ' + res.status);
        }
        bookingData = await res.json();
        renderOrderSummary(bookingData);
    } catch (e) {
        console.error('loadBookingDetail error:', e);
        coToast('Không thể tải thông tin đặt phòng. Vui lòng thử lại.', 'error');
    }
}

// ── Render Order Summary ──────────────────────────────────────────────────────

function renderOrderSummary(data) {
    // Hotel / rooms header
    setText('op-hotel-name', data.hotelName || 'HoaNien Resort');

    // Room badges
    const badgesEl = document.getElementById('op-room-badges');
    if (badgesEl) {
        const rooms = data.roomCategories || [];
        badgesEl.innerHTML = rooms.length > 0
            ? rooms.map(r => `<span class="op-badge">${r}</span>`).join('')
            : '<span class="op-badge">—</span>';
    }

    // Dates
    setText('op-checkin-date', formatVnDate(data.checkInDate));
    setText('op-checkout-date', formatVnDate(data.checkOutDate));

    const nights = nightsBetween(data.checkInDate, data.checkOutDate);
    const guests = (data.totalAdults || 0) + (data.totalChildren || 0);
    setText('op-nights', nights + ' đêm');
    setText('op-guests', guests + ' khách');

    // Price breakdown
    const basePrice = data.baseRoomPrice || 0;
    const servicesFee = data.servicesFee || 0;
    const promotion = data.promotionDiscount || 0;
    const total = data.totalAmount || 0;

    const nightLabel = nights > 0 && basePrice > 0
        ? `(${nights} đêm × ${fmtVnd(basePrice / (nights || 1))})`
        : '';

    setText('op-price-label', `Giá phòng ${nightLabel}`);
    setText('op-price-value', fmtVnd(basePrice));
    setText('op-service-value', fmtVnd(servicesFee));
    setText('op-discount-value', promotion > 0 ? `- ${fmtVnd(promotion)}` : '0 ₫');

    const discountEl = document.getElementById('op-discount-value');
    if (discountEl) discountEl.className = 'value' + (promotion > 0 ? ' discount' : '');

    // Show/hide discount row
    const discountRow = document.getElementById('op-discount-row');
    if (discountRow) discountRow.style.display = promotion > 0 ? 'flex' : 'none';

    // Total
    setText('op-total-value', fmtVnd(total));

    // Deposit (30% cọc calculated on backend)
    const deposit = data.depositAmount || 0;
    setText('op-deposit-value', fmtVnd(deposit));

    // Update submit button text with amount
    const btnPay = document.getElementById('btnPayNow');
    if (btnPay) {
        btnPay.setAttribute('data-amount', deposit);
        if (!btnPay.classList.contains('loading')) {
            btnPay.textContent = `THANH TOÁN ĐẶT CỌC: ${fmtVnd(deposit)}`;
        }
    }

    // Handle timer
    const timerContainer = document.getElementById('payment-timer-container');
    const timerEl = document.getElementById('payment-timer');

    if (data.bookingStatus === 'CANCELLED' || (data.bookingStatus === 'HOLD' && data.remainingHoldSeconds <= 0)) {
        if (timerContainer) timerContainer.classList.add('hidden');
        if (btnPay) btnPay.disabled = true;
        coToast('Đơn đặt phòng đã bị hủy do quá hạn thanh toán.', 'error');
        isPaymentSubmitted = true; // Prevent unload warning
        setTimeout(() => window.location.href = '/booking', 2500);
        return;
    }

    if (data.bookingStatus === 'HOLD' && data.remainingHoldSeconds > 0) {
        if (timerContainer) timerContainer.classList.remove('hidden');
        startPaymentTimer(data.remainingHoldSeconds, timerEl);
    }
}

function startPaymentTimer(seconds, timerEl) {
    if (paymentTimerInterval) clearInterval(paymentTimerInterval);

    const targetEndTime = Date.now() + seconds * 1000;

    const updateTimer = () => {
        const remainingMs = targetEndTime - Date.now();
        const remainingSeconds = Math.max(0, Math.floor(remainingMs / 1000));

        if (remainingSeconds <= 0) {
            clearInterval(paymentTimerInterval);
            timerEl.textContent = '00:00';
            const btnPayNow = document.getElementById('btnPayNow');
            if (btnPayNow) btnPayNow.disabled = true;
            coToast('Đã hết thời gian thanh toán! Đơn phòng đã bị hủy.', 'error');
            isPaymentSubmitted = true; // Prevent unload warning
            setTimeout(() => window.location.href = '/booking', 2500);
            return;
        }
        const m = Math.floor(remainingSeconds / 60).toString().padStart(2, '0');
        const s = (remainingSeconds % 60).toString().padStart(2, '0');
        timerEl.textContent = `${m}:${s}`;
    };

    updateTimer(); // Initialize immediately so it doesn't show --:-- for 1 second
    paymentTimerInterval = setInterval(updateTimer, 1000);
}

function setText(id, val) {
    const el = document.getElementById(id);
    if (el) el.textContent = val;
}

// ── Coupon ───────────────────────────────────────────────────────────────────

async function applyCoupon() {
    const bookingId = getBookingIdFromUrl();
    const code = document.getElementById('couponInput')?.value?.trim();
    const msgEl = document.getElementById('couponMsg');

    if (!code) { showCouponMsg('Vui lòng nhập mã coupon.', false); return; }
    if (!bookingId) { showCouponMsg('Không xác định được đơn đặt phòng.', false); return; }

    const btn = document.getElementById('btnApplyCoupon');
    btn.disabled = true;
    btn.textContent = '...';

    try {
        const res = await fetch(`/api/bookings/${bookingId}/apply-coupon`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ couponCode: code })
        });
        const data = await res.json();

        if (res.ok && data.status === 'success') {
            appliedCoupon = { code, discountAmount: data.discountAmount };
            showCouponMsg(`Áp dụng thành công! Giảm ${fmtVnd(data.discountAmount)}`, true);
            // Reload booking detail to get updated totals
            await loadBookingDetail(bookingId);
        } else {
            showCouponMsg(data.message || 'Mã coupon không hợp lệ.', false);
        }
    } catch (e) {
        showCouponMsg('Lỗi kết nối. Vui lòng thử lại.', false);
    } finally {
        btn.disabled = false;
        btn.textContent = 'Áp dụng';
    }
}

function showCouponMsg(msg, isOk) {
    const el = document.getElementById('couponMsg');
    if (!el) return;
    el.textContent = msg;
    el.className = 'op-coupon-msg ' + (isOk ? 'ok' : 'err');
}

// ── Terms checkbox → enable pay button ───────────────────────────────────────

function onTermsChange(cb) {
    const btn = document.getElementById('btnPayNow');
    if (!btn) return;
    btn.classList.toggle('active', cb.checked);
    btn.disabled = !cb.checked;
}

// ── Form validation ───────────────────────────────────────────────────────────

function validateForm() {
    const fields = [
        { id: 'fieldFullName', label: 'Họ và tên' },
        { id: 'fieldPhone', label: 'Số điện thoại' },
        { id: 'fieldCccd', label: 'CCCD / Căn cước công dân' },
        { id: 'fieldEmail', label: 'Email' },
    ];
    for (const f of fields) {
        const el = document.getElementById(f.id);
        if (!el || !el.value.trim()) {
            coToast(`Vui lòng điền ${f.label}`, 'error');
            el?.focus();
            return false;
        }
    }
    // Email format
    const email = document.getElementById('fieldEmail')?.value?.trim();
    if (email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
        coToast('Email không hợp lệ.', 'error');
        return false;
    }
    return true;
}

// ── Submit Payment ────────────────────────────────────────────────────────────

async function submitPayment() {
    if (!validateForm()) return;

    const bookingId = getBookingIdFromUrl();
    if (!bookingId) { coToast('Không xác định được đơn đặt phòng.', 'error'); return; }

    const btn = document.getElementById('btnPayNow');
    btn.disabled = true;
    btn.classList.add('loading');
    btn.textContent = 'ĐANG XỬ LÝ...';

    const payload = {
        fullName: document.getElementById('fieldFullName')?.value?.trim(),
        phone: document.getElementById('fieldPhone')?.value?.trim(),
        cccd: document.getElementById('fieldCccd')?.value?.trim(),
        email: document.getElementById('fieldEmail')?.value?.trim(),
        address: document.getElementById('fieldAddress')?.value?.trim() || null,
        notes: document.getElementById('fieldNotes')?.value?.trim() || null,
        paymentMethod: 'VNPAY',
        couponCode: appliedCoupon?.code || null,
    };

    try {
        const res = await fetch(`/api/bookings/${bookingId}/confirm`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const data = await res.json();

        if (res.ok && data.status === 'success') {
            isPaymentSubmitted = true; // Prevent unload warning
            if (data.paymentUrl) {
                // Redirect đến VNPay
                window.location.href = data.paymentUrl;
            } else {
                coToast('Đặt phòng thành công!', 'success');
                setTimeout(() => window.location.href = '/profile', 1500);
            }
        } else {
            coToast(data.message || 'Có lỗi xảy ra. Vui lòng thử lại.', 'error');
            btn.disabled = false;
            btn.classList.remove('loading');
            const depAmount = btn.getAttribute('data-amount');
            btn.textContent = depAmount ? `THANH TOÁN ĐẶT CỌC: ${fmtVnd(depAmount)}` : 'THANH TOÁN NGAY';
            // Re-check terms
            const cb = document.getElementById('termsCheck');
            if (cb?.checked) btn.classList.add('active');
        }
    } catch (e) {
        console.error(e);
        coToast('Lỗi kết nối Server! Vui lòng thử lại.', 'error');
        btn.disabled = false;
        btn.classList.remove('loading');
        const depAmount = btn.getAttribute('data-amount');
        btn.textContent = depAmount ? `THANH TOÁN ĐẶT CỌC: ${fmtVnd(depAmount)}` : 'THANH TOÁN NGAY';
        const cb = document.getElementById('termsCheck');
        if (cb?.checked) btn.classList.add('active');
    }
}

// ── Init ─────────────────────────────────────────────────────────────────────

document.addEventListener('DOMContentLoaded', () => {
    const bookingId = getBookingIdFromUrl();

    if (!bookingId) {
        // Không có bookingId → không có gì để thanh toán
        coToast('Không tìm thấy đơn đặt phòng. Đang chuyển về trang đặt phòng...', 'error');
        setTimeout(() => window.location.href = '/booking', 2500);
        return;
    }

    loadBookingDetail(bookingId);

    // Coupon enter key
    document.getElementById('couponInput')?.addEventListener('keydown', e => {
        if (e.key === 'Enter') { e.preventDefault(); applyCoupon(); }
    });

    // Cập nhật lại thời gian khi chuyển qua lại giữa các tab
    document.addEventListener("visibilitychange", () => {
        if (document.visibilityState === "visible" && !isPaymentSubmitted) {
            loadBookingDetail(bookingId);
        }
    });

    // Cảnh báo khi người dùng rời khỏi trang và hủy đơn
    window.addEventListener('beforeunload', (e) => {
        if (!isPaymentSubmitted && bookingData && bookingData.bookingStatus === 'HOLD' && bookingData.remainingHoldSeconds > 0) {
            // Hiển thị thông báo xác nhận rời trang mặc định của trình duyệt
            e.preventDefault();
            e.returnValue = '';
        }
    });

    // Thực hiện gọi API hủy đơn khi thực sự rời trang
    window.addEventListener('pagehide', (e) => {
        if (!isPaymentSubmitted && bookingData && bookingData.bookingStatus === 'HOLD' && bookingData.remainingHoldSeconds > 0) {
            // Sử dụng sendBeacon để đảm bảo request được gửi đi ngay cả khi trang đóng
            navigator.sendBeacon(`/api/bookings/${bookingId}/cancel`);
        }
    });
});
