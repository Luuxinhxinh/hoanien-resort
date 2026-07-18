/* analytics-stay.js — Thời gian lưu trú TB: bar chart distribution */

const C_ROOM = 'rgba(201,169,110,0.85)';
const C_GRID = 'rgba(44,42,30,0.07)', C_TICK = '#6a505bcf';

Chart.defaults.font.family = "'DM Sans',sans-serif";
Chart.defaults.font.size = 12; Chart.defaults.color = C_TICK;
Chart.defaults.plugins.legend.display = false;
Chart.defaults.plugins.tooltip.backgroundColor = 'rgba(28,26,20,0.92)';
Chart.defaults.plugins.tooltip.titleColor = '#F5F0E8';
Chart.defaults.plugins.tooltip.bodyColor = '#C9A96E';
Chart.defaults.plugins.tooltip.padding = 10;
Chart.defaults.plugins.tooltip.cornerRadius = 8;

document.addEventListener('DOMContentLoaded', () => {
    const ctx = document.getElementById('chart-stay-bar');
    if (!ctx) return;
    const d = window.stayDistData || { labels: [], values: [] };
    new Chart(ctx, { type: 'bar', data: { labels: d.labels, datasets: [{ label: 'Lượt khách', data: d.values, backgroundColor: C_ROOM, borderRadius: 4, borderSkipped: false }] }, options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { display: false }, tooltip: { callbacks: { label: c => ` ${c.parsed.y} lượt khách` } } }, scales: { x: { grid: { display: false }, border: { display: false }, ticks: { color: C_TICK } }, y: { grid: { color: C_GRID }, border: { display: false }, ticks: { color: C_TICK, callback: v => v + ' lượt' } } } } });
});

function toggleSubmenu(id, arrowId) {
    const s = document.getElementById(id), a = document.getElementById(arrowId);
    if (!s || !a) return;
    const open = s.classList.contains('open');
    document.querySelectorAll('.mgr-submenu').forEach(e => e.classList.remove('open'));
    document.querySelectorAll('.mgr-nav-arrow').forEach(e => e.classList.remove('open'));
    if (!open) { s.classList.add('open'); a.classList.add('open'); }
}
