/**
 * dashboard.js — UI behaviour only.
 *
 * Responsibilities:
 *   - Initialise Lucide icons
 *   - Toggle Master Data sidebar submenu
 *   - Generic UI interactions (future: tooltips, modals, etc.)
 *
 * NOT responsible for:
 *   - Generating room matrix HTML  (→ moved to AdminController + Thymeleaf)
 *   - KPI values                   (→ moved to AdminController + Thymeleaf)
 *   - Activity list data           (→ moved to AdminController + Thymeleaf)
 *   - Checkout list data           (→ moved to AdminController + Thymeleaf)
 */

document.addEventListener("DOMContentLoaded", () => {
    initIcons();
    initSidebarSubmenu();
});

// ─────────────────────────────────────────────────────────────────────────────
// Icons
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Initialise all Lucide icon placeholders in the page.
 */
function initIcons() {
    if (typeof lucide !== "undefined") {
        lucide.createIcons();
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sidebar submenu
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Wire up the Master Data toggle button to expand/collapse its submenu.
 */
function initSidebarSubmenu() {
    const toggle = document.getElementById("master-data-toggle");
    if (toggle) {
        toggle.addEventListener("click", handleMasterDataToggle);
    }
}

/**
 * Expand or collapse the Master Data sidebar submenu and update the
 * chevron icon accordingly.
 *
 * @param {Event} event
 */
function handleMasterDataToggle(event) {
    event.preventDefault();

    const submenu = document.getElementById("master-data-submenu");
    const arrow   = document.getElementById("master-data-arrow");

    if (!submenu || !arrow) return;

    const isOpen = submenu.classList.toggle("open");
    arrow.classList.toggle("open", isOpen);

    // Swap chevron direction
    const iconName = isOpen ? "chevron-down" : "chevron-right";
    arrow.innerHTML = `<i data-lucide="${iconName}"></i>`;

    // Re-initialise Lucide so the new icon renders
    if (typeof lucide !== "undefined") {
        lucide.createIcons();
    }
}
