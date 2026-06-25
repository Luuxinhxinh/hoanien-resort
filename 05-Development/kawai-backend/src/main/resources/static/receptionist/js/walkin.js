let todayStrGlobal = '';

document.addEventListener('DOMContentLoaded', () => {
    const typeSelect = document.getElementById('walkInTypeSelect');
    if (!typeSelect || typeof roomInventory === 'undefined') return;

    for (const category in roomInventory) {
        const opt = document.createElement('option');
        opt.value = category;
        opt.innerText = category;
        typeSelect.appendChild(opt);
    }

    // Set min date for checkoutDate and max date for guestDob to today (Local Time)
    const checkoutDateInput = document.getElementById('checkoutDate');
    const guestDobInput = document.getElementById('guestDob');
    const depDobInput = document.getElementById('depDob');
    const localDate = new Date();
    const year = localDate.getFullYear();
    const month = String(localDate.getMonth() + 1).padStart(2, '0');
    const day = String(localDate.getDate()).padStart(2, '0');
    todayStrGlobal = `${year}-${month}-${day}`;
    if (checkoutDateInput) {
        checkoutDateInput.min = todayStrGlobal;
        checkoutDateInput.addEventListener('change', () => {
            if (document.getElementById('step4Container').style.display === 'block') {
                showPaymentStep();
            }
        });
    }
    if (guestDobInput) guestDobInput.max = todayStrGlobal;
    if (depDobInput) depDobInput.max = todayStrGlobal;
});

function updateWalkInAvailableRooms() {
    const typeSelect = document.getElementById('walkInTypeSelect');
    const roomSelect = document.getElementById('walkInPhysicalRoomSelect');
    roomSelect.innerHTML = '<option value="">-- Select Physical Room --</option>';

    const selectedType = typeSelect.value;
    if (!selectedType) return;

    const available = roomInventory[selectedType] || [];
    available.forEach(r => {
        const opt = document.createElement('option');

        const roomId = r.id !== undefined ? r.id : r;
        const roomNum = r.number !== undefined ? r.number : r;
        opt.value = roomId;
        opt.innerText = 'Phòng ' + roomNum;

        opt.dataset.roomNum = roomNum;

        const catParts = selectedType.split(' - ');
        opt.dataset.category = catParts[0];
        if (catParts.length > 1) {
            opt.dataset.price = catParts[1].split(' (')[0];
        } else {
            opt.dataset.price = '';
        }

        roomSelect.appendChild(opt);
    });
}

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

let walkInCart = [];

function addRoomToCart() {
    const select = document.getElementById('walkInPhysicalRoomSelect');
    const roomId = select.value;
    if (!roomId) return;

    const selectedOpt = select.options[select.selectedIndex];
    const roomNum = selectedOpt.dataset.roomNum;
    const category = selectedOpt.dataset.category;
    const price = selectedOpt.dataset.price;

    if (walkInCart.find(r => r.roomId == roomId)) {
        alert("Phòng này đã có trong danh sách!");
        return;
    }

    walkInCart.push({ roomId: parseInt(roomId), roomNum, category, price });
    renderRoomCart();
    unlockStep2();

    if (document.getElementById('step4Container').style.display === 'block') {
        showPaymentStep();
    }
}

function removeRoomFromCart(index) {
    const r = walkInCart[index];
    walkInCart.splice(index, 1);

    walkInDependents = walkInDependents.filter(d => d.roomId != r.roomId);
    renderAccompaniedGuests();
    renderRoomCart();

    if (walkInCart.length === 0) {
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
        showPaymentStep();
    }
}

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
    walkInCart.forEach((room, index) => {
        const tr = document.createElement('tr');
        tr.style.borderBottom = '1px dashed #e2e8f0';
        tr.innerHTML = `
            <td style="padding: 10px 16px; font-weight: 500;">${room.roomNum}</td>
            <td style="padding: 10px 16px;">${room.category}</td>
            <td style="padding: 10px 16px; font-weight: 500; color: #16a34a;">${room.price}</td>
            <td style="padding: 10px 16px; text-align: right;">
                <button type="button" class="btn btn-sm btn-outline" style="color: #ef4444; border-color: #fca5a5;" onclick="removeRoomFromCart(${index})"><i class="fa-solid fa-trash"></i></button>
            </td>
        `;
        body.appendChild(tr);

        const opt = document.createElement('option');
        opt.value = room.roomId;
        opt.innerText = room.roomNum;
        depRoomSelect.appendChild(opt);
    });
}

