/* revenue-monthly.js — Doanh thu theo tháng: multi-line chart */

(() => {
const C_ROOM='#C9A96E',C_FNB='#7A9C7A',C_TOUR='#6B8C9A';
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
    const ctx=document.getElementById('chart-monthly-line');
    if(!ctx)return;
    const d=window.monthlyChartData||{labels:["T1","T2","T3","T4","T5","T6"],room:[3.20,2.80,3.50,3.80,4.10,1.13],fnb:[1.10,0.94,1.20,1.30,1.40,0.49],tour:[0.62,0.51,0.68,0.72,0.79,0.23]};
    new Chart(ctx,{type:'line',data:{labels:d.labels,datasets:[
        {label:'Phòng',data:d.room,borderColor:C_ROOM,backgroundColor:'rgba(201,169,110,0.08)',borderWidth:2.5,pointRadius:4,pointBackgroundColor:C_ROOM,tension:0.3,fill:false},
        {label:'F&B',data:d.fnb,borderColor:C_FNB,backgroundColor:'rgba(122,156,122,0.08)',borderWidth:2.5,pointRadius:4,pointBackgroundColor:C_FNB,tension:0.3,fill:false},
        {label:'Tour',data:d.tour,borderColor:C_TOUR,backgroundColor:'rgba(107,140,154,0.08)',borderWidth:2.5,pointRadius:4,pointBackgroundColor:C_TOUR,tension:0.3,fill:false}
    ]},options:{responsive:true,maintainAspectRatio:false,interaction:{mode:'index',intersect:false},plugins:{legend:{display:false},tooltip:{callbacks:{label:c=>` ${c.dataset.label}: ${c.parsed.y} Tỷ`}}},scales:{x:{grid:{display:false},border:{display:false},ticks:{color:C_TICK}},y:{grid:{color:C_GRID},border:{display:false},ticks:{color:C_TICK,callback:v=>v+' Tỷ'}}}}});
})();
});


}
