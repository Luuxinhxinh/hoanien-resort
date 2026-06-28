package com.kawai.controllers.api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;
@RestController
public class DebugSqlController {
    @Autowired private JdbcTemplate jdbcTemplate;
    @GetMapping("/api/v1/debug/sql")
    public List<Map<String, Object>> debugSql(@org.springframework.web.bind.annotation.RequestParam(value="q", defaultValue="SELECT * FROM Table_Reservations") String query) {
        try {
            return jdbcTemplate.queryForList(query);
        } catch (Exception e) {
            return List.of(Map.of("error", e.getMessage()));
        }
    }
}
