/**
 * payment.js — Trang điền thông tin & thanh toán (UC10)
 *
 * Luồng mới (Deferred DB Save):
 *  1. Đọc giỏ phòng từ sessionStorage('hoanien_room_cart') — không có bookingId trên URL
 *  2. Render Order Summary từ dữ liệu sessionStorage
 *  3. Áp mã coupon → GET /api/bookings/promo/validate (tính nháp, KHÔNG lưu DB)
 *  4. Submit → POST /api/bookings (gom giỏ + thông tin khách → tạo Pending_Payment + link VNPay)
 *  5. Sau khi redirect VNPay: nếu back lại → pagehide cancel Pending_Payment
 */

// ── Format helpers ───────────────────────────────────────────────────────────

const VI_DAYS = ['Chủ Nhật', 'Thứ Hai', 'Thứ Ba', 'Thứ Tư', 'Thứ Năm', 'Thứ Sáu', 'Thứ Bảy'];
const VI_MONTHS = ['Tháng 1', 'Tháng 2', 'Tháng 3', 'Tháng 4', 'Tháng 5', 'Tháng 6',
    'Tháng 7', 'Tháng 8', 'Tháng 9', 'Tháng 10', 'Tháng 11', 'Tháng 12'];

function formatVnDate(dateStr) {
    if (!dateStr) return '—';
    const d = new Date(dateStr + 'T00:00:00');
    return `${VI_DAYS[d.getDay()]}, ${d.getDate()} ${VI_MONTHS[d.getMonth()]} ${d.getFullYear()}`;
}

function nightsBetween(checkIn, checkOut) {
    if (!checkIn || !checkOut) return 0;
    return Math.max(0, Math.round(
        (new Date(checkOut + 'T00:00:00') - new Date(checkIn + 'T00:00:00')) / 86400000
    ));
}

function setText(id, val) {
    const el = document.getElementById(id);
    if (el) el.textContent = val;
}

// ── State ─────────────────────────────────────────────────────────────────────

let cartData = null;          // Giỏ phòng từ sessionStorage
let appliedCoupon = null;     // { code, discountAmount, finalPrice, depositAmount }
let isPaymentSubmitted = false;

// ── Load từ sessionStorage ────────────────────────────────────────────────────

function loadCartFromSession() {
    const raw = sessionStorage.getItem('hoanien_room_cart');
    if (!raw) {
        showToast('Giỏ phòng của bạn đã hết hạn. Vui lòng chọn lại phòng.', 'error');
        setTimeout(() => window.location.replace('/booking'), 2500);
        return false;
    }
    try {
        cartData = JSON.parse(raw);
        return true;
    } catch (e) {
        console.error('Lỗi parse hoanien_room_cart:', e);
        sessionStorage.removeItem('hoanien_room_cart');
        showToast('Dữ liệu giỏ phòng bị lỗi. Vui lòng chọn lại.', 'error');
        setTimeout(() => window.location.replace('/booking'), 2500);
        return false;
    }
}

// ── Pre-fill thông tin khách từ localStorage ──────────────────────────────────

function prefillCustomerInfo() {
    const raw = localStorage.getItem('hoanien_customer_info');
    if (!raw) return;
    try {
        const info = JSON.parse(raw);
        // Chỉ fill nếu field đang rỗng (không ghi đè data từ Thymeleaf)
        fillIfEmpty('fieldFullName', info.fullName);
        fillIfEmpty('fieldPhone', info.phone);
        fillIfEmpty('fieldEmail', info.email);
        fillIfEmpty('fieldDob', info.dateOfBirth);
    } catch (e) {
        // localStorage bị corrupt → bỏ qua
        localStorage.removeItem('hoanien_customer_info');
    }
}

function fillIfEmpty(fieldId, value) {
    const el = document.getElementById(fieldId);
    if (el && !el.value.trim() && value) el.value = value;
}

// ── Render Order Summary ──────────────────────────────────────────────────────

