package com.flightontime.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ApiErrorResponse> handleExternalServiceException(
            ExternalServiceException ex,
            HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse();
        error.setTimestamp(LocalDateTime.now());
        error.setStatus(HttpStatus.BAD_GATEWAY.value());
        error.setError("Bad Gateway");
        error.setMessage(ex.getMessage());
        error.setPath(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse();
        error.setTimestamp(LocalDateTime.now());
        error.setStatus(HttpStatus.BAD_REQUEST.value());
        error.setError("Bad Request");

        // Construir mensaje con todos los errores
        StringBuilder messageBuilder = new StringBuilder();
        ex.getBindingResult().getFieldErrors().forEach(fieldError -> {
            messageBuilder
                    .append(fieldError.getField())
                    .append(": ")
                    .append(fieldError.getDefaultMessage())
                    .append("; ");
        });

        error.setMessage(messageBuilder.toString());
        error.setPath(request.getRequestURI());

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(AerolineaNoEncontradaException.class)
    public ResponseEntity<ApiErrorResponse> handleAerolineaNoEncontrada(
            AerolineaNoEncontradaException ex,
            HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse();
        error.setTimestamp(LocalDateTime.now());
        error.setStatus(HttpStatus.BAD_REQUEST.value());
        error.setError("Bad Request");
        error.setMessage(ex.getMessage());
        error.setPath(request.getRequestURI());

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse();
        error.setTimestamp(LocalDateTime.now());
        error.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        error.setError("Internal Server Error");
        error.setMessage("Error inesperado en el servidor");
        error.setPath(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleJsonParseError(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse();
        error.setTimestamp(LocalDateTime.now());
        error.setStatus(HttpStatus.BAD_REQUEST.value());
        error.setError("Bad Request");
        error.setMessage("Formato de fecha inválido. Use yyyy-MM-dd");
        error.setPath(request.getRequestURI());

        return ResponseEntity.badRequest().body(error);
    }
}
