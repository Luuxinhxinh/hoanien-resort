package com.kawai.controllers.web;

import com.kawai.dto.TourSearchResult;
import com.kawai.services.interfaces.TourService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

/**
 * Web Controller cho UC19: Tìm kiếm gói tour.
 *
 * <p>
 * Hiển thị trang tìm kiếm tour cho Guest / Customer.
 * Controller chỉ làm nhiệm vụ Routing (điều hướng request, kiểm tra quyền cơ
 * bản)
 * và gọi {@link TourService}. Hoàn toàn không chứa Business Logic.
 *
 * <p>
 * Nguyên tắc Clean Code:
 * <ul>
 * <li>Thin Controller: Method duy nhất, mỗi dòng code đều có mục đích rõ
 * ràng</li>
 * <li>Magic Numbers: Default search range (7 days) khai báo hằng</li>
 * <li>Self-documenting: Tên param gợi nhớ, không comment thừa</li>
 * </ul>
 */
@Controller
@RequestMapping("/tours")
public class TourController {

    private static final int DEFAULT_SEARCH_DAYS = 7;

    private final TourService tourService;
    private final com.kawai.repositories.CustomerRepository customerRepository;
    private final com.kawai.repositories.TourRepository tourRepository;
    private final com.kawai.services.interfaces.WeatherApiClient weatherApiClient;
    private final com.kawai.repositories.TourItineraryDetailRepository tourItineraryDetailRepository;
    private final com.kawai.repositories.DependentRepository dependentRepository;
    private final com.kawai.repositories.RoomBookingDetailRepository roomBookingDetailRepository;
    private final com.kawai.repositories.RoomGuestRepository roomGuestRepository;

    public TourController(TourService tourService,
            com.kawai.repositories.CustomerRepository customerRepository,
            com.kawai.repositories.TourRepository tourRepository,
            com.kawai.services.interfaces.WeatherApiClient weatherApiClient,
            com.kawai.repositories.TourItineraryDetailRepository tourItineraryDetailRepository,
            com.kawai.repositories.DependentRepository dependentRepository,
            com.kawai.repositories.RoomBookingDetailRepository roomBookingDetailRepository,
            com.kawai.repositories.RoomGuestRepository roomGuestRepository) {
        this.tourService = tourService;
        this.customerRepository = customerRepository;
        this.tourRepository = tourRepository;
        this.weatherApiClient = weatherApiClient;
        this.tourItineraryDetailRepository = tourItineraryDetailRepository;
        this.dependentRepository = dependentRepository;
        this.roomBookingDetailRepository = roomBookingDetailRepository;
        this.roomGuestRepository = roomGuestRepository;
    }

    @GetMapping
    public String defaultToursRedirect() {
        return "redirect:/tours/search";
    }

    /**
     * Hiển thị danh sách tour khả dụng theo khoảng ngày.
     *
     * <p>
     * Nếu không có tham số {@code fromDate} / {@code toDate}, tự động tìm kiếm
     * trong 7 ngày tới kể từ hôm nay.
     *
     * @param fromDate ngày bắt đầu (ISO format: yyyy-MM-dd, optional)
     * @param toDate   ngày kết thúc (ISO format: yyyy-MM-dd, optional)
     * @param model    Spring MVC model để truyền dữ liệu sang Thymeleaf view
     * @return tên view template "guest/tours"
     */
    @GetMapping("/search")
    public String searchTours(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            java.security.Principal principal,
            Model model) {

        LocalDate effectiveFrom = resolveFromDate(fromDate);
        LocalDate effectiveTo = resolveToDate(toDate, effectiveFrom);

        List<TourSearchResult> tours = tourService.searchAvailableTours(effectiveFrom, effectiveTo);

        model.addAttribute("isLoggedIn", principal != null);
        model.addAttribute("tours", tours);
        model.addAttribute("fromDate", effectiveFrom);
        model.addAttribute("toDate", effectiveTo);

        return "guest/tours";
    }

