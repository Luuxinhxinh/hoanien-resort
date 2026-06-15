/**
 * ROOM SERVICE DETAIL - MOCK LOGIC FOR DEMONSTRATION
 */

document.addEventListener('DOMContentLoaded', () => {
  const urlParams = new URLSearchParams(window.location.search);
  const orderId = urlParams.get('id') || 'RS-1024';

  const elOrderId = document.getElementById('order-id');
  const elStatusChip = document.getElementById('order-status-chip');
  
  const btnCancel = document.getElementById('btn-cancel');
  const btnConfirm = document.getElementById('btn-confirm');
  const btnForward = document.getElementById('btn-forward');
  const btnServed = document.getElementById('btn-served');

  const elGuest = document.getElementById('info-guest');
  const elRoom = document.getElementById('info-room');
  const elDate = document.getElementById('order-date');
  
  const tbody = document.getElementById('items-tbody');

  elOrderId.textContent = orderId;

  // Mock Data Based on ID
  if (orderId === 'RS-1024') {
    // Pending
    elStatusChip.className = 'order-status-chip chip-pending';
    elStatusChip.innerHTML = '<span class="dot"></span>Pending';
    
    elGuest.textContent = 'Trần Thị Lan Anh';
    elRoom.textContent = 'Phòng 208';
    
    btnCancel.style.display = 'block';
    btnConfirm.style.display = 'block';
    btnForward.style.display = 'none';
    btnServed.style.display = 'none';

  } else if (orderId === 'RS-1023') {
    // Preparing
    elStatusChip.className = 'order-status-chip chip-cooking';
    elStatusChip.innerHTML = '<span class="dot"></span>Preparing';
    
    elGuest.textContent = 'Phạm Thùy Linh';
    elRoom.textContent = 'Phòng 501';
    
    btnCancel.style.display = 'block';
    btnConfirm.style.display = 'none';
    btnForward.style.display = 'block';
    btnServed.style.display = 'none';

  } else if (orderId === 'RS-1020') {
    // Served
    elStatusChip.className = 'order-status-chip chip-served';
    elStatusChip.innerHTML = '<span class="dot"></span>Served';
    
    elGuest.textContent = 'Lê Hoàng Nam';
    elRoom.textContent = 'Phòng 112';
    
    btnCancel.style.display = 'none';
    btnConfirm.style.display = 'none';
    btnForward.style.display = 'none';
    btnServed.style.display = 'none';

    // Thêm nút in bill
    btnConfirm.outerHTML = '<button class="btn btn-outline" id="btn-print"><span class="material-symbols-outlined">print</span> Print Bill</button>';
    
  } else if (orderId === 'RS-1019') {
    // Cancelled
    elStatusChip.style.background = '#fbecec';
    elStatusChip.style.color = '#d32f2f';
    elStatusChip.style.borderColor = '#d32f2f';
    elStatusChip.innerHTML = '<span class="dot" style="background:#d32f2f;"></span>Cancelled';
    
    elGuest.textContent = 'Nguyễn Văn Cường';
    elRoom.textContent = 'Phòng 305';
    
    btnCancel.style.display = 'none';
    btnConfirm.style.display = 'none';
    btnForward.style.display = 'none';
    btnServed.style.display = 'none';
  }

});
