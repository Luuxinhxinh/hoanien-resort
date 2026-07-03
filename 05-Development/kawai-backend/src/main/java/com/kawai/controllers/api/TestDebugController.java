package com.kawai.controllers.api;
import com.kawai.models.TableReservation;
import com.kawai.repositories.TableReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
public class TestDebugController {
    @Autowired private TableReservationRepository repo;
    @GetMapping("/api/v1/debug/res-20")
    public Object debug() {
        return repo.findById(20L).orElse(null);
    }
}
