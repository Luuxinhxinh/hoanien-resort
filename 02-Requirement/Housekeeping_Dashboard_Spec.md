### 3.1.2 Housekeeping Dashboard

This screen is used by Housekeeping staff to manage, track, and execute room cleaning operations, record minibar consumption, and report maintenance issues in real-time.

**On this screen, Housekeeping Staff can:**

* View the overall work status of the shift (To Do, In Progress, Done).
* Track the completion progress bar for assigned rooms (e.g., "Đã hoàn thành 3 / 6 phòng").
* Filter the list of rooms using tabs: "Tất cả nhiệm vụ", "Cần hoàn thành", and "Đã hoàn thành".
* Identify urgent cleaning requests marked by Receptionists (labeled "Lễ tân báo dọn khẩn").
* Start a cleaning task (click "Bắt đầu") to change its status to In Progress.
* Complete a cleaning task (click "Hoàn thành") to mark the room as clean.
* Report room device damage or maintenance issues (click "Báo sự cố") which automatically creates a maintenance request.
* Record minibar items consumed by guests (click "Minibar") to post charges directly to the room folio.
* Log out of the system safely (click "Đăng xuất").

| Field Name | Description |
| :--- | :--- |
| Brand Logo / Header | Static brand identity displaying the resort brand "Hoanien Retreat" and the department name "HOUSEKEEPING". |
| User Profile Card | Displays logged-in staff's username ("housekeep1") and department name ("Bộ phận Buồng phòng"). |
| Logout Button ("Đăng xuất") | Button that terminates the current session and redirects the staff back to the login screen. |
| Progress Bar | Visual progress bar showing the count and percentage of completed tasks (e.g. "Đã hoàn thành 3 / 6 phòng"). |
| Cần dọn (To Do) Card | Stat card displaying the total count of pending room cleaning tasks for the shift. |
| Đang dọn (In Progress) Card | Stat card displaying the total count of rooms currently in progress of being cleaned. |
| Hoàn thành (Done) Card | Stat card displaying the total count of successfully cleaned rooms. |
| Task Filter Tabs | Tab navigation buttons ("Tất cả nhiệm vụ", "Cần hoàn thành", "Đã hoàn thành") used to filter the task grid dynamically. |
| Room Identity | Displays the physical room number (e.g. "203") and category name (e.g. "River Pool Villa"). |
| Urgent Text ("Lễ tân báo dọn khẩn") | Urgent notification line with a bolt icon, styled in a dark red color (`--urgent-color`), showing that receptionist requested emergency cleaning. |
| Báo sự cố Button | Button that opens the damage reporting modal to upload an issue description, priority, and optional damage photo. |
| Minibar Button | Button that opens the minibar recording modal to check and submit consumed beverage/food item counts. |
| Bắt đầu Button | Button displayed on pending task cards to initiate cleaning. Updates task status to "InProgress" and starts the timer. |
| Hoàn thành Button | Button displayed on in-progress task cards that opens a completion notes modal to mark the room clean. |
