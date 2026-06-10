package com.kawai.services;

import com.kawai.dto.TourSearchResult;
import com.kawai.dto.WeatherInfo;
import com.kawai.models.Tour;
import com.kawai.models.TourSchedule;
import com.kawai.repositories.TourScheduleRepository;
import com.kawai.services.impl.TourServiceImpl;
import com.kawai.services.interfaces.WeatherApiClient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * ================================================================
 * KAWAI RESORT — TDD Unit Test cho TourService (UC19)
 * ================================================================
 * 
 * Test Case tham chiếu:
 *   - TC-M4-001 (UC19): Tìm kiếm tour — trả danh sách tour khả dụng + thông tin thời tiết
 *   - TC-M4-002 (UC19): API thời tiết không phản hồi → vẫn trả tour, ẩn thông tin thời tiết
 * 
 * Business Rules liên quan:
 *   - Danh sách tour chỉ hiển thị lịch trình có trạng thái "Open"
 *   - Thông tin thời tiết được tích hợp từ OpenWeather API
 *   - Graceful degradation: Nếu API thời tiết lỗi, tour vẫn hiển thị bình thường
 * 
 * TDD Phase: 🔴 RED — Test được viết TRƯỚC khi implement production code.
 *            Service hiện tại ném UnsupportedOperationException.
 * 
 * @see com.kawai.services.interfaces.TourService
 * @see com.kawai.services.impl.TourServiceImpl
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UC19 — Tìm kiếm gói tour (TourService)")
class TourServiceTest {

    @Mock
    private TourScheduleRepository tourScheduleRepository;

    @Mock
    private WeatherApiClient weatherApiClient;

    @InjectMocks
    private TourServiceImpl tourService;

    // ===== Test Data Fixtures =====

    private Tour sampleTour;
    private TourSchedule sampleSchedule;
    private LocalDate searchFromDate;
    private LocalDate searchToDate;

    @BeforeEach
    void setUp() {
        // Tạo Tour mẫu
        sampleTour = new Tour();
        sampleTour.setId(1L);
        sampleTour.setTourName("Vịnh Hạ Long - 1 Ngày");
        sampleTour.setTourType("DAY_TRIP");
        sampleTour.setBasePrice(new BigDecimal("1500000"));
        sampleTour.setMaxCapacity(30);

        // Tạo TourSchedule mẫu
        sampleSchedule = new TourSchedule();
        sampleSchedule.setId(100L);
        sampleSchedule.setTour(sampleTour);
        sampleSchedule.setDepartureDate(LocalDate.of(2026, 6, 20));
        sampleSchedule.setDepartureTime(LocalTime.of(9, 0));
        sampleSchedule.setBookedSeats(15); // capacity 30 - 15 = 15 available slots
        sampleSchedule.setScheduleStatus("Open");

        // Khoảng ngày tìm kiếm
        searchFromDate = LocalDate.of(2026, 6, 15);
        searchToDate = LocalDate.of(2026, 6, 25);
    }

    // ================================================================
    // TC-M4-001: Tìm kiếm tour — trả danh sách tour khả dụng + thông tin thời tiết
    // ================================================================
    @Nested
    @DisplayName("TC-M4-001: Tìm kiếm tour có kết quả + thời tiết")
    class TC_M4_001 {

