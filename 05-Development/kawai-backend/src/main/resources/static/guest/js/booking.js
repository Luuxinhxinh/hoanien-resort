// ============================================================================
// 1. CONSTANTS & CONFIG
// ============================================================================
const CONFIG = {
    monthsShort: ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"],
    monthsFull: ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"],
    icons: {
        check: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"></polyline></svg>',
        star: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon></svg>',
        capacity: '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6"><circle cx="9" cy="8" r="3"/><path d="M3.5 19c0-3 2.6-5 5.5-5"/><circle cx="17" cy="9" r="2.2"/><path d="M15.5 18c0-2.2 1.7-3.5 3.5-3.5"/></svg>',
        arrowDown: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M6 9l6 6 6-6"/></svg>',
        arrowUp: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="transform: rotate(180deg)"><path d="M6 9l6 6 6-6"/></svg>',
        mediaIcon: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6"><path d="M4 11l8-6 8 6v8a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1z"/></svg>',
        deleteIcon: '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6L6 18M6 6l12 12"/></svg>'
    },
    defaultImages: [
        "https://images.unsplash.com/photo-1566665797739-1674de7a421a?w=800&q=80",
        "https://images.unsplash.com/photo-1596394516093-501ba68a0ba6?w=800&q=80",
        "https://images.unsplash.com/photo-1571501443899-2a9c3722a8a8?w=800&q=80"
    ]
};

// ============================================================================
// 2. STATE MANAGEMENT
// ============================================================================
const today = new Date();
const defaultCheckOut = new Date(today);
defaultCheckOut.setDate(defaultCheckOut.getDate() + 1);

let bookingState = {
    units: 1,
    checkIn: today,
    checkOut: defaultCheckOut,
    priceFilter: 'all',
    currentCalendarMonth: today.getMonth(),
    currentCalendarYear: today.getFullYear()
};

let selectedRoomsCart = {};
let showAllRooms = false;

// Modal Room Gallery State
let currentRoomImages = [];
let currentRoomImageIndex = 0;

function saveCartToStorage() {
    sessionStorage.setItem('kawai_cart', JSON.stringify(selectedRoomsCart));
    sessionStorage.setItem('kawai_bookingState', JSON.stringify(bookingState));
}

function restoreCartFromStorage() {
    const savedCart = sessionStorage.getItem('kawai_cart');
    const savedState = sessionStorage.getItem('kawai_bookingState');
    if (savedCart) {
        selectedRoomsCart = JSON.parse(savedCart);
    }
    if (savedState) {
        const parsedState = JSON.parse(savedState);
        bookingState = parsedState;
        if (bookingState.checkIn) bookingState.checkIn = new Date(bookingState.checkIn);
        if (bookingState.checkOut) bookingState.checkOut = new Date(bookingState.checkOut);
    }
}

// ============================================================================
// 3. UTILITY HELPER FUNCTIONS
// ============================================================================
function formatDateString(date) {
    if (!date) return '';
    return `${date.getDate()} ${CONFIG.monthsShort[date.getMonth()]} ${date.getFullYear()}`;
}

