package com.flightontime.controller;

import com.flightontime.dto.FlightRequest;
import com.flightontime.dto.PredictionResponse;
import com.flightontime.service.PredictionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class PredictionController {

    @Autowired
    private PredictionService predictionService;

    @PostMapping("/predict")
    public ResponseEntity<PredictionResponse> predict(@Valid @RequestBody FlightRequest request) {
        PredictionResponse response = predictionService.predict(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "FlightOnTime API");
        response.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("nombre", "FlightOnTime API");
        info.put("version", "1.0.0");
        info.put("descripcion", "API para predicción de retrasos de vuelos");

        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("POST /api/predict", "Realiza predicción de retraso");
        endpoints.put("GET /api/health", "Estado del servicio");
        endpoints.put("GET /api/info", "Información del API");

        info.put("endpoints", endpoints);

        return ResponseEntity.ok(info);
    }
}