package com.kawai.services;

import com.kawai.models.TourAttendee;
import com.kawai.repositories.TourAttendeeRepository;
import com.kawai.services.impl.TourServiceImpl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import com.kawai.services.interfaces.AIServiceClient;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ================================================================
 * KAWAI RESORT — TDD Unit Test cho Tour AI Attendance (UC21)
 * ================================================================
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UC21 — Tour AI Attendance TDD Test")
public class TourAttendanceServiceUC21Test {

    @Mock
    private TourAttendeeRepository tourAttendeeRepository;

    // Giả định có interface AIServiceClient để gọi service nhận diện khuôn mặt
    @Mock
    private AIServiceClient aiServiceClient;

    @InjectMocks
    private TourServiceImpl tourService;

    @Mock
    private MultipartFile mockImage;

    @Test
    @DisplayName("TC-M4-008: Gửi ảnh -> AI Service trả match -> Tour_Attendees cập nhật PRESENT")
    void TC_M4_008_aiFaceScanMatch() {
        Long attendeeId = 1L;
        TourAttendee attendee = new TourAttendee();
        attendee.setId(attendeeId);
        attendee.setStatus("ABSENT");

        when(tourAttendeeRepository.findById(attendeeId)).thenReturn(Optional.of(attendee));
        when(aiServiceClient.verifyFaceMatch(any(MultipartFile.class), anyLong())).thenReturn(0.98); // 98% match > 85%

        // Act
        boolean result = tourService.verifyAttendance(attendeeId, mockImage);

        // Assert
        assertTrue(result, "Verify attendance should return true for 98% match");
        assertEquals("PRESENT", attendee.getStatus(), "Status should be updated to PRESENT");
        verify(tourAttendeeRepository, times(1)).save(attendee);
    }

    @Test
    @DisplayName("TC-M4-009: AI Service không khả dụng -> cho phép điểm danh thủ công")
    void TC_M4_009_aiServiceLoiDiemDanhThuCong() {
        Long attendeeId = 2L;
        TourAttendee attendee = new TourAttendee();
        attendee.setId(attendeeId);
        attendee.setStatus("ABSENT");

        when(tourAttendeeRepository.findById(attendeeId)).thenReturn(Optional.of(attendee));
        when(aiServiceClient.verifyFaceMatch(any(MultipartFile.class), anyLong()))
            .thenThrow(new RuntimeException("AI Service Down"));

        // Act & Assert 1: Gọi AI báo lỗi
        Exception exception = assertThrows(RuntimeException.class, () -> {
            tourService.verifyAttendance(attendeeId, mockImage);
        });
        assertEquals("AI Service Down", exception.getMessage());

        // Act & Assert 2: Cho phép điểm danh thủ công
        tourService.markAttendanceManually(attendeeId, "PRESENT");
        assertEquals("PRESENT", attendee.getStatus(), "Status should be manually updated to PRESENT");
        verify(tourAttendeeRepository, times(1)).save(attendee);
    }
}
