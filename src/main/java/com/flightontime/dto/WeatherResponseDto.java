package com.flightontime.dto;

import lombok.Data;

@Data
public class WeatherResponseDto {

    private String city;
    private Double temperature;
    private String description;
    private Integer humidity;
    private Double windSpeed;
}
