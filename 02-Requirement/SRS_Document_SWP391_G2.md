# SOFTWARE REQUIREMENT SPECIFICATION

## Kawai Resort & Tour Hub

**Group 2 — SE2003-NET**

**Lecturer:** Nguyễn Mạnh Cường

*– Hanoi, May 2026 –*

---

## Table of Contents

- [I. Record of Changes](#i-record-of-changes)
- [II. Software Requirement Specification](#ii-software-requirement-specification)
  - [1. Overall Requirements](#1-overall-requirements)
    - [1.1 Context Diagram](#11-context-diagram)
    - [1.2 Main Business Processes](#12-main-business-processes)
    - [1.3 User Requirements](#13-user-requirements)
    - [1.4 System Functionalities](#14-system-functionalities)
    - [1.5 Entity Relationship Diagram](#15-entity-relationship-diagram)
  - [2. Use Case Specifications](#2-use-case-specifications)
  - [3. Functional Requirements](#3-functional-requirements)
  - [4. Non-Functional Requirements](#4-non-functional-requirements)
  - [5. Requirement Appendix](#5-requirement-appendix)

---

## I. Record of Changes

| Date | A* | M, D | In charge | Change Description |
|---|---|---|---|---|
| 24/5/2026 | A | | Team | Added Context Diagram, User Requirement (Actors), User Case Diagram, Entity Relationship Diagram |
| 25/5/2026 | A | | Team | Added System Functionalities |
| 27/5/2026 | A | | Team | Added User Requirement (Use Cases) |
| 28/5/2026 | A | | Team | Added Entities Description |
| 30/5/2026 | M | | Team | Updated Use Case Diagram |
| 1/6/2026 | M | | Team | Updated Use Case Diagram and Screens |
| 2/6/2026 | M | | Team | Updated Context Diagram and Use Case Diagram |
| 2/6/2026 | A | | Team | Added Use Case Specifications |
| 3/6/2026 | A | | | Added Main Business Processes, UC7-15, Business Rules |
| 3/6/2026 | D | | | Deleted Entity Relationship Diagram |

> *A - Added, M - Modified, D - Deleted*

---

## II. Software Requirement Specification

### 1. Overall Requirements

#### 1.1 Context Diagram

> URL: Context Diagram

#### 1.2 Main Business Processes

##### 1.2.1 BookRoom

| Step # | Step Name | Detailed Description | Role | Note |
|---|---|---|---|---|
| 1 | Search Available Rooms | **Activity:** Guest filters room categories by check-in/out dates, guest count, and requirements. System validates availability and calculates real-time pricing. **Input:** Search criteria (dates, capacity, category). **Output:** List of available room categories with descriptions and base prices. | Guest / Customer | Searching can be performed without authentication. |
| 2 | Book Room & Pay Deposit | **Activity:** Customer selects rooms, confirms stay details, and initiates payment. System processes deposit through VNPay Sandbox to secure the booking. **Input:** Booking details, guest information, selected rooms. **Output:** Confirmed booking record, deposit transaction receipt, booking status updated to Confirmed. | Customer | If deposit payment is not received within 15 minutes, the booking is automatically cancelled (BR-FO-02) |
| 3 | Check-in & Allocation | **Activity:** Receptionist verifies guest identity (ID/Passport), confirms payment, assigns a physical room, and configures spending authorization (Credit Limit/PIN). **Input:** Booking reference, identification documents. **Output:** Assigned room number, updated Room/Booking status to Checked_In, active guest folio. | Receptionist | Primary guest must be at least 18 years old (BR-FO-03). |
| 4 | Check-out & Final Invoice | **Activity:** Receptionist reviews all folio charges (room, F&B, tours), applies any promotion codes, processes final payment, and issues a consolidated invoice. **Input:** Outstanding folio items, final payment method. **Output:** Consolidated invoice, payment confirmation, room status set to Vacant_Dirty, automatic trigger of cleaning task. | Receptionist | Checkout is prohibited unless all outstanding balances are settled (BR-FIN-01). |

#### 1.3 User Requirements

##### 1.3.1 Actors

| # | Actor | Description |
|---|---|---|
| 1 | Administrator | System administrator. Manages system configurations, user accounts, and monitors overall system operations |
| 2 | Guest | Unauthenticated user. Can browse system information, view room details, and check availability, but cannot book rooms or access personalized features. |
| 3 | Customer | Registered and authenticated user. Can browse information, book rooms, tours, and services, manage booking history, leave reviews, and update their personal profile. |
| 4 | F&B Staff | Hotel Food & Beverage staff. Manages food and beverage orders, processes payments, and updates order statuses for restaurant or room service requests. |
| 5 | Receptionist | Front desk staff. Responsible for managing guest reservations, handling check-in and check-out processes, assigning rooms, and generating operational reports. |
| 6 | HouseKeeping | Room attendant and cleaning staff. Responsible for viewing assigned cleaning tasks, updating room cleanliness statuses, and reporting maintenance issues. |
| 7 | Tour Guide | Tour leader or guide. Responsible for viewing assigned tour schedules, itineraries, and managing guest attendance for the tours. |
| 8 | Maintainer | Maintainer and technical staff. Responsible for viewing reported facility maintenance requests and updating the status of technical issues after fixed. |

##### 1.3.2 Use Cases (UC)

| ID | Use Case Feature | Use Case Description |
|---|---|---|
| 01 | Login System | **Authentication** — All users (Administrator, Manager, F&B Staff,...) log into the system using their assigned credentials. |
| 02 | Register Account | **Authentication** — Allows public guests to create a new customer account on the website portal. |
| 03 | Verify Authentication via OTP | **Authentication** — Enhances security by requiring an OTP or Email Code verification during login or key actions. |
| 04 | Reset Password | **Authentication** — Allows users who forgot their password to safely request a reset link or code via registered email. |
| 05 | Manage Profiles & Upload Identity Documents | **User Profiles** — Customers manage personal profiles and upload encrypted photos of National ID/Passport for legal compliance. |
| 06 | Upgrade Dependent to Customer | **User Profiles** — Receptionists upgrade checked-in dependents (≥18 years old) to independent Customer accounts without losing history. |
| 07 | Request Personal Data Deletion | **Data Protection** — Allows customers to exercise their "Right to Deletion" by requesting data anonymization after checking out. |
| 08 | Manage Staff Accounts & Roles | **Master Data** — Administrators create, lock, or update employee accounts and dynamically assign granular operational roles. |
| 09 | Manage Master Data | **Master Data** — Administrators perform CRUD operations on room categories, public restaurant menus, and tour catalogs. |
| 10 | Search Available Rooms | **Room Booking** — Customers filter and search for vacant rooms based on selected date range, guest count, and room type. |
| 11 | Book Room & Pay Online Deposit | **Room Booking** — Customers book multiple rooms in one order and securely process a deposit payment via VNPay Sandbox. |
| 12 | View Expected Arrivals Matrix | **Front Desk** — Receptionists monitor a daily visual dashboard of room matrices, expected arrivals, and pending checkout schedules. |
| 13 | Check-In & Allocate Physical Rooms | **Front Desk** — Receptionists execute check-in, assign specific physical room numbers, and collect temporary residence data. |
| 14 | Authorize Spend Limit | **Front Desk** — Receptionists enable sub-rooms to charge expenses directly to the master room up to a strict maximum credit limit. |
| 15 | Order Room Service | **Restaurant POS** — Checked-in customers browse the in-app dining menu and place food orders to be delivered directly to their room. |
| 16 | Order Dine-In Meal | **Restaurant POS** — F&B Staff log into restaurant POS terminals to place food and beverage orders for customers dining at tables. |
| 17 | Track Kitchen Order Ticket (KOT) | **Restaurant POS** — Kitchen staff view incoming food orders and track state lifecycle updates (Pending → Preparing → Served). |
| 18 | Flag Out-of-Stock Dishes | **Restaurant POS** — Kitchen staff mark exhausted dishes as out-of-stock, automatically greying them out on all POS terminals and customer interfaces. |
| 19 | Reserve Dining Table | **Restaurant POS** — Customers or staff reserve specific restaurant dining tables by setting dates, target times, and total party sizes. |
| 20 | Charge Meal Bill to Room | **Hospitality Finance** — F&B Staff route dining expenses directly into the guest's folio to defer payment until the final checkout. |
| 21 | Schedule Tour Deliveries | **Tour Booking** — Tour managers set up practical daily vehicle departures (Tour_Schedules) from parent packages, assigning guides and seats. |
| 22 | Browse & Book Tours | **Tour Booking** — Customers browse tour itineraries with live OpenWeather integrations and book tickets for explicit participant counts. |
| 23 | Auto-Cancel Under-Capacity Tours | **Tour Booking** — The system triggers automated cancellations and issues 100% refunds if minimum occupancy isn't met 24 hours prior. |
| 24 | Execute AI Face Scan Attendance | **Tour Booking** — Tour guides open mobile cameras to scan passenger faces, matching identity vectors real-time to log attendance. |
| 25 | Submit 5-Star Reviews | **Feedback System** — Customers write detailed feedback and submit structured 5-star ratings within 7 days of checking out or completing a tour. |
| 26 | Moderate Customer Reviews | **Feedback System** — Administrators moderate (hide or display) guest reviews on the public web portal while saving clear audit trails. |
| 27 | Track Itemized Room Debt (Folio) | **Hospitality Finance** — Customers monitor per-room itemized expenses (Folio_Items) in real-time on their personal screens. |
| 28 | Generate Consolidated Invoice | **Hospitality Finance** — The system aggregates all accommodation, unpaid dining, and tour charges into one final combined invoice. |
| 29 | Process Final Checkout Payment | **Hospitality Finance** — Receptionists record final digital or cash payments, mark invoices as settled, and print digital receipts. |
| 30 | Run Night Audit & Daily Closing | **Hospitality Finance** — Managers trigger a "Daily Closing" function to update occupancy statistics, balance logs, and roll over business dates. |
| 31 | Monitor Financial Dashboards | **Hospitality Finance** — Executive managers view interactive dashboards splitting revenues into Room, F&B, and Tour sectors per USALI standards. |
| 32 | Track Anti-Fraud Audit Logs | **Master Data** — Administrators audit back-end historical traces (Audit_Logs) by filtering rooms or timestamps to trap internal fraud. |
| 33 | Export Financial Reports | **Hospitality Finance** — Managers generate and download comprehensive monthly operation reports in structured Excel or PDF formats. |
| 34 | Auto-Trigger Checkout Cleaning | **Housekeeping** — Completing a room checkout automatically posts a Checkout_Clean mobile task and shifts room states to Vacant_Dirty. |
| 35 | Update Cleaning Status | **Housekeeping** — Cleaners mark cleaning tasks as complete on mobile, triggering a state shift back to Vacant_Clean on the front-desk matrix. |
| 36 | Log Maintenance Requests | **Maintenance** — Housekeepers or staff report broken room appliances on-site, moving the room state to Maintenance to close inventory. |
| 37 | Resolve Engineering Failures | **Maintenance** — Technicians receive mobile maintenance repair tickets and input diagnostic summaries to unlock rooms back into service. |
| 38 | Apply Promotion Campaign Vouchers | **Financial Controls** — Receptionists select registered promotional campaign codes from centralized tables instead of entering manual bill discounts. |
| 39 | Log Multi-Stage Transactions | **Financial Controls** — The system tracks chronological financial movements including advances, split installments, final settlements, or refunds. |
| 40 | Set Dynamic Holiday Pricing | **Financial Controls** — Managers adjust pricing algorithms dynamically based on specific holiday schedules or custom calendar dates. |

##### 1.3.2 Use Cases (UC)

| ID | Use Case Feature | Use Case Description |
|---|---|---|
| UC01 | Account Management & Authentication | **Authentication** — Allows customers and staff to register, login, handle OAuth2 Google, and auto-lock after failed attempts. |
| UC02 | Password Reset | **Authentication** — Allows users who forgot their passwords to securely reset them via email token links. |
| UC03 | Profile & Dependent Management | **Profiles** — Allows customers to manage profiles, upload avatars, and link dependent travelers (family members). |
| UC04 | FaceID Enrollment & Verification | **AI Features** — Registers guest facial images and extracts vectors for local tour checkpoint authentication. |
| UC05 | Authorization & Security | **Security** — Enforces RBAC permissions, intercepts modifications for Audit Logs, and tracks Entity revisions using Envers. |
| UC06 | Master Data — Rooms | **Master Data** — Administrators manage physical room inventory, layouts, categories, and availability. |
| UC07 | Master Data — Tables | **Master Data** — Administrators manage restaurant table coordinates, occupancy, and seating capacities. |
| UC08 | Master Data — Tours | **Master Data** — Administrators manage tour routes, itineraries, scheduling, and staff assignments. |
| UC09 | Pricing, Marketing & Workflows | **Admin Ops** &mdash; Manages daily rates, dynamic seasonal prices, discount promo codes, and approval threshold workflows. |
| UC10 | Room Availability Search | **Booking** — Guest-facing room searches based on custom criteria: date ranges, category preferences, and capacities. |
| UC11 | Temporary Room Holding | **Booking** — Locks rooms temporarily (15 minutes TTL) in cache to prevent overbooking during payment checkout. |
| UC12 | Front-Desk Operations | **Front Desk** — Handles check-ins (CCCD/Passport OCR scan), credit limits, room swaps, walk-ins, and checkout invoices. |
| UC13 | Housekeeping & Maintenance | **Operations** — Automates cleaning task dispatch upon check-out, status reporting, and repair tracking. |
| UC14 | Restaurant Table Reservations | **Restaurant** — Allows customers to reserve tables in advance for specific times and party sizes. |
| UC15 | Menu Configuration | **Restaurant** — Admin CRUD configurations of food menu, categories, pricing, and allergen warning labels. |
| UC16 | Room Service Ordering | **Restaurant** — In-room dining service allows checked-in guests to place culinary orders by scanning QR codes. |
| UC17 | POS Dine-In Order Creation | **Restaurant** — POS terminals used by F&B staff to record orders for customers dining at tables. |
| UC18 | POS Settlement & Post-to-Room | **Finance** — Integrates POS orders directly to guest room folios using PIN authentication. |
| UC19 | KDS Real-time Kitchen Screen | **Kitchen** — Displays Kitchen Order Tickets (KOT) and coordinates cooking, completion, and serving states. |
| UC20 | Tour Searching & Weather | **Tours** — Guests browse upcoming tour schedules integrated with live OpenWeather API forecasts. |
| UC21 | Tour Booking & Capacity | **Tours** — Handles tour registrations, ticket checkout, and prevents over-booking of vehicle passenger seats. |
| UC22 | Tour Operation & GPS | **Tours** — Tracks tour guides, route checkpoints, GPS coordinates, and handles emergency cancellations. |
| UC23 | Service Add-ons Booking | **Booking** — Allows customers to request additional amenities like spa, gym sessions, and shuttle transfers. |
| UC24 | Submit Service Reviews | **Feedback** — Customers provide 5-star ratings and textual reviews within 7 days of service completion. |
| UC25 | Review Moderation System | **Feedback** — Administrators audit, approve, or hide customer reviews before publishing on public landing pages. |
| UC26 | Folio Aggregation | **Finance** — Aggregates room rates, dining charges, and tour fees into a single-source-of-truth folio. |
| UC27 | Night Audit & e-Invoice | **Finance** — Runs automated chots-so cronjob at 02:00 AM, posts room fees, locks daily books, and emails PDF e-Invoices. |
| UC28 | Manager Dashboard & USALI | **Analytics** — Aggregates strategic performance charts, computes USALI segment reports, and exports raw data. |
| UC29 | Automated Notification Emails | **System Integration** — Manages automated mailing of OTP tokens, booking receipts, password reset links, and e-Invoices. |
| UC30 | Scheduled Jobs Engine | **System Integration** &mdash; Houses system cronjobs (room holds, table holds, and night audits). |
| UC31 | Landing Pages & Portal | **User Experience** &mdash; Displays high-end marketing pages, room previews, and the customer booking portal. |

##### 1.3.2 Use Case Diagrams

**1.3.2.1 UCs for Guest**

>> Guest_Usecase

**1.3.2.2 UCs for Customer**

>> Customer-UseCase

**1.3.2.3 UCs for Receptionist**

>> Receptionist_UseCase

**1.3.2.4 UCs for F&B Staff**

>> F&B Staff

**1.3.2.5 UCs for Tour Guide, Housekeeping, Maintenance**

>> HouseKeeping_Usecase

**1.3.2.6 UCs for Admin**

>> Admin_Usecase

**1.3.2.7 UCs for Manager**

=>> Manager

#### 1.4 System Functionalities

##### 1.4.1 Screens Flow

> Figure 1: UserScreen Screen Flow
>
> Figure 2: Housekeeping & Maintainer Screen Flow
>
> Figure 3: Receptionist Screen Flow
>
> Figure 4: F&B Screen Flow
>
> Figure 5: Manager Screen Flow
>
> Figure 6: Admin Screen Flow

##### 1.4.2 Screen Authorization

| Screen | Guest | Customer | Receptionist | F&B Staff | Kitchen Staff | Tour Guide | Housekeeping | Maintenance | Admin/Manager |
|---|---|---|---|---|---|---|---|---|---|
| Single Login Portal | X | X | X | X | X | X | X | X | X |
| Public Home Page | X | | | | | | | | |
| User Registration Form | X | | | | | | | | |
| Member Dashboard | | X | | | | | | | |
| Room Service Menu | | X | X | | | | | | |
| Booking History & Folio | | X | | | | | | | |
| Service Feedback Form | | X | | | | | | | |
| Room Matrix Dashboard | | | X | | | | | | |
| Check-in Processing Form | | | X | | | | | | |
| Checkout & Bill Workspace | | | X | | | | | | |
| Restaurant POS Terminal | | | | X | | X | | | |
| Payment Verification Gate | | | | X | | | | | |
| Kitchen Status Dashboard | | | | | X | X | | | |
| Assigned Tour Schedules | | | | | | X | | | |
| AI Face Scan Attendance | | | | | | X | | | |
| Housekeeping Task Grid | | | | | | | X | | |
| Maintenance Request Queue | | | | | | | | X | |
| USALI Financial Analytics | | | | | | | | | X |
| Config & Audit Log Panel | | | | | | | | | X |
| Staff Directory Management | | | | | | | | | X |

##### 1.4.3 Non-UI Functions

| # | Feature | System Function | Description |
|---|---|---|---|
| 1 | Authentication & Security | Password_Encryption_Filter | Extracts and encrypts raw user passwords using the BCrypt hashing algorithm before performing data matching in the Accounts table. |
| 2 | Authentication & Security | Role_Based_Routing_Engine | A Spring Security filter that analyzes the role_name of the authenticated Token to automatically configure and trigger URL redirection for each Actor. |
| 3 | Room Booking | Pending_Booking_Auto_Cancellation | Runs a background Task Scheduler every 5 minutes to automatically scan and cancel room reservations (status = 'Cancelled') if the 30% deposit is not completed within 15 minutes. |
| 4 | Front-Desk Operations | Dependent_Account_Auto_Generation | Triggered when the Receptionist upgrades a dependent's account, automatically executing parallel data insertion into both the Accounts and Customers tables. |
| 5 | Front-Desk Operations | Housekeeping_Task_Trigger | A database Trigger that detects when a room booking status changes to Checked_Out and immediately initializes a new cleaning task entry in the Housekeeping_Tasks table. |
| 6 | F&B Billing | Credit_Limit_Realtime_Validator | A Constraint Validator that executes in the background when a guest orders a service, aggregating the current outstanding folio balance with the new order total to ensure it does not exceed the room's Credit_Limit. |
| 7 | F&B Billing | Kitchen_Notification_Dispatcher | Uses WebSockets or a Message Broker to automatically dispatch Kitchen Order Tickets (KOT) to the kitchen monitor as soon as a room service order passes PIN verification. |
| 8 | Kitchen Inventory | Menu_Item_Availability_Sync | Triggered when the kitchen marks an item as out of stock, updating the database state to is_available = false and broadcasting real-time UI updates to all POS terminals and guest devices. |
| 9 | Tour Attendance | AI_Face_Vector_Matching_Service | A background Python AI service that receives the camera video stream, extracts facial feature vectors, and compares them against the encodings.pickle template file to verify identity. |
| 10 | Tour Attendance | Attendance_Status_Synchronizer | Receives positive match results from the AI Service or manual inputs from the Tour Guide to execute the UPDATE query, changing attendance_status to 'Attended'. |
| 11 | Housekeeping & Maintenance | Room_Status_State_Machine | Automatically manages and syncs the physical room state (Rooms table) on the Receptionist's Matrix Dashboard whenever a housekeeping or maintenance staff member updates task progress on mobile. |
| 12 | Housekeeping & Maintenance | Maintenance_Request_Auto_Bridge | Triggered when housekeeping reports equipment damage, automatically inserting a new issue record into the Maintenance_Requests table and switching the room status to 'Maintenance'. |
| 13 | Financial Accounting | USALI_Revenue_Decomposer | Automatically decomposes the gross revenue from finalized invoices, calculating and allocating financial data into three distinct departmental revenue categories (Rooms, F&B, Tours) following USALI standards. |
| 14 | Marketing & Promotions | Voucher_Validity_Checker | Validates promotional codes against predefined constraints (expiration date, global usage limit, and minimum order amount) before applying discounts to the total invoice. |
| 15 | Security Auditing | AOP_Data_Interception_Logger | Utilizes Spring AOP (Aspect-Oriented Programming) to transparently intercept modifications to financial tables, capturing old/new values, staff ID, and system logs into the Audit_Logs table. |
| 16 | Staff Directory | Employee_Account_Transactional_Link | Encapsulates the staff onboarding process within a @Transactional block, ensuring a complete rollback of both employee records and login accounts if any insertion error occurs. |

#### 1.5 Entity Relationship Diagram

> URL: ERD

##### Entities Description

| # | Entity | Description |
|---|---|---|
| 1 | Account | Stores account information for all system users (including Customers, Staff, and Admins), such as usernames, encrypted passwords, emails, phone numbers, and account statuses. |
| 2 | Role | Manages system roles and permissions (e.g., Admin, Staff, Customer) to restrict and authorize access to specific features. |
| 3 | Employees | Stores detailed personal and professional profiles of the staff members linked to an account, managing information such as full name, gender, national ID (CCCD), phone number, email, and salary details. |
| 4 | Audit_Logs | Records security and data modification history within the system. It tracks which employee performed what action (e.g., Insert, Update, Delete) on a specific table, keeping old versus new values alongside IP addresses and timestamps for security auditing. |
| 5 | Hotel_Operations | Manages daily internal hotel tasks and operational workflows (such as room cleaning, maintenance, or room service). It links tasks to specific hotel rooms, assigns them to an operational staff member (staff_id), designates a supervisor, and tracks priority, task status, and execution timelines. |
| 6 | Tour_Staff_Assignments | Handles the staffing and assignment of employees to specific tour schedules. It maps which employee is assigned to a tour schedule and defines their specific operational role during that tour (e.g., Tour Guide, Driver, Coordinator). |
| 7 | Dynamic_Pricing | Manages seasonal or time-based price adjustments for hotel room categories. It stores specific date ranges (start_date to end_date) and a pricing multiplier or modifier to calculate flexible room rates dynamically during holidays, weekends, or peak seasons. |
| 8 | Room_Categories | Defines the types or classifications of hotel rooms available (e.g., Standard, Deluxe, Suite). It captures the structural details of each category, including the category name, standard baseline price (base_price), and maximum guest accommodation capacity. |
| 9 | Rooms | Stores the physical inventory of individual hotel rooms. Each record tracks a specific room identifier (room_number), references its operational classification (category_id), monitors its availability state (room_status), and keeps track of any ongoing reservation via the active booking detail. |
| 10 | Room_Bookings | Extends the global booking entity specifically for hotel room stays. It acts as a 1:1 sub-type mapping to booking_id (PK, FK) and captures financial policy controls like deposit_amount, credit_limit, check-in/out schedules, strict cancellation deadlines, and a secured personal_pin_hash for digital room access or self-check-in verification. |
| 11 | Bookings | Acts as the central, high-level transaction record for any reservation made by a customer. It stores core structural metadata including the booking date, total accumulated price (total_price), current global state (booking_status), source of the booking (e.g., Online, Walk-in), and data concurrency control (version). |
| 12 | Tours | Manages the catalog of holiday packages and sightseeing trips offered by the business. It stores core details such as the official tour name, the standard base cost calculated per individual passenger (price_per_person), and the maximum group size constraint (max_capacity). |
| 13 | Tour_Schedules | Handles the specific operational departures and calendar events for the scheduled tours. It charts exactly when a tour will run by defining its precise departure date, departure time, and current scheduling status (e.g., Active, Fully Booked, Departed, Cancelled). |
| 14 | Customers | Stores comprehensive personal profiles and loyalty data for registered guests. It tracks details such as full name, gender, identification (cccd, passport), contact information (phone, email), accumulated reward points (loyalty_points), and membership tier levels. |
| 15 | Dependents | Manages information about family members or companions traveling alongside a primary customer. It links directly to the customer record and tracks the dependent's name, birth date, and legal ID or passport number for check-in compliance. |
| 16 | Room_Booking_Details | Actively logs the specific operational segments of a room reservation. It links the primary booking reference to an allocated physical room (room_id), its category, and dynamically records the calculated room_charge and current status (detail_status). It also references a guest_customer_id if the room occupant differs from the booking owner. |
| 17 | Table_Reservations | Manages dining reservations at the hotel's internal restaurant facilities. It maps a customer to a specific physical table assignment (table_id) and tracks the intended reservation date (reserve_date), time, and fulfillment status. |
| 18 | Restaurant_Tables | Stores the physical inventory of dining tables within the hotel's internal restaurant facilities. It tracks specific data for each table, including its official identifier (table_number) and maximum seating accommodation (capacity). |
| 19 | Reviews | Stores quality assessments, service ratings, and text feedback written by customers. It enables granular feedback loops by allowing guests to rate either a specific room stay (room_booking_detail_id) or a specific sightseeing excursion (tour_booking_id). |
| 20 | Tour_Bookings | Extends the global booking entity for sightseeing excursions. It maps the parent booking_id directly to a specific operational trip (schedule_id), keeping track of the total guest headcount (participant_count) and their overall attendance_status. |
| 21 | Food_Orders | Manages dining and room-service culinary orders. It cross-references back to a primary booking or a specific active room stay (room_booking_detail_id), tracking the ordering channel (order_type), real-time kitchen display status (kot_status - Kitchen Order Ticket), payment handling, and POS point-of-sale verification. |
| 22 | Food_Order_Details | A transactional line-item entity that captures the granular details of a food order. It documents exactly which culinary items were selected, the ordered quantity, and freezes the historical selling price (price_at_order) at the specific moment the ticket was sent to the kitchen. |
| 23 | Menu_Items | Houses the master catalog of all dishes, beverages, and culinary options available across the hotel's dining facilities. It keeps track of the official dish name, standard retail price, and an operational toggle (is_available) to easily handle out-of-stock menu options. |
| 24 | Folio_Items | Manages incidental charges, room service costs, and departmental expenses incurred by guests during their stay. It links back to an active booking segment, tracks the source department (e.g., Restaurant, Spa, Room Service), stores the calculated amount, and captures an optional employee signature (signature_img_url) for internal validation. |
| 25 | Consolidated_Invoices | Acts as the master billing statement for an entire customer booking. It aggregates all costs from room stays, tour reservations, and incidental folio items, handles fiscal calculations like tax (vat_amount) and pre-tax balances, applies active promotional codes, and monitors the legal document's issuance timeline. |
| 26 | Promotions | Stores active marketing and discount campaigns configuration within the system. It contains specific programmatic parameters including unique voucher alphanumeric strings (promo_code), discount types (e.g., percentage or fixed amount), validity thresholds (valid_to), and an operational availability toggle. |
| 27 | Payment_Transactions | Tracks distinct monetary funding actions processed against generated invoices. It captures precise accounting logs such as the processed transaction amount, chosen gateway methods (e.g., Credit Card, Cash, Digital Wallet), and the authoritative processing status to secure historical payment receipts. |

---

### 2. Use Case Specifications

> Provide specifications for the use cases (UCs) those are covered in the system. The UCs are grouped by the system features and even sub features. You just need to provide UC specifications for complex UCs involving in the main workflows (business processes). Other UCs (i.e CRUD or data-viewing UCs) are simple, and you just need to refer the descriptions in the Functional Requirement (part 3) below.

#### 2.1.1 Register

**ID and Name:** UC-1 Register  
**Primary Actor:** Guest  
**Secondary Actors:** Mail Server, Google OAuth  
**Description:** Allows a guest to create a new user account in the system by filling in required information or using a Google account. After registration, the system sends a verification email to activate the account.  
**Trigger:** The guest clicks the "Sign Up" button on the homepage.

**Preconditions:**
- The guest has not registered before.
- The guest has access to a valid email.
- The guest has not used an email that already exists in the system.

**Postconditions:**
- A new inactive user account is created.
- A verification email is sent to the user.
- The account is pending activation.

**Normal Flow:**
1. The guest opens the registration page.
2. Guest enters username, email, password, full name, gender, date of birth, and address.
3. Guest clicks "Sign Up".
4. System checks for required fields (MSG02).
5. System checks for email duplication (MSG05).
6. The system encrypts the password and stores the account (BR-02).
7. System displays success toast: "Registration successful" (MSG03).

**Alternative Flows:**
- **AF1:** Guest chooses "Sign in with Google" → Redirected to Google OAuth → Successful → Auto-register user and proceed to verification.

**Exceptions:**
- **EX1:** Required fields missing → MSG02.
- **EX2:** Email already in use → MSG05.
- **EX3:** Password too weak → MSG14.

**Priority:** High  
**Frequency of Use:** Very frequent – every new student  
**Business Rules:** BR-01, BR-02, BR-03, BR-105

**Other Information:**
- Email verification is required before login.
- Google OAuth is optional but supported.

**Assumptions:**
- Google OAuth is correctly configured.
- The mail server is operational.
- The user has internet access and a valid email.

---

#### 2.1.2 Login

**ID and Name:** UC-2 Login  
**Primary Actor:** Customer/ Employee  
**Secondary Actors:** None  
**Description:** Allows a registered user to securely log into the system by entering their registered email address and password. Upon successful authentication, the system grants access to the user's dashboard based on their role.  
**Trigger:** The guest clicks the "Login" button on the homepage.

**Preconditions:**
- The user has a registered account associated with an email address.
- The account is active and not suspended or banned.
- The user is not currently logged into the system.

**Postconditions:**
- The system successfully authenticates the user and creates a secure login session.
- The user is redirected to the appropriate dashboard based on their role.
- The user's status is updated to Online.

**Normal Flow:**
1. The user opens the Login page.
2. The system displays the login form.
3. The user enters their registered email address and password.
4. The user clicks the "Login" button.
5. The system validates that all required fields are provided and that the email format is valid.
6. The system verifies that the email exists in the database.
7. The system verifies that the account status is active.
8. The system compares the entered password with the encrypted password stored in the database.
9. The system checks whether the account is currently locked.
10. The system authenticates the user.
11. The system creates a secure login session.
12. The system determines the user's role and permissions.
13. The system updates the user's status to Online.
14. The system redirects the user to the appropriate dashboard.
15. The system displays the dashboard interface.

**Alternative Flows:**
- **AF1:** At Step 10, the system requires additional OTP/Email verification for a new or unrecognized device. After successful verification, resume at Step 11.
- **AF2:** At Step 14, the user is redirected to a role-specific dashboard (Customer, Receptionist, F&B Staff, Housekeeping, Tour Guide, Admin, or Manager).

**Priority:** High  
**Frequency of Use:** Very frequent  
**Business Rules:** BR-SYS-01, BR-SYS-02, BR-SYS-03

**Other Information:**
- Login activities may be recorded for security monitoring.
- OTP verification may be required for suspicious login attempts.
- Users are redirected to different dashboards according to their assigned roles.

**Assumptions:**
- The authentication service is available.
- The database connection is operational.
- The user has a valid registered account.
- Email/OTP services are functioning normally.

---

#### 2.1.3 Forgot Password

**ID and Name:** UC-3 Forgot Password  
**Primary Actor:** Guest/ Customer  
**Secondary Actors:** Mail Server  
**Description:** Allows a customer to reset the account password by verifying their registered email address and creating a new password.  
**Trigger:** The user clicks the "Forgot Password" link on the Login page.

**Preconditions:**
- User has a registered account.
- User remembers and can access the registered email address.
- The mail server is available.

**Postconditions:**
- The password is updated successfully.
- Old password becomes invalid.
- User can log in using the new password.

**Normal Flow:**
1. Users open the Login page.
2. Users click "Forgot Password".
3. The system displays the password recovery form.
4. Users enter the registered email address.
5. The system validates the email format.
6. System checks whether the email exists in the system.
7. The system sends an OTP/Email Code to the registered email.
8. Users enter the received OTP/Email Code.
9. The system validates the OTP/Email Code.
10. Users enter a new password and confirm it.
11. The system encrypts the new password.
12. System updates the account password.
13. The system displays a message: "Password reset successful."
14. Users are redirected to the Login page.

**Alternative Flows:**
- **AF1:** Invalid OTP → At Step 9 → The entered OTP is incorrect → System displays an error message → Customer re-enters the OTP.

**Priority:** High  
**Frequency of Use:** Frequent  
**Business Rules:** BR-SYS-01, BR-SYS-02

**Other Information:**
- Password recovery is performed through email verification.
- The customer must remember the registered email address.

**Assumptions:**
- The mail server is operational.
- The customer has internet access.
- The registered email account is accessible.

---

#### 2.1.4 Search Available Rooms

**ID and Name:** UC-4 Search Available Rooms  
**Primary Actor:** Guest / Customer  
**Secondary Actors:** System  
**Description:** Allows guests or customers to search for available room categories based on check-in date, check-out date, number of guests, and room requirements.  
**Trigger:** The user selects room search on the website.

**Preconditions:**
- The system is operational.
- Room categories are available in the system.
- The user provides search criteria.

**Postconditions:**
- Available room categories are displayed.
- Room availability information is returned to the user.

**Normal Flow:**
1. The user accesses the room search page.
2. The system displays search forms.
3. User enters:
   - Check-in date.
   - Check-out date.
   - Number of guests.
4. User clicks Search.
5. The system validates search criteria.
6. The system checks room availability.
7. System filters room categories that satisfy:
   - Capacity requirements.
   - Availability during the selected period.
8. The system calculates room prices based on current pricing rules.
9. System displays available room categories with:
   - Room category name.
   - Capacity.
   - Price.
   - Description.
10. User reviews search results.

**Alternative Flows:**
- **AF1:** User is logged in → Customer accesses the member homepage → Customer performs room search directly from the booking page.

**Priority:** High  
**Frequency of Use:** Very Frequent  
**Business Rules:** BR-FO-01

**Other Information:**
- Guests can search rooms without logging in.
- Booking requires authentication.

**Assumptions:**
- Room categories have been configured.
- Pricing information is available.
- Availability data is up to date.

---

#### 2.1.5 Book Room

**ID and Name:** UC-5 Book Room  
**Primary Actor:** Customer  
**Secondary Actors:** Payment Gateway (VNPay Sandbox)  
**Description:** Allows a customer to create a room booking, select room categories, specify stay dates, provide guest information, and make a deposit payment to reserve the booking.  
**Trigger:** The customer clicks the "Book Room" button after searching available rooms.

**Preconditions:**
- The customer is logged in.
- Selected room categories are available.
- Check-in and check-out dates are valid.
- The customer account is active.

**Postconditions:**
- A booking record is created.
- Room booking details are stored.
- Deposit payment is recorded.
- Booking status becomes Confirmed.

**Normal Flow:**
1. Customer searches available rooms.
2. The system displays available room categories.
3. The customer selects one or more room categories.
4. Customer enters:
   - Check-in date.
   - Check-out date.
   - Number of rooms.
   - Number of guests.
   - Special requests (optional).
5. The system calculates booking cost.
6. System displays booking summary.
7. Customer confirms booking information.
8. The system creates a Booking record with status Pending.
9. The system creates a Room_Booking record.
10. The system creates Room_Booking_Details records.
11. The system calculates the required deposit amount.
12. The system redirects customers to the VNPay payment page.
13. Customer completes deposit payment.
14. Payment Gateway returns successful transaction results.
15. System updates booking status to Confirmed.
16. The system displays booking confirmation.

**Alternative Flows:**
- **AF1:** Multiple Room Booking → Customer selects multiple rooms → System creates multiple Room_Booking_Details records under the same booking.
- **AF2:** Payment Timeout → If the customer does not complete the deposit payment within 15 minutes → System automatically cancels the booking → Room is released back to inventory → MSG: "Booking expired due to payment timeout"

**Priority:** Critical  
**Frequency of Use:** Very Frequent  
**Business Rules:** BR-FO-01, BR-FO-02

**Other Information:**
- One booking may contain multiple rooms.
- Physical room assignment is performed later during Check-in by the Receptionist.

**Assumptions:**
- Payment Gateway is operational.
- Room pricing has been configured.
- Room availability information is accurate.

---

#### 2.1.6 Cancel Booking

**ID and Name:** UC-6 Cancel Booking  
**Primary Actor:** Customer  
**Secondary Actors:** System  
**Description:** Allows a customer to cancel a confirmed room booking before the check-in date. The system determines whether a refund is applicable according to the cancellation policy.  
**Trigger:** The customer selects a booking and clicks "Cancel Booking".

**Preconditions:**
- The customer is logged in.
- Booking exists.
- Booking status is not Checked-In.
- Booking status is not Cancelled.

**Postconditions:**
- Booking status becomes Cancelled.
- Refund transaction is created if eligible.
- Room inventory becomes available again.

**Normal Flow:**
1. Customer opens booking history.
2. The system displays customer bookings.
3. The customer selects a booking.
4. Customer clicks Cancel Booking.
5. The system displays cancellation information.
6. Customer confirms cancellation.
7. System checks cancellation policy.
8. The system calculates a refundable amount.
9. System updates booking status to Cancelled.
10. The system creates refund transactions if applicable.
11. The system displays cancellation results.

**Alternative Flows:**
- **AF1:** Booking qualifies for full refund → Cancellation occurs more than 48 hours before check-in → System approves full refund.

**Priority:** High  
**Frequency of Use:** Medium  
**Business Rules:** BR-FIN-02

**Other Information:**
- Refund records are stored in Payment_Transactions.
- Refund transactions use transaction_type = Refund.

**Assumptions:**
- Payment records exist.
- The booking belongs to the current customer.
- Refund service is available.

---

#### 2.1.7 Front Desk

##### UC-7 Check-in

**ID and Name:** UC-7 Check-in  
**Primary Actor:** Receptionist  
**Secondary Actors:** Customer / System  
**Description:** Allows a receptionist to verify guest information, assign physical rooms, register accompanying guests, configure spending authorization, and complete the check-in process.  
**Trigger:** The customer arrives at the resort and requests check-in.

**Preconditions:**
- Booking status is Confirmed.
- Deposit payment has been completed.
- Assigned rooms are available.
- Customer presents valid identification documents.

**Postconditions:**
- Guest successfully checks in.
- A physical room is assigned.
- Booking status becomes Checked_In.
- Room status becomes Occupied_Clean.
- Guest spending authorization is configured.

**Normal Flow:**
1. The receptionist opens the Check-in screen.
2. Receptionist searches for the booking.
3. The system displays booking information.
4. The receptionist verifies customer identity using CCCD or Passport.
5. The receptionist records information of accompanying guests.
6. The receptionist selects available physical rooms.
7. System assigns selected rooms to Room_Booking_Details.
8. Receptionist configures:
   - Spending authorization.
   - Credit limit.
   - Guest PIN.
9. Receptionist confirms check-in.
10. System updates Room_Booking_Details status to Checked_In.
11. System updates Booking status to Checked_In.
12. System updates Room status to Occupied_Clean.
13. The system completes the check-in process.

**Alternative Flows:**
- **AF1:** Upgrade Dependent to Customer → Receptionist selects "Upgrade Account" → System creates a new Customer account for the dependent → Existing dependent information is linked to the new account.

**Priority:** Critical  
**Frequency of Use:** Very Frequent  
**Business Rules:** BR-FO-03, BR-FO-04

**Other Information:**
- Physical room assignment is performed during check-in.
- Credit Limit and PIN are configured during check-in.
- Dependents may be upgraded to Customer accounts.

**Assumptions:**
- Room inventory information is accurate.
- The receptionist has proper authorization.
- Customer identification data is valid.

---

##### UC-8 Check-out

**ID and Name:** UC-8 Check-out  
**Primary Actor:** Receptionist  
**Secondary Actor:** Customer  
**Description:** Allows a receptionist to review all charges incurred during the guest's stay, generate a consolidated invoice, process final payment, and complete the check-out procedure.  
**Trigger:** The customer requests check-out at the reception desk.

**Preconditions:**
- Booking status is Checked_In.
- A customer is currently staying at the resort.
- All charges have been recorded in the system.

**Postconditions:**
- Final payment is completed.
- Consolidated invoice is issued.
- Booking status becomes Checked_Out.
- Room status becomes Vacant_Dirty.
- The housekeeping task is automatically created.

**Normal Flow:**
1. The receptionist opens the Check-out screen.
2. Receptionist searches for the guest booking.
3. System retrieves booking information.
4. System retrieves all unpaid charges from Folio_Items.
5. System calculates:
   - Room charges.
   - Food & Beverage charges.
   - Tour charges.
   - Additional fees.
6. The system generates a consolidated invoice summary.
7. The receptionist reviews invoice details with the customer.
8. The receptionist verifies charge evidence if requested.
9. The system calculates outstanding balance.
10. The customer selects the payment method.
11. The receptionist processes payment.
12. System records payment transaction.
13. System issues consolidated invoice.
14. System updates Booking status to Checked_Out.
15. System updates Room status to Vacant_Dirty.
16. The system automatically creates a Housekeeping task.
17. System completes check-out.

**Alternative Flows:**
- **AF1:** Apply Promotion → Customer has a valid promotion code → System applies the corresponding discount.

**Priority:** Critical  
**Frequency of Use:** Very Frequent  
**Business Rules:** BR-FIN-01, BR-FO-04

**Other Information:**
- The invoice includes charges from Rooms, Food & Beverage, and Tours.
- Payment transactions are stored in Payment_Transactions.
- Housekeeping tasks are automatically generated after check-out.

**Assumptions:**
- All Folio_Items have been correctly recorded.
- Payment services are available.
- The receptionist has permission to perform check-out.

---

#### 2.1.7 Restaurant POS

##### UC-9 Place Food Order

**ID and Name:** UC-9 Place Food Order  
**Primary Actor:** Customer  
**Secondary Actor:** System / F&B Staff  
**Description:** Allows a customer to select food and beverage items from the menu and submit an order for room service.  
**Trigger:** The customer accesses the Room Service page and selects food items.

**Preconditions:**
- The customer is currently checked in.
- Customers have access to the Room Service page.
- Selected menu items are available.

**Postconditions:**
- A food order is created.
- Order details are stored.
- The kitchen receives the order.
- Order status becomes Pending.

**Normal Flow:**
1. The customer opens the Room Service page.
2. The system displays available menu items.
3. The customer selects food and beverage items.
4. The customer enters a quantity for each item.
5. The system calculates the order total.
6. The customer reviews the order.
7. The customer confirms the order.
8. The system verifies that selected items are available.
9. The system creates a Food_Order record.
10. The system creates Food_Order_Detail records.
11. The system sets order status to Pending.
12. The system sends the order to the Kitchen Dashboard.
13. The system displays order confirmation.

**Alternative Flows:**
- **AF1:** Multiple Menu Items → At Step 3 → Customer selects multiple menu items → System creates multiple Food_Order_Detail records.
- **AF2:** Room Charge Payment → At Step 7 → Customer chooses to charge the order to the room → System records the order for later settlement during Check-out.

**Priority:** High  
**Frequency of Use:** Very Frequent  
**Business Rules:** BR-FB-01, BR-FB-02

**Other Information:**
- Orders are stored in Food_Orders and Food_Order_Details.
- New orders are initially created with Pending status.
- Kitchen staff cannot modify order prices.

**Assumptions:**
- Menu information is up to date.
- Kitchen Dashboard is operational.
- Customers are associated with an active room booking.

---

##### UC-10 Process Food Order

**ID and Name:** UC-10 Process Food Order  
**Primary Actor:** F&B Staff  
**Secondary Actor:** System  
**Description:** Allows kitchen staff to receive food orders from the POS or Room Service system, update preparation status, and complete food preparation.  
**Trigger:** A new food order is received by the Kitchen Dashboard.

**Preconditions:**
- A food order exists.
- Order status is Pending.
- F&B staff is logged in.

**Postconditions:**
- Order status is updated.
- Prepared food is ready for serving.
- Order reaches Served status.

**Normal Flow:**
1. F&B Staff opens the Kitchen Dashboard.
2. The system displays all Pending food orders.
3. F&B Staff selects an order.
4. F&B Staff reviews order details.
5. F&B Staff clicks Start Preparing.
6. System updates order status to Preparing.
7. F&B Staff prepares the food.
8. F&B Staff completes food preparation.
9. F&B Staff clicks Mark as Served.
10. System updates order status to Served.
11. The system records the completion time.
12. System updates the dashboard.

**Alternative Flows:**
- **AF1:** Multiple Orders Processing → At Step 2 → Multiple pending orders exist → F&B Staff processes orders one by one.
- **AF2:** Out-of-Stock Item Report → At Step 4 → F&B Staff discovers insufficient ingredients → F&B Staff reports the item as unavailable.

**Priority:** High  
**Frequency of Use:** Very Frequent  
**Business Rules:** BR-FB-02

**Other Information:**
- F&B Staff cannot create orders.
- F&B Staff cannot modify prices.
- F&B Staff can only update order status.
- Order lifecycle follows: Pending → Preparing → Served

**Assumptions:**
- Orders are correctly received from POS or Room Service.
- Kitchen Dashboard is operational.
- Ingredient availability is accurately managed.

---

##### UC-11 Pay Food Order

**ID and Name:** UC-11 Pay Food Order  
**Primary Actor:** F&B Staff  
**Secondary Actor:** Customer  
**Description:** Allows restaurant staff to process payment for a food order either by immediate payment or by charging the order to a guest room.  
**Trigger:** The food order has been completed and is ready for payment processing.

**Preconditions:**
- Food order exists.
- Order status is Pending.
- Kitchen staff is logged in.

**Postconditions:**
- Payment information is recorded.
- The order is marked as paid or charged to the room.
- Financial records are updated.

**Normal Flow:**
1. F&B Staff opens the payment screen.
2. System displays served food orders.
3. F&B Staff selects an order.
4. The system calculates the total amount.
5. F&B Staff asks the customer to choose a payment method.
6. The customer selects Pay Immediately.
7. F&B Staff records payment information.
8. The system marks the order as paid.
9. The system updates financial records.
10. The system displays payment success confirmation.

**Alternative Flows:**
- **AF1:** Charge To Room → At Step 6 → Customer chooses Charge To Room → FB Staff enters room number → System verifies room information → System verifies spending authorization → System verifies remaining credit limit → Customer enters PIN or provides signature → System creates a Folio_Item record → System marks the order as charged to room.

**Priority:** High  
**Frequency of Use:** Very Frequent  
**Business Rules:** BR-FB-01

**Other Information:**
- Immediate payment is typically used for external restaurant customers.
- Charge-to-room orders are settled during Check-out.
- PIN or signature evidence is stored for dispute resolution.

**Assumptions:**
- The POS system is operational.
- Room information is up to date.
- Customer authorization data is available.

---

##### UC-12 Book Tour

**ID and Name:** UC-12 Book Tour  
**Primary Actor:** Customer  
**Secondary Actor:** System  
**Description:** Allows a customer to book a tour schedule, select the number of participants, and reserve available seats for a specific departure.  
**Trigger:** The customer selects a tour and clicks Book Tour.

**Preconditions:**
- The customer is logged in.
- Tour schedule exists.
- Tour schedule status is Open.
- Seats are available.

**Postconditions:**
- Tour booking is created.
- Reserved seats are updated.
- Booking status becomes Confirmed.

**Normal Flow:**
1. The customer opens the Tour page.
2. The system displays available tours.
3. The customer selects a tour.
4. The system displays available tour schedules.
5. The customer selects a schedule.
6. The customer enters the number of participants.
7. The system checks seat availability.
8. System displays booking summary.
9. Customer confirms booking information.
10. The system creates a Booking record.
11. The system creates a Tour_Booking record.
12. System updates booked_seats in Tour_Schedules.
13. The system confirms the booking.
14. The system displays booking success information.

**Alternative Flows:**
- **AF1:** Group Tour Booking → At Step 6 → Customer books for multiple participants → System records participant_count greater than 1.

**Priority:** High  
**Frequency of Use:** Very Frequent  
**Business Rules:** BR-TR-01

**Other Information:**
- A booking is linked to a specific Tour_Schedule.
- Booked seats are tracked through booked_seats.
- Attendance will be recorded later during the AI Face Scan process.

**Assumptions:**
- Tour schedules have been created by the Tour Manager.
- Seat availability information is accurate.
- Customer information is valid.

---

##### UC-13 Cancel Tour Booking

**ID and Name:** UC-13 Cancel Tour Booking  
**Primary Actor:** Customer  
**Secondary Actors:** System, Receptionist  
**Description:** Allows a customer or receptionist to cancel an existing tour booking. The system validates cancellation conditions, updates tour slot availability, processes refund rules if applicable, records audit logs, and notifies the customer.  
**Trigger:** The customer selects Cancel Tour Booking from their booking history, or requests a receptionist to cancel the booking.

**Preconditions:**
- The customer is logged into the system.
- The tour booking exists.
- The booking belongs to the customer or is accessed by an authorized receptionist.
- The tour has not started.
- The customer has not been checked-in for the tour.
- The booking has not already been cancelled.

**Postconditions:**
- Tour booking is marked as Cancelled.
- Tour slot availability is updated.
- Refund request is generated if applicable.
- Audit Log is recorded.
- Cancellation confirmation is sent to the customer.

**Normal Flow:**
1. The customer opens the My Tour Bookings page.
2. The system displays the list of the customer's tour bookings.
3. Customer selects a tour booking.
4. The system displays the booking details and cancellation policy.
5. Customer clicks Cancel Booking.
6. The system asks the customer to confirm the cancellation request.
7. Customer confirms the cancellation.
8. The system validates the booking information.
9. The system verifies that the booking status is eligible for cancellation.
10. The system verifies that the tour has not started.
11. System updates the booking status to Cancelled.
12. System releases the reserved tour slot(s).
13. The system processes the refund according to the cancellation policy, if applicable.
14. The system records the cancellation activity in the Audit Log.
15. The system sends a cancellation confirmation notification/email to the customer.
16. The system displays a cancellation success message.

**Alternative Flows:**
- **AF1:** At Step 13, the booking is eligible for a refund. The system processes the refund and resumes at Step 14.
- **AF2:** At Step 13, the booking was charged to a room folio. The system adjusts the folio balance and resumes at Step 14.
- **AF3:** At Step 1, a receptionist cancels the booking on behalf of the customer. The system records the action and resumes at Step 13.

**Exceptions:**
- **E1:** Booking not found.
- **E2:** Tour has already started.
- **E3:** Customer has already checked in.
- **E4:** Refund transaction failed.
- **E5:** Session timeout.

**Priority:** High  
**Frequency of Use:** Occasional  
**Business Rules:** BR-TR-01, BR-TR-02, BR-SYS-04

**Other Information:**
- Cancellation confirmation is sent to the customer via email.
- Cancelled bookings remain in the system for auditing and reporting purposes.
- Refund transactions are processed through the integrated payment gateway (Stripe/VNPay).
- Released tour slots become immediately available for new bookings.
- All cancellation records can be reviewed by authorized administrators through the Audit Log system.

**Assumptions:**
- The customer has a valid and authenticated account.
- The tour booking exists in the system.
- The payment gateway (Stripe/VNPay) is available for refund processing.
- Tour schedule and booking information are up to date.
- The customer has permission to cancel only their own bookings.

---

##### UC-14 Make Online Payment

**ID and Name:** UC-14 Make Online Payment  
**Primary Actor:** Customer  
**Secondary Actors:** System / Payment Gateway (VNPay Sandbox)  
**Description:** Allows a customer to make an online payment for a booking deposit through the VNPay payment gateway.  
**Trigger:** The customer confirms a booking and chooses online payment.

**Preconditions:**
- A booking has been created.
- Booking status is Pending.
- The deposit amount has been calculated.
- Payment gateway is available.

**Postconditions:**
- Payment transaction is recorded.
- Booking status becomes Confirmed.
- Deposit payment is successfully completed.

**Normal Flow:**
1. Customer reviews booking information.
2. The system calculates the required deposit amount.
3. Customer chooses Online Payment.
4. The system creates a payment request.
5. The system redirects the customer to VNPay.
6. The customer enters payment information.
7. The customer confirms the payment.
8. VNPay processes the transaction.
9. VNPay returns a successful payment result.
10. The system verifies the payment response.
11. System records a Payment_Transaction.
12. System updates booking status to Confirmed.
13. The system displays payment success information.

**Alternative Flows:**
- **AF1:** Partial Deposit Payment → At Step 2 → Booking requires only a deposit payment → System calculates the deposit amount rather than the full booking value.

**Priority:** Critical  
**Frequency of Use:** Frequent  
**Business Rules:** BR-FO-02

**Other Information:**
- VNPay Sandbox is used for payment processing.
- Successful transactions are stored in Payment_Transactions.
- Deposit payments are linked to bookings.

**Assumptions:**
- VNPay service is operational.
- Internet connection is available.
- Payment information is valid.

---

##### UC-15 Submit Feedback

**ID and Name:** UC-15 Submit Feedback  
**Primary Actor:** Customer  
**Secondary Actor:** System  
**Description:** Allows customers to submit ratings and feedback for completed services, including rooms, tours, and dining experiences.  
**Trigger:** Customer selects the Submit Feedback option for a completed booking.

**Preconditions:**
- The customer is logged into the system.
- The related booking has been completed.
- The booking belongs to the customer.
- The feedback submission period has not expired.

**Postconditions:**
- Feedback is successfully recorded in the system.
- Rating score and review content are associated with the selected booking.
- Feedback is available for viewing and moderation.

**Normal Flow:**
1. The customer opens the booking history page.
2. System displays completed bookings eligible for review.
3. Customer selects a completed booking.
4. The system displays the feedback form.
5. The customer enters a rating score.
6. Customer enters review comments.
7. Customer clicks Submit Feedback.
8. The system validates the input data.
9. The system saves the feedback information.
10. The system links the feedback to the corresponding booking.
11. The system displays a confirmation message.

**Alternative Flows:**
- **AF1:** At Step 5, the customer submits only a rating without comments. The system records the rating and resumes at Step 9.
- **AF2:** At Step 5, the customer submits feedback for multiple service categories (room, tour, dining). The system records all feedback entries and resumes at Step 9.

**Priority:** Critical  
**Frequency of Use:** Frequent  
**Business Rules:** BR-TR-03, BR-TR-04

**Other Information:**
- Feedback may include both a rating score and review comments.
- Submitted feedback can be displayed publicly on the resort website.
- Reviews are subject to moderation according to resort policies.
- Customers can submit feedback only for services they have actually used.

**Assumptions:**
- The customer is authenticated.
- Booking completion information is accurate.
- The review system is available.
- The booking belongs to the customer submitting the feedback.

---

### 2. Use Case Specifications

This section outlines the detailed use case specifications for the core business processes of the Kawai Resort & Hub platform. Simple data management tasks (CRUD) and basic informational lookups are detailed directly within the functional specifications in Section 3.

#### **UC01: Account Management & Authentication**
* **Primary Actor:** Guest / Customer / Staff / Admin
* **Description:** Provides registration, validation, traditional login, Google OAuth2 integration, security lockout, and redirection based on role.
* **Preconditions:** User has a device with internet access.
* **Postconditions:** Active authentication session established or new account registered.
* **Normal Flow:** 
  1. Guest submits register form or clicks Google OAuth button.
  2. System validates fields, bhashes passwords using BCrypt, or queries email against Google profile.
  3. System sends OTP for online registration or establishes active session for OAuth users.
* **Business Rules:** BR-SYS-01, BR-SYS-02, BR-SYS-03.

#### **UC02: Password Reset**
* **Primary Actor:** Customer / Staff
* **Description:** Initiates secure forgot-password flows using tokenized email validation.
* **Preconditions:** Account exists under target email.
* **Normal Flow:** User submits email -> System generates UUID token valid for 15 minutes -> Emails reset link -> User opens link, enters new password -> System updates credentials and deletes token.

#### **UC03: Profile & Dependent Management**
* **Primary Actor:** Customer
* **Description:** Manages customer profile data and links accompanying travelers (dependents) for sảnh check-in compliance.
* **Normal Flow:** Customer updates profile fields -> Optional: adds accompanying dependent names and document IDs.
* **Tác động DB:** `Customers` (UPDATE), `Dependents` (INSERT/UPDATE/DELETE).

#### **UC04: FaceID Enrollment & Verification**
* **Primary Actor:** Customer / Tour Guide
* **Description:** Captures photo templates to extract and match 128-dimensional facial vector signatures using Python AI service.
* **Normal Flow:** Customer uploads direct headshot -> Python service extracts vector and saves to database -> Tour guide scans customer's face at checkpoints to match vector and confirm attendance status.

#### **UC05: Authorization & Security**
* **Primary Actor:** Admin / System
* **Description:** Restricts access via permission-based RBAC, intercepts transactions via AOP to record audit logs, and monitors revisions with Hibernate Envers.
* **Business Rules:** BR-SYS-04, BR-SYS-06.

#### **UC06: Master Data — Rooms**
* **Primary Actor:** Admin
* **Description:** Master data configurations of physical rooms and Categories.

#### **UC07: Master Data — Tables**
* **Primary Actor:** Admin / Manager
* **Description:** Master data configurations of restaurant tables, capacities, and layout grids.

#### **UC08: Master Data — Tours**
* **Primary Actor:** Admin / Manager
* **Description:** CRUD operations on Tour routes, itineraries, scheduling, and guide assignments.

#### **UC09: Pricing, Marketing & Workflows**
* **Primary Actor:** Admin / Manager
* **Description:** Configures daily rates, dynamic season schedules, promo codes, and JSON-based workflow schemas.

#### **UC10: Room Availability Search**
* **Primary Actor:** Guest / Customer / Receptionist
* **Description:** Queries available rooms for target check-in/out dates, computing seasonal costs via `Daily_Rates`.
* **Preconditions:** Check-in date >= today; Check-out date > check-in.

#### **UC11: Temporary Room Holding**
* **Primary Actor:** System / Customer
* **Description:** Sets a 15-minute Cart Lock on chosen rooms to prevent double-booking during transaction checkouts.

#### **UC12: Front-Desk Operations**
* **Primary Actor:** Receptionist / Customer
* **Description:** Orchestrates Check-ins (CCCD OCR scan, primary contact linkage, and credit limit setup), Room swappings, Walk-ins, and checkout settlements.
* **Business Rules:** BR-FO-01, BR-FO-02, BR-FO-03.

#### **UC13: Housekeeping & Maintenance**
* **Primary Actor:** Housekeeper / Maintenance Staff / System
* **Description:** Auto-creates cleaning tasks upon check-out, updates room cleanliness status, and tracks mechanical repair tickets.
* **Business Rules:** BR-HK-02, BR-HK-03, BR-HK-04.

#### **UC14: Restaurant Table Reservations**
* **Primary Actor:** Customer / Receptionist
* **Description:** Reserves dining tables for specific times and party sizes, validating against table capacities.

#### **UC15: Menu Configuration**
* **Primary Actor:** Admin / Manager
* **Description:** Configures restaurant menu items, pricing, availability toggles, and allergy information labels.

#### **UC16: Room Service Ordering**
* **Primary Actor:** Customer
* **Description:** In-room dining ordering via scanning QR codes, with options to post to room folio.
* **Preconditions:** Customer must be currently checked in.

#### **UC17: POS Dine-In Order Creation**
* **Primary Actor:** F&B Staff
* **Description:** Staff enters dine-in table orders on restaurant tablets.

#### **UC18: POS Settlement & Post-to-Room**
* **Primary Actor:** F&B Staff / Customer / Cashier
* **Description:** Routes dining bills directly to guest room folios using PIN validation and credit limit checks.
* **Business Rules:** BR-FB-01, BR-FB-05.

#### **UC19: KDS Real-time Kitchen Screen**
* **Primary Actor:** Kitchen Staff
* **Description:** Displays real-time cooking tickets, updating states from Cooking to Ready and broadcasting out-of-stock items.

#### **UC20: Tour Searching & Weather**
* **Primary Actor:** Guest / Customer
* **Description:** Searches local sightseeing packages, integrating OpenWeather API forecasts for safety.

#### **UC21: Tour Booking & Capacity**
* **Primary Actor:** Customer / Receptionist
* **Description:** Registers seats on tour routes, validating capacity limit checks.
* **Business Rules:** BR-TR-01.

#### **UC22: Tour Operation & GPS**
* **Primary Actor:** Tour Guide / Admin
* **Description:** Manages staff assignments, tracks vehicle route coordinates, validates checkpoints via AI Face Scan, and coordinates weather cancellations.
* **Business Rules:** BR-TR-02, BR-TR-05, BR-TR-06.

#### **UC23: Service Add-ons Booking**
* **Primary Actor:** Customer / Receptionist
* **Description:** Books spa sessions, gym schedules, and shuttle bus transfers, billing directly to the folio.

#### **UC24: Submit Service Reviews**
* **Primary Actor:** Customer
* **Description:** Allows guests to write rating reviews within 7 days of check-out or tour completion.

#### **UC25: Review Moderation System**
* **Primary Actor:** Admin
* **Description:** Audits and decides to approve or hide reviews on public portal pages.

#### **UC26: Folio Aggregation**
* **Primary Actor:** Receptionist / Customer
* **Description:** Integrates room charges, dining fees, tour tickets, and incidentals into room sub-folios.

#### **UC27: Night Audit & e-Invoice**
* **Primary Actor:** System / Receptionist
* **Description:** Runs automated 02:00 AM cronjobs posting daily room rates, locks journals, finishes guest checkouts, and sends e-Invoice PDFs.
* **Business Rules:** BR-FIN-01, BR-FIN-03.

#### **UC28: Manager Dashboard & USALI**
* **Primary Actor:** Manager
* **Description:** Tracks strategic indicators, visualizes USALI category metrics, and exports reports.
* **Business Rules:** BR-FIN-04.

#### **UC29: Automated Notification Emails**
* **Primary Actor:** System
* **Description:** Dispatch mechanism for registration OTPs, booking confirmations, invoice attachments, and security alerts.

#### **UC30: Scheduled Jobs Engine**
* **Primary Actor:** System
* **Description:** Manages background schedulers (cart timeouts, table releases, and night audits).

#### **UC31: Landing Pages & Portal**
* **Primary Actor:** Guest / Customer
* **Description:** Guest-facing portal showcasing room catalogs, restaurants, tours, and reviews.

### 3. Functional Requirements

> Provide descriptions about the system's functions/screens. The functions/screens are grouped by the system features, and even sub-features if needed. For the screens, you need to provide the screen layouts (mock-up screens) and relevant specifications if needed.

#### 3.1 Feature Name1

##### 3.1.1 SubFeature Name1.1

###### 3.1.1.1 Screen/Function Name1

> [Content #1: UI layout (Mockup screen prototype)]
>
> [Content #2: Brief descriptions of the screen/function, mapped to the relevant use cases]
>
> [Content #3: Provide further descriptions for the screen's components/fields using table format below]

| Field Name | Description |
|---|---|
| Field Name1 | Field description: data type, min/max length or value, initial data, etc. |
| Field Name2 | ... |
| **Field Group-Name1** | |
| Field Name3 | ... |
| Field Name4 | ... |
| **Field Group-Name2** | |
| ... | ... |

###### 3.1.1.2 Screen/Function Name2

...

##### 3.1.2 SubFeature Name1.2

...

#### 3.2 User Authentication

##### 3.2.1 User Register
...

##### 3.2.2 User Login
...

##### 3.2.3 Password Reset
...

#### 3.3 System Administration

##### 3.3.1 Master Data

###### 3.3.1.1 Setting List

This screen allows the Administrator to:
- **View Setting List:** view list of current master data.
- **Filter Setting List:** filter master data by data types, statuses.
- **Search Settings:** enter keyword(s) to search master data by their names or values.
- **Sort Setting List:** sort master data list (ascending, descending) by clicking column headers.

On the screen, s/he can also:
- **Activate/Deactivate Setting:** change status of a specific inactive/active master data.
- Choose to go to the Setting Details screens for adding new or updating an existing master data by clicking the New Setting or Edit link.

| Field | Description |
|---|---|
| (1) | Initial values: all the active setting names with null or blank type. Hover the mouse to show the field name: "Setting Type" |
| (2) | Initial values: All Statuses, Active, Inactive (default value "All Status"). Hover the mouse to show the field name: "Setting Status" |
| (3) | The change-status action is Activate or Deactivate depending on the current status of the relevant setting (Inactive or Active, respectively). |

###### 3.3.1.2 Setting Details

This screen allows the Administrator to:
- **Add New Setting:** add new master data.
- **Update Setting Details:** update details of a specific master data.

| Field | Description |
|---|---|
| Name | Data type: non-digit string, max length of 20 characters |
| Type | Initial data values: all active setting names (with null or blank type) |
| Value | Data type: any string, max length of 100 characters |
| Priority | Data type: a positive integer |
| Description | Data type: any string, max length of 200 characters |

##### 3.3.2 User Management

###### 3.3.2.1 User List
...

###### 3.3.2.2 User Details
...

---

### 4. Non-Functional Requirements

#### 3.1 External Interfaces

> This section provides information to ensure that the system will communicate properly with users and with external hardware or software/system elements.

#### 3.2 Quality Attributes

> List all the required system characteristics (quality attributes) specification. Some of the possible attributes are provided with the guide/descriptions are mentioned here.

##### 3.2.1 Usability

> This section includes all those requirements that affect usability. For example, specify the required training time for a normal user and a power user to become productive at particular operations; specify measurable task times for typical tasks or base the new system's usability requirements on other systems that the users know and like; specify requirement to conform to common usability standards, such as IBM's CUA standards or Microsoft's GUI standards.

##### 3.2.2 Performance

> The system's performance characteristics are outlined in this section. Include specific response times. Where applicable, reference related Use Cases by name.
>
> - Response time for a transaction (average, maximum)
> - Throughput, for example, transactions per second
> - Capacity, for example, the number of customers or transactions the system can accommodate
> - Resource utilization, such as memory, disk, communications, and so forth.

##### 3.2.3 ...

---

### 5. Requirement Appendix

> Provide business rules, common requirements, or other extra requirements information here.

#### 5.1 Business Rules

| ID | Rule Definition |
|---|---|
| BR-SYS-01 | All personal identification information (Citizen ID, Passport) and passwords must be encrypted before being stored in the database. Passwords use BCrypt hashing; identity documents use AES-256 encryption. |
| BR-SYS-02 | Accounts that fail login more than five consecutive times shall be locked for 15 minutes. Email Codes expire after 3 minutes. |
| BR-SYS-03 | Employee sessions shall automatically expire after 15 minutes of inactivity. |
| BR-SYS-04 | All critical actions such as booking cancellation, invoice modification, permission changes, and pricing updates must be recorded in the Audit Log and cannot be deleted. |
| BR-SYS-05 | Customer deletion requests must be handled using soft deletion by anonymizing personal information while preserving transaction records. |
| BR-FO-01 | The system must prevent room overbooking by ensuring that the same physical room cannot be assigned to overlapping reservations. Database transactions using SELECT ... FOR UPDATE must be applied. |
| BR-FO-02 | Online room bookings remain reserved for 15 minutes only. If no deposit payment is received within this period, the booking is automatically cancelled and the room is released back to inventory. |
| BR-FO-03 | The representative guest performing check-in must be at least 18 years old and must present valid identification documents (Citizen ID or Passport). |
| BR-FO-04 | Room status must follow the lifecycle: Vacant_Clean → Occupied → Vacant_Dirty → Vacant_Clean. Only rooms with Vacant_Clean status may be assigned during check-in. |
| BR-FO-05 | Rooms flagged as Rush Room must be prioritized and moved to the top of the housekeeping task queue. |
| BR-FB-01 | Food and room service charges may only be posted to rooms that are currently occupied and belong to the registered guest. PIN verification and Credit Limit checks are required before recording any Folio charge. |
| BR-FB-02 | When a menu item is marked as unavailable by the kitchen, it must immediately become unavailable across all POS terminals and customer ordering interfaces via real-time broadcast. |
| BR-FB-03 | Reserved restaurant tables are held for a maximum of 30 minutes after the scheduled reservation time. If the customer has not arrived, the table is automatically released. |
| BR-TR-01 | The system must prevent tour overbooking by ensuring that the total number of booked participants cannot exceed the available seats of a Tour Schedule. |
| BR-TR-02 | AI Face Scan attendance is considered valid only when facial similarity reaches at least 85% compared to the registered customer image. Results below this threshold must fall back to manual attendance. |
| BR-TR-03 | Customers may submit reviews only within 7 days after tour completion or room check-out. |
| BR-TR-04 | Administrators may hide or show reviews but are not permitted to modify review content. A moderation reason must always be recorded in the Audit Log. |
| BR-FIN-01 | Check-out is not permitted until all outstanding balances in the consolidated invoice have been fully settled. |
| BR-FIN-02 | Room cancellations made more than 48 hours before the scheduled check-in date receive a full deposit refund. Cancellations within 48 hours or no-shows receive no refund. |
| BR-FIN-03 | The Night Audit process shall automatically run at 02:00 AM every day to close the previous business day, post nightly room charges to guest folios, and roll over to the new business date. |
| BR-FIN-04 | Revenue reports must classify all income into three distinct categories: Room Revenue, Food & Beverage Revenue, and Tour Revenue, in accordance with USALI standards. |
| BR-FIN-05 | A tour schedule is automatically cancelled and 100% refunds are issued to all registered participants if the total confirmed bookings fall below the minimum participant threshold at 24 hours prior to departure. |

#### 5.2 System Messages

| # | Message Code | Message Type | Context | Content |
|---|---|---|---|---|
| 1 | MSG01 | In line | There is not any search result | No search results. |
| 2 | MSG02 | In red, under the text box | Input-required fields are empty | The * field is required. |
| 3 | MSG03 | Toast message | Updating asset(s) information successfully | Update asset(s) successfully. |
| 4 | MSG04 | Toast message | Adding new asset successfully | Add asset successfully. |
| 5 | MSG05 | Toast message | Confirming email of asset hand-over is sent successfully | A confirmation email has been sent to {email_address}. |
| 6 | MSG06 | Toast message | Resetting asset information successfully | Return asset(s) successfully. |
| 7 | MSG07 | Toast message | Deleting asset information successfully | Delete asset(s) successfully. |
| 8 | MSG08 | In red, under the text box | Input value length > max length | Exceed max length of {max_length}. |
| 9 | MSG09 | In line | Username or password is not correct when clicking sign-in | Incorrect user name or password. Please check again. |
| 10 | MSG10 | In red | Login fields blank | Email or password cannot be empty. |
| 11 | MSG11 | In line | Email not verified | Account not verified. Please check your email for the verification link. |
| 12 | MSG12 | In line | Account inactive | The account is blocked or inactive. Contact administrator. |
| 13 | MSG13 | Toast | Account locked | Account locked for 15 minutes due to multiple failed login attempts. |
| 14 | MSG14 | In red | Weak password | Passwords must be at least 8 characters, including letters, numbers, and symbols. |

#### 5.3 Other Requirements