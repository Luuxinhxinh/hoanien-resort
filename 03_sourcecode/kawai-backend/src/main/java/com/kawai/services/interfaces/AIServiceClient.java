package com.kawai.services.interfaces;

import org.springframework.web.multipart.MultipartFile;

public interface AIServiceClient {
    double verifyFaceMatch(MultipartFile image, Long attendeeId);
}
