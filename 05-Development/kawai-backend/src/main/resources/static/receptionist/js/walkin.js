// =============================================================================
// walkin.js — Walk-In Check-in Page Logic
// -----------------------------------------------------------------------------
// Tổng quan các chức năng trong file:
//   1. Toast notification helper
//   2. Khởi tạo trang (DOMContentLoaded)
//   3. Quản lý phòng bẩn (Dirty Room Escalation)
//   4. Danh sách phòng khả dụng (Room Inventory / Cart)
//   5. Thông tin khách hàng & xác thực
//   6. Bước thanh toán & tính tiền
//   7. Khách đi kèm (Accompanied Guests / Dependents)
//   8. Auto-save form xuống localStorage
/**
 * Hiển thị toast notification góc phải màn hình.
 * @param {string} msg  - Nội dung thông báo
 * @param {'success'|'error'|'warning'} type - Loại toast
 */
function showToast(msg, type = 'success') {
    // Tạo container nếu chưa có
    let container = document.getElementById('custom-toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'custom-toast-container';

        const style = document.createElement('style');
        style.innerHTML = `
            #custom-toast-container { position: fixed; top: 20px; right: 20px; z-index: 99999; display: flex; flex-direction: column; gap: 10px; }
            .custom-toast { display: flex; align-items: flex-start; padding: 16px; border-radius: 8px; box-shadow: 0 10px 15px -3px rgba(0,0,0,0.1); border-left: 4px solid; width: 320px; transform: translateX(120%); opacity: 0; transition: all 0.3s ease; font-family: sans-serif; background: white; }
            .custom-toast.show { transform: translateX(0); opacity: 1; }
            .custom-toast.error { border-color: #ef4444; color: #7f1d1d; background-color: #fef2f2; }
            .custom-toast.success { border-color: #22c55e; color: #14532d; background-color: #f0fdf4; }
            .custom-toast.warning { border-color: #f59e0b; color: #78350f; background-color: #fffbeb; }
            .custom-toast-icon { font-size: 20px; margin-right: 12px; }
            .custom-toast.error .custom-toast-icon { color: #ef4444; }
            .custom-toast.success .custom-toast-icon { color: #22c55e; }
            .custom-toast.warning .custom-toast-icon { color: #f59e0b; }
            .custom-toast-title { font-weight: 700; font-size: 13px; text-transform: uppercase; margin: 0 0 4px 0; }
            .custom-toast-message { font-size: 12px; margin: 0; line-height: 1.4; }
        `;
        document.head.appendChild(style);
        document.body.appendChild(container);
    }

    // Chọn icon và tiêu đề dựa theo loại
    let icon = 'fa-circle-check';
    let title = 'THÀNH CÔNG';
    if (type === 'error') { icon = 'fa-circle-exclamation'; title = 'LỖI'; }
    if (type === 'warning') { icon = 'fa-triangle-exclamation'; title = 'CẢNH BÁO'; }

    const toast = document.createElement('div');
    toast.className = `custom-toast ${type}`;
    toast.innerHTML = `<i class="fa-solid ${icon} custom-toast-icon"></i><div><h4 class="custom-toast-title">${title}</h4><p class="custom-toast-message">${msg}</p></div>`;

    container.appendChild(toast);

    // Animate: hiện sau 10ms, ẩn sau 4 giây
    requestAnimationFrame(() => setTimeout(() => toast.classList.add('show'), 10));
    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}


// =============================================================================
// 2. KHỞI TẠO TRANG (DOMContentLoaded)
// =============================================================================

/** Biến toàn cục lưu ngày hôm nay dạng 'YYYY-MM-DD' (set trong DOMContentLoaded). */
let todayStrGlobal = '';

// --- Khởi tạo dropdown loại phòng & ràng buộc ngày ---
document.addEventListener('DOMContentLoaded', () => {
    const typeSelect = document.getElementById('walkInTypeSelect');
    if (!typeSelect || typeof roomInventory === 'undefined') return;

    // Thêm từng loại phòng vào dropdown kèm thông tin số phòng thực còn trống
    for (const category in roomInventory) {
        const opt = document.createElement('option');
        opt.value = category;

        // Số phòng thực còn khả dụng (đã trừ slot bị giữ bởi booking online Confirmed)
        const netAvailable = (typeof categoryAvailability !== 'undefined' && categoryAvailability[category] !== undefined)
            ? categoryAvailability[category]
            : roomInventory[category].length;
        const totalVacant = roomInventory[category].length;

        let availabilityLabel = '';
        if (netAvailable <= 0) {
            availabilityLabel = ' ⚠ Hết phòng thực tế (đã bị giữ bởi booking online)';
            opt.style.color = '#dc2626'; // đỏ — hết phòng
        } else if (netAvailable < totalVacant) {
            availabilityLabel = ` (Còn ${netAvailable}/${totalVacant} phòng walk-in được)`;
            opt.style.color = '#d97706'; // vàng cam — còn ít
        } else {
            availabilityLabel = ` (Còn ${netAvailable} phòng)`;
        }

        opt.innerText = category + availabilityLabel;
        opt.dataset.netAvailable = netAvailable;
        typeSelect.appendChild(opt);
    }

    // Tính ngày hôm nay theo múi giờ local
    const localDate = new Date();
    const year = localDate.getFullYear();
    const month = String(localDate.getMonth() + 1).padStart(2, '0');
    const day = String(localDate.getDate()).padStart(2, '0');
    todayStrGlobal = `${year}-${month}-${day}`;

    // Ràng buộc: ngày trả phòng phải >= hôm nay
    const checkoutDateInput = document.getElementById('checkoutDate');
    if (checkoutDateInput) {
        checkoutDateInput.min = todayStrGlobal;
        // Khi đổi ngày trả phòng mà bước thanh toán đang hiện → tính lại
        checkoutDateInput.addEventListener('change', () => {
            if (document.getElementById('step4Container').style.display === 'block') {
                showPaymentStep();
            }
        });
    }

    // Ràng buộc ngày sinh: không được ở tương lai
    const guestDobInput = document.getElementById('guestDob');
    const depDobInput = document.getElementById('depDob');
    if (guestDobInput) guestDobInput.max = todayStrGlobal;
    if (depDobInput) depDobInput.max = todayStrGlobal;
});

