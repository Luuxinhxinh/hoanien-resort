
// ============================================================
// NÂNG CẤP DEPENDENT → CUSTOMER
// ============================================================
function upgradeDependent(dependentId, btnElement) {
    if (!confirm('Bạn có chắc chắn muốn nâng cấp người này thành Khách hàng chính không? (Sẽ được cấp tài khoản)')) return;

    btnElement.disabled = true;

    fetch(`/receptionist/checkin/upgrade-dependent/${dependentId}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' }
    })
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                alert(data.message);
                location.reload();
            } else {
                alert('Lỗi: ' + data.message);
                btnElement.disabled = false;
            }
        })
        .catch(() => {
            alert('Lỗi mạng hoặc server');
            btnElement.disabled = false;
        });
}

// ============================================================
// TRANSFER ROOM MODAL — Đổi hạng phòng (3 bước)
// ============================================================
let _transferBookingId = null;
let _transferSelectedDetailId = null;
let _transferSelectedCategoryName = null;

function openTransferRoomModal(bookingId) {
    _transferBookingId = bookingId;
    _transferSelectedDetailId = null;
    _transferSelectedCategoryName = null;

    document.getElementById('transferRoomModal').style.display = 'flex';
    transferGoStep(1);

    const sel = document.getElementById('transferCurrentRoomSelect');
    sel.innerHTML = '<option value="">-- Đang tải... --</option>';

    fetch('/receptionist/in-house/detail/' + bookingId)
        .then(res => res.json())
        .then(data => {
            if (!data.roomDetails || data.roomDetails.length === 0) {
                sel.innerHTML = '<option value="">-- Khách chưa nhận phòng --</option>';
                return;
            }
            let html = '<option value="">-- Chọn phòng muốn đổi hạng --</option>';
            data.roomDetails.forEach(rd => {
                if (rd.status && rd.status.toUpperCase() === 'CHECKED_IN') {
                    html += `<option value="${rd.detailId}">${rd.roomNumber} — ${rd.category}</option>`;
                }
            });
            sel.innerHTML = html;
        })
        .catch(() => {
            sel.innerHTML = '<option value="">-- Lỗi tải dữ liệu --</option>';
        });
}

function transferStep1Next() {
    const sel = document.getElementById('transferCurrentRoomSelect');
    const detailId = sel.value;
    if (!detailId) {
        alert('Vui lòng chọn phòng hiện tại của khách.');
        return;
    }
    _transferSelectedDetailId = detailId;

    const catList = document.getElementById('transferCategoryList');
    catList.innerHTML = '<p style="color:#64748b; font-size:13px;">Đang tải danh sách hạng phòng...</p>';
    transferGoStep(2);

    fetch('/receptionist/in-house/categories-available?excludeDetailId=' + detailId)
        .then(res => res.json())
        .then(cats => {
            let html = '';
            cats.forEach(cat => {
                const hasVacant = cat.vacantCount > 0;
                const diff = cat.priceDiff;
                const isCurrent = cat.isCurrent;
                const isClickable = hasVacant; // Cho phép đổi sang cùng hạng nếu còn phòng trống

                // Determine variant colors
                let accentColor, accentBg, badgeBg, badgeColor, borderColor, cardBg;
                if (isCurrent) {
                    accentColor = '#946f38'; accentBg = '#fdf8f1';
                    badgeBg = '#946f38'; badgeColor = '#fff';
                    borderColor = '#946f38'; cardBg = '#fdf8f1';
                } else if (!hasVacant) {
                    accentColor = '#eb5757'; accentBg = '#fff5f5';
                    badgeBg = '#fdeeee'; badgeColor = '#eb5757';
                    borderColor = '#f5b5b5'; cardBg = '#fff8f8';
                } else if (diff > 0) {
                    accentColor = '#f2994a'; accentBg = '#fff9f3';
                    badgeBg = '#fdf5eb'; badgeColor = '#c0692a';
                    borderColor = '#f5cc99'; cardBg = '#fff9f3';
                } else if (diff < 0) {
                    accentColor = '#27ae60'; accentBg = '#f3fdf7';
                    badgeBg = '#eaf7ef'; badgeColor = '#1a7a43';
                    borderColor = '#a8e6c0'; cardBg = '#f3fdf7';
                } else {
                    accentColor = '#7b61ff'; accentBg = '#f4f2ff';
                    badgeBg = '#f2efff'; badgeColor = '#5a45cc';
                    borderColor = '#c4b9f5'; cardBg = '#f4f2ff';
                }

                let priceLine = diff > 0
                    ? `<span style="color:${accentColor}; font-weight:700;">+${Number(diff).toLocaleString('vi-VN')}₫/đêm</span> <span style="font-size:11px; color:#8b8b8b;">(Nâng hạng — phụ phí được tính)</span>`
                    : diff < 0
                        ? `<span style="color:${accentColor}; font-weight:700;">${Number(diff).toLocaleString('vi-VN')}₫/đêm</span> <span style="font-size:11px; color:#8b8b8b;">(Hạ hạng)</span>`
                        : `<span style="color:#8b8b8b; font-weight:500;">Bằng hạng hiện tại</span>`;

                let vacancyBadge = hasVacant
                    ? `<span style="display:inline-flex; align-items:center; gap:4px; background:#eaf7ef; color:#27ae60; border:1px solid #a8e6c0; padding:4px 10px; border-radius:20px; font-size:12px; font-weight:600;">✔ ${cat.vacantCount} phòng trống</span>`
                    : `<span style="display:inline-flex; align-items:center; gap:4px; background:#fdeeee; color:#eb5757; border:1px solid #f5b5b5; padding:4px 10px; border-radius:20px; font-size:12px; font-weight:600;">✘ Hết phòng</span>`;

                let currentBadge = isCurrent
                    ? `<span style="background:#946f38; color:#fff; padding:3px 10px; border-radius:20px; font-size:11px; font-weight:700; margin-left:8px;">Hiện tại</span>`
                    : '';

                let categoryNameHtml = '';
                if (!hasVacant) {
                    // Strikethrough for sold-out categories
                    categoryNameHtml = `<span style="font-weight:700; font-size:16px; color:#aaa; text-decoration:line-through;">${cat.categoryName}</span>`;
                } else {
                    categoryNameHtml = `<span style="font-weight:700; font-size:16px; color:#1a1a1a;">${cat.categoryName}</span>`;
                }

                let subtitleHtml = '';
                if (isCurrent && hasVacant) {
                    subtitleHtml = `<p style="margin:6px 0 0; font-size:12px; color:#946f38; font-style:italic;">↻ Đổi sang phòng khác cùng hạng này (không phụ phí)</p>`;
                } else if (!hasVacant) {
                    subtitleHtml = `<p style="margin:6px 0 0; font-size:12px; color:#eb5757; font-style:italic;">✘ Không còn phòng trống — không thể đổi sang hạng này</p>`;
                }

                const safeDiff = Number(diff) || 0; // Ép kiểu tránh lỗi BigDecimal notation từ Java (VD: 0E+0)
                const clickHandler = isClickable
                    ? `transferSelectCategory('${cat.categoryName.replace(/'/g, "\\'")}', ${safeDiff})`
                    : '';

                html += `
                <div onclick="${clickHandler}"
                    style="border:2px solid ${borderColor}; border-radius:12px; padding:18px 20px;
                           background:${cardBg}; cursor:${isClickable ? 'pointer' : 'not-allowed'};
                           opacity:${isClickable ? '1' : '0.65'};
                           transition: all 0.2s ease; box-shadow: 0 2px 6px rgba(0,0,0,0.06);"
                    onmouseover="${isClickable ? `this.style.boxShadow='0 6px 18px rgba(0,0,0,0.12)'; this.style.transform='translateY(-2px)'` : ''}"
                    onmouseout="${isClickable ? `this.style.boxShadow='0 2px 6px rgba(0,0,0,0.06)'; this.style.transform='translateY(0)'` : ''}">
                    <div style="display:flex; justify-content:space-between; align-items:flex-start; margin-bottom:10px;">
                        <div>
                            <div style="display:flex; align-items:center; gap:8px;">
                                ${categoryNameHtml}
                                ${currentBadge}
                            </div>
                            ${subtitleHtml}
                        </div>
                        ${vacancyBadge}
                    </div>
                    <div style="display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:8px;">
                        <span style="font-size:13px; color:${!hasVacant ? '#aaa' : '#5a5a5a'};">
                            Giá cơ bản: <b style="color:${!hasVacant ? '#aaa' : '#1a1a1a'}; font-size:15px; ${!hasVacant ? 'text-decoration:line-through;' : ''}">${Number(cat.basePrice).toLocaleString('vi-VN')}₫</b><span style="color:#8b8b8b;">/đêm</span>
                        </span>
                        <span style="font-size:13px; color:#5a5a5a;">${!hasVacant ? '<span style="color:#aaa;">—</span>' : `Chênh lệch: ${priceLine}`}</span>
                    </div>
                </div>`;
            });
            catList.innerHTML = html;
        })
        .catch(() => {
            catList.innerHTML = '<p style="color:#ef4444;">Lỗi tải danh sách hạng phòng.</p>';
        });
}

