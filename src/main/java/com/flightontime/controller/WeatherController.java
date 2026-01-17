package com.flightontime.controller;

import com.flightontime.dto.WeatherCityDto;
import com.flightontime.dto.WeatherResponseDto;
import com.flightontime.service.WeatherService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/weather")
@CrossOrigin
public class WeatherController {

    private final WeatherService service;

    public WeatherController(WeatherService service) {
        this.service = service;
    }

    @GetMapping
    public WeatherResponseDto getWeather(
            @RequestParam String city
    ) {
        return service.getWeather(city);
    }

    @GetMapping("/cities")
    public List<WeatherCityDto> autocompleteCities(
            @RequestParam String q
    ) {
        return service.searchCities(q);
    }

    @GetMapping("/by-coordinates")
    public WeatherResponseDto getWeatherByCoordinates(
            @RequestParam Double lat,
            @RequestParam Double lon
    ) {
        return service.getWeatherByCoordinates(lat, lon);
    }


}