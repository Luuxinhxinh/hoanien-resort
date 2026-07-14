// ==============================================
// SECTION 1: GLOBAL STATE & CONSTANTS
// ==============================================

let checkinMasterCreditLimit = 5000000;
let expectedTotalGuests = 0;
let expectedTotalAdults = 0;
let expectedTotalChildren = 0;
let maxTotalAdults = 0;
let maxTotalChildren = 0;

let assignedRooms = [];
let depIndexCounter = 0;
let currentEditingDependentRow = null;
let currentTours = [];

// Face ID state
let faceApiLoaded = false;
let videoStream = null;
let currentEnrollType = null;
let currentEnrollId = null;
let pendingFaceEnrollments = {};
const MODEL_URL = 'https://cdn.jsdelivr.net/npm/@vladmandic/face-api@1.7.12/model/';

// QR / Remote Scan state
let html5QrcodeScanner = null;
let currentQrTarget = null;
let remoteScanEventSource = null;
let remoteScanQrCode = null;
const persistentSessionId = (window.crypto && crypto.randomUUID)
    ? crypto.randomUUID()
    : ('session-' + Date.now() + '-' + Math.random().toString(36).substring(2, 10));


// ==============================================
// SECTION 2: UTILITIES & HELPERS
// ==============================================

function showToast(msg, type = 'error') {
    let container = document.getElementById('custom-toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'custom-toast-container';

        const style = document.createElement('style');
        style.innerHTML = `
            #custom-toast-container {
                position: fixed;
                top: 20px;
                right: 20px;
                z-index: 99999;
                display: flex;
                flex-direction: column;
                gap: 10px;
            }
            .custom-toast {
                display: flex;
                align-items: flex-start;
                padding: 16px;
                border-radius: 8px;
                box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
                border-left: 4px solid;
                width: 320px;
                transform: translateX(120%);
                opacity: 0;
                transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
                font-family: 'Inter', sans-serif;
            }
            .custom-toast.show {
                transform: translateX(0);
                opacity: 1;
            }
            .custom-toast.error {
                background-color: #fef2f2;
                border-color: #ef4444;
                color: #7f1d1d;
            }
            .custom-toast.success {
                background-color: #f0fdf4;
                border-color: #22c55e;
                color: #14532d;
            }
            .custom-toast-icon {
                font-size: 20px;
                margin-right: 12px;
                margin-top: 2px;
            }
            .custom-toast.error .custom-toast-icon {
                color: #ef4444;
            }
            .custom-toast.success .custom-toast-icon {
                color: #22c55e;
            }
            .custom-toast-content {
                flex: 1;
            }
            .custom-toast-title {
                font-weight: 700;
                font-size: 13px;
                text-transform: uppercase;
                letter-spacing: 0.025em;
                margin: 0 0 4px 0;
            }
            .custom-toast-message {
                font-size: 12px;
                opacity: 0.9;
                margin: 0;
                line-height: 1.4;
            }
        `;
        document.head.appendChild(style);
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `custom-toast ${type}`;

    const icon = type === 'error' ? 'fa-circle-exclamation' : 'fa-circle-check';
    const title = type === 'error' ? 'LỖI' : 'THÀNH CÔNG';

    toast.innerHTML = `
        <i class="fa-solid ${icon} custom-toast-icon"></i>
        <div class="custom-toast-content">
            <h4 class="custom-toast-title">${title}</h4>
            <p class="custom-toast-message">${msg}</p>
        </div>
    `;

    container.appendChild(toast);

    requestAnimationFrame(() => {
        setTimeout(() => toast.classList.add('show'), 10);
    });

    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

function calculateAge(dobStr) {
    if (!dobStr) return 0;
    const dob = new Date(dobStr);
    const diff = Date.now() - dob.getTime();
    return Math.floor(diff / (1000 * 60 * 60 * 24 * 365.25));
}

/**
 * Fetch helper: tự động đính kèm CSRF token cho các request POST/DELETE/PUT.
 * Dùng cho các endpoint nội bộ cần bảo vệ CSRF.
 */
function csrfFetch(url, method) {
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content') || '';
    return fetch(url, {
        method,
        headers: { 'X-CSRF-TOKEN': csrfToken }
    }).then(res => res.json());
}


// ==============================================
// SECTION 3: ROOM ASSIGNMENT MODULE
// ==============================================

function updateAvailableRooms() {
    const typeSelect = document.getElementById('bookTypeSelect');
    const roomSelect = document.getElementById('physicalRoomSelect');
    roomSelect.innerHTML = '<option value="">-- Select Room --</option>';

    const selectedType = typeSelect.value;
    if (!selectedType) return;

    const inventoryForType = roomInventory[selectedType] || [];
    const available = inventoryForType.filter(r => !assignedRooms.some(a => a.room === r.number));

    if (available.length === 0) return;

    available.forEach(r => {
        const opt = document.createElement('option');
        opt.value = r.number;
        opt.innerText = r.number + (r.status === 'Vacant_Dirty' ? ' (Chưa dọn)' : '');
        opt.setAttribute('data-status', r.status);
        roomSelect.appendChild(opt);
    });
}

// Dirty-room inline warning listener
document.addEventListener('DOMContentLoaded', () => {
    const roomSelect = document.getElementById('physicalRoomSelect');
    if (roomSelect) {
        roomSelect.addEventListener('change', function () {
            const opt = this.options[this.selectedIndex];
            if (!opt) return;
            const status = opt.getAttribute('data-status');
            const gridContainer = this.closest('div[style*="display: grid"]');
            if (!gridContainer) return;

            const existingWarning = document.getElementById('dirtyRoomWarningInline');
            if (existingWarning) existingWarning.remove();

            const assignBtn = gridContainer.querySelector('button[onclick="assignRoom()"]');

            if (status === 'Vacant_Dirty') {
                const warningDiv = document.createElement('div');
                warningDiv.id = 'dirtyRoomWarningInline';
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
                        <i class="fa-solid fa-triangle-exclamation"></i> <strong>Cảnh báo:</strong> Phòng <b>${this.value}</b> chưa dọn dẹp. Vui lòng chọn hành động:
                    </div>
                    <div style="display: flex; gap: 8px; flex-wrap: wrap;">
                        <button type="button" style="background:#3b82f6; color:white; border:none; padding: 6px 12px; border-radius: 4px; font-size: 13px; cursor: pointer; display: flex; align-items: center; gap: 6px;" onclick="escalateDirtyRoom('${this.value}', false)">
                            <i class="fa-solid fa-broom"></i> Chỉ yêu cầu dọn (Chưa gán)
                        </button>
                        <button type="button" style="background:#ef4444; color:white; border:none; padding: 6px 12px; border-radius: 4px; font-size: 13px; cursor: pointer; display: flex; align-items: center; gap: 6px;" onclick="escalateDirtyRoom('${this.value}', true)">
                            <i class="fa-solid fa-bolt"></i> Yêu cầu dọn & Phân phòng (Treo)
                        </button>
                    </div>
                `;
                gridContainer.appendChild(warningDiv);

                if (assignBtn) {
                    assignBtn.disabled = true;
                    assignBtn.style.opacity = '0.5';
                    assignBtn.style.cursor = 'not-allowed';
                }
            } else {
                if (assignBtn) {
                    assignBtn.disabled = false;
                    assignBtn.style.opacity = '1';
                    assignBtn.style.cursor = 'pointer';
                }
            }
        });
    }
});

window.escalateDirtyRoom = function (roomNumber, assignAfter) {
    csrfFetch(`/receptionist/rooms/escalate-dirty?roomNumber=${roomNumber}`, 'POST')
        .then(data => {
            if (data.status === 'success') {
                showToast('Đã gửi yêu cầu dọn khẩn cấp cho buồng phòng.', 'success');

                const typeSelect = document.getElementById('bookTypeSelect');
                const roomSelect = document.getElementById('physicalRoomSelect');

                const existingWarning = document.getElementById('dirtyRoomWarningInline');
                if (existingWarning) existingWarning.remove();

                const gridContainer = roomSelect.closest('div[style*="display: grid"]');
                if (gridContainer) {
                    const assignBtn = gridContainer.querySelector('button[onclick="assignRoom()"]');
                    if (assignBtn) {
                        assignBtn.disabled = false;
                        assignBtn.style.opacity = '1';
                        assignBtn.style.cursor = 'pointer';
                    }
                }

                if (assignAfter) {
                    const selectedType = typeSelect.value;
                    const selectedRoomOpt = roomSelect.options[roomSelect.selectedIndex];
                    try {
                        const pendingRaw = sessionStorage.getItem('walkInPendingCleanRooms');
                        const pendingRooms = pendingRaw ? JSON.parse(pendingRaw) : {};
                        pendingRooms[roomNumber] = true;
                        sessionStorage.setItem('walkInPendingCleanRooms', JSON.stringify(pendingRooms));
                    } catch (e) { }
                    proceedAssignRoom(typeSelect, selectedType, roomNumber, selectedRoomOpt);
                } else {
                    roomSelect.value = "";
                }
            } else {
                showToast('Lỗi khi gửi yêu cầu: ' + data.message, 'error');
            }
        }).catch(err => {
            console.error(err);
            showToast('Lỗi kết nối khi gửi yêu cầu khẩn cấp.', 'error');
        });
};

function assignRoom() {
    const typeSelect = document.getElementById('bookTypeSelect');
    const roomSelect = document.getElementById('physicalRoomSelect');
    const selectedType = typeSelect.value;
    const selectedRoom = roomSelect.value;

    if (!selectedType || !selectedRoom) return;

    const selectedRoomOpt = roomSelect.options[roomSelect.selectedIndex];
    proceedAssignRoom(typeSelect, selectedType, selectedRoom, selectedRoomOpt);
}

function proceedAssignRoom(typeSelect, selectedType, selectedRoom, selectedRoomOpt) {
    const selectedOption = typeSelect.options[typeSelect.selectedIndex];
    let pending = parseInt(selectedOption.getAttribute('data-pending'));

    if (pending > 0) {
        assignedRooms.push({ type: selectedType, room: selectedRoom });

        pending--;
        selectedOption.setAttribute('data-pending', pending);
        selectedOption.innerText = `${selectedType} (${pending} pending)`;

        if (pending === 0) {
            selectedOption.disabled = true;
            typeSelect.value = '';
        }

        updateAvailableRooms();
        renderAssignedRooms();
    }
}

function removeAssignedRoom(roomNumber) {
    const index = assignedRooms.findIndex(a => a.room === roomNumber);
    if (index > -1) {
        const type = assignedRooms[index].type;
        assignedRooms.splice(index, 1);

        const typeSelect = document.getElementById('bookTypeSelect');
        for (let i = 0; i < typeSelect.options.length; i++) {
            if (typeSelect.options[i].value === type) {
                let pending = parseInt(typeSelect.options[i].getAttribute('data-pending')) + 1;
                typeSelect.options[i].setAttribute('data-pending', pending);
                typeSelect.options[i].innerText = `${type} (${pending} pending)`;
                typeSelect.options[i].disabled = false;
                break;
            }
        }

        updateAvailableRooms();
        renderAssignedRooms();
    }
}

function renderAssignedRooms() {
    const container = document.getElementById('assignedRoomsList');
    container.innerHTML = '';

    document.querySelectorAll('input[name="assignedRoomNumbers"]').forEach(el => el.remove());
    document.querySelectorAll('input[name="allocatedCreditLimits"]').forEach(el => el.remove());

    const masterLabel = document.getElementById('masterCustomerRoomLabel');
    if (masterLabel) {
        if (assignedRooms.length > 0) {
            masterLabel.innerText = `(Phòng: ${assignedRooms[0].room})`;
            masterLabel.style.color = '#059669';
        } else {
            masterLabel.innerText = `(Chưa phân phòng)`;
            masterLabel.style.color = '#d97706';
        }
    }

    assignedRooms.forEach((a, index) => {
        const row = document.createElement('div');
        row.style.cssText = 'display: flex; align-items: center; justify-content: space-between; background:#e0f2fe; margin-bottom: 8px; padding: 8px 12px; border-radius: 6px; border: 1px solid #bae6fd;';

        const infoSpan = document.createElement('span');
        infoSpan.style.cssText = 'color:#0284c7; font-weight: 500; font-size: 14px;';
        infoSpan.innerText = `${a.room} (${a.type})`;

        const rightDiv = document.createElement('div');
        rightDiv.style.cssText = 'display: flex; align-items: center; gap: 10px;';

        const limitInput = document.createElement('input');
        limitInput.type = 'number';
        limitInput.className = 'form-control';
        limitInput.style.cssText = 'width: 120px; padding: 4px 8px; font-size: 13px; border: 1px solid #cbd5e1; border-radius: 4px;';
        limitInput.placeholder = 'Credit Limit';
        limitInput.value = (a.allocatedCreditLimit && a.allocatedCreditLimit > 0) ? a.allocatedCreditLimit : '';
        limitInput.min = "0";
        limitInput.oninput = function () { handleCheckinCreditInput(this, index); };

        const delBtn = document.createElement('i');
        delBtn.className = 'fa-solid fa-xmark';
        delBtn.style.cssText = 'cursor:pointer; color: #ef4444; font-size: 16px;';
        delBtn.onclick = function () { removeAssignedRoom(a.room); };

        rightDiv.appendChild(limitInput);
        rightDiv.appendChild(delBtn);
        row.appendChild(infoSpan);
        row.appendChild(rightDiv);
        container.appendChild(row);

        const hiddenInput = document.createElement('input');
        hiddenInput.type = 'hidden';
        hiddenInput.name = 'assignedRoomNumbers';
        hiddenInput.value = a.room;
        document.getElementById('checkinFormWrapper').appendChild(hiddenInput);

        const hiddenLimitInput = document.createElement('input');
        hiddenLimitInput.type = 'hidden';
        hiddenLimitInput.name = 'allocatedCreditLimits';
        hiddenLimitInput.value = a.allocatedCreditLimit || 0;
        document.getElementById('checkinFormWrapper').appendChild(hiddenLimitInput);
    });

    const depRoomSelect = document.getElementById('depRoom');
    if (depRoomSelect) {
        depRoomSelect.innerHTML = '<option value="">-- Select Room --</option>';
        assignedRooms.forEach(a => {
            const opt = document.createElement('option');
            opt.value = a.room;
            opt.innerText = `${a.room} (${a.type})`;
            depRoomSelect.appendChild(opt);
        });
    }

    document.querySelectorAll('select[id^="slot_room_"]').forEach(selectEl => {
        const currentValue = selectEl.value;
        selectEl.innerHTML = '<option value="">-- Chọn phòng --</option>';
        assignedRooms.forEach(a => {
            const opt = document.createElement('option');
            opt.value = a.room;
            opt.innerText = `Phòng ${a.room}`;
            selectEl.appendChild(opt);
        });
        if (assignedRooms.some(a => a.room === currentValue)) {
            selectEl.value = currentValue;
        }
    });

    updateTourPhysicalRooms();
    updateCheckinCreditLimitDisplay();
}


// ==============================================
// SECTION 4: CHECKIN MODAL & CREDIT LIMIT
// ==============================================

function handleCheckinCreditInput(input, index) {
    let val = parseFloat(input.value);
    if (isNaN(val) || val < 0) {
        val = 0;
        if (input.value !== '') input.value = '';
    }
    assignedRooms[index].allocatedCreditLimit = val;
    let hiddens = document.querySelectorAll('input[name="allocatedCreditLimits"]');
    if (hiddens && hiddens[index]) hiddens[index].value = val;
    updateCheckinCreditLimitDisplay();
    const totalAllocated = assignedRooms.reduce((s, r) => s + (r.allocatedCreditLimit || 0), 0);
    input.style.borderColor = totalAllocated > checkinMasterCreditLimit ? '#ef4444' : '#cbd5e1';
}

function updateCheckinCreditLimitDisplay() {
    let totalAllocated = 0;
    assignedRooms.forEach(r => totalAllocated += (r.allocatedCreditLimit || 0));
    let remaining = checkinMasterCreditLimit - totalAllocated;
    let displayEl = document.getElementById('checkinRemainingCreditDisplay');
    if (displayEl) {
        displayEl.innerText = remaining.toLocaleString() + ' VND';
        displayEl.style.color = remaining < 0 ? 'red' : '#16a34a';
    }
}

function openCheckinModal(bookingId, guestName, phone, cccd, dob, roomSummary, depsDivId, toursDivId, creditLimit, expectedGuests, expectedAdults, expectedChildren, maxAdults, maxChildren, gender) {
    console.log('[openCheckinModal] called:', { bookingId, guestName, phone, cccd, dob, roomSummary, depsDivId, toursDivId, creditLimit, expectedGuests, expectedAdults, expectedChildren, maxAdults, maxChildren, gender });

    const modalEl = document.getElementById('checkinModal');
    if (!modalEl) {
        console.error('[openCheckinModal] CRITICAL: #checkinModal không tìm thấy trong DOM!');
        return;
    }

    const currentBookingId = document.getElementById('submitBookingId').value;
    if (currentBookingId === bookingId.toString()) {
        modalEl.style.display = 'flex';
        return;
    }

    checkinMasterCreditLimit = creditLimit ? parseFloat(creditLimit) : 5000000;
    expectedTotalGuests = expectedGuests ? parseInt(expectedGuests) : 1;
    expectedTotalAdults = expectedAdults ? parseInt(expectedAdults) : 1;
    expectedTotalChildren = expectedChildren ? parseInt(expectedChildren) : 0;
    maxTotalAdults = maxAdults ? parseInt(maxAdults) : expectedTotalAdults;
    maxTotalChildren = maxChildren ? parseInt(maxChildren) : expectedTotalChildren;

    const capacitySpan = document.getElementById('modalExpectedCapacity');
    if (capacitySpan) {
        capacitySpan.innerHTML = `<i class="fa-solid fa-users" style="margin-right: 4px;"></i> Tiêu chuẩn: ${expectedTotalAdults} Người Lớn, ${expectedTotalChildren} Trẻ Em`;
        capacitySpan.style.display = 'inline-block';
    }

    updateCheckinCreditLimitDisplay();
    document.getElementById('submitBookingId').value = bookingId;

    const dependentsListContainer = document.getElementById('dependentsList');
    dependentsListContainer.innerHTML = '';
    depIndexCounter = 0;

    if (depsDivId) {
        const depsDiv = document.getElementById(depsDivId);
        if (depsDiv) {
            const items = depsDiv.querySelectorAll('li');
            items.forEach(li => {
                const name = li.getAttribute('data-name');
                const dob = li.getAttribute('data-dob');
                const dependentId = li.getAttribute('data-id');
                const cccd = li.getAttribute('data-cccd');
                const displayName = name ? name : '(Chưa cập nhật)';
                addDependentRow(displayName, cccd || '', dob || '', 'Khác', dependentId, null, false);
            });
        }
    }

    const alreadyRegistered = dependentsListContainer.querySelectorAll('tr[data-dob]').length;
    const totalSlotsNeeded = (expectedTotalAdults - 1) + expectedTotalChildren;
    const slotsToGenerate = Math.max(0, totalSlotsNeeded - alreadyRegistered);

    if (slotsToGenerate > 0) {
        for (let i = 0; i < slotsToGenerate; i++) {
            addDependentRow('(Chưa cập nhật)', '', '', 'Khác', null, null, false);
        }
    }

    if (dependentsListContainer.querySelectorAll('tr').length === 0) {
        dependentsListContainer.innerHTML = '<tr><td colspan="5" style="text-align:center; padding: 16px; color: #94a3b8; font-size: 13px;">No group members added yet</td></tr>';
    }

    document.getElementById('modalGuestName').value = guestName || '';
    if (document.getElementById('modalGuestPhone')) document.getElementById('modalGuestPhone').value = phone || '';
    if (document.getElementById('modalGuestCccd')) document.getElementById('modalGuestCccd').value = (cccd && cccd !== 'null') ? cccd : '';
    if (document.getElementById('modalGuestDob')) document.getElementById('modalGuestDob').value = (dob && dob !== 'null') ? dob : '';
    if (document.getElementById('modalGuestGender')) {
        const validGender = (gender === 'Nam' || gender === 'Nữ') ? gender : 'Khác';
        document.getElementById('modalGuestGender').value = validGender;
    }
    if (document.getElementById('modalRoom')) document.getElementById('modalRoom').innerText = (roomSummary && roomSummary !== 'null') ? roomSummary : '';

    assignedRooms.length = 0;
    renderAssignedRooms();

    const typeSelect = document.getElementById('bookTypeSelect');
    typeSelect.innerHTML = '<option value="">-- Select Type --</option>';
    typeSelect.value = '';

    if (roomSummary && roomSummary !== 'N/A' && roomSummary !== 'Pending Details') {
        const parts = roomSummary.split(',');
        parts.forEach(part => {
            const match = part.trim().match(/^(\d+)x\s+(.+)$/);
            if (match) {
                const count = parseInt(match[1]);
                const category = match[2];
                const opt = document.createElement('option');
                opt.value = category;
                opt.setAttribute('data-pending', count);
                opt.innerText = `${category} (${count} pending)`;
                typeSelect.appendChild(opt);
            }
        });
    } else {
        for (const category in roomInventory) {
            const opt = document.createElement('option');
            opt.value = category;
            opt.setAttribute('data-pending', 1);
            opt.innerText = `${category} (1 pending)`;
            typeSelect.appendChild(opt);
        }
    }

    updateAvailableRooms();
    renderTourBookingsTable(toursDivId);
    document.getElementById('checkinModal').style.display = 'flex';
}

function closeCheckinModal() {
    document.getElementById('checkinModal').style.display = 'none';
}

function cancelCheckinModal() {
    closeCheckinModal();
    document.getElementById('submitBookingId').value = '';
    pendingFaceEnrollments = {};
    const gallery = document.getElementById('faceGallery');
    if (gallery) {
        gallery.innerHTML = '<div id="faceGalleryEmpty" style="font-size: 13px; color: #94a3b8; text-align: center; padding: 20px 0;">Chưa có ảnh nào được chụp</div>';
    }
}


// ==============================================
// SECTION 5: DEPENDENTS (GUESTS) MANAGEMENT
// ==============================================

function addDependent() {
    const name = document.getElementById('depName').value.trim();
    const id = document.getElementById('depId').value.trim();
    const dob = document.getElementById('depDob').value;
    const roomId = document.getElementById('depRoom').value;
    const gender = document.getElementById('depGender').value;
    const isPrimary = document.getElementById('depIsPrimary').checked;

    if (!name || !dob) {
        showToast('Vui lòng nhập họ tên và ngày sinh!', 'error');
        return;
    }
    if (!roomId) {
        showToast('Vui lòng chọn phòng!', 'error');
        return;
    }

    const todayStr = new Date().toISOString().split('T')[0];
    if (dob > todayStr) {
        showToast('Ngày sinh không được vượt quá ngày hiện tại!', 'error');
        return;
    }
    if (dob < '1900-01-01') {
        showToast('Ngày sinh không hợp lệ!', 'error');
        return;
    }

    const age = calculateAge(dob);
    if (age >= 14 && !id) {
        showToast('Người từ 14 tuổi trở lên bắt buộc có CCCD/Passport!', 'error');
        return;
    }

    if (id) {
        const isNumericOnly = /^\d+$/.test(id);
        if (isNumericOnly) {
            if (id.length !== 12) {
                showToast('Số CCCD không hợp lệ! Nếu chỉ nhập số, CCCD phải gồm đúng 12 chữ số.', 'error');
                return;
            }
        } else {
            const isValidPassport = /^[A-Za-z0-9]{6,15}$/.test(id);
            if (!isValidPassport) {
                showToast('Số Passport không hợp lệ! Passport phải từ 6-15 ký tự chữ và số.', 'error');
                return;
            }
        }
    }

    let actualAdultCount = 1;
    let actualChildCount = 0;
    document.querySelectorAll('#dependentsList tr').forEach(row => {
        if (row === currentEditingDependentRow) return;
        const rowDob = row.getAttribute('data-dob');
        if (rowDob) {
            const rowAge = calculateAge(rowDob);
            if (rowAge >= 12) actualAdultCount++;
            else actualChildCount++;
        }
    });

    if (age >= 12) {
        if (actualAdultCount >= maxTotalAdults) {
            showToast(`Số lượng Người Lớn đã đạt sức chứa tối đa (${maxTotalAdults}) của các phòng!`, 'error');
            return;
        }
        if (actualAdultCount >= expectedTotalAdults) {
            showToast(`Khách thêm vượt tiêu chuẩn đơn (${expectedTotalAdults}), hệ thống sẽ tự động tính phụ thu.`, 'warning');
        }
    } else {
        if (actualChildCount >= maxTotalChildren) {
            showToast(`Số lượng Trẻ Em đã đạt sức chứa tối đa (${maxTotalChildren}) của các phòng!`, 'error');
            return;
        }
        if (actualChildCount >= expectedTotalChildren) {
            showToast(`Trẻ em thêm vượt tiêu chuẩn đơn (${expectedTotalChildren}), hệ thống sẽ tự động tính phụ thu.`, 'warning');
        }
    }

    if (isPrimary) {
        if (assignedRooms.length > 0 && roomId === assignedRooms[0].room) {
            showToast(`Phòng đầu tiên (${roomId}) mặc định do Chủ đoàn đứng đầu. Bạn không thể gán chức danh này cho khách phụ thuộc!`, 'error');
            return;
        }

        let primaryExists = false;
        document.querySelectorAll('#dependentsList tr').forEach(row => {
            if (row === currentEditingDependentRow) return;
            const roomInput = row.querySelector('input[name$=".assignedPhysicalRoomNumber"]');
            const primaryInput = row.querySelector('input[name$=".isPrimaryContact"]');
            if (roomInput && primaryInput && roomInput.value === roomId && primaryInput.value === 'true') {
                primaryExists = true;
            }
        });
        if (primaryExists) {
            showToast(`Phòng ${roomId} đã có người đứng đầu. Vui lòng bỏ chọn "Đứng đầu" hoặc chọn phòng khác.`, 'error');
            return;
        }
    }

    if (currentEditingDependentRow) {
        currentEditingDependentRow.remove();
        currentEditingDependentRow = null;
        const addBtn = document.querySelector('button[onclick="addDependent()"]');
        if (addBtn) addBtn.innerHTML = '<i class="fa-solid fa-plus"></i> Add';
    }

    const depId = document.getElementById('depId').getAttribute('data-dependent-id');
    const parsedDepId = (depId && depId !== 'null' && depId !== '') ? depId : null;

    addDependentRow(name, id, dob, gender, parsedDepId, roomId, isPrimary);

    document.getElementById('depName').value = '';
    document.getElementById('depId').value = '';
    document.getElementById('depId').removeAttribute('data-dependent-id');
    document.getElementById('depDob').value = '';
    document.getElementById('depRoom').value = '';
    document.getElementById('depIsPrimary').checked = false;
}

function addDependentRow(name, cccd, dob, gender, dependentId, assignedPhysicalRoomNumber, isPrimary = false) {
    const tbody = document.getElementById('dependentsList');
    if (tbody.children.length === 1 && tbody.children[0].innerText.includes('No group members')) {
        tbody.innerHTML = '';
    }

    const tr = document.createElement('tr');
    tr.setAttribute('data-dob', dob || '');
    tr.style.borderBottom = '1px solid #f1f5f9';
    tr.style.transition = 'background-color 0.2s ease';
    tr.onmouseover = () => tr.style.backgroundColor = '#f8fafc';
    tr.onmouseout = () => tr.style.backgroundColor = 'transparent';

    let hiddenIdInput = dependentId ? `<input type="hidden" name="dependents[${depIndexCounter}].dependentId" value="${dependentId}" />` : '';
    let hiddenRoomInput = assignedPhysicalRoomNumber ? `<input type="hidden" name="dependents[${depIndexCounter}].assignedPhysicalRoomNumber" value="${assignedPhysicalRoomNumber}" />` : '';
    let hiddenPrimaryInput = `<input type="hidden" name="dependents[${depIndexCounter}].isPrimaryContact" value="${isPrimary ? 'true' : 'false'}" />`;

    let effectiveDepId = dependentId || ('NEW_' + depIndexCounter);
    let depIdArg = `'${effectiveDepId}'`;
    let hiddenMapping = `<input type="hidden" class="faceid-mapping-id" data-target-id="${effectiveDepId}" data-index="${depIndexCounter}" />`;

    let roomIdArg = assignedPhysicalRoomNumber ? `'${assignedPhysicalRoomNumber}'` : 'null';
    let roleBadge = isPrimary
        ? `<span style="display:inline-block; margin-top: 4px; padding: 2px 8px; background: #fef3c7; color: #d97706; border-radius: 12px; font-size: 11px; font-weight: 700; letter-spacing: 0.5px; text-transform: uppercase;"><i class="fa-solid fa-star"></i> Đứng đầu</span>`
        : `<span style="font-size: 13px; color: #64748b; font-weight: 500;">Thành viên</span>`;
    let roomDisplay = assignedPhysicalRoomNumber
        ? `<span style="font-weight: 600; color: #0f766e; font-size: 13px;">Phòng ${assignedPhysicalRoomNumber}</span><br>`
        : '';
    let finalRoleDisplay = `${roomDisplay}${roleBadge}`;

    tr.innerHTML = `
        <td style="padding: 12px 16px; font-size: 14px; font-weight: 600; color: #1e293b;">
            ${name}
            ${hiddenIdInput}
            ${hiddenRoomInput}
            ${hiddenPrimaryInput}
            ${hiddenMapping}
            <input type="hidden" name="dependents[${depIndexCounter}].fullName" value="${name}" />
        </td>
        <td style="padding: 12px 16px; font-size: 14px; color: #475569; font-weight: 500;">
            ${cccd}
            <input type="hidden" name="dependents[${depIndexCounter}].cccd" value="${cccd}" />
        </td>
        <td style="padding: 12px 16px; font-size: 14px;">
            ${finalRoleDisplay}
        </td>
        <td style="padding: 12px 16px; font-size: 14px; color: #475569;">
            ${dob}
            <input type="hidden" name="dependents[${depIndexCounter}].dateOfBirth" value="${dob}" />
            <input type="hidden" name="dependents[${depIndexCounter}].gender" value="${gender}" />
        </td>
        <td style="padding: 12px 16px;">
            <button type="button" class="btn btn-outline btn-sm" style="color: #6366f1; border-color: #c7d2fe; background: #eef2ff; padding: 6px 10px; margin-right: 6px; border-radius: 6px; transition: all 0.2s;" onmouseover="this.style.background='#e0e7ff'" onmouseout="this.style.background='#eef2ff'" title="FaceID" onclick="openEnrollModal('DEPENDENT', ${depIdArg}, '${name}')"><i class="fa-solid fa-camera"></i></button>
            <button type="button" class="btn btn-outline btn-sm" style="color: #3b82f6; border-color: #bfdbfe; background: #eff6ff; padding: 6px 10px; margin-right: 6px; border-radius: 6px; transition: all 0.2s;" onmouseover="this.style.background='#dbeafe'" onmouseout="this.style.background='#eff6ff'" title="Edit" onclick="editDependentRow(this, '${name}', '${cccd}', '${dob}', '${gender}', ${depIdArg}, ${roomIdArg}, ${isPrimary})"><i class="fa-solid fa-pen"></i></button>
            <button type="button" class="btn btn-outline btn-sm" style="color: #ef4444; border-color: #fecaca; background: #fef2f2; padding: 6px 10px; border-radius: 6px; transition: all 0.2s;" onmouseover="this.style.background='#fee2e2'" onmouseout="this.style.background='#fef2f2'" title="Delete" onclick="removeDependentRow(this, ${depIdArg})"><i class="fa-solid fa-trash"></i></button>
        </td>
    `;
    tbody.appendChild(tr);
    depIndexCounter++;
}

function editDependentRow(btn, name, cccd, dob, gender, depId, roomId, isPrimary) {
    if (currentEditingDependentRow) {
        currentEditingDependentRow.style.display = '';
    }
    const tr = btn.closest('tr');
    tr.style.display = 'none';
    currentEditingDependentRow = tr;

    document.getElementById('depName').value = name === '(Chưa cập nhật)' ? '' : name;
    document.getElementById('depId').value = cccd === 'null' ? '' : cccd;
    document.getElementById('depDob').value = dob === 'null' ? '' : dob;
    if (gender !== 'null') {
        document.getElementById('depGender').value = gender;
    }
    if (roomId && roomId !== 'null') {
        document.getElementById('depRoom').value = roomId;
    } else {
        document.getElementById('depRoom').value = '';
    }
    document.getElementById('depIsPrimary').checked = isPrimary;

    if (depId && depId !== 'null' && !depId.startsWith('NEW_')) {
        document.getElementById('depId').setAttribute('data-dependent-id', depId);
    } else {
        document.getElementById('depId').removeAttribute('data-dependent-id');
    }

    const wrapper = document.getElementById('dependentsTableWrapper');
    if (wrapper.style.display === 'none') {
        toggleDependentsList();
    }

    document.getElementById('depName').scrollIntoView({ behavior: 'smooth', block: 'center' });

    const addBtn = document.querySelector('button[onclick="addDependent()"]');
    if (addBtn) addBtn.innerHTML = '<i class="fa-solid fa-check"></i> Cập nhật';
}

function removeDependentRow(btn, depId) {
    if (!depId || String(depId).startsWith('NEW_')) {
        btn.closest('tr').remove();
        return;
    }

    if (confirm('Bạn có chắc chắn muốn xóa khách đi kèm này? Hành động này không thể hoàn tác và sẽ xóa trực tiếp trên hệ thống.')) {
        csrfFetch('/receptionist/api/room-guests/by-dependent/' + depId, 'DELETE')
            .then(data => {
                if (data.status === 'success') {
                    showToast(data.message, 'success');
                    btn.closest('tr').remove();
                } else {
                    showToast(data.message, 'error');
                }
            })
            .catch(err => {
                console.error(err);
                showToast('Lỗi kết nối khi xóa khách đi kèm.', 'error');
            });
    }
}

function toggleDependentsList() {
    const wrapper = document.getElementById('dependentsTableWrapper');
    const btn = document.getElementById('toggleDependentsBtn');
    if (wrapper.style.display === 'none') {
        wrapper.style.display = 'block';
        btn.innerHTML = '<i class="fa-solid fa-chevron-up"></i> Thu gọn';
    } else {
        wrapper.style.display = 'none';
        btn.innerHTML = '<i class="fa-solid fa-chevron-down"></i> Hiện hết';
    }
}


// ==============================================
// SECTION 6: TOUR MANAGEMENT
// ==============================================

function renderTourBookingsTable(toursDivId) {
    const section = document.getElementById('tourBookingsSection');
    const tbody = document.getElementById('tourBookingsList');
    if (!section || !tbody) return;

    tbody.innerHTML = '';
    currentTours = [];

    const toursDiv = toursDivId ? document.getElementById(toursDivId) : null;
    if (!toursDiv) { section.style.display = 'none'; return; }

    const items = toursDiv.querySelectorAll('li');
    if (items.length === 0) { section.style.display = 'none'; return; }

    section.style.display = 'block';

    items.forEach((li, index) => {
        const id = li.getAttribute('data-id');
        const tourName = li.getAttribute('data-tour');
        const category = li.getAttribute('data-category') || 'Không rõ';
        const participant = li.getAttribute('data-participant') || '0';

        currentTours.push({ id, tourName, category, participant });

        const tr = document.createElement('tr');
        tr.style.borderBottom = '1px solid #f1f5f9';
        tr.style.transition = 'background-color 0.2s ease';
        tr.onmouseover = () => tr.style.backgroundColor = '#f8fafc';
        tr.onmouseout = () => tr.style.backgroundColor = 'transparent';

        tr.innerHTML =
            '<td style="padding:12px 16px;font-size:14px;font-weight:600;color:#1e293b">' + tourName + '</td>' +
            '<td style="padding:12px 16px;font-size:14px;color:#475569">' + category + '</td>' +
            '<td style="padding:12px 16px;text-align:center;font-size:14px;color:#475569">' + participant + '</td>' +
            '<td style="padding:12px 16px;font-size:14px;font-weight:600;color:#64748b" id="tour-room-span-' + index + '">(Chưa gán)</td>';
        tbody.appendChild(tr);
    });

    updateTourPhysicalRooms();
}

function updateTourPhysicalRooms() {
    const availableRooms = JSON.parse(JSON.stringify(assignedRooms));

    currentTours.forEach((tour, index) => {
        const roomSpan = document.getElementById('tour-room-span-' + index);
        if (!roomSpan) return;

        const roomIndex = availableRooms.findIndex(r => r.type === tour.category);
        if (roomIndex !== -1) {
            const assignedRoom = availableRooms[roomIndex];
            roomSpan.innerText = 'Phòng ' + assignedRoom.room;
            roomSpan.style.color = '#0f766e';
            availableRooms.splice(roomIndex, 1);
        } else {
            roomSpan.innerText = '(Chưa gán)';
            roomSpan.style.color = '#64748b';
        }
    });
}


// ==============================================
// SECTION 7: BIOMETRICS — FACE ID ENROLLMENT
// ==============================================

async function loadFaceApiModels() {
    if (faceApiLoaded) return true;
    try {
        await Promise.all([
            faceapi.nets.ssdMobilenetv1.loadFromUri(MODEL_URL),
            faceapi.nets.faceLandmark68Net.loadFromUri(MODEL_URL),
            faceapi.nets.faceRecognitionNet.loadFromUri(MODEL_URL)
        ]);
        faceApiLoaded = true;
        return true;
    } catch (error) {
        console.error("Lỗi khi tải FaceAPI models:", error);
        showToast("Không thể tải AI Models. Vui lòng kiểm tra kết nối mạng.");
        return false;
    }
}

async function openEnrollModal(type, targetId, targetName) {
    if (!targetId || targetId === "undefined" || targetId === "") {
        showToast("Không xác định được ID Khách hàng! Hãy kiểm tra lại.");
        return;
    }

    currentEnrollType = type;
    currentEnrollId = targetId;

    const modal = document.getElementById('enrollFaceModal');
    const overlay = document.getElementById('enrollOverlay');
    const captureBtn = document.getElementById('captureBtn');

    document.getElementById('enrollTargetName').innerText = type === 'CUSTOMER'
        ? "Đang đăng ký cho Người Đặt Phòng..."
        : "Đang đăng ký cho " + (targetName || "Người Đi Kèm...");

    modal.style.display = 'flex';
    overlay.style.display = 'flex';
    overlay.innerText = 'Đang tải AI Model...';
    captureBtn.disabled = true;

    const loaded = await loadFaceApiModels();
    if (!loaded) {
        closeEnrollModal();
        return;
    }

    try {
        videoStream = await navigator.mediaDevices.getUserMedia({ video: {} });
        const video = document.getElementById('enrollVideo');
        video.srcObject = videoStream;

        video.onloadedmetadata = () => {
            overlay.style.display = 'none';
            captureBtn.disabled = false;
        };
    } catch (err) {
        console.error("Không có quyền truy cập camera: ", err);
        overlay.innerText = 'Lỗi truy cập Camera';
        showToast("Vui lòng cấp quyền truy cập Camera cho trình duyệt.");
    }
}

function closeEnrollModal() {
    if (videoStream) {
        videoStream.getTracks().forEach(track => track.stop());
        videoStream = null;
    }
    document.getElementById('enrollFaceModal').style.display = 'none';
}

async function captureFace() {
    const video = document.getElementById('enrollVideo');
    const overlay = document.getElementById('enrollOverlay');
    const captureBtn = document.getElementById('captureBtn');

    captureBtn.disabled = true;
    overlay.style.display = 'flex';

    for (let i = 3; i > 0; i--) {
        overlay.innerHTML = `<span style="font-size: 24px; font-weight: bold;">Chụp trong: ${i}s<br><span style="font-size: 14px; font-weight: normal; color: #cbd5e1;">(Vui lòng mở mắt to và nhìn thẳng)</span></span>`;
        await new Promise(r => setTimeout(r, 1000));
    }

    overlay.innerText = 'Đang trích xuất khuôn mặt...';

    try {
        const detection = await faceapi.detectSingleFace(video).withFaceLandmarks().withFaceDescriptor();

        if (!detection) {
            showToast("Không tìm thấy khuôn mặt rõ ràng. Vui lòng nhìn thẳng vào camera và thử lại.");
            overlay.style.display = 'none';
            captureBtn.disabled = false;
            return;
        }

        const descriptor = Array.from(detection.descriptor);

        // Kiểm tra trùng lặp với các khuôn mặt đã chụp trong cùng session
        const currentKey = currentEnrollType === 'CUSTOMER' ? 'CUSTOMER' : currentEnrollId;
        for (const existingKey in pendingFaceEnrollments) {
            if (existingKey === currentKey.toString()) continue;

            const existingVector = JSON.parse(pendingFaceEnrollments[existingKey].vector);
            const distance = faceapi.euclideanDistance(descriptor, existingVector);

            // Threshold = 0.5 for faceapi
            // TẠM TẮT ĐỂ DEMO 1 NGƯỜI QUÉT NHIỀU MẶT:
            /*
            if (distance < 0.5) {
                showToast("Khuôn mặt này đã được quét cho một người khác trong đoàn! Vui lòng quét khuôn mặt khác.");
                overlay.style.display = 'none';
                captureBtn.disabled = false;
                return;
            }
            */
        }

        // Capture face image
        const canvas = document.createElement('canvas');
        canvas.width = video.videoWidth;
        canvas.height = video.videoHeight;
        const ctx = canvas.getContext('2d');
        ctx.drawImage(video, 0, 0, canvas.width, canvas.height);

        const box = detection.detection.box;
        const faceCanvas = document.createElement('canvas');

        const padX = box.width * 0.2;
        const padY = box.height * 0.2;
        const startX = Math.max(0, box.x - padX);
        const startY = Math.max(0, box.y - padY);
        const drawWidth = Math.min(canvas.width - startX, box.width + padX * 2);
        const drawHeight = Math.min(canvas.height - startY, box.height + padY * 2);

        faceCanvas.width = drawWidth;
        faceCanvas.height = drawHeight;
        faceCanvas.getContext('2d').drawImage(canvas, startX, startY, drawWidth, drawHeight, 0, 0, drawWidth, drawHeight);

        const base64Image = faceCanvas.toDataURL('image/jpeg', 0.85);

        const key = currentEnrollType === 'CUSTOMER' ? 'CUSTOMER' : currentEnrollId;
        pendingFaceEnrollments[key] = {
            type: currentEnrollType,
            id: currentEnrollId,
            vector: JSON.stringify(descriptor),
            image: base64Image
        };

        updateFacePreviewUI(currentEnrollType, currentEnrollId, base64Image);
        closeEnrollModal();

    } catch (e) {
        console.error("Lỗi quét:", e);
        showToast("Đã xảy ra lỗi khi quét khuôn mặt.");
        overlay.style.display = 'none';
        captureBtn.disabled = false;
    }
}

function updateFacePreviewUI(type, targetId, base64Image) {
    const gallery = document.getElementById('faceGallery');
    const emptyMsg = document.getElementById('faceGalleryEmpty');
    if (emptyMsg) emptyMsg.style.display = 'none';

    let guestName = "Khách hàng";
    if (type === 'CUSTOMER') {
        guestName = document.getElementById('modalGuestName').value || "Người đặt phòng";
    } else {
        const btn = document.querySelector(`button[onclick*="openEnrollModal('DEPENDENT', '${targetId}')"]`) || document.querySelector(`button[onclick*="openEnrollModal('DEPENDENT', ${targetId})"]`);
        if (btn) {
            const tr = btn.closest('tr');
            if (tr) {
                const nameInput = tr.querySelector(`input[name$=".fullName"]`);
                if (nameInput) guestName = nameInput.value;
            }
        }
    }

    const keyId = type === 'CUSTOMER' ? 'CUSTOMER' : targetId;
    let card = document.getElementById('face-card-' + keyId);

    if (!card) {
        card = document.createElement('div');
        card.id = 'face-card-' + keyId;
        card.style.cssText = 'display: flex; flex-direction: column; align-items: center; padding: 10px; border: 1px solid #cbd5e1; border-radius: 6px; background: #f8fafc; gap: 8px; position: relative;';
        gallery.appendChild(card);
    }

    card.innerHTML = `
        <img src="${base64Image}" style="width: 80px; height: 80px; border-radius: 50%; object-fit: cover; border: 3px solid #10b981; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
        <span style="font-size: 13px; font-weight: 600; color: #334155; text-align: center; width: 100%; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">${guestName}</span>
        <i class="fa-solid fa-circle-check" style="color: #10b981; position: absolute; top: 10px; right: 10px; font-size: 16px;"></i>
    `;
}


// ==============================================
// SECTION 8: BIOMETRICS — QR CODE & REMOTE SCAN
// ==============================================

function handleQrScan(val, target, inputEl) {
    if (!val) return;

    // Format: 001205015836||Nguyễn Xuân Lưu|14102005|Nam|Địa chỉ|31052021
    const parts = val.split('|');
    if (parts.length >= 7) {
        const id = parts[0];
        const name = parts[2];
        const dobStr = parts[3]; // DDMMYYYY

        let dob = '';
        if (dobStr && dobStr.length === 8) {
            dob = `${dobStr.substring(4, 8)}-${dobStr.substring(2, 4)}-${dobStr.substring(0, 2)}`;
        }

        const mainCccd = document.getElementById('modalGuestCccd').value.trim();
        let isDuplicateDep = false;
        document.querySelectorAll('input[name$=".cccd"]').forEach(inp => {
            if (inp.value === id) isDuplicateDep = true;
        });

        if (mainCccd === id && target !== 'main') {
            showToast(`Thẻ CCCD của ${name} đã được quét cho trưởng đoàn!`, 'warning');
            if (inputEl) inputEl.value = '';
            return;
        }

        if (isDuplicateDep) {
            showToast(`Thành viên ${name} đã có trong danh sách!`, 'warning');
            if (inputEl) inputEl.value = '';
            return;
        }

        if (target === 'main') {
            document.getElementById('modalGuestName').value = name;
            document.getElementById('modalGuestCccd').value = id;
            showToast('Đã tự động điền thông tin chủ đoàn từ QR!', 'success');
        } else if (target === 'dep') {
            document.getElementById('depName').value = name;
            document.getElementById('depId').value = id;
            if (dob) document.getElementById('depDob').value = dob;
            showToast('Đã tự động điền thông tin thành viên từ QR!', 'success');
        } else if (target === 'auto-dep') {
            let actualAdultCount = 1;
            let actualChildCount = 0;

            document.querySelectorAll('#dependentsList tr').forEach(tr => {
                if (tr.querySelector('input')) {
                    const trDob = tr.getAttribute('data-dob');
                    const trAge = calculateAge(trDob);
                    if (trAge >= 12) actualAdultCount++;
                    else actualChildCount++;
                }
            });

            const newAge = calculateAge(dob);
            if (newAge >= 12) {
                if (actualAdultCount >= maxTotalAdults) {
                    showToast(`Số lượng Người Lớn đã đạt sức chứa tối đa (${maxTotalAdults}) của các phòng!`, 'error');
                    if (inputEl) inputEl.value = '';
                    return;
                }
                if (actualAdultCount >= expectedTotalAdults) {
                    showToast(`Khách thêm vượt tiêu chuẩn đơn (${expectedTotalAdults}), hệ thống sẽ tự động tính phụ thu.`, 'warning');
                }
            } else {
                if (actualChildCount >= maxTotalChildren) {
                    showToast(`Số lượng Trẻ Em đã đạt sức chứa tối đa (${maxTotalChildren}) của các phòng!`, 'error');
                    if (inputEl) inputEl.value = '';
                    return;
                }
                if (actualChildCount >= expectedTotalChildren) {
                    showToast(`Trẻ em thêm vượt tiêu chuẩn đơn (${expectedTotalChildren}), hệ thống sẽ tự động tính phụ thu.`, 'warning');
                }
            }

            addDependentRow(name, id, dob, 'Nam', null, '');
            showToast(`Đã tự động thêm thành viên: ${name}`, 'success');
        }

        if (inputEl) inputEl.value = '';
    }
}

function openQrScannerModal(target) {
    currentQrTarget = target;
    document.getElementById('qrScannerModal').style.display = 'flex';

    if (!html5QrcodeScanner) {
        html5QrcodeScanner = new Html5QrcodeScanner(
            "qr-reader",
            {
                fps: 20,
                qrbox: { width: 300, height: 300 },
                formatsToSupport: [Html5QrcodeSupportedFormats.QR_CODE],
                useBarCodeDetectorIfSupported: true,
                videoConstraints: {
                    width: { ideal: 1920 },
                    height: { ideal: 1080 }
                }
            },
            /* verbose= */ false
        );
    }

    html5QrcodeScanner.render(onScanSuccess, onScanFailure);
}

function closeQrScannerModal() {
    document.getElementById('qrScannerModal').style.display = 'none';
    if (html5QrcodeScanner) {
        html5QrcodeScanner.clear().catch(error => {
            console.error('Failed to clear html5QrcodeScanner. ', error);
        });
    }
    currentQrTarget = null;
}

function onScanSuccess(decodedText, decodedResult) {
    console.log("Scan result: " + decodedText);
    closeQrScannerModal();
    let mockInput = { value: decodedText, tagName: 'MOCK' };
    handleQrScan(decodedText, currentQrTarget, mockInput);
}

function onScanFailure(error) {
    // Intentionally left empty — scan failures are expected and not errors
}

async function scanQrFromFile(input) {
    if (!input.files || input.files.length === 0) return;
    const file = input.files[0];

    if (html5QrcodeScanner) {
        try { await html5QrcodeScanner.clear(); } catch (e) { }
    }

    const html5QrCode = new Html5Qrcode("qr-reader");
    try {
        const decodedText = await html5QrCode.scanFile(file, true);
        console.log("Scan result from file: " + decodedText);

        html5QrCode.clear().catch(e => { });
        closeQrScannerModal();

        let mockInput = { value: decodedText, tagName: 'MOCK' };
        handleQrScan(decodedText, currentQrTarget, mockInput);
    } catch (err) {
        console.error(err);
        showToast('Không tìm thấy mã QR hợp lệ trong ảnh!', 'error');
        html5QrCode.clear().catch(e => { });
    }
    input.value = '';
}

// Tự động kết nối SSE ngay từ đầu và duy trì mãi mãi
function initPersistentSse() {
    remoteScanEventSource = new EventSource('/api/v1/remote-scan/' + persistentSessionId + '/subscribe');

    remoteScanEventSource.addEventListener('SCAN_RESULT', function (event) {
        console.log('Received from remote: ' + event.data);

        let target = currentQrTarget;

        if (!target) {
            const mainNameInput = document.getElementById('modalGuestName');
            const mainCccdInput = document.getElementById('modalGuestCccd');
            if (!mainNameInput.value.trim() && !mainCccdInput.value.trim()) {
                target = 'main';
            } else {
                target = 'auto-dep';
            }
        }

        let mockInput = { value: event.data, tagName: 'MOCK' };
        handleQrScan(event.data, target, mockInput);

        if (currentQrTarget) {
            closeRemoteScanModal();
        }
    });

    remoteScanEventSource.onerror = function () {
        console.log('SSE Connection lost, reconnecting...');
        remoteScanEventSource.close();
        setTimeout(initPersistentSse, 2000);
    };
}

function openRemoteScanModal(target) {
    currentQrTarget = target;
    document.getElementById('remoteScanModal').style.display = 'flex';

    fetch('/api/v1/remote-scan/host-ip')
        .then(res => res.json())
        .then(data => {
            let host = window.location.host;
            let protocol = window.location.protocol;

            if (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1') {
                host = data.ip + (window.location.port ? ':' + window.location.port : '');
                protocol = 'http:';
            }

            const scanUrl = protocol + '//' + host + '/receptionist/remote-scan?session=' + persistentSessionId;

            const qrContainer = document.getElementById('remote-qrcode-container');
            if (!remoteScanQrCode) {
                qrContainer.innerHTML = '';
                remoteScanQrCode = new QRCode(qrContainer, {
                    text: scanUrl,
                    width: 200,
                    height: 200,
                    colorDark: '#0f172a',
                    colorLight: '#ffffff',
                    correctLevel: QRCode.CorrectLevel.H
                });
            } else {
                remoteScanQrCode.clear();
                remoteScanQrCode.makeCode(scanUrl);
            }
        })
        .catch(err => console.error("Could not fetch host IP", err));
}

function closeRemoteScanModal() {
    document.getElementById('remoteScanModal').style.display = 'none';
    currentQrTarget = null;
}


// ==============================================
// SECTION 9: FORM VALIDATION & SUBMIT
// ==============================================

const _checkinForm = document.getElementById('checkinFormWrapper');
if (_checkinForm) {
    _checkinForm.addEventListener('submit', async function (e) {

        let actualAdultCount = 1;
        let actualChildCount = 0;

        document.querySelectorAll('#dependentsList tr').forEach(tr => {
            if (tr.querySelector('input')) {
                const trDob = tr.getAttribute('data-dob');
                const trAge = calculateAge(trDob);
                if (trAge >= 12) actualAdultCount++;
                else actualChildCount++;
            }
        });

        if (actualAdultCount > maxTotalAdults) {
            e.preventDefault();
            showToast(`Không thể hoàn tất! Tổng số Người Lớn (${actualAdultCount}) vượt quá sức chứa tối đa (${maxTotalAdults}). Vui lòng xóa bớt hoặc điều chỉnh hạng phòng.`, 'error');
            return;
        }
        if (actualChildCount > maxTotalChildren) {
            e.preventDefault();
            showToast(`Không thể hoàn tất! Tổng số Trẻ Em (${actualChildCount}) vượt quá sức chứa tối đa (${maxTotalChildren}). Vui lòng xóa bớt hoặc điều chỉnh hạng phòng.`, 'error');
            return;
        }

        if (assignedRooms.length === 0) {
            e.preventDefault();
            showToast('Vui long phan it nhat 1 phong truoc khi hoan tat Check-in!');
            return;
        }

        const mainPhone = document.getElementById('modalGuestPhone').value;
        const mainCccd = document.getElementById('modalGuestCccd').value;
        if (!mainPhone || !mainCccd) {
            e.preventDefault();
            showToast('Khách đứng đầu (chủ đoàn) phải điền đầy đủ số điện thoại và CCCD!');
            return;
        }

        if (!pendingFaceEnrollments['CUSTOMER']) {
            e.preventDefault();
            showToast('Người chủ đoàn bắt buộc phải cập nhật khuôn mặt (FaceID) để hoàn tất đơn!');
            return;
        }

        let missingFaceName = null;
        document.querySelectorAll('.faceid-mapping-id').forEach(input => {
            const row = input.closest('tr');
            if (row && row.style.display !== 'none') {
                const targetId = input.getAttribute('data-target-id');
                const dobInput = row.querySelector('input[name$=".dateOfBirth"]');
                const nameInput = row.querySelector('input[name$=".fullName"]');
                if (dobInput) {
                    const age = calculateAge(dobInput.value);
                    if (age >= 14) {
                        if (targetId && targetId.startsWith('NEW_') && !pendingFaceEnrollments[targetId]) {
                            missingFaceName = nameInput ? nameInput.value : 'Người đi kèm';
                        }
                    }
                }
            }
        });

        if (missingFaceName) {
            e.preventDefault();
            showToast(`Thành viên ${missingFaceName} từ 14 tuổi trở lên bắt buộc phải cập nhật khuôn mặt (FaceID mới) để hoàn tất đơn!`);
            return;
        }

        const assignedRoomNumbers = assignedRooms.map(r => r.room);

        const primaryInputs = document.querySelectorAll('input[name$=".isPrimaryContact"][value="true"]');
        const primaryRooms = [];
        primaryInputs.forEach(input => {
            const row = input.closest('tr');
            if (row && !row.classList.contains('editing-row')) {
                const roomInput = row.querySelector('input[name$=".assignedPhysicalRoomNumber"]');
                if (roomInput && roomInput.value) {
                    primaryRooms.push(roomInput.value);
                }
            }
        });

        const missingRooms = [];
        for (let i = 1; i < assignedRoomNumbers.length; i++) {
            const room = assignedRoomNumbers[i];
            if (!primaryRooms.includes(room)) {
                missingRooms.push(room);
            }
        }

        if (missingRooms.length > 0) {
            e.preventDefault();
            showToast('Thieu nguoi dung dau cho phong: ' + missingRooms.join(', '));
            return;
        }

        const dependentRows = document.querySelectorAll('#dependentsList tr:not(.editing-row)');
        let unassignedDependentCount = 0;
        let missingDependentName = null;
        dependentRows.forEach(row => {
            if (row.style.display !== 'none') {
                const roomInput = row.querySelector('input[name$=".assignedPhysicalRoomNumber"]');
                if (roomInput !== null && (!roomInput.value || roomInput.value.trim() === '')) {
                    unassignedDependentCount++;
                    if (!missingDependentName) {
                        const nameTd = row.querySelector('td:first-child');
                        if (nameTd) missingDependentName = nameTd.innerText.trim().split('\n')[0];
                    }
                }
            }
        });

        if (unassignedDependentCount > 0) {
            e.preventDefault();
            showToast(`Vui lòng chọn phòng cho khách "${missingDependentName || 'ẩn danh'}" và các khách chưa được phân phòng khác!`, 'error');
            return;
        }

        // Prevent default submission to process FaceIDs first
        e.preventDefault();

        const submitBtn = _checkinForm.querySelector('button[type="submit"]');
        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Đang xử lý FaceID...';
        }

        // Inject pending FaceIDs into the form
        for (const key in pendingFaceEnrollments) {
            const data = pendingFaceEnrollments[key];
            if (data.type === 'CUSTOMER') {
                const input1 = document.createElement('input');
                input1.type = 'hidden';
                input1.name = 'faceVectorData';
                input1.value = data.vector;
                _checkinForm.appendChild(input1);

                const input2 = document.createElement('input');
                input2.type = 'hidden';
                input2.name = 'faceImageBase64';
                input2.value = data.image;
                _checkinForm.appendChild(input2);
            } else {
                const mapping = _checkinForm.querySelector(`.faceid-mapping-id[data-target-id="${data.id}"]`);
                if (mapping) {
                    const idx = mapping.getAttribute('data-index');
                    const input1 = document.createElement('input');
                    input1.type = 'hidden';
                    input1.name = `dependents[${idx}].faceVectorData`;
                    input1.value = data.vector;
                    _checkinForm.appendChild(input1);

                    const input2 = document.createElement('input');
                    input2.type = 'hidden';
                    input2.name = `dependents[${idx}].faceImageBase64`;
                    input2.value = data.image;
                    _checkinForm.appendChild(input2);
                }
            }
        }

        _checkinForm.submit();
    });
} else {
    console.error('[checkin.js] CRITICAL: #checkinFormWrapper khong tim thay trong DOM!');
}


// ==============================================
// SECTION 10: AUTO-SAVE MODULE
// ==============================================

document.addEventListener('DOMContentLoaded', () => {
    const pageKey = 'kawai_autosave_' + window.location.pathname.replace(/[^a-zA-Z0-9]/g, '_');

    // Khôi phục dữ liệu
    const savedDataStr = localStorage.getItem(pageKey);
    if (savedDataStr) {
        try {
            const savedData = JSON.parse(savedDataStr);
            document.querySelectorAll('input, select, textarea').forEach(el => {
                const key = el.id || el.name;
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

    // Lắng nghe sự kiện để lưu dữ liệu (dùng event delegation)
    document.body.addEventListener('input', (e) => {
        const el = e.target;
        if (el.tagName === 'INPUT' || el.tagName === 'SELECT' || el.tagName === 'TEXTAREA') {
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
        }
    });

    // Xóa dữ liệu khi submit thành công bằng form truyền thống
    document.querySelectorAll('form').forEach(form => {
        form.addEventListener('submit', () => {
            localStorage.removeItem(pageKey);
        });
    });

    // Gắn đè hàm fetch để xóa dữ liệu khi fetch api checkin thành công
    const originalFetch = window.fetch;
    window.fetch = async function () {
        const response = await originalFetch.apply(this, arguments);
        const url = arguments[0];
        if (response.ok && typeof url === 'string' && (url.includes('/walkin/checkin') || url.includes('/checkin/complete'))) {
            localStorage.removeItem(pageKey);
        }
        return response;
    };
});
initPersistentSse();