// --- Khởi tạo sự kiện thay đổi dropdown chọn phòng vật lý ---
document.addEventListener('DOMContentLoaded', () => {
    const roomSelect = document.getElementById('walkInPhysicalRoomSelect');
    if (!roomSelect) return;

    roomSelect.addEventListener('change', function () {
        const opt = this.options[this.selectedIndex];
        if (!opt) return;

        const status = opt.dataset.status || opt.getAttribute('data-status');
        const roomNum = opt.dataset.roomNum || opt.getAttribute('data-roomnum') || opt.getAttribute('data-roomNum');

        const gridContainer = this.closest('div[style*="display: grid"]');
        if (!gridContainer) return;

        // Xóa cảnh báo phòng bẩn cũ (nếu có)
        const existingWarning = document.getElementById('dirtyRoomWarningInlineWalkIn');
        if (existingWarning) existingWarning.remove();

        const addBtn = gridContainer.querySelector('button[onclick="addRoomToCart()"]');

        if (status === 'Vacant_Dirty') {
            // Phòng chưa dọn: hiện banner cảnh báo và vô hiệu nút thêm vào cart
            const warningDiv = document.createElement('div');
            warningDiv.id = 'dirtyRoomWarningInlineWalkIn';
            warningDiv.style.gridColumn = '1 / -1';
            warningDiv.style.marginTop = '12px';
            warningDiv.style.padding = '12px 16px';
            warningDiv.style.background = '#fef2f2';
            warningDiv.style.border = '1px solid #fca5a5';
            warningDiv.style.borderRadius = '6px';
            warningDiv.style.display = 'flex';
            warningDiv.style.flexDirection = 'column';
            warningDiv.style.gap = '8px';
            warningDiv.innerHTML = `
                <div style="color: #dc2626; font-size: 13px;">
                    <i class="fa-solid fa-triangle-exclamation"></i> <strong>Cảnh báo:</strong> Phòng <b>${roomNum}</b> chưa dọn dẹp. Vui lòng chọn hành động:
                </div>
                <div style="display: flex; gap: 8px; flex-wrap: wrap;">
                    <button type="button" style="background:#3b82f6; color:white; border:none; padding: 6px 12px; border-radius: 4px; font-size: 13px; cursor: pointer; display: flex; align-items: center; gap: 6px;" onclick="escalateDirtyRoomWalkIn('${roomNum}', false)">
                        <i class="fa-solid fa-broom"></i> Chỉ yêu cầu dọn (Chưa gán)
                    </button>
                    <button type="button" style="background:#ef4444; color:white; border:none; padding: 6px 12px; border-radius: 4px; font-size: 13px; cursor: pointer; display: flex; align-items: center; gap: 6px;" onclick="escalateDirtyRoomWalkIn('${roomNum}', true)">
                        <i class="fa-solid fa-bolt"></i> Yêu cầu dọn & Phân phòng (Treo)
                    </button>
                </div>
            `;
            gridContainer.appendChild(warningDiv);

            if (addBtn) {
                addBtn.disabled = true;
                addBtn.style.opacity = '0.5';
                addBtn.style.cursor = 'not-allowed';
            }
        } else {
            // Phòng sạch: cho phép thêm vào cart bình thường
            if (addBtn) {
                addBtn.disabled = false;
                addBtn.style.opacity = '1';
                addBtn.style.cursor = 'pointer';
            }
        }
    });
});


// =============================================================================
// 3. QUẢN LÝ PHÒNG BẨN (DIRTY ROOM ESCALATION)
// =============================================================================

/**
 * Gửi yêu cầu dọn phòng khẩn cấp (escalate) lên server.
 * @param {string}  roomNum     - Số phòng cần dọn
 * @param {boolean} assignAfter - true = phân phòng ngay sau khi gửi yêu cầu dọn
 */
function escalateDirtyRoomWalkIn(roomNum, assignAfter) {
    const csrfMeta = document.querySelector('meta[name="_csrf"]');
    const csrfToken = csrfMeta ? csrfMeta.getAttribute('content') : '';

    fetch(`/receptionist/rooms/escalate-dirty?roomNumber=${roomNum}`, {
        method: 'POST',
        headers: { 'X-CSRF-TOKEN': csrfToken }
    })
        .then(res => res.json())
        .then(data => {
            if (data.status === 'success') {
                showToast('Đã gửi yêu cầu dọn khẩn cấp cho buồng phòng.', 'success');

                const select = document.getElementById('walkInPhysicalRoomSelect');

                // Xóa banner cảnh báo phòng bẩn
                const existingWarning = document.getElementById('dirtyRoomWarningInlineWalkIn');
                if (existingWarning) existingWarning.remove();

                // Mở lại nút "Thêm phòng" trong grid
                const gridContainer = select.closest('div[style*="display: grid"]');
                if (gridContainer) {
                    const addBtn = gridContainer.querySelector('button[onclick="addRoomToCart()"]');
                    if (addBtn) {
                        addBtn.disabled = false;
                        addBtn.style.opacity = '1';
                        addBtn.style.cursor = 'pointer';
                    }
                }

                if (assignAfter) {
                    // Ghi nhớ phòng "treo" vào sessionStorage để room-alert.js poll
                    // và hiện thông báo khi housekeeping dọn xong
                    try {
                        const pendingRaw = sessionStorage.getItem('walkInPendingCleanRooms');
                        const pendingRooms = pendingRaw ? JSON.parse(pendingRaw) : {};
                        pendingRooms[roomNum] = true;
                        sessionStorage.setItem('walkInPendingCleanRooms', JSON.stringify(pendingRooms));
                    } catch (e) { /* ignore storage errors */ }

                    const selectedOpt = select.options[select.selectedIndex];
                    if (!selectedOpt) return;

                    proceedAddRoomToCart(
                        select.value,
                        selectedOpt.dataset.roomNum,
                        selectedOpt.dataset.category,
                        selectedOpt.dataset.price
                    );
                } else {
                    // Chỉ gửi yêu cầu dọn, chưa phân phòng → reset dropdown
                    select.value = '';
                }
            } else {
                showToast('Lỗi khi gửi yêu cầu: ' + data.message, 'error');
            }
        })
        .catch(err => {
            console.error(err);
            showToast('Lỗi kết nối khi gửi yêu cầu khẩn cấp.', 'error');
        });
}


// =============================================================================
// 4. DANH SÁCH PHÒNG KHẢ DỤNG & GIỎ HÀNG PHÒNG (ROOM CART)
// =============================================================================

