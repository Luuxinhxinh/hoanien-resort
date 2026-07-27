package com.hoanien.services.interfaces;

import com.hoanien.models.RestaurantTable;
import com.hoanien.models.MenuItem;

public interface TableService {
    RestaurantTable saveTable(String tableNumber, int capacity);
    RestaurantTable toggleStatus(Long id, String newStatus);
    void softDeleteTable(Long id);
    
    MenuItem createMenuItem(String itemName, java.math.BigDecimal price, String category);
    MenuItem toggleMenuAvailability(Long id, boolean isAvailable);
}
