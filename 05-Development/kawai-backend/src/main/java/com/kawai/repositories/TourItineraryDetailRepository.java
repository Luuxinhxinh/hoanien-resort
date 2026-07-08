package com.kawai.repositories;

import com.kawai.models.TourItineraryDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TourItineraryDetailRepository extends JpaRepository<TourItineraryDetail, Long> {
    List<TourItineraryDetail> findByItineraryTourId(Long tourId);
}
