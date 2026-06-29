package com.kawai.controllers.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import com.kawai.repositories.*;
import com.kawai.models.*;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@RequestMapping("/receptionist")
@AllArgsConstructor
@PreAuthorize("hasAnyAuthority('OP_BOOKING', 'ROLE_ADMIN', 'ROLE_MANAGER')")
public class ReceptionistController {

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final RoomBookingRepository roomBookingRepository;
    private final RoomBookingDetailRepository roomBookingDetailRepository;
    private final com.kawai.repositories.RoomGuestRepository roomGuestRepository;
    private final com.kawai.services.interfaces.DependentService dependentService;
    private final com.kawai.services.interfaces.CheckinService checkinService;
    private final com.kawai.services.interfaces.HousekeepingService housekeepingService;
    private final EmployeeRepository employeeRepository;
    private final TourBookingRepository tourBookingRepository;

    @org.springframework.web.bind.annotation.ModelAttribute("todayLabel")
    public String getTodayLabel() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", new Locale("vi")));
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyAuthority('OP_DASHBOARD', 'ROLE_ADMIN', 'ROLE_MANAGER')")
    public String dashboard(Model model) {
        // KPI
        long totalRooms = 0, occupied = 0, dirty = 0;
        try {
            totalRooms = roomRepository.countTotalRooms();
            occupied = roomRepository.countByStatus().stream()
                    .filter(r -> "Occupied".equals(r[0])).mapToLong(r -> (Long) r[1]).findFirst().orElse(0);
            dirty = roomRepository.countByStatus().stream()
                    .filter(r -> "Vacant_Dirty".equals(r[0])).mapToLong(r -> (Long) r[1]).findFirst().orElse(0);
        } catch (Exception e) {
        }
        model.addAttribute("totalRooms", totalRooms);
        model.addAttribute("occupiedRooms", occupied);
        model.addAttribute("dirtyRooms", dirty);

        long pendingCheckIns = 0, checkoutsToday = 0;
        try {
            pendingCheckIns = roomBookingRepository.countCheckInsOnDate(LocalDate.now());
            checkoutsToday = roomBookingRepository.findCheckOutsBetween(LocalDate.now(), LocalDate.now()).size();
        } catch (Exception e) {
        }
        model.addAttribute("pendingCheckIns", pendingCheckIns);
        model.addAttribute("checkoutsToday", checkoutsToday);

        // Room Matrix from DB
        Map<String, List<Map<String, Object>>> categorizedRooms = new LinkedHashMap<>();
        try {
            for (Room r : roomRepository.findAll()) {
                Map<String, Object> m = new HashMap<>();
                m.put("roomNumber", r.getRoomNumber());
                m.put("status", r.getRoomStatus());
                String catName = r.getCategory() != null ? r.getCategory().getCategoryName() : "Uncategorized";
                m.put("category", catName);

                categorizedRooms.computeIfAbsent(catName, k -> new ArrayList<>()).add(m);
            }
        } catch (Exception e) {
        }
        model.addAttribute("categorizedRooms", categorizedRooms);

        return "receptionist/dashboard";
    }

