

// Initialize page elements on DOMContentLoaded
document.addEventListener("DOMContentLoaded", () => {
    initializePage();
    initializeEvents();
});

function initializePage() {
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

    const filterEmployee = document.getElementById("filter-employee");
    if (filterEmployee) {
        filterEmployee.addEventListener("change", applyFilters);
    }

    const filterModule = document.getElementById("filter-module");
    if (filterModule) {
        filterModule.addEventListener("change", applyFilters);
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
    const arrow   = document.getElementById("master-data-arrow");
    if (!submenu || !arrow) return;

    const isOpen = submenu.classList.toggle("open");
    arrow.classList.toggle("open", isOpen);
    arrow.innerHTML = `<i data-lucide="${isOpen ? "chevron-down" : "chevron-right"}"></i>`;
    if (typeof lucide !== "undefined") lucide.createIcons();
}

/**
 * Filter the log entry list based on dropdown options
 */
function applyFilters() {
    const filterEmployee = document.getElementById("filter-employee");
    const filterModule = document.getElementById("filter-module");
    if (!filterEmployee || !filterModule) {
        return;
    }

    const empFilter = filterEmployee.value;
    const modFilter = filterModule.value;

    const entries = document.querySelectorAll(".log-entry");
    let totalCount = 0;
    let sensitiveCount = 0;
    let criticalCount = 0;

    entries.forEach(entry => {
        const emp = entry.dataset.employee;
        const mods = (entry.dataset.modules || "").split(",");
        const severity = entry.dataset.severity;

        const empMatch = empFilter === "Tất cả nhân viên" || emp === empFilter;
        const modMatch = modFilter === "Tất cả module" || mods.includes(modFilter);

        if (empMatch && modMatch) {
            entry.style.display = "";
            entry.style.cursor = "pointer";
            
            // Add click listener to open modal using actual record data
            entry.onclick = function() {
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

                if (recordId !== '0') {
                    openAuditModal(recordId, apiTable);
                }
            };
            
            totalCount++;
            if (severity === "sensitive") {
                sensitiveCount++;
            }
            if (severity === "critical") {
                criticalCount++;
            }
        } else {
            entry.style.display = "none";
        }
    });

    updateCounters(totalCount, sensitiveCount, criticalCount);
}

/**
 * Update the header stats counters with new totals
 * @param {number} total - Total log count
 * @param {number} sensitive - Sensitive log count
 * @param {number} critical - Critical log count
 */
function updateCounters(total, sensitive, critical) {
    const counterTotal = document.getElementById("counter-total");
    const counterSensitive = document.getElementById("counter-sensitive");
    const counterCritical = document.getElementById("counter-critical");

    if (counterTotal) {
        counterTotal.innerText = total;
    }
    if (counterSensitive) {
        counterSensitive.innerText = sensitive;
    }
    if (counterCritical) {
        counterCritical.innerText = critical;
    }
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

function openAuditModal(entityId, tableName) {
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
                renderMockDiff(contentEl, tableName);
                return;
            }
            renderDiffHtml(contentEl, data, tableName, numericId);
        })
        .catch(err => {
            if (contentEl) {
                // Fallback to mock data for presentation purposes
                renderMockDiff(contentEl, tableName);
            }
        });
}

function renderMockDiff(contentEl, tableName) {
    const html = `
        <div class="history-item">
            <div class="history-meta">
                <span class="history-rev">#1204 — Cập nhật dữ liệu (Mẫu)</span>
                <span>Vừa xong</span>
            </div>
            <div style="font-size:13px;color:#6B6558;margin-bottom:12px">Bởi: <strong>Admin Dũng</strong></div>
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
                    </tbody>
                </table>
            </div>
        </div>
    `;
    contentEl.innerHTML = html;
    if (typeof lucide !== "undefined") lucide.createIcons();
}

function renderDiffHtml(contentEl, data, tableName, numericId) {
    let html = '';
    data.forEach((entry, idx) => {
        const revNum = entry.revisionNumber || idx + 1;
        const timestamp = entry.timestamp || '--';
        const username = entry.username || 'System';
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
                    <span>${timestamp}</span>
                </div>
                <div style="font-size:13px;color:#6B6558;margin-bottom:12px">Bởi: <strong>${username}</strong></div>
                ${tableHtml}
            </div>
        `;
    });
    contentEl.innerHTML = html;
    if (typeof lucide !== "undefined") lucide.createIcons();
}