function transferSelectCategory(categoryName, priceDiff) {
    _transferSelectedCategoryName = categoryName;

    const priceInfo = document.getElementById('transferPriceInfo');
    const roomSel = document.getElementById('transferNewRoomSelect');
    const detailInput = document.getElementById('transferDetailIdInput');
    detailInput.value = _transferSelectedDetailId;

    let diffText = '';
    if (priceDiff > 0) diffText = `Phụ phí nâng hạng: <b>+${Number(priceDiff).toLocaleString('vi-VN')}₫/đêm</b> (tính trên số đêm còn lại)`;
    else if (priceDiff < 0) diffText = `Giảm phí: <b>${Number(priceDiff).toLocaleString('vi-VN')}₫/đêm</b>`;
    else diffText = `Cùng mức giá, không phát sinh phụ phí.`;
    priceInfo.innerHTML = `Hạng đã chọn: <b>${categoryName}</b> &nbsp;|&nbsp; ${diffText}`;

    roomSel.innerHTML = '<option value="">-- Đang tải phòng... --</option>';
    transferGoStep(3);

    fetch('/receptionist/in-house/rooms-by-category?categoryName=' + encodeURIComponent(categoryName))
        .then(res => res.json())
        .then(rooms => {
            if (!rooms || rooms.length === 0) {
                roomSel.innerHTML = '<option value="">-- Không còn phòng trống --</option>';
                return;
            }
            let html = '<option value="">-- Chọn phòng --</option>';
            rooms.forEach(r => {
                html += `<option value="${r.id}">Phòng ${r.roomNumber}</option>`;
            });
            roomSel.innerHTML = html;
        })
        .catch(() => {
            roomSel.innerHTML = '<option value="">-- Lỗi tải phòng --</option>';
        });
}

