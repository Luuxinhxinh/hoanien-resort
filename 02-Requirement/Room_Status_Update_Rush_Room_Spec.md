### 3.6.2 Room Status Update & Rush Room

This section defines the components and operational flow for the Room Status Update & Rush Room features, detailing how real-time room cleaning states and urgent flags are synchronized between the Housekeeping Dashboard and the Room Grid Matrix.

---

### 3.6.2.1 Housekeeping Dashboard — Urgent Room Cards

This card represents a physical room assigned to housekeeping with an active "Rush Room" (Urgent Clean) request.

**On this screen, users can:**

* Identify priority cleaning tasks via the highlighted dark red left-border and the "Khấn cấp" (Urgent) badge.
* Read the urgent cleaning notification "Lễ tân báo dọn khẩn" accompanied by a bolt icon.
* View specific operational instructions or VIP notes inside the task detail box (e.g., "ARRIVAL Khách VIP sắp nhận phòng...").
* Start a cleaning task by clicking the "Bắt đầu" (Start) button, which changes the status to "Đang dọn" (In Progress).
* Complete an active cleaning task by clicking the "Hoàn thành" (Complete) button.
* Report room facility damage (click "Báo sự cố") or record minibar beverage consumption (click "Minibar").

| Field Name             | Description                                                                                                                  |
| :--------------------- | :--------------------------------------------------------------------------------------------------------------------------- |
| Room Number & Category | Displays the physical room identifier and category (e.g. "203 River Pool Villa"). Read-only.                                 |
| Urgent Badge           | Highlighted red badge displaying "Khấn cấp" to denote a high-priority cleaning request. Read-only.                         |
| Status Badge           | Displays the current cleaning progress state (e.g., "Đang dọn" in orange). Read-only.                                      |
| Urgent Title           | Red warning label displaying "Lễ tân báo dọn khẩn" with a bolt icon. Read-only.                                         |
| Instruction Note Box   | Card body container displaying custom notes (e.g., "ARRIVAL Khách VIP...", "Lễ tân yêu cầu dọn phòng..."). Read-only. |
| Báo sự cố Button    | Button that opens the damage reporting modal to record maintenance issues.                                                   |
| Minibar Button         | Button that opens the minibar check modal to record guest consumption.                                                       |
| Bắt đầu Button      | Submit button displayed on pending cards. Changes task status to InProgress, starting the timer.                             |
| Hoàn thành Button    | Submit button displayed on in-progress cards. Completes the task and marks the room as clean.                                |

---

### 3.6.2.2 Room Grid Matrix — Live Status View

This screen provides front desk receptionists and managers with a visual, real-time matrix of all physical rooms categorized by their respective types.

**On this screen, users can:**

* View the real-time occupancy and maintenance state of each room based on color-coded borders and backgrounds.
* Track active housekeeping tasks via the top-right blue or yellow broom icons.
* Track active maintenance tasks via the top-right and bottom-right orange wrench icons.
* Identify rooms that have active minibar logs or food service orders via the bottom-right chicken/food icon.
* Monitor which occupied rooms have urgent dọn khẩn (rush clean) status (e.g., Room 106, Room 203).

| Field Name                       | Description                                                                                                                                                           |
| :------------------------------- | :-------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Room Grid Matrix Header          | Displays the section title "Room Grid Matrix / Sơ đồ phòng — Live status", a status legend, and the total category count. Read-only.                             |
| Legend Indicator                 | Color-coded dots showing status definitions: Green (Available), Dark Blue (Occupied), Yellow (Housekeeping), Red (Maintenance). Read-only.                            |
| Ready Card (Available)           | Room card styled with a green border, a green dot at the top-right, and the status label "Ready" (e.g., Room 101). Read-only.                                         |
| Needs Cleaning Card              | Room card styled with a yellow border, a yellow broom icon at the top-right, and the status label "Needs cleaning" (e.g., Room 102). Read-only.                       |
| Maintenance Card (Vacant)        | Room card styled with a red border, a red wrench icon at the top-right, and the status label "Maintenance (Cần sửa)" (e.g., Room 103). Read-only.                   |
| Occupied Card (Normal)           | Room card styled with a dark blue background, a yellow dot at the top-right, and the status label "Occupied" (e.g., Room 201). Read-only.                             |
| Occupied (Dọn khẩn) Card       | Room card styled with a dark blue background, a blue broom icon at the top-right, and the status label "Occupied (Dọn khẩn)" (e.g., Room 106, Room 203). Read-only. |
| Occupied (Cần sửa) Card        | Room card styled with a dark blue background, an orange wrench icon at the top-right, and the status label "Occupied (Cần sửa)" (e.g., Room 105). Read-only.        |
| Maintenance Icon (Bottom-Right)  | Wrench icon indicating that a facility damage has been reported for the room and a maintenance request is active. Read-only.                                          |
| Food/Minibar Icon (Bottom-Right) | Icon indicating that guest dining or minibar item consumption has been logged for the room. Read-only.                                                                |
