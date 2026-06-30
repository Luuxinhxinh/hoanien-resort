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
@PreAuthorize("hasAnyAuthority('OP_FNB', 'OP_FNB_ORDER', 'OP_FNB_TABLE', 'OP_FNB_ROOM_SERVICE', 'OP_FNB_REPORT', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_FB_STAFF')")
public class PosController {

    @Autowired
    private PosWebFacadeService posWebFacadeService;

    @GetMapping({"/dashboard", ""})
    @PreAuthorize("hasAnyAuthority('OP_DASHBOARD', 'ROLE_ADMIN', 'ROLE_MANAGER')")
    public String dashboard(Model model) {
        model.addAllAttributes(posWebFacadeService.getDashboardData());
        return "f&bStaff/pos-dashboard";
    }

    @GetMapping("/create-food-order")
    @PreAuthorize("hasAnyAuthority('OP_FNB', 'OP_FNB_ORDER', 'ROLE_ADMIN', 'ROLE_MANAGER')")
    public String createFoodOrder(Model model) {
        model.addAllAttributes(posWebFacadeService.getCreateFoodOrderData());
        return "f&bStaff/create-food-order";
    }

    @GetMapping("/table-management")
    @PreAuthorize("hasAnyAuthority('OP_FNB', 'OP_FNB_TABLE', 'ROLE_ADMIN', 'ROLE_MANAGER')")
    public String tableManagement(Model model) {
        model.addAllAttributes(posWebFacadeService.getTableManagementData());
        return "f&bStaff/table-management";
    }

    @GetMapping("/room-service")
    @PreAuthorize("hasAnyAuthority('OP_FNB', 'OP_FNB_ROOM_SERVICE', 'ROLE_ADMIN', 'ROLE_MANAGER')")
    public String roomService(Model model) {
        model.addAllAttributes(posWebFacadeService.getRoomServiceManagementData());
        return "f&bStaff/room-service-management";
    }

    @GetMapping("/room-service-detail")
    @PreAuthorize("hasAnyAuthority('OP_FNB', 'OP_FNB_ROOM_SERVICE', 'ROLE_ADMIN', 'ROLE_MANAGER')")
    public String roomServiceDetail() {
        return "f&bStaff/room-service-detail";
    }



    @GetMapping("/shift-report")
    @PreAuthorize("hasAnyAuthority('OP_FNB', 'OP_FNB_REPORT', 'ROLE_ADMIN', 'ROLE_MANAGER')")
    public String shiftReport() {
        return "f&bStaff/shift-report";
    }

    @GetMapping("/order-detail")
    @PreAuthorize("hasAnyAuthority('OP_FNB', 'OP_FNB_ORDER', 'OP_FNB_TABLE', 'OP_FNB_ROOM_SERVICE', 'ROLE_ADMIN', 'ROLE_MANAGER')")
    public String orderDetail(@RequestParam("id") String idParam, Model model) {
        try {
            model.addAllAttributes(posWebFacadeService.getOrderDetailData(idParam));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "f&bStaff/order-detail";
    }


}
