const ALERT_AUDIO = new Audio('https://actions.google.com/sounds/v1/alarms/beep_short.ogg');
ALERT_AUDIO.playbackRate = 2.0;
let lastAlertedRooms = new Set();

async function pollCleanedRooms() {
    try {
        const res = await fetch('/receptionist/api/notifications/cleaned-rooms');
        if (!res.ok) return;
        const rooms = await res.json();

        let playSound = false;
        let currentAlertedRooms = new Set();

        const closedRoomsData = JSON.parse(localStorage.getItem('closedAlertRooms') || '{"date":"", "rooms":[]}');
        const today = new Date().toISOString().split('T')[0];
        if (closedRoomsData.date !== today) {
            closedRoomsData.date = today;
            closedRoomsData.rooms = [];
            localStorage.setItem('closedAlertRooms', JSON.stringify(closedRoomsData));
        }

        rooms.forEach(roomInfo => {
            const roomNum = roomInfo.roomNumber;
            const customerName = roomInfo.customerName;

            // Bỏ qua nếu Lễ tân đã bấm X tắt thông báo phòng này trong ngày hôm nay
            if (closedRoomsData.rooms.includes(roomNum)) return;

            currentAlertedRooms.add(roomNum);
            if (!lastAlertedRooms.has(roomNum)) {
                playSound = true;
                // Show notification to user
                showToastAlert(`Ting! 🔔 Phòng ${roomNum} đã dọn xong! Vui lòng báo khách (${customerName}) lên nhận thẻ từ!`, roomNum);
            }
        });

        if (playSound) {
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

        lastAlertedRooms = currentAlertedRooms;
    } catch (err) {
        console.error('Failed to poll cleaned rooms:', err);
    }
}

window.dismissRoomAlert = function (roomNum, element) {
    element.remove();
    // Lưu vào localStorage để không hiện lại khi F5
    const closedRoomsData = JSON.parse(localStorage.getItem('closedAlertRooms') || '{"date":"", "rooms":[]}');
    const today = new Date().toISOString().split('T')[0];
    if (closedRoomsData.date !== today) {
        closedRoomsData.date = today;
        closedRoomsData.rooms = [];
    }
    if (!closedRoomsData.rooms.includes(roomNum)) {
        closedRoomsData.rooms.push(roomNum);
        localStorage.setItem('closedAlertRooms', JSON.stringify(closedRoomsData));
    }
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

// Start polling
pollCleanedRooms();
setInterval(pollCleanedRooms, 15000);
