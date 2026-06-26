/**
 * TABLE MANAGEMENT SCRIPT
 * Floor Plan view + Timeline View with toggle.
 */

// ─────────────────────────────────────────────
// UTILITIES
// ─────────────────────────────────────────────
function formatTime(t) {
  if (!t) return '';
  return String(t).substring(0, 5); // "19:00:00" → "19:00"
}

function timeToMinutes(t) {
  if (!t) return 0;
  const [h, m] = String(t).split(':').map(Number);
  return h * 60 + m;
}

function minutesToHHMM(mins) {
  const h = Math.floor(mins / 60) % 24;
  const m = mins % 60;
  return `${String(h).padStart(2,'0')}:${String(m).padStart(2,'0')}`;
}

function formatDateVN(dateStr) {
  const d = new Date(dateStr + 'T00:00:00');
  const days = ['CN','Th 2','Th 3','Th 4','Th 5','Th 6','Th 7'];
  return `${days[d.getDay()]}, ${d.getDate().toString().padStart(2,'0')}/${(d.getMonth()+1).toString().padStart(2,'0')}/${d.getFullYear()}`;
}

function todayISO() {
  const d = new Date();
  const yyyy = d.getFullYear();
  const mm   = String(d.getMonth() + 1).padStart(2, '0');
  const dd   = String(d.getDate()).padStart(2, '0');
  return `${yyyy}-${mm}-${dd}`;
}

// ─────────────────────────────────────────────
// SUMMARY BAR — compute counts from DOM
// ─────────────────────────────────────────────
function updateSummaryBar() {
  const tables = document.querySelectorAll('.tm-table');
  let vacant = 0, serving = 0;
  tables.forEach(t => {
    const s = (t.dataset.status || '').toLowerCase();
    if (s === 'available')       vacant++;
    else if (s === 'occupied')   serving++;
    else if (s === 'reserved')   vacant++; // A reserved table is technically still vacant currently
  });
  const el = (id, val) => { const e = document.getElementById(id); if (e) e.textContent = val; };
  el('count-vacant',   vacant);
  el('count-serving',  serving);
}
updateSummaryBar();

// ─────────────────────────────────────────────
// TOAST (reuse shared function from pos-dashboard.js)
// ─────────────────────────────────────────────
function tmShowToast(type, title, message) {
  if (typeof showToast === 'function') {
    showToast(type, title, message);
  } else {
    alert(`${title}: ${message}`);
  }
}

// ─────────────────────────────────────────────
// VIEW TOGGLE
// ─────────────────────────────────────────────
const viewFloor    = document.getElementById('view-floor-plan');
const viewTimeline = document.getElementById('view-timeline');
const btnFloor     = document.getElementById('btn-view-floor');
const btnTimeline  = document.getElementById('btn-view-timeline');

function switchView(target) {
  if (target === 'timeline') {
    viewFloor.style.display    = 'none';
    viewTimeline.classList.add('active');
    btnFloor.classList.remove('active');
    btnTimeline.classList.add('active');
    if (!tlInitialized) initTimeline();
  } else {
    viewFloor.style.display    = '';
    viewTimeline.classList.remove('active');
    btnTimeline.classList.remove('active');
    btnFloor.classList.add('active');
  }
}

if (btnFloor)    btnFloor.addEventListener('click',    () => switchView('floor'));
if (btnTimeline) btnTimeline.addEventListener('click', () => switchView('timeline'));

// ─────────────────────────────────────────────
// FLOOR PLAN — Table click popup
// ─────────────────────────────────────────────
let selectedTableId = null;
const fpPopup = document.getElementById('fp-popup');

