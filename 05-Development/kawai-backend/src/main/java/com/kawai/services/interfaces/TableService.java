package com.kawai.services.interfaces;

import com.kawai.models.RestaurantTable;
import com.kawai.models.MenuItem;

public interface TableService {
    RestaurantTable saveTable(String tableNumber, int capacity);
    RestaurantTable toggleStatus(Long id, String newStatus);
    void softDeleteTable(Long id);
    
    MenuItem createMenuItem(String itemName, java.math.BigDecimal price, String category);
    MenuItem toggleMenuAvailability(Long id, boolean isAvailable);
}
