package com.kawai.services.impl;

import com.kawai.dto.WeatherInfo;
import com.kawai.services.interfaces.WeatherApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.time.LocalDate;import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Real implementation of WeatherApiClient using wttr.in.
 */
@Component
public class WeatherApiClientImpl implements WeatherApiClient {
    private static final Logger LOG = LoggerFactory.getLogger(WeatherApiClientImpl.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public WeatherInfo getWeatherForDate(LocalDate date) {
        return getWeatherForDateAndLocation(date, "Hue");
    }

    @Override
    public WeatherInfo getWeatherForDateAndLocation(LocalDate date, String location) {
        LOG.info("WeatherApiClientImpl: Fetching real weather info from wttr.in for {} in {}", date, location);
        try {
            String queryLocation = (location != null && !location.trim().isEmpty()) ? location.trim() : "Hue";
            if ("Thap Muoi".equalsIgnoreCase(queryLocation)) {
                queryLocation = "Dong Thap";
            }
            // Sanitize query string for URL (e.g. "Quang Nam" -> "Quang+Nam")
            queryLocation = java.net.URLEncoder.encode(queryLocation, java.nio.charset.StandardCharsets.UTF_8.toString());

            String url = "https://wttr.in/" + queryLocation + "?format=j1";
            String response = restTemplate.getForObject(url, String.class);
            if (response != null) {
                JsonNode root = mapper.readTree(response);
                JsonNode currentCondition = root.path("current_condition").get(0);
                if (currentCondition != null && !currentCondition.isMissingNode()) {
                    String desc = currentCondition.path("weatherDesc").get(0).path("value").asText("Unknown");
                    double temp = currentCondition.path("temp_C").asDouble(0.0);
                    return new WeatherInfo(desc, temp);
                }
            }
        } catch (Exception e) {
            LOG.error("Failed to fetch real weather from wttr.in for location {}: {}", location, e.getMessage());
        }
        // Fallback
        return new WeatherInfo("Partly Cloudy", 28.5);
    }
}