document.querySelectorAll('.tm-table').forEach(tableEl => {
  tableEl.addEventListener('click', function (e) {
    e.stopPropagation();
    const tableId  = parseInt(this.dataset.id);
    const tableNum = this.dataset.table;
    const status   = this.dataset.status;
    const orderId  = this.dataset.orderid;
    selectedTableId = tableId;

    // Position popup near the clicked card
    const rect = this.getBoundingClientRect();
    const estimatedPopupHeight = 130;
    let topPx = rect.bottom + window.scrollY + 6;
    
    if (rect.bottom + estimatedPopupHeight > window.innerHeight) {
      topPx = rect.top + window.scrollY - estimatedPopupHeight - 6;
    }
    
    fpPopup.style.top  = `${topPx}px`;
    fpPopup.style.left = `${Math.min(rect.left + window.scrollX, window.innerWidth - 260)}px`;

    // Fill popup content
    document.getElementById('fp-popup-title').textContent = `Bàn ${tableNum}`;

    const actionsEl = document.getElementById('fp-popup-actions');
    actionsEl.innerHTML = '';

    if (status === 'vacant' || status === 'available') {
      actionsEl.innerHTML = `
        <button class="btn btn-primary" style="font-size:0.78rem;height:32px;padding:0 12px;"
          onclick="openDineInModal(${tableId},'${tableNum}')">
          <span class="material-symbols-outlined">chair</span>Dine-In ngay
        </button>
        <button class="btn btn-outline" style="font-size:0.78rem;height:32px;padding:0 12px;"
          onclick="openReserveModal(${tableId},'${tableNum}')">
          <span class="material-symbols-outlined">event</span>Đặt trước
        </button>`;
    } else if (status === 'occupied' || status === 'serving') {
      if (orderId) {
        actionsEl.innerHTML = `
          <button class="btn btn-outline" style="font-size:0.78rem;height:32px;padding:0 12px;"
            onclick="location.href='/fbStaff/order-detail?id=${orderId}&type=food'">
            <span class="material-symbols-outlined">receipt_long</span>Xem đơn
          </button>`;
      } else {
        actionsEl.innerHTML = `
          <button class="btn btn-primary" style="font-size:0.78rem;height:32px;padding:0 12px;"
            onclick="location.href='/fbStaff/create-food-order?tableId=${tableId}'">
            <span class="material-symbols-outlined">restaurant_menu</span>Gọi món
          </button>`;
      }
    } else if (status === 'reserved') {
      actionsEl.innerHTML = `
        <button class="btn btn-outline" style="font-size:0.78rem;height:32px;padding:0 12px;"
          onclick="switchView('timeline')">
          <span class="material-symbols-outlined">calendar_month</span>Xem lịch
        </button>
        <button class="btn btn-primary" style="font-size:0.78rem;height:32px;padding:0 12px;"
          onclick="openReserveModal(${tableId},'${tableNum}')">
          <span class="material-symbols-outlined">event</span>Đặt thêm
        </button>`;
    }

    fpPopup.classList.add('open');
  });
});

// Close popup on outside click
document.addEventListener('click', () => fpPopup && fpPopup.classList.remove('open'));
fpPopup && fpPopup.addEventListener('click', e => e.stopPropagation());

// ─────────────────────────────────────────────
// FLOOR PLAN — Dine-In modal helpers (unchanged)
// ─────────────────────────────────────────────
function openDineInModal(tableId, tableNum) {
  fpPopup.classList.remove('open');
  window.location.href = `/fbStaff/create-food-order?tableId=${tableId}`;
}

// Wire existing close/submit buttons if they exist
const dineInClose = document.getElementById('dineIn-close');
const dineInCancel = document.getElementById('dineIn-cancel-btn');
const dineInSubmit = document.getElementById('dineIn-submit-btn');
const dineInModal  = document.getElementById('dineInModal');

if (dineInClose)  dineInClose.addEventListener('click', () => dineInModal.style.display = 'none');
if (dineInCancel) dineInCancel.addEventListener('click', () => dineInModal.style.display = 'none');
if (dineInModal)  dineInModal.addEventListener('click', e => { if (e.target === dineInModal) dineInModal.style.display='none'; });

if (dineInSubmit) {
  dineInSubmit.addEventListener('click', async () => {
    const customerName = document.getElementById('dineIn-customer').value.trim();
    const pax          = parseInt(document.getElementById('dineIn-pax').value) || 1;
    const note         = document.getElementById('dineIn-note').value.trim();
    if (!customerName) { document.getElementById('dineIn-customer').focus(); return; }

    dineInSubmit.disabled = true;
    dineInSubmit.innerHTML = '<span class="material-symbols-outlined">sync</span> Đang tạo...';

    try {
      const res = await fetch('/api/pos/orders', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ tableId: selectedTableId, guestName: customerName, orderType: 'Dine In', note: note, items: [] })
      });
      if (!res.ok) { const e = await res.json(); throw new Error(e.message); }
      const data = await res.json();
      dineInModal.style.display = 'none';
      tmShowToast('success', 'Thành công', 'Đã tạo đơn Dine-In!');
      setTimeout(() => location.reload(), 900);
    } catch (err) {
      tmShowToast('error', 'Lỗi', err.message || 'Không thể tạo đơn');
      dineInSubmit.disabled = false;
      dineInSubmit.innerHTML = '<span class="material-symbols-outlined">chair</span>Xác nhận Dine-In';
    }
  });
}

function changePax(delta) {
  const inp = document.getElementById('dineIn-pax');
  if (inp) inp.value = Math.max(1, Math.min(20, (parseInt(inp.value) || 2) + delta));
}

