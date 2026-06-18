// Global Booking
let selectedRoomsCart = {};

// Cart Hold Timer — Frontend chỉ setTimeout để redirect user sau 10 phút.
// Việc giải phóng phòng thực sự do BACKEND Scheduler (cleanupStaleHolds) xử lý.
const CART_HOLD_MINUTES = 10;
let cartHoldTimer = null;                 // setTimeout ID
let cartHoldConfirmedBookingId = null;    // bookingId đã CONFIRMED để dùng khi hủy

const today = new Date();
const defaultCheckOut = new Date(today);
defaultCheckOut.setDate(defaultCheckOut.getDate() + 1);

// Global Booking State
let bookingState = {
    units: 1,
    checkIn: today,
    checkOut: defaultCheckOut,
    promoCode: '',
    currentCalendarMonth: today.getMonth(),
    currentCalendarYear: today.getFullYear()
};

const monthNamesShort = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
const monthNamesFull = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];

function formatDateString(date) {
    if (!date) return '';
    return `${date.getDate()} ${monthNamesShort[date.getMonth()]} ${date.getFullYear()}`;
}

// ---------------- GUEST DROPDOWN LOGIC ----------------
function toggleGuestsDropdown(e) {
    e.stopPropagation();
    closeAllDropdowns();
    document.getElementById('guestsDropdown').classList.toggle('hidden');
}

function updateGuestCount(type, val) {
    if (type === 'units') {
        bookingState.units = Math.max(1, bookingState.units + val);
        document.getElementById('unitsCount').innerText = bookingState.units;
    }
}

function applyGuests() {
    let summary = `${bookingState.units} Unit${bookingState.units > 1 ? 's' : ''}`;
    document.getElementById('guestSummaryValue').innerText = summary;
    closeAllDropdowns();
}

// ---------------- PROMO CODE LOGIC ----------------
function toggleCodeDropdown(e) {
    e.stopPropagation();
    closeAllDropdowns();
    document.getElementById('codeDropdown').classList.toggle('hidden');
}

function applyPromoCode() {
    const val = document.getElementById('promoCodeInput').value.trim();
    bookingState.promoCode = val;
    const label = document.getElementById('bookingCodeValue');
    if (val) {
        label.innerText = val;
        label.classList.remove('muted');
        label.classList.add('text-[#2c2a24]', 'font-semibold');
    } else {
        label.innerText = 'Enter code';
        label.classList.add('muted');
        label.classList.remove('text-[#2c2a24]', 'font-semibold');
    }
    closeAllDropdowns();
}

// ---------------- CALENDAR DROPDOWN LOGIC ----------------
function toggleCalendarDropdown(e) {
    e.stopPropagation();
    closeAllDropdowns();
    document.getElementById('calendarDropdown').classList.toggle('hidden');
    renderCalendar();
}

function renderCalendar() {
    const month = bookingState.currentCalendarMonth;
    const year = bookingState.currentCalendarYear;

    document.getElementById('currentMonthYear').innerText = `${monthNamesFull[month]} ${year}`;

    const firstDayIndex = new Date(year, month, 1).getDay();
    const lastDay = new Date(year, month + 1, 0).getDate();
    const prevLastDay = new Date(year, month, 0).getDate();

    let daysHtml = '';

    // Previous month empty days
    for (let i = firstDayIndex; i > 0; i--) {
        daysHtml += `<span class="py-2 text-gray-300 font-medium">${prevLastDay - i + 1}</span>`;
    }

    // Current month days
    for (let day = 1; day <= lastDay; day++) {
        const currentDate = new Date(year, month, day);
        let classes = "py-2 cursor-pointer rounded-full hover:bg-gray-100 transition-colors relative font-semibold text-center ";

        const isCheckIn = bookingState.checkIn && currentDate.getTime() === bookingState.checkIn.getTime();
        const isCheckOut = bookingState.checkOut && currentDate.getTime() === bookingState.checkOut.getTime();
        const isBetween = bookingState.checkIn && bookingState.checkOut && currentDate > bookingState.checkIn && currentDate < bookingState.checkOut;

        if (isCheckIn || isCheckOut) {
            classes += "bg-[#3a322b] text-white hover:bg-[#3a322b]";
        } else if (isBetween) {
            classes += "bg-[#eae4d8] text-[#3a322b]";
        } else {
            classes += "text-gray-700";
        }

        daysHtml += `<button type="button" class="${classes}" onclick="selectCalendarDate(${day})">${day}</button>`;
    }

    document.getElementById('calendarDays').innerHTML = daysHtml;
    updateDateSelectionSummary();
}

function selectCalendarDate(day) {
    const date = new Date(bookingState.currentCalendarYear, bookingState.currentCalendarMonth, day);

    if (!bookingState.checkIn || (bookingState.checkIn && bookingState.checkOut)) {
        bookingState.checkIn = date;
        bookingState.checkOut = null;
    } else if (bookingState.checkIn && !bookingState.checkOut) {
        if (date < bookingState.checkIn) {
            bookingState.checkIn = date;
        } else {
            bookingState.checkOut = date;
        }
    }
    renderCalendar();
}

