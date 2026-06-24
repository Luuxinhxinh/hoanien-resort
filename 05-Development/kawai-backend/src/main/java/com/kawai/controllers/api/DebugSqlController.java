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
    public List<Map<String, Object>> debugSql() {
        return jdbcTemplate.queryForList("SELECT * FROM Table_Reservations");
    }
}
