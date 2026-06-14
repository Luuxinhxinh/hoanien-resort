/**
 * TABLE MANAGEMENT SCRIPT
 * Separated from pos-dashboard.js
 */

/* ============================================================
   TABLE MANAGEMENT — Stats Counter
   ============================================================ */
function updateTMCounts() {
  const counts = { vacant: 0, reserved: 0, serving: 0 };
  document.querySelectorAll('.tm-table[data-status]').forEach(t => {
    if (counts[t.dataset.status] !== undefined) counts[t.dataset.status]++;
  });
  const set = (id, val) => { const el = document.getElementById(id); if (el) el.textContent = val; };
  set('tm-count-vacant',   counts.vacant);
  set('tm-count-reserved', counts.reserved);
  set('tm-count-serving',  counts.serving);
}

// Initial count
window.addEventListener('DOMContentLoaded', updateTMCounts);

/* ============================================================
   TABLE MANAGEMENT — Zone Filter Pills
   ============================================================ */
document.querySelectorAll('.tm-zone-filter .pill[data-zone]').forEach(pill => {
  pill.addEventListener('click', function () {
    this.closest('.tm-zone-filter').querySelectorAll('.pill').forEach(p => p.classList.remove('active'));
    this.classList.add('active');
    const zone = this.dataset.zone;
    document.querySelectorAll('.tm-zone[data-zone]').forEach(z => {
      z.style.display = (zone === 'all' || z.dataset.zone === zone) ? '' : 'none';
    });
  });
});

/* ============================================================
   TABLE MANAGEMENT — Tooltip injection
   ============================================================ */
document.querySelectorAll('.tm-table').forEach(table => {
  const status   = table.dataset.status;
  const tableNum = table.dataset.table;
  const cap      = table.dataset.capacity;
  const guest    = table.dataset.guest   || '';
  const pax      = table.dataset.pax     || '';
  const time     = table.dataset.time    || '';

  let tipHTML = `<strong>Bàn ${tableNum}</strong> · Sức chứa: ${cap} người`;
  if (status === 'serving')  tipHTML += `<br>${guest} · ${pax} khách · từ ${time}`;
  if (status === 'reserved') tipHTML += `<br>${guest} · Đặt lúc ${time}`;

  const tip = document.createElement('div');
  tip.className = 'tm-table-tooltip';
  tip.innerHTML = tipHTML;
  table.appendChild(tip);
});

/* ============================================================
   TABLE MANAGEMENT — Dine-In Modal
   ============================================================ */
const dineInModal   = document.getElementById('dineInModal');
const dineInForm    = document.getElementById('dineInForm');
const dineInClose   = document.getElementById('dineIn-close');
const dineInCancelBtn = document.getElementById('dineIn-cancel-btn');
const dineInSubmitBtn = document.getElementById('dineIn-submit-btn');

function openDineInModal(tableEl) {
  if (!dineInModal || !tableEl) return;

  const tableNum  = tableEl.dataset.table;
  const cap       = tableEl.dataset.capacity || '?';

  // Populate fields
  const badge = document.getElementById('dineIn-table-badge');
  if (badge) badge.textContent = `Bàn ${tableNum}`;
  const numInput = document.getElementById('dineIn-table-num');
  if (numInput) numInput.value = tableNum;
  const paxInput = document.getElementById('dineIn-pax');
  if (paxInput) paxInput.value = 2;
  const noteInput = document.getElementById('dineIn-note');
  if (noteInput) noteInput.value = '';
  const custInput = document.getElementById('dineIn-customer');
  if (custInput) custInput.value = '';

  // Preview
  const previewName   = document.getElementById('dineIn-preview-name');
  const previewDetail = document.getElementById('dineIn-preview-detail');
  if (previewName)   previewName.textContent   = `Bàn ${tableNum}`;
  if (previewDetail) previewDetail.textContent = `Sức chứa: ${cap} người`;

  // Store reference for submission
  dineInModal._tableEl = tableEl;

  dineInModal.classList.add('open');
  document.body.style.overflow = 'hidden';
  setTimeout(() => { if (custInput) custInput.focus(); }, 100);
}

function closeDineInModal() {
  if (!dineInModal) return;
  dineInModal.classList.remove('open');
  document.body.style.overflow = '';
  dineInModal._tableEl = null;
}

// Open when clicking a vacant table
document.querySelectorAll('.tm-table').forEach(table => {
  table.addEventListener('click', function () {
    if (this.dataset.status === 'vacant') {
      openDineInModal(this);
    } else if (this.dataset.status === 'serving') {
      const guest = this.dataset.guest || '—';
      const pax   = this.dataset.pax   || '—';
      const time  = this.dataset.time  || '—';
      if (typeof showToast === 'function') showToast('info', `Bàn ${this.dataset.table} — Đang phục vụ`, `${guest} · ${pax} khách · từ ${time}`);
    } else if (this.dataset.status === 'reserved') {
      const guest = this.dataset.guest || '—';
      const time  = this.dataset.time  || '—';
      if (typeof showToast === 'function') showToast('info', `Bàn ${this.dataset.table} — Đã đặt trước`, `${guest} · ${time}`);

    }
  });
});

// "Đặt bàn mới" quick button
const btnQuickDineIn = document.getElementById('btn-quick-dine-in');
if (btnQuickDineIn) {
  btnQuickDineIn.addEventListener('click', () => {
    // Find first vacant table
    const first = document.querySelector('.tm-table[data-status="vacant"]');
    if (first) openDineInModal(first);
    else if (typeof showToast === 'function') showToast('warning', 'Không có bàn trống', 'Tất cả bàn hiện đang bận hoặc đang dọn.');
  });
}

// Close actions
if (dineInClose)     dineInClose.addEventListener('click', closeDineInModal);
if (dineInCancelBtn) dineInCancelBtn.addEventListener('click', closeDineInModal);
dineInModal?.addEventListener('click', e => { if (e.target === dineInModal) closeDineInModal(); });

// Submit
if (dineInSubmitBtn) {
  dineInSubmitBtn.addEventListener('click', () => {
    const customer = document.getElementById('dineIn-customer')?.value?.trim();
    const pax      = document.getElementById('dineIn-pax')?.value;
    const tableNum = document.getElementById('dineIn-table-num')?.value;
    const note     = document.getElementById('dineIn-note')?.value?.trim();

    if (!customer) {
      document.getElementById('dineIn-customer')?.focus();
      if (typeof showToast === 'function') showToast('warning', 'Thiếu thông tin', 'Vui lòng nhập tên khách hàng.');
      return;
    }

    // Update table status to serving
    const tableEl = dineInModal._tableEl;
    if (tableEl) {
      tableEl.dataset.status = 'serving';
      tableEl.dataset.guest  = customer;
      tableEl.dataset.pax    = pax;
      const now = new Date();
      tableEl.dataset.time   = `${String(now.getHours()).padStart(2,'0')}:${String(now.getMinutes()).padStart(2,'0')}`;
      const lbl = tableEl.querySelector('.tm-table-status-label');
      if (lbl) lbl.textContent = 'Đang phục vụ';
    }

    updateTMCounts();
    closeDineInModal();

    const noteMsg = note ? ` · Ghi chú: ${note}` : '';
    if (typeof showToast === 'function') showToast('success', 'Đặt bàn thành công', `Bàn ${tableNum} — ${customer} · ${pax} khách${noteMsg}`);
  });
}