/** Danh sách phòng đã chọn để check-in. Mỗi phần tử: { roomId, roomNum, category, price, allocatedCreditLimit }. */
let walkInCart = [];

/** Hạn mức tín dụng (credit limit) của khách chính. Được set sau khi tìm thấy khách trong hệ thống. */
let masterCreditLimit = 0;

/** Cờ đánh dấu khách đã được xác thực (tìm thấy hoặc xác nhận thông tin mới). */
let isGuestVerified = false;

/**
 * Cập nhật dropdown danh sách phòng vật lý theo loại phòng đang chọn.
 * Hiển thị cảnh báo nếu số phòng walk-in thực còn ít / hết.
 */
function updateWalkInAvailableRooms() {
    const typeSelect = document.getElementById('walkInTypeSelect');
    const roomSelect = document.getElementById('walkInPhysicalRoomSelect');
    roomSelect.innerHTML = '<option value="">-- Select Physical Room --</option>';

    const selectedType = typeSelect.value;
    if (!selectedType) return;

    const available = roomInventory[selectedType] || [];
    const selectedOpt = typeSelect.options[typeSelect.selectedIndex];
    const netAvailable = selectedOpt
        ? parseInt(selectedOpt.dataset.netAvailable || available.length)
        : available.length;

    // Xóa banner cảnh báo cũ
    const oldBanner = document.getElementById('walkInAvailabilityWarning');
    if (oldBanner) oldBanner.remove();

    // Hiển thị banner tương ứng với mức độ khả dụng
    if (netAvailable <= 0 && available.length > 0) {
        const banner = document.createElement('div');
        banner.id = 'walkInAvailabilityWarning';
        banner.style.cssText = 'margin-top:12px; padding:10px 14px; background:#fef2f2; border:1px solid #fca5a5; border-radius:6px; color:#dc2626; font-size:13px; display:flex; align-items:center; gap:8px;';
        banner.innerHTML = `<i class="fa-solid fa-triangle-exclamation"></i> <strong>Cảnh báo:</strong> Toàn bộ ${available.length} phòng thuộc loại này đã bị giữ bởi booking online (Confirmed). Không nên chọn walk-in cho hạng phòng này!`;
        typeSelect.parentElement.appendChild(banner);
    } else if (netAvailable < available.length) {
        const banner = document.createElement('div');
        banner.id = 'walkInAvailabilityWarning';
        banner.style.cssText = 'margin-top:12px; padding:10px 14px; background:#fffbeb; border:1px solid #fcd34d; border-radius:6px; color:#92400e; font-size:13px; display:flex; align-items:center; gap:8px;';
        banner.innerHTML = `<i class="fa-solid fa-circle-info"></i> <strong>Lưu ý:</strong> ${available.length - netAvailable} phòng đã bị giữ bởi booking online. Chỉ còn <strong>${netAvailable}/${available.length}</strong> phòng thực sự khả dụng cho walk-in.`;
        typeSelect.parentElement.appendChild(banner);
    }

    // Điền danh sách phòng theo bộ lọc trạng thái (sạch/bẩn)
    const statusFilter = document.getElementById('walkInStatusFilter');
    const filterValue = statusFilter ? statusFilter.value : 'ALL';

    available.forEach(r => {
        if (filterValue === 'CLEAN' && r.status !== 'Vacant_Clean') return;
        if (filterValue === 'DIRTY' && r.status !== 'Vacant_Dirty') return;

        const roomId = r.id !== undefined ? r.id : r;
        const roomNum = r.number !== undefined ? r.number : r;

        const opt = document.createElement('option');
        opt.value = roomId;
        opt.innerText = 'Phòng ' + roomNum + (r.status === 'Vacant_Dirty' ? ' (Chưa dọn)' : '');
        opt.dataset.roomNum = roomNum;
        opt.dataset.status = r.status;

        // Tách category và giá từ chuỗi "Deluxe - 1,200,000 (xxx)"
        const catParts = selectedType.split(' - ');
        opt.dataset.category = catParts[0];
        opt.dataset.price = catParts.length > 1 ? catParts[1].split(' (')[0] : '';

        roomSelect.appendChild(opt);
    });
}

/**
 * Mở khóa Step 2 & Step 3 khi giỏ phòng có ít nhất 1 phòng.
 */
function unlockStep2() {
    if (walkInCart.length > 0) {
        const step2 = document.getElementById('step2Container');
        step2.style.opacity = '1';
        step2.style.pointerEvents = 'auto';
        step2.style.border = '1px solid #bae6fd';
        step2.style.boxShadow = '0 0 10px rgba(2,132,199,0.1)';

        const step3 = document.getElementById('step3Container');
        if (step3) {
            step3.style.opacity = '1';
            step3.style.pointerEvents = 'auto';
            step3.style.border = '1px solid #bae6fd';
        }
    }
}

/**
 * Cập nhật hiển thị hạn mức tín dụng còn lại (tổng masterCreditLimit trừ đã phân bổ).
 */
function updateCreditLimitDisplay() {
    const totalAllocated = walkInCart.reduce((sum, r) => sum + (r.allocatedCreditLimit || 0), 0);
    const remaining = masterCreditLimit - totalAllocated;

    const displayEl = document.getElementById('remainingCreditDisplay');
    if (displayEl) {
        displayEl.innerText = remaining.toLocaleString() + ' VND';
        displayEl.style.color = remaining < 0 ? 'red' : '#16a34a';
    }
}

/**
 * Xử lý khi người dùng nhập hạn mức tín dụng cho một phòng trong cart.
 * @param {HTMLInputElement} input - Input hạn mức
 * @param {number}           index - Vị trí phòng trong walkInCart
 */
function handleCreditInput(input, index) {
    walkInCart[index].allocatedCreditLimit = parseFloat(input.value) || 0;
    updateCreditLimitDisplay();

    // Highlight đỏ nếu tổng vượt quá hạn mức master
    const totalAllocated = walkInCart.reduce((s, r) => s + (r.allocatedCreditLimit || 0), 0);
    input.style.borderColor = totalAllocated > masterCreditLimit ? '#ef4444' : '#cbd5e1';
}

/**
 * Xử lý nút "Thêm phòng vào cart": lấy giá trị từ dropdown rồi gọi proceedAddRoomToCart.
 */