// ─────────────────────────────────────────────
// RESERVE MODAL — Đặt bàn trước (Staff)
// ─────────────────────────────────────────────
function openReserveModal(tableId, tableNum, prefillDate, prefillTime) {
  fpPopup && fpPopup.classList.remove('open');
  selectedTableId = tableId;

  const modal = document.getElementById('reserveModal');
  if (!modal) return;

  document.getElementById('rm-table-badge').textContent  = `Bàn ${tableNum}`;
  document.getElementById('rm-table-id').value           = tableId;

  const today = todayISO();
  document.getElementById('rm-date').value               = prefillDate || today;
  document.getElementById('rm-date').min                 = today;
  document.getElementById('rm-start-time').value         = prefillTime || '';
  document.getElementById('rm-end-time').value           = '';
  document.getElementById('rm-pax').value                = '2';
  document.getElementById('rm-customer-name').value      = '';
  document.getElementById('rm-note').value               = '';
  document.getElementById('rm-room-number').value        = '';
  document.getElementById('rm-room-error').style.display = 'none';

  modal.style.display = 'flex';
  setTimeout(() => document.getElementById('rm-room-number').focus(), 50);
}

// Fetch guest info when room number is entered
const rmRoomNumber = document.getElementById('rm-room-number');
const rmCustomerName = document.getElementById('rm-customer-name');
const rmRoomError = document.getElementById('rm-room-error');

if (rmRoomNumber) {
  rmRoomNumber.addEventListener('blur', async () => {
    const roomNumber = rmRoomNumber.value.trim();
    if (!roomNumber) {
      rmCustomerName.value = '';
      rmRoomError.style.display = 'none';
      return;
    }
    
    try {
      const res = await fetch(`/api/rooms/${roomNumber}/info`);
      if (!res.ok) throw new Error('Phòng không tồn tại');
      const data = await res.json();
      if (data.occupied && data.guestName) {
        rmCustomerName.value = data.guestName;
        rmRoomError.style.display = 'none';
      } else {
        rmCustomerName.value = '';
        rmRoomError.textContent = 'Phòng trống, không có khách lưu trú';
        rmRoomError.style.display = 'block';
      }
    } catch (err) {
      rmCustomerName.value = '';
      rmRoomError.textContent = 'Phòng không hợp lệ';
      rmRoomError.style.display = 'block';
    }
  });
}

// Auto-fill endTime = startTime + 2h
const rmStart = document.getElementById('rm-start-time');
const rmEnd   = document.getElementById('rm-end-time');
const rmEndHint = document.getElementById('rm-end-hint');

let fpStart, fpEnd;

if (rmStart && rmEnd) {
  fpEnd = flatpickr(rmEnd, {
    enableTime: true,
    noCalendar: true,
    dateFormat: "H:i",
    time_24hr: true
  });

  fpStart = flatpickr(rmStart, {
    enableTime: true,
    noCalendar: true,
    dateFormat: "H:i",
    time_24hr: true,
    onChange: function(selectedDates, dateStr, instance) {
      if (!dateStr) return;
      const startMins = timeToMinutes(dateStr);
      const endMins   = startMins + 120;
      const endStr = minutesToHHMM(endMins);
      fpEnd.setDate(endStr);
      if (rmEndHint) rmEndHint.textContent = `Mặc định 2 tiếng: đến ${endStr}`;
    }
  });
}

const reserveModal  = document.getElementById('reserveModal');
const rmClose       = document.getElementById('rm-close');
const rmCancel      = document.getElementById('rm-cancel');
const rmSubmit      = document.getElementById('rm-submit');

if (rmClose)  rmClose.addEventListener('click',  () => reserveModal.style.display = 'none');
if (rmCancel) rmCancel.addEventListener('click', () => reserveModal.style.display = 'none');
if (reserveModal) reserveModal.addEventListener('click', e => { if (e.target===reserveModal) reserveModal.style.display='none'; });

