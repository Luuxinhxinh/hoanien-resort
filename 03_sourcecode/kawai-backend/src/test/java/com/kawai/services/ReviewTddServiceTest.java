package com.kawai.services;

import com.kawai.models.Customer;
import com.kawai.models.Employee;
import com.kawai.models.Review;
import com.kawai.models.TourBooking;
import com.kawai.models.TourSchedule;
import com.kawai.repositories.CustomerRepository;
import com.kawai.repositories.EmployeeRepository;
import com.kawai.repositories.ReviewRepository;
import com.kawai.repositories.TourBookingRepository;
import com.kawai.services.impl.ReviewServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewTddServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private TourBookingRepository tourBookingRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    @Test
    @DisplayName("TC-M4-010: Khách hàng gửi đánh giá 1-5 sao và nội dung thành công")
    void TC_M4_010_submitTourReview_Success() {
        // Arrange
        Long customerId = 1L;
        Long tourBookingId = 100L;
        Integer rating = 5;
        String reviewText = "Tour rất tuyệt vời, HDV nhiệt tình!";

        Customer mockCustomer = new Customer();
        mockCustomer.setId(customerId);

        TourSchedule mockSchedule = new TourSchedule();
        mockSchedule.setDepartureDate(LocalDate.now().minusDays(2));

        TourBooking mockBooking = new TourBooking();
        mockBooking.setId(tourBookingId);
        mockBooking.setCustomer(mockCustomer);
        mockBooking.setBookingStatus("Completed"); // Khách đã sử dụng dịch vụ
        mockBooking.setSchedule(mockSchedule);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(mockCustomer));
        when(tourBookingRepository.findById(tourBookingId)).thenReturn(Optional.of(mockBooking));
        
        Review savedReview = new Review();
        savedReview.setId(1L);
        savedReview.setCustomer(mockCustomer);
        savedReview.setTourBooking(mockBooking);
        savedReview.setRatingService(rating);
        savedReview.setReviewText(reviewText);
        savedReview.setModerationStatus("Pending");

        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);

        // Act
        Review result = reviewService.submitTourReview(customerId, tourBookingId, rating, reviewText);

        // Assert
        assertNotNull(result);
        assertEquals(5, result.getRatingService());
        assertEquals(reviewText, result.getReviewText());
        assertEquals("Pending", result.getModerationStatus());

        ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(reviewCaptor.capture());
        
        Review capturedReview = reviewCaptor.getValue();
        assertEquals(customerId, capturedReview.getCustomer().getId());
        assertEquals(tourBookingId, capturedReview.getTourBooking().getId());
    }

    @Test
    @DisplayName("TC-M4-011: Khách hàng chưa sử dụng dịch vụ không được phép đánh giá")
    void TC_M4_011_submitTourReview_NotUsedService_Fail() {
        // Arrange
        Long customerId = 1L;
        Long tourBookingId = 101L;
        Integer rating = 4;
        String reviewText = "Chưa đi nhưng đánh giá trước";

        Customer mockCustomer = new Customer();
        mockCustomer.setId(customerId);

        TourBooking mockBooking = new TourBooking();
        mockBooking.setId(tourBookingId);
        mockBooking.setCustomer(mockCustomer);
        mockBooking.setBookingStatus("CONFIRMED"); // Đang chờ đi, chưa sử dụng (Completed)

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(mockCustomer));
        when(tourBookingRepository.findById(tourBookingId)).thenReturn(Optional.of(mockBooking));

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            reviewService.submitTourReview(customerId, tourBookingId, rating, reviewText);
        });

        assertTrue(ex.getMessage().contains("chưa hoàn thành") || ex.getMessage().contains("not completed") || ex.getMessage().contains("must be Completed"));
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("TC-M4-012: Admin ẩn đánh giá toxic/spam thành công")
    void TC_M4_012_moderateReview_AdminHidesToxicReview_Success() {
        // Arrange
        Long reviewId = 1L;
        Long adminId = 99L;
        String newStatus = "Hidden";
        String reason = "Ngôn từ thô tục";

        Review mockReview = new Review();
        mockReview.setId(reviewId);
        mockReview.setModerationStatus("Pending");

        Employee mockAdmin = new Employee();
        mockAdmin.setId(adminId);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(mockReview));
        when(employeeRepository.findById(adminId)).thenReturn(Optional.of(mockAdmin));
        
        Review moderatedReview = new Review();
        moderatedReview.setId(reviewId);
        moderatedReview.setModerationStatus(newStatus);
        moderatedReview.setModeratedBy(mockAdmin);
        moderatedReview.setModerationReason(reason);

        when(reviewRepository.save(any(Review.class))).thenReturn(moderatedReview);

        // Act
        Review result = reviewService.moderateReview(reviewId, adminId, newStatus, reason);

        // Assert
        assertNotNull(result);
        assertEquals(newStatus, result.getModerationStatus());
        assertEquals(reason, result.getModerationReason());
        assertNotNull(result.getModeratedBy());
        assertEquals(adminId, result.getModeratedBy().getId());

        ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(reviewCaptor.capture());
        
        Review capturedReview = reviewCaptor.getValue();
        assertEquals(newStatus, capturedReview.getModerationStatus());
        assertEquals(reason, capturedReview.getModerationReason());
        assertEquals(adminId, capturedReview.getModeratedBy().getId());
    }
}
