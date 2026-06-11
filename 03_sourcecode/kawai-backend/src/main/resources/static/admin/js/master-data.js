/**
 * master-data.js — UI interaction logic for Master Data Management.
 *
 * Dữ liệu bảng (rows, columns) được render sẵn bởi Thymeleaf từ AdminController.
 * File này chỉ xử lý:
 *   - Tab switching (server-side navigation)
 *   - Sidebar submenu toggle
 *   - Client-side search (filter tr elements trong DOM)
 *   - Modal open / close
 *   - Edit: đọc data từ DOM attributes (data-key, data-value) do Thymeleaf sinh ra
 *   - Delete: xác nhận, sau đó reload page (khi có API thật)
 *   - Add new: mở form rỗng theo active tab
 *   - Add / Edit form field generation (UI schema — không phải business data)
 *   - Account permissions modal
 *   - Lucide icon re-init
 */

// ─────────────────────────────────────────────────────────────────────────────
// State
// ─────────────────────────────────────────────────────────────────────────────

/** Active tab name — read from Thymeleaf data attribute on <body>. */
const activeTab = document.body.dataset.activeTab || "Room Categories";

let editingRowId = null;
let deletingRowId = null;
let permissionsUserId = null;

// ─────────────────────────────────────────────────────────────────────────────
// Init
// ─────────────────────────────────────────────────────────────────────────────

document.addEventListener("DOMContentLoaded", () => {
    // Sidebar toggle
    const toggleBtn = document.getElementById("master-data-toggle");
    if (toggleBtn) toggleBtn.addEventListener("click", handleSidebarToggle);

    // Search
    const searchInput = document.getElementById("search-input");
    if (searchInput) searchInput.addEventListener("input", (e) => handleSearch(e.target.value));

    // Add new button
    const addBtn = document.getElementById("btn-add-new");
    if (addBtn) addBtn.addEventListener("click", openAddModal);

    // Modal close buttons (all buttons with .btn-close-modal)
    document.querySelectorAll(".btn-close-modal").forEach(btn => {
        btn.addEventListener("click", () => closeModal(btn.dataset.modal));
    });

    // Close modal on backdrop click
    ["entity-modal", "delete-modal", "permissions-modal"].forEach(id => {
        const el = document.getElementById(id);
        if (el) el.addEventListener("click", () => closeModal(id));
    });

    // Delete confirm button
    const confirmDeleteBtn = document.getElementById("btn-confirm-delete");
    if (confirmDeleteBtn) confirmDeleteBtn.addEventListener("click", handleConfirmDelete);

    // Save permissions button
    const savePermsBtn = document.getElementById("btn-save-permissions");
    if (savePermsBtn) savePermsBtn.addEventListener("click", handleSavePermissions);

    // Entity form submit
    const entityForm = document.getElementById("entity-form");
    if (entityForm) entityForm.addEventListener("submit", handleFormSubmit);

    // Table action buttons (edit, delete, permissions)
    wireTableButtons();

    // Init icons
    if (typeof lucide !== "undefined") lucide.createIcons();
});

// ─────────────────────────────────────────────────────────────────────────────
// Sidebar submenu
// ─────────────────────────────────────────────────────────────────────────────

function handleSidebarToggle(event) {
    event.preventDefault();
    const submenu = document.getElementById("master-data-submenu");
    const arrow = document.getElementById("master-data-arrow");
    if (!submenu || !arrow) return;
    const isOpen = submenu.classList.toggle("open");
    arrow.classList.toggle("open", isOpen);
    arrow.innerHTML = `<i data-lucide="${isOpen ? "chevron-down" : "chevron-right"}"></i>`;
    if (typeof lucide !== "undefined") lucide.createIcons();
}

// ─────────────────────────────────────────────────────────────────────────────
// Client-side search (filter on Thymeleaf-rendered rows)
// ─────────────────────────────────────────────────────────────────────────────

