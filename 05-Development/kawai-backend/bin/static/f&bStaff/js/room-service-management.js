/**
 * ROOM SERVICE MANAGEMENT - MOCK LOGIC & DYNAMIC SLA
 */

document.addEventListener('DOMContentLoaded', () => {
  // Update time display
  const timeDisplay = document.getElementById('time-display');
  if (timeDisplay) {
    const updateTime = () => {
      const now = new Date();
      const h = String(now.getHours()).padStart(2, '0');
      const m = String(now.getMinutes()).padStart(2, '0');
      const s = String(now.getSeconds()).padStart(2, '0');
      timeDisplay.textContent = `${h}:${m}:${s}`;
    };
    updateTime();
    setInterval(updateTime, 1000);
  }

  // SLA Timer Logic
  const updateSLA = () => {
    const timers = document.querySelectorAll('.sla-timer');
    const now = new Date();
    timers.forEach(timer => {
      const orderTimeIso = timer.dataset.ordertime;
      const etaMins = parseInt(timer.dataset.etamins) || 15;
      if (!orderTimeIso) return;
      
      const orderDate = new Date(orderTimeIso);
      // Expected completion time
      const expectedTime = new Date(orderDate.getTime() + etaMins * 60000);
      
      const diffMs = expectedTime - now;
      const row = timer.closest('tr');
      const status = row.dataset.status;
      
      // Nếu món đã được phục vụ (served), dừng đếm ngược và hiển thị Completed
      if (status === 'served') {
        timer.textContent = 'Completed';
        timer.style.color = 'var(--status-ready-text)';
        row.style.border = '';
        return;
      }
      
      // Nếu vẫn còn thời gian (diffMs > 0)
      if (diffMs > 0) {
        const diffMins = Math.floor(diffMs / 60000);
        const diffSecs = Math.floor((diffMs % 60000) / 1000);
        timer.textContent = `Còn lại ${String(diffMins).padStart(2, '0')}:${String(diffSecs).padStart(2, '0')}`;
        
        if (diffMins > 10) {
            timer.style.color = '#2e7d32'; // Green (An toàn)
        } else {
            timer.style.color = '#e65100'; // Orange (Sắp trễ, cần chú ý)
        }
        row.style.border = '';
      } else {
        // Đã quá giờ ETA (Trễ SLA)
        const overMs = Math.abs(diffMs);
        const overMins = Math.floor(overMs / 60000);
        const overSecs = Math.floor((overMs % 60000) / 1000);
        timer.textContent = `LATE -${String(overMins).padStart(2, '0')}:${String(overSecs).padStart(2, '0')}`;
        timer.style.color = '#d32f2f'; // Red
        
        // Hiệu ứng Flash border: Nhấp nháy viền đỏ mỗi nửa giây để gây chú ý
        const msNow = now.getTime();
        if (msNow % 1000 < 500) {
            row.style.outline = '2px solid #d32f2f';
        } else {
            row.style.outline = 'none';
        }
      }
    });
  };
  setInterval(updateSLA, 1000);
  updateSLA();

  // Status Filter Logic
  const filterPills = document.querySelectorAll('#status-filters .pill');
  const tableRows = document.querySelectorAll('tr.rs-row');
  const headers = document.querySelectorAll('tr.floor-group-header');

  const updateHeaders = () => {
    // Hide floor header if no rows are visible under it
    headers.forEach(header => {
      let next = header.nextElementSibling;
      let hasVisible = false;
      while (next && next.classList.contains('rs-row')) {
        if (next.style.display !== 'none') {
          hasVisible = true;
          break;
        }
        next = next.nextElementSibling;
      }
      header.style.display = hasVisible ? 'table-row' : 'none';
    });
  };

  filterPills.forEach(pill => {
    pill.addEventListener('click', function() {
      // Update active state
      filterPills.forEach(p => p.classList.remove('active'));
      this.classList.add('active');

      const filterValue = this.dataset.filter;

      // Filter rows
      tableRows.forEach(row => {
        if (filterValue === 'all') {
          row.style.display = 'table-row';
        } else {
          if (row.dataset.status === filterValue) {
            row.style.display = 'table-row';
          } else {
            row.style.display = 'none';
          }
        }
      });
      updateHeaders();
    });
  });

  // Search Logic
  const searchInput = document.getElementById('search-input');
  if (searchInput) {
    searchInput.addEventListener('keyup', function() {
      const term = this.value.toLowerCase();
      
      // Keep track of active pill to preserve filter
      const activeFilter = document.querySelector('#status-filters .pill.active').dataset.filter;

      tableRows.forEach(row => {
        const text = row.textContent.toLowerCase();
        const matchesSearch = text.includes(term);
        const matchesFilter = (activeFilter === 'all') || (row.dataset.status === activeFilter);
        
        if (matchesSearch && matchesFilter) {
          row.style.display = 'table-row';
        } else {
          row.style.display = 'none';
        }
      });
      updateHeaders();
    });
  }
});

