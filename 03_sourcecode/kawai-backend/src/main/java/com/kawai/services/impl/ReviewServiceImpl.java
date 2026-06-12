package com.kawai.services.impl;

import com.kawai.models.Customer;
import com.kawai.models.Review;
import com.kawai.models.TourBooking;
import com.kawai.repositories.CustomerRepository;
import com.kawai.repositories.ReviewRepository;
import com.kawai.repositories.TourBookingRepository;
import com.kawai.services.interfaces.ReviewService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Service xử lý các nghiệp vụ liên quan đến Đánh giá (Review) dịch vụ.
 */
@Service
public class ReviewServiceImpl implements ReviewService {

    private static final String STATUS_COMPLETED = "Completed";
    private static final String STATUS_PENDING_MODERATION = "Pending";
    private static final int REVIEW_EXPIRATION_DAYS = 7; // BR-TR-03

    private final ReviewRepository reviewRepository;
    private final CustomerRepository customerRepository;
    private final TourBookingRepository tourBookingRepository;
    private final com.kawai.repositories.EmployeeRepository employeeRepository;

    public ReviewServiceImpl(ReviewRepository reviewRepository, CustomerRepository customerRepository, TourBookingRepository tourBookingRepository, com.kawai.repositories.EmployeeRepository employeeRepository) {
        this.reviewRepository = reviewRepository;
        this.customerRepository = customerRepository;
        this.tourBookingRepository = tourBookingRepository;
        this.employeeRepository = employeeRepository;
    }

    /**
     * Khách hàng gửi đánh giá cho Tour đã đi (UC22).
     *
     * @param customerId    ID của khách hàng
     * @param tourBookingId ID của đơn đặt tour
     * @param rating        Điểm đánh giá (1-5 sao)
     * @param reviewText    Nội dung đánh giá
     * @return Entity Review vừa tạo
     * @throws IllegalArgumentException nếu điều kiện kinh doanh không thoả mãn
     */
    @Override
    public Review submitTourReview(Long customerId, Long tourBookingId, Integer rating, String reviewText) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        TourBooking booking = tourBookingRepository.findById(tourBookingId)
                .orElseThrow(() -> new IllegalArgumentException("TourBooking not found"));

        validateBookingOwnership(booking, customerId);
        validateTourIsCompleted(booking);
        validateReviewExpiration(booking);

        Review newReview = new Review();
        newReview.setCustomer(customer);
        newReview.setTourBooking(booking);
        newReview.setRatingService(rating);
        newReview.setReviewText(reviewText);
        newReview.setModerationStatus(STATUS_PENDING_MODERATION);

        return reviewRepository.save(newReview);
    }

    private void validateBookingOwnership(TourBooking booking, Long customerId) {
        if (!booking.getCustomer().getId().equals(customerId)) {
            throw new IllegalArgumentException("Customer does not own this booking");
        }
    }

    private void validateTourIsCompleted(TourBooking booking) {
        if (!STATUS_COMPLETED.equalsIgnoreCase(booking.getBookingStatus())) {
            throw new IllegalArgumentException("Tour is not completed yet");
        }
    }

    private void validateReviewExpiration(TourBooking booking) {
        if (booking.getSchedule() != null && booking.getSchedule().getDepartureDate() != null) {
            LocalDate departureDate = booking.getSchedule().getDepartureDate();
            if (LocalDate.now().isAfter(departureDate.plusDays(REVIEW_EXPIRATION_DAYS))) {
                throw new IllegalArgumentException("Review period has expired (7 days limit)");
            }
        }
    }

    @Override
    public Review moderateReview(Long reviewId, Long adminId, String newStatus, String reason) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        com.kawai.models.Employee admin = employeeRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found"));

        review.setModerationStatus(newStatus);
        review.setModerationReason(reason);
        review.setModeratedBy(admin);

        return reviewRepository.save(review);
    }
}
