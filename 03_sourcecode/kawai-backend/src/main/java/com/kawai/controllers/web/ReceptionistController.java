package com.kawai.controllers.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/receptionist")
public class ReceptionistController {

    @GetMapping("/dashboard")
    public String dashboard() {
        return "receptionist/dashboard";
    }

    @GetMapping("/reservations")
    public String reservations() {
        return "receptionist/reservations";
    }

    @GetMapping("/check-in-out")
    public String checkInOut() {
        return "receptionist/check-in-out";
    }

    @GetMapping("/folio")
    public String folio() {
        return "receptionist/folio";
    }

    @GetMapping("/night-audit")
    public String nightAudit() {
        return "receptionist/night-audit";
    }
}
