package com.kawai.services.interfaces;

import com.kawai.dto.TourSearchResult;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface cho UC19: Tìm kiếm gói tour.
 *
 * <p>
 * Định nghĩa hợp đồng nghiệp vụ tìm kiếm tour khả dụng kèm thông tin thời tiết.
 * Tuân thủ nguyên tắc Interface Segregation (ISP) — chỉ expose đúng 1 method
 * cần thiết.
 *
 * <p>
 * Business Rules áp dụng:
 * <ul>
 * <li>BR-TR-01: Chỉ hiển thị lịch trình có trạng thái "Open"</li>
 * <li>Graceful degradation: API thời tiết lỗi → tour vẫn hiển thị, ẩn
 * weather</li>
 * </ul>
 *
 * @see com.kawai.services.impl.TourServiceImpl
 * @see com.kawai.dto.TourSearchResult
 */
public interface TourService {

    /**
     * Tìm kiếm các tour khả dụng trong khoảng ngày {@code [fromDate, toDate]}.
     *
     * <p>
     * Mỗi kết quả bao gồm thông tin tour cơ bản + thông tin thời tiết từ
     * OpenWeather API.
     * Nếu API thời tiết không phản hồi hoặc trả về lỗi, tour vẫn được trả về với
     * {@code weatherAvailable = false} (graceful degradation).
     *
     * @param fromDate ngày bắt đầu tìm kiếm (không được null)
     * @param toDate   ngày kết thúc tìm kiếm (không được null, phải >= fromDate)
     * @return danh sách {@link TourSearchResult} (không null, có thể rỗng)
     * @throws IllegalArgumentException nếu fromDate null, toDate null, hoặc
     *                                  fromDate > toDate
     */
    List<TourSearchResult> searchAvailableTours(LocalDate fromDate, LocalDate toDate);
}