function formatLocalDate(date) {
    if (!date) return '';
    const yyyy = date.getFullYear();
    const mm = String(date.getMonth() + 1).padStart(2, '0');
    const dd = String(date.getDate()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd}`;
}



function validateBookingDates(checkIn, checkOut) {
    if (!checkIn || !checkOut) return 'Vui lòng chọn ngày check-in và check-out trước khi đặt phòng.';
    if (checkOut <= checkIn) return 'Ngày check-out phải sau ngày check-in.';
    return null;
}

// ============================================================================
// 4. UI RENDERERS
// ============================================================================
// --- 4.1 Dropdowns & Filters ---
function closeAllDropdowns() {
    const ids = ['guestsDropdown', 'calendarDropdown', 'priceDropdown'];
    ids.forEach(id => {
        const el = document.getElementById(id);
        if (el) el.classList.add('hidden');
    });
}

function toggleDropdown(e, id) {
    e.stopPropagation();
    const isHidden = document.getElementById(id).classList.contains('hidden');
    closeAllDropdowns();
    if (isHidden) {
        document.getElementById(id).classList.remove('hidden');
    }
}

// Exposed UI functions
function toggleGuestsDropdown(e) { toggleDropdown(e, 'guestsDropdown'); }
function togglePriceDropdown(e) { toggleDropdown(e, 'priceDropdown'); }
function toggleCalendarDropdown(e) {
    toggleDropdown(e, 'calendarDropdown');
    renderCalendar();
}

function updateGuestCount(type, val) {
    if (type === 'units') {
        bookingState.units = Math.max(1, bookingState.units + val);
        document.getElementById('unitsCount').innerText = bookingState.units;
    }
}

function applyGuests() {
    document.getElementById('guestSummaryValue').innerText = `${bookingState.units} Unit${bookingState.units > 1 ? 's' : ''}`;
    closeAllDropdowns();
}

function applyPriceFilter() {
    const selectedRadio = document.querySelector('input[name="priceFilter"]:checked');
    if (!selectedRadio) return;

    bookingState.priceFilter = selectedRadio.value;
    const label = document.getElementById('priceFilterValue');
    label.classList.remove('muted', 'text-[#2c2a24]', 'font-semibold');

    const labelsMap = {
        'under_5': 'Dưới 5tr',
        '5_to_10': '5tr - 10tr',
        'over_10': 'Trên 10tr'
    };

    if (labelsMap[selectedRadio.value]) {
        label.innerText = labelsMap[selectedRadio.value];
        label.classList.add('text-[#2c2a24]', 'font-semibold');
    } else {
        label.innerText = 'Tất cả';
        label.classList.add('muted');
    }
}

function applyDates() {
    if (bookingState.checkIn && bookingState.checkOut) {
        document.getElementById('checkInValue').innerText = formatDateString(bookingState.checkIn);
        document.getElementById('checkOutValue').innerText = formatDateString(bookingState.checkOut);
        closeAllDropdowns();
    }
}

// --- 4.2 Calendar Render ---
function renderCalendar() {
    const month = bookingState.currentCalendarMonth;
    const year = bookingState.currentCalendarYear;
    document.getElementById('currentMonthYear').innerText = `${CONFIG.monthsFull[month]} ${year}`;

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

function nextMonth() {
    bookingState.currentCalendarMonth++;
    if (bookingState.currentCalendarMonth > 11) {
        bookingState.currentCalendarMonth = 0;
        bookingState.currentCalendarYear++;
    }
    renderCalendar();
}

// --- 4.3 Search Results Render ---
function renderChildAgesHTML(count, existingValues = []) {
    let html = '';
    for (let c = 0; c < count; c++) {
        let prevVal = existingValues[c] !== undefined ? existingValues[c] : 0;
        const optionsHtml = Array.from({ length: 18 }, (_, i) => `<option value="${i}" ${i == prevVal ? 'selected' : ''}>${i} tuổi</option>`).join('');

        html += `
            <div class="flex flex-col flex-1 min-w-[60px]">
                <span class="text-[9px] text-gray-400 mb-1 uppercase tracking-widest text-center">Tuổi TE ${c + 1}</span>
                <select class="child-age-select w-full border border-gray-200 rounded-md p-1.5 text-xs bg-gray-50 text-[#3a322b] font-medium text-center focus:outline-none focus:border-[#3a322b]">
                    ${optionsHtml}
                </select>
            </div>
        `;
    }
    return html;
}

function clampGuestInput(inputElem) {
    if (!inputElem) return 0;
    if (inputElem.value === '') return 0;

    let val = parseInt(inputElem.value, 10);
    const min = parseInt(inputElem.getAttribute('min') || 0, 10);
    const max = parseInt(inputElem.getAttribute('max') || 10, 10);

    if (isNaN(val)) val = min;
    if (val < min) val = min;
    if (val > max) val = max;

    if (inputElem.value !== val.toString()) {
        inputElem.value = val;
    }
    return val;
}

function renderChildAges(inputElem) {
    if (!inputElem) return;

    // Nếu rỗng thì coi như 0 nhưng không ép value về 0 ngay để dễ gõ
    let count = 0;
    if (inputElem.value !== '') {
        count = clampGuestInput(inputElem);
    }

    const container = inputElem.closest('.booking-action-block').querySelector('.children-ages-container');
    const existingSelects = Array.from(container.querySelectorAll('.child-age-select')).map(s => s.value);
    container.innerHTML = renderChildAgesHTML(count, existingSelects);
}

function createRoomCardHTML(room, availableCount) {
    const formattedPrice = formatCurrency(room.pricePerNight);
    const roomImages = room.coverImgUrl ? room.coverImgUrl.split(',') : CONFIG.defaultImages;
    const roomImage = roomImages[0] || CONFIG.defaultImages[0];

    let quantityOptions = '';
    for (let i = 1; i <= availableCount; i++) {
        quantityOptions += `<option value="${i}">${i} Phòng</option>`;
    }

    const initialChildren = room.baseChildren || 0;
    const childrenAgesHtml = initialChildren > 0 ? renderChildAgesHTML(initialChildren) : '';
    const roomJson = encodeURIComponent(JSON.stringify(room));

    return `
        <article class="room-card flex flex-col h-full">
            <div class="room-media relative cursor-pointer hover:opacity-90 transition-opacity" onclick="openRoomInfoModal('${roomJson}')">
                <img class="room-img" src="${roomImage}" alt="${room.categoryName}"/>
                <div class="absolute top-3 left-3 bg-black/60 text-white px-3 py-1.5 rounded-md text-[11px] font-bold tracking-wider backdrop-blur-sm z-10 pointer-events-none">
                    CÒN ${availableCount} PHÒNG
                </div>
                <div class="media-foot">
                    <span class="media-ic" aria-hidden="true">${CONFIG.icons.mediaIcon}</span>
                    <span class="media-bar"></span>
                </div>
            </div>
            <div class="room-body flex flex-col flex-grow">
                <div class="room-title-row">
                    <h2 class="room-name">${room.categoryName}</h2>
                </div>
                <ul class="room-specs">
                    <li>
                        ${CONFIG.icons.capacity}
                        <span>Tiêu chuẩn: ${room.baseAdults || 2} Người | Tối đa: ${room.maxAdults || room.capacity} Người</span>
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
                            <div class="flex flex-col justify-end">
                                <span class="text-[10px] text-gray-500 mb-1.5 font-bold uppercase tracking-widest">Phòng</span>
                                <select class="room-quantity-select w-full border border-gray-200 rounded-lg p-2 text-sm bg-gray-50 text-[#3a322b] font-bold focus:outline-none focus:border-[#3a322b] focus:ring-1 focus:ring-[#3a322b] transition-all cursor-pointer" data-category="${room.categoryName}">
                                    ${quantityOptions}
                                </select>
                            </div>
                            <div class="flex gap-2">
                                <div class="flex flex-col flex-1 justify-end">
                                    <span class="text-[10px] text-gray-500 mb-1.5 font-bold uppercase tracking-widest text-center">Người lớn</span>
                                    <input type="number" min="1" max="${room.maxAdults || 4}" value="${room.baseAdults || 2}" class="adults-input w-full border border-gray-200 rounded-lg p-2 text-sm bg-gray-50 text-[#3a322b] font-bold text-center focus:outline-none focus:border-[#3a322b] transition-all" oninput="clampGuestInput(this)" onblur="if(this.value==='') this.value=this.min" />
                                </div>
                                <div class="flex flex-col flex-1 justify-end">
                                    <span class="text-[10px] text-gray-500 mb-1.5 font-bold uppercase tracking-widest text-center">Trẻ em</span>
                                    <input type="number" min="0" max="${room.maxChildren || 2}" value="${room.baseChildren || 0}" class="children-input w-full border border-gray-200 rounded-lg p-2 text-sm bg-gray-50 text-[#3a322b] font-bold text-center focus:outline-none focus:border-[#3a322b] transition-all" oninput="renderChildAges(this)" onblur="if(this.value==='') {this.value=this.min; renderChildAges(this);}" />
                                </div>
                            </div>
                        </div>
                        <div class="children-ages-container flex gap-2 flex-wrap mt-1">
                            ${childrenAgesHtml}
                        </div>
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
}

