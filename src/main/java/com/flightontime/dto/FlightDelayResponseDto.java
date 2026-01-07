package com.flightontime.dto;

import lombok.Data;

@Data
public class FlightDelayResponseDto {

    private String airline_code;
    private Integer delay_prediction;
    private Boolean will_be_delayed;
    private Double delay_probability;
}
