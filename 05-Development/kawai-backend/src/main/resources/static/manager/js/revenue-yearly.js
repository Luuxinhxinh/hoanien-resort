/* revenue-yearly.js — Doanh thu theo năm: grouped bar chart */

(() => {
const C_ROOM='rgba(201,169,110,0.85)',C_FNB='rgba(122,156,122,0.85)',C_TOUR='rgba(107,140,154,0.85)';
const C_GRID='rgba(44,42,30,0.07)',C_TICK='#8B7355';

Chart.defaults.font.family="'DM Sans',sans-serif";
Chart.defaults.font.size=12; Chart.defaults.color=C_TICK;
Chart.defaults.plugins.legend.display=false;
Chart.defaults.plugins.tooltip.backgroundColor='rgba(28,26,20,0.92)';
Chart.defaults.plugins.tooltip.titleColor='#F5F0E8';
Chart.defaults.plugins.tooltip.bodyColor='#C9A96E';
Chart.defaults.plugins.tooltip.padding=10;
Chart.defaults.plugins.tooltip.cornerRadius=8;

document.addEventListener('DOMContentLoaded',()=>{
    const ctx=document.getElementById('chart-yearly-bar');
    if(!ctx)return;
    const d=window.yearlyChartData||{labels:["2024","2025","2026 YTD"],room:[32.0,38.0,19.5],fnb:[11.0,13.0,6.4],tour:[6.2,7.8,3.6]};
    new Chart(ctx,{type:'bar',data:{labels:d.labels,datasets:[
        {label:'Phòng',data:d.room,backgroundColor:C_ROOM,borderRadius:4,borderSkipped:false},
        {label:'F&B',data:d.fnb,backgroundColor:C_FNB,borderRadius:4,borderSkipped:false},
        {label:'Tour',data:d.tour,backgroundColor:C_TOUR,borderRadius:4,borderSkipped:false}
    ]},options:{responsive:true,maintainAspectRatio:false,plugins:{legend:{display:false},tooltip:{callbacks:{label:c=>` ${c.dataset.label}: ${c.parsed.y} Tỷ`}}},scales:{x:{grid:{display:false},border:{display:false},ticks:{color:C_TICK}},y:{grid:{color:C_GRID},border:{display:false},ticks:{color:C_TICK,callback:v=>v+' Tỷ'}}}}});
})();
});


}