function groupRoomsByCategory(roomsData) {
    const grouped = {};
    roomsData.forEach(room => {
        if (!grouped[room.categoryName]) {
            grouped[room.categoryName] = { ...room, roomNumbers: [] };
        }
        grouped[room.categoryName].roomNumbers.push(room.roomNumber);
    });
    return Object.values(grouped);
}

function renderRoomResults(roomsData) {
    const grid = document.querySelector('.room-grid');
    if (!grid) return;

    if (roomsData.length === 0) {
        grid.innerHTML = `
            <div class="col-span-full py-12 text-center text-gray-500 font-medium font-sans">
                Không tìm thấy phòng trống phù hợp với tiêu chí của quý khách.
            </div>`;
        return;
    }

    const categoryList = groupRoomsByCategory(roomsData);
    let html = '';
    categoryList.forEach(room => {
        html += createRoomCardHTML(room, room.roomNumbers.length);
    });

    grid.innerHTML = html;
    initRoomsPagination();

    if (typeof scrollObserver !== 'undefined') {
        grid.querySelectorAll('.room-card').forEach(section => {
            section.classList.add('transition-all', 'duration-[800ms]', 'ease-out', 'opacity-0', 'translate-y-8', 'will-change-transform');
            scrollObserver.observe(section);
        });
    }
}

