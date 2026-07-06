/**
 * checkout.js — Trang hoàn tất đặt phòng (UC10)
 *
 * Flow:
 *  1. Lấy bookingId từ URL param
 *  2. Gọi GET /api/bookings/{id} để lấy chi tiết booking
 *  3. Render Order Summary (bên phải)
 *  4. Submit form → POST /api/bookings/{id}/confirm → redirect VNPay hoặc profile
 *  5. Áp mã coupon → POST /api/bookings/{id}/apply-coupon
 *  6. Nút Hủy → DELETE/cancel booking → redirect /booking
 */

// ── URL helpers ──────────────────────────────────────────────────────────────

function getBookingIdFromUrl() {
    const params = new URLSearchParams(window.location.search);
    const id = params.get('bookingId');
    return id ? parseInt(id, 10) : null;
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

function nightsBetween(checkIn, checkOut) {
    if (!checkIn || !checkOut) return 0;
    const msPerDay = 86400000;
    return Math.max(0, Math.round(
        (new Date(checkOut + 'T00:00:00') - new Date(checkIn + 'T00:00:00')) / msPerDay
    ));
}

let bookingData = null;
let appliedCoupon = null;
let paymentTimerInterval = null;
let isPaymentSubmitted = false;
let canAbandonCheckout = false;


async function loadBookingDetail(bookingId) {
    try {
        const res = await fetch(`/api/bookings/${bookingId}`);
        if (!res.ok) {
            if (res.status === 404) {
                showToast('Không tìm thấy đơn đặt phòng. Vui lòng quay lại.', 'error');
                setTimeout(() => window.location.href = '/booking', 2000);
                return;
            }
            throw new Error('HTTP ' + res.status);
        }
        bookingData = await res.json();

        const currentStatus = (bookingData.bookingStatus || '').toUpperCase();
        if (currentStatus === 'PENDING' || currentStatus === 'PENDING_PAYMENT') {
            canAbandonCheckout = true;
        } else if (currentStatus === 'CANCELLED' || currentStatus === 'CANCELLED_PAYMENT') {
            canAbandonCheckout = false;
            showToast('Đơn đặt phòng này đã bị hủy. Đang chuyển về trang đặt phòng...', 'error');
            setTimeout(() => window.location.replace('/booking'), 2000);
            return;
        } else {
            canAbandonCheckout = false;
            isPaymentSubmitted = true; // prevent unload events

            // Hide the container to prevent interaction
            const container = document.querySelector('.checkout-container');
            if (container) container.style.display = 'none';

            // Redirect based on status
            if (currentStatus === 'CONFIRMED' || currentStatus === 'CHECKED_IN' || currentStatus === 'CHECKED_OUT') {
                window.location.replace('/profile');
            } else {
                window.location.replace('/booking');
            }
            return;
        }

        renderOrderSummary(bookingData);
    } catch (e) {
        console.error('loadBookingDetail error:', e);
        showToast('Không thể tải thông tin đặt phòng. Vui lòng thử lại.', 'error');
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
        ? `(${nights} đêm × ${formatCurrency(basePrice / (nights || 1), true)})`
        : '';

    setText('op-price-label', `Giá phòng ${nightLabel}`);
    setText('op-price-value', formatCurrency(basePrice, true));
    setText('op-service-value', formatCurrency(servicesFee, true));
    setText('op-discount-value', promotion > 0 ? `- ${formatCurrency(promotion, true)}` : '0 ₫');

    const discountEl = document.getElementById('op-discount-value');
    if (discountEl) discountEl.className = 'value' + (promotion > 0 ? ' discount' : '');
    const discountRow = document.getElementById('op-discount-row');
    if (discountRow) discountRow.style.display = promotion > 0 ? 'flex' : 'none';
    setText('op-total-value', formatCurrency(total, true));
    const deposit = data.depositAmount || 0;
    setText('op-deposit-value', formatCurrency(deposit, true));

    // Update submit button text with amount
    const btnPay = document.getElementById('btnPayNow');
    if (btnPay) {
        btnPay.setAttribute('data-amount', deposit);
        if (!btnPay.classList.contains('loading')) {
            btnPay.textContent = `THANH TOÁN ĐẶT CỌC: ${formatCurrency(deposit, true)}`;
        }
    }

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
            showCouponMsg(`Áp dụng thành công! Giảm ${formatCurrency(data.discountAmount, true)}`, true);
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
        { id: 'fieldEmail', label: 'Email' },
        { id: 'fieldDob', label: 'Ngày sinh' }
    ];
    for (const f of fields) {
        const el = document.getElementById(f.id);
        if (!el || !el.value.trim()) {
            showToast(`Vui lòng điền ${f.label}`, 'error');
            el?.focus();
            return false;
        }
    }
    // Email format
    const email = document.getElementById('fieldEmail')?.value?.trim();
    if (email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
        showToast('Email không hợp lệ.', 'error');
        return false;
    }

    // Age check
    const dobStr = document.getElementById('fieldDob')?.value;
    if (dobStr) {
        const dob = new Date(dobStr);
        const ageDifMs = Date.now() - dob.getTime();
        const ageDate = new Date(ageDifMs);
        const age = Math.abs(ageDate.getUTCFullYear() - 1970);
        if (age < 18) {
            showToast('Bạn phải từ đủ 18 tuổi trở lên để đặt phòng.', 'error');
            return false;
        }
    }
    return true;
}

// ── Submit Payment ────────────────────────────────────────────────────────────

async function submitPayment() {
    if (!validateForm()) return;

    const bookingId = getBookingIdFromUrl();
    if (!bookingId) { showToast('Không xác định được đơn đặt phòng.', 'error'); return; }

    const btn = document.getElementById('btnPayNow');
    btn.disabled = true;
    btn.classList.add('loading');
    btn.textContent = 'ĐANG XỬ LÝ...';

    const methodEl = document.querySelector('input[name="paymentMethod"]:checked');
    const payload = {
        fullName: document.getElementById('fieldFullName')?.value?.trim(),
        phone: document.getElementById('fieldPhone')?.value?.trim(),
        email: document.getElementById('fieldEmail')?.value?.trim(),
        dateOfBirth: document.getElementById('fieldDob')?.value?.trim() || null,
        address: document.getElementById('fieldAddress')?.value?.trim() || null,
        notes: document.getElementById('fieldNotes')?.value?.trim() || null,
        paymentMethod: methodEl ? methodEl.value : 'VNPAY',
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
                sessionStorage.setItem('vnpay_redirect_' + bookingId, 'true');
                window.location.href = data.paymentUrl;
            } else {
                showToast('Đặt phòng thành công!', 'success');
                setTimeout(() => window.location.href = '/profile', 1500);
            }
        } else {
            showToast(data.message || 'Có lỗi xảy ra. Vui lòng thử lại.', 'error');
            btn.disabled = false;
            btn.classList.remove('loading');
            const depAmount = btn.getAttribute('data-amount');
            btn.textContent = depAmount ? `THANH TOÁN ĐẶT CỌC: ${formatCurrency(depAmount, true)}` : 'THANH TOÁN NGAY';
            // Re-check terms
            const cb = document.getElementById('termsCheck');
            if (cb?.checked) btn.classList.add('active');
        }
    } catch (e) {
        console.error(e);
        showToast('Lỗi kết nối Server! Vui lòng thử lại.', 'error');
        btn.disabled = false;
        btn.classList.remove('loading');
        const depAmount = btn.getAttribute('data-amount');
        btn.textContent = depAmount ? `THANH TOÁN ĐẶT CỌC: ${formatCurrency(depAmount, true)}` : 'THANH TOÁN NGAY';
        const cb = document.getElementById('termsCheck');
        if (cb?.checked) btn.classList.add('active');
    }
}

