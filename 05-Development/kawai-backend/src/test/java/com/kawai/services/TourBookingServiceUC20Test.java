package com.kawai.services;

import com.kawai.dto.TourBookingRequest;
import com.kawai.models.*;
import com.kawai.repositories.*;
import com.kawai.services.impl.TourBookingServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ================================================================
 * KAWAI RESORT — TDD Unit Test cho TourBookingService (UC20.1)
 * ================================================================
 * 
 * Test Case tham chiếu:
 * - TC-M4-003 (UC20.1): Đặt tour thành công — tạo bản ghi Tour_Attendees
 * - TC-M4-004 (UC20.1): Tour hết slot — đặt thêm bị chặn, trả TOUR-001
 * - TC-M4-005 (UC20.1): Đặt tour Post to Room — ghi nợ vào Folio phòng
 * 
 * TDD Phase: 🔴 RED — Test được viết TRƯỚC khi implement production code.
 * Service chưa có implementation (sẽ throw compilation error hoặc
 * UnsupportedOperationException cho đến khi implement xong).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("UC20.1 — Đặt tour du lịch (TourBookingService)")
class TourBookingServiceUC20Test {

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

    @Mock
    private RoomBookingDetailRepository roomBookingDetailRepository;
    
    @Mock
    private RoomBookingRepository roomBookingRepository;

    @Mock
    private PromotionRepository promotionRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private TourBookingServiceImpl tourBookingService;

    // ===== Test Data Fixtures =====

    private Tour sampleTour;
    private TourSchedule sampleSchedule;
    private Customer sampleCustomer;
    private TourBookingRequest validRequest;

    @BeforeEach
    void setUp() {
        // Tạo Tour
        sampleTour = new Tour();
        sampleTour.setId(1L);
        sampleTour.setTourName("Vịnh Hạ Long - 1 Ngày");
        sampleTour.setBasePrice(new BigDecimal("1500000"));
        sampleTour.setMaxCapacity(30);

        // Tạo TourSchedule
        sampleSchedule = new TourSchedule();
        sampleSchedule.setId(100L);
        sampleSchedule.setTour(sampleTour);
        sampleSchedule.setDepartureDate(LocalDate.of(2026, 7, 10));
        sampleSchedule.setDepartureTime(LocalTime.of(8, 0));
        sampleSchedule.setBookedSeats(10); // capacity 30 - 10 = 20 available slots
        sampleSchedule.setScheduleStatus("Open");

        // Tạo Customer
        sampleCustomer = new Customer();
        sampleCustomer.setId(10L);
        sampleCustomer.setFullName("Nguyễn Văn A");

        // Tạo Request hợp lệ
        validRequest = new TourBookingRequest();
        validRequest.setScheduleId(100L);
        validRequest.setCustomerId(10L);
        validRequest.setParticipantCount(2);

        validRequest.setPostToRoom(false);
    }

    // ================================================================
    // TC-M4-003: Đặt tour thành công — tạo bản ghi Tour_Attendees
    // ================================================================
    @Nested
    @DisplayName("TC-M4-003: Đặt tour thành công")
    class TC_M4_003 {

