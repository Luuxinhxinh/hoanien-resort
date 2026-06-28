/**
 * reviews.js — UI interaction logic for Review Management page.
 *
 * Dữ liệu reviews được render sẵn bởi Thymeleaf từ AdminController.
 * File này chỉ xử lý:
 *   - Sidebar submenu toggle
 *   - Filter buttons (type / rating / status) — client-side toggle via data-* attributes
 *   - Text truncation (> 140 chars) + expand/collapse
 *   - Hide review: open modal, confirm → update card DOM for session
 *   - Restore review: update card DOM for session
 *   - Review counter update
 *   - Lucide icon re-init
 */

// ─────────────────────────────────────────────────────────────────────────────
// Filter state
// ─────────────────────────────────────────────────────────────────────────────

let activeTypeFilter   = "All";
let activeRatingFilter = "All";
let activeStatusFilter = "All";

// Track which review is pending hide confirmation
let pendingHideId = null;

// ─────────────────────────────────────────────────────────────────────────────
// Active button classes (keep CSS identical to original)
// ─────────────────────────────────────────────────────────────────────────────

// NOTE: Active/inactive visual styles are driven by .filter-btn-active / .filter-btn-inactive
// defined in theme.css — using classList avoids Tailwind v4 dynamic scan issues.

// ─────────────────────────────────────────────────────────────────────────────
// Init
// ─────────────────────────────────────────────────────────────────────────────

document.addEventListener("DOMContentLoaded", () => {
    // Sidebar toggle
    const toggleBtn = document.getElementById("master-data-toggle");
    if (toggleBtn) toggleBtn.addEventListener("click", handleSidebarToggle);

    // Filter buttons — type
    document.querySelectorAll(".filter-btn-type").forEach(btn => {
        btn.addEventListener("click", () => {
            activeTypeFilter = btn.dataset.type;
            updateFilterGroup("filter-type-group", "filter-btn-type", "type", activeTypeFilter);
            applyFilters();
        });
    });

    // Filter buttons — rating
    document.querySelectorAll(".filter-btn-rating").forEach(btn => {
        btn.addEventListener("click", () => {
            activeRatingFilter = btn.dataset.rating;
            updateFilterGroup("filter-rating-group", "filter-btn-rating", "rating", activeRatingFilter);
            applyFilters();
        });
    });

    // Filter buttons — status
    document.querySelectorAll(".filter-btn-status").forEach(btn => {
        btn.addEventListener("click", () => {
            activeStatusFilter = btn.dataset.status;
            updateFilterGroup("filter-status-group", "filter-btn-status", "status", activeStatusFilter);
            applyFilters();
        });
    });

    // Hide modal controls
    const closeHideModalBtn = document.getElementById("btn-close-hide-modal");
    const cancelHideBtn     = document.getElementById("btn-cancel-hide");
    const submitHideBtn     = document.getElementById("btn-submit-hide");
    const reasonTextarea    = document.getElementById("hide-reason-textarea");

    if (closeHideModalBtn) closeHideModalBtn.addEventListener("click", closeHideModal);
    if (cancelHideBtn)     cancelHideBtn.addEventListener("click", closeHideModal);

    if (reasonTextarea) {
        reasonTextarea.addEventListener("input", () => {
            const hasText = reasonTextarea.value.trim() !== "";
            submitHideBtn.disabled = !hasText;
            submitHideBtn.className = hasText
                ? "px-5 py-2 rounded-lg border-none bg-primary hover:opacity-95 text-[#1A1814] font-sans text-[14px] font-medium cursor-pointer flex items-center gap-2"
                : "px-5 py-2 rounded-lg border-none bg-primary/40 text-[#1A1814] font-sans text-[14px] font-medium cursor-not-allowed flex items-center gap-2";
        });
    }

    if (submitHideBtn) submitHideBtn.addEventListener("click", submitHideReview);

    // Close modal on backdrop click
    const modal = document.getElementById("hide-reason-modal");
    if (modal) modal.addEventListener("click", closeHideModal);

    // Wire all hide/restore buttons (already rendered by Thymeleaf)
    wireHideRestoreButtons();

    // Init text truncation for all review cards
    initTextTruncation();

    // Init Lucide icons
    if (typeof lucide !== "undefined") lucide.createIcons();
});

