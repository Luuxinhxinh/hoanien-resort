/**
 * utils.js — File tiện ích dùng chung cho hệ thống Guest
 */

// ── Toast Notification ────────────────────────────────────────────────────────

window.showToast = function(message, type = 'success') {
    // Tìm hoặc tạo container nếu chưa có
    let container = document.getElementById('toastContainer');
    if (!container) {
        // Fallback to co-toast-container (payment.js) if exists, or create a new one
        container = document.getElementById('co-toast-container');
        if (!container) {
            container = document.createElement('div');
            container.id = 'toastContainer';
            container.className = 'fixed bottom-4 left-1/2 -translate-x-1/2 z-[9999] flex flex-col gap-2 pointer-events-none';
            document.body.appendChild(container);
        }
    }

    const toast = document.createElement('div');
    
    // Unified style combining booking.js and payment.js styles
    toast.className = `min-w-[320px] max-w-md bg-white border-l-4 shadow-xl p-4 flex items-center justify-between pointer-events-auto transform translate-y-[-20px] opacity-0 transition-all duration-300 ${type === 'success' ? 'border-green-600 text-green-800' : 'border-red-600 text-red-800'}`;

    const icon = type === 'success' ? 'check_circle' : 'error';
    const iconColor = type === 'success' ? 'text-green-600' : 'text-red-600';
    
    toast.innerHTML = `
        <div class="flex items-center gap-3">
            <span class="material-symbols-outlined text-xl ${iconColor}">${icon}</span>
            <span class="text-sm font-medium text-[#2c2a24]">${message}</span>
        </div>
        <button onclick="this.parentElement.remove()" class="text-gray-400 hover:text-gray-600 active:scale-95 ml-4 flex items-center">
            <span class="material-symbols-outlined text-sm">close</span>
        </button>
    `;

    container.appendChild(toast);

    // Animate in
    requestAnimationFrame(() => {
        setTimeout(() => {
            toast.classList.remove('translate-y-[-20px]', 'opacity-0');
        }, 50);
    });

    // Auto dismiss after 4 seconds
    setTimeout(() => {
        toast.classList.add('opacity-0', 'translate-y-[-20px]');
        setTimeout(() => {
            toast.remove();
        }, 300);
    }, 4000);
};

// ── Formatting Utilities ──────────────────────────────────────────────────────

window.formatCurrency = function(amount, withSymbol = false) {
    if (amount == null) return '—';
    const val = Number(amount).toLocaleString('vi-VN');
    return withSymbol ? val + ' ₫' : val;
};
