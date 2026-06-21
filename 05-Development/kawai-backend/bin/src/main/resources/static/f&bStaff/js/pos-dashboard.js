/**
 * HOANIEN RESORT & TOUR HUB — F&B Staff POS Dashboard
 * pos-dashboard.js  (statics/f&bStaff/js/)
 */

/* ============================================================
   LIVE CLOCK
   ============================================================ */
function updateClock() {
  const now = new Date();
  const clockEl = document.getElementById('live-clock');
  const dateEl  = document.getElementById('live-date');
  if (!clockEl) return;
  const pad = n => String(n).padStart(2, '0');
  clockEl.textContent = `${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`;
  if (dateEl) {
    const days   = ['CN','T2','T3','T4','T5','T6','T7'];
    const months = ['01','02','03','04','05','06','07','08','09','10','11','12'];
    dateEl.textContent = `${days[now.getDay()]}, ${now.getDate()}/${months[now.getMonth()]}/${now.getFullYear()}`;
  }
}
setInterval(updateClock, 1000);
updateClock();

/* ============================================================
   FILTER PILLS — Order Status Tabs
   ============================================================ */
document.querySelectorAll('.pill[data-filter]').forEach(pill => {
  pill.addEventListener('click', function () {
    this.closest('.filter-pills').querySelectorAll('.pill').forEach(p => p.classList.remove('active'));
    this.classList.add('active');
    const filter = this.dataset.filter;
    document.querySelectorAll('.order-card[data-status]').forEach(card => {
      const show = filter === 'all' || card.dataset.status === filter;
      card.style.display = show ? '' : 'none';
      if (show) card.style.animation = 'slideIn 0.22s ease';
    });
    const visible = document.querySelectorAll('.order-card[data-status]:not([style*="display: none"])').length;
    const el = document.getElementById('active-order-count');
    if (el) el.textContent = visible;
  });
});

/* ============================================================
   ORDER CARD — expand/collapse items on click
   ============================================================ */
document.querySelectorAll('.order-card').forEach(card => {
  card.addEventListener('click', function (e) {
    if (e.target.closest('button') || e.target.closest('a')) return;
    const items = this.querySelector('.order-items');
    if (!items) return;
    items.style.display = items.style.display === 'none' ? 'flex' : 'none';
  });
});

/* ============================================================
   MARK AS SERVED
   ============================================================ */
document.querySelectorAll('.btn-mark-served').forEach(btn => {
  btn.addEventListener('click', function (e) {
    e.stopPropagation();
    const card = this.closest('.order-card');
    const chip = card.querySelector('.order-status-chip');
    chip.className = 'order-status-chip chip-served';
    chip.innerHTML = '<span class="dot"></span> Đã phục vụ';
    card.dataset.status = 'served';
    card.style.opacity = '0.7';
    showToast('success', 'Hoàn tất', `Đơn ${card.dataset.order} đã phục vụ.`);
  });
});

/* ============================================================
   POST TO ROOM
   ============================================================ */
document.querySelectorAll('.btn-post-room').forEach(btn => {
  btn.addEventListener('click', function (e) {
    e.stopPropagation();
    const card  = this.closest('.order-card');
    const amount = card.querySelector('.order-amount')?.textContent || '';
    showToast('info', 'Ký gửi phòng', `Đã Post to Room ${amount} — Đơn ${card.dataset.order}.`);
  });
});

/* ============================================================
   CANCEL ORDER
   ============================================================ */
document.querySelectorAll('.btn-cancel-order').forEach(btn => {
  btn.addEventListener('click', function (e) {
    e.stopPropagation();
    const card = this.closest('.order-card');
    if (confirm(`Hủy đơn ${card.dataset.order}?`)) {
      card.style.opacity = '0.35';
      card.style.pointerEvents = 'none';
      showToast('warning', 'Đã hủy', `Đơn ${card.dataset.order} đã bị hủy.`);
    }
  });
});

/* ============================================================
   ACCEPT ROOM SERVICE ALERT
   ============================================================ */
