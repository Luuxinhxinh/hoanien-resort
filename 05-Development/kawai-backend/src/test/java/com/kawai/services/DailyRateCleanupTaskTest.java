package com.kawai.services;

import com.kawai.repositories.DailyRateRepository;
import com.kawai.services.impl.DailyRateCleanupTask;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DailyRateCleanupTask Scheduler Unit Test")
public class DailyRateCleanupTaskTest {

    @Mock
    private DailyRateRepository dailyRateRepository;

    @InjectMocks
    private DailyRateCleanupTask dailyRateCleanupTask;

    @Test
    @DisplayName("Should invoke deleteByRateDateBefore with a date of 30 days ago")
    void testCleanupOldRates_Success() {
        dailyRateCleanupTask.cleanupOldRates();

        LocalDate cutOffDate = LocalDate.now().minusDays(30);
        verify(dailyRateRepository, times(1)).deleteByRateDateBefore(cutOffDate);
    }
}
