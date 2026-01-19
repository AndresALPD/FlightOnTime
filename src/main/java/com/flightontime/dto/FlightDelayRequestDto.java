package com.flightontime.dto;

/*import com.flightontime.validation.ValidWeekend;*/
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.FutureOrPresent;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
/*@ValidWeekend*/
public class FlightDelayRequestDto {

    @NotBlank(message = "La aerolínea es obligatoria")
     private String aerolinea;

    @NotNull(message = "El aeropuerto de origen es obligatorio")
    private String origen;

    @NotNull(message = "El aeropuerto de destino es obligatorio")
    private String destino;


    @NotNull(message = "La hora de salida es obligatoria")
    @Min(value = 0, message = "La hora de salida debe estar entre 0 y 23")
    @Max(value = 23, message = "La hora de salida debe estar entre 0 y 23")
    private Integer hora_salida;

    @NotNull(message = "La fecha de salida del vuelo es obligatorio")
    /*@PastOrPresent(message = "La fecha de salida no puede ser mayor a la fecha actual")
    @JsonFormat(pattern = "yyyy-MM-dd")*/
    private LocalDate fecha_salida;

    @NotNull(message = "La distancia del vuelo es obligatoria")
    @Min(value = 1, message = "La distancia del vuelo debe ser mayor a 0 km")
    @Max(value = 15500, message = "La distancia maxima entre dos aeropuertos es 15,500 km")
    private Double distancia_km;

    @Min(value = 0, message = "El tiempo de taxi-out no puede ser negativo")
    private Integer taxi_out = 15;



    public String getAerolinea() {
        return aerolinea;
    }

    public void setAerolinea(String aerolinea) {
        this.aerolinea = aerolinea;
    }


    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }


    public Integer getHoraSalida() {
        return hora_salida;
    }

    public void setHoraSalida(Integer hora_salida) {
        this.hora_salida = hora_salida;
    }

    public LocalDate getFecha_salida() {
        return fecha_salida;
    }

    public void setFecha_salida(LocalDate fecha_salida) {
        this.fecha_salida = fecha_salida;
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
