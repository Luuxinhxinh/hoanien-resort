package com.kawai.controllers.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;

import com.kawai.repositories.CustomerRepository;
import java.util.Optional;
import com.kawai.models.Customer;
import java.security.Principal;

@Controller
public class BookingController {

    @Autowired
    private CustomerRepository customerRepository;

    @GetMapping("/")
    public String showHomePage() {
        return "redirect:/living";
    }

    @GetMapping("/booking")
    public String showBookingPage(Principal principal, Model model,
            @RequestParam(name = "keyword", required = false) String keyword) {
        model.addAttribute("isLoggedIn", com.kawai.utils.SecurityUtils.isCustomerLoggedIn(principal));

        if (com.kawai.utils.SecurityUtils.isCustomerLoggedIn(principal)) {
            String username = principal.getName();
            if (principal instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) {
                org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken oauthToken = 
                    (org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) principal;
                username = oauthToken.getPrincipal().getAttribute("email");
            }
            Optional<Customer> customerOpt = customerRepository
                    .findByAccount_Username(username);
            if (customerOpt.isEmpty()) {
                customerOpt = customerRepository.findByEmail(username);
            }
            customerOpt.ifPresent(customer -> {
                model.addAttribute("customerName", customer.getFullName());
                model.addAttribute("customerEmail", customer.getEmail());
                model.addAttribute("customerPhone", customer.getPhone());
            });
        }

        model.addAttribute("keyword", keyword);

        return "guest/booking";
    }

    @GetMapping("/living")
    public String showLivingPage(Principal principal, Model model) {
        model.addAttribute("isLoggedIn", com.kawai.utils.SecurityUtils.isCustomerLoggedIn(principal));
        return "guest/living";
    }

    @GetMapping("/wellbeing")
    public String showWellbeingPage(Principal principal, Model model) {
        model.addAttribute("isLoggedIn", com.kawai.utils.SecurityUtils.isCustomerLoggedIn(principal));
        return "guest/wellbeing";
    }

    @GetMapping("/dining")
    public String showDiningPage(Principal principal, Model model) {
        model.addAttribute("isLoggedIn", com.kawai.utils.SecurityUtils.isCustomerLoggedIn(principal));
        return "guest/dining";
    }

    @GetMapping("/experiences")
    public String showExperiencesPage(Principal principal, Model model) {
        model.addAttribute("isLoggedIn", com.kawai.utils.SecurityUtils.isCustomerLoggedIn(principal));
        return "guest/experiences";
    }

    @Autowired
    private com.kawai.repositories.RestaurantTableRepository restaurantTableRepository;

    @Autowired
    private com.kawai.repositories.RoomBookingRepository roomBookingRepository;

    @Autowired
    private com.kawai.repositories.RoomBookingDetailRepository roomBookingDetailRepository;

    @Autowired
    private com.kawai.repositories.AccountRepository accountRepository;

    @Autowired
    private com.kawai.repositories.RoomRepository roomRepository;

    @GetMapping("/book-table")
    public String showTableBookingPage(Principal principal, Model model, jakarta.servlet.http.HttpSession session) {
        boolean isLoggedIn = com.kawai.utils.SecurityUtils.isCustomerLoggedIn(principal);
        model.addAttribute("isLoggedIn", isLoggedIn);

        boolean hasValidBooking = false;
        java.time.LocalDate validCheckInDate = null;
        java.time.LocalDate validCheckOutDate = null;
        String currentRoomNumber = null;

        try {
            if (isLoggedIn && principal != null) {
                com.kawai.models.Account userAccount = (com.kawai.models.Account) session.getAttribute("user");
                
                if (userAccount == null) {
                    String identifier = null;
                    if (principal instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) {
                        org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken oauthToken = 
                            (org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) principal;
                        identifier = oauthToken.getPrincipal().getAttribute("email");
                    } else {
                        identifier = principal.getName();
                    }

                    if (identifier != null) {
                        userAccount = accountRepository.findByUsername(identifier).orElse(null);
                    }
                }
                
                if (userAccount != null) {
                    java.util.List<com.kawai.models.Room> activeRooms = roomRepository.findActiveRoomsByUserId(userAccount.getId());
                    if (activeRooms != null && !activeRooms.isEmpty()) {
                        hasValidBooking = true;
                        model.addAttribute("roomOptions", activeRooms);
                        
                        // We still need validCheckInDate and validCheckOutDate for the flatpickr constraint
                        com.kawai.models.Room firstRoom = activeRooms.get(0);
                        if (firstRoom.getCurrentBookingDetailId() != null) {
                            com.kawai.models.RoomBookingDetail activeDetail = roomBookingDetailRepository.findById(firstRoom.getCurrentBookingDetailId()).orElse(null);
                            if (activeDetail != null && activeDetail.getRoomBooking() != null) {
                                validCheckInDate = activeDetail.getRoomBooking().getCheckInDate();
                                validCheckOutDate = activeDetail.getRoomBooking().getCheckOutDate();
                                java.time.LocalDate today = java.time.LocalDate.now();
                                if (validCheckOutDate != null && validCheckOutDate.isBefore(today)) {
                                    validCheckOutDate = today;
                                }
                            }
                        }
                    } else {
                        // Fallback to older check in case rooms aren't assigned yet (Pending)
                        java.util.List<com.kawai.models.RoomBookingDetail> rbds = roomBookingDetailRepository.findActiveDetailsByUserId(userAccount.getId());
                        java.time.LocalDate today = java.time.LocalDate.now();
                        com.kawai.models.RoomBookingDetail activeDetail = rbds.stream()
                                .filter(d -> d.getRoomBooking() != null)
                                .findFirst()
                                .orElse(null);

                        if (activeDetail != null) {
                            hasValidBooking = true;
                            validCheckInDate = activeDetail.getRoomBooking().getCheckInDate();
                            validCheckOutDate = activeDetail.getRoomBooking().getCheckOutDate();
                            
                            // If checkOutDate is in the past, adjust it to today so flatpickr doesn't break
                            if (validCheckOutDate != null && validCheckOutDate.isBefore(today)) {
                                validCheckOutDate = today;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            model.addAttribute("debugError", "Exception: " + e.getClass().getName() + " - " + e.getMessage());
        }
        
        model.addAttribute("hasValidBooking", hasValidBooking);
        model.addAttribute("validCheckInDate", validCheckInDate);
        model.addAttribute("validCheckOutDate", validCheckOutDate);
        model.addAttribute("currentRoomNumber", currentRoomNumber);

        java.util.List<com.kawai.models.RestaurantTable> tables = restaurantTableRepository.findAll();
        model.addAttribute("tables", tables);
        return "guest/book-table";
    }

}
