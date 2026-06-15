document.addEventListener("DOMContentLoaded", () => {
  document.querySelectorAll("[data-image-field]").forEach((group) => {
    const input = group.querySelector("[data-image-input]");
    const placeholder = group.querySelector("[data-image-placeholder]");
    const output = group.querySelector("[data-image-output]");
    if (!input || !placeholder || !output) return;

    input.addEventListener("change", () => {
      const file = input.files && input.files[0];
      if (!file) {
        output.hidden = true;
        placeholder.hidden = false;
        output.src = "";
        return;
      }

      const reader = new FileReader();
      reader.onload = () => {
        output.src = String(reader.result || "");
        output.hidden = false;
        placeholder.hidden = true;
      };
      reader.readAsDataURL(file);
    });
  });
});