// --- 4.4 Pagination Render ---
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
            if (index >= 9) card.classList.add('hidden');
            else card.classList.remove('hidden');
        });
        showMoreBtn.innerHTML = `Show more ${CONFIG.icons.arrowDown}`;
    }
}

function toggleShowMoreRooms() {
    const cards = document.querySelectorAll('.room-grid .room-card');
    const showMoreBtn = document.getElementById('showMoreBtn');
    if (!showMoreBtn) return;

    showAllRooms = !showAllRooms;
    cards.forEach((card, index) => {
        if (index >= 9) {
            showAllRooms ? card.classList.remove('hidden') : card.classList.add('hidden');
        }
    });

    showMoreBtn.innerHTML = showAllRooms
        ? `Show less ${CONFIG.icons.arrowUp}`
        : `Show more ${CONFIG.icons.arrowDown}`;
}

// --- 4.5 Room Detail Modal ---
function prevRoomDetailImage(e) {
    if (e) e.stopPropagation();
    if (currentRoomImages.length <= 1) return;
    document.getElementById(`gallery-img-${currentRoomImageIndex}`).classList.replace('opacity-100', 'opacity-0');
    currentRoomImageIndex = (currentRoomImageIndex - 1 + currentRoomImages.length) % currentRoomImages.length;
    document.getElementById(`gallery-img-${currentRoomImageIndex}`).classList.replace('opacity-0', 'opacity-100');
}

function nextRoomDetailImage(e) {
    if (e) e.stopPropagation();
    if (currentRoomImages.length <= 1) return;
    document.getElementById(`gallery-img-${currentRoomImageIndex}`).classList.replace('opacity-100', 'opacity-0');
    currentRoomImageIndex = (currentRoomImageIndex + 1) % currentRoomImages.length;
    document.getElementById(`gallery-img-${currentRoomImageIndex}`).classList.replace('opacity-0', 'opacity-100');
}

