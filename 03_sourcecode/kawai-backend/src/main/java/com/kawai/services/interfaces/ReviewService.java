package com.kawai.services.interfaces;

import com.kawai.models.Review;

public interface ReviewService {
    Review submitTourReview(Long customerId, Long tourBookingId, Integer rating, String reviewText);
}
