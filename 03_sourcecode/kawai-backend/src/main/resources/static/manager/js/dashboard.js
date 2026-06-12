/* dashboard.js — Tổng quan: bar chart, donut chart, line chart */

const C_ROOM='#C9A96E',C_FNB='#7A9C7A',C_TOUR='#6B8C9A';
const C_GRID='rgba(44,42,30,0.07)',C_TICK='#8B7355';

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
    initBar(); initDonut(); initLine();
});

function initBar(){
    const ctx=document.getElementById('chart-bar');
    if(!ctx)return;
    const d=window.barData||{labels:['T2','T3','T4','T5','T6','T7','CN'],room:[180,145,200,220,265,310,295],fnb:[65,55,78,82,95,110,105],tour:[42,28,55,61,72,88,80]};
    new Chart(ctx,{type:'bar',data:{labels:d.labels,datasets:[
        {label:'Phòng',data:d.room,backgroundColor:'rgba(201,169,110,0.85)',borderRadius:{topLeft:0,topRight:0,bottomLeft:3,bottomRight:3},borderSkipped:false,stack:'rev'},
        {label:'F&B',data:d.fnb,backgroundColor:'rgba(122,156,122,0.85)',borderRadius:0,borderSkipped:false,stack:'rev'},
        {label:'Tour',data:d.tour,backgroundColor:'rgba(107,140,154,0.85)',borderRadius:{topLeft:3,topRight:3,bottomLeft:0,bottomRight:0},borderSkipped:false,stack:'rev'}
    ]},options:{responsive:true,maintainAspectRatio:false,plugins:{legend:{display:false},tooltip:{callbacks:{label:c=>` ${c.dataset.label}: ${c.parsed.y}M`}}},scales:{x:{stacked:true,grid:{display:false},border:{display:false},ticks:{color:C_TICK}},y:{stacked:true,grid:{color:C_GRID},border:{display:false},ticks:{color:C_TICK,callback:v=>v+'M'}}}}});
}

function initDonut(){
    const ctx=document.getElementById('chart-donut');
    if(!ctx)return;
    const d=window.donutData||{room:1128,fnb:487,tour:231};
    new Chart(ctx,{type:'doughnut',data:{labels:['Phòng','F&B','Tour'],datasets:[{data:[d.room,d.fnb,d.tour],backgroundColor:[C_ROOM,C_FNB,C_TOUR],borderWidth:0,hoverOffset:6}]},options:{responsive:true,maintainAspectRatio:false,cutout:'68%',plugins:{legend:{display:false},tooltip:{callbacks:{label:c=>` ${c.label}: ${c.parsed}M`}}}}});
}

function initLine(){
    const ctx=document.getElementById('chart-line');
    if(!ctx)return;
    const vals=[72,75,78,80,82,85,88,91,89,87,85,83,80,78,76,79,82,85,88,92,94,90,87,84,81,79,77,80,83,86];
    const labels=Array.from({length:30},(_,i)=>i%5===0?`${i+1}/06`:'');
    new Chart(ctx,{type:'line',data:{labels,datasets:[{label:'Occupancy',data:vals,borderColor:C_ROOM,backgroundColor:'rgba(201,169,110,0.08)',borderWidth:2,pointRadius:0,pointHoverRadius:4,tension:0.4,fill:true}]},options:{responsive:true,maintainAspectRatio:false,interaction:{mode:'index',intersect:false},plugins:{legend:{display:false},tooltip:{callbacks:{title:i=>`Ngày ${i[0].dataIndex+1}/06`,label:c=>` Lấp đầy: ${c.parsed.y}%`}}},scales:{x:{grid:{display:false},border:{display:false},ticks:{color:C_TICK,maxRotation:0}},y:{min:60,max:100,grid:{color:C_GRID},border:{display:false},ticks:{color:C_TICK,callback:v=>v+'%',stepSize:10}}}}});
}

function toggleSubmenu(id,arrowId){
    const s=document.getElementById(id),a=document.getElementById(arrowId);
    if(!s||!a)return;
    const open=s.classList.contains('open');
    document.querySelectorAll('.mgr-submenu').forEach(e=>e.classList.remove('open'));
    document.querySelectorAll('.mgr-nav-arrow').forEach(e=>e.classList.remove('open'));
    if(!open){s.classList.add('open');a.classList.add('open');}
}
