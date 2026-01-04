package com.flightontime.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightRequest {

    @NotBlank(message = "La aerolínea es obligatoria")
    private String aerolinea;

    @NotBlank(message = "El origen es obligatorio")
    private String origen;

    @NotBlank(message = "El destino es obligatorio")
    private String destino;

    @NotBlank(message = "La fecha de partida es obligatoria")
    /**
     * fechaPartida se utiliza únicamente para extraer
     * hora de salida y día de la semana.
     * El modelo no consume la fecha completa.
     */
    private String fechaPartida;

    @NotNull(message = "La distancia es obligatoria")
    @Positive(message = "La distancia debe ser positiva")
    private Integer distanciaKm;
}