// ─────────────────────────────────────────────────────────────────────────────
// Sidebar submenu toggle
// ─────────────────────────────────────────────────────────────────────────────

function handleSidebarToggle(event) {
    event.preventDefault();
    const submenu = document.getElementById("master-data-submenu");
    const arrow   = document.getElementById("master-data-arrow");
    if (!submenu || !arrow) return;
    const isOpen = submenu.classList.toggle("open");
    arrow.classList.toggle("open", isOpen);
    arrow.innerHTML = `<i data-lucide="${isOpen ? "chevron-down" : "chevron-right"}"></i>`;
    if (typeof lucide !== "undefined") lucide.createIcons();
}

// ─────────────────────────────────────────────────────────────────────────────
// Filter logic — reads data-* attributes set by Thymeleaf
// ─────────────────────────────────────────────────────────────────────────────

function applyFilters() {
    const cards = document.querySelectorAll(".review-card");
    let visibleCount = 0;

    cards.forEach(card => {
        const type    = card.dataset.type;
        const rating  = card.dataset.rating;
        const visible = card.dataset.visible === "true";

        const typeMatch   = activeTypeFilter   === "All" || type   === activeTypeFilter;
        const ratingMatch = activeRatingFilter === "All" || rating  === activeRatingFilter;
        const statusMatch = activeStatusFilter === "All"
            || (activeStatusFilter === "Visible" ? visible : !visible);

        const show = typeMatch && ratingMatch && statusMatch;
        card.style.display = show ? "" : "none";
        if (show) visibleCount++;
    });

    const counter = document.getElementById("review-counter-label");
    if (counter) counter.textContent = `${visibleCount} đánh giá`;

    // Re-init icons for any newly shown cards
    if (typeof lucide !== "undefined") lucide.createIcons();
}

function updateFilterGroup(groupId, btnClass, dataAttr, activeVal) {
    document.querySelectorAll(`.${btnClass}`).forEach(btn => {
        const isActive = btn.dataset[dataAttr] === activeVal;
        btn.classList.toggle("filter-btn-active",   isActive);
        btn.classList.toggle("filter-btn-inactive", !isActive);
    });
}

// ─────────────────────────────────────────────────────────────────────────────
// Text truncation — shorten review text > 140 chars, add expand button
// ─────────────────────────────────────────────────────────────────────────────

function initTextTruncation() {
    document.querySelectorAll(".review-text").forEach(p => {
        const fullText = p.dataset.fullText || p.textContent;
        if (fullText.length <= 140) return;

        p.textContent = fullText.slice(0, 140) + "…";

        const readMoreBtn = p.nextElementSibling;
        if (!readMoreBtn || !readMoreBtn.classList.contains("review-read-more")) return;

        readMoreBtn.classList.remove("hidden");
        let expanded = false;

        readMoreBtn.addEventListener("click", () => {
            expanded = !expanded;
            p.textContent = expanded ? fullText : fullText.slice(0, 140) + "…";
            readMoreBtn.textContent = expanded ? "Thu gọn" : "Xem thêm";
        });
    });
}

// ─────────────────────────────────────────────────────────────────────────────
// Hide / Restore wire-up
// ─────────────────────────────────────────────────────────────────────────────

