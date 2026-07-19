### 3.6.4 Maintenance Ticket Management

This screen is used by Maintenance staff to manage, track, and execute room repair and maintenance operations in real-time.

**On this screen, the Maintenance Staff can:**

* View a list of repair tasks categorized by progress status (TODO, In Progress, Done).
* Identify urgent requests via the flashing red SLA timer.
* Claim a pending task.
* Pause an active task with a mandatory reason.
* Complete an active task with optional remarks.
* Resume a paused task.

| Field Name | Description |
| :--- | :--- |
| Room Number | Displays the target room number (e.g. "Phòng 207") and category tag. |
| SLA Timer | Live count of elapsed wait time. Flashes red when SLA limit is exceeded. |
| Nhận việc | Button. Claims the task, changing status to InProgress. |
| Tạm dừng | Button. Opens the Pause Modal to enter the pause reason. |
| Xong | Button. Opens the Complete Modal to confirm completion. |
| Tiếp tục sửa | Button. Resumes the paused task, returning its status to InProgress. |
| Lý do tạm dừng | Text input. Input the reason for pausing (inside Pause Modal). Required. |
| Lưu trạng thái | Button. Saves the pause reason and sets status to Paused. |
| Ghi chú đã sửa chữa | Text input. Input repair remarks (inside Complete Modal). Optional. |
| Hoàn thành phiếu | Button. Completes the task and releases the room. |
