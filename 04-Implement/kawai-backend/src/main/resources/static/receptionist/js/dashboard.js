document.addEventListener('DOMContentLoaded', () => {
    function updateDateTime() {
        const timeEl = document.getElementById('currentTime');
        const dateEl = document.getElementById('currentDate');
        
        if (!timeEl || !dateEl) return;

        const now = new Date();
        
        // Format Time (e.g., 10:47:24 AM)
        let timeString = now.toLocaleTimeString('en-US');
        timeEl.textContent = timeString;

        // Format Date (e.g., Wednesday, June 10, 2026)
        const dateOptions = { weekday: 'long', month: 'long', day: 'numeric', year: 'numeric' };
        dateEl.textContent = now.toLocaleDateString('en-US', dateOptions);
    }

    // Update time immediately, then every second
    updateDateTime();
    setInterval(updateDateTime, 1000);

    // Simple interaction for select inputs
    const selects = document.querySelectorAll('select');
    selects.forEach(select => {
        select.addEventListener('change', (e) => {
            console.log(`Filter changed to: ${e.target.value}`);
            // In a real app, this would trigger an API call or DOM filtering
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
