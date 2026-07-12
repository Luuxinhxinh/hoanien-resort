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
                showToastAlert(`✅ Phòng ${roomNum} đã dọn xong! Bạn có thể tiếp tục hoàn tất đơn.`, roomNum);
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

// ── Urgent Clean Done Alert ─────────────────────────────────────────────────
// Poll task URGENT_CLEAN đã hoàn thành (30 phút gần nhất).
// Dùng localStorage để track taskId đã alert → không hiện lại khi chuyển trang.
// Mỗi phòng dọn xong = 1 toast riêng biệt.
const URGENT_CLEAN_LS_KEY = 'urgentCleanAlertedTaskIds';
// Track room numbers alerted via urgentClean để tránh toast trùng với pollCleanedRooms
const URGENT_CLEAN_ROOM_KEY = 'urgentCleanAlertedRoomNums';

function getAlertedUrgentTaskIds() {
    try {
        const raw = localStorage.getItem(URGENT_CLEAN_LS_KEY);
        return raw ? JSON.parse(raw) : {};
    } catch (e) { return {}; }
}

function getUrgentCleanAlertedRooms() {
    try {
        const raw = localStorage.getItem(URGENT_CLEAN_ROOM_KEY);
        return raw ? JSON.parse(raw) : {}; // { roomNumber: timestamp }
    } catch (e) { return {}; }
}

function isRecentlyUrgentCleaned(roomNum) {
    const map = getUrgentCleanAlertedRooms();
    const ts = map[roomNum];
    if (!ts) return false;
    // Coi là "vừa mới" nếu trong vòng 30 phút
    return (Date.now() - ts) < 30 * 60 * 1000;
}

function markUrgentTaskAlerted(taskId, roomNum) {
    try {
        // Track taskId
        const map = getAlertedUrgentTaskIds();
        map[taskId] = Date.now();
        const twoHoursAgo = Date.now() - 2 * 60 * 60 * 1000;
        Object.keys(map).forEach(id => { if (map[id] < twoHoursAgo) delete map[id]; });
        localStorage.setItem(URGENT_CLEAN_LS_KEY, JSON.stringify(map));
        // Track roomNumber để pollCleanedRooms bỏ qua
        const roomMap = getUrgentCleanAlertedRooms();
        roomMap[roomNum] = Date.now();
        Object.keys(roomMap).forEach(r => { if ((Date.now() - roomMap[r]) > 2 * 60 * 60 * 1000) delete roomMap[r]; });
        localStorage.setItem(URGENT_CLEAN_ROOM_KEY, JSON.stringify(roomMap));
    } catch (e) { /* ignore */ }
}

async function pollCleanTasksDone() {
    try {
        const res = await fetch('/receptionist/api/notifications/clean-tasks-done');
        if (!res.ok) return;
        const tasks = await res.json(); // [ { taskId, roomNumber, completedAt }, ... ]
        if (!tasks || tasks.length === 0) return;

        const alerted = getAlertedUrgentTaskIds();
        let playSound = false;

        tasks.forEach(task => {
            if (alerted[task.taskId]) return; // Đã alert rồi → bỏ qua
            playSound = true;
            markUrgentTaskAlerted(task.taskId, task.roomNumber); // truyền roomNumber để suppress handover toast
            showToastAlert(
                `🧹 Phòng <strong>${task.roomNumber}</strong> đã dọn xong! Sẵn sàng nhận khách.`,
                task.roomNumber,
                {
                    icon: '<i class="fa-solid fa-broom fa-bounce" style="font-size:22px;color:#fde047;"></i>',
                    bg: 'linear-gradient(135deg, #065f46, #047857)',
                    onDismiss: null // clean toast chỉ cần close, không cần dismissRoomAlert
                }
            );
        });

        if (playSound) playAlertSound();
    } catch (err) {
        console.error('Failed to poll clean tasks done:', err);
    }
}

// ── Start polling (10s interval) ────────────────────────────────────────────
pollCleanedRooms();
pollPendingWalkInRooms();
pollCleanTasksDone();
setInterval(pollCleanedRooms, 10000);
setInterval(pollPendingWalkInRooms, 10000);
setInterval(pollCleanTasksDone, 10000);