function addRoomToCart() {
    const select = document.getElementById('walkInPhysicalRoomSelect');
    const roomId = select.value;
    if (!roomId) return;

    const selectedOpt = select.options[select.selectedIndex];
    const roomNum = selectedOpt.dataset.roomNum;
    const category = selectedOpt.dataset.category;
    const price = selectedOpt.dataset.price;

    if (walkInCart.find(r => r.roomId == roomId)) {
        alert('Phòng này đã có trong danh sách!');
        return;
    }

    proceedAddRoomToCart(roomId, roomNum, category, price);
}

/**
 * Thêm phòng vào giỏ và cập nhật UI (dùng chung cho addRoomToCart và escalateDirtyRoomWalkIn).
 */
function proceedAddRoomToCart(roomId, roomNum, category, price) {
    walkInCart.push({ roomId: parseInt(roomId), roomNum, category, price });
    renderRoomCart();
    unlockStep2();

    // Nếu bước thanh toán đang hiện → tính lại
    if (document.getElementById('step4Container').style.display === 'block') {
        showPaymentStep();
    }
}

/**
 * Xóa phòng khỏi giỏ theo index. Đồng thời xóa khách đi kèm đã gán phòng đó.
 */
function removeRoomFromCart(index) {
    const removedRoom = walkInCart[index];
    walkInCart.splice(index, 1);

    // Xóa dependents của phòng bị xóa
    walkInDependents = walkInDependents.filter(d => d.roomId != removedRoom.roomId);
    renderAccompaniedGuests();
    renderRoomCart();

    if (walkInCart.length === 0) {
        // Khóa lại Step 2 & 3 khi giỏ trống
        document.getElementById('step2Container').style.opacity = '0.5';
        document.getElementById('step2Container').style.pointerEvents = 'none';

        const step3 = document.getElementById('step3Container');
        if (step3) {
            step3.style.opacity = '0.5';
            step3.style.pointerEvents = 'none';
        }

        document.getElementById('step4Container').style.display = 'none';
        document.getElementById('continueToPaymentBtn').parentElement.style.display = 'flex';
    } else if (document.getElementById('step4Container').style.display === 'block') {
        // Tính lại nếu bước thanh toán đang hiện
        showPaymentStep();
    }
}

/**
 * Vẽ lại bảng giỏ phòng (Room Cart) và cập nhật dropdown "Phòng cho khách đi kèm".
 */
function renderRoomCart() {
    const container = document.getElementById('roomCartContainer');
    const body = document.getElementById('roomCartBody');
    const depRoomSelect = document.getElementById('depRoom');

    body.innerHTML = '';
    depRoomSelect.innerHTML = '<option value="">-- Chọn phòng --</option>';

    if (walkInCart.length === 0) {
        container.style.display = 'none';
        return;
    }

    container.style.display = 'block';

    // Ẩn/hiện cột hạn mức tín dụng tùy theo trạng thái xác thực khách
    const remainingCreditWrapper = document.getElementById('remainingCreditWrapper');
    const creditLimitHeader = document.getElementById('creditLimitHeader');
    if (isGuestVerified) {
        if (remainingCreditWrapper) remainingCreditWrapper.style.display = 'inline';
        if (creditLimitHeader) creditLimitHeader.style.display = 'table-cell';
    } else {
        if (remainingCreditWrapper) remainingCreditWrapper.style.display = 'none';
        if (creditLimitHeader) creditLimitHeader.style.display = 'none';
    }

    walkInCart.forEach((room, index) => {
        const tr = document.createElement('tr');
        tr.style.borderBottom = '1px dashed #e2e8f0';

        const creditCell = isGuestVerified
            ? `<td style="padding: 10px 16px;">
                <input type="number" class="form-control room-credit-input" data-index="${index}" value="${room.allocatedCreditLimit || ''}" min="0" oninput="handleCreditInput(this, ${index})" style="width:120px; padding: 6px; border: 1px solid #cbd5e1; border-radius: 4px;">
               </td>`
            : `<td style="display: none;"></td>`;

        tr.innerHTML = `
            <td style="padding: 10px 16px; font-weight: 500;">${room.roomNum}</td>
            <td style="padding: 10px 16px;">${room.category}</td>
            <td style="padding: 10px 16px; font-weight: 500; color: #16a34a;">${room.price}</td>
            ${creditCell}
            <td style="padding: 10px 16px; text-align: right;">
                <button type="button" class="btn btn-sm btn-outline" style="color: #ef4444; border-color: #fca5a5;" onclick="removeRoomFromCart(${index})"><i class="fa-solid fa-trash"></i></button>
            </td>
        `;
        body.appendChild(tr);

        // Thêm phòng vào dropdown chọn phòng cho khách đi kèm
        const opt = document.createElement('option');
        opt.value = room.roomId;
        opt.innerText = room.roomNum;
        depRoomSelect.appendChild(opt);
    });

    updateCreditLimitDisplay();
}


// =============================================================================
// 5. THÔNG TIN KHÁCH HÀNG & XÁC THỰC
// =============================================================================

/**
 * Mở khóa nút "Tiếp tục thanh toán" và hiển thị Customer ID.
 * @param {string} customerId - ID khách hàng dạng "CUST-123" hoặc "NEW-GUEST"
 */
function unlockFinalButton(customerId) {
    document.getElementById('guestStatusMsg').style.display = 'inline-block';
    document.getElementById('mockCustomerId').innerText = customerId;

    const continueBtn = document.getElementById('continueToPaymentBtn');
    if (continueBtn) {
        continueBtn.style.opacity = '1';
        continueBtn.style.pointerEvents = 'auto';
    }
}

/**
 * Tìm kiếm khách hàng cũ theo số điện thoại hoặc CCCD.
 * Tự động điền thông tin nếu tìm thấy.
 */
