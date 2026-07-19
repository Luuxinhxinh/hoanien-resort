### 3.6.3 Minibar & Damage Reporting

This section defines the components and operational behaviors for the Minibar & Amenities Check modal and the Damage Reporting modal, which are accessed from the Housekeeping Dashboard.

---

### 3.6.3.1 Minibar & Amenities Check Modal ("Kiểm tra Minibar & Tiện ích")

This modal is used by Housekeeping staff to record guest consumption of food/beverages and to log the replenishment of standard amenities.

**On this modal, staff can:**

* Record the quantity of consumed drinks or snacks for the selected room.
* Record the quantity of replenished amenities (e.g., toothbrushes, shower gel, shampoo, towels).
* Dismiss changes without saving by clicking the "Hủy" (Cancel) button.
* Save information by clicking the "Lưu thông tin" (Save Information) button, which automatically posts minibar charges to the guest's folio and updates the inventory.

| Field Name | Description |
| :--- | :--- |
| Modal Title | Header displaying "Kiểm tra Minibar & Tiện ích" and a close (X) button. Read-only. |
| Target Room Label | Instructional text displaying the target room number (e.g. "Phòng 106"). Read-only. |
| Beverages / Snacks Items | Set of number input fields to record the quantity of individual consumed minibar items (e.g. Evian Water, Coca Cola, Heineken, Potato Chips). Default is 0. Constraints: Non-negative integer. |
| Extra Amenities Items | Set of number input fields to record the quantity of replenished room amenities (e.g. Toothbrushes, Mini shower gel, Mini shampoo, Replaced bath towels). Default is 0. Constraints: Non-negative integer. |
| Hủy Button | Closes the dialog without saving any input data. |
| Lưu thông tin Button | Submits the recorded counts, updates guest folio billing charges, and logs the replenishment data. |

---

### 3.6.3.2 Damage Reporting Modal ("Báo cáo sự cố bảo trì")

This modal is used by Housekeeping staff to report mechanical, electrical, or structural facility issues identified inside a room.

**On this modal, staff can:**

* View the pre-filled target room number.
* Input a detailed description of the facility issue or broken equipment.
* Optionally upload a photograph showing the damage or issue.
* Dismiss the reporting action without submitting by clicking the "Hủy bỏ" (Cancel) button.
* Submit the report immediately by clicking the "Gửi yêu cầu ngay" (Send Request Now) button, which flags the room for maintenance.

| Field Name | Description |
| :--- | :--- |
| Modal Title | Header displaying "Báo cáo sự cố bảo trì" and a close (X) button. Read-only. |
| Phòng đang xảy ra sự cố | Text field displaying the target room number (e.g. "Phòng 106"). Read-only. |
| Mô tả sự cố gặp phải | Text area field to type a detailed description of the incident. Placeholder: "Ví dụ: Tivi không lên nguồn, vòi hoa sen bị rỉ nước...". Required: Yes. |
| Hình ảnh sự cố (Tùy chọn) | File upload input field ("Chọn tệp") to optionally attach a photo of the damaged asset. Optional. |
| Hủy bỏ Button | Closes the dialog without submitting the incident report. |
| Gửi yêu cầu ngay Button | Submits the report, automatically creates a Maintenance task, and updates the room status on the grid. |
