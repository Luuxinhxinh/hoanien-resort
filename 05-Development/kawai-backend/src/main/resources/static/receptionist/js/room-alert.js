const ALERT_AUDIO = new Audio('https://actions.google.com/sounds/v1/alarms/beep_short.ogg');
ALERT_AUDIO.playbackRate = 2.0;

// lastAlertedRooms dùng sessionStorage để persist qua navigate giữa các trang
// (reset khi đóng tab — phù hợp với ca làm việc của Lễ tân)
const HANDOVER_SS_KEY = 'handoverAlertedRooms';
function getHandoverAlerted() {
    try { return JSON.parse(sessionStorage.getItem(HANDOVER_SS_KEY) || '[]'); } catch { return []; }
}
function setHandoverAlerted(arr) {
    try { sessionStorage.setItem(HANDOVER_SS_KEY, JSON.stringify(arr)); } catch { /* ignore */ }
}
function isHandoverAlerted(roomNum) { return getHandoverAlerted().includes(roomNum); }
function addHandoverAlerted(roomNum) {
    const arr = getHandoverAlerted();
    if (!arr.includes(roomNum)) { arr.push(roomNum); setHandoverAlerted(arr); }
}

// ── Shared Utilities ────────────────────────────────────────────────────────

function getOrCreateToastContainer() {
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
    return container;
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

/**
 * Hiện toast thông báo dọn phòng.
 * @param {string} message  - Nội dung hiển thị
 * @param {string} roomNum  - Số phòng (dùng cho dismiss)
 * @param {object} [opts]   - Tùy chọn giao diện
 *   opts.icon   : HTML icon (mặc định: fa-bell fa-shake)
 *   opts.bg     : CSS background (mặc định: #0f766e)
 *   opts.onDismiss : callback khi bấm X (ngoài việc remove element)
 */
function showToastAlert(message, roomNum, opts = {}) {
    const container = getOrCreateToastContainer();

    const icon = opts.icon || '<i class="fa-solid fa-bell fa-shake" style="font-size:24px;color:#fde047;"></i>';
    const bg = opts.bg || '#0f766e';
    const onDismissAttr = opts.onDismiss
        ? `onclick="${opts.onDismiss}; this.parentElement.remove()"`
        : `onclick="dismissRoomAlert('${roomNum}', this.parentElement)"`;

    const toast = document.createElement('div');
    toast.style.cssText = `
        background:${bg}; color:#ffffff; border:2px solid #064e3b;
        padding:20px 28px; border-radius:12px;
        box-shadow:0 10px 15px -3px rgba(0,0,0,0.2);
        display:flex; align-items:center; gap:16px;
        font-size:18px; font-weight:600; min-width:320px;
        transition:opacity 0.3s ease;
    `;
    toast.innerHTML = `
        ${icon}
        <span>${message}</span>
        <button ${onDismissAttr} style="background:none;border:none;color:#ffffff;cursor:pointer;font-size:20px;margin-left:auto;">
            <i class="fa-solid fa-times"></i>
        </button>
    `;
    container.appendChild(toast);
}

window.dismissRoomAlert = function (roomNum, element) {
    element.remove();
    // Thêm vào sessionStorage để không hiện lại khi chuyển trang
    addHandoverAlerted(roomNum);
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

// ── Urgent Clean Notification ─────────────────────────────────────────────────
const URGENT_CLEAN_SS_KEY = 'urgentCleanAlertedTasks';
const RECENTLY_URGENT_CLEANED_ROOMS_KEY = 'recentlyUrgentCleanedRooms';

function getUrgentCleanAlerted() {
    try { return JSON.parse(sessionStorage.getItem(URGENT_CLEAN_SS_KEY) || '[]'); } catch { return []; }
}
function setUrgentCleanAlerted(arr) {
    try { sessionStorage.setItem(URGENT_CLEAN_SS_KEY, JSON.stringify(arr)); } catch { /* ignore */ }
}
function getRecentlyUrgentCleanedRooms() {
    try { return JSON.parse(sessionStorage.getItem(RECENTLY_URGENT_CLEANED_ROOMS_KEY) || '[]'); } catch { return []; }
}
function setRecentlyUrgentCleanedRooms(arr) {
    try { sessionStorage.setItem(RECENTLY_URGENT_CLEANED_ROOMS_KEY, JSON.stringify(arr)); } catch { /* ignore */ }
}

window.isRecentlyUrgentCleaned = function(roomNum) {
    return getRecentlyUrgentCleanedRooms().includes(roomNum);
};

window.pollCleanTasksDone = async function() {
    try {
        const res = await fetch('/receptionist/api/notifications/clean-tasks-done');
        if (!res.ok) return;
        const tasks = await res.json();
        
        let playSound = false;
        tasks.forEach(task => {
            const alertedTasks = getUrgentCleanAlerted();
            if (alertedTasks.includes(task.taskId)) return;
            
            playSound = true;
            alertedTasks.push(task.taskId);
            setUrgentCleanAlerted(alertedTasks);
            
            const recentRooms = getRecentlyUrgentCleanedRooms();
            if (!recentRooms.includes(task.roomNumber)) {
                recentRooms.push(task.roomNumber);
                setRecentlyUrgentCleanedRooms(recentRooms);
            }
            
            showToastAlert(`🚨 Housekeeping đã hoàn tất dọn khẩn cấp phòng ${task.roomNumber}!`, task.roomNumber, {
                icon: '<i class="fa-solid fa-check-circle fa-beat" style="font-size:24px;color:#4ade80;"></i>',
                bg: '#b91c1c'
            });
        });
        if (playSound) playAlertSound();
    } catch (err) {
        console.error('Failed to poll urgent clean tasks:', err);
    }
};

// ── WalkIn Pending Clean Rooms ──────────────────────────────────────────────
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

// ── Cleaned Rooms (WalkIn handover) ────────────────────────────────────────
async function pollCleanedRooms() {
    try {
        const res = await fetch('/receptionist/api/notifications/cleaned-rooms');
        if (!res.ok) return;
        const rooms = await res.json();

        let playSound = false;
        // Lấy danh sách phòng backend đang trả về (còn pending handover)
        const currentRoomNums = rooms.map(r => r.roomNumber);

        rooms.forEach(roomInfo => {
            const roomNum = roomInfo.roomNumber;
            const customerName = roomInfo.customerName;
            // Bỏ qua nếu phòng này vừa được alert bởi pollUrgentCleanDone (tránh toast trùng)
            if (isHandoverAlerted(roomNum) || isRecentlyUrgentCleaned(roomNum)) return;
            playSound = true;
            addHandoverAlerted(roomNum);
            showToastAlert(`Ting! 🔔 Phòng ${roomNum} đã dọn xong! Vui lòng báo khách (${customerName}) lên nhận thẻ từ!`, roomNum);
        });

        // Dọn phòng đã check-in xong khỏi danh sách (để hiện lại nếu lần sau cần)
        const cleaned = getHandoverAlerted().filter(r => currentRoomNums.includes(r));
        setHandoverAlerted(cleaned);

        if (playSound) playAlertSound();
    } catch (err) {
        console.error('Failed to poll cleaned rooms:', err);
    }
}

// ── Khởi chạy lần đầu khi load trang ──────────────────────────────────────
window.pollCleanTasksDone();
pollCleanedRooms();
pollPendingWalkInRooms();
