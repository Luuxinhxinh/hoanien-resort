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
    private final RestaurantTableRepository restaurantTableRepository;

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
        List<Map<String, Object>> rooms = new ArrayList<>();
        try {
            for (Room r : roomRepository.findAll()) {
                Map<String, Object> m = new HashMap<>();
                m.put("roomNumber", r.getRoomNumber());
                m.put("status", r.getRoomStatus());
                m.put("category", r.getCategory() != null ? r.getCategory().getCategoryName() : "");
                rooms.add(m);
            }
        } catch (Exception e) {
        }
        model.addAttribute("rooms", rooms);

        // Today label
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", new Locale("vi")));
        model.addAttribute("todayLabel", today);
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

    @GetMapping("/check-in-out")
    public String checkInOut(Model model) {
        return "receptionist/check-in-out";
    }

    @GetMapping("/folio")
    public String folio(Model model) {
        return "receptionist/folio";
    }

    @GetMapping("/night-audit")
    public String nightAudit(Model model) {
        return "receptionist/night-audit";
    }
}