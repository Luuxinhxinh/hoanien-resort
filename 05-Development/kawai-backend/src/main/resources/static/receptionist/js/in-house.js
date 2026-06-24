// In-House Details Modal Logic
function openInHouseModal(guestName, phone, roomSummary, depsDivId) {
    document.getElementById('modalInHouseGuestName').innerText = guestName;
    document.getElementById('modalInHousePhone').innerText = phone ? 'SĐT: ' + phone : '';
    document.getElementById('modalInHouseRooms').innerText = roomSummary;

    // Lấy nội dung HTML của div ẩn chứa danh sách dependents và gán vào modal
    const depsHtml = document.getElementById(depsDivId).innerHTML;
    document.getElementById('modalInHouseDependents').innerHTML = depsHtml;

    document.getElementById('inHouseModal').style.display = 'flex';
}

function closeInHouseModal() {
    document.getElementById('inHouseModal').style.display = 'none';
}
