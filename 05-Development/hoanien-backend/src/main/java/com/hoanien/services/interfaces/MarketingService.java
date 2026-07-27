package com.hoanien.services.interfaces;

import com.hoanien.models.Promotion;
import java.util.Map;

public interface MarketingService {
    Promotion createCombo(String promoCode, Map<String, Object> comboData);
}