// ── Init ─────────────────────────────────────────────────────────────────────

document.addEventListener('DOMContentLoaded', () => {
    const bookingId = getBookingIdFromUrl();

    if (!bookingId) {
        // Không có bookingId → không có gì để thanh toán
        showToast('Không tìm thấy thông tin đơn đặt phòng.', 'error');
        setTimeout(() => window.location.href = '/booking', 2000);
        return;
    }

    // Check if user backed out from VNPay via Browser Back Button
    if (sessionStorage.getItem('vnpay_redirect_' + bookingId) === 'true') {
        sessionStorage.removeItem('vnpay_redirect_' + bookingId);
        // User came back from VNPay. Cancel immediately to free the room.
        fetch(`/api/bookings/${bookingId}/cancel`, { method: 'POST', keepalive: true }).finally(() => {
            showToast('Giao dịch đã bị hủy. Đang chuyển về trang đặt phòng...', 'error');
            setTimeout(() => window.location.replace('/booking'), 2000);
        });
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

    // Click logo để về trang chủ
    document.getElementById('paymentPageLogo')?.addEventListener('click', () => {
        window.location.href = '/';
    });

    // Hiển thị thông báo xác nhận khi cố gắng thoát trang (reload, close tab)
    window.addEventListener('beforeunload', (e) => {
        if (canAbandonCheckout && !isPaymentSubmitted && bookingId) {
            e.preventDefault();
            e.returnValue = 'Đơn đặt phòng của bạn sẽ bị hủy nếu bạn rời khỏi trang này. Bạn có chắc chắn muốn thoát?';
        }
    });

    // Hủy đơn đặt phòng ngay lập tức khi khách hàng rời khỏi trang (thoát, close tab)
    window.addEventListener('pagehide', () => {
        if (canAbandonCheckout && !isPaymentSubmitted && bookingId) {
            fetch(`/api/bookings/${bookingId}/cancel`, { method: 'POST', keepalive: true });
        }
    });

});

// Handle BFCache (when user clicks Back button from VNPay)
window.addEventListener('pageshow', (event) => {
    if (event.persisted) {
        isPaymentSubmitted = false;
        const bookingId = getBookingIdFromUrl();
        if (bookingId && sessionStorage.getItem('vnpay_redirect_' + bookingId) === 'true') {
            sessionStorage.removeItem('vnpay_redirect_' + bookingId);
            fetch(`/api/bookings/${bookingId}/cancel`, { method: 'POST', keepalive: true }).finally(() => {
                showToast('Giao dịch đã bị hủy. Đang chuyển về trang đặt phòng...', 'error');
                setTimeout(() => window.location.replace('/booking'), 2000);
            });
        }
    }
});