if (rmSubmit) {
  rmSubmit.addEventListener('click', async () => {
    const tableId      = document.getElementById('rm-table-id').value;
    const roomNumber   = document.getElementById('rm-room-number').value.trim();
    const customerName = document.getElementById('rm-customer-name').value.trim();
    const date         = document.getElementById('rm-date').value;
    const startTime    = document.getElementById('rm-start-time').value;
    let   endTime      = document.getElementById('rm-end-time').value;
    const pax          = parseInt(document.getElementById('rm-pax').value) || 2;
    const note         = document.getElementById('rm-note').value.trim();

    if (!roomNumber) {
      tmShowToast('error', 'Thiếu thông tin', 'Vui lòng nhập số phòng');
      return;
    }
    if (!customerName) {
      tmShowToast('error', 'Lỗi khách', 'Phòng này không có khách lưu trú');
      return;
    }
    if (!date || !startTime) {
      tmShowToast('error', 'Thiếu thông tin', 'Vui lòng nhập đủ ngày và giờ bắt đầu');
      return;
    }

    const today = todayISO();
    if (date < today) {
      tmShowToast('error', 'Sai ngày', 'Không thể đặt bàn ở ngày trong quá khứ');
      return;
    }
    
    if (date === today) {
      const now = new Date();
      const currentMins = now.getHours() * 60 + now.getMinutes();
      if (timeToMinutes(startTime) < currentMins) {
        tmShowToast('error', 'Sai giờ', 'Không thể chọn giờ bắt đầu trong quá khứ');
        return;
      }
    }

    // Auto-compute endTime if empty
    if (!endTime) {
      endTime = minutesToHHMM(timeToMinutes(startTime) + 120);
    }

    let startMins = timeToMinutes(startTime);
    let endMins = timeToMinutes(endTime);
    if (endMins <= startMins) {
      endMins += 24 * 60;
    }

    if (endMins - startMins > 12 * 60) {
      tmShowToast('error', 'Sai giờ', 'Thời gian đặt bàn quá dài hoặc giờ kết thúc không hợp lệ');
      return;
    }

    rmSubmit.disabled = true;
    rmSubmit.innerHTML = '<span class="material-symbols-outlined">sync</span> Đang lưu...';

    try {
      const res = await fetch('/api/v1/tables/reservations', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          tableId: parseInt(tableId),
          roomNumber,
          customerName,
          reserveDate: date,
          startTime,
          endTime,
          partySize: pax,
          specialRequests: note || null
        })
      });
      if (!res.ok) { const e = await res.json(); throw new Error(e.message); }
      reserveModal.style.display = 'none';
      tmShowToast('success', 'Thành công', 'Đã đặt bàn thành công!');
      // Refresh timeline if currently active
      const tlDateStr = document.getElementById('tl-date-input') && document.getElementById('tl-date-input').value;
      if (date === (tlDateStr || todayISO())) {
        tlCurrentDate = date;
        renderTimeline(date);
      }
      setTimeout(() => location.reload(), 1200);
    } catch (err) {
      tmShowToast('error', 'Lỗi đặt bàn', err.message || 'Vui lòng thử lại');
      rmSubmit.disabled = false;
      rmSubmit.innerHTML = '<span class="material-symbols-outlined">event_available</span> Xác nhận đặt bàn';
    }
  });
}

// ─────────────────────────────────────────────
// TIMELINE VIEW
// ─────────────────────────────────────────────
const TL_START_HOUR = 8;   // 08:00
const TL_END_HOUR   = 23;  // 23:00
const TL_HOUR_PX    = 90;  // pixels per hour
const TL_TOTAL_MINS = (TL_END_HOUR - TL_START_HOUR) * 60;
const TL_OFFSET_MINS = TL_START_HOUR * 60;

let tlInitialized = false;
let tlCurrentDate = todayISO();
let tlData = [];        // array of table objects from API
let tlNowTimer = null;

function tlMinsToPx(mins) {
  return (mins / 60) * TL_HOUR_PX;
}

async function fetchTimelineData(dateStr) {
  const res  = await fetch(`/api/v1/tables/reservations/all?date=${dateStr}`);
  if (!res.ok) throw new Error('Failed to fetch timeline data');
  return res.json();
}

function buildTimeAxis() {
  const axis = document.getElementById('tl-time-axis');
  if (!axis) return;
  axis.innerHTML = '';
  const totalPx = tlMinsToPx(TL_TOTAL_MINS);
  axis.style.minWidth = totalPx + 'px';

  for (let h = TL_START_HOUR; h <= TL_END_HOUR; h++) {
    const offsetPx = tlMinsToPx((h - TL_START_HOUR) * 60);
    const tick = document.createElement('span');
    tick.className = 'tl-time-tick';
    tick.style.left = offsetPx + 'px';
    tick.textContent = `${String(h).padStart(2,'0')}:00`;
    axis.appendChild(tick);
  }
}

function getBlockClass(status) {
  switch ((status || '').toLowerCase()) {
    case 'confirmed': return 'tl-block-pending';
    case 'seated':    return 'tl-block-seated';
    case 'completed': return 'tl-block-completed';
    case 'cancelled': return 'tl-block-cancelled';
    default:          return 'tl-block-pending';
  }
}

