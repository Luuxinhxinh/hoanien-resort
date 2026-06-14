

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
            entry.style.display = "flex";
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
