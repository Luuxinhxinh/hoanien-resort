package com.kawai.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kawai.models.TourAttendee;
import com.kawai.repositories.TourAttendeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * REST API cho chức năng AI Face Scan (UC21).
 * Nhận ảnh chụp từ browser camera, gửi đến Python AI service để nhận diện,
 * cập nhật trạng thái điểm danh trong DB.
 *
 * Flow: Browser capture → POST /api/faceid/scan (base64 image) →
 * Save temp file → Call Python script → Parse result → Update DB
 */
@RestController
@RequestMapping("/api/faceid")
public class FaceIdApiController {

    @Autowired
    private TourAttendeeRepository tourAttendeeRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Thư mục tạm lưu ảnh chụp từ camera
    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir") + File.separator + "kawai-faceid";
    // Đường dẫn tới Python script nhận diện
    private static final String PYTHON_SCRIPT = "main.py";
    private static final String PYTHON_WORK_DIR = System.getProperty("user.dir") + File.separator + ".." + File.separator + "kawai-ai-service";

    /**
     * POST /api/faceid/scan
     * Nhận ảnh base64 từ browser, chạy AI face recognition, trả kết quả.
     * Request body: { "image": "data:image/jpeg;base64,..." }
     */
    @PostMapping("/scan")
    public ResponseEntity<?> scanFace(@RequestBody Map<String, String> body) {
        String base64Image = body.get("image");
        if (base64Image == null || base64Image.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Không nhận được dữ liệu ảnh"));
        }

        File tempProbeFile = null;
        try {
            // 1. Tạo thư mục tạm nếu chưa có
            File tempDir = new File(TEMP_DIR);
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }

            // 2. Decode base64 image thành file tạm
            String base64Data = base64Image;
            if (base64Data.contains(",")) {
                base64Data = base64Data.split(",")[1]; // Loại bỏ phần "data:image/jpeg;base64,"
            }
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);
            tempProbeFile = new File(tempDir, "probe_" + System.currentTimeMillis() + ".jpg");
            Files.write(tempProbeFile.toPath(), imageBytes);

            // 3. Lấy danh sách khách đi tour hôm nay
            List<TourAttendee> attendees = tourAttendeeRepository
                    .findByTourBooking_Schedule_DepartureDate(LocalDate.now());
            List<Map<String, Object>> guestList = buildGuestList(attendees);

            // Fallback nếu DB trống (môi trường test)
            if (guestList.isEmpty()) {
                guestList.add(Map.of("customer_id", 1L, "name", "Lê Hoàng Nam", "image_path", "me.jpg"));
                guestList.add(Map.of("customer_id", 2L, "name", "Nguyễn Văn An", "image_path", "ngoclon.jpg"));
            }

            String jsonArgs = objectMapper.writeValueAsString(guestList);

            // 4. Chạy Python script với probe image + guest list
            // python nhandien.py <probe_image_path> '<json_args>'
            ProcessBuilder pb = new ProcessBuilder(
                    "python", PYTHON_SCRIPT,
                    tempProbeFile.getAbsolutePath().replace("\\", "/"),
                    jsonArgs);
            pb.directory(new File(PYTHON_WORK_DIR));
            pb.redirectErrorStream(true);
            // Fix encoding UTF-8 cho Windows
            java.util.Map<String, String> env = pb.environment();
            env.put("PYTHONIOENCODING", "utf-8");
            env.put("PYTHONUTF8", "1");
            Process process = pb.start();

