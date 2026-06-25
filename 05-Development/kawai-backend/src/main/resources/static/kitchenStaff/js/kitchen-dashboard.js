// Live clock
setInterval(() => {
  const clock = document.getElementById('live-clock');
  if(clock) clock.textContent = new Date().toLocaleTimeString();
}, 1000);

function refreshBoard() {
  fetch(location.href)
    .then(res => res.text())
    .then(html => {
      const parser = new DOMParser();
      const doc = parser.parseFromString(html, 'text/html');
      const newBoard = doc.querySelector('.kanban-board');
      if (newBoard) {
        document.querySelector('.kanban-board').innerHTML = newBoard.innerHTML;
        restoreStates();
      }
    })
    .catch(err => console.error('Background refresh failed:', err));
}

// Auto refresh every 15s
setInterval(refreshBoard, 15000);

// Toggle Food Item state
function toggleItem(liElement) {
  liElement.classList.toggle('item-done');

  const detailId = liElement.getAttribute('data-id');
  const orderList = liElement.closest('.order-items');
  const orderId = orderList.id.replace('list-', '');

  // Save state to LocalStorage
  let doneItems = JSON.parse(localStorage.getItem('kitchenDoneItems') || '{}');
  doneItems[detailId] = liElement.classList.contains('item-done');
  localStorage.setItem('kitchenDoneItems', JSON.stringify(doneItems));

  checkOrderCompletion(orderId, orderList);
}

function checkOrderCompletion(orderId, orderList) {
  const allItems = orderList.querySelectorAll('li');
  const doneItems = orderList.querySelectorAll('li.item-done');

  const readyBtn = document.getElementById('btn-ready-' + orderId);
  if (readyBtn) {
    if (allItems.length > 0 && allItems.length === doneItems.length) {
      readyBtn.disabled = false;
    } else {
      readyBtn.disabled = true;
    }
  }
}

function restoreStates() {
  let doneItems = JSON.parse(localStorage.getItem('kitchenDoneItems') || '{}');

  document.querySelectorAll('.order-items li').forEach(li => {
    const detailId = li.getAttribute('data-id');
    if (doneItems[detailId]) {
      li.classList.add('item-done');
    }
  });

  // Check all preparing orders to enable/disable button
  document.querySelectorAll('.preparing .order-items').forEach(orderList => {
    const orderId = orderList.id.replace('list-', '');
    checkOrderCompletion(orderId, orderList);
  });
}

// Restore state on load
window.addEventListener('DOMContentLoaded', restoreStates);

// Update status
function updateStatus(orderId, status) {
  fetch('/api/pos/orders/' + orderId + '/status?status=' + status, {
    method: 'PUT'
  })
    .then(res => res.json())
    .then(data => {
      if (data.status === 'success') {
        // If order is completed, cleanup localstorage for its items
        if (status === 'Ready') {
          const list = document.getElementById('list-' + orderId);
          if (list) {
            let doneItems = JSON.parse(localStorage.getItem('kitchenDoneItems') || '{}');
            list.querySelectorAll('li').forEach(li => {
              delete doneItems[li.getAttribute('data-id')];
            });
            localStorage.setItem('kitchenDoneItems', JSON.stringify(doneItems));
          }
        }
        location.reload();
      } else {
        alert('Lỗi: ' + data.message);
      }
    })
    .catch(err => {
      alert('Lỗi kết nối');
    });
}
