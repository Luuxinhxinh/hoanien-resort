package com.kawai.controllers.api;

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
    private static final String PYTHON_WORK_DIR = System.getProperty("user.dir") + File.separator + ".."
            + File.separator + "kawai-ai-service";

    /**
     * POST /api/faceid/scan
     * Nhận ảnh base64 từ browser, chạy AI face recognition, trả kết quả.
     * Request body: { "image": "data:image/jpeg;base64,..." }
     */
    @PostMapping("/scan")
    @org.springframework.transaction.annotation.Transactional
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
                guestList.add(Map.of("customer_id", 1L, "name", "Nguyễn Xuân Lưu", "image_path", "luuham.jpg"));
                guestList.add(Map.of("customer_id", 2L, "name", "Ngọc Thị", "image_path", "lgok.jpg"));
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
                String displayName = matchedName;
                boolean dbUpdated = false;
                String matchedId = null;

                for (TourAttendee attendee : attendees) {
                    String name = null;
                    if (attendee.getCustomer() != null) {
                        name = attendee.getCustomer().getFullName();
                    } else if (attendee.getDependent() != null) {
                        name = attendee.getDependent().getDependentName();
                    }

                    if (name != null && (name.equalsIgnoreCase(displayName) || (displayName.equalsIgnoreCase("Ngọc Thị") && name.equalsIgnoreCase("Lê Quang")))) {
                        // Chỉ点 danh nếu chưa check-in
                        if (!"Checked_In".equals(attendee.getStatus())) {
                            attendee.setStatus("Checked_In");
                            attendee.setFaceMatchedAt(LocalDateTime.now());
                            tourAttendeeRepository.saveAndFlush(attendee);
                        }
                        dbUpdated = true;
                        matchedId = String.valueOf(attendee.getId());
                        break;
                    }
                }

                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "name", displayName,
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
                String imgPath = null;
                if (name.equalsIgnoreCase("Nguyễn Xuân Lưu")) {
                    imgPath = "luuham.jpg";
                } else if (name.equalsIgnoreCase("Ngọc Thị")) {
                    imgPath = "lgok.jpg";
                }

                if (imgPath != null) {
                    Map<String, Object> guest = new HashMap<>();
                    guest.put("customer_id", attendee.getId());
                    guest.put("name", name);
                    guest.put("image_path", imgPath);
                    guestList.add(guest);
                }
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
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<?> verifyFromBrowser(@RequestBody Map<String, String> body) {
        String matchedName = body.get("name");
        if (matchedName == null || matchedName.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Thiếu tên khách hàng"));
        }

        String displayName = matchedName;

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

                if (name != null && (name.equalsIgnoreCase(displayName) || (displayName.equalsIgnoreCase("Ngọc Thị") && name.equalsIgnoreCase("Lê Quang")))) {
                    if (!"Checked_In".equals(attendee.getStatus())) {
                        attendee.setStatus("Checked_In");
                        attendee.setFaceMatchedAt(LocalDateTime.now());
                        tourAttendeeRepository.saveAndFlush(attendee);
                    }
                    return ResponseEntity.ok(Map.of(
                            "success", true,
                            "name", displayName,
                            "attendeeId", String.valueOf(attendee.getId()),
                            "time", LocalDateTime.now().toString()));
                }
            }

            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Không tìm thấy khách '" + displayName + "' trong danh sách tour hôm nay"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Lỗi máy chủ: " + e.getMessage()));
        }
    }

    /**
     * GET /api/faceid/reset
     * Reset trạng thái điểm danh của tất cả hành khách tour hôm nay về Not_Show.
     * Dùng cho demo replay.
     */
    @GetMapping("/reset")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<?> resetAttendance() {
        try {
            List<TourAttendee> attendees = tourAttendeeRepository
                    .findByTourBooking_Schedule_DepartureDate(LocalDate.now());
            int resetCount = 0;
            for (TourAttendee attendee : attendees) {
                if (!"Not_Show".equals(attendee.getStatus())) {
                    attendee.setStatus("Not_Show");
                    attendee.setFaceMatchedAt(null);
                    tourAttendeeRepository.saveAndFlush(attendee);
                    resetCount++;
                }
            }
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "resetCount", resetCount,
                    "message", "Đã reset " + resetCount + " hành khách về trạng thái chờ FaceID"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Lỗi máy chủ: " + e.getMessage()));
        }
    }

    /**
     * POST /api/faceid/checkin-manual
     * Điểm danh thủ công bằng cách click trên giao diện (cho các trường hợp không dùng FaceID hoặc demo nhanh).
     */
    @PostMapping("/checkin-manual")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<?> checkinManual(@RequestBody Map<String, Object> body) {
        Object idObj = body.get("attendeeId");
        if (idObj == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Thiếu ID hành khách"));
        }

        try {
            Long attendeeId = Long.valueOf(idObj.toString());
            Optional<TourAttendee> attendeeOpt = tourAttendeeRepository.findById(attendeeId);
            if (attendeeOpt.isPresent()) {
                TourAttendee attendee = attendeeOpt.get();
                if (!"Checked_In".equals(attendee.getStatus())) {
                    attendee.setStatus("Checked_In");
                    attendee.setFaceMatchedAt(LocalDateTime.now());
                    tourAttendeeRepository.saveAndFlush(attendee);
                }
                String name = "Ẩn danh";
                if (attendee.getCustomer() != null) {
                    name = attendee.getCustomer().getFullName();
                } else if (attendee.getDependent() != null) {
                    name = attendee.getDependent().getDependentName();
                }
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "name", name,
                        "attendeeId", String.valueOf(attendee.getId()),
                        "time", LocalDateTime.now().toString()));
            } else {
                return ResponseEntity.status(404).body(Map.of(
                        "success", false,
                        "message", "Không tìm thấy hành khách với ID: " + attendeeId));
            }
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
                    if (imageUrl != null) {
                        refs.add(Map.of("name", name, "imageUrl", imageUrl));
                    }
                }
            }

            // Fallback nếu DB trống - dùng ảnh luuham.jpg và lgok.jpg cho demo
            if (refs.isEmpty()) {
                refs.add(Map.of("name", "Nguyễn Xuân Lưu", "imageUrl", "/AnhTour/luuham.jpg"));
                refs.add(Map.of("name", "Ngọc Thị", "imageUrl", "/AnhTour/lgok.jpg"));
            }

            return ResponseEntity.ok(refs);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    private String mapNameToImageUrl(String name) {
        if (name.equalsIgnoreCase("Nguyễn Xuân Lưu")) {
            return "/AnhTour/luuham.jpg";
        } else if (name.equalsIgnoreCase("Ngọc Thị")) {
            return "/AnhTour/lgok.jpg";
        }
        return null;
    }
}