function buildRows(tables) {
  const container = document.getElementById('tl-rows-container');
  if (!container) return;
  container.innerHTML = '';

  const totalPx = tlMinsToPx(TL_TOTAL_MINS);

  tables.forEach(table => {
    const row = document.createElement('div');
    row.className = 'tl-row';

    // Label column
    const label = document.createElement('div');
    label.className = 'tl-row-label';
    label.innerHTML = `
      <div class="tl-row-table-num">${table.tableNumber.replace('T','')}</div>
      <div class="tl-row-table-cap"><span class="material-symbols-outlined" style="font-size:0.65rem;">person</span>${table.capacity}</div>`;
    row.appendChild(label);

    // Track
    const track = document.createElement('div');
    track.className = 'tl-row-track';
    // Use min-width for the explicit time bounds
    track.style.minWidth = totalPx + 'px';
    // Draw hour grid lines directly as inline background
    track.style.backgroundImage = `repeating-linear-gradient(to right, rgba(0,0,0,0.08) 0px, rgba(0,0,0,0.08) 1px, transparent 1px, transparent ${TL_HOUR_PX}px)`;
    track.style.backgroundSize  = `${TL_HOUR_PX}px 100%`;

    // Click on empty track → open reserve modal
    track.addEventListener('click', (e) => {
      if (e.target !== track) return; // only bare track clicks
      
      const today = todayISO();
      if (tlCurrentDate < today) {
        tmShowToast('error', 'Không hợp lệ', 'Không thể tạo đơn đặt bàn ở ngày trong quá khứ.');
        return;
      }
      
      const trackRect = track.getBoundingClientRect();
      const clickPx   = e.clientX - trackRect.left;
      const clickMins = Math.round((clickPx / TL_HOUR_PX) * 60 / 30) * 30; // snap to 30min
      const startMins = TL_START_HOUR * 60 + clickMins;
      
      let finalStartMins = startMins;
      if (tlCurrentDate === today) {
        const now = new Date();
        const currentMins = now.getHours() * 60 + now.getMinutes();
        
        // Block only if clicked time is more than an hour in the past
        if (startMins < currentMins - 60) {
          tmShowToast('error', 'Không hợp lệ', 'Không thể đặt trước cho khoảng thời gian trong quá khứ.');
          return;
        }
        
        // If clicked time is slightly in the past but within the hour, snap to nearest future 5-min
        if (startMins < currentMins) {
          finalStartMins = Math.ceil(currentMins / 5) * 5;
        }
      }

      const startHHMM = minutesToHHMM(finalStartMins);
      openReserveModal(table.tableId, table.tableNumber.replace('T',''), tlCurrentDate, startHHMM);
    });

    // Reservation blocks
    (table.reservations || []).forEach(res => {
      const startMins = timeToMinutes(res.reserveTime) - TL_OFFSET_MINS;
      const endMins   = timeToMinutes(res.endTime)     - TL_OFFSET_MINS;
      const durationMins = Math.max(endMins - startMins, 10);

      if (startMins > TL_TOTAL_MINS || endMins < 0) return; // out of view

      const block = document.createElement('div');
      block.className = `tl-block ${getBlockClass(res.status)}`;
      block.style.left  = tlMinsToPx(Math.max(startMins, 0)) + 'px';
      block.style.width = tlMinsToPx(Math.min(durationMins, TL_TOTAL_MINS - Math.max(startMins, 0))) + 'px';
      block.innerHTML = `
        <div class="tl-block-name">${res.customerName}</div>
        <div class="tl-block-meta">${formatTime(res.reserveTime)}–${formatTime(res.endTime)} · ${res.partySize || '?'}👤</div>`;

      block.addEventListener('click', (e) => {
        e.stopPropagation();
        showBlockPopup(e, res, table);
      });

      track.appendChild(block);
    });

    row.appendChild(track);
    container.appendChild(row);
  });

  // Now-line (absolute inside rows-container, spans full height)
  updateNowLine();
}

function updateNowLine() {
  const nowLine = document.getElementById('tl-now-line');
  if (!nowLine) return;

  const now  = new Date();
  const nowMins = now.getHours() * 60 + now.getMinutes() - TL_OFFSET_MINS;

  if (nowMins < 0 || nowMins > TL_TOTAL_MINS) {
    nowLine.style.display = 'none';
    return;
  }
  nowLine.style.display = 'block';
  nowLine.style.left    = tlMinsToPx(nowMins) + 'px';
}

function scrollToNow() {
  const scroll = document.getElementById('tl-gantt-scroll');
  if (!scroll) return;
  const now  = new Date();
  const nowMins = now.getHours() * 60 + now.getMinutes() - TL_OFFSET_MINS;
  if (nowMins < 0) return;
  const targetPx = tlMinsToPx(nowMins) + 90 /* label col */ - scroll.clientWidth / 2;
  scroll.scrollLeft = Math.max(0, targetPx);
}

