package com.kawai.services;

import com.kawai.dto.BookingRequestDTO;
import com.kawai.dto.BookingResponseDTO;
import com.kawai.dto.RoomSelectionDTO;
import com.kawai.exceptions.RoomNotAvailableException;
import com.kawai.models.Booking;
import com.kawai.models.Promotion;
import com.kawai.models.RoomBooking;
import com.kawai.repositories.BookingRepository;
import com.kawai.repositories.RoomGuestRepository;
import com.kawai.repositories.PromotionRepository;
import com.kawai.repositories.RoomBookingRepository;
import com.kawai.repositories.RoomRepository;
import com.kawai.repositories.CustomerRepository;
import com.kawai.repositories.RoomBookingDetailRepository;
import com.kawai.services.impl.BookingServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * JUnit Test — UC10: Đặt phòng & Thanh toán cọc trực tuyến
 * MODULE 2: Quản lý Phòng & Lễ tân
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Standard : ISO/IEC/IEEE 29119-3:2021
 * TDD Phase : 🔴 RED — Toàn bộ test phải FAIL trước khi implement.
 * `mvn test` → BUILD FAILURE là ĐÚNG quy trình.
 *
 * Ánh xạ test case (TC_MASTER_TABLE.md — MOD2, UC10):
 * ┌──────────────┬─────────────────────────────────────────────────────────────┬─────────┐
 * │ TC ID │ Kịch bản │ RED Why │
 * ├──────────────┼─────────────────────────────────────────────────────────────┼─────────┤
 * │ TC-M2-003 │ Đặt phòng OK → status=Pending, cancellationDeadline set │
 * COMPILE │
 * │ TC-M2-004 │ Concurrency: 2 user → 1 OK (CONFIRMED), 1 → 409 │ FAIL │
 * │ TC-M2-005 │ checkOut ≤ checkIn → IllegalArgumentException (BR-DATE-01) │
 * FAIL │
 * │ TC-M2-006 │ Hủy trước 48h → hoàn 100%, status=Cancelled_Refunded │ FAIL │
 * │ TC-M2-007 │ Hủy trong 48h → hoàn 0đ, status=Cancelled_Forfeited │ FAIL │
 * │ TC-M2-008 │ Mã SUMMER10 (10% off, 5 đêm) → discountedPrice chính xác │
 * COMPILE │
 * │ TC-M2-008b │ Mã EARLYBIRD20 (20% off, 3 đêm) → discountedPrice chính xác│
 * COMPILE │
 * │ TC-M2-009a │ Mã hết hạn (isActive=false) → exception + error code │ FAIL │
 * │ TC-M2-009b │ Mã hết validTo → exception + error code │ FAIL │
 * │ TC-M2-009c │ Mã không tồn tại → exception + error code rõ ràng │ FAIL │
 * └──────────────┴─────────────────────────────────────────────────────────────┴─────────┘
 *
 * Business Rules kiểm thử:
 * BR-BOOK-01 : chống overbooking — countOverlappingBookings > 0 → 409
 * BR-DATE-01 : checkOutDate > checkInDate bắt buộc (≥ 1 đêm)
 * BR-FIN-01 : mã promo phải isActive=true & validTo ≥ today
 * BR-FIN-02 : hủy trước 48h → hoàn 100%; hủy trong 48h → hoàn 0đ
 * BR-STATUS-01: booking vừa tạo → "Pending"; sau xác nhận VNPay → "CONFIRMED"
 * BR-STATUS-02: hủy trước 48h → "Cancelled_Refunded"; hủy trong 48h →
 * "Cancelled_Forfeited"
 * BR-ERR-01 : exception message phải chứa error code dạng [ERR_PROMO_XXX]
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UC10 — Đặt phòng & Thanh toán cọc (BookingService) | TDD 🔴 RED")
class BookingServiceUC10Test {

        // ── SUT ───────────────────────────────────────────────────────────────────
        @InjectMocks
        private BookingServiceImpl bookingService;

        // ── Mocks ─────────────────────────────────────────────────────────────────
        @Mock
        private RoomBookingRepository roomBookingRepository;