function renderOrderSummary(data) {
    setText('op-hotel-name', 'HoaNien Resort');

    // Room badges
    const badgesEl = document.getElementById('op-room-badges');
    if (badgesEl) {
        const rooms = data.roomCategories || [];
        badgesEl.innerHTML = rooms.length > 0
            ? rooms.map(r => `<span class="op-badge">${r}</span>`).join('')
            : '<span class="op-badge">—</span>';
    }

    // Dates & nights
    setText('op-checkin-date', formatVnDate(data.checkInDate));
    setText('op-checkout-date', formatVnDate(data.checkOutDate));
    const nights = data.nights || nightsBetween(data.checkInDate, data.checkOutDate);
    const guests = (data.totalAdults || 0) + (data.totalChildren || 0);
    setText('op-nights', nights + ' đêm');
    setText('op-guests', guests + ' khách');

    // Price breakdown — dùng giá từ server nếu có (sau khi áp coupon), ngược lại dùng ước tính
    const surchargeFee = appliedCoupon ? 0 : (data.estimatedSurchargeFee || 0);
    const totalPrice   = appliedCoupon ? appliedCoupon.finalPrice  : (data.estimatedTotalPrice    || 0);
    const depositAmount = appliedCoupon ? appliedCoupon.depositAmount : (data.estimatedDepositAmount || 0);
    const discountAmount = appliedCoupon ? appliedCoupon.discountAmount : 0;

    // Giá phòng thuần (không gồm phụ thu, không gồm giảm giá coupon)
    const baseRoomPrice = totalPrice + discountAmount - surchargeFee;

    setText('op-price-label', `Giá phòng (${nights} đêm)`);
    setText('op-price-value', formatCurrency(baseRoomPrice, true));

    // Surcharge row (phụ thu thêm người / độ tuổi)
    const serviceRow = document.getElementById('op-service-row');
    if (surchargeFee > 0) {
        if (serviceRow) serviceRow.style.display = 'flex';
        setText('op-service-value', formatCurrency(surchargeFee, true));
    } else {
        if (serviceRow) serviceRow.style.display = 'none';
        setText('op-service-value', '—');
    }

    // Discount row
    const discountRow = document.getElementById('op-discount-row');
    if (discountRow) discountRow.style.display = discountAmount > 0 ? 'flex' : 'none';
    setText('op-discount-value', discountAmount > 0 ? `- ${formatCurrency(discountAmount, true)}` : '0 ₫');
    const discountEl = document.getElementById('op-discount-value');
    if (discountEl) discountEl.className = 'value' + (discountAmount > 0 ? ' discount' : '');

    setText('op-total-value', formatCurrency(totalPrice, true));
    setText('op-deposit-value', formatCurrency(depositAmount, true));

    // Cập nhật nút thanh toán
    const btnPay = document.getElementById('btnPayNow');
    if (btnPay) {
        btnPay.setAttribute('data-amount', depositAmount);
        if (!btnPay.classList.contains('loading')) {
            btnPay.textContent = `THANH TOÁN ĐẶT CỌC: ${formatCurrency(depositAmount, true)}`;
        }
    }
}

// ── Coupon ────────────────────────────────────────────────────────────────────

