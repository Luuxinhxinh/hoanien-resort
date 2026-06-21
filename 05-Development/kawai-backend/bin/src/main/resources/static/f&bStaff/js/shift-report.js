/**
 * HOANIEN RESORT & TOUR HUB — F&B Staff Shift Report
 * shift-report.js
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
   ANIMATE PROGRESS BARS
   ============================================================ */
window.addEventListener('load', () => {
  setTimeout(() => {
    document.querySelectorAll('.util-bar').forEach(bar => {
      const w = bar.style.width;
      bar.style.width = '0';
      requestAnimationFrame(() => {
        setTimeout(() => { bar.style.width = w; }, 50);
      });
    });
  }, 300);
});

/* ============================================================
   REVENUE BAR CHART
   ============================================================ */
const hourlyRevenue = [
  { hour: '06', amount: 320000 },
  { hour: '07', amount: 580000 },
  { hour: '08', amount: 920000 },
  { hour: '09', amount: 1150000 },
  { hour: '10', amount: 850000 },
  { hour: '11', amount: 1400000 },
  { hour: '12', amount: 1850000 },
  { hour: '13', amount: 1320000 },
  { hour: '14', amount: 720000 },
];

function renderBarChart() {
  const container = document.getElementById('revenue-bar-chart');
  if (!container) return;

  const maxVal = Math.max(...hourlyRevenue.map(d => d.amount));
  const maxH   = 70; // px

  container.innerHTML = '';
  hourlyRevenue.forEach(d => {
    const h    = Math.round((d.amount / maxVal) * maxH);
    const isPeak = d.amount === maxVal;
    const col  = document.createElement('div');
    col.className = 'bar-chart-col';
    col.title = `${d.hour}:00 — ${d.amount.toLocaleString('vi-VN')}₫`;
    col.innerHTML = `
      <div class="bar-chart-bar ${isPeak ? 'peak' : ''}" style="height:${h}px;"></div>
      <div class="bar-chart-lbl">${d.hour}h</div>
    `;
    container.appendChild(col);
  });
}
renderBarChart();

/* ============================================================
   MOCK ORDER DATA (125 orders)
   ============================================================ */
const ORDER_STATUSES = ['pending','preparing','served','served','served','cancelled'];
const DINE_IN_TABLES = ['Bàn 01 · Indochine','Bàn 02 · Indochine','Bàn 03 · Terrace','Bàn 05 · Indochine','Bàn 06 · Indochine','Bàn 09 · Terrace','Bàn 11 · Terrace','Bàn 17 · Poolside'];
const ROOMS = ['Phòng 101','Phòng 204','Phòng 308','Phòng 112','Phòng 501','Phòng 215','Phòng 407'];
const GUEST_NAMES = ['Nguyễn Minh Quân','Trần Thị Lan Anh','Lê Văn Hùng','Hoàng Thị Minh Nguyệt','Vũ Đình Hào','Phạm Ngọc Bảo','Đặng Quốc Trung','Trần Lan Anh','Phan Minh Tài','Ngô Thị Kim','Lê Hữu Phước','Bùi Thị Mai'];
const BASE_AMOUNTS = [185000, 285000, 430000, 520000, 750000, 850000, 1240000, 1850000, 320000, 480000];

function rand(arr) { return arr[Math.floor(Math.random() * arr.length)]; }
function randInt(min, max) { return Math.floor(Math.random() * (max - min + 1)) + min; }

function generateOrders(n = 125) {
  const orders = [];
  for (let i = 1; i <= n; i++) {
    const type   = Math.random() < 0.64 ? 'dine-in' : 'room-service';
    const status = rand(ORDER_STATUSES);
    const hh     = randInt(6, 13);
    const mm     = randInt(0, 59);
    const amount = rand(BASE_AMOUNTS) + randInt(-20000, 50000);
    orders.push({
      id:       `ORD-${String(i).padStart(3, '0')}`,
      type,
      location: type === 'dine-in' ? rand(DINE_IN_TABLES) : rand(ROOMS),
      customer: rand(GUEST_NAMES),
      time:     `${String(hh).padStart(2,'0')}:${String(mm).padStart(2,'0')}`,
      timeSort: hh * 60 + mm,
      status,
      amount:   Math.max(80000, amount),
    });
  }
  return orders;
}