function transferGoStep(step) {
    document.getElementById('transferStep1').style.display = step === 1 ? 'flex' : 'none';
    document.getElementById('transferStep2').style.display = step === 2 ? 'flex' : 'none';
    document.getElementById('transferStep3').style.display = step === 3 ? 'flex' : 'none';
}

function closeTransferRoomModal() {
    document.getElementById('transferRoomModal').style.display = 'none';
}

// ============================================================
// AJAX SUBMIT ĐỔI PHÒNG + TOAST NOTIFICATION
// ============================================================
function submitTransferRoom(event) {
    event.preventDefault(); // Chặn submit form truyền thống

    const detailId = document.getElementById('transferDetailIdInput').value;
    const newRoomId = document.getElementById('transferNewRoomSelect').value;

    if (!detailId || !newRoomId) {
        showToast('Vui lòng chọn phòng trước khi xác nhận.', 'error');
        return;
    }

    const submitBtn = event.target.querySelector('button[type="submit"]');
    const originalText = submitBtn.innerText;
    submitBtn.disabled = true;
    submitBtn.innerText = '⏳ Đang xử lý...';

    // Gửi dưới dạng form-urlencoded (khớp với @RequestParam của controller)
    const body = new URLSearchParams({ bookingDetailId: detailId, newRoomId: newRoomId });

    fetch('/receptionist/in-house/transfer-room', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: body.toString()
    })
        .then(async res => {
            let data = {};
            try {
                data = await res.json();
            } catch (e) {
                throw new Error('Lỗi phản hồi từ server (không phải JSON)');
            }

            if (res.ok && data.success) {
                closeTransferRoomModal();
                showToast(data.message || '✔ Đổi phòng thành công!', 'success');
                setTimeout(() => location.reload(), 1500);
            } else {
                throw new Error(data.message || 'Lỗi xử lý đổi phòng');
            }
        })
        .catch(err => {
            console.error('Transfer room error:', err);
            showToast('✘ Đổi phòng thất bại: ' + (err.message || 'Lỗi hệ thống'), 'error');
            submitBtn.disabled = false;
            submitBtn.innerText = originalText;
        });
}

function showToast(message, type = 'success') {
    const container = document.getElementById('toastContainer');
    const toast = document.createElement('div');

    const isSuccess = type === 'success';
    toast.style.cssText = `
        background: ${isSuccess ? '#fdf8f1' : '#fff5f5'};
        border: 2px solid ${isSuccess ? '#946f38' : '#eb5757'};
        border-left: 5px solid ${isSuccess ? '#946f38' : '#eb5757'};
        color: ${isSuccess ? '#5a4020' : '#7a1a1a'};
        padding: 14px 20px;
        border-radius: 10px;
        font-size: 14px;
        font-weight: 600;
        font-family: 'Inter', sans-serif;
        box-shadow: 0 8px 24px rgba(0,0,0,0.12);
        max-width: 380px;
        animation: toastSlideIn 0.3s ease-out;
        opacity: 1;
        transition: opacity 0.4s;
    `;
    toast.innerText = message;
    container.appendChild(toast);

    // Auto-dismiss sau 4 giây
    setTimeout(() => {
        toast.style.opacity = '0';
        setTimeout(() => toast.remove(), 400);
    }, 4000);
}

