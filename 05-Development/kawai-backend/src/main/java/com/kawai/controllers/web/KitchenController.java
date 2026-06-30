package com.kawai.controllers.web;

import com.kawai.services.interfaces.PosWebFacadeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@RequestMapping("/kitchenStaff")
@PreAuthorize("hasAnyAuthority('OP_FNB', 'OP_FNB_ORDER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_FB_STAFF')")
public class KitchenController {

    @Autowired
    private PosWebFacadeService posWebFacadeService;

    @GetMapping("/emenu")
    @PreAuthorize("hasAnyAuthority('OP_FNB_ORDER', 'OP_FNB', 'ROLE_ADMIN', 'ROLE_MANAGER')")
    public String emenu(Model model) {
        model.addAttribute("menuItems", posWebFacadeService.getMappedMenuItems());
        return "kitchenStaff/e-menu";
    }

    @GetMapping({"/kitchen", "/dashboard", ""})
    @PreAuthorize("hasAnyAuthority('OP_DASHBOARD', 'ROLE_ADMIN', 'ROLE_MANAGER')")
    public String kitchenDashboard(Model model) {
        model.addAllAttributes(posWebFacadeService.getKitchenData());
        return "kitchenStaff/kitchen-dashboard";
    }
}
