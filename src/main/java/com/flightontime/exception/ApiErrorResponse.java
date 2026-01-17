package com.flightontime.exception;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class ApiErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;

    private Map<String, String> errors;

}
