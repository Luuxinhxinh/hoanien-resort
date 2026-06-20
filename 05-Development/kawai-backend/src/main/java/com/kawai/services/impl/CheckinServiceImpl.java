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
        private static final String STATUS_DIRTY = "Dirty";
        private static final String STATUS_MAINTENANCE = "Maintenance";
        private static final String STATUS_VACANT_CLEAN = "Vacant_Clean";
        private static final String STATUS_AVAILABLE = "Available";
        private static final String STATUS_OCCUPIED = "Occupied";
        private static final String STATUS_CHECKED_IN = "CHECKED_IN";
        private static final String ROLE_CUSTOMER = "CUSTOMER";
        private static final String TEMP_PHONE = "PENDING";
        private static final String DEFAULT_MEMBERSHIP = "Regular";
        private static final String TEMP_PASSWORD_HASH = "TEMPORARY_HASH";

        // ===== Dependencies =====
        private final RoomBookingDetailRepository roomBookingDetailRepo;
        private final RoomRepository roomRepo;
        private final RoomBookingRepository roomBookingRepo;
        private final CustomerRepository customerRepo;
        private final AccountRepository accountRepo;
        private final RoleRepository roleRepo;
        private final DependentRepository dependentRepo;

        @Autowired
        public CheckinServiceImpl(
                        RoomBookingDetailRepository roomBookingDetailRepo,
                        RoomRepository roomRepo,
                        RoomBookingRepository roomBookingRepo,
                        CustomerRepository customerRepo,
                        AccountRepository accountRepo,
                        RoleRepository roleRepo,
                        DependentRepository dependentRepo) {
                this.roomBookingDetailRepo = roomBookingDetailRepo;
                this.roomRepo = roomRepo;
                this.roomBookingRepo = roomBookingRepo;
                this.customerRepo = customerRepo;
                this.accountRepo = accountRepo;
                this.roleRepo = roleRepo;
                this.dependentRepo = dependentRepo;
        }

        // ========================================================================
        // UC12.1: Check-in
        // ========================================================================

        @Override
        @Transactional
        public RoomBookingDetail checkIn(Long bookingDetailId, Long roomId) {
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

                assignRoomToGuest(detail, room);

                return detail;
        }

        private void assignRoomToGuest(RoomBookingDetail detail, Room room) {
                detail.setRoom(room);
                detail.setDetailStatus(STATUS_CHECKED_IN);

                room.setRoomStatus(STATUS_OCCUPIED);
                room.setCurrentBookingDetailId(detail.getId());

                roomBookingDetailRepo.save(detail);
                roomRepo.save(room);

                // Cập nhật trạng thái của toàn bộ Booking sang Checked_In để xóa khỏi danh sách Arrivals
                RoomBooking parent = detail.getRoomBooking();
                if (parent != null && "CONFIRMED".equalsIgnoreCase(parent.getBookingStatus())) {
                        parent.setBookingStatus("Checked_In");
                        roomBookingRepo.save(parent);
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
                if (newCreditLimit.compareTo(BigDecimal.ZERO) < 0) {
                        throw new IllegalArgumentException("Credit Limit âm không hợp lệ (MOD2-001)");
                }
                RoomBookingDetail detail = findBookingDetail(bookingDetailId);
                detail.getRoomBooking().setCreditLimit(newCreditLimit);
                roomBookingRepo.save(detail.getRoomBooking());
        }

        // ========================================================================
        // UC12.3: Đổi phòng
        // ========================================================================

        @Override
        @Transactional
        public RoomBookingDetail transferRoom(Long bookingDetailId, Long newRoomId) {
                RoomBookingDetail detail = findBookingDetail(bookingDetailId);
                validateDetailIsCheckedIn(detail);

                Room oldRoom = extractCurrentRoom(detail);
                Room newRoom = findRoom(newRoomId);
                validateNewRoomAvailable(newRoom);

                performRoomTransfer(detail, oldRoom, newRoom);

                return detail;
        }

        private void validateDetailIsCheckedIn(RoomBookingDetail detail) {
                if (!STATUS_CHECKED_IN.equals(detail.getDetailStatus())) {
                        throw new IllegalStateException("Chỉ có thể đổi phòng cho khách đang CHECKED_IN");
                }
        }

        private Room extractCurrentRoom(RoomBookingDetail detail) {
                Room room = detail.getRoom();
                if (room == null) {
                        throw new IllegalStateException("Booking detail chưa được gán phòng");
                }
                return room;
        }

        private void validateNewRoomAvailable(Room room) {
                String status = room.getRoomStatus();
                if (!STATUS_VACANT_CLEAN.equals(status) && !STATUS_AVAILABLE.equals(status)) {
                        throw new IllegalStateException(
                                        "ROOM-001: Phòng mới không khả dụng (trạng thái: " + status + ")");
                }
        }

        private void performRoomTransfer(RoomBookingDetail detail, Room oldRoom, Room newRoom) {
                // Gán phòng mới
                detail.setRoom(newRoom);

                // Cập nhật phòng cũ → Dirty (BR-FO-04)
                oldRoom.setRoomStatus(STATUS_DIRTY);
                oldRoom.setCurrentBookingDetailId(null);

                // Cập nhật phòng mới → Occupied (BR-FO-04)
                newRoom.setRoomStatus(STATUS_OCCUPIED);
                newRoom.setCurrentBookingDetailId(detail.getId());

                // Lưu toàn bộ
                roomBookingDetailRepo.save(detail);
                roomRepo.save(oldRoom);
                roomRepo.save(newRoom);
        }

        // ========================================================================
        // UC12.4: Nâng cấp Dependent → Customer
        // ========================================================================

        @Override
        @Transactional
        public Customer upgradeDependentToCustomer(Long dependentId) {
                Dependent dependent = findDependent(dependentId);
                Role customerRole = findCustomerRole();

                Account savedAccount = createAccountForDependent(customerRole);
                Customer savedCustomer = createCustomerFromDependent(dependent, savedAccount);

                return savedCustomer;
        }

        private Account createAccountForDependent(Role customerRole) {
                Account account = new Account();
                account.setUsername("customer_" + UUID.randomUUID().toString().substring(0, 8));
                account.setPasswordHash(TEMP_PASSWORD_HASH);
                account.setIsActive(true);
                account.setRole(customerRole);
                return accountRepo.save(account);
        }

        private Customer createCustomerFromDependent(Dependent dependent, Account account) {
                Customer customer = new Customer();
                customer.setAccount(account);
                customer.setFullName(dependent.getDependentName());
                customer.setGender(dependent.getGender());
                customer.setCccdPassportEncrypted(dependent.getCccdPassportEncrypted());
                customer.setPhone(TEMP_PHONE);
                customer.setEmail("pending_" + account.getId() + "@kawai-resort.com");
                customer.setLoyaltyPoints(0);
                customer.setMembershipTier(DEFAULT_MEMBERSHIP);
                return customerRepo.save(customer);
        }

        // ========================================================================
        // Private helpers: repository lookups
        // ========================================================================

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
}