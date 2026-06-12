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

    public TourController(TourService tourService) {
        this.tourService = tourService;
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