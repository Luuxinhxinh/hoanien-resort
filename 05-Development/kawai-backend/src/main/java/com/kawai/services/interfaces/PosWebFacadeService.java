package com.kawai.services.interfaces;

import java.util.List;
import java.util.Map;

public interface PosWebFacadeService {
    Map<String, Object> getDashboardData();
    Map<String, Object> getCreateFoodOrderData();
    Map<String, Object> getTableManagementData();
    List<Map<String, Object>> getMappedMenuItems();
    Map<String, Object> getOrderDetailData(String idParam);
}
