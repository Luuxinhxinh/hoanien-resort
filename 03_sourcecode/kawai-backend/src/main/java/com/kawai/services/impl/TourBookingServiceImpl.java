package com.kawai.services.impl;

import com.kawai.dto.TourBookingRequest;
import com.kawai.models.*;
import com.kawai.repositories.*;
import com.kawai.services.interfaces.TourBookingService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation của {@link TourBookingService} cho UC20.1: Đặt tour du lịch.
 *
 * <p>
 * Triển khai đầy đủ nghiệp vụ:
 * <ul>
 *   <li>Validate schedule & customer tồn tại</li>
 *   <li>Chống Double-booking: kiểm tra available slots trước khi tạo booking</li>
 *   <li>Tạo TourBooking + N TourAttendee records</li>
 *   <li>Hỗ trợ Post to Room: ghi nợ vào Folio phòng</li>
 * </ul>
 *
 * <p>
 * TDD Phase: 🟢 GREEN — Triển khai production code để pass tất cả test cases
 * (TC-M4-003, TC-M4-004, TC-M4-005).
 */
@Service
public class TourBookingServiceImpl implements TourBookingService {

    private static final Logger LOG = LoggerFactory.getLogger(TourBookingServiceImpl.class);

    private final TourScheduleRepository tourScheduleRepository;
    private final TourBookingRepository tourBookingRepository;
    private final TourAttendeeRepository tourAttendeeRepository;
    private final CustomerRepository customerRepository;
    private final FolioItemRepository folioItemRepository;

    public TourBookingServiceImpl(TourScheduleRepository tourScheduleRepository,
                                  TourBookingRepository tourBookingRepository,
                                  TourAttendeeRepository tourAttendeeRepository,
                                  CustomerRepository customerRepository,
                                  FolioItemRepository folioItemRepository) {
        this.tourScheduleRepository = tourScheduleRepository;
        this.tourBookingRepository = tourBookingRepository;
        this.tourAttendeeRepository = tourAttendeeRepository;
        this.customerRepository = customerRepository;
        this.folioItemRepository = folioItemRepository;
    }

    @Override
    public Long createTourBooking(TourBookingRequest request) {
        // 1. Validate schedule & customer
        TourSchedule schedule = tourScheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new IllegalStateException("TOUR-002: Schedule not found"));
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new IllegalStateException("TOUR-003: Customer not found"));

        // 2. Chống Double-booking: kiểm tra available slots
        int alreadyBooked = tourBookingRepository.countByScheduleAndBookingStatus(schedule, "Confirmed");
        int remainingCapacity = schedule.getTour().getMaxCapacity() - alreadyBooked;

        if (request.getParticipantCount() > remainingCapacity) {
            LOG.warn("TOUR-001: Tour schedule {} hết chỗ. Already={}, Request={}, Max={}",
                    schedule.getId(), alreadyBooked, request.getParticipantCount(),
                    schedule.getTour().getMaxCapacity());
            throw new IllegalStateException(
                    "TOUR-001: Hết chỗ. Chỉ còn " + remainingCapacity + " chỗ trống");
        }

        // 3. Tính tổng giá
        BigDecimal totalPrice = schedule.getTour().getBasePrice()
                .multiply(BigDecimal.valueOf(request.getParticipantCount()));

        // 4. Tạo TourBooking
        TourBooking booking = new TourBooking();
        booking.setSchedule(schedule);
        booking.setCustomer(customer);
        booking.setBookingDate(LocalDate.now());
        booking.setParticipantCount(request.getParticipantCount());
        booking.setIsWalkInTour(request.isWalkInTour());
        booking.setBookingStatus("Confirmed");
        booking.setBookingSource("Direct_Web");
        booking.setTotalPrice(totalPrice);

        TourBooking savedBooking = tourBookingRepository.save(booking);
        LOG.info("Created tour booking {} for schedule {} ({} pax)",
                savedBooking.getId(), schedule.getId(), request.getParticipantCount());

        // 5. Tạo TourAttendee records
        List<TourAttendee> attendees = new ArrayList<>();
        for (int i = 0; i < request.getParticipantCount(); i++) {
            TourAttendee attendee = new TourAttendee();
            attendee.setTourBooking(savedBooking);
            attendee.setCustomer(customer);
            attendee.setFullName(customer.getFullName());
            attendee.setIsCheckedIn(false);
            attendees.add(attendee);
        }
        tourAttendeeRepository.saveAll(attendees);

        // 6. Post to Room: ghi nợ vào Folio phòng
        if (request.isPostToRoom()) {
            FolioItem folioItem = new FolioItem();
            folioItem.setBooking(savedBooking);
            folioItem.setRoomBookingDetail(null);
            folioItem.setPayerCustomer(customer);
            folioItem.setSourceDepartment("Tour");
            folioItem.setAmount(totalPrice);
            folioItem.setDescription("Tour: " + schedule.getTour().getTourName()
                    + " (" + request.getParticipantCount() + " pax)");
            folioItem.setIsSettledSeparately(false);

            folioItemRepository.save(folioItem);
            LOG.info("Post to Room: FolioItem created for tour booking {} - {} VND",
                    savedBooking.getId(), totalPrice);
        }

        return savedBooking.getId();
    }
}
