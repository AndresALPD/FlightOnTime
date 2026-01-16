package com.flightontime.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "destinos")
@IdClass(DestinoId.class)
public class Destino {

    @Id
    @Column(name = "airline_code")
    private String airlineCode;

    @Id
    @Column(name = "origin")
    private String origin;

    @Id
    @Column(name = "dest")
    private String dest;

    @Column(name = "airline")
    private String airline;

    @Column(name = "origin_city")
    private String originCity;

    @Column(name = "dest_city")
    private String destCity;

    @Column(name = "taxi_out")
    private Integer taxiOut;

    @Column(name = "distance")
    private Integer distance;
}
