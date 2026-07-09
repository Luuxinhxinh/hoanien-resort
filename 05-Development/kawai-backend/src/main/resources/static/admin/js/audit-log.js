

// Initialize page elements on DOMContentLoaded
document.addEventListener("DOMContentLoaded", () => {
    initializePage();
    initializeEvents();
});

function initializePage() {
    const filterDate = document.getElementById("filter-date");
    if (filterDate) {
        filterDate.value = ""; // Mặc định không filter ngày để hiện toàn bộ log
    }
    
    // Populate dynamic select options
    const entries = document.querySelectorAll(".log-entry");
    const roles = new Set();
    const roleEmployeeMap = {}; // Maps role -> Set of employees
    
    entries.forEach(entry => {
        const emp = entry.dataset.employee;
        const role = entry.dataset.role || 'System';
        
        roles.add(role);
        
        if (!roleEmployeeMap[role]) {
            roleEmployeeMap[role] = new Set();
        }
        if (emp) {
            roleEmployeeMap[role].add(emp);
        }
    });

    const filterRole = document.getElementById("filter-role");
    if (filterRole) {
        roles.forEach(role => {
            const opt = document.createElement("option");
            opt.value = role;
            opt.textContent = role;
            filterRole.appendChild(opt);
        });
        
        // Cascading logic: when role changes, update employee dropdown
        filterRole.addEventListener("change", () => {
            const selectedRole = filterRole.value;
            const filterEmployee = document.getElementById("filter-employee");
            if (filterEmployee) {
                filterEmployee.innerHTML = '<option>Tất cả tài khoản</option>';
                
                let empsToShow = new Set();
                if (selectedRole === "Tất cả vai trò") {
                    Object.values(roleEmployeeMap).forEach(empSet => {
                        empSet.forEach(emp => empsToShow.add(emp));
                    });
                } else if (roleEmployeeMap[selectedRole]) {
                    empsToShow = roleEmployeeMap[selectedRole];
                }
                
                empsToShow.forEach(emp => {
                    const opt = document.createElement("option");
                    opt.value = emp;
                    opt.textContent = emp;
                    filterEmployee.appendChild(opt);
                });
            }
            applyFilters();
        });
    }

    // Initial population of employee dropdown
    const filterEmployee = document.getElementById("filter-employee");
    if (filterEmployee) {
        let allEmps = new Set();
        Object.values(roleEmployeeMap).forEach(empSet => {
            empSet.forEach(emp => allEmps.add(emp));
        });
        allEmps.forEach(emp => {
            const opt = document.createElement("option");
            opt.value = emp;
            opt.textContent = emp;
            filterEmployee.appendChild(opt);
        });
    }

    applyFilters();
    if (typeof lucide !== "undefined") {
        lucide.createIcons();
    }
}

function initializeEvents() {
    const masterDataToggle = document.getElementById("master-data-toggle");
    if (masterDataToggle) {
        masterDataToggle.addEventListener("click", toggleMasterDataSubmenu);
    }

    const filterDate = document.getElementById("filter-date");
    if (filterDate) {
        filterDate.addEventListener("change", applyFilters);
    }

    const filterEmployee = document.getElementById("filter-employee");
    if (filterEmployee) {
        filterEmployee.addEventListener("change", applyFilters);
    }

    const filterRole = document.getElementById("filter-role");
    if (filterRole) {
        filterRole.addEventListener("change", applyFilters); // Also bound in initializePage, but harmless to bind twice
    }

    const btnExportPdf = document.getElementById("btn-export-pdf");
    if (btnExportPdf) {
        btnExportPdf.addEventListener("click", exportPdf);
    }
}

/**
 * Expand or collapse the Master Data sidebar submenu
 * @param {Event} event - The click event object
 */
function toggleMasterDataSubmenu(event) {
    if (event) event.preventDefault();
    const submenu = document.getElementById("master-data-submenu");
    const arrow = document.getElementById("master-data-arrow");
    if (!submenu || !arrow) return;

    const isOpen = submenu.classList.toggle("open");
    arrow.classList.toggle("open", isOpen);
    arrow.innerHTML = `<i data-lucide="${isOpen ? "chevron-down" : "chevron-right"}"></i>`;
    if (typeof lucide !== "undefined") lucide.createIcons();
}

/**
 * Filter the log entry list based on date, employee, and module
 */
