document.addEventListener("DOMContentLoaded", function() {
    document.getElementById("svc-form").addEventListener("submit", function(e) {
        e.preventDefault();
        const id = document.getElementById("svc-id").value;
        const payload = {
            serviceName: document.getElementById("svc-name").value.trim(),
            basePrice: document.getElementById("svc-price").value,
            sourceDepartment: document.getElementById("svc-dept").value,
            description: document.getElementById("svc-desc").value.trim(),
            isAvailable: document.getElementById("svc-avail").checked
        };
        fetch("/api/v1/add-ons", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(payload)
        }).then(r => {
            if (!r.ok) throw new Error("Lưu thất bại");
            location.reload();
        }).catch(err => alert(err.message));
    });
});
function openCreateModal() {
    document.getElementById("svc-id").value = "";
    document.getElementById("svc-name").value = "";
    document.getElementById("svc-price").value = "";
    document.getElementById("svc-dept").value = "SPA";
    document.getElementById("svc-desc").value = "";
    document.getElementById("svc-avail").checked = true;
    document.getElementById("modal-title").textContent = "Thêm dịch vụ Add-on";
    document.getElementById("svc-modal").style.display = "flex";
}
function editSvc(btn) {
    const row = btn.closest("tr");
    const cells = row.querySelectorAll("td");
    document.getElementById("svc-id").value = cells[0].textContent.trim();
    document.getElementById("svc-name").value = cells[1].textContent.trim();
    document.getElementById("svc-price").value = cells[2].textContent.trim();
    document.getElementById("svc-desc").value = cells[4].textContent.trim();
    document.getElementById("svc-avail").checked = cells[5].textContent.includes("Có");
    document.getElementById("modal-title").textContent = "Sửa dịch vụ Add-on";
    document.getElementById("svc-modal").style.display = "flex";
}
function closeModal() {
    document.getElementById("svc-modal").style.display = "none";
}
