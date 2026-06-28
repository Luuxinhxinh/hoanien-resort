package com.kawai.controllers.api;

import com.kawai.models.MenuItem;
import com.kawai.models.Promotion;
import com.kawai.repositories.FoodItemRepository;
import com.kawai.repositories.PromotionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/admin/api/v1/import")
public class ImportApiController {

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private FoodItemRepository foodItemRepository;

    @PostMapping
    public ResponseEntity<?> importCsv(@RequestParam("file") MultipartFile file, @RequestParam("tab") String tab) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("{\"message\": \"File trống.\"}");
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            int successCount = 0;
            int errorCount = 0;
            boolean isFirstLine = true;
            String[] headers = null;

            while ((line = br.readLine()) != null) {
                // Xử lý BOM UTF-8 ở đầu file nếu có
                if (isFirstLine && line.startsWith("\uFEFF")) {
                    line = line.substring(1);
                }

                if (line.trim().isEmpty()) continue;

                // Split CSV line correctly avoiding commas inside quotes
                String[] columns = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                for (int i = 0; i < columns.length; i++) {
                    columns[i] = columns[i].replaceAll("^\"|\"$", "").trim();
                }

                if (isFirstLine) {
                    headers = columns;
                    isFirstLine = false;
                    continue;
                }

                try {
                    if ("Promotions".equals(tab)) {
                        importPromotion(headers, columns);
                    } else if ("Restaurant Menu".equals(tab)) {
                        importMenuItem(headers, columns);
                    } else {
                        return ResponseEntity.badRequest().body("{\"message\": \"Chưa hỗ trợ Import cho tab: " + tab + "\"}");
                    }
                    successCount++;
                } catch (Exception ex) {
                    errorCount++;
                }
            }

            return ResponseEntity.ok(String.format("{\"message\": \"Import hoàn tất! Thành công: %d dòng, Lỗi: %d dòng.\"}", successCount, errorCount));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"message\": \"Lỗi xử lý file: " + e.getMessage() + "\"}");
        }
    }

    private void importPromotion(String[] headers, String[] columns) throws Exception {
        Promotion promo = new Promotion();
        // Set default values for not-null fields
        promo.setPromoCode("P" + System.currentTimeMillis() + (int)(Math.random() * 1000));
        promo.setDiscountType("PERCENTAGE");
        promo.setDiscountValue(BigDecimal.ZERO);
        promo.setValidFrom(LocalDateTime.now());
        promo.setValidTo(LocalDate.now().plusMonths(1));
        promo.setMaxUses(100);
        promo.setCurrentUses(0);
        promo.setIsActive(true);

        for (int i = 0; i < headers.length && i < columns.length; i++) {
            String colName = headers[i].toLowerCase();
            String value = columns[i];
            if (value == null || value.isEmpty()) continue;

            if (colName.contains("mã") || colName.contains("code")) {
                promo.setPromoCode(value);
            } else if (colName.contains("loại") || colName.contains("type")) {
                if (value.toLowerCase().contains("phần trăm") || value.toLowerCase().contains("percentage")) promo.setDiscountType("PERCENTAGE");
                else if (value.toLowerCase().contains("tiền") || value.toLowerCase().contains("amount")) promo.setDiscountType("FIXED_AMOUNT");
                else promo.setDiscountType(value);
            } else if (colName.contains("giá trị") || colName.contains("value")) {
                promo.setDiscountValue(new BigDecimal(value.replaceAll("[^0-9.]", "")));
            } else if (colName.contains("bắt đầu") || colName.contains("from")) {
                // Try parse Date, very naive for demo
                try {
                    if (value.contains("T")) promo.setValidFrom(LocalDateTime.parse(value));
                    else promo.setValidFrom(LocalDate.parse(value).atStartOfDay());
                } catch(Exception e) {}
            } else if (colName.contains("kết thúc") || colName.contains("to")) {
                try {
                    if (value.contains("T")) promo.setValidTo(LocalDateTime.parse(value).toLocalDate());
                    else promo.setValidTo(LocalDate.parse(value));
                } catch(Exception e) {}
            } else if (colName.contains("tối đa") || colName.contains("max")) {
                promo.setMaxUses(Integer.parseInt(value.replaceAll("[^0-9]", "")));
            } else if (colName.contains("mô tả") || colName.contains("description")) {
                promo.setDescription(value);
            }
        }
        
        // Prevent duplicate promo code
        try {
            promotionRepository.save(promo);
        } catch (Exception e) {
            throw new Exception("Duplicate or invalid code");
        }
    }

    private void importMenuItem(String[] headers, String[] columns) throws Exception {
        MenuItem item = new MenuItem();
        item.setItemName("New Item");
        item.setPrice(BigDecimal.ZERO);
        item.setCategory("Tráng miệng");
        item.setIsAvailable(true);

        for (int i = 0; i < headers.length && i < columns.length; i++) {
            String colName = headers[i].toLowerCase();
            String value = columns[i];
            if (value == null || value.isEmpty()) continue;

            if (colName.contains("tên") || colName.contains("name")) {
                item.setItemName(value);
            } else if (colName.contains("giá") || colName.contains("price")) {
                item.setPrice(new BigDecimal(value.replaceAll("[^0-9.]", "")));
            } else if (colName.contains("danh mục") || colName.contains("loại") || colName.contains("category")) {
                item.setCategory(value);
            } else if (colName.contains("mô tả") || colName.contains("description")) {
                item.setDescription(value);
            } else if (colName.contains("trạng thái") || colName.contains("status")) {
                if (value.toLowerCase().contains("có sẵn") || value.toLowerCase().contains("true")) item.setIsAvailable(true);
                else item.setIsAvailable(false);
            }
        }
        foodItemRepository.save(item);
    }
}