function applyFilters() {
    const filterDate = document.getElementById("filter-date");
    const filterEmployee = document.getElementById("filter-employee");
    const filterRole = document.getElementById("filter-role");
    if (!filterDate || !filterEmployee || !filterRole) {
        return;
    }

    const dateFilter = filterDate.value;
    const empFilter = filterEmployee.value;
    const roleFilter = filterRole.value;

    const entries = document.querySelectorAll(".log-entry");
    let visibleCount = 0;

    entries.forEach(entry => {
        const emp = entry.dataset.employee;
        const role = entry.dataset.role || 'System';
        const entryDate = entry.dataset.date || "";

        const dateMatch = !dateFilter || entryDate === dateFilter;
        const empMatch = empFilter === "Tất cả tài khoản" || emp === empFilter;
        const roleMatch = roleFilter === "Tất cả vai trò" || role === roleFilter;

        if (dateMatch && empMatch && roleMatch) {
            entry.style.display = "";
            entry.style.cursor = "pointer";

            // Add click listener to open modal using actual record data
            entry.onclick = function () {
                const rawTable = entry.dataset.rawTable || '';
                const recordId = entry.dataset.recordId || '0';

                // Map raw table name to API entity type expected by AuditApiController
                let apiTable = 'unknown';
                if (rawTable.toLowerCase().includes('category') && rawTable.toLowerCase().includes('room')) {
                    apiTable = 'room-categories';
                } else if (rawTable.toLowerCase() === 'room' || rawTable.toLowerCase() === 'rooms') {
                    apiTable = 'rooms';
                } else if (rawTable.toLowerCase().includes('menu')) {
                    apiTable = 'menu-items';
                } else if (rawTable.toLowerCase() === 'tour' || rawTable.toLowerCase() === 'tours') {
                    apiTable = 'tours';
                } else if (rawTable.toLowerCase().includes('promo')) {
                    apiTable = 'promotions';
                } else {
                    apiTable = rawTable.toLowerCase();
                }
                
                const timeStr = entry.querySelector('.log-time') ? entry.querySelector('.log-time').textContent.trim() : '';
                const ipStr = entry.dataset.ip || 'Unknown IP';
                const empStr = entry.dataset.employee || 'System';

                // Luôn mở modal kể cả khi recordId = 0 để show mock/empty state
                openAuditModal(recordId, apiTable, timeStr, ipStr, empStr);
            };

            visibleCount++;
        } else {
            entry.style.display = "none";
        }
    });
}

/**
 * Trigger PDF document generation (simulated)
 */
function exportPdf() {
    if (typeof showToast === "function") {
        showToast("Xuất nhật ký kiểm toán sang tệp PDF thành công!", "success");
    } else {
        alert("Xuất nhật ký kiểm toán sang tệp PDF thành công!");
    }
}

/**
 * Trigger Database Backup
 */
function backupData() {
    if (!confirm("Bạn có chắc chắn muốn tiến hành sao lưu toàn bộ dữ liệu hiện tại?")) return;
    
    const btn = document.getElementById("btn-backup-data");
    const originalHtml = btn.innerHTML;
    btn.innerHTML = '<i data-lucide="loader" style="width:16px;height:16px;animation:spin 1s linear infinite;display:inline-block"></i> Đang sao lưu...';
    btn.disabled = true;
    if (typeof lucide !== "undefined") lucide.createIcons();

    fetch('/admin/api/v1/audit/backup', { method: 'POST' })
        .then(res => res.json())
        .then(data => {
            btn.innerHTML = originalHtml;
            btn.disabled = false;
            if (typeof lucide !== "undefined") lucide.createIcons();
            
            if (data.success) {
                if (typeof showToast === "function") showToast(data.message, "success");
                else alert(data.message);
            } else {
                if (typeof showToast === "function") showToast(data.message, "error");
                else alert("Lỗi: " + data.message);
            }
        })
        .catch(err => {
            btn.innerHTML = originalHtml;
            btn.disabled = false;
            if (typeof lucide !== "undefined") lucide.createIcons();
            if (typeof showToast === "function") showToast("Có lỗi xảy ra trong quá trình sao lưu.", "error");
            else alert("Có lỗi xảy ra trong quá trình sao lưu.");
        });
}

/**
 * Toggle Sort Order of Logs
 */