    /**
     * Hiển thị chi tiết tour du lịch (Đoàn tụ, Đồng nội, Di sản, Tĩnh lặng)
     * trong một trang duy nhất.
     */
    @GetMapping("/detail")
    public String tourDetail(
            @RequestParam(name = "type", defaultValue = "doantu") String type,
            java.security.Principal principal,
            Model model) {
        model.addAttribute("isLoggedIn", principal != null);
        model.addAttribute("type", type);

        com.kawai.models.Tour tour = tourRepository.findFirstByTourTypeAndIsActiveTrueOrderByIdDesc(type).orElse(null);
        if (tour == null) {
            tour = tourRepository.findByTourType(type).orElse(null);
        }
        if (tour != null) {
            model.addAttribute("dynamicPrice", tour.getBasePrice());
        }

        String[] tourTypes = { "doantu", "dongnoi", "disan", "tinhlang", "halong", "sapa", "cattien", "muine",
                "phuquoc", "cantho" };
        for (String tType : tourTypes) {
            com.kawai.models.Tour activeTour = tourRepository.findFirstByTourTypeAndIsActiveTrueOrderByIdDesc(tType)
                    .orElse(null);
            if (activeTour == null) {
                activeTour = tourRepository.findByTourType(tType).orElse(null);
            }
            if (activeTour != null) {
                model.addAttribute("dbTour_" + tType, activeTour);
                // Truyền thông tin bảo hiểm theo từng loại tour để JS đọc
                model.addAttribute("insurance_required_" + tType,
                        Boolean.TRUE.equals(activeTour.getIsInsuranceRequired()));
                model.addAttribute("insurance_price_" + tType,
                        activeTour.getInsurancePrice() != null ? activeTour.getInsurancePrice()
                                : java.math.BigDecimal.ZERO);
            }
        }

        // Fetch detailed activities for all 10 tours and format as JSON
        java.util.Map<String, java.util.List<java.util.Map<String, Object>>> dbTourActivitiesMap = new java.util.HashMap<>();
        for (String tType : tourTypes) {
            com.kawai.models.Tour activeTour = tourRepository.findFirstByTourTypeAndIsActiveTrueOrderByIdDesc(tType)
                    .orElse(null);
            if (activeTour == null) {
                activeTour = tourRepository.findByTourType(tType).orElse(null);
            }
            if (activeTour != null) {
                java.util.List<com.kawai.models.TourItineraryDetail> details = tourItineraryDetailRepository
                        .findByItineraryTourId(activeTour.getId());
                java.util.List<java.util.Map<String, Object>> list = new java.util.ArrayList<>();
                for (com.kawai.models.TourItineraryDetail d : details) {
                    java.util.Map<String, Object> map = new java.util.HashMap<>();
                    String timeStr = "";
                    if (d.getStartTime() != null) {
                        timeStr = d.getStartTime().toString().substring(0, 5);
                        if (d.getEndTime() != null) {
                            timeStr += " - " + d.getEndTime().toString().substring(0, 5);
                        }
                    }
                    map.put("time", timeStr);
                    map.put("title", d.getActivityTitle());
                    map.put("desc", d.getActivityDescription());
                    list.add(map);
                }
                dbTourActivitiesMap.put(tType, list);
            }
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String dbTourActivitiesJson = mapper.writeValueAsString(dbTourActivitiesMap);
            model.addAttribute("dbTourActivitiesJson", dbTourActivitiesJson);
        } catch (Exception e) {
            model.addAttribute("dbTourActivitiesJson", "{}");
            e.printStackTrace();
        }

        if (principal != null) {
            String username = principal.getName();
            if (principal instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) {
                org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken oauthToken = (org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) principal;
                username = oauthToken.getPrincipal().getAttribute("email");
            }
            java.util.Optional<com.kawai.models.Customer> customerOpt = customerRepository
                    .findByAccount_Username(username);
            customerOpt.ifPresent(customer -> {
                model.addAttribute("customerName", customer.getFullName());
                model.addAttribute("customerEmail", customer.getEmail());
                model.addAttribute("customerPhone", customer.getPhone());
                model.addAttribute("customerCccd",
                        customer.getCccdPassportEncrypted() != null
                                ? com.kawai.utils.EncryptionUtils.decrypt(customer.getCccdPassportEncrypted())
                                : "");

                List<com.kawai.models.Dependent> deps = dependentRepository.findByCustomer(customer).stream()
                        .filter(d -> d.getIsDeleted() == null || !d.getIsDeleted())
                        .collect(java.util.stream.Collectors.toList());
                model.addAttribute("userDependents", deps);
                try {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    List<java.util.Map<String, Object>> depMapList = new java.util.ArrayList<>();
                    java.util.Set<String> addedNames = new java.util.HashSet<>();

                    // 0. Luôn đưa Customer (người đặt) vào đầu danh sách
                    String cusName = customer.getFullName();
                    if (cusName == null || cusName.trim().isEmpty()) {
                        cusName = "Người đặt phòng (Bạn)";
                    }
                    
                    java.util.Map<String, Object> mc = new java.util.HashMap<>();
                    mc.put("id", "cust_" + customer.getId());
                    mc.put("name", cusName.trim());
                    mc.put("phone", customer.getPhone() != null ? customer.getPhone() : "");
                    mc.put("cccd", customer.getCccdPassportEncrypted() != null ? com.kawai.utils.EncryptionUtils.decrypt(customer.getCccdPassportEncrypted()) : "");
                    mc.put("gender", customer.getGender() != null ? customer.getGender() : "");
                    mc.put("relationship", "Người đặt phòng");
                    int customerAge = 18;
                    if (customer.getBirthDate() != null) {
                        customerAge = java.time.Period.between(customer.getBirthDate(), java.time.LocalDate.now()).getYears();
                    }
                    mc.put("age", customerAge);
                    depMapList.add(mc);
                    addedNames.add(cusName.trim().toLowerCase());

                    // 1. Uu tien danh sach khach luu tru trong phong hien tai (RoomGuests)
                    if (customer.getAccount() != null) {
                        List<com.kawai.models.RoomBookingDetail> activeDetails = roomBookingDetailRepository
                                .findActiveDetailsByUserId(customer.getAccount().getId());
                        for (com.kawai.models.RoomBookingDetail rbd : activeDetails) {
                            List<com.kawai.models.RoomGuest> roomGuests = roomGuestRepository
                                    .findByRoomBookingDetailId(rbd.getId());
                            for (com.kawai.models.RoomGuest rg : roomGuests) {
                                if (rg.getCustomer() != null) {
                                    com.kawai.models.Customer c = rg.getCustomer();
                                    String cName = c.getFullName() != null ? c.getFullName().trim() : "";
                                    if (!cName.isEmpty() && !addedNames.contains(cName.toLowerCase())) {
                                        java.util.Map<String, Object> m = new java.util.HashMap<>();
                                        m.put("id", "cust_" + c.getId());
                                        m.put("name", cName);
                                        m.put("phone", c.getPhone() != null ? c.getPhone() : "");
                                        m.put("cccd", c.getCccdPassportEncrypted() != null ? com.kawai.utils.EncryptionUtils.decrypt(c.getCccdPassportEncrypted()) : "");
                                        m.put("gender", c.getGender() != null ? c.getGender() : "");
                                        m.put("relationship", c.getId().equals(customer.getId()) ? "Người đặt phòng" : "Khách cùng phòng");
                                        int guestAge = 18;
                                        if (c.getBirthDate() != null) {
                                            guestAge = java.time.Period.between(c.getBirthDate(), java.time.LocalDate.now()).getYears();
                                        }
                                        m.put("age", guestAge);
                                        depMapList.add(m);
                                        addedNames.add(cName.toLowerCase());
                                    }
                                } else if (rg.getDependent() != null) {
                                    com.kawai.models.Dependent d = rg.getDependent();
                                    String dName = d.getDependentName() != null ? d.getDependentName().trim() : "";
                                    boolean isAnonymous = "Khách đi kèm".equalsIgnoreCase(dName);
                                    if (!dName.isEmpty() && (isAnonymous || !addedNames.contains(dName.toLowerCase()))) {
                                        java.util.Map<String, Object> m = new java.util.HashMap<>();
                                        m.put("id", d.getId());
                                        m.put("name", isAnonymous ? "Khách đi kèm (Chưa có tên)" : dName);
                                        m.put("phone", "");
                                        m.put("cccd", d.getCccdPassportEncrypted() != null ? com.kawai.utils.EncryptionUtils.decrypt(d.getCccdPassportEncrypted()) : "");
                                        m.put("gender", d.getGender() != null ? d.getGender() : "");
                                        m.put("relationship", "Khách cùng phòng");
                                        int dependentAge = 18;
                                        if (d.getBirthDate() != null) {
                                            dependentAge = java.time.Period.between(d.getBirthDate(), java.time.LocalDate.now()).getYears();
                                        }
                                        m.put("age", dependentAge);
                                        depMapList.add(m);
                                        if (!isAnonymous) {
                                            addedNames.add(dName.toLowerCase());
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 2. Them cac Dependents khac (chua duoc load vao) tu danh sach phu thuoc cua Customer
                    for (com.kawai.models.Dependent d : deps) {
                        if (d == null) continue;
                        String dName = d.getDependentName() != null ? d.getDependentName().trim() : "";
                        if (dName.isEmpty() || "Khách đi kèm".equalsIgnoreCase(dName) || addedNames.contains(dName.toLowerCase())) continue;

                        java.util.Map<String, Object> m = new java.util.HashMap<>();
                        m.put("id", d.getId());
                        m.put("name", dName);
                        m.put("phone", "");
                        m.put("cccd", d.getCccdPassportEncrypted() != null ? com.kawai.utils.EncryptionUtils.decrypt(d.getCccdPassportEncrypted()) : "");
                        m.put("gender", d.getGender() != null ? d.getGender() : "");
                        m.put("relationship", "Người thân");
                        int otherDepAge = 18;
                        if (d.getBirthDate() != null) {
                            otherDepAge = java.time.Period.between(d.getBirthDate(), java.time.LocalDate.now()).getYears();
                        }
                        m.put("age", otherDepAge);
                        depMapList.add(m);
                        addedNames.add(dName.toLowerCase());
                    }

                    model.addAttribute("userDependentsJson", mapper.writeValueAsString(depMapList));
                } catch (Exception e) {
                    model.addAttribute("userDependentsJson", "[]");
                }
            });
        } else {
            model.addAttribute("userDependentsJson", "[]");
        }
        return "guest/tour-detail";
    }

    /**
     * API lấy thông tin thời tiết thực tế từ wttr.in cho giao diện.
     */
    @GetMapping("/api/weather")
    @org.springframework.web.bind.annotation.ResponseBody
    public com.kawai.dto.WeatherInfo getWeather(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false, defaultValue = "Hue") String location) {
        if (date == null)
            date = LocalDate.now();
        return weatherApiClient.getWeatherForDateAndLocation(date, location);
    }

    /**
     * Trả về {@code fromDate} nếu được cung cấp, ngược lại trả về hôm nay.
     */
    private LocalDate resolveFromDate(LocalDate fromDate) {
        return (fromDate != null) ? fromDate : LocalDate.now();
    }

    /**
     * Trả về {@code toDate} nếu được cung cấp, ngược lại trả về
     * {@code fromDate + DEFAULT_SEARCH_DAYS}.
     */
    private LocalDate resolveToDate(LocalDate toDate, LocalDate fromDate) {
        return (toDate != null) ? toDate : fromDate.plusDays(DEFAULT_SEARCH_DAYS);
    }
}