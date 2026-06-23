package com.kawai.services.interfaces;

import java.util.Map;

public interface WorkflowEngineService {
    void triggerEvent(String eventType, Map<String, Object> payload);
}