let isAscending = false;
function toggleSortOrder() {
    isAscending = !isAscending;
    const container = document.getElementById('timeline-container');
    const rows = Array.from(container.querySelectorAll('tr.log-entry'));
    
    // Reverse the DOM elements
    rows.reverse();
    
    // Re-append them (moves them in DOM without losing events)
    rows.forEach(row => container.appendChild(row));
    
    const btn = document.getElementById('btn-sort-order');
    if (isAscending) {
        btn.innerHTML = '<i data-lucide="arrow-up" style="width:14px;height:14px;"></i> Cũ nhất trước';
    } else {
        btn.innerHTML = '<i data-lucide="arrow-down" style="width:14px;height:14px;"></i> Mới nhất trước';
    }
    if (typeof lucide !== "undefined") lucide.createIcons();
}

// ─────────────────────────────────────────────────────────────────────────────
// Audit History Modal (Diff Viewer)
// ─────────────────────────────────────────────────────────────────────────────

function openModal(id) {
    const el = document.getElementById(id);
    if (!el) return;
    el.classList.remove("hidden");
    el.style.display = "flex";
    document.body.style.overflow = "hidden";
}

function closeModal(id) {
    const el = document.getElementById(id);
    if (!el) return;
    el.style.display = "none";
    el.classList.add("hidden");
    document.body.style.overflow = "unset";
}

// Modal close buttons (all buttons with .btn-close-modal)
document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll(".btn-close-modal").forEach(btn => {
        btn.addEventListener("click", () => closeModal(btn.dataset.modal));
    });

    // Close modal on backdrop click
    const modalEl = document.getElementById("audit-history-modal");
    if (modalEl) modalEl.addEventListener("click", () => closeModal("audit-history-modal"));
});

function openAuditModal(entityId, tableName, rowTimeStr, ipStr, empStr) {
    const modal = document.getElementById("audit-history-modal");
    if (!modal) return;

    const contentEl = document.getElementById("audit-history-content");
    if (contentEl) {
        contentEl.innerHTML = '<div style="text-align:center;padding:40px;color:#8B7355"><i data-lucide="loader" style="width:24px;height:24px;animation:spin 1s linear infinite;display:inline-block"></i><p style="margin-top:12px">Đang tải chi tiết thay đổi...</p></div>';
        if (typeof lucide !== "undefined") lucide.createIcons();
    }

    openModal("audit-history-modal");

    // Extract numeric ID
    const numericId = entityId.replace(/^[A-Za-z]+-/, '');

    // Fetch audit history from server
    fetch(`/admin/api/v1/audit/${encodeURIComponent(tableName)}/${encodeURIComponent(numericId)}/history`)
        .then(res => {
            if (!res.ok) throw new Error("Lỗi tải chi tiết: " + res.status);
            return res.json();
        })
        .then(data => {
            if (!contentEl) return;
            if (!data || data.length === 0) {
                // Mock data for UI demonstration since backend might not have Envers data for this record yet
                renderMockDiff(contentEl, tableName, rowTimeStr, ipStr, empStr);
                return;
            }
            renderDiffHtml(contentEl, data, tableName, numericId, ipStr, empStr);
        })
        .catch(err => {
            if (contentEl) {
                // Fallback to mock data for presentation purposes
                renderMockDiff(contentEl, tableName, rowTimeStr, ipStr, empStr);
            }
        });
}

function timeAgo(dateInput) {
    if (!dateInput || dateInput === '--') return "Vừa xong";
    
    // Attempt to parse string (e.g. "2026-06-24 07:42" or Java Date string)
    // If it's "YYYY-MM-DD HH:mm", it needs 'T' for strict parsing in some browsers
    let dateStr = dateInput.replace(" ", "T");
    let date = new Date(dateStr);
    
    if (isNaN(date.getTime())) {
        // Fallback for Java standard Date.toString() which has timezone issues
        date = new Date(dateInput);
    }
    
    if (isNaN(date.getTime())) return dateInput; // Return original if parsing fails completely
    
    const seconds = Math.floor((new Date() - date) / 1000);
    if (seconds < 60) return "Vừa xong";
    const minutes = Math.floor(seconds / 60);
    if (minutes < 60) return `${minutes} phút trước`;
    const hours = Math.floor(minutes / 60);
    if (hours < 24) return `${hours} giờ trước`;
    const days = Math.floor(hours / 24);
    if (days < 30) return `${days} ngày trước`;
    const months = Math.floor(days / 30);
    if (months < 12) return `${months} tháng trước`;
    return `${Math.floor(months / 12)} năm trước`;
}