function searchCustomer() {
    const phone = document.getElementById('guestPhone').value;
    const cccd = document.getElementById('guestId').value;
    const keyword = phone || cccd;

    if (!keyword) {
        alert('Vui lòng nhập Số điện thoại hoặc CCCD/Passport để tìm kiếm.');
        return;
    }

    fetch('/api/receptionist/walkin/search-customer?keyword=' + encodeURIComponent(keyword))
        .then(response => {
            if (response.ok) return response.json();
            if (response.status === 404) throw new Error('Không tìm thấy khách hàng với SĐT hoặc CCCD vừa nhập.');
            throw new Error('Lỗi hệ thống khi tìm kiếm khách hàng.');
        })
        .then(data => {
            // Điền thông tin khách vào form
            document.getElementById('guestName').value = data.fullName || '';
            document.getElementById('guestId').value = data.cccd || '';
            document.getElementById('guestEmail').value = data.email || '';

            masterCreditLimit = (data.creditLimit !== undefined && data.creditLimit !== null)
                ? data.creditLimit
                : 5000000;

            isGuestVerified = true;
            renderRoomCart();
            updateCreditLimitDisplay();

            if (data.dateOfBirth) {
                document.getElementById('guestDob').value = data.dateOfBirth;
            }

            unlockFinalButton('CUST-' + data.id);

            const statusEl = document.getElementById('guestStatusMsg');
            statusEl.innerHTML = `<i class="fa-solid fa-check-circle"></i> Đã tìm thấy khách hàng cũ (ID: <span id="mockCustomerId">${data.id}</span>)`;
            statusEl.style.color = '#16a34a';
            statusEl.style.background = '#dcfce7';
        })
        .catch(error => {
            alert(error.message);
        });
}

/**
 * Xác thực thông tin khách hàng nhập tay trên form (Step 2).
 * @returns {boolean} true nếu hợp lệ, false nếu có lỗi
 */
function validateGuestInfo() {
    const name = document.getElementById('guestName').value;
    const phone = document.getElementById('guestPhone').value;
    const id = document.getElementById('guestId').value;
    const email = document.getElementById('guestEmail').value;
    const dob = document.getElementById('guestDob').value;

    if (!name || !phone || !id || !dob) {
        alert('Vui lòng điền đầy đủ thông tin bắt buộc (*), bao gồm cả Ngày sinh.');
        return false;
    }

    // Kiểm tra ngày sinh không ở tương lai
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const birthDate = new Date(dob);
    if (birthDate > today) {
        alert('LỖI: Ngày sinh không thể ở trong tương lai!');
        return false;
    }

    // Kiểm tra tuổi tối thiểu 18
    let age = today.getFullYear() - birthDate.getFullYear();
    const m = today.getMonth() - birthDate.getMonth();
    if (m < 0 || (m === 0 && today.getDate() < birthDate.getDate())) age--;
    if (age < 18) {
        alert('LỖI: Khách hàng phải từ 18 tuổi trở lên mới được phép đứng tên đăng ký phòng!');
        return false;
    }

    // Kiểm tra định dạng số điện thoại Việt Nam
    const phoneRegex = /^(0|\+84)[3|5|7|8|9][0-9]{8}$/;
    if (!phoneRegex.test(phone)) {
        alert('Số điện thoại không hợp lệ. Vui lòng nhập số điện thoại Việt Nam hợp lệ (VD: 0901234567).');
        return false;
    }

    // Kiểm tra email nếu có nhập
    if (email) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(email)) {
            alert('Định dạng Email không hợp lệ.');
            return false;
        }
    }

    // Kiểm tra định dạng CCCD (12 chữ số)
    const cccdRegex = /^\d{12}$/;
    if (!cccdRegex.test(id)) {
        alert('CCCD không hợp lệ. Vui lòng nhập đúng 12 chữ số CCCD.');
        return false;
    }

    return true;
}

/**
 * Xác nhận khách vãng lai mới (chưa có trong hệ thống).
 * Gán hạn mức mặc định 5 triệu và mở khóa bước tiếp theo.
 */
function validateNewCustomer() {
    if (!validateGuestInfo()) return;

    masterCreditLimit = 5000000; // Hạn mức mặc định cho khách vãng lai mới
    isGuestVerified = true;
    renderRoomCart();
    updateCreditLimitDisplay();

    unlockFinalButton('NEW-GUEST');

    const statusEl = document.getElementById('guestStatusMsg');
    statusEl.innerHTML = `<i class="fa-solid fa-info-circle"></i> Sẵn sàng Check-in (Tài khoản sẽ được tạo tự động)`;
    statusEl.style.display = 'inline-block';
    statusEl.style.color = '#0284c7';
    statusEl.style.background = '#e0f2fe';
}


// =============================================================================
// 6. BƯỚC THANH TOÁN & TÍNH TIỀN (STEP 4)
// =============================================================================

/** Tổng tiền phòng hiện tại (sau tính phụ thu giờ cao điểm). */
let currentTotalCharge = 0;

/**
 * Tính và hiển thị bước thanh toán (Step 4).
 * Gọi API /calculate-surcharge để lấy phí phụ thu theo giờ.
 */
function showPaymentStep() {
    if (walkInCart.length === 0) {
        alert('Vui lòng chọn ít nhất 1 phòng để Check-in!');
        return;
    }

    const checkOutDate = document.getElementById('checkoutDate').value;
    if (!checkOutDate) {
        alert('Vui lòng nhập Ngày Trả Phòng dự kiến!');
        return;
    }

    if (!validateGuestInfo()) return;

    // Tính ngày check-in là hôm nay theo local time
    const localNow = new Date();
    const checkInStr = `${localNow.getFullYear()}-${String(localNow.getMonth() + 1).padStart(2, '0')}-${String(localNow.getDate()).padStart(2, '0')}`;
    const dob = document.getElementById('guestDob').value;

    const payload = {
        roomSelections: buildRoomSelections(),
        checkInDate: checkInStr,
        checkOutDate: checkOutDate,
        dateOfBirth: dob
    };

    fetch('/api/receptionist/walkin/calculate-surcharge', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    })
        .then(res => {
            if (!res.ok) throw new Error('Lỗi Server khi tính phí');
            return res.json();
        })
        .then(data => {
            document.getElementById('step4Container').style.display = 'block';
            document.getElementById('continueToPaymentBtn').parentElement.style.display = 'none';

            currentTotalCharge = data.totalCharge || 0;

            // Tính số đêm lưu trú
            const ci = new Date(); ci.setHours(0, 0, 0, 0);
            const co = new Date(checkOutDate); co.setHours(0, 0, 0, 0);
            let nights = Math.round(Math.abs(co - ci) / (1000 * 60 * 60 * 24));
            if (nights <= 0) nights = 1;

            const formatter = new Intl.NumberFormat('vi-VN');

            // Cập nhật nhãn "Tổng tiền phòng (X đêm)"
            const labelEl = document.getElementById('summaryBasePriceLabel');
            if (labelEl) labelEl.innerText = `Tổng tiền phòng (${nights} đêm):`;

            // Vẽ breakdown từng phòng
            const breakdownContainer = document.getElementById('summaryRoomBreakdown');
            if (breakdownContainer) {
                breakdownContainer.innerHTML = '';
                walkInCart.forEach(r => {
                    const row = document.createElement('div');
                    row.style.display = 'flex';
                    row.style.justifyContent = 'space-between';
                    row.style.marginBottom = '6px';
                    row.style.fontSize = '13px';
                    row.innerHTML = `
                    <span style="color: #475569;"><i class="fa-solid fa-bed" style="font-size: 11px; margin-right: 6px; color: #94a3b8;"></i>Phòng ${r.roomNum} (${r.category})</span>
                    <span style="font-weight: 500; color: #334155;">${r.price || ''}</span>
                `;
                    breakdownContainer.appendChild(row);
                });
            }

            // Cập nhật tóm tắt chi phí
            document.getElementById('summaryBasePrice').innerText = formatter.format(data.baseRoomPrice || 0) + ' VNĐ';
            document.getElementById('summarySurcharge').innerText = formatter.format(data.surchargeAmount || 0) + ' VNĐ';
            document.getElementById('summaryTotal').innerText = formatter.format(currentTotalCharge) + ' VNĐ';

            updateWalkInDeposit();

            document.getElementById('step4Container').scrollIntoView({ behavior: 'smooth' });
        })
        .catch(err => {
            alert('Lỗi tính toán: ' + err.message);
        });
}