            // Đọc output từ Python
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
            String line;
            String matchedName = null;
            StringBuilder allOutput = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                allOutput.append(line).append("\n");
                System.out.println("[Python FaceID] " + line);
                if (line.startsWith("MATCH: ")) {
                    matchedName = line.substring(7).trim();
                }
            }

            // Chờ tối đa 30 giây
            boolean finished = process.waitFor(30, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return ResponseEntity.status(408).body(Map.of(
                        "success", false,
                        "message", "Quá thời gian quét (Timeout 30s)"));
            }

            // 5. Xử lý kết quả
            if (matchedName != null) {
                boolean dbUpdated = false;
                String matchedId = null;

                for (TourAttendee attendee : attendees) {
                    String name = null;
                    if (attendee.getCustomer() != null) {
                        name = attendee.getCustomer().getFullName();
                    } else if (attendee.getDependent() != null) {
                        name = attendee.getDependent().getDependentName();
                    }

                    if (name != null && name.equalsIgnoreCase(matchedName)) {
                        // Chỉ点 danh nếu chưa check-in
                        if (!"Checked_In".equals(attendee.getStatus())) {
                            attendee.setStatus("Checked_In");
                            attendee.setFaceMatchedAt(LocalDateTime.now());
                            tourAttendeeRepository.save(attendee);
                        }
                        dbUpdated = true;
                        matchedId = String.valueOf(attendee.getId());
                        break;
                    }
                }

                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "name", matchedName,
                        "attendeeId", matchedId != null ? matchedId : "",
                        "dbUpdated", dbUpdated,
                        "time", LocalDateTime.now().toString()));
            } else {
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "Không tìm thấy khuôn mặt khớp trong danh sách hành khách"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Lỗi máy chủ: " + e.getMessage()));
        } finally {
            // Dọn dẹp file tạm
            if (tempProbeFile != null && tempProbeFile.exists()) {
                try {
                    Files.deleteIfExists(tempProbeFile.toPath());
                } catch (IOException ignored) {
                }
            }
        }
    }

    /**
     * Xây dựng danh sách khách từ DB để gửi cho Python script.
     */
    private List<Map<String, Object>> buildGuestList(List<TourAttendee> attendees) {
        List<Map<String, Object>> guestList = new ArrayList<>();
        for (TourAttendee attendee : attendees) {
            String name = null;
            if (attendee.getCustomer() != null) {
                name = attendee.getCustomer().getFullName();
            } else if (attendee.getDependent() != null) {
                name = attendee.getDependent().getDependentName();
            }

            if (name != null) {
                Map<String, Object> guest = new HashMap<>();
                guest.put("customer_id", attendee.getId());
                guest.put("name", name);
                // Ánh xạ file ảnh tương ứng trong thư mục D:\VStudio
                if (name.equalsIgnoreCase("Lê Hoàng Nam")) {
                    guest.put("image_path", "me.jpg");
                } else if (name.equalsIgnoreCase("Nguyễn Văn An")) {
                    guest.put("image_path", "ngoclon.jpg");
                } else {
                    guest.put("image_path", "me.jpg"); // Fallback
                }
                guestList.add(guest);
            }
        }
        return guestList;
    }

    /**
     * POST /api/faceid/verify
     * Nhận kết quả nhận diện từ JavaScript (face-api.js) trên trình duyệt.
     * Frontend đã tự xử lý nhận diện và gửi tên khách khớp về server.
     * Request: { "name": "Lê Hoàng Nam" }
     */
    @PostMapping("/verify")
    public ResponseEntity<?> verifyFromBrowser(@RequestBody Map<String, String> body) {
        String matchedName = body.get("name");
        if (matchedName == null || matchedName.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Thiếu tên khách hàng"));
        }

        try {
            List<TourAttendee> attendees = tourAttendeeRepository
                    .findByTourBooking_Schedule_DepartureDate(LocalDate.now());

            for (TourAttendee attendee : attendees) {
                String name = null;
                if (attendee.getCustomer() != null) {
                    name = attendee.getCustomer().getFullName();
                } else if (attendee.getDependent() != null) {
                    name = attendee.getDependent().getDependentName();
                }

                if (name != null && name.equalsIgnoreCase(matchedName)) {
                    if (!"Checked_In".equals(attendee.getStatus())) {
                        attendee.setStatus("Checked_In");
                        attendee.setFaceMatchedAt(LocalDateTime.now());
                        tourAttendeeRepository.save(attendee);
                    }
                    return ResponseEntity.ok(Map.of(
                            "success", true,
                            "name", matchedName,
                            "attendeeId", String.valueOf(attendee.getId()),
                            "time", LocalDateTime.now().toString()));
                }
            }

            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Không tìm thấy khách '" + matchedName + "' trong danh sách tour hôm nay"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Lỗi máy chủ: " + e.getMessage()));
        }
    }

    /**
     * GET /api/faceid/references
     * Trả về danh sách ảnh tham chiếu khuôn mặt cho hôm nay (dùng cho face-api.js).
     */
    @GetMapping("/references")
    public ResponseEntity<?> getFaceReferences() {
        try {
            List<TourAttendee> attendees = tourAttendeeRepository
                    .findByTourBooking_Schedule_DepartureDate(LocalDate.now());
            List<Map<String, String>> refs = new ArrayList<>();

            for (TourAttendee attendee : attendees) {
                String name = null;
                if (attendee.getCustomer() != null) {
                    name = attendee.getCustomer().getFullName();
                } else if (attendee.getDependent() != null) {
                    name = attendee.getDependent().getDependentName();
                }
                if (name != null) {
                    String imageUrl = mapNameToImageUrl(name);
                    refs.add(Map.of("name", name, "imageUrl", imageUrl));
                }
            }

            // Fallback nếu DB trống - dùng ảnh me.jpg và ngoclon.jpg cho demo
            if (refs.isEmpty()) {
                refs.add(Map.of("name", "Lê Hoàng Nam", "imageUrl", "/AnhTour/me.jpg"));
                refs.add(Map.of("name", "Nguyễn Văn An", "imageUrl", "/AnhTour/ngoclon.jpg"));
            }

            return ResponseEntity.ok(refs);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    private String mapNameToImageUrl(String name) {
        if (name.equalsIgnoreCase("Lê Hoàng Nam")) {
            return "/AnhTour/me.jpg";
        } else if (name.equalsIgnoreCase("Nguyễn Văn An")) {
            return "/AnhTour/ngoclon.jpg";
        }
        // Fallback: dung anh me.jpg cho tat ca
        return "/AnhTour/me.jpg";
    }
}