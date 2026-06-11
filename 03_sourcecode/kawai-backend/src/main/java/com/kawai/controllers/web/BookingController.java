package com.kawai.controllers.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
public class BookingController {
    
    @GetMapping({"/", "/booking"})
    public String showBookingPage(Principal principal, Model model) {
        model.addAttribute("isLoggedIn", principal != null);

        // ---- Search summary ----
        model.addAttribute("search", Map.of(
                "summary", "1 Unit, 2 Adults",
                "checkIn", "12 Jun 2026",
                "checkOut", "15 Jun 2026",
                "guestLabel", "2 adults"
        ));

        // ---- Resort info (footer) ----
        model.addAttribute("resort", Map.of(
                "name", "Namia River Retreat",
                "title", "Namia River Retreat - Wellness Inclusive Resort, Managed by Lumina Wellbeing",
                "address", "232 Tran Nhan Tong Street, Hoi An Dong Ward, Danang City, Viet Nam"
        ));

        // ---- Suggested picks & filters ----
        model.addAttribute("defaultPicks", List.of(
                "King Bed 200x200 cm", "Terrace and Loungers", "Direct River View"));
        model.addAttribute("defaultFilters", List.of(
                "Bedding", "Layout", "Location", "View"));

        // ---- Rooms ----
        model.addAttribute("rooms", List.of(
                new BookingRoom("NIPA POOL VILLA - King bed",
                        "https://cdnphoto.dantri.com.vn/dQBtl5WxjzNsef4JCGFZSlf8WRk=/2023/01/24/khoa-hocdocx-1674520013659.png",
                        "Lowest Price", "price", false,
                        2, 1, 62,
                        List.of("Outdoor shower", "Sunken Bathtub With View", "Private Pool",
                                "Nipa Palm View", "1 Bedroom Villa", "M - size: 62 sqm", "King Bed 200x200 cm"),
                        0,
                        "₫ 20,460,000.00", "-25%", "₫ 15,345,000.00", "₫ 46,035,000.00"),

                new BookingRoom("RIVER POOL VILLA - King bed",
                        "https://cdnphoto.dantri.com.vn/dQBtl5WxjzNsef4JCGFZSlf8WRk=/2023/01/24/khoa-hocdocx-1674520013659.png",
                        "Our Tip", "tip", false,
                        2, 1, 62,
                        List.of("Sunken Bathtub With View", "Private Pool", "Outdoor Shower",
                                "On Islet", "Indoor Shower", "Ensuite Bathroom", "1 Bedroom Villa"),
                        4,
                        "₫ 25,626,666.67", "-25%", "₫ 19,220,000.00", "₫ 57,660,000.00"),

                new BookingRoom("NIPA POOL VILLA - Twin bed",
                        "https://cdnphoto.dantri.com.vn/dQBtl5WxjzNsef4JCGFZSlf8WRk=/2023/01/24/khoa-hocdocx-1674520013659.png",
                        null, "price", false,
                        2, 1, 62,
                        List.of("Outdoor shower", "Twin Beds 110x200 cm", "Sunken Bathtub With View",
                                "Private Pool", "Nipa Palm View", "1 Bedroom Villa"),
                        1,
                        "₫ 20,461,033.33", "-25%", "₫ 15,345,774.67", "₫ 46,037,324.00"),

                new BookingRoom("RIVER POOL VILLA - Twin bed",
                        "https://cdnphoto.dantri.com.vn/dQBtl5WxjzNsef4JCGFZSlf8WRk=/2023/01/24/khoa-hocdocx-1674520013659.png",
                        null, "price", false,
                        2, 1, 62,
                        List.of("Twin Beds 110x200 cm", "Sunken Bathtub With View", "Private Pool",
                                "1 Bedroom Villa", "Terrace and Loungers", "M - size: 62 sqm"),
                        1,
                        "₫ 25,626,666.67", "-25%", "₫ 19,220,000.00", "₫ 57,660,000.00"),

                new BookingRoom("WELLNESS RETREATS - GENTLE RESET - 03 NIGHTS",
                        "https://cdnphoto.dantri.com.vn/dQBtl5WxjzNsef4JCGFZSlf8WRk=/2023/01/24/khoa-hocdocx-1674520013659.png",
                        null, "price", true,
                        2, 1, 62,
                        List.of("Sunken Bathtub With View", "Private Pool", "Nipa Palm View",
                                "L- size: 80 sqm", "1 Bedroom Villa", "King Bed 200x200 cm", "Retreats package"),
                        0,
                        null, null, "₫ 26,936,244.00", "₫ 80,808,732.00"),

                new BookingRoom("WELLNESS RETREATS - MINDFUL RECOVERY - 03 NIGHTS",
                        "https://cdnphoto.dantri.com.vn/dQBtl5WxjzNsef4JCGFZSlf8WRk=/2023/01/24/khoa-hocdocx-1674520013659.png",
                        null, "price", true,
                        2, 1, 62,
                        List.of("Sunken Bathtub With View", "Private Pool", "Nipa Palm View",
                                "L- size: 80 sqm", "1 Bedroom Villa", "King Bed 200x200 cm", "Retreats package"),
                        0,
                        null, null, "₫ 29,990,180.00", "₫ 89,970,540.00")
        ));

        return "guest/booking"; 
    }

    @GetMapping("/living")
    public String showLivingPage(Principal principal, Model model) {
        model.addAttribute("isLoggedIn", principal != null);
        return "guest/living";
    }

    @GetMapping("/wellbeing")
    public String showWellbeingPage(Principal principal, Model model) {
        model.addAttribute("isLoggedIn", principal != null);
        return "guest/wellbeing";
    }

    @GetMapping("/dining")
    public String showDiningPage(Principal principal, Model model) {
        model.addAttribute("isLoggedIn", principal != null);
        return "guest/dining";
    }

    @GetMapping("/experiences")
    public String showExperiencesPage(Principal principal, Model model) {
        model.addAttribute("isLoggedIn", principal != null);
        return "guest/experiences";
    }


    public static class BookingRoom {
        private final String name;
        private final String image;
        private final String badge;
        private final String badgeType;
        private final boolean hasInfo;
        private final int guests;
        private final int beds;
        private final int size;
        private final List<String> amenities;
        private final int moreCount;
        private final String originalPrice;
        private final String discount;
        private final String price;
        private final String total;

        public BookingRoom(String name, String image, String badge, String badgeType, boolean hasInfo,
                           int guests, int beds, int size, List<String> amenities, int moreCount,
                           String originalPrice, String discount, String price, String total) {
            this.name = name;
            this.image = image;
            this.badge = badge;
            this.badgeType = badgeType;
            this.hasInfo = hasInfo;
            this.guests = guests;
            this.beds = beds;
            this.size = size;
            this.amenities = amenities;
            this.moreCount = moreCount;
            this.originalPrice = originalPrice;
            this.discount = discount;
            this.price = price;
            this.total = total;
        }

        public String getName() { return name; }
        public String getImage() { return image; }
        public String getBadge() { return badge; }
        public String getBadgeType() { return badgeType; }
        public boolean isHasInfo() { return hasInfo; }
        public int getGuests() { return guests; }
        public int getBeds() { return beds; }
        public int getSize() { return size; }
        public List<String> getAmenities() { return amenities; }
        public int getMoreCount() { return moreCount; }
        public String getOriginalPrice() { return originalPrice; }
        public String getDiscount() { return discount; }
        public String getPrice() { return price; }
        public String getTotal() { return total; }
    }
}