let allOrders    = generateOrders(125);
let sortAsc      = false; // newest first by default
let currentPage  = 1;
const PAGE_SIZE  = 10;

function formatAmount(n) {
  return n.toLocaleString('vi-VN') + '₫';
}

function statusChip(status) {
  const map = {
    pending:   ['chip-pending',   'Chờ', 'schedule'],
    preparing: ['chip-preparing', 'Đang nấu', 'skillet'],
    served:    ['chip-served',    'Đã phục vụ', 'check_circle'],
    cancelled: ['chip-cancelled', 'Đã hủy', 'cancel'],
  };
  const [cls, label, icon] = map[status] || ['', status, 'info'];
  return `<span class="order-status-chip ${cls}"><span class="dot"></span>${label}</span>`;
}

function typeBadge(type) {
  if (type === 'dine-in') {
    return `<span class="order-type-badge type-dine-in"><span class="material-symbols-outlined" style="font-size:0.7rem;font-variation-settings:'FILL' 1,'wght' 500,'GRAD' 0,'opsz' 24;">restaurant</span>Dine-In</span>`;
  }
  return `<span class="order-type-badge type-room-svc"><span class="material-symbols-outlined" style="font-size:0.7rem;font-variation-settings:'FILL' 1,'wght' 500,'GRAD' 0,'opsz' 24;">room_service</span>Room Service</span>`;
}

function getFiltered() {
  const search  = document.getElementById('order-search')?.value.toLowerCase().trim() || '';
  const status  = document.getElementById('filter-status')?.value || 'all';
  const type    = document.getElementById('filter-type')?.value || 'all';

  return allOrders.filter(o => {
    const matchSearch = !search
      || o.id.toLowerCase().includes(search)
      || o.customer.toLowerCase().includes(search)
      || o.location.toLowerCase().includes(search);
    const matchStatus = status === 'all' || o.status === status;
    const matchType   = type === 'all' || o.type === type;
    return matchSearch && matchStatus && matchType;
  }).sort((a, b) => sortAsc ? a.timeSort - b.timeSort : b.timeSort - a.timeSort);
}

function renderTable() {
  const filtered = getFiltered();
  const tbody    = document.getElementById('orders-tbody');
  const empty    = document.getElementById('table-empty');
  const pageInfo = document.getElementById('page-info');

  const totalPages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
  if (currentPage > totalPages) currentPage = totalPages;

  const start = (currentPage - 1) * PAGE_SIZE;
  const end   = Math.min(start + PAGE_SIZE, filtered.length);
  const slice = filtered.slice(start, end);

  tbody.innerHTML = '';

  if (slice.length === 0) {
    empty.style.display = 'flex';
    pageInfo.textContent = 'Không có dữ liệu';
  } else {
    empty.style.display = 'none';
    pageInfo.textContent = `Hiển thị ${start + 1}–${end} trên ${filtered.length} đơn hàng`;
    slice.forEach(o => {
      const tr = document.createElement('tr');
      tr.dataset.orderId = o.id;
      tr.innerHTML = `
        <td class="col-order-id">${o.id}</td>
        <td>${typeBadge(o.type)}</td>
        <td style="font-size:0.78rem; color:var(--text-secondary);">${o.location}</td>
        <td style="font-weight:500;">${o.customer}</td>
        <td style="color:var(--text-secondary); white-space:nowrap;">${o.time}</td>
        <td>${statusChip(o.status)}</td>
        <td class="col-amount">${formatAmount(o.amount)}</td>
        <td>
          <button class="btn btn-sm btn-outline" onclick="viewDetail('${o.id}', event)" style="white-space:nowrap;">
            <span class="material-symbols-outlined" style="font-size:0.8rem;">open_in_new</span>
            View Detail
          </button>
        </td>
      `;
      tr.addEventListener('click', (e) => {
        if (!e.target.closest('button')) viewDetail(o.id, e);
      });
      tbody.appendChild(tr);
    });
  }

  renderPagination(totalPages);
}

function renderPagination(totalPages) {
  const prevBtn = document.getElementById('btn-prev');
  const nextBtn = document.getElementById('btn-next');
  const numCont = document.getElementById('page-numbers');

  prevBtn.disabled = currentPage <= 1;
  nextBtn.disabled = currentPage >= totalPages;
  numCont.innerHTML = '';

  // Show max 5 page buttons
  let startP = Math.max(1, currentPage - 2);
  let endP   = Math.min(totalPages, startP + 4);
  if (endP - startP < 4) startP = Math.max(1, endP - 4);

  for (let p = startP; p <= endP; p++) {
    const btn = document.createElement('button');
    btn.className = `page-btn ${p === currentPage ? 'active' : ''}`;
    btn.textContent = p;
    btn.addEventListener('click', () => { currentPage = p; renderTable(); });
    numCont.appendChild(btn);
  }
}

