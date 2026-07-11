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
   REVENUE PIE CHART
   ============================================================ */
function renderPieChart(data) {
  const container = document.getElementById('revenue-pie-chart-container');
  if (!container) return;

  const total = data.totalRevenue || 0;
  const pie = document.getElementById('revenue-pie-chart');
  const legend = document.getElementById('revenue-pie-legend');

  if (total === 0) {
    pie.style.background = 'conic-gradient(#e0e0e0 0deg, #e0e0e0 360deg)';
    legend.innerHTML = '<div style="color: #999; font-size: 0.8rem; padding: 10px;">No revenue data</div>';
    return;
  }

  const cash = data.totalCashRevenue || 0;
  const vnpay = data.totalVnpayRevenue || 0;
  const charge = data.totalChargeToRoomRevenue || 0;

  const cashPct = Math.round((cash / total) * 100);
  const vnpayPct = Math.round((vnpay / total) * 100);
  const chargePct = Math.round((charge / total) * 100); // Or 100 - cashPct - vnpayPct

  const degCash = (cash / total) * 360;
  const degVnpay = (vnpay / total) * 360;
  
  const cCash = '#10B981'; // Green
  const cVnpay = '#3B82F6'; // Blue
  const cCharge = '#F59E0B'; // Orange

  // Conic gradient: Cash -> VNPay -> Charge To Room
  pie.style.background = `conic-gradient(
    ${cCash} 0deg, ${cCash} ${degCash}deg,
    ${cVnpay} ${degCash}deg, ${cVnpay} ${degCash + degVnpay}deg,
    ${cCharge} ${degCash + degVnpay}deg, ${cCharge} 360deg
  )`;

  legend.innerHTML = `
    <div class="legend-item">
      <div class="legend-color" style="background: ${cCash}"></div>
      <span>Cash</span>
      <b>${cashPct}%</b>
    </div>
    <div class="legend-item">
      <div class="legend-color" style="background: ${cVnpay}"></div>
      <span>VNPay</span>
      <b>${vnpayPct}%</b>
    </div>
    <div class="legend-item">
      <div class="legend-color" style="background: ${cCharge}"></div>
      <span>Charge to Room</span>
      <b>${chargePct}%</b>
    </div>
  `;
}

/* ============================================================
   API FETCH & DATA STATE
   ============================================================ */
let allOrders    = [];
let sortAsc      = false; // newest first by default
let currentPage  = 1;
const PAGE_SIZE  = 10;
const CURRENT_STAFF_ID = 1; // Hardcoded for now

let currentReportDate = new Date();

function updateDateUI() {
  const subtitle = document.querySelector('.topbar-subtitle');
  const dateLabel = document.getElementById('sr-date-label');
  const dateInput = document.getElementById('sr-date-input');
  const nextBtnNav = document.getElementById('btn-next-day-nav');
  const closeBtn = document.getElementById('btn-close-day');
  
  const today = new Date();
  const isToday = currentReportDate.toDateString() === today.toDateString();
  
  const viDateStr = currentReportDate.toLocaleDateString('vi-VN');
  const days = ['CN','Th 2','Th 3','Th 4','Th 5','Th 6','Th 7'];
  const dayName = days[currentReportDate.getDay()];
  
  const year = currentReportDate.getFullYear();
  const month = String(currentReportDate.getMonth() + 1).padStart(2, '0');
  const day = String(currentReportDate.getDate()).padStart(2, '0');
  const isoDate = `${year}-${month}-${day}`;
  
  if (subtitle) {
    subtitle.textContent = isToday ? `F&B Daily Report · Current Day` : `F&B Daily Report · ${viDateStr}`;
  }
  
  if (dateLabel) {
    dateLabel.textContent = isToday ? `Hôm nay — ${dayName}, ${viDateStr}` : `${dayName}, ${viDateStr}`;
  }
  
  if (dateInput) {
    dateInput.value = isoDate;
  }
  
  if (nextBtnNav) {
    nextBtnNav.disabled = isToday;
    nextBtnNav.style.opacity = isToday ? '0.5' : '1';
    nextBtnNav.style.cursor = isToday ? 'not-allowed' : 'pointer';
  }
  
  if (closeBtn) {
    closeBtn.style.display = isToday ? 'inline-flex' : 'none';
  }
}

