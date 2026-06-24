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

@Controller
@RequestMapping("/receptionist")
@AllArgsConstructor
public class ReceptionistController {

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final RoomBookingRepository roomBookingRepository;
    private final RoomBookingDetailRepository roomBookingDetailRepository;
    private final com.kawai.services.interfaces.DependentService dependentService;

    @org.springframework.web.bind.annotation.ModelAttribute("todayLabel")
    public String getTodayLabel() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", new Locale("vi")));
    }

    @GetMapping("/dashboard")
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
        return "receptionist/walk-in";
    }

    @GetMapping("/check-in")
    public String checkIn(@org.springframework.web.bind.annotation.RequestParam(defaultValue = "1") int page,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String keyword,
            Model model) {
        List<Booking> allConfirmed = bookingRepository.findConfirmed();
        List<Booking> filteredArrivals = new ArrayList<>();

        // 1. Lọc theo keyword và loại Booking (chỉ lấy RoomBooking)
        if (allConfirmed != null) {
            for (Booking b : allConfirmed) {
                if (!(b instanceof RoomBooking))
                    continue;

                if (keyword != null && !keyword.trim().isEmpty()) {
                    String guestName = b.getCustomer() != null ? b.getCustomer().getFullName() : "";
                    String phone = b.getCustomer() != null ? b.getCustomer().getPhone() : "";
                    String cccdEnc = b.getCustomer() != null ? b.getCustomer().getCccdPassportEncrypted() : "";
                    String cccd = "";
                    if (!cccdEnc.isEmpty()) {
                        try {
                            cccd = com.kawai.utils.EncryptionUtils.decrypt(cccdEnc);
                        } catch (Exception e) {
                            cccd = cccdEnc;
                        }
                    }

                    String kw = keyword.trim().toLowerCase();
                    boolean match = guestName.toLowerCase().contains(kw) ||
                            phone.toLowerCase().contains(kw) ||
                            cccd.toLowerCase().contains(kw);
                    if (!match)
                        continue;
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
            if (!cccdEnc.isEmpty()) {
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

            String roomSummary = "N/A";
            Map<String, Long> categoryCount = details.stream()
                    .filter(d -> d.getCategory() != null)
                    .collect(Collectors.groupingBy(d -> d.getCategory().getCategoryName(),
                            Collectors.counting()));

            if (!categoryCount.isEmpty()) {
                roomSummary = categoryCount.entrySet().stream()
                        .map(entry -> entry.getValue() + "x " + entry.getKey())
                        .collect(Collectors.joining(", "));
            }
            map.put("roomSummary", roomSummary);

            // (Removed unused detailsList creation)
            List<com.kawai.dto.DependentResponseDTO> deps = dependentService.getGuestListByBooking(b.getId());
            map.put("dependents", deps);

            pagedArrivals.add(map);
        }

        // In-house logic has been moved to /in-house

        model.addAttribute("arrivals", pagedArrivals);
        model.addAttribute("pendingArrivalsCount", totalItems);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);

        Map<String, List<String>> inventory = new HashMap<>();
        for (Room r : roomRepository.findVacant()) {
            String cat = r.getCategory() != null ? r.getCategory().getCategoryName() : "Other";
            inventory.computeIfAbsent(cat, k -> new ArrayList<>()).add(r.getRoomNumber());
        }
        model.addAttribute("roomInventory", inventory);

        return "receptionist/check-in";
    }

    @GetMapping("/in-house")
    public String inHouse(@org.springframework.web.bind.annotation.RequestParam(defaultValue = "1") int page,
            Model model) {
        int pageSize = 10;
        List<Booking> allInHouseBookings = bookingRepository.findCheckedIn();
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
            String phone = b.getCustomer() != null ? b.getCustomer().getPhone() : "";

            Map<String, Object> map = new HashMap<>();
            map.put("id", b.getId());
            map.put("guestName", guestName);
            map.put("phone", phone);

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
            List<com.kawai.dto.DependentResponseDTO> deps = dependentService.getGuestListByBooking(b.getId());
            map.put("dependents", deps);

            int guestCount = 1 + deps.size();
            String bookingScale = roomCount + " Phòng, " + guestCount + " Khách";
            map.put("bookingScale", bookingScale);

            String roomSummary = details.stream()
                    .filter(d -> d.getRoom() != null)
                    .map(d -> d.getRoom().getRoomNumber() + " ("
                            + (d.getCategory() != null ? d.getCategory().getCategoryName() : "Unknown") + ")")
                    .collect(Collectors.joining(", "));
            if (roomSummary.isEmpty())
                roomSummary = "N/A";
            map.put("roomSummary", roomSummary);

            pagedInHouse.add(map);
        }

        model.addAttribute("inHouseBookings", pagedInHouse);
        model.addAttribute("totalInHouseCount", totalInHouseItems);
        model.addAttribute("currentInHousePage", inHousePage);
        model.addAttribute("totalInHousePages", totalInHousePages);
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
}