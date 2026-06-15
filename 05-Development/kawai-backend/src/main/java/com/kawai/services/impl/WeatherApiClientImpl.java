package com.kawai.services.impl;

import com.kawai.dto.WeatherInfo;
import com.kawai.services.interfaces.WeatherApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

/**
 * Mock implementation of WeatherApiClient to allow application context loading and fallback behaviour.
 */
@Component
public class WeatherApiClientImpl implements WeatherApiClient {
    private static final Logger LOG = LoggerFactory.getLogger(WeatherApiClientImpl.class);

    @Override
    public WeatherInfo getWeatherForDate(LocalDate date) {
        LOG.info("WeatherApiClientImpl: Fetching mock weather info for {}", date);
        return new WeatherInfo("Partly Cloudy", 28.5);
    }
}