        @Test
        @DisplayName("TC-M4-003.1: Đặt tour thành công — tạo TourBooking và Tour_Attendees")
        void createTourBooking_ValidRequest_ShouldCreateBookingAndAttendees() {
            // ARRANGE
            validRequest.setParticipantCount(3);
            when(tourScheduleRepository.findById(100L)).thenReturn(Optional.of(sampleSchedule));
            when(customerRepository.findById(10L)).thenReturn(Optional.of(sampleCustomer));
            when(roomBookingRepository.findById(1L)).thenReturn(Optional.of(new RoomBooking()));
            when(tourBookingRepository.countByScheduleAndBookingStatus(sampleSchedule, "Confirmed"))
                    .thenReturn(0);

            TourBooking savedBooking = new TourBooking();
            savedBooking.setId(200L);
            savedBooking.setSchedule(sampleSchedule);
            savedBooking.setParticipantCount(3);
            savedBooking.setBookingStatus("Confirmed");
            when(tourBookingRepository.save(any(TourBooking.class))).thenReturn(savedBooking);
            when(tourAttendeeRepository.saveAll(anyList())).thenReturn(null);

            // ACT
            Long bookingId = tourBookingService.createTourBooking(validRequest);

            // ASSERT
            assertNotNull(bookingId, "Booking ID không được null");
            assertEquals(200L, bookingId, "Phải trả về đúng booking ID");

            // Verify các repository được gọi đúng
            verify(tourScheduleRepository).findById(100L);
            verify(customerRepository).findById(10L);
            verify(tourBookingRepository).save(any(TourBooking.class));
            verify(tourAttendeeRepository).saveAll(argThat(list -> {
                // participantCount = 3, nên phải tạo đúng 3 TourAttendee records
                return ((java.util.List<?>) list).size() == 3;
            }));
        }
    }

    // ================================================================
    // TC-M4-004: Tour hết slot — chặn đặt thêm, trả TOUR-001
    // ================================================================
    @Nested
    @DisplayName("TC-M4-004: Tour hết slot — chặn đặt thêm")
    class TC_M4_004 {

        @Test
        @DisplayName("TC-M4-004.1: Tour hết chỗ — đặt thêm bị chặn, trả IllegalStateException")
        void createTourBooking_NoAvailableSlots_ShouldThrowException() {
            // ARRANGE: Schedule có 5 chỗ trống (bookedSeats = 25)
            sampleSchedule.setBookedSeats(25);
            validRequest.setParticipantCount(3);
            when(tourScheduleRepository.findById(100L)).thenReturn(Optional.of(sampleSchedule));
            when(customerRepository.findById(10L)).thenReturn(Optional.of(sampleCustomer));
            when(roomBookingRepository.findById(1L)).thenReturn(Optional.of(new RoomBooking()));

            // Đã có 18 người đặt, capacity = 30, chỉ còn 12 chỗ, nhưng participantCount = 3
            // availableSlots = maxCapacity - alreadyBooked = 30 - 0 = 30 (không dùng
            // availableSlots)
            // Test này cần check: alreadyBooked + participantCount > maxCapacity
            when(tourBookingRepository.countByScheduleAndBookingStatus(sampleSchedule, "Confirmed"))
                    .thenReturn(28); // 28 đã đặt, capacity 30, còn 2 chỗ, nhưng request 3 người

            // ACT & ASSERT
            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> tourBookingService.createTourBooking(validRequest),
                    "Phải throw IllegalStateException khi không đủ chỗ");

            assertTrue(exception.getMessage().contains("TOUR-001") ||
                    exception.getMessage().contains("hết chỗ") ||
                    exception.getMessage().contains("đủ chỗ"),
                    "Exception message phải chứa mã lỗi TOUR-001 hoặc thông báo hết chỗ");

            // Verify: KHÔNG được tạo booking
            verify(tourBookingRepository, never()).save(any(TourBooking.class));
            verify(tourAttendeeRepository, never()).saveAll(anyList());
        }

