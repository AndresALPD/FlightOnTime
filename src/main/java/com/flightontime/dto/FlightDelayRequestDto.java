package com.flightontime.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

@Data
public class FlightDelayRequestDto {

    @NotBlank
    private String aerolinea;

    @NotNull
    @Min(0)
    @Max(23)
    private Integer hora_salida;

    @NotNull
    @Min(1)
    @Max(7)
    private Integer dia_semana;

    @NotNull
    @Min(1)
    private Double distancia_km;

    @Min(0)
    private Integer taxi_out = 15;

    @Min(0)
    @Max(1)
    private Integer es_finde = 0;
}