// ===== Hiện thông báo khi VNPay redirect về trang In-House =====
document.addEventListener('DOMContentLoaded', function () {
    const urlParams = new URLSearchParams(window.location.search);
    const paymentStatus = urlParams.get('payment');
    if (paymentStatus === 'success') {
        showToast('✅ Nạp tiền nâng hạn mức thành công!', 'success');
        const cleanUrl = window.location.pathname;
        window.history.replaceState({}, document.title, cleanUrl);
    } else if (paymentStatus === 'failed') {
        showToast('❌ Thanh toán VNPay thất bại hoặc đã hủy!', 'error');
        const cleanUrl = window.location.pathname;
        window.history.replaceState({}, document.title, cleanUrl);
    }
});



// ===== Credit Deposit Logic =====
let cdCurrentDetailId = null;
let cdCurrentAvailable = 0;
let cdRoomsData = [];

function formatVnd(num) {
    if (num === undefined || num === null) return '0 ₫';
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(Number(num));
}

function openCreditDepositModal(bookingId) {
    cdCurrentDetailId = null;
    cdCurrentAvailable = 0;
    cdRoomsData = [];
    document.getElementById('cdRoomSelect').innerHTML = '<option value="">-- Đang tải... --</option>';
    document.getElementById('cdCreditInfo').style.display = 'none';
    document.getElementById('cdInputSection').style.display = 'none';
    document.getElementById('cdAmount').value = '';
    document.getElementById('cdSubmitBtn').disabled = true;
    document.getElementById('cdSubmitBtn').style.background = '#d1d5db';
    document.getElementById('cdSubmitBtn').style.color = '#9ca3af';
    document.getElementById('cdSubmitBtn').style.cursor = 'not-allowed';
    document.querySelector('input[name="cdMethod"][value="CASH"]').checked = true;
    selectCdMethod();
    document.getElementById('creditDepositModal').style.display = 'flex';

    // Gọi API lấy thông tin hạn mức từng phòng
    fetch(`/api/folios/booking/${bookingId}/credit-info`)
        .then(r => {
            if (!r.ok) {
                return r.text().then(txt => { throw new Error(`HTTP ${r.status}: ${txt}`); });
            }
            return r.json();
        })
        .then(data => {
            if (!data.success) { alert(data.message || 'Lỗi tải thông tin phòng'); return; }
            cdRoomsData = data.rooms;
            const sel = document.getElementById('cdRoomSelect');
            sel.innerHTML = '<option value="">-- Chọn phòng cần nâng hạn mức --</option>';
            data.rooms.forEach(r => {
                const opt = document.createElement('option');
                opt.value = r.detailId;
                opt.text = `Phòng ${r.roomNumber}  (Khả dụng: ${formatVnd(r.available)})`;
                sel.appendChild(opt);
            });
            // Nếu chỉ có 1 phòng, tự động chọn luôn
            if (data.rooms.length === 1) {
                sel.value = data.rooms[0].detailId;
                try { onCdRoomChange(); } catch (e) { console.error('[onCdRoomChange]', e); }
            }
        })
        .catch(err => {
            console.error('[CreditDeposit] Fetch error:', err);
            alert('Lỗi tải dữ liệu: ' + err.message);
        });
}

function closeCreditDepositModal() {
    document.getElementById('creditDepositModal').style.display = 'none';
}

function onCdRoomChange() {
    const sel = document.getElementById('cdRoomSelect');
    const detailId = sel.value;
    if (!detailId) {
        document.getElementById('cdCreditInfo').style.display = 'none';
        document.getElementById('cdInputSection').style.display = 'none';
        return;
    }
    cdCurrentDetailId = detailId;
    const room = cdRoomsData.find(r => r.detailId == detailId);
    if (!room) return;
    cdCurrentAvailable = Number(room.available);
    document.getElementById('cdLimitDisplay').innerText = formatVnd(room.creditLimit);
    document.getElementById('cdDepositedDisplay').innerText = '+' + formatVnd(room.deposited);
    document.getElementById('cdChargedDisplay').innerText = formatVnd(room.charged);
    document.getElementById('cdCreditInfo').style.display = 'block';
    document.getElementById('cdInputSection').style.display = 'block';
    document.getElementById('cdAmount').value = '';
    document.getElementById('cdSubmitBtn').disabled = true;
    document.getElementById('cdSubmitBtn').style.background = '#d1d5db';
    document.getElementById('cdSubmitBtn').style.color = '#9ca3af';
    document.getElementById('cdSubmitBtn').style.cursor = 'not-allowed';
}