function unlockFinalButton(customerId) {
    document.getElementById('guestStatusMsg').style.display = 'inline-block';
    document.getElementById('mockCustomerId').innerText = customerId;
    const continueBtn = document.getElementById('continueToPaymentBtn');
    if (continueBtn) {
        continueBtn.style.opacity = '1';
        continueBtn.style.pointerEvents = 'auto';
    }
}

function searchCustomer() {
    const phone = document.getElementById('guestPhone').value;
    const cccd = document.getElementById('guestId').value;
    const keyword = phone || cccd;
    if (!keyword) {
        alert("Vui lòng nhập Số điện thoại hoặc CCCD/Passport để tìm kiếm.");
        return;
    }

    fetch('/api/receptionist/walkin/search-customer?keyword=' + encodeURIComponent(keyword))
        .then(response => {
            if (response.ok) {
                return response.json();
            } else if (response.status === 404) {
                throw new Error('Không tìm thấy khách hàng với SĐT hoặc CCCD vừa nhập.');
            } else {
                throw new Error('Lỗi hệ thống khi tìm kiếm khách hàng.');
            }
        })
        .then(data => {
            document.getElementById('guestName').value = data.fullName || '';
            document.getElementById('guestId').value = data.cccd || '';
            document.getElementById('guestEmail').value = data.email || '';
            if (data.dateOfBirth) {
                document.getElementById('guestDob').value = data.dateOfBirth;
            }

            unlockFinalButton("CUST-" + data.id);
            document.getElementById('guestStatusMsg').innerHTML = `<i class="fa-solid fa-check-circle"></i> Đã tìm thấy khách hàng cũ (ID: <span id="mockCustomerId">${data.id}</span>)`;
            document.getElementById('guestStatusMsg').style.color = '#16a34a';
            document.getElementById('guestStatusMsg').style.background = '#dcfce7';
        })
        .catch(error => {
            alert(error.message);
        });
}

function validateGuestInfo() {
    const name = document.getElementById('guestName').value;
    const phone = document.getElementById('guestPhone').value;
    const id = document.getElementById('guestId').value;
    const email = document.getElementById('guestEmail').value;
    const dob = document.getElementById('guestDob').value;

    if (!name || !phone || !id || !dob) {
        alert("Vui lòng điền đầy đủ thông tin bắt buộc (*), bao gồm cả Ngày sinh.");
        return false;
    }

    const today = new Date();
    today.setHours(0, 0, 0, 0);
    if (new Date(dob) > today) {
        alert("LỖI: Ngày sinh không thể ở trong tương lai!");
        return false;
    }

    const phoneRegex = /^(0|\+84)[3|5|7|8|9][0-9]{8}$/;
    if (!phoneRegex.test(phone)) {
        alert("Số điện thoại không hợp lệ. Vui lòng nhập số điện thoại Việt Nam hợp lệ (VD: 0901234567).");
        return false;
    }
    if (email) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(email)) {
            alert("Định dạng Email không hợp lệ.");
            return false;
        }
    }
    const cccdRegex = /^\d{12}$/;
    if (!cccdRegex.test(id)) {
        alert("CCCD không hợp lệ. Vui lòng nhập đúng 12 chữ số CCCD.");
        return false;
    }

    return true;
}

