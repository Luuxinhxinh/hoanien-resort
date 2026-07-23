package com.kawai.services.impl;

import com.kawai.models.*;
import com.kawai.repositories.*;
import com.kawai.services.interfaces.CheckinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * CheckinServiceImpl — UC12: Check-in / Check-out / Đổi phòng.
 * <p>
 * Chịu trách nhiệm xử lý vòng đời lưu trú của khách tại tiền sảnh:
 * check-in, quản lý hạn mức, đổi phòng, và nâng cấp người đi kèm.
 * <p>
 * Business Rules áp dụng:
 * <ul>
 * <li>BR-FO-04: Luân chuyển trạng thái phòng (Vacant_Clean → Occupied →
 * Dirty)</li>
 * <li>BR-FO-06: Hạn mức chi tiêu phòng (Credit Limit)</li>
 * <li>BR-HK-03: Chặn Check-in phòng đang bảo trì</li>
 * <li>BR-SYS-01: Mã hóa dữ liệu nhạy cảm</li>
 * </ul>
 */
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CheckinServiceImpl implements CheckinService {

        // ===== Constants =====
        private static final String STATUS_DIRTY = "Vacant_Dirty";
        private static final String STATUS_MAINTENANCE = "Maintenance";
        private static final String STATUS_VACANT_CLEAN = "Vacant_Clean";
        private static final String STATUS_AVAILABLE = "Available";
        private static final String STATUS_OCCUPIED = "Occupied";
        private static final String STATUS_CHECKED_IN = "CHECKED_IN";
        private static final String ROLE_CUSTOMER = "CUSTOMER NORMAL";
        private static final String TEMP_PHONE = "PENDING";
        private static final String DEFAULT_MEMBERSHIP = "Regular";

        private final RoomBookingDetailRepository roomBookingDetailRepo;
        private final RoomRepository roomRepo;
        private final RoomBookingRepository roomBookingRepo;
        private final CustomerRepository customerRepo;
        private final AccountRepository accountRepo;
        private final RoleRepository roleRepo;
        private final DependentRepository dependentRepo;
        private final com.kawai.repositories.MembershipTierRepository membershipTierRepo;
        private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
        private final com.kawai.repositories.RoomGuestRepository roomGuestRepo;
        private final com.kawai.repositories.TourBookingRepository tourBookingRepo;
        private final com.kawai.services.interfaces.DependentService dependentService;
        private final com.kawai.repositories.MaintenanceRequestRepository maintenanceRequestRepo;
        private final com.kawai.services.interfaces.EmailService emailService;
        private final com.kawai.services.interfaces.WorkflowEngineService workflowEngineService;

        @Autowired
        public CheckinServiceImpl(
                        RoomBookingDetailRepository roomBookingDetailRepo,
                        RoomRepository roomRepo,
                        RoomBookingRepository roomBookingRepo,
                        CustomerRepository customerRepo,
                        AccountRepository accountRepo,
                        RoleRepository roleRepo,
                        DependentRepository dependentRepo,
                        com.kawai.repositories.MembershipTierRepository membershipTierRepo,
                        org.springframework.security.crypto.password.PasswordEncoder passwordEncoder,
                        com.kawai.repositories.RoomGuestRepository roomGuestRepo,
                        com.kawai.repositories.TourBookingRepository tourBookingRepo,
                        com.kawai.repositories.MaintenanceRequestRepository maintenanceRequestRepo,
                        @org.springframework.context.annotation.Lazy com.kawai.services.interfaces.DependentService dependentService,
                        com.kawai.services.interfaces.EmailService emailService,
                        @org.springframework.context.annotation.Lazy com.kawai.services.interfaces.WorkflowEngineService workflowEngineService) {
                this.roomBookingDetailRepo = roomBookingDetailRepo;
                this.roomRepo = roomRepo;
                this.roomBookingRepo = roomBookingRepo;
                this.customerRepo = customerRepo;
                this.accountRepo = accountRepo;
                this.roleRepo = roleRepo;
                this.dependentRepo = dependentRepo;
                this.membershipTierRepo = membershipTierRepo;
                this.passwordEncoder = passwordEncoder;
                this.roomGuestRepo = roomGuestRepo;
                this.tourBookingRepo = tourBookingRepo;
                this.maintenanceRequestRepo = maintenanceRequestRepo;
                this.dependentService = dependentService;
                this.emailService = emailService;
                this.workflowEngineService = workflowEngineService;
        }

        // UC12.1: Check-in
        @Override
        @Transactional
        public RoomBookingDetail checkIn(Long bookingDetailId, Long roomId, java.math.BigDecimal allocatedCreditLimit) {
                RoomBookingDetail detail = findBookingDetail(bookingDetailId);

                if (STATUS_CHECKED_IN.equalsIgnoreCase(detail.getDetailStatus())) {
                        throw new IllegalStateException("BookingDetail đã CHECKED_IN không được check-in lại");
                }
                String bookingStatus = detail.getRoomBooking().getBookingStatus();
                if (!"CONFIRMED".equalsIgnoreCase(bookingStatus) && !"Checked_In".equalsIgnoreCase(bookingStatus)) {
                        throw new IllegalStateException("Booking chưa CONFIRMED không được phép check-in");
                }

                Room room = findRoom(roomId);

                validateRoomAvailableForCheckin(room);

                assignRoomToGuest(detail, room, allocatedCreditLimit);

                return roomBookingDetailRepo.save(detail);
        }

        private void assignRoomToGuest(RoomBookingDetail detail, Room room, java.math.BigDecimal allocatedCreditLimit) {
                detail.setRoom(room);
                if (allocatedCreditLimit != null && allocatedCreditLimit.compareTo(java.math.BigDecimal.ZERO) > 0) {
                        detail.setSubCreditLimit(allocatedCreditLimit);
                } else if (detail.getSubCreditLimit() == null || detail.getSubCreditLimit().compareTo(java.math.BigDecimal.ZERO) == 0) {
                        java.math.BigDecimal defaultLimit = (detail.getRoomBooking() != null && detail.getRoomBooking().getCreditLimit() != null)
                                        ? detail.getRoomBooking().getCreditLimit()
                                        : new java.math.BigDecimal("5000000.00");
                        detail.setSubCreditLimit(defaultLimit);
                }
                detail.setDetailStatus(STATUS_CHECKED_IN);

                room.setRoomStatus(STATUS_OCCUPIED);
                room.setCurrentBookingDetailId(detail.getId());

                roomBookingDetailRepo.save(detail);
                roomRepo.save(room);

                // Cập nhật trạng thái của toàn bộ Booking sang Checked_In
                RoomBooking parent = detail.getRoomBooking();
                if (parent != null && "CONFIRMED".equalsIgnoreCase(parent.getBookingStatus())) {
                        java.util.List<RoomBookingDetail> allDetails = roomBookingDetailRepo
                                        .findByRoomBookingId(parent.getId());
                        boolean allCheckedIn = true;
                        for (RoomBookingDetail d : allDetails) {
                                if (!d.getId().equals(detail.getId())
                                                && !STATUS_CHECKED_IN.equalsIgnoreCase(d.getDetailStatus())) {
                                        allCheckedIn = false;
                                        break;
                                }
                        }
                        if (allCheckedIn) {
                                parent.setBookingStatus("Checked_In");
                                roomBookingRepo.save(parent);
                        }
                }
        }

        private void validateRoomAvailableForCheckin(Room room) {
                String status = room.getRoomStatus();
                // Chặn Maintenance, Occupied, và Vacant_Dirty (phòng chưa dọn dẹp)
                if (STATUS_MAINTENANCE.equalsIgnoreCase(status)) {
                        throw new IllegalStateException(
                                        "ROOM-001: Phòng đang MAINTENANCE, đang bảo trì. Không thể check-in. (BR-HK-03)");
                }
                if (STATUS_OCCUPIED.equalsIgnoreCase(status)) {
                        throw new IllegalStateException(
                                        "ROOM-001: Phòng đang Occupied, không thể check-in. (MOD2-002)");
                }
                if (STATUS_DIRTY.equalsIgnoreCase(status) || "Dirty".equalsIgnoreCase(status)) {
                        throw new IllegalStateException(
                                        "ROOM-001: Phòng đang DIRTY, chưa dọn dẹp xong. Không thể check-in. (BR-FO-04)");
                }
                boolean hasPendingMaintenance = maintenanceRequestRepo.existsByRoomIdAndStatusInAndOperationalTypeIn(
                                room.getId(),
                                java.util.Arrays.asList("Pending", "InProgress"),
                                java.util.Arrays.asList("MAINTENANCE", "DAMAGE_CHECK"));
                if (hasPendingMaintenance) {
                        throw new IllegalStateException(
                                        "ROOM-001: Phòng đang có task bảo trì/kiểm tra chờ xử lý, không thể check-in.");
                }
        }

        // ========================================================================
        // UC12.2: Cập nhật Credit Limit
        // ========================================================================

        @Override
        @Transactional
        public void updateCreditLimit(Long bookingDetailId, BigDecimal newCreditLimit) {
                if (newCreditLimit == null || newCreditLimit.compareTo(BigDecimal.ZERO) < 0) {
                        throw new IllegalArgumentException("Credit Limit null hoặc âm không hợp lệ (MOD2-001)");
                }
                RoomBookingDetail detail = findBookingDetail(bookingDetailId);
                RoomBooking parent = detail.getRoomBooking();

                BigDecimal currentTotal = BigDecimal.ZERO;
                java.util.List<RoomBookingDetail> allDetails = roomBookingDetailRepo
                                .findByRoomBookingId(parent.getId());
                for (RoomBookingDetail d : allDetails) {
                        if (!d.getId().equals(detail.getId()) && d.getSubCreditLimit() != null) {
                                currentTotal = currentTotal.add(d.getSubCreditLimit());
                        }
                }

                if (parent.getCreditLimit() != null
                                && currentTotal.add(newCreditLimit).compareTo(parent.getCreditLimit()) > 0) {
                        throw new com.kawai.exceptions.BusinessException("MOD2-UC14-016",
                                        "Tổng hạn mức phân bổ vượt quá hạn mức tổng");
                }

                detail.setSubCreditLimit(newCreditLimit);
                roomBookingDetailRepo.save(detail);
        }

        // UC12.4: Nâng cấp Dependent → Customer
        @Override
        @Transactional
        public java.util.Map<String, Object> upgradeDependentToCustomer(Long dependentId, Long roomBookingDetailId) {
                Dependent dependent = findDependent(dependentId);

                // Xác định Customer sẽ gán vào (cũ hoặc mới)
                com.kawai.models.Customer targetCustomer;
                String username;
                String password;
                boolean isExistingCustomer = false;

                // Guard: Nếu CCCD đã có Customer → tái sử dụng, không tạo mới
                if (dependent.getCccdPassportEncrypted() != null
                                && !dependent.getCccdPassportEncrypted().isBlank()) {
                        java.util.Optional<com.kawai.models.Customer> existingCustomerOpt = customerRepo
                                        .findFirstByCccdPassportEncrypted(
                                                        dependent.getCccdPassportEncrypted());
                        if (existingCustomerOpt.isPresent()) {
                                targetCustomer = existingCustomerOpt.get();
                                username = targetCustomer.getAccount() != null
                                                ? targetCustomer.getAccount().getUsername()
                                                : "";
                                password = null;
                                isExistingCustomer = true;
                        } else {
                                // CCCD chưa có Customer → tạo mới
                                String[] creds = new String[2];
                                targetCustomer = createNewCustomerFromDependent(dependent, creds);
                                username = creds[0];
                                password = creds[1];
                        }
                } else {
                        // Không có CCCD → tạo mới
                        String[] creds = new String[2];
                        targetCustomer = createNewCustomerFromDependent(dependent, creds);
                        username = creds[0];
                        password = creds[1];
                }

                // Unlink RoomGuest CỦA BOOKING HIỆN TẠI (theo roomBookingDetailId)
                // → Chỉ gán Customer, không xóa Dependent nếu booking khác vẫn dùng
                unlinkCurrentBookingRoomGuest(dependentId, roomBookingDetailId, targetCustomer);

                // Kiểm tra còn booking nào khác dùng Dependent này không
                // Nếu không còn → xóa Dependent để tránh rác DB
                long remainingRefs = roomGuestRepo.findAllByDependentId(dependentId).size();
                if (remainingRefs == 0) {
                        dependentRepo.delete(dependent);
                }

                // Gửi email nếu vừa tạo Customer mới
                if (!isExistingCustomer && dependent.getCustomer() != null) {
                        try {
                                emailService.sendDependentUpgradeEmail(dependent.getCustomer(),
                                                targetCustomer, username, password);
                        } catch (Exception e) {
                                log.error("Lỗi gửi email thông báo nâng cấp Dependent: ", e);
                        }
                }

                java.util.Map<String, Object> result = new java.util.HashMap<>();
                result.put("customer", targetCustomer);
                result.put("username", username);
                result.put("password", isExistingCustomer ? null : password);
                result.put("isExistingCustomer", isExistingCustomer);
                return result;
        }

        /**
         * Unlink chỉ RoomGuest thuộc booking hiện tại (roomBookingDetailId).
         * Nếu roomBookingDetailId null → unlink record có dependent và có
         * RoomBookingDetail đầu tiên tìm thấy.
         * Không động đến các RoomGuest thuộc booking khác.
         */
        private void unlinkCurrentBookingRoomGuest(Long dependentId, Long roomBookingDetailId,
                        com.kawai.models.Customer targetCustomer) {
                java.util.List<com.kawai.models.RoomGuest> allRgs = roomGuestRepo.findAllByDependentId(dependentId);

                com.kawai.models.RoomGuest targetRg = null;
                if (roomBookingDetailId != null) {
                        // Tìm đúng RoomGuest thuộc booking đang xử lý
                        targetRg = allRgs.stream()
                                        .filter(rg -> rg.getRoomBookingDetail() != null
                                                        && rg.getRoomBookingDetail().getId()
                                                                        .equals(roomBookingDetailId))
                                        .findFirst().orElse(null);
                }
                // Fallback: nếu không tìm được → lấy cái đầu tiên có RoomBookingDetail
                if (targetRg == null) {
                        targetRg = allRgs.stream()
                                        .filter(rg -> rg.getRoomBookingDetail() != null)
                                        .findFirst().orElse(allRgs.isEmpty() ? null : allRgs.get(0));
                }

                if (targetRg != null) {
                        targetRg.setCustomer(targetCustomer);
                        targetRg.setDependent(null);
                        targetRg.setIsPrimaryContact(true);
                        roomGuestRepo.saveAndFlush(targetRg); // flush ngay để DB cập nhật trước khi delete Dependent

                        if (targetRg.getRoomBookingDetail() != null) {
                                com.kawai.models.RoomBookingDetail detail = targetRg.getRoomBookingDetail();
                                detail.setCustomer(targetCustomer);
                                roomBookingDetailRepo.saveAndFlush(detail);
                        }
                }
        }

        /**
         * Tạo Customer + Account mới từ Dependent. Lưu ý: trả về Customer nhưng
         * passwordHash trong Account tạm thời chứa raw password để trả về cho Lễ tân.
         * Raw password được ghi đè bằng encoded trước khi save.
         */
        /**
         * Tạo Customer + Account mới từ Dependent.
         * Trả về String[]{rawPassword, username} để service có thể build response.
         */
        private com.kawai.models.Customer createNewCustomerFromDependent(Dependent dependent, String[] outCredentials) {
                Role customerRole = findCustomerRole();
                String randomSuffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
                String username = "guest_" + randomSuffix.substring(0, 4);
                String dummyEmail = username + "@kawai-resort.com";
                String rawPwd = randomSuffix.substring(4, 8)
                                + UUID.randomUUID().toString().replace("-", "").substring(0, 4);

                Account account = new Account();
                account.setUsername(username);
                account.setPasswordHash(passwordEncoder.encode(rawPwd));
                account.setIsActive(true);
                account.setRole(customerRole);
                Account savedAccount = accountRepo.save(account);

                Customer customer = new Customer();
                customer.setAccount(savedAccount);
                customer.setFullName(dependent.getDependentName());
                customer.setGender(dependent.getGender());
                customer.setBirthDate(dependent.getBirthDate());
                customer.setCccdPassportEncrypted(dependent.getCccdPassportEncrypted());
                customer.setPhone(TEMP_PHONE);
                customer.setEmail(dummyEmail);
                customer.setLoyaltyPoints(0);
                customer.setMembershipTier(
                                membershipTierRepo.findByTierNameIgnoreCase(DEFAULT_MEMBERSHIP).orElse(null));
                Customer savedCustomer = customerRepo.save(customer);

                // Trả raw credentials qua out-param array (tránh lưu plaintext vào entity)
                if (outCredentials != null && outCredentials.length >= 2) {
                        outCredentials[0] = username;
                        outCredentials[1] = rawPwd;
                }
                return savedCustomer;
        }

        private Account createAccountForDependent(Role customerRole, String username, String randomPwd) {
                Account account = new Account();
                account.setUsername(username);
                account.setPasswordHash(passwordEncoder.encode(randomPwd));
                account.setIsActive(true);
                account.setRole(customerRole);
                return accountRepo.save(account);
        }

        private Customer createCustomerFromDependent(Dependent dependent, Account account, String dummyEmail) {
                Customer customer = new Customer();
                customer.setAccount(account);
                customer.setFullName(dependent.getDependentName());
                customer.setGender(dependent.getGender());
                customer.setBirthDate(dependent.getBirthDate());
                customer.setCccdPassportEncrypted(dependent.getCccdPassportEncrypted());
                customer.setPhone(TEMP_PHONE);
                customer.setEmail(dummyEmail);
                customer.setLoyaltyPoints(0);
                customer.setMembershipTier(
                                membershipTierRepo.findByTierNameIgnoreCase(DEFAULT_MEMBERSHIP).orElse(null));
                return customerRepo.save(customer);
        }
        // Private helpers: repository lookups

        private RoomBookingDetail findBookingDetail(Long id) {
                return roomBookingDetailRepo.findById(id)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Booking detail không tìm thấy với ID: " + id));
        }

        private Room findRoom(Long id) {
                return roomRepo.findById(id)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Phòng không tìm thấy với ID: " + id));
        }

        private Dependent findDependent(Long id) {
                return dependentRepo.findById(id)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Người phụ thuộc không tìm thấy với ID: " + id));
        }

        private Role findCustomerRole() {
                return roleRepo.findByRoleName(ROLE_CUSTOMER)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Role CUSTOMER không tồn tại trong hệ thống"));
        }

        @Override
        @Transactional
        public void processBulkCheckin(com.kawai.dto.CheckinSubmitFormDTO form, Customer customer,
                        com.kawai.models.Booking booking) {
                java.util.List<com.kawai.models.RoomBookingDetail> details = roomBookingDetailRepo
                                .findByRoomBookingId(form.getBookingId());
                if (details == null || details.isEmpty()) {
                        throw new com.kawai.exceptions.BusinessException("CHECKIN-001",
                                        "Không tìm thấy thông tin phòng cho Đơn này!");
                }
                if (form.getAssignedRoomNumbers() == null || form.getAssignedRoomNumbers().isEmpty()) {
                        throw new com.kawai.exceptions.BusinessException("CHECKIN-002",
                                        "Bạn chưa chọn phòng vật lý nào để giao cho khách!");
                }

                java.util.List<com.kawai.models.RoomBookingDetail> pendingDetails = new java.util.ArrayList<>();
                for (com.kawai.models.RoomBookingDetail d : details) {
                        if (!STATUS_CHECKED_IN.equalsIgnoreCase(d.getDetailStatus())) {
                                pendingDetails.add(d);
                        }
                }

                if (form.getAssignedRoomNumbers() == null) {
                        form.setAssignedRoomNumbers(new java.util.ArrayList<>());
                }
                form.getAssignedRoomNumbers().removeIf(String::isEmpty);

                java.math.BigDecimal totalRequested = java.math.BigDecimal.ZERO;
                if (form.getAllocatedCreditLimits() != null && !form.getAllocatedCreditLimits().isEmpty()) {
                        for (java.math.BigDecimal limit : form.getAllocatedCreditLimits()) {
                                if (limit != null && limit.compareTo(java.math.BigDecimal.ZERO) < 0) {
                                        throw new com.kawai.exceptions.BusinessException("CHECKIN-008",
                                                        "Hạn mức không được là số âm!");
                                }
                                if (limit != null) {
                                        totalRequested = totalRequested.add(limit);
                                }
                        }
                }
                java.math.BigDecimal existingUsed = java.math.BigDecimal.ZERO;
                for (com.kawai.models.RoomBookingDetail d : details) {
                        if (!pendingDetails.contains(d) && d.getSubCreditLimit() != null) {
                                existingUsed = existingUsed.add(d.getSubCreditLimit());
                        }
                }
                com.kawai.models.RoomBooking roomBooking = (com.kawai.models.RoomBooking) booking;
                java.math.BigDecimal masterCreditLimit = roomBooking.getCreditLimit() != null
                                ? roomBooking.getCreditLimit()
                                : new java.math.BigDecimal("5000000.00");
                if (existingUsed.add(totalRequested).compareTo(masterCreditLimit) > 0) {
                        throw new com.kawai.exceptions.BusinessException("CHECKIN-007",
                                        "Tổng hạn mức cấp cho các phòng vượt quá hạn mức của tài khoản (Master: "
                                                        + masterCreditLimit
                                                        + ")");
                }

                if (form.getAssignedRoomNumbers().size() != pendingDetails.size()) {
                        throw new com.kawai.exceptions.BusinessException("CHECKIN-005",
                                        "Bạn phải phân đủ " + pendingDetails.size()
                                                        + " phòng trước khi hoàn tất Check-in!");
                }

                java.util.Map<String, Long> roomNumberToDetailIdMap = new java.util.HashMap<>();

                for (String roomNumber : form.getAssignedRoomNumbers()) {
                        com.kawai.models.Room room = roomRepo.findByRoomNumber(roomNumber)
                                        .orElseThrow(() -> new com.kawai.exceptions.BusinessException("CHECKIN-003",
                                                        "Không tìm thấy phòng số " + roomNumber));

                        com.kawai.models.RoomBookingDetail matchedDetail = null;
                        for (com.kawai.models.RoomBookingDetail d : pendingDetails) {
                                if (d.getCategory().getId().equals(room.getCategory().getId())) {
                                        matchedDetail = d;
                                        break;
                                }
                        }

                        if (matchedDetail == null) {
                                throw new com.kawai.exceptions.BusinessException("CHECKIN-004",
                                                "Phòng " + roomNumber + " thuộc hạng "
                                                                + room.getCategory().getCategoryName()
                                                                + " không khớp với bất kỳ hạng phòng nào đang chờ check-in của đơn này!");
                        }
                        pendingDetails.remove(matchedDetail);

                        java.math.BigDecimal allocatedLimit = null;
                        int roomIdx = form.getAssignedRoomNumbers().indexOf(roomNumber);
                        if (form.getAllocatedCreditLimits() != null
                                        && roomIdx < form.getAllocatedCreditLimits().size()) {
                                allocatedLimit = form.getAllocatedCreditLimits().get(roomIdx);
                        }
                        this.checkIn(matchedDetail.getId(), room.getId(), allocatedLimit);
                        roomNumberToDetailIdMap.put(roomNumber, matchedDetail.getId());
                }

                if (form.getAssignedRoomNumbers() != null && !form.getAssignedRoomNumbers().isEmpty()) {
                        String firstRoom = form.getAssignedRoomNumbers().get(0);
                        Long firstDetailId = roomNumberToDetailIdMap.get(firstRoom);

                        if (firstDetailId != null && customer != null) {
                                com.kawai.models.RoomBookingDetail firstDetail = roomBookingDetailRepo
                                                .findById(firstDetailId)
                                                .orElse(null);
                                if (firstDetail != null) {
                                        com.kawai.models.RoomGuest existingMasterGuest = null;
                                        for (com.kawai.models.RoomBookingDetail detail : details) {
                                                java.util.List<com.kawai.models.RoomGuest> detailGuests = roomGuestRepo
                                                                .findByRoomBookingDetailId(detail.getId());
                                                for (com.kawai.models.RoomGuest rg : detailGuests) {
                                                        if (rg.getCustomer() != null && rg.getCustomer().getId()
                                                                        .equals(customer.getId())) {
                                                                existingMasterGuest = rg;
                                                                break;
                                                        }
                                                }
                                                if (existingMasterGuest != null)
                                                        break;
                                        }

                                        if (existingMasterGuest != null) {
                                                existingMasterGuest.setRoomBookingDetail(firstDetail);
                                                existingMasterGuest.setIsPrimaryContact(true);
                                                roomGuestRepo.saveAndFlush(existingMasterGuest);
                                        } else {
                                                com.kawai.models.RoomGuest masterGuest = new com.kawai.models.RoomGuest();
                                                masterGuest.setRoomBookingDetail(firstDetail);
                                                masterGuest.setCustomer(customer);
                                                masterGuest.setGuestType("ADULT");
                                                masterGuest.setIsPrimaryContact(true);
                                                roomGuestRepo.saveAndFlush(masterGuest);
                                        }
                                }
                        }
                }

                if (form.getDependents() != null) {
                        for (com.kawai.dto.DependentRegistrationDTO dependentDTO : form.getDependents()) {
                                if (dependentDTO != null && dependentDTO.getFullName() != null
                                                && !dependentDTO.getFullName().trim().isEmpty()) {
                                        if (dependentDTO.getAssignedPhysicalRoomNumber() != null
                                                        && !dependentDTO.getAssignedPhysicalRoomNumber().isEmpty()) {
                                                Long detailId = roomNumberToDetailIdMap
                                                                .get(dependentDTO.getAssignedPhysicalRoomNumber());
                                                if (detailId != null) {
                                                        dependentDTO.setRoomBookingDetailId(detailId);
                                                }
                                        }
                                        dependentService.registerDependent(form.getBookingId(), dependentDTO);
                                }
                        }
                }

                if (form.getAssignedRoomNumbers() != null && !form.getAssignedRoomNumbers().isEmpty()) {
                        for (String roomNumber : form.getAssignedRoomNumbers()) {
                                Long detailId = roomNumberToDetailIdMap.get(roomNumber);
                                if (detailId != null) {
                                        java.util.List<com.kawai.models.RoomGuest> guests = roomGuestRepo
                                                        .findByRoomBookingDetailId(detailId);
                                        long primaryCount = guests.stream()
                                                        .filter(g -> Boolean.TRUE.equals(g.getIsPrimaryContact()))
                                                        .count();
                                        if (primaryCount != 1) {
                                                throw new com.kawai.exceptions.BusinessException("CHECKIN-006",
                                                                "Phòng " + roomNumber
                                                                                + " phải có đúng 1 người đứng đầu!");
                                        }
                                }
                        }
                }

                // Kịch bản 1: Trigger Workflow ROOM_CHECKIN để hỗ trợ Khách VIP (F&B / Zalo)
                if (customer != null && form.getAssignedRoomNumbers() != null) {
                        for (String roomNumber : form.getAssignedRoomNumbers()) {
                                Long detailId = roomNumberToDetailIdMap.get(roomNumber);
                                com.kawai.models.Room room = roomRepo.findByRoomNumber(roomNumber).orElse(null);
                                if (detailId != null && room != null) {
                                        java.util.Map<String, Object> payload = new java.util.HashMap<>();
                                        payload.put("booking_id", booking.getId());
                                        payload.put("booking_detail_id", detailId);
                                        payload.put("room_id", room.getId());
                                        payload.put("room_number", room.getRoomNumber());
                                        payload.put("customer_email", customer.getEmail());
                                        payload.put("customer_name", customer.getFullName());

                                        String tier = "NONE";
                                        if (customer.getMembershipTier() != null
                                                        && customer.getMembershipTier().getTierName() != null) {
                                                tier = customer.getMembershipTier().getTierName().toUpperCase();
                                        }
                                        payload.put("customer_tier", tier);

                                        workflowEngineService.triggerEvent("ROOM_CHECKIN", payload);
                                }
                        }
                }

                // Gửi email xác nhận Check-in cho Khách đặt phòng chính (Sử dụng template walkin-checkin-existing)
                if (emailService != null && customer != null && roomBooking != null) {
                        try {
                                com.kawai.models.RoomBookingDetail mailDetail = null;
                                if (form.getAssignedRoomNumbers() != null && !form.getAssignedRoomNumbers().isEmpty()) {
                                        Long fId = roomNumberToDetailIdMap.get(form.getAssignedRoomNumbers().get(0));
                                        if (fId != null) {
                                                mailDetail = roomBookingDetailRepo.findById(fId).orElse(null);
                                        }
                                }
                                if (mailDetail != null) {
                                        emailService.sendWalkInCheckInEmail(roomBooking, mailDetail, customer, false, null, null);
                                }
                        } catch (Exception e) {
                                log.error("Lỗi gửi email xác nhận Check-in Booking Online: ", e);
                        }
                }

        }

        // UC12.3: Đổi phòng
        @Override
        @Transactional
        public RoomBookingDetail transferRoom(Long bookingDetailId, Long newRoomId) {
                RoomBookingDetail detail = findBookingDetail(bookingDetailId);

                // Guard: detail phải có phòng cũ
                Room oldRoom = detail.getRoom();
                if (oldRoom == null) {
                        throw new RuntimeException("Booking detail chưa được gán phòng, không thể đổi phòng");
                }

                // Guard: detail phải đang CHECKED_IN
                if (!STATUS_CHECKED_IN.equalsIgnoreCase(detail.getDetailStatus())) {
                        throw new IllegalStateException("Booking detail chưa CHECKED_IN, không thể đổi phòng");
                }

                Room newRoom = findRoom(newRoomId);

                // Validate phòng mới phải Vacant_Clean
                if (!STATUS_VACANT_CLEAN.equalsIgnoreCase(newRoom.getRoomStatus())) {
                        throw new IllegalStateException(
                                        "Phòng mới không khả dụng (trạng thái: " + newRoom.getRoomStatus()
                                                        + "). Chỉ được đổi sang phòng Vacant_Clean (BR-FO-04)");
                }

                // Đổi phòng cũ → Vacant_Dirty
                oldRoom.setRoomStatus(STATUS_DIRTY);
                oldRoom.setCurrentBookingDetailId(null);
                roomRepo.save(oldRoom);

                // Gán phòng mới → Occupied
                newRoom.setRoomStatus(STATUS_OCCUPIED);
                newRoom.setCurrentBookingDetailId(detail.getId());
                roomRepo.save(newRoom);

                // Cập nhật detail
                detail.setRoom(newRoom);
                roomBookingDetailRepo.save(detail);

                return detail;
        }

}