window.changeReportDate = function(offsetOrDate) {
  if (offsetOrDate === 'today') {
    currentReportDate = new Date();
  } else if (typeof offsetOrDate === 'string') {
    currentReportDate = new Date(offsetOrDate);
  } else {
    currentReportDate.setDate(currentReportDate.getDate() + offsetOrDate);
  }
  
  const today = new Date();
  if (currentReportDate > today) {
    currentReportDate = new Date();
  }
  
  updateDateUI();
  fetchDailyReport();
};

async function fetchDailyReport() {
  try {
    const year = currentReportDate.getFullYear();
    const month = String(currentReportDate.getMonth() + 1).padStart(2, '0');
    const day = String(currentReportDate.getDate()).padStart(2, '0');
    const dateStr = `${year}-${month}-${day}`;
    
    const res = await fetch(`/api/v1/fnb/daily-reports/preview?date=${dateStr}&staffId=${CURRENT_STAFF_ID}`);
    if (!res.ok) throw new Error('Failed to fetch daily report');
    
    const data = await res.json();
    
    // Update Revenue Cards
    document.getElementById('rev-cash').textContent = formatAmount(data.totalCashRevenue);
    document.getElementById('rev-vnpay').textContent = formatAmount(data.totalVnpayRevenue);
    document.getElementById('rev-charge').textContent = formatAmount(data.totalChargeToRoomRevenue);
    document.getElementById('rev-total').textContent = formatAmount(data.totalRevenue);
    
    if (data.totalRevenue > 0) {
      document.getElementById('rev-cash-pct').textContent = Math.round((data.totalCashRevenue / data.totalRevenue) * 100) + '%';
      document.getElementById('rev-vnpay-pct').textContent = Math.round((data.totalVnpayRevenue / data.totalRevenue) * 100) + '%';
      document.getElementById('rev-charge-pct').textContent = Math.round((data.totalChargeToRoomRevenue / data.totalRevenue) * 100) + '%';
    }
    
    // Update Order Stats
    document.getElementById('stat-total-orders').textContent = data.totalDineInOrders + data.totalRoomServiceOrders;
    document.getElementById('stat-dine-in').textContent = data.totalDineInOrders;
    document.getElementById('stat-room-svc').textContent = data.totalRoomServiceOrders;
    
    let servedCount = 0;
    let cancelledCount = 0;
    
    // Map transactions to allOrders
    allOrders = (data.transactions || []).map(t => {
      let type = (t.orderType || '').toLowerCase().includes('room') ? 'room-service' : 'dine-in';
      let loc = type === 'room-service' ? ('Phòng ' + (t.roomName || 'N/A')) : ('Bàn ' + (t.tableName || 'N/A'));
      let statusMap = {
        'PENDING': 'pending',
        'PREPARING': 'preparing',
        'COOKING': 'preparing',
        'READY': 'ready',
        'DELIVERING': 'served',
        'SERVED': 'served',
        'PAID': 'served',
        'COMPLETED': 'served',
        'CANCELLED': 'cancelled',
        'AWAITING_PAYMENT': 'pending'
      };
      let s = statusMap[(t.orderStatus || 'PENDING').toUpperCase()] || 'pending';
      
      if (s === 'served') servedCount++;
      if (s === 'cancelled') cancelledCount++;
      
      let tDate = new Date(t.orderTime);
      let hh = String(tDate.getHours()).padStart(2, '0');
      let mm = String(tDate.getMinutes()).padStart(2, '0');

      return {
        id: 'ORD-' + String(t.orderId).padStart(3, '0'),
        rawId: t.orderId,
        type: type,
        location: loc,
        customer: t.paymentType || 'Khách',
        time: `${hh}:${mm}`,
        timeSort: tDate.getTime(),
        status: s,
        amount: t.totalAmount || 0
      };
    });
    
    document.getElementById('stat-served').textContent = servedCount;
    document.getElementById('stat-cancelled').textContent = cancelledCount;
    
    // Revenue Summary
    let revTotal = data.totalRevenue || 0;
    document.getElementById('rev-cash').textContent = formatAmount(data.totalCashRevenue || 0);
    document.getElementById('rev-vnpay').textContent = formatAmount(data.totalVnpayRevenue || 0);
    document.getElementById('rev-charge').textContent = formatAmount(data.totalChargeToRoomRevenue || 0);
    document.getElementById('rev-total').textContent = formatAmount(revTotal);
    
    document.getElementById('rev-cash-pct').textContent = revTotal > 0 ? Math.round(((data.totalCashRevenue || 0) / revTotal) * 100) + '%' : '0%';
    document.getElementById('rev-vnpay-pct').textContent = revTotal > 0 ? Math.round(((data.totalVnpayRevenue || 0) / revTotal) * 100) + '%' : '0%';
    document.getElementById('rev-charge-pct').textContent = revTotal > 0 ? Math.round(((data.totalChargeToRoomRevenue || 0) / revTotal) * 100) + '%' : '0%';

    // Generate dynamic Hourly Revenue Chart
    let hourMap = {};
    for (let i = 0; i <= 23; i++) {
        let hStr = String(i).padStart(2, '0');
        hourMap[hStr] = 0;
    }
    allOrders.forEach(o => {
        if (o.status === 'served' || o.status === 'ready') {
            let h = o.time.split(':')[0];
            if (hourMap[h] !== undefined) {
                hourMap[h] += o.amount;
            }
        }
    });
    
    // Generate Pie Chart
    renderPieChart(data);
    
    // Sort & Render
    renderTable();
    
  } catch (err) {
    console.error(err);
    showToast('error', 'Error', 'Cannot load daily report data');
  }
}

