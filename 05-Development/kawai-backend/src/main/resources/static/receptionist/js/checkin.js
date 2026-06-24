

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

    // Xóa các hidden input cũ
    document.querySelectorAll('input[name="assignedRoomNumbers"]').forEach(el => el.remove());

    assignedRooms.forEach(a => {
        const badge = document.createElement('span');
        badge.className = 'status-badge';
        badge.style.cssText = 'background:#e0f2fe; color:#0284c7; display: inline-flex; align-items: center; gap: 6px; padding: 4px 10px; font-size: 13px; border: 1px solid #bae6fd;';
        badge.innerHTML = `${a.room} (${a.type}) <i class="fa-solid fa-xmark" style="cursor:pointer;" onclick="removeAssignedRoom('${a.room}')"></i>`;
        container.appendChild(badge);

        // Thêm hidden input cho mảng phòng
        const hiddenInput = document.createElement('input');
        hiddenInput.type = 'hidden';
        hiddenInput.name = 'assignedRoomNumbers';
        hiddenInput.value = a.room;
        document.getElementById('checkinFormWrapper').appendChild(hiddenInput);
    });

    // Cập nhật lại dropdown Chọn phòng cho Người phụ thuộc
    const depRoomSelect = document.getElementById('depRoom');
    depRoomSelect.innerHTML = '<option value="">-- Select Room --</option>';
    assignedRooms.forEach(a => {
        const opt = document.createElement('option');
        opt.value = a.room;
        opt.innerText = `${a.room} (${a.type})`;
        depRoomSelect.appendChild(opt);
    });
}



function openCheckinModal(bookingId, guestName, phone, cccd, roomSummary, depsDivId) {
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
    if (!name || !dob) {
        alert('Please fill Name and Date of Birth!');
        return;
    }
    if (!roomId) {
        alert('Please assign the guest to a room!');
        return;
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

    addDependentRow(name, id, dob, parsedDepId, roomId);

    document.getElementById('depName').value = '';
    document.getElementById('depId').value = '';
    document.getElementById('depId').removeAttribute('data-dependent-id');
    document.getElementById('depDob').value = '';
    document.getElementById('depRoom').value = '';
}

function addDependentRow(name, cccd, dob, dependentId, assignedPhysicalRoomNumber) {
    const tbody = document.getElementById('dependentsList');
    // remove empty message if present
    if (tbody.children.length === 1 && tbody.children[0].innerText.includes('No group members')) {
        tbody.innerHTML = '';
    }

    const tr = document.createElement('tr');
    tr.style.borderBottom = '1px solid #e2e8f0';
    let hiddenIdInput = dependentId ? `<input type="hidden" name="dependents[${depIndexCounter}].dependentId" value="${dependentId}" />` : '';
    let hiddenRoomInput = assignedPhysicalRoomNumber ? `<input type="hidden" name="dependents[${depIndexCounter}].assignedPhysicalRoomNumber" value="${assignedPhysicalRoomNumber}" />` : '';

    let depIdArg = dependentId ? `'${dependentId}'` : 'null';
    let roomIdArg = assignedPhysicalRoomNumber ? `'${assignedPhysicalRoomNumber}'` : 'null';

    tr.innerHTML = `
        <td style="padding: 8px; font-size: 14px;">
            ${name}
            ${hiddenIdInput}
            ${hiddenRoomInput}
            <input type="hidden" name="dependents[${depIndexCounter}].fullName" value="${name}" />
        </td>
        <td style="padding: 8px; font-size: 14px;">
            ${cccd}
            <input type="hidden" name="dependents[${depIndexCounter}].cccd" value="${cccd}" />
        </td>
        <td style="padding: 8px; font-size: 14px;">
            ${dob}
            <input type="hidden" name="dependents[${depIndexCounter}].dateOfBirth" value="${dob}" />
            <input type="hidden" name="dependents[${depIndexCounter}].gender" value="Other" />
        </td>
        <td style="padding: 8px;">
            <button type="button" class="btn btn-outline btn-sm" style="color: #3b82f6; border-color: #3b82f6; padding: 4px 8px; margin-right: 4px;" title="Edit" onclick="editDependentRow(this, '${name}', '${cccd}', '${dob}', ${depIdArg}, ${roomIdArg})"><i class="fa-solid fa-pen"></i></button>
            <button type="button" class="btn btn-outline btn-sm" style="color: #ef4444; border-color: #ef4444; padding: 4px 8px;" title="Delete" onclick="this.closest('tr').remove()"><i class="fa-solid fa-trash"></i></button>
        </td>
    `;
    tbody.appendChild(tr);

    depIndexCounter++;
}

function editDependentRow(btn, name, cccd, dob, depId, roomId) {
    document.getElementById('depName').value = name === '(Chưa cập nhật)' ? '' : name;
    document.getElementById('depId').value = cccd;
    document.getElementById('depDob').value = dob;
    if (roomId) {
        document.getElementById('depRoom').value = roomId;
    } else {
        document.getElementById('depRoom').value = '';
    }

    // Set attributes to track which dependent is being edited
    if (depId) {
        document.getElementById('depId').setAttribute('data-dependent-id', depId);
    } else {
        document.getElementById('depId').removeAttribute('data-dependent-id');
    }

    // Mở rộng danh sách nếu đang bị thu gọn để tiện xem
    const wrapper = document.getElementById('dependentsTableWrapper');
    if (wrapper.style.display === 'none') {
        toggleDependentsList();
    }

    btn.closest('tr').remove();
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