/**
 * Cập nhật số tiền cọc dựa trên lựa chọn "100%" hay "30%".
 */
function updateWalkInDeposit() {
    const option = document.querySelector('input[name="paymentOption"]:checked');
    if (!option) return;

    let deposit = currentTotalCharge;
    if (option.value === '30') {
        deposit = Math.round(currentTotalCharge * 0.3);
    }

    const formatter = new Intl.NumberFormat('vi-VN');
    document.getElementById('depositAmount').value = deposit;
    document.getElementById('depositAmountDisplay').value = formatter.format(deposit);
}

/** Ẩn bước thanh toán, quay về Step 3. */
function backToStep3() {
    document.getElementById('step4Container').style.display = 'none';
    document.getElementById('continueToPaymentBtn').parentElement.style.display = 'flex';
}

/**
 * Tổng hợp thông tin các phòng trong cart thành payload cho API.
 * Dùng chung giữa showPaymentStep() và submitCheckIn().
 * @returns {Array} Mảng roomSelections
 */
function buildRoomSelections() {
    return walkInCart.map(r => ({
        roomId: r.roomId,
        allocatedCreditLimit: r.allocatedCreditLimit || 0,
        accompaniedGuests: walkInDependents.filter(d => d.roomId == r.roomId)
    }));
}

/**
 * Hoàn tất quá trình Walk-In Check-in: xác thực, build payload và gửi API.
 */
function submitCheckIn() {
    if (walkInCart.length === 0) {
        alert('Vui lòng chọn ít nhất 1 phòng để Check-in!');
        return;
    }

    const checkOutDate = document.getElementById('checkoutDate').value;
    if (!checkOutDate) {
        alert('Vui lòng nhập Ngày Trả Phòng dự kiến!');
        return;
    }

    // Ngày trả phòng không được ở quá khứ
    const today = new Date(); today.setHours(0, 0, 0, 0);
    const selectedDate = new Date(checkOutDate);
    if (selectedDate < today) {
        alert('LỖI: Ngày trả phòng (Checkout Date) không thể ở trong quá khứ!');
        return;
    }

    // Xác thực lại thông tin khách trước khi gửi (tránh user sửa data sau khi Unlock)
    if (!validateGuestInfo()) return;

    // Kiểm tra mỗi phòng (từ phòng thứ 2 trở đi) đã có người đứng đầu chưa
    const primaryRoomIds = walkInDependents.filter(d => d.isPrimaryContact).map(d => d.roomId);
    const missingRooms = [];
    for (let i = 1; i < walkInCart.length; i++) {
        const room = walkInCart[i];
        // roomId trong walkInCart lưu dạng number, dep.roomId lưu dạng string → so sánh qua String()
        if (!primaryRoomIds.includes(String(room.roomId))) {
            missingRooms.push(room.roomNum);
        }
    }
    if (missingRooms.length > 0) {
        alert(`Thiếu người đứng đầu cho phòng: ${missingRooms.join(', ')}. Vui lòng quay lại Step 3 và chọn 1 người đi kèm làm người đứng đầu cho mỗi phòng này trước khi hoàn tất!`);
        return;
    }

    // Build payload
    const localNow = new Date();
    const checkInStr = `${localNow.getFullYear()}-${String(localNow.getMonth() + 1).padStart(2, '0')}-${String(localNow.getDate()).padStart(2, '0')}`;

    const payload = {
        roomSelections: buildRoomSelections(),
        checkInDate: checkInStr,
        checkOutDate: checkOutDate,
        fullName: document.getElementById('guestName').value,
        phone: document.getElementById('guestPhone').value,
        cccd: document.getElementById('guestId').value,
        email: document.getElementById('guestEmail').value,
        dateOfBirth: document.getElementById('guestDob').value,
        gender: document.getElementById('guestGender') ? document.getElementById('guestGender').value : 'Nam',
        accompaniedGuests: walkInDependents,
        depositAmount: parseFloat(document.getElementById('depositAmount').value) || 0,
        paymentMethod: document.getElementById('paymentMethod').value || 'Tiền mặt'
    };

    // Đổi trạng thái nút thành Loading
    const completeBtn = document.getElementById('completeBtn');
    const oldBtnText = completeBtn.innerHTML;
    completeBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Processing...';
    completeBtn.disabled = true;

    fetch('/api/receptionist/walkin/checkin', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    })
        .then(response => {
            if (!response.ok) {
                return response.json().then(err => {
                    throw new Error(err.error || err.message || 'Lỗi khi Check-in');
                });
            }
            return response.json();
        })
        .then(data => {
            // Xóa danh sách phòng "treo" để tránh toast trùng với pollCleanedRooms()
            try { sessionStorage.removeItem('walkInPendingCleanRooms'); } catch (e) { /* ignore */ }

            // Cập nhật nội dung modal thành công
            const modal = document.getElementById('successModal');
            const msg = document.getElementById('modalMessage');
            const accInfo = document.getElementById('modalAccountInfo');
            const closeBtn = document.getElementById('modalCloseBtn');

            const isNewCustomer = data.newCustomer === true || data.isNewCustomer === true;
            if (isNewCustomer && data.newAccountUsername) {
                msg.innerHTML = 'Hệ thống đã <b>tự động tạo hồ sơ</b> khách hàng mới và làm thủ tục nhận phòng thành công.';
            } else {
                msg.innerText = 'Đã làm thủ tục nhận phòng hoàn tất cho khách lưu trú này.';
            }
            if (accInfo) accInfo.style.display = 'none';

            if (data.paymentUrl) {
                // Thanh toán qua VNPay: nút chuyển hướng + nút hủy check-in
                if (closeBtn) {
                    closeBtn.innerHTML = '<i class="fa-solid fa-qrcode"></i> Thanh toán VNPay';
                    closeBtn.onclick = function () {
                        closeBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Đang chuyển hướng...';
                        window.location.href = data.paymentUrl;
                    };

                    let cancelBtn = document.getElementById('modalCancelBtn');
                    if (!cancelBtn) {
                        cancelBtn = document.createElement('button');
                        cancelBtn.id = 'modalCancelBtn';
                        cancelBtn.className = 'btn btn-secondary';
                        cancelBtn.style.padding = '8px 24px';
                        cancelBtn.style.marginRight = '10px';
                        cancelBtn.style.backgroundColor = '#64748b';
                        cancelBtn.style.color = 'white';
                        cancelBtn.style.border = 'none';
                        cancelBtn.style.borderRadius = '6px';
                        closeBtn.parentNode.insertBefore(cancelBtn, closeBtn);
                    }
                    cancelBtn.style.display = 'inline-block';
                    cancelBtn.style.backgroundColor = '#e11d48';
                    cancelBtn.innerHTML = 'Hủy Check-in';
                    cancelBtn.onclick = function () {
                        cancelBtn.disabled = true;
                        cancelBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Đang hủy...';
                        fetch('/api/receptionist/walkin/cancel-pending/' + data.bookingId, { method: 'POST' })
                            .then(() => { window.location.href = '/receptionist/dashboard'; })
                            .catch(err => {
                                alert('Lỗi khi hủy: ' + err);
                                cancelBtn.disabled = false;
                                cancelBtn.innerHTML = 'Hủy Check-in';
                            });
                    };
                }
            } else {
                // Thanh toán tiền mặt: nút đóng về danh sách lưu trú
                if (closeBtn) {
                    closeBtn.innerHTML = 'Đóng & Về danh sách Lưu trú';
                    closeBtn.onclick = function () {
                        window.location.href = '/receptionist/in-house?t=' + new Date().getTime();
                    };
                    const cancelBtn = document.getElementById('modalCancelBtn');
                    if (cancelBtn) cancelBtn.style.display = 'none';
                }
            }

            if (modal) {
                modal.style.display = 'flex';
            } else {
                // Fallback nếu không có modal trong HTML
                if (data.paymentUrl) {
                    window.location.href = data.paymentUrl;
                } else {
                    alert('Check-in thành công!');
                    window.location.href = '/receptionist/in-house?t=' + new Date().getTime();
                }
            }
        })
        .catch(error => {
            alert('Lỗi Check-in: ' + error.message);
            completeBtn.innerHTML = '<i class="fa-solid fa-check-double"></i> Complete Walk-In Check-in';
            completeBtn.disabled = false;
        });
}