function updateDateSelectionSummary() {
    const summaryLabel = document.getElementById('dateSelectionSummary');
    if (bookingState.checkIn && bookingState.checkOut) {
        const diffTime = Math.abs(bookingState.checkOut - bookingState.checkIn);
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
        summaryLabel.innerText = `${formatDateString(bookingState.checkIn)} - ${formatDateString(bookingState.checkOut)} (${diffDays} night${diffDays > 1 ? 's' : ''})`;
    } else if (bookingState.checkIn) {
        summaryLabel.innerText = `Check-in: ${formatDateString(bookingState.checkIn)} (Select check-out)`;
    } else {
        summaryLabel.innerText = 'Select dates';
    }
}

function prevMonth() {
    bookingState.currentCalendarMonth--;
    if (bookingState.currentCalendarMonth < 0) {
        bookingState.currentCalendarMonth = 11;
        bookingState.currentCalendarYear--;
    }
    renderCalendar();
}

// ---------------- EXPORT CALENDAR HELPER FUNCTIONS ----------------
function nextMonth() {
    bookingState.currentCalendarMonth++;
    if (bookingState.currentCalendarMonth > 11) {
        bookingState.currentCalendarMonth = 0;
        bookingState.currentCalendarYear++;
    }
    renderCalendar();
}

function applyDates() {
    if (bookingState.checkIn && bookingState.checkOut) {
        document.getElementById('checkInValue').innerText = formatDateString(bookingState.checkIn);
        document.getElementById('checkOutValue').innerText = formatDateString(bookingState.checkOut);
        closeAllDropdowns();
    }
}

function clearPromoCode() {
    document.getElementById('promoCodeInput').value = '';
    applyPromoCode();
}

function closeAllDropdowns() {
    document.getElementById('guestsDropdown').classList.add('hidden');
    document.getElementById('calendarDropdown').classList.add('hidden');
    document.getElementById('codeDropdown').classList.add('hidden');
}

// Close dropdowns on outside click
document.addEventListener('click', function (e) {
    if (!e.target.closest('.search-field') && !e.target.closest('.search-btn') && !e.target.closest('#guestsDropdown') && !e.target.closest('#calendarDropdown') && !e.target.closest('#codeDropdown')) {
        closeAllDropdowns();
    }
});

