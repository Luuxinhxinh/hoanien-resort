/* dashboard.js — Tổng quan: bar chart, donut chart, line chart */

const C_ROOM = '#C9A96E', C_FNB = '#7A9C7A', C_TOUR = '#6B8C9A';
const C_GRID = 'rgba(44,42,30,0.07)', C_TICK = '#8B7355';

Chart.defaults.font.family = "'DM Sans',sans-serif";
Chart.defaults.font.size = 12;
Chart.defaults.color = C_TICK;
Chart.defaults.plugins.legend.display = false;
Chart.defaults.plugins.tooltip.backgroundColor = 'rgba(28,26,20,0.92)';
Chart.defaults.plugins.tooltip.titleColor = '#F5F0E8';
Chart.defaults.plugins.tooltip.bodyColor = '#C9A96E';
Chart.defaults.plugins.tooltip.padding = 10;
Chart.defaults.plugins.tooltip.cornerRadius = 8;
Chart.defaults.plugins.tooltip.displayColors = false;

document.addEventListener('DOMContentLoaded', () => {
    initLineToday(); initDonut(); initLine();
});

function initLineToday() {
    const ctx = document.getElementById('chart-line-today');
    if (!ctx) return;
    const d = window.todayData || { labels: [], room: [], fnb: [], tour: [] };
    
    // Đăng ký plugin datalabels nếu có (tuỳ chọn)
    let plugins = [];
    if (typeof ChartDataLabels !== 'undefined') {
        Chart.register(ChartDataLabels);
        plugins = [ChartDataLabels];
    }
    
    new Chart(ctx, {
        type: 'line', 
        data: {
            labels: d.labels, 
            datasets: [
                { 
                    label: 'Phòng', 
                    data: d.room, 
                    borderColor: '#C9A96E', 
                    backgroundColor: 'rgba(201,169,110,0.08)', 
                    borderWidth: 2, 
                    pointBackgroundColor: '#C9A96E',
                    pointRadius: 4, 
                    pointHoverRadius: 6, 
                    tension: 0.2, 
                    fill: true 
                },
                { 
                    label: 'F&B', 
                    data: d.fnb, 
                    borderColor: '#7A9C7A', 
                    backgroundColor: 'rgba(122,156,122,0.08)', 
                    borderWidth: 2, 
                    pointBackgroundColor: '#7A9C7A',
                    pointRadius: 4, 
                    pointHoverRadius: 6, 
                    tension: 0.2, 
                    fill: true 
                },
                { 
                    label: 'Tour', 
                    data: d.tour, 
                    borderColor: '#6B8C9A', 
                    backgroundColor: 'rgba(107,140,154,0.08)', 
                    borderWidth: 2, 
                    pointBackgroundColor: '#6B8C9A',
                    pointRadius: 4, 
                    pointHoverRadius: 6, 
                    tension: 0.2, 
                    fill: true 
                }
            ]
        }, 
        plugins: plugins,
        options: { 
            responsive: true, 
            maintainAspectRatio: false, 
            plugins: { 
                legend: { display: false }, 
                tooltip: { callbacks: { label: c => ` ${c.dataset.label}: ${c.parsed.y}M` } },
                datalabels: {
                    align: 'top',
                    font: { weight: 'bold', size: 11 },
                    color: function(context) {
                        return context.dataset.borderColor;
                    },
                    formatter: function(value) { return value > 0 ? value + 'M' : ''; }
                }
            }, 
            scales: { 
                x: { grid: { display: false }, border: { display: false }, ticks: { color: C_TICK } }, 
                y: { min: 0, grid: { color: C_GRID }, border: { display: false }, ticks: { color: C_TICK, callback: v => v + 'M' } } 
            } 
        }
    });
}

function initDonut() {
    const ctx = document.getElementById('chart-donut');
    if (!ctx) return;
    const d = window.donutData || { room: 0, fnb: 0, tour: 0 };
    new Chart(ctx, { type: 'doughnut', data: { labels: ['Phòng', 'F&B', 'Tour'], datasets: [{ data: [d.room, d.fnb, d.tour], backgroundColor: [C_ROOM, C_FNB, C_TOUR], borderWidth: 0, hoverOffset: 6 }] }, options: { responsive: true, maintainAspectRatio: false, cutout: '68%', plugins: { legend: { display: false }, tooltip: { callbacks: { label: c => ` ${c.label}: ${c.parsed}M` } } } } });
}

function initLine() {
    const ctx = document.getElementById('chart-line');
    if (!ctx) return;
    const d = window.occupancyData || { labels: [], data: [] };
    const labels = d.labels.map((l, i) => i % 5 === 0 ? l : '');
    new Chart(ctx, { type: 'line', data: { labels, datasets: [{ label: 'Occupancy', data: d.data, borderColor: C_ROOM, backgroundColor: 'rgba(201,169,110,0.08)', borderWidth: 2, pointRadius: 0, pointHoverRadius: 4, tension: 0.4, fill: true }] }, options: { responsive: true, maintainAspectRatio: false, interaction: { mode: 'index', intersect: false }, plugins: { legend: { display: false }, tooltip: { callbacks: { title: i => `Ngày ${d.labels[i[0].dataIndex]}`, label: c => ` Lấp đầy: ${c.parsed.y}%` } } }, scales: { x: { grid: { display: false }, border: { display: false }, ticks: { color: C_TICK, maxRotation: 0 } }, y: { min: 0, max: 100, grid: { color: C_GRID }, border: { display: false }, ticks: { color: C_TICK, callback: v => v + '%', stepSize: 10 } } } } });
}

function toggleSubmenu(id, arrowId) {
    const s = document.getElementById(id), a = document.getElementById(arrowId);
    if (!s || !a) return;
    const open = s.classList.contains('open');
    document.querySelectorAll('.mgr-submenu').forEach(e => e.classList.remove('open'));
    document.querySelectorAll('.mgr-nav-arrow').forEach(e => e.classList.remove('open'));
    if (!open) { s.classList.add('open'); a.classList.add('open'); }
}