function openRoomInfoModal(roomJsonStr) {
    const room = JSON.parse(decodeURIComponent(roomJsonStr));
    currentRoomImages = room.coverImgUrl ? room.coverImgUrl.split(',') : CONFIG.defaultImages;

    currentRoomImageIndex = 0;
    document.getElementById('detailRoomName').innerText = room.categoryName;

    const gallery = document.getElementById('detailRoomImageGallery');
    if (gallery) {
        gallery.innerHTML = '';
        currentRoomImages.forEach((src, idx) => {
            const img = document.createElement('img');
            img.src = src;
            img.id = `gallery-img-${idx}`;
            img.className = `absolute inset-0 w-full h-full object-cover transition-opacity duration-500 ${idx === 0 ? 'opacity-100' : 'opacity-0'}`;
            gallery.appendChild(img);
        });
    }

    const prevBtn = document.getElementById('prevRoomImageBtn');
    const nextBtn = document.getElementById('nextRoomImageBtn');
    if (currentRoomImages.length > 1) {
        prevBtn?.classList.remove('hidden');
        nextBtn?.classList.remove('hidden');
    } else {
        prevBtn?.classList.add('hidden');
        nextBtn?.classList.add('hidden');
    }

    document.getElementById('detailRoomGuests').innerText = room.capacity + ' Người';
    document.getElementById('detailRoomTotal').innerText = 'Trống ' + room.availableCount + ' phòng';
    document.getElementById('detailRoomDesc').innerText = room.description || 'Không có mô tả chi tiết cho phòng này.';
    document.getElementById('detailRoomBaseAdults').innerText = room.baseAdults || 0;
    document.getElementById('detailRoomBaseChildren').innerText = room.baseChildren || 0;
    document.getElementById('detailRoomMaxAdults').innerText = room.maxAdults || 0;
    document.getElementById('detailRoomMaxChildren').innerText = room.maxChildren || 0;
    document.getElementById('detailRoomSurchargeAdult').innerText = formatCurrency(room.extraAdultSurcharge || 0) + ' VNĐ';
    document.getElementById('detailRoomSurchargeChild').innerText = 'Theo độ tuổi';

    const safeSetText = (id, text) => { const el = document.getElementById(id); if (el) el.innerText = text; };
    safeSetText('detailRoomBedType', room.bedType || 'Tiêu chuẩn');
    safeSetText('detailRoomSize', room.roomSize ? room.roomSize + ' m²' : 'Tiêu chuẩn');
    safeSetText('detailRoomView', room.viewType || 'Không rõ');

    const amenitiesListEl = document.getElementById('detailRoomAmenitiesList');
    if (amenitiesListEl) {
        let amenitiesHtml = '';
        const addAmenity = (text, iconSvg) => {
            amenitiesHtml += `
                <li class="flex items-center gap-1.5 bg-[#d4af37]/10 text-[#a38015] px-3 py-1.5 rounded-full font-medium border border-[#d4af37]/20">
                    <span class="w-3.5 h-3.5 flex items-center justify-center">${iconSvg}</span>
                    <span>${text}</span>
                </li>`;
        };
        if (room.hasBathtub) addAmenity('Bồn tắm', CONFIG.icons.check);
        if (room.hasBalcony) addAmenity('Ban công', CONFIG.icons.check);
        if (room.hasFreeBreakfast) addAmenity('Bao gồm bữa sáng', CONFIG.icons.star);
        if (room.complimentaryServices) {
            room.complimentaryServices.split(',').forEach(s => {
                if (s.trim()) addAmenity(s.trim(), CONFIG.icons.star);
            });
        }
        amenitiesListEl.innerHTML = amenitiesHtml;
    }

    const container = document.getElementById('detailBookButtonContainer');
    if (container) {
        container.innerHTML = `<button onclick="closeRoomDetailsModal()" type="button" class="bg-[#2c2a24] text-white px-8 py-3 rounded-full text-xs font-bold tracking-widest uppercase hover:bg-black transition-all hover:shadow-lg hover:-translate-y-0.5 shadow-md">Đóng</button>`;
    }

    const modal = document.getElementById('roomDetailsModal');
    const modalContent = document.getElementById('roomDetailsModalContent');
    modal.style.display = 'flex';
    void modal.offsetWidth;
    modal.classList.remove('opacity-0');
    modalContent.classList.remove('scale-95', 'opacity-0');
    modalContent.classList.add('scale-100', 'opacity-100');
}

function closeRoomDetailsModal() {
    const modal = document.getElementById('roomDetailsModal');
    const modalContent = document.getElementById('roomDetailsModalContent');
    modal.classList.add('opacity-0');
    modalContent.classList.remove('scale-100', 'opacity-100');
    modalContent.classList.add('scale-95', 'opacity-0');
    setTimeout(() => { modal.style.display = 'none'; }, 500);
}