/**
 * Hàm đóng modal mặc định (khi onclick trong HTML chưa bị override bởi submitCheckIn).
 */
function defaultModalClose() {
    document.getElementById('successModal').style.display = 'none';
    window.location.href = '/receptionist/in-house?t=' + new Date().getTime();
}


// =============================================================================
// 7. KHÁCH ĐI KÈM (ACCOMPANIED GUESTS / DEPENDENTS)
// =============================================================================

/** Danh sách khách đi kèm. Mỗi phần tử: { fullName, dateOfBirth, cccd, gender, roomId, roomNum, isPrimaryContact }. */
let walkInDependents = [];

/**
 * Vẽ lại bảng danh sách khách đi kèm.
 */
function renderAccompaniedGuests() {
    const list = document.getElementById('accompaniedGuestsList');
    list.innerHTML = '';

    if (walkInDependents.length === 0) {
        list.innerHTML = '<tr><td colspan="6" style="text-align:center; padding: 16px; color: #94a3b8; font-size: 13px;">Chưa có khách đi kèm</td></tr>';
        return;
    }

    walkInDependents.forEach((dep, index) => {
        const roleBadge = dep.isPrimaryContact
            ? `<span style="display:inline-block; padding: 2px 6px; background: #fef3c7; color: #d97706; border-radius: 4px; font-size: 11px; font-weight: 600;"><i class="fa-solid fa-star"></i> Đứng đầu</span>`
            : `<span style="font-size: 13px; color: #64748b;">Thành viên</span>`;

        const roomDisplay = dep.roomNum ? `<span style="font-weight: 500; color: #334155;">Phòng ${dep.roomNum}</span><br>` : '';
        const finalRoleDisplay = `${roomDisplay}${roleBadge}`;

        const tr = document.createElement('tr');
        tr.style.borderBottom = '1px dashed #e2e8f0';
        tr.innerHTML = `
            <td style="padding: 12px 8px;">${dep.fullName}</td>
            <td style="padding: 12px 8px;">${dep.dateOfBirth}</td>
            <td style="padding: 12px 8px;">${dep.cccd || '-'}</td>
            <td style="padding: 12px 8px;">${finalRoleDisplay}</td>
            <td style="padding: 12px 8px;">${dep.gender}</td>
            <td style="padding: 12px 8px;">
                <button type="button" class="btn btn-sm" style="background: #fee2e2; color: #ef4444; border: 1px solid #fca5a5; padding: 4px 8px;" onclick="removeAccompaniedGuest(${index})">
                    <i class="fa-solid fa-trash"></i>
                </button>
            </td>
        `;
        list.appendChild(tr);
    });
}

/**
 * Thêm khách đi kèm từ form (Step 3).
 * Validate: không trùng người đứng đầu, ngày sinh hợp lệ, CCCD hợp lệ.
 */
