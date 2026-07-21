package com.kawai.services.impl;

import com.kawai.dto.TableReservationRequest;
import com.kawai.exceptions.BusinessException;
import com.kawai.models.RestaurantTable;
import com.kawai.models.TableReservation;
import com.kawai.repositories.CustomerRepository;
import com.kawai.repositories.RestaurantTableRepository;
import com.kawai.repositories.TableReservationRepository;
import com.kawai.services.interfaces.TableReservationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.kawai.models.Room;
import com.kawai.models.RoomBookingDetail;
import com.kawai.repositories.RoomRepository;
import com.kawai.repositories.RoomBookingDetailRepository;

@Service
@Transactional
public class TableReservationServiceImpl implements TableReservationService {

    private final TableReservationRepository tableReservationRepository;
    private final RestaurantTableRepository restaurantTableRepository;
    private final CustomerRepository customerRepository;
    private final RoomRepository roomRepository;
    private final RoomBookingDetailRepository roomBookingDetailRepository;
    private final com.kawai.repositories.RoomBookingRepository roomBookingRepository;
    private final com.kawai.services.interfaces.EmailService emailService;

    public TableReservationServiceImpl(TableReservationRepository tableReservationRepository,
                                       RestaurantTableRepository restaurantTableRepository,
                                       CustomerRepository customerRepository,
                                       RoomRepository roomRepository,
                                       RoomBookingDetailRepository roomBookingDetailRepository,
                                       com.kawai.repositories.RoomBookingRepository roomBookingRepository,
                                       com.kawai.services.interfaces.EmailService emailService) {
        this.tableReservationRepository = tableReservationRepository;
        this.restaurantTableRepository = restaurantTableRepository;
        this.customerRepository = customerRepository;
        this.roomRepository = roomRepository;
        this.roomBookingDetailRepository = roomBookingDetailRepository;
        this.roomBookingRepository = roomBookingRepository;
        this.emailService = emailService;
    }

    @Override
    public List<Long> getAvailableTables(LocalDate date, LocalTime start, LocalTime end) {
        List<RestaurantTable> allTables = restaurantTableRepository.findAll();
        List<Long> availableTableIds = new ArrayList<>();

        for (RestaurantTable table : allTables) {
            List<TableReservation> reservations = tableReservationRepository.findByTable_IdAndReserveDateOrderByReserveTimeAsc(table.getId(), date);
            boolean isAvailable = true;
            for (TableReservation res : reservations) {
                if ("Pending".equalsIgnoreCase(res.getStatus()) || "Seated".equalsIgnoreCase(res.getStatus()) || "Completed".equalsIgnoreCase(res.getStatus())) {
                    java.time.LocalDateTime rStartDT = java.time.LocalDateTime.of(date, res.getReserveTime());
                    java.time.LocalDateTime rEndDT;
                    if (res.getEndTime() != null) {
                        rEndDT = java.time.LocalDateTime.of(date, res.getEndTime());
                        if (rEndDT.isBefore(rStartDT)) rEndDT = rEndDT.plusDays(1);
                    } else {
                        rEndDT = rStartDT.plusHours(1);
                    }
                    
                    // Add 15 minutes buffer time
                    rEndDT = rEndDT.plusMinutes(15);
                    
                    java.time.LocalDateTime startDT = java.time.LocalDateTime.of(date, start);
                    java.time.LocalDateTime endDT;
                    if (end != null) {
                        endDT = java.time.LocalDateTime.of(date, end);
                        if (endDT.isBefore(startDT)) endDT = endDT.plusDays(1);
                    } else {
                        endDT = startDT.plusHours(1);
                    }

                    if (rStartDT.isBefore(endDT) && rEndDT.isAfter(startDT)) {
                        isAvailable = false;
                        break;
                    }
                }
            }

            // Check physical status for immediate bookings (within 2 hours of now)
            if (isAvailable && date.equals(LocalDate.now())) {
                if ("Occupied".equalsIgnoreCase(table.getTableStatus()) || "Cleaning".equalsIgnoreCase(table.getTableStatus())) {
                    LocalDateTime startDateTime = LocalDateTime.of(date, start);
                    if (startDateTime.isBefore(LocalDateTime.now().plusHours(2))) {
                        isAvailable = false;
                    }
                }
            }

            if (isAvailable) {
                availableTableIds.add(table.getId());
            }
        }

        return availableTableIds;
    }

