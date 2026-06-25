package com.kawai.controllers.web;

import com.kawai.services.interfaces.PosWebFacadeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/kitchenStaff")
public class KitchenController {

    @Autowired
    private PosWebFacadeService posWebFacadeService;

    @GetMapping("/emenu")
    public String emenu(Model model) {
        model.addAttribute("menuItems", posWebFacadeService.getMappedMenuItems());
        return "kitchenStaff/e-menu";
    }

    @GetMapping({"/kitchen", "/dashboard", ""})
    public String kitchenDashboard(Model model) {
        model.addAllAttributes(posWebFacadeService.getKitchenData());
        return "kitchenStaff/kitchen-dashboard";
    }
}