// Call on load
updateDateUI();
fetchDailyReport();



function formatAmount(n) {
  return n.toLocaleString('vi-VN') + '₫';
}

function statusChip(status) {
  const map = {
    pending:   ['chip-pending',   'Pending', 'schedule'],
    preparing: ['chip-preparing', 'Processing', 'skillet'],
    ready:     ['chip-ready',     'Complete', 'done'],
    served:    ['chip-served',    'Served', 'check_circle'],
    cancelled: ['chip-cancelled', 'Cancelled', 'cancel'],
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
  let rawId = orderId.replace('ORD-', '');
  window.location.href = `/fbStaff/order-detail?id=${parseInt(rawId)}&type=food`;
}

// Initial render
// renderTable(); // Called in fetchDailyReport()

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
    const statusMap = { 'pending': 'Pending', 'preparing': 'Processing', 'ready': 'Complete', 'served': 'Served', 'cancelled': 'Cancelled' };
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

/* ============================================================
   CLOSE DAY LOGIC
   ============================================================ */
function openCloseDayModal() {
  document.getElementById('close-day-notes').value = '';
  document.getElementById('close-day-modal').style.display = 'flex';
}

async function submitCloseDay() {
  const btn = document.getElementById('btn-confirm-close');
  btn.disabled = true;
  btn.innerHTML = `<span class="material-symbols-outlined">hourglass_empty</span> Đang xử lý...`;
  
  const notes = document.getElementById('close-day-notes').value;
  const today = new Date().toISOString().split('T')[0];
  
  try {
    const res = await fetch(`/api/v1/fnb/daily-reports/close?date=${today}&staffId=${CURRENT_STAFF_ID}&notes=${encodeURIComponent(notes)}`, {
      method: 'POST'
    });
    
    if (res.ok) {
      showToast('success', 'Thành công', 'Đã chốt ngày thành công!');
      document.getElementById('close-day-modal').style.display = 'none';
      document.getElementById('btn-close-day').disabled = true;
      document.getElementById('btn-close-day').innerHTML = `<span class="material-symbols-outlined">lock</span> Đã chốt`;
    } else {
      const err = await res.json();
      showToast('error', 'Thất bại', err.message || 'Không thể chốt ngày');
    }
  } catch (error) {
    console.error(error);
    showToast('error', 'Lỗi', 'Có lỗi xảy ra khi kết nối máy chủ');
  } finally {
    btn.disabled = false;
    btn.innerHTML = `<span class="material-symbols-outlined">check_circle</span> Đồng ý chốt`;
  }
}

