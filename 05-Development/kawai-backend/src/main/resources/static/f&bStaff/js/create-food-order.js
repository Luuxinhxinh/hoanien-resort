const MENU_ITEMS = window.SERVER_MENU_ITEMS || [];

document.addEventListener('DOMContentLoaded', () => {
  // --- STATE ---
  const state = {
    orderType: 'dine-in', // 'dine-in' or 'room-svc'
    cart: {}, // foodId -> { item, qty }
    vatRate: 0.1,
    roomLimit: 1000000, // Hardcoded for mockup (1,000,000 VND)
    roomOccupied: false
  };

  // --- ELEMENTS ---
  const typeBtns = document.querySelectorAll('.type-btn');
  const dineInFields = document.getElementById('fields-dine-in');
  const roomSvcFields = document.getElementById('fields-room-svc');
  const paymentSection = document.getElementById('payment-section-wrapper');

  const roomInput = document.getElementById('roomInput');
  const roomInfo = document.getElementById('roomInfo');
  const guestNameEl = document.getElementById('guestName');
  const roomLimitEl = document.getElementById('roomLimit');
  const limitWarning = document.getElementById('limitWarning');

  const cartList = document.getElementById('cart-items-list');
  const emptyMsg = document.getElementById('cart-empty-msg');
  const countEl = document.getElementById('cart-count');
  const subtotalEl = document.getElementById('summary-subtotal');
  const vatEl = document.getElementById('summary-vat');
  const totalEl = document.getElementById('summary-total');

  const btnSendKitchen = document.getElementById('btn-send-kitchen');
  const searchInput = document.getElementById('food-search');
  const catPills = document.querySelectorAll('.cat-pill');

  // --- ORDER TYPE TOGGLE ---
  typeBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      typeBtns.forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      state.orderType = btn.dataset.type;

      if (state.orderType === 'dine-in') {
        dineInFields.style.display = 'grid';
        roomSvcFields.style.display = 'none';

        // Show normal payment options
        paymentSection.innerHTML = `
          <label class="form-label">Thanh toán</label>
          <select class="form-control">
            <option>Thanh toán tại quầy</option>
            <option>Xác nhận thanh toán sau</option>
          </select>
        `;
      } else {
        dineInFields.style.display = 'none';
        roomSvcFields.style.display = 'grid';

        // Show charge to room info
        paymentSection.innerHTML = `
          <label class="form-label">Thanh toán</label>
          <div class="room-charge-info">
            <div class="rc-row"><span class="rc-label">Hình thức</span><span class="rc-val">Charge to Room</span></div>
            <div class="rc-row"><span class="rc-label">Hạn mức còn lại</span><span class="rc-val">${formatMoney(state.roomLimit)}</span></div>
          </div>
        `;
      }
      validateCheckout();
    });
  });

  // --- ROOM SERVICE API INTEGRATION ---
  let roomSearchTimeout = null;
  roomInput.addEventListener('input', (e) => {
    const val = e.target.value.trim().toUpperCase();

    // Clear previous timeout
    if (roomSearchTimeout) clearTimeout(roomSearchTimeout);

    if (!val) {
      roomInfo.style.display = 'none';
      state.roomOccupied = false;
      validateCheckout();
      return;
    }

    // Debounce API call
    roomSearchTimeout = setTimeout(() => {
      fetch(`/api/rooms/${val}/info`)
        .then(response => {
          if (!response.ok) throw new Error('Not found');
          return response.json();
        })
        .then(data => {
          if (data.occupied) {
            roomInfo.style.display = 'block';
            guestNameEl.textContent = data.guestName || 'Không có tên';
            state.roomLimit = data.limitRemaining || 0;
            roomLimitEl.textContent = formatMoney(state.roomLimit);
            state.roomOccupied = true;
            document.getElementById('roomStatusBadge').className = 'status-badge status-occupied';
            document.getElementById('roomStatusBadge').textContent = 'Occupied';

            if (state.orderType === 'room-svc') {
              const limitDisplay = document.querySelector('.room-charge-info .rc-val:last-child');
              if (limitDisplay) limitDisplay.textContent = formatMoney(state.roomLimit);
            }
          } else {
            showVacant();
          }
          validateCheckout();
        })
        .catch(err => {
          showVacant();
          validateCheckout();
        });
    }, 400); // 400ms debounce
  });

  function showVacant() {
    roomInfo.style.display = 'none';
    state.roomOccupied = false;
    document.getElementById('roomStatusBadge').className = 'status-badge status-vacant';
    document.getElementById('roomStatusBadge').textContent = 'Trống';
  }

  // --- FILTER & SEARCH ---
  searchInput.addEventListener('input', (e) => filterFoods());
  catPills.forEach(pill => {
    pill.addEventListener('click', () => {
      catPills.forEach(p => p.classList.remove('active'));
      pill.classList.add('active');
      filterFoods();
    });
  });

  function filterFoods() {
    const query = searchInput.value.toLowerCase();
    const activeCat = document.querySelector('.cat-pill.active').dataset.cat;

    document.querySelectorAll('.food-card').forEach(card => {
      const name = card.dataset.name.toLowerCase();
      const cat = card.dataset.cat;
      const matchesSearch = name.includes(query);
      const matchesCat = activeCat === 'all' || cat === activeCat;

      card.style.display = (matchesSearch && matchesCat) ? 'flex' : 'none';
    });
  }

  // --- DYNAMIC RENDERING ---
  function getCatLabel(cat) {
    switch (cat) {
      case 'starter': return 'Khai vị';
      case 'main': return 'Món chính';
      case 'dessert': return 'Tráng miệng';
      case 'drink': return 'Đồ uống';
      default: return cat;
    }
  }

  function formatMoney(amount) {
    return new Intl.NumberFormat('vi-VN').format(amount) + '₫';
  }

  function renderFoodGrid() {
    const grid = document.getElementById('food-grid');
    if (!grid) return;
    grid.innerHTML = MENU_ITEMS.map(item => {
      const isOut = item.status === 'out-of-stock';
      const currentQty = state.cart[item.id] ? state.cart[item.id].qty : 0;
      return `
        <div class="food-card ${isOut ? 'disabled' : ''}" data-name="${item.name}" data-cat="${item.cat}">
          <div class="food-img-wrapper" style="${item.imageUrl ? `background-image: url('${item.imageUrl}'); background-size: cover; background-position: center;` : `--food-bg: linear-gradient(135deg, ${item.bgFrom}, ${item.bgTo}); --food-icon-color: ${item.iconColor};`}">
            ${item.imageUrl ? '' : `
            <div class="food-icon-bg"></div>
            <div class="food-icon-wrapper">
              <span class="material-symbols-outlined">${item.icon}</span>
            </div>
            `}
            ${isOut ? '<span class="food-stock-badge out-of-stock">Hết món</span>' : ''}
          </div>
          <div class="food-details">
            <span class="food-cat">${getCatLabel(item.cat)}</span>
            <span class="food-name">${item.name}</span>
            <span class="food-price">${formatMoney(item.price)}</span>
            <div class="qty-controls">
              <button class="qty-btn" ${isOut ? 'disabled' : `onclick="changeQty(this, -1, '${item.id}', '${item.name}', ${item.price})"`}>−</button>
              <span class="qty-value">${currentQty}</span>
              <button class="qty-btn" style="${!isOut ? 'background:var(--namia-text);color:#fff;border-color:var(--namia-text);' : ''}" ${isOut ? 'disabled' : `onclick="changeQty(this, 1, '${item.id}', '${item.name}', ${item.price})"`}>+</button>
            </div>
          </div>
        </div>
      `;
    }).join('');
    // re-apply filters after render
    filterFoods();
  }

  // --- CART LOGIC ---
  window.changeQty = function (btn, delta, id, name, price) {
    // If out of stock, do nothing
    const card = btn.closest('.food-card');
    if (card && card.classList.contains('disabled')) return;

    if (!state.cart[id]) {
      state.cart[id] = { id, name, price, qty: 0 };
    }

    const newQty = state.cart[id].qty + delta;
    if (newQty < 0) return;

    state.cart[id].qty = newQty;
    if (newQty === 0) {
      delete state.cart[id];
    }

    // Update input display
    const display = btn.parentElement.querySelector('.qty-value');
    display.textContent = newQty;

    renderCart();
  };

  function renderCart() {
    const items = Object.values(state.cart);
    let subtotal = 0;
    let totalQty = 0;

    cartList.innerHTML = '';

    if (items.length === 0) {
      cartList.appendChild(emptyMsg);
      emptyMsg.style.display = 'block';
    } else {
      emptyMsg.style.display = 'none';
      items.forEach(item => {
        subtotal += item.price * item.qty;
        totalQty += item.qty;

        const div = document.createElement('div');
        div.className = 'cart-item';
        div.innerHTML = `
          <div class="cart-item-header">
            <span class="cart-item-name">${item.name}</span>
            <span class="cart-item-total">${formatMoney(item.price * item.qty)}</span>
          </div>
          <div class="cart-item-meta">
            <span>SL: <strong style="color:var(--text-primary)">${item.qty}</strong></span>
            <span class="cart-item-unit">${formatMoney(item.price)}/phần</span>
          </div>
        `;
        cartList.appendChild(div);
      });
    }

    const vat = subtotal * state.vatRate;
    const total = subtotal + vat;

    countEl.textContent = totalQty;
    subtotalEl.textContent = formatMoney(subtotal);
    vatEl.textContent = formatMoney(vat);
    totalEl.textContent = formatMoney(total);

    state.currentTotal = total;
    validateCheckout();
  }

  function validateCheckout() {
    let isValid = true;
    const items = Object.values(state.cart);

    // Condition 1: Must have items
    if (items.length === 0) {
      isValid = false;
    }

    // Condition 2: Room Service checks
    if (state.orderType === 'room-svc') {
      if (!state.roomOccupied) {
        isValid = false;
      }
      if (state.currentTotal > state.roomLimit) {
        isValid = false;
        limitWarning.style.display = 'block';
      } else {
        limitWarning.style.display = 'none';
      }
    } else {
      limitWarning.style.display = 'none';
    }

    btnSendKitchen.disabled = !isValid;
  }

  // --- PREFILL FROM URL PARAMS ---
  const urlParams = new URLSearchParams(window.location.search);
  const tableIdParam = urlParams.get('tableId');
  const customerNameParam = urlParams.get('customerName');

  if (tableIdParam) {
    const tableSelect = document.querySelector('#fields-dine-in select');
    if (tableSelect) tableSelect.value = tableIdParam;
  }
  if (customerNameParam) {
    const guestInput = document.getElementById('guestNameInput');
    if (guestInput) guestInput.value = customerNameParam;
  }

  // --- INIT ---
  renderFoodGrid();

  // Expose send logic
  window.sendToKitchen = function () {
    const tableSelect = document.querySelector('#fields-dine-in select');
    const guestInput = document.getElementById('guestNameInput');
    const dineInNote = document.getElementById('dineInNoteInput');
    const roomSvcNote = document.getElementById('roomSvcNoteInput');
    const payload = {
      orderType: state.orderType,
      roomNumber: state.orderType === 'room-svc' ? roomInput.value.trim().toUpperCase() : null,
      tableId: state.orderType === 'dine-in' && tableSelect ? tableSelect.value : null,
      guestName: state.orderType === 'dine-in' && guestInput ? guestInput.value.trim() : null,
      note: state.orderType === 'dine-in' && dineInNote ? dineInNote.value.trim() : (state.orderType === 'room-svc' && roomSvcNote ? roomSvcNote.value.trim() : null),
      paymentType: 'Pay_Later',
      items: Object.values(state.cart).map(item => ({
        id: item.id,
        qty: item.qty,
        price: item.price
      }))
    };

    btnSendKitchen.disabled = true;
    btnSendKitchen.textContent = 'Đang xử lý...';

    fetch('/api/pos/orders', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(payload)
    })
      .then(res => {
        if (!res.ok) {
          return res.json().then(errData => {
            throw new Error(errData.message || 'API Error');
          });
        }
        return res.json();
      })
      .then(data => {
        alert("Đã sinh Kitchen Order Ticket (KOT) và chuyển xuống bếp!");
        window.location.href = "/fbStaff/dashboard";
      })
      .catch(err => {
        alert("Lỗi khi tạo đơn: " + err.message);
        btnSendKitchen.disabled = false;
        btnSendKitchen.textContent = 'Gửi xuống Bếp (KOT)';
      });
  }



});
