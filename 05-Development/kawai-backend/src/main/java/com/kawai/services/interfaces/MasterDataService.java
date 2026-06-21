package com.kawai.services.interfaces;

import java.util.Map;

public interface MasterDataService {
    Map<String, Object> createEntity(String entityType, Map<String, Object> payload) throws Exception;
    Map<String, Object> updateEntity(String entityType, String id, Map<String, Object> payload) throws Exception;
    void deleteEntity(String entityType, String id) throws Exception;
    void toggleEntityStatus(String entityType, String id, Boolean newStatus) throws Exception;
}
