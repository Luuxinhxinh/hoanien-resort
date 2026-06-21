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

    public TourController(TourService tourService, 
                          com.kawai.repositories.CustomerRepository customerRepository, 
                          com.kawai.repositories.TourRepository tourRepository,
                          com.kawai.services.interfaces.WeatherApiClient weatherApiClient) {
        this.tourService = tourService;
        this.customerRepository = customerRepository;
        this.tourRepository = tourRepository;
        this.weatherApiClient = weatherApiClient;
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

        String[] tourTypes = {"doantu", "dongnoi", "disan", "tinhlang"};
        for (String tType : tourTypes) {
            com.kawai.models.Tour activeTour = tourRepository.findFirstByTourTypeAndIsActiveTrueOrderByIdDesc(tType).orElse(null);
            if (activeTour == null) {
                activeTour = tourRepository.findByTourType(tType).orElse(null);
            }
            if (activeTour != null) {
                model.addAttribute("dbTour_" + tType, activeTour);
            }
        }

        if (principal != null) {
            String username = principal.getName();
            if (principal instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) {
                org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken oauthToken = 
                    (org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) principal;
                username = oauthToken.getPrincipal().getAttribute("email");
            }
            java.util.Optional<com.kawai.models.Customer> customerOpt = customerRepository.findByAccount_Username(username);
            customerOpt.ifPresent(customer -> {
                model.addAttribute("customerName", customer.getFullName());
                model.addAttribute("customerEmail", customer.getEmail());
                model.addAttribute("customerPhone", customer.getPhone());
            });
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
        if (date == null) date = LocalDate.now();
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