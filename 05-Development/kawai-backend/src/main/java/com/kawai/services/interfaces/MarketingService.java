package com.kawai.services.interfaces;

import com.kawai.models.Promotion;
import java.util.Map;

public interface MarketingService {
    Promotion createCombo(String promoCode, Map<String, Object> comboData);
}
