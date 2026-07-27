package com.hoanien.services.interfaces;

import com.hoanien.models.Review;

public interface ReviewService {
    Review submitTourReview(Long customerId, Long tourBookingId, Integer rating, String reviewText);
    Review moderateReview(Long reviewId, Long adminId, String newStatus, String reason);
}
