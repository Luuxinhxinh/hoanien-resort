/* export.js — Xuất báo cáo: form handling + format selection */

let selectedFormat = 'xlsx';

document.addEventListener('DOMContentLoaded', () => {
    setDefaultDates();
});

function setDefaultDates() {
    const today = new Date();
    const firstDay = new Date(today.getFullYear(), today.getMonth(), 1);
    const fmt = d => d.toISOString().split('T')[0];
    const fromEl = document.getElementById('export-from');
    const toEl = document.getElementById('export-to');
    if (fromEl) fromEl.value = fmt(firstDay);
    if (toEl) toEl.value = fmt(today);
}

function selectFormat(fmt) {
    selectedFormat = fmt;
    document.querySelectorAll('.mgr-format-chip').forEach(el => el.classList.remove('active'));
    const chip = document.getElementById('fmt-' + fmt);
    if (chip) chip.classList.add('active');
}

function handleExport() {
    const type = document.getElementById('export-type')?.value || 'revenue';
    const from = document.getElementById('export-from')?.value;
    const to = document.getElementById('export-to')?.value;

    if (!from || !to) {
        alert('Vui lòng chọn khoảng thời gian trước khi xuất báo cáo.');
        return;
    }
    if (from > to) {
        alert('"Từ ngày" phải nhỏ hơn hoặc bằng "Đến ngày".');
        return;
    }

    const typeLabels = {
        revenue: 'Doanh thu tổng hợp',
        room: 'Báo cáo phòng',
        fnb: 'Báo cáo F&B',
        tour: 'Báo cáo Tour',
        occupancy: 'Tỷ lệ lấp đầy',
        stay: 'Thời gian lưu trú'
    };
    const label = typeLabels[type] || type;

    // TODO: Thay bằng API call thực khi có backend
    // fetch(`/manager/api/export?type=${type}&from=${from}&to=${to}&format=${selectedFormat}`)
    console.log('[Export]', { type, from, to, format: selectedFormat });
    alert(`✓ Đang xuất: "${label}"\n   Từ: ${from}  →  Đến: ${to}\n   Định dạng: .${selectedFormat}\n\n(Mock — chưa kết nối backend)`);
}

function toggleSubmenu(id, arrowId) {
    const s = document.getElementById(id);
    const a = document.getElementById(arrowId);
    if (!s || !a) return;
    const open = s.classList.contains('open');
    document.querySelectorAll('.mgr-submenu').forEach(e => e.classList.remove('open'));
    document.querySelectorAll('.mgr-nav-arrow').forEach(e => e.classList.remove('open'));
    if (!open) { s.classList.add('open'); a.classList.add('open'); }
}
