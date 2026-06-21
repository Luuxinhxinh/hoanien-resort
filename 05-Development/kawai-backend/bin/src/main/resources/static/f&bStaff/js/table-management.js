/**
 * TABLE MANAGEMENT SCRIPT
 * Split-screen table selection and Dine-in creation.
 */

// Format time utility
function formatTime(timeString) {
  if (!timeString) return '';
  return timeString.substring(0, 5); // "19:00:00" -> "19:00"
}

let selectedTableId = null;

// Handle click on table
document.querySelectorAll('.tm-table').forEach(table => {
  table.addEventListener('click', async function () {
    const tableIdStr = this.dataset.table; // "01", "02", etc
    const capacity = this.dataset.capacity;
    const tableId = parseInt(this.dataset.id);
    
    selectedTableId = tableId;

    // Highlight selected table
    document.querySelectorAll('.tm-table').forEach(t => t.classList.remove('selected-table'));
    this.classList.add('selected-table');

    // Activate split screen
    const container = document.querySelector('.tm-container');
    container.classList.add('split-active');
    
    // Update panel header
    document.getElementById('panel-table-name').textContent = `Bàn ${tableIdStr}`;
    document.getElementById('panel-table-cap').textContent = `Sức chứa: ${capacity} người`;

    // Fetch reservations
    const scheduleList = document.getElementById('panel-schedule-list');
    scheduleList.innerHTML = `<div style="text-align:center;padding:20px 0;color:var(--text-muted);font-size:0.85rem;"><span class="material-symbols-outlined" style="animation: spin 1s linear infinite;">sync</span> Đang tải...</div>`;
    
    try {
      const response = await fetch(`/api/v1/tables/${tableId}/reservations`);
      if (!response.ok) throw new Error("Failed to load");
      const data = await response.json();
      
      if (!data || data.length === 0) {
        scheduleList.innerHTML = `<div style="color:var(--text-muted);font-size:0.85rem;text-align:center;padding:20px 0;">Không có lịch đặt nào hôm nay</div>`;
      } else {
        scheduleList.innerHTML = '';
        data.forEach(res => {
          const startTime = formatTime(res.reserveTime);
          const endTime = formatTime(res.endTime);
          
          const item = document.createElement('div');
          item.className = 'schedule-item';
          item.innerHTML = `
            <div class="schedule-item-time">${startTime} - ${endTime}</div>
            <div class="schedule-item-detail">
              <strong>${res.customerName}</strong> · ${res.partySize} khách
              ${res.specialRequests ? `<br><span style="font-size:0.75rem;opacity:0.8">Ghi chú: ${res.specialRequests}</span>` : ''}
            </div>
          `;
          scheduleList.appendChild(item);
        });
      }
    } catch (err) {
      console.error(err);
      scheduleList.innerHTML = `<div style="color:#b03232;font-size:0.85rem;text-align:center;padding:20px 0;">Lỗi tải dữ liệu</div>`;
    }
  });
});



// Remove unused functions and modal code.
