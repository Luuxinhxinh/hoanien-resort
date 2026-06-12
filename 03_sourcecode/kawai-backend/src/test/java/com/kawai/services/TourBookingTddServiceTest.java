package com.kawai.services;

import com.kawai.dto.TourBookingRequest;
import com.kawai.models.*;
import com.kawai.repositories.*;
import com.kawai.services.impl.TourBookingServiceImpl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ================================================================
 * KAWAI RESORT — TDD Unit Test cho Tour Booking & Operations (UC20.1, UC20.2,
 * UC20.3)
 * ================================================================
 *
 * Test Case tham chiếu:
 * - TC-M4-005 (UC20.1): Đặt tour Post to Room — ghi nợ vào Folio phòng
 * - TC-M4-006 (UC20.2): Lập lịch chuyến tour — gán xe, tài xế, Tour Guide
 * - TC-M4-007 (UC20.3): Hủy tour — hoàn tiền theo chính sách hoặc đổi lịch
 *
 * TDD Phase: 🔴 RED — Test được viết để xác nhận fail trước khi viết code triển
 * khai thực tế.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UC20.1/UC20.2/UC20.3 — Tour Operations & Booking TDD Test")
public class TourBookingTddServiceTest {

    @Mock
    private TourScheduleRepository tourScheduleRepository;

    @Mock
    private TourBookingRepository tourBookingRepository;

    @Mock
    private TourAttendeeRepository tourAttendeeRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private FolioItemRepository folioItemRepository;

    @Mock
    private TourStaffAssignmentRepository tourStaffAssignmentRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private TourBookingServiceImpl tourBookingService;

    // ================================================================
    // TC_M4_005_datTourPostToRoom
    // ================================================================
    @Test
    @DisplayName("TC-M4-005: Đặt tour Post to Room — ghi nợ vào Folio phòng")
    void TC_M4_005_datTourPostToRoom() {
        TourBookingRequest request = new TourBookingRequest();
        request.setScheduleId(100L);
        request.setCustomerId(10L);
        request.setParticipantCount(2);
        request.setPostToRoom(true);

        Tour tour = new Tour();
        tour.setId(1L);
        tour.setBasePrice(new BigDecimal("100000"));
        tour.setMaxCapacity(10);

        TourSchedule schedule = new TourSchedule();
        schedule.setId(100L);
        schedule.setTour(tour);
        schedule.setBookedSeats(2);
        schedule.setScheduleStatus("Open");

        Customer customer = new Customer();
        customer.setId(10L);

        when(tourScheduleRepository.findById(100L)).thenReturn(Optional.of(schedule));
        when(customerRepository.findById(10L)).thenReturn(Optional.of(customer));
        when(tourBookingRepository.countByScheduleAndBookingStatus(schedule, "Confirmed")).thenReturn(2);

        TourBooking savedBooking = new TourBooking();
        savedBooking.setId(1L);
        savedBooking.setSchedule(schedule);
        savedBooking.setParticipantCount(2);
        savedBooking.setTotalPrice(new BigDecimal("200000"));
        when(tourBookingRepository.save(any(TourBooking.class))).thenReturn(savedBooking);

        Long bookingId = tourBookingService.createTourBooking(request);

        assertNotNull(bookingId);
        verify(folioItemRepository, times(1)).save(any(FolioItem.class));
    }

    // ================================================================
    // TC_M4_006_lapLichChuyenTour
    // ================================================================
    @Test
    @DisplayName("TC-M4-006: Lập lịch chuyến tour — gán xe, tài xế, Tour Guide")
    void TC_M4_006_lapLichChuyenTour() {
        Long scheduleId = 100L;
        Long employeeId = 5L;
        String role = "Tour_Guide";

        TourSchedule schedule = new TourSchedule();
        schedule.setId(scheduleId);

        Employee employee = new Employee();
        employee.setId(employeeId);

        when(tourScheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

        // Call scheduleTour
        tourBookingService.scheduleTour(scheduleId, employeeId, role);

        // Verify TourStaffAssignment is saved
        verify(tourStaffAssignmentRepository, times(1)).save(any(TourStaffAssignment.class));
    }

    // ================================================================
    // TC_M4_007_huyTourHoanTien
    // ================================================================
    @Test
    @DisplayName("TC-M4-007: Hủy tour — hoàn tiền theo chính sách hoặc đổi lịch")
    void TC_M4_007_huyTourHoanTien() {
        Long bookingId = 200L;

        TourBooking booking = new TourBooking();
        booking.setId(bookingId);
        booking.setTotalPrice(new BigDecimal("1000000"));
        booking.setBookingStatus("Confirmed");

        // Mock booking lookup
        when(tourBookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        // Act: Hủy tour do sự cố resort (hoàn tiền 100%)
        BigDecimal refundAmountResort = tourBookingService.cancelTour(bookingId, true);

        // Assert
        assertEquals(0, new BigDecimal("1000000").compareTo(refundAmountResort),
                "Refund amount phải là 1,000,000 (100%)");
        assertEquals("Cancelled_Refunded", booking.getBookingStatus());

        // Reset booking status for next condition: Khách tự hủy (hoàn 50% cọc)
        booking.setBookingStatus("Confirmed");
        BigDecimal refundAmountCustomer = tourBookingService.cancelTour(bookingId, false);

        // Assert
        assertEquals(0, new BigDecimal("500000").compareTo(refundAmountCustomer),
                "Refund amount phải là 500,000 (50%)");
        assertEquals("Cancelled_Forfeited", booking.getBookingStatus());
    }
}
