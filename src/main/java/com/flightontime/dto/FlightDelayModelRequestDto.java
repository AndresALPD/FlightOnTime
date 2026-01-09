package com.flightontime.dto;

import lombok.Data;

@Data
public class FlightDelayModelRequestDto {

    private String aerolinea;
    private Integer hora_salida;
    private Integer dia_semana;
    private Integer es_finde;
    private Double distancia_km;
    private Integer taxi_out;
}