function onCdAmountChange() {
    const amount = parseFloat(document.getElementById('cdAmount').value);
    const submitBtn = document.getElementById('cdSubmitBtn');
    if (amount > 0) {
        submitBtn.disabled = false;
        submitBtn.style.background = 'linear-gradient(135deg, #10b981, #059669)';
        submitBtn.style.color = 'white';
        submitBtn.style.cursor = 'pointer';
    } else {
        submitBtn.disabled = true;
        submitBtn.style.background = '#d1d5db';
        submitBtn.style.color = '#9ca3af';
        submitBtn.style.cursor = 'not-allowed';
    }
}

function selectCdMethod() {
    const methods = ['CASH', 'VNPAY'];
    methods.forEach(m => {
        const lbl = document.getElementById(`cdMethod${m === 'CASH' ? 'Cash' : 'Vnpay'}Label`);
        const checked = document.querySelector(`input[name="cdMethod"][value="${m}"]`).checked;
        lbl.style.borderColor = checked ? (m === 'CASH' ? '#10b981' : '#1d4ed8') : '#e5e7eb';
        lbl.style.background = checked ? (m === 'CASH' ? '#f0fdf4' : '#eff6ff') : 'white';
    });
}

function submitCreditDeposit() {
    const amount = parseFloat(document.getElementById('cdAmount').value);
    const method = document.querySelector('input[name="cdMethod"]:checked').value;
    if (!cdCurrentDetailId || !amount || amount <= 0) {
        alert('Vui lòng chọn phòng và nhập số tiền hợp lệ!');
        return;
    }

    const btn = document.getElementById('cdSubmitBtn');
    btn.disabled = true;
    btn.innerText = 'Đang xử lý...';

    fetch(`/api/folios/${cdCurrentDetailId}/deposit`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ amount: amount, method: method })
    })
        .then(r => r.json())
        .then(data => {
            if (data.success) {
                closeCreditDepositModal();
                if (data.paymentUrl) {
                    window.open(data.paymentUrl, '_blank');
                    alert('Đang mở cổng thanh toán VNPay. Tải lại trang sau khi khách hoàn tất!');
                } else {
                    alert('✅ ' + data.message);
                    window.location.reload();
                }
            } else {
                alert('❌ ' + (data.message || 'Lỗi khi nạp tiền'));
                btn.disabled = false;
                btn.innerText = 'Xác nhận nạp tiền';
                btn.style.background = 'linear-gradient(135deg, #10b981, #059669)';
                btn.style.color = 'white';
                btn.style.cursor = 'pointer';
            }
        })
        .catch(() => {
            alert('Lỗi kết nối máy chủ');
            btn.disabled = false;
            btn.innerText = 'Xác nhận nạp tiền';
        });
}

function switchTab(tab) {
    if (tab === 'inhouse') {
        document.getElementById('tabContentInHouse').style.display = 'block';
        document.getElementById('tabContentCancelled').style.display = 'none';
        document.getElementById('btnTabInHouse').className = 'btn btn-primary';
        document.getElementById('btnTabInHouse').style.background = '';
        document.getElementById('btnTabInHouse').style.color = '';
        document.getElementById('btnTabInHouse').style.border = '';
        document.getElementById('btnTabCancelled').className = 'btn btn-outline';
        document.getElementById('btnTabCancelled').style.background = 'white';
        document.getElementById('btnTabCancelled').style.color = '#475569';
        document.getElementById('btnTabCancelled').style.border = '1px solid #cbd5e1';
    } else if (tab === 'cancelled') {
        document.getElementById('tabContentInHouse').style.display = 'none';
        document.getElementById('tabContentCancelled').style.display = 'block';
        document.getElementById('btnTabCancelled').className = 'btn btn-primary';
        document.getElementById('btnTabCancelled').style.background = '';
        document.getElementById('btnTabCancelled').style.color = '';
        document.getElementById('btnTabCancelled').style.border = '';
        document.getElementById('btnTabInHouse').className = 'btn btn-outline';
        document.getElementById('btnTabInHouse').style.background = 'white';
        document.getElementById('btnTabInHouse').style.color = '#475569';
        document.getElementById('btnTabInHouse').style.border = '1px solid #cbd5e1';
    }
}