// --- 4.6 Cart UI ---
function updateCartUI() {
    const cartWrapper = document.getElementById('bookingCartWrapper');
    const container = document.getElementById('cartItemsContainer');
    const cartDatesSummary = document.getElementById('cartDatesSummary');
    const cartTotalPrice = document.getElementById('cartTotalPrice');
    const btnCheckout = document.getElementById('btnCreateCart');

    if (!cartWrapper) return;

    cartDatesSummary.innerText = (!bookingState.checkIn || !bookingState.checkOut)
        ? "Vui lòng chọn ngày"
        : `${formatDateString(bookingState.checkIn)} - ${formatDateString(bookingState.checkOut)}`;

    container.innerHTML = '';
    let totalCartPrice = 0;
    let totalItems = 0;

    for (const catName in selectedRoomsCart) {
        const data = selectedRoomsCart[catName];
        if (data.quantity > 0) {
            totalItems++;
            totalCartPrice += data.estimatedTotal;
            container.insertAdjacentHTML('beforeend', `
                <div class="p-4 border-b border-[#e7e1d5] relative">
                    <div class="flex justify-between items-start mb-2">
                        <div class="pr-6">
                            <h4 class="font-semibold text-sm text-[#2f2a24] leading-tight">${catName}</h4>
                            <p class="text-xs text-[#8b8478] mt-1">${data.quantity} Phòng x ${formatCurrency(data.pricePerNight)} VNĐ/đêm</p>
                            <p class="text-[10px] text-[#a59f93] mt-0.5">Khách: ${data.adultsPerRoom} NL, ${data.childrenPerRoom} TE / phòng</p>
                        </div>
                        <button type="button" class="text-gray-400 hover:text-red-500 absolute top-4 right-4" onclick="removeCartItem('${catName}')">
                            ${CONFIG.icons.deleteIcon}
                        </button>
                    </div>
                    <div class="flex justify-between items-center mt-2">
                        <span class="text-[10px] text-gray-400 italic">Ước tính</span>
                        <span class="text-xs font-medium text-[#2f2a24]">${formatCurrency(data.estimatedTotal)} VNĐ</span>
                    </div>
                </div>`);
        }
    }

    if (totalItems === 0) {
        container.innerHTML = `<div class="p-6 text-center text-gray-400 text-sm italic">Chưa chọn phòng nào</div>`;
        cartTotalPrice.innerText = '0 VNĐ';
        btnCheckout.disabled = true;
        cartWrapper.classList.add('hidden');
    } else {
        cartTotalPrice.innerHTML = `${formatCurrency(totalCartPrice)} VNĐ <span class='text-[10px] text-gray-400 font-normal normal-case'>(ước tính)</span>`;
        btnCheckout.disabled = false;
        cartWrapper.classList.remove('hidden');
    }
}

function removeCartItem(catName) {
    delete selectedRoomsCart[catName];
    updateCartUI();
    saveCartToStorage();
}

function clearCartAndGoHome() {
    selectedRoomsCart = {};
    cartHoldConfirmedBookingId = null;
    sessionStorage.removeItem('kawai_cart');
    sessionStorage.removeItem('kawai_bookingState');
    window.location.href = '/booking';
}