// Block popup
function showBlockPopup(e, res, table) {
  const popup = document.getElementById('tl-popup');
  if (!popup) return;

  document.getElementById('tl-popup-name').textContent  = res.customerName;
  document.getElementById('tl-popup-table').textContent = table.tableNumber;
  document.getElementById('tl-popup-time').textContent  = `${formatTime(res.reserveTime)} – ${formatTime(res.endTime)}`;
  document.getElementById('tl-popup-pax').textContent   = `${res.partySize || '?'} người`;
  document.getElementById('tl-popup-status').textContent = res.status;

  const reasonRow = document.getElementById('tl-popup-reason-row');
  const reasonSpan = document.getElementById('tl-popup-reason');
  if (res.status === 'Cancelled' && res.specialRequests) {
    reasonSpan.textContent = res.specialRequests;
    reasonRow.style.display = 'flex';
  } else {
    reasonRow.style.display = 'none';
  }

  const actions = document.getElementById('tl-popup-actions');
  actions.innerHTML = '';

  if (res.status === 'Confirmed' || res.status === 'Pending') {
    actions.innerHTML = `
      <button class="btn btn-outline" style="font-size:0.75rem;height:30px;padding:0 10px;margin-right:5px;"
        onclick="openHoldModal(${res.id})">
        <span class="material-symbols-outlined">timer</span>Giữ bàn
      </button>
      <button class="btn btn-primary" style="font-size:0.75rem;height:30px;padding:0 10px;"
        onclick="openCccdCheckInModal(${res.id}, ${table.tableId})">
        <span class="material-symbols-outlined">how_to_reg</span>Check-in
      </button>`;
  } else if (res.status === 'Seated') {
    actions.innerHTML = `
      <button class="btn btn-outline" style="font-size:0.75rem;height:30px;padding:0 10px;"
        onclick="handleOrderFood(${table.tableId}, '${encodeURIComponent(res.customerName)}')">
        <span class="material-symbols-outlined">restaurant_menu</span>Gọi món
      </button>`;
  }

  // Position popup near mouse, avoid overflow
  const px = Math.min(e.clientX + 10, window.innerWidth  - 310);
  const py = Math.min(e.clientY - 10, window.innerHeight - 220);
  popup.style.left = px + 'px';
  popup.style.top  = py + 'px';
  popup.classList.add('open');
}

const tlPopup = document.getElementById('tl-popup');
const tlPopupClose = document.getElementById('tl-popup-close');
if (tlPopupClose) tlPopupClose.addEventListener('click', () => tlPopup.classList.remove('open'));
document.addEventListener('click', () => tlPopup && tlPopup.classList.remove('open'));
tlPopup && tlPopup.addEventListener('click', e => e.stopPropagation());

// Render timeline for a date
async function renderTimeline(dateStr) {
  const loading = document.getElementById('tl-loading');
  if (loading) loading.style.display = 'flex';

  try {
    tlData = await fetchTimelineData(dateStr);
    buildTimeAxis();
    buildRows(tlData);
  } catch (err) {
    console.error('Timeline error:', err);
    tmShowToast('error', 'Lỗi', 'Không thể tải dữ liệu timeline');
  } finally {
    if (loading) loading.style.display = 'none';
  }

  // Update date label
  const label = document.getElementById('tl-date-label');
  if (label) {
    const isToday = dateStr === todayISO();
    label.textContent = isToday ? `Hôm nay — ${formatDateVN(dateStr)}` : formatDateVN(dateStr);
  }
}

function initTimeline() {
  tlInitialized = true;
  tlCurrentDate = todayISO();

  const dateInput = document.getElementById('tl-date-input');
  if (dateInput) dateInput.value = tlCurrentDate;

  renderTimeline(tlCurrentDate).then(() => scrollToNow());

  // Update now-line every 60s
  if (tlNowTimer) clearInterval(tlNowTimer);
  tlNowTimer = setInterval(updateNowLine, 60000);
}

// Date navigation
document.getElementById('tl-prev-day') && document.getElementById('tl-prev-day').addEventListener('click', () => {
  const d = new Date(tlCurrentDate + 'T00:00:00');
  d.setDate(d.getDate() - 1);
  tlCurrentDate = `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`;
  document.getElementById('tl-date-input').value = tlCurrentDate;
  renderTimeline(tlCurrentDate);
});

document.getElementById('tl-next-day') && document.getElementById('tl-next-day').addEventListener('click', () => {
  const d = new Date(tlCurrentDate + 'T00:00:00');
  d.setDate(d.getDate() + 1);
  tlCurrentDate = `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`;
  document.getElementById('tl-date-input').value = tlCurrentDate;
  renderTimeline(tlCurrentDate);
});

document.getElementById('tl-today-btn') && document.getElementById('tl-today-btn').addEventListener('click', () => {
  tlCurrentDate = todayISO();
  document.getElementById('tl-date-input').value = tlCurrentDate;
  renderTimeline(tlCurrentDate).then(() => scrollToNow());
});

document.getElementById('tl-date-input') && document.getElementById('tl-date-input').addEventListener('change', (e) => {
  tlCurrentDate = e.target.value;
  renderTimeline(tlCurrentDate);
});