        @Test
        @DisplayName("TC-M4-004.2: Đặt tour với số lượng khách vượt quá capacity — bị chặn")
        void createTourBooking_ExceedsMaxCapacity_ShouldThrowException() {
            // ARRANGE: capacity = 30, đã có 29 người đặt, request 3 người
            when(tourScheduleRepository.findById(100L)).thenReturn(Optional.of(sampleSchedule));
            when(customerRepository.findById(10L)).thenReturn(Optional.of(sampleCustomer));
            when(roomBookingRepository.findById(1L)).thenReturn(Optional.of(new RoomBooking()));
            when(tourBookingRepository.countByScheduleAndBookingStatus(sampleSchedule, "Confirmed"))
                    .thenReturn(29);

            validRequest.setParticipantCount(3); // 29 + 3 = 32 > 30

            // ACT & ASSERT
            assertThrows(IllegalStateException.class,
                    () -> tourBookingService.createTourBooking(validRequest));

            verify(tourBookingRepository, never()).save(any(TourBooking.class));
        }
    }

    // ================================================================
    // TC-M4-005: Đặt tour Post to Room — ghi nợ vào Folio phòng
    // ================================================================
    @Nested
    @DisplayName("TC-M4-005: Đặt tour Post to Room — ghi nợ Folio")
    class TC_M4_005 {

        @Test
        @DisplayName("TC-M4-005.1: Đặt tour Post to Room — ghi nợ Folio thành công")
        void createTourBooking_PostToRoom_ShouldChargeToFolio() {
            // ARRANGE: Post to Room
            validRequest.setPostToRoom(true);
            validRequest.setRoomBookingDetailId(50L);
            validRequest.setParticipantCount(3);

            RoomBookingDetail sampleDetail = new RoomBookingDetail();
            sampleDetail.setId(50L);
            sampleDetail.setSubCreditLimit(new java.math.BigDecimal("10000000"));

            when(tourScheduleRepository.findById(100L)).thenReturn(Optional.of(sampleSchedule));
            when(customerRepository.findById(10L)).thenReturn(Optional.of(sampleCustomer));
            when(roomBookingRepository.findById(1L)).thenReturn(Optional.of(new RoomBooking()));
            when(roomBookingDetailRepository.findById(50L)).thenReturn(Optional.of(sampleDetail));
            when(tourBookingRepository.countByScheduleAndBookingStatus(sampleSchedule, "Confirmed"))
                    .thenReturn(0);

            TourBooking savedBooking = new TourBooking();
            savedBooking.setId(300L);
            savedBooking.setSchedule(sampleSchedule);
            savedBooking.setParticipantCount(3);
            savedBooking.setBookingStatus("Confirmed");
            savedBooking.setTotalPrice(new BigDecimal("4500000")); // 3 * 1.500.000
            when(tourBookingRepository.save(any(TourBooking.class))).thenReturn(savedBooking);
            when(tourAttendeeRepository.saveAll(anyList())).thenReturn(null);

            FolioItem savedFolioItem = new FolioItem();
            savedFolioItem.setId(400L);
            when(folioItemRepository.save(any(FolioItem.class))).thenReturn(savedFolioItem);

            // ACT
            Long bookingId = tourBookingService.createTourBooking(validRequest);

            // ASSERT
            assertNotNull(bookingId, "Booking ID không được null");

            // Verify FolioItem được tạo với đúng thông tin
            verify(folioItemRepository).save(argThat((FolioItem item) -> {
                return item.getSourceDepartment().equals("Tour")
                        && item.getAmount().equals(new BigDecimal("4500000"))
                        && item.getDescription().contains("Vịnh Hạ Long");
            }));
        }

        @Test
        @DisplayName("TC-M4-005.2: Đặt tour không Post to Room — KHÔNG ghi nợ Folio")
        void createTourBooking_NotPostToRoom_ShouldNotChargeFolio() {
            // ARRANGE: Request mặc định — không Post to Room
            when(tourScheduleRepository.findById(100L)).thenReturn(Optional.of(sampleSchedule));
            when(customerRepository.findById(10L)).thenReturn(Optional.of(sampleCustomer));
            when(roomBookingRepository.findById(1L)).thenReturn(Optional.of(new RoomBooking()));
            when(tourBookingRepository.countByScheduleAndBookingStatus(sampleSchedule, "Confirmed"))
                    .thenReturn(0);

            TourBooking savedBooking = new TourBooking();
            savedBooking.setId(400L);
            savedBooking.setSchedule(sampleSchedule);
            savedBooking.setParticipantCount(3);
            savedBooking.setBookingStatus("Confirmed");
            when(tourBookingRepository.save(any(TourBooking.class))).thenReturn(savedBooking);
            when(tourAttendeeRepository.saveAll(anyList())).thenReturn(null);

            // ACT
            Long bookingId = tourBookingService.createTourBooking(validRequest);

            // ASSERT
            assertNotNull(bookingId);

            // Verify FolioItem KHÔNG được gọi
            verify(folioItemRepository, never()).save(any(FolioItem.class));
        }

        @Test
        @DisplayName("TC-M4-005.3: Đặt tour Post to Room nhưng không cung cấp phòng — lỗi TOUR-004")
        void createTourBooking_PostToRoom_NullRoomId_ShouldThrowTOUR004() {
            // ARRANGE: postToRoom=true nhưng roomBookingDetailId=null → phải throw TOUR-004
            validRequest.setPostToRoom(true);
            validRequest.setRoomBookingDetailId(null);

            when(tourScheduleRepository.findById(100L)).thenReturn(Optional.of(sampleSchedule));
            when(customerRepository.findById(10L)).thenReturn(Optional.of(sampleCustomer));
            when(tourBookingRepository.countByScheduleAndBookingStatus(sampleSchedule, "Confirmed"))
                    .thenReturn(0);

            TourBooking savedBooking = new TourBooking();
            savedBooking.setId(500L);
            savedBooking.setSchedule(sampleSchedule);
            savedBooking.setParticipantCount(3);
            savedBooking.setBookingStatus("Confirmed");
            savedBooking.setTotalPrice(new BigDecimal("4500000"));
            when(tourBookingRepository.save(any(TourBooking.class))).thenReturn(savedBooking);
            when(tourAttendeeRepository.saveAll(anyList())).thenReturn(null);

            // ACT & ASSERT
            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> tourBookingService.createTourBooking(validRequest),
                    "Phải throw IllegalStateException khi postToRoom=true nhưng roomBookingDetailId=null");

            assertTrue(exception.getMessage().contains("TOUR-004"),
                    "Exception message phải chứa mã lỗi TOUR-004. Actual: " + exception.getMessage());

            // Verify: FolioItem KHÔNG được tạo
            verify(folioItemRepository, never()).save(any(FolioItem.class));
        }

        @Test
        @DisplayName("TC-M4-005.4: Đặt tour Post to Room với phòng không tồn tại trong DB — lỗi TOUR-005")
        void createTourBooking_PostToRoom_RoomNotFound_ShouldThrowTOUR005() {
            // ARRANGE: roomBookingDetailId=99L nhưng không tồn tại trong DB → phải throw TOUR-005
            validRequest.setPostToRoom(true);
            validRequest.setRoomBookingDetailId(99L);

            when(tourScheduleRepository.findById(100L)).thenReturn(Optional.of(sampleSchedule));
            when(customerRepository.findById(10L)).thenReturn(Optional.of(sampleCustomer));
            when(tourBookingRepository.countByScheduleAndBookingStatus(sampleSchedule, "Confirmed"))
                    .thenReturn(0);
            when(roomBookingDetailRepository.findById(99L)).thenReturn(Optional.empty());

            TourBooking savedBooking = new TourBooking();
            savedBooking.setId(600L);
            savedBooking.setSchedule(sampleSchedule);
            savedBooking.setParticipantCount(3);
            savedBooking.setBookingStatus("Confirmed");
            savedBooking.setTotalPrice(new BigDecimal("4500000"));
            when(tourBookingRepository.save(any(TourBooking.class))).thenReturn(savedBooking);
            when(tourAttendeeRepository.saveAll(anyList())).thenReturn(null);

            // ACT & ASSERT
            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> tourBookingService.createTourBooking(validRequest),
                    "Phải throw IllegalStateException khi RoomBookingDetail ID=99 không tồn tại");

            assertTrue(exception.getMessage().contains("TOUR-005"),
                    "Exception message phải chứa mã lỗi TOUR-005. Actual: " + exception.getMessage());

            // Verify: FolioItem KHÔNG được tạo
            verify(folioItemRepository, never()).save(any(FolioItem.class));
        }
    }
}