function wireHideRestoreButtons() {
    document.querySelectorAll(".btn-hide-review").forEach(btn => {
        btn.addEventListener("click", () => {
            pendingHideId = btn.dataset.id;
            const text    = btn.dataset.text   || "";
            const guest   = btn.dataset.guest  || "Ẩn danh";
            const service = btn.dataset.service || "";

            document.getElementById("hide-modal-text").textContent =
                `"${text.slice(0, 100)}${text.length > 100 ? "…" : ""}"`;
            document.getElementById("hide-modal-meta").textContent =
                `— ${guest} · ${service}`;
            document.getElementById("hide-reason-textarea").value = "";

            // Reset submit button
            const submitBtn = document.getElementById("btn-submit-hide");
            submitBtn.disabled = true;
            submitBtn.className = "px-5 py-2 rounded-lg border-none bg-primary/40 text-[#1A1814] font-sans text-[14px] font-medium cursor-not-allowed flex items-center gap-2";

            openHideModal();
        });
    });

    document.querySelectorAll(".btn-restore-review").forEach(btn => {
        btn.addEventListener("click", () => {
            const id = btn.dataset.id;
            moderateReview(id, "Approved", "Khôi phục hiển thị đánh giá")
                .then(() => restoreReviewInDom(id))
                .catch(err => alert(err.message || "Không thể khôi phục đánh giá. Vui lòng thử lại."));
        });
    });
}

// ─────────────────────────────────────────────────────────────────────────────
// Hide modal helpers
// ─────────────────────────────────────────────────────────────────────────────

function openHideModal() {
    const modal = document.getElementById("hide-reason-modal");
    if (!modal) return;
    modal.classList.remove("hidden");
    modal.classList.add("flex");
    document.body.style.overflow = "hidden";
    if (typeof lucide !== "undefined") lucide.createIcons();
}

function closeHideModal() {
    const modal = document.getElementById("hide-reason-modal");
    if (!modal) return;
    modal.classList.add("hidden");
    modal.classList.remove("flex");
    document.body.style.overflow = "unset";
    pendingHideId = null;
}

function submitHideReview() {
    const reason = document.getElementById("hide-reason-textarea").value.trim();
    if (!pendingHideId || !reason) return;

    const id = pendingHideId;
    moderateReview(id, "Hidden", reason)
        .then(() => {
            hideReviewInDom(id, reason);
            closeHideModal();
        })
        .catch(err => alert(err.message || "Không thể ẩn đánh giá. Vui lòng thử lại."));
}

function normalizeReviewId(id) {
    return String(id || "").replace(/^RV-/i, "");
}

async function moderateReview(id, status, reason) {
    const params = new URLSearchParams({
        newStatus: status,
        reason: reason || (status === "Approved" ? "Khôi phục hiển thị đánh giá" : "Ẩn đánh giá")
    });
    const response = await fetch(`/api/v1/reviews/${normalizeReviewId(id)}/moderate?${params}`, {
        method: "PUT"
    });
    if (!response.ok) {
        const message = await response.text();
        throw new Error(message || "Moderation API failed");
    }
    return response.json();
}

// ─────────────────────────────────────────────────────────────────────────────
// DOM updates for hide / restore (session-only; no backend call yet)
// ─────────────────────────────────────────────────────────────────────────────

function hideReviewInDom(id, reason) {
    const card = document.querySelector(`.review-card[data-id="${id}"]`);
    if (!card) return;

    // Update data attribute for filter logic
    card.dataset.visible = "false";
    card.style.opacity   = "0.78";
    card.style.outline   = "1px solid rgba(140,60,40,0.2)";

    // Swap visibility badge
    const visibleBadge = card.querySelector(".bg-\\[\\#5A8C6B\\]\\/15");
    const hiddenBadge  = card.querySelector(".bg-\\[\\#8C3C28\\]\\/12");
    if (visibleBadge) {
        visibleBadge.className = "px-2.5 py-0.5 rounded-md flex items-center gap-1 bg-[#8C3C28]/12 text-[#8C3C28] font-sans text-[12px] font-medium";
        visibleBadge.innerHTML = `<i data-lucide="eye-off" class="w-[11px] h-[11px]"></i> Đã ẩn`;
    }

    // Update audit trail
    const auditEl = card.querySelector(".review-audit-trail");
    if (auditEl) {
        const today = new Date().toLocaleDateString("vi-VN");
        auditEl.textContent = `Ẩn bởi Admin Dũng ngày ${today} — ${reason}`;
        auditEl.classList.remove("hidden");
    }

    // Swap action button: hide → restore
    const hideBtn = card.querySelector(".btn-hide-review");
    if (hideBtn) {
        const restoreBtn = document.createElement("button");
        restoreBtn.className = "btn-restore-review flex items-center gap-2 px-4 py-2 rounded-lg border border-[#2D6B4A]/30 bg-[#2D6B4A]/12 text-[#2D6B4A] font-sans text-[13px] cursor-pointer hover:opacity-75 transition-opacity";
        restoreBtn.dataset.id = id;
        restoreBtn.innerHTML = `<i data-lucide="eye" class="w-[13px] h-[13px]"></i> Khôi phục hiển thị`;
        restoreBtn.addEventListener("click", () => {
            moderateReview(id, "Approved", "Khôi phục hiển thị đánh giá")
                .then(() => restoreReviewInDom(id))
                .catch(err => alert(err.message || "Không thể khôi phục đánh giá. Vui lòng thử lại."));
        });
        hideBtn.parentNode.replaceChild(restoreBtn, hideBtn);
    }

    if (typeof lucide !== "undefined") lucide.createIcons();
    applyFilters();
}

