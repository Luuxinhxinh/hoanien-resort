package com.kawai.controllers.web;

import com.kawai.repositories.HotelServiceRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminAddOnController {
    private final HotelServiceRepository hotelServiceRepository;

    public AdminAddOnController(HotelServiceRepository hotelServiceRepository) {
        this.hotelServiceRepository = hotelServiceRepository;
    }

    @PreAuthorize("hasAnyAuthority('OP_MASTER_DATA', 'ROLE_ADMIN')")
    @GetMapping("/addons")
    public String addons(Model model) {
        model.addAttribute("services", hotelServiceRepository.findAll());
        return "admin/addons";
    }
}
