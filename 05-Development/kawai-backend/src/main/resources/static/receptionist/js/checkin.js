

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
    
    // trigger animation
    requestAnimationFrame(() => {
        setTimeout(() => toast.classList.add('show'), 10);
    });
    
    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}


function updateAvailableRooms() {
    const typeSelect = document.getElementById('bookTypeSelect');
    const roomSelect = document.getElementById('physicalRoomSelect');
    roomSelect.innerHTML = '<option value="">-- Select Room --</option>';

    const selectedType = typeSelect.value;
    if (!selectedType) return;

    const inventoryForType = roomInventory[selectedType] || [];
    const available = inventoryForType.filter(r => !assignedRooms.some(a => a.room === r));

    if (available.length === 0) {
        return; // Giữ lại dropdown mặc định "-- Select Room --" mà không hiện lỗi
    }

    available.forEach(r => {
        const opt = document.createElement('option');
        opt.value = r;
        opt.innerText = r;
        roomSelect.appendChild(opt);
    });
}

function assignRoom() {
    const typeSelect = document.getElementById('bookTypeSelect');
    const roomSelect = document.getElementById('physicalRoomSelect');
    const selectedType = typeSelect.value;
    const selectedRoom = roomSelect.value;

    if (!selectedType || !selectedRoom) return;

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

let checkinMasterCreditLimit = 5000000;

function handleCheckinCreditInput(input, index) {
    let val = parseFloat(input.value) || 0;
    assignedRooms[index].allocatedCreditLimit = val;
    let hiddens = document.querySelectorAll('input[name="allocatedCreditLimits"]');
    if (hiddens && hiddens[index]) {
        hiddens[index].value = val;
    }
    updateCheckinCreditLimitDisplay();
    // Cảnh báo real-time nếu tổng hạn mức vượt quá
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
        if (remaining < 0) {
            displayEl.style.color = 'red';
        } else {
            displayEl.style.color = '#16a34a';
        }
    }
}

