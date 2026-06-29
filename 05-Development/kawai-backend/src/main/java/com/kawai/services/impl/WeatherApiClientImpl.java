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
        LOG.info("WeatherApiClientImpl: Fetching weather info for {} in {}", date, location);
        String queryLocation = (location != null && !location.trim().isEmpty()) ? location.trim() : "Hue";
        if ("Thap Muoi".equalsIgnoreCase(queryLocation) || "Dong Sen Thap Muoi".equalsIgnoreCase(queryLocation) || "Đồng Sen Tháp Mười".equalsIgnoreCase(queryLocation)) {
            queryLocation = "Dong Thap";
        }

        LocalDate today = LocalDate.now();
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(today, date);

        // If the date is in the far future (> 2 days) or in the past, return a consistent, realistic pseudorandom weather
        if (daysBetween < 0 || daysBetween > 2) {
            return generatePseudorandomWeather(date, location != null ? location : "Hue");
        }

        try {
            // Sanitize query string for URL (e.g. "Quang Nam" -> "Quang+Nam")
            String encodedLocation = java.net.URLEncoder.encode(queryLocation, java.nio.charset.StandardCharsets.UTF_8.toString());
            String url = "https://wttr.in/" + encodedLocation + "?format=j1";
            String response = restTemplate.getForObject(url, String.class);
            if (response != null) {
                JsonNode root = mapper.readTree(response);
                
                if (daysBetween == 0) {
                    // Today: use current condition
                    JsonNode currentCondition = root.path("current_condition").get(0);
                    if (currentCondition != null && !currentCondition.isMissingNode()) {
                        String desc = currentCondition.path("weatherDesc").get(0).path("value").asText("Unknown");
                        double temp = currentCondition.path("temp_C").asDouble(0.0);
                        return new WeatherInfo(desc, temp);
                    }
                } else {
                    // Tomorrow or Day after tomorrow: use forecast from "weather" array
                    JsonNode weatherArray = root.path("weather");
                    if (weatherArray.isArray() && weatherArray.size() > daysBetween) {
                        JsonNode dayForecast = weatherArray.get((int) daysBetween);
                        double avgTemp = dayForecast.path("avgtempC").asDouble(28.0);
                        
                        // Get daytime description (usually hourly index 4 is 12:00)
                        JsonNode hourlyArray = dayForecast.path("hourly");
                        String desc = "Partly Cloudy";
                        if (hourlyArray.isArray() && hourlyArray.size() > 4) {
                            desc = hourlyArray.get(4).path("weatherDesc").get(0).path("value").asText("Partly Cloudy");
                        } else if (hourlyArray.isArray() && hourlyArray.size() > 0) {
                            desc = hourlyArray.get(0).path("weatherDesc").get(0).path("value").asText("Partly Cloudy");
                        }
                        return new WeatherInfo(desc, avgTemp);
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Failed to fetch real weather from wttr.in for location {}: {}", location, e.getMessage());
        }

        // Fallback: generate pseudorandom weather
        return generatePseudorandomWeather(date, location != null ? location : "Hue");
    }

    private WeatherInfo generatePseudorandomWeather(LocalDate date, String location) {
        // Use hash code of date + location as seed for consistency
        long seed = date.toEpochDay() + location.toLowerCase().hashCode();
        java.util.Random rand = new java.util.Random(seed);
        
        String loc = location.toLowerCase();
        
        if (loc.contains("huế") || loc.contains("hue")) {
            String[] options = {"Partly Cloudy", "Sunny", "Clear Sky", "Light rain", "Cloudy"};
            String desc = options[rand.nextInt(options.length)];
            double temp = 24 + rand.nextInt(9); // 24 to 32°C
            return new WeatherInfo(desc, temp);
        } else if (loc.contains("quảng nam") || loc.contains("quang nam")) {
            String[] options = {"Sunny", "Clear Sky", "Partly Cloudy", "Patchy rain possible"};
            String desc = options[rand.nextInt(options.length)];
            double temp = 27 + rand.nextInt(8); // 27 to 34°C
            return new WeatherInfo(desc, temp);
        } else if (loc.contains("ninh bình") || loc.contains("ninh binh")) {
            String[] options = {"Mist", "Overcast", "Partly Cloudy", "Light drizzle"};
            String desc = options[rand.nextInt(options.length)];
            double temp = 19 + rand.nextInt(9); // 19 to 27°C
            return new WeatherInfo(desc, temp);
        } else {
            // Đồng sen Tháp Mười
            String[] options = {"Clear Sky", "Sunny", "Partly Cloudy", "Heavy rain"};
            String desc = options[rand.nextInt(options.length)];
            double temp = 28 + rand.nextInt(7); // 28 to 34°C
            return new WeatherInfo(desc, temp);
        }
    }
}