function handleSearch(query) {
    const q = query.toLowerCase().trim();
    const totalEl = document.getElementById("pagination-info");
    let visible = 0;
    let total = 0;

    document.querySelectorAll("tbody tr[data-id]").forEach(tr => {
        total++;
        const match = !q || tr.textContent.toLowerCase().includes(q);
        tr.style.display = match ? "" : "none";
        if (match) visible++;
    });

    if (totalEl) {
        totalEl.textContent = `Hiển thị ${visible} / ${total} bản ghi`;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Modal helpers
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

// ─────────────────────────────────────────────────────────────────────────────
// Wire table action buttons (edit / delete / permissions)
// ─────────────────────────────────────────────────────────────────────────────

function wireTableButtons() {
    document.querySelectorAll(".btn-edit").forEach(btn => {
        btn.addEventListener("click", () => openEditModal(btn.dataset.id));
    });
    document.querySelectorAll(".btn-delete").forEach(btn => {
        btn.addEventListener("click", () => openDeleteConfirm(btn.dataset.id));
    });
    document.querySelectorAll(".btn-permissions").forEach(btn => {
        btn.addEventListener("click", () => openPermissionsModal(btn.dataset.id, btn.dataset.name, btn.dataset.role));
    });
}

// ─────────────────────────────────────────────────────────────────────────────
// Add / Edit modal
// ─────────────────────────────────────────────────────────────────────────────

function openAddModal() {
    editingRowId = null;
    const title = document.getElementById("modal-title");
    if (title) title.innerText = `Thêm mới ${activeTab}`;

    const form = document.getElementById("entity-form");
    if (form) {
        form.reset();
        form.dataset.action = '';
        form.dataset.method = 'POST';
    }

    // Gắn sự kiện hiển thị loại tài khoản nếu ở tab Account Management
    if (activeTab === "Account Management") {
        const typeSelect = document.getElementById('account-type-select');
        if (typeSelect) {
            Array.from(typeSelect.options).forEach(opt => {
                if (opt.value === 'Khách hàng') {
                    opt.style.display = 'none';
                    opt.disabled = true;
                }
            });
            typeSelect.value = 'Nhân viên';
            // Prevent changing type since it's the only valid one for new accounts
            typeSelect.style.pointerEvents = 'none';
            typeSelect.style.backgroundColor = '#f3f4f6';
        }
    }

    initAccountFormToggles();
    openModal("entity-modal");
}

function openEditModal(id) {
    editingRowId = id;
    const title = document.getElementById("modal-title");
    if (title) title.innerText = `Chỉnh sửa ${activeTab}`;

    const tr = document.querySelector(`tr[data-id="${id}"]`);
    if (!tr) return;

    const entityJson = tr.getAttribute('data-entity');
    const entityData = entityJson ? JSON.parse(entityJson) : {};

    const form = document.getElementById("entity-form");
    if (form) {
        form.reset();

        if (activeTab === "Account Management") {
            const typeSelect = document.getElementById('account-type-select');
            if (typeSelect) {
                Array.from(typeSelect.options).forEach(opt => {
                    if (opt.value === 'Khách hàng') {
                        opt.style.display = 'block';
                        opt.disabled = false;
                    }
                });
                // Restore styling so it's not disabled
                typeSelect.style.pointerEvents = 'auto';
                typeSelect.style.backgroundColor = '';
            }
        }

        for (const [key, value] of Object.entries(entityData)) {
            const input = form.elements[key];
            if (input) {
                if (input.type === "checkbox") {
                    input.checked = (value === "true" || value === true);
                } else {
                    input.value = value;
                }
            }
        }
        form.dataset.action = `/admin/api/v1/accounts/${id}`;
        form.dataset.method = "PUT";
    }

    initAccountFormToggles();
    openModal("entity-modal");
}

function handleFormSubmit(event) {
    event.preventDefault();
    const isEdit = editingRowId !== null;
    const form = event.target;

    // Disable submit button
    const submitBtn = form.querySelector('button[type="submit"]');
    if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.textContent = 'Đang xử lý...';
    }

    // Generic form submission logic
    let endpoint = form.dataset.action || '';
    let method = form.dataset.method || 'POST';

    // Fallback for Account Management tab if data-action wasn't explicitly set
    if (activeTab === "Account Management" && !endpoint) {
        endpoint = isEdit ? `/admin/api/v1/accounts/${editingRowId}` : '/admin/api/v1/accounts';
    }

    if (endpoint) {
        const formData = new FormData(form);
        const payload = Object.fromEntries(formData.entries());
        // Fix checkbox booleans
        form.querySelectorAll('input[type="checkbox"]').forEach(cb => {
            payload[cb.name] = cb.checked;
        });

        fetch(endpoint, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        })
            .then(res => {
                if (!res.ok) throw new Error("Lỗi API: " + res.status);
                return res.json();
            })
            .then(data => {
                closeModal("entity-modal");
                window.location.reload();
            })
            .catch(err => {
                console.error("API Error: ", err);
                alert("Lỗi khi lưu: " + err.message);
                if (submitBtn) {
                    submitBtn.disabled = false;
                    submitBtn.textContent = 'Lưu';
                }
            });
        return;
    }

    // Default flow if no endpoint
    closeModal("entity-modal");
    editingRowId = null;

    if (typeof showToast === "function") {
        showToast(isEdit ? "Cập nhật thành công!" : "Thêm mới thành công!", "success");
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Delete modal
// ─────────────────────────────────────────────────────────────────────────────

function openDeleteConfirm(id) {
    deletingRowId = id;
    openModal("delete-modal");
}

function handleConfirmDelete() {
    if (deletingRowId) {
        const tr = document.querySelector(`tr[data-id="${deletingRowId}"]`);
        if (tr) tr.remove();
        handleSearch(document.getElementById("search-input")?.value || "");
    }
    closeModal("delete-modal");
    deletingRowId = null;
    if (typeof showToast === "function") showToast("Đã xóa bản ghi!", "success");
}

// ─────────────────────────────────────────────────────────────────────────────
// Form field loading — UI schema (not business data)
// ─────────────────────────────────────────────────────────────────────────────

// loadFormFields deleted - Forms are strictly server-side rendered by Thymeleaf

// Helper to toggle employee fields visibility on static Thymeleaf forms
function initAccountFormToggles() {
    if (activeTab !== "Account Management") return;
    const typeSelect = document.getElementById('account-type-select');
    const roleSelect = document.getElementById('account-role-select');
    const empFields = document.getElementById('account-employee-fields');
    if (!typeSelect || !empFields || !roleSelect) return;

    function updateVisibility() {
        const isEmp = typeSelect.value === "Nhân viên";

        // Hiện/ẩn form fields của Employee
        empFields.style.display = isEmp ? "flex" : "none";

        // Yêu cầu (require) các trường employee-specific nếu là Nhân viên
        empFields.querySelectorAll('input').forEach(input => {
            input.required = isEmp;
        });

        // Xử lý field chung nằm ngoài
        const form = document.getElementById("entity-form");
        if (form) {
            const pwdInput = form.querySelector('input[name="password"]');
            if (pwdInput) {
                // Password bắt buộc khi thêm mới, không bắt buộc khi chỉnh sửa
                pwdInput.required = !editingRowId;
            }
        }

        // Hiện/ẩn options tùy vào Employee hay Guest, dựa trên class đã gắn sẵn (role-staff, role-guest)
        Array.from(roleSelect.options).forEach(opt => {
            if (opt.classList.contains('role-staff')) {
                opt.style.display = isEmp ? "block" : "none";
            } else if (opt.classList.contains('role-guest')) {
                opt.style.display = isEmp ? "none" : "block";
            }
        });

        // Reset giá trị selected nếu option hiện tại đang bị ẩn
        const currentOption = roleSelect.options[roleSelect.selectedIndex];
        if (currentOption && currentOption.style.display === "none") {
            const firstVisible = Array.from(roleSelect.options).find(opt => opt.style.display !== "none");
            if (firstVisible) roleSelect.value = firstVisible.value;
        }
    }

    typeSelect.removeEventListener("change", updateVisibility);
    typeSelect.addEventListener("change", updateVisibility);
    updateVisibility();
}

// ─────────────────────────────────────────────────────────────────────────────
// Permissions modal
// ─────────────────────────────────────────────────────────────────────────────

const PERMISSION_LABELS = {
    dashboard: "Dashboard Panel",
    masterData: "Master Data Management",
    auditLog: "Audit Log Monitor",
    reviews: "Review Management",
    booking: "Booking Management",
    fnb: "F&B Management",
    housekeeping: "Housekeeping",
    maintenance: "Maintenance Panel"
};

let localPermissions = {};

function openPermissionsModal(id, name, role) {
    permissionsUserId = id;

    const titleEl = document.getElementById("permissions-modal-title");
    const descEl = document.getElementById("permissions-desc");
    if (titleEl) titleEl.innerText = `Cấp quyền - ${name}`;
    if (descEl) descEl.innerHTML = `Quản lý quyền truy cập cho nhân viên <strong>${name}</strong> (Vai trò: ${role})`;

    // Default permissions based on role — computed from role value (server logic simulated client-side)
    localPermissions = {
        dashboard: true,
        masterData: role === "Admin" || role === "Manager",
        auditLog: role === "Admin" || role === "Manager",
        reviews: true,
        booking: role !== "Housekeeping",
        fnb: role === "F&B" || role === "Admin" || role === "Manager",
        housekeeping: role === "Housekeeping" || role === "Admin" || role === "Manager",
        maintenance: role === "Admin" || role === "Manager"
    };

    renderPermissionsList();
    openModal("permissions-modal");
}

function renderPermissionsList() {
    const container = document.getElementById("permissions-list");
    if (!container) return;
    container.innerHTML = "";

    Object.entries(localPermissions).forEach(([key, value]) => {
        const label = document.createElement("label");
        label.className = "flex items-center gap-3.5 px-4 py-3 rounded-lg bg-[#2C2A1E]/4 cursor-pointer hover:bg-primary/8 transition-colors";
        const checkbox = document.createElement("input");
        checkbox.type = "checkbox";
        checkbox.checked = value;
        checkbox.className = "w-4 h-4 cursor-pointer";
        checkbox.addEventListener("change", () => { localPermissions[key] = checkbox.checked; });
        const span = document.createElement("span");
        span.className = "font-sans text-[14px] text-[#2C2A1E] flex-1";
        span.innerText = PERMISSION_LABELS[key] || key;
        label.appendChild(checkbox);
        label.appendChild(span);
        container.appendChild(label);
    });
}

function handleSavePermissions() {
    // TODO: POST permissions to backend when API ready
    closeModal("permissions-modal");
    permissionsUserId = null;
    if (typeof showToast === "function") showToast("Lưu phân quyền thành công!", "success");
}
// ─────────────────────────────────────────────────────────────────────────────
// ACCOUNT TOGGLE STATUS
// Cập nhật UI ngay (optimistic). Khi backend sẵn sàng:
//   1. Uncomment phần fetch() bên dưới
//   2. Xóa phần "// Cập nhật UI" phía trên fetch (để tránh double-update)
// ─────────────────────────────────────────────────────────────────────────────

document.addEventListener("click", (e) => {
    const btn = e.target.closest(".btn-toggle");
    if (!btn) return;

    const id = btn.dataset.id;
    const current = btn.dataset.value === "true";
    const next = !current;

    // ── Cập nhật UI ngay (không cần API) ─────────────────────────────────────
    btn.dataset.value = String(next);

    const icon = btn.querySelector("i");
    if (icon) {
        icon.setAttribute("data-lucide", next ? "toggle-right" : "toggle-left");
        icon.className = `w-[22px] h-[22px] ${next ? "text-[#C9A96E]" : "text-[#8B7355]"}`;
        if (typeof lucide !== "undefined") lucide.createIcons();
    }

    // Đồng bộ data-value trên <td> để edit modal đọc đúng
    const td = btn.closest("td[data-key]");
    if (td) td.dataset.value = String(next);

});

// ─────────────────────────────────────────────────────────────────────────────
// Image Upload Handlers
// ─────────────────────────────────────────────────────────────────────────────

function handleSingleImageUpload(input) {
    const file = input.files[0];
    if (file) {
        const reader = new FileReader();
        reader.onload = function (e) {
            const preview = input.parentElement.querySelector('.image-preview');
            const placeholder = input.parentElement.querySelector('.upload-placeholder');
            if (preview) {
                preview.src = e.target.result;
                preview.style.display = 'block';
            }
            if (placeholder) {
                placeholder.style.display = 'none';
            }
        };
        reader.readAsDataURL(file);
    }
}

function handleGalleryUpload(input) {
    if (!input.files || input.files.length === 0) return;

    const container = input.closest('.gallery-upload-container');
    const previewList = container.querySelector('.gallery-preview-list');
    const hiddenContainer = container.querySelector('.hidden-inputs-container');

    // Tạo một input mới để thay thế input hiện tại
    const newInput = document.createElement('input');
    newInput.type = 'file';
    newInput.name = 'galleryImages';
    newInput.accept = 'image/*';
    newInput.multiple = true;
    newInput.style.display = 'none';
    newInput.onchange = function () { handleGalleryUpload(this) };

    // Đổi tên biến input cũ để tiện quản lý (mỗi input ẩn sẽ có 1 id riêng)
    const inputId = 'gallery_input_' + Date.now();
    input.id = inputId;

    // Chuyển input cũ sang vùng ẩn
    hiddenContainer.appendChild(input);
    // Gắn input mới vào nút Thêm ảnh
    container.querySelector('.gallery-upload-btn').appendChild(newInput);

    const files = Array.from(input.files);

    files.forEach((file, index) => {
        const reader = new FileReader();
        reader.onload = function (e) {
            const item = document.createElement('div');
            item.className = 'gallery-item';
            item.innerHTML = `
                <img src="${e.target.result}" alt="preview">
                <button type="button" class="remove-btn" onclick="removeGalleryItem(this, '${inputId}', ${index})" title="Xóa ảnh này">
                    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
                </button>
            `;
            previewList.appendChild(item);
        };
        reader.readAsDataURL(file);
    });
}

function removeGalleryItem(btn, inputId, fileIndex) {
    btn.parentElement.remove();
}
