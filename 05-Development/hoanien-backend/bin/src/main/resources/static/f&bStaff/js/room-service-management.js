/**
 * ROOM SERVICE MANAGEMENT - MOCK LOGIC
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

  // Status Filter Logic
  const filterPills = document.querySelectorAll('#status-filters .pill');
  const tableRows = document.querySelectorAll('#rs-tbody tr');

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
    });
  }
});
