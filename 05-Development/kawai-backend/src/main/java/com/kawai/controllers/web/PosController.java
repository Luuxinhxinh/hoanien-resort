package com.kawai.controllers.web;

import com.kawai.services.interfaces.PosWebFacadeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@RequestMapping("/fbStaff")
@PreAuthorize("hasAnyAuthority('OP_FNB', 'ROLE_ADMIN', 'ROLE_MANAGER')")
public class PosController {

    @Autowired
    private PosWebFacadeService posWebFacadeService;

    @GetMapping({"/dashboard", ""})
    public String dashboard(Model model) {
        model.addAllAttributes(posWebFacadeService.getDashboardData());
        return "f&bStaff/pos-dashboard";
    }

    @GetMapping("/create-food-order")
    public String createFoodOrder(Model model) {
        model.addAllAttributes(posWebFacadeService.getCreateFoodOrderData());
        return "f&bStaff/create-food-order";
    }

    @GetMapping("/table-management")
    public String tableManagement(Model model) {
        model.addAllAttributes(posWebFacadeService.getTableManagementData());
        return "f&bStaff/table-management";
    }

    @GetMapping("/room-service")
    public String roomService() {
        return "f&bStaff/room-service-management";
    }

    @GetMapping("/room-service-detail")
    public String roomServiceDetail() {
        return "f&bStaff/room-service-detail";
    }



    @GetMapping("/shift-report")
    public String shiftReport() {
        return "f&bStaff/shift-report";
    }

    @GetMapping("/order-detail")
    public String orderDetail(@RequestParam("id") String idParam, Model model) {
        try {
            model.addAllAttributes(posWebFacadeService.getOrderDetailData(idParam));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "f&bStaff/order-detail";
    }


}
