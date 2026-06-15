/**
 * HOANIEN RESORT & TOUR HUB — F&B Staff E-Menu
 * e-menu.js  (statics/f&bStaff/js/)
 */

/* ============================================================
   LIVE CLOCK
   ============================================================ */
(function initClock() {
  const pad = n => String(n).padStart(2, '0');
  const days   = ['CN','T2','T3','T4','T5','T6','T7'];
  const months = ['01','02','03','04','05','06','07','08','09','10','11','12'];

  function tick() {
    const now = new Date();
    const clockEl = document.getElementById('live-clock');
    const dateEl  = document.getElementById('live-date');
    if (clockEl) clockEl.textContent = `${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`;
    if (dateEl)  dateEl.textContent  = `${days[now.getDay()]}, ${now.getDate()}/${months[now.getMonth()]}/${now.getFullYear()}`;
  }
  setInterval(tick, 1000);
  tick();
})();

const MENU_ITEMS = window.SERVER_MENU_ITEMS || [];

/* ============================================================
   STATE
   ============================================================ */
let activeCategory = 'all';
let activeStatus   = 'all';
let searchQuery    = '';
let isListView     = false;

/* ============================================================
   RENDER
   ============================================================ */
function getFilteredItems() {
  return MENU_ITEMS.filter(item => {
    const matchCat    = activeCategory === 'all' || item.cat === activeCategory;
    const matchStatus = activeStatus   === 'all' || item.status === activeStatus;
    const matchSearch = !searchQuery   ||
      item.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      item.desc.toLowerCase().includes(searchQuery.toLowerCase()) ||
      item.catLabel.toLowerCase().includes(searchQuery.toLowerCase());
    return matchCat && matchStatus && matchSearch;
  });
}

function formatPrice(n) {
  return n.toLocaleString('vi-VN') + '₫';
}

function statusLabel(s) {
  return { available: 'Còn món', 'out-of-stock': 'Hết món', 'low-stock': 'Sắp hết' }[s] || s;
}
function statusClass(s) {
  return { available: 'available', 'out-of-stock': 'out-of-stock', 'low-stock': 'low-stock' }[s] || '';
}

function renderMenu() {
  const grid = document.getElementById('menu-grid');
  const countEl = document.getElementById('results-count');
  if (!grid) return;

  const items = getFilteredItems();
  if (countEl) countEl.textContent = items.length;

  if (items.length === 0) {
    grid.innerHTML = `
      <div class="empty-state">
        <span class="material-symbols-outlined">search_off</span>
        <div class="empty-state-title">Không tìm thấy món nào</div>
        <div class="empty-state-sub">Thử tìm kiếm với từ khóa khác hoặc thay đổi bộ lọc</div>
      </div>`;
    return;
  }

  grid.innerHTML = items.map(item => `
    <div class="menu-card ${item.status === 'out-of-stock' ? 'out-of-stock' : ''}"
         data-id="${item.id}" data-cat="${item.cat}" data-status="${item.status}">

      <!-- Image area -->
      <div class="menu-card-image">
        <div class="food-icon-bg" style="background: linear-gradient(135deg, ${item.bgFrom}, ${item.bgTo});"></div>
        <div class="food-icon-wrapper">
          <span class="material-symbols-outlined" style="color:${item.iconColor};">${item.icon}</span>
        </div>
        <div class="avail-badge ${statusClass(item.status)}">
          <span class="badge-dot"></span>
          ${statusLabel(item.status)}
        </div>
        <div class="card-hover-overlay">
          <button class="overlay-add-btn btn-add-to-order" data-id="${item.id}" ${item.status === 'out-of-stock' ? 'disabled style="opacity:0.5;cursor:not-allowed;"' : ''}>
            <span class="material-symbols-outlined">add_shopping_cart</span>
            Thêm vào đơn
          </button>
        </div>
      </div>

      <!-- Card body -->
      <div class="menu-card-body">
        <div class="menu-card-top-row">
          <span class="cat-chip cat-${item.cat}">${item.catLabel}</span>
        </div>
        <div class="menu-item-name">${item.name}</div>
        <div class="menu-item-desc">${item.desc}</div>

        <div class="menu-card-footer">
          <span class="menu-price">${formatPrice(item.price)}</span>
          <div class="card-actions">
            <button class="btn-toggle btn-status-toggle" data-id="${item.id}" title="Đổi trạng thái">
              <span class="material-symbols-outlined">${item.status === 'available' ? 'toggle_on' : 'toggle_off'}</span>
            </button>
            <button class="btn-add btn-add-to-order" data-id="${item.id}" ${item.status === 'out-of-stock' ? 'disabled style="opacity:0.5;cursor:not-allowed;"' : ''} title="Thêm vào đơn">
              <span class="material-symbols-outlined">add</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  `).join('');

  // Bind events after render
  bindCardEvents();
}

