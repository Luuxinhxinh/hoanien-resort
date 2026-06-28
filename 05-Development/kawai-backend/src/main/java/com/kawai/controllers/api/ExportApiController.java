package com.kawai.controllers.api;

import com.kawai.services.interfaces.AdminViewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/api/v1/export")
public class ExportApiController {

    @Autowired
    private AdminViewService adminViewService;

    @GetMapping(value = "/csv", produces = "text/csv")
    public ResponseEntity<byte[]> exportToCsv(@RequestParam(defaultValue = "Account Management") String tab) {
        try {
            List<Map<String, String>> rows = adminViewService.getMasterDataRows(tab);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // Thêm BOM (Byte Order Mark) để Excel đọc được tiếng Việt (UTF-8)
            baos.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});

            PrintWriter pw = new PrintWriter(baos, true, StandardCharsets.UTF_8);

            if (rows != null && !rows.isEmpty()) {
                // Header (Lấy key của Map đầu tiên làm Header)
                Map<String, String> firstRow = rows.get(0);
                StringBuilder headerLine = new StringBuilder();
                for (String key : firstRow.keySet()) {
                    if (key.equals("jsonString") || key.startsWith("__")) continue; // Bỏ qua dữ liệu kĩ thuật
                    headerLine.append("\"").append(key).append("\",");
                }
                if (headerLine.length() > 0) headerLine.setLength(headerLine.length() - 1);
                pw.println(headerLine.toString());

                // Data
                for (Map<String, String> row : rows) {
                    StringBuilder dataLine = new StringBuilder();
                    for (Map.Entry<String, String> entry : row.entrySet()) {
                        String key = entry.getKey();
                        if (key.equals("jsonString") || key.startsWith("__")) continue;
                        
                        String value = entry.getValue() != null ? entry.getValue() : "";
                        // Xử lý dấu nháy kép trong CSV
                        value = value.replace("\"", "\"\"");
                        dataLine.append("\"").append(value).append("\",");
                    }
                    if (dataLine.length() > 0) dataLine.setLength(dataLine.length() - 1);
                    pw.println(dataLine.toString());
                }
            }

            pw.flush();
            byte[] csvBytes = baos.toByteArray();

            String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = tab.replaceAll("\\s+", "_") + "_Export_" + timeStamp + ".csv";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", filename);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return new ResponseEntity<>(csvBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