// ─────────────────────────────────────────────
// CHECK-IN & ORDER FOOD (shared)
// ─────────────────────────────────────────────
async function handleCheckIn(reservationId, tableId) {
  if (!confirm('Xác nhận khách đã đến?')) return;
  try {
    const res = await fetch(`/api/v1/tables/reservations/${reservationId}/check-in`, { method: 'POST' });
    if (res.ok) {
      tmShowToast('success', 'Thành công', 'Check-in khách thành công!');
      tlPopup && tlPopup.classList.remove('open');
      if (tlInitialized) renderTimeline(tlCurrentDate);
      
      // Update Floor Plan DOM manually without reloading
      const gridTable = document.querySelector(`.tm-table[data-id="${tableId}"]`);
      if (gridTable) {
        gridTable.dataset.status = 'occupied';
        const labelEl = gridTable.querySelector('.tm-table-status-label');
        if (labelEl) labelEl.textContent = 'Có khách';
        updateSummaryBar();
      }
    } else {
      const err = await res.json();
      tmShowToast('error', 'Lỗi', err.message || 'Check-in thất bại');
    }
  } catch (e) {
    tmShowToast('error', 'Lỗi', 'Không thể kết nối máy chủ');
  }
}

function handleOrderFood(tableId, customerNameEnc) {
  let url = `/fbStaff/create-food-order?tableId=${tableId}`;
  if (customerNameEnc) url += `&customerName=${customerNameEnc}`;
  window.location.href = url;
}



// ─────────────────────────────────────────────
// CCCD CHECK-IN MODAL LOGIC
// ─────────────────────────────────────────────

function openCccdCheckInModal(resId, tableId) {
  tlPopup.classList.remove('open'); // close timeline popup
  document.getElementById('cccdCheckIn-resId').value = resId;
  document.getElementById('cccdCheckIn-tableId').value = tableId;
  
  // reset fields
  document.getElementById('cccdCheckIn-cccd').value = '';
  document.getElementById('cccdCheckIn-name').value = '';
  document.getElementById('cccdCheckIn-room').value = '';
  document.getElementById('cccdCheckIn-error').style.display = 'none';
  document.getElementById('cccdCheckIn-submit').disabled = true;

  document.getElementById('cccdCheckInModal').classList.add('open');
}

function closeCccdCheckInModal() {
  document.getElementById('cccdCheckInModal').classList.remove('open');
}

async function searchCccdInfo() {
  const cccd = document.getElementById('cccdCheckIn-cccd').value.trim();
  const errorEl = document.getElementById('cccdCheckIn-error');
  const submitBtn = document.getElementById('cccdCheckIn-submit');
  const nameEl = document.getElementById('cccdCheckIn-name');
  const roomEl = document.getElementById('cccdCheckIn-room');

  if (!cccd) {
    nameEl.value = '';
    roomEl.value = '';
    submitBtn.disabled = true;
    errorEl.style.display = 'none';
    return;
  }

  try {
    const res = await fetch('/api/rooms/by-cccd?cccd=' + encodeURIComponent(cccd));
    if (res.ok) {
      const data = await res.json();
      nameEl.value = data.guestName || 'Không rõ';
      roomEl.value = data.roomNumber || 'Không rõ';
      errorEl.style.display = 'none';
      submitBtn.disabled = false;
    } else {
      nameEl.value = '';
      roomEl.value = '';
      submitBtn.disabled = true;
      errorEl.textContent = 'Không tìm thấy phòng đang lưu trú với CCCD này.';
      errorEl.style.display = 'block';
    }
  } catch (err) {
    nameEl.value = '';
    roomEl.value = '';
    submitBtn.disabled = true;
    errorEl.textContent = 'Lỗi kết nối máy chủ.';
    errorEl.style.display = 'block';
  }
}

function submitCccdCheckIn() {
  const resId = document.getElementById('cccdCheckIn-resId').value;
  const tableId = document.getElementById('cccdCheckIn-tableId').value;
  closeCccdCheckInModal();
  handleCheckIn(resId, tableId);
}

// ─────────────────────────────────────────────
// HOLD RESERVATION MODAL LOGIC
// ─────────────────────────────────────────────
function openHoldModal(resId) {
  tlPopup.classList.remove('open');
  document.getElementById('hold-resId').value = resId;
  const holdMinutesInput = document.getElementById('hold-minutes');
  holdMinutesInput.value = '';
  
  if (!holdMinutesInput._flatpickr) {
    flatpickr(holdMinutesInput, {
      enableTime: true,
      noCalendar: true,
      time_24hr: true,
      defaultDate: "00:15",
      minTime: "00:00",
      maxTime: "00:30",
      minuteIncrement: 1,
      onOpen: function(selectedDates, dateStr, instance) {
        if (instance.calendarContainer) {
          instance.calendarContainer.classList.add('hide-hours');
        }
      }
    });
  }
  
  document.getElementById('holdModal').style.display = 'flex';
}