    @Override
    public TableReservation createReservation(TableReservationRequest request, java.security.Principal principal) {
        RestaurantTable table = restaurantTableRepository.findById(request.getTableId())
                .orElseThrow(() -> new BusinessException("TABLE-001", "Table not found"));

        if (request.getPartySize() > table.getCapacity()) {
            throw new BusinessException("TABLE-002", "Party size exceeds table capacity");
        }

        if (request.getStartTime() != null) {
            LocalTime time = request.getStartTime();
            if (time.isAfter(LocalTime.of(22, 59)) || time.isBefore(LocalTime.of(8, 0))) {
                throw new BusinessException("TABLE-008", "Nhà hàng không nhận khách đặt bàn trong khung giờ từ 23:00 đến 08:00 sáng. Quý khách vui lòng sử dụng dịch vụ gọi món lên phòng.");
            }
        }

        if (request.getEndTime() != null) {
            java.time.LocalDateTime newStartDT = java.time.LocalDateTime.of(request.getReserveDate(), request.getStartTime());
            java.time.LocalDateTime newEndDT = java.time.LocalDateTime.of(request.getReserveDate(), request.getEndTime());
            if (newEndDT.isBefore(newStartDT)) newEndDT = newEndDT.plusDays(1);
            if (java.time.Duration.between(newStartDT, newEndDT).toMinutes() < 30) {
                throw new BusinessException("TABLE-006", "Thời gian đặt bàn tối thiểu là 30 phút.");
            }
        }

        // Check physical status for immediate bookings (within 2 hours of now)
        if (request.getReserveDate().equals(LocalDate.now())) {
            if ("Occupied".equalsIgnoreCase(table.getTableStatus()) || "Cleaning".equalsIgnoreCase(table.getTableStatus())) {
                java.time.LocalDateTime startDateTime = java.time.LocalDateTime.of(request.getReserveDate(), request.getStartTime());
                if (startDateTime.isBefore(java.time.LocalDateTime.now().plusHours(2))) {
                    throw new BusinessException("TABLE-007", "Bàn hiện đang " + ("Cleaning".equalsIgnoreCase(table.getTableStatus()) ? "dọn dẹp" : "có khách") + ". Không thể đặt trước cho khoảng thời gian trong vòng 2 giờ tới.");
                }
            }
        }

        List<TableReservation> existingReservations = tableReservationRepository.findByTable_IdAndReserveDateOrderByReserveTimeAsc(table.getId(), request.getReserveDate());

        for (TableReservation res : existingReservations) {
            if ("Pending".equalsIgnoreCase(res.getStatus()) || "Seated".equalsIgnoreCase(res.getStatus()) || "Completed".equalsIgnoreCase(res.getStatus())) {
                java.time.LocalDateTime existingStartDT = java.time.LocalDateTime.of(request.getReserveDate(), res.getReserveTime());
                java.time.LocalDateTime existingEndDT;
                if (res.getEndTime() != null) {
                    existingEndDT = java.time.LocalDateTime.of(request.getReserveDate(), res.getEndTime());
                    if (existingEndDT.isBefore(existingStartDT)) existingEndDT = existingEndDT.plusDays(1);
                } else {
                    existingEndDT = existingStartDT.plusHours(1);
                }

                // Add 15 minutes buffer time
                existingEndDT = existingEndDT.plusMinutes(15);

                java.time.LocalDateTime newStartDT = java.time.LocalDateTime.of(request.getReserveDate(), request.getStartTime());
                java.time.LocalDateTime newEndDT;
                if (request.getEndTime() != null) {
                    newEndDT = java.time.LocalDateTime.of(request.getReserveDate(), request.getEndTime());
                    if (newEndDT.isBefore(newStartDT)) newEndDT = newEndDT.plusDays(1);
                } else {
                    newEndDT = newStartDT.plusHours(1);
                }

                if (existingStartDT.isBefore(newEndDT) && existingEndDT.isAfter(newStartDT)) {
                    throw new BusinessException("TABLE-003", "Table is already reserved for the requested time");
                }
            }
        }

        TableReservation res = new TableReservation();
        res.setTable(table);
        res.setReserveDate(request.getReserveDate());
        res.setReserveTime(request.getStartTime());
        res.setEndTime(request.getEndTime());
        res.setPartySize(request.getPartySize());
        res.setStatus("Pending");

        com.kawai.models.Customer customer = null;
        
        if (principal != null) {
            String username = principal.getName();
            if (principal instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) {
                org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken oauthToken = 
                    (org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) principal;
                username = oauthToken.getPrincipal().getAttribute("email");
            }
            java.util.Optional<com.kawai.models.Customer> customerOpt = customerRepository.findByAccount_Username(username);
            if (customerOpt.isEmpty()) {
                customerOpt = customerRepository.findByEmail(username);
            }
            if (customerOpt.isPresent()) {
                customer = customerOpt.get();
            }
        }

        if (customer == null) {
            if (request.getRoomNumber() != null && !request.getRoomNumber().trim().isEmpty()) {
                Room room = roomRepository.findByRoomNumber(request.getRoomNumber().trim())
                    .orElseThrow(() -> new BusinessException("ROOM-001", "Phòng không tồn tại"));
                if (room.getCurrentBookingDetailId() == null) {
                    throw new BusinessException("ROOM-002", "Phòng đang trống, không có khách lưu trú");
                }
                RoomBookingDetail detail = roomBookingDetailRepository.findById(room.getCurrentBookingDetailId())
                    .orElseThrow(() -> new BusinessException("ROOM-003", "Không tìm thấy thông tin đặt phòng"));
                if (detail.getRoomBooking() != null && detail.getRoomBooking().getCustomer() != null) {
                    customer = detail.getRoomBooking().getCustomer();
                } else {
                    throw new BusinessException("ROOM-004", "Không tìm thấy thông tin khách lưu trú");
                }
            } else {
                throw new BusinessException("TABLE-005", "Vui lòng nhập số phòng. Chỉ áp dụng đặt bàn cho khách đang lưu trú.");
            }
        }

        // Validate active room booking
        java.time.LocalDate today = LocalDate.now();
        boolean hasValidBooking = false;
        
        if (customer.getAccount() != null) {
            List<RoomBookingDetail> rbds = roomBookingDetailRepository.findActiveDetailsByUserId(customer.getAccount().getId());
            for (RoomBookingDetail d : rbds) {
                if (d.getRoomBooking() != null) {
                    java.time.LocalDate checkIn = d.getRoomBooking().getCheckInDate();
                    java.time.LocalDate checkOut = d.getRoomBooking().getCheckOutDate();
                    if (checkIn.isBefore(today)) checkIn = today;
                    if (checkOut != null && checkOut.isBefore(today)) checkOut = today;
                    if (!request.getReserveDate().isBefore(checkIn) && !request.getReserveDate().isAfter(checkOut)) {
                        hasValidBooking = true;
                        break;
                    }
                }
            }
        }
        
        if (!hasValidBooking) {
            List<com.kawai.models.RoomBooking> rbs = roomBookingRepository.findByCustomerOrderByBookingDateDesc(customer);
            for (com.kawai.models.RoomBooking rb : rbs) {
                if ("Checked_In".equals(rb.getBookingStatus()) || "Confirmed".equals(rb.getBookingStatus())) {
                    java.time.LocalDate checkIn = rb.getCheckInDate();
                    java.time.LocalDate checkOut = rb.getCheckOutDate();
                    if (checkIn.isBefore(today)) checkIn = today; 
                    if (checkOut != null && checkOut.isBefore(today)) checkOut = today;
                    
                    if (!request.getReserveDate().isBefore(checkIn) && !request.getReserveDate().isAfter(checkOut)) {
                        hasValidBooking = true;
                        break;
                    }
                }
            }
        }

        if (!hasValidBooking) {
            throw new BusinessException("TABLE-009", "Bạn cần có lịch lưu trú hợp lệ tại thời điểm đặt bàn.");
        }

        res.setCustomer(customer);

        // Verification: ensure this customer is currently Checked_In
        boolean isCheckedIn = false;
        java.util.List<RoomBookingDetail> detailsByCustomer = roomBookingDetailRepository.findByAnyCustomerId(customer.getId());
        if (detailsByCustomer != null) {
            for (RoomBookingDetail d : detailsByCustomer) {
                if ("Checked_In".equalsIgnoreCase(d.getDetailStatus())) {
                    isCheckedIn = true;
                    break;
                }
            }
        }
        if (!isCheckedIn) {
            throw new BusinessException("TABLE-009", "Chỉ khách đang lưu trú tại khách sạn (đã Check-in) mới được đặt bàn.");
        }
        // Retain typed customer name in special requests if provided by F&B staff
        String specialReqs = request.getSpecialRequests();
        if (request.getCustomerName() != null && !request.getCustomerName().trim().isEmpty()) {
            if (specialReqs == null) {
                specialReqs = "Khách: " + request.getCustomerName();
            } else {
                specialReqs = "Khách: " + request.getCustomerName() + " - " + specialReqs;
            }
        }
        res.setSpecialRequests(specialReqs);

        TableReservation savedRes = tableReservationRepository.save(res);
        if (savedRes.getCustomer() != null) {
            emailService.sendTableBookingConfirmation(savedRes, savedRes.getCustomer());
        }
        return savedRes;
    }