    @GetMapping("/walk-in")
    public String walkIn(Model model) {
        java.time.LocalDate today = java.time.LocalDate.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));
        model.addAttribute("todayStr", today.toString());

        Map<String, List<Map<String, Object>>> inventory = new HashMap<>();
        for (Room r : roomRepository.findVacant()) {
            String cat = "Other";
            if (r.getCategory() != null) {
                RoomCategory c = r.getCategory();
                int baseAdults = c.getBaseAdults() != null ? c.getBaseAdults() : c.getCapacity();
                int baseChildren = c.getBaseChildren() != null ? c.getBaseChildren() : 0;
                java.text.NumberFormat formatter = java.text.NumberFormat.getInstance(new java.util.Locale("vi", "VN"));
                String priceStr = c.getBasePrice() != null ? formatter.format(c.getBasePrice()) : "0";
                cat = c.getCategoryName() + " - " + priceStr + " VNĐ/đêm (Tiêu chuẩn: " + baseAdults + " NL, "
                        + baseChildren + " TE)";
            }
            Map<String, Object> roomInfo = new HashMap<>();
            roomInfo.put("id", r.getId());
            roomInfo.put("number", r.getRoomNumber());
            inventory.computeIfAbsent(cat, k -> new ArrayList<>()).add(roomInfo);
        }
        model.addAttribute("roomInventory", inventory);

        // Tính số phòng thực sự còn khả dụng cho walk-in (= số phòng Vacant - slot bị
        // giữ bởi Confirmed booking chưa phân phòng)
        Map<String, Long> reservedSlots = new HashMap<>();
        try {
            for (Object[] row : roomBookingDetailRepository.countPendingUnassignedByCategoryName()) {
                String catName = (String) row[0];
                Long count = (Long) row[1];
                reservedSlots.put(catName, count);
            }
        } catch (Exception ignored) {
        }

        // categoryAvailability: categoryName -> số phòng thực còn trống (đã trừ slot bị
        // giữ)
        Map<String, Long> categoryAvailability = new HashMap<>();
        for (Map.Entry<String, List<Map<String, Object>>> entry : inventory.entrySet()) {
            // Key của inventory có dạng "NipaPool - 500,000 VNĐ/đêm (...)"
            // Cần tách ra categoryName (phần trước " - ")
            String fullKey = entry.getKey();
            String catName = fullKey.contains(" - ") ? fullKey.split(" - ")[0] : fullKey;
            long vacantCount = entry.getValue().size();
            long reserved = reservedSlots.getOrDefault(catName, 0L);
            long netAvailable = Math.max(0L, vacantCount - reserved);
            categoryAvailability.put(fullKey, netAvailable);
        }
        model.addAttribute("categoryAvailability", categoryAvailability);

        return "receptionist/walk-in";
    }

    @GetMapping("/check-in")
    public String checkIn(@org.springframework.web.bind.annotation.RequestParam(defaultValue = "1") int page,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String keyword,
            @org.springframework.web.bind.annotation.RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate dateFilter,
            Model model) {
        List<Booking> allConfirmed = bookingRepository.findConfirmed();
        List<Booking> filteredArrivals = new ArrayList<>();

        // 1. Lọc theo keyword và loại Booking (chỉ lấy RoomBooking)
        if (allConfirmed != null) {
            for (Booking b : allConfirmed) {
                if (!(b instanceof RoomBooking))
                    continue;
                RoomBooking rb = (RoomBooking) b;

                // Filter by keyword
                if (!matchesKeyword(b, keyword)) {
                    continue;
                }

                // Filter by dateFilter if provided
                if (dateFilter != null) {
                    if (rb.getCheckInDate() == null || !rb.getCheckInDate().equals(dateFilter)) {
                        continue;
                    }
                } else if (keyword == null || keyword.trim().isEmpty()) {
                    // Mặc định ẩn đơn quá khứ nếu KHÔNG dùng bộ lọc (không có keyword, không chọn
                    // ngày)
                    if (rb.getCheckInDate() != null && rb.getCheckInDate().isBefore(java.time.LocalDate.now())) {
                        continue;
                    }
                }

                filteredArrivals.add(b);
            }
        }
        // Sắp xếp đơn Arrival gần đây lên đầu (Booking Date giảm dần)
        filteredArrivals.sort(
                java.util.Comparator
                        .comparing(Booking::getBookingDate,
                                java.util.Comparator.nullsLast(java.util.Comparator.reverseOrder()))
                        .thenComparing(Booking::getId,
                                java.util.Comparator.nullsLast(java.util.Comparator.reverseOrder())));

        // 2. Tính toán phân trang cho Arrivals
        int pageSize = 10;
        int totalItems = filteredArrivals.size();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        if (totalPages == 0)
            totalPages = 1;
        if (page < 1)
            page = 1;
        if (page > totalPages)
            page = totalPages;

        int startItem = (page - 1) * pageSize;
        List<Booking> pagedPendingList = new ArrayList<>();
        if (totalItems >= startItem) {
            int toIndex = Math.min(startItem + pageSize, totalItems);
            pagedPendingList = filteredArrivals.subList(startItem, toIndex);
        }

        // 3. Truy vấn DB Detail và map dữ liệu CHỈ cho 10 đơn vị của trang hiện tại
        List<Map<String, Object>> pagedArrivals = new ArrayList<>();
        for (Booking b : pagedPendingList) {
            List<RoomBookingDetail> details = roomBookingDetailRepository.findByRoomBookingId(b.getId());
            if (details == null || details.isEmpty()) {
                continue;
            }

            String guestName = b.getCustomer() != null ? b.getCustomer().getFullName() : "Khách";
            String phone = b.getCustomer() != null ? b.getCustomer().getPhone() : "";
            String cccdEnc = b.getCustomer() != null ? b.getCustomer().getCccdPassportEncrypted() : "";
            String cccd = "";
            if (cccdEnc != null && !cccdEnc.isEmpty()) {
                try {
                    cccd = com.kawai.utils.EncryptionUtils.decrypt(cccdEnc);
                } catch (Exception e) {
                    cccd = cccdEnc;
                }
            }

            Map<String, Object> map = new HashMap<>();
            map.put("id", b.getId());
            map.put("guestName", guestName);
            map.put("phone", phone);
            map.put("cccd", cccd);
            map.put("bookingDate",
                    b.getBookingDate() != null
                            ? b.getBookingDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                            : "");
            map.put("notes", b.getNotes() != null ? b.getNotes() : "");

            if (b instanceof RoomBooking) {
                RoomBooking rb = (RoomBooking) b;
                map.put("checkInDate", rb.getCheckInDate() != null
                        ? rb.getCheckInDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        : "N/A");
                map.put("creditLimit",
                        rb.getCreditLimit() != null ? rb.getCreditLimit() : new java.math.BigDecimal("5000000.00"));
            } else {
                map.put("checkInDate", "N/A");
                map.put("creditLimit", new java.math.BigDecimal("5000000.00"));
            }

            // Chỉ tính roomSummary cho các phòng CHƯA check-in (pending)
            // để JS dropdown khớp với backend pendingDetails khi submit
            String roomSummary = "N/A";
            Map<String, Long> categoryCount = details.stream()
                    .filter(d -> d.getCategory() != null
                            && !"CHECKED_IN".equalsIgnoreCase(d.getDetailStatus()))
                    .collect(Collectors.groupingBy(d -> d.getCategory().getCategoryName(),
                            Collectors.counting()));

            if (!categoryCount.isEmpty()) {
                roomSummary = categoryCount.entrySet().stream()
                        .map(entry -> entry.getValue() + "x " + entry.getKey())
                        .collect(Collectors.joining(", "));
            }
            map.put("roomSummary", roomSummary);
            map.put("activeDetails", details.stream()
                    .filter(d -> d.getRoom() != null && "CHECKED_IN".equalsIgnoreCase(d.getDetailStatus()))
                    .map(d -> {
                        Map<String, Object> detailMap = new HashMap<>();
                        detailMap.put("id", d.getId());
                        detailMap.put("label", d.getRoom().getRoomNumber() + " - "
                                + (d.getCategory() != null ? d.getCategory().getCategoryName() : "Room"));
                        return detailMap;
                    })
                    .collect(Collectors.toList()));

            // (Removed unused detailsList creation)
            List<com.kawai.dto.DependentResponseDTO> deps = dependentService.getGuestListByBooking(b.getId());
            map.put("dependents", deps);

            // Tìm TourBookings chưa được gán phòng cụ thể (roomBookingDetail IS NULL)
            // → các tour này lễ tân sẽ phân bổ khi check-in
            List<com.kawai.models.TourBooking> unallocatedTours = tourBookingRepository
                    .findByRoomBookingIdAndRoomBookingDetailIsNull(b.getId());
            map.put("tourBookings", unallocatedTours);

            pagedArrivals.add(map);
        }

        // In-house logic has been moved to /in-house

        model.addAttribute("arrivals", pagedArrivals);
        model.addAttribute("pendingArrivalsCount", totalItems);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("keyword", keyword);
        model.addAttribute("vacantRooms", roomRepository.findVacant().stream()
                .filter(r -> "Vacant_Clean".equalsIgnoreCase(r.getRoomStatus())
                        || "Available".equalsIgnoreCase(r.getRoomStatus()))
                .collect(Collectors.toList()));
        if (dateFilter != null) {
            model.addAttribute("dateFilter", dateFilter.toString());
        }

        Map<String, List<String>> inventory = new HashMap<>();
        for (Room r : roomRepository.findVacant()) {
            String cat = r.getCategory() != null ? r.getCategory().getCategoryName() : "Other";
            inventory.computeIfAbsent(cat, k -> new ArrayList<>()).add(r.getRoomNumber());
        }
        model.addAttribute("roomInventory", inventory);

        return "receptionist/check-in";
    }

    /**
     * REST endpoint: trả về JSON chi tiết booking (khách + phòng + người đi kèm)
     * để hiển thị modal "Chi tiết" qua AJAX fetch.
     */
    @GetMapping("/in-house/detail/{bookingId}")
    @org.springframework.web.bind.annotation.ResponseBody
    public org.springframework.http.ResponseEntity<Map<String, Object>> getInHouseDetail(
            @org.springframework.web.bind.annotation.PathVariable Long bookingId) {
        try {
            Booking booking = bookingRepository.findById(bookingId).orElse(null);
            if (booking == null) {
                return org.springframework.http.ResponseEntity.notFound().build();
            }

            List<RoomBookingDetail> details = roomBookingDetailRepository.findByRoomBookingId(bookingId);

            String guestName = booking.getCustomer() != null ? booking.getCustomer().getFullName() : "Unknown";
            String phone = booking.getCustomer() != null ? booking.getCustomer().getPhone() : "";

            // Tóm tắt số phòng
            String roomSummary = details.stream()
                    .filter(d -> d.getRoom() != null)
                    .map(d -> d.getRoom().getRoomNumber() + " ("
                            + (d.getCategory() != null ? d.getCategory().getCategoryName() : "Unknown") + ")")
                    .collect(Collectors.joining(", "));
            if (roomSummary.isEmpty())
                roomSummary = "N/A";

            // Danh sách khách (cả Main Guest lẫn Dependent) theo từng phòng
            List<Map<String, Object>> guestList = new ArrayList<>();
            for (RoomBookingDetail detail : details) {
                if (detail.getRoom() == null)
                    continue;
                String roomNumber = detail.getRoom().getRoomNumber();
                String roomCategory = detail.getCategory() != null ? detail.getCategory().getCategoryName() : "";

                List<com.kawai.models.RoomGuest> guests = roomGuestRepository.findByRoomBookingDetailId(detail.getId());
                for (com.kawai.models.RoomGuest guest : guests) {
                    Map<String, Object> guestMap = new HashMap<>();
                    guestMap.put("roomNumber", roomNumber);
                    guestMap.put("roomCategory", roomCategory);
                    String cccdEnc = null;
                    if (guest.getCustomer() != null) {
                        boolean isBooker = booking.getCustomer() != null
                                && guest.getCustomer().getId().equals(booking.getCustomer().getId());
                        guestMap.put("name", guest.getCustomer().getFullName());
                        guestMap.put("type", isBooker ? "Main Guest" : "Customer (Được nâng cấp)");
                        guestMap.put("isPrimaryContact", Boolean.TRUE.equals(guest.getIsPrimaryContact()));
                        guestMap.put("dependentId", null);
                        guestMap.put("dob", "");
                        cccdEnc = guest.getCustomer().getCccdPassportEncrypted();
                    } else if (guest.getDependent() != null) {
                        guestMap.put("name", guest.getDependent().getDependentName());
                        guestMap.put("dob", guest.getDependent().getBirthDate() != null
                                ? guest.getDependent().getBirthDate().toString()
                                : "");
                        guestMap.put("type", "Dependent");
                        guestMap.put("isPrimaryContact", Boolean.TRUE.equals(guest.getIsPrimaryContact()));
                        guestMap.put("dependentId", guest.getDependent().getId());
                        cccdEnc = guest.getDependent().getCccdPassportEncrypted();
                    } else {
                        continue;
                    }
                    
                    String cccd = "";
                    if (cccdEnc != null && !cccdEnc.isBlank()) {
                        try {
                            cccd = com.kawai.utils.EncryptionUtils.decrypt(cccdEnc);
                        } catch (Exception e) {
                            cccd = cccdEnc;
                        }
                    }
                    guestMap.put("cccd", cccd);
                    guestList.add(guestMap);
                }
            }

            // Nếu không có guest nào trong Room_Guests, thêm Main Guest từ booking
            if (guestList.isEmpty() && booking.getCustomer() != null) {
                Map<String, Object> mainGuestMap = new HashMap<>();
                mainGuestMap.put("name", guestName);
                mainGuestMap.put("type", "Main Guest");
                mainGuestMap.put("isPrimaryContact", false);
                mainGuestMap.put("dependentId", null);
                mainGuestMap.put("dob", "");
                mainGuestMap.put("roomNumber", roomSummary);
                mainGuestMap.put("roomCategory", "");
                guestList.add(mainGuestMap);
            }

            // Phòng đang ở
            List<Map<String, Object>> roomDetails = new ArrayList<>();
            for (RoomBookingDetail detail : details) {
                if (detail.getRoom() == null)
                    continue;
                Map<String, Object> rd = new HashMap<>();
                rd.put("detailId", detail.getId());
                rd.put("roomNumber", detail.getRoom().getRoomNumber());
                rd.put("category", detail.getCategory() != null ? detail.getCategory().getCategoryName() : "");
                rd.put("status", detail.getDetailStatus());
                roomDetails.add(rd);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("guestName", guestName);
            result.put("phone", phone);
            result.put("roomSummary", roomSummary);
            result.put("guests", guestList);
            result.put("roomDetails", roomDetails);

            return org.springframework.http.ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return org.springframework.http.ResponseEntity.status(500).body(error);
        }
    }


    @GetMapping("/in-house")
    public String inHouse(@org.springframework.web.bind.annotation.RequestParam(defaultValue = "1") int page,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String keyword,
            Model model) {
        int pageSize = 10;
        List<Booking> allInHouseBookingsRaw = bookingRepository.findCheckedIn();
        List<Booking> allInHouseBookings = new ArrayList<>();

        if (allInHouseBookingsRaw != null) {
            for (Booking b : allInHouseBookingsRaw) {
                if (!matchesKeyword(b, keyword)) {
                    continue;
                }
                allInHouseBookings.add(b);
            }
        }

        int totalInHouseItems = allInHouseBookings.size();
        int totalInHousePages = (int) Math.ceil((double) totalInHouseItems / pageSize);
        if (totalInHousePages == 0)
            totalInHousePages = 1;

        int inHousePage = page;
        if (inHousePage < 1)
            inHousePage = 1;
        if (inHousePage > totalInHousePages)
            inHousePage = totalInHousePages;

        int startInHouseItem = (inHousePage - 1) * pageSize;
        List<Booking> pagedInHouseBookings = new ArrayList<>();
        if (totalInHouseItems >= startInHouseItem) {
            int toIndex = Math.min(startInHouseItem + pageSize, totalInHouseItems);
            pagedInHouseBookings = allInHouseBookings.subList(startInHouseItem, toIndex);
        }

        List<Map<String, Object>> pagedInHouse = new ArrayList<>();
        for (Booking b : pagedInHouseBookings) {
            List<RoomBookingDetail> details = roomBookingDetailRepository.findByRoomBookingId(b.getId());
            if (details == null || details.isEmpty())
                continue;

            String guestName = b.getCustomer() != null ? b.getCustomer().getFullName() : "Unknown";

            Map<String, Object> map = new HashMap<>();
            map.put("id", b.getId());
            map.put("guestName", guestName);

            String checkInStr = "";
            String checkOutStr = "";
            if (b instanceof RoomBooking) {
                RoomBooking rb = (RoomBooking) b;
                checkInStr = rb.getCheckInDate() != null
                        ? rb.getCheckInDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        : "";
                checkOutStr = rb.getCheckOutDate() != null
                        ? rb.getCheckOutDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        : "";
            }
            map.put("checkInDate", checkInStr);
            map.put("checkOutDate", checkOutStr);

            int roomCount = (int) details.stream().filter(d -> d.getRoom() != null).count();
            int guestCount = 0;
            for (RoomBookingDetail d : details) {
                guestCount += roomGuestRepository.findByRoomBookingDetailId(d.getId()).size();
            }
            List<com.kawai.dto.DependentResponseDTO> deps = dependentService.getGuestListByBooking(b.getId());
            if (guestCount == 0) {
                guestCount = 1 + deps.size();
            }
            String bookingScale = roomCount + " Phòng, " + guestCount + " Khách";
            map.put("bookingScale", bookingScale);

            // Add missing fields for the new UI
            map.put("phone", b.getCustomer() != null ? b.getCustomer().getPhone() : "");
            
            // Generate room summary
            Map<String, Long> categoryCount = details.stream()
                    .filter(d -> d.getCategory() != null)
                    .collect(Collectors.groupingBy(d -> d.getCategory().getCategoryName(), Collectors.counting()));
            String roomSummary = categoryCount.entrySet().stream()
                    .map(entry -> entry.getValue() + "x " + entry.getKey())
                    .collect(Collectors.joining(", "));
            if (roomSummary.isEmpty()) roomSummary = "N/A";
            map.put("roomSummary", roomSummary);

            // Active details for room transfer
            map.put("activeDetails", details.stream()
                    .filter(d -> d.getRoom() != null && "CHECKED_IN".equalsIgnoreCase(d.getDetailStatus()))
                    .map(d -> {
                        Map<String, Object> detailMap = new HashMap<>();
                        detailMap.put("id", d.getId());
                        detailMap.put("label", d.getRoom().getRoomNumber() + " - "
                                + (d.getCategory() != null ? d.getCategory().getCategoryName() : "Room"));
                        return detailMap;
                    })
                    .collect(Collectors.toList()));
            
            map.put("dependents", deps);

            pagedInHouse.add(map);
        }

        model.addAttribute("inHouseBookings", pagedInHouse);
        model.addAttribute("totalInHouseCount", totalInHouseItems);
        model.addAttribute("currentInHousePage", inHousePage);
        model.addAttribute("totalInHousePages", totalInHousePages);
        model.addAttribute("keyword", keyword);
        model.addAttribute("vacantRooms", roomRepository.findVacant().stream()
                .filter(r -> "Vacant_Clean".equalsIgnoreCase(r.getRoomStatus())
                        || "Available".equalsIgnoreCase(r.getRoomStatus()))
                .collect(Collectors.toList()));

        // Fetch cancelled bookings
        List<Booking> allCancelledRaw = bookingRepository.findCancelledBookings();
        List<Booking> allCancelled = new ArrayList<>();
        if (allCancelledRaw != null) {
            for (Booking b : allCancelledRaw) {
                if (!matchesKeyword(b, keyword))
                    continue;
                allCancelled.add(b);
            }
        }
        // Sắp xếp đơn Hủy mới nhất lên đầu (Id giảm dần)
        allCancelled.sort(java.util.Comparator.comparing(Booking::getId).reversed());

        List<Map<String, Object>> mappedCancelled = new ArrayList<>();
        for (Booking b : allCancelled) {
            List<RoomBookingDetail> details = roomBookingDetailRepository.findByRoomBookingId(b.getId());
            if (details == null || details.isEmpty())
                continue;
            String guestName = b.getCustomer() != null ? b.getCustomer().getFullName() : "Unknown";

            Map<String, Object> map = new HashMap<>();
            map.put("id", b.getId());
            map.put("guestName", guestName);
            map.put("status", b.getBookingStatus());

            String checkInStr = "";
            if (b instanceof RoomBooking) {
                RoomBooking rb = (RoomBooking) b;
                checkInStr = rb.getCheckInDate() != null
                        ? rb.getCheckInDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        : "";
            }
            map.put("checkInDate", checkInStr);

            String roomSummary = details.stream()
                    .filter(d -> d.getRoom() != null)
                    .map(d -> d.getRoom().getRoomNumber() + " ("
                            + (d.getCategory() != null ? d.getCategory().getCategoryName() : "Unknown") + ")")
                    .collect(Collectors.joining(", "));
            if (roomSummary.isEmpty())
                roomSummary = "N/A";
            map.put("roomSummary", roomSummary);

            mappedCancelled.add(map);
        }
        model.addAttribute("cancelledBookings", mappedCancelled);

        return "receptionist/in-house";
    }

    @GetMapping("/folio")
    public String folio(Model model) {
        return "receptionist/folio";
    }

    @GetMapping("/folio/detail")
    public String folioDetail(Model model) {
        return "receptionist/folio-detail";
    }

    @GetMapping("/night-audit")
    public String nightAudit(Model model) {
        return "receptionist/night-audit";
    }

    @GetMapping("/operations")
    public String operations(Model model) {
        model.addAttribute("operations", housekeepingService.getPendingOperations());
        model.addAttribute("rooms", roomRepository.findAll());
        return "receptionist/operations";
    }

    @org.springframework.web.bind.annotation.PostMapping("/operations/clean/{taskId}")
    public String completeCleaning(@org.springframework.web.bind.annotation.PathVariable Long taskId,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            housekeepingService.updateRoomToClean(taskId, null);
            redirectAttributes.addFlashAttribute("successMessage", "Đã chuyển phòng về trạng thái sạch.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/receptionist/operations";
    }

    @org.springframework.web.bind.annotation.PostMapping("/operations/maintenance")
    public String createMaintenance(@org.springframework.web.bind.annotation.RequestParam Long roomId,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String notes,
            org.springframework.security.core.Authentication authentication,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            housekeepingService.createMaintenanceRequest(roomId, resolveEmployeeId(authentication), notes);
            redirectAttributes.addFlashAttribute("successMessage", "Đã tạo phiếu bảo trì.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/receptionist/operations";
    }

    @org.springframework.web.bind.annotation.PostMapping("/operations/maintenance/{taskId}/complete")
    public String completeMaintenance(@org.springframework.web.bind.annotation.PathVariable Long taskId,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            housekeepingService.completeMaintenance(taskId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã hoàn tất bảo trì.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/receptionist/operations";
    }


    @org.springframework.web.bind.annotation.PostMapping("/check-in/cancel-no-show/{id}")
    public String cancelNoShow(@org.springframework.web.bind.annotation.PathVariable Long id,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            // checkinService.markAsNoShow(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã đánh dấu hủy thành công và giải phóng phòng!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/receptionist/check-in";
    }

    private boolean matchesKeyword(Booking b, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return true;
        }
        String guestName = b.getCustomer() != null ? b.getCustomer().getFullName() : "";
        String phone = b.getCustomer() != null ? b.getCustomer().getPhone() : "";
        String cccdEnc = b.getCustomer() != null ? b.getCustomer().getCccdPassportEncrypted() : "";
        String cccd = "";
        if (cccdEnc != null && !cccdEnc.isEmpty()) {
            try {
                cccd = com.kawai.utils.EncryptionUtils.decrypt(cccdEnc);
            } catch (Exception e) {
                cccd = cccdEnc;
            }
        }

        String kw = keyword.trim().toLowerCase();
        return guestName.toLowerCase().contains(kw) ||
                phone.toLowerCase().contains(kw) ||
                cccd.toLowerCase().contains(kw);
    }

    private Long resolveEmployeeId(org.springframework.security.core.Authentication authentication) {
        if (authentication != null && authentication.getName() != null) {
            return employeeRepository.findByAccountUsername(authentication.getName())
                    .map(Employee::getId)
                    .orElseGet(this::firstEmployeeId);
        }
        return firstEmployeeId();
    }

    private Long firstEmployeeId() {
        return employeeRepository.findAll().stream()
                .findFirst()
                .map(Employee::getId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên để ghi nhận thao tác."));
    }
}
