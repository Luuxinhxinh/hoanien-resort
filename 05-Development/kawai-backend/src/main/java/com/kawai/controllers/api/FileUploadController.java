package com.kawai.controllers.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/upload")
public class FileUploadController {

    private static final String UPLOAD_DIR = "src/main/resources/static/uploads/";

    @PostMapping
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "File rỗng"));
            }
            
            // Tạo thư mục nếu chưa tồn tại
            File directory = new File(UPLOAD_DIR);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Sinh tên file ngẫu nhiên (UUID) để không bao giờ bị trùng tên
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") ? 
                               originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String newFilename = UUID.randomUUID().toString() + extension;
            
            // Lưu file vật lý vào ổ cứng
            Path filePath = Paths.get(UPLOAD_DIR + newFilename);
            Files.write(filePath, file.getBytes());
            
            // Trả về đường dẫn ảo để HTML đọc được
            String fileUrl = "/uploads/" + newFilename;
            return ResponseEntity.ok(Map.of("url", fileUrl));
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Không thể lưu file: " + e.getMessage()));
        }
    }
}