function restoreReviewInDom(id) {
    const card = document.querySelector(`.review-card[data-id="${id}"]`);
    if (!card) return;

    card.dataset.visible = "true";
    card.style.opacity   = "1";
    card.style.outline   = "none";

    // Update audit trail — hide it
    const auditEl = card.querySelector(".review-audit-trail");
    if (auditEl) {
        auditEl.textContent = "";
        auditEl.classList.add("hidden");
    }

    // Swap visibility badge back
    const hiddenBadge = card.querySelector(".bg-\\[\\#8C3C28\\]\\/12");
    if (hiddenBadge) {
        hiddenBadge.className = "px-2.5 py-0.5 rounded-md flex items-center gap-1 bg-[#5A8C6B]/15 text-[#2D6B4A] font-sans text-[12px] font-medium";
        hiddenBadge.innerHTML = `<i data-lucide="check" class="w-[11px] h-[11px]"></i> Hiển thị`;
    }

    // Swap action button: restore → hide
    const restoreBtn = card.querySelector(".btn-restore-review");
    if (restoreBtn) {
        const text    = card.querySelector(".review-text")?.dataset.fullText || "";
        const guest   = card.querySelector("p.font-sans.text-\\[15px\\]")?.textContent || "Ẩn danh";
        const service = card.querySelector(".font-serif-heading.italic")?.textContent || "";

        const hideBtn = document.createElement("button");
        hideBtn.className = "btn-hide-review flex items-center gap-2 px-4 py-2 rounded-lg border border-[#8C3C28]/30 text-[#8C3C28] font-sans text-[13px] bg-transparent cursor-pointer hover:opacity-75 transition-opacity";
        hideBtn.dataset.id      = id;
        hideBtn.dataset.text    = text;
        hideBtn.dataset.guest   = guest;
        hideBtn.dataset.service = service;
        hideBtn.innerHTML = `<i data-lucide="eye-off" class="w-[13px] h-[13px]"></i> Ẩn đánh giá`;
        hideBtn.addEventListener("click", () => {
            pendingHideId = id;
            document.getElementById("hide-modal-text").textContent = `"${text.slice(0, 100)}…"`;
            document.getElementById("hide-modal-meta").textContent = `— ${guest} · ${service}`;
            document.getElementById("hide-reason-textarea").value = "";
            const submitBtn = document.getElementById("btn-submit-hide");
            submitBtn.disabled = true;
            submitBtn.className = "px-5 py-2 rounded-lg border-none bg-primary/40 text-[#1A1814] font-sans text-[14px] font-medium cursor-not-allowed flex items-center gap-2";
            openHideModal();
        });
        restoreBtn.parentNode.replaceChild(hideBtn, restoreBtn);
    }

    if (typeof lucide !== "undefined") lucide.createIcons();
    applyFilters();
}
