const ALERT_AUDIO = new Audio('https://actions.google.com/sounds/v1/alarms/beep_short.ogg');
ALERT_AUDIO.playbackRate = 2.0;
let lastAlertedRooms = new Set();

// ── WalkIn Pending Clean Rooms ─────────────────────────────────────────────
// Khi lễ tân bấm "Yêu cầu dọn & Phân phòng (Treo)" trên trang WalkIn,
// roomNum được lưu vào sessionStorage['walkInPendingCleanRooms'].
// Hàm bên dưới poll trực tiếp roomStatus của từng phòng đó.
// Khi phòng chuyển sang Vacant_Clean → hiện toast ngay trên màn WalkIn.
async function pollPendingWalkInRooms() {
    try {
        const raw = sessionStorage.getItem('walkInPendingCleanRooms');
        if (!raw) return;
        const pendingRooms = JSON.parse(raw); // { roomNum: true, ... }
        if (!pendingRooms || Object.keys(pendingRooms).length === 0) return;

        const roomNums = Object.keys(pendingRooms);
        const params = roomNums.map(r => 'rooms=' + encodeURIComponent(r)).join('&');
        const res = await fetch('/receptionist/api/notifications/room-status?' + params);
        if (!res.ok) return;
        const statusMap = await res.json(); // { "101": "Vacant_Clean", "102": "Vacant_Dirty" }

        let changed = false;
        roomNums.forEach(roomNum => {
            const status = statusMap[roomNum];
            if (status === 'Vacant_Clean') {
                showToastAlert(`✅ Phòng ${roomNum} đã dọn xong!`, roomNum);
                playAlertSound();
                delete pendingRooms[roomNum];
                changed = true;
            }
        });

        if (changed) {
            if (Object.keys(pendingRooms).length === 0) {
                sessionStorage.removeItem('walkInPendingCleanRooms');
            } else {
                sessionStorage.setItem('walkInPendingCleanRooms', JSON.stringify(pendingRooms));
            }
        }
    } catch (err) {
        console.error('Failed to poll walk-in pending clean rooms:', err);
    }
}

function playAlertSound() {
    let playCount = 0;
    const maxPlays = 3;
    const playHandler = () => {
        playCount++;
        if (playCount < maxPlays) {
            ALERT_AUDIO.play().catch(e => console.log('Autoplay blocked:', e));
        } else {
            ALERT_AUDIO.removeEventListener('ended', playHandler);
        }
    };
    ALERT_AUDIO.removeEventListener('ended', playHandler);
    ALERT_AUDIO.addEventListener('ended', playHandler);
    ALERT_AUDIO.play().catch(e => console.log('Autoplay blocked:', e));
}

async function pollCleanedRooms() {
    try {
        const res = await fetch('/receptionist/api/notifications/cleaned-rooms');
        if (!res.ok) return;
        const rooms = await res.json();

        let playSound = false;
        let currentAlertedRooms = new Set();


        rooms.forEach(roomInfo => {
            const roomNum = roomInfo.roomNumber;
            const customerName = roomInfo.customerName;

            currentAlertedRooms.add(roomNum);
            if (!lastAlertedRooms.has(roomNum)) {
                playSound = true;
                showToastAlert(`Ting! 🔔 Phòng ${roomNum} đã dọn xong! Vui lòng báo khách (${customerName}) lên nhận thẻ từ!`, roomNum);
            }
        });

        if (playSound) {
            playAlertSound();
        }

        lastAlertedRooms = currentAlertedRooms;
    } catch (err) {
        console.error('Failed to poll cleaned rooms:', err);
    }
}

window.dismissRoomAlert = function (roomNum, element) {
    element.remove();
    // Thêm vào lastAlertedRooms để không hiện lại trong session này
    lastAlertedRooms.add(roomNum);
    // Xóa khỏi walkInPendingCleanRooms nếu có
    try {
        const raw = sessionStorage.getItem('walkInPendingCleanRooms');
        if (raw) {
            const pending = JSON.parse(raw);
            if (pending[roomNum]) {
                delete pending[roomNum];
                if (Object.keys(pending).length === 0) {
                    sessionStorage.removeItem('walkInPendingCleanRooms');
                } else {
                    sessionStorage.setItem('walkInPendingCleanRooms', JSON.stringify(pending));
                }
            }
        }
    } catch (e) { /* ignore */ }
};

function showToastAlert(message, roomNum) {
    let container = document.getElementById('toast-alert-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-alert-container';
        container.style.position = 'fixed';
        container.style.top = '30px';
        container.style.left = '50%';
        container.style.transform = 'translateX(-50%)';
        container.style.zIndex = '9999';
        container.style.display = 'flex';
        container.style.flexDirection = 'column';
        container.style.gap = '16px';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.style.background = '#0f766e';
    toast.style.color = '#ffffff';
    toast.style.border = '2px solid #064e3b';
    toast.style.padding = '20px 32px';
    toast.style.borderRadius = '12px';
    toast.style.boxShadow = '0 10px 15px -3px rgba(0, 0, 0, 0.2)';
    toast.style.display = 'flex';
    toast.style.alignItems = 'center';
    toast.style.gap = '16px';
    toast.style.fontSize = '18px';
    toast.style.fontWeight = '600';
    toast.style.transition = 'opacity 0.3s ease';

    toast.innerHTML = `
        <i class="fa-solid fa-bell fa-shake" style="font-size: 24px; color: #fde047;"></i>
        <span>${message}</span>
        <button onclick="dismissRoomAlert('${roomNum}', this.parentElement)" style="background:none; border:none; color:#ffffff; cursor:pointer; font-size:20px; margin-left:16px;">
            <i class="fa-solid fa-times"></i>
        </button>
    `;

    container.appendChild(toast);

    // Bỏ tự động remove, để Lễ tân chủ động bấm X xác nhận mới mất
}

// Start polling — interval 5 giây
pollCleanedRooms();
pollPendingWalkInRooms();
setInterval(pollCleanedRooms, 10000);
setInterval(pollPendingWalkInRooms, 10000);