async function applyCoupon() {
    const code = document.getElementById('couponInput')?.value?.trim();
    if (!code) { showCouponMsg('Vui lòng nhập mã coupon.', false); return; }
    if (!cartData) { showCouponMsg('Không tìm thấy thông tin giỏ phòng.', false); return; }

    const btn = document.getElementById('btnApplyCoupon');
    btn.disabled = true;
    btn.textContent = '...';

    try {
        const totalPrice = cartData.estimatedTotalPrice || 0;
        const res = await fetch(
            `/api/bookings/promo/validate?code=${encodeURIComponent(code)}&amount=${totalPrice}`,
            { method: 'GET' }
        );
        const data = await res.json();

        if (res.ok && data.success) {
            appliedCoupon = {
                code,
                discountAmount: data.discountAmount,
                finalPrice: data.newAmount,
                depositAmount: Math.round(data.newAmount * 0.3)
            };
            showCouponMsg(`Áp dụng thành công! Giảm ${formatCurrency(data.discountAmount, true)}`, true);
            renderOrderSummary(cartData);
        } else {
            appliedCoupon = null;
            showCouponMsg(data.message || 'Mã coupon không hợp lệ.', false);
            renderOrderSummary(cartData);
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
    const required = [
        { id: 'fieldFullName', label: 'Họ và tên' },
        { id: 'fieldPhone',    label: 'Số điện thoại' },
        { id: 'fieldEmail',    label: 'Email' },
        { id: 'fieldDob',      label: 'Ngày sinh' }
    ];
    for (const f of required) {
        const el = document.getElementById(f.id);
        if (!el || !el.value.trim()) {
            showToast(`Vui lòng điền ${f.label}`, 'error');
            el?.focus();
            return false;
        }
    }

    const email = document.getElementById('fieldEmail')?.value?.trim();
    if (email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
        showToast('Email không hợp lệ.', 'error');
        return false;
    }

    const dobStr = document.getElementById('fieldDob')?.value;
    if (dobStr) {
        const age = Math.abs(new Date(Date.now() - new Date(dobStr).getTime()).getUTCFullYear() - 1970);
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
    if (!cartData) {
        showToast('Giỏ phòng đã hết hạn. Vui lòng chọn lại.', 'error');
        setTimeout(() => window.location.replace('/booking'), 2000);
        return;
    }

    const btn = document.getElementById('btnPayNow');
    btn.disabled = true;
    btn.classList.add('loading');
    btn.textContent = 'ĐANG XỬ LÝ...';

    const methodEl = document.querySelector('input[name="paymentMethod"]:checked');
    const payload = {
        // Từ sessionStorage (giỏ phòng)
        roomSelections:  cartData.roomSelections,
        checkInDate:     cartData.checkInDate,
        checkOutDate:    cartData.checkOutDate,
        promotionCode:   appliedCoupon?.code || null,
        // Từ form (thông tin khách)
        fullName:    document.getElementById('fieldFullName')?.value?.trim(),
        phone:       document.getElementById('fieldPhone')?.value?.trim(),
        email:       document.getElementById('fieldEmail')?.value?.trim(),
        dateOfBirth: document.getElementById('fieldDob')?.value?.trim() || null,
        address:     document.getElementById('fieldAddress')?.value?.trim() || null,
        notes:       document.getElementById('fieldNotes')?.value?.trim() || null,
        paymentMethod: methodEl ? methodEl.value : 'VNPAY'
    };

    try {
        const res = await fetch('/api/bookings', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const data = await res.json();

        if (res.ok && data.bookingId) {
            // Lưu thông tin khách để pre-fill lần sau
            localStorage.setItem('hoanien_customer_info', JSON.stringify({
                fullName:    payload.fullName,
                phone:       payload.phone,
                email:       payload.email,
                dateOfBirth: payload.dateOfBirth
            }));

            // Giỏ phòng đã dùng xong
            sessionStorage.removeItem('hoanien_room_cart');

            if (data.paymentUrl) {
                // Đánh dấu bookingId để pagehide biết cần cancel nếu user back từ VNPay
                sessionStorage.setItem('hoanien_pending_booking_id', data.bookingId);
                isPaymentSubmitted = true;
                window.location.href = data.paymentUrl;
            } else {
                showToast('Đặt phòng thành công!', 'success');
                setTimeout(() => window.location.href = '/profile', 1500);
            }
        } else {
            showToast(data.message || 'Có lỗi xảy ra. Vui lòng thử lại.', 'error');
            resetPayButton(btn);
        }
    } catch (e) {
        console.error('submitPayment error:', e);
        showToast('Lỗi kết nối Server! Vui lòng thử lại.', 'error');
        resetPayButton(btn);
    }
}

function resetPayButton(btn) {
    btn.disabled = false;
    btn.classList.remove('loading');
    const depAmount = btn.getAttribute('data-amount');
    btn.textContent = depAmount ? `THANH TOÁN ĐẶT CỌC: ${formatCurrency(depAmount, true)}` : 'THANH TOÁN NGAY';
    const cb = document.getElementById('termsCheck');
    if (cb?.checked) btn.classList.add('active');
}

// ── Init ─────────────────────────────────────────────────────────────────────

document.addEventListener('DOMContentLoaded', () => {
    // Bước 1: Load giỏ phòng từ sessionStorage
    if (!loadCartFromSession()) return;

    // Bước 2: Render tóm tắt đơn hàng
    renderOrderSummary(cartData);

    // Bước 3: Pre-fill thông tin khách từ lần trước
    prefillCustomerInfo();

    // Bước 4: Coupon — Enter key shortcut
    document.getElementById('couponInput')?.addEventListener('keydown', e => {
        if (e.key === 'Enter') { e.preventDefault(); applyCoupon(); }
    });

    // Bước 5: Logo click về trang chủ
    document.getElementById('paymentPageLogo')?.addEventListener('click', () => {
        window.location.href = '/';
    });
});

// ── Handle BFCache & Back từ VNPay ───────────────────────────────────────────

// Khi user back từ VNPay → pageshow với event.persisted = true
window.addEventListener('pageshow', (event) => {
    if (event.persisted) {
        // Trang được khôi phục từ BFCache (bộ nhớ đệm trình duyệt)
        // Reset flag để cho phép pagehide cancel đơn nếu cần
        isPaymentSubmitted = false;
    }
});

// Khi user rời trang sau khi đã submit → cancel Pending_Payment để giải phóng phòng
window.addEventListener('pagehide', () => {
    const pendingId = sessionStorage.getItem('hoanien_pending_booking_id');
    if (pendingId && !isPaymentSubmitted) {
        // Đơn đã được tạo (Pending_Payment) nhưng user hủy giữa chừng
        fetch(`/api/bookings/${pendingId}/cancel`, { method: 'POST', keepalive: true });
        sessionStorage.removeItem('hoanien_pending_booking_id');
    }
});
