package com.flightontime.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "predicciones_vuelos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrediccionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String airlineCode;
    private String origin;
    private String dest;
    private LocalDate flightDate;
    private Long flightHora;
    private Double probabilidadRetraso;
    private String retrasado;
    private String nivelRiesgo;
    private String mensaje;
    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

}