        @Test
        @DisplayName("TC-M4-001.1: Trả về danh sách tour khả dụng khi có lịch trình Open trong khoảng ngày")
        void searchAvailableTours_WithOpenSchedules_ShouldReturnTourList() {
            // ARRANGE: Mock repository trả về 1 lịch trình tour Open
            when(tourScheduleRepository.findByDepartureDateBetweenAndScheduleStatus(
                    eq(searchFromDate), eq(searchToDate), eq("Open")))
                    .thenReturn(List.of(sampleSchedule));

            // Mock weather API trả về thời tiết bình thường
            WeatherInfo weatherInfo = new WeatherInfo("Sunny", 32.5);
            when(weatherApiClient.getWeatherForDate(sampleSchedule.getDepartureDate()))
                    .thenReturn(weatherInfo);

            // ACT
            List<TourSearchResult> results = tourService.searchAvailableTours(searchFromDate, searchToDate);

            // ASSERT
            assertNotNull(results, "Kết quả tìm kiếm không được null");
            assertEquals(1, results.size(), "Phải trả về đúng 1 tour khả dụng");

            TourSearchResult result = results.get(0);
            assertEquals(1L, result.getTourId(), "Tour ID phải khớp");
            assertEquals("Vịnh Hạ Long - 1 Ngày", result.getTourName(), "Tên tour phải khớp");
            assertEquals("DAY_TRIP", result.getTourType(), "Loại tour phải khớp");
            assertEquals(new BigDecimal("1500000"), result.getBasePrice(), "Giá tour phải khớp");
            assertEquals(30, result.getMaxCapacity(), "Sức chứa tối đa phải khớp");
            assertEquals(15, result.getAvailableSlots(), "Số chỗ trống phải khớp");
            assertEquals(LocalDate.of(2026, 6, 20), result.getDepartureDate(), "Ngày khởi hành phải khớp");
            assertEquals("Open", result.getScheduleStatus(), "Trạng thái lịch trình phải là Open");

            // Verify thông tin thời tiết được gắn kèm
            assertTrue(result.isWeatherAvailable(), "Thời tiết phải available khi API phản hồi thành công");
            assertEquals("Sunny", result.getWeatherDescription(), "Mô tả thời tiết phải khớp");
            assertEquals(32.5, result.getTemperature(), "Nhiệt độ phải khớp");

            // Verify interactions
            verify(tourScheduleRepository).findByDepartureDateBetweenAndScheduleStatus(
                    searchFromDate, searchToDate, "Open");
            verify(weatherApiClient).getWeatherForDate(sampleSchedule.getDepartureDate());
        }

        @Test
        @DisplayName("TC-M4-001.2: Trả về danh sách rỗng khi không có tour nào trong khoảng ngày")
        void searchAvailableTours_NoSchedulesFound_ShouldReturnEmptyList() {
            // ARRANGE: Repository trả về danh sách rỗng
            when(tourScheduleRepository.findByDepartureDateBetweenAndScheduleStatus(
                    eq(searchFromDate), eq(searchToDate), eq("Open")))
                    .thenReturn(Collections.emptyList());

            // ACT
            List<TourSearchResult> results = tourService.searchAvailableTours(searchFromDate, searchToDate);

            // ASSERT
            assertNotNull(results, "Kết quả không được null, phải trả danh sách rỗng");
            assertTrue(results.isEmpty(), "Danh sách phải rỗng khi không có tour khả dụng");

            // Verify weather API KHÔNG được gọi khi không có tour
            verify(weatherApiClient, never()).getWeatherForDate(any());
        }