function bindAcceptBtn(btn, roomLabel) {
  btn.addEventListener('click', function () {
    const item = this.closest('.alert-item');
    item.style.transition = 'all 0.28s ease';
    item.style.opacity = '0';
    item.style.transform = 'translateX(12px)';
    setTimeout(() => item.remove(), 300);
    showToast('success', 'Đã nhận', `Đơn Room Service ${roomLabel} được tiếp nhận.`);
    const badge = document.getElementById('roomsvc-badge');
    if (badge) {
      const c = Math.max(0, parseInt(badge.textContent) - 1);
      badge.textContent = c;
      if (c === 0) badge.style.display = 'none';
    }
  });
}

document.querySelectorAll('.btn-accept-alert').forEach(btn => {
  const title = btn.closest('.alert-item')?.querySelector('.alert-title')?.textContent || '';
  bindAcceptBtn(btn, title);
});

/* ============================================================
   TABLE MINI-MAP TOOLTIP
   ============================================================ */
const tableLabels = { vacant: 'Trống', occupied: 'Có khách', reserved: 'Đặt trước', cleaning: 'Đang dọn' };
document.querySelectorAll('.table-dot').forEach(dot => {
  dot.title = `Bàn ${dot.dataset.table} — ${tableLabels[dot.dataset.status] || dot.dataset.status}`;
});

/* ============================================================
   NEW ORDER MODAL
   ============================================================ */
const newOrderModal = document.getElementById('newOrderModal');

document.querySelectorAll('[data-open-modal="newOrder"]').forEach(btn => {
  btn.addEventListener('click', () => {
    newOrderModal?.classList.add('open');
    document.body.style.overflow = 'hidden';
  });
});

document.querySelectorAll('[data-close-modal]').forEach(btn => {
  btn.addEventListener('click', closeModal);
});

newOrderModal?.addEventListener('click', e => {
  if (e.target === newOrderModal) closeModal();
});

function closeModal() {
  newOrderModal?.classList.remove('open');
  document.body.style.overflow = '';
}

document.getElementById('newOrderForm')?.addEventListener('submit', function (e) {
  e.preventDefault();
  const table = document.getElementById('selectTable').value;
  const type  = document.getElementById('selectType').value;
  const label = type === 'dine-in' ? 'Bàn' : 'Phòng';
  closeModal();
  showToast('success', 'Đơn đã tạo', `Đơn mới cho ${label} ${table} đã gửi KDS.`);
  const kpi = document.getElementById('kpi-active-orders');
  if (kpi) kpi.textContent = parseInt(kpi.textContent) + 1;
});

/* ============================================================
   TOAST SYSTEM
   ============================================================ */
function showToast(type, title, message, duration = 4000) {
  const container = document.getElementById('toast-container');
  if (!container) return;
  const icons = { success: 'check_circle', warning: 'warning', info: 'info', error: 'error' };
  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  toast.innerHTML = `
    <span class="toast-icon material-symbols-outlined">${icons[type] || 'info'}</span>
    <div style="flex:1;min-width:0;">
      <div class="toast-title">${title}</div>
      <div class="toast-msg">${message}</div>
    </div>
    <button class="toast-dismiss" onclick="this.closest('.toast').remove()">
      <span class="material-symbols-outlined">close</span>
    </button>
  `;
  container.appendChild(toast);
  setTimeout(() => {
    toast.style.transition = 'all 0.28s ease';
    toast.style.opacity = '0';
    toast.style.transform = 'translateX(14px)';
    setTimeout(() => toast.remove(), 300);
  }, duration);
}

/* ============================================================
   SIMULATED REAL-TIME: New Room Service every ~30s
   ============================================================ */
