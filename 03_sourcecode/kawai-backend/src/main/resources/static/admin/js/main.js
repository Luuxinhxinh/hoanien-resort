// Common JavaScript helper functions for Kawai Retreat Operations Hub

// Initialize Lucide Icons after the DOM is fully loaded
document.addEventListener("DOMContentLoaded", () => {
    if (typeof lucide !== 'undefined') {
        lucide.createIcons();
    }
});

// Modal Management Helper
const ModalManager = {
    open(modalId) {
        const modal = document.getElementById(modalId);
        if (modal) {
            modal.style.display = 'flex';
            document.body.style.overflow = 'hidden';
        }
    },

    close(modalId) {
        const modal = document.getElementById(modalId);
        if (modal) {
            modal.style.display = 'none';
            document.body.style.overflow = 'unset';
        }
    }
};

// Toast notification simple implementation (optional helper)
function showToast(message, type = 'success') {
    const toastContainer = document.getElementById('toast-container');
    if (!toastContainer) return;

    const toast = document.createElement('div');
    toast.className = `flex items-center gap-2 px-4 py-3 rounded-lg text-sm font-medium shadow-lg transition-all duration-300 transform translate-y-2 opacity-0`;

    if (type === 'success') {
        toast.className += ' bg-green-100 text-green-800 border border-green-200';
    } else {
        toast.className += ' bg-red-100 text-red-800 border border-red-200';
    }

    toast.innerHTML = `
    <span>${message}</span>
  `;

    toastContainer.appendChild(toast);

    // Animate in
    setTimeout(() => {
        toast.classList.remove('translate-y-2', 'opacity-0');
    }, 10);

    // Remove after 3 seconds
    setTimeout(() => {
        toast.classList.add('translate-y-2', 'opacity-0');
        setTimeout(() => {
            toast.remove();
        }, 300);
    }, 3000);
}