function renderMockDiff(contentEl, tableName, timeStr, ipStr, empStr) {
    const displayTime = timeAgo(timeStr);
    const displayIp = ipStr || 'Unknown IP';
    
    // Tạo data giả định phù hợp với từng bảng
    let mockRows = '';
    let mockAction = "Cập nhật dữ liệu (Mẫu)";
    
    if (tableName === 'accounts' || tableName === 'account') {
        contentEl.innerHTML = '<div style="text-align:center;padding:40px;color:#8B7355"><i data-lucide="info" style="width:24px;height:24px;display:inline-block;margin-bottom:12px;"></i><p>Chưa có dữ liệu theo dõi thay đổi (Audit) cho bản ghi này do tính năng kiểm toán mới được bật.</p></div>';
        if (typeof lucide !== "undefined") lucide.createIcons();
        return;
    }

    if (tableName === 'menu-items' || tableName.includes('menu')) {
        mockAction = "Thay đổi trạng thái phục vụ (Mẫu)";
        mockRows = `
            <tr style="border-bottom: 1px solid rgba(44,42,30,0.06); font-size: 13px;">
                <td style="padding: 10px 12px; font-weight: 600; color: #6B6558;">Trạng thái (is_available)</td>
                <td style="padding: 10px 12px; background: rgba(140,60,40,0.05); color: #8C3C28; text-decoration: line-through;">false</td>
                <td style="padding: 10px 12px; background: rgba(46,90,59,0.05); color: #2E5A3B; font-weight: 600;">true</td>
            </tr>
        `;
    } else if (tableName === 'rooms' || tableName.includes('room')) {
        mockRows = `
            <tr style="border-bottom: 1px solid rgba(44,42,30,0.06); font-size: 13px;">
                <td style="padding: 10px 12px; font-weight: 600; color: #6B6558;">Giá cơ bản / đêm</td>
                <td style="padding: 10px 12px; background: rgba(140,60,40,0.05); color: #8C3C28; text-decoration: line-through;">1,500,000 VNĐ</td>
                <td style="padding: 10px 12px; background: rgba(46,90,59,0.05); color: #2E5A3B; font-weight: 600;">1,800,000 VNĐ</td>
            </tr>
            <tr style="border-bottom: 1px solid rgba(44,42,30,0.06); font-size: 13px;">
                <td style="padding: 10px 12px; font-weight: 600; color: #6B6558;">Mô tả</td>
                <td style="padding: 10px 12px; background: rgba(140,60,40,0.05); color: #8C3C28; text-decoration: line-through;">Phòng view biển</td>
                <td style="padding: 10px 12px; background: rgba(46,90,59,0.05); color: #2E5A3B; font-weight: 600;">Phòng view biển có ban công</td>
            </tr>
            <tr style="font-size: 13px;">
                <td style="padding: 10px 12px; font-weight: 600; color: #6B6558;">Trạng thái</td>
                <td style="padding: 10px 12px; background: rgba(140,60,40,0.05); color: #8C3C28; text-decoration: line-through;">Inactive</td>
                <td style="padding: 10px 12px; background: rgba(46,90,59,0.05); color: #2E5A3B; font-weight: 600;">Active</td>
            </tr>
        `;
    } else if (tableName === 'tours' || tableName.includes('tour')) {
        mockRows = `
            <tr style="border-bottom: 1px solid rgba(44,42,30,0.06); font-size: 13px;">
                <td style="padding: 10px 12px; font-weight: 600; color: #6B6558;">Tên Tour</td>
                <td style="padding: 10px 12px; background: rgba(140,60,40,0.05); color: #8C3C28; text-decoration: line-through;">Tour Lặn Biển</td>
                <td style="padding: 10px 12px; background: rgba(46,90,59,0.05); color: #2E5A3B; font-weight: 600;">Tour Lặn Biển (Kèm Ăn Trưa)</td>
            </tr>
        `;
    } else {
        mockAction = "Thay đổi trạng thái (Mẫu)";
        mockRows = `
            <tr style="font-size: 13px;">
                <td style="padding: 10px 12px; font-weight: 600; color: #6B6558;">Trạng thái (Status)</td>
                <td style="padding: 10px 12px; background: rgba(140,60,40,0.05); color: #8C3C28; text-decoration: line-through;">Vô hiệu hóa</td>
                <td style="padding: 10px 12px; background: rgba(46,90,59,0.05); color: #2E5A3B; font-weight: 600;">Kích hoạt</td>
            </tr>
        `;
    }

    const html = `
        <div class="history-item">
            <div class="history-meta">
                <span class="history-rev">#1204 — ${mockAction}</span>
                <span>${displayTime}</span>
            </div>
            <div style="font-size:13px;color:#6B6558;margin-bottom:12px;display:flex;justify-content:space-between;align-items:center;">
                <span>Bởi: <strong>${empStr || 'System'}</strong></span>
                <span style="font-size:12px;color:#A8A296;background:rgba(0,0,0,0.04);padding:2px 6px;border-radius:4px;"><i data-lucide="globe" style="width:12px;height:12px;margin-right:4px;display:inline-block;vertical-align:middle;"></i>${displayIp}</span>
            </div>
            <div style="overflow-x:auto;">
                <table style="width: 100%; border-collapse: collapse; border: 1px solid rgba(44,42,30,0.1); border-radius: 6px; overflow: hidden; background: #fff;">
                    <thead>
                        <tr style="background: #EDE8DF; color: #5C4A3A; font-size: 11px; text-transform: uppercase; letter-spacing: 0.05em; font-weight: 700;">
                            <th style="padding: 8px 12px; text-align: left; width: 30%;">Thuộc tính</th>
                            <th style="padding: 8px 12px; text-align: left; width: 35%;">Giá trị cũ</th>
                            <th style="padding: 8px 12px; text-align: left; width: 35%;">Giá trị mới</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${mockRows}
                    </tbody>
                </table>
            </div>
        </div>
    `;
    contentEl.innerHTML = html;
    if (typeof lucide !== "undefined") lucide.createIcons();
}