const rsQueue = [
  { room: '204', guest: 'Nguyễn Văn An',    items: '2x Phở Bò, 1x Nước cam',          amount: '320.000₫' },
  { room: '308', guest: 'Trần Thị Bình',    items: '1x Cơm Chiên, 2x Bia Hà Nội',      amount: '185.000₫' },
  { room: '112', guest: 'Lê Hoàng Nam',     items: '1x Burger Wagyu, 1x Smoothie',     amount: '275.000₫' },
  { room: '501', guest: 'Phạm Thùy Linh',   items: '3x Bánh mì thịt nướng, 1x Cà phê', amount: '210.000₫' },
];
let rsIdx = 0;

function simulateRoomService() {
  const o = rsQueue[rsIdx++ % rsQueue.length];
  const alertList = document.getElementById('alert-list');
  if (!alertList) return;
  const now = new Date();
  const time = `${String(now.getHours()).padStart(2,'0')}:${String(now.getMinutes()).padStart(2,'0')}`;
  const item = document.createElement('div');
  item.className = 'alert-item';
  item.innerHTML = `
    <div class="alert-icon"><span class="material-symbols-outlined">room_service</span></div>
    <div class="alert-body">
      <div class="alert-title">Phòng ${o.room} — ${o.guest}</div>
      <div class="alert-desc">${o.items}</div>
      <div class="alert-time">${time} · ${o.amount}</div>
    </div>
    <div class="alert-actions">
      <button class="btn btn-sm btn-success btn-accept-alert"><span class="material-symbols-outlined">check</span></button>
    </div>
  `;
  bindAcceptBtn(item.querySelector('.btn-accept-alert'), `Phòng ${o.room}`);
  alertList.prepend(item);
  const badge = document.getElementById('roomsvc-badge');
  if (badge) { badge.style.display = ''; badge.textContent = parseInt(badge.textContent || 0) + 1; }
  showToast('info', 'Room Service Mới', `Phòng ${o.room}: ${o.items}`);
}

setTimeout(() => { simulateRoomService(); setInterval(simulateRoomService, 30000); }, 18000);

/* ============================================================
   MINI STAT BARS — animate on load
   ============================================================ */
window.addEventListener('load', () => {
  setTimeout(() => {
    document.querySelectorAll('.mini-stat-bar-fill').forEach(bar => {
      bar.style.width = (bar.dataset.width || '50') + '%';
    });
  }, 350);
});

/* ============================================================
   KEYBOARD SHORTCUTS
   N  = open new order modal
   Esc = close modal
   ============================================================ */
document.addEventListener('keydown', e => {
  const tag = document.activeElement.tagName;
  if (e.key === 'n' && !e.ctrlKey && !e.metaKey && tag !== 'INPUT' && tag !== 'TEXTAREA' && tag !== 'SELECT') {
    newOrderModal?.classList.add('open');
    document.body.style.overflow = 'hidden';
  }
  if (e.key === 'Escape') closeModal();
});

/* ============================================================
   MODAL QTY HELPER (global for inline onclick)
   ============================================================ */
window.changeQty = function (btn, delta) {
  const display = btn.parentElement.querySelector('.qty-display');
  let val = parseInt(display.textContent) || 0;
  display.textContent = Math.max(0, val + delta);
};

/* ============================================================
   DASHBOARD TABS (Food Orders vs Table Orders)
   ============================================================ */
document.querySelectorAll('.dash-tab').forEach(tab => {
  tab.addEventListener('click', function() {
    // Switch active tab
    document.querySelectorAll('.dash-tab').forEach(t => t.classList.remove('active'));
    this.classList.add('active');

    const target = this.dataset.target;
    
    // Switch lists
    const listFood = document.getElementById('list-food');
    const listTable = document.getElementById('list-table');
    listFood.style.display = (target === 'list-food') ? 'flex' : 'none';
    listFood.style.flexDirection = 'column';
    listTable.style.display = (target === 'list-table') ? 'flex' : 'none';
    listTable.style.flexDirection = 'column';

    // Switch filters
    document.getElementById('filters-food').style.display = (target === 'list-food') ? 'flex' : 'none';
    document.getElementById('filters-table').style.display = (target === 'list-table') ? 'flex' : 'none';
  });
});