// Helper for formatting Date to local YYYY-MM-DD
function formatLocalDate(date) {
    if (!date) return '';
    const yyyy = date.getFullYear();
    const mm = String(date.getMonth() + 1).padStart(2, '0');
    const dd = String(date.getDate()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd}`;
}

function getRoomImage(categoryName) {
    if (!categoryName) return "https://images.unsplash.com/photo-1566665797739-1674de7a421a?w=800&q=80";
    const nameLower = categoryName.toLowerCase();
    if (nameLower.includes("nipa pool villa") || nameLower.includes("nipa")) {
        return "https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800&q=80";
    } else if (nameLower.includes("river pool villa") || nameLower.includes("river")) {
        return "https://images.unsplash.com/photo-1618773928121-c32242e63f39?w=800&q=80";
    } else if (nameLower.includes("wellness retreats") || nameLower.includes("wellness retreat") || nameLower.includes("wellness")) {
        return "https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=800&q=80";
    } else {
        return "https://images.unsplash.com/photo-1566665797739-1674de7a421a?w=800&q=80";
    }
}

function getRoomBadge(price) {
    if (price < 18000000) {
        return { text: "Giá Tốt Nhất", type: "price", icon: "&#127991;" };
    }
    if (price > 19000000) {
        return { text: "Gợi Ý", type: "tip", icon: "&#9728;" };
    }
    return null;
}

function renderRoomResults(roomsData) {
    const grid = document.querySelector('.room-grid');
    if (!grid) return;

    if (roomsData.length === 0) {
        grid.innerHTML = `
            <div class="col-span-full py-12 text-center text-gray-500 font-medium font-sans">
                Không tìm thấy phòng trống phù hợp với tiêu chí của quý khách.
            </div>
        `;
        return;
    }

    // Group rooms by category
    const groupedRooms = {};
    roomsData.forEach(room => {
        if (!groupedRooms[room.categoryName]) {
            groupedRooms[room.categoryName] = {
                categoryName: room.categoryName,
                pricePerNight: room.pricePerNight,
                capacity: room.capacity,
                description: room.description,
                beds: room.beds,
                size: room.size,
                view: room.view,
                amenities: room.amenities,
                baseAdults: room.baseAdults,
                baseChildren: room.baseChildren,
                maxAdults: room.maxAdults,
                maxChildren: room.maxChildren,
                extraAdultSurcharge: room.extraAdultSurcharge,
                extraChildSurcharge: room.extraChildSurcharge,
                roomNumbers: []
            };
        }
        groupedRooms[room.categoryName].roomNumbers.push(room.roomNumber);
    });

    const categoryList = Object.values(groupedRooms);

    // Calculate nights
    const diffTime = Math.abs(bookingState.checkOut - bookingState.checkIn);
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) || 1;

    let html = '';
    categoryList.forEach(room => {
        const priceVal = room.pricePerNight;
        const formattedPrice = formatCurrencyVND(priceVal);
        const totalVal = priceVal * diffDays;
        const formattedTotal = formatCurrencyVND(totalVal);

        const badge = getRoomBadge(priceVal);
        const badgeHtml = badge ? `
            <span class="room-badge ${badge.type === 'tip' ? 'badge-tip' : 'badge-price'}">
                <span class="badge-ic" aria-hidden="true">${badge.icon}</span>
                <span>${badge.text}</span>
            </span>
        ` : '';

        const roomImage = getRoomImage(room.categoryName);
        const availableCount = room.roomNumbers.length;

        // Generate quantity options
        let quantityOptions = '';
        for (let i = 1; i <= availableCount; i++) {
            quantityOptions += `<option value="${i}">${i} Phòng</option>`;
        }

        html += `
            <article class="room-card flex flex-col h-full">
                <!-- Image / carousel -->
                <div class="room-media relative">
                    ${badgeHtml}
                    <img class="room-img" src="${roomImage}" alt="${room.categoryName}"/>

                    <!-- CÒN X TRỐNG - Giữa ảnh -->
                    <div class="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 bg-black/50 text-white px-3 py-1.5 rounded-full text-[11px] font-bold tracking-widest backdrop-blur-md shadow-lg z-10 uppercase border border-white/20 whitespace-nowrap pointer-events-none">
                        Còn ${availableCount} trống
                    </div>

                    <button type="button" class="carousel-btn prev" aria-label="Previous photo">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M15 6l-6 6 6 6"/></svg>
                    </button>
                    <button type="button" class="carousel-btn next" aria-label="Next photo">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 6l6 6-6 6"/></svg>
                    </button>

                    <div class="media-foot">
                        <span class="media-ic" aria-hidden="true">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6"><path d="M4 11l8-6 8 6v8a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1z"/></svg>
                        </span>
                        <span class="media-bar"></span>
                    </div>
                </div>

                <!-- Body -->
                <div class="room-body flex flex-col flex-grow">
                    <div class="room-title-row">
                        <h2 class="room-name">${room.categoryName}</h2>
                    </div>

                    <ul class="room-specs">
                        <li>
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6"><circle cx="9" cy="8" r="3"/><path d="M3.5 19c0-3 2.6-5 5.5-5"/><circle cx="17" cy="9" r="2.2"/><path d="M15.5 18c0-2.2 1.7-3.5 3.5-3.5"/></svg>
                            <span>${room.capacity} Người / phòng</span>
                        </li>
                    </ul>

                    <div class="room-foot mt-auto pt-4 flex flex-col gap-4 border-t border-gray-100">
                        <div class="price-block w-full">
                            <p class="price-from">From</p>
                            <p class="price-main">
                                <span class="price-amount">${formattedPrice}</span>
                                <span class="price-unit">/đêm</span>
                            </p>
                        </div>
                        
                        <div class="booking-action-block w-full bg-white p-4 rounded-xl border border-gray-100 shadow-[0_2px_10px_rgba(0,0,0,0.03)] flex flex-col gap-4">
                            <div class="grid grid-cols-[1fr_1.5fr] gap-4">
                                <!-- Cột 1: Số lượng phòng -->
                                <div class="flex flex-col justify-end">
                                    <span class="text-[10px] text-gray-500 mb-1.5 font-bold uppercase tracking-widest">Phòng</span>
                                    <select class="room-quantity-select w-full border border-gray-200 rounded-lg p-2 text-sm bg-gray-50 text-[#3a322b] font-bold focus:outline-none focus:border-[#3a322b] focus:ring-1 focus:ring-[#3a322b] transition-all cursor-pointer" data-category="${room.categoryName}">
                                        ${quantityOptions}
                                    </select>
                                </div>
                                
                                <!-- Cột 2: Số khách / phòng -->
                                <div class="flex gap-2">
                                    <div class="flex flex-col flex-1 justify-end">
                                        <span class="text-[10px] text-gray-500 mb-1.5 font-bold uppercase tracking-widest text-center">Người lớn</span>
                                        <input type="number" min="1" max="${(room.maxAdults || 4) + 2}" value="${room.baseAdults || 2}" class="adults-input w-full border border-gray-200 rounded-lg p-2 text-sm bg-gray-50 text-[#3a322b] font-bold text-center focus:outline-none focus:border-[#3a322b] transition-all" />
                                    </div>
                                    <div class="flex flex-col flex-1 justify-end">
                                        <span class="text-[10px] text-gray-500 mb-1.5 font-bold uppercase tracking-widest text-center">Trẻ em</span>
                                        <input type="number" min="0" max="${(room.maxChildren || 2) + 2}" value="${room.baseChildren || 0}" class="children-input w-full border border-gray-200 rounded-lg p-2 text-sm bg-gray-50 text-[#3a322b] font-bold text-center focus:outline-none focus:border-[#3a322b] transition-all" />
                                    </div>
                                </div>
                            </div>
                            
                            <!-- Dưới cùng: Nút Select Room -->
                            <div class="w-full mt-1">
                                <button type="button" class="w-full bg-[#3a322b] text-[#f3efe6] px-4 py-2.5 rounded-md text-sm font-bold tracking-wider uppercase hover:bg-[#2c2620] transition-colors shadow-sm"
                                        data-room-numbers='${JSON.stringify(room.roomNumbers)}'
                                        data-category="${room.categoryName}"
                                        data-price="${room.pricePerNight}"
                                        data-base-adults="${room.baseAdults || 0}"
                                        data-base-children="${room.baseChildren || 0}"
                                        data-extra-adult="${room.extraAdultSurcharge || 0}"
                                        data-extra-child="${room.extraChildSurcharge || 0}"
                                        onclick="handleSelectRoomClick(this)">
                                    Select Room
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </article>
        `;
    });
    grid.innerHTML = html;
    initRoomsPagination();
}

// ---------------- SEARCH SIMULATION ----------------
function executeSearch(e) {
    if (e) e.stopPropagation();
    closeAllDropdowns();

    const grid = document.querySelector('.room-grid');
    if (!grid) return;

    grid.style.opacity = '0.3';
    grid.style.pointerEvents = 'none';

    const spinner = document.createElement('div');
    spinner.id = 'searchSpinner';
    spinner.className = 'fixed inset-0 z-50 flex items-center justify-center bg-black/10 backdrop-blur-[1px]';
    spinner.innerHTML = `
        <div class="bg-white p-6 rounded-xl shadow-xl flex items-center gap-3 font-sans">
            <svg class="animate-spin h-5 w-5 text-[#3a322b]" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            <span class="text-sm font-semibold text-[#2c2a24]">Searching for available options...</span>
        </div>
    `;
    document.body.appendChild(spinner);

    const checkInStr = formatLocalDate(bookingState.checkIn);
    const checkOutStr = formatLocalDate(bookingState.checkOut);
    const units = bookingState.units || 1;

    fetch(`/api/rooms/search?checkIn=${checkInStr}&checkOut=${checkOutStr}&minRooms=${units}`)
        .then(async response => {
            if (!response.ok) {
                let errorMsg = 'Có lỗi xảy ra khi tìm kiếm phòng trống!';
                try {
                    const errData = await response.json();
                    if (errData && errData.message) errorMsg = errData.message;
                } catch (e) { }
                throw new Error(errorMsg);
            }
            return response.json();
        })
        .then(data => {
            spinner.remove();
            grid.style.opacity = '1';
            grid.style.pointerEvents = 'auto';
            renderRoomResults(data);
        })
        .catch(err => {
            spinner.remove();
            grid.style.opacity = '1';
            grid.style.pointerEvents = 'auto';
            showToast(err.message, 'error');
            console.error(err);
        });
}

// ---------------- ROOM DETAIL MODAL LOGIC ----------------
function handleSelectRoomClick(button) {
    const name = button.getAttribute('data-name') || 'Room';
    const image = button.getAttribute('data-image');
    const badge = button.getAttribute('data-badge');
    const badgeType = button.getAttribute('data-badge-type');
    const guests = button.getAttribute('data-guests');
    const total = button.getAttribute('data-total');

    const desc = button.getAttribute('data-desc') || '';
    const beds = button.getAttribute('data-beds') || '';
    const size = button.getAttribute('data-size') || '';
    const view = button.getAttribute('data-view') || '';
    const amenitiesStr = button.getAttribute('data-amenities') || '';
    const amenities = amenitiesStr ? amenitiesStr.split(',') : [];

    document.getElementById('detailRoomName').innerText = name;
    document.getElementById('detailRoomImg').src = image;

    const badgeEl = document.getElementById('detailRoomBadge');
    if (badge && badge !== 'null' && badge !== '') {
        badgeEl.innerText = badge;
        badgeEl.style.display = 'inline-block';
        if (badgeType === 'tip') {
            badgeEl.className = 'inline-block px-3 py-1 rounded text-xs font-semibold text-white mb-3 bg-[#5a4837]';
        } else {
            badgeEl.className = 'inline-block px-3 py-1 rounded text-xs font-semibold text-white mb-3 bg-[#6e7869]';
        }
    } else {
        badgeEl.style.display = 'none';
    }

    document.getElementById('detailRoomGuests').innerText = `${guests} Guest${guests > 1 ? 's' : ''}`;
    document.getElementById('detailRoomTotal').innerText = total;

    document.getElementById('detailRoomDesc').innerText = desc;

    function isValEmpty(val) {
        return !val || val === 'null' || val === 0 || val === '0' || val === '' || val === 'undefined';
    }

    const bedsEl = document.getElementById('detailRoomBeds');
    if (bedsEl) {
        if (isValEmpty(beds)) {
            bedsEl.parentElement.style.display = 'none';
        } else {
            bedsEl.parentElement.style.display = 'flex';
            bedsEl.innerText = beds;
        }
    }

    const sizeEl = document.getElementById('detailRoomSize');
    if (sizeEl) {
        if (isValEmpty(size)) {
            sizeEl.parentElement.style.display = 'none';
        } else {
            sizeEl.parentElement.style.display = 'flex';
            sizeEl.innerText = size;
        }
    }

    const viewEl = document.getElementById('detailRoomView');
    if (viewEl) {
        if (isValEmpty(view)) {
            viewEl.parentElement.style.display = 'none';
        } else {
            viewEl.parentElement.style.display = 'flex';
            viewEl.innerText = view;
        }
    }

    if (bedsEl && bedsEl.parentElement && bedsEl.parentElement.parentElement) {
        const specsContainer = bedsEl.parentElement.parentElement;
        if (isValEmpty(beds) && isValEmpty(size) && isValEmpty(view)) {
            specsContainer.style.display = 'none';
        } else {
            specsContainer.style.display = 'grid';
        }
    }

    const amenitiesHeader = document.querySelector('#detailRoomAmenities') ? document.querySelector('#detailRoomAmenities').previousElementSibling : null;
    const amenitiesList = document.getElementById('detailRoomAmenities');
    if (amenitiesList) {
        if (!amenities || amenities.length === 0 || (amenities.length === 1 && isValEmpty(amenities[0]))) {
            amenitiesList.style.display = 'none';
            if (amenitiesHeader) amenitiesHeader.style.display = 'none';
        } else {
            amenitiesList.style.display = 'grid';
            if (amenitiesHeader) amenitiesHeader.style.display = 'block';
            amenitiesList.innerHTML = amenities.map(a => `
                <li class="flex items-center gap-1.5">
                    <span class="material-symbols-outlined text-[14px] text-green-600">check_circle</span>
                    <span>${a}</span>
                </li>
            `).join('');
        }
    }

    const container = document.getElementById('detailBookButtonContainer');
    if (typeof isUserLoggedIn !== 'undefined' && isUserLoggedIn) {
        container.innerHTML = `
            <button onclick="confirmBooking()" type="button" class="bg-[#3a322b] text-[#f3efe6] px-6 py-3 rounded-lg text-sm font-semibold hover:bg-[#2c2620] transition-colors">
                Book Now
            </button>
        `;
    } else {
        container.innerHTML = `
            <button onclick="triggerLoginFromDetail()" type="button" class="bg-[#3a322b] text-[#f3efe6] px-6 py-3 rounded-lg text-sm font-semibold hover:bg-[#2c2620] transition-colors">
                Book Now
            </button>
        `;
    }

    document.getElementById('roomDetailsModal').style.display = 'flex';
}

function closeRoomDetailsModal() {
    document.getElementById('roomDetailsModal').style.display = 'none';
}

function triggerLoginFromDetail() {
    closeRoomDetailsModal();
    openLoginModal();
}

/**
 * Validates check-in and check-out dates.
 * Returns null if valid, or an error message string if invalid.
 */
function validateBookingDates(checkIn, checkOut) {
    if (!checkIn || !checkOut) {
        return 'Vui lòng chọn ngày check-in và check-out trước khi đặt phòng.';
    }
    if (checkOut <= checkIn) {
        return 'Ngày check-out phải sau ngày check-in.';
    }
    return null;
}

// ---------------- CART LOGIC ----------------
function updateCartUI() {
    const cartWrapper = document.getElementById('bookingCartWrapper');
    const container = document.getElementById('cartItemsContainer');
    const cartDatesSummary = document.getElementById('cartDatesSummary');
    const cartTotalPrice = document.getElementById('cartTotalPrice');
    const btnCheckout = document.getElementById('btnCartCheckout');

    if (!cartWrapper) return;

    if (!bookingState.checkIn || !bookingState.checkOut) {
        cartDatesSummary.innerText = "Vui lòng chọn ngày";
    } else {
        cartDatesSummary.innerText = formatDateString(bookingState.checkIn) + " - " + formatDateString(bookingState.checkOut);
    }

    container.innerHTML = '';
    let totalCartPrice = 0;
    let totalItems = 0;

    for (const catName in selectedRoomsCart) {
        const data = selectedRoomsCart[catName];
        if (data.quantity > 0) {
            totalItems++;
            totalCartPrice += data.estimatedTotal; // ước tính phía client

            const itemHTML = `
                <div class="p-4 border-b border-[#e7e1d5] relative">
                    <div class="flex justify-between items-start mb-2">
                        <div class="pr-6">
                            <h4 class="font-semibold text-sm text-[#2f2a24] leading-tight">${catName}</h4>
                            <p class="text-xs text-[#8b8478] mt-1">${data.quantity} Phòng x ${formatCurrencyVND(data.pricePerNight)} VNĐ/đêm</p>
                            <p class="text-[10px] text-[#a59f93] mt-0.5">Khách: ${data.adultsPerRoom} NL, ${data.childrenPerRoom} TE / phòng</p>
                        </div>
                        <button type="button" class="text-gray-400 hover:text-red-500 absolute top-4 right-4" onclick="removeCartItem('${catName}')">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6L6 18M6 6l12 12"/></svg>
                        </button>
                    </div>
                    <div class="flex justify-between items-center mt-2">
                        <span class="text-[10px] text-gray-400 italic">Ước tính</span>
                        <span class="text-xs font-medium text-[#2f2a24]">${formatCurrencyVND(data.estimatedTotal)} VNĐ</span>
                    </div>
                </div>`
                ;
            container.insertAdjacentHTML('beforeend', itemHTML);
        }
    }

    if (totalItems === 0) {
        container.innerHTML = `
            <div class="p-6 text-center text-gray-400 text-sm italic">
                Chưa chọn phòng nào
            </div>
        `;
        cartTotalPrice.innerText = "0 VNĐ";
        btnCheckout.disabled = true;
        // Dừng timer khi giỏ trống
        stopCartHoldTimer();
    } else {
        cartTotalPrice.innerHTML = formatCurrencyVND(totalCartPrice) + " VNĐ <span class='text-[10px] text-gray-400 font-normal normal-case'>(ước tính)</span>";
        btnCheckout.disabled = false;
        cartWrapper.classList.remove('hidden');
    }
}

function removeCartItem(catName) {
    delete selectedRoomsCart[catName];
    updateCartUI();
}

// ── Cart Hold Timer ─────────────────────────────────────────────────────────
// Frontend chỉ dùng setTimeout một lần để redirect user sau 10 phút.
// Việc giải phóng phòng (HOLD → CANCELLED) do BACKEND Scheduler xử lý.

/**
 * Bắt đầu đếm 10 phút. Nếu timer đang chạy rồi → không reset.
 */
function startCartHoldTimer() {
    if (cartHoldTimer !== null) return;
    cartHoldTimer = setTimeout(() => {
        cartHoldTimer = null;
        clearCartAndGoHome(true);
    }, CART_HOLD_MINUTES * 60 * 1000);
}

/** Dừng timer (khi giỏ trống hoặc user hủy chủ động) */
function stopCartHoldTimer() {
    if (cartHoldTimer) {
        clearTimeout(cartHoldTimer);
        cartHoldTimer = null;
    }
}

/**
 * Xóa giỏ hàng và redirect về trang booking (home).
 * @param {boolean} isTimeout - true nếu hết timer, false nếu user chủ động hủy
 */
function clearCartAndGoHome(isTimeout = false) {
    stopCartHoldTimer();
    selectedRoomsCart = {};
    cartHoldConfirmedBookingId = null;

    if (isTimeout) {
        showToast('⏰ Đã hết 10 phút! Phòng đã được giải phóng. Vui lòng chọn lại.', 'error');
        setTimeout(() => { window.location.href = '/booking'; }, 2500);
    } else {
        window.location.href = '/booking';
    }
}

/**
 * Hủy giỏ hàng khi user nhấn nút Hủy trong Cart panel.
 * Nếu đã có booking CONFIRMED → gọi API hủy trước rồi mới về home.
 */
async function cancelCartHold() {
    if (!confirm('Bạn có chắc muốn hủy và xóa giỏ phòng không?')) return;

    if (cartHoldConfirmedBookingId) {
        // Gọi API hủy booking đã CONFIRMED
        try {
            await fetch(`/api/bookings/${cartHoldConfirmedBookingId}/cancel`, { method: 'POST' });
        } catch (e) { /* ignore */ }
    }

    clearCartAndGoHome(false);
}

function handleSelectRoomClick(button) {
    const categoryName = button.getAttribute('data-category');
    const pricePerNight = parseFloat(button.getAttribute('data-price'));
    const allRoomNumbers = JSON.parse(button.getAttribute('data-room-numbers'));
    const baseAdults = parseInt(button.getAttribute('data-base-adults') || '0', 10);
    const baseChildren = parseInt(button.getAttribute('data-base-children') || '0', 10);

    const bookingActionBlock = button.closest('.booking-action-block');
    const selectEl = bookingActionBlock.querySelector('.room-quantity-select');
    const adultsInput = bookingActionBlock.querySelector('.adults-input');
    const childrenInput = bookingActionBlock.querySelector('.children-input');

    const quantity = parseInt(selectEl.value, 10);
    const adultsPerRoom = adultsInput ? parseInt(adultsInput.value, 10) : baseAdults;
    const childrenPerRoom = childrenInput ? parseInt(childrenInput.value, 10) : baseChildren;

    if (quantity === 0) return;

    const checkIn = bookingState.checkIn;
    const checkOut = bookingState.checkOut;

    const validationError = validateBookingDates(checkIn, checkOut);
    if (validationError) {
        showToast(validationError, 'error');
        return;
    }

    const roomNumbers = allRoomNumbers.slice(0, quantity);
    const diffTime = Math.abs(checkOut - checkIn);
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) || 1;

    const estimatedBaseTotal = pricePerNight * quantity * diffDays;

    selectedRoomsCart[categoryName] = {
        quantity,
        pricePerNight,
        adultsPerRoom,
        childrenPerRoom,
        roomNumbers,
        // Chỉ lưu ước tính để hiển thị trước khi backend xác nhận
        estimatedTotal: estimatedBaseTotal,
        diffDays
    };
    startCartHoldTimer();
    updateCartUI();
    const cartWrapper = document.getElementById('bookingCartWrapper');
    if (cartWrapper) {
        cartWrapper.classList.remove('hidden');
        cartWrapper.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }

    showToast(`Đã thêm ${quantity} phòng ${categoryName} vào giỏ.`, 'success');
}

function confirmCartBooking() {
    if (typeof isUserLoggedIn !== 'undefined' && !isUserLoggedIn) {
        showToast('Vui lòng đăng nhập để tiếp tục đặt phòng.', 'error');
        if (typeof openLoginModal === 'function') openLoginModal();
        return;
    }

    const btnCheckout = document.getElementById('btnCartCheckout');
    btnCheckout.disabled = true;
    const originalText = btnCheckout.innerText;
    btnCheckout.innerText = "ĐANG XỬ LÝ...";

    let roomSelections = [];
    let totalRoomsCount = 0;
    for (const catName in selectedRoomsCart) {
        const item = selectedRoomsCart[catName];
        totalRoomsCount += item.roomNumbers.length;
        item.roomNumbers.forEach(roomNo => {
            roomSelections.push({
                categoryName: catName,
                roomNumber: roomNo,
                numberOfAdults: item.adultsPerRoom,
                numberOfChildren: item.childrenPerRoom
            });
        });
    }

    if (roomSelections.length === 0) {
        showToast('Chưa chọn phòng nào!', 'error');
        btnCheckout.disabled = false;
        btnCheckout.innerText = originalText;
        return;
    }

    const checkIn = bookingState.checkIn;
    const checkOut = bookingState.checkOut;

    const payload = {
        roomSelections: roomSelections,
        checkInDate: formatLocalDate(checkIn),
        checkOutDate: formatLocalDate(checkOut),
        promotionCode: bookingState.promoCode || null
    };

    fetch('/api/bookings', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    })
        .then(async response => {
            if (!response.ok) {
                let errorMsg = 'Có lỗi xảy ra trong quá trình đặt phòng.';
                try {
                    const errData = await response.json();
                    if (errData && errData.message) errorMsg = errData.message;
                } catch (e) { }
                throw new Error(errorMsg);
            }
            return response.json();
        })
        .then(data => {
            btnCheckout.innerText = originalText;
            btnCheckout.disabled = false;

            const bookingStatus = data.bookingStatus || data.status;
            if (bookingStatus === 'CONFIRMED' || bookingStatus === 'PENDING' || bookingStatus === 'HOLD') {
                // Lưu bookingId để dùng nếu user huỷ giỏ
                if (data.bookingId) cartHoldConfirmedBookingId = data.bookingId;

                showToast('Đặt phòng thành công! Đang chuyển đến trang thanh toán...', 'success');

                // ✅ Redirect sang trang thanh toán với bookingId
                setTimeout(() => {
                    window.location.href = `/payment?bookingId=${data.bookingId}`;
                }, 1200);

            } else {
                showToast('Lỗi đặt phòng: ' + (data.message || 'Phòng đã được đặt hoặc không khả dụng!'), 'error');
            }
        })
        .catch(err => {
            btnCheckout.innerText = originalText;
            btnCheckout.disabled = false;
            console.error(err);
            showToast(err.message || 'Lỗi kết nối Server! Vui lòng thử lại.', 'error');
        });
}


function closeBookingConfirmModal() {
    document.getElementById('bookingConfirmModal').style.display = 'none';
}

function goToProfile() {
    window.location.href = '/profile';
}

// Close modal when clicking outside
const confirmModal = document.getElementById('bookingConfirmModal');
if (confirmModal) {
    confirmModal.addEventListener('click', function (e) {
        if (e.target.id === 'bookingConfirmModal') {
            closeBookingConfirmModal();
        }
    });
}

// ---------------- AUTH MODAL LOGIC ----------------
function openLoginModal() {
    document.getElementById('authModal').style.display = 'flex';
    toggleAuthView('login');
}
function closeAuthModal() {
    document.getElementById('authModal').style.display = 'none';
}
function toggleAuthView(view) {
    if (view === 'login') {
        document.getElementById('loginSection').style.display = 'block';
        document.getElementById('registerSection').style.display = 'none';
    } else {
        document.getElementById('loginSection').style.display = 'none';
        document.getElementById('registerSection').style.display = 'block';
    }
}
const authModalEl = document.getElementById('authModal');
if (authModalEl) {
    authModalEl.addEventListener('click', function (e) {
        if (e.target.id === 'authModal') {
            closeAuthModal();
        }
    });
}

let showAllRooms = false;
function initRoomsPagination() {
    const cards = document.querySelectorAll('.room-grid .room-card');
    const showMoreContainer = document.getElementById('showMoreContainer');
    const showMoreBtn = document.getElementById('showMoreBtn');

    if (!showMoreContainer || !showMoreBtn) return;

    if (cards.length <= 9) {
        showMoreContainer.style.display = 'none';
        cards.forEach(card => card.classList.remove('hidden'));
    } else {
        showMoreContainer.style.display = 'flex';
        showAllRooms = false;
        cards.forEach((card, index) => {
            if (index >= 9) {
                card.classList.add('hidden');
            } else {
                card.classList.remove('hidden');
            }
        });
        showMoreBtn.innerHTML = `
            Show more
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M6 9l6 6 6-6"/></svg>
        `;
    }
}

function toggleShowMoreRooms() {
    const cards = document.querySelectorAll('.room-grid .room-card');
    const showMoreBtn = document.getElementById('showMoreBtn');
    if (!showMoreBtn) return;

    showAllRooms = !showAllRooms;

    cards.forEach((card, index) => {
        if (index >= 9) {
            if (showAllRooms) {
                card.classList.remove('hidden');
            } else {
                card.classList.add('hidden');
            }
        }
    });

    if (showAllRooms) {
        showMoreBtn.innerHTML = `
            Show less
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="transform: rotate(180deg)"><path d="M6 9l6 6 6-6"/></svg>
        `;
    } else {
        showMoreBtn.innerHTML = `
            Show more
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M6 9l6 6 6-6"/></svg>
        `;
    }
}

// -- DOM Initializations --
document.addEventListener("DOMContentLoaded", function () {
    const hasError = document.querySelector('.bg-red-100');
    const hasSuccess = document.querySelector('.bg-green-100');
    const urlParams = new URLSearchParams(window.location.search);
    if (hasError || hasSuccess || urlParams.get('login') === 'true') {
        openLoginModal();
    }

    // Check if user just logged in to show welcome toast
    if (typeof isUserLoggedIn !== 'undefined' && isUserLoggedIn) {
        if (!sessionStorage.getItem('loginToastShown')) {
            showToast("Chào mừng quay trở lại! Bạn đã đăng nhập thành công.", "success");
            sessionStorage.setItem('loginToastShown', 'true');
        }
    } else {
        sessionStorage.removeItem('loginToastShown');
    }

    // Initialize rooms pagination
    initRoomsPagination();

    // Automatically load dynamic grouped rooms on page load
    setTimeout(() => {
        applyDates();
        executeSearch({ preventDefault: () => { }, stopPropagation: () => { } });
    }, 100);
});

// ── Intersection Observer reveal animations ───────────────────────────────
document.addEventListener("DOMContentLoaded", () => {
    const observerOptions = {
        root: null,
        rootMargin: '0px',
        threshold: 0.15
    };

    const observer = new IntersectionObserver((entries, observer) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('opacity-100', 'translate-y-0');
                entry.target.classList.remove('opacity-0', 'translate-y-8');
                observer.unobserve(entry.target);
            }
        });
    }, observerOptions);

    const sections = document.querySelectorAll('main section, .feature-row, .room-grid, .room-card, .results, .site-footer');
    sections.forEach(section => {
        section.classList.add('transition-all', 'duration-[800ms]', 'ease-out', 'opacity-0', 'translate-y-8', 'will-change-transform');
        observer.observe(section);
    });

    const subNav = document.querySelector('.fixed.top-\\[70px\\]');
    if (subNav) {
        subNav.style.opacity = '0';
        subNav.style.transform = 'translateY(-10px)';
        subNav.style.transition = 'all 0.6s ease-out';
        setTimeout(() => {
            subNav.style.opacity = '1';
            subNav.style.transform = 'translateY(0)';
        }, 300);
    }
});
