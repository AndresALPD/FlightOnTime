package com.flightontime.service;

import com.flightontime.dto.WeatherCityDto;
import com.flightontime.dto.WeatherResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class WeatherService {

    @Value("${weather.api.key}")
    private String apiKey;

    @Value("${weather.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public WeatherResponseDto getWeather(String city) {

        String url = String.format(
                "%s?q=%s&units=metric&appid=%s",
                apiUrl,
                city,
                apiKey
        );

        Map<String, Object> response =
                restTemplate.getForObject(url, Map.class);

        Map<String, Object> main =
                (Map<String, Object>) response.get("main");

        Map<String, Object> wind =
                (Map<String, Object>) response.get("wind");

        Map<String, Object> weather =
                ((java.util.List<Map<String, Object>>) response.get("weather")).get(0);

        WeatherResponseDto dto = new WeatherResponseDto();
        dto.setCity(city);
        dto.setTemperature((Double) main.get("temp"));
        dto.setHumidity((Integer) main.get("humidity"));
        dto.setDescription((String) weather.get("description"));
        dto.setWindSpeed(((Number) wind.get("speed")).doubleValue());

        return dto;
    }

    public List<WeatherCityDto> searchCities(String query) {

        String url = String.format(
                "https://api.openweathermap.org/geo/1.0/direct?q=%s&limit=5&appid=%s",
                query,
                apiKey
        );

        List<Map<String, Object>> response =
                restTemplate.getForObject(url, List.class);

        return response.stream().map(item -> {
            WeatherCityDto dto = new WeatherCityDto();
            dto.setName((String) item.get("name"));
            dto.setCountry((String) item.get("country"));
            dto.setLat(((Number) item.get("lat")).doubleValue());
            dto.setLon(((Number) item.get("lon")).doubleValue());
            return dto;
        }).toList();
    }

    public WeatherResponseDto getWeatherByCoordinates(
            Double lat,
            Double lon
    ) {

        String url = String.format(
                "%s?lat=%s&lon=%s&units=metric&appid=%s",
                apiUrl,
                lat,
                lon,
                apiKey
        );

        Map<String, Object> response =
                restTemplate.getForObject(url, Map.class);

        Map<String, Object> main =
                (Map<String, Object>) response.get("main");

        Map<String, Object> wind =
                (Map<String, Object>) response.get("wind");

        Map<String, Object> weather =
                ((java.util.List<Map<String, Object>>) response.get("weather")).get(0);

        WeatherResponseDto dto = new WeatherResponseDto();
        dto.setTemperature((Double) main.get("temp"));
        dto.setHumidity((Integer) main.get("humidity"));
        dto.setDescription((String) weather.get("description"));
        dto.setWindSpeed(((Number) wind.get("speed")).doubleValue());

        return dto;
    }


}
