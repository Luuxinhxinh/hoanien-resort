package com.hoanien.services.interfaces;

import com.hoanien.models.RoomCategory;
import java.math.BigDecimal;

public interface CategoryService {
    void deleteCategory(Long id);
    RoomCategory updateCategory(Long id, BigDecimal newBasePrice);
}
