/* revenue-daily.js — Doanh thu theo ngày: stacked bar chart */

const C_ROOM='rgba(201,169,110,0.85)',C_FNB='rgba(122,156,122,0.85)',C_TOUR='rgba(107,140,154,0.85)';
const C_GRID='rgba(44,42,30,0.07)',C_TICK='#6a505bcf';

Chart.defaults.font.family="'DM Sans',sans-serif";
Chart.defaults.font.size=12;
Chart.defaults.color=C_TICK;
Chart.defaults.plugins.legend.display=false;
Chart.defaults.plugins.tooltip.backgroundColor='rgba(28,26,20,0.92)';
Chart.defaults.plugins.tooltip.titleColor='#F5F0E8';
Chart.defaults.plugins.tooltip.bodyColor='#C9A96E';
Chart.defaults.plugins.tooltip.padding=10;
Chart.defaults.plugins.tooltip.cornerRadius=8;
Chart.defaults.plugins.tooltip.displayColors=false;

document.addEventListener('DOMContentLoaded',()=>{
    const ctx=document.getElementById('chart-daily-bar');
    if(!ctx)return;
    const d=window.dailyChartData||{labels:[],room:[],fnb:[],tour:[]};
    new Chart(ctx,{type:'bar',data:{labels:d.labels,datasets:[
        {label:'Phòng',data:d.room,backgroundColor:C_ROOM,borderRadius:{topLeft:0,topRight:0,bottomLeft:3,bottomRight:3},borderSkipped:false,stack:'rev'},
        {label:'F&B',data:d.fnb,backgroundColor:C_FNB,borderRadius:0,borderSkipped:false,stack:'rev'},
        {label:'Tour',data:d.tour,backgroundColor:C_TOUR,borderRadius:{topLeft:3,topRight:3,bottomLeft:0,bottomRight:0},borderSkipped:false,stack:'rev'}
    ]},options:{responsive:true,maintainAspectRatio:false,plugins:{legend:{display:false},tooltip:{callbacks:{label:c=>` ${c.dataset.label}: ${c.parsed.y}M`}}},scales:{x:{stacked:true,grid:{display:false},border:{display:false},ticks:{color:C_TICK}},y:{stacked:true,grid:{color:C_GRID},border:{display:false},ticks:{color:C_TICK,callback:v=>v+'M'}}}}});
});

function toggleSubmenu(id,arrowId){
    const s=document.getElementById(id),a=document.getElementById(arrowId);
    if(!s||!a)return;
    const open=s.classList.contains('open');
    document.querySelectorAll('.mgr-submenu').forEach(e=>e.classList.remove('open'));
    document.querySelectorAll('.mgr-nav-arrow').forEach(e=>e.classList.remove('open'));
    if(!open){s.classList.add('open');a.classList.add('open');}
}