/* ============================================================
   BIND CARD EVENTS
   ============================================================ */
function bindCardEvents() {
  // Add to order
  document.querySelectorAll('.btn-add-to-order:not([disabled])').forEach(btn => {
    btn.addEventListener('click', function (e) {
      e.stopPropagation();
      const id   = parseInt(this.dataset.id);
      const item = MENU_ITEMS.find(i => i.id === id);
      if (!item) return;
      showToast('success', 'Đã thêm vào đơn', `${item.name} · ${formatPrice(item.price)}`);
    });
  });

  // Toggle status
  document.querySelectorAll('.btn-status-toggle').forEach(btn => {
    btn.addEventListener('click', function (e) {
      e.stopPropagation();
      const id   = parseInt(this.dataset.id);
      const item = MENU_ITEMS.find(i => i.id === id);
      if (!item) return;

      const newStatus = (item.status === 'available' || item.status === 'low-stock') ? 'out-of-stock' : 'available';
      const isAvailable = newStatus === 'available';

      fetch(`/api/menu-items/${id}/toggle?isAvailable=${isAvailable}`, { method: 'POST' })
        .then(response => {
            if (response.ok) {
                item.status = newStatus;
                if (newStatus === 'out-of-stock') {
                    showToast('warning', 'Đã đánh dấu Hết món', item.name);
                } else {
                    showToast('success', 'Đã khôi phục Còn món', item.name);
                }
                renderMenu();
            } else {
                showToast('error', 'Lỗi', 'Không thể cập nhật trạng thái món ăn!');
            }
        })
        .catch(err => {
            console.error(err);
            showToast('error', 'Lỗi', 'Không thể kết nối đến máy chủ!');
        });
    });
  });
}

/* ============================================================
   CATEGORY PILLS
   ============================================================ */
document.querySelectorAll('.cat-pill').forEach(pill => {
  pill.addEventListener('click', function () {
    document.querySelectorAll('.cat-pill').forEach(p => p.classList.remove('active'));
    this.classList.add('active');
    activeCategory = this.dataset.cat;
    renderMenu();
  });
});

// Update category counts
function updateCatCounts() {
  document.querySelectorAll('.cat-pill[data-cat]').forEach(pill => {
    const cat   = pill.dataset.cat;
    const count = cat === 'all' ? MENU_ITEMS.length : MENU_ITEMS.filter(i => i.cat === cat).length;
    const el    = pill.querySelector('.cat-count');
    if (el) el.textContent = count;
  });
}
updateCatCounts();

/* ============================================================
   STATUS FILTER BUTTONS
   ============================================================ */
document.querySelectorAll('.filter-btn[data-status]').forEach(btn => {
  btn.addEventListener('click', function () {
    document.querySelectorAll('.filter-btn[data-status]').forEach(b => {
      b.classList.remove('active', 'active-avail', 'active-out');
    });
    const s = this.dataset.status;
    activeStatus = s;
    if (s === 'all') this.classList.add('active');
    else if (s === 'available') this.classList.add('active-avail');
    else this.classList.add('active-out');
    renderMenu();
  });
});

/* ============================================================
   SEARCH
   ============================================================ */
const searchInput = document.getElementById('menu-search');
searchInput?.addEventListener('input', function () {
  searchQuery = this.value.trim();
  renderMenu();
});

// Clear search
document.getElementById('search-clear')?.addEventListener('click', function () {
  if (searchInput) { searchInput.value = ''; searchQuery = ''; renderMenu(); }
});

/* ============================================================
   VIEW TOGGLE (Grid / List)
   ============================================================ */
document.getElementById('btn-grid-view')?.addEventListener('click', function () {
  isListView = false;
  const grid = document.getElementById('menu-grid');
  grid?.classList.remove('list-view');
  this.classList.add('active');
  document.getElementById('btn-list-view')?.classList.remove('active');
});

document.getElementById('btn-list-view')?.addEventListener('click', function () {
  isListView = true;
  const grid = document.getElementById('menu-grid');
  grid?.classList.add('list-view');
  this.classList.add('active');
  document.getElementById('btn-grid-view')?.classList.remove('active');
});

/* ============================================================
   TOAST
   ============================================================ */
function showToast(type, title, message, duration = 3500) {
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
   INITIAL RENDER
   ============================================================ */
renderMenu();
