package com.kawai.repositories;

import com.kawai.models.TourItinerary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TourItineraryRepository extends JpaRepository<TourItinerary, Long> {
    List<TourItinerary> findByTourId(Long tourId);
    void deleteByTourId(Long tourId);
}
