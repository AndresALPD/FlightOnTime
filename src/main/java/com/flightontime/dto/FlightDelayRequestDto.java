package com.flightontime.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

@Data
public class FlightDelayRequestDto {

    @NotBlank(message = "La aerolínea es obligatoria")
    private String aerolinea;

    @NotNull(message = "La hora de salida es obligatoria")
    @Min(value = 0, message = "La hora de salida debe estar entre 0 y 23")
    @Max(value = 23, message = "La hora de salida debe estar entre 0 y 23")
    private Integer hora_salida;

    @NotNull(message = "El día de la semana es obligatorio")
    @Min(value = 1, message = "El día de la semana debe estar entre 1 (Lunes) y 7 (Domingo)")
    @Max(value = 7, message = "El día de la semana debe estar entre 1 (Lunes) y 7 (Domingo)")
    private Integer dia_semana;

    @NotNull(message = "La distancia del vuelo es obligatoria")
    @Min(value = 1, message = "La distancia del vuelo debe ser mayor a 0 km")
    private Double distancia_km;

    @Min(value = 0, message = "El tiempo de taxi-out no puede ser negativo")
    private Integer taxi_out = 15;

    @Min(value = 0, message = "El campo es_finde solo puede ser 0 (No) o 1 (Sí)")
    @Max(value = 1, message = "El campo es_finde solo puede ser 0 (No) o 1 (Sí)")
    private Integer es_finde = 0;
}
