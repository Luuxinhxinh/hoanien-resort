package com.kawai.controllers.api;

import com.kawai.dto.BookingInfoDto;
import com.kawai.repositories.BookingInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingInfoApiController {

    @Autowired
    private BookingInfoRepository bookingInfoRepository;

    @GetMapping("/recent")
    public ResponseEntity<List<BookingInfoDto>> recentBookings() {
        List<BookingInfoDto> recent = bookingInfoRepository.findRecentBookings();
        return ResponseEntity.ok(recent);
    }
}