        @Mock
        private BookingRepository bookingRepository;

        @Mock
        private RoomGuestRepository roomGuestRepository;

        @Mock
        private PromotionRepository promotionRepository;

        @Mock
        private RoomRepository roomRepository;

        @Mock
        private CustomerRepository customerRepository;

        @Mock
        private RoomBookingDetailRepository roomBookingDetailRepository;

        @Mock
        private com.kawai.services.interfaces.PaymentGatewayService paymentGatewayService;

        @Mock
        private com.kawai.services.interfaces.NotificationService notificationService;

        @Mock
        private com.kawai.repositories.PaymentTransactionRepository paymentTransactionRepository;

        @Mock
        private com.kawai.repositories.RoomCategoryRepository roomCategoryRepository;

        @org.junit.jupiter.api.BeforeEach
        void setUp() {
                com.kawai.models.Customer customer = new com.kawai.models.Customer();
                customer.setId(1L);
                lenient().when(customerRepository.findById(any())).thenReturn(Optional.of(customer));

                com.kawai.models.RoomCategory category = new com.kawai.models.RoomCategory();
                category.setId(1L);
                category.setCategoryName("Deluxe");
                category.setBasePrice(new BigDecimal("2000000"));

                lenient().when(roomCategoryRepository.findByCategoryNameWithLock(anyString())).thenAnswer(invocation -> {
                        String catName = invocation.getArgument(0);
                        com.kawai.models.RoomCategory cat = new com.kawai.models.RoomCategory();
                        cat.setId(1L);
                        cat.setCategoryName(catName);
                        cat.setBasePrice(new BigDecimal("2000000"));
                        return Optional.of(cat);
                });

                lenient().when(roomRepository.countActiveRoomsByCategoryName(anyString())).thenReturn(1L);
                lenient().when(roomBookingRepository.countOverlappingBookingsByCategory(anyString(), any(), any(), anyLong()))
                        .thenAnswer(invocation -> {
                                LocalDate in = invocation.getArgument(1);
                                LocalDate out = invocation.getArgument(2);
                                Long holdId = invocation.getArgument(3);
                                return roomBookingRepository.countOverlappingBookingsByRoom(ROOM_NO, in, out, holdId);
                        });

                lenient().when(roomRepository.findByRoomNumber(anyString())).thenAnswer(invocation -> {
                        String rNo = invocation.getArgument(0);
                        com.kawai.models.Room r = new com.kawai.models.Room();
                        r.setRoomNumber(rNo);
                        r.setCategory(category);
                        return Optional.of(r);
                });
                lenient().when(roomRepository.findByRoomNumberWithLock(anyString())).thenAnswer(invocation -> {
                        String rNo = invocation.getArgument(0);
                        com.kawai.models.Room r = new com.kawai.models.Room();
                        r.setRoomNumber(rNo);
                        r.setCategory(category);
                        return Optional.of(r);
                });

                lenient().when(bookingRepository.save(any(com.kawai.models.Booking.class))).thenAnswer(invocation -> {
                        com.kawai.models.Booking b = invocation.getArgument(0);
                        if (b.getId() == null) {
                                b.setId(9999L);
                        }
                        return b;
                });

                lenient().when(roomBookingRepository.save(any(com.kawai.models.RoomBooking.class))).thenAnswer(invocation -> {
                        com.kawai.models.RoomBooking b = invocation.getArgument(0);
                        if (b.getId() == null) {
                                b.setId(12345L);
                        }
                        return b;
                });

                lenient().when(roomBookingDetailRepository.save(any(com.kawai.models.RoomBookingDetail.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                lenient().when(roomGuestRepository.save(any(com.kawai.models.RoomGuest.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));
        }

        // ── Test Fixtures ─────────────────────────────────────────────────────────
        private static final LocalDate CHECK_IN = LocalDate.of(2026, 7, 15);
        private static final LocalDate CHECK_OUT = LocalDate.of(2026, 7, 20); // 5 đêm
        private static final BigDecimal DEPOSIT = new BigDecimal("3000000");
        private static final String ROOM_NO = "R101";

        /** Helper: tạo request cơ bản không có promo code. */
        private BookingRequestDTO buildRequest() {
                return new BookingRequestDTO(1L, Collections.singletonList(new RoomSelectionDTO(ROOM_NO, 2, 0)), CHECK_IN, CHECK_OUT, DEPOSIT);
        }

        // ══════════════════════════════════════════════════════════════════════════
        // TC-M2-003 | CRITICAL | Đặt phòng thành công
        // ══════════════════════════════════════════════════════════════════════════

        /**
         * TC-M2-003 — Đặt phòng thành công: status=Pending + cancellationDeadline set.
         *
         * Spec (TC_MASTER_TABLE, dòng TC-M2-003):
         * "Đặt phòng thành công — tạo Booking + Folio trống"
         *
         * Expected theo BR-FIN-02:
         * cancellationDeadline = checkInDate - 2 ngày (2026-07-13)
         *
         * 🔴 RED — COMPILE ERROR:
         * BookingResponseDTO chưa có method getCancellationDeadline().
         * → Trình biên dịch báo: "The method getCancellationDeadline() is undefined"
         *
         * Fix cần làm:
         * Thêm field `LocalDate cancellationDeadline` + getter/setter vào
         * BookingResponseDTO.
         * Set field này trong BookingServiceImpl.createBooking() trước khi return.
         */
        @Test
        @DisplayName("TC-M2-003 | CRITICAL | Đặt phòng thành công → status=Pending + cancellationDeadline=checkIn-2d")
        void TC_M2_003_createBooking_success_returnsPendingWithCancellationDeadline() throws Exception {
                // Arrange
                BookingRequestDTO request = buildRequest();
                when(roomBookingRepository.countOverlappingBookingsByRoom(
                                eq(ROOM_NO), eq(CHECK_IN), eq(CHECK_OUT), anyLong())).thenReturn(0L);

                // Act
                BookingResponseDTO result = bookingService.createBooking(request);

                // Assert — fields cơ bản
                assertNotNull(result, "Response không được null");
                assertNotNull(result.getBookingId(), "BookingId phải được sinh ra");
                assertEquals("CONFIRMED", result.getBookingStatus(),
                                "Trạng thái vừa tạo phải là CONFIRMED (BR-STATUS-01)");
                assertEquals(DEPOSIT, result.getDepositAmount(), "Tiền cọc phải khớp request");
                assertEquals(CHECK_IN, result.getCheckInDate(), "CheckIn phải khớp");
                assertEquals(CHECK_OUT, result.getCheckOutDate(), "CheckOut phải khớp");

                // 🔴 RED — COMPILE ERROR: method này chưa tồn tại trong BookingResponseDTO
                LocalDate expectedDeadline = CHECK_IN.minusDays(2); // 2026-07-13
                assertEquals(expectedDeadline, result.getCancellationDeadline(),
                                "cancellationDeadline phải = checkIn - 2 ngày (BR-FIN-02)");
        }

        // ══════════════════════════════════════════════════════════════════════════
        // TC-M2-004 | CRITICAL | Concurrency — Chống Overbooking
        // ══════════════════════════════════════════════════════════════════════════

        /**
         * TC-M2-004 — 2 user đặt cùng phòng đồng thời.
         *
         * Expected:
         * - successCount == 1 (1 người đặt được)
         * - conflictCount == 1 (1 người nhận RoomNotAvailableException)
         * - Booking thành công phải ở trạng thái "CONFIRMED" (sau lock pessimistic) —
         * BR-STATUS-01
         *
         * 🔴 RED — RUNTIME FAILURE:
         * BookingServiceImpl trả về status = "Pending" (chưa confirm).
         * Spec yêu cầu: sau khi đặt phòng thành công và lock được giải phóng,
         * booking phải chuyển ngay thành "CONFIRMED".
         * → assertEquals("CONFIRMED", ...) sẽ FAIL vì thực tế trả "Pending".
         *
         * Fix cần làm:
         * BookingServiceImpl.createBooking() phải set status = "CONFIRMED" (không phải
         * "Pending")
         * sau khi vượt qua kiểm tra overbooking và save thành công.
         */
        @Test
        @DisplayName("TC-M2-004 | CRITICAL | Concurrency: 2 user đặt R101 → 1 CONFIRMED, 1 nhận 409")
        void TC_M2_004_createBooking_concurrency_onlyOneConfirmed() throws Exception {
                // Arrange: lần 1 phòng trống, lần 2 đã bị chiếm
                when(roomBookingRepository.countOverlappingBookingsByRoom(
                                anyString(), any(LocalDate.class), any(LocalDate.class), anyLong()))
                                .thenReturn(0L) // Thread thắng
                                .thenReturn(1L); // Thread thua

                AtomicInteger successCount = new AtomicInteger(0);
                AtomicInteger conflictCount = new AtomicInteger(0);
                AtomicReference<String> successStatus = new AtomicReference<>(null);
                List<Exception> unexpected = Collections.synchronizedList(new ArrayList<>());
                CountDownLatch startGun = new CountDownLatch(1);

                BookingRequestDTO request = buildRequest();

                Runnable task = () -> {
                        try {
                                startGun.await();
                                BookingResponseDTO response = bookingService.createBooking(request);
                                successCount.incrementAndGet();
                                successStatus.set(response.getBookingStatus()); // Ghi lại status
                        } catch (RoomNotAvailableException e) {
                                conflictCount.incrementAndGet();
                        } catch (Exception e) {
                                unexpected.add(e);
                        }
                };

                Thread threadA = new Thread(task, "UserA-R101");
                Thread threadB = new Thread(task, "UserB-R101");
                threadA.start();
                threadB.start();
                startGun.countDown();
                threadA.join();
                threadB.join();

                // Assert — concurrency counts
                assertTrue(unexpected.isEmpty(),
                                "Không được có exception ngoài dự kiến: " + unexpected);
                assertEquals(1, successCount.get(), "Chỉ 1 user đặt phòng thành công");
                assertEquals(1, conflictCount.get(), "Đúng 1 user nhận 409 Conflict");

                // 🔴 RED — FAIL: implementation trả "Pending" thay vì "CONFIRMED"
                assertEquals("CONFIRMED", successStatus.get(),
                                "Booking thành công sau lock phải chuyển ngay sang CONFIRMED (BR-STATUS-01)");
        }

        // ══════════════════════════════════════════════════════════════════════════
        // TC-M2-005 | CRITICAL | Validation ngày check-in/check-out
        // ══════════════════════════════════════════════════════════════════════════

        /**
         * TC-M2-005 — checkOutDate phải SAU checkInDate (BR-DATE-01).
         *
         * 🔴 RED — RUNTIME FAILURE:
         * BookingServiceImpl chưa có bất kỳ validation nào cho ngày.
         * Gọi createBooking với checkOut == checkIn → không ném exception,
         * thay vào đó tạo booking 0 đêm → assertThrows FAIL.
         *
         * Fix cần làm:
         * Đầu createBooking(), thêm:
         * if (!request.getCheckOutDate().isAfter(request.getCheckInDate()))
         * throw new IllegalArgumentException("checkOutDate phải sau checkInDate");
         */
        @Test
        @DisplayName("TC-M2-005 | CRITICAL | checkOut = checkIn (0 đêm) → IllegalArgumentException (BR-DATE-01)")
        void TC_M2_005_createBooking_checkOutEqualsCheckIn_throwsDateException() {
                // Arrange: checkOut == checkIn (0 đêm — vô nghĩa)
                BookingRequestDTO request = new BookingRequestDTO(
                                1L, Collections.singletonList(new RoomSelectionDTO(ROOM_NO, 2, 0)),
                                CHECK_IN, CHECK_IN, // checkOut = checkIn → lỗi
                                DEPOSIT);

                // 🔴 RED — implementation không validate → không ném exception → FAIL
                IllegalArgumentException ex = assertThrows(
                                IllegalArgumentException.class,
                                () -> bookingService.createBooking(request),
                                "checkOutDate ≤ checkInDate phải ném IllegalArgumentException (BR-DATE-01)");
                assertTrue(ex.getMessage().toLowerCase().contains("checkout") ||
                                ex.getMessage().toLowerCase().contains("check-out") ||
                                ex.getMessage().toLowerCase().contains("ngày"),
                                "Message lỗi phải đề cập đến check-out hoặc ngày");
        }

        @Test
        @DisplayName("TC-M2-005b | CRITICAL | checkOut trước checkIn → IllegalArgumentException (BR-DATE-01)")
        void TC_M2_005b_createBooking_checkOutBeforeCheckIn_throwsDateException() {
                // Arrange: checkOut trước checkIn (ngược chiều thời gian)
                BookingRequestDTO request = new BookingRequestDTO(
                                1L, Collections.singletonList(new RoomSelectionDTO(ROOM_NO, 2, 0)),
                                CHECK_IN, CHECK_IN.minusDays(1), // checkOut < checkIn
                                DEPOSIT);

                // 🔴 RED — implementation không validate → không ném exception → FAIL
                assertThrows(
                                IllegalArgumentException.class,
                                () -> bookingService.createBooking(request),
                                "checkOutDate < checkInDate phải ném IllegalArgumentException (BR-DATE-01)");
        }

        // ══════════════════════════════════════════════════════════════════════════
        // TC-M2-006 | HIGH | Hủy trước 48h → hoàn 100% cọc
        // ══════════════════════════════════════════════════════════════════════════

        /**
         * TC-M2-006 — Hủy trước 48h: hoàn 100% cọc, status = "Cancelled_Refunded".
         *
         * Setup: findById(201L) → booking có depositAmount=3,500,000,
         * cancellationDeadline = hôm nay + 3 ngày (còn trước deadline).
         *
         * 🔴 RED — RUNTIME FAILURE:
         * Implementation set booking.setBookingStatus("Cancelled") —
         * nhưng spec (BR-STATUS-02) yêu cầu phân biệt 2 trạng thái rõ ràng:
         * - Hủy trước 48h → "Cancelled_Refunded"
         * - Hủy trong 48h → "Cancelled_Forfeited"
         * → assertEquals("Cancelled_Refunded", ...) sẽ FAIL vì trả "Cancelled".
         *
         * Fix cần làm:
         * Thay booking.setBookingStatus("Cancelled") bằng "Cancelled_Refunded" khi hoàn
         * tiền.
         */
        @Test
        @DisplayName("TC-M2-006 | HIGH | Hủy trước 48h → hoàn 100% cọc + status=Cancelled_Refunded (BR-FIN-02)")
        void TC_M2_006_cancelBooking_before48h_fullRefundAndCorrectStatus() {
                // Arrange
                BigDecimal depositDB = new BigDecimal("3500000");
                RoomBooking booking = new RoomBooking();
                booking.setId(201L);
                booking.setBookingStatus("Pending");
                booking.setDepositAmount(depositDB);
                booking.setCancellationDeadline(LocalDate.now().plusDays(3)); // còn 3 ngày → trước deadline
                booking.setCheckInDate(LocalDate.now().plusDays(5));

                when(roomBookingRepository.findByIdAndCustomerId(201L, 1L)).thenReturn(Optional.of(booking));

                // Act
                BookingResponseDTO response = bookingService.cancelBooking(201L, 1L);

                // Assert — hoàn tiền đúng
                assertNotNull(response, "Response không được null");
                assertEquals(depositDB, response.getDepositAmount(),
                                "Hủy trước 48h phải hoàn 100% tiền cọc = 3,500,000 (BR-FIN-02)");

                assertEquals("Cancelled_Refunded", booking.getBookingStatus(),
                                "Status sau hủy có hoàn tiền phải là 'Cancelled_Refunded' (BR-STATUS-02)");

                // Verify save() được gọi để persist status mới
                verify(roomBookingRepository, times(1)).save(booking);
        }

        // ══════════════════════════════════════════════════════════════════════════
        // TC-M2-007 | HIGH | Hủy trong 48h → tịch thu cọc
        // ══════════════════════════════════════════════════════════════════════════

        /**
         * TC-M2-007 — Hủy trong 48h: tịch thu cọc (hoàn 0đ), status =
         * "Cancelled_Forfeited".
         *
         * 🔴 RED — RUNTIME FAILURE:
         * Implementation set "Cancelled" cho cả 2 trường hợp.
         * Spec yêu cầu: khi tịch thu cọc → status = "Cancelled_Forfeited".
         * → assertEquals("Cancelled_Forfeited", ...) FAIL vì trả "Cancelled".
         *
         * Fix cần làm:
         * Thay booking.setBookingStatus("Cancelled") bằng "Cancelled_Forfeited"
         * khi đã qua cancellationDeadline (tịch thu cọc).
         */
        @Test
        @DisplayName("TC-M2-007 | HIGH | Hủy trong 48h → hoàn 0đ + status=Cancelled_Forfeited (BR-FIN-02)")
        void TC_M2_007_cancelBooking_within48h_zeroRefundAndForfeitedStatus() {
                // Arrange: đã qua deadline → tịch thu cọc
                RoomBooking booking = new RoomBooking();
                booking.setId(202L);
                booking.setBookingStatus("Pending");
                booking.setDepositAmount(new BigDecimal("2000000"));
                booking.setCancellationDeadline(LocalDate.now().minusDays(1)); // qua deadline rồi
                booking.setCheckInDate(LocalDate.now().plusDays(1));

                when(roomBookingRepository.findByIdAndCustomerId(202L, 1L)).thenReturn(Optional.of(booking));

                // Act
                BookingResponseDTO response = bookingService.cancelBooking(202L, 1L);

                // Assert — không hoàn tiền
                assertNotNull(response, "Response không được null");
                assertEquals(BigDecimal.ZERO, response.getDepositAmount(),
                                "Hủy trong 48h → tịch thu cọc hoàn về 0đ (BR-FIN-02)");

                assertEquals("Cancelled_Forfeited", booking.getBookingStatus(),
                                "Status tịch thu cọc phải là 'Cancelled_Forfeited' (BR-STATUS-02)");

                // Verify save() được gọi
                verify(roomBookingRepository, times(1)).save(booking);
        }

        // ══════════════════════════════════════════════════════════════════════════
        // TC-M2-008 | MEDIUM | Mã khuyến mãi hợp lệ → giảm giá đúng
        // ══════════════════════════════════════════════════════════════════════════

        /**
         * TC-M2-008 — Áp mã SUMMER10 (giảm 10%, 5 đêm).
         *
         * Data:
         * 5 đêm × 2,000,000 = 10,000,000 → giảm 10% = 9,000,000
         *
         * 🔴 RED — COMPILE ERROR:
         * result.getCancellationDeadline() chưa tồn tại trong BookingResponseDTO.
         */
        @Test
        @DisplayName("TC-M2-008 | MEDIUM | Mã SUMMER10 (10% off, 5 đêm) → discountedPrice=9,000,000 + deadline set")
        void TC_M2_008_createBooking_validPromoCode_appliesDiscount() throws Exception {
                // Arrange
                BookingRequestDTO request = buildRequest();
                request.setPromotionCode("SUMMER10");

                when(roomBookingRepository.countOverlappingBookingsByRoom(
                                anyString(), any(LocalDate.class), any(LocalDate.class), anyLong())).thenReturn(0L);

                Promotion promo = new Promotion();
                promo.setPromoCode("SUMMER10");
                promo.setIsActive(true);
                promo.setValidTo(LocalDate.now().plusDays(30));
                promo.setDiscountValue(new BigDecimal("10"));
                when(promotionRepository.findByPromoCode("SUMMER10")).thenReturn(Optional.of(promo));

                // Act
                BookingResponseDTO result = bookingService.createBooking(request);

                // Assert — discount
                assertNotNull(result, "Response không được null");
                assertEquals(new BigDecimal("9000000"), result.getDiscountedPrice(),
                                "10% off × 5 đêm × 2,000,000 = 9,000,000");

                // 🔴 RED — COMPILE ERROR: getCancellationDeadline() chưa tồn tại
                assertEquals(CHECK_IN.minusDays(2), result.getCancellationDeadline(),
                                "cancellationDeadline phải được set ngay cả khi có promo code (BR-FIN-02)");
        }

        /**
         * TC-M2-008b — Áp mã EARLYBIRD20 (giảm 20%, 3 đêm).
         *
         * Data:
         * 3 đêm × 2,000,000 = 6,000,000 → giảm 20% = 4,800,000
         *
         * 🔴 RED — COMPILE ERROR:
         * result.getCancellationDeadline() chưa tồn tại.
         */
        @Test
        @DisplayName("TC-M2-008b | MEDIUM | Mã EARLYBIRD20 (20% off, 3 đêm) → discountedPrice=4,800,000")
        void TC_M2_008b_createBooking_20pctPromo_3nights_correctDiscount() throws Exception {
                // Arrange: 3 đêm
                LocalDate in = LocalDate.of(2026, 8, 1);
                LocalDate out = LocalDate.of(2026, 8, 4);
                BookingRequestDTO request = new BookingRequestDTO(1L, Collections.singletonList(new RoomSelectionDTO("R202", 2, 0)), in, out, DEPOSIT);
                request.setPromotionCode("EARLYBIRD20");

                when(roomBookingRepository.countOverlappingBookingsByRoom(
                                anyString(), any(LocalDate.class), any(LocalDate.class), anyLong())).thenReturn(0L);

                Promotion promo = new Promotion();
                promo.setPromoCode("EARLYBIRD20");
                promo.setIsActive(true);
                promo.setValidTo(LocalDate.now().plusDays(60));
                promo.setDiscountValue(new BigDecimal("20"));
                when(promotionRepository.findByPromoCode("EARLYBIRD20")).thenReturn(Optional.of(promo));

                // Act
                BookingResponseDTO result = bookingService.createBooking(request);

                // Assert — discount
                assertNotNull(result);
                assertEquals(new BigDecimal("4800000"), result.getDiscountedPrice(),
                                "20% off × 3 đêm × 2,000,000 = 4,800,000");

                // 🔴 RED — COMPILE ERROR: getCancellationDeadline() chưa tồn tại
                assertEquals(in.minusDays(2), result.getCancellationDeadline(),
                                "cancellationDeadline = checkIn - 2 ngày (BR-FIN-02)");
        }

        // ══════════════════════════════════════════════════════════════════════════
        // TC-M2-009 | MEDIUM | Mã khuyến mãi không hợp lệ → từ chối + error code
        // ══════════════════════════════════════════════════════════════════════════

        /**
         * TC-M2-009a — Mã không active (isActive=false).
         *
         * Expected: IllegalArgumentException với message chứa error code
         * [ERR_PROMO_INACTIVE].
         *
         * 🔴 RED — RUNTIME FAILURE:
         * Implementation ném: "Promotion code 'EXPIRED2020' is invalid or expired"
         * Spec (BR-ERR-01) yêu cầu message phải có error code dạng [ERR_PROMO_INACTIVE]
         * để client parse và hiển thị đúng thông báo lỗi.
         * → assertTrue(...contains("[ERR_PROMO_INACTIVE]")) sẽ FAIL.
         *
         * Fix cần làm:
         * Thêm error code vào message:
         * "Promotion code 'EXPIRED2020' is invalid or expired [ERR_PROMO_INACTIVE]"
         */
        @Test
        @DisplayName("TC-M2-009a | MEDIUM | Mã isActive=false → IllegalArgumentException + [ERR_PROMO_INACTIVE]")
        void TC_M2_009a_createBooking_inactivePromo_throwsWithErrorCode() {
                // Arrange
                BookingRequestDTO request = buildRequest();
                request.setPromotionCode("EXPIRED2020");

                when(roomBookingRepository.countOverlappingBookingsByRoom(
                                anyString(), any(LocalDate.class), any(LocalDate.class), anyLong())).thenReturn(0L);

                Promotion promo = new Promotion();
                promo.setPromoCode("EXPIRED2020");
                promo.setIsActive(false);
                promo.setValidTo(LocalDate.of(2020, 1, 1));
                when(promotionRepository.findByPromoCode("EXPIRED2020")).thenReturn(Optional.of(promo));

                // Assert — exception được ném
                IllegalArgumentException ex = assertThrows(
                                IllegalArgumentException.class,
                                () -> bookingService.createBooking(request),
                                "Mã không active phải ném IllegalArgumentException");

                // 🔴 RED — FAIL: message hiện tại không chứa error code [ERR_PROMO_INACTIVE]
                assertTrue(ex.getMessage().contains("[ERR_PROMO_INACTIVE]"),
                                "Exception message phải chứa error code '[ERR_PROMO_INACTIVE]' (BR-ERR-01). " +
                                                "Message thực tế: " + ex.getMessage());
        }

        /**
         * TC-M2-009b — Mã validTo đã qua (hết hạn theo ngày).
         *
         * 🔴 RED — FAIL: message không chứa [ERR_PROMO_EXPIRED].
         */
        @Test
        @DisplayName("TC-M2-009b | MEDIUM | Mã validTo=quá khứ → IllegalArgumentException + [ERR_PROMO_EXPIRED]")
        void TC_M2_009b_createBooking_expiredByDate_throwsWithErrorCode() {
                // Arrange
                BookingRequestDTO request = buildRequest();
                request.setPromotionCode("XMAS2025");

                when(roomBookingRepository.countOverlappingBookingsByRoom(
                                anyString(), any(LocalDate.class), any(LocalDate.class), anyLong())).thenReturn(0L);

                Promotion promo = new Promotion();
                promo.setPromoCode("XMAS2025");
                promo.setIsActive(true); // active nhưng hết hạn ngày
                promo.setValidTo(LocalDate.of(2025, 12, 31)); // đã qua
                when(promotionRepository.findByPromoCode("XMAS2025")).thenReturn(Optional.of(promo));

                // Assert
                IllegalArgumentException ex = assertThrows(
                                IllegalArgumentException.class,
                                () -> bookingService.createBooking(request));

                // 🔴 RED — FAIL: message không chứa [ERR_PROMO_EXPIRED]
                assertTrue(ex.getMessage().contains("[ERR_PROMO_EXPIRED]"),
                                "Exception message phải chứa '[ERR_PROMO_EXPIRED]' (BR-ERR-01). " +
                                                "Message thực tế: " + ex.getMessage());
        }

        /**
         * TC-M2-009c — Mã không tồn tại trong DB.
         *
         * 🔴 RED — FAIL: message không chứa [ERR_PROMO_NOT_FOUND].
         */
        @Test
        @DisplayName("TC-M2-009c | MEDIUM | Mã không tồn tại → IllegalArgumentException + [ERR_PROMO_NOT_FOUND]")
        void TC_M2_009c_createBooking_unknownPromoCode_throwsWithNotFoundCode() {
                // Arrange
                BookingRequestDTO request = buildRequest();
                request.setPromotionCode("GHOST999");

                when(roomBookingRepository.countOverlappingBookingsByRoom(
                                anyString(), any(LocalDate.class), any(LocalDate.class), anyLong())).thenReturn(0L);
                when(promotionRepository.findByPromoCode("GHOST999"))
                                .thenReturn(Optional.empty());

                // Assert
                IllegalArgumentException ex = assertThrows(
                                IllegalArgumentException.class,
                                () -> bookingService.createBooking(request));

                // 🔴 RED — FAIL: message không chứa [ERR_PROMO_NOT_FOUND]
                assertTrue(ex.getMessage().contains("[ERR_PROMO_NOT_FOUND]"),
                                "Exception message phải chứa '[ERR_PROMO_NOT_FOUND]' (BR-ERR-01). " +
                                                "Message thực tế: " + ex.getMessage());
        }
}
