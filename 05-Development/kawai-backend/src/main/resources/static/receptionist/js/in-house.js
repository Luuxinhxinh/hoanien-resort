// ============================================================
// IN-HOUSE MODAL — Xem chi tiết đơn lưu trú
// ============================================================
function openInHouseModal(bookingId) {
    document.getElementById('modalInHouseGuestName').innerText = 'Đang tải...';
    document.getElementById('modalInHousePhone').innerText = '';
    document.getElementById('modalInHouseRooms').innerText = '';
    document.getElementById('modalInHouseDependents').innerHTML = '<div style="text-align: center; padding: 20px;"><i class="fa-solid fa-spinner fa-spin text-primary" style="font-size: 24px;"></i></div>';
    document.getElementById('inHouseModal').style.display = 'flex';

    fetch(`/receptionist/in-house/detail/${bookingId}`)
        .then(res => {
            if (!res.ok) throw new Error('Network response was not ok');
            return res.json();
        })
        .then(data => {
            document.getElementById('modalInHouseGuestName').innerText = data.guestName || 'Unknown';
            document.getElementById('modalInHousePhone').innerText = data.phone ? 'SĐT: ' + data.phone : '';
            document.getElementById('modalInHouseRooms').innerText = data.roomSummary || 'N/A';

            let html = '<ul style="list-style: none; padding: 0; margin: 0; color: #475569; font-size: 15px;">';
            if (data.guests && data.guests.length > 0) {
                let hasDependent = false;
                data.guests.forEach((dep, idx) => {
                    if (dep.type === 'Main Guest') return;
                    hasDependent = true;
                    let borderBottom = idx < data.guests.length - 1 ? 'border-bottom: 1px dashed #cbd5e1;' : '';
                    html += `<li style="padding: 16px 0; display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 12px; ${borderBottom}">`;

                    html += `<div style="display: flex; align-items: center; gap: 12px; flex-wrap: wrap;">`;
                    html += `  <div style="display: flex; align-items: center; gap: 8px;">`;
                    html += `    <span style="font-weight: 600; color: #334155; font-size: 16px;">${dep.name}</span>`;
                    if (dep.type === 'Customer (Được nâng cấp)') {
                        html += ` <span style="background-color: #d1fae5; color: #065f46; padding: 2px 6px; border-radius: 4px; font-size: 11px; font-weight: bold;">Customer</span>`;
                    }
                    html += `  </div>`;

                    let subInfo = [];
                    if (dep.dob)  subInfo.push(`Sinh: <span style="color: #334155; font-weight: 500;">${dep.dob}</span>`);
                    if (dep.cccd) subInfo.push(`CCCD/Passport: <span style="color: #334155; font-weight: 500;">${dep.cccd}</span>`);
                    if (subInfo.length > 0) {
                        html += `<div style="font-size: 14px; color: #64748b; display: flex; gap: 8px; align-items: center;"><span style="color: #cbd5e1;">|</span>${subInfo.join(' <span style="color: #cbd5e1;">|</span> ')}</div>`;
                    }
                    html += `</div>`;

                    html += `<div style="display: flex; align-items: center; gap: 8px;">`;
                    if (dep.isPrimaryContact) {
                        html += ` <span style="background-color: #fef08a; color: #854d0e; padding: 4px 8px; border-radius: 4px; font-size: 12px; font-weight: bold;">Đứng đầu phòng ${dep.roomNumber || ''}</span>`;
                        if (dep.dependentId != null) {
                            html += ` <button onclick="upgradeDependent(${dep.dependentId}, this)" style="background-color: #3b82f6; color: white; border: none; padding: 4px 10px; border-radius: 4px; font-size: 12px; cursor: pointer; transition: background 0.2s;" onmouseover="this.style.backgroundColor='#2563eb'" onmouseout="this.style.backgroundColor='#3b82f6'">Nâng cấp Customer</button>`;
                        }
                    }
                    html += `</div>`;
                    html += `</li>`;
                });
                if (!hasDependent) {
                    html += `<li>Không có người đi kèm</li>`;
                }
            } else {
                html += `<li>Không có người đi kèm</li>`;
            }
            html += '</ul>';
            document.getElementById('modalInHouseDependents').innerHTML = html;
        })
        .catch(err => {
            console.error('Error fetching details:', err);
            document.getElementById('modalInHouseGuestName').innerText = 'Lỗi tải dữ liệu';
            document.getElementById('modalInHouseDependents').innerHTML = '<div style="color: red;">Không thể tải thông tin. Vui lòng thử lại sau.</div>';
        });
}

function closeInHouseModal() {
    document.getElementById('inHouseModal').style.display = 'none';
}

// ============================================================
// NÂNG CẤP DEPENDENT → CUSTOMER
// ============================================================
function upgradeDependent(dependentId, btnElement) {
    if (!confirm('Bạn có chắc chắn muốn nâng cấp người này thành Khách hàng chính không? (Sẽ được cấp tài khoản)')) return;

    btnElement.disabled = true;
    btnElement.innerText = 'Đang nâng cấp...';

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
                btnElement.innerText = 'Nâng cấp Customer';
            }
        })
        .catch(() => {
            alert('Lỗi mạng hoặc server');
            btnElement.disabled = false;
            btnElement.innerText = 'Nâng cấp Customer';
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

    const priceInfo  = document.getElementById('transferPriceInfo');
    const roomSel    = document.getElementById('transferNewRoomSelect');
    const detailInput = document.getElementById('transferDetailIdInput');
    detailInput.value = _transferSelectedDetailId;

    let diffText = '';
    if (priceDiff > 0)      diffText = `Phụ phí nâng hạng: <b>+${Number(priceDiff).toLocaleString('vi-VN')}₫/đêm</b> (tính trên số đêm còn lại)`;
    else if (priceDiff < 0) diffText = `Giảm phí: <b>${Number(priceDiff).toLocaleString('vi-VN')}₫/đêm</b>`;
    else                    diffText = `Cùng mức giá, không phát sinh phụ phí.`;
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