async function submitHoldReservation() {
  const resId = document.getElementById('hold-resId').value;
  const timeStr = document.getElementById('hold-minutes').value;
  if (!timeStr) {
    tmShowToast('error', 'Lỗi', 'Vui lòng chọn số phút');
    return;
  }
  
  const holdMinutes = parseInt(timeStr.split(':')[1], 10);
  if (isNaN(holdMinutes) || holdMinutes < 0 || holdMinutes > 30) {
    tmShowToast('error', 'Lỗi', 'Số phút không hợp lệ');
    return;
  }
  
  const btn = document.getElementById('hold-submit');
  btn.disabled = true;
  btn.innerHTML = '<span class="material-symbols-outlined">sync</span> Đang xử lý...';
  
  try {
    const res = await fetch(`/api/v1/tables/reservations/${resId}/hold`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ holdMinutes })
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Lỗi hệ thống');
    
    tmShowToast('success', 'Thành công', data.message);
    document.getElementById('holdModal').style.display = 'none';
    if (tlInitialized) renderTimeline(tlCurrentDate);
  } catch (err) {
    tmShowToast('error', 'Lỗi', err.message);
  } finally {
    btn.disabled = false;
    btn.innerHTML = '<span class="material-symbols-outlined">save</span>Xác nhận';
  }
}

// ─────────────────────────────────────────────
// OVERSTAY ALERTS (2-Stage Warning)
// ─────────────────────────────────────────────
const ALERT_AUDIO = new Audio('https://actions.google.com/sounds/v1/alarms/beep_short.ogg');
ALERT_AUDIO.playbackRate = 2.0; // Tăng tốc độ phát x2 để tiếng kêu gấp gáp hơn
let lastAlertedTables = new Set();

async function updateOverstayAlerts() {
  try {
    const res = await fetch(`/api/v1/tables/reservations/all?date=${todayISO()}`);
    if (!res.ok) return;
    const data = await res.json();
    if (!data || !Array.isArray(data)) return;

    let totalReservationsToday = 0;
    data.forEach(tableData => {
      // Only count reservations that are not cancelled
      totalReservationsToday += (tableData.reservations || []).filter(r => r.status !== 'Cancelled').length;
    });
    const reservedCountEl = document.getElementById('count-reserved');
    if (reservedCountEl) {
      reservedCountEl.textContent = totalReservationsToday;
    }

    const now = new Date();
    const nowMins = now.getHours() * 60 + now.getMinutes();

    let playSound = false;
    let currentAlertedTables = new Set();

    data.forEach(tableData => {
      const tableId = tableData.tableId;
      const reservations = tableData.reservations || [];
      const tableEl = document.querySelector(`.tm-table[data-id="${tableId}"]`);
      if (!tableEl) return;

      const status = tableEl.dataset.status;
      
      // Reset classes
      tableEl.classList.remove('tm-warning-time', 'tm-alert-time');

      if (status === 'occupied' || status === 'serving') {
        // Find the active Seated reservation
        const seatedRes = reservations.find(r => r.status === 'Seated');
        
        if (seatedRes && seatedRes.endTime) {
          const endMins = timeToMinutes(seatedRes.endTime);
          
          if (nowMins >= endMins - 15 && nowMins <= endMins) {
             // Stage 1: Warning (15 mins left)
             tableEl.classList.add('tm-warning-time');
          } else if (nowMins > endMins) {
             // Stage 2: Alert (Overstayed)
             // Check for conflict: any Pending or Confirmed reservation today subsequent to this one
             const conflict = reservations.find(r => 
                 (r.status === 'Pending' || r.status === 'Confirmed') && 
                 timeToMinutes(r.reserveTime) >= endMins
             );
             
             if (conflict) {
                 tableEl.classList.add('tm-alert-time');
                 currentAlertedTables.add(tableId);
                 if (!lastAlertedTables.has(tableId)) {
                     playSound = true;
                 }
             }
          }
        }
      }
    });

    if (playSound) {
       let playCount = 0;
       const maxPlays = 5;
       const playHandler = () => {
           playCount++;
           if (playCount < maxPlays) {
               ALERT_AUDIO.play().catch(e => console.log('Autoplay blocked:', e));
           } else {
               ALERT_AUDIO.removeEventListener('ended', playHandler);
           }
       };
       // Avoid memory leak by removing old listeners if any
       ALERT_AUDIO.removeEventListener('ended', playHandler);
       ALERT_AUDIO.addEventListener('ended', playHandler);
       ALERT_AUDIO.play().catch(e => console.log('Autoplay blocked:', e));
    }
    lastAlertedTables = currentAlertedTables;

  } catch (err) {
    console.error('Failed to update overstay alerts:', err);
  }
}

// Run immediately and every 60 seconds
updateOverstayAlerts();
setInterval(updateOverstayAlerts, 60000);
