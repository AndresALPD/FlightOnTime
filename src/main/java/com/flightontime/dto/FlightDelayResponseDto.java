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

    public String getAerolinea_codigo() {
        return aerolinea_codigo;
    }

    public void setAerolinea_codigo(String aerolinea_codigo) {
        this.aerolinea_codigo = aerolinea_codigo;
    }

    public String getAerolinea_nombre() {
        return aerolinea_nombre;
    }

    public void setAerolinea_nombre(String aerolinea_nombre) {
        this.aerolinea_nombre = aerolinea_nombre;
    }

    public String getRetrasado() {
        return retrasado;
    }

    public void setRetrasado(String retrasado) {
        this.retrasado = retrasado;
    }

    public Double getProbabilidad_retraso() {
        return probabilidad_retraso;
    }

    public void setProbabilidad_retraso(Double probabilidad_retraso) {
        this.probabilidad_retraso = probabilidad_retraso;
    }

    public String getNivel_riesgo() {
        return nivel_riesgo;
    }

    public void setNivel_riesgo(String nivel_riesgo) {
        this.nivel_riesgo = nivel_riesgo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}