package com.kawai.controllers.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/receptionist/remote-scan")
public class RemoteScanWebController {

    @GetMapping
    public String showRemoteScanPage(@RequestParam(name = "session", required = false) String sessionId, Model model) {
        model.addAttribute("sessionId", sessionId);
        return "receptionist/remote-scan";
    }
}