function handleSelectRoomClick(button) {
    const categoryName = button.getAttribute('data-category');
    const pricePerNight = parseFloat(button.getAttribute('data-price'));
    const allRoomNumbers = JSON.parse(button.getAttribute('data-room-numbers'));
    const baseAdults = parseInt(button.getAttribute('data-base-adults') || '0', 10);
    const baseChildren = parseInt(button.getAttribute('data-base-children') || '0', 10);
    const extraAdultSurcharge = parseFloat(button.getAttribute('data-extra-adult') || '0');
    const extraChildSurcharge = parseFloat(button.getAttribute('data-extra-child') || '0');

    const bookingActionBlock = button.closest('.booking-action-block');
    const quantity = parseInt(bookingActionBlock.querySelector('.room-quantity-select').value, 10);
    const adultsInput = bookingActionBlock.querySelector('.adults-input');
    const childrenInput = bookingActionBlock.querySelector('.children-input');

    // Đảm bảo clamp lại 1 lần nữa phòng khi user chưa blur mà bấm nút luôn
    if (adultsInput) clampGuestInput(adultsInput);
    if (childrenInput) clampGuestInput(childrenInput);

    const adultsPerRoom = adultsInput ? parseInt(adultsInput.value, 10) : baseAdults;
    const childrenPerRoom = childrenInput ? parseInt(childrenInput.value, 10) : baseChildren;
    const childrenAges = Array.from(bookingActionBlock.querySelectorAll('.child-age-select')).map(s => parseInt(s.value, 10));

    if (quantity === 0) return;

    const validationError = validateBookingDates(bookingState.checkIn, bookingState.checkOut);
    if (validationError) {
        showToast(validationError, 'error');
        return;
    }

    const diffDays = Math.ceil(Math.abs(bookingState.checkOut - bookingState.checkIn) / (1000 * 60 * 60 * 24)) || 1;

    // Tính phụ thu cho người lớn thêm
    const extraAdults = Math.max(0, adultsPerRoom - baseAdults);
    const adultSurchargePerNight = extraAdultSurcharge * extraAdults;

    // Tính phụ thu trẻ em (child vượt baseChildren sẽ bị tính phụ thu theo tuổi)
    // Dùng extraChildSurcharge làm fallback mặc định khi không có data tuổi chi tiết
    const chargeableChildren = Math.max(0, childrenPerRoom - baseChildren);
    const childSurchargePerNight = extraChildSurcharge * chargeableChildren;

    const surchargePerNightPerRoom = adultSurchargePerNight + childSurchargePerNight;
    const estimatedSurchargeTotal = surchargePerNightPerRoom * quantity * diffDays;
    const roomBaseTotal = pricePerNight * quantity * diffDays;

    selectedRoomsCart[categoryName] = {
        quantity, pricePerNight, adultsPerRoom, childrenPerRoom, childrenAges,
        baseAdults, baseChildren,
        extraAdultSurcharge, extraChildSurcharge,
        roomNumbers: allRoomNumbers.slice(0, quantity),
        estimatedSurchargePerNight: surchargePerNightPerRoom,
        estimatedSurchargeFee: estimatedSurchargeTotal,
        estimatedTotal: roomBaseTotal + estimatedSurchargeTotal,
        diffDays
    };

    updateCartUI();
    saveCartToStorage();

    const cartWrapper = document.getElementById('bookingCartWrapper');
    if (cartWrapper) {
        cartWrapper.classList.remove('hidden');
        cartWrapper.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
    showToast(`Đã thêm ${quantity} phòng ${categoryName} vào giỏ.`, 'success');
}

// ============================================================================
// 5. API SERVICES
// ============================================================================
function showSearchSpinner() {
    const grid = document.querySelector('.room-grid');
    if (grid) { grid.style.opacity = '0.3'; grid.style.pointerEvents = 'none'; }
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
        </div>`;
    document.body.appendChild(spinner);
    return spinner;
}

function hideSearchSpinner(spinner) {
    if (spinner) spinner.remove();
    const grid = document.querySelector('.room-grid');
    if (grid) { grid.style.opacity = '1'; grid.style.pointerEvents = 'auto'; }
}

function executeSearch(e) {
    if (e) e.stopPropagation();
    closeAllDropdowns();

    const spinner = showSearchSpinner();
    const checkInStr = formatLocalDate(bookingState.checkIn);
    const checkOutStr = formatLocalDate(bookingState.checkOut);
    const keywordInput = document.querySelector('input[name="keyword"]');
    const keyword = keywordInput ? keywordInput.value.trim() : '';

    let url = `/api/rooms/search?checkIn=${checkInStr}&checkOut=${checkOutStr}&minRooms=${bookingState.units || 1}`;
    if (keyword) url += `&categoryName=${encodeURIComponent(keyword)}`;

    fetch(url)
        .then(async res => {
            if (!res.ok) throw new Error((await res.json().catch(() => { })).message || 'Có lỗi xảy ra khi tìm kiếm phòng trống!');
            return res.json();
        })
        .then(data => {
            hideSearchSpinner(spinner);
            if (bookingState.priceFilter && bookingState.priceFilter !== 'all') {
                data = data.filter(room => {
                    const p = room.pricePerNight;
                    if (bookingState.priceFilter === 'under_5') return p < 5000000;
                    if (bookingState.priceFilter === '5_to_10') return p >= 5000000 && p <= 10000000;
                    if (bookingState.priceFilter === 'over_10') return p > 10000000;
                    return true;
                });
            }
            renderRoomResults(data);
        })
        .catch(err => {
            hideSearchSpinner(spinner);
            showToast(err.message, 'error');
            console.error(err);
        });
}

function confirmCartBooking() {
    if (typeof isUserLoggedIn !== 'undefined' && !isUserLoggedIn) {
        showToast('Vui lòng đăng nhập để tiếp tục đặt phòng.', 'error');
        saveCartToStorage();
        if (typeof openLoginModal === 'function') openLoginModal();
        return;
    }
    const roomSelections = [];
    let totalCartPrice = 0;
    let totalSurchargeFee = 0;
    const roomCategories = [];
    let totalAdults = 0;
    let totalChildren = 0;

    for (const catName in selectedRoomsCart) {
        const item = selectedRoomsCart[catName];
        if (!item || item.quantity <= 0) continue;

        item.roomNumbers.forEach(roomNo => {
            roomSelections.push({
                categoryName: catName,
                roomNumber: roomNo,
                numberOfAdults: item.adultsPerRoom,
                numberOfChildren: item.childrenPerRoom,
                childrenAges: item.childrenAges || []
            });
        });

        totalCartPrice += item.estimatedTotal || 0;
        totalSurchargeFee += item.estimatedSurchargeFee || 0;
        roomCategories.push(`${item.quantity}x ${catName}`);
        totalAdults += (item.adultsPerRoom || 0) * item.quantity;
        totalChildren += (item.childrenPerRoom || 0) * item.quantity;
    }

    if (roomSelections.length === 0) {
        showToast('Chưa chọn phòng nào!', 'error');
        return;
    }

    const cartSnapshot = {
        roomSelections,
        checkInDate: formatLocalDate(bookingState.checkIn),
        checkOutDate: formatLocalDate(bookingState.checkOut),
        estimatedTotalPrice: totalCartPrice,
        estimatedSurchargeFee: totalSurchargeFee,
        estimatedDepositAmount: Math.round(totalCartPrice * 0.3),
        roomCategories,
        totalAdults,
        totalChildren,
        nights: bookingState.checkIn && bookingState.checkOut
            ? Math.max(0, Math.round((new Date(bookingState.checkOut) - new Date(bookingState.checkIn)) / 86400000))
            : 0
    };

    sessionStorage.setItem('hoanien_room_cart', JSON.stringify(cartSnapshot));
    sessionStorage.removeItem('kawai_cart');
    sessionStorage.removeItem('kawai_bookingState');
    selectedRoomsCart = {};

    window.location.href = '/payment';
}

async function cancelCartHold() {
    // Không còn cần hủy record ở DB — giỏ phòng chỉ tồn tại trong sessionStorage
    if (!confirm('Bạn có chắc muốn hủy và xóa giỏ phòng không?')) return;
    clearCartAndGoHome();
}

// ============================================================================
// 6. INITIALIZATION & GLOBAL OBSERVERS
// ============================================================================
document.addEventListener('click', function (e) {
    if (!e.target.closest('.search-field') && !e.target.closest('.search-btn') && !e.target.closest('#guestsDropdown') && !e.target.closest('#calendarDropdown') && !e.target.closest('#priceDropdown')) {
        closeAllDropdowns();
    }
});

const scrollObserver = new IntersectionObserver((entries, observer) => {
    entries.forEach(entry => {
        if (entry.isIntersecting) {
            entry.target.classList.add('opacity-100', 'translate-y-0');
            entry.target.classList.remove('opacity-0', 'translate-y-8');
            observer.unobserve(entry.target);
        }
    });
}, { root: null, rootMargin: '0px', threshold: 0.15 });
// Load trang khi chưa tìm kiếm
document.addEventListener("DOMContentLoaded", function () {
    restoreCartFromStorage();
    updateCartUI();

    const hasError = document.querySelector('.bg-red-100');
    const hasSuccess = document.querySelector('.bg-green-100');
    const urlParams = new URLSearchParams(window.location.search);
    if (hasError || hasSuccess || urlParams.get('login') === 'true') {
        if (typeof openLoginModal === 'function') openLoginModal();
    }

    if (typeof isUserLoggedIn !== 'undefined' && isUserLoggedIn) {
        if (!sessionStorage.getItem('loginToastShown')) {
            showToast("Chào mừng quay trở lại! Bạn đã đăng nhập thành công.", "success");
            sessionStorage.setItem('loginToastShown', 'true');
        }
    } else {
        sessionStorage.removeItem('loginToastShown');
    }

    document.addEventListener('submit', (e) => {
        if (e.target && e.target.action && e.target.action.includes('/auth/logout')) sessionStorage.clear();
    });

    initRoomsPagination();
    setTimeout(() => {
        applyDates();
        executeSearch({ preventDefault: () => { }, stopPropagation: () => { } });
    }, 100);

    const sections = document.querySelectorAll('main section, .feature-row, .room-grid, .results, .site-footer');
    sections.forEach(section => {
        section.classList.add('transition-all', 'duration-[800ms]', 'ease-out', 'opacity-0', 'translate-y-8', 'will-change-transform');
        scrollObserver.observe(section);
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

