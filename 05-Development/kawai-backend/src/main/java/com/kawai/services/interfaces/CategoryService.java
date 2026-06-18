package com.kawai.services.interfaces;

import com.kawai.models.RoomCategory;
import java.math.BigDecimal;

public interface CategoryService {
    void deleteCategory(Long id);
    RoomCategory updateCategory(Long id, BigDecimal newBasePrice);
}
