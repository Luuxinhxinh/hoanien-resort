/* analytics-occupancy.js — Tỷ lệ lấp đầy phòng: line chart 30 ngày */

const C_ROOM='#C9A96E';
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
    const ctx=document.getElementById('chart-occ-line');
    if(!ctx)return;
    const vals=[72,75,78,80,82,85,88,91,89,87,85,83,80,78,76,79,82,85,88,92,94,90,87,84,81,79,77,80,83,86];
    const labels=Array.from({length:30},(_,i)=>i%5===0?`${i+1}/06`:'');
    new Chart(ctx,{type:'line',data:{labels,datasets:[{label:'Occupancy',data:vals,borderColor:C_ROOM,backgroundColor:'rgba(201,169,110,0.08)',borderWidth:2,pointRadius:0,pointHoverRadius:4,tension:0.4,fill:true}]},options:{responsive:true,maintainAspectRatio:false,interaction:{mode:'index',intersect:false},plugins:{legend:{display:false},tooltip:{callbacks:{title:i=>`Ngày ${i[0].dataIndex+1}/06`,label:c=>` Lấp đầy: ${c.parsed.y}%`}}},scales:{x:{grid:{display:false},border:{display:false},ticks:{color:C_TICK,maxRotation:0}},y:{min:60,max:100,grid:{color:C_GRID},border:{display:false},ticks:{color:C_TICK,callback:v=>v+'%',stepSize:10}}}}});
});

function toggleSubmenu(id,arrowId){
    const s=document.getElementById(id),a=document.getElementById(arrowId);
    if(!s||!a)return;
    const open=s.classList.contains('open');
    document.querySelectorAll('.mgr-submenu').forEach(e=>e.classList.remove('open'));
    document.querySelectorAll('.mgr-nav-arrow').forEach(e=>e.classList.remove('open'));
    if(!open){s.classList.add('open');a.classList.add('open');}
}
