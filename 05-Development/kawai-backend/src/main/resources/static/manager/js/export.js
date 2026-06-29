/* export.js - report export form handling */

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

async function handleExport() {
    const type = document.getElementById('export-type')?.value || 'revenue';
    const from = document.getElementById('export-from')?.value;
    const to   = document.getElementById('export-to')?.value;

    if (!from || !to) {
        alert('Vui long chon khoang thoi gian truoc khi xuat bao cao.');
        return;
    }
    if (from > to) {
        alert('Tu ngay phai nho hon hoac bang Den ngay.');
        return;
    }

    const typeLabels = {
        revenue: 'Doanh thu tong hop',
        room: 'Bao cao phong',
        fnb: 'Bao cao F&B',
        tour: 'Bao cao Tour',
        occupancy: 'Ty le lap day',
        stay: 'Thoi gian luu tru'
    };
    const label = typeLabels[type] || type;

    const btn = document.getElementById('btn-export');
    if (btn) btn.disabled = true;
    try {
        const response = await fetch(`/manager/api/reports/export?type=${encodeURIComponent(type)}&from=${from}&to=${to}&format=${selectedFormat}`);
        if (!response.ok) {
            throw new Error(await response.text() || 'Khong the xuat bao cao.');
        }
        const blob = await response.blob();
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `${label}-${from}-${to}.${selectedFormat === 'xlsx' ? 'csv' : selectedFormat}`;
        document.body.appendChild(a);
        a.click();
        a.remove();
        URL.revokeObjectURL(url);
    } catch (err) {
        alert(err.message || 'Khong the xuat bao cao.');
    } finally {
        if (btn) btn.disabled = false;
    }
}

function toggleSubmenu(id, arrowId) {
    const s = document.getElementById(id);
    const a = document.getElementById(arrowId);
    if (!s || !a) return;
    const open = s.classList.contains('open');
    document.querySelectorAll('.mgr-submenu').forEach(e => e.classList.remove('open'));
    document.querySelectorAll('.mgr-nav-arrow').forEach(e => e.classList.remove('open'));
    if (!open) {
        s.classList.add('open');
        a.classList.add('open');
    }
}