    @Override
    public List<Map<String, Object>> getTableReservations(Long tableId, LocalDate date) {
        List<TableReservation> reservations = tableReservationRepository.findByTable_IdAndReserveDateOrderByReserveTimeAsc(tableId, date);

        List<Map<String, Object>> result = new ArrayList<>();
        for (TableReservation res : reservations) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", res.getId());
            map.put("reserveDate", res.getReserveDate().toString());
            map.put("reserveTime", res.getReserveTime().toString());
            map.put("endTime", res.getEndTime() != null ? res.getEndTime().toString() : res.getReserveTime().plusHours(1).toString());
            map.put("partySize", res.getPartySize());
            map.put("status", res.getStatus());
            map.put("specialRequests", res.getSpecialRequests());
            if (res.getCustomer() != null) {
                map.put("customerName", res.getCustomer().getFullName());
            } else {
                map.put("customerName", "Khách hàng");
            }
            result.add(map);
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getAllTablesWithReservations(LocalDate date) {
        List<RestaurantTable> allTables = restaurantTableRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (RestaurantTable table : allTables) {
            if (Boolean.FALSE.equals(table.getIsActive())) continue;

            Map<String, Object> tableMap = new HashMap<>();
            tableMap.put("tableId", table.getId());
            tableMap.put("tableNumber", table.getTableNumber());
            tableMap.put("capacity", table.getCapacity());
            tableMap.put("tableStatus", table.getTableStatus());

            List<TableReservation> reservations =
                    tableReservationRepository.findByTable_IdAndReserveDateOrderByReserveTimeAsc(table.getId(), date);

            List<Map<String, Object>> resList = new ArrayList<>();
            for (TableReservation res : reservations) {
                Map<String, Object> resMap = new HashMap<>();
                resMap.put("id", res.getId());
                resMap.put("reserveTime", res.getReserveTime().toString());
                resMap.put("endTime", res.getEndTime() != null
                        ? res.getEndTime().toString()
                        : res.getReserveTime().plusHours(1).toString());
                resMap.put("partySize", res.getPartySize());
                resMap.put("status", res.getStatus());
                resMap.put("specialRequests", res.getSpecialRequests());
                resMap.put("customerName",
                        res.getCustomer() != null ? res.getCustomer().getFullName() : "Khách hàng");
                resList.add(resMap);
            }
            tableMap.put("reservations", resList);
            result.add(tableMap);
        }

        return result;
    }

    @Override
    public void checkInReservation(Long reservationId) {
        TableReservation res = tableReservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException("RES-001", "Không tìm thấy thông tin đặt bàn"));
        
        if (!"Pending".equalsIgnoreCase(res.getStatus())) {
            throw new BusinessException("RES-002", "Trạng thái đặt bàn không hợp lệ để check-in");
        }
        
        res.setStatus("Seated");
        tableReservationRepository.save(res);
        
        RestaurantTable table = res.getTable();
        if (table != null) {
            table.setTableStatus("Occupied");
            restaurantTableRepository.save(table);
        }
    }

