### 3.4.1 Tour Catalog & Tour Detail (Public)

This screen allows guests and visitors to explore the variety of tour packages offered by HoaNien Retreat through a cinematic interface and view the details and itineraries of each tour.

**On this screen, guests can:**

* Explore the Tour Catalog through a cinematic, full-screen vertical scrolling experience (scroll-snap).
* View the introductory hero section ("Dư âm tĩnh lặng") that sets the overall theme and mood of the HoaNien Retreat tours.
* Navigate seamlessly between different tour packages (e.g., Đoàn Tụ, Đồng Nội, Tĩnh Lặng, v.v.) using the horizontal sub-navigation bar or the intuitive pagination slider (e.g., 01, 02, 03...10) at the bottom.
* Read a short description and view a background image for each specific tour in the slider.
* Click the "Khám phá hành trình" or "Tìm về tĩnh lặng" button to proceed to the Tour Detail page.
* On the Tour Detail page, guests can read the comprehensive itinerary, check available dates, select the number of participants, configure travel insurance options, and proceed with the booking and room assignment validation process.

| Field Name | Description |
| :--- | :--- |
| Main Navigation Bar | The transparent top menu for global navigation (Living, Dining, Experiences, Tours, Feedback). Always accessible. |
| Sub-Navigation Bar | A horizontal list of quick links to specific tour packages (Đoàn Tụ, Đồng Nội, Di Sản...). Clicking a link scrolls smoothly to that tour's slide. |
| Hero Intro Section | Full-screen landing section with the collection's title ("Dư âm tĩnh lặng") and a "Khám phá hành trình" call-to-action that scrolls down to the tour viewer. |
| Journey Viewer (Slider) | A dynamic full-screen section that displays one tour at a time. It updates the background image, title, and description seamlessly when navigating. |
| Tour Title & Description | A poetic summary of the currently selected tour (e.g., "Tĩnh lặng liên hoa - Tháp Mười"). |
| CTA Button | Action button (e.g., "Tìm về tĩnh lặng") that directs the guest to the detailed itinerary and booking page (/tours/detail). |
| Pagination Controls | Numbered navigation (01, 02, 03...) and left/right arrows to manually swipe through the 10 available tour packages. |
| Email Subscription | Input field and "Đăng ký" button at the end of the catalog to register for updates or newsletters. |
| Departure Date Picker | A selector field on the Tour Detail page containing all available dates for the selected tour. |
| Participant Counters | Dynamic increment/decrement counters for the number of Adults and Children. |
| Travel Insurance Checkbox | A checkbox to toggle the addition of travel insurance (50,000 VNĐ per person) with live total price updates. |
| Live Total Price Display | A text area displaying the updated total calculated price in real-time as date, participants, or insurance are modified. |
| Book Tour Button | A CTA button to submit the selected booking configuration and redirect to the Passenger Information & Payment page. |

---

### 3.4.2 Tour Booking & Payment

This screen is used by guests to fill in passenger information and perform online payment or assign the transaction to their active room folio.

**On this screen, guests can:**

* Prefill the main customer information automatically on their first tour registration of the day.
* Input individual details (Full Name, Contact Phone, CCCD/Passport, Birthdate) for all accompanying companions.
* View a "Booking on behalf" warning popup if they have registered for this tour on the same departure date before. Under this mode, the system leaves the first passenger's fields blank and generates full input fields for all registered guests.
* Select a payment gateway option (VNPay Online Payment or Post to Room folio billing).
* Input Room Number and personal room secure PIN to authenticate room folio charging.
* Review room capability alerts that prevent submission if the participant count exceeds the room's remaining maximum capacity.
* Verify payment details and receive booking confirmation notifications via email.

