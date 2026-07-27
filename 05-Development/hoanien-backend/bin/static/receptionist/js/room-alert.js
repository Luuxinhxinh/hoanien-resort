const ALERT_AUDIO = new Audio('https://actions.google.com/sounds/v1/alarms/beep_short.ogg');
ALERT_AUDIO.playbackRate = 2.0;

// Dùng localStorage với TTL 2 giờ để persist giữa các lần đăng nhập.
// Tránh toast hiện lại mỗi khi nhân viên đăng nhập lại trong cùng ca trực.
const HANDOVER_LS_KEY = 'handoverAlertedRooms_v2';
const URGENT_CLEAN_LS_KEY = 'urgentCleanAlertedTasks_v2';
const ALERT_TTL_MS = 2 * 60 * 60 * 1000; // 2 giờ

function _lsGetWithTTL(key) {
    try {
        const raw = localStorage.getItem(key);
        if (!raw) return [];
        const obj = JSON.parse(raw);
        const now = Date.now();
        // Lọc bỏ các entry đã quá TTL
        const valid = (obj.items || []).filter(item => (now - item.ts) < ALERT_TTL_MS);
        if (valid.length !== (obj.items || []).length) {
            localStorage.setItem(key, JSON.stringify({ items: valid }));
        }
        return valid.map(item => item.id);
    } catch { return []; }
}

function _lsAddWithTTL(key, id) {
    try {
        const raw = localStorage.getItem(key);
        const obj = raw ? JSON.parse(raw) : { items: [] };
        const now = Date.now();
        const valid = (obj.items || []).filter(item => (now - item.ts) < ALERT_TTL_MS);
        if (!valid.find(item => item.id === id)) {
            valid.push({ id, ts: now });
        }
        localStorage.setItem(key, JSON.stringify({ items: valid }));
    } catch { /* ignore */ }
}

function getHandoverAlerted() { return _lsGetWithTTL(HANDOVER_LS_KEY); }
function setHandoverAlerted(arr) {
    // Ghi lại toàn bộ danh sách (dùng khi cần trim), giữ timestamp hiện tại
    try {
        const now = Date.now();
        const items = arr.map(id => ({ id, ts: now }));
        localStorage.setItem(HANDOVER_LS_KEY, JSON.stringify({ items }));
    } catch { /* ignore */ }
}
function isHandoverAlerted(roomNum) { return getHandoverAlerted().includes(roomNum); }
function addHandoverAlerted(roomNum) { _lsAddWithTTL(HANDOVER_LS_KEY, roomNum); }

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
// urgentCleanAlertedTasks → dùng localStorage với TTL (đã khai báo ở trên)
const RECENTLY_URGENT_CLEANED_ROOMS_KEY = 'recentlyUrgentCleanedRooms';

function getUrgentCleanAlerted() { return _lsGetWithTTL(URGENT_CLEAN_LS_KEY); }
function setUrgentCleanAlerted(arr) {
    // Noop — dùng _lsAddWithTTL trực tiếp để tránh mất timestamp
}
function getRecentlyUrgentCleanedRooms() {
    try { return JSON.parse(sessionStorage.getItem(RECENTLY_URGENT_CLEANED_ROOMS_KEY) || '[]'); } catch { return []; }
}
function setRecentlyUrgentCleanedRooms(arr) {
    try { sessionStorage.setItem(RECENTLY_URGENT_CLEANED_ROOMS_KEY, JSON.stringify(arr)); } catch { /* ignore */ }
}

window.isRecentlyUrgentCleaned = function (roomNum) {
    return getRecentlyUrgentCleanedRooms().includes(roomNum);
};

window.handleRoomCleanedEvent = function (payload) {
    const roomNumber = payload.roomNumber || (payload.message && payload.message.match(/Phòng (\w+)/) ? payload.message.match(/Phòng (\w+)/)[1] : null);
    if (!roomNumber) return;

    // 1. Kiểm tra xem phòng này có đang nằm trong danh sách chờ nhận phòng vãng lai / dọn khẩn cấp không
    let pendingRooms = {};
    try {
        const raw = sessionStorage.getItem('walkInPendingCleanRooms');
        if (raw) pendingRooms = JSON.parse(raw);
    } catch { /* ignore */ }

    if (pendingRooms[roomNumber]) {
        // Đây là phòng lễ tân phiên này đang hóng dọn khẩn
        showToastAlert(` ✅ Phòng ${roomNumber} đã dọn xong!`, roomNumber, {
            icon: '<i class="fa-solid fa-check-circle fa-beat" style="font-size:24px;color:#4ade80;"></i>',
            bg: '#b91c1c'
        });
        playAlertSound();

        // Cập nhật lại session storage (xóa phòng đã xong)
        delete pendingRooms[roomNumber];
        if (Object.keys(pendingRooms).length === 0) {
            sessionStorage.removeItem('walkInPendingCleanRooms');
        } else {
            sessionStorage.setItem('walkInPendingCleanRooms', JSON.stringify(pendingRooms));
        }

        // Đánh dấu đã dọn khẩn để tí nữa không bị toast trùng
        const recentRooms = getRecentlyUrgentCleanedRooms();
        if (!recentRooms.includes(roomNumber)) {
            recentRooms.push(roomNumber);
            setRecentlyUrgentCleanedRooms(recentRooms);
        }
        return;
    }

    // 2. Thông báo dọn phòng thành công thông thường
    if (isHandoverAlerted(roomNumber) || window.isRecentlyUrgentCleaned(roomNumber)) return;

    addHandoverAlerted(roomNumber);
    const displayMsg = payload.message ? payload.message : `Phòng ${roomNumber} đã dọn xong!`;
    showToastAlert(`Ting!${displayMsg}`, roomNumber);
    playAlertSound();
};