// Events
document.getElementById('btn-prev')?.addEventListener('click', () => {
  if (currentPage > 1) { currentPage--; renderTable(); }
});
document.getElementById('btn-next')?.addEventListener('click', () => {
  currentPage++;
  renderTable();
});

document.getElementById('order-search')?.addEventListener('input', () => {
  currentPage = 1;
  renderTable();
});

document.getElementById('filter-status')?.addEventListener('change', () => {
  currentPage = 1;
  renderTable();
});

document.getElementById('filter-type')?.addEventListener('change', () => {
  currentPage = 1;
  renderTable();
});

document.getElementById('btn-sort-time')?.addEventListener('click', function () {
  sortAsc = !sortAsc;
  this.innerHTML = sortAsc
    ? `<span class="material-symbols-outlined">swap_vert</span> Cũ nhất`
    : `<span class="material-symbols-outlined">swap_vert</span> Mới nhất`;
  currentPage = 1;
  renderTable();
});

function viewDetail(orderId, e) {
  if (e) e.stopPropagation();
  window.location.href = `order-detail.html?id=${orderId}&type=food`;
}

// Initial render
renderTable();

/* ============================================================
   MODALS LOGIC
   ============================================================ */
window.openOrdersModal = function(filter) {
  const tbody = document.getElementById('orders-modal-tbody');
  const title = document.getElementById('orders-modal-title');
  const subtitle = document.getElementById('orders-modal-subtitle');
  
  let filtered = [];
  if (filter === 'all') {
    filtered = allOrders;
    title.textContent = 'Tất cả đơn hàng';
  } else if (filter === 'dine-in' || filter === 'room-service') {
    filtered = allOrders.filter(o => o.type === filter);
    title.textContent = `Đơn hàng: ${filter === 'dine-in' ? 'Dine-In' : 'Room Service'}`;
  } else {
    filtered = allOrders.filter(o => o.status === filter);
    const statusMap = { 'pending': 'Chờ', 'preparing': 'Đang nấu', 'served': 'Đã phục vụ', 'cancelled': 'Đã hủy' };
    title.textContent = `Đơn hàng: ${statusMap[filter] || filter}`;
  }
  
  subtitle.textContent = `Tổng cộng: ${filtered.length} đơn hàng`;
  
  tbody.innerHTML = '';
  if (filtered.length === 0) {
    tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; padding:30px; color:var(--text-muted);">Không có đơn hàng nào</td></tr>`;
  } else {
    filtered.forEach(o => {
      const tr = document.createElement('tr');
      tr.style.cursor = 'pointer';
      tr.innerHTML = `
        <td class="col-order-id">${o.id}</td>
        <td>${typeBadge(o.type)}</td>
        <td style="font-size:0.78rem; color:var(--text-secondary);">${o.location}</td>
        <td style="color:var(--text-secondary); white-space:nowrap;">${o.time}</td>
        <td>${statusChip(o.status)}</td>
        <td class="col-amount">${formatAmount(o.amount)}</td>
        <td>
          <button class="btn btn-sm btn-outline" onclick="viewDetail('${o.id}', event)" style="white-space:nowrap;">
            <span class="material-symbols-outlined" style="font-size:0.8rem;">open_in_new</span>
            Chi tiết
          </button>
        </td>
      `;
      tr.addEventListener('click', (e) => {
        if (!e.target.closest('button')) viewDetail(o.id, e);
      });
      tbody.appendChild(tr);
    });
  }
  
  document.getElementById('orders-modal').style.display = 'flex';
};

