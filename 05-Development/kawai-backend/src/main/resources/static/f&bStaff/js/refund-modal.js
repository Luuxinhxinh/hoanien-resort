let currentRefundType = 'FOOD';

function openRefundModal(orderId, type = 'FOOD') {
    document.getElementById('refundOrderId').value = orderId;
    currentRefundType = type;
    document.getElementById('refundModal').style.display = 'flex';
}

function closeRefundModal() {
    document.getElementById('refundModal').style.display = 'none';
    document.getElementById('refundForm').reset();
}

function submitRefundForm(event) {
    event.preventDefault();
    
    const orderId = document.getElementById('refundOrderId').value;
    const btn = document.getElementById('btn-submit-refund');
    
    const payload = {
        bankName: document.getElementById('refundBankName').value,
        accountNumber: document.getElementById('refundAccountNumber').value,
        accountName: document.getElementById('refundAccountName').value,
        phoneNumber: document.getElementById('refundPhoneNumber').value,
        reason: document.getElementById('refundReason') ? document.getElementById('refundReason').value : ''
    };
    
    if (!confirm('Xác nhận gửi thông tin hoàn tiền và hủy đơn hàng này?')) return;
    
    btn.disabled = true;
    btn.innerText = 'Đang xử lý...';
    
    let apiUrl = '/api/pos/orders/' + orderId + '/cancel';
    if (currentRefundType === 'ROOM') {
        apiUrl = '/api/bookings/' + orderId + '/cancel';
    }
    
    fetch(apiUrl, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
    })
    .then(res => {
        if(!res.ok) return res.json().then(e => { throw new Error(e.message) });
        return res.json();
    })
    .then(data => {
        alert('Đã tạo yêu cầu hoàn tiền và hủy đơn thành công!');
        window.location.reload();
    })
    .catch(err => {
        alert('Lỗi: ' + err.message);
        btn.disabled = false;
        btn.innerText = 'Gửi Yêu cầu Hủy đơn';
    });
}
