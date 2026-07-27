package com.hoanien.repositories;

import com.hoanien.models.TourItineraryDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TourItineraryDetailRepository extends JpaRepository<TourItineraryDetail, Long> {
    List<TourItineraryDetail> findByItineraryTourId(Long tourId);
}
