/* analytics-food.js — Tỷ lệ các món ăn: donut chart by category */

const COLORS=['#7A9C7A','#C9A96E','#6B8C9A','#A89060','#8A7A9A'];

Chart.defaults.font.family="'DM Sans',sans-serif";
Chart.defaults.font.size=12;
Chart.defaults.plugins.legend.display=false;
Chart.defaults.plugins.tooltip.backgroundColor='rgba(28,26,20,0.92)';
Chart.defaults.plugins.tooltip.titleColor='#F5F0E8';
Chart.defaults.plugins.tooltip.bodyColor='#C9A96E';
Chart.defaults.plugins.tooltip.padding=10;
Chart.defaults.plugins.tooltip.cornerRadius=8;

document.addEventListener('DOMContentLoaded',()=>{
    const ctx=document.getElementById('chart-food-donut');
    if(!ctx)return;
    const d=window.foodDonutData||{labels:["Món chính","Súp","Set Menu","Tráng miệng","Đồ uống"],values:[42,28,21,16,18]};
    new Chart(ctx,{type:'doughnut',data:{labels:d.labels,datasets:[{data:d.values,backgroundColor:COLORS,borderWidth:0,hoverOffset:6}]},options:{responsive:true,maintainAspectRatio:false,cutout:'62%',plugins:{legend:{display:false},tooltip:{callbacks:{label:c=>` ${c.label}: ${c.parsed} đơn`}}}}});
});

function toggleSubmenu(id,arrowId){
    const s=document.getElementById(id),a=document.getElementById(arrowId);
    if(!s||!a)return;
    const open=s.classList.contains('open');
    document.querySelectorAll('.mgr-submenu').forEach(e=>e.classList.remove('open'));
    document.querySelectorAll('.mgr-nav-arrow').forEach(e=>e.classList.remove('open'));
    if(!open){s.classList.add('open');a.classList.add('open');}
}
