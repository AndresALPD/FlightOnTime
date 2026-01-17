package com.flightontime.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class DestinoId implements Serializable {

    private String airlineCode;
    private String origin;
    private String dest;
}
