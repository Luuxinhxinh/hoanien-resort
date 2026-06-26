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
    if(displayEl) {
        displayEl.innerText = remaining.toLocaleString() + ' VND';
        if(remaining < 0) {
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
        limitInput.oninput = function() { handleCheckinCreditInput(this, index); };
        
        const delBtn = document.createElement('i');
        delBtn.className = 'fa-solid fa-xmark';
        delBtn.style.cssText = 'cursor:pointer; color: #ef4444; font-size: 16px;';
        delBtn.onclick = function() { removeAssignedRoom(a.room); };
        
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
    updateCheckinCreditLimitDisplay();
}


function openCheckinModal(bookingId, guestName, phone, cccd, roomSummary, depsDivId, creditLimit) {
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

    document.getElementById('checkinModal').style.display = 'flex';
}

function closeCheckinModal() {
    document.getElementById('checkinModal').style.display = 'none';
}

let depIndexCounter = 0;

function addDependent() {
    const name = document.getElementById('depName').value;
    const id = document.getElementById('depId').value;
    const dob = document.getElementById('depDob').value;
    const roomId = document.getElementById('depRoom').value;
    const isPrimary = document.getElementById('depIsPrimary').checked;
    if (!name || !dob) {
        alert('Please fill Name and Date of Birth!');
        return;
    }
    if (!roomId) {
        alert('Please assign the guest to a room!');
        return;
    }

    if (isPrimary) {
        if (assignedRooms.length > 0 && roomId === assignedRooms[0].room) {
            alert(`Phòng ${roomId} đã được chỉ định cho khách chính đứng đầu! Vui lòng không chọn người đi kèm làm người đứng đầu cho phòng này.`);
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
            alert(`Phòng này đã có người đứng đầu! Vui lòng chọn người khác hoặc bỏ chọn người đứng đầu cũ.`);
            return;
        }
    }

    // Kiểm tra ngày sinh không được ở tương lai
    const todayStr = new Date().toISOString().split('T')[0];
    if (dob > todayStr) {
        alert('Ngày sinh không được vượt quá ngày hiện tại!');
        return;
    }
    if (dob < '1900-01-01') {
        alert('Ngày sinh không hợp lệ!');
        return;
    }

    // Kiểm tra CCCD/Passport nếu có nhập
    if (id) {
        const isNumericOnly = /^\d+$/.test(id);
        if (isNumericOnly) {
            if (id.length !== 12) {
                alert('Số CCCD không hợp lệ! Nếu chỉ nhập số, CCCD phải gồm đúng 12 chữ số.');
                return;
            }
        } else {
            const isValidPassport = /^[A-Za-z0-9]{6,15}$/.test(id);
            if (!isValidPassport) {
                alert('Số Passport không hợp lệ! Passport phải từ 6-15 ký tự chữ và số.');
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
}

function addDependentRow(name, cccd, dob, dependentId, assignedPhysicalRoomNumber, isPrimary = false) {
    const tbody = document.getElementById('dependentsList');
    // remove empty message if present
    if (tbody.children.length === 1 && tbody.children[0].innerText.includes('No group members')) {
        tbody.innerHTML = '';
    }

    const tr = document.createElement('tr');
    tr.style.borderBottom = '1px solid #e2e8f0';
    let hiddenIdInput = dependentId ? `<input type="hidden" name="dependents[${depIndexCounter}].dependentId" value="${dependentId}" />` : '';
    let hiddenRoomInput = assignedPhysicalRoomNumber ? `<input type="hidden" name="dependents[${depIndexCounter}].assignedPhysicalRoomNumber" value="${assignedPhysicalRoomNumber}" />` : '';
    let hiddenPrimaryInput = `<input type="hidden" name="dependents[${depIndexCounter}].isPrimaryContact" value="${isPrimary ? 'true' : 'false'}" />`;

    let depIdArg = dependentId ? `'${dependentId}'` : 'null';
    let roomIdArg = assignedPhysicalRoomNumber ? `'${assignedPhysicalRoomNumber}'` : 'null';
    let roleBadge = isPrimary ? `<span style="display:inline-block; margin-top: 4px; padding: 2px 6px; background: #fef3c7; color: #d97706; border-radius: 4px; font-size: 11px; font-weight: 600;"><i class="fa-solid fa-star"></i> Đứng đầu</span>` : `<span style="font-size: 13px; color: #64748b;">Thành viên</span>`;
    let roomDisplay = assignedPhysicalRoomNumber ? `<span style="font-weight: 500; color: #334155;">Phòng ${assignedPhysicalRoomNumber}</span><br>` : '';
    let finalRoleDisplay = `${roomDisplay}${roleBadge}`;

    tr.innerHTML = `
        <td style="padding: 8px; font-size: 14px;">
            ${name}
            ${hiddenIdInput}
            ${hiddenRoomInput}
            ${hiddenPrimaryInput}
            <input type="hidden" name="dependents[${depIndexCounter}].fullName" value="${name}" />
        </td>
        <td style="padding: 8px; font-size: 14px;">
            ${cccd}
            <input type="hidden" name="dependents[${depIndexCounter}].cccd" value="${cccd}" />
        </td>
        <td style="padding: 8px; font-size: 14px;">
            ${finalRoleDisplay}
        </td>
        <td style="padding: 8px; font-size: 14px;">
            ${dob}
            <input type="hidden" name="dependents[${depIndexCounter}].dateOfBirth" value="${dob}" />
            <input type="hidden" name="dependents[${depIndexCounter}].gender" value="Other" />
        </td>
        <td style="padding: 8px;">
            <button type="button" class="btn btn-outline btn-sm" style="color: #3b82f6; border-color: #3b82f6; padding: 4px 8px; margin-right: 4px;" title="Edit" onclick="editDependentRow(this, '${name}', '${cccd}', '${dob}', ${depIdArg}, ${roomIdArg}, ${isPrimary})"><i class="fa-solid fa-pen"></i></button>
            <button type="button" class="btn btn-outline btn-sm" style="color: #ef4444; border-color: #ef4444; padding: 4px 8px;" title="Delete" onclick="this.closest('tr').remove()"><i class="fa-solid fa-trash"></i></button>
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

    // Thay vì xóa luôn, ta chỉ ẩn nó đi và đánh dấu
    const tr = btn.closest('tr');
    tr.classList.add('editing-row');
    tr.style.display = 'none';
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

document.getElementById('checkinFormWrapper').addEventListener('submit', function (e) {
    if (assignedRooms.length === 0) {
        e.preventDefault();
        alert('Vui lòng phân ít nhất 1 phòng trước khi hoàn tất Check-in!');
        return;
    }

    const assignedRoomNumbers = assignedRooms.map(r => r.room);
    
    // Master customer đứng đầu phòng đầu tiên
    const masterRoom = assignedRoomNumbers[0];
    
    // Kiểm tra các phòng còn lại xem đã có đủ người đứng đầu chưa
    const primaryInputs = document.querySelectorAll(`input[name$=".isPrimaryContact"][value="true"]`);
    const primaryRooms = [];
    primaryInputs.forEach(input => {
        const row = input.closest('tr');
        if (row && !row.classList.contains('editing-row')) {
            const roomInput = row.querySelector(`input[name$=".assignedPhysicalRoomNumber"]`);
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
        alert(`Thiếu người đứng đầu cho phòng: ${missingRooms.join(', ')}. Vui lòng chọn 1 người phụ thuộc làm người đứng đầu cho mỗi phòng này trước khi hoàn tất Check-in!`);
    }
});
