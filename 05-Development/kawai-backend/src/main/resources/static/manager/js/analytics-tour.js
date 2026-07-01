/* analytics-tour.js — Tỷ lệ tour: donut chart */

const COLORS=['#C9A96E','#7A9C7A','#6B8C9A','#A89060','#D17C6B','#9B8B6B','#5A7D7C','#B59B7D','#6A8F70','#8D7E61','#4A6B80','#D49E7C'];

Chart.defaults.font.family = "'DM Sans',sans-serif";
Chart.defaults.font.size = 12;
Chart.defaults.plugins.legend.display = false;
Chart.defaults.plugins.tooltip.backgroundColor = 'rgba(28,26,20,0.92)';
Chart.defaults.plugins.tooltip.titleColor = '#F5F0E8';
Chart.defaults.plugins.tooltip.bodyColor = '#C9A96E';
Chart.defaults.plugins.tooltip.padding = 10;
Chart.defaults.plugins.tooltip.cornerRadius = 8;

document.addEventListener('DOMContentLoaded', () => {
    const ctx = document.getElementById('chart-tour-donut');
    if (!ctx) return;
    const d = window.tourDonutData || { labels: [], values: [] };
    new Chart(ctx, { type: 'doughnut', data: { labels: d.labels, datasets: [{ data: d.values, backgroundColor: COLORS, borderWidth: 0, hoverOffset: 6 }] }, options: { responsive: true, maintainAspectRatio: false, cutout: '70%', plugins: { legend: { display: false }, tooltip: { callbacks: { label: c => ` ${c.label}: ${c.parsed} lượt` } } } } });
});

function toggleSubmenu(id, arrowId) {
    const s = document.getElementById(id), a = document.getElementById(arrowId);
    if (!s || !a) return;
    const open = s.classList.contains('open');
    document.querySelectorAll('.mgr-submenu').forEach(e => e.classList.remove('open'));
    document.querySelectorAll('.mgr-nav-arrow').forEach(e => e.classList.remove('open'));
    if (!open) { s.classList.add('open'); a.classList.add('open'); }
}
