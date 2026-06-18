package com.kawai.services;

import com.kawai.models.Tour;
import com.kawai.models.TourItinerary;
import com.kawai.models.TourSchedule;
import com.kawai.repositories.TourItineraryRepository;
import com.kawai.repositories.TourRepository;
import com.kawai.repositories.TourScheduleRepository;
import com.kawai.services.impl.TourServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UC08 - Tour Core Data CRUD (TourService)")
public class TourServiceUC08Test {

    @Mock
    private TourRepository tourRepository;

    @Mock
    private TourItineraryRepository tourItineraryRepository;

    @Mock
    private TourScheduleRepository tourScheduleRepository;

    @InjectMocks
    private TourServiceImpl tourService;

    @Test
    @DisplayName("TC-UC08-001 | Update Itinerary thay thế toàn bộ thành công")
    void testUpdateItineraries_Success() {
        Tour tour = new Tour();
        tour.setId(1L);

        TourItinerary newItin1 = new TourItinerary();
        newItin1.setDayNumber(1);
        TourItinerary newItin2 = new TourItinerary();
        newItin2.setDayNumber(2);
        List<TourItinerary> newItineraries = Arrays.asList(newItin1, newItin2);

        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));

        tourService.updateItineraries(1L, newItineraries);

        // Verify old records are deleted
        verify(tourItineraryRepository, times(1)).deleteByTourId(1L);
        // Verify new records are saved
        verify(tourItineraryRepository, times(2)).save(any(TourItinerary.class));
    }

    @Test
    @DisplayName("TC-UC08-002 | Chặn sửa Tour đang có lịch mở bán (Schedule Active)")
    void testUpdateTour_ActiveSchedules_ThrowsException() {
        Tour tour = new Tour();
        tour.setId(2L);

        TourSchedule activeSchedule = new TourSchedule();
        activeSchedule.setTour(tour);
        activeSchedule.setScheduleStatus("Open");

        when(tourScheduleRepository.findAll()).thenReturn(Collections.singletonList(activeSchedule));

        assertThrows(IllegalStateException.class, () -> {
            tourService.updateTour(2L, new BigDecimal("1000000"));
        });
    }

    @Test
    @DisplayName("TC-UC08-003 | Soft Delete ẩn Tour khỏi hệ thống")
    void testSoftDeleteTour_Success() {
        Tour tour = new Tour();
        tour.setId(3L);
        tour.setIsActive(true);

        when(tourScheduleRepository.findAll()).thenReturn(Collections.emptyList());
        when(tourRepository.findById(3L)).thenReturn(Optional.of(tour));

        tourService.softDeleteTour(3L);

        assertFalse(tour.getIsActive());
        verify(tourRepository, times(1)).save(tour);
    }
}
