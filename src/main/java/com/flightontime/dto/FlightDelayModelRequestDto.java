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

    public String getAerolinea() {
        return aerolinea;
    }

    public void setAerolinea(String aerolinea) {
        this.aerolinea = aerolinea;
    }

    public Integer getHora_salida() {
        return hora_salida;
    }

    public void setHora_salida(Integer hora_salida) {
        this.hora_salida = hora_salida;
    }

    public Integer getDia_semana() {
        return dia_semana;
    }

    public void setDia_semana(Integer dia_semana) {
        this.dia_semana = dia_semana;
    }

    public Integer getEs_finde() {
        return es_finde;
    }

    public void setEs_finde(Integer es_finde) {
        this.es_finde = es_finde;
    }

    public Double getDistancia_km() {
        return distancia_km;
    }

    public void setDistancia_km(Double distancia_km) {
        this.distancia_km = distancia_km;
    }

    public Integer getTaxi_out() {
        return taxi_out;
    }

    public void setTaxi_out(Integer taxi_out) {
        this.taxi_out = taxi_out;
    }
}