function validateNewCustomer() {
    if (!validateGuestInfo()) return;

    unlockFinalButton("NEW-GUEST");
    document.getElementById('guestStatusMsg').innerHTML = `<i class="fa-solid fa-info-circle"></i> Sẵn sàng Check-in (Tài khoản sẽ được tạo tự động)`;
    document.getElementById('guestStatusMsg').style.display = 'inline-block';
    document.getElementById('guestStatusMsg').style.color = '#0284c7';
    document.getElementById('guestStatusMsg').style.background = '#e0f2fe';
}

function showPaymentStep() {
    if (walkInCart.length === 0) {
        alert("Vui lòng chọn ít nhất 1 phòng để Check-in!");
        return;
    }
    const checkOutDate = document.getElementById('checkoutDate').value;
    if (!checkOutDate) {
        alert("Vui lòng nhập Ngày Trả Phòng dự kiến!");
        return;
    }
    const dob = document.getElementById('guestDob').value;
    if (!validateGuestInfo()) return;

    const localNow = new Date();
    const checkInStr = `${localNow.getFullYear()}-${String(localNow.getMonth() + 1).padStart(2, '0')}-${String(localNow.getDate()).padStart(2, '0')}`;

    // Format payload
    const roomSelections = walkInCart.map(r => {
        return {
            roomId: r.roomId,
            accompaniedGuests: walkInDependents.filter(d => d.roomId == r.roomId)
        };
    });

    const payload = {
        roomSelections: roomSelections,
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
            if (!res.ok) throw new Error("Lỗi Server khi tính phí");
            return res.json();
        })
        .then(data => {
            document.getElementById('step4Container').style.display = 'block';
            document.getElementById('continueToPaymentBtn').parentElement.style.display = 'none';

            currentTotalCharge = data.totalCharge || 0;

            const ci = new Date();
            ci.setHours(0, 0, 0, 0);
            const co = new Date(checkOutDate);
            co.setHours(0, 0, 0, 0);
            let nights = Math.round(Math.abs(co - ci) / (1000 * 60 * 60 * 24));
            if (nights <= 0) nights = 1;

            const formatter = new Intl.NumberFormat('vi-VN');
            const labelEl = document.getElementById('summaryBasePriceLabel');
            if (labelEl) labelEl.innerText = `Tổng tiền phòng (${nights} đêm):`;

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

            document.getElementById('summaryBasePrice').innerText = formatter.format(data.baseRoomPrice || 0) + ' VNĐ';
            document.getElementById('summarySurcharge').innerText = formatter.format(data.surchargeAmount || 0) + ' VNĐ';
            document.getElementById('summaryTotal').innerText = formatter.format(currentTotalCharge) + ' VNĐ';

            updateWalkInDeposit();

            document.getElementById('step4Container').scrollIntoView({ behavior: 'smooth' });
        })
        .catch(err => {
            alert("Lỗi tính toán: " + err.message);
        });
}

let currentTotalCharge = 0;

function updateWalkInDeposit() {
    const option = document.querySelector('input[name="paymentOption"]:checked');
    if (!option) return;

    let deposit = currentTotalCharge;
    if (option.value === "30") {
        deposit = Math.round(currentTotalCharge * 0.3);
    }

    document.getElementById('depositAmount').value = deposit;

    const formatter = new Intl.NumberFormat('vi-VN');
    document.getElementById('depositAmountDisplay').value = formatter.format(deposit);
}

function backToStep3() {
    document.getElementById('step4Container').style.display = 'none';
    document.getElementById('continueToPaymentBtn').parentElement.style.display = 'flex';
}

