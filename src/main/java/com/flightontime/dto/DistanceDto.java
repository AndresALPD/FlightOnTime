package com.flightontime.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DistanceDto {

    private Integer distance;
    private Integer taxi_out;
}