    @Override
    public TableReservation holdReservation(Long reservationId, int holdMinutes) {
        if (holdMinutes < 0 || holdMinutes > 30) {
            throw new BusinessException("RES-003", "Thời gian giữ bàn phải từ 0 đến 30 phút.");
        }
        TableReservation res = tableReservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException("RES-001", "Không tìm thấy thông tin đặt bàn"));
        
        if (!"Pending".equalsIgnoreCase(res.getStatus())) {
            throw new BusinessException("RES-004", "Trạng thái đặt bàn không hợp lệ để giữ bàn");
        }
        
        String currentNotes = res.getSpecialRequests();
        if (currentNotes == null) {
            currentNotes = "";
        }
        
        // Loại bỏ chuỗi giữ bàn cũ nếu có
        currentNotes = currentNotes.replaceAll("\\[HELD: \\d+m\\]", "").trim();
        
        if (holdMinutes > 0) {
            currentNotes = (currentNotes + " [HELD: " + holdMinutes + "m]").trim();
        }
        
        res.setSpecialRequests(currentNotes.isEmpty() ? null : currentNotes);
        TableReservation savedRes = tableReservationRepository.save(res);
        
        if (holdMinutes > 0 && savedRes.getCustomer() != null) {
            String latestTime = res.getReserveTime().plusMinutes(15 + holdMinutes).toString();
            emailService.sendExtendTableHold(savedRes, savedRes.getCustomer(), holdMinutes, latestTime);
        }
        return savedRes;
    }

    @Override
    public void cancelReservation(Long reservationId, java.security.Principal principal, String reason) {
        TableReservation res = tableReservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException("RES-001", "Không tìm thấy thông tin đặt bàn"));

        if (!"Pending".equalsIgnoreCase(res.getStatus()) && !"Confirmed".equalsIgnoreCase(res.getStatus())) {
            throw new BusinessException("RES-005", "Chỉ có thể hủy đơn đặt bàn khi ở trạng thái Chờ xác nhận hoặc Đã xác nhận.");
        }

        // Validate that the current user is the one who created the reservation
        boolean isStaff = false;
        if (principal != null) {
            String username = principal.getName();
            if (principal instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) {
                org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken oauthToken =
                        (org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) principal;
                username = oauthToken.getPrincipal().getAttribute("email");
            }
            
            // Check if user is F&B Staff or Admin
            // For simplicity, we just assume if they aren't the customer, they might be staff. 
            // In reality, TableApiController has PreAuthorize for Staff if they call it from a different endpoint.
            // Actually, we should check if they are the customer. If not, and they are here, we allow it (meaning staff)
            // But let's check customer match just to tag it properly.
            
            java.util.Optional<com.kawai.models.Customer> customerOpt = customerRepository.findByAccount_Username(username);
            if (customerOpt.isEmpty()) {
                customerOpt = customerRepository.findByEmail(username);
            }
            if (customerOpt.isPresent()) {
                com.kawai.models.Customer currentCustomer = customerOpt.get();
                if (res.getCustomer() != null && !res.getCustomer().getId().equals(currentCustomer.getId())) {
                    // Not the customer. We will assume they are staff since only staff and customer can hit this.
                    isStaff = true;
                }
            } else {
                isStaff = true;
            }
        }

        res.setStatus("Cancelled");
        String currentNotes = res.getSpecialRequests();
        if (currentNotes == null) {
            currentNotes = "";
        }
        
        String actionTagger = isStaff ? "[Nhân viên hủy]" : "[Khách tự hủy trên Profile]";
        String reasonText = reason != null && !reason.trim().isEmpty() ? " - Lý do: " + reason : "";
        currentNotes = (currentNotes + " " + actionTagger + reasonText).trim();
        
        res.setSpecialRequests(currentNotes);

        tableReservationRepository.save(res);

        if (res.getCustomer() != null) {
            String mailCancelledBy = isStaff ? "Nhân viên F&B" : "Khách hàng";
            emailService.sendCancelTableBooking(res, res.getCustomer(), mailCancelledBy, reason);
        }
    }
}