function submitCheckIn() {
    if (walkInCart.length === 0) {
        alert("Vui lòng chọn ít nhất 1 phòng để Check-in!");
        return;
    }

    const name = document.getElementById('guestName').value;
    const phone = document.getElementById('guestPhone').value;
    const cccd = document.getElementById('guestId').value;
    const email = document.getElementById('guestEmail').value;
    const dob = document.getElementById('guestDob').value;
    const checkOutDate = document.getElementById('checkoutDate').value;

    if (!checkOutDate) {
        alert("Vui lòng nhập Ngày Trả Phòng dự kiến!");
        return;
    }

    const today = new Date();
    today.setHours(0, 0, 0, 0); // Đặt về 0 giờ để so sánh đúng ngày
    const selectedDate = new Date(checkOutDate);
    if (selectedDate < today) {
        alert("LỖI: Ngày trả phòng (Checkout Date) không thể ở trong quá khứ!");
        return;
    }

    // Xác thực lại thông tin khách hàng trước khi gửi API (để tránh user sửa data sau khi Unlock)
    if (!validateGuestInfo()) return;

    // Master customer đứng đầu phòng đầu tiên
    const masterRoomId = walkInCart[0].roomId;

    // Kiểm tra các phòng còn lại xem đã có đủ người đứng đầu chưa
    const primaryRoomIds = walkInDependents.filter(d => d.isPrimaryContact).map(d => d.roomId);

    const missingRooms = [];
    for (let i = 1; i < walkInCart.length; i++) {
        const room = walkInCart[i];
        // roomId của walkInCart lưu dưới dạng string vì get từ select option,
        // dep.roomId cũng string
        if (!primaryRoomIds.includes(String(room.roomId))) {
            missingRooms.push(room.roomNum);
        }
    }

    if (missingRooms.length > 0) {
        alert(`Thiếu người đứng đầu cho phòng: ${missingRooms.join(', ')}. Vui lòng quay lại Step 3 và chọn 1 người đi kèm làm người đứng đầu cho mỗi phòng này trước khi hoàn tất!`);
        return;
    }

    const localNow = new Date();
    const checkInStr = `${localNow.getFullYear()}-${String(localNow.getMonth() + 1).padStart(2, '0')}-${String(localNow.getDate()).padStart(2, '0')}`;

    const roomSelections = walkInCart.map(r => {
        return {
            roomId: r.roomId,
            accompaniedGuests: walkInDependents.filter(d => d.roomId == r.roomId)
        };
    });

    const payload = {
        roomSelections: roomSelections,
        checkInDate: checkInStr,
        checkOutDate: checkOutDate,
        fullName: name,
        phone: phone,
        cccd: cccd,
        email: email,
        dateOfBirth: dob,
        accompaniedGuests: walkInDependents,
        depositAmount: parseFloat(document.getElementById('depositAmount').value) || 0,
        paymentMethod: document.getElementById('paymentMethod').value || 'Tiền mặt'
    };

    // Đổi state nút thành Loading
    const completeBtn = document.getElementById('completeBtn');
    const oldText = completeBtn.innerHTML;
    completeBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Processing...';
    completeBtn.disabled = true;

    fetch('/api/receptionist/walkin/checkin', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
    })
        .then(response => {
            if (!response.ok) {
                return response.json().then(err => { throw new Error(err.error || err.message || "Lỗi khi Check-in"); });
            }
            return response.json();
        })
        .then(data => {
            const modal = document.getElementById('successModal');
            const msg = document.getElementById('modalMessage');
            const accInfo = document.getElementById('modalAccountInfo');
            const closeBtn = document.getElementById('modalCloseBtn');

            const isNew = data.newCustomer === true || data.isNewCustomer === true;
            if (isNew && data.newAccountUsername) {
                msg.innerHTML = "Hệ thống đã <b>tự động tạo hồ sơ</b> khách hàng mới và làm thủ tục nhận phòng thành công.";
                document.getElementById('modalUsername').innerText = data.newAccountUsername;
                document.getElementById('modalPassword').innerText = data.newAccountPassword;
                accInfo.style.display = 'block';
            } else {
                msg.innerText = "Đã làm thủ tục nhận phòng hoàn tất cho khách lưu trú này.";
                accInfo.style.display = 'none';
            }

            if (data.paymentUrl) {
                msg.innerHTML += "<br><br><span style='color:#e11d48;'><b>Lưu ý:</b> Vui lòng ghi lại thông tin tài khoản của khách (nếu có) trước khi ấn nút Thanh Toán bên dưới!</span>";
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
                    cancelBtn.style.backgroundColor = '#e11d48'; // Red color for cancel
                    cancelBtn.innerHTML = 'Hủy Check-in';
                    cancelBtn.onclick = function () {
                        cancelBtn.disabled = true;
                        cancelBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Đang hủy...';
                        fetch('/api/receptionist/walkin/cancel-pending/' + data.bookingId, {
                            method: 'POST'
                        })
                            .then(() => {
                                window.location.href = "/receptionist/dashboard";
                            })
                            .catch(err => {
                                alert("Lỗi khi hủy: " + err);
                                cancelBtn.disabled = false;
                                cancelBtn.innerHTML = 'Hủy Check-in';
                            });
                    };
                }
            } else {
                if (closeBtn) {
                    closeBtn.innerHTML = 'Đóng & Về danh sách Lưu trú';
                    closeBtn.onclick = function () {
                        window.location.href = "/receptionist/in-house";
                    };
                    let cancelBtn = document.getElementById('modalCancelBtn');
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
                    alert("Check-in thành công!");
                    window.location.href = "/receptionist/in-house";
                }
            }
        })
        .catch(error => {
            alert("Lỗi Check-in: " + error.message);
            completeBtn.innerHTML = '<i class="fa-solid fa-check-double"></i> Complete Walk-In Check-in';
            completeBtn.disabled = false;
        });
}