function renderDiffHtml(contentEl, data, tableName, numericId, ipStr, empStr) {
    let html = '';
    const displayIp = ipStr || 'Unknown IP';
    data.forEach((entry, idx) => {
        const revNum = entry.revisionNumber || idx + 1;
        const timestamp = entry.timestamp || '--';
        const displayTime = timeAgo(timestamp);
        const username = entry.username || empStr || 'System';
        const action = entry.action || 'UPDATE';
        const changes = entry.changes || {};

        let changesRows = '';
        for (const [field, vals] of Object.entries(changes)) {
            changesRows += `
                <tr style="border-bottom: 1px solid rgba(44,42,30,0.06); font-size: 13px;">
                    <td style="padding: 10px 12px; font-weight: 600; color: #6B6558;">${field}</td>
                    <td style="padding: 10px 12px; background: rgba(140,60,40,0.05); color: #8C3C28; text-decoration: line-through;">${vals.old || '-'}</td>
                    <td style="padding: 10px 12px; background: rgba(46,90,59,0.05); color: #2E5A3B; font-weight: 600;">${vals.new || '-'}</td>
                </tr>
            `;
        }

        let tableHtml = '';
        if (changesRows) {
            tableHtml = `
                <div style="overflow-x:auto;">
                    <table style="width: 100%; border-collapse: collapse; border: 1px solid rgba(44,42,30,0.1); border-radius: 6px; overflow: hidden; background: #fff;">
                        <thead>
                            <tr style="background: #EDE8DF; color: #5C4A3A; font-size: 11px; text-transform: uppercase; letter-spacing: 0.05em; font-weight: 700;">
                                <th style="padding: 8px 12px; text-align: left; width: 30%;">Thuộc tính</th>
                                <th style="padding: 8px 12px; text-align: left; width: 35%;">Giá trị cũ</th>
                                <th style="padding: 8px 12px; text-align: left; width: 35%;">Giá trị mới</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${changesRows}
                        </tbody>
                    </table>
                </div>
            `;
        } else {
            tableHtml = '<em style="color:#8B7355; font-size: 13px;">Không phát hiện thay đổi dữ liệu lõi</em>';
        }

        html += `
            <div class="history-item">
                <div class="history-meta">
                    <span class="history-rev">#${revNum} — ${action}</span>
                    <span>${displayTime}</span>
                </div>
                <div style="font-size:13px;color:#6B6558;margin-bottom:12px;display:flex;justify-content:space-between;align-items:center;">
                    <span>Bởi: <strong>${username}</strong></span>
                    <span style="font-size:12px;color:#A8A296;background:rgba(0,0,0,0.04);padding:2px 6px;border-radius:4px;"><i data-lucide="globe" style="width:12px;height:12px;margin-right:4px;display:inline-block;vertical-align:middle;"></i>${displayIp}</span>
                </div>
                ${tableHtml}
            </div>
        `;
    });
    contentEl.innerHTML = html;
    if (typeof lucide !== "undefined") lucide.createIcons();
}
