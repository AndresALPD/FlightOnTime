package com.flightontime.dto;

import lombok.Data;

@Data
public class FlightDelayResponseDto {

    private String aerolinea_codigo;
    private String aerolinea_nombre;
    private String retrasado;
    private Double probabilidad_retraso;
    private String nivel_riesgo;
    private String mensaje;
}