        @Test
        @DisplayName("TC-M4-001.3: Trả về nhiều tour khi có nhiều lịch trình Open cùng khoảng ngày")
        void searchAvailableTours_MultipleSchedules_ShouldReturnAllTours() {
            // ARRANGE: Tạo tour thứ 2
            Tour secondTour = new Tour();
            secondTour.setId(2L);
            secondTour.setTourName("Tràng An - Ninh Bình");
            secondTour.setTourType("FULL_DAY");
            secondTour.setBasePrice(new BigDecimal("2000000"));
            secondTour.setMaxCapacity(20);

            TourSchedule secondSchedule = new TourSchedule();
            secondSchedule.setId(101L);
            secondSchedule.setTour(secondTour);
            secondSchedule.setDepartureDate(LocalDate.of(2026, 6, 22));
            secondSchedule.setDepartureTime(LocalTime.of(10, 0));
            secondSchedule.setBookedSeats(12); // capacity 20 - 12 = 8 available slots
            secondSchedule.setScheduleStatus("Open");

            when(tourScheduleRepository.findByDepartureDateBetweenAndScheduleStatus(
                    eq(searchFromDate), eq(searchToDate), eq("Open")))
                    .thenReturn(List.of(sampleSchedule, secondSchedule));

            // Mock weather cho cả 2 ngày
            when(weatherApiClient.getWeatherForDate(LocalDate.of(2026, 6, 20)))
                    .thenReturn(new WeatherInfo("Sunny", 32.0));
            when(weatherApiClient.getWeatherForDate(LocalDate.of(2026, 6, 22)))
                    .thenReturn(new WeatherInfo("Cloudy", 28.0));

            // ACT
            List<TourSearchResult> results = tourService.searchAvailableTours(searchFromDate, searchToDate);

            // ASSERT
            assertNotNull(results);
            assertEquals(2, results.size(), "Phải trả về đúng 2 tour khả dụng");

            // Kiểm tra tour đầu tiên
            assertEquals("Vịnh Hạ Long - 1 Ngày", results.get(0).getTourName());
            assertTrue(results.get(0).isWeatherAvailable());
            assertEquals("Sunny", results.get(0).getWeatherDescription());

            // Kiểm tra tour thứ hai
            assertEquals("Tràng An - Ninh Bình", results.get(1).getTourName());
            assertTrue(results.get(1).isWeatherAvailable());
            assertEquals("Cloudy", results.get(1).getWeatherDescription());
            assertEquals(8, results.get(1).getAvailableSlots());

            // Verify weather API được gọi đúng 2 lần
            verify(weatherApiClient, times(2)).getWeatherForDate(any());
        }
    }

    // ================================================================
    // TC-M4-002: API thời tiết không phản hồi → vẫn trả tour, ẩn thời tiết
    // ================================================================
    @Nested
    @DisplayName("TC-M4-002: API thời tiết lỗi — Graceful Degradation")
    class TC_M4_002 {

        @Test
        @DisplayName("TC-M4-002.1: Khi API thời tiết throw RuntimeException → vẫn trả tour, weatherAvailable = false")
        void searchAvailableTours_WeatherApiThrowsException_ShouldReturnToursWithoutWeather() {
            // ARRANGE: Repository trả về tour bình thường
            when(tourScheduleRepository.findByDepartureDateBetweenAndScheduleStatus(
                    eq(searchFromDate), eq(searchToDate), eq("Open")))
                    .thenReturn(List.of(sampleSchedule));

            // Mock weather API ném exception (API không khả dụng)
            when(weatherApiClient.getWeatherForDate(any()))
                    .thenThrow(new RuntimeException("OpenWeather API connection timeout"));

            // ACT — KHÔNG được ném exception ra ngoài
            List<TourSearchResult> results = tourService.searchAvailableTours(searchFromDate, searchToDate);

            // ASSERT
            assertNotNull(results, "Kết quả không được null dù API thời tiết lỗi");
            assertEquals(1, results.size(), "Vẫn phải trả về tour dù thời tiết không khả dụng");

            TourSearchResult result = results.get(0);

            // Thông tin tour vẫn đầy đủ
            assertEquals(1L, result.getTourId());
            assertEquals("Vịnh Hạ Long - 1 Ngày", result.getTourName());
            assertEquals(new BigDecimal("1500000"), result.getBasePrice());
            assertEquals(15, result.getAvailableSlots());

            // Thông tin thời tiết bị ẩn
            assertFalse(result.isWeatherAvailable(),
                    "weatherAvailable phải = false khi API thời tiết lỗi");
            assertNull(result.getWeatherDescription(),
                    "weatherDescription phải null khi API không phản hồi");
            assertNull(result.getTemperature(),
                    "temperature phải null khi API không phản hồi");
        }