function removeAssignedRoom(roomNumber) {
    const index = assignedRooms.findIndex(a => a.room === roomNumber);
    if (index > -1) {
        const type = assignedRooms[index].type;
        assignedRooms.splice(index, 1);

        // restore pending count
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
        limitInput.value = a.allocatedCreditLimit || 0;
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

    // Cập nhật dropdown phòng trong phân bổ tour (PER_TOUR)
    renderPerTourAllocationRows();

    updateCheckinCreditLimitDisplay();
}


function openCheckinModal(bookingId, guestName, phone, cccd, roomSummary, depsDivId, toursDivId, creditLimit) {
    console.log('[openCheckinModal] called:', { bookingId, guestName, phone, cccd, roomSummary, depsDivId, toursDivId, creditLimit });
    const modalEl = document.getElementById('checkinModal');
    if (!modalEl) {
        console.error('[openCheckinModal] CRITICAL: #checkinModal không tìm thấy trong DOM!');
        return;
    }

    const currentBookingId = document.getElementById('submitBookingId').value;
    if (currentBookingId === bookingId.toString()) {
        // Resume from previous state if clicking the same check-in button
        modalEl.style.display = 'flex';
        return;
    }

    checkinMasterCreditLimit = creditLimit ? parseFloat(creditLimit) : 5000000;
    updateCheckinCreditLimitDisplay();
    // Gán bookingId vào form submit hidden input
    document.getElementById('submitBookingId').value = bookingId;
    const dependentsListContainer = document.getElementById('dependentsList');
    dependentsListContainer.innerHTML = '';
    depIndexCounter = 0; // Reset index khách kèm
    // Lấy danh sách khách kèm hiện có
    if (depsDivId) {
        const depsDiv = document.getElementById(depsDivId);
        if (depsDiv) {
            const items = depsDiv.querySelectorAll('li');
            items.forEach(li => {
                const name = li.getAttribute('data-name');
                const dob = li.getAttribute('data-dob');
                const dependentId = li.getAttribute('data-id');
                // Hiển thị tất cả các khách, bao gồm cả những khách chưa có tên (Stub từ Đặt phòng online)
                const displayName = name ? name : '(Chưa cập nhật)';
                addDependentRow(displayName, '', dob || '', dependentId);
            });
        }
    }

    if (dependentsListContainer.children.length === 0) {
        dependentsListContainer.innerHTML = '<tr><td colspan="4" style="text-align:center; padding: 16px; color: #94a3b8; font-size: 13px;">No group members added yet</td></tr>';
    }

    document.getElementById('modalGuestName').value = guestName || '';
    document.getElementById('modalGuestPhone').value = phone || '';
    document.getElementById('modalGuestCccd').value = cccd || '';
    document.getElementById('modalRoom').innerText = roomSummary || '';

    // reset assignment state
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
        // Fallback if no specific booked types are found
        for (const category in roomInventory) {
            const opt = document.createElement('option');
            opt.value = category;
            opt.setAttribute('data-pending', 1);
            opt.innerText = `${category} (1 pending)`;
            typeSelect.appendChild(opt);
        }
    }
    updateAvailableRooms();

    // Render danh sách tour chưa phân bổ
    renderExistingTourBookings(toursDivId);

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

function calculateAge(dobStr) {
    if (!dobStr) return 0;
    const dob = new Date(dobStr);
    const diff = Date.now() - dob.getTime();
    return Math.floor(diff / (1000 * 60 * 60 * 24 * 365.25));
}

let depIndexCounter = 0;

function addDependent() {
    const name = document.getElementById('depName').value;
    const id = document.getElementById('depId').value;
    const dob = document.getElementById('depDob').value;
    const roomId = document.getElementById('depRoom').value;
    const isPrimary = document.getElementById('depIsPrimary').checked;
    if (!name || !dob) {
        showToast('Please fill Name and Date of Birth!');
        return;
    }
    const age = calculateAge(dob);
    if (age >= 14 && !id) {
        showToast('Người đi kèm từ 14 tuổi trở lên bắt buộc phải cung cấp CCCD/Passport!');
        return;
    }
    
    if (!roomId) {
        showToast('Please assign the guest to a room!');
        return;
    }

    if (isPrimary) {
        if (assignedRooms.length > 0 && roomId === assignedRooms[0].room) {
            showToast(`Phòng ${roomId} đã được chỉ định cho khách chính đứng đầu! Vui lòng không chọn người đi kèm làm người đứng đầu cho phòng này.`);
            return;
        }

        const primaryInputs = document.querySelectorAll(`input[name$=".isPrimaryContact"][value="true"]`);
        let conflict = false;
        primaryInputs.forEach(input => {
            const row = input.closest('tr');
            if (row && row.style.display !== 'none') { // Bỏ qua dòng đang edit (ẩn)
                const roomInput = row.querySelector(`input[name$=".assignedPhysicalRoomNumber"]`);
                if (roomInput && roomInput.value === roomId) {
                    conflict = true;
                }
            }
        });
        if (conflict) {
            showToast(`Phòng này đã có người đứng đầu! Vui lòng chọn người khác hoặc bỏ chọn người đứng đầu cũ.`);
            return;
        }
    }

    // Kiểm tra ngày sinh không được ở tương lai
    const todayStr = new Date().toISOString().split('T')[0];
    if (dob > todayStr) {
        showToast('Ngày sinh không được vượt quá ngày hiện tại!');
        return;
    }
    if (dob < '1900-01-01') {
        showToast('Ngày sinh không hợp lệ!');
        return;
    }

    // Kiểm tra CCCD/Passport nếu có nhập
    if (id) {
        const isNumericOnly = /^\d+$/.test(id);
        if (isNumericOnly) {
            if (id.length !== 12) {
                showToast('Số CCCD không hợp lệ! Nếu chỉ nhập số, CCCD phải gồm đúng 12 chữ số.');
                return;
            }
        } else {
            const isValidPassport = /^[A-Za-z0-9]{6,15}$/.test(id);
            if (!isValidPassport) {
                showToast('Số Passport không hợp lệ! Passport phải từ 6-15 ký tự chữ và số.');
                return;
            }
        }
    }

    const depId = document.getElementById('depId').getAttribute('data-dependent-id');
    const parsedDepId = (depId && depId !== 'null' && depId !== '') ? depId : null;
    const editingRow = document.querySelector('.editing-row');
    if (editingRow) {
        editingRow.remove();
    }

    addDependentRow(name, id, dob, parsedDepId, roomId, isPrimary);

    document.getElementById('depName').value = '';
    document.getElementById('depId').value = '';
    document.getElementById('depId').removeAttribute('data-dependent-id');
    document.getElementById('depDob').value = '';
    document.getElementById('depRoom').value = '';
    document.getElementById('depIsPrimary').checked = false;
    
    const btnAddDependent = document.getElementById('btnAddDependent');
    if (btnAddDependent) {
        btnAddDependent.innerHTML = '<i class="fa-solid fa-plus"></i> Add';
    }
}

function addDependentRow(name, cccd, dob, dependentId, assignedPhysicalRoomNumber, isPrimary = false) {
    const tbody = document.getElementById('dependentsList');
    // remove empty message if present
    if (tbody.children.length === 1 && tbody.children[0].innerText.includes('No group members')) {
        tbody.innerHTML = '';
    }

    const tr = document.createElement('tr');
    tr.style.borderBottom = '1px solid #f1f5f9';
    tr.style.transition = 'background-color 0.2s ease';
    tr.onmouseover = () => tr.style.backgroundColor = '#f8fafc';
    tr.onmouseout = () => tr.style.backgroundColor = 'transparent';

    let hiddenIdInput = dependentId ? `<input type="hidden" name="dependents[${depIndexCounter}].dependentId" value="${dependentId}" />` : '';
    let hiddenRoomInput = assignedPhysicalRoomNumber ? `<input type="hidden" name="dependents[${depIndexCounter}].assignedPhysicalRoomNumber" value="${assignedPhysicalRoomNumber}" />` : '';
    let hiddenPrimaryInput = `<input type="hidden" name="dependents[${depIndexCounter}].isPrimaryContact" value="${isPrimary ? 'true' : 'false'}" />`;

    let effectiveDepId = dependentId || ('NEW_' + depIndexCounter);
    let depIdArg = `'${effectiveDepId}'`;
    
    // Thêm div ẩn chứa ID mapping để submit form dễ tìm
    let hiddenMapping = `<input type="hidden" class="faceid-mapping-id" data-target-id="${effectiveDepId}" data-index="${depIndexCounter}" />`;

    let roomIdArg = assignedPhysicalRoomNumber ? `'${assignedPhysicalRoomNumber}'` : 'null';
    let roleBadge = isPrimary ? `<span style="display:inline-block; margin-top: 4px; padding: 2px 8px; background: #fef3c7; color: #d97706; border-radius: 12px; font-size: 11px; font-weight: 700; letter-spacing: 0.5px; text-transform: uppercase;"><i class="fa-solid fa-star"></i> Đứng đầu</span>` : `<span style="font-size: 13px; color: #64748b; font-weight: 500;">Thành viên</span>`;
    let roomDisplay = assignedPhysicalRoomNumber ? `<span style="font-weight: 600; color: #0f766e; font-size: 13px;">Phòng ${assignedPhysicalRoomNumber}</span><br>` : '';
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
            <input type="hidden" name="dependents[${depIndexCounter}].gender" value="Other" />
        </td>
        <td style="padding: 12px 16px;">
            <button type="button" class="btn btn-outline btn-sm" style="color: #6366f1; border-color: #c7d2fe; background: #eef2ff; padding: 6px 10px; margin-right: 6px; border-radius: 6px; transition: all 0.2s;" onmouseover="this.style.background='#e0e7ff'" onmouseout="this.style.background='#eef2ff'" title="FaceID" onclick="openEnrollModal('DEPENDENT', ${depIdArg}, '${name}')"><i class="fa-solid fa-camera"></i></button>
            <button type="button" class="btn btn-outline btn-sm" style="color: #3b82f6; border-color: #bfdbfe; background: #eff6ff; padding: 6px 10px; margin-right: 6px; border-radius: 6px; transition: all 0.2s;" onmouseover="this.style.background='#dbeafe'" onmouseout="this.style.background='#eff6ff'" title="Edit" onclick="editDependentRow(this, '${name}', '${cccd}', '${dob}', ${depIdArg}, ${roomIdArg}, ${isPrimary})"><i class="fa-solid fa-pen"></i></button>
            <button type="button" class="btn btn-outline btn-sm" style="color: #ef4444; border-color: #fecaca; background: #fef2f2; padding: 6px 10px; border-radius: 6px; transition: all 0.2s;" onmouseover="this.style.background='#fee2e2'" onmouseout="this.style.background='#fef2f2'" title="Delete" onclick="this.closest('tr').remove()"><i class="fa-solid fa-trash"></i></button>
        </td>
    `;
    tbody.appendChild(tr);

    depIndexCounter++;
}

function editDependentRow(btn, name, cccd, dob, depId, roomId, isPrimary) {
    // Phục hồi dòng đang edit dang dở (nếu khách click sửa liên tục mà quên bấm Add)
    const editingRow = document.querySelector('.editing-row');
    if (editingRow) {
        editingRow.classList.remove('editing-row');
        editingRow.style.display = 'table-row';
    }

    document.getElementById('depName').value = name !== 'null' ? name : '';
    document.getElementById('depId').value = cccd !== 'null' ? cccd : '';
    if (depId) {
        document.getElementById('depId').setAttribute('data-dependent-id', depId);
    } else {
        document.getElementById('depId').removeAttribute('data-dependent-id');
    }
    document.getElementById('depDob').value = dob !== 'null' ? dob : '';
    document.getElementById('depRoom').value = roomId !== 'null' ? roomId : '';
    document.getElementById('depIsPrimary').checked = isPrimary;

    // Mở rộng danh sách nếu đang bị thu gọn để tiện xem
    const wrapper = document.getElementById('dependentsTableWrapper');
    if (wrapper.style.display === 'none') {
        toggleDependentsList();
    }

    const tr = btn.closest('tr');
    tr.classList.add('editing-row');
    tr.style.display = 'none';
    
    const btnAddDependent = document.getElementById('btnAddDependent');
    if (btnAddDependent) {
        btnAddDependent.innerHTML = '<i class="fa-solid fa-check"></i> Update';
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
        btn.innerHTML = '<i class="fa-solid fa-chevron-down"></i> Hien het';
    }
}

const _checkinForm = document.getElementById('checkinFormWrapper');
if (_checkinForm) {
    _checkinForm.addEventListener('submit', async function (e) {
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

        // Ràng buộc FaceID cho chủ đoàn
        // Lấy targetId của CUSTOMER
        const submitBookingId = document.getElementById('submitBookingId').value;
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
                    // Bắt buộc FaceID nếu đủ 14 tuổi, VÀ khách hàng mới hoặc phụ thuộc mới (có thể check targetId bắt đầu bằng NEW_)
                    if (age >= 14) {
                        // Nếu backend có check FaceID rồi thì không nói, ở đây force capture tại UI
                        // Chỉ force nếu là NEW_ hoặc chưa có vector. 
                        // Tạm thời nếu user muốn thì force hết. Hoặc nếu nó không nằm trong targetId (tức là targetId bắt đầu bằng NEW_)
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

        if (currentUnallocatedTours.length > 0) {
            const allocationMode = document.querySelector('input[name="tourAllocationMode"]:checked');
            if (allocationMode && allocationMode.value === 'PER_TOUR') {
                const hiddenRoomInputs = document.querySelectorAll('input[name^="tourAllocations"][name$=".roomNumber"]');
                const unassigned = Array.from(hiddenRoomInputs).filter(inp => !inp.value);
                if (unassigned.length > 0) {
                    e.preventDefault();
                    showToast('Che do Phan bo tung tour: vui long chon phong cho tat ca ' + currentUnallocatedTours.length + ' tour!');
                    return;
                }
            }
        }
        
        // Prevent default submission to process FaceIDs first
        e.preventDefault();
        
        // Show loading state
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
                // Dependent
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
        
        // Sau khi upload xong, submit form gốc
        _checkinForm.submit();
    });
} else {
    console.error('[checkin.js] CRITICAL: #checkinFormWrapper khong tim thay trong DOM!');
}

let currentUnallocatedTours = [];

function renderExistingTourBookings(toursDivId) {
    const section = document.getElementById('tourAllocationSection');
    const tbody = document.getElementById('tourBookingsList');
    if (!section || !tbody) return;

    tbody.innerHTML = '';
    currentUnallocatedTours = [];
    document.querySelectorAll('input[name^="tourAllocations"]').forEach(el => el.remove());

    const toursDiv = toursDivId ? document.getElementById(toursDivId) : null;
    if (!toursDiv) { section.style.display = 'none'; return; }

    const items = toursDiv.querySelectorAll('li');
    if (items.length === 0) { section.style.display = 'none'; return; }

    section.style.display = 'block';

    items.forEach(li => {
        const id = li.getAttribute('data-id');
        const tourName = li.getAttribute('data-tour');
        const date = li.getAttribute('data-date');
        const time = li.getAttribute('data-time');
        const count = li.getAttribute('data-count');
        const charge = parseFloat(li.getAttribute('data-charge') || 0);

        currentUnallocatedTours.push({ id, tourName, date, time, count, charge });

        const tr = document.createElement('tr');
        tr.style.borderBottom = '1px solid #f1f5f9';
        tr.style.transition = 'background-color 0.2s ease';
        tr.onmouseover = () => tr.style.backgroundColor = '#f8fafc';
        tr.onmouseout = () => tr.style.backgroundColor = 'transparent';
        tr.innerHTML =
            '<td style="padding:12px 16px;font-size:14px;font-weight:600;color:#1e293b">' + tourName + '</td>' +
            '<td style="padding:12px 16px;font-size:14px;color:#475569">' + date + '</td>' +
            '<td style="padding:12px 16px;font-size:14px;color:#475569">' + time + '</td>' +
            '<td style="padding:12px 16px;font-size:14px;color:#475569">' + count + ' khách</td>' +
            '<td style="padding:12px 16px;font-size:14px;font-weight:600;color:#0f766e;text-align:right">' + charge.toLocaleString('vi-VN') + ' đ</td>';
        tbody.appendChild(tr);
    });

    const allRadio = document.querySelector('input[name="tourAllocationMode"][value="ALL"]');
    if (allRadio) allRadio.checked = true;
    toggleTourAllocationMode();
}

function toggleTourAllocationMode() {
    const mode = document.querySelector('input[name="tourAllocationMode"]:checked');
    const perTourContainer = document.getElementById('perTourAllocationContainer');
    if (perTourContainer) {
        perTourContainer.style.display = (mode && mode.value === 'PER_TOUR') ? 'block' : 'none';
    }
    if (mode && mode.value === 'PER_TOUR') {
        renderPerTourAllocationRows();
    }
}

function renderPerTourAllocationRows() {
    const container = document.getElementById('perTourAllocationRows');
    if (!container) return;
    container.innerHTML = '';
    document.querySelectorAll('input[name^="tourAllocations"]').forEach(el => el.remove());

    currentUnallocatedTours.forEach(function (tour, i) {
        const row = document.createElement('div');
        row.style.cssText = 'display:grid;grid-template-columns:1fr 32px 1fr;align-items:center;padding:12px 16px;background:#f8fafc;border-radius:6px;border:1px solid #e2e8f0;transition:all 0.2s ease;gap:12px;';

        const labelInfo = document.createElement('div');
        labelInfo.style.cssText = 'display:flex;flex-direction:column;gap:4px;min-width:0;';
        
        const labelName = document.createElement('span');
        labelName.style.cssText = 'font-size:14px;font-weight:600;color:#1e293b;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;';
        labelName.innerText = tour.tourName;

        const labelDate = document.createElement('span');
        labelDate.style.cssText = 'font-size:12px;color:#64748b;font-weight:500;';
        labelDate.innerText = tour.date + ' • ' + tour.time;
        
        labelInfo.appendChild(labelName);
        labelInfo.appendChild(labelDate);

        const arrow = document.createElement('span');
        arrow.innerHTML = '<i class="fa-solid fa-arrow-right-long"></i>';
        arrow.style.cssText = 'color:#94a3b8;font-size:14px;text-align:center;';

        const select = document.createElement('select');
        select.style.cssText = 'width:100%;padding:8px 12px;border:1px solid #cbd5e1;border-radius:6px;background:white;font-size:14px;color:#334155;outline:none;cursor:pointer;box-shadow:0 1px 2px rgba(0,0,0,0.05);';
        select.innerHTML = '<option value="">-- Chọn phòng --</option>';
        assignedRooms.forEach(function (a) {
            const opt = document.createElement('option');
            opt.value = a.room;
            opt.innerText = 'Phòng ' + a.room + ' (' + a.type + ')';
            select.appendChild(opt);
        });

        const hiddenId = document.createElement('input');
        hiddenId.type = 'hidden';
        hiddenId.name = 'tourAllocations[' + i + '].tourBookingId';
        hiddenId.value = tour.id;

        const hiddenRoom = document.createElement('input');
        hiddenRoom.type = 'hidden';
        hiddenRoom.name = 'tourAllocations[' + i + '].roomNumber';
        hiddenRoom.value = '';

        select.addEventListener('change', function () {
            hiddenRoom.value = select.value;
        });

        row.appendChild(labelInfo);
        row.appendChild(arrow);
        row.appendChild(select);
        container.appendChild(row);

        const form = document.getElementById('checkinFormWrapper');
        if (form) {
            form.appendChild(hiddenId);
            form.appendChild(hiddenRoom);
        }
    });
}

// ==========================================
// THU THẬP KHUÔN MẶT (FACE ID ENROLLMENT)
// ==========================================
let faceApiLoaded = false;
let videoStream = null;
let currentEnrollType = null; // 'CUSTOMER' or 'DEPENDENT'
let currentEnrollId = null;
let pendingFaceEnrollments = {}; // Temporary storage for face vectors and images

const MODEL_URL = 'https://cdn.jsdelivr.net/npm/@vladmandic/face-api@1.7.12/model/';

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
    
    // Convert bookingId to customerId via UI element if it's CUSTOMER type
    // Since check-in uses bookingId, we will use bookingId and the backend can resolve customer or we just pass customerId from the backend.
    // In our case, the button passes bookingId, but backend needs customerId. We should have passed customer_id.
    // Let's modify the UI directly in JS: we can just ask user to scan, and we send it to backend API.
    // Wait, let's fetch customer id from the global variable or DOM.
    // Check-in modal has dataset.id which is bookingId.
    // To keep it simple, we assume targetId is customerId for CUSTOMER, and dependentId for DEPENDENT.
    // Actually in check-in.html `openEnrollModal('CUSTOMER', document.getElementById('submitBookingId').value)`
    // This is wrong, it sends bookingId. Let's fix that. I'll send it as `bookingId` for CUSTOMER and backend will find customer from booking.
    
    currentEnrollId = targetId; 
    
    const modal = document.getElementById('enrollFaceModal');
    const overlay = document.getElementById('enrollOverlay');
    const captureBtn = document.getElementById('captureBtn');
    
    document.getElementById('enrollTargetName').innerText = type === 'CUSTOMER' ? "Đang đăng ký cho Người Đặt Phòng..." : "Đang đăng ký cho " + (targetName || "Người Đi Kèm...");
    
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
    
    // Countdown 3 seconds
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
            if (existingKey === currentKey.toString()) continue; // Bỏ qua nếu chụp lại cho chính người này
            
            const existingVector = JSON.parse(pendingFaceEnrollments[existingKey].vector);
            const distance = faceapi.euclideanDistance(descriptor, existingVector);
            
            // Threshold = 0.5 for faceapi
            if (distance < 0.5) {
                showToast("Khuôn mặt này đã được quét cho một người khác trong đoàn! Vui lòng quét khuôn mặt khác.");
                overlay.style.display = 'none';
                captureBtn.disabled = false;
                return;
            }
        }
        
        // Capture face image
        const canvas = document.createElement('canvas');
        canvas.width = video.videoWidth;
        canvas.height = video.videoHeight;
        const ctx = canvas.getContext('2d');
        ctx.drawImage(video, 0, 0, canvas.width, canvas.height);
        
        const box = detection.detection.box;
        const faceCanvas = document.createElement('canvas');
        
        // Add padding around the face for better visibility
        const padX = box.width * 0.2;
        const padY = box.height * 0.2;
        const startX = Math.max(0, box.x - padX);
        const startY = Math.max(0, box.y - padY);
        const drawWidth = Math.min(canvas.width - startX, box.width + padX * 2);
        const drawHeight = Math.min(canvas.height - startY, box.height + padY * 2);
        
        faceCanvas.width = drawWidth;
        faceCanvas.height = drawHeight;
        faceCanvas.getContext('2d').drawImage(
            canvas, 
            startX, startY, drawWidth, drawHeight, 
            0, 0, drawWidth, drawHeight
        );
        
        const base64Image = faceCanvas.toDataURL('image/jpeg', 0.85);

        // Lưu tạm vào bộ nhớ JS
        const key = currentEnrollType === 'CUSTOMER' ? 'CUSTOMER' : currentEnrollId;
        pendingFaceEnrollments[key] = {
            type: currentEnrollType,
            id: currentEnrollId,
            vector: JSON.stringify(descriptor),
            image: base64Image
        };
        
        // Hiển thị ảnh xem trước trên giao diện checkin
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


function handleQrScan(val, target, inputEl) {
    if (!val) return;
    
    // Format expected: 001205015836||Nguyễn Xuân Lưu|14102005|Nam|TDP Ninh Sơn, Chúc Sơn, Chương Mỹ, Hà Nội|31052021
    const parts = val.split('|');
    if (parts.length >= 7) {
        const id = parts[0];
        const name = parts[2];
        const dobStr = parts[3]; // DDMMYYYY
        
        let dob = '';
        if (dobStr && dobStr.length === 8) {
            dob = `${dobStr.substring(4, 8)}-${dobStr.substring(2, 4)}-${dobStr.substring(0, 2)}`;
        }
        
        if (target === 'main') {
            document.getElementById('modalGuestName').value = name;
            document.getElementById('modalGuestCccd').value = id;
            showToast('Đã tự động điền thông tin chủ đoàn từ QR!', 'success');
        } else if (target === 'dep') {
            document.getElementById('depName').value = name;
            document.getElementById('depId').value = id;
            if (dob) {
                document.getElementById('depDob').value = dob;
            }
            showToast('Đã tự động điền thông tin thành viên từ QR!', 'success');
        } else if (target === 'auto-dep') {
            // Check for duplicates
            const mainCccd = document.getElementById('modalGuestCccd').value.trim();
            if (mainCccd === id) {
                showToast(`Thẻ CCCD của ${name} đã được quét cho trưởng đoàn!`, 'warning');
                if (inputEl) inputEl.value = '';
                return;
            }
            
            // Check if already in dependents
            const depCccdInputs = document.querySelectorAll('input[name$=".cccd"]');
            let isDuplicate = false;
            depCccdInputs.forEach(inp => {
                if (inp.value === id) isDuplicate = true;
            });
            
            if (isDuplicate) {
                showToast(`Thành viên ${name} đã có trong danh sách!`, 'warning');
                if (inputEl) inputEl.value = '';
                return;
            }

            addDependentRow(name, id, dob, null, '');
            showToast(`Đã tự động thêm thành viên: ${name}`, 'success');
        }
        
        if (inputEl) {
            inputEl.value = '';
        }
    }
}

let html5QrcodeScanner = null;
let currentQrTarget = null;

function openQrScannerModal(target) {
    currentQrTarget = target;
    document.getElementById('qrScannerModal').style.display = 'flex';
    
    // CCCD barcode is a square QR Code, use square box and limit format for speed
    if (!html5QrcodeScanner) {
        html5QrcodeScanner = new Html5QrcodeScanner(
            "qr-reader", 
            { 
                fps: 20, 
                qrbox: {width: 300, height: 300},
                formatsToSupport: [ Html5QrcodeSupportedFormats.QR_CODE ],
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
}

async function scanQrFromFile(input) {
    if (!input.files || input.files.length === 0) return;
    const file = input.files[0];
    
    // Stop the camera scanner if it's currently running
    if (html5QrcodeScanner) {
        try {
            await html5QrcodeScanner.clear();
        } catch(e) {}
    }
    
    const html5QrCode = new Html5Qrcode("qr-reader");
    try {
        const decodedText = await html5QrCode.scanFile(file, true);
        console.log("Scan result from file: " + decodedText);
        
        html5QrCode.clear().catch(e => {});
        closeQrScannerModal();
        
        let mockInput = { value: decodedText, tagName: 'MOCK' };
        handleQrScan(decodedText, currentQrTarget, mockInput);
    } catch (err) {
        console.error(err);
        showToast('Không tìm thấy mã QR hợp lệ trong ảnh!', 'error');
        html5QrCode.clear().catch(e => {});
    }
    input.value = '';
}

let remoteScanEventSource = null;
let remoteScanQrCode = null;

// Khởi tạo một Session duy nhất cho máy tính này khi tải trang
const persistentSessionId = (window.crypto && crypto.randomUUID) ? crypto.randomUUID() : ('session-' + Date.now() + '-' + Math.random().toString(36).substring(2, 10));

// Tự động kết nối SSE ngay từ đầu và duy trì mãi mãi
function initPersistentSse() {
    remoteScanEventSource = new EventSource('/api/v1/remote-scan/' + persistentSessionId + '/subscribe');
    
    remoteScanEventSource.addEventListener('SCAN_RESULT', function(event) {
        console.log('Received from remote: ' + event.data);
        
        let target = currentQrTarget;
        
        // Auto-assign logic for smooth continuous scanning
        if (!target) {
            const mainNameInput = document.getElementById('modalGuestName');
            const mainCccdInput = document.getElementById('modalGuestCccd');
            if (!mainNameInput.value.trim() && !mainCccdInput.value.trim()) {
                // If main guest is empty, assign to main
                target = 'main';
            } else {
                // If main guest is filled, assign as auto-dependent
                target = 'auto-dep';
            }
        }
        
        let mockInput = { value: event.data, tagName: 'MOCK' };
        handleQrScan(event.data, target, mockInput);
        
        if (currentQrTarget) {
            closeRemoteScanModal();
        }
    });
    
    remoteScanEventSource.onerror = function() {
        console.log('SSE Connection lost, reconnecting...');
        remoteScanEventSource.close();
        setTimeout(initPersistentSse, 2000); // Auto reconnect
    };
}
// Khởi chạy
initPersistentSse();

function openRemoteScanModal(target) {
    currentQrTarget = target;
    document.getElementById('remoteScanModal').style.display = 'flex';
    
    fetch('/api/v1/remote-scan/host-ip')
        .then(res => res.json())
        .then(data => {
            let host = window.location.host;
            let protocol = window.location.protocol;
            
            // If accessing via localhost, replace localhost with the actual IP
            if (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1') {
                host = data.ip + (window.location.port ? ':' + window.location.port : '');
                protocol = 'http:'; // Fallback to http for IP
            }
            
            const scanUrl = protocol + '//' + host + '/receptionist/remote-scan?session=' + persistentSessionId;
            
            const qrContainer = document.getElementById('remote-qrcode-container');
            if (!remoteScanQrCode) {
                qrContainer.innerHTML = '';
                remoteScanQrCode = new QRCode(qrContainer, {
                    text: scanUrl,
                    width: 200,
                    height: 200,
                    colorDark : '#0f172a',
                    colorLight : '#ffffff',
                    correctLevel : QRCode.CorrectLevel.H
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