| Field Name | Description |
| :--- | :--- |
| Behalf Booking Warning | A popup modal warning the guest about duplicate tour bookings: "Bạn đã đăng ký tour này trước đó. Bạn đang đặt hộ cho người khác?". Clicking "Xác nhận" initializes behalf booking entry mode. |
| Passenger Details Form | Group of fields for each passenger to enter Full Name, Phone (falls back to main customer's phone if empty), CCCD/Passport (automatically generates child placeholders for users under 14), and Birthdate. |
| Payment Gateway Options | Radio buttons to select payment method between "VNPay Gateway" or "Post to Room". |
| Room Number Field | Numeric input field to enter the guest's physical room number (e.g., 102) for room billing. |
| Secure Room PIN | Password field to enter the 4-6 digit security PIN associated with the room for authorization. |
| Credit Limit Status Badge | Display showing the remaining credit limit of the room to ensure sufficient funds. |
| Capacity Alert Box | Real-time warning box preventing payment execution if guest count exceeds the current room capacity quota. |
| Pay & Confirm Button | Submits the form data, initiates VNPay gateway redirection, or executes room folio charging backend validations. |

---

### 3.4.3 Tour Guide Dashboard & Itinerary

This screen is used by Tour Guides to manage their assigned daily tours, view attendee rosters, read special logistic notes, and report emergency incidents.

**On this screen, Tour Guides can:**

* View their daily assigned schedule (Tour Name, Departure Time, Destination, and Current Status).
* Track real-time attendance stats (e.g., "12 / 15 Passengers Checked In").
* Read consolidated booking special notes (dietary restrictions, medical warnings, allergies).
* Access the Tour Handbook containing itinerary guides, landmark explanations, and emergency procedures.
* Write and submit emergency incident reports to the resort managers.
* Request immediate tour cancellation and automatic refund routing in case of critical force majeure events (e.g., weather alerts).

| Field Name | Description |
| :--- | :--- |
| Guide Info Banner | Header displaying the active guide's full name, employee ID, and contact details. |
| Active Schedule Card | Card component displaying the name of the tour, time of departure, and color-coded status badge (Not Started, In Progress, Completed). |
| Headcount Progress Bar | Visual gauge tracking the number of checked-in attendees relative to the total registered passengers. |
| Special Notes Container | Scrollable section compiling all customer special requests (e.g. Vegetarian diet, shell-fish allergy). |
| Handbook Logistics Tab | Document section containing target itinerary landmarks, details of activities, and safety codes. |
| Incident Report Input | Textarea field to record details of route accidents, vehicle failures, or weather disturbances. |
| Force Majeure Cancel Checkbox | Toggle checkbox to declare tour cancellation, which triggers automatic 100% refund logic on backend. |
| Submit Report Button | Sends the incident log directly to the Manager's dashboard alerts. |

---

### 3.4.4 FaceID Attendance Check

This screen is used by Tour Guides to automatically check in passengers via facial recognition using live device camera feeds or manually override status.

**On this screen, Tour Guides can:**

* Start the device camera stream inside a circular camera frame (Squircle) to begin scanning.
* Detect and recognize passenger faces in real-time, matching them with local reference descriptors fetched from the server.
* Receive success popup alerts when a face matches with high confidence (Euclidean distance < 0.45).
* Perform manual check-in overrides for passengers who fail to match due to low lighting or accessories.
* Mark passengers as "Absent" and enter the specific reason in a popup modal.
* Reset the entire attendee list status back to "Not Checked In" if needed.
* Click on a passenger card to open the Customer Contact Modal and view detailed profile information.

| Field Name | Description |
| :--- | :--- |
| Camera Squircle Viewport | Live video feed showing the scanning camera. Draws bounding boxes around detected faces. |
| Scan Status Indicator | Label showing current status: "READY", "LOADING MODELS...", "AUTHENTICATION SUCCESSFUL". |
| Attendee List Table | Roster showing all passenger names, ticket IDs, check-in status (Checked-In, Absent, Pending), and manual action buttons. |
| Manual Check-In Button | A green checkmark button to manually mark a guest as present. |
| Mark Absent Button | A red cross button that opens the absent reason popup modal. |
| Reset Attendance Button | Button to reset all check-in logs for the current tour guide session. |
| Absent Reason Modal | Input field to submit explanation of passenger absence (e.g., "Missed bus", "Illness"). |
| Customer Contact Modal | Modal displaying decrypted CCCD/Passport, calculated Age, Phone number, Email, and physical Room Number (retrieved from active room folio details). Includes quick-action call/chat buttons. |

---

### 3.4.5 Review & Feedback

This screen is used by guests to submit ratings and written reviews about their resort stay or tour experience.

**On this screen, guests can:**

* Rate their overall experience with the resort on a scale of 1 to 5 stars.
* Rate the tour guide's service quality and attitude on a scale of 1 to 5 stars.
* Enter descriptive feedback comments (up to 1000 characters) in the review text area.
* Submit the feedback form and receive a success/thank-you message.

| Field Name | Description |
| :--- | :--- |
| Resort Service Rating | Dynamic 5-star interactive selector to evaluate the resort. Binds a value from 1 to 5. |
| Tour Guide Rating | Dynamic 5-star interactive selector to evaluate guide performance (active only for tour reviews). |
| Review Textarea | Text field to input detailed qualitative feedback. Restriced to 1000 characters maximum. |
| Character Counter | Label displaying remaining characters allowed (e.g., "950 / 1000"). |
| Submit Review Button | Validates star input requirements and uploads the feedback database entry. |