const MOCK_TABLES = [
  { table: 'Bàn 01', cap: 4, area: 'Indochine', status: 'Occupied' },
  { table: 'Bàn 02', cap: 2, area: 'Indochine', status: 'Available' },
  { table: 'Bàn 03', cap: 6, area: 'Terrace', status: 'Occupied' },
  { table: 'Bàn 04', cap: 4, area: 'Terrace', status: 'Reserved' },
  { table: 'Bàn 05', cap: 8, area: 'Indochine', status: 'Available' },
  { table: 'Bàn 06', cap: 4, area: 'Indochine', status: 'Completed' },
  { table: 'Bàn 07', cap: 2, area: 'Terrace', status: 'Completed' },
  { table: 'Bàn 08', cap: 4, area: 'Poolside', status: 'Occupied' },
  { table: 'Bàn 09', cap: 6, area: 'Terrace', status: 'Available' },
  { table: 'Bàn 10', cap: 4, area: 'Poolside', status: 'Reserved' },
  { table: 'Bàn 11', cap: 2, area: 'Terrace', status: 'Occupied' },
  { table: 'Bàn 12', cap: 4, area: 'Indochine', status: 'Completed' },
  { table: 'Bàn 14', cap: 8, area: 'Indochine', status: 'Available' },
  { table: 'Bàn 15', cap: 4, area: 'Terrace', status: 'Completed' },
  { table: 'Bàn 16', cap: 2, area: 'Poolside', status: 'Reserved' },
  { table: 'Bàn 17', cap: 6, area: 'Poolside', status: 'Occupied' },
  { table: 'Bàn 18', cap: 4, area: 'Indochine', status: 'Available' },
  { table: 'Bàn 19', cap: 2, area: 'Terrace', status: 'Completed' },
  { table: 'Bàn 20', cap: 4, area: 'Poolside', status: 'Available' },
  { table: 'Bàn 21', cap: 6, area: 'Indochine', status: 'Completed' },
  { table: 'Bàn 22', cap: 4, area: 'Terrace', status: 'Reserved' },
  { table: 'Bàn 23', cap: 2, area: 'Poolside', status: 'Occupied' },
  { table: 'Bàn 24', cap: 8, area: 'Indochine', status: 'Completed' },
  { table: 'Bàn 25', cap: 4, area: 'Terrace', status: 'Available' },
  { table: 'Bàn 26', cap: 2, area: 'Poolside', status: 'Completed' },
  { table: 'Bàn 27', cap: 6, area: 'Indochine', status: 'Available' },
  { table: 'Bàn 28', cap: 4, area: 'Terrace', status: 'Available' },
  { table: 'Bàn 29', cap: 2, area: 'Poolside', status: 'Available' }
];

window.openTablesModal = function(filter) {
  const tbody = document.getElementById('tables-modal-tbody');
  const title = document.getElementById('tables-modal-title');
  const subtitle = document.getElementById('tables-modal-subtitle');
  
  let filtered = [];
  if (filter === 'all') {
    filtered = MOCK_TABLES;
    title.textContent = 'Tất cả Bàn';
  } else {
    filtered = MOCK_TABLES.filter(t => t.status === filter);
    title.textContent = `Bàn: ${filter}`;
  }
  
  subtitle.textContent = `Tổng cộng: ${filtered.length} bàn`;
  
  tbody.innerHTML = '';
  if (filtered.length === 0) {
    tbody.innerHTML = `<tr><td colspan="4" style="text-align:center; padding:30px; color:var(--text-muted);">Không có bàn nào</td></tr>`;
  } else {
    filtered.forEach(t => {
      let statusHtml = '';
      if (t.status === 'Occupied') statusHtml = `<span class="order-status-chip chip-preparing"><span class="dot"></span>Occupied</span>`;
      else if (t.status === 'Available') statusHtml = `<span class="order-status-chip chip-served"><span class="dot"></span>Available</span>`;
      else if (t.status === 'Reserved') statusHtml = `<span class="order-status-chip chip-pending"><span class="dot"></span>Reserved</span>`;
      else if (t.status === 'Completed') statusHtml = `<span class="order-status-chip chip-served" style="background:var(--status-served-bg);color:var(--status-served-text);border-color:var(--status-served-ring);"><span class="dot"></span>Completed</span>`;
      
      const tr = document.createElement('tr');
      tr.innerHTML = `
        <td style="font-weight:600; font-family:'Courier New', monospace;">${t.table}</td>
        <td>${t.cap} người</td>
        <td style="color:var(--text-secondary);">${t.area}</td>
        <td>${statusHtml}</td>
      `;
      tbody.appendChild(tr);
    });
  }
  
  document.getElementById('tables-modal').style.display = 'flex';
};

/* ============================================================
   TOAST (shared util)
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