function addAccompaniedGuest() {
    const name = document.getElementById('depName').value;
    const dob = document.getElementById('depDob').value;
    const cccd = document.getElementById('depId').value;
    const gender = document.getElementById('depGender').value;
    const isPrimary = document.getElementById('depIsPrimary').checked;

    const roomSelect = document.getElementById('depRoom');
    const roomId = roomSelect.value;
    const roomNum = roomId ? roomSelect.options[roomSelect.selectedIndex].text : '';

    if (!name || !dob || !roomId) {
        alert('Vui lòng nhập đầy đủ Họ tên, Ngày sinh và Chọn phòng xếp cho người đi kèm!');
        return;
    }

    if (isPrimary) {
        // Phòng đầu tiên đã do khách chính đứng đầu
        if (walkInCart.length > 0 && roomId == walkInCart[0].roomId) {
            alert(`Phòng ${roomNum} đã được chỉ định cho khách chính đứng đầu! Vui lòng không chọn người đi kèm làm người đứng đầu cho phòng này.`);
            return;
        }
        // Mỗi phòng chỉ được có 1 người đứng đầu
        const conflict = walkInDependents.some(dep => dep.roomId === roomId && dep.isPrimaryContact);
        if (conflict) {
            alert(`Phòng ${roomNum} đã có người đứng đầu! Vui lòng chọn người khác hoặc bỏ chọn người đứng đầu cũ.`);
            return;
        }
    }

    // Ngày sinh không được ở tương lai
    const todayStr = new Date().toISOString().split('T')[0];
    if (dob > todayStr) {
        alert('Ngày sinh không được ở tương lai!');
        return;
    }

    // CCCD phải đúng 12 chữ số nếu có nhập
    if (cccd) {
        const cccdRegex = /^\d{12}$/;
        if (!cccdRegex.test(cccd)) {
            alert('CCCD không hợp lệ! Phải gồm đúng 12 chữ số.');
            return;
        }
    }

    walkInDependents.push({
        fullName: name,
        dateOfBirth: dob,
        cccd: cccd || null,
        gender: gender,
        roomId: roomId,
        roomNum: roomNum,
        isPrimaryContact: isPrimary
    });

    // Reset form nhập liệu khách đi kèm
    document.getElementById('depName').value = '';
    document.getElementById('depDob').value = '';
    document.getElementById('depId').value = '';
    document.getElementById('depGender').value = 'Nam';
    document.getElementById('depRoom').value = '';
    document.getElementById('depIsPrimary').checked = false;

    renderAccompaniedGuests();

    // Cập nhật lại tính toán nếu bước thanh toán đang hiện
    if (document.getElementById('step4Container').style.display === 'block') {
        showPaymentStep();
    }
}

/**
 * Xóa khách đi kèm theo index.
 */
function removeAccompaniedGuest(index) {
    walkInDependents.splice(index, 1);
    renderAccompaniedGuests();

    if (document.getElementById('step4Container').style.display === 'block') {
        showPaymentStep();
    }
}

/**
 * Ẩn/hiện bảng danh sách khách đi kèm (toggle).
 */
function toggleWalkInDependents() {
    const wrapper = document.getElementById('dependentsTableWrapper');
    const btn = document.getElementById('toggleWalkInDependentsBtn');

    if (wrapper.style.display === 'none') {
        wrapper.style.display = 'block';
        btn.innerHTML = '<i class="fa-solid fa-chevron-up"></i> Thu gọn';
    } else {
        wrapper.style.display = 'none';
        btn.innerHTML = '<i class="fa-solid fa-chevron-down"></i> Hiện hết';
    }
}


// =============================================================================
// 8. AUTO-SAVE FORM VÀO LOCALSTORAGE (CHỐNG MẤT DỮ LIỆU KHI CHUYỂN TAB)
// =============================================================================

document.addEventListener('DOMContentLoaded', () => {
    // Key riêng cho từng page (dựa trên URL path)
    const pageKey = 'kawai_autosave_' + window.location.pathname.replace(/[^a-zA-Z0-9]/g, '_');

    // ---- Khôi phục dữ liệu đã lưu ----
    const savedDataStr = localStorage.getItem(pageKey);
    if (savedDataStr) {
        try {
            const savedData = JSON.parse(savedDataStr);
            document.querySelectorAll('input, select, textarea').forEach(el => {
                const key = el.id || el.name;
                // Bỏ qua trường nhạy cảm hoặc không cần lưu
                if (!key || el.type === 'password' || el.type === 'file' || el.type === 'hidden') return;
                if (key.toLowerCase().includes('search') || key.toLowerCase().includes('keyword')) return;

                if (savedData[key] !== undefined) {
                    if (el.type === 'checkbox' || el.type === 'radio') {
                        el.checked = savedData[key];
                    } else {
                        el.value = savedData[key];
                    }
                }
            });
            console.log('Khôi phục dữ liệu đang nhập dở thành công.');
        } catch (e) {
            console.error('Lỗi khi khôi phục dữ liệu autosave:', e);
        }
    }

    // ---- Lắng nghe & lưu dữ liệu theo thời gian thực (event delegation) ----
    document.body.addEventListener('input', (e) => {
        const el = e.target;
        if (el.tagName !== 'INPUT' && el.tagName !== 'SELECT' && el.tagName !== 'TEXTAREA') return;

        const key = el.id || el.name;
        if (!key || el.type === 'password' || el.type === 'file' || el.type === 'hidden') return;
        if (key.toLowerCase().includes('search') || key.toLowerCase().includes('keyword')) return;

        const currentData = JSON.parse(localStorage.getItem(pageKey) || '{}');
        if (el.type === 'checkbox' || el.type === 'radio') {
            currentData[key] = el.checked;
        } else {
            currentData[key] = el.value;
        }
        localStorage.setItem(pageKey, JSON.stringify(currentData));
    });

    // ---- Xóa dữ liệu khi submit form truyền thống ----
    document.querySelectorAll('form').forEach(form => {
        form.addEventListener('submit', () => {
            localStorage.removeItem(pageKey);
        });
    });

    // ---- Gắn đè fetch để xóa autosave khi check-in thành công qua API ----
    const originalFetch = window.fetch;
    window.fetch = async function () {
        const response = await originalFetch.apply(this, arguments);
        const url = arguments[0];
        if (response.ok && typeof url === 'string' &&
            (url.includes('/walkin/checkin') || url.includes('/checkin/complete'))) {
            localStorage.removeItem(pageKey);
        }
        return response;
    };
});
