package com.kawai.controllers.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;

import com.kawai.repositories.RoomCategoryRepository;
import com.kawai.models.RoomCategory;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class BookingController {

    @Autowired
    private RoomCategoryRepository roomCategoryRepository;
    
    @GetMapping("/")
    public String showHomePage() {
        return "redirect:/living";
    }

    @GetMapping("/booking")
    public String showBookingPage(Principal principal, Model model, @RequestParam(name = "keyword", required = false) String keyword) {
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
                "name", "Hoanien River Retreat",
                "title", "Hoanien River Retreat - Wellness Inclusive Resort, Managed by Lumina Wellbeing",
                "address", "232 Tran Nhan Tong Street, Hoi An Dong Ward, Danang City, Viet Nam"
        ));

        // ---- Fetch Rooms from DB ----
        List<RoomCategory> categories = roomCategoryRepository.findAll();
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.toLowerCase();
            categories = categories.stream()
                .filter(c -> c.getCategoryName().toLowerCase().contains(kw) || 
                             (c.getDescription() != null && c.getDescription().toLowerCase().contains(kw)))
                .collect(Collectors.toList());
        }

        List<String> tempImages = List.of(
            "https://images.unsplash.com/photo-1582719478250-c89400652e71?w=800&q=80",
            "https://images.unsplash.com/photo-1618773928121-c32242e63f39?w=800&q=80",
            "https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=800&q=80",
            "https://images.unsplash.com/photo-1566665797739-1674de7a421a?w=800&q=80",
            "https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800&q=80"
        );

        List<BookingRoom> rooms = categories.stream().map(cat -> {
            long priceVal = cat.getBasePrice() != null ? cat.getBasePrice().longValue() : 0L;
            String priceStr = cat.getBasePrice() != null ? String.format("₫ %,d", cat.getBasePrice().longValue()) : "Contact Us";
            int capacity = cat.getCapacity() != null ? cat.getCapacity() : 2;
            String image = tempImages.get((int)(cat.getId() != null ? cat.getId() % tempImages.size() : 0));
            
            return new BookingRoom(
                cat.getCategoryName(),
                image,
                null, null, false, // no badges
                capacity, 
                1, // beds
                40, // size
                List.of(), // no amenities mapped yet
                0,
                null, null, priceStr, priceStr,
                priceVal
            );
        }).collect(Collectors.toList());

        model.addAttribute("rooms", rooms);
        model.addAttribute("keyword", keyword);

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
        private final long priceVal;

        public BookingRoom(String name, String image, String badge, String badgeType, boolean hasInfo,
                           int guests, int beds, int size, List<String> amenities, int moreCount,
                           String originalPrice, String discount, String price, String total,
                           long priceVal) {
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
            this.priceVal = priceVal;
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
        public long getPriceVal() { return priceVal; }
    }
}
