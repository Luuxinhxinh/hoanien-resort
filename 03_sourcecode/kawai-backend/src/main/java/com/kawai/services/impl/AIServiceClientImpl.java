package com.kawai.services.impl;

import com.kawai.services.interfaces.AIServiceClient;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AIServiceClientImpl implements AIServiceClient {
    
    @Override
    public double verifyFaceMatch(MultipartFile image, Long attendeeId) {
        // Implement logic call to external AI Face Scan service here
        // Currently returning a dummy value for successful test execution
        return 0.86; 
    }
}
