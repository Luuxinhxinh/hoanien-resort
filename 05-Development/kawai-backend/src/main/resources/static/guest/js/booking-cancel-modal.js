function openBkCancelModal(bookingId) {
    document.getElementById('bkcm-booking-id').value = bookingId;
    document.getElementById('bkcm-booking-code').innerText = '#HN' + bookingId;
    document.getElementById('bookingCancelModal').style.display = 'flex';
}

function closeBkCancelModal() {
    document.getElementById('bookingCancelModal').style.display = 'none';
    document.getElementById('bkcm-form').reset();
}

function submitBkCancelForm(event) {
    event.preventDefault();

    const bookingId = document.getElementById('bkcm-booking-id').value;
    const btn = document.getElementById('bkcm-btn-submit');

    const payload = {
        bankName: document.getElementById('bkcm-bank-name').value,
        accountNumber: document.getElementById('bkcm-account-number').value,
        accountName: document.getElementById('bkcm-account-name').value,
        phoneNumber: document.getElementById('bkcm-phone').value
    };

    if (!confirm(`Xác nhận gửi thông tin hoàn tiền và hủy đơn đặt phòng #HN${bookingId}?`)) return;

    btn.disabled = true;
    btn.innerHTML = 'Đang xử lý...';

    fetch('/api/bookings/' + bookingId + '/cancel', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
    })
    .then(res => res.json().then(data => ({ status: res.status, body: data })))
    .then(resObj => {
        if (resObj.status !== 200) {
            alert(`Không thể hủy đơn: ${resObj.body.message || 'Lỗi hệ thống'}`);
            btn.disabled = false;
            btn.innerHTML = `
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24"
                     fill="none" stroke="currentColor" stroke-width="2"
                     stroke-linecap="round" stroke-linejoin="round">
                    <polyline points="3 6 5 6 21 6"/>
                    <path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/>
                    <path d="M10 11v6"/><path d="M14 11v6"/>
                    <path d="M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/>
                </svg>
                Gửi yêu cầu hủy phòng`;
        } else {
            alert(resObj.body.message || 'Hủy phòng thành công!');
            window.location.reload();
        }
    })
    .catch(err => {
        console.error(err);
        alert("Đã xảy ra lỗi hệ thống khi hủy đơn.");
        btn.disabled = false;
        btn.innerHTML = `
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24"
                 fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round">
                <polyline points="3 6 5 6 21 6"/>
                <path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/>
                <path d="M10 11v6"/><path d="M14 11v6"/>
                <path d="M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/>
            </svg>
            Gửi yêu cầu hủy phòng`;
    });
}
