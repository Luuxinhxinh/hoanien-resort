package com.kawai.controllers.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    /**
     * Ops login page — unified entry for admin / manager / staff / receptionist.
     */
    @GetMapping("/ops-login")
    public String opsLogin() {
        return "ops-login";
    }
}