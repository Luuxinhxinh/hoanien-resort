package com.kawai.services.impl;

import com.kawai.models.FolioItem;
import com.kawai.repositories.FolioItemRepository;
import com.kawai.repositories.RoomBookingDetailRepository;
import com.kawai.repositories.RoomRepository;
import com.kawai.services.interfaces.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private FolioItemRepository folioItemRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private RoomBookingDetailRepository roomBookingDetailRepository;

    @Override
    public Map<String, BigDecimal> getUsaliRevenueReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        List<FolioItem> items = folioItemRepository.findAll().stream()
                .filter(item -> item.getCreatedAt().isAfter(start) && item.getCreatedAt().isBefore(end))
                .toList();

        BigDecimal revRoom = BigDecimal.ZERO;
        BigDecimal revFb = BigDecimal.ZERO;
        BigDecimal revTour = BigDecimal.ZERO;

        for (FolioItem item : items) {
            String dept = item.getSourceDepartment();
            if (dept == null) continue;

            BigDecimal amount = item.getAmount() != null ? item.getAmount() : BigDecimal.ZERO;

            if ("PAYMENT".equalsIgnoreCase(dept) || amount.compareTo(BigDecimal.ZERO) < 0) {
                continue;
            }

            if ("Rooms".equalsIgnoreCase(dept) || "Room".equalsIgnoreCase(dept) || "Minibar".equalsIgnoreCase(dept) || "Mini-bar".equalsIgnoreCase(dept)) {
                revRoom = revRoom.add(amount);
            } else if ("POS".equalsIgnoreCase(dept) || "F&B".equalsIgnoreCase(dept) || "RoomService".equalsIgnoreCase(dept) || "Restaurant".equalsIgnoreCase(dept)) {
                revFb = revFb.add(amount);
            } else if ("Tour".equalsIgnoreCase(dept) || "Tours".equalsIgnoreCase(dept)) {
                revTour = revTour.add(amount);
            } else {
                revRoom = revRoom.add(amount);
            }
        }

        Map<String, BigDecimal> report = new HashMap<>();
        report.put("REV-ROOM", revRoom);
        report.put("REV-FB", revFb);
        report.put("REV-TOUR", revTour);
        report.put("TOTAL", revRoom.add(revFb).add(revTour));
        return report;
    }

    @Override
    public double getOccupancyRate(LocalDate date) {
        long totalRooms = roomRepository.count();
        if (totalRooms == 0) return 0.0;

        long occupiedRooms = roomRepository.findAll().stream()
                .filter(r -> "Occupied".equalsIgnoreCase(r.getRoomStatus()))
                .count();

        return ((double) occupiedRooms / totalRooms) * 100.0;
    }

    @Override
    public long getGuestCount(LocalDate date) {
        return roomBookingDetailRepository.findAll().stream()
                .filter(d -> "CHECKED_IN".equalsIgnoreCase(d.getDetailStatus()))
                .count();
    }

    @Override
    public byte[] exportUsaliReport(LocalDate startDate, LocalDate endDate, String format) {
        Map<String, BigDecimal> report = getUsaliRevenueReport(startDate, endDate);
        
        StringBuilder sb = new StringBuilder();
        sb.append("USALI OPERATIONAL REVENUE REPORT\n");
        sb.append("Period: ").append(startDate).append(" to ").append(endDate).append("\n");
        sb.append("-------------------------------------------\n");
        sb.append("REV-ROOM (Rooms, Minibar): ").append(report.get("REV-ROOM")).append(" VND\n");
        sb.append("REV-FB (POS, Room Service): ").append(report.get("REV-FB")).append(" VND\n");
        sb.append("REV-TOUR (Tours, Excursions): ").append(report.get("REV-TOUR")).append(" VND\n");
        sb.append("-------------------------------------------\n");
        sb.append("TOTAL REVENUE: ").append(report.get("TOTAL")).append(" VND\n");
        
        return sb.toString().getBytes();
    }
}