function closeModalAndRedirect() {
    document.getElementById('successModal').style.display = 'none';
    window.location.href = "/receptionist/dashboard";
}

let walkInDependents = [];

function renderAccompaniedGuests() {
    const list = document.getElementById('accompaniedGuestsList');
    list.innerHTML = '';

    if (walkInDependents.length === 0) {
        list.innerHTML = '<tr><td colspan="6" style="text-align:center; padding: 16px; color: #94a3b8; font-size: 13px;">Chưa có khách đi kèm</td></tr>';
        return;
    }

    walkInDependents.forEach((dep, index) => {
        const roleBadge = dep.isPrimaryContact ? `<span style="display:inline-block; padding: 2px 6px; background: #fef3c7; color: #d97706; border-radius: 4px; font-size: 11px; font-weight: 600;"><i class="fa-solid fa-star"></i> Đứng đầu</span>` : `<span style="font-size: 13px; color: #64748b;">Thành viên</span>`;
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
        if (walkInCart.length > 0 && roomId == walkInCart[0].roomId) {
            alert(`Phòng ${roomNum} đã được chỉ định cho khách chính đứng đầu! Vui lòng không chọn người đi kèm làm người đứng đầu cho phòng này.`);
            return;
        }

        // Kiểm tra xem phòng này đã có người đứng đầu chưa
        const conflict = walkInDependents.some(dep => dep.roomId === roomId && dep.isPrimaryContact);
        if (conflict) {
            alert(`Phòng ${roomNum} đã có người đứng đầu! Vui lòng chọn người khác hoặc bỏ chọn người đứng đầu cũ.`);
            return;
        }
    }

    const todayStr = new Date().toISOString().split('T')[0];
    if (dob > todayStr) {
        alert('Ngày sinh không được ở tương lai!');
        return;
    }

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

    // Clear form
    document.getElementById('depName').value = '';
    document.getElementById('depDob').value = '';
    document.getElementById('depId').value = '';
    document.getElementById('depGender').value = 'Nam';
    document.getElementById('depRoom').value = '';
    document.getElementById('depIsPrimary').checked = false;

    renderAccompaniedGuests();

    if (document.getElementById('step4Container').style.display === 'block') {
        showPaymentStep();
    }
}

function removeAccompaniedGuest(index) {
    walkInDependents.splice(index, 1);
    renderAccompaniedGuests();

    if (document.getElementById('step4Container').style.display === 'block') {
        showPaymentStep();
    }
}

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
