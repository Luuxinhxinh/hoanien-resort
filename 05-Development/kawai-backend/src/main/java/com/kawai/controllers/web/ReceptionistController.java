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
    private final RestaurantTableRepository restaurantTableRepository;

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
        model.addAttribute("currentGuests", occupied * 2);

        long pendingCheckIns = 0, checkoutsToday = 0;
        try {
            pendingCheckIns = bookingRepository.countConfirmed();
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
        model.addAttribute("rooms", new ArrayList<>()); // fallback for isEmpty check

        return "receptionist/dashboard";
    }

    @GetMapping("/reservations")
    public String reservations(Model model) {
        List<Map<String, Object>> reservations = new ArrayList<>();
        try {
            for (Booking b : bookingRepository.findConfirmed()) {
                Map<String, Object> m = new HashMap<>();
                m.put("id", b.getId());
                m.put("customerName", b.getCustomer() != null ? b.getCustomer().getFullName() : "Khách");
                m.put("totalPrice", b.getTotalPrice() != null ? b.getTotalPrice().toString() : "0");
                m.put("status", b.getBookingStatus());
                reservations.add(m);
            }
        } catch (Exception e) {
        }
        model.addAttribute("reservations", reservations);
        model.addAttribute("reservationCount", reservations.size());
        return "receptionist/reservations";
    }

    @GetMapping("/walk-in")
    public String walkIn(Model model) {
        Map<String, List<String>> inventory = new HashMap<>();
        try {
            for (Room r : roomRepository.findVacant()) {
                String cat = r.getCategory() != null ? r.getCategory().getCategoryName() : "Other";
                inventory.computeIfAbsent(cat, k -> new ArrayList<>()).add(r.getRoomNumber());
            }
        } catch (Exception e) {
        }
        model.addAttribute("roomInventory", inventory);
        return "receptionist/walk-in";
    }

    @GetMapping("/check-in")
    public String checkIn(@org.springframework.web.bind.annotation.RequestParam(defaultValue = "1") int page,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String keyword,
            Model model) {
        List<Map<String, Object>> arrivals = new ArrayList<>();
        try {
            for (Booking b : bookingRepository.findConfirmed()) {
                List<RoomBookingDetail> details = roomBookingDetailRepository.findByRoomBookingId(b.getId());
                if (details == null || details.isEmpty()) {
                    continue; // Skip bookings without room details (e.g., restaurant bookings)
                }

                String guestName = b.getCustomer() != null ? b.getCustomer().getFullName() : "Khách";
                String phone = b.getCustomer() != null ? b.getCustomer().getPhone() : "";
                String cccd = b.getCustomer() != null ? b.getCustomer().getCccdPassportEncrypted() : "";

                if (keyword != null && !keyword.trim().isEmpty()) {
                    String kw = keyword.trim().toLowerCase();
                    boolean match = guestName.toLowerCase().contains(kw) ||
                            phone.toLowerCase().contains(kw) ||
                            cccd.toLowerCase().contains(kw);
                    if (!match)
                        continue;
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

                // Construct Room Summary from RoomBookingDetail
                String roomSummary = "N/A";
                try {
                    Map<String, Long> categoryCount = details.stream()
                            .filter(d -> d.getCategory() != null)
                            .collect(Collectors.groupingBy(d -> d.getCategory().getCategoryName(),
                                    Collectors.counting()));

                    roomSummary = categoryCount.entrySet().stream()
                            .map(entry -> entry.getValue() + "x " + entry.getKey())
                            .collect(Collectors.joining(", "));
                } catch (Exception e) {
                }

                map.put("roomSummary", roomSummary);
                arrivals.add(map);
            }
        } catch (Exception e) {
        }

        // Pagination logic
        int pageSize = 10;
        int totalItems = arrivals.size();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        if (totalPages == 0)
            totalPages = 1;
        if (page < 1)
            page = 1;
        if (page > totalPages)
            page = totalPages;

        int startItem = (page - 1) * pageSize;
        List<Map<String, Object>> pagedArrivals = new ArrayList<>();
        if (totalItems >= startItem) {
            int toIndex = Math.min(startItem + pageSize, totalItems);
            pagedArrivals = arrivals.subList(startItem, toIndex);
        }

        model.addAttribute("arrivals", pagedArrivals);
        model.addAttribute("pendingArrivalsCount", totalItems);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);

        Map<String, List<String>> inventory = new HashMap<>();
        try {
            for (Room r : roomRepository.findVacant()) {
                String cat = r.getCategory() != null ? r.getCategory().getCategoryName() : "Other";
                inventory.computeIfAbsent(cat, k -> new ArrayList<>()).add(r.getRoomNumber());
            }
        } catch (Exception e) {
        }
        model.addAttribute("roomInventory", inventory);

        return "receptionist/check-in";
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