document.addEventListener('DOMContentLoaded', () => {
    // Set current date once (no timer)
    const dateEl = document.getElementById('currentDate');
    if (dateEl) {
        const dateOptions = { weekday: 'long', month: 'long', day: 'numeric', year: 'numeric' };
        dateEl.textContent = new Date().toLocaleDateString('en-US', dateOptions);
    }

    // Simple interaction for select inputs
    const selects = document.querySelectorAll('select');
    selects.forEach(select => {
        select.addEventListener('change', (e) => {
            console.log(`Filter changed to: ${e.target.value}`);

        });
    });

    // Add tooltip/toast for some buttons just for interaction feel
    const notificationBtn = document.querySelector('.notification-btn');
    if (notificationBtn) {
        notificationBtn.addEventListener('click', () => {
            alert('You have 3 new notifications.');
        });
    }
});

function toggleCategory(headerElement) {
    const grid = headerElement.nextElementSibling;
    const icon = headerElement.querySelector('.toggle-icon');

    if (grid.classList.contains('expanded')) {
        grid.classList.remove('expanded');
        icon.style.transform = "rotate(0deg)";
        // Reset to 1 row height
        grid.style.maxHeight = "142px";
    } else {
        grid.classList.add('expanded');
        icon.style.transform = "rotate(180deg)";
        // Set to exact scroll height for smooth animation
        grid.style.maxHeight = grid.scrollHeight + "px";

        // Optional: reset to 'none' after animation so it adapts to window resize
        setTimeout(() => {
            if (grid.classList.contains('expanded')) {
                grid.style.maxHeight = "none";
            }
        }, 400);
    }
}

let currentFilter = null;
function toggleFilter(filterClass, legendElement) {
    const allLegendItems = document.querySelectorAll('.legend-item');
    const allRooms = document.querySelectorAll('.room-card');

    // If clicking the already active filter, clear it (show all)
    if (currentFilter === filterClass) {
        currentFilter = null;
        allLegendItems.forEach(el => el.style.opacity = '1');
        allRooms.forEach(room => room.style.display = 'flex'); // Original display is flex/grid in css
        return;
    }

    // Set new filter
    currentFilter = filterClass;

    // Update legend UI (dim others)
    allLegendItems.forEach(el => el.style.opacity = '0.3');
    legendElement.style.opacity = '1';

    // Filter rooms
    allRooms.forEach(room => {
        if (room.classList.contains(filterClass)) {
            room.style.display = 'flex';
        } else {
            room.style.display = 'none';
        }
    });

    // Automatically expand all categories so filtered rooms are visible
    const grids = document.querySelectorAll('.room-grid');
    grids.forEach(grid => {
        const headerElement = grid.previousElementSibling;
        const icon = headerElement.querySelector('.toggle-icon');
        if (!grid.classList.contains('expanded')) {
            grid.classList.add('expanded');
            icon.style.transform = "rotate(180deg)";
            grid.style.maxHeight = grid.scrollHeight + "px";
            setTimeout(() => { if (grid.classList.contains('expanded')) grid.style.maxHeight = "none"; }, 400);
        }
    });
}
