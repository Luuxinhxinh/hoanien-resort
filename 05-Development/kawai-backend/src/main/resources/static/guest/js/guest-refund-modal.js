
let currentGuestRefundType = 'FOOD';
let currentTourRefundAmount = 0;
function openGuestRefundModal(orderId) {
    document.getElementById('guestRefundOrderId').value = orderId;
    currentGuestRefundType = 'FOOD';
    document.getElementById('guestRefundModal').style.display = 'flex';
    _hideTourRefundBanner();
}

function openRefundModal(orderId, type, isRoomRefundable, hasAttachedTours) {
    document.getElementById('guestRefundOrderId').value = orderId;
    currentGuestRefundType = type;
    _hideTourRefundBanner();
    // Reset title/button về mặc định (phòng trường hợp trước đó mở modal tour)
    const title = document.getElementById('guestRefundModalTitle');
    const desc = document.getElementById('guestRefundModalDesc');
    const btn = document.getElementById('btn-submit-guest-refund');
    
    if (type === 'ROOM' && isRoomRefundable === false && hasAttachedTours === true) {
        if (title) title.innerText = 'Hoàn tiền Tour đi kèm (50%)';
        if (desc) desc.innerText = 'Lưu ý: Phòng của bạn đã quá hạn hoàn tiền, nhưng Tour đi kèm vẫn đủ điều kiện hoàn 50%. Vui lòng cung cấp tài khoản ngân hàng.';
    } else if (type === 'ROOM' && isRoomRefundable === true && hasAttachedTours === true) {
        if (title) title.innerText = 'Hoàn tiền Phòng và Tour đi kèm';
        if (desc) desc.innerText = 'Đơn đặt phòng và Tour đi kèm của bạn đều đủ điều kiện hoàn tiền. Vui lòng cung cấp tài khoản ngân hàng để Kế toán tiến hành hoàn tổng tiền.';
    } else {
        if (title) title.innerText = 'Yêu cầu thông tin hoàn tiền';
        if (desc) desc.innerText = 'Đơn hàng này đã được thanh toán trực tuyến. Vui lòng cung cấp thông tin tài khoản ngân hàng để Kế toán tiến hành hoàn tiền.';
    }
    
    if (btn) btn.innerText = 'Gửi Yêu cầu Hủy đơn';
    document.getElementById('guestRefundModal').style.display = 'flex';
}

// Hàm riêng cho hủy TOUR có hoàn tiền 50%
function openTourRefundModal(bookingId, refundAmount) {
    document.getElementById('guestRefundOrderId').value = bookingId;
    currentGuestRefundType = 'TOUR';
    currentTourRefundAmount = refundAmount || 0;
    _showTourRefundBanner(refundAmount);
    document.getElementById('guestRefundModal').style.display = 'flex';
}

function _showTourRefundBanner(refundAmount, tourName) {
    let banner = document.getElementById('guestRefundTourBanner');
    if (!banner) return;
    const fmt = new Intl.NumberFormat('vi-VN').format(Math.round(refundAmount || 0));
    banner.innerHTML = `
        <div style="background:linear-gradient(135deg,#fff7ed,#fef3c7);border:1px solid #f59e0b;border-radius:10px;padding:14px 16px;margin-bottom:14px;">
            <div style="display:flex;align-items:center;gap:8px;margin-bottom:6px;">
                <span style="font-size:18px;">&#128176;</span>
                <strong style="color:#92400e;font-size:0.9rem;">Số tiền được hoàn (50%)</strong>
            </div>
            <div style="font-size:1.5rem;font-weight:700;color:#b45309;font-family:monospace;">${fmt} ₫</div>
            <div style="font-size:0.78rem;color:#78350f;margin-top:4px;">Số tiền sẽ được chuyển vào tài khoản ngân hàng trong vòng 3–7 ngày làm việc.</div>
        </div>`;
    banner.style.display = 'block';
}

function _hideTourRefundBanner() {
    let banner = document.getElementById('guestRefundTourBanner');
    if (banner) banner.style.display = 'none';
}



function closeGuestRefundModal() {
    document.getElementById('guestRefundModal').style.display = 'none';
    document.getElementById('guestRefundForm').reset();
}

function submitGuestRefundForm(event) {
    event.preventDefault();

    const orderId = document.getElementById('guestRefundOrderId').value;
    const btn = document.getElementById('btn-submit-guest-refund');

    const payload = {
        bankName: document.getElementById('guestRefundBankName').value,
        accountNumber: document.getElementById('guestRefundAccountNumber').value,
        accountName: document.getElementById('guestRefundAccountName').value,
        phoneNumber: document.getElementById('guestRefundPhoneNumber').value,
        reason: 'Khách hàng tự hủy trên Profile'
    };

    if (!confirm('Xác nhận gửi thông tin hoàn tiền và hủy đơn hàng này?')) return;

    btn.disabled = true;
    btn.innerText = 'Đang xử lý...';


    let apiUrl = '/api/pos/orders/' + orderId + '/cancel';
    if (currentGuestRefundType === 'ROOM') {
        apiUrl = '/api/bookings/' + orderId + '/cancel';
    } else if (currentGuestRefundType === 'TOUR') {
        apiUrl = '/profile/tours/cancel/' + orderId;
    }

    fetch(apiUrl, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
    })

        .then(res => {
            if (!res.ok) return res.json().then(e => { throw new Error(e.message || 'Lỗi xử lý hoàn tiền') });
            return res.json();
        })
        .then(data => {
            alert(data.message || 'Đã tạo yêu cầu hoàn tiền và hủy đơn thành công!');
            window.location.reload();
        })
        .catch(err => {
            alert('Lỗi: ' + err.message);
            btn.disabled = false;
            btn.innerText = 'Gửi Yêu cầu Hủy đơn';
        });
}
