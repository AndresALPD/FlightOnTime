package com.flightontime.exception;

public class AerolineaNoEncontradaException extends RuntimeException {

    public AerolineaNoEncontradaException(String message) {
        super(message);
    }
}