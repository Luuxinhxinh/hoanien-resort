function openGuestRefundModal(orderId) {
    document.getElementById('guestRefundOrderId').value = orderId;
    document.getElementById('guestRefundModal').style.display = 'flex';
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
    
    // Sử dụng endpoint dành riêng cho Khách hàng
    fetch('/api/pos/guest/orders/' + orderId + '/cancel', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
    })
    .then(async res => {
        if(!res.ok) {
            // [SAFE FETCH] Lấy chuỗi raw text trước để tránh lỗi SyntaxError 
            // khi server sập / chặn quyền và trả về HTML (Ví dụ: Whitelabel Error Page)
            const text = await res.text();
            try {
                const e = JSON.parse(text);
                throw new Error(e.message || 'Lỗi hệ thống');
            } catch (err) {
                // Nếu parse JSON thất bại -> đây là HTML page -> báo lỗi HTTP code
                throw new Error(`Mã lỗi HTTP ${res.status}: Không thể hủy đơn`);
            }
        }
        return res.json();
    })
    .then(data => {
        alert('Đã tạo yêu cầu hoàn tiền và hủy đơn thành công!');
        window.location.reload();
    })
    .catch(err => {
        alert('Lỗi: ' + (err.message || 'Không xác định'));
        btn.disabled = false;
        btn.innerText = 'Gửi Yêu cầu Hủy đơn';
    });
}
