package com.kawai;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.kawai.repositories.TourScheduleRepository;
import com.kawai.models.TourSchedule;
import java.util.List;

@SpringBootTest
public class CheckDbTest {

    @Autowired
    private TourScheduleRepository tourScheduleRepository;

    @Test
    public void testCount() {
        System.out.println("DEBUG_COUNT: " + tourScheduleRepository.count());
        List<TourSchedule> schedules = tourScheduleRepository.findAll();
        for (TourSchedule s : schedules) {
            System.out.println("SCHEDULE_ID: " + s.getId() + " - " + (s.getTour() != null ? s.getTour().getTourName() : "No Tour"));
        }
    }
}
