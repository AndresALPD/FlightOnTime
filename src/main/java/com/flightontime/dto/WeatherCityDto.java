package com.flightontime.dto;

import lombok.Data;

@Data
public class WeatherCityDto {
    private String name;
    private String country;
    private Double lat;
    private Double lon;
}
