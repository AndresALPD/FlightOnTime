package com.flightontime.dto;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor  // Permite crear el objeto con 'new' y ayuda a Jackson
@AllArgsConstructor // Permite al Builder funcionar correctamente y ser público
public class FlightDelayStatsResponseDto {
    private String fecha;
    private long totalVuelos;
    private long vuelosRetrasados;
    private long vuelosATiempo;
    private double porcentajeRetrasados;
    private double porcentajePuntuales;
}