function updateStatus(orderId, newStatus) {
    if (!confirm('Xác nhận chuyển trạng thái đơn hàng này?')) return;
    
    fetch(`/api/pos/orders/${orderId}/status?status=${newStatus}`, {
        method: 'PUT'
    })
    .then(response => {
        if (response.ok) {
            const row = document.querySelector(`tr.rs-row[data-orderid="RS-${orderId}"]`);
            if (row) {
                updateRowUI(row, newStatus);
            }
        } else {
            alert('Có lỗi xảy ra khi cập nhật.');
        }
    });
}

function updateRowUI(row, newStatus) {
    row.dataset.status = newStatus;
    const statusCol = row.querySelector('.col-status');
    const actionCol = row.querySelector('.col-action');
    const orderId = row.dataset.orderid.replace('RS-', '');
    
    if (newStatus === 'delivering') {
        statusCol.innerHTML = `<span class="order-status-chip chip-delivering" style="background:#e0f7fa; color:#006064; border-color:#006064;"><span class="dot" style="background:#006064;"></span>Delivering</span>`;
        actionCol.innerHTML = `<button class="btn btn-sm btn-primary" data-id="${orderId}" onclick="updateStatus(this.getAttribute('data-id'), 'served')"><span class="material-symbols-outlined" style="font-size:1rem; margin-right:4px;">check_circle</span> Xác nhận giao</button>`;
    } else if (newStatus === 'served') {
        statusCol.innerHTML = `<span class="order-status-chip chip-served"><span class="dot"></span>Served</span>`;
        actionCol.innerHTML = ``;
        const timer = row.querySelector('.sla-timer');
        if (timer) {
            timer.textContent = 'Completed';
            timer.style.color = 'var(--status-ready-text)';
            row.style.border = '';
            row.style.outline = 'none';
        }
    }
    
    // Re-apply active filter to hide row if it no longer matches
    const activePill = document.querySelector('#status-filters .pill.active');
    if (activePill) {
        activePill.click();
    }
}

function dispatchFloor(floorName) {
    if (!confirm(`Bạn có chắc chắn muốn giao tất cả khay đang ở trạng thái Ready của ${floorName}?`)) return;
    
    // Tìm tất cả các dòng đơn hàng thuộc Tầng này và đang ở trạng thái Ready (Bếp đã làm xong)
    const rows = document.querySelectorAll(`tr.rs-row[data-floor="${floorName}"][data-status="ready"]`);
    if (rows.length === 0) {
        alert('Không có đơn nào đang Ready ở tầng này!');
        return;
    }
    
    // Bóc tách lấy mảng các Order ID (Loại bỏ tiền tố 'RS-')
    const ids = Array.from(rows).map(row => {
        return row.dataset.orderid.replace('RS-', '');
    });
    
    // Gửi mảng ID xuống Backend API để đổi trạng thái hàng loạt sang 'delivering'
    fetch('/api/pos/batch-update-status', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            orderIds: ids,
            newStatus: 'delivering'
        })
    })
    .then(response => {
        if (response.ok) {
            rows.forEach(row => {
                updateRowUI(row, 'delivering');
            });
        } else {
            alert('Có lỗi xảy ra khi cập nhật hàng loạt.');
        }
    });
}