        @Test
        @DisplayName("TC-M4-002.2: Khi API thời tiết trả null → vẫn trả tour, weatherAvailable = false")
        void searchAvailableTours_WeatherApiReturnsNull_ShouldReturnToursWithoutWeather() {
            // ARRANGE
            when(tourScheduleRepository.findByDepartureDateBetweenAndScheduleStatus(
                    eq(searchFromDate), eq(searchToDate), eq("Open")))
                    .thenReturn(List.of(sampleSchedule));

            // Mock weather API trả null (dịch vụ không khả dụng - SYS-002)
            when(weatherApiClient.getWeatherForDate(any()))
                    .thenReturn(null);

            // ACT
            List<TourSearchResult> results = tourService.searchAvailableTours(searchFromDate, searchToDate);

            // ASSERT
            assertNotNull(results);
            assertEquals(1, results.size());

            TourSearchResult result = results.get(0);
            assertFalse(result.isWeatherAvailable(),
                    "weatherAvailable phải = false khi API trả null");
            assertNull(result.getWeatherDescription());
            assertNull(result.getTemperature());

            // Thông tin tour vẫn nguyên vẹn
            assertEquals("Vịnh Hạ Long - 1 Ngày", result.getTourName());
            assertEquals(15, result.getAvailableSlots());
        }

        @Test
        @DisplayName("TC-M4-002.3: Khi 1 trong 2 tour lỗi thời tiết → tour đó ẩn thời tiết, tour kia vẫn hiển thị")
        void searchAvailableTours_PartialWeatherFailure_ShouldHandlePerTour() {
            // ARRANGE: 2 lịch trình tour
            Tour secondTour = new Tour();
            secondTour.setId(2L);
            secondTour.setTourName("Cát Bà Island");
            secondTour.setTourType("OVERNIGHT");
            secondTour.setBasePrice(new BigDecimal("3500000"));
            secondTour.setMaxCapacity(25);

            TourSchedule secondSchedule = new TourSchedule();
            secondSchedule.setId(102L);
            secondSchedule.setTour(secondTour);
            secondSchedule.setDepartureDate(LocalDate.of(2026, 6, 23));
            secondSchedule.setDepartureTime(LocalTime.of(14, 0));
            secondSchedule.setBookedSeats(15); // capacity 25 - 15 = 10 available slots
            secondSchedule.setScheduleStatus("Open");

            when(tourScheduleRepository.findByDepartureDateBetweenAndScheduleStatus(
                    eq(searchFromDate), eq(searchToDate), eq("Open")))
                    .thenReturn(List.of(sampleSchedule, secondSchedule));

            // Tour 1: Weather thành công
            when(weatherApiClient.getWeatherForDate(LocalDate.of(2026, 6, 20)))
                    .thenReturn(new WeatherInfo("Rainy", 25.0));
            // Tour 2: Weather lỗi
            when(weatherApiClient.getWeatherForDate(LocalDate.of(2026, 6, 23)))
                    .thenThrow(new RuntimeException("Service Unavailable"));

            // ACT
            List<TourSearchResult> results = tourService.searchAvailableTours(searchFromDate, searchToDate);

            // ASSERT
            assertEquals(2, results.size(), "Cả 2 tour đều phải được trả về");

            // Tour 1 — có thời tiết
            TourSearchResult tour1 = results.get(0);
            assertTrue(tour1.isWeatherAvailable(), "Tour 1 phải có thời tiết");
            assertEquals("Rainy", tour1.getWeatherDescription());
            assertEquals(25.0, tour1.getTemperature());

            // Tour 2 — không có thời tiết
            TourSearchResult tour2 = results.get(1);
            assertFalse(tour2.isWeatherAvailable(), "Tour 2 phải ẩn thời tiết do API lỗi");
            assertNull(tour2.getWeatherDescription());
            assertNull(tour2.getTemperature());

            // Thông tin tour 2 vẫn đầy đủ
            assertEquals("Cát Bà Island", tour2.getTourName());
            assertEquals(10, tour2.getAvailableSlots());
        }
    }
}
