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
    pendingFaceEnrollments = {};
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
    tr.style.borderBottom = '1px solid #f1f5f9';
    tr.style.transition = 'background-color 0.2s ease';
    tr.onmouseover = () => tr.style.backgroundColor = '#f8fafc';
    tr.onmouseout = () => tr.style.backgroundColor = 'transparent';

    let hiddenIdInput = dependentId ? `<input type="hidden" name="dependents[${depIndexCounter}].dependentId" value="${dependentId}" />` : '';
    let hiddenRoomInput = assignedPhysicalRoomNumber ? `<input type="hidden" name="dependents[${depIndexCounter}].assignedPhysicalRoomNumber" value="${assignedPhysicalRoomNumber}" />` : '';
    let hiddenPrimaryInput = `<input type="hidden" name="dependents[${depIndexCounter}].isPrimaryContact" value="${isPrimary ? 'true' : 'false'}" />`;

    let depIdArg = dependentId ? `'${dependentId}'` : 'null';
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
            <button type="button" class="btn btn-outline btn-sm" style="color: #6366f1; border-color: #c7d2fe; background: #eef2ff; padding: 6px 10px; margin-right: 6px; border-radius: 6px; transition: all 0.2s;" onmouseover="this.style.background='#e0e7ff'" onmouseout="this.style.background='#eef2ff'" title="FaceID" onclick="openEnrollModal('DEPENDENT', ${depIdArg})"><i class="fa-solid fa-camera"></i></button>
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
        btn.innerHTML = '<i class="fa-solid fa-chevron-down"></i> Hien het';
    }
}

const _checkinForm = document.getElementById('checkinFormWrapper');
if (_checkinForm) {
    _checkinForm.addEventListener('submit', async function (e) {
        if (assignedRooms.length === 0) {
            e.preventDefault();
            alert('Vui long phan it nhat 1 phong truoc khi hoan tat Check-in!');
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
            alert('Thieu nguoi dung dau cho phong: ' + missingRooms.join(', '));
            return;
        }

        if (currentUnallocatedTours.length > 0) {
            const allocationMode = document.querySelector('input[name="tourAllocationMode"]:checked');
            if (allocationMode && allocationMode.value === 'PER_TOUR') {
                const hiddenRoomInputs = document.querySelectorAll('input[name^="tourAllocations"][name$=".roomNumber"]');
                const unassigned = Array.from(hiddenRoomInputs).filter(inp => !inp.value);
                if (unassigned.length > 0) {
                    e.preventDefault();
                    alert('Che do Phan bo tung tour: vui long chon phong cho tat ca ' + currentUnallocatedTours.length + ' tour!');
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
        
        // Upload pending FaceIDs
        for (const key in pendingFaceEnrollments) {
            const data = pendingFaceEnrollments[key];
            const payload = {
                faceVectorData: data.vector,
                faceImageBase64: data.image
            };
            if (data.type === 'CUSTOMER') {
                payload.bookingId = data.id;
            } else {
                payload.dependentId = data.id;
            }
            
            try {
                await fetch('/api/faceid/enroll', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(payload)
                });
            } catch (err) {
                console.error("Lỗi khi upload FaceID cho", key, err);
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
        row.style.cssText = 'display:flex;align-items:center;justify-content:space-between;padding:12px 16px;background:#f8fafc;border-radius:6px;border:1px solid #e2e8f0;transition:all 0.2s ease;';

        const labelInfo = document.createElement('div');
        labelInfo.style.cssText = 'display:flex;flex-direction:column;gap:4px;';
        
        const labelName = document.createElement('span');
        labelName.style.cssText = 'font-size:14px;font-weight:600;color:#1e293b';
        labelName.innerText = tour.tourName;

        const labelDate = document.createElement('span');
        labelDate.style.cssText = 'font-size:12px;color:#64748b;font-weight:500;';
        labelDate.innerText = tour.date + ' • ' + tour.time;
        
        labelInfo.appendChild(labelName);
        labelInfo.appendChild(labelDate);

        const rightSide = document.createElement('div');
        rightSide.style.cssText = 'display:flex;align-items:center;gap:16px;flex:0.6;';

        const arrow = document.createElement('span');
        arrow.innerHTML = '<i class="fa-solid fa-arrow-right-long"></i>';
        arrow.style.cssText = 'color:#94a3b8;font-size:14px;';

        const select = document.createElement('select');
        select.style.cssText = 'flex:1;padding:8px 12px;border:1px solid #cbd5e1;border-radius:6px;background:white;font-size:14px;color:#334155;outline:none;cursor:pointer;box-shadow:0 1px 2px rgba(0,0,0,0.05);';
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

        rightSide.appendChild(arrow);
        rightSide.appendChild(select);
        
        row.appendChild(labelInfo);
        row.appendChild(rightSide);
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
        alert("Không thể tải AI Models. Vui lòng kiểm tra kết nối mạng.");
        return false;
    }
}

async function openEnrollModal(type, targetId) {
    if (!targetId || targetId === "undefined" || targetId === "") {
        alert("Không xác định được ID Khách hàng! Hãy kiểm tra lại.");
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
    
    document.getElementById('enrollTargetName').innerText = type === 'CUSTOMER' ? "Đang đăng ký cho Người Đặt Phòng..." : "Đang đăng ký cho Người Đi Kèm...";
    
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
        alert("Vui lòng cấp quyền truy cập Camera cho trình duyệt.");
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
            alert("Không tìm thấy khuôn mặt rõ ràng. Vui lòng nhìn thẳng vào camera và thử lại.");
            overlay.style.display = 'none';
            captureBtn.disabled = false;
            return;
        }

        const descriptor = Array.from(detection.descriptor);
        
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
        alert("Đã xảy ra lỗi khi quét khuôn mặt.");
        overlay.style.display = 'none';
        captureBtn.disabled = false;
    }
}

function updateFacePreviewUI(type, targetId, base64Image) {
    let container;
    if (type === 'CUSTOMER') {
        container = document.getElementById('customerFacePreviewContainer');
        if (!container) {
            const btn = document.querySelector('button[onclick*="openEnrollModal(\\\'CUSTOMER\\\'"]');
            if (btn) {
                container = document.createElement('div');
                container.id = 'customerFacePreviewContainer';
                container.style.cssText = 'display: inline-block; margin-left: 10px; vertical-align: middle; position: relative;';
                btn.parentNode.insertBefore(container, btn.nextSibling);
            }
        }
    } else {
        const tr = document.querySelector(`button[onclick*="openEnrollModal('DEPENDENT', '${targetId}')"]`).closest('tr');
        if (tr) {
            const td = tr.querySelector('td:last-child');
            container = td.querySelector('.dep-face-preview');
            if (!container) {
                container = document.createElement('div');
                container.className = 'dep-face-preview';
                container.style.cssText = 'display: inline-block; margin-left: 10px; vertical-align: middle; position: relative;';
                td.appendChild(container);
            }
        }
    }
    
    if (container) {
        container.innerHTML = `
            <img src="${base64Image}" style="width: 36px; height: 36px; border-radius: 4px; object-fit: cover; border: 2px solid #10b981; box-shadow: 0 2px 4px rgba(0,0,0,0.1);" title="Đã chụp ảnh. Bấm nút FaceID để chụp lại.">
            <i class="fa-solid fa-circle-check" style="color: #10b981; position: absolute; top: -6px; right: -6px; background: white; border-radius: 50%; font-size: 14px;"></i>
        `;
    }
}

