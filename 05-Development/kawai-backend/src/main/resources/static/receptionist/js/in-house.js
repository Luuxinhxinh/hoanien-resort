// In-House Details Modal Logic
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
                    if (dep.type === 'Main Guest') return; // Bỏ qua Main Guest vì đã hiện ở trên
                    hasDependent = true;
                    let borderBottom = idx < data.guests.length - 1 ? 'border-bottom: 1px dashed #cbd5e1;' : '';
                    html += `<li style="padding: 16px 0; display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 12px; ${borderBottom}">`;
                    
                    // Column 1: Info
                    html += `<div style="display: flex; align-items: center; gap: 12px; flex-wrap: wrap;">`;
                    html += `  <div style="display: flex; align-items: center; gap: 8px;">`;
                    html += `    <span style="font-weight: 600; color: #334155; font-size: 16px;">${dep.name}</span>`;
                    if (dep.type === 'Customer (Được nâng cấp)') {
                         html += ` <span style="background-color: #d1fae5; color: #065f46; padding: 2px 6px; border-radius: 4px; font-size: 11px; font-weight: bold;">Customer</span>`;
                    }
                    html += `  </div>`;
                    
                    let subInfo = [];
                    if (dep.dob) subInfo.push(`Sinh: <span style="color: #334155; font-weight: 500;">${dep.dob}</span>`);
                    if (dep.cccd) subInfo.push(`CCCD/Passport: <span style="color: #334155; font-weight: 500;">${dep.cccd}</span>`);
                    if (subInfo.length > 0) {
                         html += `<div style="font-size: 14px; color: #64748b; display: flex; gap: 8px; align-items: center;"><span style="color: #cbd5e1;">|</span>${subInfo.join(' <span style="color: #cbd5e1;">|</span> ')}</div>`;
                    }
                    html += `</div>`;

                    // Column 2: Badges
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
            console.error("Error fetching details:", err);
            document.getElementById('modalInHouseGuestName').innerText = 'Lỗi tải dữ liệu';
            document.getElementById('modalInHouseDependents').innerHTML = '<div style="color: red;">Không thể tải thông tin. Vui lòng thử lại sau.</div>';
        });
}

function closeInHouseModal() {
    document.getElementById('inHouseModal').style.display = 'none';
}

function upgradeDependent(dependentId, btnElement) {
    if (!confirm('Bạn có chắc chắn muốn nâng cấp người này thành Khách hàng chính không? (Sẽ được cấp tài khoản)')) return;

    btnElement.disabled = true;
    btnElement.innerText = 'Đang nâng cấp...';

    fetch(`/receptionist/checkin/upgrade-dependent/${dependentId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        }
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
        .catch(err => {
            alert('Lỗi mạng hoặc server');
            btnElement.disabled = false;
            btnElement.innerText = 'Nâng cấp Customer';
        });
}
