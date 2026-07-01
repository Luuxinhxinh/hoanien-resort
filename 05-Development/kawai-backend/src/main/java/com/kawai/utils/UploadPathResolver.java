package com.kawai.utils;

import java.io.File;

/**
 * Tiện ích giải quyết đường dẫn lưu file tải lên (Upload) trên môi trường Dev.
 * Tự động phát hiện nếu chạy Spring Boot từ thư mục gốc của dự án (SWP391 workspace)
 * để chuyển hướng lưu file vào đúng phân hệ 05-Development/kawai-backend.
 */
public class UploadPathResolver {

    private static final String WORKSPACE_PREFIX = "05-Development/kawai-backend/";

    public static String resolvePath(String relativePath) {
        if (relativePath == null) {
            return "";
        }
        
        // Kiểm tra xem process Java có đang được chạy từ thư mục gốc của workspace hay không
        File workspaceDir = new File(WORKSPACE_PREFIX);
        if (workspaceDir.exists() && workspaceDir.isDirectory()) {
            return WORKSPACE_PREFIX + relativePath;
        }
        
        return relativePath;
    }
}
