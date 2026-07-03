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
@Service
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
                        @org.springframework.context.annotation.Lazy com.kawai.services.interfaces.DependentService dependentService) {
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
                this.dependentService = dependentService;
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

                return detail;
        }

        private void assignRoomToGuest(RoomBookingDetail detail, Room room, java.math.BigDecimal allocatedCreditLimit) {
                detail.setRoom(room);
                detail.setSubCreditLimit(
                                allocatedCreditLimit != null ? allocatedCreditLimit : java.math.BigDecimal.ZERO);
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
                if (STATUS_DIRTY.equalsIgnoreCase(status)) {
                        throw new IllegalStateException(
                                        "ROOM-001: Phòng đang DIRTY, chưa được dọn dẹp. Không thể check-in. (BR-FO-04)");
                }
                if (STATUS_MAINTENANCE.equalsIgnoreCase(status)) {
                        throw new IllegalStateException(
                                        "ROOM-001: Phòng đang MAINTENANCE, đang bảo trì. Không thể check-in. (BR-HK-03)");
                }
                if (STATUS_OCCUPIED.equalsIgnoreCase(status)) {
                        throw new IllegalStateException(
                                        "ROOM-001: Phòng đang Occupied, không thể check-in. (MOD2-002)");
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
        public java.util.Map<String, Object> upgradeDependentToCustomer(Long dependentId) {
                Dependent dependent = findDependent(dependentId);

                // Guard: Kiểm tra Dependent đã được nâng cấp thành Customer chưa
                // (dựa trên CCCD/Passport đã mã hóa để tránh tạo duplicate)
                if (dependent.getCccdPassportEncrypted() != null
                                && !dependent.getCccdPassportEncrypted().isBlank()) {
                        java.util.Optional<com.kawai.models.Customer> existingCustomer = customerRepo
                                        .findByCccdPassportEncrypted(
                                                        dependent.getCccdPassportEncrypted());
                        if (existingCustomer.isPresent()) {
                                throw new com.kawai.exceptions.BusinessException(
                                                "UPGRADE-001",
                                                "Người phụ thuộc này đã được nâng cấp thành Khách hàng (CCCD/Passport đã tồn tại)");
                        }
                }

                Role customerRole = findCustomerRole();

                String randomSuffix = UUID.randomUUID().toString().substring(0, 2);
                String username = "guest" + randomSuffix;
                String dummyEmail = username + "@kawai-resort.com";
                String randomPwd = UUID.randomUUID().toString().substring(0, 6);

                Account savedAccount = createAccountForDependent(customerRole, username, randomPwd);
                Customer savedCustomer = createCustomerFromDependent(dependent, savedAccount, dummyEmail);

                // Cập nhật tất cả các RoomGuest liên quan đến Dependent này sang Customer mới
                // Do repository chỉ có findByDependentId trả Optional, ta dùng Optional. Tốt nhất nên là List nếu 1 Dependent tham gia nhiều booking
                java.util.Optional<com.kawai.models.RoomGuest> rgOpt = roomGuestRepo.findByDependentId(dependentId);
                if (rgOpt.isPresent()) {
                    com.kawai.models.RoomGuest rg = rgOpt.get();
                    rg.setCustomer(savedCustomer);
                    rg.setDependent(null);
                    rg.setIsPrimaryContact(true);
                    roomGuestRepo.save(rg);
                    
                    if (rg.getRoomBookingDetail() != null) {
                        com.kawai.models.RoomBookingDetail detail = rg.getRoomBookingDetail();
                        detail.setCustomer(savedCustomer);
                        roomBookingDetailRepo.save(detail);
                    }
                }

                // Xóa Dependent sau khi nâng cấp thành công để tránh dữ liệu bị duplicate
                dependentRepo.delete(dependent);

                // Trả về map chứa customer và mật khẩu
                java.util.Map<String, Object> result = new java.util.HashMap<>();
                result.put("customer", savedCustomer);
                result.put("username", username);
                result.put("password", randomPwd);

                return result;
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
        public void processBulkCheckin(com.kawai.dto.CheckinSubmitFormDTO form, Customer customer, com.kawai.models.Booking booking) {
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
            if (form.getAllocatedCreditLimits() != null) {
                for (java.math.BigDecimal limit : form.getAllocatedCreditLimits()) {
                    if (limit != null) {
                        if (limit.compareTo(java.math.BigDecimal.ZERO) < 0) {
                            throw new com.kawai.exceptions.BusinessException("CHECKIN-008", "Hạn mức không được là số âm!");
                        }
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
                        "Tổng hạn mức cấp cho các phòng vượt quá hạn mức của tài khoản (Master: " + masterCreditLimit
                                + ")");
            }

            if (form.getAssignedRoomNumbers().size() != pendingDetails.size()) {
                throw new com.kawai.exceptions.BusinessException("CHECKIN-005",
                        "Bạn phải phân đủ " + pendingDetails.size() + " phòng trước khi hoàn tất Check-in!");
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
                            "Phòng " + roomNumber + " thuộc hạng " + room.getCategory().getCategoryName()
                                    + " không khớp với bất kỳ hạng phòng nào đang chờ check-in của đơn này!");
                }
                pendingDetails.remove(matchedDetail);

                java.math.BigDecimal allocatedLimit = null;
                int roomIdx = form.getAssignedRoomNumbers().indexOf(roomNumber);
                if (form.getAllocatedCreditLimits() != null && roomIdx < form.getAllocatedCreditLimits().size()) {
                    allocatedLimit = form.getAllocatedCreditLimits().get(roomIdx);
                }
                this.checkIn(matchedDetail.getId(), room.getId(), allocatedLimit);
                roomNumberToDetailIdMap.put(roomNumber, matchedDetail.getId());
            }

            if (form.getAssignedRoomNumbers() != null && !form.getAssignedRoomNumbers().isEmpty()) {
                String firstRoom = form.getAssignedRoomNumbers().get(0);
                Long firstDetailId = roomNumberToDetailIdMap.get(firstRoom);

                if (firstDetailId != null && customer != null) {
                    com.kawai.models.RoomBookingDetail firstDetail = roomBookingDetailRepo.findById(firstDetailId)
                            .orElse(null);
                    if (firstDetail != null) {
                        com.kawai.models.RoomGuest existingMasterGuest = null;
                        for (com.kawai.models.RoomBookingDetail detail : details) {
                            java.util.List<com.kawai.models.RoomGuest> detailGuests = roomGuestRepo
                                    .findByRoomBookingDetailId(detail.getId());
                            for (com.kawai.models.RoomGuest rg : detailGuests) {
                                if (rg.getCustomer() != null && rg.getCustomer().getId().equals(customer.getId())) {
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
                            Long detailId = roomNumberToDetailIdMap.get(dependentDTO.getAssignedPhysicalRoomNumber());
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
                                    "Phòng " + roomNumber + " phải có đúng 1 người đứng đầu!");
                        }
                    }
                }
            }

            String tourAllocationMode = form.getTourAllocationMode();
            if ("PER_TOUR".equalsIgnoreCase(tourAllocationMode)
                    && form.getTourAllocations() != null
                    && !form.getTourAllocations().isEmpty()) {
                for (com.kawai.dto.TourRoomAllocationDTO allocation : form.getTourAllocations()) {
                    if (allocation.getTourBookingId() == null || allocation.getRoomNumber() == null
                            || allocation.getRoomNumber().isEmpty()) {
                        continue;
                    }
                    com.kawai.models.TourBooking tourBooking = tourBookingRepo.findById(allocation.getTourBookingId())
                            .orElse(null);
                    if (tourBooking == null)
                        continue;

                    Long detailId = roomNumberToDetailIdMap.get(allocation.getRoomNumber());
                    if (detailId == null) {
                        continue;
                    }
                    com.kawai.models.RoomBookingDetail detail = roomBookingDetailRepo.findById(detailId).orElse(null);
                    if (detail != null) {
                        tourBooking.setRoomBookingDetail(detail);
                        tourBookingRepo.save(tourBooking);
                    }
                }
            }
        }
}
